package com.unhooked.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.engine.RuleResolver
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

                // Launch BlockActivity safely on its own task stack
                val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
                    putExtra(BlockActivity.EXTRA_APP_NAME, appLabel)
                    putExtra(BlockActivity.EXTRA_REASON, resolution.reason?.name)
                    putExtra(BlockActivity.EXTRA_UNLOCK_TIME, resolution.unlockTimeMs ?: 0L)
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
