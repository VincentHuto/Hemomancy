# Track 6: Ocean placement rules

**Status:** DONE 2026-10-30. Seat and outpost structure sets with one-block stub sites, `CourtPlacementRules`, `CourtSeatCandidates.nearest` and `court_worldgen_validation` landed and pass on seeds 42, 1337 and 8675309 (forward and reverse); left: real templates (Tracks 7 and 8), the Garden coast check (Track 7), Shared Wake natural schools (Track 9), and the reviewer pass.

Phase B. No dependencies; can start any time. Spec sections: "Houses mapped onto the existing ocean layers", "Three tiers of presence", "Placement rules". Terrain contract: `docs/PELAGIC_WORLDGEN.md`.

## Goal

Court structures land in the right house's water, at the right depth, never stacked over each other, never crowding another faction's landmark, and never costing the worldgen thread what the Circus pavilion once did. The ocean should feel inhabited in places and empty in most: you find a house; you don't sail through a village chain.

## Where each house lives (SETTLED)

| House | Biome | Y band (nominal; quart sampling blends edges) | Tier |
| --- | --- | --- | --- |
| The Holding Stone | Rockpool Shore | coast, pools | outpost (Tide Refuge) |
| The Giving Light | Erythrocoral Reef and the coast beside it | sunlit shelf | **Seat** (Lighthouse Garden) |
| The Shared Wake | Pelagic Ocean | open water | roaming school; rare nursery ground |
| The Woven Current | Twilight Ocean | 33..13, gullies | outpost (Line Camp) |
| The Veiled Fold | Midnight Ocean under temperate or warm surface | 12..-8, canyon clefts | outpost (Screened Cleft) |
| The Still Bell | Midnight Ocean under cold or frozen surface | 12..-8 | outpost (The Bell) |
| Last Marrow | Carrion Depths | -9..-30, sediment fans, fossil beds | outpost (Whale Fall) |
| The Kindling | Hydrothermal Depths | -31..-53, vent fields | **Seat** (Vent Sanctum) |

Veiled Fold and Still Bell share Midnight and split by the surface climate above them, so they never meet.

## Structure sets (SETTLED starting values 2026-10-29, W2; tune on the review seeds)

| Set | Entries | Placement | Notes |
| --- | --- | --- | --- |
| `brined_court_seats` | `lighthouse_garden`, `vent_sanctum` | `random_spread` spacing 64, separation 24 | About 1,000 blocks between Seat cells. Each entry only validates in its own habitat, so a cell holds at most one Seat. |
| `brined_court_outposts` | `tide_refuge`, `line_camp`, `screened_cleft`, `still_bell`, `whale_fall`, `wake_nursery` (weight low) | `random_spread` spacing 28, separation 10, `exclusion_zone` against `brined_court_seats` | One set means one outpost per cell whatever its depth: outposts can't stack vertically over the same seabed. The cell picks among entries valid there. |
| signs | placed features, not structures | per-biome rarity and a per-region cap | Track 8. |

All files under `data/hemomancy/worldgen/structure/` and `structure_set/`, with a `hemomancy:brined_court` structure tag for discovery and exclusion. Salts (chosen 2026-10-30): `brined_court_seats` 1029834751, `brined_court_outposts` 1029834763.

## The placement predicate (PROPOSED)

One class, `common/worldgen/structure/court/CourtPlacementRules`, pure and testable, called first from every Court structure's `findGenerationPoint`:

