# Discussion Bug Batch Implementation Plan

> **Required subskill:** Use `superpowers:executing-plans` to implement this plan task by task.

**Goal:** Fix the nine reported discussion bugs at their shared causes while preserving current gameplay contracts, authored structures, save data, and unrelated working-tree changes.

**Architecture:** Keep the fixes at the narrowest existing ownership points: recipe JSON for structure geometry, manipulation lifecycle hooks for transient cooldowns, the scratch-engram event for click routing, each machine block entity for its output/feedback, and biome feature lists for worldgen ordering. Add one focused regression check per non-trivial rule; do not introduce new services, registries, or configuration.

**Tech Stack:** Java 21, NeoForge 1.21.1 events and GameTests, JUnit 5/source-contract tests, Minecraft data JSON, Gradle.

**Spec:** Findings from the nine-item discussion bug list supplied on 2026-08-18.

**Status:** Implemented. Fourteen focused regression tests pass; `compileJava` and
`compileGameTestJava` pass; an isolated dedicated-server world reached `Done`
without a placed-feature order cycle. The live-client checks listed below remain
manual verification boundaries.

## Protected boundaries

- Work in the current checkout and preserve all unrelated dirty changes.
- Do not stage, commit, reset, clean, or create a worktree as part of this plan.
- Keep registry IDs, recipe IDs, save keys, networking, progression, and authored assets unchanged.
- Format blood and strain only at display boundaries; do not round stored values or calculations.
- Focused JVM checks do not replace live client checks for interaction routing, particles, or cross-world cooldown lifecycle.
- Transient state may survive dimension changes inside one server when that is intentional, but no static timestamp or client world cache may leak into a different server/world session.

## Findings summary

| # | Report | Finding | Confidence |
|---|---|---|---|
| 1 | Alembic hit block lacks venous stone | Confirmed data defect affecting five blood-structure recipes. Their `hitBlock` block never appears in the pattern. | Source-confirmed |
| 2 | Absorption/projection stuck on first unlock | The universal cooldown map is static, keyed only by player UUID, and stores absolute world game time. A later integrated-server world can inherit a far-future expiry. Other time-bearing static runtime collections have the same session-ownership flaw. | Source-confirmed lifecycle flaw; live reproduction still required |
| 3 | Vivianite click creates engrams on machines | The global scratch-engram right-click event consumes eligible tool clicks before blocks such as the Somatic Loom can insert the item. | Source-confirmed |
| 4 | Loom does not explain missing enzyme | Recipe readiness correctly requires enzymes, but catalyst insertion gives no deficit message; empty-hand feedback uses raw tendency enum names and only appears later. | Source-confirmed feedback gap |
| 5 | Centrifuge emits befouling ash | Auxiliary output slot 18 is hard-coded to `befouling_ash_trail` instead of `hematic_iron_powder`. | Source-confirmed |
| 6 | Armature particles look flipped | East/west particle offsets rotate opposite to the renderer's east/west item transforms. North/south agree. | Source-confirmed coordinate mismatch |
| 7 | Blood/strain text shows floating-point noise | Raw `float`/`double` values reach the dimension message, radial blood display, and Muscle Memory tincture tooltip. | Source-confirmed |
| 8 | Fungal Spine travel causes feature cycle | Fungal Gardens has one order, while the other destination biomes use conflicting orders for the same three placed features, forming a cycle. The existing test checks only one biome. | Source-confirmed; server startup validation required |
| 9 | Qliphoth trees flash in new worlds | Logout clears Qliphoth renderer buffers but not `QliphothBloomClientData`, so old Bloom coordinates remain renderable until the new world's sync replaces them. | Source-confirmed client-session leak |

## Task 1: Enforce reachable blood-structure hit blocks

**Files:**

