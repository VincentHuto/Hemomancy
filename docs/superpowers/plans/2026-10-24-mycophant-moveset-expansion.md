# Mycophant Moveset Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the Mycophant a position-aware move picker, strings and follow-ups, thirteen new moves, brood tumors, grown brood, hold kinds and the Last Brood set piece.

**Architecture:** Pure rule classes (`Mycophant*Rules`) decide where the player is, what she picks, where each hit lands and when the brood and the Last Brood change state. The controller runs the picker and the follow-ups. The moves live in per-family classes behind one dispatch. New lingering effects are entities drawn on the client with the existing toolkit. Poses are authored in the model generator.

**Tech Stack:** NeoForge 1.21.1 (21.1.251), Java 21, JUnit 5, Python model generator (`tools/model_export/mycophant.py`).

**Spec:** `docs/superpowers/specs/2026-10-24-mycophant-moveset-expansion-design.md` (read it with this plan; section numbers below refer to it).

## Global Constraints

- Follow `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md`; guardrail tests enforce most of it.
- No vanilla `ParticleTypes` in any new or touched file; effects use `BossTelegraphs`, `HemoParticleData`, `PacketHandler.sendClawSlash`, and toolkit materials via a public bridge in `client/render/world`.
- Never hand-edit `MycophantModel.java`: edit `tools/model_export/mycophant_anim.java` and run `python tools/model_export/mycophant.py`.
- Animation time only through `MycophantAnimState` / `BossReplayClock`; never `get…Tick(s)() + partial`.
- Player-facing text is `en_us.json` keys. That file has another session's uncommitted edits: add keys only, next to the existing `mycophant` keys; do not reorder or rewrite other lines.
- Owned fights never kill: claim-style damage uses `HemoDamageTypes.mycophantClaim` and `MycophantClaimRules.clampNonlethal`.
- Holds and pins move the player only through the client pin (`MycophantHoldClientEvents`); pulls use velocity plus `hurtMarked`, never per-tick teleports.
- No payload changes (all new sync goes through entity data), so `PacketHandler.PROTOCOL_VERSION` stays.
- Do not commit; the user commits.
- **Gradle is shared.** Before any Gradle command, take the lock: `mkdir build/.mycophant-gradle-lock` (if it fails, someone holds it; wait 30 s and retry). Remove it with `rmdir build/.mycophant-gradle-lock` when the command finishes, including on failure. Compile with `./gradlew.bat compileJava compileTestJava --console=plain -q`; run your tests with `./gradlew.bat test --tests "<pattern>" --console=plain -q`. A compile error in a file you do not own means another task is mid-edit: wait and retry, never edit their file.

## Review Focus

- A held, pinned or cocooned player who dies, logs out or changes dimension is released at once, and their client stops pinning (Task 3 test `holdKindsReleaseWhenTheHeldPlayerIsGone`).
- An unowned (summoned) Mycophant never places brood tumors in the world, and its Gaze is still survivable by outrunning it (Task 2 test `tumorsOnlyInOwnedFights`; Task 9 live scene).
- When she yields, fails the owner or is discarded, every hazard, lance, glob, tendril and tumor she made is removed (Task 4 test `hazardsExpireWithoutALivingOwner`; Task 8 `clearsEverythingOnLeave`).
- A split or Last Brood trigger arriving mid-move or mid-string cancels the pending follow-up and releases any pin (Task 1 test `followUpsStopForAnInterrupt`).
- Creative and spectator players are never pinned, netted, cocooned, pulled or hit by the beam (Task 2 test `onlySurvivalPlayersAreTaken`).

---

## Ownership map (for parallel waves)

| Wave | Task | Owns (only this task edits these) |
|---|---|---|
| 1 | 1 Selection rules | `MycophantAttack`, `MycophantControllerRules`, new `MycophantPositionRules`, `MycophantSelectionRules`, their tests, `MycophantControllerRulesTest` |
| 1 | 2 Strike, brood and Last Brood rules | new `MycophantStrikeRules`, `MycophantBroodRules`, `MycophantLastBroodRules`, `MycophantClaimRules` (+ tests) |
| 2 | 3 Holds and entity state | `MycophantEntity`, new `MycophantHoldKind`, `MycophantHoldClientEvents` (+ tests) |
| 2 | 4 Entities, block, grown brood | new `MycophantHazardEntity`, `MycophantGlobEntity`, `MycophantLanceEntity`, `MycophantBroodTumors`, `BroodTumorBlock`, their renderers, `EntityInit`, `BlockInit`, `EntityRendererRegistrar`, `FunglingEntity`, block assets, lang keys |
| 2 | 5 Poses | `tools/model_export/mycophant_anim.java`, regenerated `MycophantModel.java`, `MycophantAnimState` |
| 3 | 6 Controller, dispatch, claw moves | `MycophantController`, `MycophantMoves`, new `MycophantClawMoves` |
| 3 | 7 Ranged and brood moves | new `MycophantRangedMoves` |
| 3 | 8 Wing moves, Gaze beam, Last Brood | new `MycophantWingMoves`, `MycophantLastBrood`, `client/render/world/MycophantGazeGeometry`, the one `collect` line in `ManipulationVisualRenderer` |
| 4 | 9 Review client, docs, verification | `MycophantClientReview`, docs |

All new common classes go in `com.vincenthuto.hemomancy.common.entity.boss.endgame` unless noted.

---

### Task 1: Position-aware selection rules

**Files:**
- Modify: `common/entity/boss/endgame/MycophantAttack.java`, `MycophantControllerRules.java`
- Create: `MycophantPositionRules.java`, `MycophantSelectionRules.java`
- Test: `src/test/.../endgame/MycophantSelectionRulesTest.java` (new), `MycophantControllerRulesTest.java`
- Also fix the two `CLAW_RAKE` references outside the owned files that the rename breaks: `MycophantMoves.java:43` (rename only) and `tools/model_export/mycophant_anim.java` plus a regenerate (rename only). `LatentCombatRules.Attack.CLAW_RAKE` is a different enum: leave it.

