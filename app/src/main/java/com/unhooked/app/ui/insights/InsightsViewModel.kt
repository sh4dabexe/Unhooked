package com.unhooked.app.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.AppUsageStat
import com.unhooked.app.domain.model.BlockedAttemptModel
import com.unhooked.app.system.usage.UsageStatsHelper
import com.unhooked.app.ui.components.ChartBarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class InsightsUiState(
    val selectedTab: Int = 0, // 0 = Day, 1 = Week, 2 = Month
    val totalControlledTimeMinutes: Int = 85,
    val totalFocusTimeMinutes: Int = 110,
    val blockedAttemptsCount: Int = 24,
    val chartData: List<ChartBarData> = emptyList(),
    val topApps: List<AppUsageStat> = emptyList(),
    val recentBlocks: List<BlockedAttemptModel> = emptyList()
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadData()

        viewModelScope.launch {
            repository.recentBlockedAttemptsFlow.collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    recentBlocks = list.take(5),
                    blockedAttemptsCount = list.size
                )
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    private fun loadData() {
        val context = getApplication<Application>()
        val top = UsageStatsHelper.getTopUsedApps(context, 5)

        // Mock/Seed chart data matching the visual style of 125147.jpg
        val weekBars = listOf(
            ChartBarData(label = "22 Mar", percentage = 72),
            ChartBarData(label = "23 Mar", percentage = 64),
            ChartBarData(label = "24 Mar", percentage = 56),
            ChartBarData(label = "25 Mar", percentage = 98, isBestDay = true),
            ChartBarData(label = "26 Mar", percentage = 80)
        )

        _uiState.value = _uiState.value.copy(
            topApps = top,
            chartData = weekBars
        )
    }
}
