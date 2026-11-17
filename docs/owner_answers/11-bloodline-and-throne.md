# 11. Bloodlines, ledgers, the blood pool and the Covenant Throne

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (B22, 2026-11-15): `BloodlineMembership` (expel, leave, dissolve, petitions, login settlement); away members' notices in `BloodlineSavedData`; the purified-player fault fixed. |
| T2 | Built (B22): Leave on the ledger screen (two clicks) and at the Vicar; role words are keys. |
| T3 | Built (B22): removal record, `minted` stamp, the crumble; membership read from world data. |
| T4 | Built (B22): banked Unbinding share paid at login or respawn, capped by room. |
| T5 | Built (P12). |
| T6 | Built (B22): one binding rule (line's Fane), bond recorded and cleared, Trance for the Progenitor only. The Covenant Vigil has no leader-only step. |
| T7 | Built (B22): `found_bloodline`, `found_fane` or `line_has_no_fane` at D6; `bind_throne` reworded. |
| T8 | Built (B22): join gate, petitions, Admit and Refuse, two payloads, protocol 53. |
| T9 | Built (P12, B22): the Vicar's full-pool clause and two ScreenChromeReview scenes (full pool, petitions). |
| T10 | Built (B22): founder name plus custom name, `displayName()`. |
| T11 | Built (B22): `eternal_covenant_sealed` on the degree attachment; the cure clears it. |
| T12 | Built (P12, B22): every ruling recorded in its topic doc and the roll-up. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings turn the bloodline from a one-way door into a membership with rules. Joining by ledger needs D1+, active blood and the leader's answer. A member can leave. A kicked player's ledger crumbles whenever it is in their inventory. Offline members are settled at login (a line of text, a banked pool share, a cleared Throne bond). Members bind the leader's Throne, and a Throne only binds inside the binder's own line's Fane.

The largest pieces are the leader's confirm step (PT-35, new saved petitions, two new payloads, a ledger screen row) and one shared departure service that kick, leave, Disband, Unbinding, the `/hemo` disband and the two Unstained exits all call (today six places each do part of the job).

Surprises from the code:

- A ledger does not record a member. It carries a full snapshot of the line (`Bloodline.serialize()`), and `UnsignedLedgerItem.use` trusts that snapshot's member list. That is why a kicked player's ledger does nothing: the stale snapshot still lists them. A stale ledger cannot be told from a fresh invitation without a new record of who was removed and when.
- `PacketLumpDonate` takes the whole donation from the player even when the pool has no room. The blood is destroyed and the player is told it was donated. Read from the code, not run.
- The ledger's free Disband returns no pool blood to anyone. Only the Unbinding rite pays shares.
- PT-64 and PT-65 fit as one rule: the Throne must stand in the Fane of the binder's line. A Fane is keyed to the leader, so "the binder's own Fane" has to mean "the binder's line's Fane", or members could never bind.
- S-1 is not a pending step. Its scheduled part (S-1c) was done in consolidation step 12. What is left is a ratchet with no date, so PT-66 is planned here as its own small task.
- CD-11 is already fixed. The Hermit line was reworded in commit `d5a7ddbfa`.
- None of this chunk's files changed after 2026-11-11 except `PacketHandler.java` (protocol version is now `"50"`). No conflicts.

Counts: 14 rows. BUILD 13, KEEP 0, DONE 1, MOOT 0, CONFLICT 0, UNCLEAR 0, OWNER 0, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| PT-35 | A signed ledger joins only a D1+ player with active blood, and only after the leader confirms. | BUILD | `UnsignedLedgerItem.use`: the only gates are the Unstained block and "already in another line". `savedData.addMember` runs at once. Probe `PlaythroughIlluminatusGameTests.ledgerJoinHasNoGate` is `required = false`. `EarlyInitiation.bindBloodline` is a second join path (founder officiates in person). | T8 |
| PT-36 | Add a leave action for members. | BUILD | `PacketLedgerAction` has four actions (summon, recall, set point, disband). `LedgerScreen` has four buttons. No leave anywhere. Probe `bloodlineRecallAndMemberPowers` hard-codes `leaveExists = false`. | T2 |
| PT-37 | OWN. When a player is kicked, any ledger of that line in their inventory crumbles to ash and is destroyed. The same happens later if one is moved into their inventory from a chest. | BUILD | `PacketKickBloodlinePlayer.handle` does not touch ledgers. `BloodlineDisbandHelper.burnInvalidLedgerIfPresent` (called from `UnsignedLedgerItem.inventoryTick`) burns only when the line no longer exists. The ledger tag holds `state` and a `bloodline` snapshot, no holder and no date. Probe `kickedMemberStaleLedger`. | T3 |
| PT-38 | The first Unbinding warns at activation, before the Nether Star is taken. | BUILD | `CardinalRiteCompletionEffects.completeHematicUnbinding` warns at completion, after the offering is spent (`HematicUnbindingRules.decision`, 12,000 tick window, two root persistent keys). `CardinalRiteActivation.canStartFoundingFane` is the activation-time pattern to copy. Probe `hematicUnbindingCostsTwoStars`. | T5 |
| PT-42 | OWN. No "pool is full" line. Grey out the deposit control when the pool is full. | BUILD | `BloodlinePoolScreen.donateButton` is greyed only when the player has no line. The client knows the fill: `BloodlinePoolClientData.getPoolVolume/getPoolMax`, fed by `PacketSyncBloodlinePool` at login, on screen open and on every pool change. The probe `fullPoolDepositSaysNothing` uses Projection on the Bloodwell, which has no control to grey. | T9 |
| PT-44 | An offline member's share of a dissolved pool is banked and paid at login. | BUILD | `completeHematicUnbinding` pays `poolBlood / memberCount` only to members found by `getPlayerList().getPlayer`. Nothing is stored for the rest. Probe `hematicUnbindingOfflineShare`. | T4 |
| PT-64 | A member may bind the leader's Throne. | BUILD | `CovenantThroneBlock.handleInteraction` refuses anyone who fails `isProgenitor` (active blood and leader of a valid line). Probe `PlaythroughSanctifiedGameTests.memberCannotBindThrone`. | T6 |
| PT-65 | A Throne binds only inside the binder's own Fane (read as the Fane of the binder's line). | BUILD | `CovenantThroneBlock` has no Fane check at all. `CovenantThroneBlockEntity` stores no owner. `ConsecratedBloodwellBlock.isInOwnFane` already resolves "own Fane" as the line leader's Fane. Probe `anyProgenitorBindsAnyThrone`. | T6 |
| PT-66 | Eternal Covenant moves out of root persistent data so it survives death and stays sealed. | BUILD | `MaxBloodLedger.ETERNAL_COVENANT_TAG` is read with `getPersistentData()` in `MaxBloodLedger.collect` and twice in `CardinalRiteCompletionEffects`. `persistent-data-allow-list.txt` still lists both files under "not yet moved (S-1)". S-1 has no remaining scheduled step (`docs/architecture/CONSOLIDATION_PROGRESS.md`: S-1c done, step 12). Probe `eternalCovenantAcrossDeath`. | T11 |
| PT-69 | A lineless D6 is told to found a line first, not to bind a Throne. | BUILD | `CovenantProgress.steps(6)` ignores `inBloodline` and returns `chamber_return`, `bind_throne`, `complete_vigil`. The Vicar and the Monolith both read it. Probe `linelessD6VicarGuidance` accepts the existing `found_bloodline` key. | T7 |
| PT-70 | When a line is dissolved, the progenitor's Throne bond is cleared along with the Fane. | BUILD | `PacketLedgerAction.handleDisbandBloodline` removes Fanes and resets members but leaves the `covenant_throne_bound` advancement and the respawn point set by `CovenantThroneBlock.claimRespawn`. No advancement revoke helper exists in `HarbingerAdvancementGranter`. Probe `disbandedProgenitorThrone`. | T6 |
| PT-87 | The default bloodline name becomes translatable. | BUILD | `CardinalRiteCompletionEffects.presignBloodlineLedger` line 991: `caster.getName().getString() + "'s Blood Line"`, saved to world data and to the ledger. `english-literal-allow-list.txt` has an entry for it. Probe `PlaythroughLateNpcUsabilityGameTests.bloodlineNameIsEnglish`. | T10 |
| PT-90 | Members get a leave action, so no member is stuck below D7. | BUILD | Same gap as PT-36. With PT-64 a member can also reach D7 without leaving. Probe `memberRouteToD7` hard-codes `leave = false`. | T2 |
| CD-11 | Check the Hermit's `illuminatus.hint` against the gate and reword if wrong. | DONE | `en_us.json` `hemomancy.hermit.illuminatus.hint` now reads "Raise a Fane of your own, or stand in your line's Fane and give your own blood to its Bloodwell." The old "Find allies who share your blood" text survives only under `tools/i18n/voice_rewrite/`. The gate is the chapter `COVENANT_WRITTEN_IN_PLACE` (`CardinalRiteActivation` line 164, `HarbingerChapterProgression`). It needs a line (your own Fane, or a deposit in your line's Fane) but not allies. A solo founder passes. The new text matches. One stale note remains in `docs/NPC_DIALOGUE_AND_TRADE.md` line 171. | T12 |

