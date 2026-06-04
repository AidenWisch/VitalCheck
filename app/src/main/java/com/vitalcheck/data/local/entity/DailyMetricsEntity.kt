package com.vitalcheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_metrics")
data class DailyMetricsEntity(
    @PrimaryKey
    val date: String,                              // "2026-03-28"
    val steps: Int,
    @ColumnInfo(name = "calories_burned")
    val caloriesBurned: Int,
    @ColumnInfo(name = "active_minutes")
    val activeMinutes: Int,
    @ColumnInfo(name = "resting_heart_rate")
    val restingHeartRate: Int? = null,
    @ColumnInfo(name = "hr_zone_out_of_range_min")
    val hrZoneOutOfRangeMin: Int? = null,
    @ColumnInfo(name = "hr_zone_fat_burn_min")
    val hrZoneFatBurnMin: Int? = null,
    @ColumnInfo(name = "hr_zone_cardio_min")
    val hrZoneCardioMin: Int? = null,
    @ColumnInfo(name = "hr_zone_peak_min")
    val hrZonePeakMin: Int? = null,
    @ColumnInfo(name = "sleep_deep_min")
    val sleepDeepMin: Int? = null,
    @ColumnInfo(name = "sleep_light_min")
    val sleepLightMin: Int? = null,
    @ColumnInfo(name = "sleep_rem_min")
    val sleepRemMin: Int? = null,
    @ColumnInfo(name = "sleep_wake_min")
    val sleepWakeMin: Int? = null,
    @ColumnInfo(name = "total_sleep_min")
    val totalSleepMin: Int? = null,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long                              // epoch millis
)
