package com.example.unum.presentation.spec

import com.example.unum.data.model.BookSpecs
import com.example.unum.data.model.BookThemeId
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.PremiumTopic

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
    val themeId: BookThemeId,
    val archiveKeywords: List<String>
) {
    val coverTheme: String
        get() = themeId.key
}

object FeatureSpecs {
    val premiumTopicPlans: List<PremiumTopicPlan> =
        BookSpecs.personalSpecs.map { spec ->
            PremiumTopicPlan(
                topic = requireNotNull(spec.topic),
                bookLabel = spec.bookLabel,
                themeId = spec.themeId,
                archiveKeywords = spec.archiveKeywords
            )
        }

    fun planFor(topic: PremiumTopic): PremiumTopicPlan =
        premiumTopicPlans.first { it.topic == topic }

    fun compatibilityThemeFor(status: CompatibilityRelationshipStatus): String =
        BookSpecs.forStatus(status).themeId.key
}
