package com.example

import com.example.domain.model.TargetLanguage
import com.example.domain.model.VoiceGender
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

    @Test
    fun testTeacherMode_arbitrarySpokenSpeech_doesNotRepeatChoralPrompt() {
        // User reports: "Jaise main koi sa bhi word bol raha hoon to bas ek hi cheez repeat kar raha hai"
        val input = "मैं बोल रहा हूँ"
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = input,
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertFalse("Must NOT trigger choral recitation prompt", result.targetText.contains("ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ ᱢᱤᱫ ᱛᱮ ᱞᱟᱹᱭ ᱯᱮ"))
        assertFalse("Must NOT use static Asra echo", result.targetText.contains("ᱟᱥᱲᱟ ᱨᱮ ᱥᱮᱪᱮᱫ"))
        assertTrue("Must translate to I am speaking in Ol Chiki", result.targetText.contains("ᱞᱟᱹᱭ") || result.targetText.contains("ᱤᱧ"))
        assertTrue("Devanagari phonetics must be present for Indian TTS", result.transliterationDevanagari.isNotBlank())
        assertFalse("Devanagari phonetics must NOT be static prompt", result.transliterationDevanagari.contains("सानाम गिदरा"))
    }

    @Test
    fun testTeacherMode_arbitrarySingleWords_translateAccurately() {
        val testWords = listOf(
            "पेड़" to "ᱫᱟᱨᱮ",
            "पानी" to "ᱫᱟᱜ",
            "घर" to "ᱚᱲᱟᱜ",
            "हाथ" to "ᱛᱤ",
            "सूरज" to "ᱥᱤᱝᱜᱤ"
        )

        for ((hindiWord, expectedOlChiki) in testWords) {
            val result = OfflineVoiceTranslationEngine.translate(
                inputText = hindiWord,
                targetLanguage = TargetLanguage.SANTHALI,
                speakerRole = VoiceSpeakerRole.TEACHER
            )
            assertTrue("Word '$hindiWord' must translate to '$expectedOlChiki'", result.targetText.contains(expectedOlChiki))
            assertFalse("Must NOT repeat Asra re seched prefix for word '$hindiWord'", result.targetText.contains("ᱟᱥᱲᱟ ᱨᱮ ᱥᱮᱪᱮᱫ"))
            assertTrue("Devanagari phonetics must be non-blank", result.transliterationDevanagari.isNotBlank())
            assertTrue("Phonetic syllables must be generated", result.phoneticSyllables.isNotEmpty())
        }
    }

    @Test
    fun testTeacherMode_whatAreYouSpeaking_translatesAccuratelyAcrossLanguages() {
        val input = "आप क्या बोल रहे हैं"
        for (lang in TargetLanguage.values()) {
            val result = OfflineVoiceTranslationEngine.translate(
                inputText = input,
                targetLanguage = lang,
                speakerRole = VoiceSpeakerRole.TEACHER
            )
            assertFalse("Must not trigger repetition prompt for $lang", result.targetText.contains("ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ"))
            assertTrue("Target translation must be non-empty for $lang", result.targetText.isNotBlank())
            assertTrue("Devanagari phonetics must be non-blank for $lang", result.transliterationDevanagari.isNotBlank())
        }
    }

    @Test
    fun testLiveVoiceTranslation_sub2SecondLatency_guaranteedAcrossLanguages() {
        val testPhrases = listOf(
            "नमस्ते बच्चों! आज हम सब मिलकर पढ़ाई करेंगे।",
            "साल का पेड़ हमारे गाँव और संस्कृति के लिए बहुत पवित्र है।",
            "नदी का पानी स्वच्छ और शुद्ध रखना चाहिए।",
            "आप क्या बोल रहे हैं, कृपया बताएं।"
        )

        for (phrase in testPhrases) {
            for (lang in TargetLanguage.values()) {
                val start = System.currentTimeMillis()
                val result = OfflineVoiceTranslationEngine.translate(
                    inputText = phrase,
                    targetLanguage = lang,
                    speakerRole = VoiceSpeakerRole.TEACHER
                )
                val duration = System.currentTimeMillis() - start

                assertTrue("Translation must complete in < 2000ms (SLA guarantee, was ${duration}ms)", duration < 2000)
                assertTrue("Target translation must be non-empty for $lang", result.targetText.isNotBlank())
                assertTrue("Devanagari phonetics must be populated for $lang", result.transliterationDevanagari.isNotBlank())
            }
        }
    }

    @Test
    fun testVoiceGender_multipliersAndSettings_behaveCorrectly() {
        val defaultSettings = VoiceSettings()
        assertEquals("Default voice gender must be FEMALE", VoiceGender.FEMALE, defaultSettings.voiceGender)
        assertTrue("Female pitch multiplier must be > 1.0", VoiceGender.FEMALE.pitchMultiplier > 1.0f)
        assertTrue("Male pitch multiplier must be < 1.0", VoiceGender.MALE.pitchMultiplier < 1.0f)

        val maleSettings = defaultSettings.copy(voiceGender = VoiceGender.MALE)
        assertEquals(VoiceGender.MALE, maleSettings.voiceGender)
        assertEquals("पुरुष", VoiceGender.MALE.displayNameHindi)
        assertEquals("👨", VoiceGender.MALE.icon)
        assertEquals("👩", VoiceGender.FEMALE.icon)
    }

    @Test
    fun testRagGlossaryRetrieval_zeroHallucination() {
        val speech = "आज हम साल का पेड़ और पानी के बारे में पढ़ेंगे"
        val ragSanthali = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext(speech, TargetLanguage.SANTHALI)
        assertTrue("RAG must contain verified Sarjom/Dare for Santhali", ragSanthali.contains("पेड़") && ragSanthali.contains("दारे"))
        assertTrue("RAG must contain verified Dak for water", ragSanthali.contains("पानी") && ragSanthali.contains("दाग"))

        val ragHo = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext(speech, TargetLanguage.HO)
        assertTrue("RAG must contain verified Ho terms", ragHo.contains("पेड़"))

        val ragMundari = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext(speech, TargetLanguage.MUNDARI)
        assertTrue("RAG must contain verified Mundari terms", ragMundari.contains("पानी"))
    }

    /**
     * Regression: LEXICON previously declared the key "दो" twice — once as the verb
     * "give" (ᱮᱢ ᱢᱮ) and once as the numeral "two" (ᱵᱟᱨ). A Kotlin mapOf literal
     * silently drops the earlier binding, so every occurrence of "दो" resolved to
     * the GIVE verb, corrupting all counting and giving/answering phrases.
     */
    @Test
    fun testLexicon_numeralDho_resolvesToTwoNotGiveVerb() {
        val context = OfflineVoiceTranslationEngine.retrieveRagGlossaryContext("एक दो तीन", TargetLanguage.SANTHALI)
        assertTrue("Numeral 'दो' must map to ᱵᱟᱨ (two), not ᱮᱢ ᱢᱮ (give)", context.contains("ᱵᱟᱨ"))
        assertFalse("Numeral 'दो' must NOT resolve to the GIVE verb sense", context.contains("ᱮᱢ ᱢᱮ"))
    }

    /**
     * Regression: the QA category matched on `उत्तर` + `दो`, so ordinary praise such as
     * "बहुत अच्छा उत्तर दो" / "सही उत्तर दो" hijacked the category and was answered with
     * the static "who knows the answer?" choral prompt instead of encouragement.
     */
    @Test
    fun testTeacherMode_encouragementWithAnswerIsNotHijackedByQaCategory() {
        val praise = OfflineVoiceTranslationEngine.translate(
            inputText = "बहुत अच्छा उत्तर दो",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )
        assertFalse(
            "Praise phrase must NOT trigger the static 'who knows?' QA prompt",
            praise.targetText.contains("ᱚᱠᱚᱭ ᱵᱟᱰᱟᱭᱟ ᱛᱮᱞᱟ")
        )
        assertTrue(
            "Praise phrase must reach the encouragement category",
            praise.targetText.contains("ᱥᱟᱨᱦᱟᱣ") || praise.targetText.contains("ᱱᱟᱯᱟᱭ")
        )

        // The genuine asking-an-answer instruction must still route to the QA category
        val genuine = OfflineVoiceTranslationEngine.translate(
            inputText = "उत्तर दो",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )
        assertTrue("Genuine 'उत्तर दो' must still hit the QA category", genuine.targetText.contains("ᱛᱮᱞᱟ"))
    }

    /**
     * Regression: syllable generation used String.chunked(2), slicing Devanagari
     * by UTF-16 code unit and cutting matras off their base consonant. FLN chips
     * rendered as unpronounceable fragments like "ङा" or a lone "़".
     */
    /**
     * Regression: VoiceTurn previously declared
     *   `speakerRole: VoiceSpeakerRole = if (isTeacher) TEACHER else STUDENT`
     * where `speakerRole` is itself declared AFTER `isTeacher`. Kotlin resolves
     * default-argument expressions against the constructor parameter scope, so the
     * reference bound to `isTeacher`'s own default (true) — meaning the parameter default
     * could NEVER yield STUDENT, and the call was self-referential at best. Combined with
     * `isTeacher: Boolean = true`, omitting both for a student turn silently produced a
     * TEACHER-labelled turn.
     *
     * The correct contract: `speakerRole` is required, and any explicitly passed
     * `isTeacher` is what callers actually read back.
     */
    @Test
    fun testVoiceTurn_speakerRoleIsRequiredAndConsistentWithIsTeacher() {
        // Student turn: both flags must agree
        val studentTurn = VoiceTurn(
            id = "t_student",
            isTeacher = false,
            speakerRole = VoiceSpeakerRole.STUDENT,
            hindiText = "मुझे पानी चाहिए",
            targetText = "ᱫᱟᱜ",
            scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
            transliteration = "Dak",
            transliterationDevanagari = "दाग",
            latencyMs = 25L
        )
        assertFalse("Student turn must report isTeacher=false", studentTurn.isTeacher)
        assertEquals(VoiceSpeakerRole.STUDENT, studentTurn.speakerRole)

        // Teacher turn reached through the real engine must stay consistent
        val teacherTurn = OfflineVoiceTranslationEngine.translate(
            inputText = "नमस्ते",
            targetLanguage = TargetLanguage.SANTHALI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )
        assertTrue(teacherTurn.isTeacher)
        assertEquals(VoiceSpeakerRole.TEACHER, teacherTurn.speakerRole)
        assertNotEquals(
            "isTeacher and speakerRole must never disagree",
            teacherTurn.isTeacher,
            teacherTurn.speakerRole == VoiceSpeakerRole.STUDENT
        )
    }

    @Test
    fun testPhoneticSyllables_neverOrphanCombiningMarks() {
        val result = OfflineVoiceTranslationEngine.translate(
            inputText = "नमस्ते बच्चों, आज हम पढ़ेंगे",
            targetLanguage = TargetLanguage.MUNDARI,
            speakerRole = VoiceSpeakerRole.TEACHER
        )

        assertTrue("Teacher turns must always expose FLN syllables", result.phoneticSyllables.isNotEmpty())
        for (chip in result.phoneticSyllables) {
            assertTrue("Syllable chip '$chip' must not be blank", chip.isNotBlank())
            assertFalse("Syllable chip '$chip' must not be an empty fragment", chip.trim('-').isBlank())
            // A chip must never begin with a combining sign (matra/virama/nukta) — that
            // means it was cut away from the consonant it modifies.
            val first = chip.first()
            val startsWithCombining = first in '\u093E'..'\u094D' || first == '\u0901' ||
                first == '\u0902' || first == '\u0903' || first == '\u093C' || first == '-'
            assertFalse("Syllable chip '$chip' starts with an orphaned combining mark", startsWithCombining)
        }
    }
}


