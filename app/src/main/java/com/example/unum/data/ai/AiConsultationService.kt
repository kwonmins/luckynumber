package com.example.unum.data.ai

import com.example.unum.data.ai.compatibility.*
import com.example.unum.data.ai.premium.*
import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.ConsultationTocItem
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.service.JsonChatClient
import com.example.unum.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AiConsultationService(private val client: JsonChatClient) {
    suspend fun personal(apiKey: String, topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): PremiumConsultation = withContext(Dispatchers.IO) {
        val content = client.requestJsonContent(apiKey, com.example.unum.data.ai.premium.OPENAI_MODEL,
            com.example.unum.data.ai.premium.SYSTEM_PROMPT, com.example.unum.data.ai.premium.buildPrompt(topic, concern, bundle), "운세노트")
        PersonalResultParser.parseConsultation(content, topic, bundle)
    }
    suspend fun compatibility(apiKey: String, maleBundle: NumerologyResultBundle, femaleBundle: NumerologyResultBundle, concern: String, relationshipStatus: CompatibilityRelationshipStatus, requesterIsPersonA: Boolean? = null): CompatibilityConsultation = withContext(Dispatchers.IO) {
        val number = relationshipNumber(maleBundle, femaleBundle)
        val content = client.requestJsonContent(apiKey, com.example.unum.data.ai.compatibility.OPENAI_MODEL,
            com.example.unum.data.ai.compatibility.SYSTEM_PROMPT,
            com.example.unum.data.ai.compatibility.buildPrompt(maleBundle, femaleBundle, concern, relationshipStatus, number, requesterIsPersonA), "궁합노트")
        CompatibilityResultParser.parseConsultation(content, maleBundle, femaleBundle, concern, number, relationshipStatus)
    }
}
private object PersonalResultParser {
fun parseConsultation(rawContent: String, topic: PremiumTopic, bundle: NumerologyResultBundle): PremiumConsultation {
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
            bestMonth = json.optString("bestMonth"),
            bestMonthReason = json.optString("bestMonthReason"),
            riskyMonth = json.optString("riskyMonth"),
            riskyMonthReason = json.optString("riskyMonthReason"),
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
private fun parseAnswerCard(json: JSONObject?): ConsultationAnswerCard {
    if (json == null) return ConsultationAnswerCard()
    return ConsultationAnswerCard(
        question = json.optString("question"),
        shortAnswer = json.optString("shortAnswer"),
        body = json.optJSONArray("body").toStringList()
    )
}
private fun parseToc(array: JSONArray?): List<ConsultationTocItem> {
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
private fun parsePages(array: JSONArray?): List<ConsultationPage> {
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
private fun normalizePersonalPages(
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
private fun sanitizePersonalPages(
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
private fun premiumPageFallback(
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
private fun String.withoutJournalingAdvice(fallback: String): String {
    val cleaned = trim()
    if (cleaned.isBlank()) return fallback
    val blocked = listOf("수첩", "메모하세요", "기록하세요", "적어두세요", "일기를", "체크리스트", "복사하기 좋은", "상대에게 보낼")
    val sentences = cleaned
        .split(Regex("(?<=[.!?。])\\s+"))
        .filter { sentence -> blocked.none(sentence::contains) }
    return sentences.joinToString(" ").ifBlank { fallback }
}
private fun readingPointFallback(topic: PremiumTopic): String {
    return "${topic.label} 흐름은 지시보다, 지금 반복되는 분위기를 차분히 읽는 쪽에 가깝습니다."
}
private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}
private fun normalizeMonthInsights(
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
    val bestReason = rawBestReason
    val rawRiskyReason = consultation.riskyMonthReason.takeIf {
        consultation.riskyMonth == expectedRiskyMonth && it.isNotBlank()
    } ?: buildRiskyMonthReason(topic, expectedRiskyMonth, riskySelection)
    val riskyReason = rawRiskyReason
    return consultation.copy(
        bestMonth = expectedBestMonth,
        bestMonthReason = bestReason.limitPremiumText(80),
        riskyMonth = expectedRiskyMonth,
        riskyMonthReason = riskyReason.limitPremiumText(80)
    )
}
private fun buildBestMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
    return "${monthText}은 ${topic.label} 고민을 돌아볼 추천 시기로 제시된 참고 정보입니다. 실제 일정과 준비 상황을 우선하세요."
}
private fun buildRiskyMonthReason(topic: PremiumTopic, monthText: String, selection: PremiumMonthPlanner.MonthSelection): String {
    return "${monthText}은 중요한 결정을 서두르지 말자는 상징적 참고 시기입니다. 실제로 나쁜 일이 생긴다는 뜻은 아닙니다."
}

}

private object CompatibilityResultParser {
fun parseConsultation(
    rawContent: String,
    maleBundle: NumerologyResultBundle,
    femaleBundle: NumerologyResultBundle,
    concern: String,
    relationshipNumber: Int,
    relationshipStatus: CompatibilityRelationshipStatus
): CompatibilityConsultation {
    val jsonText = rawContent
        .substringAfter("{", rawContent)
        .substringBeforeLast("}", rawContent)
        .let { "{$it}" }
    val json = JSONObject(jsonText)
    val answerCard = parseAnswerCard(json.optJSONObject("answerCard")).limitPremiumLength()
    val narrativePlan = compatibilityNarrativePlan(relationshipStatus, relationshipNumber, concern)
    val pages = sanitizeCompatibilityPages(
        normalizeCompatibilityPages(
            pages = parsePages(json.optJSONArray("pages")),
            answerCard = answerCard,
            relationshipNumber = relationshipNumber,
            relationshipStatus = relationshipStatus,
            narrativePlan = narrativePlan
        )
    ).limitPremiumPages()
    val toc = pages.map { ConsultationTocItem(id = it.id, title = it.title) }
    val attraction = pages.getOrNull(0)
    val friction = pages.getOrNull(1)
    val view = pages.getOrNull(2)
    val action = pages.firstOrNull { it.id == "recommendation" } ?: pages.getOrNull(3)
    val closingAdvice = json.optString("closingAdvice")
        .withoutJournalingAdvice("")
        .limitPremiumText(50)
    val fallbackTone = relationshipToneFallback()
    val parsed = CompatibilityConsultation(
        maleEnergy = view?.body?.joinToString("\n\n").orEmpty(),
        femaleEnergy = view?.highlight.orEmpty(),
        relationshipFlow = answerCard.body.joinToString("\n\n"),
        strengths = attraction?.body?.joinToString("\n\n").orEmpty(),
        friction = friction?.body?.joinToString("\n\n").orEmpty(),
        homeTone = action?.body?.joinToString("\n\n").orEmpty().withoutJournalingAdvice(fallbackTone),
        longTermTip = action?.highlight.orEmpty().withoutJournalingAdvice(fallbackTone),
        oneLineSummary = answerCard.shortAnswer.ifBlank { closingAdvice },
        bestMonth = json.optString("bestMonth"),
        bestMonthReason = json.optString("bestMonthReason"),
        riskyMonth = json.optString("riskyMonth"),
        riskyMonthReason = json.optString("riskyMonthReason"),
        coverTitle = json.optString("coverTitle").limitPremiumText(30),
        coverSubtitle = json.optString("coverSubtitle").limitPremiumText(60),
        answerCard = answerCard,
        toc = toc,
        pages = pages,
        closingAdvice = closingAdvice
    )
    return normalizeConsultation(
        consultation = parsed,
        maleBundle = maleBundle,
        femaleBundle = femaleBundle,
        concern = concern,
        relationshipNumber = relationshipNumber,
        relationshipStatus = relationshipStatus
    )
}
private fun normalizeConsultation(
    consultation: CompatibilityConsultation,
    maleBundle: NumerologyResultBundle,
    femaleBundle: NumerologyResultBundle,
    concern: String,
    relationshipNumber: Int,
    relationshipStatus: CompatibilityRelationshipStatus
): CompatibilityConsultation {
    val concernText = concern.takeIf { it.isNotBlank() } ?: "두 사람의 관계"
    val fallbackSummary = "두 사람은 ${relationshipMeaning(relationshipNumber)} 다만 $concernText 안에서는 속도와 표현 방식을 맞추지 않으면 작은 오해가 오래 갈 수 있습니다."
    val relationshipNumbers = relationshipNumbers(maleBundle, femaleBundle, relationshipNumber)
    val currentMonth = PremiumMonthPlanner.currentMonth()
    val bestSelection = PremiumMonthPlanner.pickBestMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth)
    val riskySelection = PremiumMonthPlanner.pickRiskyMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth)
    val bestMonth = bestSelection.toDisplayText()
    val riskyMonth = riskySelection.toDisplayText()
    val narrativePlan = compatibilityNarrativePlan(relationshipStatus, relationshipNumber, concern)
    return consultation.copy(
        relationshipFlow = consultation.relationshipFlow.ifBlank { fallbackSummary },
        strengths = consultation.strengths.ifBlank {
            "서로 다른 결이 만나는 관계라 처음에는 낯설어도, 대화의 리듬이 맞으면 서로에게 필요한 균형을 줄 수 있습니다."
        },
        friction = consultation.friction.ifBlank {
            "감정을 확인하는 속도가 어긋나면 한쪽은 재촉으로, 다른 한쪽은 부담으로 받아들일 수 있습니다. 이 지점을 그냥 넘기면 관계가 차갑게 굳을 수 있습니다."
        },
        homeTone = consultation.homeTone.ifBlank {
            "오래 가려면 중요한 말은 미루지 말고, 다툰 뒤에는 결론보다 회복의 시간을 먼저 정해야 합니다."
        },
        longTermTip = consultation.longTermTip.ifBlank {
            "서로를 바꾸려 하기보다 반응 속도와 표현 방식을 맞추는 것이 관계를 살리는 핵심입니다."
        },
        oneLineSummary = consultation.oneLineSummary.ifBlank { fallbackSummary },
        bestMonth = bestMonth,
        bestMonthReason = (consultation.bestMonthReason.takeIf { consultation.bestMonth == bestMonth && it.isNotBlank() }
            ?: buildCompatibilityBestMonthReason(bestMonth, bestSelection)).limitPremiumText(80),
        riskyMonth = riskyMonth,
        riskyMonthReason = (consultation.riskyMonthReason.takeIf { consultation.riskyMonth == riskyMonth && it.isNotBlank() }
            ?: buildCompatibilityRiskyMonthReason(riskyMonth, riskySelection)).limitPremiumText(80),
        coverTitle = consultation.coverTitle.ifBlank { narrativePlan.coverTitle },
        coverSubtitle = consultation.coverSubtitle.ifBlank { narrativePlan.coverSubtitle }
    )
}
private fun parseAnswerCard(json: JSONObject?): ConsultationAnswerCard {
    if (json == null) return ConsultationAnswerCard()
    return ConsultationAnswerCard(
        question = json.optString("question"),
        shortAnswer = json.optString("shortAnswer"),
        body = json.optJSONArray("body").toStringList()
    )
}
private fun parseToc(array: JSONArray?): List<ConsultationTocItem> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            add(ConsultationTocItem(id = item.optString("id"), title = item.optString("title")))
        }
    }
}
private fun parsePages(array: JSONArray?): List<ConsultationPage> {
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
private fun normalizeCompatibilityPages(
    pages: List<ConsultationPage>,
    answerCard: ConsultationAnswerCard,
    relationshipNumber: Int,
    relationshipStatus: CompatibilityRelationshipStatus,
    narrativePlan: CompatibilityNarrativePlan
): List<ConsultationPage> {
    val byId = pages.associateBy { it.id }
    return narrativePlan.sections.mapIndexed { index, section ->
        val source = byId[section.id] ?: pages.getOrNull(index)
        if (source != null) {
            source.copy(
                id = section.id,
                ribbon = source.ribbon.ifBlank { section.ribbon },
                title = source.title.ifBlank { section.title }
            )
        } else {
            val fallback = compatibilityPageFallback(
                relationshipStatus = relationshipStatus,
                sectionIndex = index,
                relationshipNumber = relationshipNumber,
                answerSummary = answerCard.shortAnswer,
                sectionId = section.id
            )
            ConsultationPage(
                id = section.id,
                ribbon = section.ribbon,
                title = section.title,
                highlight = fallback.first,
                body = fallback.second
            )
        }
    }
}
private fun sanitizeCompatibilityPages(pages: List<ConsultationPage>): List<ConsultationPage> {
    val fallback = relationshipToneFallback()
    return pages.map { page ->
        page.copy(
            highlight = page.highlight.withoutJournalingAdvice(fallback),
            body = page.body.map { it.withoutJournalingAdvice("") }.filter { it.isNotBlank() }
                .ifEmpty { listOf(fallback) }
        )
    }
}
private fun compatibilityPageFallback(
    relationshipStatus: CompatibilityRelationshipStatus,
    sectionIndex: Int,
    relationshipNumber: Int,
    answerSummary: String,
    sectionId: String
): Pair<String, List<String>> {
    val meaning = relationshipMeaning(relationshipNumber)
    val highlights = when (relationshipStatus) {
        CompatibilityRelationshipStatus.COUPLE -> listOf(
            answerSummary.ifBlank { "두 사람의 친밀감은 거창한 표현보다 반복되는 편안함에서 선명해집니다." },
            "감정의 크기보다 확인하는 속도가 다를 때 서운함이 커질 수 있습니다.",
            "갈등 뒤에 얼마나 빨리 답을 내느냐보다 안전하게 다시 말할 수 있느냐가 중요합니다.",
            "이 관계의 지속성은 서로의 생활과 감정을 함께 존중할 때 살아납니다."
        )
        CompatibilityRelationshipStatus.CRUSH -> listOf(
            answerSummary.ifBlank { "호감은 말보다 대화를 이어가려는 반복에서 먼저 드러납니다." },
            "친절한 반응과 관계를 향한 관심은 같은 모습처럼 보여도 지속성에서 차이가 납니다.",
            "지금은 감정을 크게 증명하기보다 서로 편안한 거리를 확인하는 구간입니다.",
            "가능성은 한 사람의 확신보다 두 사람의 반응이 함께 움직일 때 선명해집니다."
        )
        CompatibilityRelationshipStatus.REUNION -> listOf(
            answerSummary.ifBlank { "남아 있는 감정이 곧 다시 만날 준비를 뜻하는 것은 아닙니다." },
            "헤어진 이유가 해결되지 않았다면 그리움이 커도 같은 장면이 반복될 수 있습니다.",
            "연락의 의미는 답장 여부보다 상대가 대화를 편안하게 이어가는지에서 드러납니다.",
            "재회 가능성은 과거로 돌아가는 힘보다 새로운 관계를 만들 변화에서 생깁니다."
        )
    }
    val bodies = when (relationshipStatus) {
        CompatibilityRelationshipStatus.COUPLE -> listOf(
            listOf(meaning, "둘 사이의 애정은 함께 보내는 시간의 양보다 그 안에서 편안하게 자기 모습을 드러낼 수 있는지에 가깝습니다."),
            listOf("한쪽은 바로 확인하고 싶고 다른 한쪽은 생각할 시간이 필요할 수 있습니다.", "이 차이가 무관심이나 집착으로 번역될 때 갈등이 길어집니다."),
            listOf("회복 방식이 다르면 사과를 받아들이는 시점도 어긋납니다.", "결론보다 서로의 감정이 가라앉는 순서를 이해할 때 관계가 다시 부드러워집니다."),
            listOf("오래 가는 관계는 설렘만으로 정해지지 않습니다.", "약속, 휴식, 책임을 나누는 방식이 두 사람에게 공평하게 느껴질수록 안정감이 커집니다.")
        )
        CompatibilityRelationshipStatus.CRUSH -> listOf(
            listOf(meaning, "짧은 대화 뒤에도 상대가 질문을 돌려주고 다음 이야기를 남긴다면 관심의 온도가 이어지고 있다는 신호에 가깝습니다."),
            listOf("한 번의 다정한 반응만으로는 마음의 방향을 단정하기 어렵습니다.", "연락과 만남이 상대 쪽에서도 자연스럽게 이어지는지가 더 정확한 단서가 됩니다."),
            listOf("가까워지는 속도가 빠르면 설렘도 커지지만 상대에게는 부담으로 읽힐 수 있습니다.", "두 사람이 모두 편안하게 대화를 이어가는 범위가 현재 관계의 실제 거리입니다."),
            listOf("마음이 이어질 가능성은 기다린 시간보다 상호적인 반응에서 확인됩니다.", "내 감정만 커지고 상대의 움직임은 멈춰 있다면 관계보다 기대가 앞서 있는 상태일 수 있습니다.")
        )
        CompatibilityRelationshipStatus.REUNION -> listOf(
            listOf(meaning, "그리움에는 사랑뿐 아니라 끝내 말하지 못한 감정과 익숙함도 함께 섞여 있을 수 있습니다."),
            listOf("관계가 멈춘 이유가 한 번의 사건인지 반복된 패턴인지에 따라 재회의 의미가 달라집니다.", "반복된 피로가 원인이었다면 감정 확인만으로는 예전 구조가 바뀌지 않습니다."),
            listOf("상대가 대화를 열어두는지와 단순히 예의를 지키는지는 온도가 다릅니다.", "짧은 답장보다 질문과 감정이 오가는지에서 현재의 경계를 읽을 수 있습니다."),
            listOf("다시 만남이 가능하더라도 예전 자리로 돌아가는 방식은 오래가기 어렵습니다.", "두 사람이 헤어진 원인을 다르게 다룰 수 있을 때 비로소 새로운 관계가 시작됩니다.")
        )
    }
    if (sectionIndex in 0..3) {
        return highlights[sectionIndex] to bodies[sectionIndex]
    }
    val extra = when (sectionId) {
        "opportunity" -> "관계의 가능성은 한 번의 강한 반응보다 서로의 움직임이 반복해서 이어질 때 선명해집니다." to listOf(
            "지금 눈여겨볼 기회는 대화를 계속 이어가려는 반응과 편안한 만남이 자연스럽게 반복되는지에 있습니다.",
            "가능성은 정해진 약속이 아니라 두 사람이 함께 만드는 여지이므로 상대의 현재 상황과 경계도 함께 살펴야 합니다."
        )
        "avoid" -> "불안한 마음으로 답을 재촉하면 관계의 실제 온도를 보기 어려워집니다." to listOf(
            "상대의 한두 번 반응만으로 전체 마음을 단정하거나 감정을 증명받으려는 행동은 거리를 키울 수 있습니다.",
            "관계가 불확실할수록 사실과 기대를 구분하는 태도가 필요합니다."
        )
        "recommendation" -> "지금 필요한 행동은 관계를 몰아가는 것이 아니라 서로의 반응을 확인할 수 있는 크기로 다가가는 것입니다." to listOf(
            "대화는 짧고 분명하게 시작하고, 상대가 질문과 감정을 돌려주는지 살펴보세요.",
            "반응이 편안하게 이어질 때 다음 만남이나 중요한 대화를 제안하는 순서가 부담을 줄입니다."
        )
        else -> "행운의 요소는 관계의 결정을 대신하지 않지만 마음을 차분히 정돈하는 상징이 될 수 있습니다." to listOf(
            "숫자와 색상, 방향, 시간, 요일은 가볍게 참고하고 실제 판단은 두 사람의 대화와 행동을 기준으로 보세요.",
            "관계의 다음 장면은 정해져 있지 않으며 지금의 태도에 따라 충분히 달라질 수 있습니다."
        )
    }
    return extra
}
private fun String.withoutJournalingAdvice(fallback: String): String {
    val cleaned = trim()
    if (cleaned.isBlank()) return fallback
    val blocked = listOf("수첩", "메모하세요", "기록하세요", "적어두세요", "일기를", "체크리스트", "복사하기 좋은", "상대에게 보낼")
    val sentences = cleaned
        .split(Regex("(?<=[.!?。])\\s+"))
        .filter { sentence -> blocked.none(sentence::contains) }
    return sentences.joinToString(" ").ifBlank { fallback }
}
private fun relationshipToneFallback(): String {
    return "이 관계는 지시보다, 서로의 속도와 말의 온도를 차분히 읽는 쪽에 가깝습니다."
}
private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}
private fun buildCompatibilityBestMonthReason(monthText: String, selection: PremiumMonthPlanner.MonthSelection): String {
    return "${monthText}은 두 사람의 관계를 돌아볼 추천 시기로 제시된 참고 정보입니다. 실제 대화와 상대의 의사를 우선하세요."
}
private fun buildCompatibilityRiskyMonthReason(monthText: String, selection: PremiumMonthPlanner.MonthSelection): String {
    return "${monthText}은 관계의 결정을 서두르지 말자는 상징적 참고 시기입니다. 이별이나 갈등을 예고하는 뜻은 아닙니다."
}
private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor
}
