package com.example.androidfarmerfriend.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.androidfarmerfriend.data.model.Crop
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Draws a branded, social-ready market rate card as a bitmap.
 * Portrait layout that shares well to WhatsApp, Instagram and X.
 */
object RateCardRenderer {

    private const val WIDTH = 1080
    private const val HEADER_HEIGHT = 380f
    private const val ROW_HEIGHT = 128f
    private const val FOOTER_HEIGHT = 160f
    private const val PADDING = 56f
    private const val CORNER = 56f

    private val GREEN_BRIGHT = 0xFF66BB6A.toInt()
    private val GREEN_PRIMARY = 0xFF2E7D32.toInt()
    private val GREEN_DEEP = 0xFF1B5E20.toInt()
    private val PAGE_BG = 0xFFF7F6F3.toInt()
    private val TEXT_DARK = 0xFF1C1C1E.toInt()
    private val TEXT_FADED = 0xFF9A9A9E.toInt()
    private val RED = 0xFFE53935.toInt()

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
        val bodyHeight = rows.size * ROW_HEIGHT + PADDING * 2
        val height = (HEADER_HEIGHT + bodyHeight + FOOTER_HEIGHT).roundToInt()

        val source = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(source)

        canvas.drawColor(PAGE_BG)
        drawHeader(canvas, title, dateLabel, marketName, filterLabel)
        drawBody(canvas, rows, HEADER_HEIGHT, bodyHeight)
        drawFooter(canvas, HEADER_HEIGHT + bodyHeight, height.toFloat())

        return rounded(source)
    }

    private fun drawHeader(
        canvas: Canvas,
        title: String,
        dateLabel: String,
        marketName: String,
        filterLabel: String
    ) {
        val gradient = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEADER_HEIGHT,
                GREEN_BRIGHT, GREEN_PRIMARY, Shader.TileMode.CLAMP
            )
        }
        val rect = RectF(0f, 0f, WIDTH.toFloat(), HEADER_HEIGHT)
        canvas.drawRect(rect, gradient)

        // Decorative glows.
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x24FFFFFF }
        canvas.drawCircle(WIDTH - 40f, 30f, 180f, glow)
        canvas.drawCircle(70f, HEADER_HEIGHT - 20f, 140f, glow)

        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        val softWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE6FFFFFF.toInt() }

        white.text(48f).let { canvas.drawText("Farmer Friend", PADDING, 92f, it) }
        white.bold(66f).let { canvas.drawText(title, PADDING, 182f, it) }
        softWhite.medium(34f).let { canvas.drawText("$dateLabel   •   $marketName", PADDING, 242f, it) }

        if (filterLabel.isNotBlank()) {
            val label = filterLabel.uppercase()
            val labelWidth = softWhite.semiBold(32f).measureText(label)
            val pill = RectF(PADDING, 268f, PADDING + labelWidth + 60f, 330f)
            canvas.drawRoundRect(pill, 31f, 31f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF })
            canvas.drawText(label, pill.left + 30f, pill.centerY() + 11f, white.semiBold(32f))
        }

        white.text(96f).let { canvas.drawText("🌾", WIDTH - 170f, 200f, it) }
    }

    private fun drawBody(canvas: Canvas, rows: List<Crop>, headerBottom: Float, bodyHeight: Float) {
        val cardTop = headerBottom - 40f
        val cardRect = RectF(PADDING / 2, cardTop, WIDTH - PADDING / 2f, cardTop + bodyHeight + 24f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            pathEffect = CornerPathEffect(48f)
            setShadowLayer(16f, 0f, 8f, 0x22000000)
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        var y = cardTop + PADDING
        val divider = Paint().apply { color = 0xFFEDECE7.toInt(); strokeWidth = 2f }

        rows.forEachIndexed { index, crop ->
            val name = crop.nameEng.ifBlank { crop.name }
            canvas.drawText(name, PADDING, y + 46f, textPaint(TEXT_DARK, 40f, bold = true))

            val retailLine = crop.retailPrice.takeIf { it.isNotBlank() }?.let { "Retail ₹$it" }
            if (retailLine != null) {
                canvas.drawText(retailLine, PADDING, y + 92f, textPaint(TEXT_FADED, 27f))
            }

            canvas.drawText(crop.price, WIDTH - PADDING, y + 58f, rightAligned(textPaint(GREEN_DEEP, 42f, bold = true)))

            val trend = crop.priceDiffPercent ?: crop.trend.takeIf { it != 0.0 }
            if (trend != null && trend != 0.0) {
                val arrow = if (trend > 0) "▲" else "▼"
                val label = "$arrow ${"%.1f".format(abs(trend))}% vs month avg"
                canvas.drawText(label, WIDTH - PADDING, y + 100f, rightAligned(textPaint(if (trend > 0) GREEN_PRIMARY else RED, 28f, bold = true)))
            }

            y += ROW_HEIGHT
            if (index != rows.lastIndex) {
                canvas.drawLine(cardRect.left + 40f, y - 14f, cardRect.right - 40f, y - 14f, divider)
            }
        }
    }

    private fun drawFooter(canvas: Canvas, top: Float, cardBottom: Float) {
        val gradient = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, top, WIDTH.toFloat(), cardBottom,
                GREEN_PRIMARY, GREEN_BRIGHT, Shader.TileMode.CLAMP
            )
        }
        val rect = RectF(0f, top, WIDTH.toFloat(), cardBottom)
        canvas.drawRoundRect(rect, CORNER, CORNER, gradient)
        canvas.drawRect(0f, top + CORNER, WIDTH.toFloat(), cardBottom, gradient)

        val center = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; color = 0xFFFFFFFF.toInt() }
        canvas.drawText("Daily market rates for farmers", WIDTH / 2f, top + heightFraction(rect, 0.42f), center.medium(32f))
        canvas.drawText("Farmer Friend", WIDTH / 2f, top + heightFraction(rect, 0.78f), center.bold(44f))
    }

    private fun heightFraction(rect: RectF, fraction: Float): Float =
        rect.height() * fraction

    private fun rounded(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val path = Path().apply {
            addRoundRect(
                RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
                CORNER, CORNER,
                Path.Direction.CW
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

private fun rightAligned(paint: Paint): Paint = paint.apply { textAlign = Paint.Align.RIGHT }

private fun Paint.text(size: Float): Paint = apply { textSize = size; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
private fun Paint.bold(size: Float): Paint = apply { textSize = size; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
private fun Paint.medium(size: Float): Paint = apply { textSize = size }
private fun Paint.semiBold(size: Float): Paint = apply { textSize = size; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
