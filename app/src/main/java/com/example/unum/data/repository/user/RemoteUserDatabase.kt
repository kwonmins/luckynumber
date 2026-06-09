package com.example.unum.data.repository.user

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook

interface RemoteUserDatabase {
    val isConfigured: Boolean

    suspend fun upsertUser(user: AuthUser)
    suspend fun loadFortuneBooks(userId: String): List<FortuneBook>
    suspend fun upsertFortuneBooks(userId: String, books: List<FortuneBook>)
    suspend fun deleteFortuneBook(userId: String, bookId: String)
}
