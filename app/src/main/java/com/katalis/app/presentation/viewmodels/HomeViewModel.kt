package com.katalis.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katalis.app.domain.repository.KnowledgeRepository
import com.katalis.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectInfo(
    val name: String,
    val articleCount: Int
)

data class StudyProgressInfo(
    val title: String,
    val progress: Float,
    val progressText: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val subjects: List<SubjectInfo> = emptyList(),
    val recentProgress: StudyProgressInfo? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get available categories (subjects)
                val categories = knowledgeRepository.getAllCategories()
                val subjects = categories.map { category ->
                    val count = knowledgeRepository.getArticleCountByCategory(category)
                    SubjectInfo(
                        name = category,
                        articleCount = count
                    )
                }.filter { it.articleCount > 0 } // Only show subjects with content

                // Get recent progress (placeholder for now)
                val recentProgress = StudyProgressInfo(
                    title = "Recent Study",
                    progress = 0.65f,
                    progressText = "35% remaining"
                )

                _uiState.value = HomeUiState(
                    isLoading = false,
                    subjects = subjects,
                    recentProgress = recentProgress,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

}

// Placeholder data models for Subject Chapter functionality
data class Topic(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val description: String = ""
)

data class Chapter(
    val id: String,
    val title: String,
    val topics: List<Topic> = emptyList()
)

data class SubjectWithChapters(
    val id: String,
    val title: String,
    val chapters: List<Chapter> = emptyList()
)

// Mock data for development
object MockData {
    val algebraTopics = listOf(
        Topic("1", "Foundations", false, "Basic algebraic concepts"),
        Topic("2", "Solving Linear Equations & Inequalities", false, "Linear equations and inequalities"),
        Topic("3", "Functions & Graphing", false, "Functions and their graphs"),
        Topic("4", "Polynomials", true, "Polynomial functions and operations"),
        Topic("5", "Exponentials", false, "Exponential functions and logarithms")
    )
    
    val trigonometryTopics = listOf(
        Topic("6", "Unit Circle", false, "Understanding the unit circle"),
        Topic("7", "Trigonometric Functions", false, "Sine, cosine, and tangent"),
        Topic("8", "Identities", false, "Trigonometric identities")
    )
    
    val calculusTopics = listOf(
        Topic("9", "Limits", false, "Introduction to limits"),
        Topic("10", "Derivatives", false, "Differentiation"),
        Topic("11", "Integration", false, "Integration techniques")
    )
    
    val mechanicsTopics = listOf(
        Topic("12", "Newton's Laws", false, "Laws of motion"),
        Topic("13", "Energy", false, "Kinetic and potential energy"),
        Topic("14", "Momentum", false, "Conservation of momentum")
    )
    
    val pureMatematicsWithMechanics = SubjectWithChapters(
        id = "pmm",
        title = "Pure Mathematics with Mechanics",
        chapters = listOf(
            Chapter("algebra", "Algebra", algebraTopics),
            Chapter("trigonometry", "Trigonometry", trigonometryTopics),
            Chapter("calculus", "Calculus", calculusTopics),
            Chapter("mechanics", "Mechanics", mechanicsTopics)
        )
    )
    
    val biologyChapters = listOf(
        Chapter("cell-biology", "Cell Biology", emptyList()),
        Chapter("genetics", "Genetics", emptyList()),
        Chapter("evolution", "Evolution", emptyList())
    )
    
    val chemistryChapters = listOf(
        Chapter("atomic-structure", "Atomic Structure", emptyList()),
        Chapter("bonding", "Chemical Bonding", emptyList()),
        Chapter("reactions", "Chemical Reactions", emptyList())
    )
    
    val historyChapters = listOf(
        Chapter("ancient", "Ancient History", emptyList()),
        Chapter("modern", "Modern History", emptyList())
    )
    
    val computerScienceChapters = listOf(
        Chapter("programming", "Programming Fundamentals", emptyList()),
        Chapter("algorithms", "Algorithms", emptyList())
    )
    
    val physicsChapters = listOf(
        Chapter("mechanics", "Classical Mechanics", emptyList()),
        Chapter("thermodynamics", "Thermodynamics", emptyList())
    )
    
    val allSubjects = listOf(
        pureMatematicsWithMechanics,
        SubjectWithChapters("biology", "Biology", biologyChapters),
        SubjectWithChapters("chemistry", "Chemistry", chemistryChapters),
        SubjectWithChapters("history", "History", historyChapters),
        SubjectWithChapters("computer-science", "Computer Science", computerScienceChapters),
        SubjectWithChapters("physics", "Physics", physicsChapters)
    )
    
    fun getSubjectById(id: String): SubjectWithChapters? {
        return allSubjects.find { it.id == id }
    }
    
    fun getChapterById(subjectId: String, chapterId: String): Chapter? {
        return getSubjectById(subjectId)?.chapters?.find { it.id == chapterId }
    }
}