1. **Pelagic active?** If the Pelagic hooks aren't installed (disabled config, unsupported preset, other dimension), no Court structure generates (SETTLED 2026-10-29, W1).
2. **Cheap biome check first.** Ask the biome source (no chunk loads) for the house's biome at the cell centre and the house's Y band. Vanilla's stub check samples one point at one height; the depth biomes are only about 20 blocks tall, so pick the sample Y deliberately: the band's middle, then confirm at the chosen floor Y. Midnight houses also read the surface biome's temperature at sea level.
3. **Floor from the terrain sampler.** Use `PelagicTerrainSampler` (seeded, coordinate-only) for floor height at the few footprint points the structure needs. Never build a noise chunk per column (the pavilion lesson). Reject steep floors with corners and middle points before any interior scan.
4. **Seat anchors.** The Vent Sanctum takes its centre from the terrain's seeded vent-field centres (the same candidates that raise the mounds and chimneys), not the cell's random point, so it always sits on a real vent mound. Expose those centres from the sampler if they aren't public. The Lighthouse Garden needs a coast: shore or reef next to land within its footprint, solved in Track 7 with the same corner-first screening.
5. **Exclusions.** Reject a candidate within the exclusion radius of another faction's landmark: Circus pavilion, troupe camps and abandoned stages, Voyager wrecks and vessels, ocean monuments, mausoleums. Don't look for generated structure starts (slow and order-dependent). Compute the other sets' **potential** cells with their own placement math (pure, like vanilla's `RandomSpreadStructurePlacement` potential-chunk calculation) and reject if one falls inside the radius. Potential cells over-reject slightly; that is fine.

`exclusion_zone` in JSON covers outposts versus Seats. The predicate covers everything else.

## Clutter budget (SETTLED starting values 2026-10-29, W2)

- **One landmark per view.** No two faction landmarks (Court or otherwise) within 96 blocks horizontally. Underwater fog hides the rest: only the Lighthouse Garden and Tide Refuge are visible from the surface, and they are the rarest.
- **No vertical stacking.** One outpost per cell (above). A Seat cell excludes outposts.
- **People stay home.** Court NPCs spawn with their structure and are tied to it (Track 9). No natural Court NPC spawns, except Shared Wake schools (allowed, P3), capped at one school per 1024×1024 region and never within 256 blocks of a Seat.
- **Signs are sparse.** Track 8's per-region cap.
- **Measured, not guessed.** The validation run below counts landmarks per 512×512 region and fails over budget.

## Performance

- Predicate order: config gate, biome-source check, sampler floor points, exclusion math, then any footprint scan. The pavilion's all-column scan once took 47% of the worldgen thread; Court structures don't do all-column scans.
- No `/locate`-driven discovery. The Relay Chart uses `CourtSeatCandidates.nearest` (Track 5), built on this predicate's first two steps.
- `/locate structure` on Court structures still works and still runs vanilla's synchronous search; the biome-locate stride fix (`MixinPelagicBiomeSearch`) is for biomes only. Note the cost in the docs rather than fix vanilla.

## Validation

Add a `court_worldgen_validation` row to `gradle/game-tests.gradle` (run name `courtValidationServer`, needs a fresh seeded noise Overworld like `pelagic_worldgen_validation`) with its own generator class `CourtWorldValidation` beside `PelagicWorldValidation`. Separate class because `PelagicWorldValidation` has uncommitted edits from another session.

Checks on seeds 42, 1337 and 8675309, forward and reverse:

- Every placed Court structure's centre biome and Y band match its house.
- No two Court outposts in one cell; no outpost within the Seat exclusion zone.
- No faction landmark pair within 96 blocks.
- Landmarks per 512×512 region within budget.
- The Vent Sanctum sits on a seeded vent centre.
- Placement time: `findGenerationPoint` samples under a set share of the worldgen thread on a 45-second travel recording (JFR, as in `PELAGIC_WORLDGEN.md`).
- `CourtSeatCandidates.nearest` returns within its time bound.
- Inland and other-dimension controls generate nothing.

## Tests

- Unit: `CourtPlacementRulesTest` (biome and band per house, Midnight climate split, exclusion math on known cells, gate off when Pelagic is off).
- Frozen fixtures for any new structures and tags.

## Docs

`docs/PELAGIC_WORLDGEN.md` gets a Court placement section (the Generation contract currently says Morphling systems sit outside the terrain work; update that line). A new Court section in `docs/HEMOMANCY_REFERENCE.md` names where each house is found.


## What landed (2026-10-30)

