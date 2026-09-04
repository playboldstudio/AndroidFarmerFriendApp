package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.api.ApiClient
import com.example.androidfarmerfriend.data.api.ItemImageTable
import com.example.androidfarmerfriend.data.api.NetworkErrors
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.model.*
import com.example.androidfarmerfriend.data.scraper.WebDataScraper
import com.example.androidfarmerfriend.util.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class FarmerRepository {
    private val weatherApi = ApiClient.weatherApi
    private val vegetableMarketApi = ApiClient.vegetableMarketApi
    private val eggRatesApi = ApiClient.eggRatesApi

    private fun todayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    suspend fun getWeather(lat: Double, lon: Double, locationName: String = "Namakkal, Tamil Nadu", strings: AppStrings = AppStrings.English): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getForecast(latitude = lat, longitude = lon)
            val current = response.current ?: throw Exception("Failed to load weather data")
            val daily = response.daily

            val forecast = buildList {
                val times = daily?.time ?: emptyList()
                times.forEachIndexed { index, date ->
                    add(
                        ForecastDay(
                            day = WeatherCodeMapper.dayLabel(date, index, strings),
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

            // Hourly strip: take the next ~8 hours from now so farmers see
            // what's coming today (spraying / irrigation windows).
            val hourly = buildHourly(response.hourly)
            val sunrise = daily?.sunrise?.getOrNull(0)?.timeOfDay() ?: ""
            val sunset = daily?.sunset?.getOrNull(0)?.timeOfDay() ?: ""

            WeatherInfo(
                temperature = "${current.temperature?.toInt() ?: 0}°C",
                condition = WeatherCodeMapper.condition(code, strings),
                humidity = "${current.humidity?.toInt() ?: 0}%",
                windSpeed = "${current.windSpeed?.toInt() ?: 0} km/h",
                windDirection = WeatherCodeMapper.windDirection(current.windDirection ?: 0.0),
                rainChance = "${rainProb ?: 0}%",
                location = locationName,
                feelsLike = "${current.feelsLike?.toInt() ?: 0}°C",
                todayHigh = todayHigh?.let { "$it°" } ?: "",
                todayLow = todayLow?.let { "$it°" } ?: "",
                weatherCode = code,
                forecast = forecast,
                hourly = hourly,
                sunrise = sunrise,
                sunset = sunset
            )
        } catch (e: Exception) {
            NetworkErrors.record("getWeather", e)
            null
        }
    }

    suspend fun getVegetablePrices(location: String = "koyambedu", date: String = todayDate()): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getVegetablePrices(location, date)
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            NetworkErrors.record("getVegetablePrices", e)
            emptyList()
        }
    }

    suspend fun getFruitPrices(location: String = "koyambedu", date: String = todayDate()): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getFruitPrices(location, date)
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            NetworkErrors.record("getFruitPrices", e)
            emptyList()
        }
    }

    suspend fun getNonVegPrices(location: String = "bangalore", date: String = todayDate()): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getNonVegPrices(location, date)
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            NetworkErrors.record("getNonVegPrices", e)
            emptyList()
        }
    }

    suspend fun getGoldPrices(location: String = "chennai", date: String = todayDate()): List<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = vegetableMarketApi.getGoldPrices(location, date)
            response.data?.mapNotNull { it.toCrop() } ?: emptyList()
        } catch (e: Exception) {
            NetworkErrors.record("getGoldPrices", e)
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
            NetworkErrors.record("getEggPrices", e)
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
            NetworkErrors.record("searchLocations", e)
            emptyList()
        }
    }

    // --- Wikipedia-backed reference lists ---

    suspend fun getSchemes(language: Language = Language.ENGLISH): List<Scheme> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchSchemes(language)
    }

    suspend fun getCropNotes(language: Language = Language.ENGLISH): List<CropNote> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchCropNotes(language)
    }

    suspend fun getDiseases(language: Language = Language.ENGLISH): List<Disease> = withContext(Dispatchers.IO) {
        WebDataScraper.fetchDiseases(language)
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
        val retail = retailprice?.toString() ?: ""
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "vegetable",
            units = units ?: "kg",
            retailPrice = retail,
            imageUrl = table?.imageUrl?.let { ItemImageTable.BASE_URL + it }.orEmpty()
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
        val retail = retailprice?.toString() ?: ""
        return Crop(
            id = id?.hashCode() ?: nameEng.hashCode(),
            name = name,
            nameEng = nameEng,
            price = "₹${"%.0f".format(priceVal)} / ${units ?: "kg"}",
            priceValue = priceVal,
            trend = 0.0,
            category = "fruit",
            units = units ?: "kg",
            retailPrice = retail
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
        val avgVal = when (val a = avg) {
            is Double -> a
            is Int -> a.toDouble()
            is String -> a.toDoubleOrNull()
            is Number -> a.toDouble()
            else -> null
        }
        val diffPercent = if (avgVal != null && avgVal > 0.0 && priceVal > 0.0) {
            ((priceVal - avgVal) / avgVal) * 100.0
        } else {
            null
        }
        val cityName = city ?: "Chennai"
        return Crop(
            id = "egg_$cityName".hashCode(),
            name = "Egg",
            nameEng = "Egg",
            price = "₹${"%.2f".format(priceVal)} / piece",
            priceValue = priceVal,
            trend = 0.0,
            category = "egg",
            units = "piece",
            prevPrice = avgVal,
            priceDiffPercent = diffPercent
        )
    }

    // ---- Hourly mapping ----

    private fun buildHourly(hourly: com.example.androidfarmerfriend.data.api.OpenMeteoHourly?): List<HourlyForecast> {
        if (hourly == null) return emptyList()
        val times = hourly.time ?: return emptyList()
        val temps = hourly.temperature ?: return emptyList()
        val codes = hourly.weatherCode ?: emptyList()
        val rain = hourly.precipitationProbability ?: emptyList()

        val nowHour = LocalTime.now().hour
        // Find the index of the current (or nearest past) hour and take the next 8.
        val startIndex = times.indexOfFirst { entry ->
            val h = entry.timeOrNull()?.hour ?: return@indexOfFirst false
            h >= nowHour
        }.coerceAtLeast(0)

        return (startIndex until minOf(startIndex + 8, times.size)).mapNotNull { i ->
            val timeStr = times.getOrNull(i) ?: return@mapNotNull null
            val hour = timeStr.timeOrNull() ?: return@mapNotNull null
            HourlyForecast(
                hourLabel = hour.format(DateTimeFormatter.ofPattern("HH:mm")),
                temp = "${temps.getOrNull(i)?.toInt() ?: 0}°",
                weatherCode = codes.getOrNull(i) ?: 0,
                rainChance = rain.getOrNull(i)
            )
        }
    }

    /** Extract the time portion from an ISO datetime string like "2026-09-04T06:12". */
    private fun String.timeOrNull(): LocalTime? = try {
        val timePart = this.substringAfter('T')
        LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        null
    }

    /** Return just "HH:MM" from an ISO datetime string, for sunrise/sunset display. */
    private fun String.timeOfDay(): String = timeOrNull()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
}
