# Vesper Fight Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Vesper encounter to the Annetta standard:
- a stable, telegraphed phase 1 with fluid flight, the Burrow attack, the Absorption-drained Wound and the puppet roster, on a regenerated insect mount
- a three-act phase 2 with chained movesets, any-school manipulation casting, an expanded Sickle act and puppet-borrowed attacks

**Architecture:**
- Timing, selection and state rules live in pure `*Rules` classes with JUnit tests.
- Each entity delegates its tick to one controller class (`VesperMountController` for phase 1, `VesperEveningStarController` for phase 2) that owns movement, facing and the move timeline.
- The mount is a generated `HierarchicalModel` (Python rig script plus a hand-written `_anim.java` spliced in). Vesper's Blockbench rider parts are kept and posed in Java.

**Tech Stack:** Java 21, NeoForge 1.21.1 (21.1.251), HutosLib, JUnit 5, NeoForge GameTests, Python 3 + Pillow + numpy (model export).

**Spec:** `docs/superpowers/specs/2026-10-16-vesper-fight-overhaul-design.md`. Read it first; this plan argues from it.

## Global Constraints

- Vesper is male: he/him everywhere.
- NeoForge imports only (`net.neoforged.*`), `DeferredHolder`, `Hemomancy.rloc("snake_case")`. Payloads use `CustomPacketPayload` + `TYPE` + `STREAM_CODEC`, registered in `PacketHandler`.
- Visuals use the Hemomancy/HutosLib suite (`VesperVisualEffects`, `HemoParticleData`, `ManipulationVisuals`, claw/tendril/lightning payloads). There are no vanilla particle clouds at focal moments. Read `docs/MANIPULATION_VISUALS.md` before any focal visual.
- No copper, brass or bronze on the mount. Use chitin, bone, lacquer, hematic iron and the red/black palette.
- Never `setNoAi(true)` on either Vesper. Clear a persisted `NoAI` on load.
- Phase 1 thresholds: wounds at **72% / 42% / 12%** health; wings at **50%**.
- Burrow hunt: **10–15 s** (200–300 ticks) on the ground, **5–7 s** (100–140 ticks) from a dive. Recovery: **3–4 s**.
- Roster: **7 shapes** (Veinwing Vulture, Marrow Spitter, Gorebound Hulk, Scarlet Mummer, Sanguine Hound, Cinder Bellows, Mnemonist Puppet). **Ringmaster Pattern excluded.** **3 out max.**
- Phase 2 acts: **Locked 100%→67%**, **Open 67%→33%**, **Sickles 33%→0**. Harvest once, below **10%**.
- Don't change ordeal entry, retry, owner binding, victory, Memory of Vesper delivery, Bloom seal or Silent Archon finalization.
- Old saves load into a sane state: IDLE, grounded, no burrow; a legacy `ActiveThroneAnchor` becomes an open wound.
- **Commits:** the user's standing rule is *no auto-commit*. Leave all work in the working tree; "Commit" steps below mean "stop, run the checks, leave uncommitted".
- Parallel sessions share build dirs. A mid-run `NoClassDefFoundError` is usually a build collision: rerun before debugging.
- Run Gradle from the project root on Windows: `./gradlew.bat ...`.

## Review Focus

1. **Save/quit mid-state** (mid-burrow, mid-wound, mid-flight, mid-muster, mid-sickle-reel) must reload without a stuck boss. Each state's rules class owns an NBT round-trip test, and Task 23 adds a GameTest save/reload fixture.
2. **Owner death, logout or exit with puppets out**: the puppets unspool, the roster isn't corrupted, and a retry spawns no strays. Covered in Task 15 (rules) and Task 23 (GameTest).
3. **No valid target at a lock moment** (target dead, creative or spectator during a rumble lock or reel): the move resolves to its recovery without stalling. Every timeline's tests include a `targetLost` case.
4. **No way to drain a wound** (no Absorption tool, all puppets struck): it must not soft-lock. Direct hits drain at **10%** of the Absorption rate as a fallback (plan call, reported to the user). Task 12 tests this.
5. **Arena edges and multiple players**: the burrow trail, eruption point and flight points are clamped to the safe half-extent. Shake and Absorption are limited to the arena or ordeal owner. Tests are in Tasks 7, 8 and 12.

---

## File Structure

**Phase 1 — common (`common/entity/boss/endgame/`)**
- `VesperMoveTimeline.java` — record `(int windup, int active, int recovery)` plus a stage lookup.
- `VesperMountControllerRules.java` — interrupt policy, turn-rate limiting, aim-lock tick and patrol rules (pure).
- `VesperMountController.java` — server tick owner for the Crowned Refusal: locomotion, facing and move timeline.
- `VesperMountMoves.java` — the ground move bodies, moved out of `EndgameBossActions` (Scuttle, Pincer, Stinger, Trample, Aneurysm, Grab, Muster, Recall).
- `VesperFlightSteeringRules.java` — velocity steering, bank and pitch (pure). Extends or replaces the steering half of `VesperWingedFlightRules`.
- `VesperBurrowRules.java` + `VesperBurrow.java` — burrow stages (pure) and the server state/behavior.
- `VesperWoundRules.java` + `VesperWound.java` — wound meter rules (pure) and the server state. Replaces the anchor exposure in the entity. `VesperThroneAnchorPart` becomes the wound hit part.
- `VesperPuppetRoster.java` — roster state and scheduling (pure, NBT).

**Phase 2 — common**
- `VesperEveningStarAct.java` — enum `LOCKED, OPEN, SICKLES` plus thresholds.
- `VesperEveningStarController.java` — replaces `VesperPhaseTwoCombat.tick/tickRage` selection.
- `VesperChainGraph.java` — pose-linked action graph and branch selection (pure).
- `VesperSickleRules.java` — sickle strings, momentum, reel break and Harvest (pure).
- `VesperManipulationCaster.java` + `VesperManipulationCoverage.java` — boss casting and the coverage table.
- `VesperSynergyChains.java` — authored cross-school chains (pure data).
- `VesperBorrowedAttacks.java` — the seven puppet-borrowed attacks.
- `VesperPhaseTwoCombat.java` — keeps the action bodies (hit helpers, visuals). Its selection logic moves out.

**Client**
- `client/model/entity/boss/endgame/VesperMountModel.java` — **generated**; never hand-edit.
- `client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java` — becomes the rider only: drop `lowerBody` and the authored mount clips, add Java rider poses.
- `client/model/entity/boss/endgame/VesperRiderMotion.java` and `VesperEveningStarMotion.java` — Java procedural/keyframe motion.
- `client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java` — renders the mount model as the main model, plus `VesperRiderLayer`.
- `client/render/layer/mob/endgame/VesperRiderLayer.java`, `VesperWoundLayer.java` (replaces `VesperThroneAnchorLayer`), `VesperMountWingMembraneLayer.java` (if the membranes are procedural).
- `client/render/world/chamberofwill/VesperBurrowFissureRenderer.java` and `client/data/VesperBurrowClientData.java`.
- `client/data/VesperRumbleClientState.java` + its camera hook.

