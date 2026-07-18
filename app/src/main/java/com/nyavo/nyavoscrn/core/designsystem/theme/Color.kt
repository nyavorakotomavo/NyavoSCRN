package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val Violet900 = Color(0xFF0A0014)
val Violet800 = Color(0xFF1A0033)
val Violet700 = Color(0xFF2D004D)
val Violet600 = Color(0xFF400066)
val Violet500 = Color(0xFF530080) // Nuance 1 : écran qui marche (bleu-violet)
val Violet400 = Color(0xFF660099)
val Violet300 = Color(0xFF8000B3) // Nuance 2 : faux touchers
val Violet200 = Color(0xFF9900CC)
val Violet100 = Color(0xFFB300E6)
val Violet50 = Color(0xFFCC00FF)  // Zone active (violet lumineux)

val ZoneDeadColor = Color(0xFFE53935)
val ZoneFalsePositiveColor = Violet300
val ZoneWorkingColor = Violet500
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