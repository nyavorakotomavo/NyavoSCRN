package com.nyavo.nyavoscrn.features.floatingbuttons.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nyavo.nyavoscrn.MainActivity
import com.nyavo.nyavoscrn.features.floatingbuttons.domain.FloatingButtonManager
import com.nyavo.nyavoscrn.features.floatingbuttons.ui.FloatingButtonEditor

/**
 * Service Android responsable de l'affichage des boutons flottants
 * par-dessus toutes les autres applications.
 * 
 * Il implémente LifecycleOwner et SavedStateRegistryOwner pour permettre
 * l'hébergement correct de Compose UI dans un contexte de Service.
 */
class FloatingButtonService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private const val CHANNEL_ID = "nyavo_floating_buttons_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "nyavo_floating_prefs"
        private const val KEY_IS_RUNNING = "is_service_running"

        /**
         * Démarre le service si la permission SYSTEM_ALERT_WINDOW est accordée.
         */
        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, FloatingButtonService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                // Gérer le cas où la permission n'est pas accordée (ex: lancer l'activité de demande)
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }

        /**
         * Arrête le service.
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingButtonService::class.java))
        }

        /**
         * Vérifie si le service est actuellement en cours d'exécution.
         */
        fun isRunning(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_RUNNING, false)
        }
    }

    // Gestion du cycle de vie pour Compose
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Marquer le service comme actif        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_RUNNING, true)
            .apply()

        // Démarrer en premier plan avec une notification
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Afficher l'overlay Compose
        showOverlay()
        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // START_STICKY pour que le service redémarre s'il est tué par le système
        return START_STICKY
    }

    /**
     * Crée et ajoute la vue Compose au WindowManager.
     */
    private fun showOverlay() {
        if (overlayView != null) return

        // Configuration des paramètres de la fenêtre overlay
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        // Création de la vue Compose
        val composeView = ComposeView(this).apply {
            // Lier le cycle de vie du service à la vue Compose
            setViewTreeLifecycleOwner(this@FloatingButtonService)
            setViewTreeSavedStateRegistryOwner(this@FloatingButtonService)
                        setContent {
                // Utiliser applicationContext pour le Singleton Manager
                val manager = remember { 
                    FloatingButtonManager.getInstance(applicationContext) 
                }
                
                FloatingButtonEditor(
                    manager = manager,
                    onDismiss = { 
                        // Optionnel : logique si on veut quitter le mode édition
                    }
                )
            }
        }

        windowManager.addView(composeView, layoutParams)
        overlayView = composeView
    }

    /**
     * Crée la notification persistante requise pour les services en premier plan.
     */
    private fun createNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        // Créer le canal de notification pour Android 8.0+
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Boutons flottants Nyavo",
            NotificationManager.IMPORTANCE_LOW // Importance basse pour ne pas déranger
        ).apply {
            description = "Service de boutons flottants actif"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)

        // Intent pour ouvrir l'application principale au clic sur la notification
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("NyavoSCRN")
            .setContentText("Boutons flottants actifs")
            .setSmallIcon(android.R.drawable.ic_menu_compass)            .setContentIntent(pendingIntent)
            .setOngoing(true) // Notification non swipable
            .build()
    }

    override fun onDestroy() {
        // Marquer le service comme inactif
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_RUNNING, false)
            .apply()

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // Retirer la vue overlay de manière sécurisée
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: IllegalArgumentException) {
                // La vue n'était pas attachée, ignorer
            }
        }
        overlayView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
