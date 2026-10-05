package com.example.unum.data.repository.books

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook

/** The screen sees book operations, not SharedPreferences or remote DB details. */
interface FortuneBookRepository {
    fun loadBooks(): List<FortuneBook>
    fun saveBooks(books: List<FortuneBook>)
    suspend fun synchronize(user: AuthUser, localBooks: List<FortuneBook>): List<FortuneBook>
    suspend fun pushBooks(userId: String, books: List<FortuneBook>)
    suspend fun deleteRemoteBook(userId: String, bookId: String)
}
