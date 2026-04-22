package com.example.unum.ui.components

import androidx.annotation.DrawableRes
import com.example.unum.R
import com.example.unum.data.model.PremiumTopic

object MascotArt {
    @get:DrawableRes
    val Home: Int = R.drawable.suri_scroll

    @get:DrawableRes
    val Input: Int = R.drawable.suri_scroll

    @get:DrawableRes
    val Result: Int = R.drawable.suri_tea

    @get:DrawableRes
    val Premium: Int = R.drawable.suri_writer

    @get:DrawableRes
    val PremiumLoading: Int = R.drawable.suri_writer

    @get:DrawableRes
    val Library: Int = R.drawable.suri_hanbok

    @get:DrawableRes
    val Reader: Int = R.drawable.suri_tea

    @get:DrawableRes
    val Settings: Int = R.drawable.suri_tea

    @get:DrawableRes
    val Story: Int = R.drawable.suri_scroll
}

@DrawableRes
fun premiumTopicMascot(topic: PremiumTopic): Int = when (topic) {
    PremiumTopic.ROMANCE -> R.drawable.suri_hanbok
    PremiumTopic.CAREER -> R.drawable.suri_writer
    PremiumTopic.MONEY -> R.drawable.suri_coins
    PremiumTopic.SELF_ESTEEM -> R.drawable.suri_tea
    PremiumTopic.RELATIONSHIP -> R.drawable.suri_scroll
}

@DrawableRes
fun premiumThemeMascot(theme: String): Int = when (theme.lowercase()) {
    PremiumTopic.ROMANCE.name.lowercase() -> R.drawable.suri_hanbok
    PremiumTopic.CAREER.name.lowercase() -> R.drawable.suri_writer
    PremiumTopic.MONEY.name.lowercase() -> R.drawable.suri_coins
    PremiumTopic.SELF_ESTEEM.name.lowercase() -> R.drawable.suri_tea
    PremiumTopic.RELATIONSHIP.name.lowercase() -> R.drawable.suri_scroll
    "compatibility" -> R.drawable.suri_hanbok
    else -> R.drawable.mascot_icon
}
