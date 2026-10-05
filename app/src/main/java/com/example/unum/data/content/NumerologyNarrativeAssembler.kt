package com.example.unum.data.content

import com.example.unum.data.model.DestinyProfile
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.LifeRecord

object NumerologyNarrativeAssembler {
    fun build(
        code: String,
        gender: GenderOption,
        profile: DestinyProfile,
        earlyProfile: DestinyProfile,
        middleProfile: DestinyProfile,
        lateProfile: DestinyProfile
    ): LifeRecord {
        val destiny = profile.destiny
        val earlyInteraction = NumerologyInteractionCatalog.describe(destiny, earlyProfile.destiny)
        val middleInteraction = NumerologyInteractionCatalog.describe(destiny, middleProfile.destiny)
        val earlyText = earlyProfile.earlyScene
        val middleText = middleProfile.middleScene
        val lateText = lateProfile.lateScene
        val interactionText = buildString {
            append("처음 반응에서는 ")
            append(earlyInteraction)
            append(" 시간이 지나 역할이 커지면 ")
            append(middleInteraction)
        }
        val lifeText = listOf(profile.summary, earlyText, middleText, lateText, profile.caution)
            .filter(String::isNotBlank)
            .joinToString("\n\n")

        return LifeRecord(
            code = code,
            destiny = destiny,
            destinyProfileKey = destiny,
            lifeTitle = "운세 성향 ${destiny}번 리포트",
            destinyText = profile.summary,
            lifeText = lifeText,
            summaryText = interactionText,
            keywords = profile.coreKeywords,
            cautionKeywords = profile.cautionKeywords,
            earlyText = earlyText,
            middleText = middleText,
            lateText = lateText,
            interactionText = interactionText,
            genderText = NumerologyInteractionCatalog.genderCue(gender, destiny),
            relationshipText = profile.relationshipScene,
            workText = profile.workScene,
            moneyText = profile.moneyScene,
            actionText = profile.actionGuide
        )
    }
}
