# Phlegethontic Nether validation

Implementation and measurements: 2026-09-12, current checkout, Minecraft 1.21.1 / NeoForge 21.1.219 / TerraBlender 4.1.0.8 / Java 21. All worlds used for this work are disposable copies under `build`. Normal saves were not opened or regenerated.

## Fingered shores, material dithering, and surface relief

The 2026-09-13 terrain pass replaces the reservoirs' smooth elliptical margins with connected five-to-eight-lobed shore profiles. Measured radial crossings on a 32-block reference basin range from at least six blocks inside to at least six blocks outside the base bowl, with a minimum twelve-block total span and exactly one boundary crossing per sampled ray. The vault roof remains driven by the smooth bowl so the stronger shoreline silhouette does not produce a matching spiked ceiling.

All generated solid faces directly touching Basin ichor are now blood-scorched scab. The exposed shore then dithers from scab into blackstone over distances 2-7, remains blackstone through 7-10, and dithers from blackstone into venous stone through 10-16. The biome's untouched floor surface is venous stone, allowing the procedural outer band to finish without reverting to a biome-wide scab cap. Banks use broad deterministic zero-to-two-block relief with no greater than a one-block adjacent step in the focused sample. Submerged basin floors vary by minus one, zero, or plus one block, while channels and ichor surface elevations are unchanged.

Focused JVM cases cover the connected finger/cove silhouette, all three bank heights, bounded floor relief, every solid ichor-contact face, both dither transition bands, the outer venous surface rule, and relief-aware sentinel footing. The `*Phlegethontic*` suite and `compileGameTestJava` pass.

Fresh forward/reverse worlds for seeds 20260912 and 8675309 each generated 576 chunks and produced byte-for-byte equivalent procedural river and vein block states in the comparison tool. All four runs reported zero exposed buried cores, zero fluid escapes after 120 ticks, zero out-of-chunk writes, and zero duplicate sentinel homes. Seed 20260912 retained 4,030 buried and 20,620 Basin ichor blocks; seed 8675309 retained 1,818 buried and 8,243 Basin ichor blocks. Final feature median/p95 measurements were 0.255/14.981 ms and 0.252/14.978 ms for seed 20260912, and 0.118/15.232 ms and 0.142/15.346 ms for seed 8675309. Each final generated-area check retained one unique persistent sentinel.

## 16-pixel ichor texture refinement

The requested fluid-art refinement replaces both sprites with exact 16×16 frames: still is 16×64 (four keyframes, a 72-tick forward/backward loop), flow is 16×256 (sixteen frames, a 64-tick downward loop), and the matching overlay is 16×16 with alpha 105. Built-in ImageGen source images and exact prompts are preserved in [materials/ichor-16px](materials/ichor-16px/). The focused PowerShell exporter reduces and packs those images; the general resource packer now copies these masters instead of recreating the old sprites.

`build/phlegethontic-16px-build.log` records a successful packaged build and all 1,951 JVM tests passing. Direct asset checks verify dimensions, alpha, palette counts, animation metadata, downward frame continuity, and byte-identical copies inside the release JAR. Results are in `materials/ichor-16px/validation.json`. Dedicated GameTests were not rerun for this resource-only refinement; the preceding 399-case result below covers the unchanged gameplay code.

A native client loaded the updated resources in the disposable `Phlegethontic Vault Review` world. `materials/ichor-16px/client-spillway.png` shows the close-up surface and falling faces in normal Nether light. Native captures establish resource loading and appearance; still screenshots do not establish subjective animation smoothness. No new fluid sprite-loading errors appeared. The existing unrelated infected-cap/Engram/Abocipher/grimoire resource warnings remain.

Texture-refinement packaged JAR SHA-256: `ef3cb1a1e94ec4cbe8ce656c8fc9638ceebdca003b815d53b1ddd4ed630e681d`.

## Cavern clearance correction

