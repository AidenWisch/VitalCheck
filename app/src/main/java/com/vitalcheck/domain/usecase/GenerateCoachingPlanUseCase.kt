package com.vitalcheck.domain.usecase

import com.vitalcheck.data.repository.AiPlanRepository
import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.data.repository.StreakRepository
import com.vitalcheck.domain.model.CoachingPlan
import com.vitalcheck.util.DateUtils
import javax.inject.Inject

class GenerateCoachingPlanUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val aiPlanRepository: AiPlanRepository,
    private val streakRepository: StreakRepository,
    private val foodLogRepository: FoodLogRepository
) {
    suspend operator fun invoke(metricsDate: String, planDate: String): CoachingPlan {
        val metrics = metricsRepository.getMetrics(metricsDate)
            ?: throw IllegalStateException("No metrics found for $metricsDate")
        val streak = streakRepository.getLatestStreak()
        val foodCalories = foodLogRepository.getDailyCalories(planDate)

        return aiPlanRepository.generateAndSavePlan(planDate, metrics, streak, foodCalories)
    }
}
