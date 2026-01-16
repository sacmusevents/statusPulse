package com.signalapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import androidx.appcompat.view.ContextThemeWrapper
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
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

    private var healthCheckHandler: Handler? = null
    private var healthCheckRunnable: Runnable? = null
    private val HEALTH_CHECK_INTERVAL = 5 * 1000L // 5 seconds

    private var retryCount = 0
    private val MAX_RETRIES = 10 // ~30-60 seconds with exponential backoff
    private var onTemporaryDisconnectCallback: (() -> Unit)? = null
    private var onReconnectedCallback: (() -> Unit)? = null
    private var isReconnecting = false

    companion object {
        private var instance: OverlayService? = null
        private var onConnectionLostCallback: (() -> Unit)? = null
        private var onPermanentDisconnectCallback: ((sessionId: String?, sessionTitle: String?) -> Unit)? = null
        private var onSessionClosedCallback: (() -> Unit)? = null

        fun stopExistingOverlay() {
            instance?.stopSelf()
            instance = null
        }

        fun isSessionActive(): Boolean {
            return instance != null
        }

        fun updateSignalColorGlobally(color: String) {
            android.util.Log.d("OverlayService", "[SIGNAL_UPDATE] updateSignalColorGlobally called: color=$color, instance=$instance")
            instance?.updateSignalColor(color)
        }

        fun closeSessionGracefully() {
            instance?.updateSignalColor("green")
            Handler(Looper.getMainLooper()).postDelayed({
                stopExistingOverlay()
                onSessionClosed()
            }, 300)
        }

        fun setOnConnectionLostCallback(callback: (() -> Unit)?) {
            onConnectionLostCallback = callback
        }

        fun onConnectionLost() {
            onConnectionLostCallback?.invoke()
        }

        fun setOnTemporaryDisconnectCallback(callback: (() -> Unit)?) {
            instance?.let {
                it.onTemporaryDisconnectCallback = callback
            }
        }

        fun setOnReconnectedCallback(callback: (() -> Unit)?) {
            instance?.let {
                it.onReconnectedCallback = callback
            }
        }

        fun setOnPermanentDisconnectCallback(callback: ((String?, String?) -> Unit)?) {
            onPermanentDisconnectCallback = callback
        }

        fun onPermanentDisconnect(sessionId: String?, sessionTitle: String?) {
            onPermanentDisconnectCallback?.invoke(sessionId, sessionTitle)
        }

        fun setOnSessionClosedCallback(callback: (() -> Unit)?) {
            onSessionClosedCallback = callback
        }

        fun onSessionClosed() {
            onSessionClosedCallback?.invoke()
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
        val themedContext = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat)
        val view = LayoutInflater.from(themedContext).inflate(R.layout.overlay_layout, null)

        val closeButton: Button = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            closeSessionGracefully()
        }

        val params = WindowManager.LayoutParams(
            960,
            112,
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

        // Start health check to monitor connection
        startHealthCheck()
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

    fun updateSignalColor(color: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("OverlayService", "[SIGNAL_UPDATE] updateSignalColor: color=$color, sessionId=$currentSessionId")
                // Use UTC time, not local time
                val now = java.time.Instant.now()
                val formatter = DateTimeFormatter.ISO_INSTANT
                val timestamp = formatter.format(now)

                android.util.Log.d("OverlayService", "[SIGNAL_UPDATE] Calling SupabaseManager.updateSignal with timestamp=$timestamp")
                SupabaseManager.updateSignal(currentSessionId!!, color, timestamp, this@OverlayService)
                android.util.Log.d("OverlayService", "[SIGNAL_UPDATE] SupabaseManager.updateSignal completed successfully")

                // Connection restored - reset retry count
                resetRetryCount()

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
                android.util.Log.e("OverlayService", "[SIGNAL_UPDATE] updateSignalColor error: ${e.message}", e)
                // User action failed - just notify, don't increment retry count
                handleConnectionError(isAutoRetry = false)
            }
        }
    }

    private fun handleConnectionError(isAutoRetry: Boolean = false) {
        // Only user-triggered failures start reconnecting state
        if (!isAutoRetry && !isReconnecting) {
            isReconnecting = true
            retryCount = 0 // Start counter for automatic retries
            onTemporaryDisconnectCallback?.invoke()
        }

        // Don't retry user-triggered actions, let health check handle reconnection
        if (!isAutoRetry) {
            android.util.Log.d("OverlayService", "User action failed, waiting for health check to detect")
            return
        }

        // Only automatic retries increment the counter
        retryCount++

        if (retryCount < MAX_RETRIES) {
            // Calculate exponential backoff: 1s, 2s, 4s, 8s, etc.
            val delayMs = (1000L * Math.pow(2.0, (retryCount - 1).toDouble())).toLong()
            android.util.Log.d("OverlayService", "Auto-retry in ${delayMs}ms (attempt $retryCount/$MAX_RETRIES)")

            Handler(Looper.getMainLooper()).postDelayed({
                // Retry the health check
                performHealthCheck()
            }, delayMs)
        } else {
            // Max retries exceeded - permanent disconnect
            android.util.Log.e("OverlayService", "Max retries exceeded, permanent disconnect")
            onPermanentDisconnect(instance?.currentSessionId, instance?.currentSessionTitle)
        }
    }

    private fun resetRetryCount() {
        if (isReconnecting) {
            // Was reconnecting, now restored - notify UI
            onReconnectedCallback?.invoke()
        }
        isReconnecting = false
        retryCount = 0
    }

    private fun startHealthCheck() {
        if (healthCheckHandler == null) {
            healthCheckHandler = Handler(Looper.getMainLooper())
        }

        healthCheckRunnable = Runnable {
            performHealthCheck()
        }
        healthCheckHandler?.post(healthCheckRunnable!!)
    }

    private fun performHealthCheck() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                // Try to fetch the session to verify connection
                if (currentSessionId != null) {
                    SupabaseManager.getSessions(this@OverlayService)
                }
                // If successful, reset retry count and schedule next check
                resetRetryCount()
                healthCheckHandler?.postDelayed(healthCheckRunnable!!, HEALTH_CHECK_INTERVAL)
            } catch (e: Exception) {
                // Connection error - use exponential backoff retry
                android.util.Log.e("OverlayService", "Health check failed: ${e.message}")
                handleConnectionError(isAutoRetry = true)
            }
        }
    }

    private fun stopHealthCheck() {
        if (healthCheckHandler != null && healthCheckRunnable != null) {
            healthCheckHandler!!.removeCallbacks(healthCheckRunnable!!)
            healthCheckHandler = null
            healthCheckRunnable = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        stopHealthCheck()

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
