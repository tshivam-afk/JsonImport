package com.example.biologytester.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateImported: Long,
    val bestScore: Int = 0,
    val totalQuestions: Int = 0,
    val timeSpentSeconds: Int = 0 // Track time spent
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val optionsJson: String, // Stored as JSON string
    val correctIndex: Int,
    val explanation: String,

    // Progress fields
    val isFlagged: Boolean = false,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val selectedIndex: Int = -1 // -1 if not answered
) {
    fun getOptionsList(): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(optionsJson, type) ?: emptyList()
    }
}
