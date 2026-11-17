# Open questions and next work

Date: 2026-11-01. The one roll-up after the October passes (Saint mausoleums and their models, the Brined Court, the D8
split with possession, NPC trade and the dialogue clean-up). Everything those passes built is committed on `neo-1.21.1`
(`185aa3502` to `ea7bd732d`); the gaps are review, decisions and proof, not landing code.

Updated 2026-11-06 with your answers to D-1, D-3 to D-10, D-13, D-15 to D-17 and D-19: each is recorded in its topic
doc's "Rulings that bind" and built uncommitted; D-26 to D-28 are follow-ups those answers raised.

Updated 2026-11-07: §1.7 holds the Crimson Troupe audit's owner calls (D-29 to D-46).

Updated 2026-11-11 (round two P4 added CQ-15 to CQ-19; P5 built CL-27 and CQ-18 and added CQ-20 to CQ-26; P6 added CQ-27 to CQ-33; P7 added CQ-34 to CQ-40; P8 added CQ-41 to CQ-44; P9 added CQ-45 to CQ-49; P10 added CQ-50 to CQ-55; P11 added CQ-56 to CQ-62): §1.8 holds the core loop overhaul's owner decisions (CL-1 to CL-27, built to their
recommendations and not yet ruled) and the calls its tracks raised (CQ-1 to CQ-14); §2 and §3 gain a core-loop row each.

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
| D-1 | **Review the Canon Memory rework draft.** You ruled on 2026-11-06 that the four Canon Memories need complete reworks; a draft design for all four (ranks, the trial record, the C-6 gate, each gift's mechanics and visuals) is in [the spec](superpowers/specs/2026-11-06-canon-memory-rework-design.md) with 17 numbered questions. | Nothing is built until you approve it. | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) |
| D-18 | **Circus candidates still unbuilt:** midgame mini-bosses toward the Ringmaster and the snake creature (C-13), the Ringmaster's staff form gated on a hostile route never designed. Build, or strike? The 2026-11-06 audit is that pass and asks the C-13 half again as D-33. | Strike until the Circus gets its own pass. | [CIRCUS.md](CIRCUS.md) |
| D-21 | **Working-craft offerings.** 14 of the 20 Working crafts got first-pass offerings "that can be tuned freely"; no balance pass since. | As built until the play-proof pass says otherwise. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| D-22 | **Vent Sanctum threshold chimney:** usable by the player or not, and what happens to someone who enters Hydra. Plus whether other players' Morphling mutations should always be visible (no client setting today). | Not enterable; always visible. | [BRINED_COURT.md](BRINED_COURT.md) |
| D-26 | **Lux Summa gap.** With Saint memories out of tier coverage (D-19), Lux has no Summa entry: the Unclosing Eye was its only one. Add a Lux Summa manipulation, or re-rank one of Lux's four Mediocritas entries (Conjure Spear, Prismatic Reproof, Hematic Beacon, Lumen Suture)? | Leave the gap until the Canon Memory rework settles the Eye's rank. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |

### 1.2 Mausoleum calls (from `archive/tracks/mausoleums/12-next-pass.md` §3.8)

| # | Question | Default |
| --- | --- | --- |
| M-4 | Sound picks once heard: Seraphae choir (beacon hum), Putriciel creak (`mud.step`), Velorum creak (glass or amethyst). | Decide in the played session. |
| M-5 | `/hemo test mausoleum`: production command or dev-only? | Dev-only. |
| M-6 | Retire the disabled legacy `mausoleum` structure (its JSON, pools and the ten NBTs)? | Retire. |

### 1.3 Canon conflicts left by the docs pass (overnight report, 2026-10-28)

| # | Conflict | Default |
| --- | --- | --- |
| C-2 | "Severance" means the Qliphoth cut, but the Unstained founder cure route and the Silent Archon armour ability `silent_severance` still use the word. | Rename the armour ability; the cure route keeps its name. |
| C-5 | Velorum keeps internal `MARTYR` names in code; `wiki/Harbinger-Path` says the Archon severs bloodline members where LORE says the D6 leader does. | Rename and fix the wiki line. |
| C-8 | AGENTS.md names three special rites (Apotheos, Bloom, Ancestral) but `CardinalRitePurposeDataTest.SPECIAL` holds four since Possession joined. There is no Apotheos section in the Liber. | Update AGENTS.md; add the Liber section with D-1. |

### 1.4 Dialogue writers' questions (pass 3 notes, `tools/i18n/voice_rewrite/pass3/notes/`)

- `spawn_egg_mnemonic_whale.line2` says to lead squid into its waters; nothing in the mod makes squid leashable.
- `FungalWhisperEvents` sends the Archon whispers to Degree 7 and above, so a D8 Apotheos or Silentium is still called "Archon" in them; Ancestral Communion has the same `required_degree` issue.
- `NPC_VOICES.md` open decisions 10, 11 and 13 still stand: successors reuse the teacher's tree, shared keys flatten voices, dead performer barks.

### 1.5 Architecture decisions still PENDING (ids from the consolidation ledger)

Balance and design: G-1/F-18 (explicit damage stages), F-17 (Curved Horn uses
`LastRiteHelper`). State and delivery: F-5, F-12 (waits on NQ-13-2). Visuals: A-4
(freeze behaviour per animation). Housekeeping: M-2 (dead mobs), F-25 (MnA config read). G-7b, F-13, F-14, F-15, F-4, F-19,
F-20, S-2b, S-5b, M-3, O-5, F-22, G-2c (with CQ-12), V-6b, G-2b, F-16, F-11, V-3b and V-3c were approved on 2026-11-13
and built on 2026-11-14; the ledger's Decisions table records each.
None blocks content; G-1/F-18 changes blood lost per hit and waits on NQ-17-1 and NQ-17-2.

### 1.6 Playthrough suite decisions (2026-11-01, [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions))

Each is a `required = false` probe in `playthrough_validation` that fails until you rule (a few late rows are evidence
logged inside a passing test); the topic doc has the evidence and the probe test names. Ids are `PT-` plus the tests'
`// DECISION: D-n` number. PT-35 on (2026-11-02) are the D5 to D8 bands; the bracket gives the band id the test names
(D5, BC Brined Court, CT Crimson Troupe, D6, D7, D8, NPC, CHAIN for the late run).
PT-32, PT-34, PT-67 and PT-68, and the PT-88 ruling (2026-11-13, built 2026-11-14), are under "Rulings that bind" in
[CARDINAL_RITES.md](CARDINAL_RITES.md), with D-11; D-6 is under "Rulings that bind" in [ENDGAME_PATHS.md](ENDGAME_PATHS.md).
PT-6, PT-12, PT-17, PT-20, PT-24 and PT-33, and PT-4, PT-14, PT-16, PT-22 and PT-28 (2026-11-13, built 2026-11-14), are
under "Fixes the suite drove" in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md); PT-45 to PT-48, PT-52, PT-53, PT-55 and PT-56 (2026-11-13,
built 2026-11-14) are there too and under "Rulings that bind" in [BRINED_COURT.md](BRINED_COURT.md); PT-39, PT-43, PT-71 and PT-86, with D-23, under "Rulings that bind" in
[NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md); PT-7 in the Vicar's sheet in [NPC_VOICES.md](NPC_VOICES.md). The
`complete_vigil` half of PT-86 was not part of the ruling and stays open in PLAYTHROUGH_TESTS.md (D-86).

