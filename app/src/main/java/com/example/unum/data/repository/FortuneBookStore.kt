package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.FortuneBook

class FortuneBookStore(context: Context) {
    private val prefs = context.getSharedPreferences("fortune_books", Context.MODE_PRIVATE)

    fun loadBooks(): List<FortuneBook> {
        val raw = prefs.getString(KEY_BOOKS, null) ?: return emptyList()
        return runCatching { FortuneBookJsonCodec.decodeBooks(raw) }.getOrDefault(emptyList())
    }

    fun saveBooks(books: List<FortuneBook>) {
        prefs.edit().putString(KEY_BOOKS, FortuneBookJsonCodec.encodeBooks(books)).apply()
    }

    private companion object {
        const val KEY_BOOKS = "books"
    }
}
