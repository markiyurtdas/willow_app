package com.marki.willow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marki.willow.data.repository.ConflictLogRepository
import com.marki.willow.data.repository.ExerciseLogRepository
import com.marki.willow.data.repository.SleepLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sleepLogRepository: SleepLogRepository,
    private val exerciseLogRepository: ExerciseLogRepository,
    private val conflictLogRepository: ConflictLogRepository
) : ViewModel() {

    private val _summaryData = MutableStateFlow(HealthSummary())
    val summaryData: StateFlow<HealthSummary> = _summaryData.asStateFlow()

    init {
        loadSummaryData()
    }

    private fun loadSummaryData() {
        viewModelScope.launch {
            combine(
                sleepLogRepository.getAllSleepLogs(),
                exerciseLogRepository.getAllExerciseLogs(),
                conflictLogRepository.getUnresolvedConflicts()
            ) { sleepLogs, exerciseLogs, conflicts ->
                val sevenDaysAgo = LocalDateTime.now().minusDays(7)
                
                val recentSleepLogs = sleepLogs.filter { it.bedTime.isAfter(sevenDaysAgo) }
                val recentExerciseLogs = exerciseLogs.filter { it.startTime.isAfter(sevenDaysAgo) }
                
                val avgSleepQuality = if (recentSleepLogs.isNotEmpty()) {
                    recentSleepLogs.map { it.sleepQuality }.average()
                } else 0.0
                
                val totalExerciseMinutes = recentExerciseLogs.sumOf { it.durationMinutes }
                val totalCaloriesBurned = recentExerciseLogs.sumOfNullable { it.caloriesBurned }
                
                HealthSummary(
                    totalSleepLogs = sleepLogs.size,
                    totalExerciseLogs = exerciseLogs.size,
                    weeklyAverageSleepQuality = avgSleepQuality,
                    weeklyExerciseMinutes = totalExerciseMinutes,
                    weeklyCaloriesBurned = totalCaloriesBurned,
                    unresolvedConflicts = conflicts.size
                )
            }.collect { summary ->
                _summaryData.value = summary
            }
        }
    }
}

data class HealthSummary(
    val totalSleepLogs: Int = 0,
    val totalExerciseLogs: Int = 0,
    val weeklyAverageSleepQuality: Double = 0.0,
    val weeklyExerciseMinutes: Int = 0,
    val weeklyCaloriesBurned: Int = 0,
    val unresolvedConflicts: Int = 0
)

private fun <T> Iterable<T>.sumOfNullable(selector: (T) -> Int?): Int {
    return this.mapNotNull(selector).sum()
}