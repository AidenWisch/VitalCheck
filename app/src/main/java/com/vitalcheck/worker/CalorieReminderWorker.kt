package com.vitalcheck.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitalcheck.data.local.dao.FoodLogDao
import com.vitalcheck.util.DateUtils
import com.vitalcheck.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime

@HiltWorker
class CalorieReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val foodLogDao: FoodLogDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = DateUtils.today()
        val totalCals = foodLogDao.getDailyCalories(today)
        val hour = LocalTime.now().hour

        val message = when {
            // Morning - no entries yet
            hour < 10 && totalCals == 0 ->
                "Log your breakfast. Every calorie counts — and so does every one you forget to track."

            // Lunch time
            hour in 11..13 && totalCals == 0 ->
                "It's lunchtime and you've logged ZERO calories today. Are you fasting or just lying to yourself?"

            hour in 11..13 ->
                "Lunch time. You're at $totalCals cal so far. Log what you eat — no hiding."

            // Afternoon check
            hour in 15..17 && totalCals == 0 ->
                "Half the day is gone and you haven't logged a single thing. The app can't help you if you don't use it."

            hour in 15..17 ->
                "Afternoon check: $totalCals cal logged. Snacking? Log it. That handful of chips counts."

            // Dinner time
            hour in 18..20 && totalCals > 1600 ->
                "You're at $totalCals cal and it's dinner time. You have ${2000 - totalCals} cal left. Choose wisely or get locked out."

            hour in 18..20 ->
                "Dinner time. $totalCals cal so far today. Log your meal — don't make me guess."

            // Late night
            hour >= 21 && totalCals > 2000 ->
                "You're at $totalCals cal. Over your 2,000 limit. Step away from the kitchen."

            hour >= 21 ->
                "End of day: $totalCals cal logged. If you ate anything else, log it now before evaluation."

            else ->
                "Don't forget to log your meals. You're at $totalCals cal today."
        }

        NotificationHelper.createChannel(applicationContext)
        NotificationHelper.showNotification(
            context = applicationContext,
            id = 7000 + hour,
            title = "VitalCheck - Log Your Food",
            message = message,
            targetScreen = NotificationHelper.SCREEN_FOOD_LOG
        )

        return Result.success()
    }
}
