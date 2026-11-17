# Living Hematic HUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the rejected hematic HUD chassis with a living iron-and-tendril binding, pouring medical Temper vials, a slow darkening heartbeat and a Morphling jar slot, all fitted per visible part and per side.

**Architecture:** Pure-Java geometry (`BloodHudLayout`, `HudAssembly`, `HudBinding`, `HudHeartbeat`, `TemperVialArt`, `TemperStream`) builds everything from the parts' alpha masks and is unit-tested without Minecraft. A client renderer caches the binding and heartbeat bands as `DynamicTexture`s per (assembly, degree) and draws vials and streams as fills. The binding is generated at runtime rather than baked, because textures never flip and every reserve outline must fit each side separately. Generating at runtime gets all combinations and both sides from one algorithm.

**Tech Stack:** NeoForge 1.21.1, Java 21, JUnit 5 (`./gradlew.bat test`), PIL for the one baked jar sprite, the existing `TemperRackReview` client harness for screenshots.

**Spec:** `docs/superpowers/specs/2026-11-12-living-hematic-hud-design.md`

## Global Constraints

- Existing texture files stay byte-identical: `vessel_back`, `base_degree_0..8`, `pomes_*`, `fill_apotheos_*`, `halo_apotheos`, `gourd_*`, `curved_horn_*`, `hemorath_rib_*`.
- No texture is ever drawn flipped. Only layout, binding, vials, streams and jar position mirror.
- The vessel draws 1:1 at 64×128 art px; `OVERLAY_SCALE`/`BloodHudLayout.SCALE` stays 0.6.
- Binding: weight 6 px (half 3), contour radius 4, struts cross every 18 px of arc, collars every ~44 px, a curl at every third collar where red. Iron ramp `#0A0A0A #141414 #1E1E1F #2A2A2B #343335 #464548 #605F63`; vein ramp `#350107 #4B0000 #7A0610 #A80E1C #D6343E`; recessed `#140A0A`/`#1A0C0E`; outline `#040404`.
- Heartbeat: lub-dub, 25 bpm at full blood rising to 45 bpm empty, darkens toward `#280006` (Apotheos `#461600`) at peak mix 0.45 × (0.5 + 0.5 × bloodRatio). Never brightens.
- Vials: 8, school enum order, schools 0-3 on the outward flank, 4-7 inward, tipped 50°, about 7×15 px. Liquid settles to the lowest screen pixels. Colours from `TemperPhials.schoolColour`.
- Trickle only when the vessel is shown and the vial is neither empty nor dry. 1 px stream at alpha 0.55, 3 travelling beads, 7-row fading plume.
- No gourd-to-vessel tube. No vanilla particles. No `Component.literal` English. Time from `AnimationTime`.
- HUD drawing stays in the `hemomancy:bloodvolume` layer; no `RenderGuiEvent` handlers.
- Commits: the owner commits. Leave all work in the working tree (owner preference); the commit steps below are replaced by "leave in working tree".
- Removed files move to the untracked `removed_assets/` mirror path, never plain-deleted.

## Review Focus

1. **The HUD hidden behind a full-screen menu, or `hideGui`.** Nothing renders and no textures leak. The assembly cache must survive a resource reload (`clearCaches`) without a crash on the next frame. Owned by Task 6 (`clearCaches` releases every texture; a test asserts the key resets).
2. **Degree or path changes mid-session.** Going D6→D7, D7→Apotheos or Silentium must rebuild the binding paint; a stale texture would show old red. Owned by Task 6 (cache key includes degree and Apotheos flag) and Task 2 (`paint` test across degrees).
3. **A school seen with exactly 0 held, or a cap of 0.** No trickle, no divide by zero, and the vial shows empty (a cap of 0 currently yields fraction 0). Owned by Task 4 (`TemperStream.flows` test).
4. **Tiny viewports and GUI scale 4.** The assembly is larger than before (jar, binding margin). `place` must still fit it above the hotbar in every corner. Owned by Task 3 (the existing corner-fit test extended to the jar-and-everything layout at 320×180).
5. **Morphling equipped with `LEGACY_ICON`, or an item without HUD visuals.** It must sit inside the jar, not at the old vessel-wall spot. Owned by Task 6 (renders through `renderInJar` for every mode except `OFF`) and Task 7 (review scene).

