package com.example.biologytester.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz

@Database(entities = [Quiz::class, Question::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                )
                .fallbackToDestructiveMigration() // Reset DB for schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
