package com.example.unum.data.repository

import com.example.unum.data.model.NumerologyContent
import com.example.unum.data.model.RecentSearch
import kotlinx.coroutines.flow.Flow

interface NumerologyRepository {
    suspend fun getContent(code: String): NumerologyContent
    fun observeRecentSearches(): Flow<List<RecentSearch>>
    suspend fun addRecentSearch(search: RecentSearch)
    suspend fun removeRecentSearch(search: RecentSearch)
}
