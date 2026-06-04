package com.vitalcheck.data.remote.anthropic.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoachingPlanDto(
    @Json(name = "calorie_target") val calorieTarget: Int,
    @Json(name = "workout") val workout: WorkoutDto,
    @Json(name = "notifications") val notifications: List<NotificationDto>
)

@JsonClass(generateAdapter = true)
data class WorkoutDto(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String,
    @Json(name = "duration_minutes") val durationMinutes: Int
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "hour") val hour: Int,
    @Json(name = "minute") val minute: Int,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class FoodEstimateDto(
    @Json(name = "estimated_calories") val estimatedCalories: Int,
    @Json(name = "confidence") val confidence: String,
    @Json(name = "items") val items: List<FoodItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class FoodItemDto(
    @Json(name = "name") val name: String,
    @Json(name = "calories") val calories: Int
)
