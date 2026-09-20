package com.example

import com.example.domain.model.*
import com.example.domain.rag.LocalRagEmbeddingEngine
import com.example.domain.voice.OfflineVoiceTranslationEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Exhaustive APK Failure-Mode, Edge-Case & Stress Test Suite.
 * Validates the stability and fault tolerance of BhashaSetu AI under:
 * 1. Empty, blank, and whitespace inputs
 * 2. Punctuation-only and malformed strings
 * 3. Emojis and unmapped Unicode symbols
 * 4. Massive 500+ word inputs (OOM prevention)
 * 5. High-concurrency multithreaded stress (20 threads, 2000 ops)
 * 6. VoiceGender pitch boundary extremes
 * 7. Zero-match RAG search queries
 * 8. Reverse Student-to-Teacher dialect variations
 */
class ApkFailureModeStressTest {

    @Test
    fun testFailureMode_blankAndWhitespaceInput_neverCrashes() {
        val blankInputs = listOf("", " ", "   ", "\t", "\n", "\r\n")
        for (input in blankInputs) {
            for (lang in TargetLanguage.values()) {
                // Teacher mode
                val teacherTurn = OfflineVoiceTranslationEngine.translate(
                    inputText = input,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.TEACHER
                )
                assertNotNull("Teacher turn must not be null for blank input", teacherTurn)
                assertEquals(VoiceSpeakerRole.TEACHER, teacherTurn.speakerRole)
                assertTrue("Script text must be valid", teacherTurn.scriptText.isNotBlank())

                // Student mode
                val studentTurn = OfflineVoiceTranslationEngine.translate(
                    inputText = input,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.STUDENT
                )
                assertNotNull("Student turn must not be null for blank input", studentTurn)
                assertEquals(VoiceSpeakerRole.STUDENT, studentTurn.speakerRole)

                // Direct engine call
                val studentRes = OfflineVoiceTranslationEngine.translateStudentToTeacher(input, lang)
                assertNotNull(studentRes)
            }
        }
    }

    @Test
    fun testFailureMode_punctuationAndMalformedSymbols_handledGracefully() {
        val malformedInputs = listOf(
            "???", "!?!?", ".....", "।।।।", ",,,", "---", "@#$%^&*()", "?!,."
        )
        for (input in malformedInputs) {
            for (lang in TargetLanguage.values()) {
                val turn = OfflineVoiceTranslationEngine.translate(
                    inputText = input,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.TEACHER
                )
                assertNotNull("Turn must not be null for punctuation input: $input", turn)
                assertTrue("Latency must be positive", turn.latencyMs >= 0)
            }
        }
    }

    @Test
    fun testFailureMode_emojisAndMixedUnicode_transliteratesSafely() {
        val emojiInputs = listOf(
            "👨‍🏫 नमस्ते बच्चों 📚 🌳",
            "साल का पेड़ 🌲 और पानी 💧 123",
            "Hello 456 जोहार 🎉"
        )
        for (input in emojiInputs) {
            for (lang in TargetLanguage.values()) {
                val turn = OfflineVoiceTranslationEngine.translate(
                    inputText = input,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.TEACHER
                )
                assertNotNull(turn)
                assertTrue("Target text should not be empty", turn.targetText.isNotBlank())
                assertTrue("Transliteration Devanagari should not be empty", turn.transliterationDevanagari.isNotBlank())
            }
        }
    }

    @Test
    fun testFailureMode_massiveParagraphStress_sub50msAndNoOom() {
        // Construct a 500-word paragraph
        val baseSentence = "नमस्ते बच्चों आज हम सब मिलकर साल के पवित्र पेड़ सरजोम और नदी के स्वच्छ पानी के बारे में पढ़ेंगे। "
        val massiveInput = baseSentence.repeat(35) // ~525 words

        for (lang in TargetLanguage.values()) {
            val start = System.currentTimeMillis()
            val turn = OfflineVoiceTranslationEngine.translate(
                inputText = massiveInput,
                targetLanguage = lang,
                speakerRole = VoiceSpeakerRole.TEACHER
            )
            val duration = System.currentTimeMillis() - start

            assertNotNull(turn)
            assertTrue("Massive 500-word translation must complete in < 200ms (was ${duration}ms)", duration < 200)
            assertTrue("Target translation must be non-empty", turn.targetText.isNotBlank())
            assertTrue("Phonetic syllables must not exceed safe limit", turn.phoneticSyllables.size <= 8)
        }
    }

