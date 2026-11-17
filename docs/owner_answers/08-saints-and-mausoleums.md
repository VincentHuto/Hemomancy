# 08. Saints, mausoleums, Chapels and canon memories

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P07). |
| T2 | Built (B16, 2026-11-15): an Apotheos anywhere in the chamber wakes an unopened tomb forced, a late Apotheos gets the fragment (NQ-08-18), and a tomb gives one peaceful sample ever (NQ-08-19 A). |
| T3 | Built (P07, B16): the blood debt is gone (SM-4), and B16 removed the `countsAsSpend` flag, `addBloodSpend` and the stale debt comments (NQ-08-20). |
| T4 | Built (P07): CR-17 (start 20), CR-18 (creep 12 / 10 / 8). NQ-08-21 kept the split at 2 / 2 / 1, so nothing was left to build. |
| T5 | Built (P07, B16): reload, wave UUIDs, wave drops, the burn through armour, the cooldown line, the cysts, the STRAY branch; B16 kept wave mobs through Peaceful (NQ-08-22), raised Seraphae's light fragments in place of the Vex (NQ-08-23 A), made `chapelWaveMobsLeaveNoLootOrExperience` deterministic and tested the cooldown line. |
| T6 | Built (P07, B16): Hemorath and Seraphae, then Velorum's four lines and close (NQ-08-24). |
| T7 | Built (P07). The acceptance line stays as it is (NQ-08-25). |
| T8 | Built (P07): three seeds, 12 of 12 complexes found, every entrance flush. SM-13 A: the check and the spacing stay, so no counter is built. |
| T9 | Built (P07): SM-12 and the mottle half of SM-8. |
| T10 | Not built. After the owner's session with Hemorath. |
| T11 | Not built. After the owner's session with Seraphae. |
| T12 | Not built. After the owner's session with Putriciel. |
| T13 | Not built. After the owner's session with Velorum. |
| T14 | Not built. After all four sessions. The Vex part was built in B16 (NQ-08-23). |
| D-1 build | First half built (B17, 2026-11-15): the spec refreshed; the trial record (NQ-08-2, NQ-08-3); Perfectus, 2 + 2 enzymes, 300 blood (NQ-08-1); the wider `never_sold` tag; the tree edges (NQ-08-14); the source tooltips (NQ-08-17); `CanonGiftState`; Crimson Tithe and the Unclosing Eye with their visuals, player only (NQ-08-5, NQ-08-6, NQ-08-12). Second half built (B18, 2026-11-15): Bloom of Rot and the Endless Hour with their visuals, player only (NQ-08-5, NQ-08-8, NQ-08-9, NQ-08-10, NQ-08-12, NQ-08-17); the old Hour debt and `endless_hour_debt` deleted; each Saint answering its own gift with one GameTest each (NQ-08-11); the old forms removed and `PROTOCOL_VERSION` 52. Not played. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