**Interfaces (produces):**
- `MycophantAttack`: rename `CLAW_RAKE` → `RAKE_STRING` in place; append, in order, `BROOD_SETTLE, CLAW_PIN, TAIL_LASH, VENT_BLOOM, HAND_SWEEP, SIPHON_SPIT, SPORE_MORTAR, NURSING, HYPHAE_LANCES, LATTICE_NET, GAZE, LAST_BROOD`. `isEmbrace()` unchanged.
- `MycophantPositionRules`: `enum Band { HUG, NEAR, MID, FAR }`, `enum Sector { FRONT, FLANK, REAR }`; `static Band band(double horizontalDistance)` (HUG < 3.5 ≤ NEAR < 7 ≤ MID < 13 ≤ FAR); `static Sector sector(double degreesFromFacing)` (FRONT ≤ 60 < FLANK ≤ 120 < REAR); `static double degreesFromFacing(double fromX, double fromZ, float yawDegrees, double toX, double toZ)` (0..180, using the same forward vector as `MycophantEntity.forward()`: `(-sin yaw, cos yaw)`).
- `MycophantControllerRules`:
  - `static BossMoveTimeline timeline(MycophantAttack attack, int phase, int variant)`; keep `timeline(attack, phase)` delegating with variant 0.
  - `allowed`: phase-2 only adds `HYPHAE_LANCES, LATTICE_NET, GAZE, LAST_BROOD`.
  - Delete `minRange`, `maxRange`, `weight` and `choose` (selection moves to `MycophantSelectionRules`).
  - Cooldowns for the new moves.
- `MycophantSelectionRules`:
  - `static int weight(MycophantAttack attack, int phase, Band band, Sector sector)`: 0 means ineligible there.
  - `static final class History { void record(MycophantAttack a); MycophantAttack last(); float repeatFactor(MycophantAttack a); }`: factor 0 for the last pick, then 0.4, 0.6, 0.8 for the next three most recent, 1 otherwise.
  - `static final class Pressure { void sample(Band band); int kiteTicks(); int hugTicks(); void resetKite(); }`: a 200-tick ring of samples.
  - `static int embraceWeight(int kiteTicks)` = `min(40, 8 + 4 * (kiteTicks / 20))`.
  - `static int settleBonus(int hugTicks)` = `min(24, 3 * (hugTicks / 20))`.
  - `record Context(boolean nursingTarget)`: true when a living brood fungling is within 14 blocks and fewer than 2 are grown.
  - `static MycophantAttack choose(int phase, Band band, Sector sector, ToIntFunction<MycophantAttack> cooldownLeft, History history, Pressure pressure, Context context, IntUnaryOperator roll)`.
  - `enum Condition { ALWAYS, ON_HIT, HUGGED, NETTED }`.
  - `record FollowUp(MycophantAttack next, Condition condition, float chance)`.
  - `static MycophantAttack followUp(MycophantAttack done, int phase, boolean hitLanded, boolean netted, Band band, int depth, boolean interruptPending, DoubleSupplier chanceRoll)`: returns `NONE` when `depth >= 2` or `interruptPending`.
  - `static int linkGapTicks(int phase)` = 6 in phase 1, 4 in phase 2.
  - `static int rollVariant(MycophantAttack attack, int phase, IntUnaryOperator roll)`: `RAKE_STRING` phase 1 → 0 or 1 (50/50), phase 2 → 1 or 2 (50/50); `TAIL_LASH` phase 2 → 1; everything else 0.

**Values to pin.**

Weights, by band and sector; "½" is half weight there:

| Move | Phase | Weight | Full cells | Half cells |
|---|---|---|---|---|
| RUSHING_EMBRACE / FUSION_EMBRACE | 1 / 2 | `embraceWeight(kite)` | MID, FAR × FRONT | MID, FAR × FLANK |
| SIPHON_FEED | 1, 2 | 16 | NEAR, MID × FRONT | — |
| PLUCK | 1, 2 | 14 | HUG, NEAR × FRONT | — |
| RAKE_STRING | 1, 2 | 24 | HUG, NEAR × FRONT | HUG, NEAR × FLANK |
| BROOD_CALL | 1, 2 | 8 | MID, FAR × any | HUG, NEAR × any |
| NECTAR_SURGE | 1, 2 | 5 | any | — |
| BROOD_SETTLE | 1, 2 | 10 + `settleBonus(hug)` | HUG × any | — |
| CLAW_PIN | 1, 2 | 14 | NEAR × FRONT | — |
| TAIL_LASH | 1, 2 | 26 | HUG, NEAR × REAR | HUG, NEAR × FLANK |
| VENT_BLOOM | 1, 2 | 14 | HUG, NEAR, MID × REAR | HUG, NEAR, MID × FLANK |
| HAND_SWEEP | 1, 2 | 18 | NEAR, MID × FRONT | — |
| SIPHON_SPIT | 1, 2 | 18 | MID, FAR × FRONT | MID, FAR × FLANK |
| SPORE_MORTAR | 1, 2 | 16 | FAR × any | MID × any |
| NURSING | 1, 2 | 12 only if `context.nursingTarget` | any | — |
| WING_GUARD | 2 | 10 | NEAR, MID × FRONT | — |
| WING_SWEEP | 2 | 18 | HUG, NEAR × FRONT | HUG, NEAR × FLANK |
| PULP_BLOOM | 2 | 12 | HUG, NEAR × FRONT | — |
| HYPHAE_LANCES | 2 | 16 | NEAR, MID, FAR × FRONT | NEAR, MID, FAR × FLANK |
| LATTICE_NET | 2 | 12 | MID × FRONT | — |
| GAZE | 2 | 10 | MID, FAR × FRONT | — |
| LAST_BROOD | 2 | 0 (triggered, never picked) | — | — |

Follow-ups:

| After | Phase | Next | Condition | Chance |
|---|---|---|---|---|
| CLAW_PIN | 1, 2 | PLUCK | ON_HIT | 1.0 |
| HAND_SWEEP | 1 / 2 | RUSHING_EMBRACE / FUSION_EMBRACE | ON_HIT | 0.7 |
| WING_SWEEP | 2 | PLUCK | HUGGED (band HUG or NEAR) | 0.4 |
| LATTICE_NET | 2 | FUSION_EMBRACE | NETTED | 0.85 |
| RAKE_STRING | 1, 2 | BROOD_SETTLE | HUGGED (band HUG) | 0.3 |

