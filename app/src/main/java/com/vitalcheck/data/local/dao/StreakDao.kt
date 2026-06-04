package com.vitalcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalcheck.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Upsert
    suspend fun upsert(streak: StreakEntity)

    @Query("SELECT * FROM streaks WHERE date = :date")
    suspend fun getByDate(date: String): StreakEntity?

    @Query("SELECT * FROM streaks WHERE date = :date")
    fun observeByDate(date: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): StreakEntity?

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 30): List<StreakEntity>
}