- The rulings keep the mausoleums, the Chapels, the ring door and the Saint combat kit as built, and ask for a set of small closes: the canon recipe gate, the two enrage dodges, the old blood debt, two fight numbers, the Chapel robustness list, the owed Beholding text and the doc promotion.
- D-1 is not answered. The answer is a copy of the recommendation ("Review it"). The spec's 17 questions are listed below as NQ-08-1 to NQ-08-17. VX-2, D-26 and chunk 12's C-8 wait on them.
- The look rows (SM-5 to SM-12) are ruled, but PR-1 and PR-2 put the work after one played session per Saint. They are planned as one task per Saint that starts after that Saint's session. Two changes are safe earlier: SM-12 (frost lips) and the mottle half of SM-8.
- Largest pieces of work: the canon gate (T1), the Chapel robustness pass (T5), and the per-Saint look tasks after play (T10 to T13).
- Surprise 1: nothing reads the blood debt any more, yet every `countsAsSpend` payment still writes it. Retiring it leaves the `countsAsSpend` flag on 114 `Source` constants doing nothing (NQ-08-20).
- Surprise 2: C-9 is half out of date. All four Saints now have phase cards and a kit ending. What is still true: an enraged Saint looks normal, Seraphae's Downfall raises vanilla Vexes, the voices are vanilla, the two Court bosses are silent.
- Surprise 3: the Loom is not ungated today. It has the rank gate, and the canon memories are SUMMA, so they weave and teach at D3.
- Surprise 4: the 2026-11-13 sound audit says all three M-4 picks are vanilla stand-ins that need their own files. It changes what the owner should listen for.
- The only Saint code edited after the sheet is three sound fixes on 2026-11-13 (doubled hurt and ambient sounds). They touch no kept number.
- Counts: 38 rows. BUILD 16, KEEP 15, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 1, OWNER 3, PARKED 3.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| D-1 | Meant to answer the 17 questions in the Canon Memory rework spec. The answer only says "Review it". No question is answered. | UNCLEAR | `docs/superpowers/specs/2026-11-06-canon-memory-rework-design.md` section 4. The four manipulations are unchanged since 2026-11-09 (`common/manipulation/saint/*.java`), still SUMMA and QUICK in `ManipulationInit`. | NQ-08-1 to 17 |
| M-1 | No hematic iron plating in `ductilis_conductors` or `laboring_arms_resistant`. | KEEP | `data/hemomancy/tags/block/ductilis_conductors.json`, `laboring_arms_resistant.json`. No `plating` entry in either. | none |
| M-2 | Charred planks stay out of every planks and wooden tag. | KEEP | `data/minecraft/tags`. Charred blocks appear only in `mineable/axe`, `slabs` and `stairs`. No planks or wooden tag names them. | none |
| M-3 | Keep Hemorath's heartbeat quickening from 70 to 45 ticks. | KEEP | `client/sound/MausoleumAmbienceRules.java` `ENTRANCE_INTERVAL` 70, `CHAMBER_INTERVAL` 45. The sound audit wants the file replaced, not the timing. | none |
| M-4 | Decide the three sound picks in the played session. | OWNER | `assets/hemomancy/sounds.json` `mausoleum.seraphae.choir`, `mausoleum.putriciel.creak`, `mausoleum.velorum.creak` are still vanilla aliases. The 2026-11-13 audit marks all three "needs own file" with a brief each. See NQ-08-26. | none |
| C-3 | The ward and the ring door are the D8 gate. The sarcophagus gets no gate of its own. | KEEP | `SaintSarcophagusBlock.handleUse`: no Degree check on relic, Foul Paste or vial. `ChapelRules.ATTEMPT_DEGREE` is 8. | none |
| C-6 | Canon recipes need D8 at the Loom and at the learn step. `mausoleum_treasure` stops dropping `memory_crimson_tithe`. | BUILD | `recipe/memory_weaving/memory_crimson_tithe.json` (no degree field). `KnownManipulationGrantHelper.checkMemoryGrant` gates by rank only (SUMMA is D3). `loot_table/chests/mausoleum_treasure.json` pool 2 still has the memory at weight 3. Recollections are in `never_sold`. | T1 |
| SM-1 | Close both enrage dodges. | BUILD | `HarbingerSaintEncounterHooks.spawnSaintBoss` asks only whether the waker is an Apotheos. `SaintSarcophagusBlockEntity.restoreDormant` clears `sampleYielded` on every return. Both dodges are live. | T2 |
| SM-2 | Three fragments make one Hallowed Residuum. | KEEP | `SaintEnrageRules.FRAGMENTS_PER_RESIDUUM` 3. `recipe/hallowed_residuum_hemorath_from_fragments.json` takes three. | none |
| SM-3 | Write the owed Beholding memory text for Hemorath and Seraphae. | BUILD | `ChapelFocusBlockEntity.tickShow` flashes the witnesses with a chime and no text. `en_us.json` has one generic `message.hemomancy.chapel.begin.beholding`. Nothing per Saint. | T6 |
| SM-4 | Retire the old blood debt on `IBloodVolume`. | BUILD | `HarbingerSaintEncounterHooks` line 56 and `PathMutualExclusionHelper` line 131 reset it. `BloodPaymentRules` and `ToothPecksEntity` write it. Only `BloodAccountingGameTests` reads it. It is not saved. | T3 |
| SM-5 | Darken Hemorath's flooded pool slightly. Brimming body and light floor 8 stay. | BUILD | `shaders/core/world/hemorath_fissure.fsh` (the `pool` colour). `HemorathRenderer.LIGHT_FLOOR` 8. Ordered after his played session. | T10 |
| SM-6 | Keep Seraphae's light floor 12, her stronger telegraphs and the overgrowth. | KEEP | `SeraphaeRenderer.LIGHT_FLOOR` 12. | none |
| SM-7 | Keep Putriciel's palette and the palm at the brow. Give a 12-block Bloom's growths more mass. | BUILD | `client/render/world/PutricielBloomRules.java`, `PutricielGrowthGeometry.java`, `putriciel_growth.fsh` exist. Not changed since the sheet. Ordered after their session. | T12 |
| SM-8 | Push the soot, lower the mottle contrast, thicken the sludge. | BUILD | `PutricielBodyFireGeometry` (soot layer), `tools/model_export/putriciel.py` `MOTTLE`, `PutricielSludgeField` and `PutricielSludgeGeometry`. The mottle half is safe before play. | T9, T12 |
| SM-9 | Keep the Endless Hour darkness and the RIME school's red crystals. | KEEP | Nothing to change. Velorum render files unchanged since the sheet. | none |
| SM-10 | Make Lantern Sweep, Staff Toll and Draw of Silence wind-ups bolder. | BUILD | `VelorumTelegraphPass` methods `sweep`, `toll`, `silenceDraw`. Ordered after his session. | T13 |
| SM-11 | The gloam fog should spare the umbral shapes. | BUILD | `client/event/VelorumGloamFog.java` `ECLIPSE_NEAR` 4, `ECLIPSE_FAR` 14. Shapes drawn in `VelorumTelegraphPass.eclipse`. Ordered after his session. | T13 |
| SM-12 | Drop the frost-white lips on the mark ring and welled seams. Every seam in the fight changes. | BUILD | `shaders/core/world/velorum_frost.fsh` shape 0 (`lip` 0.07) and shape 13 (the lip on a welled seam). Safe before play. | T9 |
| SM-13 | Judge the 14-block entrance check after the natural runs. | PARKED | `MausoleumEntranceRules.REACH` 14, `MAX_RISE` 4. No run since it widened. Waits on T8. | T8 |
| C-9 | Fix the Saint presentation gaps after a played session per Saint. | BUILD | Out of date in part. Cards exist for all four (`BossPresentationIds`). Each Saint has an ending rules class. Still true: no client reads the enraged state, `ChapelFocusBlockEntity.spawnWave` raises `EntityType.VEX`, voices are vanilla, `BloomWardenEntity` and `KindledChampionEntity` play no sound. | T14 |
| CR-17 | Seraphae's integrity starts at 20. | BUILD | `SeraphaeIntegrityRules.START` 30. Thresholds 0.40 and 0.75. | T4 |
| CR-18 | Putriciel's Kindling creep becomes 12 / 10 / 8. The absolution split is not settled by the answer. | BUILD | `PutricielPhase` creep ticks 12, 20, 15. `PutricielAbsolutionRules.NEEDED` 5. See NQ-08-21. | T4 |
| RV-5 | Keep the Chapel numbers, the ring door cost, the enrage per Chapel, the flower forest and the cyst rarity. | KEEP | `ChapelRules` constants, `RingDoorRules.OPEN_TICKS` 200 and `BLOOD_COST` 1000, `SaintEnrageRules`, `has_structure/mausoleum_seraphae.json`, `placed_feature/recollection_cyst.json` chance 24. All match. | none |
| RV-6 | Keep the layout. | KEEP | Generator files under `tools/worldgen/mausoleum/` unchanged since the sheet. I did not re-derive the -39 depth from the generator. | none |
| RV-19 | Keep the shared Saint combat kit. | KEEP | `SaintMoves.MELEE_FLOOR` 12, `AREA_FLOOR` 20. `SaintArena.EMPTY_RESET_TICKS` 600. `SaintKit.CHALLENGER_SCALING` 0.5. | none |
| RV-20 | Keep the four fights' numbers and the two voice sheets until each is played. | KEEP | Saint rules classes unchanged since the sheet. CR-17 and CR-18 are the two accepted exceptions. | none |
| RV-44 | A Saint's tomb closes over any replaceable cell. | KEEP | `SaintSarcophagusBlock.blockingCell` uses `canBeReplaced()`. | none |
| RV-80 | Keep visual pass 2 and the two revisions. | KEEP | Saint render classes and shaders unchanged since the sheet. SM-5, 7, 8, 10, 11 and 12 are the accepted exceptions. | none |
| RV-81 | Keep the placement numbers. | KEEP | `worldgen/structure/mausoleum_<saint>.json` `max_distance_from_center` 116. `MausoleumEntranceRules.REACH` 14. I did not re-check the 80 radius or the 33 drop. | none |
| RV-83 | Keep the tomb palettes, the pylon simplification and the ambience picks. | KEEP | Ambience constants match (`MausoleumAmbienceRules`). I did not open the palette class. See NQ-08-26 for the sound audit. | none |
| FX-18 | Fix the six Chapel robustness items together. | BUILD | `ChapelFocusBlockEntity.saveAdditional` saves no timer or cooldown. `tickDownfall` treats an unloaded mob as dead. `begin` returns silently during the cooldown. `hallowed_burn` is not in `bypasses_armor.json`. `BlockInit.recollection_cyst` has strength 0.6 and `PushReaction.DESTROY`. | T5 |
| C-1 | Promote the Saints to "implemented, overhauls pending" and update the pin. | BUILD | `docs/LORE_REFERENCE.md` line 710, `docs/HEMOMANCY_REFERENCE.md` lines 2095 and 6536, `wiki/Public-Alpha-Readiness.md` line 38, `wiki/Saints-and-Encounters.md` line 12 all still say post-release WIP. The pin is `HemomancyCreativeTabsSourceTest` line 96. | T7 |
| PR-1 | The owner plays one session per Saint before new art. | OWNER | Only Hemorath has been played (2026-11-06). | none |
| PR-2 | The looks are judged in motion, first person, multiplayer and for frame cost. Play first. | OWNER | The owner accepted every SM look recommendation on the sheet. The order still puts the work after each session. | none |
| PR-3 | Run natural worldgen for all four mausoleums. | BUILD | `gradle/review-clients.gradle` `mausoleumReviewClient` with `-PmausoleumNatural=true`. `MausoleumClientReview.locateNatural` finds each Saint's nearest complex. A build session can run it. | T8 |
| VX-2 | The Saint canon memory visuals wait for D-1. | PARKED | Waits on NQ-08-1 to 17. | none |
| D-26 | Lux's missing Summa entry waits for the Canon Memory rework. | PARKED | `ManipulationInit`: Unclosing Eye is still the only Lux SUMMA. Waits on NQ-08-1 and NQ-08-13. | none |

