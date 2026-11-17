# Circus Phantom Show Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Circus perception cues (figure planes, cloth, diamonds, NPC shake) with a phantom show: a translucent audience and performers built from the troupe's own models, string lights, spotlights and a darkening tent, staged Disturbed → Acclimating → Attuned, with a hollow echo in a ruined pavilion.

**Architecture:** Pure rules (`PhantomShowRules`, `Catenary`) decide every number per stage and are unit-tested. `CircusPerceptionOverlay` keeps the synced state and the two-second scan, now producing a `ShowMarks` record, and hands the world pass to `PhantomShow`, which draws lights, spotlights and phantoms. Fog is one `ViewportEvent` presentation. The ruin flag is one boolean added to the existing sync payload.

**Tech Stack:** NeoForge 1.21.1 client rendering (PoseStack, RenderType, HumanoidModel parts), JUnit 5, the `circusReviewClient` for stills.

**Spec:** `docs/superpowers/specs/2026-11-16-circus-phantom-show-design.md`

## Global Constraints

- Client-only presentation; the server never reads it. The only server change is the `ruined` boolean on `PacketSyncCircusPerception` (bump `PacketHandler.PROTOCOL_VERSION`, rewrite `src/test/resources/network/payload-registry.txt`).
- World drawing only from `WorldEffectDispatcher` (existing `CircusPerceptionOverlay.renderWorld` call); HUD only in the existing layer; time from `AnimationTime`; easing `HemoEasing`; render types static constants (`RenderTypeCachingGuardrailTest`); particles `HemoParticleData` only.
- Fog far plane never under 32 blocks; scaled by `options.darknessEffectScale()`.
- Phantoms: never opaque; fade within ~2 blocks of the viewer; fade while overlapping a living entity other than the viewer; Disturbed audience fades within ~20° of the crosshair.
- Nothing drawn when Unaware, when `inside` is false, or when the scan found no ring curb.
- Ruin: Disturbed look whatever the stage, no performers. Tenancy: normal.
- No `Component.literal`, no vanilla particles, no new wall-clock animation.

## Review Focus

1. Stage changes mid-view (score crosses a threshold): phantoms and lights cross-fade, never pop. → Task 1 test `lookBlendsBetweenStages`.
2. Player leaves the tent / logs out / changes dimension: fog and phantoms drop at once. → Task 6 test `fogInactiveOutside` and `clear()` resets marks (Task 2 test `clearDropsMarks`).
3. A pavilion with poles but a missing anchor partner (ruin, player edits): a light run with one end is skipped, not drawn to the origin. → Task 3 test `runsNeedTwoAnchors`.
4. Darkness slider at 0: no fog change at all. → Task 6 test `darknessScaleZeroLeavesFog`.
5. `/tick freeze`: every phantom, bulb and spotlight holds still (AnimationTime only). → guardrail `AnimationTimeGuardrailTest` stays green; Task 5 uses only `AnimationTime.gameTime`.

---

