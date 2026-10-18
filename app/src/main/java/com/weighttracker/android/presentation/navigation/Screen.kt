package com.weighttracker.android.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Log", Icons.Default.FitnessCenter)
    data object Analytics : Screen("analytics", "Trends", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        fun items(): List<Screen> = listOf(Dashboard, Analytics, Settings)
    }
}
