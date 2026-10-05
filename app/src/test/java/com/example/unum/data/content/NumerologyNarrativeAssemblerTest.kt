package com.example.unum.data.content

import com.example.unum.data.model.DestinyProfile
import com.example.unum.data.model.GenderOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NumerologyNarrativeAssemblerTest {
    @Test
    fun `all destiny and companion interactions use distinct behavioral scenes`() {
        val messages = (0..9).flatMap { destiny ->
            (0..9).map { companion -> NumerologyInteractionCatalog.describe(destiny, companion) }
        }

        assertEquals(100, messages.size)
        assertEquals(100, messages.toSet().size)
    }

    @Test
    fun `same destiny produces different reading when life numbers change`() {
        val profiles = (0..9).associateWith(::profile)
        val first = NumerologyNarrativeAssembler.build(
            code = "1234",
            gender = GenderOption.NONE,
            profile = profiles.getValue(1),
            earlyProfile = profiles.getValue(2),
            middleProfile = profiles.getValue(3),
            lateProfile = profiles.getValue(4)
        )
        val second = NumerologyNarrativeAssembler.build(
            code = "1567",
            gender = GenderOption.NONE,
            profile = profiles.getValue(1),
            earlyProfile = profiles.getValue(5),
            middleProfile = profiles.getValue(6),
            lateProfile = profiles.getValue(7)
        )

        assertEquals(first.destinyText, second.destinyText)
        assertNotEquals(first.interactionText, second.interactionText)
        assertNotEquals(first.earlyText, second.earlyText)
        assertNotEquals(first.middleText, second.middleText)
        assertNotEquals(first.lateText, second.lateText)
        assertEquals(first.workText, second.workText)
        assertEquals(first.moneyText, second.moneyText)
    }

    @Test
    fun `gender cue changes context without changing number interaction`() {
        val profiles = (0..9).associateWith(::profile)
        val male = build1234(profiles, GenderOption.MALE)
        val female = build1234(profiles, GenderOption.FEMALE)

        assertNotEquals(male.genderText, female.genderText)
        assertEquals(male.interactionText, female.interactionText)
        assertEquals(male.earlyText, female.earlyText)
        assertEquals(male.middleText, female.middleText)
        assertEquals(male.lateText, female.lateText)
    }

    private fun build1234(profiles: Map<Int, DestinyProfile>, gender: GenderOption) =
        NumerologyNarrativeAssembler.build(
            code = "1234",
            gender = gender,
            profile = profiles.getValue(1),
            earlyProfile = profiles.getValue(2),
            middleProfile = profiles.getValue(3),
            lateProfile = profiles.getValue(4)
        )

    private fun profile(number: Int) = DestinyProfile(
        destiny = number,
        title = "제목 $number",
        polarity = "중성",
        coreKeywords = listOf("핵심 $number"),
        cautionKeywords = listOf("주의 $number"),
        resultTitle = "결과 $number",
        summary = "요약 $number",
        strength = "강점 $number",
        caution = "주의점 $number",
        actionGuide = "행동 $number",
        earlyScene = "초기 장면 $number.",
        middleScene = "중기 장면 $number.",
        lateScene = "후기 장면 $number.",
        relationshipScene = "관계 장면 $number.",
        workScene = "업무 장면 $number.",
        moneyScene = "돈 장면 $number."
    )
}
