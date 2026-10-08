package com.example.unum

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unum.data.model.*
import com.example.unum.graphics.MoonlightBookPainter
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.UnumTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class MoonlightCoverUiTest {
    @get:Rule val compose = createComposeRule()
    private fun book(spec: BookSpec) = FortuneBook(
        bookId=spec.id, code="1234", destiny=1, early=2, middle=3, late=4,
        concernTopic=spec.bookLabel, concernText="", coverTitle="나에게 맞는 선택을 알아보는 시간",
        coverSubtitle="", summary="", bookType=spec.bookType, chapters=emptyList(), createdAt=0, coverTheme=spec.themeId.key
    )

    @Test fun allCategoriesHaveAccessibleClickableSmallCoversAndExportableArtwork() {
        val selected = mutableStateOf(BookSpecs.all.first())
        var clicks = 0
        compose.setContent {
            UnumTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                    Column(Modifier.width(280.dp)) {
                        BookThumbnailCard(book(selected.value), Modifier.size(148.dp,198.dp).testTag("cover-thumb")) { clicks++ }
                        MoonlightBookCover(book(selected.value), Modifier.size(240.dp,350.dp).testTag("cover-full"))
                    }
                }
            }
        }
        for (spec in BookSpecs.all) {
            compose.runOnIdle { selected.value = spec }
            val label = MoonlightCoverStyles.forBook(book(spec)).label
            compose.onAllNodes(hasContentDescription(label, substring = true)).assertCountEquals(2)
            compose.onNodeWithTag("cover-thumb").assertIsDisplayed().performClick()
        }
        compose.runOnIdle { assertEquals(12, clicks) }

        // Render the exact native painter used by both the app and PDF to inspect all categories.
        val bitmap = Bitmap.createBitmap(1500, 1690, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFF3F0F7.toInt())
        BookSpecs.all.forEachIndexed { index, spec ->
            val x = 24f + (index % 4) * 372f
            val y = 24f + (index / 4) * 552f
            MoonlightBookPainter.draw(canvas, RectF(x,y,x+340,y+510), MoonlightCoverStyles.forBook(book(spec)), book(spec), compact=false)
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "moonlight-cover-board.png")
        output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        assertTrue(output.length()>0)
        bitmap.recycle()
    }

    @Test fun pdfCoverRendersWithTheSameMoonlightArtwork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val book = book(BookSpecs.forTopic(PremiumTopic.SELF_ESTEEM))
        val exported = com.example.unum.data.export.FortuneBookPdfExporter.saveToDownloads(context, book)
        context.contentResolver.openFileDescriptor(exported.uri, "r")!!.use { descriptor ->
            android.graphics.pdf.PdfRenderer(descriptor).use { renderer ->
                assertTrue(renderer.pageCount >= 1)
                renderer.openPage(0).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width*2, page.height*2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val output = File(context.getExternalFilesDir(null), "moonlight-pdf-cover.png")
                    output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                    assertTrue(output.length()>0)
                    bitmap.recycle()
                }
            }
        }
    }
}
