package com.example.unum.domain.usecase

import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.SuriSpeechSceneState
import com.example.unum.data.model.SuriSpeechScript
import com.example.unum.data.model.SuriSpeechSegment

class BuildSuriSpeechScriptUseCase {

    fun buildPersonalPreview(
        bundle: NumerologyResultBundle,
        topic: PremiumTopic,
        concern: String
    ): SuriSpeechScript {
        val concernText = concern.takeIf { it.isNotBlank() }
        val profile = bundle.content.destinyProfile
        val life = bundle.content.lifeRecord

        return SuriSpeechScript(
            scriptId = "personal-preview-${bundle.numbers.code}-${topic.name}",
            title = "수리 상담 미리 듣기",
            subtitle = "수리가 조금 더 신비롭고 부드러운 말투로 먼저 읽어드릴게요.",
            segments = listOfNotNull(
                segment(
                    key = "greeting",
                    chip = "점사 시작",
                    title = "손님, 먼저 큰 흐름부터 가만히 보겠습니다",
                    body = "어서 오세요, 손님. 지금은 답을 서둘러 정하기보다, 손님 곁으로 어떤 결이 조용히 들어와 있는지부터 살펴보는 편이 좋겠습니다.",
                    state = SuriSpeechSceneState.GREETING
                ),
                segment(
                    key = "profile",
                    chip = "기본 기운",
                    title = "${profile.title}의 기운이 은은하게 바탕에 깔려 있습니다",
                    body = spokenBody(
                        lead = "가만히 들여다보면, 손님께서는 기본적으로 ${profile.title}의 결을 타고 계십니다",
                        text = profile.destinyText
                    ),
                    state = SuriSpeechSceneState.EXPLAIN
                ),
                concernText?.let {
                    segment(
                        key = "concern",
                        chip = topic.label,
                        title = "마음에 걸린 질문도 함께 짚어보겠습니다",
                        body = "요즘 손님 마음에 오래 머무는 질문은 \"$it\" 쪽으로 읽힙니다. 이 고민은 스치듯 지나가는 것이 아니라, 지금 흐름하고 맞닿아 있어서 조금 더 깊게 살펴보는 편이 좋겠습니다.",
                        state = SuriSpeechSceneState.FOCUS
                    )
                },
                segment(
                    key = "summary",
                    chip = "흐름 요약",
                    title = "지금 보이는 큰 줄기는 이렇습니다",
                    body = spokenBody(
                        lead = "짧게 정리해드리면, 지금 손님 흐름은 이런 결로 모입니다",
                        text = life.summaryText
                    ),
                    state = SuriSpeechSceneState.HIGHLIGHT
                ),
                segment(
                    key = "guide",
                    chip = "다음 안내",
                    title = "원하시면 더 깊은 자리까지 이어가겠습니다",
                    body = "여기까지는 기본 흐름이고요. 조금 더 들어가면 손님 고민을 중심으로 어디에서 막히고, 어느 시점에서 풀릴 수 있는지까지 한 겹 더 섬세하게 말씀드릴 수 있습니다.",
                    state = SuriSpeechSceneState.CLOSING
                )
            )
        )
    }

