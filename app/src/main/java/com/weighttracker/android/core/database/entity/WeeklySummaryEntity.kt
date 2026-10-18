package com.weighttracker.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_summaries",
    indices = [
        Index(value = ["userId", "year", "weekNumber"], unique = true),
        Index(value = ["userId"])
    ]
)
data class WeeklySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val year: Int,
    val weekNumber: Int,
    val averageWeightKg: Double,
    val startDate: Long, // LocalDate.toEpochDay()
    val endDate: Long,   // LocalDate.toEpochDay()
    val entryCount: Int
)
