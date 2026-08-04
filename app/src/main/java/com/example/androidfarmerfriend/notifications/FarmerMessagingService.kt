package com.example.androidfarmerfriend.notifications

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FarmerMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_TOKEN = "fcm_token"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received")
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received - has notification: ${message.notification != null}, has data: ${message.data.isNotEmpty()}")

        val title = message.notification?.title ?: message.data["title"] ?: "Farmer Friend"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val channelId = message.data["channel"] ?: NotificationHelper.CHANNEL_PRICES
        val alertType = message.data["type"] ?: "PRICE"
        val location = message.data["location"] ?: ""
        val actionRoute = message.data["actionRoute"] ?: ""

        // Dedup key from the server (if any) — used for deterministic doc ID.
        val dedupeKey = message.data["dedupeKey"] ?: "${alertType}_${System.currentTimeMillis()}"

        Log.d(TAG, "Alert: title=$title, type=$alertType, location=$location, dedupeKey=$dedupeKey")

        // Save to Firestore with deterministic ID so FCM + workers don't duplicate.
        saveAlertToFirestore(title, body, alertType, location, actionRoute, dedupeKey)

        NotificationHelper.showNotification(
            context = this,
            channelId = channelId,
            title = title,
            message = body,
            notificationId = dedupeKey.hashCode(),
            data = message.data
        )
    }

    private fun saveAlertToFirestore(
        title: String,
        message: String,
        type: String,
        location: String,
        actionRoute: String,
        dedupeKey: String
    ) {
        val alertData = hashMapOf(
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis(),
            "location" to location,
            "isRead" to false,
            "actionRoute" to actionRoute,
            "time" to "Just now",
            "dedupeKey" to dedupeKey
        )

        Log.d(TAG, "Saving alert to Firestore: $alertData")

        // Deterministic doc ID — overwrites instead of appending duplicates.
        FirebaseFirestore.getInstance()
            .collection("alerts")
            .document(dedupeKey)
            .set(alertData)
            .addOnSuccessListener {
                Log.d(TAG, "Alert saved successfully with key: $dedupeKey")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save alert to Firestore", e)
            }
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
