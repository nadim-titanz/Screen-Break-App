package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.R
import java.util.concurrent.TimeUnit

class FloatingOverlayManager(
    private val context: Context,
    private val onDismissCallback: (packageName: String, appName: String) -> Unit,
    private val onSnoozeCallback: () -> Unit,
    private val onIgnoreAppCallback: (packageName: String, appName: String) -> Unit
) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // --- Break Reminder Alert Overlay ---
    private var reminderOverlayView: View? = null
    private var reminderMessageText: TextView? = null
    var isShowing: Boolean = false
        private set

    private var currentPackage: String = ""
    private var currentAppName: String = ""

    // --- Center Screen Time Elapsed HUD (from digipaws feature) ---
    private var centerHUDView: View? = null
    private var centerHUDTimerText: TextView? = null
    private var centerHUDAppText: TextView? = null
    private var centerHUDReelsText: TextView? = null
    var isCenterHUDVisible: Boolean = false
        private set

    /**
     * Shows the full-screen interactive break alert when usage interval threshold is reached.
     */
    @SuppressLint("InflateParams")
    fun show(continuousMinutes: Int, appName: String, packageName: String) {
        if (!Settings.canDrawOverlays(context)) return
        if (isShowing) {
            updateReminderContent(continuousMinutes, appName)
            return
        }

        currentPackage = packageName
        currentAppName = appName

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val overlay = createBreakAlertView(continuousMinutes, appName, packageName)
        try {
            windowManager.addView(overlay, layoutParams)
            reminderOverlayView = overlay
            isShowing = true
        } catch (e: Exception) {
            isShowing = false
        }
    }

    fun dismiss() {
        if (!isShowing || reminderOverlayView == null) return
        try {
            windowManager.removeView(reminderOverlayView)
        } catch (e: Exception) {
            // ignore
        } finally {
            reminderOverlayView = null
            reminderMessageText = null
            isShowing = false
        }
    }

    private fun updateReminderContent(minutes: Int, appName: String) {
        val displayName = appName.ifEmpty { "Apps" }
        reminderMessageText?.text = "You've been continuously using $displayName for $minutes minutes."
    }

    private fun createBreakAlertView(minutes: Int, appName: String, packageName: String): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E6000000")) // 90% OLED black
            setPadding(dpToPx(24), dpToPx(32), dpToPx(24), dpToPx(32))
            isClickable = true
            isFocusable = true
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(28))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#121214"))
                setStroke(dpToPx(1), Color.parseColor("#2E2E35"))
                cornerRadius = dpToPx(24).toFloat()
            }
        }

        // Live Header Indicator
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dot = View(context).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#D71921")) // Nothing Red
                shape = GradientDrawable.OVAL
            }
            layoutParams = LinearLayout.LayoutParams(dpToPx(8), dpToPx(8)).apply {
                marginEnd = dpToPx(8)
            }
        }
        val headerText = TextView(context).apply {
            text = "NOTHING // SCREEN BREAK"
            setTextColor(Color.parseColor("#D71921"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            letterSpacing = 0.15f
        }
        headerLayout.addView(dot)
        headerLayout.addView(headerText)
        card.addView(headerLayout)

        // Title
        val titleText = TextView(context).apply {
            text = "TIME TO DETOX"
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(16), 0, dpToPx(8))
        }
        card.addView(titleText)

        // Body message
        val bodyText = TextView(context).apply {
            val displayName = appName.ifEmpty { "Apps" }
            text = "You've been continuously using $displayName for $minutes minutes."
            setTextColor(Color.parseColor("#B3B3B3"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(24))
        }
        reminderMessageText = bodyText
        card.addView(bodyText)

        // Action Button: Strict Detox Break
        val btnTakeBreak = Button(context).apply {
            text = "TAKE A BREAK (LOCK APP)"
            setTextColor(Color.BLACK)
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(30).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
            ).apply {
                bottomMargin = dpToPx(12)
            }
            setOnClickListener {
                dismiss()
                onDismissCallback(packageName, appName)
            }
        }
        card.addView(btnTakeBreak)

        val footnote = TextView(context).apply {
            text = "DETOX PROTOCOL: 30s lockout across applications"
            setTextColor(Color.parseColor("#666666"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            gravity = Gravity.CENTER
        }
        card.addView(footnote)

        root.addView(card)
        return root
    }

    // =========================================================================
    // FEATURE: Show time elapsed using an app on the centre of the screen (HUD)
    // Non-touchable, non-focusable floating minimal pill centered on screen
    // =========================================================================

    fun showCenterElapsedHUD(appName: String, elapsedSeconds: Long, reelsCount: Int = 0) {
        if (!Settings.canDrawOverlays(context)) return

        if (centerHUDView == null) {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val view = createCenterHUDLayout()
            try {
                windowManager.addView(view, layoutParams)
                centerHUDView = view
                isCenterHUDVisible = true
            } catch (e: Exception) {
                return
            }
        }

        updateCenterElapsedHUD(appName, elapsedSeconds, reelsCount)
    }

    fun updateCenterElapsedHUD(appName: String, elapsedSeconds: Long, reelsCount: Int = 0) {
        val formattedTime = formatElapsed(elapsedSeconds)
        centerHUDTimerText?.text = formattedTime

        if (appName.isNotEmpty()) {
            centerHUDAppText?.text = appName.uppercase()
            centerHUDAppText?.visibility = View.VISIBLE
        } else {
            centerHUDAppText?.visibility = View.GONE
        }

        if (reelsCount > 0) {
            centerHUDReelsText?.text = "⚡ $reelsCount REELS"
            centerHUDReelsText?.visibility = View.VISIBLE
        } else {
            centerHUDReelsText?.visibility = View.GONE
        }
    }

    fun hideCenterElapsedHUD() {
        if (!isCenterHUDVisible || centerHUDView == null) return
        try {
            windowManager.removeView(centerHUDView)
        } catch (e: Exception) {
            // ignore
        } finally {
            centerHUDView = null
            centerHUDTimerText = null
            centerHUDAppText = null
            centerHUDReelsText = null
            isCenterHUDVisible = false
        }
    }

    private fun createCenterHUDLayout(): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#D90D0D0F")) // Semi-transparent OLED dark glass
                setStroke(dpToPx(1), Color.parseColor("#383842"))
                cornerRadius = dpToPx(20).toFloat()
            }
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val dot = View(context).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#D71921")) // Nothing Red
                shape = GradientDrawable.OVAL
            }
            layoutParams = LinearLayout.LayoutParams(dpToPx(6), dpToPx(6)).apply {
                marginEnd = dpToPx(6)
            }
        }
        topRow.addView(dot)

        centerHUDAppText = TextView(context).apply {
            text = "APP"
            setTextColor(Color.parseColor("#888888"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            letterSpacing = 0.08f
        }
        topRow.addView(centerHUDAppText)
        container.addView(topRow)

        centerHUDTimerText = TextView(context).apply {
            text = "00:00:00"
            setTextColor(Color.WHITE)
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setPadding(0, dpToPx(2), 0, 0)
        }
        container.addView(centerHUDTimerText)

        centerHUDReelsText = TextView(context).apply {
            text = "⚡ 0 REELS"
            setTextColor(Color.parseColor("#D71921"))
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            visibility = View.GONE
            setPadding(0, dpToPx(2), 0, 0)
        }
        container.addView(centerHUDReelsText)

        return container
    }

    private fun formatElapsed(seconds: Long): String {
        val hrs = TimeUnit.SECONDS.toHours(seconds)
        val mins = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
