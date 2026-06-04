package com.vitalcheck.domain.model

data class CoachingPlan(
    val date: String,
    val calorieTarget: Int,
    val workoutType: String,
    val workoutDescription: String,
    val workoutDurationMin: Int,
    val notifications: List<NotificationMessage>
)

data class NotificationMessage(
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val message: String
)