## Plan

### T1. One departure service and a login settlement (size M)

- Rulings: groundwork for PT-36, PT-37, PT-44, PT-70, PT-90
- Change: add a `BloodlineMembership` service (new class beside `Bloodline.java`) with `expel`, `leave` and `dissolve`. Each one updates `BloodlineSavedData`, clears the line on the member's attachment through `HemoCapabilityAccess`, ends in `BloodVolumeEvents.syncVolume`, and sends the notices. Move the six existing call sites onto it: `PacketKickBloodlinePlayer.handle`, `PacketLedgerAction.handleDisbandBloodline`, `completeHematicUnbinding`, `BloodlineSubcommand.disbandBloodline`, `UnstainedCardinalRiteEvents.completeSeveredCovenant`, `PathMutualExclusionHelper.completeUnstainedCure`. For offline members, `BloodlineSavedData` gains a per-player pending list that outlives the line: kind (expelled, dissolved, left, admitted, refused), line name, who did it, blood owed, clear-Throne flag. An offline player's attachment cannot be written, so this has to be world data. Settle it in `BloodVolumeEvents.playerLoggedIn`, which already re-associates the line, and again at respawn if the player logged in dead (the `PendingDelivery` living-player rule). This task changes one thing a player sees: a member kicked or dissolved while offline is told at login. Today they are told nothing. Keep these new records out of `Bloodline.serialize()`, because that tag is also written onto every ledger. Whether it is also sent to clients inside `BloodVolumeServerPacket` was not checked.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineSavedData.java`, `BloodlineDisbandHelper.java`, `BloodVolumeEvents.java` (same folder), `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketKickBloodlinePlayer.java`, `PacketLedgerAction.java`, `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteCompletionEffects.java`, `src/main/java/com/vincenthuto/hemomancy/common/command/BloodlineSubcommand.java`, `src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java`, `src/main/java/com/vincenthuto/hemomancy/common/capability/PathMutualExclusionHelper.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: new JVM test for the pending list's NBT round trip. `PlaythroughIlluminatusGameTests.kickAndRelog` gains "the offline kicked member is told at login". `BloodlineDisbandFaneSourceTest` and `NetworkPackageGuardrailTest` stay green (the two payloads now call one service method each). `hemo_command_validation` fingerprints checked.
- Docs: `docs/HEMOMANCY_REFERENCE.md` bloodline section (around line 3499, which also still says the first use of an unsigned ledger founds a line, while the code says only the Founding rite does)
- Depends on: nothing

