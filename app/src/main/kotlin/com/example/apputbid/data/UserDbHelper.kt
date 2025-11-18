package com.example.apputbid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

 data class User(val id: Long, val username: String, val saltB64: String, val hashB64: String)
class UserDbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx, "app.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
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

        // NEW: store result overrides for seeded games
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
        // add future upgrades here
    }

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

    companion object {
        @Volatile private var INSTANCE: UserDbHelper? = null
        fun get(ctx: Context): UserDbHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDbHelper(ctx.applicationContext).also { INSTANCE = it }
            }
    }

    fun getAllUsernames(): List<String> {
        val db = readableDatabase
        val usernames = mutableListOf<String>()

        // Adjust "users" and "username" if your table/column names differ
        val cursor = db.query(
            "users",                    // table name
            arrayOf("username"),        // columns
            null,                       // selection
            null,                       // selectionArgs
            null,                       // groupBy
            null,                       // having
            null                        // orderBy
        )

        cursor.use {
            while (it.moveToNext()) {
                usernames.add(it.getString(0))
            }
        }

        return usernames
    }

    fun setUserBanned(username: String, banned: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("banned", if (banned) 1 else 0)
        }
        db.update(
            "users",
            values,
            "username = ?",
            arrayOf(username)
        )
    }

    fun isUserBanned(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            "users",
            arrayOf("banned"),
            "username = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) == 1
            }
        }
        return false
    }

    fun getAllBannedUsernames(): List<String> {
        val db = readableDatabase
        val result = mutableListOf<String>()

        val cursor = db.query(
            "users",
            arrayOf("username"),
            "banned = 1",
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                result.add(it.getString(0))
            }
        }
        return result
    }

    fun setGameResult(gameId: Int, homeScore: Int, awayScore: Int, status: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("game_id", gameId)
            put("home_score", homeScore)
            put("away_score", awayScore)
            put("status", status)
        }
        db.insertWithOnConflict(
            "game_results",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAllGameResults(): List<AuthRepository.GameResultOverride> {
        val db = readableDatabase
        val results = mutableListOf<AuthRepository.GameResultOverride>()

        val cursor = db.query(
            "game_results",
            arrayOf("game_id", "home_score", "away_score", "status"),
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                results += AuthRepository.GameResultOverride(
                    gameId = it.getInt(0),
                    homeScore = it.getInt(1),
                    awayScore = it.getInt(2),
                    status = it.getString(3)
                )
            }
        }
        return results
    }



}
