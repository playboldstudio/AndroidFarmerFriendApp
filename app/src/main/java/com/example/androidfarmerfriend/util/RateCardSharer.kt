package com.example.androidfarmerfriend.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

/** Saves a rendered rate card to cache and opens the system share sheet with a caption. */
object RateCardSharer {

    private const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.playboldstudio.farmerfriend"

    fun share(context: Context, bitmap: Bitmap, marketName: String = "", dateLabel: String = "") {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "rate-card.png")
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val caption = buildCaption(marketName, dateLabel)

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null))
    }

    private fun buildCaption(marketName: String, dateLabel: String): String {
        val sb = StringBuilder()
        sb.appendLine("📊 Daily Market Rates — Farmer Friend")
        if (dateLabel.isNotBlank() || marketName.isNotBlank()) {
            sb.appendLine("$dateLabel • $marketName".trim())
        }
        sb.appendLine()
        sb.appendLine("📲 Download the app for live prices:")
        sb.append(PLAY_STORE_URL)
        return sb.toString()
    }
}