    fun buildPersonalResult(
        bundle: NumerologyResultBundle,
        consultation: PremiumConsultation,
        topic: PremiumTopic,
        concern: String,
        book: FortuneBook? = null
    ): SuriSpeechScript {
        val concernLine = concern.takeIf { it.isNotBlank() }?.let {
            "이번에는 ${topic.label} 쪽으로 손님께서 마음에 두고 계신 \"$it\"라는 질문을 함께 놓고 살펴보겠습니다."
        } ?: "이번에는 ${topic.label} 흐름을 중심으로, 지금 손님 곁에 머무는 결을 차분히 풀어드리겠습니다."

        return SuriSpeechScript(
            scriptId = "personal-result-${book?.bookId ?: bundle.numbers.code}-${topic.name}",
            title = book?.coverTitle ?: "AI 프리미엄 운세",
            subtitle = "책은 정리형으로 남기고, 목소리는 조금 더 부드럽게 들려드릴게요.",
            segments = listOf(
                segment(
                    key = "greeting",
                    chip = "상담 시작",
                    title = "손님, 이제 본격적으로 흐름을 열어보겠습니다",
                    body = concernLine,
                    state = SuriSpeechSceneState.GREETING
                ),
                segment(
                    key = "core",
                    chip = "핵심 흐름",
                    title = "지금 가장 깊게 걸리는 결은 이쪽입니다",
                    body = spokenBody(
                        lead = "가만히 짚어보면, 지금 손님께 가장 크게 걸려 있는 중심은 이 부분입니다",
                        text = consultation.core
                    ),
                    state = SuriSpeechSceneState.FOCUS
                ),
                segment(
                    key = "interpretation",
                    chip = "종합 해석",
                    title = "숫자의 결을 따라가면 이렇게 읽힙니다",
                    body = spokenBody(
                        lead = "이 흐름을 숫자와 함께 묶어보면, 결은 이렇게 이어집니다",
                        text = consultation.interpretation
                    ),
                    state = SuriSpeechSceneState.EXPLAIN
                ),
                segment(
                    key = "caution",
                    chip = "조심할 부분",
                    title = "이 대목에서는 조금만 숨을 고르시는 게 좋겠습니다",
                    body = spokenBody(
                        lead = "다만 손님, 여기서는 마음이 앞서가면 흐름이 조금 거칠어질 수 있습니다",
                        text = consultation.caution
                    ),
                    state = SuriSpeechSceneState.CAUTION
                ),
                segment(
                    key = "direction",
                    chip = "풀어가는 길",
                    title = "이 방향으로 가시면 기운이 한결 부드러워집니다",
                    body = spokenBody(
                        lead = "제가 조용히 권해드리고 싶은 방향은 이렇습니다",
                        text = consultation.direction
                    ),
                    state = SuriSpeechSceneState.COMFORT
                ),
                segment(
                    key = "closing",
                    chip = "마지막 조언",
                    title = "끝으로 짧고 부드럽게 남기겠습니다",
                    body = spokenBody(
                        lead = "마지막으로 한 말씀만 덧붙이면",
                        text = consultation.oneLineAdvice
                    ),
                    state = SuriSpeechSceneState.CLOSING
                )
            )
        )
    }

    fun buildCompatibilityPreview(concern: String): SuriSpeechScript {
        val concernLine = concern.takeIf { it.isNotBlank() }?.let {
            "손님께서 \"$it\" 같은 질문을 함께 두고 보시면, 두 사람 사이의 공기가 훨씬 또렷하게 잡힙니다."
        } ?: "궁금한 점을 같이 적어주시면, 두 사람 사이로 어떤 기류가 드나드는지 더 분명하게 읽을 수 있습니다."

        return SuriSpeechScript(
            scriptId = "compatibility-preview-${concern.hashCode()}",
            title = "AI 궁합 미리 듣기",
            subtitle = "두 사람의 기운을 한 겹씩 풀어드릴게요.",
            segments = listOf(
                segment(
                    key = "greeting",
                    chip = "궁합 시작",
                    title = "궁합은 각자 가진 결부터 봐야 합니다",
                    body = "손님, 궁합은 누가 더 맞고 덜 맞고를 먼저 정하는 자리가 아닙니다. 남자 쪽 기운, 여자 쪽 기운을 먼저 보고, 그다음 두 사람 사이에 어떤 결이 만들어지는지 이어서 살펴봐야 합니다.",
                    state = SuriSpeechSceneState.GREETING
                ),
                segment(
                    key = "guide",
                    chip = "읽는 순서",
                    title = "이 순서로 보면 흐름이 맑게 정리됩니다",
                    body = "먼저 남자 쪽 기본 기운을 읽고, 다음에 여자 쪽 기본 기운을 봅니다. 그 뒤에 관계의 결을 붙여 잘 맞는 점과 부딪히는 점, 그리고 함께 지낼 때의 분위기까지 차례대로 풀어드립니다. $concernLine",
                    state = SuriSpeechSceneState.EXPLAIN
                ),
                segment(
                    key = "closing",
                    chip = "다음 안내",
                    title = "입력이 끝나면 바로 이어서 봐드리겠습니다",
                    body = "생년월일을 두 분 다 적어주시면, 수리가 맞은편에 앉아 이야기하듯 천천히 읽어드릴게요.",
                    state = SuriSpeechSceneState.CLOSING
                )
            )
        )
    }

