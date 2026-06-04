package com.vitalcheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_logs",
    indices = [Index("date")]
)
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "raw_input")
    val rawInput: String,
    @ColumnInfo(name = "estimated_calories")
    val estimatedCalories: Int,
    val confidence: String? = null,         // "high", "medium", "low"
    @ColumnInfo(name = "logged_at")
    val loggedAt: Long
)