- Modify `src/main/resources/data/hemomancy/recipe/blood_structure/ghastly_alembic.json`
- Modify `src/main/resources/data/hemomancy/recipe/blood_structure/mind_spike.json`
- Modify `src/main/resources/data/hemomancy/recipe/blood_structure/mnemonic_reliquary.json`
- Modify `src/main/resources/data/hemomancy/recipe/blood_structure/runic_chisel_station.json`
- Modify `src/main/resources/data/hemomancy/recipe/blood_structure/visceral_mirror.json`
- Create `src/test/java/com/vincenthuto/hemomancy/common/recipe/BloodStructureHitBlockTest.java`

### Finding

Both `BloodStructureCraftingHelper.findStructurePatternAtHit` and `BloodCraftingKeyPressPacket` first require the clicked block state to match `recipe.hitBlock()`, then require the matched pattern to contain that clicked position. The following recipes therefore cannot be activated:

| Recipe | Required hit block | Present in pattern |
|---|---|---|
| `ghastly_alembic` | `hemomancy:venous_stone` | No |
| `mind_spike` | `hemomancy:polished_venous_stone` | No |
| `mnemonic_reliquary` | `hemomancy:venous_stone` | No |
| `runic_chisel_station` | `hemomancy:polished_venous_stone` | No |
| `visceral_mirror` | `hemomancy:sanguine_glass` | No |

### Implementation

1. Write a resource test that loads every JSON file under `data/hemomancy/recipe/blood_structure`, collects the symbols actually used by `pattern`, resolves their `key` entries, and asserts that at least one used symbol names the recipe's `hitBlock`.
2. Run the test and confirm it reports exactly the five recipes above.
3. Add one dedicated `H` symbol to each recipe and replace one existing central pattern cell without changing dimensions:
   - `ghastly_alembic`: `H = venous_stone`; change the center `V` in `GVG` to `H`.
   - `mind_spike`: `H = polished_venous_stone`; change the center `P` in `SPS` to `H`.
   - `mnemonic_reliquary`: `H = venous_stone`; change the center `V` in `TVT` to `H`.
   - `runic_chisel_station`: `H = polished_venous_stone`; change the center cell of one `SSS` row to `H`.
   - `visceral_mirror`: `H = sanguine_glass`; change the center `G` in `PGP` to `H`.
4. Run the test again; it must enumerate every structure recipe and pass.
5. In a live client, build at least the Ghastly Alembic and one other corrected structure, hit the new `H` block, and verify normal crafting begins.

## Task 2: Scope every transient game-time cache to one server session

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java`
- Modify `src/main/java/com/vincenthuto/hemomancy/common/manipulation/stillarts/StillArt.java`
- Modify the other time-bearing static runtime owners found by the audit, including `SporiticThuribleResonanceState`, `BloodwoodGrowthHandler`, `EquippedMorphlingEvents`, `ChummedWatersAreaManager`, `BlackVeilCovenantManager`, `TemporaryIceManager`, `SanguineFormationProjectionHandler`, `BloodStructureFeedManager`, `HematicSalvageEvents`, `RootedStateHelper`, and `ConserveStateHelper`
- Create `src/main/java/com/vincenthuto/hemomancy/common/event/TransientServerStateEvents.java`
- Create `src/test/java/com/vincenthuto/hemomancy/common/event/TransientServerStateLifecycleSourceTest.java`

### Finding

Absorption and Projection are granted as degree-one utilities with ordinary 40-tick cooldowns. Their shared `BloodManipulation.UNIVERSAL_COOLDOWN_MAP` is static, keyed only by UUID, and records `level.getGameTime() + cooldown`. Returning to the title screen and loading a lower-game-time world with the same profile leaves the old expiry active, so newly granted utilities can appear stuck until the new world's clock catches up.

This is not unique to the two starter manipulations. `StillArt` has the same static UUID-to-expiry design, and the source audit found other static runtime collections whose expiry/last-update values are derived from a level clock. Examples include thurible resonance, Bloodwood growth progress, temporary morphling webs, Chummed Waters, Black Veil zones, temporary ice, formation/feed progress, salvage throttling, and fungal scar caches. Dimension keys do not distinguish two different saves that both contain `minecraft:overworld`. Persisted capability or SavedData timestamps are separate: they belong to their save and must retain their intended relog/dimension semantics.

### Implementation

1. Inventory every mutable static collection whose entries contain or compare `Level.getGameTime()` values. Classify each as immutable definition data, save-owned persistent data, or transient runtime state; only the last category belongs in this cleanup.
2. Give each transient owner a minimal `clear` method if it does not already have one.
3. Add one `TransientServerStateEvents` subscriber that clears the full transient inventory on both `ServerStoppedEvent` and `ServerAboutToStartEvent`. Startup clearing is the defensive guarantee if shutdown was abnormal.
4. Clear entire server-session collections, not only the current player UUID. Keep legitimate cooldowns across relog and dimension changes within the same running server.
5. Do not clear cooldowns inside `grantDegreeOneUtilities`; re-granting knowledge must not become a cooldown bypass.
6. Add one source-contract test that asserts both lifecycle events invoke every inventoried transient owner, plus the smallest pure reset checks for the manipulation and Still Art cooldown maps.
7. Live reproduction:
   - In World A, advance game time and use a manipulation.
   - Return to title and load a fresh/lower-time World B using the same local profile.
   - Unlock degree-one utilities through the Hermit/Sanguine Initiation path.
   - Verify Absorption and Projection are immediately usable, then observe their normal 40-tick cooldown.
   - Repeat with one non-manipulation transient effect, such as thurible resonance, to prove the fix is session-wide.

## Task 3: Reserve normal tool clicks for block interaction

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/event/ScratchEngramHandler.java`
- Modify or create the closest scratch-engram interaction test under `src/test/java/com/vincenthuto/hemomancy/common/event/`

