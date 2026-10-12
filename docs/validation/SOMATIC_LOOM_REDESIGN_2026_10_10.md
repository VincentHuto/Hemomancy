# Somatic Loom redesign — 2026-10-10

The Somatic Loom now matches the other redesigned Harbinger stations. A script authors its baked model, and the renderer still draws only the floating items and ritual effects. Memory weaving, the footprint and the interactions are unchanged.

## Design

- **Frame.** Two hematic iron side frames stand on bone claw feet. Each has front and back posts with bone capitals, and a pillar castle upright crowned with a bone finial and braced from the sill. Iron stretchers tie the frames.
- **Warp.** Fifteen pale nerve-fibre threads run from a nerve-bundle warp roll (with bone flanges and a ratchet) over a blood-wood back rest. They open in a shed with a bone shuttle thrown through it.
- **Harness.**
  - Two heddle shafts carry bone-eyed hyphae heddles. They hang over bone pulleys from a nerve ganglion bound to the castle crossbar.
  - Veins climb the uprights to the ganglion.
  - Lamm cords tie each shaft to a blood-wood treadle with a bone foot-pad.
- **Reed.** An underslung beater: rocker shaft, blood-wood swords and shuttle race, iron dents, and a bone hand-tree.
- **Cloth.**
  - The woven tapestry runs from the fell over the breast beam and down onto a cloth roll, which is held by a bone ratchet and turned by a bone-handled crank.
  - Recent weft picks and the fell glow (full-bright hemorrhagic crust). Darker glyphs are woven further toward the weaver.
- **Pirns.** Eight wool pirns, dyed in `EnumBloodTendency` order, stand on bone shelves outside the side frames. They are the loom's enzyme reservoirs.
- **Materials.** Hematic iron, bone, blood-wood, nerve bundle, calcified hyphae, puppeteer's wool and hemorrhagic crust, plus vanilla wool for the pirn colours. There is no copper, brass or silver.
- **Placement.** The weaver stands at the front filler and the warp roll sits in the back filler. The castle rises into the filler above. The memory floats over the fell at the block centre, clear of the reed and the catalyst orbit.

## Code

- `SomaticLoomRenderer.ORB_THREAD_CENTER_Y` moved from 0.58 to 0.86 blocks, so the ritual orb strands converge on the fell.
- `SomaticLoomBlock`'s outline and collision cells are retraced from the new model, through `ModelShapes.cells`.
- `SomaticLoomBlockEntity` no longer uses vanilla particles:
  - The drag trail is a tendency-coloured HutosLib glow with periodic embers.
  - The absorption burst is dark-glow soot, tendency embers and glow.
  - `DustParticleOptions`, `REVERSE_PORTAL` and `POOF` were removed.
- Retired: the converted `model_somatic_loom.png` atlas and the old Blockbench-exported element list.

## Sources

`tools/model_export/somatic_loom.py` uses `station_kit`. It writes:

- `models/block/somatic_loom.json`, which bakes in the cutout layer, under the translucent ritual effects;
- the item model, which carries the body with its display transforms;
- the facing blockstate;
- `models/block/bbmodel/somatic_loom.bbmodel`.

Run it with `--preview` to write `build/somatic_loom_preview.png`.

## Results

- **Export.** 335 elements. Two runs gave identical hashes. `coplanar.py` reports 0 fights.
- **JVM.**
  - `compileJava`, `compileTestJava` and `compileGameTestJava` passed.
  - These contracts pass when run directly: `ModelFirstTileRendererResourceTest`, `MemoryWeavingRedesignSourceTest`, `WaterloggableDecorativeBlocksTest`, `RegistryCreativeOrderSourceTest` and `BloodStructureStationProgressionRecipeTest`.
  - `ModelFirstTileRendererResourceTest` has new loom assertions: generator credit, cutout layer, bounds, named parts, the retired atlas, the traced outline, and no `ParticleTypes.` in the block entity.
  - `MemoryWeavingRedesignSourceTest` now pins the custom particles.
