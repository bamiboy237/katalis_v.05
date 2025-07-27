package com.katalis.app.presentation.components.chat

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalis.app.presentation.theme.KatalisTheme
import kotlinx.coroutines.delay

@Composable
fun CleanMessageBubble(
    message: String,
    isFromUser: Boolean,
    modifier: Modifier = Modifier,
    image: Bitmap? = null,
    isBookmarked: Boolean = false,
    onBookmark: () -> Unit = {},
    timestamp: Long = System.currentTimeMillis()
) {
    val clipboardManager = LocalClipboardManager.current
    var showActions by remember { mutableStateOf(false) }

    val alignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isFromUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val textColor = if (isFromUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // Clean message bubble without clutter
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isFromUser) 20.dp else 6.dp,
                    bottomEnd = if (isFromUser) 6.dp else 20.dp
                ),
                modifier = Modifier
                    .clickable { showActions = !showActions }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Message text with educational typography
                    if (message.contains("```")) {
                        // Code block detection - simplified for now
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = textColor,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    } else {
                        // Regular educational text
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }

            // Educational actions (show on tap)
            if (showActions && !isFromUser) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Copy action
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message))
                            showActions = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy answer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Bookmark action
                    IconButton(
                        onClick = {
                            onBookmark()
                            showActions = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Minimal sender indicator (only when needed)
            if (!isFromUser) {
                Text(
                    text = "Katalis",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    message: String = "thinking..."
) {
    // Track elapsed time for progress feedback
    var elapsedSeconds by remember { mutableStateOf(0) }

    // Update elapsed time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Dynamic message based on elapsed time
    val progressMessage = when {
        elapsedSeconds < 5 -> "thinking..."
        elapsedSeconds < 15 -> "analyzing your question..."
        elapsedSeconds < 30 -> "generating detailed explanation..."
        elapsedSeconds < 45 -> "almost ready..."
        elapsedSeconds < 55 -> "finalizing response..."
        else -> "taking longer than usual..."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Animated typing dots
                    repeat(3) { index ->
                        var alpha by remember { mutableStateOf(0.3f) }

                        LaunchedEffect(Unit) {
                            delay(index * 200L)
                            while (true) {
                                alpha = 1f
                                delay(600)
                                alpha = 0.3f
                                delay(600)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(4.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = progressMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        // Show elapsed time after 10 seconds
                        if (elapsedSeconds >= 10) {
                            val timeDisplay = if (elapsedSeconds >= 30) {
                                "${elapsedSeconds}s / 60s limit"
                            } else {
                                "${elapsedSeconds}s elapsed"
                            }

                            Text(
                                text = timeDisplay,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (elapsedSeconds >= 50) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = "Katalis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun MessageBubblePreview() {
    KatalisTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            CleanMessageBubble(
                message = "Can you explain photosynthesis?",
                isFromUser = true
            )

            CleanMessageBubble(
                message = "Photosynthesis is the process by which plants convert light energy into chemical energy. It occurs in two main stages:\n\n**Light-dependent reactions** - Convert light energy to ATP and NADPH\n\n**Calvin cycle** - Uses ATP and NADPH to convert CO₂ into glucose",
                isFromUser = false
            )

            TypingIndicator(message = "thinking...")

            CleanMessageBubble(
                message = "What about cellular respiration?",
                isFromUser = true
            )
        }
    }
}