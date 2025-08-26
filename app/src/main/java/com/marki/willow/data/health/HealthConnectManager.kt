package com.marki.willow.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    companion object {
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getWritePermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class)
        )
    }
    
    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            println("zxc HealthConnectManager: SDK status: $status")
            when (status) {
                1 -> { // SDK_AVAILABLE
                    println("zxc HealthConnectManager: Health Connect is available")
                    true
                }
                2 -> { // SDK_UNAVAILABLE  
                    println("zxc HealthConnectManager: Health Connect is unavailable")
                    false
                }
                3 -> { // SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
                    println("zxc HealthConnectManager: Health Connect provider update required")
                    false
                }
                else -> {
                    println("zxc HealthConnectManager: Unknown SDK status: $status")
                    false
                }
            }
        } catch (e: Exception) {
            println("zxc HealthConnectManager: isAvailable error: $e")
            false
        }
    }
    
    /**
     * Get SDK status
     */
    suspend fun getSdkStatus(): Int {
        return try {
            HealthConnectClient.getSdkStatus(context)
        } catch (e: Exception) {
            println("zxc HealthConnectManager: getSdkStatus error: $e")
            2 // Default to UNAVAILABLE
        }
    }
    
    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(): Boolean {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            println("zxc HealthConnectManager: Granted permissions: ${grantedPermissions.map { it }}")
            println("zxc HealthConnectManager: Required permissions: ${PERMISSIONS.map { it.toString() }}")
            
            val missingPermissions = mutableListOf<String>()
            val result = PERMISSIONS.all { permission ->
                val hasPermission = grantedPermissions.any { it == permission.toString() }
                if (!hasPermission) {
                    missingPermissions.add(permission.toString())
                }
                println("zxc HealthConnectManager: Permission ${permission} granted: $hasPermission")
                hasPermission
            }
            
            if (missingPermissions.isNotEmpty()) {
                println("zxc HealthConnectManager: Missing permissions: $missingPermissions")
            }
            
            println("zxc HealthConnectManager: Has all permissions: $result")
            result
        } catch (e: Exception) {
            println("zxc HealthConnectManager: hasAllPermissions error: $e")
            false
        }
    }
    
    /**
     * Get permission controller for requesting permissions
     */
    fun getPermissionController(): PermissionController {
        return healthConnectClient.permissionController
    }
    
    /**
     * Read sleep records from Health Connect
     */
    suspend fun readSleepRecords(
        startTime: Instant? = null,
        endTime: Instant? = null
    ): List<SleepSessionRecord> {
        return try {
            println("zxc HealthConnectManager: Starting to read sleep records...")
            
            val timeRangeFilter = if (startTime != null && endTime != null) {
                println("zxc HealthConnectManager: Using custom time range: $startTime to $endTime")
                TimeRangeFilter.between(startTime, endTime)
            } else {
                // Read last 30 days if no time range specified
                val thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60)
                val now = Instant.now()
                println("zxc HealthConnectManager: Using default time range: $thirtyDaysAgo to $now")
                TimeRangeFilter.between(thirtyDaysAgo, now)
            }
            
            val request = ReadRecordsRequest<SleepSessionRecord>(timeRangeFilter)
            println("zxc HealthConnectManager: Making API call to read sleep records...")
            
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            println("zxc HealthConnectManager: Successfully fetched ${records.size} sleep records")
            records.forEachIndexed { index, record ->
                println("zxc HealthConnectManager: Sleep record $index: ${record.startTime} to ${record.endTime}, notes: ${record.notes}")
            }
            
            records
        } catch (e: Exception) {
            println("zxc HealthConnectManager: Error reading sleep records: $e")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Read exercise records from Health Connect
     */
    suspend fun readExerciseRecords(
        startTime: Instant? = null,
        endTime: Instant? = null
    ): List<ExerciseSessionRecord> {
        return try {
            println("zxc HealthConnectManager: Starting to read exercise records...")
            
            val timeRangeFilter = if (startTime != null && endTime != null) {
                println("zxc HealthConnectManager: Using custom time range: $startTime to $endTime")
                TimeRangeFilter.between(startTime, endTime)
            } else {
                // Read last 30 days if no time range specified
                val thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60)
                val now = Instant.now()
                println("zxc HealthConnectManager: Using default time range: $thirtyDaysAgo to $now")
                TimeRangeFilter.between(thirtyDaysAgo, now)
            }
            
            val request = ReadRecordsRequest<ExerciseSessionRecord>(timeRangeFilter)
            println("zxc HealthConnectManager: Making API call to read exercise records...")
            
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            println("zxc HealthConnectManager: Successfully fetched ${records.size} exercise records")
            records.forEachIndexed { index, record ->
                println("zxc HealthConnectManager: Exercise record $index: ${record.startTime} to ${record.endTime}, type: ${record.exerciseType}, notes: ${record.notes}")
            }
            
            records
        } catch (e: Exception) {
            println("zxc HealthConnectManager: Error reading exercise records: $e")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Write sleep records to Health Connect
     */
    suspend fun writeSleepRecords(records: List<SleepSessionRecord>): Boolean {
        return try {
            healthConnectClient.insertRecords(records)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Write exercise records to Health Connect
     */
    suspend fun writeExerciseRecords(records: List<ExerciseSessionRecord>): Boolean {
        return try {
            healthConnectClient.insertRecords(records)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get Health Connect availability as a flow
     */
    fun getAvailabilityFlow(): Flow<Boolean> = flow {
        emit(isAvailable())
    }
    
    /**
     * Get permission status as a flow
     */
    fun getPermissionStatusFlow(): Flow<Boolean> = flow {
        emit(hasAllPermissions())
    }
}