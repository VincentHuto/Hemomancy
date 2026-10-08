# Pelagic terrain implementation

The approved expansion combines rocky shore and tidepools, retains Erythrocoral Reef on shallow shelves, adds open Pelagic Ocean, and places Twilight, Midnight, Carrion and Hydrothermal biomes down the ocean water column. Terrain quality and recognizably different landforms are acceptance requirements.

## Generation contract

- Sea level remains 63; the deepest ordinary troughs remain around -54 +/- 1, with a solid foundation above untouched bedrock.
- Broad ocean bathymetry applies across the vanilla Overworld noise presets. The special coast, reef and open-ocean surface habitats use TerraBlender regions.
- Shore: jointed exposed benches, irregular enclosed pocket pools and coastal gullies. Reef: stable rounded lobes with sediment channels. Open ocean: broad banks and sparse elongated seamounts.
- Twilight (33..13): scalloped shoulders and curved gullies. Midnight (12..-8): incised branching canyons and wet alcoves. Carrion (-9..-30): low-relief sediment fans and sparse fossil beds. Hydrothermal (-31..-53): gently rolling plains with localized fault-aligned mineral mounds, aprons and chimneys.
- Deep biomes occupy the water and a thin seabed skin; underlying cave biomes remain separate. Minecraft's quart sampling blends the nominal elevation boundaries.
- Terrain sampling is coordinate-only and deterministic. Cross-chunk decoration writes only its owning chunk.
- Geology uses the existing terrain materials. The subsequent [ecology pass](PELAGIC_ECOLOGY.md) adds six mobile species and four habitat growths without changing these landforms. Morphling systems, currents, pressure and temperature remain outside this work.
- Terrain and generated growths affect new chunks only. Natural fauna can populate existing suitable chunks, and players can place collected growths. Existing world borders may show a change of elevation. No retrogen, custom world-height change, or replacement generator codec.

## Implementation

Hydrothermal plains retain broad low stretches, with gentle rolls across several elevations instead of a single level sheet. Fault-aligned vent fields share their seeded centers with irregular elongated mounds, roughly 58 by 40 blocks wide and 5–8 blocks high before blending into shallower bathymetry. Each seeded field has 1–5 chimneys with varied heights, directions and spacing across the mound. The older snail-bearing vent feature also chooses 1–5 chimneys and builds a 1–3-block-thick mineral bed beneath them. These changes apply only to new chunks.

`PelagicTerrainSampler` supplies seeded, continuous bathymetry and landforms. TerraBlender selects the shore, reef and open-ocean surface habitats. Runtime density and aquifer wrappers shape and flood the ocean; the biome-source wrapper supplies the depth biomes. Surface rules and chunk-owned landmark plans provide the sediment, exposed rock, fossil ribs and mineral chimneys.

Reef shelves and Rockpool Shore blend into neighboring ocean and vanilla coastal terrain using smoothed biome coverage on a 16-block sampling grid. Both floor height and terrain-shaping strength fade across the boundary. Narrow shore cores retain full shaping for enclosed pools and grounded rock stacks, and reef interiors retain their flat coral-growing lobes. The shore profile also fades continuously into the ocean profile at its climate limit. The blend reads the seeded biome source, never neighboring generated chunks; foreign mod terrain retains its own handling. This applies to newly generated terrain and does not reshape saved chunks.

The wrappers preserve the saved generator codec. They install only in the vanilla Overworld with Normal, Large Biomes or Amplified noise settings, sea level 63 and minimum Y -64. Other dimensions, custom settings and foreign surface-biome geology retain their own generation. Underlying caves remain below a sealed seabed skin. Kelp, seagrass and sea pickles check their actual placement target and stop below Y13 in fully shaped ocean columns.

`worldgen.enablePelagicWorldgen` defaults to `true`; disabling it restores the previous Erythrocoral Reef region without the new terrain hooks or depth biomes. `worldgen.pelagicRegionWeight` defaults to `2`. Both settings require restart.

Biome searches targeting Twilight, Midnight, Carrion or Hydrothermal use a vertical stride of at most 16 blocks while the Pelagic hooks are active. Vanilla `/locate biome` uses 64-block height intervals, which can skip these roughly 20-block layers entirely (including Hydrothermal when searching from Y-27). `MixinPelagicBiomeSearch` preserves the requested horizontal spacing and radius, already-finer searches, and searches that exclude the four depth biomes. Disabled expansion and unsupported worlds keep vanilla search behavior. This changes lookup only; it does not alter terrain or regenerate saved chunks.

Monuments sample their footprint, reject steep ground and move their complete room graph to the deeper floor. Reconstruction preserves the saved elevation. Existing shipwreck, ruin and Voyager placement continues using the ocean-floor heightmap.

