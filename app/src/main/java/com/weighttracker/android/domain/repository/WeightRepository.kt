package com.weighttracker.android.domain.repository

import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeeklySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WeightRepository {
    fun getEntriesForUser(userId: String): Flow<List<WeightEntry>>
    fun getEntriesForUserBetween(userId: String, start: LocalDate, end: LocalDate): Flow<List<WeightEntry>>
    fun getLatestEntryForUser(userId: String): Flow<WeightEntry?>
    suspend fun getEntryForUserAndDate(userId: String, date: LocalDate): WeightEntry?
    suspend fun upsertEntry(entry: WeightEntry): Long
    suspend fun deleteEntryById(id: Long)

    fun getWeeklySummariesForUser(userId: String): Flow<List<WeeklySummary>>
    suspend fun getWeeklySummary(userId: String, year: Int, weekNumber: Int): WeeklySummary?
    suspend fun upsertWeeklySummary(summary: WeeklySummary): Long
    suspend fun upsertWeeklySummaries(summaries: List<WeeklySummary>)

    suspend fun getEntriesOlderThan(userId: String, cutoffDate: LocalDate): List<WeightEntry>
    suspend fun executePruneAndArchive(userId: String, cutoffDate: LocalDate, summaries: List<WeeklySummary>)
}
