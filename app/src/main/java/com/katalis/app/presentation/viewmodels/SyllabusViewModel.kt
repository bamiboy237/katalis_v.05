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
data class SyllabusChapter(
    val id: String,
    val title: String,
    val topicCount: Int,
    val isCompleted: Boolean = false,
    val progress: Float = 0f // 0.0 to 1.0
)

@Immutable
data class SyllabusSubject(
    val id: String,
    val name: String,
    val description: String,
    val chapters: List<SyllabusChapter>,
    val totalTopics: Int,
    val completedTopics: Int,
    val overallProgress: Float = 0f // 0.0 to 1.0
)

sealed class SyllabusUiState {
    object Loading : SyllabusUiState()
    data class Success(
        val subjects: List<SyllabusSubject>,
        val selectedSubject: SyllabusSubject? = null,
        val showDropdown: Boolean = false
    ) : SyllabusUiState()

    data class Error(val message: String) : SyllabusUiState()
}

sealed class SyllabusEvent {
    object LoadSyllabus : SyllabusEvent()
    data class SelectSubject(val subject: SyllabusSubject) : SyllabusEvent()
    object ClearSelection : SyllabusEvent()
    object ShowDropdown : SyllabusEvent()
    object HideDropdown : SyllabusEvent()
    data class NavigateToChapter(val subjectId: String, val chapterId: String) : SyllabusEvent()
    object Retry : SyllabusEvent()
}

