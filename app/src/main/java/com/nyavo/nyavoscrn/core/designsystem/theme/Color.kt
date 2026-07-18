package com.nyavo.nyavoscrn.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Palette bleu-violet (dégradé complet)
val BlueViolet900 = Color(0xFF0A0014) // Fond très sombre
val BlueViolet800 = Color(0xFF1A0033)
val BlueViolet700 = Color(0xFF2D004D)
val BlueViolet600 = Color(0xFF400066)
val BlueViolet500 = Color(0xFF530080) // Nuance 1 : écran qui marche
val BlueViolet400 = Color(0xFF660099)
val BlueViolet300 = Color(0xFF8000B3) // Nuance 2 : faux touchers
val BlueViolet200 = Color(0xFF9900CC)
val BlueViolet100 = Color(0xFFB300E6)
val BlueViolet50 = Color(0xFFCC00FF) // Zone active (violet)

val PixelPalette = listOf(
    BlueViolet900, BlueViolet800, BlueViolet700, BlueViolet600, BlueViolet500,
    BlueViolet400, BlueViolet300, BlueViolet200, BlueViolet100, BlueViolet50
)

val ZoneWorkingColor = BlueViolet500 // Nuance 1 : écran fonctionnel
val ZoneFalsePositiveColor = BlueViolet300 // Nuance 2 : faux touchers
val ZoneActiveGlow = BlueViolet50 // Violet lumineux : zone active
val ZoneDeadColor = Color(0xFFE53935) // Rouge : zones mortes

// Fonctions utilitaires
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
