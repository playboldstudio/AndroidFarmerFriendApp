package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.model.*
import com.example.androidfarmerfriend.data.scraper.WebDataScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FarmerRepository {
    private val api = ApiClient.api

    suspend fun getWeather(lat: Double = 13.0827, lon: Double = 80.2707): WeatherInfo? = withContext(Dispatchers.IO) {
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
            throw Exception(response.message ?: "Failed to load weather data")
        }
    }

    suspend fun getMarketPrices(location: String = "chennai", productType: String? = null): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getLatestPrices(location, productType)
        if (response.success && response.data?.prices != null) {
            response.data.prices!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

    suspend fun getVegetablePrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getVegetablePrices(location)
        if (response.success && response.data?.data != null) {
            response.data.data!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

    suspend fun getFruitPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getFruitPrices(location)
        if (response.success && response.data?.data != null) {
            response.data.data!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

    suspend fun getNonVegPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getNonVegPrices(location)
        if (response.success && response.data?.data != null) {
            response.data.data!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

    suspend fun getGoldPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getGoldPrices(location)
        if (response.success && response.data?.data != null) {
            response.data.data!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

    suspend fun getEggPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        val response = api.getLatestEggPrices(location)
        if (response.success && response.data?.prices != null) {
            response.data.prices!!.mapNotNull { it.toCrop() }
        } else {
            emptyList()
        }
    }

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
            prevPrice = prevPrice?.let {
                when (it) { is Double -> it; is Number -> it.toDouble(); else -> null }
            },
            priceDiff = priceDiff?.let {
                when (it) { is Double -> it; is Number -> it.toDouble(); else -> null }
            },
            priceDiffPercent = diffPctVal
        )
    }
}
