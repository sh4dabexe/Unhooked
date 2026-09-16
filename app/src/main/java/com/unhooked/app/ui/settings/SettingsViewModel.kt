package com.unhooked.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.ProtectionMode
import com.unhooked.app.domain.security.SecurityUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "Tanim",
    val protectionMode: ProtectionMode = ProtectionMode.NORMAL,
    val overallLimitMinutes: Int = 180,
    val isStrictActive: Boolean = false,
    val strictUntilMs: Long = 0L,
    val hasPasswordSet: Boolean = false,
    val isDarkMode: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferences.userNameFlow.collectLatest { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }

        viewModelScope.launch {
            repository.preferences.protectionModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(protectionMode = mode)
            }
        }

        viewModelScope.launch {
            repository.preferences.overallLimitMinFlow.collectLatest { limit ->
                _uiState.value = _uiState.value.copy(overallLimitMinutes = limit)
            }
        }

        viewModelScope.launch {
            repository.preferences.strictUntilMsFlow.collectLatest { untilMs ->
                val now = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    strictUntilMs = untilMs,
                    isStrictActive = untilMs > now
                )
            }
        }

        viewModelScope.launch {
            val creds = repository.preferences.getPasswordCredentials()
            _uiState.value = _uiState.value.copy(hasPasswordSet = creds != null)
        }

        viewModelScope.launch {
            repository.preferences.isDarkModeFlow.collectLatest { isDark ->
                _uiState.value = _uiState.value.copy(isDarkMode = isDark)
            }
        }
    }

    fun setProtectionMode(mode: ProtectionMode) {
        viewModelScope.launch {
            repository.preferences.setProtectionMode(mode)
        }
    }

    fun setOverallDailyLimit(minutes: Int) {
        viewModelScope.launch {
            repository.preferences.setOverallLimitMin(minutes)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            repository.preferences.setUserName(name)
        }
    }

    fun setPassword(plainPassword: String) {
        viewModelScope.launch {
            val salt = SecurityUtil.generateSalt()
            val hash = SecurityUtil.hashPassword(plainPassword, salt)
            repository.preferences.setPassword(hash, salt)
            _uiState.value = _uiState.value.copy(hasPasswordSet = true)
        }
    }

    fun enableStrictMode(durationHours: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val untilMs = now + (durationHours * 3600_000L)
            repository.preferences.setStrictUntilMs(untilMs)
            repository.preferences.setProtectionMode(ProtectionMode.STRICT)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setDarkMode(enabled)
        }
    }
}
