package com.vitalcheck.domain.model

data class LockoutState(
    val date: String,
    val isLocked: Boolean,
    val reason: String?,
    val lockedAt: Long?,
    val unlockedAt: Long?,
    val unlockTrigger: String?
)