| # | Question | Default if unanswered | Topic doc |
| --- | --- | --- | --- |
| PT-1 | Living-creature absorption is 1 mL per pulse: about 50 cows for the D1 500 mL proof. Raise the rate, lower the proof, or point the hint at reservoirs? | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-1b | Absorbing your own projected blood back out of your cauldron meets the proof with no creature. Intended? | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-2 | A full vessel's absorption counts 0 mL toward the proof though the hint says everything counts. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-3 | Shared station outputs hand their result to whoever is there. (The seven one-time NPC rewards are `PendingDelivery` claims since 2026-11-14; the station half waits on NQ-10-10.) | As built; the station probes stay failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-5 | The D5 Vicar gives the Monolith blueprint, but the Monolith is a D6 craft. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-8 | The temple is fragile (breakable Display, re-offered heart, another temple's Hermit's dead farewell); PT-8b an awakened player at an unclaimed Display is sent to a Hermit who cannot bless them. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-9 | Founder-led reception asks the recruit nothing. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-10 | A killed Respite Anchorite never returns: a D4 softlock. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-11 | D3 needs a server night skip; one awake player blocks it with no line. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-13 | Should outpost stations need a personal craft at all? (The ledger itself reads 8/8.) | As built; the probe stays failing. | [STATION_UPGRADES.md](STATION_UPGRADES.md) |
| PT-15 | The Hermit Road report is D1-only; a later remnant leaves 1/2 forever. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-18 | A second assigned spin overwrites the First Separation proof. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-19 | Reissues check only the carried inventory (stashed or dropped vials are reissued). | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-23 | An obstructed outpost marker silently drops its resident. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-25 | The dispel refund is always the 90% cap, whatever the wear. | As built; the probe stays failing. | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) |
| PT-26 | The charm taken off while Concentrated Blood is pending leaves the player at D2 with no explanation. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-27 | The D5 Monolith scrap can be taken again and again. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-29 | With vascular degradation off in the server config, the D5 Mason strain cannot be finished. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-30 | A lost scar blank before the first carve cannot be replaced. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-31 | No NPC names the D4 Sporitic Thurible. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-35 [D5-1] | A signed ledger joins anyone (D0, inactive blood), with no leader consent. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-36 [D5-2] | Members can recruit and mint ledgers but cannot leave or manage the line. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-37 [D5-3] | A kicked member's old ledger does nothing and says nothing. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-41 [D5-7] | The Basin commission reads only the main hand; commissions give no reward. | As built; the probe stays failing. | [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) |
| PT-44 [D5-11] | An offline member's share of the pool is lost when the line is dissolved. | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-49 [BC-5] | Nothing points to the Shared Wake's nursery. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-50 [BC-6] | Nothing locates the Vent Sanctum or Court outposts. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-51 [BC-7] | The Lightkeeper's feeding line misdescribes the bonus. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-54 [BC-10] | No Court NPC speaks past D5 or about Primal. | As built; the probe stays failing. | [BRINED_COURT.md](BRINED_COURT.md) |
| PT-57 [CT-1] | The idle Ringmaster's perch cannot be reached from the pavilion floor. | As built; the probe stays failing. | [CIRCUS.md](CIRCUS.md) |
| PT-64 [D6-1] | Only a progenitor binds a Covenant Throne. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-65 [D6-2] | Any progenitor binds any Throne. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-66 [D6-3] | Eternal Covenant is lost on death and repeatable (S-1 family). | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-69 [D6-7] | A lineless D6 is still sent to bind a Throne to their Fane. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-70 [D6-8] | A dissolved line's progenitor keeps the Throne bond, unmentioned. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-74 [D7-3] | A low shatter roll before the cornerstone leaves the Weight of the Frame short. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-77 [D7-6] | The atlas shows D7/D8 materials at D5. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-80 [D8-1] | A heart-node respawn can trap an Apotheos in the Gardens. | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| PT-83 [D8-4] | A D8 Silentium is greeted with the D7 Archon tree. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-84 [D8-5] | The Mycophant rematch line promises a retry that never comes by itself. | As built; the probe stays failing. | [BOSSES.md](BOSSES.md) |
| PT-85 [NPC-1] | A ledger summon strips a shared outpost NPC for good. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-87 [NPC-6] | The default bloodline name is stored English. | As built; the probe stays failing. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| PT-88 [CHAIN-1] | Fill-before-repair is built (2026-11-14), but a solo caster's grand ordeal still stalls: one dry, damaged anchor is never fed. Which lever next? | As built; the probe stays failing. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| PT-90 [CHAIN-3] | Bloodline members can never reach D7 (no leave, no Throne, no second line). | As built; the probe stays failing. | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |

### 1.7 Crimson Troupe audit calls (2026-11-06)

From [the Circus audit](audits/2026-11-06-circus/README.md) section C, items 1 to 18 (its item 19 is PT-21 and PT-57 to
PT-63 in §1.6). Each report gives the options and the evidence; the ids in brackets are the audit's findings.

