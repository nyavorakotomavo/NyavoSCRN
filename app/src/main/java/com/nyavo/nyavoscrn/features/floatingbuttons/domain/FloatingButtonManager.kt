package com.nyavo.nyavoscrn.features.floatingbuttons.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.geometry.Offset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nyavo.nyavoscrn.features.floatingbuttons.data.FloatingButtonEntity
import com.nyavo.nyavoscrn.features.screentest.domain.TestZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class FloatingButtonManager private constructor(context: Context) {
    companion object {
        private const val PREFS = "fb_prefs"
        private const val KEY = "buttons"
        private const val MAX = 5
        @Volatile private var instance: FloatingButtonManager? = null
        fun getInstance(ctx: Context): FloatingButtonManager {
            return instance ?: synchronized(this) { instance ?: FloatingButtonManager(ctx.applicationContext).also { instance = it } }
        }
    }
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _buttons = MutableStateFlow<List<FloatingButtonEntity>>(emptyList())
    val buttons: StateFlow<List<FloatingButtonEntity>> = _buttons.asStateFlow()
    private val _edit = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _edit.asStateFlow()
    private var _workingZones: List<TestZone> = emptyList()
    init { loadButtons() }
    
    fun setWorkingZones(zones: List<TestZone>) { _workingZones = zones.filter { it.isWorking } }
    fun toggleEditMode() { _edit.update { !it } }
    
    fun addButton(x: Float, y: Float): Boolean {
        if (_buttons.value.size >= MAX) return false
        val newBtn = FloatingButtonEntity(id = UUID.randomUUID().toString(), x = x, y = y, iconType = FloatingButtonEntity.IconType.PLUS)
        _buttons.update { it + newBtn }
        saveButtons()
        return true
    }
    fun updateButtonPosition(id: String, x: Float, y: Float) {
        _buttons.update { list -> list.map { btn -> if (btn.id == id) btn.copy(x = x, y = y) else btn } }
        saveButtons()
    }
    fun updateButton(btn: FloatingButtonEntity) {
        _buttons.update { list -> list.map { if (it.id == btn.id) btn else it } }
        saveButtons()
    }
    fun removeButton(id: String) {
        _buttons.update { it.filter { b -> b.id != id } }
        saveButtons()
    }
    fun findNearestWorkingZone(pos: Offset): Offset? {
        if (_workingZones.isEmpty()) return null
        var nearest: TestZone? = null
        var minDist = Float.MAX_VALUE
        for (zone in _workingZones) {
            val cx = (zone.col * 100f) + 50f
            val cy = (zone.row * 100f) + 50f
            val dist = kotlin.math.hypot(cx - pos.x, cy - pos.y)
            if (dist < minDist) { minDist = dist; nearest = zone }
        }
        return nearest?.let { Offset((it.col * 100f) + 50f, (it.row * 100f) + 50f) }
    }
    private fun loadButtons() {
        val json = prefs.getString(KEY, null) ?: return
        val type = object : TypeToken<List<FloatingButtonEntity>>() {}.type
        try {
            val loaded: List<FloatingButtonEntity> = gson.fromJson(json, type) ?: emptyList()
            _buttons.value = loaded.take(MAX)
        } catch (e: Exception) { _buttons.value = emptyList() }
    }
    private fun saveButtons() {
        prefs.edit().putString(KEY, gson.toJson(_buttons.value)).apply()
    }
}
