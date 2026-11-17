# 10. D0 to D6 journey: proofs, deliveries, outposts, quest cards

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built. B21 (2026-11-15): the two PT-19 reissue probes now pin the kept rule and are required. |
| T2 | Built (P11, B21). B21 moved the late rewards (Monolith scrap, church map, waybill, Relay Chart, Artificer fittings and materials, D7 fitting, Vein-Mason continuation, D5 and D6 rewards and replacements) onto `OwedRewards`, and built the station half (NQ-10-10): only its player takes an assigned Centrifuge output, and the Effigy pattern and its credit go to the preparer. |
| T3 | Built (P10). |
| T4 | Built (B21): PT-8 (the Display is protected and relinks, the Hermit reads his Display, the awakened-blood line first, no farewell from another temple's Hermit) and PT-9 (the recruit kneels to accept). |
| T5 | Built in part (B21): PT-2, PT-15, PT-1b (a per-player tally of poured blood that the D1 proof does not count). Left: PT-1, the filled, unowned blood font at each outpost and temple (NQ-10-1 A) and the hint that points at it. |
| T6 | Built (P10, B21): PT-12, PT-33, PT-11 (100 ticks in bed), PT-18 with the Alchemist's clear option, PT-26. |
| T7 | Built (P10, B21): B21 reworded the D4 hint (the Effigy prepares, the Iron Brazier commits) and the Woven Vessel card counts two steps. Left: proving PT-24 where the Chamber loads. |
| T8 | Built (P11, B21): B21 added the D5+ map reissue, moved the issued lessons into an attachment, any Varicose section counts, the fallback strain route and the wider refusal audit. Left: PT-30, the scar blank trade (its price clashes with the one-card-per-want rule). |
| T9 | Built (P10, B21): B21 added PT-5 (Monolith craft at D5, one authored map node), PT-27 (a scrap whenever none is carried or owed), PT-41 (either hand, 10 rapport and 2 Vivacious Enzyme) and the `complete_vigil` wording. |
| T10 | Built in part (B21): PT-23 (the nearest clear cell within 3 blocks). Left: PT-10, the shared vacancy store (the Court rule it would share does not refill in `courtLeadLostForGood`), and PT-85 (held). |
| PT-31 (no task) | Built (B23, 2026-11-15, NQ-10-3 A): the Sporitic Thurible is a D8 Apotheos relic (use, burn, Working craft, atlas, tab), same effect; probe `d4ThuribleHasATeacher` retired to `removed_assets/`. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings close most of the D0 to D6 playthrough probes. Three shared mechanisms carry the bulk of the work: one
`PendingDelivery` claim for every NPC reward (PT-3, PT-16), one "credit by UUID at next login" store (PT-17, PT-20), and
one vacancy rule for unique Harbinger NPCs (PT-10, PT-23, PT-85). The rest is small, per Degree.

Surprises from the code:

- HK-3 is half done already. The eight required failures were fixed on 2026-11-11 (round two P2). The suite was re-run
  at 16:41 that day, after the sheet was written: 427 required pass, 98 optional fail, the same 98 as before the core
  loop. It has not been run since the lash, Shaping and HUD work of 11-12 and 11-13.
- None of the "may be moot" probes in this chunk went moot. PT-1 and PT-13 pass and only log. PT-17's Venous Stone half
  passes now and its premise changed. The others fail exactly as before.
- PT-1 and PT-1b pull against each other. Core loop CL-8 lets a player draw only from their own tanks and unowned ones.
  With their own projected blood excluded, a D1 player has no reservoir left to be pointed at.
- PT-10's answer is PT-11's text ("Settle after a minimum time in bed"). The sheet's own recommendation was "A respawn
  rule for unique NPCs". I planned that and ask for a confirm.
- PT-29's sheet text names the wrong config. The strain depends on `vascularDegradationOnManipEnabled`, which defaults
  to true. Only a server that turns it off is stuck.
- PT-26 and PT-31 cannot be planned as written. Both are new questions.

Counts: 39 rows. BUILD 35, KEEP 2, DONE 0, MOOT 0, CONFLICT 0, UNCLEAR 2, OWNER 0, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| HK-3 | Fix the eight required failures and re-run the probes that may be moot. | BUILD | `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` P2 and P12, `build/p12-playthrough-optional.txt`, `build/playthrough-gametest/logs/latest.log` (2026-11-11 16:41). The eight failures are fixed. The re-run is stale by two days of work. | T1 |
| PT-1 | Keep the living rate and the 500 mL proof. Point the hint at reservoirs. | BUILD | `BloodAbsorptionItem.draw`, `FirstBloodcraftAssignment.recordAbsorption`. Log: 500 mL from 53 cows in 4992 ticks, so the probe passes. `hemomancy.vicar.neophyte.absorption_hint` already names reservoirs. See NQ-10-1. | T5 |
| PT-1b | The player's own projected blood does not count toward the D1 proof. | BUILD | `BlockBloodInteractions.tryAbsorbFromLookedAtBlock` counts any reservoir draw. `BlockBloodVolume.owner()` exists since the core loop. Probe `condenserRoundTripCountsAsAbsorption` still fails (500 mL in 16 ticks). | T5 |
| PT-2 | Keep net counting. Reword the hint that says everything counts. | BUILD | `recordAbsorption(player, Math.min(personalBlood, room))`. `hemomancy.vicar.blood_crafting.absorption` says "counts everything you absorb". Probe still fails (0 mL at a full vessel). | T5 |
| PT-4 | Reissue the Mason's Respite map when none is carried. | BUILD | `VicarDialogueEvents.handleVicarMasonsRespiteDirective`: after the advancement it only says "known". | T8 |
| PT-6 | Any Blood Shot weave still opens the Brotherhood. The ledger and the Mnemonist say the index is optional. | BUILD | Lang `screen.hemomancy.harbinger_assignment_ledger.woven_vessel.desc` reads as if the index is required. Probe `weaveWithoutMnemonistUnlocksD4` asserts the opposite of the ruling. | T7 |
| PT-8 | Protect the Mortal Display and relink it to its Hermit. An awakened player is told the heart cannot attach (8b). | BUILD | `MortalDisplayBlock.handleInteraction` checks the blessing before active blood. `BlockInit.mortal_display` has strength 1.5. `HarbingerHermitEntity` reads only `TempleOathRules.hasClaimedHeartFrom`. | T4 |
| PT-9 | Founder-led reception asks the recruit first. | BUILD | `EarlyInitiation.tryProject` calls `begin(recruit, founder)` with no prompt. The form of the prompt is NQ-10-9. | T4 |
| PT-10 | A respawn rule for unique NPCs, one rule shared. Read from the sheet's recommendation, since the answer is PT-11's text. | BUILD | `MasonsRespiteStructure.spawnMob` spawns the Anchorite once. `ProtectedNpc` admits only `/kill` and Creative damage. `CourtVacancies` already refills Court marks after a day. See NQ-10-4. | T10 |
| PT-11 | Concentrated Blood settles after a minimum time in bed, without the server's night skip. | BUILD | `ChamberVisitEvents.onWake` passes `completed` only on a night skip. `ConcentratedBlood.completeSleep` returns false otherwise. Log: 120 ticks in bed, still D2. | T6 |
| PT-12 | Heat the outpost template's Alembic. | BUILD | `tools/worldgen/harbinger_sites/outpost.py` `alchemist()` puts the Alembic at (8, 3, 5) with no heat block below. `GhastlyAlembicBlockEntity.isHeatSource` reads the block below. Not moot. | T6 |
| PT-13 | Outpost stations keep needing a personal craft. | KEEP | Log: `hemomancy.machine_access.locked` refuses a player who never crafted a Centrifuge. The ledger reads 8/8. Probe passes. | none |
| PT-14 | Persist the lesson the Anchorite issued. | BUILD | `VeinMasonDialogueEvents` calls `VeinMasonScarLesson.forPlayer`, `continuationForPlayer` and `strongestForPlayer` live at issue and at replace. Not moot. | T8 |
| PT-15 | The Hermit Road report can be made at any Degree. | BUILD | The report option is built only in the D1 tree (`HarbingerVicarDialogueTrees` lines 231 to 239). The handler has no Degree check. | T5 |
| PT-16 | A Creative discard of the starter kit stays owed, as `PendingDelivery` does. | BUILD | `EarlyInitiation.give` uses `Inventory.add`, then grants `hemomancy/initiation_blood_claimed`. | T2 |
| PT-17 | Record the proof by UUID and apply it at next login. | BUILD | Structure half stands: `PendingBloodCraftManager` skips `recordStructure` when the crafter is offline. Venous Stone half changed: `BloodInfusionManager.complete` now leaves the block standing when the infuser has left, so nothing drops unproven. The probe passes. | T3 |
| PT-18 | Refuse a second assigned spin until the first is recovered. | BUILD | `FirstSeparationAssignment.beginAssignmentSpin` overwrites `DATA_ASSIGNED_SPIN`. A lost first output would lock the chapter, see NQ-10-6. | T6 |
| PT-19 | Reissues keep checking only the carried inventory, as a safety net. | KEEP | `ConcentratedBlood.replace` checks `carries(player)`. Same pattern in `VicarDialogueEvents.handleVicarBloodcraftBlueprint`. | none |
| PT-20 | Record the first-weave milestone by UUID and apply it at next login. | BUILD | `SomaticLoomBlockEntity.tickCompletion` credits only `player instanceof ServerPlayer`. An offline weaver gets the memory and no credit. | T3 |
| PT-22 | A section already Varicose at D5 counts once as the Mason's strain. | BUILD | `VeinMasonAssignments.onThelemicStrain` needs `before != VARICOSE`. | T8 |
| PT-23 | An obstructed outpost marker uses a fallback spot. | BUILD | `HarbingerOutpostStructure.spawnMob` returns silently when the marker or the cell above is not air. | T10 |
| PT-24 | A guided Chamber visit completes only on timeout or the paired exit. | BUILD | `ChamberVisitService.returnFromVisit` sets `guidedComplete` for every return. `ChamberVisitEvents.onLogout` and `onDeath` call it. | T7 |
| PT-26 | The answer is a copy of the question. No ruling to act on. | UNCLEAR | `ConcentratedBlood.completeSleep` returns false with no line when `EarlyInitiation.attached` is false. Probe still fails. See NQ-10-2. | none |
| PT-27 | One Monolith scrap per player. | BUILD | `VicarDialogueEvents.handleGiveSanguineMonolithBlueprint` has no once flag. Log: 3 asks, 3 carried. Clashes with PT-19, see NQ-10-7. | T9 |
| PT-28 | A refusal line wherever a reward can be refused. | BUILD | Silent returns in `VeinMasonDialogueEvents.handleAnchoriteReward` and `replaceAnchoritePattern`, and in `VicarDialogueEvents` (blueprints, scrap, church map). The Anchorite probe itself passes, because the option is hidden. | T8 |
| PT-29 | A fallback strain route when degradation is off. | BUILD | `ThelemicMemoryActivationService` calls `onThelemicStrain` only when `VASCULAR_DEGRADATION_ON_MANIP_ENABLED` is on. That config defaults to true in `HemoServerConfig`. The sheet named `vascularDebuffsEnabled`, which is a different switch. See NQ-10-11. | T8 |
| PT-30 | The Anchorite trades a scar blank. | BUILD | `data/hemomancy/npc_trade/cicatrix_anchorite/` holds only `bone.json` and `clay.json`. | T8 |
| PT-31 | No NPC line at D4. The Sporitic Thurible "should probably" move to be an Apotheos relic. | UNCLEAR | `SporiticThuribleItem` (D4 gate in `SporiticThuribleRules.REQUIRED_DEGREE`), `recipe/cardinal_rite/working/sporitic_thurible.json` (`required_degree` 4). A new design with open parts. See NQ-10-3. | none |
| PT-33 | Offer the Specimen Jar lesson in the D3+ Alchemist trees. | BUILD | `SpecimenJarLessonDialogue.append` hangs the option on `living_bestiary_intro`. Log: a D3 to D5 player is never offered it, and a forged event still teaches it. | T6 |
| PT-41 | The Basin commission reads either hand. Commissions pay a reward. | BUILD | `PhlegethonticCommission.progress` reads `getMainHandItem()` only. `report` gives nothing. The reward is not named, see NQ-10-8. | T9 |
| PT-3 | One-time NPC rewards and shared station outputs become `PendingDelivery` claims. | BUILD | `DialogueRewards.giveOrDropAtEntity` drops at the NPC first. Log confirms 7 of 10 land at the NPC's feet. `CentrifugeOutputSlot` and `MasonsEffigyBlockEntity` hand outputs to whoever is there. | T2 |
| PT-5 | Allow the Monolith Working craft at D5. | BUILD | `recipe/cardinal_rite/working/sanguine_monolith.json` has `"required_degree": 6`. Its Quintessence comes from Exsanguination, a D3 rite. | T9 |
| PT-7 | Rewrite the Vicar's D3 and D4 hints to name the real gate. | BUILD | Lang `hemomancy.vicar.initiate.degree_hint` says "study the Bloodline system". `hemomancy.vicar.adept.degree_hint` says "skill tree". | T7 |
| PT-39 | A Vicar line says where Sanguine Quintessence comes from. | BUILD | Only `hemomancy.rite.exsanguination.desc` and a completion message name the source. No speaker does. Not moot. | T9 |
| PT-43 | Offer the Monolith blueprint at D5 and above. | BUILD | The `monolith_rumor` node exists only in the D5 Vicar tree. The handler checks `ProgressionGates.isDegree(player, 5)`. | T9 |
| PT-71 | The D6 Anchorite counsel is a Quest Work card with a sigil. | BUILD | `HarbingerMnemonistDialogueTrees` line 194: the option carries `EVENT_ANCHORITE_COUNSEL` and `.withLore()`. Log: read once, sigil gone, counsel not recorded. | T9 |
| PT-85 | A summoned outpost NPC is sent home after use. | BUILD | `PacketLedgerAction.moveLoadedRecruit` teleports and never returns the NPC. Only `HarbingerVicarEntity` stores `outpostHome`. "After use" is not defined, see NQ-10-5. | T10 |
| PT-86 | Each D5 and D6 Main step is a Quest card with a sigil. | BUILD | `HarbingerVicarDialogueTrees`: `what_degree_next` is `.withLore()` at D5 and D6. `withCovenantGuidance` swaps its lines only. | T9 |
| D-23 | The Monolith warns before the shatter that the free Cornerstone is lost. | BUILD | Lang `hemomancy.monolith.archon.press_again` warns of the break and says nothing of the Cornerstone. | T9 |

## Plan

### T1. Playthrough baseline and probe re-read (size S)

- Rulings: HK-3
- Change: run `./gradlew.bat runPlaythroughGameTestServer --offline` once, alone, and compare the optional list with
  `build/p12-playthrough-optional.txt`. Nothing in `src/gameTest/.../playthrough/` changed after 2026-11-11 02:49, so
  any new failure comes from the 11-12 and 11-13 work. Then re-read the probes below in
  `build/playthrough-gametest/logs/latest.log`. What I expect from reading the code and the last log:
  - PT-1 `livingCowAbsorptionRate`: passes, logs 53 cows. Not moot. The rate is unchanged.
  - PT-1b `condenserRoundTripCountsAsAbsorption`: fails. Not moot.
  - PT-2 `fullVesselAbsorptionCounts`: fails. Not moot.
  - PT-12 `outpostAlembicHasNoHeat`: fails. Not moot. The probe places an Alembic on plain floor, so heating the
    template will not turn it green. It needs a rewrite in T6.
  - PT-13 `outpostOnlyStationsLeaveLedgerIncomplete`: passes. Kept.
  - PT-14 `temperDriftDuplicatesPatterns`: fails. Not moot. The Temper rename did not change the live lookup.
  - PT-26 (HK-3 calls it D-26) `charmRemovedWhilePending`: fails. Not moot.
  - PT-39 `quintessenceHasNoPointer`: fails. Not moot. The 21 round-two lessons did not add a source line.
  - PT-17 `proofLostWhenOfflineAtCollapse`: passes since the core loop. See T3.
  - PT-25 (chunk 4) and PT-88 (chunk 13) are not this chunk's rows. PT-25 is logged by a passing test (900 mL back
    from 1,000). PT-88 `apotheosRiteWithoutRescue` failed in every run but one.
  - F-12 (chunk 13) and F-13 (chunk 17) are not in this suite. F-12 is still pinned as pending by
    `BloodAccountingGameTests.riteLockedStationStillTakesProjectedBloodUntilF12` in the main suite. Those chunks re-run
    them.
