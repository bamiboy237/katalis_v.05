package com.katalis.app.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object SubjectIconMapper {
    
    fun getIconForSubject(subjectName: String): ImageVector {
        // For now, use simple mapping until we can identify available icons
        return when (subjectName.lowercase()) {
            "physics", "chemistry" -> Icons.Outlined.Settings
            "mathematics", "maths", "math", "pure mathematics", "pure maths", "pmm" -> Icons.Outlined.AccountCircle
            else -> Icons.Outlined.Settings // Generic subject icon
        }
    }
    
    fun getFilledIconForSubject(subjectName: String): ImageVector {
        return when (subjectName.lowercase()) {
            "physics", "chemistry" -> Icons.Filled.Settings
            "mathematics", "maths", "math", "pure mathematics", "pure maths", "pmm" -> Icons.Filled.AccountCircle
            else -> Icons.Filled.Settings // Generic subject icon
        }
    }
}