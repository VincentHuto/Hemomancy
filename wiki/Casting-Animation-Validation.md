# Casting animation validation — 2026-09-16

## Cast outpouring (2026-10-19)

The caster-body anatomy, hand veins and Lux wings described below were removed and replaced by the cast outpouring (see `Casting-Animations.md`). The outpouring review client (`./gradlew.bat runOutpouringReviewClient`, then drop `{"op":"create","name":"OutpouringReview","seed":1}` into `build/outpouring-client-review/operator/inbox/`) previews:

- every school at Humilis, Summa and Perfectus, each across a rotating section, at build, peak and aftermath
- an Animus section sweep
- Lux and Tenebris at night and in first person
- an armoured caster
- Minimal particles

Shots land in `build/outpouring-client-review/screenshots`.

## Lux wings revision (superseded)

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

# Casting animations v2: 2026-10-19

The v2 rebuild ([spec](../docs/superpowers/specs/2026-10-19-casting-animations-v2-design.md), [current behaviour](Casting-Animations.md)) replaces the three-pose clips and the purpose blend with authored keyframe tracks in both views, an aim layer, 31 bespoke styles and the Unstained Still Art lane. It deliberately reverses parts of the restrained-motion revision above: torso pitch returns (pivoted about the waist and capped at ±14°), Magister holds carry small loops, and Summa and above use larger rank-scaled gestures. The torso-separation artifact that prompted the restraint is guarded by the waist-pivot invariant test.

## Automated coverage

All of the following is source and test evidence. None of it has been seen in a live client yet.

- **Track model.** `CastingTrackRulesTest` covers easing shapes, HOLD steps, strain blending by charge, release leaving from strain, loops within amplitude and period, passive on/off, phase durations from segments and return to rest. `CastingPlaybackTest` covers track-defined phase boundaries, duration replacement when the track changes, charge refresh without restarting opening, stale holds and replacement of recovery by a new cast.
- **Schools.** One test class per school (`AnimusTracksTest` through `TenebrisTracksTest`) checks timing by rank, continuity and return to rest, rotation and pitch bounds, Humilis moving only the lead arm and head, the rank at which both arms, the waist and the stance join, first-person hands clear of the crosshair, monotonic strain where authored, and each school's verb (for example Ductilis HOLD steps and head counter-jerks, Congeatio's same-tick lock and 3-tick fracture, Ferric's dead stop and recoil). `CastingTracksTest` repeats bounds, crosshair, mirroring, Humilis torso stillness and Perfectus stillness over 240 ticks across every base.
- **Bespoke styles.** `StyleTracksTest`, `PerfectusTracksTest`, `GameplayTracksTest` and `ChannelTracksTest` check that exactly their styles resolve and are cached, the shared continuity and bounds contract, and each style's defining motion (crown then guard, bell swing, empty Absolute Stillness loop, Riposte sweep, head-led Deadly Gaze, Marionette alternation, channel loops that differ from the Magister base, passive on inward and off outward).
- **Layers and rendering rules.** `AimLayerRulesTest` (ramp, hold and fade; lead arm and head only), `FirstPersonHandRulesTest` (lead is the main arm, left-handed mirroring, a hand switch mid-hold keeps the same lead pose, Humilis and Awakened never touch the off hand, clamping, the crosshair band), `WaistPivotRulesTest` (the waist point stays put for every allowed pitch, pitch clamp, head compensation, stance weight continuity across speed) and the `CastingPose` sign test for `toModel()`.
- **Still Arts.** `StillArtTracksTest` checks every stage in both views, settling to rest, open hands and no thrown-back head, Awakened moving only the main arm, symmetric later stages, Vigilant and above narrowing the stance, Enlightened stillness after the settle, ending bounds and crosshair clearance, and Unstained packets resolving the stage track and ending. `StillArtPresentationSourceTest` checks that `StillArt.performAction` publishes one release after a successful cast and that `StillArtInit` has no vanilla `ParticleTypes`. `CastingAnimationPacketTest` round-trips both lanes, including a null school.
- **Documentation and preview.** `CastingDocsSourceTest` checks that [Casting-Animations.md](Casting-Animations.md) names every bespoke style and Still Art ending, that `/hemocastpreview` registers the `still` form and the `style`, `type` and `charge` options, and that the reference describes Still Art presentation.
- **GameTests.** The `casting_animation_validation` namespace adds `StillArtAnimationGameTests` (one release per successful Still Art, to the caster and trackers; none on unknown art, cooldown or low clarity) beside the existing registry, quick-cast, charge, continuous and late-tracking tests. Running it found and fixed a pre-existing registry gap: `axonal_transduction` had no presentation purpose and now maps to Self.

