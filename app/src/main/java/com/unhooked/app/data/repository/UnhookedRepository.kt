package com.unhooked.app.data.repository

import com.unhooked.app.data.db.AppDatabase
import com.unhooked.app.data.db.entities.AppRuleEntity
import com.unhooked.app.data.db.entities.BlockedAttemptEntity
import com.unhooked.app.data.db.entities.FocusSessionEntity
import com.unhooked.app.data.db.entities.ScheduleEntity
import com.unhooked.app.data.preferences.UserPreferencesDataStore
import com.unhooked.app.domain.model.AppRuleModel
import com.unhooked.app.domain.model.BlockReason
import com.unhooked.app.domain.model.BlockedAttemptModel
import com.unhooked.app.domain.model.FocusSessionModel
import com.unhooked.app.domain.model.ScheduleModel
import com.unhooked.app.domain.model.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnhookedRepository(
    private val database: AppDatabase,
    val preferences: UserPreferencesDataStore
) {
    private val ruleDao = database.ruleDao()
    private val sessionDao = database.sessionDao()
    private val analyticsDao = database.analyticsDao()

    // --- App Rules ---
    val allAppRulesFlow: Flow<List<AppRuleModel>> = ruleDao.getAllAppRulesFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getActiveAppRules(): List<AppRuleModel> {
        return ruleDao.getActiveAppRules().map { it.toDomain() }
    }

    suspend fun getRuleForPackage(packageName: String): AppRuleModel? {
        return ruleDao.getRuleForPackage(packageName)?.toDomain()
    }

    suspend fun saveAppRule(rule: AppRuleModel) {
        ruleDao.insertOrUpdateAppRule(rule.toEntity())
    }

    suspend fun deleteAppRule(rule: AppRuleModel) {
        ruleDao.deleteAppRule(rule.toEntity())
    }

    // --- Schedules ---
    val allSchedulesFlow: Flow<List<ScheduleModel>> = ruleDao.getAllSchedulesFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getActiveSchedules(): List<ScheduleModel> {
        return ruleDao.getActiveSchedules().map { it.toDomain() }
    }

    suspend fun saveSchedule(schedule: ScheduleModel): Long {
        return ruleDao.insertSchedule(schedule.toEntity())
    }

    suspend fun deleteSchedule(schedule: ScheduleModel) {
        ruleDao.deleteSchedule(schedule.toEntity())
    }

    // --- Focus Sessions ---
    val activeFocusSessionFlow: Flow<FocusSessionModel?> = sessionDao.getActiveFocusSessionFlow().map { it?.toDomain() }

    suspend fun getActiveFocusSession(): FocusSessionModel? {
        return sessionDao.getActiveFocusSession()?.toDomain()
    }

    suspend fun startFocusSession(session: FocusSessionModel): Long {
        return sessionDao.insertSession(session.toEntity())
    }

    suspend fun completeFocusSession(sessionId: Long) {
        sessionDao.markSessionCompleted(sessionId)
    }

    suspend fun getTodayFocusMinutes(): Int {
        val midnightMs = com.unhooked.app.domain.engine.RuleResolver.calculateNextMidnightMs(
            System.currentTimeMillis() - 86_400_000L
        )
        return sessionDao.getTotalFocusMinutesSince(midnightMs) ?: 0
    }

    // --- Blocked Attempts ---
    val recentBlockedAttemptsFlow: Flow<List<BlockedAttemptModel>> =
        analyticsDao.getRecentBlockedAttemptsFlow().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun recordBlockedAttempt(packageName: String, appName: String, reason: BlockReason) {
        analyticsDao.insertBlockedAttempt(
            BlockedAttemptEntity(
                packageName = packageName,
                appName = appName,
                reason = reason.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getTodayBlockedCountFlow(): Flow<Int> {
        val midnightMs = com.unhooked.app.domain.engine.RuleResolver.calculateNextMidnightMs(
            System.currentTimeMillis() - 86_400_000L
        )
        return analyticsDao.getBlockedCountSinceFlow(midnightMs)
    }

    // --- Mappers ---
    private fun AppRuleEntity.toDomain() = AppRuleModel(
        id = id,
        packageName = packageName,
        appName = appName,
        dailyLimitMinutes = dailyLimitMinutes,
        isHardBlocked = isHardBlocked,
        isWhitelisted = isWhitelisted,
        enabled = enabled
    )

    private fun AppRuleModel.toEntity() = AppRuleEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        dailyLimitMinutes = dailyLimitMinutes,
        isHardBlocked = isHardBlocked,
        isWhitelisted = isWhitelisted,
        enabled = enabled,
        updatedAt = System.currentTimeMillis()
    )

    private fun ScheduleEntity.toDomain() = ScheduleModel(
        id = id,
        name = name,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        daysOfWeek = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() },
        targetPackages = targetPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        enabled = enabled
    )

    private fun ScheduleModel.toEntity() = ScheduleEntity(
        id = id,
        name = name,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        daysOfWeek = daysOfWeek.joinToString(","),
        targetPackages = targetPackages.joinToString(","),
        enabled = enabled
    )

    private fun FocusSessionEntity.toDomain() = FocusSessionModel(
        id = id,
        timerType = try { TimerType.valueOf(timerType) } catch (e: Exception) { TimerType.COUNTDOWN },
        startedAt = startedAt,
        endAt = endAt,
        durationMinutes = durationMinutes,
        isStrict = isStrict,
        completed = completed,
        targetPackages = targetPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    )

    private fun FocusSessionModel.toEntity() = FocusSessionEntity(
        id = id,
        timerType = timerType.name,
        startedAt = startedAt,
        endAt = endAt,
        durationMinutes = durationMinutes,
        isStrict = isStrict,
        completed = completed,
        targetPackages = targetPackages.joinToString(",")
    )

    private fun BlockedAttemptEntity.toDomain() = BlockedAttemptModel(
        id = id,
        packageName = packageName,
        appName = appName,
        reason = try { BlockReason.valueOf(reason) } catch (e: Exception) { BlockReason.DAILY_LIMIT },
        timestamp = timestamp
    )
}
