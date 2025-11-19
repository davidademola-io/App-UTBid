package com.example.apputbid.data

import com.example.apputbid.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val db: UserDbHelper) {

    // ============================================================
    // USERS
    // ============================================================

    suspend fun getAllUsernames(): List<String> =
        withContext(Dispatchers.IO) {
            db.getAllUsernames()
        }

    suspend fun register(username: String, password: CharArray): Result<Long> =
        withContext(Dispatchers.IO) {
            if (username.isBlank() || password.isEmpty())
                return@withContext Result.failure(IllegalArgumentException("Username and password required"))

            if (!username.endsWith("@mavs.uta.edu")) {
                return@withContext Result.failure(IllegalArgumentException("Must be a UTA email"))
            }

            if (db.usernameExists(username)) {
                java.util.Arrays.fill(password, '\u0000')
                return@withContext Result.failure(IllegalStateException("Username already exists"))
            }

            val salt = PasswordHasher.newSalt()
            val hash = PasswordHasher.hash(password, salt)

            val rowId = try {
                db.insertUser(username, PasswordHasher.b64(salt), PasswordHasher.b64(hash))
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                return@withContext Result.failure(IllegalStateException("Username already exists"))
            }
            Result.success(rowId)
        }

    suspend fun login(username: String, password: CharArray): Result<User> =
        withContext(Dispatchers.IO) {
            val user = db.getUserByUsername(username)
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid username or password"))

            val salt = PasswordHasher.fromB64(user.saltB64)
            val expected = PasswordHasher.fromB64(user.hashB64)
            val computed = PasswordHasher.hash(password, salt)

            if (PasswordHasher.constantTimeEq(expected, computed)) {
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Invalid username or password"))
            }
        }

    suspend fun setBanned(username: String, banned: Boolean) =
        withContext(Dispatchers.IO) {
            db.setUserBanned(username, banned)
        }

    suspend fun isBanned(username: String): Boolean =
        withContext(Dispatchers.IO) {
            db.isUserBanned(username)
        }

    suspend fun getAllBannedUsernames(): List<String> =
        withContext(Dispatchers.IO) {
            db.getAllBannedUsernames()
        }

    // ============================================================
    // GAME RESULTS
    // ============================================================

    data class GameResultOverride(
        val gameId: Int,
        val homeScore: Int,
        val awayScore: Int,
        val status: String
    )

    suspend fun saveGameResult(gameId: Int, home: Int, away: Int, status: String) =
        withContext(Dispatchers.IO) {
            db.setGameResult(gameId, home, away, status)
        }

    suspend fun getAllGameResults(): List<GameResultOverride> =
        withContext(Dispatchers.IO) {
            db.getAllGameResults()
        }

    // ============================================================
    // NEW: BET HISTORY (persistent)
    // ============================================================

    suspend fun insertBetHistory(
        username: String,
        gameId: Int,
        pick: String,
        stake: Double,
        odds: Double,
        result: String,
        payout: Double
    ) = withContext(Dispatchers.IO) {
        db.insertBetHistory(
            username = username,
            gameId = gameId,
            pick = pick,
            stake = stake,
            odds = odds,
            result = result,
            payout = payout
        )
    }

    suspend fun getTotalWon(username: String): Double =
        withContext(Dispatchers.IO) {
            db.getTotalWon(username)
        }

    suspend fun getUserBetStats(username: String): UserBetStats =
        withContext(Dispatchers.IO) {
            db.getUserBetStats(username)
        }

    data class UserBetStats(
        val wins: Int,
        val losses: Int,
        val totalGames: Int,
        val winRate: Double
    )

    data class BetHistoryEntry(
        val id: Long,
        val gameId: Int,
        val pick: String,        // "home" or "away"
        val stake: Double,
        val odds: Double,
        val result: String,      // "PENDING", "WIN", "LOSS", "PUSH"
        val payout: Double,
        val timestamp: Long
    )

    suspend fun getBetHistory(username: String): List<BetHistoryEntry> =
        withContext(Dispatchers.IO) {
            db.getBetHistory(username)
        }

    suspend fun updateBetHistoryResult(
        username: String,
        gameId: Int,
        pick: String,
        result: String,
        payout: Double
    ) = withContext(Dispatchers.IO) {
        db.updateBetHistoryResult(username, gameId, pick, result, payout)
    }



}
