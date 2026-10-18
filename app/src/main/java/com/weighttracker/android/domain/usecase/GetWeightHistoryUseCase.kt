package com.weighttracker.android.domain.usecase

import com.weighttracker.android.domain.model.WeeklySummary
import com.weighttracker.android.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetWeightHistoryUseCase(
    private val repository: WeightRepository,
    private val calculateWeeklyAverageUseCase: CalculateWeeklyAverageUseCase
) {
    operator fun invoke(userId: String): Flow<List<WeeklySummary>> {
        return combine(
            repository.getEntriesForUser(userId),
            repository.getWeeklySummariesForUser(userId)
        ) { entries, archivedSummaries ->
            // Compute weekly summaries for all currently stored raw entries
            val liveComputedSummaries = calculateWeeklyAverageUseCase(userId, entries)

            // Merge archived summaries with live computed summaries.
            // If both have an entry for the same (year, weekNumber), the live computed one takes precedence.
            val mergedMap = LinkedHashMap<Pair<Int, Int>, WeeklySummary>()

            for (archived in archivedSummaries) {
                mergedMap[archived.year to archived.weekNumber] = archived
            }
            for (live in liveComputedSummaries) {
                mergedMap[live.year to live.weekNumber] = live
            }

            mergedMap.values.sortedWith(
                compareBy({ it.year }, { it.weekNumber })
            )
        }
    }
}
