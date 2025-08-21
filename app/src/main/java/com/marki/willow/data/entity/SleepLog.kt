package com.marki.willow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey val id: String,
    val bedTime: LocalDateTime,
    val wakeTime: LocalDateTime,
    val sleepQuality: Int, // 1-5 rating
    val notes: String? = null,
    val source: DataSource,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isConflicted: Boolean = false
)

enum class DataSource {
    MANUAL,
    GOOGLE_HEALTH,
    SAMSUNG_HEALTH,
    GARMIN
}