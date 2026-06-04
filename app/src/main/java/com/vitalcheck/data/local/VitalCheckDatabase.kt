package com.vitalcheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vitalcheck.data.local.converter.Converters
import com.vitalcheck.data.local.dao.AiPlanDao
import com.vitalcheck.data.local.dao.DailyMetricsDao
import com.vitalcheck.data.local.dao.FoodLogDao
import com.vitalcheck.data.local.dao.LockoutStateDao
import com.vitalcheck.data.local.dao.StreakDao
import com.vitalcheck.data.local.entity.AiPlanEntity
import com.vitalcheck.data.local.entity.DailyMetricsEntity
import com.vitalcheck.data.local.entity.FoodLogEntity
import com.vitalcheck.data.local.entity.LockoutStateEntity
import com.vitalcheck.data.local.entity.NotificationMessageEntity
import com.vitalcheck.data.local.entity.StreakEntity

@Database(
    entities = [
        DailyMetricsEntity::class,
        AiPlanEntity::class,
        NotificationMessageEntity::class,
        FoodLogEntity::class,
        StreakEntity::class,
        LockoutStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VitalCheckDatabase : RoomDatabase() {
    abstract fun dailyMetricsDao(): DailyMetricsDao
    abstract fun aiPlanDao(): AiPlanDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun streakDao(): StreakDao
    abstract fun lockoutStateDao(): LockoutStateDao
}
