package com.example.unum.domain.usecase

import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic

class BuildPremiumDummyConsultationUseCase {
    operator fun invoke(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): PremiumConsultation {
        val concernText = concern.ifBlank { "아직 구체화되지 않은 고민" }
        val destiny = bundle.content.destinyProfile
        val coreKeyword = destiny.coreKeywords.firstOrNull() ?: destiny.title
        val bestSelection = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers)
        val riskySelection = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers)
        val bestMonth = bestSelection.toDisplayText()
        val riskyMonth = riskySelection.toDisplayText()

        return when (topic) {
            PremiumTopic.ROMANCE -> buildConsultation(
                core = "$coreKeyword 기운이 강하게 드러나는 연애 흐름입니다. 마음이 깊어질수록 관계를 빨리 확정하고 싶어질 수 있지만, 지금은 감정의 속도와 상대의 온도를 함께 보는 편이 더 자연스럽습니다.",
                interpretation = "'$concernText' 안에는 좋아하는 마음과 확인받고 싶은 마음이 함께 섞여 있습니다. 상대의 반응이 작아 보일수록 마음이 더 크게 흔들릴 수 있으니, 이 관계는 감정보다 속도 차이를 읽는 것이 핵심입니다.",
                caution = "확신을 서두르면 상대에게는 부담으로 느껴질 수 있습니다. 호감이 약해서가 아니라, 서로 편안해지는 속도가 아직 맞춰지는 중일 수 있습니다.",
                direction = "이 리포트는 행동 지시보다 관계의 온도를 읽는 데 초점을 둡니다. 지금의 흐름은 강하게 밀기보다 부담과 호감이 어디에서 갈리는지 살피는 쪽에 가깝습니다.",
                oneLineAdvice = "사랑은 확인을 세게 할수록 선명해지는 것이 아니라, 부담이 줄어들 때 오래 남기 쉽습니다.",
                bestMonth = bestMonth,
                riskyMonth = riskyMonth,
                bestMonthReason = monthTone(bestSelection, "마음의 온도가 부드럽게 맞춰지기 쉬운 구간입니다."),
                riskyMonthReason = "$riskyMonth 흐름에서는 감정의 속도 차이가 크게 느껴질 수 있습니다."
            )

            PremiumTopic.CAREER -> buildConsultation(
                core = "$coreKeyword 기운이 일과 진로 고민에서 강하게 드러납니다. 능력 부족보다 방향과 구조가 맞지 않아 피로가 커지는 흐름에 가깝습니다.",
                interpretation = "'$concernText' 안에는 계속 버틸지, 방향을 바꿀지에 대한 긴장이 들어 있습니다. 지금의 답답함은 게으름보다 역할과 환경의 결이 맞는지 확인하라는 신호에 가깝습니다.",
                caution = "성과만 보고 버티면 생활 리듬과 마음의 여유가 같이 줄어들 수 있습니다. 오래 갈 수 있는 구조인지 살피는 관점이 필요합니다.",
                direction = "이 리포트는 새 계획을 강요하기보다 일의 흐름이 어디에서 막히는지 읽는 데 초점을 둡니다. 지금은 노력의 양보다 방향의 결이 더 중요한 장면입니다.",
                oneLineAdvice = "진로는 더 많이 버티는 문제보다 오래 갈 수 있는 구조를 찾는 문제에 가깝습니다.",
                bestMonth = bestMonth,
                riskyMonth = riskyMonth,
                bestMonthReason = monthTone(bestSelection, "제안이나 방향 전환의 실마리가 비교적 선명하게 보이는 구간입니다."),
                riskyMonthReason = "$riskyMonth 흐름에서는 급한 결론이 피로를 더 키울 수 있습니다."
            )

            PremiumTopic.MONEY -> buildConsultation(
                core = "$coreKeyword 기운이 돈의 흐름에서도 드러납니다. 수입의 크기보다 기준이 흔들릴 때 불안이 커지는 타입입니다.",
                interpretation = "'$concernText' 안에는 얻고 싶은 마음과 잃을까 봐 걱정하는 마음이 함께 있습니다. 돈의 흐름은 감정의 속도에 끌려갈 때 가장 쉽게 흐려집니다.",
                caution = "좋아 보이는 기회일수록 마음이 먼저 앞서갈 수 있습니다. 불안해서 움직이는 선택은 나중에 부담으로 돌아오기 쉽습니다.",
                direction = "이 리포트는 지출 지시보다 돈을 대하는 심리의 흐름을 읽는 데 초점을 둡니다. 지금은 빠른 이익보다 흔들리지 않는 기준이 더 중요한 구간입니다.",
                oneLineAdvice = "재물운은 큰돈보다 기준에서 갈립니다. 기준이 흔들리면 마음까지 같이 흔들릴 수 있습니다.",
                bestMonth = bestMonth,
                riskyMonth = riskyMonth,
                bestMonthReason = monthTone(bestSelection, "돈의 흐름을 차분히 바라보기 쉬운 구간입니다."),
                riskyMonthReason = "$riskyMonth 흐름에서는 빠른 이익에 마음이 쏠리기 쉽습니다."
            )

            PremiumTopic.SELF_ESTEEM -> buildConsultation(
                core = "$coreKeyword 기운이 자기평가의 방식에서 강하게 나타납니다. 약해서 흔들리는 것이 아니라, 스스로를 보는 기준이 너무 엄격해진 상태에 가깝습니다.",
                interpretation = "'$concernText' 안에는 더 나아지고 싶은 마음과 이미 지친 마음이 함께 있습니다. 지금은 자신을 증명하는 문제보다 나를 바라보는 말의 온도를 낮추는 것이 중요합니다.",
                caution = "비교가 깊어지면 실제 능력보다 부족함만 크게 보일 수 있습니다. 그 상태에서는 좋은 기회 앞에서도 자신을 작게 느끼기 쉽습니다.",
                direction = "이 리포트는 자기관리 과제를 늘리기보다 마음이 자신을 평가하는 방식을 읽는 데 초점을 둡니다. 지금의 중심은 증명보다 회복의 결에 가깝습니다.",
                oneLineAdvice = "자존감은 강한 결심보다 나를 바라보는 기준이 부드러워질 때 더 안정됩니다.",
                bestMonth = bestMonth,
                riskyMonth = riskyMonth,
                bestMonthReason = monthTone(bestSelection, "자기평가의 결이 조금 부드러워지기 쉬운 구간입니다."),
                riskyMonthReason = "$riskyMonth 흐름에서는 비교와 조급함이 커질 수 있습니다."
            )

            PremiumTopic.RELATIONSHIP -> buildConsultation(
                core = "$coreKeyword 기운이 인간관계 안에서 강하게 드러납니다. 사람을 많이 만나는 문제보다 누구에게 에너지를 쓰는지가 더 중요해지는 흐름입니다.",
                interpretation = "'$concernText' 안에는 관계를 지키고 싶은 마음과 피로해진 마음이 함께 있습니다. 지금의 핵심은 친절함과 소모감의 경계를 읽는 것입니다.",
                caution = "좋은 사람으로 보이고 싶은 마음이 커지면 불편함을 오래 참게 될 수 있습니다. 그럴수록 관계는 편안함보다 의무감으로 느껴질 수 있습니다.",
                direction = "이 리포트는 관계 숙제를 늘리기보다 사람 사이에서 반복되는 감정의 결을 읽는 데 초점을 둡니다. 지금은 넓히는 흐름보다 가벼워지는 흐름이 더 중요합니다.",
                oneLineAdvice = "인연은 넓히는 힘만큼 가볍게 두는 힘도 있을 때 오래 편안해집니다.",
                bestMonth = bestMonth,
                riskyMonth = riskyMonth,
                bestMonthReason = monthTone(bestSelection, "관계의 온도를 편안하게 느끼기 쉬운 구간입니다."),
                riskyMonthReason = "$riskyMonth 흐름에서는 작은 오해도 크게 느껴질 수 있습니다."
            )
        }
    }

    private fun buildConsultation(
        core: String,
        interpretation: String,
        caution: String,
        direction: String,
        oneLineAdvice: String,
        bestMonth: String,
        riskyMonth: String,
        bestMonthReason: String,
        riskyMonthReason: String
    ): PremiumConsultation {
        return PremiumConsultation(
            core = core,
            interpretation = interpretation,
            caution = caution,
            direction = direction,
            oneLineAdvice = oneLineAdvice,
            bestMonth = bestMonth,
            bestMonthReason = bestMonthReason,
            riskyMonth = riskyMonth,
            riskyMonthReason = riskyMonthReason
        )
    }

    private fun monthTone(selection: PremiumMonthPlanner.MonthSelection, message: String): String {
        val passedMonth = selection.replacedPastMonth
        val prefix = if (passedMonth != null) {
            "올해 가장 강했던 구간은 ${passedMonth}월이었고, 지금 이후에는 ${selection.toDisplayText()} 흐름을 참고할 수 있습니다. "
        } else {
            "${selection.toDisplayText()}에는 "
        }
        return prefix + message
    }
}
