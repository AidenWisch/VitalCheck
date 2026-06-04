package com.vitalcheck.domain.model

data class FoodEntry(
    val id: Long = 0,
    val date: String,
    val rawInput: String,
    val estimatedCalories: Int,
    val confidence: String?,
    val loggedAt: Long
)
