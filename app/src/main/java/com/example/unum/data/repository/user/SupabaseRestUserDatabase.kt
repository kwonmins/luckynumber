package com.example.unum.data.repository.user

import android.content.Context
import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.StarWallet
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
    context: Context,
    private val supabaseUrl: String,
    private val anonKey: String
) : RemoteUserDatabase {

    private val prefs = context.getSharedPreferences("supabase_app_sessions", Context.MODE_PRIVATE)
    private var activeSessionToken: String? = null

    override val isConfigured: Boolean = supabaseUrl.isNotBlank() && anonKey.isNotBlank()

    override suspend fun upsertUser(user: AuthUser) {
        if (!isConfigured) return

        val accessToken = user.accessToken
        if (!accessToken.isNullOrBlank()) {
            val session = openKakaoSession(accessToken)
            activeSessionToken = session.sessionToken
            prefs.edit()
                .putString(sessionKey(session.userId), session.sessionToken)
                .apply()
            return
        }

        activeSessionToken = prefs.getString(sessionKey(user.id), null)
            ?: error("로그인 보안 세션이 만료되었습니다. 카카오로 다시 로그인해주세요.")
    }

    override suspend fun loadFortuneBooks(userId: String): List<FortuneBook> {
        if (!isConfigured) return emptyList()
        ensureAppSessionFor(userId)
        val raw = request(
            method = "GET",
            path = "/rest/v1/fortune_books",
            query = "user_id=eq.${userId.urlEncode()}&select=book_json"
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
        ensureAppSessionFor(userId)
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
            body = rows.toString()
        )
    }

    override suspend fun deleteFortuneBook(userId: String, bookId: String) {
        if (!isConfigured) return
        ensureAppSessionFor(userId)
        request(
            method = "DELETE",
            path = "/rest/v1/fortune_books",
            query = "book_id=eq.${bookId.urlEncode()}&user_id=eq.${userId.urlEncode()}"
        )
    }

    override suspend fun loadStarWallet(userId: String): StarWallet? {
        if (!isConfigured) return null
        ensureAppSessionFor(userId)
        val raw = request(
            method = "GET",
            path = "/rest/v1/star_wallets",
            query = "user_id=eq.${userId.urlEncode()}&select=*"
        )
        val array = JSONArray(raw.ifBlank { "[]" })
        if (array.length() == 0) return null
        return array.getJSONObject(0).toStarWallet()
    }

    override suspend fun upsertStarWallet(userId: String, wallet: StarWallet) {
        if (!isConfigured) return
        ensureAppSessionFor(userId)
        val body = JSONArray()
            .put(
                JSONObject()
                    .put("user_id", userId)
                    .put("balance", wallet.balance)
                    .put("lifetime_earned", wallet.lifetimeEarned)
                    .put("signup_bonus_claimed", wallet.signupBonusClaimed)
                    .put("last_attendance_date", wallet.lastAttendanceDate.ifBlank { JSONObject.NULL })
                    .put("attendance_streak", wallet.attendanceStreak)
                    .put("last_ad_reward_date", wallet.lastAdRewardDate.ifBlank { JSONObject.NULL })
                    .put("daily_ad_reward_count", wallet.dailyAdRewardCount)
                    .put("last_ad_reward_at", wallet.lastAdRewardAt.takeIf { it > 0L } ?: JSONObject.NULL)
                    .put("last_premium_use_date", wallet.lastPremiumUseDate.ifBlank { JSONObject.NULL })
                    .put("daily_premium_use_count", wallet.dailyPremiumUseCount)
                    .put("referral_code", wallet.referralCode.ifBlank { JSONObject.NULL })
                    .put("referred_by", wallet.referredBy ?: JSONObject.NULL)
                    .put("referral_bonus_claimed", wallet.referralBonusClaimed)
                    .put("beta_feedback_reward_claimed", wallet.betaFeedbackRewardClaimed)
                    .put("share_reward_count", wallet.shareRewardCount)
                    .put("updated_at", Instant.now().toString())
            )
        request(
            method = "POST",
            path = "/rest/v1/star_wallets",
            query = "on_conflict=user_id",
            prefer = "resolution=merge-duplicates",
            body = body.toString()
        )
    }

    override suspend fun clearLocalSession() {
        activeSessionToken = null
        prefs.edit().clear().apply()
    }

    private suspend fun openKakaoSession(accessToken: String): RemoteSession = withContext(Dispatchers.IO) {
        val raw = request(
            method = "POST",
            path = "/functions/v1/kakao-session",
            body = JSONObject().put("accessToken", accessToken).toString(),
            requiresAppSession = false
        )
        val json = JSONObject(raw)
        val user = json.getJSONObject("user")
        RemoteSession(
            userId = user.getString("id"),
            sessionToken = json.getString("sessionToken")
        )
    }

    private suspend fun request(
        method: String,
        path: String,
        query: String = "",
        prefer: String? = null,
        body: String? = null,
        requiresAppSession: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val sessionToken = activeSessionToken
        if (requiresAppSession && sessionToken.isNullOrBlank()) {
            error("로그인 보안 세션이 없습니다. 카카오로 다시 로그인해주세요.")
        }

        val separator = if (query.isBlank()) "" else "?$query"
        val connection = (URL("$supabaseUrl$path$separator").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (requiresAppSession) {
                setRequestProperty("x-unum-session-token", sessionToken)
            }
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

    private fun sessionKey(userId: String): String = "session_token_$userId"

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun JSONObject.toStarWallet(): StarWallet {
        return StarWallet(
            userId = optString("user_id").ifBlank { null },
            balance = optInt("balance", 0),
            lifetimeEarned = optInt("lifetime_earned", 0),
            signupBonusClaimed = optBoolean("signup_bonus_claimed", false),
            lastAttendanceDate = optString("last_attendance_date", ""),
            attendanceStreak = optInt("attendance_streak", 0),
            lastAdRewardDate = optString("last_ad_reward_date", ""),
            dailyAdRewardCount = optInt("daily_ad_reward_count", 0),
            lastAdRewardAt = optLong("last_ad_reward_at", 0L),
            lastPremiumUseDate = optString("last_premium_use_date", ""),
            dailyPremiumUseCount = optInt("daily_premium_use_count", 0),
            referralCode = optString("referral_code", ""),
            referredBy = optString("referred_by").ifBlank { null },
            referralBonusClaimed = optBoolean("referral_bonus_claimed", false),
            betaFeedbackRewardClaimed = optBoolean("beta_feedback_reward_claimed", false),
            shareRewardCount = optInt("share_reward_count", 0)
        )
    }

    private fun ensureAppSessionFor(userId: String) {
        if (!activeSessionToken.isNullOrBlank()) return
        activeSessionToken = prefs.getString(sessionKey(userId), null)
        if (activeSessionToken.isNullOrBlank()) {
            error("로그인 보안 세션이 없습니다. 카카오로 다시 로그인해주세요.")
        }
    }

    private data class RemoteSession(
        val userId: String,
        val sessionToken: String
    )

    private companion object {
        const val NETWORK_TIMEOUT_MS = 15_000
    }
}
