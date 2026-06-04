package com.vitalcheck.domain.model

data class Streak(
    val date: String,
    val goalsMet: Boolean,
    val currentStreak: Int,
    val longestStreak: Int
)
