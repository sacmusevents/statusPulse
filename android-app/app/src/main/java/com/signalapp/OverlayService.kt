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
    private lateinit var overlayView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
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
    private var dragButton: Button? = null

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
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Setup close button
        val closeButton: Button = overlayView.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            stopSelf()
        }

        layoutParams = WindowManager.LayoutParams(
            420,
            60,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.x = 20
        layoutParams.y = 20
        layoutParams.alpha = 0.7f

        windowManager.addView(overlayView, layoutParams)
        setupButtonListeners()
        setupTouchListener()
    }

    private fun setupTouchListener() {
        val handler = Handler(Looper.getMainLooper())
        val longPressRunnable = Runnable {
            isMoving = true
        }

        dragButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    handler.postDelayed(longPressRunnable, 500) // 500ms long press
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isMoving) {
                        val deltaX = event.rawX.toInt() - lastX
                        val deltaY = event.rawY.toInt() - lastY

                        layoutParams.x = initialX + deltaX
                        layoutParams.y = initialY + deltaY

                        windowManager.updateViewLayout(overlayView, layoutParams)
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

    private fun setupButtonListeners() {
        val redButton: Button = overlayView.findViewById(R.id.red_button)
        val yellowButton: Button = overlayView.findViewById(R.id.yellow_button)
        val greenButton: Button = overlayView.findViewById(R.id.green_button)
        dragButton = overlayView.findViewById(R.id.drag_button)

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

        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}
