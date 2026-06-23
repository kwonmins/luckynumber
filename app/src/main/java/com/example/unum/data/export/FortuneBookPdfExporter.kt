package com.example.unum.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.unum.R
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FortuneBookPdfExport(
    val uri: Uri,
    val displayName: String
)

object FortuneBookPdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f
    private const val BODY_SIZE = 12.5f
    private const val BODY_LINE_HEIGHT = 20f

    fun saveToDownloads(context: Context, book: FortuneBook): FortuneBookPdfExport {
        val displayName = "${safeFileName(book.pdfTitle())}.pdf"
        val document = buildDocument(context, book)

        try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Unum")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("PDF 저장 위치를 만들 수 없습니다.")
                resolver.openOutputStream(uri)?.use { document.writeTo(it) }
                    ?: error("PDF 파일을 저장할 수 없습니다.")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                return FortuneBookPdfExport(uri, displayName)
            }

            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Unum")
                .apply { mkdirs() }
            val file = File(directory, displayName)
            FileOutputStream(file).use { document.writeTo(it) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            return FortuneBookPdfExport(uri, displayName)
        } finally {
            document.close()
        }
    }

    private fun buildDocument(context: Context, book: FortuneBook): PdfDocument {
        val document = PdfDocument()
        val writer = PdfPageWriter(context, document)
        writer.cover(book)
        writer.contentTitle(book)
        writer.section("요약", book.summary)

        if (book.bestMonth.isNotBlank() || book.riskyMonth.isNotBlank()) {
            writer.heading("흐름 타이밍")
            if (book.bestMonth.isNotBlank()) {
                writer.labelBody("추천 시기", listOfNotNull(book.bestMonth, book.bestMonthReason.ifBlank { null }))
            }
            if (book.riskyMonth.isNotBlank()) {
                writer.labelBody("주의 시기", listOfNotNull(book.riskyMonth, book.riskyMonthReason.ifBlank { null }))
            }
        }

        book.chapters.forEachIndexed { index, chapter ->
            writer.heading("${index + 1}. ${chapter.title}")
            writer.paragraph(chapter.lead)
            chapter.body.forEach(writer::paragraph)
            if (chapter.highlightQuote.isNotBlank()) {
                writer.labelBody("기억할 문장", listOf(chapter.highlightQuote))
            }
            if (chapter.actionTip.isNotEmpty()) {
                writer.labelBody("실천", chapter.actionTip.map { "- $it" })
            }
        }
        writer.footer()
        return document
    }

    private fun FortuneBook.pdfTitle(): String =
        coverTitle.takeUnless { it.isBlank() || it.looksBrokenKorean() }
            ?: when {
                coverTheme == "compatibility_couple" -> "커플 운세노트"
                coverTheme == "compatibility_crush" -> "짝사랑 운세노트"
                coverTheme == "compatibility_reunion" -> "재회 운세노트"
                bookType == FortuneBookType.COMPATIBILITY -> "궁합 운세노트"
                else -> "프리미엄 운세노트"
            }

    private fun String.looksBrokenKorean(): Boolean =
        contains("??") || contains("쨌") || contains("?댁") || contains("沅곹") || contains("吏")

    private fun safeFileName(raw: String): String {
        val date = SimpleDateFormat("yyyyMMdd_HHmm", Locale.KOREA).format(Date())
        val cleaned = raw
            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
            .replace(Regex("\\s+"), "_")
            .trim('_')
            .take(36)
            .ifBlank { "unum_premium_fortune" }
        return "${cleaned}_$date"
    }

    private data class PdfTheme(
        val kicker: String,
        val title: String,
        val accent: Int,
        val accentDark: Int,
        val accentSoft: Int,
        val background: Int,
        val surface: Int,
        val text: Int,
        val muted: Int,
        val imageResId: Int,
        val ribbon: Int,
        val coverStyle: CoverStyle
    )

    private enum class CoverStyle {
        RomanticCard,
        MatchBook,
        HanjiReport
    }

    private fun FortuneBook.pdfTheme(): PdfTheme = when {
        coverTheme == "compatibility_couple" -> matchTheme(
            kicker = "PREMIUM COUPLE NOTE",
            title = "커플 운세노트",
            accent = 0xFFEC5A8F.toInt(),
            dark = 0xFF0D6B66.toInt(),
            ribbon = 0xFFFFD166.toInt()
        )
        coverTheme == "compatibility_crush" -> matchTheme(
            kicker = "PREMIUM CRUSH NOTE",
            title = "짝사랑 운세노트",
            accent = 0xFFB99CFF.toInt(),
            dark = 0xFF303A78.toInt(),
            ribbon = 0xFFC7B8FF.toInt()
        )
        coverTheme == "compatibility_reunion" -> matchTheme(
            kicker = "PREMIUM REUNION NOTE",
            title = "재회 운세노트",
            accent = 0xFFFF8A3D.toInt(),
            dark = 0xFF8B3D1D.toInt(),
            ribbon = 0xFFFFB14A.toInt()
        )
        bookType == FortuneBookType.COMPATIBILITY -> matchTheme(
            kicker = "PREMIUM MATCH NOTE",
            title = "궁합 운세노트",
            accent = 0xFFE879F9.toInt(),
            dark = 0xFF4A1942.toInt(),
            ribbon = 0xFF78DCCA.toInt()
        )
        coverTheme == "romance" -> PdfTheme(
            kicker = "PREMIUM LOVE NOTE",
            title = coverTitle.takeUnless { it.looksBrokenKorean() }.orEmpty().ifBlank { "연애 운세노트" },
            accent = 0xFFE85D8B.toInt(),
            accentDark = 0xFF8F2F55.toInt(),
            accentSoft = 0xFFFFE7EF.toInt(),
            background = 0xFFFFF7FA.toInt(),
            surface = 0xFFFFFFFF.toInt(),
            text = 0xFF2E1B26.toInt(),
            muted = 0xFF8B5E70.toInt(),
            imageResId = R.drawable.suri_tea,
            ribbon = 0xFFF3A8C1.toInt(),
            coverStyle = CoverStyle.RomanticCard
        )
        coverTheme == "career" -> hanjiTheme("PREMIUM CAREER NOTE", "일과 진로 운세노트", R.drawable.suri_writer)
        coverTheme == "money" -> hanjiTheme("PREMIUM MONEY NOTE", "돈 운세노트", R.drawable.suri_coins)
        coverTheme == "self_esteem" -> hanjiTheme("PREMIUM SELF NOTE", "나 자신 운세노트", R.drawable.suri_scroll)
        coverTheme == "relationship" -> hanjiTheme("PREMIUM RELATION NOTE", "인간관계 운세노트", R.drawable.suri_hanbok)
        else -> hanjiTheme("PREMIUM FORTUNE NOTE", pdfTitle(), R.drawable.suri_scroll)
    }

    private fun matchTheme(kicker: String, title: String, accent: Int, dark: Int, ribbon: Int): PdfTheme =
        PdfTheme(
            kicker = kicker,
            title = title,
            accent = accent,
            accentDark = dark,
            accentSoft = 0xFFFFE8F2.toInt(),
            background = 0xFFFFF6FA.toInt(),
            surface = 0xFFFFFBF3.toInt(),
            text = 0xFF24111F.toInt(),
            muted = 0xFF8A6078.toInt(),
            imageResId = R.drawable.suri_reader_compatibility,
            ribbon = ribbon,
            coverStyle = CoverStyle.MatchBook
        )

    private fun hanjiTheme(kicker: String, title: String, imageResId: Int): PdfTheme =
        PdfTheme(
            kicker = kicker,
            title = title,
            accent = 0xFFB36B43.toInt(),
            accentDark = 0xFF5B341F.toInt(),
            accentSoft = 0xFFFFEBD7.toInt(),
            background = 0xFFFFFBF1.toInt(),
            surface = 0xFFFFF7E8.toInt(),
            text = 0xFF2F261D.toInt(),
            muted = 0xFF7D6654.toInt(),
            imageResId = imageResId,
            ribbon = 0xFFE1A35F.toInt(),
            coverStyle = CoverStyle.HanjiReport
        )

    private class PdfPageWriter(
        private val context: Context,
        private val document: PdfDocument
    ) {
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF374151.toInt()
            textSize = BODY_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6B7280.toInt()
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        private var theme: PdfTheme? = null
        private var pageNumber = 0
        private var page: PdfDocument.Page = newPage(0xFFFFFBF3.toInt())
        private var y = MARGIN
        private var finished = false

        fun cover(book: FortuneBook) {
            val pdfTheme = book.pdfTheme()
            theme = pdfTheme
            page.canvas.drawColor(pdfTheme.background)
            when (pdfTheme.coverStyle) {
                CoverStyle.RomanticCard -> drawRomanticCover(book, pdfTheme)
                CoverStyle.MatchBook -> drawMatchCover(book, pdfTheme)
                CoverStyle.HanjiReport -> drawHanjiCover(book, pdfTheme)
            }
            finishCurrentPage(showFooter = false)
            page = newPage(pdfTheme.background)
            y = MARGIN
        }

        fun contentTitle(book: FortuneBook) {
            val pdfTheme = theme ?: book.pdfTheme()
            drawContentHeader(book, pdfTheme)
        }

        fun section(title: String, body: String) {
            heading(title)
            paragraph(body)
        }

        fun heading(text: String) {
            val pdfTheme = theme ?: return
            ensureSpace(54f)
            val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 32f)
            drawRound(rect, 14f, pdfTheme.accentSoft, null, 0f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentDark
                textSize = 15.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            page.canvas.drawText(text, MARGIN + 16f, y + 21f, paint)
            y += 44f
        }

        fun paragraph(text: String) {
            if (text.isBlank()) return
            val pdfTheme = theme ?: return
            bodyPaint.color = pdfTheme.text
            drawWrapped(text.trim(), bodyPaint, BODY_LINE_HEIGHT, PAGE_WIDTH - MARGIN * 2)
            line(7f)
        }

        fun labelBody(label: String, lines: List<String>) {
            val pdfTheme = theme ?: return
            ensureSpace(64f)
            val top = y
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentDark
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            drawRound(RectF(MARGIN, top, PAGE_WIDTH - MARGIN, top + 30f), 12f, 0xFFFFFFFF.toInt(), pdfTheme.accentSoft, 1.4f)
            page.canvas.drawText(label, MARGIN + 14f, top + 20f, paint)
            y = top + 42f
            lines.forEach(::paragraph)
        }

        fun footer() {
            if (!finished) finishCurrentPage(showFooter = true)
        }

        private fun drawRomanticCover(book: FortuneBook, pdfTheme: PdfTheme) {
            drawSoftCircles(pdfTheme)
            drawCoverCard(RectF(58f, 74f, 537f, 740f), pdfTheme, round = 28f)
            drawRibbon(440f, 74f, pdfTheme.ribbon)
            drawImage(pdfTheme.imageResId, RectF(180f, 100f, 415f, 300f))
            drawCentered(pdfTheme.kicker, 328f, 10f, 0xFF9D5571.toInt(), Typeface.BOLD)
            drawCentered(pdfTheme.title, 382f, 32f, pdfTheme.text, Typeface.BOLD)
            drawCentered(book.coverSubtitle.cleanPdfText().ifBlank { "내 생년월일로 제작" }, 422f, 11f, pdfTheme.muted, Typeface.NORMAL)
            drawSeal(book.displayNumber(), 472f, pdfTheme)
            drawSummaryBox(book, RectF(92f, 554f, 503f, 680f), pdfTheme)
        }

        private fun drawMatchCover(book: FortuneBook, pdfTheme: PdfTheme) {
            val cover = RectF(108f, 74f, 487f, 746f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(cover.left, cover.top, cover.right, cover.bottom, pdfTheme.accentDark, 0xFF2B0D2D.toInt(), Shader.TileMode.CLAMP)
            }
            page.canvas.drawRoundRect(cover, 28f, 28f, paint)
            drawRound(cover, 28f, 0x00000000, pdfTheme.accent, 2.4f)
            drawRound(RectF(126f, 94f, 469f, 726f), 20f, 0x00000000, 0x66FFFFFF, 1.1f)
            drawRibbon(400f, 74f, pdfTheme.ribbon)
            drawImage(pdfTheme.imageResId, RectF(194f, 128f, 401f, 300f))
            drawCentered(pdfTheme.kicker, 342f, 9.5f, 0xFFEECDE4.toInt(), Typeface.BOLD)
            drawCentered(pdfTheme.title, 410f, 38f, 0xFFFFFFFF.toInt(), Typeface.BOLD)
            drawCentered(book.coverSubtitle.cleanPdfText().ifBlank { "두 사람의 숫자의 궁합을 읽어볼게요" }, 456f, 11f, 0xFFEBCDDD.toInt(), Typeface.NORMAL)
            drawSeal(book.displayNumber(), 526f, pdfTheme.copy(text = 0xFFFFFFFF.toInt()))
            drawSummaryBox(book, RectF(146f, 610f, 449f, 702f), pdfTheme.copy(surface = 0x24FFFFFF, text = 0xFFFFFFFF.toInt(), muted = 0xFFEBCDDD.toInt()))
        }

        private fun drawHanjiCover(book: FortuneBook, pdfTheme: PdfTheme) {
            drawHanjiPattern(pdfTheme.background)
            val frame = RectF(62f, 72f, 533f, 742f)
            drawRound(frame, 24f, pdfTheme.surface, 0xFFD8B28A.toInt(), 2f)
            drawRound(RectF(82f, 92f, 513f, 722f), 18f, 0x00000000, 0x44B36B43, 1f)
            drawImage(pdfTheme.imageResId, RectF(180f, 116f, 415f, 310f))
            drawCentered(pdfTheme.kicker, 354f, 9.5f, pdfTheme.muted, Typeface.BOLD)
            drawCentered(pdfTheme.title, 408f, 34f, pdfTheme.text, Typeface.BOLD)
            drawCentered(book.coverSubtitle.cleanPdfText().ifBlank { "내 생년월일로 제작" }, 450f, 11f, pdfTheme.muted, Typeface.NORMAL)
            drawSeal(book.displayNumber(), 508f, pdfTheme)
            drawSummaryBox(book, RectF(100f, 596f, 495f, 695f), pdfTheme)
        }

        private fun drawContentHeader(book: FortuneBook, pdfTheme: PdfTheme) {
            ensureSpace(172f)
            val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 132f)
            drawRound(rect, 22f, pdfTheme.surface, pdfTheme.accentSoft, 1.6f)
            drawImage(pdfTheme.imageResId, RectF(MARGIN + 18f, y + 18f, MARGIN + 108f, y + 108f))
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.text
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.muted
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            page.canvas.drawText(pdfTheme.title, MARGIN + 128f, y + 44f, titlePaint)
            val created = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(book.createdAt))
            page.canvas.drawText("${book.bookType.pdfLabel()} · 제작일 $created · 핵심 숫자 ${book.displayNumber()}", MARGIN + 130f, y + 70f, metaPaint)
            val concern = book.concernText.cleanPdfText().ifBlank { "내 생년월일로 제작" }
            drawWrapped("질문: $concern", metaPaint, 15f, PAGE_WIDTH - MARGIN * 2 - 130f, MARGIN + 130f, y + 94f)
            y += 156f
        }

        private fun drawSummaryBox(book: FortuneBook, rect: RectF, pdfTheme: PdfTheme) {
            drawRound(rect, 18f, pdfTheme.surface, pdfTheme.accentSoft, 1.2f)
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentDark
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.text
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            page.canvas.drawText("요약", rect.left + 18f, rect.top + 28f, labelPaint)
            drawWrapped(book.summary.cleanPdfText().take(180), textPaint, 15f, rect.width() - 36f, rect.left + 18f, rect.top + 52f)
        }

        private fun drawSeal(number: Int, centerY: Float, pdfTheme: PdfTheme) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = pdfTheme.accent
            }
            page.canvas.drawCircle(PAGE_WIDTH / 2f, centerY, 28f, paint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.text
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            page.canvas.drawText(number.toString(), PAGE_WIDTH / 2f, centerY + 8f, textPaint)
        }

        private fun drawRibbon(left: Float, top: Float, color: Int) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
            page.canvas.drawRoundRect(RectF(left, top, left + 24f, top + 112f), 8f, 8f, paint)
            page.canvas.drawRect(left, top + 86f, left + 24f, top + 112f, paint)
        }

        private fun drawImage(resId: Int, rect: RectF) {
            val bitmap = BitmapFactory.decodeResource(context.resources, resId) ?: return
            drawBitmapFit(bitmap, rect)
        }

        private fun drawBitmapFit(bitmap: Bitmap, rect: RectF) {
            val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val dstRatio = rect.width() / rect.height()
            val dst = RectF(rect)
            if (srcRatio > dstRatio) {
                val height = rect.width() / srcRatio
                dst.top = rect.centerY() - height / 2f
                dst.bottom = dst.top + height
            } else {
                val width = rect.height() * srcRatio
                dst.left = rect.centerX() - width / 2f
                dst.right = dst.left + width
            }
            page.canvas.drawBitmap(bitmap, null, dst, Paint(Paint.ANTI_ALIAS_FLAG))
        }

        private fun drawSoftCircles(pdfTheme: PdfTheme) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pdfTheme.accentSoft }
            page.canvas.drawCircle(92f, 120f, 78f, paint)
            page.canvas.drawCircle(512f, 690f, 94f, paint)
        }

        private fun drawHanjiPattern(color: Int) {
            page.canvas.drawColor(color)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = 0x19B36B43
                strokeWidth = 1f
            }
            var x = 34f
            while (x < PAGE_WIDTH) {
                page.canvas.drawLine(x, 0f, x - 90f, PAGE_HEIGHT.toFloat(), paint)
                x += 46f
            }
        }

        private fun drawCoverCard(rect: RectF, pdfTheme: PdfTheme, round: Float) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(rect.left, rect.top, rect.right, rect.bottom, 0xFFFFFFFF.toInt(), pdfTheme.accentSoft, Shader.TileMode.CLAMP)
            }
            page.canvas.drawRoundRect(rect, round, round, paint)
            drawRound(rect, round, 0x00000000, pdfTheme.accentSoft, 2f)
        }

        private fun drawRound(rect: RectF, radius: Float, fill: Int, stroke: Int?, strokeWidth: Float) {
            if (fill != 0x00000000) {
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = fill
                    style = Paint.Style.FILL
                }
                page.canvas.drawRoundRect(rect, radius, radius, fillPaint)
            }
            if (stroke != null && strokeWidth > 0f) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = stroke
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                }
                page.canvas.drawRoundRect(rect, radius, radius, strokePaint)
            }
        }

        private fun drawCentered(text: String, baseline: Float, size: Float, color: Int, style: Int) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                typeface = Typeface.create(Typeface.DEFAULT, style)
                textAlign = Paint.Align.CENTER
            }
            page.canvas.drawText(text.cleanPdfText(), PAGE_WIDTH / 2f, baseline, paint)
        }

        private fun drawWrapped(
            text: String,
            paint: Paint,
            lineHeight: Float,
            maxWidth: Float,
            startX: Float = MARGIN,
            startY: Float? = null
        ) {
            if (startY != null) y = startY
            wrap(text.cleanPdfText(), paint, maxWidth).forEach { line ->
                ensureSpace(lineHeight)
                page.canvas.drawText(line, startX, y, paint)
                y += lineHeight
            }
        }

        private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
            return text.split('\n').flatMap { raw ->
                val lines = mutableListOf<String>()
                var remaining = raw.trim()
                if (remaining.isEmpty()) return@flatMap listOf("")
                while (remaining.isNotEmpty()) {
                    var count = paint.breakText(remaining, true, maxWidth, null)
                    if (count <= 0) count = 1
                    if (count < remaining.length) {
                        val lastSpace = remaining.substring(0, count).lastIndexOf(' ')
                        if (lastSpace > 8) count = lastSpace
                    }
                    lines += remaining.substring(0, count).trim()
                    remaining = remaining.substring(count).trimStart()
                }
                lines
            }
        }

        private fun ensureSpace(required: Float) {
            val pdfTheme = theme
            if (y + required <= PAGE_HEIGHT - MARGIN) return
            finishCurrentPage(showFooter = true)
            page = newPage(pdfTheme?.background ?: 0xFFFFFBF3.toInt())
            y = MARGIN
        }

        private fun line(height: Float) {
            y += height
        }

        private fun newPage(background: Int): PdfDocument.Page {
            finished = false
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val newPage = document.startPage(info)
            newPage.canvas.drawColor(background)
            return newPage
        }

        private fun finishCurrentPage(showFooter: Boolean) {
            if (showFooter) {
                val pdfTheme = theme
                mutedPaint.color = pdfTheme?.muted ?: 0xFF6B7280.toInt()
                page.canvas.drawText("UNUM · $pageNumber", MARGIN, PAGE_HEIGHT - 24f, mutedPaint)
            }
            document.finishPage(page)
            finished = true
        }
    }

    private fun FortuneBook.displayNumber(): Int =
        relationshipNumber ?: destiny

    private fun FortuneBookType.pdfLabel(): String = when (this) {
        FortuneBookType.PERSONAL -> "프리미엄 운세노트"
        FortuneBookType.COMPATIBILITY -> "프리미엄 궁합노트"
    }

    private fun String.cleanPdfText(): String =
        takeUnless { it.looksBrokenKorean() }.orEmpty()
}
