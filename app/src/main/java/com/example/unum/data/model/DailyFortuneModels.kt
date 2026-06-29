package com.example.unum.data.model

import java.time.LocalDate

enum class DailyFortuneTopic(val messageSeed: Int) {
    LOVE(0),
    WORK(1),
    MONEY(2),
    STUDY(4),
    SELF(3)
}

data class DailyTopicFortune(
    val topic: DailyFortuneTopic,
    val message: String
)

data class DailyFortuneResult(
    val date: LocalDate,
    val coreNumber: Int,
    val coreTitle: String,
    val coreSummary: String,
    val topics: List<DailyTopicFortune>
)
