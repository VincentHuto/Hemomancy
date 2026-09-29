# Phase 8: documentation reconciliation and integrated acceptance

[Index](HARBINGER_PROGRESSION_REDESIGN.md) · Run focused checks during each phase, then integrate here.

## Documentation to reconcile

| Surface | Required cleanup |
|---|---|
| HEMOMANCY_REFERENCE.md | Correct degree/teacher tables, Main proofs, actual station gates, staff/syringe ownership vs lesson timing, gourds, kit owner/name, cylinder uses and Chamber modes. |
| LORE_REFERENCE.md | Preserve early mystery, D4 scar apprenticeship, D5 founding responsibility, D6 limits of ordinary teachers, and distinct ending responses. Remove obsolete mechanics presented as current lore. |
| LORE_CONSISTENCY_REVIEW.md | Add a dated adopted decision for distributed teaching. Explain that the Vicar retains core bloodcraft/doctrine and specialists own their practical lessons. Preserve resolved cosmology/ending decisions. |
| Harbinger-Path.md | Replace stale first-rank rites, seven-tendency claims, obsolete scar methods and unimplemented progression promises. Show Main vs optional branches and teaching vs access. |
| Getting-Started.md / Clinical-Blood-Tools.md | D1 collection, D2 processing, correct vial controls, syringe reward/instruction timing, Concentrated Blood sleep, ordinary wax. |
| Clairaudiograph.md / Antecedent-Inquiry.md | New ordinary medium, legacy ancient recordings, optional early inquiry vs D5 formal commission; no invented audio features. |
| Blood-Systems.md / Lore-and-Story.md | Accurate memory/station distinctions, eight tendencies, Chamber vs fungal contexts, degree vs NPC role. |
| Liber pages, dialogue/inquiries, Conduit/JEI/material atlas | Match the same requirements, names, costs, recovery routes and spoiler stages as the server. |
| Earlier Chamber/repair/design plans | Add clear dated supersession pointers where contradictory, preserving historical status/evidence. Do not mark old implementation claims newly verified. |

Only edit pages that exist and apply. MNA compatibility is outside this packet's scope. Do not perform blanket rewrites of unrelated references or erase historic design records.

## Source checks

Run narrow searches for the touched obsolete wording and references. Review matches in context; many legacy registry IDs must remain.

- Vicar's Consecration Kit: display text changes; vicars_consecration_kit ID and old claim compatibility remain.
- Seven tendencies / generic Fungal tendency: normal tendency count is eight.
- First Bloodcraft 5000 ml: current proof is 500 ml absorbed; distinguish vessel capacity and other unrelated costs.
- Concentrated Blood grants D2: target/current promotion is D2 -> D3.
- Somatic Loom or Reliquary first unlocked D4: actual gates remain D3.
- Anchorite first available D5: first scar chapter remains D4.
- Chamber first exists D6: distinguish early dreams/seat practice from D6 attunement.
- Fungal Gardens exclusively a dimension: preserve the explicit Overworld biome.
- Any sapling becomes bloodwood: target supports Dead Bush.
- Ordinary cylinders require ambergris: update all ordinary consumers, not ancient/specialty content.
- Saints, Canon acquisition or Drudges mandatory in alpha: keep deferred.

Check resource JSON, recipe IDs, translation keys, new item model/texture references, assignment serialization, packet field order, and every new Markdown link.

## Focused automated checks

Choose tests based on actual changed behavior. Favor existing fixtures and observable outcomes over new tests that merely search source text. Registry-backed interactions need a mod-loaded server.

The suite catalog is [gradle/game-tests.gradle](../../gradle/game-tests.gradle); [TESTING.md](../TESTING.md) explains launchers and run directories. Verify task availability again at execution time.

Typical compile/resource check:

    ./gradlew.bat compileJava compileGameTestJava processResources

Examples of existing focused commands:

    ./gradlew.bat test --tests '*FirstBloodcraftLedgerProgressTest' --tests '*FirstSeparationLedgerProgressTest' --tests '*FirstSeparationSpinProofTest' --tests '*HarbingerChapterMilestoneTest' --tests '*ClinicalBloodProgressTest'
    ./gradlew.bat runClinicalGameTestServer runBloodInjectionGameTestServer runDistillationGameTestServer
    ./gradlew.bat runScriptoriumGameTestServer runAdvancedBrewingGameTestServer
    ./gradlew.bat runAntecedentGameTestServer

Do not assume the combined suite includes every focused namespace. Current clinical, advanced-brewing, Scriptorium, Antecedent and several encounter suites are opt-in. For a new namespace, update DevTestHooks/catalog/template registration and run verifyGameTestSuites; do not create unregistered tests that never execute.

At integration, run the repository's required progression gate:

    ./gradlew.bat alphaCheck

Run additional focused suites that alphaCheck excludes when this work touches them. Record exact failures and whether the failing code changed. Never convert a partially green run into a full pass claim.

## Runtime acceptance matrix

| Scenario | Required evidence |
|---|---|
| Fresh Main-only player | D0 through D7 without optional catalogue completion, full armor sets, Circus, rare expeditions, Saints, or unavailable teachers. |
| Early scientist | D1 direct-vial samples survive and count at D2; restricted profiles still need syringe; centrifuge and personal extraction proofs work. |
| Out-of-order explorer | Early staff, crude memory, chair, reef/Deep Dark discovery, and station crafting are acknowledged without revoked access or duplicate rewards. |
| D3 memory route | Actual blank memory plus first weave produced before D4 cultivation or D5 incubation; material hints are sufficient without commands. |
| Routine cylinder user | Ordinary audio and basic/Precision Forge work require no ambergris; legacy ancient/master data survive. |
| Chamber learner | Explicit guided visit, direct chair entry, sleeping chair entry, timed return, and independent rite behave as documented. |
| Scar apprentice | D4 scar/effigy sequence is achievable and alone opens the D5 chapter gate. |
| Solo founder | Legitimate bloodline/Fane/recruit route supplies a valid Vigil helper without another human player. |
| Two players | Proof ownership, NPC event validation, distinct Chamber inventories/cells, founder membership, and rite ownership do not cross-credit. |
| Existing save | Old degree/claims, pending rest, completed weave/scars, Fane, Chamber attunement, loadouts, cylinders, and ending flags survive. |
| Recovery | Interrupt, die, log out, restart, fill inventory/output, and retry at each changed state boundary. |
| Endings | From legitimate pre-choice state, separately complete refusal and Apotheosis without cross-granting status/rewards. |

Use disposable test worlds/copies for recovery and endings. Do not delete or overwrite the user's runtime worlds. A fixture may accelerate reaching a late test state; label it as a fixture and separately record the survival acquisition path.

## Live flow review

- Read the first option and next-step text at every rank. The player should know what advances them, what is optional, where to go, and why.
- Check that degree unlocks do not present four equally urgent quest walls. Prefer the Main continuation, one contextual referral, and accessible optional branches.
- Verify cross-NPC referrals acknowledge completed work and shared observations.
- Check that recipe hints show actual catalysts, mediums, costs, floors, required staff, supported containers, and replacement routes.
- Time introductory lessons and trips. Record excessive material grind, repeated empty nights, duplicate travel, or text that reveals more than the player has earned.
- Inspect in-world items, ledger, dialogue, cylinder models and Chamber presentation. Automated source checks cannot approve these views.

## Phase handoff record

### 2026-09-28: D2-to-D3 operator journey

- Behavior changed: The developer journey follows Vicar Degree 2, First Separation, ordinary Ghastly Alembic extraction, Alchemist-supplied Concentrated Blood, and completed rest. Its centrifuge automation now moves the filled vial created by sampling instead of the old empty stack. The rest fixture supplies a two-part bed. The Incarnadine Fane rite remains ceremonial, not a Degree-3 promotion.
- Files changed: `HemoJourneyStage`, `HemoJourneyFixtures`, `HarbingerJourneyAutomation`, `HemoJourneyChecks`, `HemoJourneyController`, `HemoJourneySnapshot`, their GameTests/source contract, and `docs/HEMOMANCY_REFERENCE.md`. The Body Answers GameTest fixture now sets Degree 2 before asking for its briefing.
- Save/compatibility handling: Journey snapshots capture and restore the advanced-brewing attachment and durable player mission data, including pending Concentrated Blood rest. No production save schema changed.
- Tests run: `compileGameTestJava` and `GameTestHarnessSourceContractTest` passed. The latest `runGameTestServer` completed 581 tests; the D3 distillation/rest, snapshot, stage-order, First Separation automation, and Body Answers checks passed, but 17 other required tests failed, so the combined gate is not green. Intermediate runs crashed in unrelated Mortarbound assertions before completing.
- Live scenarios observed: None. No live client, natural survival route, multiplayer session, or existing-save migration was exercised.
- Remaining failures or unverified behavior: The combined suite still reports fixture occupancy and other failures. Full-inventory reward recovery, an actual player sleep cycle, and the Main-only D0-D7 journey remain unverified.
- Next phase: Continue early Main-route acceptance and repair affected journey fixture failures before claiming integration.

