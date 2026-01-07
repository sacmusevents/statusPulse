# Signal Lights

Real-time signal light control with Android overlay and web display.

## Quick Start

### Web Display
1. Open `web-display/index.html` in a browser
2. Select or create a session

### Android App
1. Build: `cd android-app && ./gradlew assembleDebug`
2. Install: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Grant overlay permission when prompted
4. Create/join session from app

## How It Works

- Android app controls red/yellow/green lights via buttons
- Web display syncs via Supabase polling (every 3 seconds)
- Non-green lights auto-reset to green after 10 seconds
- Multiple independent sessions supported

## Files

- `web-display/index.html` - Web app (single HTML file)
- `android-app/` - Android app source
- `android-app/app/src/main/java/com/signalapp/` - Main code

## Requirements

- Android 6.0+ for app
- Modern browser for web display
- Supabase account (free tier: 500MB, 2M messages/month)
