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
data class LessonPage(
    val id: String,
    val content: String,
    val hasInitialLetter: Boolean = false,
    val sections: List<LessonSection> = emptyList()
)

@Immutable
data class LessonSection(
    val title: String,
    val content: String,
    val hasVisualContent: Boolean = false
)

@Immutable
data class LessonContent(
    val id: String,
    val title: String,
    val subjectId: String,
    val topicId: String,
    val pages: List<LessonPage>,
    val estimatedDuration: Int = 0 // in minutes
)

sealed class LessonUiState {
    object Loading : LessonUiState()
    data class Success(
        val lesson: LessonContent,
        val currentPage: Int = 0,
        val readingProgress: Float = 0f,
        val isCompleted: Boolean = false
    ) : LessonUiState()
    data class Error(val message: String) : LessonUiState()
}

sealed class LessonEvent {
    data class LoadLesson(val subjectId: String, val topicId: String) : LessonEvent()
    data class NavigateToPage(val pageIndex: Int) : LessonEvent()
    object NextPage : LessonEvent()
    object PreviousPage : LessonEvent()
    object MarkAsCompleted : LessonEvent()
    object Retry : LessonEvent()
}

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun onEvent(event: LessonEvent) {
        when (event) {
            is LessonEvent.LoadLesson -> loadLesson(event.subjectId, event.topicId)
            is LessonEvent.NavigateToPage -> navigateToPage(event.pageIndex)
            LessonEvent.NextPage -> navigateNextPage()
            LessonEvent.PreviousPage -> navigatePreviousPage()
            LessonEvent.MarkAsCompleted -> markLessonCompleted()
            LessonEvent.Retry -> retry()
        }
    }

    private fun loadLesson(subjectId: String, topicId: String) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading

            try {
                // TODO: Replace with actual database query when lesson data is available
                // For now, using enhanced mock data structure
                val mockLesson = createMockLesson(subjectId, topicId)
                
                _uiState.value = LessonUiState.Success(
                    lesson = mockLesson,
                    currentPage = 0,
                    readingProgress = 0f,
                    isCompleted = false
                )
            } catch (e: Exception) {
                _uiState.value = LessonUiState.Error(
                    "Failed to load lesson: ${e.message}"
                )
            }
        }
    }

    private fun navigateToPage(pageIndex: Int) {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success) {
            val validPageIndex = pageIndex.coerceIn(0, currentState.lesson.pages.size - 1)
            val progress = (validPageIndex + 1).toFloat() / currentState.lesson.pages.size
            
            _uiState.value = currentState.copy(
                currentPage = validPageIndex,
                readingProgress = progress,
                isCompleted = progress >= 1.0f
            )
        }
    }

    private fun navigateNextPage() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success) {
            val nextPage = (currentState.currentPage + 1)
                .coerceAtMost(currentState.lesson.pages.size - 1)
            navigateToPage(nextPage)
        }
    }

    private fun navigatePreviousPage() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success) {
            val previousPage = (currentState.currentPage - 1).coerceAtLeast(0)
            navigateToPage(previousPage)
        }
    }

    private fun markLessonCompleted() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success) {
            viewModelScope.launch {
                try {
                    // TODO: Save completion status to database
                    // userProfileRepository.markLessonCompleted(currentState.lesson.id)
                    
                    _uiState.value = currentState.copy(isCompleted = true)
                } catch (e: Exception) {
                    // Handle error silently for now, but log it
                    // In production, might show a subtle error message
                }
            }
        }
    }

    private fun retry() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Error) {
            // Retry loading - would need to store last requested subject/topic
            _uiState.value = LessonUiState.Loading
        }
    }

    // Mock data generator - TODO: Replace with database integration
    private fun createMockLesson(subjectId: String, topicId: String): LessonContent {
        return LessonContent(
            id = "${subjectId}_${topicId}_lesson",
            title = when (topicId) {
                "linear_equations" -> "Solving Linear Equations"
                "quadratic_formulas" -> "Quadratic Formula Applications"
                "motion" -> "Laws of Motion"
                "forces" -> "Understanding Forces"
                else -> "Educational Content"
            },
            subjectId = subjectId,
            topicId = topicId,
            estimatedDuration = 15,
            pages = listOf(
                LessonPage(
                    id = "page_1",
                    content = "inear equations form the foundation of algebraic thinking and problem-solving. Understanding how to manipulate and solve these equations systematically will provide you with powerful tools for tackling more complex mathematical concepts.",
                    hasInitialLetter = true,
                    sections = listOf(
                        LessonSection(
                            title = "Understanding the Structure",
                            content = "A linear equation in one variable can be written in the form ax + b = c, where a, b, and c are constants and a ≠ 0. The goal is to isolate the variable x by performing inverse operations on both sides of the equation.",
                            hasVisualContent = true
                        ),
                        LessonSection(
                            title = "Step-by-Step Solution Process",
                            content = "The key principle in solving linear equations is maintaining balance. Whatever operation you perform on one side of the equation, you must perform the same operation on the other side.\n\nConsider the equation 3x + 7 = 22. To solve for x, we need to isolate it by undoing the operations in reverse order. First, subtract 7 from both sides, then divide both sides by 3."
                        )
                    )
                ),
                LessonPage(
                    id = "page_2",
                    content = "Building on the fundamentals, let's explore more complex scenarios and applications.",
                    sections = listOf(
                        LessonSection(
                            title = "Practice and Application",
                            content = "Mastery comes through practice with various types of linear equations. Start with simple one-step equations and gradually work toward more complex multi-step problems involving fractions and decimals.",
                            hasVisualContent = false
                        )
                    )
                ),
                LessonPage(
                    id = "page_3",
                    content = "Now that you understand the fundamentals, you're ready to tackle real-world applications.",
                    sections = listOf(
                        LessonSection(
                            title = "Real-World Applications",
                            content = "Linear equations appear everywhere in daily life - from calculating distances and speeds to determining costs and profits. These mathematical tools help us model and solve practical problems with precision.",
                            hasVisualContent = true
                        )
                    )
                )
            )
        )
    }
}