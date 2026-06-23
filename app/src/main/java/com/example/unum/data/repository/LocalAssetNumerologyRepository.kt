package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.DestinyProfile
import com.example.unum.data.model.FreeReadingPhrase
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
import java.util.LinkedHashMap

class LocalAssetNumerologyRepository(
    private val context: Context
) : NumerologyRepository {

    private val mutex = Mutex()
    private val loadedVariants = mutableSetOf<DatasetVariant>()
    private val destinyProfilesByVariant = mutableMapOf<DatasetVariant, MutableMap<Int, DestinyProfile>>()
    private var freeReadingPhrases: List<FreeReadingPhrase>? = null

    /**
     * access-order=true 로 두어 최근 접근 chunk가 뒤로 가도록 구성.
     * 최대 CHUNK_CACHE_LIMIT개까지만 메모리에 유지합니다.
     */
    private val chunkCache = LinkedHashMap<String, Map<String, LifeRecord>>(CHUNK_CACHE_LIMIT, 0.75f, true)
    private val recentSearches = MutableStateFlow(emptyList<RecentSearch>())

    override suspend fun getContent(code: String, gender: GenderOption): NumerologyContent {
        require(code.length == 4 && code.all(Char::isDigit)) {
            "올바른 4자리 code 형식이 아닙니다: $code"
        }

        val profileVariant = DatasetVariant.from(gender)
        ensureDestinyProfilesLoaded(profileVariant)
        val record = getLifeRecord(code)
        val destiny = destinyProfilesByVariant[profileVariant]
            ?.get(record.destinyProfileKey)
            ?: error("운명수 프로필이 없습니다: ${record.destinyProfileKey}")

        return NumerologyContent(
            destinyProfile = destiny,
            lifeRecord = record
        )
    }

    override fun observeRecentSearches(): Flow<List<RecentSearch>> = recentSearches.asStateFlow()

    override suspend fun getFreeReadingPhrases(): List<FreeReadingPhrase> {
        freeReadingPhrases?.let { return it }

        return mutex.withLock {
            freeReadingPhrases ?: withContext(Dispatchers.IO) {
                val raw = context.assets
                    .open(FREE_READING_PHRASES_FILE)
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText().trimStart('\uFEFF') }
                val array = JSONArray(raw)
                buildList {
                    for (index in 0 until array.length()) {
                        val obj = array.getJSONObject(index)
                        add(
                            FreeReadingPhrase(
                                id = obj.getString("id"),
                                category = obj.getString("category"),
                                number = obj.phraseNumberOrNull(),
                                polarity = obj.getString("polarity"),
                                tone = obj.getString("tone"),
                                intensity = obj.getString("intensity"),
                                keywords = obj.getJSONArray("keywords").toStringList(),
                                text = obj.getString("text"),
                                avoidWith = obj.getJSONArray("avoidWith").toStringList()
                            )
                        )
                    }
                }
            }.also { freeReadingPhrases = it }
        }
    }

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

    private suspend fun getLifeRecord(code: String): LifeRecord {
        val chunkKey = chunkKeyFor(code)
        val cacheKey = "life:$chunkKey"

        chunkCache[cacheKey]?.let { cachedChunk ->
            return cachedChunk[code] ?: error("chunk $cacheKey 에 code=$code 가 없습니다.")
        }

        val loadedChunk = mutex.withLock {
            chunkCache[cacheKey] ?: withContext(Dispatchers.IO) {
                val parsed = loadChunkFromAssets(chunkKey)
                chunkCache[cacheKey] = parsed
                trimChunkCacheIfNeeded()
                parsed
            }
        }

        return loadedChunk[code] ?: error("로컬 데이터셋에 code=$code 가 없습니다.")
    }

    private fun loadChunkFromAssets(chunkKey: Int): Map<String, LifeRecord> {
        val fileName = chunkFileName(chunkKey)
        val map = LinkedHashMap<String, LifeRecord>(CHUNK_SIZE)

        context.assets.open(fileName).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter { it.isNotBlank() }.forEach { line ->
                val obj = JSONObject(line.trimStart('\uFEFF'))
                val record = LifeRecord(
                    code = obj.getString("code"),
                    destiny = obj.getInt("destiny"),
                    early = obj.getInt("early"),
                    middle = obj.getInt("middle"),
                    late = obj.getInt("late"),
                    destinyProfileKey = obj.getInt("destinyProfileKey"),
                    lifeTitle = obj.getString("lifeTitle"),
                    earlyText = obj.getString("earlyText"),
                    middleText = obj.getString("middleText"),
                    lateText = obj.getString("lateText"),
                    lifeText = obj.getString("lifeText"),
                    summaryText = obj.getString("summaryText"),
                    keywords = obj.getJSONArray("keywords").toStringList(),
                    cautionKeywords = obj.getJSONArray("cautionKeywords").toStringList(),
                    oneLineAdvice = obj.getString("oneLineAdvice")
                )
                map[record.code] = record
            }
        }

        return map
    }

    private fun trimChunkCacheIfNeeded() {
        while (chunkCache.size > CHUNK_CACHE_LIMIT) {
            val oldestKey = chunkCache.entries.iterator().next().key
            chunkCache.remove(oldestKey)
        }
    }

    private fun chunkKeyFor(code: String): Int {
        val numericCode = code.toIntOrNull() ?: error("숫자 code 가 아닙니다: $code")
        return numericCode / CHUNK_SIZE
    }

    private fun chunkFileName(chunkKey: Int): String {
        val start = chunkKey * CHUNK_SIZE
        val end = start + (CHUNK_SIZE - 1)
        return "life_records_%04d_%04d.jsonl".format(start, end)
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
            optString("caution"),
            optString("actionGuide")
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }

    private fun JSONObject.phraseNumberOrNull(): Int? {
        val value = opt("number") ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private enum class DatasetVariant(val key: String) {
        NEUTRAL("neutral"),
        MALE("male"),
        FEMALE("female");

        companion object {
            fun from(gender: GenderOption): DatasetVariant = when (gender) {
                GenderOption.MALE -> MALE
                GenderOption.FEMALE -> FEMALE
                GenderOption.NONE -> NEUTRAL
            }
        }
    }

    companion object {
        private const val CHUNK_SIZE = 1000
        private const val CHUNK_CACHE_LIMIT = 6
        private const val FREE_READING_PHRASES_FILE = "free_reading_phrases.json"
    }
}
