package com.weighttracker.android.domain.usecase

import com.weighttracker.android.domain.model.WeightEntry
import com.weighttracker.android.domain.repository.WeightRepository

class SaveWeightEntryUseCase(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(entry: WeightEntry): Result<Long> {
        if (entry.weightKg <= 0.0 || entry.weightKg > 500.0) {
            return Result.failure(IllegalArgumentException("Weight must be between 1 and 500 kg."))
        }
        return try {
            val id = repository.upsertEntry(entry)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
