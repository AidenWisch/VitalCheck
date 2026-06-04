package com.vitalcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vitalcheck.data.local.entity.AiPlanEntity
import com.vitalcheck.data.local.entity.NotificationMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiPlanDao {
    @Upsert
    suspend fun upsertPlan(plan: AiPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationMessageEntity>)

    @Query("SELECT * FROM ai_plans WHERE date = :date")
    suspend fun getPlanByDate(date: String): AiPlanEntity?

    @Query("SELECT * FROM ai_plans WHERE date = :date")
    fun observePlanByDate(date: String): Flow<AiPlanEntity?>

    @Query("SELECT * FROM notification_messages WHERE plan_date = :date ORDER BY scheduled_hour, scheduled_minute")
    suspend fun getNotificationsForDate(date: String): List<NotificationMessageEntity>

    @Query("SELECT * FROM notification_messages WHERE plan_date = :date ORDER BY scheduled_hour, scheduled_minute")
    fun observeNotificationsForDate(date: String): Flow<List<NotificationMessageEntity>>

    @Query("SELECT * FROM notification_messages WHERE id = :id")
    suspend fun getNotificationById(id: Long): NotificationMessageEntity?

    @Query("UPDATE notification_messages SET is_fired = 1 WHERE id = :id")
    suspend fun markNotificationFired(id: Long)

    @Query("UPDATE notification_messages SET work_request_id = :workRequestId WHERE id = :id")
    suspend fun setWorkRequestId(id: Long, workRequestId: String)

    @Transaction
    suspend fun savePlanWithNotifications(plan: AiPlanEntity, notifications: List<NotificationMessageEntity>) {
        upsertPlan(plan)
        insertNotifications(notifications)
    }
}
