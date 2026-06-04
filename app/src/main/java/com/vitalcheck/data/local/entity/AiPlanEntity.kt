package com.vitalcheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_plans")
data class AiPlanEntity(
    @PrimaryKey
    val date: String,
    @ColumnInfo(name = "calorie_target")
    val calorieTarget: Int,
    @ColumnInfo(name = "workout_type")
    val workoutType: String,
    @ColumnInfo(name = "workout_description")
    val workoutDescription: String,
    @ColumnInfo(name = "workout_duration_min")
    val workoutDurationMin: Int,
    @ColumnInfo(name = "raw_json")
    val rawJson: String,
    @ColumnInfo(name = "generated_at")
    val generatedAt: Long
)