### Finding

`ScratchEngramHandler` handles `RightClickBlock` globally for vivianite clusters and the other engraving tools. When the clicked face is sturdy and the adjacent space is empty, it places an engram and cancels the event before the target block receives its item interaction. `SomaticLoomBlock.handleItemUse` already accepts vivianite as a catalyst, but never receives that normal click.

### Implementation

1. Require the player to be crouching before `ScratchEngramHandler` performs scratch engraving.
2. Leave normal right-click uncancelled so machine blocks can process insertion through their existing handlers.
3. Keep crouch-right-click as the deliberate engraving gesture for vivianite, flint, quartz, and obsidian flakes; do not add a Somatic Loom-specific blacklist.
4. Add one interaction-rule test covering both branches: normal right-click does not engrave/cancel; crouch-right-click remains eligible.
5. Live-check vivianite insertion on the Somatic Loom and at least one other item-accepting block, then confirm crouch-right-click still creates a scratch engram on a valid ordinary face.

## Task 4: Report the loom's exact missing enzyme

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/SomaticLoomBlockEntity.java`
- Extend `src/test/java/com/vincenthuto/hemomancy/common/recipe/MemoryWeavingRedesignSourceTest.java` or the nearest runnable loom test

### Finding

`refreshRecipe` intentionally sets `curRecipe` only after both catalysts and all required enzymes are present. Catalyst insertion succeeds silently when enzymes are missing. Empty-hand `provideTendencyFeedback` can find the catalyst-matched recipe, but reports only the first deficit using `EnumBloodTendency.name()` rather than the registered enzyme item name. The issue is feedback, not recipe decoding or consumption.

### Implementation

1. Reuse the existing tendency-to-enzyme item mapping already used by loom inventory handling; do not add a second registry.
2. After the second catalyst is placed and a catalyst-matched recipe exists, report the first missing requirement with its item display name, deficit, and stored/required count, for example: `Missing 1 Frigid Enzyme (0/1 stored).`
3. Use the same message from the existing empty-hand feedback path so insertion and later inspection cannot disagree.
4. Do not change `curRecipe` readiness, enzyme capacity, consumption, or ritual start rules.
5. Add a focused test for one absent enzyme, one partially supplied multi-count enzyme, and no warning once requirements are met.
6. Live-check a memory recipe that requires an enzyme not currently stored.

## Task 5: Correct the vial centrifuge auxiliary product

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/VialCentrifugeBlockEntity.java`
- Extend `src/test/java/com/vincenthuto/hemomancy/common/tile/crafting/VialCentrifugeStartupRulesTest.java` or create the adjacent output test

