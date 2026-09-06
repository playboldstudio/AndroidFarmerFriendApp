package com.example.androidfarmerfriend.data.scraper

import com.example.androidfarmerfriend.data.api.NetworkErrors
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        // Upper bound on total time (incl. retries) so a hung host can't block forever.
        .callTimeout(30, TimeUnit.SECONDS)
        // Retry transient connection failures — safe: all calls here are GETs.
        .retryOnConnectionFailure(true)
        .build()

    private val gson = Gson()

    // --- Language → Wikipedia subdomain ---
    private const val ENGLISH_WIKI_HOST = "https://en.wikipedia.org"

    /** Retry budget for each host; bounded overall by the client's callTimeout. */
    private const val MAX_ATTEMPTS = 2

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

    suspend fun fetchCropNotes(language: Language = Language.ENGLISH): List<CropNote> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.agriculture, 10)
        if (results.isEmpty()) {
            // Gracefully degrade to an empty list instead of crashing the screen.
            NetworkErrors.record("fetchCropNotes.${language.code}", IllegalStateException("No Wikipedia results"))
            return@withContext emptyList()
        }
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

    suspend fun fetchDiseases(language: Language = Language.ENGLISH): List<Disease> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.plantDisease, 10)
        if (results.isEmpty()) {
            NetworkErrors.record("fetchDiseases.${language.code}", IllegalStateException("No Wikipedia results"))
            return@withContext emptyList()
        }
        results.mapIndexed { index, page ->
            Disease(
                id = index + 1,
                name = page.title ?: "${t.fallbackDisease} ${index + 1}",
                cropAffected = page.description ?: t.fallbackGeneral,
                sourceUrl = pageUrl(host, page.key),
                excerpt = cleanExcerpt(page.excerpt) ?: page.description ?: ""
            )
        }
    }

    suspend fun fetchSchemes(language: Language = Language.ENGLISH): List<Scheme> = withContext(Dispatchers.IO) {
        val t = terms(language)
        val host = wikiHost(language)
        val results = searchWikipedia(host, t.agriculturalScheme, 10)
        if (results.isEmpty()) {
            NetworkErrors.record("fetchSchemes.${language.code}", IllegalStateException("No Wikipedia results"))
            return@withContext emptyList()
        }
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

    /**
     * Search Wikipedia for the given query. Tries the requested-language host
     * first, then falls back to English Wikipedia if the language host fails or
     * returns nothing, so the reference screens still show content rather than
     * a hard failure. Real failures are recorded to Crashlytics.
     */
    private fun searchWikipedia(host: String, query: String, limit: Int): List<WikipediaPage> {
        val hosts = listOf(host, ENGLISH_WIKI_HOST).distinct()
        for (h in hosts) {
            val pages = trySearch(h, query, limit)
            if (pages.isNotEmpty()) return pages
        }
        return emptyList()
    }

    private fun trySearch(host: String, query: String, limit: Int): List<WikipediaPage> {
        var lastError: Throwable? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "$host/w/rest.php/v1/search/page?q=$encoded&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "AndroidFarmerFriend/${com.example.androidfarmerfriend.BuildConfig.VERSION_NAME}")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return emptyList()
                        val searchResult = try {
                            gson.fromJson(body, WikipediaSearchResult::class.java)
                        } catch (_: Exception) {
                            null
                        }
                        val pages = searchResult?.pages ?: emptyList()
                        if (pages.isNotEmpty()) return pages
                        lastError = IllegalStateException("Wikipedia returned empty body on $host")
                    } else if (response.code == 429 || response.code in 500..599) {
                        // Transient — worth a retry.
                        lastError = RuntimeException("Wikipedia HTTP ${response.code} on $host")
                    }
                }
            } catch (e: Exception) {
                lastError = e
            }
            // Backoff before retrying (skip on the last attempt).
            if (attempt < MAX_ATTEMPTS - 1) Thread.sleep(500L * (attempt + 1))
        }
        lastError?.let { NetworkErrors.record("searchWikipedia.$host", it) }
        return emptyList()
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