The existing snail-bearing `deep_ocean_vent` feature now belongs exclusively to Hydrothermal Depths. Its biome modifier still schedules one copy at the surface-structures step, with the existing ocean-floor heightmap, one-in-96 rarity and biome filter. Vent geometry and one-time generated snail clusters are unchanged. Previously generated reef/deep-ocean vents remain in saved chunks.

## Acceptance

Use seeds 42, 1337 and 8675309, forward/reverse generation comparisons, negative coordinates, inland and other-dimension controls, and structure reload checks. Review three natural examples per biome with overhead views, profiles and client views. A passing JVM suite does not establish runtime or visual acceptance.

Terrain-foundation checks recorded on 2026-10-05, before the ecology pass:

| Check | Result |
|---|---|
| Focused JVM morphology, density and landmark tests | 13 passed |
| Normal preset, seeds 42 / 1337 / 8675309, each forward and reverse | Six fresh-world runs passed; all 24 sampled terrain and biome hashes match within each seed |
| Large Biomes and Amplified, seed 42 | Both fresh-world runs passed |
| Disabled expansion, seed 42 | Passed; no Pelagic context or new biome candidates installed |
| Loaded Pelagic registration/dimension and four-orientation monument tests | Both passed in the combined suite |
| Native client review, seed 42 | Inspected 24 views covering three sites per habitat and all three landmark types; two obstructed views recaptured from wider angles |
| Packaged asset paths, PNG headers and font license | Passed; JAR assembled |
| Full JVM suite | 2,436 run; three unrelated failures described below |
| Full dedicated GameTest suite | 728 run; five failures outside Pelagic described below |

Each enabled fresh-world run measures three natural sites per habitat plus a fossil, shore stack and vent site. It checks actual landmark blocks, flooded columns, exposed bedrock/lava, deep vegetation, vertical biome selection and 20 unchanged inland columns. Monument acceptance places a generated candidate on natural terrain and checks actual base blocks plus serialized/reconstructed pieces. Natural structure spacing and a cold monument reload remain separate acceptance cases.

The order comparison covers base terrain and biome assignment. Finished decoration differs in 18 / 14 / 17 of the sampled patches for seeds 42 / 1337 / 8675309 respectively, including existing reef decoration and vanilla ore/sediment features. It does not establish order independence for every finished block.

The broad release gates remain red: two JVM harness checks cannot decode the existing `docs/TESTING.md` (the same invalid UTF-8 byte exists in `HEAD`); the Morphling rename scan finds an old name in an ignored `docs/OrchestratorReports` discussion archive. The combined GameTests report a Liber entry-count mismatch (81 expected, 82 found), two centrifuge material-accounting failures, and two whale-feeding failures. That suite uses a flat world, where Pelagic terrain hooks are inactive. Those files and tests were left unchanged during the terrain pass; the subsequent ecology pass investigates the whale-feeding failures. `alphaCheck` and `build` were attempted with `--continue`; compilation, assembly and packaged-resource checks completed, but the overall gates did not pass.