### Finding

`VialCentrifugeBlockEntity.outputResults` hard-codes auxiliary slot 18 to `BlockInit.befouling_ash_trail` for both empty-slot insertion and stack matching. The intended registry entry is `ItemInit.hematic_iron_powder` (display name: Hematic Iron Powder).

### Implementation

1. Replace both auxiliary-output references with `ItemInit.hematic_iron_powder`; retain the current chance, slot, and stack-limit behavior.
2. Add one focused runnable check that fails if the centrifuge auxiliary output references befouling ash or stacks against any item other than hematic iron powder.
3. Run the centrifuge test and a live batch; confirm the main vial result is unchanged and only slot 18's auxiliary product changes.

## Task 6: Align Hematic Armature particles with rendered bowls

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/HematicArmatureBlockEntity.java`
- Create or extend a pure armature offset test

### Finding

The renderer maps east to 90 degrees and west to 270 degrees. `rotateBowlOffset` applies the opposite sign for those two facings, so server particle positions mirror the rendered reagent positions east/west. South and north already agree.

### Implementation

1. Keep the authored south-facing bowl offsets unchanged.
2. Change only the east/west cases in `rotateBowlOffset` to match the renderer:
   - east: `(z, y, -x)`
   - west: `(-z, y, x)`
   - north remains `(-x, y, -z)`
   - south remains `(x, y, z)`
3. Expose the existing rotation method only as far as needed for a package-level pure test; do not add a new rotation utility for one caller.
4. Test all four facings with a non-symmetric bowl offset so mirrored results cannot pass accidentally.
5. Live-check reagent and ritual particles for all four horizontal facings. JVM math passing is not renderer parity evidence.

## Task 7: Bound player-facing blood and strain formatting

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java`
- Modify `src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java`
- Modify `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MuscleMemoryTinctureItem.java`
- Extend the nearest focused formatting tests

### Finding

Raw numeric concatenation currently leaks floating-point tails from:

- the blood-volume dimension-change message;
- the radial screen's self/gourd blood text;
- `MuscleMemoryTinctureItem.trimCost`, whose integer check falls through to `Double.toString` for widened float values such as `0.30000001192092896`.

`RadialChooseManipScreen.trimNumber` already demonstrates the required bounded-decimal approach, and `BloodGourdItem` already rounds display amounts. Stored blood and strain values do not need a type or persistence change.

### Implementation

1. Format blood-volume displays as whole millilitres, matching the existing HUD convention.
2. Format strain/cost to at most two decimal places using `Locale.ROOT`, then trim trailing zeroes and the decimal point.
3. Replace the radial center concatenation with labelled, bounded values for self and gourd blood; keep the current layout footprint.
4. Keep the formatter local to the existing display classes unless the exact same code remains duplicated three times after the edits; no new formatting subsystem.
5. Add focused cases for `0.30000001192092896 -> 0.3`, an integer value, and a blood value just below an integer boundary.
6. Live-check the dimension message, tincture tooltip, and radial screen at non-integer values.

## Task 8: Remove the fungal placed-feature order cycle

**Files:**

