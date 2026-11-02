# NPC Dialogue and Trade

Status (2026-11-01): built and reviewed in a client and in GameTests on 2026-10-31, uncommitted; rapport lore content, trade balance and a few screen details still wait on the user.

## What it is

**The hub.** Right-clicking any NPC with a dialogue hub opens four boxes, in this order: **Quest Work**, **Inquiries**, **Conversation**, **Trade** (`DialogueCategory`, built by `DialogueHubFactory`). The old Lore box is gone: Conversation lists one card per root option in authored order, and a lore line is an explicit `withLore()` flag on its option (never a keyword guess). A lore card shows the lore glyph and an unread dot until opened, and keeps its old read id `hemomancy:<npc>/lore/<slug>`. Quest Work holds options flagged `withQuest()` and mission options. The hub covers the eight Harbinger NPCs, the Zealot, Guardian, Acolyte and Scout, all Brined Court roles, the Ringmaster and the eight Circus performers. Circus speakers skip the newcomer "ask the Vicar" line; every other D0 speaker shows it until the player has right-clicked a Hermit (a PlayerPersisted flag, so it survives death). (`docs/HEMOMANCY_REFERENCE.md` §5.4a)

**Dialogue events.** A chosen option posts a `DialogueEvent`; `DialogueEventRouter` tries its ordered `ROUTES` table, one `<Npc>DialogueEvents` class per NPC (Annetta, Ringmaster, Mnemonist, Alchemist, Artificer, Vicar, Voyager, Court, Vein-Mason, Acolyte, Zealot, Guardian, Hermit, Recruitment, Monolith, Fungal whisper, Scout). Trades are never dialogue events.

**Who may send an event** (2026-11-01, from the playthrough suite). `DialogueOptionPacket.dispatch` refuses a dead or spectating player before anything else (the screen they chose from is stale). Beside the station-claim speaker check, `DialogueEventSpeakers` names the speaker each progression event needs, and an event sent with `entityId 0` or to another speaker is refused: Vicar for `hermit_road_report`, `masons_respite_directive`, `give_sanguine_monolith_blueprint`, `vicar_begin_initiation` and `vicar_release_charm`; Vicar or Zealot for `give_stained_church_map`; Alchemist for `first_separation_brief`, `body_answers_brief`, `alchemist_red_taxonomy_*` and `alchemist_bestiary_*`; Mnemonist for `reliquary_taught` and `anchorite_counsel`; Anchorite for the diagnosis, D5 and D6 rewards, the D6 referral and `vein_mason_replace_*`. Whisper events are not listed. Routes also refuse events their trees never offer: the Monolith scrap only at exactly Degree 5 (the only tree that offers it); the stained church map from a Vicar at Degree 7+ or a Zealot to a purifying player with Clarity or enlightenment; the reliquary lesson at Degree 3+ within 8 blocks of a Mnemonist and not purifying; an Anchorite pattern replacement only for a tier the player earned (first lesson, D5 reward, D6 reward).

**Card states and sigils.** Main steps are Quest Work with a notice sigil while actionable: the Vicar's formal introduction, the Alchemist's First Separation assignment, and the Alchemist's Concentrated Blood card (only while a vial can be issued: nothing pending and none carried). A Quest Work card shows Turn In only when its option sends a claim or reward event, or opens a node that does; a not-ready reward card stays Active, a claimed one and a learned clinical lesson's card are Complete (`DialogueHubFactory.questState`). A Hermit dissolving after his farewell takes no trades (`NpcTradeService.serves`).

**Item inquiries.** "Ask about the held item" answers are data under `data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json`, loaded by `ItemInquiryLoader`. Entries are line lists or ordered branches gated by `min_degree`, `max_degree`, `min_purity`, `max_purity`. 27 speaker folders exist today (Harbinger NPCs, Zealot, Guardian, and the Court roles); 385 inquiry files are on disk (the §5.4 count of 317 in the reference is older).

