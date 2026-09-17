package com.unhooked.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unhooked.app.domain.model.BlockReason

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 0,
    val isHardBlocked: Boolean = false,
    val isWhitelisted: Boolean = false,
    val enabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "website_rules")
data class WebsiteRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val isBlocked: Boolean = true,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String, // Comma separated integers: "2,3,4,5,6"
    val targetPackages: String, // Comma separated package names
    val enabled: Boolean = true,
    val protectionMode: String = "NORMAL",
    val pinHash: String = "",
    val pinSalt: String = ""
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timerType: String,
    val startedAt: Long,
    val endAt: Long,
    val durationMinutes: Int,
    val isStrict: Boolean = false,
    val completed: Boolean = false,
    val targetPackages: String = "" // Comma separated
)

@Entity(tableName = "blocked_attempts")
data class BlockedAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
