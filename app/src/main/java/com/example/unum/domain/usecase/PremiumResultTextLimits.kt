package com.example.unum.domain.usecase

import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage

/** Keeps premium results concise even when the model returns more text than requested. */
internal fun ConsultationAnswerCard.limitPremiumLength(): ConsultationAnswerCard {
    val compactBody = body.joinToString(" ").limitPremiumText(120)
    return copy(
        question = question.limitPremiumText(70),
        shortAnswer = shortAnswer.limitPremiumText(70),
        body = compactBody.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    )
}

internal fun List<ConsultationPage>.limitPremiumPages(): List<ConsultationPage> = map { page ->
    val compactBody = page.body.joinToString(" ").limitPremiumText(150)
    page.copy(
        ribbon = page.ribbon.limitPremiumText(16),
        title = page.title.limitPremiumText(30),
        highlight = page.highlight.limitPremiumText(40),
        body = compactBody.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    )
}

internal fun String.limitPremiumText(maxChars: Int): String {
    val normalized = trim().replace(Regex("\\s+"), " ")
    if (normalized.length <= maxChars) return normalized

    val clipped = normalized.take(maxChars)
    val sentenceEnd = listOf('.', '!', '?', '。')
        .maxOf { clipped.lastIndexOf(it) }
    if (sentenceEnd >= maxChars * 2 / 3) {
        return clipped.take(sentenceEnd + 1).trim()
    }

    val wordEnd = clipped.lastIndexOf(' ')
    val safeEnd = wordEnd.takeIf { it >= maxChars * 2 / 3 } ?: maxChars
    return clipped.take(safeEnd).trimEnd(' ', ',', '.', '。') + "…"
}