**Trade.** Each speaker's **want cards** are data under `data/hemomancy/npc_trade/<speaker>/<offer>.json` (same speaker ids as `dialogue_inquiry/`), loaded by `NpcTradeLoader` into `NpcTradeRegistry`. 56 cards ship across 29 speakers. A card names a want, a give, an order, optional `requires` (path, degree, Unstained stage, rapport floor, advancement, active blood), `rapport` earned, `stock {uses, restock_days}`, `show_locked`, and for Court cards an `opens` key. No two cards from one speaker want the same thing, and no speaker gives what another of its cards wants.

| Want kind | Price | Who may ask |
|---|---|---|
| `items` | item, list or tag, count, optional appraisal | any side |
| `bundle` | two or three `items` parts from different stacks | any side |
| `blood` | mB paid through `BloodAccounting.pay`, source `npc_tithe` (reserve-protected, counts as spend); needs active blood | Harbinger only |
| `custom` | one offering of the speaker's own house custom (`CourtHospitality.customAct`); no gives, no stock | Court only |
| `act` | one banked `spectacle` | Circus only |

**Sides.** `NpcTradeSides` is a Java table (speaker id to `HARBINGER`, `UNSTAINED`, `COURT`, `CIRCUS`); a speaker folder without a row fails the loader. An Unstained speaker refuses any player at Degree 1 or above: the box shows only the refusal line (`hemomancy.trade.<speaker>.refuse_harbinger`), and a forged request is refused in the transaction too. Unstained cards also refuse stacks tagged `#hemomancy:trade/stained`. Court cards are customs that pay in standing, once per custom, exactly as the dialogue offering does. Circus cards take curiosities (`#hemomancy:trade/curiosity`, graded by rarity), act-themed goods, and banked spectacle; each completed pavilion challenge banks one spectacle for a living, non-spectator player, up to 8 (`NpcTradeActs`).

**Appraisal** (`NpcTradeAppraisals`, grades POOR / FAIR / FINE): `none` is always FAIR; `durability` (under a quarter left POOR, three quarters or more FINE); `rarity` (common FAIR, rarer FINE); `tendency` (a filled Bloody Vial with the named tendency; identified under the microscope is FINE); `specimen` (empty jar POOR, `hemomancy:` creature FINE). The trade takes the lowest grade among the stacks used; POOR is refused; FINE pays `give_fine` or, without one, one extra rapport. The Trade box previews grades client-side with the same graders.

**One transaction.** `NpcTradeRequestPacket(entityId, offerId, slots)` is validated by `ServerboundGuards` (alive, not spectator, distinct main-inventory slots, living speaker within 8 blocks that `tradesWith` the player, one request per 4 ticks). `NpcTradeService` checks side, gates, stock, rotation day, the price and room for the goods on a simulated inventory copy before anything changes hands; if the goods will not fit, nothing is taken. Insertion is confirmed by counting, never by `Inventory.add`'s boolean. `NpcTradeResultPacket` returns the outcome, the reason or thanks key, and a fresh `NpcTradeBoard`.

**Rapport** is per NPC, 0 to 100, in the `NpcTradeLedger` attachment (`npc_trade_ledger`, copied on death; also holds stock windows, `RotationSeen` and `Acts`). Tiers: Stranger 0-9, Known 10-29, Trusted 30-59, Confidant 60-100. `DialogueRapportLore` gives each speaker with cards a Known and a Trusted lore line in Conversation (56 lines over 28 speakers); above the player's tier the line is a locked lore card with a teaser.

**Rotation.** A card with `rotation {pool, weight}` is a daily want: one pick per speaker and pool from `hash(levelSeed, speaker, pool, day)`, worn with a "Today" ribbon and an unread dot until the Trade box is opened that day (`NpcTradeSeenPacket`). Shipped pools: `alchemist_daily`, `artificer_daily`. Six cross-NPC links name another NPC's goods (Voyager to Alchemist, Vein-Mason to Artificer, Mnemonist to Vicar, Wayfarer to Voyager); no Unstained card names Harbinger goods.

