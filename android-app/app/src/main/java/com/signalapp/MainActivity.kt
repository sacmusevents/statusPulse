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

        try {
            setContentView(R.layout.activity_main)

            val statusText: TextView? = findViewById(R.id.statusText)
            val createSessionBtn: Button? = findViewById(R.id.createSessionBtn)
            val joinSessionBtn: Button? = findViewById(R.id.joinSessionBtn)
            val checkPermBtn: Button? = findViewById(R.id.checkPermButton)

            if (createSessionBtn == null || joinSessionBtn == null || checkPermBtn == null) {
                statusText?.text = "Layout Error: Views not found"
                return
            }

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
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
        }
    }

    private fun updatePermissionStatus() {
        try {
            val statusText: TextView? = findViewById(R.id.statusText)
            val checkPermBtn: Button? = findViewById(R.id.checkPermButton)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    statusText?.text = "✓ Overlay permission granted\n\nCreate or join a session to begin"
                    checkPermBtn?.visibility = android.view.View.GONE
                } else {
                    statusText?.text = "Overlay permission required.\n\nClick below to grant permission."
                    checkPermBtn?.visibility = android.view.View.VISIBLE
                    checkPermBtn?.isEnabled = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating permission status", e)
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