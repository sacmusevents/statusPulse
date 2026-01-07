# SignalLights Android App

Native Android overlay app for controlling red/yellow/green signal lights in real-time.

## Features

- **Persistent Overlay**: Floating overlay with signal control buttons (red, yellow, green)
- **Session Management**: Create new sessions or join existing ones
- **10-Second Auto-Reset**: Red/yellow lights automatically reset to green after 10 seconds
- **Real-time Sync**: Updates synchronized with web app via Supabase
- **Long-press Dragging**: Move the overlay by long-pressing and dragging
- **Web Integration**: 🌐 button opens web display with auto-selected session

## Build & Run

### Prerequisites
- Android Studio
- Android SDK 26+
- Kotlin

### Setup

1. **Configure Supabase Credentials**

   Edit `app/src/main/java/com/signalapp/SupabaseManager.kt`:
   ```kotlin
   const val SUPABASE_URL = "your-supabase-url"
   const val SUPABASE_ANON_KEY = "your-anon-key"
   ```

2. **Update Web App URL**

   Edit `app/src/main/java/com/signalapp/OverlayService.kt` line 161:
   ```kotlin
   val webAppUrl = "https://yourusername.github.io/red-yellow-green/?session=${currentSessionId}"
   ```

3. **Build Release APK**

   ```bash
   ./gradlew assembleRelease --stacktrace
   ```

   APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Architecture

### Main Components

- **MainActivity.kt**: Splash screen, permission requests
- **SessionListActivity.kt**: Browse and join existing sessions
- **CreateSessionActivity.kt**: Create new sessions
- **OverlayService.kt**: Persistent overlay with signal controls
- **SupabaseManager.kt**: Database operations via REST API

### Signal Flow

1. User clicks RED/YELLOW button on overlay
2. Android app updates Supabase `signals` table with new color + timestamp
3. Android sets 10-second timer to auto-reset to green
4. Web app polls every 3 seconds and displays the change
5. After 10 seconds, Android automatically resets to green in Supabase
6. Web app picks up the reset on next poll

### Key Features

- **No Realtime Complexity**: Uses simple HTTP polling instead of unreliable Realtime subscriptions
- **10-Second Timer**: Handled by Android app, not web app
- **Efficient**: ~1,200 API calls per day (well under Supabase free tier)
- **Dark Theme**: Navy gradient UI matching web app

## Permissions Required

- `INTERNET`: Network access to Supabase
- `SYSTEM_ALERT_WINDOW`: Display overlay on top of other apps
- `ACCESS_NETWORK_STATE`: Check network availability

## Testing

1. Install APK on Android device
2. Grant overlay permission when prompted
3. Create a session - overlay appears
4. Click buttons and verify web app updates within 3 seconds
5. Wait 10 seconds - verify auto-reset to green

## Troubleshooting

**App crashes on startup**
- Check Supabase credentials are correct
- Verify device has Android 8.0+

**Overlay doesn't appear**
- Grant "Display over other apps" permission in Settings
- Restart the app

**Web app not updating**
- Check both app and web are using same Supabase project
- Verify internet connection on both devices
- Check web app is polling (should see status at bottom)

**Buttons not working**
- Check device has internet connection
- Verify Supabase credentials
- Check Supabase dashboard for the signal update

## Dependencies

- Supabase JS SDK (v2)
- OkHttp 4.11.0
- Kotlinx Serialization 1.6.0
- AppCompat (AndroidX)