- Files: none changed. Read `build/playthrough-gametest/logs/latest.log`.
- Tests: the playthrough suite itself.
- Docs: `docs/PLAYTHROUGH_TESTS.md` status line, `docs/TESTING.md` status, `docs/OPEN_QUESTIONS.md` section 1.6.
- Depends on: nothing. Only one Gradle build may run at a time.

### T2. NPC rewards and the starter kit through PendingDelivery (size L)

- Rulings: PT-3, PT-16
- Change: one new claim, registered in `PendingDelivery.Claims.ALL`, that holds exact stacks owed by any dialogue
  reward. `DialogueRewards.giveOrDropAtEntity` stops dropping at the NPC. It appends the stacks to the player's owed
  list, records the one-time flag as today, and calls `PendingDelivery.deliver`. Whatever does not fit stays owed and
  the player is told to make room. The seven rewards the survey found at the NPC's feet:
  1. First Bloodcraft kit (4 Hematic Iron Scrap, 8 Befouling Ash, 2 Sanguine Formation, 1 Vivacious Enzyme).
  2. Hermit Road report (4 Befouling Ash).
  3. Mnemonist starter memory.
  4. Worn Vow reward (4 Hematic Iron Scrap).
  5. Woven Vessel kit (1 Bleeding Bulb, 3 Vivacious Enzyme).
  6. Mason's Respite map.
  7. Vein-Mason first lesson (pattern, blank, catalyst, knapper).
  The same call sites cover the late rewards (Monolith scrap, church map, waybill, Relay Chart, D7 fitting), so chunks
  12, 14 and 15 get them for free. The starter kit (Sanguine Conduit, 4 Bloody Flasks, ledger) in `EarlyInitiation.tick`
  moves to the same claim, so a Creative discard no longer counts. Store the owed list in a new attachment copied on
  death, not in new `getPersistentData()` keys. Shared station outputs: the assigned Centrifuge output already carries
  its player id, so `CentrifugeOutputSlot` refuses another player's take. The Effigy's finished pattern goes to the
  preparer's owed list and the first-pattern advancement to the preparer. That last part is my reading, see NQ-10-10.
