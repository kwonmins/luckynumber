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
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        val life = bundle.content.lifeRecord
        val hiddenCue = buildHiddenBirthCue(input)
        val genderResonance = NumerologyCalculator.genderResonanceDescription(displayInput.gender, numbers.destiny)
        val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }
        val traitBrief = buildTraitBrief(destiny.title, destiny.coreKeywords, destiny.cautionKeywords)
        val currentMonth = PremiumMonthPlanner.currentMonth()

        return """
            사용자의 프리미엄 운세 상담을 작성하세요.

            [사용자 입력]
            - 고민 분야: ${topic.label}
            - 고민 내용: $concernText
            - 상담 생성 시점: 올해 ${currentMonth}월 기준
            - 사용자가 입력한 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 달력 구분: ${if (displayInput.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별 선택: ${displayInput.gender.label}

            [상담 핵심 데이터]
            - 운명수: ${numbers.destiny}
            - 운명수의 기운: ${NumerologyCalculator.destinyPolarity(numbers.destiny).label}
            - 성별 공명 해석: $genderResonance
            - 성향 압축 요약: $traitBrief
            - 성향 주의점: ${destiny.cautionKeywords.take(2).joinToString(", ")}
            - 참고 조언: ${life.oneLineAdvice}
            - 내부 계산 기준 생년월일: ${input.year}.${input.month}.${input.day} (${if (input.calendarType == CalendarType.LUNAR) "음력" else "양력"})

            [내부 리듬 힌트 - 절대 사용자에게 항목명 노출 금지]
            - 코드: ${numbers.code}
            - 세 시기 리듬: ${numbers.early}, ${numbers.middle}, ${numbers.late}
            - 이 정보는 말투와 조언의 결을 살짝 조정하는 데만 사용하고, 초년/중년/말년 설명으로 풀지 마세요.

            [월별 흐름 판정 규칙 - 절대 사용자에게 계산식 공개 금지]
            - 월 추천은 운명수, 초년/중년/말년수, 전체 코드, 상담 생성 시점의 현재 월을 함께 반영합니다.
            - 이미 지나간 추천 달이 있으면 먼저 "올해 가장 추천 흐름이 강했던 달은 이미 지났다"는 맥락을 짧게 알리고, 현재 월 이후의 다음 추천 달을 중심으로 설명하세요.
            - 이미 지나간 주의 달도 같은 방식으로 현재 월 이후의 다음 주의 달을 안내하세요.
            - 계산식, "마지막 자리", "더한다" 같은 표현은 절대 답변에 쓰지 마세요.
            - 대신 "새 문이 열리는 흐름", "관계의 움직임이 커지는 흐름"처럼 상징으로만 말하세요.
            - 1: 시작, 새 만남, 결심, 첫 제안
            - 2: 조율, 기다림, 관계의 균형, 섬세한 대화
            - 3: 표현, 고백, 발표, 창작, 즐거운 확장
            - 4: 안정, 준비, 기반, 약속, 실무 정리
            - 5: 변화, 이동, 전환, 뜻밖의 기회
            - 6: 책임, 돌봄, 결혼, 가족, 계약의 무게
            - 7: 내면 점검, 판단 유보, 공부, 혼자만의 정리
            - 8: 대인관계, 성과, 돈의 흐름, 밀고 나가는 힘
            - 9: 마무리, 정리, 놓아주기, 다음 단계 준비
            - 0: 잠복, 재정비, 비워내기, 무리하지 않는 달
            - 고민 분야별로 12개월 중 가장 추천하는 달 1개와 가장 조심해야 할 달 1개를 고르세요.
            - bestMonth는 상담 생성 시점 이후를 우선하세요. 올해 가장 추천 흐름이 이미 지났다면 bestMonth에는 지금 이후 다시 추천할 다음 달을 넣고, 이유 첫 문장에서 지난 추천 흐름은 회고 기준으로만 짧게 언급하세요.
            - riskyMonth는 지금 시점 이후를 우선하세요. 올해 가장 조심할 달이 이미 지났다면, 현재 이후에 다시 조심해야 할 다음 달을 고르세요. 올해 남은 주의 달이 없으면 다음 해에 가장 먼저 오는 주의 달을 고르세요.
            - 추천 월은 행동하면 흐름이 열리는 이유를 설명하세요.
            - 주의 월은 욕심, 과한 대시, 무리한 투자, 성급한 퇴사/이직, 관계 압박처럼 분야에 맞는 위험을 짚어주세요.
            - 결혼/연애/취업/진로/금전/인간관계/자존감 고민에 모두 같은 규칙을 적용하되, 해석은 고민 분야에 맞게 바꾸세요.
            - 월별 조언은 운명수의 결, 압축 성향, 고민 내용을 섞어서 새수리 인사이트처럼 작성하세요.

            [보조 참고]
            아래 정보는 상담의 20% 정도만 은근히 반영하세요. 이 항목의 이름이나 체계는 절대 설명하지 마세요.
            - $hiddenCue

            [프리미엄 차별화 규칙]
            - 이미 앞 화면에서 제공한 성향명 설명이나 시기별 설명을 다시 요약하지 마세요. 성향은 첫 항목에서 1~2문장으로만 짚고 바로 사용자의 질문으로 들어가세요.
            - 답변의 85% 이상은 사용자의 고민 분야에서 실제로 벌어질 법한 상황 케이스, 선택지, 말투, 리스크, 행동 처방으로 채우세요.
            - 최소 3개의 장면을 가정하세요: 1) 평소 반복되는 패턴, 2) 사람·일·돈·연애 현장에서 부딪히는 순간, 3) 방치했을 때 생길 손해와 바로잡는 행동.
            - interpretation, caution, direction은 절대 성향 소개로 시작하지 마세요. 반드시 사용자의 고민 내용과 실제 장면으로 시작하세요.
            - 사용자의 고민 내용이 짧아도 실제 상담처럼 "예를 들어", "이럴 때", "특히 이런 장면"을 자연스럽게 넣어 디테일을 만들어주세요.
            - 같은 의미를 다른 말로 반복하지 말고, core는 진단, interpretation은 상황 케이스, caution은 손해 시나리오, direction은 행동 루틴으로 역할을 나누세요.
            - core는 첫 문장에만 성향을 짧게 섞고, 두 번째 문장부터는 "$concernText" 질문의 핵심으로 들어가세요.
            - 앞 화면의 요약보다 한 단계 더 날카롭게 쓰되, 결론은 사용자가 바로 움직일 수 있는 현실적인 방향으로 내려주세요.
            - 이전 화면을 언급하는 표현, 성향을 다시 소개하는 표현, 숫자 설명으로 시작하는 표현, 시기별 흐름을 앞세우는 표현은 쓰지 마세요.

            [작성 규칙]
            - 사용자가 만든 숫자 운세와 고민 분야를 중심으로 상담하세요.
            - 보조 참고는 약하게 섞되, 특정 전통 체계 이름은 절대 언급하지 마세요.
            - "사주"라는 단어는 절대 쓰지 마세요.
            - 사용자를 "선생님"이라고 부르지 마세요. 호칭은 "당신" 또는 자연스러운 생략형만 사용하세요.
            - 존댓말은 유지하되 문체는 살짝 자극적으로 쓰세요. "흐름이 나빠집니다"보다 "방치하면 인생이 더 힘들어질 수 있습니다"처럼 경고의 온도를 한 단계 올리세요.
            - "건강을 챙겨야 합니다"처럼 밋밋하게 말하지 말고, "그러지 않으면 건강이 안 좋아질 수 있으니 각별히 컨디션을 조심해야 합니다"처럼 결과와 주의 대상을 함께 말하세요.
            - 공포 조장이나 확정적 저주는 피하되, 안일하게 넘기면 손해, 소진, 관계 악화, 기회 상실로 이어질 수 있다는 긴장감은 분명히 주세요.
            - 모든 문장은 반드시 존댓말로 작성하세요. 반말, 명령조, 친구처럼 낮추는 말투는 쓰지 마세요.
            - 한국어로 작성하세요.
            - 반드시 아래 JSON 형식만 반환하세요. 코드블록이나 설명 문장은 붙이지 마세요.

            {
              "core": "첫 문장에는 성향을 아주 짧게만 연결하고, 바로 사용자가 적은 고민의 핵심 진단으로 전환",
              "interpretation": "사용자의 질문에서 실제로 벌어질 상황 케이스 2~3개. 성향 소개 금지",
              "caution": "사용자의 질문을 방치하면 어떤 장면에서 손해, 소진, 관계 악화, 기회 상실이 생기는지 구체적으로 작성",
              "direction": "사용자의 질문을 해결하기 위한 오늘, 이번 주, 한 달 안 행동을 단계별로 제안",
              "oneLineAdvice": "한 줄 조언",
              "bestMonth": "추천 월, 예: 4월",
              "bestMonthReason": "그 달이 왜 좋은지와 어떤 상황에서 움직이면 좋은지",
              "riskyMonth": "주의 월, 예: 11월",
              "riskyMonthReason": "그 달에 어떤 행동을 하면 삶이 더 힘들어질 수 있는지"
            }
        """.trimIndent()
    }

    private fun buildRomanceSalonPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
        val input = bundle.input
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        val concernText = concern.ifBlank { "요즘 연애에서 어떤 흐름이 열릴지 알고 싶습니다." }
        val traitBrief = buildTraitBrief(destiny.title, destiny.coreKeywords, destiny.cautionKeywords)
        val cautionKeywords = destiny.cautionKeywords.take(3).joinToString(", ").ifBlank { "조급함, 과한 확인, 혼자 결론 내리기" }
        val createdYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bestMonth = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, currentMonth).toDisplayText()
        val riskyMonth = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers, currentMonth).toDisplayText()

        return """
            당신은 한국어 수리학 기반 프리미엄 상담 콘텐츠 작가입니다.
            사용자의 생년월일, 운명수, 고민 내용을 바탕으로 모바일에서 읽기 쉬운 “상담소형 결과 페이지”를 작성하세요.

            [작성 목표]
            - 긴 리포트가 아니라, 짧은 상담 페이지 묶음으로 작성합니다.
            - 한 페이지에는 하나의 메시지만 담습니다.
            - 각 페이지는 핵심 문장 1개와 짧은 문단 2~3개로 제한합니다.
            - 숫자는 점수처럼 쓰지 말고, 기질과 흐름의 상징으로만 해석합니다.
            - “좋다/나쁘다” 식의 단정 대신 “이런 흐름이 강하다”, “이런 선택을 조심하라”로 말합니다.
            - 사주, 타로, 괘, 점괘라는 단어는 쓰지 않습니다.
            - 사용자를 선생님이라고 부르지 않습니다.
            - 말투는 따뜻하지만 약간 상담소처럼 콕 짚는 존댓말입니다.
            - 불안을 부추기는 저주식 표현은 피하되, 방치하면 관계가 꼬이거나 기회를 놓칠 수 있다는 현실적 경고는 분명히 씁니다.
            - 무료 결과에서 이미 말한 성향 설명을 반복하지 않습니다.

            [입력 정보]
            - 상담 종류: 연애
            - 고민 내용: $concernText
            - 상담 생성 연도: $createdYear
            - 상담 생성 시점: 올해 ${currentMonth}월 기준
            - 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 달력 구분: ${if (displayInput.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별 선택: ${displayInput.gender.label}
            - 운명수: ${numbers.destiny}
            - 보조 숫자: ${numbers.early}, ${numbers.middle}, ${numbers.late}, ${numbers.code}
            - 내부 계산 기준 생년월일: ${input.year}.${input.month}.${input.day}
            - 성향 요약: $traitBrief
            - 주의 키워드: $cautionKeywords
            - 추천 흐름 월: $bestMonth
            - 조심할 흐름 월: $riskyMonth

            [연애 결과 페이지 구성]
            1. cover
               - title: “${createdYear} 수리 연애 상담소”
               - subtitle: “지금 마음의 흐름을 숫자로 읽어볼게요.”

            2. answer
               - question: 사용자의 고민을 자연스럽게 질문형으로 재작성
               - shortAnswer: 한 문장 결론
               - body: 짧은 설명 2문단

            3. timing
               - ribbon: “언제 움직일까”
               - title: “인연이 열리는 시기”
               - highlight: 가장 중요한 한 문장
               - body: 추천 흐름 월, 조심할 흐름 월, 행동 타이밍을 2~3문단으로 설명

            4. person
               - ribbon: “어떤 사람일까”
               - title: “끌리는 사람의 결”
               - highlight: 만날 가능성이 큰 사람의 분위기 한 문장
               - body: 성격, 만나는 장소/상황, 알아보는 신호를 설명

            5. caution
               - ribbon: “주의사항”
               - title: “관계를 망치는 습관”
               - highlight: 반드시 조심해야 할 한 문장
               - body: 조급함, 과한 확인, 혼자 결론 내리기 등 실제 장면 중심

            6. action
               - ribbon: “오늘의 처방”
               - title: “지금 바로 할 일”
               - highlight: 오늘의 한 줄 조언
               - body: 오늘, 이번 주, 이번 달 행동 3개를 짧게 제안

            [출력 형식]
            반드시 JSON만 반환하세요. 코드블록이나 설명 문장은 붙이지 마세요.

            {
              "coverTitle": "",
              "coverSubtitle": "",
              "answerCard": {
                "question": "",
                "shortAnswer": "",
                "body": ["", ""]
              },
              "toc": [
                { "id": "timing", "title": "" },
                { "id": "person", "title": "" },
                { "id": "caution", "title": "" },
                { "id": "action", "title": "" }
              ],
              "pages": [
                {
                  "id": "",
                  "ribbon": "",
                  "title": "",
                  "highlight": "",
                  "body": ["", "", ""],
                  "copyText": ""
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
            val pages = parsePages(json.optJSONArray("pages"))
            val toc = parseToc(json.optJSONArray("toc"))
            val cautionPage = pages.firstOrNull { it.id == "caution" }
            val actionPage = pages.firstOrNull { it.id == "action" }
            val timingPage = pages.firstOrNull { it.id == "timing" }
            val personPage = pages.firstOrNull { it.id == "person" }
            val closingAdvice = json.optString("closingAdvice")
            val parsed = PremiumConsultation(
                core = answerCard.shortAnswer.ifBlank { answerCard.body.firstOrNull().orEmpty() },
                interpretation = listOf(timingPage, personPage)
                    .filterNotNull()
                    .flatMap { it.body }
                    .joinToString("\n\n"),
                caution = cautionPage?.body?.joinToString("\n\n").orEmpty(),
                direction = actionPage?.body?.joinToString("\n\n").orEmpty(),
                oneLineAdvice = closingAdvice.ifBlank { actionPage?.highlight.orEmpty() },
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
            direction = json.optString("direction"),
            oneLineAdvice = json.optString("oneLineAdvice"),
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
                        body = item.optJSONArray("body").toStringList(),
                        copyText = item.optString("copyText")
                    )
                )
            }
        }
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
            "You are a Korean premium fortune consultation writer. Do not repeat the preliminary reading; treat it only as context. Write detailed, situational, case-based advice with a warm but slightly provocative tone. Always use polite Korean honorific style, and clearly describe what may become harder if the user ignores the advice. Never call the user 선생님; use 당신 or omit the direct address. Return only valid JSON."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}