### T2. Leave action (size M)

- Rulings: PT-36, PT-90
- Change: `PacketLedgerAction` gets `ACTION_LEAVE_BLOODLINE = 4`, members only, routed to `BloodlineMembership.leave`. `LedgerScreen` shows a Leave button where the leader sees Disband, with a second click to confirm. Leaving removes the member, clamps the pool (as `Bloodline.removeMember` already does), tells the line, and applies the PT-37 ledger rule and the PT-70 Throne rule to the leaver. The leaver takes no pool share (NQ-11-4). The leader cannot leave (NQ-11-6). A leaver keeps every proof already earned. From there the road to D7 is the one `formerMemberReachesTheThrone` already plays for a kicked member: found a line, raise a Fane, bind a Throne. Members below D5 have no Ancestral Ledger (Bloodline Recall is D5), so add a Vicar dialogue option for them in `VicarDialogueEvents` (NQ-11-5). The action is an int inside an existing payload, so the layout does not change.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketLedgerAction.java`, `src/main/java/com/vincenthuto/hemomancy/client/screen/item/LedgerScreen.java`, `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/route/VicarDialogueEvents.java`, `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`, `en_us.json`
- Tests: make `bloodlineRecallAndMemberPowers` and `memberRouteToD7` required and play the leave. `DialogueRoutingGuardrailTest` and the `dialogue_route_validation` fingerprints for the new Vicar route. `BloodlineScreenRegressionSourceTest`. `LedgerScreen` also holds the literals "Leader" and "Member", which should become keys while the file is open (`EnglishLiteralAllowListTest` count).
- Docs: `docs/ENDGAME_PATHS.md` and `docs/PLAYTHROUGH_TESTS.md` (D-36, D-90 move to rulings), `docs/NPC_VOICES.md` sheet for the Vicar line, `wiki/Harbinger-Path.md`
- Depends on: T1

### T3. A removed player's ledger crumbles (size M)

- Rulings: PT-37
- Change: record removals. `BloodlineSavedData` keeps, per line, a map of removed player to the game time of removal. Kick writes it (and leave, see NQ-11-3). Each ledger gets a `minted` game time, written by `presignBloodlineLedger` and `recallBloodlineLedger`. The check lives where the dissolved-line burn already lives, `BloodlineDisbandHelper.burnInvalidLedgerIfPresent`, called every tick from `UnsignedLedgerItem.inventoryTick`: a signed ledger whose line exists, whose holder is in that line's removed map, and whose `minted` is not later than the removal, is destroyed with the ash line. That one check covers both of the owner's cases, in the inventory at the moment of the kick and moved in from a chest later. An offline kicked player needs nothing extra: T1 tells them at login and the first inventory tick crumbles the ledger. Also call the check from `use` so a ledger used from the cursor path cannot slip by. Stop reading members from the item: `use` asks `BloodlineSavedData` who is a member. Today a member who right-clicks an older copy of their own line's ledger is "joined" again, with the message and the member notice repeated. The effect is one translatable line, a sound and a small custom mote burst through `HemoParticleData` (no vanilla particle, `VanillaParticleRatchetTest`). No item is left behind (NQ-11-2). No migration: a ledger with no stamp reads as minted at 0.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/bloodline/UnsignedLedgerItem.java`, `BloodlineDisbandHelper.java`, `BloodlineSavedData.java`, `CardinalRiteCompletionEffects.java` (the two ledger writers), `en_us.json`
- Tests: make `kickedMemberStaleLedger` required and assert the ledger is gone and the ash line was said. Add GameTest cases: ledger taken from a chest after the kick crumbles, a third player's copy does not, a ledger minted after the kick does not, an offline kicked player's ledger crumbles on the first tick after login. JVM test for the stale rule as a pure function (line id, removed time, minted time).
- Docs: `docs/PLAYTHROUGH_TESTS.md` (D-37), `docs/HEMOMANCY_REFERENCE.md` bloodline section
- Depends on: T1. The stamp half depends on NQ-11-1.