Timelines (wind-up / active / recovery):

| Move | Phase 1 | Phase 2 |
|---|---|---|
| RAKE_STRING v0 | 14 / 16 / 18 | — |
| RAKE_STRING v1 | 14 / 30 / 22 | 12 / 28 / 20 |
| RAKE_STRING v2 | — | 12 / 34 / 22 |
| BROOD_SETTLE | 18 / 6 / 40 | 16 / 6 / 36 |
| CLAW_PIN | 14 / 4 / 16 | 12 / 4 / 14 |
| TAIL_LASH v0 / v1 | 12 / 6 / 18 | 10 / 16 / 18 |
| VENT_BLOOM | 16 / 4 / 18 | 14 / 4 / 16 |
| HAND_SWEEP | 16 / 6 / 20 | 14 / 6 / 18 |
| SIPHON_SPIT | 18 / 12 / 18 | 16 / 14 / 16 |
| SPORE_MORTAR | 22 / 10 / 22 | 20 / 10 / 20 |
| NURSING | 12 / 80 / 16 | 12 / 80 / 16 |
| HYPHAE_LANCES | — | 18 / 24 / 20 |
| LATTICE_NET | — | 16 / 6 / 24 |
| GAZE | — | 24 / 40 / 20 |
| LAST_BROOD | — | 30 / 160 / 0 (outcomes are staggers the controller forces) |

Cooldowns (ticks, phase 1 / phase 2): RAKE_STRING 50 / 40, BROOD_SETTLE 120 / 100, CLAW_PIN 140 / 120, TAIL_LASH 60 / 50, VENT_BLOOM 200 / 170, HAND_SWEEP 80 / 70, SIPHON_SPIT 120 / 100, SPORE_MORTAR 220 / 180, NURSING 300 / 260, HYPHAE_LANCES — / 140, LATTICE_NET — / 260, GAZE — / 320. The phase-1 embrace's existing cooldowns stay.

- [ ] **Step 1: Write the failing tests** in `MycophantSelectionRulesTest`:
  - `bandAndSectorThresholds`: `band(3.49)==HUG`, `band(3.5)==NEAR`, `band(6.99)==NEAR`, `band(7)==MID`, `band(13)==FAR`; `sector(60)==FRONT`, `sector(60.01)==FLANK`, `sector(120.01)==REAR`; `degreesFromFacing(0,0,0F,0,5)` ≈ 0 and `(0,0,0F,0,-5)` ≈ 180.
  - `everyCellHasTwoAnswersPerPhase`: for each phase, band and sector, at least two moves (embrace at kite 0, nursing with `nursingTarget` true) have `weight > 0`.
  - `neverTheSameMoveTwiceInARow`: with every cooldown at 0, after `history.record(X)` `choose` never returns X over 500 seeded rolls in X's best cell.
  - `recentMovesDecay`: `repeatFactor` gives 0, 0.4, 0.6, 0.8, 1.0 down the history.
  - `kiteRaisesTheEmbrace`: `embraceWeight(0)==8`, `embraceWeight(100)==28`, `embraceWeight(400)==40`; `settleBonus(200)==24`.
  - `followUpsStopForAnInterrupt`: CLAW_PIN with a hit gives PLUCK; with `interruptPending` true, or depth 2, gives NONE.
  - `variantsByPhase`: RAKE_STRING variants are in {0,1} in phase 1 and {1,2} in phase 2; TAIL_LASH phase 2 is 1.
  - `simulatedFightHasVariety`: a scripted quarry cycling through every band × sector (100 picks per cell, Pressure sampled 20 ticks per pick, cooldowns honoured via a tick counter, History recorded). Over phase 1 and phase 2 separately, no move exceeds 33% of all picks.
- [ ] **Step 2:** Update `MycophantControllerRulesTest` (timeline and cooldown values above; delete the range and choose tests that moved). Run both tests (with the Gradle lock); they fail.
- [ ] **Step 3:** Implement the enum, `MycophantPositionRules`, `MycophantSelectionRules` and the `MycophantControllerRules` changes. Point `MycophantController.tickRoam`'s old `choose` call at `MycophantSelectionRules.choose` with a temporary local `History`, `Pressure` and `Context(false)`, just so it compiles; Task 6 replaces that wiring.
- [ ] **Step 4:** Run `--tests "*MycophantSelectionRules*" --tests "*MycophantControllerRules*"`; they pass, and `compileJava` is clean.

### Task 2: Strike, brood, Last Brood and claim rules

**Files:**
- Create: `MycophantStrikeRules.java`, `MycophantBroodRules.java`, `MycophantLastBroodRules.java`
- Modify: `MycophantClaimRules.java`
- Test: new `MycophantStrikeRulesTest`, `MycophantBroodRulesTest`, `MycophantLastBroodRulesTest`; update `MycophantClaimRulesTest`

**Interfaces (produces):**
- `MycophantStrikeRules` (pure; doubles and floats only):
  - `static boolean inCone(double dx, double dz, float yawDeg, double radius, double halfAngleDeg)`
  - `static boolean inRearArc(double dx, double dz, float yawDeg, double radius, double arcDeg)` (arc centred on her rear)
  - `static boolean inCircle(double dx, double dz, double radius)`
  - `static boolean inSweep(double dx, double dz, float yawDeg)`: 4–9 blocks, 120°
  - `static boolean clearsSweep(double feetAboveGround)`: at least 0.8
  - `static int[] stringHits(int variant, int phase)`: active-relative strike ticks for RAKE_STRING. v0 `{0, 8}`; v1 phase 1 `{0, 8, 20}`, phase 2 `{0, 7, 18}`; v2 `{0, 7, 14, 26}`. The last entry is the slam for v1 and v2.
  - `static boolean isSlam(int variant, int index, int hits)`: true for the last hit when the variant is at least 1.
  - `static int[] tailHits(int variant)`: v0 `{0}`, v1 `{0, 10}`.
  - `static int[] globTicks(int phase)`: phase 1 `{0, 4, 8}`, phase 2 `{0, 3, 6, 9, 12}`.
  - `static double[][] spread(double tx, double tz, int count, long seed, double minRadius, double maxRadius)`: entry 0 is the target; the rest sit on a ring between the radii, evenly spaced in angle with seeded jitter.
  - `static int mortarFlightTicks(double horizontalDistance)` = `max(14, round(8 + distance * 1.1))`.
  - `static double[] lobVelocity(double dx, double dy, double dz, int flightTicks, double gravity)`: per-tick velocity that lands at the offset after `flightTicks` (drag-free).
  - `static double[][] lanceLine(double ox, double oz, double tx, double tz, double length, double spacing)`: points from the origin toward the target.
  - `static int lanceDelay(int index)` = `3 * index`.
  - `LANCE_WARN_TICKS = 10`.
  - `static float gazeYaw(float startYawDeg, int direction, float sweepTick)`: `startYaw - 50 * direction + 100 * direction * clamp(t / 40)`. The start yaw is the quarry's bearing at commit; direction is ±1 by variant.
  - `static boolean gazeHits(float beamYawDeg, double dx, double dz, double maxRange)`: within 6° of the beam and inside range.
  - `static double gazeTargetHeight(double bbHeight)` = `bbHeight * 0.5` (body centre, §8).
  - `static boolean onlySurvival(boolean spectator, boolean creative)`.
