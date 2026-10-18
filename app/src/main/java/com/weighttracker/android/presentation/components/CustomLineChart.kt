package com.weighttracker.android.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weighttracker.android.domain.model.WeeklySummary
import com.weighttracker.android.domain.model.WeightUnit
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CustomLineChart(
    primarySummaries: List<WeeklySummary>,
    primaryColor: Color,
    primaryLabel: String,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier,
    secondarySummaries: List<WeeklySummary>? = null,
    secondaryColor: Color = Color(0xFFE63946),
    secondaryLabel: String? = null
) {
    if (primarySummaries.isEmpty() && secondarySummaries.isNullOrEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val allPrimaryValues = primarySummaries.map { weightUnit.toDisplay(it.averageWeightKg) }
    val allSecondaryValues = secondarySummaries?.map { weightUnit.toDisplay(it.averageWeightKg) } ?: emptyList()
    val combinedValues = allPrimaryValues + allSecondaryValues

    val rawMin = combinedValues.minOrNull() ?: 0.0
    val rawMax = combinedValues.maxOrNull() ?: 100.0
    val paddingValue = max((rawMax - rawMin) * 0.15, 1.0)
    val minY = max(0.0, rawMin - paddingValue)
    val maxY = rawMax + paddingValue

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.US) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Chart Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = primaryColor, label = primaryLabel)
            if (secondarySummaries != null && secondaryLabel != null) {
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = secondaryColor, label = secondaryLabel)
            }
        }

        // Scrub Tooltip Callout
        AnimatedVisibility(
            visible = selectedIndex != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val idx = selectedIndex
            if (idx != null && idx in primarySummaries.indices) {
                val item = primarySummaries[idx]
                val secItem = secondarySummaries?.getOrNull(idx)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Week ${item.weekNumber} (${item.startDate.format(dateFormatter)} - ${item.endDate.format(dateFormatter)})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor)
                                )
                                Text(
                                    text = " $primaryLabel: ${String.format(Locale.US, "%.1f %s", weightUnit.toDisplay(item.averageWeightKg), weightUnit.symbol)} (${item.entryCount} logs)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (secItem != null && secondaryLabel != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(secondaryColor)
                                    )
                                    Text(
                                        text = " $secondaryLabel: ${String.format(Locale.US, "%.1f %s", weightUnit.toDisplay(secItem.averageWeightKg), weightUnit.symbol)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Canvas Line Chart
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(primarySummaries) {
                        detectTapGestures(
                            onPress = { offset ->
                                val count = primarySummaries.size
                                if (count > 0) {
                                    val pointSpacing = size.width / max(1, count - 1)
                                    val touchedIndex = (offset.x / pointSpacing)
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    selectedIndex = touchedIndex
                                }
                            }
                        )
                    }
                    .pointerInput(primarySummaries) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                val count = primarySummaries.size
                                if (count > 0) {
                                    val pointSpacing = size.width / max(1, count - 1)
                                    val draggedIndex = (change.position.x / pointSpacing)
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    selectedIndex = draggedIndex
                                }
                            },
                            onDragEnd = {
                                // keep or dismiss on release after delay
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw Horizontal Reference Lines
                val gridLineCount = 4
                for (i in 0..gridLineCount) {
                    val y = canvasHeight * (i.toFloat() / gridLineCount)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Function to map data to coordinate points
                fun getPoints(data: List<WeeklySummary>): List<Offset> {
                    if (data.isEmpty()) return emptyList()
                    val pointSpacing = if (data.size > 1) canvasWidth / (data.size - 1) else canvasWidth / 2f
                    return data.mapIndexed { index, item ->
                        val displayVal = weightUnit.toDisplay(item.averageWeightKg)
                        val ratio = if (maxY > minY) ((displayVal - minY) / (maxY - minY)).toFloat() else 0.5f
                        val x = if (data.size == 1) canvasWidth / 2f else index * pointSpacing
                        val y = canvasHeight - (ratio * (canvasHeight - 40.dp.toPx())) - 20.dp.toPx()
                        Offset(x, y)
                    }
                }

                // Draw Secondary Series if available
                if (secondarySummaries != null && secondarySummaries.isNotEmpty()) {
                    val secPoints = getPoints(secondarySummaries)
                    drawSeriesCurve(secPoints, secondaryColor, canvasHeight)
                }

                // Draw Primary Series
                if (primarySummaries.isNotEmpty()) {
                    val primePoints = getPoints(primarySummaries)
                    drawSeriesCurve(primePoints, primaryColor, canvasHeight)

                    // Draw touch scrub indicator if index selected
                    selectedIndex?.let { idx ->
                        if (idx in primePoints.indices) {
                            val selectedPoint = primePoints[idx]
                            drawLine(
                                color = primaryColor.copy(alpha = 0.7f),
                                start = Offset(selectedPoint.x, 0f),
                                end = Offset(selectedPoint.x, canvasHeight),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 7.dp.toPx(),
                                center = selectedPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = selectedPoint
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeriesCurve(
    points: List<Offset>,
    color: Color,
    canvasHeight: Float
) {
    if (points.isEmpty()) return

    if (points.size == 1) {
        drawCircle(color = color, radius = 6.dp.toPx(), center = points.first())
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = points.first())
        return
    }

    val strokePath = Path()
    val fillPath = Path()

    strokePath.moveTo(points.first().x, points.first().y)
    fillPath.moveTo(points.first().x, canvasHeight)
    fillPath.lineTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

        strokePath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            p1.x, p1.y
        )
        fillPath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            p1.x, p1.y
        )
    }

    fillPath.lineTo(points.last().x, canvasHeight)
    fillPath.close()

    // Draw area gradient fill
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.35f),
                color.copy(alpha = 0.05f),
                Color.Transparent
            ),
            startY = 0f,
            endY = canvasHeight
        )
    )

    // Draw main curve stroke
    drawPath(
        path = strokePath,
        color = color,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Draw dots for each point
    for (point in points) {
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = point
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = point
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No weekly records yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Log daily weights to visualize your weekly progress curves",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
