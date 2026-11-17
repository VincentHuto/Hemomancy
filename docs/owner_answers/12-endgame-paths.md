# 12. D7 to D8: Monolith, Qliphoth, Apotheos, Silentium, possession, heart-node

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P13). |
| T2 | Built (P13). The follow-up (V-12-2) is built (B23, 2026-11-15): a stranger past the Seed hears `past_the_seed`. |
| T3 | Built (P13). The Liber's Qliphoth page was read and needed no change. |
| T4 | Built (P13). The projection resumes only when the player logs back in inside the Gardens. |
| T5 | Built (P13). |
| T6 | Built (P13). |
| T7 | Built (P13). |
| T8 | Built (B23, 2026-11-15, NQ-12-4 A, V-12-4): the node keeps the respawn it replaced (`PriorRespawn`), a Leave the Gardens button (`HeartNodeLeavePacket`, protocol 54) and the Spine with no return point go to it, else the world spawn, and the node gives it back when it goes. The walk home in the Gardens is owed to `heartNodeReviewClient`. |
| T9 | Built (P13, B23): FX-16 items 1 to 4 (P13); item 5 (B23): a reset Apotheos loses the node and gets their old respawn back, checked again at login. |
| T10 | Built (P13). |
| T11 | Built (B24, 2026-11-15): Silentium trees for the Vicar and Alchemist, a Silentium greeting and lore card in the Mnemonist's woven tree, a Silentium greeting and the Silent Archon vestments lesson in the Artificer's workshop, and one Silentium line for the Hermit (UN-19 A). `greetingDegree` keeps a Silentium at 8; the probe is `silentiumHasAGreetingOfTheirOwn`, required. No new event, so no route fingerprint changed (to be confirmed by the combined run). |
| T12 | Built (P13, B23): the gaze, the Visitation refusal, the totem and wax particles (P13); the axe rule written into reference §5.6f and a new Alchemist bog body inquiry (B23, NQ-12-5). Possession minor V-12-5 (Mind Broken survives a death) built too. |
| T13 | Dropped (NQ-12-3, 2026-11-14): the ability keeps the name Silent Severance. |
| T14 | Built (P13). |
| PT-74 (no task) | Built (P10, B23): the Cornerstone warning (D-23) and 8 fragments from every shatter (NQ-12-1 A). |
| PT-77 (no task) | Built (B23, NQ-12-2 A, V-12-1): Monolith items D7 and Residua D8, each veiled one Degree early; the Silent Archon chestplate at D8 and the three lineage chests at D7. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings leave the endgame's numbers alone and fix its seams. Thirteen rows keep what is built. Seventeen need work, and most of that is small: whisper order, a Monolith owner, a warning before the ninth pome, a paused projection, the Truth entry, a dormant Podium, pruned synapses, two pointer lines and two fix lists.

The largest pieces are the Silentium greeting trees for four teachers (PT-83) and the way home for a stranded Apotheos (PT-80).

Surprises from the code:

- PT-74, the "shatter roll". A shattered Monolith drops a random 5 to 8 Monolith Fragments. Shatter before taking the stone's free Cornerstone and the Weight of the Frame needs 8. Only one roll in four gives 8. Explained in full under NQ-12-1.
- PT-72. The probe asserts the opposite of the ruling (it expects no lock). The lock also holds a Silence through both Vesper phases. Weapon forms still change by selecting them, so the Living Arsenal survives the lock.
- PT-79. By the time a player has chosen Silence, their own Monolith is a broken husk that says one line. So the Vicar is the speaker, not the Monolith.
- PT-80. The Spine's return point is lost at every death, and a death outside the Gardens leaves the Spine behind. "Their bed" also needs remembering, because planting the node overwrites it.
- CD-10 and CD-12 are already true in code and lore. Only stale text is left.
- PT-73. The Seed half is already fixed (the Seed is bound and only the breaker walks in). Only the shatter needs binding.
- No endgame code under these rows changed after 2026-11-11, so there are no conflicts.

