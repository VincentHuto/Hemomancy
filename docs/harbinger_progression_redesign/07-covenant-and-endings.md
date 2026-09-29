# Phase 7: founding, covenant mastery, and endings

[Index](HARBINGER_PROGRESSION_REDESIGN.md) · Depends on the earlier teaching and Chamber/scar phases.

## Outcome

D5 turns knowledge into responsibility for a community. D6 demonstrates coordinated covenant practice. D7 exceeds ordinary instruction and reaches the established choice; D8 preserves relationships while leaving ordinary teachers behind.

## Read before editing

- [Chapter certification](../../src/main/java/com/vincenthuto/hemomancy/common/mission/shared/HarbingerChapterProgression.java).
- DegreeProgression, EarlyInitiation, BloodProjectionItem, BloodlineSavedData, FoundingFaneSavedData.
- HarbingerCardinalRiteEvents and current founding, recruitment, throne, helper-assignment, and Vigil completion paths.
- sanctified_rite, archon_rite, founding and covenant recipes; current NPC recruitment restrictions.
- Existing Qliphoth, Spine projection, Archon choice, Vesper, Mycophant, and scar/armor outcome checks.
- [Current lore reference](../LORE_REFERENCE.md), reconciled against implementation and this packet. The earlier `LORE_CONSISTENCY_REVIEW.md` is not present in this checkout.

## D5 instructions

1. Use Illuminatus of the Crimson Lodge as the degree. Explain that the player is qualified to keep a Fane and perform reception duties associated with a Vicar.
2. Teach Bloodline Founding before attempting to found a Fane. The degree alone does not create a covenant; right-clicking an unsigned ledger must not become a shortcut.
3. Explain Founding Fane as a separate rite with the actual founding medium and site requirements. Preserve the bloodwell/connected-stake footprint and all existing ownership checks.
4. Keep A Covenant Written in Place tied to a valid founded bloodline and usable Fane. Preserve migration for an existing legitimate founder/site.
5. Teach recruitment after the player can meet its actual bloodline conditions. Do not promise that every outpost specialist is recruitable: current specialist roles must be checked individually, particularly the Artificer.
6. Preserve outpost services for an unrecruited Artificer. A personal Fane must not require a nonexistent Artificer recruitment route to obtain a kit or finish equipment lessons.
7. Preserve founder-led player initiation: active blood, D5+, real founded bloodline, leader identity and non-conflicting membership; bind only when the 200-tick ceremony completes.
8. Keep Hematic Succession as its own advanced optional branch. It is not a synonym for ordinary recruitment and must not become a new requirement for founding or D6.

## D6 instructions

1. Teach the Chamber rite at D6, then the Covenant Throne and Covenant Vigil using the actual authored helper conditions.
2. The Living Covenant still combines a safe Chamber-return proof, a bound Throne, and successful Vigil. Preserve already earned return evidence and require the remaining real proofs.
3. Provide a valid solo-survival helper route through existing recruitable NPCs. Do not make another human player, an optional Circus outcome, or an unavailable recruitable role mandatory.
4. Losing or interrupting the Vigil must not award success. Retrying with legitimate replacement support must remain possible.
5. Keep the Vagrant Mind investigation optional. D6 competence comes from covenant practice; discovering a rare End structure is not an unannounced Archon prerequisite.
6. Let NPCs retain their expertise while acknowledging uncertainty. Most practitioners stop at D5; a routine outpost teacher should not narrate every cosmic fact with certainty.

## D7 and D8 instructions

1. Preserve the Monolith -> Seed -> Bloom -> nine Pomes from one Bloom -> Spine -> first projection -> forced return -> response sequence.
2. Do not grant the Spine on reaching D7, reveal the response menu before the required projection returns, or complete Communion by combining unrelated Bloom counters.
3. Teach the final Scriptorium/Forge/equipment stages without replacing the revelation with another mandatory checklist.
4. Preserve owner-bound Bloom cutting/entry, the two Vesper phases, retry recovery, and verified victory before Silent Archon status or exclusive victory rewards.
5. Keep Silent Archon D7. Only Apotheosis grants D8. Preserve Mycophant consequences and the actual unlocks of that branch.
6. Differentiate NPC responses to an undecided Archon, pending refusal, proven Silent Archon, and Apotheos. Maintain valid ordinary services, recovery dialogue, and unfinished optional assignments.
7. Remove premature certainty from lower-degree dialogue. D5 may reveal the blood/mycelium connection; full sporulation and final-choice knowledge follow the established revelation sequence.
8. Keep Saints/Canon trials and Drudges explicitly deferred. Do not use them to fill a degree's remaining space.

