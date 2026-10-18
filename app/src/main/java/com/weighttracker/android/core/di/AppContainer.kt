package com.weighttracker.android.core.di

import android.content.Context
import com.weighttracker.android.core.database.AppDatabase
import com.weighttracker.android.core.datastore.UserPreferencesDataSource
import com.weighttracker.android.core.notification.NotificationHelper
import com.weighttracker.android.data.repository.UserPreferencesRepositoryImpl
import com.weighttracker.android.data.repository.WeightRepositoryImpl
import com.weighttracker.android.domain.repository.UserPreferencesRepository
import com.weighttracker.android.domain.repository.WeightRepository
import com.weighttracker.android.domain.usecase.CalculateWeeklyAverageUseCase
import com.weighttracker.android.domain.usecase.DeleteWeightEntryUseCase
import com.weighttracker.android.domain.usecase.GetWeightHistoryUseCase
import com.weighttracker.android.domain.usecase.PruneOldEntriesUseCase
import com.weighttracker.android.domain.usecase.SaveWeightEntryUseCase

interface AppContainer {
    val database: AppDatabase
    val weightRepository: WeightRepository
    val userPreferencesRepository: UserPreferencesRepository
    val calculateWeeklyAverageUseCase: CalculateWeeklyAverageUseCase
    val saveWeightEntryUseCase: SaveWeightEntryUseCase
    val deleteWeightEntryUseCase: DeleteWeightEntryUseCase
    val pruneOldEntriesUseCase: PruneOldEntriesUseCase
    val getWeightHistoryUseCase: GetWeightHistoryUseCase
    val notificationHelper: NotificationHelper
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val weightRepository: WeightRepository by lazy {
        WeightRepositoryImpl(
            database = database,
            weightEntryDao = database.weightEntryDao(),
            weeklySummaryDao = database.weeklySummaryDao()
        )
    }

    private val userPreferencesDataSource: UserPreferencesDataSource by lazy {
        UserPreferencesDataSource(context)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(userPreferencesDataSource)
    }

    override val calculateWeeklyAverageUseCase: CalculateWeeklyAverageUseCase by lazy {
        CalculateWeeklyAverageUseCase()
    }

    override val saveWeightEntryUseCase: SaveWeightEntryUseCase by lazy {
        SaveWeightEntryUseCase(weightRepository)
    }

    override val deleteWeightEntryUseCase: DeleteWeightEntryUseCase by lazy {
        DeleteWeightEntryUseCase(weightRepository)
    }

    override val pruneOldEntriesUseCase: PruneOldEntriesUseCase by lazy {
        PruneOldEntriesUseCase(weightRepository, calculateWeeklyAverageUseCase)
    }

    override val getWeightHistoryUseCase: GetWeightHistoryUseCase by lazy {
        GetWeightHistoryUseCase(weightRepository, calculateWeeklyAverageUseCase)
    }

    override val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }
}
