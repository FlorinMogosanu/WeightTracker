package com.weighttracker.android.domain.usecase

import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeeklySummary
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

class CalculateWeeklyAverageUseCase {

    operator fun invoke(userId: String, entries: List<WeightEntry>): List<WeeklySummary> {
        if (entries.isEmpty()) return emptyList()

        val weekFields = WeekFields.ISO
        val groupedByWeek = entries.groupBy { entry ->
            val year = entry.date.get(weekFields.weekBasedYear())
            val week = entry.date.get(weekFields.weekOfWeekBasedYear())
            year to week
        }

        return groupedByWeek.map { (yearWeek, weekEntries) ->
            val (year, week) = yearWeek
            val averageWeight = weekEntries.map { it.weightKg }.average()
            val sampleDate = weekEntries.first().date
            val monday = sampleDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val sunday = monday.plusDays(6)

            WeeklySummary(
                id = 0,
                userId = userId,
                year = year,
                weekNumber = week,
                averageWeightKg = averageWeight,
                startDate = monday,
                endDate = sunday,
                entryCount = weekEntries.size
            )
        }.sortedWith(compareBy({ it.year }, { it.weekNumber }))
    }
}
