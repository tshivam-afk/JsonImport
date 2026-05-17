package com.example.biologytester.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biologytester.data.AppDatabase
import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz
import com.example.biologytester.utils.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).quizDao()

    val quizzes: StateFlow<List<Quiz>> = dao.getAllQuizzes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reviewQuestions: StateFlow<List<Question>> = dao.getReviewQuestions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus = _importStatus.asStateFlow()

    fun importQuiz(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                val title = getFileName(context, uri) ?: "Imported Quiz"
                importQuiz(jsonString, title)
            } catch (e: Exception) {
                _importStatus.value = "Error reading file: \${e.message}"
            }
        }
    }

    private fun getFileName(context: android.content.Context, uri: android.net.Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        // remove extension
        return result?.substringBeforeLast(".") ?: result
    }

    fun importQuiz(jsonString: String, title: String) {
        viewModelScope.launch {
            try {
                val parsed = JsonParser.parseQuizJson(jsonString, title)
                if (parsed != null) {
                    val (quiz, questions) = parsed
                    val quizId = dao.insertQuiz(quiz)

                    val questionsWithQuizId = questions.map { it.copy(quizId = quizId) }
                    dao.insertQuestions(questionsWithQuizId)

                    _importStatus.value = "Success"
                } else {
                    _importStatus.value = "Error: Invalid JSON format"
                }
            } catch (e: Exception) {
                _importStatus.value = "Error: \${e.message}"
            }
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }

    fun deleteQuiz(quiz: Quiz) {
        viewModelScope.launch {
            dao.deleteQuestionsForQuiz(quiz.id)
            dao.deleteQuiz(quiz)
        }
    }

    fun resetQuizProgress(quizId: Long) {
        viewModelScope.launch {
            dao.resetQuizProgress(quizId)
            // also reset time tracking for the quiz
            val quiz = dao.getQuizById(quizId)
            if (quiz != null) {
                dao.updateQuiz(quiz.copy(timeSpentSeconds = 0))
            }
        }
    }
}
