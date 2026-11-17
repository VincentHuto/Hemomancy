# 17. Architecture decisions, combat rules, tests, cleanup and misc keeps

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P01, P08): the approvals and keeps are recorded. The ledger rows for G-1/F-18 and F-17 were set to approved, not built, on 2026-11-14. Left: the stale combat clause in `AGENTS.md`. |
| T2 | Built (P01). |
| T3 | Built (P01, B02): F-19, S-5b, F-4. B02 added the Spine reclaim test (`spineReclaimCountsTheSpineThatLanded`) and moved the Mortal Display's lodestone onto `PendingDelivery.insert`. |
| T4 | Built (P01). |
| T5 | Built (P01, B02). B02: the Elytra effect's grant ends when it expires or is removed (`ElytraEffectEvents`). |
| T6 | Built (P01). The frozen tab order was refreshed in the build 2 baseline, and `creativetabordermatchesthefrozensnapshot` now passes with the `dictation_table` row gone. |
| T7 | Built (P01, B02). B02: `CardinalRiteSync` sends each player only the rites within 128 blocks and their own, and an empty list once; the Vesper ordeal's direct scene sends go through `ChamberOfWillManager.sendScene`, which forgets the `LastSent` mark; the lit Black Heart test added. |
| T8 | Built (Housekeeping, B02). B02 added the manual `cleanGameTestWorlds` task (NQ-17-5) and ran it once: 65 folders, the 29 GB combined world among them. |
| T9 | Built (P08). |
| T10 | Built (P08). |
| T11 | Built (P08, B02). B02: Sheolic retribution places its flames through `PlayerBlockEdits`; the F-16 follow-ups (look target and `Reach.UNGATED` deleted, Verdigris Aura asks `canHarmFor`). The `AGENTS.md` clause was already gone. |
| T12 | Built (B03), NQ-17-3 A. `LastRiteHelper.CURVED_HORN_ID`; the Horn arms only while nothing else is armed and any other source arms over it (`isOpenToArm`); its save asks `canFire`, calls `consume`, keeps the 1 durability and shares the 12,000-tick cooldown. Tests: `LastRiteRulesTest.theCurvedHornYieldsTheSlot`, `curvedHornSavesOnceInsideTheSharedCooldown`, `curvedHornYieldsToALaterLastRite`. |
| T13 | Built (B03), NQ-17-1 A, NQ-17-2 A. `common/damage/DamageStages` is the one `Pre` subscriber (attackers HIGH, defenders NORMAL, death-saves LOW, readers LOWEST); Iron Will split out of the wound handler; the Horn moved off `LivingDeathEvent` into the death-save stage; readers take `DamageStages.landing`. Ink Mantle and Cryptobiosis stay on `Post`. `DamageStageGuardrailTest` added; both goldens re-cut, the Absolved rows added. |
| UN-71 | Built (B37, 2026-11-16): the 293 legacy `main()` tests are JUnit (`LegacyMainTestGuardrailTest`), 431 private test helpers gave way to `support/SourceAssertions` (ratchet 593 to 101), the `getPersistentData()` allow-list re-checked (84 files, 357 calls; three stale migration reasons fixed, five dead `ChamberVisitService` constants removed). |
| Rows outside the tasks | B02: the flakes (chapel control made persistent; test origin folded into 1,000,000 blocks), the clinical and succession tests rewritten, the GeckoLib jar moved to `removed_assets/libs/`. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings approve every architecture decision that was still PENDING in the consolidation ledger, keep nine
built things as they are, and ask for a round of housekeeping. Paths that start with `common/` sit under
`src/main/java/com/vincenthuto/hemomancy/`.

- The largest and riskiest work is the combat-rule block (T9 to T13): G-2b, F-16, F-17 and G-1/F-18. It changes who
  gets hit, who gets kill credit, and how much blood a hit costs. It needs its tests written first and must run alone.
- Every approved ledger item is still pending in code. None was done by the core loop rounds. F-13 is not
  reserve-protected today: Track 4 called scar upkeep "mostly moot" and left the flag alone.
- The lash, Shaping and HUD work of 2026-11-12 and 11-13 added no new `Reach` preset site and no new damage handler.
  The new strand code asks the strict `canHarm` everywhere. `Reach` is named 20 times in 14 files: 19 wider presets
  and one explicit `STRICT`. Of those files only `MorphlingItem` changed after the sheet, and its preset line did not.
- Recount of damage paths today: 50 subscribed damage handlers in 37 files, and 40 death handlers, four of which
  cancel a death. The audit counted 48 listeners.
- Flipping G-2b's flag alone would crash: with `allies = false` a preset needs a player caster, and three sites pass a
  Drudge or a `LivingEntity`.
- Surprises: FX-22 is already done (no CR CR LF file is left). The RT-25 half of FX-14 is done (`TemporaryIceSavedData`).
  M-3's textures are already in `removed_assets/`. The GameTest world is 115 GB. RV-38's "keep" describes fixtures
  that were moved to subcommand classes later on the day the sheet was written, so it is a conflict.
- DS-5 cannot be planned as written. Advanced brewing is four operations, not three recipes, and the Athanor's proof
  of use is "a refinement and a compound".

