package com.example.unum.data.ai.compatibility

import com.example.unum.data.ai.FORTUNE_WRITING_RULES
import com.example.unum.data.ai.bundlePromptContext
import org.json.JSONArray
import org.json.JSONObject
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.usecase.*

internal fun buildPrompt(
    maleBundle: NumerologyResultBundle,
    femaleBundle: NumerologyResultBundle,
    concern: String,
    relationshipStatus: CompatibilityRelationshipStatus,
    relationshipNumber: Int,
    requesterIsPersonA: Boolean? = null
): String {
    val concernText = concern.trim()
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
        riskyMonth = riskyMonth,
        requesterIsPersonA = requesterIsPersonA
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
    riskyMonth: String,
    requesterIsPersonA: Boolean? = null
): String {
    val narrativePlan = compatibilityNarrativePlan(relationshipStatus, relationshipNumber, concernText)
    val context = JSONObject().put("status", relationshipStatus.label).put("concern", concernText)
        .put("hasConcern", concernText.isNotBlank()).put("year", createdYear).put("currentMonth", currentMonth)
        .put("requester", when (requesterIsPersonA) { true -> "personA"; false -> "personB"; null -> JSONObject.NULL })
        .put("personA", bundlePromptContext(maleBundle).put("relationshipTrait", maleTrait))
        .put("personB", bundlePromptContext(femaleBundle).put("relationshipTrait", femaleTrait))
        .put("relationshipNumber", relationshipNumber).put("relationshipCue", relationshipMeaning)
        .put("cautions", cautionKeywords).put("bestMonth", bestMonth).put("riskyMonth", riskyMonth)
    val toc = JSONArray()
    val pages = JSONArray()
    narrativePlan.sections.forEach {
        toc.put(JSONObject().put("id", it.id).put("title", it.title))
        pages.put(JSONObject().put("id", it.id).put("ribbon", it.ribbon).put("title", it.title).put("highlight", "").put("body", JSONArray().put("")))
    }
    val schema = JSONObject().put("coverTitle", narrativePlan.coverTitle).put("coverSubtitle", narrativePlan.coverSubtitle)
        .put("bestMonth", bestMonth).put("bestMonthReason", "").put("riskyMonth", riskyMonth).put("riskyMonthReason", "")
        .put("answerCard", JSONObject().put("question", "").put("shortAnswer", "").put("body", JSONArray().put("").put("")))
        .put("toc", toc).put("pages", pages).put("closingAdvice", "")
    val guide = narrativePlan.sections.joinToString("\n") { "id=${it.id}, title=${it.title}: ${it.focus}" }
    val statusRule = when (relationshipStatus) {
        CompatibilityRelationshipStatus.COUPLE -> "이미 교제 중인 관계입니다. 사용자가 말한 대화·약속·갈등을 중심으로 두 성격의 차이를 설명합니다. 갈등을 말하지 않았다면 있다고 가정하지 않습니다."
        CompatibilityRelationshipStatus.CRUSH -> "짝사랑입니다. 친절과 호감은 구별하며 상대의 실제 마음은 숫자로 확인할 수 없습니다. 사용자가 말한 반응이 없으면 확인할 수 있는 상호 반응을 조건으로 설명합니다. 거절 의사를 존중합니다."
        CompatibilityRelationshipStatus.REUNION -> "재회 고민입니다. 이별 원인과 상대의 현재 마음을 지어내지 않습니다. 사용자가 말한 이별 사유와 연락 반응만 참고하고 다시 만날 때 필요한 실제 변화를 설명합니다. 연락 거부 의사를 존중합니다."
    }
    return """
        [상담 입력 JSON]
        $context
        [입력 끝]

        [관계 상태]
        $statusRule
        requester가 가리키는 사람이 질문자이고 다른 사람이 상대방입니다. 값이 null이면 어느 쪽이 질문자인지 추정하지 않습니다. 성별에 따른 역할을 부여하지 않습니다.
        두 사람의 성격을 각각 길게 나열하지 말고 같은 상황에 다르게 반응하는 이유를 연결합니다.
        관계수는 보조 해석입니다. 두 사람 각자의 핵심 성격과 사용자가 직접 밝힌 사실을 우선합니다.
        [장별 역할]
        $guide
        이 순서와 id를 유지합니다. 본문은 한 문단 2~3문장, 보통 120~220자이며 짧게 끝나는 장은 늘려 쓰지 않습니다.
        실천은 recommendation에 서로 다른 1~2개만 제안합니다. opportunity는 조건, avoid는 행동의 영향, recommendation은 실행 방법으로 구분합니다.
        lucky_elements는 60~120자입니다. answerCard.shortAnswer는 40~70자, body는 합계 100~180자 이내로 질문에 먼저 답합니다.
        월별 이유는 80자 이내, closingAdvice는 40자 이내입니다. 각 장에서 같은 경고나 '선을 정하세요' 같은 조언을 반복하지 않습니다.
        설명할 과거 사건이나 상대 반응이 없으면 조건·가정임을 밝힙니다. 상대가 이미 호감이나 미련을 가진 것처럼 결론내리지 않습니다.
        [출력 JSON 형식]
        $schema
    """.trimIndent()
}
internal const val SYSTEM_PROMPT = FORTUNE_WRITING_RULES
internal const val OPENAI_MODEL = "gpt-5.1"

