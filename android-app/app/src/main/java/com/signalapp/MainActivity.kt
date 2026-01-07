package com.signalapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
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

        updatePermissionStatus()
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
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            updatePermissionStatus()
        }
    }
}
