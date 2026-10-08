package com.example.unum.ui.components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.MoonlightCoverStyles
import com.example.unum.graphics.MoonlightBookPainter

@Composable
fun MoonlightBookCover(book: FortuneBook?, modifier: Modifier = Modifier, compact: Boolean = false) {
    val style = remember(book?.concernTopic, book?.bookType, book?.coverTheme) { MoonlightCoverStyles.forBook(book) }
    val description = "${style.label} ${if (style.compatibility) "궁합노트" else "운세노트"} · ${book?.coverTitle ?: style.subtitle}"
    Canvas(modifier.clip(RoundedCornerShape(12.dp)).semantics { contentDescription = description }) {
        drawIntoCanvas { MoonlightBookPainter.draw(it.nativeCanvas, RectF(0f, 0f, size.width, size.height), style, book, compact) }
    }
}
