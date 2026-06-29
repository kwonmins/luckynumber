package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.ConsultationTocItem
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.domain.service.OpenAiChatClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeneratePremiumConsultationUseCase(
    private val chatClient: OpenAiChatClient = OpenAiChatClient()
) {
    suspend operator fun invoke(
        apiKey: String,
        topic: PremiumTopic,
        concern: String,
        bundle: NumerologyResultBundle
    ): PremiumConsultation = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(topic, concern, bundle)
        val content = chatClient.requestJsonContent(
            apiKey = apiKey,
            model = OPENAI_MODEL,
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = prompt,
            failureLabel = "운세노트"
        )
        parseConsultation(content, topic, bundle)
    }

    private fun buildPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
        if (topic == PremiumTopic.ROMANCE) {
            return buildRomanceSalonPrompt(topic, concern, bundle)
        }

        val input = bundle.input
        val destiny = bundle.content.destinyProfile
        val hiddenCue = buildHiddenBirthCue(input)
        val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }
        val traitBrief = buildTraitBrief(destiny.title, destiny.coreKeywords, destiny.cautionKeywords)
        val currentMonth = PremiumMonthPlanner.currentMonth()

        return buildCompactPremiumPrompt(
            topic = topic,
            concernText = concernText,
            bundle = bundle,
            hiddenCue = hiddenCue,
            traitBrief = traitBrief,
            currentMonth = currentMonth
        )

    }

    private fun buildRomanceSalonPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
        val destiny = bundle.content.destinyProfile
        val concernText = concern.ifBlank { "요즘 연애에서 어떤 흐름이 열릴지 알고 싶습니다." }
        val traitBrief = buildTraitBrief(destiny.title, destiny.coreKeywords, destiny.cautionKeywords)
        val cautionKeywords = destiny.cautionKeywords.take(3).joinToString(", ").ifBlank { "조급함, 과한 확인, 혼자 결론 내리기" }
        val createdYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bestMonth = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, currentMonth).toDisplayText()
        val riskyMonth = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers, currentMonth).toDisplayText()

        return buildCompactRomancePrompt(
            concernText = concernText,
            bundle = bundle,
            traitBrief = traitBrief,
            cautionKeywords = cautionKeywords,
            createdYear = createdYear,
            currentMonth = currentMonth,
            bestMonth = bestMonth,
            riskyMonth = riskyMonth
        )

    }

    private fun buildCompactPremiumPrompt(
        topic: PremiumTopic,
        concernText: String,
        bundle: NumerologyResultBundle,
        hiddenCue: String,
        traitBrief: String,
        currentMonth: Int
    ): String {
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        return """
            Write Korean premium counseling JSON for ${topic.label}.
            Input: concern="$concernText"; birth=${displayInput.year}.${displayInput.month}.${displayInput.day}; calendar=${if (displayInput.calendarType == CalendarType.LUNAR) "lunar" else "solar"}; gender=${displayInput.gender.label}; currentMonth=$currentMonth.
            Cues: destiny=${numbers.destiny}; polarity=${NumerologyCalculator.destinyPolarity(numbers.destiny).label}; traits="$traitBrief"; caution="${destiny.cautionKeywords.take(2).joinToString(", ")}"; hidden="$hiddenCue".
            Style: polite Korean, direct to the user, concrete scenes and emotional cues, no long personality recap, no system-name terms, no "선생님", no fear-mongering.
            Do not write task lists or "what to do today" advice. Avoid notebook, memo, journaling, recording, checklist, routine-building, "write it down", "try this today", "this week", or "within a month" instructions.
            Do not create copy-ready/share-ready sections. Avoid labels or phrases like "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", or "상대에게 보내기 좋은 말".
            Length: each field 2-4 short mobile sentences. Avoid repeated phrasing.
            Month fields: choose one best and one risky month from now onward, with timing/context reasons. Do not explain calculations.
            Return only valid JSON:
            {"core":"","interpretation":"","caution":"","direction":"","oneLineAdvice":"","bestMonth":"","bestMonthReason":"","riskyMonth":"","riskyMonthReason":""}
        """.trimIndent()
    }

    private fun buildCompactRomancePrompt(
        concernText: String,
        bundle: NumerologyResultBundle,
        traitBrief: String,
        cautionKeywords: String,
        createdYear: Int,
        currentMonth: Int,
        bestMonth: String,
        riskyMonth: String
    ): String {
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        return """
            Write Korean romance counseling page JSON.
            Input: concern="$concernText"; year=$createdYear; currentMonth=$currentMonth; birth=${displayInput.year}.${displayInput.month}.${displayInput.day}; gender=${displayInput.gender.label}; destiny=${numbers.destiny}; traits="$traitBrief"; caution="$cautionKeywords"; bestMonth="$bestMonth"; riskyMonth="$riskyMonth".
            Style: warm but realistic, mobile-friendly, concrete dating scenes and relationship tone. No long trait recap, no system-name terms, no "선생님", no code block.
            Rules: one message per page; body arrays have 2 short paragraphs; avoid repeated phrasing.
            Do not write task lists or "what to do today" advice. Avoid notebook, memo, journaling, recording, checklist, routine-building, "write it down", "try this today", "this week", or "within a month" instructions.
            Do not create copy-ready/share-ready sections. Avoid labels or phrases like "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", or "상대에게 보내기 좋은 말".
            Return only valid JSON:
            {"coverTitle":"","coverSubtitle":"","answerCard":{"question":"","shortAnswer":"","body":["",""]},"toc":[{"id":"timing","title":""},{"id":"person","title":""},{"id":"caution","title":""},{"id":"action","title":""}],"pages":[{"id":"timing","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"person","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"caution","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"action","ribbon":"","title":"","highlight":"","body":["",""]}],"closingAdvice":""}
        """.trimIndent()
    }

    private fun buildShortPremiumPrompt(
        topic: PremiumTopic,
        concernText: String,
        bundle: NumerologyResultBundle,
        hiddenCue: String,
        traitBrief: String,
        currentMonth: Int
    ): String {
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        return """
            사용자의 프리미엄 운세 상담 JSON을 작성하세요.

            [입력]
            - 고민 분야: ${topic.label}
            - 고민 내용: $concernText
            - 기준 월: 올해 ${currentMonth}월
            - 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 달력 구분: ${if (displayInput.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별: ${displayInput.gender.label}
            - 운명수: ${numbers.destiny}
            - 기운: ${NumerologyCalculator.destinyPolarity(numbers.destiny).label}
            - 성향 요약: $traitBrief
            - 주의 키워드: ${destiny.cautionKeywords.take(2).joinToString(", ")}
            - 보조 힌트: $hiddenCue

            [작성 목표]
            사용자의 고민을 짧고 선명하게 상담하세요.
            성향 설명은 길게 반복하지 말고, 실제 장면과 행동 조언 중심으로 작성하세요.
            전체 글은 사람이 직접 상담해주는 듯한 자연스러운 존댓말로 쓰세요.

            [작성 규칙]
            - core 첫 문장에만 성향을 짧게 연결하고, 바로 고민의 핵심으로 들어가세요.
            - interpretation은 실제로 벌어질 수 있는 상황 2개를 중심으로 쓰세요.
            - caution은 방치했을 때 생길 손해를 구체적으로 쓰세요.
            - direction은 행동 지시가 아니라 지금 흐름을 읽는 포인트로 쓰세요.
            - bestMonthReason과 riskyMonthReason은 타이밍 조언만 담당하게 하세요.
            - 같은 의미를 다른 항목에서 반복하지 마세요.
            - 숫자 계산식과 내부 구조는 노출하지 마세요.
            - 사주, 타로, 점괘, 괘 같은 특정 전통 체계 이름은 쓰지 마세요.
            - 사용자를 "선생님"이라고 부르지 마세요.
            - 공포 조장은 피하되, 안일하게 넘기면 손해가 생길 수 있다는 현실적 경고는 넣으세요.
            - 각 항목은 2~4문장 안에서 끝내세요.
            - "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", "상대에게 보내기 좋은 말" 같은 복사용 문장 섹션은 만들지 마세요.
            - JSON 외의 설명은 출력하지 마세요.

            [월별 조언]
            현재 월 이후를 기준으로 추천 월 1개와 주의 월 1개를 고르세요.
            추천 월은 행동하면 흐름이 열리는 이유를, 주의 월은 조급함이나 무리한 선택으로 꼬일 수 있는 지점을 설명하세요.
            계산 방식은 절대 설명하지 마세요.

            [출력 형식]
            {
              "core": "",
              "interpretation": "",
              "caution": "",
              "direction": "",
              "oneLineAdvice": "",
              "bestMonth": "",
              "bestMonthReason": "",
              "riskyMonth": "",
              "riskyMonthReason": ""
            }
        """.trimIndent()
    }

    private fun buildRomanceSalonPromptV2(
        concernText: String,
        bundle: NumerologyResultBundle,
        traitBrief: String,
        cautionKeywords: String,
        createdYear: Int,
        currentMonth: Int,
        bestMonth: String,
        riskyMonth: String
    ): String {
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        return """
            한국어 연애 상담소형 결과 페이지를 JSON으로 작성하세요.

            [입력]
            - 고민 내용: $concernText
            - 상담 연도: $createdYear
            - 기준 월: 올해 ${currentMonth}월
            - 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 성별: ${displayInput.gender.label}
            - 운명수: ${numbers.destiny}
            - 성향 요약: $traitBrief
            - 주의 키워드: $cautionKeywords
            - 추천 흐름 월: $bestMonth
            - 조심할 흐름 월: $riskyMonth

            [작성 목표]
            긴 리포트가 아니라 모바일에서 읽기 쉬운 짧은 상담 페이지로 작성하세요.
            한 페이지에는 하나의 메시지만 담고, 각 body는 2~3문장으로 제한하세요.
            무료 결과의 성향 설명을 반복하지 말고, 연애 장면과 행동 조언 중심으로 쓰세요.

            [페이지 역할]
            - answer: 고민에 대한 한 문장 결론과 짧은 이유
            - timing: 언제 움직이면 좋은지
            - person: 어떤 사람이나 관계 흐름이 들어오기 쉬운지
            - caution: 관계를 꼬이게 만드는 습관
            - action: 행동 지시가 아니라 관계의 흐름을 읽는 포인트

            [문체]
            - 따뜻하지만 콕 짚는 존댓말
            - 과장된 불안 조장 금지
            - “좋다/나쁘다” 단정 금지
            - 연락, 만남, 거리감, 말투, 확인 욕구처럼 실제 연애 장면을 넣기
            - 같은 말을 반복하지 않기
            - 사주, 타로, 점괘, 괘 같은 단어 쓰지 않기
            - 복사하거나 공유하기 좋은 문장 섹션 만들지 않기
            - JSON만 반환하기

            [출력 형식]
            {
              "coverTitle": "${createdYear} 수리 연애 상담소",
              "coverSubtitle": "지금 마음의 흐름을 숫자로 읽어볼게요.",
              "answerCard": {
                "question": "",
                "shortAnswer": "",
                "body": ["", ""]
              },
              "toc": [
                { "id": "timing", "title": "인연이 열리는 시기" },
                { "id": "person", "title": "끌리는 사람의 결" },
                { "id": "caution", "title": "관계를 망치는 습관" },
                { "id": "action", "title": "읽는 포인트" }
              ],
              "pages": [
                {
                  "id": "timing",
                  "ribbon": "언제 움직일까",
                  "title": "인연이 열리는 시기",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "person",
                  "ribbon": "어떤 사람일까",
                  "title": "끌리는 사람의 결",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "caution",
                  "ribbon": "주의사항",
                  "title": "관계를 망치는 습관",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "action",
                  "ribbon": "흐름 정리",
                  "title": "읽는 포인트",
                  "highlight": "",
                  "body": ["", ""]
                }
              ],
              "closingAdvice": ""
            }
        """.trimIndent()
    }

    private fun buildTraitBrief(
        title: String,
        coreKeywords: List<String>,
        cautionKeywords: List<String>
    ): String {
        val core = coreKeywords.take(2).joinToString(", ").ifBlank { title }
        val caution = cautionKeywords.take(2).joinToString(", ").ifBlank { "조급함" }
        return "$title 기질은 $core 쪽이 강하고, 고민 상황에서는 ${caution}이 과해질 수 있습니다."
    }

    private fun buildHiddenBirthCue(input: BirthInput): String {
        val season = when (input.month) {
            3, 4, 5 -> "새로움과 확장의 기운이 강한 시기"
            6, 7, 8 -> "표현과 열기가 강해 빠르게 움직이기 쉬운 시기"
            9, 10, 11 -> "정리와 결실을 통해 방향을 고르는 시기"
            else -> "내면을 다지고 다음 흐름을 준비하는 시기"
        }
        val dayTone = when (input.day % 4) {
            0 -> "판단보다 감각이 먼저 반응하는 결"
            1 -> "시작과 결심이 중요한 결"
            2 -> "관계와 균형에서 답을 찾는 결"
            else -> "정리와 마무리에서 힘을 얻는 결"
        }
        return "$season, $dayTone"
    }

    private fun parseConsultation(rawContent: String, topic: PremiumTopic, bundle: NumerologyResultBundle): PremiumConsultation {
        val jsonText = rawContent
            .substringAfter("{", rawContent)
            .substringBeforeLast("}", rawContent)
            .let { "{$it}" }
        val json = JSONObject(jsonText)
        if (json.has("pages") || json.has("answerCard")) {
            val answerCard = parseAnswerCard(json.optJSONObject("answerCard"))
            val pages = sanitizePersonalPages(
                pages = normalizePersonalPages(
                    pages = parsePages(json.optJSONArray("pages")),
                    answerCard = answerCard,
                    topic = topic
                ),
                topic = topic
            )
            val toc = parseToc(json.optJSONArray("toc"))
            val cautionPage = pages.firstOrNull { it.id == "caution" }
            val actionPage = pages.firstOrNull { it.id == "action" }
            val timingPage = pages.firstOrNull { it.id == "timing" }
            val personPage = pages.firstOrNull { it.id == "person" }
            val closingAdvice = json.optString("closingAdvice").withoutTaskAdvice("")
            val readingPoint = readingPointFallback(topic)
            val parsed = PremiumConsultation(
                core = answerCard.shortAnswer.ifBlank { answerCard.body.firstOrNull().orEmpty() },
                interpretation = listOf(timingPage, personPage)
                    .filterNotNull()
                    .flatMap { it.body }
                    .joinToString("\n\n"),
                caution = cautionPage?.body?.joinToString("\n\n").orEmpty(),
                direction = actionPage?.body?.joinToString("\n\n").orEmpty().withoutTaskAdvice(readingPoint),
                oneLineAdvice = closingAdvice.ifBlank { actionPage?.highlight.orEmpty() }.withoutTaskAdvice(""),
                coverTitle = json.optString("coverTitle"),
                coverSubtitle = json.optString("coverSubtitle"),
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
            direction = json.optString("direction").withoutTaskAdvice(readingPointFallback(topic)),
            oneLineAdvice = json.optString("oneLineAdvice").withoutTaskAdvice(""),
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
        val topicLabel = topic.label
        return listOf(
            byId["timing"] ?: ConsultationPage(
                id = "timing",
                ribbon = "언제 움직일까",
                title = "흐름이 열리는 시기",
                highlight = answerCard.shortAnswer.ifBlank { "$topicLabel 고민은 속도보다 타이밍을 맞추는 쪽이 중요합니다." },
                body = listOf(
                    "지금은 결론을 재촉하기보다 움직일 수 있는 구간을 차분히 고르는 편이 좋습니다.",
                    "추천 월에는 작게라도 행동을 만들고, 주의 월에는 충동적인 선택을 하루 늦추는 쪽이 안전합니다."
                )
            ),
            byId["person"] ?: ConsultationPage(
                id = "person",
                ribbon = "어떤 결일까",
                title = "끌리는 흐름의 모양",
                highlight = "$topicLabel 안에서 반복되는 선택의 결을 먼저 봐야 합니다.",
                body = listOf(
                    "당장 좋아 보이는 조건보다 실제로 마음이 편해지는 장면을 기준으로 보는 편이 좋습니다.",
                    "겉으로 드러난 말보다 반복되는 행동, 약속을 지키는 방식, 부담을 다루는 태도를 살펴보세요."
                )
            ),
            byId["caution"] ?: ConsultationPage(
                id = "caution",
                ribbon = "주의사항",
                title = "흐름을 망치는 습관",
                highlight = "혼자 결론을 내리고 급하게 움직이면 좋은 흐름도 쉽게 꼬일 수 있습니다.",
                body = listOf(
                    "확인하고 싶은 마음이 커질수록 말투가 강해지거나 선택이 급해질 수 있습니다.",
                    "이때 바로 밀어붙이면 상대나 상황이 닫힐 수 있으니, 하루 정도 여백을 두고 다시 보는 편이 좋습니다."
                )
            ),
            byId["action"] ?: ConsultationPage(
                id = "action",
                ribbon = "흐름 정리",
                title = "읽는 포인트",
                highlight = "지금은 행동보다 흐름의 결을 읽는 것이 더 중요합니다.",
                body = listOf(
                    "지금의 흐름은 결론보다 반복되는 분위기를 차분히 보는 쪽에 가깝습니다.",
                    "마음이 급해질수록 선택의 폭이 좁아질 수 있으니, 속도보다 온도를 읽는 관점이 중요합니다."
                )
            )        )
    }

    private fun sanitizePersonalPages(
        pages: List<ConsultationPage>,
        topic: PremiumTopic
    ): List<ConsultationPage> {
        val fallback = readingPointFallback(topic)
        return pages.map { page ->
            if (page.id != "action") {
                page
            } else {
                page.copy(
                    ribbon = "흐름 정리",
                    title = "읽는 포인트",
                    highlight = page.highlight.withoutTaskAdvice(fallback),
                    body = page.body.withoutTaskAdvice(fallback)
                )
            }
        }
    }

    private fun List<String>.withoutTaskAdvice(fallback: String): List<String> {
        val filtered = map { it.withoutTaskAdvice("") }.filter { it.isNotBlank() }
        return filtered.ifEmpty { listOf(fallback) }
    }

    private fun String.withoutTaskAdvice(fallback: String): String {
        val cleaned = trim()
        if (cleaned.isBlank()) return fallback
        return if (cleaned.isTaskLikeAdvice()) fallback else cleaned
    }

    private fun String.isTaskLikeAdvice(): Boolean {
        val blocked = listOf(
            "수첩", "메모", "기록", "적어", "체크", "체크리스트",
            "오늘은", "오늘 할", "오늘 해야", "이번 주", "한 달 안",
            "해야", "해보", "정하세요", "만드세요", "나누세요", "실행"
        )
        return blocked.any { contains(it) }
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
        val bestReason = withBestMonthTimingNote(rawBestReason, bestSelection)
        val rawRiskyReason = consultation.riskyMonthReason.takeIf {
            consultation.riskyMonth == expectedRiskyMonth && it.isNotBlank()
        } ?: buildRiskyMonthReason(topic, expectedRiskyMonth, riskySelection)
        val riskyReason = withRiskyMonthTimingNote(rawRiskyReason, riskySelection)

        return consultation.copy(
            bestMonth = expectedBestMonth,
            bestMonthReason = bestReason,
            riskyMonth = expectedRiskyMonth,
            riskyMonthReason = riskyReason
        )
    }

    private fun withBestMonthTimingNote(reason: String, selection: PremiumMonthPlanner.MonthSelection): String {
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

    private fun withRiskyMonthTimingNote(reason: String, selection: PremiumMonthPlanner.MonthSelection): String {
        val passedTopMonth = selection.replacedPastMonth ?: return reason
        val alreadyExplained = reason.contains("${passedTopMonth}월은 이미 지났")
        if (alreadyExplained) return reason
        return if (selection.isNextYear) {
            "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났고, 올해 남은 구간에는 같은 결의 주의 달이 약하게 지나갑니다. 그래서 다음 해에 가장 먼저 돌아오는 ${selection.month}월을 다음 주의 구간으로 봅니다. $reason"
        } else {
            "올해 가장 강하게 조심할 달인 ${passedTopMonth}월은 이미 지났으니, 지금 이후에는 ${selection.month}월을 다음 주의 구간으로 보세요. $reason"
        }
    }

    private fun buildBestMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
        return when (topic) {
            PremiumTopic.ROMANCE ->
                "${monthText}에는 마음을 새롭게 열기 좋은 흐름이 강합니다. ${bundle.content.destinyProfile.title}의 결을 살려 가볍고 진심 어린 대화부터 시작하면 관계의 문이 부드럽게 열립니다."
            PremiumTopic.CAREER ->
                "${monthText}에는 방향을 정리하고 실제 행동으로 옮기기 좋은 기운이 모입니다. 준비해둔 포트폴리오, 지원, 제안처럼 손에 잡히는 움직임을 만들기 좋습니다."
            PremiumTopic.MONEY ->
                "${monthText}에는 돈의 흐름을 구조화하기 좋습니다. 큰 욕심보다 수입과 지출의 길을 또렷하게 나누면 기회가 안정적으로 이어집니다."
            PremiumTopic.SELF_ESTEEM ->
                "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 남의 반응보다 작은 약속을 지키는 경험을 쌓을수록 마음의 중심이 단단해집니다."
            PremiumTopic.RELATIONSHIP ->
                "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋은 달입니다."
        }
    }

    private fun buildRiskyMonthReason(
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

    companion object {
        private const val SYSTEM_PROMPT =
            "Write concise Korean counseling JSON. Be concrete and polite. No system-name terms, no '선생님'. JSON only."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}


