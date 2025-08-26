package com.marki.willow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    initialDateTime: LocalDateTime
) {
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }
    var showingDatePicker by remember { mutableStateOf(true) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Toggle buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = showingDatePicker,
                        onClick = { showingDatePicker = true },
                        label = { Text("Date") }
                    )
                    FilterChip(
                        selected = !showingDatePicker,
                        onClick = { showingDatePicker = false },
                        label = { Text("Time") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (showingDatePicker) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.height(400.dp)
                    )
                } else {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Current selection display
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year} at ${String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Update selected values from picker states
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    }
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    
                    val selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
                    onDateTimeSelected(selectedDateTime)
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
    
    // Update selected values when picker states change
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
        }
    }
    
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
    }
}