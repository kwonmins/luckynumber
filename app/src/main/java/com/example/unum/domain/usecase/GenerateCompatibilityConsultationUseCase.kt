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
        parseConsultation(
            rawContent = content,
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
            relationshipNumber = relationshipNumber,
            relationshipStatus = relationshipStatus
        )
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
        val narrativePlan = compatibilityNarrativePlan(relationshipStatus, relationshipNumber, concernText)
        val sectionGuide = narrativePlan.sections.mapIndexed { index, section ->
            "${index + 1}. id=${section.id}, ribbon=${section.ribbon}, title=${section.title}: ${section.focus}"
        }.joinToString("\n")
        val tocSchema = narrativePlan.sections.joinToString(prefix = "[", postfix = "]") { section ->
            "{\"id\":\"${section.id}\",\"title\":\"${section.title}\"}"
        }
        val pagesSchema = narrativePlan.sections.joinToString(prefix = "[", postfix = "]") { section ->
            "{\"id\":\"${section.id}\",\"ribbon\":\"${section.ribbon}\",\"title\":\"${section.title}\",\"highlight\":\"\",\"body\":[\"\"]}"
        }
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
            Narrative angle: ${narrativePlan.angle}
            Style: polite Korean, concrete relationship scenes, no long individual trait recap, no good/bad verdict, no system-name terms, no "선생님", no code block.
            Rules: keep all counseling text within 2,500 Korean characters total. Use one message per page, one clear highlight, and one 160-220 character body paragraph with 2-3 sentences. Keep the answer card and each month reason under 120 characters and closingAdvice under 40 characters. Avoid repeated phrasing. Use the given month values and do not explain calculations.
            Do not fall back to the generic order "끌림-충돌-상대 시선-행동". Follow this relationship-specific section blueprint exactly:
            $sectionGuide
            Make the opening scene, vocabulary, and conclusion different for every section. Do not begin two sections with the same grammatical pattern.
            Give practical relationship advice only in the recommendation section. Avoid notebook, memo, journaling, recording, checklist, routine-building, or "write it down" instructions.
            Do not create copy-ready/share-ready sections. Avoid labels or phrases like "복사하면 좋은 문장", "기억할 문장", "공유하기 좋은 문장", or "상대에게 보내기 좋은 말".
            Return only valid JSON:
            {"coverTitle":"${narrativePlan.coverTitle}","coverSubtitle":"${narrativePlan.coverSubtitle}","bestMonth":"$bestMonth","bestMonthReason":"","riskyMonth":"$riskyMonth","riskyMonthReason":"","answerCard":{"question":"","shortAnswer":"","body":["",""]},"toc":$tocSchema,"pages":$pagesSchema,"closingAdvice":""}
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

    private fun compatibilityNarrativePlan(
        relationshipStatus: CompatibilityRelationshipStatus,
        relationshipNumber: Int,
        concern: String
    ): CompatibilityNarrativePlan {
        val concernSeed = concern.trim()
            .ifBlank { "두 사람의 관계가 잘 이어질 수 있을지 궁금합니다." }
            .lowercase()
            .hashCode()
            .floorMod(3)
        val variant = (relationshipNumber + concernSeed).floorMod(3)
        val angle = when (relationshipNumber) {
            0, 5 -> "자유와 안정 사이에서 두 사람이 허용할 수 있는 거리"
            1, 4, 8 -> "주도권, 약속, 생활 속 책임이 관계의 온도에 미치는 영향"
            2, 6 -> "배려가 편안함이 되는 순간과 부담으로 바뀌는 순간의 차이"
            3, 7 -> "말과 침묵, 답장 속도처럼 표현 방식이 만드는 친밀감"
            else -> "지나간 감정의 정리와 다음 관계 단계로 넘어가는 속도"
        }
        val statusSections = when (relationshipStatus) {
            CompatibilityRelationshipStatus.COUPLE -> when (variant) {
                0 -> listOf(
                    CompatibilitySectionSpec("comfort", "함께 있을 때", "편안함이 생기는 순간", "둘만의 일상에서 애정이 자연스럽게 드러나는 장면"),
                    CompatibilitySectionSpec("rhythm", "생활의 온도차", "서로 다른 리듬", "연락, 약속, 혼자 있는 시간에서 생기는 속도 차이"),
                    CompatibilitySectionSpec("repair", "서운함 이후", "다시 가까워지는 방식", "다툰 뒤 두 사람이 회복을 받아들이는 방식"),
                    CompatibilitySectionSpec("future", "관계의 다음 장", "함께 갈 수 있는 방향", "현재 관계가 오래 가기 위해 필요한 정서적 조건")
                )
                1 -> listOf(
                    CompatibilitySectionSpec("affection", "사랑의 표현", "마음을 확인하는 방식", "각자가 사랑받는다고 느끼는 구체적인 반응"),
                    CompatibilitySectionSpec("balance", "주도권의 균형", "한쪽이 앞서갈 때", "결정과 책임이 한 사람에게 몰리는 장면"),
                    CompatibilitySectionSpec("language", "말이 닿는 거리", "오해가 풀리는 말투", "같은 말이 다르게 들리는 이유와 감정의 번역"),
                    CompatibilitySectionSpec("continuity", "오래 남는 힘", "관계를 지키는 기반", "설렘 이후에도 관계를 지탱하는 현실적인 강점")
                )
                else -> listOf(
                    CompatibilitySectionSpec("spark", "요즘의 두 사람", "다시 설레는 지점", "익숙함 속에서도 관계가 살아나는 순간"),
                    CompatibilitySectionSpec("distance", "가까움과 여백", "거리감이 생기는 순간", "함께 있고 싶은 마음과 혼자 있고 싶은 마음의 차이"),
                    CompatibilitySectionSpec("trust", "믿음의 모양", "안심이 필요한 부분", "확인, 약속, 반복되는 행동이 신뢰에 미치는 영향"),
                    CompatibilitySectionSpec("chapter", "다음 계절", "관계가 향하는 곳", "두 사람이 지금 함께 선택하고 있는 관계의 방향")
                )
            }
            CompatibilityRelationshipStatus.CRUSH -> when (variant) {
                0 -> listOf(
                    CompatibilitySectionSpec("signal", "호감의 단서", "마음이 보이는 순간", "말투와 반응에서 드러나는 실제 호감 신호"),
                    CompatibilitySectionSpec("uncertainty", "헷갈리는 이유", "기대와 현실의 간격", "친절과 호감을 혼동하기 쉬운 장면"),
                    CompatibilitySectionSpec("distance", "지금의 거리", "다가가도 되는 범위", "상대가 편안하게 받아들일 접근 속도"),
                    CompatibilitySectionSpec("turning", "마음의 분기점", "관계가 달라지는 조건", "고백보다 먼저 확인해야 할 상호성")
                )
                1 -> listOf(
                    CompatibilitySectionSpec("attraction", "처음 끌린 이유", "시선이 머무는 지점", "두 사람 사이에서 호감이 자라기 쉬운 장면"),
                    CompatibilitySectionSpec("response", "답장의 온도", "반응 속도가 말해주는 것", "연락 빈도와 대화 지속성에서 읽히는 거리"),
                    CompatibilitySectionSpec("approach", "부담 없는 접근", "친밀감이 자라는 방식", "상대가 압박 없이 마음을 열 수 있는 관계 리듬"),
                    CompatibilitySectionSpec("choice", "기다림의 기준", "내 마음을 지킬 선", "관계 가능성과 일방적인 소모를 구분하는 기준")
                )
                else -> listOf(
                    CompatibilitySectionSpec("chemistry", "두 사람의 공기", "말하지 않아도 통하는 부분", "짧은 만남에서도 친밀감이 생기는 이유"),
                    CompatibilitySectionSpec("ambiguity", "애매함의 정체", "확신이 늦어지는 이유", "상대의 상황과 표현 방식이 만드는 모호함"),
                    CompatibilitySectionSpec("timing", "마음의 속도", "지금 표현해도 될까", "감정을 드러낼 때 관계가 받아들일 수 있는 온도"),
                    CompatibilitySectionSpec("outcome", "가능성의 방향", "이어질 때와 멈출 때", "서로의 반응으로 확인되는 다음 단계")
                )
            }
            CompatibilityRelationshipStatus.REUNION -> when (variant) {
                0 -> listOf(
                    CompatibilitySectionSpec("remaining", "아직 남은 것", "감정이 끝나지 않은 이유", "그리움과 미해결 감정을 구분하는 장면"),
                    CompatibilitySectionSpec("breakpoint", "관계가 멈춘 곳", "헤어진 이유의 핵심", "반복되던 갈등과 놓쳤던 감정"),
                    CompatibilitySectionSpec("contact", "다시 닿는 온도", "연락이 받아들여질 조건", "재촉과 진심이 다르게 읽히는 연락 방식"),
                    CompatibilitySectionSpec("boundary", "재회의 문턱", "다시 만나기 전 필요한 변화", "예전 관계로 돌아가지 않기 위한 조건")
                )
                1 -> listOf(
                    CompatibilitySectionSpec("memory", "추억의 무게", "좋았던 기억이 남긴 것", "현재의 외로움과 실제 관계 가치를 구분하는 관점"),
                    CompatibilitySectionSpec("pattern", "반복된 장면", "다시 만나도 부딪힐 부분", "사과만으로 바뀌지 않는 관계 습관"),
                    CompatibilitySectionSpec("welcome", "상대의 현재", "연락을 열어둘 가능성", "상대가 여지를 보이는 반응과 경계를 세우는 반응"),
                    CompatibilitySectionSpec("renewal", "새 관계의 조건", "재회가 아니라 새로 시작하기", "두 사람이 실제로 달라졌는지 확인할 지점")
                )
                else -> listOf(
                    CompatibilitySectionSpec("unfinished", "미완의 마음", "자꾸 돌아보게 되는 이유", "후회, 미련, 애정이 섞여 있는 현재 감정"),
                    CompatibilitySectionSpec("cause", "멀어진 과정", "마음이 닫힌 순간", "관계가 끝난 결정적 장면과 누적된 피로"),
                    CompatibilitySectionSpec("window", "연락의 창", "대화가 가능한 시기", "상대의 경계를 존중하면서 대화가 열릴 조건"),
                    CompatibilitySectionSpec("decision", "다시 선택한다면", "같은 결말을 피할 기준", "재회 여부보다 먼저 확인할 관계의 안전성")
                )
            }
        }
        val extraSections = listOf(
            CompatibilitySectionSpec(
                "opportunity",
                "기회",
                "관계가 움직일 가능성",
                when (relationshipStatus) {
                    CompatibilityRelationshipStatus.COUPLE -> "둘 사이의 신뢰, 데이트, 대화가 좋아질 실제 기회와 추천 시기"
                    CompatibilityRelationshipStatus.CRUSH -> "상대 관심도, 호감 가능성, 경쟁자 여부, 관계 발전 가능성과 고백 타이밍"
                    CompatibilityRelationshipStatus.REUNION -> "상대의 현재 심리, 연락 가능성, 재회 가능성과 대화가 열릴 조건"
                }
            ),
            CompatibilitySectionSpec(
                "avoid",
                "피해야 할 행동",
                "관계를 더 멀게 만드는 선택",
                when (relationshipStatus) {
                    CompatibilityRelationshipStatus.COUPLE -> "떠보기, 압박, 과거 갈등 반복, 일방적 결론처럼 현재 관계를 흔드는 행동과 이유"
                    CompatibilityRelationshipStatus.CRUSH -> "과한 연락, 의미 부여, 경쟁심, 성급한 고백처럼 부담을 키우는 행동과 이유"
                    CompatibilityRelationshipStatus.REUNION -> "반복 연락, 감정 호소, 답을 재촉하기, 과거 미화처럼 재회를 방해하는 요소"
                }
            ),
            CompatibilitySectionSpec(
                "recommendation",
                "추천 행동",
                "지금 가능한 현실적인 선택",
                when (relationshipStatus) {
                    CompatibilityRelationshipStatus.COUPLE -> "연락, 데이트, 갈등 회복에서 오늘부터 가능한 구체적인 행동 2~3개와 관계 총평"
                    CompatibilityRelationshipStatus.CRUSH -> "먼저 연락할지, 고백 시점, 관계 발전을 높이는 구체적인 행동 2~3개와 총평"
                    CompatibilityRelationshipStatus.REUNION -> "먼저 연락할지 기다릴지 판단 기준, 재회 가능성을 높이는 행동 2~3개와 총평"
                }
            ),
            CompatibilitySectionSpec(
                "lucky_elements",
                "행운의 요소",
                "관계의 상징과 한줄 조언",
                "행운의 숫자, 색상, 방향, 시간, 요일을 모두 포함하고 상징적 참고 정보임을 밝힌 뒤 짧고 희망적인 상담 한마디로 마무리"
            )
        )
        val coverTitle = when (relationshipStatus) {
            CompatibilityRelationshipStatus.COUPLE -> "우리 사이의 리듬"
            CompatibilityRelationshipStatus.CRUSH -> "마음이 닿을 가능성"
            CompatibilityRelationshipStatus.REUNION -> "다시 이어질 조건"
        }
        return CompatibilityNarrativePlan(
            coverTitle = coverTitle,
            coverSubtitle = "두 사람의 ${angle}을 중심으로 읽었습니다.",
            angle = angle,
            sections = statusSections + extraSections
        )
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

    private data class CompatibilityNarrativePlan(
        val coverTitle: String,
        val coverSubtitle: String,
        val angle: String,
        val sections: List<CompatibilitySectionSpec>
    )

    private data class CompatibilitySectionSpec(
        val id: String,
        val ribbon: String,
        val title: String,
        val focus: String
    )

    companion object {
        private const val SYSTEM_PROMPT =
            "Write concise Korean compatibility counseling JSON within 2,500 Korean characters total. Vary the narrative structure by relationship status and follow the supplied section blueprint exactly. Be concrete and polite. JSON only."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}
