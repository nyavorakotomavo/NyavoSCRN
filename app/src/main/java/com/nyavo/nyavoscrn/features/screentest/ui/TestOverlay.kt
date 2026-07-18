package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestPhase

@Composable
fun TestOverlay(
    modifier: Modifier = Modifier,
    phase: ScreenTestPhase,
    currentZoneIndex: Int,
    totalZones: Int
) {
    val message = when (phase) {
        is ScreenTestPhase.Instructions -> "Touchez pour commencer"
        is ScreenTestPhase.ActiveTest -> "Zone ${(currentZoneIndex + 1).coerceAtMost(totalZones)}/$totalZones • Touchez maintenant"
        is ScreenTestPhase.FalsePositiveDetection -> "Ne touchez rien • Détection en cours..."
        is ScreenTestPhase.Completed -> "Test terminé"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "overlayMessage"
        ) { currentMessage ->
            Text(
                text = currentMessage,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black,
                        blurRadius = 12f
                    )
                )
            )
        }
    }
}