| # | Question | Default if unanswered | Topic doc |
| --- | --- | --- | --- |
| D-29 | **Reach the Ringmaster.** The private per-viewer galleries, stairs and canopy that acclimation was meant to make solid (500/1000) were never built, so the idle Ringmaster's perch cannot be clicked from the floor (PT-57). Build them as designed (per-player collision, the multiplayer cost), or make real galleries and a reachable perch for everyone and let acclimation change only how they look? (audit P-2, W-4, D-1) | Nothing built; players build up to him. | [CIRCUS.md](CIRCUS.md) |
| D-30 | **Gate the culmination.** Should the pact or the finale need D4 (the Pattern's Degree), and school work (practicals or The Wrong Audience for Succession, The Missing Understudy for Liberation)? Should the 50-point first talk with each performer still give acclimation? (Q-2, Q-7) | As built: no Degree or school gate; first talks count. | [CIRCUS.md](CIRCUS.md) |
| D-31 | **Unstained in the Circus.** Refuse, perform but not teach, or Liberation only? Today an Unstained Successor silently loses the Pattern. (Q-6) | As built. | [CIRCUS.md](CIRCUS.md) |
| D-33 | **The fight on the boss kit.** Move the Ringmaster (and any Circus mini-bosses) onto the generic half of the Saint/boss kit, and does that settle G-2c for them? Build C-13 now (see D-18), and do the mini-bosses replace acts 2 to 4? (B-3, B-5) | As built: own controller with the kit's wind-ups and telegraphs (2026-11-07); no ladder. | [CIRCUS.md](CIRCUS.md) |
| D-36 | **After a Liberation run.** What happens to performers killed in a failed Liberation and to survivors after a ruin? (Since 2026-11-07 CAROUSEL counts only the performers still there; the dead never return.) Should Succession end with a yield scene like the Kindled Champion's? Does the Ringmaster get music? (B-4, B-13, B-9) | As built: no return, no yield scene, no music. | [CIRCUS.md](CIRCUS.md) |
| D-37 | **Mental verbs.** Where do attention, illusion, decoys, displacement, flash and sound live: one player-fired act per body, Circus-taught memories or staff forms, or docs only (the Circus's mental side is presentation)? (P-1) | As built: only the Mummer's automatic taunt. | [CIRCUS.md](CIRCUS.md) |
| D-38 | **Ringmaster Pattern.** A free slot, a group act, or a weak extra body as now? And does it belong in the transposition ring, where it shows today? (P-3, P-12) | As built. | [CIRCUS.md](CIRCUS.md) |
| D-39 | **Re-role a D3 body.** The Vulture and the Juggler are both D3 ranged volleys; re-role one, and carry each professor's own act into the body they teach? (P-4) | As built. | [CIRCUS.md](CIRCUS.md) |
| D-41 | **Circus NPC rig and costumes.** Rebuild the Circus NPCs on the jointed rig now or with the Circus's own pass? Are faculty costumes exempt from the no-brass rule? (Brass is off the Crossbar and Juggler since 2026-11-07.) (P-11, P-10) | Later; costumes not exempt. | [CIRCUS.md](CIRCUS.md) |
| D-42 | **Rebuild or re-dress the sites.** A full rebuild of the pavilion and camps in a new `tools/worldgen/circus/` generator (dropping the old arena), or a re-dress of the shell around it? The pavilion's builder cannot run (W-3). (W-1..W-3) | The audit recommends the rebuild; nothing until you choose. | [CIRCUS.md](CIRCUS.md) |
| D-43 | **Set pieces and rarity.** Which set pieces the big top and each camp get (seating tiers, ring curb, king pole, rigging towers, wagons, knife wheel, fire pit, costume trunks, mask walls), and how rare pavilions (48/16) and camps (24/8) should be. (W-1, W-2, W-5) | As built. | [CIRCUS.md](CIRCUS.md) |
| D-44 | **Loot and site outcomes.** Do Circus sites hold loot, or leave gifts to the NPCs as the Court does? What does a Succeeded or Liberated pavilion look like afterwards, beyond six cobwebs? (W-16, W-11) | No loot; six cobwebs. | [CIRCUS.md](CIRCUS.md) |

### 1.8 Core loop calls (2026-11-09 to 11-11)

From [the core loop suite](core_loop/00-README.md) and its two build logs: round one built the twelve tracks
([BUILD_LOG.md](core_loop/BUILD_LOG.md)), round two (packages P1 to P12) repaired, finished and taught them
([BUILD_LOG_ROUND2.md](core_loop/BUILD_LOG_ROUND2.md), whose closing section lists every call, deferral and tunable
number in one place). All of it is uncommitted. Every CL id below was built to the recommendation in brackets. Ruled:
CL-2 (2026-11-10, the verb key plus the radial's inner-ring verb picks) and CL-28 (the Temper names), both under
"Rulings that bind" in [CORE_LOOP.md](CORE_LOOP.md); CL-9, CL-10 and CL-11 (2026-11-13, kept as built), and CQ-16 and
CQ-41 (2026-11-13), under "Rulings that bind" in [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md). CL-5, CL-7, CL-8, CL-15, CL-18 and CL-19 and CQ-34 to CQ-40, CQ-42, CQ-48 and CQ-49 (2026-11-13, kept, done or
parked) are under "Rulings that bind" in [CORE_LOOP.md](CORE_LOOP.md), and so are CL-13, CQ-27 to CQ-33 and CQ-46
(2026-11-13, kept as built). The rest stay here and in the suite's README until you rule
them. To judge them in play, use [the play-test script](core_loop/PLAYTEST.md); the lines the overhaul would add to
`AGENTS.md` are in [the AGENTS patch](core_loop/AGENTS_PATCH.md).

| # | Question | Built to (default if unanswered) | Track |
| --- | --- | --- | --- |
| CL-1 | R for combat casts, or every manipulation through the verbs? | R stays; world-facing memories became Shapings. | 1 |
| CL-2 | How the player chooses the verb. | **Ruled (2026-11-10).** The verb is player state set by the verb key (Y), and Projection and Absorption are also verb picks on the radial's inner ring, shown with their hand icons (`BloodVerbChoicePacket`, `VerbSelection.choose`, which puts the selected memory down, so the pick is the plain verb). The cycle key still skips them (round two P3, task J). | 1 |
| CL-3 | Name for a manipulation used as a verb mode. | Shaping. | 1 |
| CL-4 | Does a claiming Shaping run ahead of machines? | Ahead when it claims; strands, Living Land and mining never claim a block that takes blood. | 1 |
| CL-6 | Flask and Jug. | They clot, stamped when bottled or first carried; Blood Rock does not. | 3 |
| CL-12 | Unexamined blood weaker or only unlabelled? | Only unlabelled. | 5 |
| CL-14 | Do living veins move blood? | Yes: 20 mL a second at most, 64 cells, only with the owner within 32 blocks. | 6 |
| CL-16 | Pickaxe tier and enchantments. | Read against the track's acceptance: stone-tier harvest at the bond (takes iron ore), iron at ring 2, diamond at ring 4, never netherite; no enchantment on any form. One line (`LivingStaffGrowthRules.digTier`) gives the literal "iron at the bond". | 8 |
| CL-17 | Living Land's school. | Animus, Ferric secondary. | 9 |
| CL-20 | Held Projection as a weapon. | Yes, on five memories and the new Blood Lash, built without the 12a spike. | 12 |
| CL-28 | Must a held manipulation be one of the five strand forms? | No (owner, 2026-11-12). The forms are optional shared kits. Each held manipulation is designed from what it is, with its own mechanic; none may be a recolour of another. Six held forms that only repeated their R cast were cut. | 12 |
| CL-21 | Converted memories keep their R cast? | Kept; decide after play. | 12 |
| CL-22 | Left click to snap a strand. | Yes; a stroke's crack stays free. For a lash (owner, 2026-11-12): left click alone is a tap straight down the crosshair; held Projection gathers an orb, a mouse flick throws the lash along an arc that cracks at first contact, the whip then follows the hand with the view capped, and fades when left still; left click on the orb releases the charge down the crosshair, on a live whip it pays the snap and recentres. Each lash carries its own lean between tap and charge. | 12 |
| CL-23 | The first strand. | Blood Lash, a new Animus Humilis memory. | 12 |
| CL-24 | Absorption's damage gets an owner; bounty on a draw kill? | Owner yes; no kill blood or kill shift on a draw kill. | 12 |
| CL-25 | Pace while a strand is out. | Spray 1.0, lash 0.8, plant and brand 0.6, jet 0.5; Free Hand (round 2, P5) adds 0.1 a level, never past walking. | 12 |
| CL-26 | Absorption specialises by school state on the target. | Yes, one rider table. | 12 |
| CL-27 | An off-hand second strand. | **Built** (round 2, P5) as the late skill Second Strand: a twin of the open strand from a free off hand, half again the blood (CQ-22). | 12 |

Calls the tracks raised while building (ids `CQ-`):

| # | Question | Default if unanswered | Track |
| --- | --- | --- | --- |
| CQ-1 | **Built (round 2, P9).** The thirty older Liber Sanguinium pages and ten Liber Immaculatus pages are registered (`LiberEntryDefinitions.OLDER_PAGES`, `OLDER_IMMACULATUS_PAGES`), each with a Degree floor (a purity or clarity stage for Liber Immaculatus) and earlier openers; the eight Manipulations school pages, the overview, Temperament, the Condenser and the Forge page were corrected. Degree floors, openers and the dropped prices are CQ-45 to CQ-47. | Built. | 11, R2 P9 |
| CQ-2 | **The older Shapings pay in protected ground.** Lignum Mortis, Pyretic Forge, Lode Draw, Hematic Hewing and Vital Effusion learn of a refusal only after the pulse is paid; Track 9's `VerbShaping.admits` hook would fix each in a few lines. | **Fixed** (round 2, P3): all five check in `admits`, before Temper and blood are priced, and say `message.hemomancy.player_block_edits.refused`. A running crawl, draw or heating on the same target is not checked again each pulse. Proved by the refusal GameTests in `shaping_validation`, `staff_mining_validation` and `farming_validation`. | 2, 7, 8, 9 |
| CQ-3 | **Draw rights for drips.** A scab spike drips from any tank above it, and a Condenser made by a drip has no owner and never goes back to a cauldron. | **Condenser half fixed** (round 2, P3): a drip-made Condenser belongs to the owner of the tank the drop fell from (nobody, for an unowned tank), a drop counts as filling it, and a spent one goes back to a cauldron. **Still open:** a spike drips from any tank above it, with no draw-rights check on the spike itself. | 7 |
| CQ-4 | **Vessel ownership.** Anyone can take a husk out of, or pick up, someone else's vessel; a stranger can lead a creature into someone else's empty vessel. A vial on a vessel skips the Living-Syringe-only rule (a vatted blaze can be vialled). | **Vial half fixed** (round 2, P6): a vial is refused at no cost on a syringe-only kept kind, and the Living Syringe samples kept creatures for 3 Vigor. **Still open:** the ownership half, as built. | 5 |
| CQ-5 | **Brazier recipes with no Shaping.** Bone to bone meal and gravel to flint work with nothing selected, so a held Absorption on a lit signature brazier can eat a rite's bone. Give them a `shaping`? | As built. | 2 |
| CQ-6 | **Freshness without the owner.** Blood Thrall couriers restamp the tanks they deliver to; a rite-held station comes out freshly fed. | As built. | 3 |
| CQ-7 | **Runs kept by catalysts.** A feeder of bone meal or coal keeps a feed or hot run working with no blood after the first pulse; hot and cold runs edit blocks without `PlayerBlockEdits`. | **Edits half fixed** (round 2, P3): a hot run lights campfires and candles and a cold run freezes water as edits for the player who lit the run (`PlayerBlockEdits`), and only while that player is online, alive and in the run's level; an ownerless run smelts and chills crops but lights and freezes nothing. **Still open:** a feeder keeps a run working with no blood after the first pulse. | 6 |
| CQ-8 | **Free or farmable growth.** The Talaromyces vein charges nothing per block after the first; the Staff's rings can be farmed by moving blood back and forth through a tank. | **Tank half built** (2026-11-13, P03): blood cycled through a tank, station or vessel grows no ring. The per-ore vein price waits on NQ-04-5. | 8 |
| CQ-9 | **Temper edges.** A full body draws no creature, so fills no Temper without Sated Siphon; the scar's "floor" is a slow refill; dry strength reaches school damage and Shaping work only. | As built. | 4 |
| CQ-10 | **Strand pace off the claim.** A strand's pace applies whenever its memory is selected and Projection held, even into a station it does not claim (0.8 against vanilla's 0.2). | **Fixed** (round 2, P3): the pace applies only while a strand is out (`StrandService.isOut`); the client also keeps it for the first 6 use ticks while the strand claims the aim, so the open does not hitch. Proved by `strand_validation` `eachStrandSetsThePlayersPace` and `projectionIntoAStationWithAStrandSelectedKeepsThePlainPace`. | 12 |
| CQ-11 | **Old saves.** `KnownManipulations.getSelectedManip` called `get(0)` on an empty known list, so a save from before the run kicked its player ("Invalid player data"). | **Fixed** (round 2, P1): an unreadable entry (the key name from before the Temper rename, an unknown enum name) is dropped on load, the selection falls back to the first known form or BLANK, and the owner packet never writes a null. No migration: old dev worlds lose their known manipulations but load. Proved by `KnownManipulationsOldSaveTest` and the `owner_snapshot_validation` GameTest `anUnreadableManipulationSaveLoadsWithoutKickingThePlayer`. | 2 |
| CQ-13 | **`AGENTS.md`.** No package edited the project instructions. The patch is written: [core_loop/AGENTS_PATCH.md](core_loop/AGENTS_PATCH.md) holds the exact replacement and added lines for every rule and guardrail from both rounds (the pose order with `strand_stroke` and `venous_cling` among them), ready to paste. | `AGENTS.md` still not edited. | all |
| CQ-14 | **Archive the track files?** Track 11 says to lift the suite into `CORE_LOOP.md` (done) and move the track files to `docs/archive/tracks/core_loop/`. Unchanged by round two; round two added three files beside them (`BUILD_LOG_ROUND2.md`, `PLAYTEST.md`, `AGENTS_PATCH.md`). | Kept in `docs/archive/tracks/core_loop/` until you have reviewed the CL rulings. | 11 |
| CQ-15 | **Furnace Veins' bank.** One stream feeds every furnace within 2 blocks of the struck one (at most 8) that has something to cook, for 6 mL a pulse in all; it never cooks faster than coal and banks nothing. Should the bank be smaller, priced per furnace, or the struck furnace only? | As built (round 2, P4). | R2 P4 |
| CQ-17 | **What Sanguine Mending may heal.** The Shaping heals iron and snow golems (`#hemomancy:mendable_constructs`) unless the golem is targeting the caster, not by `canHarm`; a hostile-faction golem that has not yet targeted the player can be healed. Add the mod's own constructs, or use `canHarm`? | As built (round 2, P4). | R2 P4 |
| CQ-18 | **Crimson Flame Conjuration's slot.** Its Projection Shaping is now the kindling; Track 12 names it a Plant strand candidate. When that strand lands, should the kindling become a narrow Shaping (`addProjectionShaping`) ahead of the strand, as Glacial Grasp's freeze is? | **Built** (round 2, P5): the kindling is the narrow Shaping, tried first; the fire plant is the main one. Proved by `strand_validation` `crimsonFlameKindlesFirstAndPlantsOnBareStone`. | R2 P4 |
| CQ-19 | **Glacial Grasp's R cast loses water.** The R cast's Frozen Cruor melts to air, so freezing a pond on R drains it; the Shaping's ice melts back to water. Make the R cast use the Shaping's rule? | R cast left unchanged (CL-21). | R2 P4 |
| CQ-20 | **Six strand skills, not five.** The package brief called Free Hand the off-hand strand; the track doc names Free Hand as pace and Second Strand as the off-hand one. Both are built: Long Strand, Taut Line, Free Hand, Whipcrack, Deep Brand, Second Strand (ids 74 to 79). Keep six, or fold the pace into another? | Six, as the track doc lists them. | R2 P5 |
| CQ-21 | **Where the strand skills sit.** The surface ring round Living Conduit is full, so Long Strand (D2), Taut Line and Free Hand (D3) are on the surface and Whipcrack and Deep Brand are Degree 5 on the deep layer, with Second Strand at Degree 6. Move Whipcrack and Deep Brand to D4 and re-lay the surface? | As built. | R2 P5 |
| CQ-22 | **The twin.** Second Strand pours a twin of the open strand, not a second channel: a trailing lash at half damage that never cracks, a jet 1.4 times as hard, a spray cone of 50 degrees, two sunk pulses for one paid, a second brand on the nearest other body within 4, all for half again the blood. Only a free or cell-hand off hand pours it. | As built. | R2 P5 |
| CQ-23 | **Three Mortem brands.** Hemorrhage (40 mL/s), Insatiable Hunger (30) and Grave Debt (60, Necrosis for 360 ticks) differ only by price and state length under one brand form. Give each its own rider, or move one to another form? | As built. | R2 P5 |
| CQ-24 | **Blood Binding brands each pulse.** Every creature the binding holds carries the binder's Animus brand for the binder's Deep Brand duration, refreshed each pulse, so a chain or lattice brands several. Brand only the aimed creature? | Every held creature. | R2 P5 |
| CQ-25 | **The stain run.** A plant on a wet stain floods six-connected wet cells within 8 blocks, 4 plus 2 a sunk pulse (at most 20), each cell erupting 0.75 wide 2 ticks after the last for the plant's damage, a body struck once per run; each cell it passes goes tacky. | As built. | R2 P5 |
| CQ-26 | **Whipcrack on sprays.** Every spray pulse lays its state, so every spray pulse is an accent and Whipcrack raises all spray damage and state, not just a drench. | As built. | R2 P5 |
| CQ-43 | **Furnaces per hot run.** A hot run burns every furnace, smoker and blast furnace beside any of its cells (15 ticks a beat, no cap), only for an owner online, alive and in its level. With CQ-7's open half (a catalyst feeder keeps a hot run up after the first pulse without blood) a coal-stocked feeder runs a furnace bank while its owner is merely in the level. Cap furnaces per run, charge blood per furnace, or make feeders need blood? | As built. | R2 P8 |
| CQ-44 | **Gouges block water.** Gouges and living veins hold flowing water back like glow lichen (waterloggable), so a nearly invisible, shapeless mark stops a stream. Stains and Lux traces still wash. Keep, or let gouges sit under flowing water without stopping it? | As built. | R2 P8 |
| CQ-45 | **Sixteen more core-loop firsts.** Round two P9 added First Drop, Fed and Reaped, Something Else Came Up (a Tainted Harvest), Something Bit (a chum catch), In Glass, Studied, The Ground Rose (Living Land), A Full Night, Brought On (a Blood Moon), What the Dead Give (Carrion), Both Hands (a twin strand), Along the Red (a stain run), Bound and Branded, Snuffed (a field of dark), Carried by the Vein and Stoked, for 27 in all. Their parents put them under Set Through, Shaped, Kept, Meat Is Blood, the second Degree, First Crack and Cut and Lit. Too many for one tab, or the wrong parents? | As built. | R2 P9 |
| CQ-47 | **No prices on the school pages.** The eight Manipulations school pages list every memory of the school with its cast type and one line of effect, plus its Shaping or strand, but no mL price (the old numbers were stale; the tooltip and HUD show the live price). Keep them price-free? | As built. | R2 P9 |
| CQ-50 | **Who teaches what.** 21 core-loop lessons over six outpost speakers: the Hermit the verbs and the world's answer; the Vicar the verbs and their order, the day, the world's count and chum, keeping creatures; the Alchemist living blood, fed ground, Temper, reading a creature, strain beds; the Mnemonist Shapings, strands, the ground memories; the Artificer hot runs, the Staff, the strand skills, climbing; the Vein-Mason ground marks, the carried crossing, walls and scars. Move any? | As built (`CoreLoopLessons`). | R2 P10 |
| CQ-51 | **Lessons are lore cards.** Each is an unread lore card, so every outpost speaker shows the NOTICE sigil until its lessons are read, as the Alchemist's machine lore already does. Make them plain Conversation cards with no dot? | As built. | R2 P10 |
| CQ-52 | **The Hermit stops at the fourth Degree.** His sheet says he knows less than the player from about the fifth, so his two lessons are offered to awake blood from Degree 0 (verbs) or 1 (answer) up to the fourth. Keep teaching later? | As built. | R2 P10 |
| CQ-53 | **The Vein-Mason from the fourth Degree.** Their lessons follow their locked greeting, so a third-Degree player who walks to Mason's Respite hears none, though living veins open at the third. Open earlier? | As built. | R2 P10 |
| CQ-54 | **A lesson only opens Liber pages.** Asking any branch opens the lesson's pages (source DIALOGUE) and nothing else: no item, proof, advancement or saved flag beyond the lore read record. Reward a first hearing? | As built. | R2 P10 |
| CQ-55 | **Successors tell the lessons.** A successor reuses its profession's tree, so it tells that profession's lessons in the original speaker's voice, against the Successors sheet. Give successors their own lines or none? | As built. | R2 P10 |
| CQ-56 | **Verb presence for onlookers.** Another player's client now gets a caster's verb reach, active verb and selected manipulation (`PacketSyncVerbPresence`), sent to trackers only while a verb is channelled: when a channel opens, on change every 10 ticks, and to a player who starts tracking mid-channel. Nothing is sent for an idle player or one whose blood sleeps. A remote Shaping's own `reachScale(player)` and `claims` still read the caster's unsynced skills and attachments on the onlooker's client (Long Strand's length, for one). | As built. | R2 P11 |
| CQ-57 | **The refused-claim mark.** A refused pulse (protected ground, nothing to burn, too many walls, no blood) sends the owner `VerbRefusalPacket` for the Shaping's cadence plus 4 ticks; the four reticle ticks are pulled out 2 px and crossed by a 3 px stroke in pale bone (`0xD8B8A888`, a little clear) and the price line reads "Refused". Red still means claimed. | As built. | R2 P11 |
| CQ-58 | **Pyretic Forge drops the outpouring.** `presentsCast` is false: no Flammeus column round the caster and no third-person casting arm from the presentation (the vanilla use pose stays). Its look is a heat tendril from the hand each pulse and a forge bed at 0.45 size sunk under the items, with ember licks off the rim; a fired block's burst is 0.8 size. | As built. | R2 P11 |
| CQ-59 | **Stains.** Three splashes per age (`blood_stain_<age>_<n>`), one shape per variant across the ages so a stain keeps its outline as it dries; vanilla picks the variant by position. Tacky alpha 200 with a raised rim, dry 160 as a cracked ring with a clear middle. | As built. | R2 P11 |
| CQ-60 | **Worn cleats.** The cleats keep their flat inventory icon and wear their own model: a hematic iron shell and sole, a sinew wrap at the ankle, two toe spurs, a heel spur, a spur splayed off each side and two studs, in hematic iron, bone and sinew. | As built. | R2 P11 |
| CQ-61 | **Planted Staff rings and a stalled screen.** A rite's planted Staff shows its escrowed Staff's ring stamp, else its owner's rings; synced, never saved; bands lifted 3 px clear of the Focus throat. A sutured machine keeps its suture's state on the tank (saved under `Link` only when not `NONE`) and its own blood gauge shows a broken-stitch pip while stalled, with the reason on hover; a fane suture whose owner is below the fifth Degree is its own reason. | As built. | R2 P11 |
| CQ-63 | **Who gives the Titration Bandolier.** The HUD's Temper vials now show only while the bandolier is in its Scarlet Vanity slot, so until someone hands it out, survival players see no vials. The Mnemonist is the likely giver; when, and against what proof, is open. | Creative-only for now (2026-11-13, owner's call). | Living HUD |
| CQ-62 | **Fixture commands.** The core-loop fixtures are four dev-only `/hemo` branches (`temperpool`, `tank`, `vat`, `gouge`); a relog forgets a player's fixture and the pool to restore (the blocks stay); feedback names amounts, never positions. | As built. | R2 P11 |

