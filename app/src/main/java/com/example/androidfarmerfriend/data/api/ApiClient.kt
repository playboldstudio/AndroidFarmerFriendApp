package com.example.androidfarmerfriend.data.api

import android.content.Context
import com.example.androidfarmerfriend.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ApiClient {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient by lazy {
        val cacheDir = appContext?.let { File(it.cacheDir, "http_cache") }
        val cache = cacheDir?.let { Cache(it, 10L * 1024 * 1024) } // 10MB

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
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
