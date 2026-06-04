package com.vitalcheck.data.repository

import com.vitalcheck.data.local.dao.StreakDao
import com.vitalcheck.data.local.entity.StreakEntity
import com.vitalcheck.domain.model.Streak
import com.vitalcheck.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val streakDao: StreakDao
) {
    suspend fun updateStreak(date: String, goalsMet: Boolean) {
        val yesterday = DateUtils.format(DateUtils.parse(date).minusDays(1))
        val previousStreak = streakDao.getByDate(yesterday)

        val currentStreak = if (goalsMet) {
            (previousStreak?.currentStreak ?: 0) + 1
        } else {
            0
        }

        val longestStreak = maxOf(
            currentStreak,
            previousStreak?.longestStreak ?: 0,
            streakDao.getLatest()?.longestStreak ?: 0
        )

        streakDao.upsert(
            StreakEntity(
                date = date,
                goalsMet = goalsMet,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )
        )
    }

    suspend fun getStreak(date: String): Streak? =
        streakDao.getByDate(date)?.toDomain()

    fun observeStreak(date: String): Flow<Streak?> =
        streakDao.observeByDate(date).map { it?.toDomain() }

    suspend fun getLatestStreak(): Streak? =
        streakDao.getLatest()?.toDomain()
}

private fun StreakEntity.toDomain() = Streak(
    date = date,
    goalsMet = goalsMet,
    currentStreak = currentStreak,
    longestStreak = longestStreak
)