Earlier questions the overhaul touches: **F-12 and F-13** look answered (scars now steady Temper; Track 4) and
**PT-1, PT-1b, PT-2, PT-12, PT-13, PT-14, PT-25, PT-39, PT-88 and D-26** may be answered or moot. Their probes are
optional and still fail the same way, so each still needs its ruling before it moves. The opt-in playthrough suite's
required probes all pass since round two P2 (the eight inherited failures were fixed or traced there); its optional
decision probes fail exactly as they did before the overhaul (see the round-two closing section for the last run).

CQ-50 to CQ-62 (round two P10 and P11) came after the owner answer sheet of 2026-11-11 was written, so they are not on
it yet; the round-two closing section lists them with the sheet rows that P10 and P11 answered in passing.

---

## 2. Decided without you, awaiting review

Sessions made these calls because you were away or said "go on your guess". Each stays until you have seen it in a
client and kept or changed it. The topic docs list the full set; these are the ones that change play or look.

| Area | The call | Topic doc |
| --- | --- | --- |
| Possession | Mind Broken applied at the return's cut; the bat hangs when stopped under a ceiling. (The numbers, the Ancestral family and the rite's cost were kept on 2026-11-13: RV-3, DS-1, RV-14.) | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) |
| Brined Court | Ink Cap Primal hood still shows the cap; polyp ocean spawn spacing of 48 blocks. (The tide rock, the Purse share, the 1 in 40 odds and the nursery rate were ruled on 2026-11-13: HN-1, HN-2, RV-7, RV-78.) | [BRINED_COURT.md](BRINED_COURT.md) |
| NPC trade | Box order Quest Work, Inquiries, Conversation, Trade; one card per Conversation option; payment all or nothing; rapport lore content and balance; D0 players may trade with the Unstained; catalogue substitutions (Bleeding Bulbs not Flasks, vials not enzymes, Scar Blank never sold, only 8 Court roles get cards); only Known and Trusted rapport lines, no Confidant; daily cards pay more and cap at 2 uses. | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) |
| Circus visuals | The Bellows, Juggler, Hulk, faculty and camp rebuilds were authored from previews only. (The trade calls were kept on 2026-11-13: RV-15.) | [CIRCUS.md](CIRCUS.md) |
| Circus audit round 1 (2026-11-07) | A lost Ringmaster releases the site rather than settling it; CAROUSEL counts only performers still there; the Waybill search stops at 32 spacing cells; the 18 pavilion lanterns were placed by script; troupe wind-up poses and telegraph looks were authored without a client. | [CIRCUS.md](CIRCUS.md) |
| Boss fights | The Mycophant brood clutch and amber failure beat are not built and stay deferred (CR-29). The rest of this row (presentation, fight numbers, the grounded burrow) was approved on 2026-11-13. | [BOSSES.md](BOSSES.md) |
| Rite merge stages 2 to 4 | The Rites & Crafts tab layout, special floor styles 4 to 6, the Sanguine Omen as the only rite-bound member. | [CARDINAL_RITES.md](CARDINAL_RITES.md) |
| Core loop | Every price, pace, window and yield in Tracks 0 to 12 and round two, the scar floor as a refill, stone-tier harvest at the bond, Sinew Cord for the Tendon Line, "crossing" for the Spine's trip, the 27 core-loop firsts and which Degree opens each Liber page, the brood (designed from scratch), the vessel screen's layout and the 21 NPC lessons; each track's "My own calls" in the first build log, and every round-two call in one list in the round-two closing section ([BUILD_LOG_ROUND2.md](core_loop/BUILD_LOG_ROUND2.md)). | [CORE_LOOP.md](CORE_LOOP.md) |

