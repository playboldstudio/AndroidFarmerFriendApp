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

    private const val WIKI_HOST = "https://ta.wikipedia.org"
    private const val SEARCH_API = "$WIKI_HOST/w/rest.php/v1/search/page"

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
                content = cleanExcerpt(page.excerpt) ?: page.description ?: "",
                sourceUrl = pageUrl(page.key)
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
                cropAffected = page.description ?: cleanExcerpt(page.excerpt) ?: "பொது",
                sourceUrl = pageUrl(page.key)
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
                description = cleanExcerpt(page.excerpt) ?: page.description ?: "",
                category = "மத்திய அரசு",
                sourceUrl = pageUrl(page.key)
            )
        }
    }

    private fun searchWikipedia(query: String, limit: Int): List<WikipediaPage> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "$SEARCH_API?q=$encoded&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AndroidFarmerFriend/1.0 (contact: app@farmerfriend.example)")
                .build()
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) return@use emptyList()
                val body = it.body?.string() ?: return@use emptyList()
                try {
                    val searchResult = gson.fromJson(body, WikipediaSearchResult::class.java)
                    searchResult.pages ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun pageUrl(key: String?): String {
        if (key.isNullOrBlank()) return ""
        val encoded = URLEncoder.encode(key, "UTF-8").replace("+", "%20")
        return "$WIKI_HOST/wiki/$encoded"
    }

    private fun cleanExcerpt(excerpt: String?): String? {
        if (excerpt.isNullOrBlank()) return null
        return excerpt
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#039;", "'")
            .replace("&nbsp;", " ")
            .trim()
            .ifBlank { null }
    }
}

data class WikipediaSearchResult(
    val pages: List<WikipediaPage>? = null
)

data class WikipediaPage(
    val id: Long? = null,
    val key: String? = null,
    val title: String? = null,
    val excerpt: String? = null,
    val description: String? = null,
    @SerializedName("matched_title")
    val matchedTitle: String? = null
)