- **Native client.**
  - Run `gradlew.bat -I build/loom-world.init.gradle runLoomReviewClient`.
  - `LoomClientReview` places an empty loom, a stocked north-facing loom and a stocked east-facing loom (memory, ganglion catalyst and enzymes) in a disposable world. It captures `build/loom-client-review/screenshots/loom-0..10.png` by day and night.
  - The models load with no loom model errors, and both facings render.
  - The memory floats above the fell and the catalyst orbits clear of the castle. The tendency star sits above the frame (since removed; see the follow-up).
  - The weft, fell and veins glow at night. The item renders in the hotbar.
  - The first-person and third-person scales were reduced after the first capture, then re-captured.
- **Not run.** The full `test` task, the GameTest suite and `alphaCheck`. The ritual orb drag (and its new particles) was not exercised natively.

## Follow-up (2026-10-11): ritual effects on the loom

The floating ritual displays were replaced with effects drawn on the loom's own parts. The tendency star, enzyme rings, blood-volume ring, progress ring and awaiting-blood ring are gone. Weaving mechanics are unchanged.

| State | Where it now shows |
|---|---|
| Stored enzymes | Dyed thread wound on each bone pirn. The `somatic_loom_cop_<tendency>` parts are stretched to the square root of stock/64, so a weave's 1–8 enzymes still show. Empty pirns are bare spindles. |
| Loom reservoir | The upright veins, now dark scab in the model, fill from the sill. |
| Chosen weave | Its tendencies take contiguous shares of the 15 warp threads, sized by requirement, and glow faintly in their colours. A share whose enzyme is short gutters dark toward clot red. |
| Projected blood | A hot pulse climbs the veins and runs along the crossbar into the ganglion. The ganglion beats and swells, quickening with the charge, and the crossbar vein fills. |
| Weaving | Each absorbed orb lights its tendency's threads and lays a shimmering band at the fell; earlier bands sit further toward the breast beam. Orb strands converge on the fell. |
| Items | The memory floats over the fell and the result hovers above the ganglion. Catalysts lie in a row on the fresh cloth: the shuttle race sits under the warp, so they could not rest there. |

- **Anchors.** `SomaticLoomGeometry` is generated by `somatic_loom.py`, so the renderer and the baked model share coordinates.
- **Rules.** `SomaticLoomEffectRules` is pure maths (thread shares, fills, heartbeat, pulse, band spacing, catalyst spread), covered by `SomaticLoomEffectRulesTest` (11 tests).
- **Model changes.**
  - The static weft stripes near the fell were removed; the remaining old weft is unlit.
  - The item model shows every pirn fully wound.
  - The pirn spindle radius is 0.3, because a 0.35 spindle nearly coincided with the cop's octagon facets (64 coplanar fights, now 0).
- **Results.**
  - Export is deterministic, with 0 coplanar fights across the body and the cops together and on the item.
  - The resource and weaving contracts are updated and pass. The weaving test now pins the world-space orb pass and the fell-anchored strands.
  - `LoomClientReview` now stages five looms by NBT/API: empty; stocked with no weave; Sanguine Tempest short of Animus; Sanguine Tempest half-paid on an east-facing loom; and Blood Rush mid-weave with 3 of 7 orbs home. It captures `loom-0..12.png` by day and night.
  - Every state reads on the loom, and the effects follow the east-facing rotation.
- **Not run.** The full `test` task, the GameTest suite and `alphaCheck`. Orb dragging and absorption were staged through NBT, not exercised with a Living Staff.

## Follow-up (2026-10-11): strands and enzyme shortages

