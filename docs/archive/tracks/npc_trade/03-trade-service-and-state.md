# Track 3: trade state and the server transaction

**Status (2026-10-31): built, uncommitted.** What later tracks code against:
- **Ledger:** `common/capability/player/shared/trade/NpcTradeLedger`, attachment `npc_trade_ledger` (`HemoAttachmentTypes.NPC_TRADE_LEDGER`, copy on death; `HemoCapabilityKeys.NPC_TRADE_LEDGER`; added to `src/gameTest/resources/frozen/registry-ids.txt`). Read it with `HemoCapabilityAccess.npcTrades(player)`. API: `rapport(speaker)`, `addRapport(speaker, delta)` (clamped 0..100, returns the new value), `used / usesLeft(offer, uses, day, restockDays)`, `daysToRestock(offer, day, restockDays)`, `recordUse(offer, day, restockDays)`. A window older than `restockDays`, or one opened on a later day than now (the clock set back), reads as empty and is replaced on the next `recordUse`. NBT keys are `Rapport` and `Uses` (`{Start, Used}` per offer). `rotationSeen` isn't there yet; Track 6 adds it as a new key.
- **Rules:** `common/entity/npc/trade/NpcTradeRules`: `RAPPORT_MAX`, `tier(rapport)` (`STRANGER`/`KNOWN`/`TRUSTED`/`CONFIDANT` = 0..3), `tierFloor(tier)`, `tierKey(tier)` (`hemomancy.trade.tier.<name>`), `day(dayTime)`, `MAX_SLOTS` = 36, `REQUEST_INTERVAL_TICKS` = 4.
- **Blood source:** `NpcTradeSources.TITHE` = `Source.of("npc_tithe", Category.OTHER, true, true)`; `NpcTradeSources.byId(id)`. The lang key `blood_source.hemomancy.npc_tithe` is in `en_us.json`.
- **Speakers:** `NpcTradeSpeaker` (`tradeSpeakerId()`, `tradesWith(ServerPlayer)` default true). `ProgressionDialogueNpc` extends it, and `tradeSpeakerId()` returns `progressionDialogueId()`. `CircusRingmasterEntity` (`circus_ringmaster`, trades only once `CircusProgressRules.canReceivePact`) and `CircusPerformerEntity` (`facultyRoleId()`, trades only when it isn't downed or mid-threat) implement it.
- **Cards on open:** `NpcTradeCards.build(player, speaker)` returns an `NpcTradeBoard(speaker, cards, Optional refusalKey, rapport, tier)`. `DialogueHubFactory.decorate(tree, npcId, ServerPlayer)` passes the board to `DialogueHubFactory.withTrade(tree, board)`. That adds one `TRADE` topic per card (`tradeTopicId(offerId)` = `trade/<offerId>`, title = card title, summary = card line, display item = first give) or a single `TRADE_REFUSAL_TOPIC` (`trade/refusal`) whose summary is the refusal key. All of them use `TRADE_ICON` and the target `TRADE_TARGET` (`__trade`, which names no node). The board itself rides in `DialoguePresentation.trade()`, with `tradeCards()` as a shortcut.
- **Card:** `NpcTradeCard(offerId, titleKey, lineKey, order, want, give, giveFine, state, Optional lockKey, daysToRestock, usesLeft, Optional opens)`. `State` is `OPEN`, `SOLD_OUT` or `LOCKED`. `usesLeft` is `NpcTradeCard.UNLIMITED` (-1) for an unlimited card. The whole `NpcTradeWant` goes on the wire (match ids and tags, count, appraisal grader and params), so the client can match its own inventory with `NpcTradeMatch.matches` and preview a grade with `NpcTradeAppraisals.grade`. Gives are `NpcTradeGive` records: build stacks client-side with `give.toStack(level.registryAccess())`.
- **Request:** `common/network/trade/NpcTradeRequestPacket(int entityId, ResourceLocation offerId, int[] slots)`, `playToServer`. Slots are main-inventory indices (`Inventory.items`, 0..35) in the order the player picked them. Each slot pays at most one bundle part, and earlier slots are used first.
- **Reply:** `NpcTradeResultPacket(int entityId, ResourceLocation offerId, NpcTradeService.Outcome outcome, String reasonKey, NpcTradeBoard board)`, `playToClient`. `rapport()` and `tier()` read the board. `ClientboundTradeHandlers` calls `DialogueScreen.receiveTradeResult(entityId, TradeReply, board)`. If the open screen is that speaker's, the call replaces `DialogueScreen.tradeBoard()` and sets `lastTradeReply()`. Track 4 draws from those two.
- **Reason keys:** `NpcTradeService.REASON_KEYS` holds `hemomancy.trade.refused.{unknown,sold_out,missing,poor,blood,no_room}` and `hemomancy.trade.done[_fine]`. A refusal can also carry a `NpcTradeRequirements` lock key, the speaker's `refuse_harbinger` key, or `CourtHospitality.REFUSED`. `NpcTradeLangKeysTest` checks these keys, the tier names and the tithe label.
- **Wire:** `PROTOCOL_VERSION` is 29, and `payload-registry.txt` has been rewritten.

Deviations from the plan:
- The reply carries one `NpcTradeBoard` instead of separate `cards`, `rapport` and `tier` fields, and the presentation carries the same board, not just a `List<NpcTradeCard>`. The refusal key and the rapport have to reach the client on open too, and one record keeps the open and the reply the same shape.
- The transaction's pure core is `NpcTradeService.run(offer, Party, ledger, day, slots)`. `Party` is the interface over the player and world. `PlayerTradeParty` backs it for real players, and `NpcTradeServiceTest` uses a fake. `attempt(player, entity, offerId, slots)` is what the packet calls. It applies the rate limit (`NpcTradeRequestLimiter`, registered in `TransientServerStateEvents`), looks up the offer and checks that it belongs to this speaker, refuses a misbegotten or non-serving successor Harbinger, then calls `run` and sends the reply. Unknown offers and refused speakers get a reply too; a rate-limited request gets none.
- Blood: `BloodAccounting.pay` empties the volume when asked for more than it holds, so it can't serve as the check. I added `BloodAccounting.canPayInFull(player, amount, source)`, built on a new `BloodPaymentRules.coversInFull` / `allowed`. These use the same reserve rule `settle` uses, so the reserve isn't worked out a second time. A blood price is paid before item takes. It is the only price on its card, so nothing else has been taken if it fails.
- Court custom: `CourtHospitality` gained `isCustomOffering(stack, house)`, `customRefusal(player, member, act)` (shunned, turned away, role takes no custom, or wrong act, all answered with `REFUSED`) and `acceptCustom(player, house, stack)`. The dialogue `offer` path now uses these too. The offering is taken from the first chosen slot that holds the house's custom, not from the main hand.
- `ServerboundGuards` gained `nearLivingEntity(player, id, reach, type)`, `activePlayer(player)` and `distinctSlots(slots, size)`. The packet uses dialogue reach (8 blocks) and also requires `NpcTradeSpeaker.tradesWith(player)`.
- POOR: the trade is refused as soon as any chosen stack that would be taken grades `POOR`, even if other chosen stacks could cover the count.
- A shortfall after insertion (step 7) is dropped as an `ItemEntity` at the player's feet with no pickup delay, and a warning is logged.
- No `docs/HEMOMANCY_REFERENCE.md` change: there are still no shipped offers, so nothing is player-visible until Tracks 4 and 5 land.


**Goal:** one server service runs a trade from a validated request: it checks the gates, takes the price, inserts the goods, records rapport and stock, and answers the client. One ledger attachment holds what must survive relog.

## The ledger
`common/capability/player/shared/trade/NpcTradeLedger implements INBTSerializable<CompoundTag>`, registered as `npc_trade_ledger` in `HemoAttachmentTypes` (copy on death, like `DIALOGUE_KNOWLEDGE`), keyed in `HemoCapabilityKeys`, read through a new `HemoCapabilityAccess.npcTrades(player)`.

It holds:
- `rapport: Map<String speaker, Integer>`, clamped 0 to `NpcTradeRules.RAPPORT_MAX` (100).
- `uses: Map<ResourceLocation offer, Window>`, where `Window(long startDay, int used)`. A window older than the offer's `restock_days` counts as empty; it is reset lazily on the next read, not by a tick.
- `rotationSeen` is reserved for Track 6.

NBT keys: `Rapport`, `Uses`, each a compound. It is a new attachment, so no `PlayerDataMigrations` entry is needed.

**Sync:** none through `PlayerStateSync`. The ledger only shows inside an open dialogue, and every open sends fresh card state (below). The stock window is its own system's state, so it lives here, not in `PlayerCooldowns` (`CooldownTierGuardrailTest` allows a system's own attachment).

