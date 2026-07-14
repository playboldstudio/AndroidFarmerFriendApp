package com.example.androidfarmerfriend.data.scraper

import com.example.androidfarmerfriend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object WebDataScraper {

    private const val TIMEOUT = 8000
    private const val WIKI_BASE = "https://ta.wikipedia.org"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"

    suspend fun fetchCropNotes(): List<CropNote> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%B5%E0%AE%BF%E0%AE%B5%E0%AE%9A%E0%AE%BE%E0%AE%AF%E0%AE%AE%E0%AF%8D")
                .userAgent(USER_AGENT).timeout(TIMEOUT).get()
            parseCropNotes(doc)
        } catch (_: Exception) {
            fallbackCropNotes()
        }
    }

    suspend fun fetchDiseases(): List<Disease> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%A4%E0%AE%BE%E0%AE%B5%E0%AE%B0_%E0%AE%A8%E0%AF%8B%E0%AE%AF%E0%AF%8D%E0%AE%95%E0%AE%B3%E0%AF%8D")
                .userAgent(USER_AGENT).timeout(TIMEOUT).get()
            parseDiseases(doc)
        } catch (_: Exception) {
            fallbackDiseases()
        }
    }

    suspend fun fetchSchemes(): List<Scheme> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%87%E0%AE%A8%E0%AF%8D%E0%AE%A4%E0%AE%BF%E0%AE%AF_%E0%AE%B5%E0%AE%BF%E0%AE%B5%E0%AE%9A%E0%AE%BE%E0%AE%AF_%E0%AE%A4%E0%AE%BF%E0%AE%9F%E0%AF%8D%E0%AE%9F%E0%AE%99%E0%AF%8D%E0%AE%95%E0%AE%B3%E0%AF%8D")
                .userAgent(USER_AGENT).timeout(TIMEOUT).get()
            parseSchemes(doc)
        } catch (_: Exception) {
            fallbackSchemes()
        }
    }

    private fun parseCropNotes(doc: Document): List<CropNote> {
        val notes = mutableListOf<CropNote>()
        val sections = doc.select("div.mw-parser-output > h2, div.mw-parser-output > h3, div.mw-parser-output > p")
        var currentTitle = "பயிர் குறிப்பு"
        var currentCrop = "பொது"
        for (el in sections) {
            val tag = el.tagName()
            when {
                tag == "h2" || tag == "h3" -> {
                    val text = el.text().trim()
                    if (text.isNotBlank() && !text.contains("உசாத்துணை") && !text.contains("வெளி இணைப்புகள்")) {
                        currentTitle = text
                        currentCrop = text.split(" ").getOrElse(0) { "பொது" }
                    }
                }
                tag == "p" -> {
                    val text = el.text().trim()
                    if (text.length > 30) {
                        val id = currentTitle.hashCode() + notes.size
                        notes.add(CropNote(id, currentCrop, currentTitle, text, ""))
                    }
                }
            }
            if (notes.size >= 10) break
        }
        return notes.ifEmpty { fallbackCropNotes() }
    }

    private fun parseDiseases(doc: Document): List<Disease> {
        val diseases = mutableListOf<Disease>()
        val lis = doc.select("div.mw-parser-output ul li")
        for (li in lis) {
            val text = li.text().trim()
            if (text.contains("நோய்") && text.length in 10..200) {
                val parts = text.split(" - ", " – ", " : ", ":")
                val name = parts.first().trim().take(60)
                val crop = parts.getOrElse(1) { "" }.trim().take(40)
                diseases.add(Disease(diseases.size + 1, name, crop.ifEmpty { "பொது" }))
            }
            if (diseases.size >= 12) break
        }
        return diseases.ifEmpty { fallbackDiseases() }
    }

    private fun parseSchemes(doc: Document): List<Scheme> {
        val schemes = mutableListOf<Scheme>()
        val lis = doc.select("div.mw-parser-output ul li")
        for (li in lis) {
            val text = li.text().trim()
            if (text.contains("திட்டம்") && text.length in 15..250) {
                val parts = text.split(" - ", " – ", " : ", ":")
                val title = parts.first().trim().take(60)
                val desc = parts.getOrElse(1) { text }.trim().take(120)
                schemes.add(Scheme(schemes.size + 1, title, desc, "மத்திய அரசு"))
            }
            if (schemes.size >= 10) break
        }
        return schemes.ifEmpty { fallbackSchemes() }
    }

    private fun fallbackCropNotes() = listOf(
        CropNote(1, "தக்காளி", "நடவு செய்வது", "தக்காளி நாற்றுகளை 60×45 செ.மீ இடைவெளியில் நட வேண்டும். அதிக மகசூலுக்கு சூரிய ஒளி தேவை.", "கார்"),
        CropNote(2, "தக்காளி", "நீர் பாசனம்", "வாரத்திற்கு 2-3 முறை நீர் பாய்ச்சவும். சொட்டு நீர் பாசனம் சிறந்தது.", "கார்"),
        CropNote(3, "வெங்காயம்", "நடவு முறை", "வெங்காய விதைகளை 15×10 செ.மீ இடைவெளியில் விதைக்கவும்.", "திருப்பூர்"),
        CropNote(4, "வெங்காயம்", "உர மேலாண்மை", "ஏக்கருக்கு 60 கிலோ நைட்ரஜன், 30 கிலோ பாஸ்பரஸ், 30 கிலோ பொட்டாஷ் இடவும்.", "திருப்பூர்"),
        CropNote(5, "மிளகாய்", "நோய் தடுப்பு", "இலை கருகல் நோய் தடுக்க 14 நாட்களுக்கு ஒருமுறை இயற்கை பூச்சிக்கொல்லி தெளிக்கவும்.", "மே-சூன்"),
        CropNote(6, "உருளைக்கிழங்கு", "சேமிப்பு முறை", "உருளைக்கிழங்கை குளிர்ந்த, இருண்ட இடத்தில் சேமிக்கவும். நேரடி சூரிய ஒளி தவிர்க்கவும்.", "தற்போது")
    )

    private fun fallbackDiseases() = listOf(
        Disease(1, "இலை கருகல் நோய்", "தக்காளி"),
        Disease(2, "பூஞ்சை நோய்", "வெங்காயம்"),
        Disease(3, "பழு சிதைவு", "மிளகாய்"),
        Disease(4, "வேர் அழுகல்", "நிலக்கடலை"),
        Disease(5, "மஞ்சள் வைரஸ்", "பயறு")
    )

    private fun fallbackSchemes() = listOf(
        Scheme(1, "பிரதான் மந்திரி கிசான் திட்டம்", "சிறு மற்றும் குறு விவசாயிகளுக்கு ஆண்டுக்கு ரூ. 6000", "மத்திய அரசு"),
        Scheme(2, "உழவர் காப்பீட்டு திட்டம்", "விவசாயிகளுக்கான பயிர் இழப்பு ஈடு செய்யும் திட்டம்", "மாநில அரசு"),
        Scheme(3, "பயண உதவி திட்டம்", "விவசாயப் பொருட்களை சந்தைக்கு கொண்டு செல்ல மானியம்", "மாநில அரசு"),
        Scheme(4, "விதை மானியம் திட்டம்", "உயர் விளைச்சல் ரக விதைகள் மானிய விலையில்", "மத்திய அரசு")
    )
}
