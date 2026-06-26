package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.DestinyProfile
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.LifeRecord
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.RecentSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LocalAssetNumerologyRepository(
    private val context: Context
) : NumerologyRepository {

    private val mutex = Mutex()
    private val loadedVariants = mutableSetOf<DatasetVariant>()
    private val destinyProfilesByVariant = mutableMapOf<DatasetVariant, MutableMap<Int, DestinyProfile>>()
    private val recentSearches = MutableStateFlow(emptyList<RecentSearch>())

    override suspend fun getContent(code: String, gender: GenderOption): NumerologyContent {
        require(code.length == 4 && code.all(Char::isDigit)) {
            "올바른 4자리 code 형식이 아닙니다: $code"
        }

        val profileVariant = DatasetVariant.from(gender)
        ensureDestinyProfilesLoaded(profileVariant)

        val destiny = code[0].digitToInt()
        val early = code[1].digitToInt()
        val middle = code[2].digitToInt()
        val late = code[3].digitToInt()
        val profile = destinyProfilesByVariant[profileVariant]
            ?.get(destiny)
            ?: error("운명수 프로필이 없습니다: $destiny")

        return NumerologyContent(
            destinyProfile = profile,
            lifeRecord = buildLightLifeRecord(
                code = code,
                profile = profile,
                early = early,
                middle = middle,
                late = late
            )
        )
    }

    override fun observeRecentSearches(): Flow<List<RecentSearch>> = recentSearches.asStateFlow()

    override suspend fun addRecentSearch(search: RecentSearch) {
        val next = buildList {
            add(search)
            addAll(
                recentSearches.value.filterNot {
                    it.code == search.code &&
                        it.dateLabel == search.dateLabel &&
                        it.gender == search.gender &&
                        it.inputCalendarType == search.inputCalendarType
                }
            )
        }.take(8)
        recentSearches.emit(next)
    }

    override suspend fun removeRecentSearch(search: RecentSearch) {
        recentSearches.emit(
            recentSearches.value.filterNot {
                it.code == search.code &&
                    it.dateLabel == search.dateLabel &&
                    it.gender == search.gender &&
                    it.inputCalendarType == search.inputCalendarType
            }
        )
    }

    private suspend fun ensureDestinyProfilesLoaded(variant: DatasetVariant) {
        if (variant in loadedVariants) return

        mutex.withLock {
            if (variant in loadedVariants) return
            val loadedProfiles = withContext(Dispatchers.IO) {
                val raw = context.assets
                    .open(destinyFileName(variant))
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText().trimStart('\uFEFF') }

                val array = JSONArray(raw)
                buildMap {
                    for (index in 0 until array.length()) {
                        val obj = array.getJSONObject(index)
                        val profile = DestinyProfile(
                            destiny = obj.getInt("destiny"),
                            title = obj.getString("title"),
                            polarity = obj.getString("polarity"),
                            coreKeywords = obj.getJSONArray("coreKeywords").toStringList(),
                            cautionKeywords = obj.getJSONArray("cautionKeywords").toStringList(),
                            destinyText = obj.optString("destinyText")
                                .ifBlank { obj.composedDestinyText() },
                            oneLineAdvice = obj.getString("oneLineAdvice")
                        )
                        put(profile.destiny, profile)
                    }
                }.toMutableMap()
            }
            destinyProfilesByVariant[variant] = loadedProfiles
            loadedVariants += variant
        }
    }

    private fun buildLightLifeRecord(
        code: String,
        profile: DestinyProfile,
        early: Int,
        middle: Int,
        late: Int
    ): LifeRecord {
        val destinyTone = numberTone(profile.destiny)
        val earlyTone = numberTone(early)
        val middleTone = numberTone(middle)
        val lateTone = numberTone(late)
        val tones = listOf(destinyTone, earlyTone, middleTone, lateTone)
        val keywords = (profile.coreKeywords + tones.flatMap { it.keywords }).distinct().take(8)
        val cautionKeywords = (profile.cautionKeywords + tones.flatMap { it.cautionKeywords }).distinct().take(8)
        val destinyText = "${profile.title}의 핵심은 ${destinyTone.role}입니다. ${destinyTone.strength}이 장점으로 드러나고, ${destinyTone.caution}."
        val summaryText = "초년에는 ${earlyTone.name}, 중년에는 ${middleTone.name}, 말년에는 ${lateTone.name}의 결이 이어집니다. 높고 낮은 점수보다 시기마다 달라지는 역할의 흐름으로 읽는 편이 자연스럽습니다."

        return LifeRecord(
            code = code,
            destiny = profile.destiny,
            early = early,
            middle = middle,
            late = late,
            destinyProfileKey = profile.destiny,
            lifeTitle = "운명수 ${profile.destiny} 기본 리포트",
            destinyText = destinyText,
            earlyText = phaseText("초년", earlyTone, "관계와 습관이 자리 잡기 시작하는 시기"),
            middleText = phaseText("중년", middleTone, "일과 책임, 선택의 폭이 넓어지는 시기"),
            lateText = phaseText("말년", lateTone, "관계와 삶의 기준이 더 선명해지는 시기"),
            lifeText = "$destinyText $summaryText",
            summaryText = summaryText,
            keywords = keywords,
            cautionKeywords = cautionKeywords,
            oneLineAdvice = "${destinyTone.name}의 장점은 ${destinyTone.strength}에 있고, 주의할 점은 ${destinyTone.caution}입니다."
        )
    }

    private fun phaseText(phase: String, tone: NumberTone, context: String): String {
        return "${phase}에는 ${tone.name}의 흐름이 비교적 선명합니다. $context 안에서 ${tone.strength}이 강점으로 보이고, ${tone.caution}."
    }

    private fun destinyFileName(variant: DatasetVariant): String {
        return when (variant) {
            DatasetVariant.NEUTRAL -> "destiny_profiles.json"
            DatasetVariant.MALE,
            DatasetVariant.FEMALE -> "destiny_profiles_gendered.json"
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (i in 0 until length()) add(getString(i))
    }

    private fun JSONObject.composedDestinyText(): String {
        return listOf(
            optString("summary"),
            optString("strength"),
            optString("caution")
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }

    private fun numberTone(number: Int): NumberTone = when (number) {
        0 -> NumberTone(
            name = "가능성",
            role = "아직 고정되지 않은 흐름을 열어두는 결",
            strength = "새로운 방향을 받아들이는 여백",
            caution = "방향이 흐려지면 선택이 늦어질 수 있습니다",
            keywords = listOf("가능성", "전환", "여백"),
            cautionKeywords = listOf("방향 미정", "선택 지연")
        )
        1 -> NumberTone(
            name = "시작",
            role = "먼저 움직이며 길을 여는 결",
            strength = "결정력과 독립성",
            caution = "속도가 앞서면 혼자 밀어붙이는 인상이 생길 수 있습니다",
            keywords = listOf("시작", "독립", "결정"),
            cautionKeywords = listOf("성급함", "독단")
        )
        2 -> NumberTone(
            name = "조율",
            role = "사람과 분위기의 온도를 섬세하게 읽는 결",
            strength = "협력과 감정 감각",
            caution = "눈치가 과해지면 마음이 쉽게 누적될 수 있습니다",
            keywords = listOf("조율", "협력", "감정선"),
            cautionKeywords = listOf("눈치 과다", "감정 누적")
        )
        3 -> NumberTone(
            name = "표현",
            role = "생각과 감정을 말과 글로 풀어내는 결",
            strength = "표현력과 해석력",
            caution = "말이 빨라지면 오해가 생기기 쉽습니다",
            keywords = listOf("표현", "해석", "설득"),
            cautionKeywords = listOf("말의 과속", "해석 과잉")
        )
        4 -> NumberTone(
            name = "구조",
            role = "흐트러진 일을 안정된 기준으로 세우는 결",
            strength = "질서와 지속성",
            caution = "기준이 단단해질수록 경직되어 보일 수 있습니다",
            keywords = listOf("구조", "질서", "지속성"),
            cautionKeywords = listOf("경직", "통제감")
        )
        5 -> NumberTone(
            name = "변화",
            role = "새로운 자극과 기회를 빠르게 감지하는 결",
            strength = "확장성과 실험 감각",
            caution = "관심이 흩어지면 마무리가 약해질 수 있습니다",
            keywords = listOf("변화", "확장", "실험"),
            cautionKeywords = listOf("산만함", "무리한 확장")
        )
        6 -> NumberTone(
            name = "책임",
            role = "맡은 일을 끝까지 돌보고 정리하는 결",
            strength = "현실감과 책임감",
            caution = "부담을 혼자 안으면 피로가 빠르게 쌓일 수 있습니다",
            keywords = listOf("책임", "관리", "현실감"),
            cautionKeywords = listOf("책임 과다", "피로 누적")
        )
        7 -> NumberTone(
            name = "집중",
            role = "한 가지 흐름을 깊게 파고드는 결",
            strength = "분석력과 몰입",
            caution = "몰입이 깊어지면 고립감이 커질 수 있습니다",
            keywords = listOf("집중", "분석", "몰입"),
            cautionKeywords = listOf("고립", "집착")
        )
        8 -> NumberTone(
            name = "연결",
            role = "사람과 자원을 현실적으로 묶어내는 결",
            strength = "대인 감각과 추진력",
            caution = "평판을 의식하면 관계가 소모적으로 느껴질 수 있습니다",
            keywords = listOf("연결", "평판", "자원"),
            cautionKeywords = listOf("평판 의식", "관계 소모")
        )
        9 -> NumberTone(
            name = "완성",
            role = "흩어진 경험을 의미 있는 흐름으로 정리하는 결",
            strength = "마무리와 의미화",
            caution = "미련이 길어지면 다음 흐름으로 넘어가기 어렵습니다",
            keywords = listOf("완성", "정리", "의미화"),
            cautionKeywords = listOf("미련", "마감 지연")
        )
        else -> NumberTone(
            name = "흐름",
            role = "현재의 흐름을 읽는 결",
            strength = "상황을 살피는 감각",
            caution = "흐름이 과해지면 균형이 흔들릴 수 있습니다",
            keywords = listOf("흐름"),
            cautionKeywords = listOf("균형")
        )
    }

    private data class NumberTone(
        val name: String,
        val role: String,
        val strength: String,
        val caution: String,
        val keywords: List<String>,
        val cautionKeywords: List<String>
    )

    private enum class DatasetVariant {
        NEUTRAL,
        MALE,
        FEMALE;

        companion object {
            fun from(gender: GenderOption): DatasetVariant = when (gender) {
                GenderOption.MALE -> MALE
                GenderOption.FEMALE -> FEMALE
                GenderOption.NONE -> NEUTRAL
            }
        }
    }
}