The screenshot-reported rectangular excavations came from a constant 24-block clearance height, hard changes between overlapping basin surfaces, and abrupt clipping at protected or biome boundaries. Clearance now uses the union of rounded basin/channel vaults, with broad and fine seeded relief, independently of the fluid column selected below it. Upper cuts recede before obstructions. The lined trough, fluid elevations, arch supports, protected blocks, and chunk ownership retain their existing rules. Only newly generated chunks use the correction.

Three new regression tests first reproduced the flat roof, vertical border cut, and overlap height step, then passed with the correction. The full JVM suite now passes 1,951 cases, and the dedicated server passes all 399 required GameTests. `alphaCheck compileGameTestJava build` passed in `build/phlegethontic-vault-release-check.log`.

A fresh seed 20260912 run retained 19,705 Basin fluid blocks, 4,030 buried fluid blocks, one distinct sentinel, and zero exposed buried cores, fluid escapes, or out-of-chunk feature writes after 120 fluid ticks. Saved-world roof measurements changed from 1,833 columns at a uniform 25-block solid-roof offset to 19. The histogram only counts roofs 8-36 blocks above surface ichor and includes existing cavern geometry; it supports the flat-clearance diagnosis rather than measuring every Nether roof. See `validation/vault-roof-comparison.json`.

Native-client review used a disposable copy of seed 20260912. `validation/vault-matched-camera.png` uses the same camera as the earlier `final-basin-course.png`; `vault-normal-light.png` and `vault-night-vision.png` look upward at the revised clearance from (188.5, 61, 427.5), yaw 0, pitch -20. The captures show curved shoulders and stepped, irregular overhangs. Night Vision is for geometry inspection, not color calibration. The review client was closed cleanly after saving. Existing user worlds were not modified.

Cavern-correction packaged JAR SHA-256, before the texture refinement above: `fb6ea93465e23528137401796f30392cc9e0042d26cd269b03eed5c13981bae7`.

Final-code validation also passed seed 8675309 and seed 20260912 in reverse generation order. Both reported zero exposed cores, fluid escapes, out-of-chunk writes, and duplicate homes. Their feature median/p95 timings were 0.133/13.636 ms and 0.284/14.904 ms respectively; these retain the timing limitations described below. The earlier exact-cache run was 0.276/16.678 ms before reusing the same block-position cache for the main column checks.

Forward/reverse seed 20260912 has identical river, vein, fluid-level, and scab block states. A separate comparison of air versus solid cells at Y=21-105 found six differences clustered around one Nether gold ore placement. These are preserved in `validation/vault-air-order.json`; full native-terrain equality is not claimed. Pure vault geometry and chunk ownership tests pass independently of that decorator variation.

Biome clearance is cached at exact block positions. An exploratory quart-cell cache was rejected after the real-world fluid test detected seven escapes: Minecraft's biome manager uses perturbed boundaries within those cells. That cache is absent from the final code and its failed run is excluded from accepted measurements.

## Initial implementation evidence

The initial full alpha gate and packaged build passed with 1,948 JVM test cases and 399 required dedicated-server GameTests. Thirteen runtime cases are in `PhlegethonticGameTests`, explicitly registered through `DevTestHooks`. Seventeen focused JVM cases cover geometry, containment, pulse rules, resource wiring, and the actual climate mappings. Runtime cases decode the configured features, placed features, and biome with live registries.

Commands and final logs:

```powershell
./gradlew.bat test --tests '*Phlegethontic*' compileGameTestJava runGameTestServer
./gradlew.bat alphaCheck compileGameTestJava build
```

The initial combined log is `build/phlegethontic-release-check.log`; `build/phlegethontic-alpha-build.log` records the preceding successful 399-case alpha/build run. The initial packaged output was `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`; the cavern correction above rebuilds that path. The release archive was checked for biome/fluid/guardian resources and excludes all GameTest classes, review drivers, and the compatibility fixture biome. SHA-256: `2255223687f08b7b72c84fbf90b4c038dc619d54504ec5325fa0d334b836fc30`.

