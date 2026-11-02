# Harbinger Cast Outpouring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task (native execution; one final reviewer). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace every Harbinger cast effect drawn on or around the caster with the Cast Outpouring. The caster's blood leaves the body at the manipulation's vascular section, shader veins spread from that section, the blood transforms per tendency, it scales with rank, and it leaves fading marks.

**Architecture:**
- **Shared data.** A pure `OutpouringRules` holds timing, rank and section data. A pure `SkinBodyAtlas` maps every skin texel to a canonical 3D body point. Because the vein noise is computed in body space, veins stay continuous across UV seams and across the armour shell.
- **Client simulation.** A client-side `OutpouringSim` moves motes (drops, strands, orbs, fog puffs) with per-tendency forces and block collision. Collisions become decal marks.
- **Rendering.** Everything draws through new core shaders:
  - `cast_veins` on the player model and on an armour shell
  - `cast_outpouring` for the motes
  - `cast_mark` for the decals
  - `cast_lumen` and `cast_umbra` as depth-reading full-screen passes for Lux light and Tenebris darkness
- **Server.** The only server change is an off-by-default real-effect hook, plus suppressing the generic activation burst for players.

**Tech Stack:** NeoForge 1.21.1, Java 21, GLSL 150 core shaders through `ShaderHolder`/`ExtendedShaderInstance`, JUnit 5 (`./gradlew.bat test`).

**Spec:** `docs/superpowers/specs/2026-10-19-harbinger-cast-outpouring-design.md`

## Global Constraints

- Harbinger lane only. `StillArtRings`, `stillHand` and `stillGround` must behave exactly as before.
- No vanilla `ParticleTypes` in any new or touched class. HutosLib/Hemomancy particles are accents only.
- No brass, bronze, copper or silver tones. Use the blood palette plus each tendency's material colours. Accents: hot core `0xFFD2C4`, clot `0x7A1020`.
- Nothing floats detached behind or above the player.
- Marks are client-only, never saved, capped at 256, and cleared on level change.
- Real world effects: the server config `castOutpouringRealEffectMinRank` defaults to `-1` (never), and every effect implementation is a no-op.
- Particle setting:
  - MINIMAL: veins only.
  - DECREASED: reduced outpouring, with no marks farther than 1.5 blocks from the caster's feet.
- Distance thresholds:
  - Over 24 blocks: reduced.
  - Over 32 blocks: veins only.
  - Over 48 blocks: nothing.
- In first person, the caster's own motes within 0.8 blocks of the camera are skipped. Tenebris self-shade is drawn at 0.35 strength with a clear centre.
- NeoForge idioms only. Use `Hemomancy.rloc`. Register shaders in `ShaderInit` and check reserved words (`ShaderReservedWordTest`). Never shadow GLSL built-ins (`distance`, `offset`, `noise2`, `packed`).
- Do not commit. Leave the work in the working tree on top of the uncommitted Casting Animations v2 changes.

## Review Focus

1. **Recast while an aftermath is still playing.** A new cast must start fresh at the new section and tendency, and the old motes must finish their own fade without being re-targeted. Test: `OutpouringClientStateTest.recastDuringAftermathStartsNewCast`, Task 6.
2. **Caster dies, leaves, or the level changes mid-cast.** Sim state, marks and screen-pass entries must all clear, with no stale lights or darkness. Test: `OutpouringClientStateTest.levelChangeClearsEverything`, Task 6. Mark store: `OutpouringMarksTest.clearEmptiesStore`, Task 7.
3. **A preview or packet whose manipulation id has no registry entry** (`"preview"`, ids from a removed manipulation). Section falls back to ARMS and the mode to FULL, with no NPE. Test: `OutpouringRulesTest.unknownIdFallsBackToArms`, Task 2.
4. **The caster is enclosed in blocks (a 1×2 tunnel) or standing in water or lava.** Motes must not tunnel through walls or explode in count, and marks must land only on faces that touch air. Test: `OutpouringSimTest.enclosedCasterKeepsMotesInsideAndBounded`, Task 5. Test: `OutpouringMarksTest.marksOnlyOnExposedFaces`, Task 7.
5. **Many casters at once (8+ Perfectus Lux/Tenebris).** Screen passes take at most 8 entries each, nearest first, and mote count is capped globally. Test: `OutpouringScreenPassesTest.capsAtEightNearest`, Task 8. Test: `OutpouringSimTest.globalMoteCapHolds`, Task 5.

---

### Task 1: Remove the old caster effects

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/CastingAnatomyRenderer.java`. Keep only `stillHand`/`stillGround`, and make `hand`/`body` dispatch Still Art only.
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/StillArtRings.java`. Add `public static RenderType renderType()`, moved verbatim from `LuxCastingWings`.
- Delete: `src/main/java/com/vincenthuto/hemomancy/client/render/world/LuxCastingWings.java`
- Delete: `src/test/java/com/vincenthuto/hemomancy/client/render/world/LuxCastingWingsTest.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/CastingAnimationLayer.java`. Remove the Lux body pivot and the Harbinger `hand`/`body` calls; the Still Art calls stay.
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/CastingAnimationClientEvents.java:71-79`. Remove the Harbinger `hand` call; the Still Art call stays.
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationParticles.java`. In `activate`, return early when `caster instanceof Player`.
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/render/world/StillArtRingsTest.java:45-51`.
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRemovalSourceTest.java` (new).

**Interfaces:**
- Produces: `StillArtRings.renderType()` (RenderType, POSITION_COLOR, unlit, translucent, the former `lux_casting_wings` state, renamed `still_art_rings`).
- Produces: `CastingAnatomyRenderer.stillHand(...)` / `stillGround(...)`, with unchanged signatures.

- [ ] **Step 1: Write the failing source test** `OutpouringRemovalSourceTest`:
  - `oldAnatomyIsGone`: `CastingAnatomyRenderer.java` contains none of `"void heart("`, `"void furnace("`, `"void nerves("`, `"void cadaver("`, `"void crystal("`, `"void foundry("`, `"void shadow("`, `"LuxCastingWings"`, `"VisceralMesh"`.
  - `wingsDeleted`: `Files.exists(.../LuxCastingWings.java)` is false.
  - `playersGetNoActivationBurst`: `ManipulationParticles.java` contains `"caster instanceof Player"`.
  - `layerNoLongerPivotsForLux`: `CastingAnimationLayer.java` does not contain `"EnumBloodTendency.LUX"`.
