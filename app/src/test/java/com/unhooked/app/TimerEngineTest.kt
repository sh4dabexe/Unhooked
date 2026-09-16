package com.unhooked.app

import com.unhooked.app.domain.engine.TimerEngine
import com.unhooked.app.domain.model.TimerStatus
import com.unhooked.app.domain.model.TimerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {

    @Test
    fun startTimerSetsCorrectTimestamps() {
        val now = 100_000L
        val snapshot = TimerEngine.startTimer(
            type = TimerType.COUNTDOWN,
            durationMinutes = 25,
            nowMs = now
        )

        assertEquals(now, snapshot.startedAt)
        assertEquals(now + 25 * 60_000L, snapshot.endAt)
        assertEquals(TimerStatus.RUNNING, snapshot.status)
    }

    @Test
    fun calculateRemainingAccurateWithoutDrift() {
        val now = 100_000L
        val snapshot = TimerEngine.startTimer(
            type = TimerType.COUNTDOWN,
            durationMinutes = 10,
            nowMs = now
        )

        // 3 minutes passed
        val remaining = TimerEngine.calculateRemainingMs(snapshot, nowMs = now + 180_000L)
        assertEquals(7 * 60_000L, remaining)
    }

    @Test
    fun pauseAndResumeAdjustsEndTimestamp() {
        val now = 100_000L
        val snapshot = TimerEngine.startTimer(
            type = TimerType.COUNTDOWN,
            durationMinutes = 20,
            nowMs = now
        )

        // Pause after 5 minutes
        val paused = TimerEngine.pauseTimer(snapshot, nowMs = now + 300_000L)
        assertEquals(TimerStatus.PAUSED, paused.status)

        // Resume after 2 minutes of pause
        val resumed = TimerEngine.resumeTimer(paused, nowMs = now + 420_000L)
        assertEquals(TimerStatus.RUNNING, resumed.status)
        // endAt shifted forward by 120_000L
        assertEquals(snapshot.endAt + 120_000L, resumed.endAt)
    }

    @Test
    fun formatDurationProducesCorrectFormat() {
        assertEquals("25:00", TimerEngine.formatDuration(25 * 60_000L))
        assertEquals("04:30", TimerEngine.formatDuration(270_000L))
        assertEquals("00:00", TimerEngine.formatDuration(0L))
        assertEquals("00:00", TimerEngine.formatDuration(-5000L))
    }
}
