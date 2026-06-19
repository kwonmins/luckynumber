package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.StarWallet
import org.json.JSONObject

class StarWalletStore(context: Context) {
    private val prefs = context.getSharedPreferences("star_wallet", Context.MODE_PRIVATE)

    fun loadWallet(userId: String?): StarWallet {
        val key = keyFor(userId)
        val raw = prefs.getString(key, null) ?: return StarWallet(userId = userId)
        return runCatching {
            val json = JSONObject(raw)
            StarWallet(
                userId = json.optString("userId").ifBlank { userId },
                balance = json.optInt("balance", 0),
                lifetimeEarned = json.optInt("lifetimeEarned", 0),
                signupBonusClaimed = json.optBoolean("signupBonusClaimed", false),
                lastAttendanceDate = json.optString("lastAttendanceDate", ""),
                attendanceStreak = json.optInt("attendanceStreak", 0),
                lastAdRewardDate = json.optString("lastAdRewardDate", ""),
                dailyAdRewardCount = json.optInt("dailyAdRewardCount", 0),
                lastAdRewardAt = json.optLong("lastAdRewardAt", 0L),
                lastPremiumUseDate = json.optString("lastPremiumUseDate", ""),
                dailyPremiumUseCount = json.optInt("dailyPremiumUseCount", 0),
                referralCode = json.optString("referralCode", ""),
                referredBy = json.optString("referredBy").ifBlank { null },
                referralBonusClaimed = json.optBoolean("referralBonusClaimed", false),
                betaFeedbackRewardClaimed = json.optBoolean("betaFeedbackRewardClaimed", false),
                shareRewardCount = json.optInt("shareRewardCount", 0)
            )
        }.getOrDefault(StarWallet(userId = userId))
    }

    fun saveWallet(wallet: StarWallet) {
        prefs.edit()
            .putString(keyFor(wallet.userId), wallet.toJson().toString())
            .apply()
    }

    fun clearUserWallet(userId: String?) {
        prefs.edit().remove(keyFor(userId)).apply()
    }

    private fun keyFor(userId: String?): String = "wallet_${userId ?: "guest"}"

    private fun StarWallet.toJson(): JSONObject = JSONObject()
        .put("userId", userId)
        .put("balance", balance)
        .put("lifetimeEarned", lifetimeEarned)
        .put("signupBonusClaimed", signupBonusClaimed)
        .put("lastAttendanceDate", lastAttendanceDate)
        .put("attendanceStreak", attendanceStreak)
        .put("lastAdRewardDate", lastAdRewardDate)
        .put("dailyAdRewardCount", dailyAdRewardCount)
        .put("lastAdRewardAt", lastAdRewardAt)
        .put("lastPremiumUseDate", lastPremiumUseDate)
        .put("dailyPremiumUseCount", dailyPremiumUseCount)
        .put("referralCode", referralCode)
        .put("referredBy", referredBy)
        .put("referralBonusClaimed", referralBonusClaimed)
        .put("betaFeedbackRewardClaimed", betaFeedbackRewardClaimed)
        .put("shareRewardCount", shareRewardCount)
}