Counts: 35 rows. BUILD 20, KEEP 9, DONE 1, MOOT 0, CONFLICT 1, UNCLEAR 1, OWNER 0, PARKED 3.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| G-1, F-18 | Give damage handlers explicit stages and put every death-save in one stage, before the next balance pass. | BUILD | 15 `LivingDamageEvent.Pre` handlers. Blood loss (`common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java` `onPlayerDamaged`) and vein strain (`common/capability/player/harbinger/vascular/VascularSystemEvents.java` `onPlayerDamaged`) read the amount at NORMAL beside six handlers that change it. Death-saves sit in four places. No ordering test exists. | T9, T13 |
| F-16 | Area damage from armour, Still Arts, Sheolic and Saprovitta is attributed to the player and respects allies and PvP. | BUILD | `common/manipulation/ManipulationCombatHelper.java` `Reach`. Five sites still use `damageSources().magic()` with no attacker. Listed under T11. | T9, T11 |
| G-2b | The caster's own puppets, allies and impressed mobs stop matching `Monster.class` filters. | BUILD | `Reach.MONSTER_FILTER` and `MONSTER_FILTER_ANY_STATE` still have `allies = true`. 15 uses in 11 files. Listed under T10. | T9, T10 |
| G-7b | A flight revoke checks the other flight sources first. | BUILD | `common/util/FlightArbiter.java` `revoke` clears `mayfly` without asking. Its Javadoc names the Elytra and Edacious case. `FlightSourceGameTests#elytraLandingDropsEdaciousFlightUntilItsNextTick` pins today's behaviour. | T5 |
| F-13 | Scar upkeep is reserve-protected. | BUILD | `common/item/harbinger/scar/ScarDefinition.java` line 40: `UPKEEP = Source.of("scar_upkeep", Category.SCAR, false, false)`. Not protected today. Upkeep is still charged every tick (`onWornTick`). The sync half is done. | T4 |
| F-14 | Passive scar effects survive milk. | BUILD | `ScarDefinition.onEquipped` adds each passive effect once with infinite duration. Nothing re-adds it, so milk strips it until the scar is re-equipped. | T4 |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve. | BUILD | `BLOOD_PRICE` in `LivingAxeItem`, `LivingBladeItem`, `LivingCrossbowItem`, `LivingToolItem` (`common/item/harbinger/tool/living/`). Each pays in both branches, with no `isActive()` check, and pays the full price even when the player cannot cover it. | T4 |
| F-17 | The Curved Horn uses `LastRiteHelper`. | BUILD | `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java` `playerHurt(LivingDeathEvent)`: cancels the death, sets health to 1, no `LastRiteHelper` call, no cooldown. | T12 |
| F-4 | ScaleGrip respects cancelled deaths and persists pending returns. | BUILD | `common/item/shared/ScaleGripEvents.java`: NORMAL priority, static `PENDING_RETURNS` map, add-or-drop on respawn. | T3 |
| F-19 | Station kits follow the `PendingDelivery` contract. | BUILD | `common/capability/player/harbinger/station/StationUpgradeProgress.java` `deliver`: checks alive only. No observational-visit check. A Creative discard counts as delivered. | T3 |
| F-20 | Resync Ichorian knowledge on dimension change. | BUILD | `common/capability/PlayerStateSync.java` row `ICHORIAN_KNOWLEDGE`: `LOGIN, RESPAWN`. | T2 |
| S-2b | Resend skill progress on respawn and dimension change. | BUILD | `PlayerStateSync` row `SKILLS`: `LOGIN` only. | T2 |
| S-5b | The Concentrated Blood replacement vial counts confirmed insertion. | BUILD | `common/mission/alchemist/ConcentratedBlood.java` `replace` calls `EarlyInitiation.give`, which is add-or-drop. | T3 |
| M-3 (arch) | Delete `IBloodRoutingTarget` and the two orphan GUI textures to `removed_assets/`. | BUILD | `IBloodRoutingTarget` has no implementer. Three dead `instanceof` branches in `BloodRoutingHelper`. The textures (`vein_selection.png`, `vein_teleport.png`) and the five older orphans are already in `removed_assets/` (MANIFEST lines 405 and 406). `FieldNotesItem` and `LinkedPlayerSource` are already gone. | T6 |
| O-5 | New data on an item being reworked goes in a typed component. A standing rule. | BUILD | `docs/architecture/ARCHITECTURE_STANDARDS.md` section 6 still says O-5 waits on a decision. No item is due now. Only the rule needs recording. | T1 |
| F-22 | Hide the legacy Dictation Table from the main creative tab. | BUILD | `common/init/CreativeTabInit.java` `shouldShowItemInCreativeTab`: the check is still a no-op with its comment. The frozen snapshot row is now line 95 of `src/gameTest/resources/frozen/creative-tab-order.txt`, not 633. | T6 |
| HK-2, E-10 | Remove the last save-compatibility reads when their files are next touched. | PARKED | Still there: `tag.contains("BloodVolume")` reads in `EnzymaticScriptoriumBlockEntity`, `ResonantForgeBlockEntity`, `SanguineCondenserBlockEntity`. The origin-keyed pome count is `InitiatoryDegree.recordPomeConsumed(long bloomOrigin)`. I did not list every legacy blood read. | T1 |
| FX-5 | Move the six dead Mana and Artifice OBJ composite models to `removed_assets/`. | BUILD | Six model JSONs, 12 OBJ and MTL files, and two GeckoLib leftovers. No Java or resource reads them. Listed under T6. | T6 |
| FX-14 | Diff the periodic broadcasts (RT-7). Stop temporary frozen cruor becoming permanent (RT-25). | BUILD | RT-25 is done: `common/manipulation/congeatio/TemporaryIceSavedData.java` saves the melt list with the level and the upkeep checks `level.isLoaded`. RT-7 has four sites left of five (the Circus one now uses a dirty flag). | T7 |
| FX-22 | Normalise the CR CR LF endings in the loom files. | DONE | A byte scan of `src/`, `tools/model_export`, `tools/circus` and `tools/worldgen` found no CR CR LF in any file. `SomaticLoomBlockEntity.java` is pure LF. One test file is mixed (35 CRLF, 1 LF): `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomMissingEnzymeFeedbackTest.java`. | - |
| FX-23 | Make the tests remove their temp folders and clean out the leaked ones. | BUILD | `BloodProfileGameTests.packOverrideReloadAndClientSyncReplaceWholeProfiles` (line 432) and `BloodInjectionGameTests.packPriorityAndReloadRefreshTheResolvedAnswer` (line 672) call `createTempDirectory(Path.of("."), ...)` and never delete it. 2,279 folders in `build/phlegethontic-gametest`, 112 in `build/blood-injection-gametest`, 2 in each of 32 old run folders. | T8 |
| FX-24 | Delete the reused GameTest world folder between sessions. | BUILD | `build/phlegethontic-gametest/world` is 115 GB today. It is the combined run's game directory (`gradle/game-tests.gradle`, `combined.gameDirectory`). No clean step exists. | T8 |
| RV-38 | Keep the core-loop fixtures as `/hemo test` dev scenarios, not subcommand classes. | CONFLICT | Round two P11 moved them at 16:12 on 2026-11-11: `common/command/TemperPoolSubcommand`, `TankSubcommand`, `VatSubcommand`, `GougeSubcommand` over `CoreLoopFixtures`. `GameTestHarnessSourceContractTest` now asserts the scenarios are gone. | - |
| RV-39 | Keep the random, chunk-aligned GameTest origin with 2 margin chunks. | KEEP | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/GameTestIsolation.java` `MARGIN_CHUNKS = 2`. `docs/TESTING.md` "Deterministic GameTests". | T1 |
| PR-29 | Watch the next failures' messages for the known test noise. | PARKED | Diagnostics are in the tests (`docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` P1 table). Nothing to build until one fails again. | - |
| DS-5, C-16 | Fold advanced brewing into tinctures. | UNCLEAR | Three refine recipes in `src/main/resources/data/hemomancy/recipe/advanced_brewing/`. `common/brewing/BrewingResolver.java` also compounds, binds and refills in code. `StationUpgradeCatalog` makes "refine" and "compound" the Athanor's proof of use. Tinctures are Thelemic memory doses. | - |
| DS-6, D-A | Merge the four quest frameworks into one, after alpha. | PARKED | Not checked beyond the audit text. Deferred by the answer. | - |
| DS-7, D-D | Keep both `organic_prism` and `suspended_vivianite`. Their use is not decided. | KEEP | Both registered in `common/init/BlockInit.java`, both have recipes. No other row on the sheet names them. | T1 |
| DS-8, E-5 | Keep all the leftovers. | KEEP | Three husk effigies, `vein_spider`, `vitality_chalice`, `vascular_status_gauge` in `ItemInit`. `gourdvine_tap` in `BlockInit`. All have recipes. No other row deletes them (PR-15 only asks about the Gauge readout). | T1 |
| RV-8 | Keep the Blood Wood tree shape. | KEEP | `common/block/harbinger/plant/BloodwoodGrowthRules.java`: fork at 3 to 5, 3 or 4 limbs, a leaf clot per limb with hanging strands. Not seen in game. | T1 |
| RV-9 | Keep the audit's section E defaults. | KEEP | `data/hemomancy/npc_trade/guardian/silthmere_glaive.json` (4 pale silver, stage 3). `hemorath_rib` in `loot_table/entities/hemorath.json`. `recipe/sanguine_omen.json`. `loot_table/chests/dynasty_castle.json`. | T1 |
| RV-10 | Keep the section D renames. | KEEP | `enthralling_filament`, `harbinger_lodestone`, `barbed_splinter` in lang and `ItemInit`. `ferric_rampart` in `ManipulationInit`. `hemorath_rib` reads "Rib Reliquary". `recipe/iron_nugget_from_bog_iron_smelting.json`. | T1 |
| RV-11 | Keep the Heartwood numbers. | KEEP | `HeartwoodTreeRules.PROJECTION_BLOOD_COST` = 900 x 2 = 1,800. `HeartwoodCoreRules.FULL` = 1800, `PLAYER_NEAR_BLOCKS` = 4. The core drops Ravenous Heartwood or a log, never itself. `WoodSet` has no boats. Art not checked. | T1 |
| RV-25 | Keep the creative tab filing. | KEEP | `common/init/CreativeTabSection.java`: the puppet mount items (`veinwing_harness`, `marrow_spitter_carriage`, `gorebound_yoke`, `cinder_bellows_frame`) in CIRCUS, Silent Archon armour and facsimile eggs in GARDENS, bone bricks and `blood_gutter` in SAINTS, `spawn_egg_verdigris_moth` in UNSTAINED. | T1 |
| RV-26 | Keep the Fane ground style. | KEEP | `config/HemoClientConfig.java` `faneBoundaryStyle` defaults to `SURFACE`. `FaneGroundRenderer` and `FaneBoundaryRenderer` read it. Not seen in game. | T1 |

## Plan

Tasks T1 to T8 are small and independent of each other. T9 to T13 are the combat-rule block and come last. Only one
Gradle run at a time, as always.

### T1. Record the approvals and the keeps (size S)

- Rulings: O-5, HK-2, DS-7, DS-8, RV-8, RV-9, RV-10, RV-11, RV-25, RV-26, RV-39, and the APPROVED flag for every ledger id in this chunk
- Change: no code. Set the Decision column to APPROVED (2026-11-13, owner's answer sheet) for G-1/F-18, F-16, F-17, G-2b, G-7b, F-13, F-14, F-15, F-4, F-19, F-20, S-2b, S-5b, M-3, O-5, F-22. Each row gets its commit id as its task lands. Write O-5 as a rule: an item being reworked moves its new data to a typed component from `DataComponentInit`. Write HK-2 as a rule: whoever next edits a file with a legacy block-blood read or the origin-keyed pome count removes it then. Record the keeps as rulings that bind, with DS-7 and DS-8 marked "kept on purpose, use undecided" so no later cleanup cuts them.
- Files: `docs/architecture/CONSOLIDATION_PROGRESS.md` (Decisions table), `docs/architecture/ARCHITECTURE_STANDARDS.md` (section 6 item row, the line under section 5 about waiting decisions), `docs/OPEN_QUESTIONS.md` (section 1.5 and the rulings sections), `docs/audits/2026-11-02-cleanup/README.md` (D-D, E-5, E-10 rows)
- Tests: none
- Docs: the four files above. `AGENTS.md` only when T10 and T11 land (its combat paragraph calls `Reach.STRICT` moves a pending decision).
- Depends on: nothing

### T2. Two sync table rows (size S)

- Rulings: F-20, S-2b
- Change: in `PlayerStateSync`, the `ICHORIAN_KNOWLEDGE` row gains `Lifecycle.DIMENSION`. The `SKILLS` row gains `Lifecycle.RESPAWN` and `Lifecycle.DIMENSION`.
- Files: `common/capability/PlayerStateSync.java`
- Tests: `OwnerSnapshotGameTests.everySyncedAttachmentReachesTheOwnerOnEachLifecycle` reads the table, so it should follow. Confirm it sees both rows on all three events. `PlayerStateSyncTest` stays green.
- Docs: `ARCHITECTURE_STANDARDS.md` if it lists the login-only exception. Ledger rows.
- Depends on: nothing

### T3. Deliveries that follow the contract (size M)

- Rulings: F-19, S-5b, F-4
- Change:
  - F-19: station kits become a `PendingDelivery` claim. `owed` reads the pending bits in `StationUpgradeProgress`, `settle` clears a bit only when the kit landed. This adds the observational Chamber check and stops a Creative discard counting. Drop the two direct `deliver(player)` calls (`StationUpgradeEvents` line 17, `HarbingerCardinalRiteEvents` line 118). The claim's driver replaces them.
  - S-5b: `ConcentratedBlood.replace` inserts through `PendingDelivery.insert`, counts what landed and drops what did not fit, as the ledger note says. It is not a stored claim (that would add an NBT key). The same change applies to the Bloom Spine reclaim and `CardinalRiteSavedData.deliverRecovery` per the ledger note. I did not open those two bodies.
  - F-4: `ScaleGripEvents.onLivingDeath` moves to `EventPriority.LOWEST`, so a death another handler cancels never reaches it. Parked stacks move from the static map into a new player attachment (copied on death) and are returned by a `PendingDelivery` claim on respawn or login. Returned items are never dropped at the death point.
- Files: `common/capability/PendingDelivery.java`, `common/capability/player/harbinger/station/StationUpgradeProgress.java`, `common/event/StationUpgradeEvents.java`, `common/rite/harbinger/HarbingerCardinalRiteEvents.java`, `common/mission/alchemist/ConcentratedBlood.java`, `common/mission/vicar/EarlyInitiation.java`, `common/rite/CardinalRiteSavedData.java`, `common/item/shared/ScaleGripEvents.java`, `common/capability/HemoAttachmentTypes.java`, `HemoCapabilityAccess`, `HemoCapabilityRegistrar`
- Tests: `StationUpgradeGameTests` (kit held back while dead, during an observational visit, and on a full Creative inventory, then delivered once). A ScaleGrip GameTest (a cancelled death strips nothing, a relog between death and respawn still returns the items). `ScaleGripRulesTest`. `AttachmentAccessTest` and `SessionStateRegistryTest` for the map that goes away.
- Docs: `docs/STATION_UPGRADES.md` (kit delivery), `docs/HEMOMANCY_REFERENCE.md` (ScaleGrip, Concentrated Blood), `AGENTS.md` documentation contract if kits join the listed claims
- Depends on: chunk 10 T2 edits the same `PendingDelivery` claim list. Build one after the other. Chunk 13 if it touches rite recovery delivery.

### T4. Scar and living weapon blood rules (size M)

- Rulings: F-13, F-14, F-15
- Change:
  - F-13: `ScarDefinition.UPKEEP` becomes `Source.of("scar_upkeep", Category.SCAR, false, true)`. Upkeep then stops at the Sanguine Reserve floor for players with that technique. Nobody else sees a change.
  - F-14: `ScarDefinition.onWornTick` re-adds any passive effect the wearer has lost (check every 20 ticks). Milk, death and `/effect clear` then cost at most a second of the effect.
  - F-15: the four `BLOOD_PRICE` sites check `volume.isActive()` first and skip the blood rule for a dormant volume. When the player cannot cover the price, the weapon crumbles or returns to the Staff without the payment (today it empties the volume first). `BLOOD_PRICE` becomes reserve-protected so a living weapon cannot reach under the reserve.
- Files: `common/item/harbinger/scar/ScarDefinition.java`, `common/item/harbinger/tool/living/LivingAxeItem.java`, `LivingBladeItem.java`, `LivingCrossbowItem.java`, `LivingToolItem.java`
- Tests: `BloodAccountingGameTests` (scar upkeep stops at the reserve floor). A scar GameTest for milk. `LivingWeaponCombatGameTests` (dormant blood pays nothing, a short volume crumbles the weapon and keeps its blood). `BloodAccountingGuardrailTest` stays green.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (scar upkeep, living weapon price), `wiki/Blood-Systems.md`, `docs/MANIPULATIONS_AND_CASTING.md` rulings
- Depends on: chunk 04 T3 (Temper pass, scar refill) edits scar code, and chunk 08 T3 may remove `countsAsSpend` from every `Source` constant. Do not run either at the same time as this task.

### T5. Flight revoke asks the other sources (size S)

- Rulings: G-7b
- Change: `FlightArbiter` remembers which `Source`s currently grant a player flight (a UUID-keyed session map cleared through `TransientServerStateEvents`). `grant` adds the source. `revoke` removes it and clears `mayfly` only when none is left. Otherwise it applies the remaining source's flying speed. An Edacious wearer then keeps flight when the Elytra effect lands, in water too.
- Files: `common/util/FlightArbiter.java`, `common/event/TransientServerStateEvents.java`
- Tests: flip `FlightSourceGameTests#elytraLandingDropsEdaciousFlightUntilItsNextTick` (rename it). Add a row for every pair of sources. `HealAndFlightGuardrailTest`, `SessionStateRegistryTest`.
- Docs: `ARCHITECTURE_STANDARDS.md` (flight row), ledger
- Depends on: chunk 12 T12 (possession minors) if it touches the `POSSESSION` source