- [ ] **Step 2: Update `StillArtRingsTest.stillArtRingsUseTheUntexturedLuxType`.** Slice from `"public static void stillHand"` to the end of the file. Assert `still.contains("StillArtRings.renderType()")` and `!still.contains("ManipulationMaterials")`, then rename the method `stillArtRingsUseTheUntexturedRingType`.
- [ ] **Step 3: Run the test and confirm it fails.** Run `./gradlew.bat test --tests "*OutpouringRemovalSourceTest" --tests "*StillArtRingsTest"`. Expected: FAIL.
- [ ] **Step 4: Make the edits listed under Files.** In `CastingAnatomyRenderer`:
  - `hand(...)` becomes: if UNSTAINED, call `stillHand`, otherwise return.
  - `body(...)` becomes: if UNSTAINED, call `stillGround`, otherwise return.
  - Delete `blood`, `accent`, `vertices` and every construct helper.
  - Replace the class javadoc with: "Still Art casting accents. Harbinger casts draw through the outpouring package."
- [ ] **Step 5: Run the tests and confirm they pass.** Run `./gradlew.bat test --tests "*OutpouringRemovalSourceTest" --tests "*StillArtRingsTest" --tests "*StillArtPresentationSourceTest"`. Expected: PASS.

### Task 2: OutpouringRules (pure timeline, rank, section and roster)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRulesTest.java`

**Interfaces:**
- Consumes: `EnumManipulationType`, `EnumManipulationRank`, `EnumVeinSections`, `CastPhase` (common enums only, no Minecraft client types).
- Produces:
  - `enum Mode { FULL, WOUND, TOGGLE }`
  - `enum Stage { BUILD, PEAK, AFTERMATH, WITHDRAW, DONE }`
  - `enum Detail { FULL, REDUCED, VEINS, NONE }`
  - `record Profile(double volume, double reach, double wildness, int markLife, int maxMarks, double veinReach, int aftermath)`
  - `record Pulse(float strength, Stage stage)`
  - `static Profile profile(EnumManipulationRank rank)`
  - `static Mode mode(String id, EnumManipulationType type)`, where a null type means QUICK
  - `static boolean woundOnly(String id)`
  - `static Pulse pulse(EnumManipulationType type, CastPhase phase, float charge, float sincePhase, float carried, EnumManipulationRank rank)`
  - `static Detail detail(int particleSetting, double distanceSq)`
  - `static EnumVeinSections previewSection(String id)` (null when the id is not a preview id)
  - `static String previewId(EnumVeinSections s)`, which returns `"preview_section_" + s.name().toLowerCase(Locale.ROOT)`
  - `static double bodyContact(Pulse pulse, EnumManipulationRank rank)` (0..1)

**Values pinned:**
- `profile` rows (volume, reach, wildness, markLife, maxMarks, veinReach, aftermath):

  | Rank | volume | reach | wildness | markLife | maxMarks | veinReach | aftermath |
  |---|---|---|---|---|---|---|---|
  | HUMILIS | .2 | .6 | .1 | 60 | 2 | .25 | 20 |
  | MEDIOCRITAS | .4 | 1.0 | .3 | 100 | 6 | .4 | 30 |
  | SUMMA | .65 | 2.0 | .55 | 160 | 14 | .6 | 40 |
  | MAGISTER | .85 | 3.5 | .8 | 240 | 26 | .8 | 50 |
  | PERFECTUS | 1.0 | 5.0 | 1.0 | 320 | 40 | 1.0 | 60 |

- `woundOnly` covers:
  - `hematic_riposte` and `crimson_coronation`
  - every id starting with `summon_avatar`
  - every id for which `LivingStaffWeaponFormRules.isStaffWeaponFormManip(id)` is true
- `mode`:
  - `woundOnly` → WOUND
  - else PASSIVE → TOGGLE
  - else FULL
- `pulse`:
  - **Sustained (OPENING/HOLD), CHARGED:** stage BUILD, strength `.15 + .6 * charge`.
  - **Sustained, CONTINUOUS (or any other type):** stage BUILD, strength `.8 * clamp(sincePhase / (10 + 2.5 * rank.ordinal()), 0, 1)`.
  - **RELEASE or RECOVERY:**
    - `sincePhase < 2`: stage PEAK, strength `lerp(carried, 1, sincePhase / 2)`.
    - `sincePhase < 8`: stage PEAK, strength 1.
    - `sincePhase < 8 + aftermath`: stage AFTERMATH, strength `1 - smoothstep((sincePhase - 8) / aftermath)`.
    - Otherwise: stage DONE, strength 0.
  - **PASSIVE:** the same RELEASE curve scaled by .55, with a 14-tick aftermath.
  - **CANCEL:** stage WITHDRAW, strength `carried * (1 - clamp(sincePhase / 6, 0, 1))`. From `sincePhase >= 6` the stage is DONE.
- `detail`:
  - `distanceSq > 48²` → NONE
  - particle setting 2 (MINIMAL) or `> 32²` → VEINS
  - setting 1 (DECREASED) or `> 24²` → REDUCED
  - otherwise FULL
- `bodyContact`:
  - BUILD: 0
  - PEAK: `rank.ordinal() / 4.0 * .8 + .2`
  - AFTERMATH: the PEAK value × strength
  - WITHDRAW and DONE: 0

- [ ] **Step 1: Write the failing tests:**
  - `profilesGrowMonotonically`: every field of `profile(r+1)` is ≥ `profile(r)`, and `profile(PERFECTUS).reach() == 5.0`.
  - `woundRosterCoversOnBodySpells`: `woundOnly` is true for `hematic_riposte`, `crimson_coronation`, `summon_avatar_arms` and `conjure_blade`, and false for `blood_shot`.
  - `passivesToggle`: `mode("blackhearted", PASSIVE) == TOGGLE`.
  - `chargedBuildsWithCharge`: `pulse(CHARGED, HOLD, .5f, 0, 0, SUMMA)` is BUILD with strength `.45`, within 1e-4.
  - `quickPeaksOnArrival`: `pulse(QUICK, RELEASE, 0, 2, 0, SUMMA)` is PEAK with strength 1.
  - `aftermathLastsRankTicks`: `pulse(QUICK, RELEASE, 0, 8 + 40, 0, SUMMA).stage() == DONE` and `pulse(QUICK, RELEASE, 0, 8 + 39, 0, SUMMA).stage() == AFTERMATH`.
  - `channelRampsThenHolds`: `pulse(CONTINUOUS, HOLD, 0, 100, 0, PERFECTUS).strength() == .8f`.
  - `cancelWithdraws`: `pulse(CHARGED, CANCEL, 0, 3, .6f, SUMMA)` is WITHDRAW with strength `.3`.
  - `passiveIsReduced`: the peak strength from `pulse(PASSIVE, RELEASE, 0, 4, 0, PERFECTUS)` is `.55`.
  - `detailThresholds`: `detail(0, 10*10) == FULL`, `detail(0, 30*30) == REDUCED`, `detail(0, 40*40) == VEINS`, `detail(0, 50*50) == NONE`, `detail(2, 1) == VEINS`, `detail(1, 1) == REDUCED`.
  - `unknownIdFallsBackToArms`: `previewSection("preview") == null`, and `previewSection(previewId(HEART)) == HEART`.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*OutpouringRulesTest"`. Expected: FAIL (the class does not exist).
