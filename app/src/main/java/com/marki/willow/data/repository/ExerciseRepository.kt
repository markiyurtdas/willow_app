package com.marki.willow.data.repository

import com.marki.willow.data.dao.ExerciseLogDao
import com.marki.willow.data.entity.ExerciseLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
class ExerciseRepository(
    private val exerciseLogDao: ExerciseLogDao
) {
    
    fun getAllExerciseLogs(): Flow<List<ExerciseLog>> = exerciseLogDao.getAllExerciseLogs()
    
    suspend fun getExerciseLogById(id: String): ExerciseLog? = exerciseLogDao.getExerciseLogById(id)
    
    suspend fun getExerciseLogsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ExerciseLog> =
        exerciseLogDao.getExerciseLogsByDateRange(startDate, endDate)
    
    fun getConflictedExerciseLogs(): Flow<List<ExerciseLog>> = exerciseLogDao.getConflictedExerciseLogs()
    
    suspend fun getExerciseLogsByType(type: String): List<ExerciseLog> = 
        exerciseLogDao.getExerciseLogsByType(type)
    
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.insertExerciseLog(exerciseLog)
    
    suspend fun insertExerciseLogs(exerciseLogs: List<ExerciseLog>) = exerciseLogDao.insertExerciseLogs(exerciseLogs)
    
    suspend fun updateExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.updateExerciseLog(exerciseLog)
    
    suspend fun deleteExerciseLog(exerciseLog: ExerciseLog) = exerciseLogDao.deleteExerciseLog(exerciseLog)
    
    suspend fun deleteExerciseLogById(id: String) = exerciseLogDao.deleteExerciseLogById(id)
}