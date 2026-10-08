package com.example.unum.data.repository

import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.RecentSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class LocalAssetNumerologyRepository(
    private val engineProvider: () -> com.example.unum.data.content.FreeFortuneEngine
) : NumerologyRepository {

    private val recentSearches = MutableStateFlow(emptyList<RecentSearch>())

    override suspend fun getContent(code: String, gender: GenderOption): NumerologyContent = withContext(Dispatchers.IO) {
        engineProvider().content(code, gender)
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

    override suspend fun clearRecentSearches() {
        recentSearches.emit(emptyList())
    }

}
