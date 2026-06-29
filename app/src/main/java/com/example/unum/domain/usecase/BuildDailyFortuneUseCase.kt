package com.example.unum.domain.usecase

import com.example.unum.data.content.DailyFortuneCatalog
import com.example.unum.data.model.DailyFortuneResult
import com.example.unum.data.model.DailyFortuneTopic
import com.example.unum.data.model.DailyTopicFortune
import com.example.unum.data.model.NumerologyNumbers
import java.time.LocalDate
import kotlin.math.absoluteValue

class BuildDailyFortuneUseCase {
    operator fun invoke(numbers: NumerologyNumbers, date: LocalDate = LocalDate.now()): DailyFortuneResult {
        val coreNumber = calculateCoreNumber(numbers, date)
        val topics = TOPIC_ORDER.map { topic ->
            val messages = DailyFortuneCatalog.topicMessages(topic)
            val messageIndex = (date.dayOfYear + coreNumber * 3 + topic.messageSeed * 5).floorMod(messages.size)
            DailyTopicFortune(topic = topic, message = messages[messageIndex])
        }

        return DailyFortuneResult(
            date = date,
            coreNumber = coreNumber,
            coreTitle = DailyFortuneCatalog.coreTitle(coreNumber),
            coreSummary = DailyFortuneCatalog.coreSummary(coreNumber) +
                " (${date.monthValue}/${date.dayOfMonth} 기준)",
            topics = topics
        )
    }

    internal fun calculateCoreNumber(numbers: NumerologyNumbers, date: LocalDate): Int {
        val total = digitSum(date.year) +
            date.monthValue +
            date.dayOfMonth +
            numbers.destiny +
            numbers.early +
            numbers.middle +
            numbers.late
        return reduceToSingleDigit(total)
    }

    private fun digitSum(value: Int): Int = value.absoluteValue.toString().sumOf { it.digitToInt() }

    private fun reduceToSingleDigit(value: Int): Int {
        var current = value.absoluteValue
        while (current > 9) current = digitSum(current)
        return current.coerceAtLeast(1)
    }

    private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

    private companion object {
        val TOPIC_ORDER = listOf(
            DailyFortuneTopic.LOVE,
            DailyFortuneTopic.WORK,
            DailyFortuneTopic.MONEY,
            DailyFortuneTopic.STUDY,
            DailyFortuneTopic.SELF
        )
    }
}
