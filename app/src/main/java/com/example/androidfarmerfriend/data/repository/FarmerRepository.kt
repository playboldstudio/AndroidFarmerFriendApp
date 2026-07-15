package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.model.*
import com.example.androidfarmerfriend.data.scraper.WebDataScraper
import com.example.androidfarmerfriend.util.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FarmerRepository {
    private val api = ApiClient.api
    private val weatherApi = ApiClient.weatherApi

    suspend fun getWeather(lat: Double = 11.2189, lon: Double = 78.1674): WeatherInfo? = withContext(Dispatchers.IO) {
        val response = weatherApi.getForecast(latitude = lat, longitude = lon)
        val current = response.current ?: throw Exception("Failed to load weather data")
        val daily = response.daily

        val forecast = buildList {
            val times = daily?.time ?: emptyList()
            times.forEachIndexed { index, date ->
                add(
                    ForecastDay(
                        day = WeatherCodeMapper.dayLabelTamil(date, index),
                        maxTemp = "${daily?.tempMax?.getOrNull(index)?.toInt() ?: 0}°",
                        minTemp = "${daily?.tempMin?.getOrNull(index)?.toInt() ?: 0}°",
                        weatherCode = daily?.weatherCode?.getOrNull(index) ?: 0
                    )
                )
            }
        }

        val code = current.weatherCode ?: 0
        val todayHigh = daily?.tempMax?.getOrNull(0)?.toInt()
        val todayLow = daily?.tempMin?.getOrNull(0)?.toInt()
        val rainProb = daily?.precipitationProbabilityMax?.getOrNull(0)

        WeatherInfo(
            temperature = "${current.temperature?.toInt() ?: 0}°C",
            condition = WeatherCodeMapper.conditionTamil(code),
            humidity = "${current.humidity?.toInt() ?: 0}%",
            windSpeed = "${current.windSpeed?.toInt() ?: 0} km/h",
            windDirection = WeatherCodeMapper.windDirection(current.windDirection ?: 0.0),
            rainChance = "${rainProb ?: 0}%",
            location = "Namakkal, Tamil Nadu",
            feelsLike = "${current.feelsLike?.toInt() ?: 0}°C",
            todayHigh = todayHigh?.let { "$it°" } ?: "",
            todayLow = todayLow?.let { "$it°" } ?: "",
            weatherCode = code,
            forecast = forecast
        )
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
