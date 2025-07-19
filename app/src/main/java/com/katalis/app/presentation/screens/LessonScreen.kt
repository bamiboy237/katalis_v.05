package com.katalis.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.katalis.app.presentation.components.PlaceholderImage
import com.katalis.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    subjectId: String,
    chapterId: String,
    topicId: String,
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onStartQuiz: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Mock lesson data - will be replaced with database integration
    val lessonTitle = "Solving Linear Equations" // TODO: Load from database
    val lessonPages = getLessonPages() // Mock data
    
    val pagerState = rememberPagerState(pageCount = { lessonPages.size })
    var readingProgress by remember { mutableFloatStateOf(0f) }
    
    // Calculate reading progress based on current page
    LaunchedEffect(pagerState.currentPage) {
        readingProgress = (pagerState.currentPage + 1).toFloat() / lessonPages.size
    }
    
    val isLessonComplete = readingProgress >= 1.0f

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with back button and title
        TopAppBar(
            title = {
                Text(
                    text = lessonTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
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
            progress = { readingProgress },
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
                page = lessonPages[pageIndex],
                pageNumber = pageIndex + 1,
                totalPages = lessonPages.size,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Check Understanding button - only shows when lesson is complete
        if (isLessonComplete) {
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
                        onStartQuiz("lesson_1") // TODO: Use actual lesson ID
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // TODO: Use quiz/check icon
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

// Mock data structures - TODO: Replace with database models
data class LessonPage(
    val content: String,
    val hasInitialLetter: Boolean = false,
    val sections: List<LessonSection> = emptyList()
)

data class LessonSection(
    val title: String,
    val content: String,
    val hasVisualContent: Boolean = false
)

// Mock lesson data - TODO: Replace with database queries
private fun getLessonPages(): List<LessonPage> {
    return listOf(
        LessonPage(
            content = "inear equations form the foundation of algebraic thinking and problem-solving. Understanding how to manipulate and solve these equations systematically will provide you with powerful tools for tackling more complex mathematical concepts.",
            hasInitialLetter = true,
            sections = listOf(
                LessonSection(
                    title = "Understanding the Structure",
                    content = "A linear equation in one variable can be written in the form ax + b = c, where a, b, and c are constants and a ≠ 0. The goal is to isolate the variable x by performing inverse operations on both sides of the equation.",
                    hasVisualContent = true
                ),
                LessonSection(
                    title = "Step-by-Step Solution Process",
                    content = "The key principle in solving linear equations is maintaining balance. Whatever operation you perform on one side of the equation, you must perform the same operation on the other side.\n\nConsider the equation 3x + 7 = 22. To solve for x, we need to isolate it by undoing the operations in reverse order. First, subtract 7 from both sides, then divide both sides by 3."
                )
            )
        ),
        LessonPage(
            content = "Building on the fundamentals, let's explore more complex scenarios and applications.",
            sections = listOf(
                LessonSection(
                    title = "Practice and Application",
                    content = "Mastery comes through practice with various types of linear equations. Start with simple one-step equations and gradually work toward more complex multi-step problems involving fractions and decimals.",
                    hasVisualContent = false
                )
            )
        )
    )
}