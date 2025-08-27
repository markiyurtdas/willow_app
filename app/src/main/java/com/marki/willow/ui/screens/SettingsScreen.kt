package com.marki.willow.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.marki.willow.data.health.PermissionState
import com.marki.willow.ui.viewmodel.SettingsViewModel
import com.marki.willow.ui.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    
    // Health Connect permissions we need
    val healthConnectPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )
    
    // Create permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = { granted ->
            println("zxc SettingsScreen: Permission result: $granted")
            // Refresh permission state after user grants/denies permissions
            viewModel.onPermissionResult(granted)
        }
    )
    
    // Show snackbar for sync messages
    LaunchedEffect(syncMessage) {
        if (syncMessage.isNotEmpty()) {
            // Auto-clear message after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearSyncMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                HealthConnectSection(
                    syncState = syncState,
                    permissionState = permissionState,
                    syncMessage = syncMessage,
                    onSyncAll = viewModel::syncWithHealthConnect,
                    onImport = viewModel::syncFromHealthConnect,
                    onExport = viewModel::exportToHealthConnect,
                    onRequestPermissions = {
                        println("zxc SettingsScreen: Launching permission request")
                        permissionLauncher.launch(healthConnectPermissions)
                    },
                    onOpenHealthConnect = {
                        println("zxc SettingsScreen: Opening Health Connect app")
                        try {
                            context.startActivity(viewModel.openHealthConnectApp())
                        } catch (e: Exception) {
                            println("zxc SettingsScreen: Failed to open Health Connect app: $e")
                        }
                    },
                    onImportFromHealthConnect = viewModel::importFromHealthConnect
                )
            }
            

        }
    }
}

@Composable
private fun HealthConnectSection(
    syncState: SyncState,
    permissionState: PermissionState,
    syncMessage: String,
    onSyncAll: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onImportFromHealthConnect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health Connect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                PermissionStatusChip(permissionState = permissionState)
            }
            
            Text(
                text = "Sync your health data with Google Health Connect and other health apps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Go to Health Connect > App Permissions > find and allow Willow to access your data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            

            
            when (permissionState) {
                PermissionState.NOT_AVAILABLE -> {
                    Text(
                        text = "Health Connect is not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                PermissionState.UPDATE_REQUIRED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Health Connect provider update required (Status 3)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = onOpenHealthConnect,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Health Connect App")
                        }
                        
                        OutlinedButton(
                            onClick = onImportFromHealthConnect,
                            enabled = syncState != SyncState.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import from Health Connect (Works Despite Status)")
                        }
                    }
                }
                PermissionState.UNKNOWN -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Health Connect status checking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = onOpenHealthConnect,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Health Connect App")
                        }
                        
                        OutlinedButton(
                            onClick = onImportFromHealthConnect,
                            enabled = syncState != SyncState.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import from Health Connect")
                        }
                    }
                }
                PermissionState.DENIED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Health Connect permissions are required to sync your health data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = onRequestPermissions,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Health Connect Permissions")
                        }
                        
                        Text(
                            text = "This will open the Health Connect permissions dialog",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                PermissionState.GRANTED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSyncAll,
                            enabled = syncState != SyncState.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (syncState == SyncState.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Full Sync (Two-way)")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onImport,
                                enabled = syncState != SyncState.Syncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import")
                            }
                            
                            OutlinedButton(
                                onClick = onExport,
                                enabled = syncState != SyncState.Syncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export")
                            }
                        }
                        
                        // Import from Health Connect button
                        OutlinedButton(
                            onClick = onImportFromHealthConnect,
                            enabled = syncState != SyncState.Syncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import from Health Connect")
                        }
                        
                        // Open Health Connect app button
                        OutlinedButton(
                            onClick = onOpenHealthConnect,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🏥 Open Health Connect App")
                        }
                    }
                }
                PermissionState.ERROR -> {
                    Text(
                        text = "Error accessing Health Connect",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    OutlinedButton(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusChip(permissionState: PermissionState) {
    val (text, color) = when (permissionState) {
        PermissionState.GRANTED -> "Connected" to MaterialTheme.colorScheme.primary
        PermissionState.DENIED -> "Not Connected" to MaterialTheme.colorScheme.error
        PermissionState.NOT_AVAILABLE -> "Unavailable" to MaterialTheme.colorScheme.outline
        PermissionState.UPDATE_REQUIRED -> "Update Required" to MaterialTheme.colorScheme.error
        PermissionState.ERROR -> "Error" to MaterialTheme.colorScheme.error
        PermissionState.UNKNOWN -> "Checking..." to MaterialTheme.colorScheme.outline
    }
    
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}