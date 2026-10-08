package com.example.unum.data.content

import com.example.unum.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FreeFortuneEngineTest {
    private val raw=File("src/main/assets/FreeFortuneContent.json").readText()
    private val engine=FreeFortuneEngine(raw)

    @Test fun `all ten thousand codes keep complete readings`() {
        for(code in 0..9999) {
            val content=engine.content(code.toString().padStart(4,'0'),GenderOption.NONE)
            val reading=engine.reading(content)
            assertTrue(reading.opening.isNotBlank())
            assertTrue(reading.core.isNotBlank())
            assertTrue(reading.early.isNotBlank())
            assertTrue(reading.middle.isNotBlank())
            assertTrue(reading.late.isNotBlank())
        }
    }

    @Test fun `daily scores and lucky elements are stable and valid for a year`() {
        val n=NumerologyNumbers(7,8,1,8,"7818")
        for(day in 0..365) {
            val date=LocalDate.of(2026,1,1).plusDays(day.toLong())
            val result=engine.daily(n,date)
            assertEquals(result,engine.daily(n,date))
            assertTrue(result.score in 0..100)
            assertEquals(5,result.topics.size)
            assertTrue(result.topics.all {it.score in 0..100 && it.message.isNotBlank() && it.keyword.isNotBlank()})
            assertTrue(result.luckyColor.isNotBlank())
            assertTrue(result.luckyTime.isNotBlank())
        }
    }

    @Test(expected=IllegalArgumentException::class)
    fun `rejects unknown content version`() {
        FreeFortuneEngine(raw.replace("\"version\": 1","\"version\": 99"))
    }
}
