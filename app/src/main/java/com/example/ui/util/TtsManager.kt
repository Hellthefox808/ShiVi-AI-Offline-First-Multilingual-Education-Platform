package com.example.ui.util

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.domain.model.VoiceGender
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
    var currentVoiceGender: VoiceGender = VoiceGender.FEMALE
        private set
    var currentRelayPauseMs: Long = 450L
        private set

    val effectivePitch: Float
        get() = (currentPitch * currentVoiceGender.pitchMultiplier).coerceIn(0.6f, 1.8f)

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
            tts?.language = Locale.forLanguageTag("hi-IN")
            applyNativeGenderVoice(currentVoiceGender)
            tts?.setPitch(effectivePitch)
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
            tts?.setPitch(effectivePitch)
        }
    }

    fun setVoiceGender(gender: VoiceGender) {
        currentVoiceGender = gender
        if (isInitialized) {
            applyNativeGenderVoice(gender)
            tts?.setPitch(effectivePitch)
        }
    }

    private fun applyNativeGenderVoice(gender: VoiceGender) {
        try {
            val voices = tts?.voices ?: return
            val targetLocale = Locale.forLanguageTag("hi-IN")
            val isFemale = gender == VoiceGender.FEMALE
            val matchingVoice = voices.firstOrNull { voice ->
                val matchesLang = voice.locale?.language.equals(targetLocale.language, ignoreCase = true)
                val nameLower = (voice.name ?: "").lowercase(Locale.ROOT)
                matchesLang && if (isFemale) {
                    nameLower.contains("female") || nameLower.contains("woman") || nameLower.contains("#f")
                } else {
                    nameLower.contains("male") || nameLower.contains("man") || nameLower.contains("#m")
                }
            }
            if (matchingVoice != null) {
                tts?.voice = matchingVoice
                Log.d("TtsManager", "Applied native gender voice: ${matchingVoice.name}")
            }
        } catch (e: Exception) {
            Log.d("TtsManager", "Native voice selection fallback to pitch modulation: ${e.message}")
        }
    }

    fun setRelayPauseMs(pauseMs: Long) {
        currentRelayPauseMs = pauseMs.coerceIn(200L, 1000L)
    }

    fun applyVoicePreset(pitch: Float, rate: Float, relayPauseMs: Long) {
        setPitch(pitch)
        setSpeechRate(rate)
        setRelayPauseMs(relayPauseMs)
    }

    /**
     * Normalizes tribal Devanagari phonetics for high-fidelity acoustic rendering via hi-IN acoustic engine.
     * Cleans Ol Chiki punctuation (᱾, ᱿), removes parenthetical pronunciation hints, and ensures
     * correct prosodic pauses for authentic classroom audio.
     */
    fun normalizeTribalPhonetics(rawPhonetic: String): String {
        if (rawPhonetic.isBlank()) return ""
        var cleaned = rawPhonetic
            .replace("᱾", "। ")
            .replace("᱿", "। ")
            .replace(" (दाक्')", " दाक")
            .replace(" (मित')", " मित")
            .replace(Regex("\\([A-Za-z'\\s]+\\)"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.ifBlank { rawPhonetic }
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
                tts?.language = Locale.forLanguageTag("hi-IN")
            }

            tts?.setPitch(effectivePitch)
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
     * Routes accurate Devanagari phonetic transliterations through the hi-IN acoustic engine,
     * providing authentic, natural, and clear spoken output for classroom students.
     */
    fun speakTribalPhonetic(
        devanagariPhonetic: String,
        fallbackText: String,
        slowMode: Boolean = false,
        utteranceId: String = "tribal_${System.currentTimeMillis()}"
    ) {
        val rawText = devanagariPhonetic.ifBlank { fallbackText }
        val normalized = normalizeTribalPhonetics(rawText)
        val rate = if (slowMode) 0.72f else currentSpeechRate
        speak(text = normalized, languageCode = "hi", utteranceId = utteranceId, overrideRate = rate)
    }

    /**
     * Bilingual Relay: Plays the teacher's Hindi speech first, then after a configurable pedagogical pause,
     * speaks the tribal mother-tongue translation so foundational learners hear both languages back-to-back.
     */
    fun speakBilingualRelay(
        hindiSource: String,
        tribalDevanagari: String,
        pauseMs: Long? = null,
        utterancePrefix: String = "relay_${System.currentTimeMillis()}",
        onComplete: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null) return
        stop()

        val relayId1 = "${utterancePrefix}_hi"
        val relayId2 = "${utterancePrefix}_tr"
        val effectivePause = pauseMs ?: currentRelayPauseMs

        try {
            tts?.language = Locale.forLanguageTag("hi-IN")
            tts?.setPitch(effectivePitch)
            tts?.setSpeechRate(currentSpeechRate)

            // Step 1: Queue Hindi
            val params1 = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, relayId1)
            }
            tts?.speak(hindiSource, TextToSpeech.QUEUE_FLUSH, params1, relayId1)

            // Step 2: Pedagogical pause (200ms - 1000ms)
            tts?.playSilentUtterance(effectivePause, TextToSpeech.QUEUE_ADD, "pause_relay")

            val params2 = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, relayId2)
            }
            val rawTarget = tribalDevanagari.ifBlank { hindiSource }
            val normalizedTarget = normalizeTribalPhonetics(rawTarget)
            tts?.speak(normalizedTarget, TextToSpeech.QUEUE_ADD, params2, relayId2)
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

    /**
     * Barge-in interruption: Immediately cancels ongoing speech synthesis, clears all queued
     * utterances and pauses, and resets playback state.
     */
    fun interruptPlayback() {
        try {
            mainHandler.removeCallbacksAndMessages(null)
            tts?.stop()
            Log.d("TtsManager", "Playback interrupted (barge-in active)")
        } catch (e: Exception) {
            Log.e("TtsManager", "Error interrupting TTS: ${e.message}")
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
