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
data class SubjectInfo(
    val name: String,
    val articleCount: Int
)

@Immutable
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

// State/Event pattern implementation
@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val subjects: List<SubjectInfo> = emptyList(),
    val recentProgress: StudyProgressInfo? = null,
    val error: String? = null
)

sealed class HomeEvent {
    object Refresh : HomeEvent()
    object ClearError : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> loadHomeData()
            HomeEvent.ClearError -> _uiState.value = _uiState.value.copy(error = null)
        }
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

                _uiState.value = HomeState(
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

