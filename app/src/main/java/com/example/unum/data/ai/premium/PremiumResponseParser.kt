package com.example.unum.data.ai.premium

import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.ConsultationTocItem
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import org.json.JSONArray
import org.json.JSONObject

import com.example.unum.domain.usecase.*

internal fun parseConsultation(rawContent: String, topic: PremiumTopic, bundle: NumerologyResultBundle): PremiumConsultation {
    val jsonText = rawContent
        .substringAfter("{", rawContent)
        .substringBeforeLast("}", rawContent)
        .let { "{$it}" }
    val json = JSONObject(jsonText)
    if (json.has("pages") || json.has("answerCard")) {
        val answerCard = parseAnswerCard(json.optJSONObject("answerCard")).limitPremiumLength()
        val pages = sanitizePersonalPages(
            pages = normalizePersonalPages(
                pages = parsePages(json.optJSONArray("pages")),
                answerCard = answerCard,
                topic = topic
            ),
            topic = topic
        ).limitPremiumPages()
        val toc = pages.map { ConsultationTocItem(id = it.id, title = it.title) }
        val cautionPage = pages.firstOrNull { it.id == "caution" }
        val actionPage = pages.firstOrNull { it.id == "recommendation" }
        val currentPage = pages.firstOrNull { it.id == "current_flow" }
        val opportunityPage = pages.firstOrNull { it.id == "opportunity" }
        val closingAdvice = json.optString("closingAdvice")
            .withoutJournalingAdvice("")
            .limitPremiumText(50)
        val readingPoint = readingPointFallback(topic)
        val parsed = PremiumConsultation(
            core = answerCard.shortAnswer.ifBlank { answerCard.body.firstOrNull().orEmpty() },
            interpretation = listOf(currentPage, opportunityPage)
                .filterNotNull()
                .flatMap { it.body }
                .joinToString("\n\n"),
            caution = cautionPage?.body?.joinToString("\n\n").orEmpty(),
            direction = actionPage?.body?.joinToString("\n\n").orEmpty().withoutJournalingAdvice(readingPoint),
            oneLineAdvice = closingAdvice.ifBlank { actionPage?.highlight.orEmpty() }.withoutJournalingAdvice(""),
            coverTitle = json.optString("coverTitle").limitPremiumText(30),
            coverSubtitle = json.optString("coverSubtitle").limitPremiumText(60),
            answerCard = answerCard,
            toc = toc,
            pages = pages,
            closingAdvice = closingAdvice
        )
        return normalizeMonthInsights(parsed, topic, bundle)
    }
    val parsed = PremiumConsultation(
        core = json.optString("core"),
        interpretation = json.optString("interpretation"),
        caution = json.optString("caution"),
        direction = json.optString("direction").withoutJournalingAdvice(readingPointFallback(topic)),
        oneLineAdvice = json.optString("oneLineAdvice").withoutJournalingAdvice(""),
        bestMonth = json.optString("bestMonth"),
        bestMonthReason = json.optString("bestMonthReason"),
        riskyMonth = json.optString("riskyMonth"),
        riskyMonthReason = json.optString("riskyMonthReason")
    )
    return normalizeMonthInsights(parsed, topic, bundle)
}

internal fun parseAnswerCard(json: JSONObject?): ConsultationAnswerCard {
    if (json == null) return ConsultationAnswerCard()
    return ConsultationAnswerCard(
        question = json.optString("question"),
        shortAnswer = json.optString("shortAnswer"),
        body = json.optJSONArray("body").toStringList()
    )
}

internal fun parseToc(array: JSONArray?): List<ConsultationTocItem> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            add(
                ConsultationTocItem(
                    id = item.optString("id"),
                    title = item.optString("title")
                )
            )
        }
    }
}

internal fun parsePages(array: JSONArray?): List<ConsultationPage> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            add(
                ConsultationPage(
                    id = item.optString("id"),
                    ribbon = item.optString("ribbon"),
                    title = item.optString("title"),
                    highlight = item.optString("highlight"),
                    body = item.optJSONArray("body").toStringList()
                )
            )
        }
    }
}

