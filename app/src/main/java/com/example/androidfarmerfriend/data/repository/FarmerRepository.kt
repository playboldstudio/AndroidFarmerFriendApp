package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.model.*
import com.example.androidfarmerfriend.data.scraper.WebDataScraper
import com.example.androidfarmerfriend.util.WeatherCodeMapper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmerRepository {
    private val weatherApi = ApiClient.weatherApi
    private val vegetableMarketApi = ApiClient.vegetableMarketApi
    private val eggRatesApi = ApiClient.eggRatesApi
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private fun todayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    suspend fun getWeather(lat: Double, lon: Double, locationName: String = "Namakkal, Tamil Nadu"): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
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
                location = locationName,
                feelsLike = "${current.feelsLike?.toInt() ?: 0}°C",
                todayHigh = todayHigh?.let { "$it°" } ?: "",
                todayLow = todayLow?.let { "$it°" } ?: "",
                weatherCode = code,
                forecast = forecast
            )
        } catch (e: Exception) {
            crashlytics.recordException(e)
            null
        }
    }

    suspend fun getVegetablePrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getVegetablePrices(todayDate())
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun getFruitPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getFruitPrices(todayDate())
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun getNonVegPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getNonVegPrices(todayDate())
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun getGoldPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getGoldPrices(todayDate())
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun getEggPrices(location: String = "chennai"): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val now = java.util.Calendar.getInstance()
            val month = String.format("%02d", now.get(java.util.Calendar.MONTH) + 1)
            val year = now.get(java.util.Calendar.YEAR).toString()
            val response = eggRatesApi.getEggRates(month = month, year = year)
            val targetCity = location.replaceFirstChar { it.uppercase() }
            val eggData = response.find { it.city?.equals(targetCity, ignoreCase = true) == true }
                ?: response.find { it.city?.equals("Chennai", ignoreCase = true) == true }
                ?: response.firstOrNull()
            eggData?.let { listOf(it.toCrop()) } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun searchLocations(query: String): List<SelectedLocation> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()
        try {
            val response = ApiClient.geocodingApi.search(query)
            response.results?.mapNotNull { result ->
                val name = result.name ?: return@mapNotNull null
                val lat = result.latitude ?: return@mapNotNull null
                val lon = result.longitude ?: return@mapNotNull null
                val region = listOfNotNull(result.admin1, result.country).joinToString(", ")
                SelectedLocation(
                    name = if (region.isNotEmpty()) "$name, $region" else name,
                    lat = lat,
                    lon = lon
                )
            } ?: emptyList()
        } catch (e: Exception) {
            crashlytics.recordException(e)
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

    private fun com.example.androidfarmerfriend.data.api.VegetableItem.toCrop(): Crop? {
        val rawName = vegetablename ?: columnNameEng ?: return null
        val nameEng = columnNameEng ?: rawName
        val name = rawName
        val priceVal = when (val p = price) {
            is Double -> p
            is Int -> p.toDouble()
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "vegetable",
            units = units ?: "kg"
        )
    }

    private fun com.example.androidfarmerfriend.data.api.FruitItem.toCrop(): Crop? {
        val rawName = fruitname ?: columnNameEng ?: return null
        val nameEng = columnNameEng ?: rawName
        val name = rawName
        val priceVal = when (val p = price) {
            is Double -> p
            is Int -> p.toDouble()
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "fruit",
            units = units ?: "kg"
        )
    }

    private fun com.example.androidfarmerfriend.data.api.NonVegItem.toCrop(): Crop? {
        val rawName = nonvegname ?: columnNameEng ?: return null
        val nameEng = columnNameEng ?: rawName
        val name = rawName
        val priceVal = when (val p = price) {
            is Double -> p
            is Int -> p.toDouble()
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "nonveg",
            units = units ?: "kg"
        )
    }

    private fun com.example.androidfarmerfriend.data.api.GoldItem.toCrop(): Crop? {
        val rawName = name ?: columnNameEng ?: return null
        val nameEng = columnNameEng ?: rawName
        val name = rawName
        val priceVal = when (val p = price) {
            is Double -> p
            is Int -> p.toDouble()
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "gm"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "gold",
            units = units ?: "gm"
        )
    }

    private fun com.example.androidfarmerfriend.data.api.NcecEggPriceItem.toCrop(): Crop {
        val priceVal = when (val p = price) {
            is Double -> p
            is Int -> p.toDouble()
            is String -> p.toDoubleOrNull() ?: 0.0
            is Number -> p.toDouble()
            else -> 0.0
        }
        val cityName = city ?: "Chennai"
        return Crop(
            id = "egg_$cityName".hashCode(),
            name = "முட்டை",
            nameEng = "Egg",
            price = "₹${"%.2f".format(priceVal)} / piece",
            priceValue = priceVal,
            trend = 0.0,
            category = "egg",
            units = "piece"
        )
    }
}
