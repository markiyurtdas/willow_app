package com.marki.willow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.marki.willow.ui.viewmodel.SleepLogViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLogScreen(
    navController: NavHostController,
    viewModel: SleepLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showBedTimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Sleep") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val bedTime by viewModel.bedTime.collectAsStateWithLifecycle()
        val wakeTime by viewModel.wakeTime.collectAsStateWithLifecycle()
        val sleepQuality by viewModel.sleepQuality.collectAsStateWithLifecycle()
        val notes by viewModel.notes.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Log Your Sleep",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sleep Times",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedButton(
                            onClick = {
                                showBedTimePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (bedTime != null) {
                                    "Bed Time: ${bedTime!!.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}"
                                } else {
                                    "Set Bed Time"
                                }
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                showWakeTimePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (wakeTime != null) {
                                    "Wake Time: ${wakeTime!!.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}"
                                } else {
                                    "Set Wake Time"
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sleep Quality (1-5)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (rating in 1..5) {
                                IconButton(
                                    onClick = { viewModel.setSleepQuality(rating) }
                                ) {
                                    Icon(
                                        imageVector = if (rating <= sleepQuality) Icons.Default.Star else Icons.Outlined.Star,
                                        contentDescription = "$rating stars",
                                        tint = if (rating <= sleepQuality) Color(0xFFFFD700) else Color.Gray
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = when (sleepQuality) {
                                1 -> "Very Poor"
                                2 -> "Poor"
                                3 -> "Average"
                                4 -> "Good"
                                5 -> "Excellent"
                                else -> "Rate your sleep"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Notes (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = viewModel::setNotes,
                            placeholder = { Text("How was your sleep? Any dreams?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        viewModel.saveSleepLog()
                        navController.popBackStack()
                    },
                    enabled = !isLoading && bedTime != null && wakeTime != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save Sleep Log")
                    }
                }
            }
        }
        
        if (showBedTimePicker) {
            com.marki.willow.ui.components.DateTimePickerDialog(
                onDateTimeSelected = { selectedDateTime ->
                    viewModel.setBedTime(selectedDateTime)
                    showBedTimePicker = false
                },
                onDismiss = { showBedTimePicker = false },
                title = "Select Bed Time",
                initialDateTime = bedTime ?: LocalDateTime.now().minusHours(8)
            )
        }
        
        if (showWakeTimePicker) {
            com.marki.willow.ui.components.DateTimePickerDialog(
                onDateTimeSelected = { selectedDateTime ->
                    viewModel.setWakeTime(selectedDateTime)
                    showWakeTimePicker = false
                },
                onDismiss = { showWakeTimePicker = false },
                title = "Select Wake Time",
                initialDateTime = wakeTime ?: LocalDateTime.now()
            )
        }
    }
}