internal fun normalizePersonalPages(
    pages: List<ConsultationPage>,
    answerCard: ConsultationAnswerCard,
    topic: PremiumTopic
): List<ConsultationPage> {
    val byId = pages.associateBy { it.id }
    val sections = premiumSections(topic)
    return sections.mapIndexed { index, section ->
        val source = byId[section.id] ?: pages.getOrNull(index)
        source?.copy(
            id = section.id,
            ribbon = source.ribbon.ifBlank { section.ribbon },
            title = source.title.ifBlank { section.title }
        ) ?: premiumPageFallback(section, topic, answerCard.shortAnswer)
    }
}

internal fun sanitizePersonalPages(
    pages: List<ConsultationPage>,
    topic: PremiumTopic
): List<ConsultationPage> {
    val fallback = readingPointFallback(topic)
    return pages.map { page ->
        page.copy(
            highlight = page.highlight.withoutJournalingAdvice(fallback),
            body = page.body.map { it.withoutJournalingAdvice("") }.filter { it.isNotBlank() }
                .ifEmpty { listOf(fallback) }
        )
    }
}

internal fun premiumPageFallback(
    section: PremiumSectionSpec,
    topic: PremiumTopic,
    answerSummary: String
): ConsultationPage {
    val summary = answerSummary.ifBlank {
        "${topic.label}은 한 번의 결과보다 현재 반복되는 선택과 생활 장면을 함께 살필 때 더 정확하게 읽힙니다."
    }
    val body = when (section.id) {
        "current_flow" -> listOf(summary, "지금의 고민이 커진 이유와 최근 반복된 장면을 나누어 보면 현재 위치가 조금 더 선명해집니다.")
        "good_energy" -> listOf("이미 잘하고 있는 부분은 쉽게 사라지지 않습니다.", "익숙해서 대수롭지 않게 여겼던 강점이 이번 시기에는 실제 기회를 붙잡는 기반이 됩니다.")
        "caution" -> listOf("불안할수록 결론을 서두르거나 한 가지 반응만 보고 전체를 판단하기 쉽습니다.", "문제가 커지기 전에 속도와 범위를 조절하면 충분히 다른 결과를 만들 수 있습니다.")
        "opportunity" -> listOf("기회는 갑작스러운 행운보다 이미 이어지고 있는 제안과 관계 속에서 먼저 보입니다.", "반복해서 눈에 들어오는 선택지를 현실 조건과 함께 살피는 것이 중요합니다.")
        "avoid" -> listOf("확인되지 않은 기대만으로 큰 결정을 내리는 행동은 피하는 편이 안전합니다.", "감정이 가장 큰 순간보다 사실과 조건이 함께 보이는 순간의 판단이 오래갑니다.")
        "recommendation" -> listOf("오늘 할 수 있는 행동은 가장 영향이 큰 한 가지를 먼저 처리하는 것입니다.", "필요한 대화나 확인을 미루지 않되, 상대와 상황이 받아들일 수 있는 크기로 시작하세요.")
        "lucky_elements" -> listOf("행운의 숫자와 색상, 방향, 시간, 요일은 결정을 대신하는 예언이 아니라 마음을 정돈하는 상징으로 활용할 수 있습니다.", "생활 속에서 부담 없이 떠올리는 정도가 가장 적절합니다.")
        else -> listOf("지금의 흐름은 고정된 결론이 아닙니다.", "현재의 선택을 조금 더 분명하게 바라보는 것만으로도 다음 장면은 달라질 수 있습니다.")
    }
    return ConsultationPage(
        id = section.id,
        ribbon = section.ribbon,
        title = section.title,
        highlight = body.first(),
        body = body
    )
}

internal fun String.withoutJournalingAdvice(fallback: String): String {
    val cleaned = trim()
    if (cleaned.isBlank()) return fallback
    val blocked = listOf("수첩", "메모하세요", "기록하세요", "적어두세요", "일기를", "체크리스트", "복사하기 좋은", "상대에게 보낼")
    val sentences = cleaned
        .split(Regex("(?<=[.!?。])\\s+"))
        .filter { sentence -> blocked.none(sentence::contains) }
    return sentences.joinToString(" ").ifBlank { fallback }
}

internal fun readingPointFallback(topic: PremiumTopic): String {
    return "${topic.label} 흐름은 지시보다, 지금 반복되는 분위기를 차분히 읽는 쪽에 가깝습니다."
}

internal fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}

