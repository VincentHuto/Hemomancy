# Open questions and next work

Date: 2026-11-01. The one roll-up after the October passes (Saint mausoleums and their models, the Brined Court, the D8
split with possession, NPC trade and the dialogue clean-up). Everything those passes built is committed on `neo-1.21.1`
(`185aa3502` to `ea7bd732d`); the gaps are review, decisions and proof, not landing code.

Each item names its topic doc, where the full context sits. When a question is answered, record the answer in that
topic doc under "Rulings that bind" and delete the row here. Architecture decisions keep their ids from
`architecture/CONSOLIDATION_PROGRESS.md` (its Decisions table has each one's exact change).

Sections:

1. [Needs your call](#1-needs-your-call): nothing can move until you decide.
2. [Decided without you, awaiting review](#2-decided-without-you-awaiting-review): a session chose; kept until you have seen it.
3. [Proof owed](#3-proof-owed): built and tested, never watched in a client.
4. [What to work on next](#4-what-to-work-on-next): the recommended order.

---

## 1. Needs your call

### 1.1 Design calls that block content

| # | Question | Default if unanswered | Topic doc |
| --- | --- | --- | --- |
| D-1 | **Saint gifts as learnable powers.** Each Saint's gift is a full rework of its Canon Memory. Nothing is designed; the recipes only gained the Recollection as a second catalyst. | Stays as today: Canon Memories unchanged, Recollection required. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| D-2 | **Per-Saint combat and visual overhauls** (you stressed each gets its own). Order? Hemorath (drain, leaking meter, iron spikes), Seraphae (nervous wings, white light), Putriciel (fire only in the cleansing interval, no red), Velorum (stillness meter, wakeable congregation). | Hemorath first: his Downfall hall and wells are already in the layout. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| D-3 | **Silentium D8 powers.** What Mind Broken diminishes and what a Silentium gains over a D7 Archon beyond the Silent Archon set, now that all Silent Archon power is D8-only. | As built: the vestment set, claimed-Will bonus and death refusal; nothing more. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-4 | **Morphic tonic.** Which beneficial effect, and which vessel. Built as a bloody jug in morphic nectar giving Regeneration I for 10 s, drinkable anywhere. | Keep the built version. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-5 | **Molting.** What an Apotheos can re-shape and at what cost. Not built. | Parked. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-6 | **Chimerization offering.** The offering item's name and recipe. | Parked. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-7 | **The Core and the heir thread.** Confirm the Core as the Mycophant's domain; `mycophant_heir` is written and never read. | Leave inert until the Core is designed. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md), [BOSSES.md](BOSSES.md) |
| D-8 | **Visitation's cost.** It takes 2 spore sacs in the braziers and skips the Spine's 500-blood travel cost. Design call flagged by the overnight run. | Keep the sacs; the Spine cost is for the first crossing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-9 | **PvP possession.** A possessed Silentium cannot attack players (one cancelled event). Wanted, or should possession be a PvP form? | Keep the block. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| D-10 | **Rite spaces need members.** Only the Sanguine Omen is rite-bound. Which blocks, items and entities should be? | Nothing more until a rite-bound feature is designed. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| D-11 | **Unstained rites.** The purpose grammar is Harbinger-only; the Unstained get their own ritual system later. When, and on what model? | Parked. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| D-12 | **Brined Court elder challenges and fuller Court trading.** The Champion title gives recognition only; elders challenging the player, and trades beyond customs, were deferred by you. | Parked. | [BRINED_COURT.md](BRINED_COURT.md) |
| D-13 | **Wake nursery.** Keep it beside the natural Shared Wake schools, or drop it? Also final Seat names and the eight house marker designs. | Keep; names and markers stay as built. | [BRINED_COURT.md](BRINED_COURT.md) |
| D-14 | **Circus deferred pool.** The underwater boundary doc lists deferred items (sea-cucumber forms, siphonophores, whale-fall culture). Most are now the Court's; confirm what, if anything, is still owed to the Circus suite. | Nothing. | [CIRCUS.md](CIRCUS.md) |
| D-15 | **Unstained Scout and the hub.** Routed through the hub on 2026-10-31; does she get trade cards and lore lines of her own? | No cards; the Unstained refuse Harbingers anyway. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| D-16 | **Mycophant pronouns.** The rebuild spec chose she/her for player-facing text while the entity is a genderless brood-mother. Keep she/her in text, or move to they/them? | Keep she/her in text; genderless stays canon. | [BOSSES.md](BOSSES.md) |
| D-17 | **Saint epithets.** `en_us.json` and the presentation spec disagree (Hemorath "The First Saint" vs "The Hollow Vessel", a name you retired). Which wording is canon per Saint? | Lang wins; drop "Hollow Vessel" everywhere. | [BOSSES.md](BOSSES.md), [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| D-18 | **Circus candidates still unbuilt:** midgame mini-bosses toward the Ringmaster and the snake creature (C-13), the Ringmaster's staff form gated on a hostile route never designed. Build, or strike? | Strike until the Circus gets its own pass. | [CIRCUS.md](CIRCUS.md) |
| D-19 | **Manipulation balance §9 opens:** Animus as the larger default school, Thread Ripper Circus-only or general, Saint memories counting toward tier coverage, Sanguine Tempest's extra Neurotic enzyme. | As built. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |
| D-20 | **Casting v2 §16 "(user)" decisions** never answered: restraint level, quick-cast flick (dropped by the builder), Tier B scope, Still Art ground ring, animation library. | As built. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |
| D-21 | **Working-craft offerings.** 14 of the 20 Working crafts got first-pass offerings "that can be tuned freely"; no balance pass since. | As built until the play-proof pass says otherwise. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| D-22 | **Vent Sanctum threshold chimney:** usable by the player or not, and what happens to someone who enters Hydra. Plus whether other players' Morphling mutations should always be visible (no client setting today). | Not enterable; always visible. | [BRINED_COURT.md](BRINED_COURT.md) |
| D-23 | **Monolith shattering** loses the free Cornerstone with no warning line. Add a line, or keep the loss silent? | Add a warning line. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |

### 1.2 Mausoleum calls (from `archive/tracks/mausoleums/12-next-pass.md` §3.8)

| # | Question | Default |
| --- | --- | --- |
| M-1 | Hematic iron plating in `ductilis_conductors` / `laboring_arms_resistant`? | No: plating is architecture. |
| M-2 | Charred planks in any planks or wooden tag? Lacquer stays out. | No, same as lacquer. |
| M-3 | Hemorath's heartbeat quickens toward the pit (70 to 45 ticks, pitch drops). Keep? | Keep. |
| M-4 | Sound picks once heard: Seraphae choir (beacon hum), Putriciel creak (`mud.step`), Velorum creak (glass or amethyst). | Decide in the played session. |
| M-5 | `/hemo test mausoleum`: production command or dev-only? | Dev-only. |
| M-6 | Retire the disabled legacy `mausoleum` structure (its JSON, pools and the ten NBTs)? | Retire. |

### 1.3 Canon conflicts left by the docs pass (overnight report, 2026-10-28)

| # | Conflict | Default |
| --- | --- | --- |
| C-1 | The Saints are still "post-release WIP" in LORE_REFERENCE (Release scope), HEMOMANCY_REFERENCE §38 and `wiki/Public-Alpha-Readiness` (pinned by a test), while the four complexes generate. | Promote to "implemented, overhauls pending" and update the pin. |
| C-2 | "Severance" means the Qliphoth cut, but the Unstained founder cure route and the Silent Archon armour ability `silent_severance` still use the word. | Rename the armour ability; the cure route keeps its name. |
| C-3 | The sarcophagus has no D8 gate: a relic, Foul Paste or a misaligned vial can wake a Saint at any degree. The D8 gate is the seal and door in front of it. | Accept: the door is the gate. |
| C-4 | LORE_REFERENCE §3.3 "Transcendence (Late/DLC)" overlaps the Apotheos identity. | Fold into the Apotheos section. |
| C-5 | Velorum keeps internal `MARTYR` names in code; `wiki/Harbinger-Path` says the Archon severs bloodline members where LORE says the D6 leader does. | Rename and fix the wiki line. |
| C-7 | LORE_REFERENCE §11 says the Priestess's lancea shares the standard blood-lance path; the reference describes a dedicated `StainedLanceEntity`. The reference also still lists Annetta as "Partial". | Fix the lore line; promote Annetta. |
| C-8 | AGENTS.md names three special rites (Apotheos, Bloom, Ancestral) but `CardinalRitePurposeDataTest.SPECIAL` holds four since Possession joined. There is no Apotheos section in the Liber. | Update AGENTS.md; add the Liber section with D-1. |
| C-6 | Recollections are tradeable and the Loom has no degree gate; `mausoleum_treasure` still drops `memory_crimson_tithe` directly. | Add `recollection_*` to `never_sold`; gate the canon recipes at D8. |

### 1.4 Dialogue writers' questions (pass 3 notes, `tools/i18n/voice_rewrite/pass3/notes/`)

- Should the Brine-Sealed Letter's item inquiry offer a chart option? It used to.
- `spawn_egg_mnemonic_whale.line2` says to lead squid into its waters; nothing in the mod makes squid leashable.
- `custom.opens` is a trade-card label: narration, or the Gardener's voice?
- `FungalWhisperEvents` sends the Archon whispers to Degree 7 and above, so a D8 Apotheos or Silentium is still called "Archon" in them; Ancestral Communion has the same `required_degree` issue.
- Holdfast inquiry line "hold you to stone" can be read as permanent rooting; the player's anchoring is temporary.
- `NPC_VOICES.md` open decisions 8, 10, 11, 13 and 14 still stand: Annetta speaks the player's line, successors reuse the teacher's tree, shared keys flatten voices, dead performer barks, the Alchemist's slips.
- The Artificer offers the Silent Archon vestments at D7 in the Weight of the Frame lineage while lore now puts them after Silentium; the lines follow the code. Decide which moves.
- `spine_growth.degree8.canon` is never sent and reads wrong for a Silentium; `coreWitnessDialogue` Javadoc disagrees with lore.

### 1.5 Architecture decisions still PENDING (ids from the consolidation ledger)

Balance and design: G-1/F-18 (explicit damage stages), F-16 (attribute AoE damage, respect allies and PvP), G-2b (the
caster's own puppets stop matching `Monster.class` filters), G-2c (one boss definition), G-7b (flight revoke checks other
sources), F-13 (scar upkeep reserve-protected), F-14 (passive scar effects survive milk), F-15 (living weapons check
`isActive()`), F-17 (Curved Horn uses `LastRiteHelper`), F-11 (Blood-structure crafts tick once per server tick).
State and delivery: F-4, F-5, F-19, F-20, S-2b, S-5b, F-12. Visuals: A-4 (freeze behaviour per animation), V-3b
(Ferric 128 vs 127), V-3c (Outpouring marks through `MaterialQuads`), V-6b (Mycophant spore colours). Housekeeping: M-2
and M-3 (dead mobs and dead code), O-5 (typed components), F-22 (legacy Dictation Table), F-25 (MnA config read).
None blocks content; F-16 and G-2b change what spells hit and deserve a decision before the next balance pass.

### 1.6 Playthrough suite decisions (2026-11-01, [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions))

Each is a `required = false` probe in `playthrough_validation` that fails until you rule (a few late rows are evidence
logged inside a passing test); the topic doc has the evidence and the probe test names. Ids are `PT-` plus the tests'
`// DECISION: D-n` number. PT-35 on (2026-11-02) are the D5 to D8 bands; the bracket gives the band id the test names
(D5, BC Brined Court, CT Crimson Troupe, D6, D7, D8, NPC, CHAIN for the late run).

| # | Question | Default if unanswered | Topic doc |
| --- | --- | --- | --- |
| PT-1 | Living-creature absorption is 1 mL per pulse: about 50 cows for the D1 500 mL proof. Raise the rate, lower the proof, or point the hint at reservoirs? | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-1b | Absorbing your own projected blood back out of your cauldron meets the proof with no creature. Intended? | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-2 | A full vessel's absorption counts 0 mL toward the proof though the hint says everything counts. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-3 | One-time NPC rewards (7 of 10) and shared station outputs drop unowned, so another player can take them. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-4 | The Mason's Respite map is never reissued. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-5 | The D5 Vicar gives the Monolith blueprint, but the Monolith is a D6 craft. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-6 | Any Blood Shot weave opens the Brotherhood; the Mnemonist's indexing is optional. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-7 | The Vicar's D3 and D4 hints do not name the real gate (the Loom weave; the Vein-Mason loadout). | As built; the probe stays failing. | [NPC_VOICES.md](NPC_VOICES.md) |
| PT-8 | The temple is fragile (breakable Display, re-offered heart, another temple's Hermit's dead farewell); PT-8b an awakened player at an unclaimed Display is sent to a Hermit who cannot bless them. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-9 | Founder-led reception asks the recruit nothing. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-10 | A killed Respite Anchorite never returns: a D4 softlock. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-11 | D3 needs a server night skip; one awake player blocks it with no line. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-12 | The outpost Alembic has no heat and no line says it needs heat. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-13 | Should outpost stations need a personal craft at all? (The ledger itself reads 8/8.) | As built; the probe stays failing. | [STATION_UPGRADES.md](STATION_UPGRADES.md) |
| PT-14 | The Vein-Mason lesson follows the live tendency: refused carves and free extra patterns. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-15 | The Hermit Road report is D1-only; a later remnant leaves 1/2 forever. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-16 | Creative discard of the starter kit still records the one-time claim. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-17 | Logging out during a Venous Stone collapse or a pending structure craft loses the proof. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-18 | A second assigned spin overwrites the First Separation proof. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-19 | Reissues check only the carried inventory (stashed or dropped vials are reissued). | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-20 | A weaver offline at the Loom climax loses the first-weave milestone. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-21 | Nothing points a D3 player to the Circus that D3 puppetry needs. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-22 | A section already Varicose at D5 never counts as the Mason's strain. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-23 | An obstructed outpost marker silently drops its resident. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-24 | A guided Chamber visit left early (logout, death protection) completes the lesson. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-25 | The dispel refund is always the 90% cap, whatever the wear. | As built; the probe stays failing. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |
| PT-26 | The charm taken off while Concentrated Blood is pending leaves the player at D2 with no explanation. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-27 | The D5 Monolith scrap can be taken again and again. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-28 | A refused reward can leave the client on a "claimed" node with no line (not reachable for the Anchorite's D5 reward). | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-29 | With vascular degradation off in the server config, the D5 Mason strain cannot be finished. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-30 | A lost scar blank before the first carve cannot be replaced. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-31 | No NPC names the D4 Sporitic Thurible. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-32 | A D4 player at a D5 Covenant floor is told no rite matches the structure, not that it needs D5. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-33 | The Specimen Jar lesson is offered only from the D2 tree; a D3 player who skipped it never sees it. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-34 | Pending blood crafts count down once per loaded level (about 3x early). Same as F-11 in 1.5. | As built; the probe stays failing. | [architecture/CONSOLIDATION_PROGRESS.md](architecture/CONSOLIDATION_PROGRESS.md) |
| PT-35 [D5-1] | A signed ledger joins anyone (D0, inactive blood), with no leader consent. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-36 [D5-2] | Members can recruit and mint ledgers but cannot leave or manage the line. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-37 [D5-3] | A kicked member's old ledger does nothing and says nothing. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-38 [D5-4] | The warning-only first Unbinding still burns its Nether Star; Disband is free. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-39 [D5-5] | No NPC says where Sanguine Quintessence comes from. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-40 [D5-6] | The D5 Monolith gives no guidance; a held item hides its tree. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-41 [D5-7] | The Basin commission reads only the main hand; commissions give no reward. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-42 [D5-8] | A deposit into a full pool earns no proof and says nothing. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-43 [D5-10] | The Monolith blueprint is offered only at exactly D5 (the forged-event guard copies that gate). | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-44 [D5-11] | An offline member's share of the pool is lost when the line is dissolved. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-45 [BC-1] | A custom before the site visit locks the player as a guest. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-46 [BC-2] | Escaping a usurpation by logout costs no wait; dying costs a day. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-47 [BC-3] | Ask to join spends the free switch with no warning. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-48 [BC-4] | The Champion challenge has no confirmation. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-49 [BC-5] | Nothing points to the Shared Wake's nursery. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-50 [BC-6] | Nothing locates the Vent Sanctum or Court outposts. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-51 [BC-7] | The Lightkeeper's feeding line misdescribes the bonus. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-52 [BC-8] | The Champion has no degree gate. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-53 [BC-9] | With Pelagic worldgen off, NPCs still point to the Court. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-54 [BC-10] | No Court NPC speaks past D5 or about Primal. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-55 [BC-12] | Court members can be leashed; the Lamp-colony's Leads tie up the next member clicked. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-56 [NPC-2] | The custom trade card pays rapport; the dialogue offering does not. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-57 [CT-1] | The idle Ringmaster's perch cannot be reached from the pavilion floor. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-58 [CT-2] | Nothing points to the abandoned stage. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-59 [CT-3] | Practicals cannot finish in Peaceful. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-60 [CT-5] | The Threadkeeper's lesson node is unreachable. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-61 [CT-6] | The claimed and not-ready finale lines mislead. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-62 [CT-7] | A lost Ringmaster never settles the site. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-63 [CT-8] | One killed rehearsal threat stalls a two-threat practical silently. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-64 [D6-1] | Only a progenitor binds a Covenant Throne. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-65 [D6-2] | Any progenitor binds any Throne. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-66 [D6-3] | Eternal Covenant is lost on death and repeatable (S-1 family). | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-67 [D6-5] | The Chamber rite attunes even when no visit can start. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-68 [D6-6] | A Formation medium seats only on a sneak-click, with no hint. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-69 [D6-7] | A lineless D6 is still sent to bind a Throne to their Fane. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-70 [D6-8] | A dissolved line's progenitor keeps the Throne bond, unmentioned. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-71 [NPC-4] | The D6 Anchorite counsel is a lore card, not a quest. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-72 [D7-1] | Casting and rites lock from the ninth pome to D8, undocumented. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-73 [D7-2] | Any D7 can shatter another player's Monolith. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-74 [D7-3] | A low shatter roll before the cornerstone leaves the Weight of the Frame short. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-75 [D7-4] | Logging out mid-projection grants the revelation. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-76 [D7-5] | No D7 NPC names the shatter, Seed or Bloom. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-77 [D7-6] | The atlas shows D7/D8 materials at D5. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-78 [D7-8] | Nothing names the Fungal Spine after Communion. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-79 [D7-9] | Nothing names the Severance to a D7 Silence. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-80 [D8-1] | A heart-node respawn can trap an Apotheos in the Gardens. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-81 [D8-2] | A Mycophant win is lost to a logout or death during the yield. | As built; the probe stays failing. | [BOSSES.md](BOSSES.md) |
| PT-82 [D8-3] | The Rite of Apotheos is not discoverable when it is needed. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-83 [D8-4] | A D8 Silentium is greeted with the D7 Archon tree. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-84 [D8-5] | The Mycophant rematch line promises a retry that never comes by itself. | As built; the probe stays failing. | [BOSSES.md](BOSSES.md) |
| PT-85 [NPC-1] | A ledger summon strips a shared outpost NPC for good. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-86 [NPC-3] | The D5/D6 Main steps are a one-read lore card, not quests. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-87 [NPC-6] | The default bloodline name is stored English. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-88 [CHAIN-1] | A solo caster deadlocks in a grand rite's ordeal (dry, damaged woken rings). | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-89 [CHAIN-2] | The revelation choice waits behind memo whispers in the inbox. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-90 [CHAIN-3] | Bloodline members can never reach D7 (no leave, no Throne, no second line). | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |

---

## 2. Decided without you, awaiting review

Sessions made these calls because you were away or said "go on your guess". Each stays until you have seen it in a
client and kept or changed it. The topic docs list the full set; these are the ones that change play or look.

| Area | The call | Topic doc |
| --- | --- | --- |
| Heart-node | Cut-off synapses keep working (leapfrog risk); no withered-heart gate; no support rule; toggle labels Strangers / Leave line / Spores; slider notches Hint..Carpet; no Liber line. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| Apotheos body | Spore gain 2/s in the dimension, 0.5/s in the Overworld biome; Burn Spores 25 load for 6 HP on a 100-tick cooldown; morphling symbiotic split off for an Apotheos. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| Possession | Surrogate stats per form (bat 3 ... golem 14); hit cost 1 (2 at 6+ damage); Mind Broken 6000 ticks with no cure; Mind Broken applied at the return's cut; the rite sits in the Ancestral family; bat hangs when stopped under a ceiling. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| Gaze and grab, adipocere | Gaze drains 1 Concentration per 20 ticks within 10 blocks with sight; grab ejects with Mind Broken 12000; 35% bog revenant drop; +3 max Concentration as an offering. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| Chapels and door | Trial numbers (Beholding 4 flashes in 400 ticks, Litany 3 of 6 items, Downfall 3 waves of 3, Repose 600 progress); ring door opened by held Blood Projection, 200 fed ticks at 5 blood (1000, changed in `62f5b38da` from 3 x 300); enrage bonuses per skipped Chapel; 3 fragments craft one residuum; Seraphae biomes swap plains for flower forest; Recollection cyst rarity 1 in 24 chunks. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| Mausoleum layout | The spiral fold-back under the entrance, chamber at -39, ward only past the threshold, set pieces unbreakable, every theme's depth grading and lamp picks. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| Brined Court | The Lighthouse Garden raises its own tide rock; Mermaid's Purse +35% against the Prismatic cap; Ink Cap Primal hood still shows the cap; polyp ocean spawns 1 in 40 with 48-block spacing; nursery rarity 1 in 6. | [BRINED_COURT.md](BRINED_COURT.md) |
| NPC trade | Box order Quest Work, Inquiries, Conversation, Trade; one card per Conversation option; payment all or nothing; rapport lore content and balance; D0 players may trade with the Unstained; catalogue substitutions (Bleeding Bulbs not Flasks, vials not enzymes, Scar Blank never sold, only 8 Court roles get cards); only Known and Trusted rapport lines, no Confidant; daily cards pay more and cap at 2 uses. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| Possession rite | Blood cost 0 (the spec's 400 dropped because a test forbids a lump Harbinger blood cost) and filed in the Ancestral recipe-map family; possession borrows the Qliphoth grand floor as a special bond rite. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md), [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| Circus trade and visuals | The "finale's leavings" card dropped, the D4 bell carrot, act-themed performer cards; the Bellows, Juggler, Hulk, faculty and camp rebuilds were authored from previews only. | [CIRCUS.md](CIRCUS.md) |
| Boss presentation | Every frame and opening shot and their lengths; title card on the lower third (the spec and `TitleCard` agree; an earlier note said upper); 50-tick phase cards, unskippable; bars hidden under the opening; skip by holding sneak 20 ticks. | [BOSSES.md](BOSSES.md) |
| Boss fights | Vesper's wound sites and rates, the Sickle act at 33%, whether a grounded burrow is in the rotation; Mycophant band and weight numbers, the 8% fusion bonus, the Nursery ceiling raise; Annetta's pools and 1 HP guard. The brood clutch and amber failure beat are not built. | [BOSSES.md](BOSSES.md) |
| Rite merge stages 2 to 4 | The Rites & Crafts tab layout, special floor styles 4 to 6, the Sanguine Omen as the only rite-bound member. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| Cast impacts, basics, infusion | Every design call: delivery x school geometry, the 5-tick impact throttle, the six body-anchored forms, the sore and melt overlays. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |
| Creative tab | Section filing of ambiguous items (puppet mounts to Troupe, Silentium and possession to Fungal Gardens, bone bricks and gutters to Saints, Verdigris moth to Unstained). | `CreativeTabSection`, `runCreativeTabReviewClient` |
| Fane ground | SURFACE style as the default, floors only, outsider tint unchanged, distant domes still separate bubbles per stake. | `FaneGroundRenderer`, `faneGroundReviewClient` |
| Wayside and Harbinger sites, Brined Votary and heart-node remodels | Rebuilt 2026-10-31 (`b219f836b`) on the structure quality bar; not yet walked by you. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) for the bar; `tools/worldgen/harbinger_sites.py`, `wayside_sites.py` |

---

## 3. Proof owed

Built and covered by JVM tests, GameTests and review-client stills, but never watched in motion, heard, or played by
you. One played session per area, with a findings log, closes most of these.

- **Mausoleums** (`archive/tracks/mausoleums/12-next-pass.md` §3.2): every trial completed solo per Saint; set pieces
  mid-trial; the hallowed-ground burn layer; `SCENE_SAINT_RISE`; Seraphae's anchors waking; Putriciel's purge ring
  and pyre/shelf switch; Velorum's frost pulse; all ambience; natural worldgen for all four Saints after the entrance
  check widened to 14 blocks. Also §3.3 lighting and §3.4 art gaps.
- **Endgame D8 journey**: an Apotheos run (Rite of Apotheosis, spore body, heart-node planting, synapses, spread
  shader, death cache) and a Silentium run (Memory of Vesper, possession rite with the brazier offerings, each of the
  nine forms, the wheel, the four cinematics, gaze and grab, Visitation). Nothing of this has been seen by you.
- **Brined Court**: a hand walk of a fresh world (Seats, outposts, residents, the Bloom Warden on a transgression, the
  Champion usurp), the two-client strand sync check, a full `alphaCheck` run with `court_worldgen_validation`.
- **NPC trade**: the Trade box on each side (Harbinger blood tithe, Unstained refusal at D1, Court custom, Circus
  curiosity), the Conversation box with lore glyphs, the Alchemist's daily card below the first screen.
- **Boss presentation**: every bar frame and opening scene (`runBossPresentationReviewClient`), the Mycophant waking
  and capture scenes, the Saint rise.
- **Casting**: cast impacts, the body-anchored basics and infusion veins in a played session, not the harness; neither balance pass has been playtested.
- **Heart-node**: `heartNodeReviewClient` passed 18 checks on 2026-10-29, but the screen, spread, set-piece and possession scenes were added afterwards and need a GPU launch.
- **Mausoleum code**: Putriciel's Java never had a final fresh reviewer pass; the raised descent vault is unchecked in the review client; the Succession test `legacyVialsAreRejectedBeforeConsumption` may still read a brazier at a stale offset.
- **Known test noise**: two Succession opt-in failures; the flaky False Omens deadline and stronger-prey tests; the
  combined `alphaCheck` flakes about one test in two runs. Give grid-sensitive tests their own batch.

---

## 4. What to work on next

Recommended order. Each step is one or two sessions and ends with a findings log and a topic-doc update.

1. **Play-proof the October work, one area per session, logging what fails and what reads badly.** Order: Saint
   mausoleums (§3.2 list, one Saint per session), the Apotheos D8 journey, the Silentium D8 journey, a Brined Court
   fresh-world walk, NPC trade boxes, boss openings. This is where the "decided without you" rows get kept or changed,
   so take section 2 with you. Fix in place only what blocks the walk; everything else goes on the log.
2. **Answer the blocking design calls** (section 1.1, D-1 to D-15) and the mausoleum calls (1.2), ideally as one
   written round like the Brined Court Q-rounds. D-1, D-2 and D-3 shape the next content pass; the rest can take their
   defaults.
3. **Clear the canon conflicts** (1.3) and the dialogue writers' questions (1.4) in one docs-and-lang session:
   LORE_REFERENCE, HEMOMANCY_REFERENCE §38, the wiki pins, the `silent_severance` rename, the whisper degree gate.
4. **Green baseline:** `alphaCheck` plus the opt-in suites the October work touched (`court_worldgen_validation`,
   `saint_power_validation`, `succession_validation`), fixing the flakes named in section 3 rather than rerunning past
   them.
5. **Then new content, in this order unless step 2 changes it:** the per-Saint overhauls (D-2) with the Saint gifts
   (D-1); Vesper's congregation thaw and Hemorath's swelling (owed to the Saint awakening); rite-bound content (D-10);
   Court elder challenges (D-12); the Canon Memory and Recollection gate (C-6).
6. **Housekeeping debt, in the gaps:** the pending architecture decisions (1.5) as a batch, 335 legacy `main()` tests,
   the private-helper and `getPersistentData()` ratchets, the migrations that sunset on 2027-04-23, the deferred-minor
   lists in each topic doc.
