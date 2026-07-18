package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NyavoSCRNColorScheme = darkColorScheme(
    primary = BlueViolet50,
    secondary = BlueViolet300,
    background = BlueViolet900,
    surface = BlueViolet800,
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

@Composable
fun NyavoSCRNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NyavoSCRNColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
