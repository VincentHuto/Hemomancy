# Casting animation validation — 2026-09-16

## Lux wings revision

Lux's Summa, Magister and Perfectus anatomy now renders paired white wings instead of the eye canopy. `build/lux-casting-wings-validation.log` records **33 focused JVM tests passing** and a successful `assemble`. The new geometry checks cover all rank gates, increasing wingspan, mirrored shoulder roots throughout unfolding and long holds, white vertex colors, fade opacity and the bounded mesh size. The suite also includes existing casting playback, pose, ordering, Lux/Umbral geometry and vascular strand continuity checks.

The wings have not yet been inspected in a live client. The older Lux screenshots below show the previous eye canopy and do not validate the new wing silhouette or material.

## Restrained-motion revision

The subsequent motion pass replaces every repeating body/arm hold with a fixed pose, moves active gestures to chest/shoulder height, reduces first-person rotations and removes casting-driven torso pitch. It also removes instantaneous Ductilis release jumps and the bespoke opening-to-hold discontinuity. Surrounding anatomy can animate independently.

`build/casting-restraint-tests.log` records **18 focused tests passing**, including new checks for all 40 presentations remaining still for 240 ticks, upright torso tracks, elevated active hands, small first-person rotations and continuous opening/hold/release boundaries for every bespoke style and purpose. The two new failure cases reproduced the original looping and torso-pitch problems before the fix. `compileGameTestJava` also passed; gameplay code and network cadence were not changed by this revision.

The disposable `casting-restraint` two-client run captures each of the 40 profiles at two hold ages as well as release and integration scenes. First- and third-person Animus captures and third-person Mortem/Ferric captures were inspected: the torso stays joined to the waist and active hands remain raised. The two Animus hold frames retain the same body pose while the blood anatomy moves. These revision captures supersede the earlier pose-quality examples below.

- [Revised Animus hold](../build/casting-restraint-Observer/screenshots/casting-animus-magister-hold.png)
- [Same hold later](../build/casting-restraint-Observer/screenshots/casting-animus-magister-held.png)
- [Revised Mortem](../build/casting-restraint-Observer/screenshots/casting-mortem-magister-hold.png)
- [Revised first person](../build/casting-restraint-Caster/screenshots/casting-animus-magister-hold.png)

## Automated evidence

`build/casting-final-verification.log` records **36 focused JVM tests and 8 dedicated-server GameTests passing**. The focused set includes casting tracks/playback/order, manipulation input, pose priority, Living Torch, Living Staff morph sequences and vial injection poses/playback. The dedicated run uses the `casting_animation_validation` namespace rather than the unrelated full GameTest suite.

Coverage includes every nonretired registry manipulation resolving explicit metadata, all 40 school/rank compositions, all phase payloads, mirroring, both-view recovery continuity, charge reduction without clock reset, same-sequence charge release, cancellation, rejected quick casts, immediate payment/cooldowns, uninterrupted channel upkeep and late tracking without old release replay. Presentation metadata stays outside saved manipulation data.

A source review found and fixed shared first-person pose-stack leakage, accidental third-person pose application during first-person model setup, and stale visual sessions surviving tracking departure. The follow-up review found those issues resolved. Terminal ordering survives visual expiry in a bounded, transient cache.

## Two-client captures

Disposable localhost runs used `CastCaster` and `CastObserver`, an empty quartz stage, and independent game directories under `build/`. They did not load the normal development world. The main capture run, `casting-review-0916b`, saved **64 PNGs per client**:

- All 40 school/rank holds and one Summa release per school.
- A real Sanguine Ward channel, observer teleport out/back into tracking, and channel cleanup.
- Two simultaneous Ferric Perfectus casters with full and Minimal effects.
- Armor, held sword/apple, handedness, walking, crouching, cancellation and replacement scenes.

Observer logs show the retracked channel in HOLD at age 31 rather than restarting; the cleanup capture reports no animation. First-person captures show separately moving hands with a clear crosshair. Representative images were inspected for the contrasting Lux, Ferric and Tenebris compositions, Animus anatomy, first-person arms, armor and occupied hands.

Representative local outputs:

- [Lux Perfectus](../build/casting-review-0916b-Observer/screenshots/casting-lux-perfectus-hold.png)
- [Ferric Perfectus](../build/casting-review-0916b-Observer/screenshots/casting-ferric-perfectus-hold.png)
- [Tenebris Perfectus](../build/casting-review-0916b-Observer/screenshots/casting-tenebris-perfectus-hold.png)
- [First-person Animus](../build/casting-review-0916b-Caster/screenshots/casting-animus-mediocritas-hold.png)
- [Armor](../build/casting-review-0916b-Observer/screenshots/casting-armor.png)

The initial `0916a` run hid hands with the HUD, so its caster captures are not first-person evidence. The main `0916b` run corrected that. A short `0916c` integration pass logs actual skin model types and exercises shield use through the client input path; setting server use state alone was insufficient for that first-person check.

The `0916c` logs confirm both initial players used SLIM models. A separate `0916d` pass used `CastCaster1`, confirmed WIDE, and repeated armor, occupied hands, shield use, handedness and movement captures. The WIDE armor and first-person captures were inspected. The shield-use capture shows vanilla blocking with casting hand effects suppressed, while the presentation continues in state. Both review clients and their server exited normally after capture.

## Frame-time observations

The observer ran at 1280×800 with both clients capped at 120 FPS on this workstation. Reported values are wall-clock intervals between rendered frames, not isolated GPU timings:

| Scene | Mean frame interval |
|---|---:|
| Initial no-cast baseline, including warm-up | 9.42 ms |
| No-cast channel cleanup, warmed | 8.56 ms |
| Two Ferric Perfectus casters, full effects | 8.46–8.47 ms |
| Two Ferric Perfectus casters, Minimal effects | 8.49 ms |

The cap dominates these measurements. They show no obvious regression in this small scene, but do not establish available rendering headroom, a speedup from reduced effects, or a large-crowd performance budget. Large anatomy is bounded per caster and distance/quality culled; it adds no particle emitters or gameplay entities.

## Remaining acceptance limits

These tests and still captures do not replace a sustained combat review. Morphling replacement anatomy, Avatar overlays, every specialized weapon action, swimming/crawling/riding/Elytra, and larger multiplayer crowds still need visual inspection together. Required postures and specialized limb ownership are preserved by the integration rules, but that is not a claim that every combination has been live-verified. Teleport tracking was exercised; a real Umbral Step/Reversal combat sequence and dimension transition were not captured. Gameplay timings and costs were checked through the existing execution path and focused fixtures, not a full playthrough of every power.
