package com.example.biologytester.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Library : Screen("library")
    object Quiz : Screen("quiz/{quizId}") {
        fun createRoute(quizId: Long) = "quiz/$quizId"
    }
    object Results : Screen("results/{quizId}") {
        fun createRoute(quizId: Long) = "results/$quizId"
    }
    object Review : Screen("review")
}
