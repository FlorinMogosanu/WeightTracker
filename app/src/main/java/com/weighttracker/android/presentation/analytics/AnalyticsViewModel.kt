package com.weighttracker.android.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weighttracker.android.core.di.AppContainer
import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.WeeklySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _isCompareMode = MutableStateFlow(false)
    private val userSettingsFlow = container.userPreferencesRepository.userSettings

    @OptIn(ExperimentalCoroutinesApi::class)
    private val primaryHistoryFlow = userSettingsFlow.flatMapLatest { settings ->
        container.getWeightHistoryUseCase(settings.activeProfile.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val secondaryHistoryFlow = userSettingsFlow.flatMapLatest { settings ->
        val oppositeProfileId = if (settings.activeProfile == UserProfile.ME) {
            UserProfile.PARTNER.id
        } else {
            UserProfile.ME.id
        }
        container.getWeightHistoryUseCase(oppositeProfileId)
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        userSettingsFlow,
        _isCompareMode,
        primaryHistoryFlow,
        secondaryHistoryFlow
    ) { settings, isCompareMode, primaryHistory, secondaryHistory ->
        val sortedPrimary = primaryHistory.sortedWith(compareBy({ it.year }, { it.weekNumber }))
        val sortedSecondary = secondaryHistory.sortedWith(compareBy({ it.year }, { it.weekNumber }))

        val lowest = sortedPrimary.minOfOrNull { it.averageWeightKg }
        val highest = sortedPrimary.maxOfOrNull { it.averageWeightKg }
        val current = sortedPrimary.lastOrNull()?.averageWeightKg
        val netChange = if (sortedPrimary.size >= 2) {
            sortedPrimary.last().averageWeightKg - sortedPrimary.first().averageWeightKg
        } else null

        AnalyticsUiState(
            activeProfile = settings.activeProfile,
            weightUnit = settings.weightUnit,
            isCompareMode = isCompareMode,
            primaryWeeklySummaries = sortedPrimary,
            secondaryWeeklySummaries = sortedSecondary,
            currentWeekAverage = current,
            allTimeLowestAverage = lowest,
            allTimeHighestAverage = highest,
            netChangeKg = netChange
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun toggleCompareMode() {
        _isCompareMode.value = !_isCompareMode.value
    }

    fun switchProfile(profile: UserProfile) {
        viewModelScope.launch {
            container.userPreferencesRepository.setActiveProfile(profile)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(container) as T
        }
    }
}
