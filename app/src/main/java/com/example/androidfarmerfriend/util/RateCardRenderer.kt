package com.example.androidfarmerfriend.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.model.Crop
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Draws a branded, social-ready market rate card as a bitmap.
 * Layout matches the HTML rate-card.html mockup exactly:
 *   header (gradient + glows + brand/title/meta/pill)
 *   → overlapping body card (stats chips + crop rows)
 *   → no footer
 */
object RateCardRenderer {

    private const val WIDTH = 1080
    private const val PAD = 65f          // ~24px @3x
    private const val CARD_PAD = 43f     // ~16px @3x
    private const val HEADER_H = 520f
    private const val BODY_OVERLAP = 43f // body card pulls up into header
    private const val STATS_H = 200f
    private const val ROW_H = 155f
    private const val ICON_SIZE = 114f   // ~42px @3x crop icon
    private const val CORNER = 65f       // ~24px @3x

    // ── HTML tokens ──
    private val GREEN_DEEP   = Color.parseColor("#1B5E20")
    private val GREEN_PRIMARY = Color.parseColor("#2E7D32")
    private val GREEN_BRIGHT  = Color.parseColor("#4CAF50")
    private val PAGE_BG       = Color.parseColor("#FAF8F5")
    private val SURFACE       = Color.parseColor("#FFFFFF")
    private val SURFACE_MUTED = Color.parseColor("#F5F3EF")
    private val TEXT_DARK      = Color.parseColor("#1B2A1E")
    private val TEXT_BODY      = Color.parseColor("#3A4A3E")
    private val TEXT_MUTED     = Color.parseColor("#7A8A7E")
    private val TEXT_FAINT     = Color.parseColor("#A8B8AC")
    private val BORDER         = Color.parseColor("#E8E6E1")
    private val GREEN_SOFT     = Color.parseColor("#E8F5E9")
    private val TREND_UP       = Color.parseColor("#2E7D32")
    private val TREND_DOWN     = Color.parseColor("#C62828")