## Plan

Tasks T1 to T9 can start now. T10 to T13 each start after the owner's session with that Saint. T14 starts after all four sessions. One builder at a time, no parallel Gradle runs.

### T1. Canon gate and the treasure barrel (size M)

- Rulings: C-6
- Change:
  - `MemoryWeavingRecipe` gains an optional `required_degree` (absent means 0). The serializer reads and writes it in JSON and in its stream codec, and the key joins the serializer's `KEYS` list.
  - The four canon recipes set `required_degree` 8. Keep the files' current spacing. `SupportingLoreCanonSourceTest` pins `"animus":  1` with two spaces.
  - `SomaticLoomBlockEntity` refuses a weave below the recipe's degree before any enzyme, span or blood is taken. Reuse `message.hemomancy.somatic_loom_block_entity.requires_degree`. Compare through `ProgressionGates.hasDegree`. The file has unusual line endings. Keep them.
  - Learn step: a per-manipulation floor. `BloodManipulation.setRequiredDegree(8)` on the four registrations, and `ManipulationRankGates.minDegreeFor(BloodManipulation)` returning the larger of the rank gate and the floor. Move the callers that hold a manipulation: `KnownManipulationGrantHelper.checkMemoryGrant`, `BloodMemoryItem` and `CrudeMemoryShardItem` tooltips, `ManipulationsTabController`, `ManipulationTraceLayerCache`.
  - `loot_table/chests/mausoleum_treasure.json`: remove the `memory_crimson_tithe` entry (weight 3) from the second pool. Raise the other three memories by one each (6, 6, 5) so the pool total stays 31 with `empty` at 14.
  - The rank stays SUMMA in this task. Rank, the trial record and the wider `never_sold` list are D-1 questions.
