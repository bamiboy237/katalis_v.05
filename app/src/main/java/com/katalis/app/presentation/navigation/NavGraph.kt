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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Physics : Screen("physics")
    object PMM : Screen("pmm")
    object Chemistry : Screen("chemistry")
    object Syllabus : Screen("syllabus")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object SubjectChapter : Screen("subject_chapter/{subjectId}/{chapterId}") {
        fun createRoute(subjectId: String, chapterId: String) =
            "subject_chapter/$subjectId/$chapterId"
    }
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
                    // Placeholder for lesson navigation
                    // Will be implemented when lesson screen is ready
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Chat.route) {
            PlaceholderScreen(title = "Chat")
        }
        
        composable(Screen.Profile.route) {
            PlaceholderScreen(title = "Profile")
        }
        
        composable(Screen.Settings.route) {
            PlaceholderScreen(title = "Settings")
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