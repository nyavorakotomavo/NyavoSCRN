package com.nyavo.nyavoscrn.features.floatingbuttons.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class FloatingButtonEntity(
    val id: String,
    val x: Float,
    val y: Float,
    val iconType: IconType,
    val sizeDp: Float = 64f,
    val colorHex: Long = 0xFF6B4EFF,
    val actionType: ButtonAction = ButtonAction.REDIRECT_TAP
) {
    enum class IconType { PLUS, BACK, HOME, RECENT, CUSTOM }
    enum class ButtonAction { REDIRECT_TAP, GLOBAL_BACK, GLOBAL_HOME, GLOBAL_RECENTS }
}