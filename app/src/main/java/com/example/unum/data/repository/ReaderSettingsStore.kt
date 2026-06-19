package com.example.unum.data.repository

import android.content.Context
import com.example.unum.data.model.ReaderFontScale

class ReaderSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("reader_settings", Context.MODE_PRIVATE)

    fun loadFontScale(): ReaderFontScale {
        val saved = prefs.getString(KEY_FONT_SCALE, ReaderFontScale.MEDIUM.name)
        return runCatching { ReaderFontScale.valueOf(saved.orEmpty()) }
            .getOrDefault(ReaderFontScale.MEDIUM)
    }

    fun saveFontScale(scale: ReaderFontScale) {
        prefs.edit()
            .putString(KEY_FONT_SCALE, scale.name)
            .apply()
    }

    private companion object {
        const val KEY_FONT_SCALE = "font_scale"
    }
}
