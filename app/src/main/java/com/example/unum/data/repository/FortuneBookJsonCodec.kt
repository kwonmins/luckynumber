package com.example.unum.data.repository

import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookChapter
import com.example.unum.data.model.FortuneBookType
import org.json.JSONArray
import org.json.JSONObject

object FortuneBookJsonCodec {
    fun encodeBooks(books: List<FortuneBook>): String {
        return JSONArray().also { array ->
            books.forEach { array.put(it.toJson()) }
        }.toString()
    }

    fun decodeBooks(raw: String): List<FortuneBook> {
        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).toBook())
            }
        }
    }

    fun encodeBook(book: FortuneBook): JSONObject = book.toJson()

    fun decodeBook(raw: String): FortuneBook = JSONObject(raw).toBook()

    private fun FortuneBook.toJson(): JSONObject = JSONObject()
        .put("bookId", bookId)
        .put("userId", userId)
        .put("code", code)
        .put("destiny", destiny)
        .put("early", early)
        .put("middle", middle)
        .put("late", late)
        .put("concernTopic", concernTopic)
        .put("concernText", concernText)
        .put("coverTitle", coverTitle)
        .put("coverSubtitle", coverSubtitle)
        .put("summary", summary)
        .put("bookType", bookType.name)
        .put("relationshipNumber", relationshipNumber)
        .put("maleBirthLabel", maleBirthLabel)
        .put("femaleBirthLabel", femaleBirthLabel)
        .put("maleDestiny", maleDestiny)
        .put("femaleDestiny", femaleDestiny)
        .put("maleCode", maleCode)
        .put("femaleCode", femaleCode)
        .put("bestMonth", bestMonth)
        .put("bestMonthReason", bestMonthReason)
        .put("riskyMonth", riskyMonth)
        .put("riskyMonthReason", riskyMonthReason)
        .put("chapters", JSONArray().also { array -> chapters.forEach { array.put(it.toJson()) } })
        .put("createdAt", createdAt)
        .put("lastOpenedAt", lastOpenedAt)
        .put("purchasedAt", purchasedAt)
        .put("isBookmarked", isBookmarked)
        .put("coverTheme", coverTheme)

    private fun FortuneBookChapter.toJson(): JSONObject = JSONObject()
        .put("title", title)
        .put("lead", lead)
        .put("body", JSONArray(body))
        .put("highlightQuote", highlightQuote)
        .put("actionTip", JSONArray(actionTip))

    private fun JSONObject.toBook(): FortuneBook = FortuneBook(
        bookId = getString("bookId"),
        userId = optString("userId").ifBlank { null },
        code = getString("code"),
        destiny = getInt("destiny"),
        early = getInt("early"),
        middle = getInt("middle"),
        late = getInt("late"),
        concernTopic = getString("concernTopic"),
        concernText = getString("concernText"),
        coverTitle = getString("coverTitle"),
        coverSubtitle = getString("coverSubtitle"),
        summary = getString("summary"),
        bookType = runCatching { FortuneBookType.valueOf(optString("bookType", FortuneBookType.PERSONAL.name)) }
            .getOrDefault(FortuneBookType.PERSONAL),
        relationshipNumber = optIntOrNull("relationshipNumber"),
        maleBirthLabel = optString("maleBirthLabel").ifBlank { null },
        femaleBirthLabel = optString("femaleBirthLabel").ifBlank { null },
        maleDestiny = optIntOrNull("maleDestiny"),
        femaleDestiny = optIntOrNull("femaleDestiny"),
        maleCode = optString("maleCode").ifBlank { null },
        femaleCode = optString("femaleCode").ifBlank { null },
        bestMonth = optString("bestMonth"),
        bestMonthReason = optString("bestMonthReason"),
        riskyMonth = optString("riskyMonth"),
        riskyMonthReason = optString("riskyMonthReason"),
        chapters = getJSONArray("chapters").toChapters(),
        createdAt = getLong("createdAt"),
        lastOpenedAt = optLongOrNull("lastOpenedAt"),
        purchasedAt = optLongOrNull("purchasedAt"),
        isBookmarked = optBoolean("isBookmarked"),
        coverTheme = optString("coverTheme", "soft")
    )

    private fun JSONArray.toChapters(): List<FortuneBookChapter> = buildList {
        for (index in 0 until length()) {
            val obj = getJSONObject(index)
            add(
                FortuneBookChapter(
                    title = obj.getString("title"),
                    lead = obj.getString("lead"),
                    body = obj.getJSONArray("body").toStringList(),
                    highlightQuote = obj.getString("highlightQuote"),
                    actionTip = obj.getJSONArray("actionTip").toStringList()
                )
            )
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) add(getString(index))
    }

    private fun JSONObject.optLongOrNull(name: String): Long? {
        return if (has(name) && !isNull(name)) optLong(name) else null
    }

    private fun JSONObject.optIntOrNull(name: String): Int? {
        return if (has(name) && !isNull(name)) optInt(name) else null
    }
}
