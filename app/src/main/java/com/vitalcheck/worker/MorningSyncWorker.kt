package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.domain.usecase.GenerateCoachingPlanUseCase
import com.vitalcheck.domain.usecase.SyncFitbitDataUseCase
import com.vitalcheck.util.Constants
import com.vitalcheck.util.DateUtils
import com.vitalcheck.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder

@HiltWorker
class MorningSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncFitbitData: SyncFitbitDataUseCase,
    private val generateCoachingPlan: GenerateCoachingPlanUseCase,
    private val aiPlanRepository: AiPlanRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.createChannel(applicationContext)

            val yesterday = DateUtils.yesterday()
            val today = DateUtils.today()

            // Step 1: Sync Fitbit data from yesterday + today's baseline
            syncFitbitData(yesterday)
            try { syncFitbitData(today) } catch (_: Exception) { /* today may have no data yet */ }

            // Step 2: Generate AI coaching plan for today
            val plan = generateCoachingPlan(metricsDate = yesterday, planDate = today)

            // Step 3: Schedule notifications throughout the day
            val notifications = aiPlanRepository.getNotificationsForDate(today)
            val workManager = WorkManager.getInstance(applicationContext)
            val now = LocalDateTime.now()

            for (notification in notifications) {
                val scheduledTime = LocalDateTime.of(
                    now.toLocalDate(),
                    LocalTime.of(notification.scheduledHour, notification.scheduledMinute)
                )
                val delay = Duration.between(now, scheduledTime)
                if (delay.isNegative) continue // skip past notifications

                val workRequest = OneTimeWorkRequestBuilder<ScheduledNotificationWorker>()
                    .setInputData(workDataOf("notification_id" to notification.id))
                    .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                    .build()

                workManager.enqueue(workRequest)
                aiPlanRepository.setNotificationWorkRequestId(notification.id, workRequest.id.toString())
            }

            // Step 4: Enqueue periodic intraday sync (every 2 hours) to keep today's data fresh
            val intradayWork = PeriodicWorkRequestBuilder<IntradaySyncWorker>(
                2, TimeUnit.HOURS
            ).addTag(Constants.WORK_INTRADAY_SYNC).build()

            workManager.enqueueUniquePeriodicWork(
                Constants.WORK_INTRADAY_SYNC,
                ExistingPeriodicWorkPolicy.KEEP,
                intradayWork
            )

            // Step 5: Schedule calorie reminder notifications at meal times
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

            // Step 6: Schedule evening evaluation at 9 PM
            val eveningTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(21, 0))
            val eveningDelay = Duration.between(now, eveningTime)
            if (!eveningDelay.isNegative) {
                val eveningWork = OneTimeWorkRequestBuilder<EveningEvalWorker>()
                    .setInitialDelay(eveningDelay.toMillis(), TimeUnit.MILLISECONDS)
                    .addTag(Constants.WORK_EVENING_EVAL)
                    .build()
                workManager.enqueue(eveningWork)
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
