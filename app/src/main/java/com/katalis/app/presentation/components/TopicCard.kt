package com.katalis.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katalis.app.presentation.viewmodels.Topic
import com.katalis.app.ui.theme.KatalisTheme

@Composable
fun TopicCard(
    topic: Topic,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(topic.id) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Topic Title
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right side indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Completion check mark
                    if (topic.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Arrow indicator
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicCardPreview() {
    KatalisTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopicCard(
                topic = Topic(
                    id = "1",
                    title = "Foundations",
                    isCompleted = false,
                    description = "Basic algebraic concepts"
                ),
                onClick = {}
            )

            TopicCard(
                topic = Topic(
                    id = "2",
                    title = "Solving Linear Equations & Inequalities",
                    isCompleted = true,
                    description = "Linear equations and inequalities"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicCardDarkPreview() {
    KatalisTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopicCard(
                topic = Topic(
                    id = "1",
                    title = "Functions & Graphing",
                    isCompleted = false,
                    description = "Functions and their graphs"
                ),
                onClick = {}
            )

            TopicCard(
                topic = Topic(
                    id = "2",
                    title = "Polynomials",
                    isCompleted = true,
                    description = "Polynomial functions and operations"
                ),
                onClick = {}
            )
        }
    }
}