- `common/worldgen/structure/court/`: `CourtSite` (the eight sites, house, layer, band sample Y, surface climate, footprint and variation limits, rarity), `CourtPlacementRules` (the predicate; `stub`, `screen`, `habitat`, `ventAnchor`, `levelFootprint`), `CourtLandmarkCells` (pure potential-cell exclusion over `Spread`s), `CourtWorldProbe` (biome source, climate sampler, `PelagicContext` and registry behind the predicate), `CourtStructure` (one `hemomancy:brined_court` structure type; the JSON names its `site`), `CourtSeatCandidates.nearest(ServerLevel, BlockPos, maxRings)` for Track 5.
- `HemoJigsawStructure.codec` gained an overload with one extra field; the six jigsaw fields are one shared `MapCodec`.
- `PelagicTerrainSampler.VentSite`/`ventSite` and `VENT_CELL` are public; `PelagicLandmarks.hostsVentField` is the one rule for "this seeded site builds its mound and chimneys", used by the landmark plan and the Vent Sanctum anchor.
- Data: eight structures, two structure sets, eight template pools (`brined_court/<site>/start_pool`), one stub template `structure/brined_court/stub_marker.nbt` (a single bone block), eight `has_structure/brined_court_<site>` biome tags, the `hemomancy:brined_court` structure tag. Frozen `registry-ids.txt` updated.
- Tests: `CourtPlacementRulesTest` (11), `CourtStructureResourceTest` (4). GameTest generator `CourtWorldValidation`, suite `court_worldgen_validation`, launcher `runCourtValidationServer` (`-PcourtSeed`, `-PcourtRun`, `-PcourtOrder`, `-PcourtTravelSeconds`).
- Docs: `docs/PELAGIC_WORLDGEN.md` "Brined Court placement" and the Generation contract line; `docs/HEMOMANCY_REFERENCE.md` §29 "The Brined Court: where each house is found".

