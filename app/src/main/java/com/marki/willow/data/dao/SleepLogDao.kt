package com.marki.willow.data.dao

import androidx.room.*
import com.marki.willow.data.entity.SleepLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SleepLogDao {
    
    @Query("SELECT * FROM sleep_logs ORDER BY bedTime DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>
    
    @Query("SELECT * FROM sleep_logs WHERE id = :id")
    suspend fun getSleepLogById(id: String): SleepLog?
    
    @Query("SELECT * FROM sleep_logs WHERE bedTime BETWEEN :startDate AND :endDate ORDER BY bedTime DESC")
    suspend fun getSleepLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<SleepLog>
    
    @Query("SELECT * FROM sleep_logs WHERE isConflicted = 1")
    fun getConflictedSleepLogs(): Flow<List<SleepLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(sleepLog: SleepLog)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLogs(sleepLogs: List<SleepLog>)
    
    @Update
    suspend fun updateSleepLog(sleepLog: SleepLog)
    
    @Delete
    suspend fun deleteSleepLog(sleepLog: SleepLog)
    
    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLogById(id: String)
}