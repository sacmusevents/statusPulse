# SignalLights - Distribution & Security Strategy

## Overview
This document outlines how to distribute the SignalLights app/website while maintaining security and privacy.

## Part 1: App Distribution

### Option A: Direct APK Download (Recommended for Testing)

**Setup:**
1. Build the signed APK:
   ```bash
   cd /Users/yahya/AndroidStudioProjects/SignalLights
   ./gradlew assembleRelease
   ```
   APK location: `app/build/outputs/apk/release/app-release.apk`

2. Host the APK on your web server or cloud storage:
   - AWS S3
   - Google Drive (share link)
   - GitHub Releases
   - Your own web server

3. Create a download page on your web app with a big download button

**Pros:** Simple, direct control
**Cons:** Manual updates required

### Option B: Google Play Store (Production)

**Steps:**
1. Create a Google Play Console account ($25 one-time)
2. Set up app signing certificate
3. Create release build with version incrementing
4. Upload to Play Store with screenshots/description
5. Users install from Play Store app

**Pros:** Official distribution, auto-updates, discoverability
**Cons:** Review process, compliance requirements, fee

### Option C: GitHub Releases (For Development)

**Setup:**
1. Create a GitHub release with the APK attached
2. Users download directly from GitHub
3. Can use `gh release create` command

**Pros:** Free, automatic CI/CD integration
**Cons:** Not user-friendly for non-developers

## Part 2: Web App Distribution

### Current Setup
Web app is static HTML at: `/Users/yahya/Documents/Github-sacmusevents/red-yellow-green/web-display/index.html`

### Hosting Options

#### Option A: GitHub Pages (Free, Recommended for Testing)

**Setup:**
```bash
# Push web-display folder to GitHub
git add web-display/
git commit -m "Add web display app"
git push origin main

# Enable GitHub Pages in repository settings
# Settings → Pages → Source: main branch → /root (or /docs folder)
```

**Access:** `https://yourusername.github.io/repo-name/web-display/`

**Pros:** Free, automatic HTTPS, easy updates
**Cons:** Public by default, limited customization

#### Option B: Self-Hosted Web Server

**Setup:**
```bash
# Copy web-display/index.html to your server
scp web-display/index.html user@yourserver.com:/var/www/html/signallights/

# Make it accessible
# https://yourdomain.com/signallights/
```

**Pros:** Full control, custom domain, analytics
**Cons:** Requires server infrastructure

#### Option C: Netlify or Vercel (Free with Custom Domain)

1. Connect GitHub repo
2. Deploy automatically on push
3. Custom domain (optional paid)

## Part 3: Linking App ↔ Web

### Strategy: QR Code Exchange

#### 1. QR Code in Android App
The overlay can show a QR code that opens the web app:

```kotlin
// In OverlayService.kt - add QR code button
val qrButton: Button = overlayView.findViewById(R.id.qr_button)
qrButton.setOnClickListener {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("https://yourdomain.com/signallights/?session=${currentSessionId}")
    startActivity(intent)
}
```

#### 2. QR Code in Web App
The web app shows a QR code to install the app:

```html
<!-- In web-display/index.html - session selection screen -->
<div style="text-align: center; margin-top: 40px;">
    <p>Want to control the light?</p>
    <img src="qr-code-to-app-download.png" alt="Download app" />
    <p>Scan to install Android app</p>
</div>
```

#### 3. Deep Linking
When user scans QR code from web:
- If app installed: Opens app directly to session
- If app not installed: Opens Play Store for download

**Deep Link Setup:**
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".SessionListActivity">
    <intent-filter android:label="@string/app_name">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="signallights" android:host="session" />
    </intent-filter>
</activity>

<!-- When Android receives: signallights://session?id=123 -->
<!-- It opens the app with session ID -->
```

## Part 4: Security - Preventing Random Discovery

### Current Issue
- Web app and Android app use public Supabase API keys
- Anyone with the API keys can access any session
- Need to prevent unauthorized access

### Solution: Session-Based Access Control

#### Level 1: Session ID Obfuscation (Current)
- Each session has a UUID: `550e8400-e29b-41d4-a716-446655440000`
- You must know the exact ID to access it
- Hard to guess or brute-force (2^128 possibilities)

#### Level 2: Public/Private Sessions (Recommended)

Add to Supabase `sessions` table:
```sql
ALTER TABLE sessions ADD COLUMN access_type TEXT DEFAULT 'private';
-- access_type: 'private' (invite only) or 'public' (anyone with link)
```

**In Android app:**
```kotlin
// When creating session
val newSession = Session(
    id = sessionId,
    title = title,
    status = "active",
    access_type = "private"  // Default to private
)
```

**In web app:**
```javascript
// Check access type
const { data: session, error } = await supabaseClient
    .from('sessions')
    .select('access_type')
    .eq('id', sessionId)
    .single();

