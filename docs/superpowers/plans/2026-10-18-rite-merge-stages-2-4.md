# Rite merge stages 2–4 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (the user's standing choice: native, one final reviewer). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** One purpose-grouped Liber map and JEI listing for every Harbinger rite and craft, three special floors for the special rites, and lasting rite spaces with tag-driven rite-bound blocks/items/entities.

**Architecture:** Purpose groups are authored once in `HarbingerRecipeMapDefinitions` and drive the merged Liber controller, the layout order and canvas tinting. JEI reuses `BloodStructureRecipeCategory` per purpose. Special floors are floor JSON plus a declared purpose, a palette and a shader motif. Rite spaces live in `common/rite/zone` beside the existing zones: an Overworld saved list written on rite completion and released by the Focus's floor check. Pure `*Rules` classes hold the geometry and admission logic.

**Tech Stack:** NeoForge 1.21.1, Java 21, JEI, GLSL core shaders, JUnit 5, NeoForge GameTests.

**Spec:** `docs/superpowers/specs/2026-10-18-rite-merge-stages-2-4-design.md`

## Global Constraints

- Harbinger only; Unstained tabs, rites and structures stay as they are.
- NeoForge payload networking (`CustomPacketPayload`, `TYPE`, `STREAM_CODEC`, registered in `PacketHandler`); no `SimpleChannel`.
- Custom Hemomancy particles (`HemoParticleData`), never vanilla `ParticleTypes`.
- Match each edited file's own indentation (tabs vs 4 spaces) and line endings.
- Core shader edits must compile on NVIDIA: avoid GLSL reserved words (`packed`, `input`, `output`, `filter`, `sample`…).
- Do not commit (user preference); leave work in the tree.

## Review Focus

- A Focus whose floor is upgraded to a higher tier of the same style: the space must release (its floor id changed), not silently keep a smaller cube. Pinned in Task 6's rules test (`releasesWhenFloorIdChanges`).
- Chunk unload must not release a space (only block removal or a floor change). Pinned by using `onRemove` with a state check, plus the source test in Task 6.
- A rite-bound block placed by a non-player (dispenser, structure) must not crash the placement event. Covered: the event ignores non-player placers (Task 7 rules test `nonPlayerPlacementIsIgnored`).
- Special floors must not be matched by `formedFloor` as ambiguous with a same-tier standard floor. Their block palettes differ from every standard grand floor; Task 4's data test asserts no two grand patterns are identical.
- The merged map must keep nodes at least 38 px apart on both layers. Task 2's layout data test lays out the real merged entry set.

---