Counts: 39 rows. BUILD 17, KEEP 13, DONE 1, MOOT 0, CONFLICT 0, UNCLEAR 2, OWNER 3, PARKED 3.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| D-3 | A Silentium gains nothing new at D8 until a design lands. | KEEP | `docs/ENDGAME_PATHS.md` ruling of 2026-10-31. `HarbingerPathPermissions.isSilentium` gates the set. No new power found in code. | none |
| D-24 | The Lidless Door stays creative-only. | KEEP | `CreativeTabInit.java` line 74 lists `BlockInit.lidless_door` in the WIP list. No `*lidless*` recipe file exists. | none |
| D-25 | Chamber gateway numbers and looks stay. | KEEP | `SanguineMonolithBlockEntity`: `GATE_BLOOD_PER_TICK` 2.5 for `MonolithEyeRules.CHARGE_TICKS` 40 is 100 blood. `GATE_OPEN_TICKS` 200 is 10 s. Ribs and the sealed gate not opened in this review. | none |
| D-27 | The Fungal Podium goes dormant after the first crossing. The Rite of Visitation is the only way in. | BUILD | `FungalPodiumBlock.handleUse` calls `handleTravelUse` with `bySpine` false, so `FungalProjectionRules.travelKind` still returns `PHYSICAL` for an Apotheos. | T6 |
| D-28 | Molting, tonic and Mind Broken numbers stay. | KEEP | `MoltingRules` (`KEPT` 0.90, `COOLDOWN_TICKS` 24000, cost a full spore load), `MorphicTonicRules` (200 ticks, 1 health per 25 ticks, 2.5 mL a tick), `MindBrokenRules` (1.15 and 0.85). All match. | none |
| DS-1 | Possession stays in the Ancestral recipe-map family. | KEEP | `HarbingerRecipeMapDefinitions.java` line 79: `registerRites("Ancestral", "ancestral_communion", "possession_rite", "visitation_rite")`. | none |
| DS-2 | Prune cut-off synapses. | BUILD | `HeartNodeSavedData.removeSynapse` drops one synapse and leaves the rest. `MycelialSynapseGameTests.aCutOffSynapseKeepsWorking` pins the old rule. | T7 |
| DS-10 | The remaining endgame pieces stay deferred. | PARKED | The 2026-11-13 HUD added an Apotheos binding (`blood_hud/binding_*_apotheos.png`). It added no Silentium art, so that item is still unbuilt. | none |
| PT-72 | Keep the lock from the ninth pome to D8. Document it. Warn before the ninth pome. | BUILD | `HarbingerPathPermissions.isChoiceUnresolved` (9 pomes summed over all Blooms, path not APOTHEOS or SILENTIUM). Called from `BloodManipulation.passBodyGates`, `CardinalRiteActivation`, `CardinalRiteCompletionEffects`, `PuppeteerTrialRiteController`. No warning exists in `QliphothPomeItem`. | T3 |
| PT-73 | Bind the Monolith to its builder. | BUILD | `SanguineMonolithBlock.setPlacedBy` records no placer. `pressFurther` checks only D7 and the open conversation. `owner` is set by `beginBreak` to whoever presses. | T2 |
| PT-74 | The owner does not know what the shatter roll is. Explained in NQ-12-1 and asked again. | UNCLEAR | `MonolithFragmentDropRules` (5 to 8), `SanguineMonolithBlockEntity.blast`, `recipe/monolithic_cornerstone.json`, `recipe/monolith_imbued_cloth.json`, `recipe/cardinal_rite/monolithic_armature.json`. | none |
| PT-75 | A logout pauses the first projection. It resumes at login. | BUILD | `FungalProjectionEvents.onPlayerLogout` calls `performForcedProjectionReturn`, which sets the revelation witnessed. | T4 |
| PT-77 | Answer is the one word "Need". Cut off. Asked again as NQ-12-2. | UNCLEAR | `MaterialsData`: `monolith_fragment`, `monolith_imbued_cloth`, `monolithic_cornerstone` and four `hallowed_residuum_*` are `minDegree(5)`. `MaterialAtlasSpec` gates them `d(5)`. | none |
| PT-78 | Add an Alchemist line that names the Fungal Spine after Communion. | BUILD | `HarbingerAlchemistDialogueTrees.archon` has two greeting lines and no path state. No `fungal_spine` file under `dialogue_inquiry/`. | T10 |
| PT-79 | Add a Monolith or Vicar line that names the Severance to a D7 Silence. Speaker chosen: the Vicar. | BUILD | `SanguineMonolithBlock.handleInteraction`: a broken stone says only `hemomancy.monolith.dormant` or the gate hint. `hemomancy.dialogue.ending.vicar.silent_pending` exists and names only Vesper. | T10 |
| PT-80 | An Apotheos stranded by the heart-node respawn goes to their bed, or the Overworld spawn if that fails. | BUILD | `HeartNodeEvents.plant` overwrites the respawn point with the node. `FungalGardenTravelHelper` keeps the return point in root player data, which a death clears. Reading to confirm in NQ-12-4. | T8 |
| PT-82 | Unlock the Truth entry at APOTHEOS_PENDING. | BUILD | `LiberKnowledgeHelper.unlockForDegree` gives `TRUTH` at D8 only. `RiteDiscoveryRules` gates `apotheos_rite` on `TRUTH`. `FungalWhisperDialogueEvents` sets the path and unlocks nothing. | T5 |
| PT-83 | A D8 Silentium gets a greeting tree of their own. | BUILD | `HarbingerPathPermissions.greetingDegree` returns 7 for a Silentium. Callers: Vicar, Alchemist, Hermit entities. | T11 |
| PT-89 | Decision whispers open first. | BUILD | `PendingWhispers.open` always takes `list.getCompound(0)`. Each entry already carries a `decision` flag. | T1 |
| RV-1 | Keep the heart-node calls, except cut-off synapses: DS-2 wins and they are pruned. No withered-heart gate, no support rule, toggle labels and slider notches stay. "No Liber line" stays until C-8. | KEEP | `MycelialSynapseBlock.sanctumRefusal` (no support rule). `docs/ENDGAME_PATHS.md` "Calls made without the user". Screen labels not opened in this review. | T7 for the one override |
| RV-2 | Apotheos body numbers stay. | KEEP | `SporeLoadRules`: gain 2.0 and 0.5 per second, `BURN_COST` 25, `BURN_HEAL` 6, `BURN_COOLDOWN_TICKS` 100. `ApotheosBodyRules.symbioticSplit` is false for an Apotheos. | none |
| RV-3 | Possession numbers stay. | KEEP | `SurrogateForm` (bat 3, golem 14), `PossessionRules.hitCost` (1, or 2 at 6 damage), `MIND_BROKEN_TICKS` 6000. | none |
| RV-4 | Gaze, grab and adipocere numbers stay. | KEEP | `AbhorrentGazeRules` (range 10, 20 ticks, cost 1, grab 12000), `loot_table/entities/bog_revenant.json` chance 0.35, `PossessionRules.ADIPOCERE_BONUS` 3. | none |
| RV-13 | The Monolith hub stays as built. | KEEP | `SanguineMonolithDialogueTrees` (press further only in `archon`), `MonolithDialogueContext.CONVERSATION_TICKS` 6000, `npc_trade/monolith/` has three cards. Not changed since 2026-11-05. | none |
| RV-14 | Possession rite cost 0 on the borrowed Qliphoth grand floor stays. | KEEP | `recipe/cardinal_rite/possession_rite.json`: `bloodCost` 0, `purpose` bond, `special` true, floor `hemomancy:qliphoth_grand`. | none |
| RV-76 | The overnight endgame calls stay, the forced heart-node respawn among them. | KEEP | `HeartNodeEvents.plant` sets a forced respawn. This is why PT-80 is planned as a way home and not as a change to the respawn. | none |
| RV-77 | Mind Broken sends no fungal whisper. | KEEP | `MindBrokenEffect` class comment. No whisper call in `PossessionService`. | none |
| FX-16 | Fix the five heart-node minors together. | BUILD | `FruitingBodyCacheBlock.useWithoutItem` uses `Inventory.add`. `HeartNodeEvents.cacheSpot` takes `isAir()` only. The other three read from `docs/superpowers/OVERNIGHT_REPORT_2026-10-28.md` section 3. | T9 |
| FX-17 | Fix the possession minors together. | BUILD | `AbhorrentThoughtEntity.customServerAiStep` has no game-mode check. `VisitationService.refusal` has no Chamber check. `PossessionEvents` has two `ParticleTypes.TOTEM_OF_UNDYING` bursts. `BogBodyBlock.scrapeWax` uses `levelEvent(3004)`. | T12 |
| CN-11 | Spore load feeding manipulations stays deferred. | PARKED | Not built. No call found. | none |
| C-2 | Rename the armour ability. The cure route keeps "Severance". No new name was given. | BUILD | `ArmorSetAbilityRegistry.SILENT_ARCHON_SEVERANCE` (`hemomancy:silent_archon_severance`), `SilentArchonArmorAbilityHandler.activateSilentArchonSeverance`, three lang keys. Name asked in NQ-12-3. | T13 |
| C-4 | Fold LORE section 3.3 into the Apotheos section. | BUILD | `docs/LORE_REFERENCE.md` line 156 is still its own section. The Apotheos text is the Degree 8 block of section 6.4a (lines 431 to 437). | T14 |
| C-8 | Add an Apotheos section to the Liber together with D-1. | PARKED | No Liber page mentions the heart-node or Apotheos body. D-1 (Canon Memory rework) is unanswered and belongs to chunk 08. | none |
| CD-3 | Cut `spine_growth.degree8.canon` and fix the `coreWitnessDialogue` Javadoc. | BUILD | `FungalWhisperDialogueTrees.java` line 278 still maps case 8 to the key. The Javadoc at line 305 still says the whisper fires at a pool in the Gardens. | T14 |
| CD-10 | Code follows lore: Silent Archon vestments after Silentium. | BUILD | The code already does this. The four recipes need `required_degree` 8 and the silent path (`SilentArchonArmatureDegreeTest`). `ArtificerAssignments.recordD7Lineage` refuses Silent Archon below Silentium. Only three text lines still read wrong. | T14 |
| CD-12 | Follow the code for the Monolith's Degree numbering. | DONE | `SanguineMonolithBlock.MIN_DEGREE` 5. LORE section 6.5a now numbers the steps 5, 6, 7. Section 10 no longer mentions the Monolith. `docs/HEMOMANCY_REFERENCE.md` line 4859 agrees. Only the stale note in `docs/NPC_DIALOGUE_AND_TRADE.md` line 173 is left. | T14 |
| PR-4 | One owner session for the Apotheos D8 journey. | OWNER | `heartNodeReviewClient` exists in `gradle/review-clients.gradle`. | none |
| PR-5 | One owner session for the Silentium D8 journey. | OWNER | `possessionReviewClient` exists in `gradle/review-clients.gradle`. | none |
| PR-12 | Launch the review client for the heart-node screen, spread, set pieces and possession scenes. | OWNER | Both client rows exist. No shader was changed by this review. | none |

