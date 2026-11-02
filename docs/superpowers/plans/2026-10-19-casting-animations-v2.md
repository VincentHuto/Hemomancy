# Casting Animations v2 Implementation Plan

> Caster-body effects (hand veins, anatomy constructs, Lux wings) were replaced by the Cast Outpouring: `docs/superpowers/specs/2026-10-19-harbinger-cast-outpouring-design.md`.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task (the user always wants native execution with one fresh reviewer at the end). Steps use checkbox (`- [ ]`) syntax for tracking. Do not commit; leave work in the working tree unless the user asks for a commit.

**Goal:** Replace the three-pose casting presentation with authored multi-keyframe first- and third-person tracks for every Harbinger school and rank, every cast type and purpose, twenty-one bespoke manipulations, and a new Unstained Still Art lane.

**Architecture:** The server session model (`CastingAnimationManager`, `CastingAnimationPacket`, `CastPhase`) stays and gains a lane. On the client, a pure `CastingTrackRules` samples Java-authored `CastingTrack` records (segments of eased keyframes in both views), an aim layer owns the lead arm and head at release, and bespoke tracks replace whole base tracks. Still Arts publish a RELEASE on success and play stage tracks with ending layers.

**Tech Stack:** NeoForge 1.21.1 / Java 21, existing `VisceralMesh` and `ManipulationMaterials` meshes, JUnit 5 JVM tests, source-contract tests, `src/gametest` GameTests, the two-client casting review fixture.

**Spec:** `docs/superpowers/specs/2026-10-19-casting-animations-v2-design.md`

## Global Constraints

- Six rigid player parts only; armour, sleeves, pants and jacket copy posed parts. No animation library, no new bones.
- Gameplay is never gated or delayed by presentation: quick casts execute first, charged and continuous clocks are the server's.
- Body pitch pivots about the waist and is capped at ±14°. Arm rotation bounds ±130° x, ±95° z.
- First person: no hand in the central 12% of the frame during HOLD; release may cross it for at most 2 ticks; translation clamp ±0.35 horizontal, ±0.25 vertical blocks from vanilla rest; the camera and aim never move.
- Timing budgets per spec §6 (quick release 6–10, recovery 12–18; charged opening 8–12; continuous loop amplitude ≤ 4°, period 48; passive on 6/4/6, off 4/3/5; Still Art release 8–12, settle 14–24). Non-sustained client expiry = 30 ticks; sustained heartbeat window = 40.
- Humilis and Awakened move the main arm only. Perfectus and Enlightened holds have zero body motion (Ductilis snaps excepted).
- No vanilla `ParticleTypes` in any touched file; Harbinger lane adds no particle emitters; Unstained accents use `LUX` material and `HemoParticleData.glow`.
- No copper/brass/bronze in the Harbinger lane. Harbinger copy uses Hematic Order / Sanguine Brotherhood / Crimson Lodge; Unstained copy uses Lethean / still-water language.
- Match the formatting of the file being edited (dense style in `CastingClips`/`CastingPlayerPose`; tabs in `StillArt*`/`ClientEvents`). Pure maths in `*Rules` classes without Minecraft types.
- Presentation metadata is never written to saved manipulation data.

## Review Focus

- A player who switches hands mid-hold (left-handed option, or swaps main-hand item) must not see the pose jump or the wrong hand animate. Test in Task 4 (mirror and hand-role switch mid-track).
- Taking damage during a charge must ease the strain back, not restart or snap the pose. Test in Task 5 (strain is continuous in charge, decreasing charge decreases strain without clock reset).
- Casting while walking, then stopping, must blend the stance in without a leg pop; casting while sprinting must keep vanilla legs. Test in Task 4 (stance weight continuity across speed changes).
- A Still Art refused by the server (cooldown, clarity too low, unknown art) must not animate for observers, and the local flick must cancel within 5 ticks. Tests in Task 12 (GameTest) and Task 13 (client cancel timing).
- A quick cast spammed every tick (Blood Shot) must restart release cleanly without accumulating recovery or moving the torso. Test in Task 2 (new sequence replaces recovery; Humilis quick moves only the main arm).

---

### Task 1: Track model and sampling rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/HandPose.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingTrack.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingTrackRules.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingPose.java` (add `pitch` field, see below)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/CastingTrackRulesTest.java`

