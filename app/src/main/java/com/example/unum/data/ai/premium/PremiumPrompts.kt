package com.example.unum.data.ai.premium

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.NumerologyCalculator

import com.example.unum.domain.usecase.*

internal fun buildPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
    val input = bundle.input
    val destiny = bundle.content.destinyProfile
    val hiddenCue = buildHiddenBirthCue(input)
    val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }
    val traitBrief = buildTraitBrief(topic, bundle)
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

internal fun buildDetailedPremiumPrompt(
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

internal fun buildRomanceSalonPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
    val destiny = bundle.content.destinyProfile
    val concernText = concern.ifBlank { "요즘 연애에서 어떤 흐름이 열릴지 알고 싶습니다." }
    val traitBrief = buildTraitBrief(topic, bundle)
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

internal fun buildCompactPremiumPrompt(
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

internal fun buildCompactRomancePrompt(
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

internal fun buildShortPremiumPrompt(
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

internal fun buildRomanceSalonPromptV2(
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

internal fun buildTraitBrief(topic: PremiumTopic, bundle: NumerologyResultBundle): String {
    val profile = bundle.content.destinyProfile
    val record = bundle.content.lifeRecord
    val topicScene = when (topic) {
        PremiumTopic.ROMANCE, PremiumTopic.RELATIONSHIP -> record.relationshipText
        PremiumTopic.CAREER, PremiumTopic.BUSINESS -> record.workText
        PremiumTopic.MONEY -> record.moneyText
        else -> record.interactionText
    }.ifBlank { profile.summary }
    val caution = profile.cautionKeywords.take(2).joinToString(", ").ifBlank { "반복 손실" }
    val genderCue = record.genderText.takeIf(String::isNotBlank)?.let { " 성별 공명 참고: $it" }.orEmpty()
    return "${profile.title} 유형의 관찰 장면: $topicScene$genderCue 주의할 반복 패턴: $caution."
}

internal fun buildHiddenBirthCue(input: BirthInput): String {
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


internal const val SYSTEM_PROMPT =
    "Write concise Korean premium counseling JSON in a natural human voice. Keep all counseling text within 2,500 Korean characters total. Avoid repeated expressions, abstract filler, deterministic predictions, and fear-mongering. JSON only."
internal const val OPENAI_MODEL = "gpt-5.1"

