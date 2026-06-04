package com.vitalcheck.data.repository

import com.vitalcheck.data.local.dao.LockoutStateDao
import com.vitalcheck.data.local.entity.LockoutStateEntity
import com.vitalcheck.domain.model.LockoutState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockoutRepository @Inject constructor(
    private val lockoutStateDao: LockoutStateDao
) {
    suspend fun activateLockout(date: String, reason: String) {
        lockoutStateDao.upsert(
            LockoutStateEntity(
                date = date,
                isLocked = true,
                reason = reason,
                lockedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deactivateLockout(date: String, trigger: String) {
        lockoutStateDao.unlock(
            date = date,
            unlockedAt = System.currentTimeMillis(),
            trigger = trigger
        )
    }

    suspend fun getLockoutState(date: String): LockoutState? =
        lockoutStateDao.getByDate(date)?.toDomain()

    fun observeLockoutState(date: String): Flow<LockoutState?> =
        lockoutStateDao.observeByDate(date).map { it?.toDomain() }

    suspend fun isLocked(date: String): Boolean =
        lockoutStateDao.getByDate(date)?.isLocked ?: false
}

private fun LockoutStateEntity.toDomain() = LockoutState(
    date = date,
    isLocked = isLocked,
    reason = reason,
    lockedAt = lockedAt,
    unlockedAt = unlockedAt,
    unlockTrigger = unlockTrigger
)
