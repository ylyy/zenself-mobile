package com.zenself.mobile.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zenself.mobile.worker.MetricsWorker
import java.util.concurrent.TimeUnit

/**
 * Re-enqueues the periodic metrics worker after device boot, so collection
 * resumes without the user having to open the app.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        scheduleMetrics(context)
    }

    companion object {
        /**
         * Public entry point: the Settings screen and the Application class
         * call this to (re)schedule the worker with the user's chosen interval.
         */
        fun scheduleMetrics(context: Context, intervalMinutes: Long = 30L) {
            val request = PeriodicWorkRequestBuilder<MetricsWorker>(intervalMinutes, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                MetricsWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
