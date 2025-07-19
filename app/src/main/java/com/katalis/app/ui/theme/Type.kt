package com.katalis.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = FontFamily.Serif),
    displayMedium = baseline.displayMedium.copy(fontFamily = FontFamily.Serif),
    displaySmall = baseline.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = FontFamily.Serif),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = FontFamily.Serif),
    titleLarge = baseline.titleLarge.copy(fontFamily = FontFamily.Serif),
    titleMedium = baseline.titleMedium.copy(fontFamily = FontFamily.Serif),
    titleSmall = baseline.titleSmall.copy(fontFamily = FontFamily.Serif),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = FontFamily.Default),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = FontFamily.Default),
    bodySmall = baseline.bodySmall.copy(fontFamily = FontFamily.Default),
    labelLarge = baseline.labelLarge.copy(fontFamily = FontFamily.Default),
    labelMedium = baseline.labelMedium.copy(fontFamily = FontFamily.Default),
    labelSmall = baseline.labelSmall.copy(fontFamily = FontFamily.Default),
)