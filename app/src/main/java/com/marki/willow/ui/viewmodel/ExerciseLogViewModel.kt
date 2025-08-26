package com.marki.willow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marki.willow.data.entity.DataSource
import com.marki.willow.data.entity.ExerciseIntensity
import com.marki.willow.data.entity.ExerciseLog
import com.marki.willow.data.entity.ExerciseType
import com.marki.willow.data.repository.ExerciseLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseLogViewModel @Inject constructor(
    private val repository: ExerciseLogRepository
) : ViewModel() {

    private val _exerciseLogs = MutableStateFlow<List<ExerciseLog>>(emptyList())
    val exerciseLogs: StateFlow<List<ExerciseLog>> = _exerciseLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _exerciseType = MutableStateFlow(ExerciseType.WALKING)
    val exerciseType: StateFlow<ExerciseType> = _exerciseType.asStateFlow()

    private val _startTime = MutableStateFlow<LocalDateTime?>(null)
    val startTime: StateFlow<LocalDateTime?> = _startTime.asStateFlow()

    private val _durationMinutes = MutableStateFlow(30)
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _intensity = MutableStateFlow(ExerciseIntensity.MODERATE)
    val intensity: StateFlow<ExerciseIntensity> = _intensity.asStateFlow()

    private val _caloriesBurned = MutableStateFlow<Int?>(null)
    val caloriesBurned: StateFlow<Int?> = _caloriesBurned.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _conflictMessage = MutableStateFlow<String?>(null)
    val conflictMessage: StateFlow<String?> = _conflictMessage.asStateFlow()

    init {
        loadExerciseLogs()
    }

    private fun loadExerciseLogs() {
        viewModelScope.launch {
            repository.getAllExerciseLogs().collect { logs ->
                _exerciseLogs.value = logs
            }
        }
    }

    fun setExerciseType(type: ExerciseType) {
        _exerciseType.value = type
    }

    fun setStartTime(startTime: LocalDateTime) {
        _startTime.value = startTime
    }

    fun setDurationMinutes(duration: Int) {
        _durationMinutes.value = duration
    }

    fun setIntensity(intensity: ExerciseIntensity) {
        _intensity.value = intensity
    }

    fun setCaloriesBurned(calories: Int?) {
        _caloriesBurned.value = calories
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun saveExerciseLog() {
        val startTime = _startTime.value

        if (startTime != null && _durationMinutes.value > 0) {
            viewModelScope.launch {
                _isLoading.value = true
                _conflictMessage.value = null
                try {
                    val exerciseLog = ExerciseLog(
                        id = UUID.randomUUID().toString(),
                        exerciseType = _exerciseType.value,
                        startTime = startTime,
                        durationMinutes = _durationMinutes.value,
                        intensity = _intensity.value,
                        caloriesBurned = _caloriesBurned.value,
                        notes = _notes.value.takeIf { it.isNotBlank() },
                        source = DataSource.MANUAL
                    )
                    
                    // Use conflict-aware insertion
                    val (success, conflicts) = repository.insertExerciseLogWithConflictDetection(exerciseLog)
                    
                    if (conflicts.isNotEmpty()) {
                        val conflictCount = conflicts.size
                        _conflictMessage.value = "Exercise log saved, but $conflictCount time conflict${if (conflictCount > 1) "s" else ""} detected with existing entries."
                        println("zxc ExerciseLogViewModel: Saved exercise log with $conflictCount conflicts")
                    }
                    
                    clearForm()
                } catch (e: Exception) {
                    println("zxc ExerciseLogViewModel: Error saving exercise log: ${e.message}")
                    _conflictMessage.value = "Error saving exercise log: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun clearForm() {
        _exerciseType.value = ExerciseType.WALKING
        _startTime.value = null
        _durationMinutes.value = 30
        _intensity.value = ExerciseIntensity.MODERATE
        _caloriesBurned.value = null
        _notes.value = ""
    }

    fun clearConflictMessage() {
        _conflictMessage.value = null
    }

    fun deleteExerciseLog(exerciseLog: ExerciseLog) {
        viewModelScope.launch {
            repository.deleteExerciseLog(exerciseLog)
        }
    }
}