internal fun normalizeMonthInsights(
    consultation: PremiumConsultation,
    topic: PremiumTopic,
    bundle: NumerologyResultBundle
): PremiumConsultation {
    val currentMonth = PremiumMonthPlanner.currentMonth()
    val bestSelection = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, currentMonth)
    val riskySelection = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers, currentMonth)
    val expectedBestMonth = bestSelection.toDisplayText()
    val expectedRiskyMonth = riskySelection.toDisplayText()
    val rawBestReason = consultation.bestMonthReason.takeIf {
        consultation.bestMonth == expectedBestMonth && it.isNotBlank()
    } ?: buildBestMonthReason(topic, expectedBestMonth, bundle)
    val bestReason = withBestMonthTimingNote(rawBestReason, bestSelection)
    val rawRiskyReason = consultation.riskyMonthReason.takeIf {
        consultation.riskyMonth == expectedRiskyMonth && it.isNotBlank()
    } ?: buildRiskyMonthReason(topic, expectedRiskyMonth, riskySelection)
    val riskyReason = withRiskyMonthTimingNote(rawRiskyReason, riskySelection)

    return consultation.copy(
        bestMonth = expectedBestMonth,
        bestMonthReason = bestReason.limitPremiumText(80),
        riskyMonth = expectedRiskyMonth,
        riskyMonthReason = riskyReason.limitPremiumText(80)
    )
}

internal fun withBestMonthTimingNote(reason: String, selection: PremiumMonthPlanner.MonthSelection): String {
    val passedTopMonth = selection.replacedPastMonth ?: return reason
    val alreadyExplained = reason.contains("${passedTopMonth}월은 이미 지났")
    if (alreadyExplained) return reason
    val displayMonth = selection.toDisplayText()
    return if (selection.isNextYear) {
        "올해 가장 추천 흐름이 강했던 ${passedTopMonth}월은 이미 지났습니다. 올해 남은 구간에는 같은 결이 약하게 지나가므로, 다음 해에 가장 먼저 돌아오는 ${selection.month}월을 다음 추천 구간으로 보세요. $reason"
    } else {
        "올해 가장 추천 흐름이 강했던 ${passedTopMonth}월은 이미 지났습니다. 지금 이후에는 ${displayMonth}을 추천 구간으로 보세요. $reason"
    }
}

internal fun withRiskyMonthTimingNote(reason: String, selection: PremiumMonthPlanner.MonthSelection): String {
    val passedTopMonth = selection.replacedPastMonth ?: return reason
    val alreadyExplained = reason.contains("${passedTopMonth}월은 이미 지났")
    if (alreadyExplained) return reason
    return if (selection.isNextYear) {
        "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났고, 올해 남은 구간에는 같은 결의 주의 달이 약하게 지나갑니다. 그래서 다음 해에 가장 먼저 돌아오는 ${selection.month}월을 다음 주의 구간으로 봅니다. $reason"
    } else {
        "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났으니, 지금 이후에는 ${selection.month}월을 다음 주의 구간으로 보세요. $reason"
    }
}

internal fun buildBestMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
    return when (topic) {
        PremiumTopic.ROMANCE ->
            "${monthText}에는 마음을 새롭게 열기 좋은 흐름이 강합니다. ${bundle.content.destinyProfile.title}의 결을 살려 가볍고 진심 어린 대화부터 시작하면 관계의 문이 부드럽게 열립니다."
        PremiumTopic.CAREER ->
            "${monthText}에는 방향을 정리하고 실제 행동으로 옮기기 좋은 기운이 모입니다. 준비해둔 포트폴리오, 지원, 제안처럼 손에 잡히는 움직임을 만들기 좋습니다."
        PremiumTopic.MONEY ->
            "${monthText}에는 돈의 흐름을 구조화하기 좋습니다. 큰 욕심보다 수입과 지출의 길을 또렷하게 나누면 기회가 안정적으로 이어집니다."
        PremiumTopic.STUDY ->
            "${monthText}에는 집중해야 할 범위가 선명해지고 학습 리듬을 안정시키기 좋습니다. 취약한 영역을 반복해서 보완하면 시험과 과제에서 실수가 줄어듭니다."
        PremiumTopic.HEALTH ->
            "${monthText}에는 수면과 활동 리듬을 다시 맞추기 좋습니다. 무리한 변화보다 꾸준한 휴식과 식사 시간을 지키는 쪽이 컨디션 회복에 도움이 됩니다."
        PremiumTopic.BUSINESS ->
            "${monthText}에는 거래 조건을 정리하고 제안이나 계약을 구체화하기 좋습니다. 확장보다 수익 구조와 책임 범위를 분명히 하면 기회가 안정적으로 이어집니다."
        PremiumTopic.GENERAL ->
            "${monthText}에는 일, 돈, 관계의 우선순위가 선명해집니다. 가장 중요한 한 영역에 힘을 모으면 다른 문제도 함께 정리되기 쉽습니다."
        PremiumTopic.SELF_ESTEEM ->
            "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 남의 반응보다 작은 약속을 지키는 경험을 쌓을수록 마음의 중심이 단단해집니다."
        PremiumTopic.RELATIONSHIP ->
            "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋은 달입니다."
    }
}

