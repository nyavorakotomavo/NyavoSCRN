package com.nyavo.nyavoscrn.features.floatingbuttons.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nyavo.nyavoscrn.features.floatingbuttons.data.FloatingButtonEntity
import kotlinx.coroutines.launch

@Composable
fun FloatingButton(
    button: FloatingButtonEntity,
    isEditMode: Boolean,
    screenWidth: Float,
    screenHeight: Float,
    onPositionChanged: (Float, Float) -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(button.x) }
    var offsetY by remember { mutableFloatStateOf(button.y) }
    val pulseAnim = remember { Animatable(1f) }
    val scale by pulseAnim.asState()
    val size = button.sizeDp.dp
    val color = Color(button.colorHex)
    val iconVector = when (button.iconType) {
        FloatingButtonEntity.IconType.PLUS -> Icons.Default.Add
        FloatingButtonEntity.IconType.BACK -> Icons.Default.ArrowBack
        FloatingButtonEntity.IconType.HOME -> Icons.Default.Home
        else -> Icons.Default.Add
    }
    LaunchedEffect(button.x, button.y) {
        offsetX = button.x
        offsetY = button.y
    }
    Box(
        modifier = modifier
            .offset(x = (offsetX - button.sizeDp / 2).dp, y = (offsetY - button.sizeDp / 2).dp)
            .size(size)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .background(color, CircleShape)
            .pointerInput(isEditMode) {
                if (isEditMode) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(button.sizeDp / 2, screenWidth - button.sizeDp / 2)
                        offsetY = (offsetY + dragAmount.y).coerceIn(button.sizeDp / 2, screenHeight - button.sizeDp / 2)
                    }
                } else {
                    detectTapGestures(
                        onTap = {
                            scope.launch {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                pulseAnim.animateTo(targetValue = 0.7f, animationSpec = tween(100))
                                pulseAnim.animateTo(targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                            onTap()
                        },
                        onlongPress = { onLongPress() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = "Floating button",
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}