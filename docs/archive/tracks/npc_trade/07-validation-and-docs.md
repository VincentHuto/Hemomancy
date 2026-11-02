# Track 7: validation, guardrails, review and docs

**Final review (2026-10-31): all seven tracks reviewed, uncommitted.** I checked these against the plan and found them
correct: the Unstained refusal (in `NpcTradeCards.build` and in `NpcTradeService.run`), blood paid only through
`BloodAccounting.pay` after `canPayInFull` and only on Harbinger speakers, the Court custom paid through
`CourtHospitality.customRefusal`/`acceptCustom`, the give bans (by id, by memory class and by `never_sold`), all checks
running before anything is taken, room simulated on an inventory copy with insertion confirmed by count,
`ServerboundGuards` on both serverbound payloads, `lore/<slug>` read ids, locked rapport lines getting no node, and
rapport kept per speaker. One defect was fixed:
- **Court prices (Q-1).** The loader stopped custom wants off the Court but let a Court speaker take an items, bundle
  or blood price. `NpcTradeLoader.parse` now rejects any Court card that isn't a custom, and `NpcTradeLoaderTest` has
  the new case. Shipped data was already all customs.

**Status (2026-10-31): built, uncommitted.** What later work codes against:
- **Guardrail.** `architecture/NpcTradeGuardrailTest` (listed in `ARCHITECTURE_STANDARDS.md`, with a row in its
  section 5 mechanics table) reads the shipped JSON and the trade sources:
  - no `give`/`give_fine` names a memory, a station kit (`NpcTradeGiveRules.isBannedById`) or a
    `trade/never_sold` entry;
  - no speaker gives an item another of its cards wants;
  - every speaker folder has an `NpcTradeSides` row, and `blood`, `custom` and `act` stay on Harbinger, Court and
    Circus;
  - `NpcTradeCards.build(ServerPlayer, ...)` and `NpcTradeService.run` (through `PlayerTradeParty.refusedBySide`) both
    ask `NpcTradeSides.refuses`;
  - only these classes write `NpcTradeLedger`: `NpcTradeService` (`addRapport`, `recordUse`, `spendAct`),
    `NpcTradeActs` (`recordAct`) and `NpcTradeCards` (`markRotationSeen`). A new writer must be added to
    `LEDGER_WRITERS`;
  - nothing in `common/entity/npc/trade` or `common/network/trade` names `DialogueEvent`, and no dialogue route names
    a trade class.
- **GameTests.** `gametest/NpcTradeGameTests`, namespace `npc_trade_validation`, launcher `npcTradeGameTestServer`,
  in the combined `alphaCheck` gate (`gradle/game-tests.gradle`; template copied to
  `src/gameTest/resources/data/npc_trade_validation/structure/empty.nbt`). 14 tests:
  - the live catalogue: `NpcTradeRegistry.errors() == 0`, exactly 56 cards (`SHIPPED_CARDS`), every give a real
    stack within its stack size, and the never_sold, stained and curiosity tags bound;
  - the hub on all 8 Harbinger NPCs, the 3 hub Unstained, the Ringmaster, the 8 performers and all 18 Court roles,
    opened by a real interact: four boxes in order, a Trade box where the speaker has offers, and the tree
    round-trips `OpenDialoguePacket.STREAM_CODEC`;
  - an items trade with rapport for one speaker only, uses and sell-out, a full inventory, POOR/FAIR/FINE, and blood
    through `npc_tithe` with restock after `setDayTime`. The clock test runs alone in batch `npc_trade_clock` and puts
    the clock back;
  - the Court custom, whose standing matches `CourtHospitality.handle(OFFER)`;
  - stale requests through `NpcTradeRequestPacket.handle` (wrong speaker, unknown card, repeated or out-of-range
    slots, out of reach, dead player, the rate limit);
  - the Zealot at D0 and D1, including a forged request;
  - the Ringmaster's curiosity card and his Quest Work topics (trial, accept/reject, finale);
  - Track 6's server paths: spectacle banking and spending, rapport lore locked then open at Trusted, and a daily
    card fresh until `markRotationSeen`.
  The helpers `trade(...)` (service, past the limiter through `NpcTradeRequestLimiter.logout`) and `request(...)`
  (payload handler with a proxy `IPayloadContext`) are the pattern for more cases.
- **Review client.** `NpcTradeClientReview` gained two Track 6 shots: `alchemist-conversation-trusted` and
  `alchemist-trade-trusted` (PASS "the Alchemist's board reads Trusted"). The run on 2026-10-31 logged every PASS line
  and wrote 31 shots.
- **Docs.**
  - `HEMOMANCY_REFERENCE.md`: new §5.4a "The Dialogue Hub and NPC Trading", and the old "no trades" lines on the
    Artificer, Votary Wayfarer, Voyager vessel and Court hospitality now point to it.
  - `AGENTS.md`: one architecture-map bullet.
  - `ARCHITECTURE_STANDARDS.md`: an index row and a mechanics row.
  - Wiki: Getting-Started, Brined-Court, Crimson-Troupe and Unstained-Path.
  - `docs/NPC_VOICES.md`: a **Trade keys.** line under each speaker's sheet.
  - The source-text impact map is regenerated.

**Deviations and findings.**
- The loaded catalogue test found a real data bug: `mnemonist/living_specimen` gave 2 `memory_thread` in one entry,
  but the item stacks to 1, so the live loader rejected the whole file. Its `give_fine` is now two entries of 1.
