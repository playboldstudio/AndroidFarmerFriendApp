package com.example.androidfarmerfriend.util

import com.example.androidfarmerfriend.data.localization.AppStrings

object WeatherCodeMapper {

    fun condition(code: Int, strings: AppStrings): String = when (code) {
        0 -> strings.clearSky
        1 -> strings.mostlyClear
        2 -> strings.partlyCloudy
        3 -> strings.overcast
        45, 48 -> strings.wFog
        51, 53, 55 -> strings.wDrizzle
        56, 57 -> strings.freezingDrizzle
        61, 63, 65 -> strings.wRain
        66, 67 -> strings.freezingRain
        71, 73, 75 -> strings.snowfall
        77 -> strings.snowGrains
        80, 81, 82 -> strings.rainShowers
        85, 86 -> strings.snowShowers
        95 -> strings.thunderstorm
        96, 99 -> strings.hailstorm
        else -> strings.wWeather
    }

    fun windDirection(degrees: Double): String {
        val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees % 360) / 45.0).toInt() % 8
        return dirs[index]
    }

    fun dayLabel(date: String, index: Int, strings: AppStrings): String {
        if (index == 0) return strings.today
        if (index == 1) return strings.tomorrow
        return try {
            val parts = date.split("-")
            val day = parts.getOrNull(2)?.toIntOrNull() ?: return date
            val month = parts.getOrNull(1)?.toIntOrNull() ?: return date
            val months = listOf(
                strings.monthJan, strings.monthFeb, strings.monthMar,
                strings.monthApr, strings.monthMay, strings.monthJun,
                strings.monthJul, strings.monthAug, strings.monthSep,
                strings.monthOct, strings.monthNov, strings.monthDec
            )
            "$day ${months.getOrElse(month - 1) { "" }}"
        } catch (e: Exception) {
            date
        }
    }
}
