package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class SpeechToTextManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Optional listener invoked immediately when user begins speaking, allowing the audio
     * subsystem to execute barge-in interruption and halt TTS playback.
     */
    var onSpeechStartedListener: (() -> Unit)? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(languageCode: String = "hi-IN", onResultCallback: ((String) -> Unit)? = null) {
        _errorMessage.value = null
        _recognizedText.value = ""
        _partialText.value = ""

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _errorMessage.value = null
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                        onSpeechStartedListener?.invoke()
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _rmsDb.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsDb.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग त्रुटि (Audio Error)"
                            SpeechRecognizer.ERROR_CLIENT -> "क्लाइंट त्रुटि (Client Error)"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन अनुमति आवश्यक है (Permission Denied)"
                            SpeechRecognizer.ERROR_NETWORK -> "नेटवर्क त्रुटि (Network Error)"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "नेटवर्क समय समाप्त (Network Timeout)"
                            SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ स्पष्ट नहीं सुनाई दी, पुनः बोलें (No speech match)"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "स्पीच इंजन व्यस्त है (Recognizer Busy)"
                            SpeechRecognizer.ERROR_SERVER -> "सर्वर त्रुटि (Server Error)"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "आवाज़ नहीं आई (Speech Timeout)"
                            else -> "पहचान में त्रुटि ($error)"
                        }
                        _errorMessage.value = msg
                        Log.w("SpeechToTextManager", "STT Error: $error - $msg")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsDb.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull()?.trim().orEmpty()
                        if (spokenText.isNotBlank()) {
                            _recognizedText.value = spokenText
                            onResultCallback?.invoke(spokenText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            _partialText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                // Sub-2s SLA & Offline-First: Prefer local on-device speech model
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
                putExtra("android.speech.extra.DICTATION_MODE", true)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            _errorMessage.value = "माइक्रोफ़ोन प्रारंभ करने में त्रुटि: ${e.message}"
            Log.e("SpeechToTextManager", "Error starting speech recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("SpeechToTextManager", "Error stopping: ${e.message}")
        } finally {
            _isListening.value = false
            _rmsDb.value = 0f
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("SpeechToTextManager", "Error cancelling: ${e.message}")
        } finally {
            _isListening.value = false
            _rmsDb.value = 0f
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("SpeechToTextManager", "Error destroying: ${e.message}")
        }
    }
}
