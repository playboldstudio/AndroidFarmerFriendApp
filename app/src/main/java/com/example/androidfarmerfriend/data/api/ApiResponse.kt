package com.example.androidfarmerfriend.data.api

import com.google.gson.annotations.SerializedName

// Generic wrapper matching backend: { success, message, data }
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class PriceListResponse(
    val total: Int?,
    val limit: Int?,
    val offset: Int?,
    val data: List<MarketPriceDto>?
)

data class LatestPricesResponse(
    val location: String?,
    val productType: String?,
    val count: Int?,
    val prices: List<MarketPriceDto>?
)

data class MarketPriceDto(
    val id: String?,
    val productType: String?,
    val productId: String?,
    val productNameEng: String?,
    val productNameTam: String?,
    val price: Any?,
    val retailPrice: Any?,
    val units: String?,
    val location: String?,
    val date: String?,
    val imageUrl: String?,
    val localImageUrl: String?,
    val prevPrice: Any?,
    val priceDiff: Any?,
    val priceDiffPercent: Any?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class WeatherResponse(
    val id: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val temperature: Double?,
    val feelsLike: Double?,
    val humidity: Double?,
    val pressure: Double?,
    val windSpeed: Double?,
    val cloudCoverage: Double?,
    val condition: String?,
    val description: String?,
    val icon: String?,
    val rainProbability: Double?,
    val visibility: Double?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("fetchedAt") val fetchedAt: String?
)