internal fun compatibilityNarrativePlan(
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
        1, 4 -> "주도권, 약속, 생활 속 책임이 관계의 온도에 미치는 영향"
        2, 6 -> "배려가 편안함이 되는 순간과 부담으로 바뀌는 순간의 차이"
        8 -> "친구와 모임에서의 사교성이 둘만의 관계에 미치는 영향"
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
                CompatibilitySectionSpec("signal", "호감의 단서", "마음이 보이는 순간", "사용자가 말한 반응이 있다면 참고하고 없다면 호감과 친절을 구별할 수 있는 조건"),
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
                CompatibilitySectionSpec("cause", "멀어진 과정", "마음이 닫힌 순간", "사용자가 밝힌 이별 과정과 두 성격의 차이. 이별 원인을 말하지 않았다면 추정하지 않기"),
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
                CompatibilityRelationshipStatus.CRUSH -> "서로 연락과 만남을 이어갈 의사가 있는지 확인할 조건. 경쟁자 존재나 상대의 속마음을 추정하지 않기"
                CompatibilityRelationshipStatus.REUNION -> "사용자가 밝힌 연락 반응과 재회 전에 달라져야 할 조건. 상대의 현재 심리를 추정하지 않기"
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
                CompatibilityRelationshipStatus.COUPLE -> "연락, 데이트, 갈등 회복에서 오늘부터 가능한 구체적인 행동 1~2개와 각각의 목적"
                CompatibilityRelationshipStatus.CRUSH -> "먼저 연락할지, 고백 시점, 관계 발전을 높이는 구체적인 행동 1~2개와 각각의 목적"
                CompatibilityRelationshipStatus.REUNION -> "먼저 연락할지 기다릴지 판단 기준, 상대 의사를 존중하는 행동 1~2개와 각각의 목적"
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
        coverSubtitle = when (relationshipStatus) {
            CompatibilityRelationshipStatus.COUPLE -> "서로의 성격과 함께하는 일상을 살펴봅니다."
            CompatibilityRelationshipStatus.CRUSH -> "다가가고 싶은 마음과 상대의 반응을 살펴봅니다."
            CompatibilityRelationshipStatus.REUNION -> "지난 관계와 다시 만날 때 필요한 변화를 살펴봅니다."
        },
        angle = angle,
        sections = statusSections + extraSections
    )
}
internal fun traitBrief(bundle: NumerologyResultBundle): String {
    val profile = bundle.content.destinyProfile
    val caution = profile.cautionKeywords.take(2).joinToString(", ").ifBlank { "조급함" }
    val scene = bundle.content.lifeRecord.relationshipText.ifBlank { profile.summary }
    return "${profile.title} 성격의 관계 장면: $scene 주의할 성향: $caution."
}
internal fun relationshipNumber(maleBundle: NumerologyResultBundle, femaleBundle: NumerologyResultBundle): Int {
    return (maleBundle.numbers.destiny + femaleBundle.numbers.destiny) % 10
}
internal fun relationshipNumbers(
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
internal fun calendarTypeLabel(type: CalendarType): String {
    return if (type == CalendarType.LUNAR) "음력" else "양력"
}
internal fun relationshipMeaning(number: Int): String = when (number) {
    0 -> "새로운 데이트나 대화 주제를 떠올리는 창의력. 아이디어가 많아도 실제 약속이 이어지는지는 따로 확인합니다."
    1 -> "먼저 연락하고 만남을 제안하는 추진력. 상대도 만날 의사가 있는지 확인하는 과정이 필요합니다."
    2 -> "말투와 표정의 작은 차이를 알아차리는 관찰력. 한 반응만으로 상대 마음을 단정하지 않습니다."
    3 -> "대화를 즐겁게 이끄는 언변력. 말이 많아질 때 상대의 이야기도 충분히 들을 수 있는지가 중요합니다."
    4 -> "약속과 기본적인 신뢰를 중요하게 여기는 안정 지향 성향. 익숙한 방식만 고집하면 다른 선호를 이해하기 어려울 수 있습니다."
    5 -> "새 경험을 즐기는 모험심과 적응력. 즉흥적인 약속이 상대의 일정과도 맞는지 살펴봅니다."
    6 -> "상대를 챙기고 약속을 지키려는 책임감. 상대가 원하지 않은 도움은 부담이 될 수 있습니다."
    7 -> "대화와 행동의 이유를 이해하려는 분석력. 작은 반응마다 의미를 찾으면 피로해질 수 있습니다."
    8 -> "사람들과 어울리고 서로 소개하는 사교성. 함께하는 모임과 둘만의 시간을 원하는 정도가 다를 수 있습니다."
    else -> "관계의 문제를 끝까지 풀고 마무리하려는 성향. 두 사람이 대화할 준비가 되어 있는지도 중요합니다."
}
internal data class CompatibilityNarrativePlan(
    val coverTitle: String,
    val coverSubtitle: String,
    val angle: String,
    val sections: List<CompatibilitySectionSpec>
)
internal data class CompatibilitySectionSpec(
    val id: String,
    val ribbon: String,
    val title: String,
    val focus: String
)

internal fun Int.floorMod(divisor: Int): Int = Math.floorMod(this, divisor)
