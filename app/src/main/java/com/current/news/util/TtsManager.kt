package com.current.news.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

/**
 * Thin wrapper around Android's on-device [TextToSpeech] engine — no
 * external dependency needed, it's part of the platform SDK and doesn't
 * require any special runtime permission for playback.
 *
 * Exposes playing state as Compose [State] via an [UtteranceProgressListener]
 * so the Listen button in the UI can reflect whether speech is actually
 * in progress, not just whether the user tapped it.
 */
class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null

    private val _isReady = mutableStateOf(false)
    val isReady: State<Boolean> = _isReady

    private val _isSpeaking = mutableStateOf(false)
    val isSpeaking: State<Boolean> = _isSpeaking

    private val _isUnavailable = mutableStateOf(false)
    val isUnavailable: State<Boolean> = _isUnavailable

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                _isReady.value = true
            } else {
                _isUnavailable.value = true
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java", ReplaceWith(""))
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String) {
        if (_isUnavailable.value || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        private const val UTTERANCE_ID = "current_article_reader"
    }
}
