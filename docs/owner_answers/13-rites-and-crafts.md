# 13. Cardinal Rites, Working crafts, rite floors and the Unstained ritual question

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P09). |
| T2 | Built (P09), option A: running rites, plain Projection only. |
| T3 | Built. P09: the Degree line. B25 (2026-11-15): the "You have already attained" line for a finished rank rite. |
| T4 | Built. P09: the refusal. B25: the visit path proven on a world with the Chamber (`chamberRiteReviewClient`, all checks pass). |
| T5 | Built (P09). |
| T6 | Built. P09: fill before repair. B25: the solo stall was the harness aiming at a full, damaged anchor before a dry one; fixed, `apotheosRiteWithoutRescue` required, rescues removed. |
| T7 | Built (P09). |
| T8 | Built. P09: the brazier offset. The other failure was rewritten by B02; B25 ran the suite: 22 of 22 pass. |
| T9 | Built (P09): the Lantern craft feeds 750 mL. |
| T10 | Built. P09: the motif and hub label. B25: the Floorless and Making tints, and the editor's merged preview. |
| T11 | Built. P09: the Bloodline Covenant. B25: Hungering Earth and Possession, and the no-shared-icon check. |
| T12 | Built (B25). 19 lines, one new page, the data test. |
| T13 | Built (B25). The test is flipped and renamed. |
| T14 | Not built. After a play session. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings leave the rite system as it is and close its known holes. Twelve rows need work, and most of it is small.
The largest pieces are the ordeal change for solo grand rites (PT-88), the recipe codec fix (FX-10) and the Liber
lines for rites with no entry (DS-4). The cost pass waits for play. Its data tables are in T14.

- F-12 cannot be closed by re-running its GameTest. The test is `riteLockedStationStillTakesProjectedBloodUntilF12` and
  it asserts the old behaviour. Projection still fills a rite-locked station.
- CB-12's "25ml" has two readings, a tick or a second. Nothing is planned for it.
- FX-13 is real. The test reads a brazier at offset (5, 1, 0) and the fixture puts the sample at (-3, 1, -1). The
  suite is opt-in, so nobody has seen it fail.
- FX-12 names one pair of rites that share an icon. There are three pairs.
- The Working roster is 18 crafts, not 20. Rites cost 200 to 752 mL in anchor blood (Apotheos 3008), so the Throne's
  8000 mL feed is more than ten times any of them but Apotheos.
- No file this plan touches changed after 2026-11-11 14:00. There are no conflicts.

I ran no test, build or client. Every "fails" or "passes" below is from reading.

