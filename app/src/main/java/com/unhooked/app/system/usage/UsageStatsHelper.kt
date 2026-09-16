package com.unhooked.app.system.usage

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.unhooked.app.domain.engine.RuleResolver
import com.unhooked.app.domain.model.AppUsageStat
import com.unhooked.app.domain.whitelist.WhitelistHelper

object UsageStatsHelper {

    /**
     * Checks whether the user has granted Usage Access in Android Settings.
     */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Queries foreground usage from today's local midnight until now.
     */
    fun getTodayUsageStats(context: Context): Map<String, Long> {
        if (!hasUsageAccess(context)) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val now = System.currentTimeMillis()
        val midnight = RuleResolver.calculateNextMidnightMs(now - 86_400_000L)

        val statsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            midnight,
            now
        ) ?: emptyList()

        val usageMap = mutableMapOf<String, Long>()
        for (stat in statsList) {
            val pkg = stat.packageName ?: continue
            val time = stat.totalTimeInForeground
            if (time > 0) {
                usageMap[pkg] = (usageMap[pkg] ?: 0L) + time
            }
        }
        return usageMap
    }

    /**
     * Returns a sorted list of top used apps today with resolved application labels.
     */
    fun getTopUsedApps(context: Context, limit: Int = 10): List<AppUsageStat> {
        val usageMap = getTodayUsageStats(context)
        val pm = context.packageManager

        return usageMap.entries
            .filter { (pkg, time) -> time > 30_000L && WhitelistHelper.isEligibleForBlocking(pkg) }
            .sortedByDescending { it.value }
            .take(limit)
            .map { (pkg, time) ->
                val label = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    pkg
                }
                AppUsageStat(
                    packageName = pkg,
                    appName = label,
                    totalTimeInForegroundMs = time,
                    lastTimeUsed = System.currentTimeMillis()
                )
            }
    }
}
