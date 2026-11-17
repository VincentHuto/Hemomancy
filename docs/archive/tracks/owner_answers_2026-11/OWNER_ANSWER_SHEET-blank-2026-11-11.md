# Owner answer sheet

Date: 2026-11-11. Every question that still needs your answer, from every open list in the repo, deduplicated into one
sheet. Nothing here was built or changed; this is a reading of the docs and audits.

## How to answer

- Write in the last column, "Your answer". Write "ok" to accept the recommendation. Leave it blank to keep things as
  they are built now ("Where it stands now").
- Ids that already existed (CL-4, CQ-6, D-12, PT-35, G-2b and so on) are kept. New ids use a two-letter prefix per
  section so they collide with nothing: LP (A), CN (B), CB (C), CR (D), VX (E), DS (F), SM (G), HN (H), CD (I), HK (J),
  RV (K), PR (L), FX (M). Where several ids raised the same decision they share one row.
- "PT-n" rows are the playthrough suite's probes (`docs/PLAYTHROUGH_TESTS.md`, the tests' `// DECISION: D-n`); they are
  not the D-n rows of section F.
- "(mine)" marks a recommendation written for this sheet where the source gave none.
- Architecture ids that clash with section ids elsewhere carry "(arch)": A-4 (arch), M-3 (arch). Mausoleum ids are
  M-1 to M-4. The Saint audit row C-9 is marked "(2026-11-02 audit)".

## Sources read, and when (2026-11-11, CST)

| Source | Read at |
| --- | --- |
| `docs/audits/2026-11-11-visual-balance/` (manipulation-and-economy-balance.md, creature-balance.md, manipulation-visuals.md, particles-and-texels.md, tuning-sheet.csv) | 13:24 |
| `docs/audits/2026-11-02-cleanup/README.md`, `docs/audits/2026-11-06-circus/README.md` | 13:24 |
| Topic docs: BOSSES, BRINED_COURT, CARDINAL_RITES, CIRCUS, CORE_LOOP, ENDGAME_PATHS, MANIPULATIONS_AND_CASTING, NPC_DIALOGUE_AND_TRADE, SAINT_MAUSOLEUMS, PLAYTHROUGH_TESTS (open questions); STATION_UPGRADES, PELAGIC_ECOLOGY, PELAGIC_WORLDGEN, MORPHLING_STRAINS, TESTING, HEMATIC_SUCCESSION_DESIGN_SPEC (no open-question sections) | 13:24 |
| `docs/architecture/CONSOLIDATION_PROGRESS.md` Decisions table (for the §1.5 ids) | 13:25 |
| `docs/archive/tracks/core_loop/00-README.md`, `docs/archive/tracks/core_loop/BUILD_LOG.md` (deferred, client looks, verifier "still open", closing section, owner corrections) | 13:24 |
| `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` (P1 to P9, 2175 lines; unchanged between 13:25:03 and the read) | 13:25 |
| `docs/OPEN_QUESTIONS.md` (415 lines, read last) | 13:25 |

Rows checked and left out because they are closed: CL-2 (ruled 2026-11-10; the cycle-key follow-up is CN-1), CL-28
(ruled), CQ-1, CQ-2, CQ-10, CQ-11 (fixed or built in round two), M-5 (`/hemo test mausoleum` is dev-only since cleanup
E-20), M-6 (legacy mausoleum structure deleted in cleanup B-9), M-2 (arch) (the five mobs deleted in cleanup B-5), F-5
(Visceral Organs cut, cleanup E-5), F-25 (MnA compat deleted, cleanup B-4), the §1.4 squid line (vanilla squid can be
leashed, cleanup H), the §1.4 "still called Archon" row (fixed in code, cleanup H), the cleanup audit's section E rows
(all done 2026-11-03), PT-40 and PT-76 (resolved 2026-11-05), the MANIPULATIONS plan §9 migration note (moot: no
migrations before release).

## Count of questions per section

| Section | Rows |
| --- | --- |
| A. Rules the core loop depends on | 21 |
| B. Core loop owner decisions | 76 |
| C. Core loop balance and feel calls | 30 |
| D. Creatures, spawns and bosses | 31 |
| E. Manipulation visuals, particles and texels | 18 |
| F. Design calls that block content | 73 |
| G. Saints and mausoleums | 21 |
| H. Brined Court, Circus and NPCs | 58 |
| I. Canon and dialogue | 19 |
| J. Architecture, tests and housekeeping | 29 |
| K. Decided without you, awaiting review | 85 |
| L. Proof owed | 30 |
| M. Will fix unless you object | 29 |
| Total | 520 |

---

## A. Rules the core loop depends on

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| LP-1 | Close the two free Formation sources: the crafting recipe (foul paste, bleeding bulb, redstone make 2) and blood-stained stone (1 Formation comes back as 2 at the Alembic)? | Both mint Formations with no blood, undercutting the Condenser, the 300 mL key and the whole clotting design. | Remove the recipe or add at least 250 mL to it; the Alembic returns at most 1 Formation. | |
| LP-2 | Keep the gourd fill (4 Formations for 200 mL of blood that never clots)? | Built; while LP-1 is open it turns free Formations into blood. | Keep only once LP-1 is closed. | |
| CQ-6, LP-3 | Should any fill restamp a whole tank as fresh, and should Thrall deliveries and rite holds count as the owner feeding it? | Any fill above 0 restamps the tank: 1 mL every five minutes keeps 8000 mL fresh. Couriers restamp; rite-held stations come out fresh. | Freshen only the fed share, or need 5% of capacity; Thrall deliveries do not restamp. | |
| LP-4 | Should a sutured buffer stay fresh for free? | Restamped free every 200 ticks with the owner in range, which is anywhere in their Fane at D5+. | Charge 1 mL per restamp, or keep the 16-block range inside a Fane too. | |
| CQ-7 | Should a catalyst feeder keep a ground run working with no blood after the first 15 mL pulse? | Yes. The edits half was fixed in round two; this half is open. Tainted harvest, glowroot and emberpod feeders make runs that feed themselves. | The feeder drips 15 mL each time it feeds, or 1 mL per beat while lit. | |
| CQ-43 | Cap the furnaces one hot run burns, or charge blood per furnace? | Every furnace, smoker and blast furnace beside any hot cell, no cap, 15 ticks a beat, while the owner is in the level. | Cap at about 4 per run and close CQ-7 first (mine). | |
| LP-5 | Close the hematic iron scrap loop? | Scrap distils to 3 powder; powder, coal and 40 mL braze back to 1 scrap: +2 scrap a cycle, and unlimited Ferric enzymes. | Distil scrap to 1 powder. | |
| LP-6 | Put a floor under stacked cast discounts? | Efficiency, mastery, dynamic use, Blood Moon, Dominion and Bloom multiply to about 0.17x with no floor. | A floor of 0.4x; it also closes LP-7. | |
| LP-7 | Change the Exsanguinate and Grave Debt refunds? | 250 mL back on 300 and 325 mL casts; Grave Debt pays on any death. Both profit above about a 17% and 23% discount. | Refund 60% of what was paid; Grave Debt only on the caster's kill. | |
| LP-8 | Cut the Formation drop from Hemorrhage-family blood loss? | 0.1% a tick on mobs, about 21% per 75 mL cast; Gloam Laceration and Umbral Rend share the roll. | 0.02% a tick, or only on a kill. | |
| LP-9 | Close the Crimson Tithe debt dodge? | Repaid as min(debt, current volume), so a player near empty repays almost nothing for 500 mL borrowed every 100 ticks. | Carry the unpaid debt forward, or 1 HP per unpaid 100 mL. | |
| LP-10 | Should strands out-value their own R casts by 3 to 22 times per mL? | Every damage strand beats its cast per mL; Blood Lash, the teaching strand, is the most efficient damage in the mod (up to 0.84 HP/mL). R keeps value only for reach. | No: price strands toward 2x a cast's value per mL; decide with CL-21 (mine). | |
| LP-11 | Should Shapings undercut their own channels 6 to 12 times? | Lignum Mortis, Canopy Mortis, Worked Lignum and Furnace Veins: channel 100 to 175 mL/s, Shaping 12 to 16 mL/s. | Accept it for world work and reprice the channels instead (CB-4) (mine). | |
| LP-12 | Should a higher rank buy more damage per mL? | Flat at about 0.02 HP/mL from Humilis to Summa; Perfectus is worst at about 0.015. Storm, Collapse, Eclipse Well, White Verdict and Funeral Bell sit at 0.009 to 0.017. | Yes: about +25% per rank, mostly by cutting high-rank costs (mine). | |
| LP-13 | Should scar tiers 2 and 3 mean that school never runs dry? | The refill toward 5 x tier at 0.25 x tier per second outpaces the scarred burn. | Refill at 0.1 x tier per second. | |
| LP-14 | Should Temper burn scale with a Shaping's or cast's price? | Every Shaping and strand burns 1 Temper/s from 4 to 60 mL/s; R casts burn 1 to 5 while one second of Staff draw gives 8, so the dry penalty is rarely felt. | Burn by price band (0.5 to 2 per second) and raise R burns to 2 to 8 (mine). | |
| CQ-9 | Should a full body still draw Temper from a creature? | No: the full-body stop blocks the draw unless Sated Siphon. Catalysts, meals, Wills, the Vat and Carrion still fill. Dry strength reaches school damage and Shaping work only. | Let Temper fill at full blood and discard the blood. | |
| CQ-16 | Change Carrion Communion's yields? | Flesh 2, bone 3, skull 10, bone block 24 Mortem (3 bones, worth 9 as catalysts) and 1 HP each, for 4 mL a pulse, times the Staff's work scale. | Bone block 9; do not multiply by the work scale. | |
| LP-15 | Cut coal and charcoal as Flammeus catalysts? | 3 Temper each, and the brazier makes 2 charcoal per log for 20 mL. | Cut to 1. | |
| CQ-8 | Charge the Talaromyces vein per extra ore, and stop Staff rings counting blood cycled through a tank? | Up to 64 ores free after the first (Lode Draw is pointless with the scar); tank round trips count toward rings and lose no blood. | 6 to 12 mL per extra ore; count only blood spent or drawn from creatures. | |
| LP-16 | Limit Blood Binding on bosses? | The Shaping renews a forced hold every 10 ticks for 20 mL/s, and R relocks before the boss hold lapses; Blood Lattice pins 8 creatures for 44 mL/s. | Bosses: one hold per 10 s, or an immunity window after a binding ends. | |

## B. Core loop owner decisions

