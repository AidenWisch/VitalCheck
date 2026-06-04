package com.vitalcheck.data.repository

import com.vitalcheck.data.local.dao.DailyMetricsDao
import com.vitalcheck.data.local.entity.DailyMetricsEntity
import com.vitalcheck.data.remote.fitbit.FitbitApiService
import com.vitalcheck.domain.model.DailyMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsRepository @Inject constructor(
    private val fitbitApi: FitbitApiService,
    private val metricsDao: DailyMetricsDao
) {
    suspend fun syncMetrics(date: String): DailyMetrics {
        val activity = fitbitApi.getDailyActivitySummary(date)
        val heartRate = try {
            fitbitApi.getHeartRateByDate(date)
        } catch (_: Exception) { null }
        val sleep = try {
            fitbitApi.getSleepLogByDate(date)
        } catch (_: Exception) { null }

        val summary = activity.summary
        val hrValue = heartRate?.activitiesHeart?.firstOrNull()?.value
        val hrZones = hrValue?.heartRateZones ?: summary.heartRateZones
        val sleepStages = sleep?.summary?.stages

        val entity = DailyMetricsEntity(
            date = date,
            steps = summary.steps,
            caloriesBurned = summary.caloriesOut,
            activeMinutes = summary.veryActiveMinutes + summary.fairlyActiveMinutes,
            restingHeartRate = hrValue?.restingHeartRate,
            hrZoneOutOfRangeMin = hrZones?.find { it.name == "Out of Range" }?.minutes,
            hrZoneFatBurnMin = hrZones?.find { it.name == "Fat Burn" }?.minutes,
            hrZoneCardioMin = hrZones?.find { it.name == "Cardio" }?.minutes,
            hrZonePeakMin = hrZones?.find { it.name == "Peak" }?.minutes,
            sleepDeepMin = sleepStages?.deep,
            sleepLightMin = sleepStages?.light,
            sleepRemMin = sleepStages?.rem,
            sleepWakeMin = sleepStages?.wake,
            totalSleepMin = sleep?.summary?.totalMinutesAsleep,
            syncedAt = System.currentTimeMillis()
        )

        metricsDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun getMetrics(date: String): DailyMetrics? =
        metricsDao.getByDate(date)?.toDomain()

    fun observeMetrics(date: String): Flow<DailyMetrics?> =
        metricsDao.observeByDate(date).map { it?.toDomain() }

    suspend fun getRecentMetrics(limit: Int = 7): List<DailyMetrics> =
        metricsDao.getRecent(limit).map { it.toDomain() }
}

private fun DailyMetricsEntity.toDomain() = DailyMetrics(
    date = date,
    steps = steps,
    caloriesBurned = caloriesBurned,
    activeMinutes = activeMinutes,
    restingHeartRate = restingHeartRate,
    hrZoneOutOfRangeMin = hrZoneOutOfRangeMin,
    hrZoneFatBurnMin = hrZoneFatBurnMin,
    hrZoneCardioMin = hrZoneCardioMin,
    hrZonePeakMin = hrZonePeakMin,
    sleepDeepMin = sleepDeepMin,
    sleepLightMin = sleepLightMin,
    sleepRemMin = sleepRemMin,
    sleepWakeMin = sleepWakeMin,
    totalSleepMin = totalSleepMin
)
