package com.nyavo.nyavoscrn.features.screentest.ui

import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import com.nyavo.nyavoscrn.core.designsystem.theme.PixelPalette
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneActiveGlow
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneDeadColor
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneFalsePositiveColor
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneWorkingColor
import com.nyavo.nyavoscrn.core.designsystem.theme.darken
import com.nyavo.nyavoscrn.core.designsystem.theme.lighten
import com.nyavo.nyavoscrn.features.screentest.domain.TestZone
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PIXEL_SIZE_PX = 18

private data class TouchEffect(
    val id: Long,
    val center: Offset
)

@Composable
fun PixelGridCanvas(
    modifier: Modifier = Modifier,
    zones: List<TestZone>,
    activeZoneId: Int?,
    rows: Int,
    cols: Int,
    onZoneTouched: (Int, Offset) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var canvasWidthPx by remember { mutableStateOf(0) }
    var canvasHeightPx by remember { mutableStateOf(0) }

    val pixelBitmap: ImageBitmap? = remember(canvasWidthPx, canvasHeightPx) {
        buildPixelGridBitmap(canvasWidthPx, canvasHeightPx)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "zonePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val activeRipples = remember { mutableStateListOf<TouchEffect>() }
    val rippleProgress = remember { mutableStateMapOf<Long, Float>() }

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                canvasWidthPx = size.width
                canvasHeightPx = size.height
            }
            .pointerInput(zones, activeZoneId, rows, cols) {
                detectTapGestures { offset ->
                    val zone = findZoneAt(offset, zones, rows, cols, size.width.toFloat(), size.height.toFloat())
                    if (zone != null && zone.id == activeZoneId) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        val effectId = System.nanoTime()
                        val effect = TouchEffect(id = effectId, center = offset)
                        activeRipples.add(effect)
                        rippleProgress[effectId] = 0f

                        coroutineScope.launch {
                            animateRippleProgress(effectId, rippleProgress)
                            activeRipples.removeAll { it.id == effectId }
                            rippleProgress.remove(effectId)
                        }

                        onZoneTouched(zone.id, offset)
                    }
                }
            }
    ) {
        pixelBitmap?.let { bmp ->
            drawImage(bmp)
        }

        if (rows > 0 && cols > 0) {
            val zoneWidth = size.width / cols
            val zoneHeight = size.height / rows

            zones.forEach { zone ->
                val overlayColor: Color? = when {
                    zone.id == activeZoneId -> ZoneActiveGlow.copy(alpha = pulseAlpha * 0.7f)
                    zone.isFalsePositive -> ZoneFalsePositiveColor.copy(alpha = 0.65f)
                    zone.isTested && !zone.isWorking -> ZoneDeadColor.copy(alpha = 0.55f)
                    zone.isTested && zone.isWorking -> ZoneWorkingColor.copy(alpha = 0.6f)
                    else -> null
                }

                if (overlayColor != null) {
                    drawRect(
                        color = overlayColor,
                        topLeft = Offset(zone.col * zoneWidth, zone.row * zoneHeight),
                        size = Size(zoneWidth, zoneHeight)
                    )
                }
            }
        }

        activeRipples.forEach { effect ->
            val progress = rippleProgress[effect.id] ?: 0f
            drawTouchEffect(effect.center, progress)
        }
    }
}

private fun findZoneAt(
    offset: Offset,
    zones: List<TestZone>,
    rows: Int,
    cols: Int,
    width: Float,
    height: Float
): TestZone? {
    if (rows <= 0 || cols <= 0 || width <= 0f || height <= 0f) return null
    val zoneWidth = width / cols
    val zoneHeight = height / rows
    val col = (offset.x / zoneWidth).toInt().coerceIn(0, cols - 1)
    val row = (offset.y / zoneHeight).toInt().coerceIn(0, rows - 1)
    return zones.firstOrNull { it.row == row && it.col == col }
}

private suspend fun animateRippleProgress(id: Long, progressMap: SnapshotStateMap<Long, Float>) {
    val animatable = Animatable(0f)
    animatable.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    ) {
        progressMap[id] = value
    }
}

private fun DrawScope.drawTouchEffect(offset: Offset, progress: Float) {
    drawCircle(
        color = Color(0xFFB300E6).copy(alpha = (1f - progress) * 0.6f),
        radius = 90f * progress,
        center = offset
    )

    val particleCount = 8
    val baseSeed = (offset.x + offset.y).toInt()
    repeat(particleCount) { index ->
        val random = Random(baseSeed + index)
        val angle = random.nextFloat() * 360f
        val distance = (40f + random.nextFloat() * 40f) * progress
        val radians = Math.toRadians(angle.toDouble())
        val particleOffset = Offset(
            x = offset.x + (cos(radians) * distance).toFloat(),
            y = offset.y + (sin(radians) * distance).toFloat()
        )
        drawCircle(
            color = Color(0xFF9900CC).copy(alpha = (1f - progress) * 0.5f),
            radius = 6f * (1f - progress).coerceAtLeast(0.05f),
            center = particleOffset
        )
    }
}

private fun buildPixelGridBitmap(widthPx: Int, heightPx: Int): ImageBitmap? {
    if (widthPx <= 0 || heightPx <= 0) return null

    val pixelSizePx = PIXEL_SIZE_PX
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var colorIndex = 0
    var y = 0
    while (y < heightPx) {
        var x = 0
        while (x < widthPx) {
            val baseColor = PixelPalette[colorIndex % PixelPalette.size]
            colorIndex++

            val topColor = baseColor.lighten(0.3f)
            val leftColor = baseColor.lighten(0.2f)
            val rightColor = baseColor.darken(0.2f)
            val bottomColor = baseColor.darken(0.3f)

            val gradient = LinearGradient(
                x.toFloat(),
                y.toFloat(),
                (x + pixelSizePx).toFloat(),
                (y + pixelSizePx).toFloat(),
                intArrayOf(
                    topColor.toArgb(),
                    leftColor.toArgb(),
                    rightColor.toArgb(),
                    bottomColor.toArgb()
                ),
                floatArrayOf(0f, 0.33f, 0.66f, 1f),
                Shader.TileMode.CLAMP
            )

            paint.shader = gradient
            canvas.drawRect(
                x.toFloat(),
                y.toFloat(),
                (x + pixelSizePx).toFloat(),
                (y + pixelSizePx).toFloat(),
                paint
            )

            x += pixelSizePx
        }
        y += pixelSizePx
    }

    return bitmap.asImageBitmap()
}
