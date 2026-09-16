package com.unhooked.app.domain.model

enum class ProtectionMode {
    NORMAL,
    PASSWORD,
    ADMIN,
    STRICT
}

enum class TimerType {
    COUNTDOWN,
    POMODORO,
    STOPWATCH
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}

enum class PomodoroCycle {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

enum class BlockReason(val displayName: String) {
    DAILY_LIMIT("Daily limit reached"),
    OVERALL_LIMIT("Overall limit exhausted"),
    SCHEDULED("Scheduled block active"),
    FOCUS_SESSION("Focus session in progress"),
    INSTANT_BLOCK("Manual block active"),
    WEBSITE_BLOCKED("Website restricted"),
    KEYWORD_BLOCKED("Content restricted"),
    REELS_SHORTS_BLOCKED("Shorts/Reels restricted")
}

data class AppRuleModel(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 0, // 0 means no daily limit
    val isHardBlocked: Boolean = false,
    val isWhitelisted: Boolean = false,
    val enabled: Boolean = true
)

data class ScheduleModel(
    val id: Long = 0,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int>, // 1 = Sunday, 2 = Monday, ..., 7 = Saturday
    val targetPackages: List<String>,
    val enabled: Boolean = true
)

data class FocusSessionModel(
    val id: Long = 0,
    val timerType: TimerType,
    val startedAt: Long,
    val endAt: Long,
    val durationMinutes: Int,
    val isStrict: Boolean = false,
    val completed: Boolean = false,
    val targetPackages: List<String> = emptyList()
)

data class BlockedAttemptModel(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val reason: BlockReason,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long,
    val lastTimeUsed: Long
)
