package com.marki.willow.data.dao

import androidx.room.*
import com.marki.willow.data.entity.ExerciseLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExerciseLogDao {
    
    @Query("SELECT * FROM exercise_logs ORDER BY startTime DESC")
    fun getAllExerciseLogs(): Flow<List<ExerciseLog>>
    
    @Query("SELECT * FROM exercise_logs WHERE id = :id")
    suspend fun getExerciseLogById(id: String): ExerciseLog?
    
    @Query("SELECT * FROM exercise_logs WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    suspend fun getExerciseLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ExerciseLog>
    
    @Query("SELECT * FROM exercise_logs WHERE isConflicted = 1")
    fun getConflictedExerciseLogs(): Flow<List<ExerciseLog>>
    
    @Query("SELECT * FROM exercise_logs WHERE exerciseType = :type ORDER BY startTime DESC")
    suspend fun getExerciseLogsByType(type: String): List<ExerciseLog>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLogs(exerciseLogs: List<ExerciseLog>)
    
    @Update
    suspend fun updateExerciseLog(exerciseLog: ExerciseLog)
    
    @Delete
    suspend fun deleteExerciseLog(exerciseLog: ExerciseLog)
    
    @Query("DELETE FROM exercise_logs WHERE id = :id")
    suspend fun deleteExerciseLogById(id: String)
}