For Tracks 7 and 8: replace the pool's element, keep the structure JSON's `site`, `biomes` and set. The jigsaw start is passed one block above the marker (a pool element's ground layer sits one block under its start), so a template's bottom layer lands at the marker, the first water block above the sampled floor. Seats can stand up to 72 blocks from their cell's chunk centre; keep a Seat's pieces within about 56 blocks of its marker so they stay inside vanilla's eight-chunk reference range. The stub bone block replaces whatever the vent landmark put at the Sanctum's centre column; Track 7's "chimneys unchanged" check needs its template to leave that column alone.

## Calls made unattended

- **Midnight split:** cold or frozen means the climate sampler's temperature at sea level below -0.15, the top of vanilla's cold band. Biome base temperatures can't split it (vanilla cold and temperate oceans both have 0.5).
- **Vent Sanctum anchor:** the nearest active seeded vent site within 72 blocks of the cell's chunk centre, over the 3-by-3 vent cells around it, that passes `hostsVentField`.
- **Lighthouse Garden anchor:** reef is patchy, and with one point per cell only 1 of 256 Seat cells on seed 42 was reef. The Garden tries a 5-by-5 grid at 24-block steps (up to 48 blocks out), nearest first. The coast requirement stays Track 7's.
- **Floor bands:** the layer check is made at the marker and at the start above it through the real biome source, so quart sampling decides band edges. The Tide Refuge also requires floor Y 56..68; the wake nursery needs a Pelagic Ocean floor at or above Y 33 (banks), since its biome only exists above Y 34.
- **Footprint limits:** corner and edge-midpoint floor variation 6 for Tide Refuge, nursery, Line Camp and Whale Fall; 8 for the Bell and Garden; 10 for the Sanctum's mound; 12 for the Screened Cleft's canyons. Footprint half-widths 8 (outposts) and 24 (Seats). A point under half shaping strength refuses the site.
- **Wake nursery rarity:** "weight low" alone does nothing when habitats don't overlap, so the nursery keeps one in four valid cells (seeded hash of the start chunk); weight 1 versus 3 for the others.
- **Exclusion filter:** a faction's potential cell counts only if its surface (Y63) or floor biome is in some structure of that set's biome list, so land sets do not blank the sea; their distance gets 16 blocks of slack for template centres. Outposts also keep 96 blocks from where a Seat would actually stand (`SEAT_REACH` 72), because the Seat set's `exclusion_zone` (6 chunks) measures from the cell's chunk, not the anchored Seat.
- **`CourtSeatCandidates.nearest`** runs the full predicate (not only the first two steps) on each Seat cell; it is still chunk-free, and it then names exactly the Garden that generates.
- **Stub marker:** one shared one-block template (bone block) for all eight pools.
- **Validation budgets:** at most 2 Court sites and 4 landmarks of any faction in a 512-by-512 region that holds a Court site (land-only regions are not counted: troupe camps alone exceed any sea budget); Court code at most 2% of generation-worker JFR samples; Seat search over 8 rings within 2 seconds. Pair spacing is checked for pairs involving a Court site; faction-faction pairs are those sets' own business.
- **JFR** is recorded in-process (`jdk.jfr.Recording`; the launcher adds `--add-modules jdk.jfr`) during 45 s of full-chunk generation along a 7-chunk lane starting near the nearest outpost.
- **Validation square:** outposts and faction landmarks are loaded within 6,144 blocks of the origin (3,072 found only two outposts on seed 42, much of it land); Seats within 8,192, then predicted out to 32,768 if one kind is missing.

- **Wake nursery widened (review fixes 2026-10-30, user: "never able to be found").** The review seeds had only 6 to 9 Pelagic Ocean cells each, all deep basins (centre floors -48 to 4; the shallowest point within 64 blocks reached Y 13 at best), so no bank rule on that habitat could ever pass. The nursery now takes open water on a bank or the upper slope: habitat is Pelagic Ocean at Y 48 **or** Twilight water at Y 24 under any ocean (`NURSERY_SLOPE_Y`); its floor must be at or above Y 18 (`NURSERY_MIN_FLOOR`); its marker may read Pelagic Ocean or Twilight (`CourtPlacementRules.markerLayer`, and the structure's biome tag gains `twilight_ocean`); it searches a 9-by-9 grid at 16-block steps up to 64 blocks round its cell's chunk centre, nearest first, like the Garden; floor variation 8 (was 6); rarity one in 24 valid cells (was one in four). At one in two, seed 42 generated 55, far more than the 6 Line Camps, hence 24. The validation report's funnel row lists each nursery habitat cell's floor and shallowest search point (`habitatCells`) for later tuning.

## Verification (2026-10-30)

- `./gradlew.bat test`: 4,687 JVM tests, no failures, including `CourtPlacementRulesTest` (11), `CourtStructureResourceTest` (4), `PelagicLandmarksTest` and `StructureTemplateCoverageTest`.
- `./gradlew.bat runRegistrationGameTestServer`: all 47 tests pass, including the frozen registry ids.
- `./gradlew.bat runCourtValidationServer -PcourtSeed=<seed> -PcourtRun=t6-final-<order> -PcourtOrder=<order>` for seeds 42, 1337 and 8675309, forward and reverse: all six pass; forward and reverse produce identical sites per seed. Numbers are in `docs/PELAGIC_WORLDGEN.md` (Brined Court placement). The JFR travel lanes sampled no Court frame, so the cost is below the sampler's resolution there, not a measured share.
- Reviewer pass (2026-10-30): no defects found. Re-ran `./gradlew.bat test` (4,687, no failures), `runRegistrationGameTestServer` (47 pass) and `runCourtValidationServer -PcourtSeed=42 -PcourtRun=t6-review-forward` (pass, the same 19 sites). Open tuning, not a Track 6 defect: the Wake nursery passed habitat in 6 to 9 cells per seed but the floor screen in none, so it never generated on any review seed. Decide with Track 8 (whether the nursery is wanted) whether to widen its bank rule.
