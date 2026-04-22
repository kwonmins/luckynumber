package com.example.unum.data.model

enum class SuriSpeechSceneState {
    GREETING,
    EXPLAIN,
    FOCUS,
    HIGHLIGHT,
    CAUTION,
    COMFORT,
    CLOSING
}

data class SuriSpeechSegment(
    val key: String,
    val chip: String,
    val title: String,
    val body: String,
    val state: SuriSpeechSceneState
)

data class SuriSpeechScript(
    val scriptId: String,
    val title: String,
    val subtitle: String,
    val segments: List<SuriSpeechSegment>
)
