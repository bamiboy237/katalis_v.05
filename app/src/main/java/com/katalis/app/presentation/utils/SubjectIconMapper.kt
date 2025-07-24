package com.katalis.app.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object SubjectIconMapper {
    
    fun getIconForSubject(subjectName: String): ImageVector {
        return when (subjectName.lowercase()) {
            "physics" -> Icons.Outlined.Science
            "chemistry" -> Icons.Outlined.Biotech
            "mathematics", "maths", "math", "pure mathematics", "pure maths", "pmm", "pure mathematics with mechanics" -> Icons.Outlined.Functions
            "biology" -> Icons.Outlined.Eco
            "history" -> Icons.AutoMirrored.Outlined.MenuBook
            "computer science", "computer-science", "computing" -> Icons.Outlined.Code
            "literature", "english" -> Icons.Outlined.AutoStories
            "geography" -> Icons.Outlined.Public
            "economics" -> Icons.AutoMirrored.Outlined.TrendingUp
            "art", "arts" -> Icons.Outlined.Brush
            "music" -> Icons.Outlined.MusicNote
            "psychology" -> Icons.Outlined.Psychology
            "philosophy" -> Icons.Outlined.Book
            else -> Icons.Outlined.School // Generic educational icon
        }
    }
    
    fun getFilledIconForSubject(subjectName: String): ImageVector {
        return when (subjectName.lowercase()) {
            "physics" -> Icons.Filled.Science
            "chemistry" -> Icons.Filled.Biotech
            "mathematics", "maths", "math", "pure mathematics", "pure maths", "pmm", "pure mathematics with mechanics" -> Icons.Filled.Functions
            "biology" -> Icons.Filled.Eco
            "history" -> Icons.AutoMirrored.Filled.MenuBook
            "computer science", "computer-science", "computing" -> Icons.Filled.Code
            "literature", "english" -> Icons.Filled.AutoStories
            "geography" -> Icons.Filled.Public
            "economics" -> Icons.AutoMirrored.Filled.TrendingUp
            "art", "arts" -> Icons.Filled.Brush
            "music" -> Icons.Filled.MusicNote
            "psychology" -> Icons.Filled.Psychology
            "philosophy" -> Icons.Filled.Book
            else -> Icons.Filled.School // Generic educational icon
        }
    }
    
    /**
     * Provides semantic content descriptions for accessibility compliance
     * Following Material 3 accessibility guidelines
     */
    fun getContentDescriptionForSubject(subjectName: String): String {
        return when (subjectName.lowercase()) {
            "physics" -> "Physics class"
            "chemistry" -> "Chemistry class"
            "mathematics", "maths", "math", "pure mathematics", "pure maths", "pmm", "pure mathematics with mechanics" -> "Mathematics class"
            "biology" -> "Biology class"
            "history" -> "History class"
            "computer science", "computer-science", "computing" -> "Computer Science class"
            "literature", "english" -> "English Literature class"
            "geography" -> "Geography class"
            "economics" -> "Economics class"
            "art", "arts" -> "Art class"
            "music" -> "Music class"
            "psychology" -> "Psychology class"
            "philosophy" -> "Philosophy class"
            else -> "$subjectName class"
        }
    }
}