The runtime suite includes a deliberately uncontained fluid source as a positive control. Protected vein sections are then ticked beside caves, ore, a chest, and HutosLib's foreign-namespace display glass. The suite verifies continuing Blood Loss under Fire Resistance, contact deduplication, guardian immunity, bucket rejection, full collision/footprint spawning, home return, clotting/recovery, piercing collision, one tether per victim, independent severing by two server players, sounding delay, bounded movement against a wall, expiry, owner removal, and reload cleanup. Ore-candidate masks survive an NBT round trip, including negative chunk coordinates.

Adding the new cases exposed three existing combat tests whose targets stood outside their one-block templates. Those three tests now use a fixture that contains their target footprint. Their assertions and production manipulation code were preserved; the complete suite passes. New test entities are explicitly removed after their batch.

## Initial five-seed measurements

Each final run uses a fresh `SEED-network-final` directory, generates a 24x24 area (576 chunks), examines the inner 10x10 (100 chunks) for veins and containment, then advances 120 fluid ticks. Layout, raised-course, and arch materialization counts inspect the full 576-chunk area. The launcher rejects a run name that already contains Nether region files. Earlier exploratory results have been replaced by these final measurements.

| Seed | Basin locate distance | Successful origins / 100 chunks | Buried ichor blocks | Basin ichor blocks | Sentinels / unique homes | Feature median / p95 ms |
|---|---:|---:|---:|---:|---:|---:|
| 17 | 321.6 | 7 | 2,360 | 351 | 0 / 0 | 0.046 / 7.040 |
| 42 | 659.7 | 4 | 1,115 | 1,230 | 0 / 0 | 0.032 / 6.755 |
| 8675309 | 273.4 | 6 | 1,824 | 8,076 | 2 / 2 | 0.134 / 11.551 |
| -314159 | 747.0 | 6 | 2,260 | 13,924 | 1 / 1 | 0.048 / 9.542 |
| 20260912 | 500.9 | 8 | 4,030 | 19,705 | 1 / 1 | 0.247 / 12.043 |

Across all five samples: **zero exposed buried cores, zero fluid escapes, zero out-of-chunk feature writes, and zero duplicate sentinel homes**. Data is preserved in [validation](validation/). Small discovered biome patches can have no valid sentinel shore; the feature does not force a spawn into obstructed terrain.

The active macro-cell samples contain 64 planned basins and 48 planned channels. All 22 active layouts select raised connections in pure geometry. After biome and obstacle clipping, **9 of 16 materialized layouts contain raised fluid courses (56.25%)**, below the 60% tuning target. Those courses occupy 5,985 columns, with 2,351 columns containing air openings below the supported trough. Edge layouts may be partial. Planned connectivity is tested, but these counts do not establish that every connection survives clipping or that every opening is visible from an open cavern.

| Seed | Materialized layouts | With raised rivers | Raised river columns | Columns with arch openings |
|---|---:|---:|---:|---:|
| 17 | 1 | 1 | 117 | 47 |
| 42 | 2 | 2 | 297 | 128 |
| 8675309 | 6 | 2 | 2,091 | 773 |
| -314159 | 2 | 1 | 172 | 35 |
| 20260912 | 5 | 3 | 3,308 | 1,368 |

Feature timings aggregate Basin generation, the underground-decoration queue, and deferred vein completion per generated chunk. They meet the draft's 5/15 ms median/p95 target in these runs. They exclude ore-reservation interception overhead and are not a controlled whole-generator overhead benchmark. Whole chunk request medians were 10.6-12.8 ms and p95 values 60.5-76.5 ms, including vanilla and other installed generation.

The measured origin frequency is **6.2 successful origins per 100 chunks**, above the 3-5 tuning target. The settled origin activation probability remains exactly 1/18. The frequency and materialized raised-course targets are not passed. Further tuning should use a larger geographically independent sample, retain protected boundaries, and preserve the required origin probability.

