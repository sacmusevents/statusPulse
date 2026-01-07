package com.signalapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_list)

        val sessionContainer: LinearLayout = findViewById(R.id.sessionContainer)
        val statusText: TextView = findViewById(R.id.statusText)

        loadSessions(sessionContainer, statusText)
    }

    private fun loadSessions(container: LinearLayout, status: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sessions = SupabaseManager.getSessions()

                runOnUiThread {
                    container.removeAllViews()
                    if (sessions.isEmpty()) {
                        status.text = "No active sessions"
                        return@runOnUiThread
                    }

                    for (session: Session in sessions) {
                        // Create a horizontal layout for session button + delete button
                        val rowLayout = LinearLayout(this@SessionListActivity)
                        rowLayout.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        rowLayout.orientation = LinearLayout.HORIZONTAL

                        // Session button
                        val button = Button(this@SessionListActivity)
                        button.text = session.title
                        button.layoutParams = LinearLayout.LayoutParams(
                            0,
                            80,
                            1f
                        )
                        button.setTextColor(android.graphics.Color.WHITE)
                        button.textSize = 14f
                        button.setBackgroundColor(android.graphics.Color.parseColor("#3f51b5"))
                        button.layoutParams.let {
                            (it as LinearLayout.LayoutParams).setMargins(0, 8, 8, 8)
                        }
                        button.setOnClickListener {
                            joinSession(session.id, session.title)
                        }

                        // Delete button
                        val deleteButton = Button(this@SessionListActivity)
                        deleteButton.text = "✕"
                        deleteButton.layoutParams = LinearLayout.LayoutParams(
                            80,
                            80
                        )
                        deleteButton.setTextColor(android.graphics.Color.parseColor("#ff6b6b"))
                        deleteButton.textSize = 16f
                        deleteButton.setBackgroundColor(android.graphics.Color.parseColor("#2a2a3e"))
                        deleteButton.layoutParams.let {
                            (it as LinearLayout.LayoutParams).setMargins(0, 8, 0, 8)
                        }
                        deleteButton.setOnClickListener {
                            deleteSession(session.id, container, status)
                        }

                        rowLayout.addView(button)
                        rowLayout.addView(deleteButton)
                        container.addView(rowLayout)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SessionListActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun deleteSession(sessionId: String, container: LinearLayout, status: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseManager.deleteSession(sessionId)
                // Reload sessions after deletion
                loadSessions(container, status)
                runOnUiThread {
                    Toast.makeText(
                        this@SessionListActivity,
                        "Session deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SessionListActivity,
                        "Error deleting: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun joinSession(sessionId: String, title: String) {
        // Stop any existing overlay
        stopService(Intent(this, OverlayService::class.java))
        // Start new overlay
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("SESSION_ID", sessionId)
        intent.putExtra("SESSION_TITLE", title)
        startService(intent)
        finish()
    }
}
