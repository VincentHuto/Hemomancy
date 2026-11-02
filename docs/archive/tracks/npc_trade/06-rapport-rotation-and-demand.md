# Track 6 (second pass): rapport-gated lore, rotating wants, networked demand

**Status (2026-10-31): built, uncommitted.** Rapport opens lore, daily wants rotate, four cross-NPC links ship, and the
Ringmaster takes a spectacle. What later tracks code against:
- **Rapport lore.** `DialogueOptionPresentation` gained `int rapportTier` (on the wire, after `quest`), with
  `withRapportTier(tier)` and `asDisabled(detailKey)`; `DialogueOption.withRapportTier(tier)`. The lines are a table,
  `common/entity/npc/dialogue/DialogueRapportLore`: per speaker a Known and a Trusted `Line(speaker, slug, tier, lines)`,
  keyed `hemomancy.dialogue.rapport.<speaker>.<slug>` (question), `.teaser`, `.line1..n`; node id `rapport_<slug>`, read
  id `hemomancy:<speaker>/lore/rapport_<slug>`. 56 lines over 28 speakers (all eight Harbingers, the Zealot, Acolyte and
  Guardian, the eight Court roles with a custom card, the Ringmaster and the eight performers). `DialogueRapportLore.with
  (tree, speaker, tier)` adds them before the farewell; only open lines bring their answer node.
  `DialogueHubFactory.decorate(ServerPlayer)` reads the tier from `NpcTradeLedger` (`NpcTradeRules.tier`), skips the lines
  for a speaker who refuses the player (Unstained facing D1+), and calls the new `decorate(tree, npcId, knowledge,
  rapportTier)` (the old 3-arg form is tier 0). A lore option above the tier becomes a `LOCKED` Conversation card with the
  lore glyph, `summaryKey` = `<text>.teaser`, target `DialogueHubFactory.LOCKED_TARGET` (`__locked`), no unread dot; the
  greeting lists it as a disabled option whose detail is the teaser. All 56 are in `lore-options.txt`;
  `DialogueLoreFlagTest` now accepts every `NpcTradeSides` speaker id there.
- **Rotation.** `"rotation": {"pool": "<id>", "weight": 1-100}` is parsed into `NpcTradeOffer.rotation()`
  (`Optional<NpcTradeRotation.Entry>`; the old 14-arg constructor still builds an ordinary card). Customs and acts may not
  rotate. `NpcTradeRotation.today(offers, levelSeed, day)` keeps every ordinary card and one weighted pick per
  speaker+pool from `hash(seed, speaker, pool, day)` (SplitMix64 over `String.hashCode`), members sorted by id;
  `offeredToday(offer, speakerOffers, seed, day)` and `pick(...)` are public. `NpcTradeCards.build(player, speaker)`
  filters through it; `NpcTradeService.attempt` refuses an off-day request with `REFUSED_NOT_TODAY`
  (`hemomancy.trade.refused.not_today`, in `REASON_KEYS`). Pools shipped: `alchemist_daily` (reef salts, herring oil) and
  `artificer_daily` (venous offcuts, bone offcuts).
- **Seen and the dot.** `NpcTradeCard` gained `daily` and `fresh` (on the wire; the old 12-arg constructor and
  `of(offer, lock, uses, days)` still exist). `NpcTradeLedger` gained `RotationSeen` (offer -> last day seen:
  `rotationSeen`, `markRotationSeen`) and `Acts` (`acts`, `recordAct` capped at `ACTS_MAX` 8, `spendAct`). A fresh card's
  TRADE topic is `unread`. New serverbound `NpcTradeSeenPacket(entityId)` (guarded like the request) calls
  `NpcTradeCards.markRotationSeen(player, speaker)`; `DialogueScreen` sends it from `enterCategory(TRADE)` when it clears a
  fresh topic locally. Hub category cards now draw the unread dot when any of their topics is unread (Conversation lore
  too). `DialogueTradeView` draws a "Today" ribbon on daily cards.
- **Acts (Circus spectacle).** New want kind `NpcTradeWant.Act(act)` (`{"kind": "act", "act": "spectacle"}`), Circus
  speakers only, exclusive claim `act:<act>`, default rapport 2, `show_locked` defaults true, no `give_fine`, no
  `opens`. `CircusPerformanceController` banks one `SPECTACLE` through `NpcTradeActs.witnessed(player, act)` (living,
  non-spectator only) whenever a pavilion challenge completes. The card is locked on `Act.lockKey()`
  (`hemomancy.trade.locked.act.spectacle`) while none is banked; the trade spends one. Card: `circus_ringmaster/spectacle`
  -> laughing mask + 2 circus curtain, rapport 4.
- **Networked demand.** `alchemist/reef_salts` (4 erythrocoral fragment, the Voyager's), `alchemist/pelagic_oil` (4
  cooked pelagic herring, the Voyager's), `artificer/venous_offcuts` (8 polished venous stone, the Vein-Mason's),
  `artificer/bone_offcuts` (8 bone bricks, the Vein-Mason's), `vicar/thread_blessing` (2 memory thread, the
  Mnemonist's), `voyager/wreck_coral` (1 calcified erythrocoral, the Wayfarer's). Each line names the other NPC; no
  Unstained card names Harbinger goods.
