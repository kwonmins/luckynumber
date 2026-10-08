package com.example.unum.data.content

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TarotCatalogTest {
    @Test fun allServerCardIdsHaveContentForEveryTheme() {
        assertEquals((0..21).toList(),TarotCatalog.cards.map {it.id})
        assertEquals(22,TarotCatalog.cards.map {it.name}.toSet().size)
        TarotCatalog.cards.forEach {card ->
            TarotTheme.entries.forEach {theme ->
                val draw=TarotDraw(LocalDate.of(2026,10,8),theme,card.id)
                assertSame(card,draw.card)
                val text=TarotCatalog.detail(draw)
                assertTrue(text.contains(card.meaning))
                assertTrue(text.contains(theme.label))
                assertTrue(text.contains(card.action))
            }
        }
    }
    @Test fun dayChangesAtKoreanMidnight() {
        val before=Instant.parse("2026-10-08T14:59:59Z").atZone(TarotCatalog.korea).toLocalDate()
        val after=Instant.parse("2026-10-08T15:00:00Z").atZone(TarotCatalog.korea).toLocalDate()
        assertEquals(LocalDate.of(2026,10,8),before)
        assertEquals(before.plusDays(1),after)
    }
}
