package com.nyavo.nyavoscrn.features.floatingbuttons.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * Service d'accessibilité responsable de la simulation de gestes (taps).
 * 
 * Il est nécessaire d'activer ce service dans les paramètres d'accessibilité
 * d'Android pour que l'application puisse cliquer à la place de l'utilisateur
 * sur une zone saine de l'écran.
 */
class TapAccessibilityService : AccessibilityService() {

    companion object {
        /**
         * Instance singleton pour accéder au service depuis l'extérieur.
         */
        var instance: TapAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Nous n'avons pas besoin de réagir aux événements d'accessibilité
        // Ce service est utilisé uniquement pour ses capacités de geste.
    }

    override fun onInterrupt() {
        // Rien à faire en cas d'interruption
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Simule un tap (clic) aux coordonnées X et Y spécifiées.
     *
     * @param x Coordonnée X du point de touché
     * @param y Coordonnée Y du point de touché     */
    fun performTap(x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    50 // Durée du tap : 50 millisecondes
                )
            )
            .build()

        dispatchGesture(
            gesture, 
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                }
            }, 
            Handler(Looper.getMainLooper())
        )
    }

    /**
     * Exécute une action globale (ex: retour, écran d'accueil).
     */
    override fun performGlobalAction(action: Int): Boolean {
        return super.performGlobalAction(action)
    }
fun performGlobalAction(action: Int): Boolean {
        return super.performGlobalAction(action)
    }
}

/**
 * Objet utilitaire pour simplifier l'appel aux méthodes du service
 * depuis n'importe où dans l'application (comme le FloatingButtonManager).
 */
object TapSimulator {
    
    /**
     * Simule un tap si le service est actif.
     */
    fun simulateTap(x: Float, y: Float) {
        TapAccessibilityService.instance?.performTap(x, y)    }

    /**
     * Simule le bouton "Retour" système.
     */
    fun goBack() {
        TapAccessibilityService.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_BACK
        )
    }

    /**
     * Simule le bouton "Accueil" système.
     */
    fun goHome() {
        TapAccessibilityService.instance?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_HOME
        )
    }
}