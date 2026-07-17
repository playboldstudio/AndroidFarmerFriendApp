package com.example.androidfarmerfriend

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FarmerFriendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Crashlytics collection in release builds
        // In debug builds, it might be better to keep it disabled to avoid polluting data
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
