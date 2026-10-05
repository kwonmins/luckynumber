package com.example.unum

import android.app.Application
import com.example.unum.di.AppContainer
import com.kakao.sdk.common.KakaoSdk

class UnumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
        AppContainer.init(this)
    }
}
