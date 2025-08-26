package com.marki.willow.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marki.willow.data.health.HealthConnectPermissionManager
import com.marki.willow.data.health.PermissionState
import com.marki.willow.data.repository.ExerciseLogRepository
import com.marki.willow.data.repository.SleepLogRepository
import com.marki.willow.data.repository.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sleepLogRepository: SleepLogRepository,
    private val exerciseLogRepository: ExerciseLogRepository,
    private val permissionManager: HealthConnectPermissionManager
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.UNKNOWN)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    init {
        // Set up lifecycle-aware permission state monitoring
        viewModelScope.launch {
            permissionManager.permissionState.collect { state ->
                println("zxc SettingsViewModel: Permission state changed to: $state")
                _permissionState.value = state
            }
        }
    }

    fun checkHealthConnectPermissions() {
        viewModelScope.launch {
            println("zxc SettingsViewModel: Checking Health Connect permissions...")
            permissionManager.checkPermissionStatus()
            // Use current value instead of infinite collect
            val state = permissionManager.permissionState.value
            println("zxc SettingsViewModel: Permission state: $state")
            _permissionState.value = state
        }
    }

    fun syncWithHealthConnect() {
        viewModelScope.launch {
            println("zxc SettingsViewModel: syncWithHealthConnect called, permission state: ${_permissionState.value}")
            
            if (_permissionState.value != PermissionState.GRANTED) {
                println("zxc SettingsViewModel: Permissions not granted, showing message")
                _syncMessage.value = "Health Connect permissions not granted"
                return@launch
            }

            println("zxc SettingsViewModel: Starting sync...")
            _syncState.value = SyncState.Syncing
            _syncMessage.value = "Syncing with Health Connect..."

            try {
                // Sync sleep data
                val sleepResult = sleepLogRepository.performFullSync()
                
                // Sync exercise data
                val exerciseResult = exerciseLogRepository.performFullSync()

                when {
                    sleepResult is SyncResult.Success && exerciseResult is SyncResult.Success -> {
                        val totalSynced = sleepResult.synced + exerciseResult.synced
                        val totalSkipped = sleepResult.skipped + exerciseResult.skipped
                        
                        _syncState.value = SyncState.Success
                        _syncMessage.value = "Sync completed! $totalSynced items synced, $totalSkipped skipped"
                    }
                    sleepResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Sleep sync failed: ${sleepResult.message}"
                    }
                    exerciseResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Exercise sync failed: ${exerciseResult.message}"
                    }
                    else -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Unknown sync error occurred"
                    }
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error
                _syncMessage.value = "Sync failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun syncFromHealthConnect() {
        viewModelScope.launch {
            if (_permissionState.value != PermissionState.GRANTED) {
                _syncMessage.value = "Health Connect permissions not granted"
                return@launch
            }

            _syncState.value = SyncState.Syncing
            _syncMessage.value = "Importing from Health Connect..."

            try {
                val sleepResult = sleepLogRepository.syncFromHealthConnect()
                val exerciseResult = exerciseLogRepository.syncFromHealthConnect()

                when {
                    sleepResult is SyncResult.Success && exerciseResult is SyncResult.Success -> {
                        val totalSynced = sleepResult.synced + exerciseResult.synced
                        
                        _syncState.value = SyncState.Success
                        _syncMessage.value = "Import completed! $totalSynced new items imported"
                    }
                    sleepResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Import failed: ${sleepResult.message}"
                    }
                    exerciseResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Import failed: ${exerciseResult.message}"
                    }
                    else -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Unknown import error occurred"
                    }
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error
                _syncMessage.value = "Import failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun exportToHealthConnect() {
        viewModelScope.launch {
            if (_permissionState.value != PermissionState.GRANTED) {
                _syncMessage.value = "Health Connect permissions not granted"
                return@launch
            }

            _syncState.value = SyncState.Syncing
            _syncMessage.value = "Exporting to Health Connect..."

            try {
                val sleepResult = sleepLogRepository.syncToHealthConnect()
                val exerciseResult = exerciseLogRepository.syncToHealthConnect()

                when {
                    sleepResult is SyncResult.Success && exerciseResult is SyncResult.Success -> {
                        val totalSynced = sleepResult.synced + exerciseResult.synced
                        
                        _syncState.value = SyncState.Success
                        _syncMessage.value = "Export completed! $totalSynced items exported"
                    }
                    sleepResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Export failed: ${sleepResult.message}"
                    }
                    exerciseResult is SyncResult.Error -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Export failed: ${exerciseResult.message}"
                    }
                    else -> {
                        _syncState.value = SyncState.Error
                        _syncMessage.value = "Unknown export error occurred"
                    }
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error
                _syncMessage.value = "Export failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = ""
        _syncState.value = SyncState.Idle
    }

    fun onPermissionResult(grantedPermissions: Set<String>) {
        viewModelScope.launch {
            println("zxc SettingsViewModel: onPermissionResult called with: $grantedPermissions")
            
            // Handle the permission result
            permissionManager.handlePermissionResult(grantedPermissions)
            
            // Refresh permission state
            permissionManager.checkPermissionStatus()
            
            // Update UI message based on result
            if (grantedPermissions.isNotEmpty()) {
                _syncMessage.value = "Health Connect permissions granted successfully!"
                _syncState.value = SyncState.Success
            } else {
                _syncMessage.value = "Health Connect permissions were denied. You can try again or grant them manually in Settings."
                _syncState.value = SyncState.Error
            }
        }
    }
    
    fun requestPermissions() {
        viewModelScope.launch {
            println("zxc SettingsViewModel: requestPermissions called - this should not be used anymore")
            _syncMessage.value = "Use the permission launcher instead"
        }
    }
    
    fun openHealthConnectSettings(): Intent {
        println("zxc SettingsViewModel: Opening Health Connect settings")
        return Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
    }
    
    fun checkHealthConnectStatusManually() {
        viewModelScope.launch {
            println("zxc SettingsViewModel: Manual Health Connect status check started")
            _syncMessage.value = "Checking Health Connect status..."
            _syncState.value = SyncState.Syncing
            
            try {
                // Give Health Connect time to initialize if needed
                delay(2000)
                
                // Retry mechanism - try 3 times with delay
                for (attempt in 0 until 3) {
                    println("zxc SettingsViewModel: Check attempt ${attempt + 1}")
                    
                    permissionManager.checkPermissionStatus()
                    
                    // Wait a bit for state to update
                    delay(1000)
                    
                    val currentState = permissionManager.permissionState.value
                    println("zxc SettingsViewModel: Current state after attempt ${attempt + 1}: $currentState")
                    
                    // If we got a definitive state (not UNKNOWN), break
                    if (currentState != PermissionState.UNKNOWN) {
                        break
                    }
                    
                    // Wait before next attempt (except for last attempt)
                    if (attempt < 2) {
                        delay(2000)
                    }
                }
                
                // Use current value instead of infinite collect
                val state = permissionManager.permissionState.value
                println("zxc SettingsViewModel: Final permission state: $state")
                _permissionState.value = state
                
                _syncMessage.value = when (state) {
                    PermissionState.GRANTED -> "Health Connect is ready!"
                    PermissionState.DENIED, PermissionState.UNKNOWN -> "Health Connect found but permissions needed"
                    PermissionState.UPDATE_REQUIRED -> "Health Connect needs update or restart"
                    PermissionState.NOT_AVAILABLE -> "Health Connect is not available"
                    PermissionState.ERROR -> "Error checking Health Connect"
                }
                _syncState.value = SyncState.Idle
                
            } catch (e: Exception) {
                println("zxc SettingsViewModel: Manual check error: $e")
                _syncMessage.value = "Failed to check Health Connect status"
                _syncState.value = SyncState.Error
            }
        }
    }
}

enum class SyncState {
    Idle,
    Syncing,
    Success,
    Error
}