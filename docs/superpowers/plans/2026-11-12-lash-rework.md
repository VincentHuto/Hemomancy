# Lash Rework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the lash strand as a left-click tap plus a right-click charge, flick and live whip with real whip
motion and a capped camera.

**Architecture:** `LashRules` stays the one pure rule class the server and every client run; it gains phases, a
momentum rope and a throw arc, and reads its numbers from a `LashTuning` record. The lash is driven by a whip aim
(yaw and pitch) that the casting client sends to the server, which clamps it, decides every hit and relays it to
watchers. A client mixin on the mouse turn splits raw movement between the whip aim and a capped camera.

**Tech Stack:** Java 21, NeoForge 21.1.251 for Minecraft 1.21.1, Mixin, JUnit 5, NeoForge GameTests.

**Spec:** `docs/superpowers/specs/2026-11-12-lash-rework-design.md`. Read it first; this plan argues from it.

## Global Constraints

- Read `AGENTS.md` before any task. Its rules bind every task.
- **No commits.** The owner commits. Leave work in the working tree; "done" for a task is its tests green.
- **One Gradle at a time.** Other sessions share the build directories. A mid-run `NoClassDefFoundError` is usually
  a collision; rerun.
- Jet, Spray, Plant and Brand behave exactly as before. The R-key casts and their tests stay green untouched.
- Hits are the server's (`StrandHits`, `ManipulationCombatHelper.canHarm`). Blood only through `BloodAccounting`.
- Payloads: `CustomPacketPayload`, real direction, `ServerboundGuards`, `ServerboundRateLimiter`, sent through the
  `PacketHandler` facade; a payload decodes, validates and calls one service method.
- Player-facing text is an `en_us.json` key. No `Component.literal`.
- No vanilla particles. Read `docs/MANIPULATION_VISUALS.md` before Task 7.
- Nothing under `common/` names a client class.
- The mod is unreleased: no migrations, no aliases; delete what is replaced.
- Unit tests: `./gradlew.bat test --tests "<pattern>"`. GameTests and the client: see `docs/TESTING.md`.

## Review Focus

1. **Camera damping set to 0 in config:** the view must turn exactly as vanilla and the whip aim equal the look.
   (Task 6 test.)
2. **Right click released mid-throw or mid-fade, death, logout, dimension change:** the camera cap must switch off and
   no whip aim or tap cooldown may survive. (Tasks 3 and 6 tests.)
3. **A whip-aim payload with NaN, infinite or out-of-range angles, or with no open lash:** ignored, session
   untouched. (Task 4 test.)
4. **Tap aimed at a block in vanilla reach, or spammed inside its cooldown:** the block is mined as vanilla; a
   cooled-down tap pays nothing. (Task 5 tests.)
5. **Blood running out while charging or live:** the session ends cleanly; a left-click release at partial charge
   cracks at the partial bonus. (Task 3 tests.)

---

## File map

| File | Change |
| --- | --- |
| `common/manipulation/verb/strand/LashTuning.java` | New. Every feel number. |
| `common/manipulation/verb/strand/LashProfile.java` | New. Per-manipulation tap and charge numbers. |
| `common/manipulation/verb/strand/LashRules.java` | Rewritten: phases, rope, throw arc. |
| `common/manipulation/verb/strand/StrandSpec.java`, `Strands.java`, `verb/Shapings.java` | Carry a `LashProfile`. |
| `common/manipulation/verb/strand/StrandService.java`, `StrandSession.java`, `StrandShaping.java`, `StrandCue.java` | Lash phases, costs, whip aim, tap, cue bits. |
| `common/network/capa/harbinger/manips/LashAimPacket.java`, `LashAimSyncPacket.java`, `LashTapPacket.java` | New payloads. |
| `common/network/PacketHandler.java`, `src/test/resources/network/payload-registry.txt` | Registration, version 49 to 50. |
| `client/input/LashCameraRules.java`, `client/input/LashCamera.java` | New. Pure cap-and-drag rule; the client state the mixin calls. |
| `mixin/client/MixinMouseHandler.java`, `hemomancy.mixins.json` | New client mixin. |
| `config/HemoClientConfig.java` | `lashCameraDamping`. |
| `client/data/StrandClientState.java`, `client/event/ClientInputEvents.java`, `client/network/Clientbound*Handlers` | Whip aim in, tap and snap input. |
| `client/render/world/StrandRenderer.java`, `client/player/StrandPlayerPose.java` | Orb, throw, fade, tap, poses. |
| `src/gameTest/.../StrandClientReview.java`, `gradle/review-clients.gradle` | Live tuning mode. |
| Tests: `LashRulesTest`, `LashCameraRulesTest` (new), `StrandCueTest`, `StrandSkillsTest`, `StrandGameTests` | Rewritten or extended. |

