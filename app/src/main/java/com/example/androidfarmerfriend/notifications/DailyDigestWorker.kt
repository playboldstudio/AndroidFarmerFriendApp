package com.example.androidfarmerfriend.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.androidfarmerfriend.data.repository.FirestoreAlertRepository
import com.example.androidfarmerfriend.data.model.AlertType

class DailyDigestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = FirestoreAlertRepository.getInstance()

            val recentAlerts = repository.getAlerts(limit = 10)
            val unreadCount = recentAlerts.count { !it.isRead }
            val priceAlerts = recentAlerts.count { it.type == AlertType.PRICE && !it.isRead }
            val weatherAlerts = recentAlerts.count { it.type == AlertType.WEATHER && !it.isRead }
            val cropAlerts = recentAlerts.count { it.type == AlertType.CROP && !it.isRead }

            if (unreadCount > 0) {
                val summary = buildString {
                    if (priceAlerts > 0) append("$priceAlerts price alert${if (priceAlerts > 1) "s" else ""}")
                    if (weatherAlerts > 0) {
                        if (isNotEmpty()) append(", ")
                        append("$weatherAlerts weather alert${if (weatherAlerts > 1) "s" else ""}")
                    }
                    if (cropAlerts > 0) {
                        if (isNotEmpty()) append(", ")
                        append("$cropAlerts crop alert${if (cropAlerts > 1) "s" else ""}")
                    }
                }

                NotificationHelper.showNotification(
                    context = applicationContext,
                    channelId = NotificationHelper.CHANNEL_DIGEST,
                    title = "Daily Digest",
                    message = "You have $unreadCount unread alerts: $summary",
                    notificationId = DAILY_DIGEST_ID
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val DAILY_DIGEST_ID = 9999
        const val WORK_NAME = "daily_digest"
    }
}
