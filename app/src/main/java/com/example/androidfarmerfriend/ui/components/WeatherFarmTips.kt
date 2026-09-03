package com.example.androidfarmerfriend.ui.components

import com.example.androidfarmerfriend.data.model.WeatherInfo

/**
 * Generates a context-aware farm tip based on current weather conditions.
 * Returns a data-driven tip when weather data is available, or null to
 * signal that the caller should fall back to the static localized tip.
 */
object WeatherFarmTips {

    /**
     * Pick the most actionable tip for the current conditions.
     * Priority: severe weather > rain > wind > humidity > heat/cold > generic.
     */
    fun tipFor(weather: WeatherInfo): String? {
        val rain = weather.rainChance.toDoubleOrNull() ?: 0.0
        val wind = weather.windSpeed.toDoubleOrNull() ?: 0.0
        val humidity = weather.humidity.toDoubleOrNull() ?: 0.0
        val temp = weather.temperature.toDoubleOrNull() ?: 0.0

        return when {
            // Heavy rain expected
            rain > 70 -> RAIN_HIGH
            // Strong winds — no spraying
            wind > 30 -> WIND_HIGH
            // High humidity — fungal risk
            humidity > 85 -> HUMIDITY_HIGH
            // Heavy rain likely + wet conditions
            rain > 50 && humidity > 70 -> RAIN_MODERATE
            // Extreme heat
            temp >= 40 -> HEAT_EXTREME
            // Heat wave
            temp >= 35 -> HEAT_MODERATE
            // Cold conditions
            temp <= 10 -> COLD_WAVE
            // Moderate wind — spray caution
            wind > 20 -> WIND_MODERATE
            // Mild conditions — generic positive tip
            else -> null
        }
    }

    private const val RAIN_HIGH =
        "Heavy rain expected. Ensure field drainage is clear and postpone spraying. " +
        "Harvest ready crops before rainfall begins."

    private const val RAIN_MODERATE =
        "Rain likely today. Delay irrigation and avoid applying fertilizers " +
        "that could wash away. Plan indoor tasks instead."

    private const val WIND_HIGH =
        "Strong winds forecasted. Avoid spraying pesticides or fertilizers — " +
        "drift will reduce effectiveness. Secure loose structures and young plants."

    private const val WIND_MODERATE =
        "Moderate winds today. Spray early morning when wind is calmest " +
        "for best pesticide coverage."

    private const val HUMIDITY_HIGH =
        "High humidity increases fungal disease risk. Inspect crops for " +
        "early signs and ensure good air circulation between rows."

    private const val HEAT_EXTREME =
        "Extreme heat warning. Irrigate crops during early morning or late evening. " +
        "Provide shade for livestock and sensitive seedlings."

    private const val HEAT_MODERATE =
        "Hot conditions today. Water crops early morning to reduce evaporation. " +
        "Mulch around plants to retain soil moisture."

    private const val COLD_WAVE =
        "Cold conditions detected. Cover sensitive crops with protective sheets. " +
        "Delay transplanting seedlings until temperatures rise."
}
