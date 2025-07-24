
@file:OptIn(ExperimentalMaterial3Api::class)

package com.katalis.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katalis.app.presentation.components.ChapterDropdownOverlay
import com.katalis.app.presentation.components.SubjectCard
import com.katalis.app.presentation.viewmodels.SyllabusViewModel
import com.katalis.app.presentation.viewmodels.SyllabusEvent
import com.katalis.app.presentation.viewmodels.SyllabusUiState
import com.katalis.app.presentation.viewmodels.SyllabusSubject
import com.katalis.app.presentation.theme.KatalisTheme

@Composable
fun SyllabusScreen(
    navController: NavController,
    viewModel: SyllabusViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToChapter: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is SyllabusUiState.Loading -> {
            LoadingContent()
        }
        is SyllabusUiState.Success -> {
            SyllabusContent(
                uiState = uiState as SyllabusUiState.Success,
                viewModel = viewModel,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick,
                onNavigateToChapter = onNavigateToChapter
            )
        }
        is SyllabusUiState.Error -> {
            ErrorContent(
                message = (uiState as SyllabusUiState.Error).message,
                onRetry = { viewModel.onEvent(SyllabusEvent.Retry) }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading syllabus...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun SyllabusContent(
    uiState: SyllabusUiState.Success,
    viewModel: SyllabusViewModel,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToChapter: (String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    )
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }

            // Subject List Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.subjects) { subject ->
                    SyllabusSubjectCard(
                        subject = subject,
                        isExpanded = uiState.selectedSubject?.id == subject.id && uiState.showDropdown,
                        onClick = { 
                            viewModel.onEvent(SyllabusEvent.SelectSubject(subject))
                        }
                    )
                }
            }
        }

        // Dropdown Overlay
        if (uiState.showDropdown && uiState.selectedSubject != null) {
            SyllabusChapterDropdownOverlay(
                subject = uiState.selectedSubject!!,
                onDismiss = { 
                    viewModel.onEvent(SyllabusEvent.HideDropdown)
                    viewModel.onEvent(SyllabusEvent.ClearSelection)
                },
                onChapterSelected = { chapterId ->
                    viewModel.onEvent(SyllabusEvent.NavigateToChapter(uiState.selectedSubject!!.id, chapterId))
                    onNavigateToChapter(uiState.selectedSubject!!.id, chapterId)
                }
            )
        }
    }
}

@Composable
private fun SyllabusSubjectCard(
    subject: SyllabusSubject,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subject.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${subject.completedTopics}/${subject.totalTopics} topics",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${(subject.overallProgress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { subject.overallProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SyllabusChapterDropdownOverlay(
    subject: SyllabusSubject,
    onDismiss: () -> Unit,
    onChapterSelected: (String) -> Unit
) {
    // Simplified dropdown overlay for chapters
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${subject.name} Chapters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                subject.chapters.forEach { chapter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onChapterSelected(chapter.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                if (chapter.isCompleted) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            if (!chapter.isCompleted) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { chapter.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
        
        // Background overlay to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Transparent background that can be clicked to dismiss
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyllabusScreenPreview() {
    KatalisTheme {
        val navController = rememberNavController()
        SyllabusScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun SyllabusScreenDarkPreview() {
    KatalisTheme(darkTheme = true) {
        val navController = rememberNavController()
        SyllabusScreen(navController = navController)
    }
}