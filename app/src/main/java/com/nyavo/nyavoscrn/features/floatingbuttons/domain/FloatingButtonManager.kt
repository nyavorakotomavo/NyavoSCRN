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
        private const val PREFS_NAME = "floating_buttons_prefs"
        private const val KEY_BUTTONS = "buttons"
        private const val MAX_BUTTONS = 5

        @Volatile
        private var instance: FloatingButtonManager? = null

        fun getInstance(context: Context): FloatingButtonManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingButtonManager(
                    context.applicationContext
                ).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = Gson()

    private val _buttons = MutableStateFlow<List<FloatingButtonEntity>>(emptyList())
    val buttons: StateFlow<List<FloatingButtonEntity>> = _buttons.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var _workingZones: List<TestZone> = emptyList()

    init {
        loadButtons()
    }
    fun setWorkingZones(zones: List<TestZone>) {
        _workingZones = zones.filter { it.isWorking }
    }

    fun toggleEditMode() {
        _isEditMode.update { !it }
    }

    fun addButton(x: Float, y: Float): Boolean {
        if (_buttons.value.size >= MAX_BUTTONS) return false
        
        val newButton = FloatingButtonEntity(
            id = UUID.randomUUID().toString(),
            x = x,
            y = y,
            iconType = FloatingButtonEntity.IconType.PLUS
        )
        
        _buttons.update { it + newButton }
        saveButtons()
        return true
    }

    fun updateButtonPosition(id: String, x: Float, y: Float) {
        _buttons.update { list ->
            list.map { btn ->
                if (btn.id == id) btn.copy(x = x, y = y) else btn
            }
        }
        saveButtons()
    }

    fun updateButton(button: FloatingButtonEntity) {
        _buttons.update { list ->
            list.map { if (it.id == button.id) button else it }
        }
        saveButtons()
    }

    fun removeButton(id: String) {
        _buttons.update { it.filter { btn -> btn.id != id } }
        saveButtons()
    }

    fun findNearestWorkingZone(buttonPosition: Offset): Offset? {
        val workingZones = _workingZones
        if (workingZones.isEmpty()) return null

        var nearestZone: TestZone? = null        var minDistance = Float.MAX_VALUE

        for (zone in workingZones) {
            val centerX = (zone.col * 100f) + 50f
            val centerY = (zone.row * 100f) + 50f
            val distance = kotlin.math.hypot(
                centerX - buttonPosition.x,
                centerY - buttonPosition.y
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestZone = zone
            }
        }

        return nearestZone?.let {
            Offset((it.col * 100f) + 50f, (it.row * 100f) + 50f)
        }
    }

    private fun loadButtons() {
        val json = prefs.getString(KEY_BUTTONS, null) ?: return
        val type = object : TypeToken<List<FloatingButtonEntity>>() {}.type
        
        try {
            val loaded: List<FloatingButtonEntity> = gson.fromJson(json, type) ?: emptyList()
            _buttons.value = loaded.take(MAX_BUTTONS)
        } catch (e: Exception) {
            _buttons.value = emptyList()
        }
    }

    private fun saveButtons() {
        val json = gson.toJson(_buttons.value)
        prefs.edit().putString(KEY_BUTTONS, json).apply()
    }
}