---

## 3. Proof owed

Built and covered by JVM tests, GameTests and review-client stills, but never watched in motion, heard, or played by
you. One played session per area, with a findings log, closes most of these.

- **Mausoleums** (`archive/tracks/mausoleums/12-next-pass.md` §3.2): every trial completed solo per Saint; set pieces
  mid-trial; the hallowed-ground burn layer; `SCENE_SAINT_RISE`; all ambience (the Saint fights are their own row). Natural worldgen was run for all four Saints on
  three seeds on 2026-11-14 (PR-3; all 12 found and flush, results in the Saint doc's "Proof owed"); whether the 14-block
  entrance check refuses too many sites is SM-13. Also §3.3 lighting and §3.4 art gaps.
- **Saint fights** (built 2026-11-05, visual pass 2 on 2026-11-06, uncommitted): only Hemorath has been played (by you, which started visual pass 2: each Saint's own telegraph pass and shaders, rebuilt animation, the trench lanes carried from his rake to the other three, five native polish rounds each; contact sheets under `build/<saint>-pass2/`, honest gaps per Saint in the Saint doc's "Proof owed"). Each passes its GameTests and a fight review client that reached every move, phase, opening and the ending with the chamber restored block for block; feel, timing, the four-to-six-minute length, multiplayer and both D8 paths are unproven. Looks to judge from the stills in `build/boss-presentation-review/screenshots/`: Hemorath's Brimming body and light floor, the tide's swirl against the spaghetti ruling, the Wells Answer and Last Thirst streams; Seraphae's in-chamber look, light floor and telegraph strength, her fragments, Flood dome and Thread; Putriciel's palette, Offering pose, eschar seams and the sparse rot world geometry; Velorum's Lantern Sweep, the Endless Hour's darkness, the devotees' rime crust, and the shared RIME school's red crystals on his challengers; since the 2026-11-06 revision (Frozen Cruor and liquid dark: every piece of his ice is dark oxblood blood-ice under pale frost, six new moves, pools of liquid dark and the Eclipse) also the cruor at eye height, the Cruor Javelins and Gloam Cuts from the boss camera, and the Eclipse's red seam grid and umbral shapes (sheets in `build/velorum-cruor/`, gaps in the Velorum design's "Revision polish (2026-11-06)"); since Putriciel's 2026-11-06 revision (the burning purifier, rot sludge and colour) also the body fire's soot tips against the dark chamber, the flesh mottle at portrait distance, and the sludge's thickness at a player's eye (sheets in `build/putriciel-revision/`, gaps in the Putriciel design's "Revision polish (2026-11-06)"). Gaps listed in the Saint doc's "Proof owed".
- **Endgame D8 journey**: an Apotheos run (Rite of Apotheosis, spore body, heart-node planting, synapses, spread
  shader, death cache) and a Silentium run (Memory of Vesper, possession rite with the brazier offerings, each of the
  nine forms, the wheel, the four cinematics, gaze and grab, Visitation). Nothing of this has been seen by you.
- **Brined Court**: a hand walk of a fresh world (Seats, outposts, residents, the Bloom Warden on a transgression, the
  Champion usurp), the two-client strand sync check, a full `alphaCheck` run with `court_worldgen_validation`.
- **NPC trade**: the Trade box on each side (Harbinger blood tithe, Unstained refusal at D1, Court custom, Circus
  curiosity), the Conversation box with lore glyphs, the Alchemist's daily card below the first screen.
- **Crimson Troupe**: none of it has been watched in a client (audit section D): the finale bar, opening scene and phase cards, the troupe's new wind-ups and telegraphs, the carousel and horse arcs, perception stages, performer threads, the rebuilt puppet and faculty models, the camps, the new pavilion lanterns, the Luminal Cicada and fresh-chunk placement. The 2026-11-14 footage fixes (FX-25 to FX-28) wait on the owner's re-film with `runCircusReviewClient` (PR-10).
- **Wayside and Harbinger sites, Brined Votary and heart-node remodels**: kept as built until you walk them (RV-27,
  2026-11-13, [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md)). Rebuilt 2026-10-31 (`b219f836b`) on the structure quality
  bar; `tools/worldgen/harbinger_sites.py`, `wayside_sites.py`.
- **Boss presentation**: every bar frame and opening scene (`runBossPresentationReviewClient`), the Mycophant waking
  and capture scenes, the Saint rise.
- **Casting**: cast impacts, the body-anchored basics and infusion veins in a played session, not the harness; neither balance pass has been playtested.
- **Heart-node**: `heartNodeReviewClient` passed 18 checks on 2026-10-29, but the screen, spread, set-piece and possession scenes were added afterwards and need a GPU launch.
- **Mausoleum code**: the raised descent vault is unchecked in the review client; the Succession test `legacyVialsAreRejectedBeforeConsumption` may still read a brazier at a stale offset.
- **Core loop** (2026-11-10, round two to 11-11): every visual was proven by a review client, but feel was not: the
  first build log's closing "Needs a client look" list (the lash, the dry penalty, the five-minute window, paces and
  prices, the vat economy, Blood Moon frequency, scavengers) and round two's additions (the strand skills and twin,
  the brood, strain beds, the field of dark, the carried crossing, the lessons). [core_loop/PLAYTEST.md](core_loop/PLAYTEST.md)
  is the script for that session: one sitting per system, the review client and `/hemo` fixture for each, what to try
  in order, and the feel and balance questions with where each number lives. Two-client looks at other players'
  streams, strands, cling, Tendon Line and field of dark were taken in round two P11 (`tools/review/review-onlooker.ps1`);
  a played multiplayer session was not.
