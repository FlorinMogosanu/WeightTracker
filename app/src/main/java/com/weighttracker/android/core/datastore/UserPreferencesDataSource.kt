package com.weighttracker.android.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.UserSettings
import com.weighttracker.android.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val AUTO_PRUNE_ENABLED = booleanPreferencesKey("auto_prune_enabled")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val profileId = preferences[PreferencesKeys.ACTIVE_PROFILE] ?: UserProfile.ME.id
            val unitId = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.id
            val autoPrune = preferences[PreferencesKeys.AUTO_PRUNE_ENABLED] ?: false
            val reminder = preferences[PreferencesKeys.REMINDER_ENABLED] ?: true
            val hour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 8
            val minute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0

            UserSettings(
                activeProfile = UserProfile.fromId(profileId),
                weightUnit = WeightUnit.fromId(unitId),
                autoPruneEnabled = autoPrune,
                reminderEnabled = reminder,
                reminderHour = hour,
                reminderMinute = minute
            )
        }

    suspend fun setActiveProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROFILE] = profile.id
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.id
        }
    }

    suspend fun setAutoPruneEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_PRUNE_ENABLED] = enabled
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }
}
