package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextPixelArt(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFCC00FF),
        style = MaterialTheme.typography.headlineMedium.copy(
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 4f,
                offset = androidx.compose.ui.geometry.Offset(2f, 2f)
            )
        ),
        modifier = modifier
    )
}