- Files: `common/recipe/MemoryWeavingRecipe.java`, `common/recipe/serializer/MemoryWeavingRecipeSerializer.java`, `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`, `common/manipulation/ManipulationRankGates.java`, `common/capability/player/harbinger/manip/KnownManipulationGrantHelper.java`, `common/init/ManipulationInit.java`, `common/item/harbinger/memories/BloodMemoryItem.java`, `CrudeMemoryShardItem.java`, `client/screen/skilltree/harbinger/ManipulationsTabController.java`, `ManipulationTraceLayerCache.java`, the four `data/hemomancy/recipe/memory_weaving/memory_<gift>.json`, `data/hemomancy/loot_table/chests/mausoleum_treasure.json`
- Tests: `CanonMemoryRecipeResourceTest` pins `required_degree` 8. Check `MemoryWeavingRecipeResourceShapeTest` accepts the new key. A JVM test for `minDegreeFor`. A GameTest beside `SomaticLoomWeavingGameTests` that refuses the weave and the grant at D7 and allows both at D8. A resource test that the treasure table holds no canon memory.
- Docs: `SAINT_MAUSOLEUMS.md` (Residuum and Recollections, Rulings that bind), `HEMOMANCY_REFERENCE.md` 5.6g, 5.6h and 25.7.2, `wiki/Saints-and-Encounters.md`, `OPEN_QUESTIONS.md` C-6 to rulings
- Depends on: nothing. It is step 1 of the D-1 spec's build order and stands alone.

### T2. Close the two enrage dodges (size M)

- Rulings: SM-1
- Change:
  - Dodge 1. The awakening is decided from everyone in the chamber, not only the hand on the tomb. In `HarbingerSaintEncounterHooks.spawnSaintBoss` the `apotheosWaker` argument becomes "the waker or any living, non-spectator player in the chamber is an Apotheos". A door record of NONE then wakes FORCED as it does today for an Apotheos waker.
  - Late arrivals. At the reward roll, a NONE-door fight with any Apotheos among `arena.challengers()` gives the fragment. The stat enrage is not applied mid-fight.
  - Dodge 2. `SaintSarcophagusBlockEntity.restoreDormant` stops clearing `sampleYielded`. A tomb yields one peaceful Consecrated Syringe in its life. A later vial gets the existing "already yielded" message. This is my reading of "close". NQ-08-19 lists the other ways.
  - A PROPER door stays PROPER for a mixed party. See NQ-08-18.
- Files: `common/entity/boss/saint/HarbingerSaintEncounterHooks.java`, `SaintEnrageRules.java`, `SaintTrialEntity.java` (`rollTrialRewards`), `common/tile/harbinger/puzzle/SaintSarcophagusBlockEntity.java`
- Tests: `SaintEnrageRulesTest` for the new rule. `SaintEncounterGameTests`: a non-Apotheos wakes a NONE tomb with an Apotheos in the chamber and the Saint rises FORCED. A returned tomb refuses a second peaceful draw.
- Docs: `SAINT_MAUSOLEUMS.md` (Awakening, Sarcophagus, Rulings that bind), `HEMOMANCY_REFERENCE.md` 5.8 and 25.8, `wiki/Saints-and-Encounters.md`
- Depends on: NQ-08-18 and NQ-08-19 if the owner wants a different close. Otherwise nothing.

### T3. Retire the blood debt (size S, or M with the flag)

- Rulings: SM-4
- Change: remove `bloodDebt`, `addDamage`, `consumeDebt`, `getBloodDebt` and `resetBloodDebt` from `IBloodVolume`, `BloodVolume` and `ItemStackBloodVolume`. Remove the three callers: the reset in `HarbingerSaintEncounterHooks`, the reset in `PathMutualExclusionHelper`, and `ToothPecksEntity`'s `addDamage(1.0)`. The debt is not saved, so no data changes. `addBloodSpend` then writes nothing. Whether the `countsAsSpend` flag goes too is NQ-08-20. Until answered, `addBloodSpend` becomes an empty method and the flag stays.
- Files: `common/capability/player/harbinger/bloodvolume/IBloodVolume.java`, `BloodVolume.java`, `ItemStackBloodVolume.java`, `BloodPaymentRules.java`, `common/entity/boss/saint/HarbingerSaintEncounterHooks.java`, `common/capability/PathMutualExclusionHelper.java`, `common/entity/mob/animal/ToothPecksEntity.java`
- Tests: `BloodAccountingGameTests.brazierLightingPaysAndCountsItsPour` drops its debt assertion. The stub in `BloodRoutingRulesTest` loses the removed methods. `HemorathPresentationGuardrailTest` keeps its ban list. `BloodAccountingGuardrailTest` is unchanged unless the flag goes.
- Docs: `HEMOMANCY_REFERENCE.md` lines 1124 and 1406 (blood debt tracking), `SAINT_MAUSOLEUMS.md` Rulings that bind. `AGENTS.md` and `ARCHITECTURE_STANDARDS.md` only if the flag goes.
- Depends on: NQ-08-20 for the flag. The chunk that owns blood accounting should see this task.