## Acceptance

- [ ] Existing founders/sites retain certification; unaffiliated D5 players cannot skip actual founding.
- [ ] Fane rites and recruitment are completable through supported NPCs in solo survival.
- [ ] No kit/equipment dependency assumes Artificer recruitment.
- [ ] Interrupted founder reception gives neither rank nor membership; conflicting bloodlines remain unchanged.
- [ ] Throne ownership, Vigil helper survival, and Chamber-return checks are individually enforced.
- [ ] Failed/abandoned/restarted rites remain recoverable under their established policies.
- [ ] A complete Main-only journey reaches the final choice without the optional research branches.
- [ ] Both ending paths obey ownership, victory, retry, and degree rules; saved-state reload preserves the branch.
- [ ] D8 dialogue changes do not remove required recovery/services or silently force a faction/ending switch.

Use existing progression, bloodline/Fane, succession, Cardinal rite, and endgame server suites. A genuine live single-player run and targeted two-player ownership checks remain required for final acceptance.

Implementation note: Silent Archon exclusive armor death refusal, claimed-Will capacity, and the reduced Commandeer thread cost now read the canonical degree-7 `SILENT_ARCHON` path set after Vesper victory. A pending refusal does not receive them. The historical string choice is retained for old-save migration and the armor rule's compatibility overload, but no longer gates these live rewards. Focused `SilentArchonVerifiedPathTest` and an 18-test clinical GameTest run passed; a live Vesper victory/reload and the full two-ending acceptance remain open.

Implementation note: The Vicar's D5 degree hint now follows founded-bloodline and Fane chapter state, and D6 names each missing Chamber-return, Throne, and Vigil proof separately. The founding and Chamber instructions use the current rite recipes. A recruited original outpost Vicar can be manually assigned during Vigil inscription and count as a live helper; automatic helper travel remains Succession-only. The ancestral ledger now moves the actual loaded recruit instead of spawning a substitute with the same UUID. For an unloaded recruit it loads only the bounded, recorded outpost chunks and retries the exact UUID for five seconds, cancelling if the player leaves the Fane or bloodline. Focused dialogue and bloodline JUnit tests, 20 clinical GameTests (including delayed arrival and Fane-exit cancellation), and 22 Succession GameTests pass. Live cross-dimension summon, full Vigil ceremony, interruption/retry, and multiplayer ownership checks remain open.

Implementation note: Founding Fane completion now checks the heart site, another Fane's footprint or bloodwell, and the founder's registered bloodline before spending the rite medium or awarding completion. Reconsecration places the new heart before retiring the prior heart, stakes, and saved record in any dimension. Legacy chapter migration requires both a registered founder and a real Consecrated Bloodwell at the saved heart; stale records cannot certify D6. The six `journeyFoundingFane` GameTests reported no failures, including new obstructed-site, cross-dimension relocation, and migration cases. The combined `runGameTestServer` still failed on 23 other required tests; this is not an integrated pass. Live site relocation, interrupted completion, and two-player ownership remain unverified.

Implementation note: A rejected rite completion now clears remaining threats and returns the exact planted Living Staff before removing the rite. The added Fane recovery GameTest starts through the real activation path, invalidates the registered bloodline, and checks the failed-completion tick. All seven `journeyFoundingFane` tests reported no failures in the latest combined run; that run still had 25 other required failures. The focused clinical suite passed 20/20. Live interruption, full-inventory return, and restart recovery remain unverified.

Implementation note: During Vigil inscription, a bloodline player can now claim a station reserved by a missing NPC, and an explicitly recruited replacement NPC can take a slot held by an unloaded or lost NPC. Present bloodline players and loaded helpers keep their assignments until explicitly replaced by the established player-claim path. The focused clinical suite passed 20/20 with both replacement cases. Full live Vigil, multiplayer handoff, and restart retry remain unverified.

Implementation note: Rite completion now checks required helpers again before consuming a medium or awarding completion. A real-activation Covenant Vigil GameTest first proved that a removed helper still allowed success, then passed after the completion guard was added; the existing live-helper Living Covenant GameTest reported no failure. The latest combined run completed 579 tests with 22 other required failures, so it is not a green integration gate. Natural helper death during the full ordeal, live replacement/retry, and multiplayer ownership remain unverified.