### T6. Dead code and dead assets to `removed_assets/` (size S)

- Rulings: M-3 (arch), F-22, FX-5
- Change:
  - M-3: move `IBloodRoutingTarget.java` to `removed_assets/` and remove its three `instanceof` branches in `BloodRoutingHelper` (lines 95, 220, 244). Routing then always fills toward the working reserve, which is what every station does today.
  - F-22: exclude `dictation_table` on the block side of `CreativeTabInit` (the item loop never meets it) and delete the no-op item check and its comment. Remove line 95 from the frozen creative tab order in the same change. It does not go on the WIP list unless the owner says so.
  - FX-5: move to `removed_assets/` under the same paths and add them to `MANIFEST.txt`: `models/block/broken_mana_trapazahedron.json`, `broken_mana_trapazahedron_gem.json`, `broken_mana_trapazahedron_runes.json`, `models/item/faction_horn_harbingers.json`, `models/item/special/grimoire_harbinger_closed.json`, `grimoire_harbinger_open.json`, the folders `models/block/obj/broken_mana_trapazahedron/` (6 files, two named "- Copy"), `models/item/obj/books/` (4 files), `models/item/obj/horns/` (2 files), and the GeckoLib leftovers `animations/block/broken_mana_trapazahedron_armature.animation.json` and `geo/block/broken_mana_trapazahedron_armature.geo.json`. All under `src/main/resources/assets/hemomancy/`. The five Lowtide Ruin OBJ models stay.
