package com.example.unum.domain.usecase

import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage

/** Normalizes premium text without dropping the model's explanation. */
internal fun ConsultationAnswerCard.limitPremiumLength(): ConsultationAnswerCard {
    val compactBody = body.joinToString(" ").limitPremiumText(Int.MAX_VALUE)
    return copy(
        question = question.limitPremiumText(Int.MAX_VALUE),
        shortAnswer = shortAnswer.limitPremiumText(Int.MAX_VALUE),
        body = compactBody.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    )
}

internal fun List<ConsultationPage>.limitPremiumPages(): List<ConsultationPage> = map { page ->
    val compactBody = page.body.joinToString(" ").limitPremiumText(Int.MAX_VALUE)
    page.copy(
        ribbon = page.ribbon.limitPremiumText(Int.MAX_VALUE),
        title = page.title.limitPremiumText(Int.MAX_VALUE),
        highlight = page.highlight.limitPremiumText(Int.MAX_VALUE),
        body = compactBody.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    )
}

internal fun String.limitPremiumText(maxChars: Int): String {
    val normalized = trim().replace(Regex("\\s+"), " ")
    // Do not add an ellipsis to a premium interpretation. The reader must receive
    // the complete explanation, including timing reasons and follow-up guidance.
    return normalized
}
