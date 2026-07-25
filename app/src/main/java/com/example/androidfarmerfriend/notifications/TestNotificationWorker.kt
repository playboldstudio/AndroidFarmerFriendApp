package com.example.androidfarmerfriend.notifications

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Test worker that creates an alert in Firestore + shows a notification.
 * After each successful run, it schedules itself again after 1 minute.
 * Only active in debug builds.
 */
class TestNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val testAlert = testAlerts[Random.nextInt(testAlerts.size)]

            val alertData = hashMapOf(
                "title" to testAlert.title,
                "message" to testAlert.message,
                "type" to testAlert.type,
                "timestamp" to System.currentTimeMillis(),
                "location" to testAlert.location,
                "isRead" to false,
                "actionRoute" to testAlert.route,
                "time" to "Just now"
            )

            val docRef = FirebaseFirestore.getInstance()
                .collection("alerts")
                .add(alertData)
                .await()

            Log.d(TAG, "Test alert saved: ${testAlert.title} (id: ${docRef.id})")

            val channelId = when (testAlert.type) {
                "PRICE" -> NotificationHelper.CHANNEL_PRICES
                "WEATHER" -> NotificationHelper.CHANNEL_WEATHER
                "CROP" -> NotificationHelper.CHANNEL_CROP
                else -> NotificationHelper.CHANNEL_PRICES
            }

            NotificationHelper.showNotification(
                context = applicationContext,
                channelId = channelId,
                title = testAlert.title,
                message = testAlert.message,
                notificationId = Random.nextInt(100000)
            )

            // Schedule the next test alert in 1 hour (debug testing)
            scheduleNext()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create test alert", e)
            // Still schedule next even on failure, so the chain doesn't break
            scheduleNext()
            Result.success()
        }
    }

    private fun scheduleNext() {
        val nextRequest = OneTimeWorkRequestBuilder<TestNotificationWorker>()
            .setInitialDelay(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(TAG)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueue(nextRequest)
        Log.d(TAG, "Next test alert scheduled in 1 minute")
    }

    companion object {
        const val TAG = "TestNotifWorker"
        const val WORK_NAME = "test_notification"

        private data class TestAlert(
            val title: String,
            val message: String,
            val type: String,
            val location: String,
            val route: String
        )

        private val testAlerts = listOf(
            TestAlert(
                title = "🍅 Tomato Price Drop",
                message = "Tomato price dropped to ₹18/kg in Koyambedu market, down 12% from yesterday.",
                type = "PRICE",
                location = "Koyambedu",
                route = "market"
            ),
            TestAlert(
                title = "🌧️ Heavy Rain Warning",
                message = "Heavy rainfall expected in Chennai district tomorrow. Secure your crops and ensure proper drainage.",
                type = "WEATHER",
                location = "Chennai",
                route = "weather"
            ),
            TestAlert(
                title = "🐛 Pest Alert - Brinjal",
                message = "Fruit borer infestation spotted in brinjal fields near Salem. Use pheromone traps immediately.",
                type = "CROP",
                location = "Salem",
                route = "disease"
            ),
            TestAlert(
                title = "📈 Onion Price Surge",
                message = "Onion price increased to ₹52/kg in Bangalore market, up 18% this week.",
                type = "PRICE",
                location = "Bangalore",
                route = "market"
            ),
            TestAlert(
                title = "⛈️ Storm Alert",
                message = "Thunderstorm warning for Coimbatore district. Avoid field work between 2PM-6PM today.",
                type = "WEATHER",
                location = "Coimbatore",
                route = "weather"
            ),
            TestAlert(
                title = "🌾 Rice Disease Warning",
                message = "Rice blast disease risk high due to humid conditions in Thanjavur. Apply preventive fungicide.",
                type = "CROP",
                location = "Thanjavur",
                route = "disease"
            ),
            TestAlert(
                title = "💰 Gold Price Update",
                message = "Gold price reached ₹7,250/gram in Chennai market. Good time for planned purchases.",
                type = "PRICE",
                location = "Chennai",
                route = "market"
            ),
            TestAlert(
                title = "☀️ Heat Wave Alert",
                message = "Extreme heat warning for Madurai district. Ensure adequate water supply for livestock.",
                type = "WEATHER",
                location = "Madurai",
                route = "weather"
            )
        )
    }
}
