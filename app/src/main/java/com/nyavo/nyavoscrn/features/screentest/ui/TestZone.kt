package com.nyavo.nyavoscrn.features.screentest.domain

data class TestZone(
    val id: Int,
    val row: Int,
    val col: Int,
    val isTested: Boolean = false,
    val isWorking: Boolean = true,
    val isFalsePositive: Boolean = false
)