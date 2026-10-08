package com.example.unum.data.ai

import com.example.unum.data.ai.premium.buildPrompt as personalPrompt
import com.example.unum.data.ai.compatibility.buildPrompt as compatibilityPrompt
import com.example.unum.data.ai.compatibility.relationshipNumber
import com.example.unum.data.content.FreeFortuneEngine
import com.example.unum.data.model.*
import com.example.unum.domain.usecase.PremiumMonthPlanner
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import com.example.unum.domain.service.JsonChatClient
import kotlinx.coroutines.runBlocking

class PremiumPromptContractTest {
    private val engine = FreeFortuneEngine(File("src/main/assets/FreeFortuneContent.json").readText())
    private fun bundle(code: String) = NumerologyResultBundle(
        BirthInput(CalendarType.SOLAR, 1999, 3, 13, GenderOption.NONE),
        NumerologyNumbers(code[0].digitToInt(), code[1].digitToInt(), code[2].digitToInt(), code[3].digitToInt(), code),
        engine.content(code, GenderOption.NONE)
    )
    private fun context(prompt: String) = JSONObject(prompt.substringAfter("[상담 입력 JSON]").substringBefore("[입력 끝]").trim())
    private fun schema(prompt: String) = JSONObject(prompt.substringAfter("[출력 JSON 형식]").trim())

    @Test fun `all topics retain core and companion data and stable reader schema`() {
        val concern = "재직 중입니다. \"A회사\"에 지원할까요?\n퇴사부터 하기는 부담돼요."
        for (digit in 0..9) for (topic in PremiumTopic.entries) {
            val bundle = bundle("${digit}234")
            val prompt = personalPrompt(topic, concern, bundle)
            val data = context(prompt)
            val out = schema(prompt)
            assertEquals(concern, data.getString("concern"))
            assertEquals(digit, data.getJSONObject("person").getJSONObject("numbers").getInt("destiny"))
            assertEquals(2, data.getJSONObject("person").getJSONObject("numbers").getInt("early"))
            assertEquals(bundle.content.destinyProfile.summary, data.getJSONObject("person").getJSONObject("coreProfile").getString("summary"))
            val expected = PremiumMonthPlanner.pickBestMonth(topic, bundle.numbers, data.getInt("currentMonth")).toDisplayText()
            assertEquals(expected, out.getString("bestMonth"))
            val ids = (0 until out.getJSONArray("pages").length()).map { out.getJSONArray("pages").getJSONObject(it).getString("id") }
            assertEquals(listOf("current_flow", "good_energy", "caution", "opportunity", "avoid", "recommendation", "lucky_elements", "closing"), ids)
            assertEquals(8, out.getJSONArray("toc").length())
        }
    }

    @Test fun `blank question stays blank rather than becoming an invented concern`() {
        val data = context(personalPrompt(PremiumTopic.SELF_ESTEEM, "  ", bundle("4234")))
        assertFalse(data.getBoolean("hasConcern"))
        assertEquals("", data.getString("concern"))
    }

    @Test fun `AI month explanations survive parsing while wrong month values are corrected`() = runBlocking {
        var wrongMonths = false
        val service = AiConsultationService(object : JsonChatClient {
            override fun requestJsonContent(apiKey: String, model: String, systemPrompt: String, userPrompt: String, failureLabel: String, maxCompletionTokens: Int): String {
                val out = schema(userPrompt)
                out.put("bestMonthReason", "준비한 지원 서류를 점검하는 시기로 참고하세요.")
                out.put("riskyMonthReason", "회사에서 제시한 입사 조건을 확인하는 데 시간을 두세요.")
                if (wrongMonths) out.put("bestMonth", "잘못된 월").put("riskyMonth", "잘못된 월")
                out.getJSONObject("answerCard").put("shortAnswer", "현재 직장을 유지하며 지원부터 시작하는 편이 좋겠습니다.")
                val pages = out.getJSONArray("pages")
                for (index in 0 until pages.length()) {
                    pages.getJSONObject(index).put("highlight", "이직할 회사의 업무와 급여 조건을 확인하세요.")
                        .put("body", org.json.JSONArray().put("입사 제안을 받은 뒤 현재 회사의 퇴사 절차를 살펴보세요."))
                }
                return out.toString()
            }
        })
        val bundle = bundle("1234")
        val result = service.personal("fixture-only", PremiumTopic.CAREER, "재직 중 이직을 고민합니다.", bundle)
        assertEquals("준비한 지원 서류를 점검하는 시기로 참고하세요.", result.bestMonthReason)
        assertEquals("회사에서 제시한 입사 조건을 확인하는 데 시간을 두세요.", result.riskyMonthReason)
        wrongMonths = true
        val corrected = service.personal("fixture-only", PremiumTopic.CAREER, "재직 중 이직을 고민합니다.", bundle)
        assertEquals(result.bestMonth, corrected.bestMonth)
        assertEquals(result.riskyMonth, corrected.riskyMonth)
        assertNotEquals(result.bestMonthReason, corrected.bestMonthReason)
    }

    @Test fun `all relationship states preserve two profiles and their output contract`() {
        val a = bundle("8234")
        val b = bundle("1678")
        val concern = "상대가 \"시간이 필요하다\"고 했어요.\n연락을 기다릴까요?"
        for (status in CompatibilityRelationshipStatus.entries) {
            val prompt = compatibilityPrompt(a, b, concern, status, relationshipNumber(a, b))
            val data = context(prompt)
            val out = schema(prompt)
            assertEquals(concern, data.getString("concern"))
            assertEquals(8, data.getJSONObject("personA").getJSONObject("numbers").getInt("destiny"))
            assertEquals(1, data.getJSONObject("personB").getJSONObject("numbers").getInt("destiny"))
            assertEquals(9, data.getInt("relationshipNumber"))
            assertEquals(8, out.getJSONArray("pages").length())
            assertEquals(8, out.getJSONArray("toc").length())
            assertEquals(data.getString("riskyMonth"), out.getString("riskyMonth"))
        }
    }

    @Test fun `questioner role is explicit even when legacy gender ordering puts them second`() {
        val data = context(compatibilityPrompt(bundle("8234"), bundle("1678"), "제가 먼저 연락할까요?", CompatibilityRelationshipStatus.CRUSH, 9, requesterIsPersonA = false))
        assertEquals("personB", data.getString("requester"))
        assertEquals(1, data.getJSONObject(data.getString("requester")).getJSONObject("numbers").getInt("destiny"))
    }
}