- `MycophantBroodRules`:
  - Constants: `NURSE_RANGE = 14`, `NURSE_TICKS = 80`, `NURSE_BREAK_DAMAGE = 8`, `GROWN_CAP = 2`, `GROWN_SCALE = 1.8F`, `GROWN_HEALTH = 30`, `GROWN_BITE = 8`, `GROWN_GRIEF_TICKS = 50`, `TUMOR_CAP = 6`, `TUMOR_HATCH_TICKS = 400`, `BROOD_CAP = 4`.
  - `static boolean mayNurse(int livingBrood, int grown)`
  - `static boolean mayPlantTumor(boolean owned, int standing)`
  - `static boolean hatchDue(int plantedTick, int now)`
  - `static int griefTicks(boolean grown)`
- `MycophantLastBroodRules`:
  - Constants: `THRESHOLD = 0.25F`, `TELL = 30`, `PULL = 160`, `COLLAPSE = 80`, `SPENT = 30`, `COCOON = 60`, `TENDRILS = 4`, `HITS_PER_TENDRIL = 2`, `FLOOR = 0.05F`, `DAMAGE_SCALE = 0.5F`, `ANCHOR_MIN = 6`, `ANCHOR_MAX = 9`, `PULL_PER_TICK = 0.09` (tuned in review), `COCOON_DAMAGE = 14F`, `COCOON_DRAIN = 0.15F`, `COCOON_PRESSURE = 25F`.
  - `static boolean triggers(int phase, float health, float maxHealth, boolean done)`
  - `static float incoming(float amount, float health, float maxHealth)`: halves the damage and never takes health below `FLOOR * maxHealth`.
  - `enum Outcome { PULLING, CUT_FREE, SPENT, COCOONED }`
  - `static Outcome outcome(int activeTick, int tendrilsLeft, boolean reached)`
  - `static double[][] anchors(double cx, double cz, float yawDeg, long seed)`: four points, 6–9 blocks out, a quarter-turn apart, with seeded jitter.
- `MycophantClaimRules`: `meterPerTick(1) = 1/100`, `meterPerTick(2) = 1/80`; `hitsToBreak(1) = 4`, `hitsToBreak(2) = 5`.

