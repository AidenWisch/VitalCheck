package com.vitalcheck.domain.usecase

import com.vitalcheck.data.repository.MetricsRepository
import com.vitalcheck.domain.model.DailyMetrics
import javax.inject.Inject

class SyncFitbitDataUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository
) {
    suspend operator fun invoke(date: String): DailyMetrics =
        metricsRepository.syncMetrics(date)
}
