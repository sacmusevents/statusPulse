# Signal Lights System

A real-time signal light system with Android overlay control and web display. Perfect for events!

## What It Does

- **Android App**: Overlay that sticks on top of other apps with 3 buttons (red, yellow, green)
- **Web Display**: Shows the current light state on a separate device
- **Real-time Sync**: Changes appear instantly on the web display via Pusher
- **Auto-reset**: Non-green lights automatically reset to green after 5 minutes

## System Architecture

```
Android Overlay App
       ↓ (button press)
      Pusher
       ↑ (real-time event)
Web Display
```

## Quick Start

1. **Set up Pusher**: Create free account at [pusher.com](https://pusher.com)
2. **Configure Web App**: Add your Pusher credentials to `web-display/index.html`
3. **Open Web Display**: Open `web-display/index.html` in a browser
4. **Build Android App**: Follow instructions in `SETUP_GUIDE.md`
5. **Grant Permissions**: Allow overlay permission when prompted
6. **Done!**: Click buttons on overlay, watch web display update

## Files

| File/Folder | Purpose |
|---|---|
| `web-display/index.html` | Web display app (single HTML file) |
| `android-app/` | Android source files |
| `SETUP_GUIDE.md` | Detailed setup instructions |

## Requirements

- **Web Display**: Any modern browser
- **Android App**: Android 6.0+ (API 24+), Android Studio

## Features

✅ Real-time communication via Pusher (free tier)
✅ No backend server needed
✅ Persistent overlay on Android
✅ Auto-reset timer for non-green lights
✅ Low latency (milliseconds)
✅ No credit card required

## Next Steps

1. Read `SETUP_GUIDE.md` for detailed instructions
2. Create Pusher account at [pusher.com](https://pusher.com)
3. Follow the setup steps for web and Android apps

---

**For questions, see SETUP_GUIDE.md troubleshooting section.**
