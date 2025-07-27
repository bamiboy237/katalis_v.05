package com.katalis.app.presentation.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katalis.app.domain.engine.Gemma3nEngine
import com.katalis.app.domain.service.EducationalRAGService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val ragService: EducationalRAGService,
    private val gemma3nEngine: Gemma3nEngine
) : ViewModel() {

    @Immutable
    data class ChatState(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false,
        val isModelReady: Boolean = false,
        val error: String? = null,
        val initializationProgress: String = "Preparing AI assistant..."
    )

    @Immutable
    data class ChatMessage(
        val id: String = UUID.randomUUID().toString(),
        val text: String,
        val isFromUser: Boolean,
        val image: Bitmap? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val sources: List<String> = emptyList()
    )

    sealed class ChatEvent {
        data class SendTextMessage(val text: String) : ChatEvent()
        data class SendMultimodalMessage(val text: String, val image: Bitmap) : ChatEvent()
        object InitializeAI : ChatEvent()
        object ClearChat : ChatEvent()
        object DismissError : ChatEvent()
    }

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        initializeAI()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.InitializeAI -> initializeAI()
            is ChatEvent.SendTextMessage -> sendTextMessage(event.text)
            is ChatEvent.SendMultimodalMessage -> sendMultimodalMessage(event.text, event.image)
            ChatEvent.ClearChat -> clearChat()
            ChatEvent.DismissError -> dismissError()
        }
    }

    private fun initializeAI() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    initializationProgress = "Loading Gemma 3n model...",
                    error = null
                )

                Log.d("ChatViewModel", "Starting AI initialization...")

                gemma3nEngine.initialize()
                    .onSuccess {
                        Log.d("ChatViewModel", "AI initialization successful")
                        _state.value = _state.value.copy(
                            isModelReady = true,
                            isLoading = false,
                            initializationProgress = "Ready!",
                            messages = _state.value.messages + ChatMessage(
                                text = "Hello! I'm Katalis, your AI tutor for Physics, Mathematics, and Medicine. How can I help you learn today?",
                                isFromUser = false
                            )
                        )
                    }
                    .onFailure { error ->
                        Log.e("ChatViewModel", "AI initialization failed", error)
                        val detailedError = when {
                            error.message?.contains("Model file not found") == true ->
                                "Model file missing. Please place 'gemma-3n-E2B-it-int4.task' (~3GB) in your Downloads folder and restart the app."

                            error.message?.contains("Model file too small") == true ->
                                "Model file appears corrupted. Please re-download the complete Gemma 3n model file."

                            error.message?.contains("MediaPipe") == true ->
                                "MediaPipe library error. This might be an API compatibility issue."

                            else ->
                                "AI initialization failed: ${error.message}"
                        }

                        _state.value = _state.value.copy(
                            error = detailedError,
                            isLoading = false,
                            initializationProgress = "Failed to load AI"
                        )
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception during AI initialization", e)
                _state.value = _state.value.copy(
                    error = "Unexpected error during AI setup: ${e.message}",
                    isLoading = false,
                    initializationProgress = "Setup failed"
                )
            }
        }
    }

    private fun sendTextMessage(text: String) {
        if (!_state.value.isModelReady || _state.value.isLoading) {
            Log.w("ChatViewModel", "Attempted to send message when AI not ready")
            return
        }

        // Add user message immediately
        val userMessage = ChatMessage(text = text, isFromUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Sending text message: ${text.take(50)}...")
                val conversationHistory = buildConversationHistory()

                when (val result =
                    ragService.generateEducationalResponse(text, null, conversationHistory)) {
                    is EducationalRAGService.RAGResult.Success -> {
                        Log.d("ChatViewModel", "Received successful response")
                        val aiMessage = ChatMessage(
                            text = result.response,
                            isFromUser = false,
                            sources = result.sources
                        )
                        _state.value = _state.value.copy(
                            messages = _state.value.messages + aiMessage,
                            isLoading = false
                        )
                    }

                    is EducationalRAGService.RAGResult.Error -> {
                        Log.e("ChatViewModel", "RAG service returned error: ${result.message}")
                        val errorMessage = when {
                            result.message.contains("Engine not initialized") ->
                                "AI engine not ready. Please wait for initialization to complete."

                            result.message.contains("timeout") ->
                                "This question required more processing time than available . Try asking a simpler or more specific question, or restart the app if this continues."

                            result.message.contains("Generation failed") ->
                                "AI processing failed. This might be due to model complexity or device limitations. Try a shorter question or restart the app."

                            else ->
                                "Unable to generate response: ${result.message}"
                        }

                        _state.value = _state.value.copy(
                            error = errorMessage,
                            isLoading = false
                        )
                    }

                    EducationalRAGService.RAGResult.Loading -> {
                        Log.d("ChatViewModel", "RAG service still loading")
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception during message processing", e)
                _state.value = _state.value.copy(
                    error = "Message processing failed: ${e.message}. Please try restarting the app.",
                    isLoading = false
                )
            }
        }
    }

    private fun sendMultimodalMessage(text: String, image: Bitmap) {
        if (!_state.value.isModelReady || _state.value.isLoading) {
            Log.w("ChatViewModel", "Attempted to send multimodal message when AI not ready")
            return
        }

        // Add user message with image
        val userMessage = ChatMessage(text = text, isFromUser = true, image = image)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Sending multimodal message: ${text.take(50)}...")
                val conversationHistory = buildConversationHistory()

                when (val result =
                    ragService.generateEducationalResponse(text, image, conversationHistory)) {
                    is EducationalRAGService.RAGResult.Success -> {
                        Log.d("ChatViewModel", "Received successful multimodal response")
                        val aiMessage = ChatMessage(
                            text = result.response,
                            isFromUser = false,
                            sources = result.sources
                        )
                        _state.value = _state.value.copy(
                            messages = _state.value.messages + aiMessage,
                            isLoading = false
                        )
                    }

                    is EducationalRAGService.RAGResult.Error -> {
                        Log.e(
                            "ChatViewModel",
                            "RAG service returned multimodal error: ${result.message}"
                        )
                        _state.value = _state.value.copy(
                            error = "Image analysis failed: ${result.message}",
                            isLoading = false
                        )
                    }

                    EducationalRAGService.RAGResult.Loading -> {
                        Log.d("ChatViewModel", "Multimodal RAG service still loading")
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception during multimodal message processing", e)
                _state.value = _state.value.copy(
                    error = "Image processing failed: ${e.message}. Please try text-only messages.",
                    isLoading = false
                )
            }
        }
    }

    private fun buildConversationHistory(): List<String> {
        return _state.value.messages.takeLast(10).map { message ->
            "${if (message.isFromUser) "Student" else "Katalis"}: ${message.text}"
        }
    }

    private fun clearChat() {
        _state.value = _state.value.copy(
            messages = listOf(
                ChatMessage(
                    text = "Chat cleared. What would you like to learn about?",
                    isFromUser = false
                )
            )
        )
    }

    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        gemma3nEngine.cleanup()
    }
}