- Files: as listed, plus `common/capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java`, `common/init/CreativeTabInit.java`, `src/gameTest/resources/frozen/creative-tab-order.txt`, `removed_assets/MANIFEST.txt`
- Tests: `FrozenIdentifierGameTests` (tab order), `CreativeTabSectionTest`, the packaged resource checks in `alphaCheck`. Regenerate `docs/architecture/evidence/source-text-impact-map.csv` if a test read a moved file.
- Docs: `docs/HEMOMANCY_REFERENCE.md` lines 4303 to 4305 and 6516 name `IBloodRoutingTarget` as the routing hook. Ledger M-3 row. `docs/audits/2026-11-11-visual-balance/particles-and-texels.md` OBJ note.
- Depends on: nothing

### T7. Send periodic broadcasts only on change (size M)

- Rulings: FX-14 (RT-7 half)
- Change: keep the last payload per player and send on change, as `RiteZoneSync` and `NpcProgressionMarkerEvents` do. Four sites: `FoundingFaneEvents.onLevelTick` (an empty fane list to every living player in every level every 40 ticks), `BlackheartedEvents.onPlayerTick` (a Black Heart visual for every player every second, active or not), `ChamberOfWillManager.syncTo` (two packets per player every 40 ticks), `CardinalRiteSync.syncActiveRites` (every rite to every player in the level). Each cache is session state cleared through `TransientServerStateEvents`. A login, respawn or dimension change must still get a full send.
- Files: `common/event/worldevent/FoundingFaneEvents.java`, `common/manipulation/mortem/BlackheartedEvents.java`, `common/encounter/ChamberOfWillManager.java`, `common/rite/harbinger/CardinalRiteSync.java`
- Tests: a GameTest per site that counts packets over 200 idle ticks (one, then none) and sees a send on change. `SessionStateRegistryTest`.
- Docs: `docs/audits/2026-11-02-cleanup/README.md` C-19 row (RT-7 and RT-25 closed)
- Depends on: `CardinalRiteSync.java` changed on 2026-11-11 for the planted Staff ring stamp. Rite sync also drives the countdown display, so check a running rite still animates. Chunk 13 if it edits rite sync.

