package com.katalis.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katalis.app.presentation.viewmodels.QuizViewModel
import com.katalis.app.presentation.viewmodels.QuizEvent
import com.katalis.app.presentation.viewmodels.QuizUiState
import com.katalis.app.presentation.viewmodels.QuizQuestion
import com.katalis.app.presentation.viewmodels.QuizAttempt
import com.katalis.app.presentation.viewmodels.QuizContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    subjectId: String,
    topicId: String,
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel(),
    onBackClick: () -> Unit = { navController.popBackStack() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load quiz when screen first loads
    LaunchedEffect(subjectId, topicId) {
        viewModel.onEvent(QuizEvent.LoadQuiz(subjectId, topicId))
    }

    when (uiState) {
        is QuizUiState.Loading -> {
            LoadingContent(modifier = modifier.fillMaxSize())
        }
        is QuizUiState.QuizInProgress -> {
            QuizInProgressContent(
                uiState = uiState as QuizUiState.QuizInProgress,
                viewModel = viewModel,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is QuizUiState.QuizCompleted -> {
            QuizResultsContent(
                uiState = uiState as QuizUiState.QuizCompleted,
                viewModel = viewModel,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is QuizUiState.Error -> {
            ErrorContent(
                message = (uiState as QuizUiState.Error).message,
                onRetry = { viewModel.onEvent(QuizEvent.Retry) },
                onBackClick = onBackClick,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading quiz",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = onBackClick) {
                Text("Go Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizInProgressContent(
    uiState: QuizUiState.QuizInProgress,
    viewModel: QuizViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuestion = uiState.quiz.questions[uiState.currentQuestionIndex]
    val isLastQuestion = uiState.currentQuestionIndex == uiState.quiz.questions.size - 1
    val hasSelectedAnswer = uiState.selectedAnswers.containsKey(uiState.currentQuestionIndex)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with title and close button
        TopAppBar(
            title = {
                Text(
                    text = uiState.quiz.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close quiz"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // Question progress
            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.quiz.questions.size}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Progress indicator
            LinearProgressIndicator(
                progress = { (uiState.currentQuestionIndex + 1).toFloat() / uiState.quiz.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Question text
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // Answer options
            currentQuestion.options.forEachIndexed { optionIndex, option ->
                val isSelected = uiState.selectedAnswers[uiState.currentQuestionIndex] == optionIndex
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                viewModel.onEvent(QuizEvent.SelectAnswer(uiState.currentQuestionIndex, optionIndex))
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) 
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else 
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null, // Handle click on Card level
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button
            if (uiState.currentQuestionIndex > 0) {
                TextButton(
                    onClick = { viewModel.onEvent(QuizEvent.PreviousQuestion) }
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp)) // Placeholder for alignment
            }

            // Next/Submit button
            Button(
                onClick = {
                    if (isLastQuestion) {
                        viewModel.onEvent(QuizEvent.SubmitQuiz)
                    } else {
                        viewModel.onEvent(QuizEvent.NextQuestion)
                    }
                },
                enabled = hasSelectedAnswer
            ) {
                Text(
                    text = if (isLastQuestion) "Submit" else "Next",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun QuizResultsContent(
    uiState: QuizUiState.QuizCompleted,
    viewModel: QuizViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attempt = uiState.attempt
    val passed = attempt.passed
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Results",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        // Pass/Fail status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (passed) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (passed) "✓ Congratulations! You passed!" else "✗ Keep trying!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = "TO PASS ${uiState.quiz.passingScore}% or higher",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Text(
                    text = "GRADE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Text(
                    text = "${attempt.score}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }

        // Question breakdown
        Text(
            text = "Latest attempt grade",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Text(
            text = "${attempt.score}% (${attempt.correctAnswers}/${attempt.totalQuestions} correct)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onEvent(QuizEvent.RetryQuiz) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Try Again")
            }
            
            Button(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Lesson")
            }
        }
    }
}