package com.example.unum.graphics

import android.graphics.*
import com.example.unum.data.model.*
import kotlin.math.*

/** The same native artwork is used by the shelf, reader and exported PDF. */
object MoonlightBookPainter {
    private const val PAPER = 0xFFFFFCF5.toInt()
    private const val FOIL = 0xFFAD8B60.toInt()

    fun draw(canvas: Canvas, bounds: RectF, style: MoonlightCoverStyle, book: FortuneBook?, compact: Boolean) {
        val save = canvas.save()
        canvas.translate(bounds.left, bounds.top)
        canvas.scale(bounds.width() / 1000f, bounds.height() / 1480f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PAPER }
        canvas.drawRoundRect(RectF(0f, 0f, 1000f, 1480f), 48f, 48f, p)
        p.shader = LinearGradient(0f, 0f, 1000f, 1480f, PAPER, 0xFFF5F0E6.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(0f, 0f, 1000f, 1480f), 48f, 48f, p)
        p.shader = null
        p.color = 0x098D795F
        p.strokeWidth = 1.5f
        repeat(100) { i -> canvas.drawLine(0f, i * 22f - 600f, 1000f, i * 22f + 400f, p) }
        p.color = style.tint
        canvas.drawRoundRect(RectF(0f, 0f, 38f, 1480f), 14f, 14f, p)
        p.color = FOIL
        p.alpha = 120
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        canvas.drawRoundRect(RectF(52f, 52f, 948f, 1428f), 25f, 25f, p)
        p.style = Paint.Style.FILL
        p.color = style.accent
        p.alpha = 170
        canvas.drawPath(Path().apply { moveTo(811f, 52f); lineTo(868f, 52f); lineTo(868f, 247f); lineTo(839f, 222f); lineTo(811f, 247f); close() }, p)

        p.shader = RadialGradient(500f, 393f, 365f, intArrayOf(style.tint, PAPER), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        p.alpha = 255
        canvas.drawOval(RectF(165f, 88f, 835f, 702f), p)
        p.shader = null
        p.style = Paint.Style.STROKE
        p.color = style.accent
        p.alpha = 40
        p.strokeWidth = 3f
        when (style.symbol) {
            CoverSymbol.COMPASS -> { canvas.drawCircle(500f, 416f, 257f, p); canvas.drawLine(243f, 416f, 757f, 416f, p) }
            CoverSymbol.MIRROR -> canvas.drawOval(RectF(298f, 145f, 702f, 660f), p)
            CoverSymbol.COINS -> repeat(3) { canvas.drawOval(RectF(320f, 528f + it * 30, 680f, 596f + it * 30), p) }
            CoverSymbol.BOOK -> { canvas.drawLine(260f, 608f, 470f, 640f, p); canvas.drawLine(530f, 640f, 740f, 608f, p) }
            CoverSymbol.CASE -> { canvas.drawLine(276f, 634f, 435f, 535f, p); canvas.drawLine(435f, 535f, 590f, 571f, p); canvas.drawLine(590f, 571f, 729f, 478f, p) }
            else -> canvas.drawOval(RectF(245f, 185f, 755f, 650f), p)
        }
        symbol(canvas, RectF(222f, 540f, 314f, 632f), style.symbol, style.accent, 50)
        symbol(canvas, RectF(702f, 506f, 776f, 580f), style.symbol, style.accent, 40)
        p.color = FOIL
        p.alpha = 150
        p.strokeWidth = 3f
        canvas.drawCircle(500f, 367f, 145f, p)
        symbol(canvas, RectF(416f, 283f, 584f, 451f), style.symbol, style.accent)
        star(canvas, 263f, 307f, 22f)
        star(canvas, 740f, 197f, 27f)
        star(canvas, 655f, 650f, 15f)

        text(canvas, style.label, 795f, 115f, style.accent, 790f, serif = true)
        text(canvas, if (style.compatibility) "궁합노트" else "운세노트", 913f, 55f, style.accent, 790f)
        p.color = FOIL
        p.alpha = 150
        p.strokeWidth = 3f
        canvas.drawLine(415f, 999f, 585f, 999f, p)
        if (!compact) {
            val subtitle = book?.coverTitle?.takeIf { it.isNotBlank() } ?: style.subtitle
            wrappedText(canvas, subtitle, 1090f, 45f, style.accent, 760f, 2)
        }
        symbol(canvas, RectF(291f, 1304f, 345f, 1358f), CoverSymbol.MOON, FOIL)
        text(canvas, "SURI NOTE", 1350f, 43f, FOIL, 430f, x = 546f)
        canvas.restoreToCount(save)
    }

    private fun text(canvas: Canvas, value: String, y: Float, size: Float, color: Int, maxWidth: Float, serif: Boolean = false, x: Float = 500f) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(if (serif) "serif" else "sans-serif", if (serif) Typeface.BOLD else Typeface.NORMAL)
        }
        if (p.measureText(value) > maxWidth) p.textSize *= maxWidth / p.measureText(value)
        canvas.drawText(value, x, y, p)
    }

    private fun wrappedText(canvas: Canvas, value: String, y: Float, size: Float, color: Int, maxWidth: Float, maxLines: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; this.color = color }
        var rest = value.replace('\n', ' ').trim()
        repeat(maxLines) { line ->
            if (rest.isEmpty()) return
            val count = p.breakText(rest, true, maxWidth, null).coerceAtLeast(1)
            var part = rest.take(count)
            rest = rest.drop(count).trimStart()
            if (line == maxLines - 1 && rest.isNotEmpty()) part = part.dropLast(1) + "…"
            text(canvas, part, y + line * size * 1.6f, size, color, maxWidth)
        }
    }

    private fun star(canvas: Canvas, x: Float, y: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = FOIL; alpha = 150 }
        canvas.drawPath(Path().apply { moveTo(x, y-r); quadTo(x+3, y-3, x+r, y); quadTo(x+3, y+3, x, y+r); quadTo(x-3, y+3, x-r, y); quadTo(x-3, y-3, x, y-r); close() }, p)
    }

    fun symbol(canvas: Canvas, bounds: RectF, symbol: CoverSymbol, color: Int, alpha: Int = 255) {
        val saved = canvas.save()
        canvas.translate(bounds.left, bounds.top)
        canvas.scale(bounds.width()/100f, bounds.height()/100f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; this.alpha = alpha; style = Paint.Style.STROKE; strokeWidth = 3.7f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        fun line(x: Float, y: Float, xx: Float, yy: Float) = canvas.drawLine(x, y, xx, yy, p)
        fun heart(x: Float = 0f, y: Float = 0f, scale: Float = 1f) {
            val id = canvas.save(); canvas.translate(x,y); canvas.scale(scale,scale)
            canvas.drawPath(Path().apply { moveTo(50f,84f); cubicTo(8f,58f,7f,19f,29f,19f); cubicTo(40f,19f,46f,27f,50f,34f); cubicTo(65f,7f,91f,20f,91f,40f); cubicTo(91f,57f,70f,74f,50f,84f); close() }, p)
            canvas.restoreToCount(id)
        }
        when (symbol) {
            CoverSymbol.HEART -> heart()
            CoverSymbol.COMPASS -> { canvas.drawCircle(50f,50f,37f,p); canvas.drawPath(Path().apply { moveTo(68f,28f); lineTo(57f,57f); lineTo(29f,71f); lineTo(42f,42f); close() },p); line(50f,8f,50f,14f); line(50f,86f,50f,92f) }
            CoverSymbol.COINS -> repeat(3) { i -> val y = 28f+i*23; canvas.drawOval(RectF(18f,y,82f,y+18),p); canvas.drawArc(RectF(18f,y+3,82f,y+26),0f,180f,false,p) }
            CoverSymbol.BOOK -> { canvas.drawPath(Path().apply { moveTo(50f,27f); quadTo(31f,14f,13f,23f); lineTo(13f,75f); quadTo(31f,66f,50f,79f); quadTo(69f,66f,87f,75f); lineTo(87f,23f); quadTo(69f,14f,50f,27f); close() },p); line(50f,27f,50f,79f); line(23f,37f,39f,40f); line(61f,40f,77f,37f); line(23f,49f,39f,52f); line(61f,52f,77f,49f) }
            CoverSymbol.LEAF -> { canvas.drawPath(Path().apply { moveTo(24f,76f); cubicTo(9f,24f,50f,24f,83f,13f); cubicTo(87f,53f,60f,82f,24f,76f); close() },p); line(16f,86f,68f,31f); line(43f,57f,42f,39f); line(43f,57f,64f,59f) }
            CoverSymbol.CASE -> { canvas.drawRoundRect(RectF(12f,34f,88f,82f),7f,7f,p); canvas.drawRoundRect(RectF(35f,18f,65f,34f),4f,4f,p); line(12f,53f,88f,53f); canvas.drawRoundRect(RectF(43f,48f,57f,60f),2f,2f,p) }
            CoverSymbol.MOON -> canvas.drawPath(Path().apply { moveTo(62f,15f); cubicTo(16f,6f,6f,67f,42f,83f); cubicTo(66f,94f,88f,78f,91f,60f); cubicTo(58f,80f,35f,35f,62f,15f); close() },p)
            CoverSymbol.MIRROR -> { canvas.drawOval(RectF(25f,10f,75f,75f),p); line(50f,75f,50f,92f); line(35f,92f,65f,92f); line(39f,45f,58f,25f); line(44f,57f,63f,37f) }
            CoverSymbol.PEOPLE -> { canvas.drawCircle(34f,29f,13f,p); canvas.drawCircle(71f,34f,11f,p); canvas.drawArc(RectF(12f,49f,57f,95f),180f,180f,false,p); canvas.drawArc(RectF(58f,52f,92f,91f),180f,180f,false,p); line(12f,72f,12f,80f); line(57f,72f,57f,80f); line(92f,71f,92f,80f) }
            CoverSymbol.RINGS -> { canvas.drawCircle(35f,57f,24f,p); canvas.drawCircle(66f,57f,24f,p); heart(44f,1f,.4f) }
            CoverSymbol.LETTER -> { canvas.drawRoundRect(RectF(10f,30f,90f,82f),5f,5f,p); line(10f,34f,50f,62f); line(50f,62f,90f,34f); heart(32f,3f,.38f) }
            CoverSymbol.BLOSSOM -> { repeat(5) { i -> val id=canvas.save(); canvas.rotate(i*72f,50f,50f); canvas.drawOval(RectF(36f,13f,64f,51f),p); canvas.restoreToCount(id) }; canvas.drawCircle(50f,50f,8f,p) }
        }
        canvas.restoreToCount(saved)
    }
}
