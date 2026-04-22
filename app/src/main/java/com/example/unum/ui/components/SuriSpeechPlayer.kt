package com.example.unum.ui.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.unum.data.model.SuriSpeechScript
import java.util.Locale
import kotlin.Comparator

@Composable
fun rememberSuriSpeechPlayer(): SuriSpeechPlayer {
    val context = LocalContext.current
    val player = remember(context) { SuriSpeechPlayer(context) }

    DisposableEffect(player) {
        onDispose { player.dispose() }
    }

    return player
}

class SuriSpeechPlayer(context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val appContext = context.applicationContext

    private var playbackSessionId: Long = 0L
    private var pendingPlay = false
    private var activeScript: SuriSpeechScript? = null

    private val textToSpeech = TextToSpeech(appContext) { status ->
        mainHandler.post { handleInitialization(status) }
    }.apply {
        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (!isCurrentUtterance(utteranceId)) return
                mainHandler.post {
                    errorMessage = null
                    isPlaying = true
                }
            }

            override fun onDone(utteranceId: String?) {
                if (!isCurrentUtterance(utteranceId)) return
                mainHandler.post {
                    val script = activeScript ?: return@post
                    val nextIndex = currentIndex + 1
                    if (!isPlaying) return@post
                    if (nextIndex < script.segments.size) {
                        speakIndex(nextIndex)
                    } else {
                        isPlaying = false
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                if (!isCurrentUtterance(utteranceId)) return
                mainHandler.post {
                    isPlaying = false
                    errorMessage = "기기 음성 엔진에서 재생을 완료하지 못했습니다."
                }
            }
        })
    }

    var isReady by mutableStateOf(false)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateScript(script: SuriSpeechScript?) {
        if (activeScript?.scriptId == script?.scriptId) return
        stop()
        activeScript = script
        currentIndex = 0
        errorMessage = null
    }

    fun togglePlayback() {
        if (isPlaying) {
            stop()
            return
        }

        val script = activeScript ?: return
        val startIndex = if (currentIndex >= script.segments.lastIndex) 0 else currentIndex
        playFrom(startIndex)
    }

    fun replay() {
        currentIndex = 0
        playFrom(0)
    }

    fun stop() {
        pendingPlay = false
        isPlaying = false
        playbackSessionId += 1
        textToSpeech.stop()
    }

    fun dispose() {
        stop()
        textToSpeech.shutdown()
    }

    private fun playFrom(index: Int) {
        val script = activeScript ?: return
        if (script.segments.isEmpty()) return

        pendingPlay = true
        errorMessage = null
        currentIndex = index.coerceIn(0, script.segments.lastIndex)

        if (!isReady) return

        playbackSessionId += 1
        speakIndex(currentIndex)
    }

    private fun speakIndex(index: Int) {
        val script = activeScript ?: return
        val segment = script.segments.getOrNull(index) ?: run {
            isPlaying = false
            return
        }
        currentIndex = index
        val utteranceId = "${script.scriptId}:$playbackSessionId:$index"
        val result = textToSpeech.speak(segment.body, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            isPlaying = false
            errorMessage = "기기 음성 엔진이 이 대사를 재생하지 못했습니다."
        }
    }

    private fun handleInitialization(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            isReady = false
            errorMessage = "기기 음성 엔진을 준비하지 못했습니다."
            return
        }

        val languageResult = textToSpeech.setLanguage(Locale.KOREAN)
        if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            val fallbackResult = textToSpeech.setLanguage(Locale.KOREA)
            if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = false
                errorMessage = "이 기기에서 한국어 음성을 사용할 수 없습니다."
                return
            }
        }

        textToSpeech.setSpeechRate(0.9f)
        textToSpeech.setPitch(1.06f)
        selectPreferredKoreanVoice()

        isReady = true
        if (pendingPlay) {
            playFrom(currentIndex)
        }
    }

    private fun isCurrentUtterance(utteranceId: String?): Boolean {
        val script = activeScript ?: return false
        return utteranceId?.startsWith("${script.scriptId}:$playbackSessionId:") == true
    }

    private fun selectPreferredKoreanVoice() {
        val voices = runCatching { textToSpeech.voices }.getOrNull().orEmpty()
        if (voices.isEmpty()) return

        val koreanVoices = voices.filter { voice ->
            voice.locale?.language == Locale.KOREAN.language
        }
        if (koreanVoices.isEmpty()) return

        val selected = koreanVoices.minWithOrNull(
            compareBy<Voice>(
                { voice -> if ("female" in voice.name.lowercase()) 0 else 1 },
                { voice -> if ("neural" in voice.name.lowercase() || "network" in voice.name.lowercase()) 0 else 1 },
                { voice -> voice.latency },
                { voice -> voice.quality * -1 }
            )
        )

        selected?.let { textToSpeech.setVoice(it) }
    }
}
