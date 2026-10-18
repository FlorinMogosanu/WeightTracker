package com.weighttracker.android.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weighttracker.android.core.di.AppContainer
import com.weighttracker.android.domain.model.WeightUnit
import com.weighttracker.android.domain.usecase.PruneResult
import com.weighttracker.android.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val container: AppContainer,
    private val appContext: Context
) : ViewModel() {

    private val _isPruning = MutableStateFlow(false)
    private val _lastPruneResult = MutableStateFlow<PruneResult?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        container.userPreferencesRepository.userSettings,
        _isPruning,
        _lastPruneResult,
        _infoMessage
    ) { settings, isPruning, lastPruneResult, infoMessage ->
        SettingsUiState(
            settings = settings,
            isPruning = isPruning,
            lastPruneResult = lastPruneResult,
            infoMessage = infoMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            container.userPreferencesRepository.setWeightUnit(unit)
        }
    }

    fun setAutoPruneEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.userPreferencesRepository.setAutoPruneEnabled(enabled)
            _infoMessage.value = if (enabled) {
                "Auto-archiving enabled. Old raw records will be safely summarized."
            } else {
                "Auto-archiving disabled."
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.userPreferencesRepository.setReminderEnabled(enabled)
            val settings = uiState.value.settings
            if (enabled) {
                WorkScheduler.scheduleDailyReminder(appContext, settings.reminderHour, settings.reminderMinute)
                _infoMessage.value = "Daily reminder active."
            } else {
                WorkScheduler.cancelDailyReminder(appContext)
                _infoMessage.value = "Daily reminder turned off."
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            container.userPreferencesRepository.setReminderTime(hour, minute)
            if (uiState.value.settings.reminderEnabled) {
                WorkScheduler.scheduleDailyReminder(appContext, hour, minute)
            }
            _infoMessage.value = String.format("Reminder scheduled for %02d:%02d", hour, minute)
        }
    }

    fun executeManualPrune() {
        viewModelScope.launch {
            _isPruning.value = true
            try {
                val result = container.pruneOldEntriesUseCase()
                _lastPruneResult.value = result
            } catch (e: Exception) {
                _infoMessage.value = "Error during archive: ${e.message}"
            } finally {
                _isPruning.value = false
            }
        }
    }

    fun dismissPruneDialog() {
        _lastPruneResult.value = null
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    class Factory(
        private val container: AppContainer,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(container, appContext) as T
        }
    }
}
