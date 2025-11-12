package com.example.apputbid.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITER = 120_000
    private const val KEY_BITS = 256
    private const val ALGO = "PBKDF2WithHmacSHA256"

    fun newSalt(len: Int = 16): ByteArray = ByteArray(len).also { SecureRandom().nextBytes(it) }

    fun hash(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITER, KEY_BITS)
        val key = SecretKeyFactory.getInstance(ALGO).generateSecret(spec).encoded
        // wipe password chars
        java.util.Arrays.fill(password, '\u0000')
        return key
    }

    fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun fromB64(s: String): ByteArray = Base64.decode(s, Base64.NO_WRAP)

    fun constantTimeEq(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var r = 0
        for (i in a.indices) r = r or (a[i].toInt() xor b[i].toInt())
        return r == 0
    }
}