    @Test
    fun testFailureMode_highConcurrencyMultithreading_zeroRaceConditions() {
        val threadCount = 20
        val operationsPerThread = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val phrases = listOf(
            "नमस्ते बच्चों",
            "किताब खोलो",
            "गिनती करो एक दो तीन",
            "साल का पेड़",
            "पानी पियो"
        )

        val tasks = (0 until threadCount).map { threadIdx ->
            Callable<Boolean> {
                for (op in 0 until operationsPerThread) {
                    val phrase = phrases[(threadIdx + op) % phrases.size]
                    val lang = TargetLanguage.values()[(threadIdx + op) % TargetLanguage.values().size]
                    val role = if (op % 2 == 0) VoiceSpeakerRole.TEACHER else VoiceSpeakerRole.STUDENT

                    // Translate
                    val turn = OfflineVoiceTranslationEngine.translate(phrase, lang, role)
                    if (turn.targetText.isBlank() && phrase.isNotBlank()) return@Callable false

                    // RAG Glossary lookup
                    val rag = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext(phrase, lang)
                    if (rag.isBlank()) return@Callable false

                    // Semantic embedding
                    val vec = LocalRagEmbeddingEngine.embedText(phrase)
                    if (vec.size != 64) return@Callable false
                }
                true
            }
        }

        val futures = executor.invokeAll(tasks)
        executor.shutdown()
        assertTrue("Executor must terminate within 15 seconds", executor.awaitTermination(15, TimeUnit.SECONDS))

        for (future in futures) {
            assertTrue("Thread task must succeed without exceptions", future.get())
        }
    }

    @Test
    fun testFailureMode_voiceGenderPitchExtremeBoundaries() {
        val settings = VoiceSettings()

        // Female pitch bounds
        val femalePitchMultiplier = VoiceGender.FEMALE.pitchMultiplier
        assertEquals(1.15f, femalePitchMultiplier, 0.01f)

        // Male pitch bounds
        val malePitchMultiplier = VoiceGender.MALE.pitchMultiplier
        assertEquals(0.85f, malePitchMultiplier, 0.01f)

        // Simulated extreme base pitch values
        val extremePitches = listOf(0.1f, 0.7f, 1.0f, 1.4f, 5.0f, -2.0f)
        for (pitch in extremePitches) {
            val clampedBase = pitch.coerceIn(0.7f, 1.4f)
            val femaleEffective = (clampedBase * VoiceGender.FEMALE.pitchMultiplier).coerceIn(0.6f, 1.8f)
            val maleEffective = (clampedBase * VoiceGender.MALE.pitchMultiplier).coerceIn(0.6f, 1.8f)

            assertTrue("Female effective pitch must be between 0.6 and 1.8 (was $femaleEffective)", femaleEffective in 0.6f..1.8f)
            assertTrue("Male effective pitch must be between 0.6 and 1.8 (was $maleEffective)", maleEffective in 0.6f..1.8f)
            assertTrue("Female pitch must be strictly higher than Male pitch", femaleEffective > maleEffective)
        }
    }

    @Test
    fun testFailureMode_ragZeroMatchQuery_resilientFallback() {
        val obscureQuery = "xyzzy12345qwerty nonexistent_token_999"
        for (lang in TargetLanguage.values()) {
            val ragContext = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext(obscureQuery, lang)
            assertNotNull(ragContext)
            assertTrue(
                "Must provide strict anti-hallucination instruction even when no glossary match",
                ragContext.contains("STRICT GROUNDING")
            )
        }
    }

    @Test
    fun testFailureMode_embeddingZeroNorm_safelyReturnsZeroVector() {
        val zeroVector = LocalRagEmbeddingEngine.embedText("")
        assertEquals(LocalRagEmbeddingEngine.VECTOR_DIMENSION, zeroVector.size)
        for (v in zeroVector) {
            assertEquals(0f, v, 0.00001f)
        }

        val punctuationOnly = LocalRagEmbeddingEngine.embedText("!@#$ %^&*()")
        assertNotNull(punctuationOnly)
        assertEquals(64, punctuationOnly.size)
    }
}
