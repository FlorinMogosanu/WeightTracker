package com.weighttracker.android.domain.usecase

import com.weighttracker.android.domain.repository.WeightRepository

class DeleteWeightEntryUseCase(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteEntryById(id)
    }
}