- Modify `src/main/resources/data/hemomancy/worldgen/biome/fungal_gardens.json`
- Modify `src/main/resources/data/hemomancy/worldgen/biome/drifting_mycelium.json`
- Modify `src/main/resources/data/hemomancy/worldgen/biome/hyphal_spires.json`
- Modify `src/main/resources/data/hemomancy/worldgen/biome/sporecrown_thicket.json`
- Modify `src/main/resources/data/hemomancy/worldgen/biome/fungal_isles.json`
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java`
- Extend `src/test/java/com/vincenthuto/hemomancy/common/worldgen/FungalGardensFeatureOrderTest.java`

### Finding

The fungal dimension biome source includes multiple biomes that share placed features but disagree on relative order:

- `fungal_gardens`: `bleeding_hearts`, `patch_hyphae`, `small_infected_fungus`
- `drifting_mycelium`, `hyphal_spires`, `sporecrown_thicket`: `patch_hyphae`, `small_infected_fungus`, `bleeding_hearts`
- `fungal_isles`: `small_infected_fungus`, `bleeding_hearts`

Together these impose contradictory edges and produce a `Feature order cycle found` error when the Fungal Spine loads the destination. `BiomeInit` also has inconsistent relative order. The existing `FungalGardensFeatureOrderTest` checks only `fungal_gardens.json`, so it can pass while the dimension-wide graph is cyclic.

### Implementation

1. Choose one canonical relative order for every fungal biome: `bleeding_hearts`, then `patch_hyphae`, then `small_infected_fungus`. A biome may omit a feature but must preserve the relative order of those it contains.
2. Reorder only the affected feature entries in all five biome JSON files; do not add features to biomes that did not already contain them.
3. Apply the same relative order to every corresponding biome builder in `BiomeInit`.
4. Expand `FungalGardensFeatureOrderTest` to:
   - load every biome named by `dimension/fungal_gardens.json`;
   - collect ordering constraints for shared placed features within each decoration step;
   - assert the resulting constraint graph is acyclic;
   - assert the three known shared features follow the canonical order when present.
5. Run the focused test and then `gradlew.bat runServer --no-daemon`. Create/load the fungal dimension and verify registry/feature sorting completes without a cycle.
6. In a live client, use the Fungal Spine to enter the Fungal Gardens and generate terrain across more than one destination biome.

## Task 9: Clear Qliphoth Bloom client data between worlds

**Files:**

- Modify `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Create `src/test/java/com/vincenthuto/hemomancy/client/data/QliphothBloomClientDataTest.java`

### Finding

`QliphothBloomClientData` is a static list of server-synchronized Bloom positions. `QliphothBloomRenderer.render` reads that list every frame. The existing client logout handler clears `QliphothBloomRenderer` GPU caches but never calls `QliphothBloomClientData.clear()`. Consequently, the previous world's tree can render at its old coordinates during the interval before the new world's `PacketSyncQliphothBlooms` arrives. The cached bark VBOs are stage geometry and are safe to reuse; the stale world-position list is not.

### Implementation

1. Call `QliphothBloomClientData.clear()` from the existing `ClientPlayerNetworkEvent.LoggingOut` handler alongside the other world-owned client caches.
2. Also clear it at client login before accepting the new session's sync, providing a defensive clean boundary after an abnormal disconnect.
3. Keep `QliphothBloomRenderer.clearCaches()` for GPU-resource lifecycle, but do not rebuild or disable the reusable stage meshes to solve a data-ownership bug.
4. Add a focused client-data test: seed one Bloom entry, execute `clear`, and assert both `getActiveBlooms()` and `containsCenter()` are empty/false. Add a source assertion that the disconnect boundary calls that clear method.
5. Live-check: load a world containing a visible Qliphoth tree, return to title, create/load a completely new world at comparable coordinates, and verify no old tree appears during initial frames before synchronization.

## Final verification

Run focused checks first, then the normal compilation boundary:

```powershell
.\gradlew.bat test --tests '*BloodStructureHitBlockTest' --tests '*FungalGardensFeatureOrderTest' --tests '*TransientServerState*' --tests '*QliphothBloomClientDataTest' --tests '*Armature*' --tests '*Loom*' --tests '*Centrifuge*' --no-daemon --rerun-tasks
.\gradlew.bat compileJava compileGameTestJava --no-daemon --rerun-tasks
.\gradlew.bat runServer --no-daemon
```

Report separately:

- focused regression results for these nine fixes;
- unrelated existing suite/GameTest failures;
- live checks completed for structure activation, server-session cleanup, Qliphoth cross-world rendering, item insertion, loom feedback, centrifuge output, particles, UI text, and Fungal Spine travel;
- any live checks still outstanding.
