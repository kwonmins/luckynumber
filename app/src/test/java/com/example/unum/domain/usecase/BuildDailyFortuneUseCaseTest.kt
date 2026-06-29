package com.example.unum.domain.usecase

import com.example.unum.data.model.DailyFortuneTopic
import com.example.unum.data.model.NumerologyNumbers
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildDailyFortuneUseCaseTest {
    private val useCase = BuildDailyFortuneUseCase()

    @Test
    fun `builds the same daily number and topic order for a fixed date`() {
        val numbers = NumerologyNumbers(
            destiny = 7,
            early = 8,
            middle = 1,
            late = 8,
            code = "7818"
        )

        val result = useCase(numbers, LocalDate.of(2026, 6, 29))

        assertEquals(6, result.coreNumber)
        assertEquals("돌봄형", result.coreTitle)
        assertEquals(
            listOf(
                DailyFortuneTopic.LOVE,
                DailyFortuneTopic.WORK,
                DailyFortuneTopic.MONEY,
                DailyFortuneTopic.STUDY,
                DailyFortuneTopic.SELF
            ),
            result.topics.map { it.topic }
        )
    }

    @Test
    fun `returns deterministic content for the same numbers and date`() {
        val numbers = NumerologyNumbers(3, 4, 5, 6, "3456")
        val date = LocalDate.of(2026, 7, 1)

        assertEquals(useCase(numbers, date), useCase(numbers, date))
    }
}
