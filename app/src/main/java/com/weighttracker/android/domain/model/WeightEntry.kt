package com.weighttracker.android.domain.model

import java.time.LocalDate

data class WeightEntry(
    val id: Long = 0,
    val userId: String,
    val date: LocalDate,
    val weightKg: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
