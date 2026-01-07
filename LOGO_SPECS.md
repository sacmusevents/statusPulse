# StatusPulse Logo Specifications

## Logo Design Overview

**Concept:** A modern, minimalist traffic light with a subtle pulse/heartbeat visual metaphor

### Core Visual Elements

#### 1. Traffic Light Frame
- **Shape:** Rounded rectangle (pill shape)
- **Aspect Ratio:** 3:4 (narrow, vertical, like a real traffic light)
- **Border Radius:** 12-16px on corners, 24-28px on sides
- **Stroke:** 2px, using Dark Navy (#1A1A2E)
- **Fill:** None (transparent or white background)
- **Dimensions:** 80px wide × 100px tall (reference)

#### 2. Color Circles (Lights)
Three stacked circles representing the status lights:

**Red (Top)**
- **Color:** Pulse Red (#FF4D4D)
- **Diameter:** 18-22px
- **Glow:** Subtle outer shadow (3px blur, rgba(255,77,77,0.3))
- **Position:** Top third of frame

**Yellow (Middle)**
- **Color:** Pulse Yellow (#FFD700)
- **Diameter:** 18-22px
- **Glow:** Subtle outer shadow (3px blur, rgba(255,215,0,0.3))
- **Position:** Center of frame

**Green (Bottom)**
- **Color:** Pulse Green (#4CAF50)
- **Diameter:** 18-22px
- **Glow:** Subtle outer shadow (3px blur, rgba(76,175,80,0.3))
- **Position:** Bottom third of frame

#### 3. Pulse Indicator (Optional Animation)
- **Style:** Subtle heartbeat lines emanating from the frame
- **Lines:** 2-3 curved lines extending outward
- **Animation:** Pulse at 1.5-2 second intervals
- **Opacity:** 60-80%, fading out as they extend
- **Color:** Use primary glow color or Pulse Blue

### Technical Specifications

#### File Formats
- **SVG** (scalable, recommended for web/digital)
- **PNG** (1024x1024px minimum, transparent background)
- **PDF** (for printing)
- **Adobe Illustrator** (.ai, .eps) - source files

#### SVG Attributes
```
<svg viewBox="0 0 80 100" width="80" height="100" xmlns="http://www.w3.org/2000/svg">
  <!-- Main frame -->
  <rect x="8" y="10" width="64" height="80" rx="12" ry="28"
    fill="none" stroke="#1A1A2E" stroke-width="2"/>

  <!-- Red light (top) -->
  <circle cx="40" cy="25" r="11" fill="#FF4D4D"
    filter="drop-shadow(0 0 6px rgba(255,77,77,0.3))"/>

  <!-- Yellow light (middle) -->
  <circle cx="40" cy="50" r="11" fill="#FFD700"
    filter="drop-shadow(0 0 6px rgba(255,215,0,0.3))"/>

  <!-- Green light (bottom) -->
  <circle cx="40" cy="75" r="11" fill="#4CAF50"
    filter="drop-shadow(0 0 6px rgba(76,175,80,0.3))"/>

  <!-- Optional pulse animation -->
  <g opacity="0.7" style="animation: pulse 2s infinite;">
    <circle cx="40" cy="50" r="30" fill="none" stroke="#2196F3" stroke-width="1"/>
    <circle cx="40" cy="50" r="40" fill="none" stroke="#2196F3" stroke-width="1" opacity="0.5"/>
  </g>
</svg>

<style>
  @keyframes pulse {
    0% { opacity: 0.7; r: 24px; }
    50% { opacity: 0.3; r: 32px; }
    100% { opacity: 0; r: 40px; }
  }
</style>
```

---

## Logo Variations

### 1. Primary Logo (Full Color Horizontal)
```
[Traffic Light Icon] StatusPulse
```
- Icon: 48px × 60px
- Text: "StatusPulse" in Inter/Roboto Bold, 28px
- Space between: 16px
- Use for: Main branding, websites, header, app stores

### 2. Vertical Logo
```
[Traffic Light Icon]
   StatusPulse
```
- Icon: 64px × 80px (centered)
- Text: "StatusPulse" in Inter/Roboto Bold, 24px (centered below)
- Use for: Limited horizontal space, vertical layouts, app icons

### 3. Icon Only (App Icon)
```
[Traffic Light Icon]
```
- Icon: 512x512px (for digital, scaled down for app icons)
- No text
- Transparent background
- Use for: App launcher icon, favicons, social media avatars

### 4. Compact Logo (Icon + Text Horizontal)
```
[Icon] StatusPulse
```
- Icon: 32px × 40px
- Text: "StatusPulse" in Inter/Roboto Semibold, 16px
- Space between: 12px
- Use for: Navigation bars, sidebar logos, small headers

---

## Color Variations

### Primary (Full Color)
- Red: #FF4D4D
- Yellow: #FFD700
- Green: #4CAF50
- Frame: Dark Navy #1A1A2E

**Use:** On light backgrounds, primary digital applications

### Monochrome Dark (All Navy)
- All elements: Dark Navy #1A1A2E

**Use:** On light/white backgrounds when color not appropriate

### Monochrome Light (All White)
- All elements: White #FFFFFF

**Use:** On dark backgrounds, colored backgrounds

### Single Color (Pulse Blue)
- All elements: Pulse Blue #2196F3

**Use:** Secondary applications, when color coding not needed

---

## Clear Space & Minimum Size

### Clear Space
Minimum 16px of clear space around all sides of the logo. Never crowd the logo with other elements.

```
╔════════════════════════════════╗
║                                ║
║    [  Logo Area  ]             ║
║                                ║
║  No text or elements           ║
║  within this space             ║
╚════════════════════════════════╝
```

### Minimum Dimensions
- **Digital:** Never smaller than 32px wide
- **Print:** Never smaller than 1 inch (96px) wide
- **App Icon:** 48x48px minimum (scales up to 512x512px)

---

## Common Mistakes (Avoid)

❌ Changing the color order or colors
❌ Rotating or tilting the traffic light
❌ Removing the frame
❌ Stretching or distorting proportions
❌ Adding drop shadows or effects not specified
❌ Using gradients on the lights
❌ Changing fonts when using text version
❌ Overlapping with other logos nearby
❌ Using on backgrounds with insufficient contrast

---

## Usage Examples

### Website Header
- Logo size: 48px × 60px
- Position: Top left of navigation
- Colors: Full color on white background

### App Icon
- Logo size: App-specific (see Icon Specs)
- Position: Centered
- Colors: Full color with optional glow
- Background: Transparent or light gray

### Documentation/Help
- Logo size: 32px × 40px (inline with text)
- Colors: Full color
- Position: Beside "StatusPulse" text when space allows

### Social Media
- Logo size: 512x512px
- Position: Centered
- Colors: Full color
- Background: Light gray (#F5F5F5) or white

---

## Animation Guidelines (Optional)

### Pulse Animation
- **Duration:** 2 seconds
- **Timing:** Ease in-out (smooth)
- **Effect:** Subtle expanding rings from center
- **Frequency:** Continuous loop

### Hover State
- **Duration:** 0.3 seconds
- **Effect:** Slight glow increase or scale (1.05x)
- **Use for:** Interactive logo (e.g., clickable header)

### Active State
- **Duration:** 0.3 seconds
- **Effect:** One of the lights brightens or pulses
- **Use for:** App switcher, active status indicator

---

## Design Files

**Get design files from:**
- Adobe Illustrator: `StatusPulse_Logo.ai`
- SVG (Web): `status-pulse-logo.svg`
- PNG (High Res): `status-pulse-logo-1024.png`
- PNG (Web): `status-pulse-logo-256.png`, `status-pulse-logo-128.png`

---

## Questions for Designer

When working with a designer, confirm:

1. Should pulse animation be included in animated versions?
2. Preferred stroke width for the frame (2px recommended)?
3. Should outer glow be applied to lights? (Recommended: yes)
4. Color exact matches confirmed with design tool?
5. SVG file should include animation? (Yes for web)
6. Backup static SVG without animation needed? (Yes)
