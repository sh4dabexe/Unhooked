package com.unhooked.app.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.engine.TimerEngine
import com.unhooked.app.domain.engine.TimerSnapshot
import com.unhooked.app.domain.model.FocusSessionModel
import com.unhooked.app.domain.model.PomodoroCycle
import com.unhooked.app.domain.model.TimerStatus
import com.unhooked.app.domain.model.TimerType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FocusUiState(
    val timerType: TimerType = TimerType.COUNTDOWN,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val durationMinutes: Int = 25,
    val remainingMs: Long = 25 * 60_000L,
    val progress: Float = 1.0f,
    val cycle: PomodoroCycle = PomodoroCycle.FOCUS,
    val isStrict: Boolean = false,
    val activeSessionId: Long = 0L
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var currentSnapshot: TimerSnapshot? = null

    init {
        viewModelScope.launch {
            repository.activeFocusSessionFlow.collectLatest { session ->
                if (session != null && session.endAt > System.currentTimeMillis()) {
                    currentSnapshot = TimerSnapshot(
                        type = session.timerType,
                        status = TimerStatus.RUNNING,
                        startedAt = session.startedAt,
                        endAt = session.endAt
                    )
                    _uiState.value = _uiState.value.copy(
                        timerType = session.timerType,
                        timerStatus = TimerStatus.RUNNING,
                        durationMinutes = session.durationMinutes,
                        isStrict = session.isStrict,
                        activeSessionId = session.id
                    )
                    startTicker()
                }
            }
        }
    }

    fun setDuration(minutes: Int) {
        if (_uiState.value.timerStatus == TimerStatus.IDLE) {
            _uiState.value = _uiState.value.copy(
                durationMinutes = minutes,
                remainingMs = minutes * 60_000L,
                progress = 1.0f
            )
        }
    }

    fun setTimerType(type: TimerType) {
        if (_uiState.value.timerStatus == TimerStatus.IDLE) {
            _uiState.value = _uiState.value.copy(timerType = type)
        }
    }

    fun startTimer() {
        val now = System.currentTimeMillis()
        val durationMins = _uiState.value.durationMinutes
        val type = _uiState.value.timerType

        val snapshot = TimerEngine.startTimer(type, durationMins, now)
        currentSnapshot = snapshot

        viewModelScope.launch {
            val sessionId = repository.startFocusSession(
                FocusSessionModel(
                    timerType = type,
                    startedAt = snapshot.startedAt,
                    endAt = snapshot.endAt,
                    durationMinutes = durationMins,
                    isStrict = _uiState.value.isStrict
                )
            )
            _uiState.value = _uiState.value.copy(
                timerStatus = TimerStatus.RUNNING,
                activeSessionId = sessionId
            )
            startTicker()
        }
    }

    fun pauseTimer() {
        val snapshot = currentSnapshot ?: return
        val paused = TimerEngine.pauseTimer(snapshot)
        currentSnapshot = paused
        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.PAUSED)
    }

    fun resumeTimer() {
        val snapshot = currentSnapshot ?: return
        val resumed = TimerEngine.resumeTimer(snapshot)
        currentSnapshot = resumed
        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.RUNNING)
    }

    fun stopTimer() {
        tickerJob?.cancel()
        currentSnapshot = null

        val sessionId = _uiState.value.activeSessionId
        if (sessionId != 0L) {
            viewModelScope.launch {
                repository.completeFocusSession(sessionId)
            }
        }

        _uiState.value = _uiState.value.copy(
            timerStatus = TimerStatus.IDLE,
            remainingMs = _uiState.value.durationMinutes * 60_000L,
            progress = 1.0f,
            activeSessionId = 0L
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val snapshot = currentSnapshot
                if (snapshot != null && snapshot.status == TimerStatus.RUNNING) {
                    val remaining = TimerEngine.calculateRemainingMs(snapshot)
                    val totalMs = _uiState.value.durationMinutes * 60_000L
                    val progress = if (totalMs > 0) (remaining.toFloat() / totalMs).coerceIn(0f, 1f) else 0f

                    if (remaining <= 0) {
                        stopTimer()
                        _uiState.value = _uiState.value.copy(timerStatus = TimerStatus.COMPLETED)
                        break
                    } else {
                        _uiState.value = _uiState.value.copy(
                            remainingMs = remaining,
                            progress = progress
                        )
                    }
                }
                delay(1000)
            }
        }
    }
}
