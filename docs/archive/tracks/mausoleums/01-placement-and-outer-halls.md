# Track 1 — Placement depth and the outer halls

**Goal:** complexes sit on the ground, not one block into it, and the D3–4 outer halls no longer use the offering-gate trial.

## Issue 0: one block too deep
Every complex's entrance floor lands one block below grass level (screenshot: the paved walk and the stone pylons sit in a dirt-rimmed pit).

Where the number comes from:
- `data/hemomancy/worldgen/structure/mausoleum_<saint>.json`: `start_height` absolute 1, `project_start_to_heightmap` WORLD_SURFACE_WG, `terrain_adaptation` beard_thin.
- `HemoJigsawStructure.findGenerationPoint` passes that to `JigsawPlacement.addPieces`, which moves the start piece so `boundingBox.minY + groundLevelDelta` (1 for a rigid piece) lands at `start_height + firstFreeHeight`.
- The start jigsaw in `structure/mausoleum/<saint>/top.nbt` sits at template y=1 (copied from the original `top.nbt`, where it was `[0, 1, 8]`, orientation `up_south`).

Tasks:
1. Measure, don't guess: spawn a complex (Track 0 or `structure_spawner`) on flat ground and note which template row ends up at the grass row.
2. Fix at one place only. Prefer the structure JSON (`start_height` absolute 2) if the offset is uniform; move the start jigsaw in the generator's copy step if the top piece itself is wrong. Do not touch the inner pieces.
3. Add a JVM test (`MausoleumComplexResourceTest`) that pins the start jigsaw's template y and the structure JSON's start height together, with a comment explaining the arithmetic, so the next change to either breaks loudly.
4. Check `beard_thin` on a slope: the top piece should sit in the hillside with its floor level, not carve a pit. If it still digs, try `beard_box` or `bury` on the top piece only (vanilla allows one adaptation per structure, so this may mean a second structure for the entrance; note the trade-off in the ledger).

## Issue 1: drop the offering gate
The outer trial hall (`trialroom` copies: two `blood_trial_altar`, one `offering_gate`, five `blood_basin`, two `blood_pylon`) is a 2024-era trial and no longer the bar. The chain currently runs top → stairs → antechamber → trialroom → seal → nave.

Tasks:
1. Remove `trialroom` from `COPIED` in `tools/worldgen/mausoleum_complexes.py`; re-point the antechamber's forward jigsaw at the next piece (today `seal`; after Track 3, the first descent). Delete the four `trialroom.nbt` copies and pools.
2. Replace the outer halls with **crypt galleries**: two or three generated gallery pieces (Track 7's kit) lined with sealed niches, a few opened ones with `chests/mausoleum` barrels, inscriptions (`DiscoveryInscriptionPlacement`, as `MausoleumStructure.afterPlace` already does) and a single `chests/mausoleum_treasure` barrel behind an ordinary iron-door room. No combat trial here for now; the D3–4 challenge is a later design.
3. Keep `blood_trial_altar`, `offering_gate`, `blood_basin` and `blood_pylon` registered (other content may use them); only the mausoleum stops placing them.
4. Update `MausoleumComplexResourceTest` (it asserts the antechamber barrels and the treasure barrel) and HEMOMANCY_REFERENCE §5.6g's chain description.

## Acceptance
- Spawned on flat grass, the entrance floor is level with the grass.
- No complex contains an offering gate or trial altar.
- The assembly GameTest (Track 0) still places every mandatory piece.
