package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY createdAt DESC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLesson(id: String)

    @Query("SELECT COUNT(*) FROM lessons WHERE status = 'APPROVED' OR status = 'PUBLISHED'")
    fun getApprovedLessonCount(): Flow<Int>
}

@Dao
interface WorksheetDao {
    @Query("SELECT * FROM worksheets WHERE lessonId = :lessonId")
    fun getWorksheetsForLesson(lessonId: String): Flow<List<WorksheetEntity>>

    @Query("SELECT * FROM worksheets ORDER BY createdAt DESC")
    fun getAllWorksheets(): Flow<List<WorksheetEntity>>

    @Query("SELECT * FROM worksheets WHERE id = :id")
    suspend fun getWorksheetById(id: String): WorksheetEntity?

    @Query("UPDATE worksheets SET isApproved = 1 WHERE id = :id")
    suspend fun approveWorksheet(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheet(worksheet: WorksheetEntity)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY createdAt DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE lessonId = :lessonId")
    fun getFlashcardsForLesson(lessonId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE category = :category")
    fun getFlashcardsByCategory(category: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)
}

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessment_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<AssessmentAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AssessmentAttemptEntity)

    @Query("SELECT AVG(score * 100.0 / maxScore) FROM assessment_attempts")
    fun getAverageClassScore(): Flow<Double?>
}

@Dao
interface GlossaryDao {
    @Query("SELECT * FROM glossary ORDER BY category ASC, hindiWord ASC")
    fun getAllGlossary(): Flow<List<GlossaryEntity>>

    @Query("SELECT * FROM glossary WHERE hindiWord LIKE '%' || :query || '%' OR santhaliWord LIKE '%' || :query || '%' OR hoWord LIKE '%' || :query || '%' OR mundariWord LIKE '%' || :query || '%'")
    fun searchGlossary(query: String): Flow<List<GlossaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(glossary: List<GlossaryEntity>)
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox WHERE status = 'PENDING' ORDER BY sequenceNumber ASC")
    fun getPendingOutbox(): Flow<List<OutboxEntity>>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(outbox: OutboxEntity)

    @Query("UPDATE outbox SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM outbox WHERE status = 'ACKNOWLEDGED'")
    suspend fun clearAcknowledged()
}

@Dao
interface SyncLogDao {
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSyncLogs(): Flow<List<SyncLogEntity>>

    @Insert
    suspend fun insertLog(log: SyncLogEntity)
}

@Dao
interface CurriculumDao {
    @Query("SELECT * FROM curriculum_rag_content ORDER BY grade ASC, chapterNumber ASC")
    fun getAllCurriculum(): Flow<List<CurriculumContentEntity>>

    @Query("SELECT * FROM curriculum_rag_content WHERE grade = :grade AND subject = :subject ORDER BY chapterNumber ASC")
    fun getCurriculumByGradeAndSubject(grade: String, subject: String): Flow<List<CurriculumContentEntity>>

    @Query("SELECT * FROM curriculum_rag_content WHERE tribalLanguage = :tribalLanguage ORDER BY grade ASC, chapterNumber ASC")
    fun getCurriculumByLanguage(tribalLanguage: String): Flow<List<CurriculumContentEntity>>

    @Query("SELECT * FROM curriculum_rag_content WHERE learningOutcomeCode = :outcomeCode LIMIT 1")
    suspend fun getByLearningOutcome(outcomeCode: String): CurriculumContentEntity?

    @Query("""
        SELECT * FROM curriculum_rag_content 
        WHERE (:language IS NULL OR tribalLanguage = :language)
        AND (:grade IS NULL OR grade LIKE '%' || :grade || '%')
        AND (
            lessonTextHindi LIKE '%' || :query || '%' 
            OR topic LIKE '%' || :query || '%' 
            OR chapterTitle LIKE '%' || :query || '%'
            OR keywordsForRetrieval LIKE '%' || :query || '%' 
            OR tribalLessonText LIKE '%' || :query || '%' 
            OR transliterationLatin LIKE '%' || :query || '%'
            OR culturalContextTag LIKE '%' || :query || '%'
            OR tribalNativeScriptText LIKE '%' || :query || '%'
            OR transliterationDevanagari LIKE '%' || :query || '%'
            OR learningOutcomeCode LIKE '%' || :query || '%'
            OR subject LIKE '%' || :query || '%'
        )
        ORDER BY grade ASC, chapterNumber ASC
    """)
    fun searchCurriculumRAG(
        query: String,
        language: String? = null,
        grade: String? = null
    ): Flow<List<CurriculumContentEntity>>

    @Query("SELECT * FROM curriculum_rag_content WHERE id = :id")
    suspend fun getCurriculumById(id: String): CurriculumContentEntity?

    @Query("SELECT COUNT(*) FROM curriculum_rag_content")
    fun getCurriculumCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(curriculum: List<CurriculumContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: CurriculumContentEntity)

    @Update
    suspend fun updateChunk(chunk: CurriculumContentEntity)

    @Query("DELETE FROM curriculum_rag_content WHERE id = :id")
    suspend fun deleteChunkById(id: String)
}