- **Known test noise**: since core-loop round two the combined suite is deterministic under the isolation harness (P1:
  26 runs, every failure root-caused; P12: four consecutive green runs of 1,808, three by hand and one inside
  `alphaCheck`), and the opt-in playthrough suite's required probes all pass (P2; P12's run read "All 427 required tests
  passed" with the same 98 optional probe failures). Still only "probable": the playthrough's `successionfinale` and
  `understudymnemonistpuppet`, and the combined suite's coronation, chain-binding, ninth-pome and sutured-machine
  watchlist tests; none has failed since, and each now names its cause in its failure message. The optional probe
  `apotheosritewithoutrescue` passed once (P10) and failed in every other run. The two Succession opt-in failures and the
  False Omens deadline test were not re-checked in round two. Read `docs/TESTING.md` "Deterministic GameTests" before
  writing a GameTest.

---

## 4. What to work on next

Recommended order. Each step is one or two sessions and ends with a findings log and a topic-doc update.

1. **Play-proof the October work, one area per session, logging what fails and what reads badly.** Order: Saint
   mausoleums (§3.2 list, one Saint per session), the Apotheos D8 journey, the Silentium D8 journey, a Brined Court
   fresh-world walk, NPC trade boxes, boss openings. This is where the "decided without you" rows get kept or changed,
   so take section 2 with you. Fix in place only what blocks the walk; everything else goes on the log.