- **Enzyme shortages mid-weave.**
  - *Cause.* Each strand cost its base enzyme plus a drift surcharge of up to 8, which grew as it wandered from where it scattered. Payment checked only the base requirement, and enzymes cannot be inserted while weaving. So drift could run the loom dry, giving "The loom lacks X enzymes" on drag or "The final strand frays" on absorption.
  - *Fix.* The surcharge is removed: a strand costs exactly its share of the recipe.
  - *Never blocking.* `spendOrbEnzyme` never blocks. Older saves whose stock was already drained by the old surcharge still finish.
  - *Text.* The selection hint no longer warns about extra enzymes; requirements read "N × Enzyme (stored)".
  - *Test.* `somatic_loom_validation` (new suite, `runSomaticLoomGameTestServer`, alpha) has `driftingStrandsNeverRunTheLoomDry`. It pays Axonal Transduction with exactly 4 Neurotic enzymes, drifts every strand to the far edge of the ritual bounds, then weaves them home. It failed before the fix, at "Every drifted strand should be woven home", and passes after it: the memory crystallizes and the stock reaches exactly 0.
- **Strand visuals.** The "yarn balls" are replaced with loose thread-ends, all drawn on `LOOM_EFFECT`.
  - Each strand is a camera-facing knot: a hot core, a breathing halo and three turning wraps.
  - Three frayed fibres trail against its motion, and hang when it is still.
  - One quadratic-Bezier tether sags to the fell (`SomaticLoomEffectRules.tetherSag`), with a bead of light running loomward (`shimmerPosition`/`shimmer`).
  - Removed: the shader-shell spheres, the nine radial fractal strands, the fractal tether, the drag trail and the public `HemoRenderTypes.loomOrbShell` wrapper. The private shell and `loom_orb` shader stay for the fane boundary shell.
  - The scatter and wander ranges are unchanged.
- **Results.**
  - `SomaticLoomEffectRulesTest` has 13 tests and passes.
  - `MemoryWeavingRedesignSourceTest` now pins the thread-end hooks and forbids the yarn-ball pieces and the shortage messages. It passes, along with `ModelFirstTileRendererResourceTest` and `MnemonicLowtideParchmentSourceTest` (slice marker moved to `faneBoundaryShell`).
  - Native captures (`loom-5`, `loom-10`) show clean tethered knots converging on the fell by day and night.

## The witnessed weave (core) — 2026-10-11

Plan: `docs/superpowers/plans/2026-10-11-somatic-loom-witnessed-weave-core.md`; spec: `docs/superpowers/specs/2026-10-11-somatic-loom-weave-design.md`.

