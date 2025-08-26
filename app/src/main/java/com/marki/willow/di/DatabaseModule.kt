package com.marki.willow.di

import android.content.Context
import androidx.room.Room
import com.marki.willow.data.dao.ConflictLogDao
import com.marki.willow.data.dao.ExerciseLogDao
import com.marki.willow.data.dao.SleepLogDao
import com.marki.willow.data.database.HealthDatabase
import com.marki.willow.data.health.HealthConnectManager
import com.marki.willow.data.health.HealthConnectPermissionManager
import com.marki.willow.data.health.HealthConnectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHealthDatabase(@ApplicationContext context: Context): HealthDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HealthDatabase::class.java,
            "health_database"
        ).build()
    }

    @Provides
    fun provideSleepLogDao(database: HealthDatabase): SleepLogDao {
        return database.sleepLogDao()
    }

    @Provides
    fun provideExerciseLogDao(database: HealthDatabase): ExerciseLogDao {
        return database.exerciseLogDao()
    }

    @Provides
    fun provideConflictLogDao(database: HealthDatabase): ConflictLogDao {
        return database.conflictLogDao()
    }

    // Health Connect providers

    @Provides
    @Singleton
    fun provideHealthConnectManager(
        @ApplicationContext context: Context
    ): HealthConnectManager {
        return HealthConnectManager(context)
    }

    @Provides
    @Singleton
    fun provideHealthConnectRepository(
        healthConnectManager: HealthConnectManager
    ): HealthConnectRepository {
        return HealthConnectRepository(healthConnectManager)
    }

    @Provides
    @Singleton
    fun provideHealthConnectPermissionManager(
        @ApplicationContext context: Context,
        healthConnectManager: HealthConnectManager
    ): HealthConnectPermissionManager {
        return HealthConnectPermissionManager(context, healthConnectManager)
    }
}