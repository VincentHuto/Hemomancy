# Open questions and remaining work

Date: 2026-11-16. This is the one list. Every other list of open questions has been folded into it and archived
(see [Where the old lists went](#6-where-the-old-lists-went)). Topic docs carry rulings and point here.

It was rebuilt on 2026-11-14 from your 520 sheet answers and the overnight build, then cut down on 2026-11-16 after
you answered all 102 questions and build 2 built the answers. Every row left was checked against the tree, and the
counts below come from the final check's test runs.

## What is in here

| Section | Rows | What you do |
| --- | --- | --- |
| [1. Needs your answer](#1-needs-your-answer) | 8 | Write in "Your answer". "ok" accepts the recommendation. |
| [2. Will be done unless you object](#2-will-be-done-unless-you-object) | 7 | Nothing, unless you disagree with a row. |
| [3. Remaining work](#3-remaining-work) | 35 (9 ready now) | Pick what to build next. |
| [4. Play and tune](#4-play-and-tune) | 180 | Play, watch, read, tune. |
| [5. Parked](#5-parked) | 38 | Nothing. |

The plans behind the task numbers (for example "04 T5") are the chunk files in `docs/owner_answers/`. What the
overnight build did is in [owner_answers/BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md); what build
2 did, with a report at the top, is in [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md).

## State of the code on 2026-11-16

- Build 2 ran 37 packages (B01 to B37). All were verified and none was reverted. Nothing is committed: 1,822 tracked
  files are changed and 1,902 untracked files are new. Restore points are `refs/overnight/*` and
  `refs/build2/B01-before` to `B37-before`.
- JVM unit suite: 6,200 tests, none failing.
- Combined GameTest run: 2,013 tests, all required passing, in two runs in a row.
- Playthrough suite: 440 required passing, 17 optional failing (59 before build 2), none outside the baseline. Each
  of the 17 is a `NOT RUN` probe or waits on a row here.
- Opt-in suites: the clinical suite passed 132 of 132 in B21's verification; the succession suite 22 of 22 in B25's.

---

## 1. Needs your answer

8 rows. You answered all 102 rows on 2026-11-14. Every answer whose work is built, or that needed no work, is now
one line under "Rulings that bind" in its topic doc. What is left: the two rows you asked to have explained first,
the blood-on-the-ground question, the PT-85 investigation you asked for, two answers the build could not carry out
as written, and two new questions the build raised.

### 1.1 Asked to explain before you answer

**What "brand" means.** A brand was one of the five strand forms of 2026-11-11. You held the strand on one creature
for about a second (`BrandRules`, 20 ticks) and it took: the creature carried the school's state for a long time (600
ticks) and a mark naming you, and Absorption then reached it from twice as far and drew it before anything nearer. In
lore it is a claim written in your blood on another body, "this one is mine to drink". No memory brands through a
strand since 2026-11-12. The only brand left is Blood Binding's: a creature the binding holds carries the binder's
Animus brand, and the first Bound and Branded reads it.

**What "plant strand" means.** A plant was another of the five forms (`PlantRules`). You aimed at the ground and held:
blood sank into the block a pulse at a time, a warning ring grew with it (up to 8 pulses), and on release, a snap, the
cap or walking off it erupted, radius 1.5 to 4 and damage 4 to 12 by the blood sunk. Planted on a wet stain it did not
ring: it ran along the stain, one small eruption a wet cell, up to 20 cells within 8 blocks, spending the stain's
blood. That run is "the stain run". In lore it is blood already on the ground answering blood sent after it. No memory
plants since 2026-11-12, so neither the plant nor the run can be used, and Along the Red (granted only by a stain run)
cannot be earned.

| Id | Question | Options | Recommendation | Frees | Your answer |
| --- | --- | --- | --- | --- | --- |
| CQ-25, UN-4 | You want blood on the ground explored, but only once injured mobs and attacks in general also spill blood on the ground, so a player is never made to project onto the ground by hand for each ground synergy. Until a design for that exists, the jet, spray and plant kits and the stain run stay in the code (only tests reach them). The open question is that design: what spills blood on the ground, how much, how long it stays, and what reads it. | A: write the ground-blood design as its own pass, then decide what of the kits it reuses. B: remove the kits and stain run now and design ground blood from scratch later. | A. The code costs nothing while it waits. | 05 dead kits; 03 T2 (CQ-45); 16 T5 stain branch | |
| VX-18 | You approved per-school looks for planted and branded strands. No memory plants or brands through a strand now; the only brand mark left is Blood Binding's (see above). Drop that work? | A: drop it. B: spend it on the looks of the eight rebuilt held manipulations. C: keep it for whatever the ground-blood design (CQ-25) brings back. | A, or C if CQ-25 brings the plant back. | Chunk 09 batches 5 and 10 | |
| CQ-45, UN-6, NQ-03-6 | Along the Red is one of the 27 firsts, goal-framed, granted only when a plant strand runs along a wet stain (see above). It cannot be earned. What happens to it? | A: remove it (26 firsts), with its Liber opener and the Mnemonist's stain lesson. B: move it to a live act, such as a Ferric Spikes run crossing a wet stain. C: leave it dormant until the ground-blood design (CQ-25). | C. It follows CQ-25. | 03 T2 (rest), 16 T5 stain branch | |

### 1.2 Investigation you asked for

| Id | Question | Options | Recommendation | Frees | Your answer |
| --- | --- | --- | --- | --- | --- |
| NQ-10-5, PT-85 | You said an outpost NPC should no longer be carried off, since Succession makes bodies for a line. The investigation agrees: today the ledger's Summon teleports the real outpost NPC into the Fane and nothing sends it back, and a summoned original can hold a rite station. The report, with every entry point and the six probes that rely on it, is "PT-85 investigation" in [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md). | A: the pledge stays a pool bond only; Summon and every rite station take successors only; an original always walks home. B: as A, and ordinary recruitment goes too (the Bequest is the only bond). C: as A, but a pledged original can "keep vigil from the outpost" and count as a helper without moving. | A. Its cost: a solo player needs one successor (D5) before the D6 Covenant Vigil and the Archon rite, and three before Ancestral Communion. Pick C if you do not want that gate. | 10 T10 (PT-85); shrinks 10 T10 (PT-10) | |

### 1.3 Answered, but the build could not carry it out

| Id | What happened | Options | Recommendation | Frees | Your answer |
| --- | --- | --- | --- | --- | --- |
| NQ-10-1, PT-1, PT-1b | You answered A: a filled, unowned blood font at each outpost and temple. PT-1b (your own poured blood never counts toward the D1 proof) is built. The font was skipped (B21): no such block exists, so it needs a new non-clotting tank block with its own art and both structure generators run again. Until then a D1 player's reservoir is creatures or an unowned machine. | Build the font as its own package / a smaller stand-in (an unowned Alembic placed at each site) | The font, as its own package, with a review client. | 10 T5 (PT-1), the Vicar's font hint | |
| PT-30 | You set the scar blank reissue at 8 bone and 4 clay, stock 1, restock 7 days. It was built and put back (B21): the trade loader drops a card that wants what another card of the same speaker wants, and the Anchorite's Bone and Clay cards already want bone and clay. The trade also takes the scar blank off the never-sold list you kept in RV-12. | A: price the blank in other goods (for example bone meal and a Sinew Cord). B: change the Anchorite's Bone and Clay cards. C: let one want span several cards. | A. Smallest change; the rule stays. | 10 T8 (PT-30), the Vein-Mason's card | |

### 1.4 New questions from the build

| Id | Question | Options | Recommendation | Frees | Your answer |
| --- | --- | --- | --- | --- | --- |
| D-31 (faculty) | An Unstained player may run Liberation only (D-31, built in B27). Does a Circus professor also refuse to teach an Unstained pupil? Nobody has ruled it, so no line was written (B32). | A: the professors teach anyone with the pact, Unstained too. B: they refuse an Unstained pupil, with a line each. | A. The pact gate already carries the rule. | The faculty line (line work) | |
| FX-21 section G | The docs-drift audit marked eleven findings as yours to call: DC-15, DC-32, DC-34 (the Lowtide theme gate), DC-38 (the tier-3 scar gate), DC-46 (the Loom's Blood Moon discount), DC-47, DC-60, DC-70 (Chamber size), DC-85 (the MnA brainstorm instruction in `AGENTS.md`), DC-86, DC-89. Each is described in `docs/audits/2026-11-02-cleanup/docs-and-test-drift.md` and listed in the B37 log, "Skipped". | Rule each, or accept the audit's suggestion for all | Read them in one sitting. | 16 T7 (FX-21) section G rest | |

---

## 2. Will be done unless you object

None of these rows drew an objection on 2026-11-14. 7 rows are left; the rest were built in build 2 or kept the code
as it stood, and are recorded in their topic docs. These are not questions. Each is a choice a builder makes when its
task is built. If you disagree with one, write what you want in its last column and it becomes a question.

### Saints and mausoleums

| Id | What will be done | Why it needs no answer | Objection |
| --- | --- | --- | --- |
| NQ-08-26 | In the Saint sessions he judges timing, volume and whether each moment wants a sound. The stand-in files are replaced by the sound pass. | The 2026-11-13 sound audit marks every mausoleum pick a vanilla stand-in. The kept numbers stand either way. | |

### Visuals, particles and texels

| Id | What will be done | Why it needs no answer | Objection |
| --- | --- | --- | --- |
| NQ-09-4 | A build session downsamples the texel T2 textures, cleans them by hand and shows them in a review scene. The owner approves or repaints. The new Covenant Throne texture and the Mnemonic Whale upsample are drafted and flagged for his hand. | Nothing ships unseen (PR-27). He can still repaint any of the twelve. | |
| NQ-09-5 | The ten T4 models with no generator each get one in `tools/model_export/`, worst and most seen first (Living Blade, Axe and Spear hands, Choir Keeper, Cardinal Focus heart), mild ones last (Living Flail, Thurible). | Generators are how every remodel here is done. It is order of work, not a design choice. He judges each look after (PR-27). | |

### D0 to D6 journey and bloodlines

| Id | What will be done | Why it needs no answer | Objection |
| --- | --- | --- | --- |
| NQ-10-4, PT-10 | Unique Harbinger NPCs lost to `/kill` or the void come back after one day with no player within 48 blocks, as Court members do. A Creative kill stays final. | The answer cell held PT-11's text. This is the sheet's own recommendation and the Court's built rule. Survival players cannot kill these NPCs. | |
| PT-85 | Two lines may still pledge the same outpost NPC. | The PT-85 investigation finds a pledge moves nothing, so sharing does no harm under any of its options. | |
| 10 T10 | One shared vacancy store for the Court and the Harbinger sites, built once by chunk 10 T10. Chunks 14 and 15 call it. Under NQ-10-5 A it needs no `outpostHome` or "sent home" path. | Where the class lives. | |

### Dialogue, lessons and Liber

| Id | What will be done | Why it needs no answer | Objection |
| --- | --- | --- | --- |
| RV-10 (one name) | The item `barbed_splinter` is "Barbed Splinter" as an item and still "Calcified Blood Spine" in the materials atlas (`material.hemomancy.barbed_splinter`). The atlas entry is renamed to Barbed Splinter. | RV-10 kept the rename to `barbed_splinter`, so the item name is the ruled one. | |

---

## 3. Remaining work

35 rows: 9 can be built today, 26 wait on an answer, another task, a play or listening session, or a hold you
placed. Build 2 (2026-11-14 to 2026-11-16, 37 packages, [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md))
built the rest. "Ready" means nothing open touches the row.

| Area | Rows | Ready | Waiting | Plan |
| --- | --- | --- | --- | --- |
| Living blood and containers | 0 | 0 | 0 | [01-living-blood-and-containers.md](owner_answers/01-living-blood-and-containers.md) |
| Ground network and farming | 0 | 0 | 0 | [02-ground-network-and-farming.md](owner_answers/02-ground-network-and-farming.md) |
| Menagerie and the firsts | 1 | 0 | 1 | [03-menagerie-research-and-teaching.md](owner_answers/03-menagerie-research-and-teaching.md) |
| Cast economy and Temper | 0 | 0 | 0 | [04-cast-economy-and-temper.md](owner_answers/04-cast-economy-and-temper.md) |
| Verbs, strand skills and cleats | 1 | 0 | 1 | [05-verbs-sculpting-and-strands.md](owner_answers/05-verbs-sculpting-and-strands.md) |
| Lash and rebuilt held forms | 1 | 0 | 1 | the lash ledger `.superpowers/sdd/2026-11-12-lash-rework/progress.md` |
| HUD and the Bandolier | 0 | 0 | 0 | the HUD ledger `.superpowers/sdd/2026-11-12-living-hematic-hud/progress.md` |
| Creatures and spawns | 0 | 0 | 0 | [06-creatures-and-spawns.md](owner_answers/06-creatures-and-spawns.md) |
| Bosses | 1 | 1 | 0 | [07-bosses.md](owner_answers/07-bosses.md) |
| Saints and mausoleums | 5 | 0 | 5 | [08-saints-and-mausoleums.md](owner_answers/08-saints-and-mausoleums.md) |
| Visuals, particles and texels | 8 | 2 | 6 | [09-visuals-particles-and-texels.md](owner_answers/09-visuals-particles-and-texels.md) |
| D0 to D6 journey and bloodlines | 5 | 0 | 5 | [10-early-and-mid-progression.md](owner_answers/10-early-and-mid-progression.md) and [11-bloodline-and-throne.md](owner_answers/11-bloodline-and-throne.md) |
| D7 to D8 endgame | 0 | 0 | 0 | [12-endgame-paths.md](owner_answers/12-endgame-paths.md) |
| Rites and Working crafts | 1 | 0 | 1 | [13-rites-and-crafts.md](owner_answers/13-rites-and-crafts.md) |
| Brined Court | 4 | 3 | 1 | [14-brined-court.md](owner_answers/14-brined-court.md) |
| Crimson Troupe | 1 | 0 | 1 | [15-circus.md](owner_answers/15-circus.md) |
| Dialogue, lessons and Liber | 2 | 1 | 1 | [16-npc-dialogue-canon-and-liber.md](owner_answers/16-npc-dialogue-canon-and-liber.md) |
| Sound | 3 | 0 | 3 | the sound audit `docs/audits/2026-11-13-sound/README.md` |
| Architecture, tests and housekeeping | 2 | 2 | 0 | [17-architecture-combat-rules-and-housekeeping.md](owner_answers/17-architecture-combat-rules-and-housekeeping.md) |

### Menagerie and the firsts

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 03 T2 (CN-6, CQ-45) | Along the Red. If removed: delete `stain_run.json`, its grant, its Liber opener and the Mnemonist's `strands.stain` branch, and move the pin from 27 to 26. If moved: build the new grant and give it the goal frame. | S | waits on CQ-45 and CQ-25 |

### Verbs, strand skills and cleats

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 05 dead kits (UN-4, CQ-26, RV-59) | Move to `removed_assets/`: `JetRules`, `SprayRules`, `PlantRules`, `Strands.jet`, `spray` and `plant`, the stain run, the twin's four dead effects, `StrandSkills.quicken` and `QUICKEN_PER_LEVEL`, the 20/15/10 brand set ticks, the four unused first-person arm cases, and their tests. | M | held: waits on the ground-blood design (CQ-25) |

### Lash and rebuilt held forms

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| UN-41 write-back | Write the values the owner settles on back as the defaults: `lash-tuning.json` into `LashTuning.DEFAULT`, and any profile or signature number he calls out into `Sculptings`. Update reference 8.0.2, `docs/archive/tracks/core_loop/12-strand-combat.md` section 3 and the tests that pin them. | S | held: after the UN-41 sitting |

### Bosses

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 07 T4 (test) | A live GameTest for the Vesper victory experience. Today only a source assertion in `EndgameBossRewardResourceTest` pins the 180. Skipped in B15: the GameTest server loads no Chamber of Will, and `VesperOrdealManager.completeVictory` returns for an owner outside it, so it needs a Chamber-loaded launcher (as 10 T7) or a split of the guarded method. | M | ready |

### Saints and mausoleums

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 08 T10 (SM-5) | Darken Hemorath's standing pool in `hemorath_fissure.fsh` by 10 to 15 percent, pool body only. Add what the session finds. | S | held: after the owner's session with Hemorath |
| 08 T11 (SM-6) | No accepted change. Take the session's findings and check the fight from a start of 20. | S | held: after the owner's session with Seraphae |
| 08 T12 (SM-7, SM-8) | More mass on a 12-block Bloom's growths. Push the black soot tips and the back plume. Thicken the sludge at a player's eye and break up the long runnel. | M | held: after the owner's session with Putriciel |
| 08 T13 (SM-10, SM-11) | Bolder wind-ups for Lantern Sweep, Staff Toll and Draw of Silence. The gloam fog spares the Eclipse's umbral shapes. | M | held: after the owner's session with Velorum |
| 08 T14 (C-9) | Sync the forced awakening to the client and give an enraged Saint a look and a wake sound. The look is a design to write after the sessions. | M | held: after all four sessions |

### Visuals, particles and texels

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 09 T7 (VX-17, PR-7) | Capture the 43 A rows one by one with the casting harness (`tools/review/casting-animation-review.init.gradle`, a server and two clients). The held forms, lashes, second-player view, form roster and the re-grade addendum were done on 2026-11-15 (B19). | S | needs a run that may hold three Gradle runs at once |
| 09 T11 batches 2, 4, 6, 7, 12 (VX-1) | Ductilis threads, Congeatio, the six capstones, Animus mid-tier, Ferric minor. Glacial Grasp's held form is re-graded first. | L | held: after 09 T7 and the owner's A-row look (VX-17) |
| 09 T11 batch 3 (VX-1) | The four Saint canon memories. | M | held: after 09 T7, VX-17 and the Saint sessions (chunk 08) |
| 09 T11 batches 5, 9, 10 (VX-1) | Mortem, Tenebris and Flammeus members that still stand. | L | held: after the owner's A-row look (VX-17) and VX-18; the three cut members are dropped |
| 09 T11 batches 8, 11 (VX-1) | Lux and Ductilis bolts. Per NQ-09-2 D, Prismatic Reproof's R cast reuses the cut spray's prism rays and Synaptic Jolt gets a fresh design that covers its held form. | L | held: after 09 T7 and VX-17 |
| 09 T13 (VX-11) | Bring twelve textures to their layout size. Add a worn-armour and elytra review scene. Not started in B36 (the package ran long). | M to L | ready under NQ-09-4 (section 2) |
| 09 T14 (VX-12), Marionette Crossbar | Bake the renderer scale (0.5 to 0.62) into the Java mesh of `tools/circus/build_marionette_crossbar.py`. It is a batch T4 job: the puppet thread knots, `CrossbarMotionRules` and Vesper's crowned weapon layer (scale 1.7) read the same mesh. The other T3 models were done on 2026-11-15 (B20). | M | with 09 T15 |
| 09 T15 (VX-13) | Bake renderer scale into geometry for the Unstained weapons and ten models with no generator. Last in the chunk. Not started in B36. | L | ready under NQ-09-5 (section 2) |

### D0 to D6 journey and bloodlines

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 10 T7 (PT-24) | Prove `guidedChamberLogoutAndLethalDamage` and the Chamber branch of `guidedChamberStartAndReturnDoNotGrantAttunement` where the Chamber of Will loads. With `-PchamberLoadedValidation=true` the GameTest server still has no Chamber dimension, so both report NOT RUN. A launcher that loads the Chamber is needed (07 T4 needs the same). | S | needs a Chamber-loaded launcher |
| 10 T5 (PT-1) | The filled, unowned blood font at each outpost and temple (NQ-10-1 A), and the Vicar's absorption hint pointing a D1 player at it. Needs a font block (a living tank that does not clot, with its look) and both structure generators run again. PT-1b was built in B21. | M | waits on NQ-10-1 (section 1.3) |
| 10 T8 (PT-30) | The scar blank trade `npc_trade/cicatrix_anchorite/scar_blank.json`, off the never-sold list. Built and put back in B21: the trade loader drops a card that wants what another card of its speaker wants. Flip `lostBlankBeforeCarve`. | S | waits on PT-30 (section 1.3) |
| 10 T10 (PT-10) | The shared vacancy rule for unique Harbinger NPCs lost to `/kill` or the void. Flip `uniqueNpcLossIsRecoverable` and `uniqueNpcLossLate`. Smaller under NQ-10-5 A: no `outpostHome` on every resident and no "sent home" path. | L | after the cause of `courtLeadLostForGood` is found (chunk 14); size after NQ-10-5 |
| 10 T10 (PT-85) | Under NQ-10-5 A: Summon and every rite station take successors only (drop `loadRecordedOutpost` and `RECRUIT_LOOKUP_TICKET`, `eligibleNpcHelper` successors only), `returnOriginalHome` runs every time an original stands outside its loaded outpost (drop the one-shot `SuccessionHomeChecked`), rewrite `vicar.covenant.complete_vigil` and `monolith.counsel.complete_vigil`, move the six probes that summon an original onto a successor fixture, and flip `summonStripsTheOutpost` to "the Vicar stays". | M | waits on NQ-10-5 (section 1.2) |

### Rites and Working crafts

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 13 T14 (D-21, CB-15) | The cost and offering pass over rites and crafts: feeds, anchor costs, offerings, the three `riteType` values that do not match their floor tier. The starting tables are in the chunk file. | L | held: after a play session |

### Brined Court

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 14 extra (polyp models) | Polyp appendage geometry per family is still Track 1's placeholder (`MorphlingPolypLayerModel`). Not on the sheet. Not started in B36. | M | ready |
| 14 extra (first-person arms) | First-person hand rendering of arm growths (`EquippedMorphlingHandRenderer`) was never done. Not on the sheet. Not started in B36. | M | ready |
| 14 T12 (PR-6, automated rest) | Shots from `morphlingStrainReviewClient`, `brinedSeatReviewClient`, `brinedOutpostReviewClient` and `courtPresenceReviewClient` (the Bloom Warden re-shot), retaken now that the B36 strain look fixes are in, and the tooltip, Bestiary dossier and Liber text, which no Court harness captures yet. | M | ready |
| 08 T14 (C-9, Court part) | The Bloom Warden and the Kindled Champion have no sounds at all (no sound call under `common/entity/boss/court/`). | M | after the sound pass |

### Crimson Troupe

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 15 T8 (jigsaw split) | The plan asked for the pavilion as jigsaw pieces (big top, backyard, midway). B29 kept it as one template, because every test and the review client place it whole. Split it only if you still want that. The old `tools/circus` builders move to `removed_assets/` once you decide. | M | your call |
| Phantom show sound | The phantom show (2026-11-16) is silent apart from the old stage cues: a crowd murmur that grows with the audience, applause on an act's end at Attuned, and a faint band under the string lights. | M | ready |

### Dialogue, lessons and Liber

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 16 T5 (CN-5), the stain branch | The Mnemonist's `strands.stain` branch (two lines, its option, the branch id in `CoreLoopLessons`). It teaches a plant on a wet stain, which no memory holds. | S | held: waits on CQ-25, CQ-45 |
| 16 T7 (FX-21), section G rest | Code: DC-1 (`Mth.smoothstep` in three renderers), DC-58 (the Antecedent epilogue reaches a Silentium), DC-78 (staged files). Docs: DC-22 (topic-doc headings in eight docs), DC-73 (20 wiki pages not linked from Home; images embedded from `build/`), DC-81 (the impact map reads only literal paths), DC-23 and DC-25 detail counts, DC-27 (`thanks_fine`), DC-62 (Saint epithets, Voluntas Sola). The eleven findings that need a ruling are in section 1.4. | S | ready |

### Sound

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| Sound S3 | The shared sets that fill the most rows (UN-11 A): the 73 palette events, one `blood.heartbeat.deep` set for seven places, living weapon wake and rest on seven items, ten block sound types, voices for the summon puppets and the hound, vulture and spitter, and a cast file per manipulation. Funeral Bell and Velorum's toll become struck iron (UN-13 A). | L | held: after the owner listens to the candidates (UN-44) |
| Sound S4 | Priority 1, the player's own sounds, about 197 rows: held Projection and Absorption (silent today), lash and strands, living tools and items, stations, rite cues, HUD and screens, the Fungal Gardens loop, creature voices and attacks. | XL | held: after Sound S3 |
| Sound S5 | Priority 1, bosses and Saints, about 219 rows: Annetta and the Stained Priestess, Vesper, the Mycophant, Phlegethosoma, the four Saints (Putriciel non-vocal, UN-14 A), the shared kit's silent rise, the mausoleum stand-ins of M-4, and the files for the Circus ids of chunk 15 T14. | XL | held: after Sound S3 |

### Architecture, tests and housekeeping

| Task | What is left | Size | State |
| --- | --- | --- | --- |
| 17 flakes | Watch `afalteringwillstilltakestheprojection` (`SculptingValidationGameTests`). Its one failure was at x 14,556,977, past the test origin now folded into 1,000,000 blocks. If it fails again inside that range, find the real cause. It did not fail in the final check. | S | watch |
| MnA jar pin | `gradle/dependency-bundle.json` still pins `libs/mna-3.1.11-neoforge.1.21.1.0.jar` and `docs/DEPENDENCY_PROVISIONING.md` still says nine jars with Mana and Artifice among them, though the integration was removed on 2026-11-03. The preflight would fail on stale inventory (B02 log). | S | ready |

### Line work by speaker

A second view of rows above: every new or changed NPC line the tasks need, gathered by speaker, because lines
are written one speaker at a time. These rows are not extra work.

| Speaker | Lines owed | Size | State |
| --- | --- | --- | --- |
| Vicar | The D1 absorption hint that points at the blood font (PT-1), once the font exists; the `complete_vigil` line under NQ-10-5. | S | waits on 10 T5 and NQ-10-5 |
| Monolith | The `counsel.complete_vigil` line under NQ-10-5. | S | waits on NQ-10-5 |
| Mnemonist | The `strands.stain` branch, which waits on the stain run. | S | held: waits on CQ-25, CQ-45 |
| Vein-Mason | 1 trade card that replaces a lost Scar Blank (PT-30: title, line, thanks). | S | waits on PT-30 |
| Crimson Troupe faculty | A line for a professor who will not teach an Unstained pupil, only if D-31 (faculty) is answered B. | S | waits on D-31 (faculty) |

---

## 4. Play and tune

126 rows. Built things only you can judge by playing, watching, reading or hearing. This is the tweaking and
balancing list.

### Living blood and containers

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-15 | Play a session on the five-minute window, the colour drain, the clot band, scraping speed and the Gauge readout. Since B07 a top-up only blends the age and projected blood wakes clot. | now |
| B07 (clot revive) | Run `/hemo tank clot`, project into it and read the Gauge: half of each pour wakes the clot, 2 mL for each mL spent. Suture a machine, let its buffer age past five minutes and watch the owner pay about a quarter of what clots (4 mL per mL). Read the new Gauge and bar lines, the rewritten Liber page "Living Blood" and the Alchemist's lines on how long blood keeps and what keeps it fresh. Tune the three `living_blood` server values if needed. | now |
| LP-1, LP-8 | Play D1 to D3 with no free Formations and the 0.02% mob drop. Are Formations too scarce before the first Condenser? | now |
| CB-16 | A jug tincture is now cheaper per dose than a flask (667 mL against 833). Keep or tune. | now |
| LP-15 | One coal now covers two Pyretic Forge pulses, not six. Keep or tune. | now |
| FX-1 | Read the new lines: the gourd tooltip "Works only when worn in the gourd slot", the Alchemist's `gourd_use.fill` and `craft.temper.catalysts.line2`. | now |
| CB-28 | The Formation pass changed six crafts (Hematic Memory 8 Formations in all to 4; Charm of Vascularium, Morphling Jar, Sanguine Conduit, Self-Reflection Mirror 4 to 2; Vitality Chalice 3 to 2) and kept the rest. Read the old and new counts in `docs/owner_answers/01-T11-formation-price-sheet.csv`, then craft them in a D1 to D3 world and tune. | now |
| B06 (CL-6) | Drink Blood Rocks, Flasks and Jugs in a row: 100, 80, 60, 40, then 25%, back to full after three minutes without a drink. Read the action-bar line ("The vessel gave only %s%% of its blood.") and the tooltip. | now |
| B06 (brazier) | Grind a bone under Vital Effusion and gravel under Pyretic Forge; pack ice with 3 more in the off hand (30 mL), blue ice with 3 more packed ice (60 mL). Look at the JEI rows: the off-hand slot shows the count. | now |
| B06 (Lantern, rendering) | A Lantern enzyme now costs 150 mL. Rendering gives 25% (blaze powder, scrap, packed ice, spider eye) or 10% (the other six). Judge enzyme supply. | now |
| B06 (rights) | With a second player: a stranger's scab spike under your tank stays dry, a stranger cannot take your husk, lead a creature into your vat or lift your jar or vat. Log out with kin standing by a sutured machine: the Gauge says kin feed it and they pay from their own blood. | now |

### Ground network and farming

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-18 | Play the ground network (`docs/CORE_LOOP_PLAYTEST.md`, `groundNetworkReviewClient`): stains as clutter or answer, the pulse's price and shimmer, trace density, vein flow and range. Also the feeder's 15 mL, four furnaces on one coal-fed hot run, and flow under the draw rule (B09). | Now |
| PR-19 | Play farming (`farmingReviewClient`): drip ceilings, the chum tint, basin pace, cloche speed and price, gourds needing blood. Judge the 30 s feed run (built): it matches the cloche's pace over many crops for almost no blood, so the cloche may still look pointless. | Now |
| B09 (water) | Watch water run over a living vein and wash a gouge out: `groundNetworkReviewClient` shot 15 (`build/build2/B09-shots/ground-15-vein-water-60.png`), or pour a bucket on each in a world. A stream now flows across a vein instead of stopping at it, so a vein no longer dams water. Read the rewritten Artificer, Anchorite, Vicar and Mnemonist lines and the Liber pages The Ground Network, Living Veins and Basins, Second Sculptings and The Body and the Day. | Now |
| B09 (Furnace Veins) | Hold Furnace Veins on a bank of furnaces: each furnace fed now costs 6 mL a pulse, so a full bank of 8 costs 48 mL a pulse. The channel HUD still shows one furnace's price. | Now |
| PR-23 | Play the daily loop (`dailyLoopReviewClient`): meal values, starving regeneration, the Well Rested window, how fast a region gets heavy, Blood Moon frequency, bat and drowned arrival, the lunette. Since B09 kills stop counting at 600 mL, and meals are tainted meat 10, raw fish 4, cooked fish 5. | Now |
| CQ-59 | Look at stains: three splashes per age, one outline kept as it dries, alpha wet 228, tacky 200, dry 160. Built without review. Kept unless you object. | Now, with PR-18 |

### Menagerie and the firsts

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-17 | Play the menagerie (section 8 of `docs/CORE_LOOP_PLAYTEST.md`): leash hold and upkeep, the vat economy (about 0.3 Temper per mL fed on a 20-health kind, about 0.6 on a cow, on a read kind; an unread kind gives half since B10), how fast unfed vessels die, the rattle, taint speed, the tether and the brood thread. | Now |
| PR-24 | Play the firsts and the Liber's reveal pace. Brought On and Carried by the Vein now wear the goal frame. Along the Red cannot be earned yet. | Now. Best after CQ-45. |
| FX-20 | Compare `build/overnight/P05-fix1-shots/vessel-10-bestiary-met-50.png` and `vessel-11-bestiary-studied-50.png` with the `before-` shots in `build/overnight/P05-fx20-scale3/`. Say whether the 130 px list and 108 px text column read, or whether to stack the preview above the text. | Now |
| CL-12 | Read the new lines: the vessel line and the two Bestiary lines now say an unread kind "gives half its Temper", the new action-bar line "Unread blood. Only half its Temper takes hold until you examine its kind.", the Liber page Unread Humours and the Alchemist's `craft.humours.line1`. Tune the unread share (0.5, `BloodResearch.UNREAD_SHARE`) in play. | Now |
| B10 (school pages) | Read the seven rewritten school pages in the Liber (Ferric, Fervent, Frigid, Incandescent, Neurotic, Ruinous, Umbral): each held line now says what the held form does since 2026-11-12, and the six cut memories read R-only. | Now |
| B10 (vessel chrome) | Look at the new `ScreenChromeReview` shot of the Specimen Vat screen: `build/screen-chrome-review/screenshots/chrome-after-specimen_vessel.png`. | Now |

### Cast economy and Temper

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-16, NQ-04-7 | Play Temper: the dry penalty, first casts, how fast a school runs out, scar refill. Built at 0.1 x tier: at tier 3 a Mediocritas cast a second now runs dry after about 72 seconds, but a standard held form (0.25/s after the scar's cut, against 0.3 refilled) still never does. | Now: LP-13 and LP-14 are both built (B04). |
| P03-numbers | Tune the built numbers: half charge to cast (CB-6), Osseous Bloom cap 20 (CB-8), Beacon 0.8 a pulse (CB-9), Endless Hour 1,200 ticks from the hour's end (CB-10), Surge 1 to 3 mL/s, Candle 5 mL/s, Rib gourd 2.5 mL/s (CB-2), bone block 9 (CQ-16). Exsanguinate's 25 mL kill bounty still comes on top of its 60% refund. | Now |
| P03-text | Read the six changed lines and three new keys: the Exsanguinate, Grave Debt, Osseous Bloom and Endless Hour descriptions, Blood Eclipse's Absorption line, the Sanguine Surge skill text, "Discount floor", and the two refund source names. | Now |
| LP-6 | The 0.4x floor also holds under lash tap and snap prices and every held-form pulse. No lash number moved. See whether stacked discounts still feel worth collecting. | Now |
| CQ-8 | Staff rings: blood put into anything that keeps it (stations, basins, jars, vats, the Bloodwell, an unlit Brazier) grows no ring. Blood a block uses up (Mason's Effigy, Heartwood core, Ring Door, Monolith gate, Somatic Loom, Covenant Throne) still does. Check that reading. | Now |
| P03-dry-swap | Feel a Staff form swap with an empty school: 312.5 mL instead of 250, and 1 Temper burned when there is some. | Now |
| NQ-04-4 (lash) | The Blood Lash charged crack roots anything, bosses included, for up to 40 ticks (Slowness VII) and sits outside the binding limit. Play it on a boss and say if it should count. | Now (04 T6 built in B04) |
| B04 (LP-16) | Bind a boss with the held form and the R cast. The Warden takes 5 bindings, a 1,000 HP boss 10. Each hold lasts the boss length (60 ticks plain) and drops; a slip says "The binding slips off ..." and the held form waits 3 seconds. When the count is spent the line reads "... will not be bound again." Judge the fail chance (half the time at full health on a 1,000 HP boss). | Now |
| B04 (prices) | Feel the new prices: Avatar stages 10 to 45 mL/s, Iron Choir 50, Absolute Stillness 70, Carrion Communion 50, Radiant Wake and Living Circuit 30, Lignum 30/40/50, Furnace Veins 40, Thread Ripper 300 mL, Sovereign Instinct 200 mL, Ironhearted 600 mL, Void Shroud 300 mL. Base regeneration is 2 mL/s now; an old dev world keeps 1.0 until its server config is edited or deleted. | Now |
| B04 (Temper) | Feel the Temper burn bands: a cheap held form (up to 10 mL/s) burns 0.5 a second, a 60 mL/s one 2. R casts burn 2 to 8 by rank. With Sated Siphon and a worn gourd, draw a creature at full blood and watch the gourd fill. | Now |
| B04 (debts) | Dispel a conjured cell hand after a discounted conjure: 90% of what you paid comes back. | Now |
| B04 (mining, mending, moon) | Vein-mine a lode with Talaromyces: 12 mL per further ore, stopping when blood runs out. Hew deepslate with a full face (54 mL) and stone (36 mL floor). Mend a wild golem, your own Drudge and an iron pillar; an enemy's Drudge refuses. In the Nether under a Blood Moon, casts lose the 0.75x discount. | Now |
| B04 (text) | Read the new and changed lines: the Crimson Tithe description and its three debt lines, the Sated Siphon skill text, the Talaromyces Minus tooltip, the two boss binding lines, the Talaromyces Vein source name. | Now |
| B05 (damage prices) | Feel the twelve cut R casts, priced so each rank buys 0.020 to 0.049 HP per mL (a bolt on one body, an area cast on three): Insatiable Hunger 100, Cruor Javelin 160, Searing Lash 160, Blood Aneurysm 260, Bloom of Rot 290, Fruiting Contagion 290, Sanguine Magnetism 385, Rimebound Sentence 310, White Verdict 735, Funeral Bell 430, Lodestone Collapse 610, Synaptic Storm 490. Funeral Bell is read without its affliction bonus and Blood Aneurysm without its splash; say if either should count. The sheet is `docs/owner_answers/04-T5c-price-sheet.csv`. | Now |

### Verbs, strand skills and cleats

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-13, CQ-57 | Play the verbs: the Y key's three jobs, the Staff tap, the reticle, long channels. See the refused-claim mark (ticks pulled out, barred in pale bone, "Refused" for the price). | now |
| PR-14, CQ-58 | Play the first conversions. Judge Pyretic Forge with no outpouring: a heat tendril and a small forge bed stand in. | now |
| PR-20 | Play the three lashes and the eight rebuilt held manipulations. Judge whether the eight should keep playing the cast outpouring (the lashes play none). The lash numbers are tuned in `lashTuningClient`. | now |
| CL-21 | Decide whether the converted memories keep their R cast. Ruled: they keep it through the first play pass. Six memories now have only their R cast. | PR-20 played |
| PR-21, CQ-61 | Play the Staff and mining. Look at the planted Staff rings and the stalled-machine pip with its hover reason; since B10 the pip is 7 by 7 with a pulsing gold halo (UN-55). | now |
| PR-22 | Play Living Land and the cling. Cleats halve the upkeep (wall 2 mL/s, ceiling 3). Read the changed tooltip, the Artificer's two lines and the Liber cling page. | now |
| B11 (CN-10) | Fit Hematic Cleats at an Armature: craft them from 2 scrap, 4 bone and a Sinew Cord, put them in a bowl, stand in the rack in hematic iron boots (or any later blood-side pair) with 250 mL in the tank. Cling with them on, then turn them off from the new slice on the radial's inner ring and feel the full price and slow crawl. Upgrade the boots and see the cleats stay. Look at the spurs over each boot model (`build/build2/B11-shots/`). Read the new tooltips, the Armature messages, the JEI "Cleats Fitting" rows, the Artificer's four cleats lines and the Liber cling page. | now |
| PR-25 | One two-client session: the lashes and the relayed whip aim, the twin, the cling, the Tendon Line cord as another player sees it, fields of dark, the dome, beam, volley and javelin. Watch whether Long Strand's length lands off for the watcher. | now |
| B01 (CL-3) | Read the Sculpting rename where a player meets it: a memory tooltip ("Sculpting, Projection: ..."), the channel readout, the "Sculpted" advancement toast, the Mnemonist's Sculpting lesson and the Liber pages "Sculptings of the Ground", "Mining Sculptings" and the second Sculptings. | now |
| B10 (CQ-20 B) | Feel the three strand skills on the eight held manipulations: Long Strand 3 reaches them 30% further, Whipcrack 3 makes the click hit 45% harder with its state 60% longer, Free Hand 3 gives back 0.3 pace (never past walking). Read the five rewritten skill texts and the Liber pages "Lashes and Held Forms" and "More Held Forms". | now |
| B10 (UN-54) | The Will's attached forge read too small at 0.7 (sunk in the body: `build/build2/B10-visual-run2/visual-16-forges-8.png`) and is back to its old full size (`build/visual-fixes-client-review/screenshots/visual-16-forges-8.png`). The Brazier bowl forge stays at 0.8, but it floats about three blocks above the bowl in both shots; say if that height is wrong. | now |
| B10 (NQ-05-13) | Freeze a pond with Rimebound Sentence or Glacial Rampart's R cast: the Frozen Cruor now melts back to water, as a Will's, Vesper's and a Drudge's does. | now |
| B10 (CQ-13, CQ-14) | Read the core-loop lines pasted into `AGENTS.md` (the lash bullet and the held-manipulation bullet first). The track files are in `docs/archive/tracks/core_loop/`; the play script is `docs/CORE_LOOP_PLAYTEST.md` and the ledger `docs/CORE_LOOP_TEACHING_LEDGER.md`. | now |

### Lash and rebuilt held forms

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| UN-41, UN-23 | Tune the lash by hand in `lashTuningClient`. You have never opened it. It stages a floor, husks and the three lashes on any single-player world; edit `lash-tuning.json` in the game directory and it is read within a second. The 20 first values: flick 6 degrees a tick, rest 1.5, fade after 6 still ticks over 30, regrow 4, throw 4 ticks across 60 degrees, damping 0.92, aim pull 0.2, node speed cap 4.0, slack 0.35, full 1.0, crack 0.9, camera cap 4.0 with drag 0.6, whip held within 85 degrees of the view and 60 a tick, release at half charge, lead 70 degrees kept at 0.88 a tick. Four differ from the plan after a rope simulation (pull, slack, full, crack). Judge by the spec's tests: a wrist flick cracks at full strength with the view readable; it looks like a whip; tap-only play is a weapon; the fade reads; the three lashes feel different. Swing fast up and down too: the whip's return to the crosshair (`leadKeep`; near 0.99 turns it off) was the fix for your video and has not been seen with vertical swings. | Nothing. Ready now. |
| UN-24 | Play the three lashes and judge their numbers. They are in `Sculptings`, not the tuning file: say the number and it is changed in code. Blood Lash: 15 mL/s, tap 4.5 for 8 mL every 12 ticks, 20 ticks to a 1.4 times crack that holds its target up to 40 ticks (Slowness VII), a landed sweep returns 4 mL once a tick. Searing Lash: 60 mL/s, tap 7 for 24 mL every 20 ticks with 80 ticks of Searing, 40 ticks to a 1.75 times crack, radius up to double, fire to 80 ticks, each crack in a row adds 15% to four. Gloam Laceration: 40 mL/s, tap 4.5 in three cuts for 12 mL every 6 ticks, three tails at two fifths each 2 ticks apart, 30 ticks to a 1.25 times crack, pierce from 60% charge, the third tap inside 25 ticks is a 1.5 times crack that lays Obscured, others see it at 40%. Builder's calls inside these: only the third Gloam tap lays a state; Searing's throw is not slower, only its pull is lower. Prices are also the conflict row LP-10. | In the UN-41 sitting |
| UN-27 | Small lash calls to notice while playing: no charging pose (the default Projection pose holds the orb); the orb was enlarged about 1.4 times; the fade runs at a fixed rate, so a revived short lash fades a little quicker; a left click on the orb or a live whip cracks at the first body on the look line; a click on a block still mines and never taps; a refused flick sends no message. Watch for a phantom whip (up to 15 ticks) in a case the builder did not find. | In the UN-41 sitting |
| UN-28 | See and play the four step-2 rebuilds; you approved the proposals, the numbers and these calls are the builder's. Blood Eclipse: a carried dome, 2 to 8 blocks over 60 ticks, 60 mL/s, drawn as ribs of dark. Look at the ribs first: you have rejected "spaghetti" twice. Glacial Grasp: an ice hand that holds one creature, 40 mL/s, squeeze 4 to 14. Cruor Javelin: three draw stages, damage 4 / 7 / 10, 50 mL/s; it breaks on a body and sticks only in blocks, so only a block-stuck javelin can be recalled. Ferric Spikes: a run steered by the look's ground point (it looks through creatures), 50 mL/s, erupts as three blades. Who the dome hides is NQ-04-12. | UN-41. The UN-42 shots in `strandReviewClient` show them first. |
| UN-29 | See and play the four step-3 rebuilds; you said "built as described", the numbers and these calls are the builder's. Hematic Flare: a searchlight beam that passes through bodies and does not wound; heat builds and only the flash hurts (2 to 8), 40 mL/s. Synaptic Jolt: keeps a conductor live or jolts a creature for 2 a pulse, 40 mL/s. Blood Needle Lance: up to six hovering needles, drawn as a crown ahead of the face, 35 mL/s. Crimson Flame Conjuration: painted fire, at most 24 flames, flare 5 damage, 35 mL/s. | UN-41. The UN-42 shots in `strandReviewClient` show them first. |
| UN-42 | Cases no client has shown, one look each: vertical whip swings after the lead fix, javelin recall by Absorption, the ice hand on a moving mob, the flare flash on a moving mob, flame paint refused by a claim, Searing's crack growing with heat, Gloam's pierce accent, three tails in a fast first-person swing (they read small on the review sheet). | Now: `runStrandReviewClient -PstrandOnly=lash-vertical,javelin-recall,lash-tap-gloam-ambush,gloam-charged,searing-heat,flame-paint-claimed,ice-hand-moving,flare-flash-moving` (B12 staged them; shots in `build/build2/B12-shots/strand/`), or by hand in the UN-41 sitting |
| UN-43 | Two-client look. Nothing has shown the new lash or the eight rebuilt manipulations to a second client; the onlooker frames of 2026-11-11 predate them. Re-run `tools/review/review-onlooker.ps1`, then play one real two-client session. From outside, check: the whip matching what the caster sees, the faint Gloam lash, the dome's fog, the beam, the volley's crown, the javelin in flight. | UN-41. The harness is ready since B12: re-run `tools/review/review-onlooker.ps1` |

### HUD and the Bandolier

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| UN-33 | Look at the two HUD changes made after your approval: the Morphling jar as vivianite glass (green back glass, 11 round pores, two beaded with blood, a crimson stopper in an iron collar) and the polished vials (shaded cap, paper label, a front-glass layer drawn twice on a full vial). `runTemperRackReviewClient`. The whole-pixel HUD scale in the same row is ruled. | Any time |
| UN-46 | In a real session, hover each Temper vial and read its tooltip. Two review runs aborted on the hover check and a later run passed all 44 scenes and 3 hovers. Press F3+T once with the HUD up and see the binding redraw: no test covers the cache after a resource reload. While there, watch the heartbeat quicken at low blood and the gold binding at Apotheos. | Now: the Alchemist gives the Bandolier with her D2 Temper lesson (CQ-63, built 2026-11-15) |
| UN-45 | Your own planned texture edits for the vial seats. The frames `base_degree_0` to `8` are now written by `tools/gui/vessel_frame_seats.py` from pristine copies in `tools/gui/vessel_frame_source/`. Edit the pristine copies and re-run the script, not the outputs. `exportHudLayouts -Pforce` overwrites hand-edited layouts in `textures/gui/blood_hud/layouts/`. | Any time |
| UN-34 | Look at the Sanguine Vigil in a client, dormant and with blood stored: stone plinth, iron shaft with bone vertebrae and sinew stays, catalyst dish, iron yoke with an eye that opens when lit, light 6. Keep or redraw. Say if the dish should show the stored catalyst and the gauge a fill level. It is on or off today. | Now: `runMenagerieReviewClient` shots `vigils`, `vigil-dormant-close`, `vigil-lit-close` (B12) |
| UN-35 | Play joined vats: set four vats in a square, stack to 2x2x2, lead a horse in, break one vat. The rules you will meet, built without you: only squares join (three vats stay apart), a vessel holding a creature never grows, breaking any vat frees the creature, a joined vessel cannot be lifted with its creature, each vat adds 800 mL of buffer. Say if any should change. | Now: `runMenagerieReviewClient` shots `joined-vats`, `vat-horse-close`, `vat-cube-close` (B12) |
| B12 (CQ-63, UN-8, UN-56, UN-62, UN-64) | Read the new Alchemist line `hemomancy.dialogue.event.alchemist_bandolier_given` and the changed `hemomancy.alchemist.craft.temper.catalysts.line2`; craft a Bandolier (two leather, a Sinew Cord, three glass bottles); feel Gloam without the Obscured ambush; check a tap on a boat, an item frame or with a sword in the main hand now hits as vanilla; check the lash lets go the moment you release; see the Morphling jar in LEGACY_ICON mode behind its glass. | Any time |

### Creatures and spawns

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| CR-3 | Hunt Blood-Drunk Puppeteers at night in dark forest, swamp and old-growth taiga. Judge whether weight 24 still makes them findable now that they need darkness. | now |
| CR-1 | Walk the Overworld Fungal Gardens. Judge Abhorrent Thought and Fargone packs of 1 to 4. | now |
| CR-8, CR-9 | Fight a Venom-Rib Centipede at 26 HP. Check lush and dripstone caves for bats against Hemolymphopoda at weight 10. | now |
| RV-27 | Walk the wayside sites, the Harbinger sites, the Brined Votary and the heart node. They stay as built until you have. | now |
| VX-16 (B36) | Look at the new Hemojelly and Myelin Borer, then the 15 new effect icons (every effect now has its own). Sheets: `build/build2/B36-review/hemojelly-sheet.png`, `myelin-borer-sheet.png`, `effect-icons-8x.png`, `effect-icons-sheet.png`; full shots in `build/hemojelly-review/screenshots/`, `build/myelin-borer-review/screenshots/`, `build/effect-icon-review/screenshots/`. Repaint any you dislike in its generator. | now |
| CR-7 (B14) | Pour 500 mL into a broad pool of Basin ichor (hold Blood Projection about 14 seconds), watch it rise and fight it. Then call one and walk off: it should dive after a minute and leave nothing. Read the Alchemist's new D5 commission line `hemomancy.alchemist.phlegethontic_commission.phlegethosoma` and the Liber page The Phlegethosoma (Cosmic Forces). Look at the pour and dive visuals (the chum bloom and the fluke's ripple ring); no screenshot was taken. | now |
| NQ-06-3, NQ-06-4, NQ-06-5, NQ-06-6, NQ-06-12, NQ-06-13 | Tune the creature tables in play: Gardens roster, Lantern Tick, template mobs, sent Wills, tempers, drops. The values are in `docs/HEMOMANCY_REFERENCE.md` 26.7. | now (B13) |
| B13 (NQ-06-1, NQ-06-12) | Walk the Overworld Fungal Gardens and mine below it: it now has vanilla ores and the Overworld-wide spawns such as the Polyp. Draw a polar bear with an ordinary Blood Vial: I read "so do polar bears" as "a clean Congeatio draw to an ordinary vial", so the bear no longer needs a Living Syringe. Say if you meant only the Temper. | now |

### Bosses

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-9 | Look through the stills: every bar frame, opening and phase card, the Mycophant waking and capture, the Saint rise. They are in `build/build2/B15-shots/` with `index.txt` (53 presentation stills, 303 Mycophant stills). | now (B15) |
| V-6b, CR-15, CR-19 | Watch in the stills or a client: green and amber spores at the capture, the Mycophant notch at 60%, the Crowned Refusal notches at 70/40/10 (`boss-bars_mycophant-40.png`, `boss-bars_vesper-40.png`). | now (B15) |
| CR-15 | Play the Mycophant end to end. The last recorded play predates the 2026-10-24 moveset, and phase 2 is now the longer half. | now |
| CR-19, CR-28 | Play the Crowned Refusal once. The grounded burrow should first show just after the first wound opens. | now |
| CR-20, CR-22 | Tune in play: Kindled Champion regrowth 2 / 3 / 3, Vesper experience 180, Saint experience 80 and 120. | now |
| RV-18, RV-84, RV-85 | Watch three things never verified by reading: bars hidden under every opening, summoned Vespers burrowing with particles only, the Pulp Bloom screen effect. | now |
| NQ-07-2 | In the Seraphae and Velorum fights, judge whether adds that shrug off Binding and Paralysis feel right. | a Saint play session |
| NQ-07-1 | Fight Annetta on a timer. She has 51 health now, so her bar empties after about 50 damage and she then stands at 1 HP until two minutes have passed. Raise the number if that wait reads as a stuck boss. | now (B15) |
| NQ-07-4 (PT-84) | Lose a Mycophant rematch, then stay in the Gardens for the five minute cooldown: the claim should start again by itself, as a rematch with the short opening. Lose again and it comes back again. | now (B15) |
| D-33 (boss tag) | Fight the Ringmaster with Binding, Paralysis, the leash, the jar and Hematic Command: he now takes the boss rules for all of them, and the Scarlet Mummer can no longer pull his attention. | now (B15) |

### Saints and mausoleums

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-1 Hemorath | Play his complex and fight: the three Chapels solo (the Beholding now tells its memory), set pieces mid-trial, the hallowed burn as an Apotheos, the rise, the fight's feel and length. Judge the looks in motion and first person (PR-2), the heartbeat and drips (M-4), and the raised descent vault. | now (played once on 2026-11-06, before the trench pass) |
| PR-1 Seraphae | The same session for Seraphae. Her integrity now starts at 20 (CR-17): a solo player who finds no opening meets the Radiance Flood in about 100 seconds. Hear the choir (M-4). | now |
| PR-1 Putriciel | The same session for Putriciel. The Kindling creeps every 12, 10 and 8 ticks (CR-18). Judge the lowered flesh mottle, the soot and the sludge. Hear the creak (M-4). | now |
| PR-1 Velorum | The same session for Velorum. The seam lips are dark now (SM-12). Judge the three quiet wind-ups and the Eclipse through the gloam. Hear the ice creak (M-4). | now |
| SM-12 | Compare `build/overnight/P07-velorum-pulse-before-after.png`, `P07-velorum-ring-zoom.png` and `P07-velorum-ring-frames.png`. The white lip is gone but the hoarfrost needles keep a pale fringe. Say if the needles should go too. | now |
| SM-8 | Judge the mottle value (109, 108, 77), the builder's pick, at portrait distance: `build/overnight/P07-putriciel-portrait-after.png` against `P07-putriciel-atlas-before.png`. | now |
| SM-3, NQ-08-24 | Read the fifteen Beholding lines and "The memory has not settled yet." (`message.hemomancy.chapel.beholding.hemorath.*`, `.seraphae.*`, `.velorum.*`, `message.hemomancy.chapel.cooldown`). They show in chat, one per flash. Velorum's five are new (B16). | now |
| PR-3 | Look at the three natural sheets in `build/overnight/P07-natural/` (`20261028-sheet.png`, `20261114-sheet.png`, `1-sheet.png`). Trees crowd the door in forest and taiga, and another building stands beside Hemorath's pavilion on seed 20261028. Say if either should change. | now |
| NQ-08-23, NQ-08-22 | In Seraphae's Downfall, fight her light fragments: they follow you, strike for 3 every 20 ticks they touch, and end at one blow. Say if they are too easy or too hard, and look at them in motion (nobody has seen them in a client yet). Try a Downfall in Peaceful: the wave now stays. | now (B16) |
| NQ-08-18, NQ-08-19 | Wake an unopened tomb with an Apotheos friend standing in the chamber: the Saint rises enraged. Draw a peaceful syringe, let the Saint rise and return, and try the vial again: the tomb refuses. | now (B16) |
| D-1 (B18) | Play Bloom of Rot and the Endless Hour at D8. Bloom: kneel and sow, watch bodies colonise (you too), light it with the second press once kindled, or leave it unlit and be reclaimed. Hour: plant, let still bodies set in cruor, try the dark rim, step off, take a heavy wound for the Bow, and sit through the close and your own set. Tune the 450 and 600 costs, the 15 a second upkeep and the 600 and 900 cooldowns. Read the eleven new narration lines (B18 log). Fight each Saint with its own gift: the Tithe feeds Hemorath, the Eye holds Seraphae, the Bloom feeds Putriciel and fails in their interval, the Hour stills you faster near Velorum. Look at the stills in `build/build2/B18-review/`. | now (B18) |
| D-1 (B17) | Play Crimson Tithe and the Unclosing Eye at D8 (end a Saint's trial first, or set the record with a command fixture). Judge the chain drink, Brimming, the overfill burst and the lanes, then the beam's sweep, growth, Burst, the ally heal and the Lapse; tune costs and cooldowns. Read the tooltip and narration lines. Look at the review stills in `build/build2/B17-review-round2/`. | now (B17) |

### Visuals, particles and texels

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-26 | Judge the new particles. Every count, size and speed is the audit's starting value. Stations: `build/harbinger-station-review/screenshots/`, `build/fungal-station-client-review/screenshots/`. Ferric: `build/visceral-review-client/screenshots/ferric-p16v1-*.png`. Unstained: `build/overnight/P17-verify1-shots/` and `build/unstained-particle-review/screenshots/`. Summons and ecology: `build/overnight/P18-verify1-shots/`, `build/summon-particle-review/screenshots/`, `build/ecology-particle-review/screenshots/`. Known: the Unstained rite bursts read thin, the Cicada flash and Silver Dawn glow do not show by day, the Incubator craft sends 2 spore puffs a tick, fine motes barely show in a still. Batches B and E: `build/sweep-particle-review/screenshots/`. B19 added the unowned iron constructs, orb, vial and polyp (`build/summon-particle-review/screenshots/`) and the jelly, snail and cuttle (`build/ecology-particle-review/screenshots/`), shaped the Echo, Bloodburst, mutation and Sickle bursts and lowered the Thrall and Drudge bursts. Not staged anywhere: the Circus overlay, Curved Horn, Thurible coal, Elytra effect, Silent Archon negation and the Sickle cut. | nothing; best in motion |
| A-4, VX-4, VX-5 (B19) | Run `/tick freeze` and see the Monolith Fragment, Qliphoth Seed and Pome, Scar Pattern, Specimen Jar and the Silent Archon set stop, in hand and in a slot. Cast an Animus mortar, cloud, impact, Sovereign Lash and conjure, and Umbra Well, Black Veil and Gloam's quick cut: their glowing cores and shadow wisps use new shader shapes (`build/build2/B19-review/vx4-shapes-sheet.png` is far off and only shows they draw). Cast one thing twice and see the shared-tier variant change. | now (B19) |
| VX-17, PR-7 | Watch the A-row pack and say which rows hold at the Riposte bar: the lashes and eight held forms in `build/strand-client-review/screenshots/` (one still each on `build/build2/B19-review/strand-held-forms-sheet.png`), the same from a second player in `build/onlooker-observer/screenshots/` (`build/build2/B19-review/onlooker-sheet.png`), every form on a stand in `build/visceral-review-client/screenshots/visceral-roster-*`. Read the re-grade in `docs/audits/2026-11-11-visual-balance/2026-11-15-a-row-review-and-regrade.md`. Blood Eclipse's field of dark did not show from outside. | now (B19) |
| PR-8 | Play one session with cast impacts, the body-anchored basics and infusion veins. Harness captures are the only evidence so far. | nothing |
| PR-27 | Judge each texel batch as it lands: the T1 models after the UV rescale, the repainted textures and the Silent Archon leggings, the regenerated and rebuilt models. | 09 T12, T13, T14, T15 |
| VX-10, VX-12 (B20) | Judge the batch T1 and T3 looks. One sheet per row: `build/build2/B20-review/texel-sheet-1-items.png` (held weapons, instruments, sarcophagus and pylon items), `texel-sheet-2-staffs.png` (staff heads and the seven fittings), `texel-sheet-3-blocks.png` (podiums, crystals, scyphus, effigies, centrifuge); full size in `build/texel-review/screenshots/`. Measurements before and after: `build/build2/B20-density-before.txt`, `B20-density-after.txt`. Old fitting textures are in `removed_assets/2026-11-15-texel-t3-old-textures/`. | now (B20) |
| FX-6, V-3b | A glance each: the three resized sprites (before in `build/overnight/P16/png-before/`; the Incandescent Enzyme's tips were repainted) and Ferric rust on 127 tiles (`build/visceral-review-client/screenshots/ferric-p16v1-*.png`). | nothing |

### D0 to D6 journey and bloodlines

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PT-12 | Look at the lit campfire under the outpost Alembic in a client. Its smoke may show through the still. | now |
| PT-6, PT-7, PT-39, D-23, PT-86 | Read the P10 lines: the Vicar's D3 and D4 hints, the Mnemonist's archive line, the Woven Vessel ledger card, the Quintessence rumour line, the Cornerstone warning, the seven covenant card titles. | now |
| PT-43 | At D7 and D8 the Vicar asks an Archon or Apotheos "Have you heard of any path beyond the seven degrees?". Judge whether that wording fits them. | now |
| PT-3, PT-16 | Play a full pack at the Vicar's kit and the starter kit. Rewards stay owed, chat says to make room, they arrive when there is room. Read the make-room line and the eleven new narration lines. | now |
| PT-17 | Log out for real while a fed Venous Stone is forming and see that it stands until you return. The probe's player is never removed, so its pass does not prove it. | now |
| PT-38 | Read the Unbinding warning said at activation, and plant the Staff twice to feel the two steps. | now |
| PT-42 | See the pool screen's Donate button grey out on a full pool. Shot: `build/build2/B22-review/chrome-b22-bloodline_pool_full.png` (the review player's blood is inactive there, which greys it too, so play it once). | now |
| PT-9 | Play the founder-led reception from the recruit's side and judge the kneel to accept. | now |
| PT-5 | Look at the deep D5 ring of the Rites and Crafts map with the Monolith on it. | now |
| B21 (D0 to D6) | Read the new and changed lines: the Vicar's absorption line and hint, his Masons Respite reissue, the scrap refusal and `complete_vigil`; the Hermit's taken-heart and stranger lines; the Alchemist's clear-spin option and line and the commission lines; the Anchorite's two strain lines; the founder offer lines; the two refusal lines and the charm line. Play D2 on a server with a second player awake and lie down for five seconds. Play a full pack at the late rewards and see them stay owed. | now |
| PT-37 | Watch a kicked player's ledger crumble: the line, the sound, the ash motes. Built in B22; no client shot. | now |
| PT-35, PT-36 | Look at the ledger screen: the leader's petition row and a member's Leave button (two clicks). Shot: `build/build2/B22-review/chrome-b22-ledger_petitions.png`. Read the B22 lines (`owner_answers/BUILD_LOG_2.md`, B22 "For the owner"). | now |

### D7 to D8 endgame

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-4 | Play the Apotheos D8 journey: the rite, the spore body, planting the heart-node, synapses and pruning, the spread, a death cache, Molting, the dormant Podium, Visitation. The kept numbers (RV-1, RV-2, RV-76, D-28) are judged here. | Now. |
| PR-5 | Play the Silentium D8 journey: the ninth-pome lock through both Vesper phases (PT-72), the Memory of Vesper, possession with each form, the wheel, the four scenes, gaze and grab, the bat hanging under a ceiling. The kept numbers (RV-3, RV-4) are judged here. | Now. |
| PR-12 | Launch `heartNodeReviewClient` and `possessionReviewClient` on a GPU: the screen (Strangers off by default), spread, set pieces and possession scenes. Also the P13 things no client has shown: the pome tooltip, the Podium line, pruned synapses withering. | Now |
| P13 lines | Read the 13 new or changed lines of P13 (build log, "For the owner"). Two are flagged: the stranger's counsel `monolith.counsel.not_your_stone`, and "your own spent Bloom" in the Vicar's Severance line. | Now |
| P13 colours | Tune the totem burst gold (232, 196, 92) and the bog body wax grey (214, 204, 168). | PR-12 |
| PT-83 lines | Read the Silentium greeting trees, one speaker at a time: the Vicar, Alchemist, Mnemonist and Artificer, and the Hermit's one line (UN-19). The English is in `owner_answers/BUILD_LOG_2.md`, B24 "For the owner". | Now |
| B23 (D7 to D8) | Play or watch, in `heartNodeReviewClient` or a world: the Leave the Gardens button on the heart-node screen (shot: `build/build2/B23-review/chrome-b23-heart_node.png`) and the walk home to your bed, then with the bed broken to the world spawn; an Apotheos reset by the cure losing the node; the Sporitic Thurible refusing a D4 and a Silentium; the atlas veiling the Monolith items at D6 and the Residua at D7; Mind Broken still on after a death; 8 fragments from a shatter. Read the new lines (B23 "For the owner"). | Now |
| archonRiteWithHelper | Watch the playthrough runs: the Vicar now arrives after the braziers are built. If the rite still stalls at INSCRIPTION once, the cause is elsewhere. | Each playthrough run |

### Rites and Working crafts

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| D-21, CB-15 | Play the rites and crafts, then judge feeds, anchor costs and offerings in one sitting. Start with the Covenant Throne (8000 mL), the Sanguine Vigil craft (6500 mL) and the Mycelial Lantern (now 750 mL). | Now. T14 follows it. |
| PT-88 | Cast a grand rite alone (Apotheos or the Bloom) and judge the ordeal's weight with fill-before-repair. The solo playthrough probe now finishes the Rite of Apotheos with no rescue. | Now |
| NQ-13-3 | While a rite runs, watch whether a Sculpting or a ground mark (ash trail, vein node, gouge) breaks its floor. If one does, the claim widens. | Any rite in play |
| FX-11 (motif) | Look at the turned Qliphoth floor motif: `build/rite-floor-client-review/screenshots/rite-floor-5.png` (south) and `rite-floor-7.png` (east). On a south floor the husk swirls now turn the other way and the halo tint shifted. | Now |
| FX-11 (map) | Look at the "Rites & Crafts" hub label and the new group tints: Floorless crafts in bone and Making in hematic iron (`build/build2/B25-shots/liber-rites-and-crafts.png`). | Now |
| FX-12 | Look at the map icons of the Bloodline Covenant (the Enthralling Filament), Hungering Earth (the Sanguine Formation) and Possession (Adipocere). Say if any should be drawn art. | Now |
| PT-67 | Read the reworded refusal `message.hemomancy.chamber_visit.rite_unavailable`: "The Chamber does not answer, so no visit can begin. The rite will not take hold." The visit itself is proven in `chamberRiteReviewClient` (`build/build2/B25-shots/chamber-rite-visit.png`). | Now |
| DS-4 | Read the 19 Liber lines and the new page "Blood Gourds" for lore and voice (the list is in the B25 section of `docs/owner_answers/BUILD_LOG_2.md`). The Possession and Visitation lines touch the 2026-10-31 rulings. | Now |

### Brined Court

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-6 | One session. Walk a fresh world: a Seat, the outposts, residents, the Bloom Warden on a wrong, the Champion challenge, the three discovery routes. Two clients: Rope strands, Court NPC poses, the school. Look for a Tide Refuge, a Bell and a Wake nursery: the seed runs found few. | Nothing. The automated half is green. |
| PR-30 | Tune the first-pass strain numbers inside the bands. Includes the swim total of x1.82 for a Primal Purse at home inside the full Prismatic set (x1.62 at Apex away). | PR-6 |
| P14 lines | Read the eight new or changed lines: the switch warning, the Head-tender's `challenge.line2`, `refused_wait` and `refused_degree`, the two system lines (`champion.refused_degree`, `champion.forfeit`), the Voyager's "Could you draw me a Relay Chart to the Court?" and the Braced elder's Holdfast line. Say if each lead should speak the switch warning. | Nothing |
| PT-46 | A dropped connection, and most likely a server stop mid-fight, costs the challenger the day, however far they are from the Champion (B26 closed the 64-block gap). Say if that is too harsh. | Nothing |
| T7 lines | Read the Court line pass: 26 directions lines that now name each house's site, the 17 lesson lines that say "wear" instead of "feed", the reworded `head_tender.challenge.line1`, and 17 new keys (three `degree.late`, nine `strain.primal`, the Primal option and four `primal.line1/2`). The list is in the B26 section of `docs/owner_answers/BUILD_LOG_2.md`. | Now |
| HN-4 (B36) | Judge the three strain look fixes: `build/build2/B36-review/strains-before-after.png` (top row before, bottom after: Ink Cap at Primal, Holdfast and Sunburst at Mature) and `rope-before-after.png` (the ink thread under the rope strands). Full shots in `build/morphling-strain-review/screenshots-B36-before/` and `screenshots/`. | now |
| FX-19 icons (B36) | Judge `stung`, `pinned` and `morphling_sealed` in the effect row: `build/effect-icon-review/screenshots/effects-4-*.png` and `effects-5-*.png`, and the 8x sheet `build/build2/B36-review/effect-icons-8x.png`. | now |
| FX-19 flush | Judge the school's sail flush (a second emissive pass over the sail while `SHARED_WAKE` is on): `build/court-npc-review/screenshots/school/`, the close shots `08-close_day.png`, `09-close_night.png` and `10-close_night.png`, against a lone swimmer's `role_edge_swimmer/04-night_glow.png`. | Now |
| C-9 (Court) | Hear the Bloom Warden and the Kindled Champion. | 08 T14 Court part |
| Polyp models | Look at the eight polyp families once each has its own appendages. | 14 extra (polyp models) |

### Crimson Troupe

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| PR-10 | Look at the footage fixes (FX-25 to FX-28). Kept frames: `build/overnight/P15-verify1-shots/performers-00370.png` (cone footprint), `performers-01054.png` (knife and fan), `performers-01120.png` (Strongman), `liberation-00200.png` (carousel iron and bone, lanterns moved). All 2,609 frames: `build/circus-review/screenshots/`. Or re-film with `runCircusReviewClient`. | now |
| PR-10 (numbers) | The builder's picks inside the fixes: fan at -18, 0, +18 degrees and 3 ticks apart, knives at twice a needle's size, bone footprints every tick. The knife wake is tinted bone but reads crimson. | now |
| FX-27 (carousel) | The carousel trim and finial are bone, not crimson (the canopy already was). The iron pole is flat with no shading and may want a highlight pass by hand. The old pavilion's lanterns went with it in the B29 rebuild. | now |
| FX-28 (follow slots) | Walk with ground puppets anywhere: they stand in a row 2.5 blocks behind you, 1.6 apart, the Hulk 4.5 back. This changed following for every puppet, not only in the pavilion. | now |
| P15 lines | Read the new lines: `hemomancy.circus.school.ask_empty_ring`, `finale.busy`, `finale.settled`, `finale.no_route`, `finale.solo`, `message.hemomancy.circus.school.peaceful`, the Mnemonist's `circus.teaser.line1` and `line2`, the Waybill's `seeking.*` lines, `message.hemomancy.thread_ripper.vesper_strings`, the guest ledger line. | now |
| PR-26 (Circus overlay) | The perception stages' new motes (crimson spore, bone glow drifting to you, rising bone ember) were never seen in a client. `runCircusReviewClient -PcircusReviewOnly=perception`. | now |
| Circus play session | A natural-Survival apprenticeship and both endings, the finale's feel and arena bounds, two clients, relog and a cold restart, the Luminal Cicada, the rebuilt puppet and faculty models. Never done. | now (T15 built in B31) |
| D-29 (galleries) | Live proof of the real galleries and two clients at different acclimation. Placement and rarity were measured on 2026-11-15 (see "B30 placement"). | now |
| B29 pavilion | Walk the rebuilt pavilion: the big top, the two galleries and their stairs, the Ringmaster's box (talk to him from it), the rigging towers, seating, the midway and the backyard. Then judge the perception cues on the galleries, rails and rigging at each stage. Frames: `build/build2/B29-shots/` (lanterns at night, the day walk, the perception stages); all frames in `build/circus-review/screenshots/` (`lanterns-*`, `pavilion-*`, `perception-*`); cutaway `build/circus-kit/crimson_troupe_pavilion.png`. Say what wants more set dressing, a different layout or other lamp spots. | now |
| D-36 (audio) | Pick the Circus cue sounds by ear and supply the Ringmaster's music. Each event plays every candidate it has as a variant until you pick: `entity.circus_ringmaster.phase_change` (three: whip crack 529925, whip and crack 615761, 463491), `.finale_collapse` (two: snap-pizz 661854, sub-bass impulse 875528), `.hurt` (786287), `entity.impaled_procession.thread_sever` (486234), `.anchor_break` (105284), `entity.circus_performer.windup` (three: snare rolls 50711 and 50710, 13212). Files are in `assets/hemomancy/sounds/entity/circus_ringmaster/`, `impaled_procession/` and `circus_performer/`, mixed to mono; the sources are `tools/sound_audit/candidates/`. The music event `entity.circus_ringmaster.music` plays nothing until a track is added to `sounds.json`. | now (B31) |
| B31 finale | Play both finales on the rebuilt pavilion and judge the Ringmaster on the kit: the silent 60-tick opening, knives on the rafters beat, the Spotlight (look away in the light and you are held), the Knife Board (dash out), the Cue (the troupe strikes on his beat), Sleight (two doubles, only he bleeds) and Strings Cut (a downed performer's act). Then the yield (he kneels, the topper comes off, he speaks, the Pattern comes when he finishes, then he vaults home) and Liberation's 40-tick collapse. Tune the numbers in `docs/CIRCUS.md` ("Ringmaster finale presentation"). Re-film: `runCircusReviewClient -PcircusReviewOnly=succession,liberation`. Frames from this build are listed in the B31 section of `docs/owner_answers/BUILD_LOG_2.md`. | now (B31) |
| B31 lines | Read the new lines: `hemomancy.circus.ringmaster.yield` (his yield line, Ringmaster sheet), `hemomancy.circus.ringmaster.says`, `hemomancy.circus.finale.leash`, `hemomancy.circus.finale.spotlight`, the bar objectives `hemomancy.circus.finale.bar.troupe` and `.anchors`, the cards `boss.hemomancy.circus_yield.*` ("Yields the Ring") and `boss.hemomancy.circus_collapse.*` ("The Threads Let Go"), and seven subtitles. | now (B31) |
| D-37 (act numbers) | Tune each act's thread cost and cooldown, and the Ring's. Starting values are in `docs/CIRCUS.md` "What it is" (6 to 10 thread, 12 to 30 s; Ring 30 thread, 60 s). | now |
| B27 acts | Play the seven acts and the Ring from the Crossbar radial's Act entry, two or three bodies out. Say if Feint (threats turn on the flying Juggler), Smoke (3 s invisibility) and Lift (a throw with 2 s of slow fall) read right, and whether the Ring's thread tendrils are enough or want a pulse along the threads. | now |
| B27 pact lines | Read the five Ringmaster gate lines `hemomancy.circus.ringmaster.gate.needs_degree`, `needs_report`, `needs_degree_and_report`, `needs_understudy`, `unstained`, the act names, descriptions and refusals (`hemomancy.summon.act.*`, `screen.hemomancy.crossbar_radial.act.desc`), the Juggler's "Ranged Control" role and pin sentence, the Pattern's lore and Spindle tooltip, and the Waybill's `attuned` and `not_found` lines. | now |
| B27 looks | Judge the Circus dialogue theme and troupe crest: `build/build2/B27-shots/npc-trade-27-ringmaster-hub.png` to `npc-trade-33-ringmaster-trade-done.png`, and the sprite sheet `build/build2/B27-dialogue-sheet.png`. The Acrobat's airborne knee tuck and the carousel's new crimson glow motes were never caught in a frame; look at them on the next Circus re-film. | now |
| B28 kit | Judge the Circus site kit before T8 and T9 build on it: 37 set-piece blocks (poles, ring curb, seating, gallery rail, trapeze, net, valance, guy stake, podium, crimson footlight, Crossbar rig, Spindle rack, trophy, pelt drape, plinth, wagon panels and two-block wheels, cook pot, costume rack, trunk, ticket window, pennant line, the camp apparatus, canvas, wainscot, sawdust) and the four records as objects. Frames: `build/build2/B28-shots2/kit-sheet.png` and `kit-*.png`; model turnarounds in `build/model-previews/` (`circus_*`). Say which pieces want more detail, a different shape or colour. | now |
| B30 sites | Walk a camp and the abandoned stage, by day and at night, and judge the yard, the bank, the lean-to, the wagon, the lamp posts and each apparatus. Then judge the Liberated ruin (torn roof, canvas heaps, splintered ring, fallen trapezes) and the successor's banner behind the podium. Frames: `build/build2/B30-shots/sites-sheet.png` and its frames (`sites-00015.png` to `sites-00630.png`); all frames in `build/circus-review/screenshots/sites-*`. The camps are dark at night beyond their four lamps. | now |
| B30 placement | The validation run on three fresh seeds put the nearest pavilion at 653, 1,968 and 3,064 blocks; your aim was about 1,500. Most pavilion cells fail the biome (about 55%), the 6-block slope (about 20%) or wet ground (about 13%). Say whether to loosen the slope for the pavilion, widen its biome list, or keep it rare. Reports: `build/build2/B30-circus-report-<seed>.json`. | now |
| B30 gifts and lines | Read the gift line `message.hemomancy.circus.school.gift`, the Ringmaster's tenancy line `hemomancy.circus.ringmaster.tenancy` and the banner name `hemomancy.circus.tenancy.banner`. Judge the frame materials: Venous Pinion (Acrobat), Barbed Splinter (Dagger Thrower), Puppeteer's Wool (Stilt Walker), Hematic Iron Scrap (Strongman), Leather (Beast Tamer), Blaze Powder (Fire Eater), Engram Stamp (Understudy). | now |

### Dialogue, lessons and Liber

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| UN-21, RV-71 | Read the 2026-11-11 lesson lines: 169 lines and 91 option labels under `hemomancy.<npc>.craft.*`, six new item inquiries, and the Vicar's three P8 keys (`vicar.votary.rooted_vein.recovery`, `vicar.item_inquiry.earthen_vein.line1` and `.line2`). Use `lessonReviewClient`, brief mode, or `tools/i18n/voice_rewrite/pass5_lessons/<speaker>/proposed.json`. | Nothing. Best before T5, so the notes go into each teacher's sitting. |
| P12 "Read these" | Read the lines the overnight build added, changed or deleted, by lang key: the "Read these" list in the Morning report of `docs/owner_answers/BUILD_LOG_2026-11-14.md`. Four are flagged as the builder's calls (the Monolith's counsel for a stranger, "your own spent Bloom", the one shared switch warning, the D4 hint's "committed through a Mason's Effigy"). | Nothing. |
| PR-11, RV-12 | One session on the Trade and Conversation boxes: a blood tithe at a Harbinger, the Unstained refusal at D1, a Court custom card, a Circus curiosity card, a lore card and a lesson card, the "Today" ribbon (the Alchemist's daily card sits below the first screen), the Scout's hub. Judge trade balance and the rapport lore lines there. | Nothing. FX-15 is built. |
| FX-15 (look) | Look at `build/npc-trade-review/screenshots/npc-trade-08-alchemist-hub-scale3.png` and `npc-trade-09-alchemist-trade-scale3.png`. At GUI scale 3 the hub cards are taller and the hub scrolls. Say if the text should shrink or the window grow instead. | Nothing. |
| B32 (lines) | Read the lines B32 wrote or changed, listed under "For the owner" in [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md) section B32: the corrected lesson lines, the nine Observance prompts, the 84 Crimson Troupe professor lines, the three "later lesson" lines, the eleven Court newcomer lines and the recruit accept lines (now spoken after the pledge). In game: `lessonReviewClient`, a Circus professor at each step, an Unstained speaker with an Observance. A topic card title that is too long for one line now wraps; no review scene has one yet, so open a lesson card or a long Court question at GUI scale 3 to see it. | now |
| B33 (lines) | Read the Mnemonist's memory inquiries: 130 lines, two for each of 65 memories, by school, under `hemomancy.mnemonist.item_inquiry.memory_<id>.line1/.line2`. The list is under "For the owner" in [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md) section B33. In game: hold a memory and ask the Mnemonist about it. Check the four Canon Memories, the Memory of Vesper and Thread Ripper first; they say the most. | now |
| B34 (Liber pages) | Read the 26 new Liber pages (DS-9, NQ-16-3): the Hematic Armature chapter (the rack and its two rite lessons), 17 craft pages in The Hematic Order, Vesper, the Mycophant and Naeglerophaeon in Cosmic Forces, and the new Brined Court chapter (the eight houses, the Bloom Warden, the Kindled Champion). Frames: `build/build2/B34-shots/`. In game the crafts open at their Degree, the bosses when their fight begins, the Court chapter when you learn of the Court. Check the boss pages for tone and the Court page for the houses' order. | now |
| B37 (docs) | Read what the FX-21 docs pass changed: `HEMOMANCY_REFERENCE.md` section 38 (now a short table), the new archive `archive/done/HEMOMANCY_REFERENCE_DATED_NOTES.md`, the skill table in section 11 (37 skills added), the rewritten wiki pages (Blood-Systems tempers, vascular, skills, effects; Getting-Started items, biomes, keys; Unstained-Path stages, Still Arts, equipment; Harbinger-Path armour and tools), the lore lines in `LORE_REFERENCE.md` (member removal, seven ordeals, the D3 wake, the Neophyte proofs), and five changed lang lines (listed in the B37 log). | Now |

### Sound

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| UN-44 | Open `tools/sound_audit/candidates/audition.html` and listen. It holds 763 CC0 clips for 373 priority 1 events (40 of those events have no clip yet). Mark a pick or "none fit" per event, the shared palette first. Every verdict in the audit was made from file stats, not by ear. | Now. The picks are used once UN-18 is answered. |
| UN-36 | In any session, hear the same-day fixes: the quiet "not enough blood" refusal, Vesper and the Mycophant now fading with distance, the Loom's Staff charge ticking every 8 ticks. | Now |
| B35 (casts) | Hear a few casts and a Will's or Vesper's casts. Each cast now plays its own `manipulation.<id>.cast` event, which stands in with the same vanilla cue, so a player cast should sound as before. An entity's Blood Shot, Blood Needle and Deadly Gaze now make the player's cast sound (they were silent), an entity's Activation Potential uses the player's quieter cue, and an entity's Blood Cloud is heard over 16 blocks instead of 160. Umbral Rend plays one sweep, not two, and Crimson Flame priming TNT has no flint-and-steel scrape. | now |
| B35 (thinning) | Hear a termite chew (one quiet wood knock a second instead of a door bang every 8 ticks), a Stained Priestess plunge (only the first and last thorn of the line sound; the ring round the spear is silent), and the Mycophant's yield (the death sound now ends with the 6 s scene). | now |
| UN-70 | Fill in [SOUND_CREDITS.md](SOUND_CREDITS.md): 110 of its 121 rows read "source not recorded", including eight `.mp3` files under `sounds/entity/samples/` that no event plays (delete them if they are leftovers). | now |

### Architecture, tests and housekeeping

| Id | What to play, watch, read or tune | After |
| --- | --- | --- |
| Tree | Commit the work. On 2026-11-16, 1,822 tracked files are changed and 1,902 untracked files are new. Nothing is committed. Three changes are staged (renames). Restore points are `refs/overnight/start`, `refs/overnight/P01-before` to `P18-before` and `refs/build2/B01-before` to `B37-before`. | Your review of the build log |
| Build 2 report | Read the "Report" section at the top of [owner_answers/BUILD_LOG_2.md](owner_answers/BUILD_LOG_2.md): every package, the lines to read, the looks to judge, the numbers builders chose, the tests that changed meaning, and the PT-85 investigation. | Now |
| F-16, G-2b | Two clients: check that your pets, puppets, team-mates' monsters and a PvP-off player are spared by the Monster-filter effects and by Silent Severance, Autoimmune Edge, Silver Rebuke, the Saprovitta trail and Crimson Retribution, and that their kills pay you blood, Temper, skill points and purity. Watch for a trail pulse ending Penumbral Drift. | Now |
| F-15 | Swing a living weapon with dormant blood (it should cost nothing) and with too little blood (you keep the blood and lose or recall the weapon). | Now |
| F-13, F-14 | Wear a scar at low blood: upkeep stops at the Sanguine Reserve floor. Drink milk with the Eye scar: Night Vision returns within a second. | Now |
| F-4, G-7b | Die holding a Scale Grip item with a full pack: it waits for room and is never dropped. Land the Elytra effect or touch water in the Edacious set: flight stays. Drink milk in the air under the Elytra effect alone: you drop. Do it again in the Edacious set, then take the set off: you drop then too. | Now |
| B02 (F-16, FX-14) | Two clients: stand an ally's Hemomancy mob (a puppet or pet) in your Verdigris Aura; it is not slowed or weakened. Walk 150 blocks from a running rite with a second player: its boundary goes when you pass 128 blocks and comes back on return. Trigger Crimson Retribution in spawn protection as a non-operator: no Crimson Flames are placed. | Now |
| G-1, F-18 | Play a combat session and judge blood lost per hit before the balance pass. A zeroed or reduced hit now costs less blood, and a saved lethal hit costs only what got through (a 40-point hit at full health: Silent Refusal 3,095 mL in all, Phoenix Debt 1,295). Stack Iron Will with the avatar to feel the worst reduction (5.25 of 10). | Now |
| B03 (F-17, NQ-17-3) | Wear the Curved Horn alone and die twice inside 10 minutes: the first death leaves 1 health and costs 1 durability, the second kills. Wear it with Phoenix Debt ready: Phoenix Debt fires, not the Horn. Hold Endless Hour with the Horn worn: the hour holds the debt and the Horn is not spent. | Now |

---

## 5. Parked

Deferred or parked by your answers. Listed so they are not lost.

| Id | What is parked | Area |
| --- | --- | --- |
| CL-8 (backlash) | A backlash on a stranger's draw from a tank. A possible later feature; the owner raises it again. | Living blood and containers |
| CN-7 | Track 10's leftovers: Wills reading recent spend, a Tenebris or Lux world answer, spreading sod, Bestiary world pages. Deferred until the CL rulings land. | Ground network and farming |
| CL-19 | The Parish (villages). Its own design later. | Ground network and farming |
| CL-15 tiers, NQ-02-6 | Cloche tiers for specialty fungus. Your answer was "maybe". Parked until farming is played (PR-19), then a design: it is a third station on the upgrade rail (kits, rites, three bodies, proofs). | Ground network and farming |
| CN-8 | A vessel form on the syringe draw packet, so the draw animates on jars and vats. The sheet answer was left blank and it stays as built. It could ride on a later change to that payload. | Menagerie and the firsts |
| CQ-31 | Real quirks per kind. "As built for now": quirks stay notes read from existing rules until the owner starts a later pass. | Menagerie and the firsts |
| CL-13 | Caps on led creatures and vats. "As built for now" (no vat cap; one, two, three led at D3, D5, D7), revisited after PR-17. | Menagerie and the firsts |
| CN-3 | Vascular Dowsing's veins toward ore, and "Lode Draw follows the veins". Waits for a later visuals pass and a new owner-only cue. | Verbs, strand skills and cleats |
| VX-16 rest | The nine cube blocks, the Strider sabatons and the kitbashed Forge and Loom art. Your order was mobs first, then icons; these wait for a later art pass. | Creatures and spawns |
| CR-29 | The Mycophant's brood clutch and amber failure beat stay deferred to a later Mycophant pass. | Bosses |
| D-26, NQ-08-13 | Lux has no Summa entry. Waits for the Canon Memory rework to settle the Eye's rank, then goes to the next balance pass. | Saints and mausoleums |
| SM-3 (steles) | The Beholding steles carved with Hemorath's students are not modelled. Only the text was ruled. | Saints and mausoleums |
| PR-28 | Shader proof on a second GPU vendor. NVIDIA-only is accepted for now. | Visuals, particles and texels |
| VX-3 | A Vascular Dowsing visual upgrade. It stays a UI-like locator unless he asks. | Visuals, particles and texels |
| VX-9 (glyphs) | Rising glyphs on the Scriptorium, only if he wants letters back. | Visuals, particles and texels |
| VX-7 (redesign) | Bold Unstained rite visuals, and real rings and falls for the Bells, Clarity Ascension, Glass Lungs, Silver Veil, hand bell, moth shed and White Humour burst. Waits for the Unstained ritual system. | Visuals, particles and texels |
| VX-1 (held forms) | Any upgrade to the eight rebuilt held forms and the three lashes. A new decision after the re-grade in 09 T7. | Visuals, particles and texels |
| VX-6 (Neural Overload arc) | The audit's optional lightning arc beside Neural Overload's edge glow. Only if the glow reads weak in PR-26. | Visuals, particles and texels |
| Playthrough (no id) | Unstained playthrough bands, until that path's design settles. | D0 to D6 journey and bloodlines |
| D-3 | A design for what a Silentium gains at D8. Nothing new until it lands. | D7 to D8 endgame |
| DS-10 | Fungal mannequin, the Gardens digesting left gear, echo shader, JEI rendering category, hunters converging on a possessed facsimile, Silentium vessel HUD art. | D7 to D8 endgame |
| CN-11 | Spore load feeding manipulations and refilling blood. | D7 to D8 endgame |
| C-8 | An Apotheos section in the Liber. It comes with the canon memory rework (D-1, chunk 08). | D7 to D8 endgame |
| DS-3 | Per-floor ceremony mechanics (Endure, Hold, Share, Precision, sigil modifiers, a participatory procession). | Rites and Working crafts |
| D-6 | The Chimerization offering's name and recipe. | Rites and Working crafts |
| D-11 | The Unstained ritual system. | Rites and Working crafts |
| DS-11 | The Unstained invasion rite. | Rites and Working crafts |
| D-12 | Elder challenges and fuller Court trading. The Champion title gives recognition only. | Brined Court |
| HN-6 | Elders challenging a title-holder, the Tier 3 conflict between the poles, Sleeper waking, house greetings, Court lines in a house voice. | Brined Court |
| HN-7 | Deriving Court rapport from standing, after alpha. Until then standing and trade rapport stay two ladders (PT-56 pays the dialogue custom meanwhile). | Brined Court |
| D-41 | Circus NPCs on the jointed rig, and the brass and gold off the faculty costumes (`tools/circus/build_faculty.py`), both with a later pass. | Crimson Troupe |
| CD-6, CQ-55 | Successors get their own trees and their own lesson lines in a later pass. Until then they reuse the teacher's tree and tell its lessons in the teacher's voice. | Dialogue, lessons and Liber |
| DS-6, D-A | Merging the four quest frameworks into one. After alpha. | Architecture, tests and housekeeping |
| HK-2, E-10 | Removing the legacy block-blood reads and the origin-keyed pome count. Standing rule: whoever next edits those files removes them. | Architecture, tests and housekeeping |
| DS-7, D-D | `organic_prism` and `suspended_vivianite` are kept on purpose. Their use is undecided. | Architecture, tests and housekeeping |
| PR-29 | Reading the diagnostics of the older known test noise. Nothing to build until one of those tests fails again. | Architecture, tests and housekeeping |
| NQ-17-4, DS-5, C-16, 17 DS-5 | Folding advanced brewing into tinctures. Your answer of 2026-11-14: do not cut advanced brewing yet; a later Alembic pass builds on this discussion. The Alchemist's advanced brewing lines wait with it. | Architecture, tests and housekeeping |
| Sound S6 | Priority 2 (615 rows) and priority 3 (145 rows) of the sound audit: other biome loops, moods and music, second-rank voices and moves, the custom cue for the elder guardian curse of RV-66. UN-11 A builds priority 1 only. | Sound |

---

## 6. Where the old lists went

Everything below is in `docs/archive/tracks/owner_answers_2026-11/`. None of it is authoritative any more.

| File | What it was |
| --- | --- |
| `OWNER_ANSWER_SHEET-blank-2026-11-11.md` | The 520-row sheet as compiled, with no answers. |
| `answered-sheet-2026-11-13.md` | Your answered copy (it was `docs/Open Questions.md`). Its rulings are recorded in the topic docs. |
| `OPEN_QUESTIONS-before-2026-11-14.md` | This file as it stood before the rewrite. |
| `90-conflict-sheet.md` | The 14 conflict rows. Now group 1.1. |
| `91-new-questions.md` | The 183 review questions. Now sections 1 and 2. |
| `18-questions-never-on-the-sheet.md` | The 76 rows never asked. Now sections 1 to 4. |
| `92-still-to-do.md` | The first still-to-do list. Now sections 3 to 5. |

Still live in `docs/owner_answers/`: the 17 chunk files (the plans, each with a "Status on 2026-11-14" block), the
index `00-README.md` and the build log.

## Keeping this file

- An answered row moves to "Rulings that bind" in its topic doc and is deleted here.
- A built row is deleted here. Its record is the topic doc and the build log.
- A new question is added to section 1 with a recommendation, or to section 2 if a builder can decide it.
- No other file lists open questions.
