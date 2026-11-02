# Track 5 — Adipocere in vanilla dungeons, strongholds and ancient cities

**Goal:** corpse wax is found where bodies rotted in the dark: rarely on the walls of vanilla dungeons (monster rooms), strongholds and ancient cities. Bog bodies stay a vivianite source first.

## Current state
- `adipocere` (item) comes from an axe scrape on an unharvested bog body (`BogBodyBlock.scrapeWax`), a 35% bog-revenant drop and a Ghastly Alembic distillation (honeycomb + Ruinous Enzyme).
- Nothing places it in the world.

## Target
- A wall block, **`adipocere_crust`** (`facing`, wall-mounted, no collision, cutout model authored by a `tools/model_export/` generator: a greasy pale wax drip with a darker rind, 16 px/block, with a `potted`-free item-less registration). Right-click with an empty hand or any tool scrapes it: the block is removed and drops 1 adipocere (2 with an axe, with the axe-scrape sound and a `HemoParticleData` wax puff; never vanilla `SCRAPE` particles). It does not regrow.
- **Where.** Rarely (target: a couple per stronghold, zero or one per monster room, a handful across an ancient city) on interior wall faces of: `minecraft:monster_room` (the vanilla dungeon feature), `minecraft:stronghold`, `minecraft:ancient_city`. Not in the mausoleums (they have their own wax source later) and not on exterior faces.
- **How.** Vanilla strongholds and dungeons are code-generated pieces, not template pools, so a jigsaw processor cannot reach them. Use a one-time **structure decoration pass**: on `ChunkEvent.Load` (first load only, tracked in a small `SavedData` set of decorated chunks keyed by structure), for each listed structure whose pieces touch the chunk (`StructureManager.getStructureWithPieceAt` / `startsForStructure`), scan the piece's interior for air cells adjacent to a solid wall block of the piece's palette and place crusts at the rarity above with a seed derived from the chunk and structure start, so the result is deterministic per world. Ancient cities are template-pool structures; the same pass covers them (a template processor is possible there but one path is simpler to keep).
- **Rules.** `AdipocereCrustRules` (pure, tested): candidate face test (interior air, solid wall, not floor or ceiling), per-structure density, seeded selection; a cap per chunk so a pass can never carpet a wall.
- **Teaching.** The Liber material entry for Adipocere (`material.hemomancy.adipocere.desc`, add it if missing) names all four sources; the Vicar's wax line if one exists.

## Tasks
1. Block + generator + model + lang + fixtures; scrape interaction; a loot-free drop through the interaction (or a loot table with the axe condition, matching `fungal_gill`'s pattern).
2. `AdipocereCrustRules` + tests.
3. The decoration pass and its saved data; a config value for density in `HemoServerConfig`.
4. GameTest: a generated stronghold piece in a test world gets crusts on walls only (assemble a stronghold piece through the vanilla `StrongholdPieces` in the test, run the pass, count).
5. Proof: screenshots in a stronghold corridor and an ancient city hall.

## Acceptance
- Crusts appear on walls in the three structures and nowhere else; never on floors or ceilings; never twice in the same chunk after reload.
- Scraping drops wax once with the right particles and sound.
- World load cost is one pass per chunk per structure, recorded, never repeated.

## Status (2026-10-29)
Built, uncommitted.
- **Block:** `adipocere_crust` (`AdipocereCrustBlock`, `facing`, no collision); model, atlas, blockstate and Blockbench project from `tools/model_export/adipocere_crust.py`. A scrape by hand or with any `#c:tools` item gives 1 adipocere, an axe gives 2 (axe-scrape sound, one durability), with a `HemoParticleData` blood-cell puff in wax colours. Breaking it drops 1 (loot table; now the crust itself, see the follow-up). Liber material entry `material.hemomancy.adipocere.desc` names all four sources. No Vicar wax line exists, so nothing was added there.
- **Rules:** `AdipocereCrustRules` + `AdipocereCrustRulesTest`. Expected crusts per site: monster room 0.5, stronghold chunk 0.04, ancient city chunk 0.05; one per site, two per chunk at most; `adipocere_crust.density` in the server config (0 to 10).
- **Pass:** `AdipocereCrustDecoration` + `AdipocereCrustSavedData`. Changes from the plan: (1) the monster room is a feature, not a structure, so `StructureManager` cannot find it; it is found by a spawner standing on cobblestone or mossy cobblestone, with a 3-cell interior around it. (2) Chunk load only queues the chunk. The pass runs on the level tick, four chunks a tick, and reads and writes only cells and walls inside the chunk, so the result does not depend on load order. (3) Only chunks that held a site are recorded. "Interior" means inside a structure piece's box (or the room), under a roof within 8 blocks, on `#hemomancy:adipocere_crust_walls` masonry that carries on above or below.
- **Tests:** `adipocere_crust_validation` (alpha) GameTests: a real `StrongholdPieces.Straight` corridor buried in stone gets crusts on walls only, a monster room takes one crust, a recorded chunk is never done twice, and hand/axe scrapes give 1/2.
- **Proof:** `adipocereCrustReviewClient` (`AdipocereCrustClientReview`; create `WaxReview`, seed 4242, through the operator inbox) flies to the nearest stronghold and ancient city at density 10, checks every crust is on masonry, and captures one in each (`build/adipocere-crust-review/screenshots`). First run: 38 crusts in 56 stronghold chunks and 53 in 133 city chunks, 0 misplaced (before the stronghold rate went from 0.07 to 0.04).

**Follow-up (2026-10-29):** the crust now has an ordinary block item (four adipocere in a 2x2, recipe `adipocere_crust`), places on the clicked wall like a wall torch and drops itself when broken; scraping still gives wax. Rates raised about fivefold: monster room 2 (cap 3), stronghold chunk 0.35, ancient city chunk 0.3 (cap 2 each), four per chunk.
