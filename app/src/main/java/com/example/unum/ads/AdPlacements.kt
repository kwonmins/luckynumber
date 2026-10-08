package com.example.unum.ads

import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/** The switch precedes all SDK initialization, ad requests and AndroidView creation. */
@Composable
fun BannerAdPlacement() {
    if (!AdMobConfig.ADS_ENABLED || AdMobConfig.BANNER_AD_UNIT_ID.isBlank()) return
    val context=LocalContext.current
    val view=remember(context) { AdView(context).apply { adUnitId=AdMobConfig.BANNER_AD_UNIT_ID;setAdSize(AdSize.BANNER) } }
    LaunchedEffect(view) { MobileAds.initialize(context);view.loadAd(AdRequest.Builder().build()) }
    DisposableEffect(view) { onDispose { view.destroy() } }
    Column(Modifier.fillMaxWidth().padding(vertical=12.dp)) {
        Text("광고",style=MaterialTheme.typography.labelSmall)
        AndroidView(factory={view},modifier=Modifier.fillMaxWidth().height(50.dp))
    }
}

@Composable
fun NativeAdPlacement() {
    if (!AdMobConfig.ADS_ENABLED || AdMobConfig.NATIVE_AD_UNIT_ID.isBlank()) return
    val context=LocalContext.current
    var ad by remember { mutableStateOf<NativeAd?>(null) }
    DisposableEffect(context) {
        var disposed=false
        MobileAds.initialize(context)
        AdLoader.Builder(context,AdMobConfig.NATIVE_AD_UNIT_ID).forNativeAd { next ->
            if(disposed) next.destroy() else {ad?.destroy();ad=next}
        }.build().loadAd(AdRequest.Builder().build())
        onDispose { disposed=true;ad?.destroy() }
    }
    val native=ad ?: return
    AndroidView(factory={ctx ->
        NativeAdView(ctx).apply {
            val column=LinearLayout(ctx).apply { orientation=LinearLayout.VERTICAL;setPadding(24,24,24,24) }
            column.addView(TextView(ctx).apply {text="광고"})
            val title=TextView(ctx);column.addView(title);headlineView=title
            val body=TextView(ctx);column.addView(body);bodyView=body
            val image=ImageView(ctx);column.addView(image,LinearLayout.LayoutParams(72,72));iconView=image
            val button=Button(ctx);column.addView(button);callToActionView=button
            addView(column)
        }
    },update={view ->
        (view.headlineView as TextView).text=native.headline
        (view.bodyView as TextView).text=native.body.orEmpty()
        (view.iconView as ImageView).setImageDrawable(native.icon?.drawable)
        (view.callToActionView as Button).text=native.callToAction.orEmpty()
        view.setNativeAd(native)
    },modifier=Modifier.fillMaxWidth().padding(vertical=16.dp))
}
