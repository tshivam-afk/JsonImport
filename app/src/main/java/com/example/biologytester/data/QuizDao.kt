package com.example.biologytester.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.biologytester.model.Question
import com.example.biologytester.model.Quiz
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY dateImported DESC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: Long): Quiz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)

    @Update
    suspend fun updateQuiz(quiz: Quiz)

    // Questions
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: Long): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Query("UPDATE questions SET isFlagged = 0, isAnswered = 0, isCorrect = 0, selectedIndex = -1 WHERE quizId = :quizId")
    suspend fun resetQuizProgress(quizId: Long)

    @Query("SELECT * FROM questions WHERE isFlagged = 1 OR (isAnswered = 1 AND isCorrect = 0)")
    fun getReviewQuestions(): Flow<List<Question>>
}
