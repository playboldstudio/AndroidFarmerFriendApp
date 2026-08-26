package com.example.androidfarmerfriend.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VegetableMarketApi {

    @GET("api/dataapi/market/{location}/daywisedata")
    suspend fun getVegetablePrices(
        @Path("location") location: String,
        @Query("date") date: String
    ): VegetableMarketResponse<VegetableItem>

    @GET("api/dataapi/fruits/{location}/daywisedata")
    suspend fun getFruitPrices(
        @Path("location") location: String,
        @Query("date") date: String
    ): VegetableMarketResponse<FruitItem>

    @GET("api/dataapi/nonveg/{location}/daywisedata")
    suspend fun getNonVegPrices(
        @Path("location") location: String,
        @Query("date") date: String
    ): VegetableMarketResponse<NonVegItem>

    @GET("api/dataapi/gold/{location}/daywisedata")
    suspend fun getGoldPrices(
        @Path("location") location: String,
        @Query("date") date: String
    ): VegetableMarketResponse<GoldItem>
}
