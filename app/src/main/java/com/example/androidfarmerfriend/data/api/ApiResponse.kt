package com.example.androidfarmerfriend.data.api

import com.google.gson.annotations.SerializedName

// Generic wrapper matching backend: { success, message, data }
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

// --- VegetableMarketPrice.com API DTOs ---

data class VegetableMarketResponse<T>(
    val startDateLimit: String?,
    val date: String?,
    val dataHeaders: List<DataHeader>?,
    val data: List<T>?
)

data class DataHeader(
    val headerName: String?,
    val headerType: String?,
    val headerValue: String?,
    val orderId: Int?
)

data class VegetableItem(
    val columnNameEng: String?,
    val vegetablename: String?,
    val price: Any?,
    val retailprice: Any?,
    val units: String?,
    val id: String?
)

data class FruitItem(
    val columnNameEng: String?,
    val fruitname: String?,
    val price: Any?,
    val retailprice: Any?,
    val units: String?,
    val id: String?
)

data class NonVegItem(
    val columnNameEng: String?,
    val nonvegname: String?,
    val price: Any?,
    val units: String?,
    val id: String?
)

data class GoldItem(
    val columnNameEng: String?,
    val name: String?,
    val price: Any?,
    val units: String?,
    val id: String?
)

// --- NECC Egg Price API DTOs ---

data class NcecEggPriceItem(
    val city: String?,
    val price: Any?,
    val avg: Any?
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

data class EggPricesResponse(
    val location: String?,
    val count: Int?,
    val prices: List<EggPriceDto>?
)

data class EggPriceDto(
    val id: String?,
    val eggType: String?,
    val location: String?,
    val price: Any?,
    val retailPrice: Any?,
    val units: String?,
    val gradeType: String?,
    val date: String?,
    val prevPrice: Any?,
    val priceDiff: Any?,
    val priceDiffPercent: Any?
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
