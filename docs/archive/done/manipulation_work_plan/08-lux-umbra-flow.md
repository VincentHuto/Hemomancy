# Lux and Umbra: fluid light and liquid darkness

This revision replaces the physical material treatment for Lux and Tenebris manipulation cues. Lux uses unlit white light, soft warm fringes, fine streams and a small crimson Eye pupil. Umbra uses a shallow near-black liquid body, charcoal currents and eroded smoke. The former Beacon supports, Eye tubing, solid droplets, field tentacles and school-specific tendril meshes no longer draw for these effects.

## Rendering

`LuxUmbraGeometry` handles the beam, charge focus, Beacon, Eye, Flare, Suture, healing, Well, veil, teleport and slash forms before the shared physical renderer. Geometry consists of cards, narrow ribbons and a shallow field surface. Shader masks supply the bright cores, flowing density and feathered contours. The Eye faces the camera; its pupil occupies a separate small card. Verdict preserves its clipped endpoint and full damage radius, with a concentrated core and faint edge filaments. The Well body stays within 0.66 blocks above its original center; its exact-radius indicator remains separate.

`ShaderInit` registers `world/manipulation_lux` and `world/manipulation_umbra` through the existing `ShaderHolder` system. Both use `POSITION_TEX_COLOR`, Minecraft fog uniforms and the `HemoTime` animation uniform. UV tiles encode shape and seed while retaining each shape's normalized UV range. Broad noise drives the density; no additional texture asset is required. Uniform defaults are restored when each render state clears.

Both passes test depth, write color only and disable culling. `LuxUmbraBatch` sorts all their quads together from far to near before selecting the appropriate shader. Cues, moving connections, decorative mist and the Black Veil field share this ordering. Its storage is reused between frames. Terrain depth still occludes the effects. This is conventional quad sorting; it does not provide order-independent transparency with Minecraft's already-composited glass, water or third-party effects.

## Casting and lifetime

The existing cue refresh, source tracking, cancellation and retirement system remains authoritative. Lux and Umbra bypass coalescing mesh overlays and physical expiry fragments, using material fades and a bounded mist residue instead. Release connections appear immediately without postponing any gameplay event.

The appended forms are `LUX_MENDING`, `UMBRA_SLASH`, `LUX_MIST`, `UMBRA_MIST` and `UMBRA_ARRIVAL`. Existing form ordinals and packet layouts remain unchanged. Ferric Mending and other claw visuals keep their original routes. Teleport departures contract; the distinct arrival form reforms from a narrow concentration.

`ManipulationFlowPacket` adds the cosmetic `hemomancy:manipulation_flow` payload. It serializes the existing HutosLib anchors and `TendrilEffectConfig` with their own codecs. Lux Suture, Blood Eclipse and Black Veil retain moving endpoints, configuration lifetimes and surface adherence. The client derives only a 12-segment centerline, interpolates between ticks, and renders a shader ribbon. Surface raycasts occur once per game tick. If either entity disappears, the last resolved connection dissolves for up to eight ticks. Self-targeted Suture produces a small focus rather than invalid zero-length geometry.

Lux activations and impacts use the authored glow particles in a pale palette. Umbra accents use procedural smoke with the existing particle budget. The same treatment is applied to supported entity-cast paths. Essential cards, fields, beams and tethers remain visible with Minimal particles; optional mist is omitted. The renderer retains the 128-block effect limit, 48-block decorative mist limit, 192 cue cap, 128 flow cap and 128 mist cap. Black Veil fields are capped at 64.

Gameplay targeting, timing, damage, healing, costs, progression and sounds are outside this revision. Physical weapon models and other schools' primary visuals are unchanged by this pass. Preexisting checkout changes are preserved.

## Verification and replay

The original material pass passed all 33 JUnit tests, `compileGameTestJava` and `assemble`. That material review completed all 39 scenes, captured 702 frames and successfully reloaded resources. Its measured frame intervals were 8.69 ms mean / 11.67 ms p95 during active phases and 8.52 ms mean / 10.73 ms p95 during quiet phases.

Focused checks cover shader resources and uniforms, finite/bounded geometry, complete UV masks, horizontal and vertical beam endpoints and width, camera-facing Eye geometry, shallow Well geometry, teleport motion, cross-school transparency ordering, flow packet round trips, moving and missing anchors, self-targeting, cue lifecycle, exact boundaries and particle budgets. A JUnit adapter also runs the existing main-based Lux/Tenebris combat and HutosLib tendril contract checks.

```powershell
./gradlew.bat test --tests '*LuxUmbra*' --tests '*ManipulationLifecycleTest' --tests '*ManipulationBoundaryStyleTest' --tests '*ManipulationSignatureEffectsTest' --tests '*ManipulationParticleBudgetTest' assemble
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PluxUmbraReview runGameTestServer
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PluxUmbraReview runClient
```

The opt-in `LuxUmbraVisualReview` fixture uses only the disposable `VisceralReview` save under `build/visceral-review-client`. The init script places dedicated GameTests in `build/lux-umbra-gametest`. Neither command uses the normal gameplay save directory. The client fixture records 13 forms in daylight, darkness and Minimal-particle overlap scenes. Each scene includes formation, refresh and retirement; the study also includes resource reload, camera movement, vertical beams, nearby stone/glass, self-targeted Suture, moving endpoints, surface flows and entity removal.

