package com.weighttracker.android.domain.model

data class UserSettings(
    val activeProfile: UserProfile = UserProfile.ME,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val autoPruneEnabled: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)
