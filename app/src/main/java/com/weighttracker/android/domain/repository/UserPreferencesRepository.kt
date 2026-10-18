package com.weighttracker.android.domain.repository

import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.UserSettings
import com.weighttracker.android.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userSettings: Flow<UserSettings>
    suspend fun setActiveProfile(profile: UserProfile)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setAutoPruneEnabled(enabled: Boolean)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
}
