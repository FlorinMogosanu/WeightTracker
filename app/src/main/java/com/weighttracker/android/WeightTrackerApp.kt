package com.weighttracker.android

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.weighttracker.android.core.di.AppContainer
import com.weighttracker.android.core.di.DefaultAppContainer
import com.weighttracker.android.worker.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeightTrackerApp : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            container = DefaultAppContainer(this)
        } catch (t: Throwable) {
            Log.e("WeightTrackerApp", "Failed to initialize DefaultAppContainer", t)
        }

        applicationScope.launch {
            // Pre-create notification channel safely
            try {
                container.notificationHelper
            } catch (t: Throwable) {
                Log.e("WeightTrackerApp", "Failed to initialize NotificationHelper", t)
            }

            // Initialize background work requests safely
            try {
                WorkScheduler.schedulePeriodicPruning(this@WeightTrackerApp)
            } catch (t: Throwable) {
                Log.e("WeightTrackerApp", "Failed to schedule periodic pruning", t)
            }

            // Check if daily reminder should be scheduled based on stored preferences
            try {
                val settings = container.userPreferencesRepository.userSettings.first()
                if (settings.reminderEnabled) {
                    WorkScheduler.scheduleDailyReminder(
                        context = this@WeightTrackerApp,
                        hour = settings.reminderHour,
                        minute = settings.reminderMinute
                    )
                }
            } catch (t: Throwable) {
                Log.e("WeightTrackerApp", "Failed to schedule daily reminder", t)
            }
        }
    }
}
