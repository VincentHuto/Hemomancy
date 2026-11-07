# Circus worldgen and structures audit (2026-11-06)

Scope: where the Crimson Troupe's sites stand, how they are built and checked, what is inside them, the private perception geometry, the trail that leads a player there, and the block models the sites use. Read-only audit: no source, resource, tool or existing doc was changed, and neither Gradle nor any generator was run. Evidence comes from Grep and file reads, and from a standard-library NBT reader run with `python -I` from a temp directory over the committed `.nbt` templates (block counts below are exact counts from those files). The Brined Court Seats and outposts and the four Saint mausoleums were read the same way as the comparison bar.

## Summary

The Circus is on the oldest pipeline in the mod and it shows. The 65x65 pavilion is the 33x33 arena that a Java datagen class drew in September (a wool cone on iron pillars around a stone disc), pasted unchanged into the middle of a larger template, with six red-wool boxes, stone walks and two terracotta practice squares added around it by `tools/circus/build_troupe_assets.py`. The five teaching camps are the same 25x25 layout five times over: two red and white wool A-frames, a plank boardwalk and one to four props that differ by variant. Measured from the templates, wool (vanilla and Puppeteer's Wool) and vanilla planks are 64% of the pavilion's solid blocks and 47% of the camps'; the camps are 98.6% vanilla. The pavilion has no stairs, walls or trim at all. Apart from the Puppeteer's Spindle, no Circus block comes from `tools/model_export/`, and the four investigation records are plain dark-oak log cubes.

The pipeline cannot be re-run. The builder reads a legacy NBT and deletes a JSON file, and the 2026-11-03 cleanup removed both, so it would stop on its first structure step. `CircusPavilionTemplate`, the Java class that drew the arena, is still in `src/main` with nothing calling it except two tests.

The private galleries and canopy that the docs describe at 500 and 1000 acclimation do not exist in code. Perception is three vanilla particle types, a red screen edge and flat glow quads round the carousel. The Ringmaster stands on a beam that no floor cell can reach (D-57).

Placement is careful about cost but has no validation run of its own. The only fresh-world evidence is a locate probe inside the Pelagic validation server, which found the nearest pavilion 33,752 blocks away after 209 seconds. Nothing keeps a pavilion, a camp, an abandoned stage and a village apart. Apart from the Scarlet Waybill, which searches on the server thread, nothing leads a player to a site: no signs, no wagons, no road.

The audit raised 18 findings:

- **4 High.** The pavilion is a legacy arena plus wool boxes (W-1); the camps are one vanilla tent layout five times (W-2); the build pipeline is broken and split across Java datagen and an untracked-in-AGENTS Python script (W-3); the private perception geometry is documented as built but absent (W-4).
- **7 Medium.** A strict all-column terrain gate makes pavilions very rare (W-5); no worldgen validation server, generator checks or review client (W-6); the Waybill's synchronous search (W-7); no exclusion between Circus sites and villages (W-8); no discovery trail (W-9); dark interiors that spawn monsters (W-10); the site outcomes are six cobwebs and a reveal that was never built (W-11).
- **7 Low.** Perception visuals (W-12), Outpost emitters in the pavilion (W-13), vegetation in camp yards (W-14), the abandoned stage (W-15), no loot (W-16), block models outside the generator (W-17), doc drift (W-18).

## Site inventory

Counts are solid blocks (air, structure void, water and jigsaw excluded). "Set pieces" are modelled Circus fixtures placed in the template, counted as types / placements; building materials (venous stone, hematic iron pillar and chain, Puppeteer's Wool, Circus Curtain) are not counted. "Vanilla share" is the share of solid blocks in the `minecraft:` namespace.

| Site | Template size | Solid blocks | Pipeline | Set pieces (types / placed) | Vanilla share | Wool (any) + vanilla planks | Validation |
| --- | --- | ---: | --- | --- | ---: | ---: | --- |
| Pavilion (`crimson_troupe_pavilion`) | 65x14x65, one piece | 6,131 | Java datagen arena (`CircusPavilionTemplate`, provider deleted) copied into a Python-built shell (`build_troupe_assets.py`) | 5 / 16: Spindle 2, Specimen Jar (Prism Cuttle) 2, Blank Mask 7, records 3, Hematic Lantern 2; carousel and 10 NPCs are template entities | 57.5% | 64.1% | Template contents (`CrimsonTroupeStructureGameTests`), noise-column regression (`TroupeFootprintGameTests`), locate probe in `pelagic_worldgen_validation` |
| Teaching camps x5 (`troupe_camp_<air/blades/pageantry/embers/memory>`) | 25x9x25 each, one piece | 871 to 879 each | Python (`build_teaching_camps.py`) | 2 to 4 per camp: Spindle 1, Hematic Lantern 4, plus Troupe Rigging 1 (air), Blank Mask 3 (pageantry) or a record 1 (memory) | 98.4 to 98.7% | 47.1% | Template contents only (roof peak, ivory stripe, Spindle, crafting table, clear entrance) |
| Abandoned stage (`troupe_abandoned_stage`) | 25x9x25, one piece | 337 | Python (`build_troupe_assets.py`) | 2 / 2: Stage record, Mourning Mask | 99.4% | 76.6% | Template exists |

## Compared with Court seats and mausoleums

| | Circus (pavilion + camps) | Brined Court Seats and outposts | Saint mausoleums |
| --- | --- | --- | --- |
| Generator | Split: a Java datagen class (dead, no provider) plus `tools/circus/build_troupe_assets.py` and `build_teaching_camps.py`; the main builder cannot run (W-3) | `tools/worldgen/brined_court_seats.py`, `brined_court_outposts.py` over `tools/worldgen/brined_court/` (`kit.py`, `checks.py`, `preview.py`); output byte-identical across runs (`07-seats.md` verification) | `tools/worldgen/mausoleum_complexes.py` over `tools/worldgen/mausoleum/` with an architecture kit and one theme file per Saint |
| Pieces | 1 per site | Seats 2 pieces each, outposts 1 to 2 variants | 26 pieces per Saint (wings, chapels, niches, chamber) |
| Custom set-piece block types | Spindle only from `model_export`; masks and rigging are hand-written JSON boxes; records are vanilla log cubes | 7 Seat set pieces + 9 outpost set pieces + `house_marker`, each from a `tools/model_export/` generator with `.bbmodel` and preview | Per Saint about 20 to 27 Hemomancy block types: ring door, sarcophagus, chapel focus, reliquary sconces, basins, seals, witness and devotee figures, theme families |
| Detailing (stairs, slabs, walls, panes, lanterns as share of solid blocks) | Pavilion 1.4% (no stairs or walls); camps 3.5% | Garden 2.6% across 29,711 solid blocks of tuff, andesite, diorite, stone and moss brick, coral, bone and lacquer | 6.8 to 8.0% per Saint; "no exposed surface is a flat run of one block larger than 3 x 3" (HR §1049) |
| Distinct block states palette | Pavilion 29 solid types; camps 30 across all five | Garden 45; Harbinger Outpost 72 | 36 to 53 per Saint |
| Placement | `random_spread` 48/16 (pavilion), 24/8 (camps), 32/10 (stages); every footprint column dry, clear and within 3 blocks; `beard_thin` | Pure predicate on biome source and terrain sampler, no chunk loads; Seats bring their own terrain (tide rock); 96-block faction exclusion; outposts `exclusion_zone` against Seats | Depth placement with a widened 14-block entrance check (`SAINT_MAUSOLEUMS.md:197`) |
| Fresh-world validation | None dedicated; one locate probe | `court_worldgen_validation` (`runCourtValidationServer`) on seeds 42 forward and reverse, 1337, 8675309: sites, dry pockets after reload, chimneys unchanged, sign caps | `MausoleumAssemblyGameTests` (four seeds by four Saints), one natural run (Velorum) |
| Template checks | None beyond the GameTest's spot checks | `checks.py` (forbidden blocks, floodable air), `CourtSeatTemplateTest` | Generator-side; frozen fixtures |
| Review client | None | `brinedSeatReviewClient`, `brinedOutpostReviewClient` | `mausoleumReviewClient` (with `-PmausoleumNatural`) |
| Trail to the site | Scarlet Waybill only (D4, Mnemonist) | Relay Chart (off-thread), Brine-Sealed Letter, `court_signs` placed feature (blooms, house markers, relay markers, buoys, vent offerings), six outposts | Outer halls, Recollections |
| Residents | Template entities with `PersistenceRequired`; lost for good if killed outside the rules | `CourtResidentSpawner` on jigsaw marks, one-day vacancy refill | Encounter hooks |

## Findings

### W-1 · High · The 65x65 pavilion is the September arena plus six red-wool boxes

- **Claim.** The pavilion template is built by translating every block of the legacy 33x12x33 arena 16 blocks in and then adding a shell around it. The arena is what `CircusPavilionTemplate` drew in Java: a polished venous stone disc, nine hematic iron pillars, a stepped Puppeteer's Wool cone with blue and red wool flecks chosen by `(x + z) % 17` and `(x - z + 34) % 13`, a ring of Circus Curtain panes, two lanterns on chains, a ring of iron-pillar beams, two bone blocks and two jarred Cuttles. The shell is six `Template.room` boxes (dark oak plank floor, one plank course, red wool walls and a flat red wool roof, one vanilla lantern in a corner), polished venous stone walks in straight lines, two 8x8 terracotta-edged smooth stone squares and a few vanilla props (crafting table, smoker, barrel, cauldron, anvil, hay, ladder, a slab catwalk).
- **What the counts show.** 6,131 solid blocks: red wool 2,026, dark oak planks 1,242, polished venous stone 1,148, Circus Curtain 660, Puppeteer's Wool 612, hematic iron pillar 143. No stairs, no walls, no trim blocks. Sixteen modelled fixtures in total. There are no hanging Crossbars (the Crossbar is an item, not a block), no pelts and two bone blocks, though the physical layer the docs promise is "dark cloth, blackened frames, Crossbars, Spindles, bones, masks, thread". The seven Blank Masks carry no `facing` state, so they all face north.
- **Against the bar.** The owner's bar for the Court and the Saints is a legacy-dungeon set piece with modelled multiblocks, custom set-piece blocks and detailed walls. The mausoleum rework README lists "small box rooms" and "flat single-block walls" as exactly what the first pass got wrong (`docs/archive/tracks/mausoleums/00-README.md:3`). The pavilion's six rooms are that pattern. The Circus is meant to be the Court's equal on the mental side, and its main site has about the detail of a first-pass village.
- **Why it stayed.** Every Circus doc carries the rule that the legacy pavilion and existing sites keep their blocks (`build_troupe_assets.py:3`, `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md:129`, `docs/CIRCUS.md:101`). That rule predates the ruling that the mod is not live and needs no save compatibility (`AGENTS.md`, player-data bullet). Nothing now requires the old arena.
- **Evidence:**
  - `tools/circus/build_troupe_assets.py:71-109` (arena copy at `:74-80`, rooms `:82-83`, walks `:84-88`, courts `:89-91`, props `:94-97`, loft `:98-108`)
  - `tools/circus/troupe_templates.py:165-177` (`room`: planks, red wool, one lantern)
  - `src/main/java/com/vincenthuto/hemomancy/common/data/gen/CircusPavilionTemplate.java:19-111`
  - `src/main/resources/data/hemomancy/structure/crimson_troupe_pavilion.nbt` (counts above)
  - `docs/CIRCUS.md:32`, `docs/archive/tracks/circus/02-location-worldgen-and-perception.md:13-22`
- **Confidence:** Verified (template counts).
- **Proposed fix.** Rebuild the pavilion from scratch in a new `tools/worldgen/circus/` generator on the Court/mausoleum model: a kit (`kit.py`) with canvas, rigging, frame and stage parts; a big top with a real king-pole structure, quarter poles, a scalloped valance and guyed stakes instead of a stepped wool cone; tiered seating and galleries the Ringmaster's perch connects to (which also answers D-57); a backyard of wagons, a cookhouse and dressing tents in place of the wool boxes; a midway with a ticket booth and banner line. Add set-piece blocks from `tools/model_export/` generators: hanging Crossbar rigs, a Spindle rack, painted wagon wheels and wagon bodies, a ticket booth, a ringmaster's podium, a ring curb, costume racks, mask walls, bone-and-thread trophies, a pelt drape. Split into jigsaw pieces (big top, backyard, midway) so variants are cheap. Drop the legacy-preservation rule in the docs.
- **User's call:** yes. The pavilion's layout and its set-piece list are design calls (see the decisions below).

### W-2 · High · The five teaching camps are one vanilla tent layout five times

- **Claim.** `build_teaching_camps.camp` writes the same ground, two A-frame tents (`red_wool` with `white_wool` stripes at fixed z), a plank boardwalk, two practice pads, two fence-and-lantern posts, a bed, two barrels, slab-and-trapdoor benches, the Threadkeeper's Spindle and crafting table for every variant. The variants differ by one to seven blocks: a fence pole with one Troupe Rigging (air), two targets on fences (blades), an anvil, blackstone, a bone block and three masks (pageantry), a cauldron and smoker (embers), two bookshelves and a record (memory).
- **What the counts show.** 871 to 879 solid blocks per camp, 11 to 14 of them Hemomancy (1.3 to 1.6%), and those are four Hematic Lanterns, six polished venous stone stepping stones, the Spindle and the variant prop. Palette 24 to 27 types, all but six vanilla.
- **Consequence.** These are the sites a player visits most: the Waybill points at a camp before the pavilion for every untaught professor (`CircusWaybillItem.java:14-25`). Seven professors, five camps, and each one reads as the same pair of wool tents. The GameTest pins vanilla wool by position (`CrimsonTroupeStructureGameTests.java:49-52`), so the vanilla look is locked in by a test.
- **Evidence:**
  - `tools/circus/build_teaching_camps.py:14-108`
  - `src/main/resources/data/hemomancy/structure/troupe_camp_*.nbt` (counts above)
  - `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CrimsonTroupeStructureGameTests.java:45-65`
- **Confidence:** Verified.
- **Proposed fix.** Rebuild the camps in the same `tools/worldgen/circus/` kit as the pavilion: a travelling camp of one or two painted wagons, a canvas lean-to and a practice ring, with each discipline's camp built round its own apparatus as a set piece (a rigging tower and net for air, a knife wheel and target board for blades, stilts racks and a weight frame for pageantry, a fire pit and bellows stand for embers, a prompt-book table and costume trunk for memory). Rewrite the GameTest to check roles and set pieces, not wool positions.
- **User's call:** yes, on what each camp's apparatus is.

### W-3 · High · The build pipeline is broken and split between dead Java datagen and an untracked-in-AGENTS script

- **Claim.** `build_troupe_assets.py` cannot be run. Its `structures()` step loads `src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt` (the legacy arena) and later calls `unlink()` on `data/hemomancy/worldgen/structure/crimson_troupe_pavilion.json`; the cleanup commit `d5a7ddbfa` deleted the generated NBT and its provider, and the JSON no longer exists, so the script stops with a file error before it writes any template. The committed pavilion NBT can only be edited by hand now, which the mausoleum and Court rule forbids ("change the generator, re-run it, never hand-edit an NBT", `docs/archive/tracks/mausoleums/00-README.md:45`).
- **The script also rewrites authored assets.** On every run `native_items()` overwrites the mask, rigging and record models, blockstates, item models, loot tables and the mask/rigging recipes, and the Cinder Bellows frame texture. A re-run would undo any hand fix made to those files since.
- **Dead Java.** `CircusPavilionTemplate` (148 lines) stays in `src/main/java/.../data/gen/` with no caller in main code; two tests (`CircusPavilionTemplateTest`, a legacy `main`-method test, and `CircusPavilionPerformerTemplateTest`) keep it alive. `DynastyCastlePieceProvider`'s Javadoc still links the deleted `CircusPavilionStructureProvider`.
- **Tracking.** `.gitignore` tracks `tools/circus/*.py`, but the AGENTS.md list of version-controlled tools does not mention `tools/circus/` at all and says "the rest of `tools/` stays local".
- **Evidence:**
  - `tools/circus/build_troupe_assets.py:74`, `:115`, `:30-62`
  - `src/generated/resources/data/hemomancy/structure/` (empty)
  - `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java:19-27` (two providers only)
  - `src/main/java/com/vincenthuto/hemomancy/common/data/gen/CircusPavilionTemplate.java:7`
  - `src/main/java/com/vincenthuto/hemomancy/common/data/gen/DynastyCastlePieceProvider.java:26`
  - `src/test/java/com/vincenthuto/hemomancy/common/data/gen/CircusPavilionTemplateTest.java`, `CircusPavilionPerformerTemplateTest.java`
  - `.gitignore:73-75`, `AGENTS.md:29`
- **Confidence:** Verified (paths read; the script was not run).
- **Proposed fix.** Move all Circus structure authoring into `tools/worldgen/circus_sites.py` + `tools/worldgen/circus/` (kit, pavilion, camps, stage, `checks.py`, `preview.py`), track it in `.gitignore` and AGENTS.md like the other site generators, and have it write only templates and pools. Keep model and asset building in `tools/model_export/` generators. Delete `CircusPavilionTemplate` and its two tests (copy to `removed_assets/` per the store-removed-assets rule) and fix the `DynastyCastlePieceProvider` comment. Make the generator's output byte-identical across runs, as the Court's is.
- **User's call:** no, beyond approving W-1/W-2.

### W-4 · High · The private galleries and canopy are documented as built but do not exist

- **Claim.** `docs/CIRCUS.md` and the school contract say that from 500 acclimation private geometry (lower galleries, half-step stairs, seating) supports the player, that at 1000 the upper gallery and canopy are "privately solid", that it uses "server-authorized masks, acknowledged revisions and the same rotated authored cells for drawing, collision and targeting", that Succession commits an "upward public reveal" filling loaded dry air, and that a `circusDistortion` client option disables refraction. None of it is in code. No class handles private collision or masks; the only Succession or Liberation block edit is the cobweb ruin; there is no `circusDistortion` config (the only distortion option is the Founding Fane's).
- **Consequence.** The playthrough probe CT-1 confirms the result: from all 1,888 standable floor cells of the real pavilion, none reaches the idle Ringmaster on his beam with the server's interact check, so his pact, acts, finale and Trade box need a click a floor-standing player cannot make (D-57).
- **Multiplayer.** What does exist is per viewer: `PacketSyncCircusPerception` sets the client's score and `CircusPerceptionOverlay` draws only for that client, so two players at different stages see different cues without disagreeing about real blocks. Private collision would be the hard part (per-player collision shapes need a mixin on the entity collision path and client prediction), which is probably why it was never built.
- **Evidence:**
  - `docs/CIRCUS.md:32`
  - `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md:114-129` ("IMPLEMENTED")
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusRingmasterEntity.java:369-376` (the only site edit)
  - `src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java:131`
  - `src/gameTest/java/com/vincenthuto/hemomancy/gametest/playthrough/PlaythroughCrimsonTroupeGameTests.java:936-967`
  - `docs/PLAYTHROUGH_TESTS.md:288` (D-57)
- **Confidence:** Verified.
- **Proposed fix.** Decide between (a) building real geometry the whole troupe shares: generated galleries and a perch stair that exist for everyone, with acclimation changing only how they look (the Circus's "shared perception becomes local reality", C-09, read at the site level), or (b) building the private geometry as designed, as one bounded system: a per-player cell mask synced by packet, a collision mixin that adds those cells' shapes for that player only, the same cells drawn by a world renderer, and GameTests for two players at different scores. Option (a) is far cheaper and fixes D-57 directly. Either way, correct `CIRCUS.md:32` and the archived contract now, and remove the `circusDistortion` sentence unless the option is added.
- **User's call:** yes (decision 2 below).

### W-5 · Medium · An all-column terrain gate makes pavilions very rare and the dead 5-block rule hides the real one

- **Claim.** `isSuitableTroupeFootprint` checks every column of the rotated bounding box: dry (surface equals ocean floor), below Y150, no terrain above the ground up to the box top, every ground height within 3 of every other and within 3 of the start. For the pavilion that is 4,225 columns, so the site needs a 65x65 patch of land that varies by at most three blocks. The one recorded natural search found the nearest pavilion 33,752 blocks from the player after 209 seconds of server-thread search. The camps and stages use the same structure type and the same gate over 625 columns.
- **Dead rule.** `CircusPavilionPlacementRules.MAX_SURFACE_VARIATION = 5` and `canPlacePavilion` are used only by their own test. The live limit is the literal `3` in `StructurePlacementChecks`. `CIRCUS.md:58` still quotes the five-block legacy rule.
- **Against the bar.** The Lighthouse Garden raises its own tide rock rather than wait for a natural coast (`07-seats.md`, "No coast requirement"); every outpost brings its own gully or stack. The pavilion waits for a perfect field.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/StructurePlacementChecks.java:179-201` (limit at `:198`, clearance at `:199-200`)
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionStructure.java:35-46`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionPlacementRules.java:4-15`, `src/test/java/.../circus/CircusPavilionPlacementRulesTest.java`
  - `docs/PELAGIC_WORLDGEN.md:150`
  - `src/main/resources/data/hemomancy/worldgen/structure/circus_pavilion.json` (`beard_thin`)
- **Confidence:** Verified for the code; the rarity rests on one locate in a Pelagic-heavy world, not a density sample.
- **Proposed fix.** Let the pavilion bring its own ground: a trodden earthwork ring and a timber stage platform in the template, `beard_box` adaptation (or a generator-built foundation skirt) so it can sit on 6 to 8 blocks of slope, and a gate that samples the footprint's corners, edge midpoints and a coarse grid rather than every column. Keep the dry and height checks. Delete `CircusPavilionPlacementRules` (or make it the live rule with one named constant). Then measure density in the new validation run (W-6).
- **User's call:** how rare a pavilion should be (decision 4).

### W-6 · Medium · No worldgen validation run, generator checks or review client for any Circus site

- **Claim.** The Circus has three kinds of structure test: a template-content GameTest (sizes, NPC roles, a few camp blocks), two noise-column regression tests for the footprint scan, and a pavilion locate-timing probe that runs inside `pelagic_worldgen_validation` behind a flag. Nothing generates a fresh seeded world and checks that pavilions, camps and stages appear, how often, on what ground, whether they overlap anything, or that their NPCs survive the first load. There is no generator-side `checks.py`, no preview tool for the pavilion (only `preview_teaching_camps.py`), and no review client; `CIRCUS.md` itself lists fresh-chunk placement, density and visual fit as unproven.
- **Against the bar.** The Court has `court_worldgen_validation` on four seed runs, `CourtSeatTemplateTest`, two review clients and a reviewer pass; the mausoleums have `MausoleumAssemblyGameTests` (four seeds by four Saints) and a review client with a natural mode.
- **Evidence:**
  - `gradle/game-tests.gradle:6`, `:12-13`, `:86-89`
  - `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CrimsonTroupeStructureGameTests.java:22-43`
  - `src/gameTest/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/TroupeFootprintGameTests.java:16-49`
  - `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PelagicWorldValidation.java:35-37`, `:52-81`
  - `docs/CIRCUS.md:55`, `:101`
- **Confidence:** Verified.
- **Proposed fix.** Add a `circus_worldgen_validation` namespace with its own `circusValidationServer` row in `gradle/game-tests.gradle`, modelled on `CourtWorldValidation`: seeds 42, 1337 and 8675309, forward and reverse; for each set's potential cells in a fixed radius, record accepted and refused sites with the reason, load the accepted ones, and assert the pieces placed, the NPCs and carousel exist, the floor is supported, nothing else overlaps the box and a village is not within the exclusion distance. Add a `circusSiteReviewClient` row (approach, big top, backyard, each camp, day and night). Add `checks.py` to the generator (forbidden blocks, every NPC spawn cell clear, every room lit, see W-10).
- **User's call:** no.

### W-7 · Medium · The Scarlet Waybill searches for structures on the server thread

- **Claim.** `CircusWaybillItem.findTarget` calls `level.findNearestMapStructure` with a 160-chunk radius; if the nearest pavilion is completed it calls it up to eight more times at 96 chunks from offset origins. It runs on right-click and from `inventoryTick` whenever the held Waybill has no target in this dimension. Each candidate cell runs the full footprint scan (W-5). The vanilla locate for the same structure took 209 seconds and footprint checks were 68.2% of server-thread samples. The Court's Relay Chart runs its search off the server thread through `CourtSeatSearch` for this reason.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/CircusWaybillItem.java:11-41` (`:29`, `:34-39`)
  - `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/CovenantWaybillItem.java:59-73`
  - `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/RelayChartItem.java:21`, `:33`
  - `docs/PELAGIC_WORLDGEN.md:150`
- **Confidence:** Verified for the code path; the Waybill's own pause was not measured.
- **Proposed fix.** Give the Circus a pure placement predicate like `CourtPlacementRules.stub` (biome source plus terrain sampler, no chunk loads) and search candidate cells with it off the server thread, as `RelayChartItem` does, recording the target later. Bound the search by rings.
- **User's call:** no.

### W-8 · Medium · Nothing keeps Circus sites, villages and each other apart

- **Claim.** The pavilion (48/16), the camps (24/8) and the abandoned stages (32/10) are three `random_spread` sets with no `exclusion_zone`, all on the same biome tag, all at `surface_structures`. A camp or stage can generate inside a pavilion's 65x65 box, and any of them can land in or beside a village (villages share most of these biomes). The footprint scan reads only terrain noise, never other structures. The Court computes the Circus's own potential cells to keep 96 blocks away from it, so the Court knows where the Circus might be but the Circus does not.
- **Evidence:**
  - `src/main/resources/data/hemomancy/worldgen/structure_set/circus_pavilion.json`, `troupe_teaching_camps.json`, `troupe_abandoned_stages.json`
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/circus_pavilion.json`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/court/CourtLandmarkCells.java:19-26`
- **Confidence:** Verified for the data; no overlap has been observed (there is no run that would show one).
- **Proposed fix.** Add `exclusion_zone` to the camps and stages against `hemomancy:circus_pavilion` (5 chunks clears the 65-block box) and to all three against `minecraft:villages`, or reuse `CourtLandmarkCells`' potential-cell math for a Circus landmark spacing. Check it in the W-6 run.
- **User's call:** no.

### W-9 · Medium · There is no discovery trail to a pavilion or camp

- **Claim.** Before D4 a player meets the Circus only by stumbling on a site. After D4 the Mnemonist's Scarlet Waybill points at the nearest camp or pavilion. There are no placed features (posters, painted stakes, discarded masks, wagon ruts), no travelling wagons or roadside stops, no Circus loot in other structures' chests, no map. The abandoned stage, the one small site, is pointed at by nothing (D-58). The Court has three converging discovery routes, six outposts and a capped sign feature in every Pelagic biome.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/CircusWaybillItem.java:13-30`
  - `src/main/resources/data/hemomancy/tags/worldgen/structure/circus_waybill_targets.json` (pavilion only)
  - `docs/PLAYTHROUGH_TESTS.md:289` (D-58)
  - `docs/BRINED_COURT.md` ("Discovery", "Placement")
- **Confidence:** Verified (no Circus placed feature, loot table or trail structure exists; searched `worldgen/`, `loot_table/chests/` and Java for wagon, poster, handbill, caravan).
- **Proposed fix.** Add a Circus sign tier as one capped placed feature, like `court_signs`: a handbill post, a painted tent stake, a dropped mask, a burnt-out ring of torches, each pointing with its facing toward the nearest pavilion cell, denser within a few hundred blocks. Make the abandoned stage a roadside stop that carries a Waybill-style handbill, and add it to the Waybill targets or to an NPC's directions (answers D-58). Optionally a wandering wagon (a small travelling camp structure or entity) in the same biomes.
- **User's call:** yes, on which trail pieces (decision 5).

### W-10 · Medium · The pavilion's rooms and arena rim are dark enough to spawn monsters

- **Claim.** Each of the six shell rooms is 13x11 or 19x13 under a solid wool roof with one vanilla lantern at its corner (`x+1, 3, z+1`). The far floor corner of a 13x11 room is 10 + 2 + 8 = 20 steps from the lantern, so block light there is 0, which is the Overworld's monster spawn limit; the 19x13 rooms are worse. The arena has two lanterns at y5 near its centre; the rim of the 15-block-radius disc is more than 15 steps from both. No event suppresses spawns in a pavilion. Camps are open-roofed and get skylight, so they are less affected.
- **Evidence:**
  - `tools/circus/troupe_templates.py:165-177` (one lantern per room)
  - `tools/circus/build_troupe_assets.py:82-83` (room sizes)
  - `src/main/java/com/vincenthuto/hemomancy/common/data/gen/CircusPavilionTemplate.java:79-80` (arena lanterns)
  - `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java:1157-1158` (Hematic Lantern copies vanilla lantern light)
- **Confidence:** Suspected (light arithmetic from the templates; not observed in a world).
- **Proposed fix.** Light the rebuilt site in the generator with Circus lamps (footlights, a chandelier ring, lamp-lit wagons) and add a `checks.py` rule that every standable interior cell has block light above 0.
- **User's call:** no.

### W-11 · Medium · The persistent site outcomes are six cobwebs, and Succession's reveal was never built

- **Claim.** Liberation's "persistent ruin" of the carousel and pavilion is six cobwebs placed into air at fixed offsets round the encounter origin. Succession's "upward public reveal" (filling loaded dry air so the galleries become real for everyone, saved per node) is described as implemented but has no code. So a settled pavilion looks the same after either ending, apart from six cobwebs.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/CircusRingmasterEntity.java:369-376`
  - `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md:127`
  - `docs/HEMOMANCY_REFERENCE.md:2850`
- **Confidence:** Verified.
- **Proposed fix.** Author both outcome states in the generator as alternate pieces or a stored "ruin" and "crowned" palette, and apply them through one service at completion (only loaded cells, saved progress, as the contract intended): Liberation collapses the big top's canvas, splinters the carousel and leaves the rigging hanging; Succession raises the galleries and the Ringmaster's colours. Edits outside a player's cast are exempt from `PlayerBlockEdits`, but the service should still skip non-air player blocks.
- **User's call:** yes, on what each ending leaves (decision 2 covers Succession's reveal).

### W-12 · Low · Perception cues are vanilla particles and flat quads borrowed from the Qliphoth

- **Claim.** The stage cues are `CRIMSON_SPORE`, `REVERSE_PORTAL` and `END_ROD` particles and vanilla sounds (`AMBIENT_CAVE`, `ENCHANTMENT_TABLE_USE`, `AMETHYST_BLOCK_CHIME`) around the player, a red screen-edge gradient, and silhouette, cloth and light planes drawn with `HemoRenderTypes.QLIPHOTH_GLOW` in rings round the nearest carousel within 32 blocks. The file is on the vanilla-particle allow-list pending the A-33 sweep. Nothing appears anywhere but round the carousel, so the 65x65 shell gets no cue at all. The overlay is properly per viewer and the subtle-overlay rule fits it, but it does not use the mod's custom particle or material toolkit and has no Circus material of its own.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/CircusPerceptionOverlay.java:44-77`, `:79-100`, `:102-147`
  - `src/test/resources/architecture/vanilla-particle-allow-list.txt:11`
- **Confidence:** Verified.
- **Proposed fix.** After the rebuild, anchor cues to authored marks in the template (gallery cells, rigging lines, the ring curb) rather than the carousel, and replace the particles with `HemoParticleData` motes and thread glints. Keep it subtle (it is an overlay).
- **User's call:** no.

### W-13 · Low · The pavilion places Harbinger Outpost emitters on a three-floor height rule

- **Claim.** `CircusPavilionStructure.afterPlace` calls `AbocipherEmitterPlacement.placeHarbingerOutpostEmitters`, which places three emitters with the `HARBINGER_OUTPOST` profile at heights split for a three-floor outpost. The pavilion is one storey, so the upper emitter is set well above the floor, wherever the first air cell is. It is placed from the centre chunk only, into neighbouring cells whose own template part may be placed later. The Circus has no emitter profile of its own.
- **Evidence:**
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionStructure.java:53-65`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/AbocipherEmitterPlacement.java:23-38`
  - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/AbocipherEmitterPlacementRules.java:10-20`
  - `src/test/java/.../circus/CircusPavilionWorldgenResourceTest.java:40-44` (pins the call)
- **Confidence:** Verified for the code; where emitters actually land was not observed.
- **Proposed fix.** Place emitters (or a Circus ambience block) as authored template cells in the generator and drop the `afterPlace` hook, or give the Circus its own profile and fixed positions.
- **User's call:** no.

### W-14 · Low · Camp yards are grass and coarse dirt, which later decoration can grow trees on

- **Claim.** Each camp writes grass block or coarse dirt over its whole 25x25 floor and air above it. Structures place at `surface_structures`, before vegetal decoration, and coarse dirt and grass are both in `#minecraft:dirt`, so tree and plant features in forest, dark forest and taiga camps can still grow in the yard and practice rings after the camp is placed. The pavilion's open ground between rooms is untouched natural surface with the same exposure.
- **Evidence:**
  - `tools/circus/build_teaching_camps.py:40-46`
  - `src/main/resources/data/hemomancy/worldgen/structure/troupe_camp_air.json` (`step: surface_structures`)
  - `src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/circus_pavilion.json` (dark forest, old-growth taigas)
- **Confidence:** Suspected (vanilla ordering; no world inspected).
- **Proposed fix.** Floor the yards with a path block, trodden mud or a Circus sawdust block that is not in `#minecraft:dirt`, and include a forest seed in the W-6 run.
- **User's call:** no.

### W-15 · Low · The abandoned stage is a bare deck that needs a perfect 25x25 site

- **Claim.** The stage is 337 blocks: a dark-oak plank and coarse-dirt deck, two log posts, a record and a mask, in a 25x9x25 box. It uses the pavilion structure type, so it runs the full 625-column flat-and-clear gate for a 19x17 deck, and it places no air, so grass and flowers stay through it. It is the only home of the Missing Understudy's stage record and nothing points to it (D-58).
- **Evidence:**
  - `tools/circus/build_troupe_assets.py:123-131`
  - `src/main/resources/data/hemomancy/worldgen/structure/troupe_abandoned_stage.json`
  - `docs/PLAYTHROUGH_TESTS.md:289`
- **Confidence:** Verified.
- **Proposed fix.** Fold it into the W-9 trail as a roadside wreck of a stage wagon, generated by the new kit, with a smaller box and a gate sized to it.
- **User's call:** covered by decision 5.

### W-16 · Low · Circus sites carry no loot

- **Claim.** No Circus chest loot table exists; the three barrels in the pavilion and the two per camp have no loot table set and generate empty. The Court ruled "loot sparse, gifts belong to NPCs"; the Circus has no such ruling, so the empty barrels are a gap rather than a choice.
- **Evidence:**
  - `src/main/resources/data/hemomancy/loot_table/chests/` (no circus or troupe table)
  - `src/main/resources/data/hemomancy/structure/crimson_troupe_pavilion.nbt` (no block entity NBT except the two Specimen Jars)
- **Confidence:** Verified.
- **Proposed fix.** Decide the rule (decision 6). If loot, add `chests/circus_backyard` (Enthralling Filament, masks, costume scraps, a handbill) and set it on authored containers in the generator; if not, replace the barrels with set dressing that cannot be opened.
- **User's call:** yes.

### W-17 · Low · Circus block models sit outside the model generator; texel density is fine

- **Claim.** Texel density holds at 16 px per block: masks and rigging map their 16x16 PIL-painted textures 1:1 through default UVs, the curtain is a 16x16 pane, the carousel's Java model uses a 256x256 texture at one pixel per unit. The convention gap is provenance. Only the Spindle has a `tools/model_export/` generator with `.bbmodel` and preview. The masks and rigging are JSON boxes written inline by `build_troupe_assets.py`; the four investigation records are `cube_all` of `minecraft:block/stripped_dark_oak_log` (cleanup C-11, still open); the carousel model is 49 hand-placed boxes in Java with no generator.
- **Evidence:**
  - `tools/circus/build_troupe_assets.py:37-62`
  - `src/main/resources/assets/hemomancy/models/block/circus_stage_record.json` (and `_loft_`, `_quarters_`, `impaled_procession_record.json`)
  - `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/CircusCarouselModel.java:38-88`
  - `tools/model_export/puppeteers_spindle.py`
  - `docs/audits/2026-11-02-cleanup/README.md:114` (C-11)
- **Confidence:** Verified.
- **Proposed fix.** Move the masks, rigging and records into `tools/model_export/` generators (a records generator can make each record a distinct object: a stage bill, a loft rigging log, a quarters ledger, a carousel maker's plate); regenerate the carousel from a generator when the pavilion is rebuilt.
- **User's call:** no.

### W-18 · Low · Circus worldgen docs describe a pipeline and systems that are gone or never existed

- **Claim.**
  - `CIRCUS.md:47` and `HEMOMANCY_REFERENCE.md:319`, `:370` name `CircusPavilionStructureProvider` as a live `runData` provider; it was deleted on 2026-11-03.
  - `CIRCUS.md:53` says the builder "never rewrites the legacy pavilion"; the builder needs the deleted legacy file to run at all (W-3).
  - `CIRCUS.md:58` quotes the 33x33 / 49x49 / five-block legacy numbers as current and omits the abandoned stages' 32/10 set.
  - `CIRCUS.md:32` and `03-...:114-129` describe private geometry, a public reveal and `circusDistortion` (W-4, W-11).
  - `CIRCUS.md:101`, `HEMOMANCY_REFERENCE.md:2854`, `:2862` and `wiki/World-Content.md:62` promise that existing sites keep legacy blocks, which no longer matters under the not-live rule.
  - AGENTS.md's tools list omits `tools/circus/` (W-3).
- **Confidence:** Verified.
- **Proposed fix.** Correct these with the rebuild; until then, fix the provider and private-geometry lines so nobody builds on them.
- **User's call:** no.

## Decisions the owner must make

1. **Rebuild or reskin.** Rebuild the pavilion and camps from scratch in a `tools/worldgen/circus/` generator to the Court and mausoleum bar (W-1, W-2, W-3), dropping the legacy-arena preservation rule; or keep the arena and only re-dress the shell. The audit recommends the rebuild: the arena is the weakest part and nothing has to be kept.
2. **Private or shared geometry.** Build the 500/1000 private galleries and canopy as designed (per-player collision, the expensive path), or make the galleries and the Ringmaster's perch real for everyone and let acclimation change only how they look (W-4, D-57, W-11). This also decides what Succession leaves behind.
3. **What the big top contains.** The set-piece list for the pavilion and each camp's apparatus (W-1, W-2): seating tiers, ring curb, king pole, rigging towers, wagons, booth, knife wheel, fire pit, costume trunks, mask walls, hanging Crossbars.
4. **How rare a pavilion is.** Keep 48/16 with a site that brings its own earthwork (W-5), or change spacing once the validation run (W-6) gives real density numbers. Same question for camps at 24/8 with seven professors.
5. **The trail.** Which discovery pieces the Circus gets (W-9): a capped sign feature, roadside stage wagons, a wandering wagon, handbills in other structures' loot, an NPC direction to the abandoned stage (D-58).
6. **Loot.** Whether Circus sites hold loot or, like the Court, leave gifts to the NPCs (W-16).
7. **Site outcomes.** What a Liberated and a Succeeded pavilion look like afterwards, beyond six cobwebs (W-11).
