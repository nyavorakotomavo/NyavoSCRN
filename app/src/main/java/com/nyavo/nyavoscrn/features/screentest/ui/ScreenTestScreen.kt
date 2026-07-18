package com.nyavo.nyavoscrn.features.screentest.domain

sealed class ScreenTestPhase {
    object Instructions : ScreenTestPhase()
    object ActiveTest : ScreenTestPhase()
    object FalsePositiveDetection : ScreenTestPhase()
    object Completed : ScreenTestPhase()
}

data class ScreenTestState(
    val phase: ScreenTestPhase = ScreenTestPhase.Instructions,
    val zones: List<TestZone> = emptyList(),
    val currentZoneIndex: Int = 0,
    val progress: Float = 0f
)