## Chunk order and compatibility

Seed 8675309 was generated in forward and reverse chunk order in separate fresh `network-final` and `network-reverse` worlds. Comparing saved river and vein block states, including fluid levels, found **zero differences**. Both runs contained 1,824 buried core blocks, 8,076 Basin fluid blocks, and two distinct sentinels. Eleven high cavern-surface differences remain where vanilla lava/magma decoration changes scab from biome surface rules; they are above the procedural reservoir/bank volume. The comparison tool reports them separately instead of hiding them in a single passing world hash.

All five runs include a second registered TerraBlender Nether region, `phlegethontic_validation:companion`, with the same five explicit climate slots, and a foreign-namespace Nether biome. Runtime registry assertions verify that the Nether tag modifier injects veins into that biome. A foreign HutosLib block is preserved in the ticking containment fixture. This is an executable compatibility fixture, not certification of every third-party Nether biome or structure mod.

## Live review

Native Minecraft captures from disposable review worlds are in [validation](validation/). The `phlegethontic-105/109/111.png` images show an earlier geometry pass and are retained as material/entity review evidence, not final terrain acceptance. `final-aqueduct-underside.png` and `final-basin-course.png` show the final seed 20260912 terrain. The underside capture confirms venous bands and supported openings, but also shows how surrounding stone can obscure an arch. Some captures use Night Vision to expose geometry; they are not color-calibration references.

Two actual client processes, IchorReview and IchorObserver, connected through the local network stack to a disposable integrated server. Both players were separately tethered and pulled. The observer's real Sneak input reached the server and released its tether. Host dimension change cleared its link. Disconnecting the observer immediately after recording an active tether left no Recall Barb entities while the owner remained loaded. The server snapshots and relevant session log are preserved as `live-*.json` and `live-session-log.txt`. Neither player's blood magic became active. Exact twenty-tick escape timing and payment are established by the runtime tests; the manual session does not independently measure that timing or payment.

`phlegethontic-tether-205.png` and `phlegethontic-tether-210.png` show the two-player tether. `observer-bow-draw.png` records the earlier draw pose; `final-sagittary-draw.png` shows the final arm/string positioning from a stepped native-client draw pose. The native model uses interpolated draw/release and rear/breath transitions, with a two-segment right arm solved against the moving string. Still images do not establish animation smoothness or swimming feel. A broader artistic pass across all five worlds, a controlled total-generation-overhead benchmark, and third-party modpack testing remain separate acceptance work.

Unrelated client resource warnings are present for existing Engram/Abocipher blockstate variants, an old infected-cap texture, and the Harbinger grimoire models. They do not identify the new biome, fluid, guardian, or projectile resources. The dedicated alpha resource-error gate passes.

## Corrections found by this work

- Nether geometry now uses the chunk generator's sea level (32), rather than the level accessor's 63.
- Structure references beyond a generation region's readable cache are conservatively protected instead of requesting unavailable chunks.
- Explicit source-fluid ticks exercise real containment after protochunk placement.
- Banks retain the highest adjacent reservoir while preserving the lower trough, closing the observed cross-elevation leak.
- Deferred vein completion and persisted ore-candidate reservations remove ore/decorating-order seams without moving block writes into neighboring chunks.
- Ordinary bucket pickup returns an empty result without deleting the source block.
- The first twenty tether ticks measure without pulling; piercing volleys can hit two victims and still stop at blocks.
- Tether rendering uses the animated bow's interpolated transform; barb launch originates near the drawn bow instead of the guardian's center.

Final geometry tuning spaces basin centers further apart, gives primary reservoir connections raised supports, preserves channel cores through overlapping bowls, and uses an approximately eleven-block arch period. Retaining walls on closed lower courses extend to the highest adjacent bank, keeping mixed-elevation boundaries sealed.
