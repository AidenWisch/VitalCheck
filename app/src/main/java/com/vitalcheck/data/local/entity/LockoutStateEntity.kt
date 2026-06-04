package com.vitalcheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lockout_state")
data class LockoutStateEntity(
    @PrimaryKey
    val date: String,
    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = false,
    val reason: String? = null,
    @ColumnInfo(name = "locked_at")
    val lockedAt: Long? = null,
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null,
    @ColumnInfo(name = "unlock_trigger")
    val unlockTrigger: String? = null
)
