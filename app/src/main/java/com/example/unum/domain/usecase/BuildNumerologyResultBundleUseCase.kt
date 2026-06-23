package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.FreeReadingPhrase
import com.example.unum.data.model.FreeReadingResult
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.repository.NumerologyRepository
import com.example.unum.domain.NumerologyCalculator
import java.time.LocalDate
import java.lang.Math.floorMod

class BuildNumerologyResultBundleUseCase(
    private val repository: NumerologyRepository,
    private val calculateNumerologyUseCase: CalculateNumerologyUseCase
) {
    suspend operator fun invoke(userBirthInput: BirthInput): NumerologyResultBundle {
        val calculationInput = when (userBirthInput.calendarType) {
            CalendarType.SOLAR -> NumerologyCalculator.toLunarBirthInput(userBirthInput)
            CalendarType.LUNAR -> userBirthInput
        }
        val displaySolarInput = when (userBirthInput.calendarType) {
            CalendarType.SOLAR -> userBirthInput
            CalendarType.LUNAR -> NumerologyCalculator.toSolarBirthInput(userBirthInput)
        }

        val numbers = calculateNumerologyUseCase(calculationInput)
        val content = repository.getContent(numbers.code, userBirthInput.gender)
        val freeReading = FreeReadingPhraseComposer.compose(
            phrases = repository.getFreeReadingPhrases(),
            numbers = numbers,
            content = content,
            displayInput = displaySolarInput
        )
        return NumerologyResultBundle(
            input = calculationInput,
            numbers = numbers,
            content = content,
            displayInput = displaySolarInput,
            freeReading = freeReading
        )
    }
}

private object FreeReadingPhraseComposer {
    fun compose(
        phrases: List<FreeReadingPhrase>,
        numbers: NumerologyNumbers,
        content: NumerologyContent,
        displayInput: BirthInput,
        today: LocalDate = LocalDate.now()
    ): FreeReadingResult {
        val usedAvoidWords = mutableSetOf<String>()
        val dailyNumber = floorMod(numbers.destiny + today.dayOfYear, 10)

        fun pick(category: String, preferredNumbers: List<Int>, salt: Int): String {
            val categoryPhrases = phrases.filter { it.category == category }
            val numberMatches = categoryPhrases.filter { phrase ->
                phrase.number == null || phrase.number in preferredNumbers
            }
            val candidates = (numberMatches.ifEmpty { categoryPhrases }).sortedBy { it.id }
            if (candidates.isEmpty()) return ""

            val seed = "${numbers.code}:${displayInput.year}:${displayInput.month}:${displayInput.day}:${today}:$category:$salt"
            val start = floorMod(seed.hashCode(), candidates.size)
            val ordered = candidates.drop(start) + candidates.take(start)
            val selected = ordered.firstOrNull { phrase ->
                phrase.avoidWith.none { avoid -> avoid in usedAvoidWords }
            } ?: ordered.first()

            usedAvoidWords += selected.avoidWith
            usedAvoidWords += selected.keywords
            return selected.text
        }

        return FreeReadingResult(
            opening = pick("opening", listOf(numbers.destiny, dailyNumber), 11)
                .ifBlank { content.destinyProfile.oneLineAdvice },
            core = pick("core", listOf(numbers.destiny, numbers.middle), 23)
                .ifBlank { content.lifeRecord.summaryText },
            strength = pick("strength", listOf(numbers.early, numbers.destiny), 37)
                .ifBlank { content.lifeRecord.keywords.take(2).joinToString(", ") },
            caution = pick("caution", listOf(numbers.late, numbers.middle, numbers.destiny), 41)
                .ifBlank { content.lifeRecord.cautionKeywords.take(2).joinToString(", ") },
            action = pick("action", listOf(dailyNumber, numbers.late, numbers.destiny), 53)
                .ifBlank { content.lifeRecord.oneLineAdvice },
            relationship = pick("relationship", listOf(numbers.middle, numbers.destiny), 67),
            career = pick("career", listOf(numbers.early, numbers.middle, numbers.destiny), 79),
            money = pick("money", listOf(numbers.late, numbers.destiny), 83)
        )
    }
}
