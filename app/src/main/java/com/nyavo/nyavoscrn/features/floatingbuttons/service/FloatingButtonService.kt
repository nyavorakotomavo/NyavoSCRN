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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nyavo.nyavoscrn.app.MainActivity
import com.nyavo.nyavoscrn.features.floatingbuttons.domain.FloatingButtonManager
import com.nyavo.nyavoscrn.features.floatingbuttons.ui.FloatingButtonEditor

class FloatingButtonService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    companion object {
        private const val CHANNEL_ID = "nyavo_fb"
        private const val NOTIF_ID = 1001
        fun start(ctx: Context) { if (Settings.canDrawOverlays(ctx)) ctx.startForegroundService(Intent(ctx, FloatingButtonService::class.java)) }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, FloatingButtonService::class.java)) }
    }
    private lateinit var wm: WindowManager
    private var overlay: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, createNotification())
        showOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }
    private fun showOverlay() {
        if (overlay != null) return
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = 0 }
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingButtonService)
            setViewTreeSavedStateRegistryOwner(this@FloatingButtonService)
            setContent {
                val manager = remember { FloatingButtonManager.getInstance(applicationContext) }
                FloatingButtonEditor(manager = manager, onDismiss = {})
            }
        }
        wm.addView(composeView, params)
        overlay = composeView
    }
    private fun createNotification(): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "Boutons flottants", NotificationManager.IMPORTANCE_LOW).apply { description = "Service actif" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL_ID).setContentTitle("NyavoSCRN").setContentText("Boutons activs").setSmallIcon(android.R.drawable.ic_menu_compass).setContentIntent(pi).setOngoing(true).build()
    }
    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        overlay?.let { wm.removeView(it) }
        overlay = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}