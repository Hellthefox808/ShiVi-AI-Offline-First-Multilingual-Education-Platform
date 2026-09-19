package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.WorksheetQuestion
import com.example.domain.model.parseWorksheetQuestionsJson

@Entity(
    tableName = "lessons",
    indices = [
        Index("status"),
        Index("targetLanguage"),
        Index("grade")
    ]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val grade: String,
    val subject: String,
    val learningOutcome: String,
    val hindiPrompt: String,
    val targetLanguage: String,
    val adaptedExplanation: String,
    val nativeScriptText: String,
    val transliterationText: String,
    val culturalAnalogy: String,
    val activityPrompt: String,
    val pronunciationGuide: String,
    val status: String, // DRAFT, GENERATING, REVIEW_REQUIRED, APPROVED, PUBLISHED
    val qualityScore: Float,
    val groundingScore: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "worksheets",
    indices = [
        Index("lessonId"),
        Index("isApproved")
    ]
)
data class WorksheetEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val title: String,
    val grade: String,
    val targetLanguage: String,
    val instructions: String,
    val questionsJson: String,
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun WorksheetEntity.parseQuestions(): List<WorksheetQuestion> =
    parseWorksheetQuestionsJson(this.questionsJson)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val hindiWord: String,
    val targetWord: String,
    val transliteration: String,
    val scriptVisual: String,
    val category: String,
    val culturalNote: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rollNo: String,
    val grade: String,
    val village: String,
    val totalScore: Int = 0,
    val lessonsCompleted: Int = 0
)

@Entity(tableName = "assessment_attempts")
data class AssessmentAttemptEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val lessonId: String,
    val lessonTitle: String,
    val score: Int,
    val maxScore: Int,
    val answersJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING_OUTBOX"
)

@Entity(
    tableName = "glossary",
    indices = [
        Index("category"),
        Index("hindiWord")
    ]
)
data class GlossaryEntity(
    @PrimaryKey val id: String,
    val category: String,
    val hindiWord: String,
    val santhaliWord: String,
    val santhaliOlChiki: String,
    val hoWord: String,
    val hoDevanagari: String,
    val mundariWord: String,
    val pronunciation: String,
    val englishMeaning: String,
    val exampleSentenceHindi: String,
    val exampleSentenceTarget: String
)

@Entity(
    tableName = "outbox",
    indices = [
        Index("status"),
        Index("createdAt"),
        Index("sequenceNumber")
    ]
)
data class OutboxEntity(
    @PrimaryKey val id: String,
    val operationId: String,
    val entityType: String,
    val operation: String, // INSERT, UPDATE, SUBMIT_ASSESSMENT, APPROVE_LESSON
    val payloadJson: String,
    val sequenceNumber: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING" // PENDING, SENDING, ACKNOWLEDGED, FAILED
)

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val recordsPushed: Int,
    val recordsPulled: Int,
    val networkLatencyMs: Long,
    val details: String
)

/**
 * Offline-First Curriculum Content Entity for RAG (Retrieval-Augmented Generation).
 * Stores syllabus text, pedagogical metadata (Bloom's, FLN learning outcomes),
 * tribal language sources (Santhali, Ho, Mundari), and cultural grounding context.
 */
@Entity(
    tableName = "curriculum_rag_content",
    indices = [
        Index("tribalLanguage"),
        Index("grade"),
        Index(value = ["grade", "tribalLanguage"]),
        Index("learningOutcomeCode")
    ]
)
data class CurriculumContentEntity(
    @PrimaryKey val id: String,
    // Academic & Curriculum Hierarchy
    val state: String = "झारखंड (Jharkhand)",
    val curriculumBoard: String = "JCERT / NCERT",
    val grade: String, // "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5"
    val subject: String, // "भाषा व बुनियादी साक्षरता (FLN)", "गणित व संख्या ज्ञान", "पर्यावरण अध्ययन (EVS)", "कला व संस्कृति"
    val chapterNumber: Int,
    val chapterTitle: String,
    val topic: String,
    val learningOutcomeCode: String, // e.g. "FLN-JH-L1-02", "JCERT-EVS-G2-05"
    val learningOutcomeDescription: String,

    // Lesson Text & Core Pedagogical Content (Hindi Primary)
    val lessonTextHindi: String,
    val pedagogicalExplanationHindi: String,
    val classroomActivityPrompt: String,
    val oralAssessmentQuestion: String,

    // Tribal Language Source & Native Script Alignment
    val tribalLanguage: String, // "SANTHALI", "HO", "MUNDARI"
    val tribalLessonText: String, // Target translated content
    val tribalScriptType: String, // "OL_CHIKI", "WARANG_CHITI", "DEVANAGARI_PHONETIC"
    val tribalNativeScriptText: String, // Native script glyphs (e.g. Ol Chiki: ᱫᱟᱨᱮ ᱟᱨ ᱫᱟᱜ)
    val transliterationLatin: String, // Roman phonetics for non-tribal teachers
    val transliterationDevanagari: String, // Devanagari phonetics for Hindi teachers
    val dialectOrRegion: String, // e.g. "संथाल परगना (Santhal Pargana)", "कोल्हान (Kolhan)", "खूंटी / राँची"
    val culturalContextTag: String, // e.g. "सरहुल (Baha Porob)", "सोहराय (Sohrai)", "करम पूजा", "जाहेरथान"

    // Pedagogical Metadata & RAG Indexing
    val bloomsTaxonomyLevel: String, // "REMEMBER", "UNDERSTAND", "APPLY", "EVALUATE"
    val difficultyLevel: String, // "FOUNDATIONAL", "INTERMEDIATE", "ADVANCED"
    val ageGroupMinYears: Int = 5,
    val ageGroupMaxYears: Int = 8,
    val estimatedDurationMinutes: Int = 45,
    val keywordsForRetrieval: String, // Comma-separated lexical tokens for full-text & hybrid RAG
    val ragDenseVectorTag: String? = null, // Optional vector cluster/identifier
    val approvalStatus: String = "JCERT_VERIFIED", // "JCERT_VERIFIED", "COMMUNITY_VALIDATED", "DRAFT"
    val textbookSourceReference: String, // e.g. "JCERT भाषा अंजलि कक्षा 2, पृष्ठ 24"
    val version: Int = 1,
    val isOfflineAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

