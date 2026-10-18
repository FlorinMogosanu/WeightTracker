package com.weighttracker.android.data.mapper

import com.weighttracker.android.core.database.entity.WeightEntryEntity
import com.weighttracker.android.core.database.entity.WeeklySummaryEntity
import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeeklySummary
import java.time.LocalDate

fun WeightEntryEntity.toDomain(): WeightEntry {
    return WeightEntry(
        id = id,
        userId = userId,
        date = LocalDate.ofEpochDay(date),
        weightKg = weightKg,
        note = note,
        createdAt = createdAt
    )
}

fun WeightEntry.toEntity(): WeightEntryEntity {
    return WeightEntryEntity(
        id = id,
        userId = userId,
        date = date.toEpochDay(),
        weightKg = weightKg,
        note = note,
        createdAt = createdAt
    )
}

fun WeeklySummaryEntity.toDomain(): WeeklySummary {
    return WeeklySummary(
        id = id,
        userId = userId,
        year = year,
        weekNumber = weekNumber,
        averageWeightKg = averageWeightKg,
        startDate = LocalDate.ofEpochDay(startDate),
        endDate = LocalDate.ofEpochDay(endDate),
        entryCount = entryCount
    )
}

fun WeeklySummary.toEntity(): WeeklySummaryEntity {
    return WeeklySummaryEntity(
        id = id,
        userId = userId,
        year = year,
        weekNumber = weekNumber,
        averageWeightKg = averageWeightKg,
        startDate = startDate.toEpochDay(),
        endDate = endDate.toEpochDay(),
        entryCount = entryCount
    )
}
