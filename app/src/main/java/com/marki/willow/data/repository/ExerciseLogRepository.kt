package com.marki.willow.data.repository

import com.marki.willow.data.dao.ExerciseLogDao
import com.marki.willow.data.entity.ConflictLog
import com.marki.willow.data.entity.ConflictType
import com.marki.willow.data.entity.DataSource
import com.marki.willow.data.entity.ExerciseLog
import com.marki.willow.data.health.HealthConnectRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseLogRepository @Inject constructor(
    private val exerciseLogDao: ExerciseLogDao,
    private val healthConnectRepository: HealthConnectRepository,
    private val conflictLogRepository: ConflictLogRepository
) {
    
    fun getAllExerciseLogs(): Flow<List<ExerciseLog>> = exerciseLogDao.getAllExerciseLogs()
    
    suspend fun getExerciseLogById(id: String): ExerciseLog? = exerciseLogDao.getExerciseLogById(id)
    
    suspend fun getExerciseLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ExerciseLog> =
        exerciseLogDao.getExerciseLogsByDateRange(startDate, endDate)
    
    fun getConflictedExerciseLogs(): Flow<List<ExerciseLog>> = exerciseLogDao.getConflictedExerciseLogs()
    
    suspend fun getExerciseLogsByType(type: String): List<ExerciseLog> = 
        exerciseLogDao.getExerciseLogsByType(type)
    
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.insertExerciseLog(exerciseLog)
    
    /**
     * Insert exercise log with conflict detection against existing entries
     */
    suspend fun insertExerciseLogWithConflictDetection(exerciseLog: ExerciseLog): Pair<Boolean, List<ConflictLog>> {
        val conflicts = mutableListOf<ConflictLog>()
        
        // Get existing exercise logs in a reasonable time range around the new entry
        val exerciseStartTime = exerciseLog.startTime
        val exerciseEndTime = exerciseLog.startTime.plusMinutes(exerciseLog.durationMinutes.toLong())
        val startRange = exerciseStartTime.minusHours(12) // 12 hours before
        val endRange = exerciseEndTime.plusHours(12) // 12 hours after
        val existingExerciseLogs = getExerciseLogsByDateRange(startRange, endRange)
        
        // Check for conflicts with existing entries
        existingExerciseLogs.forEach { existingLog ->
            // Don't check conflict with itself if updating
            if (existingLog.id == exerciseLog.id) return@forEach
            
            // Check for time overlap
            if (doExerciseTimesOverlap(existingLog, exerciseLog)) {
                val conflictDetails = buildString {
                    append("Exercise time overlap detected: ")
                    append("New ${exerciseLog.source.name} ${exerciseLog.exerciseType.name} exercise (${exerciseLog.startTime} for ${exerciseLog.durationMinutes}min) ")
                    append("overlaps with existing ${existingLog.source.name} ${existingLog.exerciseType.name} exercise (${existingLog.startTime} for ${existingLog.durationMinutes}min)")
                }
                
                val conflict = ConflictLog(
                    id = java.util.UUID.randomUUID().toString(),
                    conflictType = ConflictType.EXERCISE_TIME_OVERLAP,
                    primaryDataId = existingLog.id,
                    conflictingDataId = exerciseLog.id,
                    conflictDetails = conflictDetails
                )
                conflicts.add(conflict)
                
                println("zxc ExerciseLogRepository: Manual entry conflict - $conflictDetails")
            }
        }
        
        // Log conflicts if found
        if (conflicts.isNotEmpty()) {
            conflicts.forEach { conflict ->
                conflictLogRepository.insertConflictLog(conflict)
            }
            println("zxc ExerciseLogRepository: Found ${conflicts.size} conflicts for manual exercise entry")
        }
        
        // Insert the exercise log regardless of conflicts (same behavior as Health Connect sync)
        insertExerciseLog(exerciseLog)
        
        return Pair(true, conflicts)
    }
    
    suspend fun insertExerciseLogs(exerciseLogs: List<ExerciseLog>) = exerciseLogDao.insertExerciseLogs(exerciseLogs)
    
    suspend fun updateExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.updateExerciseLog(exerciseLog)
    
    suspend fun deleteExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.deleteExerciseLog(exerciseLog)
    
    suspend fun deleteExerciseLogById(id: String) = exerciseLogDao.deleteExerciseLogById(id)
    
    // Health Connect integration methods
    
    /**
     * Sync exercise data from Health Connect and save to local database
     */
    suspend fun syncFromHealthConnect(
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): SyncResult {
        return try {
            val healthConnectData = healthConnectRepository.syncExerciseDataFromHealthConnect(startTime, endTime)
            
            // Filter out duplicates by checking existing data
            val existingExerciseLogs = getExerciseLogsByDateRange(
                startTime ?: LocalDateTime.now().minusDays(30),
                endTime ?: LocalDateTime.now().plusDays(1)
            )
            
            val newHealthConnectData = healthConnectData.filterNot { healthLog ->
                existingExerciseLogs.any { existingLog ->
                    existingLog.startTime == healthLog.startTime && 
                    existingLog.exerciseType == healthLog.exerciseType &&
                    existingLog.durationMinutes == healthLog.durationMinutes &&
                    existingLog.source == DataSource.GOOGLE_HEALTH
                }
            }
            
            if (newHealthConnectData.isNotEmpty()) {
                insertExerciseLogs(newHealthConnectData)
            }
            
            SyncResult.Success(synced = newHealthConnectData.size, skipped = healthConnectData.size - newHealthConnectData.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Upload local exercise data to Health Connect
     */
    suspend fun syncToHealthConnect(): SyncResult {
        return try {
            // Get all local exercise logs that are not from Health Connect
            val localExerciseLogs = getAllExerciseLogs()
            val filteredLogs = mutableListOf<ExerciseLog>()
            
            localExerciseLogs.collect { exerciseLogs ->
                filteredLogs.addAll(exerciseLogs.filter { it.source != DataSource.GOOGLE_HEALTH })
            }
            
            val success = healthConnectRepository.uploadExerciseDataToHealthConnect(filteredLogs)
            
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
     * Check if two exercise sessions have overlapping times
     */
    private fun doExerciseTimesOverlap(exercise1: ExerciseLog, exercise2: ExerciseLog): Boolean {
        // Calculate end times for both exercises
        val exercise1Start = exercise1.startTime
        val exercise1End = exercise1.startTime.plusMinutes(exercise1.durationMinutes.toLong())
        val exercise2Start = exercise2.startTime
        val exercise2End = exercise2.startTime.plusMinutes(exercise2.durationMinutes.toLong())
        
        // Check for any overlap
        return !(exercise1End.isBefore(exercise2Start) || exercise1End.isEqual(exercise2Start) ||
                 exercise2End.isBefore(exercise1Start) || exercise2End.isEqual(exercise1Start))
    }
}