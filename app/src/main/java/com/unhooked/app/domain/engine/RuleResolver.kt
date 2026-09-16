package com.unhooked.app.domain.engine

import com.unhooked.app.domain.model.AppRuleModel
import com.unhooked.app.domain.model.BlockReason
import com.unhooked.app.domain.model.FocusSessionModel
import com.unhooked.app.domain.model.ScheduleModel
import com.unhooked.app.domain.whitelist.WhitelistHelper
import java.util.Calendar

data class RuleResolution(
    val isBlocked: Boolean,
    val reason: BlockReason? = null,
    val unlockTimeMs: Long? = null
)

object RuleResolver {

    /**
     * Evaluates all rules, schedules, limits, and focus sessions for a target package.
     * Guaranteed: Strictest rule wins. Critical/Whitelisted apps always ALLOW.
     */
    fun resolve(
        packageName: String,
        currentTimeMs: Long = System.currentTimeMillis(),
        usageTodayMinutes: Int = 0,
        overallUsageTodayMinutes: Int = 0,
        overallDailyLimitMinutes: Int = 0,
        appRule: AppRuleModel? = null,
        schedules: List<ScheduleModel> = emptyList(),
        activeFocusSession: FocusSessionModel? = null,
        customWhitelistedPackages: Set<String> = emptySet()
    ): RuleResolution {
        // 1. Critical System & Financial Whitelist (Paytm, DigiLocker, PhonePe, System UI, etc.)
        if (WhitelistHelper.isCriticalApp(packageName)) {
            return RuleResolution(isBlocked = false)
        }

        // 2. Custom User Whitelist
        if (customWhitelistedPackages.contains(packageName) || appRule?.isWhitelisted == true) {
            return RuleResolution(isBlocked = false)
        }

        // 3. Instant / Hard Block
        if (appRule != null && appRule.enabled && appRule.isHardBlocked) {
            return RuleResolution(
                isBlocked = true,
                reason = BlockReason.INSTANT_BLOCK
            )
        }

        // 4. Active Focus Session
        if (activeFocusSession != null && activeFocusSession.endAt > currentTimeMs) {
            val appliesToTarget = activeFocusSession.targetPackages.isEmpty() ||
                    activeFocusSession.targetPackages.contains(packageName)
            if (appliesToTarget) {
                return RuleResolution(
                    isBlocked = true,
                    reason = BlockReason.FOCUS_SESSION,
                    unlockTimeMs = activeFocusSession.endAt
                )
            }
        }

        // 5. Per-app Daily Limit
        if (appRule != null && appRule.enabled && appRule.dailyLimitMinutes > 0) {
            if (usageTodayMinutes >= appRule.dailyLimitMinutes) {
                return RuleResolution(
                    isBlocked = true,
                    reason = BlockReason.DAILY_LIMIT,
                    unlockTimeMs = calculateNextMidnightMs(currentTimeMs)
                )
            }
        }

        // 6. Overall Controlled Usage Limit
        if (overallDailyLimitMinutes > 0 && overallUsageTodayMinutes >= overallDailyLimitMinutes) {
            return RuleResolution(
                isBlocked = true,
                reason = BlockReason.OVERALL_LIMIT,
                unlockTimeMs = calculateNextMidnightMs(currentTimeMs)
            )
        }

        // 7. Active Schedules
        for (schedule in schedules) {
            if (!schedule.enabled) continue
            if (!schedule.targetPackages.contains(packageName)) continue

            val scheduleMatch = isTimeInSchedule(schedule, currentTimeMs)
            if (scheduleMatch.first) {
                return RuleResolution(
                    isBlocked = true,
                    reason = BlockReason.SCHEDULED,
                    unlockTimeMs = scheduleMatch.second
                )
            }
        }

        return RuleResolution(isBlocked = false)
    }

    /**
     * Checks if current timestamp falls within the schedule window and returns end timestamp.
     */
    fun isTimeInSchedule(schedule: ScheduleModel, currentTimeMs: Long): Pair<Boolean, Long?> {
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, ..., 7 = Saturday

        if (!schedule.daysOfWeek.contains(dayOfWeek)) {
            return Pair(false, null)
        }

        val currentMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinuteOfDay = schedule.startHour * 60 + schedule.startMinute
        val endMinuteOfDay = schedule.endHour * 60 + schedule.endMinute

        val inWindow = if (startMinuteOfDay <= endMinuteOfDay) {
            currentMinuteOfDay in startMinuteOfDay until endMinuteOfDay
        } else {
            // Spans overnight across midnight
            currentMinuteOfDay >= startMinuteOfDay || currentMinuteOfDay < endMinuteOfDay
        }

        if (inWindow) {
            val endCal = Calendar.getInstance().apply {
                timeInMillis = currentTimeMs
                set(Calendar.HOUR_OF_DAY, schedule.endHour)
                set(Calendar.MINUTE, schedule.endMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (startMinuteOfDay > endMinuteOfDay && currentMinuteOfDay >= startMinuteOfDay) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            return Pair(true, endCal.timeInMillis)
        }

        return Pair(false, null)
    }

    /**
     * Calculates the local midnight epoch timestamp for daily reset.
     */
    fun calculateNextMidnightMs(currentTimeMs: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = currentTimeMs
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
