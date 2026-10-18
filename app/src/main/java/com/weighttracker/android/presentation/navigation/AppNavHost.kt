package com.weighttracker.android.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weighttracker.android.core.di.AppContainer
import com.weighttracker.android.presentation.analytics.AnalyticsScreen
import com.weighttracker.android.presentation.analytics.AnalyticsViewModel
import com.weighttracker.android.presentation.dashboard.DashboardScreen
import com.weighttracker.android.presentation.dashboard.DashboardViewModel
import com.weighttracker.android.presentation.settings.SettingsScreen
import com.weighttracker.android.presentation.settings.SettingsViewModel

@Composable
fun AppNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Screen.items().forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                DashboardScreen(
                    uiState = uiState,
                    onProfileSelected = viewModel::switchProfile,
                    onDateSelected = viewModel::onDateSelected,
                    onWeightInputChanged = viewModel::onWeightInputChanged,
                    onWeightInputFocused = viewModel::onWeightInputFocused,
                    onNoteInputChanged = viewModel::onNoteInputChanged,
                    onAdjustWeight = viewModel::adjustWeight,
                    onSaveEntry = viewModel::saveEntry,
                    onLoadForEdit = viewModel::loadForEdit,
                    onCancelEditing = viewModel::cancelEditing,
                    onDeleteEntry = viewModel::deleteEntry,
                    onClearUserMessage = viewModel::clearUserMessage
                )

            }

            composable(Screen.Analytics.route) {
                val viewModel: AnalyticsViewModel = viewModel(
                    factory = AnalyticsViewModel.Factory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                AnalyticsScreen(
                    uiState = uiState,
                    onProfileSelected = viewModel::switchProfile,
                    onToggleCompareMode = viewModel::toggleCompareMode
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(container, context.applicationContext)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SettingsScreen(
                    uiState = uiState,
                    onSetWeightUnit = viewModel::setWeightUnit,
                    onSetAutoPruneEnabled = viewModel::setAutoPruneEnabled,
                    onSetReminderEnabled = viewModel::setReminderEnabled,
                    onSetReminderTime = viewModel::setReminderTime,
                    onExecuteManualPrune = viewModel::executeManualPrune,
                    onDismissPruneDialog = viewModel::dismissPruneDialog,
                    onClearInfoMessage = viewModel::clearInfoMessage
                )
            }
        }
    }
}
