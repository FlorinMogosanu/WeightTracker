package com.weighttracker.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weighttracker.android.core.datastore.UserPreferencesDataSource
import com.weighttracker.android.core.notification.NotificationHelper
import kotlinx.coroutines.flow.first

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userPreferencesDataSource = UserPreferencesDataSource(applicationContext)
        val settings = userPreferencesDataSource.userSettings.first()

        if (settings.reminderEnabled) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showReminderNotification()
        }

        return Result.success()
    }
}
