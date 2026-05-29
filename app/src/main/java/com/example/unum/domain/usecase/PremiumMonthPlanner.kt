package com.example.unum.domain.usecase

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
        val topMonth = rankedBestMonths(topic, destiny).first()
        if (topMonth >= currentMonth) return MonthSelection(month = topMonth)

        val preferredFlows = bestFlows(topic)
        val remainingThisYear = (currentMonth..12).firstOrNull { month ->
            flowNumber(destiny, month) in preferredFlows
        }
        if (remainingThisYear != null) {
            return MonthSelection(month = remainingThisYear, replacedPastMonth = topMonth)
        }

        val firstNextYear = (1..12).first { month ->
            flowNumber(destiny, month) in preferredFlows
        }
        return MonthSelection(month = firstNextYear, isNextYear = true, replacedPastMonth = topMonth)
    }

    fun pickRiskyMonth(
        topic: PremiumTopic,
        destiny: Int,
        currentMonth: Int = currentMonth()
    ): MonthSelection {
        val topMonth = rankedRiskyMonths(topic, destiny).first()
        if (topMonth >= currentMonth) return MonthSelection(month = topMonth)

        val riskyFlows = riskyFlows(topic)
        val remainingThisYear = (currentMonth..12).firstOrNull { month ->
            flowNumber(destiny, month) in riskyFlows
        }
        if (remainingThisYear != null) {
            return MonthSelection(month = remainingThisYear, replacedPastMonth = topMonth)
        }

        val firstNextYear = (1..12).first { month ->
            flowNumber(destiny, month) in riskyFlows
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

    private fun rankedBestMonths(topic: PremiumTopic, destiny: Int): List<Int> {
        val preferredFlows = bestFlows(topic)
        return (1..12).sortedWith(
            compareBy<Int> {
                preferredFlows.indexOf(flowNumber(destiny, it)).let { index -> if (index == -1) 99 else index }
            }.thenBy { it }
        )
    }

    private fun rankedRiskyMonths(topic: PremiumTopic, destiny: Int): List<Int> {
        val riskyFlows = riskyFlows(topic)
        return (1..12).sortedWith(
            compareBy<Int> {
                riskyFlows.indexOf(flowNumber(destiny, it)).let { index -> if (index == -1) 99 else index }
            }.thenByDescending { it }
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

    private fun flowNumber(destiny: Int, month: Int): Int = (destiny + month) % 10
}
