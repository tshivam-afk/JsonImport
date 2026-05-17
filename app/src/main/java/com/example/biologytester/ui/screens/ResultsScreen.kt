package com.example.biologytester.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.biologytester.ui.theme.CorrectBorderLight
import com.example.biologytester.ui.theme.FlaggedColor
import com.example.biologytester.ui.theme.WrongBorderLight
import com.example.biologytester.viewmodel.ActiveQuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    quizId: Long,
    onNavigateHome: () -> Unit,
    viewModel: ActiveQuizViewModel = viewModel()
) {
    val quiz by viewModel.quiz.collectAsState()
    val questions by viewModel.questions.collectAsState()

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    if (quiz == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val correctCount = questions.count { it.isAnswered && it.isCorrect }
    val wrongCount = questions.count { it.isAnswered && !it.isCorrect }
    val flaggedCount = questions.count { it.isFlagged }
    val percentage = if (questions.isNotEmpty()) (correctCount.toFloat() / questions.size * 100).toInt() else 0

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage / 100f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Library")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (percentage >= 80) "Excellent Work! 🎉" else if (percentage >= 50) "Good Job! 👍" else "Keep Practicing! 💪",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Circular Progress Chart
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val circleColor = MaterialTheme.colorScheme.surfaceVariant
                val progressColor = if (percentage >= 70) CorrectBorderLight else if (percentage >= 40) FlaggedColor else WrongBorderLight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = circleColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress.value * 360f,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress.value * 100).toInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox("✅", correctCount.toString(), "Correct", CorrectBorderLight)
                StatBox("❌", wrongCount.toString(), "Incorrect", WrongBorderLight)
                StatBox("🚩", flaggedCount.toString(), "Flagged", FlaggedColor)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Return to Library", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatBox(icon: String, value: String, label: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