Implementation note: The Apotheos recipe no longer declares a 7,000 mL lump cost that its interactive ceremony never charged. Its blood payment is through the authored 32 anchors, with one brazier offering. The degree-7 Apotheos ceremony now has explicit limits for those anchors, four support sockets, six wave options, one offering, and no required helpers instead of bypassing progression validation. The targeted policy/resource JUnit tests and all 11 recipe-codec GameTests passed. Full survival acquisition, the complete ceremony, and both ending paths still need live acceptance.

Implementation note: The Vicar, Alchemist, Artificer, and Mnemonist now acknowledge the canonical ending state when a D7+ player opens dialogue: undecided, pending refusal, proven Silent Archon, pending Apotheosis, or Apotheos. An undecided D7 greeting does not assume the Spine projection or ending choice has occurred. The response is added to the greeting without replacing service, recovery, or optional-assignment options. The Vicar and Alchemist's generic Archon and Apotheos greetings no longer claim their work is finished. `ArchonEndingDialogueTest` and the full dialogue JUnit package passed. Live dialogue presentation, save/reload state, and both complete ending paths remain unverified.

Implementation note: Ordinary unbound Qliphoth Pomes still feed and apply their effects, but no longer advance the nine-fruit Communion counter or award the Fungal Spine. Only sequential owner-bound fruit with origin and husk data counts. Blooms now save a lifecycle UUID; fruit, pending husks, per-tree counters, and Vesper attempts use that ID, so a tree rebuilt at the same site starts fresh. Old Blooms keep a marker that permits their position-keyed player progress to migrate, while new trees cannot claim it. The focused clinical server run passed all 24 GameTests, including unbound, nine-bound-fruit, replacement-tree, and Vesper phase/reload checks. The latest combined GameTest run crashed in `MortarboundGameTests` before reaching these checks, so it is not an integrated pass. Actual tree growth and picking, old-save migration in a live world, Vesper reconnect/victory, and both endings remain open for acceptance.

Implementation note: Bloom rite placement is now checked when the player starts the ceremony and again before the final medium payment. A nearby Bloom, an occupied root, or a blocked filler column refuses the rite; a failed completion leaves the Qliphoth Seed seated for a clear-site retry. The mod-loaded clinical suite passed 26/26 after the start and completion tests were added. The complete 3,600-tick ceremony, natural fruiting, and live-client feedback still need acceptance.

Implementation note: The developer D7 Communion fixture places a registered, owned Bloom without supplying fruit. The manual route waits for each ripe pome and picks it from that tree; automation accelerates the ripe state, then uses the real Bloom interaction and eating path. Completion checks require the same lifecycle ID and per-Bloom count; coordinate-only legacy fruit cannot satisfy the new fixture. Cleanup removes the root, fillers, and saved Bloom. The focused clinical suite passed 35/35, including continuation after the first ripe pome was already picked. This is server fixture coverage, not evidence for natural elapsed-time ripening or either full ending.

Implementation note: A focused server test now walks a real Bloom block interaction from each ripe husk through owner-only picking, duplicate-claim refusal, eating, and release of the pending fruit, ending in Communion and the Fungal Spine. It passed in the 28/28 clinical suite. The test prepares each ripe husk in saved state; random world-tick ripening, normal survival acquisition, and live-client feedback are still unverified. The combined suite again stopped at the unrelated Mortarbound distant-sound assertion before its Apotheos journey test.

Implementation note: The developer Apotheos journey no longer writes the revelation-choice flag during fixture preparation. It equips the Spine earned from Communion; the manual route uses its normal two-minute projection and the automation invokes the Spine before accelerating the forced return. The focused Apotheos GameTest verifies that the choice is unavailable before projection, appears only after return, and can lead through the real Rite of Apotheos activation/completion path. A separate focused test exercises the automation's Spine use, return, and choice. Degree-7 snapshot restoration now preserves numeric projection/return fields and restores the exact captured recipe book after degree synchronization, whose recipe-award side effect previously broke restore. The clinical suite passed 31/31. This is a server fixture route, not a live Gardens visit, full ceremony, or survival completion of either ending.

Implementation note: The temporary Living Sickle now records its Bloom's lifecycle UUID. A Sickle shaped for a removed tree cannot sever a replacement at identical coordinates; a newly shaped Sickle can. Pre-ID Sickles remain usable only on Blooms marked for legacy progress migration. The clinical suite passed 33/33, including both lifecycle and migration cases, and the adjacent Sickle/Bloom identity JUnit selection passed 10/10. Vesper arena entry, its two-phase fight, reconnect, victory rewards, and the sealed-tree outcome still need live acceptance.

