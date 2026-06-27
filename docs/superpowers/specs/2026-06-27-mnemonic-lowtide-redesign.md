# Mnemonic Lowtide — Full Visual Redesign (Plan C)

**Date:** 2026-06-27  
**Branch:** neo-1.21.1  
**Status:** Approved, ready for implementation

## Goal

Rework `MnemonicLowtideChamberEffects` so the in-game result matches the concept art: a vast dim cranial cavern with a near-black cerebrospinal lake, atmospheric horizon haze, distant Gothic ruin silhouettes, artery bridges embedded in mist, and a skull-vault ceiling that reads as deep and organic rather than a boxy texture wall.

Emotional target: melancholy revelation. Mid-progression state between will_default and Silent Archon/Qliphoth.

## Root Causes of Current Failure

1. Fluid shader `softLine(fract(...))` shimmer creates periodic parking-lot stripe artifacts.
2. Ground ring quads drawn on fluid use orange-amber color `(190,111,51)` — read as bright bands.
3. No horizon fog/haze pass — hard box seam visible where fluid plane meets skybox walls.
4. Bridge color `(182,46,28)` at alpha 118 reads as a foreground UI band, not distant architecture.
5. Outpost billboard color `(13,10,10)` — invisible against dark sky, no recognizable silhouette.
6. Vault bands use straight horizontal strokes with alpha 18–76 and warm orange-brown colors — reads as flat wall.
7. Fluid vertex color `(96,55,44)` is too warm/amber; should be near-black.
8. Fog override color too warm: `(0.38,0.21,0.14)`.

## Changes

### 1. Fluid Shader (`mnemonic_lowtide_fluid.fsh` + `.vsh`)

- **Remove** `softLine`/`fract` shimmer — replace with FBM layered sines at incommensurate frequencies (no repeating period).
- `deepFluid` base: `vec3(0.008, 0.002, 0.004)` (near-black).
- Gold tint replaced with subtle dark-red ripple glint only: `vec3(0.45, 0.06, 0.03)`.
- Add UV-distance horizon fade: `smoothstep` from UV center so fluid alpha drops to 0 at the quad edges — eliminates hard rim.
- Vertex color in Java: `(12, 4, 6)` instead of `(96, 55, 44)`.

### 2. Ground Ring Quads (`renderMnemonicLowtideBlackFluid`)

- Ring color: `(190,111,51)` → `(38,10,8)`, alpha 12–30.
- Sigil/reflection glints: `(220,167,91)` → `(90,55,20)`, alpha 10–18.

### 3. New Horizon Haze Pass

New method `renderMnemonicLowtideHorizonHaze` called in `renderAfterNebula` (after fluid, before outposts).

- 5 stacked wide quads at fluid/wall boundary, full width, alpha 8–22, dark maroon→near-black.
- 1 shallow "fog floor" plane just above fluid surface, alpha 18 fading up to 0.
- Hides hard seam, creates atmospheric depth band matching concept art horizon.

### 4. Skull Vault (`renderMnemonicLowtideSkullVault`)

Keep multi-face approach. Changes:

- **Color:** Bands near zenith → pale bone `(195,172,140)`. Bands near horizon → deep maroon `(75,18,12)`. Color lerps by `bandT`.
- **Alpha:** Drop range to `6–38` (was 18–76).
- **Curvature:** 24 segments (was 18). Add arc bow offset `Mth.sin(t*PI) * arc * 0.45F` so strokes arch concave toward face center rather than running straight.
- **Top face extra pass:** 12 thin branching vascular strokes, dark crimson `(90,22,16)`, alpha 15–28, width `0.0018F * skyDistance`.

### 5. Bridges (`renderMnemonicLowtideArteryBridges`)

- Deck color: `(182,46,28)` → `(88,48,28)`.
- `bridgeHalfWidth` multiplier: `0.0105F` → `0.006F`.
- `startDistance`: `0.54F` → `0.68F`; `endDistance`: `1.06F` → `1.30F`.
- Max alpha: 118 → 48.
- Rail gold: `(228,160,66)` → `(155,105,38)`, alpha/4 (was alpha/2).

### 6. Outpost Silhouettes (`renderMnemonicLowtideOutposts`)

Replace 3-rectangle billboards with 5-quad Gothic spire profiles per outpost:
- Wide base platform quad.
- Tapered mid-body quad (narrower at top).
- Narrow shaft quad.
- Pointed spire tip (triangle fan approximated as thin quad).
- Optional small flanking turret quad offset sideways.

Color: `(42,26,18)` to `(68,44,28)`, alpha 28–68.  
Distance: push to `0.72F–1.25F * skyDistance`.

### 7. Neural Shared Layers

Override shared neural pass contribution in `MnemonicLowtideChamberEffects`. If the base class exposes an alpha multiplier, pass `0.35F`. Otherwise suppress the pass entirely for this theme — Lowtide is still/melancholy, not electric.

### 8. Minor Touches

- **Nerve roots:** Count 34→28, color `(190,168,128)`.
- **Memory fragments:** Count 26→14, halfWidth capped at `0.016F`, color `(182,160,115)`.
- **Fog override:** `getBrightnessDependentFogColor` base → `(0.22,0.09,0.07)`.
- **Synapse flashes and vein strands:** Unchanged.

## Files Touched

| File | Change |
|------|--------|
| `MnemonicLowtideChamberEffects.java` | All render methods reworked + new haze pass |
| `mnemonic_lowtide_fluid.fsh` | Shader rewrite (shimmer, color, horizon fade) |
| `mnemonic_lowtide_fluid.vsh` | Minor: ensure wave height stays subtle |
| No new files required | Haze pass is a new method in existing class |

## Success Criteria

- No parallel stripe artifacts visible on the fluid surface at any view angle.
- Hard horizontal seam at fluid/wall boundary is not visible — replaced by dark atmospheric haze band.
- Bridges read as distant architecture embedded in mist, not foreground UI bands.
- Outpost Gothic spire silhouettes are legible at distance against the dark sky.
- Skull vault reads as a receding organic overhead structure, not a flat brown wall.
- Overall palette: near-black base, dark crimson reds, aged bone/ivory highlights, no orange.
- Build passes: `./gradlew.bat build` and `./gradlew.bat test` green.
