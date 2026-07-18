package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val Violet900 = Color(0xFF0A0014)
val Violet800 = Color(0xFF1A0033)
val Violet700 = Color(0xFF2D004D)
val Violet600 = Color(0xFF400066)
val Violet500 = Color(0xFF530080)
val Violet400 = Color(0xFF660099)
val Violet300 = Color(0xFF8000B3)
val Violet200 = Color(0xFF9900CC)
val Violet100 = Color(0xFFB300E6)
val Violet50 = Color(0xFFCC00FF)

val VioletPalette = listOf(
    Violet900, Violet800, Violet700, Violet600, Violet500,
    Violet400, Violet300, Violet200, Violet100, Violet50
)

val ZoneDeadColor = Color(0xFFE53935)
val ZoneFalsePositiveColor = Color(0xFFFF9800)
val ZoneActiveGlow = Violet50

fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}