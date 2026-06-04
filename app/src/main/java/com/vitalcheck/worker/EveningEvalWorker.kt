package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitalcheck.domain.usecase.EvaluateLockoutUseCase
import com.vitalcheck.util.Constants
import com.vitalcheck.util.DateUtils
import com.vitalcheck.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class EveningEvalWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val evaluateLockout: EvaluateLockoutUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = DateUtils.today()
            val goalsMet = evaluateLockout(today)

            NotificationHelper.createChannel(applicationContext)

            if (goalsMet) {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    id = 9999,
                    title = "VitalCheck - Day Complete",
                    message = "Goals met. Streak extended. Don't get comfortable."
                )
            } else {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    id = 9999,
                    title = "VitalCheck - LOCKOUT ACTIVATED",
                    message = "You failed today. Instagram, Snapchat, and YouTube are locked until you fix this. Log a workout or hit your step goal to unlock."
                )

                // Start periodic lockout check
                val lockoutWork = PeriodicWorkRequestBuilder<LockoutCheckWorker>(
                    15, TimeUnit.MINUTES
                ).addTag(Constants.WORK_LOCKOUT_CHECK).build()

                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    Constants.WORK_LOCKOUT_CHECK,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    lockoutWork
                )
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
