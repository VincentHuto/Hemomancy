# Phlebotomist's Cabinet validation

## Verification result (2026-09-15)

- `./gradlew.bat assemble`: passed; cabinet code/resources are present in the release jar, with development GameTests excluded.
- Dedicated suite: **38 required tests passed**, including **15 cabinet tests** and the existing blood-vial/microscope cases.
- Full JVM suite: **2,035 passed, 2 failed**. The failures are outside cabinet code: `LegacyMainTestSuiteContractTest` expects 361 legacy tests but discovers 360; `MorphlingLumenlaceRenameResourceTest` fails reading the existing non-UTF-8 `docs/phlegethontic-nether-worldgen/orcadian-horseman/README.md`. Neither file was changed for the cabinet.
- Cabinet resource JSON, mesh bounds and embedded editable-model textures validated. `git diff --check` passed.
- Interactive client and real-network checks below remain unverified.

## Automated coverage

Run `./gradlew.bat runBloodInjectionGameTestServer`. The shared blood-sample suite includes `PhlebotomistsCabinetGameTests` alongside the existing vial and microscope tests.

Cabinet cases exercise:

- Exact full-component identity, separate identification, capacity, matching-first insertion, defensive copies, simulation without notifications, zero and bounded extraction.
- Missing entity sources, rejected new malformed/partial inputs, bounded counts, surviving entries beside wholly undecodable data, persistence round trips.
- Left/right pickup, shift movement, refused swap/clone/throw/double-collect/quick-craft, ordinary player slots, stale active menus, full cursor/cell/inventory, two viewers, and one-count outputs.
- Real upper/lower hopper transfers while a menu observes the cabinet; comparator totals 0, 1, 288, 575 and 576; representative vial snapshots unchanged by count-only mutations, with separate synchronized counts for world inspection.
- Multiple viewers, death/menu replacement and reload closure; glazing preserves facing/open/contents, refuses unrelated tools, returns one pane with a full inventory, and has explicit creative behavior.
- The actual `ServerPlayerGameMode.useItemOn` path for sneaking Knapper/vial interactions, and actual survival/creative `destroyBlock` behavior.
- Explosion destruction, piston movement refusal, Wither/dragon destruction eligibility, empty loot with glaze-preserving placement, and pick-block item serialization without specimen data.

The renderer's meshes are reproducible with `python tools/model_export/phlebotomists_cabinet.py`. It reuses existing authored textures and embeds those plus vanilla textures in the editable Blockbench source when the Minecraft resource jar is available.

## Remaining live checks

These require an interactive client and are not established by dedicated-server GameTests:

1. Place cabinets facing north, east, south and west. Check the body/hinge alignment and smooth roughly 100-degree swing.
2. Compare solid and glazed cabinets while open, closed, and closing. Samples must remain visible until an opaque door finishes closing. Inspect glass depth and a glint/custom-name vial from several angles.
3. Use small GUI scales and long translated source names. Check counts, full hover tooltips, tendency colors and the question-mark treatment. A press/release must perform exactly one cell transaction even when a server cursor update arrives before release.
4. Open the same cabinet from two real clients while a hopper transfers; disconnect/reconnect one player, walk out of range, die, replace the menu and unload/reload the chunk. Confirm server quantities and door closure.
5. Attempt survival mining while occupied and inspect client prediction; empty it and recover a glazed cabinet item. Verify no mass item spill from creative removal or ordinary explosions.

No promise is made for forced removal by commands or arbitrary other mods. No unrelated runtime world or save is needed for this checklist.
