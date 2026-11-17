# 02. Ground network, farming, water, daily loop and world answer

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (B09): the lighter pays 15 mL per catalyst dissolved, source `ground_feeder`, reserve protected; away, dead or short means no top-up (NQ-02-1, NQ-02-2). |
| T2 | Built (B09): a hot run burns its four nearest furnaces, saved per lit cell. |
| T3 | Built (P04). |
| T4 | Built (B09): 6 mL a pulse for each furnace fed (NQ-02-9). |
| T5 | Built (B09): veins move blood only toward a drawing end; a machine under 600 mL draws, stores never pull by level (NQ-02-3 A); 5 s draw window. |
| T6 | Built (B09) in full: water breaks gouges (flowing or poured only, NQ-02-5); veins hold a custom water state and water flows over them (NQ-02-4), through a mixin on `FlowingFluid.tick`. |
| T7 | Built (B09): kills count up to 600 mL in a region (NQ-02-7). |
| T8 | Built (B09): tainted meat 10, raw fish 4, cooked fish 5 (NQ-02-8). |
| T9 | Built (P04, B09): every ruling of this chunk is in `docs/CORE_LOOP.md` "Rulings that bind". |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings keep almost all of Tracks 6, 7 and 10 as built and tighten the ground network so it cannot run a base for
free. Eight rows need work. The two largest are the owner's own designs: living veins move blood only toward an end that
is drawing (CL-14), and water breaks gouges but not living veins (CQ-44). The rest are small number and rule changes:
the feeder's 15 mL, a cap of 4 furnaces on a hot run, feed runs at one stage per 30 s, Furnace Veins priced per
furnace, a cap on the kill share of region pressure, and two meal values.

Surprises from the code:

- None of the ground, farm, chum or world-answer code changed after the sheet (file times checked). No conflicts.
- CQ-44 for gouges is a revert of half of round two P8. Before P8 water already replaced a gouge. For veins the as-built
  P8 behaviour (hold back flowing water, a source lies over) is the cheap way to "not break". True flow-over needs a
  custom flowing-water state.
- CL-14 has no "draw" signal in code today. `GroundRunRules.veinTransfer` levels the two tanks by share whenever the
  owner is near. A new stamp on `BlockBloodVolume` is needed, and a dry machine never draws unless the rule says so.
- CQ-7's two options differ by about 24 times in price (15 mL per catalyst against 2 mL a second). The first is planned.
- CB-24 at 30 s equals the cloche's own pace (`CLOCHE_STAGE_TICKS` 600). A feed run still wins on price and area.
- CB-3 makes raw fish equal cooked fish (both 4) and puts tainted meat (30 mL an item) under tainted harvest (36 mL).
  `BloodFoodDataTest` and one GameTest pin the old order.
- CN-7's "only drowned answer chum" is out of date. P7 added the Votary and the Bog Revenant.

