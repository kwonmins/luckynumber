package com.example.unum.data.repository.user

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.StarWallet

interface RemoteUserDatabase {
    val isConfigured: Boolean

    suspend fun upsertUser(user: AuthUser)
    suspend fun loadFortuneBooks(userId: String): List<FortuneBook>
    suspend fun upsertFortuneBooks(userId: String, books: List<FortuneBook>)
    suspend fun deleteFortuneBook(userId: String, bookId: String)
    suspend fun loadStarWallet(userId: String): StarWallet?
    suspend fun upsertStarWallet(userId: String, wallet: StarWallet)
    suspend fun clearLocalSession()
}
