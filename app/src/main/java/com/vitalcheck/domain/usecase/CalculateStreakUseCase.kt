package com.vitalcheck.domain.usecase

import com.vitalcheck.data.repository.StreakRepository
import com.vitalcheck.domain.model.Streak
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(date: String): Streak? =
        streakRepository.getStreak(date)
}
