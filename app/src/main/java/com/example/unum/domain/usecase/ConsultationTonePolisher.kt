package com.example.unum.domain.usecase

import com.example.unum.data.model.LifeRecord
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.NumerologyResultBundle

object ConsultationTonePolisher {
    fun polishFreeResult(bundle: NumerologyResultBundle): NumerologyResultBundle {
        val profile = bundle.content.destinyProfile
        val life = bundle.content.lifeRecord
        val destinyTone = toneFor(bundle.numbers.destiny)
        val earlyTone = toneFor(bundle.numbers.early)
        val middleTone = toneFor(bundle.numbers.middle)
        val lateTone = toneFor(bundle.numbers.late)

        return bundle.copy(
            content = NumerologyContent(
                destinyProfile = profile.copy(
                    destinyText = buildDestinyText(bundle.numbers.destiny, destinyTone),
                    oneLineAdvice = buildDestinyAdvice(destinyTone)
                ),
                lifeRecord = life.copy(
                    earlyText = buildStageText(Stage.EARLY, earlyTone, life),
                    middleText = buildStageText(Stage.MIDDLE, middleTone, life),
                    lateText = buildStageText(Stage.LATE, lateTone, life),
                    lifeText = buildLifeText(destinyTone, earlyTone, middleTone, lateTone, life),
                    summaryText = buildSummaryText(destinyTone, earlyTone, middleTone, lateTone, life),
                    oneLineAdvice = buildLifeAdvice(destinyTone, life)
                )
            )
        )
    }

    private fun buildDestinyText(destiny: Int, tone: NumberTone): String {
        return "운명수 ${destiny}의 주요 키워드는 \"${tone.title}\"입니다. " +
            "${tone.strength} ${tone.fit} " +
            "근데 이 장점이 도를 넘으면 ${tone.risk} 쪽으로 금방 기울 수 있어요. " +
            "${tone.warning} 지금부터 각별히 신경 써두는 편이 좋아요."
    }

    private fun buildStageText(stage: Stage, tone: NumberTone, life: LifeRecord): String {
        val caution = buildCautionPhrase(life, tone)
        return when (stage) {
            Stage.EARLY ->
                "인생 초반에는 ${tone.earlyIntro} " +
                    "${tone.early} ${tone.strength} " +
                    "다만 ${caution} 초반부터 마음고생이 길어질 수 있어요. " +
                    "${tone.stageWarning}"
            Stage.MIDDLE ->
                "인생 중반에 접어들면 ${tone.middleIntro} " +
                    "${tone.middle} " +
                    "이때 ${caution} 본인 특성에 의해, 버티는 방식이 잘못돼서 일이 더 힘들어지는 상황이 옵니다. " +
                    "이는 절대 능력이 부족해서가 아닙니다. ${tone.healthAdvice}함께 할 사람과 함께 맞춰갈 자신만의 기준을 미리 정해두는 게 훨씬 낫습니다."
            Stage.LATE ->
                "인생 후반에는 ${tone.lateIntro} " +
                    "${tone.late} " +
                    "어떤 기회가 와도 자신의 명확한 기준이 아닌 애매한 행동을 취하면, 말년에 삶이 이전보다 더 피곤해집니다. " +
                    "남길 것과 끊어낼 것을 천천히, 신중하게 가려내야 합니다."
        }
    }

    private fun buildLifeText(
        destinyTone: NumberTone,
        earlyTone: NumberTone,
        middleTone: NumberTone,
        lateTone: NumberTone,
        life: LifeRecord
    ): String {
        val caution = life.cautionKeywords.take(2).joinToString(", ").ifBlank { destinyTone.caution }
        return "전체적으로는 ${destinyTone.title}을 바탕으로, 초반에는 ${earlyTone.title}, 중반에는 ${middleTone.title}, 후반에는 ${lateTone.title}의 과제가 이어집니다. " +
            "장점은 ${destinyTone.core}에서 분명하게 살아나지만, ${caution}을 방치하면 좋은 재능이 오히려 스스로를 괴롭히는 쪽으로 바뀔 수 있어요. " +
            "잘하는 걸 더 세게 밀어붙이는 것보다, 어디서 멈추고 어디서 선을 그을지 아는 게 훨씬 중요합니다."
    }

