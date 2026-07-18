package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkPurpleColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    background = Color(0xFF0A0014),
    surface = Color(0xFF120024)
)

@Composable
fun NyavoSCRNTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkPurpleColorScheme, content = content)
}