**Interfaces:**
- Produces:
  - `record CastingPose(..., float pitch)` — waist pitch in radians, added as a seventh component; `pose(head, lean, twist, rightX, rightZ, leftX, leftZ, stance)` keeps its signature and maps `lean` to `pitch` (body x rotation becomes 0); `blend` and `mirror` include it.
  - `record HandPose(Hand lead, Hand support)` with `record Hand(float tx, float ty, float tz, float rx, float ry, float rz)`; `HandPose.ZERO`, `blend(HandPose, float)`, `mirror()` (swaps lead/support and negates tx, ry, rz).
  - `enum Easing { LINEAR, SMOOTH, EASE_OUT, HOLD }` and `record Keyframe<P>(int tick, P pose, Easing easing)`.
  - `record Segment<P>(List<Keyframe<P>> frames)` with `int duration()` (last tick) and `P sample(int/float time, P zero)`.
  - `record CastingTrack(Segment<CastingPose> opening, hold, strain, release, recovery, cancel, on, off, Loop loop, Segment<HandPose> firstOpening, firstHold, firstStrain, firstRelease, firstRecovery, firstCancel, firstOn, firstOff, Loop firstLoop)` where `record Loop(CastingPose amplitude, HandPose firstAmplitude, int period)`; `strain`, `loop`, `on`, `off` may be null. `CastingTrack.Builder` with fluent `opening(Keyframe...)`, `hold(CastingPose)`, `strain(CastingPose)`, `loop(CastingPose, HandPose, int)`, `release(...)`, `recovery(...)`, `cancel(...)`, `on(...)`, `off(...)`, `first*` equivalents, `build()`.
  - `CastingTrackRules.sample(CastingTrack, CastPhase, float time, float charge, boolean passive)` → `CastingPose`, and `sampleFirst(...)` → `HandPose`. Rules: OPENING samples `opening`; HOLD samples `hold` blended toward `strain` by `charge` when `strain != null`, plus `loop` offset `amplitude * sin(2π·time/period)` when `loop != null`; RELEASE samples `release` starting from the current hold/strain pose; RECOVERY samples `recovery` (must end at ZERO); CANCEL samples `cancel`; when `passive` is true, RELEASE samples `on` and CANCEL samples `off`.
  - `CastingTrackRules.phaseDuration(CastingTrack, CastPhase, boolean passive)` → int.
  - `CastingTrackRules.ease(Easing, float t)`; `HOLD` returns 0 until `t >= 1`.

- [ ] **Step 1: Write the failing tests**

```java
class CastingTrackRulesTest {
    @Test void easingShapes() { assertEquals(0f, ease(HOLD,.99f)); assertEquals(1f, ease(HOLD,1f)); assertEquals(.5f, ease(LINEAR,.5f)); assertTrue(ease(EASE_OUT,.5f)>.5f); assertEquals(.5f, ease(SMOOTH,.5f),1e-6); }
    @Test void segmentInterpolatesBetweenKeyframesAndClampsPastEnd() { /* two keyframes at 0 and 6; sample(3)=midpoint under LINEAR; sample(9)=last */ }
    @Test void holdBlendsTowardStrainByCharge() { /* strain set; charge 0 == hold; charge 1 == strain; charge .5 == midpoint */ }
    @Test void loopOffsetsHoldWithinAmplitudeAndPeriod() { /* |sample(HOLD,t) - hold| <= amplitude for t in 0..200; sample at t and t+period equal */ }
    @Test void passiveUsesOnAndOffSegments() { /* passive=true: RELEASE==on.sample, CANCEL==off.sample */ }
    @Test void recoveryAndCancelEndAtZero() { /* sample(RECOVERY, duration+10)==ZERO; same for CANCEL */ }
    @Test void handPoseMirrorSwapsHandsAndNegatesLateral() { /* mirror().lead == support with -tx,-ry,-rz */ }
    @Test void poseBlendIncludesPitch() { /* pose(...,lean=10,...) has pitch==rad(10) and body.x()==0; blend halves it */ }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*CastingTrackRulesTest'`
Expected: compilation failure (classes missing).

- [ ] **Step 3: Implement `HandPose`, `Keyframe`, `Segment`, `Easing`, `CastingTrack` (+ `Builder`) and `CastingTrackRules` per the Interfaces block; add `pitch` to `CastingPose`**

