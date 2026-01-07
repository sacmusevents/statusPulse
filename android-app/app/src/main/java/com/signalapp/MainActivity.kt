package com.signalapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<android.widget.TextView>(R.id.statusText)
        val sessionList = findViewById<LinearLayout>(R.id.sessionList)
        val createInput = findViewById<EditText>(R.id.sessionNameInput)
        val createBtn = findViewById<Button>(R.id.createBtn)
        val checkPermBtn = findViewById<Button>(R.id.checkPermButton)

        checkPermBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    OVERLAY_PERMISSION_CODE
                )
            }
        }

        createBtn.setOnClickListener {
            val title = createInput.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter session name", Toast.LENGTH_SHORT).show()
            } else {
                createAndStartSession(title)
                createInput.text.clear()
            }
        }

        updatePermissionStatus()
        loadSessions(sessionList)
    }

    private fun updatePermissionStatus() {
        val statusText = findViewById<android.widget.TextView>(R.id.statusText)
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

    private fun loadSessions(container: LinearLayout) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sessions = SupabaseManager.getSessions()
                runOnUiThread {
                    container.removeAllViews()
                    if (sessions.isEmpty()) {
                        val tv = android.widget.TextView(this@MainActivity)
                        tv.text = "No sessions"
                        tv.setTextColor(android.graphics.Color.GRAY)
                        container.addView(tv)
                    } else {
                        sessions.forEach { session ->
                            val btn = Button(this@MainActivity)
                            btn.text = session.title
                            btn.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(0, 5, 0, 5) }
                            btn.setOnClickListener { startSession(session.id, session.title) }
                            container.addView(btn)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading sessions", e)
            }
        }
    }

    private fun createAndStartSession(title: String) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sessionId = UUID.randomUUID().toString()
                SupabaseManager.insertSession(Session(id = sessionId, title = title, status = "active"))
                SupabaseManager.insertSignal(Signal(id = UUID.randomUUID().toString(), session_id = sessionId, color = "green"))
                runOnUiThread { startSession(sessionId, title) }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun startSession(sessionId: String, title: String) {
        stopService(Intent(this, OverlayService::class.java))
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("SESSION_ID", sessionId)
        intent.putExtra("SESSION_TITLE", title)
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        val sessionList = findViewById<LinearLayout>(R.id.sessionList)
        loadSessions(sessionList)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            updatePermissionStatus()
        }
    }
}