### Task 1: The show's rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/circus/PhantomShowRules.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/circus/CircusProgressRules.java` (remove `silhouetteCount`, `clothCount`, `lightCount`, `motionJitter`; keep `motionEchoAlpha` as the Disturbed-only stutter alpha, 0 for the rest)
- Modify: `src/test/java/.../circus/CircusProgressRulesTest.java` (drop the removed asserts; Acclimating echo alpha now 0)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/circus/PhantomShowRulesTest.java`

**Interfaces:**
- Produces:
  - `record Look(float audienceShare, int performers, float bulbLit, float flicker, boolean chase, int spotlights, float house, float vignette, float phantomAlpha, boolean peripheralOnly)` — `house` 0 (no darkening) to 1 (dark).
  - `static Look look(CircusProgressRules.Stage stage, boolean ruined)`
  - `static Look blend(Look from, Look to, float t)` (lerp numbers, booleans and ints from `to` once t ≥ 0.5)
  - `static float peripheralFade(float degreesFromCrosshair)` — 0 at ≤ 20°, 1 at ≥ 35°, smoothstep via `HemoEasing` between.
  - `static float nearFade(double distance)` — 0 at ≤ 1.5, 1 at ≥ 3.
  - `enum Bulb { DEAD, FLICKER, LIT }` and `static Bulb bulb(Look look, long seed)` — seeded: share `1 - bulbLit` dead, `flicker` share flickering.
  - `static float fogFar(float vanillaFar, float house, double darknessScale)` — crossfade toward `max(32, vanillaFar * 0.45)` by `house * darknessScale`.

Values (from the spec table): Disturbed `audienceShare` 0 with 1-3 fragments (`performers` 1, glimpse), `bulbLit` 0.25, `flicker` 0.15, spotlights 0, house 0.2, vignette 0.25, phantomAlpha 0.22, peripheralOnly true. Acclimating 0.33, 1, 0.8, 0.3, false, 1, 0.55, 0.35, 0.32, false. Attuned 1.0, 3, 1.0, 0, true, 3, 0.85, 0.5, 0.42, false. Unaware all zero. Ruined: Disturbed values with `performers` 0, whatever the stage.

- [ ] **Step 1: Write the failing tests** `PhantomShowRulesTest`: `stagesGrowTowardTheFullShow` (audienceShare, spotlights, house strictly increase Disturbed→Attuned; Unaware is all zero), `ruinIsAHollowEcho` (`look(ATTUNED, true)` equals Disturbed but `performers == 0`), `lookBlendsBetweenStages` (blend at 0.5 gives the mean house), `peripheralFadeHidesTheCrosshair` (0 at 10°, 1 at 40°), `nearFadeKeepsPhantomsUntouchable`, `bulbsAreStableAndShared` (same seed same bulb; over 1000 seeds Disturbed dead share within 0.05 of 0.75), `fogNeverUnder32` (`fogFar(48, 1, 1) >= 32`), `darknessScaleZeroLeavesFog` (`fogFar(48, 1, 0) == 48`).
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*PhantomShowRulesTest"` — FAIL (class missing).
- [ ] **Step 3: Implement** `PhantomShowRules` (pure; no `Minecraft` reference) and trim `CircusProgressRules.Stage`, fixing `CircusProgressRulesTest`.
- [ ] **Step 4: Run** the two tests — PASS.

### Task 2: Marks and the ruin flag