**Death and End return:** rapport and stock survive both. These are relationships, not cooldowns.

## Rapport tiers
`NpcTradeRules.tier(rapport)`: 0 Stranger (0-9), 1 Known (10-29), 2 Trusted (30-59), 3 Confidant (60-100). Offers gate on the raw number (`rapport_at_least`). Lore gates on the tier (Track 6). Tier names are lang keys.

## Card state on open
`DialogueHubFactory.decorate` (after Track 1) asks `NpcTradeCards.build(player, speaker)` for a list of `NpcTradeCard`s and adds one `TRADE` topic per card, so the box enables itself.

For an `UNSTAINED` speaker and a player at D1 or above, `build` returns no cards and a refusal key (`hemomancy.trade.<speaker>.refuse_harbinger`). The Trade box still enables itself and opens on the speaker's refusal line, so the refusal reads as a choice they made, not a missing feature.

A card carries:
- offer id, title and line keys, the want (display stacks, count, or a blood amount, or a custom id), the gives, `order`;
- `state`: `OPEN`, `SOLD_OUT` (with the days to restock), `LOCKED` (with a reason key, only when `show_locked`);
- `usesLeft`;
- for `items` wants, nothing about the player's inventory. The client matches its own inventory (Track 4); the server checks again.

The cards ride in `DialoguePresentation` as a new `List<NpcTradeCard> tradeCards` (wire change, protocol bump). They don't go in a second open packet: the trade box and the hub need to agree in a single frame.

