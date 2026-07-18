package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestPhase

@Composable
fun TestOverlay(
    phase: ScreenTestPhase,
    currentZoneIndex: Int,
    totalZones: Int,
    modifier: Modifier = Modifier
) {
    val message = when (phase) {
        ScreenTestPhase.Instructions -> "TOUCHEZ POUR COMMENCER"
        ScreenTestPhase.ActiveTest -> "ZONE ${currentZoneIndex + 1}/$totalZones"
        ScreenTestPhase.FalsePositiveDetection -> "NE TOUCHEZ RIEN"
        ScreenTestPhase.Completed -> "TEST TERMINE"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = message,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "overlayText"
        ) { text ->
            // Texte principal avec effet pixel art
            TextPixelArtWithOutline(
                text = text,
                color = Color(0xFFCC00FF), // Violet lumineux
                outlineColor = Color.Black,
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentSize()
            )
            
            // Sous-titre pour la phase de test
            if (phase == ScreenTestPhase.ActiveTest) {
                androidx.compose.material3.Text(
                    text = "TOUCHEZ MAINTENANT",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                )
            } else if (phase == ScreenTestPhase.FalsePositiveDetection) {
                androidx.compose.material3.Text(
                    text = "DETECTION EN COURS...",
                    fontSize = 16.sp,
                    color = Color(0xFFFF9800).copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                )
            }
        }
    }
}