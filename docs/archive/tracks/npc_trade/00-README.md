# NPC trading and the Conversation merge: work paths

Date: 2026-10-31. Status: all seven tracks built, uncommitted (each track file has its Status block).

Every NPC with a dialogue hub shows four boxes today (`DialoguePresentation.CATEGORIES`): **Quest Work**, **Inquiries**, **Lore** and **Conversation** ("Speak Freely"). Topics land in Lore or Conversation by keyword guessing in `DialogueHubFactory.classify`, so the split is arbitrary: an option is "lore" because its id contains `rite` or `why_`. NPCs trade nothing. The only trades in the mod are vanilla villager listings in `VillageEvents`.

**The new shape:**
- **Lore and Conversation become one box, Conversation.** Every root option gets its own card. A lore line carries a lore glyph and keeps the unread dot it has now. Lore is an explicit flag on the option, not a keyword match.
- **The freed slot becomes Trade.** Each NPC offers a set of **want cards**. A card names what the NPC wants (an item, a tag, blood, or a house custom) and what they give. Every card asks for its own input, so two cards from one NPC never want the same thing.
- What a card pays depends on **who the NPC is**: Harbingers take blood and specimens, the Unstained take clean materials (and won't deal with Harbingers at all), the Brined Court keeps its gift economy (customs for standing, never prices), and the Circus takes curiosities and spectacle.
- NPCs **appraise** quality, and **rapport** from trading opens better cards and new lore lines in Conversation.

Each file below is one track. A session takes one track, reads this README first, and leaves the others alone.

| # | Track | File | Depends on |
| --- | --- | --- | --- |
| 1 | Conversation merge: one box for lore and talk, explicit lore flag, per-option cards; the Circus joins the hub | [01-conversation-merge.md](01-conversation-merge.md) | — |
| 2 | Trade data: the offer JSON, its loader, wants, prices, gives, gates | [02-trade-data-model.md](02-trade-data-model.md) | — |
| 3 | Trade state and service: the ledger attachment, the server transaction, payloads | [03-trade-service-and-state.md](03-trade-service-and-state.md) | 2 (offer records) |
| 4 | The Trade box: want cards, inventory matching, appraisal preview, confirm | [04-trade-screen.md](04-trade-screen.md) | 1 (the TRADE category), 3 (payloads) |
| 5 | Catalogues: every NPC's offers, currencies by side, the Court's customs | [05-npc-catalogues.md](05-npc-catalogues.md) | 2 (schema); lines need 1 |
| 6 | Second pass: rapport-gated lore, rotating wants, trade chains between NPCs | [06-rapport-rotation-and-demand.md](06-rapport-rotation-and-demand.md) | 1, 3, 5 |
| 7 | Validation, guardrails, review client and docs | [07-validation-and-docs.md](07-validation-and-docs.md) | each track adds its part |

Track 1 and Track 2 can start in parallel. Track 1 owns the `DialogueCategory` change (`LORE` out, `TRADE` in), so Track 4 waits for it. Track 3 codes against Track 2's records. Track 5 is mostly data and lang and can be drafted any time, but only loads once Track 2's loader exists.

## Decisions (my calls; the user can overturn any of them)
- **Box order:** Quest Work, Inquiries, Conversation, Trade. The hub stays a four-card grid, so `DialogueHubLayout` doesn't change shape.
- **Conversation lists one card per option**, the way Lore does now, instead of today's single "Speak Freely" card that opens a node of options. A lore card has the lore glyph and the unread dot. Ordinary talk has no glyph.
- **Read ids keep their old form** (`hemomancy:<npc>/lore/<slug>`), so lore a tester already read stays read. The mod isn't live, so this is a convenience, not a migration.
- **Trades are not dialogue events.** They use their own serverbound payload and service (Track 3), not a `DialogueEvent` route. `DialogueRoutingGuardrailTest` doesn't change.
- **Trades are data**, under `data/hemomancy/npc_trade/<speaker>/<offer>.json`, using the same speaker ids as `dialogue_inquiry/`.
- **Payment is all or nothing.** The server checks the price and the room for the goods before it takes anything. If the goods won't fit, the trade is refused. Nothing is dropped and nothing waits in a pending claim.
- **Trades never grant progression.** No manipulation memories, no rank proofs, no station kits that skip a gate, no advancements. A trade can sell what a player could otherwise craft or find. It never sells the thing a lesson is meant to prove.

## The user's answers (2026-10-31)
| Id | Question | Answer |
| --- | --- | --- |
| Q-1 | Does the Brined Court keep its gift economy? | **Yes.** Court cards are customs that earn standing, and gifts unlock by standing. No prices (Track 5). |
| Q-2 | Will Unstained NPCs trade with a Harbinger? | **No.** An Unstained NPC refuses any player at D1 or above (`ProgressionGates.hasDegree(player, 1)`). The Trade box opens on a refusal line and shows no cards. Players on neither path (D0, not Unstained) may trade (my call). Stained goods are still refused at the card. |
| Q-3 | Blood prices only at Harbinger NPCs? | **Yes.** The loader rejects a `blood` want on any speaker that isn't on the Harbinger side. Blood trades also need `volume.isActive()`. |
| Q-4 | Does the Circus get a Trade box in the first pass? | **Yes.** The Ringmaster and the performers (acrobat, fire-eater, knife-thrower, stilt-walker, faculty) go through the hub (Track 1) and get catalogues (Track 5). |
| Q-5 | Rapport per NPC or per faction? | **Per NPC.** No spillover between NPCs. |

## Sides
Each speaker belongs to one side, held in `NpcTradeSides` (a table, speaker id → side): `HARBINGER`, `UNSTAINED`, `COURT`, `CIRCUS`. The side decides who may trade at all (Unstained refuse Harbingers), which price kinds are legal (`blood` only for `HARBINGER`, `custom` only for `COURT`), and the review client's coverage. A speaker missing from the table fails the loader.

## Shared rules for every track
- Read `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md` first. Function-first packages: offers and the service in `common/entity/npc/trade/`, the ledger in `common/capability/player/shared/trade/`, payloads in `common/network/trade/`, the view inside `client/screen/dialogue/`. No top-level `trade/` folder.
- Player-facing text is `en_us.json` keys only. Card titles, refusal reasons, appraisal grades and NPC trade lines are all keys. Trade lines follow the speaker's sheet in `docs/NPC_VOICES.md`, one speaker at a time.
- Payloads go through `PacketHandler`: bump `PROTOCOL_VERSION` (27 on disk today) and rewrite `src/test/resources/network/payload-registry.txt`. Tracks 1 and 3 both change the wire, so bump by one from whatever is on disk when you land.
- Serverbound trade claims are checked with `ServerboundGuards` (the entity is near, alive and the right speaker; the offer exists and is open to this player).
- Blood prices go through `BloodAccounting.pay` with a named `Source`, never a direct drain.
- Rapport, stock and rotation live in a player attachment registered through `HemoAttachmentTypes` / `HemoCapabilityKeys` and read through `HemoCapabilityAccess`. No new `getPersistentData()` calls.
- Gates use `ProgressionGates`, never a degree compared with a literal.
- Harbinger and Unstained stay mutually exclusive. A trade never changes degree, purity or path, and never counts as a step on either path.
- No commits; leave work in the tree.

## Review client
Track 4 adds `npcTradeReviewClient` as a row in `gradle/review-clients.gradle` (driver `NpcTradeClientReview` under `src/gameTest`, the operator-inbox pattern from `heartNodeReviewClient`). It spawns the Alchemist, a Zealot, a Gardener and the Ringmaster, gives the test player a spread of items, opens each hub and captures the Conversation box (lore glyphs, unread dots) and the Trade box (matched, unmatched, appraised, refused). The Zealot is captured twice: once for a D0 player (cards) and once for a D1 Harbinger (the refusal). Screenshots go to `build/npc-trade-review/screenshots`.
