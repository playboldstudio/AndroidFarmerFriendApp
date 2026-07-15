package com.example.androidfarmerfriend.data.scraper

import com.example.androidfarmerfriend.data.model.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object WebDataScraper {

    private const val WIKI_API = "https://ta.wikipedia.org/api/rest_v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun fetchCropNotes(): List<CropNote> = withContext(Dispatchers.IO) {
        val results = searchWikipedia("விவசாயம்", 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            val title = page.title ?: "குறிப்பு ${index + 1}"
            CropNote(
                id = index + 1,
                cropName = title,
                title = title,
                content = page.extract ?: page.description ?: "",
                sourceUrl = page.contentUrls?.desktop?.page ?: ""
            )
        }
    }

    suspend fun fetchDiseases(): List<Disease> = withContext(Dispatchers.IO) {
        val results = searchWikipedia("தாவர நோய்", 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            Disease(
                id = index + 1,
                name = page.title ?: "நோய் ${index + 1}",
                cropAffected = page.description ?: "பொது",
                sourceUrl = page.contentUrls?.desktop?.page ?: ""
            )
        }
    }

    suspend fun fetchSchemes(): List<Scheme> = withContext(Dispatchers.IO) {
        val results = searchWikipedia("விவசாய திட்டம்", 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            Scheme(
                id = index + 1,
                title = page.title ?: "திட்டம் ${index + 1}",
                description = page.extract ?: page.description ?: "",
                category = "மத்திய அரசு",
                sourceUrl = page.contentUrls?.desktop?.page ?: ""
            )
        }
    }

    suspend fun getPageSummary(title: String): WikipediaPage? = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(title, "UTF-8")
        val url = "$WIKI_API/page/summary/$encoded"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null
        val body = response.body?.string() ?: return@withContext null
        try {
            gson.fromJson(body, WikipediaPage::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun searchWikipedia(query: String, limit: Int): List<WikipediaPage> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "$WIKI_API/search/title?q=$encoded&limit=$limit"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()
        val body = response.body?.string() ?: return emptyList()
        return try {
            val searchResult = gson.fromJson(body, WikipediaSearchResult::class.java)
            searchResult.pages ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class WikipediaSearchResult(
    val pages: List<WikipediaPage>? = null
)

data class WikipediaPage(
    val id: Long? = null,
    val key: String? = null,
    val title: String? = null,
    val description: String? = null,
    val extract: String? = null,
    @SerializedName("content_urls")
    val contentUrls: WikipediaContentUrls? = null
)

data class WikipediaContentUrls(
    val desktop: WikipediaPageUrl? = null,
    val mobile: WikipediaPageUrl? = null
)

data class WikipediaPageUrl(
    val page: String? = null
)