CL-1 to CL-27 are built to their bracketed recommendation and not yet ruled. "Ok" rules them as built.

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| CL-1 | Does R stay for combat casts, or does every manipulation go through the verbs? | R stays; world-facing memories became Shapings. | R stays. | |
| CN-1 | Should the manipulation cycle key also step through Projection and Absorption now that they are back on the radial? | It skips them: verbs are never equipped, and the inner-ring picks set the verb without touching the selected memory (round two P3, task J). | Leave it; one key stepping two kinds of state would contradict the CL-2 ruling. | |
| CL-3 | Name for a manipulation used as a verb mode? | Shaping. | Shaping. | |
| CL-4 | Does a claiming Shaping run ahead of machines and reservoirs? | Ahead when it claims; strands, Living Land and mining never claim a block that takes blood. Infusion inputs and structure frames are still claimed by a strand. | Ahead when it claims. | |
| CL-5 | Fresh window and clot rate for inert containers? | 5 minutes fresh, then 10% of what is fresh each minute; none fresh after an hour. | As built. | |
| CL-6 | Flask and Jug: clot or cut? | They clot, stamped when bottled or first carried; Blood Rock does not. | Clot. | |
| CL-7 | A machine whose owner is offline or out of range? | It stalls and its buffer clots. | As built. | |
| CL-8 | May a player absorb from another player's machines and vats? | Owner and bloodline only; others behind the `bloodline.foreignReservoirDraw` flag (off). | As built. | |
| CL-9 | Name for the spendable pool? | Temper. | Temper. | |
| CL-10 | Does Temper apply to R casts too? | One rule, never refused: dry casts x1.25 blood and x0.9 damage, dry Shapings x1.5 blood and x0.75 work. | As built. | |
| CL-11 | What does death do to Temper? | Death keeps half. | As built. | |
| CL-12 | Is an unexamined creature's blood weaker, or only unlabelled? | Only unlabelled. | Only unlabelled. | |
| CL-13 | Caps on led creatures and vats? | No vat cap; one led creature at D3, two at D5, three at D7. | As built. | |
| CL-14 | Do living veins move blood? | Yes: at most 20 mL a second, 64 cells, only with the owner within 32 blocks. | As built. | |
| CL-15 | The cloche: new block or the Mycelial Lantern re-housed? | The Lantern re-housed: same id and recipes; its culture slot takes seeds. | As built. | |
| CL-16 | Living Staff dig tier: stone tier at the bond (as built), or the literal "iron-equal at the bond"? | Stone at the bond (takes iron ore, not diamond), iron at ring 2, diamond at ring 4, never netherite; no enchantments. One line changes it. | As built: it meets the track's acceptance. | |
| CL-17 | Living Land's school? | Animus, Ferric secondary. | As built. | |
| CL-18 | Do food and sleep feed blood regeneration? | None starving, half hungry, x1.5 well fed; Well Rested +1x for 5 minutes. | Yes (see CB-2 for the skills that swamp it). | |
| CL-19 | Is the Parish (villages) in scope for this overhaul? | Deferred; nothing built. | Defer; its own design after Stage C. | |
| CL-20 | Held Projection as a weapon? | Yes, on the first-wave memories and the new Blood Lash, built without the 12a spike. | Keep; judge in play (see LP-10). | |
| CL-21 | Do converted memories keep their R cast? | Kept, marked for removal after review. | Keep through the first play pass, then decide. | |
| CL-22 | Left click to snap a strand? | Yes, the one new serverbound payload; a mouse reversal crack stays free. | As built. | |
| CL-23 | The first strand a Harbinger meets? | Blood Lash, a new Animus Humilis memory. | As built. | |
| CL-24 | Does Absorption damage get an owner, and does a draw kill pay the kill bounty? | Owner yes; no kill blood or kill shift on a draw kill. | As built. | |
| CL-25 | Movement pace while a strand is out? | Spray 1.0, lash 0.8, plant and brand 0.6, jet 0.5; Free Hand adds 0.1 a level, never past walking. | As built. | |
| CL-26 | One Absorption rider table keyed by school state? | Yes. | As built. | |
| CL-27 | Keep the off-hand second strand (your bracket was "defer")? | Built in round two as the late skill Second Strand: a twin of the open strand from a free off hand for half again the blood. | Keep. | |
| CQ-3 | Should a scab spike check draw rights on the tank it drips from? | The Condenser half is fixed; a spike still drips from any tank above it. | Yes: drip only from tanks the spike's placer may draw (mine). | |
| CQ-4 | Who may empty, pick up or fill another player's specimen vessel? | Anyone can take a husk out or pick it up; a stranger can lead a creature into an empty owned vessel. (The vial skip was fixed in round two.) | Owner and bloodline only (mine). | |
| CQ-5 | Give the bone-meal and flint brazier recipes a Shaping, so held Absorption cannot eat a rite's signature bone? | They work with nothing selected. | Yes, give them a Shaping (mine). | |
| CQ-15 | Furnace Veins' bank: smaller, priced per furnace, or the struck furnace only? | One stream feeds up to 8 furnaces within 2 blocks for 6 mL a pulse in all; never faster than coal. | Price per furnace (mine). | |
| CQ-17 | Should Sanguine Mending heal by `canHarm` and include the mod's own constructs? | Heals iron and snow golems not targeting the caster, so a hostile-faction golem can be healed. | Use `canHarm`; add the mod's constructs (mine). | |
| CQ-19 | Should Glacial Grasp's R cast leave water when its ice melts, as the Shaping does? | The R cast's ice melts to air, so freezing a pond on R drains it. | Yes, same rule (mine). | |
| CQ-20 | Six strand skills (Long Strand, Taut Line, Free Hand, Whipcrack, Deep Brand, Second Strand), or fold the pace into another? | Six built (ids 74 to 79). | Six. | |
| CQ-21 | Move Whipcrack and Deep Brand to D4 and re-lay the surface ring? | Long Strand D2, Taut Line and Free Hand D3; Whipcrack and Deep Brand D5 on the deep layer; Second Strand D6. | As built (mine). | |
| CQ-22 | Keep the twin's per-form effects? | Trailing lash at half damage, jet x1.4, 50-degree spray, two sunk pulses for one, a second brand within 4; half again the blood; free or cell-hand off hand only. | As built. | |
| CQ-23 | Give the three Mortem brands their own riders, or move one to another form? | Hemorrhage 40 mL/s, Insatiable Hunger 30, Grave Debt 60 (longer Necrosis) differ only by price and state length. | One rider each (mine). | |
| CQ-24 | Should Blood Binding brand only the aimed creature? | It brands every creature it holds, each pulse, for the binder's Deep Brand time. | Every held creature. | |
| CQ-25 | Keep the stain run's rules? | A plant on a wet stain floods up to 20 six-connected wet cells within 8 blocks, each erupting for the plant's damage, a body struck once per run. | As built. | |
| CQ-26 | Should Whipcrack raise every spray pulse? | Every spray pulse counts as an accent, so Whipcrack raises all spray damage and state. | As built. | |
| CQ-27 | The brood's numbers? | 30 mL per parent health point (at least 150) over 120 fed seconds; both parents at 75 Vigor; rests 6,000 ticks; young at 40 Vigor as a baby. | As built. | |
| CQ-28 | Should only hand-projected blood pay a brood? | A Vigil and an in-range suture let a keeper standing near finish one without holding Projection. | As built. | |
| CQ-29 | Restrict breeding to animals (`AgeableMob`) or tag more kinds out? | Everything keepable except the undead and the Morphling Polyp, hostile kinds such as blazes included. | As built. | |
| CQ-30 | When is a kind "met" in the Living Bestiary? | On sampling, leading, jarring, breeding or opening its vessel's screen; profiled kinds only; seeing one does not count. | As built. | |
| CQ-31 | Give kinds real quirks of their own? | The second research level shows notes read from existing rules. | As built for now. | |
| CQ-32 | Keep syringe and vial costs on kept creatures? | Syringe 3 Vigor, vial 5; a vial is refused at no cost on a syringe-only kind. | As built. | |
| CQ-33 | Hide the humour count on an unread kind? | Its screen shows one hollow phial per Temper, so the count shows before the kinds. | As built. | |
| CQ-34 | Keep the Brined Votary as the chum hunter in the Court's home waters? | A strong pool there draws a Votary, once found only in wrecks. | As built. | |
| CQ-35 | Keep the fungal substrate split? | Flesh strains on Ravenous Heartwood; Neurotic, Frigid and Ferric on Salt-Stained Timber; Incandescent and Umbral on podzol or mycelium. | As built; Ferric could move to flesh. | |
| CQ-36 | Is Salt-Stained Timber the right reading, and are its recipes too cheap? | The voyager's lore log inoculates logs: 1 + 8 logs make 8 timber; 1 timber, 1 kelp, 4 logs make 5. | As built. | |
| CQ-37 | Keep tainted harvest's rules? | 8% per ripe crop broken on level-2 ground; never from a drip, a stain or the cloche; food 3/0.3, 12 mL a point, gouge feed 1,200 ticks. | As built. | |
| CQ-38 | Build catalyst crops for the other six schools? | Only Lux (glowroot) and Flammeus (emberpod); the eight strains give every school spores worth 5. | Let the strains stand for them. | |
| CQ-39 | Is farmable spore income acceptable? | A ripe strain returns its spore and a second half the time, so spores, enzymes and First Culture are renewable from one spore. | Accept (mine). | |
| CQ-40 | Should floor spikes do anything? | 10% per drop on bare scab or stone, up to 3 blocks, 2 short of the hanging tip; vanilla stalagmite fall damage only. | As built. | |
| CQ-41 | The field of dark hides players too: add a cost, limit it to the caster, or shrink it? | Blood Eclipse's Absorption hangs a field (up to 2,400 ticks, radius 3 to 8); anyone in it is veiled from creatures beyond 4 blocks. | Caster only (mine). | |
| CQ-42 | The carried crossing between two veins one living run joins: flat 125 mL, free, or scaled with run length? | Flat 125 mL (against 500 and up) for a within-base hop; nodes are not destinations. | As built. | |
| CQ-44 | Should gouges and living veins hold back flowing water? | Yes, like glow lichen, so a near-invisible mark stops a stream. | Let water flow over them (mine). | |
| CQ-45 | Sixteen more firsts (27 in all): too many for one tab, or wrong parents? | Parented under Set Through, Shaped, Kept, Meat Is Blood, the second Degree, First Crack and Cut and Lit. | As built. | |
| CQ-46 | Change any older Liber page's Degree floor? | Overview, Temperament, Condenser D1; school pages, Thelemic, morphlings, mob materials D2; weaving, Forge, stations D3; Cradle, Nectar D5; Annetta D6; Canon Memories D7. | As built. | |
| CQ-47 | Keep the school pages free of mL prices? | They list each memory's type and effect; the tooltip and HUD show the live price. | Keep. | |
| CQ-48 | Credit Brought On only to the player whose region raised the moon most? | Every awake player on counted ground gets it when the raised chance made the roll. | As built. | |
| CQ-49 | Grant Carried by the Vein on arrival instead of on payment? | Granted when the opening is fed in full. | As built. | |
| CN-2 | Should rite floors be protected from other players' Projection, not just the caster's? | Since P2 a caster's own rite claims their Projection over its ground; another player's can still infuse the floor (as mining can break it). | Yes, every player (mine). | |
| CN-3 | Build Vascular Dowsing's veins toward ore and "Lode Draw follows the veins"? | Deferred: needs a new owner-only cue (a new Form or packet field). | Yes, in a later visuals pass (mine). | |
| CN-4 | Make Living Land a Plant strand? | Deferred in Track 9; it is a held channel. | No (mine). | |
| CN-5 | Write NPC lessons for the core loop? | No NPC names the second-wave Shapings, strand skills, vat screen, brood, strains, crops, hunters or the field; the Liber, tooltips and advancements teach them. | One voice pass per teacher (Mnemonist, Artificer, Alchemist, Vicar) (mine). | |
| CN-6 | Give the harder firsts goal or challenge frames? | All 27 use the task frame. | Goal frame for Brought On, Along the Red and Carried by the Vein (mine). | |
| CN-7 | Build Track 10's leftovers? | Wills read region pressure, not recent spend; no Tenebris or Lux world answer; no spreading sod; no bestiary world pages; only drowned answer chum. | Defer until the CL rulings (mine). | |
| CN-8 | Add a vessel form to the syringe draw packet so the draw animates on jars and vats? | It plays a sound and a line; a position form changes the payload. | Yes, with the next protocol bump (mine). | |
| CN-9 | May a bloodline member stand in for an offline owner at a sutured machine? | Deferred in Track 3; the machine stalls. | Yes (mine). | |
| CN-10 | Make Hematic Cleats an Armature upgrade instead of a crafting recipe, with their own look? | Crafted from hematic iron boots, bone, scrap and Sinew Cord; worn they look like plain hematic iron boots. | Keep the recipe; give them a look (mine). | |
| CN-11 | Spore load feeding manipulations and refilling blood (ENDGAME_PATHS)? | Not built. | Defer (mine). | |
| CN-12 | Should a Rib gourd's blood feed regeneration, and may two Rib gourds stack? | Unclear in code (audit could not settle it); a double tick across worn slots is possible. | One Rib's generation at most; see FX-1 (mine). | |
| CN-13 | Should a held Shaping session be re-checked for protection each pulse? | A running crawl, draw or heating on the same target is not checked again after `admits`. | Yes, each pulse (mine). | |
| CN-14 | Should Hematic Hewing's `admits` check every cell of the cut, not only the struck block? | The struck block only; later cells are refused one by one as they break. | As built (mine). | |
| CN-15 | Should the blaze stay bloodless to Absorption? | Yes; Track 5 gave it a vat route instead. | As built. | |

## C. Core loop balance and feel calls

