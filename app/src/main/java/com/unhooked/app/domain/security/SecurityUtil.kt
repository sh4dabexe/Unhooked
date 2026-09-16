package com.unhooked.app.domain.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityUtil {

    private const val SALT_LENGTH = 16

    /**
     * Generates a cryptographically random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    /**
     * Hashes the password using SHA-256 and the given salt.
     */
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.getDecoder().decode(salt))
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    /**
     * Verifies the input password against the stored hash and salt using constant-time comparison.
     */
    fun verifyPassword(inputPassword: String, storedHash: String, salt: String): Boolean {
        if (storedHash.isBlank() || salt.isBlank()) return false
        val computedHash = hashPassword(inputPassword, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }
}