### 2026-09-28: integrated journey fixture repair

- Behavior changed: The Vicar report checkpoint accepts the First Bloodcraft Ledger already issued at D1 initiation and still requires a real report plus four newly awarded Befouling Ash Trail. The operator prompt tells the player to recover the ledger if missing. Production save schema and rank gates did not change.
- Files changed: `HemoJourneyChecks`, `HemoJourneyFixtures`, `HemoJourneyController`, `JourneyAutomationGameTests`, `HarbingerPilotGameTests`, `HarbingerJourneyFixtureGameTests`, and `PuppeteeringGameTests`; a clean 32x12x24 Hemomancy GameTest template and an optional `combinedGameTestDirectory` Gradle property were added. The default test directory is unchanged.
- Save/compatibility handling: The ledger remains an inventory item, not a new claim at report time. The canonical `SILENT_ARCHON` path is used in the claimed-Will GameTest; no migration code changed.
- Tests run and exact result: `compileGameTestJava` and `verifyGameTestSuites` passed. One combined `runGameTestServer` completed 581 tests; the Vicar report runner, Temple Heart claim, Sanguine initiation, formation baseline, Worn Vow, Weight of the Frame, and claimed-Will checks passed, while only `vagrantmindislocatableintheend` failed. A later run in a new directory also completed 581 tests and failed the locator plus `carouselspawnsthreeboundpuppeteercaptives` and `circussuccessionrunnercompletesatdegreefour`. Neither run makes the combined gate green.
- Live scenarios observed: None. This does not establish natural survival acquisition, client presentation, multiplayer ownership, or existing-save recovery.
- Remaining failures or unverified behavior: A fresh End chunk had a valid Vagrant Mind generation point but no structure start, so stale test-world data is not the sole cause. The variable Circus/Puppeteering failures also need diagnosis. Full Main-only D0-D7 and both ending routes still need live acceptance.
- Next phase: Diagnose the End locator separately; continue player-run progression and recovery checks without treating fixture success as survival proof.

### 2026-09-28: End structure GameTest boundary

- Behavior changed: No production worldgen behavior changed. Minecraft 1.21.1's `GameTestServer` uses `WorldOptions(0L, false, false)`, so its worlds never generate structure starts. The Vagrant Mind GameTest now checks the registered End biome and structure set, a biome-valid placement candidate, and a valid one-piece generated start directly. It no longer asserts that `/locate` works in a structure-disabled test world.
- Files changed: `CorticalDriftGameTests` and this handoff record.
- Save/compatibility handling: None.
- Tests run and exact result: `compileGameTestJava` and the combined `runGameTestServer` completed successfully; all 581 required GameTests passed. Earlier fresh-world runs confirmed direct generation produced one valid piece while natural chunk starts remained absent under the GameTest server's disabled-structures setting.
- Live scenarios observed: None. This test does not prove natural Vagrant Mind generation or `/locate` on a normal server.
- Remaining failures or unverified behavior: Natural End structure generation and the D6 locator still need a structure-enabled server or live-world check. Main-only survival, existing-save recovery, multiplayer, client presentation, and endings remain open.
- Next phase: Run the broader `alphaCheck`, then validate natural End generation separately in a structure-enabled world.

### 2026-09-28: Incarnadine Fane selector

- Behavior changed: The optional D2 Incarnadine Fane ceremony now takes one iron nugget instead of Hematic Iron Powder. It and the D3 Sanguine Brotherhood rank rite previously used the same floor, pillars, and offering; because the D2 ceremony remains available later, a D3 player could receive an ambiguous station result. The ceremonial rite is also removed from the rank-target lookup. Degree promotion still comes from First Separation, ordinary distillation, Concentrated Blood injection, and completed sleep.
- Files changed: `initiate_rite.json`, `RecipeDegreeGates`, `CardinalRiteProgressionResourceTest`, and `docs/HEMOMANCY_REFERENCE.md`.
- Save/compatibility handling: The recipe ID and rite completion reward remain unchanged. A previously built floor and pillars still work; the brazier offering must be replaced with an iron nugget.
- Tests run and exact result: The two focused rite resource classes passed 19/19. One earlier combined GameTest run passed 581/581 before this offering change. After the change, one server run crashed in a Mortarbound test and a retry completed 581 tests with one unrelated Puppeteering failure; neither is a green post-change resource gate. `alphaCheck` stopped at 19 JVM failures before this fix. A subsequent full JVM run completed 2,342 tests with 18 failures, so `alphaCheck` remains red.
- Live scenarios observed: None. A player has not performed either rite at D3 on a live server.
- Remaining failures or unverified behavior: The 18 JVM failures include source/resource expectations outside this selector. Natural structure location, full player progression, multiplayer, existing saves, and endings remain open.
- Next phase: Validate the two rite selectors in a mod-loaded test and work through the remaining progression-relevant JVM failures without changing unrelated user work.

### 2026-09-28: progression contract reconciliation

- Behavior changed: No further production behavior changed. The First Separation ledger keeps distillation inside one Main assignment, Lantern culture slots accept authored fruiting recipes without a new research gate, and Armature/Fane/outpost checks now point at their current owners.
- Files changed: `HarbingerPilotGameTests` gained a mod-loaded check of both rite offerings. Source checks for First Separation, Mnemonist recipe teaching, Founding Fane, Armature recovery and tier milestones, Artificer outpost placement, First Culture, and the Blood Stained Stone item route were updated to inspect the current implementations.
- Save/compatibility handling: None.
- Tests run and exact result: The rite resource tests passed 19/19. The new offering GameTest passed in two completed 582-test combined runs; both runs failed the same two Veinwing Vulture flight tests, so neither combined run was green. The latest full JVM run completed 2,342 tests with 10 remaining failures. `alphaCheck` therefore remains red. `verifyGameTestResourceLog` also failed to finish cleanly: one run crashed in Mortarbound, and another completed with a Puppeteering failure.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: The 10 JVM failures concern HUD assets/pixels, encounter and blood-profile contracts, waterlogging, a creative-tab source guard, and Morphling resource encoding. The two Veinwing GameTests need separate diagnosis. Live D0-D7, natural End location, multiplayer, existing saves, and endings remain open.
- Next phase: Continue the player-run and persistence acceptance work; diagnose the remaining broad-gate failures as separate issues.

For each implementation slice append:

    Phase and date:
    Behavior changed:
    Files changed:
    Save/compatibility handling:
    Tests run and exact result:
    Live scenarios observed:
    Remaining failures or unverified behavior:
    Next phase:

Update the index's implementation status table with this evidence. Keep plan status distinct from implemented status and from live acceptance.

## Completion rule

The cleanup is complete only when the implemented player journey, server gates, saved state, dialogue/ledger/recipes, references, and observed runtime journey agree. If code and automated tests are complete but live checks are unavailable, state that exact limit and leave the live acceptance items open.

## Verification of this documentation packet

This packet was prepared from a read-only gameplay-source audit of the current checkout on 2026-09-28, including its existing uncommitted progression changes. Ten new Markdown files were created. Three narrow .gitignore exceptions make this packet visible to Git despite the existing docs-wide ignore rule. No gameplay code, resources, existing lore/reference pages, or runtime worlds were changed for this request. Current-source checks established the promotion graph, relevant recipe gates, direct-vial path, syringe reward, first-weave ingredient route, Chamber modes, station tiers, kit claim path, and cylinder consumers.

Packet verification passed: 10 Markdown files, 45 local links, eight implementation phases, 22 current recipe gates, all six promotion certifications, and the chicken-profile prerequisite for Neurotic Enzyme. Markdown structure/whitespace checks and the scoped Git whitespace check passed. No Java build, GameTest, fresh-world survival journey, or multiplayer run was performed for this documentation-only request.

## 2026-09-28 Phase 7 dialogue slice

- Behavior changed: Four outpost NPCs acknowledge the canonical D7/D8 ending state without removing existing conversation options; undecided D7 lines do not assume a projection or choice, and generic Vicar and Alchemist greetings no longer imply all work is finished.
- Files changed: `DialogueHubFactory.java`, `en_us.json`, `ArchonEndingDialogueTest.java`, and the Phase 7 and index status notes.
- Save/compatibility handling: No new saved fields. Dialogue reads the existing `EnumArchonPath` capability.
- Tests run and exact result: The focused test failed to compile before the helper existed, then passed after implementation. The full `common.entity.npc.dialogue` JUnit package passed. Translation-key completeness and option preservation are covered. No GameTest or live-client run was performed for this slice.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Ending-choice presentation, actual Vesper and Apotheosis completion, saved-state reload, and live NPC service/recovery behavior remain open. The previously reported combined GameTest and `alphaCheck` failures are not resolved by this dialogue change.
- Next phase: Continue Phase 7 ending acceptance and Phase 8 integrated validation.