### T8. Test housekeeping: temp folders and the test world (size S)

- Rulings: FX-23, FX-24
- Change:
  - FX-23: both tests delete their temp root in a `finally` after the resource manager closes.
  - FX-24: add a Gradle task `cleanGameTestWorlds` beside `combined.gameDirectory` in `gradle/game-tests.gradle`. It deletes `world/` and any `blood-profile-packs-*` or `blood-response-packs-*` folder under the combined directory and every suite directory in the table. It keeps `logs/`, which `verifyGameTestResourceLog` reads. It is not wired into `runGameTestServer` (see NQ-17-5). The random origin (RV-39) stays, because a world still collects leftovers within a session.
  - The first run of that task is the deletion the owner asked for: 115 GB in `build/phlegethontic-gametest/world`, and 2,455 leaked folders. That is a build-session action. This review deleted nothing.
- Files: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/BloodProfileGameTests.java`, `BloodInjectionGameTests.java`, `gradle/game-tests.gradle`
- Tests: run the two tests twice and see no new folder. `verifyGameTestSuites` stays green.
- Docs: `docs/TESTING.md` (the clean task, when to run it), `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` deferred list
- Depends on: no other session may be running a GameTest server when the clean task runs

**Combat-rule block: T9 to T13.** These five tasks change every damage path. Build them in this order, in one
session at a time, with nothing else touching damage, targeting or blood payment while they are open. Do not run
these at the same time: chunk 04 T1 to T6 and T9 (costs, Temper, repricing, binding limit, field of dark), chunk 05
(anything that changes lash or Shaping damage numbers, `LashTuning`, `LashProfile`, `StrandHits`), chunk 06 T3, T4, T8,
T9 and T11 (mob health and damage), chunk 07 T1 and T2 (the boss definition lives in `ManipulationReactiveEvents`,
the same file as Phoenix Debt), chunk 08 T2 to T4 (enrage, blood debt, fight numbers), chunk 12 T12 (possession cancels
damage and death). Chunk 04 T5, the repricing pass, should start only after T13 is in. No owner balance session
should be judged against a tree with this block half built.

### T9. Characterise first (size M)

- Rulings: G-1, F-18, F-16, G-2b, F-17
- Change: tests only, written against today's code, so each later task shows as a named change in a golden.
  - Targeting: `CombatTargetingMatrixGameTests` has rows for 7 of the 19 wider-preset uses (drudge blood_shot, Silent Severance, Prismatic flash, Autoimmune Edge, Lethean Mute, the morphling look target, Glacier). Add a row for each missing site: Silver Rebuke, Still Pulse, Memory Shear, Quietus Bell, the Saprovitta trail, Sheolic retribution, Crimson Wake, the Pale Silver Bell, the Sporitic Thurible, the Thelemic memory world effect, the Phantasmal "no monster near" check, the Unstained rite gather, and the Drudge's activation_potential. Add a kill-credit column for the four damaging F-16 sites (today: no credit).
  - Damage stages: a new test in `CombatOrderGameTests` (suite `combat_order_validation`). A fixed 10-point hit on a player with active blood. It records final damage, mL of blood lost and vein strain for: no modifier, each defender modifier alone (Sheolic stance, Silent Archon negation, avatar, sabatons, Chitinite, Iron Will, Still Waters, the memory tool), each attacker modifier alone (Second Pulse, Sanguine Fists, Penumbral ambush, the Unstained milestone), two stacks (avatar with sabatons, Iron Will with avatar), and a zeroed hit. Results go in a golden file.
  - Death-saves: a lethal hit with each save armed alone and in pairs: Silent Refusal, Phoenix Debt, Endless Hour, Ink Mantle, Cryptobiosis, Curved Horn. Record who fired, blood paid, health after, and the shared cooldown.
- Files: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CombatTargetingMatrixGameTests.java`, `CombatOrderGameTests.java`, a new golden under `src/gameTest/resources/`
- Tests: these are the tests
- Docs: `docs/TESTING.md` (the two goldens)
- Depends on: nothing. It must be green on the tree as it is before T10 starts.