---

### Task 1: Heartbeat math

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/bloodhud/HudHeartbeat.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/screen/overlay/bloodhud/HudHeartbeatTest.java`

**Interfaces:**
- Produces: `HudHeartbeat.bpm(float bloodRatio): float`, `stroke(float phase): float` (0..1), `darkness(float seconds, float bloodRatio, float delayFraction): float`, `darken(int rgb, float amount, boolean apotheos): int`, constants `FULL_BPM=25`, `EMPTY_BPM=45`, `PEAK=0.45f`, `MAX_DELAY=0.3f`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void beatsSlowlyAndQuickensAsBloodRuns() {
    assertEquals(25f, HudHeartbeat.bpm(1f), 1e-4); assertEquals(45f, HudHeartbeat.bpm(0f), 1e-4);
    assertEquals(35f, HudHeartbeat.bpm(0.5f), 1e-4); assertEquals(25f, HudHeartbeat.bpm(2f), 1e-4);
}
@Test void lubDubThenRest() {
    assertEquals(1f, HudHeartbeat.stroke(0.06f), 1e-3);
    float dub = HudHeartbeat.stroke(0.26f); assertTrue(dub > 0.5f && dub < 0.7f);
    assertEquals(0f, HudHeartbeat.stroke(0.6f), 1e-6);
}
@Test void darkeningNeverBrightensAndPeaksAtTheCap() {
    float max = 0; for (int i = 0; i < 2400; i++) max = Math.max(max, HudHeartbeat.darkness(i / 1000f, 1f, 0f));
    assertEquals(0.45f, max, 0.01f);
    int lit = 0xD6343E, dark = HudHeartbeat.darken(lit, 0.45f, false);
    for (int s = 0; s < 24; s += 8) assertTrue(((dark >> s) & 0xFF) <= ((lit >> s) & 0xFF));
    assertTrue(HudHeartbeat.darkness(0.06f * 60 / 45f, 0f, 0f) < 0.26f, "fainter near empty");
}
```

- [ ] **Step 2: Run** `./gradlew.bat test --tests "*HudHeartbeatTest"`. Expected: compilation FAIL.
- [ ] **Step 3: Implement `HudHeartbeat`.** `stroke(p)` = `min(1, pl(0.06,0.09)^1.5 + 0.6·pl(0.26,0.08)^1.5)` with `pl(c,w)=max(0,1-|p-c|/w)`. `darkness` = `stroke(frac((seconds - delay·MAX_DELAY·period)/period)) · PEAK · (0.5+0.5·clamp(bloodRatio))`, with period `60/bpm`. `darken` mixes each channel toward `0x280006`, or `0x461600` for Apotheos.
- [ ] **Step 4: Run** the same command. Expected: PASS.
- [ ] **Step 5:** Leave in the working tree.

### Task 2: Silhouette and binding generator

**Files:**
- Create: `.../overlay/bloodhud/HudMask.java`, `HudSilhouette.java`, `HudBinding.java`
- Test: `src/test/java/.../overlay/bloodhud/HudBindingTest.java`, plus `src/test/java/.../overlay/bloodhud/HudMasksFixture.java` (loads real PNG alphas through `RepoPaths.ASSETS` + `ImageIO`)

