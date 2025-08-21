package com.marki.willow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "conflict_logs")
data class ConflictLog(
    @PrimaryKey val id: String,
    val conflictType: ConflictType,
    val primaryDataId: String, // ID of the main conflicting data
    val conflictingDataId: String, // ID of the conflicting data
    val conflictDetails: String, // JSON string describing the conflict
    val isResolved: Boolean = false,
    val resolution: ConflictResolution? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val resolvedAt: LocalDateTime? = null
)

enum class ConflictType {
    SLEEP_TIME_OVERLAP,
    EXERCISE_TIME_OVERLAP,
    DUPLICATE_ENTRY,
    DATA_MISMATCH
}

enum class ConflictResolution {
    KEEP_MANUAL,
    KEEP_GOOGLE_HEALTH,
    KEEP_SAMSUNG_HEALTH,
    KEEP_GARMIN,
    MERGE_DATA,
    DELETE_DUPLICATE
}