## Plan

### T1. Decision whispers open first (size S)

- Rulings: PT-89
- Change: `PendingWhispers.open` opens the oldest entry whose `decision` flag is true. With none, it opens the oldest entry as now. The order of the rest does not change.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/PendingWhispers.java`
- Tests: make `PlaythroughArchonGameTests.revelationWhisperOpensFirst` required. `PlaythroughLateNpcUsabilityGameTests.openUntilChoice` should report the choice at position 1. Add a JVM test if the pick rule is pulled into a pure helper.
- Docs: `docs/ENDGAME_PATHS.md` rulings, `docs/PLAYTHROUGH_TESTS.md` (D-89 closed), the roll-up.
- Depends on: nothing

### T2. Bind the Monolith to its builder (size M)

- Rulings: PT-73
- Change: `SanguineMonolithBlock.setPlacedBy` records the placing player's UUID on the block entity as its builder (saved and synced). `pressFurther` refuses anyone else with a new action-bar line, and the Archon tree hides the press-further button from a non-builder. A stone with no builder (placed by command, or standing before this change) stays open to any D7. Counsel, Conversation, Inquiries, Trade and the Cornerstone claim stay open to every eligible player. That last sentence is my assumption: the question was only about the shatter.
- Already true: the Seed is bound to the breaker (`SeedBinding.bind` in `takeSeed` and `spillSeed`) and only the breaker walks into the gap (`admits`). Fragments still drop as loose items.
- Files: `common/block/harbinger/functional/SanguineMonolithBlock.java`, `common/tile/harbinger/functional/SanguineMonolithBlockEntity.java`, `common/entity/npc/dialogue/SanguineMonolithDialogueTrees.java` (`hub` needs to know the stone), `assets/hemomancy/lang/en_us.json`
- Tests: make `PlaythroughArchonGameTests.strangerShattersYourMonolith` required. `SanguineMonolithDialogueTreesTest` for the hidden button. `PlaythroughSites.monolith` already places through the block item, so the builder is set in tests.
- Docs: `docs/HEMOMANCY_REFERENCE.md` section 5.9 stage 1 and the block table row at line 4859, `docs/LORE_REFERENCE.md` section 6.5a, `docs/ENDGAME_PATHS.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: nothing

