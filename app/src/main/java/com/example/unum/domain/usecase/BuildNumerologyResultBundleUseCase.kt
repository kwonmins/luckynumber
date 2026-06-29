package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.FreeReadingResult
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.repository.NumerologyRepository
import com.example.unum.domain.NumerologyCalculator

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
        val freeReading = FreeReadingRecordComposer.compose(content)
        return NumerologyResultBundle(
            input = calculationInput,
            numbers = numbers,
            content = content,
            displayInput = displaySolarInput,
            freeReading = freeReading
        )
    }
}

private object FreeReadingRecordComposer {
    fun compose(content: NumerologyContent): FreeReadingResult {
        val record = content.lifeRecord
        val profile = content.destinyProfile
        return FreeReadingResult(
            opening = profile.resultTitle,
            core = concisePreview(profile.summary, sentenceLimit = 2),
            strength = concisePreview(profile.strength, sentenceLimit = 2),
            caution = concisePreview(profile.caution, sentenceLimit = 2),
            // Free results should preview the tone only. Task-style advice belongs nowhere in the basic reading.
            action = concisePreview(profile.actionGuide, sentenceLimit = 1),
            relationship = concisePreview(record.summaryText, sentenceLimit = 1),
            career = strengthPreview(record.keywords.drop(1), profile.title),
            money = cautionPreview(record.cautionKeywords.drop(1))
        )
    }

    private fun concisePreview(text: String, sentenceLimit: Int = 1): String {
        val cleaned = text
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleaned.isBlank()) return ""
        return cleaned
            .split(Regex("(?<=[.!?。])\\s+"))
            .filter { it.isNotTaskLikeAdvice() }
            .take(sentenceLimit)
            .joinToString(" ")
            .ifBlank {
                cleaned.split(".").firstOrNull().orEmpty().trim()
            }
    }

    private fun strengthPreview(keywords: List<String>, fallback: String): String {
        val focus = keywords.take(2).filter { it.isNotBlank() }.joinToString(", ").ifBlank { fallback }
        return "$focus 쪽의 기운이 비교적 선명하게 드러납니다."
    }

    private fun cautionPreview(keywords: List<String>): String {
        val focus = keywords.take(2).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "과한 몰입" }
        return "$focus 쪽이 강해질 때 흐름이 좁아질 수 있습니다."
    }

    private fun String.isNotTaskLikeAdvice(): Boolean {
        val blocked = listOf("수첩", "메모", "기록", "적어", "오늘은", "이번 주", "한 달 안", "해야", "해보", "정하세요", "만드세요")
        return blocked.none { contains(it) }
    }
}
