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

        val sessionNameInput = findViewById<EditText>(R.id.sessionNameInput)
        val createBtn = findViewById<Button>(R.id.createBtn)
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)

        createBtn.setOnClickListener {
            val title = sessionNameInput.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter session name", Toast.LENGTH_SHORT).show()
            } else {
                createSession(title)
            }
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun createSession(title: String) {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sessionId = UUID.randomUUID().toString()
                SupabaseManager.insertSession(Session(id = sessionId, title = title, status = "active"), this@CreateSessionActivity)
                SupabaseManager.insertSignal(Signal(id = UUID.randomUUID().toString(), session_id = sessionId, color = "green"), this@CreateSessionActivity)

                runOnUiThread {
                    val intent = Intent(this@CreateSessionActivity, MainActivity::class.java)
                    intent.putExtra("SESSION_ID", sessionId)
                    intent.putExtra("SESSION_TITLE", title)
                    startService(Intent(this@CreateSessionActivity, OverlayService::class.java).apply {
                        putExtra("SESSION_ID", sessionId)
                        putExtra("SESSION_TITLE", title)
                    })
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CreateSessionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
