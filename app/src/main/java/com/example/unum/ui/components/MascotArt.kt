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
    val Premium: Int = R.drawable.suri_anim_consult_07

    @get:DrawableRes
    val PremiumLoading: Int = R.drawable.suri_anim_writer_hero

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
    PremiumTopic.ROMANCE -> R.drawable.suri_anim_romance_hero
    PremiumTopic.CAREER -> R.drawable.suri_anim_writer_hero
    PremiumTopic.MONEY -> R.drawable.suri_anim_money_01
    PremiumTopic.SELF_ESTEEM -> R.drawable.suri_anim_numbers_hero
    PremiumTopic.RELATIONSHIP -> R.drawable.suri_anim_consult_07
}

@DrawableRes
fun premiumThemeMascot(theme: String): Int = when (theme.lowercase()) {
    PremiumTopic.ROMANCE.name.lowercase() -> R.drawable.suri_anim_romance_hero
    PremiumTopic.CAREER.name.lowercase() -> R.drawable.suri_anim_writer_hero
    PremiumTopic.MONEY.name.lowercase() -> R.drawable.suri_anim_money_01
    PremiumTopic.SELF_ESTEEM.name.lowercase() -> R.drawable.suri_anim_numbers_hero
    PremiumTopic.RELATIONSHIP.name.lowercase() -> R.drawable.suri_anim_consult_07
    "compatibility" -> R.drawable.suri_anim_romance_hero
    else -> R.drawable.mascot_icon
}