Counts: 21 rows. BUILD 12, KEEP 3, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 2, OWNER 0, PARKED 4.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| DS-3 | Per-floor ceremony mechanics stay deferred. | PARKED | `docs/CARDINAL_RITES.md` "Open questions". Nothing in `common/rite/` builds Endure, Hold, Share or Precision. | none |
| DS-4 | Every rite with no Liber entry gets one line. | BUILD | `LiberEntryDefinitions` `registerRite` and `registerUpgradeLesson`. 49 of 91 rite files are mapped. 42 are not: 19 Harbinger rites, 18 Working crafts, 5 Unstained rites. The 2026-10-12 audit counted 22. Scope is NQ-13-5. | T12 |
| RV-22 | Keep the Rites & Crafts tab, special floors, the Omen as the only rite-bound member, the roster's tiers and feeds, the Loom at 3x2. | KEEP | `tags/*/rite_bound/*.json`: only `block/rite_bound/world` has a member, `sanguine_omen`. `working/somatic_loom.json` subject is 3 wide, 2 tall. `WorkingCraftRosterDataTest.ROSTER` pins 18 crafts. The sheet and `docs/CARDINAL_RITES.md` still say twenty. Mind Spike, Visceral Mirror and the Semi-Sentient Construct are gone and the Morphling Cradle was added. `HarbingerRecipeMapDefinitions.WORKING_CRAFTS` still lists `mind_spike`. | none |
| FX-10 | Fix `MapCodec.encode` dropping rite ceremony data. | BUILD | `CardinalRiteRecipeSerializer.encode`. It writes no `ceremony`, `floor`, `required_structure`, `brazier_signature`, `optional_offerings`, `pattern`, `key` or `subject`. Decode throws on a Harbinger rite without `ceremony`, so a round trip fails. Still applies. | T7 |
| FX-11 | Fix the five Rites & Crafts minors together. | BUILD | All five still apply. (1) `cardinal_floor_membrane.fsh` has no uniform for the floor's facing. (2) `RecipeMapPurposeGroup`: Floorless is `0xFFC0303A`, World reads the Dominion glow `0xFFE0303C`. Making reads `0xFFFF4F6E`, Bond `0xFFFFC2B0`. The first pair is close in value. I could not see the screen for the second. (3) `CardinalRiteRecipeSerializer` lines 115, 159, 160, 208, 307 read nested booleans with `getAsBoolean`, not `RecipeCodecJson.booleanField`. (4) `tools/skill_tree_editor/src/shared/types.ts` `RecipeMapTabKey = 'RITES' \| 'CRAFTING'`. The tool is git-ignored. (5) `RecipeMapCanvas` line 209 draws `screen.hemomancy.recipe_map_canvas.rites`, "Rites". | T7, T10 |
| FX-12 | The two rites that share one icon get their own. | BUILD | `HarbingerRecipeMapDefinitions` `iconRites`. The audited pair (2026-11-02 BK-22) is `sanctified_rite` (Rite of the Bloodline Covenant) and `armature_consecration`, both on `vicars_consecration_kit`. Still shared. Two more pairs share: `apotheos_rite` and `possession_rite` on `sanguine_quintessence`, `rooted_vein` and `hungering_earth` on `earthen_vein`. See NQ-13-4. | T11 |
| FX-13 | Check the succession test's brazier offset and fix it. | BUILD | `SuccessionGameTests.legacyVialsAreRejectedBeforeConsumption` line 397 reads `f.center.offset(5,1,0)`. `setup` places braziers at `floor.brazierSockets()` of `dominion_greater`. From `CardinalRiteGashRules.cells(3)` the first socket is (-3, 1, 1), world offset (-3, 1, -1). Nothing stands at (5, 1, 0), so the test throws on a null block entity. `succession_validation` is `alpha: false` in `gradle/game-tests.gradle`. Not run. | T8 |
| CB-12 | "25ml". Two readings, see NQ-13-1. | UNCLEAR | `HarbingerCardinalRiteEvents.CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK = 25.0`, paid once a tick with `BloodAccounting.pay` (source `rite_boundary`) while the caster is outside the leash. `CASTER_BOUNDARY_DAMAGE_PER_TICK = 1.0f` goes with it. Working rites pause and Unstained rites only warn. The leash is three times the ritual radius (`CardinalRiteBoundaryLeashRules`): at least 7.5 blocks at 3x3 and 34.5 at 9x9. | none |
| D-21, CB-15 | Run one cost and offering pass after play. Mycelial Lantern craft to 750. Check the Throne and the Effigy. | BUILD | `working/mycelial_lantern.json` `subject.feed` 2500. `working/covenant_throne.json` 8000. `working/vascular_effigy.json` 6500, which now makes `sanguine_vigil`. All pinned in `WorkingCraftRosterDataTest.ROSTER`. No pass has been run. Tables in T14. | T9, T14 |
| CN-2 | A running rite's ground refuses every player's plain Projection, not only the caster's. | BUILD | `CardinalRiteFootprintResolver.onOwnRiteGround(caster, pos)` looks up only the caster's rite. `BloodProjectionItem.projectFromEntity` lines 186 to 202 use it to skip `BloodInfusionManager.feedBlock` and `GroundNetwork.stain`. Another player's Projection still infuses the floor. | T2 |
| RV-43 | Keep the caster's own claim. | KEEP | Same code. GameTest `RiteKindCharacterisationGameTests.projectionDoesNotInfuseTheCastersOwnRiteGround`. Under CN-2 it becomes one case of the wider rule and its test stays as written. | T2 |
| RV-48 | Keep the three listed raw edits. | KEEP | `src/test/resources/architecture/player-block-edit-allow-list.txt` rows for `HarbingerCardinalRiteEvents.java` (collapse backlash), `EngramStampItem.java` and `CrimsonFireballEntity.java`. The list was edited on 11-13 and the rows are still there. | none |
| PT-32 | A player below a floor's rite is told the Degree it needs. | BUILD | `CardinalRiteActivation.tryStart` line 68 to 74 filters layered rites with `RecipeDegreeGates.playerMayAttempt` before matching. The Degree line at line 142 is never reached for a floor rite. The player gets `...the_floor_answers_but_no_structure`. | T3 |
| PT-67 | The Chamber rite refuses before completion when no visit can start. | BUILD | `CardinalRiteCompletionEffects` registers `cardinal_rite/chamber_of_will` with no refusal. `ChamberVisitService.beginRiteVisit` calls `attune` and only then `startVisit`. The line `chamber_visit.rite_unavailable` reads "The rite holds and you are attuned". | T4 |
| PT-68 | A Formation medium seats on a plain click when the floor wants one. | BUILD | `SanguineFormationItem.useOn` seats only on a sneak-click. A plain click calls `CardinalRiteActivation.tryStart`. Every Harbinger rite file names `living_staff` as its focus, so that call can never start one. 11 of 18 crafts take the Formation as medium. | T5 |
| PT-88 | Projection fills a dry anchor before it repairs it. | BUILD | Not moot. `CardinalRiteInteractionHandler.tryProject` lines 132 to 142 still pay the repair (`REPAIR_BLOOD_ML = 50`) before the fill. `CardinalRiteOrdealEngine` line 412 still applies deficit pressure each second. Probe `PlaythroughEndgameGameTests.apotheosRiteWithoutRescue` not run. | T6 |
| F-11, PT-34 | Blood-structure crafts count down once per server tick. | BUILD | `HarbingerCardinalRiteEvents.onLevelTick` line 102 calls the global `PendingBloodCraftManager.tick()` for every loaded level. Its `clear()` is wired (`TransientServerStateEvents`), so that half of the audit is stale. | T1 |
| F-12 | Re-run the GameTest, then close. The test cannot close it, see NQ-13-2. | UNCLEAR | `IBloodReservoir.takeProjectedBlood` does not ask `canReceiveBlood()`. Its comment says "F-12 (pending approval)". `BloodAccountingGameTests.riteLockedStationStillTakesProjectedBloodUntilF12` asserts the locked Centrifuge takes 100 mL. Round one covered routing, couriers and the ground network only (`ReservoirGateSourceTest`). | T13 |
| D-6 | Chimerization offering stays parked. | PARKED | `docs/ENDGAME_PATHS.md` line 101 and 124. No Java names chimerization. | none |
| D-11 | The Unstained ritual system stays parked. | PARKED | The 17 Unstained rite files carry no `purpose` or `floor`. `common/rite/unstained/` holds their own events. | none |
| DS-11 | Unstained invasion rite stays parked. | PARKED | Nothing in the repo. | none |

## Plan

### T1. Craft countdown once per server tick (size S)

- Rulings: F-11, PT-34
- Change: a pending Blood Structure craft counts down once per server tick. Give `PendingBloodCraftManager` a
  `tick(ServerLevel)` that advances only the crafts whose own level is ticking, and call it from
  `HarbingerCardinalRiteEvents.onLevelTick` beside `BloodStructureFeedManager.tick(sLevel)`. This keeps the one shared
  rite tick. A craft scheduled for N ticks then closes at N ticks with any number of dimensions loaded.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/event/PendingBloodCraftManager.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java`
- Tests: add a GameTest that schedules a craft with a 20 tick delay and asserts it has not closed at 10 server ticks and
  has by 21 (if three levels tick there, as row D-34 reports, the old code closes it near tick 7). Five GameTest files
  hurry crafts with `PendingBloodCraftManager.tick()` in a loop and need the level passed:
  `HarbingerJourneyFixtureGameTests`, `HematicStakeBloodStructureGameTests`, `MycelialSynapseGameTests`,
  `SuccessionGameTests`, `journey/HarbingerJourneyAutomation`. `PlaythroughNeophyteGameTests.proofLostWhenOfflineAtCollapse`
  shows the timing in its log.
- Docs: `docs/CARDINAL_RITES.md` (move F-11 to "Rulings that bind"), `docs/architecture/CONSOLIDATION_PROGRESS.md`
  (F-11 to DONE), `docs/PLAYTHROUGH_TESTS.md` row D-34, `docs/HEMOMANCY_REFERENCE.md` section 25.1 if it states a craft
  time.
- Depends on: chunk 10 PT-17 edits the same class (record a craft's proof by UUID). Build T1 first.

### T2. Rite ground refuses every player's Projection (size S)

- Rulings: CN-2, RV-43
- Change: replace the own-rite lookup with one over every running rite in the level. `CardinalRiteFootprintResolver`
  gains `onRiteGround(ServerLevel, BlockPos)`, which walks `CardinalRiteSavedData.get(level).getActiveRites()`, skips
  ended rites and uses the same cube as today (the larger of the ritual radius and the footprint radius, on x, y and
  z). `BloodProjectionItem` calls it for any server player. On that ground plain Projection neither infuses a block nor
  leaves a stain. Unchanged: anchors, the daemon, sigils, sinks, braziers and the Focus still take blood, helpers still
  feed through `CardinalRiteInteractionHandler.tryProject`, and mining still breaks the floor. How the two rulings fit:
  RV-43 is the caster's case of the CN-2 rule, so nothing is undone.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteFootprintResolver.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java`
