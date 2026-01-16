package com.signalapp

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_CODE = 100
    private val WEB_APP_URL = "https://sacmusevents.github.io/statusPulse/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if Supabase key is configured
        if (!KeyManager.hasSupabaseKey(this)) {
            showKeyEntryDialog()
        }

        val createSessionBtn = findViewById<Button>(R.id.createSessionBtn)
        val joinSessionBtn = findViewById<Button>(R.id.joinSessionBtn)
        val checkPermBtn = findViewById<Button>(R.id.checkPermButton)
        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        checkPermBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE)
            }
        }

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        createSessionBtn.setOnClickListener {
            startActivity(Intent(this, CreateSessionActivity::class.java))
        }

        joinSessionBtn.setOnClickListener {
            startActivity(Intent(this, SessionListActivity::class.java))
        }

        val openWebAppBtn = findViewById<Button>(R.id.openWebAppBtn)
        val copyWebAppUrlBtn = findViewById<Button>(R.id.copyWebAppUrlBtn)

        openWebAppBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEB_APP_URL))
            startActivity(intent)
        }

        copyWebAppUrlBtn.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Web App URL", WEB_APP_URL)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        val shareWebAppQrBtn = findViewById<ImageButton>(R.id.shareWebAppQrBtn)
        shareWebAppQrBtn.setOnClickListener {
            showQRCodeDialog(WEB_APP_URL, "Web App Link")
        }

        val rejoinSessionBtn = findViewById<Button>(R.id.rejoinSessionBtn)
        val endSessionBtn = findViewById<Button>(R.id.endSessionBtn)

        endSessionBtn.setOnClickListener {
            OverlayService.closeSessionGracefully()
        }

        // Register callback for when session is closed gracefully
        OverlayService.setOnSessionClosedCallback {
            updateControlBarVisibility()
            setupSessionButtons(rejoinSessionBtn, endSessionBtn)
        }

        setupSessionButtons(rejoinSessionBtn, endSessionBtn)

        // Setup bottom control bar buttons
        setupControlButtons()

        updatePermissionStatus()
        updateControlBarVisibility()
    }

    private fun setupSessionButtons(rejoinBtn: Button, endSessionBtn: Button) {
        val hasActiveSession = OverlayService.isSessionActive()
        val prefs = getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        val lastSessionId = prefs.getString("lastSessionId", null)
        val lastSessionTitle = prefs.getString("lastSessionTitle", null)

        if (hasActiveSession) {
            // Show end session button, hide rejoin button
            endSessionBtn.visibility = View.VISIBLE
            rejoinBtn.visibility = View.GONE
        } else if (lastSessionId != null && lastSessionTitle != null) {
            // Show rejoin button, hide end session button
            rejoinBtn.visibility = View.VISIBLE
            rejoinBtn.text = "↺ Rejoin: $lastSessionTitle"
            rejoinBtn.setOnClickListener {
                rejoinSession(lastSessionId, lastSessionTitle)
            }
            endSessionBtn.visibility = View.GONE
        } else {
            // Hide both if no session active and no history
            rejoinBtn.visibility = View.GONE
            endSessionBtn.visibility = View.GONE
        }
    }

    private fun rejoinSession(sessionId: String, title: String) {
        stopService(Intent(this, OverlayService::class.java))
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("SESSION_ID", sessionId)
        intent.putExtra("SESSION_TITLE", title)
        startService(intent)
        finish()
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
        val joinSessionBtn = findViewById<Button>(R.id.joinSessionBtn)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                statusText.text = "✓ Permission granted"
                checkPermBtn.visibility = android.view.View.GONE
                joinSessionBtn.isEnabled = true
                joinSessionBtn.alpha = 1.0f
            } else {
                statusText.text = "Grant overlay permission"
                checkPermBtn.visibility = android.view.View.VISIBLE
                joinSessionBtn.isEnabled = false
                joinSessionBtn.alpha = 0.5f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateControlBarVisibility()

        val rejoinSessionBtn = findViewById<Button>(R.id.rejoinSessionBtn)
        val endSessionBtn = findViewById<Button>(R.id.endSessionBtn)
        setupSessionButtons(rejoinSessionBtn, endSessionBtn)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            updatePermissionStatus()
        }
    }

    private fun showKeyEntryDialog() {
        val input = EditText(this)
        input.hint = "Paste key here"

        AlertDialog.Builder(this)
            .setTitle("Enter Key")
            .setMessage("Paste your key here. If you don't know your key, ask whoever shared the app with you to copy the key from their settings.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val keyInput = input.text.toString().trim()
                if (keyInput.isNotEmpty()) {
                    processKeyInput(keyInput)
                } else {
                    Toast.makeText(this, "Key cannot be empty", Toast.LENGTH_SHORT).show()
                    showKeyEntryDialog()
                }
            }
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
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            showKeyEntryDialog()
        }
    }

    private fun generateQRCode(content: String, size: Int = 512): Bitmap? {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating QR code: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun showQRCodeDialog(content: String, title: String) {
        val qrBitmap = generateQRCode(content)
        if (qrBitmap == null) return

        val imageView = ImageView(this).apply {
            setImageBitmap(qrBitmap)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(imageView)
            .setPositiveButton("Share") { _, _ ->
                shareQRCode(qrBitmap)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun shareQRCode(bitmap: Bitmap) {
        try {
            val file = java.io.File(cacheDir, "qr_code.png")
            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share QR Code"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
