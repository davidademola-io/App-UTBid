package com.example.apputbid.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class User(val id: Long, val username: String, val saltB64: String, val hashB64: String)

class UserDbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx, "app.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL,
              salt TEXT NOT NULL,
              created_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_users_username ON users(username);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // add migrations here when bumping version
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
}
