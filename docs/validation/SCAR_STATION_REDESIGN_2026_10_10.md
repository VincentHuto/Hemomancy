# Cerebral Scarring Station redesign — 2026-10-10

The Cerebral Scarring Station now matches the other redesigned Harbinger stations. A script authors the baked model, and the renderer only lays the slot items out on it. Slots, the scar grid, recipes, access gates and the 1×2×1 footprint are unchanged.

## Design

- **Concept.** A Vein Mason's surgical lectern, where the grid writes into a mind. It replaces the converted desk with shelf clutter.
- **Plinth and cabinet.**
  - A polished venous-brick plinth stands on hematic iron corner cleats.
  - On it sits a blood-wood instrument cabinet in an iron frame. Its four drawers have bone pulls.
  - Blood from the carving bed runs down the centre stile into a bone basin on the plinth.
  - Bone-handled scalpels and probes hang from rails on both flanks.
- **Carving bed.**
  - An iron bed pitched 22.5° toward the mason holds the scar grid. The grid is an 8×8 bone lattice, one cell per `ScarRecipe` cell, over a flesh bed, with iron clamps at its corners.
  - At the mason's right hand are a bone catalyst cup and a forked bone rest for the knapper.
  - Stepped iron cheeks fill the wedge under the bed.
- **Backboard.**
  - Two hematic iron pillars carry a bone-framed blood-wood board. The pattern and the finished scar are pinned to it.
  - A bone-finialed lintel crowns the pillars. Iron straps cross the board's back.
- **Cortex.**
  - A preserved cortex sits under a glass bell on a bracketed shelf. Its fissure glows crimson.
  - Nerve cables run from its stem down both pillars into the bed. They are studded with full-bright synapses.
  - A surgeon's loupe hangs over the grid on an articulated iron arm off the east pillar.
- **Materials.** Hematic iron, bone, blood-wood, venous stone, bog flesh, hemorrhagic crust, nerve fibre and synaptic nodes. There is no copper, brass, silver or gold.
- **Particles.** The station spawns none, so there were no vanilla particles to sweep.

## Sources

- `tools/model_export/scar_station.py` is built on `station_kit`. It writes:
  - `scar_station.json`, the cutout body (162 elements with the glass);
  - `scar_station_glass.json`, the translucent bell and lens;
  - the item model, which carries both;
  - the two-layer facing blockstate;
  - `textures/block/scar_station_cortex.png`;
  - the Blockbench project.
- Run it with `--preview` to write `build/scar_station_preview.png`, with stand-in plates at the renderer's item anchors.
- The model is now authored north-facing, like the other scripted stations: north y0, east y90, south y180, west y270. The old converted model faced south.
- `ScarStationRenderer` replaced its per-facing hand-tuned transforms with one model-space layout, turned with the blockstate:
  - the blank lies on the grid, and the knapper lies across its rest; both are pitched with the bed;
  - the catalyst stands in its cup;
  - the pattern and the finished scar hang on the board.
  - It keeps the capped tabletop lighting and the `GROUND` display context, as `ScarStationRendererLightingResourceTest` requires. Its anchors mirror the generator's constants.
- `ScarStationBlock` outlines the plinth, cabinet, pitched bed, backboard and bell through `ModelShapes.cells`, across the controller and its filler. It previously used two full cubes.
- Retired: `textures/block/model_scar_station.png` and `textures/entity/model_scar_station.png`.

## Results

- **Export.** Deterministic: two runs gave identical hashes for the models and the texture. `coplanar.py` reports 0 fights on the body, on the glass, and on both together.
- **JVM.**
  - `compileJava`, `compileTestJava` and `compileGameTestJava` passed.
  - These contracts pass when run directly: `ModelFirstTileRendererResourceTest`, `ScarStationRendererLightingResourceTest`, `WaterloggableDecorativeBlocksTest`, `RegistryCreativeOrderSourceTest` and `CustomBlockItemGuiPoseSourceTest`.
  - `ModelFirstTileRendererResourceTest`'s scar station assertions were rewritten for the generator-authored model. They now cover:
    - the generator credit and the cutout/translucent split;
    - the 18-bar grid and the pitched bed;
    - the cortex, its texture, the glass bell, and the item carrying both layers;
    - the display transforms and the retired atlases;
    - the traced outline, the facing blockstate, and the renderer's facing and pitch transforms.
- **Native client.** Run `gradlew.bat -I build/scar-station-world.init.gradle runScarStationReviewClient`. `ScarStationClientReview` stocks every slot on four facings, plus one empty station. It captures `build/scar-station-client-review/screenshots/scar-station-0..9.png` by day and night.
  - The log shows no scar station model or texture errors.
  - All four facings turn correctly. The blank lies on the grid, the knapper on its rest, and the enzyme catalyst in its cup. The pattern and the finished scar hang on the board.
  - The fissure and synapses glow at night. The item renders in the hotbar.
  - **Fix from the first capture.** The loupe's drop rod and ring hung in front of the pinned scar. The arm now runs at y 17.9, outboard of the board, below the pinned items' sightline. The re-capture (`scar-station-1.png`) shows both pinned items clear.
- **Not run.** The full `test` task, the GameTest suite and `alphaCheck`.
