package com.vitalcheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_messages",
    foreignKeys = [
        ForeignKey(
            entity = AiPlanEntity::class,
            parentColumns = ["date"],
            childColumns = ["plan_date"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plan_date")]
)
data class NotificationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "plan_date")
    val planDate: String,
    @ColumnInfo(name = "scheduled_hour")
    val scheduledHour: Int,
    @ColumnInfo(name = "scheduled_minute")
    val scheduledMinute: Int,
    val message: String,
    @ColumnInfo(name = "is_fired")
    val isFired: Boolean = false,
    @ColumnInfo(name = "work_request_id")
    val workRequestId: String? = null
)
