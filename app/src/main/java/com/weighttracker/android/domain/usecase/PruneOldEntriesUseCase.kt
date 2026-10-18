package com.weighttracker.android.domain.usecase

import com.weighttracker.android.domain.model.UserProfile
import com.weighttracker.android.domain.repository.WeightRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class PruneResult(
    val summariesArchived: Int,
    val rawEntriesPruned: Int
)

class PruneOldEntriesUseCase(
    private val weightRepository: WeightRepository,
    private val calculateWeeklyAverageUseCase: CalculateWeeklyAverageUseCase
) {

    suspend operator fun invoke(specificUserId: String? = null): PruneResult {
        val userIds = if (specificUserId != null) {
            listOf(specificUserId)
        } else {
            UserProfile.entries.map { it.id }
        }

        val currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var totalSummariesArchived = 0
        var totalEntriesPruned = 0

        for (userId in userIds) {
            val oldEntries = weightRepository.getEntriesOlderThan(userId, currentMonday)
            if (oldEntries.isNotEmpty()) {
                val weeklySummaries = calculateWeeklyAverageUseCase(userId, oldEntries)
                weightRepository.executePruneAndArchive(userId, currentMonday, weeklySummaries)
                totalSummariesArchived += weeklySummaries.size
                totalEntriesPruned += oldEntries.size
            }
        }

        return PruneResult(
            summariesArchived = totalSummariesArchived,
            rawEntriesPruned = totalEntriesPruned
        )
    }
}