Counts: 34 rows. BUILD 8, KEEP 20, DONE 1, MOOT 0, CONFLICT 0, UNCLEAR 0, OWNER 3, PARKED 2.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CQ-7 | A feeder spends 15 mL of blood each time it dissolves a catalyst (first option of the "or"). | BUILD | `common/world/ground/GroundNetwork.java` `feed(ServerLevel, CatalystFeederBlockEntity)`: extracts one item and calls `pulse` with `lit.owner()`. No blood is paid. `CatalystFeederBlockEntity` has no tank. | T1 |
| CQ-43 | One hot run burns at most 4 furnaces. Build after CQ-7. | BUILD | `GroundNetwork.heat`: every hot cell stokes all six neighbours each effect tick through `FurnaceHeat.stoke`, no count. `GroundRunRules.HOT_FURNACE_BURN` 15. | T2 |
| CL-14 | Living veins move blood only while something draws from one end. A vein with no drawing current does not siphon. | BUILD | `GroundNetwork.tickNode` and `GroundRunRules.veinTransfer`: with the owner within 32 blocks the fuller tank (by share) always runs to the emptier, 20 mL/s at most, 1 mL/s upkeep. No notion of demand exists. | T5 |
| CQ-44 | Water breaks gouges. Water does not break living veins. | BUILD | `common/block/harbinger/ground/BloodGougeBlock.java` implements `SimpleWaterloggedBlock` with `WATERLOGGED`. `LivingVeinBlock extends BloodGougeBlock`, so both hold flowing water back and take a source. Build log P8 says water washed gouges away before P8. | T6 |
| CQ-42 | Carried crossing stays a flat 125 mL. | KEEP | `common/world/vein/EarthenVeinTravelRules.java` `LIVING_VEIN_COST = 125`. `GroundNetwork.livingRunJoins`. | T9 |
| CQ-15 | Furnace Veins is priced per furnace fed. | BUILD | `common/manipulation/flammeus/FurnaceVeinsShaping.java`: `BLOOD_PER_PULSE = 6.0` flat for a bank of up to `BANK_MAX = 8` within `BANK_RADIUS = 2`. Price is read in `BloodManipulation` through `shaping.bloodPerPulse(player)` right after `admits`. File not touched since the sheet. | T4 |
| CB-24 | Feed runs grow one stage per 30 s (first option). | BUILD | `GroundRunRules.FEED_EVERY = 12` effect ticks of 10 ticks, so 6 s. `GroundNetwork.feed(level, pos)` grows every `CropBlock` beside the cell. | T3 |
| RV-45 | Ownerless or absent-owner run smelts and holds crops back, lights and freezes nothing. Keep. | KEEP | `GroundNetwork.owner`, `heat` (returns before kindling and stoking when owner is null), `chill`, `chilledBeside`. Matches. After T1 such a run is also no longer topped up by its feeder. | T9 |
| RV-46 | A feeder keeps a run for whoever lit it. Keep. | KEEP | `GroundNetwork.feed` passes `lit.owner()` to `pulse`. T1 charges that same player. | T9 |
| CL-15 | The cloche is the Mycelial Lantern re-housed. Tiers for specialty fungus are an idea ("maybe"). | DONE | `common/tile/harbinger/crafting/MycelialLanternBlockEntity.java` `tendPlant`, `common/world/farm/ClochePlanting.java`. Re-housing is built. The Lantern is not in `UpgradeStation` and has no stage. Tiers are NQ-02-6. | none |
| CQ-34 | Keep the Brined Votary as the chum hunter in home waters. | KEEP | `common/world/answer/WorldAnswer.java` `drawHunter`: `case HOME -> EntityInit.brined_votary`. | T9 |
| CQ-35 | Keep the substrate split. Ferric stays on Salt-Stained Timber unless the owner moves it. | KEEP | `common/block/harbinger/plant/FungalStrains.java`: Vivacious, Fervent, Ruinous `FLESH`. Neurotic, Frigid, Ferric `BRINED`. Incandescent, Umbral `COMMON`. | T9 |
| CQ-36 | Salt-Stained Timber and its two recipes stay. | KEEP | `data/hemomancy/recipe/salt_stained_timber_from_voyager_log.json` (1 log item + 8 logs make 8) and `salt_stained_timber.json` (1 timber, 1 dried kelp, 4 logs make 5). | T9 |
| CQ-37 | Tainted harvest rules stay. | KEEP | `BloodFedGrowthRules.TAINT_CHANCE = 0.08`, `taints` (level 2 only). `ItemInit.tainted_harvest` food 3 / 0.3. `blood_food/tainted_harvest.json` 12. `gouge_catalyst/feed.json` 1,200 ticks. See NQ-02-8 for how it now sits against tainted meat. | T9 |
| CQ-38 | No catalyst crops for the other six schools. The strains stand for them. | KEEP | `BlockInit.glowroot`, `BlockInit.emberpod` are the only `CatalystCropBlock`s. Spores are worth 5 in `temper_catalyst/*.json` (Lux and Flammeus read). | T9 |
| CQ-39 | Farmable spore income is accepted. | KEEP | `FungalStrainRules.RIPE_SPORES = 1`, `BONUS_SPORE_CHANCE = 0.5`. | T9 |
| CQ-40 | Floor spikes stay cosmetic. | KEEP | `BloodFedGrowthRules.FLOOR_GROW_CHANCE = 0.1`, `MAX_FLOOR_SPIKE = 3`, `FLOOR_SPIKE_GAP = 2`. Damage not checked beyond the vanilla parent class. | T9 |
| RV-63 | Strains are bush blocks the cloche does not grow. 1 in 6, no light, bone meal +1 on fed ground. Keep. | KEEP | `FungalStrainBlock.randomTick` (`GROW_ROLL = 6`, no light read), `performBonemeal` (+1), `BloodFedGrowthEvents.onBonemeal` (refused on unfed ground). `ClochePlanting.seedling` takes only crops and stems. | T9 |
| RV-64 | Strain-bed first on cutting a ripe strain, Harbingers only. Keep. | KEEP | `FungalStrainBlock.playerDestroy`: ripe, `volume.isActive()`, `ADV_FIRST_STRAIN_BED`. | T9 |
| RV-65 | Catalyst crops worth 2. Ripe glowroot light 4, Incandescent strain 3. Keep. | KEEP | `temper_catalyst/lux.json` glowroot 2, `flammeus.json` emberpod 2. `BlockInit.glowroot` light 4 at max age. Strain registration gives Incandescent 3 at max age. | T9 |
| RV-66 | Chum hunters spawn as events, despawn normally, cap 1. Elder guardian curse sound at low volume. Keep. | KEEP | `WorldAnswer.drawHunter`: `MobSpawnType.EVENT`, `WorldAnswerRules.HUNTER_CAP = 1`, `ELDER_GUARDIAN_CURSE` at 0.35. The 2026-11-13 sound audit (`rites_world_ui.md`) proposes a custom `world.answer.rise` here. That is a proposal, not a change, so no conflict. | T9 |
| RV-67 | Atlas placement stays. | KEEP | `client/screen/skilltree/shared/MaterialAtlasSpec.java`: `salt_stained_timber` `d(3)`. `glowroot`, `emberpod`, `tainted_harvest` `d(2)`. | T9 |
| RV-70 | Ground runs draw before translucent blocks. Snuff stream on the Mortem flow material. Keep. | KEEP | `client/render/world/WorldEffectDispatcher.java` draws `GroundRunRenderer` at `AFTER_CUTOUT_BLOCKS`. `EclipseSnuffShaping` uses `BloodFlowPacket.Style.EXTRACTION` and is still registered in `Shapings` after the 2026-11-12 dome rebuild. Still needed after T6 for waterlogged veins and glass. | T9 |
| RV-72 | JEI gouge pages in function order. Keep. | KEEP | `compat/jei/GougeCatalystRecipeCategory.java` iterates `GroundFunction.values()` (none, light, hot, point, cold, pull, feed, dark). | T9 |
| CN-7 | Track 10's leftovers stay deferred until the CL rulings. | PARKED | `WorldAnswer.willMultiplier(level, pos)` reads region pressure. No Tenebris or Lux answer, no spreading sod, no bestiary world pages found. The "only drowned" line is stale: `drawHunter` has three hunters since P7. | none |
| CL-19 | The Parish is deferred. | PARKED | Nothing built. `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md` "Deferred: the Parish". | none |
| CB-26 | Cap how much of a region's pressure kills can supply. | BUILD | `WorldAnswerRules.KILL_SPILL = 6.0`, `killSpill`. `WorldAnswer.killed` calls `BloodSpilledLedger.spill` like any spill. The ledger keeps one number per region, so kills cannot be told apart. No cap value is in the answer (NQ-02-7). | T7 |
| CL-18 | Food and sleep feed regeneration. As built. | KEEP | `BloodRegenRules`: starving 0, hungry 0.5, plain 1.0, well fed 1.5, `RESTED_BONUS = 1.0`, `RESTED_TICKS = 6_000`. Read in `BloodVolumeEvents` and `DailyLoopEvents`. CB-2 (skills that swamp it) is another chunk's row. | T9 |
| CB-3 | Tainted meat 10 mL per nutrition. Raw fish 4. | BUILD | `data/hemomancy/blood_food/tainted_meat.json` 20, `raw_fish.json` 3, `cooked_fish.json` 4. `BloodFoodDataTest.cookedIsWorthMoreAndTaintedMeatTheMost` pins cooked fish above raw fish. | T8 |
| CQ-48 | Brought On credits every awake player on counted ground. As built. | KEEP | `WorldAnswer.creditMoonBroughtOn`, called from `BloodMoonEvents` when `WorldAnswerRules.broughtOn` is true. | T9 |
| CQ-49 | Carried by the Vein is granted when the opening is fed in full. As built. | KEEP | `EarthenVeinTravelManager.project`: grants `ADV_CARRIED_CROSSING` at phase `READY` for a carried crossing. | T9 |
| PR-18 | Play the ground network. | OWNER | `docs/CORE_LOOP_PLAYTEST.md` exists. Not read line by line. | none |
| PR-19 | Play farming. | OWNER | Same. | none |
| PR-23 | Play the daily loop. | OWNER | Same. | none |

