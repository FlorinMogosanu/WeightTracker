package com.weighttracker.android.data.repository

import com.weighttracker.android.core.datastore.UserPreferencesDataSource
import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.UserSettings
import com.weighttracker.android.domain.model.WeightUnit
import com.weighttracker.android.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override val userSettings: Flow<UserSettings> = dataSource.userSettings

    override suspend fun setActiveProfile(profile: UserProfile) {
        dataSource.setActiveProfile(profile)
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        dataSource.setWeightUnit(unit)
    }

    override suspend fun setAutoPruneEnabled(enabled: Boolean) {
        dataSource.setAutoPruneEnabled(enabled)
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        dataSource.setReminderEnabled(enabled)
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        dataSource.setReminderTime(hour, minute)
    }
}