- Files: `common/capability/PendingDelivery.java`, `common/entity/npc/dialogue/route/DialogueRewards.java` and its
  callers in `VicarDialogueEvents`, `AlchemistDialogueEvents`, `ArtificerDialogueEvents`, `MnemonistDialogueEvents`,
  `VeinMasonDialogueEvents`, `VoyagerDialogueEvents`, `ScoutDialogueEvents`, `common/mission/vicar/EarlyInitiation.java`,
  `common/menu/slot/CentrifugeOutputSlot.java`, `common/tile/harbinger/functional/MasonsEffigyBlockEntity.java`,
  `common/capability/HemoAttachmentTypes.java`.
- Tests: `PendingDeliveryTest`, `PendingDeliveryRegistryTest`, `PersistentDataAllowListTest`. Flip to required:
  `rewardLandingSurvey`, `rewardLandingSurveyLate`, `secondPlayerTakesTheKit`, `twoPlayersWovenVesselKit`,
  `twoPlayersOneCentrifuge`, `sharedEffigyChargeCredit`, `creativeFullInventoryStarterKit`. Many required playthrough
  tests call `pickUpNear` after a claim and must change. `dialogue_route_validation` fingerprints will move.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` "Rulings that bind", `docs/HEMOMANCY_REFERENCE.md`, `docs/PLAYTHROUGH_TESTS.md`,
  `AGENTS.md` documentation contract (one line for the new claim).
- Depends on: T1. NQ-10-10 for the station half. The AGENTS.md rules hold: living player, no delivery in an
  observational Chamber visit, count what lands, no inferred claims for old saves.

### T3. Credit by UUID at next login (size M)

- Rulings: PT-17, PT-20
- Change: one level saved-data store of credits owed to offline players, keyed by UUID, applied once at login through a
  small table of appliers. Two entries now. Structure craft: `recordStructure`, `MachineAccessEvents.awardMachineCrafted`
  and the Living Staff bond unlock when `PendingBloodCraftManager` resolves with the crafter offline. Loom climax: the
  Loom use records, the skill point and `ADV_MNEMONIST_FIRST_WEAVE_COMPLETE` when `tickCompletion` has no player. The
  Venous Stone needs no entry: the stone now waits for its infuser. Chunk 7 can add the Mycophant yield (PT-81) as a
  third entry.
- Files: `common/event/PendingBloodCraftManager.java`, `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`,
  `common/rite/harbinger/WorkingCraftRites.java` (check its caster lookup), one new class beside
  `common/capability/PendingDelivery.java`.
- Tests: a JVM test for the store's rule. Flip `weaverOfflineDuringClimax` to required. Add a structure-craft twin of
  `proofLostWhenOfflineAtCollapse`. `SomaticLoomWeavingGameTests` stays green.
- Docs: `docs/PLAYTHROUGH_TESTS.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md`.
- Depends on: T1. Chunk 7 for the Mycophant entry. This is not a migration, so it stays out of `PlayerDataMigrations`.

### T4. D0: the temple and founder reception (size M)

- Rulings: PT-8, PT-9
- Change: the Mortal Display cannot be broken in survival while it holds a living Hermit link, and a Display placed or
  loaded without one relinks to the nearest unlinked Hermit of its temple. The Hermit reads his Display's claimed
  state, so he stops offering a spent heart. `MortalDisplayBlock.handleInteraction` checks active blood before the
  blessing and says `hemomancy.initiation.cannot_attach` (or a clearer new key) to an awakened player. A Hermit whose
  heart this player did not take offers no farewell. That last line is my reading of "relink", see NQ-10-12. Founder
  reception: `EarlyInitiation.tryProject` starts the ceremony only after the recruit consents. I plan the form in
  NQ-10-9 option A: the recruit is told who offers and must kneel (sneak) to accept. No new payload.
- Files: `common/block/harbinger/functional/MortalDisplayBlock.java`,
  `common/tile/harbinger/functional/MortalDisplayBlockEntity.java`, `common/rite/TempleOathRules.java`,
  `common/entity/npc/harbinger/HarbingerHermitEntity.java`, `common/mission/vicar/EarlyInitiation.java`, lang.
- Tests: flip `twoPlayersOneTemple`, `brokenMortalDisplayLeavesNoOath`, `wrongHermitClaimsAndSilentFarewell`,
  `awakenedPlayerAtUnclaimedDisplay`, `founderProjectionReceivesRecruit`. `deathBetweenBlessingAndTouch` stays a log.
- Docs: `docs/HEMOMANCY_REFERENCE.md` onboarding section, `wiki/Getting-Started.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: NQ-10-9. Chunk 11 (Bloodline) for founder reception, which joins the recruit to the founder's line.
  Hermit and system lines follow `docs/NPC_VOICES.md`. Chunk 16 runs the voice pass.

