# Sanguine Condenser Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace bare-block Sanguine Formation projection with a cauldron-born Sanguine Condenser that fills from Blood Projection, crystallizes formations passively, and upgrades in place as the seventh station.

**Architecture:**
- `hemomancy:sanguine_condenser` is one block with `FACING` + `StationTierProperty.STAGE` and a block entity that is an `IBloodReservoir` and an `UpgradeableStation`.
- Blood Projection converts an empty vanilla cauldron into stage 0 through an explicit branch in `BloodProjectionItem.performProjection`, placed after rite and structure handling and before infusion. The old `SanguineFormationProjectionHandler` stack is deleted.
- Pure numbers live in `SanguineCondenserRules`. Station wiring reuses `StationUpgradeCatalog` and the existing rites, escrow and dialogue.

**Tech Stack:** NeoForge 1.21.1 (21.1.251), Java 21, HutosLib, JUnit 5, NeoForge GameTests.

**Spec:** `docs/superpowers/specs/2026-10-20-sanguine-condenser-design.md`

## Global Constraints

- **Imports and IDs:** NeoForge imports only (`net.neoforged.*`), `DeferredHolder`, and `Hemomancy.rloc("snake_case")`.
- **Player state:** read and write player state through `HemoCapabilityAccess` only. Gate on `volume.isActive()`.
- **Resource paths:** singular 1.21 paths: `data/hemomancy/recipe/`, `data/hemomancy/loot_table/`.
- **Particles:** no vanilla particle types in new code. Use the `HemoParticleData` / HutosLib toolkit and read `docs/MANIPULATION_VISUALS.md` before Task 6.
- **Materials:** no copper, brass, bronze or silver on the models. Use hematic iron, bone and crimson lacquer at 16 px/block texel density.
- **Station upgrades:** follow `docs/STATION_UPGRADES.md`. Keep `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest` and `RecipeMapPurposeGroupDataTest` green.
- **Numbers by stage:**

  | Stage | Pool (mL) | Cost per formation (mL) | Ticks per formation | Holds | Collapse | Bonus |
  |---|---|---|---|---|---|---|
  | 0 | 300 | 150 | 200 | 1 | `max(0, 0.25 − 0.05·lvl)` | 0 |
  | 1 | 750 | 125 | 120 | 4 | 0 | 0 |
  | 2 | 1500 | 100 | 60 | 8 | 0 | 0.25 |

- **Upgrade tiers:**
  - Tier 1: D3, `working_lesser`, kit `clotting_lattice`, rite `cardinal_rite/first_clotting`, usage `condense`, 2 × 50 mL.
  - Tier 2: D5, `working_greater`, kit `druse_heart`, rite `cardinal_rite/druse_crystallization`, usage `condense_latticed`, 3 × 50 mL.
  - Seat distance is 1 and the structure pattern is 3×3 `[" S "]`, as for the Alembic and Athanor.
- **Display names:** stage 0 "Sanguine Cauldron", stage 1 "Latticed Condenser", stage 2 "Druse Condenser". Kits are "Clotting Lattice" and "Druse Heart".
- **Version control:** leave all work uncommitted in the working tree. The user commits.
- **Builds:** parallel sessions share build dirs. A mid-run `NoClassDefFoundError` is usually a build collision, so re-run before debugging.

## Review Focus

1. **Projecting at a water, lava or powder-snow cauldron** must do nothing: no conversion, no blood spent. Pinned in Task 3.
2. **A rite-locked stage-0 Condenser drained to empty** must not revert mid-rite, or the upgrade subject vanishes. Pinned in Task 2.
3. **Collecting with a full inventory, or in Creative,** must leave unaccepted formations in the block and not count discarded ones. Pinned in Task 2.
4. **The support block under a Condenser being broken** must drop a cauldron at stage 0 and the stage-kept item at stages 1–2, plus formations, with no duplication. Pinned in Task 2.
5. **Projection while the player is looking at a Condenser seated in an active station rite** must still go to the rite's circuit handling, not fill the Condenser. Pinned in Task 3.

---

