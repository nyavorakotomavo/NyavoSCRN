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
import androidx.compose.ui.platform.LocalHapticFeedback
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

private data class TouchEffect(    val id: Long,
    val position: Offset,
    val ripple: Animatable<Float, *>,
    val particles: List<Particle>
)

private data class Particle(
    val angle: Float,
    val distance: Float
)

private fun buildParticles(count: Int = 8): List<Particle> {
    return List(count) {
        Particle(
            angle = Random.nextFloat() * 360f,
            distance = 40f + Random.nextFloat() * 40f
        )
    }
}

@Composable
fun PixelGridCanvas(
    zones: List<TestZone>,
    rows: Int,
    cols: Int,
    activeZoneId: Int?,
    onZoneTap: (TestZone, Offset) -> Unit,
    modifier: Modifier = Modifier,
    pixelSizePx: Float = 20f
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    val touchEffects = remember { mutableStateListOf<TouchEffect>() }
    val zoneScales = remember { mutableStateMapOf<Int, Animatable<Float, *>>() }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    var baseBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(zones, activeZoneId) {
                detectTapGestures { offset ->
                    if (canvasWidth <= 0f || canvasHeight <= 0f) return@detectTapGestures
                    val zoneWidthPx = canvasWidth / cols
                    val zoneHeightPx = canvasHeight / rows
                    val col = (offset.x / zoneWidthPx).toInt().coerceIn(0, cols - 1)
                    val row = (offset.y / zoneHeightPx).toInt().coerceIn(0, rows - 1)
                    val zone = zones.firstOrNull { it.row == row && it.col == col } ?: return@detectTapGestures
                    if (zone.id != activeZoneId) return@detectTapGestures

                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    val effect = TouchEffect(
                        id = System.nanoTime(),
                        position = offset,
                        ripple = Animatable(0f),
                        particles = buildParticles()
                    )
                    touchEffects.add(effect)
                    scope.launch {
                        effect.ripple.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
                        touchEffects.remove(effect)
                    }

                    val scaleAnim = zoneScales.getOrPut(zone.id) { Animatable(1f) }
                    scope.launch {
                        scaleAnim.animateTo(0.95f, tween(90))
                        scaleAnim.animateTo(1f, spring(stiffness = 200f))
                    }

                    onZoneTap(zone, offset)
                }
            }
    ) {
        if (canvasWidth != size.width || canvasHeight != size.height) {
            canvasWidth = size.width
            canvasHeight = size.height
        }

        if (baseBitmap == null || baseBitmap!!.width != size.width.toInt()) {
            baseBitmap = createPixelGridBitmap(
                widthPx = size.width.toInt().coerceAtLeast(1),
                heightPx = size.height.toInt().coerceAtLeast(1),
                pixelSizePx = pixelSizePx
            )
        }
        baseBitmap?.let { drawImage(it) }

        val zoneWidthPx = size.width / cols
        val zoneHeightPx = size.height / rows

        zones.forEach { zone ->
            val left = zone.col * zoneWidthPx
            val top = zone.row * zoneHeightPx

            val overlayColor: Color? = when {
                zone.id == activeZoneId -> ZoneActiveGlow.copy(alpha = pulseAlpha * 0.7f)
                zone.isFalsePositive -> ZoneFalsePositiveColor.copy(alpha = 0.65f)
                zone.isTested && !zone.isWorking -> ZoneDeadColor.copy(alpha = 0.55f)
                zone.isTested && zone.isWorking -> ZoneWorkingColor.copy(alpha = 0.6f)
                else -> null
            }

            if (overlayColor != null) {
                val scale = zoneScales[zone.id]?.value ?: 1f
                val cx = left + zoneWidthPx / 2f
                val cy = top + zoneHeightPx / 2f
                val w = zoneWidthPx * scale
                val h = zoneHeightPx * scale
                drawRect(
                    color = overlayColor,
                    topLeft = Offset(cx - w / 2f, cy - h / 2f),
                    size = Size(w, h)
                )
            }
        }

        touchEffects.forEach { effect ->
            drawTouchEffect(effect)
        }
    }
}

private fun DrawScope.drawTouchEffect(effect: TouchEffect) {
    val progress = effect.ripple.value
    val maxRadius = 90f
    drawCircle(
        color = Color(0xFFB300E6).copy(alpha = (1f - progress) * 0.6f),
        radius = maxRadius * progress,
        center = effect.position
    )
    effect.particles.forEach { particle ->
        val rad = Math.toRadians(particle.angle.toDouble())
        val dist = particle.distance * progress
        val x = effect.position.x + (cos(rad) * dist).toFloat()        val y = effect.position.y + (sin(rad) * dist).toFloat() - (progress * 60f)
        drawCircle(
            color = Color(0xFFCC00FF).copy(alpha = (1f - progress).coerceIn(0f, 1f)),
            radius = 4f * (1f - progress * 0.5f),
            center = Offset(x, y)
        )
    }
}

private fun createPixelGridBitmap(
    widthPx: Int,
    heightPx: Int,
    pixelSizePx: Float
): ImageBitmap {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint()

    val cols = (widthPx / pixelSizePx).toInt() + 1
    val rows = (heightPx / pixelSizePx).toInt() + 1

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val x = col * pixelSizePx
            val y = row * pixelSizePx
            
            // Palette simulée pour l'effet 3D (alternance de nuances)
            val baseColor = if ((row + col) % 2 == 0) Color(0xFF530080) else Color(0xFF660099)
            val lightColor = baseColor.lighten(0.3f)
            val darkColor = baseColor.darken(0.3f)

            paint.shader = LinearGradient(
                x, y, x + pixelSizePx, y + pixelSizePx,
                lightColor.toArgb(), darkColor.toArgb(),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(x, y, x + pixelSizePx, y + pixelSizePx, paint)
        }
    }
    return bitmap.asImageBitmap()
}