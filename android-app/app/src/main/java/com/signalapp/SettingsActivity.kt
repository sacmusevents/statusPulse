package com.signalapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backBtn = findViewById<Button>(R.id.backBtn)
        val keyDisplay = findViewById<TextView>(R.id.keyDisplay)
        val copyEncryptedKeyBtn = findViewById<Button>(R.id.copyEncryptedKeyBtn)
        val changeKeyBtn = findViewById<Button>(R.id.changeKeyBtn)
        val shareBtn = findViewById<Button>(R.id.shareBtn)

        backBtn.setOnClickListener { finish() }

        updateKeyDisplay(keyDisplay)

        copyEncryptedKeyBtn.setOnClickListener {
            val key = KeyManager.getSupabaseKey(this)
            if (key != null) {
                val encryptedKey = CryptoManager.encryptKey(key)
                if (encryptedKey != null) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Encrypted Supabase Key", encryptedKey)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Encrypted key copied to clipboard", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error encrypting key", Toast.LENGTH_SHORT).show()
                }
            }
        }

        changeKeyBtn.setOnClickListener {
            showKeyEntryDialog()
        }

        shareBtn.setOnClickListener {
            val key = KeyManager.getSupabaseKey(this)
            if (key != null) {
                val encryptedKey = CryptoManager.encryptKey(key)
                if (encryptedKey != null) {
                    val downloadUrl = "https://github.com/sacmusevents/red-yellow-green/releases/latest"
                    val shareText = """
                        StatusPulse - Signal Status App

                        Download APK: $downloadUrl

                        First-time Key: $encryptedKey
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "StatusPulse App + Key")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(intent, "Share app link and key"))
                } else {
                    Toast.makeText(this, "Error encrypting key", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateKeyDisplay(keyDisplay: TextView) {
        val key = KeyManager.getSupabaseKey(this)
        if (key != null) {
            val encryptedKey = CryptoManager.encryptKey(key)
            if (encryptedKey != null) {
                keyDisplay.text = encryptedKey
                keyDisplay.setTextIsSelectable(true)
            } else {
                keyDisplay.text = "No key configured"
            }
        } else {
            keyDisplay.text = "No key configured"
        }
    }

    private fun showKeyEntryDialog() {
        val input = EditText(this)
        input.hint = "Paste key here"

        AlertDialog.Builder(this)
            .setTitle("Change Key")
            .setMessage("Paste your key here. If you don't know your key, ask whoever shared the app with you to copy the key from their settings.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val keyInput = input.text.toString().trim()
                if (keyInput.isNotEmpty()) {
                    processKeyInput(keyInput)
                } else {
                    Toast.makeText(this, "Key cannot be empty", Toast.LENGTH_SHORT).show()
                    showKeyEntryDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processKeyInput(keyInput: String) {
        try {
            // Remove all whitespace (spaces, tabs, newlines) from input
            val cleanedInput = keyInput.replace(Regex("\\s"), "")

            val keyToStore = if (CryptoManager.isEncrypted(cleanedInput)) {
                // Try to decrypt
                val decrypted = CryptoManager.decryptKey(cleanedInput)
                if (decrypted != null) {
                    Toast.makeText(this, "Key decrypted successfully", Toast.LENGTH_SHORT).show()
                    decrypted
                } else {
                    Toast.makeText(this, "Error decrypting key. Make sure you're using the correct app.", Toast.LENGTH_SHORT).show()
                    showKeyEntryDialog()
                    return
                }
            } else {
                // Use as-is (raw key)
                cleanedInput
            }

            KeyManager.setSupabaseKey(this, keyToStore)
            Toast.makeText(this, "Key saved successfully", Toast.LENGTH_SHORT).show()
            val keyDisplay = findViewById<TextView>(R.id.keyDisplay)
            updateKeyDisplay(keyDisplay)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            showKeyEntryDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        val keyDisplay = findViewById<TextView>(R.id.keyDisplay)
        updateKeyDisplay(keyDisplay)
    }
}
