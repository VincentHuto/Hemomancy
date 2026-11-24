# Naeglerophaeon axonal swimmer validation

The runtime rig comes from `models/entity/bbmodel/NaeglerophaeonRedesign.bbmodel` in the
Hemomancy assets. Run `python tools/model_export/export_naeglerophaeon.py` after editing its
embedded texture or rig. It exports the geometry and both atlases. The authored pose uses
one texel per model unit.

The core has seven emissive ganglia. Twelve jointed arms carry the crimson membranes and
conduct pulses to the animated tips. The hooked axon keeps its authored silhouette at rest,
unfurls to the shared weak point on the victim, and curls back on release. The rig also
poses for the lunge, lash, volley, nova, conduction, phase transition, dive and emergence.
During Overload the body inverts and light climbs the hanging axon. Drained bodies use the
desaturated atlas, twitch gently and skip the emissive pass.

## Automated checks

Commands:

```powershell
./gradlew.bat test --tests '*Naeglerophaeon*'
./gradlew.bat runNaeglerophaeonGameTestServer
```

The focused JVM suite sends the animated rig through its render passes and checks finite vertices,
the 4,000-quad ceiling, web and light separation, the lunge and dive poses, pulse timing, shared
grip coordinates, release interpolation, turn-history resets and resource/combat contracts.

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

The 2026-11-19 redesign review captured idle, grab, release, phase transition, Overload and
Drained in the native client. `naeglerophaeon-redesign-front.png`, `membrane-phase-3.png`,
`naeglerophaeon-redesign-finale.png` and `membrane-phase-18.png` show the new rig.
All 26 focused JVM tests passed. The dedicated run passed 59 of 60 GameTests. Its remaining
Axonal Transduction test expects 150 mL on entry, while the fixture's dry cast costs 187.5 mL.

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