Numbers live in `docs/audits/2026-11-11-visual-balance/tuning-sheet.csv`.

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| CB-1 | Raise base blood regeneration? | 1 mL/s; a 5000 mL pool takes 83 minutes. Strands cost 15 to 60 mL/s. | 2 mL/s if strand prices stay. | |
| CB-2 | Cut the regeneration skills that swamp food and sleep? | Sanguine Surge 5/10/15 mL/s, the Candle 30, the Rib gourd 5 skipping the circulation cap, against 1 mL/s base and x1.5 to x2.5 for food and sleep. | Surge 0.05 a level (1 to 3 mL/s), Candle to 5 mL/s, halve the Rib. | |
| CB-3 | Retune meal blood? | Tainted meat 20 mL per nutrition (3.3x steak); raw fish 3 is too small to notice; a full bar of steak is 120 mL. | Tainted meat 10, raw fish 4 (mine). | |
| CB-4 | Cut channel upkeeps? | Avatar 75 to 400 mL/s, Iron Choir 250, Absolute Stillness 300, Carrion Communion 220, Radiant Wake 120, Living Circuit 100, for 1 to 3 DPS or small buffs. | Avatar 10 to 45; Choir 40 to 60; Stillness 60 to 80; Communion 40 to 60; Wake and Circuit 30. | |
| CB-5 | Reprice Thread Ripper? | 35 mL at Summa (median 400); deletes any foreign summon you may harm, a PvP pet included. | 250 to 350 mL. | |
| CB-6 | Raise the charged-cast minimum? | 25% charge pays 25% cost: Deadly Gaze lifts and stops a mob for 25 mL on a 20-tick cooldown. | 50% minimum, or a fixed 25% cost floor. | |
| CB-7 | Sovereign Instinct's automatic 450 mL? | Charged every time it fires on its own. | 200 mL, or warn before paying. | |
| CB-8 | Cap Osseous Bloom on big non-boss targets? | 25% of current HP, capped at 12 on bosses only. | Cap 20 on anything over 100 max HP. | |
| CB-9 | Fix Hematic Beacon's value? | 350 mL heals 3.2 HP (0.009 HP/mL); Lumen Suture is 2.7x better. | Double the heal per pulse. | |
| CB-10 | Stop Endless Hour chaining? | Cooldown equals duration (200 ticks). | 1200-tick cooldown after it ends. | |
| PT-25 | Should the conjuration dispel refund scale with wear? | The Staff and both cell hands refund 90% whatever the wear (probe fails). | Scale with wear. | |
| CB-11 | Limit the Blood Moon's 25% cast discount and check its drain? | Discounts every caster everywhere; drains 75 mL/s from exposed players. | Discount only Harbingers in the moon's region, or 0.9x; confirm the drain. | |
| CB-12 | Is the rite boundary's 500 mL/s drain intended? | 25 mL a tick; empties a pool in 10 s. | Confirm, or 50 mL/s (mine). | |
| CB-13 | Cut the Mycelial Lantern's enzyme price? | 600 mL per enzyme, while rendering gives enzymes free. | 150 to 200 mL per enzyme. | |
| CB-14 | Should rendering give enzymes for no blood? | Vanilla reagents give enzymes at 25% to 50%, so enzyme fruiting is pointless. | Keep free, cut yields to 10% to 25% (mine). | |
| D-21, CB-15 | Run one blood-cost and offering pass over rites and Working crafts? | No pass has ever been done; 14 of 20 crafts have first-pass offerings. Covenant Throne 8000, Vascular Effigy 6500, Mycelial Lantern 2500 at D3 (13 to 16x their tier). | Yes after play; Lantern to 750, check the Throne and Effigy. | |
| CB-16 | Should the Alembic jug save blood per dose? | Flask 2500 + 1 Formation for 3 doses; jug 5000 for 6, no saving. | Jug 4000 for 6. | |
| CB-17 | Bring memory_blood_rush in line with other Humilis weaves? | 350 mL and 7 enzymes against 50 to 100 mL for the rest. | 100 mL and 2 enzymes. | |
| CB-18 | Trim brazier yields above vanilla, and Pyretic Forge's free smelting? | 7 of 12 beat vanilla (ice to blue ice 1:1 for 90 mL, vanilla 81:1); with Pyretic Forge selected the brazier smelts anything for 15 mL and no fuel. | Ice chain to 4:1; keep the rest (mine). | |
| CB-19 | Should catalysts be spent by best fit? | Off hand first, then the first inventory slot. | Pick the smallest catalyst that covers the shortfall. | |
| CB-20 | Scale Hematic Hewing by hardness? | 4 mL per block at any hardness (the pickaxe pays 2 per hardness point, up to 40). | 2 mL per hardness point. | |
| CB-21 | Hematic Cleats' cling discount? | Upkeep x0.25. | x0.5. | |
| CB-22 | Ironhearted's price? | +4 absorb HP per cast (cap 10) for 10 minutes at 75 mL per HP; Iron Retort is 250 mL for 3 seconds. | 150 mL per HP or 5 minutes (mine). | |
| CB-23 | Void Shroud near-permanent stealth? | Veiled and Speed II for about 20 mL/s; Penumbral Drift costs 75 mL/s for nearly the same. | Raise Shroud to 60 mL/s equivalent (mine). | |
| CB-24 | Slow feed runs? | Every crop beside a lit cell (64 cells) grows a stage every 6 s, so the cloche and planting basin are pointless. | One stage per 30 s, or blood per beat with CQ-7 (mine). | |
| CB-25 | Charge Temper for Staff weapon forms? | Forms burn no Temper; a swap costs 250 mL (floor 25). | 1 Temper per swap. | |
| CB-26 | Stop a mob farm holding a region at seething? | About one 20 HP kill a second keeps it there; Blood Moons rise to one night in 4. | Cap the kill share of pressure (mine). | |
| CB-27 | Strand fine tuning: snap wait and the plant tap? | Spray snaps have no wait; a one-pulse plant tap beats a full plant 3.3x per mL. | 10-tick snap wait; price the first plant pulse as a full plant (mine). | |
| CB-28 | Rebalance the Formation recipes once LP-1 closes? | 71 recipe files name Formations; 67 were never rebalanced for living blood. | Yes, after LP-1 (mine). | |
| CB-29 | Accept that combat Absorption cannot pay for combat? | A base Staff draws 8 mL/s from one target; strands cost 15 to 60 mL/s. It is a Temper tool. | Accept; it matches CL-24. | |

## D. Creatures, spawns and bosses

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| CR-1, C-5 (2026-11-02 audit) | Fix the Overworld Fungal Gardens biome, and which spawn list is intended: the shipped JSON or `BiomeInit.fungalGardens`? | JSON: only Abhorrent Thoughts and Fargones, packs of exactly 8; bats 8-8; no vanilla ores; not in `#minecraft:is_overworld`; on by default; a D2 side assignment sends players there. | Fix in `fungal_gardens_worldgen.py` with packs of 1 to 4; delete the other list. | |
| CR-2 | Raise the Gardens dimension roster to D7 to D8 strength? | Fungling 7/1 to Abhorrent Thought 40/7; all die in 1 to 4 Hematic hits; only the Facsimile Golem tests a D8 player. | Yes, after CR-1. | |
| CR-3 | Stop Blood-Drunk Puppeteers spawning in daylight? | Weight 24, no light check, 4 dolls each: most of the daytime hostiles in dark forest, swamps, old-growth taiga. | Use the monster light rule. | |
| CR-4 | One scale for creature blood drain: the Desiccant's sting? | 250 mL per sting from a 12 HP mob; the Lantern Tick takes 8 mL per 160 ticks. | 50 mL per sting (mine). | |
| CR-5 | Give template-stat mobs their own numbers? | Chitinite, Fervent Chitinite, Fungling, Enthralled Doll and Barbed Urchin share 7 HP, 1 damage, speed 0.3; the Nether Fervent Chitinite matches the overworld one. | Own stats for Fervent Chitinite and the Fungling (mine). | |
| CR-6 | The Chthonian Queen: a miniboss in a D0 savanna that fits a jar? | 60 HP, 6 armour, 7 damage; exactly on the jar's 60 HP limit. | Remove her from the jar tag. | |
| CR-7 | Should the Phlegethosoma (120 HP boss) spawn naturally? | Weight 1, one per 64 blocks in the Nether basin. | Keep (mine). | |
| CR-8 | Tone down the Venom-Rib Centipede? | 34 HP, 6 damage, poison and binding, weight 4 in common forests; fits a jar but no vat. | Weight 2, or 26 HP (mine). | |
| CR-9 | Lower Hemolymphopoda's ambient weight? | Weight 30 in packs of 2 to 5; takes about three quarters of the ambient cap; bats become rare in lush and dripstone caves. | Weight 10. | |
| CR-10 | Confirm these spawn intents: Vampire Bats are all of the Nether's ambient; the Morphling Polyp spawns in every Overworld biome; the Desiccant spawns by day? | All as built. | Confirm all three (mine). | |
| CR-11 | The Hematic Construct: glass cannon on purpose, or swapped numbers? | 12 HP, 8 damage: dies in two hits, kills a bare player in three. | Confirm or 24 HP / 5 damage (mine). | |
| CR-12 | Strengthen the End band? | Myelin Borer 8/2 and Choir Keeper at overworld strength for D5 to D6; Neural Overload is the real threat. | Leave (mine). | |
| CR-13 | Is the Excoriated (52/6/8) fair for a D2 player who wanders into the basin? | Double anything wild in the Overworld. | Accept; the basin is a D5 commission (mine). | |
| CR-14 | Should Wills scale harder with Degree? | Only Degree-scaled enemy; a D8 sent Will is about 40 HP and 8.5 damage, 4 to 5 Living-weapon blows. | Leave until play (mine). | |
| CR-15 | Rebalance the Mycophant's phase split? | Phase 1 is 360 of 720 HP; phase 2 before the Last Brood 180. | Phase 2 at 60% (mine). | |
| CR-16 | Annetta's 350 HP never matters: lower it or make the break health-based? | The break needs 2400 ticks and 50 damage after the shield. | Lower health to match (mine). | |
| CR-17 | Widen Seraphae's phase 1? | Integrity starts 30, phases at 40 and 75: bands 10, 35, 25. | Start at 20 (mine). | |
| CR-18 | Fix Putriciel's Rot II creeping slower than Rot I, and the 2/2/1 absolution split? | Kindling creep 12 / 20 / 15 ticks per point. | Make it monotonic, 12 / 10 / 8 (mine). | |
| CR-19 | Even out the Crowned Refusal's bands? | Anchors 0.72 / 0.42 / 0.12: bands 145.6 / 156 / 156, last 62.4 HP never used. | 0.70 / 0.40 / 0.10. | |
| CR-20 | The Kindled Champion's last band regrows fastest (4.5/s) and is shortest; slow it? | Can stall a low-damage player. | Regrowth 2 / 3 / 3 (mine). | |
| CR-21 | Is the Bloom Warden out-healing a D0 to D2 player intended? | Heals 4/s with 12 blooms fed; x0.25 damage while rooted. | Intended: the answer is amends, not damage (mine). | |
| CR-22 | Dead rewards: delete the Evening Star's loot table and XP, or route them through `completeVictory`? Same for the Saints' XP (80 to 120). | Never fire, because the bodies are discarded, not killed. | Route the XP through the victory; delete the duplicate table (mine). | |
| CR-23 | Add blood profiles for the 29 creatures that have none, and wild Lux and Congeatio sources? | Fargone, Thirster, Vampire Bat, Lantern Tick, Leech, Bloodlicker, Tooth Pecks and more yield no Temper; Lux has two sources, Congeatio no wild one. | Yes; pick a Congeatio creature (mine). | |
| CR-24 | Should the Gardens mobs, the Whale and the Queen give Temper through a Living Syringe? | Their profiles hold no tempers, only the syringe gate, so a draw yields blood with no Temper. | Yes, add tempers (mine). | |
| CR-25 | Should six wild creatures drop nothing? | Pyrosome, Hagfish, Blood Lantern Jelly, Antibody, Bloodlicker, Velorum Devotee. | Keep for the Antibody and Devotee; small drops for the rest (mine). | |
| CR-26 | Base menagerie keeping on a tag instead of the package? | Excoriated keepable, Phlegethosoma not, by package; Abhorrent Thought, Mortarbound and the Whale can be leashed but fit no vessel. | A `menagerie_unkeepable` tag (mine). | |
| CR-27 | Title cards on the lower third, as built? | Code and spec agree; an earlier note said upper. | Confirm. | |
| CR-28 | Is a grounded burrow in Vesper's rotation? | The builder chose the gate; the reference records only "every second Diving Rend". | Keep as built (mine). | |
| CR-29 | Build the Mycophant's brood clutch and amber failure beat? | Both deferred. | Defer (mine). | |
| PT-81 | Should a Mycophant win survive a logout or death during the yield? | Lost. | Record by UUID, settle at next login. | |
| PT-84 | The rematch line promises a retry that never comes: reword or re-arm? | Probe fails. | Re-arm after the cooldown (mine). | |

