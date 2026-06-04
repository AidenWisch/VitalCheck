package com.vitalcheck.domain.usecase

import com.vitalcheck.data.repository.FoodLogRepository
import com.vitalcheck.domain.model.FoodEntry
import javax.inject.Inject

class LogFoodUseCase @Inject constructor(
    private val foodLogRepository: FoodLogRepository
) {
    suspend operator fun invoke(rawInput: String): FoodEntry =
        foodLogRepository.logFood(rawInput)
}
