package com.weighttracker.android.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weighttracker.android.core.di.AppContainer
import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeightUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class DashboardViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _weightInput = MutableStateFlow("")
    private val _noteInput = MutableStateFlow("")
    private val _isEditing = MutableStateFlow(false)
    private val _editingEntryId = MutableStateFlow<Long?>(null)
    private val _isAutoFilled = MutableStateFlow(false)
    private val _hasUserManuallyEdited = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    // Flow of settings (active profile, weight unit)
    private val userSettingsFlow = container.userPreferencesRepository.userSettings

    // Dynamic stream of entries based on active profile
    @OptIn(ExperimentalCoroutinesApi::class)
    private val entriesForActiveUserFlow = userSettingsFlow
        .flatMapLatest { settings ->
            container.weightRepository.getEntriesForUser(settings.activeProfile.id)
        }

    init {
        // Automatically prefill weight with current week average upon data/profile load
        viewModelScope.launch {
            combine(userSettingsFlow, entriesForActiveUserFlow) { settings, entries ->
                Pair(settings, entries)
            }.collect { (settings, entries) ->
                if (!_isEditing.value && !_hasUserManuallyEdited.value) {
                    prefillWeeklyAverage(settings.weightUnit, entries)
                }
            }
        }
    }

    private fun prefillWeeklyAverage(weightUnit: WeightUnit, entries: List<WeightEntry>) {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekEntries = entries.filter { !it.date.isBefore(monday) && !it.date.isAfter(monday.plusDays(6)) }
        val currentWeekAvg = if (currentWeekEntries.isNotEmpty()) {
            currentWeekEntries.map { it.weightKg }.average()
        } else null

        // If no entries for this week yet, fall back to most recent entry or 70.0 kg
        val defaultKg = currentWeekAvg ?: entries.firstOrNull()?.weightKg ?: 70.0
        val displayWeight = weightUnit.toDisplay(defaultKg)
        _weightInput.value = String.format(Locale.US, "%.1f", displayWeight)
        _isAutoFilled.value = true
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        userSettingsFlow,
        entriesForActiveUserFlow,
        _selectedDate,
        _weightInput,
        _noteInput,
        _isEditing,
        _editingEntryId,
        _isAutoFilled,
        _userMessage
    ) { params ->
        val settings = params[0] as com.weighttracker.android.domain.model.UserSettings
        @Suppress("UNCHECKED_CAST")
        val entries = params[1] as List<WeightEntry>
        val selectedDate = params[2] as LocalDate
        val weightInput = params[3] as String
        val noteInput = params[4] as String
        val isEditing = params[5] as Boolean
        val editingEntryId = params[6] as Long?
        val isAutoFilled = params[7] as Boolean
        val userMessage = params[8] as String?

        val today = LocalDate.now()
        val todayEntry = entries.firstOrNull { it.date == today }

        // Compute current week's rolling average
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekEntries = entries.filter { !it.date.isBefore(monday) && !it.date.isAfter(monday.plusDays(6)) }
        val currentWeekAvg = if (currentWeekEntries.isNotEmpty()) {
            currentWeekEntries.map { it.weightKg }.average()
        } else null

        DashboardUiState(
            activeProfile = settings.activeProfile,
            weightUnit = settings.weightUnit,
            selectedDate = selectedDate,
            weightInput = weightInput,
            noteInput = noteInput,
            isEditing = isEditing,
            editingEntryId = editingEntryId,
            isAutoFilled = isAutoFilled,
            recentEntries = entries.take(15),
            todayEntry = todayEntry,
            currentWeekAverageKg = currentWeekAvg,
            userMessage = userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun switchProfile(profile: UserProfile) {
        viewModelScope.launch {
            _hasUserManuallyEdited.value = false
            _isEditing.value = false
            _editingEntryId.value = null
            _noteInput.value = ""
            _selectedDate.value = LocalDate.now()
            container.userPreferencesRepository.setActiveProfile(profile)
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        viewModelScope.launch {
            val currentProfile = uiState.value.activeProfile
            val existing = container.weightRepository.getEntryForUserAndDate(currentProfile.id, date)
            if (existing != null) {
                val displayWeight = uiState.value.weightUnit.toDisplay(existing.weightKg)
                _weightInput.value = String.format(Locale.US, "%.1f", displayWeight)
                _noteInput.value = existing.note ?: ""
                _isEditing.value = true
                _editingEntryId.value = existing.id
                _isAutoFilled.value = false
                _hasUserManuallyEdited.value = true
            } else {
                _noteInput.value = ""
                _isEditing.value = false
                _editingEntryId.value = null
                _hasUserManuallyEdited.value = false
                val entries = container.weightRepository.getEntriesForUser(currentProfile.id)
                    .stateIn(viewModelScope).value
                prefillWeeklyAverage(uiState.value.weightUnit, entries)
            }
        }
    }

    fun onWeightInputFocused() {
        if (_isAutoFilled.value) {
            _weightInput.value = ""
            _isAutoFilled.value = false
            _hasUserManuallyEdited.value = true
        }
    }

    fun onWeightInputChanged(input: String) {
        _isAutoFilled.value = false
        _hasUserManuallyEdited.value = true
        // Accept only digits and at most one decimal point
        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
            _weightInput.value = input
        }
    }

    fun onNoteInputChanged(note: String) {
        _noteInput.value = note
    }

    fun adjustWeight(delta: Double) {
        val current = _weightInput.value.toDoubleOrNull() ?: 70.0
        val updated = (current + delta).coerceAtLeast(1.0)
        _weightInput.value = String.format(Locale.US, "%.1f", updated)
    }

    fun saveEntry() {
        val weightVal = _weightInput.value.toDoubleOrNull()
        if (weightVal == null || weightVal <= 0) {
            _userMessage.value = "Please enter a valid weight."
            return
        }

        viewModelScope.launch {
            val currentState = uiState.value
            val weightKg = currentState.weightUnit.toKg(weightVal)

            val entry = WeightEntry(
                id = currentState.editingEntryId ?: 0,
                userId = currentState.activeProfile.id,
                date = currentState.selectedDate,
                weightKg = weightKg,
                note = _noteInput.value.trim().ifEmpty { null }
            )

            val result = container.saveWeightEntryUseCase(entry)
            result.onSuccess {
                _userMessage.value = if (currentState.isEditing) "Entry updated successfully" else "Entry saved successfully"
                resetInputs()
            }.onFailure { error ->
                _userMessage.value = error.message ?: "Failed to save entry"
            }
        }
    }

    fun loadForEdit(entry: WeightEntry) {
        _selectedDate.value = entry.date
        val displayWeight = uiState.value.weightUnit.toDisplay(entry.weightKg)
        _weightInput.value = String.format(Locale.US, "%.1f", displayWeight)
        _noteInput.value = entry.note ?: ""
        _isEditing.value = true
        _editingEntryId.value = entry.id
        _isAutoFilled.value = false
        _hasUserManuallyEdited.value = true
    }

    fun cancelEditing() {
        resetInputs()
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            container.deleteWeightEntryUseCase(id)
            _userMessage.value = "Entry deleted"
            if (_editingEntryId.value == id) {
                resetInputs()
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun resetInputs() {
        _selectedDate.value = LocalDate.now()
        _noteInput.value = ""
        _isEditing.value = false
        _editingEntryId.value = null
        _hasUserManuallyEdited.value = false
        viewModelScope.launch {
            val currentProfile = uiState.value.activeProfile
            val entries = container.weightRepository.getEntriesForUser(currentProfile.id)
                .stateIn(viewModelScope).value
            prefillWeeklyAverage(uiState.value.weightUnit, entries)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(container) as T
        }
    }
}