## Plan

### T1. The feeder's 15 mL (size S)

- Rulings: CQ-7, RV-46.
- Change: in `GroundNetwork.feed(ServerLevel, CatalystFeederBlockEntity)`, before the catalyst is extracted, resolve
  the run's owner with the existing `owner(level, lit.owner())` helper (online, alive, in the level). No owner, or an
  owner who cannot pay `GroundRunRules.PULSE_ML` (15) in full, means the feeder does not feed and the run goes dark on
  time. Otherwise pay 15 mL through `BloodAccounting.pay` with a new named source (`ground_feeder`, category `WORLD`).
  I recommend it be reserve protected like `VEIN_UPKEEP`, since the player pressed nothing. An ownerless run is never
  topped up. This plan assumes the lighter pays (NQ-02-1) and the first price option (NQ-02-2).
- Files: `src/main/java/com/vincenthuto/hemomancy/common/world/ground/GroundNetwork.java`,
  `common/world/ground/GroundRunRules.java` (a named constant if the price should differ from `PULSE_ML` later),
  `common/block/harbinger/ground/CatalystFeederBlock.java` (class comment says "never blood"),
  `src/main/resources/assets/hemomancy/lang/en_us.json` (`hemomancy.artificer.craft.hot_runs.feeder.line1/2`,
  `hemomancy.artificer.item_inquiry.catalyst_feeder.line1/2`, `hemomancy.jei.gouge_catalyst.projection.tip`),
  `data/hemomancy/books/libersanguinium/manipulations/pages/the_ground_network.json`.
