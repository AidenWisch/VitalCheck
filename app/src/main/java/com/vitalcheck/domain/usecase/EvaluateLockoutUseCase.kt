package com.vitalcheck.domain.usecase

import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.data.repository.LockoutRepository
import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.data.repository.StreakRepository
import javax.inject.Inject

class EvaluateLockoutUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val aiPlanRepository: AiPlanRepository,
    private val foodLogRepository: FoodLogRepository,
    private val lockoutRepository: LockoutRepository,
    private val streakRepository: StreakRepository
) {
    /**
     * Evaluates whether the user met their goals for the day.
     * Triggers lockout if goals are missed, updates streak.
     * Returns true if goals were met, false if lockout was triggered.
     */
    suspend operator fun invoke(date: String): Boolean {
        val plan = aiPlanRepository.getPlan(date)
        val calorieTarget = plan?.calorieTarget ?: DEFAULT_CALORIE_TARGET
        val metrics = metricsRepository.getMetrics(date)
        val foodCalories = foodLogRepository.getDailyCalories(date)

        val calorieExceeded = foodCalories > calorieTarget
        val stepsLow = (metrics?.steps ?: 0) < 8000
        val noWorkout = (metrics?.activeMinutes ?: 0) < 20

        val reasons = mutableListOf<String>()
        if (calorieExceeded) reasons.add("calorie_surplus")
        if (stepsLow) reasons.add("missed_steps")
        if (noWorkout) reasons.add("missed_workout")

        val goalsMet = reasons.isEmpty()
        streakRepository.updateStreak(date, goalsMet)

        if (!goalsMet) {
            lockoutRepository.activateLockout(date, reasons.joinToString(","))
        }

        return goalsMet
    }

    companion object {
        const val DEFAULT_CALORIE_TARGET = 2000
    }
}
