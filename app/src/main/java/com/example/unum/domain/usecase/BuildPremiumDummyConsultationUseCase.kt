package com.example.unum.domain.usecase

import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic

class BuildPremiumDummyConsultationUseCase {
    operator fun invoke(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): PremiumConsultation {
        val concernText = concern.ifBlank { "지금 마음속에서 가장 자주 떠오르는 고민" }
        val destiny = bundle.content.destinyProfile
        val coreKeyword = destiny.coreKeywords.firstOrNull() ?: destiny.title
        val bestSelection = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers.destiny)
        val riskySelection = PremiumMonthPlanner.pickRiskyMonth(topic, bundle.numbers.destiny)
        val bestMonth = bestSelection.toDisplayText()
        val riskyMonth = riskySelection.toDisplayText()

        return when (topic) {
            PremiumTopic.ROMANCE -> PremiumConsultation(
                core = "$coreKeyword 쪽이 강한 만큼 마음이 꽂히면 깊어지는 편입니다. 지금 연애 고민은 마음이 있느냐 없느냐보다, 감정이 올라왔을 때 관계를 너무 빨리 몰아붙이는 패턴이 핵심입니다.",
                interpretation = "'$concernText' 상황에서는 세 장면을 특히 보셔야 합니다. 답장이 늦을 때 혼자 결론을 내리는 장면, 애매한 말투 하나에 마음이 출렁이는 장면, 상대가 다가오기도 전에 관계 이름부터 확인하고 싶어지는 장면입니다. 진심이 깊어질수록 확인하고 싶은 마음도 커지지만, 상대 입장에서는 감정 검사를 받는 느낌이 들 수 있습니다. 그러면 사랑이 부족해서가 아니라 압박감 때문에 관계가 식을 수 있습니다.",
                caution = "관계를 확인하고 싶은 마음을 계속 밀어붙이면 상대가 방어적으로 변하고, 당신은 더 불안해지는 악순환이 생길 수 있습니다. 특히 밤늦게 긴 메시지를 보내거나, 답을 듣기 전까지 계속 분위기를 떠보는 습관은 각별히 조심해야 합니다. 방치하면 인연이 끊긴다기보다, 좋은 인연마저 피곤한 사람처럼 느껴질 수 있습니다.",
                direction = "오늘은 결론을 묻기보다 가벼운 안부 하나로 온도를 확인하세요. 이번 주에는 상대 반응을 해석하기 전에 실제 행동 3가지만 적어보세요. 한 달 안에는 연락 속도, 만남 빈도, 감정 표현 방식 중 하나를 자연스럽게 맞춰보는 게 좋습니다. 이 순서를 건너뛰면 관계가 깊어지기 전에 먼저 지칠 수 있습니다.",
                oneLineAdvice = "사랑은 확인을 세게 할수록 선명해지는 게 아니라, 부담을 줄일수록 오래 갈 수 있습니다.",
                bestMonth = bestMonth,
                bestMonthReason = bestMonthLead(bestSelection) + "마음을 새롭게 여는 흐름이 좋습니다. 고백이나 관계 정리는 무겁게 던지기보다, 같이 시간을 보내는 구체적인 제안으로 시작하면 흐름이 훨씬 부드럽습니다.",
                riskyMonth = riskyMonth,
                riskyMonthReason = "${riskyMonth}에는 감정이 앞서 결론을 재촉하기 쉽습니다. 이때 확인 욕구를 그대로 밀어붙이면 상대가 뒤로 물러나 관계가 더 힘들어질 수 있으니, 답을 듣기 전 하루 정도 여백을 두는 편이 낫습니다."
            )
            PremiumTopic.CAREER -> PremiumConsultation(
                core = "$coreKeyword 쪽으로 힘이 붙을 때 성과가 잘 나는 편입니다. 지금 일과 진로 고민은 능력이 부족해서가 아니라, 버티는 구조가 맞지 않아 피로가 커지는 쪽에 가깝습니다.",
                interpretation = "'$concernText' 고민에서는 회의에서 할 말을 삼키는 장면, 일이 몰려도 혼자 책임지는 장면, 퇴사나 이직을 갑자기 확정하고 싶어지는 장면을 보셔야 합니다. 지금은 방향과 방식이 어긋나면 성과보다 소진이 먼저 올 수 있습니다. 특히 남들은 괜찮아 보이는데 나만 지친다고 느끼는 순간이 반복되면, 그건 게으름이 아니라 구조가 몸에 안 맞는 신호일 수 있습니다.",
                caution = "지금 피로를 대충 넘기면 일의 문제가 아니라 생활 리듬과 건강까지 같이 무너질 수 있습니다. 성급한 퇴사도 위험하지만, 아무 기준 없이 계속 참는 것도 위험합니다. 둘 다 방치하면 커리어가 좋아지는 게 아니라 버티는 인생이 되어버릴 수 있으니 각별히 조심해야 합니다.",
                direction = "오늘은 지금 일에서 에너지를 빼앗는 요소 3개를 적으세요. 이번 주에는 줄일 일, 넘길 일, 반드시 잡을 일을 나누세요. 한 달 안에는 이직이든 유지든 판단 기준을 숫자로 정하는 게 좋습니다. 감정으로만 움직이면 후련함은 짧고 뒷감당은 길어질 수 있습니다.",
                oneLineAdvice = "진로는 오래 갈 구조를 못 잡으면 능력이 있어도 삶이 빡빡해질 수 있습니다.",
                bestMonth = bestMonth,
                bestMonthReason = bestMonthLead(bestSelection) + "준비한 것을 실제 제안, 지원, 면담으로 옮기기 좋습니다. 포트폴리오나 조건 협상처럼 손에 잡히는 행동을 만들면 기회가 더 선명해집니다.",
                riskyMonth = riskyMonth,
                riskyMonthReason = "${riskyMonth}에는 변화 욕구가 커져 성급한 결정을 내리기 쉽습니다. 홧김에 퇴사하거나 검증 안 된 제안을 잡으면 커리어가 예상보다 더 힘들어질 수 있습니다."
            )
            PremiumTopic.MONEY -> PremiumConsultation(
                core = "$coreKeyword 감각은 기회를 잡는 데 도움이 되지만, 기준이 흔들리면 돈의 흐름도 같이 흔들릴 수 있습니다. 지금 돈 고민은 수입의 크기보다 돈을 다루는 기준이 흔들리는 데 핵심이 있습니다.",
                interpretation = "'$concernText' 상황에서는 갑자기 좋아 보이는 투자 제안, 스트레스를 풀기 위한 충동 지출, 주변 사람 말에 흔들려 돈을 움직이는 장면을 조심해야 합니다. 마음이 불안할수록 돈으로 해결하려는 선택이 나올 수 있습니다. 이때 기준 없이 움직이면 한 번의 지출이 아니라 생활 전체의 압박으로 돌아옵니다.",
                caution = "돈 문제를 감으로만 처리하면 생각보다 빨리 삶이 답답해질 수 있습니다. 특히 확인되지 않은 투자, 빌려주고 말 못 하는 돈, 할부로 미루는 소비를 가볍게 보면 나중에 선택지가 줄어듭니다. 돈이 없어서 힘든 게 아니라 돈의 출구가 많아서 힘들어질 수 있으니 각별히 조심해야 합니다.",
                direction = "오늘은 고정비, 충동 지출, 회복 자금을 따로 적으세요. 이번 주에는 당장 끊을 지출 하나를 정하고, 한 달 안에는 안전 자금과 도전 자금을 분리하세요. 이 구분을 미루면 좋은 기회가 와도 불안해서 제대로 잡지 못할 수 있습니다.",
                oneLineAdvice = "재물운은 큰돈보다 기준에서 갈립니다. 기준 없이 움직이면 돈 때문에 마음까지 흔들릴 수 있습니다.",
                bestMonth = bestMonth,
                bestMonthReason = bestMonthLead(bestSelection) + "수입과 지출 구조를 다시 잡기 좋습니다. 투자보다 정리, 확장보다 기준을 먼저 세우면 돈의 흐름이 더 안정됩니다.",
                riskyMonth = riskyMonth,
                riskyMonthReason = "${riskyMonth}에는 빠른 이익을 좇는 마음이 커질 수 있습니다. 확인 안 된 제안이나 충동 구매를 가볍게 보면 돈의 흐름이 한 번에 꼬일 수 있습니다."
            )
            PremiumTopic.SELF_ESTEEM -> PremiumConsultation(
                core = "$coreKeyword 쪽이 살아나면 스스로를 세우는 힘이 분명해집니다. 지금 자존감 고민은 당신이 약해서가 아니라, 스스로를 평가하는 기준이 너무 거칠어진 데서 시작됐을 가능성이 큽니다.",
                interpretation = "'$concernText' 상황에서는 SNS를 보고 비교하는 장면, 한 번의 실수를 오래 곱씹는 장면, 쉬어야 할 때도 뭔가 증명하려는 장면이 반복되기 쉽습니다. 지금은 마음의 체력을 먼저 회복해야 하는데, 계속 자신을 몰아세우면 성과가 아니라 무기력만 커질 수 있습니다. 자존감은 기분 문제가 아니라 일상 루틴의 문제로 봐야 합니다.",
                caution = "비교와 자기비난을 방치하면 마음만 힘든 게 아니라 몸의 리듬까지 무너질 수 있습니다. 잠, 식사, 운동 같은 기본 리듬이 흔들리면 판단도 더 어두워지고, 결국 좋은 기회 앞에서도 스스로를 못 믿게 될 수 있습니다. 이 흐름은 각별히 조심해야 합니다.",
                direction = "오늘은 남에게 보여줄 목표가 아니라 내가 지킬 수 있는 약속 하나만 정하세요. 이번 주에는 비교를 부르는 환경을 줄이고, 한 달 안에는 작은 성취를 기록하는 루틴을 만드세요. 거창한 변화보다 반복 가능한 약속이 지금의 중심을 살립니다.",
                oneLineAdvice = "자존감은 생각으로만 버티면 더 흔들릴 수 있으니, 몸과 루틴부터 다시 잡아야 합니다.",
                bestMonth = bestMonth,
                bestMonthReason = bestMonthLead(bestSelection) + "스스로를 다시 세우는 흐름이 좋습니다. 새로운 목표보다 작은 약속을 지키는 경험을 쌓으면 마음의 중심이 살아납니다.",
                riskyMonth = riskyMonth,
                riskyMonthReason = "${riskyMonth}에는 비교와 조급함이 커질 수 있습니다. 무리해서 증명하려 들면 컨디션과 자존감이 같이 떨어질 수 있으니 각별히 쉬는 리듬을 챙겨야 합니다."
            )
            PremiumTopic.RELATIONSHIP -> PremiumConsultation(
                core = "$coreKeyword 쪽이 강할수록 사람과의 접점에서 기회도 피로도 같이 커질 수 있습니다. 지금 인간관계 고민은 사람이 많고 적음의 문제가 아니라, 누구에게 에너지를 써야 하는지 기준이 흐려진 데 핵심이 있습니다.",
                interpretation = "'$concernText' 상황에서는 부탁을 거절 못 하는 장면, 단체 대화방에서 눈치를 보는 장면, 이미 불편한 사람에게 또 맞춰주는 장면이 반복될 수 있습니다. 관계는 당신에게 기회이기도 하지만, 기준 없이 열어두면 가장 빠르게 체력을 빼앗는 통로가 됩니다. 모두에게 좋은 사람으로 남으려다 정작 내 생활이 무너질 수 있습니다.",
                caution = "애매한 관계를 계속 방치하면 좋은 인연까지 피곤하게 느껴질 수 있습니다. 특히 상대가 선을 넘었는데도 웃고 넘기는 습관은 나중에 더 큰 거리감으로 돌아옵니다. 관계 때문에 삶이 힘들어지는 사람은 대개 큰 사건보다 작은 불편함을 오래 참다가 무너집니다.",
                direction = "오늘은 편한 사람, 피곤한 사람, 거리를 둬야 할 사람을 나눠보세요. 이번 주에는 부탁 하나를 부드럽게 거절해보고, 한 달 안에는 자주 만나는 사람의 기준을 다시 정리하세요. 선을 세우지 않으면 관계가 아니라 감정 노동이 늘어납니다.",
                oneLineAdvice = "인연은 넓히는 것보다 가려두는 힘이 없으면 결국 삶이 더 피곤해질 수 있습니다.",
                bestMonth = bestMonth,
                bestMonthReason = bestMonthLead(bestSelection) + "관계의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 새 만남을 가볍게 시작하면 좋은 흐름을 만들 수 있습니다.",
                riskyMonth = riskyMonth,
                riskyMonthReason = "${riskyMonth}에는 사람 사이의 오해가 커지기 쉽습니다. 단정적인 말이나 무리한 맞춤을 계속하면 관계가 생각보다 차갑게 틀어질 수 있습니다."
            )
        }
    }

    private fun bestMonthLead(selection: PremiumMonthPlanner.MonthSelection): String {
        val displayMonth = selection.toDisplayText()
        val passedMonth = selection.replacedPastMonth
        return if (passedMonth != null) {
            "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${displayMonth}을 추천 구간으로 보고, "
        } else {
            "${displayMonth}에는 "
        }
    }
}