- **Wire.** `PROTOCOL_VERSION` 29 -> 30; `payload-registry.txt` gained `playToServer NpcTradeSeenPacket`.
- **Tests.** `NpcTradeRotationTest` (stable per seed/day, changes across days and seeds, one per pool, weight, fresh until
  seen), `DialogueHubFactoryTest.aRapportLoreLineIsALockedCardAtTierZeroAndOpensAtItsTier`, `DialogueRapportLoreTest`
  (keys, golden, speakers with cards, gating), new cases in `NpcTradeLoaderTest`, `NpcTradeServiceTest` (act spend),
  `NpcTradeLedgerTest` (new NBT keys), `NpcTradeLangKeysTest` (act keys). Rapport isolation was already covered by
  `NpcTradeServiceTest.aTradeTakesThePriceInsertsTheGoodsAndRecordsOnlyThisSpeaker`.

Deviations from the plan, with reasons:
- **No `{ "lore": ... }` give for the Hermit.** His bread card already pays 2 rapport, and rapport is what opens his
  lines, so a lore give would be a second road to the same gate. His Known line ("the vigil") carries the scrap of
  advice; the card still gives a candle.
- **Two lines per speaker, Known and Trusted.** No Confidant line yet; the tier exists for a later pass.
- **Scout lines came later.** At first the Scout's dialogue wasn't routed through the hub, so she had no lines. Since
  2026-10-31 `UnstainedScoutEntity` decorates her tree like the other Unstained, which opens her `scout/paper` card and
  her Known (`why_scouting`) and Trusted (`anyone_waiting`) lines.
- **Court roles without a custom card get no lines**, since their rapport can't rise.
- **"today" ribbon is drawn, not a sprite**: a filled tab in the theme's progress colour with the `hemomancy.trade.ui.today`
  label, so it follows every theme without new art.
- **Seen is recorded when the Trade box opens**, by a new packet, not when the hub is built: hub building also runs for
  NPC attention markers, which would have cleared the dot unseen.
- **Spectacle is a new `act` want, not a Circus reading of `custom`.** A custom is an item offering under
  `CourtHospitality`; a spectacle is a banked count with no item, so it fits neither the custom's slot pick nor its
  refusal rules. Challenges complete once each, so a player banks at most four spectacles from the current pavilion.
- **Rotation cards "pay above the usual rate"** is data: the four daily cards pay a little more per item than their
  neighbours (two binders for eight stone against one for a worn tool, two primers for four fragments against one for four
  recycled enzymes) and are limited to 2 uses a day.


**Goal:** trading feeds the conversation and sends the player around the world. Build this after Tracks 1, 3 and 5 have played for a while; the numbers here are starting values.

## Rapport opens lore
- `DialogueOptionPresentation` (Track 1's lore flag) gains `int rapportTier` (default 0). A lore option with a tier above the player's shows in Conversation as a **locked lore card**: the glyph, a lock, and a teaser key (`<option key>.teaser`), but no target. At the tier, it opens like any lore card and gets the unread dot.
- `DialogueHubFactory.decorate` reads the tier from `NpcTradeLedger` through `HemoCapabilityAccess`.
- Each NPC gets two or three new rapport lore lines (Known, Trusted, Confidant), written to its voice sheet. They're about the speaker, their past or their side, not mechanics.
- The Hermit's single card unlocks a lore line instead of an item. A `give` entry of `{ "lore": "<option id>" }` that grants rapport to exactly that line's tier is cleaner than a fake item. Decide during the build.

## Rotating wants
- An offer with `"rotation": { "pool": "alchemist_daily", "weight": 1 }` joins a rotation pool. Each speaker shows **one** card from each of its pools per in-game day.
- The pick is deterministic per world and day: `hash(levelSeed, speaker, pool, day)`. Two players on one server see the same daily want, so they can talk about it.
- A rotation card pays above the usual rate and is marked with a small "today" ribbon in Track 4's view.
- `NpcTradeLedger.rotationSeen` records which daily cards the player has seen, so the first sighting gets a `NOTICE` dot on the Trade box.

## Networked demand
One NPC's goods become another's want, so a run through the outpost becomes a route. First links:
- Voyager (Reef goods) → Alchemist (salt and brine reagents).
- Vein-Mason (offcuts) → Artificer (station parts).
- Mnemonist (`memory_thread`) → Vicar (a blessing).
- Wayfarer (wreck finds) → Voyager (route logs).

Each link is just catalogue data. A card's `line` can name the other NPC ("The Voyager brings these in; I can't get enough."), which is also a nudge for new players.

Links never cross into the Unstained: an Unstained card never names a Harbinger NPC's goods, since a player who can reach both sides' trades has to be D0.

## Rapport is per NPC (Q-5)
No spillover. Each NPC's rapport moves only through trades with that NPC.

## Circus spectacle (built on Track 5's Circus catalogue)
A second-pass Circus want: **spectacle**, a performance watched through to the end, recorded from `CircusPerformanceController` as an act in the ledger. This needs a new want kind (`act`, generalised from Court `custom`) or a Circus-only reading of `custom`. Decide which when you build it. It only applies to living, non-spectator players (the pavilion observation rule in AGENTS.md).

## Tests
- `NpcTradeRotationTest`: the daily pick is stable for a seed and day and changes across days.
- `DialogueHubFactoryTest`: a locked lore card at tier 0, opened at tier 2.
- A trade with one NPC leaves every other NPC's rapport unchanged.