### T4. Seraphae's start and Putriciel's creep (size S)

- Rulings: CR-17, CR-18
- Change:
  - `SeraphaeIntegrityRules.START` 30 to 20. Bands become 20, 35, 25. Thresholds stay 40 and 75. Side effects to check in the fight review: she starts further from containment, so her chain starts longer (`SeraphaeLeashRules` follows integrity), and a solo player who never finds an opening reaches the Radiance Flood at 0 in about 100 seconds, not 150.
  - `PutricielPhase` creep ticks: Rot I 12 (unchanged), Rot II 20 to 10, Rot III 15 to 8.
  - The absolution split stays 2 / 2 / 1 (`NEEDED` 5) until NQ-08-21 is answered.
- Files: `common/entity/boss/saint/seraphae/SeraphaeIntegrityRules.java`, `common/entity/boss/saint/putriciel/PutricielPhase.java`
- Tests: `SeraphaeIntegrityRulesTest` (pins 30), `SeraphaeFightGameTests` (sets the meter to 30 by hand), `PutricielKindlingRulesTest` (pins 1/20 for Rot II). Run `seraphae_fight_validation` and `putriciel_fight_validation`.
- Docs: `SAINT_MAUSOLEUMS.md` (kit table "from 30", Calls made, Rulings that bind), `HEMOMANCY_REFERENCE.md` 5.8 (line 2082 says "from 30"), `wiki/Saints-and-Encounters.md`
- Depends on: nothing

### T5. Chapel robustness (size M)

- Rulings: FX-18
- Change:
  - Reload: save and load `timer` and `cooldown` in `ChapelFocusBlockEntity`.
  - Wave UUIDs: a wave mob that `getEntity` cannot find counts as dead only when its chunk's entities are loaded. Until then the wave waits. This also stops a reload from skipping a wave.
  - Farming: Chapel wave mobs drop no loot and no experience. Use a session set of wave UUIDs cleared through `TransientServerStateEvents`, not `getPersistentData()`.
  - Peaceful: the Downfall must not finish itself when the wave despawns. The fix depends on NQ-08-22. My default is that wave mobs do not despawn in Peaceful, as the Saints already do not.
  - `hallowed_burn` joins `data/minecraft/tags/damage_type/bypasses_armor.json`.
  - The failure cooldown speaks: `begin` during the cooldown shows a new lang key and a low chime.
  - Cysts: `recollection_cyst` becomes unbreakable in survival with `PushReaction.BLOCK`, like the trial set pieces. Digging out the ground under it still removes it. I left that as is.
  - Delete the unreachable STRAY branch in `spawnWave` (Velorum has no Downfall).
- Files: `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java`, `common/block/harbinger/puzzle/ChapelRules.java`, `common/init/BlockInit.java`, `data/minecraft/tags/damage_type/bypasses_armor.json`, `assets/hemomancy/lang/en_us.json`
- Tests: `ChapelRulesTest` for any rule moved into `ChapelRules`. New GameTests in `MausoleumWardGameTests` or a Chapel batch: a save and load mid-trial keeps the timer and cooldown, a Peaceful Downfall does not complete on its own, wave mobs drop nothing, the burn ignores armour. A resource test for the tag.
- Docs: `SAINT_MAUSOLEUMS.md` (Chapels, Proof owed: remove the robustness line), `HEMOMANCY_REFERENCE.md` 5.6g
- Depends on: NQ-08-22. NQ-08-23 if the Vex is replaced in the same pass.

### T6. Beholding memory text (size S)

- Rulings: SM-3
- Change: each Beholding flash shows one line of narration to the challenger, four lines a Saint, and a closing line on completion. Hemorath: his students sealing the vessel one by one. Seraphae: four touches, four growths. Narration only, no Saint speaks. Lore limits: Hemorath was never a Harbinger and his students feared his bloodlust, Seraphae was sealed by her own people, no God or Satan framing, no martyr framing. Keys such as `message.hemomancy.chapel.beholding.hemorath.1` to `.4` and `.done`. The flash timing (25 ticks) is a kept number, so the lines go to chat where they stay readable. Velorum's Beholding has the same gap. See NQ-08-24.
- Files: `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java` (`tickShow`, `finish`), `assets/hemomancy/lang/en_us.json`
- Tests: a lang test that every Saint with a Beholding slot has its four lines and its closing line. `HardCodedEnglishGuardrailTest` stays green.
- Docs: `SAINT_MAUSOLEUMS.md` (Chapels, remove the owed line), `LORE_REFERENCE.md` section 10 (the two memories), `wiki/Saints-and-Encounters.md`
- Depends on: nothing

### T7. Promote the Saints in the docs (size S)

- Rulings: C-1, and the record of every KEEP row in this chunk
- Change: replace "post-release WIP" with "implemented, overhauls pending" in the four places and the pin. Remove "no Saint chamber generates naturally". Move the answered questions in `SAINT_MAUSOLEUMS.md` from "Open questions" to "Rulings that bind": M-1, M-2, M-3, C-3, SM-2, SM-6, SM-9, RV-5, RV-6, RV-19, RV-20, RV-44, RV-80, RV-81, RV-83. Whether Saint fights join first-release acceptance is NQ-08-25. Until answered, line 62 of the wiki page stays.
- Files: `docs/LORE_REFERENCE.md` (line 710), `docs/HEMOMANCY_REFERENCE.md` (lines 2095 and 6536), `wiki/Public-Alpha-Readiness.md` (line 38), `wiki/Saints-and-Encounters.md` (line 12), `docs/SAINT_MAUSOLEUMS.md`, `src/test/java/com/vincenthuto/hemomancy/common/creative/HemomancyCreativeTabsSourceTest.java` (line 96)
- Tests: the pin in `HemomancyCreativeTabsSourceTest` changes with the wiki sentence.
- Docs: the task is the docs. Also `OPEN_QUESTIONS.md` C-1 and the rows above to rulings.
- Depends on: nothing

