package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Runs every 2 hours during the day to pull live Fitbit data for today.
 * This keeps steps, calories burned, active minutes, and heart rate zones
 * up to date so the dashboard reflects real-time progress and the lockout
 * system can detect when unlock conditions are met (e.g., workout completed).
 */
@HiltWorker
class IntradaySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val metricsRepository: MetricsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = DateUtils.today()
            metricsRepository.syncMetrics(today)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