Paths are under `src/main/java/com/vincenthuto/hemomancy/` unless shown otherwise. If `client/input/` or
`mixin/client/` does not fit `PackageLayoutGuardrailTest`, use the nearest existing package and say so.

---

### Task 1: Tuning and profile data

**Files:**
- Create: `common/manipulation/verb/strand/LashTuning.java`, `LashProfile.java`
- Modify: `StrandSpec.java`, `Strands.java`, `verb/Shapings.java:185-220`
- Test: `src/test/java/.../common/manipulation/verb/strand/LashProfileTest.java` (new), `StrandSkillsTest`

**Interfaces — Produces:**

```java
public record LashTuning(
    double flickTurn,        // 6.0   deg/tick of whip-aim turn that counts as a flick
    double restTurn,         // 1.5   deg/tick below which the aim is at rest
    int restTicksToFade,     // 6     ticks at rest before LIVE becomes FADING
    int fadeTicks,           // 30
    int regrowTicks,         // 4
    double arcDegrees,       // 60.0  the throw's arc, centred on the look
    int throwTicks,          // 4
    double damping,          // 0.92  velocity kept per tick by a node
    double aimPull,          // 0.12  fraction a node is pulled toward the aim line per tick, at the tip
    double maxNodeSpeed,     // 4.0   blocks/tick clamp
    double slackSpeed,       // 0.3   tip speed below which a sweep does not hurt
    double fullSpeed,        // 1.2   tip speed for cap damage
    double crackSpeed,       // 1.0   tip speed a reversal or stroke end needs to crack
    double cameraCap,        // 4.0   deg/tick the view may turn while LIVE or FADING
    double cameraDrag,       // 0.6   fraction of camera turn rate kept per tick once input stops
    double maxAimOffset,     // 150.0 deg the whip aim may sit from the server's look
    double maxAimStep        // 60.0  deg/tick the server lets the whip aim move
) { public static final LashTuning DEFAULT = ...; }

public record LashProfile(float tapDamage, double tapBlood, int tapCooldown, int tapStateTicks,
                          double chargeBloodPerPulse, int chargeTicks, float chargedMultiplier) {}
```

- `StrandSpec` gains a last component `@Nullable LashProfile lash`; the compact constructor throws if `form == LASH`
  and it is null.
- `Strands.lash(double bloodPerPulse, float damageScale, LashProfile profile)`; the old two-argument overload is
  deleted. The other four factories pass null.
- `Shapings` profiles (tap damage is before `damageScale`):

| Factory | tapDamage | tapBlood | tapCooldown | tapStateTicks | chargeBloodPerPulse | chargeTicks | chargedMultiplier |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bloodLash` | 4.5 | 8 | 12 | 0 | 7.5 | 20 | 1.4 |
| `searingLashStrand` | 4.5 | 20 | 12 | 0 | 30 | 40 | 1.75 |
| `gloamLacerationLash` | 4.5 | 12 | 8 | 60 | 20 | 30 | 1.25 |

- [ ] **Step 1:** Write `LashProfileTest`: a LASH spec without a profile throws; `Shapings.searingLashStrand()` has
  `chargedMultiplier == 1.75F` and `chargeTicks == 40`; `gloamLacerationLash()` has `tapStateTicks == 60`;
  `bloodLash()` has `tapBlood == 8.0`; `LashTuning.DEFAULT.cameraCap() == 4.0`.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*LashProfileTest"`. Expected: does not compile.
