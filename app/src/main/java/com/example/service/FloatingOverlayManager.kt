package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
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
import com.example.ui.components.ReminderOverlayContent
import com.example.ui.theme.MyApplicationTheme

class FloatingOverlayManager(
    private val context: Context,
    private val onDismissCallback: (packageName: String, appName: String) -> Unit,
    private val onSnoozeCallback: () -> Unit = {},
    private val onIgnoreAppCallback: (packageName: String, appName: String) -> Unit = { _, _ -> }
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            savedStateRegistryController.performRestore(Bundle())
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun destroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    }

    private var lifecycleOwner: OverlayLifecycleOwner? = null

    @SuppressLint("InflateParams")
    fun show(continuousMinutes: Int, activeAppName: String, activePackage: String) {
        if (!CurrentAppDetector.hasOverlayPermission(context)) return
        dismiss()

        try {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val owner = OverlayLifecycleOwner()
            lifecycleOwner = owner

            val composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setContent {
                    MyApplicationTheme {
                        ReminderOverlayContent(
                            continuousMinutes = continuousMinutes,
                            activeAppName = activeAppName,
                            activePackage = activePackage,
                            onDismiss = {
                                dismiss()
                                onDismissCallback(activePackage, activeAppName)
                            },
                            onSnooze = {
                                dismiss()
                                onSnoozeCallback()
                            }
                        )
                    }
                }
            }

            overlayView = composeView
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View might not be attached
            }
        }
        overlayView = null
        lifecycleOwner?.destroy()
        lifecycleOwner = null
    }

    val isShowing: Boolean get() = overlayView != null
}
