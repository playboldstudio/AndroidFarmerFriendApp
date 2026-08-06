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
     * Sends daily market price summary at a randomized time within 7:00–7:30am.
     */
    fun schedulePriceAlerts(context: Context) {
        Log.d(TAG, "Scheduling daily price alerts (7:00–7:30am)")

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        // Stable "random" offset between 0 and 29 minutes derived from the target
        // day, so the daily push lands somewhere in 7:00–7:30am without varying
        // across app restarts (avoids duplicate-schedule drift).
        val daySeed = target.timeInMillis / 86_400_000L
        val randomMinute = (daySeed * 2654435761L).toInt().let { if (it < 0) -it else it } % 30
        val delay = (target.timeInMillis - now.timeInMillis) + randomMinute * 60_000L

        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(
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
            PriceAlertWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelPriceAlerts(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PriceAlertWorker.WORK_NAME)
    }
}
