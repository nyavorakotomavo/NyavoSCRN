package com.nyavo.nyavoscrn.features.screentest.domain

object ScreenTestEngine {

    const val DEFAULT_ROWS = 10
    const val DEFAULT_COLS = 6
    const val FALSE_POSITIVE_DURATION_MS = 5000L

    fun buildZones(rows: Int = DEFAULT_ROWS, cols: Int = DEFAULT_COLS): List<TestZone> {
        val zones = mutableListOf<TestZone>()
        var id = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                zones.add(TestZone(id = id, row = row, col = col))
                id++
            }
        }
        return zones
    }

    fun markZoneTested(zones: List<TestZone>, zoneId: Int, working: Boolean = true): List<TestZone> {
        return zones.map { zone ->
            if (zone.id == zoneId) zone.copy(isTested = true, isWorking = working) else zone
        }
    }

    fun markFalsePositive(zones: List<TestZone>, zoneId: Int): List<TestZone> {
        return zones.map { zone ->
            if (zone.id == zoneId) zone.copy(isFalsePositive = true) else zone
        }
    }

    fun computeProgress(zones: List<TestZone>): Float {
        if (zones.isEmpty()) return 0f
        val tested = zones.count { it.isTested }
        return tested.toFloat() / zones.size.toFloat()
    }

    fun findZoneAt(zones: List<TestZone>, row: Int, col: Int): TestZone? {
        return zones.firstOrNull { it.row == row && it.col == col }
    }
}