## The request
`NpcTradeRequestPacket(int entityId, ResourceLocation offerId, int[] slots)`, `playToServer`, in `common/network/trade/`. It decodes, validates and calls `NpcTradeService.attempt(player, entity, offerId, slots)`. Nothing else lives in the packet.

`ServerboundGuards` checks before the service runs:
- the entity exists, is alive, and is within dialogue reach, and its speaker id is the offer's speaker. A `ProgressionDialogueNpc` gives it through `progressionDialogueId()`. The Circus entities aren't `ProgressionDialogueNpc`s, so give them (and anything later) one small `NpcTradeSpeaker` interface with `tradeSpeakerId()`, and have `ProgressionDialogueNpc` provide it by default;
- the player is alive and not a spectator;
- `slots` is at most 36 entries, each a valid main-inventory index, with no repeats;
- a rate limit: one request per 4 ticks per player (a session map cleared through `TransientServerStateEvents`).

## The transaction (`NpcTradeService.attempt`)
In this order, and nothing changes until step 6:
1. Look up the offer; refuse `unknown`.
2. Check the side (an `UNSTAINED` speaker refuses a player at D1 or above: `refused_path`), then `requires` and stock again; refuse `locked` / `sold_out`.
3. Check the want:
   - `items` / `bundle`: the chosen slots hold enough matching stacks. Each stack is appraised; a `POOR` stack is refused with its reason. The grade of the trade is the **lowest** grade among the stacks taken.
   - `blood`: `volume.isActive()` and enough volume above the reserve. `BloodAccounting` decides the reserve; don't re-derive it here.
   - `custom`: defer to `CourtHospitality`'s own checks (shunned, turned away, the house takes this custom).
4. Pick the gives (`give_fine` on a `FINE` grade).
5. **Simulate insertion** into a copy of the main inventory. Refuse `no_room` if any give doesn't fit. Don't use `Inventory.add`'s boolean: in Creative it reports success for a discard (the same trap `PendingDelivery` documents).
6. Take the price: shrink the chosen stacks, or `BloodAccounting.pay(player, amount, NpcTradeSources.TITHE)`, or run the custom through `CourtHospitality`.
7. Insert the gives for real and confirm by count. If a count comes up short (a mod changed the inventory between 5 and 7), drop the shortfall **at the player's feet** and log it. This should never happen; a GameTest pins the normal path.
8. Record: `uses +1`, `rapport += offer.rapport` (+1 on `FINE` if there's no `give_fine`), for this speaker only. Rapport is per NPC (Q-5) and never spills over to other NPCs.
9. Reply with `NpcTradeResultPacket` and a fresh card list.

`NpcTradeSources.TITHE = new Source("npc_tithe", Category.<closest existing>, countsAsSpend=true, reserveProtected=true)`. Read `BloodAccounting.Source` and pick the category from what's there. Don't invent one without checking.

## The reply
`NpcTradeResultPacket(int entityId, ResourceLocation offerId, Outcome outcome, String reasonKey, List<NpcTradeCard> cards, int rapport, int tier)`, `playToClient`. `Outcome`: `DONE`, `DONE_FINE`, `REFUSED`. The client handler goes in `client/network/ClientboundTradeHandlers`, registered as `ClientPayloadHandlers::dispatch`. It updates the open `DialogueScreen` if the entity matches, and does nothing otherwise.

## Tests
- `NpcTradeLedgerTest`: NBT round trip, the lazy window reset, clamping.
- `NpcTradeServiceTest` (pure, with a fake inventory): every refusal leaves the inventory untouched; the lowest grade wins; Creative doesn't count as inserted.
- GameTests in Track 7 cover the real inventory, blood and the Court custom.
