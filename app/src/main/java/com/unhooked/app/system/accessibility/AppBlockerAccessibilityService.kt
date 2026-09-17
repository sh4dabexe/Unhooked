package com.unhooked.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.engine.RuleResolver
import com.unhooked.app.domain.model.ProtectionMode
import com.unhooked.app.domain.whitelist.WhitelistHelper
import com.unhooked.app.system.usage.UsageStatsHelper
import com.unhooked.app.ui.blocked.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastBlockedPackage: String? = null
    private var lastBlockedTimestamp: Long = 0L

    companion object {
        // Packages to block when Admin mode is active (prevents Settings-based bypass/uninstall)
        private val ADMIN_BLOCKED_PACKAGES = setOf(
            "com.android.settings",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.coloros.packageinstaller"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // 1. CRITICAL GUARANTEE: Never inspect or touch sensitive apps (Paytm, DigiLocker, PhonePe, System)
        if (WhitelistHelper.isCriticalApp(packageName)) {
            return
        }

        // Debounce repeated events for the same package within 1.5 seconds
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockedTimestamp) < 1500) {
            return
        }

        val repository = UnhookedApp.repository ?: return

        serviceScope.launch {
            // 2. Admin Mode: Block access to Settings / Package Installer to prevent uninstall
            val protectionMode = repository.preferences.protectionModeFlow.first()
            if (protectionMode == ProtectionMode.ADMIN && packageName in ADMIN_BLOCKED_PACKAGES) {
                // Check if there are any active Admin-mode schedules currently in their window
                val schedules = repository.getActiveSchedules()
                val hasActiveAdminSchedule = schedules.any { schedule ->
                    schedule.protectionMode == ProtectionMode.ADMIN &&
                            RuleResolver.isTimeInSchedule(schedule, now).first
                }

                if (hasActiveAdminSchedule) {
                    lastBlockedPackage = packageName
                    lastBlockedTimestamp = now

                    val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
                        putExtra(BlockActivity.EXTRA_APP_NAME, "Settings")
                        putExtra(BlockActivity.EXTRA_REASON, "ADMIN_LOCKED")
                        putExtra(BlockActivity.EXTRA_UNLOCK_TIME, 0L)
                        putExtra(BlockActivity.EXTRA_IS_ADMIN_MODE, true)
                        putExtra(BlockActivity.EXTRA_SCHEDULE_ID, schedules.first {
                            it.protectionMode == ProtectionMode.ADMIN
                        }.id)
                    }
                    startActivity(intent)
                    return@launch
                }
            }

            // 3. Normal rule resolution for app blocking
            val appRule = repository.getRuleForPackage(packageName)
            val schedules = repository.getActiveSchedules()
            val focusSession = repository.getActiveFocusSession()
            val overallLimitMin = repository.preferences.overallLimitMinFlow.first()

            val usageStats = UsageStatsHelper.getTodayUsageStats(applicationContext)
            val usageTodayMs = usageStats[packageName] ?: 0L
            val usageTodayMinutes = (usageTodayMs / 60_000L).toInt()

            val totalControlledMs = usageStats.filter { (pkg, _) ->
                WhitelistHelper.isEligibleForBlocking(pkg)
            }.values.sum()
            val totalControlledMinutes = (totalControlledMs / 60_000L).toInt()

            val resolution = RuleResolver.resolve(
                packageName = packageName,
                currentTimeMs = now,
                usageTodayMinutes = usageTodayMinutes,
                overallUsageTodayMinutes = totalControlledMinutes,
                overallDailyLimitMinutes = overallLimitMin,
                appRule = appRule,
                schedules = schedules,
                activeFocusSession = focusSession
            )

            if (resolution.isBlocked) {
                lastBlockedPackage = packageName
                lastBlockedTimestamp = now

                val appLabel = try {
                    val info = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Log attempt
                resolution.reason?.let { reason ->
                    repository.recordBlockedAttempt(packageName, appLabel, reason)
                }

                // Check if blocked by an Admin-mode schedule
                val blockingAdminSchedule = schedules.firstOrNull { schedule ->
                    schedule.protectionMode == ProtectionMode.ADMIN &&
                            schedule.targetPackages.contains(packageName) &&
                            RuleResolver.isTimeInSchedule(schedule, now).first
                }

                // Launch BlockActivity safely on its own task stack
                val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
                    putExtra(BlockActivity.EXTRA_APP_NAME, appLabel)
                    putExtra(BlockActivity.EXTRA_REASON, resolution.reason?.name)
                    putExtra(BlockActivity.EXTRA_UNLOCK_TIME, resolution.unlockTimeMs ?: 0L)
                    putExtra(BlockActivity.EXTRA_IS_ADMIN_MODE, blockingAdminSchedule != null)
                    putExtra(BlockActivity.EXTRA_SCHEDULE_ID, blockingAdminSchedule?.id ?: 0L)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by system
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
