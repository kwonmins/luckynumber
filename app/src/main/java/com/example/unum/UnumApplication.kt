package com.example.unum

import android.app.Application
import com.example.unum.domain.ServiceLocator
import com.google.android.gms.ads.MobileAds

class UnumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        MobileAds.initialize(this) {}
    }
}
