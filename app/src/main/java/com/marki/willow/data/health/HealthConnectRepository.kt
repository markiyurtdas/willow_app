package com.marki.willow.data.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Power
import com.marki.willow.data.entity.DataSource
import com.marki.willow.data.entity.ExerciseIntensity
import com.marki.willow.data.entity.ExerciseLog
import com.marki.willow.data.entity.ExerciseType
import com.marki.willow.data.entity.SleepLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    
    /**
     * Sync sleep data from Health Connect and convert to local entities
     */
    suspend fun syncSleepDataFromHealthConnect(
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<SleepLog> {
        if (!healthConnectManager.hasAllPermissions()) {
            return emptyList()
        }
        
        val startInstant = startTime?.toInstant(ZoneOffset.UTC)
        val endInstant = endTime?.toInstant(ZoneOffset.UTC)
        
        val healthRecords = healthConnectManager.readSleepRecords(startInstant, endInstant)
        
        return healthRecords.map { healthRecord ->
            convertHealthConnectSleepToLocal(healthRecord)
        }
    }
    
    /**
     * Sync exercise data from Health Connect and convert to local entities
     */
    suspend fun syncExerciseDataFromHealthConnect(
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<ExerciseLog> {
        if (!healthConnectManager.hasAllPermissions()) {
            return emptyList()
        }
        
        val startInstant = startTime?.toInstant(ZoneOffset.UTC)
        val endInstant = endTime?.toInstant(ZoneOffset.UTC)
        
        val healthRecords = healthConnectManager.readExerciseRecords(startInstant, endInstant)
        
        return healthRecords.map { healthRecord ->
            convertHealthConnectExerciseToLocal(healthRecord)
        }
    }
    
    /**
     * Upload local sleep data to Health Connect
     */
    suspend fun uploadSleepDataToHealthConnect(sleepLogs: List<SleepLog>): Boolean {
        if (!healthConnectManager.hasAllPermissions()) {
            return false
        }
        
        val healthRecords = sleepLogs.map { sleepLog ->
            convertLocalSleepToHealthConnect(sleepLog)
        }
        
        return healthConnectManager.writeSleepRecords(healthRecords)
    }
    
    /**
     * Upload local exercise data to Health Connect
     */
    suspend fun uploadExerciseDataToHealthConnect(exerciseLogs: List<ExerciseLog>): Boolean {
        if (!healthConnectManager.hasAllPermissions()) {
            return false
        }
        
        val healthRecords = exerciseLogs.map { exerciseLog ->
            convertLocalExerciseToHealthConnect(exerciseLog)
        }
        
        return healthConnectManager.writeExerciseRecords(healthRecords)
    }
    
    /**
     * Get Health Connect availability status
     */
    fun getHealthConnectStatus(): Flow<HealthConnectStatus> = flow {
        val isAvailable = healthConnectManager.isAvailable()
        val hasPermissions = healthConnectManager.hasAllPermissions()
        
        emit(
            when {
                !isAvailable -> HealthConnectStatus.NOT_AVAILABLE
                !hasPermissions -> HealthConnectStatus.NO_PERMISSIONS
                else -> HealthConnectStatus.AVAILABLE
            }
        )
    }
    
    // Private conversion methods
    
    private fun convertHealthConnectSleepToLocal(record: SleepSessionRecord): SleepLog {
        val bedTime = record.startTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val wakeTime = record.endTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        
        return SleepLog(
            id = java.util.UUID.randomUUID().toString(),
            bedTime = bedTime,
            wakeTime = wakeTime,
            sleepQuality = 3, // Default to average, Health Connect doesn't have direct quality rating
            notes = record.notes ?: "",
            source = DataSource.GOOGLE_HEALTH
        )
    }
    
    private fun convertHealthConnectExerciseToLocal(record: ExerciseSessionRecord): ExerciseLog {
        val startTime = record.startTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val endTime = record.endTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes().toInt()
        
        // Map exercise type from Health Connect to local enum
        val exerciseType = mapHealthConnectExerciseType(record.exerciseType)
        
        // Default intensity to MODERATE if not specified
        val intensity = ExerciseIntensity.MODERATE
        
        return ExerciseLog(
            id = java.util.UUID.randomUUID().toString(),
            exerciseType = exerciseType,
            startTime = startTime,
            durationMinutes = durationMinutes,
            intensity = intensity,
            caloriesBurned = null, // We'll extract this from metadata if available
            notes = record.notes ?: "",
            source = DataSource.GOOGLE_HEALTH
        )
    }
    
    // Upload functionality disabled - Health Connect 1.2.0-alpha01 has internal constructors
    private fun convertLocalSleepToHealthConnect(sleepLog: SleepLog): SleepSessionRecord {
        throw UnsupportedOperationException(
            "Health Connect upload disabled: All Record constructors are internal in current API version. " +
            "This is a known limitation of androidx.health.connect:connect-client library."
        )
    }
    
    // Upload functionality disabled - Health Connect 1.2.0-alpha01 has internal constructors
    private fun convertLocalExerciseToHealthConnect(exerciseLog: ExerciseLog): ExerciseSessionRecord {
        throw UnsupportedOperationException(
            "Health Connect upload disabled: All Record constructors are internal in current API version. " +
            "This is a known limitation of androidx.health.connect:connect-client library."
        )
    }
    
    private fun mapHealthConnectExerciseType(exerciseType: Int): ExerciseType {
        return when (exerciseType) {
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> ExerciseType.RUNNING
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> ExerciseType.WALKING
            24 -> ExerciseType.CYCLING // EXERCISE_TYPE_CYCLING constant
            56 -> ExerciseType.SWIMMING // EXERCISE_TYPE_SWIMMING constant
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> ExerciseType.WEIGHT_TRAINING
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> ExerciseType.YOGA
            8 -> ExerciseType.BASKETBALL // EXERCISE_TYPE_BASKETBALL constant
            37 -> ExerciseType.FOOTBALL // EXERCISE_TYPE_FOOTBALL constant
            else -> ExerciseType.OTHER
        }
    }
    
    private fun mapLocalExerciseTypeToHealthConnect(exerciseType: ExerciseType): Int {
        return when (exerciseType) {
            ExerciseType.RUNNING -> ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
            ExerciseType.WALKING -> ExerciseSessionRecord.EXERCISE_TYPE_WALKING
            ExerciseType.CYCLING -> 24 // EXERCISE_TYPE_CYCLING constant
            ExerciseType.SWIMMING -> 56 // EXERCISE_TYPE_SWIMMING constant
            ExerciseType.WEIGHT_TRAINING -> ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
            ExerciseType.YOGA -> ExerciseSessionRecord.EXERCISE_TYPE_YOGA
            ExerciseType.BASKETBALL -> 8 // EXERCISE_TYPE_BASKETBALL constant
            ExerciseType.FOOTBALL -> 37 // EXERCISE_TYPE_FOOTBALL constant
            ExerciseType.OTHER -> ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
        }
    }
}

enum class HealthConnectStatus {
    NOT_AVAILABLE,
    NO_PERMISSIONS,
    AVAILABLE
}