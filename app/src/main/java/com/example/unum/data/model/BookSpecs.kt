package com.example.unum.data.model

data class BookSpec(
    val id: String,
    val bookType: FortuneBookType,
    val label: String,
    val bookLabel: String,
    val themeId: BookThemeId,
    val archiveKeywords: List<String>,
    val promptSchemaId: String,
    val defaultChapterTitles: List<String>,
    val pdfTemplateId: String = "premium_book",
    val topic: PremiumTopic? = null,
    val relationshipStatus: CompatibilityRelationshipStatus? = null,
    val coverTitle: String = bookLabel,
    val coverKicker: String = "PREMIUM NOTE"
)

object BookSpecs {
    val personalSpecs: List<BookSpec> = listOf(
        BookSpec(
            id = "romance",
            bookType = FortuneBookType.PERSONAL,
            label = "연애",
            bookLabel = "연애",
            themeId = BookThemeId.ROMANCE,
            archiveKeywords = listOf("연애", "사랑"),
            promptSchemaId = "premium_romance_v1",
            defaultChapterTitles = listOf("상담소 첫 답변", "상황 해석", "주의사항", "오늘의 처방"),
            topic = PremiumTopic.ROMANCE,
            coverTitle = "연애 운세노트",
            coverKicker = "PREMIUM ROMANCE NOTE"
        ),
        BookSpec(
            id = "career",
            bookType = FortuneBookType.PERSONAL,
            label = "일과 진로",
            bookLabel = "일과 진로",
            themeId = BookThemeId.CAREER,
            archiveKeywords = listOf("일", "진로", "직업", "커리어"),
            promptSchemaId = "premium_career_v1",
            defaultChapterTitles = listOf("상담소 첫 답변", "상황 해석", "주의사항", "오늘의 처방"),
            topic = PremiumTopic.CAREER,
            coverTitle = "일과 진로 운세노트",
            coverKicker = "PREMIUM CAREER NOTE"
        ),
        BookSpec(
            id = "money",
            bookType = FortuneBookType.PERSONAL,
            label = "돈",
            bookLabel = "돈과 경제",
            themeId = BookThemeId.MONEY,
            archiveKeywords = listOf("돈", "금전", "경제", "재물"),
            promptSchemaId = "premium_money_v1",
            defaultChapterTitles = listOf("상담소 첫 답변", "상황 해석", "주의사항", "오늘의 처방"),
            topic = PremiumTopic.MONEY,
            coverTitle = "돈 운세노트",
            coverKicker = "PREMIUM MONEY NOTE"
        ),
        BookSpec(
            id = "self",
            bookType = FortuneBookType.PERSONAL,
            label = "나 자신",
            bookLabel = "나 자신",
            themeId = BookThemeId.SELF_ESTEEM,
            archiveKeywords = listOf("나 자신", "자아", "마음", "자존감"),
            promptSchemaId = "premium_self_v1",
            defaultChapterTitles = listOf("상담소 첫 답변", "상황 해석", "주의사항", "오늘의 처방"),
            topic = PremiumTopic.SELF_ESTEEM,
            coverTitle = "나 자신 운세노트",
            coverKicker = "PREMIUM SELF NOTE"
        ),
        BookSpec(
            id = "relationship",
            bookType = FortuneBookType.PERSONAL,
            label = "인간관계",
            bookLabel = "인간관계",
            themeId = BookThemeId.RELATIONSHIP,
            archiveKeywords = listOf("인간관계", "관계"),
            promptSchemaId = "premium_relationship_v1",
            defaultChapterTitles = listOf("상담소 첫 답변", "상황 해석", "주의사항", "오늘의 처방"),
            topic = PremiumTopic.RELATIONSHIP,
            coverTitle = "인간관계 운세노트",
            coverKicker = "PREMIUM RELATION NOTE"
        )
    )

    val compatibilitySpecs: List<BookSpec> = listOf(
        BookSpec(
            id = "compatibility_couple",
            bookType = FortuneBookType.COMPATIBILITY,
            label = "커플",
            bookLabel = "커플 운세노트",
            themeId = BookThemeId.COMPATIBILITY_COUPLE,
            archiveKeywords = listOf("궁합", "커플", "couple"),
            promptSchemaId = "compatibility_couple_v1",
            defaultChapterTitles = listOf("궁합 한 문장", "서로 끌리는 이유", "주의사항", "오래 가려면"),
            relationshipStatus = CompatibilityRelationshipStatus.COUPLE,
            coverTitle = "커플 운세노트",
            coverKicker = "PREMIUM COUPLE NOTE"
        ),
        BookSpec(
            id = "compatibility_crush",
            bookType = FortuneBookType.COMPATIBILITY,
            label = "짝사랑",
            bookLabel = "짝사랑 운세노트",
            themeId = BookThemeId.COMPATIBILITY_CRUSH,
            archiveKeywords = listOf("궁합", "짝사랑", "crush"),
            promptSchemaId = "compatibility_crush_v1",
            defaultChapterTitles = listOf("궁합 한 문장", "서로 끌리는 이유", "주의사항", "오래 가려면"),
            relationshipStatus = CompatibilityRelationshipStatus.CRUSH,
            coverTitle = "짝사랑 운세노트",
            coverKicker = "PREMIUM CRUSH NOTE"
        ),
        BookSpec(
            id = "compatibility_reunion",
            bookType = FortuneBookType.COMPATIBILITY,
            label = "재회",
            bookLabel = "재회 운세노트",
            themeId = BookThemeId.COMPATIBILITY_REUNION,
            archiveKeywords = listOf("궁합", "재회", "reunion"),
            promptSchemaId = "compatibility_reunion_v1",
            defaultChapterTitles = listOf("궁합 한 문장", "서로 끌리는 이유", "주의사항", "오래 가려면"),
            relationshipStatus = CompatibilityRelationshipStatus.REUNION,
            coverTitle = "재회 운세노트",
            coverKicker = "PREMIUM REUNION NOTE"
        )
    )

    val all: List<BookSpec> = personalSpecs + compatibilitySpecs

    fun forTopic(topic: PremiumTopic): BookSpec =
        personalSpecs.first { it.topic == topic }

    fun forStatus(status: CompatibilityRelationshipStatus): BookSpec =
        compatibilitySpecs.first { it.relationshipStatus == status }

    fun forTheme(themeId: BookThemeId): BookSpec? =
        all.firstOrNull { it.themeId == themeId }

    fun forBook(book: FortuneBook): BookSpec {
        val themeId = book.resolvedThemeId()
        forTheme(themeId)?.let { return it }

        val searchable = "${book.concernTopic} ${book.coverTitle}"
        return all.firstOrNull { spec ->
            spec.bookType == book.bookType &&
                spec.archiveKeywords.any { keyword -> searchable.contains(keyword, ignoreCase = true) }
        } ?: if (book.bookType == FortuneBookType.COMPATIBILITY) {
            compatibilitySpecs.first()
        } else {
            personalSpecs.first()
        }
    }
}

fun FortuneBook.resolvedThemeId(): BookThemeId =
    BookThemeId.fromThemeOrText(
        theme = coverTheme,
        text = "$concernTopic $coverTitle",
        bookType = bookType
    )

fun FortuneBook.matchesBookSpec(spec: BookSpec): Boolean {
    if (bookType != spec.bookType) return false
    if (resolvedThemeId() == spec.themeId) return true

    val searchable = "$concernTopic $coverTitle"
    return spec.archiveKeywords.any { keyword ->
        searchable.contains(keyword, ignoreCase = true)
    }
}
