package com.example

import com.example.domain.model.TargetLanguage
import com.example.domain.model.VoiceSpeakerRole
import com.example.domain.model.VoiceSettings
import com.example.domain.model.VoiceTurn
import com.example.domain.voice.OfflineVoiceTranslationEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Native Android Unit Tests for BhashaSetu AI Voice Translation Subsystem.
 * Verifies:
 * - Teacher Mode (Hindi -> Santhali, Ho, Mundari)
 * - Student Mode (Tribal -> Hindi reverse translation)
 * - Devanagari phonetic transliteration and FLN syllable splitting
 * - Multithreaded concurrency & sub-30ms offline latency SLA
 */
class VoiceTranslationTest {

    @Test
    fun testTeacherMode_santhaliGreeting_generatesOlChikiAndSyllables() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "नमस्ते बच्चों, आज हम पढ़ेंगे",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertTrue("Turn must be identified as Teacher", result.isTeacher)
        assertEquals(VoiceSpeakerRole.TEACHER, result.speakerRole)
        assertTrue("Target text must contain Ol Chiki characters", result.targetText.contains("ᱡᱚᱦᱟᱨ"))
        assertEquals("Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)", result.scriptText)
        assertTrue("Devanagari phonetic synthesis text must be present", result.transliterationDevanagari.isNotBlank())
        assertTrue("Phonetic syllables list must not be empty for FLN practice", result.phoneticSyllables.isNotEmpty())
        assertTrue("Phonetic syllables should contain 'जो-हार'", result.phoneticSyllables.contains("जो-हार"))
        assertTrue("Offline latency must be < 100ms", result.latencyMs < 100L)
    }

    @Test
    fun testTeacherMode_hoGreeting_generatesWarangChiti() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "नमस्ते",
            targetLanguage = TargetLanguage.HO,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertTrue(result.isTeacher)
        assertEquals(VoiceSpeakerRole.TEACHER, result.speakerRole)
        assertEquals("Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)", result.scriptText)
        assertTrue(result.targetText.isNotBlank())
        assertTrue(result.transliterationDevanagari.contains("जोहार"))
        assertTrue(result.phoneticSyllables.isNotEmpty())
    }

    @Test
    fun testTeacherMode_mundariGreeting_generatesDevanagariMundari() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "नमस्ते बच्चों",
            targetLanguage = TargetLanguage.MUNDARI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertTrue(result.isTeacher)
        assertEquals(VoiceSpeakerRole.TEACHER, result.speakerRole)
        assertEquals("Devanagari Mundari (देवनागरी मुण्डारी)", result.scriptText)
        assertTrue(result.targetText.contains("जोहार"))
        assertTrue(result.phoneticSyllables.isNotEmpty())
    }

    @Test
    fun testTeacherMode_classroomPhrases_coversCoreCategories() {
        val testCategories = listOf(
            "किताब खोलो" to "Classroom Books",
            "गिनती करो" to "Math & Counting",
            "पेड़ और पौधे" to "Science & Nature",
            "पानी पियो" to "Hygiene & Health",
            "बहुत अच्छा शाबाश" to "Encouragement"
        )

        for ((phrase, _) in testCategories) {
            for (lang in TargetLanguage.values()) {
                val turn = OfflineVoiceTranslationEngine.translate(
                    inputText = phrase,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.TEACHER
                )
                assertTrue("Translation should not be empty for $phrase in $lang", turn.targetText.isNotBlank())
                assertTrue("Transliteration should not be empty for $phrase in $lang", turn.transliterationDevanagari.isNotBlank())
                assertTrue("Syllables should not be empty for $phrase in $lang", turn.phoneticSyllables.isNotEmpty())
            }
        }
    }

    @Test
    fun testStudentMode_joharUtterance_translatesToHindiGreeting() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "ᱡᱚᱦᱟᱨ",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.STUDENT
        )

        assertFalse("Student turn must have isTeacher=false", result.isTeacher)
        assertEquals(VoiceSpeakerRole.STUDENT, result.speakerRole)
        assertTrue("Student greeting should translate to Hindi Namaste/Johar",
            result.targetText.contains("नमस्ते") || result.targetText.contains("जोहार"))
        assertEquals("Devanagari Hindi", result.scriptText)
        assertTrue("Syllables should be present for pronunciation", result.phoneticSyllables.isNotEmpty())
    }

    @Test
    fun testStudentMode_waterRequest_translatesToHindiWaterNeed() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "ᱫᱟᱜ ᱧᱩ",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.STUDENT
        )

        assertFalse(result.isTeacher)
        assertEquals(VoiceSpeakerRole.STUDENT, result.speakerRole)
        assertTrue("Water request must translate to drinking water in Hindi",
            result.targetText.contains("पानी") || result.targetText.contains("पीना"))
    }

    @Test
    fun testStudentMode_bookResponse_translatesToHindiBook() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "ᱯᱚᱛᱚᱵ",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.STUDENT
        )

        assertFalse(result.isTeacher)
        assertEquals(VoiceSpeakerRole.STUDENT, result.speakerRole)
        assertTrue("Book response must translate to 'किताब'", result.targetText.contains("किताब"))
    }

    @Test
    fun testStudentMode_generalFallback_includesLanguageAndInput() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "ᱟᱞᱮ ᱟᱹᱛᱩ",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.STUDENT
        )

        assertFalse(result.isTeacher)
        assertEquals(VoiceSpeakerRole.STUDENT, result.speakerRole)
        assertTrue("Fallback should reference student response", result.targetText.contains("विद्यार्थी का उत्तर"))
        assertTrue("Fallback should contain original speech", result.targetText.contains("ᱟᱞᱮ ᱟᱹᱛᱩ"))
    }

    @Test
    fun testVoiceTurn_favoriteToggle() {
        val turn = OfflineVoiceTranslationEngine.translate(
            inputText = "शाबाश बच्चों",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertFalse("Initial turn favorite state must be false", turn.isFavorite)
        val favorited = turn.copy(isFavorite = true)
        assertTrue("Updated turn favorite state must be true", favorited.isFavorite)
    }

    @Test
    fun testVoiceSettings_defaultValues() {
        val settings = VoiceSettings()
        assertFalse("Two-way dialogue mode defaults to false until toggled", settings.isTwoWayDialogueMode)
        assertEquals(VoiceSpeakerRole.TEACHER, settings.activeSpeakerRole)
        assertTrue("Phonetic syllables should be enabled by default", settings.enablePhoneticSyllables)
        assertFalse("Bilingual relay defaults to false in default settings", settings.isBilingualRelayEnabled)
    }

    @Test
    fun testHighThroughput_concurrencyAndSub30msSLA() {
        val threadPool = Executors.newFixedThreadPool(8)
        val totalCalls = 400
        val languages = TargetLanguage.values()
        val roles = listOf(VoiceSpeakerRole.TEACHER, VoiceSpeakerRole.STUDENT)
        val testInputs = listOf("नमस्ते", "किताब खोलो", "पानी पियो", "ᱡᱚᱦᱟᱨ", "ᱫᱟᱜ")

        val startTime = System.currentTimeMillis()
        val tasks = (0 until totalCalls).map { i ->
            threadPool.submit(Callable {
                val lang = languages[i % languages.size]
                val role = roles[i % roles.size]
                val input = testInputs[i % testInputs.size]
                OfflineVoiceTranslationEngine.translate(input, lang, role)
            })
        }

        threadPool.shutdown()
        val finished = threadPool.awaitTermination(10, TimeUnit.SECONDS)
        val totalDuration = System.currentTimeMillis() - startTime
        val avgLatency = totalDuration.toDouble() / totalCalls

        assertTrue("All concurrent translation tasks must finish cleanly", finished)
        assertEquals(totalCalls, tasks.count { it.get().targetText.isNotBlank() })
        println("Offline Voice Engine Concurrency: $totalCalls calls across 8 threads in ${totalDuration}ms (avg: ${avgLatency}ms)")
        assertTrue("Average translation throughput must be under 30ms (was ${avgLatency}ms)", avgLatency < 30.0)
    }
}
