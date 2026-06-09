package com.example.unum.data.repository.user

import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.repository.FortuneBookJsonCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.time.Instant

class SupabaseRestUserDatabase(
    private val supabaseUrl: String,
    private val anonKey: String
) : RemoteUserDatabase {

    override val isConfigured: Boolean = supabaseUrl.isNotBlank() && anonKey.isNotBlank()

    override suspend fun upsertUser(user: AuthUser) {
        if (!isConfigured) return
        request(
            method = "POST",
            path = "/rest/v1/app_users",
            query = "on_conflict=id",
            prefer = "resolution=merge-duplicates",
            userId = user.id,
            body = JSONObject()
                .put("id", user.id)
                .put("provider", user.provider.name.lowercase())
                .put("provider_user_id", user.providerUserId)
                .put("display_name", user.displayName)
                .put("email", user.email)
                .put("avatar_url", user.avatarUrl)
                .put("last_login_at", Instant.now().toString())
                .toString()
        )
    }

    override suspend fun loadFortuneBooks(userId: String): List<FortuneBook> {
        if (!isConfigured) return emptyList()
        val raw = request(
            method = "GET",
            path = "/rest/v1/fortune_books",
            query = "user_id=eq.${userId.urlEncode()}&select=book_json",
            userId = userId
        )
        val array = JSONArray(raw.ifBlank { "[]" })
        return buildList {
            for (index in 0 until array.length()) {
                val obj = array.getJSONObject(index)
                val bookJson = obj.getJSONObject("book_json").toString()
                add(FortuneBookJsonCodec.decodeBook(bookJson))
            }
        }
    }

    override suspend fun upsertFortuneBooks(userId: String, books: List<FortuneBook>) {
        if (!isConfigured || books.isEmpty()) return
        val rows = JSONArray()
        books.forEach { book ->
            val ownedBook = book.copy(userId = userId)
            rows.put(
                JSONObject()
                    .put("book_id", ownedBook.bookId)
                    .put("user_id", userId)
                    .put("book_type", ownedBook.bookType.name.lowercase())
                    .put("cover_title", ownedBook.coverTitle)
                    .put("updated_at", Instant.now().toString())
                    .put("book_json", FortuneBookJsonCodec.encodeBook(ownedBook))
            )
        }
        request(
            method = "POST",
            path = "/rest/v1/fortune_books",
            query = "on_conflict=book_id",
            prefer = "resolution=merge-duplicates",
            userId = userId,
            body = rows.toString()
        )
    }

    override suspend fun deleteFortuneBook(userId: String, bookId: String) {
        if (!isConfigured) return
        request(
            method = "DELETE",
            path = "/rest/v1/fortune_books",
            query = "book_id=eq.${bookId.urlEncode()}&user_id=eq.${userId.urlEncode()}",
            userId = userId
        )
    }

    private suspend fun request(
        method: String,
        path: String,
        query: String = "",
        prefer: String? = null,
        userId: String? = null,
        body: String? = null
    ): String = withContext(Dispatchers.IO) {
        val separator = if (query.isBlank()) "" else "?$query"
        val connection = (URL("$supabaseUrl$path$separator").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            userId?.let { setRequestProperty("x-unum-user-id", it) }
            prefer?.let { setRequestProperty("Prefer", it) }
            if (body != null) {
                doOutput = true
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(body) }
            }
        }

        val responseCode = connection.responseCode
        val raw = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        connection.disconnect()

        if (responseCode !in 200..299) {
            error("Supabase 요청 실패: $method $path ($responseCode) $raw")
        }
        raw
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    private companion object {
        const val NETWORK_TIMEOUT_MS = 15_000
    }
}
