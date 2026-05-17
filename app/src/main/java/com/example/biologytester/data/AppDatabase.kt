package com.example.biologytester.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz

@Database(entities = [Quiz::class, Question::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to quizzes table
                db.execSQL("ALTER TABLE quizzes ADD COLUMN timeSpentSeconds INTEGER NOT NULL DEFAULT 0")

                // Add new columns to questions table
                db.execSQL("ALTER TABLE questions ADD COLUMN isFlagged INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN isAnswered INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN isCorrect INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN selectedIndex INTEGER NOT NULL DEFAULT -1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to quizzes table
                db.execSQL("ALTER TABLE quizzes ADD COLUMN timeSpentSeconds INTEGER NOT NULL DEFAULT 0")

                // Add new columns to questions table
                db.execSQL("ALTER TABLE questions ADD COLUMN isFlagged INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN isAnswered INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN isCorrect INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE questions ADD COLUMN selectedIndex INTEGER NOT NULL DEFAULT -1")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                )
                // Add explicit SQL migrations to preserve user data instead of throwing Exceptions
                .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