**Interfaces:**
- Produces:
  - `record HudMask(int width, int height, boolean[] solid)` with `at(int x, int y)` and `static HudMask fromArgb(int w, int h, int[] argb, int minAlpha)` and `static HudMask union(HudMask a, HudMask b)` (same size).
  - `record HudSilhouette(int width, int height, boolean[] solid, boolean[] graft)` with `static HudSilhouette of(int w, int h)`, `stamp(HudMask m, int x, int y)`, `fillPolygon(int[][] points)` (marks graft cells that aren't already solid), `fillRect(int x, int y, int w, int h)` (spine: solid and graft).
  - `final class HudBinding` with `static HudBinding build(HudSilhouette s)`, `int width()`, `int height()`, `int[] paint(int degree, boolean apotheos)` (ARGB, 0 = clear), `int[] beatBand(int degree, int band, int bands)` (ARGB opaque on revealed red pixels whose delay falls in the band, else 0), `int seatX(int row, boolean fromLeft)` (first column on that row inside the contour radius, scanning from that edge; -1 if none), `int collarCount()`, `float length()`.
  - Constants `HALF = 3`, `RADIUS = 4`, `STRUT_PERIOD = 18`, `COLLAR_SPACING = 44`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void bindingRingsARectangleAndLeavesItsInsideClear() {
    var s = HudSilhouette.of(80, 80); s.fillRect(30, 20, 20, 40); var b = HudBinding.build(s);
    int[] p = b.paint(7, false);
    assertEquals(0, p[40 * 80 + 40], "inside of a non-graft solid stays clear");
    assertNotEquals(0, p[40 * 80 + 30 - 4], "a pixel 4 px outside the edge is binding");
    assertEquals(0, p[40 * 80 + 30 - 9], "nothing past the binding's outer edge");
}
@Test void calmLatticeSpacing() { /* 120×120 rect silhouette: collarCount == round(length/44) ± 1 */ }
@Test void redClimbsByDegreeFromTheBase() {
    // vessel silhouette from HudMasksFixture.vessel(): count pixels whose colour is in the vein ramp for degree 0..7
    // assert counts non-decreasing, degree 0 == 0, degree 7 == every red-capable pixel, and at degree 3
    // the mean y of red pixels is below the binding's vertical midpoint
}
@Test void graftKeepsTheDipAboveTheReserveShoulder() {
    // vessel at (8,8), gourd at (78,74), graft polygon as BloodHudLayout.graftPolygon (Task 3 values, unmirrored):
    // the pixel midway between the vessel's right edge and the gourd's left edge, 10 rows above the gourd top, is clear
}
@Test void asymmetricReserveIsFittedPerSideNotMirrored() {
    // horn mask stamped at x and at mirrored x (unflipped texture): paint(left) mirrored != paint(right)
}
@Test void beatBandsPartitionTheRevealedRed() { /* union of beatBand(7, i, 8) over i == revealed red pixel set, bands disjoint */ }
@Test void seatsFollowTheOutline() { /* gourd alone: seatX on a lobe row < seatX on the waist row (from left) */ }
```

- [ ] **Step 2: Run** `./gradlew.bat test --tests "*HudBindingTest"`. Expected: compilation FAIL.
- [ ] **Step 3: Implement.** This is the mockup algorithm (`docs/superpowers/mockups/living-hud/states-fit.html`, functions `distanceField`, `tracePath`, `build`), ported to Java:
  1. Two-pass 8-neighbour chamfer distance (1, √2), run forward, backward and forward.
  2. Trace the Moore-neighbour contour of `dist ≤ RADIUS`, starting at the first cell in row-major order with search start `(d+5)%8`.
  3. Smooth with a ±4 cyclic box average, then resample every 0.35 px with arc position `s`.
  4. Pick the outward normal by sampling `dist` 2 px out.
  5. Rasterise offsets `o ∈ [-HALF-0.6, HALF+0.6]` step 0.3, with `u = o/HALF`: past |u|>1 is outline (priority 0); on a strut (`|u ∓ 0.58·sin(2πs/18)| < 0.72/HALF`, front strut by `cos > 0`) is iron with `ramp(0.42 + lit - 0.22·d)` and `lit = (-nx-ny)·0.12` (priority 2); otherwise the pixel is red-capable with vein shade `0.2 + 0.65·n - 0.25·u`, where `n` is a cell hash from `floor(s/9)`.
  6. Lay a collar every `length/round(length/44)` with the 12-pixel diamond pattern from the mockup (priority 3); its stud is the 2×2 at the centre.
  7. Curl at every third collar, as in the mockup.
  8. Graft cells: a 16-px diamond lattice (`(x±y) mod 16 < 1.5` is iron), red-capable only within 2.6 px of a strut and where `hash(x>>2,y>>2) > 0.25`; otherwise recessed.

  Store per pixel: base colour, red-capable flag, height fraction (0 at the lowest binding row, 1 at the top), and beat delay (`s/length` on the contour, `(maxY-y)/(maxY-minY)` in the graft). For `paint`, red-capable pixels show their vein colour if `height ≤ degree/7` (degree ≥ 7 shows all) and recessed otherwise. Apotheos (`apotheos`) maps the vein ramp to `#593000 #7E2500 #A04A08 #C8701A #E89A3A`. Studs are grey `#46464A` until revealed, then `#E2283A`. Keep `dist` for `seatX`.
- [ ] **Step 4: Run** the same command. Expected: PASS.
- [ ] **Step 5:** Leave in the working tree.

### Task 3: Layout geometry

**Files:**
- Rewrite: `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BloodHudLayout.java`
- Rewrite: `src/test/java/com/vincenthuto/hemomancy/client/screen/overlay/BloodHudLayoutTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `record BloodHudLayout(boolean blood, Reserve reserve, boolean temper, boolean jar, boolean mirrored)`:
  - `enum Reserve { NONE(0), GOURD(64), HORN(48), RIB(64); int height(); }` and `record Rect(int x, int y, int w, int h)`.
  - `record VialSeat(int school, int mouthX, int mouthY, boolean pointsRight, int entryX, int entryY, int fitRow)`. `entryX = -1` means no stream. `fitRow ≥ 0` means the mouth is resolved later onto the binding at that row.
  - `parts(): int` (bit 1 blood, 2 reserve, 4 temper, 8 jar), `empty()`, `boolean gourd()` (reserve ≠ NONE).
  - `width()`, `height()`, `vessel(): Rect`, `reserveRect(): Rect`, `jarRect(): Rect`, `spine(): Rect` (null when absent).
  - `graftPolygons(): List<int[][]>`, `vials(): List<VialSeat>`, `degreeLabelY()`, `volumeLabelY()`, `reserveLabelY()`, `jarMeterX()`, `jarMeterY()`, `key(): String`.
  - `place(int screenW, int screenH, boolean left, boolean top, boolean statusIcons): Placement`, with `Placement` unchanged. `SCALE = 0.6F`, `MARGIN = 8`, `LABEL_BAND = 16`.
- All coordinates are art px, unmirrored = HUD on the left with the inward side to the right. With `vx, vy` the vessel origin:
  - Outward vials: mouths `(vx+6, vy+36+20i)`; inward vials `(vx+57, same)`; entries `(vx+23 | vx+40, row+8)`.
  - Reserve: `(vx+70, vy+130-h)`. Graft polygon: `[[vx+61, ry+22], [rx+4, ry+26], [rx+5, ry+h-2], [vx+46, vy+127]]`.
  - Jar: 28×40 at `(vx+72, vy+12)`. Jar graft: `[[vx+60, jy+12], [jx+3, jy+14], [jx+3, jy+34], [vx+60, jy+38]]`.
  - No vessel: the reserve is centred, vials fit rows `ry+6+14i` (horn `ry+4+12i`), the jar sits 46 px above the reserve, and there is no stream.
  - Vials only: spine `(cx-3, top, 7, 62)`, mouths `cx∓4` at `top+8+16i`, jar 44 px above the spine.
  - Translate so the union of rects, vial boxes (mouth ± 14) and `MARGIN` starts at 0, then add `LABEL_BAND` when the vessel or reserve shows. `mirrored` maps x → `width-x-w` for rects, `width-1-x` for points, and flips `pointsRight`.

- [ ] **Step 1: Write the failing tests** (replace the old file entirely; the old ampoule and art tests go):
  - `mirroringMovesGeometryButKeepsSize`: for every non-empty part combination and reserve, the mirrored layout has the same width/height, `vessel().x == width - x - w` and every vial's `pointsRight` flipped.
  - `reserveAndJarSitOnTheInwardFlank`: unmirrored `reserveRect().x > vessel().x + 32`, and `jarRect()` bottom + 12 ≤ `reserveRect().y`.
  - `vialsNeverCoverTheBloodWindow`: vial boxes (mouth ± 14) avoid `vessel` columns 24..39.
  - `streamsOnlyWithTheVessel`: `entryX ≥ 0` iff `blood`.
  - `fitRowsOnlyWithoutTheVessel`: `fitRow ≥ 0` iff `!blood && reserve != NONE`.
  - `everyCornerFits`: the old corner test over all 16 parts × 4 reserves at 320×180 and 320×360.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*BloodHudLayoutTest"`. Expected: FAIL.
- [ ] **Step 3: Implement** with the values above.
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5:** Leave in the working tree.

### Task 4: Vials, streams and the assembly

**Files:**
- Create: `.../overlay/bloodhud/TemperVialArt.java`, `TemperStream.java`, `HudAssembly.java`, `HudMasks.java`
- Test: `src/test/java/.../overlay/bloodhud/TemperVialArtTest.java`, `HudAssemblyTest.java`

**Interfaces:**
- Consumes: `BloodHudLayout`, `HudSilhouette`, `HudBinding`, `HudMask`.
- Produces:
  - `record HudMasks(HudMask vessel, HudMask gourd, HudMask horn, HudMask rib, HudMask jar, HudMask vesselWindow)`. `vessel` is the `vessel_back` ∪ `base_degree_7` alpha; `vesselWindow` is back-without-frame (alpha <40).
  - `TemperVialArt.sprite(boolean pointsRight): VialSprite` with `record VialSprite(int[] dx, int[] dy, int[] argb, int[] liquidDx, int[] liquidDy)`. Liquid is sorted lowest screen row first. `fillCount(VialSprite, float fraction): int` = `round(n·clamp(fraction))`. `socket(boolean pointsRight)` gives the healed-socket pixels in the same form with no liquid.
  - `TemperStream.flows(boolean vessel, float held, float cap, boolean dry): boolean` and `TemperStream.path(VialSeat seat, int surfaceY): int[][]` (quadratic arc from the mouth to the entry, then a vertical drop to `surfaceY-1`).
  - `HudAssembly.build(BloodHudLayout, HudMasks): HudAssembly` with `layout()`, `binding()`, `vials(): List<VialSeat>` (fit rows resolved through `binding.seatX(row, outward)`, mouth moved 4 px back along the tilt), `schoolAt(float x, float y): int` (sprite bounding boxes), `vesselSurfaceY(double ratio): int`.
- Vial sprite: the analytic sampler from the mockup's `vialPixel`, with the mouth at the origin and body direction `(∓sin50°, -cos50°)`:
  - lip `w<1, |u|<1.1`
  - cap `w<3, |u|<2.4`
  - neck `w<4, |u|<1.7`
  - body `w<15, |u|<3.5`, with glass rim `|u|>2.5 ∨ w>14`, label `11<w<13 ∧ u>-0.6`, inner otherwise
  - palette lip `#787C84`, cap `#B0B4BC`/`#565A62`, neck `#3C343E`, glass `#2C222C`, label `#D8CCB2`, empty `#3A323E` alpha 0.85

- [ ] **Step 1: Write the failing tests:**
  - `liquidSettlesLowest`: for both orientations the first liquid pixel's dy ≥ the last one's.
  - `fillCountsAreProportional`: 0 → 0, 1 → all, 0.5 → about half.
  - `mouthPointsInward`: for `pointsRight`, every body pixel has dx ≤ 1.
  - `streamsNeedTheVesselAndTemper`: `flows(false, 5, 10, false)` false; `flows(true, 0, 10, false)` false; `flows(true, 5, 0, false)` false; `flows(true, 5, 10, true)` false; `flows(true, 5, 10, false)` true.
  - `streamEndsAtTheSurfaceInsideTheGlass`: the full layout's school-0 path ends at `(entryX, surface-1)` and every vertical step is inside `vesselWindow`.
  - `fittedVialsSitOnTheBinding` (`HudAssemblyTest`, real masks): reserve + temper gives every resolved mouth within 6 px of `seatX` on its row, and `schoolAt` at each sprite centre returns that school.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*TemperVialArtTest" --tests "*HudAssemblyTest"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run.** Expected: PASS.
- [ ] **Step 5:** Leave in the working tree.

### Task 5: Morphling jar art

**Files:**
- Create: `tools/gui/living_hud_art.py` (local tool), `src/main/resources/assets/hemomancy/textures/gui/blood_hud/morphling_jar_back.png`, `morphling_jar_front.png` (28×40)
- Test: add `jarArtHasAClearWindow` to `HudAssemblyTest`

- [ ] **Step 1: Write the failing test.** Both PNGs are 28×40; `front` alpha < 128 over the inner window (5,10)-(22,35) except a 1-px glint column; `back` is opaque over that window.
- [ ] **Step 2: Run.** Expected: FAIL (missing file).
- [ ] **Step 3: Author the jar in the generator** as character grids:
  - The shape follows `textures/item/morphling_jar.png`: a squat glass jar with a lid band.
  - Lid and base are hematic iron in the binding ramp, with a red seal stud.
  - The back is dark tinted glass `#1C1418`.
  - The front is rim plus highlights only. No copper or brass.
  - Run `python tools/gui/living_hud_art.py`.
- [ ] **Step 4: Run** the test. Expected: PASS.
- [ ] **Step 5:** Leave in the working tree.

### Task 6: Client renderer and integration

**Files:**
- Create: `.../overlay/bloodhud/LivingHudRenderer.java`, `HudMasksLoader.java`
- Modify: `BloodVolumeOverlay.java`, `TemperMarksOverlay.java`, `EquippedMorphlingOverlay.java`, `EquippedMorphlingOverlayPlacement.java`, `src/test/.../EquippedMorphlingOverlayTest.java`, `src/gameTest/.../TemperRackReview.java` (API callers)
- Move to `removed_assets/`: `tools/gui/hematic_hud_art.py`, `tools/gui/temper_rack_art.py`, `textures/gui/temper_overlay/phial_*.png` (keep `sigil*.png`)

**Interfaces:**
- Consumes: everything above.
- Produces:
  - `BloodVolumeOverlay.layout(Player): BloodHudLayout` (jar = morphling equipped ∧ mode ≠ OFF; mirrored = corner 1 or 3).
  - `BloodVolumeOverlay.assembly(Player): HudAssembly` (cached by `layout.key()`).
  - `placement(...)` unchanged.
  - `EquippedMorphlingOverlay.renderInJar(GuiGraphics, ItemStack, float centreX, float centreY, float size, boolean mirror, float seconds)` and `renderBondMeter` at layout jar-meter coordinates.
  - `TemperMarksOverlay.phials(Player)` becomes package-visible for the renderer. Its tooltip uses `assembly.schoolAt`.

- [ ] **Step 1: Write the failing test** `EquippedMorphlingOverlayTest.jarSpriteFitsInsideTheWindow`: the scale for size 18 makes the 32-px sprite ≤ 18 px. Remove tests for deleted vessel-wall helpers.
- [ ] **Step 2: Implement.** Draw order inside the pose (translate + scale, never a negative scale):
  1. Binding paint texture.
  2. Eight beat-band textures, each with `setShaderColor(1,1,1, darkness(seconds, ratio, (i+0.5)/8))`.
  3. Halos (Apotheos `halo_apotheos` at `(vx-6, vy+1)` 76×126; open-reserve halo as today).
  4. Vessel back.
  5. Fill: `vesselFillMask` sampled 1:1. `VESSEL_TOP=27`, `VESSEL_H=87`. Meniscus, bubbles and veins use mask row spans instead of `BloodVesselShape`, which is deleted if unused. `POME_POINTS` are scaled by (64/76, 128/126).
  6. Degree frame.
  7. Vessel red beat bands: red pixels of `base_degree_N`, where red means alpha > 150, r > 80, r > 1.5g and r > 1.4b. Banded by `(127-y)/127` into 8 cached textures and darkened the same way.
  8. Pomes.
  9. Reserve (existing methods, positioned by `reserveRect`).
  10. Jar back, then the morphling (`renderInJar`), then the jar front.
  11. Streams, with `fill` per path pixel, beads and plume.
  12. Vials, drawn as fills of cached sprites; a full vial gets a +12% lighter glass rim; an unseen school gets a socket.
  13. Labels in unmirrored GUI space.

  Time is `AnimationTime.gameTime(level, partialTick) / 20f`. Textures are cached per `layout.key() + degree + apotheos`. `clearCaches()` releases all of them and resets the key.
- [ ] **Step 3: Run** `./gradlew.bat compileJava compileGameTestJava test --tests "*overlay*"`. Expected: BUILD SUCCESSFUL, tests PASS.
- [ ] **Step 4:** Leave in the working tree.

### Task 7: Native review

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/TemperRackReview.java`

- [ ] **Step 1: Extend `Scene`** with `float blood` and `boolean jarShown`. Add these scenes:
  - Degrees 0, 3 and 5.
  - Blood at 15%.
  - Reserve + temper with each reserve (gourd, horn, rib), on both sides.
  - Jar + vessel, jar + reserve, jar + vials-only and jar-only.
  - `LEGACY_ICON` in the jar.
  - All parts top-right, and all parts at GUI scale 4.
- [ ] **Step 2: Run** `./gradlew.bat temperRackReviewClient`. Get past the Accessibility screen (memory: review clients stall there). Inspect every screenshot in `build/temper-rack-review/screenshots`.
- [ ] **Step 3: Tune coordinates and art** until the owner's rules hold:
  - The binding is thin and calm.
  - The graft dip is present and the binding is form-fitting.
  - The vials are seated, and the trickles land in the glass.
  - No text is mirrored, and nothing overlaps the hotbar.
- [ ] **Step 4:** Re-run the unit tests after any geometry change.

### Task 8: Docs, guardrails, full suite

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md` (the "Showing it" paragraph near line 650), `docs/architecture/evidence/source-text-impact-map.csv`

- [ ] **Step 1:** Rewrite "Showing it" to describe the binding, heartbeat, vials, trickle, jar, states and mirroring, naming `HudAssembly`/`HudBinding` and the review client. Check `wiki/*.md` and `docs/archive/tracks/core_loop/04-temper-and-catalysts.md` for HUD prose.
- [ ] **Step 2:** Run `python gradle/source-text-impact-map.py`.
- [ ] **Step 3:** Run `./gradlew.bat test`. Expected: BUILD SUCCESSFUL. Pre-existing failures unrelated to the HUD are reported, not fixed.
- [ ] **Step 4:** Run the final whole-change review (one reviewer).
