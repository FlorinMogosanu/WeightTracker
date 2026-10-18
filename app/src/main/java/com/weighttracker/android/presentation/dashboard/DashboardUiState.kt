package com.weighttracker.android.presentation.dashboard

import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeightUnit
import java.time.LocalDate

data class DashboardUiState(
    val activeProfile: UserProfile = UserProfile.ME,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val selectedDate: LocalDate = LocalDate.now(),
    val weightInput: String = "",
    val noteInput: String = "",
    val isEditing: Boolean = false,
    val editingEntryId: Long? = null,
    val isAutoFilled: Boolean = false,
    val recentEntries: List<WeightEntry> = emptyList(),
    val todayEntry: WeightEntry? = null,
    val currentWeekAverageKg: Double? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

