package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
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
        require(apiKey.isNotBlank()) { "OpenAI API 키가 설정되지 않았습니다." }
        require(apiKey.startsWith("sk-")) { "OpenAI API 키 형식이 올바르지 않습니다." }

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
                "OpenAI API 키가 유효하지 않습니다. 새 키를 발급해 다시 설정해주세요."
            responseCode == 429 ->
                "OpenAI 사용량 한도나 결제 상태를 확인해주세요."
            responseCode in 500..599 ->
                "OpenAI 서버 응답이 불안정합니다. 잠시 뒤 다시 시도해주세요."
            else ->
                "AI 상담 요청에 실패했습니다. 설정을 확인한 뒤 다시 시도해주세요."
        }

        error(message)
    }

    private fun buildPrompt(topic: PremiumTopic, concern: String, bundle: NumerologyResultBundle): String {
        val input = bundle.input
        val numbers = bundle.numbers
        val destiny = bundle.content.destinyProfile
        val life = bundle.content.lifeRecord
        val hiddenCue = buildHiddenBirthCue(input)
        val concernText = concern.ifBlank { "요즘 마음에 가장 자주 떠오르는 고민을 아직 구체적으로 적지 않았습니다." }

        return """
            사용자의 프리미엄 운세 상담을 작성하세요.

            [사용자 입력]
            - 고민 분야: ${topic.label}
            - 고민 내용: $concernText
            - 생년월일: ${input.year}.${input.month}.${input.day}
            - 달력 구분: ${if (input.calendarType == CalendarType.LUNAR) "음력" else "양력"}
            - 성별 선택: ${input.gender.label}

            [숫자 운세 데이터]
            - 운명수: ${numbers.destiny}
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

            [작성 규칙]
            - 사용자가 만든 숫자 운세와 고민 분야를 중심으로 상담하세요.
            - 보조 참고는 약하게 섞되, 특정 전통 체계 이름은 절대 언급하지 마세요.
            - "사주"라는 단어는 절대 쓰지 마세요.
            - 겁주거나 단정하지 말고, 신비롭지만 현실적인 해결 방향을 주세요.
            - 모든 문장은 반드시 존댓말로 작성하세요. 반말, 명령조, 친구처럼 낮추는 말투는 쓰지 마세요.
            - 한국어로 작성하세요.
            - 반드시 아래 JSON 형식만 반환하세요. 코드블록이나 설명 문장은 붙이지 마세요.

            {
              "core": "지금 고민의 핵심",
              "interpretation": "숫자 운세와 고민을 엮은 종합 해석",
              "caution": "주의할 점",
              "direction": "해결을 위한 구체적인 방향",
              "oneLineAdvice": "한 줄 조언",
              "bestMonth": "추천 월, 예: 4월",
              "bestMonthReason": "그 달이 왜 좋은지와 어떻게 움직이면 좋은지",
              "riskyMonth": "주의 월, 예: 11월",
              "riskyMonthReason": "그 달에 무엇을 조심해야 하는지"
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
        return fillMissingMonthInsights(parsed, topic, bundle)
    }

    private fun fillMissingMonthInsights(
        consultation: PremiumConsultation,
        topic: PremiumTopic,
        bundle: NumerologyResultBundle
    ): PremiumConsultation {
        if (
            consultation.bestMonth.isNotBlank() &&
            consultation.bestMonthReason.isNotBlank() &&
            consultation.riskyMonth.isNotBlank() &&
            consultation.riskyMonthReason.isNotBlank()
        ) {
            return consultation
        }

        val bestMonth = consultation.bestMonth.ifBlank { pickBestMonth(topic, bundle.numbers.destiny).toMonthText() }
        val riskyMonth = consultation.riskyMonth.ifBlank { pickRiskyMonth(topic, bundle.numbers.destiny).toMonthText() }

        return consultation.copy(
            bestMonth = bestMonth,
            bestMonthReason = consultation.bestMonthReason.ifBlank {
                buildBestMonthReason(topic, bestMonth, bundle)
            },
            riskyMonth = riskyMonth,
            riskyMonthReason = consultation.riskyMonthReason.ifBlank {
                buildRiskyMonthReason(topic, riskyMonth, bundle)
            }
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
                "\${monthText}에는 마음을 새롭게 열기 좋은 흐름이 강합니다. ${bundle.content.destinyProfile.title}의 결을 살려 가볍고 진심 어린 대화부터 시작하면 관계의 문이 부드럽게 열립니다."
            PremiumTopic.CAREER ->
                "\${monthText}에는 방향을 정리하고 실제 행동으로 옮기기 좋은 기운이 모입니다. 준비해둔 포트폴리오, 지원, 제안처럼 손에 잡히는 움직임을 만들기 좋습니다."
            PremiumTopic.MONEY ->
                "\${monthText}에는 돈의 흐름을 구조화하기 좋습니다. 큰 욕심보다 수입과 지출의 길을 또렷하게 나누면 기회가 안정적으로 이어집니다."
            PremiumTopic.SELF_ESTEEM ->
                "\${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 남의 반응보다 작은 약속을 지키는 경험을 쌓을수록 마음의 중심이 단단해집니다."
            PremiumTopic.RELATIONSHIP ->
                "\${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋은 달입니다."
        }
    }

    private fun buildRiskyMonthReason(topic: PremiumTopic, monthText: String, bundle: NumerologyResultBundle): String {
        return when (topic) {
            PremiumTopic.ROMANCE ->
                "\${monthText}에는 관계의 움직임이 커지는 만큼 조급함도 함께 올라올 수 있습니다. 마음이 앞서 과하게 다가가기보다 상대의 속도와 여백을 지켜주는 편이 좋습니다."
            PremiumTopic.CAREER ->
                "\${monthText}에는 변화 욕구가 커져 성급한 결정으로 흐르기 쉽습니다. 퇴사, 이직, 계약처럼 큰 선택은 한 번 더 검토한 뒤 움직이는 편이 안전합니다."
            PremiumTopic.MONEY ->
                "\${monthText}에는 빠른 이익을 좇고 싶은 마음이 강해질 수 있습니다. 무리한 투자나 충동 지출을 피하고, 확인되지 않은 제안은 거리를 두는 것이 좋습니다."
            PremiumTopic.SELF_ESTEEM ->
                "\${monthText}에는 비교와 조급함이 커지기 쉽습니다. 결과를 빨리 증명하려 하기보다 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
            PremiumTopic.RELATIONSHIP ->
                "\${monthText}에는 사람 사이의 반응이 커져 오해도 빨리 번질 수 있습니다. 단정적인 말이나 압박은 피하고, 중요한 대화는 차분히 시간을 두는 편이 좋습니다."
        }
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "You are a warm Korean fortune consultation writer. Always write in polite Korean honorific style. Return only valid JSON."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}

