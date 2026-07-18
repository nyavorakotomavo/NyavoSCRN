package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestPhase

@Composable
fun TestOverlay(phase: ScreenTestPhase, currentZoneIndex: Int, totalZones: Int, modifier: Modifier = Modifier) {
    val message = when (phase) {
        ScreenTestPhase.Instructions -> "TOUCHEZ POUR COMMENCER"
        ScreenTestPhase.ActiveTest -> "ZONE ${currentZoneIndex + 1}/$totalZones"
        ScreenTestPhase.FalsePositiveDetection -> "NE TOUCHEZ RIEN"
        ScreenTestPhase.Completed -> "TEST TERMINE"
    }
    val subMessage = when (phase) {
        ScreenTestPhase.ActiveTest -> "TOUCHEZ MAINTENANT"
        ScreenTestPhase.FalsePositiveDetection -> "DETECTION EN COURS..."
        else -> ""
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = message, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "overlayText") { text ->
            Text(text = text, fontSize = 28.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFFCC00FF),
                style = MaterialTheme.typography.headlineMedium.copy(shadow = Shadow(color = Color.Black, blurRadius = 4f, offset = androidx.compose.ui.geometry.Offset(2f, 2f))),
                textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
            if (subMessage.isNotEmpty()) {
                val subColor = if (phase == ScreenTestPhase.FalsePositiveDetection) Color(0xFFFF9800) else Color.White.copy(alpha = 0.7f)
                Text(text = subMessage, fontSize = 16.sp, color = subColor, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
            }
        }
    }
}