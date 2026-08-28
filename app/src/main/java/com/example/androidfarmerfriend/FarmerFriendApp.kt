package com.example.androidfarmerfriend

import android.app.Application
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.cache.OfflineCache
import com.example.androidfarmerfriend.notifications.NotificationHelper
import com.example.androidfarmerfriend.notifications.WorkManagerScheduler
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FarmerFriendApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()

        ApiClient.init(this)
        OfflineCache.init(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        NotificationHelper.createChannels(this)
        WorkManagerScheduler.scheduleWeatherAlerts(this)
        WorkManagerScheduler.schedulePriceAlerts(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
