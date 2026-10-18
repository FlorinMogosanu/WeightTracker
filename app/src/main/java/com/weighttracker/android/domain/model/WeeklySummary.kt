package com.weighttracker.android.domain.model

import java.time.LocalDate

data class WeeklySummary(
    val id: Long = 0,
    val userId: String,
    val year: Int,
    val weekNumber: Int,
    val averageWeightKg: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val entryCount: Int
)