### T8. Natural runs for all four (size S)

- Rulings: PR-3, SM-13
- Change: no code. A build session runs `./gradlew.bat mausoleumReviewClient -PmausoleumNatural=true` (the Saint defaults to all, the seed to 20261028), then twice more with `-PmausoleumSeed=<n>`. Skip the first-load Accessibility screen and confirm each launch got past it. The run logs `MAUSOLEUM_REVIEW` lines, a `MISSING` line for a Saint with no complex within 100 chunks, the entrance ground measure, and shots in `build/mausoleum-review/natural/<saint>/`. Record per Saint and seed: found or missing, distance from the origin, how the entrance sits. The structure does not count refused sites. If the owner wants a refusal rate, add a dev-only counter in `MausoleumStructure.settleOnEntranceGround` and `keepHallsBuried` behind a system property (size M then).
- Files: none changed. Reads `gradle/review-clients.gradle`, `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MausoleumClientReview.java`, `common/worldgen/structure/MausoleumStructure.java`
- Tests: none
- Docs: `SAINT_MAUSOLEUMS.md` "Proof owed" (the natural worldgen line) gets the results.
- Depends on: nothing. SM-13 is decided from the results.

### T9. Look changes safe before play (size S)

- Rulings: SM-12, SM-8 (mottle only)
- Change:
  - SM-12: remove the frost-white lip on seam shape 0 and on the welled seam (shape 13) in `velorum_frost.fsh`. Colour only, no new geometry, easy to put back. Prove it compiles with the Velorum fight review client.
  - SM-8, mottle: lower the flesh mottle's contrast in `tools/model_export/putriciel.py` (`MOTTLE` against `FLESH`) and re-run the generator. A texture change judged at portrait distance, which motion does not alter.
  - Why these two: neither moves a telegraph edge, adds frame cost or trades against legibility. Every other look change does one of those and waits for play.
- Files: `assets/hemomancy/shaders/core/world/velorum_frost.fsh`, `tools/model_export/putriciel.py` and the atlas it writes
- Tests: `PutricielPaletteTest` value bands must still pass. `ShaderNoiseIncludeGuardrailTest` stays green.
- Docs: `SAINT_MAUSOLEUMS.md` (the two revision sections, Rulings that bind)
- Depends on: nothing. The owner then plays Velorum and Putriciel with these already in.

### T10. Hemorath after his session (size S)

- Rulings: SM-5, PR-1, PR-2
- Change: darken the standing pool in `hemorath_fissure.fsh` slightly. My starting value is 10 to 15 percent on the pool body only. The meniscus, the buried seam and the lit cut edge stay, since they carry the telegraph. Brimming body and light floor 8 stay. Add whatever the session finds.
- Files: `assets/hemomancy/shaders/core/world/hemorath_fissure.fsh`
- Tests: none new. Client launch with `-PhemorathFightReview`.
- Docs: `SAINT_MAUSOLEUMS.md`
- Depends on: the owner's Hemorath session. He has played once, on 2026-11-06, before the trench pass.

### T11. Seraphae after her session (size S)

- Rulings: SM-6, PR-1, PR-2
- Change: none accepted. Light floor 12, the stronger telegraphs and the overgrowth stay. The task exists to take the session's findings, and to check how the fight feels from a start of 20 (T4).
- Files: none yet
- Tests: none yet
- Docs: `SAINT_MAUSOLEUMS.md`
- Depends on: the owner's Seraphae session, T4

### T12. Putriciel after their session (size M)

- Rulings: SM-7, SM-8 (soot and sludge), PR-1, PR-2
- Change: more mass on a 12-block Bloom's growths (bigger and taller colony bodies, not more cells, because a Bloom already draws about 450 cells). Push the black soot tips and the back plume so the fire reads three-tone against the dark chamber. Thicken the sludge at a player's eye and break up the long even runnel. Palette and the palm at the brow stay. The fire keeps the Flammeus look with no green or yellow.
- Files: `client/render/world/PutricielBloomRules.java`, `PutricielGrowthGeometry.java`, `PutricielColonisationGeometry.java`, `PutricielBodyFireGeometry.java`, `PutricielSludgeField.java`, `PutricielSludgeGeometry.java`, `assets/hemomancy/shaders/core/world/putriciel_growth.fsh`
- Tests: `PutricielFireResourceTest` stays green. Client launch with `-PputricielFightReview`. Watch frame cost.
- Docs: `SAINT_MAUSOLEUMS.md`
- Depends on: the owner's Putriciel session, T9

### T13. Velorum after his session (size M)

