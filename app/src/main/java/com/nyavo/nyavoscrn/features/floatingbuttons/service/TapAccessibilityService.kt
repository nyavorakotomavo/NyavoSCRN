package com.nyavo.nyavoscrn.features.floatingbuttons.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class TapAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TapAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Pas besoin d'écouter des événements spécifiques
    }

    override fun onInterrupt() {
        // Rien à faire
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        
        dispatchGesture(gesture, null, Handler(Looper.getMainLooper()))
    }
}

object TapSimulator {
    fun simulateTap(x: Float, y: Float) {
        TapAccessibilityService.instance?.performTap(x, y)
    }
    
    fun goBack() {
        TapAccessibilityService.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_BACK
        )
    }
    
    fun goHome() {
        TapAccessibilityService.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_HOME
        )
    }
}