### T3. Document the power lock and warn before the ninth pome (size M)

- Rulings: PT-72
- Change: no rule changes. `HarbingerPathPermissions.isChoiceUnresolved` stays as it is. Add the warning in `QliphothPomeItem`: a chat line when the eighth pome is eaten, and a tooltip line on a pome when the player has eaten eight. The warning must read the same number the lock reads, the total over all Blooms (`getTotalPomesConsumed`), not the per-Bloom Communion count. Otherwise a player who split pomes over two Blooms is locked with no warning.
- What the warning and the docs must say: after the ninth husk no manipulation answers except `conjure_staff`, and no rite starts except the Rite of Apotheos, until D8. Weapon forms still change by selecting them on the wheel (`ChangeSelectedManipPacket` calls `LivingStaffWeaponFormHelper.applySelection` and does not pass the lock). So a Silence fights both Vesper phases with the Living Arsenal and no manipulations.
- Files: `common/item/harbinger/QliphothPomeItem.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `PlaythroughArchonGameTests.powerLockAfterTheNinthPome` currently asserts that a Silence and a split-pome player can still cast. Under the ruling it must assert the lock holds and the eighth-pome warning was said. Then make it required. `HarbingerPathPermissionsTest` does not change.
- Docs: a new paragraph in `docs/HEMOMANCY_REFERENCE.md` section 5.9 (line 2637 already names "the ninth-pome power lock" with no explanation), `docs/ENDGAME_PATHS.md`, `wiki/The-Qliphoth.md`, `wiki/Harbinger-Path.md`, `docs/PLAYTHROUGH_TESTS.md`. The Liber's Qliphoth page was not opened in this review. Check it when building.
- Depends on: nothing

### T4. Pause and resume the first projection (size M)

- Rulings: PT-75
- Change: remove the forced return from `FungalProjectionEvents.onPlayerLogout`. The projection's two keys (`PROJECTION_ACTIVE`, `PROJECTION_REMAINING`) already survive a logout, and the tick handler already counts down only while the player is in the Gardens. At login the remaining time is sent to the client again (`PacketSyncFungalProjection`) through the `PlayerStateSync` table, not a new login handler. The revelation is granted only when the timer reaches 0.
- Not covered: with no Gardens dimension loaded the 500 mL is still spent. That only happens on the GameTest server.
- Files: `common/event/FungalProjectionEvents.java`, `common/encounter/FungalGardenTravelHelper.java`, `common/capability/PlayerStateSync.java`
- Tests: make `PlaythroughArchonGameTests.logoutCompletesProjection` required. Update the count for `FungalProjectionEvents.java` in `src/test/resources/architecture/persistent-data-allow-list.txt` if the number of `getPersistentData()` calls changes (`PersistentDataAllowListTest`). `FungalProjectionSourceTest` may pin the logout call. Check it.
- Docs: `docs/HEMOMANCY_REFERENCE.md` section 5.6, `docs/ENDGAME_PATHS.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: nothing

### T5. The Truth entry at APOTHEOS_PENDING (size S)

