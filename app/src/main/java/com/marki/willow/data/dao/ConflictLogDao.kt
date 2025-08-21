package com.marki.willow.data.dao

import androidx.room.*
import com.marki.willow.data.entity.ConflictLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictLogDao {
    
    @Query("SELECT * FROM conflict_logs ORDER BY createdAt DESC")
    fun getAllConflictLogs(): Flow<List<ConflictLog>>
    
    @Query("SELECT * FROM conflict_logs WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedConflicts(): Flow<List<ConflictLog>>
    
    @Query("SELECT * FROM conflict_logs WHERE id = :id")
    suspend fun getConflictLogById(id: String): ConflictLog?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflictLog(conflictLog: ConflictLog)
    
    @Update
    suspend fun updateConflictLog(conflictLog: ConflictLog)
    
    @Delete
    suspend fun deleteConflictLog(conflictLog: ConflictLog)
    
    @Query("UPDATE conflict_logs SET isResolved = 1, resolution = :resolution, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolveConflict(id: String, resolution: String, resolvedAt: String)
}