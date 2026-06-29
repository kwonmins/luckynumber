package com.example.unum.domain.usecase

import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.ConsultationTocItem
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.domain.service.OpenAiChatClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GenerateCompatibilityConsultationUseCase(
    private val chatClient: OpenAiChatClient = OpenAiChatClient()
) {
    suspend operator fun invoke(
        apiKey: String,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipStatus: CompatibilityRelationshipStatus
    ): CompatibilityConsultation = withContext(Dispatchers.IO) {
        val relationshipNumber = relationshipNumber(maleBundle, femaleBundle)
        val prompt = buildPrompt(
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
            relationshipStatus = relationshipStatus,
            relationshipNumber = relationshipNumber
        )
        val content = chatClient.requestJsonContent(
            apiKey = apiKey,
            model = OPENAI_MODEL,
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = prompt,
            failureLabel = "궁합노트"
        )
        parseConsultation(content, maleBundle, femaleBundle, concern, relationshipNumber)
    }

    private fun buildPrompt(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipStatus: CompatibilityRelationshipStatus,
        relationshipNumber: Int
    ): String {
        val concernText = concern.ifBlank { "두 사람의 관계가 잘 이어질 수 있을지 궁금합니다." }
        val createdYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val relationshipNumbers = relationshipNumbers(maleBundle, femaleBundle, relationshipNumber)
        val bestMonth = PremiumMonthPlanner.pickBestMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth).toDisplayText()
        val riskyMonth = PremiumMonthPlanner.pickRiskyMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth).toDisplayText()
        val maleTrait = traitBrief(maleBundle)
        val femaleTrait = traitBrief(femaleBundle)
        val cautionKeywords = listOf(
            maleBundle.content.destinyProfile.cautionKeywords.take(2),
            femaleBundle.content.destinyProfile.cautionKeywords.take(2)
        ).flatten().distinct().joinToString(", ").ifBlank { "속도 차이, 감정 확인, 거리 조절" }

        return buildCompactCompatibilityPrompt(
            concernText = concernText,
            createdYear = createdYear,
            currentMonth = currentMonth,
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            relationshipNumber = relationshipNumber,
            relationshipMeaning = relationshipMeaning(relationshipNumber),
            maleTrait = maleTrait,
            femaleTrait = femaleTrait,
            cautionKeywords = cautionKeywords,
            relationshipStatus = relationshipStatus,
            bestMonth = bestMonth,
            riskyMonth = riskyMonth
        )

    }

    private fun buildCompactCompatibilityPrompt(
        concernText: String,
        createdYear: Int,
        currentMonth: Int,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int,
        relationshipMeaning: String,
        maleTrait: String,
        femaleTrait: String,
        cautionKeywords: String,
        relationshipStatus: CompatibilityRelationshipStatus,
        bestMonth: String,
        riskyMonth: String
    ): String {
        val maleInput = maleBundle.displayInput
        val femaleInput = femaleBundle.displayInput
        val statusRule = when (relationshipStatus) {
            CompatibilityRelationshipStatus.COUPLE ->
                "They are already a couple. Focus on warmth, friction repair, communication tone, and emotional rhythm."
            CompatibilityRelationshipStatus.CRUSH ->
                "This is a crush/unrequited-love reading. Focus on attraction signals, safe distance, how to approach, when to express feelings, and how to handle rejection without pressuring the other person."
            CompatibilityRelationshipStatus.REUNION ->
                "This is a reunion/getting-back-together reading. Focus on why the connection broke, whether contact is safe and welcome, timing for a light message, apology/closure boundaries, and how to avoid repeating the same pattern. Do not promise reconciliation."
        }
        return """
            Write Korean compatibility counseling page JSON.
            Input: status="${relationshipStatus.label}"; concern="$concernText"; date=${createdYear}.${currentMonth}; relationshipNumber=$relationshipNumber; relationshipCue="$relationshipMeaning"; caution="$cautionKeywords"; bestMonth="$bestMonth"; riskyMonth="$riskyMonth".
            Male: birth=${maleInput.year}.${maleInput.month}.${maleInput.day}; calendar=${calendarTypeLabel(maleInput.calendarType)}; destiny=${maleBundle.numbers.destiny}; traits="$maleTrait".
            Female: birth=${femaleInput.year}.${femaleInput.month}.${femaleInput.day}; calendar=${calendarTypeLabel(femaleInput.calendarType)}; destiny=${femaleBundle.numbers.destiny}; traits="$femaleTrait".
            Relationship status rule: $statusRule
            Style: polite Korean, concrete relationship scenes, no long individual trait recap, no good/bad verdict, no system-name terms, no "선생님", no code block.
            Rules: one message per page; each highlight is one clear sentence; each body has 2 short paragraphs; avoid repeated phrasing. Use the given month values and do not explain calculations.
            Do not write task lists or "what to do today" advice. Avoid notebook, memo, journaling, recording, checklist, routine-building, "write it down", "try this today", "this week", or "within a month" instructions.
            Do not create copy-ready/share-ready sections. Avoid labels or phrases like "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", or "상대에게 보내기 좋은 말".
            Return only valid JSON:
            {"coverTitle":"","coverSubtitle":"","bestMonth":"$bestMonth","bestMonthReason":"","riskyMonth":"$riskyMonth","riskyMonthReason":"","answerCard":{"question":"","shortAnswer":"","body":["",""]},"toc":[{"id":"attraction","title":""},{"id":"friction","title":""},{"id":"view","title":""},{"id":"action","title":""}],"pages":[{"id":"attraction","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"friction","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"view","ribbon":"","title":"","highlight":"","body":["",""]},{"id":"action","ribbon":"","title":"","highlight":"","body":["",""]}],"closingAdvice":""}
        """.trimIndent()
    }

    private fun buildCompatibilitySalonPromptV2(
        concernText: String,
        createdYear: Int,
        currentMonth: Int,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int,
        relationshipMeaning: String,
        maleTrait: String,
        femaleTrait: String,
        cautionKeywords: String,
        bestMonth: String,
        riskyMonth: String
    ): String {
        val maleInput = maleBundle.displayInput
        val femaleInput = femaleBundle.displayInput
        return """
            두 사람의 궁합 상담 JSON을 작성하세요.

            [입력]
            - 상담 종류: 궁합
            - 고민 내용: $concernText
            - 상담 기준: ${createdYear}년 ${currentMonth}월
            - 남자 생년월일: ${maleInput.year}.${maleInput.month}.${maleInput.day}
            - 남자 달력 구분: ${calendarTypeLabel(maleInput.calendarType)}
            - 남자 운명수: ${maleBundle.numbers.destiny}
            - 남자 성향 요약: $maleTrait
            - 여자 생년월일: ${femaleInput.year}.${femaleInput.month}.${femaleInput.day}
            - 여자 달력 구분: ${calendarTypeLabel(femaleInput.calendarType)}
            - 여자 운명수: ${femaleBundle.numbers.destiny}
            - 여자 성향 요약: $femaleTrait
            - 관계수: $relationshipNumber
            - 관계 흐름 참고: $relationshipMeaning
            - 주의 키워드: $cautionKeywords
            - 추천 흐름 월: $bestMonth
            - 조심할 흐름 월: $riskyMonth

            [작성 목표]
            두 사람의 관계를 짧고 선명하게 상담하세요.
            개인 성향을 길게 설명하지 말고, 두 사람이 만났을 때 생기는 끌림, 엇갈림, 조율법을 중심으로 작성하세요.
            전체 글은 사람이 직접 상담해주는 듯한 자연스러운 존댓말로 쓰세요.

            [핵심 작성 규칙]
            - 관계수는 점수처럼 쓰지 말고, 두 사람 사이의 흐름으로만 해석하세요.
            - 남자 성향, 여자 성향을 따로 길게 설명하지 마세요.
            - 두 사람이 함께 있을 때 나타나는 장면으로 보여주세요.
            - 같은 의미를 여러 페이지에서 반복하지 마세요.
            - 각 페이지는 하나의 메시지만 담으세요.
            - 각 body는 2문장으로 제한하세요.
            - highlight는 한 문장으로 짧고 선명하게 쓰세요.
            - 계산식과 내부 숫자 구조는 절대 쓰지 마세요.
            - 사주, 타로, 점괘, 괘 같은 단어는 쓰지 마세요.
            - 좋다/나쁘다로 단정하지 말고 “이런 흐름이 강하다”, “이 부분은 조율이 필요하다”처럼 표현하세요.
            - "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", "상대에게 보내기 좋은 말" 같은 복사용 문장 섹션은 만들지 마세요.

            [페이지별 역할]
            1. answer
            - 사용자의 고민을 질문형으로 자연스럽게 바꾸세요.
            - shortAnswer는 두 사람의 관계를 한 문장으로 진단하세요.
            - body는 전체 분위기를 2문단으로 설명하세요.
            - 개인 성향 소개가 아니라 관계의 현재 흐름으로 시작하세요.

            2. attraction
            - 두 사람이 왜 끌리는지 설명하세요.
            - 말투, 반응 속도, 안정감, 자극, 생활 리듬 중 2가지를 골라 구체적으로 쓰세요.
            - 막연히 “잘 맞는다”라고 하지 말고, 어떤 순간에 끌림이 생기는지 보여주세요.

            3. friction
            - 반복될 수 있는 충돌 방식을 설명하세요.
            - 실제 다툼 장면, 오해 방식, 감정 확인 속도 차이를 중심으로 쓰세요.
            - 누가 잘못했다는 식으로 몰아가지 말고, 서로 다른 반응 방식 때문에 생기는 문제로 풀어주세요.

            4. view
            - 상대가 나를 어떻게 느낄 수 있는지 설명하세요.
            - 매력으로 느끼는 점과 부담으로 느끼는 점을 균형 있게 쓰세요.
            - “상대는 당신을 이렇게 볼 수 있습니다”라는 관점으로 작성하세요.

            5. action
            - 관계를 오래 유지하기 위한 행동을 제안하세요.
            - 대화법, 거리 조절, 다툰 뒤 회복법을 각각 1개씩 제안하세요.
            - 바로 실천할 수 있는 말투나 행동으로 작성하세요.

            [월별 조언]
            - bestMonthReason은 두 사람이 가까워지기 좋은 행동 타이밍만 설명하세요.
            - riskyMonthReason은 그 달에 조심해야 할 관계 습관만 설명하세요.
            - 이미 지난 달에 대한 계산 과정은 설명하지 마세요.
            - 추천 월과 주의 월은 입력값을 그대로 사용하세요.

            [문체]
            - 짧고 자연스럽게 쓰세요.
            - "~할 수 있습니다"를 반복하지 마세요.
            - “예를 들어”, “특히”, “이럴 때”를 자연스럽게 섞되 과하게 반복하지 마세요.
            - 연락, 약속, 답장 속도, 서운함, 거리감, 말투처럼 실제 연애 장면이 보이는 단어를 사용하세요.
            - 마지막은 관계를 지키는 현실적인 행동 조언으로 마무리하세요.

            [출력 형식]
            반드시 JSON만 반환하세요. 코드블록이나 설명 문장은 붙이지 마세요.

            {
              "coverTitle": "수리 궁합 상담소",
              "coverSubtitle": "두 사람 사이의 흐름을 읽어볼게요.",
              "bestMonth": "$bestMonth",
              "bestMonthReason": "",
              "riskyMonth": "$riskyMonth",
              "riskyMonthReason": "",
              "answerCard": {
                "question": "",
                "shortAnswer": "",
                "body": ["", ""]
              },
              "toc": [
                { "id": "attraction", "title": "맞닿는 지점" },
                { "id": "friction", "title": "엇갈리는 방식" },
                { "id": "view", "title": "상대의 눈에 비친 모습" },
                { "id": "action", "title": "관계를 살리는 습관" }
              ],
              "pages": [
                {
                  "id": "attraction",
                  "ribbon": "서로 끌리는 이유",
                  "title": "맞닿는 지점",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "friction",
                  "ribbon": "주의사항",
                  "title": "엇갈리는 방식",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "view",
                  "ribbon": "상대가 보는 나",
                  "title": "상대의 눈에 비친 모습",
                  "highlight": "",
                  "body": ["", ""]
                },
                {
                  "id": "action",
                  "ribbon": "오래 가려면",
                  "title": "관계를 살리는 습관",
                  "highlight": "",
                  "body": ["", ""]
                }
              ],
              "closingAdvice": ""
            }
        """.trimIndent()
    }

    private fun parseConsultation(
        rawContent: String,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipNumber: Int
    ): CompatibilityConsultation {
        val jsonText = rawContent
            .substringAfter("{", rawContent)
            .substringBeforeLast("}", rawContent)
            .let { "{$it}" }
        val json = JSONObject(jsonText)
        val answerCard = parseAnswerCard(json.optJSONObject("answerCard"))
        val pages = sanitizeCompatibilityPages(
            normalizeCompatibilityPages(
                pages = parsePages(json.optJSONArray("pages")),
                answerCard = answerCard,
                relationshipNumber = relationshipNumber
            )
        )
        val toc = parseToc(json.optJSONArray("toc"))
        val attraction = pages.firstOrNull { it.id == "attraction" }
        val friction = pages.firstOrNull { it.id == "friction" }
        val view = pages.firstOrNull { it.id == "view" }
        val action = pages.firstOrNull { it.id == "action" }
        val closingAdvice = json.optString("closingAdvice").withoutTaskAdvice("")
        val fallbackTone = relationshipToneFallback()

        val parsed = CompatibilityConsultation(
            maleEnergy = view?.body?.joinToString("\n\n").orEmpty(),
            femaleEnergy = view?.highlight.orEmpty(),
            relationshipFlow = answerCard.body.joinToString("\n\n"),
            strengths = attraction?.body?.joinToString("\n\n").orEmpty(),
            friction = friction?.body?.joinToString("\n\n").orEmpty(),
            homeTone = action?.body?.joinToString("\n\n").orEmpty().withoutTaskAdvice(fallbackTone),
            longTermTip = action?.highlight.orEmpty().withoutTaskAdvice(fallbackTone),
            oneLineSummary = answerCard.shortAnswer.ifBlank { closingAdvice },
            bestMonth = json.optString("bestMonth"),
            bestMonthReason = json.optString("bestMonthReason"),
            riskyMonth = json.optString("riskyMonth"),
            riskyMonthReason = json.optString("riskyMonthReason"),
            coverTitle = json.optString("coverTitle"),
            coverSubtitle = json.optString("coverSubtitle"),
            answerCard = answerCard,
            toc = toc,
            pages = pages,
            closingAdvice = closingAdvice
        )
        return normalizeConsultation(parsed, maleBundle, femaleBundle, concern, relationshipNumber)
    }

    private fun normalizeConsultation(
        consultation: CompatibilityConsultation,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipNumber: Int
    ): CompatibilityConsultation {
        val concernText = concern.takeIf { it.isNotBlank() } ?: "두 사람의 관계"
        val fallbackSummary = "두 사람은 ${relationshipMeaning(relationshipNumber)} 다만 $concernText 안에서는 속도와 표현 방식을 맞추지 않으면 작은 오해가 오래 갈 수 있습니다."
        val relationshipNumbers = relationshipNumbers(maleBundle, femaleBundle, relationshipNumber)
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bestSelection = PremiumMonthPlanner.pickBestMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth)
        val riskySelection = PremiumMonthPlanner.pickRiskyMonth(PremiumTopic.ROMANCE, relationshipNumbers, currentMonth)
        val bestMonth = bestSelection.toDisplayText()
        val riskyMonth = riskySelection.toDisplayText()
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
            bestMonthReason = consultation.bestMonthReason.takeIf { consultation.bestMonth == bestMonth && it.isNotBlank() }
                ?: buildCompatibilityBestMonthReason(bestMonth, bestSelection),
            riskyMonth = riskyMonth,
            riskyMonthReason = consultation.riskyMonthReason.takeIf { consultation.riskyMonth == riskyMonth && it.isNotBlank() }
                ?: buildCompatibilityRiskyMonthReason(riskyMonth, riskySelection),
            coverTitle = consultation.coverTitle.ifBlank { "수리 궁합 상담소" },
            coverSubtitle = consultation.coverSubtitle.ifBlank { "두 사람 사이의 흐름을 읽어볼게요." }
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
        relationshipNumber: Int
    ): List<ConsultationPage> {
        val byId = pages.associateBy { it.id }
        return listOf(
            byId["attraction"] ?: ConsultationPage(
                id = "attraction",
                ribbon = "서로 끌리는 이유",
                title = "맞닿는 지점",
                highlight = answerCard.shortAnswer.ifBlank { "두 사람은 서로 다른 속도 안에서 필요한 균형을 줄 수 있습니다." },
                body = listOf(
                    "관계수 $relationshipNumber 흐름은 두 사람이 같은 방식으로 움직인다기보다, 서로의 빈틈을 건드리며 가까워지는 결에 가깝습니다.",
                    "말투, 반응 속도, 생활 리듬이 완전히 같지 않아도 상대에게서 낯선 안정감이나 자극을 느낄 수 있습니다."
                )
            ),
            byId["friction"] ?: ConsultationPage(
                id = "friction",
                ribbon = "주의사항",
                title = "엇갈리는 방식",
                highlight = "감정 확인 속도가 어긋나면 작은 말도 크게 번질 수 있습니다.",
                body = listOf(
                    "한쪽은 바로 확인하고 싶고, 다른 한쪽은 시간을 두고 정리하고 싶어질 수 있습니다.",
                    "이 차이를 성의 부족으로 단정하면 관계가 차갑게 굳을 수 있으니, 대화 전 숨을 고르는 시간이 필요합니다."
                )
            ),
            byId["view"] ?: ConsultationPage(
                id = "view",
                ribbon = "상대가 보는 나",
                title = "상대의 눈에 비친 모습",
                highlight = "상대는 당신에게 끌리면서도 때로는 속도나 표현의 압박을 느낄 수 있습니다.",
                body = listOf(
                    "당신의 진심은 장점이지만, 확인이 잦아지면 상대에게는 부담으로 읽힐 수 있습니다.",
                    "좋아하는 마음을 증명하려 하기보다 편안한 반복을 보여주는 쪽이 더 오래 남습니다."
                )
            ),
            byId["action"] ?: ConsultationPage(
                id = "action",
                ribbon = "오래 가려면",
                title = "관계를 살리는 습관",
                highlight = "좋은 관계는 맞는 사람을 찾는 것보다 맞춰가는 방식을 잃지 않는 데서 오래 갑니다.",
                body = listOf(
                    "중요한 말은 문자보다 직접 대화로 짧게 확인하세요.",
                    "서운함이 생기면 바로 결론 내리지 말고 감정과 요청을 나눠 말하세요.",
                    "다툰 뒤에는 누가 맞았는지보다 어떻게 회복할지부터 정하세요."
                )
            )
        )
    }

    private fun sanitizeCompatibilityPages(pages: List<ConsultationPage>): List<ConsultationPage> {
        val fallback = relationshipToneFallback()
        return pages.map { page ->
            if (page.id != "action") {
                page
            } else {
                page.copy(
                    ribbon = "관계 흐름",
                    title = "오래 가는 결",
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

    private fun traitBrief(bundle: NumerologyResultBundle): String {
        val profile = bundle.content.destinyProfile
        val core = profile.coreKeywords.take(2).joinToString(", ").ifBlank { profile.title }
        val caution = profile.cautionKeywords.take(2).joinToString(", ").ifBlank { "조급함" }
        return "${profile.title} 기질은 $core 쪽이 강하고, 관계에서는 ${caution}이 과해질 수 있습니다."
    }

    private fun relationshipNumber(maleBundle: NumerologyResultBundle, femaleBundle: NumerologyResultBundle): Int {
        return (maleBundle.numbers.destiny + femaleBundle.numbers.destiny) % 10
    }

    private fun relationshipNumbers(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int
    ): NumerologyNumbers {
        return NumerologyNumbers(
            destiny = relationshipNumber,
            early = (maleBundle.numbers.early + femaleBundle.numbers.early).floorMod(10),
            middle = (maleBundle.numbers.middle + femaleBundle.numbers.middle).floorMod(10),
            late = (maleBundle.numbers.late + femaleBundle.numbers.late).floorMod(10),
            code = "${maleBundle.numbers.code}${femaleBundle.numbers.code}"
        )
    }

    private fun buildCompatibilityBestMonthReason(
        monthText: String,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val base = "${monthText}에는 두 사람의 대화와 만남 리듬을 새로 맞추기 좋은 흐름이 강합니다. 고백, 관계 정리, 만남 약속처럼 마음을 실제 행동으로 옮기면 서로의 온도를 확인하기 좋습니다."
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 그래서 다음 해 ${selection.month}월을 다음 추천 구간으로 보세요. $base"
        } else {
            "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 추천 구간으로 보세요. $base"
        }
    }

    private fun buildCompatibilityRiskyMonthReason(
        monthText: String,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val base = "${monthText}에는 감정 확인 욕구와 서운함이 커지기 쉽습니다. 이때 상대를 몰아붙이거나 혼자 결론을 내리면 관계가 생각보다 깊게 틀어질 수 있으니, 중요한 말은 시간을 두고 나누는 편이 안전합니다."
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났고, 다음 해 ${selection.month}월에 비슷한 주의 흐름이 먼저 돌아옵니다. $base"
        } else {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났으니, 지금 이후에는 ${monthText}을 다음 주의 구간으로 보세요. $base"
        }
    }

    private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor

    private fun calendarTypeLabel(type: CalendarType): String {
        return if (type == CalendarType.LUNAR) "음력" else "양력"
    }

    private fun relationshipMeaning(number: Int): String = when (number) {
        0 -> "관계의 모양이 쉽게 고정되지 않아, 서로에게 자유와 여백을 주어야 살아나는 흐름입니다."
        1 -> "시작과 주도권의 기운이 강해 빠르게 가까워질 수 있지만, 한쪽의 속도가 너무 앞서면 균형이 흔들리는 흐름입니다."
        2 -> "배려와 조율의 기운이 강해 마음을 천천히 맞추기 좋지만, 서운함을 숨기면 오해가 쌓이는 흐름입니다."
        3 -> "대화와 표현의 기운이 강해 즐거움이 살아나지만, 말이 앞서면 감정이 쉽게 번지는 흐름입니다."
        4 -> "생활 리듬과 약속을 맞추기 좋은 흐름이지만, 답답함과 규칙 싸움이 생기기 쉬운 흐름입니다."
        5 -> "변화와 자극이 강해 끌림이 빠르게 생기지만, 안정감이 부족하면 쉽게 흔들리는 흐름입니다."
        6 -> "책임과 돌봄의 기운이 강해 오래 갈 기반이 있으나, 부담이 사랑을 눌러버리지 않게 조심해야 하는 흐름입니다."
        7 -> "깊이와 집중이 강해 함께 몰입하기 좋지만, 침묵과 거리감이 오해로 번지기 쉬운 흐름입니다."
        8 -> "현실 감각과 추진력이 살아나는 흐름이지만, 관계가 성과나 역할로만 굳지 않게 조심해야 합니다."
        else -> "마무리와 정리의 기운이 강해 깊은 결론에 닿기 쉽지만, 감정의 무게가 커질 수 있는 흐름입니다."
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "Write concise Korean compatibility counseling JSON. Be concrete and polite. No system-name terms, no '선생님'. JSON only."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}
