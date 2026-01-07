# Android Multi-Session Signal Light App - Implementation Guide

This guide provides detailed instructions for implementing the updated Android app with Supabase integration and multi-session support.

## Overview of Changes

The Android app now includes:
- **Session Management**: Create new sessions or join existing ones
- **Supabase Integration**: Replace Pusher with Supabase
- **Multi-Activity UI**: MainActivity, SessionListActivity, CreateSessionActivity
- **Overlay Service**: Updated with Supabase and session context

---

## Step 1: Update build.gradle

Replace the Pusher SDK dependency with Supabase SDK:

```gradle
// OLD (Remove):
implementation 'com.pusher:pusher-http-java:1.3.3'

// NEW (Add):
implementation 'io.github.jan-tennert.supabase:supabase-android:1.1.0'
implementation 'io.github.jan-tennert.supabase:realtime-android:1.1.0'
implementation 'io.github.jan-tennert.supabase:postgrest-android:1.1.0'
implementation 'io.github.jan-tennert.supabase:auth-android:1.1.0'

// Also add if not present:
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

---

## Step 2: Create SupabaseClient Singleton

Create a new file: `app/src/main/java/com/signalapp/SupabaseManager.kt`

```kotlin
package com.signalapp

import android.content.Context
import io.github.jan_tennert.supabase.Supabase
import io.github.jan_tennert.supabase.realtime.Realtime
import io.github.jan_tennert.supabase.postgrest.Postgrest

object SupabaseManager {
    private var supabaseClient: Supabase? = null
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL_HERE"
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY_HERE"

    fun getClient(context: Context): Supabase {
        if (supabaseClient == null) {
            supabaseClient = Supabase.create(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            )
        }
        return supabaseClient!!
    }

    fun isConfigured(): Boolean {
        return SUPABASE_URL != "YOUR_SUPABASE_URL_HERE" &&
               SUPABASE_ANON_KEY != "YOUR_SUPABASE_ANON_KEY_HERE"
    }
}
```

---

## Step 3: Update strings.xml

Replace `strings.xml` with Supabase configuration:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Signal Lights</string>

    <!-- Supabase Configuration -->
    <string name="supabase_url">YOUR_SUPABASE_URL_HERE</string>
    <string name="supabase_anon_key">YOUR_SUPABASE_ANON_KEY_HERE</string>

    <!-- Table and Column Names -->
    <string name="table_sessions">sessions</string>
    <string name="table_signals">signals</string>
</resources>
```

---

## Step 4: Update AndroidManifest.xml

Ensure these permissions are present:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.SignalApp"
        android:debuggable="true">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".SessionListActivity"
            android:exported="false" />

        <activity
            android:name=".CreateSessionActivity"
            android:exported="false" />

        <service
            android:name=".OverlayService"
            android:exported="false" />

    </application>