- Tests: `GroundNetworkGameTests.aFeederKeepsALitRunLit` (give the run an owner and assert 15 mL left them),
  `aFeederNeverLightsADarkRun` (unchanged), new `aFeederStopsWhenItsOwnerIsAwayOrDry`. `BloodAccountingGuardrailTest`
  must stay green. Artificer lines follow his sheet in `docs/NPC_VOICES.md`.
- Docs: `docs/archive/tracks/core_loop/06-ground-network.md` ("It feeds catalysts only, never blood"), `docs/CORE_LOOP.md` rulings,
  `docs/HEMOMANCY_REFERENCE.md` Track 6 section, `wiki/Blood-Systems.md`.
- Depends on: nothing. NQ-02-1 and NQ-02-2 can change it.

### T2. Four furnaces per hot run (size M)

- Rulings: CQ-43.
- Change: new `GroundRunRules.HOT_FURNACE_MAX = 4`. `GroundNetwork.heat` runs per cell, so the cap has to be decided
  for the whole run. Decide it in `pulse(...)`, which already walks the run nearest first: for a hot run pick the first
  4 distinct furnace, smoker or blast furnace positions beside its cells and record on each cell which sides it may
  stoke (a new field on `GroundRunSavedData.Lit`, saved with the cell). `heat` then stokes only those sides. A furnace
  set down later joins at the next pulse or feeder top-up. Kindling campfires and candles is not capped. No migration
  is needed (the mod is unreleased).
- Files: `common/world/ground/GroundNetwork.java`, `common/world/ground/GroundRunRules.java`,
  `common/world/ground/GroundRunSavedData.java`, `en_us.json` (`hemomancy.jei.gouge_catalyst.hot` says "any furnace"),
  the Liber page `manipulations/pages/the_ground_network.json`.
- Tests: `GroundNetworkGameTests.aHotRunBurnsTheFurnacesBesideIt` (still passes with four blocks), new
  `aHotRunBurnsAtMostFourFurnaces` (six furnaces, the four nearest the pulse burn). A JVM case in `GroundRunRulesTest`
  if the pick is written as a pure function.
- Docs: `docs/archive/tracks/core_loop/06-ground-network.md` catalyst table (hot row), `docs/HEMOMANCY_REFERENCE.md`,
  `docs/audits/2026-11-11-visual-balance/tuning-sheet.csv` is a dated audit and is left alone.
- Depends on: T1 (the owner asked for CQ-7 first).

### T3. Feed runs at one stage per 30 s (size S)

- Rulings: CB-24.
- Change: `GroundRunRules.FEED_EVERY` from 12 to 60 (60 effect ticks of 10 ticks is 600 ticks). Fix a side effect while
  there: a crop touching two lit cells is grown by each of them, so it gets two stages per period. Key the beat on the
  crop's position and let one cell grow it per period, so "one stage per 30 s" is true per crop.
- Files: `common/world/ground/GroundRunRules.java`, `common/world/ground/GroundNetwork.java` (`feed(level, pos)`,
  `beat`).
- Tests: `GroundNetworkGameTests.aFeedingRunGrowsTheCropsBesideIt` (longer wait or a time jump), new
  `aCropBesideTwoFeedingCellsGrowsOnce`.
