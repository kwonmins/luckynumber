package com.example.unum.domain.usecase

import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookChapter
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.NumerologyCalculator

class BuildFortuneBookUseCase {
    fun buildPersonalBook(
        consultation: PremiumConsultation,
        bundle: NumerologyResultBundle,
        topic: PremiumTopic,
        concern: String
    ): FortuneBook {
        val now = System.currentTimeMillis()
        val topicLabel = topic.label
        val createdYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val chapters = if (consultation.pages.isNotEmpty() || consultation.answerCard.hasContent()) {
            buildSalonChapters(consultation.answerCard, consultation.pages, consultation.closingAdvice)
        } else {
            buildPersonalFallbackChapters(consultation)
        }

        return FortuneBook(
            bookId = "${bundle.numbers.code}-$now",
            code = bundle.numbers.code,
            destiny = bundle.numbers.destiny,
            early = bundle.numbers.early,
            middle = bundle.numbers.middle,
            late = bundle.numbers.late,
            concernTopic = topicLabel,
            concernText = concern,
            coverTitle = consultation.coverTitle.ifBlank { buildCoverTitle(topic, bundle.numbers.destiny, createdYear) },
            coverSubtitle = consultation.coverSubtitle.ifBlank { "운명수 ${bundle.numbers.destiny} · ${bundle.content.destinyProfile.title}" },
            summary = consultation.answerCard.shortAnswer.ifBlank { consultation.core },
            bookType = FortuneBookType.PERSONAL,
            bestMonth = consultation.bestMonth,
            bestMonthReason = consultation.bestMonthReason,
            riskyMonth = consultation.riskyMonth,
            riskyMonthReason = consultation.riskyMonthReason,
            chapters = chapters,
            createdAt = now,
            lastOpenedAt = now,
            purchasedAt = now,
            coverTheme = topic.name.lowercase()
        )
    }

    fun buildCompatibilityBook(
        consultation: CompatibilityConsultation,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String
    ): FortuneBook {
        val now = System.currentTimeMillis()
        val relationshipNumber = (maleBundle.numbers.destiny + femaleBundle.numbers.destiny) % 10
        val maleBirthLabel = NumerologyCalculator.formatDate(
            maleBundle.displayInput.year,
            maleBundle.displayInput.month,
            maleBundle.displayInput.day
        )
        val femaleBirthLabel = NumerologyCalculator.formatDate(
            femaleBundle.displayInput.year,
            femaleBundle.displayInput.month,
            femaleBundle.displayInput.day
        )
        val chapters = if (consultation.pages.isNotEmpty() || consultation.answerCard.hasContent()) {
            buildSalonChapters(consultation.answerCard, consultation.pages, consultation.closingAdvice)
        } else {
            buildCompatibilityFallbackChapters(consultation)
        }

        return FortuneBook(
            bookId = "compatibility-${maleBundle.numbers.code}-${femaleBundle.numbers.code}-$now",
            code = "${maleBundle.numbers.code}/${femaleBundle.numbers.code}",
            destiny = relationshipNumber,
            early = maleBundle.numbers.early,
            middle = femaleBundle.numbers.middle,
            late = relationshipNumber,
            concernTopic = "궁합",
            concernText = concern,
            coverTitle = consultation.coverTitle.ifBlank { "수리 궁합 상담소" },
            coverSubtitle = consultation.coverSubtitle.ifBlank { "남자 $maleBirthLabel · 여자 $femaleBirthLabel" },
            summary = consultation.answerCard.shortAnswer.ifBlank { consultation.oneLineSummary },
            bookType = FortuneBookType.COMPATIBILITY,
            relationshipNumber = relationshipNumber,
            maleBirthLabel = maleBirthLabel,
            femaleBirthLabel = femaleBirthLabel,
            maleDestiny = maleBundle.numbers.destiny,
            femaleDestiny = femaleBundle.numbers.destiny,
            maleCode = maleBundle.numbers.code,
            femaleCode = femaleBundle.numbers.code,
            chapters = chapters,
            createdAt = now,
            lastOpenedAt = now,
            purchasedAt = now,
            coverTheme = "compatibility"
        )
    }

