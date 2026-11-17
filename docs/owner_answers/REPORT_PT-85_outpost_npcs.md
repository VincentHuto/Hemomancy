# PT-85: outpost NPCs and the ledger summon (report, 2026-11-14)

## Summary
1. Recruiting an outpost Vicar, Alchemist or Mnemonist copies nothing and makes nothing new. It adds the real outpost NPC's UUID to the line. The ledger's Summon then teleports that same entity into the Fane, and nothing sends it back.
2. This is a leftover. The Succession spec and the wiki already say originals stay at the outpost and never take rite stations. The code never caught up: the summon, the helper check and a one-shot "return home" flag still let an original be carried off.
3. The path is still live. The Vicar's and the Monolith's Covenant Vigil lines tell the player to summon a recruited Vicar, and six playthrough probes use a summoned original as a rite helper.
4. Recommendation (option A): keep the pledge as a pool bond only. Make the summon and every rite station successor-only, and send any displaced original home every time. The planned "send home after use" fix and NQ-10-5 then go away.
5. The catch: a solo player's D6 Covenant Vigil and Archon rite each need one helper. Under A, a solo player must make a successor at D5 first. Say if you want that gate. If not, pick option C.

## 1. What the code does today
- **Recruit.** `RecruitmentDialogueEvents.handleRecruitHarbinger` handles the `recruit_harbinger` event. The Vicar, Alchemist and Mnemonist trees offer it, at D5 only. It calls `BloodlineSavedData.addNpcMember(line, entity.getUUID(), type, outpostKey)`, so the outpost NPC itself becomes a line member. `Bloodline.addNpcMember` gives it 1,000 mL of pool capacity and a 1,000 mL rite reserve. The limits (`hasNpcMemberType`, `hasNpcMemberOutpost`) are checked per line, so a second line can pledge the same NPC. Nothing is copied or spawned.
- **Ledger summon.** `PacketLedgerAction.handleNpcSummon` runs from the ledger screen's Summon button (`LedgerScreen`). It loops over `getNpcMemberUUIDs()`. `teleportNpc` force-loads the recorded outpost (`loadRecordedOutpost`, a chunk ticket). `moveLoadedRecruit` then moves the outpost's own entity to the player with `teleportTo` or `changeDimension`. No home is stored and no return is queued. Only `HarbingerVicarEntity` keeps an `outpostHome` (set by `HarbingerOutpostStructure`), and it is only a `restrictTo` radius of 12.
- **The only way back.** `SuccessionResidents.returnOriginalHome` runs from `ProfessionalHarbingerEntity`'s tick for non-successors and moves an original back to its outpost. The first time it finds the NPC at home it sets `SuccessionHomeChecked`, and it never clears that flag. So an NPC that has stood at its outpost once is never returned after a later summon. This is why `summonStripsTheOutpost` (PT-85, in `PlaythroughLateNpcUsabilityGameTests`) finds no Vicar for the D0 newcomer.
- **Rite stations.** `CardinalRiteAllyService.eligibleNpcHelper` returns true for any line NPC member that is not a successor (`!npc.isSuccessor() || SuccessionResidents.helper(npc)`). So a summoned original can be given a station through `tryAssignNpc`, which `CardinalRiteInteractionHandler` calls. When the rite ends, `returnNpcAlliesToFane` only stops an original's navigation, so it stays in the Fane.
- **Succession, for contrast.**
  - `SuccessionDialogue.handle` records a Bequest and fills donor vials from the original, which stays where it is.
  - The Hematic Succession rite makes a new entity with its own UUID, a `SuccessorRecord` in `SuccessionSavedData` and a Fane workplace. `SuccessionResidents.join` adds it to the line through `Bloodline.addSuccessorMember`.
  - `CardinalRiteNpcTravel.gather` brings successors to stations, and `returnHome` walks them back to their workplace.
  - Mnemonic Restoration (the `mnemonic_restoration` rite, with a Vacant Effigy and a Bound Mnemonic Remnant) rebuilds the same successor.
  - `SuccessionDialogue.decorate` already removes the recruit and expel options from successors.

## 2. Entry points that still take an outpost NPC
1. Ledger Summon: the `LedgerScreen` button, `PacketLedgerAction.ACTION_SUMMON_NPCS`, `handleNpcSummon`, and its retry in `PacketLedgerAction.onServerTick` (`PENDING_SUMMONS`).
2. Station assignment of a summoned original: `CardinalRiteAllyService.tryAssignNpc` through `eligibleNpcHelper`. After that, `directToStation` can `teleportTo` the station (its `RECALL` case).
3. The missing return: `returnNpcAlliesToFane` and the one-shot `SuccessionHomeChecked` flag.
4. Teaching that sends players this way: `hemomancy.vicar.covenant.complete_vigil` and `hemomancy.monolith.counsel.complete_vigil` in `en_us.json` both say "summon a recruited Vicar to the Fane".

