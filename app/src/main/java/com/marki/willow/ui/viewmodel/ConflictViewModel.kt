package com.marki.willow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marki.willow.data.entity.ConflictLog
import com.marki.willow.data.entity.ConflictResolution
import com.marki.willow.data.repository.ConflictLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ConflictViewModel @Inject constructor(
    private val repository: ConflictLogRepository
) : ViewModel() {

    private val _unresolvedConflicts = MutableStateFlow<List<ConflictLog>>(emptyList())
    val unresolvedConflicts: StateFlow<List<ConflictLog>> = _unresolvedConflicts.asStateFlow()

    private val _allConflicts = MutableStateFlow<List<ConflictLog>>(emptyList())
    val allConflicts: StateFlow<List<ConflictLog>> = _allConflicts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadConflicts()
    }

    private fun loadConflicts() {
        viewModelScope.launch {
            repository.getUnresolvedConflicts().collect { conflicts ->
                _unresolvedConflicts.value = conflicts
            }
        }
        
        viewModelScope.launch {
            repository.getAllConflictLogs().collect { conflicts ->
                _allConflicts.value = conflicts
            }
        }
    }

    fun resolveConflict(conflictId: String, resolution: ConflictResolution) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.resolveConflict(conflictId, resolution, LocalDateTime.now())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteConflict(conflict: ConflictLog) {
        viewModelScope.launch {
            repository.deleteConflictLog(conflict)
        }
    }
}