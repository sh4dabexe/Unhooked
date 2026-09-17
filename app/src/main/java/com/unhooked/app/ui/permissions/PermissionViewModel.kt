package com.unhooked.app.ui.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.unhooked.app.system.deviceadmin.UnhookedAdminReceiver
import com.unhooked.app.system.usage.UsageStatsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val intentAction: String
)

data class PermissionsUiState(
    val permissions: List<PermissionItem> = emptyList()
)

class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val context = getApplication<Application>()

        val hasUsage = UsageStatsHelper.hasUsageAccess(context)
        val hasAccessibility = isAccessibilityServiceEnabled(context)
        val hasOverlay = Settings.canDrawOverlays(context)
        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val hasNotificationListener = isNotificationListenerEnabled(context)
        val hasDeviceAdmin = isDeviceAdminEnabled(context)

        val items = listOf(
            PermissionItem(
                id = "usage",
                title = "Usage Access",
                description = "Required to calculate screen time, app limits, and overall daily allowances.",
                isGranted = hasUsage,
                isRequired = true,
                intentAction = Settings.ACTION_USAGE_ACCESS_SETTINGS
            ),
            PermissionItem(
                id = "accessibility",
                title = "App Blocker Accessibility",
                description = "Used to detect when a blocked app is launched and open your calm focus screen. Unhooked CANNOT read your screen content, passwords, or personal messages.",
                isGranted = hasAccessibility,
                isRequired = true,
                intentAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
            ),
            PermissionItem(
                id = "overlay",
                title = "Display Over Other Apps",
                description = "Allows Unhooked to show critical alerts and permission reminders when needed.",
                isGranted = hasOverlay,
                isRequired = false,
                intentAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            ),
            PermissionItem(
                id = "notifications",
                title = "Focus Notifications",
                description = "Shows active timer progress and completion alerts.",
                isGranted = hasNotifications,
                isRequired = false,
                intentAction = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            ),
            PermissionItem(
                id = "notification_listener",
                title = "Notification Suppressor",
                description = "Optional: Silences distracting notifications during deep focus sessions.",
                isGranted = hasNotificationListener,
                isRequired = false,
                intentAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            ),
            PermissionItem(
                id = "device_admin",
                title = "Admin Protection",
                description = "Optional: Prevents casual uninstallation when Admin Mode is locked.",
                isGranted = hasDeviceAdmin,
                isRequired = false,
                intentAction = DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
            )
        )

        _uiState.value = PermissionsUiState(permissions = items)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName
        }
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val packages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packages.contains(context.packageName)
    }

    private fun isDeviceAdminEnabled(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        val component = ComponentName(context, UnhookedAdminReceiver::class.java)
        return dpm.isAdminActive(component)
    }
}