## E. Manipulation visuals, particles and texels

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| VX-1, C-10 (2026-11-02 audit) | Approve the visual upgrade order? Batches: Ferric vanilla sweep; Ductilis threads; Saint canon memories; Congeatio; Mortem drains; capstones; Animus mid-tier; Lux; Tenebris; Flammeus; Ductilis bolts; Ferric minor; toolkit enablers. | 43 A, 53 B, 17 C, 2 D (Sanguine Marionette, Vascular Dowsing); all A rows source-only. | Yes; batches 1 to 3 clear every D and the worst ten. | |
| VX-2 | Do the Saint canon memory visuals (Crimson Tithe, Unclosing Eye, Bloom of Rot, Endless Hour) wait for the Canon Memory rework (D-1)? | All C or B. | Wait for D-1. | |
| VX-3 | Keep Vascular Dowsing as a UI-like locator (D grade)? | Legacy locator box. | Keep; low priority. | |
| VX-4 | Add new shader shape indices for Animus FOCUS and SLASH and the Umbra shapes, leaving 4 and 7 alone for Riposte? | Animus FOCUS and SLASH fall back to the glossy droplet; Umbra has three shapes. | Yes. | |
| VX-5 | Seed shared-tier casts per cast so repeats differ? | Seeds come from the entity id, so one caster's casts look identical. | Yes. | |
| VX-6, A14 (2026-11-06 audit) | Approve the 19 vanilla particle sites that stay (block and item debris, the candle's smoke, the vanilla shrieker, copper scrape, the potion swirl on tipped bolts, bubbles, ender vocabulary)? | 154 call sites in 73 files; 133 to replace, 2 dead. | Yes, each with its honest reason in the allow-list. | |
| VX-7 | Sweep the Unstained particles now (batch C, 49 sites) or wait for the Unstained ritual system? | `END_ROD`, `SCRAPE`, `WAX_OFF` and others in rites, Altar, Bells, Retort and effects. | Swap the particles now, no redesign. | |
| VX-8 | Approve a new verdigris colour (86, 160, 132) for Unstained-only effects? | No shared palette class. | Yes. | |
| VX-9 | The Scriptorium's `ENCHANT` letters: delete only, or replace with rising glyphs? | Deleted in the plan; a Will-absorption glow already flies the path. | Delete; add one glyph a tick only if you want letters. | |
| VX-10 | Approve batch T1, the kitbashed UV rescale that changes some models' look (Living Staff heads, Baghnakh, Barbed Blade, Living Crossbow, Blood Crystal, Puffball, podiums, Phlebotomist's Cabinet)? | Up to 1,048 texels per block; one script, checked by eye. | Yes. | |
| VX-11 | Approve batch T2, repainting textures at their layout size (two elytras, Covenant Mantle, Chalybeate and Strider armour, Barbed Shield, Silent Archon leggings, Mnemonic Whale, Ferric Rampart, Polyp, Covenant Throne, Sanguine Omen)? | 32 texels per block for most; the Whale is 8; the Throne borrows the Monolith texture. | Yes. | |
| VX-12 | Approve batch T3, regenerating generator models at density 1 (staff fittings, Living Syringe, Hydra Crown, cylinders, Centrifuge flywheel, Speculum, sarcophagus and pylon items, Crossbar)? | 23 to 42 texels per block. | Yes. | |
| VX-13 | Approve batch T4, baking renderer scale into geometry (Living Blade, Axe, Spear hands, Unstained weapons, Thurible, Lancea, Flail, Cardinal Focus heart, Choir Keeper, Mason's Effigy, Gourd)? | Large: most have no generator in the repo. | Yes, in its own pass. | |
| VX-14 | Accept the Hemolymphopoda and Lantern Tick headpieces as creatures worn as hats? | 40 and 29 texels per block. | Accept (mine). | |
| VX-15 | Three entities bind textures that are missing: point the Leech at a real texture, and delete the Blood Bullet and Will Anchor renderers if those entities are dead? | `model_denizen.png` and `modelwretchedwill.png` are not in assets. | Yes, after checking the concurrent job did not move them. | |
| VX-16, C-11 (2026-11-02 audit) | Which placeholder art next: flat-colour Hemojelly and Myelin Borer, nine cube blocks, 29 shared effect icons, Strider sabatons, the kitbashed Forge and Loom? | All still placeholders. | Mobs first, then the icons (mine). | |
| VX-17 | Bring every A-grade visual through an in-game review before more A work? | All 43 are "pending in-game review". | Yes (see PR-7). | |
| VX-18 | Strands: give PLANT and BRAND per-school looks (a Mortem brand of its own, a Flammeus fire column)? | Shared plant telegraph and brand. | Yes, with batch 5 and 10. | |

## F. Design calls that block content

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| D-3 | What does a Silentium gain at D8 beyond the Silent Archon set? | The vestments, claimed-Will bonus and death refusal; nothing more. | As built until a design lands. | |
| D-6 | Name and recipe of the Chimerization offering? | Parked. | Parked. | |
| D-11 | When, and on what model, do the Unstained get their own ritual system? | Parked; purpose grammar is Harbinger-only. | Parked. | |
| D-20 | Casting v2's unanswered calls: restraint level, the quick-cast flick (dropped), Tier B scope, the Still Art ground ring, an animation library? | As built. | As built; no library. | |
| D-24 | The Lidless Door block beyond the Monolith walk: side, cost, recipe, lore? | Creative-only on the WIP tab. | Stays creative-only. | |
| D-25 | The Chamber gateway's numbers and looks (100 blood to wake the eye, 10 s open, ribs drawn not built, sealed gate from the first visit)? | Unreviewed builder calls. | As built. | |
| D-26 | Lux has no Summa entry: add one, or re-rank a Mediocritas entry? | The Unclosing Eye was its only Summa. | Wait for the Canon Memory rework. | |
| D-27 | Should the Fungal Podium go dormant after the first crossing, like the Spine? | It still carries an Apotheos bodily. | Dormant, leaving Visitation (mine). | |
| D-28 | Molting and tonic numbers: whole points only, negative tempers count, cooldown resets on death, Morphic Mending 8 HP and 500 mL over 10 s, Mind Broken's 15% cut? | As built. | As built. | |
| DS-1 | Should possession stay in the Ancestral recipe-map family? | Filed there by the overnight run. | Keep. | |
| DS-2 | Prune cut-off synapses, or keep their lobe as stakes do? | They keep working (leapfrog risk). | Prune (mine). | |
| DS-3 | Build per-floor ceremony mechanics (Endure, Hold, Share, Precision; sigil modifiers; a participatory procession)? | Out of scope in every spec. | Defer. | |
| DS-4 | Give rites without a chapter Liber entries? | A lore choice left open. | Yes, one line each (mine). | |
| DS-5, C-16 (2026-11-02 audit) | Advanced brewing: grow to eight recipes or fold into tinctures? | Three recipes behind a station tier; four overlapping potion ideas. | Fold into tinctures (mine). | |
| DS-6, D-A (2026-11-02 audit) | Merge the four quest frameworks (assignment ledger, Observances, Circus quests, Court life) into one? | Separate state, screens and packets. | Yes, post-alpha. | |
| DS-7, D-D (2026-11-02 audit) | Give `organic_prism` and `suspended_vivianite` a job, or cut them? | No use (organic prism is a gouge catalyst only). | Cut suspended vivianite; keep the prism (mine). | |
| DS-8, E-5 (2026-11-02 audit) | Finish or cut the leftovers: husk effigies, Vein Spider, Vitality Chalice, Status Gauge, Gourdvine Tap? | Not named by the 2026-11-03 default; still in. | Keep the Gauge; cut the rest (mine). | |
| DS-9, C-12b (2026-11-02 audit) | Write the missing Liber pages: the Hematic Armature, 13 Working crafts, 3 floorless crafts, Vesper, Mycophant, Naeglerophaeon, Phlegethosoma, Bloom Warden, Kindled Champion? | Missing; the manipulation coverage gap closed in round two P9. | Yes (mine). | |
| DS-10 | Build the remaining endgame pieces (fungal mannequin, the Gardens digesting left gear, echo shader, JEI rendering category, hunters converging on a possessed facsimile, Silentium vessel HUD art)? | Not built. | Defer (mine). | |
| DS-11 | Unstained invasion rite? | Far future, parked. | Parked. | |
| PT-1 | Living absorption pays 1 mL a pulse (53 cows for the D1 500 mL proof): raise the rate, lower the proof, or point at reservoirs? | Probe fails; the core loop may have made it moot; re-run before ruling. | Point the hint at reservoirs (mine). | |
| PT-1b | Should projecting into your own cauldron and absorbing back meet the D1 proof? | Counts (500 mL in 16 ticks). | Exclude your own projected blood (mine). | |
| PT-2 | A full vessel's absorption counts 0 toward the proof though the hint says everything counts. | Probe fails. | Keep net; reword the hint (mine). | |
| PT-4 | The Mason's Respite map is never reissued. | No recipe, no reissue. | Reissue when not carried. | |
| PT-6 | Any Blood Shot weave opens the Brotherhood; the Mnemonist's indexing is optional. | Probe fails. | Keep as Side work; say it is optional. | |
| PT-8 | The temple is fragile (breakable Display, re-offered heart, dead farewell); an awakened player at an unclaimed Display is sent to a Hermit who cannot bless them (8b). | Probe fails. | Protect and relink; say the heart cannot attach to awakened blood. | |
| PT-9 | Founder-led reception asks the recruit nothing. | Consent-free. | Add a consent prompt (mine). | |
| PT-10 | A killed Respite Anchorite never returns (a D4 softlock). | Probe fails. | A respawn rule for unique NPCs. | |
| PT-11 | D3 needs a night skip; one awake player blocks it with no line. | Probe fails. | Settle after a minimum time in bed. | |
| PT-12 | The outpost Alembic has no heat and nothing says it needs heat. | Probe fails; may be moot. | Heat the template. | |
| PT-13 | Should outpost stations need a personal craft at all? | Yes, as built. | Keep. | |
| PT-14 | The Vein-Mason lesson follows the live temper: refused carves and free extra patterns. | Probe fails; may be moot. | Persist the issued lesson. | |
| PT-15 | The Hermit Road report is D1-only; a later remnant leaves 1/2 forever. | Probe fails. | Report at any degree. | |
| PT-16 | Creative discard of the starter kit still records the one-time claim. | Probe fails. | Treat it as undelivered, as PendingDelivery does. | |
| PT-17 | Logging out during a Venous Stone collapse or pending structure craft loses the proof. | Probe fails. | Record by UUID, apply at next login. | |
| PT-18 | A second assigned spin overwrites the First Separation proof. | Probe fails. | Refuse a second spin until the first is recovered (mine). | |
| PT-19 | Reissues check only the carried inventory. | Stashed or dropped vials are reissued. | Keep as a lost-item safety net (mine). | |
| PT-20 | A weaver offline at the Loom climax loses the first-weave milestone. | Probe fails. | Record by UUID at next login. | |
| PT-22 | A section already Varicose at D5 never counts as the Mason's strain. | Probe fails. | Count it once. | |
| PT-23 | An obstructed outpost marker silently drops its resident. | Probe fails. | A fallback spot. | |
| PT-24 | A guided Chamber visit left early (logout, death protection) completes the lesson. | Probe fails. | Complete only on timeout or the paired exit. | |
| PT-26 | Taking the charm off while Concentrated Blood is pending leaves the player at D2 with no explanation. | Probe fails. | A line on waking. | |
| PT-27 | The D5 Monolith scrap can be taken again and again. | Probe fails. | One per player. | |
| PT-28 | A refused reward can leave the client on a "claimed" node with no line. | Probe fails. | A refusal line wherever a reward can be refused. | |
| PT-29 | With vascular degradation off, the D5 Mason strain cannot be finished. | `vascularDebuffsEnabled` defaults to false. | A fallback strain route (mine). | |
| PT-30 | A lost scar blank before the first carve cannot be replaced. | Probe fails. | Let the Anchorite trade one. | |
| PT-31 | No NPC names the D4 Sporitic Thurible. | Probe fails. | An Alchemist or Artificer line. | |
| PT-32 | A D4 player at a D5 Covenant floor is told no rite matches. | Probe fails. | Show the degree requirement. | |
| PT-33 | The Specimen Jar lesson is offered only from the D2 tree. | A D3 player who skipped it never sees it. | Offer it in the D3+ trees. | |
| PT-36 | Bloodline members can recruit and mint ledgers but cannot leave or manage the line. | Probe fails. | Add a leave action (also PT-90). | |
| PT-37 | A kicked member's old ledger does nothing and says nothing. | Probe fails. | Refuse with a line. | |
| PT-38 | The warning-only first Unbinding still burns its Nether Star; Disband is free. | Probe fails. | Warn at activation. | |
| PT-41 | The Basin commission reads only the main hand; commissions give no reward. | Probe fails. | Any-hand check and a reward. | |
| PT-42 | A deposit into a full pool earns no proof and says nothing. | Probe fails. | A "pool is full" line. | |
| PT-44 | An offline member's share of the pool is lost when the line is dissolved. | Probe fails. | Bank it and pay at login. | |
| PT-64 | Only a progenitor binds a Covenant Throne. | Probe fails. | Members bind the leader's Throne. | |
| PT-65 | Any progenitor binds any Throne. | Probe fails. | Require the binder's own Fane. | |
| PT-66 | Eternal Covenant is lost on death and repeatable. | S-1 family. | Move it with S-1. | |
| PT-67 | The Chamber rite attunes even when no visit can start. | Probe fails. | Refuse before completion. | |
| PT-68 | A Formation medium seats only on a sneak-click, with no hint. | Probe fails. | Seat on a plain click when the floor wants one. | |
| PT-70 | A dissolved line's progenitor keeps the Throne bond, unmentioned. | Probe fails. | Clear it with the Fane. | |
| PT-72 | Casting and rites lock from the ninth pome to D8, undocumented. | Probe fails. | Document it and warn before the ninth pome. | |
| PT-73 | Any D7 can shatter another player's Monolith. | Probe fails. | Bind the Monolith to its builder. | |
| PT-74 | A low shatter roll before the cornerstone leaves the Weight of the Frame short. | Probe fails. | Raise the minimum roll (mine). | |
| PT-75 | Logging out mid-projection grants the revelation. | Probe fails. | Pause and resume. | |
| PT-77 | The atlas shows D7 and D8 materials at D5. | Probe fails. | Keep them as previews (mine). | |
| PT-78 | Nothing names the Fungal Spine after Communion. | Probe fails. | A Vicar or Monolith line. | |
| PT-79 | Nothing names the Severance to a D7 Silence. | Probe fails. | A Vicar or Mnemonist line. | |
| PT-80 | A heart-node respawn can trap an Apotheos in the Gardens. | Probe fails. | Respawn at their bed (mine). | |
| PT-82 | The Rite of Apotheos is not discoverable when it is needed. | Probe fails. | Unlock the Truth entry at APOTHEOS_PENDING. | |
| PT-88 | A solo caster deadlocks in a grand rite's ordeal (dry, damaged woken rings). | Probe fails; may be moot. | Fill a dry anchor before repairing it. | |
| PT-89 | The revelation choice waits behind memo whispers in the inbox. | Probe fails. | Open decision whispers first. | |
| PT-90 | Bloodline members can never reach D7 (no leave, no Throne, no second line). | Probe fails. | Add a leave action. | |

## G. Saints and mausoleums

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| D-1 | Review the Canon Memory rework draft and answer its 17 numbered questions (`superpowers/specs/2026-11-06-canon-memory-rework-design.md` §4)? | Nothing built until approved. | Review it; it shapes the next content pass. | |
| M-1 | Hematic iron plating in `ductilis_conductors` or `laboring_arms_resistant`? | Not in them. | No: plating is architecture. | |
| M-2 | Charred planks in any planks or wooden tag? | Not tagged; lacquer stays out. | No. | |
| M-3 | Keep Hemorath's heartbeat quickening toward the pit (70 to 45 ticks)? | Built. | Keep. | |
| M-4 | Sound picks once heard: Seraphae's choir (beacon hum), Putriciel's creak, Velorum's glass or amethyst creak? | Unheard. | Decide in the played session. | |
| C-3 | The sarcophagus has no D8 gate: a relic, Foul Paste or a misaligned vial wakes a Saint at any degree. Accept the ward and door as the gate? | No Degree check on Saint, sarcophagus or relic. | Accept: the door is the gate. | |
| C-6 | Gate the canon recipes at D8, and stop `mausoleum_treasure` dropping `memory_crimson_tithe`? | Recollections are already in `never_sold`; the Loom has no degree gate. | Gate at D8; drop the direct memory. | |
| SM-1 | Close the enrage dodges: a non-Apotheos can wake the tomb for an Apotheos; peaceful syringe draws give whole residuum after each return? | Both possible. | Close both (mine). | |
| SM-2 | How many Hallowed Residuum fragments make a whole one? | Three (builder's call). | Three. | |
| SM-3 | Write the owed Chapel memory content (Hemorath's students sealing the vessel; Seraphae's "four touches, four growths")? | Owed. | Yes (mine). | |
| SM-4 | Retire the old `IBloodVolume` blood debt now Hemorath no longer uses it? | `HarbingerSaintEncounterHooks` still resets it. | Retire. | |
| SM-5 | Hemorath's looks: is his Brimming body changed enough, and a flooded rake lane (a red band with a lit cut edge) legible, or darken the pool at some cost to the telegraph? | Light floor 8 on his bone. | Darken the pool slightly (mine). | |
| SM-6 | Seraphae's looks: keep the light floor of 12 and her stronger telegraphs, and the overgrowth that reads as brain or intestine? | As built. | Keep both (mine). | |
| SM-7 | Putriciel's palette and pose: keep pale mould over olive and the Offering palm at the brow; give a 12-block Bloom's growths more mass? | The colonised floor reads mostly as a pattern. | Keep palette; more mass (mine). | |
| SM-8 | Putriciel's revision: push the black soot on the body fire or accept two-tone; lower the flesh mottle's contrast; thicken the sludge? | Two-tone fire, busy mottle, slightly smooth sludge. | Push soot, lower mottle, thicken sludge (mine). | |
| SM-9 | Velorum's Endless Hour darkness, and the shared RIME school's red crystals at his challengers' feet: keep? | As built. | Keep (mine). | |
| SM-10 | Velorum's floor-only wind-ups (Lantern Sweep, Staff Toll, Draw of Silence) read as quiet frost: make them bolder? | Pale frost on blue-black. | Bolder (focal moves should be bold). | |
| SM-11 | Velorum's gloam fog hides the umbral shapes from high cameras: should the fog spare them? | Fog 4 near, 14 far. | Spare the shapes (mine). | |
| SM-12 | Drop the frost-white lips on Velorum's mark ring and welled seams (the shared seam shape 0, so every seam in the fight changes)? | As built. | Drop them (mine). | |
| C-9 (2026-11-02 audit) | Fix the Saint presentation gaps: uneven endings, an enraged Saint looking normal, phase cards only for Hemorath, Seraphae raising vanilla Vexes, vanilla boss voices, silent Court bosses? | Part of D-2's follow-up. | Yes, after a played session per Saint. | |
| SM-13 | Natural mausoleum placement: is the 14-block entrance check refusing too many steep sites? | One natural run since it widened. | Run natural mode for all four first (see PR-3). | |

## H. Brined Court, Circus and NPCs

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| D-12 | Brined Court elder challenges and fuller Court trading? | Deferred by you; the Champion title gives recognition only. | Parked. | |
| D-22 | Can the player use the Vent Sanctum threshold chimney, what happens to someone who enters Hydra, and should other players' Morphling mutations obey a client setting? | Not enterable; always visible. | Not enterable; always visible. | |
| HN-1 | Should the Lighthouse Garden need a true shoreline instead of raising its own tide rock? | Raises its own rock. | As built. | |
| HN-2 | Is Mermaid's Purse's +35% a ceiling on total swim speed with the Prismatic set, or the strain's own share? | A ceiling: inside the full set the strain adds nothing. | The strain's own share (mine). | |
| HN-3 | Does Sun-Fed's hunger-to-blood surplus count underwater near a daylit surface? | Open. | Yes, within 5 blocks of a lit surface (mine). | |
| HN-4 | Fix the strain looks: Ink Cap's cap above its Primal hood, Holdfast and Sunburst small at Mature, Rope strands faint in daylight? | Left for later. | Yes (mine). | |
| HN-5 | Lore: when a Marimo follower petrifies; Last Marrow elders when a fall is exhausted; customs for passage and disputes; how a house takes a migrant? | Open lore. | Answer in a lore round (mine). | |
| HN-6 | Deferred by ruling: elders challenging a title-holder, the Tier 3 pole conflict, Sleeper waking, house greetings, Court lines in a house voice. Still deferred? | Deferred. | Still deferred. | |
| HN-7, D-B (2026-11-02 audit) | Derive Court rapport from standing, so there is one "how well they know you" ladder? | Two ladders (standing and rapport). | Yes, post-alpha. | |
| HN-8 | Should the Mnemonist also hint at the Court? | Proposed no, not confirmed. | No. | |
| PT-45 | A custom given before the site visit locks the player as a guest. | Probe fails. | Order-independent standing. | |
| PT-46 | Escaping a usurpation by logout costs no wait; dying costs a day. | Probe fails. | The same wait for an escape. | |
| PT-47 | Ask to join spends the free switch with no warning. | Probe fails. | A confirm step. | |
| PT-48 | The Champion challenge has no confirmation. | Probe fails. | A confirm node. | |
| PT-49 | Nothing points to the Shared Wake's nursery. | Probe fails. | A pointer line. | |
| PT-50 | Nothing locates the Vent Sanctum or Court outposts. | Probe fails. | Residents name places (mine). | |
| PT-51 | The Lightkeeper's feeding line misdescribes the bonus. | Probe fails. | Reword. | |
| PT-52 | The Champion has no degree gate. | Probe fails. | A D5 gate. | |
| PT-53 | With Pelagic worldgen off, NPCs still point to the Court. | Probe fails. | Hide the pointers. | |
| PT-54 | No Court NPC speaks past D5 or about Primal. | Probe fails. | Add lines. | |
| PT-55 | Court members can be leashed; the Lamp-colony's leads tie up the next member clicked. | Probe fails. | No leashing, as for villagers. | |
| D-14 | Is anything from the underwater boundary doc's deferred pool still owed to the Circus? | Most items are now the Court's. | Nothing. | |
| D-18, D-33 | Circus mini-bosses toward the Ringmaster (C-13), the snake creature, the staff form: build or strike? Move the Ringmaster onto the generic boss kit, and do mini-bosses replace acts 2 to 4? | Own controller with the kit's wind-ups and telegraphs; no ladder. | Move to the kit; strike until the Circus pass. | |
| D-29, PT-57 | Reach the Ringmaster: build the per-viewer galleries as designed, or real galleries and a reachable perch for everyone? | Never built; the idle perch cannot be reached from the floor. | Real galleries for everyone; acclimation changes only the look (mine). | |
| D-30 | Should the pact or finale need D4 and school work, and should first talks give acclimation? | No Degree or school gate; first talks count. | D4 and one school report (mine). | |
| D-31 | The Unstained in the Circus: refuse, perform but not teach, or Liberation only? | An Unstained Successor silently loses the Pattern. | Liberation only (mine). | |
| D-32, PT-21, PT-58 | Add a D3 pointer to the Circus before the D4 Waybill, and something pointing at the abandoned stage? | None. | A D3 teaser line; a Waybill mode for the stage (mine). | |
| D-34 | Should puppet damage count as the owner's, should area attacks spare bystanders, and solo or co-op finale? | Owner hits only, bystanders hit, solo. | Count puppets; spare bystanders; solo (mine). | |
| D-35 | Liberation's cut: keep right-click cutting with Thread Ripper as the prize, or grant it earlier and require it? May it unravel Vesper's puppets? | Right-click cuts; it works on Vesper's bodies. | Keep; not on Vesper's bodies (mine). | |
| D-36, PT-62 | Performers killed in a failed Liberation, survivors after a ruin, a Succession yield scene, Ringmaster music? | No return, no yield scene, no music; a lost Ringmaster releases the site. | A yield scene and music; a site with no Ringmaster leaves the Waybill (mine). | |
| D-37 | Where do the mental verbs live: one act per body, Circus memories or staff forms, or docs only? | Only the Mummer's automatic taunt. | One act per body (mine). | |
| D-38 | Ringmaster Pattern: a free slot, a group act, or a weak extra body? Should it sit in the transposition ring? | Weak extra body in the ring. | A group act (mine). | |
| D-39 | Re-role the Vulture or the Juggler (both D3 ranged volleys)? | As built. | Re-role the Juggler (mine). | |
| D-40 | Puppet Interference: keep both penalties or one, and add a Court-Circus synergy? | Both; no synergy. | Keep both; no synergy. | |
| D-41 | Rebuild Circus NPCs on the jointed rig now or later; are faculty costumes exempt from the no-brass rule? | Later; not exempt. | Later; not exempt. | |
| D-42 | Rebuild the pavilion and camps in a new generator, or re-dress the old shell? | The pavilion's builder cannot run. | Rebuild. | |
| D-43 | Which set pieces each site gets, and how rare pavilions (48/16) and camps (24/8) are? | As built. | Set out in the rebuild (mine). | |
| D-44 | Do Circus sites hold loot, and what does a finished pavilion look like? | No loot; six cobwebs. | Gifts through NPCs; a visible ruin or new tenancy (mine). | |
| D-45 | Guest pay: automatic PendingDelivery claim or collect from a Threadkeeper; one act a day per school or per professor? | Collect; per school. | As built; fix the ledger line. | |
| PT-59 | Practicals cannot finish in Peaceful. | Probe passed in the 2026-11-07 runs with no code change; recheck before ruling. | Recheck, then a Peaceful refusal line (mine). | |
| PT-60 | The Threadkeeper's lesson node is unreachable. | Probe fails. | Give the Threadkeeper an option to it. | |
| PT-61 | The claimed and not-ready finale lines mislead. | Probe fails. | Separate lines. | |
| PT-63 | One killed rehearsal threat stalls a two-threat practical silently. | Probe fails. | End the practical with the retry line. | |
| HN-9 | Give the trade box a Circus theme and crest? | Default crest; separate art job. | Yes, with the Circus pass (mine). | |
| D-23 | Monolith shattering loses the free Cornerstone with no warning: add a line? | Silent. | Add a warning line. | |
| PT-3 | One-time NPC rewards (7 of 10) and shared station outputs drop unowned, so another player can take them. | Probe fails. | PendingDelivery claims. | |
| PT-5 | The D5 Vicar gives the Monolith blueprint, but the Monolith is a D6 craft. | Probe fails. | Allow the craft at D5 (mine). | |
| PT-7 | The Vicar's D3 and D4 hints do not name the real gate (the Loom weave, the Vein-Mason loadout). | Probe fails. | Rewrite both in his voice. | |
| PT-35 | A signed ledger joins anyone (D0, inactive blood) with no leader consent. | Probe fails. | D1+ with active blood; the leader confirms. | |
| PT-39 | No NPC says where Sanguine Quintessence comes from. | Probe fails; may be moot. | A Vicar or Alchemist line. | |
| PT-43 | The Monolith blueprint is offered only at exactly D5. | Probe fails. | Offer at D5+. | |
| PT-56 | The custom trade card pays rapport; the dialogue offering does not. | Probe fails. | Both pay. | |
| PT-69 | A lineless D6 is still sent to bind a Throne to their Fane. | Probe fails. | Tell them to found a line first. | |
| PT-71 | The D6 Anchorite counsel is a lore card, not a quest. | Probe fails. | A Quest Work card with a sigil. | |
| PT-83 | A D8 Silentium is greeted with the D7 Archon tree. | Probe fails. | A Silentium tree. | |
| PT-85 | A ledger summon strips a shared outpost NPC for good. | Probe fails. | Send home after use. | |
| PT-86 | The D5 and D6 Main steps are one-read lore cards, not quests. | Probe fails. | A Quest card with a sigil per step. | |
| PT-87 | The default bloodline name is stored English. | Probe fails. | A translatable default name. | |

## I. Canon and dialogue

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| C-1 | Promote the Saints from "post-release WIP" in LORE_REFERENCE, HEMOMANCY_REFERENCE §38 and the pinned wiki page? | The four complexes generate. | Promote to "implemented, overhauls pending"; update the pin. | |
| C-2 | "Severance" is the Qliphoth cut, but the founder cure route and `silent_severance` use it too. Rename? | Not renamed. | Rename the armour ability; the cure route keeps its name. | |
| C-4 | LORE_REFERENCE §3.3 "Transcendence (Late/DLC)" overlaps the Apotheos identity: keep, merge or retire? | Separate section. | Fold into the Apotheos section. | |
| C-7 | Promote the Annetta encounter from "Partial" in the reference? | Listed Partial with polish passes outstanding. | Promote; the lancea lore line is FX-21. | |
| C-8 | Add an Apotheos section to the Liber? | None exists (the AGENTS.md half is done). | Add it with D-1. | |
| CD-1 | Should the Brine-Sealed Letter's item inquiry offer a chart option again? | It used to. | Yes (mine). | |
| CD-2 | Is `custom.opens` narration or the Gardener's voice? | A trade-card label. | Narration (mine). | |
| CD-3 | `spine_growth.degree8.canon` is never sent and reads wrong for a Silentium, and `coreWitnessDialogue`'s Javadoc disagrees with lore: cut the line and fix the Javadoc? | Open. | Cut and fix (mine). | |
| CD-4 | Cut "In time" from the Holdfast inquiry line so it does not read as permanent rooting? | "In time it will hold you to stone." | Cut it. | |
| CD-5 | NPC_VOICES 8: Annetta speaks the player's line (`annetta.draught.truth.line1`). Fix? | Open. | Give the line to the player (mine). | |
| CD-6 | NPC_VOICES 10: successors reuse the teacher's tree past the greeting. Give them their own? | Open. | Later (mine). | |
| CD-7 | NPC_VOICES 11: shared keys (Circus status, Unstained Observance, newcomer and item-hint templates) flatten voices. Split them per speaker? | Open. | Yes, speaker by speaker (mine). | |
| CD-8 | NPC_VOICES 13: the performer barks `dialogue.circus_<role>.line1-3` are dead text. Wire or delete? | Dead. | Delete (mine). | |
| CD-9 | NPC_VOICES 14: `votary.temper_lore` lists enzyme names as tempers; `item_inquiry.clairaudiograph` lines 2 to 4 are identical. Fix? | Open. | Fix both (mine). | |
| CD-10 | The Artificer offers the Silent Archon vestments at D7 while lore puts them after Silentium. Which moves? | Lines follow the code. | Code follows lore: offer after Silentium (mine). | |
| CD-11 | The Hermit's `illuminatus.hint` ("Find allies who share your blood"): does the Bloodline Covenant need a bloodline? | Unchecked. | Check and reword if not (mine). | |
| CD-12 | The Monolith steps' Degree numbering differs between LORE §6.5a and §10. Which is right? | Two numberings. | Follow the code (mine). | |
| CD-13 | Should the Wayfarer name "the Court" at D5+? | Assumes the crew speaks of it openly; not blocking. | Keep. | |
| CD-14, DL-27 | Write Mnemonist inquiries for the 41 live memories that have none (Memory of Vesper included)? | Unanswered. | Yes (mine). | |

## J. Architecture, tests and housekeeping

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| G-1, F-18 | Explicit damage stages and one death-save stage? | Pending; changes blood lost per hit; needs an ordering test. | Approve before the next balance pass (mine). | |
| F-16 | Attribute AoE damage from armour, Still Arts, Sheolic and Saprovitta, and respect allies and PvP (sites move to `Reach.STRICT`)? | Sites name their preset; pending. | Approve. | |
| G-2b | Stop the caster's own puppets matching `Monster.class` filters? | `allies = false` in `Reach.MONSTER_FILTER`; Drudges need their owner resolved. | Approve. | |
| G-2c, CQ-12 | One boss definition? | `isBoss` and `ThreadRipperManip.protectedBody` disagree; the core loop added three more readers (specimen capture, menagerie, the binding's tail). | Approve; fold all readers in (also D-33). | |
| G-7b | Should a flight revoke check the other flight sources? | Elytra landing drops Edacious flight until its next tick. | Approve. | |
| F-13 | Scar upkeep reserve-protected? | One flag; the roll-up says it may be answered by Track 4. | Confirm, then approve. | |
| F-14 | Should passive scar effects survive milk? | Pending design. | Yes (mine). | |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve? | Pending. | Approve. | |
| F-17 | Curved Horn uses `LastRiteHelper`? | Pending. | Approve. | |
| F-11, PT-34 | Blood-structure crafts tick once per server tick? | They count down once per loaded level (about 3x early). | Approve. | |
| F-4 | ScaleGrip respects cancelled deaths and persists pending returns? | Pending. | Approve. | |
| F-19 | Station kits follow the PendingDelivery contract? | Pending. | Approve. | |
| F-20 | Resync Ichorian knowledge on dimension change? | One line. | Approve. | |
| S-2b | Resend skill progress on respawn and dimension change? | Login-only. | Approve. | |
| S-5b | Concentrated Blood replacement vial counts confirmed insertion? | Add-or-drop. | Approve. | |
| F-12 | Projected blood honours the rite lock? | Round one made stations honour `canReceiveBlood`; may be answered. | Re-run the GameTest, then close (mine). | |
| A-4 (arch) | Which wall-clock animations freeze under `/tick freeze`? | 29 client classes listed. | Freeze all but screen chrome (mine). | |
| V-3b | Ferric's 128 seed-tile constant against the shared 127? | Pending. | Use 127 (mine). | |
| V-3c | Write Outpouring surface marks through `MaterialQuads`? | Alpha rounded by hand. | Approve. | |
| V-6b | Fix the Mycophant spore colours given on the 0 to 1 scale (they render black)? | Pending. | Approve. | |
| M-3 (arch) | Delete the remaining "requires verification" dead code (`IBloodRoutingTarget`, two orphan GUI textures)? | Pending. | Delete to `removed_assets/`. | |
| O-5 | Typed components for reworked items? | Applies only to items being reworked. | Approve. | |
| F-22 | Hide the legacy Dictation Table from the main creative tab? | A no-op check. | Approve. | |
| CQ-13 | Paste the core loop's suggested AGENTS.md lines (verbs, strands, living blood, Temper, keeping, ground marks, crops, Staff, Living Land, world answer, teaching, pose order) and add `tools/circus/` to the tracked-tools bullet? | AGENTS.md not edited. | Paste them after the CL rulings. | |
| CQ-14 | Move the core loop track files to `docs/archive/tracks/core_loop/`? | Kept in `docs/archive/tracks/core_loop/`. | After you rule the CL ids. | |
| D-46 | Is one to two more minutes on `alphaCheck` acceptable for the gated compact finale? | Already in the gated batch since 2026-11-07. | Yes. | |
| HK-1, B-10 (2026-11-02 audit) | Keep the `lowtide_ruin/*` structure NBTs as a regeneration source? | Kept, reachable from nothing. | Keep. | |
| HK-2, E-10 (2026-11-02 audit) | Remove the last save-compatibility leftovers (the L-7 block-blood reads; the origin-keyed pome count)? | Left; they also carry sync and rite snapshots. | Remove when next touched (mine). | |
| HK-3 | Fix the opt-in playthrough suite's eight deterministic required failures, and re-run the probes that may be moot (F-12, F-13, PT-1, PT-1b, PT-2, PT-12, PT-13, PT-14, PT-25, PT-39, PT-88, D-26)? | Unchanged since before the core loop run. | Yes, one session (mine). | |

## K. Decided without you, awaiting review

"Ok" keeps the call. Each row names what a session decided.

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| RV-1 | Heart-node: keep cut-off synapses working, no withered-heart gate, no support rule, toggle labels and slider notches, no Liber line? | Built. | Keep; see DS-2. | |
| RV-2 | Apotheos body: spore gain 2/s in the dimension and 0.5/s in the biome, Burn Spores 25 for 6 HP on a 100-tick cooldown, symbiotic split off? | Built. | Keep. | |
| RV-3 | Possession: surrogate stats (bat 3 to golem 14), hit cost 1 or 2, Mind Broken 6000 ticks with no cure, applied at the return's cut? | Built. | Keep. | |
| RV-4 | Gaze and grab, adipocere: 1 Concentration per 20 ticks within 10 blocks, grab gives Mind Broken 12000, 35% revenant drop, +3 Concentration offering? | Built. | Keep. | |
| RV-5 | Chapels and door: trial numbers, ring door by held Projection (1000 over 200 ticks), enrage per skipped Chapel, Seraphae's flower forest, cysts 1 in 24 chunks? | Built. | Keep. | |
| RV-6 | Mausoleum layout: spiral fold-back, chamber at -39, ward past the threshold, unbreakable set pieces, depth grading and lamps? | Built. | Keep. | |
| RV-7 | Brined Court: house customs, food fed only when hungry, rest heal 8 a minute, one school per 1024 region, Head-tender calls on approach, Champion and Warden numbers, polyp spawns 1 in 40? | Built. | Keep. | |
| RV-8 | The Blood Wood tree as a low fork of arterial limbs with separate leaf crowns and hanging strands? | Built. | Keep. | |
| RV-9 | The 2026-11-02 audit's section E defaults (Glaive for 4 pale silver at stage 3, Rib Reliquary from Hemorath, the Omen recipe, Dynasty chests)? | Built 2026-11-03. | Keep. | |
| RV-10 | Section D renames (`enthralling_filament`, `ferric_rampart`, `harbinger_lodestone`, `barbed_splinter` and others; Rib Reliquary; Bog Iron smelts to a nugget)? | Built. | Keep. | |
| RV-11 | Heartwood numbers (Dead Bush 1,800 mL, core fills at 1,800 feed, players bleed within 4 blocks, core item unobtainable, recoloured oak art, no boats)? | Built. | Keep. | |
| RV-12 | NPC trade: box order, one card per option, all-or-nothing payment, D0 may trade with the Unstained, catalogue substitutions, Known and Trusted lines only, daily cards capped at 2? | Built; rapport lore content and trade balance unreviewed. | Keep; review balance in play. | |
| RV-13 | The Monolith's trade cards, rapport, counsel, crest, portraits, 6000-tick open window and the hidden Press further button below D7? | Built 2026-11-05. | Keep. | |
| RV-14 | Possession rite: blood cost 0 and the Qliphoth grand floor borrowed as a special bond rite? | Built. | Keep. | |
| RV-15 | Circus trade and visuals: leavings card dropped, D4 bell carrot, act-themed performer cards, model rebuilds authored from previews? | Built. | Keep. | |
| RV-16 | Circus round 1: lost Ringmaster releases the site, CAROUSEL counts performers still there, Waybill search capped at 32 cells, scripted lanterns, unseen telegraph looks? | Built 2026-11-07. | Keep. | |
| RV-17 | Circus implementation: Thread Ripper numbers, the 32-block Pattern relay, acclimation thresholds and rate, the Luminal Cicada as the one creature? | Built. | Keep. | |
| RV-18 | Boss presentation: every frame and opening, 50-tick phase cards, bars hidden under openings, skip by holding sneak 20 ticks, the title-card wording? | Built. | Keep. | |
| RV-19 | The shared Saint combat kit (band set, wind-up floors 12 and 20, the 600-tick empty-chamber leash, challenger scaling, deeds as moves)? | Built 2026-11-05. | Keep. | |
| RV-20 | The four Saint fights' numbers and moves (each design's section 20) and the two voice sheets? | Built; only Hemorath played. | Keep until each is played. | |
| RV-21 | Boss fights: Vesper's wound sites and rates, the Sickle act at 33%; Mycophant band numbers, 8% fusion bonus, Nursery ceiling; Annetta's pools and 1 HP guard? | Built. | Keep. | |
| RV-22 | Rite merge: Rites & Crafts tab layout, special floor styles, the Omen as the only rite-bound member, the twenty-craft roster's tiers and feeds, the Loom trimmed to 3x2? | Built. | Keep. | |
| RV-23 | Cast impacts, basics and infusion: delivery by school geometry, the 5-tick impact throttle, six body-anchored forms, sore and melt overlays? | Built. | Keep. | |
| RV-24 | Manipulation balance passes 1 and 2: Endless Hour overflow-only deferral, Marionette as a Binding form, Living Circuit falls back to the caster, the charge clamp, kill alignment split by profile? | Built. | Keep. | |
| RV-25 | Creative tab filing of ambiguous items (puppet mounts to Troupe, Silentium and possession to Gardens, bone bricks and gutters to Saints, the moth to Unstained)? | Built. | Keep. | |
| RV-26 | Fane ground: SURFACE style by default, floors only, outsider tint unchanged, domes separate per stake? | Built. | Keep. | |
| RV-27 | The wayside and Harbinger site rebuilds, the Brined Votary and heart-node remodels? | Built 2026-10-31; not walked. | Keep until walked. | |
| RV-28 | Core loop round one prices and paces (Shapings 4 to 48 mL/s, strands 15 to 60, the 300 mL key, 2 mL per hardness point, Blood Moons one night in fourteen)? | All first values. | Keep; tune with sections A and C. | |
| RV-29 | The verb key on Y also conjures and dispels the hand? | Built. | Keep. | |
| RV-30 | One reach for both verbs: bare Absorption 5.0 to 5.5 blocks, the Staff projects as far as it absorbs? | Built. | Keep. | |
| RV-31 | A Staff tap of 20 ticks or less that moved no blood calls the Primal Morphling? | Built. | Keep. | |
| RV-32 | Strands play no cast outpouring (it buried the strand in first person)? | Built. | Keep. | |
| RV-33 | Tendon Line's replacement offering is a new Sinew Cord; the Constrictor Cord retired, its slow kept as the binding's tail? | Built. | Keep. | |
| RV-34 | The Shaping homes: gouge on Vascular Dowsing, Lux trace on Hematic Beacon, item draw on Sanguine Magnetism, growth on Vital Effusion, chum by plain Projection? | Built. | Keep. | |
| RV-35 | Mining: the pickaxe taught with the bond, the cut's shape by stance, digging forms counting toward Staff age, the axe paying per block? | Built. | Keep. | |
| RV-36 | Venous Cling as a new Animus Humilis memory, transient, upkeep through `payUpkeep` with no Temper after the root? | Built. | Keep. | |
| RV-37 | "Crossing" for the Spine's trip, and the first nine core-loop firsts with their parents? | Built. | Keep. | |
| RV-38 | The `/hemo` core-loop fixtures as dev scenarios rather than `<Name>Subcommand` classes? | Built in Track 11. | Keep (mine). | |
| RV-39 | P1: a random, chunk-aligned GameTest origin with 2 margin chunks (about 50% more run time)? | Built. | Keep. | |
| RV-40 | P1: Hematic Beacon's trace keeps its heading at a wall instead of dying at three cells? | Built. | Keep. | |
| RV-41 | P1: Shared Wake school followers fold into a line along a shelf? | Built. | Keep. | |
| RV-42 | P2: the Vicar's newcomer card leaves when the player's blood wakes, and the met-Vicar flag is set by his right-click? | Built. | Keep. | |
| RV-43 | P2: a caster's own rite claims their Projection over its ritual bounds (no infusion, no stain)? | Built. | Keep; see CN-2. | |
| RV-44 | P2: a Saint's tomb closes over any replaceable cell (air, water, stains, grass)? | Built. | Keep. | |
| RV-45 | P3: an ownerless or absent-owner run smelts and holds crops back but lights and freezes nothing? | Built. | Keep. | |
| RV-46 | P3: a catalyst feeder keeps a run for whoever lit it, never for whoever stocked it? | Built. | Keep. | |
| RV-47 | P3: a drip-made Condenser takes the tank owner's crystallization when online, 0 otherwise; feeding prefers any fresh basin? | Built. | Keep. | |
| RV-48 | P3: the rite collapse backlash, Engram Stamp and Crimson Fireball edits listed, not routed through `PlayerBlockEdits`? | Listed in the allow-list. | Keep. | |
| RV-49 | P3: the client strand pace grace of 6 use ticks? | Built. | Keep. | |
| RV-50 | P4: narrow Shapings tried in attach order before the main; an open held channel beats every claim? | Built. | Keep. | |
| RV-51 | P4: Sanguine Mending mends dropped gear 10 a pulse, golems 3 HP, and an anvil one stage per 8 paid pulses; worn gear stays the R cast's? | Built. | Keep. | |
| RV-52 | P4: Crimson Flame kindles 2 lights a pulse within 2 and primes TNT only when it is the struck block? | Built. | Keep. | |
| RV-53 | P4: Glacial Grasp freezes open water (ice melts back to water) and thaws vanilla and frosted ice, never packed or blue? | Built. | Keep. | |
| RV-54 | P4: Glacial Rampart leaves cells with bodies open and held on its own wall keeps it standing; dry walls stand shorter, never under a quarter? | Built. | Keep. | |
| RV-55 | P4: Ferric Rampart claims any face with room, faces across the sweep, caps 12 standing within 64 blocks? | Built. | Keep. | |
| RV-56 | P4: a blood tool beats a block's own use whenever the selected Shaping claims the aim? | Built. | Keep. | |
| RV-57 | P5: strand skill numbers (Long Strand x1.1 to 1.3, Taut Line, Free Hand +0.1, Whipcrack +15%, Deep Brand 20/15/10 ticks)? | Built. | Keep. | |
| RV-58 | P5: Cruor Javelin is Congeatio's jet (no new memory); about 3.5 spikes per block of eruption radius? | Built. | Keep. | |
| RV-59 | P5: first-person strand arm angles and school looks (Tenebris umbra over violet-white, Ferric plant drawn in blood, prism light white, gold, crimson, rose)? | Built. | Keep. | |
| RV-60 | P6: the vessel screen as a slotless menu with no payload, 300 by 236 pixels, Met Creatures listed first? | Built. | Keep. | |
| RV-61 | P6: the brood design (adjacent vessels, same owner, parting a pair loses what was paid, inheritance odds, hostile kinds allowed)? | Built. | Keep. | |
| RV-62 | P6: the brood thread arcs over the lids; the birth bloom drawn from a block event? | Built. | Keep. | |
| RV-63 | P7: strains as bush blocks the cloche does not grow; 1 in 6 growth, no light, bone meal +1 stage on fed ground? | Built. | Keep. | |
| RV-64 | P7: the strain-bed first granted on cutting a ripe strain, Harbingers only? | Built. | Keep. | |
| RV-65 | P7: catalyst crops worth 2; ripe glowroot light 4, Incandescent strains 3? | Built. | Keep. | |
| RV-66 | P7: chum hunters spawn as events and despawn normally, cap 1; the elder guardian curse sound at low volume? | Built. | Keep. | |
| RV-67 | P7: atlas placement (timber at D3, crops and Tainted Harvest at D2)? | Built. | Keep. | |
| RV-68 | P8: field numbers (20 ticks per light level, max 2,400, radius 3 to 8, soft edge 1.5, dry floor a quarter), one field per player, centred on the aimed block? | Built. | Keep. | |
| RV-69 | P8: fields are session state, so a restart lifts them? | Built. | Keep. | |
| RV-70 | P8: ground runs drawn before translucent blocks; the snuff stream on the Mortem flow material? | Built. | Keep. | |
| RV-71 | P8: the Vicar's new lines, written without your line-by-line review? | Built. | Read them (mine). | |
| RV-72 | P8: JEI gouge pages in function order (light, hot, point, cold, pull, feed, dark)? | Built. | Keep. | |
| RV-73 | P9: What the Dead Give is granted on each draw even at the Temper cap; Stoked is not gated on the owner's blood? | Built. | Keep. | |
| RV-74 | P9: learning a memory opens its school's pages; Liber Immaculatus floors are purity and clarity stages; Annetta's page opens on starting her route? | Built. | Keep. | |
| RV-75 | CQ-18: Crimson Flame's kindling became a narrow Shaping ahead of the fire plant strand? | Built in P5. | Keep. | |
| RV-76 | ENDGAME overnight calls still standing: spore load numbers, Facsimile roster, network alarm and antibodies, heart-node recipe and forced respawn, adipocere crust placement? | Built. | Keep. | |
| RV-77 | Mind Broken sends no fungal whisper, because a Silentium hears nothing? | Built. | Keep. | |
| RV-78 | Brined Court: Midnight's cold split, the Wake nursery cell rate, the Screened Cleft flooded, the Bell as the deep outpost with air? | Built. | Keep. | |
| RV-79 | Brined Court: a transgression drops only Giving Light standing; amends restore guest, not friend; the Champion gate reads Kindling standing as friend? | Built. | Keep. | |
| RV-80 | Saint visual pass 2 and the Velorum and Putriciel revisions (every look, number and camera)? | Built 2026-11-06. | Keep until played. | |
| RV-81 | Placement: `max_distance_from_center` 116, footprint radius 80, the 14-block entrance check, the threshold 33 down? | Built. | Keep; see SM-13. | |
| RV-82 | Annetta's Broken Church in swamp and dark forest too, and thrall or puppet kills crediting the owner? | Built 2026-11-03 (E-6). | Keep. | |
| RV-83 | The Saint tomb palettes, the pylon simplification, and Hemorath's and Velorum's ambience picks? | Built. | Keep. | |
| RV-84 | Vesper's own manipulation caster, the mount's head and neck, summoned Vespers burrowing with particles only? | Built. | Keep. | |
| RV-85 | Mycophant: she/her in player text, 2x render scale, a 4.5x5 hitbox, brood capped at 4, the Pulp Bloom screen effect? | Built. | Keep. | |

## L. Proof owed

Things only you can confirm by playing. Answer "seen, fine", or note what was wrong.

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| PR-1 | Play one session per Saint: trials solo, set pieces mid-trial, the hallowed burn, the Saint rise, ambience, feel and fight length? | Only Hemorath played; GameTests and review clients pass. | One session per Saint before new art. | |
| PR-2 | Do the Saint looks in the contact sheets hold in motion, first person, multiplayer and frame cost? | Judged from 3-tick stills. | Play first, then answer SM-5 to SM-12. | |
| PR-3 | Natural worldgen for all four mausoleums since the entrance check widened? | One natural run (Velorum). | Run natural mode for all four. | |
| PR-4 | The Apotheos D8 journey end to end (rite, spore body, heart-node planting, synapses, spread, death cache)? | Never seen by you. | One session. | |
| PR-5 | The Silentium D8 journey (Memory of Vesper, possession with each form, the wheel, the four cinematics, gaze and grab, Visitation)? | Never seen by you. | One session. | |
| PR-6 | A Brined Court fresh-world walk, the two-client strand sync and a full `alphaCheck` with `court_worldgen_validation`? | Not run since 2026-10-30. | One session. | |
| PR-7 | The 43 A-grade manipulation visuals in play, and whether strand PLANT and BRAND read per school at play distance? | Source-only grades. | Use the casting and outpouring review clients. | |
| PR-8 | Cast impacts, the body-anchored basics and the infusion veins in a played session; neither balance pass playtested? | Harness only. | One session. | |
| PR-9 | Every bar frame, opening scene and phase card (`runBossPresentationReviewClient`), the Mycophant waking and capture, the Saint rise? | Unreviewed. | Screenshot pass. | |
| PR-10 | The Crimson Troupe in a client: finale bar and cards, telegraphs, carousel and horses, perception stages, threads, models, camps, lanterns, the Cicada, fresh-chunk placement? | First footage 2026-11-07 found issues (FX-25 to FX-28). | Re-film after the fixes. | |
| PR-11 | The NPC Trade box on each side (blood tithe, Unstained refusal at D1, Court custom, Circus curiosity) and the Conversation box? | Proved for the Monolith and in the 2026-10-31 run; not the "Today" ribbon or the Scout's hub. | One session. | |
| PR-12 | The heart-node screen, spread, set-piece and possession scenes on a GPU? | Added after the 18-check run. | Launch the review client. | |
| PR-13 | Verbs and Shapings feel: the Y key's three jobs, the Staff's 20-tick tap, the faint idle reticle, long channels carrying the full outpouring? | Review client stills only. | Play. | |
| PR-14 | First conversions: the rot's crawl speed, the haul's overshoot, the constricted tails? | Stills only. | Play. | |
| PR-15 | Living blood: is five minutes a working window or a timer; the colour drain; the clot band; scraping speed; the Gauge readout? | Stills only. | Play. | |
| PR-16 | Temper: is the dry penalty felt, does casting dry from the first cast discourage a new player, the dry notice frequency, phial legibility, scar refill pace? | Stills only. | Play with LP-13 and LP-14 in mind. | |
| PR-17 | Menagerie: leash hold and upkeep, the vat economy (about 0.3 Temper per mL fed), how fast unfed vessels die, the rattle, taint speed, the tether look? | Stills only. | Play. | |
| PR-18 | Ground network: stains as clutter or answer, the pulse's price and shimmer, trace density, vein flow and range? | Stills only. | Play. | |
| PR-19 | Farming: is a drip ceiling worth building; the chum tint; basin pace; cloche speed and price; gourds needing blood as a grind? | Stills only. | Play. | |
| PR-20 | Strands: the lash's lag, tip window, tiring, crack frequency; snap; paces; mL/s against R; the HUD arc; a red lash on red floors? | Stills only. | Play with LP-10 in mind. | |
| PR-21 | Staff and mining: ring bands as rings or stripes, the pickaxe's feel and price, the axe's drop from netherite, Lode Draw's pace, Hewing's stance controls? | Stills only. | Play. | |
| PR-22 | Living Land and cling: the strike's charge and damage at 30 mL/s, the socket pool on a busy floor, the raise as a step, the cling's crawl and price? | Stills only. | Play. | |
| PR-23 | Daily loop: meal values, starving regen, the Well Rested window, how fast a region gets heavy, Blood Moon frequency, bats and drowned arrival, the lunette from across a room? | Stills only. | Play. | |
| PR-24 | Teaching: the 27 firsts and the Liber's reveal pace? | Stills only. | Play. | |
| PR-25 | Two clients: other players' strands, twins, cling, Tendon Line and fields of dark? | Never launched. | One two-client session. | |
| PR-26 | Particle sweep replacements: do the counts, sizes and speeds read bold where focal and quiet where ambient? | Starting values only. | Check per batch in its review client. | |
| PR-27 | Texels: does the T1 UV rescale keep each kitbashed model's look, and do the Silent Archon leggings visibly stretch? | Not seen. | Check per batch. | |
| PR-28 | Shader compiles on a real GPU for the new work (outpouring veins, infusion veins, engram sigil, rite floors, Saint shaders, Fane ground)? | Most proven on this RTX 4090; untested on other GPUs. | Accept NVIDIA-only proof for now (mine). | |
| PR-29 | Known test noise: two Succession opt-in failures, the False Omens and stronger-prey flakes, the two "not reproduced" round-two flakes? | Diagnostics added. | Watch the next failures' messages. | |
| PR-30 | Brined Court in-play tuning of every first-pass number inside the bands (M8)? | First pass. | Tune after PR-6. | |

## M. Will fix unless you object

Plain bugs with one obvious fix. Write "no" in the last column to stop one.

| Id | Question | Where it stands now | Recommendation | Your answer |
| --- | --- | --- | --- | --- |
| FX-1 | Check and fix a possible double regeneration tick when two Rib gourds are worn. | Unverified. | Fix if real. | |
| FX-2 | Stamp `bottled_at` on every flask creation path (craft, loot, hopper). | An unstamped flask reads fully fresh forever. | Stamp on creation. | |
| FX-3 | Route the Mycelial Lantern's blood drain through `BloodAccounting`. | Drains the tank directly. | Route it. | |
| FX-4 | Move hard-coded mob damage (Circus performers, Vampire Bat) into attributes, and drop the unused attack attribute on non-fighting NPCs. | Hard-coded in four classes. | Move and drop. | |
| FX-5 | Delete the six dead Mana and Artifice OBJ composite models. | Point at textures that no longer exist. | Move to `removed_assets/`. | |
| FX-6 | Resize three sprites to 16 px (`barbed_splinter`, `incandescent_enzyme`, `hematic_condenser_kit`). | 18, 18 and 15 px. | Resize. | |
| FX-7 | Delete the commented-out particle blocks in two armour items. | Dead comments. | Delete. | |
| FX-8 | Move the Morphling Incubator renderer's particle spawns from the per-frame render into the client tick. | Frame-rate dependent. | Move. | |
| FX-9 | Sync the Tendon Line anchor so other players see the cord. | Drawn only for the local player. | Sync and draw for trackers. | |
| FX-10 | Fix `MapCodec.encode` dropping rite ceremony data. | Open since the rite audit. | Fix. | |
| FX-11 | Rites & Crafts minors: the Qliphoth membrane ignoring floor rotation, look-alike Making/Bond and Floorless/World tints, nested recipe booleans misread through NBT, the skill-tree editor's separate tabs, the hub label "Rites". | Deferred minors. | Fix together. | |
| FX-12 | Give the two rites that share one icon their own. | Shared since the 2026-11-02 audit. | New icon. | |
| FX-13 | Check `legacyVialsAreRejectedBeforeConsumption` still reads the brazier at the right offset. | May be stale. | Check and fix. | |
| FX-14 | Diff periodic broadcasts (RT-7) and stop temporary frozen cruor becoming permanent across a restart (RT-25). | Open. | Fix both. | |
| FX-15 | Stop NPC trade card lines and hub summaries clipping mid-sentence. | Clip at two lines. | Fit or wrap. | |
| FX-16 | Heart-node minors: Creative overflow lost on a cache, an ownerless cache, `/setblock` nodes anywhere, `cacheSpot` air-only, a reset player keeping a forced Gardens respawn. | Deferred. | Fix together. | |
| FX-17 | Possession minors: the gaze draining creative and spectator possessors, Visitation not refusing during a Chamber visit, totem vanilla particles, the bog scrape's vanilla particles and axe rule. | Deferred. | Fix together. | |
| FX-18 | Chapel robustness: reload mid-trial loses timer and cooldown, Downfall wave UUIDs, Peaceful auto-completing and farming Downfall, `hallowed_burn` without `bypasses_armor`, a silent failure cooldown, griefable cysts. | Deferred. | Fix together. | |
| FX-19 | Court minors: placeholder effect icons (`stung`, `pinned`, `morphling_sealed`), the Head-tender challenge missing from the route fingerprints, school state not synced. | Open. | Fix together. | |
| FX-20 | The vessel screen's Bestiary preview is too narrow at GUI scale 3. | Text dossier shown. | Widen the panel. | |
| FX-21 | Docs drift: the economy and creature audits' doc tables (Phlegethosoma drop, Vesper "defeat", Saint move counts, Saint damage scaling, the "Saint is D8" wording, 115 manipulations, Shaping price range), the cleanup audit's section G, the Priestess lancea lore line, the wiki's Archon severing line. | Code is right; docs are stale. | One docs pass. | |
| FX-22 | Fix the CR CR LF line endings in the loom files. | Mixed endings. | Normalise. | |
| FX-23 | Clean the leaked `blood-profile-packs-*` and `blood-response-packs-*` temp folders and make the test remove them. | Thousands left. | Fix the test. | |
| FX-24 | Delete the reused 70 GB GameTest world folder between sessions. | Build output. | Delete. | |
| FX-25 | Circus footage: floor telegraphs faint on venous stone, the Strongman's subtle wind-up, knives too small and the fan reading as one throw. | Seen 2026-11-07. | Bolder telegraphs and knives. | |
| FX-26 | Circus footage: knives stick in the player as vanilla arrows; the Acrobat's vault slides and its lunge ends inside the target. | Seen 2026-11-07. | Fix. | |
| FX-27 | Circus footage: the carousel's gold or brass poles (no-copper rule), lanterns hanging in front of the carousel and reading amber, vanilla flame sprites at its base. | Seen 2026-11-07. | Hematic iron and crimson; move two lanterns. | |
| FX-28 | Circus footage: "Hold Left Shift to skip" lingers 1.5 s, Liberation's sever messages stack over the frame, the perception vignette is invisible on the red interior, puppets bunch on the owner. | Seen 2026-11-07. | Fix. | |
| FX-29 | Add review clients that do not exist yet: Unstained particles, Ferric constructs and summons, a station scene (Armature, Alembic, Suture Node, Engram), and a row for `FungalStationClientReview`. | Missing. | Add them with their batches. | |

## Sources not found or unreadable

- `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md`: read in full; no P10 or later at read time.
- `docs/brined_court/`, `docs/mausoleums/`, `docs/possession/`, `docs/heart_node/` do not exist at `docs/` root; their tracks live under `docs/archive/tracks/`. The roll-up's Q-ids there were already lifted into the topic docs read above, so the archive folders were not opened.
- `docs/superpowers/specs/2026-11-06-canon-memory-rework-design.md` (D-1's 17 questions) was not opened; its questions are one row (D-1) here.
- `tools/i18n/voice_rewrite/pass3/notes/` was not opened; the topic doc's summary of its open notes was used.
