package com.signalapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private val overlayViews = mutableListOf<View>()
    private val layoutParamsList = mutableListOf<WindowManager.LayoutParams>()
    private var currentSessionId: String? = null
    private var currentSessionTitle: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isMoving = false
    private var lastX = 0
    private var lastY = 0
    private var initialX = 0
    private var initialY = 0
    private var resetHandler: Handler? = null
    private var resetRunnable: Runnable? = null
    private val RESET_TIME = 10 * 1000L // 10 seconds

    companion object {
        private var instance: OverlayService? = null

        fun stopExistingOverlay() {
            instance?.stopSelf()
            instance = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Stop any existing overlay
        stopExistingOverlay()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentSessionId = intent?.getStringExtra("SESSION_ID")
        currentSessionTitle = intent?.getStringExtra("SESSION_TITLE")

        if (currentSessionId != null && currentSessionTitle != null) {
            setupOverlay()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create single overlay in top-left corner with 56dp height
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val closeButton: Button = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            stopSelf()
        }

        val params = WindowManager.LayoutParams(
            400,
            56,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 20
        params.alpha = 0.7f

        overlayViews.add(view)
        layoutParamsList.add(params)
        windowManager.addView(view, params)

        setupButtonListenersForView(view)
        setupTouchListenerForView(view, view.findViewById(R.id.drag_button), params)
    }

    private fun setupTouchListenerForView(view: View, dragBtn: Button, params: WindowManager.LayoutParams) {
        val handler = Handler(Looper.getMainLooper())
        val longPressRunnable = Runnable {
            isMoving = true
        }

        dragBtn.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    initialX = params.x
                    initialY = params.y
                    handler.postDelayed(longPressRunnable, 500) // 500ms long press
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isMoving) {
                        val deltaX = event.rawX.toInt() - lastX
                        val deltaY = event.rawY.toInt() - lastY

                        params.x = initialX + deltaX
                        params.y = initialY + deltaY

                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    isMoving = false
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtonListenersForView(view: View) {
        val redButton: Button = view.findViewById(R.id.red_button)
        val yellowButton: Button = view.findViewById(R.id.yellow_button)
        val greenButton: Button = view.findViewById(R.id.green_button)

        redButton.setOnClickListener { updateSignalColor("red") }
        yellowButton.setOnClickListener { updateSignalColor("yellow") }
        greenButton.setOnClickListener { updateSignalColor("green") }
    }

    private fun updateSignalColor(color: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val timestamp = formatter.format(now)

                SupabaseManager.updateSignal(currentSessionId!!, color, timestamp)

                // Cancel existing timer callback
                if (resetHandler != null && resetRunnable != null) {
                    resetHandler!!.removeCallbacks(resetRunnable!!)
                }

                // Set auto-reset timer for red/yellow (not for green)
                if (color != "green") {
                    // Create handler if it doesn't exist
                    if (resetHandler == null) {
                        resetHandler = Handler(Looper.getMainLooper())
                    }

                    // Create new runnable and post it
                    resetRunnable = Runnable {
                        android.util.Log.d("OverlayService", "Auto-resetting to green after 10 seconds")
                        updateSignalColor("green")
                    }
                    resetHandler!!.postDelayed(resetRunnable!!, RESET_TIME)
                } else {
                    // Clear runnable when setting to green
                    resetRunnable = null
                }
            } catch (e: Exception) {
                android.util.Log.e("OverlayService", "updateSignalColor error", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        // Cancel any pending reset
        if (resetHandler != null && resetRunnable != null) {
            resetHandler!!.removeCallbacks(resetRunnable!!)
            resetHandler = null
            resetRunnable = null
        }

        // Remove all overlay views
        overlayViews.forEach { view ->
            windowManager.removeView(view)
        }
        overlayViews.clear()
        layoutParamsList.clear()
    }
}
