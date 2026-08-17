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
 * in progress, not just whether the user tapped it. Also exposes
 * [isStarting] — the (usually brief, but real) window between a tap and
 * actual audio output, covering both first-time engine initialization and
 * the platform's normal per-utterance startup latency — so the UI can show
 * something other than "nothing happening" during that gap.
 */
class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null

    // If speak() is called before the engine finishes initializing, the
    // request is queued here and fired the moment onInit succeeds — calling
    // TextToSpeech.speak() before init completes can silently no-op on some
    // devices, which was the real bug behind "Listen does nothing at first".
    private var pendingText: String? = null

    private val _isReady = mutableStateOf(false)
    val isReady: State<Boolean> = _isReady

    private val _isStarting = mutableStateOf(false)
    val isStarting: State<Boolean> = _isStarting

    private val _isSpeaking = mutableStateOf(false)
    val isSpeaking: State<Boolean> = _isSpeaking

    private val _isUnavailable = mutableStateOf(false)
    val isUnavailable: State<Boolean> = _isUnavailable

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                _isReady.value = true
                pendingText?.let { text ->
                    pendingText = null
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
                }
            } else {
                _isUnavailable.value = true
                _isStarting.value = false
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isStarting.value = false
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isStarting.value = false
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java", ReplaceWith(""))
            override fun onError(utteranceId: String?) {
                _isStarting.value = false
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String) {
        if (_isUnavailable.value || text.isBlank()) return
        _isStarting.value = true
        if (_isReady.value) {
            pendingText = null
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } else {
            // Engine still initializing — the onInit callback above will
            // fire this the moment it's ready instead of dropping it.
            pendingText = text
        }
    }

    fun stop() {
        pendingText = null
        tts?.stop()
        _isStarting.value = false
        _isSpeaking.value = false
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun shutdown() {
        pendingText = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        private const val UTTERANCE_ID = "current_article_reader"
    }
}
