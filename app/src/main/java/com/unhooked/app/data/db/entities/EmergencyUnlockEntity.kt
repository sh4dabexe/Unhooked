package com.unhooked.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the salted hash of the emergency unlock secret for each Admin-mode schedule.
 * The plaintext secret is NEVER persisted — only shown once at creation time.
 */
@Entity(tableName = "emergency_unlocks")
data class EmergencyUnlockEntity(
    @PrimaryKey val scheduleId: Long,
    val secretHash: String,
    val secretSalt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
