package com.example.unum.domain.books

import com.example.unum.data.model.FortuneBook
import org.junit.Assert.assertEquals
import org.junit.Test

class FortuneBookPolicyTest {
    @Test
    fun `merge keeps newer remote copy and retains distinct local books`() {
        val local = book("shared", 10)
        val remote = local.copy(lastOpenedAt = 30, summary = "remote")
        val distinct = book("local", 20)
        val merged = FortuneBookPolicy.mergeBooks(listOf(local, distinct), listOf(remote))
        assertEquals(listOf(remote, distinct), merged)
    }

    @Test
    fun `equal timestamps keep local edits without duplicate books`() {
        val remote = book("shared", 10)
        val local = remote.copy(isBookmarked = true)
        assertEquals(listOf(local), FortuneBookPolicy.mergeBooks(listOf(local), listOf(remote)))
    }

    @Test
    fun `sort uses creation time to break equal opened time`() {
        val older = book("older", 10).copy(lastOpenedAt = 30)
        val newer = book("newer", 20).copy(lastOpenedAt = 30)
        assertEquals(listOf(newer, older), FortuneBookPolicy.sortBooks(listOf(older, newer)))
    }

    private fun book(id: String, createdAt: Long) = FortuneBook(
        bookId = id, code = "1234", destiny = 1, early = 2, middle = 3, late = 4,
        concernTopic = "", concernText = "", coverTitle = "", coverSubtitle = "",
        summary = "", chapters = emptyList(), createdAt = createdAt
    )
}