- Docs: `docs/archive/tracks/core_loop/06-ground-network.md`, `docs/archive/tracks/core_loop/07-farming-and-water.md`, `docs/HEMOMANCY_REFERENCE.md`.
- Depends on: nothing. Fits T1: both take the first option, so no per-beat charge is added here.

### T4. Furnace Veins priced per furnace (size S)

- Rulings: CQ-15.
- Change: the pulse costs `BLOOD_PER_PULSE` (6 mL) for each furnace in the bank that has something to cook, not 6 mL
  for the whole bank. A full bank of 8 is 48 mL a pulse, 96 mL a second. `admits` already builds the bank just before
  the price is read, so it can remember the count per player (a UUID session map cleared through
  `TransientServerStateEvents`, as `GougeCuttingShaping.LAST` is) and `bloodPerPulse(Player)` returns 6 times it.
  The client cannot see furnace contents, so any client price readout shows the one-furnace price. The 6 is the
  as-built number; the answer gives none (NQ-02-9).
- Files: `common/manipulation/flammeus/FurnaceVeinsShaping.java`, `common/event/TransientServerStateEvents.java`,
  `en_us.json` (`hemomancy.manipulation.furnace_veins.shaping.project`, `hemomancy.mnemonist.craft.shapings.mend.line2`),
  the Liber page `manipulations/pages/second_shapings.json`.
- Tests: `SecondShapingsGameTests.furnaceVeinsCooksABankWhileHeldAndGoesOutOnRelease` (assert blood paid scales with
  the furnaces fed), `furnaceVeinsRefusesAnEmptyBankUnpaid`, `FurnaceVeinsBurnTest` (unchanged), `ShapingGuardrailTest`,
  `CooldownTierGuardrailTest` (new session map).