- [ ] **Step 3:** Add the two records, the spec component, the factory and the three profiles.
- [ ] **Step 4:** Run the same command plus `--tests "*StrandSkillsTest" --tests "*ShapingGuardrailTest"`. Expected: pass.

---

### Task 2: `LashRules` — phases, rope, throw

**Files:**
- Modify (rewrite): `common/manipulation/verb/strand/LashRules.java`
- Test (rewrite): `src/test/java/.../verb/strand/LashRulesTest.java`

**Interfaces — Consumes:** `LashTuning`, `LashProfile`.

**Interfaces — Produces:**

```java
public enum Phase { CHARGING, THROWING, LIVE, FADING }

public static LashTuning tuning();
public static void setTuning(LashTuning tuning);          // the review client's seam; DEFAULT otherwise

public static final class State {                         // getters only outside the package
    Phase phase();  double charge();  /* 0..1 */  double extent();  /* 0..1 of full length */
    float aimYaw(); float aimPitch();
    Vec3 node(int i); Vec3 previous(int i); Vec3 tip(); boolean seeded(); long lastCrack();
}

/** One tick. lookYaw/lookPitch are the caster's view; aimYaw/aimPitch the whip aim (ignored unless LIVE or FADING). */
public static Step step(State state, Vec3 hand, float lookYaw, float lookPitch, float aimYaw, float aimPitch,
        double length, long now, Clipper clipper, double taut, LashProfile profile);

public record Step(Phase phase, double tipSpeed, double turnRate, boolean threw, boolean crack,
        double crackCharge, boolean tipBlocked, boolean tired, boolean resting) {}

/** Early contact during THROWING: cracks now and goes LIVE. Returns the charge the crack carries. */
public static double struck(State state, long now);
/** Left click: from CHARGING releases the charge down the look; from LIVE/FADING pulls the aim back to the look. */
public static double release(State state, float lookYaw, float lookPitch, long now);
public static Vec3 direction(float yaw, float pitch);
public static float sweepDamage(double tipSpeed, boolean tired);      // floor 3, cap 6, by tuning speeds
public static float crackDamage(double crackCharge, LashProfile p);   // 9 * lerp(1, p.chargedMultiplier, charge)
public static boolean maySnap(State state, long now);                 // unchanged meaning
public static List<Vec3[]> sweeps(State state);                       // unchanged meaning
```

Rules the tests pin (numbers from `LashTuning.DEFAULT`):

- **CHARGING:** no nodes are out (`seeded()` false, `sweeps` empty). `charge` rises by `1 / profile.chargeTicks` a
  tick and holds at 1. When the look turns at `flickTurn` or more in one tick, the phase becomes THROWING with
  `threw` true. The arc centre is the previous tick's look; the flick direction is the unit turn.
- **THROWING:** lasts `throwTicks`. The aim is set by the rules, not the caller: it runs from centre minus half
  `arcDegrees` along the flick direction to centre plus half. The chain is seeded straight along the start aim at
  full length. On the last tick `crack` is true with `crackCharge` = the charge at the throw, and the phase is LIVE
  with the aim at the arc's end. `struck` ends it early the same way. `tipBlocked` is true on a tick the clipper
  moved the tip.
- **LIVE:** the aim is the caller's. Rope: each node keeps `(node - previous) * damping`, is pulled toward its place
  on the aim line by a fraction that falls from near 1 at the hand to `aimPull * taut` at the tip, is clamped to
  `maxNodeSpeed`, to the stretch limit (1.2 rest lengths from its anchor) and by the clipper. A reversal or a stroke
  end with the tip at `crackSpeed` or more cracks, at most once per 20 ticks, with `crackCharge` 0. The existing
  stroke-tiring rule (90 degrees of one-way turn) stays, measured on the whip aim. After `restTicksToFade` ticks
  with the aim turning under `restTurn`, the phase is FADING.
- **FADING:** `extent` falls from its value to 0 over `fadeTicks`; length is `length * extent`. An aim turn of
  `flickTurn` or more returns to LIVE and `extent` regrows to 1 over `regrowTicks`. At 0 the phase is CHARGING with
  `charge` 0 and no nodes.