### T10. G-2b: puppets and allies leave the Monster filters (size M)

- Rulings: G-2b
- Change: set `allies = false` in `Reach.MONSTER_FILTER` and `Reach.MONSTER_FILTER_ANY_STATE`. This spares the caster's own and allied puppets, pets, team-mates' monsters and mobs the caster has impressed. Sites, recounted today:
  - `MONSTER_FILTER` (11): `PhanstmalBloodlustArmorAbilityHandler` line 94, `UnstainedCardinalRiteEvents` line 380, `ThelemicMemoryWorldEvents` line 140, `PaleSilverBellItem` line 57, `ArmorSetBonusHandler` line 263 (Prismatic flash), `StillArtInit` lines 72 (Lethean Mute), 84 (Still Pulse), 115 (Memory Shear), 146 (Quietus Bell), `SporiticThuribleEvents` line 136, `DrudgeActions` line 143.
  - `MONSTER_FILTER_ANY_STATE` (4): `ScarsEntityEventHandler` line 93 (Saprovitta trail), `ToggleableSkillEvents` line 55 (Crimson Wake), `ScarDefinition` line 189 (Glacier), `DrudgeActions` line 49 (`monsters`, behind 27 lookups in 39 Drudge actions).
  - Three of these pass a caster that is not a player, and `canHarm(LivingEntity, LivingEntity, Reach)` throws for those once the preset has `allies = false`. The two `DrudgeActions` sites move to `ManipulationCombatHelper.canHarmFor(drudge, monster)`, which already resolves an `OwnableEntity` owner and applies that player's strict `canHarm`. With the owner offline it falls back to team and `canAttack`. It also stops reaching dying bodies and Blood Constructs. `ScarDefinition` line 189 asks the preset only when its wearer is a player.
- Files: `common/manipulation/ManipulationCombatHelper.java`, `common/manipulation/DrudgeActions.java`, `common/item/harbinger/scar/ScarDefinition.java`
- Tests: flip the matrix rows for drudge blood_shot, Prismatic flash, Lethean Mute, Glacier and every monster-filter row T9 added (`hostile ally puppet boss` becomes `hostile boss`). Rewrite `reachPresetsReproduceTheirLegacyFilters`: the two presets no longer reproduce the old filters. Update the pinned strings in `CombatTargetingGuardrailTest` (`drudgeWorkLivesInDrudgeActions`).
- Docs: ledger G-2b row, `ARCHITECTURE_STANDARDS.md` section 5, `docs/MANIPULATIONS_AND_CASTING.md` rulings, `docs/HEMOMANCY_REFERENCE.md` (Drudges, Still Arts, the Glacier scar), `wiki/School-Combat.md`
- Depends on: T9. Chunk 04's Drudge Grave Debt finding edits `DrudgeActions`.

### T11. F-16: attributed area damage that respects allies and PvP (size M)

- Rulings: F-16
- Change: five sites, recounted today.
  - Silent Severance: `SilentArchonArmorAbilityHandler.activateSilentArchonSeverance` line 132, `Reach.UNGATED_SURVIVAL` becomes the strict `canHarm`.
  - Autoimmune Edge: `StillArtInit` line 155, `Reach.UNGATED` becomes strict.
  - Silver Rebuke: `StillArtInit` line 57, `Reach.UNGATED` becomes strict. It pushes and slows, so there is nothing to attribute.
  - Saprovitta trail: `ScarsEntityEventHandler` line 93 becomes strict.
  - Sheolic retribution: `SheolicBastionBloodlustArmorAbilityHandler.triggerCrimsonRetribution` line 157 keeps acting on its attacker.
  - The four damaging sites replace `player.damageSources().magic()` with a source that names the player (`damageSources().indirectMagic(player, player)`, no school context, my call). Kills then count for the player: blood on kill, Temper, skill points, purity. Pets, allies, own puppets and PvP-off players are no longer hit.
  - `Reach.UNGATED_SURVIVAL` has no user left and is deleted. `Reach.UNGATED` keeps one user, the morphling look target (`MorphlingItem.findLookTarget`), which the `Reach` Javadoc says needs its own decision, as does the Verdigris mark. Both are left alone.
  - Player-attributed hits pass through `SchoolCombatEvents.prepare` (HIGH), which already cancels a weapon-school hit on the caster's own puppet. The step plan must check these four sources are not treated as weapon hits.
