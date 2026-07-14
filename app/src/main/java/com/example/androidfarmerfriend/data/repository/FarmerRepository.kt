package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FarmerRepository {
    private val api = ApiClient.api

    suspend fun getWeather(lat: Double = 13.0827, lon: Double = 80.2707): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val response = api.getWeather(lat, lon)
            if (response.success && response.data != null) {
                val w = response.data
                WeatherInfo(
                    temperature = "${w.temperature?.toInt() ?: 32}°C",
                    condition = w.condition ?: "Partly Cloudy",
                    humidity = "${w.humidity?.toInt() ?: 65}%",
                    windSpeed = "${w.windSpeed?.toInt() ?: 12} km/h",
                    rainChance = "${w.rainProbability?.toInt() ?: 20}%",
                    location = "Namakkal, Tamil Nadu",
                    feelsLike = "${w.feelsLike?.toInt() ?: 30}°C",
                    visibility = "${w.visibility?.toInt() ?: 10} km"
                )
            } else {
                getWeatherFallback()
            }
        } catch (e: Exception) {
            getWeatherFallback()
        }
    }

    suspend fun getMarketPrices(location: String = "chennai", productType: String? = null): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLatestPrices(location, productType)
            if (response.success && response.data?.prices != null) {
                response.data.prices!!.mapNotNull { it.toCrop() }
            } else {
                getCropsFallback()
            }
        } catch (e: Exception) {
            getCropsFallback()
        }
    }

    private fun getWeatherFallback(): WeatherInfo = WeatherInfo(
        temperature = "32°C",
        condition = "Partly Cloudy",
        humidity = "65%",
        windSpeed = "12 km/h",
        windDirection = "SW",
        rainChance = "20%",
        location = "Namakkal, Tamil Nadu"
    )

    private fun getCropsFallback(): List<Crop> = listOf(
        Crop(1, "தக்காளி", "Tomato", "₹28 / kg", 28.0, 4.2, "vegetable"),
        Crop(2, "வெங்காயம்", "Onion", "₹22 / kg", 22.0, -1.3, "vegetable"),
        Crop(3, "மிளகாய்", "Chilli", "₹60 / kg", 60.0, 2.1, "vegetable"),
        Crop(4, "உருளைக்கிழங்கு", "Potato", "₹18 / kg", 18.0, -0.5, "vegetable"),
        Crop(5, "கத்தரிக்காய்", "Eggplant", "₹32 / kg", 32.0, 1.8, "vegetable")
    )

    fun getAlerts(): List<Alert> = listOf(
        Alert(1, "விலை அலர்ட்", "தக்காளி விலை ₹30/kg ஆக உயர்ந்துள்ளது", "2 மணி நேரத்திற்கு முன்", AlertType.PRICE),
        Alert(2, "வானிலை அலர்ட்", "நாளை கனமழைக்கு வாய்ப்பு - Namakkal மாவட்டத்தில்", "1 மணி நேரத்திற்கு முன்", AlertType.WEATHER),
        Alert(3, "பயிர் அலர்ட்", "இலை கருகல் நோய் பரவ வாய்ப்பு - தக்காளி பயிரில் கவனம் தேவை", "2 மணி நேரத்திற்கு முன்", AlertType.CROP)
    )

    fun getSchemes(): List<Scheme> = listOf(
        Scheme(1, "பிரதான் மந்திரி கிசான் திட்டம்", "சிறு மற்றும் குறு விவசாயிகளுக்கு ஆண்டுக்கு ரூ. 6000", "மத்திய அரசு"),
        Scheme(2, "உழவர் காப்பீட்டு திட்டம்", "விவசாயிகளுக்கான பயிர் இழப்பு ஈடு செய்யும் திட்டம்", "மாநில அரசு"),
        Scheme(3, "பயண உதவி திட்டம்", "விவசாயப் பொருட்களை சந்தைக்கு கொண்டு செல்ல மானியம்", "மாநில அரசு"),
        Scheme(4, "விதை மானியம் திட்டம்", "உயர் விளைச்சல் ரக விதைகள் மானிய விலையில்", "மத்திய அரசு")
    )

    fun getDiseases(): List<Disease> = listOf(
        Disease(1, "இலை கருகல் நோய்", "தக்காளி"),
        Disease(2, "பூஞ்சை நோய்", "வெங்காயம்"),
        Disease(3, "பழு சிதைவு", "மிளகாய்"),
        Disease(4, "வேர் அழுகல்", "நிலக்கடலை"),
        Disease(5, "மஞ்சள் வைரஸ்", "பயறு")
    )

    private fun com.example.androidfarmerfriend.data.api.MarketPriceDto.toCrop(): Crop? {
        val nameEng = productNameEng ?: return null
        val priceVal = when (val p = price) {
            is Double -> p
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        val prevVal = when (val p = prevPrice) {
            is Double -> p
            is String -> p.toDoubleOrNull()
            is Number -> p.toDouble()
            else -> null
        }
        val diffVal = when (val d = priceDiff) {
            is Double -> d
            is String -> d.toDoubleOrNull()
            is Number -> d.toDouble()
            else -> null
        }
        val diffPctVal = when (val d = priceDiffPercent) {
            is Double -> d
            is String -> d.toDoubleOrNull()
            is Number -> d.toDouble()
            else -> null
        }
        val iconUrl = imageUrl ?: localImageUrl ?: ""
        val fullImageUrl = if (iconUrl.startsWith("/")) "https://farmerbackend-zz45.onrender.com$iconUrl" else iconUrl

        return Crop(
            id = productId?.hashCode() ?: nameEng.hashCode(),
            name = productNameTam ?: nameEng,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = diffPctVal ?: 0.0,
            category = productType ?: "",
            units = units ?: "kg",
            imageUrl = fullImageUrl,
            prevPrice = prevVal,
            priceDiff = diffVal,
            priceDiffPercent = diffPctVal
        )
    }
}
