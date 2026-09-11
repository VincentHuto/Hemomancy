# Flammeus and Congeatio materials

Fire manipulations use animated dark crimson flame with curved, tapered roots and smoky near-black tips, rising dark smoke and shallow boiling blood. Vitric Combustion gathers and ejects thin, uneven sanguine glass plates. The updraft variants retain their different lift and blast shapes. Furnace Veins maintains a surface-following molten field; Sanguine Ignition produces a short outward wash. Forge, cauterization, conjuration and Phoenix readiness/activation use the same materials at the scale of their actions.

Cold manipulations use opaque wine-red cruor cores beneath pale frost and translucent fractured edges. Powder and heavy fragments accompany crystallization. Glacial Grasp follows affected water and targets; rampart and bastion formation retain their block collision. Rimebound Sentence, Osseous Bloom and cold impacts add model-following frost and vein patterns. Absolute Stillness holds the material almost motionless. Endless Hour keeps its duration/deferred-damage display while suspending frozen blood, then breaks it on settlement.

Glacial Circulation and standalone Glacial Bastion remain retired. The active Glacial Rampart still provides its crouched bastion mode. Weapon models, IDs, costs, targeting, charge scaling, damage and progression are unchanged by this presentation work.

## Rendering and synchronization

- Four world shader programs cover flame/smoke, molten blood, glass and cruor/powder. A fifth program draws freezing and cauterized seams on posed living models.
- Core geometry writes depth. Translucent materials share depth ordering with Lux and Umbra, without writing depth over later effects.
- Surface samples and particle/fragment translations update on game ticks. Render frames interpolate motion. Existing accent budgets and particle settings limit secondary effects; spell bodies and boundaries remain visible on Minimal.
- New visual forms are appended to the existing packet enum. Ignition, cold pulse, block formation/breakup, frost attachment and Hour settlement have separate cues. Packet fields and existing enum ordinals are retained.
- Cosmetic status state is transient, sends active frost to new entity observers, and explicitly retires on thaw or expiry. Temporary cruor surfaces refresh near observers and stop rendering when the block is replaced.

## Review commands

Run from the repository root:

```powershell
python tools/validate_thermal_shaders.py
.\gradlew.bat test --tests '*Thermal*Test' --tests '*LuxUmbra*Test' --tests '*ManipulationLifecycleTest' --tests '*ManipulationParticleBudgetTest' --max-workers=1 --no-daemon
.\gradlew.bat runClient -I tools/manipulation-visceral-review.init.gradle -PthermalReview -PthermalReviewModes=5 --max-workers=1 --no-daemon
.\gradlew.bat runClient -I tools/manipulation-visceral-review.init.gradle -PthermalReview -PthermalCastReview --max-workers=1 --no-daemon
.\gradlew.bat runClient -I tools/manipulation-visceral-review.init.gradle -PthermalReview -PthermalBenchmark -PthermalReviewModes=1 --max-workers=1 --no-daemon
```

The client harness requires the disposable `VisceralReview` save in `build/visceral-review-client`. It changes only that review world and its settings. Material scenes cover daylight, night, first/third person, particle settings, overlap and resource reload. The cast review uses paid actions, channels, their cancellation, and supported NPC actions; it also executes the packet-observer regression. The benchmark pass does not capture screenshots. It disables VSync and uses the 260/unlimited FPS setting at 1280 x 800. Use `-PthermalReviewStartMode=3 -PthermalReviewModes=4` to repeat the distant view alone. Add `-PthermalNpcReview` to the cast command for the eight supported NPC actions.

For two-client review, start the cast command with `-PthermalMultiplayerReview`, then run `./tools/review-thermal-observer.ps1 -Label casts` in a second PowerShell session. The script prepares the observer arguments and starts Java directly, keeping dependency JARs intact while the first client is running. The first client waits for the observer before casting. These development clients use offline profiles and the disposable world; port 25579 closes when the review ends.

## Validation, 2026-09-09

- 41 focused JVM tests passed: thermal material resources, geometry and motion, packet round trips, Lux/Umbral rendering contracts, lifecycle, particle limits and cross-material depth ordering.
- All five programs (10 stages) compiled and linked with `glslangValidator`, using imports from the local Minecraft resource archive. Thermal programs loaded and reloaded in the client without shader warnings.
- The complete JVM run executed 1,835 tests. It contained 57 failures outside this overhaul, plus the thermal lifecycle assertion subsequently corrected and passed in the focused run. They include stale source paths and existing layout/source assertions. The captured failure list is `build/thermal-full-suite-failures.json` (its initial 58 entries include the corrected thermal assertion).
- All thermal GameTests and relevant manipulation tests passed. The complete server run passed 266 of 267 tests. `formationacceptsautopickedupoutput` failed because its unrelated journey fixture encountered an occupied stone block.
- All 21 player cast scenes completed, including four updraft forms, both Rampart modes, successful weapon selection, interrupted channels and Phoenix's actual lethal-damage trigger. Packet checks covered distinct NPC/player cues, late observers, thaw removal and discarded targets.
- A second client connected and recorded 629 frames of those casts. An initial disconnect hit a HutosLib class-loading failure after a concurrent Gradle run rebuilt the dependency JAR. With dependency rebuilds excluded from the live session, the observer rerun captured 240 frames and exited normally (exit 0). The rerun showed seven NPC casts; its first Ignition fixture cast was rejected. All eight NPC actions passed in the separate single-client run. No production gameplay was changed for that fixture limitation.

Material captures include formation, action and dissipation in daylight/night, first/third person, all particle settings, walls, uneven terrain, close/distant views, resource reload and Lux overlap. The fixed-camera comparison uses the original 572 baseline frames and their matching updated scenes. Player casts and the second-client captures provide separate action/attachment evidence. The camera/platform defect in the initial distant pass was corrected before the final distant capture.

The eight supported NPC actions completed in a separate live run with visible caster entities (432 frames). The observer/cleanup checks also passed again after guarding Hour settlement against inactive debt. A previous NPC fixture attempt raced the existing live known-manipulation map during packet encoding; the NPC fixture now leaves player progression untouched.

The packaged build passed with `build -x test`; the complete `build` remains red because of the unrelated full-suite failures described above. The packaged JAR is `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`.

| Screenshot-free frame intervals | Samples | Mean | 95th percentile |
|---|---:|---:|---:|
| Active material effects | 49,336 | 0.56 ms | 0.90 ms |
| Idle scene | 17,795 | 0.51 ms | 0.87 ms |

Measured on an RTX 4090 / i9-13900K, 1280 x 800, VSync off, unlimited FPS, one client. These are whole-frame intervals, not isolated GPU shader timings. Screenshot capture and the two-client review were separate runs.

[Matched daylight before/after motion](../build/thermal-review/flammeus-congeatio-before-after.mp4) covers 13 canonical forms over 57.2 seconds. Additional updated captures cover the full cast roster and the other viewing conditions; there is no matching night/terrain baseline. Logs are `build/thermal-focused-final.log`, `build/thermal-shaders-final.log`, `build/thermal-gametests.log`, `build/thermal-casts-final.log`, `build/thermal-observer-final.log`, `build/thermal-observer-clean.log`, `build/thermal-multiplayer-final.log`, `build/thermal-npc-final.log`, `build/thermal-benchmark-final.log` and `build/thermal-package-final.log`.
Performance observations apply to this disposable scene on this machine; no baseline renderer timing was captured. Shader and unit-test results alone do not establish visual acceptance.

