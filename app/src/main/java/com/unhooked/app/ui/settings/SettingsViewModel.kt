package com.unhooked.app.ui.settings

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.ProtectionMode
import com.unhooked.app.domain.security.SecurityUtil
import com.unhooked.app.system.deviceadmin.UnhookedAdminReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "Friend",
    val protectionMode: ProtectionMode = ProtectionMode.NORMAL,
    val overallLimitMinutes: Int = 180,
    val isAdminActive: Boolean = false,
    val hasPasswordSet: Boolean = false,
    val isDarkMode: Boolean = false,
    val isDeviceAdminGranted: Boolean = false
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
                _uiState.value = _uiState.value.copy(
                    protectionMode = mode,
                    isAdminActive = mode == ProtectionMode.ADMIN && isDeviceAdminEnabled()
                )
            }
        }

        viewModelScope.launch {
            repository.preferences.overallLimitMinFlow.collectLatest { limit ->
                _uiState.value = _uiState.value.copy(overallLimitMinutes = limit)
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

        refreshDeviceAdminState()
    }

    fun refreshDeviceAdminState() {
        val granted = isDeviceAdminEnabled()
        _uiState.value = _uiState.value.copy(
            isDeviceAdminGranted = granted,
            isAdminActive = _uiState.value.protectionMode == ProtectionMode.ADMIN && granted
        )
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

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setDarkMode(enabled)
        }
    }

    private fun isDeviceAdminEnabled(): Boolean {
        val context = getApplication<Application>()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val component = ComponentName(context, UnhookedAdminReceiver::class.java)
        return dpm.isAdminActive(component)
    }
}
