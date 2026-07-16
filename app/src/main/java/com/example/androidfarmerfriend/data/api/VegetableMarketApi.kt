package com.example.androidfarmerfriend.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface VegetableMarketApi {

    @GET("api/dataapi/market/koyambedu/daywisedata")
    suspend fun getVegetablePrices(
        @Query("date") date: String
    ): VegetableMarketResponse<VegetableItem>

    @GET("api/dataapi/fruits/koyambedu/daywisedata")
    suspend fun getFruitPrices(
        @Query("date") date: String
    ): VegetableMarketResponse<FruitItem>

    @GET("api/dataapi/nonveg/tamilnadu/daywisedata")
    suspend fun getNonVegPrices(
        @Query("date") date: String
    ): VegetableMarketResponse<NonVegItem>

    @GET("api/dataapi/gold/chennai/daywisedata")
    suspend fun getGoldPrices(
        @Query("date") date: String
    ): VegetableMarketResponse<GoldItem>
}
