package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.data.repository.LockoutRepository
import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.util.Constants
import com.vitalcheck.util.DateUtils
import com.vitalcheck.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LockoutCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val lockoutRepository: LockoutRepository,
    private val metricsRepository: MetricsRepository,
    private val foodLogRepository: FoodLogRepository,
    private val aiPlanRepository: AiPlanRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = DateUtils.today()
        val lockoutState = lockoutRepository.getLockoutState(today)

        if (lockoutState == null || !lockoutState.isLocked) {
            // No active lockout, cancel periodic checks
            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(Constants.WORK_LOCKOUT_CHECK)
            return Result.success()
        }

        // Pull fresh Fitbit data before checking unlock conditions
        try { metricsRepository.syncMetrics(today) } catch (_: Exception) { }

        // Check if unlock conditions are now met
        val plan = aiPlanRepository.getPlan(today)
        val metrics = metricsRepository.getMetrics(today)
        val foodCalories = foodLogRepository.getDailyCalories(today)

        var shouldUnlock = false
        var unlockTrigger = ""

        // Check if workout was logged (active minutes > 20)
        if ((metrics?.activeMinutes ?: 0) >= 20) {
            shouldUnlock = true
            unlockTrigger = "workout_logged"
        }

        // Check if steps threshold reached
        if ((metrics?.steps ?: 0) >= 8000) {
            shouldUnlock = true
            unlockTrigger = "threshold_reached"
        }

        // Check if calorie target now met (user deleted food entries)
        if (plan != null && foodCalories <= plan.calorieTarget) {
            shouldUnlock = true
            unlockTrigger = "calorie_target_met"
        }

        if (shouldUnlock) {
            lockoutRepository.deactivateLockout(today, unlockTrigger)
            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(Constants.WORK_LOCKOUT_CHECK)

            NotificationHelper.createChannel(applicationContext)
            NotificationHelper.showNotification(
                context = applicationContext,
                id = 9998,
                title = "VitalCheck - Lockout Lifted",
                message = "Apps unlocked. Don't make me lock them again."
            )
        }

        return Result.success()
    }
}