Implementation note: A focused server interaction test now checks stranger denial, owner Sickle shaping and severing through the Bloom block, no premature Silent Archon promotion, and an open portal after the headless server cannot enter the absent Chamber dimension. Another test drives the real Bloom level-tick handler at its 40-tick cadence with a deterministic ripe roll, proving the first husk, the pending-fruit pause, and the second husk after release. The clinical suite passed 35/35. These tests do not time natural growth across a live session or exercise Chamber entry, Vesper combat, reload, or the rendered tree.

Implementation note: Vesper login and arena-tick recovery now verify that the saved attempt still belongs to the same owned, open Bloom and a Degree-7 `SILENT_PENDING` player. A removed, replaced, or sealed tree, or a changed ending path, cannot respawn an unwinnable boss. An invalid attempt clears the fight state; a player still in the Chamber returns through the saved return point, without sealing a replacement tree or granting Silent Archon. A focused GameTest validates the saved Bloom checks and outside-Chamber cleanup; the GameTest server still lacks the Chamber dimension, so actual login transport and boss recovery need live acceptance.

Implementation note: Chamber state sync now gives an active Vesper ordeal priority over the selectable Vesper floor preview, so its scene packet names the actual fight arena rather than the player's ordinary Chamber cell. After the attempt ends, a selected preview returns to its cell center. A packet-level GameTest covers both centers. Client rendering and a real Chamber reconnect still need live inspection.

Implementation note (2026-09-29): The developer Harbinger journey now accepts either response after the Communion-earned Spine projection. Its Silent branch retains the exact owned Bloom through the choice and refusal checkpoints, supplies a Living Staff only when needed, and asks the player to shape the Sickle, sever the tree, enter its portal, and defeat Vesper. Automation deliberately stops at that stage instead of awarding a simulated victory. The checkpoint requires the saved Bloom's sealed trophy state, D7 `SILENT_ARCHON`, and the Vesper victory advancement; the Apotheos branch still continues to its rite. A focused server test covers the choice, full-inventory transition retry, and checkpoint/snapshot contract, but sets the victory state directly because this GameTest server has no Chamber dimension. The actual two-phase fight, Blood Absorption finish, return, reload, and client presentation remain live acceptance work.

Implementation note (2026-09-29): A full inventory no longer turns the one-time Memory of Vesper into a world drop that can despawn or be taken. The victory flag remains pending until the item enters the owner's inventory; login and a 20-tick server retry deliver it after space opens. A focused GameTest verifies no drop, later delivery, and no duplicate. The clinical suite passed 39/39. Actual victory-to-return delivery and saved-world restart remain unverified.

Implementation note (2026-09-29): The ninth Qliphoth Pome now completes Communion even when the player's inventory is full, but it does not mark the Fungal Spine as granted or drop the only copy. The existing degree flags leave the claim pending; a 20-tick server retry inserts the Spine after space opens, then records the one-time grant. A mod-loaded test checks the full-inventory state, degree NBT round trip, later delivery, and no duplicate. The clinical suite passed 40/40. A saved-world restart and live client feedback remain unverified.

Implementation note (2026-09-29): Login migration now leaves an undelivered Spine claim pending instead of setting the grant flag. A later retry inserts it after room opens; if the Spine is already in player inventory, the retry records that item without duplicating it. The focused clinical suite passed 41/41 after red login and legacy-item regressions. An actual saved-world restart and live post-grant loss recovery remain unverified.

Implementation note (2026-09-29): A Communion owner who later loses the granted Spine can sneak-right-click their original living, nine-husk Bloom empty-handed to receive another. The block checks owner, matching Bloom lifecycle, nine consumed pomes, and an empty Spine inventory before issuing it; a stranger, held-item click, repeated click with a Spine, or rebuilt tree at the same coordinates cannot claim one. Normal interaction now gives the recovery hint. The focused clinical suite passed 42/42 after the owner-claim and held-item regressions failed first. Live click routing, world reload, and both ending paths remain unverified.

Implementation note (2026-09-29): A saved, developer-assisted Silent refusal now passed the actual three Crowned anchors, Evening Star spawn and downed state, ordinary Blood Absorption finish, overworld return, D7 `SILENT_ARCHON`, and one Memory of Vesper. Disconnect/reopen retained the path and item; saved Bloom data recorded `SEALED`, and the Vesper advancement was done. Combat used protection, supplied tools, teleporting, and `/damage`, so fair survival balance and multiplayer remain open. The earlier player death erased the dev journey snapshot. The death-copy regression passed in the 601-test combined run, and a separate live start/death/respawn/clear restored its snapshot and pre-journey position. A full D7 death-and-restore replay remains open. See `08-validation-and-handoff.md` for the exact assists and remaining checks.