- Tests: keep `projectionDoesNotInfuseTheCastersOwnRiteGround`. Add a twin in `RiteKindCharacterisationGameTests`
  (namespace `rite_kind_validation`) where a second captured player aims at the same floor block: no blood spent, the
  block stands, no stain. Add a control that the same aim outside the cube still infuses.
- Docs: `docs/CARDINAL_RITES.md` (move the P2 call to "Rulings that bind" with the wider rule),
  `docs/HEMOMANCY_REFERENCE.md` section 5.3 (the "caster's own running rite" sentence), `docs/MANIPULATIONS_AND_CASTING.md`
  if it lists what Projection lands on.
- Depends on: nothing. NQ-13-3 asks how far the claim should reach. The plan builds option A.

### T3. Degree line at a floor the player is too low for (size S)

- Rulings: PT-32
- Change: in `CardinalRiteActivation.tryStart`, when the click is on a Cardinal Focus and the first resolution finds no
  rite, resolve once more over the rites that `mayInitiate` allows but `playerMayAttempt` filtered out. If one matches
  the floor, structure, braziers and medium and the player's Degree is below it, show the existing line
  `message.hemomancy.cardinal_rite_activation.this_rite_requires` with `RecipeDegreeGates.degreeLabel`. With several
  matches use the lowest Degree. The floor line stays for every other case, the Mnemonic Reliquary's lesson gate
  included. The same second pass can restore the unreachable "you have already attained" line for a finished rank rite
  (the D-32 family logged in `PlaythroughSanctifiedGameTests` and `PlaythroughIlluminatusGameTests`). That part is
  beyond the answer. Drop it if the owner wants only the Degree line.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteActivation.java`
- Tests: `PlaythroughAdeptGameTests.bloodlineFoundingOpensAtD5` and `foundingFaneOpensAtD5` assert the Degree line for
  the D4 player in place of logging it. `PlaythroughEndgameGameTests` line 686 and `PlaythroughIlluminatusGameTests`
  line 1173 accept the floor line today and tighten with it. `RiteKindCharacterisationGameTests` gets a D4 caster on a
  D5 floor.
- Docs: `docs/CARDINAL_RITES.md`, `docs/PLAYTHROUGH_TESTS.md` row D-32, `docs/HEMOMANCY_REFERENCE.md` section 5.3
  (activation order).
- Depends on: chunk 11 PT-38 (warn at activation for the Unbinding) edits the same method. Either order works.

### T4. The Chamber rite refuses when no visit can start (size S)

- Rulings: PT-67
- Change: give `cardinal_rite/chamber_of_will` a refusal in `CardinalRiteCompletionEffects`, read from a new public
  `ChamberVisitService` check built on its private `canStartVisit` (a visit already running, a projection or possession
  in progress, standing in the Chamber, or no Chamber level). At activation the rite does not start and nothing is
  escrowed. At the finale the rite waits, as every refused finale does (`FINALE_HOLD_TICKS`), until a visit can start.
  `beginRiteVisit` checks first and attunes only when the visit begins, which also brings the rite route in line with
  the AGENTS.md rule that Chamber entry checks overlap and destination before it grants a binding. Reword
  `message.hemomancy.chamber_visit.rite_unavailable`, which today says "you are attuned".
- Files: `src/main/java/com/vincenthuto/hemomancy/common/encounter/ChamberVisitService.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteCompletionEffects.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `PlaythroughSanctifiedGameTests.chamberRiteAttunesWithoutVisit` becomes required. Its sibling
  `chamberRiteSaysWhyNoVisit` asserts "attuned (unchanged)" after a full rite and must be rewritten: the Staff is
  refused at the Focus, the refusal line is said, no rite runs, the Staff and offerings are kept. A second check at
  line 345 of the same class expects the old line after a played rite and changes with it.
  `HarbingerJourneyFixtureGameTests` line 1759 and `journey/HemoJourneyController` call `beginRiteVisit` directly and
  expect a visit. They should stay green where a Chamber is loaded.
- Docs: `docs/CARDINAL_RITES.md`, `docs/PLAYTHROUGH_TESTS.md` rows D-67 and LF-12, `docs/HEMOMANCY_REFERENCE.md`
  section 5.7a, `wiki/Harbinger-Path.md` if it describes the rite.
- Depends on: nothing.

### T5. A Formation seats on a plain click (size S)

- Rulings: PT-68
- Change: in `SanguineFormationItem.useOn`, a plain click on a Cardinal Focus seats the Formation when the Focus is
  empty, no rite runs there and the floor wants one. "Wants one" is read from data: the floor formed at the Focus
  (`CardinalRiteStationMatcher.formedFloor`) is accepted by at least one rite whose `medium` is the Sanguine Formation.
  Today that is every Working floor tier. Seating goes through the Focus's own insert path so the message and the
  extract rule stay shared. Everything else keeps today's behaviour: the sneak-click still seats on any floor, and a
  plain click on a floor that does not want a Formation still tries the floor.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/SanguineFormationItem.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/CardinalFocusBlock.java`
- Tests: `PlaythroughSanctifiedGameTests.formationSeatsOnPlainClick` becomes required. Add a control on a Threshold
  floor: the plain click does not seat. `PlaythroughRites.seatMedium` can go back to a plain click for Formation media.
- Docs: `docs/CARDINAL_RITES.md` (History row for 2026-11-02 says sneak-click), `docs/PLAYTHROUGH_TESTS.md` row D-68,
  `docs/HEMOMANCY_REFERENCE.md` section 5.3.
- Depends on: nothing.

### T6. Fill before repair in the ordeal (size M)

- Rulings: PT-88
- Change: in `CardinalRiteInteractionHandler.tryProject`, an anchor that is both damaged and below its requirement
  takes the fill first. Repair is paid only once the anchor is full. No number changes: the repair stays 50 mL and the
  pressure stays once a second. By reading, this breaks the loop: deficit pressure comes only from missing blood, and
  `areAnchorsConsecrated` reads blood only, so a filled ring lets the wave clear while repairs follow. The probe
  decides whether it is enough.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java`
