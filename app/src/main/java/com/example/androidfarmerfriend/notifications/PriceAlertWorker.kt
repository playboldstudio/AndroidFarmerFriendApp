package com.example.androidfarmerfriend.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.api.NetworkErrors
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Runs daily at 7:15am. Fetches current market prices from the API
 * and creates a summary alert with top price movements.
 * Also checks for significant price changes vs stored previous prices.
 */
class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Skip if already ran today
            if (prefs.getString(LAST_RUN_KEY, "") == today) {
                Log.d(TAG, "Already ran today, skipping")
                return Result.success()
            }

            Log.d(TAG, "Fetching market prices for daily alert")

            // Read user's saved location instead of hardcoding koyambedu.
            val locationPrefs = LocationPrefs(applicationContext)
            val userLocation = locationPrefs.selectedLocation
            val marketSlug = resolveMarketSlug(userLocation.name)
            val marketDisplayName = userLocation.marketName
            Log.d(TAG, "Using market: $marketSlug (from location: ${userLocation.name})")

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val messages = mutableListOf<String>()

            // Fetch vegetable prices
            try {
                val vegResponse = ApiClient.vegetableMarketApi.getVegetablePrices(marketSlug, dateStr)
                val vegData = vegResponse.data
                if (!vegData.isNullOrEmpty()) {
                    val topVeg = vegData.take(5)
                    val summary = topVeg.joinToString("\n") { item ->
                        val name = item.vegetablename ?: item.columnNameEng ?: "Unknown"
                        val price = item.price ?: 0
                        val unit = item.units ?: "kg"
                        "  - $name: Rs.${"%.0f".format(price)}/$unit"
                    }
                    messages.add("Top Vegetable Prices ($marketDisplayName):\n$summary")
                }
            } catch (e: Exception) {
                NetworkErrors.record("PriceAlertWorker.veg", e)
            }

            // Fetch fruit prices
            try {
                val fruitResponse = ApiClient.vegetableMarketApi.getFruitPrices(marketSlug, dateStr)
                val fruitData = fruitResponse.data
                if (!fruitData.isNullOrEmpty()) {
                    val topFruit = fruitData.take(3)
                    val summary = topFruit.joinToString("\n") { item ->
                        val name = item.fruitname ?: item.columnNameEng ?: "Unknown"
                        val price = item.price ?: 0
                        val unit = item.units ?: "kg"
                        "  - $name: Rs.${"%.0f".format(price)}/$unit"
                    }
                    messages.add("Top Fruit Prices ($marketDisplayName):\n$summary")
                }
            } catch (e: Exception) {
                NetworkErrors.record("PriceAlertWorker.fruit", e)
            }

            // Fetch egg prices
            try {
                val cal = java.util.Calendar.getInstance()
                val month = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
                val year = cal.get(java.util.Calendar.YEAR).toString()
                val eggResponse = ApiClient.eggRatesApi.getEggRates(month = month, year = year)
                val eggData = eggResponse.firstOrNull()
                if (eggData != null) {
                    val price = eggData.price ?: 0
                    val city = eggData.city ?: "Chennai"
                    messages.add("Egg Rate ($city): Rs.${"%.2f".format(price)}/piece")
                }
            } catch (e: Exception) {
                NetworkErrors.record("PriceAlertWorker.egg", e)
            }

            if (messages.isEmpty()) {
                Log.d(TAG, "No price data available")
                return Result.success()
            }

            val title = "Daily Market Prices"
            val body = buildString {
                append("Today's market update:\n\n")
                append(messages.joinToString("\n\n"))
            }

            // Save to Firestore with a deterministic doc ID so re-runs overwrite
            // instead of appending duplicates.
            val alertData = hashMapOf(
                "title" to title,
                "message" to body,
                "type" to "PRICE",
                "timestamp" to System.currentTimeMillis(),
                "location" to marketDisplayName,
                "isRead" to false,
                "actionRoute" to "market",
                "time" to "Just now",
                "dedupeKey" to "daily_price_$today"
            )

            FirebaseFirestore.getInstance()
                .collection("alerts")
                .document("daily_price_$today")
                .set(alertData)
                .await()

            // Show a single, fixed-ID notification (replaces, never stacks).
            NotificationHelper.showNotification(
                context = applicationContext,
                channelId = NotificationHelper.CHANNEL_PRICES,
                title = title,
                message = "Market prices updated - tap to view details",
                notificationId = DAILY_MARKET_NOTIFICATION_ID
            )

            // Mark as ran today
            prefs.edit().putString(LAST_RUN_KEY, today).apply()
            Log.d(TAG, "Daily price alert sent")

            Result.success()
        } catch (e: Exception) {
            NetworkErrors.record("PriceAlertWorker.overall", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PriceAlertWorker"
        private const val PREFS_NAME = "price_alert_prefs"
        private const val LAST_RUN_KEY = "last_run_date"
        const val WORK_NAME = "price_alerts"
        const val DAILY_MARKET_NOTIFICATION_ID = 7001

        /**
         * Maps a user location name (e.g. "Chennai, Tamil Nadu") to the
         * closest matching market API slug. Falls back to "koyambedu"
         * (major market with veg + fruit support) when no match is found.
         */
        fun resolveMarketSlug(locationName: String): String {
            val city = locationName.substringBefore(",").trim().lowercase()
            return when {
                "chennai" in city -> "chennai"
                "bangalore" in city || "bengaluru" in city -> "bangalore"
                else -> "koyambedu"
            }
        }
    }
}
