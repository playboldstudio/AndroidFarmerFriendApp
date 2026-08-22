package com.example.androidfarmerfriend.util

import com.example.androidfarmerfriend.data.localization.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Compact relative timestamp for alert rows ("just now", "5 min ago", …). */
fun relativeTimeLabel(timestamp: Long, strings: AppStrings): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = diff / 3600000
    val days = diff / 86400000

    return when {
        minutes < 1 -> strings.timeJustNow
        minutes < 60 -> strings.timeMinutesAgo.format(minutes)
        hours < 24 -> strings.timeHoursAgo.format(hours)
        days < 7 -> strings.timeDaysAgo.format(days)
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
