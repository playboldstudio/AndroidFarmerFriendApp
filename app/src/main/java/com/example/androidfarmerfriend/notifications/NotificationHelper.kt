package com.example.androidfarmerfriend.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.androidfarmerfriend.MainActivity
import com.example.androidfarmerfriend.R

object NotificationHelper {

    const val CHANNEL_PRICES = "price_alerts"
    const val CHANNEL_WEATHER = "weather_alerts"
    const val CHANNEL_CROP = "crop_alerts"
    const val CHANNEL_DIGEST = "daily_digest"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(CHANNEL_PRICES, "Price Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications about market price changes"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_WEATHER, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Weather warnings and forecasts"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_CROP, "Crop Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Crop disease and farming alerts"
                },
                NotificationChannel(CHANNEL_DIGEST, "Daily Digest", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Daily morning summary of prices and weather"
                }
            )
            manager.createNotificationChannels(channels)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt(),
        data: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Match the app logo used on the home/splash screens so every
        // notification shows Farmer Friend's own mark, not a generic system icon.
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
