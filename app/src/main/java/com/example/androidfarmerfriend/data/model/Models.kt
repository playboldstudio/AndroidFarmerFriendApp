package com.example.androidfarmerfriend.data.model

data class Crop(
    val id: Int,
    val name: String,
    val nameEng: String = "",
    val price: String,
    val priceValue: Double = 0.0,
    val trend: Double,
    val category: String,
    val units: String = "kg",
    val imageUrl: String = "",
    val prevPrice: Double? = null,
    val priceDiff: Double? = null,
    val priceDiffPercent: Double? = null
)

data class WeatherInfo(
    val temperature: String,
    val condition: String,
    val humidity: String,
    val windSpeed: String,
    val windDirection: String = "SW",
    val rainChance: String,
    val location: String,
    val highLow: String = "35° / 25°",
    val feelsLike: String = "",
    val visibility: String = ""
)

data class ForecastDay(
    val day: String,
    val temp: String,
    val icon: String
)

data class QuickAction(
    val id: Int,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

data class Scheme(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val iconRes: Int? = null
)

data class Alert(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: AlertType
)

data class Disease(
    val id: Int,
    val name: String,
    val cropAffected: String,
    val imageUrl: String = ""
)

enum class AlertType {
    PRICE, WEATHER, CROP
}
