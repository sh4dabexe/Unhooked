package com.unhooked.app

import com.unhooked.app.domain.engine.RuleResolver
import com.unhooked.app.domain.model.AppRuleModel
import com.unhooked.app.domain.model.BlockReason
import com.unhooked.app.domain.model.FocusSessionModel
import com.unhooked.app.domain.model.ScheduleModel
import com.unhooked.app.domain.model.TimerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RuleResolverTest {

    @Test
    fun paytmAndDigiLockerAreNeverBlockedEvenWithStrictRules() {
        val now = System.currentTimeMillis()
        val activeFocus = FocusSessionModel(
            timerType = TimerType.COUNTDOWN,
            startedAt = now - 60_000,
            endAt = now + 1_800_000,
            durationMinutes = 30,
            targetPackages = emptyList() // All apps
        )

        // Paytm
        val paytmResult = RuleResolver.resolve(
            packageName = "net.one97.paytm",
            currentTimeMs = now,
            usageTodayMinutes = 500,
            overallUsageTodayMinutes = 500,
            overallDailyLimitMinutes = 60,
            activeFocusSession = activeFocus
        )
        assertFalse("Paytm must NEVER be blocked", paytmResult.isBlocked)

        // DigiLocker
        val digiLockerResult = RuleResolver.resolve(
            packageName = "in.gov.digilocker.app",
            currentTimeMs = now,
            usageTodayMinutes = 500,
            overallUsageTodayMinutes = 500,
            overallDailyLimitMinutes = 60,
            activeFocusSession = activeFocus
        )
        assertFalse("DigiLocker must NEVER be blocked", digiLockerResult.isBlocked)
    }

    @Test
    fun appDailyLimitBlocksWhenUsageReachesLimit() {
        val now = System.currentTimeMillis()
        val rule = AppRuleModel(
            packageName = "com.instagram.android",
            appName = "Instagram",
            dailyLimitMinutes = 30,
            enabled = true
        )

        // Under limit: Allow
        val underLimit = RuleResolver.resolve(
            packageName = "com.instagram.android",
            currentTimeMs = now,
            usageTodayMinutes = 25,
            appRule = rule
        )
        assertFalse("Should allow when under daily limit", underLimit.isBlocked)

        // At limit: Block
        val atLimit = RuleResolver.resolve(
            packageName = "com.instagram.android",
            currentTimeMs = now,
            usageTodayMinutes = 30,
            appRule = rule
        )
        assertTrue("Should block when at daily limit", atLimit.isBlocked)
        assertEquals(BlockReason.DAILY_LIMIT, atLimit.reason)
    }

    @Test
    fun activeFocusSessionBlocksControlledApps() {
        val now = System.currentTimeMillis()
        val session = FocusSessionModel(
            timerType = TimerType.POMODORO,
            startedAt = now - 60_000,
            endAt = now + 1_200_000,
            durationMinutes = 25,
            targetPackages = listOf("com.google.android.youtube")
        )

        val result = RuleResolver.resolve(
            packageName = "com.google.android.youtube",
            currentTimeMs = now,
            activeFocusSession = session
        )
        assertTrue("YouTube should be blocked during active focus session", result.isBlocked)
        assertEquals(BlockReason.FOCUS_SESSION, result.reason)
    }

    @Test
    fun scheduleWindowActivatesAndResolvesStrictestRule() {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)

        val activeSchedule = ScheduleModel(
            name = "Work Hours",
            startHour = (currentHour - 1).coerceAtLeast(0),
            startMinute = 0,
            endHour = (currentHour + 1).coerceAtMost(23),
            endMinute = 59,
            daysOfWeek = listOf(currentDay),
            targetPackages = listOf("com.twitter.android")
        )

        val result = RuleResolver.resolve(
            packageName = "com.twitter.android",
            currentTimeMs = cal.timeInMillis,
            schedules = listOf(activeSchedule)
        )
        assertTrue("Twitter should be blocked during active schedule window", result.isBlocked)
        assertEquals(BlockReason.SCHEDULED, result.reason)
    }
}
