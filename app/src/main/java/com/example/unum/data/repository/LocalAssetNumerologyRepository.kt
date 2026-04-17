package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.DestinyProfile
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

/**
 * 10,000건 전체를 한 번에 메모리로 올리지 않고,
 * code가 속한 1000건 chunk만 필요할 때 assets에서 읽는 저장소 구현.
 *
 * 예)
 * 0781 -> life_records_0000_0999.jsonl
 * 2335 -> life_records_2000_2999.jsonl
 * 7818 -> life_records_7000_7999.jsonl
 *
 * 확장 포인트:
 * - chunk 크기 변경
 * - LRU 캐시 개수 조절
 * - Room / SQLite 기반 인덱스 교체
 */
class LocalAssetNumerologyRepository(
    private val context: Context
) : NumerologyRepository {

    private val mutex = Mutex()

    @Volatile
    private var destinyLoaded = false

    private val destinyProfiles = mutableMapOf<Int, DestinyProfile>()

    /**
     * access-order=true 로 두어 최근 접근 chunk가 뒤로 가도록 구성.
     * 최대 CHUNK_CACHE_LIMIT개까지만 메모리에 유지.
     */
    private val chunkCache = LinkedHashMap<Int, Map<String, LifeRecord>>(CHUNK_CACHE_LIMIT, 0.75f, true)

    private val recentSearches = MutableStateFlow(emptyList<RecentSearch>())

    override suspend fun getContent(code: String): NumerologyContent {
        require(code.length == 4 && code.all(Char::isDigit)) {
            "올바른 4자리 code 형식이 아닙니다: $code"
        }

        ensureDestinyProfilesLoaded()
        val record = getLifeRecord(code)
        val destiny = destinyProfiles[record.destinyProfileKey]
            ?: error("운명수 프로필이 없습니다: ${record.destinyProfileKey}")

        return NumerologyContent(
            destinyProfile = destiny,
            lifeRecord = record
        )
    }

    override fun observeRecentSearches(): Flow<List<RecentSearch>> = recentSearches.asStateFlow()

    override suspend fun addRecentSearch(search: RecentSearch) {
        val next = buildList {
            add(search)
            addAll(recentSearches.value.filterNot { it.code == search.code && it.dateLabel == search.dateLabel })
        }.take(8)
        recentSearches.emit(next)
    }

    override suspend fun removeRecentSearch(search: RecentSearch) {
        recentSearches.emit(
            recentSearches.value.filterNot {
                it.code == search.code && it.dateLabel == search.dateLabel
            }
        )
    }

    private suspend fun ensureDestinyProfilesLoaded() {
        if (destinyLoaded) return

        mutex.withLock {
            if (destinyLoaded) return
            withContext(Dispatchers.IO) {
                val raw = context.assets
                    .open(DESTINY_FILE)
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }

                val array = JSONArray(raw)
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    val profile = DestinyProfile(
                        destiny = obj.getInt("destiny"),
                        title = obj.getString("title"),
                        polarity = obj.getString("polarity"),
                        coreKeywords = obj.getJSONArray("coreKeywords").toStringList(),
                        cautionKeywords = obj.getJSONArray("cautionKeywords").toStringList(),
                        destinyText = obj.getString("destinyText"),
                        oneLineAdvice = obj.getString("oneLineAdvice")
                    )
                    destinyProfiles[profile.destiny] = profile
                }
            }
            destinyLoaded = true
        }
    }

    private suspend fun getLifeRecord(code: String): LifeRecord {
        val chunkKey = chunkKeyFor(code)

        chunkCache[chunkKey]?.let { cachedChunk ->
            return cachedChunk[code] ?: error("chunk $chunkKey 에 code=$code 가 없습니다.")
        }

        val loadedChunk = mutex.withLock {
            chunkCache[chunkKey] ?: withContext(Dispatchers.IO) {
                val parsed = loadChunkFromAssets(chunkKey)
                chunkCache[chunkKey] = parsed
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
                val obj = JSONObject(line)
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

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (i in 0 until length()) add(getString(i))
    }

    companion object {
        private const val DESTINY_FILE = "destiny_profiles.json"
        private const val CHUNK_SIZE = 1000
        private const val CHUNK_CACHE_LIMIT = 3
    }
}
