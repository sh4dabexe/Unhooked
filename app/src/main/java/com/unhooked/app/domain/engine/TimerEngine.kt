package com.unhooked.app.domain.engine

import com.unhooked.app.domain.model.PomodoroCycle
import com.unhooked.app.domain.model.TimerStatus
import com.unhooked.app.domain.model.TimerType

data class TimerSnapshot(
    val type: TimerType,
    val status: TimerStatus,
    val startedAt: Long,
    val endAt: Long,
    val pausedAt: Long = 0L,
    val totalPausedDurationMs: Long = 0L,
    val cycle: PomodoroCycle = PomodoroCycle.FOCUS,
    val completedCycles: Int = 0
)

object TimerEngine {

    /**
     * Calculates remaining time in milliseconds from absolute timestamps.
     */
    fun calculateRemainingMs(snapshot: TimerSnapshot, nowMs: Long = System.currentTimeMillis()): Long {
        if (snapshot.status == TimerStatus.IDLE || snapshot.status == TimerStatus.COMPLETED) {
            return 0L
        }
        if (snapshot.status == TimerStatus.PAUSED) {
            return (snapshot.endAt - snapshot.pausedAt).coerceAtLeast(0L)
        }
        return (snapshot.endAt - nowMs).coerceAtLeast(0L)
    }

    /**
     * Creates a new running timer snapshot.
     */
    fun startTimer(
        type: TimerType,
        durationMinutes: Int,
        nowMs: Long = System.currentTimeMillis(),
        cycle: PomodoroCycle = PomodoroCycle.FOCUS
    ): TimerSnapshot {
        val durationMs = durationMinutes * 60_000L
        return TimerSnapshot(
            type = type,
            status = TimerStatus.RUNNING,
            startedAt = nowMs,
            endAt = nowMs + durationMs,
            cycle = cycle
        )
    }

    /**
     * Pauses a running timer.
     */
    fun pauseTimer(snapshot: TimerSnapshot, nowMs: Long = System.currentTimeMillis()): TimerSnapshot {
        if (snapshot.status != TimerStatus.RUNNING) return snapshot
        return snapshot.copy(
            status = TimerStatus.PAUSED,
            pausedAt = nowMs
        )
    }

    /**
     * Resumes a paused timer, adjusting endAt by the pause duration.
     */
    fun resumeTimer(snapshot: TimerSnapshot, nowMs: Long = System.currentTimeMillis()): TimerSnapshot {
        if (snapshot.status != TimerStatus.PAUSED) return snapshot
        val pauseDuration = (nowMs - snapshot.pausedAt).coerceAtLeast(0L)
        return snapshot.copy(
            status = TimerStatus.RUNNING,
            endAt = snapshot.endAt + pauseDuration,
            totalPausedDurationMs = snapshot.totalPausedDurationMs + pauseDuration,
            pausedAt = 0L
        )
    }

    /**
     * Formats milliseconds into mm:ss format.
     */
    fun formatDuration(ms: Long): String {
        val totalSecs = (ms / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