### T5. D1: the absorption proof and the Hermit Road (size M)

- Rulings: PT-1, PT-1b, PT-2, PT-15
- Change: `BlockBloodInteractions.tryAbsorbFromLookedAtBlock` does not call `recordAbsorption` for blood the player
  projected. "Own tank" is not enough, because an owned jar or vat can hold creature blood. `BlockBloodVolume` needs a
  tally of mL projected by its owner. A draw uses that tally first and that part is not counted. Reword the Vicar:
  `hemomancy.vicar.blood_crafting.absorption` drops "counts everything" and says only blood that finds room in the
  vessel counts. `hemomancy.vicar.neophyte.absorption_hint` leads with the reservoir the owner picks in NQ-10-1 and
  says the player's own poured blood does not count. Hermit Road: the report option and its three nodes move out of
  the D1 tree into a decorator applied to every Vicar tree, as `SpecimenJarLessonDialogue.append` does.
- Files: `common/block/harbinger/BlockBloodInteractions.java`, `common/tile/BlockBloodVolume.java`,
  `common/tile/IBloodReservoir.java` (`takeProjectedBlood`), `common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`,
  lang.
- Tests: flip `condenserRoundTripCountsAsAbsorption` and `hermitRoadAfterD2IsDeadEnd`. Rewrite
  `fullVesselAbsorptionCounts` to assert net counting and the new hint text. `reservoirAbsorptionReachesFiveHundred`
  uses an arranged reservoir and must keep passing. `BlockBloodPersistenceGameTests` for the new tally.
