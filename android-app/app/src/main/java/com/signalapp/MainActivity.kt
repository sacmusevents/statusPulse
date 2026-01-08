package com.signalapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val createSessionBtn = findViewById<Button>(R.id.createSessionBtn)
        val joinSessionBtn = findViewById<Button>(R.id.joinSessionBtn)
        val checkPermBtn = findViewById<Button>(R.id.checkPermButton)

        checkPermBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE)
            }
        }

        createSessionBtn.setOnClickListener {
            startActivity(Intent(this, CreateSessionActivity::class.java))
        }

        joinSessionBtn.setOnClickListener {
            startActivity(Intent(this, SessionListActivity::class.java))
        }

        // Setup bottom control bar buttons
        setupControlButtons()

        updatePermissionStatus()
    }

    private fun setupControlButtons() {
        val controlRedBtn = findViewById<Button>(R.id.controlRedBtn)
        val controlYellowBtn = findViewById<Button>(R.id.controlYellowBtn)
        val controlGreenBtn = findViewById<Button>(R.id.controlGreenBtn)

        controlRedBtn.setOnClickListener {
            OverlayService.updateSignalColorGlobally("red")
        }

        controlYellowBtn.setOnClickListener {
            OverlayService.updateSignalColorGlobally("yellow")
        }

        controlGreenBtn.setOnClickListener {
            OverlayService.updateSignalColorGlobally("green")
        }
    }

    private fun updateControlBarVisibility() {
        val controlBar = findViewById<LinearLayout>(R.id.controlBar)

        if (OverlayService.isSessionActive()) {
            controlBar.visibility = View.VISIBLE
        } else {
            controlBar.visibility = View.GONE
        }
    }

    private fun updatePermissionStatus() {
        val statusText = findViewById<TextView>(R.id.statusText)
        val checkPermBtn = findViewById<Button>(R.id.checkPermButton)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                statusText.text = "✓ Permission granted"
                checkPermBtn.visibility = android.view.View.GONE
            } else {
                statusText.text = "Grant overlay permission"
                checkPermBtn.visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateControlBarVisibility()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            updatePermissionStatus()
        }
    }
}
