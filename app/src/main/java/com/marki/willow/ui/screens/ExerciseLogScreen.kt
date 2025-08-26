package com.marki.willow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.marki.willow.data.entity.ExerciseIntensity
import com.marki.willow.data.entity.ExerciseType
import com.marki.willow.ui.viewmodel.ExerciseLogViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLogScreen(
    navController: NavHostController,
    viewModel: ExerciseLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showStartTimePicker by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val exerciseType by viewModel.exerciseType.collectAsStateWithLifecycle()
        val startTime by viewModel.startTime.collectAsStateWithLifecycle()
        val durationMinutes by viewModel.durationMinutes.collectAsStateWithLifecycle()
        val intensity by viewModel.intensity.collectAsStateWithLifecycle()
        val caloriesBurned by viewModel.caloriesBurned.collectAsStateWithLifecycle()
        val notes by viewModel.notes.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        
        var showTypeDropdown by remember { mutableStateOf(false) }
        var showIntensityDropdown by remember { mutableStateOf(false) }
        var caloriesText by remember { mutableStateOf("") }
        
        LaunchedEffect(caloriesBurned) {
            caloriesText = caloriesBurned?.toString() ?: ""
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Log Your Exercise",
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
                            text = "Exercise Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = showTypeDropdown,
                            onExpandedChange = { showTypeDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = exerciseType.name.replace("_", " "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Exercise Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false }
                            ) {
                                ExerciseType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name.replace("_", " ")) },
                                        onClick = {
                                            viewModel.setExerciseType(type)
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                showStartTimePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (startTime != null) {
                                    "Start Time: ${startTime!!.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}"
                                } else {
                                    "Set Start Time"
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
                            text = "Duration & Intensity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = durationMinutes.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { duration -> 
                                    if (duration > 0) viewModel.setDurationMinutes(duration)
                                }
                            },
                            label = { Text("Duration (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = showIntensityDropdown,
                            onExpandedChange = { showIntensityDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = intensity.name.replace("_", " "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Intensity") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIntensityDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showIntensityDropdown,
                                onDismissRequest = { showIntensityDropdown = false }
                            ) {
                                ExerciseIntensity.values().forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(level.name.replace("_", " ")) },
                                        onClick = {
                                            viewModel.setIntensity(level)
                                            showIntensityDropdown = false
                                        }
                                    )
                                }
                            }
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
                            text = "Optional Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { 
                                caloriesText = it
                                val calories = it.toIntOrNull()
                                viewModel.setCaloriesBurned(calories)
                            },
                            label = { Text("Calories Burned (optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = viewModel::setNotes,
                            placeholder = { Text("Exercise notes, goals achieved, etc.") },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        viewModel.saveExerciseLog()
                        navController.popBackStack()
                    },
                    enabled = !isLoading && startTime != null && durationMinutes > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save Exercise Log")
                    }
                }
            }
        }
        
        if (showStartTimePicker) {
            TimePickerDialog(
                onTimeSelected = { hour: Int, minute: Int ->
                    val now = LocalDateTime.now()
                    val selectedTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                    viewModel.setStartTime(selectedTime)
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false },
                title = "Select Start Time",
                initialTime = startTime ?: LocalDateTime.now()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    initialTime: LocalDateTime
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}