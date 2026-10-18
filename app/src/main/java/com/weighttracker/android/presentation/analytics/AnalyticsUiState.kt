package com.weighttracker.android.presentation.analytics

import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.WeeklySummary
import com.weighttracker.android.domain.model.WeightUnit

data class AnalyticsUiState(
    val activeProfile: UserProfile = UserProfile.ME,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isCompareMode: Boolean = false,
    val primaryWeeklySummaries: List<WeeklySummary> = emptyList(),
    val secondaryWeeklySummaries: List<WeeklySummary> = emptyList(),
    val currentWeekAverage: Double? = null,
    val allTimeLowestAverage: Double? = null,
    val allTimeHighestAverage: Double? = null,
    val netChangeKg: Double? = null,
    val isLoading: Boolean = false
)
