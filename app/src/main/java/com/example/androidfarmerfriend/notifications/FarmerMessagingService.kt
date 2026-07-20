package com.example.androidfarmerfriend.notifications

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class FarmerMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_TOKEN = "fcm_token"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "Farmer Friend"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val channelId = message.data["channel"] ?: NotificationHelper.CHANNEL_PRICES

        NotificationHelper.showNotification(
            context = this,
            channelId = channelId,
            title = title,
            message = body,
            data = message.data
        )
    }

    private fun saveToken(token: String) {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }
}