- Docs: `docs/HEMOMANCY_REFERENCE.md` First Bloodcraft section, `docs/CORE_LOOP.md` if the tally is described there,
  `wiki/Getting-Started.md`.
- Depends on: NQ-10-1 before the hint is written. Vicar lines follow `docs/NPC_VOICES.md` (chunk 16).

### T6. D2: settling, the second spin, the outpost Alembic, the jar lesson (size M)

- Rulings: PT-11, PT-18, PT-12, PT-33
- Change: `ConcentratedBlood` settles when the player has lain in bed for 100 ticks, which is vanilla's own
  "slept long enough" mark. The value is mine. The Chamber dream keeps its night-skip rule. `attemptStartup` in the
  Centrifuge refuses an assigned spin while an earlier assigned output is unrecovered, with a line. The way out for a
  lost output is NQ-10-6. `outpost.py` sets a magma block in the floor under the Alembic (my pick: it keeps the floor
  solid and makes no smoke), then the outpost templates are regenerated with `tools/worldgen/harbinger_sites.py`. The
  Specimen Jar lesson option is added to the D3+ Alchemist trees.
- Files: `common/mission/alchemist/ConcentratedBlood.java`, `common/event/ChamberVisitEvents.java`,
  `common/mission/alchemist/FirstSeparationAssignment.java`,
  `common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java`, `tools/worldgen/harbinger_sites/outpost.py`,
  `data/hemomancy/structure/harbinger_outpost/alchemist.nbt`,
  `common/entity/npc/dialogue/SpecimenJarLessonDialogue.java`, lang.