- [ ] **Step 3: Implement `OutpouringRules` with the values above.**
- [ ] **Step 4: Run them and confirm they pass.** Expected: PASS.

### Task 3: SkinBodyAtlas (texel → body point)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/SkinBodyAtlas.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/SkinBodyAtlasTest.java`

**Interfaces:**
- Produces:
  - `static float[] point(int u, int v, boolean slim)`: `{x, y, z, part}` in body pixels, or null for an unmapped texel.
  - `static int[] rgba(boolean slim)`: 64×64 ABGR ints ready for `NativeImage.setPixelRGBA`.
- Body space:
  - x is model x (negative = the player's right).
  - y is `24 - modelY` (feet at 0, head top at 32).
  - z is `-modelZ` (front positive).
- Encoding: R = `(x + 8) / 16`, G = `y / 32`, B = `(z + 4) / 8`, A = `(part + 1) * 36`. Unmapped texels are 0.
- Parts (vanilla player mesh; `texOffs`, box origin and size, pivot):

  | Part | id | texOffs | box origin, size | pivot |
  |---|---|---|---|---|
  | head | 0 | (0,0) | (-4,-8,-4), 8×8×8 | (0,0,0) |
  | hat | 0 | (32,0) | same as head | (0,0,0) |
  | body | 1 | (16,16) | (-4,0,-2), 8×12×4 | (0,0,0) |
  | jacket | 1 | (16,32) | same as body | (0,0,0) |
  | rightArm | 2 | (40,16) | (-3,-2,-2) 4×12×4; slim (-2,-2,-2) 3×12×4 | (-5,2,0) |
  | right sleeve | 2 | (40,32) | same as rightArm | (-5,2,0) |
  | leftArm | 3 | (32,48) | (-1,-2,-2), 4 or 3 wide | (5,2,0) |
  | left sleeve | 3 | (48,48) | same as leftArm | (5,2,0) |
  | rightLeg | 4 | (0,16) | (-2,0,-2), 4×12×4 | (-1.9,12,0) |
  | right pants | 4 | (0,32) | same as rightLeg | (-1.9,12,0) |
  | leftLeg | 5 | (16,48) | same as rightLeg | (1.9,12,0) |
  | left pants | 5 | (0,48) | same as rightLeg | (1.9,12,0) |

- Face unwrap (`ModelPart.Cube`, with `f=x0, f1=y0, f2=z0, f3=x0+w, f4=y0+h, f5=z0+d`; `u1=u+d, u2=u1+w, u3=u2+w, u4=u2+d, u5=u4+w`; `v1=v+d, v2=v1+h`):

  | Face | Texel range | Point (s, t are the fractions across the face rect) |
  |---|---|---|
  | DOWN | [u1,u2)×[v,v1) | `(f+s·w, f1, f5−t·d)` |
  | UP | [u2,u3)×[v,v1) | `(f+s·w, f4, f5−t·d)` |
  | WEST | [u,u1)×[v1,v2) | `(f, f1+t·h, f5−s·d)` |
  | NORTH | [u1,u2)×[v1,v2) | `(f+s·w, f1+t·h, f2)` |
  | EAST | [u2,u4)×[v1,v2) | `(f3, f1+t·h, f2+s·d)` |
  | SOUTH | [u4,u5)×[v1,v2) | `(f3−s·w, f1+t·h, f5)` |

  Sample at texel centres (`u + .5`). Add the pivot, then convert to body space.

- [ ] **Step 1: Write the failing tests:**
  - `faceFrontIsForward`: `point(12, 12, false)` has z ≈ 4 and y ≈ 27.5 within .6.
  - `headTopIsHighest`: `point(12, 4, false)` has y == 32.
  - `rightHandIsLowAndRight`: the rightArm UP face (the hand end, at model y max) at texel `point(50, 18, false)` has y == 12 and x in [-8, -4].
  - `slimArmIsNarrower`: the max |x| over rightArm texels is 7 for slim and 8 for wide.
  - `overlayMatchesBase`: `point(44, 36, false)` (right sleeve) equals `point(44, 20, false)`.
  - `unmappedIsNull`: `point(0, 0, false) == null`, and `point(60, 2, false) == null`.
  - `encodingRoundTrips`: for every mapped texel, the decode of `rgba(false)` matches within .07·range.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*SkinBodyAtlasTest"`.
- [ ] **Step 3: Implement it.** Use a static part table, iterate faces, and cache the two `rgba` arrays.
- [ ] **Step 4: Run them and confirm they pass.**

### Task 4: Vein skin shader, armour shell and first-person arm

**Files:**
- Create shaders:
  - `src/main/resources/assets/hemomancy/shaders/core/entity/cast_veins.json`
  - `src/main/resources/assets/hemomancy/shaders/core/entity/cast_veins.vsh`
  - `src/main/resources/assets/hemomancy/shaders/core/entity/cast_veins.fsh`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java`. Add `CAST_VEINS = new ShaderHolder(rloc("entity/cast_veins"), NEW_ENTITY, "HemoTime", "ColorModulator")` and register it.
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRenderTypes.java`. It holds the veins type now and the material types in Task 6.
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/CastVeinsLayer.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/VeinEncoding.java` (pure)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java:390`. Register `CastVeinsLayer` beside `CastingAnimationLayer` on player skins.
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/CastingAnimationClientEvents.java`. Inside the `renderFirstPerson` lambda, after the vanilla arm, call `CastVeinsLayer.firstPersonArm(...)`.
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/VeinEncodingTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/CastVeinsSourceTest.java`

**Interfaces:**
- Consumes: `OutpouringRules.pulse/profile/bodyContact`, `SkinBodyAtlas.rgba`, and `OutpouringClientState.veins(Player, float partial)` from Task 6. Until Task 6, the layer reads `CastingAnimationClientState.animation` directly, and Task 6 swaps it.
- Produces:
  - `record VeinEncoding.Packed(int color, int overlay, int light)`
  - `static Packed VeinEncoding.pack(EnumBloodTendency school, EnumBloodTendency secondary, EnumVeinSections section, EnumManipulationRank rank, float strength, float contact, float fade, boolean offHand, int seed)`
  - `static float[] VeinEncoding.unpack(Packed p)` (for tests)
  - `OutpouringRenderTypes.VEINS` (NEW_ENTITY, translucent, LEQUAL, COLOR_WRITE, NO_CULL, NO_LIGHTMAP). It binds the atlas `DynamicTexture` at sampler 0 (`hemomancy:dynamic/cast_body_atlas`, wide) or 1 (slim), and sets `HemoTime`.
  - `static void CastVeinsLayer.firstPersonArm(PlayerModel<?> model, HumanoidArm arm, PoseStack poses, MultiBufferSource buffer, AbstractClientPlayer player, float partial)`
- **Packing:**
  - color ARGB:
    - A = fade·255
    - R = strength·255
    - G = school·8 + section
    - B = (secondary == null ? 8 : secondary)·8 + rank
  - overlay: u = contact·1000, v = seed % 1000
  - light: u = (offHand ? 1 : 0), v = 0
  - The shader reads `UV1`/`UV2` as raw ivec2 and ignores lighting.
- **Shader:**
  - The vertex shader pushes along the normal by `.035` and passes UV0, the raw ivec UV1/UV2, and colour.
  - The fragment shader samples the atlas at UV0 (NEAREST); alpha 0 means discard. It decodes the body point.
  - Exit points in body pixels:
    - HEAD: eyes `(±2, 28, 4)`, nose `(0, 27, 4)`, ears `(±4, 27, 0)`
    - HEART: `(1.5, 19, 2)`
    - BODY: `(0, 17, 0)`, `(±4, 16, 0)`
    - ARMS: right hand `(-6, 12.5, 0)`; left hand `(6, 12.5, 0)` only when the offHand bit is set
    - LEGS: `(±2, 6, 1)`, `(±2, 2, 1)`
  - growth = `smoothstep(R, R - 2, minDist)`, where `R = strength · (4 + 26 · veinReach(rank))`.
  - Veins: a domain-warped 3D cellular-edge noise on the body point (two octaves, scale .55 and 1.3), thresholded into branching lines that thin with distance from the exit.
  - Per-tendency colour and motion, by school index:
    - 0 Animus: hot red, 1.6 Hz pulse
    - 1 Flammeus: ember orange cracks with flicker
    - 2 Ductilis: gold crackle that strobes along the lines
    - 3 Lux: red-white glow, brightest, slow throb
    - 4 Mortem: green-brown, slow seep
    - 5 Congeatio: frost-blue rime over red
    - 6 Ferric: dark iron raised, with a hard specular edge
    - 7 Tenebris: black-violet smoke smear
  - Contact (UV1.x / 1000) adds a second field, coverage = `smoothstep(Rc, Rc - 3, minDist)`, where `Rc = contact · 30`:
    - Animus: blood smear
    - Flammeus: scorch char plus embers
    - Ductilis: soot crackle
    - Lux: bright bloom
    - Mortem: corrosion
    - Congeatio: frost crust
    - Ferric: iron crust
    - Tenebris: soot-black creep
  - Secondary tendency (B/8 < 8): 20% of vein cells take the secondary's colour.
- **Layer render:**
  1. Skip if invisible, `detail == NONE`, or a blood-avatar stage is active (`AvatarManifestationTransition.sample(...).renders()`).
  2. Save the parent model's overlay-part visibility (`hat`, `jacket`, sleeves, pants), hide those parts, and hide any base part that is armoured.
  3. `renderToBuffer` the parent with VEINS, then restore visibility.
  4. Shell: lazily bake `new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(1.1f), slim), 64, 64).bakeRoot(), slim)`, one each for wide and slim. Run `copyPropertiesTo(shell)`, make only the armoured parts visible (head ← HEAD; body and both arms ← CHEST; both legs ← LEGS or FEET), hide the overlay parts, and render with the same packing.

- [ ] **Step 1: Write the failing tests:**
  - `VeinEncodingTest.roundTrip`: for every school × section × rank, `unpack(pack(...))` returns the same school, section and rank, with strength, contact and fade within 1/255 or 1/1000.
  - `VeinEncodingTest.noSecondaryIsEight`: `unpack(pack(...null...))[secondaryIndex] == 8`.
  - `CastVeinsSourceTest`:
    - `LayerEvents.java` contains `"new CastVeinsLayer"`.
    - `CastingAnimationClientEvents.java` contains `"CastVeinsLayer.firstPersonArm("`.
    - `CastVeinsLayer.java` contains `"CubeDeformation(1.1f)"` and `"AvatarManifestationTransition"`.
    - `cast_veins.fsh` contains `"Sampler0"` and no identifier named `distance`, `offset` or `packed`.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*VeinEncodingTest" --tests "*CastVeinsSourceTest"`.
- [ ] **Step 3: Implement it.** Build `VeinEncoding`, the shader trio and its json (attributes Position, Color, UV0, UV1, UV2, Normal; sampler `Sampler0`; uniforms as in `manipulation_skin.json` plus `HemoTime`), `ShaderInit` registration, `OutpouringRenderTypes.VEINS` (create the `DynamicTexture` from `SkinBodyAtlas.rgba` on first use), `CastVeinsLayer`, and the first-person hook.
- [ ] **Step 4: Run the tests and `ShaderReservedWordTest`, and confirm they pass.**
- [ ] **Step 5: Compile with `./gradlew.bat compileJava`.** Expected: BUILD SUCCESSFUL.

### Task 5: OutpouringSim (pure mote simulation)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringSim.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringSimTest.java`

**Interfaces:**
- Produces:
  - `interface Blocks { boolean solid(int x, int y, int z); boolean liquid(int x, int y, int z); }`
  - `enum Kind { DROP, STRAND, SHARD, PLATE, SPIKE, POOL, ORB, PUFF, FLAME, BLOB }`
  - `static final class Mote`, with public fields `x, y, z, vx, vy, vz, size, age, life, kind, stuck, secondary, seed`, plus `double[] trail` holding the last 6 positions xyz (18 doubles), `trailCount`, and `prevX/prevY/prevZ` for interpolation.
  - `record Contact(int bx, int by, int bz, int face, double hx, double hy, double hz, float size, boolean spark)`, where `face` is a Direction ordinal.
  - `OutpouringSim(EnumBloodTendency school, EnumBloodTendency secondary, long seed)`
  - `void emit(double[] exits, int exitCount, double[] outward, float strength, OutpouringRules.Profile profile, boolean burst, boolean woundOnly)`. `exits` and `outward` are xyz triples in world space.
  - `List<Contact> step(Blocks blocks, double cx, double cy, double cz, boolean withdraw, int markBudget)`, which advances one tick.
  - `List<Mote> motes()`, `int size()`, `boolean finished()`
  - `static final int GLOBAL_CAP = 1200`, enforced by a static live counter in `OutpouringSim`
  - `static final int PER_CAST_CAP = 220`
- **Emission:**
  - Per tick: `rate = profile.volume() · strength · 6`.
  - On a burst: `rate = profile.volume() · 40`.
  - `woundOnly`: rate × .15 and no burst.
  - Each mote copies the secondary tendency with probability .2.
  - The kind comes from the school:

    | School | Kinds |
    |---|---|
    | ANIMUS | DROP → POOL on ground |
    | FLAMMEUS | FLAME |
    | DUCTILIS | STRAND |
    | LUX | ORB, at most `1 + rank` alive |
    | MORTEM | BLOB |
    | CONGEATIO | DROP → SHARD on collision |
    | FERRIC | 70% PLATE, 30% SPIKE |
    | TENEBRIS | PUFF |

- **Forces per tick** (gravity g = .04; drag multiplies velocity; `w` = wildness; `cx/cy/cz` = caster feet):

  | School | Behaviour |
  |---|---|
  | ANIMUS | Drops fall under gravity. On landing a drop becomes a POOL with no vertical velocity. A pool's tangential velocity around the caster is `.08 + .12·w`, pulled toward ring radius `.6 + reach·.5`. |
  | FLAMMEUS | Buoyancy `vy += .03`, drag .9, lateral jitter `w·.02`. Size grows `1.04×` per tick. Life 12–20 ticks. |
  | DUCTILIS | Speed `.35 + .4·w`. Every `2 + seed % 3` ticks the direction snaps by a random rotation of up to `(60 + 90·w)°`. Life 8–14 ticks. Each collision returns a spark contact and reflects the velocity. |
  | LUX | Orbs ease toward orbit points at chest height (`cy + 1.3`), radius `.7 + .3·rank`, angular speed .08. They never collide and never contact. |
  | MORTEM | Gravity × .35, drag .8, stickiness. They stop on any collision and slide down vertical faces at .005 per tick. Life 40–80 ticks. |
  | CONGEATIO | Gravity and drag .92. On collision the mote becomes a SHARD and sticks. |
  | FERRIC | Flung outward at `.25 + .2·w` with gravity. On collision a PLATE sticks flush and a SPIKE sticks along its velocity. |
  | TENEBRIS | Puffs sink (`vy -= .01`) and spread radially (`+.02·(1 + w)`). They stop sinking at ground height and grow `1.06×` up to `1.5 + reach·.3`. They never contact. |

- **Withdraw:** every mote steers toward the nearest exit at .25 per tick and dies within .2 of it.
- **Collision:** test the cell of the new position. If it is solid, step back to the old position. The contact face is the axis with the largest penetration. Liquids kill DROP/FLAME/PUFF without a contact.
- At most `markBudget` contacts are returned per step.
- A mote dies when `age >= life`, or when it is more than `reach + 2` from the caster.

- [ ] **Step 1: Write the failing tests.** Each uses a `Blocks` lambda: a floor at y < 64, plus optional walls.
  - `animusPoolsAndCircles`: emit an ANIMUS burst at y=65.5 and step 40 ticks. At least one POOL exists, its y ≈ 64 within .1, and its angle around the caster changes between steps 30 and 40.
  - `flammeusRises`: the mean y of FLAME after 6 steps exceeds the emission y.
  - `congeatioFreezesOnContact`: a mote emitted toward a wall becomes a stuck SHARD and returns one contact on that wall's face.
  - `ductilisSnapsAndSparks`: in a box of walls, at least one contact with `spark == true` within 15 steps.
  - `luxOrbsNeverContact`: there are zero contacts over 60 steps, and the ORB count is ≤ `1 + rank`.
  - `tenebrisSinksAndSpreads`: the mean radial distance at step 20 is greater than at step 5.
  - `withdrawReturnsToExit`: after `step(..., withdraw=true, ...)` × 20, `size() == 0`.
  - `enclosedCasterKeepsMotesInsideAndBounded`: in a 1×2 air tunnel, after a PERFECTUS burst no mote lies inside a solid cell and `size() <= PER_CAST_CAP`.
  - `globalMoteCapHolds`: 10 sims each bursting PERFECTUS keep the sum of `size()` ≤ `GLOBAL_CAP`.
  - `contactsRespectBudget`: `step(..., markBudget=1)` returns at most 1 contact.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*OutpouringSimTest"`.
- [ ] **Step 3: Implement `OutpouringSim`.** Use `java.util.Random(seed)`. No Minecraft types except the tendency enum.
- [ ] **Step 4: Run them and confirm they pass.**

### Task 6: Client state, anchors, material shader and mote rendering

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringClientState.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringAnchors.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringRenderer.java`, a `@EventBusSubscriber` on `RenderLevelStageEvent` AFTER_PARTICLES
- Modify: `OutpouringRenderTypes.java`. Add `material(EnumBloodTendency, Kind)`: 24 lazily created POSITION_TEX_COLOR types sharing `CAST_OUTPOURING`, each setting uniform `Mode = school·4 + kindGroup`, where kindGroup is 0 for strand/drop, 1 for blob/pool/plate/shard/spike, 2 for vapour/puff/flame and 3 for orb.
- Create shaders:
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_outpouring.json`
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_outpouring.vsh`
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_outpouring.fsh`
- Modify: `ShaderInit.java`. Add `CAST_OUTPOURING` (POSITION_TEX_COLOR, "HemoTime", "Mode", "ColorModulator").
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingAnimationClientState.java`. Call `OutpouringClientState.accept(packet)` from `accept` and from `preview`, and `OutpouringClientState.clear()` from `levelCheck` when the level changes.
- Modify: `CastingAnimationClientEvents.tick`. Call `OutpouringClientState.tick()`.
- Modify: `CastingAnimationLayer.java`. In third person, call `OutpouringAnchors.capture(player, model, poses, partial)` for Harbinger animations.
- Modify: `CastVeinsLayer`. Read `OutpouringClientState.veins(player, partial)`.
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringClientStateTest.java`. It covers the pure `Cast` lifecycle, extracted as the package-private `OutpouringClientState.Cast` with a `long now` parameter.

**Interfaces:**
- Consumes: `OutpouringRules`, `OutpouringSim`, `CastingAnimationPacket`, `ManipulationInit.getByName`.
- Produces:
  - `static void OutpouringClientState.accept(CastingAnimationPacket p)`, which ignores `lane() == UNSTAINED` and null `school`
  - `static void tick()`, `static void clear()`
  - `static Collection<Cast> casts()`
  - `static VeinState veins(Player player, float partial)`, where `record VeinState(VeinEncoding.Packed packed, boolean active)`
  - `static List<Light> luxLights(Vec3 camera, float partial)`, with `record Light(Vec3 at, float strength)`, nearest 8
  - `static List<Shade> shades(Vec3 camera, float partial)`, with `record Shade(Vec3 at, float radius, float strength, boolean self)`, nearest 8
  - `Cast`:
    - fields `UUID player`, `int entityId`, `String id`, `long sequence`, `EnumBloodTendency school`, `secondary`, `EnumManipulationRank rank`, `EnumVeinSections section`, `Mode mode`, `EnumManipulationType type`, `CastPhase phase`, `long phaseStart`, `int held`, `int required`, `float carried`, `OutpouringSim sim`
    - methods `Pulse pulse(long now, float partial)`, `void update(CastingAnimationPacket p, long now)`, `boolean finished(long now)`
  - `OutpouringAnchors`:
    - `static void capture(AbstractClientPlayer p, PlayerModel<?> m, PoseStack poses, float partial)` stores world-space exits in a `Map<UUID, Captured>` stamped with game time
    - `static double[] exits(Player p, EnumVeinSections s, EnumManipulationRank r, float partial, double[] outwardOut)` returns xyz triples, using the capture if it is no more than 2 ticks old and the estimate otherwise
    - Estimate exits (body-local blocks: x right, y up, z forward; rotated by `yBodyRot`, scaled by `getScale()`, lowered .3 when crouching):
      - HEAD: eyes `(±.11, 1.56, .25)` → outward (0, -.2, 1); at rank ≥ SUMMA also nose `(0, 1.48, .27)` and ears `(±.25, 1.5, 0)`
      - HEART: `(.08, 1.15, .14)` → (0, 0, 1)
      - BODY: flanks `(±.25, 1.0, 0)` → (±1, 0, 0), plus `(0, .85, .14)` and `(0, 1.1, -.14)`
      - ARMS: the hand derived from look direction, `eye + look·.6 − up·.35 ± right·.3` (off hand only when `FirstPersonHandRules.affectsOffHand`) → look
      - LEGS: `(±.12, .55, .1)` and `(±.12, .25, .08)` → (0, -1, .2)
    - Capture points: model-space part-local pixels via `translateAndRotate` on head/body/legs and `translateToHand` on arms, transformed by `poses.last().pose()` plus the camera position.
- **Accept rules:** a new `Cast` replaces the current one when there is none, the current one is AFTERMATH/WITHDRAW/DONE, or `sequence` or `id` differ. The replaced cast's sim keeps running until `finished()` in a `fading` list, with no further emission. Otherwise the packet updates `phase/phaseStart/held/required`, and `carried` is set to the strength at the phase change.
- **Section:** `ManipulationInit.getByName(id)`, then `getSection()`; else `OutpouringRules.previewSection(id)`; else ARMS.
- **Type:** from the registry, or from the preview packet (`required > 0` → CHARGED, otherwise QUICK unless id `blackhearted` → PASSIVE).
- **Rendering (OutpouringRenderer, AFTER_PARTICLES):**
  - For each cast, skip if `detail(...) == VEINS/NONE` or the caster is invisible.
  - Each mote, interpolated by partial:
    - STRAND/DROP/FLAME: `LuxUmbraGeometry.ribbonPath` along its trail, width `size`.
    - Others: a camera-facing `LuxUmbraGeometry.card` sized `size`, with UV 0..1.
    - Colour: white tint, or the secondary's tint at 0.5 alpha when `secondary`.
  - In first person (`mc.options.getCameraType().isFirstPerson()` and the caster is the local player), skip motes within .8 blocks of the camera.
  - Call `endBatch` per material.
- **`cast_outpouring.fsh` modes** (school·4 + group; the UV is ribbon length × width, or card 0..1):
  - Animus: wet glossy red with a moving sheen.
  - Flammeus: blood at the root, igniting to a flammeus flame tongue (orange-pink core `0xFFD2C4`, never yellow-white) higher along the UV.
  - Ductilis: blood strand with a travelling gold-white crackle and hard flicker.
  - Lux: orb as a soft radial red-white core with a pulsing halo, additive look via premultiplied colour.
  - Mortem: sludgy green-brown-red, lumpy alpha edge, slow churn.
  - Congeatio: reddish translucent ice with facet bands and a frost rim.
  - Ferric: dark iron plate or spike with blood seams, hard edges.
  - Tenebris: black-violet soft puff with a rolling fbm edge.

- [ ] **Step 1: Write the failing tests:**
  - `OutpouringClientStateTest.chargeBuildsThenPeaks`: Cast from a CHARGED OPENING packet (held 20 of 40), then `pulse` is BUILD. After `update` with RELEASE, `pulse(now + 3)` is PEAK.
  - `recastDuringAftermathStartsNewCast`: after RELEASE at t, an accept at t+15 with a new sequence and section HEAD produces a new current Cast with section HEAD, and the old cast lands in `fading`.
  - `cancelWithdraws`: CANCEL gives WITHDRAW, and `finished(now + 7)` is true.
  - `stillArtIgnored`: accepting a still-art packet leaves `casts()` empty.
  - `levelChangeClearsEverything`: `clear()` empties both casts and fading.
  - `previewSectionResolves`: a packet with id `OutpouringRules.previewId(LEGS)` gives section LEGS.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*OutpouringClientStateTest"`.
- [ ] **Step 3: Implement it.** Build the state, anchors, render types, shader and renderer, then wire the hooks listed under Files.
- [ ] **Step 4: Run the tests and `ShaderReservedWordTest`, and confirm they pass.**
- [ ] **Step 5: Compile with `./gradlew.bat compileJava`.** Expected: BUILD SUCCESSFUL.

### Task 7: World marks

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringMarks.java` (store, pure except the tendency enum)
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringMarkRenderer.java`
- Create shaders:
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_mark.json`
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_mark.vsh`
  - `src/main/resources/assets/hemomancy/shaders/core/world/cast_mark.fsh`
- Modify: `ShaderInit.java`. Add `CAST_MARK` (POSITION_TEX_COLOR, "HemoTime", "Mode", "ColorModulator").
- Modify: `OutpouringRenderTypes.java`. Add `mark(EnumBloodTendency)`: 8 types, depth test LEQUAL, no depth write, polygon offset (`RenderStateShard.POLYGON_OFFSET_LAYERING`).
- Modify: `OutpouringClientState.tick()`. Feed `sim.step` contacts into `OutpouringMarks.add`, filtered by detail: REDUCED keeps only contacts within 1.5 blocks of the feet; VEINS/NONE keeps none. Use the rank's `markLife`, and size `.25 + .35·wildness`.
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringMarksTest.java`

**Interfaces:**
- Produces:
  - `record Mark(int bx, int by, int bz, int face, double u, double v, EnumBloodTendency school, float size, long born, int life, int seed, boolean pool, boolean spark)`. `u/v` are the 0..1 hit position on the face.
  - `static final int CAP = 256`
  - `void add(Mark m)`, `void prune(long now)`, `void clear()`, `List<Mark> live()`
  - `static boolean exposed(int face, boolean neighbourSolid)`, which is true only when the neighbour cell is not solid
  - `static double[] quad(Mark m, long now, float partial)`: 4 corners xyz plus UVs, clamped to the face square and offset .004 along the face normal. A pool's centre drifts along `u + .12·sin(age·.07 + seed)`, `v + .12·cos(age·.05 + seed)`, clamped.
- **Vertex colour:** R = growth `clamp(age / 8)`, G = seed % 256, A = fade (`1 − smoothstep` over the last 30% of life).
- **`cast_mark.fsh` per school:**
  - Animus: quivering blood pool with a ripple and a wet rim.
  - Flammeus: scorch char with dying ember specks.
  - Ductilis: Lichtenberg soot fern, plus a short white flash when spark.
  - Lux: a faint warm glow ring that fades fast.
  - Mortem: green-brown corrosion with pitted noise.
  - Congeatio: frost feather crystals over a red-tinted rime.
  - Ferric: rust-dark iron crust flakes.
  - Tenebris: black soot bloom.

- [ ] **Step 1: Write the failing tests:**
  - `capEvictsOldest`: after 300 adds, `live().size() == 256` and the first added mark is gone.
  - `pruneRemovesExpired`.
  - `clearEmptiesStore`.
  - `marksOnlyOnExposedFaces`: `exposed(f, true) == false`.
  - `quadStaysOnFace`: for a mark at u=.98 and size .5, every corner lies within the block face bounds (±1e-6) and .004 off the face plane.
  - `poolDrifts`: the pool quad centre differs between now and now+20.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*OutpouringMarksTest"`.
- [ ] **Step 3: Implement it.** Build the store, renderer (AFTER_TRANSLUCENT_BLOCKS; skip when the chunk is not loaded or the face is no longer exposed), shader and tick wiring.
- [ ] **Step 4: Run the tests and confirm they pass.** Then run `./gradlew.bat compileJava`.

### Task 8: Lux light and Tenebris shade screen passes

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringScreenPasses.java`, a `@EventBusSubscriber` on `RenderLevelStageEvent` AFTER_LEVEL
- Create shaders:
  - `src/main/resources/assets/hemomancy/shaders/core/screen/cast_screen.vsh` (`gl_Position = vec4(Position.xy, 0, 1)`)
  - `src/main/resources/assets/hemomancy/shaders/core/screen/cast_lumen.json`
  - `src/main/resources/assets/hemomancy/shaders/core/screen/cast_lumen.fsh`
  - `src/main/resources/assets/hemomancy/shaders/core/screen/cast_umbra.json`
  - `src/main/resources/assets/hemomancy/shaders/core/screen/cast_umbra.fsh`
- Modify: `ShaderInit.java`. Add `CAST_LUMEN` and `CAST_UMBRA` (POSITION_TEX, uniforms "HemoTime", "InvProj", "InvView", "LightsA", "LightsB", "Count", "SelfShade").
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/outpouring/OutpouringScreenPassesTest.java`

**Interfaces:**
- Consumes: `OutpouringClientState.luxLights` and `shades`.
- Produces:
  - `static float[] OutpouringScreenPasses.pack(List<double[]> entries)`, a pure helper that returns 32 floats (two column-major mat4s) holding up to 8 `xyz,w` entries, nearest first, with `Count`
  - `static List<double[]> nearest(List<double[]> entries, int max)`
- **Pass flow** (only when the entry list is non-empty):
  1. Lazily create a `TextureTarget scene` (color plus depth), resized with the main target.
  2. Blit the main colour into `scene` (`glBlitFramebuffer`), then `scene.copyDepthFrom(main)`.
  3. `main.bindWrite(false)`, then bind `Sampler0` (scene colour) and `Sampler1` (scene depth).
  4. Set the uniforms: InvProj = `new Matrix4f(event.getProjectionMatrix()).invert()` and InvView = `new Matrix4f(event.getModelViewMatrix()).invert()`.
  5. Disable depth test and blend, draw a full-screen NDC quad, and restore state.
- **`cast_lumen.fsh`:**
  1. Reconstruct the camera-relative position from depth; for sky (depth 1) only add the orb glow.
  2. Get the normal from `cross(dFdx(p), dFdy(p))`.
  3. For each light: `atten = strength / (1 + d²·1.2)`, Lambert with a .35 wrap, colour `mix(vec3(1, .18, .2), vec3(1, .95, .92), pulse)`, where pulse is `.5 + .5·sin(HemoTime·.35 + i)`.
  4. Output `scene + albedo·light·1.4 + light·.08`.
- **`cast_umbra.fsh`:**
  1. Per shade, `dark = smoothstep(radius, radius·.25, |p − c|)`, and the scene is pulled toward `vec3(.02, 0, .04)` by `dark·strength·.85`, with desaturation.
  2. Fog: march 8 steps along the view ray between the entry and exit of the sphere around the shade (clamped by scene depth) with fbm density rolling downward (`p.y + HemoTime·.05`), accumulating black-violet `vec3(.05, .01, .08)`.
  3. When `self` (encoded as negative strength), multiply both by .35 and by `smoothstep(.12, .45, length(uv − .5))`.
- **Shade radius:** `.8 + profile.reach·.6`. Strength is the pulse strength. Lights are the Lux ORB motes with strength `.6 + .4·pulse` and radius 6.

- [ ] **Step 1: Write the failing tests:**
  - `capsAtEightNearest`: 12 entries in, `nearest(…, 8)` keeps the 8 smallest |xyz| in ascending order.
  - `packIsColumnMajor`: entry 0 lands in floats 0..3 and entry 4 in floats 16..19.
  - `emptyPackHasZeroCount`.
- [ ] **Step 2: Run them and confirm they fail.** Run `./gradlew.bat test --tests "*OutpouringScreenPassesTest"`.
- [ ] **Step 3: Implement it.** Build the passes, shaders and registration.
- [ ] **Step 4: Run the tests, `ShaderReservedWordTest` and `compileJava`, and confirm they pass.**

### Task 9: Real-effect hook (built, off)

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/outpouring/OutpouringWorldEffects.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java`. Add `CAST_OUTPOURING_REAL_EFFECT_MIN_RANK` in a `cast_outpouring` push: `defineInRange("realEffectMinRank", -1, -1, 4)`, with the comment "Lowest manipulation rank (0 Humilis .. 4 Perfectus) whose cast outpouring may change the world. -1 keeps outpourings purely visual."
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java:552`. After `CastingAnimationManager.release`, call `OutpouringWorldEffects.release((ServerPlayer) player, this)`.
- Test: `src/test/java/com/vincenthuto/hemomancy/common/manipulation/outpouring/OutpouringWorldEffectsTest.java`

**Interfaces:**
- Produces:
  - `interface Effect { void apply(ServerLevel level, ServerPlayer caster, EnumVeinSections section, EnumManipulationRank rank, List<BlockPos> contacts); }`
  - `static final Effect NONE = (l, c, s, r, k) -> {}`
  - `static boolean allowed(EnumManipulationRank rank, int minRank)` (pure; false when `minRank < 0`)
  - `static Effect effect(EnumBloodTendency school)`, which returns NONE for all eight tendencies
  - `static void release(ServerPlayer caster, BloodManipulation m)`, which reads the config, returns unless `allowed`, samples contacts (exposed solid block tops within `profile.reach` of the feet, at most `profile.maxMarks`) and calls `effect(m.getTend()).apply(...)`
  - The profile values are duplicated as a small server-side table: `reach` = {.6, 1, 2, 3.5, 5} and `maxMarks` = {2, 6, 14, 26, 40}. This keeps common code free of the client package.

- [ ] **Step 1: Write the failing tests:**
  - `neverByDefault`: `allowed(r, -1)` is false for every rank.
  - `gateOpensAtRank`: `allowed(SUMMA, 2)` is true and `allowed(MEDIOCRITAS, 2)` is false.
  - `allEffectsAreNoOps`: `effect(t) == NONE` for all eight.
  - `configDefaultIsNever`: a source check that `HemoServerConfig.java` contains `"realEffectMinRank\", -1, -1, 4"`.
- [ ] **Step 2: Run them and confirm they fail.** Then implement, run them again, and confirm they pass. Run `./gradlew.bat test --tests "*OutpouringWorldEffectsTest"`.

### Task 10: Preview section option, review client and live verification

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/CastingPreviewCommands.java`. Add `Option.SECTION`, `section <EnumVeinSections>`. When it is present, the packet id becomes `OutpouringRules.previewId(section)`; the `PASSIVE_ID` rule still wins for type passive.
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/OutpouringClientReview.java`
- Modify: `build.gradle`. Add an `outpouringReviewClient` run modelled on `brazierReviewClient`: game dir `build/outpouring-client-review`, properties `hemomancy.gameplayDriver=true` and `hemomancy.outpouringReview=true`, username `OutpouringReview`, 1280×720.

**Review scenes** (client tick driver, like `BrazierClientReview`; creative mode; stone floor 11×11 with a stone wall 2 blocks behind and 2 blocks to the left of the player; daytime unless noted; camera `THIRD_PERSON_FRONT`; GUI hidden):
- For each school `t`, and for ranks HUMILIS, SUMMA and PERFECTUS, with section `values()[(t + rankIndex) % 5]`:
  1. Preview CHARGED OPENING with charge .5, then shoot `"<school>-<rank>-<section>-build"` at +8 ticks.
  2. Preview RELEASE and shoot `-peak` at +3 ticks and `-after` at +20 ticks.
  3. Wait 40 ticks before the next scene.
- Then a section sweep: ANIMUS SUMMA RELEASE for each of the 5 sections, `-peak`.
- Night: LUX PERFECTUS and TENEBRIS PERFECTUS, `-peak` and `-after`.
- First person: LUX PERFECTUS ARMS and TENEBRIS PERFECTUS BODY, `-fp-peak`.
- Armour: a full iron set equipped, CONGEATIO SUMMA BODY, `-armour-peak`.
- Settings: particles MINIMAL with FERRIC PERFECTUS, `-minimal-peak`, to confirm veins only.
- Log `OUTPOURING_REVIEW captured <name>` per shot. Stop the client at the end.

- [ ] **Step 1: Implement the preview option, review driver and run config.**
- [ ] **Step 2: Run the build gate.** Run `./gradlew.bat build test`. Expected: BUILD SUCCESSFUL. All JVM tests pass, including every `Outpouring*`, `SkinBodyAtlas`, `VeinEncoding`, `StillArt*` and `ShaderReservedWordTest` test.
- [ ] **Step 3: Prove the shaders compile on the GPU.** Run `./gradlew.bat runSuccessionReviewClient`, wait for a fresh `Sound engine started`, and grep `build/succession-client/logs/latest.log` for `Couldn't compile`. Expected: no match. Stop only the client `java.exe`.
- [ ] **Step 4: Live capture.** Run `./gradlew.bat outpouringReviewClient`. After `"kind":"startup"`, drop `{"op":"create","name":"OutpouringReview","seed":1}` into `build/outpouring-client-review/operator/inbox/`. Wait for the last `OUTPOURING_REVIEW captured`.
- [ ] **Step 5: Inspect every screenshot.** Fix what fails these checks, then re-run from Step 2 as needed:
  - Blood leaves the named section.
  - The look per tendency matches the spec.
  - Nothing floats behind the player.
  - First person stays readable.
  - Armour veins show.
  - MINIMAL shows veins only.
- [ ] **Step 6: Run `./gradlew.bat alphaCheck`.** Expected: BUILD SUCCESSFUL.

### Task 11: Docs

**Files:**
- Modify: `wiki/Casting-Animations.md`. Replace "Anatomy and effects" (around lines 225–227), the Lux wings column (around line 49), the rank-scope notes (around line 42) and the rendering class table (around line 264) with the outpouring description and its classes.
- Modify: `wiki/Casting-Animation-Validation.md`. Drop the Lux wings revision note and add the outpouring review run and its shot list.
- Modify: `docs/MANIPULATION_VISUAL_CATALOGUE.md`. Replace the `CastingAnatomyRenderer`/`LuxCastingWings` row (around line 40) with `OutpouringRenderer`/`CastVeinsLayer`/`OutpouringScreenPasses`.
- Modify: `docs/HEMOMANCY_REFERENCE.md`, wherever casting presentation is described (grep `anatomy|wings|casting`).
- Modify: the `docs/superpowers/specs/2026-10-19-casting-animations-v2-design.md` and `docs/superpowers/plans/2026-10-19-casting-animations-v2.md` headers. Add one line: "Caster-body effects (hand veins, anatomy constructs, Lux wings) were replaced by the Cast Outpouring, 2026-10-19-harbinger-cast-outpouring-design.md."
- Modify: `docs/MANIPULATION_VISUALS.md`. Add a short "Casting outpouring" note: casting effects come from the caster's blood at the vascular section, with no detached constructs.

- [ ] **Step 1: Make the doc edits.**
- [ ] **Step 2: Run `./gradlew.bat test --tests "*CastingDocsSourceTest"`.** Expected: PASS. Update that test's expectations if they pin the removed wording.