**Tools:** `tools/model_export/vesper_mount.py`, `tools/model_export/vesper_mount_anim.java`, and a `package` parameter added to `annetta_kit.entity_java_source`.

**Network:** `common/network/boss/VesperRumblePayload.java` and `VesperBurrowTrailPayload.java`.

**Resources:**
- `textures/entity/boss/endgame/vesper_mount.png` (+ `_membrane.png`)
- `data/hemomancy/damage_type/vesper_rend.json`, plus the `minecraft:bypasses_armor` tag entry
- lang keys

**GameTest:** `src/gameTest/java/com/vincenthuto/hemomancy/gametest/VesperClientReview.java`, plus a `vesperReviewClient` run in `build.gradle`.

---

## Part A — Phase 1 stability

### Task 1: Move timelines and controller rules

**Files:**
- Create: `common/entity/boss/endgame/VesperMoveTimeline.java`
- Create: `common/entity/boss/endgame/VesperMountControllerRules.java`
- Test: `src/test/java/.../endgame/VesperMountControllerRulesTest.java`

**Interfaces:**
- Produces:
  - `record VesperMoveTimeline(int windup, int active, int recovery)`, with `Stage stageAt(int tick)` (`WINDUP, ACTIVE, RECOVERY, COMPLETE`), `int total()` and `int commitTick()` (= `windup`).
  - `VesperMoveTimeline VesperMountControllerRules.timeline(VesperPhaseOneAttack)`.
  - `enum Interrupt { TAKE_NOW, STAGGER, QUEUE }` and `Interrupt VesperMountControllerRules.interrupt(Stage current, Priority request)`, where `enum Priority { WOUND, WING_GROWTH, TRANSITION, BURROW_SURFACE }`.
  - `float VesperMountControllerRules.turnToward(float currentYaw, float desiredYaw, float maxStep)`.
  - `boolean VesperMountControllerRules.tracksTarget(Stage stage)` — true only in WINDUP.
  - `int VesperMountControllerRules.STAGGER_TICKS = 14`.

- [ ] **Step 1: Write failing tests**

```java
@Test void timelinesCoverEveryGroundAttackAndEndAfterRecovery() {
    for (VesperPhaseOneAttack a : VesperPhaseOneAttack.values()) {
        if (a == VesperPhaseOneAttack.IDLE) continue;
        VesperMoveTimeline t = VesperMountControllerRules.timeline(a);
        assertTrue(t.windup() >= 10, a + " needs a readable wind-up");
        assertEquals(VesperMoveTimeline.Stage.COMPLETE, t.stageAt(t.total()));
    }
}
@Test void interruptsNeverCutAnActiveSwing() {
    for (var p : VesperMountControllerRules.Priority.values())
        assertEquals(VesperMountControllerRules.Interrupt.QUEUE,
            VesperMountControllerRules.interrupt(VesperMoveTimeline.Stage.ACTIVE, p));
    assertEquals(VesperMountControllerRules.Interrupt.STAGGER,
        VesperMountControllerRules.interrupt(VesperMoveTimeline.Stage.WINDUP, VesperMountControllerRules.Priority.WOUND));
    assertEquals(VesperMountControllerRules.Interrupt.TAKE_NOW,
        VesperMountControllerRules.interrupt(VesperMoveTimeline.Stage.RECOVERY, VesperMountControllerRules.Priority.WOUND));
}
@Test void turnTowardIsRateLimitedAndWrapsShortWay() {
    assertEquals(10.0F, VesperMountControllerRules.turnToward(0F, 90F, 10F), 1e-4);
    assertEquals(-10.0F, VesperMountControllerRules.turnToward(0F, 350F, 10F), 1e-4);
    assertEquals(45.0F, VesperMountControllerRules.turnToward(40F, 45F, 10F), 1e-4);
}
@Test void aimTracksOnlyDuringWindup() {
    assertTrue(VesperMountControllerRules.tracksTarget(VesperMoveTimeline.Stage.WINDUP));
    assertFalse(VesperMountControllerRules.tracksTarget(VesperMoveTimeline.Stage.ACTIVE));
}
```

- [ ] **Step 2:** `./gradlew.bat test --tests "*VesperMountControllerRulesTest"` → FAIL (classes missing).
- [ ] **Step 3: Implement.** Timelines must preserve today's impact ticks: Scuttle commit 25, Pincer 24/38, Trample 22/38, Muster 32, Aneurysm 24 (brace) / 105 (reform end) / 120, Grab 15/42/70. Wind-up = ticks before the first impact; recovery = the remainder.
- [ ] **Step 4:** Rerun → PASS.
- [ ] **Step 5:** Leave uncommitted.

### Task 2: Mount controller replaces the vanilla goals

**Files:**
- Create: `common/entity/boss/endgame/VesperMountController.java`, `common/entity/boss/endgame/VesperMountMoves.java`
- Modify: `VesperTheCrownedRefusalEntity.java` (`registerGoals` → keep only `FloatGoal` and the two target selectors; `tickEncounter` delegates to the controller; delete the `setNoAi` calls; in `readAdditionalSaveData`, `setNoAi(false)`), `EndgameBossActions.java` (move the phase-one bodies out; keep the Mycophant code)
- Test: `VesperMountControllerRulesTest` (patrol and target loss); update `VesperCombatImplementationSourceTest`

**Interfaces:**
- Consumes: Task 1.
- Produces:
  - `VesperMountController(VesperTheCrownedRefusalEntity)` with `void tick()`, `void request(Priority)`, `boolean busy()`, `Vec3 lockedAim()`.
  - `VesperMountMoves.tick(VesperTheCrownedRefusalEntity, VesperPhaseOneAttack, int tick, Stage stage, Vec3 lockedAim)` → `void`. The controller owns completion.
  - `VesperMountControllerRules.patrolPoint(double cx, double cz, long gameTime)` → `double[2]`.

- [ ] **Step 1:** Add tests:
  - `patrolPointStaysInsideSafeHalfExtent` — for 200 sample times, `|x-cx|,|z-cz| <= 21`
  - `targetLossFinishesMoveThenPatrols` — a pure `nextState(Stage, boolean hasTarget)` returns `RECOVERY_THEN_PATROL` when the target is lost in ACTIVE
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3: Implement the controller.**
  - Locomotion via `getMoveControl().setWantedPosition` (approach / strafe / hold), issued only by the controller.
  - Facing via `turnToward` at a max of 9°/tick on the ground.
  - The move timeline uses `timeline()`. The aim locks at `commitTick`. A missed grab plays its own whiff recovery.
  - Interrupts (wound, wing growth, transition) go through `request()` and are honored per `interrupt()`.
  - Stagger plays `STAGGER_TICKS` of recovery, synced as attack `IDLE` plus a new `DATA_STAGGER_TICK`.