### Task 1: `SanguineCondenserRules`

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/SanguineCondenserRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/SanguineCondenserRulesTest.java`

**Interfaces:**
- Produces, all `public static` and taking `int stage` clamped to 0–2:
  - `double poolCapacity(int stage)`
  - `double formationCost(int stage)`
  - `int ticksPerFormation(int stage)`
  - `int heldCapacity(int stage)`
  - `double collapseChance(int stage, int crystallizationLevel)`
  - `double bonusChance(int stage)`

- [ ] **Step 1: Write the failing test.** Use JUnit 5 `@Test` (not `main()`).
  - `numbersFollowTheStageTable` asserts every value in the Global Constraints table.
  - `collapseOnlyAtStageZeroAndSkillRemovesIt` asserts:
    - `collapseChance(0,0) == 0.25`
    - `collapseChance(0,2) == 0.15` (within 1e-9)
    - `collapseChance(0,5) == 0`
    - `collapseChance(0,9) == 0`
    - `collapseChance(1,0) == 0`
    - `collapseChance(2,0) == 0`
  - `outOfRangeStagesClamp` asserts `poolCapacity(-1) == 300` and `poolCapacity(7) == 1500`.
- [ ] **Step 2: Run the test to see it fail.** Run `./gradlew.bat test --tests "*SanguineCondenserRulesTest"`. Expected: compilation failure.
- [ ] **Step 3: Implement the class.** It is a final class with a private constructor and switch-on-clamped-stage bodies.
- [ ] **Step 4: Run the test to see it pass.** Run the same command. Expected: PASS.

### Task 2: Condenser block, block entity, registration and resources

**Files:**
- Create:
  - `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineCondenserBlock.java`
  - `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/SanguineCondenserBlockEntity.java`
- Modify:
  - `common/init/BlockInit.java`: register `sanguine_condenser` in `MODELEDBLOCKS`, metal sound, strength 2.0, `noOcclusion`.
  - `common/init/BlockEntityInit.java`
  - `common/tile/harbinger/crafting/upgrade/UpgradeStation.java`: add `CONDENSER("condenser", "sanguine_condenser", 1)` and a `boolean lesserFirstTier()` accessor, true for ALEMBIC and CONDENSER.
  - `common/tile/harbinger/crafting/upgrade/StationUpgradeTier.java`: `lesser()` becomes `tier == 1 && station.lesserFirstTier()`.
  - `common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java`: add `CONDENSE = "condense"`, `CONDENSE_LATTICED = "condense_latticed"` and the two tiers. The dialogue keys are `hemomancy.vicar.condenser.lattice` and `hemomancy.vicar.condenser.druse`.
  - `common/entity/npc/dialogue/StationUpgradeDialogue.java`: `allowedSpeaker` maps `CONDENSER` to `entity instanceof HarbingerVicarEntity`.
  - `client/event/StationUpgradeTooltips.java`: `CONDENSER` maps to `"entity.hemomancy.harbinger_vicar"` (verify the lang key exists).
  - `assets/hemomancy/lang/en_us.json`
- Create resources:
  - `assets/hemomancy/blockstates/sanguine_condenser.json`. For now it is a variant per stage: stage 0 uses `minecraft:block/cauldron`, and stages 1–2 use placeholder models that Task 6 replaces.
  - `assets/hemomancy/models/item/sanguine_condenser.json`
  - `data/hemomancy/loot_table/blocks/sanguine_condenser.json`. Its alternatives entry has two branches:
    - stage 0: `minecraft:cauldron`, under a `block_state_property` condition
    - otherwise: self with `copy_state` "stage"
  - The tag entries `mineable/pickaxe`.
- Tests:
  - Modify `src/test/.../station/StationUpgradeCatalogTest.java`
  - Create `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SanguineCondenserGameTests.java`, registered the way the neighbouring `*GameTests` classes are.

> Adding the enum constant breaks compilation of the two exhaustive switches. Fix them in this task, along with the station-enumerating tests, so the tree stays green. The rite JSONs, kits and the Vicar dialogue append are Task 5.

**Interfaces:**
- Consumes the Task 1 rules.
- Produces in `SanguineCondenserBlock`:
  - `static SanguineCondenserBlockEntity convertCauldron(ServerLevel level, BlockPos pos, ServerPlayer owner)` replaces an empty `minecraft:cauldron` with stage 0 facing `owner.getDirection().getOpposite()`. It returns the new block entity, or `null` if the block isn't an empty `Blocks.CAULDRON`.
- Produces in `SanguineCondenserBlockEntity`, which implements `IBloodReservoir` and `UpgradeableStation`:
  - `double receiveProjectedBlood(ServerPlayer player, double amount)`: returns the mL accepted (0 when full or rite-locked) and adds the player to the contributors on acceptance.
  - `int collect(ServerPlayer player)`: returns the formations actually inserted.
  - `boolean isContributor(UUID id)`
  - `int heldFormations()`
  - `float progress()`: 0..1, for the renderer.
  - `UUID owner()`
  - `static void serverTick(Level, BlockPos, BlockState, SanguineCondenserBlockEntity)`
  - `getBloodCapability()` sets active and max from `SanguineCondenserRules.poolCapacity(stage)` every call, following the `EnzymaticScriptoriumBlockEntity` pattern.
- The station hook is `stationUpgrades(player).recordUse(UpgradeStation.CONDENSER, CONDENSE)` on any collection of 1 or more. It also records `CONDENSE_LATTICED` when the stage is 1 or more.
- The Vicar hook is `FirstBloodcraftAssignment.recordFormation(player)` on collection of 1 or more when `isContributor(player.getUUID())`.
- NBT keys: `Formations` (int), `Progress` (int ticks), `BloodVolume`, `Owner`, `OwnerCrystallization` (int, cached on each owner projection), `Contributors` (list), `RiteLocked`, `MachineIdentity`, `LastUpgradeRite`.
- `upgradeSnapshot` removes `RiteLocked`, `LastUpgradeRite`, `Progress` and `neoforge:attachments`.
- `readyForUpgradeRite` returns `!riteLocked`. While rite-locked, `canReceiveBlood`/`canProvideBlood` are false and ticking pauses.

- [ ] **Step 1: Update `StationUpgradeCatalogTest`.**
  - Total becomes 14.
  - The `lesser` check in `ceremonyShapeIsStandardAcrossStations` uses `tier.station().lesserFirstTier()`.
  - Add `theCondenserGrowsAtThreeAndFive`. It asserts degrees 3 and 5, the kits `hemomancy:clotting_lattice` and `hemomancy:druse_heart`, the rite paths above, usages `[condense]` and `[condense_latticed]`, tier 1 `riteType() == "lesser"`, tier 2 `"greater"`, `blockId() == hemomancy:sanguine_condenser`, and `seatDistance() == 1`.
- [ ] **Step 2: Write the failing GameTests in `SanguineCondenserGameTests`.**
  - Each test places a cauldron, calls `convertCauldron` with a mock `ServerPlayer` (follow `StationUpgradeGameTests`' mock player helper), and drives the block entity directly.
  - `condensesOverTimeAndStopsWhenFull`: fill 300, advance 200 ticks; there is 1 formation and the pool is 150. After another 200 ticks there is still 1 (stage 0 holds 1). Force collapse off by using a mock owner with crystallization 5.
  - `collectInsertsActuallyAndRecordsUsage`:
    - A full survival inventory leaves the formation in the block and returns 0.
    - With room, it returns 1 and `hasUsed(CONDENSER, "condense")` is true.
    - A Creative player with a full inventory gets 0 counted.
  - `vicarProofNeedsContribution`: two D1 players. The non-contributor collects and the formation flag stays false. The contributor collects and the flag is true.
  - `emptyStageZeroRevertsUnlessRiteLocked`:
    - Draining via `drainBlood` with no formations makes the block `Blocks.CAULDRON`.
    - The same drain with `setRiteLocked(true)` keeps the Condenser.
  - `supportBreakDropsByStage`:
    - Removing the block below a stage-0 Condenser leaves a `minecraft:cauldron` item plus held formations, and no `sanguine_condenser` item.
    - At stage 1 it drops a `sanguine_condenser` item whose block-state component has `stage=1`.
  - `fullCondenserRefusesBlood`: `receiveProjectedBlood` returns 0 at capacity.
- [ ] **Step 3: Run the tests to see them fail.**
  - Run `./gradlew.bat test --tests "*StationUpgradeCatalogTest"`. Expected: FAIL or compile error.
  - Run `./gradlew.bat gameTestServer`. Expected: the new tests fail or fail to compile.
- [ ] **Step 4: Implement the block.**
  - Its shape is the vanilla cauldron shape at stage 0, with stage 1–2 shapes within one block. Its ticker is server-only.
  - `canSurvive` needs a sturdy top face below, with removal through `updateShape`.
  - Empty-hand `useWithoutItem`, and `useItemOn` with any item other than Blood Projection or this station's kit, call `collect`. `useItemOn` first tries `CreativeStationUpgrades.tryApply`.
  - `onRemove` drops held formations unless the next state is the same block.
  - Register the extract-only `ItemHandler.BLOCK` capability in `HemoCapabilityRegistrar` next to the existing `ItemHandler.BLOCK` registrations. It has one slot exposing a `sanguine_formation` stack of `heldFormations()`, and `insertItem` returns its input unchanged.
- [ ] **Step 5: Implement the block entity.**
  - **Tick.** When the pool holds at least the cost, formations are below the held capacity, and the block isn't rite-locked, advance progress. At `ticksPerFormation`, drain the cost and roll:
    - collapse, using the cached owner level
    - otherwise one formation, plus a bonus formation when there is room and the `bonusChance` roll succeeds
  - **Revert.** At stage 0, with blood at 0, zero formations, progress 0 and no rite lock, `setBlock(Blocks.CAULDRON.defaultBlockState())`.
  - **Sync.** Sync via update tag/packet.
  - **Collect.** Use `player.getInventory().add(stack)` and count only `original − stack.getCount()` for a non-Creative player. A Creative player never counts.
- [ ] **Step 6: Fix the station-enumerating tests and switches.**
  - Adjust `StationTierPersistenceResourceTest` so it still passes against the branching loot table (its `copy_state` + `"stage"` string check already holds).
  - For `StationUpgradeActivationGameTests`, `StationUpgradeGameTests` and the `CentrifugeUpgradeGameTests` station array: add CONDENSER, or skip it until Task 5 adds its rites. Prefer adding it in Task 5, and mark loops that call `upgradeBoth`/rite lookups to skip CONDENSER until then with a `// Task 5` comment that Task 5 removes.
