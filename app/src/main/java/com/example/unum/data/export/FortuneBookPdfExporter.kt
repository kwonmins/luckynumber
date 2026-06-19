package com.example.unum.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
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
    private const val BODY_SIZE = 13f
    private const val BODY_LINE_HEIGHT = 21f

    fun saveToDownloads(context: Context, book: FortuneBook): FortuneBookPdfExport {
        val displayName = "${safeFileName(book.coverTitle.ifBlank { "unum_premium_fortune" })}.pdf"
        val document = buildDocument(book)

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
                    ?: error("PDF 파일을 열 수 없습니다.")
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

    private fun buildDocument(book: FortuneBook): PdfDocument {
        val document = PdfDocument()
        val writer = PdfPageWriter(document)
        writer.title(book.coverTitle.ifBlank { "프리미엄 운세" })
        writer.meta(book)
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

    private class PdfPageWriter(private val document: PdfDocument) {
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF111827.toInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF7C2D12.toInt()
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF92400E.toInt()
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF374151.toInt()
            textSize = BODY_SIZE
        }
        private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6B7280.toInt()
            textSize = 10f
        }
        private var pageNumber = 0
        private var page: PdfDocument.Page = newPage()
        private var y = MARGIN

        fun title(text: String) {
            drawWrapped(text, titlePaint, 28f)
            line(18f)
        }

        fun meta(book: FortuneBook) {
            val type = when (book.bookType) {
                FortuneBookType.PERSONAL -> "프리미엄 운세노트"
                FortuneBookType.COMPATIBILITY -> "프리미엄 궁합노트"
            }
            val created = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(book.createdAt))
            drawWrapped("$type · 생성일 $created · 운명수 ${book.destiny}", mutedPaint, 16f)
            if (book.concernText.isNotBlank()) {
                drawWrapped("질문: ${book.concernText}", mutedPaint, 16f)
            }
            line(16f)
        }

        fun section(title: String, body: String) {
            heading(title)
            paragraph(body)
        }

        fun heading(text: String) {
            ensureSpace(42f)
            drawWrapped(text, headingPaint, 23f)
            line(6f)
        }

        fun paragraph(text: String) {
            if (text.isBlank()) return
            drawWrapped(text.trim(), bodyPaint, BODY_LINE_HEIGHT)
            line(8f)
        }

        fun labelBody(label: String, lines: List<String>) {
            ensureSpace(48f)
            drawWrapped(label, labelPaint, 17f)
            lines.forEach(::paragraph)
        }

        fun footer() {
            finishPage()
        }

        private fun drawWrapped(text: String, paint: Paint, lineHeight: Float) {
            val maxWidth = PAGE_WIDTH - MARGIN * 2
            wrap(text, paint, maxWidth).forEach { line ->
                ensureSpace(lineHeight)
                page.canvas.drawText(line, MARGIN, y, paint)
                y += lineHeight
            }
        }

        private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
            return text.split('\n').flatMap { paragraph ->
                val words = paragraph.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                if (words.isEmpty()) return@flatMap listOf("")
                val lines = mutableListOf<String>()
                var current = ""
                words.forEach { word ->
                    val candidate = if (current.isBlank()) word else "$current $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = candidate
                    } else {
                        if (current.isNotBlank()) lines += current
                        current = word
                    }
                }
                if (current.isNotBlank()) lines += current
                lines
            }
        }

        private fun ensureSpace(required: Float) {
            if (y + required <= PAGE_HEIGHT - MARGIN) return
            finishPage()
            page = newPage()
            y = MARGIN
        }

        private fun line(height: Float) {
            y += height
        }

        private fun newPage(): PdfDocument.Page {
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val newPage = document.startPage(info)
            newPage.canvas.drawColor(0xFFFFFBF3.toInt())
            return newPage
        }

        private fun finishPage() {
            page.canvas.drawText("UNUM · $pageNumber", MARGIN, PAGE_HEIGHT - 24f, mutedPaint)
            document.finishPage(page)
        }
    }
}
