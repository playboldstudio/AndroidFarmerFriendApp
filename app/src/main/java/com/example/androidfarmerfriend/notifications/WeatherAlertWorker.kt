package com.example.androidfarmerfriend.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Periodically checks real weather data from Open-Meteo API and creates
 * alerts for severe conditions. Works in background via WorkManager.
 *
 * Checks every 3 hours for: thunderstorms, heavy rain, extreme heat/cold,
 * high winds, and heavy snow. Deduplicates by condition+date.
 */
class WeatherAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val locationPrefs = LocationPrefs(applicationContext)
            val location = locationPrefs.selectedLocation

            Log.d(TAG, "Checking weather for ${location.name} (${location.lat}, ${location.lon})")

            val response = ApiClient.weatherApi.getForecast(
                latitude = location.lat,
                longitude = location.lon
            )

            val current = response.current
            val daily = response.daily

            if (current == null) {
                Log.w(TAG, "No current weather data")
                return Result.success()
            }

            val alerts = mutableListOf<WeatherAlert>()

            // Check current conditions
            current.weatherCode?.let { code ->
                analyzeCurrentWeather(code, current, location.name, alerts)
            }

            // Check today's forecast
            daily?.let { d ->
                analyzeDailyForecast(d, location.name, alerts)
            }

            // Deduplicate and save
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

            for (alert in alerts) {
                val dedupeKey = "${alert.dedupeKey}_$today"
                if (prefs.getBoolean(dedupeKey, false)) {
                    Log.d(TAG, "Skipping duplicate alert: ${alert.title}")
                    continue
                }

                saveAlertToFirestore(alert, dedupeKey)
                showNotification(alert, dedupeKey)

                // Mark as sent
                prefs.edit().putBoolean(dedupeKey, true).apply()
                Log.d(TAG, "Weather alert sent: ${alert.title}")
            }

            if (alerts.isEmpty()) {
                Log.d(TAG, "No severe weather conditions detected")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Weather alert check failed", e)
            Result.retry()
        }
    }

    private fun analyzeCurrentWeather(
        code: Int,
        current: com.example.androidfarmerfriend.data.api.OpenMeteoCurrent,
        locationName: String,
        alerts: MutableList<WeatherAlert>
    ) {
        val temp = current.temperature ?: return
        val windSpeed = current.windSpeed ?: 0.0
        val humidity = current.humidity ?: 0.0

        when (code) {
            95 -> alerts.add(
                WeatherAlert(
                    title = "⛈️ Thunderstorm Alert",
                    message = "Thunderstorm detected near $locationName. Stay indoors and avoid open fields.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "thunderstorm"
                )
            )
            96, 99 -> alerts.add(
                WeatherAlert(
                    title = "⛈️ Severe Thunderstorm",
                    message = "Severe thunderstorm with hail near $locationName. Seek shelter immediately.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "severe_thunderstorm"
                )
            )
            in 61..67 -> alerts.add(
                WeatherAlert(
                    title = "Heavy Rain Warning",
                    message = "Heavy rainfall in $locationName. Ensure proper drainage and protect crops.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "heavy_rain"
                )
            )
            in 71..77 -> alerts.add(
                WeatherAlert(
                    title = "Snow Alert",
                    message = "Snowfall expected in $locationName. Protect livestock and sensitive crops.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "snow"
                )
            )
            in 80..82 -> if (current.precipitation != null && current.precipitation > 10) {
                alerts.add(
                    WeatherAlert(
                        title = "Heavy Showers",
                        message = "Heavy rain showers in $locationName (${current.precipitation}mm). Secure farm equipment.",
                        type = "WEATHER",
                        route = "weather",
                        dedupeKey = "heavy_showers"
                    )
                )
            }
        }

        // Temperature alerts
        when {
            temp >= 42 -> alerts.add(
                WeatherAlert(
                    title = "Extreme Heat Warning",
                    message = "Temperature at ${temp.toInt()}°C in $locationName. Provide shade and water for livestock.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "extreme_heat"
                )
            )
            temp >= 38 -> alerts.add(
                WeatherAlert(
                    title = "Heat Wave Alert",
                    message = "Temperature at ${temp.toInt()}°C in $locationName. Irrigate crops during early morning.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "heat_wave"
                )
            )
            temp <= 5 -> alerts.add(
                WeatherAlert(
                    title = "🥶 Cold Wave Alert",
                    message = "Temperature at ${temp.toInt()}°C in $locationName. Protect sensitive crops from frost.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "cold_wave"
                )
            )
        }

        // Wind alert
        if (windSpeed > 50) {
            alerts.add(
                WeatherAlert(
                    title = "💨 High Wind Alert",
                    message = "Wind speed at ${windSpeed.toInt()} km/h in $locationName. Secure loose structures and crops.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "high_wind"
                )
            )
        }
    }

    private fun analyzeDailyForecast(
        daily: com.example.androidfarmerfriend.data.api.OpenMeteoDaily,
        locationName: String,
        alerts: MutableList<WeatherAlert>
    ) {
        val todayMaxTemp = daily.tempMax?.firstOrNull() ?: return
        val todayMinTemp = daily.tempMin?.firstOrNull() ?: return
        val rainProb = daily.precipitationProbabilityMax?.firstOrNull()
        val todayCode = daily.weatherCode?.firstOrNull()

        // High temperature forecast
        if (todayMaxTemp >= 40) {
            alerts.add(
                WeatherAlert(
                    title = "🌡️ High Temperature Forecast",
                    message = "Temperature expected to reach ${todayMaxTemp.toInt()}°C in $locationName today. Plan irrigation accordingly.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "forecast_high_temp"
                )
            )
        }

        // Low temperature forecast (frost risk)
        if (todayMinTemp <= 5 && todayMinTemp > -10) {
            alerts.add(
                WeatherAlert(
                    title = "Frost Risk",
                    message = "Temperature expected to drop to ${todayMinTemp.toInt()}°C tonight in $locationName. Cover sensitive plants.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "frost_risk"
                )
            )
        }

        // High rain probability
        if (rainProb != null && rainProb > 80 && todayCode != null && todayCode in 51..82) {
            alerts.add(
                WeatherAlert(
                    title = "Rain Expected Today",
                    message = "${rainProb}% chance of rain in $locationName today. Plan field work accordingly.",
                    type = "WEATHER",
                    route = "weather",
                    dedupeKey = "rain_forecast"
                )
            )
        }
    }

    private suspend fun saveAlertToFirestore(alert: WeatherAlert, dedupeKey: String) {
        val alertData = hashMapOf(
            "title" to alert.title,
            "message" to alert.message,
            "type" to alert.type,
            "timestamp" to System.currentTimeMillis(),
            "location" to "",
            "isRead" to false,
            "actionRoute" to alert.route,
            "time" to "Just now",
            "dedupeKey" to dedupeKey
        )

        // Deterministic doc ID prevents duplicate Firestore docs on re-runs.
        FirebaseFirestore.getInstance()
            .collection("alerts")
            .document(dedupeKey)
            .set(alertData)
            .await()
    }

    private fun showNotification(alert: WeatherAlert, dedupeKey: String) {
        NotificationHelper.showNotification(
            context = applicationContext,
            channelId = NotificationHelper.CHANNEL_WEATHER,
            title = alert.title,
            message = alert.message,
            notificationId = dedupeKey.hashCode()
        )
    }

    private data class WeatherAlert(
        val title: String,
        val message: String,
        val type: String,
        val route: String,
        val dedupeKey: String
    )

    companion object {
        private const val TAG = "WeatherAlertWorker"
        private const val PREFS_NAME = "weather_alert_prefs"
        const val WORK_NAME = "weather_alerts"
    }
}
