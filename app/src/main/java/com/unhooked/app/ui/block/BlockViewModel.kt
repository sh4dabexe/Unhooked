package com.unhooked.app.ui.block

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.AppRuleModel
import com.unhooked.app.domain.model.ScheduleModel
import com.unhooked.app.domain.whitelist.WhitelistHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isCritical: Boolean = false,
    val rule: AppRuleModel? = null
)

data class BlockUiState(
    val installedApps: List<InstalledAppItem> = emptyList(),
    val filteredApps: List<InstalledAppItem> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0 = Apps, 1 = Schedules, 2 = Whitelist
    val schedules: List<ScheduleModel> = emptyList(),
    val isLoading: Boolean = true
)

class BlockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(BlockUiState())
    val uiState: StateFlow<BlockUiState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()

        viewModelScope.launch {
            repository.allAppRulesFlow.collectLatest { rules ->
                val ruleMap = rules.associateBy { it.packageName }
                val updated = _uiState.value.installedApps.map { item ->
                    item.copy(rule = ruleMap[item.packageName])
                }
                _uiState.value = _uiState.value.copy(
                    installedApps = updated,
                    filteredApps = filterApps(updated, _uiState.value.searchQuery, _uiState.value.selectedTab)
                )
            }
        }

        viewModelScope.launch {
            repository.allSchedulesFlow.collectLatest { schedules ->
                _uiState.value = _uiState.value.copy(schedules = schedules)
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                resolveInfos.map { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    val label = resolveInfo.loadLabel(pm).toString()
                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        isCritical = WhitelistHelper.isCriticalApp(pkg)
                    )
                }.sortedBy { it.appName }
            }

            _uiState.value = _uiState.value.copy(
                installedApps = apps,
                filteredApps = filterApps(apps, _uiState.value.searchQuery, _uiState.value.selectedTab),
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredApps = filterApps(_uiState.value.installedApps, query, _uiState.value.selectedTab)
        )
    }

    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTab = index,
            filteredApps = filterApps(_uiState.value.installedApps, _uiState.value.searchQuery, index)
        )
    }

    fun toggleAppBlock(item: InstalledAppItem) {
        // Critical apps like Paytm/DigiLocker can never be blocked
        if (item.isCritical) return

        viewModelScope.launch {
            val existing = item.rule
            if (existing == null) {
                repository.saveAppRule(
                    AppRuleModel(
                        packageName = item.packageName,
                        appName = item.appName,
                        isHardBlocked = true,
                        enabled = true
                    )
                )
            } else {
                repository.saveAppRule(
                    existing.copy(
                        isHardBlocked = !existing.isHardBlocked,
                        enabled = !existing.isHardBlocked || existing.dailyLimitMinutes > 0
                    )
                )
            }
        }
    }

    fun setAppDailyLimit(item: InstalledAppItem, minutes: Int) {
        if (item.isCritical) return
        viewModelScope.launch {
            val existing = item.rule
            if (existing == null) {
                repository.saveAppRule(
                    AppRuleModel(
                        packageName = item.packageName,
                        appName = item.appName,
                        dailyLimitMinutes = minutes,
                        enabled = minutes > 0
                    )
                )
            } else {
                repository.saveAppRule(
                    existing.copy(
                        dailyLimitMinutes = minutes,
                        enabled = minutes > 0 || existing.isHardBlocked
                    )
                )
            }
        }
    }

    private fun filterApps(
        list: List<InstalledAppItem>,
        query: String,
        tab: Int
    ): List<InstalledAppItem> {
        return list.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesTab = when (tab) {
                0 -> !app.isCritical // Controllable apps
                2 -> app.isCritical || app.rule?.isWhitelisted == true // Whitelisted & Protected
                else -> true
            }

            matchesQuery && matchesTab
        }
    }
}
