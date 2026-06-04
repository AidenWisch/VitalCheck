package com.vitalcheck.data.remote.fitbit.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivitySummaryDto(
    @Json(name = "summary") val summary: ActivitySummary
)

@JsonClass(generateAdapter = true)
data class ActivitySummary(
    @Json(name = "steps") val steps: Int,
    @Json(name = "caloriesOut") val caloriesOut: Int,
    @Json(name = "veryActiveMinutes") val veryActiveMinutes: Int,
    @Json(name = "fairlyActiveMinutes") val fairlyActiveMinutes: Int,
    @Json(name = "lightlyActiveMinutes") val lightlyActiveMinutes: Int,
    @Json(name = "sedentaryMinutes") val sedentaryMinutes: Int,
    @Json(name = "heartRateZones") val heartRateZones: List<HeartRateZone>? = null
)

@JsonClass(generateAdapter = true)
data class HeartRateZone(
    @Json(name = "name") val name: String,
    @Json(name = "minutes") val minutes: Int,
    @Json(name = "caloriesOut") val caloriesOut: Double
)