- Tests: flip `sleepNeedsTheWholeServer` and `secondSpinOverwritesTheProof`. Rewrite `outpostAlembicHasNoHeat` to read
  the real template. `HarbingerOutpostAssemblyGameTests`, `StationMenuGuardrailTest`. Add a D3 case to the jar lesson
  sweep in `PlaythroughNpcUsabilityGameTests.unofferedEventsRefusedSweep`.
- Docs: `docs/HEMOMANCY_REFERENCE.md` D2 sections, `docs/STATION_UPGRADES.md` only if the Centrifuge text changes.
- Depends on: NQ-10-6. PT-26 shares `completeSleep` and waits on NQ-10-2.

### T7. D3: optional index, Vicar hints, guided Chamber (size M)

- Rulings: PT-6, PT-7, PT-24
- Change: the ledger's Woven Vessel text and one Mnemonist line say the index is optional Side work. The Vicar's D3
  hint names the Loom weave and the D4 hint names the Vein-Mason loadout. `ChamberVisitService.returnFromVisit` takes a
  reason. Only the timeout and the paired chair exit set `guidedComplete`. Logout and death protection return the
  player without completing. The AGENTS.md Chamber rules stand: no attunement through early practice, originals
  restored after the transfer.
- Files: lang (`screen.hemomancy.harbinger_assignment_ledger.woven_vessel.desc`,
  `hemomancy.mnemonist.woven_vessel.archive.line1`, `hemomancy.vicar.initiate.degree_hint`,
  `hemomancy.vicar.adept.degree_hint`), `common/encounter/ChamberVisitService.java`, `common/event/ChamberVisitEvents.java`.
- Tests: rewrite `weaveWithoutMnemonistUnlocksD4` to assert the weave opens the Brotherhood and the text says
  optional. Flip `vicarD3HintNamesTheRealGate` and `degreeHintMatchesTheChapterGate`.
  `guidedChamberLogoutAndLethalDamage` cannot run without the Chamber dimension. `docs/PLAYTHROUGH_TESTS.md` names a
  `chamberReviewClient`, which I did not find in `gradle/review-clients.gradle`. Check `ChamberGatewayGameTests` and the
  `chamberLoadedValidation` property in `build.gradle` for a launcher that loads it.
- Docs: `docs/HEMOMANCY_REFERENCE.md`, `docs/NPC_VOICES.md` pins if a pinned line changes, `wiki/Harbinger-Path.md`.
- Depends on: chunk 16 for the Vicar and Mnemonist voice pass.

### T8. D4 and D5: the Vein-Mason chapter (size M)

- Rulings: PT-4, PT-14, PT-22, PT-28, PT-29, PT-30
- Change: the Vicar reissues the Mason's Respite map when the directive was given and none is carried. The scar id of
  each issued lesson (first, continuation, D5, D6) is stored when it is given, and replace reads the stored id. An
  existing Varicose section grants `D5_VARICOSE` once, at the next Thelemic cast or when the Mason's D5 tree opens.
  With `vascularDegradationOnManipEnabled` off, the first Thelemic Memory cast after the D4 reward counts as the strain
  (my design, NQ-10-11), and the Mason's `hemomancy.anchorite.d5.cause_strain` line matches. Every silent `return` in
  the D0 to D6 route classes says a `message.hemomancy.*` refusal line. New trade
  `npc_trade/cicatrix_anchorite/scar_blank.json`: one blank, D4 and active blood. The price is not ruled. I suggest 8
  bone and 4 clay, stock 1, restock 7 days, as the Syringe reissue does.
- Files: `common/entity/npc/dialogue/route/VicarDialogueEvents.java`, `VeinMasonDialogueEvents.java`,
  `common/entity/npc/dialogue/VeinMasonScarLesson.java`, `common/mission/cicatrix_anchorite/VeinMasonAssignments.java`,
  `common/capability/player/harbinger/thelemicmemory/ThelemicMemoryActivationService.java`,
  `data/hemomancy/npc_trade/cicatrix_anchorite/`, lang.