- Files: `common/armor/ability/SilentArchonArmorAbilityHandler.java`, `common/armor/ability/SheolicBastionBloodlustArmorAbilityHandler.java`, `common/init/StillArtInit.java`, `common/capability/player/harbinger/scar/ScarsEntityEventHandler.java`, `common/manipulation/ManipulationCombatHelper.java`
- Tests: flip the matrix rows for Silent Severance and Autoimmune Edge (`hostile pet ally puppet boss pvp-on pvp-off` becomes `hostile boss pvp-on`) and the T9 rows for Silver Rebuke, the trail and the retribution. The kill-credit column turns to "player". Update `CombatTargetingGuardrailTest.areaAndLookTargetsAskCanHarm`.
- Docs: ledger F-16 row, `docs/HEMOMANCY_REFERENCE.md` lines 3602 (Feeding Wake) and 3747 (Autoimmune Edge, "nearby living bodies") and the Silent Archon and Sheolic set text, `wiki/School-Combat.md`, `AGENTS.md` combat paragraph (the "pending decision, G-2b or F-16" clause goes)
- Depends on: T10. Sheolic retribution also places `crimson_flames` with a raw `setBlock` and sends a vanilla `CRIMSON_SPORE` particle. Neither is F-16. Fix them here only if the file is open anyway (`PlayerBlockEdits`, `HemoParticleData`).

### T12. F-17: the Curved Horn joins the Last Rite group (size S)

- Rulings: F-17
- Change: add `LastRiteHelper.CURVED_HORN_ID`. The Horn is armed while it sits in equipment slot 5 and nothing else is armed (the same rule Phoenix Debt uses). Its save asks `LastRiteHelper.canFire` and calls `consume`. It then shares the 12,000-tick cooldown: today it saves on every death for 1 durability. Until T13 it stays on `LivingDeathEvent`.
- Files: `common/event/LastRiteHelper.java`, `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java`
- Tests: `LastRiteRulesTest` (Horn id). The T9 death-save golden changes for the Horn rows. A GameTest: two deaths inside the cooldown, the Horn saves the first only.
- Docs: `docs/HEMOMANCY_REFERENCE.md` line 1157 (Last Rite group) and the Curved Horn row near line 4398, `wiki/Blood-Systems.md`, ledger F-17 row
- Depends on: T9. NQ-17-3 for the arming rule.

### T13. G-1, F-18: damage stages and one death-save stage (size L)

- Rulings: G-1, F-18
- Change: every `LivingDamageEvent.Pre` subscriber gets an explicit priority from one stage table. The 15 handlers today:
  - HIGH: `ThelemicMemoryEvents.applySecondPulse`, `UnstainedMilestoneHandler.onLivingHurt`.
  - NORMAL: the Phantasmal reaction, the Sheolic stance (zeroes), Silent Archon negation (zeroes), `BloodVolumeEvents.onPlayerDamaged` (Iron Will, then blood loss), the avatar reduction in `KnownManipulationEvents`, scar reactions, `VascularSystemEvents.onPlayerDamaged` (vein strain), `MnemonistPuppetEvents`, `ArmorSetBonusHandler.onPlayerHurt` (Silent Refusal, sabatons, Chitinite, thorns, Prismatic flash), `HematicMemoryToolEvents`, `StillWatersEvents`.
  - LOWEST: `ThelemicMemoryEvents.applySanguineFists`, `ManipulationReactiveEvents.onFinalDamage` (Endless Hour deferral, Penumbral ambush, Phoenix Debt).
  - Proposed stages: attacker multipliers at HIGH (Sanguine Fists and Penumbral ambush move up). Defender reductions at NORMAL (Iron Will is split out of `BloodVolumeEvents`). One death-save stage at LOW in one class: Endless Hour's deferral, then the single armed Last Rite (Silent Refusal, Phoenix Debt, the Curved Horn, moved off `LivingDeathEvent`). Consumers at LOWEST: blood lost on the hit (`WOUND`), the wound total, the Formation drop roll, vein strain, the Mnemonist puppet's memory. Reactions that do not change the amount keep NORMAL.
  - Ink Mantle and Cryptobiosis fire from `LivingDamageEvent.Post` today (`EquippedMorphlingEvents.onConfirmedPlayerHurt`). They move into the stage only if their trigger can be tested before damage lands. I did not read their conditions in full. The step plan decides.
  - The 20 `LivingIncomingDamageEvent` and 15 `Post` handlers keep their priorities and are listed in the table. The three other death cancels (possession, the Mycophant encounter, a Chamber visit) are not saves and stay.
  - What changes for a player: blood lost and vein strain follow the damage actually taken. A zeroed hit costs no blood. A reduced hit costs less. Silent Refusal stops testing lethality before the late modifiers.
