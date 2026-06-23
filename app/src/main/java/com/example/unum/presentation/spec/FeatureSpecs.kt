package com.example.unum.presentation.spec

import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.compatibilityCoverTheme

enum class LibrarySection(
    val label: String,
    val chipWidthDp: Int
) {
    ALL("전체", 64),
    ROMANCE("연애", 64),
    CAREER("일과 진로", 90),
    MONEY("돈", 64),
    SELF("나 자신", 76),
    RELATIONSHIP("인간관계", 90),
    COMPATIBILITY("궁합", 64),
    COUPLE("커플", 64),
    CRUSH("짝사랑", 76),
    REUNION("재회", 64)
}

data class PremiumTopicPlan(
    val topic: PremiumTopic,
    val bookLabel: String,
    val coverTheme: String,
    val archiveKeywords: List<String>
)

object FeatureSpecs {
    val premiumTopicPlans: List<PremiumTopicPlan> = listOf(
        PremiumTopicPlan(
            topic = PremiumTopic.ROMANCE,
            bookLabel = "연애",
            coverTheme = "romance",
            archiveKeywords = listOf("연애", "사랑")
        ),
        PremiumTopicPlan(
            topic = PremiumTopic.CAREER,
            bookLabel = "일과 진로",
            coverTheme = "career",
            archiveKeywords = listOf("일", "진로", "직업", "커리어")
        ),
        PremiumTopicPlan(
            topic = PremiumTopic.MONEY,
            bookLabel = "돈과 경제",
            coverTheme = "money",
            archiveKeywords = listOf("돈", "금전", "경제", "재물")
        ),
        PremiumTopicPlan(
            topic = PremiumTopic.SELF_ESTEEM,
            bookLabel = "나 자신",
            coverTheme = "self_esteem",
            archiveKeywords = listOf("나 자신", "자아", "마음", "자존감")
        ),
        PremiumTopicPlan(
            topic = PremiumTopic.RELATIONSHIP,
            bookLabel = "인간관계",
            coverTheme = "relationship",
            archiveKeywords = listOf("인간관계", "관계")
        )
    )

    fun planFor(topic: PremiumTopic): PremiumTopicPlan =
        premiumTopicPlans.first { it.topic == topic }

    fun compatibilityThemeFor(status: CompatibilityRelationshipStatus): String =
        status.compatibilityCoverTheme()
}
