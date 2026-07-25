package com.example.androidfarmerfriend

import android.app.Application
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.notifications.NotificationHelper
import com.example.androidfarmerfriend.notifications.WorkManagerScheduler
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FarmerFriendApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()

        ApiClient.init(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        NotificationHelper.createChannels(this)
        WorkManagerScheduler.scheduleDailyDigest(this)
        WorkManagerScheduler.scheduleWeatherAlerts(this)

        // Debug: send test alerts every 1 hour
        if (BuildConfig.DEBUG) {
            WorkManagerScheduler.scheduleTestNotifications(this)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
