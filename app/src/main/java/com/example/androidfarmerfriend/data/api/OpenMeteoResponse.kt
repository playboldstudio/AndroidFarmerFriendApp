package com.example.androidfarmerfriend.data.api

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val current: OpenMeteoCurrent?,
    val daily: OpenMeteoDaily?
)

data class OpenMeteoCurrent(
    val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Double?,
    @SerializedName("apparent_temperature") val feelsLike: Double?,
    val precipitation: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Double?
)

data class OpenMeteoDaily(
    val time: List<String>?,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>?,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?
)