- Rulings: PT-82
- Change: when the player chooses the Eighth Degree (`archon_choice_eighth_degree` in `FungalWhisperDialogueEvents`), unlock `LiberEntryDefinitions.TRUTH` with source `DIALOGUE` (the entry already accepts that source). The Rite of Apotheos is then discovered and a blank Mnemonic Blueprint takes it.
- Side effect to accept: `RiteDiscoveryRules` also gates `cardinal_rite/eternal_covenant` (a D6 rite) on the same entry, so that rite is discovered at the same moment.
- Files: `common/entity/npc/dialogue/route/FungalWhisperDialogueEvents.java`
- Tests: make `PlaythroughEndgameGameTests.apotheosRiteNotInTheLiber` required (and rename it). `revelationChoice` in `PlaythroughArchonGameTests` can assert the entry.
- Docs: `docs/HEMOMANCY_REFERENCE.md` section 5.9 stage 5, `docs/ENDGAME_PATHS.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: nothing

### T6. The Fungal Podium goes dormant (size S)

- Rulings: D-27
- Change: in `FungalProjectionRules.travelKind`, after the first crossing every way in is `DORMANT`, Podium included. `PHYSICAL` goes away. The first projection can still start at a Podium. A Podium inside the Gardens still brings its user home, as the Spine does. Creative travel does not change. The Podium needs its own dormant line (the current two lines speak of the Spine "in the hand").
- Files: `common/encounter/FungalProjectionRules.java`, `common/encounter/FungalGardenTravelHelper.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `FungalProjectionRulesTest` lines 30 and 51 expect `PHYSICAL` for an Apotheos at a Podium. The `apotheosBody` and Visitation playthrough tests should not change.
- Docs: `docs/HEMOMANCY_REFERENCE.md` lines 1759 and 4858, `docs/LORE_REFERENCE.md` section 3.1 ("by the Rite of Visitation or a Fungal Podium"), `docs/ENDGAME_PATHS.md` table and rulings, `wiki/Harbinger-Path.md` lines 45 and 106.
- Depends on: nothing. Build before T8, which touches the same helper.

### T7. Prune cut-off synapses (size M)

- Rulings: DS-2, RV-1 (the one override)
- Change: when a synapse is removed, every remaining synapse of that owner that no longer chains to the heart (`HeartNodeFootprint.chainedFrom` returns null) withers and leaves the envelope. Pruned synapses free their place in the budget of 6. Unloaded ones wither when their chunk loads, as `witherAll` already allows. The owner gets one action-bar line. `LobeEnvelope` does not change, so Founding Fane stakes keep their lobes.
- Files: `common/world/HeartNodeSavedData.java`, `common/block/harbinger/functional/MycelialSynapseBlock.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `MycelialSynapseGameTests.aCutOffSynapseKeepsWorking` flips to assert the prune. Add a case to `HeartNodeSavedDataTest`. `HeartNodeFaneSeparationGuardrailTest` and `FaneFootprintTest` must stay green untouched.
- Docs: `docs/HEMOMANCY_REFERENCE.md` section 5.6d (lines 1801 and 1802), `docs/ENDGAME_PATHS.md` ("Calls made without the user" row and rulings), `wiki/World-Content.md`.
- Depends on: nothing

### T8. A way home for a stranded Apotheos (size M)

- Rulings: PT-80 (and RV-76, which keeps the forced respawn)
- Reading planned: the heart-node stays the Apotheos's respawn point. The fix is the way out. NQ-12-4 asks the owner to confirm this against the other reading (death sends them to the bed and the node stops being a respawn point).
- What the design needs:
  1. Remember the bed. `HeartNodeEvents.plant` overwrites the player's respawn point, so the old one (dimension, position, angle, forced flag) is stored first, with the node's entry in `HeartNodeSavedData`. No new `getPersistentData()` call.
  2. A destination when no return point is stored. The return keys are root player data and do not survive a death, so today the Spine "twitches but finds no way". Instead the return goes to the remembered bed if it still stands, else the Overworld world spawn.
  3. A way to ask for it with empty hands. After a death outside the Gardens the Spine lies where the player died. Proposed: a "Leave the Gardens" button on `HeartNodeScreen`, owner only, checked on the server. This is my addition.
  4. When the node is removed or withers, the remembered respawn is restored instead of cleared (`HeartNodeBlock.onRemove`, `HeartNodeEvents.onLogin`).
- Left undecided: whether the return costs anything (the Spine's return is free today, so the plan keeps it free), and the reading itself (NQ-12-4).
- Files: `common/event/HeartNodeEvents.java`, `common/world/HeartNodeSavedData.java`, `common/world/HeartNodeEntry.java`, `common/block/harbinger/functional/HeartNodeBlock.java`, `common/encounter/FungalGardenTravelHelper.java`, `client/screen/tile/functional/HeartNodeScreen.java`, a new payload in `common/network/heartnode/`, `common/network/PacketHandler.java` (bump `PROTOCOL_VERSION`), `src/test/resources/network/payload-registry.txt`, `assets/hemomancy/lang/en_us.json`
- Tests: a pure rule for the destination (bed valid, else world spawn) with a JVM test. `HeartNodeSavedDataTest` for the stored respawn. `PlaythroughEndgameGameTests.heartNodeRespawnTrap` cannot run on the GameTest server (no Gardens). Give it its expected destination and prove it in `heartNodeReviewClient`. `NetworkPackageGuardrailTest` for the new payload.
- Docs: `docs/HEMOMANCY_REFERENCE.md` sections 5.6 and 5.6d, `docs/ENDGAME_PATHS.md`, `wiki/World-Content.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: NQ-12-4 confirmed, T6

