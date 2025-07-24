package com.katalis.app.presentation.screens.chat


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.katalis.app.presentation.viewmodels.ChatViewModel
import com.katalis.app.presentation.components.chat.EnhancedChatInput
import com.katalis.app.presentation.components.chat.EnhancedMessageBubble
import com.katalis.app.presentation.components.chat.TypingIndicator
import com.katalis.app.presentation.components.chat.ModelStatusIndicator
import com.katalis.app.presentation.theme.KatalisTheme
import kotlinx.coroutines.launch

// Simplified data classes for chat messages
//data class ChatMessage(
//    val id: String,
//    val content: String,
//    val isFromUser: Boolean,
//    val timestamp: Long = System.currentTimeMillis()
//)


//data class ChatConversation(
//    val id: String,
//    val title: String,
//    val messages: List<ChatMessage> = emptyList(),
//    val isTyping: Boolean = false
//)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or dialog
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Enhanced header with AI status
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "AI Tutor - Katalis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (!state.isModelReady) {
                        Text(
                            text = state.initializationProgress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                ModelStatusIndicator(
                    isReady = state.isModelReady,
                    isLoading = state.isLoading
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Error display
        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.onEvent(ChatViewModel.ChatEvent.DismissError) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Messages area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                EnhancedMessageBubble(
                    message = message.text,
                    isFromUser = message.isFromUser,
                    image = message.image,
                    sources = message.sources,
                    timestamp = message.timestamp
                )
            }

            if (state.isLoading) {
                item {
                    TypingIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        message = "thinking..."
                    )
                }
            }
        }

        // Enhanced input with camera support
        EnhancedChatInput(
            onSendTextMessage = { text ->
                viewModel.onEvent(ChatViewModel.ChatEvent.SendTextMessage(text))
            },
            onSendMultimodalMessage = { text, image ->
                viewModel.onEvent(ChatViewModel.ChatEvent.SendMultimodalMessage(text, image))
            },
            placeholder = "Ask me anything about Physics, Math, or Medicine...",
            isEnabled = state.isModelReady && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatConversationScreenPreview() {
    KatalisTheme {
        // Preview with mock data
        Column {
            Text("Preview - Enhanced AI Chat Screen")
        }
    }
}