The completed ecology regression on 2026-10-05 matched all 72 sampled geometry and biome hashes against both the terrain-foundation reports and fresh reverse-order runs. Finished-decoration differences were 18 / 15 / 17 patches, so the same qualification about individual finished blocks applies. The two whale failures are now resolved; the final combined suite passed 740 of 743 tests, with only the existing book and centrifuge failures above. The full JVM suite passed 2,443 of 2,446 tests, retaining the same three unrelated failures. Natural-population, reload, collection and shader-free client evidence is recorded in [Pelagic ecology acceptance](PELAGIC_ECOLOGY.md#authoring-and-verification).

Biome-locate follow-up on 2026-10-05 reproduced a failed Hydrothermal search from Y-27 with that biome confirmed in the generated column at Y-44. With the search correction, fresh seeds 42, 1337 and 8675309 each passed 16 actual `/locate biome` commands: all four depth biomes from Y-27, 63, 120 and -60. The same runs verify zero-radius horizontal bounds, unchanged four-block searches and unchanged vanilla-only queries. All 72 sampled noise-column and biome hashes match the preceding ecology reports. Finished decoration retains the variability described above. Evidence is in `build/pelagic-ecology/locate-red.log`, `locate-green-<seed>.log`, `locate-regression.json`, and the `biomeLocate` arrays in `build/pelagic-worlds/<seed>-locate-green/pelagic-report.json`.

The locate follow-up also ran `./gradlew.bat test runGameTestServer alphaCheck build --continue --no-parallel --console=plain`: 2,443 of 2,446 JVM tests and 741 of 744 loaded tests passed. The same documentation/archive, book and two centrifuge failures remain; all Pelagic ecology and whale tests passed. Compilation, JAR assembly and packaged resource checks passed, and the new search mixin and its registration were verified inside the JAR. Overall release gates remain red. The log is `build/pelagic-ecology/locate-final-gates.log`.

Run a fresh noise-world check with `./gradlew.bat runPelagicValidationServer -PpelagicSeed=42 -PpelagicRun=forward -PpelagicOrder=forward`. Use a new run name each time; the launcher refuses an existing region directory. Repeat with `-PpelagicOrder=reverse`. `-PpelagicPreset=large_biomes` and `amplified` select the other supported presets. Reports are under `build/pelagic-worlds/<seed>-<run>/pelagic-report.json`.

`python gradle/compare-pelagic-reports.py <forward-report> <reverse-report>` checks sampled noise-column and biome hashes; it separately reports finished-block differences from decoration. `python gradle/render-pelagic-report.py <report> <output.png>` renders measured terrain profiles (requires NumPy and Matplotlib). Verified normal reports use run names `verified-forward` and `verified-reverse`; seed-42 preset/control runs use `verified-large`, `verified-amplified` and `verified-disabled`.

The disposable `runPelagicReviewClient` consumes a copied test world named `Pelagic Review` and its report under `build/pelagic-client`. `-PpelagicReviewWorld="Pelagic Final Review"` selects the reviewed copy. Optional per-site `camera` coordinates/angles support wider inspection views; `captureIndex` retains the original frame number when reviewing a subset. Captures use spectator travel, night vision and conduit vision for terrain inspection; they do not measure unaided underwater visibility.

Local review artifacts are `build/pelagic-client/terrain-review.html`, `build/pelagic-client/screenshots/pelagic-reviewed-*.png`, and each seed's `terrain-profiles.png`. Gate diagnostics are retained in `build/pelagic-final-gates-continue.log`; the successful final client runs are `build/pelagic-final-client.log` and `build/pelagic-detail-client.log`.

### Reef-edge follow-up, 2026-10-05

The reef's categorical terrain branch previously switched directly between shelf and open-ocean heights at the surface biome boundary. `PelagicReefBlend` now interpolates weighted reef coverage from the original seeded biome source, and `PelagicContext` blends the two floor profiles on both sides of the boundary. Raw biome lookups are cached per generation thread with a fixed size limit. Biome identities, coral feature placement and interior shelf geometry remain intact.

All 24 focused Pelagic JVM tests passed. Fresh seeds 42, 1337 and 8675309 passed the existing terrain, landmark, colony and biome-location checks plus 336, 201 and 295 sampled natural reef crossings respectively. Maximum adjacent predicted floor steps were 3.245, 2.071 and 1.885 blocks. The latter two seeds had unblended maxima of 19.852 and 14.132 blocks, demonstrating the removed boundary cliffs. These measurements concern the terrain profile, not every decorative coral block.

The broad run passed 2,445 of 2,448 JVM tests and 742 of 746 loaded tests. The existing documentation/archive, book-count, two centrifuge and vulture-despawn failures remain; compilation, JAR assembly and packaged resource checks passed. Logs are `build/pelagic-ecology/reef-edge-focused.log`, `reef-edge-world-<seed>.log` and `reef-edge-gates.log`. Fresh reports use the `<seed>-reef-edge` run names. Existing saved terrain is not rebuilt, so this does not repair old cliffs or guarantee seamless borders between old and newly generated chunks.

A reverse-order seed-42 run (`42-reef-edge-reverse`) passed the same acceptance checks and matched all 24 sampled geology/biome hashes. Seventeen finished patches differed after existing feature decoration; the terrain-order guarantee does not extend to every decorated block.

Two native-client views of seed 1337's former steepest sampled crossing at X-225/Z-1040 show the coral shelf stepping down into the adjacent seabed. Captures `pelagic-reviewed-40-erythrocoral_reef.png` and `pelagic-reviewed-41-erythrocoral_reef.png` are under `build/pelagic-client/screenshots`; the successful client log is `build/pelagic-ecology/reef-edge-client.log`. These terrain views use night vision and conduit vision without shaders. The disposable world is `Pelagic Reef Edge Review`; the prior client report was restored afterward.

### Shore intersection correction and algae variation, 2026-10-05

The first reef-edge fix excluded Rockpool Shore and therefore did not address its cliffs. The correction blends both reef and shore profiles across neighboring vanilla biomes, including their shaping strength. It also removes a separate discontinuity at continentalness -0.22, where entering the shore branch previously disabled ocean shaping abruptly. A density regression reproduced that failure before the repair. An initial blend weakened narrow shores enough to remove the searched rock stacks; saturating shore coverage at 75% restores fully shaped cores without reinstating the categorical edge switch.

Fresh seeds 42, 1337 and 8675309 passed terrain, landmark, colony and locate checks, plus 2,705 / 1,503 / 2,405 coastal crossings. Those include 274 / 512 / 546 actual shore-to-reef contacts. The validator additionally checks noise-generated floor heights: 49 / 41 / 53 measured adjacent pairs had maximum steps of 3 / 1 / 6 blocks. The latter two runs reserve separate measurement budgets for reef contacts so other coastal crossings cannot exhaust that coverage. All 26 Pelagic JVM checks passed. The broad run passed 2,447 of 2,450 JVM tests and 743 of 746 loaded tests; the existing documentation/archive, book-count and two centrifuge failures remain. Compilation, JAR assembly and packaged resource checks passed.

Hematic Algal Crust now uses equally weighted 0/90/180/270-degree model variants with rotating UVs. The position-selected orientation stays stable across reloads and applies to existing crust blocks. Terrain changes still require new chunks. Evidence is under `build/pelagic-ecology/shore-edge-*`; the three fresh world reports use `<seed>-shore-edge-core`.

Further native inspection found that the six-block step at seed 8675309, X-20/Z-465 was a remaining lip, not a pool rim. The final correction also changes transitional density: a capped target plus a shallow foundation cutoff could make a detached shelf appear abruptly despite smooth biome weights. The regression reproduced a surface jump from Y31 to Y56. Transitional columns now retain a vertical density gradient and stay connected downward until the new surface is established. Inactive terrain and fully influenced terrain retain their prior behavior, including full-strength cave protection; partly influenced coastal foundations can fill more deeply to form the connecting slope.

Final fresh runs (`<seed>-shore-final`) passed all acceptance checks on seeds 42, 1337 and 8675309. The same 6,613 coastal crossings include 1,332 shore/reef contacts; 141 actual adjacent noise-height checks, including 86 shore/reef pairs, now have maximum steps of 3 / 1 / 3 blocks. None of those measured pairs exceeds three blocks. All 27 focused Pelagic tests pass. The final full run passed 2,448 of 2,451 JVM tests and 742 of 746 loaded tests: the same documentation/archive, book and two centrifuge failures remain, and the unchanged vulture fixture again despawned its actor. JAR assembly and packaged resource checks passed. Evidence: `shore-density-red.log`, `shore-density-final-tests.log`, `shore-final-<seed>.log`, `shore-final-gates.log` and `shore-final-results.json` under `build/pelagic-ecology`.

Native captures 42/43 inspect natural shore/reef slopes, and 44 shows varied algal rotations around natural tide pools. Captures 45/46 record the remaining lip before the density correction; 47/48 revisit the same seed-8675309 location after that correction. Files are `build/pelagic-client/screenshots/pelagic-reviewed-<index>-rockpool_shore.png`. Successful client logs are `shore-edge-client.log`, `shore-edge-ledge-client.log` and `shore-final-client.log` under `build/pelagic-ecology`. These terrain views use night/conduit vision; the algae view is above water. The prior client report was restored after each run.

## Hydrothermal relief follow-up

The plains now have gentle broad rolls and localized 5–8-block vent mounds; shared candidate coordinates keep the seeded chimneys on their raised foundations. The existing snail-bearing vent feature builds a thicker mineral apron. The deepest foundation remains sealed, rare seamounts retain their separate profile, and saved chunks are unchanged.

Verification: 28 focused Pelagic JVM tests passed. New regressions require multiple low-plain elevations, predominantly low terrain, smooth plain slopes and raised vent centers across seeds 42, 1337 and 8675309. Fresh natural-world runs for all three seeds passed the existing biome, habitat-growth, locate, water/foundation and coast/reef checks (`build/pelagic-worlds/<seed>-hydro-relief/pelagic-report.json`). Native-client captures 49 and 50 show the same seeded vent mound from elevated and near-seabed viewpoints, with night vision used to inspect its geometry. The temporary client report was restored after the disposable review session.

Full repository gates were rerun (`build/pelagic-ecology/hydro-relief-gates.log`): 2,452 JVM tests ran with the same three unrelated documentation/archive failures. Loaded tests retained the Liber entry-count mismatch, two centrifuge accounting failures and the vulture pursuit fixture despawning before fifty ticks. Compilation, JAR assembly and packaged-resource checks passed; overall `alphaCheck`/`build` remain blocked by those unrelated tests.

Vent-layout follow-up: seeded fields now choose 1–5 chimneys, with jittered directions and distances instead of the fixed three-chimney row. All 29 focused Pelagic tests passed, including all five counts across seeds 42, 1337 and 8675309, separated bodies, non-collinear larger fields and deterministic chunk ownership. A fresh seed-1337 natural-world run passed (`1337-vent-layout`), and native-client capture 51 confirms the spread across the mound. JAR assembly and packaged-resource validation passed. The full unrelated release suites were not repeated for this layout-only follow-up.
