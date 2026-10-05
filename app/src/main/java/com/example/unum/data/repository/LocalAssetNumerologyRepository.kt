package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.content.NumerologyNarrativeAssembler
import com.example.unum.data.model.DestinyProfile
import com.example.unum.data.model.GenderOption
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

class LocalAssetNumerologyRepository(
    private val context: Context
) : NumerologyRepository {

    private val mutex = Mutex()
    private var profilesLoaded = false
    private var destinyProfiles = emptyMap<Int, DestinyProfile>()
    private val recentSearches = MutableStateFlow(emptyList<RecentSearch>())

    override suspend fun getContent(code: String, gender: GenderOption): NumerologyContent {
        require(code.length == 4 && code.all(Char::isDigit)) {
            "올바른 4자리 code 형식이 아닙니다: $code"
        }

        ensureDestinyProfilesLoaded()

        val destiny = code[0].digitToInt()
        val early = code[1].digitToInt()
        val middle = code[2].digitToInt()
        val late = code[3].digitToInt()
        val profile = profileFor(destiny)
        val earlyProfile = profileFor(early)
        val middleProfile = profileFor(middle)
        val lateProfile = profileFor(late)

        return NumerologyContent(
            destinyProfile = profile,
            lifeRecord = NumerologyNarrativeAssembler.build(
                code = code,
                gender = gender,
                profile = profile,
                earlyProfile = earlyProfile,
                middleProfile = middleProfile,
                lateProfile = lateProfile
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

    private suspend fun ensureDestinyProfilesLoaded() {
        if (profilesLoaded) return

        mutex.withLock {
            if (profilesLoaded) return
            val loadedProfiles = withContext(Dispatchers.IO) {
                val raw = context.assets
                    .open("destiny_profiles.json")
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
                            resultTitle = obj.getString("resultTitle"),
                            summary = obj.getString("summary"),
                            strength = obj.getString("strength"),
                            caution = obj.getString("caution"),
                            actionGuide = obj.getString("actionGuide"),
                            earlyScene = obj.optString("earlyScene"),
                            middleScene = obj.optString("middleScene"),
                            lateScene = obj.optString("lateScene"),
                            relationshipScene = obj.optString("relationshipScene"),
                            workScene = obj.optString("workScene"),
                            moneyScene = obj.optString("moneyScene")
                        )
                        put(profile.destiny, profile)
                    }
                }.toMutableMap()
            }
            destinyProfiles = loadedProfiles
            profilesLoaded = true
        }
    }

    private fun profileFor(number: Int): DestinyProfile = destinyProfiles[number]
        ?: error("숫자 프로필이 없습니다: $number")

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (i in 0 until length()) add(getString(i))
    }

}