internal fun buildRiskyMonthReason(
    topic: PremiumTopic,
    monthText: String,
    selection: PremiumMonthPlanner.MonthSelection
): String {
    val baseReason = when (topic) {
        PremiumTopic.ROMANCE ->
            "${monthText}에는 관계의 움직임이 커지는 만큼 조급함도 함께 올라올 수 있습니다. 마음이 앞서 과하게 다가가면 상대가 부담을 느껴 관계가 더 꼬일 수 있으니, 상대의 속도와 여백을 각별히 조심해야 합니다."
        PremiumTopic.CAREER ->
            "${monthText}에는 변화 욕구가 커져 성급한 결정으로 흐르기 쉽습니다. 퇴사, 이직, 계약을 급하게 밀어붙이면 커리어가 예상보다 더 힘들어질 수 있으니, 큰 선택은 한 번 더 검토한 뒤 움직이는 편이 안전합니다."
        PremiumTopic.MONEY ->
            "${monthText}에는 빠른 이익을 좇고 싶은 마음이 강해질 수 있습니다. 무리한 투자나 충동 지출을 가볍게 보면 돈의 흐름이 한 번에 무너질 수 있으니, 확인되지 않은 제안은 반드시 거리를 두는 것이 좋습니다."
        PremiumTopic.STUDY ->
            "${monthText}에는 불안 때문에 계획만 늘리거나 밤샘으로 밀어붙이기 쉽습니다. 학습량보다 수면과 복습의 질이 떨어지면 실수가 커질 수 있으니 범위를 줄이는 편이 안전합니다."
        PremiumTopic.HEALTH ->
            "${monthText}에는 피로 신호를 무시하고 일정을 이어가기 쉽습니다. 몸의 불편함이 계속되면 운세 해석보다 의료진의 진료를 우선하고 생활 강도를 낮추는 편이 안전합니다."
        PremiumTopic.BUSINESS ->
            "${monthText}에는 확장 욕구가 커져 계약 조건과 비용을 낙관적으로 보기 쉽습니다. 구두 약속이나 검증되지 않은 투자 제안은 문서와 숫자를 확인하기 전까지 거리를 두는 편이 안전합니다."
        PremiumTopic.GENERAL ->
            "${monthText}에는 여러 문제를 한 번에 해결하려다 판단이 흐려질 수 있습니다. 큰 결정을 겹쳐 진행하기보다 가장 영향이 큰 한 가지부터 확인하는 편이 안전합니다."
        PremiumTopic.SELF_ESTEEM ->
            "${monthText}에는 비교와 조급함이 커지기 쉽습니다. 결과를 빨리 증명하려고 무리하면 자존감과 컨디션이 같이 무너질 수 있으니, 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
        PremiumTopic.RELATIONSHIP ->
            "${monthText}에는 사람 사이의 반응이 커져 오해도 빨리 번질 수 있습니다. 단정적인 말이나 압박을 계속하면 관계가 생각보다 차갑게 틀어질 수 있으니, 중요한 대화는 차분히 시간을 두는 편이 좋습니다."
    }
    val passedTopMonth = selection.replacedPastMonth ?: return baseReason
    return if (selection.isNextYear) {
        "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났고, 올해 남은 구간에는 같은 결의 주의 달이 약하게 지나갑니다. 그래서 다음 해에 가장 먼저 돌아오는 ${selection.month}월을 다음 주의 구간으로 봅니다. $baseReason"
    } else {
        "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났으니, 지금 이후에는 ${selection.month}월을 다음 주의 구간으로 보세요. $baseReason"
    }
}