- The no-want/give-loop rule moved out of `NpcTradeCatalogueDataTest` into the guardrail. It is the same rule, now
  read from the raw JSON, and it is no longer duplicated.
- The ledger rule names three writers, not one. Track 6 put act banking in `NpcTradeActs` and the seen mark in
  `NpcTradeCards`, and both are legitimate.
- `BloodAccountingGuardrailTest` keeps no list of sources, so it needed no `npc_tithe` entry.
- No Liber page describes the dialogue boxes, so the Liber did not change.
- `docs/NPC_VOICES.md` is git-ignored (`/docs/*`), so its update stays local.
- The Unstained Scout became a hub speaker on 2026-10-31; the hub test now opens her too.
- The Alchemist's daily card sits below the Trade box's first screen, so the review shots don't show the "Today"
  ribbon. The GameTest covers the daily/fresh logic instead.


Each track adds its own part of this when it lands. This file is the checklist and the one place the pieces are listed.

## Unit and resource tests
| Test | Track | Pins |
| --- | --- | --- |
| `DialogueLoreFlagTest` + `dialogue/lore-options.txt` | 1 | Every option that used to be lore is flagged. No lore keywords in `classify`. |
| `DialogueHubFactoryTest` (rewritten) | 1, 6 | One card per conversation option, lore glyph, old read ids, locked lore. |
| `NpcTradeLoaderTest` | 2 | Each want kind, banned gives, one want per item per speaker, bad files skipped. |
| `NpcTradeLangKeysTest` | 2, 5 | Every key a shipped offer names is in `en_us.json`. |
| `NpcTradeLedgerTest` | 3 | NBT, window reset, clamp. |
| `NpcTradeServiceTest` | 3 | Refusals leave everything unchanged; lowest grade wins; Creative doesn't count. |
| `DialogueTradeLayoutTest` | 4 | Card and strip geometry. |
| `NpcTradeRotationTest` | 6 | Daily pick. |
| `NpcTradeCatalogueDataTest` | 5 | Shipped cards parse; catalogue coverage; trade lines. |
| `NpcTradeGuardrailTest` (architecture) | 7 | See below. |

## Guardrails (under `.../architecture/`, listed in `ARCHITECTURE_STANDARDS.md`)
- **`NpcTradeGuardrailTest`:**
  - no `give` names a `memory_*` item, `hematic_memory`, a station kit, or anything in `hemomancy:trade/never_sold`;
  - no speaker has the same item as a want on one card and a give on another;
  - every speaker folder has an `NpcTradeSides` row; `blood` wants only on `HARBINGER`, `custom` only on `COURT`;
  - the Unstained refusal is checked in both `NpcTradeCards.build` and `NpcTradeService.attempt`;
  - `NpcTradeService` is the only class that calls `NpcTradeLedger` mutators;
  - the service pays blood only through `BloodAccounting.pay` (already covered by `BloodAccountingGuardrailTest`; add `npc_tithe` to its known sources if that test lists them).
- `DialogueRoutingGuardrailTest` is unchanged: trades have no `DialogueEvent`.
- `payload-registry.txt` and `PROTOCOL_VERSION`: bumped by Tracks 1 and 3.
- Regenerate `docs/architecture/evidence/source-text-impact-map.csv` (`python gradle/source-text-impact-map.py`) after new source-text tests.

## GameTests (`npc_trade_validation`)
- Alchemist `items` trade: stacks shrink, gives arrive, rapport and uses go up.
- Full inventory: refused, nothing taken.
- `POOR` stack: refused, nothing taken. `FINE`: `give_fine` arrives.
- Sold out, then restocked after `restock_days` of `setDayTime`.
- `blood` trade: volume drops through the ledger (`BloodFlowLedger` records `npc_tithe`); inactive volume refuses.
- Court `custom`: standing records once, the same as the dialogue offering path does today.
- A stale request (wrong speaker, far away, dead player, repeated slots) is refused by `ServerboundGuards`.
- Unstained: a D0 player trades with the Zealot; a D1 player gets no cards, and a forged request is refused with nothing taken.
- Circus: a D0, inactive-blood player trades a curiosity with the Ringmaster; the Ringmaster's trial, accept/reject and finale options are still in Quest Work.
- The hub on each of the 13 hub NPCs, the Ringmaster and each performer shows four cards and opens without error (this catches a broken catalogue).

## Client proof
`npcTradeReviewClient` (Track 4) captures the scenes in the README. The trade box has no shaders, so one launch per UI change is enough.

## Docs to update when each track lands
- `docs/HEMOMANCY_REFERENCE.md`: the dialogue hub section (four boxes, lore in Conversation), a new NPC trading section (wants, appraisal, rapport, stock, the Court's customs), and the Court hospitality section's link to it.
- `docs/NPC_VOICES.md`: no rule changes, but each speaker's sheet should list its trade keys under its existing example lines.
- `AGENTS.md`: one bullet under *Architecture map*: "NPC trades are data (`data/hemomancy/npc_trade/<speaker>/`), run by `NpcTradeService` from `NpcTradeRequestPacket`; never a `DialogueEvent`, never a memory, kit or progression proof (`NpcTradeGuardrailTest`)."
- `docs/architecture/ARCHITECTURE_STANDARDS.md`: the new guardrail in its index.
- `wiki/`: the NPC page(s), if they describe the dialogue screen.
- The Liber, if a page describes the dialogue boxes.
