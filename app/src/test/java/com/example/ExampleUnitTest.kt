package com.example

import com.example.data.local.WorksheetEntity
import com.example.data.local.parseQuestions
import com.example.data.seed.PreloadedData
import com.example.domain.model.WorksheetQuestion
import com.example.domain.model.parseWorksheetQuestionsJson
import com.example.domain.model.toJsonString
import com.example.domain.rag.LocalRagEmbeddingEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPreloadedData_curriculumIntegrity() {
        val chunks = PreloadedData.defaultCurriculumChunks
        assertTrue("Preloaded curriculum chunks must not be empty", chunks.isNotEmpty())
        for (chunk in chunks) {
            assertTrue("Chunk ID must not be blank", chunk.id.isNotBlank())
            assertTrue("Topic must not be blank", chunk.topic.isNotBlank())
            assertTrue("Lesson text Hindi must not be blank", chunk.lessonTextHindi.isNotBlank())
            assertTrue("Tribal language must be SANTHALI, HO, or MUNDARI",
                listOf("SANTHALI", "HO", "MUNDARI").contains(chunk.tribalLanguage))
        }
    }

    @Test
    fun testPreloadedData_glossaryCompleteness() {
        val glossary = PreloadedData.defaultGlossaryItems
        assertTrue("Preloaded glossary must contain entries", glossary.isNotEmpty())
        for (entry in glossary) {
            assertTrue("Glossary ID must not be blank", entry.id.isNotBlank())
            assertTrue("Hindi word must not be blank", entry.hindiWord.isNotBlank())
            assertTrue("Santhali Ol Chiki must not be blank", entry.santhaliOlChiki.isNotBlank())
            assertTrue("Ho Devanagari must not be blank", entry.hoDevanagari.isNotBlank())
            assertTrue("Mundari word must not be blank", entry.mundariWord.isNotBlank())
        }
    }

    @Test
    fun testLocalRagEmbeddingEngine_vectorDimensionalityAndNormalization() {
        val vector = LocalRagEmbeddingEngine.embedText("साल का पवित्र पेड़ सरजोम")
        assertEquals(LocalRagEmbeddingEngine.VECTOR_DIMENSION, vector.size)
        
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += (v * v)
        }
        val norm = Math.sqrt(sumSquares)
        assertEquals(1.0, norm, 0.001)
    }

    @Test
    fun testLocalRagEmbeddingEngine_cosineSimilarityPositiveForRelatedTokens() {
        val vec1 = LocalRagEmbeddingEngine.embedText("साल का पेड़ Sarjom")
        val vec2 = LocalRagEmbeddingEngine.embedText("सरजोम दारे (Sarjom Dare) साल वृक्ष")
        val sim = LocalRagEmbeddingEngine.computeCosineSimilarity(vec1, vec2)
        assertTrue("Cosine similarity should be significant (> 0.4)", sim > 0.4f)
    }

    @Test
    fun testLocalRagEmbeddingEngine_curriculumRetrieval_ranksSalTreeTop() {
        val candidates = PreloadedData.defaultCurriculumChunks
        val queryContext = LocalRagEmbeddingEngine.retrieveRankedMatches(
            query = "साल का पेड़ और सरजोम",
            candidateChunks = candidates,
            targetLanguageFilter = "SANTHALI",
            topK = 2
        )

        assertNotNull("Primary grounded chunk must be present", queryContext.primaryGroundedChunk)
        assertEquals("rag_jcert_g2_evs_01", queryContext.primaryGroundedChunk?.id)
        assertTrue("Top match similarity must be high", queryContext.topMatches.first().similarityScore > 0.5f)
        assertTrue("Latency should be calculated", queryContext.retrievalLatencyMs >= 0L)
    }

    @Test
    fun testLocalRagEmbeddingEngine_languageFilterRespected() {
        val candidates = PreloadedData.defaultCurriculumChunks
        val queryContext = LocalRagEmbeddingEngine.retrieveRankedMatches(
            query = "संख्या और गिनती",
            candidateChunks = candidates,
            targetLanguageFilter = "SANTHALI",
            topK = 5
        )

        for (match in queryContext.topMatches) {
            assertEquals("SANTHALI", match.chunk.tribalLanguage)
        }
    }

    @Test
    fun testLocalRagEmbeddingEngine_bm25Relevance_exactKeywordRanksHigh() {
        val docTokens = listOf("साल", "का", "पवित्र", "पेड़", "सरजोम", "ᱫᱟᱨᱮ")
        val matchingQuery = listOf("साल", "पेड़")
        val nonMatchingQuery = listOf("कंप्यूटर", "इंटरनेट")

        val matchScore = LocalRagEmbeddingEngine.computeBm25Score(matchingQuery, docTokens)
        val nonMatchScore = LocalRagEmbeddingEngine.computeBm25Score(nonMatchingQuery, docTokens)

        assertTrue("Matching BM25 score must be positive", matchScore > 0f)
        assertEquals("Non-matching BM25 score must be 0", 0f, nonMatchScore, 0.0001f)
        assertTrue("Matching BM25 score must exceed non-matching score", matchScore > nonMatchScore)
    }

    @Test
    fun testLocalRagEmbeddingEngine_emptyQueryHandling() {
        val candidates = PreloadedData.defaultCurriculumChunks
        val emptyResult = LocalRagEmbeddingEngine.retrieveRankedMatches("", candidates)
        assertTrue("Empty query should return empty matches", emptyResult.topMatches.isEmpty())
        assertNull("Empty query should have no primary chunk", emptyResult.primaryGroundedChunk)

        val blankResult = LocalRagEmbeddingEngine.retrieveRankedMatches("   ", candidates)
        assertTrue("Blank query should return empty matches", blankResult.topMatches.isEmpty())
    }

    @Test
    fun testPreloadedData_allTribalLanguagesRepresented() {
        val chunks = PreloadedData.defaultCurriculumChunks
        val languages = chunks.map { it.tribalLanguage }.toSet()
        assertTrue("Must include SANTHALI", languages.contains("SANTHALI"))
        assertTrue("Must include HO", languages.contains("HO"))
        assertTrue("Must include MUNDARI", languages.contains("MUNDARI"))

        val glossary = PreloadedData.defaultGlossaryItems
        val hasSanthali = glossary.any { it.santhaliOlChiki.isNotBlank() }
        val hasHo = glossary.any { it.hoDevanagari.isNotBlank() }
        val hasMundari = glossary.any { it.mundariWord.isNotBlank() }
        assertTrue("Glossary must contain Santhali Ol Chiki", hasSanthali)
        assertTrue("Glossary must contain Ho", hasHo)
        assertTrue("Glossary must contain Mundari", hasMundari)
    }

    @Test
    fun testGlassColors_themeVariantsConfigured() {
        assertNotNull(com.example.ui.theme.GlassColorsLight.surface)
        assertNotNull(com.example.ui.theme.GlassColorsDark.surface)
        assertNotNull(com.example.ui.theme.GlassColorsLight.borderLight)
        assertNotNull(com.example.ui.theme.GlassColorsDark.borderLight)
        assertNotEquals(com.example.ui.theme.GlassColorsLight.surface, com.example.ui.theme.GlassColorsDark.surface)
    }

    @Test
    fun testDomainModels_entriesCompleteness() {
        val languages = com.example.domain.model.TargetLanguage.entries
        assertTrue("Should have at least 3 tribal languages", languages.size >= 3)
        assertTrue(languages.any { it.code == "sat" })
        assertTrue(languages.any { it.code == "hoc" })
        assertTrue(languages.any { it.code == "unr" })

        val grades = com.example.domain.model.GradeLevel.entries
        assertTrue("Grade levels must include foundational grades", grades.size >= 5)
    }

    @Test
    fun testOfflineVoiceTranslation_countingCategory() {
        val sat = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "बच्चों एक दो तीन गिनती करो",
            lang = com.example.domain.model.TargetLanguage.SANTHALI
        )
        assertTrue("Santhali counting should contain Ol Chiki numbers", sat.targetText.contains("ᱢᱤᱫ"))
        assertTrue("Phonetic transliteration should not be blank", sat.transliterationDevanagari.isNotBlank())

        val hoc = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "गिनती सीखें",
            lang = com.example.domain.model.TargetLanguage.HO
        )
        assertTrue("Ho counting should contain Warang Chiti numbers", hoc.targetText.contains("ᱢᱤᱭᱟᱹᱫᱽ"))

        val unr = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "गिनती शुरू करें",
            lang = com.example.domain.model.TargetLanguage.MUNDARI
        )
        assertTrue("Mundari counting should contain Devanagari numbers", unr.targetText.contains("मियाद"))
    }

    @Test
    fun testOfflineVoiceTranslation_hygieneCategory() {
        val sat = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "खाना खाने से पहले साबुन से हाथ धोएं",
            lang = com.example.domain.model.TargetLanguage.SANTHALI
        )
        assertTrue("Santhali hygiene text should contain handwash prompt", sat.targetText.contains("ᱛᱤ ᱟᱹᱨᱩᱵ"))

        val hoc = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "सफाई और हाथ धोना",
            lang = com.example.domain.model.TargetLanguage.HO
        )
        assertTrue("Ho hygiene text should contain abung prompt", hoc.targetText.contains("ᱛᱤ ᱟᱹᱵᱩᱝ"))
    }

    @Test
    fun testOfflineVoiceTranslation_animalsCategory() {
        val sat = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "गाय और बैल हमारे पालतू जानवर हैं",
            lang = com.example.domain.model.TargetLanguage.SANTHALI
        )
        assertTrue("Santhali animal text should contain Gay/cow", sat.targetText.contains("ᱜᱟᱹᱭ"))

        val unr = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "पक्षी और चिड़िया",
            lang = com.example.domain.model.TargetLanguage.MUNDARI
        )
        assertTrue("Mundari animal text should contain bird/gay prompt", unr.targetText.contains("गयी"))
    }

    @Test
    fun testOfflineVoiceTranslation_classroomManagement() {
        val sat = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "सभी बच्चे बैठ जाइए और ध्यान से सुनो",
            lang = com.example.domain.model.TargetLanguage.SANTHALI
        )
        assertTrue("Santhali seating should contain durub", sat.targetText.contains("ᱫᱩᱲᱩᱵ"))

        val hoc = com.example.data.repository.BhashaSetuRepository.getOfflineQuickTranslationData(
            hindi = "शांत रहिए और बैठ जाइए",
            lang = com.example.domain.model.TargetLanguage.HO
        )
        assertTrue("Ho seating should contain dub", hoc.targetText.contains("ᱫᱩᱵᱽ"))
    }

    @Test
    fun testVoiceTurnUtteranceIdTargeting() {
        val turnId = "turn_test_123"
        val tribalUtterance = "tr_$turnId"
        val hindiUtterance = "hi_$turnId"
        val relayHiUtterance = "relay_${turnId}_hi"
        val relayTrUtterance = "relay_${turnId}_tr"

        assertTrue(tribalUtterance.startsWith("tr_") && tribalUtterance.endsWith(turnId))
        assertTrue(hindiUtterance.startsWith("hi_") && hindiUtterance.endsWith(turnId))
        assertTrue(relayHiUtterance.startsWith("relay_$turnId"))
        assertTrue(relayTrUtterance.startsWith("relay_$turnId"))
    }

    @Test
    fun testWorksheetQuestion_jsonSerializationRoundTrip() {
        val original = WorksheetQuestion(
            id = "q_test_1",
            questionHindi = "साल के पेड़ को संथाली में क्या कहते हैं?",
            questionTarget = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (Sarjom Dare) ᱪᱮᱫ ᱠᱟᱱᱟ?",
            type = "MCQ",
            options = listOf("A. ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ", "B. ᱫᱟᱜ", "C. ᱥᱤᱝ", "D. ᱵᱤᱨ"),
            correctAnswer = "A. ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ",
            localContextHint = "सरहुल पर्व में साल के नए फूलों की पूजा की जाती है।"
        )

        val jsonString = listOf(original).toJsonString()
        assertTrue("JSON string should not be blank", jsonString.isNotBlank())
        assertTrue("JSON should contain Ol Chiki script", jsonString.contains("ᱥᱟᱨᱡᱚᱢ"))

        val parsedList = parseWorksheetQuestionsJson(jsonString)
        assertEquals(1, parsedList.size)
        val parsed = parsedList.first()
        assertEquals(original.id, parsed.id)
        assertEquals(original.questionHindi, parsed.questionHindi)
        assertEquals(original.questionTarget, parsed.questionTarget)
        assertEquals(original.type, parsed.type)
        assertEquals(original.options.size, parsed.options.size)
        assertEquals(original.correctAnswer, parsed.correctAnswer)
        assertEquals(original.localContextHint, parsed.localContextHint)
    }

    @Test
    fun testPreloadedWorksheets_allTribalLanguagesCovered() {
        val worksheets = PreloadedData.defaultWorksheets
        assertTrue("Must have at least 3 preloaded worksheets", worksheets.size >= 3)

        val santhaliWs = worksheets.find { it.targetLanguage.contains("Santhali") }
        assertNotNull("Santhali worksheet must be preloaded", santhaliWs)
        val satQuestions = santhaliWs!!.parseQuestions()
        assertEquals("Santhali worksheet must have 4 questions", 4, satQuestions.size)
        assertTrue("Santhali worksheet must contain Ol Chiki script",
            satQuestions.any { it.questionTarget.contains("ᱥᱟᱱᱛᱟᱲᱤ") || it.questionTarget.contains("ᱫᱟᱨᱮ") })

        val hoWs = worksheets.find { it.targetLanguage.contains("Ho") }
        assertNotNull("Ho worksheet must be preloaded", hoWs)
        val hoQuestions = hoWs!!.parseQuestions()
        assertEquals("Ho worksheet must have 4 questions", 4, hoQuestions.size)
        assertTrue("Ho worksheet must contain Ho script/numerals",
            hoQuestions.any { it.questionTarget.contains("ᱦᱳ") || it.questionTarget.contains("ᱢᱤᱭᱟᱹᱫᱽ") })

        val mundariWs = worksheets.find { it.targetLanguage.contains("Mundari") }
        assertNotNull("Mundari worksheet must be preloaded", mundariWs)
        val mundariQuestions = mundariWs!!.parseQuestions()
        assertEquals("Mundari worksheet must have 4 questions", 4, mundariQuestions.size)
        assertTrue("Mundari worksheet must contain Devanagari words",
            mundariQuestions.any { it.questionTarget.contains("मुण्डारी") || it.questionTarget.contains("पानी") })
    }

    @Test
    fun testWorksheetEntity_parseQuestionsExtension() {
        val emptyWs = WorksheetEntity(
            id = "ws_empty",
            lessonId = "les_x",
            title = "Empty",
            grade = "Grade 1",
            targetLanguage = "Santhali",
            instructions = "Instructions",
            questionsJson = ""
        )
        assertTrue("Empty questionsJson should return empty list gracefully", emptyWs.parseQuestions().isEmpty())

        val invalidWs = emptyWs.copy(questionsJson = "{ not valid json [")
        assertTrue("Invalid JSON should return empty list without crashing", invalidWs.parseQuestions().isEmpty())
    }

    @Test
    fun testAppUserMode_personaValuesAndToggles() {
        val teacher = com.example.domain.model.AppUserMode.TEACHER
        val student = com.example.domain.model.AppUserMode.STUDENT
        val community = com.example.domain.model.AppUserMode.COMMUNITY

        assertEquals("👩‍🏫", teacher.iconEmoji)
        assertEquals("🎒", student.iconEmoji)
        assertEquals("🏡", community.iconEmoji)
        assertTrue(teacher.titleHindi.contains("शिक्षक"))
        assertTrue(student.titleHindi.contains("बाल संसार"))
        assertTrue(community.titleHindi.contains("सांस्कृतिक"))
    }

    @Test
    fun testAppUserMode_personaCycleThroughAllThree() {
        var mode = com.example.domain.model.AppUserMode.TEACHER
        mode = when (mode) {
            com.example.domain.model.AppUserMode.TEACHER -> com.example.domain.model.AppUserMode.STUDENT
            com.example.domain.model.AppUserMode.STUDENT -> com.example.domain.model.AppUserMode.COMMUNITY
            com.example.domain.model.AppUserMode.COMMUNITY -> com.example.domain.model.AppUserMode.TEACHER
        }
        assertEquals(com.example.domain.model.AppUserMode.STUDENT, mode)

        mode = when (mode) {
            com.example.domain.model.AppUserMode.TEACHER -> com.example.domain.model.AppUserMode.STUDENT
            com.example.domain.model.AppUserMode.STUDENT -> com.example.domain.model.AppUserMode.COMMUNITY
            com.example.domain.model.AppUserMode.COMMUNITY -> com.example.domain.model.AppUserMode.TEACHER
        }
        assertEquals(com.example.domain.model.AppUserMode.COMMUNITY, mode)

        mode = when (mode) {
            com.example.domain.model.AppUserMode.TEACHER -> com.example.domain.model.AppUserMode.STUDENT
            com.example.domain.model.AppUserMode.STUDENT -> com.example.domain.model.AppUserMode.COMMUNITY
            com.example.domain.model.AppUserMode.COMMUNITY -> com.example.domain.model.AppUserMode.TEACHER
        }
        assertEquals(com.example.domain.model.AppUserMode.TEACHER, mode)
    }

    @Test
    fun testPreloadedStudentFlashcards_integrity() {
        val flashcards = PreloadedData.defaultStudentFlashcards
        assertTrue("Student flashcards must not be empty", flashcards.isNotEmpty())
        for (card in flashcards) {
            assertTrue("Card ID must be present", card.id.isNotBlank())
            assertTrue("Hindi word must be present", card.hindiWord.isNotBlank())
            assertTrue("Santhali Ol Chiki must be present", card.santhaliOlChiki.isNotBlank())
            assertTrue("Ho word must be present", card.hoWord.isNotBlank())
            assertTrue("Devanagari phonetic must be present", card.devanagariPhonetic.isNotBlank())
            assertTrue("Emoji must be present", card.iconEmoji.isNotBlank())
        }
    }

    @Test
    fun testPreloadedClassroomQuickPhrases_integrity() {
        val phrases = PreloadedData.defaultClassroomQuickPhrases
        assertTrue("Classroom quick phrases must not be empty", phrases.isNotEmpty())
        for (phrase in phrases) {
            assertTrue("Phrase ID must be present", phrase.id.isNotBlank())
            assertTrue("Hindi text must be present", phrase.hindiText.isNotBlank())
            assertTrue("Santhali Ol Chiki text must be present", phrase.santhaliOlChiki.isNotBlank())
            assertTrue("Ho text must be present", phrase.hoText.isNotBlank())
            assertTrue("Mundari text must be present", phrase.mundariText.isNotBlank())
        }
    }

    @Test
    fun testCurriculumPhoneticSynthesis_devanagariPhoneticsAvailable() {
        val chunks = PreloadedData.defaultCurriculumChunks
        assertTrue("Default curriculum chunks must not be empty", chunks.isNotEmpty())
        for (chunk in chunks) {
            val phonetic = chunk.transliterationDevanagari.ifBlank { chunk.tribalLessonText.ifBlank { chunk.lessonTextHindi } }
            assertTrue("Tribal curriculum chunk '${chunk.id}' must have playable phonetic transliteration for hi-IN TTS synthesis", phonetic.isNotBlank())
        }
    }

    @Test
    fun testCurriculumGradeFiltering_substringMatchContract() {
        val chunks = PreloadedData.defaultCurriculumChunks
        val grade2Chunks = chunks.filter { it.grade.contains("Grade 2", ignoreCase = true) || it.grade.contains("2") }
        assertTrue("Must find at least one Grade 2 curriculum chunk", grade2Chunks.isNotEmpty())
        for (c in grade2Chunks) {
            assertTrue("Chunk grade must contain 2: ${c.grade}", c.grade.contains("2"))
        }
    }

    @Test
    fun testCurriculumSearchCoverage_nativeScriptAndDevanagari() {
        val chunks = PreloadedData.defaultCurriculumChunks
        val santhaliChunks = chunks.filter { it.tribalLanguage == "SANTHALI" }
        assertTrue("Must have Santhali curriculum chunks", santhaliChunks.isNotEmpty())
        for (sc in santhaliChunks) {
            assertTrue("Santhali script type must be OL_CHIKI", sc.tribalScriptType == "OL_CHIKI")
            assertTrue("Native script text must not be blank", sc.tribalNativeScriptText.isNotBlank())
        }
    }

    @Test
    fun testStress_hybridRagRetrievalThroughput() {
        val candidates = PreloadedData.defaultCurriculumChunks
        val queries = listOf(
            "साल का पेड़ Sarjom",
            "गिनती और संख्या 1 से 10",
            "जल चक्र और वर्षा",
            "पौधों के भाग और पत्ते",
            "स्वास्थ्य और स्वच्छता",
            "पहाड़ और नदियाँ झारखंड",
            "दारे और रूहे",
            "सरना धर्म और प्रकृति"
        )
        val numQueries = 200
        val threadPool = java.util.concurrent.Executors.newFixedThreadPool(8)
        val startTime = System.nanoTime()

        val futures = (0 until numQueries).map { index ->
            threadPool.submit(java.util.concurrent.Callable {
                val q = queries[index % queries.size]
                val lang = when (index % 3) {
                    0 -> "SANTHALI"
                    1 -> "HO"
                    else -> "MUNDARI"
                }
                LocalRagEmbeddingEngine.retrieveRankedMatches(
                    query = q,
                    candidateChunks = candidates,
                    targetLanguageFilter = lang,
                    topK = 3
                )
            })
        }

        threadPool.shutdown()
        val finishedInTime = threadPool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
        val totalDurationMs = (System.nanoTime() - startTime) / 1_000_000.0

        assertTrue("Stress execution must finish within timeout", finishedInTime)
        assertEquals(numQueries, futures.size)

        var totalLatencySum = 0.0
        for (f in futures) {
            val result = f.get()
            assertNotNull("Context must not be null", result)
            totalLatencySum += result.retrievalLatencyMs
        }

        val avgQueryLatencyMs = totalDurationMs / numQueries
        println("RAG Retrieval Stress: $numQueries queries across 8 threads completed in ${totalDurationMs}ms (avg per query: ${avgQueryLatencyMs}ms)")
        assertTrue("Average wall-clock time per query under 8-thread load must be < 15ms (was ${avgQueryLatencyMs}ms)", avgQueryLatencyMs < 15.0)
    }

    @Test
    fun testStress_vectorDimensionalityAndCosineStability() {
        val sampleWords = listOf(
            "सरजोम", "दारे", "रुहे", "होड़ो", "ओल चिकी", "वारंग क्षिति", "मुंडारी",
            "पेड़", "पौधा", "प्रकृति", "पानी", "हवा", "आकाश", "मिट्टी", "सूरज"
        )
        val iterations = 500
        val dimension = LocalRagEmbeddingEngine.VECTOR_DIMENSION

        for (i in 0 until iterations) {
            val phrase = "${sampleWords[i % sampleWords.size]} ${sampleWords[(i * 3) % sampleWords.size]} $i"
            val vec = LocalRagEmbeddingEngine.embedText(phrase)

            assertEquals("Dimension must strictly match $dimension", dimension, vec.size)

            var sumSquares = 0.0
            for (v in vec) {
                assertFalse("Vector elements must not be NaN", v.isNaN())
                assertFalse("Vector elements must not be Infinite", v.isInfinite())
                sumSquares += (v * v)
            }
            val norm = Math.sqrt(sumSquares)
            assertEquals("Vector must be unit normalized", 1.0, norm, 0.005)
        }
    }

    @Test
    fun testStress_classroomQuickPhraseConcurrency() {
        val phrases = PreloadedData.defaultClassroomQuickPhrases
        val threadPool = java.util.concurrent.Executors.newFixedThreadPool(6)
        val concurrencyCount = 300

        val tasks = (0 until concurrencyCount).map { i ->
            threadPool.submit(java.util.concurrent.Callable {
                val phrase = phrases[i % phrases.size]
                // Test simultaneous resolution of all three tribal representations
                val santhali = phrase.santhaliOlChiki
                val ho = phrase.hoText
                val mundari = phrase.mundariText
                assertTrue(santhali.isNotBlank() && ho.isNotBlank() && mundari.isNotBlank())
                phrase.id
            })
        }

        threadPool.shutdown()
        val finished = threadPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)
        assertTrue("Phrase concurrency test must complete in time", finished)
        assertEquals(concurrencyCount, tasks.count { it.get().isNotBlank() })
    }

    @Test
    fun testStress_worksheetQuestionJsonSerializationUnderLoad() {
        val originalQuestions = listOf(
            WorksheetQuestion(
                id = "q1",
                questionHindi = "सरजोम दारे का क्या अर्थ है?",
                questionTarget = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱨᱮᱱᱟᱜ ᱢᱮᱱᱮᱛ ᱫᱚ ᱪᱮᱫ ᱠᱟᱱᱟ?",
                type = "MCQ",
                options = listOf("साल का पेड़", "बरगद का पेड़", "आम का पेड़", "नीम का पेड़"),
                correctAnswer = "साल का पेड़",
                localContextHint = "संथाली में सरजोम का अर्थ साल वृक्ष होता है।"
            ),
            WorksheetQuestion(
                id = "q2",
                questionHindi = "ओल चिकी लिपि का आविष्कार किसने किया?",
                questionTarget = "ᱚᱞ ᱪᱤᱠᱤ ᱞᱤᱯᱤ ᱫᱚ ᱚᱠᱚᱭ ᱮ ᱛᱮᱭᱟᱨ ᱞᱮᱫ-ᱟ?",
                type = "MCQ",
                options = listOf("पंडित रघुनाथ मुर्मू", "बिरसा मुंडा", "सिद्धू कान्हू", "तिलका मांझी"),
                correctAnswer = "पंडित रघुनाथ मुर्मू",
                localContextHint = "वर्ष 1925 में पंडित रघुनाथ मुर्मू जी ने ओल चिकी का आविष्कार किया।"
            )
        )

        val iterations = 500
        for (i in 0 until iterations) {
            val json = originalQuestions.toJsonString()
            assertTrue("JSON should contain key markers", json.contains("q1") && json.contains("q2"))
            val parsed = parseWorksheetQuestionsJson(json)
            assertEquals(2, parsed.size)
            assertEquals(originalQuestions[0].questionHindi, parsed[0].questionHindi)
            assertEquals(originalQuestions[1].correctAnswer, parsed[1].correctAnswer)
        }
    }
}

