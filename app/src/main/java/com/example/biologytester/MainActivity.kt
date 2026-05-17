package com.example.biologytester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.biologytester.navigation.Screen
import com.example.biologytester.ui.screens.LibraryScreen
import com.example.biologytester.ui.screens.QuizScreen
import com.example.biologytester.ui.screens.ResultsScreen
import com.example.biologytester.ui.screens.ReviewScreen
import com.example.biologytester.ui.screens.SplashScreen
import com.example.biologytester.ui.theme.BiologyPracticeTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiologyPracticeTesterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.Splash.route) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate(Screen.Library.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Library.route) {
                            LibraryScreen(
                                onNavigateToQuiz = { quizId -> navController.navigate(Screen.Quiz.createRoute(quizId)) },
                                onNavigateToReview = { navController.navigate(Screen.Review.route) }
                            )
                        }
                        composable(
                            route = Screen.Quiz.route,
                            arguments = listOf(navArgument("quizId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getLong("quizId") ?: 0L
                            QuizScreen(
                                quizId = quizId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToResults = { navController.navigate(Screen.Results.createRoute(it)) {
                                    popUpTo(Screen.Library.route)
                                } }
                            )
                        }
                        composable(
                            route = Screen.Results.route,
                            arguments = listOf(navArgument("quizId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getLong("quizId") ?: 0L
                            ResultsScreen(
                                quizId = quizId,
                                onNavigateHome = { navController.navigate(Screen.Library.route) {
                                    popUpTo(0)
                                } }
                            )
                        }
                        composable(Screen.Review.route) {
                            ReviewScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
