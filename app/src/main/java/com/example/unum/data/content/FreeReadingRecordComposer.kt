package com.example.unum.data.content

import com.example.unum.data.model.FreeReadingResult
import com.example.unum.data.model.NumerologyContent

object FreeReadingRecordComposer {
    fun compose(content: NumerologyContent): FreeReadingResult {
        val profile = content.destinyProfile
        val record = content.lifeRecord
        return FreeReadingResult(
            opening = profile.summary.ifBlank { profile.resultTitle },
            core = listOf(record.interactionText, record.genderText)
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { profile.summary },
            strength = profile.strength,
            caution = profile.caution,
            action = record.actionText.ifBlank { profile.actionGuide },
            early = record.earlyText,
            middle = record.middleText,
            late = record.lateText,
            relationship = record.relationshipText,
            work = record.workText,
            money = record.moneyText
        )
    }
}
