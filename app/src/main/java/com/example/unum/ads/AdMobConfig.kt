package com.example.unum.ads

import com.example.unum.BuildConfig

object AdMobConfig {
    const val ADS_ENABLED = false
    val BANNER_AD_UNIT_ID = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    const val INTERSTITIAL_AD_UNIT_ID = ""
    val REWARDED_AD_UNIT_ID = BuildConfig.ADMOB_REWARDED_AD_UNIT_ID
    val NATIVE_AD_UNIT_ID = BuildConfig.ADMOB_NATIVE_AD_UNIT_ID
}