2. **Answer the blocking design calls** (section 1.1; D-2 was answered on 2026-11-05, and D-1, D-3 to D-5, D-7 to D-10, D-13, D-15 to D-17 and D-19 on 2026-11-06, built the same day and uncommitted) and the mausoleum
   calls (1.2), ideally as one written round like the Brined Court Q-rounds. D-1 and D-3 shape the next content pass;
   the rest can take their defaults.
3. **Clear the canon conflicts** (1.3) and the dialogue writers' questions (1.4) in one docs-and-lang session:
   LORE_REFERENCE, HEMOMANCY_REFERENCE §38, the wiki pins, the `silent_severance` rename, the whisper degree gate.
4. **Green baseline:** `alphaCheck` plus the opt-in suites the October work touched (`court_worldgen_validation`,
   the four Saint fight suites that replaced `saint_power_validation` on 2026-11-06, `succession_validation`),
   fixing the flakes named in section 3 rather than rerunning past them.
5. **Then new content, in this order unless step 2 changes it:** play the four Saint fights built on 2026-11-05 (D-2)
   and tune them, then the Saint gifts (D-1, once the draft is reviewed);
   Court elder challenges (D-12, parked on 2026-11-13). The Canon Memory D8 gate (C-6) was built on 2026-11-13.
6. **The 2026-11-02 cleanup audit** ([audits/2026-11-02-cleanup/README.md](audits/2026-11-02-cleanup/README.md)):
   its section A rows A-1 to A-8 (the menu duplication path, two exploitable payloads, four sourceless progression
   gates, the invisible ally, the blood sync storm, chunk-loading routing) and A-9 to A-14 (the construct body, the
   Ringmaster poses and presentation, the limbless mobs, template stats, the puppeteer double spawn, the spider rule)
   and A-15 to A-20 (block drops, wild poppies and hyphae, the Silence choice, the Effigy inquiry, the Lady route,
   the lang gaps) and A-21 to A-34 (recipe conflicts, marker tabs, the charm tag, missing assets, HUD under F1, the
   Silentium epilogue, clocks, tracked generators, save keys, the slots command, the strain trade, bats, the vanilla
   particle ratchet, smoothstep) were fixed on 2026-11-02, so section A is complete; section B's culls that needed no call (B-3, B-5 to B-14) were done on 2026-11-03, and B-1, B-2 and B-4 followed with section E; section C's no-call rows followed the same day (C-2 to C-4, C-6 to C-8, C-13 in part, C-15, C-17 to C-20, C-12b's corrections, C-1's promotions), leaving C-5 (E-6), C-16 and the visual tracks C-9 to C-12, while C-14 was done on E-15's answer (full wood sets, plus Heartwood); its section E lists twenty further calls, E-1 (the
   closed Unstained cure route) first; its section H corrects C-6, C-8, D-15 and two §1.4 rows in this file.
7. **Housekeeping debt, in the gaps:** the pending architecture decisions (1.5) as a batch, 335 legacy `main()` tests,
   the private-helper and `getPersistentData()` ratchets, the migrations that sunset on 2027-04-23, the deferred-minor
   lists in each topic doc.