Commands: `./gradlew.bat test --tests '*Casting*Test' --tests '*TracksTest' --tests '*StillArt*Test'`, `./gradlew.bat runCastingAnimationGameTestServer` and `./gradlew.bat alphaCheck`. Results for the final integrated tree are recorded by the run that closes this work, not here.

## Build rulings to check live

- The arm-z convention (right z negative is outward, flipped once by `CastingPose.toModel()`) was confirmed from vanilla source by two independent readings but not yet by eye. If every spread pose folds inward in the review client, this is the cause.
- Wide and cruciform poses use a small arm x with a large z, because at x −90 z only rolls the arm. Lux, the Perfectus iconics and the older bespoke styles were re-authored for this; they need a side view to confirm.
- The quick-cast and Still Art local flick was dropped, so nothing animates before the server confirms a cast.
- Bespoke styles skip the purpose aim layer. White Verdict, the one bespoke style with a projectile purpose, therefore releases from its cruciform rather than pointing at the aim.

## Live captures 2026-10-19

The `casting-v2` two-client run (`-PcastingRun=casting-v2`, localhost port 25579, SLIM skins only) now sweeps the 40 school and rank profiles, the 31 bespoke styles (each sent as the registered manipulation that owns it, passives as on/off), the five Still Art stages and the integration scenes. Each client saved **203 PNGs** (`build/casting-v2-Caster/screenshots`, `build/casting-v2-Observer/screenshots`); findings, commands and per-check filenames are in `.superpowers/sdd/2026-10-19-casting-animations-v2/live-captures.md`. Three fixture defects were fixed on the way: sequences below the last accepted one were dropped by the client order guard, all school scenes shared one manipulation id so the client kept the first track, and the walking scene carried the caster off the stage before the crouch, cancel, release and replace scenes.

Seen in the captures: spread poses open outward and authored inward poses stay inward, so the arm-z conversion reads correctly; Lux Summa to Perfectus hold a sideways cruciform; torsos stay joined at the waist with no drift between the two hold shots; first-person hands leave the crosshair clear in every hold; Still Art hand rings appear in both views and the feet ring from Discerning up, with no blood tint. Problems: the Still Art rings render charcoal to mid grey rather than pale; Phoenix Debt's passive on gesture covers the crosshair; Lux and several wide bespoke holds show no first-person hands; Tenebris's 12 to 20 degree twist does not read as turned away from a front-quarter view; Ironheart's first-person arm floats with its cut end on screen. No WIDE skin pass, side or rear camera, or motion review was done.

### Visual fix pass and re-capture (`casting-v2b`)

The problems above were fixed and re-captured in run `casting-v2b`: 203 PNGs per client under `build/casting-v2b-Caster/screenshots` and `build/casting-v2b-Observer/screenshots`, with contact sheets in `build/casting-v2b-sheets/`. Per-item evidence is in the live-captures note.

- **Still Art rings: PASS (daylight).** The rings now draw as flat, untextured quads through the unlit Lux wing type (`StillArtRings`). The textured `LUX` material had sampled a dark texel. Near-white ring pixels in `casting-still-resolute-release.png` (first person) rose from 709 to 8522, with a mean of 245/246/248. The observer hand and feet rings read white with a silver edge.
- **First-person hands: PASS.** The new rule `FirstPersonHandRules.onScreen` keeps the projected fist inside a conservative frame and the arm's cut end out of view. Holds that paired a lift (+ty) with a downward pitch (negative rx) were re-authored across 32 tracks. Hold and held shots with no hand above the hotbar band went from 25 to 1; the remaining one is an opening frame.
- **Phoenix Debt crosshair: PASS.** The crosshair check now also tests the fist, and passive on/off gestures are checked. The fold sits low and to the sides.
- **Ironheart: PASS.** The raised hammer arm rises from the bottom edge with no cut end in view.
- **Tenebris: improved, PASS with a caveat.** Hold twists are now 25° to 28° (strain up to 30°), and the shoulders read as turned away. The observer framing differs between runs, and there is still no side or rear camera.
- **Head sink: PASS, low confidence.** The head follows the neck forward but keeps its height (`WaistPivotRules.headOffsets`).
- **Stale track: fixed in code.** The client re-resolves the track whenever any presentation input changes (`CastingTracks.presentationChanged`).

The focused JVM suite passes: 35 classes, 257 tests, 0 failures. Still open: a night capture of the rings, a WIDE skin pass, and a side or rear camera. Animus Perfectus's first-person fists sit near the bottom centre, and Flammeus Perfectus's sit low at the hotbar line.
