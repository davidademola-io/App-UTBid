package com.example.apputbid.data

import com.example.apputbid.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val db: UserDbHelper) {

    suspend fun getAllUsernames(): List<String> =
        withContext(Dispatchers.IO) {
            db.getAllUsernames()
        }


    suspend fun register(username: String, password: CharArray): Result<Long> =
        withContext(Dispatchers.IO) {
            if (username.isBlank() || password.isEmpty())
                return@withContext Result.failure(IllegalArgumentException("Username and password required"))

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

}
