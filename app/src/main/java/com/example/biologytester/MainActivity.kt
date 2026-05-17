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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.biologytester.ota.UpdateManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.biologytester.ota.GithubRelease

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val updateManager = remember { UpdateManager(this@MainActivity) }
            val showUpdateDialog = remember { mutableStateOf<GithubRelease?>(null) }

            LaunchedEffect(Unit) {
                try {
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val currentVersion = "v" + packageInfo.versionName
                    val release = updateManager.checkForUpdates(currentVersion)
                    if (release != null && release.assets.isNotEmpty()) {
                        showUpdateDialog.value = release
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (showUpdateDialog.value != null) {
                val release = showUpdateDialog.value!!
                AlertDialog(
                    onDismissRequest = { showUpdateDialog.value = null },
                    title = { Text("Update Available") },
                    text = { Text("A new version (${release.tag_name}) is available. Would you like to update now?\n\n${release.body}") },
                    confirmButton = {
                        Button(onClick = {
                            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                            if (apkAsset != null) {
                                updateManager.downloadAndInstall(apkAsset.browser_download_url, apkAsset.name)
                            }
                            showUpdateDialog.value = null
                        }) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showUpdateDialog.value = null }) {
                            Text("Later")
                        }
                    }
                )
            }
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
                                onNavigateToReview = { navController.navigate(Screen.Review.route) },
                                onNavigateToSettings = { navController.navigate("settings") }
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
                        composable("settings") {
                            com.example.biologytester.ui.screens.SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
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
