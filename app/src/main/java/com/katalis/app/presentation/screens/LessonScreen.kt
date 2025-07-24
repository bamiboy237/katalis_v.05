package com.katalis.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katalis.app.presentation.components.PlaceholderImage
import com.katalis.app.presentation.navigation.Screen
import com.katalis.app.presentation.viewmodels.LessonViewModel
import com.katalis.app.presentation.viewmodels.LessonEvent
import com.katalis.app.presentation.viewmodels.LessonUiState
import com.katalis.app.presentation.viewmodels.LessonPage
import com.katalis.app.presentation.viewmodels.LessonSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    subjectId: String,
    topicId: String,
    navController: NavController,
    viewModel: LessonViewModel = hiltViewModel(),
    onBackClick: () -> Unit = { navController.popBackStack() },
    onStartQuiz: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load lesson when screen first loads
    LaunchedEffect(subjectId, topicId) {
        viewModel.onEvent(LessonEvent.LoadLesson(subjectId, topicId))
    }

    when (uiState) {
        is LessonUiState.Loading -> {
            LoadingContent(modifier = modifier.fillMaxSize())
        }
        is LessonUiState.Success -> {
            LessonContent(
                uiState = uiState as LessonUiState.Success,
                viewModel = viewModel,
                onBackClick = onBackClick,
                onStartQuiz = onStartQuiz,
                modifier = modifier
            )
        }
        is LessonUiState.Error -> {
            ErrorContent(
                message = (uiState as LessonUiState.Error).message,
                onRetry = { viewModel.onEvent(LessonEvent.Retry) },
                onBackClick = onBackClick,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading lesson",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            OutlinedButton(onClick = onBackClick) {
                Text("Go Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonContent(
    uiState: LessonUiState.Success,
    viewModel: LessonViewModel,
    onBackClick: () -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { uiState.lesson.pages.size })
    
    // Sync pager state with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(LessonEvent.NavigateToPage(pagerState.currentPage))
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with back button and title
        TopAppBar(
            title = {
                Text(
                    text = uiState.lesson.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
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

        // Progress indicator
        LinearProgressIndicator(
            progress = { uiState.readingProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Lesson content with horizontal paging
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            LessonPageContent(
                page = uiState.lesson.pages[pageIndex],
                pageNumber = pageIndex + 1,
                totalPages = uiState.lesson.pages.size,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Check Understanding button - only shows when lesson is complete
        if (uiState.isCompleted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(LessonEvent.MarkAsCompleted)
                        onStartQuiz()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // TODO: Use quiz/check icon
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Understanding",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonPageContent(
    page: LessonPage,
    pageNumber: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Opening paragraph with large initial letter
        if (page.hasInitialLetter) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = page.content.first().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                )
                Text(
                    text = page.content.drop(1),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Text(
                text = page.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section content
        page.sections.forEach { section ->
            Text(
                text = section.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = section.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Placeholder for dynamic content (images, diagrams, etc.)
            if (section.hasVisualContent) {
                PlaceholderImage(
                    title = "Illustration / Diagram Area",
                    description = "Visual representation of ${section.title.lowercase()}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Page indicator
        Text(
            text = "Page $pageNumber of $totalPages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}