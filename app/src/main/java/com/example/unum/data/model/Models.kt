package com.example.unum.data.model

enum class CalendarType { SOLAR, LUNAR }

enum class GenderOption(val label: String) {
    MALE("남성"),
    FEMALE("여성"),
    NONE("선택 안 함")
}

data class HomeFormState(
    val calendarType: CalendarType = CalendarType.LUNAR,
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val gender: GenderOption = GenderOption.NONE
)

data class BirthInput(
    val calendarType: CalendarType,
    val year: Int,
    val month: Int,
    val day: Int,
    val gender: GenderOption
)

data class NumerologyNumbers(
    val destiny: Int,
    val early: Int,
    val middle: Int,
    val late: Int,
    val code: String
)

data class DestinyProfile(
    val destiny: Int,
    val title: String,
    val polarity: String,
    val coreKeywords: List<String>,
    val cautionKeywords: List<String>,
    val destinyText: String,
    val oneLineAdvice: String
)

data class LifeRecord(
    val code: String,
    val destiny: Int,
    val early: Int,
    val middle: Int,
    val late: Int,
    val destinyProfileKey: Int,
    val lifeTitle: String,
    val earlyText: String,
    val middleText: String,
    val lateText: String,
    val lifeText: String,
    val summaryText: String,
    val keywords: List<String>,
    val cautionKeywords: List<String>,
    val oneLineAdvice: String
)

data class NumerologyContent(
    val destinyProfile: DestinyProfile,
    val lifeRecord: LifeRecord
)

data class RecentSearch(
    val code: String,
    val dateLabel: String,
    val subtitle: String
)

enum class PremiumTopic(val label: String) {
    ROMANCE("연애"),
    CAREER("진로"),
    MONEY("재물"),
    SELF_ESTEEM("자존감"),
    RELATIONSHIP("인간관계")
}

data class PremiumConsultation(
    val core: String,
    val interpretation: String,
    val caution: String,
    val direction: String,
    val oneLineAdvice: String,
    val bestMonth: String = "",
    val bestMonthReason: String = "",
    val riskyMonth: String = "",
    val riskyMonthReason: String = ""
)

data class NumerologyResultBundle(
    val input: BirthInput,
    val numbers: NumerologyNumbers,
    val content: NumerologyContent,
    val displayInput: BirthInput = input
)