- Files: the 14 files that hold the handlers above, a new stage class under `common/damage/`, `common/event/LastRiteHelper.java`, `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java`
- Tests: re-cut the T9 damage-stage and death-save goldens in the same commit and explain every changed line in the ledger. A new guardrail `DamageStageGuardrailTest` under `src/test/java/com/vincenthuto/hemomancy/architecture/`: every `Pre` subscriber is in the table with its stage, a reader that pays blood or strain is at LOWEST, and no death-save sits outside the stage class. `MorphlingPowerGameTests`, `ManipulationBalancePass2StateGameTests`, `ManipulationCastFixGameTests` and `ManipulationTargetingGameTests` use `LastRiteHelper` and must stay green (I did not read what each asserts). Add the guardrail to the index in `ARCHITECTURE_STANDARDS.md` and regenerate the source-text impact map.
- Docs: `ARCHITECTURE_STANDARDS.md` section 7 (a damage stage table beside the heal one, and the "wait on decisions" line goes), ledger G-1/F-18 row, `docs/HEMOMANCY_REFERENCE.md` (line 1200 `bloodDrainPerDamage`, line 3485 Iron Will's site, the Last Rite text), `wiki/Blood-Systems.md`, `wiki/School-Combat.md`, `docs/MANIPULATIONS_AND_CASTING.md` rulings, `AGENTS.md` (a line naming the stage table and its guardrail)
- Depends on: T9, T11, T12. NQ-17-1 and NQ-17-2. Any new death-save from another chunk (a clot-revive rule, if one is built) must be written into this stage, so it lands after T13 or inside it.

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |
| RV-38 | Keep the `/hemo` core-loop fixtures as dev scenarios rather than `<Name>Subcommand` classes. | Round two P11, 2026-11-11 16:12, replaced the five `/hemo test` scenarios with four dev-only branches (`/hemo temperpool`, `tank`, `vat`, `gouge`). `CoreLoopTestScenarios` is deleted and a test asserts it stays gone. `docs/TESTING.md`, `ARCHITECTURE_STANDARDS.md` and CQ-62 describe the new form. | The fixtures are subcommand classes now. Keep them that way (CQ-62, as built), or move them back to `/hemo test` scenarios? |

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-17-1 | G-1, F-18 | When a death-save catches a lethal hit, does the hit still cost its full blood? | A: blood loss and vein strain read the amount after the save, so a saved hit costs blood only for what got through. B: they read it before the save, so a saved lethal hit costs the full amount. | A, because Silent Refusal and Phoenix Debt already charge their own blood price for the save. |
| NQ-17-2 | F-18 | F-18 also said reductions stack with no cap. The approved change is the stages only. Add a cap? | A: no cap. The ordering test prints the worst stack so it can be judged. B: cap the total reduction at a number the owner gives. | A for now, and decide from the T9 golden. |
| NQ-17-3 | F-17 | Only one Last Rite source is armed at a time. When a player has the Horn and another source, which one is armed? | A: the Horn arms only when nothing else is armed, as Phoenix Debt does. B: the Horn takes the slot whenever it is worn. | A. Either way the Horn now shares the 10-minute cooldown and stops saving on every death. Say if that is not wanted. |
| NQ-17-4 | DS-5 | What does "fold into tinctures" remove? Advanced brewing is the three refine recipes (Ferric Poise, Fervent Reprisal, Nerve Surge) plus compounding, bound vessels and refills in code. The Athanor's proof of use is "a refinement and a compound". Tinctures are doses of the eight Thelemic memories. | A: delete only the three refine recipes and their riders. Keep compounding and bound vessels. The Athanor proof becomes a compound alone. B: also move the three riders onto three named tinctures (the owner picks which). C: delete all of advanced brewing. The Condenser and Athanor become capacity, heat and speed only, and the Athanor needs a new proof. | A, because it is the smallest cut and loses no working feature. B needs the owner's mapping. |
| NQ-17-5 | FX-24 | Should the world be deleted automatically before every combined GameTest run? | A: a manual `cleanGameTestWorlds` task run at the start of a session. B: delete `world/` in `doFirst` of every `runGameTestServer`. | A, because parallel sessions share the build folder and B could delete a world another run is using. |
| NQ-17-6 | F-22 | The Dictation Table keeps its crafting recipe (`recipe/dictation_table.json`). Hide it from the tab only, or withhold the recipe too? | A: hide from the main tab only, as ruled. B: also move the recipe to `removed_assets/`. | A, since the ruling says hide. B if "legacy" means players should not make it. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| G-1, F-18 | Play a combat session after T13 and judge blood lost per hit before the balance pass. | Owner | T13 built |
| F-16, G-2b | Check with two clients that pets, puppets and a PvP-off player are spared. | Owner | T10, T11 built |
| F-17 | Rule the arming order and accept the shared cooldown. | Owner | NQ-17-3 |
| DS-5 | Say what folding removes. Then it becomes a task that touches `StationUpgradeCatalog`, the JEI category, `AdvancedBrewingDialogue`, the Liber page `alembic/pages/refined_and_compound`, `AdvancedBrewingGameTests` and `docs/STATION_UPGRADES.md`. | Owner | NQ-17-4 |
| RV-38 | Confirm the subcommand form stands. | Owner | Conflict sheet |
| FX-24 | Delete `build/phlegethontic-gametest/world` (115 GB) with no GameTest server running. | Build | T8 |
| FX-23 | Delete the 2,455 leaked `blood-*-packs-*` folders. | Build | T8 |
| FX-22 | Normalise the one mixed test file if it is ever edited. Nothing else is left. | Later | Nothing |
| PR-29 | Read the diagnostic message the next time one of the known flakes fails, and fix the cause. | Build | A failure |
| HK-2, E-10 | Remove the legacy block-blood reads and the origin-keyed pome count when their files are next edited. | Later | Those files being touched |
| DS-6, D-A | Merge the four quest frameworks. | Later | Post-alpha |
| DS-7 | Decide what `organic_prism` and `suspended_vivianite` are for. | Owner | Nothing |
| F-16 | The morphling look target and the Verdigris mark still reach allies and PvP-off players. Each needs its own ruling. | Owner | Nothing |
| G-2c | One boss definition. Approved in chunk 07 (its T1). It edits the matrix's boss column, so build it outside T9 to T13. | Build | Chunk 07 |
