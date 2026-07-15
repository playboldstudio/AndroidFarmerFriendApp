package com.example.androidfarmerfriend.util

object WeatherCodeMapper {

    fun conditionTamil(code: Int): String = when (code) {
        0 -> "தெளிவான வானம்"
        1 -> "பெரும்பாலும் தெளிவு"
        2 -> "ஓரளவு மேகமூட்டம்"
        3 -> "மேகமூட்டம்"
        45, 48 -> "மூடுபனி"
        51, 53, 55 -> "தூறல்"
        56, 57 -> "உறை தூறல்"
        61, 63, 65 -> "மழை"
        66, 67 -> "உறை மழை"
        71, 73, 75 -> "பனிப்பொழிவு"
        77 -> "பனித்துகள்"
        80, 81, 82 -> "மழை பொழிவு"
        85, 86 -> "பனி பொழிவு"
        95 -> "இடி மழை"
        96, 99 -> "ஆலங்கட்டி மழை"
        else -> "வானிலை"
    }

    fun windDirection(degrees: Double): String {
        val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees % 360) / 45.0).toInt() % 8
        return dirs[index]
    }

    fun dayLabelTamil(date: String, index: Int): String {
        if (index == 0) return "இன்று"
        if (index == 1) return "நாளை"
        return try {
            val parts = date.split("-")
            val day = parts.getOrNull(2)?.toIntOrNull() ?: return date
            val month = parts.getOrNull(1)?.toIntOrNull() ?: return date
            val months = listOf(
                "ஜன", "பிப்", "மார்", "ஏப்", "மே", "ஜூன்",
                "ஜூலை", "ஆக", "செப்", "அக்", "நவ", "டிச"
            )
            "$day ${months.getOrElse(month - 1) { "" }}"
        } catch (e: Exception) {
            date
        }
    }
}
