package com.nyavo.nyavoscrn.features.screentest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nyavo.nyavoscrn.core.designsystem.theme.Violet900
import com.nyavo.nyavoscrn.core.designsystem.theme.Violet400
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneDeadColor
import com.nyavo.nyavoscrn.core.designsystem.theme.ZoneFalsePositiveColor
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestEngine
import com.nyavo.nyavoscrn.features.screentest.domain.ScreenTestPhase
import com.nyavo.nyavoscrn.features.screentest.domain.TestZone
import kotlinx.coroutines.delay

@Composable
fun ScreenTestScreen(modifier: Modifier = Modifier) {
    val rows = ScreenTestEngine.DEFAULT_ROWS
    val cols = ScreenTestEngine.DEFAULT_COLS

    var zones by remember { mutableStateOf(ScreenTestEngine.buildZones(rows, cols)) }
    var phase by remember { mutableStateOf<ScreenTestPhase>(ScreenTestPhase.Instructions) }
    var currentZoneIndex by remember { mutableStateOf(0) }
    val progress = ScreenTestEngine.computeProgress(zones)

    val activeZoneId = if (phase == ScreenTestPhase.ActiveTest && currentZoneIndex < zones.size) {
        zones[currentZoneIndex].id
    } else null
    Box(modifier = modifier.fillMaxSize().background(Violet900)) {

        when (phase) {
            ScreenTestPhase.Instructions -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                phase = ScreenTestPhase.ActiveTest
                            }
                        }
                ) {
                    PixelGridCanvas(
                        zones = zones,
                        rows = rows,
                        cols = cols,
                        activeZoneId = null,
                        onZoneTap = { _, _ -> },
                        modifier = Modifier.fillMaxSize()
                    )
                    TestOverlay(phase = phase, currentZoneIndex = 0, totalZones = zones.size)
                }
            }

            ScreenTestPhase.ActiveTest -> {
                PixelGridCanvas(
                    zones = zones,
                    rows = rows,
                    cols = cols,
                    activeZoneId = activeZoneId,
                    onZoneTap = { zone, _ ->
                        zones = ScreenTestEngine.markZoneTested(zones, zone.id, working = true)
                        if (currentZoneIndex < zones.size - 1) {
                            currentZoneIndex++
                        } else {
                            phase = ScreenTestPhase.FalsePositiveDetection
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                TestOverlay(phase = phase, currentZoneIndex = currentZoneIndex, totalZones = zones.size)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                )            }

            ScreenTestPhase.FalsePositiveDetection -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val zoneWidthPx = size.width / cols
                                val zoneHeightPx = size.height / rows
                                val col = (offset.x / zoneWidthPx).toInt().coerceIn(0, cols - 1)
                                val row = (offset.y / zoneHeightPx).toInt().coerceIn(0, rows - 1)
                                val zone = zones.firstOrNull { it.row == row && it.col == col }
                                if (zone != null) {
                                    zones = ScreenTestEngine.markFalsePositive(zones, zone.id)
                                }
                            }
                        }
                ) {
                    PixelGridCanvas(
                        zones = zones,
                        rows = rows,
                        cols = cols,
                        activeZoneId = null,
                        onZoneTap = { _, _ -> },
                        modifier = Modifier.fillMaxSize()
                    )
                    TestOverlay(phase = phase, currentZoneIndex = currentZoneIndex, totalZones = zones.size)
                }

                LaunchedEffect(Unit) {
                    delay(ScreenTestEngine.FALSE_POSITIVE_DURATION_MS)
                    phase = ScreenTestPhase.Completed
                }
            }

            ScreenTestPhase.Completed -> {
                ResultsScreen(
                    zones = zones,
                    rows = rows,
                    cols = cols,
                    onRestart = {
                        zones = ScreenTestEngine.buildZones(rows, cols)
                        currentZoneIndex = 0
                        phase = ScreenTestPhase.Instructions
                    },
                    onSaveProfile = { }
                )
            }
        }    }
}

@Composable
private fun ResultsScreen(
    zones: List<TestZone>,
    rows: Int,
    cols: Int,
    onRestart: () -> Unit,
    onSaveProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Violet900)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Résultats",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            items(zones) { zone ->
                val color = when {
                    zone.isFalsePositive -> ZoneFalsePositiveColor
                    !zone.isWorking -> ZoneDeadColor
                    else -> Violet400
                }
                Box(
                    modifier = Modifier
                        .padding(1.dp)
                        .background(color)
                        .fillMaxWidth()
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                Text("Recommencer")            }
            Button(onClick = onSaveProfile, modifier = Modifier.fillMaxWidth()) {
                Text("Sauvegarder le profil")
            }
        }
    }
}