    fun render(
        context: Context,
        title: String,
        dateLabel: String,
        marketName: String,
        filterLabel: String,
        crops: List<Crop>,
        maxRows: Int = 12
    ): Bitmap {
        val rows = crops.take(maxRows)

        // Compute total height: header + body card (stats + rows)
        val statsH = STATS_H
        val rowsH = rows.size * ROW_H
        val bodyContentH = statsH + rowsH + CARD_PAD * 2
        val bodyTop = HEADER_H - BODY_OVERLAP
        val totalH = (bodyTop + bodyContentH + CARD_PAD).roundToInt()

        val source = Bitmap.createBitmap(WIDTH, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(source)
        canvas.drawColor(PAGE_BG)

        val logo = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
        drawHeader(canvas, title, dateLabel, marketName, filterLabel, logo)
        drawBodyCard(canvas, bodyTop, bodyContentH)
        drawStats(canvas, rows, bodyTop)
        drawCropRows(canvas, rows, bodyTop + statsH)

        return rounded(source)
    }

    /* ── Header ───────────────────────────────────────────────────── */

    private fun drawHeader(
        canvas: Canvas,
        title: String,
        dateLabel: String,
        marketName: String,
        filterLabel: String,
        logo: Bitmap
    ) {
        // 135° gradient: deep → primary → bright
        val gradient = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEADER_H,
                intArrayOf(GREEN_DEEP, GREEN_PRIMARY, GREEN_BRIGHT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(0f, 0f, WIDTH.toFloat(), HEADER_H), gradient)

        // Decorative radial glows (matching HTML ::before and ::after)
        val glow1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, 255, 255, 255)
        }
        canvas.drawCircle(WIDTH - 110f, -50f, 430f, glow1) // top-right glow
        val glow2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(20, 255, 255, 255)
        }
        canvas.drawCircle(55f, HEADER_H - 55f, 325f, glow2) // bottom-left glow

        // Brand: logo + "Farmer Friend"
        val brandPaint = textPaint(Color.argb(180, 255, 255, 255), 36f)
        canvas.drawText("Farmer Friend", PAD, 100f, brandPaint)

        // App logo (ic_launcher_foreground) drawn as rounded square
        val logoSize = 120f
        val logoLeft = WIDTH - PAD - logoSize
        val logoTop = 50f
        val logoRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
        // Semi-transparent white background behind logo
        val logoBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 255, 255, 255) }
        canvas.drawRoundRect(logoRect, 28f, 28f, logoBgPaint)
        // Draw the logo bitmap scaled into the rect
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(logo, null, logoRect, logoPaint)

        // Title: large bold
        val titlePaint = textPaint(Color.WHITE, 76f, bold = true)
        canvas.drawText(title, PAD, 210f, titlePaint)

        // Meta: date • market
        val metaPaint = textPaint(Color.argb(200, 255, 255, 255), 38f)
        val metaText = "$dateLabel   •   $marketName"
        canvas.drawText(metaText, PAD, 280f, metaPaint)

        // Filter pill
        if (filterLabel.isNotBlank()) {
            val pillPaint = textPaint(Color.WHITE, 34f, bold = true)
            val label = filterLabel.uppercase()
            val labelW = pillPaint.measureText(label)
            val pillW = labelW + 80f
            val pillLeft = PAD
            val pillTop = 320f
            val pillBot = pillTop + 70f
            val pillRect = RectF(pillLeft, pillTop, pillLeft + pillW, pillBot)
            val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(38, 255, 255, 255)
            }
            canvas.drawRoundRect(pillRect, 35f, 35f, pillBg)
            pillPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, pillRect.centerX(), pillRect.centerY() + 12f, pillPaint)
            pillPaint.textAlign = Paint.Align.LEFT
        }
    }

    /* ── Body card ────────────────────────────────────────────────── */

    private fun drawBodyCard(canvas: Canvas, top: Float, height: Float) {
        val rect = RectF(CARD_PAD, top, WIDTH - CARD_PAD, top + height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SURFACE
            setShadowLayer(20f, 0f, 8f, Color.argb(30, 0, 0, 0))
        }
        canvas.drawRoundRect(rect, CORNER, CORNER, paint)
    }

    /* ── Stats chips ──────────────────────────────────────────────── */

    private fun drawStats(canvas: Canvas, rows: List<Crop>, bodyTop: Float) {
        val prices = rows.mapNotNull { it.priceValue }.filter { it > 0 }
        val upCount = rows.count { (it.priceDiffPercent ?: it.trend) > 0 }
        val unit = rows.firstOrNull()?.units ?: "kg"

        val itemCount = rows.size.toString()
        val rangeText = if (prices.isNotEmpty()) "₹${prices.min().toInt()} – ₹${prices.max().toInt()}" else "—"
        val upText = "$upCount ▲"

        val chipW = (WIDTH - CARD_PAD * 2 - CARD_PAD * 2) / 3f
        val chipH = 130f
        val chipTop = bodyTop + CARD_PAD
        val chipGap = CARD_PAD * 0.6f
        val chipStartX = CARD_PAD + CARD_PAD

        val values = listOf(itemCount, rangeText, upText)
        val labels = listOf("Items", "Price / $unit", "Up today")

        val valuePaint = textPaint(TEXT_DARK, 46f, bold = true)
        val labelPaint = textPaint(TEXT_MUTED, 28f)

        for (i in 0 until 3) {
            val cx = chipStartX + i * (chipW + chipGap)
            val chipRect = RectF(cx, chipTop, cx + chipW, chipTop + chipH)

            // Chip background
            val chipBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = SURFACE_MUTED
            }
            canvas.drawRoundRect(chipRect, 32f, 32f, chipBg)

            // Value
            valuePaint.textAlign = Paint.Align.CENTER
            canvas.drawText(values[i], chipRect.centerX(), chipTop + 65f, valuePaint)

            // Label
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(labels[i], chipRect.centerX(), chipTop + 105f, labelPaint)
        }

        // Reset alignment
        valuePaint.textAlign = Paint.Align.LEFT
        labelPaint.textAlign = Paint.Align.LEFT
    }

    /* ── Crop rows ────────────────────────────────────────────────── */

    private fun drawCropRows(canvas: Canvas, rows: List<Crop>, rowsTop: Float) {
        val rowStartX = CARD_PAD + CARD_PAD
        val iconBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN_SOFT }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER
            strokeWidth = 2f
        }

        val namePaint = textPaint(TEXT_DARK, 42f, bold = true)
        val retailPaint = textPaint(TEXT_MUTED, 32f)
        val pricePaint = textPaint(GREEN_DEEP, 48f, bold = true)
        val trendUpPaint = textPaint(TREND_UP, 30f, bold = true)
        val trendDownPaint = textPaint(TREND_DOWN, 30f, bold = true)
        val iconPaint = textPaint(TEXT_BODY, 52f)

        rows.forEachIndexed { index, crop ->
            val y = rowsTop + index * ROW_H

            // Icon background circle
            val iconRect = RectF(rowStartX, y + 18f, rowStartX + ICON_SIZE, y + 18f + ICON_SIZE)
            canvas.drawRoundRect(iconRect, 32f, 32f, iconPaint.apply { color = GREEN_SOFT })

            // Crop emoji/icon (first letter as fallback)
            val iconChar = getCropEmoji(crop)
            iconPaint.textAlign = Paint.Align.CENTER
            iconPaint.color = TEXT_BODY
            canvas.drawText(iconChar, iconRect.centerX(), iconRect.centerY() + 18f, iconPaint)
            iconPaint.textAlign = Paint.Align.LEFT

            // Name
            val name = crop.nameEng.ifBlank { crop.name }
            val textLeft = rowStartX + ICON_SIZE + 32f
            canvas.drawText(name, textLeft, y + 72f, namePaint)

            // Retail price
            val retail = crop.retailPrice.takeIf { it.isNotBlank() }
            if (retail != null) {
                canvas.drawText("Retail ₹$retail", textLeft, y + 115f, retailPaint)
            }

            // Price (right-aligned)
            pricePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(crop.price, WIDTH - CARD_PAD - CARD_PAD, y + 78f, pricePaint)
            pricePaint.textAlign = Paint.Align.LEFT

            // Trend
            val trend = crop.priceDiffPercent ?: crop.trend.takeIf { it != 0.0 }
            if (trend != null && trend != 0.0) {
                val trendPaint = if (trend > 0) trendUpPaint else trendDownPaint
                val arrow = if (trend > 0) "▲" else "▼"
                val label = "$arrow ${"%.1f".format(abs(trend))}%"
                trendPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(label, WIDTH - CARD_PAD - CARD_PAD, y + 120f, trendPaint)
                trendPaint.textAlign = Paint.Align.LEFT
            }

            // Divider
            if (index != rows.lastIndex) {
                val divY = y + ROW_H - 4f
                canvas.drawLine(iconRect.left, divY, WIDTH - CARD_PAD - CARD_PAD, divY, dividerPaint)
            }
        }
    }

    /* ── Helpers ──────────────────────────────────────────────────── */

    private fun getCropEmoji(crop: Crop): String {
        val name = crop.nameEng.ifBlank { crop.name }.lowercase()
        return when {
            "tomato" in name -> "🍅"
            "onion" in name -> "🧅"
            "potato" in name -> "🥔"
            "chilli" in name || "chili" in name -> "🌶️"
            "brinjal" in name || "eggplant" in name -> "🍆"
            "carrot" in name -> "🥕"
            "cabbage" in name -> "🥬"
            "beetroot" in name -> "🫓"
            "green" in name -> "🌿"
            "capsicum" in name || "pepper" in name -> "🌶️"
            "lemon" in name || "lime" in name -> "🍋"
            "banana" in name -> "🍌"
            "apple" in name -> "🍎"
            "orange" in name -> "🍊"
            "mango" in name -> "🍭"
            "grape" in name -> "🍇"
            "egg" in name -> "🥚"
            "chicken" in name || "mutton" in name || "fish" in name -> "🍗"
            "gold" in name || "silver" in name -> "💎"
            else -> "🌿"
        }
    }

    private fun rounded(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val path = Path().apply {
            addRoundRect(
                RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
                CORNER, CORNER, Path.Direction.CW
            )
        }
        canvas.drawPath(path, paint)
        return output
    }
}

private fun textPaint(color: Int, size: Float, bold: Boolean = false): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