    private fun buildSalonChapters(
        answerCard: ConsultationAnswerCard,
        pages: List<ConsultationPage>,
        closingAdvice: String
    ): List<FortuneBookChapter> {
        val answerChapter = if (answerCard.hasContent()) {
            listOf(
                FortuneBookChapter(
                    title = answerCard.question.ifBlank { "상담소의 첫 답변" },
                    lead = "Q & A",
                    body = answerCard.body.ifEmpty { listOf(answerCard.shortAnswer) },
                    highlightQuote = answerCard.shortAnswer,
                    actionTip = emptyList()
                )
            )
        } else {
            emptyList()
        }

        val pageChapters = pages.map { page ->
            FortuneBookChapter(
                title = page.title.ifBlank { page.ribbon.ifBlank { "상담 페이지" } },
                lead = page.ribbon,
                body = page.body.take(3),
                highlightQuote = page.highlight,
                actionTip = listOfNotNull(page.copyText.takeIf { it.isNotBlank() })
            )
        }

        val closingChapter = closingAdvice.takeIf { it.isNotBlank() }?.let {
            listOf(
                FortuneBookChapter(
                    title = "마지막 조언",
                    lead = "다시 읽는 문장",
                    body = listOf(it),
                    highlightQuote = it,
                    actionTip = emptyList()
                )
            )
        } ?: emptyList()

        return answerChapter + pageChapters + closingChapter
    }

    private fun buildPersonalFallbackChapters(consultation: PremiumConsultation): List<FortuneBookChapter> {
        return listOf(
            FortuneBookChapter(
                title = "지금의 핵심",
                lead = "상담소 첫 답변",
                body = listOf(consultation.core),
                highlightQuote = consultation.oneLineAdvice,
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "상황 해석",
                lead = "실제로 벌어질 장면",
                body = listOf(consultation.interpretation),
                highlightQuote = consultation.oneLineAdvice,
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "주의사항",
                lead = "방치하면 꼬이는 지점",
                body = listOf(consultation.caution),
                highlightQuote = consultation.caution.firstSentence(),
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "오늘의 처방",
                lead = "지금 바로 할 일",
                body = listOf(consultation.direction),
                highlightQuote = consultation.oneLineAdvice,
                actionTip = emptyList()
            )
        )
    }

    private fun buildCompatibilityFallbackChapters(consultation: CompatibilityConsultation): List<FortuneBookChapter> {
        return listOf(
            FortuneBookChapter(
                title = "궁합 한 문장",
                lead = "두 사람의 전체 흐름",
                body = listOf(consultation.relationshipFlow),
                highlightQuote = consultation.oneLineSummary,
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "서로 끌리는 이유",
                lead = "맞닿는 지점",
                body = listOf(consultation.strengths),
                highlightQuote = consultation.strengths.firstSentence(),
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "주의사항",
                lead = "엇갈리는 방식",
                body = listOf(consultation.friction),
                highlightQuote = consultation.friction.firstSentence(),
                actionTip = emptyList()
            ),
            FortuneBookChapter(
                title = "오래 가려면",
                lead = "관계를 살리는 습관",
                body = listOf(consultation.homeTone, consultation.longTermTip),
                highlightQuote = consultation.oneLineSummary,
                actionTip = emptyList()
            )
        )
    }

    private fun buildCoverTitle(topic: PremiumTopic, destiny: Int, createdYear: Int): String {
        return when (topic) {
            PremiumTopic.ROMANCE -> "$createdYear 수리 연애 상담소"
            PremiumTopic.CAREER -> "일과 방향 상담소"
            PremiumTopic.MONEY -> "돈의 흐름 상담소"
            PremiumTopic.SELF_ESTEEM -> "마음 기준 상담소"
            PremiumTopic.RELATIONSHIP -> "관계 패턴 상담소"
            else -> "운명수 $destiny 상담소"
        }
    }

    private fun ConsultationAnswerCard.hasContent(): Boolean {
        return question.isNotBlank() || shortAnswer.isNotBlank() || body.isNotEmpty()
    }

    private fun String.firstSentence(): String {
        val sentence = split("。", ".", "!", "?", "\n")
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        return if (sentence.isBlank()) this else sentence
    }
}