### T9. Heart-node minors (size M)

- Rulings: FX-16
- Change, five fixes:
  1. Opening a cache inserts through the item handler (as `SanguineMonolithBlockEntity.takeSeed` does), so Creative no longer discards the overflow.
  2. If the cache's block entity is missing after placement, remove the block and fall back to ordinary drops, so no ownerless cache is left.
  3. A heart-node with no owner (placed by `/setblock` or a structure) withers at its first server tick.
  4. `HeartNodeEvents.cacheSpot` accepts any replaceable block, so a death in water or lava still leaves a cache.
  5. A player who stops being an Apotheos (a reset) loses the forced Gardens respawn and their node withers. The reset entry points were not opened in this review. Find them when building.
- Files: `common/block/harbinger/functional/FruitingBodyCacheBlock.java`, `common/event/HeartNodeEvents.java`, `common/block/harbinger/functional/HeartNodeBlock.java`
- Tests: `HeartNodeRulesTest` for any rule pulled out. GameTest cases beside `MycelialSynapseGameTests` for the cache overflow and the replaceable spot. Items 3 and 5 need the Gardens, so they go to `heartNodeReviewClient`.
- Docs: `docs/ENDGAME_PATHS.md` "Proof owed and known gaps", `docs/HEMOMANCY_REFERENCE.md` section 5.6d.
- Depends on: T8 (item 5 restores the remembered respawn)

### T10. Two pointer lines: the Spine and the Severance (size M)

- Rulings: PT-78, PT-79
- Change, PT-78 (Alchemist): `DialogueHubFactory.withArchonAcknowledgment` already adds one line per path state to the Alchemist's greeting at D7. Add a state for "Communion done, Spine granted, first projection not yet made" and give the Alchemist a line there that names the Fungal Spine. Facts the line may use: it tore free at the ninth husk, it is used from the hand, using it costs blood, the body stays behind. It must not claim to know what lies beyond (the 2026-10-31 hearsay ruling).
- Change, PT-79 (Vicar): the Vicar is the speaker because the player's own Monolith is a broken husk by then. He already says `hemomancy.dialogue.ending.vicar.silent_pending` at exactly this state. Extend it (or add a second line) to name the Severance, the Living Sickle and the Bloom, as hearsay.
- Both lines are written one speaker at a time against that speaker's sheet in `docs/NPC_VOICES.md`.
- Files: `common/entity/npc/dialogue/DialogueHubFactory.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `ArchonEndingDialogueTest` (new state). The ending-lines test in `PlaythroughEndgameGameTests` counts 24 lines and will need the new one. Make `PlaythroughLateNpcUsabilityGameTests.springPointerAfterCommunion` and `severancePointerForSilence` required. `HardCodedEnglishGuardrailTest` stays green (keys only).
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/ENDGAME_PATHS.md`, `docs/PLAYTHROUGH_TESTS.md`.
- Depends on: nothing. Build before T11, which edits the same factory.

### T11. Silentium greeting trees (size L)

- Rulings: PT-83
- Change: the Vicar, Alchemist, Mnemonist and Artificer each get a Silentium tree, chosen when `HarbingerPathPermissions.isSilentium` is true. `greetingDegree` stops sending a Silentium to the Archon tree for the Vicar and Alchemist. The Hermit keeps the Archon tree (his sheet stops at the fourth Degree and `ArchonEndingDialogueTest` pins him out of the ending lines).
- Content limits: nobody at the outpost knows what a Silentium is beyond hearsay. A Silentium hears nothing from the hive. D-3 keeps their powers as built, so the trees promise nothing new. The Artificer's tree is the place where the Silent Archon vestments are offered (CD-10).
- Files: `common/capability/player/harbinger/degree/HarbingerPathPermissions.java`, `common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`, `HarbingerAlchemistDialogueTrees.java`, `HarbingerMnemonistDialogueTrees.java`, `HarbingerArtificerDialogueTrees.java`, the four entity classes that build the trees (the Alchemist, Artificer and Vicar entities were edited after 2026-11-11, so build on the current files), `assets/hemomancy/lang/en_us.json`
- Tests: `HarbingerPathPermissionsTest` line 28 pins `greetingDegree(8, SILENTIUM)` at 7. Make `PlaythroughEndgameGameTests.silentiumGreetedAsArchon` required. The `dialogue_route_validation` fingerprints change only if a new event id is added.
- Docs: `docs/NPC_VOICES.md` (a Silentium note per sheet), `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/ENDGAME_PATHS.md`, `wiki/Harbinger-Path.md`.
- Depends on: T10

### T12. Possession minors (size M)

