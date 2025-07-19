
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.katalis.app.presentation.components.BottomNavigationBar
import com.katalis.app.presentation.components.ChapterDropdownOverlay
import com.katalis.app.presentation.components.SubjectCard
import com.katalis.app.presentation.viewmodels.MockData
import com.katalis.app.presentation.viewmodels.SubjectWithChapters
import com.katalis.app.ui.theme.KatalisTheme

@Composable
fun SyllabusScreen(
    navController: NavController,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToChapter: (String, String) -> Unit = { _, _ -> }
) {
    var selectedSubject by remember { mutableStateOf<SubjectWithChapters?>(null) }
    var showDropdown by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
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
            },
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MockData.allSubjects) { subject ->
                    SubjectCard(
                        subject = subject,
                        isExpanded = selectedSubject?.id == subject.id && showDropdown,
                        onClick = { 
                            selectedSubject = subject
                            showDropdown = true
                        }
                    )
                }
            }
        }

        // Dropdown Overlay
        if (showDropdown && selectedSubject != null) {
            ChapterDropdownOverlay(
                subject = selectedSubject!!,
                onDismiss = { 
                    showDropdown = false
                    selectedSubject = null
                },
                onChapterSelected = { chapterId ->
                    showDropdown = false
                    onNavigateToChapter(selectedSubject!!.id, chapterId)
                    selectedSubject = null
                }
            )
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