package com.weighttracker.android.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.model.WeeklySummary
import com.weighttracker.android.presentation.components.CustomLineChart
import com.weighttracker.android.presentation.components.ProfileSwitcher
import com.weighttracker.android.presentation.theme.LossRed
import com.weighttracker.android.presentation.theme.ProfileMeColor
import com.weighttracker.android.presentation.theme.ProfilePartnerColor
import com.weighttracker.android.presentation.theme.SuccessGreen
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onProfileSelected: (UserProfile) -> Unit,
    onToggleCompareMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (uiState.activeProfile == UserProfile.ME) ProfileMeColor else ProfilePartnerColor
    val oppositeColor = if (uiState.activeProfile == UserProfile.ME) ProfilePartnerColor else ProfileMeColor
    val oppositeName = if (uiState.activeProfile == UserProfile.ME) UserProfile.PARTNER.displayName else UserProfile.ME.displayName

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Switcher
        item {
            ProfileSwitcher(
                activeProfile = uiState.activeProfile,
                onProfileSelected = onProfileSelected
            )
        }

        // Compare Mode Toggle Chip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Progression Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = uiState.isCompareMode,
                    onClick = onToggleCompareMode,
                    label = { Text("Compare Both") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Summary Metric Cards (Cards in 2x2 grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Latest Week Avg",
                        value = uiState.currentWeekAverage?.let { uiState.weightUnit.format(it) } ?: "--",
                        accentColor = activeColor,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "All-Time Low",
                        value = uiState.allTimeLowestAverage?.let { uiState.weightUnit.format(it) } ?: "--",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "All-Time High",
                        value = uiState.allTimeHighestAverage?.let { uiState.weightUnit.format(it) } ?: "--",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Net Change",
                        value = uiState.netChangeKg?.let { delta ->
                            val isLoss = delta < 0
                            val display = uiState.weightUnit.toDisplay(kotlin.math.abs(delta))
                            String.format(Locale.US, "%s%.1f %s", if (isLoss) "-" else "+", display, uiState.weightUnit.symbol)
                        } ?: "--",
                        accentColor = if ((uiState.netChangeKg ?: 0.0) <= 0) SuccessGreen else LossRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Line Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CustomLineChart(
                        primarySummaries = uiState.primaryWeeklySummaries,
                        primaryColor = activeColor,
                        primaryLabel = uiState.activeProfile.displayName,
                        weightUnit = uiState.weightUnit,
                        secondarySummaries = if (uiState.isCompareMode) uiState.secondaryWeeklySummaries else null,
                        secondaryColor = oppositeColor,
                        secondaryLabel = if (uiState.isCompareMode) oppositeName else null
                    )
                }
            }
        }

        // Historical Weekly Breakdown Header
        item {
            Text(
                text = "Historical Weekly Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Weekly Summaries List
        if (uiState.primaryWeeklySummaries.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "No weekly summaries available yet. Log daily measurements on the Dashboard to build trends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(
                items = uiState.primaryWeeklySummaries.reversed(),
                key = { "${it.userId}_${it.year}_${it.weekNumber}" }
            ) { summary ->
                WeeklySummaryCard(
                    summary = summary,
                    weightUnit = uiState.weightUnit,
                    accentColor = activeColor
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    summary: WeeklySummary,
    weightUnit: com.weighttracker.android.domain.model.WeightUnit,
    accentColor: Color
) {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Week ${summary.weekNumber}, ${summary.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${summary.startDate.format(formatter)} - ${summary.endDate.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = weightUnit.format(summary.averageWeightKg),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${summary.entryCount} days logged",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
