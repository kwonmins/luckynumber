package com.example.unum.data.repository.books

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.repository.FortuneBookStore
import com.example.unum.data.repository.user.UserDataRepository
import com.example.unum.domain.books.FortuneBookPolicy

class DefaultFortuneBookRepository(
    private val localStore: FortuneBookStore,
    private val remote: UserDataRepository
) : FortuneBookRepository {
    override fun loadBooks(): List<FortuneBook> = localStore.loadBooks()

    override fun saveBooks(books: List<FortuneBook>) = localStore.saveBooks(books)

    override suspend fun synchronize(user: AuthUser, localBooks: List<FortuneBook>): List<FortuneBook> {
        remote.prepareUser(user)
        val remoteBooks = FortuneBookPolicy.sortBooks(remote.loadBooks(user.id).map {
            FortuneBookPolicy.refreshStoredMonthInsights(it.copy(userId = user.id))
        })
        val merged = FortuneBookPolicy.mergeBooks(localBooks, remoteBooks).map { it.copy(userId = user.id) }
        localStore.saveBooks(merged)
        remote.saveBooks(user.id, merged)
        return merged
    }

    override suspend fun pushBooks(userId: String, books: List<FortuneBook>) = remote.saveBooks(userId, books)

    override suspend fun deleteRemoteBook(userId: String, bookId: String) = remote.deleteBook(userId, bookId)
}