- [ ] **Step 4:** Update the source test so it asserts `!crowned.contains("setNoAi(true)")` and that the entity delegates to `VesperMountController`. Remove the assertion that expects `setNoAi(true)`.
- [ ] **Step 5:** `./gradlew.bat test --tests "*Vesper*"` → PASS. Then `./gradlew.bat build` → BUILD SUCCESSFUL.

### Task 3: Telegraph pass for ground moves

**Files:**
- Modify: `VesperMountMoves.java`, `VesperVisualEffects.java` (add `floorFissure(ServerLevel, Vec3 from, Vec3 to, ParticleColor)` and `spawnMarker(ServerLevel, Vec3, ParticleColor)` built on blood-cell and dark-glow motes on the floor plane)
- Modify: `SoundInit` only if a cue is missing. Reuse the existing `ENTITY_VESPER_*` sounds first.
- Test: `VesperMountControllerRulesTest.everyGroundMoveHasATelegraphCue()` — the pure `VesperMountControllerRules.telegraph(VesperPhaseOneAttack)` returns non-`NONE` for every non-IDLE attack.

- [ ] **Step 1:** Write the test, using `enum Telegraph { NONE, LINE, RING, CONE, MARKERS, STRINGS }`.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3: Implement the telegraphs:**

  | Move | Telegraph |
  |---|---|
  | Scuttle | LINE |
  | Pincer | CONE (110°, 5.2) |
  | Stinger | MARKERS — each spike's spawn point glows for 12 ticks before the spike |
  | Trample | RING (5.5) |
  | Aneurysm | RING |
  | Grab | LINE |
  | Muster | STRINGS — strings descend at each puppet's landing point |

  Each move plays a wind-up sound at tick 1.
- [ ] **Step 4:** Run → PASS. `./gradlew.bat build`.

---

## Part B — Mount model

### Task 4: Generated mount rig, texture and preview

**Files:**
- Modify: `tools/model_export/annetta_kit.py` — add `package='com.vincenthuto.hemomancy.client.model.entity.boss.annetta'` and `entity_import` parameters to `entity_java_source`. Defaults keep Annetta byte-identical.
- Create: `tools/model_export/vesper_mount.py`, `tools/model_export/vesper_mount_anim.java`
- Generates: `client/model/entity/boss/endgame/VesperMountModel.java`, `textures/entity/boss/endgame/vesper_mount.png`, `vesper_mount_membrane.png`, `build/vesper_mount_preview.png`

**Interfaces:**
- Produces model parts, used by later tasks by exact name:
  - `root, body, thorax, neck, head, mandibleL, mandibleR, seat`
  - `clawArmL/R → clawUpperL/R → clawForeL/R → pincerL/R → pincerFingerL/R`
  - legs `leg{L,R}{0,1,2} → coxa → femur → tibia → tarsus`
  - `abdomen → tail0..tail5 → stinger`
  - wings `wingFore{L,R}, wingHind{L,R}`, each `→ wingRoot → wingSpar`
  - wound sockets `woundCarapace, woundNeck, woundTail`
  - `burrowNeckGrip`
- Produces a Java API, spliced from `_anim.java`:
  - `void setupAnim(...)`
  - `void translateToSeat(PoseStack)`
  - `void translateToSocket(PoseStack, Socket)`, where `enum Socket { SEAT, NECK_GRIP, WOUND_CARAPACE, WOUND_NECK, WOUND_TAIL, CLAW_L, CLAW_R, STINGER }`

- [ ] **Step 1: Kit change.** Run `python tools/model_export/annetta_knowles.py`, then `git diff --stat src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta` → no changes.
- [ ] **Step 2: Rig.** Write the rig in `vesper_mount.py`. Keep the current footprint: body ~3.5 blocks long and ~4.5 wide across the legs, seat height matching the current rider pivot (read `vesper` part offsets from the current model). Add:
  - a raised thorax with a **neck and head** between the claw arms (the head is new: carapace plates, mandibles, eye pits)
  - six four-segment legs
  - two grasping claw arms (pincer plus finger, matching the current `fLeftArm` reach)
  - a six-segment tail with stinger prongs
  - **two pairs of wings** whose spars span ≥ 2.2× body width when flared
  - wing membranes as flat 0-thickness planes, textured from `vesper_mount_membrane.png` and rendered translucent
- [ ] **Step 3: Palette and painters.** Chitin black-red, lacquer red edges, bone joints, dark membrane veins. No copper.
- [ ] **Step 4:** `python tools/model_export/vesper_mount.py --preview`. Inspect the preview, then **send `build/vesper_mount_preview.png` to the user** (side / front / top; wings folded, flared and beating). Iterate on their notes before Task 5. This is a user checkpoint.
- [ ] **Step 5:** `./gradlew.bat compileJava` → success.

### Task 5: Split renderer and Java animation (mount and rider)

