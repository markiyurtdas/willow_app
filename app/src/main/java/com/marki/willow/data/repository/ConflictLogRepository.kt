package com.marki.willow.data.repository

import com.marki.willow.data.dao.ConflictLogDao
import com.marki.willow.data.entity.ConflictLog
import com.marki.willow.data.entity.ConflictResolution
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictLogRepository @Inject constructor(
    private val conflictLogDao: ConflictLogDao
) {
    
    fun getAllConflictLogs(): Flow<List<ConflictLog>> = conflictLogDao.getAllConflictLogs()
    
    fun getUnresolvedConflicts(): Flow<List<ConflictLog>> = conflictLogDao.getUnresolvedConflicts()
    
    suspend fun getConflictLogById(id: String): ConflictLog? = conflictLogDao.getConflictLogById(id)
    
    suspend fun insertConflictLog(conflictLog: ConflictLog) = conflictLogDao.insertConflictLog(conflictLog)
    
    suspend fun updateConflictLog(conflictLog: ConflictLog) = conflictLogDao.updateConflictLog(conflictLog)
    
    suspend fun deleteConflictLog(conflictLog: ConflictLog) = conflictLogDao.deleteConflictLog(conflictLog)
    
    suspend fun resolveConflict(id: String, resolution: ConflictResolution, resolvedAt: LocalDateTime) {
        conflictLogDao.resolveConflict(id, resolution.name, resolvedAt.toString())
    }
}