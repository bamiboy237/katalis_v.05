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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.katalis.app.presentation.viewmodels.ChatViewModel
import com.katalis.app.presentation.components.chat.CleanChatInput
import com.katalis.app.presentation.components.chat.CleanMessageBubble
import com.katalis.app.presentation.components.chat.TypingIndicator
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
        // Minimal header
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Katalis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (!state.isModelReady) {
                        Text(
                            text = "Getting ready...",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Error display (clean, minimal)
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

        // Clean messages area - no extra padding or decorations
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                CleanMessageBubble(
                    message = message.text,
                    isFromUser = message.isFromUser,
                    timestamp = message.timestamp,
                    onBookmark = {
                        // TODO: Implement bookmark functionality
                    }
                )
            }

            if (state.isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Clean input area
        CleanChatInput(
            onSendMessage = { text ->
                viewModel.onEvent(ChatViewModel.ChatEvent.SendTextMessage(text))
            },
            placeholder = "Ask me anything about your studies...",
            isEnabled = state.isModelReady && !state.isLoading
        )
    }
}

@Composable
fun ChatConversationScreenPreview() {
    KatalisTheme {
        Column {
            Text("Clean Educational Chat Interface")
        }
    }
}