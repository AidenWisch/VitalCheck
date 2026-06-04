package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitalcheck.data.local.dao.AiPlanDao
import com.vitalcheck.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiPlanDao: AiPlanDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getLong("notification_id", -1)
        if (notificationId == -1L) return Result.failure()

        val notification = aiPlanDao.getNotificationById(notificationId) ?: return Result.failure()
        if (notification.isFired) return Result.success()

        NotificationHelper.createChannel(applicationContext)
        NotificationHelper.showNotification(
            context = applicationContext,
            id = notificationId.toInt(),
            title = "VitalCheck",
            message = notification.message
        )

        aiPlanDao.markNotificationFired(notificationId)
        return Result.success()
    }
}