### Task 1: Purpose groups in the recipe map definitions

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/RecipeMapPurposeGroup.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitionsTest.java`, new `RecipeMapPurposeGroupDataTest.java`

**Interfaces:**
- Produces: `enum RecipeMapPurposeGroup { FLOORLESS, SELF, WORLD, MAKING, BOND, REFERENCE }` with `String label()`, `int color()` (ARGB; SELF/WORLD/MAKING/BOND take `CardinalRiteFloorVisualRules.palette(styleIndex(purpose.floorStyle())).glow()`), `CardinalRitePurpose purpose()` (null for FLOORLESS/REFERENCE), `static RecipeMapPurposeGroup of(CardinalRitePurpose)`.
- Produces in `HarbingerRecipeMapDefinitions`: `List<String> FAMILIES` (purpose-major order from the spec table, then `RecipeMapLayout.MISC_FAMILY`), `RecipeMapPurposeGroup group(String family)`, `String family(RecipeMapEntry.Kind kind, String path)`, `int order(RecipeMapEntry.Kind, String path)`, `String floorFamily(CardinalRitePurpose)` (`"Threshold Floors"` etc.), `List<RecipeMapLink> links()` (rite + crafting links). The old `RITE_FAMILIES`/`CRAFTING_FAMILIES`, `riteFamily`/`craftingFamily` are replaced; `ritePaths()`, `craftingPaths()`, `puppetryPaths()`, icons and positions stay.

- [ ] Update `HarbingerRecipeMapDefinitionsTest` to the new families: Bloom → `Qliphoth`, Ancestral Communion → `Ancestral`, living_staff → `Floorless Crafts`, hematic_stake → `Floorless Crafts`, Succession/Restoration → `Fane`, Exsanguination → `Vessel`, counts unchanged (52 rites, 28 crafts, 4 puppetry); `group("Fane") == WORLD`, `group("Floorless Crafts") == FLOORLESS`.
- [ ] Write `RecipeMapPurposeGroupDataTest.everyRiteSitsInItsPurposeGroup`: read every non-Unstained `recipe/cardinal_rite/**.json`, assert `group(family(kind, path)).purpose()` equals the JSON `purpose`; Working crafts (under `working/`) are MAKING; every `blood_structure/*.json` Harbinger recipe is in `Floorless Crafts`.
- [ ] Run `./gradlew.bat test --tests "*RecipeMap*"` → FAIL.
- [ ] Implement the enum and the re-authored family table (keep the `// <recipe-map-editor>` markers).
- [ ] Run the tests → PASS.

### Task 2: Merged Rites & Crafts tab

**Files:**
- Create: `.../skilltree/shared/RitesAndCraftsTabController.java`
- Modify: `RecipeMapLayout.java` (group-major family order), `RecipeMapCanvas.java` (group tint, group arcs, group filter, inspector layout by selected kind, hub label ctor arg), `CraftingTabController.java` (Unstained only), `RitesTabController.java` (Unstained only; keep `firstUseDegrees`), `harbinger/HarbingerProgressScreen.java` (one `RITES("Rites & Crafts", 0xFF8844CC, 0, false)` tab, `CRAFTING` removed), `RecipeMapTooltip.java`.
- Test: `RecipeMapLayoutDataTest` (merged map), `BottomRightTabLayoutTest`, `RecipeMapTooltipTest`, `PuppeteerIntegrationSourceTest` (the strings it pins).

**Interfaces:**
- Consumes: Task 1 definitions.
- Produces: `RecipeMapLayout.build(List<RecipeMapEntry>, List<String> families, Map<RecipeMapKey,AuthoredPosition>, Function<String,Integer> groupRank)`; families sort by `groupRank`, then earliest degree, then authored index. The old 2/3-arg overloads keep a constant rank.
- Produces: `new RecipeMapCanvas(String hubLabel, Function<String, RecipeMapPurposeGroup> groups)`; the old `Kind` ctor maps to `("Rites"|"Craft", null)`.
- Produces: `static String RitesAndCraftsTabController.craftTooltip(CraftingMapRecipe)` and the moved `tooltipDescription(int, String, String)`.

- [ ] Change `RecipeMapLayoutDataTest` to build the merged map: rites (RITE), floorless + working crafts (CRAFTING), floors (FLOOR in `floorFamily(purpose of style)`), sigils; assert spacing and no MISC family. Change `BottomRightTabLayoutTest` to expect `["Skills","Tendencies","Rites & Crafts","Materials"]`.
- [ ] Run the tests → FAIL.
- [ ] Implement the layout rank, canvas changes, the merged controller (rite/floor/sigil selections use `RitesTabView.drawMapInspector` with a `RitesTabState`; CRAFTING selections use `CraftingTabView.drawMapInspector` with a `CraftingTabState`; layer buttons/drag/scroll route to the state of the selected kind), and the screen tab change. Strip the Harbinger branches from the two old controllers.
- [ ] Run `./gradlew.bat test --tests "*skilltree*"` and `--tests "*Puppeteer*"` → PASS.

### Task 3: JEI per-purpose categories

**Files:**
- Modify: `compat/jei/BloodStructureRecipeCategory.java`, `compat/jei/JEIPlugin.java`, `client/screen/skilltree/shared/CraftingMapRecipe.java`, `assets/hemomancy/lang/en_us.json`
- Test: new `src/test/java/com/vincenthuto/hemomancy/compat/jei/JeiPurposeCategorySourceTest.java`

**Interfaces:**
- Produces: `BloodStructureRecipeCategory(IGuiHelper, CardinalRitePurpose purpose)` (null = floorless); `JEIPlugin.riteTypeFor(CardinalRitePurpose)` returning `threshold_rite`, `dominion_rite`, `working_craft` (kept id), `communion_rite` recipe types.
- `CraftingMapRecipe.of(CardinalRiteRecipe)` shows subject, else required structure, else floor.
- Lang keys: `hemomancy.jei.threshold_rite`, `hemomancy.jei.dominion_rite`, `hemomancy.jei.working_craft` (reworded "Working Rites & Crafts"), `hemomancy.jei.communion_rite`, `hemomancy.jei.craft.steps.self|world|bond`.

- [ ] Source test: JEIPlugin registers a category per `CardinalRitePurpose` and adds recipes filtered by `getPurpose() == purpose && !isUnstained()`, excluding `SuccessionRites.isRecipe` from WORLD; the lang file has every new key.
- [ ] Run → FAIL; implement; run → PASS.

### Task 4: Special floors (data + validation)

**Files:**
- Create: `data/hemomancy/cardinal_rite_floor/apotheos_grand.json`, `qliphoth_grand.json`, `ancestral_grand.json`
- Modify: `common/rite/floor/CardinalRiteFloorDefinition.java` (+`CardinalRitePurpose purpose`, `boolean special()`), `CardinalRiteFloorLoader.java` (parse/require `purpose`), `CardinalRitePurpose.java` (`static CardinalRitePurpose ofFloorStyle(String)`), the three rite JSONs' `floor`, `CardinalRiteFloorRegistry` sync snapshot if it re-parses.
- Test: new `src/test/java/com/vincenthuto/hemomancy/common/rite/floor/SpecialFloorDataTest.java`; existing `CardinalRitePurposeDataTest` and floor listing tests.

**Interfaces:**
- Produces: `CardinalRiteFloorDefinition.purpose()`, `special()`; `CardinalRitePurpose.ofFloorStyle(String)` (null for non-purpose styles).

- [ ] `SpecialFloorDataTest`: the three floors exist, are grand, declare purposes self/world/bond; apotheos_rite→apotheos_grand, bloom_of_qliphoth→qliphoth_grand, ancestral_communion→ancestral_grand; every rite naming a non-purpose style is `special` and its purpose equals the floor's; every special floor declares a purpose; no two grand floor patterns+keys are identical; each pattern is 9×9 with one centred Focus.
- [ ] Run → FAIL; author floors (patterns per spec), loader/definition changes, rite JSON floors; run → PASS (plus `CardinalRitePurposeDataTest`, `CardinalRiteFloor*Test`).

### Task 5: Special floor visuals

**Files:**
- Modify: `common/rite/floor/CardinalRiteFloorVisualRules.java` (STYLE_APOTHEOS=4, STYLE_QLIPHOTH=5, STYLE_ANCESTRAL=6, palettes, `styleIndex` cases), `assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh` (motif/motifLine for 4–6, per-style colour accents in main, Apotheos muted beat, Qliphoth wider edge bulge)
- Test: `CardinalRiteFloorVisualRulesTest`

- [ ] Test: `styleIndex("apotheos")==4`, `"qliphoth"==5`, `"ancestral"==6`; palettes distinct from the four standard ones; unknown style still 0.
- [ ] Run → FAIL; implement rules; run → PASS.
- [ ] Shader: add the three motifs; validate by launching `./gradlew.bat runSuccessionReviewClient` until `Sound engine started` with no shader error in the log.

### Task 6: Rite spaces (state, lifecycle, sync, visuals)

**Files:**
- Create in `common/rite/zone/`: `RiteSpace.java` (record + `contains(BlockPos, ResourceKey<Level>)`), `RiteSpaceRules.java` (pure geometry: `covers(focus, half, pos)`, `shouldRelease(spaceFloor, formedFloor)`), `RiteSpaceSavedData.java` (`hemomancy_rite_spaces`), `RiteSpaces.java` (service: `consecrate(ServerLevel, ActiveCardinalRite, CardinalRiteRecipe)`, `onFloorCheck(ServerLevel, BlockPos, ResourceLocation formed)`, `release(ServerLevel, BlockPos)`, `spaceAt(Level, BlockPos)`), `RiteSpaceSyncPacket.java`
- Create client: `client/render/world/zone/RiteSpaceClientData.java`, `RiteSpaceMotes.java` (client tick boundary motes)
- Modify: `CardinalRiteTermination.end` (consecrate on COMPLETED Harbinger with a matched floor), `CardinalFocusBlockEntity.serverTick` (call `onFloorCheck`), `CardinalFocusBlock.onRemove` (release), `RiteZoneEvents` (sync rite spaces on login/dimension/respawn and when changed), `PacketHandler` (register packet)
- Test: new `src/test/java/com/vincenthuto/hemomancy/common/rite/zone/RiteSpaceRulesTest.java`, `RiteSpaceSourceTest.java`

**Interfaces:**
- Produces: `record RiteSpace(BlockPos focus, ResourceKey<Level> dimension, UUID owner, CardinalRitePurpose purpose, ResourceLocation floor, int half, ResourceLocation rite, long createdTick)`; `RiteSpaces.spaceAt(Level level, BlockPos pos) -> Optional<RiteSpace>` working on both sides (client reads `RiteSpaceClientData`).

- [ ] `RiteSpaceRulesTest`: covers x/z ±half and y 1..2·half above the Focus, not y 0 or 2·half+1; `releasesWhenFloorIdChanges` (same style, higher tier → release), null formed → release, same id → keep.
- [ ] `RiteSpaceSourceTest`: termination consecrates only on `Outcome.COMPLETED`; `onRemove` releases only when `!state.is(newState.getBlock())`; the Focus BE does not release in `setRemoved`.
- [ ] Run → FAIL; implement; run → PASS.

### Task 7: Rite-bound tags and enforcement

**Files:**
- Create: `common/rite/zone/RiteBoundRules.java` (pure), `common/rite/zone/RiteBoundEvents.java`, `client/event/RiteBoundTooltips.java`, tags `data/hemomancy/tags/{block,item,entity_type}/rite_bound/{self,world,making,bond,any}.json` (block `world` lists `hemomancy:sanguine_omen`, item `world` lists it too), lang lines
- Create GameTest: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/RiteSpaceGameTests.java`; register `'rite_space_validation'` in `gradle/game-tests.gradle`
- Modify: `common/network/PlaceStructurePacket.java` (`placeFloor` visible to tests)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/zone/RiteBoundRulesTest.java`

**Interfaces:**
- Produces: `RiteBoundRules.admits(Set<CardinalRitePurpose> required, boolean any, CardinalRitePurpose spacePurpose)`; `RiteBoundRules.requirement(...)` describing `"a Dominion rite space"`; unravel grace `UNRAVEL_GRACE_TICKS = 100`.

- [ ] `RiteBoundRulesTest`: untagged → always admitted; `world` admitted only by WORLD; `any` by every purpose; nothing admitted without a space; `nonPlayerPlacementIsIgnored`.
- [ ] Run → FAIL; implement rules, events, tooltips, tags; run → PASS.
- [ ] GameTests: (a) build `working_minor` with `placeFloor`, consecrate via `RiteSpaces.consecrate` on a completed rite, assert `spaceAt` inside/outside, break a floor block, wait 40 ticks, assert released; (b) the Sanguine Omen placement event is cancelled outside and allowed inside. Run `./gradlew.bat riteSpaceGameTestServer` → all pass.

### Task 8: Docs and full verification

**Files:** `docs/HEMOMANCY_REFERENCE.md` (§5.3 floors/special floors/rite spaces, §25.2 JEI/Liber), `docs/LORE_REFERENCE.md` (three floors), `AGENTS.md` (contract line), `wiki/*.md` mentioning the Rites/Crafting tabs or these rites' floors, `docs/TESTING.md` (new suite), `docs/CARDINAL_RITE_AUDIT.md` status.

- [ ] Grep docs/wiki/dialogue/lang for "Crafting tab", "Rites tab", and the three rites' old floors; update.
- [ ] Run `./gradlew.bat test` and compare failures against the pre-existing baseline; run `./gradlew.bat riteSpaceGameTestServer stationUpgradeGameTestServer`.
- [ ] Launch `runSuccessionReviewClient` to confirm shader compile; open the Liber Rites & Crafts tab if feasible.
- [ ] One fresh final reviewer over the whole diff.