@HiltViewModel
class SyllabusViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyllabusUiState>(SyllabusUiState.Loading)
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        loadSyllabus()
    }

    fun onEvent(event: SyllabusEvent) {
        when (event) {
            SyllabusEvent.LoadSyllabus -> loadSyllabus()
            is SyllabusEvent.SelectSubject -> selectSubject(event.subject)
            SyllabusEvent.ClearSelection -> clearSelection()
            SyllabusEvent.ShowDropdown -> showDropdown()
            SyllabusEvent.HideDropdown -> hideDropdown()
            is SyllabusEvent.NavigateToChapter -> navigateToChapter(
                event.subjectId,
                event.chapterId
            )

            SyllabusEvent.Retry -> retry()
        }
    }

    private fun loadSyllabus() {
        viewModelScope.launch {
            _uiState.value = SyllabusUiState.Loading

            try {
                // TODO: Replace with actual database query when syllabus data is available
                // For now, using enhanced mock data structure
                val mockSubjects = createMockSyllabusData()

                _uiState.value = SyllabusUiState.Success(
                    subjects = mockSubjects,
                    selectedSubject = null,
                    showDropdown = false
                )
            } catch (e: Exception) {
                _uiState.value = SyllabusUiState.Error(
                    "Failed to load syllabus: ${e.message}"
                )
            }
        }
    }

    private fun selectSubject(subject: SyllabusSubject) {
        val currentState = _uiState.value
        if (currentState is SyllabusUiState.Success) {
            _uiState.value = currentState.copy(
                selectedSubject = subject,
                showDropdown = true
            )
        }
    }

    private fun clearSelection() {
        val currentState = _uiState.value
        if (currentState is SyllabusUiState.Success) {
            _uiState.value = currentState.copy(
                selectedSubject = null,
                showDropdown = false
            )
        }
    }

    private fun showDropdown() {
        val currentState = _uiState.value
        if (currentState is SyllabusUiState.Success && currentState.selectedSubject != null) {
            _uiState.value = currentState.copy(showDropdown = true)
        }
    }

    private fun hideDropdown() {
        val currentState = _uiState.value
        if (currentState is SyllabusUiState.Success) {
            _uiState.value = currentState.copy(showDropdown = false)
        }
    }

    private fun navigateToChapter(subjectId: String, chapterId: String) {
        // Hide dropdown and clear selection after navigation
        hideDropdown()
        clearSelection()

        // Navigation logic would be handled by the UI layer
        // This ViewModel just manages the state
    }

    private fun retry() {
        val currentState = _uiState.value
        if (currentState is SyllabusUiState.Error) {
            loadSyllabus()
        }
    }

    // Helper function to get subject by ID
    fun getSubjectById(subjectId: String): SyllabusSubject? {
        val currentState = _uiState.value
        return if (currentState is SyllabusUiState.Success) {
            currentState.subjects.find { it.id == subjectId }
        } else null
    }

    // Helper function to check if a subject is selected
    fun isSubjectSelected(subjectId: String): Boolean {
        val currentState = _uiState.value
        return if (currentState is SyllabusUiState.Success) {
            currentState.selectedSubject?.id == subjectId && currentState.showDropdown
        } else false
    }

    // Mock data generator - TODO: Replace with database integration
    private fun createMockSyllabusData(): List<SyllabusSubject> {
        return listOf(
            SyllabusSubject(
                id = "mathematics",
                name = "Mathematics",
                description = "Comprehensive mathematics curriculum covering algebra, geometry, and calculus",
                chapters = listOf(
                    SyllabusChapter(
                        id = "linear_algebra",
                        title = "Linear Algebra",
                        topicCount = 8,
                        isCompleted = false,
                        progress = 0.6f
                    ),
                    SyllabusChapter(
                        id = "quadratic_equations",
                        title = "Quadratic Equations",
                        topicCount = 6,
                        isCompleted = true,
                        progress = 1.0f
                    ),
                    SyllabusChapter(
                        id = "geometry",
                        title = "Geometry",
                        topicCount = 10,
                        isCompleted = false,
                        progress = 0.3f
                    ),
                    SyllabusChapter(
                        id = "calculus_basics",
                        title = "Introduction to Calculus",
                        topicCount = 12,
                        isCompleted = false,
                        progress = 0.0f
                    )
                ),
                totalTopics = 36,
                completedTopics = 21,
                overallProgress = 0.58f
            ),
            SyllabusSubject(
                id = "physics",
                name = "Physics",
                description = "Fundamental physics concepts from mechanics to modern physics",
                chapters = listOf(
                    SyllabusChapter(
                        id = "mechanics",
                        title = "Classical Mechanics",
                        topicCount = 9,
                        isCompleted = false,
                        progress = 0.7f
                    ),
                    SyllabusChapter(
                        id = "thermodynamics",
                        title = "Thermodynamics",
                        topicCount = 7,
                        isCompleted = false,
                        progress = 0.4f
                    ),
                    SyllabusChapter(
                        id = "electromagnetism",
                        title = "Electromagnetism",
                        topicCount = 11,
                        isCompleted = false,
                        progress = 0.2f
                    ),
                    SyllabusChapter(
                        id = "modern_physics",
                        title = "Modern Physics",
                        topicCount = 8,
                        isCompleted = false,
                        progress = 0.0f
                    )
                ),
                totalTopics = 35,
                completedTopics = 12,
                overallProgress = 0.34f
            ),
            SyllabusSubject(
                id = "chemistry",
                name = "Chemistry",
                description = "Chemical principles from basic atoms to organic compounds",
                chapters = listOf(
                    SyllabusChapter(
                        id = "atomic_structure",
                        title = "Atomic Structure",
                        topicCount = 6,
                        isCompleted = true,
                        progress = 1.0f
                    ),
                    SyllabusChapter(
                        id = "chemical_bonding",
                        title = "Chemical Bonding",
                        topicCount = 8,
                        isCompleted = false,
                        progress = 0.8f
                    ),
                    SyllabusChapter(
                        id = "organic_chemistry",
                        title = "Organic Chemistry",
                        topicCount = 12,
                        isCompleted = false,
                        progress = 0.3f
                    ),
                    SyllabusChapter(
                        id = "physical_chemistry",
                        title = "Physical Chemistry",
                        topicCount = 9,
                        isCompleted = false,
                        progress = 0.1f
                    )
                ),
                totalTopics = 35,
                completedTopics = 18,
                overallProgress = 0.51f
            ),
            SyllabusSubject(
                id = "biology",
                name = "Biology",
                description = "Life sciences from cellular biology to human anatomy",
                chapters = listOf(
                    SyllabusChapter(
                        id = "cell_biology",
                        title = "Cell Biology",
                        topicCount = 7,
                        isCompleted = true,
                        progress = 1.0f
                    ),
                    SyllabusChapter(
                        id = "genetics",
                        title = "Genetics",
                        topicCount = 9,
                        isCompleted = false,
                        progress = 0.6f
                    ),
                    SyllabusChapter(
                        id = "human_anatomy",
                        title = "Human Anatomy",
                        topicCount = 14,
                        isCompleted = false,
                        progress = 0.4f
                    ),
                    SyllabusChapter(
                        id = "ecology",
                        title = "Ecology",
                        topicCount = 8,
                        isCompleted = false,
                        progress = 0.2f
                    )
                ),
                totalTopics = 38,
                completedTopics = 20,
                overallProgress = 0.53f
            )
        )
    }
}