package com.example.unum.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val activity: Activity) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var pendingShow: PendingShow? = null

    fun load(onUnavailable: ((String) -> Unit)? = null) {
        if (!AdMobConfig.ADS_ENABLED) {
            onUnavailable?.invoke("광고 설정이 비어 있습니다. AdMob 앱 ID와 광고 단위 ID를 확인해주세요.")
            return
        }
        if (isLoading || rewardedAd != null) return

        isLoading = true
        RewardedAd.load(
            activity,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded: ${AdMobConfig.REWARDED_AD_UNIT_ID}")
                    rewardedAd = ad
                    isLoading = false
                    pendingShow?.let { pending ->
                        pendingShow = null
                        showOrContinue(
                            onRewarded = pending.onRewarded,
                            onLoading = pending.onLoading,
                            onUnavailable = pending.onUnavailable
                        )
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load. code=${error.code}, message=${error.message}")
                    rewardedAd = null
                    isLoading = false
                    val message = "광고 로드 실패(${error.code}): ${error.message}"
                    pendingShow?.let { pending ->
                        pendingShow = null
                        pending.onUnavailable(message)
                    } ?: onUnavailable?.invoke(message)
                }
            }
        )
    }

    fun showOrContinue(
        onRewarded: () -> Unit,
        onLoading: () -> Unit = {},
        onUnavailable: (String) -> Unit = {}
    ) {
        val ad = rewardedAd
        if (!AdMobConfig.ADS_ENABLED) {
            onUnavailable("광고 설정이 비어 있습니다. AdMob 앱 ID와 광고 단위 ID를 확인해주세요.")
            return
        }
        if (ad == null) {
            pendingShow = PendingShow(onRewarded, onLoading, onUnavailable)
            load(onUnavailable)
            onLoading()
            return
        }

        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded ad failed to show. code=${error.code}, message=${error.message}")
                load()
                onUnavailable("광고 표시 실패(${error.code}): ${error.message}")
            }
        }
        ad.show(activity) {
            onRewarded()
        }
    }

    private data class PendingShow(
        val onRewarded: () -> Unit,
        val onLoading: () -> Unit,
        val onUnavailable: (String) -> Unit
    )

    private companion object {
        const val TAG = "RewardedAdManager"
    }
}
