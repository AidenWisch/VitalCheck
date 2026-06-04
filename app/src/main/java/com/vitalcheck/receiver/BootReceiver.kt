package com.vitalcheck.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vitalcheck.util.Constants
import com.vitalcheck.worker.CalorieReminderWorker
import com.vitalcheck.worker.IntradaySyncWorker
import com.vitalcheck.worker.MorningSyncWorker
import androidx.work.OneTimeWorkRequestBuilder
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val workManager = WorkManager.getInstance(context)

        // Re-enqueue daily morning sync
        val now = LocalDateTime.now()
        var nextRun = LocalDateTime.of(now.toLocalDate(), LocalTime.of(6, 0))
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun)

        val morningWork = PeriodicWorkRequestBuilder<MorningSyncWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(Constants.WORK_MORNING_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_MORNING_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            morningWork
        )

        // Schedule calorie reminders for remaining meal times today
        val reminderHours = listOf(8, 12, 15, 18, 21)
        for (hour in reminderHours) {
            val reminderTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, 0))
            val reminderDelay = Duration.between(now, reminderTime)
            if (reminderDelay.isNegative) continue

            val reminderWork = OneTimeWorkRequestBuilder<CalorieReminderWorker>()
                .setInitialDelay(reminderDelay.toMillis(), TimeUnit.MILLISECONDS)
                .addTag(Constants.WORK_CALORIE_REMINDER)
                .build()
            workManager.enqueue(reminderWork)
        }

        // Re-enqueue intraday sync (every 2 hours)
        val intradayWork = PeriodicWorkRequestBuilder<IntradaySyncWorker>(
            2, TimeUnit.HOURS
        ).addTag(Constants.WORK_INTRADAY_SYNC).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_INTRADAY_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            intradayWork
        )
    }
}
