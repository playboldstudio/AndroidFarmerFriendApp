package com.example.androidfarmerfriend.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val openMeteoRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.OPEN_METEO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val geocodingRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.GEOCODING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val vegetableMarketRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.VEGETABLE_MARKET_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val eggRatesRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.EGG_RATES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FarmerApi by lazy {
        retrofit.create(FarmerApi::class.java)
    }

    val weatherApi: OpenMeteoApi by lazy {
        openMeteoRetrofit.create(OpenMeteoApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        geocodingRetrofit.create(GeocodingApi::class.java)
    }

    val vegetableMarketApi: VegetableMarketApi by lazy {
        vegetableMarketRetrofit.create(VegetableMarketApi::class.java)
    }

    val eggRatesApi: EggRatesApi by lazy {
        eggRatesRetrofit.create(EggRatesApi::class.java)
    }
}
