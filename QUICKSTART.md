# Multi-Session Signal Lights - Quick Start Guide

## 🚀 30-Minute Setup

### What You're Building
- **Android overlay app**: Control red/yellow/green lights from any app
- **Web display**: See live light updates on a separate screen
- **Multi-session**: Create multiple independent signal sessions for different events
- **Free hosting**: Supabase free tier covers everything

---

## Step 1: Create Supabase Account (5 min)

1. Go to [supabase.com](https://supabase.com)
2. Click **"Start your project"**
3. Sign up with email (no credit card)
4. Create a new project (any name)
5. Wait for project to initialize

---

## Step 2: Set Up Database (5 min)

1. In Supabase dashboard, go to **SQL Editor**
2. Click **"New query"**
3. Copy the entire contents of **`SUPABASE_SETUP.sql`** (in this folder)
4. Paste into the SQL editor
5. Click **"Run"**

You should see "Success" messages for creating both tables.

**Important:** After running SQL, go to **Database → Publications** and toggle ON the realtime for:
- ✅ `sessions` table
- ✅ `signals` table

---

## Step 3: Get Your Credentials (2 min)

1. Go to **Settings → API**
2. Copy your **Project URL** (starts with https://...)
3. Copy your **anon public key**
4. Keep these safe - you'll need them

---

## Step 4: Update Web Display (3 min)

1. Open `web-display/index.html` with a text editor
2. Find these lines (around line 253):
   ```javascript
   const SUPABASE_URL = 'YOUR_SUPABASE_URL_HERE';
   const SUPABASE_ANON_KEY = 'YOUR_SUPABASE_ANON_KEY_HERE';
   ```
3. Replace with your actual Supabase credentials
4. Save the file

**Test it:**
- Open `web-display/index.html` in a browser
- You should see "Select Session" and a "Connecting..." status
- Status should turn green with "Connected"

---

## Step 5: Update Android App (10 min)

Follow **`ANDROID_IMPLEMENTATION.md`** for detailed steps to:
1. Update `build.gradle` with Supabase dependencies
2. Create `SupabaseManager.kt` singleton
3. Update/create all Java files and layout XMLs
4. Configure `strings.xml` with Supabase credentials
5. Build and install on device

**Key files to update:**
- `build.gradle` - Add Supabase SDK
- `strings.xml` - Add Supabase credentials
- `MainActivity.kt` - Create/join session selection
- `SessionListActivity.kt` - List available sessions
- `CreateSessionActivity.kt` - Create new session
- `OverlayService.kt` - Updated with Supabase

---

## Testing

### Scenario 1: Single Device Test
1. **Android**: Create new session called "Test"
2. **Web**: Refresh browser, should see "Test" session in list
3. **Web**: Click on "Test" session
4. **Web**: You should see three lights (green is bright, red/yellow are dim)
5. **Android**: Button clicks on the overlay
6. **Web**: Light should update instantly

### Scenario 2: Two Device Test
1. **Device 1** (Web Display): Open `web-display/index.html` → select a session
2. **Device 2** (Android): Create or join the same session
3. **Device 2**: Press a button on overlay
4. **Device 1**: Light should change instantly
5. **Wait 5 min**: Non-green light should auto-reset to green

---

## Troubleshooting

### Web Display Shows "Connecting..." but no "Connected"
- ✅ Check Supabase URL and anon key are correct (no typos, no extra spaces)
- ✅ Check you enabled realtime for both `sessions` and `signals` tables
- ✅ Check you have internet connection

### Web Display Shows "Configure Supabase credentials"
- ✅ You didn't replace the placeholder credentials
- ✅ Refresh the browser after updating credentials

### Android App Won't Install
- ✅ Make sure you have Supabase SDK in `build.gradle`
- ✅ Sync Gradle after updating dependencies
- ✅ Check minimum SDK is 24

### Buttons Click but Nothing Happens
- ✅ Check Supabase credentials in Android `strings.xml`
- ✅ Check you're connected to internet
- ✅ Check `signals` table exists and has realtime enabled

### Session List is Empty in Android
- ✅ You haven't created a session yet from Android
- ✅ Or the Supabase credentials are wrong

---

## File Structure Reference

```
red-yellow-green/
├── web-display/
│   └── index.html ← Open this in browser (UPDATE: Add Supabase credentials)
├── android-app/
│   ├── build.gradle (UPDATE)
│   ├── AndroidManifest.xml (UPDATE)
│   ├── MainActivity.kt (CREATE/UPDATE)
│   ├── SessionListActivity.kt (CREATE)
│   ├── CreateSessionActivity.kt (CREATE)
│   ├── OverlayService.kt (UPDATE)
│   ├── SupabaseManager.kt (CREATE)
│   └── res/
│       ├── layout/ (CREATE/UPDATE)
│       ├── drawable/ (KEEP)
│       └── values/strings.xml (UPDATE)
├── SUPABASE_SETUP.sql ← Run this in Supabase SQL editor
├── ANDROID_IMPLEMENTATION.md ← Follow this for detailed Android setup
├── README.md
└── QUICKSTART.md (This file)
```

---

## Next Steps After Setup

1. **Customize colors**: Edit RGB values in the light circles (web/Android)
2. **Adjust timer**: Change `RESET_TIME` variable (currently 5 minutes)
3. **Add more buttons**: Add orange, purple, etc. colors to signals table
4. **Host web display**: Deploy to Netlify, GitHub Pages, or any static host
5. **Share session code**: Add QR code generation for easy session joining

---

## Architecture Summary

```
┌─────────────────────────────────────────────────────┐
│  Supabase Cloud Database                            │
│  ┌──────────────────────────────────────────────┐   │
│  │ Tables:                                      │   │
│  │ - sessions (id, title, status)              │   │
│  │ - signals (session_id, color, updated_at)   │   │
│  └──────────────────────────────────────────────┘   │
└──────────┬──────────────────────────────────────────┘
           │
           │ Real-time sync
    ┌──────┴──────┐
    │             │
    v             v
┌─────────────┐   ┌──────────────────┐
│ Web Display │   │ Android Overlay  │
│ (Browser)   │   │ (Phone)          │
│             │   │                  │
│ • Session   │   │ • Create session │
│   selector  │   │ • Join session   │
│ • Lights    │   │ • 3 Buttons      │
│   display   │   │ • Title display  │
└─────────────┘   └──────────────────┘
```

---

## Support

**Common Questions:**

Q: Can I use this offline?
A: No, you need internet for Supabase. For offline, you'd need a local server.

Q: What if I refresh the web page?
A: You'll be taken back to session list. The session data persists in Supabase.

Q: Can multiple Android apps control one session?
A: Yes! Just have them create/join the same session.

Q: Will my sessions be deleted?
A: No, they persist in Supabase free tier indefinitely (up to 500MB storage).

Q: How fast are updates?
A: Typically 100-500ms depending on internet speed.

---

**You're all set! 🎉**

For detailed Android setup instructions, see `ANDROID_IMPLEMENTATION.md`
