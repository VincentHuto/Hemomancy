# Track 7: The two Seats

**Status:** DONE 2026-10-30 (unattended run). The Lighthouse Garden (tide rock, lighthouse with a living Sunburst lamp, terraces, arbors, guest house, landing, and the tube-coliseum undercroft down to Marimo's pool and the petrified followers) and the Vent Sanctum (processional ring, two dry halls of preparation, Hydra's body, and runtime crowns, basins and threshold walkway on the terrain's unchanged chimneys) are jigsaw structures from `tools/worldgen/brined_court/`, with seven generated set-piece blocks; template validation, `court_worldgen_validation` (seeds 42 forward and reverse, 1337, 8675309) and `brinedSeatReviewClient` pass. Reviewer pass done 2026-10-30 (no defects found). Left: Track 9 residents on the marks, Track 10 presences, and the open call that the Garden raises its own tide rock instead of requiring a natural coast (see "Calls made unattended").

Phase B. Depends on Track 6 (placement predicate, structure set). Spec sections: "Three tiers of presence: Seats", the Giving Light and Kindling house texts, "Marimo and Hydra". Quality bar: Seats are set pieces, built like the mausoleum rework (modelled multiblocks, custom set-piece blocks, detailed walls), not palette-swapped villages.

## Goal

Two rare, memorable settlements at the ends of the gradient. The Lighthouse Garden is the Court's welcoming face and the D5 destination. The Vent Sanctum is its far end, built on a real vent field whose chimneys the Kindling read as Hydra's heads.

## Shared approach

- **Authoring:** jigsaw structures (`HemoJigsawStructure` precedent) with `.nbt` templates built by Python builders, the way `tools/circus/build_*.py` and the mausoleum generator work. Put the builders under `tools/worldgen/brined_court/` and add that path to the version-controlled list in `AGENTS.md` (the rest of `tools/` stays local).
- **Set-piece blocks** from `tools/model_export/` generators (one `.py` each, `.bbmodel` + preview PNG, 16 px per block). No hand-written JSON models.
- **Materials:** existing Pelagic materials (Erythrocoral, Hematic Algal Crust, basalt, blackstone, deepslate, magma, mineral apron blocks) plus a small new palette per Seat. Hematic iron, bone, lacquer, cloth, stone and grown tissue. No copper, brass or bronze, no modern diving tech.
- **Lighting:** the Court's light is biological. Sunburst crust and blooms glow at the Garden; vent glow and mineral heat at the Sanctum. No torches or lanterns.
- **Water and air:** both Seats mix flooded and dry rooms. Air pockets must survive generation (waterlogging and fluid ticks at template edges); test that they stay dry after a reload.
- **Residents** come from Track 9; until then, mark their spots with data markers the spawner reads.
- **Loot:** sparse. Food, cultivation supplies and one-time gifts belong to NPCs (Track 9 and 11), not chests. No chest holds a strain.

## The Lighthouse Garden (the Giving Light)

**Site:** a coast where Rockpool Shore or dry land meets an Erythrocoral Reef shelf. The footprint spans dry land, the tide line and the shallow reef.

**Parts (PROPOSED):**

| Part | What it is |
| --- | --- |
| The lighthouse | A tall tower on the rocks. Its lamp is a living Sunburst bloom in a glass-and-bone lantern room, glowing gold at night: the Court's beacon, visible from the sea. |
| Terraced gardens | Stepped beds running from the tower down into the reef, half above water, half below, planted with Sunburst crust, algae beds and floating blooms. Communal tables and drying racks. |
| The arbors | Anchored members among the fronds: living arbors where people are still recognisable. They are residents (Track 9), set in an anchored pose with set-piece frond growth around them. Anchored, not petrified: one of them can be seen withdrawing. |
| Guest house | Where newcomers are fed and housed: the "people have sincere reasons to join" beat. Beds, a hearth of bloom-light, a table with food. |
| The undercroft (SETTLED 2026-10-29) | Your original Marimo habitat: a tube-like coliseum or catacomb below the lighthouse, half above the waterline so it stays damp but not drowned, descending through moss-covered tiers. The deeper the tier, the more moss than person the anchored members are; at the bottom, Marimo's presence (Track 10). Petrified followers line the lowest walls, the only place the player sees true petrification. |
| Signs at the edge | Floating Sunburst blooms on the surface around the site (Track 8's sign feature, denser here). |

**Visibility:** the only Court structure visible from the surface besides the Tide Refuge. The lighthouse is the landmark.

## The Vent Sanctum (the Kindling)

**Site:** a seeded Hydrothermal vent field: a raised mineral mound about 58×40 blocks with 1–5 chimneys (`PELAGIC_WORLDGEN.md`). The Sanctum is built around the chimneys, which stay where the terrain put them.

**Parts (PROPOSED):**

| Part | What it is |
| --- | --- |
| The heads | The existing chimneys, crowned with set-piece growth so they read as Hydra's heads: a pale polyp ring and flame-coloured tentacle crowns rippling in the vent water (colour and motion only, nothing burns). |
| Offering ledges | Carved ledges and basins at each chimney's foot where members feed offerings in. Players can offer too (Track 11 decides what that does). |
| The threshold | One chimney, the largest, with a walkway to its mouth: where a member at the end of life enters to surrender. A set piece, not a mechanic the player triggers. |
| Halls of preparation | Low halls cut into the mound's flank, where members cultivate themselves for a lifetime. Kindled elders (mature Hydra-form members) rest here. |
| The body | Below or beside the mound, a mass of grown tissue that members who didn't keep a head became. Ties to Track 10's Hydra presence. |

**Visibility:** never from the surface. Hydrothermal depth; reached by deep diving.

## Pre-D5 visits (SETTLED 2026-10-29, W4)

Both Seats can be reached and entered at any degree. Before D5, residents are polite and don't teach (Track 9 dialogue); the Garden still feeds a visitor. The Sanctum is less welcoming but not hostile.

## Review client

Add `brinedSeatReviewClient` (driver `BrinedSeatClientReview`): places each Seat on a seeded natural site, flies a fixed camera path (approach from the surface or the depths, every named part, the undercroft descent, the threshold), by day and night, without night vision for the "unaided" read and with it for geometry. Screenshots to `build/brined-seat-review/screenshots`.

## Tests

- Template validation: every jigsaw pool resolves, no template places a forbidden block (copper family, torches, lanterns), air pockets stay dry after reload.
- Placement: Track 6's `court_worldgen_validation` covers sites; add "Vent Sanctum chimneys unchanged by the Sanctum" (count and positions match the terrain's).
- Registry and creative goldens for new blocks.

## Docs

A Brined Court section in `docs/HEMOMANCY_REFERENCE.md` (where the Seats are, what's there). `docs/LORE_REFERENCE.md`: the Seats, anchoring versus petrification as seen at the Garden, the vents as Hydra's heads. Wiki page for the Court.

## Open questions

- Not blocking: final names for the two Seats (the spec's are working names).
- Not blocking, Track 10/11: is the threshold chimney ever usable by the player, and what happens to someone who enters (spec "Still open" 11)?

## What landed (2026-10-30)

- **Generator:** `tools/worldgen/brined_court_seats.py` runs `tools/worldgen/brined_court/` (`kit.py`, the sea-aware canvas Track 8 can share; `garden.py`; `sanctum.py`; `checks.py`; `build.py`; `preview.py` for a quick top view and cut-aways in `build/brined-seat-preview/`). Both paths are tracked (`.gitignore`, `AGENTS.md`).
- **Templates and pools:** `structure/brined_court/lighthouse_garden/{garden,undercroft}.nbt`, `structure/brined_court/vent_sanctum/{sanctum,body}.nbt`; pools `start_pool`, `undercroft`, `body`; both structure JSONs name `start_jigsaw_name: hemomancy:court_seat_start` and `size: 2`. Every element sets `override_liquid_settings: ignore_waterlogging`.
- **Set-piece blocks** (structure-only, itemless, no drops; generators in `tools/model_export/` with `court_kit.py` shared materials): `sunburst_crust`, `sunburst_lamp`, `sunburst_frond`, `petrified_follower` (`moss=partial|whole`), `hydra_crown` (animated), `vent_offering_basin`, `kindled_tissue` (animated). Classes `CourtFixtureBlock`, `CourtFacingFixtureBlock`, `PetrifiedFollowerBlock` in `common/block/harbinger/decoration/`.
- **Java:** `CourtSeatLayout` (start position per site; the Garden is pinned to Y62), `VentSanctumHeads` (crowns, basins, threshold walkway from the terrain's chimneys), `CourtStructure.afterPlace` (writes the heads into water only), `CourtResidentMarkers` (reads resident/mark jigsaws for Tracks 9 and 10), `PelagicLandmarks.Chimney` / `chimneys(...)` (the vent plan's chimneys, exposed; terrain output unchanged).
- **Residents (Track 9):** jigsaw marks `hemomancy:court_resident/<role>`: lamp_keeper, gardener (2), guest_host, arbor_anchored (3), arbor_withdrawing, undercroft_tender, undercroft_anchored, undercroft_anchored_deep (3), kindled_elder (8), kindled_preparer (2). Presences (Track 10): `hemomancy:court_mark/marimo_presence` (the undercroft's moss island), `hemomancy:court_mark/hydra_body`. The threshold chimney is `VentSanctumHeads.threshold(...)`.
- **Tests:** `CourtSeatTemplateTest` (8), `VentSanctumHeadsTest` (4); `court_worldgen_validation` gained `CourtSeatValidation` (dry pockets now, no queued fluid tick beside them, dry after a save/read-back of the chunk; Garden lamps, undercroft piece and followers; Sanctum chimneys unchanged block for block and crowned). Outposts keep the stub check.
- **Review:** `brinedSeatReviewClient` (`BrinedSeatClientReview`, `-PbrinedSeatSeed`, default 42).
- **Docs:** HR §29 "The two Seats", `LORE_REFERENCE.md` "Brined Court Seats", `wiki/Brined-Court.md`.

## Calls made unattended

- **No coast requirement.** Reef shelves meeting land are rare at Seat-cell resolution, and adding the check would change Track 6's validated sites. The Garden instead raises its own tide rock and is pinned to the sea surface (`CourtSeatLayout.GARDEN_WATERLINE_Y`), its footing reaching 24 blocks down so it stands on any shelf from about Y38 up. Revisit if the user wants true shoreline Gardens.
- **Water handling:** templates ignore waterlogging and carry their own water; open sea is structure void. A dry cell may have water below it (moon pools, Marimo's pool) but never beside or above it.
- **Resident marks are jigsaw blocks**, not data structure blocks: SinglePoolElement ignores structure blocks, which would leave a hole of world water in a dry room; a jigsaw becomes air or water.
- **Undercroft entry** is a hatch under a frond-roofed kiosk east of the tower, not through the tower (the newel stair needed the tower floor solid). About eight blocks of the coliseum stand above the waterline and twelve below.
- **Sanctum heads at runtime:** crowns, basins and the threshold walkway depend on where the terrain put the chimneys, so `afterPlace` adds them (water cells only). The template is void within 12.5 blocks of the centre at all heights.
- **Threshold** = the tallest chimney (first of equals); its walkway runs straight from the ring's inner edge, three wide, rising to the mouth level.
- **The halls' dry rooms** are entered by a flooded antechamber and a moon pool (no airlock exists in vanilla).
- **Dry-check in the world** asserts fluid-free, not empty: decoration may grow grass in an open-air cell at the waterline row.
- **Hydra Crown** reaches past its cell (y up to 26 px), as the Kindling's crowns trail; its collision is the ring only.
- **Lighting:** sea pickles, glow lichen, glow-berry vines and the new glowing set pieces. No magma inside the dry halls (it burns whoever walks there); vent heat is the chimneys' own magma mouths and the basins' glowing salts.

## Verification (2026-10-30)

- `python tools/worldgen/brined_court_seats.py`: all four templates pass `checks.py` (no forbidden block, no floodable air).
- `./gradlew.bat test`: 4,738 JVM tests, 0 failures (before the last Sanctum tweak); after it, `test --tests "*court*" --tests "*PelagicLandmarksTest"` passes (includes `CourtSeatTemplateTest` 8, `VentSanctumHeadsTest` 4, `CourtStructureResourceTest`, `CourtPlacementRulesTest`).
- `./gradlew.bat runRegistrationGameTestServer`: all 47 pass (registry golden: seven blocks and two template pools; itemless set updated in `RegistrationGameTests`).
- `./gradlew.bat runCourtValidationServer -PcourtSeed=<seed> -PcourtRun=t7-<order> -PcourtOrder=<order>`: pass on 42 forward and reverse, 1337 forward, 8675309 forward; final re-run on 42 (`t7-final`) after the last template change passes. Seed 42: Garden at 3432,42,5096 (2 pieces, 3,448 air cells under the sea dry and dry after read-back, 6 petrified followers, 3 lamps); Sanctum at 3445,-46,-1427 (2 chimneys, 83 chimney voxels unchanged, 2 crowns, 424 dry cells). Reviewer (2026-10-30) added 1337 reverse and 8675309 reverse (`-PcourtRun=t7r-reverse`): both pass.
- `./gradlew.bat runBrinedSeatReviewClient`: 72 shots (15 Garden and 9 Sanctum stops, by day, by night and by night with night vision) in `build/brined-seat-review/screenshots/`. Reviewed by eye: the lighthouse reads from the sea by day and night, the undercroft descent and pool read with night vision; a few close stops start inside a wall or post (camera tuning, not geometry).
- Reviewer re-run (2026-10-30): generator output is byte-identical across runs (decompressed NBT); `./gradlew.bat test` 4,738 tests, 0 failures, on the final tree; `runRegistrationGameTestServer` 47/47.
- Not run: the full `alphaCheck`.
