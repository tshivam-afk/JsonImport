package com.example.biologytester.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biologytester.data.AppDatabase
import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActiveQuizViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).quizDao()

    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private var isLoaded = false

    fun loadQuiz(quizId: Long) {
        if (isLoaded) return

        viewModelScope.launch {
            val q = dao.getQuizById(quizId)
            if (q != null) {
                _quiz.value = q
                val qs = dao.getQuestionsForQuiz(quizId)
                _questions.value = qs
                _currentIndex.value = 0
                isLoaded = true
            }
        }
    }

    fun answerQuestion(question: Question, selectedIndex: Int) {
        viewModelScope.launch {
            val isCorrect = question.correctIndex == selectedIndex
            val updatedQuestion = question.copy(
                isAnswered = true,
                selectedIndex = selectedIndex,
                isCorrect = isCorrect
            )
            dao.updateQuestion(updatedQuestion)

            // Update local state
            val updatedList = _questions.value.toMutableList()
            val index = updatedList.indexOfFirst { it.id == question.id }
            if (index != -1) {
                updatedList[index] = updatedQuestion
                _questions.value = updatedList
            }

            // Update best score if needed
            val currentScore = getScore(updatedList)
            val currentQuiz = _quiz.value
            if (currentQuiz != null && currentScore > currentQuiz.bestScore) {
                dao.updateQuiz(currentQuiz.copy(bestScore = currentScore))
            }
        }
    }

    fun toggleFlag(question: Question) {
        viewModelScope.launch {
            val updatedQuestion = question.copy(isFlagged = !question.isFlagged)
            dao.updateQuestion(updatedQuestion)

            val updatedList = _questions.value.toMutableList()
            val index = updatedList.indexOfFirst { it.id == question.id }
            if (index != -1) {
                updatedList[index] = updatedQuestion
                _questions.value = updatedList
            }
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value++
        }
    }

    fun previousQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }

    fun goToQuestion(index: Int) {
        if (index in 0 until _questions.value.size) {
            _currentIndex.value = index
        }
    }

    fun getScore(questionList: List<Question> = _questions.value): Int {
        return questionList.count { it.isAnswered && it.isCorrect }
    }

    fun saveTimeSpent(seconds: Int) {
        viewModelScope.launch {
            val currentQuiz = _quiz.value
            if (currentQuiz != null) {
                dao.updateQuiz(currentQuiz.copy(timeSpentSeconds = currentQuiz.timeSpentSeconds + seconds))
            }
        }
    }
}