The orb-dragging ritual is replaced. Payment spins the enzymes from new vials under the pirns onto the pirns. A translucent echo of a past Harbinger (the Mnemonist model, posed by the real casting clip for the memory's school and rank) casts on the warp. One hostile `FrayedThreadEntity` tears loose per loop and attacks by tendency, through `ThreadAttacks`, `ThreadBoltEntity` and `ThreadHazardEntity`. Survive or strike threads slack, then hook them home with a Living Staff. A 60-tick climax grows the echo, turns it to the weaver, casts again, and carries the vessel into the fell.

- **Rules.** `SomaticWeaveRules` owns the numbers: an 80-tick loop with the fray at 46, rank caps (2 below SUMMA, 3 from SUMMA), strain per attack and per hit, power, intervals, the 4-health non-lethal floor, the 200-tick harm window and the 12-block area.
- **Pausing.** Death, logout, leaving the area and blackout all pause the weave and recall loose threads; nothing fails. Pre-witnessed saves migrate their orbs to threads.
- **Removed.** The fixed-depth drag, the orb wander code and the Living Staff's 21×11×21 per-tick loom scan.
- **Client.** The client never had `curRecipe`, so the echo and warp read the weave through `getWeaveRecipe()`. The first native capture found this, because the echo was missing.
- **Echo casts.** The `EchoCasts` registry starts with every manipulation pending (`src/test/resources/echo_cast_pending.txt`) on the school-gesture fallback. Plan 3 fills it.

### Results

- **JVM.** Passing: `SomaticWeaveRulesTest` 6/6, `SomaticLoomFrameTest` 2/2, `SomaticLoomEffectRulesTest` 13/13, `EchoCastCoverageTest`. Contracts exit 0: `ModelFirstTileRendererResourceTest` and `MemoryWeavingRedesignSourceTest`.
- **GameTests.** `somatic_loom_validation` 14/14. It covers spin-up, the fray cadence and cap, the hook lifecycle and climax, taut refusal, the area pause and resume, mid-weave save, orb migration, orphan unravel, tendency hits, Animus drain, Mortem miasma and Ferric spikes, the low-rank blackout, zombies still being lethal, and helpers straining and hooking.
- **Other suites.** The main `hemomancy` suite passes 900/901. The failure, `blockItemExceptionsKeepTheirOwners`, comes from another session's `BlockInit` edit. The clinical suite passes 129/131; the two journey-snapshot failures (Fungal projection, Silent victory) are unrelated to the loom.
- **Native captures.** `LoomClientReview` stages a live weave with the spectating player as weaver. Captures `loom-0..9` show:
  - threads fraying and hunting the weaver, tethered back to the echo;
  - the echo on the warp, with the memory below it;
  - an east-facing weave;
  - the vials;
  - the climax at about 2/3 (grown echo, arms raised, school aura, woven bands);
  - night glow.
- **Tuning after the first captures.** The echo is now 0.42 → 0.8, more opaque and lightly tinted. The memory sits lower and forward, and the ganglion glow while weaving is softer.

## Loom tiers (2026-10-11)

The Somatic Loom joined the shared station-upgrade system as its sixth station (`UpgradeStation.LOOM`, seat distance 2).

| Stage | Rite / degree | Kit | Weaves | Additions |
|---|---|---|---|---|
| Base | — / D3 | — | Humilis to Summa | Small grey echo that stutters |
| Ganglionic | `ganglionic_weave` / D5 | `ganglionic_harness` | adds Magister | Grown ganglion, crossbar coils, upright nerve cords, nerve vial cradles, four feelers that take slack threads within 4 blocks of the echo, staff hook reach 8 blocks, steady coloured echo |
| Cortical | `cortical_weave` / D6 | `cortical_crown` | adds Perfectus | Cortex crown and iron arch, myelinated warp, life-size echo that brightens at the climax |

The plan's review fixes to the core weave (lava not floored, helpers unhunted, floor on first harm, echo replay on a second weave, melee-only staff strain with recovery) landed first, each test-first.

Evidence:
- `runSomaticLoomGameTestServer`: 31/31.
- `runStationUpgradeGameTestServer`: 43/43.
- Station, book, weave-rules and effect-rules JVM tests pass. The `ModelFirstTileRendererResourceTest` and `MemoryWeavingRedesignSourceTest` contracts exit 0.
- Coplanar checks report 0 fights for all three bodies, and the generator re-exports deterministically.
- Native captures in `build/loom-client-review/screenshots/loom-0..10.png` show:
  - the base, Ganglionic and Cortical weaves side by side;
  - the Cortical climax;
  - an east-facing Ganglionic loom;
  - night shots.

The captures prompted two tuning changes. The Cortical echo's alpha and colour wash were lowered after it first rendered as an opaque golden statue, and the Ganglionic feelers were thickened.

## Cortical Chamber dome (2026-10-11)

**Spike result.** Chamber themes render as a camera-centred skybox, not around a world point. They can still be windowed: `SomaticLoomDomeRenderer` builds a stencil portal at `AFTER_CUTOUT_BLOCKS`.
1. The dome's back faces are depth-tested against the opaque scene and mark the stencil.
2. The weaver's theme renders through `effectsById(theme).renderSky` inside that stencil.
3. The far wall's depth is rewritten inside the stencil, so anything beyond the dome stays hidden.

**Evidence.** Native captures `loom-4..8` show:
- the default and Qliphoth themes;
- views from inside and outside, by day and by night;
- the climax inside two overlapping domes.

In each, the loom, the echo, the threads and the ground inside the dome stay real. From inside, the ground beyond the radius gives way to the Chamber void.

**Server side.** `skyThemeOf` reads the theme without creating Chamber records. `aCorticalWeaveCarriesTheWeaversChamberTheme` checks the default theme, an override and a save round-trip.