**What a trade never does.** It never grants progression: no manipulation memory (`memory_*`, `crude_memory_*`, `hematic_memory`; Memory Thread is a material), no station kit, no advancement, nothing tagged `#hemomancy:trade/never_sold` (Fungal Spine, Memory of Vesper, Mycophant Tendril, Ringmaster's Topper, Scar Blank, Consecrated Copper, Saint relics, residua and recollections). It never changes degree, purity or path.

**Voice.** Every NPC line, card title, card line, thanks and refusal is an `en_us.json` key written against the speaker's sheet in `docs/NPC_VOICES.md` (rules, contrast map with per-speaker words-per-sentence, contractions and question targets, confusable-pair rules). Three passes: pass 1 (2026-10-24) de-AI rewrite, 1,792 keys; pass 2 (2026-10-24) per-speaker sheets, 1,080 keys across 16 speakers; pass 3 (2026-10-31) voice and lore accuracy, 606 keys including Court roles, Circus faculty, trade and rapport keys. Annetta, successors, Misbegotten and the Antecedent recordings (audio-bound) were left alone.

## Where it lives

| Piece | Path |
|---|---|
| Hub, categories, lore and quest flags, rapport lore table | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/` (`DialogueHubFactory`, `DialogueCategory`, `DialogueOptionPresentation`, `DialogueTopic`, `DialogueRapportLore`) |
| Event router and per-NPC routes | `.../common/entity/npc/dialogue/route/` (`DialogueEventRouter`, `<Npc>DialogueEvents`) |
| Item inquiry data | `src/main/resources/data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json` |
| Trade records, loader, service, sides, appraisal, rotation, acts | `.../common/entity/npc/trade/` (`NpcTradeService`, `NpcTradeLoader`, `NpcTradeRegistry`, `NpcTradeSides`, `NpcTradeCards`, `NpcTradeActs`, `NpcTradeGiveRules`, `NpcTradeAppraisals`, `NpcTradeRotation`, `PlayerTradeParty`) |
| Ledger attachment | `.../common/capability/player/shared/trade/NpcTradeLedger` (read through `HemoCapabilityAccess.npcTrades`) |
| Payloads | `.../common/network/trade/` (`NpcTradeRequestPacket`, `NpcTradeResultPacket`, `NpcTradeSeenPacket`); client handler `client/network/ClientboundTradeHandlers` |
| Trade box | `.../client/screen/dialogue/` (`DialogueTradeView`, `DialogueTradeLayout`, `DialogueTradePicks`, inside `DialogueScreen`) |
| Trade data | `src/main/resources/data/hemomancy/npc_trade/<speaker>/<offer>.json` (29 speaker folders) |
| Tags | `hemomancy:trade/never_sold`, `trade/stained`, `trade/curiosity` |
| Lang keys | `hemomancy.trade.<speaker>.<offer>.title/.line/.opens`, `.thanks[_fine]`, `.appraise.poor`, `.refuse_harbinger`, `.stained`; `hemomancy.trade.ui.*`, `.locked.*`, `.refused.*`, `.tier.*`; `hemomancy.dialogue.rapport.<speaker>.<slug>` |
| Goldens | `src/test/resources/dialogue/lore-options.txt`, `src/gameTest/resources/dialogue/route-fingerprints.txt`, `src/test/resources/network/payload-registry.txt` (protocol 30) |
| Unit tests | `DialogueLoreFlagTest`, `DialogueHubFactoryTest`, `DialogueRapportLoreTest`, `NpcTradeLoaderTest`, `NpcTradeLangKeysTest`, `NpcTradeLedgerTest`, `NpcTradeServiceTest`, `NpcTradeRotationTest`, `NpcTradeCatalogueDataTest`, `DialogueTradeLayoutTest`, `DialogueTradePicksTest` |
| Guardrails | `src/test/java/com/vincenthuto/hemomancy/architecture/DialogueRoutingGuardrailTest.java`, `.../architecture/NpcTradeGuardrailTest.java` |
| GameTests | `src/gameTest/.../gametest/NpcTradeGameTests` (`npc_trade_validation`, `npcTradeGameTestServer`, 14 tests, in `alphaCheck`); `DialogueRouteCharacterisationGameTests` (`dialogue_route_validation`) |
| Review client | `npcTradeReviewClient` row in `gradle/review-clients.gradle`, driver `src/gameTest/.../NpcTradeClientReview`, shots in `build/npc-trade-review/screenshots` |
| Voice sheets and tooling | `docs/NPC_VOICES.md`; `tools/i18n/voice_rewrite/` (`pass2/`, `pass3/` with `notes/*.md`) |
| Reference and wiki | `docs/HEMOMANCY_REFERENCE.md` §5.4a; `wiki/Getting-Started.md` (dialogue), `wiki/Crimson-Troupe.md`, `wiki/Brined-Court.md`, `wiki/Unstained-Path.md` (trade) |

## Rulings that bind

**Player data moved by the playthrough fixes (2026-11-01):** the Artificer's lesson, fork-research and D7 claim keys and his first fork and D7 lineage keys, and the Mnemonist's starter-memory claim, live under `PlayerPersisted` so a death clone keeps them. `PlayerDataMigrations` entries 10 `artificer_claim_keys` and 11 `mnemonist_starter_claim` (sunset 2027-05-01) move the old root keys once at login; a value already under `PlayerPersisted` wins (`npcClaimRootKeysMoveUnderPlayerPersisted`). The met-Hermit flag is set by the right-click, under `PlayerPersisted`; old root flags are not migrated (the mod is not live).

**Architecture** (`AGENTS.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md`):
- "A chosen dialogue option posts a `DialogueEvent`; `DialogueEventRouter` tries its ordered table of routes and one `<Npc>DialogueEvents` class per NPC (`common/entity/npc/dialogue/route/`). Add an NPC's events to its class or a new class in the table, never another `DialogueEvent` subscriber (`DialogueRoutingGuardrailTest`); `dialogue_route_validation` fingerprints every route."
- "NPC trades are data (`data/hemomancy/npc_trade/<speaker>/`), run by `NpcTradeService` from `NpcTradeRequestPacket`; never a `DialogueEvent`, never a memory, kit or progression proof (`NpcTradeGuardrailTest`). Each speaker has one `NpcTradeSides` side, rapport lives in the `NpcTradeLedger` attachment and is per NPC, and `npc_trade_validation` runs every trade kind with a real player."
- `NpcTradeGuardrailTest` (standards §5): no card sells a memory, kit or `trade/never_sold` item or gives what another of its speaker's cards wants; price kinds stay on their side; the Unstained refusal is checked at the board and in the transaction; only `NpcTradeService`, `NpcTradeActs` and `NpcTradeCards` write `NpcTradeLedger`; nothing in the trade packages names `DialogueEvent`.
- Blood prices only through `BloodAccounting.pay` with the `npc_tithe` source; gates through `ProgressionGates`; all text is `en_us.json` keys, one speaker at a time against `docs/NPC_VOICES.md`.

**User rulings, trade (2026-10-31, `archive/tracks/npc_trade/00-README.md` Q-1 to Q-5):**
- Q-1: the Brined Court keeps its gift economy. Court cards are customs that earn standing; no prices (the loader rejects any Court card that is not a `custom`).
- Q-2: Unstained NPCs refuse any player at Degree 1 or above. D0 players may trade.
- Q-3: blood prices only at Harbinger NPCs.
- Q-4: the Circus (Ringmaster and performers) gets Trade in the first pass, which routes them through the hub.
- Q-5: rapport is per NPC, no spillover.

**User rulings, lore from the dialogue review (2026-10-31, `docs/NPC_VOICES.md` "Open decisions", `docs/LORE_REFERENCE.md`):**
- The Zealot/Unstained cure closes only for an unsevered bloodline founder, never by Degree; a Degree 6+ non-founder gets the plea.
- Nobody, not even the Vicar, truly knows what follows Archon; NPC lines about Silentium and Apotheosis are hearsay and legend.
- The Monolith opens at the fifth Degree on purpose, as a stand-in for a Vicar once the player is one; the Adept branch is gone.
- All Silent Archon power is D8 Silentium only.
- Our Lady's whispers grow more frequent and clearer as Clarity rises; the "late stages are quieter" reading is withdrawn.
- Pallid Vessel is the D3 Vocation assignment with the Alchemist (`PallidVesselDialogue`).
- The Unstained Scout goes through the dialogue hub (one card, `scout/paper`, and two rapport lines).

**Pronouns (applied 2026-10-24):** Vicar, Artificer and Hermit are he; Alchemist, Mnemonist and Vein-Mason are they; an Unstained speaker says "the Church", never "the Order".

## Calls made without the user

From the track files (`archive/tracks/npc_trade/`), each overturnable:
- Box order Quest Work, Inquiries, Conversation, Trade; Conversation lists one card per option instead of a single "Speak Freely" card (00-README).
- Payment is all or nothing: refused when the goods will not fit, nothing dropped, nothing pending (00-README). A shortfall after insertion is dropped at the player's feet and logged (03).
- Players on neither path (D0, not Unstained) may trade with the Unstained; stained goods are still refused at the card (00-README Q-2 note).
- Lore golden keeps the old classifier's keyword accidents (Anchorite options flagged lore because "anchorite" contains "rite"); pass 3 later un-lored the Anchorite options (`DialogueLoreFlagTest` floor 66 to 64) (01; memory of the pass-3 run).
- POOR refuses the whole trade as soon as any chosen stack grades POOR (03).
- Catalogue substitutions: Alchemist "Fresh blood" pays Bleeding Bulbs not Bloody Flasks (a 5,000 mL loop); "Ferric draw" wants filled Bloody Vials not enzymes; Mnemonist `freshness` became `specimen`; Vein-Mason's Scar Blank and the Ringmaster's topper are `never_sold`; the Voyager does not take back the Brine-Sealed Letter; only the eight Court roles that take a custom get a card; performers get one act-themed card each; no `rank` grader shipped (05).
- No lore give for the Hermit (his bread card pays 2 rapport instead); two rapport lines per speaker (Known, Trusted), no Confidant line yet; spectacle is a new `act` want, not a Circus reading of `custom`; rotation cards pay a little more and are limited to 2 uses a day (06).
- No Circus `DialogueTheme`: the Ringmaster's box uses the theme his tree already had; a finished trade plays vanilla `ITEM_PICKUP` because no dialogue sounds exist; the "Today" ribbon is drawn, not a sprite (04, 06).
- Trade data counts: 56 cards, 29 speakers, 56 rapport lines over 28 speakers (05, 06, 07).

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-35 [D5-1], D-39 [D5-5], D-43 [D5-10], D-56 [NPC-2], D-69 [D6-7], D-71 [NPC-4], D-83 [D8-4], D-85 [NPC-1], D-86 [NPC-3], D-87 [NPC-6].

The playthrough suite's pending NPC decisions (reward drops, reissue rules, the temple's fragility, consent, unique NPC loss, hints that do not name the real gate, and more) are listed with their probe tests in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and rolled up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6.

Left for the user by the trade build (`archive/tracks/npc_trade/`, memory ledger):
- Rapport lore content and the trade balance (what a card gives against what it wants) are unreviewed.
- Long card lines clip at two lines in the Trade box.
- No Circus theme or crest on the trade box (the default crest is a fallback).
- The Alchemist's daily card sits below the first screen, so the "Today" ribbon is not in the review shots (07).

From `docs/NPC_VOICES.md` "Open decisions" (still open): 8 Annetta speaks the player's line (`annetta.draught.truth.line1`); 10 successors reuse the teacher's tree past the greeting; 11 shared keys (Circus status, Unstained Observance, newcomer and item-hint templates) flatten voices; 13 the performer barks `dialogue.circus_<role>.line1-3` are dead text; 14 `votary.tendency_lore` lists enzyme names as tendencies and `item_inquiry.clairaudiograph` lines 2 to 4 are identical.

From the pass-3 writers' notes (`tools/i18n/voice_rewrite/pass3/notes/`, "## Open"):
- `artificer.md`: lore §Silent Archon puts the vestments after Silentium, but the D7 Weight of the Frame lineage offers Silent Archon at the seventh Degree; lines follow the code.
- `court_gardener.md`, `court_arbor.md`, `court_lightkeeper.md`: is `custom.opens` narration or the Gardener's voice?
- `court_braced_elder.md`: `item_inquiry.morphling_holdfast.line1` ("In time it will hold you to stone") can read as permanent rooting; cut "In time" if that reading is unwanted.
- `court_marrow_tender.md`, `court_rooted_elder.md`: the Marrow-tender's own rooting plan is kept as the tender's body, not the player's.
- `entity.md`: Archon whispers go to Degree 7+, so a D8 Apotheos or Silentium is still called "Archon" (Ancestral Communion the same); `spine_growth.degree8.canon` is never sent and would be wrong for a Silentium; `coreWitnessDialogue` Javadoc disagrees with the lore on where it fires.
- `hermit.md`: `illuminatus.hint` "Find allies who share your blood" unchecked against whether the Bloodline Covenant needs a bloodline.
- `mnemonist.md`: the 0.3% question rate comes from two player rapport buttons, left alone.
- `monolith.md`: shattering destroys the stone and loses the free Cornerstone with no warning line (adding one would be a new fact); the Degree numbering of the Monolith steps differs between lore §6.5a and §10.
- `voyager.md`: `spawn_egg_mnemonic_whale.line2` says to lead squid on a lead, unverified; should the `brine_sealed_letter` inquiry offer a chart option?
- `votary_wayfarer.md`: the Wayfarer naming "the Court" at Degree 5+ assumes the crew speaks of it openly (not blocking).

## Proof owed and known gaps

- Proved: `runNpcTradeReviewClient` ran end to end on 2026-10-31 (31 shots, every PASS line green); `npcTradeGameTestServer` 14 of 14; the loaded catalogue has `NpcTradeRegistry.errors() == 0` and exactly 56 cards.
- Not proved in a client: the "Today" ribbon (GameTest only) and the Scout's hub, added after the review run.
- `docs/NPC_VOICES.md` and the old `docs/npc_trade/` tracks matched `.gitignore` `/docs/*` and needed force-adding; the tracks now live under `docs/archive/tracks/npc_trade/`.
- The Liber has no page on the dialogue boxes, so it did not change.
- The reference's inquiry count (317) is older than the files on disk (385); refresh when §5.4 is next touched.
- Give counts against stack sizes and potion components are checked only by the live loader (the GameTest covers it).
- Shared-key voices (open decision 11) and the dead performer barks (13) are text debt, not code gaps.

## History

- 2026-11-01: the player-centric playthrough suite ([PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md)) drove the speaker table, the forged-event gates, the dead/spectator refusal, the card-state rule, the farewell trade refusal and migrations 10 and 11 above; the D3 band also made the Artificer's fork and lineage names translatable and counted an offhand blank for the Woven Vessel turn-in.

- 2026-10-24: pass 1 de-AI rewrite (1,792 keys) and pass 2 per-speaker voice sheets (1,080 keys, 16 speakers); `docs/NPC_VOICES.md` written with the contrast map and open decisions 1 to 14.
- 2026-10-31: the NPC trade build, seven tracks in order ([archive/tracks/npc_trade/00-README.md](archive/tracks/npc_trade/00-README.md)): Conversation merge and Circus in the hub ([01](archive/tracks/npc_trade/01-conversation-merge.md)), trade data model ([02](archive/tracks/npc_trade/02-trade-data-model.md)), ledger and service ([03](archive/tracks/npc_trade/03-trade-service-and-state.md)), the Trade box ([04](archive/tracks/npc_trade/04-trade-screen.md)), catalogues ([05](archive/tracks/npc_trade/05-npc-catalogues.md)), rapport lore, rotation and spectacle ([06](archive/tracks/npc_trade/06-rapport-rotation-and-demand.md)), validation and docs ([07](archive/tracks/npc_trade/07-validation-and-docs.md)). Protocol 27 to 30. Final review fixed one defect (Court cards with prices now rejected by the loader).
- 2026-10-31: pass 3 voice and lore-accuracy rewrite (606 keys; Court, Circus, trade and rapport keys), route fingerprint golden regenerated; the dialogue review produced the lore rulings above (NPC_VOICES open decisions 2, 4, 5, 9 decided); the Unstained Scout routed through the hub.
- 2026-11-01: this topic doc written; the track folder archived.
