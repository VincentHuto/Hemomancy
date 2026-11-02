# Track 4 — The sarcophagus and the chamber

**Goal:** the tomb is the size of a Saint lying down, and the chamber around it is the grandest room in the complex: the place the story started, built to hold a boss fight.

## Current state
- `SaintSarcophagusBlock` is a 1×3×2 multiblock (`BASE_FILLER_OFFSETS`: one block wide, three long, two tall) with a BER (`SaintSarcophagusRenderer`: lid, blood, coals, veins parts) and a generator at `tools/model_export/saint_sarcophagus.py` that also writes `SarcophagusGeometry.java` (collision cells, lid pivot, pool, cressets).
- In the screenshot the chest is a slender 1-block-wide box on a 3×3 ice dais in an 11×11 room with four kneeling armor stands.
- Structure placement grows the fillers lazily on the first tick (`FillersPlaced`).

## Target
- **Model.** A tomb that reads as player-sized: footprint **2 wide × 4 long**, 1.5–2 tall on a stepped plinth, so a figure 1.8 blocks long lies in it with room for the pall and the grave goods. Keep the authored details (strapped corners, arcaded vein-tree panels, witnessing eye, bone plaque and offering gutter, gisant on the lid, corpus with the vial) and re-proportion them to the new box; the lid slide-and-tip animation, the bowl, the cressets and the blood pool all stay, re-anchored. Texel density stays 16 px/block, which means the atlas grows with the model.
- **Multiblock.** Fillers for a 2×4×2 footprint (offsets rotate per facing through `HorizontalFacingRotationHelper`, as now). `SarcophagusGeometry` collision cells regenerate from the model. The lazy-filler path keeps working for structure placement.
- **Chamber.** A great room per Saint (theme tracks dress it): at least 19 wide, 25 long, 11 tall, with a raised dais of three steps at the far end under a vault, side aisles behind pillars, and the Saint's signature set piece built into the architecture (Track 6 blocks). The ring door opens onto a short processional down the long axis so the first view is the tomb framed by the pillars. Boss-fight room: no hazards in the floor the fight cannot use, and a clear 15×15 at the centre.

## Tasks
1. `saint_sarcophagus.py`: re-proportion to 2×4; regenerate the models, atlas, `.bbmodel`, previews and `SarcophagusGeometry.java`. Keep the per-Saint texture variants if the generator has them; otherwise add a `--saint` palette switch (theme tracks fill in colours).
2. `SaintSarcophagusBlock`: new filler offsets and shape; `placeFillers` / `removeFillers` / `restoreDormant` handle the larger footprint; `SaintEncounterGameTests` and `structurePlacedSarcophagusGrowsItsFillers` update to the new filler count.
3. BER: re-anchor lid, pool and cressets from the regenerated geometry; check the open/closed states and the consecrated flames in a client.
4. Generator (`pieces.py` after Track 3): a `chamber()` builder at the new scale with a dais, aisles, vault and set-piece sockets the theme tracks fill.
5. Boss rise: `HarbingerSaintEncounterHooks.spawnSaintBoss` moves the Saint to the tomb's centre; confirm the rise scene (`SCENE_SAINT_RISE`) still frames correctly from the processional.

## Acceptance
- Standing beside the tomb, it is longer than the player is tall and clearly a coffin for a person.
- The lid animation, bowl, cressets and veins render correctly in all four facings.
- The chamber screenshot from Track 0 shows the dais, aisles and vault, with the set-piece sockets empty until a theme track fills them.
