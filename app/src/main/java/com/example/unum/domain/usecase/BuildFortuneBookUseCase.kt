package com.example.unum.domain.usecase

import com.example.unum.data.model.CompatibilityConsultation
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
        return FortuneBook(
            bookId = "${bundle.numbers.code}-$now",
            code = bundle.numbers.code,
            destiny = bundle.numbers.destiny,
            early = bundle.numbers.early,
            middle = bundle.numbers.middle,
            late = bundle.numbers.late,
            concernTopic = topicLabel,
            concernText = concern,
            coverTitle = buildCoverTitle(topicLabel, bundle.numbers.destiny),
            coverSubtitle = "운명수 ${bundle.numbers.destiny} · ${bundle.content.destinyProfile.title} · $createdYear",
            summary = consultation.core,
            bookType = FortuneBookType.PERSONAL,
            bestMonth = consultation.bestMonth,
            bestMonthReason = consultation.bestMonthReason,
            riskyMonth = consultation.riskyMonth,
            riskyMonthReason = consultation.riskyMonthReason,
            chapters = buildPersonalBookChapters(consultation, topicLabel),
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

        return FortuneBook(
            bookId = "compatibility-${maleBundle.numbers.code}-${femaleBundle.numbers.code}-$now",
            code = "${maleBundle.numbers.code}/${femaleBundle.numbers.code}",
            destiny = relationshipNumber,
            early = maleBundle.numbers.early,
            middle = femaleBundle.numbers.middle,
            late = relationshipNumber,
            concernTopic = "궁합",
            concernText = concern,
            coverTitle = buildCompatibilityCoverTitle(relationshipNumber),
            coverSubtitle = "남자 $maleBirthLabel · 여자 $femaleBirthLabel",
            summary = consultation.oneLineSummary,
            bookType = FortuneBookType.COMPATIBILITY,
            relationshipNumber = relationshipNumber,
            maleBirthLabel = maleBirthLabel,
            femaleBirthLabel = femaleBirthLabel,
            maleDestiny = maleBundle.numbers.destiny,
            femaleDestiny = femaleBundle.numbers.destiny,
            maleCode = maleBundle.numbers.code,
            femaleCode = femaleBundle.numbers.code,
            chapters = buildCompatibilityBookChapters(consultation, maleBundle, femaleBundle, relationshipNumber),
            createdAt = now,
            lastOpenedAt = now,
            purchasedAt = now,
            coverTheme = "compatibility"
        )
    }

    private fun buildPersonalBookChapters(
        consultation: PremiumConsultation,
        topicLabel: String
    ): List<FortuneBookChapter> {
        return listOf(
            FortuneBookChapter(
                title = "지금의 핵심",
                lead = "성향은 짧게만 짚고, 지금 적어둔 질문에서 실제로 걸리는 지점부터 봅니다.",
                body = listOf(
                    consultation.core,
                    "이 장에서는 성향 설명을 길게 반복하지 않고, 지금 고민이 생활 속에서 어떤 압박으로 나타나는지 먼저 짚습니다."
                ),
                highlightQuote = consultation.oneLineAdvice,
                actionTip = listOf(
                    "오늘 가장 크게 마음에 남는 고민을 한 문장으로 적어보세요.",
                    "이번 주 안에 바로 움직일 수 있는 작은 선택 하나를 정해보세요."
                )
            ),
            FortuneBookChapter(
                title = "상황별 해석",
                lead = "사용자가 던진 질문이 실제 장면에서 어떻게 반복되는지 봅니다.",
                body = listOf(
                    consultation.interpretation,
                    "핵심은 성향 이름이 아니라, 같은 장면에서 반복되는 선택과 반응을 어떻게 바꿀지입니다."
                ),
                highlightQuote = "질문은 막연할수록 커지고, 장면으로 나누면 바로 다룰 수 있습니다.",
                actionTip = listOf(
                    "반복해서 나오는 감정 반응이 무엇인지 먼저 적어보세요.",
                    "무리해서 바꾸려 하기보다 강점이 살아나는 환경을 더 자주 선택해보세요."
                )
            ),
            FortuneBookChapter(
                title = "주의할 장면",
                lead = "가까운 사람과의 대화, 기대, 거리감에서 생기는 실제 피로를 살펴봅니다.",
                body = listOf(
                    "이번 $topicLabel 고민에서는 말투, 답장 속도, 부탁과 거절 같은 작은 장면이 생각보다 크게 작동합니다.",
                    "여기서 내 감정을 계속 눌러두거나 반대로 너무 빨리 밀어붙이면, 문제는 해결되지 않고 더 피곤한 방식으로 반복될 수 있습니다."
                ),
                highlightQuote = "관계는 작은 불편함을 대충 넘길수록 나중에 더 큰 피로로 돌아올 수 있습니다.",
                actionTip = listOf(
                    "상대의 반응만 읽지 말고 내 감정의 기준도 함께 적어보세요.",
                    "불편한 신호를 느꼈다면 속도를 늦추고 다시 확인해보세요."
                )
            ),
            FortuneBookChapter(
                title = "이번 달 행동",
                lead = "오늘, 이번 주, 한 달 안에 실제로 바꿔야 할 행동을 정리합니다.",
                body = listOf(
                    consultation.direction,
                    "현실 행동을 미루면 고민은 줄어드는 게 아니라 더 복잡한 형태로 돌아올 수 있습니다."
                ),
                highlightQuote = "좋은 흐름은 큰 결심보다 바로 바꿀 수 있는 행동에서 살아납니다.",
                actionTip = listOf(
                    "지금 가장 현실적인 우선순위 한 가지를 먼저 정해보세요.",
                    "지출, 일정, 관계 중 가장 흔들리는 축을 하나만 골라 정리해보세요."
                )
            ),
            FortuneBookChapter(
                title = "월별 흐름",
                lead = "그냥 넘겼을 때 삶이 더 힘들어질 수 있는 지점을 분명히 짚습니다.",
                body = listOf(
                    consultation.caution,
                    "이 경고는 겁을 주기 위한 말이 아니라, 지금부터 피해야 할 장면을 알아두기 위한 기준입니다."
                ),
                highlightQuote = "조심할 점을 미리 아는 것은 중심을 지키기 위한 현실적인 준비입니다.",
                actionTip = listOf(
                    "결정이 흔들릴 때는 감정, 현실, 관계를 따로 나눠서 보세요.",
                    "지금 지켜야 할 기준 하나를 짧게 정리해두세요."
                )
            ),
            FortuneBookChapter(
                title = "다시 읽는 정리",
                lead = "이 리포트를 다시 열어볼 때 가장 먼저 확인하면 좋은 포인트입니다.",
                body = listOf(
                    consultation.oneLineAdvice,
                    "한 달 뒤 이 문장을 다시 읽었을 때 같은 고민이 반복되고 있다면, 생각이 아니라 행동 루틴을 바꿔야 할 때입니다."
                ),
                highlightQuote = consultation.oneLineAdvice,
                actionTip = listOf(
                    "한 달 뒤 다시 읽으며 실제로 달라진 점을 체크해보세요.",
                    "이번 책에서 가장 와닿았던 문장 하나를 메모해두세요."
                )
            )
        )
    }

    private fun buildCompatibilityBookChapters(
        consultation: CompatibilityConsultation,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int
    ): List<FortuneBookChapter> {
        val relationshipMeaning = when (relationshipNumber) {
            0 -> "여백과 가능성이 큰 관계의 결이 중심이 됩니다."
            1 -> "빠르게 시작되고 주도권이 살아나는 관계의 결입니다."
            2 -> "배려와 조화, 정서적 균형이 중심이 되는 관계입니다."
            3 -> "대화와 표현, 해석의 리듬이 강한 관계입니다."
            4 -> "생활 기반과 규칙, 안정감을 함께 세우는 관계입니다."
            5 -> "변화와 활기, 설렘의 움직임이 큰 관계입니다."
            6 -> "책임과 생활력, 오래 가는 안정감이 강한 관계입니다."
            7 -> "집중과 추진력이 커서 함께 몰입하는 힘이 강한 관계입니다."
            8 -> "연결과 대인관계, 외부 확장의 힘이 살아나는 관계입니다."
            else -> "완성과 정리, 깊은 결론성이 도는 관계입니다."
        }

        return listOf(
            FortuneBookChapter(
                title = "남자의 기본 기운",
                lead = "남자 쪽 성향이 실제 관계 장면에서 어떻게 튀어나오는지 먼저 읽습니다.",
                body = listOf(
                    consultation.maleEnergy,
                    "무료 성향은 배경으로만 두고, 여기서는 연락, 약속, 말투 같은 구체적인 장면에서의 반응을 중심으로 봅니다."
                ),
                highlightQuote = extractHighlight(consultation.maleEnergy),
                actionTip = listOf(
                    "남자 쪽은 주도하려는 순간과 물러나는 순간의 차이를 먼저 살펴보세요.",
                    "감정을 길게 품기보다 핵심만 짧게 꺼내는 습관이 도움이 됩니다."
                )
            ),
            FortuneBookChapter(
                title = "여자의 기본 기운",
                lead = "여자 쪽 성향이 관계 안에서 어떤 온도와 반응을 만드는지 살펴봅니다.",
                body = listOf(
                    consultation.femaleEnergy,
                    "기본 운세를 다시 설명하기보다, 서운함이 쌓이는 순간과 마음이 닫히는 장면을 중심으로 읽습니다."
                ),
                highlightQuote = extractHighlight(consultation.femaleEnergy),
                actionTip = listOf(
                    "여자 쪽은 배려와 자기 기준이 어디서 엇갈리는지 먼저 보세요.",
                    "상대의 속도에 맞추기 전에 내 마음의 기준도 함께 확인해보세요."
                )
            ),
            FortuneBookChapter(
                title = "둘의 궁합수",
                lead = "궁합수 ${relationshipNumber}가 두 사람 사이에서 어떤 기류를 만드는지 읽습니다.",
                body = listOf(
                    consultation.relationshipFlow,
                    "$relationshipMeaning 이 흐름은 실제로는 연락 속도, 만나는 빈도, 서로의 생활 리듬을 맞추는 방식에서 가장 먼저 드러납니다."
                ),
                highlightQuote = extractHighlight(consultation.relationshipFlow),
                actionTip = listOf(
                    "둘의 관계가 빠르게 깊어지는지, 천천히 다져지는지 먼저 합의해보세요.",
                    "궁합수의 흐름을 장점으로 쓰려면 속도와 방향을 함께 맞추는 게 중요합니다."
                )
            ),
            FortuneBookChapter(
                title = "잘 맞는 지점",
                lead = "두 사람이 편안하게 힘을 주고받는 장면을 정리합니다.",
                body = listOf(consultation.strengths),
                highlightQuote = extractHighlight(consultation.strengths),
                actionTip = listOf(
                    "둘이 잘 통하는 주제나 생활 리듬을 하나 정해 자주 반복해보세요.",
                    "장점이 잘 살아나는 상황을 관계의 기본 루틴으로 만들어보세요."
                )
            ),
            FortuneBookChapter(
                title = "부딪히기 쉬운 지점",
                lead = "자주 엇갈릴 수 있는 패턴을 미리 알고 넘어갑니다.",
                body = listOf(consultation.friction),
                highlightQuote = extractHighlight(consultation.friction),
                actionTip = listOf(
                    "서운함이 쌓이기 전에 짧고 구체적으로 말하는 연습이 필요합니다.",
                    "누가 맞는지보다 지금 어떤 리듬이 깨졌는지부터 확인해보세요."
                )
            ),
            FortuneBookChapter(
                title = "집안 분위기와 대화 온도",
                lead = "함께 있을 때 말의 세기와 집 안의 에너지가 어떻게 움직이는지 봅니다.",
                body = listOf(consultation.homeTone),
                highlightQuote = extractHighlight(consultation.homeTone),
                actionTip = listOf(
                    "활기가 큰 관계일수록 쉬는 시간과 조용한 시간을 같이 정해두면 좋습니다.",
                    "감정 표현이 큰 날에는 결론보다 진정이 먼저라는 약속을 만들어보세요."
                )
            ),
            FortuneBookChapter(
                title = "오래 가는 팁",
                lead = "이 관계를 더 편안하고 길게 이어가기 위한 현실적인 조언입니다.",
                body = listOf(consultation.longTermTip, consultation.oneLineSummary),
                highlightQuote = consultation.oneLineSummary,
                actionTip = listOf(
                    "관계를 정의하려 하기보다 서로가 편안해지는 방식을 먼저 쌓아보세요.",
                    "한 달에 한 번은 두 사람의 속도와 기대를 다시 점검해보세요."
                )
            )
        )
    }

    private fun buildCoverTitle(topicLabel: String, destiny: Int): String {
        return when (topicLabel) {
            PremiumTopic.ROMANCE.label -> "연애 운세노트"
            PremiumTopic.CAREER.label -> "일과 방향 노트"
            PremiumTopic.MONEY.label -> "돈의 흐름 노트"
            PremiumTopic.SELF_ESTEEM.label -> "자기 기준 노트"
            PremiumTopic.RELATIONSHIP.label -> "관계 패턴 노트"
            else -> "운명수 ${destiny} 노트"
        }
    }

    private fun buildCompatibilityCoverTitle(relationshipNumber: Int): String {
        return when (relationshipNumber) {
            0 -> "열린 가능성을 읽는 궁합"
            1 -> "빠르게 끌리는 두 사람의 궁합"
            2 -> "마음의 온도를 보는 궁합"
            3 -> "대화가 살아나는 두 사람의 궁합"
            4 -> "생활과 기반을 보는 궁합"
            5 -> "설렘과 변화를 품은 궁합"
            6 -> "오래 가는 현실 궁합"
            7 -> "함께 몰입하는 두 사람의 궁합"
            8 -> "인연과 확장이 살아나는 궁합"
            else -> "깊이와 결론성이 큰 궁합"
        }
    }

    private fun extractHighlight(text: String): String {
        return text.split("。", ".", "!", "?", "\n")
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?.let { if (it.endsWith(".")) it else "$it." }
            ?: "지금의 흐름을 너무 서두르지 말고 천천히 읽어보세요."
    }
}
