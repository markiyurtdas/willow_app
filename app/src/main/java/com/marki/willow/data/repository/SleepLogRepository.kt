package com.marki.willow.data.repository

import com.marki.willow.data.dao.SleepLogDao
import com.marki.willow.data.entity.ConflictLog
import com.marki.willow.data.entity.ConflictType
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
    private val healthConnectRepository: HealthConnectRepository,
    private val conflictLogRepository: ConflictLogRepository
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
            
            val (newHealthConnectData, conflicts) = filterHealthConnectDataWithConflictDetection(
                healthConnectData, 
                existingSleepLogs
            )
            
            // Log conflicts if found
            if (conflicts.isNotEmpty()) {
                conflicts.forEach { conflict ->
                    conflictLogRepository.insertConflictLog(conflict)
                }
                println("zxc SleepLogRepository: Found ${conflicts.size} sleep conflicts")
                conflicts.forEach { conflict ->
                    println("zxc SleepLogRepository: Conflict - ${conflict.conflictDetails}")
                }
            }
            
            if (newHealthConnectData.isNotEmpty()) {
                insertSleepLogs(newHealthConnectData)
            }
            
            val totalSkipped = healthConnectData.size - newHealthConnectData.size
            SyncResult.Success(
                synced = newHealthConnectData.size, 
                skipped = totalSkipped
            )
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
    
    /**
     * Filter Health Connect data and detect conflicts with existing sleep logs
     */
    private suspend fun filterHealthConnectDataWithConflictDetection(
        healthConnectData: List<SleepLog>,
        existingSleepLogs: List<SleepLog>
    ): Pair<List<SleepLog>, List<ConflictLog>> {
        val newData = mutableListOf<SleepLog>()
        val conflicts = mutableListOf<ConflictLog>()
        
        healthConnectData.forEach { healthLog ->
            var hasConflict = false
            var exactDuplicate = false
            
            existingSleepLogs.forEach { existingLog ->
                // Check for exact duplicate (same times and same source)
                if (existingLog.bedTime == healthLog.bedTime && 
                    existingLog.wakeTime == healthLog.wakeTime &&
                    existingLog.source == DataSource.GOOGLE_HEALTH) {
                    exactDuplicate = true
                    return@forEach
                }
                
                // Check for time overlap between different sources
                if (existingLog.source != DataSource.GOOGLE_HEALTH && 
                    doSleepTimesOverlap(existingLog, healthLog)) {
                    
                    hasConflict = true
                    val conflictDetails = buildString {
                        append("Sleep time overlap detected: ")
                        append("Existing ${existingLog.source.name} sleep (${existingLog.bedTime} - ${existingLog.wakeTime}) ")
                        append("overlaps with Health Connect sleep (${healthLog.bedTime} - ${healthLog.wakeTime})")
                    }
                    
                    val conflict = ConflictLog(
                        id = java.util.UUID.randomUUID().toString(),
                        conflictType = ConflictType.SLEEP_TIME_OVERLAP,
                        primaryDataId = existingLog.id,
                        conflictingDataId = healthLog.id,
                        conflictDetails = conflictDetails
                    )
                    conflicts.add(conflict)
                    
                    println("zxc SleepLogRepository: Sleep overlap conflict - $conflictDetails")
                }
            }
            
            // Only add if it's not an exact duplicate
            // Still add conflicting data but log the conflict
            if (!exactDuplicate) {
                newData.add(healthLog)
            }
        }
        
        return Pair(newData, conflicts)
    }
    
    /**
     * Check if two sleep sessions have overlapping times
     */
    private fun doSleepTimesOverlap(sleep1: SleepLog, sleep2: SleepLog): Boolean {
        // Convert to comparable instants
        val sleep1Start = sleep1.bedTime
        val sleep1End = sleep1.wakeTime
        val sleep2Start = sleep2.bedTime
        val sleep2End = sleep2.wakeTime
        
        // Check for any overlap
        return !(sleep1End.isBefore(sleep2Start) || sleep1End.isEqual(sleep2Start) ||
                 sleep2End.isBefore(sleep1Start) || sleep2End.isEqual(sleep1Start))
    }
}