package com.marki.willow.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.marki.willow.data.converter.Converters
import com.marki.willow.data.dao.ConflictLogDao
import com.marki.willow.data.dao.ExerciseLogDao
import com.marki.willow.data.dao.SleepLogDao
import com.marki.willow.data.entity.ConflictLog
import com.marki.willow.data.entity.ExerciseLog
import com.marki.willow.data.entity.SleepLog

@Database(
    entities = [SleepLog::class, ExerciseLog::class, ConflictLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {
    
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun conflictLogDao(): ConflictLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null
        
        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}