package com.example.androidfarmerfriend

import android.app.Application
import com.example.androidfarmerfriend.data.api.ApiClient
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FarmerFriendApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ApiClient.init(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
