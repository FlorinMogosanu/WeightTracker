package com.weighttracker.android.presentation.components

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val currentLocale = remember(configuration) {
        val list = configuration.locales
        if (!list.isEmpty) list[0] else Locale.getDefault()
    }

    // Ensure the calendar starts on Monday
    val mondayLocale = remember(currentLocale) {
        if (WeekFields.of(currentLocale).firstDayOfWeek == DayOfWeek.MONDAY) {
            currentLocale
        } else {
            // Keep the user's language while setting region to GB (ISO-8601, Monday first)
            Locale(currentLocale?.language ?: "en", "GB")
        }
    }

    val mondayConfiguration = remember(configuration, mondayLocale) {
        Configuration(configuration).apply {
            setLocales(LocaleList(mondayLocale))
        }
    }

    CompositionLocalProvider(LocalConfiguration provides mondayConfiguration) {
        val initialMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateSelected(localDate)
                        }
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

