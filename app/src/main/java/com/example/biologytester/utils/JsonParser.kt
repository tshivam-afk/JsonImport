package com.example.biologytester.utils

import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException

data class JsonQuestionInput(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

object JsonParser {
    fun parseQuizJson(jsonString: String, title: String): Pair<Quiz, List<Question>>? {
        return try {
            val type = object : TypeToken<List<JsonQuestionInput>>() {}.type
            val inputQuestions: List<JsonQuestionInput> = Gson().fromJson(jsonString, type)

            if (inputQuestions.isEmpty()) return null

            val quiz = Quiz(
                title = title,
                dateImported = System.currentTimeMillis(),
                totalQuestions = inputQuestions.size
            )

            val questions = inputQuestions.map { input ->
                Question(
                    quizId = 0, // Will be set after quiz is inserted
                    questionText = input.question,
                    optionsJson = Gson().toJson(input.options),
                    correctIndex = input.correctIndex,
                    explanation = input.explanation
                )
            }

            Pair(quiz, questions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
