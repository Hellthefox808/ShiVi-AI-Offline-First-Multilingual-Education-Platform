package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LessonEntity::class,
        WorksheetEntity::class,
        FlashcardEntity::class,
        StudentEntity::class,
        AssessmentAttemptEntity::class,
        GlossaryEntity::class,
        OutboxEntity::class,
        SyncLogEntity::class,
        CurriculumContentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun worksheetDao(): WorksheetDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studentDao(): StudentDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun glossaryDao(): GlossaryDao
    abstract fun outboxDao(): OutboxDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun curriculumDao(): CurriculumDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bhashasetu_local_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
