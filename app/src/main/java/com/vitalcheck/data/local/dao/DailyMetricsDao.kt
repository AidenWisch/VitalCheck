package com.vitalcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalcheck.data.local.entity.DailyMetricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricsDao {
    @Upsert
    suspend fun upsert(metrics: DailyMetricsEntity)

    @Query("SELECT * FROM daily_metrics WHERE date = :date")
    suspend fun getByDate(date: String): DailyMetricsEntity?

    @Query("SELECT * FROM daily_metrics WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyMetricsEntity?>

    @Query("SELECT * FROM daily_metrics ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 7): List<DailyMetricsEntity>
}
