package com.marki.willow.data.repository

import com.marki.willow.data.dao.SleepLogDao
import com.marki.willow.data.entity.DataSource
import com.marki.willow.data.entity.SleepLog
import com.marki.willow.data.health.HealthConnectRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepLogRepository @Inject constructor(
    private val sleepLogDao: SleepLogDao,
    private val healthConnectRepository: HealthConnectRepository
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
    
    // Health Connect integration methods
    
    /**
     * Sync sleep data from Health Connect and save to local database
     */
    suspend fun syncFromHealthConnect(
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): SyncResult {
        return try {
            val healthConnectData = healthConnectRepository.syncSleepDataFromHealthConnect(startTime, endTime)
            
            // Filter out duplicates by checking existing data
            val existingSleepLogs = getSleepLogsByDateRange(
                startTime ?: LocalDateTime.now().minusDays(30),
                endTime ?: LocalDateTime.now().plusDays(1)
            )
            
            val newHealthConnectData = healthConnectData.filterNot { healthLog ->
                existingSleepLogs.any { existingLog ->
                    existingLog.bedTime == healthLog.bedTime && 
                    existingLog.wakeTime == healthLog.wakeTime &&
                    existingLog.source == DataSource.GOOGLE_HEALTH
                }
            }
            
            if (newHealthConnectData.isNotEmpty()) {
                insertSleepLogs(newHealthConnectData)
            }
            
            SyncResult.Success(synced = newHealthConnectData.size, skipped = healthConnectData.size - newHealthConnectData.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Upload local sleep data to Health Connect
     */
    suspend fun syncToHealthConnect(): SyncResult {
        return try {
            // Get all local sleep logs that are not from Health Connect
            val localSleepLogs = getAllSleepLogs()
            val filteredLogs = mutableListOf<SleepLog>()
            
            localSleepLogs.collect { sleepLogs ->
                filteredLogs.addAll(sleepLogs.filter { it.source != DataSource.GOOGLE_HEALTH })
            }
            
            val success = healthConnectRepository.uploadSleepDataToHealthConnect(filteredLogs)
            
            if (success) {
                SyncResult.Success(synced = filteredLogs.size, skipped = 0)
            } else {
                SyncResult.Error("Failed to upload to Health Connect")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Full bidirectional sync with Health Connect
     */
    suspend fun performFullSync(): SyncResult {
        return try {
            val fromHealthConnect = syncFromHealthConnect()
            val toHealthConnect = syncToHealthConnect()
            
            when {
                fromHealthConnect is SyncResult.Success && toHealthConnect is SyncResult.Success -> {
                    SyncResult.Success(
                        synced = fromHealthConnect.synced + toHealthConnect.synced,
                        skipped = fromHealthConnect.skipped + toHealthConnect.skipped
                    )
                }
                fromHealthConnect is SyncResult.Error -> fromHealthConnect
                toHealthConnect is SyncResult.Error -> toHealthConnect
                else -> SyncResult.Error("Unknown sync error")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}