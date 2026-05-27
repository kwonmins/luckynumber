package com.example.unum.ads

import android.app.Activity

class InterstitialAdManager(private val activity: Activity) {
    fun load() = Unit

    fun showOrContinue(onContinue: () -> Unit) {
        onContinue()
    }
}