## 2026-09-28 Phase 7 Communion proof slice

- Behavior changed: Unbound Qliphoth Pomes retain food and effects but cannot count toward nine-fruit Communion, the HUD counter, or the Fungal Spine. Sequential owner-bound fruit still completes Communion.
- Files changed: `QliphothPomeItem.java`, `IInitiatoryDegree.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/index status notes.
- Save/compatibility handling: No new saved fields. Existing owned bound fruit and per-origin progress use the same data.
- Tests run and exact result: The new unbound-pome GameTest failed before the change. After the change, `runClinicalGameTestServer --offline --quiet` passed all 22 required tests, including negative unbound and positive nine-bound-fruit cases. An earlier `runGameTestServer` crashed in an unrelated `MortarboundGameTests` assertion before reaching this test. A `test --tests ...QliphothPomeRulesTest` attempt found no JUnit tests because that legacy class uses a standalone `main` method.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Bloom identity still keys several counters only by block position, despite dimension-bearing Bloom entries. Actual growth, picking, cross-dimension collision, reload, client presentation, and both full ending paths remain unverified.
- Next phase: Resolve Bloom identity and run end-to-end Phase 7/8 acceptance.

## 2026-09-28 Phase 7 Bloom lifecycle identity slice

- Behavior changed: Each Bloom has a saved lifecycle UUID. Fruit and its pending husk, dropped count, severed state, player Communion count, and Vesper attempt/phase now refer to that tree, so equal coordinates in two dimensions or a rebuilt tree cannot share progress. A Vesper loaded from an old attempt only seals a uniquely matching legacy Bloom.
- Files changed: `QliphothBloomSavedData.java`, `QliphothPomeItem.java`, `InitiatoryDegree.java` and its interface, `QliphothBloomBlock.java`, Vesper manager/entities and their callers, the clinical and repair GameTests, two identity JUnit tests, and Phase 7/index status notes.
- Save/compatibility handling: Old world Blooms receive UUIDs on load and retain a migration marker through later saves. Old position-keyed Bloom state and player progress move to that marked lifecycle; a fresh Bloom at the same site cannot claim them. Old picked fruit without a UUID uses the old position path when its original Bloom cannot be identified. Pre-ID state with two Blooms at identical coordinates in different dimensions was already ambiguous and can only be assigned to the first matching legacy entry.
- Tests run and exact result: The two identity JUnit classes passed after their initial missing-API compile failure. `compileJava compileGameTestJava --offline --quiet` passed before the final fixture adjustment. The final `runClinicalGameTestServer --offline --quiet` passed all 24 required GameTests, including replacement-tree fruit and Vesper phase/reload checks, and compiled production and GameTest sources. `git diff --check` returned zero with line-ending warnings. The combined `runGameTestServer --offline --quiet` attempt in a new `build/bloom-identity-gametest` directory crashed before completion on `MortarboundGameTests.patrolsWallWithoutShowingBody` line 30 (`Mortarbound did not patrol while embedded`).
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Old-save migration and picked-fruit reconciliation need live-world reload checks. Vesper reconnect, victory sealing, both complete endings, client presentation, multiplayer ownership, and the combined gate remain open.
- Next phase: Run full Phase 7/8 survival and saved-world acceptance, then address any integrated suite failures.

## 2026-09-28 Phase 7 Bloom placement and recovery slice

- Behavior changed: A Bloom rite now checks its root, seven filler spaces, and overlap with existing Blooms before starting. It repeats the same check before completion pays the medium. An invalid site does not report success or consume the seated Qliphoth Seed; clearing the site permits a retry.
- Files changed: `BloodCraftingKeyPressPacket.java`, `HarbingerCardinalRiteEvents.java`, `ClinicalBloodProgressionGameTests.java`, the shared journey fixture visibility, and Phase 7/index status notes.
- Save/compatibility handling: No new saved fields. A rejected start leaves no active rite; a rejected completion follows the established failure cleanup while preserving the Seed.
- Tests run and exact result: The new completion GameTest first failed because an obstructed site returned success, then passed after the pre-payment check. The start-path test first showed a blocked ceremony starting, and a separate root-block case showed the base could overwrite obsidian. After the shared preflight and base check, `runClinicalGameTestServer --offline --quiet` passed all 26 required GameTests, including blocked start, clear-site start, obstructed/overlapping completion, and successful retry. Two intermediate test attempts hit transient unrelated `ItemInit.java` edits while the checkout was changing; the final completed run compiled cleanly.
- Packet navigation: The relocated index and all nine phase pages now link to each other at their current paths. A local-link existence check found no missing targets. The absent `LORE_CONSISTENCY_REVIEW.md` was removed as a dead link, not reconstructed.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Full ceremony timing and interruption, natural fruiting, live feedback, existing-save recovery, multiplayer ownership, Vesper victory, and both endings still need acceptance. The last combined GameTest run crashed in `MortarboundGameTests` and was not rerun for this slice.
- Next phase: Continue the Main-only D7 journey and ending checks, then broader integrated and live validation.

## 2026-09-28 Phase 7 Communion journey fixture slice

- Behavior changed: The developer journey's D7 Communion fixture creates a registered Bloom lifecycle, binds its nine supplied pomes to that UUID, and removes the Bloom and fillers on cleanup. Automation and checkpoint verification require the fixture Bloom's identity and nine-fruit count.
- Files changed: `QliphothPomeItem.java`, `HemoJourneyFixtures.java`, `HemoJourneyChecks.java`, `HarbingerJourneyAutomation.java`, `HarbingerJourneyFixtureGameTests.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: The existing coordinate-only pome helper remains for old fruit; the new journey rejects it as proof of a fresh Bloom. No saved-data format changed.
- Tests run and exact result: The focused new test first failed because the old fixture had no Bloom. After implementation and a clear-site test coordinate correction, `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 27 required GameTests. One intervening Gradle compile retry hit a generated-class file lock; a full `compileJava --offline --rerun-tasks --no-daemon` passed before the final server run. The combined GameTest suite was not rerun.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Natural Bloom ripening and picking, the full Communion journey run, both ending ceremonies, reload, multiplayer ownership, and live client presentation remain open.
- Next phase: Run the Phase 7/8 journey and live ending acceptance.

## 2026-09-28 Phase 7 owner-picking validation slice

- Behavior changed: No production behavior changed. A server test now interacts with a real Bloom block for all nine ripe husks, rejects a stranger's claim and duplicate claims, then eats each picked fruit and checks pending-husk release, Communion, and the Fungal Spine.
- Files changed: `ClinicalBloodProgressionGameTests.java`, Phase 7 notes, and this handoff.
- Save/compatibility handling: No schema changes.
- Tests run and exact result: `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 28 required GameTests. The combined `runGameTestServer --offline --quiet --no-daemon` failed after two unrelated test failures and a fatal Mortarbound distant-sound assertion; it stopped before the Apotheos journey test.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: The test stages each ripe husk in saved state. Random world-tick ripening, full survival acquisition, the complete Main-only journey, both ending paths, save/reload, multiplayer, and client presentation remain open.
- Next phase: Exercise the remaining Phase 7/8 gates in a focused mod-loaded run and complete live acceptance.

## 2026-09-28 Phase 7 projection and Apotheos journey slice

- Behavior changed: Apotheos journey preparation no longer sets the revelation state. It selects the Communion-earned Fungal Spine and supplies travel blood; manual play waits for the normal projection, while automation uses the item and accelerates its forced return before choosing Apotheos. The checkpoint requires witnessed revelation and an ended projection. Snapshot ownership now includes projection and return fields, accepts their numeric NBT types, and restores the exact recipe book after degree synchronization awards recipes.
- Files changed: `HemoJourneyFixtures.java`, `HarbingerJourneyAutomation.java`, `HemoJourneyChecks.java`, `HemoJourneyController.java`, `HemoJourneySnapshot.java`, `HarbingerJourneyFixtureGameTests.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: No production save schema changed. The developer journey now preserves preexisting projection timer, return point, and rotation tags when resetting/restoring a player.
- Tests run and exact result: The focused Apotheos wrapper initially did not register because it named a filtered `minecraft` template namespace, then failed on a hard-coded fixture site; the clinical template and clear-site finder resolved both. A new test failed when fixture preparation skipped projection, then passed after the real Spine use path. Numeric projection snapshot restore first failed schema validation; after that fix, a Degree-7 restore still failed because degree sync awarded recipes after recipe-book restoration. Moving exact recipe restoration after sync passed. A separate test exercised the operator automation's Spine use, forced return, and choice. `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 31 required GameTests.
- Live scenarios observed: None. The focused server accelerates projection return and does not establish a rendered Gardens visit.
- Remaining failures or unverified behavior: Normal two-minute projection timing, actual cross-dimension travel and forced return, full Rite of Apotheos waves, the Silent Archon/Vesper branch, natural fruiting, multiplayer, reload, and live client presentation remain open. The combined suite still stops at the unrelated Mortarbound assertion before its Apotheos test.
- Next phase: Validate ordinary projection and Silent Archon/Vesper in a focused server and live client, then run the integrated Phase 8 gate.