- `release` from CHARGING: seeds the chain straight along the look, phase LIVE, returns the charge, records the
  crack time. From LIVE or FADING: aim becomes the look, `extent` 1, phase LIVE, returns 0, records the crack time.

- [ ] **Step 1:** Rewrite `LashRulesTest` with one test per bullet above, plus:
  `ropeStaysBoundedUnderAThousandRandomAims` (seeded `Random`, every node finite and within `length * 1.2 * 1.05`
  of the hand each tick), `twoStatesFedTheSameInputsMatchExactly`,
  `aFlickOfTenDegreesReachesFullSpeed` (a LIVE aim turned 10 degrees a tick for 3 ticks on a 7.5 block lash gives
  `tipSpeed >= 1.2`), `aSlowTurnNeverThrows` (5 degrees a tick while CHARGING stays CHARGING),
  `crackDamageAtFullChargeIsNineTimesTheMultiplier`, `onlyTheThrowCarriesCharge`.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*LashRulesTest"`. Expected: fail to compile.
- [ ] **Step 3:** Rewrite `LashRules` to the interface. Keep `Clipper`, `NODES = 10`, `REACH_BONUS`, `length(reach)`,
  `STRAND_RADIUS`, `CRACK_RADIUS`, `CRACK_COOLDOWN`, `SNAP_COOLDOWN`. Delete `NEAR_FOLLOW`, `TIP_FOLLOW`, `follow`,
  `SLACK_SPEED`, `FULL_SPEED`, `CRACK_TURN` and fix their readers to compile against `tuning()` (the renderer's
  heat ramp at `StrandRenderer.java:153-154`).
- [ ] **Step 4:** Run the test. Expected: pass. The project will not fully compile until Task 3; that is expected.

---

### Task 3: Server lash — phases, costs, cracks, cue

**Files:**
- Modify: `StrandService.java` (`begin`, `hold`, `snap`, `lashTick`, `twinLashTick`, `crack`, `cue`, `end`),
  `StrandSession.java`, `StrandShaping.java` (`bloodPerPulse(Player)`), `StrandCue.java`
- Test: `StrandCueTest`, `src/gameTest/.../StrandGameTests.java` (lash cases rewritten)

**Interfaces — Consumes:** Task 2's `LashRules`.

**Interfaces — Produces:**

```java
// StrandSession
float aimYaw, aimPitch;      // the whip aim the server holds; the look until Task 4 feeds it
// StrandService
public static void aim(ServerPlayer player, float yaw, float pitch);   // Task 4 calls it; clamps and stores
// StrandCue.Decoded gains: LashRules.Phase phase  (bits 23-24); for a LASH, param is the charge
public static int encode(StrandForm form, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary,
        boolean leftArm, double param, int skill, boolean twin, LashRules.Phase phase);
```

Behaviour:

- `lashTick` steps the rules with the session's whip aim. `aim(...)` rejects non-finite values, clamps the aim to
  `maxAimOffset` of the player's look and its change to `maxAimStep` a tick, and is ignored unless the phase is
  LIVE or FADING.
- THROWING: each tick, test `LashRules.sweeps` with `ManipulationCombatHelper.swept`. On the first creature, call
  `LashRules.struck` and crack at that creature's box centre; on `tipBlocked`, the same at the tip; else crack when
  the step says so. A crack's damage is `LashRules.crackDamage(charge, profile)` through `StrandHits.strike` as an
  accent; `StrandHits.stateTicks` is multiplied by the same `lerp(1, chargedMultiplier, charge)` for that hit.
- LIVE: sweeps and reversal cracks as today, from the new step.
- CHARGING and FADING deal nothing.
- Cost, in `StrandShaping.bloodPerPulse(Player)`: CHARGING pays `profile.chargeBloodPerPulse`, THROWING and LIVE pay
  `spec.bloodPerPulse`, FADING pays 0. Check that `ShapingDispatcher` keeps a channel open through a zero-cost
  pulse; if it does not, make FADING skip the pulse rather than end the session.
