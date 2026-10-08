package com.example.unum.data.ai.premium

import com.example.unum.data.ai.FORTUNE_WRITING_RULES
import com.example.unum.data.ai.bundlePromptContext
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.usecase.*
import org.json.JSONArray
import org.json.JSONObject

internal fun buildPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
    val currentMonth = PremiumMonthPlanner.currentMonth()
    val bestMonth = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, currentMonth).toDisplayText()
    val riskyMonth = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers, currentMonth).toDisplayText()
    val context = JSONObject().put("topic", topic.label).put("concern", concern.trim())
        .put("hasConcern", concern.isNotBlank()).put("person", bundlePromptContext(bundle))
        .put("currentMonth", currentMonth).put("bestMonth", bestMonth).put("riskyMonth", riskyMonth)
    val sections = premiumSections(topic)
    val toc = JSONArray()
    val pages = JSONArray()
    sections.forEach {
        toc.put(JSONObject().put("id", it.id).put("title", it.title))
        pages.put(JSONObject().put("id", it.id).put("ribbon", it.ribbon).put("title", it.title).put("highlight", "").put("body", JSONArray().put("")))
    }
    val schema = JSONObject().put("coverTitle", "").put("coverSubtitle", "")
        .put("bestMonth", bestMonth).put("bestMonthReason", "").put("riskyMonth", riskyMonth).put("riskyMonthReason", "")
        .put("answerCard", JSONObject().put("question", "").put("shortAnswer", "").put("body", JSONArray().put("").put("")))
        .put("toc", toc).put("pages", pages).put("closingAdvice", "")
    val guide = sections.joinToString("\n") { "id=${it.id}, title=${it.title}: ${it.requirements}" }
    return """
        [상담 입력 JSON]
        $context
        [입력 끝]

        [장별 역할]
        $guide
        순서와 id를 유지합니다. 본문은 한 문단 2~3문장, 핵심 장은 보통 120~220자입니다. 짧게 끝나는 장은 늘려 쓰지 않습니다.
        lucky_elements는 60~120자, closing은 60~100자 이내입니다. answerCard.shortAnswer는 40~70자, body는 합계 100~180자 이내로 질문에 직접 답합니다.
        월별 이유는 80자 이내, closingAdvice는 40자 이내입니다. 추천 행동을 caution·avoid·closing에 반복하지 않습니다.
        표지 제목은 사용자의 질문을 반영한 짧은 제목으로 씁니다. 빈 고민에는 분야에 맞는 일반 제목을 씁니다.

        [문체 참고 예시: 해당 고민과 숫자일 때만 참고]
        1번 / 재직 중 이직 고민: 1번은 새로운 목표가 생기면 먼저 행동하는 추진력이 강한 성격입니다. 관심 있는 회사에 지원하고 면접 기회를 만드는 데 적극성을 발휘할 수 있습니다. 다만 빨리 옮기고 싶은 마음에 급여나 업무 조건을 충분히 확인하지 않은 채 퇴사를 결정할 수 있어 주의가 필요합니다.
        4번 / 처음 배우는 과목: 4번은 기초와 원리를 이해해야 안심하는 성격입니다. 풀이를 외우는 것보다 공식이 적용되는 이유를 이해할 때 배운 내용을 다른 문제에도 활용하기 쉽습니다. 다만 모르는 부분 하나를 완벽히 이해하려고 붙잡으면 시험 범위를 다 살펴볼 시간이 부족해질 수 있습니다.
        예시 문장을 다른 숫자와 고민에 재사용하지 않습니다. 실제 행동은 recommendation 장에서 1~2개로 제시합니다.

        [출력 JSON 형식]
        $schema
    """.trimIndent()
}

internal const val SYSTEM_PROMPT = FORTUNE_WRITING_RULES
internal const val OPENAI_MODEL = "gpt-5.1"

internal fun premiumSections(topic: PremiumTopic): List<PremiumSectionSpec> {
    val field = when (topic) {
        PremiumTopic.ROMANCE -> "사용자가 밝힌 연애 상태와 연락·만남·표현에 관한 실제 질문"
        PremiumTopic.CAREER -> "사용자가 밝힌 취업·업무·이직 중 해당 상황과 직무·조건·준비 과정"
        PremiumTopic.MONEY -> "사용자가 밝힌 소비·저축·수입·계약 중 해당 돈의 사용 목적과 확인 가능한 조건"
        PremiumTopic.STUDY -> "사용자가 준비하는 시험·과목·과제와 이해·복습·시험 시간 배분"
        PremiumTopic.HEALTH -> "사용자가 말한 수면·피로·운동·생활 습관. 증상을 추정하거나 진단하지 않기"
        PremiumTopic.BUSINESS -> "사용자가 설명한 사업 단계·고객·거래·운영 비용과 실제 의사결정"
        PremiumTopic.GENERAL -> "사용자가 궁금해하는 일상 영역. 질문이 없다면 성격이 일·관계·생활에 나타나는 모습"
        PremiumTopic.SELF_ESTEEM -> "사용자가 말한 비교·자신감·부담·자기 이해 중 해당 고민. 마음 상태를 지어내지 않기"
        PremiumTopic.RELATIONSHIP -> "사용자가 언급한 가족·친구·동료 등 해당 상대와 실제 대화나 관계 상황"
    }
    return listOf(
        PremiumSectionSpec("current_flow", "고민의 핵심", "지금 고민하는 이유", "$field 중심으로 질문의 핵심을 짚기. 사용자 말을 길게 되풀이하지 않기"),
        PremiumSectionSpec("good_energy", "나의 강점", "성격이 도움이 되는 부분", "핵심수의 구체적인 성격 한 가지가 이 고민에 어떤 도움을 주는지 이유와 장면 연결"),
        PremiumSectionSpec("caution", "어려움의 이유", "생각대로 되지 않는 이유", "같은 성격이 이 상황에서 어려움을 만드는 과정 한 가지. 발생한 사실처럼 단정하거나 다른 장의 조언 반복하지 않기"),
        PremiumSectionSpec("opportunity", "기회의 조건", "좋은 결과에 필요한 조건", "상황이 나아지려면 실제로 어떤 조건이나 반응이 필요한지 설명. 미래 사건과 유리한 날짜를 꾸며내지 않기"),
        PremiumSectionSpec("avoid", "선택별 차이", "어떤 선택이 더 맞을까", "사용자가 제시한 선택지의 이점과 부담 비교. 선택지가 없으면 고민에 맞는 두 접근을 가정임을 밝히고 비교. caution의 위험을 반복하지 않기"),
        PremiumSectionSpec("recommendation", "추천 행동", "지금 할 수 있는 행동", "이 고민의 해결에 필요한 구체적인 행동 1~2개와 각각의 목적. 대상 없는 실행·도전이나 다른 장의 내용 재진술 금지"),
        PremiumSectionSpec("lucky_elements", "상징적 참고", "시기와 행운의 요소", "제공된 추천·주의 월을 간단히 연결하고 행운의 숫자·색상·방향·시간·요일을 제시. 참고 상징이라는 설명을 한 번만 하기"),
        PremiumSectionSpec("closing", "마무리", "나에게 남기는 이야기", "질문에 대한 관점을 짧게 마무리. 다른 장의 행동을 재차 지시하거나 새 문제를 추가하지 않기")
    )
}

internal data class PremiumSectionSpec(val id: String, val ribbon: String, val title: String, val requirements: String)