- Docs: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/HEMOMANCY_REFERENCE.md`.
- Depends on: nothing. Chunk owning Shapings should know the file is touched.

### T5. Living veins move blood only toward a draw (size M to L)

- Rulings: CL-14.
- Change: replace the levelling rule. Today `tickNode` moves blood from the fuller tank to the emptier whenever the
  owner is within 32 blocks. New rule: blood moves only toward an end that is drawing, and only to replace what is
  being taken. What the design needs:
  1. A draw signal. `BlockBloodVolume` gets a `lastDrawn` game-time stamp, set when blood leaves the tank through
     `drain` or `subtractBloodVolume` (machine work, Absorption, a scab drip through `ScabDrip`, a basin's gutter
     delivery). I did not trace how a Blood Thrall takes blood from a tank, so the step plan must check that path.
     Clotting does not set it (it uses `setBloodVolume`). The vein's own move must not set it on the source, or the
     source would start drawing back.
  2. A drawing end is one whose tank was drawn within a short window (first value 100 ticks) and has room.
  3. Flow goes from the other end to the drawing end: at most `VEIN_FLOW_PER_SECOND` (20), never more than the source
     holds fresh or the target has room for. If both ends draw, the fuller feeds the emptier as today. If neither
     draws, nothing moves and no upkeep is paid.
  4. A dry machine never spends, so it never draws. The plan treats a sutured machine below its working reserve
     (`BloodRoutingRules.DEFAULT_WORKING_RESERVE`, 600 mL) as drawing too. Whether an unsutured machine below reserve
     draws is NQ-02-3. There is no marker in code that tells a machine from a store (`IBloodRoutingTarget` has no
     implementers), so option A there needs one small flag on `IBloodReservoir`.
  5. Unchanged: owner within 32 blocks, 1 mL/s upkeep only while blood moves, `BloodDrawRights` on both tanks,
     tending and withering, vein allowance by Degree, carried crossings.
  `GroundRunRules.veinTransfer` becomes a pure function of (fresh, room, capacity, drawing) for both ends.
- Files: `common/world/ground/GroundRunRules.java`, `common/world/ground/GroundNetwork.java` (`tickNode`),
  `common/tile/BlockBloodVolume.java` (stamp and NBT key), `common/tile/IBloodReservoir.java`,
  `common/block/harbinger/ground/LivingVeinBlock.java` (comment), `en_us.json`
  (`hemomancy.anchorite.craft.ground_marks.vein.line2` says "from the fuller tank to the emptier",
  `hemomancy.vicar.item_inquiry.earthen_vein.line2`), the Liber page `manipulations/pages/living_veins_and_basins.json`.
- Tests: `GroundRunRulesTest.aVeinLevelsTwoTanksWithoutOvershooting` (rewrite for the draw rule: no draw moves
  nothing, a draw pulls up to the flow, never past room). `GroundNetworkGameTests.aVeinMovesBloodOnlyWhileItsOwnerIsNear`
  (today it fills one tank and expects flow with nothing drawing, so it must drain the far tank first). New
  `aVeinWithNothingDrawingMovesNoBlood` and `aDripUnderANodeDrawsThroughTheVein`. `living_blood_validation` for the new
  stamp. Anchorite and Vicar lines follow their sheets.
- Docs: `docs/archive/tracks/core_loop/06-ground-network.md` (Tier 3 "Does (CL-14)" and the hazard "RF by another name"),
  `docs/archive/tracks/core_loop/03-living-blood.md`, `docs/CORE_LOOP.md` rulings, `docs/HEMOMANCY_REFERENCE.md`,
  `wiki/Blood-Systems.md`, `docs/OPEN_QUESTIONS.md` roll-up.
- Depends on: NQ-02-3 for point 4. The rest can be built first.

### T6. Water breaks gouges, not living veins (size M)

- Rulings: CQ-44.
- Change, gouges: a gouge stops being a water container. Water that flows into its cell, or a bucket poured onto it,
  replaces it, as before round two P8. Its lit clock is already dropped in `BloodGougeBlock.onRemove`
  (`GroundNetwork.forgetCell`). Water standing beside a gouge and rain do not break it in this plan (NQ-02-5).
- Change, veins: `LivingVeinBlock` keeps today's behaviour. It holds flowing water back and a source poured on it lies
  over it (`WATERLOGGED`). That is the cheapest reading of "not broken", and it makes the dear tier a deliberate dam.
  Water passing over a vein is not possible with vanilla waterlogging, which holds only a source (NQ-02-4).
- Shape of the code: move `SimpleWaterloggedBlock`, `getFluidState`, the water tick in `updateShape` and the
  water-on-last-face rule from `BloodGougeBlock` down to `LivingVeinBlock`, or keep the property on both and make the
  gouge's `placeLiquid` replace itself with the fluid. The step plan picks. Either way: `GroundNetwork.asGouge` must
  not carry `WATERLOGGED` (a waterlogged vein that withers is washed out and leaves water), `asVein` starts dry,
  `GroundNetwork.markAt` keeps its struck-water branch for veins, and `GroundMarkBlock.without` keeps leaving water.
- Files: `common/block/harbinger/ground/BloodGougeBlock.java`, `LivingVeinBlock.java`, `GroundMarkBlock.java`,
  `common/world/ground/GroundNetwork.java`, `common/command/CoreLoopFixtures.java` (gouge fixture),
  `src/gameTest/java/com/vincenthuto/hemomancy/gametest/GroundNetworkClientReview.java` (the water scene sets a
  waterlogged gouge), `tools/model_export/ground_network.py` only if the property leaves the gouge (blockstates are
  multipart on faces, so likely no change), `en_us.json` (`hemomancy.anchorite.craft.ground_marks.cut.line2` and
  `hemomancy.artificer.craft.hot_runs.feeder.line2` both say water will not wash a gouge out), the Liber page
  `manipulations/pages/the_ground_network.json`.
- Tests: replace `GroundNetworkGameTests.aGougeHoldsBackFlowingWater` with `flowingWaterWashesAGougeAway` and
  `aLivingVeinHoldsBackFlowingWater`. Move `aWaterloggedGougeFilledInLeavesItsWater` to a vein. New
  `aWitheredVeinUnderWaterIsWashedOut`. `ClientBlockstateCoverageTest` if the gouge loses the property.
- Docs: `docs/archive/tracks/core_loop/06-ground-network.md` (Tier 2 paragraph on water), `docs/CORE_LOOP.md` rulings,
  `docs/HEMOMANCY_REFERENCE.md` (P8 dated section and Track 6), `wiki/Blood-Systems.md`.
- Depends on: nothing. NQ-02-4 and NQ-02-5 can widen it.

### T7. Cap the kill share of region pressure (size S)

- Rulings: CB-26.
- Change: kills stop adding to a region once it holds a set amount. New `WorldAnswerRules.KILL_SHARE_CAP_ML`, first
  value 600 (the uneasy floor), and a pure `killCounted(regionMl, spill)` that returns
  `min(spill, max(0, cap - regionMl))`. `WorldAnswer.killed` uses it. Stains and chum are not capped, so heavy and
  seething still come from waste. This needs no second number per region in `BloodSpilledLedger`. The value is mine
  (NQ-02-7). At 600 mL pressure is 0.2 and Blood Moons come about one night in 9, against one in 14.5 quiet and one
  in 4 at seething.
- Files: `common/world/answer/WorldAnswerRules.java`, `common/world/answer/WorldAnswer.java`,
  `common/world/answer/BloodSpilledLedger.java` (a read of the region's settled total before the spill).
- Tests: `WorldAnswerRulesTest` (new cases for the cap), `DailyLoopGameTests.aKillByAnAwakeHarbingerBleedsIntoTheCount`
  (still true below the cap), new `killsAloneNeverLiftARegionPastUneasy`.
- Docs: `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md`, `docs/HEMOMANCY_REFERENCE.md`, `wiki/Blood-Systems.md`.
- Depends on: nothing.

### T8. Meal blood: tainted meat 10, raw fish 4 (size S)

- Rulings: CB-3.
- Change: `tainted_meat.json` `ml_per_nutrition` 20 to 10 (30 mL an item). `raw_fish.json` 3 to 4. Raw fish then
  equals cooked fish (4). This plan also raises `cooked_fish.json` to 5 so cooking still pays (NQ-02-8, part one).
  Tainted harvest stays 12 by CQ-37, which leaves it above tainted meat (NQ-02-8, part two).
- Files: `src/main/resources/data/hemomancy/blood_food/tainted_meat.json`, `raw_fish.json`, `cooked_fish.json`.
- Tests: `BloodFoodDataTest.cookedIsWorthMoreAndTaintedMeatTheMost` (tainted meat 10 still beats venison 8 per point,
  but the name no longer holds per item, so rename it). `DailyLoopGameTests.venisonCarriesAnimusAndTaintedMeatPaysTheMost`
  expects 60 mL and must expect 30, with a new name.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (line 162 says 20. The audit found another line that says 40),
  `wiki/Blood-Systems.md` line 246 ("tainted meat gives the most"), the Liber page
  `the_hematic_order/pages/the_body_and_the_day.json` if it names the order, `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md`.
- Depends on: nothing.

### T9. Record the rulings (size S)

- Rulings: every KEEP, DONE and PARKED row above, and the BUILD rows as they land.
- Change: docs only. Move each answered row from "Open questions" to "Rulings that bind" in `docs/CORE_LOOP.md` and in
  the roll-up `docs/OPEN_QUESTIONS.md` section 1.8. Mark CL-14 and CQ-44 as owner designs with their new wording. Fix
  the stale CN-7 line (three chum hunters, not only the Drowned). Note the Votary ruling in `docs/BRINED_COURT.md` if
  it lists where Votaries rise.
- Files: `docs/CORE_LOOP.md`, `docs/OPEN_QUESTIONS.md`, `docs/archive/tracks/core_loop/06-ground-network.md`,
  `docs/archive/tracks/core_loop/07-farming-and-water.md`, `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md`.
- Tests: none.
- Docs: as listed.
- Depends on: whichever reviewer owns the roll-up edit, so the same rows are not moved twice.

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-02-1 | CQ-7 | Whose blood is the feeder's 15 mL, and what happens when it cannot be paid? | A: the player who lit the run pays while online, alive and in the level. If not, the feeder does not feed and the run goes dark. B: the feeder gets its own small tank, filled by Projection, a gutter or a vein, and runs with the owner away until it is dry. | A, because the feeder has no tank today, RV-46 already ties the run to its lighter, and B is a machine that runs with its owner away. |
| NQ-02-2 | CQ-7, CB-24 | The two options in the accepted answer are far apart. 15 mL per catalyst is about 0.08 mL a second on bone meal (3,600 ticks). 1 mL per beat is 2 mL a second, twice base regeneration. Which? | A: 15 mL each time it feeds. B: 1 mL per beat while lit. | A as planned, then judge in PR-18. B makes a lit hallway cost more than the body makes. |
| NQ-02-3 | CL-14 | What counts as drawing at a vein's end? | A: blood taken out of that end's tank in the last 5 seconds by anything but the vein or clotting (machine work, Absorption, a drip, a gutter), and also a machine sitting below its 600 mL working reserve, so a dry machine can start. Stores (Venous Basin, Gourdvine Tap, vats) never pull by level. B: only blood actually taken. A dry machine must be primed by hand. C: any tank below its working reserve pulls, with no "taken" stamp. | A. B strands a dry machine. C still lets a Tap fill an idle basin to 600 mL, which is the siphon the ruling forbids. |
| NQ-02-4 | CQ-44 | What does a living vein do when water reaches it? | A: as built. It holds flowing water back and a poured source lies over it. B: flowing water passes over it, which needs a custom flowing-water state on the vein block. | A. It is built, and a dam fits the dearer tier. B is a block-behaviour project of its own. |
| NQ-02-5 | CQ-44 | Which water breaks a gouge? | A: only water that flows or is poured into its cell. Rain and water standing beside it do not. B: as stains: rain and water beside it too. | A. B makes gouges useless outdoors, and marking a route is their first job. |
| NQ-02-6 | CL-15 | Cloche tiers for specialty fungus: build them, and what does each tier grow? Today the cloche grows crops and stems and fruits enzymes from spores. It does not grow the eight strains (RV-63, ruled Keep). A tiered cloche under `docs/STATION_UPGRADES.md` needs: a row in `UpgradeStation`, the `stage` property on the Lantern block (it is a two-cell multiblock), `UpgradeableStation` on its block entity, two kits with sprites and recipes, a free-kit teacher, a personal practice proof per tier, two Working-floor rites with six offerings each, three bodies from `tools/model_export/mycelial_lantern.py`, Liber pages, teacher dialogue and the station GameTests. Undecided: what each tier grows, the Degrees, the teacher, the proofs, the names, and whether tiers replace the substrate rule. | A: park it until farming is played (PR-19), then write a design: base as built, stage 1 grows common-ground and flesh strains, stage 2 the Brined ones. B: design it now. C: drop the idea. | A. The answer says "maybe", the strain beds were just ruled Keep, and a third station on the upgrade rail is several sessions. |
| NQ-02-7 | CB-26 | How much pressure may kills supply? | A: kills cannot lift a region past 600 mL (uneasy). B: past 1,500 mL (heavy). C: no cap, halve `KILL_SPILL` to 3 as the tuning sheet suggested. | A. A mob farm then raises Blood Moons to about one night in 9, and heavy or seething need spilled blood. |
| NQ-02-8 | CB-3, CQ-37 | Two side effects of the new meal values. One: raw fish 4 equals cooked fish 4. Two: tainted meat (30 mL an item) falls under tainted harvest (36 mL) and steak (48 mL). | Part one. A: cooked fish to 5. B: leave them equal. Part two. A: leave both as ruled. B: lower tainted harvest to 8. | Part one A. Part two A, since both numbers are his rulings, but the GameTest and wiki line "tainted meat pays the most" must go. |
| NQ-02-9 | CQ-15 | What is the per-furnace price? | A: 6 mL a pulse for each furnace fed (a bank of 8 is 96 mL a second). B: 6 for the struck furnace and 3 for each other. | A. It is the built price applied per furnace and can be tuned in play. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CQ-7 | Feeder charges 15 mL per feed. | Build | T1, NQ-02-1, NQ-02-2 |
| CQ-43 | Four furnaces per hot run. | Build | T2, after T1 |
| CB-24 | Feed runs at 30 s a stage, once per crop. | Build | T3 |
| CQ-15 | Furnace Veins per furnace. | Build | T4, NQ-02-9 |
| CL-14 | Veins move blood only toward a draw. | Build | T5, NQ-02-3 |
| CQ-44 | Water breaks gouges, veins hold. | Build | T6, NQ-02-4, NQ-02-5 |
| CB-26 | Kill share cap. | Build | T7, NQ-02-7 |
| CB-3 | Meal values. | Build | T8, NQ-02-8 |
| CL-15 | Cloche tiers: a design, then a build on the station-upgrade rail. | Later | NQ-02-6, PR-19 |
| CN-7 | Track 10 leftovers: Wills reading recent spend, a Tenebris or Lux answer, spreading sod, bestiary world pages. | Parked | The CL rulings landing |
| CL-19 | The Parish. | Parked | Its own design after Stage C |
| PR-18 | Play the ground network. Judge with it: the feeder's price (NQ-02-2), a coal-fed hot run on four furnaces (one coal still cooks about 72 items against 8 in a furnace), vein flow under the draw rule. | Owner | T1, T2, T5, T6 built |
| PR-19 | Play farming. Judge with it: a 30 s feed run still matches the cloche's pace over many crops for almost no blood, so the cloche may still look pointless. | Owner | T3 built |
| PR-23 | Play the daily loop. Judge with it: the new meal values and Blood Moon frequency near a mob farm. | Owner | T7, T8 built |
| RV-66 | The sound audit of 2026-11-13 proposes a custom sound in place of the elder guardian curse. The Keep stands until the sound pass. | Later | The sound pass |
| KEEP rows | Record the rulings in `docs/CORE_LOOP.md` and the roll-up. | Build | T9 |