## 2026-09-28 Phase 7 Sickle lifecycle slice

- Behavior changed: Newly shaped temporary Living Sickles carry the Bloom UUID. A stale Sickle cannot sever a new Bloom at the same position; the owner can shape a fresh one and proceed. A pre-ID Sickle is accepted only by a Bloom carrying the legacy migration marker.
- Files changed: `LivingSicklePruning.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: Existing no-ID Sickles remain valid against migrated old Blooms. A no-ID Sickle aimed at a newer Bloom cannot be proven to belong to that lifecycle and must be reshaped from the retained Living Weapon.
- Tests run and exact result: The replacement-tree GameTest failed before the binding change because the old Sickle severed the new tree. After the fix and a legacy compatibility test, `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 33 required GameTests. Focused `LivingSickleFeatureSourceTest` and `QliphothBloomIdentityTest` passed 10/10 JVM tests. The combined suite was not rerun for this slice; its earlier Mortarbound crash remains the known integration interruption.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Full owner/stranger cutting interaction, Chamber arena entry, both Vesper phases, retry/reconnect, victory rewards, persistent sealed-tree display, multiplayer, and live rendering remain open.
- Next phase: Exercise refusal entry and recovery in a focused mod-loaded test, then run the live ending paths and integrated gate.

## 2026-09-28 Phase 7 refusal boundary and ripening tests

- Behavior changed: No production behavior changed. Focused server tests now drive the Bloom block's owner/stranger Sickle and open-portal interactions, plus the real Bloom level-tick handler for a deterministic ripe roll, pending-fruit pause, and next husk.
- Files changed: `ClinicalBloodProgressionGameTests.java` and Phase 7/8 notes.
- Save/compatibility handling: No schema changes.
- Tests run and exact result: `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 35 required GameTests. The entrance test confirms a missing Chamber dimension does not close the portal, start an active attempt, or promote Silent Archon. The tick test prepares a deterministic roll at the 40-tick cadence; it does not use natural elapsed time.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Actual Chamber entry, Vesper's two phases and victory, retry after death/reconnect, live natural growth timing, save/reload, multiplayer, client presentation, and the integrated gate remain open.
- Next phase: Exercise the Chamber/Vesper runtime in a live disposable world and finish Phase 8 acceptance.

## 2026-09-28 Phase 7 journey fruit acquisition

- Behavior changed: The developer Communion fixture no longer supplies nine ready-made pomes. Manual play waits for and picks fruit from its registered Bloom. Automation accelerates each ripe state but claims the pome through the real block interaction and eats it in husk order. The earlier supplied-fruit fixture note above is superseded.
- Files changed: `HemoJourneyFixtures.java`, `HarbingerJourneyAutomation.java`, `HemoJourneyController.java`, `HemoJourneyChecks.java`, `HarbingerJourneyFixtureGameTests.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: No schema change. Automation can continue when the current pending fruit has already been picked; it rejects a missing or wrong-husk fruit rather than manufacturing another one.
- Tests run and exact result: The lifecycle test failed first because the fixture still supplied fruit. After implementation, an Apotheos test caught the test harness overwriting the Fungal Spine awarded by the ninth pome; that overwrite was removed, and the owner-picking test now checks the Spine in inventory. The final `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 35 required GameTests, including a previously picked pending pome and the Apotheos continuation.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Natural elapsed-time growth, reload between picking and eating, full survival Communion, Gardens projection, Vesper combat, multiplayer, rendered feedback, and the integrated gate remain open.
- Next phase: Run the D7/D8 route in a live disposable world and complete Phase 8 acceptance.

## 2026-09-28 Phase 7 stale Vesper attempt recovery

- Behavior changed: Reconnect and arena ticks no longer respawn Vesper for an attempt whose original owned Bloom is missing, replaced, sealed, or no longer paired with a Degree-7 pending refusal. Invalid attempts clear their fight state; Chamber players return through the existing return-point service. The new tree and ending path remain untouched.
- Files changed: `VesperOrdealManager.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: No schema change. A pre-ID attempt may resume only against one uniquely matched legacy-migration Bloom, consistent with the existing victory lookup.
- Tests run and exact result: The new GameTest initially failed compilation because the active-Bloom validation was absent. After implementation, the focused clinical suite passed 36/36 required GameTests, including same-coordinate replacement, sealed-tree and changed-choice rejection, and stale-attempt cleanup. `VesperOrdealRecoveryRulesTest` passed 4/4 JVM tests, and the scoped `git diff --check` found no whitespace errors. Actual Chamber login transport and fight resumption remain untested in this server.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Chamber entry/return, Vesper's two phases, death and reconnect in a live world, victory rewards, saved-state restart, multiplayer, and the integrated gate remain open.
- Next phase: Exercise both ending paths in a live disposable world and finish Phase 8 acceptance.

## 2026-09-28 Phase 7 Vesper scene sync

- Behavior changed: Chamber sync packets use the separate Vesper arena center while an ordeal is active, even when the player selected the Vesper floor preview theme. Without an active ordeal, the preview remains centered on the ordinary Chamber cell and returns there after abandonment.
- Files changed: `ChamberOfWillManager.java`, `VesperOrdealManager.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: No saved fields or packet format changed; this corrects which existing center is sent.
- Tests run and exact result: The new packet-level GameTest failed first because active sync sent the preview-cell center. After the priority change and a preview-return assertion, `runClinicalGameTestServer --offline --quiet --no-daemon` passed 37/37 required GameTests. `ChamberThemeCommandSourceTest` and `PacketSyncVesperFightSceneTest` passed 4/4 JVM tests; the scoped `git diff --check` found no whitespace errors.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Client floor rendering, real Chamber reconnect, Vesper combat/victory, both full endings, multiplayer, and the integrated gate remain open.
- Next phase: Inspect the active fight scene in a live disposable world and continue Phase 8 acceptance.

## 2026-09-29 Phase 8 Silent journey branch

- Behavior changed: The developer journey accepts either post-projection revelation response. The Silent route keeps the Communion Bloom and its lifecycle ID through a manual refusal stage; the Apotheos route still leads to its rite. The Silent checkpoint requires the original owned Bloom sealed after Vesper victory, the canonical D7 Silent Archon path, and the Vesper advancement. Automation does not fake the fight.
- Files changed: `HemoJourneyStage.java`, `HemoJourneyTransition.java`, `HemoJourneyController.java`, `HemoJourneyFixtures.java`, `HemoJourneyChecks.java`, `HarbingerJourneyAutomation.java`, `HemoJourneySnapshot.java`, `ClinicalBloodProgressionGameTests.java`, `HarbingerJourneyFixtureGameTests.java`, `GameTestHarnessSourceContractTest.java`, and Phase 7/8 notes.
- Save/compatibility handling: The existing `apotheos_choice` stage ID remains valid for in-progress developer journeys. The developer snapshot now restores the Vesper advancement and pending-memory flag. No production save schema changed. A full inventory that blocks the Living Staff handoff leaves the verified choice and original Bloom available for retry.
- Tests run and exact result: The focused Communion-to-choice test first failed when choice preparation deleted the Bloom. The Silent test then failed at the Apotheos-only check, the missing branch stage, and the missing snapshot fields before each corresponding fix. `runClinicalGameTestServer --offline --quiet --no-daemon` passed 38/38 required GameTests, including Apotheos continuation and the full-inventory retry. `GameTestHarnessSourceContractTest` initially failed two stale assertions for the new stage and removed revelation shortcut; after updating those expectations it passed 10/10. The scoped `git diff --check` found no whitespace errors. The combined GameTest suite was not rerun; its earlier Mortarbound crash remains the known integration interruption.
- Live scenarios observed: None. The test uses the real Spine return and dialogue choice, but sets a sealed Bloom, Silent Archon path, advancement, and pending memory directly to test the final checkpoint and restore contract. It does not defeat Vesper.
- Remaining failures or unverified behavior: Real Chamber entry and return, both Vesper phases, Blood Absorption finish, death/reconnect, persisted victory/reload, full survival endings, multiplayer ownership, and rendered client feedback remain open.
- Next phase: Run the manual refusal path in a disposable live world, including retry and reload, then complete the integrated Phase 8 gate.

## 2026-09-29 Phase 7 Vesper memory recovery

- Behavior changed: A full inventory no longer converts the unique Memory of Vesper into a world drop and clears its claim. The pending flag remains until inventory insertion succeeds. Login and a 20-tick server retry deliver it when space opens; victory tells the player to make room if necessary.
- Files changed: `VesperOrdealManager.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Save/compatibility handling: The existing pending-memory flag remains the source of truth; no new saved field or item ID. Old dropped memories are not reclaimed retroactively.
- Tests run and exact result: The new GameTest first failed compilation because no retry hook existed. After the production change, `runClinicalGameTestServer --offline --quiet --no-daemon` passed 39/39 required GameTests. The test checks full-inventory retention without a dropped memory, delivery after a slot opens, and no duplicate on another retry. The combined GameTest suite was not rerun.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Real Vesper victory and return, save/restart with a pending memory, client message and item presentation, multiplayer ownership, and both full endings still need acceptance.
- Next phase: Validate the complete Silent ending in a live disposable world and run the integrated Phase 8 gate.