- Tests: flip `lostMapIsNotReissued`, `temperDriftDuplicatesPatterns`, `preStrainedSectionNeverCounts`,
  `d5StrainWithDegradationOff`, `lostBlankBeforeCarve` (it must use the trade path). `VeinMasonAssignmentsTest`,
  `NpcTradeCatalogueDataTest`, `NpcTradeLangKeysTest`, `NpcTradeGuardrailTest`, `npc_trade_validation`.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/HEMOMANCY_REFERENCE.md` Vein-Mason sections, `wiki/Configuration.md`.
- Depends on: T2 (the lesson kit goes through the owed list). Anchorite lines follow `docs/NPC_VOICES.md` (chunk 16).

### T9. D5 and D6: the Monolith, Quintessence, quest cards, commissions (size M)

- Rulings: PT-5, PT-27, PT-43, D-23, PT-39, PT-71, PT-86, PT-41
- Change: `sanguine_monolith.json` `required_degree` 6 becomes 5. The `monolith_rumor` node and the scrap option are
  added to the D6, D7 and D8 Vicar trees, and the handler's guard becomes `ProgressionGates.hasDegree(player, 5)` in
  the same change, as the probe note warns. A per-player flag records the scrap. Whether a lost one is reissued is
  NQ-10-7. `hemomancy.monolith.archon.press_again` gains a line, shown only while the free Cornerstone is unclaimed,
  that the stone cannot give it once broken. The Vicar gets a line naming the Rite of Exsanguination as the source of
  Sanguine Quintessence, in his D5 and D6 trees. The Mnemonist's `anchorite_counsel` option becomes
  `DialogueOptionPresentation.attention(NOTICE).withQuest()` until `VeinMasonAssignments.onCounsel` records it. The
  D5 and D6 covenant steps from `CovenantProgress.steps` each become a Quests card with a NOTICE sigil while open, and
  `hemomancy.vicar.covenant.complete_vigil` names the other helpers. `PhlegethonticCommission.progress` reads both
  hands. The commission reward waits on NQ-10-8.
- Files: `data/hemomancy/recipe/cardinal_rite/working/sanguine_monolith.json`,
  `common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`, `route/VicarDialogueEvents.java`,
  `SanguineMonolithDialogueTrees.java`, `HarbingerMnemonistDialogueTrees.java`, `DialogueHubFactory.java`,
  `common/mission/alchemist/PhlegethonticCommission.java`, `DeepDarkCommission.java`, lang.
- Tests: flip `d5MonolithCannotSpeak`, `monolithScrapIsRepeatable`, `monolithBlueprintOnlyAtD5`,
  `quintessenceHasNoPointer`, `counselIsALoreCard`, `mainStepsAreQuests`, `alchemistBasinSampleInMainHand`. Add the
  Cornerstone warning to `shatterBeforeCornerstone`. `WorkingCraftRosterDataTest`, `CardinalRitePurposeDataTest`,
  `dialogue_route_validation`.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (D-23 moves to "Rulings that bind"), `docs/CARDINAL_RITES.md`,
  `docs/HEMOMANCY_REFERENCE.md` sections 5.3 and 25.1 and the Working craft table, `docs/ENDGAME_PATHS.md`,
  `docs/OPEN_QUESTIONS.md`.
- Depends on: T2 for the scrap delivery. Chunk 12 owns the D7 shatter and PT-74. Chunk 13 owns the Working craft
  roster. Chunk 11 owns the Living Covenant steps the new cards show. Vicar, Mnemonist and Monolith lines follow
  `docs/NPC_VOICES.md` (chunk 16).

### T10. One vacancy rule for unique Harbinger NPCs (size L)

- Rulings: PT-10, PT-23, PT-85
- Change: lift the rule in `CourtVacancies` into one shared store that both the Court and the Harbinger sites use. A
  resident records its home cell, facing and site key when it spawns. A resident lost to `/kill` or the void leaves a
  vacancy that is filled after 24,000 ticks while no player is within 48 blocks (the Court's numbers). Whether a
  Creative kill also counts is NQ-10-4. `HarbingerOutpostStructure.spawnMob` and `MasonsRespiteStructure.spawnMob` try
  the marker, then the nearest clear standable cell within 3 blocks, and log a warning if none. Every
  `ProfessionalHarbingerEntity` stores `outpostHome`, as the Vicar does today. A summoned recruit is sent back to it
  when its use ends. What ends it is NQ-10-5.
- Files: `common/entity/npc/court/CourtVacancies.java`, `common/worldgen/structure/HarbingerOutpostStructure.java`,
  `common/worldgen/structure/MasonsRespiteStructure.java`, `common/entity/npc/harbinger/HarbingerVicarEntity.java` and
  the other resident entities, `common/entity/npc/harbinger/HarbingerCicatrixAnchoriteEntity.java`,
  `common/network/capa/harbinger/PacketLedgerAction.java`, `common/entity/npc/dialogue/HarbingerRecruitmentRules.java`.
- Tests: flip `uniqueNpcLossIsRecoverable`, `uniqueNpcLossLate`, `outpostSpawnsAllResidents`, `summonStripsTheOutpost`.
  `CourtLifeGameTests` must stay green. The Court probe `courtLeadLostForGood` saw vacancies not refill after a skipped
  day. Find that cause before sharing the rule.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/BRINED_COURT.md`, `docs/HEMOMANCY_REFERENCE.md`,
  `docs/architecture/ARCHITECTURE_STANDARDS.md`.
