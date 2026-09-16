package com.example.ui.util

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId

    // Fine-tuning voice parameters
    var currentSpeechRate: Float = 0.92f
        private set
    var currentPitch: Float = 1.0f
        private set

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error initializing TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale("hi", "IN")
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentSpeechRate)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _currentUtteranceId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                    Log.w("TtsManager", "TTS error on $utteranceId: code $errorCode")
                }
            })
        } else {
            Log.e("TtsManager", "TTS initialization failed with status $status")
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 1.5f)
        if (isInitialized) {
            tts?.setSpeechRate(currentSpeechRate)
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.7f, 1.4f)
        if (isInitialized) {
            tts?.setPitch(currentPitch)
        }
    }

    /**
     * Speaks arbitrary text with language locale and optional rate override.
     */
    fun speak(
        text: String,
        languageCode: String = "hi",
        utteranceId: String = "utt_${System.currentTimeMillis()}",
        overrideRate: Float? = null
    ) {
        if (!isInitialized || tts == null || text.isBlank()) {
            return
        }

        try {
            if (languageCode.equals("en", ignoreCase = true)) {
                tts?.language = Locale.ENGLISH
            } else {
                tts?.language = Locale("hi", "IN")
            }

            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(overrideRate ?: currentSpeechRate)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e("TtsManager", "TTS speak failed: ${e.message}")
        }
    }

    /**
     * Speaks tribal speech with fine-tuned phonetic pronunciation.
     * Since standard Android TTS cannot directly synthesize Ol Chiki or Warang Chiti Unicode fonts,
     * this passes the accurate Devanagari phonetic transliteration to the Hindi acoustic voice model,
     * providing authentic, natural, and clear spoken output for classroom students.
     */
    fun speakTribalPhonetic(
        devanagariPhonetic: String,
        fallbackText: String,
        slowMode: Boolean = false,
        utteranceId: String = "tribal_${System.currentTimeMillis()}"
    ) {
        val textToSpeak = devanagariPhonetic.ifBlank { fallbackText }
        val rate = if (slowMode) 0.72f else currentSpeechRate
        speak(text = textToSpeak, languageCode = "hi", utteranceId = utteranceId, overrideRate = rate)
    }

    /**
     * Bilingual Relay: Plays the teacher's Hindi speech first, then after a natural pedagogical pause,
     * speaks the tribal mother-tongue translation so foundational learners hear both languages back-to-back.
     */
    fun speakBilingualRelay(
        hindiSource: String,
        tribalDevanagari: String,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null) return
        stop()

        val relayId1 = "relay_hi_${System.currentTimeMillis()}"
        val relayId2 = "relay_tr_${System.currentTimeMillis()}"

        try {
            tts?.language = Locale("hi", "IN")
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentSpeechRate)

            // Step 1: Queue Hindi
            val params1 = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, relayId1)
            }
            tts?.speak(hindiSource, TextToSpeech.QUEUE_FLUSH, params1, relayId1)

            // Step 2: 400ms pause and play Tribal translation
            tts?.playSilentUtterance(450L, TextToSpeech.QUEUE_ADD, "pause_relay")

            val params2 = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, relayId2)
            }
            val targetPhonetic = tribalDevanagari.ifBlank { hindiSource }
            tts?.speak(targetPhonetic, TextToSpeech.QUEUE_ADD, params2, relayId2)
        } catch (e: Exception) {
            Log.e("TtsManager", "Bilingual relay failed: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS: ${e.message}")
        } finally {
            _isSpeaking.value = false
            _currentUtteranceId.value = null
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsManager", "TTS shutdown error: ${e.message}")
        }
    }
}
