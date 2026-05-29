package com.example.unum.domain.usecase

import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.PremiumTopic
import java.util.Calendar

object PremiumMonthPlanner {
    data class MonthSelection(
        val month: Int,
        val isNextYear: Boolean = false,
        val replacedPastMonth: Int? = null
    ) {
        fun toDisplayText(): String = when {
            isNextYear -> "다음 해 ${month}월"
            else -> "${month}월"
        }
    }

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun pickBestMonth(
        topic: PremiumTopic,
        destiny: Int,
        currentMonth: Int = currentMonth()
    ): MonthSelection {
        return pickBestMonth(topic, NumerologyNumbers(destiny, destiny, destiny, destiny, destiny.toString()), currentMonth)
    }

    fun pickBestMonth(
        topic: PremiumTopic,
        numbers: NumerologyNumbers,
        currentMonth: Int = currentMonth()
    ): MonthSelection {
        val topMonth = rankedBestMonths(topic, numbers).first()
        if (topMonth >= currentMonth) return MonthSelection(month = topMonth)

        val preferredFlows = bestFlows(topic)
        val remainingThisYear = rankedBestMonths(topic, numbers).firstOrNull { month ->
            month >= currentMonth && flowNumber(numbers, month) in preferredFlows
        }
        if (remainingThisYear != null) {
            return MonthSelection(month = remainingThisYear, replacedPastMonth = topMonth)
        }

        val firstNextYear = rankedBestMonths(topic, numbers).first { month ->
            flowNumber(numbers, month) in preferredFlows
        }
        return MonthSelection(month = firstNextYear, isNextYear = true, replacedPastMonth = topMonth)
    }

    fun pickRiskyMonth(
        topic: PremiumTopic,
        destiny: Int,
        currentMonth: Int = currentMonth()
    ): MonthSelection {
        return pickRiskyMonth(topic, NumerologyNumbers(destiny, destiny, destiny, destiny, destiny.toString()), currentMonth)
    }

    fun pickRiskyMonth(
        topic: PremiumTopic,
        numbers: NumerologyNumbers,
        currentMonth: Int = currentMonth()
    ): MonthSelection {
        val topMonth = rankedRiskyMonths(topic, numbers).first()
        if (topMonth >= currentMonth) return MonthSelection(month = topMonth)

        val riskyFlows = riskyFlows(topic)
        val remainingThisYear = rankedRiskyMonths(topic, numbers).firstOrNull { month ->
            month >= currentMonth && flowNumber(numbers, month) in riskyFlows
        }
        if (remainingThisYear != null) {
            return MonthSelection(month = remainingThisYear, replacedPastMonth = topMonth)
        }

        val firstNextYear = rankedRiskyMonths(topic, numbers).first { month ->
            flowNumber(numbers, month) in riskyFlows
        }
        return MonthSelection(month = firstNextYear, isNextYear = true, replacedPastMonth = topMonth)
    }

    fun isPastMonthText(monthText: String, currentMonth: Int = currentMonth()): Boolean {
        if (monthText.startsWith("다음 해")) return false
        val month = monthText.removeSuffix("월").toIntOrNull() ?: return false
        return month < currentMonth
    }

    fun topicFromThemeOrLabel(theme: String, label: String): PremiumTopic? {
        return PremiumTopic.entries.firstOrNull { topic ->
            topic.name.equals(theme, ignoreCase = true) || topic.label == label
        }
    }

    private fun rankedBestMonths(topic: PremiumTopic, numbers: NumerologyNumbers): List<Int> {
        val preferredFlows = bestFlows(topic)
        return (1..12).sortedWith(
            compareBy<Int> {
                preferredFlows.indexOf(flowNumber(numbers, it)).let { index -> if (index == -1) 99 else index }
            }.thenBy { monthTieBreaker(numbers, it) }
                .thenBy { it }
        )
    }

    private fun rankedRiskyMonths(topic: PremiumTopic, numbers: NumerologyNumbers): List<Int> {
        val riskyFlows = riskyFlows(topic)
        return (1..12).sortedWith(
            compareBy<Int> {
                riskyFlows.indexOf(flowNumber(numbers, it)).let { index -> if (index == -1) 99 else index }
            }.thenBy { monthTieBreaker(numbers, it) }
                .thenByDescending { it }
        )
    }

    private fun bestFlows(topic: PremiumTopic): List<Int> {
        return when (topic) {
            PremiumTopic.ROMANCE -> listOf(1, 3, 6, 2)
            PremiumTopic.CAREER -> listOf(4, 8, 1, 5)
            PremiumTopic.MONEY -> listOf(8, 4, 6, 1)
            PremiumTopic.SELF_ESTEEM -> listOf(7, 1, 4, 3)
            PremiumTopic.RELATIONSHIP -> listOf(2, 8, 3, 6)
        }
    }

    private fun riskyFlows(topic: PremiumTopic): List<Int> {
        return when (topic) {
            PremiumTopic.ROMANCE -> listOf(8, 7, 9, 0)
            PremiumTopic.CAREER -> listOf(5, 9, 7, 0)
            PremiumTopic.MONEY -> listOf(5, 8, 9, 0)
            PremiumTopic.SELF_ESTEEM -> listOf(9, 8, 5, 0)
            PremiumTopic.RELATIONSHIP -> listOf(8, 5, 9, 7)
        }
    }

    private fun flowNumber(numbers: NumerologyNumbers, month: Int): Int {
        val codeSeed = numbers.code.filter(Char::isDigit).sumOf { it.digitToInt() }
        val rhythmSeed = numbers.destiny * 11 + numbers.early * 3 + numbers.middle * 5 + numbers.late * 7
        return (rhythmSeed + codeSeed + month).floorMod(10)
    }

    private fun monthTieBreaker(numbers: NumerologyNumbers, month: Int): Int {
        val codeSeed = numbers.code.filter(Char::isDigit).sumOf { it.digitToInt() }
        return (month * 7 + numbers.destiny * 5 + numbers.early * 3 + numbers.middle * 2 + numbers.late + codeSeed)
            .floorMod(12)
    }

    private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor
}
