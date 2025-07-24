package com.katalis.app.presentation.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katalis.app.domain.repository.KnowledgeRepository
import com.katalis.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String = ""
)

@Immutable
data class QuizContent(
    val id: String,
    val title: String,
    val subjectId: String,
    val topicId: String,
    val questions: List<QuizQuestion>,
    val passingScore: Int = 70, // Percentage required to pass
    val timeLimit: Int? = null // Time limit in minutes, null for no limit
)

@Immutable
data class QuizAttempt(
    val selectedAnswers: Map<Int, Int>,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)

sealed class QuizUiState {
    object Loading : QuizUiState()
    data class QuizInProgress(
        val quiz: QuizContent,
        val currentQuestionIndex: Int = 0,
        val selectedAnswers: Map<Int, Int> = emptyMap(),
        val timeRemaining: Int? = null // seconds remaining
    ) : QuizUiState()

    data class QuizCompleted(
        val quiz: QuizContent,
        val attempt: QuizAttempt,
        val selectedAnswers: Map<Int, Int>
    ) : QuizUiState()

    data class Error(val message: String) : QuizUiState()
}

sealed class QuizEvent {
    data class LoadQuiz(val subjectId: String, val topicId: String) : QuizEvent()
    data class SelectAnswer(val questionIndex: Int, val answerIndex: Int) : QuizEvent()
    data class NavigateToQuestion(val questionIndex: Int) : QuizEvent()
    object NextQuestion : QuizEvent()
    object PreviousQuestion : QuizEvent()
    object SubmitQuiz : QuizEvent()
    object RetryQuiz : QuizEvent()
    object Retry : QuizEvent()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun onEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.LoadQuiz -> loadQuiz(event.subjectId, event.topicId)
            is QuizEvent.SelectAnswer -> selectAnswer(event.questionIndex, event.answerIndex)
            is QuizEvent.NavigateToQuestion -> navigateToQuestion(event.questionIndex)
            QuizEvent.NextQuestion -> navigateNextQuestion()
            QuizEvent.PreviousQuestion -> navigatePreviousQuestion()
            QuizEvent.SubmitQuiz -> submitQuiz()
            QuizEvent.RetryQuiz -> retryQuiz()
            QuizEvent.Retry -> retry()
        }
    }

    private fun loadQuiz(subjectId: String, topicId: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading

            try {
                // TODO: Replace with actual database query when quiz data is available
                // For now, using enhanced mock data structure
                val mockQuiz = createMockQuiz(subjectId, topicId)

                _uiState.value = QuizUiState.QuizInProgress(
                    quiz = mockQuiz,
                    currentQuestionIndex = 0,
                    selectedAnswers = emptyMap(),
                    timeRemaining = mockQuiz.timeLimit?.times(60) // Convert minutes to seconds
                )
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(
                    "Failed to load quiz: ${e.message}"
                )
            }
        }
    }

    private fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizInProgress) {
            val updatedAnswers = currentState.selectedAnswers.toMutableMap()
            updatedAnswers[questionIndex] = answerIndex

            _uiState.value = currentState.copy(
                selectedAnswers = updatedAnswers
            )
        }
    }

    private fun navigateToQuestion(questionIndex: Int) {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizInProgress) {
            val validIndex = questionIndex.coerceIn(0, currentState.quiz.questions.size - 1)
            _uiState.value = currentState.copy(currentQuestionIndex = validIndex)
        }
    }

    private fun navigateNextQuestion() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizInProgress) {
            val nextIndex = (currentState.currentQuestionIndex + 1)
                .coerceAtMost(currentState.quiz.questions.size - 1)
            navigateToQuestion(nextIndex)
        }
    }

    private fun navigatePreviousQuestion() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizInProgress) {
            val previousIndex = (currentState.currentQuestionIndex - 1).coerceAtLeast(0)
            navigateToQuestion(previousIndex)
        }
    }

    private fun submitQuiz() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizInProgress) {
            viewModelScope.launch {
                try {
                    val quiz = currentState.quiz
                    val selectedAnswers = currentState.selectedAnswers

                    // Calculate score
                    val correctAnswers = quiz.questions.indices.count { questionIndex ->
                        selectedAnswers[questionIndex] == quiz.questions[questionIndex].correctAnswerIndex
                    }

                    val score = if (quiz.questions.isNotEmpty()) {
                        (correctAnswers * 100) / quiz.questions.size
                    } else 0

                    val passed = score >= quiz.passingScore

                    val attempt = QuizAttempt(
                        selectedAnswers = selectedAnswers,
                        score = score,
                        correctAnswers = correctAnswers,
                        totalQuestions = quiz.questions.size,
                        passed = passed
                    )

                    // TODO: Save attempt to database
                    // userProfileRepository.saveQuizAttempt(quiz.id, attempt)

                    _uiState.value = QuizUiState.QuizCompleted(
                        quiz = quiz,
                        attempt = attempt,
                        selectedAnswers = selectedAnswers
                    )

                } catch (e: Exception) {
                    _uiState.value = QuizUiState.Error(
                        "Failed to submit quiz: ${e.message}"
                    )
                }
            }
        }
    }

    private fun retryQuiz() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.QuizCompleted) {
            _uiState.value = QuizUiState.QuizInProgress(
                quiz = currentState.quiz,
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                timeRemaining = currentState.quiz.timeLimit?.times(60)
            )
        }
    }

    private fun retry() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Error) {
            // Retry loading - would need to store last requested subject/topic
            _uiState.value = QuizUiState.Loading
        }
    }

    // Helper function to get current question
    fun getCurrentQuestion(): QuizQuestion? {
        val currentState = _uiState.value
        return if (currentState is QuizUiState.QuizInProgress) {
            currentState.quiz.questions.getOrNull(currentState.currentQuestionIndex)
        } else null
    }

    // Helper function to check if answer is selected for current question
    fun hasSelectedCurrentAnswer(): Boolean {
        val currentState = _uiState.value
        return if (currentState is QuizUiState.QuizInProgress) {
            currentState.selectedAnswers.containsKey(currentState.currentQuestionIndex)
        } else false
    }

    // Helper function to check if it's the last question
    fun isLastQuestion(): Boolean {
        val currentState = _uiState.value
        return if (currentState is QuizUiState.QuizInProgress) {
            currentState.currentQuestionIndex == currentState.quiz.questions.size - 1
        } else false
    }

    // Mock data generator - TODO: Replace with database integration
    private fun createMockQuiz(subjectId: String, topicId: String): QuizContent {
        return QuizContent(
            id = "${subjectId}_${topicId}_quiz",
            title = when (topicId) {
                "linear_equations" -> "Solving Linear Equations and Inequalities"
                "quadratic_formulas" -> "Quadratic Formula Assessment"
                "motion" -> "Laws of Motion Quiz"
                "forces" -> "Understanding Forces Test"
                else -> "Educational Assessment"
            },
            subjectId = subjectId,
            topicId = topicId,
            passingScore = 70,
            timeLimit = null, // No time limit for now
            questions = listOf(
                QuizQuestion(
                    id = "q1",
                    question = "Solve for x in: 3x + 7 = 22",
                    options = listOf("x = 5", "x = 9.6", "x = 3", "x = 7"),
                    correctAnswerIndex = 0,
                    explanation = "First subtract 7 from both sides: 3x = 15, then divide by 3: x = 5"
                ),
                QuizQuestion(
                    id = "q2",
                    question = "For what value of y is 2y - 4 > 10?",
                    options = listOf("y > 5", "y > 9.6", "y > 7", "y > 3"),
                    correctAnswerIndex = 2,
                    explanation = "Add 4 to both sides: 2y > 14, then divide by 2: y > 7"
                ),
                QuizQuestion(
                    id = "q3",
                    question = "What is the first step in solving 4x - 12 = 8?",
                    options = listOf(
                        "Divide by 4",
                        "Add 12 to both sides",
                        "Subtract 8",
                        "Multiply by 4"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "To isolate the term with x, first add 12 to both sides to eliminate -12"
                ),
                QuizQuestion(
                    id = "q4",
                    question = "Which equation represents a linear relationship?",
                    options = listOf("y = x²", "y = 2x + 3", "y = x³ - 1", "y = 1/x"),
                    correctAnswerIndex = 1,
                    explanation = "Linear equations have variables raised to the first power only. y = 2x + 3 is linear."
                ),
                QuizQuestion(
                    id = "q5",
                    question = "If 5x = 25, what is the value of x?",
                    options = listOf("x = 4", "x = 5", "x = 6", "x = 7"),
                    correctAnswerIndex = 1,
                    explanation = "Divide both sides by 5: x = 25/5 = 5"
                )
            )
        )
    }
}