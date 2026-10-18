package com.weighttracker.android.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val UNIQUE_REMINDER_WORK = "daily_weight_reminder_work"
    private const val UNIQUE_PRUNING_WORK = "periodic_pruning_work"

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }

        val initialDelayMillis = Duration.between(now, targetTime).toMillis()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_REMINDER_WORK)
    }

    fun schedulePeriodicPruning(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val pruningRequest = PeriodicWorkRequestBuilder<PruningWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PRUNING_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            pruningRequest
        )
    }
}