### T4. Bank an offline member's pool share (size S)

- Rulings: PT-44
- Change: in `BloodlineMembership.dissolve` for the Unbinding, every player member is owed `poolBlood / memberCount` (the number as built). Online members are paid now, as today. Offline members get a pending entry with the amount, and T1's login settlement pays it into their volume as income and says the line's name and the amount. Record it to `BloodFlowLedger` so the diagnostics show where the blood came from. Payment is capped by room in the vessel, the same as for online members (NQ-11-10). The free Disband still pays nobody (NQ-11-9).
- Files: `CardinalRiteCompletionEffects.java`, `BloodlineSavedData.java`, `BloodVolumeEvents.java`, `en_us.json`
- Tests: make `hematicUnbindingOfflineShare` required. Add a case for a member who logs in dead.
- Docs: `docs/CARDINAL_RITES.md` (D-44 to rulings), `docs/PLAYTHROUGH_TESTS.md`
- Depends on: T1

### T5. Unbinding warns at activation (size S)

- Rulings: PT-38
- Change: move the two-step from completion to activation, the way `canStartFoundingFane` does it. The first activation by the leader says the warning, takes no offering and starts nothing. A second activation within `HematicUnbindingRules.CONFIRMATION_TICKS` (12,000) starts the rite, and its completion dissolves the line with no further warning. One Nether Star in total. Hold the pending window in a session map keyed by player UUID and cleared through `TransientServerStateEvents`, not in root persistent data. That removes three `getPersistentData()` calls from `CardinalRiteCompletionEffects`. Reword `message.hemomancy.cardinal_rite_completion_effects.the_covenant_loosens_but_holds_performing` for the new moment.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteActivation.java`, `CardinalRiteCompletionEffects.java`, `HematicUnbindingRules.java`, `src/main/java/com/vincenthuto/hemomancy/common/event/TransientServerStateEvents.java`, `en_us.json`, `src/test/resources/architecture/persistent-data-allow-list.txt`
- Tests: make `hematicUnbindingCostsTwoStars` required (rename it, it now costs one). `hematicUnbindingOfflineShare` arranges the old persistent keys by hand and must arrange the new window instead. `HematicUnbindingRulesTest`, `PersistentDataAllowListTest`, `CooldownTierGuardrailTest`, the `rite_kind_validation` GameTests.
- Docs: `docs/CARDINAL_RITES.md` (D-38 to rulings), `docs/HEMOMANCY_REFERENCE.md` line 5304 ("Two-performance irreversible confirmation")
- Depends on: nothing. Touches the same function as T1 and T4, so build after them.

### T6. Throne binding rule and clearing the bond (size M)

- Rulings: PT-64, PT-65, PT-70
- Change: replace `CovenantThroneBlock.isProgenitor` at the gate with one rule: the player has active blood, belongs to a valid line, and the Throne's position is inside that line's Fane (`FoundingFaneSavedData.isWithinFane(leaderUuid, thronePos)`). A member binds the leader's Throne because it stands in their line's Fane. Another line's progenitor is refused because it does not. A leader with no Fane is refused too. Split the one refusal line into two keys (no line, and not in your line's Fane). The Covenant Trance stays with the Progenitor (NQ-11-11). Record the bound Throne's position and dimension on the player (a new field on the degree attachment) so the bond can be cleared exactly. Add `HarbingerAdvancementGranter.revoke`. On dissolve, leave and kick, `BloodlineMembership` clears the bond for each affected player: revoke `covenant_throne_bound`, and reset the respawn point if it is still the Throne's. Offline players get it through T1's login settlement. A completed `living_covenant_complete` is kept (NQ-11-12). The Disband and Unbinding messages name the lost Throne bond.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/CovenantThroneBlock.java`, `src/main/java/com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java`, `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/InitiatoryDegree.java`, `IInitiatoryDegree.java`, `en_us.json`
- Tests: make `memberCannotBindThrone` (rename), `anyProgenitorBindsAnyThrone` (rename) and `disbandedProgenitorThrone` required. Check `livingCovenantByFinalThrone` and `formerMemberReachesTheThrone` still place their Thrones inside the Fane. `CovenantThroneSpectacleSourceTest`. `AttachmentAccessTest` for the new field's accessor.
- Docs: `docs/ENDGAME_PATHS.md` (D-64, D-65, D-70 to rulings), `docs/HEMOMANCY_REFERENCE.md` line 5196 ("Only the active bloodline Progenitor can bind to it"), `wiki/Harbinger-Path.md`
- Depends on: T1, T2

