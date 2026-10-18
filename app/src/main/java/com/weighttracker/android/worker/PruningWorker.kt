package com.weighttracker.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weighttracker.android.core.database.AppDatabase
import com.weighttracker.android.core.datastore.UserPreferencesDataSource
import com.weighttracker.android.data.repository.WeightRepositoryImpl
import com.weighttracker.android.domain.usecase.CalculateWeeklyAverageUseCase
import com.weighttracker.android.domain.usecase.PruneOldEntriesUseCase
import kotlinx.coroutines.flow.first

class PruningWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userPreferencesDataSource = UserPreferencesDataSource(applicationContext)
            val settings = userPreferencesDataSource.userSettings.first()

            if (settings.autoPruneEnabled) {
                val db = AppDatabase.getInstance(applicationContext)
                val repository = WeightRepositoryImpl(db, db.weightEntryDao(), db.weeklySummaryDao())
                val calculateWeeklyAverageUseCase = CalculateWeeklyAverageUseCase()
                val pruneOldEntriesUseCase = PruneOldEntriesUseCase(repository, calculateWeeklyAverageUseCase)

                pruneOldEntriesUseCase()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
