package com.marki.willow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marki.willow.data.entity.DataSource
import com.marki.willow.data.entity.SleepLog
import com.marki.willow.data.repository.SleepLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SleepLogViewModel @Inject constructor(
    private val repository: SleepLogRepository
) : ViewModel() {

    private val _sleepLogs = MutableStateFlow<List<SleepLog>>(emptyList())
    val sleepLogs: StateFlow<List<SleepLog>> = _sleepLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bedTime = MutableStateFlow<LocalDateTime?>(null)
    val bedTime: StateFlow<LocalDateTime?> = _bedTime.asStateFlow()

    private val _wakeTime = MutableStateFlow<LocalDateTime?>(null)
    val wakeTime: StateFlow<LocalDateTime?> = _wakeTime.asStateFlow()

    private val _sleepQuality = MutableStateFlow(3)
    val sleepQuality: StateFlow<Int> = _sleepQuality.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    init {
        loadSleepLogs()
    }

    private fun loadSleepLogs() {
        viewModelScope.launch {
            repository.getAllSleepLogs().collect { logs ->
                _sleepLogs.value = logs
            }
        }
    }

    fun setBedTime(bedTime: LocalDateTime) {
        _bedTime.value = bedTime
    }

    fun setWakeTime(wakeTime: LocalDateTime) {
        _wakeTime.value = wakeTime
    }

    fun setSleepQuality(quality: Int) {
        _sleepQuality.value = quality
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun saveSleepLog() {
        val bedTime = _bedTime.value
        val wakeTime = _wakeTime.value

        if (bedTime != null && wakeTime != null && wakeTime.isAfter(bedTime)) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val sleepLog = SleepLog(
                        id = UUID.randomUUID().toString(),
                        bedTime = bedTime,
                        wakeTime = wakeTime,
                        sleepQuality = _sleepQuality.value,
                        notes = _notes.value.takeIf { it.isNotBlank() },
                        source = DataSource.MANUAL
                    )
                    repository.insertSleepLog(sleepLog)
                    clearForm()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun clearForm() {
        _bedTime.value = null
        _wakeTime.value = null
        _sleepQuality.value = 3
        _notes.value = ""
    }

    fun deleteSleepLog(sleepLog: SleepLog) {
        viewModelScope.launch {
            repository.deleteSleepLog(sleepLog)
        }
    }
}