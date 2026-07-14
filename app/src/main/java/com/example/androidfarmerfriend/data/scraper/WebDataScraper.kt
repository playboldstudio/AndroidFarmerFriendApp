package com.example.androidfarmerfriend.data.scraper

import com.example.androidfarmerfriend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object WebDataScraper {

    private const val TIMEOUT = 10000
    private const val WIKI_BASE = "https://ta.wikipedia.org"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"

    suspend fun fetchCropNotes(): List<CropNote> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%B5%E0%AE%BF%E0%AE%B5%E0%AE%9A%E0%AE%BE%E0%AE%AF%E0%AE%AE%E0%AF%8D")
            .userAgent(USER_AGENT).timeout(TIMEOUT).get()
        parseCropNotes(doc)
    }

    suspend fun fetchDiseases(): List<Disease> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%A4%E0%AE%BE%E0%AE%B5%E0%AE%B0_%E0%AE%A8%E0%AF%8B%E0%AE%AF%E0%AF%8D%E0%AE%95%E0%AE%B3%E0%AF%8D")
            .userAgent(USER_AGENT).timeout(TIMEOUT).get()
        parseDiseases(doc)
    }

    suspend fun fetchSchemes(): List<Scheme> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("$WIKI_BASE/wiki/%E0%AE%87%E0%AE%A8%E0%AF%8D%E0%AE%A4%E0%AE%BF%E0%AE%AF_%E0%AE%B5%E0%AE%BF%E0%AE%B5%E0%AE%9A%E0%AE%BE%E0%AE%AF_%E0%AE%A4%E0%AE%BF%E0%AE%9F%E0%AF%8D%E0%AE%9F%E0%AE%99%E0%AF%8D%E0%AE%95%E0%AE%B3%E0%AF%8D")
            .userAgent(USER_AGENT).timeout(TIMEOUT).get()
        parseSchemes(doc)
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
                        notes.add(CropNote(currentTitle.hashCode() + notes.size, currentCrop, currentTitle, text, ""))
                    }
                }
            }
            if (notes.size >= 10) break
        }
        return notes
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
        return diseases
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
        return schemes
    }
}