- `snap`: CHARGING calls `LashRules.release` and cracks along the look for the returned charge at no snap price;
  LIVE or FADING pays `snapBlood` as today, calls `release` and cracks along the look.
- Send the cue at once on every phase change, as well as each pulse.
- The twin steps with the same aim and phase; it never cracks.

- [ ] **Step 1:** Extend `StrandCueTest`: every `Phase` round-trips through `encode`/`decode` with skill 3 and twin
  set; the count stays positive.
- [ ] **Step 2:** Rewrite the lash cases in `StrandGameTests` (they script the lash through `StrandService.lashOf`
  and the player's rotation; keep that style):
  `lashChargesBeforeAnythingIsOut`, `aFlickThrowsAndCracksOnTheFirstBodyInTheArc`,
  `aFullChargeCrackHitsHarderThanAnEmptyOne`, `theThrowCracksAtABlockItMeets`,
  `aStillWhipFadesAndReturnsToTheOrb`, `aFlickDuringTheFadeRevivesIt`, `fadingCostsNoBlood`,
  `leftClickWhileChargingReleasesDownTheCrosshair`, `leftClickWhileLivePaysTheSnapAndRecentresTheWhip`,
  `chargingRunsDryAndEndsCleanly`, `releaseMidThrowClearsTheSession`, `anAimBeyondTheLimitIsClamped`,
  `aNonFiniteAimIsIgnored`. Delete the cases they replace.
- [ ] **Step 3:** Run `./gradlew.bat test --tests "*StrandCueTest"`. Expected: fail.
- [ ] **Step 4:** Implement.
- [ ] **Step 5:** Run `./gradlew.bat test --tests "*Strand*" --tests "*LashRulesTest"`, then the strand GameTests
  (batch filter per `docs/TESTING.md`). Expected: pass; Jet, Spray, Plant and Brand cases unchanged and green.

---

### Task 4: Whip aim over the network

**Files:**
- Create: `common/network/capa/harbinger/manips/LashAimPacket.java` (`playToServer`, `float yaw, float pitch`),
  `LashAimSyncPacket.java` (`playToClient`, `int entityId, float yaw, float pitch`)
- Modify: `PacketHandler.java` (`PROTOCOL_VERSION` `"49"` to `"50"`, with Task 5's payload in the same bump),
  `src/test/resources/network/payload-registry.txt`, `client/data/StrandClientState.java`, a
  `client/network/Clientbound*Handlers` body, `StrandService.aim`
- Test: `StrandGameTests`, the network guardrail tests

**Interfaces — Consumes:** `StrandService.aim`, `StrandCue.Decoded.phase()`.

**Interfaces — Produces:**

```java
// StrandClientState
public static void acceptAim(int entityId, float yaw, float pitch);   // a watcher's copy of another caster's aim
public static void localAim(float yaw, float pitch);                  // Task 6 sets the local caster's aim each frame
// Strand gains: float aimYaw, aimPitch; LashRules.Phase phase (from the cue; the local caster predicts its own)
```

- `LashAimPacket.handle`: `ServerboundGuards.activePlayer`, using an item, strand open, rate key `"lash_aim"` at one
  per tick; then `StrandService.aim`. `aim` relays with `PacketHandler.sendTracking` (watchers only).
- The local client sends one `LashAimPacket` per client tick while its lash is LIVE or FADING, from
  `StrandClientState.tick`'s caller.
- `StrandClientState.follow` for a LASH steps the rules with the strand's aim: the local aim for the local caster,
  the relayed aim for others, the look when none has arrived. The local caster's phase is the rules' own (so the
  camera cap engages on the tick of the flick); another caster's phase is corrected to the cue's whenever a cue
  arrives.

- [ ] **Step 1:** Add `payload-registry.txt` lines for this task's two payloads in the file's order and run
  `./gradlew.bat test --tests "*Network*"`. Expected: fail (unregistered).
- [ ] **Step 2:** Add the two payloads, their registration and the version bump to `"50"`. Task 5 adds its own
  payload and registry line under the same version; the mod is unreleased, so one bump covers both.
- [ ] **Step 3:** Wire the client state and the per-tick send.
- [ ] **Step 4:** Run `./gradlew.bat test --tests "*Network*" --tests "*CommonClientIsolationTest"` and the strand
  GameTests. Expected: pass.

---

### Task 5: The tap

**Files:**
- Create: `common/network/capa/harbinger/manips/LashTapPacket.java` (`playToServer`, unit payload)
- Modify: `StrandService.java`, `StrandCue.java` (`Accent.TAP`), `client/event/ClientInputEvents.java`,
  `PacketHandler.java`, `common/event/TransientServerStateEvents.java` if a new map needs registering
- Test: `StrandGameTests`, `StrandCueTest`

**Interfaces — Produces:**

```java
public static final BloodAccounting.Source TAP = BloodAccounting.Source.of("lash_tap", Category.CAST, true, false);
/** A tap from the hand along the look. False, paying nothing, when not allowed now. */
public static boolean tap(ServerPlayer player);
```

- `tap` requires: alive, not using an item, the Projection tool or Staff in hand, the selected manipulation's
  Projection Shaping is a `StrandShaping` with form LASH (find it the way `StrandShaping.opening` does, through
  `ShapingDispatcher.currentClaim(player, Verb.PROJECT)`), and `tapCooldown` ticks since this player's last tap
  (a UUID-keyed session map in `StrandService`, cleared in `clearSessionState` and `logout`).
- Price `profile.tapBlood` times the cost ledger's multiplier, as `snap` prices itself; refuse if it cannot be paid
  in full.
- Strike: from `StrandCue.hand` along the look to `StrandService.length`, clipped by blocks; the nearest creature
  from `ManipulationCombatHelper.swept(player, line, LashRules.STRAND_RADIUS)` only. Damage
  `profile.tapDamage * damageScale * mastery * Whipcrack`. With `tapStateTicks > 0` it lands as an accent for that
  duration; otherwise damage only. `StrandHits.strike` needs a session today: give it an overload that takes the
  manipulation, spec and skill levels, and have the session version call it.
- One `Accent.TAP` burst from the hand to the end point, and a sound.
- Client: handle `InputEvent.InteractionKeyMappingTriggered` for the attack key. When the player holds the
  Projection tool or Staff with a lash selected, is not using an item, and `minecraft.hitResult` is a miss or an
  entity: cancel, suppress the swing, send `LashTapPacket`. A block hit is left alone. `handleStrandSnap` keeps the
  in-use left click and now also fires while the local lash is CHARGING.

- [ ] **Step 1:** GameTests: `aTapHitsTheFirstBodyOnTheLineOnly`, `aTapPaysItsProfileBlood`,
  `aSecondTapInsideTheCooldownPaysNothing`, `aTapLaysNoStateUnlessItsProfileSays` (Searing none, Gloam Obscured),
  `aTapWithoutALashSelectedDoesNothing`, `aTapWhileChannellingIsRefused`. Unit: `Accent.TAP` round-trips.
- [ ] **Step 2:** Run them. Expected: fail.
- [ ] **Step 3:** Implement.
- [ ] **Step 4:** Run `./gradlew.bat test --tests "*Strand*" --tests "*Network*" --tests "*CooldownTierGuardrailTest"`
  and the strand GameTests. Expected: pass.

---

### Task 6: Camera cap

**Files:**
- Create: `client/input/LashCameraRules.java`, `client/input/LashCamera.java`, `mixin/client/MixinMouseHandler.java`
- Modify: `src/main/resources/hemomancy.mixins.json` (client list), `config/HemoClientConfig.java`, `en_us.json`
  (config label and tooltip)
- Test: `src/test/java/.../client/input/LashCameraRulesTest.java` (new)

**Interfaces — Consumes:** `StrandClientState.localAim`, the local strand's phase, `LashRules.tuning()`.

**Interfaces — Produces:**

```java
public final class LashCameraRules {
    public static final class State { double yawRate, pitchRate; }      // deg/tick the view is turning
    /** Degrees the view turns this frame for a raw turn in degrees over dtTicks. strength 0 = vanilla. */
    public static double[] view(State state, double rawYaw, double rawPitch, double dtTicks,
            double cap, double drag, double strength);
}
public final class LashCamera {
    public static boolean active();                                      // local lash LIVE or FADING, strength > 0
    public static void turn(LocalPlayer player, double yawUnits, double pitchUnits);   // the mixin's only call
    public static void reset();                                          // on release, death, logout, dimension change
}
```

- `view`: with `strength` 0 it returns the raw turn. Otherwise the turn's magnitude is limited to
  `cap * dtTicks` (direction kept), blended with the raw turn by `strength`; with no input the last rate carries on,
  multiplied by `drag^dtTicks` each frame, until under 0.05 degrees a tick. The excess is dropped, never banked.
- `LashCamera.turn`: not active, pass straight to `player.turn`. Active: add the whole raw turn to the whip aim
  (pitch clamped to plus or minus 90) and call `StrandClientState.localAim`; give `player.turn` what `view`
  returns. `player.turn` takes units of 0.15 degrees. When the phase leaves LIVE and FADING, the whip aim is
  dropped; on entering LIVE it starts from the rules' aim at the end of the throw.
- Mixin: redirect the `LocalPlayer.turn(DD)V` call inside `MouseHandler.turnPlayer` to `LashCamera.turn`. Confirm
  `turnPlayer` runs every frame while the mouse is grabbed, so the drag has frames to run on; if it does not, drive
  the drag from a client tick instead. `dtTicks` comes from the client's real-time frame delta, not a wall clock
  (`AnimationTimeGuardrailTest`).
- Config: `lashCameraDamping`, double, 0 to 1, default 1.

- [ ] **Step 1:** `LashCameraRulesTest`: `zeroStrengthIsTheRawTurn`; `aTurnUnderTheCapPassesWhole` (3 deg in one
  tick, cap 4, gives 3); `aHardFlickIsCapped` (40 deg in one tick gives 4 in the same direction);
  `theExcessIsNotBanked` (after the 40, a 0 input frame gives `4 * 0.6`, then `4 * 0.36`, then under 0.05 it is 0);
  `halfStrengthBlends` (40 raw, strength 0.5, gives 22); `frameRateDoesNotChangeTheCap` (the same 40 deg/tick fed as
  four quarter-tick frames totals 4).
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*LashCameraRulesTest"`. Expected: fail to compile.
- [ ] **Step 3:** Implement the rules, then `LashCamera`, the mixin and the config. Call `LashCamera.reset` from the
  client session hooks (`ClientSessionEvents`) and when the local strand is released.
- [ ] **Step 4:** Run the test and `--tests "*architecture*"`. Expected: pass.
- [ ] **Step 5:** Launch `./gradlew.bat runStrandReviewClient` and check by hand: charge, flick, the view nudges and
  settles, the whip carries on; with the config at 0 the view turns as vanilla. A mixin can only be proved in a
  running client.

---

### Task 7: Drawing

**Files:**
- Modify: `client/render/world/StrandRenderer.java`, `client/player/StrandPlayerPose.java`, the first-person arm
  path that reads `strand.turnRate` / `strand.reversed`
- Test: existing geometry and architecture tests (`VanillaParticleRatchetTest`, `RenderTypeCachingGuardrailTest`,
  `HudAndWorldDrawingGuardrailTest`)

**Interfaces — Consumes:** `Strand.phase`, `strand.lash.charge()`, `strand.lash.extent()`, `Accent.TAP`.

Read `docs/MANIPULATION_VISUALS.md` first. Reuse the outpouring and cell-hand materials through their existing
public bridges; no new shader art, no vanilla particles, no `RenderType` built per frame.

- **Orb (CHARGING):** blood orbiting a core at the hand point (`StrandCue.hand`). Radius and orbit speed rise with
  `charge`; at 1 it tightens and pulses once a second. School colour from the cue as the strand uses now.
- **Throw (THROWING):** the orb shrinks to nothing over the throw as the strand's drawn length grows from the hand.
- **Live:** the existing lash ribbon from the rope's nodes. Check the swish trail still reads at the new speeds.
- **Fade (FADING):** drawn length follows `extent`; width thins with it and beads of blood run back along the
  strand toward the hand. It must read as leaving, not as a short whip.
- **Tap (`Accent.TAP`):** a thin fast ribbon from `from` to `to` over about 5 ticks with a small flare at the end.
- **Poses:** the first-person arm and `StrandPlayerPose` aim along the whip aim; in CHARGING the hand is held up
  around the orb.
- The existing `presence` fade on release stays.

- [ ] **Step 1:** Implement the five visuals and the pose changes.
- [ ] **Step 2:** Run `./gradlew.bat test --tests "*architecture*" --tests "*render.world*"`. Expected: pass.
- [ ] **Step 3:** Launch `runStrandReviewClient`, capture each of the three lashes in each phase and the tap, in
  first and third person. Look at them. No rod, no vanilla particles, orb charge readable.

---

### Task 8: Live tuning in the review client

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/StrandClientReview.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LashTuningFile.java`
- Modify: `gradle/review-clients.gradle` (a row beside `strandReviewClient`)

```groovy
lashTuningClient: [dir: 'build/lash-tuning-client', user: 'LashTuning', size: review,
                   props: ['hemomancy.strandReview': 'true', 'hemomancy.lashTuning': 'true']]
```

- With `hemomancy.lashTuning` set, the client does not run the review's scripted captures. It gives the player
  active blood, the three lashes known, the Projection tool, and a flat arena with still and moving targets, then
  leaves the player in control.
- `LashTuningFile` writes `lash-tuning.json` in the game directory with `LashTuning.DEFAULT` when it is missing,
  re-reads it when its modified time changes (checked once a second) and calls `LashRules.setTuning`. A malformed
  file keeps the last good values and reports the error in chat through a lang key.
- A readout of phase, charge, tip speed, whip-aim offset from the view and camera turn rate. Draw it the way
  `StrandClientReview` already shows review text; if it shows none, register a layer in `GuiLayerRegistrar` gated on
  the property.
- The integrated server shares the JVM, so one `setTuning` reaches both sides. That is the only supported setup.

- [ ] **Step 1:** Implement.
- [ ] **Step 2:** Launch `./gradlew.bat runLashTuningClient`; change `cameraCap` in the file while playing and see
  the view change within a second.
- [ ] **Step 3:** Hand the client to the owner for tuning. When they give final values, write them into
  `LashTuning.DEFAULT` and the `Shapings` profiles and update the tests that pin them.

---

### Task 9: Text, lessons, docs, full check

**Files:**
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`, `common/entity/npc/dialogue/CoreLoopLessons.java`
  and the Liber entries that teach the lash (`LiberEntryDefinitions`, `LiberDiscoveryEvents`), following the
  speaker sheets in `docs/NPC_VOICES.md`
- Modify: `docs/archive/tracks/core_loop/12-strand-combat.md` (the Lash and snap sections), `docs/archive/tracks/core_loop/12a-lash-spike.md`
  (status line: superseded by the spec), `docs/MANIPULATIONS_AND_CASTING.md`, `docs/HEMOMANCY_REFERENCE.md`,
  `docs/OPEN_QUESTIONS.md` (CL-22 moves to rulings: left click taps; right click charges)
- Regenerate: `python gradle/source-text-impact-map.py`

- [ ] **Step 1:** Rewrite every player-facing line that says the lash is swept by turning or cracked by reversing:
  tap with left click, hold right click to gather, flick to throw, keep the hand moving, left click to release or
  recentre. Check the advancement text for the first crack still fits.
- [ ] **Step 2:** Update the five docs.
- [ ] **Step 3:** Run `./gradlew.bat test`. Expected: green, including `EnglishLiteralAllowListTest`,
  `HardCodedEnglishGuardrailTest`, `VesperManipulationCoverageTest`, `searingLashLaysALongSearing` and
  `BalancePassVisualSourceTest`.
- [ ] **Step 4:** Run `./gradlew.bat alphaCheck`. Expected: green apart from the known pre-existing playthrough
  failures; list any failure by name and say whether it touches a strand.
- [ ] **Step 5:** Report what was built, what was verified in a client and what was not, and the list of numbers
  waiting on the owner's tuning.