- Tests: `PlaythroughEndgameGameTests.apotheosRiteWithoutRescue` becomes required if it passes. Then remove the logged
  rescue (`PlaythroughRites.stabilizeWhenDeadlocked` with `rescues`) from `apotheosRiteByPlay`, from the Bloom rite in
  `PlaythroughArchonGameTests` (line 305) and from its second use in `PlaythroughEndgameGameTests` (line 1844). Add a
  `rite_kind_validation` case: a dry, damaged anchor takes
  Projection as fill and its repair counter does not move.
- Docs: `docs/CARDINAL_RITES.md`, `docs/PLAYTHROUGH_TESTS.md` row D-88 and the harness notes on CHAIN-1,
  `docs/HEMOMANCY_REFERENCE.md` section 5.3 (instability paragraph), `wiki/Harbinger-Path.md` "Cardinal Rites".
- Depends on: nothing. If the probe still fails, the next levers are the other options on the row (press only anchors
  not under repair, or a cheaper repair at grand rites). That goes back to the owner.

### T7. Rite recipe codec writes everything it reads (size M)

- Rulings: FX-10, FX-11 (nested booleans)
- Change: `CardinalRiteRecipeSerializer.encode` writes `ceremony`, `floor`, `required_structure`, `brazier_signature`,
  `optional_offerings`, `pattern`, `key` and `subject`, so decode of its own output gives the same recipe. The five
  nested boolean reads (socket `required`, atmosphere `lightning` and `dome`, both `consume_on_success`) go through
  `RecipeCodecJson.booleanField` so a recipe read back from NBT keeps them.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/CardinalRiteRecipeSerializer.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/RecipeCodecJson.java`
- Tests: a GameTest over every loaded rite: encode with JSON ops and with NBT ops, decode, and compare the stream-codec
  bytes of the original and the copy. It needs registries, so it is a GameTest, not a JVM test. Keep
  `CardinalRitePurposeDataTest`, `CardinalRiteLayeredRecipeDataTest` and `WorkingCraftRosterDataTest` green.
- Docs: `docs/CARDINAL_RITES.md` (drop the two lines from "Open questions" and "Proof owed"). No player-facing change.
- Depends on: nothing. Nothing in play calls `encode` today as far as I could find, so this is correctness, not a live
  bug. I did not search datagen or commands for callers.

### T8. Succession test reads the right brazier (size S)

- Rulings: FX-13
- Change: `legacyVialsAreRejectedBeforeConsumption` takes the donated sample from the brazier the fixture filled first
  (`floor.brazierSockets().get(0)`, placed with the fixture's own offset rule), not from a fixed offset. Run the whole
  `succession_validation` suite once, since it is opt-in and may hide more drift.
- Files: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SuccessionGameTests.java`
- Tests: the test itself. No mod code changes unless the run shows the refusal is broken.
- Docs: `docs/CARDINAL_RITES.md` "Proof owed".
- Depends on: T1 touches one line in the same file.

### T9. Mycelial Lantern craft to 750 mL (size S)

- Rulings: D-21, CB-15
- Change: `subject.feed` 2500 to 750 in the Lantern's Working craft. At 750 it sits under the two D4 lesser crafts
  (Thurible 1000, Pylon 1200). The Throne and the Vigil are not changed here.
- Files: `src/main/resources/data/hemomancy/recipe/cardinal_rite/working/mycelial_lantern.json`,
  `src/test/java/com/vincenthuto/hemomancy/common/recipe/WorkingCraftRosterDataTest.java` (the `ROSTER` row)
- Tests: `WorkingCraftRosterDataTest`. `WorkingCraftGameTests` if it feeds the Lantern by amount.
- Docs: `docs/HEMOMANCY_REFERENCE.md` section 25.1 craft table, `docs/CARDINAL_RITES.md` (roster count 18),
  `wiki/Harbinger-Path.md` if it quotes the feed.
- Depends on: chunk 10 PT-5 edits the same `ROSTER` map (Monolith craft to D5). Chunk 01 CB-13 cuts the Lantern's
  running price. Chunk 02 CL-15 may give the Lantern tiers as an upgradable station. If tiers come, 750 is the base
  craft and each tier's rite joins the table in T14.

### T10. Rites & Crafts minors (size M)

- Rulings: FX-11 (the other four)
- Change:
  - Qliphoth membrane: pass the floor's facing to `cardinal_floor_membrane` and turn the motif with it. The Rite of
    Possession borrows this floor, so it gains too. Check the Apotheos and Ancestral motifs in the same launch.
  - Tints: give the Floorless and Making groups tints that read apart from World and Bond on the map. The values are a
    look call for the client. Stay inside the blood, bone and hematic iron family (no copper or brass).
  - Hub label: the merged Harbinger map shows its own key, "Rites & Crafts" or a short form that fits the 42 px hub.
    The Unstained map keeps "Rites". Pass a `Component`, not the `"Craft"` and `"Rites"` strings.
  - Skill-tree editor: one merged map preview in place of the Rites and Crafting tabs. The tool is local and
    git-ignored, so this is a convenience for whoever edits the map.