The pledge itself (`recruit_harbinger`) moves nothing. It feeds four things: the pool, the stake budget (`HematicStakeBlock.configuredStakeBudget`), the rite reserve, and the rule that lets Projection pass through an ally (`BloodProjectionInteractionEvents.prefersProjectionOverConversation`).

Probes that summon an original as a helper: `PlaythroughIlluminatusGameTests` (lines 697 to 709 and 1593), `PlaythroughArchonGameTests` (251), `PlaythroughSanctifiedGameTests` (296, 1237), `PlaythroughEndgameGameTests` (763) and `PlaythroughLateNpcUsabilityGameTests` (1703, 2146).

## 3. What the lore and the references say
- `docs/HEMATIC_SUCCESSION_DESIGN_SPEC.md` line 9: outpost NPCs "must remain persistent teachers ... rather than exhaustible resources that players abduct". Line 309: originals keep "ordinary recruitment and pool benefits but do not take physical rite-helper roles", and relocated originals are sent home.
- `wiki/Harbinger-Path.md` line 660: Succession works "without moving original teachers". Successors give rite help and originals give pool benefits.
- `docs/NPC_VOICES.md` line 600: "The original teacher stays at the outpost."
- `docs/LORE_REFERENCE.md`:
  - Lines 54 and 301 frame the pledge as a doctrine of propagation. An outpost offers one voice, and "the outpost does not empty itself into that one bloodline".
  - The "Manifested successors" section (line 1382) says a successor carries the profession "without becoming the original teacher".
- `docs/HEMOMANCY_REFERENCE.md` is the stale part. Line 3559 still lists "Summoning recruited NPCs" as a ledger action. Line 3561 still describes recruits as phantom members that add 1,000 mL.

All of these agree with you: an original is pledged, never moved. Bloodline NPCs with bodies come from Succession.

## 4. Recommendation and options
**Option A (recommended): the pledge stays, and bodies come only from Succession. Size M.**
- The summon moves successors only (`SuccessionResidents.helper`) and never loads an outpost. Remove `loadRecordedOutpost` and `RECRUIT_LOOKUP_TICKET`, and rename the button to call the line's residents.
- `eligibleNpcHelper` accepts successors only, as spec line 309 says.
- `returnOriginalHome` runs whenever an original stands outside its outpost and the outpost's chunks are loaded. Drop the one-shot flag. This also clears dev worlds where an original already stands in a Fane.
- Rewrite the two `complete_vigil` lines (this covers PT-86's line too) as "a line member or a successor of your Fane", following `NPC_VOICES.md`.
- Move the six probes onto a successor fixture, and flip `summonStripsTheOutpost` to "the Vicar stays".
- Cost to the player: a solo player needs one successor (made at D5) before the D6 Covenant Vigil and the Archon rite, and three before Ancestral Communion at D7. Today the three originals fill those slots.

**Option B: remove ordinary recruitment as well. Size L.**
- The Bequest becomes the only bond.
- Remove `recruit_harbinger`, `expel_harbinger`, the recruit nodes in three trees, the type and outpost limits, and the pool capacity originals give.
- Rewrite `LORE_REFERENCE.md` lines 54 and 301, reference lines 3561 to 3565, the stake budget rule and the recruitment probes in `PlaythroughIlluminatusGameTests`.
- This is the cleanest story but the most churn. Solo pool growth would then also wait on Succession.

**Option C: as A, but a pledged original can hold a station from afar without moving. Size M+.**
- The pledged Vicar "keeps vigil from the outpost" and counts as a helper for a solo player.
- This keeps today's solo route with no body taken.
- It needs a new helper path that is not a body in `CardinalRiteAllyService.isAvailable` and `hasRequiredHelpers`, plus some art.
- Pick it only if you do not want A's D5 successor gate.

**NPCs already pledged.** The mod is not released, so no migration is needed (`AGENTS.md`). Under A and C they stay pledged pool members, and the every-time home check returns any that stand in a Fane. Under B, the old entries are cleared from dev saves, or the dev world is reset.

**Rows of `docs/OPEN_QUESTIONS.md` this changes**
- NQ-10-5 (line 185): closed. Once originals can no longer be summoned, there is no use left to end. Record your ruling as A, B or C.
- PT-85 (line 510, the planned default that two lines may pledge the same NPC): it stands. A pledge moves nothing, so sharing does no harm. The roll-up row at line 850 becomes "Summon and stations are successor-only; originals always go home. Flip `summonStripsTheOutpost`." It no longer waits on NQ-10-5.
- PT-10 / NQ-10-4 (lines 500 and 849): smaller. An original can then be lost only to `/kill` or the void, never to a summon, so the vacancy rule covers only that loss.
- PT-23 (line 831): unchanged.
- The shared vacancy store (line 511, chunk 10 T10) no longer needs `outpostHome` on every resident or a "sent home" path, so those parts of T10 in `docs/owner_answers/10-early-and-mid-progression.md` drop out. The store keeps only the `/kill` and void refill and PT-23's fallback spot.
- PT-86 (line 509): reworded along with the Vigil lines above.