- [ ] **Step 7: Run the tests to see them pass.** Run `./gradlew.bat test` and `./gradlew.bat gameTestServer`. Expected: PASS, apart from known unrelated failures; note them in the hand-off.

### Task 3: Projection routing and removing the bare-block path

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/event/SanguineCondenserProjection.java`
- Modify `common/item/harbinger/tool/living/BloodProjectionItem.java`:
  - Insert the branch after `BloodStructureFeedManager.feedStructure` and before `BloodInfusionManager.feedBlock`.
  - Delete the formation fallback at the end of the method and its import.
- Modify:
  - `common/rite/harbinger/HarbingerCardinalRiteEvents.java`: remove the `SanguineFormationProjectionHandler.tick` call.
  - `common/event/TransientServerStateEvents.java`: remove the `clear()` call.
  - `common/network/PacketHandler.java`: remove the packet registration.
  - `client/event/ClientEvents.java`: remove the client data tick and the world render of the formation projection.
- Delete:
  - `common/event/SanguineFormationProjectionHandler.java`
  - `common/event/SanguineFormationProjectionRules.java`
  - `common/network/capa/harbinger/PacketSanguineFormationProjection.java`
  - `client/data/ActiveSanguineFormationProjectionClientData.java`
  - `data/hemomancy/tags/block/sanguine_formation_projectors.json`
- Move: `SanguineFormationProjectionRenderer.renderSphere` goes to a new `client/render/world/SanguineSphereRenderer.java`, keeping the same signature and `scaledUndulation` if it's used. Repoint `IchorianSigilLandmarkRenderer` and `AwakenedIchorianSigilRenderer`, then delete the old renderer and rename its test.
- Tests:
  - Delete or replace `SanguineFormationProjectionSourceTest` and `SanguineFormationProjectionRulesTest`.
  - Edit `TransientServerStateLifecycleTest` (drop the `clear` requirement).
  - Edit `CellHandItemRendererParticleOriginTest` lines 20 and 68, re-pointing the shared-ray assertion at `SanguineCondenserProjection`.
  - Edit `HarbingerJourneyFixtureGameTests.attunedProjectorTarget` (delete it).
  - Create `src/test/java/com/vincenthuto/hemomancy/common/event/SanguineCondenserProjectionSourceTest.java`.
  - Create `src/test/java/com/vincenthuto/hemomancy/common/recipe/CondenserRecipeSafetyResourceTest.java`.
  - Add GameTests to `SanguineCondenserGameTests`.

**Interfaces:**
- Consumes `SanguineCondenserBlock.convertCauldron` and `SanguineCondenserBlockEntity.receiveProjectedBlood` (Task 2).
- Produces `static java.util.OptionalDouble tryProject(ServerLevel level, ServerPlayer player, BlockPos target, double tileTransferRate)`.
  - It returns empty when the target is neither an empty `Blocks.CAULDRON` nor a Condenser.
  - Otherwise it returns the mL spent, which may be 0. At 0 on a full Condenser it shows the actionbar text `hemomancy.sanguine_condenser.full`: "The cauldron can hold no more."
  - It requires `HemoCapabilityAccess.getBloodVolume(player)` to be active. It drains the player, calls `addBloodSpend`, and sends a `BloodVolumeServerPacket`.
  - On conversion it plays a fill sound and a small custom splash (`HemoParticleData`).

- [ ] **Step 1: Write the failing JVM tests.**
  - `SanguineCondenserProjectionSourceTest`:
    - In `BloodProjectionItem.java`, the index of `SanguineCondenserProjection.tryProject` is greater than the index of `BloodStructureFeedManager.feedStructure` and less than the index of `BloodInfusionManager.feedBlock`.
    - The source contains no `SanguineFormationProjectionHandler`.
    - No file under `src/main/java` contains `sanguine_formation_projectors`.
  - `CondenserRecipeSafetyResourceTest`: no JSON under `recipe/blood_infusion/` mentions `minecraft:cauldron` or `hemomancy:sanguine_condenser`, and no JSON under `recipe/blood_structure/` mentions `hemomancy:sanguine_condenser`.
- [ ] **Step 2: Write the failing GameTests.**
  - `projectionConvertsEmptyCauldronAndFills`: projection via `BloodProjectionItem.projectFromEntity` with the player looking at a cauldron produces a Condenser with blood above 0.
  - `projectionIgnoresFilledCauldrons`: a water cauldron, a lava cauldron and a powder-snow cauldron each stay the same block, and the player's blood is unchanged.
  - `stoneMakesVenousStoneNeverFormation`: project at stone until done. Venous Stone results, and there is no `sanguine_formation` item entity in the area.
  - `riteSeatedCondenserDefersToRiteProjection`: during an active `first_clotting` rite, projecting at the seat does not change the Condenser's blood. This test is added in Task 5 once the rite exists; leave a TODO marker here only if Task 5 hasn't run.
- [ ] **Step 3: Run the tests to see them fail.** Run `./gradlew.bat test --tests "*Condenser*"`. Expected: FAIL.
- [ ] **Step 4: Implement `SanguineCondenserProjection`, insert the branch, and delete or move the files listed above.**
- [ ] **Step 5: Run the tests to see them pass.** Run `./gradlew.bat build`, then `./gradlew.bat gameTestServer`. Expected: PASS. A grep for `SanguineFormationProjection` under `src/` should return only the renamed sphere-renderer test, if it's kept.

### Task 4: Vicar chapter, teaching text and journey fixtures

**Files:**
- Modify:
  - `assets/hemomancy/lang/en_us.json`: the keys `vicar.blood_crafting.projection`, `vicar.neophyte.projection_hint`, `dialogue.vicar.option.projection_hint`, `hermit.neophyte.hint`, `screen…ledger.step.project_formation` and its `.desc`, `alchemist.item_inquiry.sanguine_formation.line1/2`, `skill…sanguine_crystallization.desc` and `vicar.neophyte.first_bloodcraft_reward.unready`.
  - `data/hemomancy/books/libersanguinium/the_hematic_order/pages/degrees.json`:7
  - `MaterialsData.java` and `MaterialAtlasSpec.java`: a `sanguine_condenser` node gated like `sanguine_formation`, with parent `mortal_display`.
  - Create `data/hemomancy/dialogue_inquiry/vicar/hemomancy/sanguine_condenser.json`. Mirror an existing Vicar inquiry; if the Vicar has none, use the Alchemist folder layout.
- Modify the journey harness:
  - `src/gameTest/.../journey/HemoJourneyFixtures.java:185-188`: place a `minecraft:cauldron` instead of `venous_stone`.
  - `HarbingerJourneyAutomation.projectFormation`: project, advance until a formation is held, then `collect`.
  - `HemoJourneyChecks.verifyFormation` and `HemoJourneyCheckpointRules.formationPassed`: the check is "the formation flag is set and the player holds a formation".
  - The `HemoJourneyController` instruction text.
- Tests: update `VicarBloodCraftingAssignmentSourceTest`, `FirstBloodcraftAssignmentSourceTest` (lines 45, 71), `HarbingerHermitRoadMissionSourceTest`:168 and `GameTestHarnessSourceContractTest` (lines 75, 356) to the new strings and call sites.
- Copy (exact text, see below):
  - `…projection_hint`: "Two lessons, two targets. Pour your blood into an empty cauldron and it will hold it, and in time crystallize a Sanguine Formation. Take it out yourself. Pour it instead into ordinary stone and the stone drinks it and stains to Venous Stone."
  - `dialogue.vicar.option.projection_hint`: "How do I condense a formation, and make Venous Stone?"
  - Ledger step: "Condense a Sanguine Formation". Its description: "Project blood into an empty cauldron, wait for it to crystallize, and collect the formation yourself."
  - Skill description: "Steadies a Sanguine Cauldron: reduces its collapse chance by 5% per level."
  - Liber line: "…fill a cauldron with Blood Projection and collect the Sanguine Formation it condenses, project into stone to make Venous Stone…"

- [ ] **Step 1: Update the source tests to the new strings and call sites.** Run `./gradlew.bat test --tests "*FirstBloodcraft*" --tests "*Vicar*" --tests "*GameTestHarnessSourceContractTest"`. Expected: FAIL.
- [ ] **Step 2: Implement the lang, Liber, atlas, inquiry and journey changes.**
- [ ] **Step 3: Run the tests to see them pass.** Run the same command, then `./gradlew.bat gameTestServer`. The journey `FORMATION_PROJECTED` stage and the `ClinicalBloodProgressionGameTests` first-bloodcraft tests should PASS.

### Task 5: Station upgrade rites, kits and Vicar gifts

**Files:**
- Modify `common/init/ItemInit.java`. Add these to the station-kit block, using the `rubricators_quill` pattern:
  - `clotting_lattice`: `stacksTo(1)`, UNCOMMON
  - `druse_heart`: `stacksTo(1)`, RARE
- Create the recipes:
  - `data/hemomancy/recipe/clotting_lattice.json`: shaped, `["SFS", "SAS", " S "]`, where S is `hemomancy:hematic_iron_scrap`, F is `hemomancy:sanguine_formation` and A is `hemomancy:befouling_ash_trail`.
  - `data/hemomancy/recipe/druse_heart.json`: shaped, `[" C ", "GBG", " C "]`, where C is `hemomancy:blood_crystal_shard`, G is `hemomancy:sanguine_glass` and B is `minecraft:amethyst_block`.
  - Before using them, verify that each ingredient's item ID exists and is obtainable by D3 and D5 respectively. If one isn't, substitute and note the substitution in the hand-off.
- Create the rites, copying `first_condensation.json` and `sanguine_athanor.json` respectively:
  - `data/hemomancy/recipe/cardinal_rite/first_clotting.json`:
    - lesser, `required_degree` 3, `floor` `hemomancy:working_lesser`, pattern `[[" S "],["   "],["   "]]`, S `hemomancy:sanguine_condenser`
    - 4 anchors, `riteName` "Rite of the First Clotting"
    - offerings: `clotting_lattice`, `hematic_iron_scrap`, `befouling_ash_trail`, `venous_stone`, `blood_stained_stone`, `sanguine_formation`
  - `data/hemomancy/recipe/cardinal_rite/druse_crystallization.json`:
    - greater, `required_degree` 5, `floor` `hemomancy:working_greater`, the same pattern
    - 8 anchors, `riteName` "Rite of Druse Crystallization"
    - offerings: `druse_heart`, `amethyst_shard`, `blood_crystal_shard`, `sanguine_glass`, `hematic_iron_block`, `suspended_blood_crystal`
  - Verify every offering ID exists.
- Modify:
  - `common/entity/npc/harbinger/HarbingerVicarEntity.java` `progressionDialogue`: before return, add `tree = StationUpgradeDialogue.append(tree, serverPlayer, UpgradeStation.CONDENSER, "<the Vicar's existing leave key>")`.
  - `HarbingerRecipeMapDefinitions`: file both rites in the making-purpose group.
  - `LiberEntryDefinitions`: add `RITE_UNLOCKS` for both rite paths to a new Liber entry `sanguine_condenser`. Create the entry page JSON in the bloodcraft chapter, following a neighbouring station entry.
- Lang:
  - Item names and the stage names for the block's hover text.
  - Each dialogue key's `.title`, `.prompt`, `.ready`, `.requirements` and `.claimed`. The Vicar's voice is clinical and devout, matching the `hemomancy.vicar.*` neighbours. For example, `.requirements`: "Bring me proof you have drawn a formation from a cauldron with your own hands, and the lattice is yours."
  - Rite names and descriptions.
- Assets: kit item models plus native 16×16 binary-alpha sprites. Follow `tools/model_export/centrifuge_kit_sprites.json` and `export_centrifuge_kit_sprites.py`: add a sibling `condenser_kit_sprites.json` and either extend the exporter or create a sibling one. The palette is hematic iron, bone, crimson and pale crystal, with no copper.
- Tests:
  - Remove Task 2's CONDENSER skips in `StationUpgradeActivationGameTests`, `StationUpgradeGameTests` and `CentrifugeUpgradeGameTests`.
  - Add `condenserLatticeUpgrade` and `condenserDruseUpgrade` (`assertUpgradeCompletes`) and `condenserActivationAndFinale` (`upgradeBoth`).
  - Add the Task 3 `riteSeatedCondenserDefersToRiteProjection` GameTest.
  - Add `upgradePreservesBloodFormationsAndContributors` to `SanguineCondenserGameTests`.

- [ ] **Step 1: Write the GameTests and enable the resource tests.**
  - The new GameTests above.
  - `StationUpgradeRiteResourceTest` and `StationUpgradeRecipeResourceTest` already iterate the catalog, so they now fail on the missing JSON.
  - Run `./gradlew.bat test --tests "*StationUpgrade*"`. Expected: FAIL (missing files).
- [ ] **Step 2: Implement the items, recipes, rites, dialogue hookup, recipe map, Liber entry, lang and sprites.**
- [ ] **Step 3: Run the JVM tests.** Run `./gradlew.bat test`. Expected: PASS, including `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest`, `RecipeMapPurposeGroupDataTest` and `CardinalRiteProgressionResourceTest`. Update `CardinalRiteProgressionResourceTest`:157 if it enumerates rite IDs.
- [ ] **Step 4: Run the GameTests.** Run `./gradlew.bat gameTestServer`. Expected: the new and existing station GameTests PASS.

### Task 6: Visuals (renderer, stage models, effects)

Read `docs/MANIPULATION_VISUALS.md` and `docs/agents/hemomancy-mod-agent.md` first.

**Files:**
- Create:
  - `src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/SanguineCondenserRenderer.java`, registered in `ClientEvents` next to `ScryingPodiumRenderer`.
  - `tools/model_export/sanguine_condenser.py`. It writes:
    - `models/block/bbmodel/sanguine_condenser_latticed.bbmodel` and `…_druse.bbmodel`
    - `models/block/sanguine_condenser_latticed.json` and `…_druse.json`
    - the full `blockstates/sanguine_condenser.json`: stage 0 uses `minecraft:block/cauldron` for every facing, and stages 1–2 are rotated by `FACING`

    Separate coplanar faces with `tools/model_export/coplanar.py`, following `vial_centrifuge.py`, and give it a `--preview` flag.
  - The textures under `assets/hemomancy/textures/block/sanguine_condenser/`.
- Modify `SanguineCondenserBlock`: set the stage 1–2 voxel shapes to match the exported geometry.

**Renderer behaviour:**
- **Blood surface.** The stage interior surface height is `lerp(floorY, rimY, blood/capacity)`. Draw it with the wavy dark-crimson pool technique from `ScryingPodiumRenderer`.
- **Forming formation.** Draw `SanguineSphereRenderer.renderSphere` (Task 3) at the surface centre, scaled by `progress()`.
- **Held formations.** Draw them as `ItemDisplayContext.FIXED` items resting on the surface or lattice in a ring, scale about 0.35, following `IronBrazierRenderer`.
- **Effects.** These are triggered from the server with `level.sendParticles` of `HemoParticleData`/HutosLib data only:
  - formation complete: a short glow/cell burst at the surface
  - collapse: a dark fizz
  - conversion: a splash (Task 3)

  Sounds stay vanilla: amethyst cluster break on completion, fire extinguish on collapse.

- [ ] **Step 1: Write and run the exporter.**
  - Run `python tools/model_export/sanguine_condenser.py`, then `python tools/model_export/sanguine_condenser.py --preview`.
  - Expected: the files are written, and the preview PNGs show a hematic-iron re-banded cauldron with a clotting lattice (stage 1) and a crystal-lined druse basin (stage 2), both within one block.
- [ ] **Step 2: Implement the renderer and the server effect calls.** Grep the new files for `ParticleTypes.`: there must be zero matches.
- [ ] **Step 3: Run the asset checks.** Run `./gradlew.bat build` and `./gradlew.bat alphaCheck`. Expected: PASS, with no packaged asset-path or PNG-header errors.
- [ ] **Step 4: Check in game.** Use `runClient` or a review client, following `reference_review_clients` memory: convert a cauldron, fill it, watch it condense, collect, and Creative-apply both kits. Screenshot each stage.

### Task 7: Documentation, JEI info, final verification

**Files:**
- Modify `docs/HEMOMANCY_REFERENCE.md`:
  - :205 and :221: the proof wording.
  - :223: replace the bare-block mechanic paragraph with the Condenser mechanic. Cover the numbers table, the cauldron conversion and revert, collection and proofs, and routing order.
  - :2834: the items table row.
  - Add a "Sanguine Condenser" station subsection near the other station sections.
- Modify `docs/STATION_UPGRADES.md`:
  - "seven stations" wording and the Creative paragraph.
  - Add two rows to the tiers table and two to the offerings table, plus a short stage-behaviour paragraph.
  - Note the Vicar as teacher and that `lesser()` now comes from `UpgradeStation.lesserFirstTier()`.
- Modify `wiki/Harbinger-Path.md`:36.
- Check `docs/LORE_REFERENCE.md` for formation lore.
- Modify `compat/jei/JEIPlugin.java`: add an `addIngredientInfo` page for `minecraft:cauldron` and `hemomancy:sanguine_condenser` saying "Project blood into an empty cauldron to make a Sanguine Cauldron; it condenses Sanguine Formations." Use a lang key.

- [ ] **Step 1: Update the docs and JEI info.**
- [ ] **Step 2: Run the full verification.**
  - Run `./gradlew.bat build`, `./gradlew.bat test`, `./gradlew.bat gameTestServer` and `./gradlew.bat alphaCheck`. Expected: all PASS. Record any pre-existing unrelated failures with their output.
- [ ] **Step 3: Get a final review.** Dispatch one fresh reviewer on the whole change against the spec and this plan, then fix the confirmed findings.
