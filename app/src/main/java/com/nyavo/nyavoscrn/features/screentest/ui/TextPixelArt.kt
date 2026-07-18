package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextPixelArt(
    text: String,
    color: Color = Color(0xFFCC00FF),
    fontSize: TextUnit = 32.sp,
    modifier: Modifier = Modifier
) {
    // Effet pixel art avec ombre portée et contour
    androidx.compose.material3.Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = color,
        modifier = modifier
    )
}

@Composable
fun TextPixelArtWithOutline(
    text: String,
    color: Color = Color(0xFFCC00FF),
    outlineColor: Color = Color.Black,
    fontSize: TextUnit = 32.sp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val paint = Paint().apply {
            isAntiAlias = false // Garde l'effet pixel net
        }
        
        // Dessine le contour (ombre portée multiple directions)
        paint.color = outlineColor
        for (dx in -2..2) {
            for (dy in -2..2) {
                if (dx != 0 || dy != 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        size.width / 2 + dx * 2,
                        size.height / 2 + dy * 2,
                        paint.asFrameworkPaint()
                    )
                }
            }
        }
        
        // Dessine le texte principal
        paint.color = color
        drawContext.canvas.nativeCanvas.drawText(
            text,
            size.width / 2,
            size.height / 2,
            paint.asFrameworkPaint()
        )
    }
}