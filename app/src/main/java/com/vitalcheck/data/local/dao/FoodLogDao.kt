package com.vitalcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalcheck.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert
    suspend fun insert(entry: FoodLogEntity): Long

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY logged_at DESC")
    fun observeByDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT COALESCE(SUM(estimated_calories), 0) FROM food_logs WHERE date = :date")
    fun observeDailyCalories(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(estimated_calories), 0) FROM food_logs WHERE date = :date")
    suspend fun getDailyCalories(date: String): Int

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT date FROM food_logs ORDER BY date DESC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT date, COALESCE(SUM(estimated_calories), 0) as total FROM food_logs GROUP BY date ORDER BY date DESC")
    suspend fun getDailyCalorySummaries(): List<DailyCalorieSummary>
}

data class DailyCalorieSummary(
    val date: String,
    val total: Int
)
