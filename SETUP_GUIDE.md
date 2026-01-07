# SignalLights - Complete Setup Guide

## Overview
Real-time multi-session signal light system with:
- **Android overlay app** - Control red/yellow/green lights from persistent overlay
- **Web display app** - View and control lights in real-time
- **Supabase backend** - Real-time database with PostgreSQL
- **Session sharing** - Share sessions between app and web via URL

---

## Part 1: Supabase Setup (Already Configured)

Your Supabase project is already set up with:
- **URL:** `https://pgipjbsekpwzrjxjytxv.supabase.co`
- **API Key:** `sb_publishable_9sD73_OSr4EgV9UFslJykA_bRBS5Kgr`
- **Tables:** `sessions` and `signals`
- **Real-time:** Enabled on both tables

No action needed - already connected in code.

---

## Part 2: Distribution Setup

### Option A: GitHub Pages (Recommended - Free & Easy)

Deploy web app to GitHub Pages:

```bash
cd /Users/yahya/Documents/Github-sacmusevents/red-yellow-green

# Create gh-pages branch
git checkout --orphan gh-pages
git rm -rf .
cp -r web-display/* .
git add .
git commit -m "Deploy SignalLights web app"
git push origin gh-pages
```

Your web app will be at: **`https://yourusername.github.io/red-yellow-green/`**

### Option B: Self-Hosted (Custom Domain)

If you have a server:

```bash
# Copy web app to server
scp web-display/index.html user@yourserver.com:/var/www/yourdomain.com/signallights/
```

Your web app will be at: **`https://yourdomain.com/signallights/`**

### Hosting the APK

After building the APK, host it at one of these locations:

**Option 1: Your Web Server**
```bash
scp app/build/outputs/apk/release/app-release.apk user@yourserver.com:/var/www/yourdomain.com/downloads/
```
Access: `https://yourdomain.com/downloads/app-release.apk`

**Option 2: GitHub Releases**
```bash
gh release create v1.0.0 app/build/outputs/apk/release/app-release.apk
```

**Option 3: Google Drive (Easy for Testing)**
1. Upload `app-release.apk` to Google Drive
2. Share link, then convert to direct download

---

## Part 3: Update URLs in Code

Before building the APK, update these URLs in the source code:

### Step 1: Update Web App URL (Android)

Open `app/src/main/java/com/signalapp/OverlayService.kt` around line 161:

```kotlin
val webAppUrl = "https://yourdomain.com/signallights/?session=${currentSessionId}"
```

Replace `https://yourdomain.com/signallights/` with your actual web app URL:

- **If using GitHub Pages:** `https://yourusername.github.io/red-yellow-green/`
- **If self-hosted:** `https://yourdomain.com/signallights/`

### Step 2: Update APK Download URL (Web)

Open `web-display/index.html` around line 267:

```html
<a href="https://yourdomain.com/downloads/signallights.apk" class="download-button">📱 Download App</a>
```

Replace with your actual APK URL:

- **If hosting on web server:** `https://yourdomain.com/downloads/app-release.apk`
- **If using GitHub Releases:** `https://github.com/yourusername/repo-name/releases/download/v1.0.0/app-release.apk`
- **If using Google Drive:** Your direct download link

---

## Part 4: Build the APK

After updating the URLs:

```bash
cd /Users/yahya/AndroidStudioProjects/SignalLights
./gradlew assembleRelease --stacktrace
```

APK location: `app/build/outputs/apk/release/app-release.apk` (~5MB)

Then host it at the URL you specified in step 2 above.

---

## Part 5: Testing the System

### Test 1: Basic Installation

1. **Install APK on Android device:**
   - Download the APK from the hosting URL
   - Allow installation from unknown sources if prompted
   - Install the app

2. **Open web app in browser:**
   - Open your web app URL in Chrome/Safari/Firefox
   - You should see "Select Session"
   - Check footer - should show "✓ Realtime: Connected" (green)

### Test 2: Create and Control Session

1. **Create session on Android:**
   - Tap "Create New Session"
   - Enter a name (e.g., "Test Session")
   - Overlay should appear with red/yellow/green buttons

2. **View session on web:**
   - Session should appear in web app
   - Click on it to enter display mode
   - You should see three lights (green is bright by default)

3. **Control from Android:**
   - Click **RED** button on overlay
   - Red light should turn bright on web (within 1 second)
   - Web light should auto-reset to green after 10 seconds

4. **Test linking:**
   - Click 🌐 button in Android overlay
   - Web browser should open with that session auto-selected

### Test 3: Multi-Device

If you have two devices:
1. **Device 1:** Open web app in browser
2. **Device 2:** Install Android app
3. Create/join session on Android
4. Session should appear on Device 1 immediately
5. Click buttons on Android → web updates in real-time

---

## Troubleshooting

### Web App Shows "Realtime: Timed Out (retrying...)"
- Check your internet connection
- Verify Supabase credentials are correct
- Wait 3 seconds - it should retry automatically
- If it keeps timing out, your Supabase project might be inactive

### Android App Shows "Error: null"
- Make sure you have internet connection
- Check that Supabase URL and API key are correct
- Try closing and reopening the app

### Web App Doesn't Show Sessions
- Refresh the page
- Check that Android app is using the same Supabase credentials
- Verify Realtime is enabled on the sessions table in Supabase

### 🌐 Button Opens Wrong URL
- Edit `OverlayService.kt` line 161
- Make sure your web app URL is correct (no trailing slash issues)
- Rebuild APK

### Real-time Updates Don't Work
- Check Realtime status in web footer
- Verify both devices have internet
- Try switching sessions (back to list, then select again)
- The web may need a refresh to start receiving updates

---

## Timer Behavior

- **Green:** Default state, stays until you press another button
- **Red/Yellow:** Automatically resets to green after 10 seconds
- **Re-pressing:** Resets the 10-second timer

---

## Security

All sessions are **public if someone has the link:**
- Each session has a unique UUID (2^128 possibilities - hard to guess)
- But if you share the session ID, anyone can control it
- No password protection (intentional for simplicity)

**Good practices:**
- Only share session IDs with trusted people
- Don't post session IDs publicly
- Sessions are temporary (you control the duration)

---

## URLs Reference

| Feature | Purpose | URL |
|---------|---------|-----|
| Web App | Display/control lights | `https://yourdomain.com/signallights/` |
| APK Download | Install app | `https://yourdomain.com/downloads/app-release.apk` |
| Auto-Select | Open session from app | `https://yourdomain.com/signallights/?session=UUID` |

---

## Quick Checklist

- [ ] Deploy web app (GitHub Pages or self-hosted)
- [ ] Host APK file
- [ ] Update web app URL in `OverlayService.kt`
- [ ] Update APK URL in `web-display/index.html`
- [ ] Build release APK
- [ ] Test on Android device
- [ ] Test web app connection
- [ ] Verify real-time updates work
- [ ] Test linking between app and web

---

## Getting Help

If something doesn't work:
1. **Check web app footer** - shows Realtime connection status
2. **Check browser console** - press F12 → Console tab for errors
3. **Check Android logs** - adb logcat or Android Studio Logcat
4. **Verify URLs** - make sure they match exactly what you configured
5. **Test connectivity** - both devices need internet access

Enjoy your signal light system! 🚦
