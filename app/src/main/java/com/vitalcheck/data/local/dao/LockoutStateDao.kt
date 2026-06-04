package com.vitalcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalcheck.data.local.entity.LockoutStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockoutStateDao {
    @Upsert
    suspend fun upsert(state: LockoutStateEntity)

    @Query("SELECT * FROM lockout_state WHERE date = :date")
    suspend fun getByDate(date: String): LockoutStateEntity?

    @Query("SELECT * FROM lockout_state WHERE date = :date")
    fun observeByDate(date: String): Flow<LockoutStateEntity?>

    @Query("UPDATE lockout_state SET is_locked = 0, unlocked_at = :unlockedAt, unlock_trigger = :trigger WHERE date = :date")
    suspend fun unlock(date: String, unlockedAt: Long, trigger: String)
}
