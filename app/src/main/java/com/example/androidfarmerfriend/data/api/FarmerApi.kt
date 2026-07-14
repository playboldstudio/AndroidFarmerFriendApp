package com.example.androidfarmerfriend.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface FarmerApi {

    @GET("api/prices/latest")
    suspend fun getLatestPrices(
        @Query("location") location: String = "chennai",
        @Query("productType") productType: String? = null,
        @Query("limit") limit: Int = 50
    ): ApiResponse<LatestPricesResponse>

    @GET("api/prices/vegetables")
    suspend fun getVegetablePrices(
        @Query("location") location: String = "chennai"
    ): ApiResponse<PriceListResponse>

    @GET("api/prices/fruits")
    suspend fun getFruitPrices(
        @Query("location") location: String = "chennai"
    ): ApiResponse<PriceListResponse>

    @GET("api/prices/nonveg")
    suspend fun getNonVegPrices(
        @Query("location") location: String = "chennai"
    ): ApiResponse<PriceListResponse>

    @GET("api/prices/gold")
    suspend fun getGoldPrices(
        @Query("location") location: String = "chennai"
    ): ApiResponse<PriceListResponse>

    @GET("api/egg-prices/latest")
    suspend fun getLatestEggPrices(
        @Query("location") location: String = "chennai"
    ): ApiResponse<EggPricesResponse>

    @GET("api/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double = 13.0827,
        @Query("lon") lon: Double = 80.2707
    ): ApiResponse<WeatherResponse>
}
