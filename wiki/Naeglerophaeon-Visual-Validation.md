# Naeglerophaeon axonal swimmer validation

The current model replaces the older eye-and-perch design. It has a maroon core, seven luminous
orbs, twelve 12-block ivory tendrils, translucent red webs, and a 30-block crimson capture tail.
Three disjoint pulse slots limit axonal activity to three short tendrils; tail nodes stay lit.
Geometry is sampled by arc length, with transported frames and 32/64-block detail reductions.
The core retains its 1.8 by 1.6 collision box; culling includes the full tail.

## Combat phases (2026-10-09 overhaul)

The fight is now two phases plus an Overload finale; see `docs/HEMOMANCY_REFERENCE.md`.
Each move has its own Blockbench pose in `NaeglerophaeonBlockModel`:
- lunge flare/tuck;
- lash coil and rolling starburst;
- nova cocoon and radial release;
- conduction reach;
- transition shake;
- dive/emerge body scaling (the boss is invisible in transit);
- the Overload inverted umbrella, with a writhing tail and a steady-node strobe that climbs on each 30-tick pulse, plus client tail bolts.

Phase-two core light is tinted violet. Drained bodies swap to
`textures/entity/naeglerophaeon_cube_palette_drained.png` (a desaturated copy of the cube palette). They skip the emissive
pass, draw former light organs in the opaque pass, and have no tip sparks.

## Automated checks

Commands:

```powershell
./gradlew.bat test --tests '*Naeglerophaeon*'
./gradlew.bat runNaeglerophaeonGameTestServer
```

The focused JVM suite covers finite geometry, the 4,000-quad ceiling, curve lengths and node
spacing, pulse uniqueness/order/terminal sparks, both detail reductions, outward sphere normals,
shared grip coordinates, history resets, release interpolation and resource/combat contracts.

The dedicated server suite covers gradual capture and terminal damage, actual multipart damage,
core interruption, victim removal, capture misses, obstruction during lightning windup and pulling,
isolated-platform flight, observer snapshots, and save/reload ownership without restoring a hold.
The overhaul adds 18 dedicated tests:
- lunge hit and wall stun;
- lash hit and back-off miss;
- volley dodge-then-hit;
- grab drain healing;
- single transition that persists through reload;
- node-to-node blink with transit invulnerability, and no blink without nodes;
- conduction along a fiber, sparing a player four blocks off it;
- nova in the open and behind cover;
- fiber-cut retaliation during a hold;
- the Overload line and floor, with kill-damage bypass;
- node burn-out with 2-6 sparks and no drops, then Drained death;
- immediate Drained with no nodes;
- the no-audience pause (its own batch);
- Overload/Drained reload.

`NaeglerophaeonCombatRulesTest` covers stage thresholds, health clamping, move priority, phase-two cooldowns, spark counts and the
sprint-dodge invariant. Damage numbers come from the server suite, not from the assisted client.
The `empty` template's stone floor tops out at helper y=5, and the GameTest framework encases the 32x12x24 volume in
barriers. Node fixtures must stay inside it.
Late tracking is verified by applying the real entity-data snapshot; it is not a two-client session.

## Live client review

`runNaeglerophaeonReviewClient` uses only `build/naeglerophaeon-client`. The opt-in fixture generates
a Vagrant Mind with seed 37 in a disposable End world and provides an orbit camera and combat setup.
The camera and automatic phase screenshots do not participate in the production renderer.

The review player has regeneration and may retain resistance from the disposable fixture. Damage
amounts are verified on the dedicated server, rather than inferred from this assisted client.
No quantitative frame-time benchmark is claimed.

## Art

The editable 64x64 tissue atlas is `textures/entity/naeglerophaeon.png`. The built-in imagegen
source and export details are recorded in `tools/model_export/naeglerophaeon-tissue-prompt.txt`.

## Overhaul review status: 2026-10-09

- 26 focused JVM tests and all 47 `naeglerophaeon_validation` GameTests passed (17 Axonal, 12 original, 18 new).
- The review fixture accepts `phase2` and `overload` ops in `membrane-review.json` to force each stage.
- A live client review of the new poses, Overload and Drained has **not** been performed yet.

## Completed review: 2026-09-23

- 15 focused JVM tests passed; all 12 dedicated server GameTests passed.
- Final combined run: `build/axonal-complete-checks.log` (successful build).
- Live single-player review covered swimming, turns, long-tail culling, root membranes, node
  sparks, capture/reel/discharge/recovery, first-person wrapping, and four bosses together.
- Normal client picking and attack packets hit the grip twice and broke the hold; see
  `AXON_GRIP_NETWORK_HIT` / `AXON_GRIP_NETWORK_RELEASE` in `build/axonal-release-validation.log`.
- A final packet correction preserves camera angles by sending zero relative rotation during
  pulls. The final server suite asserts the packet flags/deltas and that aiming changes survive.
- An independent read-only review found inverted sphere normals, directional orb illumination,
  and abrupt release geometry. All three were corrected; normals and release continuity have
  regression coverage. Orb illumination was inspected in-game after correction.

Screenshots under `build/naeglerophaeon-client/screenshots` include `axonal-swimmer-front.png`,
`axonal-swimmer-final.png`, `axonal-first.png` (multiple bosses), and the `membrane-phase-*.png`
phase captures. These are real game captures; the old membrane filename prefix is retained by
its disposable fixture. A two-graphical-client session and frame-time benchmark remain unperformed.
