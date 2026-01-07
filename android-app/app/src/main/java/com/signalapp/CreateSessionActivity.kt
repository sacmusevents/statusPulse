package com.signalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.UUID

class CreateSessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val sessionTitleInput: EditText = findViewById(R.id.sessionTitleInput)
        val createBtn: Button = findViewById(R.id.createBtn)
        val cancelBtn: Button = findViewById(R.id.cancelBtn)

        createBtn.setOnClickListener {
            val title = sessionTitleInput.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a session title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createSession(title)
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun createSession(title: String) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sessionId = UUID.randomUUID().toString()

                // Insert session
                val newSession = Session(
                    id = sessionId,
                    title = title,
                    status = "active"
                )
                SupabaseManager.insertSession(newSession)

                // Insert signal with green default
                val newSignal = Signal(
                    id = UUID.randomUUID().toString(),
                    session_id = sessionId,
                    color = "green"
                )
                SupabaseManager.insertSignal(newSignal)

                // Start overlay service on main thread
                runOnUiThread {
                    // Stop any existing overlay
                    stopService(Intent(this@CreateSessionActivity, OverlayService::class.java))
                    // Start new overlay
                    val intent = Intent(this@CreateSessionActivity, OverlayService::class.java)
                    intent.putExtra("SESSION_ID", sessionId)
                    intent.putExtra("SESSION_TITLE", title)
                    startService(intent)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@CreateSessionActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
