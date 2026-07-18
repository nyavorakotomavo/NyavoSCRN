package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkPurpleColorScheme = darkColorScheme(
    primary = Violet50,
    secondary = Violet300,
    tertiary = Violet400,
    background = Violet900,
    surface = Violet800
)

@Composable
fun NyavoSCRNTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkPurpleColorScheme,
        content = content
    )
}