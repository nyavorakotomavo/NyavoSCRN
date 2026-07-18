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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launchimport kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// Data classes pour les effets visuels
private data class TouchRipple(
    val pos: Offset,
    val startTime: Long
)

private data class Particle(
    val origin: Offset,
    val startTime: Long,
    val angle: Float,
    val speed: Float
)

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

    // Système de ripple basé sur le temps (comme dans le code original)
    val touchRipples = remember { mutableStateListOf<TouchRipple>() }
    val particles = remember { mutableStateListOf<Particle>() }

    // Animation de pulse pour la zone active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Nettoyage automatique des effets expirés
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            val now = System.currentTimeMillis()
            touchRipples.removeAll { now - it.startTime > 600 }
            particles.removeAll { now - it.startTime > 500 }
        }
    }

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
                    
                    val zone = zones.firstOrNull { 
                        it.row == row && it.col == col 
                    } ?: return@detectTapGestures
                    
                    if (zone.id != activeZoneId) return@detectTapGestures

                    // Retour haptique
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    // Ajoute le ripple
                    val now = System.currentTimeMillis()
                    touchRipples.add(TouchRipple(offset, now))

                    // Ajoute les particules (8 particules comme dans l'original)
                    repeat(8) {
                        val angle = Random.nextFloat() * 360f
                        val speed = Random.nextFloat() * 80f + 20f                        particles.add(Particle(offset, now, angle, speed))
                    }

                    // Notifie le changement d'état
                    onZoneTap(zone, offset)
                }
            }
    ) {
        if (canvasWidth != size.width || canvasHeight != size.height) {
            canvasWidth = size.width
            canvasHeight = size.height
        }

        // Crée la grille de pixels
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

        // Dessine les zones avec leurs couleurs
        zones.forEach { zone ->
            val left = zone.col * zoneWidthPx
            val top = zone.row * zoneHeightPx
            val rect = Rect(left, top, left + zoneWidthPx, top + zoneHeightPx)

            val baseColor = when {
                zone.isFalsePositive -> Color(0xFF42A5F5) // Bleu moyen
                zone.isTested && !zone.isWorking -> Color(0xFFE53935) // Rouge
                zone.isTested && zone.isWorking -> Color(0xFF81D4FA) // Bleu clair
                else -> Color(0xFF9C27B0) // Violet par défaut
            }

            val isActive = zone.id == activeZoneId
            val drawColor = if (isActive) {
                baseColor.copy(alpha = pulseAlpha)
            } else {
                baseColor
            }

            drawRect(
                color = drawColor,
                topLeft = rect.topLeft,                size = rect.size
            )

            // Contour pulsant pour la zone active
            if (isActive) {
                val strokeW = 3f
                val pulseRect = rect.inflate((pulseScale - 1f) * 10f)
                drawRect(
                    color = Color(0xFFE1BEE7),
                    topLeft = pulseRect.topLeft,
                    size = pulseRect.size,
                    style = Stroke(width = strokeW)
                )
            }

            // Marqueur pour les zones mortes
            if (zone.isTested && !zone.isWorking) {
                val cx = rect.center.x
                val cy = rect.center.y
                val r = min(zoneWidthPx, zoneHeightPx) / 6f
                drawCircle(
                    color = Color(0xFF8B0000),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }

        // Dessine les effets de touch
        drawTouchEffects(touchRipples, particles)
    }
}

private fun DrawScope.drawTouchEffects(
    ripples: List<TouchRipple>,
    particles: List<Particle>
) {
    val now = System.currentTimeMillis()

    // Dessine les ripples
    ripples.forEach { ripple ->
        val elapsed = now - ripple.startTime
        val progress = elapsed / 600f
        if (progress >= 1f) return@forEach
        
        val radius = progress * 120f
        val alpha = 1f - progress
        
        drawCircle(
            color = Color(0xFFCE93D8).copy(alpha = alpha),            radius = radius,
            center = ripple.pos,
            style = Stroke(width = 4f)
        )
    }

    // Dessine les particules
    particles.forEach { p ->
        val elapsed = now - p.startTime
        val progress = elapsed / 500f
        if (progress >= 1f) return@forEach
        
        val distance = p.speed * progress
        val rad = Math.toRadians(p.angle.toDouble())
        val dx = (cos(rad) * distance).toFloat()
        val dy = (sin(rad) * distance).toFloat()
        val pos = Offset(p.origin.x + dx, p.origin.y + dy)
        val alpha = 1f - progress
        val size = (1f - progress) * 8f
        
        drawCircle(
            color = Color(0xFFFFE082).copy(alpha = alpha),
            radius = size,
            center = pos
        )
    }
}

private fun createPixelGridBitmap(
    widthPx: Int,
    heightPx: Int,
    pixelSizePx: Float
): ImageBitmap {
    val bitmap = Bitmap.createBitmap(
        widthPx, 
        heightPx, 
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint()

    val cols = (widthPx / pixelSizePx).toInt() + 1
    val rows = (heightPx / pixelSizePx).toInt() + 1

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val x = col * pixelSizePx
            val y = row * pixelSizePx
            
            val baseColor = if ((row + col) % 2 == 0) {                Color(0xFF530080)
            } else {
                Color(0xFF660099)
            }
            
            val lightColor = baseColor.lighten(0.3f)
            val darkColor = baseColor.darken(0.3f)

            paint.shader = LinearGradient(
                x, y, x + pixelSizePx, y + pixelSizePx,
                lightColor.toArgb(), 
                darkColor.toArgb(),
                Shader.TileMode.CLAMP
            )
            
            canvas.drawRect(
                x, y, 
                x + pixelSizePx, 
                y + pixelSizePx, 
                paint
            )
        }
    }
    return bitmap.asImageBitmap()
}