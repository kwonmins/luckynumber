package com.example.unum.ads

import android.app.Activity

class RewardedAdManager(private val activity: Activity) {
    fun load() = Unit

    fun showOrContinue(onRewarded: () -> Unit) {
        onRewarded()
    }
}
