package com.katalis.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.katalis.app.presentation.screens.HomeScreen
import com.katalis.app.presentation.screens.SyllabusScreen
import com.katalis.app.presentation.screens.SubjectChapterScreen
import com.katalis.app.presentation.screens.LessonScreen
import com.katalis.app.presentation.screens.QuizScreen
import com.katalis.app.presentation.screens.chat.ChatWelcomeScreen
import com.katalis.app.presentation.screens.chat.ChatConversationScreen
import com.katalis.app.presentation.screens.DebugSettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Physics : Screen("physics")
    object PMM : Screen("pmm")
    object Chemistry : Screen("chemistry")
    object Syllabus : Screen("syllabus")
    object Chat : Screen("chat")
    object ChatConversation : Screen("chat_conversation/{conversationId}") {
        fun createRoute(conversationId: String) = "chat_conversation/$conversationId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object SubjectChapter : Screen("subject_chapter/{subjectId}/{chapterId}") {
        fun createRoute(subjectId: String, chapterId: String) =
            "subject_chapter/$subjectId/$chapterId"
    }
    object Lesson : Screen("lesson/{subjectId}/{topicId}") {
        fun createRoute(subjectId: String, topicId: String) =
            "lesson/$subjectId/$topicId"
    }

    object Quiz : Screen("quiz/{subjectId}/{topicId}") {
        fun createRoute(subjectId: String, topicId: String) =
            "quiz/$subjectId/$topicId"
    }
    object DebugSettings : Screen("debug_settings")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSubject = { subject ->
                    navController.navigate(subject)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onDebugClick = {
                    navController.navigate(Screen.DebugSettings.route)
                }
            )
        }
        
        // Placeholder screens for subject navigation
        composable(Screen.Physics.route) {
            PlaceholderScreen(title = "Physics")
        }
        
        composable(Screen.PMM.route) {
            PlaceholderScreen(title = "PMM")
        }
        
        composable(Screen.Chemistry.route) {
            PlaceholderScreen(title = "Chemistry")
        }
        
        composable(Screen.Syllabus.route) {
            SyllabusScreen(
                navController = navController,
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToChapter = { subjectId, chapterId ->
                    navController.navigate(Screen.SubjectChapter.createRoute(subjectId, chapterId))
                }
            )
        }

        composable(
            route = Screen.SubjectChapter.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""

            SubjectChapterScreen(
                subjectId = subjectId,
                chapterId = chapterId,
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToTopic = { topicId ->
                    navController.navigate(Screen.Lesson.createRoute(subjectId, topicId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""

            LessonScreen(
                subjectId = subjectId,
                topicId = topicId,
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                },
                onStartQuiz = {
                navController.navigate(
                        Screen.Quiz.createRoute(
                            subjectId,
                            topicId
                        )
                    )
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""

            QuizScreen(
                subjectId = subjectId,
                topicId = topicId,
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatWelcomeScreen(
                onSendMessage = { message ->
                    // Generate a conversation ID and navigate to conversation
                    val conversationId = "conv_${System.currentTimeMillis()}"
                    navController.navigate(Screen.ChatConversation.createRoute(conversationId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route = Screen.ChatConversation.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ChatConversationScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            PlaceholderScreen(title = "Profile")
        }
        
        composable(Screen.Settings.route) {
            PlaceholderScreen(title = "Settings")
        }

        composable(Screen.DebugSettings.route) {
            DebugSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title Screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}