package com.example.unum.domain.usecase

import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.ConsultationAnswerCard
import com.example.unum.data.model.ConsultationPage
import com.example.unum.data.model.ConsultationTocItem
import com.example.unum.data.model.NumerologyResultBundle
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
        concern: String
    ): CompatibilityConsultation = withContext(Dispatchers.IO) {
        val relationshipNumber = relationshipNumber(maleBundle, femaleBundle)
        val prompt = buildPrompt(
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
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
        relationshipNumber: Int
    ): String {
        val maleInput = maleBundle.displayInput
        val femaleInput = femaleBundle.displayInput
        val concernText = concern.ifBlank { "두 사람의 관계가 잘 이어질 수 있을지 궁금합니다." }
        val createdYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val maleTrait = traitBrief(maleBundle)
        val femaleTrait = traitBrief(femaleBundle)
        val cautionKeywords = listOf(
            maleBundle.content.destinyProfile.cautionKeywords.take(2),
            femaleBundle.content.destinyProfile.cautionKeywords.take(2)
        ).flatten().distinct().joinToString(", ").ifBlank { "속도 차이, 감정 확인, 거리 조절" }

        return """
            당신은 한국어 수리학 기반 프리미엄 상담 콘텐츠 작가입니다.
            사용자의 생년월일, 관계수, 궁합 고민을 바탕으로 모바일에서 읽기 쉬운 “상담소형 결과 페이지”를 작성하세요.

            [작성 목표]
            - 긴 리포트가 아니라, 짧은 상담 페이지 묶음으로 작성합니다.
            - 한 페이지에는 하나의 메시지만 담습니다.
            - 각 페이지는 핵심 문장 1개와 짧은 문단 2~3개로 제한합니다.
            - 관계수는 점수처럼 쓰지 말고, 두 사람 사이에 생기는 흐름의 상징으로만 해석합니다.
            - “좋다/나쁘다” 식의 단정 대신 “이런 흐름이 강하다”, “이런 선택을 조심하라”로 말합니다.
            - 사주, 타로, 괘, 점괘라는 단어는 쓰지 않습니다.
            - 사용자를 선생님이라고 부르지 않습니다.
            - 말투는 따뜻하지만 약간 상담소처럼 콕 짚는 존댓말입니다.
            - 불안을 부추기는 저주식 표현은 피하되, 방치하면 관계가 꼬이거나 상처가 커질 수 있다는 현실적 경고는 분명히 씁니다.
            - 무료 결과에서 이미 말한 개인 성향 설명을 반복하지 않습니다.

            [입력 정보]
            - 상담 종류: 궁합
            - 고민 내용: $concernText
            - 상담 생성 연도: $createdYear
            - 남자 생년월일: ${maleInput.year}.${maleInput.month}.${maleInput.day}
            - 남자 달력 구분: ${calendarTypeLabel(maleInput.calendarType)}
            - 남자 운명수: ${maleBundle.numbers.destiny}
            - 남자 보조 숫자: ${maleBundle.numbers.early}, ${maleBundle.numbers.middle}, ${maleBundle.numbers.late}, ${maleBundle.numbers.code}
            - 남자 성향 요약: $maleTrait
            - 여자 생년월일: ${femaleInput.year}.${femaleInput.month}.${femaleInput.day}
            - 여자 달력 구분: ${calendarTypeLabel(femaleInput.calendarType)}
            - 여자 운명수: ${femaleBundle.numbers.destiny}
            - 여자 보조 숫자: ${femaleBundle.numbers.early}, ${femaleBundle.numbers.middle}, ${femaleBundle.numbers.late}, ${femaleBundle.numbers.code}
            - 여자 성향 요약: $femaleTrait
            - 관계수: $relationshipNumber
            - 관계수 해석 참고: ${relationshipMeaning(relationshipNumber)}
            - 주의 키워드: $cautionKeywords

            [궁합 결과 페이지 구성]
            1. cover
               - title: “수리 궁합 상담소”
               - subtitle: “두 사람 사이의 흐름을 읽어볼게요.”

            2. answer
               - question: 사용자의 궁합 고민을 질문형으로 재작성
               - shortAnswer: 둘의 관계를 한 문장으로 진단
               - body: 관계의 전체 분위기 2문단

            3. attraction
               - ribbon: “서로 끌리는 이유”
               - title: “맞닿는 지점”
               - highlight: 두 사람이 끌리는 핵심 이유
               - body: 말투, 속도, 감정 반응, 생활 리듬 중심

            4. friction
               - ribbon: “주의사항”
               - title: “엇갈리는 방식”
               - highlight: 관계에서 반복될 수 있는 충돌 한 문장
               - body: 실제 다툼 장면, 오해 방식, 감정 처리 차이

            5. view
               - ribbon: “상대가 보는 나”
               - title: “상대의 눈에 비친 모습”
               - highlight: 상대가 느끼는 인상 한 문장
               - body: 장점과 부담으로 느낄 수 있는 점을 균형 있게 설명

            6. action
               - ribbon: “오래 가려면”
               - title: “관계를 살리는 습관”
               - highlight: 관계 유지의 핵심 조언
               - body: 대화법, 거리 조절, 갈등 후 회복법 3개 제안

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
                { "id": "attraction", "title": "" },
                { "id": "friction", "title": "" },
                { "id": "view", "title": "" },
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
        val pages = parsePages(json.optJSONArray("pages"))
        val toc = parseToc(json.optJSONArray("toc"))
        val attraction = pages.firstOrNull { it.id == "attraction" }
        val friction = pages.firstOrNull { it.id == "friction" }
        val view = pages.firstOrNull { it.id == "view" }
        val action = pages.firstOrNull { it.id == "action" }
        val closingAdvice = json.optString("closingAdvice")

        val parsed = CompatibilityConsultation(
            maleEnergy = view?.body?.joinToString("\n\n").orEmpty(),
            femaleEnergy = view?.highlight.orEmpty(),
            relationshipFlow = answerCard.body.joinToString("\n\n"),
            strengths = attraction?.body?.joinToString("\n\n").orEmpty(),
            friction = friction?.body?.joinToString("\n\n").orEmpty(),
            homeTone = action?.body?.joinToString("\n\n").orEmpty(),
            longTermTip = action?.highlight.orEmpty(),
            oneLineSummary = answerCard.shortAnswer.ifBlank { closingAdvice },
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

    private fun traitBrief(bundle: NumerologyResultBundle): String {
        val profile = bundle.content.destinyProfile
        val core = profile.coreKeywords.take(2).joinToString(", ").ifBlank { profile.title }
        val caution = profile.cautionKeywords.take(2).joinToString(", ").ifBlank { "조급함" }
        return "${profile.title} 기질은 $core 쪽이 강하고, 관계에서는 ${caution}이 과해질 수 있습니다."
    }

    private fun relationshipNumber(maleBundle: NumerologyResultBundle, femaleBundle: NumerologyResultBundle): Int {
        return (maleBundle.numbers.destiny + femaleBundle.numbers.destiny) % 10
    }

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
            "You are a Korean premium numerology compatibility writer. Write mobile-friendly consultation pages, not a long report. Use concrete relationship scenes, warm Korean honorific style, and realistic warnings without curses or deterministic fear. Never call the user 선생님. Return only valid JSON."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}
