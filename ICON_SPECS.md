# StatusPulse App Icon Specifications

## Icon Design Overview

**Concept:** A clean, modern traffic light icon optimized for app launchers across all platforms

### Design Approach
- Bold, recognizable at small sizes
- Maintains clarity at 16x16px and up
- Works in both light and dark app drawer backgrounds
- Uses core color palette (Red/Yellow/Green)
- Slightly abstracted from real-world traffic light for modern feel

### Core Design Elements

#### Traffic Light Shape
- **Base Shape:** Rounded rectangle (vertical pill)
- **Corner Radius:** 20% of overall height
- **Aspect Ratio:** 1:1 (square for app icons)
- **Padding:** 16% on all sides (allows background rounded corners)
- **Stroke:** 2px, Dark Navy (#1A1A2E)
- **Background:** Optional rounded square (for app drawer consistency)

#### Status Lights (Circles)
Three vertically stacked circles:

**Red Light (Top)**
- **Color:** Pulse Red (#FF4D4D)
- **Size:** 28% of icon height
- **Shadow:** Inner highlight (2px, opacity 0.3)
- **Glow:** Outer glow (1px, rgba(255,77,77,0.4))

**Yellow Light (Middle)**
- **Color:** Pulse Yellow (#FFD700)
- **Size:** 28% of icon height
- **Shadow:** Inner highlight (2px, opacity 0.3)
- **Glow:** Outer glow (1px, rgba(255,215,0,0.4))

**Green Light (Bottom)**
- **Color:** Pulse Green (#4CAF50)
- **Size:** 28% of icon height
- **Shadow:** Inner highlight (2px, opacity 0.3)
- **Glow:** Outer glow (1px, rgba(76,175,80,0.4))

### Visual Details
- **Subtle depth:** Use inner shadows to create 3D bulb effect
- **Highlights:** Reflect light on top of each circle
- **Spacing:** Equal vertical spacing between lights (16% gaps)

---

## Platform-Specific Sizes

### iOS Sizes
| Context | Size | DPI | Filename |
|---------|------|-----|----------|
| App Store | 1024x1024px | 1x | AppIcon-1024.png |
| iPhone (Latest) | 180x180px | 3x | AppIcon-180.png |
| iPhone (Older) | 120x120px | 2x | AppIcon-120.png |
| iPad | 152x152px | 2x | AppIcon-152.png |
| iPad Pro | 167x167px | 2x | AppIcon-167.png |
| Spotlight | 120x120px | 2x | AppIcon-Spotlight-120.png |
| Settings | 58x58px | 2x | AppIcon-Settings-58.png |

### Android Sizes
| Context | Size | DPI | Filename |
|---------|------|-----|----------|
| Google Play | 512x512px | — | ic_launcher-512.png |
| xxxhdpi | 192x192px | 4x | ic_launcher-192.png |
| xxhdpi | 144x144px | 3x | ic_launcher-144.png |
| xhdpi | 96x96px | 2x | ic_launcher-96.png |
| hdpi | 72x72px | 1.5x | ic_launcher-72.png |
| mdpi | 48x48px | 1x | ic_launcher-48.png |
| Web | 512x512px | — | ic_launcher-512.png |

### Web/Other Sizes
| Context | Size | Filename |
|---------|------|----------|
| Favicon | 32x32px | favicon-32.png |
| Favicon (High DPI) | 64x64px | favicon-64.png |
| Apple Touch Icon | 180x180px | apple-touch-icon.png |
| Android Chrome | 192x192px | android-chrome-192.png |
| Android Chrome (Large) | 512x512px | android-chrome-512.png |

---

## Color Specifications

### Primary Icon Colors
```
Red:    #FF4D4D (RGB: 255, 77, 77)
Yellow: #FFD700 (RGB: 255, 215, 0)
Green:  #4CAF50 (RGB: 76, 175, 80)
Frame:  #1A1A2E (RGB: 26, 26, 46)
```

### Highlight Colors (Inner Shadows)
```
White with opacity: rgba(255, 255, 255, 0.3)
```

### Glow Colors (Outer Glow - Optional)
```
Red glow:    rgba(255, 77, 77, 0.4)
Yellow glow: rgba(255, 215, 0, 0.4)
Green glow:  rgba(76, 175, 80, 0.4)
```

---

## Background Variations

### Variation 1: Transparent Background
- Icon sits on transparent/transparent alpha layer
- Use for: App store, flexible placement
- Android: Launcher will add own background
- iOS: System applies adaptive background

### Variation 2: Rounded Square Background
- Light Gray (#F5F5F5) background
- 20% corner radius
- Padding: 16% on all sides
- Use for: Screenshots, marketing, web
- Better visual weight and shelf appeal

### Variation 3: Gradient Background (Optional)
- Subtle gradient from white to light gray
- Creates depth without being distracting
- Use for: Premium positioning, App Store hero
- Do NOT use bright colors

### Variation 4: White Background
- Solid white background
- Use for: Print, PDF, light backgrounds only

---

## Design Guidelines

### Proportions (at 1024x1024px)

```
┌──────────────────────────┐
│                          │
│    ┌────────────────┐    │
│    │   Frame (12%)  │    │
│    │                │    │
│    │  ●  Red        │    │
│    │                │    │
│    │  ●  Yellow     │    │
│    │                │    │
│    │  ●  Green      │    │
│    │                │    │
│    └────────────────┘    │
│                          │
└──────────────────────────┘
```

### Sizing Details (1024x1024px base)
- **Padding:** 160px on all sides
- **Icon Area:** 704x704px
- **Frame:** 550x630px (centered in icon area)
- **Frame Stroke:** 16px (2px at 1024px scale)
- **Light Diameter:** 196px
- **Light Spacing:** 112px between centers
- **Corner Radius:** 126px

### Grid System
Use 8px grid for consistency:
- Icon 1024px = 128 grid units
- Padding 160px = 20 grid units
- Frame stroke 16px = 2 grid units

---

## Style Details

### Lighting & Depth
1. **Inner Highlight:** Small highlight at top of each light
   - Opacity: 30%
   - Size: 6-8px diameter at 1024x1024px
   - Position: Top-left of circle

2. **Glow Effect:** Subtle outer glow
   - Opacity: 40%
   - Blur: 4px at 1024x1024px
   - Width: 2-3px stroke

3. **Frame Appearance:**
   - Flat design (no depth)
   - Clean, crisp stroke
   - Dark navy color maintains legibility

### Contrast Ratios
- Red on white: 4.5:1 ✓
- Yellow on white: 7:1 ✓
- Green on white: 5.3:1 ✓
- Frame on light background: 8:1 ✓

---

## Safe Zone (Minimum Clear Space)

For iOS and Android, define a safe zone around the icon core where important visual elements live:

```
┌────────────────────────┐
│                        │
│  ┌──────────────────┐  │
│  │ Safe Zone 80%    │  │
│  │ (Important icon  │  │
│  │  elements here)  │  │
│  └──────────────────┘  │
│                        │
│  Margin 20%            │
│                        │
└────────────────────────┘
```

- Safe Zone: 80% of icon (inner 80%)
- Margin: 20% of icon (outer edge)
- Keep all critical visual information in safe zone
- Allows for rounding/masking by OS

---

## Export Format Requirements

### iOS
- **Format:** PNG or SVG
- **Color Space:** sRGB
- **Background:** Transparent (PNG) or not used (SVG)
- **Files:** Provide 1x, 2x, 3x variants (or let tools scale from 1024px)

### Android
- **Format:** PNG or WebP
- **Color Space:** sRGB
- **Background:** Transparent
- **Files:** All DPI variants needed (mdpi through xxxhdpi)

### Web
- **Format:** SVG (preferred), PNG fallback
- **Color Space:** sRGB
- **Background:** Transparent
- **Metadata:** Include title and description in SVG

---

## Testing Checklist

- [ ] Icon recognizable at 16x16px
- [ ] Shapes distinct and clear at all sizes
- [ ] Colors match brand palette exactly
- [ ] No anti-aliasing artifacts visible
- [ ] Works on both light and dark backgrounds
- [ ] Safe zone respected (no elements cut off)
- [ ] All corners consistent roundness
- [ ] Spacing between lights looks balanced
- [ ] Glow effect doesn't distract at small sizes
- [ ] Highlight creates depth without overloading

---

## Common Mistakes (Avoid)

❌ Making icon too detailed (won't work at small sizes)
❌ Using different colors than brand palette
❌ Unequal spacing between lights
❌ Asymmetrical highlights
❌ Too much glow/shadow effect
❌ Mismatched corner radius
❌ Icons touching safe zone edge
❌ Different stroke widths
❌ Flat colors without any depth cue
❌ Oversized padding (leaves too much white space)

---

## Animation (Optional)

For app launcher or notification badge:

### Pulse Animation
```css
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 77, 77, 0.7); }
  70% { box-shadow: 0 0 0 16px rgba(255, 77, 77, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 77, 77, 0); }
}
```

### Light Glow Animation
- Cycle through which light is highlighted
- 3 second rotation (1 sec per light)
- Subtle brightening of current light

---

## Design File Specifications

### Source File (Illustrator/Figma)
- **Artboard:** 1024x1024px
- **Guides:** 8px grid enabled
- **Rulers:** Visible for alignment
- **Layers:** Well-organized by component
  - Frame
  - Red Light (with sub-layers: circle, highlight, glow)
  - Yellow Light (with sub-layers)
  - Green Light (with sub-layers)
  - Background (if included)

### Export Settings
- **iOS:** Scale from 1024px to 180, 120, 152, 167, 120, 58px
- **Android:** Scale from 1024px to 192, 144, 96, 72, 48px
- **Web:** Export 512, 256, 192, 128, 64, 32px variants

---

## Accessibility Notes

- **Contrast:** All elements meet WCAG AA standards
- **Color Alone:** Icon design doesn't rely only on colors for meaning
- **Shape Distinctiveness:** Traffic light shape is universally recognized
- **Scalability:** Clear at all required sizes

---

## Questions for Designer

When working with a designer:

1. Should highlights be matte or glossy appearance?
2. Glow effect: always visible or only at certain sizes?
3. Animation for app launcher preferred?
4. Platform-specific variations needed (e.g., iOS vs Android look)?
5. Should background color be added by platform (transparency) or built in?
6. Need animated version for splash screen?
7. Preferred export format priority: SVG or PNG?
8. Corner radius percentage confirmed for frame?