if (session?.access_type === 'private') {
    // Show "This session is private" message
    showError('Private session - access denied');
}
```

#### Level 3: Invite Codes (More Secure)

**Database setup:**
```sql
CREATE TABLE session_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
    invite_code TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by TEXT NOT NULL,
    max_uses INT DEFAULT -1,
    uses INT DEFAULT 0
);
```

**Android app - Share invite code:**
```kotlin
// Copy to clipboard or share
val inviteCode = "ABC123DEF456"
val sharingIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "Join my session: https://yourdomain.com/signallights/?invite=$inviteCode")
}
startActivity(sharingIntent)
```

**Web app - Accept invite:**
```javascript
// Check URL for invite code
const params = new URLSearchParams(window.location.search);
const inviteCode = params.get('invite');

if (inviteCode) {
    // Validate invite code and get session ID
    const { data: invite } = await supabaseClient
        .from('session_invites')
        .select('session_id')
        .eq('invite_code', inviteCode)
        .single();

    if (invite) {
        selectSession(invite.session_id);
    }
}
```

#### Level 4: Authentication (Enterprise)

For maximum security, require login:

```javascript
// Web app login
const { data, error } = await supabaseClient.auth.signInWithPassword({
    email: user@example.com,
    password: password
});

// Each user can only see their own sessions
const { data: sessions } = await supabaseClient
    .from('sessions')
    .select('*')
    .eq('creator_id', user.id);  // Only creator's sessions
```

### Recommendation

**For your use case (private testing):**
- Use **Level 2** (public/private sessions)
- Default all sessions to private
- Default web app to private mode
- Users explicitly set sessions to public if they want sharing

**Implementation:**

1. Add `access_type` to sessions table
2. Add validation in web app
3. Show clear message if session is private
4. Optional: Add invite codes later if needed

## Part 5: Complete Setup Guide

### Step 1: Prepare Android App Distribution

```bash
# Build signed APK
cd /Users/yahya/AndroidStudioProjects/SignalLights
./gradlew assembleRelease --stacktrace

# Find APK
ls app/build/outputs/apk/release/
```

### Step 2: Deploy Web App

#### GitHub Pages Option:
```bash
# Create gh-pages branch
git checkout --orphan gh-pages
git rm -rf .
# Copy web-display files
cp -r web-display/* .
git add .
git commit -m "Deploy web app"
git push origin gh-pages
```

Access at: `https://yourusername.github.io/repo-name/`

#### Or Self-Host:
```bash
# On your server
mkdir -p /var/www/signallights
cp web-display/index.html /var/www/signallights/
# Update Supabase URL in index.html
```

### Step 3: Add Security (Optional)

Add to Supabase sessions table:
```sql
ALTER TABLE sessions
ADD COLUMN access_type TEXT DEFAULT 'private';
```

Update web app to check access_type before displaying session.

### Step 4: Create Distribution Links

**Option A: Direct Download**
- Host APK at: `https://yourdomain.com/downloads/signallights.apk`
- Create HTML page with download button

**Option B: QR Code Distribution**
1. Generate QR code pointing to download page
2. Place QR in session overlay (Android app)
3. Display QR code in web app's session selection screen

### Step 5: Test End-to-End

1. Install APK on Android device
2. Open web app in browser on separate device
3. Create session on Android
4. Open same session on web
5. Press button on Android → should update on web in real-time
6. Verify 10-second auto-reset works on web

## Part 6: URL Schemes for Seamless Integration

### Web App → Android App Deep Link

**Generate QR code with:**
```
https://yourdomain.com/qr?session=550e8400-e29b-41d4-a716-446655440000
```

**This QR redirects to:**
```
signallights://session?id=550e8400-e29b-41d4-a716-446655440000
```

**Android receives and:**
1. Checks if app is installed
2. If yes: Opens overlay for that session
3. If no: Opens Play Store

### Android App → Web App Deep Link

**In overlay, add button:**
```kotlin
val openWebButton = Button(this)
openWebButton.text = "📱 Open Web"
openWebButton.setOnClickListener {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://yourdomain.com/?session=${currentSessionId}")
    }
    startActivity(intent)
}
```

**Web app detects session in URL:**
```javascript
const params = new URLSearchParams(window.location.search);
const sessionId = params.get('session');
if (sessionId) {
    // Auto-select session if it exists
    const session = sessions.find(s => s.id === sessionId);
    if (session) selectSession(session.id, session.title);
}
```

## Summary

| Aspect | Recommendation |
|--------|---|
| **Android Distribution** | GitHub Releases (testing) → Play Store (production) |
| **Web Distribution** | GitHub Pages (free) or self-hosted |
| **Linking** | QR codes + deep links |
| **Security** | Private sessions by default + invite codes (optional) |
| **Discovery** | Disable public indexing, use invitation-only model |

This ensures your app stays private while remaining easy to share with intended users.