- Files: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh`,
  `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorMesh.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/CardinalFloorRenderTypes.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/RecipeMapPurposeGroup.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/RecipeMapCanvas.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`, `tools/skill_tree_editor/src/shared/types.ts`,
  `tools/skill_tree_editor/src/frontend/recipeMaps.ts`
- Tests: `RecipeMapPurposeGroupDataTest`, `RenderTypeCachingGuardrailTest`, `ShaderNoiseIncludeGuardrailTest`,
  `HardCodedEnglishGuardrailTest`. The shader needs a client launch: `riteFloorReviewClient` with
  `riteFloorReviewSpecial`, floor built at two facings.
- Docs: `docs/CARDINAL_RITES.md` "Proof owed" (remove the fixed minors).
- Depends on: nothing.

### T11. Own icons for rites that share one (size S)

- Rulings: FX-12
- Change: the Rite of the Bloodline Covenant (`sanctified_rite`) stops using the Vicar's Consecration Kit, which is
  Armature Consecration's own offering. Proposed stand-in: `enthralling_filament`, one of its three offerings. If
  NQ-13-4 is answered yes: `hungering_earth` moves to `sanguine_formation` (its offering) and `possession_rite` to
  `adipocere` (one of its optional offerings). These are existing items. "New icon" could also mean new art, which the
  plan does not assume.
- Files: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java`
- Tests: add a small JVM check that no two rite icons in the table name the same item, beside
  `RecipeMapPurposeGroupDataTest`.
- Docs: none.
- Depends on: NQ-13-4 for the two extra pairs.

### T12. One Liber line for each rite with no entry (size M)

- Rulings: DS-4
- Change: each of the 19 Harbinger rites below gets one line of Liber text and a `registerRite` row, so completing the
  rite opens the page that holds its line. Proposed homes, all lore calls the owner can redirect:
  - Seven Puppeteer ordeals (`puppeteer_trial_cinder_bellows`, `_gorebound_hulk`, `_marrow_spitter`,
    `_mnemonist_puppet`, `_sanguine_hound`, `_scarlet_mummer`, `_veinwing_vulture`): the matching
    `the_hematic_order/pages/troupe_<summon>` page, which exists for all seven and opens by dialogue only today.
  - Vessel rites (`pallid_vessel_rite`, `crimson_vessel_rite`, `ashen_vessel_rite`): one new short page in
    `the_hematic_order`, three lines. No gourd page exists.
  - `bloodline_recall`, `covenant_vigil`, `eternal_covenant`: `the_infection/pages/blood_memories` or
    `the_hematic_order/pages/historical_record`, beside the bloodline rites already mapped there.
  - `hematic_succession`, `mnemonic_restoration`: `the_infection/pages/mnemonic_ambergris`.
  - `armature_consecration`, `monolithic_armature`: `the_hematic_order/pages/bloodwork_stations`. The other twelve
    station upgrades have lesson pages. The Armature's two do not.
  - `possession_rite`, `visitation_rite`: `the_infection/pages/the_truth` and `the_infection/pages/the_entity`. These
    two touch the 2026-10-31 rulings (Silentium is cut from the hive), so the wording needs the owner's eye.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java`,
  pages under `src/main/resources/data/hemomancy/books/libersanguinium/`, `src/main/resources/assets/hemomancy/lang/en_us.json`
  if the pages read from keys.
- Tests: `LiberPageRegistrationGuardrailTest`, `ProgressionResourceIntegrityTest`, `ReleaseResourceIntegrityTest`. Add a
  data test that every Harbinger rite file under `recipe/cardinal_rite/` (not `working/`, not Unstained) has a
  `registerRite` row.
- Docs: `docs/CARDINAL_RITES.md`, `docs/HEMOMANCY_REFERENCE.md` Liber section, `docs/LORE_REFERENCE.md` only if a line
  states new lore.
- Depends on: NQ-13-5 (whether the 18 crafts and 5 Unstained rites are in scope). Chunk 16 owns Liber and canon rows
  and should see the lines. I did not read how HutosLib pages lay out text, so "one line on an existing page" is a
  guess at the cheapest shape.

### T13. Projection honours the rite lock (size S, waits on NQ-13-2)

