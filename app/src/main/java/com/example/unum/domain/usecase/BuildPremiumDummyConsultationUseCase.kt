package com.example.unum.domain.usecase

import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic

class BuildPremiumDummyConsultationUseCase {
    operator fun invoke(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): PremiumConsultation {
        val concernText = concern.ifBlank { "지금 마음속에서 가장 자주 떠오르는 고민" }
        val life = bundle.content.lifeRecord
        val destiny = bundle.content.destinyProfile

        return when (topic) {
            PremiumTopic.ROMANCE -> PremiumConsultation(
                core = "지금 고민의 핵심은 감정 자체보다 관계의 속도와 거리감을 어떻게 조절할지에 있다. ${destiny.title}의 결을 가진 사람일수록 진심이 생긴 뒤에는 마음의 방향이 빨라지기 쉬워, 상대의 반응이 느리게 느껴질 수 있다.",
                interpretation = "숫자 흐름으로 보면 당신의 기본 성향은 ${destiny.title}이고, 현재 인생 조합 ${life.code}는 관계에서 감정의 뿌리와 현실적 선택이 함께 흔들릴 수 있는 흐름을 만든다. '$concernText'도 결국 상대를 바꾸는 문제보다 내 감정의 리듬을 안정시키는 문제와 연결된다.",
                caution = "확신이 부족할 때 상대의 말보다 침묵을 더 크게 해석하면 스스로 지치기 쉽다. 지금은 결론을 재촉하는 태도를 줄이는 편이 좋다.",
                direction = "분위기를 확인할 수 있는 가벼운 대화부터 다시 여는 것이 좋다. 관계의 답은 압박보다 일관된 태도에서 더 잘 드러난다.",
                oneLineAdvice = "사랑은 정답을 서두를수록 흐려지고, 리듬을 지킬수록 선명해진다."
            )
            PremiumTopic.CAREER -> PremiumConsultation(
                core = "지금 고민의 핵심은 무엇을 선택할지보다 어떤 방식으로 오래 갈 수 있을지에 있다. ${destiny.title}의 성향이 강할수록 방향이 맞는 순간 몰입은 깊지만, 맞지 않는 구조에서는 피로가 더 빨리 온다.",
                interpretation = "인생 코드 ${life.code}는 초년의 감정 습관, 중년의 현실 판단, 말년의 정리 방식이 서로 이어지며 진로의 리듬을 만든다. '$concernText' 같은 고민도 결국 능력 부족보다 구조와 방향 정렬의 문제일 가능성이 크다.",
                caution = "지금 당장 결론 하나를 확정하려고 하면 조급함이 커질 수 있다. 특히 타인의 속도를 자신의 기준으로 삼지 않는 것이 중요하다.",
                direction = "3개월 단위로 실험 가능한 목표를 정하고, 잘하는 일·버틸 수 있는 일·계속 배우고 싶은 일을 따로 적어보는 것이 좋다.",
                oneLineAdvice = "진로는 한 번에 맞히는 답이 아니라, 맞는 방향을 더 선명하게 만드는 과정이다."
            )
            PremiumTopic.MONEY -> PremiumConsultation(
                core = "지금 고민의 핵심은 돈의 양보다 돈을 다루는 패턴에 있다. 같은 기회도 어떤 기준으로 움직이느냐에 따라 완전히 다른 결과를 만든다.",
                interpretation = "${life.code}의 흐름은 감정, 선택, 정리의 순서가 재물운에도 그대로 스며든다. '$concernText'라는 문제도 결국 감각만 믿을지, 기준과 구조를 세울지의 문제로 이어진다.",
                caution = "불안할수록 한 번에 만회하려는 판단, 들뜰수록 검증 없이 뛰어드는 판단을 조심하는 편이 좋다.",
                direction = "생활 자금, 안전 자금, 도전 자금을 나눠 보는 것부터 시작하면 재물 흐름이 훨씬 안정된다.",
                oneLineAdvice = "재물운은 큰 기회를 맞히는 힘보다 흐름을 지키는 구조에서 커진다."
            )
            PremiumTopic.SELF_ESTEEM -> PremiumConsultation(
                core = "지금 고민의 핵심은 부족함보다 기준이 흔들린 데서 오는 피로일 수 있다. ${destiny.title}의 사람은 자기 기준이 선명할수록 강해지고, 그 기준이 흐릴수록 쉽게 소진된다.",
                interpretation = "인생 코드 ${life.code}는 시기마다 감정의 뿌리와 현실의 선택이 다른 방식으로 작동한다. '$concernText'가 반복된다면, 외부 평가보다 자신의 루틴과 기준을 회복하는 쪽이 먼저다.",
                caution = "한 번의 실패나 흔들림으로 자기 전체를 평가하지 않는 것이 중요하다. 자존감은 기분보다 반복되는 태도에서 더 안정적으로 회복된다.",
                direction = "하루에 꼭 지킬 수 있는 작은 약속 하나를 정하고, 그것을 꾸준히 지키는 경험을 쌓아 보자.",
                oneLineAdvice = "자존감은 거대한 확신이 아니라, 나를 계속 버리지 않는 습관에서 자란다."
            )
            PremiumTopic.RELATIONSHIP -> PremiumConsultation(
                core = "지금 고민의 핵심은 사람을 많이 만나는가보다, 어떤 관계가 나를 편안하게 만드는가에 있다. ${destiny.title}의 흐름은 가까운 관계에서 더 크게 체감되기 쉽다.",
                interpretation = "${life.code}의 인생 흐름에서는 초년의 애착 습관, 중년의 협업 방식, 말년의 정리 태도가 인간관계 전반에 연결된다. '$concernText'도 결국 특정 사람의 문제이기보다 관계 패턴의 문제일 가능성이 있다.",
                caution = "모든 사람을 다 끌어안으려 하면 스스로가 먼저 지친다. 애매한 관계를 오래 끌기보다 기준을 세우는 태도가 필요하다.",
                direction = "편안해지는 사람과의 접점을 깊게 만들고, 불편한 관계에서는 거리 조절을 죄책감 없이 연습하는 것이 좋다.",
                oneLineAdvice = "좋은 관계는 많음보다 신뢰와 편안함의 밀도에서 결정된다."
            )
        }
    }
}