- Depends on: NQ-10-4 and NQ-10-5. Chunk 14 (Brined Court, where the rule lives today) and chunk 15 (Circus, the lost
  Ringmaster and lost performers) can use the same store. Chunk 11 (Bloodline) owns the ledger and its summon. Name the owner of the
  shared class before two chunks build it twice.

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-10-1 | PT-1, PT-1b | With your own projected blood excluded, which reservoir does the Vicar point a D1 player at? CL-8 lets a player draw only from their own tanks and from unowned, structure-placed ones. I could not confirm any outpost or temple block spawns holding blood. | A: a filled, unowned blood font at the outpost and the temple that counts. B: no reservoir route at D1, so raise the living rate or lower the proof. C: let own blood count once it has sat in a tank with a creature (jar or vat). | A, because it keeps both rulings and teaches Absorption at a place the player already visits. |
| NQ-10-2 | PT-26 | Your answer repeats the question. What should happen when the charm is off while Concentrated Blood is pending? | A: a line on waking that the blood cannot settle without the charm. B: the same line from the Alchemist as well. C: settle without the charm. D: keep silent. | A, the sheet's recommendation. It is one system line in `ConcentratedBlood.completeSleep`. |
| NQ-10-3 | PT-31 | The Thurible as an Apotheos relic: which Degree, what does it do for an Apotheos, how is it obtained, and does anything replace it at D4? Today it is a D4 lesser Working craft (Spore Sac, 1,000 mL) that burns spores for Sporitic Resonance (15% cheaper, 10% faster matching casts) and an infection aura. The Mycelial skill branch uses it as its icon, and the Liber map, atlas and creative tab list it. | A: move it to D8 Apotheos as it is, gate use and craft on the path, nothing new at D4. B: move it and redesign it around the hive (spores as the Apotheos body's own). C: leave it at D4 until chunk 12 designs the relic. | C for now. Chunk 12 owns the Apotheos kit, and "probably" is not yet a design. Retire probe `d4ThuribleHasATeacher` when it moves. |
| NQ-10-4 | PT-10 | Your PT-10 answer is PT-11's text. I planned the sheet's "respawn rule for unique NPCs". Confirm, and say whether a Creative kill is final. | A: the Court rule as it stands (`/kill` and the void refill after one day with no player within 48 blocks, a Creative kill is on purpose and final). B: a Creative kill refills too. | A, one rule for every unique NPC. The probe then kills with `/kill`. |
| NQ-10-5 | PT-85 | What ends a summon, so the NPC goes home? And may two lines pledge the same outpost NPC? The probe fails on both. | Ends: A: when no member of the line is within 32 blocks for one minute. B: when the rite or Vigil it helped ends. C: a ledger "dismiss" action only. Pledge: keep shared, or one line per NPC. | A plus a dismiss action. One line per NPC, since a shared recruit can be summoned away from the other line. |
| NQ-10-6 | PT-18 | If the first assigned output is lost or destroyed, what lets the player spin again? Without a way out the D2 chapter locks. | A: the Alchemist clears the pending spin on request. B: the lock lasts only while that output is still in the Centrifuge. | A, one dialogue option and no tracking of where the item went. |
| NQ-10-7 | PT-27, PT-19 | "One per player" for the Monolith scrap, but PT-19 keeps reissue as a lost-item safety net. Which wins for the scrap? | A: strictly once. B: once, and again only when none is carried. | B. It matches PT-19 and the probe passes either way. |
| NQ-10-8 | PT-41 | What do the Alchemist's two commissions pay? | A: rapport with the Alchemist and a small enzyme bundle. B: a unique item per commission. C: an advancement only. | A, delivered through the T2 claim. |
| NQ-10-9 | PT-9 | What form does the recruit's consent take? | A: a line names the founder and the recruit kneels (sneaks) to accept. B: a confirm screen with a new payload. | A. No protocol bump, and it reads as part of the ceremony. |
| NQ-10-10 | PT-3 | `PendingDelivery` fits NPC rewards. For station outputs I planned an owner lock on the assigned Centrifuge output and the Effigy pattern going to its preparer. Is that what you meant? | A: yes. B: every station output becomes an owed stack for whoever paid. | A. B would change how every shared station works. |
| NQ-10-11 | PT-29 | What is the fallback strain route when degradation is off? | A: the first Thelemic Memory cast after the D4 reward counts. B: the Mason accepts the diagnosis alone. C: mark the config unsupported. | A, the smallest change that keeps the lesson. |
| NQ-10-12 | PT-8 | "Protect and relink" does not name the other temple's Hermit, who says "you have claimed my heart" and offers a farewell that does nothing. | A: hide the farewell unless this Hermit's heart was taken. B: keep it. | A. The link data makes it one condition. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| HK-3 | Run the suite on today's tree and compare with the 2026-11-11 list. | Build | T1 |
| PT-26 | Rule on the missing-charm case. | Owner | NQ-10-2 |
| PT-31 | Design the Thurible's move. Nothing changes at D4 until then. | Owner | NQ-10-3, chunk 12 |
| PT-1, PT-1b | Pick the D1 reservoir before the hint is rewritten. | Owner | NQ-10-1 |
| PT-10, PT-85 | Confirm the respawn rule and define the end of a summon. | Owner | NQ-10-4, NQ-10-5 |
| PT-18 | Pick the way out for a lost first spin. | Owner | NQ-10-6 |
| PT-27 | Strict once, or reissue when none is carried. | Owner | NQ-10-7 |
| PT-41 | Name the commission reward. | Owner | NQ-10-8 |
| PT-9 | Confirm the consent form. | Owner | NQ-10-9 |
| PT-11, PT-12, PT-30 | Review my values: 100 ticks in bed, a magma block under the Alembic, 8 bone and 4 clay for a blank. | Owner | T6, T8 built |
| PT-24 | Prove the guided Chamber change where the Chamber dimension loads. The named `chamberReviewClient` was not found. | Later | T7 built |
| PT-13, PT-19 | Record the two keep rulings in `docs/STATION_UPGRADES.md` and `docs/PLAYTHROUGH_TESTS.md`, and retire their probes. | Build | T1 |
| PT-7, PT-6, PT-39, D-23, PT-86 | Voice pass on the new and changed Vicar, Mnemonist, Anchorite and Monolith lines. | Later | chunk 16 |
| PT-25, PT-88, F-12, F-13 | Named by HK-3 but owned by other chunks. PT-25 and PT-88 still behave as before. F-12 and F-13 run in the main suite. | Later | chunks 4, 13 and 17 |
| PT-17 | Confirm in play that a real logout leaves the fed stone standing. The probe's captured player is never removed, so its pass does not prove it. | Later | T3 built |