**Files:**
- Create: `client/render/world/circus/ShowMarks.java`
- Modify: `client/screen/overlay/CircusPerceptionOverlay.java` (scan fills `ShowMarks`; `setState(int, boolean, boolean ruined)`; remove planes/cloth/diamonds and the bone border)
- Modify: `common/network/circus/PacketSyncCircusPerception.java` + its sender (add `ruined`, from the site's saved state: the pavilion the player is inside has had its ruin laid), `ClientboundStateHandlers`, `PacketHandler.PROTOCOL_VERSION`, `src/test/resources/network/payload-registry.txt`
- Modify: `src/test/java/.../circus/CircusPresentationRegressionTest.java` (asserts on the new names)
- Test: `src/test/java/.../client/render/world/circus/ShowMarksTest.java`

**Interfaces:**
- Produces: `record Seat(BlockPos pos, float yaw, boolean deck)`; `record Anchor(Vec3 pos)`; `record ShowMarks(List<Seat> seats, List<BlockPos> trapezes, List<Anchor> poleTops, List<Vec3> railTops, Vec3 ringCentre, double ringRadius)` with `static ShowMarks EMPTY`, `boolean hasRing()`. Seats from `circus_seating` (yaw from FACING, the way the seat looks) plus gallery decks (as the old figure marks, `deck = true`); pole tops at each pole column's `COLLAR` section, top face; ring centre = mean of curb blocks, radius = mean distance.
- `CircusPerceptionOverlay.marks()`, `isRuined()`, `look(float partialTick)` (blended `Look`; the blend eases over 40 ticks after a stage change).

- [ ] **Step 1: Write failing tests** `ShowMarksTest`: `emptyHasNoRing`, `ringCentreIsTheCurbMean` (via `ShowMarks.ring(List<BlockPos>)` static helper), `clearDropsMarks` (overlay `clear()` → `marks() == ShowMarks.EMPTY`).
- [ ] **Step 2: Run** — FAIL.
- [ ] **Step 3: Implement** marks, scan, payload field and handler; regenerate `payload-registry.txt` with the repo's payload-registry test failure output.
- [ ] **Step 4: Run** `./gradlew.bat test --tests "*ShowMarks*" --tests "*Circus*" --tests "*Payload*" --tests "*PacketHandler*"` — PASS.

### Task 3: String lights

**Files:**
- Create: `client/render/world/circus/Catenary.java`, `client/render/world/circus/StringLights.java`
- Test: `src/test/java/.../client/render/world/circus/CatenaryTest.java`

**Interfaces:**
- Produces: `static List<Vec3> Catenary.points(Vec3 a, Vec3 b, double sagPerBlock, double spacing)` (parabolic sag `sagPerBlock * span`, both ends included); `static List<Run> StringLights.runs(ShowMarks marks)` (each pole top to its nearest two other pole tops, deduped; each gallery rail line chained along its rail tops at +1.1 height; runs whose span > 40 or with fewer than two ends skipped); `static void StringLights.render(PoseStack, Vec3 camera, List<Run>, Look, float time)`.
- Wire: 1/32-block dark crimson line (`0x2A0A10`); bulbs every 0.6 blocks, a warm bone-amber core (`0xFFD9A0`) quad pair plus a soft halo quad, through `HemoRenderTypes.QLIPHOTH_GLOW`; per-bulb `PhantomShowRules.bulb(look, hash(run, index))`; flicker from `sin(time * 0.9 + seed)`; chase brightens a moving band on the act beat (every 80 ticks).

- [ ] **Step 1: Write failing tests** `CatenaryTest`: `endsAreTheAnchors`, `midpointSagsBelowTheChord`, `spacingSetsTheCount` (10-block span, 0.6 spacing → 18 points ± 1), `runsNeedTwoAnchors` (`StringLights.runs` on marks with one pole top gives no run).
- [ ] **Step 2: Run** — FAIL. **Step 3: Implement.** **Step 4: Run** — PASS.

### Task 4: Phantoms

**Files:**
- Create: `client/render/world/circus/PhantomFigures.java` (bakes models once per resource reload, poses and draws), `client/render/world/circus/PhantomPose.java` (pure pose record and pose functions)
- Test: `src/test/java/.../client/render/world/circus/PhantomPoseTest.java`

**Interfaces:**
- Produces: `record PhantomPose(float headYaw, float headPitch, float bodyLean, float rightArmX, float leftArmX, float rightArmZ, float leftArmZ, float legX)`; `static PhantomPose seated(long seed, float time, float lookYaw, boolean applause)`; `static PhantomPose swinging(float phase)` (trapeze: arms up, legs swing); `static PhantomPose ringAct(int act, float time)` (act 0 tumble-ready, 1 throw, 2 flourish); `static double trapezeSwing(float time, long seed)` (pendulum angle, ±40°, period 90 ticks).
- `PhantomFigures.render(PoseStack, Vec3 camera, ShowMarks, Look, float time, boolean ruined)`: models baked from `CircusFacultyModel` subclass layers plus `CircusAcrobatModel.LAYER_LOCATION` and `CircusKnifeThrowerModel.LAYER_LOCATION` via `Minecraft.getEntityModels().bakeLayer`; pose set straight onto `head/body/rightArm/leftArm/rightLeg/leftLeg` (no `setupAnim`, no entity); drawn with a static `RenderType.entityTranslucentEmissive(texture)` per body texture, colour bone `0xE8DAC0` at `phantomAlpha × peripheralFade × nearFade × overlapFade`; the feet fade comes from a second, lower-alpha pass shrunk 0.98 (no shader).
- Audience: `round(audienceShare × seats)` seats chosen by seeded order, stable across frames; Disturbed picks 1-3 seats and moves a fragment to another seat after it has been fully faded for 60 ticks. Performers: Attuned one swinger per trapeze in range (max 3) + one ring act at `ringCentre`; Acclimating one figure alternating trapeze / ring every 400 ticks; Disturbed a single still figure on a trapeze or the ring for 30 ticks at most every 400.
- Overlap fade: living entities within the cue range (except the viewer) gathered once per tick in `CircusPerceptionOverlay.tick`; a phantom fades if its box (0.6 × 1.9) is within 0.8 blocks of one, or one lies within 4° of the line from camera to the phantom and nearer than it.

- [ ] **Step 1: Write failing tests** `PhantomPoseTest`: `seatedVariesBySeed`, `applauseRaisesArms`, `trapezeSwingIsBounded` (|angle| ≤ 40° over a period), `swingIsFrozenTimeStable` (same time → same pose).
- [ ] **Step 2: Run** — FAIL. **Step 3: Implement.** **Step 4: Run** — PASS.

### Task 5: Spotlights, the world pass and the screen

**Files:**
- Create: `client/render/world/circus/Spotlights.java`, `client/render/world/circus/PhantomShow.java`
- Modify: `CircusPerceptionOverlay.renderWorld` (calls `PhantomShow.render`), `CircusPerceptionOverlay.render` (vignette: soft radial gradient of `0x0C0709` at `look.vignette()`, no border), `CircusPerformerRenderer` (remove jitter translate; keep `PerceptionEchoLayer` only while Disturbed, firing a 6-tick stutter at most every 120 ticks per entity, seeded by id)
- Modify: `CircusPerceptionOverlay.tick` (motes: Attuned bone embers drifting down from the bulbs instead of round the player; keep the stage sounds as they are)

**Interfaces:**
- Consumes: Tasks 1-4.
- Produces: `PhantomShow.render(PoseStack, float partialTick)` drawing, in order, spotlights, string lights, phantoms; `Spotlights.render(PoseStack, Vec3 camera, ShowMarks, Look, float time)`: `look.spotlights()` additive cones from 12 blocks above the ring down to the floor, radius 0.4 top → 2.2 bottom, 16 segments, alpha 40 at the axis falling to 0 at the edge, colour `0xFFE6C8`; Acclimating's one swings ±25° with period 300 ticks; Attuned's hold the ring and one follows a swinging trapeze phantom.

- [ ] **Step 1:** Implement and wire.
- [ ] **Step 2: Run** `./gradlew.bat test --tests "*Guardrail*" --tests "*Circus*" --tests "*PhantomShow*" --tests "*Catenary*"` — PASS (HUD, world effect, animation time, render type caching, vanilla particle ratchet).

### Task 6: The darkened tent

**Files:**
- Create: `client/event/CircusShowFog.java` (`@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)`, `ViewportEvent.RenderFog` and `ViewportEvent.ComputeFogColor`)
- Test: `src/test/java/.../client/render/world/circus/PhantomShowRulesTest.java` (`fogInactiveOutside`: `look(UNAWARE,false).house() == 0`)

Behaviour: active only when `CircusPerceptionOverlay.isActive() && marks().hasRing()`; far plane `PhantomShowRules.fogFar`, near plane `far × 0.25`; colour crossfaded toward `0x0A0306` by `house × darknessScale`.

- [ ] **Step 1:** Add the test, run — FAIL. **Step 2:** Implement. **Step 3:** Run — PASS.

### Task 7: Stills, docs

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CircusClientReview.java` `Perception` scene: time set 18000 too (a second pass at night), a gallery camera after the floor camera, and a ruin pass (`CircusSiteStateService` ruin laid on site B, then stage Attuned).
- Modify: `docs/CIRCUS.md` (perception paragraph, the D-29 line, a dated build entry), `docs/HEMOMANCY_REFERENCE.md` (the perception sentence near line 4144), `docs/OPEN_QUESTIONS.md` (the sound pass: crowd murmur and applause), `docs/architecture/evidence/source-text-impact-map.csv` (`python gradle/source-text-impact-map.py`).

- [ ] **Step 1:** Run `./gradlew.bat runCircusReviewClient -PcircusReviewOnly=perception` (operator inbox `create`, then wait for `CIRCUS_REVIEW captured`), inspect the stills of each stage, day, night, floor, gallery, ruin; fix what reads wrong.
- [ ] **Step 2:** Docs and impact map.
- [ ] **Step 3:** `./gradlew.bat test` full — PASS.