    private fun buildSummaryText(
        destinyTone: NumberTone,
        earlyTone: NumberTone,
        middleTone: NumberTone,
        lateTone: NumberTone,
        life: LifeRecord
    ): String {
        val caution = life.cautionKeywords.take(2).joinToString(", ").ifBlank { destinyTone.caution }
        return "${destinyTone.title}을 바탕으로 ${earlyTone.title}, ${middleTone.title}, ${lateTone.title}의 흐름이 이어집니다. " +
            "강점은 ${destinyTone.core} 쪽에서 크게 살아나지만, 주의할 부분은 ${caution}입니다. " +
            "이걸 그냥 넘기면 생각보다 인생이 꽤 피곤해질 수 있습니다."
    }

    private fun buildDestinyAdvice(tone: NumberTone): String {
        return "${tone.adviceStrength} 장점은 살리되, ${tone.adviceCaution} 내려놓으세요. ${tone.adviceRisk}"
    }

    private fun buildLifeAdvice(tone: NumberTone, life: LifeRecord): String {
        return "${tone.adviceStrength} 장점은 살리되, ${tone.adviceCaution} 내려놓으세요. ${tone.adviceRisk}"
    }

    private fun buildCautionPhrase(life: LifeRecord, tone: NumberTone): String {
        val first = life.cautionKeywords.getOrNull(0)?.toNaturalCaution() ?: tone.caution
        val second = life.cautionKeywords.getOrNull(1)?.toNaturalCaution()
        return if (second.isNullOrBlank()) {
            "$first 가볍게 보면"
        } else {
            "$first 가볍게 보거나 $second 그냥 넘기면"
        }
    }

    private fun String.toNaturalCaution(): String {
        return when (this) {
            "관계 소모" -> "관계에서 소모되는 걸"
            "건강 주의" -> "건강 신호를"
            "눈치 과다" -> "눈치를 너무 보는 걸"
            "정서 소진" -> "마음이 소진되는 걸"
            "방향 상실" -> "방향을 잃는 걸"
            "평판 의존" -> "평판에 기대는 걸"
            "경계선 약화" -> "경계선이 흐려지는 걸"
            "무기력" -> "무기력을"
            "우유부단" -> "우유부단함을"
            "조급함" -> "조급함을"
            "고립" -> "고립되는 걸"
            "속앓이" -> "속앓이를"
            "말의 과속" -> "말이 앞서는 걸"
            "산만함" -> "산만함을"
            "고집" -> "고집을"
            "무리수" -> "무리수를"
            "자기희생" -> "자기희생을"
            "집착" -> "집착을"
            "번아웃" -> "번아웃을"
            "욕심" -> "욕심을"
            "과열" -> "과열을"
            else -> "${this}을"
        }
    }