- Rulings: SM-10, SM-11, PR-1, PR-2
- Change: bolder wind-ups for Lantern Sweep, Staff Toll and Draw of Silence at the Hematic Riposte bar (layered geometry off the floor, not brighter frost). The gloam fog spares the Eclipse's umbral shapes: draw them on a material the fog does not fade, built once as `RenderTypeCachingGuardrailTest` requires, and leave `ECLIPSE_NEAR` 4 and `ECLIPSE_FAR` 14 alone. The Endless Hour darkness and the red crystals stay.
- Files: `client/render/world/VelorumTelegraphPass.java`, `VelorumFrostRules.java`, `VelorumRenderTypes.java`, `client/event/VelorumGloamFog.java`
- Tests: `RenderTypeCachingGuardrailTest`, `MaterialQuadGuardrailTest`. Client launch with `-PvelorumFightReview`.
- Docs: `SAINT_MAUSOLEUMS.md`, `docs/MANIPULATION_VISUALS.md` only if a shared material changes
- Depends on: the owner's Velorum session, T9

### T14. Saint presentation gaps (size M to L)

- Rulings: C-9
- Change, by what is still true:
  - Enraged Saint looks normal: sync the forced awakening (a new entity data flag, since the deed mask can be empty on a forced fight) and give it a look and the wake sound the sound audit names (`saint.enraged_wake`). The look itself is a design to settle after the sessions.
  - Seraphae's Downfall raises vanilla Vexes: replace the mob. See NQ-08-23.
  - Vanilla voices: the 2026-11-13 sound audit lists them row by row for all four Saints and the shared kit. That work belongs to the sound pass.
  - Silent Court bosses: `BloomWardenEntity` and `KindledChampionEntity`. That work belongs to the Brined Court chunk.
  - Already done by the overhaul and only to be judged in play: endings (each Saint has its own) and phase cards (all four have them).
- Files: `common/entity/boss/saint/SaintTrialEntity.java`, the four Saint renderers under `client/render/entity/boss/`, `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java`
- Tests: `SaintCombatKitGuardrailTest` stays green. A GameTest that a forced rise sets the synced flag.
- Docs: `SAINT_MAUSOLEUMS.md`, `BOSSES.md`, the cleanup audit's C-9 row
- Depends on: all four sessions (PR-1), NQ-08-23, the sound pass, the Brined Court chunk

## Conflicts with recent work

None.

## New questions

