package com.example.androidfarmerfriend.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface EggRatesApi {

    @GET("api/egg-prices")
    suspend fun getEggRates(
        @Query("month") month: String,
        @Query("year") year: String,
        @Query("type") type: String = "Daily Rate Sheet"
    ): List<NcecEggPriceItem>
}