- Rulings: F-12
- Change: `IBloodReservoir.takeProjectedBlood` returns 0 when `canReceiveBlood()` is false, the one-line change the
  consolidation ledger wrote down. A station held by an upgrade rite then refuses Projection from anyone. The rite's
  own circuit feeding is not touched: it runs in `CardinalRiteInteractionHandler.tryProject` before plain Projection.
  The Sanguine Condenser returns false for `canReceiveBlood` at all times. Its own Projection path
  (`SanguineCondenserProjection`) runs earlier in `BloodProjectionItem`, and the build must check it never falls
  through to the default. Stations that override `takeProjectedBlood` (Forge, Loom, Monolith, Ring Door, Mason's
  Effigy) need a look each.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/tile/IBloodReservoir.java`
- Tests: flip and rename `BloodAccountingGameTests.riteLockedStationStillTakesProjectedBloodUntilF12` (batch
  `blood_accounting`): the locked Centrifuge takes 0 and the player keeps 1000 mL. `StationUpgradeGameTests` and
  `LivingBloodGameTests.projectionFreshensTheTank` stay green. Add the lock case for a station with its own override.
- Docs: `docs/architecture/CONSOLIDATION_PROGRESS.md` (F-12 to DONE), `docs/STATION_UPGRADES.md`,
  `docs/HEMOMANCY_REFERENCE.md` station upgrade section.
- Depends on: NQ-13-2.

### T14. Cost and offering pass over rites and crafts (size L, after play, not to start yet)

- Rulings: D-21, CB-15
- Change: none yet. The owner plays first. The pass then sets feeds, anchor costs and offerings in one sitting. It
  starts from the tables below, read from the recipe files on 2026-11-13.
- Files: `src/main/resources/data/hemomancy/recipe/cardinal_rite/*.json`,
  `src/main/resources/data/hemomancy/recipe/cardinal_rite/working/*.json`,
  `src/main/resources/data/hemomancy/recipe/blood_structure/*.json`,
  `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteRingRules.java` and
  `CardinalRiteCeremonyRules.java` if anchor prices move.
- Tests: `WorkingCraftRosterDataTest` (pins every feed), `CardinalRitePurposeDataTest`, `rite_kind_validation`,
  `WorkingCraftGameTests`.
- Docs: `docs/HEMOMANCY_REFERENCE.md` sections 5.3 and 25.1, `docs/CARDINAL_RITES.md`, `docs/STATION_UPGRADES.md`
  offering tables, `wiki/Harbinger-Path.md`.
- Depends on: a play session. T9. Chunk 02 CL-15. Chunk 10 PT-5.

How a rite is priced today. Every Harbinger rite file has `bloodCost` 0 except the seven Puppeteer ordeals. A rite's
blood is its anchors: 50 mL each on the first ring and a quarter more per ring out (63, 75, 88, 100, 113, 125, 138),
from `CardinalRiteRingRules.bloodForRank`. That gives 200 mL for one ring, 452 for two, 752 for three and 3008 for
Apotheos's eight. Repairs (50 mL each) and wave drains come on top. A clean rite with an awakened Reservoir sigil
returns half (`CardinalRiteCompletion.refundCleanRite`). Station upgrade rites also pay their circuits, 50 mL each and
250 for the Forge by the 2026-11-11 audit. I did not count circuits per rite.

Working crafts (18). Feed is held Projection into the subject.

| Craft (file) | Tier | D | Feed mL | Medium | Offerings |
| --- | --- | --- | --- | --- | --- |
| `scarlet_vanity` | minor | 3 | 300 | sanguine_formation | dried_gourd, leather |
| `resonant_forge` | minor | 3 | 500 | ferric_binder | wax_cylinder |
| `somatic_loom` | lesser | 3 | 150 | sanguine_formation | string, neurotic_enzyme |
| `morphling_cradle` | lesser | 4 | 200 | sanguine_formation | spore_sac |
| `sporitic_thurible` | lesser | 8 | 1000 | spore_sac | chain, brown_mushroom |
| `fungal_implantation_pylon` | lesser | 4 | 1200 | hyphal_substrate | neurotic_spores, bone_meal |
| `mycelial_lantern` | lesser | 3 | 2500 (750 after T9) | ferric_binder | brown_mushroom, glowstone_dust |
| `mnemonic_reliquary` | greater | 3 | 75 | sanguine_formation | book |
| `runic_chisel_station` | greater | 4 | 100 | sanguine_formation | flint, iron_ingot |
| `mason_effigy` | greater | 4 | 150 | runic_motif_paper | 2x #flowers |
| `sanguine_monolith` | greater | 5 | 175 | sanguine_formation | sanguine_quintessence, echo_shard |
| `morphling_incubator` | greater | 5 | 200 | morphling_polyp | swollen_leech, slime_ball |
| `somnolent_seat` | greater | 3 | 250 | sanguine_formation | 2x blood_crystal, 2x ender_pearl |
| `vacant_effigy` | greater | 5 | 250 | sanguine_formation | armor_stand, blood_stained_stone |
| `dendritic_distributor` | greater | 5 | 500 | sanguine_formation | sinew_cord, ferric_binder |
| `enzymatic_scriptorium` | greater | 3 | 500 | ferric_binder | enzyme_primer, blood_crystal_shard, lapis_block |
| `covenant_throne` | greater | 6 | 8000 | sanguine_formation | sanguine_quintessence, blood_crystal, crying_obsidian |
| `vascular_effigy` (makes the Sanguine Vigil) | grand | 6 | 6500 | sanguine_formation | vascular_poultice, sinew_cord, sanguine_quintessence |

What stands out for the check. The other nine greater crafts feed 75 to 500 mL, so the Throne is 16 times the next
highest. The Vigil is the only grand craft, so it has no peer to compare with. Both feeds are more than the 4900 mL a
D6 test player holds (`docs/PLAYTHROUGH_TESTS.md` row D-66), so both need a refill mid-feed. Two of the three D3
greater crafts (Reliquary 75, Seat 250) cost less than the D3 minor Forge (500). Thirteen of the 18 still carry the stage 1 spec's
first-pass offerings (the spec marks them "new"). The Forge, Scriptorium, Seat and Mason's Effigy kept older ones. The
Cradle came after the spec and I did not trace its offering.

Harbinger rites (49), by Degree. "Braziers" is how many lit braziers the signature needs.

| Rite (file) | Purpose | Floor | D | Anchors (rings) | Anchor blood mL | Braziers | Offerings | Medium |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `sanguine_attunement` | self | threshold_minor | 2 | 4 (1) | 200 | 0 | none | none |
| `crimson_beacon` | self | threshold_lesser | 2 | 4 (1) | 200 | 1 | compass | none |
| `initiate_rite` | self | threshold_lesser | 2 | 4 (1) | 200 | 1 | iron_nugget | none |
| `rooted_vein` | world | dominion_lesser | 2 | 4 (1) | 200 | 1 | blood_rock | none |
| `vascular_mending` | self | threshold_lesser | 2 | 4 (1) | 200 | 1 | glistering_melon_slice | none |
| `exsanguination` | making | working_lesser | 3 | 4 (1) | 200 | 1 | hematic_iron_powder | none |
| `first_clotting` | making | working_lesser | 3 | 4 (1) | 200 | 6 | clotting_lattice, hematic_iron_scrap, befouling_ash_trail, venous_stone, blood_stained_stone, sanguine_formation | none |
| `hungering_earth` | world | dominion_lesser | 3 | 4 (1) | 200 | 1 | sanguine_formation | none |
| `pallid_vessel_rite` | making | working_lesser | 3 | 4 (1) | 200 | 1 | dried_gourd | none |
| `sanguine_brotherhood` | self | threshold_lesser | 3 | 4 (1) | 200 | 1 | hematic_iron_powder | none |
| `sanguine_fervor` | world | dominion_lesser | 3 | 4 (1) | 200 | 1 | dried_leech | none |
| `first_condensation` | making | working_lesser | 4 | 4 (1) | 200 | 6 | hematic_condenser_kit, glass_bottle, blaze_powder, sanguine_glass, ferric_binder, vivacious_enzyme | none |
| `hematic_fortification` | self | threshold_lesser | 4 | 4 (1) | 200 | 1 | hematic_iron_powder | none |
| `crimson_vessel_rite` | making | working_greater | 4 | 4 (1) | 200 | 2 | blood_gourd_white, amethyst_shard | none |
| `illuminatus_rite` | self | threshold_greater | 4 | 4 (1) | 200 | 2 | hematic_memory, glowstone_dust | none |
| `sanguine_eclipse` | world | dominion_greater | 4 | 4 (1) | 200 | 2 | sanguine_quintessence, clock | none |
| `steady_separation` | making | working_greater | 4 | 4 (1) | 200 | 6 | centrifugal_governor_kit, iron_ingot, chain, sanguine_glass, ferric_binder, neurotic_enzyme | none |
| `bloodline_founding` | bond | communion_lesser | 5 | 8 (2) | 452 | 1 | blaze_powder | none |
| `bloodline_recall` | bond | communion_lesser | 5 | 4 (1) | 200 | 1 | ender_pearl | none |
| `armature_consecration` | making | working_greater | 5 | 4 (1) | 200 | 6 | vicars_consecration_kit, armor_stand, chain, sinew_cord, hemolytic_plating, sanguine_formation | none |
| `ashen_vessel_rite` | making | working_greater | 5 | 4 (1) | 200 | 2 | blood_gourd_red, ghast_tear | none |
| `druse_crystallization` | making | working_greater | 5 | 8 (2) | 452 | 6 | druse_heart, amethyst_shard, blood_crystal_shard, sanguine_glass, hematic_iron_block, sanguine_quintessence | none |
| `eightfold_script` | making | working_greater | 5 | 4 (1) | 200 | 6 | rubricators_quill, ink_sac, feather, lapis_block, cuttlefish_chromatophores, runic_motif_paper | none |
| `founding_fane` | world | dominion_grand | 5 | 8 (2) | 452 | 1 | nautilus_shell | sanguine_quintessence |
| `ganglionic_weave` | making | working_greater | 5 | 4 (1) | 200 | 6 | ganglionic_harness, neurotic_enzyme, nerve_fiber, enthralling_filament, sanguine_formation, blood_crystal_shard | none |
| `hematic_succession` | world | dominion_greater | 5 | 4 (1) | 200 | 3 | 2x bloody_vial, mnemonic_ambergris | sanguine_quintessence |
| `hematic_unbinding` | bond | communion_lesser | 5 | 8 (2) | 452 | 1 | nether_star | none |
| `mnemonic_restoration` | world | dominion_greater | 5 | 4 (1) | 200 | 2 | bound_mnemonic_remnant, bloody_vial | sanguine_quintessence |
| `sanctified_rite` | self | threshold_greater | 5 | 8 (2) | 452 | 3 | sanguine_quintessence, enthralling_filament, amethyst_shard | none |
| `sanguine_dominion` | world | dominion_greater | 5 | 8 (2) | 452 | 1 | clay_ball | none |
| `scarlet_summons` | bond | communion_greater | 5 | 8 (2) | 452 | 1 | book | none |
| `true_groove` | making | working_greater | 5 | 4 (1) | 200 | 6 | precision_governor_kit, grindstone, honeycomb, amethyst_shard, wax_cylinder, hematic_iron_block | none |
| `cortical_weave` | making | working_greater | 6 | 8 (2) | 452 | 6 | cortical_crown, nerve_bundle, synaptic_node, mnemonic_ambergris, blood_stained_stone, sanguine_glass | none |
| `covenant_vigil` | bond | communion_greater | 6 | 12 (3) | 752 | 2 | hematic_memory, enthralling_filament | none |
| `eternal_covenant` | self | threshold_greater | 6 | 12 (3) | 752 | 1 | rabbit_foot | none |
| `sanguine_athanor` | making | working_greater | 6 | 8 (2) | 452 | 6 | sanguine_athanor_kit, brewing_stand, ghast_tear, sanguine_quintessence, chromatic_sublimate, hemolytic_solution | none |
| `second_fraction` | making | working_greater | 6 | 8 (2) | 452 | 6 | fractionating_rotor_kit, amethyst_shard, ghast_tear, hematic_iron_block, chromatic_sublimate, enzyme_primer | none |
| `archon_rite` | self | threshold_grand | 6 | 12 (3) | 752 | 5 | sanguine_quintessence, hematic_memory, fervent_husk, echo_shard, ender_eye | none |
| `chamber_of_will` | self | threshold_grand | 6 | 8 (2) | 452 | 1 | echo_shard | none |
| `horn_of_culmination_rite` | making | working_grand | 6 | 4 (1) | 200 | 2 | blood_gourd_black, dragon_breath | none |
| `pallid_shadow` | world | dominion_grand | 6 | 12 (3) | 752 | 1 | glow_ink_sac | none |
| `enduring_pattern` | making | working_greater | 7 | 8 (2) | 452 | 6 | master_cam_kit, anvil, netherite_scrap, mnemonic_ambergris_block, chalybeate_sclerite, suspended_blood_crystal | none |
| `monolithic_armature` | making | working_greater | 7 | 8 (2) | 452 | 6 | monolithic_cornerstone, netherite_scrap, monolith_fragment, monolith_imbued_cloth, barbed_splinter, sanguine_quintessence | none |
| `palimpsest` | making | working_greater | 7 | 8 (2) | 452 | 6 | palimpsest_burin, echo_shard, glow_ink_sac, mnemonic_folio, axonal_slate, myelin_sheath | none |
| `ancestral_communion` | bond | ancestral_grand | 7 | 12 (3) | 752 | 1 | bone | none |
| `apotheos_rite` | self | apotheos_grand | 7 | 32 (8) | 3008 | 1 | charcoal | none |
| `bloom_of_qliphoth` | world | qliphoth_grand | 7 | 12 (3) | 752 | 1 | amethyst_shard | qliphoth_seed |
| `possession_rite` | bond | qliphoth_grand | 8 | 4 (1) | 200 | 0 | none (optional: mnemonic_ambergris, hematic_iron_block, totem_of_undying, adipocere) | sanguine_quintessence |
| `visitation_rite` | bond | communion_lesser | 8 | 4 (1) | 200 | 2 | 2x spore_sac | none |

Things the pass should look at in this table. The D2 to D4 rites all cost 200 mL whatever they do. Anchor count
follows the authored ceremony, not the floor tier: the grand Curved Horn rite costs 200 and the lesser Bloodline
Founding 452. Several offerings are single vanilla items (clay ball for Sanguine Dominion, rabbit's foot for Eternal
Covenant, a book for Scarlet Summons, charcoal for Apotheos). Three files have a `riteType` that does not match their
floor's tier (`founding_fane` greater on a grand floor, `hematic_unbinding` greater on a lesser floor,
`possession_rite` lesser on a grand floor). The first two are in the 2026-10-12 audit. All three were fixed on 2026-11-18
(see `docs/CARDINAL_RITES.md`).

Puppeteer ordeals (7). These are the only rites with a `bloodCost`.

| Trial (file) | D | Blood cost mL | Offerings |
| --- | --- | --- | --- |
| `puppeteer_trial_veinwing_vulture` | 3 | 500 | veinwing_harness |
| `puppeteer_trial_marrow_spitter` | 3 | 750 | marrow_spitter_carriage |
| `puppeteer_trial_sanguine_hound` | 4 | 850 | sanguine_hound_bridle |
| `puppeteer_trial_cinder_bellows` | 4 | 900 | cinder_bellows_frame |
| `puppeteer_trial_scarlet_mummer` | 4 | 950 | scarlet_gorget |
| `puppeteer_trial_gorebound_hulk` | 4 | 1100 | gorebound_yoke, sanguine_quintessence |
| `puppeteer_trial_mnemonist_puppet` | 5 | 1400 | mnemonist_cradle, mnemonic_ambergris, memory_thread |

Floorless Blood Structures (11). No offerings by rule.

| Structure (file) | D | Blood cost mL | Held item | Path |
| --- | --- | --- | --- | --- |
| `dried_gourd` | 0 | 150 | gourd_seeds | Harbinger |
| `hematic_iron_block` | 1 | 50 | ink_sac | Harbinger |
| `iron_brazier` | 1 | 100 | smouldering_ash_trail | Harbinger |
| `liber_sanguinum` | 1 | 100 | sanguine_formation | Harbinger |
| `living_staff` | 1 | 150 | sanguine_formation | Harbinger |
| `vial_centrifuge` | 1 | 150 | ferric_binder | Harbinger |
| `ghastly_alembic` | 2 | 100 | sanguine_formation | Harbinger |
| `hematic_stake` | 5 | 100 | sanguine_formation | Harbinger |
| `mycelial_synapse` | 8 | 120 | sanguine_formation | Harbinger |
| `pallid_retort` | 2 | 0 | pale_distillate | Unstained |
| `unstained_pillar` | 1 | 50 | glowstone_dust | Unstained |

The 17 Unstained rite files all have `bloodCost` 0, no floor and no anchors. They are outside this pass (D-11).

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-13-1 | CB-12 | You wrote "25ml" for the rite boundary drain. The code drains 25 mL every tick, which is 500 mL a second, and deals 1 damage a tick with it. Which did you mean? | A: 25 mL a tick, as built. No change. B: 25 mL a second, a twentieth of today and half the 50 mL/s the sheet offered. | A, because "25 mL a tick" is the figure the row itself quoted. Nothing is planned until you say. |
| NQ-13-2 | F-12 | The GameTest named for F-12 passes today, and what it proves is that a rite-locked station still takes projected blood. Re-running it cannot close the row. Approve the one-line change so Projection honours the lock? | A: approve. `takeProjectedBlood` refuses while the station is rite-locked and the test flips (T13). B: keep Projection through the lock, rename the test and close the row as intended behaviour. | A, because `docs/STATION_UPGRADES.md` treats the lock as closing the station and routing, couriers and the ground network already honour it. |
| NQ-13-3 | CN-2 | How far should the rite-ground claim reach? | A: running rites only, plain Projection only (no infusion, no stain). This is what T2 builds. B: also refuse Shapings and ground marks (ash trails, vein nodes, gouges) on a running rite's ground, which today run before the claim for the caster and everyone else. C: also protect a formed floor with no rite running. | A now. B if play shows a Shaping breaking a floor. Not C: an idle floor can be repaired and players need to infuse blocks near their own builds. |
| NQ-13-4 | FX-12 | Two more pairs of rites share an icon: Apotheos and Possession (Sanguine Quintessence), Rooted Vein and Hungering Earth (Earthen Vein). Fix them with the audited pair? | A: yes, with existing items as stand-ins (Hungering Earth to the Sanguine Formation, Possession to Adipocere, Bloodline Covenant to the Enthralling Filament). B: only the audited pair. C: wait for drawn icons. | A, because it is one small edit and the test in T11 would flag the other pairs anyway. |
| NQ-13-5 | DS-4 | "One line each" for rites without a Liber entry: which rites? 19 Harbinger rites have none. So do all 18 Working crafts and 5 Unstained rites. | A: the 19 Harbinger rites (T12). B: also the 18 Working crafts. C: also the 5 Unstained rites. | A. The audit's count was rites, the crafts are machines with station pages of their own, and the Unstained side is parked under D-11. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CB-12 | Say which reading of "25ml" is meant. | Owner | NQ-13-1 |
| F-12 | Approve or decline the one-line change. Then T13. | Owner | NQ-13-2 |
| D-21, CB-15 | Play, then run the cost and offering pass from the tables in T14. Judge the Throne at 8000 and the Vigil at 6500. | Owner | A play session, T9 built |
| D-21, CB-15 | Lantern craft to 750. | Build | T9 |
| CN-2 | Decide whether the claim also covers Shapings or idle floors. | Owner | NQ-13-3 |
| PT-88 | Run `apotheosRiteWithoutRescue`, the Bloom rite and the late run after T6. If a solo caster still stalls, pick the next lever. | Build | T6 built |
| PT-88 | Cast a grand rite alone and judge the ordeal's weight. | Owner | T6 built |
| PT-67 | Prove on a server with the Chamber loaded that the rite still attunes and starts a real visit. The GameTest server has no Chamber. | Build | T4 built |
| FX-11 | Look at the new group tints and the hub label on the Liber map. Launch `riteFloorReviewClient` for the turned Qliphoth motif. | Owner | T10 built |
| FX-12 | Answer NQ-13-4. Say if "new icon" means drawn art. | Owner | NQ-13-4 |
| DS-4 | Answer NQ-13-5. Read the 19 lines and their pages for lore and voice. | Owner | T12 drafted |
| FX-13 | Run the whole opt-in `succession_validation` suite once. | Build | T8 |
| RV-22 | Doc and data drift found while checking: `docs/CARDINAL_RITES.md` says twenty Working crafts (there are 18) and `HarbingerRecipeMapDefinitions.WORKING_CRAFTS` still lists `mind_spike`. | Later | T9 can carry the doc line |
| DS-3 | Per-floor ceremony mechanics. | Parked | Owner |
| D-6 | Chimerization offering name and recipe. | Parked | Owner |
| D-11 | The Unstained ritual system. | Parked | Owner |
| DS-11 | Unstained invasion rite. | Parked | Owner |