- Rulings: FX-17
- Change:
  1. The gaze skips creative and spectator possessors (`AbhorrentThoughtEntity.customServerAiStep`, or a new argument on `AbhorrentGazeRules.gazes`).
  2. `VisitationService.refusal` refuses during a Chamber visit (`ChamberVisitService.isAway`, the check the Monolith gate uses), with its own line.
  3. The two totem bursts in `PossessionEvents` use `HemoParticleData`. The count for `PossessionEvents.java` in `src/test/resources/architecture/vanilla-particle-allow-list.txt` drops from 2 to 0.
  4. The bog body scrape replaces the vanilla wax-off event (`levelEvent(3004)`) with a `HemoParticleData` poof. The axe rule is NQ-12-5.
- Files: `common/entity/mob/monster/AbhorrentThoughtEntity.java`, `common/entity/mob/monster/AbhorrentGazeRules.java`, `common/encounter/VisitationService.java`, `common/event/PossessionEvents.java`, `common/block/harbinger/plant/BogBodyBlock.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `AbhorrentGazeRulesTest`, `VisitationRulesTest`, `VanillaParticleRatchetTest`. The `visitationAndPossessionRefusals` playthrough test gains the Chamber refusal.
- Docs: `docs/ENDGAME_PATHS.md` "Proof owed and known gaps", `docs/HEMOMANCY_REFERENCE.md` section 5.6f.
- Depends on: NQ-12-5 for the axe rule only

### T13. Rename the Silent Archon armour ability (size S)

- Rulings: C-2
- Change: rename the ability once the owner picks a name (NQ-12-3). It touches the registry id `hemomancy:silent_archon_severance`, the handler method and constants, and three lang keys (`ability.hemomancy.silent_archon_severance.tooltip`, `message.hemomancy.armor_set_ability_registry.silent_severance`, `message.hemomancy.silent_archon_armor_ability_handler.silent_severance`). The mod is not live, so no alias. `HemomancyTendrilEffects.silentSeverance` is shared with the Living Sickle's spin, which is the real Severance tool. Leave that helper's name or split it. The Unstained founder cure route keeps its name.
- Files: `common/armor/ability/ArmorSetAbilityRegistry.java`, `common/armor/ability/SilentArchonArmorAbilityHandler.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `SilentArchonInputAndCombatSourceTest`, `SilentArchonIncorporealitySourceTest`, `FinalBloodlustArmorAbilitySourceTest` and `SilentSeveranceDislocationSourceTest` all read the old names. `CombatTargetingMatrixGameTests.armourSilentSeverance` is named after it. Regenerate `docs/architecture/evidence/source-text-impact-map.csv` if a test file is renamed.
- Docs: `docs/HEMOMANCY_REFERENCE.md` line 4815, `docs/ENDGAME_PATHS.md` (the Mind Broken paragraph names "the Severance" among the set's powers), `docs/LORE_REFERENCE.md` where the set is described.
- Depends on: NQ-12-3

### T14. Canon text clean-up (size S)

- Rulings: C-4, CD-3, CD-10, CD-12
- Change:
  1. C-4: delete LORE section 3.3 as a section. Move its one idea (the body stripped away, the self merged with the hive) into the Degree 8 lore paragraph of section 6.4a as the far end of Apotheos. Drop the word "DLC". Fix the table of contents and any link to 3.3.
  2. CD-3: remove `case 8` from the spine-growth switch in `FungalWhisperDialogueTrees` and delete the key `hemomancy.whisper.spine_growth.degree8.canon`. Rewrite the `coreWitnessDialogue` Javadoc: it opens after the forced return, not at a pool in the Gardens, and Silentium comes from absorbing the Memory of Vesper, not from the pruning alone.
  3. CD-10: the code is already right. Fix the three lines that still read wrong: `hemomancy.artificer.item_inquiry.monolith_imbued_cloth.line2` (no Silentium condition), the two `weight_of_frame` lines that call Silent Archon a "seventh-Degree lineage", and `tooltip.hemomancy.silent_archon_set_bonus` ("Requires the Silent Archon choice" where the handler requires Silentium).
  4. CD-12: record it as done and remove the stale note at `docs/NPC_DIALOGUE_AND_TRADE.md` line 173. Also fix the block comment in `SanguineMonolithBlock` that still says "After tier 4".
- Files: `docs/LORE_REFERENCE.md`, `common/entity/npc/dialogue/FungalWhisperDialogueTrees.java`, `assets/hemomancy/lang/en_us.json`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `common/block/harbinger/functional/SanguineMonolithBlock.java`
- Tests: any lang test or voice pin that lists the deleted key (the `tools/i18n/voice_rewrite` pins name `spine_growth.degree8`. Not checked whether a test reads them). `EnglishLiteralAllowListTest` is unaffected.
- Docs: the same files, plus `docs/ENDGAME_PATHS.md` open questions and the roll-up.
- Depends on: nothing

## Conflicts with recent work

None. Checked by modification time: after 2026-11-11 14:00 the only files under these rows that changed are the three outpost NPC entity classes (Alchemist, Artificer, Vicar) and HUD textures. Neither contradicts a keep. The new HUD gives a D8 Apotheos a gold binding and gives a Silentium nothing, which matches DS-10 staying deferred.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-12-1 | PT-74 | The shatter roll. When a D7 presses further twice, the Monolith breaks and throws out Monolith Fragments. The number is random, 5 to 8, each equally likely. The Artificer's D7 assignment, the Weight of the Frame, needs a Monolithic Cornerstone plus 1 fragment and 1 imbued cloth for its rite. A whole Monolith hands over one Cornerstone free, but a breaking or broken one gives nothing. So a player who shatters first must craft the Cornerstone (4 fragments and 2 cloths, 6 fragments in all) and then feed the rite (2 more): 8 fragments, which only one roll in four gives, and nothing warns them. What should change? | A: every shatter gives 8, no roll. B: keep 5 to 8 and add a warning to the press-again line that the stone will give nothing afterwards. C: both A and B. D: keep as built (a second Monolith makes up the shortfall). | C, because A removes the bad luck and B tells the player the free Cornerstone is about to be lost. Neither touches the 2026-11-05 rule that a breaking stone gives no Cornerstone. |
| NQ-12-2 | PT-77 | The answer was the one word "Need". The atlas unlocks Monolith Fragments, imbued cloth, the Cornerstone and the four Hallowed Residua at D5, though fragments come at D7 and Residuum at D8. Which did you mean? | A: "need to fix". Gate fragments, cloth and Cornerstone at D7 and the Residua at D8. The atlas then shows each as a veiled preview one Degree early, by its built-in rule. B: "need them". Keep all seven unlocked at D5. | A, because it matches the AGENTS.md rule for Seed, Bloom, Pome and Spine (D7 with veiled D6 previews) and the Saint D8 ruling. The same pass should look at `silent_archon_chestplate` (shown at D7, earned at D8) and the three D7 Blood Lust chests (shown at D5). |
| NQ-12-3 | C-2 | What is the Silent Archon armour ability called now? It is a burst of Monolith shards that damages nearby targets and gives them Monolithic Dislocation. Lethean words belong to the Unstained, so these stay in the Silentium and Monolith vocabulary. | A: Silent Dislocation. B: Silent Sundering. C: The Unvoicing. | A, because it says what the ability does, reuses the effect's name and pairs with Silent Slipping. "Quietus" and "Refusal" are taken (a Still Art, and the set's death refusal). |
| NQ-12-4 | PT-80 | "Respawn at their bed" can be read two ways, and RV-76 keeps the forced heart-node respawn. Which is right? | A: the node stays the respawn point. A stranded Apotheos gets a way home (the Spine, a Podium, or a button on the heart-node screen) that lands at their bed, else the Overworld spawn. B: the node stops being a respawn point. Every death sends the Apotheos to their bed, else the Overworld spawn. | A, because it keeps the 2026-10-27 ruling (a sanctum with respawn) and RV-76. T8 is planned on A. B is smaller to build, so say so if you prefer it. |
| NQ-12-5 | FX-17 | The bog body "axe rule" was never given a fix. Today the tool in hand picks the yield: an axe always scrapes adipocere, a hand or scalpel takes vivianite, and either one uses up the body until it regrows. | A: keep the rule and write it down (reference and the Alchemist's inquiry line). B: give wax its own state so one body yields both. | A, because it is already a readable rule and B adds a block state for little gain. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| PT-74 | Pick what happens to the shatter roll. | Owner | NQ-12-1 |
| PT-77 | Say which reading of "Need" is right. | Owner | NQ-12-2 |
| C-2 | Pick the new ability name. | Owner | NQ-12-3 |
| PT-80 | Confirm the reading before T8 is built. | Owner | NQ-12-4 |
| FX-17 | Rule on the bog body axe. The other three fixes do not wait. | Owner | NQ-12-5 |
| PR-4 | Play the Apotheos D8 journey end to end. | Owner | T6, T7, T8, T9 built |
| PR-5 | Play the Silentium D8 journey. | Owner | T11, T12 built |
| PR-12 | Launch `heartNodeReviewClient` and `possessionReviewClient` on a GPU. | Owner | nothing |
| PT-80 | Prove the stranded return in the Gardens. The GameTest server has no Gardens, so `heartNodeRespawnTrap` stays NOT RUN there. | Build | T8, then `heartNodeReviewClient` |
| C-8 | An Apotheos section in the Liber (heart-node, synapse, screen, Molting, Visitation). | Parked | D-1, chunk 08 |
| D-3 | A design for what a Silentium gains at D8. | Later | a design from the owner |
| DS-10 | Fungal mannequin, the Gardens digesting left gear, echo shader, JEI rendering category, hunters converging on a possessed facsimile, Silentium HUD art. | Parked | nothing |
| CN-11 | Spore load feeding manipulations and refilling blood. | Parked | nothing |
| PT-72 | Whether the lock should still count pomes across Blooms and still bind a Silence through Vesper. The ruling keeps both. Listed so the owner sees it in play. | Owner | PR-5 |
| RV rows | The kept numbers have never been seen by the owner in a client. | Owner | PR-4, PR-5, PR-12 |