NQ-08-1 to NQ-08-17 are the 17 questions of the Canon Memory rework spec, section 4, in the spec's order. "Default" is the spec's own recommendation.

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-08-1 | D-1 q1 | What rank do the four gifts take? | A: PERFECTUS with a per-manipulation D8 floor. B: a new CANON rank at D8 (touches the tab rings, vein strain, weave tiers and every switch on rank). | A, the spec's default. T1 builds the floor either way. |
| NQ-08-2 | D-1 q2 | Must a player have ended that Saint's trial to weave and learn its gift? | A: yes, a durable per-player record written at the ending. B: no, Recollection and Residuum are enough. | A, the spec's default. Without it a peaceful draw plus a handed Recollection gives the gift with no fight. |
| NQ-08-3 | D-1 q3 | Does an Apotheos who forced the door and won earn the record? | A: yes. B: no, proper endings only. | A, the spec's default. The paths are equal. |
| NQ-08-4 | D-1 q4 | Recipe weight? | A: enzymes 2 + 2 and blood 300. B: leave 1 + 1 and 100 and let the harder weave carry it. | A, the spec's default. |
| NQ-08-5 | D-1 q5 | Do non-player casters keep the gifts? | A: player only. Remove the Eye, Bloom and Hour entity casts and Drudge actions and exclude all four from Vesper's coverage. B: keep simple variants for Vesper. | A, the spec's default. |
| NQ-08-6 | D-1 q6 | Does the Crimson Tithe refund blood at release? | A: none, a small heal only. B: a quarter of the vessel, capped at the blood paid that hold. | A, the spec's default. |
| NQ-08-7 | D-1 q7 | What does the Unclosing Eye do to allies? | A: heals the hurt and grows the whole, ally Burst floored at 1 health. B: heal only. | A, the spec's default. B is safer in co-op. |
| NQ-08-8 | D-1 q8 | Bloom of Rot's price? | A: the caster is colonised by their own field and reclaimed if it goes unlit. B: the caster is spared and only the reclaim remains. | A, the spec's default. |
| NQ-08-9 | D-1 q9 | Does the Endless Hour's dark rim apply `OBSCURED`? | A: yes, for the player's gift only. B: no, the rim only raises stillness. | A, the spec's default. Velorum himself never applies it. |
| NQ-08-10 | D-1 q10 | The Hour's self-set at the close? | A: keep 20 ticks of Paralysis with 70 percent damage reduction. B: shorten. C: drop. | A, the spec's default. |
| NQ-08-11 | D-1 q11 | Do Saints answer their own gifts (the table in spec 2.6)? | A: yes, built after the gifts work in the open world. B: no. | A, the spec's default. |
| NQ-08-12 | D-1 q12 | PvP? | A: the gifts hit players wherever `canHarm` allows, and the Tithe draws a blood-active player's blood. B: no PvP for gifts. | A, the spec's default. |
| NQ-08-13 | D-1 q13 | Lux at Summa once the Eye moves to PERFECTUS? | A: flag it for the next balance pass. B: add or re-rank a Lux entry now. | A, the spec's default. This is D-26. |
| NQ-08-14 | D-1 q14 | Tree placement? | A: the four gifts on their own outer nodes, and White Verdict, Absolute Stillness, Carrion Communion and Fruiting Contagion re-parented to ordinary neighbours. B: leave the tree. | A, the spec's default. |
| NQ-08-15 | D-1 q15 | HUD? | A: no meter, the body and floor carry it. B: one small gift meter layer. | A, the spec's default. If B, it must fit the living hematic HUD built on 2026-11-13, which the spec predates. |
| NQ-08-16 | D-1 q16 | `mausoleum_treasure`? | A: drop the memory and spread its weight. B: replace it with a Residuum fragment of the complex's Saint. | A. The C-6 answer ("drop the direct memory") already reads as A, and T1 builds it. Confirm only if B was meant. |
| NQ-08-17 | D-1 q17 | Player text? | A: tooltips name the source as "Canon Memory of Hemorath, the Hollow Vessel" and so on, with a few lines of narration and no Saint speaking. B: other wording. | A, the spec's default. |
| NQ-08-18 | SM-1 | A Silentium opens the door properly and an Apotheos fights beside them. Is that a dodge? | A: no, the Chapels were done, the fight stays proper for the party. B: yes, any Apotheos challenger makes it forced. | A. T2 changes only fights where no door was opened. |
| NQ-08-19 | SM-1 | How should the peaceful syringe dodge close? | A: one peaceful sample per tomb, ever. B: one per return, but a tomb behind a forced door gives a syringe that makes a fragment. C: one per player per Saint, on a player record. | A. It is the smallest change and other complexes exist. T2 plans A. |
| NQ-08-20 | SM-4 | With the debt gone, `countsAsSpend` does nothing. Remove the flag too? | A: remove it from `Source` and all 114 constants, and from the rule text in `AGENTS.md`. B: keep the flag as a label for later use. | A, as its own small pass after T3, so no dead flag stays. It touches the blood accounting rules, so the owner should say. |
| NQ-08-21 | CR-18 | The recommendation fixes the creep only. What about the 2 / 2 / 1 absolution split? | A: keep five (2 / 2 / 1). The fifth is the Full Purge's Offering by design. B: six (2 / 2 / 2). C: five as 1 / 2 / 2. | A. The faster creep in Rot II and III already makes the fight escalate. |
| NQ-08-22 | FX-18 | What should a Downfall do in Peaceful? | A: wave mobs do not despawn in Peaceful, so the trial plays as normal. B: Peaceful refuses to start a Downfall. C: Peaceful turns it into a walk of the seals with no mobs. | A. Saints already ignore Peaceful. B would stop a Silentium opening three of the four doors properly. A needs the Vex replaced (NQ-08-23). |
| NQ-08-23 | C-9, FX-18 | What replaces the vanilla Vex in Seraphae's Downfall? | A: her light fragments (`SeraphaeFragmentEntity`, already built). B: a new mob of her patients. C: Hematic Constructs as in Hemorath's. | A, if the fragment can run without her. I did not check that. Otherwise C until B is designed. |
| NQ-08-24 | SM-3 | Velorum's Beholding ("the four who followed him into stillness") has no text either. Write it in the same task? | A: yes, three Saints. B: only the two named. | A. It is the same gap and four more lines. |
| NQ-08-25 | C-1 | Does the promotion also bring Saint fights into first-release acceptance (`wiki/Public-Alpha-Readiness.md` line 62 excludes them)? | A: keep them out of acceptance until each has been played. B: bring them in now. | A. |
| NQ-08-26 | M-4, RV-83, M-3 | The 2026-11-13 sound audit says the choir, both creaks, the heartbeat, the drip, the anchor tick, the crackle and the flare are vanilla stand-ins that need their own files. Do the "picks" still need a verdict? | A: in the session judge only timing, volume and whether each moment wants a sound. The files are replaced by the sound pass. B: judge the stand-ins as final picks. | A. The kept numbers (70 to 45 ticks, pitch 0.6, the muffled step) stand either way. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| D-1 | Answer NQ-08-1 to NQ-08-17. The spec then needs a short refresh before it is built: it predates the Temper rename, the freeform Projection and Absorption work, `VerbShaping` and the living HUD. | Owner | nothing |
| VX-2 | The four canon memory visuals. | Parked | D-1 |
| D-26 | Lux's Summa entry. | Parked | D-1, NQ-08-13 |
| C-8 (chunk 12) | The Apotheos section of the Liber is tied to D-1 by its answer. | Parked | D-1 |
| PR-1 | Play one session per Saint: trials solo, set pieces mid-trial, the hallowed burn, the rise, ambience, feel and length. Seraphae from a start of 20 and Putriciel with the new creep if T4 is built first. | Owner | T4 and T9 are best built first |
| PR-2 | Judge the looks in motion, first person, multiplayer and for frame cost. | Owner | PR-1 |
| M-4 | Hear the choir, the creak and the ice creak. | Owner | PR-1, NQ-08-26 |
| SM-5, SM-7, SM-8, SM-10, SM-11 | The look changes that wait for play. | Build | PR-1 per Saint (T10, T12, T13) |
| SM-13 | Decide whether the 14-block entrance check refuses too many sites. | Owner | T8 results |
| C-9 | The enraged look is a design still to write. Voices and Court sounds belong to other passes. | Later | PR-1, the sound pass, the Brined Court chunk |
| RV-20, RV-80 | Both keeps hold "until played". Each session may reopen its Saint's numbers and looks. | Owner | PR-1 |
| SM-1 | A late Apotheos gets the fragment but the Saint is not enraged mid-fight. Say if that should change. | Owner | T2 built |
| FX-18 | A cyst still withers if the ground under it is dug out. A Creative player's taken Recollection can be discarded. | Later | T5 built |
| SM-3 | The Beholding steles "carved with his students" are still not modelled. Text only in T6. | Later | nothing |