</manifest>
```

---

## Step 5: Implement MainActivity

File: `app/src/main/java/com/signalapp/MainActivity.kt`

This screen lets user choose: Create Session or Join Existing Session

```kotlin
package com.signalapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val OVERLAY_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText: TextView = findViewById(R.id.statusText)
        val createSessionBtn: Button = findViewById(R.id.createSessionBtn)
        val joinSessionBtn: Button = findViewById(R.id.joinSessionBtn)
        val checkPermBtn: Button = findViewById(R.id.checkPermButton)

        // Check overlay permission
        if (canDrawOverlay()) {
            statusText.text = "✓ Overlay permission granted\n\nCreate or join a session to begin"
            checkPermBtn.isEnabled = false
            checkPermBtn.text = "Permission Granted"
        } else {
            statusText.text = "Overlay permission required.\n\nClick below to grant permission."
            checkPermBtn.setOnClickListener {
                requestOverlayPermission()
            }
        }

        // Create Session button
        createSessionBtn.setOnClickListener {
            startActivity(Intent(this, CreateSessionActivity::class.java))
        }

        // Join Session button
        joinSessionBtn.setOnClickListener {
            startActivity(Intent(this, SessionListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permission again when returning
        if (canDrawOverlay() && !isServiceRunning()) {
            val createSessionBtn: Button = findViewById(R.id.createSessionBtn)
            val joinSessionBtn: Button = findViewById(R.id.joinSessionBtn)
            createSessionBtn.isEnabled = true
            joinSessionBtn.isEnabled = true
        }
    }

    private fun canDrawOverlay(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE && canDrawOverlay()) {
            val statusText: TextView = findViewById(R.id.statusText)
            statusText.text = "✓ Overlay permission granted\n\nCreate or join a session to begin"
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (OverlayService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
```

---

## Step 6: Implement SessionListActivity

File: `app/src/main/java/com/signalapp/SessionListActivity.kt`

Shows list of available sessions for user to join.

```kotlin
package com.signalapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.JsonObject

class SessionListActivity : AppCompatActivity() {
    private var currentSessionId: String? = null
    private var currentSessionTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_list)

        val sessionContainer: LinearLayout = findViewById(R.id.sessionContainer)
        val statusText: TextView = findViewById(R.id.statusText)

        // Load sessions from Supabase
        GlobalScope.launch {
            try {
                val supabase = SupabaseManager.getClient(this@SessionListActivity)
                val sessions = supabase.postgrest
                    .from("sessions")
                    .select()
                    .execute()

                // Parse and display sessions
                val jsonArray = sessions.body?.asJsonArray ?: listOf()

                runOnUiThread {
                    sessionContainer.removeAllViews()
                    if (jsonArray.isEmpty()) {
                        statusText.text = "No active sessions"
                        return@runOnUiThread
                    }

                    for (sessionJson in jsonArray) {
                        val session = sessionJson.asJsonObject
                        val sessionId = session.get("id").asString
                        val title = session.get("title").asString

                        val button = android.widget.Button(this@SessionListActivity)
                        button.text = title
                        button.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        button.setOnClickListener {
                            joinSession(sessionId, title)
                        }
                        sessionContainer.addView(button)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SessionListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun joinSession(sessionId: String, title: String) {
        currentSessionId = sessionId
        currentSessionTitle = title
        startOverlayService()
        finish()
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("SESSION_ID", currentSessionId)
        intent.putExtra("SESSION_TITLE", currentSessionTitle)
        startService(intent)
    }
}
```

---

## Step 7: Implement CreateSessionActivity

File: `app/src/main/java/com/signalapp/CreateSessionActivity.kt`

Allows user to create a new session.

```kotlin
package com.signalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.JsonObject

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

            createSessionInDatabase(title)
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun createSessionInDatabase(title: String) {
        GlobalScope.launch {
            try {
                val supabase = SupabaseManager.getClient(this@CreateSessionActivity)

                // Create session record
                val sessionData = JsonObject()
                sessionData.addProperty("title", title)
                sessionData.addProperty("status", "active")

                val sessionResponse = supabase.postgrest
                    .from("sessions")
                    .insert(sessionData.toString())
                    .execute()

                // Parse response to get session ID
                val sessionId = sessionResponse.body?.asJsonArray?.get(0)?.asJsonObject?.get("id")?.asString

                // Create initial signal record (default: green)
                if (sessionId != null) {
                    val signalData = JsonObject()
                    signalData.addProperty("session_id", sessionId)
                    signalData.addProperty("color", "green")

                    supabase.postgrest
                        .from("signals")
                        .insert(signalData.toString())
                        .execute()

                    // Start overlay service with new session
                    runOnUiThread {
                        val intent = Intent(this@CreateSessionActivity, OverlayService::class.java)
                        intent.putExtra("SESSION_ID", sessionId)
                        intent.putExtra("SESSION_TITLE", title)
                        startService(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CreateSessionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

---

## Step 8: Update OverlayService

File: `app/src/main/java/com/signalapp/OverlayService.kt`

Updated to work with Supabase and display session title.

```kotlin
package com.signalapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.JsonObject
import io.github.jan_tennert.supabase.realtime.Realtime

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var currentSessionId: String? = null
    private var currentSessionTitle: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentSessionId = intent?.getStringExtra("SESSION_ID")
        currentSessionTitle = intent?.getStringExtra("SESSION_TITLE")

        if (currentSessionId != null && currentSessionTitle != null) {
            setupOverlay()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Update session title
        val titleView: TextView = overlayView.findViewById(R.id.sessionTitle)
        titleView.text = currentSessionTitle

        // Layout parameters
        val params = WindowManager.LayoutParams(
            350,
            450,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 20

        windowManager.addView(overlayView, params)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val redButton: Button = overlayView.findViewById(R.id.red_button)
        val yellowButton: Button = overlayView.findViewById(R.id.yellow_button)
        val greenButton: Button = overlayView.findViewById(R.id.green_button)

        redButton.setOnClickListener {
            updateSignalColor("red")
        }

        yellowButton.setOnClickListener {
            updateSignalColor("yellow")
        }

        greenButton.setOnClickListener {
            updateSignalColor("green")
        }
    }

    private fun updateSignalColor(color: String) {
        GlobalScope.launch {
            try {
                val supabase = SupabaseManager.getClient(this@OverlayService)

                val updateData = JsonObject()
                updateData.addProperty("color", color)
                updateData.addProperty("updated_at", System.currentTimeMillis().toString())

                supabase.postgrest
                    .from("signals")
                    .update(updateData.toString())
                    .eq("session_id", currentSessionId!!)
                    .execute()

                runOnUiThread {
                    Toast.makeText(this@OverlayService, color.uppercase(), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        GlobalScope.launch {
            runOnUiThread {
                Toast.makeText(this@OverlayService, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}
```

---

## Step 9: Create Layout Files

### activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="30dp"
    android:background="#ffffff">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Signal Lights"
        android:textSize="32sp"
        android:textStyle="bold"
        android:textColor="#000000"
        android:layout_marginBottom="30dp" />

    <TextView
        android:id="@+id/statusText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Checking permissions..."
        android:textSize="16sp"
        android:textColor="#333333"
        android:layout_marginBottom="40dp"
        android:gravity="center" />

    <Button
        android:id="@+id/checkPermButton"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="Grant Overlay Permission"
        android:textSize="14sp"
        android:textStyle="bold"
        android:background="#ff9999"
        android:textColor="#000000"
        android:layout_marginBottom="20dp" />

    <Button
        android:id="@+id/createSessionBtn"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="Create New Session"
        android:textSize="16sp"
        android:textStyle="bold"
        android:background="#00ff00"
        android:textColor="#000000"
        android:layout_marginBottom="15dp" />

    <Button
        android:id="@+id/joinSessionBtn"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="Join Existing Session"
        android:textSize="16sp"
        android:textStyle="bold"
        android:background="#ffff00"
        android:textColor="#000000" />

</LinearLayout>
```

### activity_create_session.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="30dp"
    android:background="#ffffff">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Create Session"
        android:textSize="28sp"
        android:textStyle="bold"
        android:textColor="#000000"
        android:layout_marginBottom="30dp" />

    <EditText
        android:id="@+id/sessionTitleInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter session name (e.g., 'Main Stage')"
        android:inputType="text"
        android:padding="15dp"
        android:layout_marginBottom="20dp"
        android:background="#f0f0f0"
        android:textColor="#000000" />

    <Button
        android:id="@+id/createBtn"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="Create Session"
        android:textSize="16sp"
        android:textStyle="bold"
        android:background="#00ff00"
        android:textColor="#000000"
        android:layout_marginBottom="15dp" />

    <Button
        android:id="@+id/cancelBtn"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:text="Cancel"
        android:textSize="16sp"
        android:textStyle="bold"
        android:background="#cccccc"
        android:textColor="#000000" />

</LinearLayout>
```

### activity_session_list.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="20dp"
    android:background="#ffffff">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Select Session"
        android:textSize="28sp"
        android:textStyle="bold"
        android:textColor="#000000"
        android:layout_marginBottom="20dp" />

    <TextView
        android:id="@+id/statusText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Loading sessions..."
        android:textSize="14sp"
        android:textColor="#666666"
        android:layout_marginBottom="20dp" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:id="@+id/sessionContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="top" />

    </ScrollView>

</LinearLayout>
```

### overlay_layout.xml (Updated)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/rounded_background"
    android:padding="15dp"
    android:gravity="center">

    <TextView
        android:id="@+id/sessionTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Session Name"
        android:textSize="14sp"
        android:textStyle="bold"
        android:textColor="#00ff00"
        android:gravity="center"
        android:layout_marginBottom="15dp" />

    <Button
        android:id="@+id/red_button"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        android:text="RED"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#ffffff"
        android:background="@drawable/button_red"
        android:layout_marginBottom="10dp" />

    <Button
        android:id="@+id/yellow_button"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        android:text="YELLOW"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#000000"
        android:background="@drawable/button_yellow"
        android:layout_marginBottom="10dp" />

    <Button
        android:id="@+id/green_button"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        android:text="GREEN"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#000000"
        android:background="@drawable/button_green" />

</LinearLayout>
```

---

## Summary of Files to Create/Update

| File | Status |
|---|---|
| `build.gradle` | Update: Replace Pusher with Supabase |
| `AndroidManifest.xml` | Update: Add new activities |
| `strings.xml` | Update: Supabase credentials |
| `SupabaseManager.kt` | Create: New singleton |
| `MainActivity.kt` | Update: Replace with session selection |
| `SessionListActivity.kt` | Create: New activity |
| `CreateSessionActivity.kt` | Create: New activity |
| `OverlayService.kt` | Update: Supabase integration |
| `activity_main.xml` | Update: New UI |
| `activity_create_session.xml` | Create: New layout |
| `activity_session_list.xml` | Create: New layout |
| `overlay_layout.xml` | Update: Add session title |
| `button_red.xml` | Keep as is |
| `button_yellow.xml` | Keep as is |
| `button_green.xml` | Keep as is |
| `rounded_background.xml` | Keep as is |
| `themes.xml` | Keep as is |

---

## Next Steps

1. Update `build.gradle` with Supabase dependencies
2. Create `SupabaseManager.kt` singleton
3. Update `AndroidManifest.xml` with new activities
4. Update `strings.xml` with Supabase credentials
5. Create/update all Kotlin activity files
6. Create/update all layout XML files
7. Build and test on Android device/emulator
