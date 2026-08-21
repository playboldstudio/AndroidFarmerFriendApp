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
    val priceDiffPercent: Double? = null,
    val retailPrice: String = "",
    val avgPrice: Double? = null
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
    val visibility: String = "",
    val todayHigh: String = "",
    val todayLow: String = "",
    val weatherCode: Int = 0,
    val forecast: List<ForecastDay> = emptyList()
)

data class ForecastDay(
    val day: String,
    val maxTemp: String,
    val minTemp: String,
    val weatherCode: Int,
    val rainChance: String = ""
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
    val sourceUrl: String = "",
    val iconRes: Int? = null
)

data class Alert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: AlertType = AlertType.PRICE,
    val isRead: Boolean = false,
    val actionRoute: String = "",
    val location: String = ""
)

data class Disease(
    val id: Int,
    val name: String,
    val cropAffected: String,
    val sourceUrl: String = "",
    val imageUrl: String = ""
)

data class CropNote(
    val id: Int,
    val cropName: String,
    val title: String,
    val content: String,
    val sourceUrl: String = "",
    val season: String = ""
)

enum class AlertType {
    PRICE, WEATHER, CROP
}