## 2026-09-29 Phase 8 integrated gate audit

- Behavior changed: The Vesper flight and Archon-path source contracts now check lifecycle-bound Bloom matching and entry-based severing, not the removed coordinate-only expressions. The operator journey order test includes the Silent branch, and the duplicate-pome test supplies the required Degree-7 player. Mortarbound GameTests now use GameTest assertions, so a failure is reported without crashing the entire server.
- Files changed: `VesperWingedFlightPersistenceSourceTest.java`, `ArchonPathRiteSourceTest.java`, `HarbingerJourneyFixtureGameTests.java`, `HarbingerRepairGameTests.java`, `MortarboundGameTests.java`, and Phase 8 notes. No production behavior changed in this audit.
- Tests run and exact result: `alphaCheck --offline --quiet --no-daemon` stopped at its JVM task with 2,350 tests and 20 failures, including the two stale endgame contracts. After those were corrected, the Vesper source selection and direct Archon-path main test passed; a fresh full `test` run completed 2,350 tests with 18 failures. The first fresh combined server crashed at a raw Mortarbound assertion. After replacing raw assertions with GameTest assertions, the next combined run completed 582 tests with four failures, including stale journey-stage and D0 pome fixtures. The final `runGameTestServer -PcombinedGameTestDirectory=build/harbinger-redesign-integrated-20260929-third --offline --quiet --no-daemon` completed all 582 GameTests with five failures in Veinwing, Carousel, and Circus cases; it reported no Harbinger journey or pome failure. `alphaCheck` is still red because both of its required tasks have failures.
- Live scenarios observed: None. These are test-harness and integrated-server results, not a real survival ending or client review.
- Remaining failures or unverified behavior: One of the 18 JVM failures is D1-relevant: `BloodProfileMigrationTest` disagrees with the shipped syringe-restriction list (Chitinite is currently loose-vial eligible and newer profiles are restricted). Do not change that approved baseline or gameplay restriction by guesswork. The other failures cover resources, HUD pixels, combat/registry source contracts, and legacy tests. The combined server's five failures vary by run and need separate diagnosis. Full Main-only survival, both actual ending paths, saved-world restart, multiplayer ownership, and visual acceptance remain open.
- Next phase: Continue the Phase 8 live-path checks in a disposable world and address any progression failure they expose. Keep the unrelated gate failures visible; do not describe the integrated suite as passing.

## 2026-09-29 Phase 7 Fungal Spine recovery

- Behavior changed: Completing nine owned pomes with a full inventory still records Communion, but no longer marks the unique Fungal Spine as granted or drops it into the world. The existing degree flags keep the claim pending. A server tick retry inserts the item once a slot opens, then marks the grant complete; the player is told to make room.
- Files changed: `QliphothPomeItem.java`, `QliphothBloomEvents.java`, `ClinicalBloodProgressionGameTests.java`, `QliphothPomeSpineGrantSourceTest.java`, and Phase 7/8 notes.
- Save/compatibility handling: No new saved field or item ID. `qliphothCommunionDone=true` with `fungalSpineGranted=false` is a recoverable pending claim, including older saves that retained that state. Post-grant loss was outside this initial delivery change; the later Bloom recovery entry covers it.
- Tests run and exact result: The new full-inventory GameTest first failed because the grant flag was set and a Spine was dropped. After changing delivery, the final `runClinicalGameTestServer --offline --quiet --no-daemon` passed 40/40 required GameTests. The test checks no drop, degree NBT round trip, delivery after a slot opens, and no duplicate on a second retry. The legacy Spine source contract failed on the moved grant call, then passed after its assertion was updated; the pure `QliphothPomeRulesTest` main passed with main and test classes on the Java classpath. The scoped `git diff --check` reported no whitespace errors.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: Actual saved-world restart, live item/message presentation, loss after an already completed grant, the real Gardens projection, both full endings, and the integrated gate remain open.
- Next phase: Validate the D7-to-ending path in a disposable live world, including a full-inventory ninth pome and reload before the Spine arrives.

## 2026-09-29 Phase 7 Fungal Spine login compatibility

