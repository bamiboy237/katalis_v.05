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
import androidx.compose.foundation.BorderStroke // adding the missing import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    subjectId: String,
    chapterId: String,
    topicId: String,
    lessonId: String,
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() },
    modifier: Modifier = Modifier
) {
    // Mock quiz data - will be replaced with database integration
    val quizTitle = "Solving Linear Equations and Inequalities" // TODO: Load from database
    val questions = getQuizQuestions() // Mock data
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var showResults by remember { mutableStateOf(false) }
    
    val currentQuestion = questions[currentQuestionIndex]
    val isLastQuestion = currentQuestionIndex == questions.size - 1
    val hasSelectedAnswer = selectedAnswers.containsKey(currentQuestionIndex)

    if (showResults) {
        QuizResultsScreen(
            questions = questions,
            selectedAnswers = selectedAnswers,
            onRetryQuiz = {
                // Reset quiz state
                currentQuestionIndex = 0
                selectedAnswers = mutableMapOf()
                showResults = false
            },
            onBackToLesson = onBackClick,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Header with title and close button
            TopAppBar(
                title = {
                    Text(
                        text = quizTitle,
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
                    text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Progress indicator
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
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
                    val isSelected = selectedAnswers[currentQuestionIndex] == optionIndex
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    selectedAnswers = selectedAnswers.toMutableMap().apply {
                                        put(currentQuestionIndex, optionIndex)
                                    }
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
                if (currentQuestionIndex > 0) {
                    TextButton(
                        onClick = { currentQuestionIndex-- }
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
                            // Submit quiz and show results
                            showResults = true
                        } else {
                            // Go to next question
                            currentQuestionIndex++
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
}

@Composable
private fun QuizResultsScreen(
    questions: List<QuizQuestion>,
    selectedAnswers: Map<Int, Int>,
    onRetryQuiz: () -> Unit,
    onBackToLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalQuestions = questions.size
    val correctAnswers = questions.indices.count { questionIndex ->
        selectedAnswers[questionIndex] == questions[questionIndex].correctAnswerIndex
    }
    val score = (correctAnswers * 100) / totalQuestions
    val passed = score >= 70 // 70% passing threshold
    
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
                    text = "TO PASS 70% or higher",
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
                    text = "$score%",
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
            text = "$score% ($correctAnswers/$totalQuestions correct)",
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
                onClick = onRetryQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Try Again")
            }
            
            Button(
                onClick = onBackToLesson,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Lesson")
            }
        }
    }
}

// Mock data structures - TODO: Replace with database models
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

// Mock quiz data - TODO: Replace with database queries
private fun getQuizQuestions(): List<QuizQuestion> {
    return listOf(
        QuizQuestion(
            question = "Solve for x in: 3x + 7 = 22",
            options = listOf("x = 5", "x = 9.6", "x = 3", "x = 5"),
            correctAnswerIndex = 0
        ),
        QuizQuestion(
            question = "For what value of y is 2y - 4 > 10?",
            options = listOf("y = 5", "y = 9.6", "y = 8", "y = 5"),
            correctAnswerIndex = 2
        ),
        QuizQuestion(
            question = "What is the first step in solving 4x - 12 = 8?",
            options = listOf("Divide by 4", "Add 12 to both sides", "Subtract 8", "Multiply by 4"),
            correctAnswerIndex = 1
        ),
        QuizQuestion(
            question = "Which equation represents a linear relationship?",
            options = listOf("y = x²", "y = 2x + 3", "y = x³ - 1", "y = 1/x"),
            correctAnswerIndex = 1
        ),
        QuizQuestion(
            question = "If 5x = 25, what is the value of x?",
            options = listOf("x = 4", "x = 5", "x = 6", "x = 7"),
            correctAnswerIndex = 1
        )
    )
}