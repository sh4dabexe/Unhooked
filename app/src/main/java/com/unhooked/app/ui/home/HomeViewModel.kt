package com.unhooked.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.AppUsageStat
import com.unhooked.app.domain.model.FocusSessionModel
import com.unhooked.app.domain.model.TimerType
import com.unhooked.app.domain.whitelist.WhitelistHelper
import com.unhooked.app.system.usage.UsageStatsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Tanim",
    val todayControlledMinutes: Int = 42,
    val overallLimitMinutes: Int = 180,
    val todayFocusMinutes: Int = 55,
    val todayBlockedAttempts: Int = 14,
    val activeFocusSession: FocusSessionModel? = null,
    val topApps: List<AppUsageStat> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val selectedTimeframeIndex: Int = 0, // 0 = Daily, 1 = Weekly
    val selectedLayoutIndex: Int = 0 // 0 = Grid, 1 = Compact
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferences.userNameFlow.collectLatest { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }

        viewModelScope.launch {
            repository.preferences.overallLimitMinFlow.collectLatest { limit ->
                _uiState.value = _uiState.value.copy(overallLimitMinutes = limit)
            }
        }

        viewModelScope.launch {
            repository.activeFocusSessionFlow.collectLatest { session ->
                _uiState.value = _uiState.value.copy(activeFocusSession = session)
            }
        }

        viewModelScope.launch {
            repository.getTodayBlockedCountFlow().collectLatest { count ->
                _uiState.value = _uiState.value.copy(todayBlockedAttempts = count)
            }
        }

        refreshUsage()
    }

    fun refreshUsage() {
        val context = getApplication<Application>()
        val hasPermission = UsageStatsHelper.hasUsageAccess(context)
        _uiState.value = _uiState.value.copy(hasUsagePermission = hasPermission)

        if (hasPermission) {
            val usageStats = UsageStatsHelper.getTodayUsageStats(context)
            val controlledMs = usageStats.filter { (pkg, _) ->
                WhitelistHelper.isEligibleForBlocking(pkg)
            }.values.sum()
            val controlledMinutes = (controlledMs / 60_000L).toInt()

            val topList = UsageStatsHelper.getTopUsedApps(context, 5)

            viewModelScope.launch {
                val focusMin = repository.getTodayFocusMinutes()
                _uiState.value = _uiState.value.copy(
                    todayControlledMinutes = controlledMinutes,
                    todayFocusMinutes = focusMin,
                    topApps = topList
                )
            }
        }
    }

    fun setTimeframeIndex(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTimeframeIndex = index)
    }

    fun setLayoutIndex(index: Int) {
        _uiState.value = _uiState.value.copy(selectedLayoutIndex = index)
    }

    fun startQuickFocus(minutes: Int = 25) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val session = FocusSessionModel(
                timerType = TimerType.COUNTDOWN,
                startedAt = now,
                endAt = now + (minutes * 60_000L),
                durationMinutes = minutes
            )
            repository.startFocusSession(session)
        }
    }
}
