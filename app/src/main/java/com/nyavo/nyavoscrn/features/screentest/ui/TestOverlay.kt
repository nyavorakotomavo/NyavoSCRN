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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestPhase

@Composable
fun TestOverlay(
    phase: ScreenTestPhase,    currentZoneIndex: Int,
    totalZones: Int,
    modifier: Modifier = Modifier
) {
    val message = when (phase) {
        ScreenTestPhase.Instructions -> "Touchez pour commencer"
        ScreenTestPhase.ActiveTest -> "Zone ${currentZoneIndex + 1}/$totalZones • Touchez maintenant"
        ScreenTestPhase.FalsePositiveDetection -> "Ne touchez rien • Détection en cours..."
        ScreenTestPhase.Completed -> "Test terminé"
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
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(color = Color.Black, blurRadius = 12f)
                ),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .semantics { contentDescription = text }
            )
        }
    }
}
