package com.katalis.app.presentation.components.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katalis.app.presentation.theme.KatalisTheme

@Composable
fun ModelStatusIndicator(
    isReady: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Loading AI model",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }
            isReady -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "AI model ready",
                    tint = Color(0xFF4CAF50), // Success green
                    modifier = Modifier.size(20.dp)
                )
            }
            else -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelStatusIndicatorPreview() {
    KatalisTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ModelStatusIndicator(isReady = false, isLoading = true)
            ModelStatusIndicator(isReady = true, isLoading = false)
            ModelStatusIndicator(isReady = false, isLoading = false)
        }
    }
}