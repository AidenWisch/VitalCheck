package com.vitalcheck.domain.model

data class DailyMetrics(
    val date: String,
    val steps: Int,
    val caloriesBurned: Int,
    val activeMinutes: Int,
    val restingHeartRate: Int?,
    val hrZoneOutOfRangeMin: Int?,
    val hrZoneFatBurnMin: Int?,
    val hrZoneCardioMin: Int?,
    val hrZonePeakMin: Int?,
    val sleepDeepMin: Int?,
    val sleepLightMin: Int?,
    val sleepRemMin: Int?,
    val sleepWakeMin: Int?,
    val totalSleepMin: Int?
)
