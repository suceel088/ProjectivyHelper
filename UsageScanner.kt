package com.helper.watchbridge

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager

class UsageScanner(private val ctx: Context) {
    private val pm = ctx.packageManager

    fun getRecentApps(limit: Int = 10): List<MediaItem> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 1000L * 60 * 60 * 24 // last 24 hours
        val stats: List<UsageStats> = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)

        val filtered = stats
            .filter { isUserApp(it.packageName) && it.lastTimeUsed > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .distinctBy { it.packageName }
            .take(limit)

        return filtered.map {
            MediaItem(
                id = it.packageName + ":" + it.lastTimeUsed,
                title = getAppLabel(it.packageName) ?: it.packageName,
                subtitle = "Last used: ${formatTimeAgo(it.lastTimeUsed)}",
                appPackage = it.packageName,
                timestamp = it.lastTimeUsed
            )
        }
    }

    private fun isUserApp(pkg: String): Boolean {
        return try {
            val ai = pm.getApplicationInfo(pkg, 0)
            (ai.flags and (android.content.pm.ApplicationInfo.FLAG_SYSTEM or android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAppLabel(pkg: String): String? {
        return try {
            val ai = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun formatTimeAgo(time: Long): String {
        val diff = System.currentTimeMillis() - time
        val minutes = diff / (1000 * 60)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            minutes < 60 * 24 -> "${minutes / 60} hours ago"
            else -> "${minutes / (60 * 24)} days ago"
        }
    }
}