    private fun toneFor(number: Int): NumberTone = when (number) {
        0 -> NumberTone(
            title = "가능성",
            core = "유연함과 판을 바꾸는 감각",
            caution = "우유부단",
            strength = "무엇이든 새로 시작할 수 있는 폭이 넓고, 막힌 상황에서도 다른 길을 찾아내는 감각이 있어요.",
            fit = "정해진 틀 안에서만 움직이기보다 상황을 보면서 방향을 유연하게 바꾸는 쪽이 훨씬 잘 맞는 사람입니다.",
            risk = "방향을 잃거나 쉽게 무기력해지는",
            warning = "가능성이 많다는 사실에 취해 결정을 계속 미루다 보면, 결국 아무것도 제대로 잡지 못한 채 삶이 더 답답해질 수 있습니다.",
            earlyIntro = "가능성이 여러 방향으로 열리는 시기입니다.",
            middleIntro = "여러 가능성 중에서 자신의 기준을 골라야 하는 일이 중요한 선택으로 다가옵니다.",
            lateIntro = "열어두었던 가능성들의 결과가 선명하게 드러납니다.",
            early = "움직임이 많고 환경 변화도 잦을 수 있어요.",
            middle = "선택해야 할 상황이 많아지고, 내 의견을 계속 숨기면 나중에 손해가 커질 수 있어요.",
            late = "표현력은 끝까지 강점이 되지만, 말의 온도를 놓치는 순간 평판이 안 좋아질 수 있어요.",
            stageWarning = "충분히 생각해 보지 않고 결정하는 선택은 기회가 아니라 본인 부담을 늘리는 결과로 이어질 수 있습니다.",
            adviceStrength = "가능성을 넓히는",
            adviceCaution = "결정을 미루는 습관은",
            adviceRisk = "너무 오래 미루다 보면 좋은 가능성까지 스스로 막아버리는 결과가 됩니다."
        )
        1 -> NumberTone(
            title = "개척",
            core = "시작과 결단력",
            caution = "조급함",
            strength = "남이 만든 길을 따라가기보다 직접 길을 열어가는 힘이 강합니다.",
            fit = "가만히 기다리는 것보다 먼저 움직이고 판을 만드는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "조급함과 고립",
            warning = "혼자 빨리 가겠다는 마음이 커지면, 능력은 좋은데 주변을 잃고 일이 더 버거워질 수 있습니다.",
            earlyIntro = "개척의 기운이 먼저 치고 올라오는 시기입니다.",
            middleIntro = "개척해야 할 일이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "개척해온 것들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 자기 뜻대로 밀고 가고 싶은 마음이 강해질 수 있습니다.",
            middle = "결정해야 할 일이 많아지고 리더 역할도 커지기 쉬운 시기입니다.",
            late = "스스로 만든 기준이 힘이 되지만, 고집으로 굳으면 관계가 불편해질 수 있습니다.",
            stageWarning = "급하게 결론을 내리면 시작은 빠른데 뒷감당이 힘들어질 수 있습니다.",
            adviceStrength = "시작하고 밀고 나가는",
            adviceCaution = "조급함은",
            adviceRisk = "본인의 너무 빠른 판단은 주변을 피곤하게 만들고 결국 고립으로 이어질 수 있습니다."
        )
        2 -> NumberTone(
            title = "조화",
            core = "관계 감각과 섬세함",
            caution = "속앓이",
            strength = "사람의 감정과 분위기를 잘 읽고, 어긋난 관계를 부드럽게 맞추는 힘이 있습니다.",
            fit = "혼자 밀어붙이기보다 상대와 온도를 맞추며 풀어가는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "눈치 과다와 정서 소진",
            warning = "계속 참고 맞춰주기만 하면, 착한 사람이 아니라 제일 먼저 지치는 사람이 될 수 있습니다.",
            earlyIntro = "관계의 기운이 먼저 예민하게 살아나는 시기입니다.",
            middleIntro = "여러 관계 속에서 균형을 맞추는 일이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "맞춰온 관계들의 결과가 더 선명하게 드러납니다.",
            early = "초반부터 주변 분위기에 영향을 많이 받기 쉽습니다.",
            middle = "관계 속에서 선택해야 할 일이 많아지고, 내 의견을 숨기면 손해가 커질 수 있습니다.",
            late = "좋은 사람은 오래 남지만, 불편한 관계까지 붙잡으면 마음이 피곤해질 수 있습니다.",
            stageWarning = "상대 기분만 보다가 내 기준을 잃으면 관계가 더 힘들어질 수 있습니다.",
            adviceStrength = "관계를 살피는",
            adviceCaution = "속마음을 숨기는 습관은",
            adviceRisk = "속마음을 묻어두는 버릇은 결국 본인만 병들게 만들 수 있습니다."
        )
        3 -> NumberTone(
            title = "표현",
            core = "말과 해석력",
            caution = "말의 과속",
            strength = "생각을 말로 꺼내고 분위기를 바꾸는 힘이 좋습니다.",
            fit = "아이디어를 숨기기보다 표현하고 설득하고 풀어내는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "감정 과열과 오해",
            warning = "말이 빨라지면 의도와 다르게 전달돼서, 괜히 관계나 일이 꼬일 수 있습니다.",
            earlyIntro = "표현의 기운이 먼저 살아나는 시기입니다.",
            middleIntro = "생각을 드러내고 설득해야 하는 일이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "그동안 표현해온 것들의 결과가 선명하게 드러납니다.",
            early = "초반부터 말과 표현으로 기회를 만들기 쉽습니다.",
            middle = "생각을 드러낼 일이 많아지지만, 정리 없이 말하면 손해가 생길 수 있습니다.",
            late = "표현력은 끝까지 장점이 되지만, 말의 온도를 놓치면 평판이 흔들릴 수 있습니다.",
            stageWarning = "하고 싶은 말을 다 하는 것보다, 어떤 순서로 말할지 정하는 게 중요합니다.",
            adviceStrength = "표현하는",
            adviceCaution = "말이 앞서는 습관은",
            adviceRisk = "말이 앞서는 습관은 좋은 의도까지 가볍게 보이게 만들 수 있습니다."
        )
        4 -> NumberTone(
            title = "기반",
            core = "기준과 구조",
            caution = "고집",
            strength = "흔들리는 상황에서도 기준을 세우고 오래 갈 기반을 만드는 힘이 있습니다.",
            fit = "즉흥적으로 넓히기보다 차근차근 쌓고 검증하는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "경직성과 강박",
            warning = "기준이 너무 세지면 안정이 아니라 답답함이 되고, 주변 사람까지 숨 막히게 만들 수 있습니다.",
            earlyIntro = "기반을 잡으려는 마음이 먼저 강해지는 시기입니다.",
            middleIntro = "현실적인 기준과 책임이 훨씬 중요하게 다가옵니다.",
            lateIntro = "쌓아온 기준들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 규칙과 안정감을 찾으려는 성향이 강합니다.",
            middle = "현실적인 책임과 실무 기준이 커지는 시기입니다.",
            late = "쌓아둔 기반은 힘이 되지만, 변화에 닫히면 삶이 답답해질 수 있습니다.",
            stageWarning = "원칙만 붙잡으면 좋은 기회도 불편한 변수로만 보일 수 있습니다.",
            adviceStrength = "기반을 다지는",
            adviceCaution = "지나친 고집은",
            adviceRisk = "지나친 고집은 본인 인생을 안정시키는 게 아니라 좁게 만들 수 있습니다."
        )
        5 -> NumberTone(
            title = "변화",
            core = "기회 감각과 활동성",
            caution = "무리수",
            strength = "새로운 상황에 빠르게 반응하고 기회를 잡는 감각이 좋습니다.",
            fit = "한자리에 오래 묶이기보다 움직이고 바꾸고 넓히는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "과신과 산만함",
            warning = "재밌어 보인다고 다 건드리면, 시작은 많은데 남는 게 없어 인생이 더 정신없어질 수 있습니다.",
            earlyIntro = "변화가 많은 시기입니다.",
            middleIntro = "변화와 선택이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "바꿔온 것들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 움직임이 많고 환경 변화도 잦을 수 있습니다.",
            middle = "기회가 많아지는 만큼 선택 실수도 커질 수 있는 시기입니다.",
            late = "변화 대응력은 장점이지만, 정착할 기준이 없으면 계속 불안해질 수 있습니다.",
            stageWarning = "검토 없이 뛰어들면 기회가 아니라 부담을 늘리는 선택이 될 수 있습니다.",
            adviceStrength = "변화를 기회로 바꾸는",
            adviceCaution = "무리수는",
            adviceRisk = "무리하게 벌이는 습관은 좋은 운까지 소모품처럼 써버릴 수 있습니다."
        )
        6 -> NumberTone(
            title = "책임",
            core = "성실함과 생활력",
            caution = "자기희생",
            strength = "맡은 일을 끝까지 끌고 가고, 주변을 챙기는 힘이 남다릅니다.",
            fit = "가볍게 스쳐 지나가는 일보다 책임지고 오래 관리하는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "걱정 과다와 건강 저하",
            warning = "모든 걸 혼자 떠안으면 몸과 마음이 같이 무거워지고, 건강까지 안 좋아질 수 있습니다.",
            earlyIntro = "책임감이 일찍부터 강하게 드러나는 시기입니다.",
            middleIntro = "책임감이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "책임져온 것들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 남들보다 책임을 빨리 느끼기 쉽습니다.",
            middle = "가족, 일, 돈처럼 현실 책임이 커지며 체력 관리가 중요해지는 시기입니다.",
            late = "성실함은 크게 남지만, 자기희생을 계속하면 편해야 할 때도 쉬지 못할 수 있습니다.",
            stageWarning = "책임을 나누지 않으면 잘해놓고도 본인만 지치는 상황이 반복될 수 있습니다.",
            healthAdvice = "특히 건강관리가 가장 중요한 시기입니다. 이 시기는 자신의 몸 상태를 항상 1순위로 생각하며 생활해야 합니다. 꾸준한 건강검진을 권장합니다. ",
            adviceStrength = "책임지고 끝까지 해내는",
            adviceCaution = "자기희생은",
            adviceRisk = "과한 책임감은 건강과 마음을 같이 무너뜨릴 수 있으니 각별히 조심해야 합니다."
        )
        7 -> NumberTone(
            title = "집중",
            core = "몰입과 추진력",
            caution = "집착",
            strength = "한곳에 깊이 파고드는 힘이 남다릅니다.",
            fit = "이것저것 넓히는 것보다 한 가지 일에서 제대로 된 결과를 만들어내는 쪽이 훨씬 맞는 사람이에요.",
            risk = "예민함과 번아웃",
            warning = "집착이나 번아웃을 그냥 넘기는 버릇이 생기면 생각보다 인생이 꽤 피곤해질 수 있어요.",
            earlyIntro = "집중의 기운이 먼저 강하게 드러나는 시기입니다.",
            middleIntro = "결과를 내야 한다는 압박이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "몰입해온 것들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 꽂히는 대상이 생기면 빠르게 몰입하는 편입니다.",
            middle = "결과를 내야 한다는 압박이 커지고, 혼자 버티려는 태도도 강해질 수 있습니다.",
            late = "깊이 파온 실력은 남지만, 집착을 못 버리면 편해질 시기에도 마음이 계속 날카로워질 수 있습니다.",
            stageWarning = "몰입이 장점이어도 멈출 줄 모르면 번아웃이 따라오기 마련입니다.",
            adviceStrength = "집중해서 결과를 만드는",
            adviceCaution = "집착은",
            adviceRisk = "본인의 너무 과한 미련은 세상을 필요 이상으로 부정적으로 보게 만들 수 있습니다."
        )
        8 -> NumberTone(
            title = "인연",
            core = "사람을 끌어오는 힘과 기회 감각",
            caution = "욕심",
            strength = "사람도 기회도 자연스럽게 끌어당기는 힘이 있어요.",
            fit = "혼자 틀어박혀 있기보다 사람들 속에서 기회를 만들고 넓혀가는 쪽이 훨씬 잘 맞는 사람입니다.",
            risk = "관계 소모와 평판 의존",
            warning = "모든 사람과 모든 기회를 다 붙잡으려 하면, 결국 본인 에너지가 먼저 바닥납니다.",
            earlyIntro = "사람과 기회가 함께 넓어지는 시기입니다.",
            middleIntro = "인연과 기회를 어떻게 감당할지가 훨씬 현실적으로 다가옵니다.",
            lateIntro = "맺어온 인연과 넓혀온 기회들의 장단점이 더 선명하게 드러납니다.",
            early = "사람도, 기회도 한꺼번에 몰려드는 시기라 활동 범위가 자연스럽게 넓어집니다.",
            middle = "관계 속에서 돈, 일, 평판 문제가 같이 커질 수 있는 시기입니다.",
            late = "좋은 인연은 힘이 되지만, 관계가 많을수록 피로도 커질 수 있습니다.",
            stageWarning = "결론을 급하게 내리면 시작은 빠른데 뒷감당이 힘들어지는 경우가 많습니다.",
            adviceStrength = "인연을 끌어오는",
            adviceCaution = "욕심은",
            adviceRisk = "과한 욕심은 좋은 인연마저 부담스러운 관계로 바꿔버릴 수 있습니다."
        )
        else -> NumberTone(
            title = "완성",
            core = "마무리와 완성력",
            caution = "과열",
            strength = "일을 끝까지 끌고 가서 결과를 남기는 힘이 강합니다.",
            fit = "대충 시작하고 흐지부지 끝내기보다 의미 있는 결론을 만드는 쪽이 훨씬 잘 맞는 사람이에요.",
            risk = "소진과 완벽 강박",
            warning = "끝까지 해내야 한다는 마음이 지나치면, 성과는 남아도 본인은 완전히 지칠 수 있습니다.",
            earlyIntro = "완성하려는 마음이 먼저 강하게 드러나는 시기입니다.",
            middleIntro = "결과를 내야 하는 일이 훨씬 현실적으로 다가옵니다.",
            lateIntro = "마무리해온 것들의 장단점이 더 선명하게 드러납니다.",
            early = "초반부터 기대치가 높고 결과를 의식하기 쉽습니다.",
            middle = "완성해야 할 일이 많아지고 책임감도 강해지는 시기입니다.",
            late = "정리하고 남기는 힘은 좋지만, 끝맺음에 집착하면 새로운 시작이 늦어질 수 있습니다.",
            stageWarning = "완벽하게 끝내려다 중요한 기회까지 놓칠 수 있습니다.",
            adviceStrength = "끝까지 완성하는",
            adviceCaution = "완벽 강박은",
            adviceRisk = "완벽 강박은 성과를 키우는 척하다가 결국 본인을 소진시킬 수 있습니다."
        )
    }

    private enum class Stage {
        EARLY,
        MIDDLE,
        LATE
    }

    private data class NumberTone(
        val title: String,
        val core: String,
        val caution: String,
        val strength: String,
        val fit: String,
        val risk: String,
        val warning: String,
        val earlyIntro: String,
        val middleIntro: String,
        val lateIntro: String,
        val early: String,
        val middle: String,
        val late: String,
        val stageWarning: String,
        val healthAdvice: String = "",
        val adviceStrength: String,
        val adviceCaution: String,
        val adviceRisk: String
    )
}