    fun buildCompatibilityResult(
        consultation: CompatibilityConsultation,
        concern: String,
        book: FortuneBook? = null
    ): SuriSpeechScript {
        val concernLine = concern.takeIf { it.isNotBlank() }?.let {
            "이번에는 손님께서 궁금해하신 \"$it\"라는 질문도 함께 두고 읽어보겠습니다."
        } ?: "이번에는 두 사람 사이에 흐르는 전반적인 궁합을 중심으로 차분히 말씀드리겠습니다."

        return SuriSpeechScript(
            scriptId = "compatibility-result-${book?.bookId ?: concern.hashCode()}",
            title = book?.coverTitle ?: "AI 궁합",
            subtitle = "정리는 책에 남기고, 말은 더 부드럽게 풀어드릴게요.",
            segments = listOf(
                segment(
                    key = "greeting",
                    chip = "상담 시작",
                    title = "손님, 두 사람 기운부터 차례로 열어보겠습니다",
                    body = concernLine,
                    state = SuriSpeechSceneState.GREETING
                ),
                segment(
                    key = "male",
                    chip = "남자 기운",
                    title = "먼저 남자 쪽 기본 결입니다",
                    body = spokenBody(
                        lead = "먼저 남자 쪽 기운을 들여다보면",
                        text = consultation.maleEnergy
                    ),
                    state = SuriSpeechSceneState.FOCUS
                ),
                segment(
                    key = "female",
                    chip = "여자 기운",
                    title = "이어서 여자 쪽 기본 결입니다",
                    body = spokenBody(
                        lead = "그리고 여자 쪽 기운을 이어서 보면",
                        text = consultation.femaleEnergy
                    ),
                    state = SuriSpeechSceneState.EXPLAIN
                ),
                segment(
                    key = "flow",
                    chip = "관계의 결",
                    title = "두 사람 사이 공기는 이렇게 읽힙니다",
                    body = spokenBody(
                        lead = "이 두 기운이 만나면 관계의 흐름은 이런 결로 이어집니다",
                        text = consultation.relationshipFlow
                    ),
                    state = SuriSpeechSceneState.HIGHLIGHT
                ),
                segment(
                    key = "strengths",
                    chip = "잘 맞는 점",
                    title = "함께 있을 때 살아나는 장점이 분명합니다",
                    body = spokenBody(
                        lead = "좋게 작동할 때는 이 힘이 또렷하게 살아납니다",
                        text = consultation.strengths
                    ),
                    state = SuriSpeechSceneState.COMFORT
                ),
                segment(
                    key = "friction",
                    chip = "부딪히는 점",
                    title = "여기서는 서로 기세를 조금만 낮추셔야 합니다",
                    body = spokenBody(
                        lead = "다만 이 관계는 이 지점에서 서운함이나 힘겨루기가 생기기 쉽습니다",
                        text = consultation.friction
                    ),
                    state = SuriSpeechSceneState.CAUTION
                ),
                segment(
                    key = "home-tone",
                    chip = "대화 온도",
                    title = "같이 지낼수록 드러나는 분위기는 이렇습니다",
                    body = spokenBody(
                        lead = "함께 머무는 시간이 길어질수록 체감되는 공기는 이쪽입니다",
                        text = consultation.homeTone
                    ),
                    state = SuriSpeechSceneState.EXPLAIN
                ),
                segment(
                    key = "closing",
                    chip = "오래 가는 팁",
                    title = "끝으로 오래 가는 길을 짧게 남기겠습니다",
                    body = spokenBody(
                        lead = consultation.longTermTip,
                        text = consultation.oneLineSummary
                    ),
                    state = SuriSpeechSceneState.CLOSING
                )
            )
        )
    }

    private fun segment(
        key: String,
        chip: String,
        title: String,
        body: String,
        state: SuriSpeechSceneState
    ): SuriSpeechSegment {
        return SuriSpeechSegment(
            key = key,
            chip = chip,
            title = title,
            body = normalize(body),
            state = state
        )
    }

    private fun spokenBody(lead: String, text: String): String {
        val cleanedLead = normalize(lead).trimEnd('.', '!', '?', '…')
        val cleanedText = normalize(text)
            .removePrefix("당신은 ")
            .removePrefix("당신의 ")
            .replace("당신", "손님")
        return "$cleanedLead. $cleanedText"
    }

    private fun normalize(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }
}