**Files:**
- Modify: `VesperTheCrownedRefusalRenderer.java` (main model = `VesperMountModel`, texture `vesper_mount.png`; add `VesperRiderLayer`, `VesperWoundLayer` (stub until Task 13) and the cocoon render; drop `VesperThroneAnchorLayer`)
- Modify: `VesperTheCrownedRefusalModel.java` (rider only: delete `lowerBody` and its parts from `createBodyLayer`; delete the authored mount clips; `setupAnim` calls `VesperRiderMotion`)
- Create: `VesperRiderLayer.java`, `VesperRiderMotion.java`
- Modify: `VesperCrownedWeaponLayer.java` and `VesperMountAbsorptionLayer.java` (retarget to the rider layer's transform chain: mount seat → rider)
- Modify: `client/event/LayerEvents.java` (register `VesperMountModel.LAYER_LOCATION`)
- Modify: `VesperTheCrownedRefusalAnimations.java` (keep `TRANSFORMATION` and any rider-only clips still referenced; delete the rest)
- Modify: `VesperTheCrownedRefusalEntity.java` (synced `DATA_BANK`, `DATA_PITCH` floats for flight; set in Task 6)
- Test: `src/test/java/.../client/model/entity/boss/endgame/VesperMountMotionRulesTest.java` for the pure gait math in `VesperMountMotionRules`

**Interfaces:**
- Produces:
  - `VesperMountMotionRules.tripodPhase(int leg, float cycle)` → float in [0,1) — legs L0, R1 and L2 share a phase; the others are offset by 0.5.
  - `VesperMountMotionRules.footLift(float phase)` → float ≥ 0, and 0 during stance.
  - `VesperMountMotionRules.overshoot(float t)` — a damped settle curve.
  - `VesperRiderMotion.Pose` enum `{ SEATED, NECK_GRIP_TUCK, CROSSBAR_RAISE, WOUND_RECOIL, FLIGHT_LEAN, STAGGER }`.

- [ ] **Step 1: Write failing tests:**
  - `tripodLegsAlternate` — `tripodPhase(0,c) == tripodPhase(3,c)`, and differs from `tripodPhase(1,c)` by 0.5
  - `footLiftsOnlyInSwing` — lift is 0 for phase ≥ 0.5
  - `overshootSettlesToOne` — `overshoot(1)≈1` and `max(overshoot(0..1)) > 1`
- [ ] **Step 2:** Run → FAIL. Implement the rules. Run → PASS.
- [ ] **Step 3: Write `vesper_mount_anim.java`** (spliced by the script), driven from entity state:
  - **Walk:** tripod gait from `limbSwing`, with coxa swing, femur lift, tibia fold and a tarsus hook, plus overshoot on plant.
  - **Idle:** continual small motion — antenna/mandible twitch, claw-finger flex, tail sway, leg weight shifts.
  - **Attacks:** per-attack curves keyed off the Task 1 timelines (stage and progress).
  - **Wings:** folded flat on the back when grounded; a flare and shiver during `WING_GROWTH`; a fore/hind offset beat in flight (hind wings 0.25 cycle behind); cupped on `DIVE_TELEGRAPH`; swept back on `DIVING_REND`.
  - **Bank and pitch** applied to `body`.
- [ ] **Step 4: Rider poses in `VesperRiderMotion`** (procedural, Java). Blend by an eased weight from entity state: neck-grip tuck during the burrow, Crossbar raise during muster/recall, wound recoil, flight lean (from bank/pitch), stagger.
- [ ] **Step 5:** Regenerate the model. `./gradlew.bat build` → success.
- [ ] **Step 6:** `./gradlew.bat runClient` smoke test (`/summon hemomancy:vesper_crowned_refusal`): the mount renders, the rider sits in the seat, and the weapons are in his hands. Full visual review is in Task 23.

---

## Part C — Flight

### Task 6: Velocity steering, banking and committed dives

**Files:**
- Create: `VesperFlightSteeringRules.java`
- Modify: `VesperTheCrownedRefusalEntity.java` (flight ticks call the steering), `VesperWingedFlightRules.java` (remove `steerToward`-style point snapping from the callers; keep the timings and clamps)
- Test: `VesperFlightSteeringRulesTest.java`

**Interfaces:**
- Produces:
  - `record Steer(Vec3 velocity, float yaw, float bank, float pitch)`
  - `Steer VesperFlightSteeringRules.step(Vec3 pos, Vec3 vel, float yaw, Vec3 desired, double maxSpeed, double accel, float maxTurnDeg)`
  - `Vec3 VesperFlightSteeringRules.circlePoint(Vec3 target, double radius, double angle, double altitude)`
  - `float BANK_MAX = 35F`, `float PITCH_MAX = 40F`

- [ ] **Step 1: Write failing tests:**
  - `speedNeverExceedsMax`
  - `yawChangesAtMostMaxTurnPerStep`
  - `bankSignFollowsTurnDirectionAndIsClamped` (|bank| ≤ 35)
  - `pitchNegativeWhenDescending`
  - `velocityEasesTowardDesired` — no instant reversal: the dot of successive velocities is > 0 when the desired point flips behind
- [ ] **Step 2:** Run → FAIL. Implement. Run → PASS.
- [ ] **Step 3: Wire into the entity.**
  - **Circling:** follow smooth arcs: `circlePoint` advanced by angular speed, with the desired point fed to `step`.
  - **Tail Fusillade:** a strafing pass across the locked aim, firing at the existing ticks 12/16/20 along the pass.
  - **Dive telegraph:** hover-brake, then a committed straight dive (velocity locked).
  - **Landing:** a flared, wing-braked touchdown with a skid (horizontal velocity decays 0.8/tick on contact), then a grounded STAGGER-length recovery.
  - Sync `DATA_BANK` and `DATA_PITCH`.
- [ ] **Step 4:** `./gradlew.bat test --tests "*Vesper*"`, then `build` → PASS.

---

## Part D — Burrow

### Task 7: Burrow rules

**Files:** Create `VesperBurrowRules.java`. Test `VesperBurrowRulesTest.java`.

**Interfaces:**
- Produces:
  - `enum Stage { DIVE, HUNT, STRIKE_TELL, STRIKE, RUMBLE, ERUPTION, RECOVERY, COMPLETE }`
  - `enum Strike { CLAW, TAIL }`
  - `record Plan(int huntTicks, boolean fromDive)` with `static Plan ground(RandomSource)` (200–300) and `static Plan dive(RandomSource)` (100–140)
  - `Strike strikeAt(int index)` — alternates, CLAW first
  - Constants: `DIVE_TICKS = 24`, `STRIKE_TELL_TICKS = 12`, `STRIKE_TICKS = 8`, `STRIKE_INTERVAL = 50`, `RUMBLE_TICKS = 36`, `RUMBLE_LOCK_TICK = 22` (inside the rumble), `ERUPTION_TICKS = 10`, `RECOVERY_TICKS = 70`
  - `Vec3 clampTrail(Vec3 p, double cx, double floorY, double cz)` — uses `SAFE_ARENA_HALF_EXTENT`
  - `double trailSpeed = 0.42` blocks/tick
  - `void save(CompoundTag)` / `static State load(CompoundTag)` for `record State(Stage stage, int tick, int strikeIndex, Plan plan, double x, double z, double lockX, double lockZ)`

- [ ] **Step 1: Write failing tests:**
  - `groundHuntIsTenToFifteenSeconds` — over 100 seeds, 200 ≤ huntTicks ≤ 300
  - `diveHuntIsFiveToSevenSeconds` — over 100 seeds, 100 ≤ huntTicks ≤ 140
  - `strikesAlternateStartingWithClaw`
  - `everyStrikeIsPrecededByItsTell`
  - `rumbleLockHappensBeforeEruptionWithReactionTime` — `RUMBLE_TICKS - RUMBLE_LOCK_TICK >= 12`
  - `trailClampedToArena`
  - `stateRoundTripsThroughNbt`
  - `targetLostDuringHuntGoesToRumbleAtCurrentPosition`
- [ ] **Step 2:** Run → FAIL. Implement. Run → PASS.

### Task 8: Burrow server behavior

**Files:**
- Create: `VesperBurrow.java` (owned by `VesperMountController`)
- Modify: `VesperPhaseOneAttack.java` (add `BURROW`), the entity (persist burrow state under the `"VesperBurrow"` tag; synced `DATA_BURROW_STAGE`, `DATA_BURROW_X/Z`)
- Create: `data/hemomancy/damage_type/vesper_rend.json`; add `hemomancy:vesper_rend` to `data/minecraft/tags/damage_type/bypasses_armor.json`
- Modify: `HemoDamageTypes.java` (`VESPER_REND` and `vesperRend(Level, Entity)`), lang death messages
- Test: extend `VesperBurrowRulesTest` with `strikeEffects` — pure `effects(Strike)` → CLAW = `{damage 9, armorPiercing}`; TAIL = `{damage 6, poison 100t, blood_loss 160t}`. Eruption is `{damage 22, launch 1.6 up}`.

**Interfaces:**
- Consumes: Task 7, Task 2 controller.
- Produces: `VesperBurrow.begin(boolean fromDive)`, `tick()`, `boolean active()`, `Vec3 trailPos()`.

- [ ] **Step 1:** Write the effects test → FAIL → implement the pure table → PASS.
- [ ] **Step 2: Implement the server behavior.**
  - **While underground:** `noPhysics = true`, invisible, not pushable and not collidable. Hurt is ignored (wounds can't open underground; requests queue).
  - **Hunt:** the trail moves toward the target at `trailSpeed`, clamped.
  - **Strikes:** the strike tell at the trail's position on the target; the strike hits within 1.6 blocks (claw: `vesperRend`; tail: `vesperImpale` + poison + `EffectInit.blood_loss`).
  - **Rumble:** locks the target position at `RUMBLE_LOCK_TICK`.
  - **Eruption:** at the lock — the entity reappears, launches whoever is within 2.5 blocks, and the claw/tail parts are visible.
  - **Recovery:** grounded.
  - **Grounded selection:** `BURROW` becomes selectable once `hasWingsGrown()` or health ≤ 70%, on a 400-tick cooldown.
  - **Dive-Burrow:** a Diving Rend calls `begin(true)` at impact, alternating with the plain Rend per sortie.
  - **Reload mid-burrow:** surface into RECOVERY at the saved x/z (rules `load` mapping).
- [ ] **Step 3:** `./gradlew.bat test --tests "*Vesper*"`, then `build` → PASS.

### Task 9: Burrow client — fissure trail, chips and rumble shake

**Files:**
- Create: `VesperBurrowTrailPayload.java` (entity id, x, z, stage), `VesperRumblePayload.java` (duration, intensity ramp) — registered `playToClient` in `PacketHandler`
- Create: `client/data/VesperBurrowClientData.java`, `client/render/world/chamberofwill/VesperBurrowFissureRenderer.java` (draws crack segments on the arena floor plane using `VesperFightFissureStyle`/`VesperFightStoneSurface` colors; only while `VesperFightClientData` is active), `client/data/VesperRumbleClientState.java` (a **rising** envelope: amplitude grows over the rumble and peaks at eruption)
- Modify: the existing camera-angle hook (`client/event/CardinalRiteImpactClientEvents.java`) so it sums the rumble state with the rite shake
- Particles: floor-stone chips. Add a `hemomancy:floor_chip` particle type (small spinning shard, color from `VesperFightStoneSurface`) if no existing `HemoParticleData` fits. Blood and dark glow welling come from `VesperVisualEffects`.
- Test: `VesperRumbleClientStateTest` — the amplitude is monotonic non-decreasing over the rumble, 0 after it ends, and clamped.

- [ ] **Step 1:** Write the test → FAIL → implement → PASS.
- [ ] **Step 2: Wire it up.**
  - The server sends the trail payload every 2 ticks during HUNT/STRIKE_TELL to players within 64 blocks. A summoned Vesper's particles are server-sent the same way; only the overlay is ordeal-only.
  - The server sends the rumble payload at RUMBLE start, only to players within the arena half-extent of the arena centre.
- [ ] **Step 3:** `./gradlew.bat build` → PASS.

---

## Part E — Wound

### Task 10: Wound rules

**Files:** Create `VesperWoundRules.java`. Test `VesperWoundRulesTest.java`.

**Interfaces:**
- Produces:
  - `enum Site { CARAPACE, NECK, TAIL }` — index = anchor index 0/1/2
  - `record Wound(Site site, float drained, int idleTicks)`
  - Constants: `DRAIN_REQUIRED = 100F`, `SCAB_DELAY = 80`, `SCAB_RATE = 0.4F/tick`, `SCAB_FLOOR_FRACTION = 0.5F` (it never scabs below half of the progress already reached), `PUPPET_TEAR = 25F`, `HIT_FALLBACK_RATIO = 0.10F`
  - `Wound drain(Wound, float absorptionProgress)` — resets idle
  - `Wound hit(Wound, float damage)` — resets idle and drains `damage * HIT_FALLBACK_RATIO`
  - `Wound tear(Wound)` — resets idle
  - `Wound tick(Wound)` — scabs after the delay, down to the floor
  - `boolean broken(Wound)`
  - `float scabFloor(float peak)`
  - `void save(CompoundTag)` / `load` — legacy `ActiveThroneAnchor` + `ActiveAnchorDamage` (0–40) maps to `drained = damage * 2.5`

- [ ] **Step 1: Write failing tests:**
  - `absorptionDrainsToBreak`
  - `hitsKeepOpenAndDrainOnlyAtFallbackRate`
  - `scabsAfterDelayButNeverBelowFloor`
  - `puppetTearDrainsChunk`
  - `legacyAnchorLoadsAsOpenWoundWithScaledProgress`
  - `siteIndexMatchesThresholdOrder`
- [ ] **Step 2:** Run → FAIL. Implement. Run → PASS.

### Task 11: Wound server integration and Absorption routing

**Files:**
- Create: `VesperWound.java`
- Modify:
  - the entity — replace `exposeAnchor`/`breakAnchor`/the anchor data with the wound; keep the health floor via `VesperCombatRules.healthFloor`. The wound opens through `controller.request(Priority.WOUND)`.
  - `VesperThroneAnchorPart.java` — reposition each part at its wound socket from `VesperWoundRules.socketOffset(Site, yaw)`, a pure model-space offset table matching the Task 4 sockets. The hitbox is live only for the active wound. `hurt` goes to `wound.hit`.
  - `VesperBloodAbsorptionInteractions.java` — also search for `VesperTheCrownedRefusalEntity` with an open wound eligible for the player (owner-only for ordeals, anyone for summoned), and call `crowned.drainWound(player, progressPerTick)`. Return the drained amount.
  - `VesperEncounterPuppetEvents.java` — puppet death calls `crowned.tearWound()`.
- **Wound-guarding rotation:** while a wound is open the controller uses `VesperMountControllerRules.guardSelection(Site, double drainerAngle)` → one of `TAIL_SWEEP, BITE, SHAKE_OFF, TURN_GUARD`, a pure function tested in `VesperMountControllerRulesTest` (it always returns non-null, and `TURN_GUARD` when the drainer is behind the wound).
- Test: update `VesperCombatImplementationSourceTest` (no `setNoAi`; Absorption routes to the Crowned Refusal) and `VesperAbsorptionEligibilityRulesTest` (a new `canDrainWound(boolean open, UUID owner, UUID player)`).

- [ ] **Step 1:** Write the new tests → FAIL.
- [ ] **Step 2:** Implement. The drain gives the player blood through the same path ordinary Absorption uses (`HemoCapabilityAccess` blood volume add), proportional to the drain.
- [ ] **Step 3:** `./gradlew.bat test --tests "*Vesper*"`, then `build` → PASS.

### Task 12: Wound visuals

**Files:**
- Create: `client/render/layer/mob/endgame/VesperWoundLayer.java` (at the mount socket: split scute plates hinge open by `open` progress; an exposed pulsing core via `SanguineConduitCoreGeometry` scaled up, with pulse and agitation from the drain ratio; a hit flash)
- Delete: `VesperThroneAnchorLayer.java` and its test
- Modify: `VesperVisualEffects.java` (`woundRupture(ServerLevel, Vec3)`: a layered burst of blood cells, dark glow, void tendrils and bolt lightning, per `MANIPULATION_VISUALS.md`) and the drain stream (reuse the Absorption siphon visual already used for the Evening Star defeat; find it via `absorbedBloodCell` usages in `BloodAbsorptionItem`)
- Test: update `VesperModelAssetContractTest` and `VesperEveningStarPresentationRenderSourceTest` references to the anchor layer.

- [ ] **Step 1:** Implement.
- [ ] **Step 2:** `./gradlew.bat build` → PASS. Visual check in Task 23.

---

## Part F — Puppet roster

### Task 13: Roster rules

**Files:** Create `VesperPuppetRoster.java`. Test `VesperPuppetRosterTest.java`.

**Interfaces:**
- Produces:
  - `enum Slot { AVAILABLE, OUT, STRUCK }`
  - `static final List<String> SHAPES` = the seven names from `PuppeteerSummonDefinitions` constants (no `RINGMASTER_PATTERN`)
  - `List<String> nextMuster(RandomSource)` — up to `3 - out()` shapes, preferring never-fielded ones, then the least recently fielded
  - `void markOut(String)`, `void recall(String)`, `void strike(String)`, `int out()`
  - `boolean hasFielded(String)`
  - `List<String> survivors()` — every non-STRUCK shape, in `SHAPES` order
  - `boolean mayRecall(String, int ticksOut)` — `ticksOut >= 400`
  - `CompoundTag save()` / `static VesperPuppetRoster load(CompoundTag)`

- [ ] **Step 1: Write failing tests:**
  - `rosterExcludesRingmaster`
  - `musterNeverExceedsThreeOut`
  - `everyShapeFieldedBeforeAnyRepeat` — loop muster/recall until all are fielded; assert no shape is fielded twice before all seven have been
  - `struckShapesNeverReturn`
  - `survivorsIncludeNeverCalledAndRecalled`
  - `roundTripsThroughNbt`
  - `ownerLossUnspoolsAllOutButKeepsStruck` — pure `onEncounterReset()` sets OUT → AVAILABLE
- [ ] **Step 2:** Run → FAIL. Implement. Run → PASS.

### Task 14: Muster, recall and carry-over

**Files:**
- Modify: `VesperMountMoves.java` (muster: per shape, `PuppeteerSummonBodies.create(name, level)`, `setTrialSummon(true)`, `setTrialCasterUUID(owner)`, `setOwnerUUID(boss)`, `setSummonName(name)`, `BoundSummonBehavior.applyTrialStats(mob, PuppeteerSummonDefinitions.get(name))`, the existing tags and persistent data, `addFreshEntity`, then `PuppetStrings.descend(mob)`; recall: `PuppetStrings.unspool(mob)` and `roster.recall`)
- Modify: `EndgameBossActions.clearVesperPuppets` → unspool instead of discard
- Modify: the entity (roster NBT under `"VesperRoster"`; a recall pass every 100 ticks for puppets with `mayRecall`; a new `RECALL` attack in `VesperPhaseOneAttack` with a Crossbar-raise telegraph)
- Modify: `VesperEncounterPuppetEvents.onDeath` → `roster.strike(name)` (read the name from the summon)
- Modify: `VesperOrdealManager.copyOrdeal` → also `to.setInheritedPuppets(from.roster().survivors())`
- Modify: `VesperTheEveningStarEntity` — persist `"InheritedPuppets"` (a list of strings)
- Test: `VesperPuppetRosterTest` covers the rules; update `VesperCombatImplementationSourceTest`: `copyOrdeal` carries survivors, and `clearVesperPuppets` uses `PuppetStrings.unspool`.

- [ ] **Step 1:** Write the source-test assertions → FAIL. Implement. Run → PASS. `build` → PASS.

---

## Part G — Phase 2

### Task 15: Acts and a unified controller

**Files:**
- Create: `VesperEveningStarAct.java`, `VesperEveningStarController.java`
- Modify: `VesperTheEveningStarEntity.java` (`tickTendencyCombat`/`enterRage` delegate to the controller; synced `DATA_ACT`), `VesperRageCombatRules.isRageThreshold` (→ `< 1/3`), `VesperPhaseTwoCombat.java` (remove `tick`/`tickRage` selection; keep the action bodies)
- Test: `VesperEveningStarActTest.java`

**Interfaces:**
- Produces:
  - `VesperEveningStarAct.of(float health, float max)` → `LOCKED` (> 2/3), `OPEN` (> 1/3), `SICKLES`
  - `boolean VesperEveningStarAct.locksWeaponToStance()`
  - `boolean VesperEveningStarAct.castsAnySchool()`
  - `VesperEveningStarController(VesperTheEveningStarEntity)` with `tick()`
- Behavior:
  - The stance morph becomes a **morph-strike**: the next action begins with a morph blend, replacing the 30-tick `isMorphTelegraph` cancel.
  - Movement between actions holds a committed strafe side for 20–40 ticks, switching with a pivot.

- [ ] **Step 1: Write failing tests:**
  - `actBoundaries` — at exactly 2/3 → OPEN, at exactly 1/3 → SICKLES
  - `lockedLocksWeapon`
  - `onlyOpenCastsAnySchool`
  - update `VesperRageCombatRulesTest` to the 1/3 threshold
- [ ] **Step 2:** Run → FAIL. Implement. Run → PASS. `build`.

### Task 16: Chain graph and chained movesets

**Files:**
- Create: `VesperChainGraph.java`
- Modify: `VesperWeaponAction.java` (add the moves below, each with entry and exit `Pose`), `VesperPhaseTwoCombat.java` (bodies), `VesperWeaponEffectRules.java`
- Test: `VesperChainGraphTest.java`

**Interfaces:**
- Produces:
  - `enum Pose { NEUTRAL, HIGH_GUARD, LOW_GUARD, EXTENDED, AIRBORNE, SPINNING }`
  - `VesperWeaponAction.entry()` / `exit()`
  - `VesperWeaponAction role()` → `OPENER, FOLLOW_UP, FINISHER`
  - `VesperChainGraph.next(VesperWeaponAction previous, EnumBloodTendency tendency, VesperEveningStarAct act, Context ctx)`, where `record Context(double distance, boolean lastHitLanded, boolean targetShielding, int chainLength, RandomSource random)`

New moves (3–4 per weapon including the existing ones):

| School / weapon | Existing | New |
|---|---|---|
| Animus blade | ICHIMONJI, CROSSCUT | BLADE_RISING_CUT, BLADE_DRAWING_STEP |
| Mortem axe | LEAPING_CLEAVE, REAPER_SWEEP | AXE_HAFT_SHOVE, AXE_GRAVE_SPLITTER |
| Lux spear | SKY_LANCE, LANCE_FLURRY | SPEAR_VAULT_KICK, SPEAR_PINNING_THRUST |
| Tenebris claws | TWIN_REND, PREDATOR_POUNCE | CLAW_SHADOW_FEINT, CLAW_EVISCERATE |
| Ductilis crossbow | CONDUCTIVE_VOLLEY, STORM_LOCK | CROSSBOW_SNAP_SHOT, CROSSBOW_BOLT_RAIN |
| Flammeus torch | BRANDING_THRUSTS, UPDRAFT_IMPALEMENT, FLAMMEUS_CONCENTRATION | TORCH_CINDER_SWEEP |
| Congeatio flail | CHAIN_SWEEP, HOOK_AND_CRUSH | FLAIL_ORBIT, FLAIL_SHATTER_SLAM |
| Ferric staff | MAGNETIC_AXIS, IRON_RETORT | STAFF_LODESTONE_SWEEP, STAFF_PILLAR_VAULT |

- [ ] **Step 1: Write failing tests:**
  - `everyWeaponHasOpenerFollowUpFinisher`
  - `nextStartsFromPreviousExitPose`
  - `lockedActStaysInStanceWeapon` — `next(..., LOCKED, ...)` always returns the stance tendency
  - `openActMayCrossSchoolsAtBlendPoints`
  - `finisherEndsChainOrChainLengthCapsAtFive`
  - `noImmediateRepeat`
- [ ] **Step 2:** Run → FAIL. Implement the graph and the new action bodies: telegraphed, custom visuals, `hitArc`/`hitArea` helpers. Run → PASS. `build`.

### Task 17: Evening Star Java motion

**Files:**
- Create: `client/model/entity/boss/endgame/VesperEveningStarMotion.java`
- Modify: `VesperTheEveningStarModel.java` — new actions, morph-strike blends and pose-to-pose transitions are drawn by `VesperEveningStarMotion`. Existing authored clips remain for unchanged moves unless replaced. Cross-action blending: 4-tick eased crossfade from the previous action's exit pose.
- Test: `VesperWeaponAnimationRulesTest` — add `everyNewActionHasMotion` (a `VesperEveningStarMotion.supports(action)` table covers every `VesperWeaponAction` not in the authored set).

- [ ] **Step 1:** Write the test → FAIL. Implement. Run → PASS. `build`.

### Task 18: Boss manipulation caster and synergy chains

**Files:**
- Create: `VesperManipulationCaster.java`, `VesperManipulationCoverage.java`, `VesperSynergyChains.java`
- Modify: the controller (Locked: casts from the stance school between combo beats, max 1 per 100 ticks; Open: chains from `VesperSynergyChains`, max 1 chain per 140 ticks)
- Test: `VesperManipulationCoverageTest.java`

**Interfaces:**
- Produces:
  - `enum Coverage { CASTABLE, BOSS_VARIANT, EXCLUDED }`
  - `Map<String, Entry> VesperManipulationCoverage.TABLE`, where `record Entry(Coverage coverage, String reason)`
  - `boolean VesperManipulationCaster.cast(VesperTheEveningStarEntity, String manipulation, LivingEntity target)` — delegates to `EntityManipulationEffects.cast` when `EntityManipulationEffects.isSupported`, otherwise uses its own implementations. `umbral_step` is upgraded to the player visuals.
  - `List<Chain> VesperSynergyChains.ALL`, where `record Chain(String first, String second, int gapTicks)`. It includes at least: `sanguine_magnetism → synaptic_jolt`, `glacial_grasp → exsanguinate`, `sanguine_ignition → hematic_flare`, `umbral_step → (blade string)`.

- [ ] **Step 1: Write failing tests:**
  - `tableCoversEveryRegisteredManipulation` — every `ManipulationInit` id has an entry. Read the registry ids by parsing `ManipulationInit.java` source for `MANIPS.register("…"` (registries aren't bootstrapped in unit tests).
  - `everyExcludedEntryHasReason`
  - `chainsOnlyReferenceCastableEntries`
  - `willSupportedListUnchanged` — `EntityManipulationEffects` source still lists exactly the current 32 ids
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3: Implement.** Go school by school in this order: Animus, Mortem, Lux, Tenebris, Ductilis, Flammeus, Congeatio, Ferric. For each manipulation, read its class's `getAction` and reproduce its fight effect and visuals for an entity caster (reuse its static helpers and payloads). A player-only one gets an EXCLUDED entry with a reason.
- [ ] **Step 4:** Run → PASS. `build`.

### Task 19: The Sickle act

**Files:**
- Create: `VesperSickleRules.java`
- Modify: `VesperWeaponAction.java`:
  - new sickle actions: `SICKLE_REAP_L`, `SICKLE_REAP_R`, `SICKLE_HOOK_REEL`, `SICKLE_REEL_CYCLONE`, `SICKLE_REAPERS_RISE`, `SICKLE_PASS_THROUGH`, `SICKLE_GALLOWS_WHEEL`, `SICKLE_TRAVELLING_CYCLONE`, `SICKLE_REVERSE_CYCLONE`, `SICKLE_FEINT_CYCLONE`, `SICKLE_POUNCE_FEINT`, `SICKLE_CRESCENT_FAN`, `SICKLE_CRESCENT_STAGGER`, `SICKLE_CRESCENT_SKIM`, `SICKLE_CRESCENT_RETURN`, `SICKLE_ANCHOR_HOOK`, `SICKLE_HARVEST`
  - existing ones kept: `SICKLE_CYCLONE`, `SICKLE_POUNCE`, `SICKLE_CROSS_REND`, `SICKLE_HOOK`, `SANGUINE_CRESCENTS`
- Modify: `VesperPhaseTwoCombat.java` (bodies), `LivingSickleHookEntity` (reel mode: a tether that pulls the caught entity toward the boss each tick; a shield rip calls `EndgameBossActions.disableShieldOnHit`; `Vesper` self-reel mode for Pass-Through and Anchor Hook)
- Modify: `VesperEveningStarMotion.java` (dual-wield, both arms independent; spin momentum from synced `DATA_SPIN_RATE`)
- Modify: `VesperRageCombatRules.java` → delegate to `VesperSickleRules`
- Test: `VesperSickleRulesTest.java`

**Interfaces:**
- Produces:
  - `enum SickleString { REAPING, HOOK_AND_REEL, GALLOWS_WHEEL, CYCLONE, POUNCE, CRESCENT, ANCHOR_HOOK, HARVEST }`
  - `SickleString choose(double distance, boolean shielding, boolean cornered, SickleString last, RandomSource)`
  - `VesperWeaponAction branch(SickleString, int step, boolean lastHit, boolean shielding, double distance, RandomSource)` — returns `NONE` to end the string
  - `int reapCount(RandomSource)` → 2..5
  - `float spinRate(float t)` — ramps up then down
  - `float carryVelocity(VesperWeaponAction spin)`
  - `boolean reelBreaks(float damageDuringReel, int sprintAwayTicks)` — damage ≥ 12 or sprint ≥ 30
  - `boolean harvestReady(float health, float max, boolean used)` — < 10% and unused
  - `int recoveryTicks(int stringLength)` — grows with length
  - `boolean committedSideHolds(int ticksOnSide)` — 20–40

- [ ] **Step 1: Write failing tests:**
  - `reapingStringLengthTwoToFive`
  - `hookCatchBranchesToReelFollowUps` — the branch after `SICKLE_HOOK_REEL` with `lastHit=true` is one of REEL_CYCLONE / REAPERS_RISE / PASS_THROUGH
  - `hookMissRecoversIntoReaping`
  - `shieldingFavorsHook`
  - `corneredFavorsAnchorHook`
  - `spinRateRampsUpAndDown`
  - `reelBreakThresholds`
  - `harvestOnlyOnceBelowTenPercent`
  - `longerStringsRecoverLonger`
  - `targetLostEndsString`
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3: Implement** the rules, then the bodies.
  - Telegraphs: the hook throw has a glint (`hitGlow` at the blade 6 ticks before release); ground-skimmer crescents fly low; the Gallows lines draw as tendrils.
  - Shield rip: a caught hook against a shielding target calls `disableShieldOnHit(boss, target, 100)`.
  - Reel break: track damage taken and target sprint-away ticks during reel actions → stagger 20 ticks.
  - Movement: committed-side strafe, replacing the 6-tick flip in `positionForRage`.
- [ ] **Step 4:** Run → PASS. `build`.

### Task 20: Puppet-borrowed attacks and inheritance flare

**Files:**
- Create: `VesperBorrowedAttacks.java`
- Modify: `VesperWeaponAction.java` (`BORROWED_HULK_YANK`, `BORROWED_MARROW_VOLLEY`, `BORROWED_VULTURE_DIVE`, `BORROWED_MUMMER_DECOYS`, `BORROWED_HOUND_CURS`, `BORROWED_BELLOWS_BREATH`, `BORROWED_MNEMONIC_LASH`), the controller (inserts a borrowed attack between strings: weight 0.2 in LOCKED/OPEN and 0.1 in SICKLES; each attack has a 300-tick cooldown), `beginAwakening` (a sigil flare per inherited shape via `VesperVisualEffects.puppetSigil(ServerLevel, Vec3, String shape)`)
- Test: `VesperBorrowedAttacksTest.java`

**Interfaces:**
- Produces:
  - `static Optional<VesperWeaponAction> VesperBorrowedAttacks.forShape(String shape)`
  - `List<VesperWeaponAction> available(List<String> inherited)`

- [ ] **Step 1: Write failing tests:**
  - `everyRosterShapeMapsToOneAttack` — all 7 `VesperPuppetRoster.SHAPES` map; Ringmaster → empty
  - `noInheritanceMeansNoBorrowedAttacks`
  - `availableMatchesInheritedOrder`
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3: Implement the bodies:**

  | Attack | Body |
  |---|---|
  | Hulk yank | Crossbar thread tether pulls the player in 8 blocks, into a slam with RING telegraph |
  | Marrow volley | 3 lobbed globs that leave 100-tick caustic pools (custom glow/cells) |
  | Vulture dive | Leap and a dive strike with LINE telegraph |
  | Mummer decoys | 2 afterimage entities (reuse `PhantasmalEchoEntity` visuals) draw attention, then burst at 40 ticks |
  | Hound curs | Spawn 3 short-lived Blood Curs as encounter puppets: no roster effect, rupture at 80 ticks |
  | Bellows breath | Ember cone with CONE telegraph (reuse `livingTorchBreath` damage) |
  | Mnemonic lash | Store the max player hit in the last 200 ticks; a thread lash of 60% of it, capped at 14 |

- [ ] **Step 4:** Run → PASS. `build`.

---

## Part H — Review, docs, acceptance

### Task 21: Vesper review client

**Files:**
- Modify: `build.gradle` — add `vesperReviewClient`, mirroring `annettaReviewClient`: game dir `build/vesper-client-review`, property `hemomancy.vesperReview`, username `VesperReview`.
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/VesperClientReview.java`, modelled on `AnnettaClientReview.java`.
- Scenes:
  - stills: `mount_turnaround`, `wings_folded`, `wings_flared`, `wings_flight`, `burrow_fissure`, `wound_carapace`, `wound_neck`, `wound_tail`, `act_locked`, `act_open`, `act_sickles`
  - live: `live_ground`, `live_burrow`, `live_dive_burrow`, `live_sortie`, `live_muster`, `live_wound_drain`, and one per sickle string, run against an invulnerable target. `live_sickle_hook` uses a shielding target.

- [ ] **Step 1:** Implement. `./gradlew.bat build`.
- [ ] **Step 2:** Run `./gradlew.bat runVesperReviewClient`. If it stops on the onboarding screen, drop `{"op":"open","name":"VesperReview"}` in the operator inbox. Capture every scene, inspect the screenshots, and fix what reads wrong. Send the key screenshots to the user.

### Task 22: Docs

**Files:**
- `docs/HEMOMANCY_REFERENCE.md`:
  - §5.10 — rewrite Vesper's current fight behavior: controller, telegraphs, flight, burrow, wounds and Absorption, roster and carry-over, acts, chains, Sickle act, borrowed attacks, render path.
  - Puppeteering section — "her puppets" → "his puppets".
  - Particle and sound tables — `floor_chip` if added.
  - Manipulation coverage table.
- `docs/LORE_REFERENCE.md` — only if a lore line changes. Check for Vesper pronouns.
- `wiki/*.md` — check Vesper pages.
- Memory: update `project_vesper_fight_cleanup.md` with where the code lives and how to review it.

- [ ] **Step 1:** Write it. `grep -n -i "vesper.*\bher\b" docs wiki` → no Vesper "her".

### Task 23: Acceptance

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/common/worldgen/VesperOrdealRecoveryGameTests.java` — add `reloadMidBurrowSurfacesIntoRecovery`, `reloadMidWoundKeepsProgress`, `ownerExitUnspoolsPuppetsAndResetsOut`.

- [ ] **Step 1:** Write the GameTests → run `./gradlew.bat gameTestServer` → the new ones FAIL if the wiring is missing → fix → PASS.
- [ ] **Step 2:** `./gradlew.bat build`, `./gradlew.bat test` and `./gradlew.bat alphaCheck` → all pass. Quote the outputs.
- [ ] **Step 3:** A fresh reviewer on the most capable model checks the whole change (per the user's native-execution preference: one final reviewer).
- [ ] **Step 4:** Report to the user: what changed, review screenshots, the calls made (the hit-drain fallback at 10%), and anything not verified. Leave everything uncommitted.
