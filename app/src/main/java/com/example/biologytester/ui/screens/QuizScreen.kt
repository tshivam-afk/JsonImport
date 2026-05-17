package com.example.biologytester.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.biologytester.model.Question
import com.example.biologytester.ui.theme.*
import com.example.biologytester.viewmodel.ActiveQuizViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quizId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (Long) -> Unit,
    viewModel: ActiveQuizViewModel = viewModel()
) {
    val quiz by viewModel.quiz.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    // Timer state
    var secondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            secondsElapsed++
        }
    }

    if (quiz == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val paletteScrollState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        paletteScrollState.animateScrollToItem(currentIndex)
    }

    val formatTime = { s: Int ->
        val mins = s / 60
        val secs = s % 60
        String.format("%02d:%02d", mins, secs)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz?.title ?: "Quiz", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱ ", fontSize = 14.sp)
                            Text(formatTime(secondsElapsed), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.previousQuestion() },
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Prev")
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.saveTimeSpent(secondsElapsed)
                                onNavigateToResults(quizId)
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("Finish", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Question Palette
            LazyRow(
                state = paletteScrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(questions) { index, q ->
                    PaletteButton(
                        index = index,
                        question = q,
                        isCurrent = index == currentIndex,
                        onClick = { viewModel.goToQuestion(index) }
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Question Section
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp).padding(horizontal = 20.dp),
                label = "QuestionAnimation"
            ) { targetIndex ->
                val q = questions[targetIndex]
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${targetIndex + 1} of ${questions.size}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            OutlinedButton(
                                onClick = { viewModel.toggleFlag(q) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (q.isFlagged) FlaggedColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = BorderStroke(1.dp, if (q.isFlagged) FlaggedColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(if (q.isFlagged) Icons.Default.Warning else Icons.Default.Warning, contentDescription = "Flag", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (q.isFlagged) "Flagged" else "Flag", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 32.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Options
                        val options = q.getOptionsList()
                        options.forEachIndexed { optIndex, optText ->
                            OptionItem(
                                text = optText,
                                isSelected = q.selectedIndex == optIndex,
                                isCorrectOption = optIndex == q.correctIndex,
                                isAnswered = q.isAnswered,
                                onClick = {
                                    if (!q.isAnswered) {
                                        viewModel.answerQuestion(q, optIndex)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Explanation
                        AnimatedVisibility(
                            visible = q.isAnswered,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(24.dp))
                                ExplanationCard(
                                    isCorrect = q.isCorrect,
                                    explanation = q.explanation,
                                    correctAnswer = options[q.correctIndex]
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaletteButton(index: Int, question: Question, isCurrent: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        question.isAnswered && question.isCorrect -> CorrectBgLight
        question.isAnswered && !question.isCorrect -> WrongBgLight
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        question.isAnswered && question.isCorrect -> CorrectTextLight
        question.isAnswered && !question.isCorrect -> WrongTextLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        question.isAnswered && question.isCorrect -> CorrectBorderLight
        question.isAnswered && !question.isCorrect -> WrongBorderLight
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (borderColor != Color.Transparent) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.Transparent).border(1.dp, borderColor, RoundedCornerShape(12.dp)))
        }

        Text(
            text = (index + 1).toString(),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        if (question.isFlagged) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(FlaggedColor)
            )
        }
    }
}

@Composable
fun OptionItem(
    text: String,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isAnswered && isCorrectOption -> CorrectBgLight
        isAnswered && isSelected && !isCorrectOption -> WrongBgLight
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isAnswered && isCorrectOption -> CorrectBorderLight
        isAnswered && isSelected && !isCorrectOption -> WrongBorderLight
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    val icon = when {
        isAnswered && isCorrectOption -> Icons.Default.Check
        isAnswered && isSelected && !isCorrectOption -> Icons.Default.Close
        else -> null
    }

    val iconTint = when {
        isAnswered && isCorrectOption -> CorrectTextLight
        isAnswered && isSelected && !isCorrectOption -> WrongTextLight
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textColor = when {
        isAnswered && isCorrectOption -> CorrectTextLight
        isAnswered && isSelected && !isCorrectOption -> WrongTextLight
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAnswered, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isSelected || (isAnswered && isCorrectOption)) 2.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected || (isAnswered && isCorrectOption),
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isAnswered && isCorrectOption) CorrectTextLight
                                    else if (isAnswered && !isCorrectOption) WrongTextLight
                                    else MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = if (isSelected || (isAnswered && isCorrectOption)) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp),
                lineHeight = 24.sp
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(icon, contentDescription = null, tint = iconTint)
            }
        }
    }
}

@Composable
fun ExplanationCard(isCorrect: Boolean, explanation: String, correctAnswer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(2.dp, if (isCorrect) CorrectBorderLight else WrongBorderLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isCorrect) CorrectBgLight else WrongBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) CorrectTextLight else WrongTextLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isCorrect) "Correct" else "Incorrect",
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) CorrectTextLight else WrongTextLight,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (!isCorrect) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Correct Answer:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = correctAnswer,
                    fontWeight = FontWeight.Bold,
                    color = CorrectTextLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Explanation",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )
        }
    }
}
