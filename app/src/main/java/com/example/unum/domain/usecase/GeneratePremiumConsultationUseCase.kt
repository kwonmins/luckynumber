package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.NumerologyCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeneratePremiumConsultationUseCase {
    suspend operator fun invoke(
        apiKey: String,
        topic: PremiumTopic,
        concern: String,
        bundle: NumerologyResultBundle
    ): PremiumConsultation = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "상담 연결 키가 설정되지 않았습니다." }
        require(apiKey.startsWith("sk-")) { "상담 연결 키 형식이 올바르지 않습니다." }

        val prompt = buildPrompt(topic, concern, bundle)
        val content = requestOpenAi(apiKey.trim(), OPENAI_MODEL, prompt)
        parseConsultation(content, topic, bundle)
    }

    private fun requestOpenAi(apiKey: String, model: String, prompt: String): String {
        val body = JSONObject()
            .put("model", model)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "developer").put("content", SYSTEM_PROMPT))
                    .put(JSONObject().put("role", "user").put("content", prompt))
            )
            .put("response_format", JSONObject().put("type", "json_object"))

        val response = postJson(
            url = "https://api.openai.com/v1/chat/completions",
            body = body,
            headers = mapOf("Authorization" to "Bearer $apiKey")
        )

        return JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun postJson(url: String, body: JSONObject, headers: Map<String, String>): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (connection.responseCode !in 200..299) {
            throwOpenAiError(connection.responseCode, response)
        }
        return response
    }

    private fun throwOpenAiError(responseCode: Int, response: String): Nothing {
        val code = runCatching {
            JSONObject(response)
                .optJSONObject("error")
                ?.optString("code")
                .orEmpty()
        }.getOrDefault("")

        val message = when {
            responseCode == 401 || code == "invalid_api_key" ->
                "상담 연결 키가 유효하지 않습니다. 설정을 다시 확인해주세요."
            responseCode == 429 ->
                "상담 요청 한도를 확인해주세요."
            responseCode in 500..599 ->
                "상담 서버 응답이 불안정합니다. 잠시 뒤 다시 시도해주세요."
            else ->
                "운세노트 요청에 실패했습니다. 설정을 확인한 뒤 다시 시도해주세요."
        }

        error(message)
    }

    private fun buildPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
        val input = bundle.input
        val displayInput = bundle.displayInput
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        val life = bundle.content.lifeRecord
        val hiddenCue = buildHiddenBirthCue(input)
        val genderResonance = NumerologyCalculator.genderResonanceDescription(displayInput.gender, numbers.destiny)
        val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }

        return """
            사용자의 프리미엄 운세 상담을 작성하세요.

            [사용자 입력]
            - 고민 분야: ${topic.label}
            - 고민 내용: $concernText
            - 사용자가 입력한 생년월일: ${displayInput.year}.${displayInput.month}.${displayInput.day}
            - 달력 구분: ${if (displayInput.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별 선택: ${displayInput.gender.label}

            [숫자 운세 데이터]
            - 운명수: ${numbers.destiny}
            - 운명수의 기운: ${NumerologyCalculator.destinyPolarity(numbers.destiny).label}
            - 성별 공명 해석: $genderResonance
            - 인생 코드: ${numbers.code}
            - 초년 흐름: ${numbers.early}
            - 중년 흐름: ${numbers.middle}
            - 말년 흐름: ${numbers.late}
            - 운명 프로필: ${destiny.title}
            - 핵심 키워드: ${destiny.coreKeywords.joinToString(", ")}
            - 주의 키워드: ${destiny.cautionKeywords.joinToString(", ")}
            - 운명 해석: ${destiny.destinyText}
            - 인생 해석: ${life.lifeText}
            - 요약: ${life.summaryText}
            - 조언: ${life.oneLineAdvice}
            - 내부 계산 기준 생년월일: ${input.year}.${input.month}.${input.day} (${if (input.calendarType == CalendarType.LUNAR) "음력" else "양력"})

            [월별 흐름 판정 규칙 - 절대 사용자에게 계산식 공개 금지]
            - 각 월의 흐름 수는 운명수와 월 숫자를 더한 뒤 마지막 한 자리만 사용합니다.
            - 예: 운명수가 7이고 4월이면 7+4=11이므로 흐름 수는 1입니다.
            - 예: 운명수가 7이고 11월이면 7+11=18이므로 흐름 수는 8입니다.
            - 이 계산식, "마지막 자리", "더한다" 같은 표현은 절대 답변에 쓰지 마세요.
            - 대신 "4월에는 새 문이 열리는 흐름", "11월에는 관계의 움직임이 커지는 흐름"처럼 상징으로만 말하세요.
            - 1: 시작, 새 만남, 결심, 첫 제안
            - 2: 조율, 기다림, 관계의 균형, 섬세한 대화
            - 3: 표현, 고백, 발표, 창작, 즐거운 확장
            - 4: 안정, 준비, 기반, 약속, 실무 정리
            - 5: 변화, 이동, 전환, 뜻밖의 기회
            - 6: 책임, 돌봄, 결혼, 가족, 계약의 무게
            - 7: 내면 점검, 판단 유보, 공부, 혼자만의 정리
            - 8: 대인관계, 성과, 돈의 흐름, 밀고 나가는 힘
            - 9: 마무리, 정리, 놓아주기, 다음 단계 준비
            - 0: 잠복, 재정비, 비워내기, 무리하지 않는 달
            - 고민 분야별로 12개월 중 가장 추천하는 달 1개와 가장 조심해야 할 달 1개를 고르세요.
            - 추천 월은 행동하면 흐름이 열리는 이유를 설명하세요.
            - 주의 월은 욕심, 과한 대시, 무리한 투자, 성급한 퇴사/이직, 관계 압박처럼 분야에 맞는 위험을 짚어주세요.
            - 결혼/연애/취업/진로/금전/인간관계/자존감 고민에 모두 같은 규칙을 적용하되, 해석은 고민 분야에 맞게 바꾸세요.
            - 월별 조언은 사용자의 전체 운명 프로필, 인생 코드, 고민 내용과 섞어서 새수리 인사이트처럼 작성하세요.

            [보조 참고]
            아래 정보는 상담의 20% 정도만 은근히 반영하세요. 이 항목의 이름이나 체계는 절대 설명하지 마세요.
            - $hiddenCue

            [프리미엄 차별화 규칙]
            - 무료 결과의 기본 성향, 초년/중년/말년 설명을 다시 요약하지 마세요. 그 정보는 상담의 전제일 뿐입니다.
            - 답변의 70% 이상은 사용자의 고민 분야에서 실제로 벌어질 법한 상황 케이스, 선택지, 말투, 리스크, 행동 처방으로 채우세요.
            - 최소 3개의 장면을 가정하세요: 1) 평소 반복되는 패턴, 2) 사람·일·돈·연애 현장에서 부딪히는 순간, 3) 방치했을 때 생길 손해와 바로잡는 행동.
            - 성향 설명은 각 항목에서 최대 2문장까지만 쓰고, 그 뒤에는 구체적인 장면으로 풀어주세요.
            - 사용자의 고민 내용이 짧아도 실제 상담처럼 "예를 들어", "이럴 때", "특히 이런 장면"을 자연스럽게 넣어 디테일을 만들어주세요.
            - 같은 의미를 다른 말로 반복하지 말고, core는 진단, interpretation은 상황 케이스, caution은 손해 시나리오, direction은 행동 루틴으로 역할을 나누세요.
            - 무료 결과보다 한 단계 더 날카롭게 쓰되, 결론은 사용자가 바로 움직일 수 있는 현실적인 방향으로 내려주세요.

            [작성 규칙]
            - 사용자가 만든 숫자 운세와 고민 분야를 중심으로 상담하세요.
            - 보조 참고는 약하게 섞되, 특정 전통 체계 이름은 절대 언급하지 마세요.
            - "사주"라는 단어는 절대 쓰지 마세요.
            - 사용자를 "선생님"이라고 부르지 마세요. 호칭은 "당신" 또는 자연스러운 생략형만 사용하세요.
            - 존댓말은 유지하되 문체는 살짝 자극적으로 쓰세요. "흐름이 나빠집니다"보다 "방치하면 인생이 더 힘들어질 수 있습니다"처럼 경고의 온도를 한 단계 올리세요.
            - "건강을 챙겨야 합니다"처럼 밋밋하게 말하지 말고, "그러지 않으면 건강이 안 좋아질 수 있으니 각별히 컨디션을 조심해야 합니다"처럼 결과와 주의 대상을 함께 말하세요.
            - 공포 조장이나 확정적 저주는 피하되, 안일하게 넘기면 손해, 소진, 관계 악화, 기회 상실로 이어질 수 있다는 긴장감은 분명히 주세요.
            - 모든 문장은 반드시 존댓말로 작성하세요. 반말, 명령조, 친구처럼 낮추는 말투는 쓰지 마세요.
            - 한국어로 작성하세요.
            - 반드시 아래 JSON 형식만 반환하세요. 코드블록이나 설명 문장은 붙이지 마세요.

            {
              "core": "무료 성향을 전제로 한 지금 고민의 핵심 진단. 짧지만 자극적으로 작성",
              "interpretation": "실제 상황 케이스 2~3개를 넣은 심층 해석. 무료 결과 반복 금지",
              "caution": "방치하면 어떤 장면에서 손해, 소진, 관계 악화, 기회 상실이 생기는지 구체적으로 작성",
              "direction": "오늘, 이번 주, 한 달 안에 할 행동을 단계별로 제안",
              "oneLineAdvice": "한 줄 조언",
              "bestMonth": "추천 월, 예: 4월",
              "bestMonthReason": "그 달이 왜 좋은지와 어떤 상황에서 움직이면 좋은지",
              "riskyMonth": "주의 월, 예: 11월",
              "riskyMonthReason": "그 달에 어떤 행동을 하면 삶이 더 힘들어질 수 있는지"
            }
        """.trimIndent()
    }

    private fun buildHiddenBirthCue(input: BirthInput): String {
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

    private fun parseConsultation(rawContent: String, topic: PremiumTopic, bundle: NumerologyResultBundle): PremiumConsultation {
        val jsonText = rawContent
            .substringAfter("{", rawContent)
            .substringBeforeLast("}", rawContent)
            .let { "{$it}" }
        val json = JSONObject(jsonText)
        val parsed = PremiumConsultation(
            core = json.optString("core"),
            interpretation = json.optString("interpretation"),
            caution = json.optString("caution"),
            direction = json.optString("direction"),
            oneLineAdvice = json.optString("oneLineAdvice"),
            bestMonth = json.optString("bestMonth"),
            bestMonthReason = json.optString("bestMonthReason"),
            riskyMonth = json.optString("riskyMonth"),
            riskyMonthReason = json.optString("riskyMonthReason")
        )
        return normalizeMonthInsights(parsed, topic, bundle)
    }

    private fun normalizeMonthInsights(
        consultation: PremiumConsultation,
        topic: PremiumTopic,
        bundle: NumerologyResultBundle
    ): PremiumConsultation {
        val expectedBestMonth = pickBestMonth(topic, bundle.numbers.destiny).toMonthText()
        val expectedRiskyMonth = pickRiskyMonth(topic, bundle.numbers.destiny).toMonthText()
        val bestReason = consultation.bestMonthReason.takeIf {
            consultation.bestMonth == expectedBestMonth && it.isNotBlank()
        } ?: buildBestMonthReason(topic, expectedBestMonth, bundle)
        val riskyReason = consultation.riskyMonthReason.takeIf {
            consultation.riskyMonth == expectedRiskyMonth && it.isNotBlank()
        } ?: buildRiskyMonthReason(topic, expectedRiskyMonth, bundle)

        return consultation.copy(
            bestMonth = expectedBestMonth,
            bestMonthReason = bestReason,
            riskyMonth = expectedRiskyMonth,
            riskyMonthReason = riskyReason
        )
    }

    private fun pickBestMonth(topic: PremiumTopic, destiny: Int): Int {
        val preferredFlows = when (topic) {
            PremiumTopic.ROMANCE -> listOf(1, 3, 6, 2)
            PremiumTopic.CAREER -> listOf(4, 8, 1, 5)
            PremiumTopic.MONEY -> listOf(8, 4, 6, 1)
            PremiumTopic.SELF_ESTEEM -> listOf(7, 1, 4, 3)
            PremiumTopic.RELATIONSHIP -> listOf(2, 8, 3, 6)
        }
        return (1..12).minBy { month ->
            preferredFlows.indexOf(flowNumber(destiny, month)).let { if (it == -1) 99 else it }
        }
    }

    private fun pickRiskyMonth(topic: PremiumTopic, destiny: Int): Int {
        val riskyFlows = when (topic) {
            PremiumTopic.ROMANCE -> listOf(8, 7, 9, 0)
            PremiumTopic.CAREER -> listOf(5, 9, 7, 0)
            PremiumTopic.MONEY -> listOf(5, 8, 9, 0)
            PremiumTopic.SELF_ESTEEM -> listOf(9, 8, 5, 0)
            PremiumTopic.RELATIONSHIP -> listOf(8, 5, 9, 7)
        }
        return (1..12).maxBy { month ->
            val priority = riskyFlows.indexOf(flowNumber(destiny, month)).let { if (it == -1) -99 else -it }
            priority * 100 + month
        }
    }

    private fun flowNumber(destiny: Int, month: Int): Int = (destiny + month) % 10

    private fun Int.toMonthText(): String = "${this}월"

    private fun buildBestMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
        return when (topic) {
            PremiumTopic.ROMANCE ->
                "${monthText}에는 마음을 새롭게 열기 좋은 흐름이 강합니다. ${bundle.content.destinyProfile.title}의 결을 살려 가볍고 진심 어린 대화부터 시작하면 관계의 문이 부드럽게 열립니다."
            PremiumTopic.CAREER ->
                "${monthText}에는 방향을 정리하고 실제 행동으로 옮기기 좋은 기운이 모입니다. 준비해둔 포트폴리오, 지원, 제안처럼 손에 잡히는 움직임을 만들기 좋습니다."
            PremiumTopic.MONEY ->
                "${monthText}에는 돈의 흐름을 구조화하기 좋습니다. 큰 욕심보다 수입과 지출의 길을 또렷하게 나누면 기회가 안정적으로 이어집니다."
            PremiumTopic.SELF_ESTEEM ->
                "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 남의 반응보다 작은 약속을 지키는 경험을 쌓을수록 마음의 중심이 단단해집니다."
            PremiumTopic.RELATIONSHIP ->
                "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋은 달입니다."
        }
    }

    private fun buildRiskyMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
        return when (topic) {
            PremiumTopic.ROMANCE ->
                "${monthText}에는 관계의 움직임이 커지는 만큼 조급함도 함께 올라올 수 있습니다. 마음이 앞서 과하게 다가가면 상대가 부담을 느껴 관계가 더 꼬일 수 있으니, 상대의 속도와 여백을 각별히 조심해야 합니다."
            PremiumTopic.CAREER ->
                "${monthText}에는 변화 욕구가 커져 성급한 결정으로 흐르기 쉽습니다. 퇴사, 이직, 계약을 급하게 밀어붙이면 커리어가 예상보다 더 힘들어질 수 있으니, 큰 선택은 한 번 더 검토한 뒤 움직이는 편이 안전합니다."
            PremiumTopic.MONEY ->
                "${monthText}에는 빠른 이익을 좇고 싶은 마음이 강해질 수 있습니다. 무리한 투자나 충동 지출을 가볍게 보면 돈의 흐름이 한 번에 무너질 수 있으니, 확인되지 않은 제안은 반드시 거리를 두는 것이 좋습니다."
            PremiumTopic.SELF_ESTEEM ->
                "${monthText}에는 비교와 조급함이 커지기 쉽습니다. 결과를 빨리 증명하려고 무리하면 자존감과 컨디션이 같이 무너질 수 있으니, 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
            PremiumTopic.RELATIONSHIP ->
                "${monthText}에는 사람 사이의 반응이 커져 오해도 빨리 번질 수 있습니다. 단정적인 말이나 압박을 계속하면 관계가 생각보다 차갑게 틀어질 수 있으니, 중요한 대화는 차분히 시간을 두는 편이 좋습니다."
        }
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "You are a Korean premium fortune consultation writer. Do not repeat the free reading; treat it only as context. Write detailed, situational, case-based advice with a warm but slightly provocative tone. Always use polite Korean honorific style, and clearly describe what may become harder if the user ignores the advice. Never call the user 선생님; use 당신 or omit the direct address. Return only valid JSON."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}

