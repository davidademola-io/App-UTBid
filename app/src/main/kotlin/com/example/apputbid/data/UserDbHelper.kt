package com.example.apputbid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class User(val id: Long, val username: String, val saltB64: String, val hashB64: String)

class UserDbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx, "app.db", null, 4) {   // ⬅️ Bumped DB version to 4

    override fun onCreate(db: SQLiteDatabase) {

        // ------------------
        // USERS TABLE
        // ------------------
        db.execSQL(
            """
            CREATE TABLE users(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL,
              salt TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              banned INTEGER NOT NULL DEFAULT 0
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_users_username ON users(username);")

        // ------------------
        // GAME RESULTS TABLE
        // ------------------
        db.execSQL(
            """
            CREATE TABLE game_results(
              game_id INTEGER PRIMARY KEY,
              home_score INTEGER NOT NULL,
              away_score INTEGER NOT NULL,
              status TEXT NOT NULL
            );
            """.trimIndent()
        )

        // ------------------
        // NEW: BET HISTORY TABLE
        // ------------------
        db.execSQL(
            """
            CREATE TABLE bet_history(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL,
              game_id INTEGER NOT NULL,
              pick TEXT NOT NULL,      -- "home" / "away"
              stake REAL NOT NULL,
              odds REAL NOT NULL,
              result TEXT NOT NULL,    -- "WIN" / "LOSS" / "PUSH"
              payout REAL NOT NULL,    -- amount credited (0, stake, or stake*odds)
              timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_bet_history_username ON bet_history(username);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN banned INTEGER NOT NULL DEFAULT 0;")
        }

        if (oldVersion < 3) {
            db.execSQL(
                """
                CREATE TABLE game_results(
                  game_id INTEGER PRIMARY KEY,
                  home_score INTEGER NOT NULL,
                  away_score INTEGER NOT NULL,
                  status TEXT NOT NULL
                );
                """.trimIndent()
            )
        }

        // NEW: Add bet_history table on upgrade to version 4
        if (oldVersion < 4) {
            db.execSQL(
                """
                CREATE TABLE bet_history(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT NOT NULL,
                  game_id INTEGER NOT NULL,
                  pick TEXT NOT NULL,
                  stake REAL NOT NULL,
                  odds REAL NOT NULL,
                  result TEXT NOT NULL,
                  payout REAL NOT NULL,
                  timestamp INTEGER NOT NULL
                );
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX idx_bet_history_username ON bet_history(username);")
        }
    }

    // ====================================================
    // USER OPERATIONS
    // ====================================================

    fun insertUser(username: String, saltB64: String, hashB64: String): Long {
        val cv = ContentValues().apply {
            put("username", username.trim())
            put("password_hash", hashB64)
            put("salt", saltB64)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insertOrThrow("users", null, cv)
    }

    fun getUserByUsername(username: String): User? {
        val sql = "SELECT id, username, salt, password_hash FROM users WHERE username=? LIMIT 1"
        readableDatabase.rawQuery(sql, arrayOf(username.trim())).use { c ->
            return if (c.moveToFirst()) {
                User(
                    id = c.getLong(0),
                    username = c.getString(1),
                    saltB64 = c.getString(2),
                    hashB64 = c.getString(3)
                )
            } else null
        }
    }

    fun usernameExists(username: String): Boolean {
        val sql = "SELECT 1 FROM users WHERE username=? LIMIT 1"
        readableDatabase.rawQuery(sql, arrayOf(username.trim())).use { c ->
            return c.moveToFirst()
        }
    }

    fun getAllUsernames(): List<String> {
        val db = readableDatabase
        val list = mutableListOf<String>()

        val cursor = db.query("users", arrayOf("username"), null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) list.add(it.getString(0))
        }
        return list
    }

    fun setUserBanned(username: String, banned: Boolean) {
        val values = ContentValues().apply {
            put("banned", if (banned) 1 else 0)
        }
        writableDatabase.update("users", values, "username=?", arrayOf(username))
    }

    fun isUserBanned(username: String): Boolean {
        val cursor = readableDatabase.query(
            "users", arrayOf("banned"), "username=?",
            arrayOf(username), null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0) == 1
        }
        return false
    }

    fun getAllBannedUsernames(): List<String> {
        val result = mutableListOf<String>()
        val cursor = readableDatabase.query(
            "users", arrayOf("username"), "banned=1",
            null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) result.add(it.getString(0))
        }
        return result
    }

    // ====================================================
    // GAME RESULT OPERATIONS
    // ====================================================

    fun setGameResult(gameId: Int, homeScore: Int, awayScore: Int, status: String) {
        val values = ContentValues().apply {
            put("game_id", gameId)
            put("home_score", homeScore)
            put("away_score", awayScore)
            put("status", status)
        }
        writableDatabase.insertWithOnConflict(
            "game_results",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAllGameResults(): List<AuthRepository.GameResultOverride> {
        val list = mutableListOf<AuthRepository.GameResultOverride>()
        val cursor = readableDatabase.query(
            "game_results",
            arrayOf("game_id", "home_score", "away_score", "status"),
            null, null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                list += AuthRepository.GameResultOverride(
                    gameId = it.getInt(0),
                    homeScore = it.getInt(1),
                    awayScore = it.getInt(2),
                    status = it.getString(3)
                )
            }
        }
        return list
    }

    // ====================================================
    // NEW: BET HISTORY TABLE OPERATIONS
    // ====================================================

    fun insertBetHistory(
        username: String,
        gameId: Int,
        pick: String,
        stake: Double,
        odds: Double,
        result: String,   // "WIN", "LOSS", "PUSH"
        payout: Double
    ) {
        val cv = ContentValues().apply {
            put("username", username)
            put("game_id", gameId)
            put("pick", pick)
            put("stake", stake)
            put("odds", odds)
            put("result", result)
            put("payout", payout)
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insert("bet_history", null, cv)
    }

    fun getTotalWon(username: String): Double {
        val sql =
            """
            SELECT COALESCE(
                SUM(
                    CASE 
                       WHEN result='WIN'  THEN payout - stake
                       WHEN result='LOSS' THEN -stake
                       ELSE 0
                    END
                ),
                0
            )
            FROM bet_history
            WHERE username=?
            """

        readableDatabase.rawQuery(sql, arrayOf(username)).use { c ->
            return if (c.moveToFirst()) c.getDouble(0) else 0.0
        }
    }

    fun getUserBetStats(username: String): AuthRepository.UserBetStats {
        val sql =
            """
        SELECT
          SUM(CASE WHEN result = 'WIN'  THEN 1 ELSE 0 END) AS wins,
          SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END) AS losses,
          COUNT(*) AS total
        FROM bet_history
        WHERE username = ?
        """

        readableDatabase.rawQuery(sql, arrayOf(username)).use { c ->
            if (c.moveToFirst()) {
                val wins = c.getInt(0)
                val losses = c.getInt(1)
                val total = c.getInt(2)
                val winRate = if (total > 0) (wins.toDouble() / total) * 100.0 else 0.0

                return AuthRepository.UserBetStats(
                    wins = wins,
                    losses = losses,
                    totalGames = total,
                    winRate = winRate
                )
            }
        }

        // No bets yet → all zeros
        return AuthRepository.UserBetStats(
            wins = 0,
            losses = 0,
            totalGames = 0,
            winRate = 0.0
        )
    }


    // ====================================================

    companion object {
        @Volatile private var INSTANCE: UserDbHelper? = null
        fun get(ctx: Context): UserDbHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDbHelper(ctx.applicationContext).also { INSTANCE = it }
            }
    }

    fun getBetHistory(username: String): List<AuthRepository.BetHistoryEntry> {
        val db = readableDatabase
        val result = mutableListOf<AuthRepository.BetHistoryEntry>()

        val cursor = db.query(
            "bet_history",
            arrayOf(
                "id",
                "game_id",
                "pick",
                "stake",
                "odds",
                "result",
                "payout",
                "timestamp"
            ),
            "username = ?",
            arrayOf(username),
            null,
            null,
            "timestamp DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                result += AuthRepository.BetHistoryEntry(
                    id = it.getLong(0),
                    gameId = it.getInt(1),
                    pick = it.getString(2),
                    stake = it.getDouble(3),
                    odds = it.getDouble(4),
                    result = it.getString(5),
                    payout = it.getDouble(6),
                    timestamp = it.getLong(7)
                )
            }
        }
        return result
    }

    fun updateBetHistoryResult(
        username: String,
        gameId: Int,
        pick: String,
        result: String,
        payout: Double
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("result", result)
            put("payout", payout)
        }

        // We mark *all* pending bets for this user + game + pick as resolved.
        db.update(
            "bet_history",
            values,
            "username = ? AND game_id = ? AND pick = ? AND result = 'PENDING'",
            arrayOf(username, gameId.toString(), pick)
        )
    }


}
