package com.weighttracker.android.presentation.settings

import com.weighttracker.android.domain.model.UserSettings
import com.weighttracker.android.domain.usecase.PruneResult

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isPruning: Boolean = false,
    val lastPruneResult: PruneResult? = null,
    val infoMessage: String? = null
)