Frame captures are written to `build/visceral-review-client/screenshots/lux-umbra-frame-*.png`, with separately named lifecycle stills. The assembled review is `build/lux-umbra-review/lux-umbra-review.webm`. These are real client captures sampled at five frames per second, not a render mockup. The accompanying logs retain the resource reload result and measured frame intervals. Screenshot work is included in those timings, so they are a scene-level sanity check rather than an isolated GPU benchmark.

The dedicated run completed 264 GameTests with one unrelated failure: `formationacceptsautopickedupoutput` found a preexisting stone block at its fixture position. The manipulation tests passed. That formation fixture was not changed. The client also reports existing unrelated resource problems, including invalid legacy texture paths, old broken-mana model resources, grimoire model resources and a `hemomancy:ray` uniform warning. These should not be mistaken for errors in the new Lux/Umbra shaders. Full JVM/alpha acceptance and a new two-client multiplayer visual review are not established by this focused pass.


## Ribbon joins and White Verdict in first person

The follow-up fixes two screenshot-reproduced problems. Adjacent ribbon segments previously chose separate edges, leaving triangular gaps at bends. All streamed and anchored Lux/Umbra flows now share their edge positions and UVs at each station. Short formation curves scale their bend to their length. Tight bends narrow locally, and concave quads use the diagonal inside their projected contour; this also removes the bright triangular seams in short charging streams. The vertex budget remains unchanged for these ribbons.

White Verdict previously relied on a flat strip starting at eye position. A camera on that axis saw an edge or a horizontal line instead of the released beam. The beam now includes offset translucent light sheets and soft cross-sections, with a stronger charge preview. The flat side sheath fades out when viewed along the axis, preventing the line across the screen. Its server-authored endpoints, radius, lifetime and gameplay remain unchanged; the added geometry stays within the same clipped damage cylinder.

The focused suite now passes 36 tests, including shared joins, projected triangle orientation through animated charge bends, and visible beam area looking directly along horizontal and vertical cast axes. `assemble` also passes. No common gameplay or packet code changes were needed for this correction.

The additional `LuxUmbraRibbonReview` fixture captures real `WhiteVerdictManip.getAction` releases after a 60-tick charge preview. Its eight scenes cover first person, third person, a side view, vertical firing, daylight with Minimal particles, close Beacon/Mending joins, and both anchored flow materials. The fixture uses the same disposable save. A before/after recording is at `build/lux-umbra-review/ribbons-and-verdict-before-after.webm`; it contains 352 frames per view at ten frames per second. These captures supersede the earlier side-on study for checking the first-person beam and close ribbon joins.

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PluxUmbraRibbonReview runClient
```

![Continuous charge wisps in first person](evidence/lux-umbra/verdict-charge-joined.png)

![White Verdict after release in first person](evidence/lux-umbra/verdict-fired-first-person.png)

## Umbral Step activation placement

Umbral Step already emits body-centered smoke at departure and arrival. The shared post-cast activation previously added a third puff 0.85 blocks ahead of the new eye position, directly in the player's view. Umbral Step now opts out of that default activation accent through `usesDefaultActivationParticles()`. Its existing teleport forms and body accents remain intact; other spells retain their default activation effects.

The ribbon review also includes first- and third-person Umbral Step scenes using `tryPerformAction`, including the shared post-cast path. Each scene checks that the cast succeeds, moves the player and spends blood. Run only these scenes with:

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PluxUmbraRibbonReview -PluxUmbraStepReview -PluxUmbraReviewLabel=step-after runClient
```

The correction passes 37 focused JUnit tests, compilation and `assemble`. Both live scenes complete through the shared cast path, with 88 frames captured per run. The first-person comparison shows the forward puff removed, and the third-person capture retains the body smoke. The recording is `build/lux-umbra-review/umbral-step-before-after.webm` (before on the left, after on the right).

## White Verdict afterglow

White Verdict's released beam lasts 40 ticks (two seconds), up from 16. It reaches full brightness in one tick, holds through tick 16, then fades smoothly over 24 ticks. The bright shader core is about 25% thicker; the axial light sheets and end glows are 20% broader. All geometry remains inside the original damage cylinder and clipped endpoint planes. The afterglow stays at the firing position when the player moves or turns, and adds no damage ticks or steering after release.

The appended `WHITE_VERDICT` form separates this presentation from Prismatic Reproof's existing `VERDICT` pulse. Existing form ordinals, Prismatic Reproof's appearance and lifetime, and White Verdict's charge preview and gameplay remain unchanged.

The dedicated trail review extends each scene beyond beam expiry and includes first person with a turn after firing, third person, a side view, a vertical cast, and daylight with Minimal particles and resource reload:

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PluxUmbraRibbonReview -PluxUmbraVerdictTrailReview -PluxUmbraReviewLabel=trail runClient
```

All 42 focused JVM tests and `assemble` pass, including the two-second fade envelope and horizontal/vertical beam bounds. The client completed all five scenes and resource reload with 300 captured frames. Inspected captures show the beam still bright at one second, fading at 1.5 seconds and absent after expiry. The recording is `build/lux-umbra-review/white-verdict-afterglow.webm`; the build/client log is `build/white-verdict-trail.log`.

## Captured examples

These stills show the final client renderer. Movement, charge and retirement are in the recording described above.

![Hematic Beacon in daylight](evidence/lux-umbra/beacon-day.png)

![Unclosing Eye at night](evidence/lux-umbra/eye-night.png)

![Eclipse Well with Minimal particles](evidence/lux-umbra/well-minimal.png)

![Teleport arrival forming from smoke](evidence/lux-umbra/arrival-day.png)
