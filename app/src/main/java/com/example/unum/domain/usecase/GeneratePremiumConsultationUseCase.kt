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
        val input = bundle.input
        val destiny = bundle.content.destinyProfile
        val hiddenCue = buildHiddenBirthCue(input)
        val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }
        val traitBrief = buildTraitBrief(destiny.title, destiny.coreKeywords, destiny.cautionKeywords)
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bestMonth = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, currentMonth).toDisplayText()
        val riskyMonth = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers, currentMonth).toDisplayText()
        return buildDetailedPremiumPrompt(
            topic = topic,
            concernText = concernText,
            bundle = bundle,
            hiddenCue = hiddenCue,
            traitBrief = traitBrief,
            currentMonth = currentMonth,
            bestMonth = bestMonth,
            riskyMonth = riskyMonth
        )
    }

    private fun buildDetailedPremiumPrompt(
        topic: PremiumTopic,
        concernText: String,
        bundle: NumerologyResultBundle,
        hiddenCue: String,
        traitBrief: String,
        currentMonth: Int,
        bestMonth: String,
        riskyMonth: String
    ): String {
        val displayInput = bundle.displayInput
        val profile = bundle.content.destinyProfile
        val sections = premiumSections(topic)
        val sectionGuide = sections.mapIndexed { index, section ->
            "${index + 1}. id=${section.id}, ribbon=${section.ribbon}, title=${section.title}: ${section.requirements}"
        }.joinToString("\n")
        val tocSchema = sections.joinToString(prefix = "[", postfix = "]") {
            "{\"id\":\"${it.id}\",\"title\":\"${it.title}\"}"
        }
        val pagesSchema = sections.joinToString(prefix = "[", postfix = "]") {
            "{\"id\":\"${it.id}\",\"ribbon\":\"${it.ribbon}\",\"title\":\"${it.title}\",\"highlight\":\"\",\"body\":[\"\"]}"
        }

        return """
            당신은 20년 이상 상담 경험이 있는 한국어 프리미엄 운세 상담사입니다.
            미래를 단정하지 말고 현재의 가능성과 흐름을 상담하듯 설명하세요. 사용자가 실제 자기 이야기처럼 느낄 수 있도록 고민 속 장면과 이유를 구체적으로 연결하세요.

            [상담 입력]
            - 분야: ${topic.label}
            - 고민: $concernText
            - 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 달력: ${if (displayInput.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별: ${displayInput.gender.label}
            - 기준 월: ${currentMonth}월
            - 핵심수: ${bundle.numbers.destiny}
            - 성향: $traitBrief
            - 강점 키워드: ${profile.coreKeywords.joinToString(", ")}
            - 주의 키워드: ${profile.cautionKeywords.joinToString(", ")}
            - 보조 해석: $hiddenCue
            - 추천 시기: $bestMonth
            - 주의 시기: $riskyMonth

            [필수 상담 구조]
            $sectionGuide

            [작성 규칙]
            - 결과 JSON에 작성하는 모든 상담 문구의 합계는 반드시 한국어 기준 2,500자 이내로 제한하세요.
            - 8개 page의 body는 각각 160~220자, 한 문단, 2~3문장으로 작성하세요.
            - answerCard와 월별 이유는 각각 120자 이내, closingAdvice는 40자 이내로 작성하세요.
            - 같은 표현과 같은 문장 시작을 반복하지 마세요. 특히 "흐름이 강합니다", "할 수 있습니다", "좋습니다"의 연속 사용을 피하세요.
            - 추상적인 기운 설명만 하지 말고 연락, 약속, 업무, 계약, 소비, 휴식처럼 사용자가 알아볼 수 있는 장면을 쓰세요.
            - 좋은 부분은 왜 좋은지, 주의점은 무엇이 문제이며 어떻게 조절할 수 있는지까지 설명하세요.
            - 불안을 조성하거나 성공, 재회, 합격, 수익, 건강 결과를 확정하지 마세요.
            - 추천 행동은 바로 실행할 수 있는 구체적인 행동 1~2개만 자연스러운 문장으로 제안하세요.
            - 수첩, 메모, 일기, 기록, 체크리스트 작성이나 "적어두세요" 같은 조언은 사용하지 마세요.
            - 복사해서 보내는 문장, 공유용 문장, 상대에게 보낼 문구 섹션은 만들지 마세요.
            - 행운 요소는 숫자, 색상, 방향, 시간, 요일을 모두 포함하되 상징적인 참고 정보라고 밝혀주세요.
            - 각 page의 highlight는 해당 장을 한 문장으로 요약하고, closingAdvice는 짧고 기억에 남되 과장하지 마세요.
            - 이모지는 ribbon 또는 title에만 절제해서 사용하고 본문에는 과하게 반복하지 마세요.
            - 추천 시기와 주의 시기는 입력값을 그대로 사용하고 계산식을 노출하지 마세요.
            - JSON 외의 문장이나 코드 블록을 출력하지 마세요.

            [출력 JSON]
            {"coverTitle":"","coverSubtitle":"","bestMonth":"$bestMonth","bestMonthReason":"","riskyMonth":"$riskyMonth","riskyMonthReason":"","answerCard":{"question":"","shortAnswer":"","body":["",""]},"toc":$tocSchema,"pages":$pagesSchema,"closingAdvice":""}
        """.trimIndent()
    }

    private fun premiumSections(topic: PremiumTopic): List<PremiumSectionSpec> {
        val focus = when (topic) {
            PremiumTopic.ROMANCE -> listOf(
                "현재 연애 흐름, 새로운 인연, 기존 연인 관계를 구분해 현재 상태를 진단",
                "연락운, 데이트운, 고백운이 살아나는 장면과 연애 성공 포인트",
                "갈등 가능성, 감정 확인 속도, 과한 기대처럼 관계를 흔드는 원인과 조절법",
                "새 인연과 기존 관계에서 실제로 열릴 수 있는 기회 및 추천 시기",
                "압박, 떠보기, 단정적인 연락 등 피해야 할 행동과 그 이유",
                "솔로와 연인 모두 적용 가능한 구체적인 행동 및 이번 달 연애운 총평"
            )
            PremiumTopic.CAREER -> listOf(
                "현재 취업운, 직장운, 이직운을 나누어 지금의 위치를 진단",
                "면접운, 승진운, 상사운, 동료운에서 유리하게 작용하는 강점",
                "직장 내 오해, 성급한 퇴사, 준비 부족, 계약 조건에서 조심할 부분",
                "지원, 면접, 이직, 승진, 계약 중 가장 현실적인 기회와 시기",
                "감정적인 결정, 조건 미확인, 과도한 자기 증명처럼 피해야 할 행동",
                "취업 준비자와 재직자 모두 적용 가능한 행동 및 이번 달 커리어 총평"
            )
            PremiumTopic.MONEY -> listOf(
                "현재 금전 흐름, 수입운, 지출운을 구분하고 돈이 움직이는 구조를 진단",
                "부업운, 계약운, 수입이 늘어날 수 있는 기회와 활용 조건",
                "투자운의 위험 요소, 돈이 새는 원인, 반복되는 소비 습관과 조절법",
                "돈이 들어오는 현실적인 경로와 계약·협상에서 확인할 기회",
                "충동 소비, 검증되지 않은 투자, 급한 대출이나 보증처럼 피해야 할 행동",
                "소비 습관을 바꾸는 구체적인 행동 및 이번 달 재물운 총평"
            )
            PremiumTopic.STUDY -> listOf(
                "현재 집중력, 암기력, 학습 리듬과 슬럼프 여부를 구분해 진단",
                "시험운과 이해력이 살아나는 과목, 시간대, 공부 방식",
                "실수 가능성, 피로, 불안, 계획 과다처럼 성과를 떨어뜨리는 원인",
                "시험, 과제, 자격증 준비에서 활용할 수 있는 현실적인 기회",
                "밤샘, 계획만 늘리기, 취약 부분 회피처럼 피해야 할 행동",
                "집중력과 기억력을 높이는 공부법, 컨디션 관리 및 이번 달 학업운 총평"
            )
            PremiumTopic.HEALTH -> listOf(
                "현재 컨디션, 피로, 수면, 스트레스 상태를 생활 장면 중심으로 진단",
                "회복이 잘 되는 시간과 몸의 긍정적인 신호, 유지하면 좋은 생활 조건",
                "운동 부족, 수면 불규칙, 식습관, 과로에서 조심할 부분과 관리법",
                "생활 리듬과 컨디션을 회복하기 좋은 시기와 환경",
                "무리한 운동, 증상 방치, 극단적인 식단처럼 피해야 할 행동",
                "오늘부터 가능한 수면·운동·식습관 관리와 이번 달 건강운 총평. 의료 진단이 아니라 생활 참고임을 명시"
            )
            PremiumTopic.BUSINESS -> listOf(
                "현재 창업운, 사업 흐름, 현금 흐름과 운영 상태를 구분해 진단",
                "투자, 계약, 거래처, 직원운에서 유리하게 작용하는 강점",
                "확장 리스크, 비용 누수, 거래처 갈등, 계약 조건에서 조심할 부분",
                "새 고객, 제휴, 계약, 확장 중 가장 현실적인 기회와 시기",
                "검증 없는 확장, 구두 계약, 감정적 투자처럼 피해야 할 행동",
                "의사결정과 리스크를 줄이는 구체적인 행동 및 이번 달 사업운 총평"
            )
            PremiumTopic.GENERAL -> listOf(
                "전체적인 흐름과 이번 달 핵심 키워드를 일, 돈, 관계, 마음으로 나누어 진단",
                "현재 가장 좋은 운과 그 기운이 실제 생활에서 나타나는 장면",
                "가장 조심할 운, 무리하면 손해가 커질 영역과 조절법",
                "이번 달 활용할 기회와 중요한 날짜의 의미",
                "반드시 피해야 하는 선택과 그 이유",
                "반드시 해야 하는 현실적인 행동, 행운의 숫자·색·날짜·방향 및 인생 조언"
            )
            PremiumTopic.SELF_ESTEEM -> listOf(
                "현재 심리, 스트레스, 자존감, 감정 기복을 구분해 마음 상태를 진단",
                "회복력과 성장 가능성, 지금 스스로를 다시 신뢰할 수 있는 근거",
                "비교, 자기비난, 피로 누적, 감정 억압이 심해질 때 나타나는 신호",
                "회복이 빨라지는 환경, 쉬어야 하는 시기, 성장 포인트",
                "무리한 자기 증명, 관계에 맞춘 희생, 감정을 무시하는 행동을 피해야 하는 이유",
                "몸과 마음을 회복하는 구체적인 방법 및 이번 달 자아운 총평"
            )
            PremiumTopic.RELATIONSHIP -> listOf(
                "가족, 친구, 직장 관계와 새로운 인연을 나누어 현재 관계 상태를 진단",
                "도움을 주는 사람의 특징, 협력과 신뢰가 잘 형성되는 장면",
                "갈등 가능성, 조심해야 하는 사람과 관계 피로가 커지는 원인",
                "새로운 인연과 관계 회복에서 열릴 수 있는 현실적인 기회",
                "사람을 성급히 단정하거나 지나치게 맞춰주는 등 피해야 할 행동",
                "관계별 거리와 대화 방식을 조절하는 행동 및 이번 달 인간관계운 총평"
            )
        }
        return listOf(
            PremiumSectionSpec("current_flow", "현재 흐름", "지금 어디에 서 있나", focus[0]),
            PremiumSectionSpec("good_energy", "좋은 기운 ★★★★★", "잘 풀릴 수 있는 이유", focus[1]),
            PremiumSectionSpec("caution", "주의할 점 ★★★★★", "조심하면 달라지는 부분", focus[2]),
            PremiumSectionSpec("opportunity", "기회", "이번 시기에 열리는 문", focus[3]),
            PremiumSectionSpec("avoid", "피해야 할 행동", "흐름을 막는 선택", focus[4]),
            PremiumSectionSpec("recommendation", "추천 행동", "오늘부터 가능한 변화", focus[5]),
            PremiumSectionSpec("lucky_elements", "행운의 요소", "상징으로 보는 생활 힌트", "행운의 숫자, 색상, 방향, 시간, 요일을 빠짐없이 설명하고 각각이 현재 상담과 어떻게 연결되는지 이유를 제시"),
            PremiumSectionSpec("closing", "한줄 조언", "상담을 마치며", "전체 상담을 반복하지 말고 핵심을 짧게 정리한 뒤 현실적이고 희망적인 관점으로 마무리")
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

    private data class PremiumSectionSpec(
        val id: String,
        val ribbon: String,
        val title: String,
        val requirements: String
    )

    companion object {
        private const val SYSTEM_PROMPT =
            "Write concise Korean premium counseling JSON in a natural human voice. Keep all counseling text within 2,500 Korean characters total. Avoid repeated expressions, abstract filler, deterministic predictions, and fear-mongering. JSON only."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}


