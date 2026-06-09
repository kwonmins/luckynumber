package com.example.unum.data.repository.user

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook

class UserDataRepository(
    private val remoteDatabase: RemoteUserDatabase
) {
    val isRemoteConfigured: Boolean get() = remoteDatabase.isConfigured

    suspend fun prepareUser(user: AuthUser) {
        remoteDatabase.upsertUser(user)
    }

    suspend fun loadBooks(userId: String): List<FortuneBook> {
        return remoteDatabase.loadFortuneBooks(userId)
    }

    suspend fun saveBooks(userId: String, books: List<FortuneBook>) {
        remoteDatabase.upsertFortuneBooks(userId, books.map { it.copy(userId = userId) })
    }

    suspend fun deleteBook(userId: String, bookId: String) {
        remoteDatabase.deleteFortuneBook(userId, bookId)
    }
}
