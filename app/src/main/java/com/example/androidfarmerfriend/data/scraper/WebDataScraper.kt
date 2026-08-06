package com.example.androidfarmerfriend.data.scraper

import com.example.androidfarmerfriend.data.localization.Language
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

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // --- Language → Wikipedia subdomain ---
    private fun wikiHost(language: Language): String =
        "https://${language.code}.wikipedia.org"

    // --- Per-language search queries ---
    private data class SearchTerms(
        val agriculture: String,
        val plantDisease: String,
        val agriculturalScheme: String,
        val fallbackNote: String,
        val fallbackDisease: String,
        val fallbackScheme: String,
        val fallbackGeneral: String
    )

    private val searchTerms = mapOf(
        Language.TAMIL to SearchTerms(
            "விவசாயம்", "தாவர நோய்", "விவசாய திட்டம்",
            "குறிப்பு", "நோய்", "திட்டம்", "பொது"
        ),
        Language.ENGLISH to SearchTerms(
            "agriculture", "plant disease", "agricultural scheme",
            "Note", "Disease", "Scheme", "General"
        ),
        Language.HINDI to SearchTerms(
            "कृषि", "पौध रोग", "कृषि योजना",
            "नोट", "रोग", "योजना", "सामान्य"
        ),
        Language.TELUGU to SearchTerms(
            "వ్యవసాయం", "మొక్క వ్యాధి", "వ్యవసాయ పథకం",
            "గమనిక", "వ్యాధి", "పథకం", "సాధారణ"
        ),
        Language.MALAYALAM to SearchTerms(
            "കൃഷി", "സസ്യ രോഗം", "കൃഷി പദ്ധതി",
            "കുറിപ്പ്", "രോഗം", "പദ്ധതി", "സാധാരണ"
        ),
        Language.KANNADA to SearchTerms(
            "ಕೃಷಿ", "ಸಸ್ಯ ರೋಗ", "ಕೃಷಿ ಯೋಜನೆ",
            "ಸೂಚನೆ", "ರೋಗ", "ಯೋಜನೆ", "ಸಾಮಾನ್ಯ"
        ),
        Language.MARATHI to SearchTerms(
            "शेती", "वनस्पती रोग", "शेती योजना",
            "टीप", "रोग", "योजना", "सामान्य"
        ),
        Language.BENGALI to SearchTerms(
            "কৃষি", "উদ্ভিদ রোগ", "কৃষি প্রকল্প",
            "নোট", "রোগ", "প্রকল্প", "সাধারণ"
        ),
        Language.PUNJABI to SearchTerms(
            "ਖੇਤੀ", "ਬੂਟੇ ਦੀ ਬਿਮਾਰੀ", "ਖੇਤੀ ਯੋਜਨਾ",
            "ਨੋਟ", "ਬਿਮਾਰੀ", "ਯੋਜਨਾ", "ਆਮ"
        ),
        Language.GUJARATI to SearchTerms(
            "ખેતી", "છોડ રોગ", "ખેતી યોજના",
            "નોંધ", "રોગ", "યોજના", "સામાન્ય"
        ),
        Language.ODIA to SearchTerms(
            "କୃଷି", "ଉଦ୍ଭିଦ ରୋଗ", "କୃଷି ଯୋଜନା",
            "ନୋଟ", "ରୋଗ", "ଯୋଜନା", "ସାଧାରଣ"
        )
    )

    private fun terms(language: Language): SearchTerms =
        searchTerms[language] ?: searchTerms[Language.ENGLISH]!!

    suspend fun fetchCropNotes(language: Language = Language.TAMIL): List<CropNote> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.agriculture, 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            val title = page.title ?: "${t.fallbackNote} ${index + 1}"
            CropNote(
                id = index + 1,
                cropName = title,
                title = title,
                content = cleanExcerpt(page.excerpt) ?: page.description ?: "",
                sourceUrl = pageUrl(host, page.key)
            )
        }
    }

    suspend fun fetchDiseases(language: Language = Language.TAMIL): List<Disease> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.plantDisease, 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            Disease(
                id = index + 1,
                name = page.title ?: "${t.fallbackDisease} ${index + 1}",
                cropAffected = page.description ?: cleanExcerpt(page.excerpt) ?: t.fallbackGeneral,
                sourceUrl = pageUrl(host, page.key)
            )
        }
    }

    suspend fun fetchSchemes(language: Language = Language.TAMIL): List<Scheme> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.agriculturalScheme, 10)
        if (results.isEmpty()) throw Exception("Wikipedia API returned no results")
        results.mapIndexed { index, page ->
            Scheme(
                id = index + 1,
                title = page.title ?: "${t.fallbackScheme} ${index + 1}",
                description = cleanExcerpt(page.excerpt) ?: page.description ?: "",
                category = "central", // Use English ID for filter logic
                sourceUrl = pageUrl(host, page.key)
            )
        }
    }

    private fun searchWikipedia(host: String, query: String, limit: Int): List<WikipediaPage> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "$host/w/rest.php/v1/search/page?q=$encoded&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AndroidFarmerFriend/${com.example.androidfarmerfriend.BuildConfig.VERSION_NAME}")
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

    private fun pageUrl(host: String, key: String?): String {
        if (key.isNullOrBlank()) return ""
        val encoded = URLEncoder.encode(key, "UTF-8").replace("+", "%20")
        return "$host/wiki/$encoded"
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
