package com.example.unum.data.model

enum class CalendarType { SOLAR, LUNAR }

enum class GenderOption(val label: String) {
    MALE("남성"),
    FEMALE("여성"),
    NONE("선택 안 함")
}

enum class DestinyPolarity(val label: String) {
    YANG("양"),
    YIN("음"),
    NEUTRAL("중성")
}

enum class GenderResonance(val label: String) {
    AMPLIFIED("강하게 살아남"),
    BALANCED("조율되며 드러남"),
    NEUTRAL("중성에 가깝게 드러남")
}

data class HomeFormState(
    val calendarType: CalendarType = CalendarType.SOLAR,
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
    val subtitle: String,
    val gender: GenderOption = GenderOption.NONE,
    val inputCalendarType: CalendarType = CalendarType.SOLAR,
    val inputYear: Int? = null,
    val inputMonth: Int? = null,
    val inputDay: Int? = null
)

enum class PremiumTopic(val label: String) {
    ROMANCE("연애"),
    CAREER("일과 진로"),
    MONEY("돈"),
    SELF_ESTEEM("나 자신"),
    RELATIONSHIP("인간관계")
}

enum class PremiumMode(val label: String) {
    PERSONAL("개인 운세"),
    COMPATIBILITY("궁합")
}

data class PartnerBirthFormState(
    val calendarType: CalendarType = CalendarType.SOLAR,
    val year: String = "",
    val month: String = "",
    val day: String = ""
)

data class CompatibilityFormState(
    val male: PartnerBirthFormState = PartnerBirthFormState(),
    val female: PartnerBirthFormState = PartnerBirthFormState()
)

enum class ReaderFontScale(val label: String, val multiplier: Float) {
    SMALL("작게", 0.94f),
    MEDIUM("보통", 1.0f),
    LARGE("크게", 1.08f)
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

data class CompatibilityConsultation(
    val maleEnergy: String,
    val femaleEnergy: String,
    val relationshipFlow: String,
    val strengths: String,
    val friction: String,
    val homeTone: String,
    val longTermTip: String,
    val oneLineSummary: String
)

enum class FortuneBookType(val label: String) {
    PERSONAL("개인 운세"),
    COMPATIBILITY("궁합")
}

data class FortuneBook(
    val bookId: String,
    val userId: String? = null,
    val code: String,
    val destiny: Int,
    val early: Int,
    val middle: Int,
    val late: Int,
    val concernTopic: String,
    val concernText: String,
    val coverTitle: String,
    val coverSubtitle: String,
    val summary: String,
    val bookType: FortuneBookType = FortuneBookType.PERSONAL,
    val relationshipNumber: Int? = null,
    val maleBirthLabel: String? = null,
    val femaleBirthLabel: String? = null,
    val maleDestiny: Int? = null,
    val femaleDestiny: Int? = null,
    val maleCode: String? = null,
    val femaleCode: String? = null,
    val bestMonth: String = "",
    val bestMonthReason: String = "",
    val riskyMonth: String = "",
    val riskyMonthReason: String = "",
    val chapters: List<FortuneBookChapter>,
    val createdAt: Long,
    val lastOpenedAt: Long? = null,
    val purchasedAt: Long? = null,
    val isBookmarked: Boolean = false,
    val coverTheme: String = "calm"
)

data class FortuneBookChapter(
    val title: String,
    val lead: String,
    val body: List<String>,
    val highlightQuote: String,
    val actionTip: List<String>
)

data class NumerologyResultBundle(
    val input: BirthInput,
    val numbers: NumerologyNumbers,
    val content: NumerologyContent,
    val displayInput: BirthInput = input
)
