package com.example.unum.data.repository

import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.FreeReadingPhrase
import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.RecentSearch
import kotlinx.coroutines.flow.Flow

interface NumerologyRepository {
    suspend fun getContent(code: String, gender: GenderOption = GenderOption.NONE): NumerologyContent
    suspend fun getFreeReadingPhrases(): List<FreeReadingPhrase>
    fun observeRecentSearches(): Flow<List<RecentSearch>>
    suspend fun addRecentSearch(search: RecentSearch)
    suspend fun removeRecentSearch(search: RecentSearch)
}
