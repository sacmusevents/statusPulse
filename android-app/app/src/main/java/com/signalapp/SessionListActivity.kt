package com.signalapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SessionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_list)

        val sessionContainer = findViewById<LinearLayout>(R.id.sessionContainer)
        val backBtn = findViewById<Button>(R.id.backBtn)

        backBtn.setOnClickListener { finish() }

        loadSessions(sessionContainer)
    }

    private fun loadSessions(container: LinearLayout) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sessions = SupabaseManager.getSessions(this@SessionListActivity)
                runOnUiThread {
                    container.removeAllViews()
                    if (sessions.isEmpty()) {
                        val tv = TextView(this@SessionListActivity)
                        tv.text = "No sessions"
                        tv.setTextColor(android.graphics.Color.GRAY)
                        container.addView(tv)
                    } else {
                        sessions.forEach { session ->
                            val row = LinearLayout(this@SessionListActivity)
                            row.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(0, 8, 0, 8) }
                            row.orientation = LinearLayout.HORIZONTAL

                            val btn = Button(this@SessionListActivity)
                            btn.text = session.title
                            btn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            btn.setBackgroundResource(R.drawable.session_button)
                            btn.setTextColor(android.graphics.Color.parseColor("#00ff00"))
                            btn.setPadding(16, 20, 16, 20)
                            btn.setOnClickListener { joinSession(session.id, session.title) }
                            row.addView(btn)

                            val deleteBtn = Button(this@SessionListActivity)
                            deleteBtn.text = "✕"
                            deleteBtn.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(8, 0, 0, 0) }
                            deleteBtn.setBackgroundResource(R.drawable.session_delete_button)
                            deleteBtn.setTextColor(android.graphics.Color.parseColor("#000000"))
                            deleteBtn.setPadding(12, 12, 12, 12)
                            deleteBtn.setOnClickListener { deleteSession(session.id) { loadSessions(container) } }
                            row.addView(deleteBtn)

                            container.addView(row)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SessionListActivity", "Error loading sessions", e)
            }
        }
    }

    private fun joinSession(sessionId: String, title: String) {
        // Save the last session
        val prefs = getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("lastSessionId", sessionId)
            putString("lastSessionTitle", title)
            apply()
        }

        stopService(Intent(this, OverlayService::class.java))
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("SESSION_ID", sessionId)
        intent.putExtra("SESSION_TITLE", title)
        startService(intent)
        finish()
    }

    private fun deleteSession(sessionId: String, onComplete: () -> Unit) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                SupabaseManager.deleteSession(sessionId, this@SessionListActivity)
                runOnUiThread {
                    Toast.makeText(this@SessionListActivity, "Session deleted", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SessionListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sessionContainer = findViewById<LinearLayout>(R.id.sessionContainer)
        loadSessions(sessionContainer)
    }
}
