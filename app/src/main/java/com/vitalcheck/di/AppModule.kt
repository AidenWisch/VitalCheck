package com.vitalcheck.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vitalcheck.data.local.VitalCheckDatabase
import com.vitalcheck.data.local.dao.AiPlanDao
import com.vitalcheck.data.local.dao.DailyMetricsDao
import com.vitalcheck.data.local.dao.FoodLogDao
import com.vitalcheck.data.local.dao.LockoutStateDao
import com.vitalcheck.data.local.dao.StreakDao
import com.vitalcheck.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitalCheckDatabase {
        return Room.databaseBuilder(
            context,
            VitalCheckDatabase::class.java,
            "vitalcheck.db"
        ).build()
    }

    @Provides
    fun provideDailyMetricsDao(db: VitalCheckDatabase): DailyMetricsDao = db.dailyMetricsDao()

    @Provides
    fun provideAiPlanDao(db: VitalCheckDatabase): AiPlanDao = db.aiPlanDao()

    @Provides
    fun provideFoodLogDao(db: VitalCheckDatabase): FoodLogDao = db.foodLogDao()

    @Provides
    fun provideStreakDao(db: VitalCheckDatabase): StreakDao = db.streakDao()

    @Provides
    fun provideLockoutStateDao(db: VitalCheckDatabase): LockoutStateDao = db.lockoutStateDao()

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            Constants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