`Segment.sample` finds the bracketing keyframes, applies the later frame's easing to the local t, and blends with the pose type's `blend`. Keep every type free of Minecraft imports.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew.bat test --tests '*CastingTrackRulesTest' --tests '*CastingClipsTest' --tests '*CastingPoseIntegrationTest'`
Expected: all PASS (existing tests still compile because `pose(...)` keeps its signature).

---

### Task 2: Wire tracks into playback with converted legacy clips and the aim layer

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/CastingTracks.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/LegacyClipTracks.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/AimLayers.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/AimLayerRules.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingPlayback.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingAnimationClientState.java`
- Delete: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingPoseModifiers.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/CastingTracksTest.java`, `AimLayerRulesTest.java`; modify `CastingPlaybackTest.java`, `CastingPoseIntegrationTest.java`; delete `CastingClipsTest.java`

**Interfaces:**
- Consumes: Task 1 types.
- Produces:
  - `CastingTracks.base(EnumBloodTendency school, EnumManipulationRank rank)` → `CastingTrack`; `CastingTracks.style(CastStyle)` → `CastingTrack` or null; `CastingTracks.resolve(CastingAnimationPacket)` → `CastingTrack` (style track if present else base).
  - `LegacyClipTracks.convert(CastingClips.Clip)` → `CastingTrack`: opening = [0 ZERO, 6 opening-pose SMOOTH]; hold = clip.hold; release = [0 hold, 6 release SMOOTH]; recovery = [0 release, 14 ZERO SMOOTH]; cancel = [0 hold, 3 cancelPose, 10 ZERO]; first-person from `firstHold`/`firstRelease` mapped to `HandPose` rotations with zero translation. Used until each school is re-authored (Tasks 6–8) and then deleted with `CastingClips`.
  - `record AimLayer(Keyframe<CastingPose>[] third, Keyframe<HandPose>[] first)` and `AimLayers.purpose(CastPurpose)`.
  - `AimLayerRules.weight(CastPhase, float time, int releaseDuration, int recoveryDuration)` → 0..1: ramps 0→1 over the last 2 ticks of OPENING (callers pass OPENING time relative to its end), 1 through RELEASE, fades 1→0 across RECOVERY, 0 for HOLD and CANCEL. `AimLayerRules.apply(CastingPose base, CastingPose aim, float w, boolean leadIsRight)` replaces only the lead arm and head by `w`; `applyFirst(HandPose base, HandPose aim, float w)` replaces only the lead hand.
  - `CastingPlayback` constructor gains `int opening, int release, int recovery, int cancel` (from `CastingTrackRules.phaseDuration`) and uses them in `phase(now)`, `phaseTime` and `expired` (non-sustained expiry = release + recovery, max 30; cancel expiry = cancel duration).
  - `CastingAnimationClientState.Animation.pose(partial, firstPerson)` keeps its signature; add `handPose(partial)` → `HandPose`. Both resolve the track once per animation (`final CastingTrack track`) and apply the aim layer.

- [ ] **Step 1: Write the failing tests**

```java
class CastingTracksTest {
    @Test void everySchoolAndRankResolvesBothViews() { /* for all 40: base(...) non-null; hold != ZERO; firstHold != ZERO */ }
    @Test void everyTrackIsContinuousAndReturnsToRest() { /* opening.last == hold.first-frame; release.first == hold; recovery.last == ZERO; cancel.last == ZERO; same for first-person */ }
    @Test void humilisMovesOnlyTheMainArmAndKeepsTorsoStill() { /* for all schools at HUMILIS: leftArm==ZERO, body.y==0, pitch==0, legs==ZERO across all phases */ }
    @Test void perfectusHoldsAreStillFor240Ticks() { /* for all schools except DUCTILIS: sample(HOLD,t) equal for t in 0..240 at charge 1 */ }
}
class AimLayerRulesTest {
    @Test void weightRampsHoldsAndFades() { assertEquals(0, weight(HOLD,50,6,14)); assertEquals(1, weight(RELEASE,3,6,14)); assertEquals(0, weight(RECOVERY,14,6,14)); assertEquals(.5f, weight(RECOVERY,7,6,14),1e-6); }
    @Test void applyReplacesLeadArmAndHeadOnly() { /* support arm, body, legs equal base after apply at w=1 */ }
}
```
In `CastingPlaybackTest` add `phaseBoundariesComeFromTrackDurations` (opening 10 → HOLD at tick 10, not 6). In `CastingPoseIntegrationTest` add `newQuickCastReplacesRecoveryWithoutAccumulating` (two RELEASE packets 3 ticks apart: second's phaseTime restarts at 0, pose at tick 3 equals release.sample(3)).

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*CastingTracksTest' --tests '*AimLayerRulesTest' --tests '*CastingPlaybackTest' --tests '*CastingPoseIntegrationTest'`
Expected: FAIL (missing classes / old durations).

- [ ] **Step 3: Implement `LegacyClipTracks`, `CastingTracks`, `AimLayers` (seven purposes from spec §9, third and first person), `AimLayerRules`, and rewire `CastingPlayback` and `CastingAnimationClientState`; delete `CastingPoseModifiers`**

`CastingTracks.style` returns `LegacyClipTracks.style(CastStyle)` converted from the current `CastingPoseModifiers` a/b tables for every non-SCHOOL style, so bespoke styles keep working until Tasks 9–11.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew.bat test --tests '*Casting*Test' --tests '*AimLayer*Test' --tests '*PlayerAnimationPriorityTest'`
Expected: PASS.

- [ ] **Step 5: Smoke the client preview**

Run: `./gradlew.bat runClient`, then `/hemocastpreview animus mediocritas projectile release` and `... hold`.
Expected: the pose plays and the lead arm points forward on release; no exception in the log.

---

### Task 3: Waist pivot and stance continuity

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingPlayerPose.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/WaistPivotRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/WaistPivotRulesTest.java`

**Interfaces:**
- Produces: `WaistPivotRules.offsets(float pitchRadians)` → `float[]{y, z}` model-unit translation for the body part so its bottom edge (12 units below the origin) stays fixed: `y = 12·(1−cos θ)`, `z = −12·sin θ`; `WaistPivotRules.clamp(float pitch)` → ±14° in radians; `WaistPivotRules.headCompensation(float pitch)` → the head x counter-rotation (−θ) so the head keeps its aim; `CastingPlayerPose.stanceWeight` unchanged.

- [ ] **Step 1: Write the failing tests**

```java
class WaistPivotRulesTest {
    @Test void bottomEdgeStaysPutForEveryAllowedPitch() { /* for θ in −14..14° step 1°: rotate (0,12,0) by θ then add offsets → within 0.05 of (0,12,0) */ }
    @Test void pitchIsClamped() { assertEquals(rad(14), clamp(rad(30)), 1e-6); assertEquals(rad(-14), clamp(rad(-30)), 1e-6); }
    @Test void stanceWeightIsContinuousAcrossSpeed() { /* stanceWeight(true,false,v,0) for v in 0..0.1 step .001 never changes by more than .05 between neighbours */ }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*WaistPivotRulesTest'`