### T7. Covenant counsel for a lineless D6 (size S)

- Rulings: PT-69
- Change: `CovenantProgress` gains a live "the line has a Fane" flag. At D6, when the Throne is not bound: no line gives `found_bloodline`, a led line with no Fane gives `found_fane`, otherwise `bind_throne`. `chamber_return` and `complete_vigil` are unchanged. The Vicar and the Monolith both read this list, so both change. The existing `found_bloodline` and `found_fane` lines are written for D5. Read them at D6 and add a D6 wording if they jar. After T6, reword `hemomancy.vicar.covenant.bind_throne` and `hemomancy.monolith.counsel.bind_throne` so a member hears "your line's Fane". A member whose line has no Fane needs one new line ("your line has no Fane yet").
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CovenantProgress.java`, `HarbingerVicarDialogueTrees.java`, `SanguineMonolithDialogueTrees.java` (same folder), `en_us.json`
- Tests: make `linelessD6VicarGuidance` required. `VicarCovenantGuidanceTest`, `SanguineMonolithDialogueTreesTest`.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (D-69 to rulings), `docs/NPC_VOICES.md` sheets for the Vicar and the Monolith
- Depends on: T6 for the wording

### T8. Join gate and the leader's confirm (size L)

- Rulings: PT-35
- Change: `UnsignedLedgerItem.use` by a non-member checks `ProgressionGates.hasDegree(player, 1)` and `volume.isActive()`, each with its own refusal line. If they pass, it writes a petition (player id and name) on the line in `BloodlineSavedData` and tells the applicant their name waits for the Progenitor. Nobody is added yet. Leader online: a chat line names the applicant. Leader offline: the petition waits in saved data and the leader is told the count at login. The leader answers on the ledger screen, with a row to step through petitions and Admit and Refuse buttons (NQ-11-7). Two new payloads: clientbound petition list (sent to the leader at login, on a new petition, on screen open) and serverbound answer (applicant id, admit or refuse). The answer handler checks `ServerboundGuards.heldItem` for the ledger, checks leadership, goes through `ServerboundRateLimiter`, then calls one service method. Admit re-checks the gates. Applicant online: joined now, with the existing join message, `SkillPointGainEvents.onBloodlineJoined` and the member notices. Applicant offline: membership is written, and the message and milestone wait in T1's pending list for login. Refuse tells the applicant now or at login. A petition is dropped when the applicant joins any line, takes the Unstained path, or the line ends. A cap of 16 waiting petitions per line is my number. The founder-officiated initiation in `EarlyInitiation` is left as built (NQ-11-8). Bump `PacketHandler.PROTOCOL_VERSION` from `"50"` and rewrite `payload-registry.txt`.
- Files: `UnsignedLedgerItem.java`, `BloodlineSavedData.java`, `LedgerScreen.java`, `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`, two new payload classes under `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/`, `src/main/java/com/vincenthuto/hemomancy/client/network/ClientboundStateHandlers.java`, `en_us.json`, `src/test/resources/network/payload-registry.txt`
- Tests: make `ledgerJoinHasNoGate` required (rename). New GameTests: admit with both online, admit with the applicant offline, petition while the leader is offline, refuse, a forged answer from a member. The playthrough tests that join by ledger (the `useLedger` callers in `PlaythroughIlluminatusGameTests`, and `bloodlineNameIsEnglish`) expect an instant join and need an admit step. `ServerboundGuardsCoverageTest` entry for the new serverbound payload. `ScreenChromeGuardrailTest` for the new row.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (D-35 to rulings), `docs/HEMOMANCY_REFERENCE.md` line 3500 ("Another player uses the same signed ledger to join"), `wiki/Harbinger-Path.md`
- Depends on: T1, T3

### T9. Pool-full deposit control (size S)

- Rulings: PT-42
- Change: `BloodlinePoolScreen` sets `donateButton.active` each frame from `BloodlinePoolClientData` (inactive when volume is at or above max), next to where it already sets the banish buttons. No chat line. On the server, `PacketLumpDonate` clamps the donation to the pool's room before it pays, and does nothing at zero room. That closes the overpay found in this review. Projection on the Bloodwell keeps its as-built behaviour: nothing is taken and nothing is said, because there is no control to grey. The zero-blood half of the probe is also left as built.
- Files: `src/main/java/com/vincenthuto/hemomancy/client/screen/item/BloodlinePoolScreen.java`, `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketLumpDonate.java`, `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeTransferRules.java`
- Tests: `BloodVolumeTransferRulesTest` gains the room clamp. Rewrite `fullPoolDepositSaysNothing`: a full pool takes no blood and earns no proof, and a lump donation into a full pool costs the player nothing. A GameTest cannot see the greyed button, so the owner checks that in a client.
- Docs: `docs/PLAYTHROUGH_TESTS.md` (D-42)
- Depends on: nothing

### T10. Translatable default line name (size M)

- Rulings: PT-87
- Change: a line stores its founder's name and an optional custom name. `Bloodline` gains `displayName()` returning a `Component`: the custom name if one is set, otherwise `Component.translatable("hemomancy.bloodline.default_name", founderName)`. About fifteen callers pass `getName()` into messages or `Component.literal` (ledger tooltip, `LedgerScreen`, `BloodlinePoolScreen`, `EarlyInitiation`, `CardinalRiteCompletionEffects`, `BloodlineSubcommand`). They pass the component instead. The `"No Bloodline"` literal in `Bloodline.java` becomes a key too. No rename feature exists today, so the custom name is an empty slot. No migration.
- Files: `Bloodline.java`, `CardinalRiteCompletionEffects.java`, `UnsignedLedgerItem.java`, `LedgerScreen.java`, `BloodlinePoolScreen.java`, `src/main/java/com/vincenthuto/hemomancy/common/mission/vicar/EarlyInitiation.java`, `BloodlineSubcommand.java`, `en_us.json`, `src/test/resources/architecture/english-literal-allow-list.txt` (two entries go)
- Tests: make `bloodlineNameIsEnglish` required. It asserts the stored name is itself a lang key, so rewrite it to the new shape. `PlaythroughStates.lineName` builds the old English string. `HardCodedEnglishGuardrailTest`, `EnglishLiteralAllowListTest`, the `hemo_command_validation` fingerprints (the allow-list note says they record the name).
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (D-87 to rulings)
- Depends on: T3 (both change what a ledger stores)

### T11. Eternal Covenant into the degree attachment (size S)

- Rulings: PT-66
- Change: a new boolean on `InitiatoryDegree` (key `eternal_covenant_sealed`, beside `hematic_fortification`, which is the same kind of flag). `MaxBloodLedger.collect` and the two rite sites read and write it through `HemoCapabilityAccess`. Delete `ETERNAL_COVENANT_TAG`. No migration. Decide what the Unstained cure does to it (NQ-11-14).
- Files: `InitiatoryDegree.java`, `IInitiatoryDegree.java`, `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/MaxBloodLedger.java`, `CardinalRiteCompletionEffects.java`, `src/test/resources/architecture/persistent-data-allow-list.txt` (the `MaxBloodLedger` row goes, and with T5 the `CardinalRiteCompletionEffects` row goes from 5 to 0)
- Tests: make `eternalCovenantAcrossDeath` required (it arranges the root key and must arrange the attachment). `MaxBloodRulesTest`, `PersistentDataAllowListTest`, `AttachmentAccessTest`.
- Docs: `docs/CARDINAL_RITES.md` (D-66 to rulings), `docs/architecture/CONSOLIDATION_PROGRESS.md` (the call count)
- Depends on: nothing

### T12. Records and roll-up (size S)

- Rulings: all rows, CD-11
- Change: move the thirteen PT rows from "Open questions" to "Rulings that bind" in their topic docs and in the roll-up. Remove the stale CD-11 note (`hermit.md` ... unchecked) from `docs/NPC_DIALOGUE_AND_TRADE.md` line 171. Regenerate the source-text impact map with `python gradle/source-text-impact-map.py`.
- Files: `docs/CARDINAL_RITES.md`, `docs/ENDGAME_PATHS.md`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/PLAYTHROUGH_TESTS.md`, `docs/OPEN_QUESTIONS.md`, `docs/HEMOMANCY_REFERENCE.md`, `wiki/Harbinger-Path.md`, `wiki/Blood-Systems.md`
- Tests: none
- Docs: as listed
- Depends on: T1 to T11

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-11-1 | PT-37 | Can a kicked player ever come back to that line? The answer decides which ledgers crumble. | A: every ledger of that line crumbles for them, for good. B: only ledgers written before the kick crumble. A ledger minted after it (Bloodline Recall) is a fresh invitation and still needs the leader's confirm. C: as A, plus a pardon button for the leader. | B, because "old ledger" then has a plain meaning, and PT-35 already puts the leader in control of re-entry. |
| NQ-11-2 | PT-37 | What does "crumbles to ash" leave behind? | A: nothing. One line, a sound and a small custom mote burst. B: an ash item drops. | A. The mod's ash items (Packed Ash, Smouldering Ash, Befouling Ash) belong to other systems. |
| NQ-11-3 | PT-36, PT-37 | Does a member who leaves by choice lose their ledgers the same way? | A: yes, the same rule as a kick. B: the leaver keeps the ledger as an open invitation back. | A, one rule for every departure. |
| NQ-11-4 | PT-36 | Does a leaver take a share of the pool? | A: no share, as with a kick today. B: `pool / members` is paid out on leaving. | A. A member can already draw from the Bloodwell before leaving. |
| NQ-11-5 | PT-36, PT-90 | Where does a member with no Ancestral Ledger leave? Initiates brought in by a founder at D1 have none, and Bloodline Recall is a D5 rite. | A: the ledger screen, plus a Vicar dialogue option. B: the ledger screen only. C: the pool screen as well. | A. With B the lowest members stay stuck. |
| NQ-11-6 | PT-36 | Can the leader leave? | A: no. Disband or Unbinding only, and the button says so. B: leadership passes to the longest-standing member. | A, as built. |
| NQ-11-7 | PT-35 | Where does the leader answer a petition, and how long does it wait? | A: a row on the ledger screen. It waits until answered, at most 16 per line. B: on the pool screen (needs a Bloodwell). C: petitions expire after a set time. | A. The ledger opens anywhere and a line may have no Fane yet. |
| NQ-11-8 | PT-35 | The founder-officiated initiation binds a D0 player to the line as they become D1, with no ledger. Does it need the confirm step too? | A: no. Officiating in person is the leader's consent. B: yes. | A. |
| NQ-11-9 | PT-38, PT-44 | The ledger's free Disband returns no pool blood to anyone. Only the Unbinding rite pays shares. Keep that? | A: keep. The share is what the Nether Star buys, and the Disband tooltip says the pool is lost. B: Disband pays shares too, online and banked. | A. It gives the rite a reason to exist beside the free button. |
| NQ-11-10 | PT-44 | A share is capped by room in the member's vessel, online or at login. What happens to the part that does not fit? | A: lost, and the line says how much was paid. B: it stays owed and trickles in as room opens. | A, the same for online and offline members. B makes a free blood store. |
| NQ-11-11 | PT-64 | Does a member who binds the leader's Throne also get the Covenant Trance? The cooldown is stored on the Throne, not on the player. | A: no. Members get the bond and the respawn point. B: yes, sharing the Throne's cooldown. C: yes, with a cooldown per player. | A, the smallest change. |
| NQ-11-12 | PT-70 | How far does "clear it" go? | A: clear `covenant_throne_bound` and the Throne respawn point when the line ends, and for a member who leaves or is kicked. Keep a Living Covenant that was already complete. A Fane that is broken or moved while the line lives clears nothing. B: as A, and a broken or moved Fane clears every bond to its Throne. C: as A, and also revoke a completed Living Covenant. | A. Earned chapters are kept elsewhere (`covenant_written_in_place` survives a Disband today). |
| NQ-11-13 | PT-42 | With no line and no counted attempt, a D5 member cannot earn the covenant proof by deposit while the pool is full. They have to draw some out first. Accept that? | A: accept. The Vicar's `found_fane` line gains a clause about a full pool. B: a refused deposit into a full pool counts for the proof. | A. |
| NQ-11-14 | PT-66 | Once the seal lives in the degree attachment, does the Unstained cure clear it? Today nothing clears the root key. | A: the cure clears it with the rest of the Harbinger progress, and the rite can be done again after a return. B: it is kept for good. | A, in line with the reset rules. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| PT-37 | Rule on NQ-11-1, NQ-11-2 and NQ-11-3. | Owner | nothing |
| PT-37 | Ledgers in chests, item frames and on the ground are never checked until they enter the removed player's inventory. That is the owner's rule. Ledgers inside a shulker box or bundle in the inventory, and in an ender chest, are not ticked and would survive. | Later | T3 built |
| PT-37 | A third player holding a copy is untouched. A ledger does not belong to anyone, so their copy stays a valid invitation. | Later | NQ-11-1 |
| PT-36, PT-90 | Rule on NQ-11-4, NQ-11-5 and NQ-11-6. | Owner | nothing |
| PT-35 | Rule on NQ-11-7 and NQ-11-8. | Owner | nothing |
| PT-35 | Look at the petition row on the ledger screen in a client. | Owner | T8 built |
| PT-38, PT-44 | Rule on NQ-11-9 and NQ-11-10. | Owner | nothing |
| PT-42 | Rule on NQ-11-13. See the greyed Donate button in a client (no test can). | Owner | T9 built |
| PT-64, PT-65, PT-70 | Rule on NQ-11-11 and NQ-11-12. Confirm "own Fane" means the Fane of the binder's line. | Owner | nothing |
| PT-66 | Rule on NQ-11-14. | Owner | nothing |
| PT-35 to PT-90 | Thirteen probes are still `required = false` and fail on purpose. Each flips to required with its task. | Build | T2 to T11 |
| CD-11 | Remove the stale "unchecked" note. | Build | T12 |
| (found in passing) | `BloodVolumeEvents.playerTick` clears the line on a purified player's attachment every tick but does not remove them from the saved line, so login puts it back. Read only, not run. Outside these rows. | Later | nothing |
| (found in passing) | Whether the Covenant Vigil has a leader-only step for members was checked only in `completeCovenantVigil` (none there). The ally service was grepped, not read. | Later | T6 built |
