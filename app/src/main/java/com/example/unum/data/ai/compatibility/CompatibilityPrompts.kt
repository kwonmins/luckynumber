package com.example.unum.data.ai.compatibility

import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic

import com.example.unum.domain.usecase.*

internal fun buildPrompt(
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

internal fun buildCompactCompatibilityPrompt(
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

internal fun buildCompatibilitySalonPromptV2(
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


internal const val SYSTEM_PROMPT =
    "Write concise Korean compatibility counseling JSON within 2,500 Korean characters total. Vary the narrative structure by relationship status and follow the supplied section blueprint exactly. Be concrete and polite. JSON only."
internal const val OPENAI_MODEL = "gpt-5.1"

