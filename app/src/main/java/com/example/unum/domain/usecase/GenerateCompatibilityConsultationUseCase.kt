package com.example.unum.domain.usecase

import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.domain.NumerologyCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GenerateCompatibilityConsultationUseCase {
    suspend operator fun invoke(
        apiKey: String,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String
    ): CompatibilityConsultation = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "상담 연결 키가 설정되지 않았습니다." }
        require(apiKey.startsWith("sk-")) { "상담 연결 키 형식이 올바르지 않습니다." }

        val relationshipNumber = relationshipNumber(maleBundle, femaleBundle)
        val prompt = buildPrompt(
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
            relationshipNumber = relationshipNumber
        )
        val content = requestOpenAi(apiKey.trim(), OPENAI_MODEL, prompt)
        parseConsultation(content, maleBundle, femaleBundle, concern, relationshipNumber)
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
                "궁합노트 요청에 실패했습니다. 설정을 확인한 뒤 다시 시도해주세요."
        }

        error(message)
    }

    private fun buildPrompt(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipNumber: Int
    ): String {
        val maleInput = maleBundle.displayInput
        val femaleInput = femaleBundle.displayInput
        val maleDestiny = maleBundle.content.destinyProfile
        val femaleDestiny = femaleBundle.content.destinyProfile
        val maleResonance = NumerologyCalculator.genderResonanceDescription(maleInput.gender, maleBundle.numbers.destiny)
        val femaleResonance = NumerologyCalculator.genderResonanceDescription(femaleInput.gender, femaleBundle.numbers.destiny)
        val concernText = concern.ifBlank { "특별히 적어둔 질문은 없고, 두 사람의 전반적인 궁합 흐름을 보고 싶어 합니다." }

        return """
            다음 두 사람의 궁합을 수리학 규칙으로 해석해주세요.

            [남자 정보]
            - 사용자가 입력한 생년월일: ${maleInput.year}.${maleInput.month}.${maleInput.day}
            - 달력 구분: ${calendarTypeLabel(maleInput.calendarType)}
            - 성별: ${maleInput.gender.label}
            - 운명수: ${maleBundle.numbers.destiny}
            - 운명수의 기운: ${NumerologyCalculator.destinyPolarity(maleBundle.numbers.destiny).label}
            - 성별 보정: $maleResonance
            - 인생 코드: ${maleBundle.numbers.code}
            - 기본 성향 제목: ${maleDestiny.title}
            - 핵심 키워드: ${maleDestiny.coreKeywords.joinToString(", ")}
            - 주의 키워드: ${maleDestiny.cautionKeywords.joinToString(", ")}
            - 기본 해석: ${maleDestiny.destinyText}
            - 인생 요약: ${maleBundle.content.lifeRecord.summaryText}

            [여자 정보]
            - 사용자가 입력한 생년월일: ${femaleInput.year}.${femaleInput.month}.${femaleInput.day}
            - 달력 구분: ${calendarTypeLabel(femaleInput.calendarType)}
            - 성별: ${femaleInput.gender.label}
            - 운명수: ${femaleBundle.numbers.destiny}
            - 운명수의 기운: ${NumerologyCalculator.destinyPolarity(femaleBundle.numbers.destiny).label}
            - 성별 보정: $femaleResonance
            - 인생 코드: ${femaleBundle.numbers.code}
            - 기본 성향 제목: ${femaleDestiny.title}
            - 핵심 키워드: ${femaleDestiny.coreKeywords.joinToString(", ")}
            - 주의 키워드: ${femaleDestiny.cautionKeywords.joinToString(", ")}
            - 기본 해석: ${femaleDestiny.destinyText}
            - 인생 요약: ${femaleBundle.content.lifeRecord.summaryText}

            [궁합 핵심 규칙]
            - 궁합수는 남자 운명수 + 여자 운명수의 합을 1의 자리만 남긴 값입니다.
            - 현재 궁합수는 $relationshipNumber 입니다.
            - 개인 운명수 의미는 바꾸지 말고 그대로 유지하세요.
            - 먼저 남자 기질, 다음 여자 기질, 그다음 둘의 관계 흐름을 읽어주세요.
            - 궁합수는 개인 성향이 아니라 두 사람 사이의 관계의 결로 해석하세요.

            [프리미엄 궁합 차별화 규칙]
            - 무료 결과처럼 각자의 성향을 길게 다시 설명하지 마세요. 두 사람의 기본 성향은 관계 장면을 해석하기 위한 재료로만 쓰세요.
            - 답변의 70% 이상은 실제 커플·썸·부부 사이에서 벌어질 법한 상황 케이스로 작성하세요.
            - 최소 4개의 장면을 가정하세요: 1) 연락과 답장 속도, 2) 서운함이 쌓이는 대화, 3) 돈·일정·생활 리듬을 맞추는 순간, 4) 싸움 뒤 다시 풀어가는 방식.
            - 사용자의 관심사가 있으면 그 질문을 중심으로 "이런 말이 오갈 때", "이 선택을 미루면", "이 사람이 지쳤을 때"처럼 구체적인 장면을 만들어주세요.
            - friction에는 실제 싸움으로 번지는 패턴과 방치했을 때 손해를 쓰세요.
            - homeTone에는 같이 살거나 오래 만났을 때 집안 분위기, 말의 온도, 생활 리듬을 구체적으로 쓰세요.
            - longTermTip에는 이번 주, 한 달, 장기 룰을 나눠서 제안하세요.
            - 자극적이되 존댓말을 유지하세요. "조율이 필요합니다"보다 "그냥 넘기면 관계가 생각보다 빨리 지칠 수 있습니다"처럼 긴장감을 주세요.

            [숫자 표준 의미]
            - 0 = 여백, 가능성, 전환, 유연성
            - 1 = 시작, 독립, 주도성, 실행
            - 2 = 관계, 조화, 민감함, 인내
            - 3 = 표현, 언어, 해석, 기획
            - 4 = 질서, 기준, 구조, 정리
            - 5 = 확장, 변화, 대담함, 기회
            - 6 = 책임, 성실, 축적, 관리
            - 7 = 집중, 몰입, 추진, 규율
            - 8 = 연결, 대인관계, 확장, 매력
            - 9 = 완성, 정리, 마감, 확장

            [궁합수 ${relationshipNumber} 해석 참고]
            - ${relationshipMeaning(relationshipNumber)}

            [양의 기운이 큰 관계 규칙]
            - 홀수 궁합수는 표현력과 추진력이 크게 드러납니다.
            - 특히 5, 7, 9는 집안의 분위기와 말의 세기, 주장, 감정 표현이 활발하게 커질 수 있습니다.
            - 이것을 싸움만 강조하지 말고 활기와 추진력의 양면성으로 자연스럽게 해석하세요.
            - 남자 운명수와 여자 운명수 모두 홀수이거나, 한쪽에 7 또는 9가 있으면 말의 볼륨과 기세가 더 커질 수 있음을 반영하세요.

            [추가 질문]
            - 사용자 관심사: $concernText

            [문체 규칙]
            - 한국어 존댓말만 사용하세요.
            - 철학관 상담문처럼 부드럽고 자연스럽게 써주세요.
            - 무료 운세와 문장 구조가 겹치지 않게, 현장감 있는 상담 말투로 작성하세요.
            - "반드시", "무조건", "절대" 같은 단정형은 남발하지 마세요.
            - 파탄, 범죄, 질병, 사망 같은 공포 조장 표현은 금지합니다.
            - "선생님" 호칭은 쓰지 말고, "당신" 또는 자연스러운 생략형만 사용하세요.

            [출력 규칙]
            반드시 아래 JSON만 반환하세요.

            {
              "maleEnergy": "남자 성향이 관계 안에서 실제로 어떻게 튀어나오는지. 무료 성향 반복 금지",
              "femaleEnergy": "여자 성향이 관계 안에서 실제로 어떻게 튀어나오는지. 무료 성향 반복 금지",
              "relationshipFlow": "둘이 만났을 때 생기는 실제 관계 장면과 흐름",
              "strengths": "두 사람이 잘 맞는 순간의 구체적 케이스",
              "friction": "싸움으로 번지기 쉬운 구체적 패턴과 방치 시 손해",
              "homeTone": "오래 만나거나 같이 살 때의 생활 분위기와 말의 온도",
              "longTermTip": "이번 주, 한 달, 장기 관계 룰",
              "oneLineSummary": "한줄 궁합 요약. 살짝 자극적인 존댓말"
            }
        """.trimIndent()
    }

    private fun parseConsultation(
        rawContent: String,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipNumber: Int
    ): CompatibilityConsultation {
        val jsonText = rawContent
            .substringAfter("{", rawContent)
            .substringBeforeLast("}", rawContent)
            .let { "{$it}" }
        val json = JSONObject(jsonText)
        val parsed = CompatibilityConsultation(
            maleEnergy = json.optString("maleEnergy"),
            femaleEnergy = json.optString("femaleEnergy"),
            relationshipFlow = json.optString("relationshipFlow"),
            strengths = json.optString("strengths"),
            friction = json.optString("friction"),
            homeTone = json.optString("homeTone"),
            longTermTip = json.optString("longTermTip"),
            oneLineSummary = json.optString("oneLineSummary")
        )
        return normalizeConsultation(parsed, maleBundle, femaleBundle, concern, relationshipNumber)
    }

    private fun normalizeConsultation(
        consultation: CompatibilityConsultation,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipNumber: Int
    ): CompatibilityConsultation {
        val maleProfile = maleBundle.content.destinyProfile
        val femaleProfile = femaleBundle.content.destinyProfile
        val maleCore = maleProfile.coreKeywords.firstOrNull() ?: maleProfile.title
        val femaleCore = femaleProfile.coreKeywords.firstOrNull() ?: femaleProfile.title
        val maleCaution = maleProfile.cautionKeywords.firstOrNull() ?: "조급함"
        val femaleCaution = femaleProfile.cautionKeywords.firstOrNull() ?: "속앓이"
        val concernText = concern.takeIf { it.isNotBlank() }

        return consultation.copy(
            maleEnergy = consultation.maleEnergy.ifBlank {
                "남자 쪽은 ${maleProfile.title}의 결이 관계 안에서 $maleCore 쪽으로 먼저 튀어나오기 쉽습니다. 답장이 늦거나 약속이 흔들리는 장면에서 ${maleCaution}가 올라오면 말투가 딱딱해지고, 그 순간을 그냥 넘기면 상대에게는 차갑게 느껴질 수 있습니다."
            },
            femaleEnergy = consultation.femaleEnergy.ifBlank {
                "여자 쪽은 ${femaleProfile.title}의 결이 관계 안에서 $femaleCore 쪽으로 드러납니다. 겉으로는 괜찮다고 해도 ${femaleCaution}가 쌓이면 어느 날 갑자기 거리감이 생길 수 있으니, 작은 서운함을 오래 묵히는 흐름은 조심해야 합니다."
            },
            relationshipFlow = consultation.relationshipFlow.ifBlank {
                "두 사람의 궁합수는 ${relationshipNumber}로 읽히며, ${relationshipMeaning(relationshipNumber)} 쉽게 말해 잘 맞을 때는 속도가 붙지만, 서로의 방식만 고집하면 연락 하나, 약속 하나에서도 생각보다 빨리 피곤해질 수 있는 관계입니다."
            },
            strengths = consultation.strengths.ifBlank {
                buildStrengthsFallback(maleBundle, femaleBundle, relationshipNumber)
            },
            friction = consultation.friction.ifBlank {
                buildFrictionFallback(maleBundle, femaleBundle, relationshipNumber)
            },
            homeTone = consultation.homeTone.ifBlank {
                buildHomeToneFallback(maleBundle, femaleBundle, relationshipNumber)
            },
            longTermTip = consultation.longTermTip.ifBlank {
                buildLongTermTipFallback(relationshipNumber, concernText)
            },
            oneLineSummary = consultation.oneLineSummary.ifBlank {
                "두 사람은 끌림은 분명하지만, 감정 확인을 대충 넘기면 좋은 궁합도 금방 피곤한 관계로 변할 수 있습니다."
            }
        )
    }

    private fun buildStrengthsFallback(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int
    ): String {
        return "남자 쪽의 ${maleBundle.content.destinyProfile.title} 기운과 여자 쪽의 ${femaleBundle.content.destinyProfile.title} 기운이 만나면 ${relationshipStrengthHint(relationshipNumber)} 예를 들어 둘이 같은 목표를 잡거나 여행, 이사, 돈 관리처럼 현실 주제를 함께 정리할 때 의외로 합이 살아납니다. 이 장점을 살리면 관계가 든든해지지만, 말로만 좋아하고 실제 룰을 안 만들면 금방 흐지부지해질 수 있습니다."
    }

    private fun buildFrictionFallback(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int
    ): String {
        val maleCaution = maleBundle.content.destinyProfile.cautionKeywords.firstOrNull() ?: "조급함"
        val femaleCaution = femaleBundle.content.destinyProfile.cautionKeywords.firstOrNull() ?: "속앓이"
        return "다만 남자 쪽의 $maleCaution 흐름과 여자 쪽의 $femaleCaution 흐름이 겹치면 ${relationshipFrictionHint(relationshipNumber)} 특히 답장 하나가 늦거나, 약속 시간을 바꾸거나, 돈 쓰는 기준이 달라지는 작은 장면에서 불씨가 커질 수 있습니다. 이걸 대충 넘기면 큰 문제도 아닌 일로 서로를 피곤하게 만들 수 있습니다."
    }

    private fun buildHomeToneFallback(
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        relationshipNumber: Int
    ): String {
        val oddCount = listOf(
            maleBundle.numbers.destiny,
            femaleBundle.numbers.destiny,
            relationshipNumber
        ).count { it in listOf(1, 3, 5, 7, 9) }

        return when {
            relationshipNumber in listOf(5, 7, 9) || oddCount >= 2 ->
                "양의 기운이 겹쳐 집 안의 분위기와 말의 세기가 비교적 크게 살아나는 편입니다. 좋을 때는 웃음, 계획, 추진력이 확 올라오지만, 예민할 때는 목소리와 주장도 같이 커집니다. 둘 다 지친 날에는 말 한마디가 생각보다 크게 꽂힐 수 있으니, 싸우는 중에는 결론보다 휴식 시간을 먼저 잡는 편이 낫습니다."
            relationshipNumber in listOf(2, 4, 6, 8) ->
                "관계의 온도는 비교적 차분하고 생활 리듬을 맞추는 쪽으로 흐르기 쉽습니다. 다만 겉으로 조용하다고 문제가 없는 건 아닙니다. 서운함을 속으로만 쌓으면 어느 순간 대화가 업무 보고처럼 말라버릴 수 있으니, 작은 감정 확인을 일부러 자주 해야 합니다."
            else ->
                "관계의 분위기는 유동적인 편이라 가까워질 때는 빠르게 가까워지고, 애매해질 때도 갑자기 흐려질 수 있습니다. 두 사람이 원하는 관계 이름과 속도를 분명히 맞춰두지 않으면, 좋은 감정이 있어도 생활 속에서는 불안정하게 흔들릴 수 있습니다."
        }
    }

    private fun buildLongTermTipFallback(relationshipNumber: Int, concern: String?): String {
        val concernAddon = concern?.let { " 특히 \"$it\" 같은 현실 질문이 있다면 결론을 서두르기보다 생활 리듬과 대화 방식부터 확인해보세요." }.orEmpty()
        return when (relationshipNumber) {
            0 -> "이번 주에는 관계 이름보다 서로 원하는 속도를 먼저 말하세요. 한 달 안에는 연락 빈도와 만나는 주기를 정하고, 장기적으로는 애매함을 오래 방치하지 않는 룰이 필요합니다. 이걸 미루면 가까운 듯 멀어지는 패턴 때문에 마음이 더 힘들어질 수 있습니다.$concernAddon"
            1 -> "이번 주에는 누가 먼저 끌고 갈지보다 어떻게 같이 시작할지를 맞추세요. 한 달 안에는 데이트, 연락, 돈 쓰는 기준을 정하고, 장기적으로는 주도권 싸움이 생길 때 바로 멈추는 신호를 만들어야 합니다.$concernAddon"
            2 -> "이번 주에는 짧게라도 감정 확인을 하세요. 한 달 안에는 서운함을 하루 이상 묵히지 않는 약속을 만들고, 장기적으로는 침묵을 배려로 착각하지 않는 게 중요합니다. 그냥 참으면 관계가 조용히 지칠 수 있습니다.$concernAddon"
            3 -> "이번 주에는 말싸움에서 이기려는 표현을 줄이세요. 한 달 안에는 농담과 진심을 구분하는 대화 룰을 만들고, 장기적으로는 말맛이 독이 되지 않게 사과 속도를 빠르게 가져가야 합니다.$concernAddon"
            4 -> "이번 주에는 원칙 하나와 예외 하나를 같이 정하세요. 한 달 안에는 생활 리듬을 맞추되 숨 쉴 틈을 남기고, 장기적으로는 답답함이 쌓이기 전에 일정과 감정을 같이 점검해야 합니다.$concernAddon"
            5 -> "이번 주에는 설렘이 올라올수록 결정은 하루 늦추세요. 한 달 안에는 즉흥 약속과 중요한 결정을 나누고, 장기적으로는 흥분한 날의 약속을 다음 날 다시 확인하는 습관이 필요합니다.$concernAddon"
            6 -> "이번 주에는 챙겨주는 행동 뒤에 원하는 감정을 직접 말하세요. 한 달 안에는 책임과 애정 표현을 분리하고, 장기적으로는 의무감이 사랑을 대신하지 않게 둘만의 휴식 시간을 지켜야 합니다.$concernAddon"
            7 -> "이번 주에는 서로 몰아붙이는 말투를 줄이세요. 한 달 안에는 싸움이 커지기 전 멈춤 시간을 정하고, 장기적으로는 같이 밀고 가는 힘만큼 일부러 쉬는 시간을 넣어야 합니다.$concernAddon"
            8 -> "이번 주에는 주변 사람보다 둘만의 약속을 먼저 챙기세요. 한 달 안에는 외부 일정과 둘만의 시간을 분리하고, 장기적으로는 관계의 중심을 밖에 빼앗기지 않는 룰이 필요합니다.$concernAddon"
            else -> "이번 주에는 결론을 몰아붙이지 말고 서로에게 기대하는 관계의 끝그림을 말하세요. 한 달 안에는 정리해야 할 오해를 남기지 말고, 장기적으로는 큰 감정이 올라올 때 바로 결론 내리지 않는 습관이 필요합니다.$concernAddon"
        }
    }

    private fun relationshipStrengthHint(number: Int): String = when (number) {
        0 -> "정해진 틀보다 새로운 방향을 같이 만들 수 있는 여백이 큽니다."
        1 -> "끌림이 생기면 빠르게 시작하고 움직일 힘이 분명합니다."
        2 -> "정서적 결을 잘 읽고 배려가 오가는 온도가 자연스럽게 만들어집니다."
        3 -> "대화와 농담, 말맛이 살아나 친구처럼 잘 통하는 장점이 있습니다."
        4 -> "생활과 규칙, 현실 기반을 함께 세우는 힘이 탄탄합니다."
        5 -> "활기와 변화의 기운이 커서 함께 새로운 경험을 만들기 좋습니다."
        6 -> "책임감과 생활력이 살아나 오래 가는 관계로 다져가기 좋습니다."
        7 -> "목표를 함께 잡으면 강하게 밀고 나가는 집중력이 큽니다."
        8 -> "사람과 기회를 끌어들이는 연결력이 좋아 외부 흐름이 살아납니다."
        else -> "관계의 밀도가 빠르게 깊어지며 서로에게 큰 의미를 주기 쉽습니다."
    }

    private fun relationshipFrictionHint(number: Int): String = when (number) {
        0 -> "가까워졌다 멀어졌다 하는 흐름이 반복되며 중심이 흔들릴 수 있습니다."
        1 -> "서로의 방식이 부딪히면 주도권 다툼이 빨라질 수 있습니다."
        2 -> "서운함을 겉으로 못 내고 안에 쌓는 속앓이형 패턴으로 흐르기 쉽습니다."
        3 -> "감정이 올라오면 말이 날카로워지고 해석 싸움이 생길 수 있습니다."
        4 -> "기준이 강한 둘이 만나 답답함과 융통성 부족이 커질 수 있습니다."
        5 -> "기세가 큰 만큼 좋을 때도 크고 흔들릴 때도 크게 흔들릴 수 있습니다."
        6 -> "사랑이 책임으로만 굳어지면 무게감이 답답함으로 바뀔 수 있습니다."
        7 -> "예민할 때는 말보다 기세가 먼저 나가 긴장감이 높아질 수 있습니다."
        8 -> "주변 사람과 외부 일에 에너지를 많이 써서 둘만의 중심이 약해질 수 있습니다."
        else -> "감정의 파동이 커서 정리와 결론을 너무 빨리 내려버릴 수 있습니다."
    }

    private fun relationshipMeaning(number: Int): String = when (number) {
        0 -> "정해진 틀보다 열린 가능성이 큰 궁합입니다. 서로의 빈틈을 채우며 새 방향을 만들 수 있지만, 중심 합의가 약하면 가까워졌다 멀어졌다 하는 흐름도 생기기 쉽습니다."
        1 -> "끌림이 생기면 빠르게 시작되는 궁합입니다. 주도권을 잡는 힘이 분명하지만, 서로의 방식이 강하면 부딪힘도 빨라질 수 있습니다."
        2 -> "정서적 결을 잘 읽고 배려가 오가는 궁합입니다. 말보다 감정 확인의 질이 관계를 오래 안정시키는 편입니다."
        3 -> "대화, 농담, 해석, 메시지의 흐름이 살아나는 궁합입니다. 잘 통하지만 감정이 올라오면 언어가 날카로워질 수 있습니다."
        4 -> "생활, 규칙, 현실 기반을 함께 세우기 좋은 궁합입니다. 안정감이 크지만 답답함이 생기지 않도록 여유를 남겨야 합니다."
        5 -> "움직임과 설렘, 변화가 큰 궁합입니다. 활기는 좋지만 흥분 뒤 검증이 꼭 필요한 관계입니다."
        6 -> "생활력, 책임감, 현실 관리가 살아나는 궁합입니다. 오래 가는 힘이 있지만 돌봄과 의무의 균형이 중요합니다."
        7 -> "둘이 목표 하나를 잡으면 강하게 밀고 나가는 궁합입니다. 몰입이 강한 만큼 멈춤과 여유도 의식적으로 필요합니다."
        8 -> "대인관계, 매력, 외부 연결 운이 살아나는 궁합입니다. 다만 둘만의 중심과 경계선을 먼저 세워야 안정됩니다."
        else -> "관계의 밀도와 결론성이 큰 궁합입니다. 서로에게 주는 영향이 큰 만큼 완성의 방식을 잘 잡는 것이 중요합니다."
    }

    private fun relationshipNumber(maleBundle: NumerologyResultBundle, femaleBundle: NumerologyResultBundle): Int {
        return (maleBundle.numbers.destiny + femaleBundle.numbers.destiny) % 10
    }

    private fun calendarTypeLabel(type: CalendarType): String {
        return if (type == CalendarType.LUNAR) "음력" else "양력"
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "You are a Korean premium numerology compatibility writer. Do not repeat each person's free reading; use it only as context for concrete relationship situations. Write detailed, situational, slightly provocative compatibility advice in polite Korean honorific style. Never call the user 선생님; use 당신 or omit the direct address. Return only valid JSON."
        private const val OPENAI_MODEL = "gpt-5.1"
    }
}
