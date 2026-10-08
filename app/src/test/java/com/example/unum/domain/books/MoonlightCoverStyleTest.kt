package com.example.unum.domain.books

import com.example.unum.data.model.*
import org.junit.Assert.*
import org.junit.Test

class MoonlightCoverStyleTest {
    private fun book(spec: BookSpec) = FortuneBook(
        bookId = spec.id, code = "1234", destiny = 1, early = 2, middle = 3, late = 4,
        concernTopic = spec.bookLabel, concernText = "", coverTitle = "일상에서 마음이 달라지는 날",
        coverSubtitle = "", summary = "", bookType = spec.bookType,
        chapters = emptyList(), createdAt = 0, coverTheme = spec.themeId.key
    )

    @Test fun `categories sharing legacy themes still receive their own cover`() {
        for (spec in BookSpecs.all) {
            val style = MoonlightCoverStyles.forBook(book(spec))
            assertEquals(spec.id, style.id)
            assertEquals(spec.bookType == FortuneBookType.COMPATIBILITY, style.compatibility)
        }
        assertEquals(12, BookSpecs.all.map { MoonlightCoverStyles.forBook(book(it)).id }.toSet().size)
    }
}
