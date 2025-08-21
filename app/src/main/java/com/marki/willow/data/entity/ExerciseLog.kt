package com.marki.willow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey val id: String,
    val exerciseType: ExerciseType,
    val startTime: LocalDateTime,
    val durationMinutes: Int,
    val intensity: ExerciseIntensity,
    val caloriesBurned: Int? = null,
    val notes: String? = null,
    val source: DataSource,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isConflicted: Boolean = false
)

enum class ExerciseType {
    WALKING,
    RUNNING,
    CYCLING,
    SWIMMING,
    WEIGHT_TRAINING,
    YOGA,
    BASKETBALL,
    FOOTBALL,
    OTHER
}

enum class ExerciseIntensity {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}