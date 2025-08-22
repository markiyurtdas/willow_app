package com.marki.willow.data.repository

import com.marki.willow.data.dao.SleepLogDao
import com.marki.willow.data.entity.SleepLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepLogRepository @Inject constructor(
    private val sleepLogDao: SleepLogDao
) {
    
    fun getAllSleepLogs(): Flow<List<SleepLog>> = sleepLogDao.getAllSleepLogs()
    
    suspend fun getSleepLogById(id: String): SleepLog? = sleepLogDao.getSleepLogById(id)
    
    suspend fun getSleepLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<SleepLog> =
        sleepLogDao.getSleepLogsByDateRange(startDate, endDate)
    
    fun getConflictedSleepLogs(): Flow<List<SleepLog>> = sleepLogDao.getConflictedSleepLogs()
    
    suspend fun insertSleepLog(sleepLog: SleepLog) = sleepLogDao.insertSleepLog(sleepLog)
    
    suspend fun insertSleepLogs(sleepLogs: List<SleepLog>) = sleepLogDao.insertSleepLogs(sleepLogs)
    
    suspend fun updateSleepLog(sleepLog: SleepLog) = sleepLogDao.updateSleepLog(sleepLog)
    
    suspend fun deleteSleepLog(sleepLog: SleepLog) = sleepLogDao.deleteSleepLog(sleepLog)
    
    suspend fun deleteSleepLogById(id: String) = sleepLogDao.deleteSleepLogById(id)
}