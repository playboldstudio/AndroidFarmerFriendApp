package com.example.androidfarmerfriend.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val TAG = "WorkManagerScheduler"

    fun scheduleDailyDigest(context: Context) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<DailyDigestWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyDigestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDailyDigest(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyDigestWorker.WORK_NAME)
    }

    /**
     * Checks weather every 3 hours via Open-Meteo API.
     * Creates alerts + notifications for severe conditions.
     * Runs in background via WorkManager — works even when app is killed.
     */
    fun scheduleWeatherAlerts(context: Context) {
        Log.d(TAG, "Scheduling weather alerts (every 3 hours)")

        val request = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
            3, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeatherAlertWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelWeatherAlerts(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WeatherAlertWorker.WORK_NAME)
    }

    /**
     * Starts the test notification chain. The worker schedules itself
     * again after each run, creating a 1-minute loop.
     */
    fun scheduleTestNotifications(context: Context) {
        Log.d(TAG, "Starting test notification chain (1 hour interval)")
        val request = OneTimeWorkRequestBuilder<TestNotificationWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(TestNotificationWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelTestNotifications(context: Context) {
        Log.d(TAG, "Cancelling test notifications")
        WorkManager.getInstance(context).cancelAllWorkByTag(TestNotificationWorker.TAG)
    }
}
