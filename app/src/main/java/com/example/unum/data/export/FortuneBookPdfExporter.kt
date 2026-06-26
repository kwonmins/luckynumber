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
import com.example.unum.data.model.BookSpecs
import com.example.unum.data.model.BookThemeId
import com.example.unum.data.model.BookThemeSpecs
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.resolvedThemeId
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
            if (chapter.actionTip.isNotEmpty()) {
                writer.labelBody("실천", chapter.actionTip.map { "- $it" })
            }
        }
        writer.footer()
        return document
    }

    private fun FortuneBook.pdfTitle(): String =
        coverTitle.takeUnless { it.isBlank() || it.looksBrokenKorean() }
            ?: BookSpecs.forTheme(resolvedThemeId())?.coverTitle
            ?: if (bookType == FortuneBookType.COMPATIBILITY) "궁합 운세노트" else "프리미엄 운세노트"

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
        val shortName: String,
        val accent: Int,
        val accentDark: Int,
        val accentSoft: Int,
        val background: Int,
        val surface: Int,
        val text: Int,
        val muted: Int,
        val imageResId: Int,
        val ribbon: Int,
        val foil: Int,
        val stitch: Int,
        val coverTop: Int,
        val coverMid: Int,
        val coverBottom: Int,
        val tint: Int,
        val page: Int,
        val pageTop: Int,
        val edge: Int,
        val coverStyle: CoverStyle
    )

    private enum class CoverStyle {
        RomanticCard,
        MatchBook,
        HanjiReport
    }

    private fun FortuneBook.pdfTheme(): PdfTheme {
        val themeId = resolvedThemeId()
        val theme = BookThemeSpecs.get(themeId)
        val spec = BookSpecs.forBook(this)
        return leatherTheme(
            shortName = theme.displayName,
            kicker = spec.coverKicker,
            title = pdfCoverTitle(spec.coverTitle),
            accent = theme.pdfAccentColor,
            accentDark = theme.pdfAccentDarkColor,
            ribbon = theme.pdfRibbonColor,
            foil = theme.pdfFoilColor,
            stitch = theme.pdfStitchColor,
            coverTop = theme.pdfCoverTopColor,
            coverMid = theme.pdfCoverMidColor,
            coverBottom = theme.pdfCoverBottomColor,
            tint = theme.pdfTintColor,
            imageResId = pdfImageRes(themeId),
            page = theme.pdfPageColor,
            pageTop = theme.pdfPageTopColor,
            edge = theme.pdfEdgeColor
        )
    }

    private fun FortuneBook.pdfCoverTitle(defaultTitle: String): String {
        val themeId = resolvedThemeId()
        return if (themeId == BookThemeId.ROMANCE || themeId == BookThemeId.CALM) {
            coverTitle.takeUnless { it.looksBrokenKorean() }.orEmpty().ifBlank { defaultTitle }
        } else {
            defaultTitle
        }
    }

    private fun pdfImageRes(themeId: BookThemeId): Int = when (themeId) {
        BookThemeId.CAREER -> R.drawable.suri_anim_writer_hero
        BookThemeId.MONEY -> R.drawable.suri_reader_money_cutout
        BookThemeId.RELATIONSHIP -> R.drawable.suri_anim_consult_01
        BookThemeId.SELF_ESTEEM -> R.drawable.suri_anim_numbers_hero
        BookThemeId.COMPATIBILITY,
        BookThemeId.COMPATIBILITY_COUPLE,
        BookThemeId.COMPATIBILITY_CRUSH,
        BookThemeId.COMPATIBILITY_REUNION -> R.drawable.suri_reader_compatibility
        BookThemeId.ROMANCE,
        BookThemeId.CALM -> R.drawable.suri_reader_romance
    }

    private fun leatherTheme(
        shortName: String,
        kicker: String,
        title: String,
        accent: Int,
        accentDark: Int,
        ribbon: Int,
        foil: Int,
        stitch: Int,
        coverTop: Int,
        coverMid: Int,
        coverBottom: Int,
        tint: Int,
        imageResId: Int,
        page: Int = 0xFFFFFDF8.toInt(),
        pageTop: Int = 0xFFFFFAF1.toInt(),
        edge: Int = 0xFFE6DAC9.toInt()
    ): PdfTheme =
        PdfTheme(
            kicker = kicker,
            title = title,
            shortName = shortName,
            accent = accent,
            accentDark = accentDark,
            accentSoft = tint,
            background = coverBottom,
            surface = 0xFFFFFDF8.toInt(),
            text = 0xFF111827.toInt(),
            muted = 0xFF6B7280.toInt(),
            imageResId = imageResId,
            ribbon = ribbon,
            foil = foil,
            stitch = stitch,
            coverTop = coverTop,
            coverMid = coverMid,
            coverBottom = coverBottom,
            tint = tint,
            page = page,
            pageTop = pageTop,
            edge = edge,
            coverStyle = CoverStyle.MatchBook
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
        private val contentLeft = 82f
        private val contentRight = PAGE_WIDTH - 82f
        private val contentBottom = PAGE_HEIGHT - 64f

        fun cover(book: FortuneBook) {
            val pdfTheme = book.pdfTheme()
            theme = pdfTheme
            page.canvas.drawColor(pdfTheme.coverBottom)
            drawPremiumBookCover(book, pdfTheme)
            finishCurrentPage(showFooter = false)
            page = newPage(pdfTheme.coverBottom)
            drawPaperPageFrame(pdfTheme)
            y = 82f
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
            val rect = RectF(contentLeft, y, contentRight, y + 32f)
            drawRound(rect, 10f, 0x00FFFFFF, pdfTheme.accentSoft, 1.2f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentDark
                textSize = 14.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            page.canvas.drawText("✦ $text", contentLeft + 14f, y + 21f, paint)
            y += 44f
        }

        fun paragraph(text: String) {
            if (text.isBlank()) return
            val pdfTheme = theme ?: return
            bodyPaint.color = pdfTheme.text
            drawWrapped(text.trim(), bodyPaint, BODY_LINE_HEIGHT + 1f, contentRight - contentLeft - 18f, contentLeft + 9f)
            line(10f)
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
            drawRound(RectF(contentLeft, top, contentRight, top + 30f), 12f, 0xFFFFFCF7.toInt(), pdfTheme.accentSoft, 1.2f)
            page.canvas.drawText(label, contentLeft + 14f, top + 20f, paint)
            y = top + 42f
            lines.forEach(::paragraph)
        }

        fun footer() {
            if (!finished) finishCurrentPage(showFooter = true)
        }

        private fun drawPremiumBookCover(book: FortuneBook, pdfTheme: PdfTheme) {
            val cover = RectF(44f, 18f, 551f, 824f)
            val canvas = page.canvas
            val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    cover.left,
                    cover.top,
                    cover.right,
                    cover.bottom,
                    intArrayOf(pdfTheme.coverTop, pdfTheme.coverMid, pdfTheme.coverBottom),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(cover, 8f, 8f, coverPaint)
            drawRound(cover, 8f, 0x00000000, 0x52000000, 1.4f)

            val texturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x08FFFFFF
                strokeWidth = 1f
            }
            repeat(22) { index ->
                val yLine = cover.top + cover.height() * (index + 1) / 23f
                val wave = if (index % 2 == 0) 18f else -10f
                canvas.drawLine(cover.left + 32f, yLine, cover.right - 32f, yLine + wave, texturePaint)
            }
            texturePaint.color = 0x30000000
            repeat(18) { index ->
                val yLine = cover.top + cover.height() * (index + 1) / 19f
                canvas.drawLine(cover.left + 36f, yLine + 6f, cover.right - 34f, yLine - 5f, texturePaint)
            }

            drawRound(RectF(cover.left + 16f, cover.top + 16f, cover.right - 16f, cover.bottom - 16f), 8f, 0x00000000, pdfTheme.foil.withAlpha(0.72f), 1.2f)
            drawRound(RectF(cover.left + 24f, cover.top + 24f, cover.right - 24f, cover.bottom - 24f), 6f, 0x00000000, pdfTheme.foil.withAlpha(0.30f), 1f)
            drawRound(RectF(cover.left, cover.centerY() - 190f, cover.left + 18f, cover.centerY() + 190f), 8f, 0x38000000, null, 0f)

            val stitchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pdfTheme.stitch.withAlpha(0.72f) }
            repeat(34) { index ->
                val dotY = cover.top + 78f + index * 18f
                canvas.drawCircle(cover.left + 36f, dotY, 1.35f, stitchPaint)
            }
            val spinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pdfTheme.foil.withAlpha(0.28f) }
            canvas.drawRect(cover.left + 52f, cover.centerY() - 210f, cover.left + 53.4f, cover.centerY() + 210f, spinePaint)

            val ribbon = RectF(cover.right - 56f, cover.top + 16f, cover.right - 40f, cover.top + 112f)
            val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(ribbon.left, ribbon.top, ribbon.left, ribbon.bottom, pdfTheme.ribbon, pdfTheme.accentDark, Shader.TileMode.CLAMP)
            }
            canvas.drawRoundRect(ribbon, 4f, 4f, ribbonPaint)

            val left = cover.left + 70f
            val top = cover.top + 54f
            val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.foil
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.foil
                textSize = 43f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(pdfTheme.kicker, left, top, captionPaint)
            val mainTitle = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                listOf("수리의", "궁합노트")
            } else {
                listOf("수리의", "운세노트")
            }
            mainTitle.forEachIndexed { index, line ->
                canvas.drawText(line, left, top + 68f + index * 52f, titlePaint)
            }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.foil.withAlpha(0.86f)
                strokeWidth = 2.2f
            }
            canvas.drawLine(left, top + 185f, left + 132f, top + 185f, linePaint)

            val lowerTop = cover.bottom - 226f
            val lowerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF8FAFC.toInt()
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val lowerBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFCBD5E1.toInt()
                textSize = 10.8f
            }
            val lowerMutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF94A3B8.toInt()
                textSize = 10.5f
            }
            canvas.drawText(book.coverTitle.cleanPdfText().ifBlank { pdfTheme.shortName }, left, lowerTop, lowerTitlePaint)
            canvas.drawText(book.coverSubtitle.cleanPdfText().ifBlank { "나만의 맞춤 비책" }, left, lowerTop + 30f, lowerBodyPaint)
            canvas.drawText("운명수 ${book.displayNumber()} · ${pdfTheme.shortName}", left, lowerTop + 58f, lowerMutedPaint)
            drawCoverSeal(book.displayNumber(), left + 38f, lowerTop + 122f, pdfTheme)

            val stamp = RectF(cover.right - 74f, cover.bottom - 72f, cover.right - 28f, cover.bottom - 26f)
            drawRound(stamp, 8f, 0xEFB91C1C.toInt(), null, 0f)
            val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 13f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("수리", stamp.centerX(), stamp.centerY() + 5f, stampPaint)
        }

        private fun drawPaperPageFrame(pdfTheme: PdfTheme) {
            val outer = RectF(44f, 18f, 551f, 824f)
            val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    outer.left,
                    outer.top,
                    outer.right,
                    outer.bottom,
                    intArrayOf(pdfTheme.coverTop, pdfTheme.coverMid, pdfTheme.coverBottom),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            page.canvas.drawRoundRect(outer, 8f, 8f, outerPaint)
            drawRound(outer, 8f, 0x00000000, 0x4D000000, 1.2f)

            val paper = RectF(62f, 38f, 533f, 804f)
            val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    paper.left,
                    paper.top,
                    paper.left,
                    paper.bottom,
                    intArrayOf(pdfTheme.pageTop, pdfTheme.page, 0xFFF4EBD9.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            page.canvas.drawRoundRect(paper, 8f, 8f, paperPaint)
            drawRound(paper, 8f, 0x00000000, pdfTheme.edge, 1.1f)

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.edge.withAlpha(0.32f)
                strokeWidth = 0.8f
            }
            var lineY = paper.top + 74f
            while (lineY < paper.bottom - 32f) {
                page.canvas.drawLine(paper.left + 24f, lineY, paper.right - 24f, lineY - 3f, linePaint)
                lineY += 30f
            }
            drawRound(RectF(outer.right - 20f, outer.top + 16f, outer.right - 2f, outer.bottom - 16f), 0f, 0x28000000, null, 0f)
            drawRound(RectF(outer.left + 2f, outer.top + 16f, outer.left + 12f, outer.bottom - 16f), 0f, 0x26FFFFFF, null, 0f)
        }

        private fun drawCoverSeal(number: Int, cx: Float, cy: Float, pdfTheme: PdfTheme) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x24000000 }
            page.canvas.drawCircle(cx, cy, 38f, fillPaint)
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.foil.withAlpha(0.90f)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            page.canvas.drawCircle(cx, cy, 38f, ringPaint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.foil
                textSize = 24f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            page.canvas.drawText(number.toString(), cx, cy + 9f, textPaint)
        }

        private fun drawRomanticCover(book: FortuneBook, pdfTheme: PdfTheme) {
            drawSoftCircles(pdfTheme)
            drawLetterCoverBase(pdfTheme)
            drawPostStamp(pdfTheme, 418f, 104f)
            drawImage(pdfTheme.imageResId, RectF(332f, 134f, 498f, 330f))
            drawCoverLetterText(book, pdfTheme, "수리가 보내는 연애 편지")
            drawSeal(book.displayNumber(), 506f, pdfTheme)
            drawSummaryBox(book, RectF(88f, 574f, 507f, 704f), pdfTheme)
        }

        private fun drawMatchCover(book: FortuneBook, pdfTheme: PdfTheme) {
            drawSoftCircles(pdfTheme)
            drawLetterCoverBase(pdfTheme)
            drawPostStamp(pdfTheme, 418f, 104f)
            drawImage(pdfTheme.imageResId, RectF(328f, 126f, 504f, 356f))
            drawCoverLetterText(book, pdfTheme, "수리가 보내는 궁합 편지")
            drawSeal(book.displayNumber(), 510f, pdfTheme)
            drawSummaryBox(book, RectF(88f, 578f, 507f, 710f), pdfTheme)
        }

        private fun drawHanjiCover(book: FortuneBook, pdfTheme: PdfTheme) {
            drawHanjiPattern(pdfTheme.background)
            drawLetterCoverBase(pdfTheme)
            drawPostStamp(pdfTheme, 416f, 104f)
            drawImage(pdfTheme.imageResId, RectF(330f, 132f, 498f, 340f))
            drawCoverLetterText(book, pdfTheme, "수리가 보내는 운세 편지")
            drawSeal(book.displayNumber(), 506f, pdfTheme)
            drawSummaryBox(book, RectF(88f, 574f, 507f, 704f), pdfTheme)
        }

        private fun drawContentHeader(book: FortuneBook, pdfTheme: PdfTheme) {
            ensureSpace(168f)
            val top = y
            val rect = RectF(contentLeft, top, contentRight, top + 138f)
            drawRound(rect, 8f, 0x8FFFFFFF.toInt(), pdfTheme.accent.withAlpha(0.18f), 1.1f)
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.text
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 12f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accent
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.muted
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            page.canvas.drawCircle(contentLeft + 20f, top + 30f, 13f, circlePaint)
            page.canvas.drawText("1", contentLeft + 20f, top + 35f, numberPaint)
            page.canvas.drawText(
                if (book.bookType == FortuneBookType.COMPATIBILITY) "두 사람 궁합 해석 비책" else "상황별 고민 해결 비책",
                contentLeft + 45f,
                top + 27f,
                titlePaint
            )
            val created = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(book.createdAt))
            page.canvas.drawText("${book.concernTopic.ifBlank { book.bookType.pdfLabel() }} · 제작일 $created", contentLeft + 45f, top + 48f, metaPaint)
            page.canvas.drawText("핵심 숫자 ${book.displayNumber()} · ${pdfTheme.shortName}", contentLeft + 45f, top + 68f, metaPaint)
            val concern = book.concernText.cleanPdfText().ifBlank { "내 생년월일로 제작" }
            drawWrapped("질문: $concern", metaPaint, 15f, contentRight - contentLeft - 28f, contentLeft + 14f, top + 98f)
            y = top + 160f
        }

        private fun drawSummaryBox(book: FortuneBook, rect: RectF, pdfTheme: PdfTheme) {
            drawRound(rect, 18f, pdfTheme.surface, pdfTheme.accentSoft, 1.2f)
            drawLetterLines(rect, pdfTheme)
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

        private fun drawLetterCoverBase(pdfTheme: PdfTheme) {
            val envelope = RectF(54f, 96f, 541f, 746f)
            drawRound(envelope, 26f, pdfTheme.accentSoft, null, 0f)
            val flapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x55FFFFFF }
            val canvas = page.canvas
            val path = android.graphics.Path().apply {
                moveTo(envelope.left + 18f, envelope.top + 6f)
                lineTo(envelope.centerX(), envelope.top + 178f)
                lineTo(envelope.right - 18f, envelope.top + 6f)
                close()
            }
            canvas.drawPath(path, flapPaint)

            val paper = RectF(78f, 126f, 517f, 720f)
            drawRound(paper, 22f, pdfTheme.surface, 0x33FFFFFF, 1f)
            drawLetterLines(paper, pdfTheme)
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accent
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(7f, 8f), 0f)
            }
            canvas.drawRoundRect(RectF(92f, 142f, 503f, 704f), 16f, 16f, border)
        }

        private fun drawCoverLetterText(book: FortuneBook, pdfTheme: PdfTheme, salutation: String) {
            val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.muted
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.text
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.muted
                textSize = 12f
            }
            page.canvas.drawText(salutation, 112f, 210f, smallPaint)
            page.canvas.drawText(pdfTheme.kicker, 112f, 246f, smallPaint)
            drawWrapped(pdfTheme.title, titlePaint, 38f, 250f, 112f, 300f)
            val subtitle = book.coverSubtitle.cleanPdfText().ifBlank {
                if (book.bookType == FortuneBookType.COMPATIBILITY) "두 사람의 숫자의 궁합을 읽어볼게요" else "내 생년월일로 제작"
            }
            drawWrapped(subtitle, bodyPaint, 18f, 260f, 114f, 390f)
        }

        private fun drawPostStamp(pdfTheme: PdfTheme, left: Float, top: Float) {
            val rect = RectF(left, top, left + 58f, top + 72f)
            drawRound(rect, 8f, 0xFFFFFFFF.toInt(), pdfTheme.accentSoft, 1.4f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accent
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
            }
            page.canvas.drawCircle(rect.centerX(), rect.centerY() - 4f, 15f, paint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentDark
                textSize = 9f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            page.canvas.drawText("UNUM", rect.centerX(), rect.bottom - 14f, textPaint)
        }

        private fun drawLetterLines(rect: RectF, pdfTheme: PdfTheme) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pdfTheme.accentSoft
                strokeWidth = 0.8f
            }
            var lineY = rect.top + 64f
            while (lineY < rect.bottom - 20f) {
                page.canvas.drawLine(rect.left + 22f, lineY, rect.right - 22f, lineY, paint)
                lineY += 30f
            }
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
            if (y + required <= contentBottom) return
            finishCurrentPage(showFooter = true)
            page = newPage(pdfTheme?.coverBottom ?: 0xFFFFFBF3.toInt())
            pdfTheme?.let(::drawPaperPageFrame)
            y = 82f
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

    private fun Int.withAlpha(alpha: Float): Int {
        val alphaByte = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        return (alphaByte shl 24) or (this and 0x00FFFFFF)
    }
}