- Behavior changed: Login migration no longer marks Communion's Fungal Spine as granted without an item. The pending server-tick delivery survives a full-inventory login. If an older save already has a Spine in inventory but not the grant flag, delivery records the existing item instead of creating another.
- Files changed: `InitiatoryDegreeEvents.java`, `QliphothBloomEvents.java`, `ClinicalBloodProgressionGameTests.java`, and Phase 7/8 notes.
- Tests run and exact result: The full-inventory login assertion failed before the migration fix. The existing-item legacy test then failed before the inventory check. After both changes, `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 41 required GameTests. Gradle `test --tests ...QliphothPomeSpineGrantSourceTest` found no tests because this contract is a `main` class; direct `java -cp 'build/classes/java/test;build/classes/java/main' ...QliphothPomeSpineGrantSourceTest` passed. The scoped `git diff --check` found no whitespace errors.
- Live scenarios observed: None. The test invokes the login handler and saved-state serialization in a mod-loaded GameTest; it is not an actual server restart.
- Remaining failures or unverified behavior: Actual saved-world restart, a Spine kept outside player inventory on an older save, post-grant loss recovery in live play, live Gardens projection, both endings, and the integrated gate remain open.
- Next phase: Check the pending-claim login and delivery in a disposable saved world, then continue the D7 ending paths.

## 2026-09-29 Phase 7 post-grant Fungal Spine recovery

- Behavior changed: An owner who loses a Spine after its grant was recorded can return to the same living, fully spent Bloom and sneak-right-click empty-handed to reclaim it. The normal spent-Bloom message points to that action. Recovery does not reset Communion or produce a Spine for a stranger, an item-held click, a player already carrying one, or a new Bloom at the old coordinates.
- Files changed: `QliphothBloomBlock.java`, `ClinicalBloodProgressionGameTests.java`, `00-player-journey.md`, and Phase 7/8 notes.
- Tests run and exact result: The new owner-reclaim GameTest failed before the block change. A held-item regression then failed until recovery required an empty hand. Replacing the direct block call with `ServerPlayerGameMode.useItemOn` exposed another failure: the Bloom's item-use handler consumed an empty-hand click before default block use. Routing an empty stack into the recovery check fixed it. The final `runClinicalGameTestServer --offline --quiet --no-daemon` passed all 42 required GameTests, including stranger, held-item, duplicate, and same-coordinate replacement checks through the server player interaction path.
- Live scenarios observed: None. Server game-mode routing is covered, but a real client click was not observed.
- Remaining failures or unverified behavior: Live client sneak-click input, a saved-world reload, item loss after the original Bloom is severed or removed, both endings, and the integrated gate remain open. The normal ending route no longer depends on keeping the original Spine once the revelation response has been made.
- Next phase: Exercise the reclaim and projection in a disposable live world, then continue the Vesper and Apotheosis ending paths.

## 2026-09-29 Phase 8 integrated gate after Spine recovery

- Behavior changed: No additional production behavior. The Vein-Mason device GameTest now reports the checkpoint's unmet message when it fails, rather than only a generic assertion.
- Files changed: `HarbingerJourneyFixtureGameTests.java` and Phase 8 notes.
- Tests run and exact result: A fresh `runGameTestServer -PcombinedGameTestDirectory=build/harbinger-redesign-integrated-20260929-fourth --offline --quiet --no-daemon` completed 582 GameTests with four failures: Vein-Mason Effigy outcome, two Veinwing flight cases, and entity-type exposure. After adding the Vein-Mason diagnostic, a second fresh run in `build/harbinger-redesign-integrated-20260929-fifth` completed 582 with seven failures: two Mortarbound, Impressment, two Veinwing, Carousel, and embedded-feather health. Vein-Mason passed in the second run. Neither run is a green integrated gate.
- Live scenarios observed: None.
- Remaining failures or unverified behavior: The one-run Vein-Mason failure was not reproduced, and no root cause is established. The variable combined failures need separate diagnosis. Both live endings, saved-world reload, client presentation, and multiplayer ownership remain open.
- Next phase: Continue live-path validation and revisit the intermittent combined failures with their specific unmet diagnostics; do not change unrelated gameplay from a single run.

## 2026-09-29 Phase 8 assisted-client early route

- Behavior changed: The developer Harbinger journey now earns Blood Absorption and Venous Stone through their production actions before the remaining D1 proofs and Vicar return. It picks up the Vicar's dropped Hematic Iron Scrap for the later Alembic input and finds Concentrated Blood even when it lands outside a full hotbar. Red Taxonomy checks the five newly awarded vials against its stage baseline, not an absolute inventory count. The Living Bestiary surrender fixture provides a new Alchemist and accepts the real Enzyme Primer drop. A resumed run skips actions for an already-passed Harbinger checkpoint, and the runner stops on its first fixture-transition failure so that error is not replaced by later retries.
- Files changed: `HemoJourneyStage`, `HemoJourneyFixtures`, `HemoJourneyChecks`, `HemoJourneyController`, `HarbingerJourneyAutomation`, `JourneyAutoRunner`, `ClinicalBloodProgressionGameTests`, `JourneyAutomationGameTests`, `HarbingerJourneyFixtureGameTests`, and the exact stage-order source contract. Production rank gates and save schemas were not changed in this slice.
- Tests run and exact result: The new D1 proof GameTest failed before the stage existed and passed after the fixture and synthetic-player lookup were corrected. `runClinicalGameTestServer --offline --quiet --no-daemon` passed 43/43. `GameTestHarnessSourceContractTest` passed 10/10 after its expected stage list was updated. The latest fresh combined run in `build/harbinger-redesign-integrated-20260929-tenth` completed 582 GameTests with seven unrelated failures in natural spawn, Veinwing, Carousel, and Circus; the D1 proof, D3 full-hotbar, formation runner, Red Taxonomy, and Living Bestiary cases reported no failures. The combined gate remains red.
- Live scenarios observed: A fresh disposable client world initially reached `vicar_reward` but lacked the absorption and Venous Stone ledger flags. With the added checkpoint, another fresh assisted run reached Degree 2 and First Separation; it then exposed the dropped-scrap handoff. A later run reached ordinary Alembic distillation, and resuming its saved world selected Concentrated Blood from main inventory and reached Degree 3. The latest fresh assisted run passed Body Answers, all four Red Taxonomy diagnoses, and Living Bestiary record, then stopped at the missing Alchemist in the surrender fixture; its replacement and drop check have GameTest coverage but have not been replayed in a client yet. These runs use fixtures, server-side automation, accelerated machine/ceremony time, and a direct completed-rest call. They are not natural survival or visual acceptance.
- Remaining failures or unverified behavior: One resumed Red Taxonomy world had a latched checkpoint but could not prepare the next fixture because an owned Stone platform tile remained after reload. Loading and verifying each recorded chunk during cleanup did not resolve that observed world; its exact first transition error was obscured by repeated automatic retries, which the runner now stops. A fresh client run crossed the same transition without a restart. Full Main-only D0-D7, both ending routes, genuine sleep, saved-world fixture recovery, multiplayer, and client presentation remain open.
- Next phase: Reproduce the first cold-reload transition failure with the runner's new immediate diagnostic, then continue the assisted route from the Bestiary surrender and perform separate natural-play acceptance.

## 2026-09-29 Phase 8 assisted-client Mnemonist and Artificer continuation

- Behavior changed: The Woven Vessel checkpoint expects the Mnemonist's actual reward of one Bleeding Bulb and three Vivacious Enzymes. The assisted Loom action now selects each real strand and pulls it toward the Loom rather than tracking its current position; a resumed paid weave finishes without trying to start again. The fork-upgrade fixture removes the prior fitted Helm, Chestplate, and Leggings so its single Vitriol is spent on the intended Boots, and automation stays restrained until the Armature's real 100-tick craft completes. These are developer-journey changes, not new production rank gates.
- Tests run and exact result: The three-enzyme reward test failed before the expectation changed and passed afterward. The new real-Loom GameTest failed at an undrawn strand before the drag change, then passed with the actual Blood Shot output; a paid-weave resume test passed. The new real-Armature GameTest first failed because the fixture left other eligible armor equipped, then because automation left the restraint before the craft timer elapsed; it passed after both corrections. The latest fresh combined run in `build/harbinger-redesign-integrated-20260929-twentieth` completed 585 GameTests with 11 other required failures across wall pursuit, Synaptic Step, transfer packing, Mnemonist daylight/pursuit, Veinwing, Carousel, and Circus succession. The combined gate is still red.
- Live scenarios observed: A fresh assisted client world passed the Bestiary surrender and reached Woven Vessel. Its first Woven checkpoint stopped because one three-enzyme drop could not match the one-enzyme expectation. After that fix, the same saved world passed the handoff but hit the cold-reload platform failure. Clearing only the 25 tiles recorded as fixture-owned in that disposable world let it reach the Loom. A resumed paid weave then produced the real memory, but the next cold-reload transition hit the same platform failure; after the same manual fixture cleanup it reached the Artificer fork. The fork used its one Vitriol on the leftover Helm, not the intended Boots; the corrected fixture and restraint duration passed GameTest but have not yet been replayed in a client.
- Remaining failures or unverified behavior: The first saved-world transition diagnostic is now precise: the next fixture sees Stone at the first recorded platform tile even though the ownership list was present after login. The failed transition then clears that list, leaving the platform for manual removal. Its cause is not established, and the manual `fill` commands in disposable worlds are not a fix or acceptance evidence. The assisted route has not reached D4; natural Main-only play, both endings, genuine sleep, multiplayer, and visual acceptance remain open.
- Next phase: Diagnose the saved-world cleanup at the ownership/read, block-removal, and next-position-check boundaries. Then replay the corrected fork and continue the assisted route, followed by separate survival and ending-path acceptance.

## 2026-09-29 Phase 8 Barbed research resume

- Behavior changed: Assisted Barbed research now skips specimens already recorded before a restart and selects the exact empty jar, rather than the first jar of the same item type. The developer fixture cleanup rechecks every owned block after its removal pass and retains ownership if any block remains; this does not yet resolve the saved-world Stone transition.
- Tests run and exact result: A new partial-progress GameTest first failed on the missing already-captured Barbed Urchin. After the skip, it recorded Desiccant but reused the filled jar for Venom-Rib Centipede; selecting the exact empty slot made it pass. `runClinicalGameTestServer --offline --quiet --no-daemon` passed 43/43. The latest fresh combined run in `build/harbinger-redesign-integrated-20260929-twentysixth` completed 586 GameTests with five failures outside the new journey cases: Synaptic Step, mandatory jigsaw wings, Mnemonist daylight/pursuit, and two Veinwing flight cases. The combined gate remains red.
- Live scenarios observed: A fresh disposable client run passed Woven Vessel, the real Loom weave, and the corrected Barbed Boots Armature upgrade, then reached Barbed research. After a stop/reopen, the client retained one recorded specimen and its owned platform list. The resumed action recorded the two remaining specimens through the Alchemist. Its next fixture still rejected Stone at the first platform tile; diagnostic preparation saw zero owned blocks, although the saved list was present after login and after a status check. The failed transition left the checkpoint latched. This replay is assisted and accelerated, not natural survival evidence.
- Remaining failures or unverified behavior: The ownership loss occurs before the next fixture's cleanup begins, but its exact call boundary is not yet established. The new post-removal check cannot help when no owned positions reach cleanup. D4-D8 assisted continuation, natural Main-only flow, real sleep, saved-world fixture recovery, both endings, multiplayer, and visual acceptance remain open.
- Next phase: Trace ownership through `HemoJourneyController.next` verification and output cleanup in a replayable saved checkpoint, then fix the source of the lost list. Continue the Main route after that without manual platform edits.

## 2026-09-29 Phase 8 saved-world and scar-to-Artificer continuation

- Behavior changed: The developer journey now stores owned fixture blocks as an NBT long array and reads both long-array and older list tags. This fixes cleanup after a saved-world reload. Scar automation uses the same server-side craft checks and advancement grant as the player packet; a previously carved lesson scar can resume without carving over its output. The Living Arsenal fixture uses a daylight-safe Husk and selects its own marked target. Crimson Vestment automation collects the Alchemist's dropped lacquer before preparing the Armature.
- Files changed: `HemoJourneyFixtures.java`, `HarbingerJourneyAutomation.java`, `JourneyAutomationGameTests.java`, `HarbingerJourneyFixtureGameTests.java`, `PacketScarCraftingEvent.java`, and Phase 8 notes. The temporary ownership trace was removed after diagnosis.
- Tests run and exact result: The long-array cleanup regression failed before the storage fix and passed afterward, including legacy-list cleanup. The first-scar automation and finished-output resume regressions each failed before their fixes and passed afterward. The daylight-safe Arsenal target and lacquer pickup regressions also failed before their fixes and passed afterward. The existing full Crimson Vestment GameTest now exercises automation's real Alchemist dialogue and pickup before Armature preparation and passed. `runClinicalGameTestServer --offline --quiet --no-daemon` passed 43/43. The latest combined run completed 591 GameTests with eight other required failures across combat, transfer, Synaptic Step, and Veinwing; it is not a green integrated gate.
- Live scenarios observed: A copied, disposable save resumed Barbed research with an owned-block long array and advanced through the Artificer fitting into Degree 4. After the scar fix, that same retained save passed the first scar, Effigy, Degree 5, and first Living Graft. With a replacement marked target supplied after the old Zombie burned, it passed the Living Arsenal demonstration and reached Crimson Vestment counsel. The pre-fix counsel had left a ground drop uncollected and its failed transition discarded it. A later attempt to restore older player NBT alone was not a valid full replay because advancement files remained at newer progression; it stopped at the already-claimed Vein-Mason lesson.
- Remaining failures or unverified behavior: A clean saved-client replay of the repaired lacquer handoff is still open; the full real-dialogue GameTest covers that behavior. These client runs use developer fixtures, scripted actions, and accelerated ceremonies, not natural survival. Multiplayer ownership, visual acceptance, both endings, and a green integrated gate remain open.
- Next phase: Resume from a coherent whole-world save or a fresh assisted world to continue D5-D8 and ending validation, then exercise natural survival and multiplayer separately.

## 2026-09-29 Phase 8 assisted D5-to-Apotheos replay

- Behavior changed: The developer journey now aims the one-lacquer Blood Lust and one-proboscis Edacious Armature cycles at Boots, even when the prior full armor set was equipped. Founding Fane keeps its consecrated Bloodwell while later fixtures move to a clear site. The Degree-6 scar station grants its normal personal craft access, and the journey snapshot restores the prior station craft count. The Archon fixture supplies a sworn bloodline NPC at the recipe's role marker and assigns it through the real ally service before completion. A blocked helper station can be retried during inscription; Archon automation can resume its own already-active rite, but rejects a different active rite.
- Files changed: `HemoJourneyFixtures.java`, `HemoJourneyWorldState.java`, `HemoJourneyController.java`, `HemoJourneySnapshot.java`, `HarbingerJourneyAutomation.java`, `JourneyAutomationGameTests.java`, `HarbingerJourneyFixtureGameTests.java`, and Phase 8 notes. These are test journey and snapshot changes, not a bypass of the production helper or rank gates.
- Tests run and exact result: The Blood Lust Boots, permanent-Bloodwell relocation, scar access/restore, Archon helper, Edacious Boots, and blocked-station retry regressions each failed before their respective fixes and passed afterward. `runClinicalGameTestServer --offline --console=plain` passed 43/43 required tests. The latest `runGameTestServer --offline --console=plain` completed 597 GameTests with seven failures outside the new journey cases (three-helper ring, Synaptic Step, outpost wings, blood curs, two Veinwing cases, and Necrosis). The combined gate remains red; `git diff --check` on the changed journey source found no whitespace errors.
- Live scenarios observed: A fresh disposable assisted client world passed the lacquer handoff, Blood Lust Boots, real Founding Fane, relocated Sanctified rite, Degree-6 continuation scar, Chamber return, Covenant Throne/Vigil, and Archon promotion. Its first D7 Armature attempt upgraded the Helm and timed out; the revised Boots-only fixture passed a GameTest. A separate fresh client world stalled at Covenant Vigil after an initial unsafe-footing warning. Resetting that same saved journey then passed Vigil, Archon, Edacious Boots, nine Bloom pomes, Spine projection and choice, the Apotheos rite, and original snapshot restoration. This is an accelerated fixture/automation route, not natural survival or a visual sign-off.
- Remaining failures or unverified behavior: The initial Vigil footing warning and one timeout are not explained by the successful retry; the phase-aware retry test covers a temporary obstruction, not its precise live cause. The Silent refusal still requires a real Sickle, Chamber portal, both Vesper phases, Blood Absorption finish, and return. Natural Main-only progression, genuine timed ceremonies and sleep, saved-world restart through the ending, multiplayer ownership, client presentation, and a green integrated gate remain open.
- Next phase: Diagnose the first-attempt Vigil station availability in a disposable live world, then run the manual Silent ending and separate natural-survival and multiplayer acceptance.

## 2026-09-29 Phase 8 sworn-helper replay

- Behavior changed: The developer journey no longer sends an already-assigned successor through `tryAssignNpc` again. That interaction cycles the NPC to another role; for a fixture helper already holding Anchor, the assist now checks the existing assignment and availability instead. Production ally eligibility, station safety, and helper-count gates are unchanged.
- Files changed: `HemoJourneyWorldState.java`, `JourneyAutomationGameTests.java`, and Phase 8 notes.
- Tests run and exact result: The repeated-assist regression made the Attendant station safe and first failed because the fixture helper moved away from Anchor. It passed after the idempotent check, alongside the blocked-station retry. The latest combined `runGameTestServer --offline --console=plain` completed 597 GameTests with five other required failures; it is not a green integrated gate. `runClinicalGameTestServer --offline --console=plain` passed 43/43 required tests.
- Live scenarios observed: A new disposable assisted client world passed Covenant Vigil on its first attempt without the earlier unsafe-footing warning, then completed Archon, Edacious Boots, Communion, Apotheos, and snapshot restoration. The earlier Vigil timeout remains a real observed failure; one clean replay does not prove it cannot recur.
- Remaining failures or unverified behavior: A longer repeatability run would be needed to close the intermittent Vigil concern. The real Silent/Vesper ending, natural Main-only progression, genuine timed ceremonies and sleep, saved-world ending reload, multiplayer, visual acceptance, and the integrated gate remain open.
- Next phase: Exercise the manual Silent route in a disposable client world and verify Vesper's two phases, Blood Absorption finish, return, and save/reload without substituting advancement flags.

## 2026-09-29 Phase 8 Silent refusal entry and ordeal recovery

- Behavior changed: A developer-only `hemo test journey harbinger run_to_choice` mode now stops before the ending response, retaining the earned Spine, owned Bloom, and journey snapshot for manual play. The Silent refusal fixture teaches and equips Conjure Blade when needed so its supplied Living Staff can actually become a Living Arsenal weapon. Production unlocks and Vesper victory gates are unchanged. Ordeal recovery now prunes duplicate owner-bound Vespers that load after a login replacement, retaining an Evening Star over a stray first-phase boss.
- Tests run and exact result: The to-choice GameTest passed in the combined gate. The Silent fixture regression failed before the Blade grant and passed afterward; `runClinicalGameTestServer --offline --console=plain` passed 43/43. Two new duplicate-recovery GameTests failed before the manager change and passed afterward. The latest `runGameTestServer --offline --console=plain` completed 600 tests with five unrelated required failures in Synaptic Step, Mnemonist pursuit, two Veinwing cases, and Entity Jolt caster safety. The integrated gate remains red.
- Live scenarios observed: In disposable `HarbingerRedesignSilent_20260929`, the assisted D0-D7 route paused with nine owner-bound Pomes, Communion complete, the original Bloom, and the earned Spine. The player used the Spine, spent the normal two minutes in the Fungal Gardens, returned, and selected Silence in the rendered whisper. After the fixture correction and a saved-world reset/replay, Conjure Blade selected through the manipulation key turned the supplied Staff into a Blade. Two ordinary Bloom interactions shaped the bound Sickle and severed that same tree. The portal entered the Chamber and spawned Crowned Refusal. The player died to Vesper's summons, respawned beside the still-open Bloom, and reentered empty-handed with `SILENT_PENDING` still set. A cold reopen of an active retry initially showed two Crowned Refusals; after the recovery fix, the same test showed one several seconds after login. Temporary Resistance was used only for that final reload observation.
- Remaining failures or unverified behavior: No throne anchor was broken and neither Vesper phase nor Blood Absorption finish, Silent Archon promotion, Memory of Vesper delivery, or sealed Bloom was accepted live. The second client replay used developer fixtures and accelerated earlier ceremonies; it does not establish natural survival, multiplayer ownership, or a fair solo victory. The failed first fight and active-reload test leave an unfinished disposable world, not a completed ending.
- Next phase: Fight both Vesper phases with normal player input and verify the Blood Absorption finish, return, unique Memory delivery, sealed Bloom, and snapshot restoration. Then run natural-survival and multiplayer ownership acceptance separately.

## 2026-09-29 Phase 8 Silent ending and death-snapshot follow-up

- Behavior changed: The developer journey now copies its active snapshot and fixture keys when its player dies. The copy is limited to `hemomancy.dev_test.journey.*` data and does not overwrite unrelated data on the replacement player. Production ending and reward rules were not changed in this pass.
- Tests run and exact result: The new death-copy GameTest failed compilation before the helper existed, then compiled and passed in `runGameTestServer --offline --console=plain`. That combined run completed 601 tests with six other required failures: natural overgrowth spawning, Synaptic Step, Mnemonist pursuit, two Veinwing tests, and reactive target acquisition. It is not a green integrated gate.
- Live scenarios observed: The saved `HarbingerRedesignSilent_20260929` ordeal reopened with one Crowned Refusal. The player drove its health to each authored floor with `/damage`, broke all three exposed multipart anchors with actual client sword attacks, and saw the real Evening Star replace phase one. `/damage` downed phase two; after its defeat animation, holding the ordinary Blood Absorption item completed the finish. The player returned to the overworld as D7 `SILENT_ARCHON` with one Memory of Vesper. After disconnect and reopen, the path and item persisted, the Vesper advancement file was done, and the original Bloom's saved `bloomStates` entry was `SEALED`.
- Test assists and limits: Resistance, knockback resistance, a supplied Netherite Sword and Blood Absorption item, raised attack damage, teleports, minion removal, and boss `/damage` commands were used. This proves the state transitions, multipart hit routing, final channel, reward, and saved result in a live client, not fight balance or a fair solo clear. The previous death had already lost the dev snapshot before the clone fix was loaded, so `/hemo test journey harbinger next` reported no active snapshot in this completed save. In a separate disposable world with the fix loaded, a journey start, `/kill`, respawn, status, and clear retained the snapshot through death and restored the pre-journey position; the snapshot key was then removed. A full D7 death-and-restore replay is still open. Natural Main-only progression, multiplayer ownership, visual acceptance, and a green integrated gate remain open.
- Next phase: Test a fair survival fight and multiplayer owner/stranger behavior without combat assists, then repeat the D7 death-and-restore route with the fixed journey.

## 2026-09-29 Phase 8 D7 death recovery and Vigil helper lifetime

- Behavior changed: A completed Cardinal rite keeps its assigned successor at the station until the final helper check and rite removal; completion alone no longer sends that NPC home first. The developer Covenant Vigil fixture now supplies a bloodline-member original Vicar instead of registering a successor whose workplace is a temporary rite focus. This follows the supported recruited-Vicar helper route without changing production recruitment or helper eligibility.
- Tests run and exact result: The successor completion GameTest passed with a valid Fane/workplace, failed when the old `rite.isComplete()` return condition was restored, and passed again with that condition removed. A separate recruited-Vicar fixture test passed after an explicit residency tick. `runClinicalGameTestServer --console=plain` passed 43/43. The final combined `runGameTestServer --console=plain` completed 603 tests with five other required failures: Synaptic Step, transfer packing, two Veinwing cases, and Blood Aneurysm. `git diff --check` found no whitespace errors in the changed files. The integrated gate remains red.
- Live scenarios observed: In disposable `HarbingerSnapshotDeath_20260929`, an assisted D0-D7 run reached Apotheos choice. After `/kill` and respawn, the journey stage and snapshot remained; `hemo test journey harbinger status` still recognized the choice. `hemo test clear` restored the pre-journey D0 position and inventory and removed the snapshot. That run's first Covenant Vigil activation fell silent after its helper became unavailable, but a retry after reopening the save completed Vigil and reached D7. Inspection found the original fixture successor's recorded workplace at the temporary rite focus; that focus did not provide a stable successor home. With the fixture and completion-lifetime fixes loaded, a fresh disposable `HarbingerVigilFirstPass_20260929` client world completed Vigil on its first activation, reached Apotheos choice, and cleared back to D0 with `hemo test clear`.
- Remaining failures or unverified behavior: Both live routes used developer fixtures and accelerated progression, not natural survival. An unassisted timed ceremony with a genuinely recruited Vicar, a fair Silent fight, multiplayer owner/stranger behavior, and visual presentation remain open. The five unrelated combined-suite failures still need their own investigation.

## 2026-09-29 Phase 8 two-player Bloom ownership and alpha gate

- Behavior changed: No production behavior changed in this pass. The two-client test used separate assisted D0-D7 journeys in one disposable world. The peer's first Founding Fane was rejected inside the host's footprint, as intended; a second start in swamp terrain found no clear fixture volume. A temporary high platform let the peer found a distinct Fane far from the host. These are developer-harness site-selection limits, not permission to overlap Fanes.
- Tests run and exact result: `alphaCheck --continue --console=plain` completed 2,350 JVM tests with 18 failures and 603 combined GameTests with 10 required failures; the resource-error gate could not complete because its GameTest dependency failed. The GameTest failures included variable Mortarbound, Veinwing, transfer, Carousel, and Silent claimed-Will cases. The JVM list still includes `BloodProfileMigrationTest`: its approved restriction fixture expects Chitinite to require a syringe, but the committed profile makes it loose-vial eligible, while newer syringe-only profiles are absent from the fixture. The gameplay profile and approval baseline were not changed by guesswork.
- Live scenarios observed: In disposable `HarbingerTwoPlayer_20260929`, host `BookKitReview` and peer `BookKitPeer` connected to the same integrated server, each reached D7 Apotheos choice through separate accelerated Main journeys, and each had its own living, spent Bloom and Fungal Spine. After the peer's Spine was removed, empty-hand sneak-use on its own Bloom returned one Spine. Removing that item again and sneak-using the host's Bloom returned "The fruit tightens against another covenant" and no Spine. Clearing the peer's journey restored its baseline while the host still reported `apotheos_choice` and retained its Bloom; the host then cleared independently. Both clients exited normally.
- Remaining failures or unverified behavior: This proves one owner-bound Bloom interaction and independent developer snapshot cleanup with two connected players. It does not establish natural two-player acquisition, founder recruitment or initiation, NPC event validation, Chamber cell/inventory isolation, rite-owner handoff, other Bloom actions, or either ending under multiplayer. The combined gate remains red, and a Main-only survival run plus visual acceptance are still open.

## 2026-09-29 Phase 8 Main-only assisted route

- Behavior changed: The development journey has a separate `hemo test journey harbinger run_main_to_choice` route. It persists its Main-only selection with the snapshot and skips catalogue work, full armor fittings, extra scar continuations, and the old Vessel Filled checkpoint. The full operator route remains available. Its Vicar reward checker now accepts Liber Sanguinum or Hematic Iron, as the actual First Bloodcraft assignment already does; it no longer requires the unrelated 5,000 mL milestone. The first-proof fixture supplies enough blood for projection without awarding that milestone.
- Tests run and exact result: `compileGameTestJava` passed. After the Vessel Filled removal, the combined GameTest run completed 604 tests with nine required failures in Mortarbound pursuit, Puppeteering, transfer packing, Mnemonist pursuit, activation potential, two Veinwing cases, Carousel, and Entity Jolt. The Main-only transition test was not among the failures. The integrated gate remains red; `git diff --check` on the touched files found no whitespace errors.
- Live scenarios observed: A fresh assisted client run reached D2 via the Liber proof, then exposed the Vicar checker's stale demand for the Hematic Iron milestone. After correction, the saved snapshot resumed through First Separation, distillation, injection/rest, Woven Vessel, the first scar/effigy cycle, Founding Fane, Chamber return, Throne, Vigil, Archon, and Communion, pausing at the D7 response. A second fresh world repeated D0-D7 without visiting Vessel Filled or any optional catalogue or equipment chapter. `hemo test clear` restored that world's starting position and inventory. Both clients exited normally.
- Remaining failures or unverified behavior: These runs use developer fixtures, scripted interactions, supplied blood/materials, and accelerated rites. They do not prove natural survival acquisition, genuine sleep or timed ceremonies, fair ending combat, saved-world recovery through every stage, multiplayer initiation/recruitment, visual presentation, or a green integrated gate.
