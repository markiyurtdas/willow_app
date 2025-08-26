package com.marki.willow.data.health

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val healthConnectManager: HealthConnectManager
) {
    
    private val _permissionState = MutableStateFlow(PermissionState.UNKNOWN)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    companion object {
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getWritePermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class)
        )
    }
    
    /**
     * Check current permission status and update state
     */
    suspend fun checkPermissionStatus() {
        try {
            val sdkStatus = healthConnectManager.getSdkStatus()
            println("zxc PermissionManager: is available: ${healthConnectManager.isAvailable()}");
            println("zxc PermissionManager: ahsAllPermissions: ${healthConnectManager.hasAllPermissions()}");
            println("zxc PermissionManager: SDK status: $sdkStatus")
            
            when (sdkStatus) {
                1 -> { // SDK_AVAILABLE
                    val hasAllPermissions = healthConnectManager.hasAllPermissions()
                    _permissionState.value = if (hasAllPermissions) {
                        PermissionState.GRANTED
                    } else {
                        PermissionState.DENIED
                    }
                }
                2 -> { // SDK_UNAVAILABLE
                    _permissionState.value = PermissionState.NOT_AVAILABLE
                }
                3 -> { // SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
                    _permissionState.value = PermissionState.UPDATE_REQUIRED
                }
                else -> {
                    _permissionState.value = PermissionState.ERROR
                }
            }
            
        } catch (e: Exception) {
            _permissionState.value = PermissionState.ERROR
            println("zxc error in permisson: " + e.toString())
        }
    }
    
    /**
     * Get permission controller for requesting permissions
     */
    fun getPermissionController(): PermissionController {
        return healthConnectManager.getPermissionController()
    }
    
    /**
     * Create permission request contract
     */
    fun createPermissionRequestContract() = PermissionController.createRequestPermissionResultContract()
    
    /**
     * Handle permission request result
     */
    suspend fun handlePermissionResult(grantedPermissions: Set<String>) {
        val grantedHealthPermissions = healthConnectManager.getPermissionController().getGrantedPermissions()
        val allGranted = REQUIRED_PERMISSIONS.all { permission ->
            grantedHealthPermissions.any { it == permission.toString() }
        }
        
        _permissionState.value = if (allGranted) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }
    
    /**
     * Get missing permissions that need to be requested
     */
    suspend fun getMissingPermissions(): Set<String> {
        return try {
            val grantedPermissionStrings = healthConnectManager.getPermissionController().getGrantedPermissions()
            REQUIRED_PERMISSIONS.map { it.toString() }.toSet() - grantedPermissionStrings
        } catch (e: Exception) {
            REQUIRED_PERMISSIONS.map { it.toString() }.toSet()
        }
    }
    
    /**
     * Check if specific permission is granted
     */
    suspend fun isPermissionGranted(permission: HealthPermission): Boolean {
        return try {
            val grantedPermissionStrings = healthConnectManager.getPermissionController().getGrantedPermissions()
            grantedPermissionStrings.any { it == permission.toString() }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get user-friendly permission description
     */
    fun getPermissionDescription(permissionString: String): String {
        return when (permissionString) {
            HealthPermission.getReadPermission(SleepSessionRecord::class).toString() -> 
                "Read your sleep data from Health Connect"
            HealthPermission.getWritePermission(SleepSessionRecord::class).toString() -> 
                "Save your sleep data to Health Connect"
            HealthPermission.getReadPermission(ExerciseSessionRecord::class).toString() -> 
                "Read your exercise data from Health Connect"
            HealthPermission.getWritePermission(ExerciseSessionRecord::class).toString() -> 
                "Save your exercise data to Health Connect"
            else -> "Access health data"
        }
    }
    
    /**
     * Get all required permissions with descriptions
     */
    fun getRequiredPermissionsWithDescriptions(): List<Pair<String, String>> {
        return REQUIRED_PERMISSIONS.toList().map { permission ->
            val permissionString = permission.toString()
            Pair(permissionString, getPermissionDescription(permissionString))
        }
    }
}

enum class PermissionState {
    UNKNOWN,
    NOT_AVAILABLE,
    UPDATE_REQUIRED,
    GRANTED,
    DENIED,
    ERROR
}