Expected: FAIL (class missing).

- [ ] **Step 3: Implement `WaistPivotRules`; in `CastingPlayerPose.apply`, when both arms are available and the player is not in a special pose or using an item, add `pose.pitch()*weight` to `model.body.xRot`, apply `offsets` to `model.body.y/z`, move both arms by the same offsets, counter-rotate the head, and keep the existing arm pivot maths for body yaw**

- [ ] **Step 4: Run tests and the preview**

Run: `./gradlew.bat test --tests '*WaistPivot*' --tests '*CastingPoseIntegrationTest'`; then `/hemocastpreview mortem summa targeted hold` in F5.
Expected: PASS; in F5 the torso leans forward and stays joined to the legs.

---

### Task 4: First-person hand transform and hand-role rules

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/CastingAnimationClientEvents.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/FirstPersonHandRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/FirstPersonHandRulesTest.java`

**Interfaces:**
- Consumes: `CastingAnimationClientState.Animation.handPose(partial)` (Task 2).
- Produces: `FirstPersonHandRules.handFor(HandPose, boolean rightArm, boolean leftHanded)` → `HandPose.Hand` (lead is the main arm; mirrored for left-handed); `FirstPersonHandRules.affectsOffHand(EnumManipulationRank rank, EnumClarityStage stage)` → false for HUMILIS/AWAKENED; `FirstPersonHandRules.clamp(Hand)` → translation within ±0.35 / ±0.25 / ±0.2 blocks; `FirstPersonHandRules.insideCrosshairZone(Hand, boolean right)` → whether the hand's projected centre is within the central 12% band (a pure approximation using tx and ty against the vanilla rest offset (±0.64, −0.6)).

- [ ] **Step 1: Write the failing tests**

```java
class FirstPersonHandRulesTest {
    @Test void leadIsMainArmAndMirrorsForLeftHanded() { /* right-handed: handFor(p,true,false)==p.lead; left-handed: handFor(p,false,true)==p.mirror().lead */ }
    @Test void humilisAndAwakenedNeverTouchTheOffHand() { assertFalse(affectsOffHand(HUMILIS,null)); assertFalse(affectsOffHand(null,AWAKENED)); assertTrue(affectsOffHand(MEDIOCRITAS,null)); }
    @Test void translationIsClamped() { /* tx 1.0 → .35; ty −1 → −.25 */ }
    @Test void switchingHandsMidHoldKeepsTheSameLeadPose() { /* handFor(p,true,false) equals handFor(p,false,true) lead values up to lateral sign */ }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*FirstPersonHandRulesTest'`
Expected: FAIL.

- [ ] **Step 3: Implement the rules; in `CastingAnimationClientEvents.hand` replace the single rotation with: `translate(tx, ty, tz)` then `mulPose` x/y/z from the clamped `Hand`; keep the empty-hand and held-item render paths and the hand anatomy call; use `affectsOffHand` instead of the inline HUMILIS check**

- [ ] **Step 4: Run tests and inspect first person**

Run: `./gradlew.bat test --tests '*FirstPersonHand*'`; then `/hemocastpreview animus mediocritas area hold` in first person, with and without a held sword, and with the left-handed option.
Expected: PASS; both hands move, the crosshair stays clear, the sword follows the hand, left-handed mirrors.

---

### Task 5: Cast-type grammars (strain, loop, passive on/off, local quick flick)

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/CastingAnimationClientState.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java:368-470` (manipulation input)
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/LocalFlickRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/LocalFlickRulesTest.java`; extend `CastingPoseIntegrationTest.java`

**Interfaces:**
- Consumes: `CastingTrackRules.sample(..., charge, passive)` (Task 1); `ManipulationInit.getByName(id).getType()` on the client to detect PASSIVE.
- Produces: `CastingAnimationClientState.flick(CastingAnimationPacket local)` starts a local OPENING session for the local player with `sequence = Long.MAX_VALUE − 1` marker; `LocalFlickRules.shouldCancel(long startedTick, long now, boolean releaseArrived)` → true when `now − started >= 5` and no release arrived; `LocalFlickRules.flickTicks()` → 3. `Animation.pose` passes `playback.charge(now, partial)` and `passive` into the rules.

- [ ] **Step 1: Write the failing tests**

```java
class LocalFlickRulesTest {
    @Test void flickLastsThreeTicksAndCancelsAtFiveWithoutRelease() { assertEquals(3, flickTicks()); assertFalse(shouldCancel(0,4,false)); assertTrue(shouldCancel(0,5,false)); assertFalse(shouldCancel(0,5,true)); }
}
// CastingPoseIntegrationTest additions
@Test void strainFollowsChargeAndEasesBackOnDamageWithoutClockReset() { /* HOLD packets held 10/40 then 20/40 then 10/40 with same started: strain weight .25→.5→.25; phaseTime keeps increasing */ }
@Test void continuousHoldLoopsWithinAmplitude() { /* Magister Animus CONTINUOUS hold: pose(t) − hold ≤ 4° for t in 0..200 and not constant */ }
@Test void passiveOnAndOffGesturesDiffer() { /* RELEASE pose at t=2 != CANCEL pose at t=2 for a PASSIVE id (blackhearted) */ }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*LocalFlickRulesTest' --tests '*CastingPoseIntegrationTest'`
Expected: FAIL.

- [ ] **Step 3: Implement strain/loop/passive plumbing in `Animation.pose`/`handPose`; add `flick` and its cancel in `CastingAnimationClientState.tick`; in `ClientEvents.handleManipulationInput`, on the `CAST` branch for a QUICK selected manipulation call `CastingAnimationClientState.flick(...)` with the selected manipulation's `getCastPresentation()`, tendency and rank, before sending `UseManipKeyPacket`**

- [ ] **Step 4: Run tests and verify in game**

Run: `./gradlew.bat test --tests '*Casting*Test' --tests '*LocalFlick*'`; then in a dev world charge Blood Needle while taking fall damage, channel Sanguine Ward, toggle Blackhearted on and off, and spam Blood Shot.
Expected: PASS; strain eases back on damage; the ward hold breathes; on/off gestures differ; Blood Shot never moves the torso.

---

### Task 6: Author Animus, Flammeus and Ductilis tracks

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/AnimusTracks.java`, `FlammeusTracks.java`, `DuctilisTracks.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/CastingTracks.java`
- Test: extend `CastingTracksTest.java`

**Interfaces:**
- Produces: `AnimusTracks.get(EnumManipulationRank)` → `CastingTrack` (same for the other schools), registered in `CastingTracks.base`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void animusContractsThenOpens() { /* for each rank: lead arm z at opening end < hold z (drawn in) and release end z > hold z (opened); Humilis head.x dips ≥ 2° during opening */ }
@Test void flammeusCoilsBehindThenDrives() { /* opening end lead arm x > −70 (cocked back/up) and release end x <= −95; body.y twist sign flips between opening end and release end from Mediocritas up */ }
@Test void ductilisStepsAreDiscontinuousAndHeld() { /* opening uses HOLD easing: sample(t) constant within each step and jumps at boundaries; at least 2 steps Humilis, 3 from Mediocritas */ }
@Test void magisterStrainIsMonotonicInCharge() { /* for the three schools: |sample(HOLD,charge)−hold| non-decreasing in charge 0..1 */ }
@Test void timingBudgetsHold() { /* release 6..10, recovery 12..18, opening 8..12 for charged-capable ranks */ }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests '*CastingTracksTest'`
Expected: FAIL (legacy conversions do not satisfy the new assertions).

- [ ] **Step 3: Author the fifteen tracks from spec §7.1–7.3 in both views, including `strain` for Magister/Perfectus charged use and `loop` for Magister continuous use (Ductilis loop is a 2-step tic at period 12); register them in `CastingTracks.base`**

- [ ] **Step 4: Run tests and preview every rank**

Run: `./gradlew.bat test --tests '*CastingTracksTest'`; then `/hemocastpreview <school> <rank> projectile hold|release` for all fifteen in both views.
Expected: PASS; each rank reads as a larger version of the same verb.

---

### Task 7: Author Lux and Mortem tracks

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/LuxTracks.java`, `MortemTracks.java`
- Modify: `CastingTracks.java`; Test: extend `CastingTracksTest.java`

- [ ] **Step 1: Write the failing tests**

```java
@Test void luxIsSymmetricAndRises() { /* rightArm.x == leftArm.x and rightArm.z == −leftArm.z at hold from Mediocritas; head.x < 0 (lifted); Perfectus hold arms z == ±90 */ }
@Test void mortemReachesThenHauls() { /* opening end lead x <= −75 and pitch > 0; release end lead x >= −35 (hauled back); Perfectus pitch == rad(10) */ }
```

- [ ] **Step 2: Run tests to verify they fail** — Run: `./gradlew.bat test --tests '*CastingTracksTest'`; Expected: FAIL.

- [ ] **Step 3: Author ten tracks from spec §7.4–7.5 in both views (Lux wings keep their existing Summa+ trigger in `CastingAnatomyRenderer`)**

- [ ] **Step 4: Run tests and preview** — as Task 6 Step 4 for Lux and Mortem; Expected: PASS.

---

### Task 8: Author Congeatio, Ferric and Tenebris tracks, retire legacy clips

**Files:**
- Create: `tracks/CongeatioTracks.java`, `FerricTracks.java`, `TenebrisTracks.java`
- Delete: `client/player/CastingClips.java`, `tracks/LegacyClipTracks.java` (base part; keep style conversions until Task 9 or move them into `BespokeTracks` placeholders)
- Modify: `CastingTracks.java`; Test: extend `CastingTracksTest.java`

- [ ] **Step 1: Write the failing tests**

```java
@Test void congeatioStopsEverythingOnTheSameTick() { /* opening last keyframe: all six parts reach final value at the same tick with EASE_OUT; release at t=0 equals hold then a 1-tick snap; no loop on any rank */ }
@Test void ferricPlantsThenDrivesDownWithAHardStop() { /* opening end lead x <= −105; release end lead x >= −65; stance >= 6 from Mediocritas; release last two keyframes equal (2-tick settle) */ }
@Test void tenebrisTurnsAwayAndUnfoldsLate() { /* hold body.y < 0 (twist away) and head.y > 0 (counter); release: arm z changes sign before body.y returns (arm keyframe tick < body keyframe tick) */ }
@Test void noLegacyClipsRemain() { /* source-contract: client/player has no CastingClips.java */ }
@Test void holdHandsStayOutOfTheCrosshairZoneInEveryBase() { /* all 40: FirstPersonHandRules.insideCrosshairZone(firstHold lead/support) is false; release crosses it for at most 2 consecutive ticks */ }
@Test void everyBaseRespectsRotationAndPitchBounds() { /* all keyframes, all 40: |arm.x| <= 130°, |arm.z| <= 95°, |pitch| <= 14° */ }
```

- [ ] **Step 2: Run tests to verify they fail** — Run: `./gradlew.bat test --tests '*CastingTracksTest'`; Expected: FAIL.

- [ ] **Step 3: Author fifteen tracks from spec §7.6–7.8; delete `CastingClips` and the base legacy conversion**

- [ ] **Step 4: Run the whole casting suite and preview** — Run: `./gradlew.bat test --tests '*Casting*Test' --tests '*AimLayer*' --tests '*WaistPivot*' --tests '*FirstPersonHand*'`; Expected: PASS, 40 authored bases.

---

### Task 9: Re-author the nine existing bespoke styles

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/player/tracks/BespokeTracks.java`
- Modify: `CastingTracks.java` (style lookup), delete remaining `LegacyClipTracks.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/BespokeTracksTest.java`

**Interfaces:**
- Produces: `BespokeTracks.get(CastStyle)` → `CastingTrack` for IRONHEART, UMBRAL_STEP, UMBRAL_REVERSAL, AVATAR, STAFF, CRIMSON_TITHE, UNCLOSING_EYE, BLOOM_OF_ROT, ENDLESS_HOUR.

- [ ] **Step 1: Write the failing tests**

```java
class BespokeTracksTest {
    @Test void everyNonSchoolStyleHasAFullTrackInBothViews() { /* for each CastStyle != SCHOOL: get() non-null, continuity + rest invariants as CastingTracksTest */ }
    @Test void avatarUsesOnOffGestures() { /* AVATAR has on and off segments; on draws arms toward torso (z shrinks), off pushes outward */ }
    @Test void umbralStepAndReversalAreMirroredDepartures() { /* UMBRAL_STEP release body.y == −UMBRAL_REVERSAL release body.y at the same tick */ }
}
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*BespokeTracksTest'`; Expected: FAIL.

- [ ] **Step 3: Author the nine tracks (intent from the current `CastingPoseModifiers` tables, movement grammar from spec §7 and §10) and delete `LegacyClipTracks`**

- [ ] **Step 4: Run tests and preview** — add a `style` argument to the preview in Task 14 if not yet present; for now trigger the real manipulations in a dev world (Ironhearted charge, Umbral Step, Avatar toggle, Conjure Staff, one Saint canon). Expected: PASS and readable in both views.

---

### Task 10: Perfectus bespoke tracks

**Files:**
- Modify: `common/manipulation/animation/CastStyle.java` (add `CRIMSON_CORONATION, SYNAPTIC_STORM, WHITE_VERDICT, FUNERAL_BELL, ABSOLUTE_STILLNESS, LODESTONE_COLLAPSE, ECLIPSE_WELL, PHOENIX_DEBT`)
- Modify: `common/manipulation/animation/CastPresentation.java` (map the eight ids)
- Modify: `tracks/BespokeTracks.java`; Test: extend `BespokeTracksTest.java`, extend `src/gametest/.../CastingAnimationGameTests.java` registry test if it counts styles

- [ ] **Step 1: Write the failing tests**

```java
@Test void perfectusStylesResolveForTheirManipulations() { /* CastPresentation.forId("crimson_coronation").style()==CRIMSON_CORONATION ... all eight */ }
@Test void perfectusBodiesAreStillExceptAuthoredSnaps() { /* HOLD at charge 1 constant for 240 ticks for all except SYNAPTIC_STORM; SYNAPTIC_STORM differs only at step boundaries */ }
@Test void phoenixDebtOnFoldsAndOffThrowsOpen() { /* on end: arms z toward 0; off end: arms z >= ±60 */ }
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*BespokeTracksTest'`; Expected: FAIL.

- [ ] **Step 3: Add the enum constants and id mapping; author the eight tracks from spec §10 Tier A (charge-driven strain for the six charged ones, empty loop for Absolute Stillness, on/off for Phoenix Debt)**

- [ ] **Step 4: Run tests** — Run: `./gradlew.bat test --tests '*BespokeTracksTest' --tests '*Casting*Test'`; Expected: PASS.

---

### Task 11: Gameplay-driven bespoke tracks (Riposte, Rush, Gaze, Marionette)

**Files:**
- Modify: `CastStyle.java` (add `RIPOSTE, BLOOD_RUSH, DEADLY_GAZE, MARIONETTE`), `CastPresentation.java`, `tracks/BespokeTracks.java`
- Test: extend `BespokeTracksTest.java`

- [ ] **Step 1: Write the failing tests**

```java
@Test void riposteIsAGuardWithAShortParrySweep() { /* hold: support arm x <= −60 and z > 20 (across chest); release ≤ 6 ticks; lead arm z sweeps outward by ≥ 25° */ }
@Test void bloodRushRecoversInEightTicks() { assertEquals(8, phaseDuration(get(BLOOD_RUSH), RECOVERY, false)); }
@Test void deadlyGazeKeepsArmsDownAndLeadsWithTheHead() { /* all phases: both arms == ZERO; hold head.x grows with charge; first-person hands == ZERO */ }
@Test void marionetteLoopAlternatesHands() { /* loop amplitude rightArm.x == −leftArm.x; period 48 */ }
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*BespokeTracksTest'`; Expected: FAIL.

- [ ] **Step 3: Add constants and mappings; author the four tracks (Riposte release timed to `HematicRiposteRules` sweep 2–6 ticks; Deadly Gaze first-person returns `HandPose.ZERO`)**

- [ ] **Step 4: Run tests and verify in game** — Run: `./gradlew.bat test --tests '*BespokeTracksTest'`; then parry with Riposte, dash with Blood Rush, charge Deadly Gaze, channel Sanguine Marionette with the crossbar. Expected: PASS; nothing fights the existing Riposte arm, dash or crossbar renderer.

---

### Task 12: Still Art lane on the server

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/animation/CastLane.java` (`HARBINGER, UNSTAINED`)
- Create: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/stillarts/StillArtEnding.java` (`REBUKE, HUSH, STILLNESS, SIGHT, SHEAR, STEP, INTERCESSION`)
- Modify: `common/manipulation/stillarts/StillArt.java` (add `StillArtEnding ending` constructor arg + getter; publish release after a successful cast), `common/init/StillArtInit.java` (endings per spec §11.3; `BLANK` → STILLNESS)
- Modify: `common/network/CastingAnimationPacket.java` (add `CastLane lane`, `EnumClarityStage stage`, `StillArtEnding ending`; `school`, `rank`, `purpose` nullable with presence flags; `at(...)` carries them)
- Modify: `common/manipulation/animation/CastingAnimationManager.java` (add `releaseStillArt(ServerPlayer, StillArt)`, `canPresentStillArt`)
- Modify: `client/event/CastingPreviewCommands.java` (construct packets with `HARBINGER` lane)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/manipulation/stillarts/StillArtPresentationSourceTest.java`; `src/test/java/com/vincenthuto/hemomancy/common/network/CastingAnimationPacketTest.java` (new or extended round trip); GameTest `src/gametest/java/com/vincenthuto/hemomancy/gametest/StillArtAnimationGameTests.java`

**Interfaces:**
- Produces: `CastingAnimationPacket.harbinger(...)` and `CastingAnimationPacket.stillArt(int entityId, UUID, String art, long sequence, EnumClarityStage stage, StillArtEnding ending, CastPhase, long started, long updated, InteractionHand)` factories; `packet.lane()`, `packet.stage()`, `packet.ending()`.
- `CastingAnimationManager.releaseStillArt(ServerPlayer player, StillArt art)` publishes RELEASE once with `stage = art.getRequiredStage()`.
- `StillArt(String name, EnumClarityStage requiredStage, int cooldownTicks, StillArtEnding ending, StillArtAction action)`; the old four-arg constructor is removed (all callers are in `StillArtInit`).

- [ ] **Step 1: Write the failing tests**

```java
class CastingAnimationPacketTest { @Test void stillArtPacketRoundTripsWithNullSchool() { /* encode/decode stillArt(...) → equal; lane UNSTAINED; school null */ } @Test void harbingerPacketStillRoundTrips() { /* existing fields preserved */ } }
class StillArtPresentationSourceTest { @Test void performActionPublishesReleaseAfterSuccess() { /* StillArt.java text contains "CastingAnimationManager.releaseStillArt" after "action.cast(" */ } @Test void everyRegisteredArtDeclaresAnEnding() { /* StillArtInit.java: count of "new StillArt(" == count of "StillArtEnding." */ } }
// GameTest
@GameTest(template="empty") public static void successfulStillArtReleasesOnceAndRefusalsDoNot(GameTestHelper h) { /* fixture player with clarity unlocked + silver_rebuke known: performAction → 1 RELEASE packet with lane UNSTAINED, stage AWAKENED, ending REBUKE; second call on cooldown → still 1; unknown art → 0 */ }
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*CastingAnimationPacketTest' --tests '*StillArtPresentationSourceTest'`; Expected: FAIL / compile error.

- [ ] **Step 3: Implement the enums, constructor, packet fields and factories, manager entry point (reuse `publish`/`ACTIVE`; non-sustained so the existing 20-tick server cleanup applies), and update `StillArtInit`; `canPresentStillArt` = alive, not paralysed, clarity unlocked, art known**

- [ ] **Step 4: Run JVM tests and the GameTest server** — Run: `./gradlew.bat test --tests '*CastingAnimationPacketTest' --tests '*StillArt*Test' --tests '*Casting*Test'` and `./gradlew.bat runCastingAnimationGameTestServer`; Expected: PASS, including the existing casting GameTests.

---

### Task 13: Still Art tracks, endings, client playback and accents

**Files:**
- Create: `tracks/StillArtTracks.java` (`get(EnumClarityStage)`), `tracks/StillArtEndings.java` (`get(StillArtEnding)` → `AimLayer`)
- Modify: `CastingTracks.resolve` (UNSTAINED lane → stage track + ending layer), `CastingAnimationClientState` (lane-aware pose/handPose; `flick` for Still Arts), `client/event/ClientEvents.java:359-363` (local flick before `UseStillArtKeyPacket`), `client/render/layer/player/CastingAnimationLayer.java` and `client/event/CastingAnimationClientEvents.java` (skip school anatomy for UNSTAINED; call pale accents), `client/render/world/CastingAnatomyRenderer.java` (add `stillHand(...)` ring and `stillGround(...)` ring on `ManipulationMaterials.LUX`, colours 0xF4F6F8 / 0xC9D1D9, ground radius 0.6 + 0.25·stage, Minimal removes ground)
- Modify: `common/init/StillArtInit.java` (replace END_ROD / SNOWFLAKE / WAX_OFF with `HemoParticleData.glow` motes sent through the existing particle packet path; gameplay unchanged)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/StillArtTracksTest.java`; extend `StillArtPresentationSourceTest` with `noVanillaParticlesRemain` (`StillArtInit.java` contains no `ParticleTypes.`)

- [ ] **Step 1: Write the failing tests**

```java
class StillArtTracksTest {
    @Test void everyStageHasBothViewsAndSettlesToRest() { /* 5 stages: release 8..12, recovery 14..24, recovery last == ZERO, first-person likewise */ }
    @Test void stillArtsNeverClenchOrThrowTheHeadBack() { /* all keyframes: head.x >= 0 (level or bowed); arm z magnitude never decreases below the previous keyframe within release (opens, never closes) except HUSH/STEP endings */ }
    @Test void awakenedMovesOnlyTheMainArm() { /* leftArm == ZERO, pitch 0, legs ZERO */ }
    @Test void vigilantAndAboveNarrowTheStance() { /* legs z == 0 forced at hold for VIGILANT..ENLIGHTENED */ }
    @Test void enlightenedIsStillAfterSettle() { /* sample(RECOVERY, 24..240) == ZERO; release pose constant across its last 4 ticks */ }
    @Test void everyEndingExists() { /* 7 endings non-null; STEP recovery == 8 */ }
}
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*StillArtTracksTest' --tests '*StillArtPresentationSourceTest'`; Expected: FAIL.

- [ ] **Step 3: Author the five stage tracks and seven endings from spec §11.2–11.3; wire lane resolution, flick, accents and the particle sweep**

- [ ] **Step 4: Run tests and verify in game** — Run: `./gradlew.bat test --tests '*StillArt*' --tests '*Casting*Test'`; then on an Unstained test profile (`/hemo test` fixtures per `docs/TESTING.md`) cast each art in both views and watch a second client. Expected: PASS; each art settles; observers see it; refused casts show only the local flick cancel.

---

### Task 14: Preview command, docs and live review captures

**Files:**
- Modify: `client/event/CastingPreviewCommands.java` (optional `style`, `type quick|charged|continuous|passive`, `charge 0..1`; new `still <stage> <ending> <phase>` form)
- Modify: `wiki/Casting-Animations.md` (rewrite for the grammar; Still Arts section; bespoke list), `wiki/Casting-Animation-Validation.md` (dated entry), `docs/HEMOMANCY_REFERENCE.md` §15.1 and the status row for Still Arts, `docs/done/HEMOMANCY_CASTING_ANIMATION_CONCEPTS/README.md` (pointer), `docs/MANIPULATION_VISUAL_CATALOGUE.md` (Still Art accents)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/player/CastingDocsSourceTest.java`

- [ ] **Step 1: Write the failing test**

```java
class CastingDocsSourceTest {
    @Test void wikiNamesEveryStyleAndEnding() { /* wiki/Casting-Animations.md contains each CastStyle name (title-cased) and each StillArtEnding */ }
    @Test void previewRegistersStillForm() { /* CastingPreviewCommands.java contains literal("still") and "charge" */ }
}
```

- [ ] **Step 2: Run to verify fail** — Run: `./gradlew.bat test --tests '*CastingDocsSourceTest'`; Expected: FAIL.

- [ ] **Step 3: Extend the preview command and write the docs**

- [ ] **Step 4: Full verification**

Run: `./gradlew.bat test --tests '*Casting*Test' --tests '*StillArt*Test' --tests '*AimLayer*' --tests '*WaistPivot*' --tests '*FirstPersonHand*' --tests '*Bespoke*' --tests '*LocalFlick*' --tests '*PlayerAnimationPriorityTest'`, then `./gradlew.bat runCastingAnimationGameTestServer`, then `./gradlew.bat alphaCheck`.
Expected: all PASS.

- [ ] **Step 5: Two-client review captures**

Run: `./gradlew.bat classes gameTestClasses` once, then `runServer`, `runCastingCaster`, `runCastingObserver` with `-I tools/casting-animation-review.init.gradle -PcastingRun=casting-v2` (see `wiki/Casting-Animations.md` for the fixture rules; never a personal world). Extend the fixture sweep to every bespoke style and every Still Art stage.
Expected: screenshots for all 40 holds and releases in both views, 21 bespoke styles, 5 Still Art stages, both skin models, armour, occupied hands, walking, crouching and cancel; inspect them and record the results in `wiki/Casting-Animation-Validation.md`.

- [ ] **Step 6: Final reviewer**

Dispatch one fresh reviewer over the whole working tree against the spec, then address findings.
