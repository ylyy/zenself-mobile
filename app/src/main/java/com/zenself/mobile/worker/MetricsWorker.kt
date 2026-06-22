package com.zenself.mobile.worker

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zenself.mobile.model.EventKind
import com.zenself.mobile.model.MobileEvent
import com.zenself.mobile.sync.EventWriter

/**
 * Periodic worker (default every 30 min) that captures screen-time usage and
 * (where available) heart rate, writing a single `metric` event per run.
 *
 * Designed to degrade gracefully:
 *  - No USAGE_STATS permission → screen_time_min omitted
 *  - No TYPE_HEART_RATE sensor (the common case on phones) → heart_rate omitted
 *  - Any single failure path still returns Result.success() so WorkManager
 *    keeps the periodic schedule alive. Only an unexpected exception bubbles up
 *    as Result.retry().
 */
class MetricsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val screenTimeMin = queryScreenTimeMinutes(context)
            // Heart rate sensors (TYPE_HEART_RATE) require WearOS pairing on most
            // phones and are out of scope here — left null deliberately.
            val event = MobileEvent(
                kind = EventKind.METRIC,
                screenTimeMin = screenTimeMin,
                heartRate = null,
            )
            val customPath = readSyncDir(context)
            EventWriter.append(context, event, customPath)
            Result.success()
        } catch (e: Exception) {
            // Don't crash the periodic chain — a transient error will retry next cycle
            Result.success()
        }
    }

    /**
     * Sum foreground time across all packages over the last 30 minutes.
     * Returns null when the user has not granted PACKAGE_USAGE_STATS.
     */
    private fun queryScreenTimeMinutes(context: Context): Int? {
        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return null
            val now = System.currentTimeMillis()
            val begin = now - THIRTY_MIN_MS
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, begin, now
            ) ?: return null
            val totalMs = stats.sumOf { it.totalTimeInForeground }
            (totalMs / 60_000L).toInt().takeIf { it > 0 }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun readSyncDir(context: Context): String? {
        return context.getSharedPreferences("zenself_prefs", Context.MODE_PRIVATE)
            .getString("sync_dir", null)
    }

    companion object {
        const val WORK_NAME = "zenself_metrics"
        private const val THIRTY_MIN_MS = 30L * 60 * 1000
    }
}
