package com.example

import com.example.data.seed.PreloadedData
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
}
