package com.unhooked.app.domain.security

import android.graphics.Bitmap
import com.unhooked.app.data.db.daos.EmergencyUnlockDao
import com.unhooked.app.data.db.entities.EmergencyUnlockEntity
import java.security.SecureRandom
import java.util.Base64

/**
 * Holds the one-time-display emergency unlock credentials.
 * These are shown to the user once and never stored in plaintext.
 */
data class UnlockCredentials(
    val qrBitmap: Bitmap,
    val passphrase: String,
    val rawSecretBase64: String // For QR payload — same secret, two encodings
)

/**
 * Orchestrates the emergency unlock system for Admin-mode blocks.
 *
 * Security contract:
 * - Secret is generated with SecureRandom (256 bits / 32 bytes)
 * - Only the salted SHA-256 hash is persisted in Room
 * - QR payload and passphrase are two encodings of the same secret
 * - Neither is ever logged or stored in plaintext after initial display
 */
class EmergencyUnlockManager(
    private val emergencyUnlockDao: EmergencyUnlockDao
) {

    companion object {
        private const val SECRET_LENGTH_BYTES = 32 // 256 bits
    }

    /**
     * Generates a new emergency unlock secret for the given schedule.
     * Returns [UnlockCredentials] containing the QR bitmap and passphrase
     * for one-time display to the user.
     *
     * The salted hash is stored in Room — the raw secret is NEVER persisted.
     */
    suspend fun generateUnlockCredentials(scheduleId: Long): UnlockCredentials {
        // 1. Generate cryptographically secure 256-bit secret
        val secretBytes = ByteArray(SECRET_LENGTH_BYTES)
        SecureRandom().nextBytes(secretBytes)

        // 2. Encode as base64 for QR payload
        val secretBase64 = Base64.getEncoder().encodeToString(secretBytes)

        // 3. Generate passphrase from the SAME secret bytes
        val passphrase = Bip39Wordlist.bytesToWords(secretBytes)

        // 4. Generate QR bitmap
        val qrBitmap = QrCodeHelper.generateQrBitmap(secretBase64)

        // 5. Hash the secret for storage (never store plaintext)
        val salt = SecurityUtil.generateSalt()
        val hash = SecurityUtil.hashPassword(secretBase64, salt)

        // 6. Persist only the hash
        emergencyUnlockDao.insert(
            EmergencyUnlockEntity(
                scheduleId = scheduleId,
                secretHash = hash,
                secretSalt = salt,
                isActive = true
            )
        )

        return UnlockCredentials(
            qrBitmap = qrBitmap,
            passphrase = passphrase,
            rawSecretBase64 = secretBase64
        )
    }

    /**
     * Verifies a raw secret (e.g., scanned from QR code as base64) against the stored hash.
     * Returns true if the secret matches.
     */
    suspend fun verifySecret(scheduleId: Long, rawSecretBase64: String): Boolean {
        val entity = emergencyUnlockDao.getActiveByScheduleId(scheduleId) ?: return false
        return SecurityUtil.verifyPassword(rawSecretBase64, entity.secretHash, entity.secretSalt)
    }

    /**
     * Verifies a passphrase by decoding it back to bytes, then to base64, and comparing
     * against the stored hash. Both QR and passphrase converge on the same verification path.
     */
    suspend fun verifyPassphrase(scheduleId: Long, passphrase: String): Boolean {
        // Normalize: lowercase, collapse whitespace
        val normalized = passphrase.trim().lowercase().replace(Regex("\\s+"), " ")
        val decoded = Bip39Wordlist.wordsToBytes(normalized) ?: return false
        val secretBase64 = Base64.getEncoder().encodeToString(decoded)
        return verifySecret(scheduleId, secretBase64)
    }

    /**
     * Deactivates the emergency lock for a schedule after successful unlock.
     */
    suspend fun deactivateUnlock(scheduleId: Long) {
        emergencyUnlockDao.deactivate(scheduleId)
    }

    /**
     * Checks if a schedule has an active emergency lock.
     */
    suspend fun hasActiveLock(scheduleId: Long): Boolean {
        return emergencyUnlockDao.getActiveByScheduleId(scheduleId) != null
    }

    /**
     * Returns the total count of active emergency locks.
     */
    suspend fun getActiveUnlockCount(): Int {
        return emergencyUnlockDao.getActiveCount()
    }
}
