package com.weighttracker.android.data.repository

import com.weighttracker.android.core.database.AppDatabase
import com.weighttracker.android.core.database.dao.WeightEntryDao
import com.weighttracker.android.core.database.dao.WeeklySummaryDao
import com.weighttracker.android.data.mapper.toDomain
import com.weighttracker.android.data.mapper.toEntity
import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.model.WeeklySummary
import com.weighttracker.android.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WeightRepositoryImpl(
    private val database: AppDatabase,
    private val weightEntryDao: WeightEntryDao,
    private val weeklySummaryDao: WeeklySummaryDao
) : WeightRepository {

    override fun getEntriesForUser(userId: String): Flow<List<WeightEntry>> {
        return weightEntryDao.getEntriesForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEntriesForUserBetween(
        userId: String,
        start: LocalDate,
        end: LocalDate
    ): Flow<List<WeightEntry>> {
        return weightEntryDao.getEntriesForUserBetween(
            userId = userId,
            startDate = start.toEpochDay(),
            endDate = end.toEpochDay()
        ).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLatestEntryForUser(userId: String): Flow<WeightEntry?> {
        return weightEntryDao.getLatestEntry(userId).map { it?.toDomain() }
    }

    override suspend fun getEntryForUserAndDate(userId: String, date: LocalDate): WeightEntry? {
        return weightEntryDao.getEntryForUserAndDate(userId, date.toEpochDay())?.toDomain()
    }

    override suspend fun upsertEntry(entry: WeightEntry): Long {
        return weightEntryDao.upsert(entry.toEntity())
    }

    override suspend fun deleteEntryById(id: Long) {
        weightEntryDao.deleteById(id)
    }

    override fun getWeeklySummariesForUser(userId: String): Flow<List<WeeklySummary>> {
        return weeklySummaryDao.getSummariesForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWeeklySummary(userId: String, year: Int, weekNumber: Int): WeeklySummary? {
        return weeklySummaryDao.getSummary(userId, year, weekNumber)?.toDomain()
    }

    override suspend fun upsertWeeklySummary(summary: WeeklySummary): Long {
        return weeklySummaryDao.upsert(summary.toEntity())
    }

    override suspend fun upsertWeeklySummaries(summaries: List<WeeklySummary>) {
        weeklySummaryDao.upsertAll(summaries.map { it.toEntity() })
    }

    override suspend fun getEntriesOlderThan(userId: String, cutoffDate: LocalDate): List<WeightEntry> {
        return weightEntryDao.getEntriesOlderThan(userId, cutoffDate.toEpochDay()).map { it.toDomain() }
    }

    override suspend fun executePruneAndArchive(
        userId: String,
        cutoffDate: LocalDate,
        summaries: List<WeeklySummary>
    ) {
        database.executePruneAndArchive(
            userId = userId,
            cutoffEpochDay = cutoffDate.toEpochDay(),
            summaries = summaries.map { it.toEntity() }
        )
    }
}
