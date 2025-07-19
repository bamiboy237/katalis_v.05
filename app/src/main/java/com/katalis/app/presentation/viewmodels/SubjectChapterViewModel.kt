package com.katalis.app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectChapterUiState(
    val isLoading: Boolean = false,
    val chapterTitle: String = "",
    val topics: List<Topic> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SubjectChapterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectChapterUiState())
    val uiState: StateFlow<SubjectChapterUiState> = _uiState.asStateFlow()

    fun loadChapter(subjectId: String, chapterId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Simulate loading delay
                delay(500)

                val chapter = MockData.getChapterById(subjectId, chapterId)
                if (chapter != null) {
                    _uiState.value = SubjectChapterUiState(
                        isLoading = false,
                        chapterTitle = chapter.title,
                        topics = chapter.topics,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Chapter not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load chapter: ${e.message}"
                )
            }
        }
    }

    fun onTopicClick(topicId: String) {
        // Placeholder for topic navigation
        // Will be implemented when lesson screen is ready
        val topic = _uiState.value.topics.find { it.id == topicId }
        topic?.let {
            // Navigate to lesson screen
        }
    }

    fun markTopicCompleted(topicId: String) {
        val currentTopics = _uiState.value.topics
        val updatedTopics = currentTopics.map { topic ->
            if (topic.id == topicId) {
                topic.copy(isCompleted = true)
            } else {
                topic
            }
        }

        _uiState.value = _uiState.value.copy(topics = updatedTopics)
    }
}