package com.katalis.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katalis.app.presentation.viewmodels.SubjectWithChapters
import com.katalis.app.presentation.viewmodels.Chapter
import com.katalis.app.presentation.theme.KatalisTheme

@Composable
fun SubjectCard(
    subject: SubjectWithChapters,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Title
            Text(
                text = subject.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isExpanded)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Arrow indicator
            Icon(
                imageVector = if (isExpanded)
                    Icons.Default.KeyboardArrowDown
                else
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (isExpanded)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubjectCardPreview() {
    KatalisTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubjectCard(
                subject = SubjectWithChapters(
                    id = "pmm",
                    title = "Pure Mathematics with Mechanics",
                    chapters = emptyList()
                ),
                isExpanded = false,
                onClick = {}
            )

            SubjectCard(
                subject = SubjectWithChapters(
                    id = "biology",
                    title = "Biology",
                    chapters = emptyList()
                ),
                isExpanded = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubjectCardDarkPreview() {
    KatalisTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubjectCard(
                subject = SubjectWithChapters(
                    id = "chemistry",
                    title = "Chemistry",
                    chapters = emptyList()
                ),
                isExpanded = false,
                onClick = {}
            )

            SubjectCard(
                subject = SubjectWithChapters(
                    id = "physics",
                    title = "Physics",
                    chapters = emptyList()
                ),
                isExpanded = true,
                onClick = {}
            )
        }
    }
}