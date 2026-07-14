package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.model.*
import com.example.androidfarmerfriend.data.scraper.WebDataScraper
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

    suspend fun getVegetablePrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVegetablePrices(location)
            if (response.success && response.data?.data != null) {
                response.data.data!!.mapNotNull { it.toCrop() }
            } else {
                getCropsFallback().filter { it.category == "vegetable" }
            }
        } catch (e: Exception) {
            getCropsFallback().filter { it.category == "vegetable" }
        }
    }

    suspend fun getFruitPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFruitPrices(location)
            if (response.success && response.data?.data != null) {
                response.data.data!!.mapNotNull { it.toCrop() }
            } else {
                getCropsFallback().filter { it.category == "fruit" }
            }
        } catch (e: Exception) {
            getCropsFallback().filter { it.category == "fruit" }
        }
    }

    suspend fun getGoldPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGoldPrices(location)
            if (response.success && response.data?.data != null) {
                response.data.data!!.mapNotNull { it.toCrop() }
            } else {
                getGoldFallback()
            }
        } catch (e: Exception) {
            getGoldFallback()
        }
    }

    suspend fun getEggPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLatestEggPrices(location)
            if (response.success && response.data?.prices != null) {
                response.data.prices!!.mapNotNull { it.toCrop() }
            } else {
                getEggFallback()
            }
        } catch (e: Exception) {
            getEggFallback()
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

    private fun getGoldFallback(): List<Crop> = listOf(
        Crop(20, "Gold 24K (1g)", "Gold 24K (1g)", "₹7,200 / gram", 7200.0, 0.3, "gold", units = "gram"),
        Crop(21, "Gold 22K (1g)", "Gold 22K (1g)", "₹6,800 / gram", 6800.0, 0.2, "gold", units = "gram"),
        Crop(22, "Gold 18K (1g)", "Gold 18K (1g)", "₹5,400 / gram", 5400.0, -0.1, "gold", units = "gram")
    )

    private fun getEggFallback(): List<Crop> = listOf(
        Crop(6, "Chicken Egg", "Chicken Egg", "₹7 / piece", 7.0, 0.5, "egg", units = "piece"),
        Crop(7, "Chicken Egg (Tray)", "Chicken Egg Tray", "₹180 / tray", 180.0, -2.0, "egg", units = "tray (30 eggs)"),
        Crop(8, "Country Egg", "Country Egg", "₹10 / piece", 10.0, 1.2, "egg", units = "piece"),
        Crop(9, "Duck Egg", "Duck Egg", "₹12 / piece", 12.0, 0.8, "egg", units = "piece")
    )

    private fun getCropsFallback(): List<Crop> = listOf(
        Crop(1, "தக்காளி", "Tomato", "₹28 / kg", 28.0, 4.2, "vegetable"),
        Crop(2, "வெங்காயம்", "Onion", "₹22 / kg", 22.0, -1.3, "vegetable"),
        Crop(3, "மிளகாய்", "Chilli", "₹60 / kg", 60.0, 2.1, "vegetable"),
        Crop(4, "உருளைக்கிழங்கு", "Potato", "₹18 / kg", 18.0, -0.5, "vegetable"),
        Crop(5, "கத்தரிக்காய்", "Eggplant", "₹32 / kg", 32.0, 1.8, "vegetable"),
        Crop(10, "ஆப்பிள்", "Apple", "₹120 / kg", 120.0, 3.0, "fruit"),
        Crop(11, "வாழைப்பழம்", "Banana", "₹40 / dozen", 40.0, -1.0, "fruit"),
        Crop(12, "மாம்பழம்", "Mango", "₹80 / kg", 80.0, 5.5, "fruit"),
        Crop(13, "நெல்", "Paddy", "₹2,200 / quintal", 2200.0, 1.2, "grain"),
        Crop(14, "கோதுமை", "Wheat", "₹2,500 / quintal", 2500.0, -0.8, "grain")
    )

    fun getAlerts(): List<Alert> = listOf(
        Alert(1, "விலை அலர்ட்", "தக்காளி விலை ₹30/kg ஆக உயர்ந்துள்ளது", "2 மணி நேரத்திற்கு முன்", AlertType.PRICE),
        Alert(2, "வானிலை அலர்ட்", "நாளை கனமழைக்கு வாய்ப்பு - Namakkal மாவட்டத்தில்", "1 மணி நேரத்திற்கு முன்", AlertType.WEATHER),
        Alert(3, "பயிர் அலர்ட்", "இலை கருகல் நோய் பரவ வாய்ப்பு - தக்காளி பயிரில் கவனம் தேவை", "2 மணி நேரத்திற்கு முன்", AlertType.CROP)
    )

    suspend fun getSchemes(): List<Scheme> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchSchemes()
    }

    suspend fun getCropNotes(): List<CropNote> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchCropNotes()
    }

    suspend fun getDiseases(): List<Disease> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchDiseases()
    }

    private fun com.example.androidfarmerfriend.data.api.EggPriceDto.toCrop(): Crop? {
        val name = eggType ?: return null
        val priceVal = when (val p = price) {
            is Double -> p
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        val diffPctVal = when (val d = priceDiffPercent) {
            is Double -> d
            is String -> d.toDoubleOrNull()
            is Number -> d.toDouble()
            else -> null
        }
        return Crop(
            id = id?.hashCode() ?: name.hashCode(),
            name = name,
            nameEng = name,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "piece"}",
            priceValue = priceVal,
            trend = diffPctVal ?: 0.0,
            category = "egg",
            units = units ?: "piece"
        )
    }

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
