package com.example.unum.data.ai.compatibility

import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.NumerologyResultBundle

import com.example.unum.domain.usecase.*

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

internal fun traitBrief(bundle: NumerologyResultBundle): String {
    val profile = bundle.content.destinyProfile
    val caution = profile.cautionKeywords.take(2).joinToString(", ").ifBlank { "조급함" }
    val scene = bundle.content.lifeRecord.relationshipText.ifBlank { profile.summary }
    val genderCue = bundle.content.lifeRecord.genderText
        .takeIf(String::isNotBlank)
        ?.let { " 성별 공명 참고: $it" }
        .orEmpty()
    return "${profile.title} 유형의 관계 장면: $scene$genderCue 반복될 때 손해가 커지는 지점: $caution."
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
