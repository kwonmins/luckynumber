package com.example.unum.data.model

import java.time.LocalDate

enum class DailyFortuneTopic(val messageSeed: Int) {
    LOVE(0),
    WORK(1),
    MONEY(2),
    STUDY(4),
    SELF(3),
    HEALTH(5),
    LUCK(6)
}

data class DailyTopicFortune(
    val topic: DailyFortuneTopic,
    val message: String,
    val score: Int = 0,
    val keyword: String = ""
)

data class DailyFortuneResult(
    val date: LocalDate,
    val coreNumber: Int,
    val coreTitle: String,
    val coreSummary: String,
    val topics: List<DailyTopicFortune>,
    val score: Int = 0,
    val luckyNumber: Int = coreNumber,
    val luckyColor: String = "라벤더",
    val luckyTime: String = "오전"
)