- [ ] **Step 1: Write the failing tests:**
  - `MycophantStrikeRulesTest`:
    - cone, arc and circle edges: inside on the boundary, outside just past it.
    - `inRearArc` false straight ahead.
    - `inSweep` false at 3.9 and at 9.1.
    - `clearsSweep(0.8)` true, `clearsSweep(0.79)` false.
    - `stringHits` values as above.
    - `spread` entry 0 equals the target, with every other point within the radii.
    - `mortarFlightTicks(1) == 14`.
    - `lobVelocity` lands within 0.01 of the target when simulated per tick with the same gravity.
    - `lanceLine(0, 0, 0, 10, 12, 1)` has 12 points with `z` 1..12.
    - `gazeYaw` runs from start-50 to start+50 for direction +1.
    - `gazeHits` within 6° only.
    - `onlySurvival(false, false)` true and false otherwise (Review Focus).
  - `MycophantBroodRulesTest`:
    - `mayNurse(3, 2)` false, `mayNurse(1, 0)` true.
    - `tumorsOnlyInOwnedFights`: `mayPlantTumor(false, 0)` false, `mayPlantTumor(true, 6)` false, `mayPlantTumor(true, 5)` true.
    - `hatchDue(100, 499)` false, `hatchDue(100, 500)` true.
    - `griefTicks(true) == 50`, `griefTicks(false) == 30` (the existing `griefTicks()`).
  - `MycophantLastBroodRulesTest`:
    - `triggers(2, 179, 720, false)` true; false in phase 1, when done, and above 25%.
    - `incoming(20, 100, 720)` == 10.
    - `incoming(100, 40, 720)` == 4 (floor 36).
    - outcomes: tendrils 0 → CUT_FREE; reached → COCOONED; tick 160 with tendrils left → SPENT; otherwise PULLING.
    - `anchors` are all 6–9 from the centre and pairwise at least 60° apart.
  - `MycophantClaimRulesTest`: the new meter and hit values (update `meterRates` and the hold-pull test's expectations if they read these).
- [ ] **Step 2:** Run them; they fail. **Step 3:** Implement. **Step 4:** Run `--tests "*MycophantStrikeRules*" --tests "*MycophantBroodRules*" --tests "*MycophantLastBroodRules*" --tests "*MycophantClaimRules*"`; they pass.

### Task 3: Hold kinds and new entity state

**Files:**
- Create: `MycophantHoldKind.java`
- Modify: `MycophantEntity.java`, `client/event/MycophantHoldClientEvents.java`
- Test: new `MycophantHoldKindTest`

**Interfaces:**
- Consumes: Task 1's enum and `MycophantPositionRules`; Task 2's `MycophantLastBroodRules.incoming`, `MycophantBroodRules`.
- Produces:
  - `enum MycophantHoldKind { SAC, PIN, COCOON }` with:
    - `boolean metered()`: SAC only.
    - `boolean breakable()`: SAC only.
    - `int durationTicks()`: SAC -1, PIN 20, COCOON 60.
    - `static MycophantHoldKind byOrdinal(int)`.
  - `MycophantEntity`:
    - `public static Vec3 holdPoint(Vec3 feet, float yawDegrees, MycophantHoldKind kind)`: SAC and COCOON are the existing sac point; PIN is `feet + right * -1.6 + forward * 3.6`, at floor height.
    - `public MycophantHoldKind getHoldKind()`: `DATA_HOLD_KIND`.
    - `void beginHold(ServerPlayer player, MycophantHoldKind kind)`; the existing `beginHold(player)` means SAC.
    - `public int getAttackVariant()` / `void setAttackVariant(int)`: `DATA_ATTACK_VARIANT`, reset to 0 by `setAttack`.
    - `public float getGazeYaw()` / `void setGazeYaw(float)`: `DATA_GAZE_YAW`.
    - `boolean isLastBroodDone()` / `void finishLastBrood()`: saved as `LastBroodDone`.
    - `MycophantBroodTumors broodTumors()`: the field is created by Task 4's class. Add the field and its save/load calls (`broodTumors.save(tag)` / `broodTumors.load(tag)`) once Task 4's class compiles; if it doesn't yet, leave those three lines for Task 6 to add.
    - `void nurse(LivingEntity fungling)` / `void stopNursing()`: sets and clears `DATA_SIPHON_TARGET_ID` to the fungling's id without the player drain. `tickSiphon` ignores a non-player target.
  - Hold behaviour:
    - `tickHold` accepts holds made by `CLAW_PIN` (PIN) and `LAST_BROOD` (COCOON) as well as the embraces.
    - Non-metered kinds count down `durationTicks()` and release with no stagger and no pressure change. A COCOON's release is handled by Task 8 through `onCocoonReleased`, so call `MycophantLastBrood.onCocoonReleased(this, held)` when a COCOON runs out; Task 8 defines it, so until it exists leave a `// Task 8` call site commented in.
    - `hurt` and `onOwnerCast` break a hold only when `getHoldKind().breakable()`.
  - Damage during the Last Brood: in `hurt`, when `getState()==ATTACK && getAttack()==LAST_BROOD`, scale ordinary damage through `MycophantLastBroodRules.incoming`.
  - `MycophantHoldClientEvents` pins to `MycophantEntity.holdPoint(lerp target feet, lerp target yaw, holder.getHoldKind())`. It also cancels jumping while the local player stands in a landed lattice net (`MycophantHazardEntity.nets(level, player)` from Task 4). Add that line when Task 4's class exists, or leave it for Task 8.

- [ ] **Step 1: Write the failing tests:**
  - `MycophantHoldKindTest`: `SAC.metered() && SAC.breakable()`; PIN and COCOON neither; the durations; `holdPoint` for PIN is 3.6 forward and 1.6 to her right of the feet at yaw 0, and SAC equals the existing `holdPoint(feet, yaw)`.
  - `holdKindsReleaseWhenTheHeldPlayerIsGone`: a pure helper `MycophantHoldKind.shouldRelease(boolean heldPresent, int heldTicks, MycophantHoldKind kind)` is true when the player is gone, or when a non-SAC kind reaches its duration.
- [ ] **Step 2:** Run; they fail. **Step 3:** Implement. **Step 4:** Run `--tests "*MycophantHoldKind*" --tests "*MycophantClaimRules*" --tests "*Mycophant*Source*"`; they pass and the main code compiles.

### Task 4: Hazard, glob and lance entities, brood tumors, grown brood

**Files:**
- Create:
  - `common/entity/boss/endgame/MycophantHazardEntity.java`
  - `common/entity/boss/endgame/MycophantGlobEntity.java`
  - `common/entity/boss/endgame/MycophantLanceEntity.java`
  - `common/entity/boss/endgame/MycophantBroodTumors.java`
  - `common/block/fungal/BroodTumorBlock.java`, or the package where the Fungal Gardens blocks live (check `BlockInit`)
  - `client/render/entity/boss/endgame/MycophantHazardRenderer.java`
  - `client/render/entity/boss/endgame/MycophantGlobRenderer.java`
  - `client/render/entity/boss/endgame/MycophantLanceRenderer.java`
  - Assets: `assets/hemomancy/blockstates/brood_tumor.json`, `models/block/brood_tumor_lower.json` and `_upper.json`, the textures (generate them with a small Python/PIL script in her palette, kept under `tools/model_export/` like the other generators), and `models/item/brood_tumor.json` only if the block has an item.
- Modify: `EntityInit`, `BlockInit`, `client/registry/EntityRendererRegistrar`, `common/entity/mob/animal/FunglingEntity.java` (+ its renderer only if `Attributes.SCALE` does not already scale it), `en_us.json` (add keys only).
- Test: new `MycophantHazardRulesTest` (pure helpers below), plus whatever block and entity guardrails fail (loot table, creative tab, lang).

**Interfaces (produces):**
- `MycophantHazardEntity extends Entity`:
  - `enum Kind { NECTAR_POOL, SPORE_CLOUD, LATTICE_NET, BINDING_TENDRIL }`, with synced kind, radius, life, age, owner id, land delay (net), and the end point (tendril).
  - `static MycophantHazardEntity pool(ServerLevel, Vec3 at, MycophantEntity owner)`: radius 1.5, life 160.
  - `static MycophantHazardEntity cloud(ServerLevel, Vec3 at, MycophantEntity owner)`: radius 3, life 120.
  - `static MycophantHazardEntity net(ServerLevel, Vec3 at, MycophantEntity owner, int landDelay)`: radius 4, life `landDelay + 40`.
  - `static MycophantHazardEntity tendril(ServerLevel, MycophantEntity owner, Vec3 anchor)`: lives until cut, or until the Last Brood ends it.
  - `int hitsLeft()`: tendril, starts at `MycophantLastBroodRules.HITS_PER_TENDRIL`.
  - `boolean landed()`: net.
  - `boolean caught(LivingEntity)`: net.
  - `static boolean nets(Level level, LivingEntity entity)`: true when the entity stands inside a landed net.
- Hazard effects each tick, applied to survival players and the owner's targets only:
  - Pool: Slowness I for 25 ticks, and `owner.addPressure(0.05F)` per tick in owned fights.
  - Cloud: 1 damage per 20 ticks (`owner.damageSources().mobAttack(owner)`), and pressure as for the pool.
  - Net: when it lands, records who it caught (`caught`) and applies Slowness IV for 40 ticks.
  - Tendril: `hurt` from a player counts a hit (cut at 0) and returns true; its bounding box covers the segment from the owner's sac to the anchor.
- Pure helper `static boolean shouldExpire(boolean ownerAlive, int age, int life)`. Owner gone → expire (Review Focus). Every hazard discards itself when its owner is missing or not alive.
- `MycophantGlobEntity extends Projectile` (or `ThrowableProjectile`; follow the repo's existing projectiles, such as `blood_chum_projectile`):
  - `enum Kind { GLOB, EGG }`, a `tumor` flag, and the owner.
  - `static void launch(ServerLevel, MycophantEntity owner, Vec3 from, Vec3 target, Kind kind, boolean tumor, int flightTicks)` uses `MycophantStrikeRules.lobVelocity`.
  - On impact: GLOB deals 5 to a hit entity and spawns `pool` at the landing spot. EGG bursts for 6 damage in 2.5 blocks plus `BossTelegraphs.spores`; with `tumor` set it instead calls `owner.broodTumors().plant(level, landing)`.
- `MycophantLanceEntity extends Entity`, following `PriestessEruptionEntity`:
  - `static void erupt(ServerLevel, Vec3 at, MycophantEntity owner, int delay)`.
  - It cracks for `LANCE_WARN_TICKS`, then deals 10 damage and a toss (0.6 up) to survival players within 0.9 blocks.
  - The renderer reuses the Priestess thorn drawing in her colours, through a public bridge if that drawing is package-private.
- `MycophantBroodTumors`:
  - `void plant(ServerLevel, BlockPos)`: gated by `MycophantBroodRules.mayPlantTumor(owner owned, standing)`.
  - `void tick(ServerLevel, MycophantEntity)`: hatches due tumors into brood funglings, respecting `BROOD_CAP`, removes broken tumors from the list, and pulses `BossTelegraphs.glow` every 20 ticks.
  - `void clear(ServerLevel)`: removes the blocks.
  - `int standing()`.
  - `void save(CompoundTag)` / `void load(CompoundTag)`.
- `BroodTumorBlock`:
  - Two-tall, with a `DoubleBlockHalf` property.
  - Hardness 0.3, no drop, light level 6, no collision-free trickery (it is solid cover).
  - Breaking either half breaks both.
  - Lang key `block.hemomancy.brood_tumor`. Keep it out of the creative tab, or put it in the WIP list if a guardrail demands a tab entry.
- `FunglingEntity`:
  - `public void setGrown(boolean)` / `public boolean isGrown()`, synced and saved.
  - Growing applies an `Attributes.SCALE` modifier (×1.8), max health 30 (healing to full) and attack damage 8.
- Rendering:
  - `MycophantHazardRenderer` draws each kind with the toolkit:
    - Pool: the brazier `bloodPool` look, tinted amber.
    - Cloud: client-side spore motes through `HemoParticleData`.
    - Net: hyphae strands (the tendril drawing).
    - Tendril: a tendril tube from the sac to the anchor.
  - `MycophantGlobRenderer` draws a nectar or egg glob with a toolkit material.
  - Bridge any package-private toolkit piece through a public class in `client/render/world`.

- [ ] **Step 1: Write the failing tests** in `MycophantHazardRulesTest`:
  - `hazardsExpireWithoutALivingOwner`: `shouldExpire(false, 0, 999)` true; `shouldExpire(true, 160, 160)` true; `shouldExpire(true, 10, 160)` false.
- [ ] **Step 2:** Run; it fails. **Step 3:** Implement the entities, the block (with its assets), the grown brood and the renderers, and register everything.
- [ ] **Step 4:** Run `compileJava` and `--tests "*MycophantHazard*" --tests "com.vincenthuto.hemomancy.architecture.*"`; fix any guardrail failures (loot table, lang, particle, render-type caching) in your own files.

### Task 5: Poses for every new move

**Files:**
- Modify: `tools/model_export/mycophant_anim.java`, `client/model/entity/boss/endgame/MycophantAnimState.java`
- Regenerate: `MycophantModel.java`

**Interfaces:**
- Consumes:
  - Task 1's enum and `MycophantControllerRules.timeline(attack, phase, variant)`.
  - Task 2's `MycophantStrikeRules.stringHits`, `tailHits` and `globTicks`.
  - Task 3's `entity.getAttackVariant()` and `entity.getGazeYaw()`.
  - If Task 3 isn't merged yet, read the variant and gaze through those accessor names and expect a compile error until it lands.
- Produces: new `reviewPose` names. Each is a static pose constant in the anim file and a `case` in the review-pose switch:
  - `rake_l`, `rake_r`, `slam`, `settle_rise`, `settle_drop`, `pin`
  - `tail_coil`, `tail_lash`, `vents_open`, `hand_wind`, `hand_sweep`
  - `spit`, `mortar`, `nurse`, `lances`, `net`, `gaze`
  - `last_brood_rear`, `last_brood_pull`, `last_brood_collapse`

**What to author:**
- `MycophantAnimState`: key the attack clock on `attack.ordinal() * 4 + variant`.
- `attackPose` gets a case per new attack:
  - **Strings:** the Rake String mixes rake-L and rake-R alternately at each `stringHits` tick, with the slam pose on the last hit for v1 and v2. Hold a crouched hesitation before the slam (the delayed hit's tell). Each swing eases in over 4 ticks and out over 4.
  - **Brood Settle:** rise across the wind-up (claws spread wide, the `height` value up), drop on the active stage (`height` well below rest), then lie low through the recovery.
  - **Claw Pin:** the right front claw reaches forward and down (driven through `p.front` and `rake`).
  - **Tail Lash:** coil then lash through the tail chains, with a second lash for v1.
  - **Vent Bloom:** open the vents.
  - **Hand Sweep:** wind out to the left, then sweep across.
  - **Siphon Spit:** arch the siphon with a pulse at each glob tick.
  - **Spore Mortar:** pump the abdomen.
  - **Nursing:** reach the siphon to the fungling through the existing latch blend (the siphon target id is the fungling's).
  - **Hyphae Lances:** the wings rise, then the spars stab down.
  - **Lattice Net:** the wings spread forward.
  - **Gaze:** push the pulp forward and turn the head toward `getGazeYaw()` relative to her body yaw.
  - **Last Brood:** rear through the tell, pull (the sac's `sacPulse` scaled to 1.3 and pushed forward, the collar flared), then collapse.
- Every case blends through `blendFromLast` like the existing moves. Phase 2 uses the same poses with shorter timelines.

- [ ] **Step 1:** Author the poses and cases. **Step 2:** Run `python tools/model_export/mycophant.py`; it prints the part count and only `MycophantModel.java` changes (no texture diffs).
- [ ] **Step 3:** Run `compileJava` and `--tests "*MycophantModelSource*" --tests "*BossAnimationClock*"`; they pass.

### Task 6: Controller, dispatch and claw moves

**Files:**
- Modify: `MycophantController.java`, `MycophantMoves.java`
- Create: `MycophantClawMoves.java`
- Test: new `MycophantControllerFlowTest` (the pure helper below)

**Interfaces:**
- Consumes: Tasks 1–4.
- Produces, for Tasks 7 and 8:
  - The dispatch in `MycophantMoves.tick` routes each family with the existing signature `(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick, BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry)`:
    - `RAKE_STRING`, `BROOD_SETTLE`, `CLAW_PIN`, `TAIL_LASH` and `HAND_SWEEP` to `MycophantClawMoves.tick(...)`.
    - `VENT_BLOOM`, `SIPHON_SPIT`, `SPORE_MORTAR` and `NURSING` to `MycophantRangedMoves.tick(...)`.
    - `HYPHAE_LANCES`, `LATTICE_NET` and `GAZE` to `MycophantWingMoves.tick(...)`.
    - `LAST_BROOD` to `MycophantLastBrood.tick(...)`.
  - Create `MycophantRangedMoves`, `MycophantWingMoves` and `MycophantLastBrood` as empty shells with that `tick` signature, so Tasks 7 and 8 fill them.
  - Controller hooks:
    - `void markHit()`: the current move landed.
    - `void markNetted()`
    - `boolean hitLanded()`
    - `int variant()`
    - `void forceStagger(int ticks, @Nullable Vec3 face)` (exists)
    - `void skipToRecovery()` (exists)
    - `@Nullable FunglingEntity nursingTarget()`
    - `void beginNow(MycophantAttack attack)`: starts a move at once, skipping the decision delay; used by the riposte and the Last Brood trigger.
    - `void nursingDamage(float amount)` / `float nursingDamageTaken()`: the entity's `hurt` reports damage taken during NURSING; the count resets at `begin`.
- Controller behaviour:
  - Sample `Pressure` every tick from the quarry's band.
  - In `tickRoam`, pick with `MycophantSelectionRules.choose`, using the real `History`, `Pressure` and `Context`. Context `nursingTarget` comes from a scan for brood funglings within `NURSE_RANGE` and the grown count.
  - `begin` records the history, rolls and sets the variant, and resets the kite count for an embrace.
  - When a move completes, ask `followUp`. Depth counts the chain; the interrupt is a pending split or stagger. A hit follow-up starts after `linkGapTicks` rather than the decision delay.
  - The riposte: while guarding, a frontal blow calls `beginNow(WING_SWEEP)` instead of only reducing damage (hook it where `isGuarding` is checked in the entity's `hurt`, through a controller method `onGuardedHit()`).
  - Each tick, check `MycophantLastBroodRules.triggers(...)`. When it fires and no hold is active, `beginNow(LAST_BROOD)`; when it fires during a hold, queue it like the split.
  - Tick `boss.broodTumors()`, and clear tumors on yield, failure and discard. Add the `MycophantBroodTumors` field and its save/load to `MycophantEntity` here if Task 3 left them out.
- `MycophantClawMoves`: implement §6's Rake String, Brood Settle, Claw Pin, Tail Lash and Hand Sweep with `MycophantStrikeRules` and `stringHits` / `tailHits`. Call `markHit()` on a landed hit.
  - **Tells:** use `BossTelegraphs.cone`, `ring` and `glow`, and `PacketHandler.sendClawSlash` on each rake and the slam.
  - **Claw Pin:** on a hit against a survival player, `boss.beginHold(player, PIN)` and deal 6.
  - **Brood Settle:** shove outward 1.2.
  - **Hand Sweep:** miss a player whose `y - groundY >= 0.8`.
- Pure helper for the test: `static boolean MycophantController.mayFollowUp(int depth, boolean interruptPending)`, in a nested rules holder or in `MycophantSelectionRules` if that's cleaner.

- [ ] **Step 1: Write the failing test:** `MycophantControllerFlowTest.followUpDepthAndInterrupt`: depth 0 and 1 may follow up; depth 2 and an interrupt may not.
- [ ] **Step 2:** Run; it fails. **Step 3:** Implement. **Step 4:** Run `compileJava` and `--tests "*Mycophant*"`; they pass.

### Task 7: Ranged and brood moves

**Files:**
- Modify: `MycophantRangedMoves.java` (the shell from Task 6)
- Test: extend `MycophantBroodRulesTest` only if a new pure helper is added.

**Interfaces:**
- Consumes:
  - Task 4: `MycophantHazardEntity.cloud`, `MycophantGlobEntity.launch`, `broodTumors()`, `FunglingEntity.setGrown`.
  - Task 3: `nurse` and `stopNursing`.
  - Task 6: the controller hooks.

**What to implement (§6):**
- **Vent Bloom:** at commit, a `cloud` at `boss.bodyPoint(0, 0.4, -4.5)`.
- **Siphon Spit:** at each `globTicks(phase)` tick, launch a GLOB at `spread(target, n, seed, 1.5, 3.5)[i]` with `mortarFlightTicks`.
- **Spore Mortar:**
  - At commit, compute 3 eggs (4 in phase 2, 5 below 35%) with `spread(target, n, seed, 2.0, 4.5)`.
  - Draw a landing `BossTelegraphs.ring` at each target at once; the rings must show at least 14 ticks before impact.
  - Launch them across the active stage. One egg per volley carries `tumor = MycophantBroodRules.mayPlantTumor(...)`.
- **Nursing:**
  - At commit, `boss.nurse(controller.nursingTarget())`.
  - Through the active stage, glow along the siphon line.
  - If the fungling dies, or the boss takes `NURSE_BREAK_DAMAGE` in total during it, `stopNursing()` and `skipToRecovery()`. Track the damage in the controller, through a `nursingDamage(float)` hook that Task 6 adds.
  - At the end of the active stage, `setGrown(true)` and `stopNursing()`.
- **Grief:** grown brood grief uses `griefTicks(true)`. Find where `onBroodLost` requests the grief stagger and pass the fungling's grown flag.

- [ ] **Step 1:** Implement. **Step 2:** Run `compileJava` and `--tests "*Mycophant*"`; they pass.

### Task 8: Wing moves, the Gaze beam and the Last Brood

**Files:**
- Modify: `MycophantWingMoves.java` and `MycophantLastBrood.java` (the shells from Task 6), `client/render/world/ManipulationVisualRenderer.java` (one `collect` line)
- Create: `client/render/world/MycophantGazeGeometry.java`
- Test: extend `MycophantLastBroodRulesTest` only for new pure helpers.

**Interfaces:**
- Consumes:
  - Task 4's `lance`, `net` and `tendril` entities.
  - Task 3's `setGazeYaw`, `beginHold(player, COCOON)` and `finishLastBrood`.
  - Task 2's rules and Task 6's hooks.
- Produces: `static void MycophantLastBrood.onCocoonReleased(MycophantEntity boss, ServerPlayer held)` (called by Task 3's hold code) and `static void MycophantLastBrood.clear(MycophantEntity boss)` (removes its tendrils; call it from the controller's `endAttack` for `LAST_BROOD`).

**What to implement:**
- **Hyphae Lances** (§8):
  - At commit, lay `lanceLine(boss, quarry, 12, 1.0)` and `erupt` each point with `lanceDelay(i)`.
  - Below 35% health, add a second line at ±30°.
  - The crack telegraph is the lance entity's warn stage.
- **Lattice Net:**
  - At commit, `net(quarry position, landDelay 16)`.
  - On landing, `markNetted()` when the quarry was caught.
- **Gaze:**
  - At commit, store the quarry's bearing as the start yaw and pick the direction from the variant (a coin flip if it is 0).
  - Each active tick, `setGazeYaw(gazeYaw(...))`.
  - Every 5 ticks, hit survival players for whom `gazeHits` is true and a block raycast (`ClipContext`, `COLLIDER`) from `boss.bodyPoint(0, 5.2, 2.6)` to the player at `gazeTargetHeight` is clear: 3 damage plus 1 pressure.
  - `MycophantGazeGeometry.collect(...)` draws the beam on the client from the pulp along `getGazeYaw()`, using a toolkit material (`LuxUmbraBatch` lux or cruor, as the siphon tether does), only while `getAttack()==GAZE` and the attack is in its active stage. Wire it in `ManipulationVisualRenderer` next to `MycophantSiphonTetherGeometry.collect`.
- **Last Brood** (§9):
  - **Tell:** pull back toward the pool centre, roar, flare (glow).
  - **Active:**
    - Spawn the `TENDRILS` tendrils at `anchors(...)`.
    - Each tick, pull every survival player toward `holdPoint(SAC)` by `PULL_PER_TICK` (add to their velocity, `hurtMarked`).
    - Track the outcome with `outcome(...)`. When a player reaches within 1.2 blocks of the hold point it is COCOONED: `beginHold(player, COCOON)`.
  - **Endings:**
    - CUT_FREE: `forceStagger(COLLAPSE)` plus a big `bloodCells` burst.
    - SPENT: `forceStagger(SPENT)`.
    - COCOONED: skip to recovery once the cocoon releases.
    - `finishLastBrood()` when the move starts, so it never repeats.
  - `onCocoonReleased`:
    - Damage `clampNonlethal(health, COCOON_DAMAGE)` as `mycophantClaim`.
    - Pay `COCOON_DRAIN` of max blood through `BloodAccounting.pay` with the existing `CLAIM` source.
    - Add `COCOON_PRESSURE`, then push the player out.
- No vanilla `ParticleTypes` anywhere.

- [ ] **Step 1:** Implement. **Step 2:** Run `compileJava` and `--tests "*Mycophant*" --tests "com.vincenthuto.hemomancy.architecture.*"`; they pass.

### Task 9: Review scenes, docs and full verification

**Files:** `src/gameTest/.../MycophantClientReview.java`, `docs/HEMOMANCY_REFERENCE.md`, `docs/superpowers/specs/2026-10-18-mycophant-spawn-mother-design.md` (a pointer to the new spec), and any `wiki/*.md` that lists her moves.

- [ ] **Step 1:** Add posed scenes for each new `reviewPose` name. Extend `PHASE_ONE` / `PHASE_TWO` with every new pickable move, so the live scenes start each in turn.
- [ ] **Step 2:** Add `HoldProbe` steps:
  - **Claw Pin** against the real player: pinned, released after about 20 ticks, no stagger.
  - **Lattice Net:** caught, then the Fusion Embrace follows.
  - **Gaze:** a tumor placed beside the player blocks the hit, and without it the hit lands.
  - **Last Brood, cut free:** the probe cuts the four tendrils and expects a collapse.
  - **Last Brood, cocooned:** the player is pulled in, and the health floor holds.
  - Each step logs PASS or FAIL. `MYCOPHANT_HOLD_JITTER` stays 0.0000.
- [ ] **Step 3:** Update the docs (the phase bullets in `HEMOMANCY_REFERENCE.md`: the new moves, holds, the brood tumors, grown brood and the Last Brood).
- [ ] **Step 4:** Run the full `./gradlew.bat test` (green), then `runMycophantReviewClient -PmycophantReviewOnly=hold_live,live_phase_one,live_phase_two` and confirm `MYCOPHANT_HOLD_RESULT all=PASS` and a screenshot per new move.
