# 16. NPC dialogue, trade box, canon docs and Liber pages

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P12). |
| T2 | Built (P12, leftovers B32 2026-11-15): topic card titles wrap, unit cases for hub and topic card growth, `MonolithHubClientReview` reads the drawn hub, `NpcTradeReview` sets the Alchemist's rapport to the Trusted floor. |
| T3 | Built (B32, 2026-11-15): nine Observance lines; 84 Circus faculty lines (only reachable steps get a key: 35 status, 1 Threadkeeper status, 40 aftermath, 8 preparation) plus the Ringmaster's 5 aftermath lines; three "later lesson" lines (NQ-16-5). |
| T4 | Built (B33, 2026-11-16): 41 new Mnemonist inquiry files and 24 taken off the generic `memory_general` lines (removed), 130 lines with briefs, `MnemonistMemoryInquiryCoverageTest`. |
| T5 | Built (B32, 2026-11-15) except the stain branch, which waits on CQ-25 and CQ-45 (held). The eight false-today lines, the three ruling-driven lines earlier packages left (`alchemist.craft.temper.line2`, `.temper.fill.line1`, `vicar.craft.day.line2`), UN-9, UN-10 (lesson card summary) and UN-69 (`CoreLoopLessonNumbersTest`). The Sculpting word was B01; the other ruling-driven lines were B07, B09 and B10. CQ-50 to CQ-54 kept as built. |
| T6 | Built (B34, 2026-11-16): the `armature` chapter (rack page and two upgrade lessons), 14 Working craft pages (the Thurible as the D8 Apotheos relic) and three floorless craft pages in The Hematic Order, Vesper, Mycophant and Naeglerophaeon pages in Cosmic Forces, the `brined_court` chapter (NQ-16-3 A: houses page and both boss pages). Boss pages open when the fight begins. `LiberCraftAndBossPagesDataTest`. |
| T7 | Built (P12, B37 2026-11-16). P12: C-7 promoted, CD-2, CD-9 and CD-11 recorded. B37: the rest of FX-21 (the ready parts and the number tables, read from code after the balance packages; the lancea sentence was already right) and the cleanup audit's section G (section 38 as a table, the dated notes and appendices archived, five wiki pages rewritten, the "uncommitted" lines checked against git). What section G left is in the B37 log. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings ask for text work: lesson lines, Mnemonist inquiries, Liber pages, a docs pass and one screen fix.
The largest pieces are the Liber pages (DS-9, about 26 pages), the Mnemonist inquiries (CD-14, 41 memories) and the docs pass (FX-21 plus the older audit's section G).
Biggest surprise: CN-5 was already built. Round two P10 shipped 21 core-loop lessons across six speakers on 2026-11-11, minutes before the sheet was compiled, so the sheet's "no NPC names them" was stale on the day. What is left is a correction pass, because the 2026-11-12 cut, the 2026-11-13 HUD and about twenty rulings in other chunks changed facts the lessons state.
CD-9 is already fixed in the text, and two of CD-7's four shared-key groups (newcomer lines, item hints) are already per speaker.
The audit's "13 Working crafts" list is out of date: four of its names are no longer Working crafts, and today 9 of 18 crafts have no Liber mention at all.
The Brined Court has no Liber page of any kind, so the two Court boss pages have no chapter to live in.
All 41 uncovered memories are still live items. A further 24 Mnemonist memory inquiries only repeat one generic two-line answer.
Voice work is one speaker at a time, so the last section gathers every line another chunk asks for, by speaker.
Counts: 15 rows. BUILD 8, KEEP 2, DONE 2, MOOT 0, CONFLICT 0, UNCLEAR 0, OWNER 2, PARKED 1.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CN-5 | One voice pass per teacher to teach the core loop. | BUILD | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CoreLoopLessons.java` `ALL`: 21 lessons already exist (Hermit 2, Vicar 4, Alchemist 5, Mnemonist 3, Artificer 4, Vein-Mason 3), 288 `.craft.` keys in `en_us.json`. Every topic the row lists is taught. Some lines are now false (see T5). The work left is a correction pass, not a first pass. | T5 |
| DS-9 | Write the missing Liber pages. | BUILD | `src/main/resources/data/hemomancy/books/libersanguinium/` (10 chapters listed, every page grepped). No page for the Hematic Armature or its two rites. 9 of 18 Working crafts never named. `dried_gourd`, `hematic_stake`, `mycelial_synapse` never named. No page for Vesper, Mycophant, Naeglerophaeon, Phlegethosoma, Bloom Warden, Kindled Champion. | T6 |
| CD-2 | The Court `custom.opens` text is narration. | DONE | `en_us.json` `hemomancy.trade.court_<role>.custom.opens`, 8 keys (bellwarden, gardener, host, linesman, marrow_tender, nursery_tender, offering_bearer, tidewarden). All eight already read as narration, third person, no "I" or "you". Only the ruling needs recording. | T7 |
| CD-5 | Give Annetta's line to the player. | BUILD | `AnnettaDialogueTrees.java` node `truth` lists `hemomancy.annetta.draught.truth.line1` ("There's something inside you, Annetta...") as her own line. The option that opens the node, `hemomancy.dialogue.annetta.option.what_is_it`, is already the player saying nearly the same thing. | T1 |
| CD-6 | Successors get their own trees later. | PARKED | `docs/NPC_VOICES.md` open decision 10. Not checked further. CQ-55 (successors tell the lessons, as built) is the same subject. | none |
| CD-7 | Split shared keys speaker by speaker. | BUILD | Newcomer lines are already per speaker (`hemomancy.dialogue.newcomer.<npcId>`, 28 speaker keys, `DialogueHubFactory.java` line 159). Item hints are already per speaker (`hemomancy.<speaker>.item_hint`, 28 keys). Still shared: `hemomancy.dialogue.unstained.observance.available/.active/.ready` (`UnstainedObservanceDialogueDecorator.java` lines 48 to 50) and `hemomancy.circus.school.status.*` (6) and `.aftermath.*` (5) (`CircusSchoolDialogue.java`, built as `root + "status." + step`). | T3 |
| CD-9 | Fix `votary.temper_lore` and the identical clairaudiograph lines. | DONE | `hemomancy.vicar.votary.temper_lore` now names the eight tempers and then gives the enzyme names as the Alchemist's words. No `item_inquiry.clairaudiograph.line*` key exists: the three `clairaudiograph.json` inquiries (alchemist, artificer, mnemonist) each give one `hemomancy.clinical.inquiry.*` line of their own. One leftover is noted under T7. | T7 |
| CD-14 | Write Mnemonist inquiries for the memories that have none. | BUILD | Counted `memory_*` items in `ItemInit.java` (114) against `data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/` (84 files, 73 memories). 41 memories have no file, still 41. All 41 are registered items. 39 have a recipe, Thread Ripper and Memory of Vesper are earned. None was cut. | T4 |
| C-7 | Promote the Annetta encounter from Partial. | BUILD | `docs/HEMOMANCY_REFERENCE.md` line 6541 still says "Partial". `docs/LORE_REFERENCE.md` lines 51 and 1168 say the same, line 1179 has the stale lancea line. `StainedLanceEntity.java` and `StainedLanceRenderer.java` exist. | T7 |
| RV-12 | Keep the trade box rules and review balance in play. | KEEP | `DialogueCategory.java` order, `NpcTradeService.java` `fits` check before anything is taken, `DialogueRapportLore.java` Known and Trusted lines only, 61 cards in 30 speaker folders. No trade file changed after 2026-11-11 14:00. Balance is PR-11. | none |
| RV-42 | Keep: the Vicar's newcomer card leaves when blood wakes, his right-click sets the met-Vicar flag. | KEEP | `HarbingerVicarEntity.java` line 172 `DialogueHubFactory.markMetVicar`. `DialogueHubFactory.java` lines 55 to 56 gate his card on `isActive()`. The entity file changed after the sheet, the call is still there. | none |
| RV-71 | The owner reads the Vicar's P8 lines. | OWNER | `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` P8 names three keys. All three are in `en_us.json` today. Keys listed under "Still to do". | none |
| FX-15 | Stop trade card lines and hub summaries clipping. | BUILD | `DialogueTradeView.java` line 293 draws `Math.min(2, line.size())` lines and line 288 draws only the first title line. `DialogueScreen.java` line 245 calls `drawWrapped(..., 2, ...)` for hub summaries and line 353 for topic summaries. `DialogueTradeLayout.CARD_HEIGHT` is a fixed 54. | T2 |
| FX-21 | One docs pass over the stale tables. | BUILD | Each item checked against today's docs. Most are still stale, a few were fixed in passing. Table in T7. | T7 |
| PR-11 | One owner session on the Trade and Conversation boxes. | OWNER | `docs/NPC_DIALOGUE_AND_TRADE.md` "Proof owed": the "Today" ribbon and the Scout's hub are still unseen in a client. | none |

## Plan

### T1. Annetta's line goes to the player (size S)

- Rulings: CD-5
- Change: remove `hemomancy.annetta.draught.truth.line1` from the `truth` node, so the node opens on her stage direction (`truth.line2`) and her answer (`truth.line3`). Move the sense of the removed line into the player's option `hemomancy.dialogue.annetta.option.what_is_it` (now "It'll draw out what's inside you."), for example "There's something inside you, Annetta. This will draw it out." Delete the unused key and its brief.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AnnettaDialogueTrees.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`, `src/main/resources/assets/hemomancy/dialogue_briefs/lady_fungal_wanderers.json`
- Tests: `src/gameTest/resources/dialogue/route-fingerprints.txt` holds 260 lines that mention annetta. Regenerate the rows that change and read the diff (`DialogueRouteCharacterisationGameTests`).
- Docs: `docs/NPC_VOICES.md` open decision 8 becomes Applied. `docs/NPC_DIALOGUE_AND_TRADE.md` moves it from "Open questions" to "Rulings that bind".
- Depends on: nothing

### T2. Trade cards and hub summaries show their whole text (size S to M)

- Rulings: FX-15
- Change: "Fit or wrap" is planned as one thing: the card is sized to the wrapped text, so nothing is cut. Trade cards: `DialogueTradeLayout.calculate` takes each card's wrapped line count (title and line) and makes the body `54 + extra lines x LINE_HEIGHT` instead of a fixed `CARD_HEIGHT`. `DialogueTradeView` draws every title line and every card line. Hub boxes: `DialogueScreen` draws as many summary lines as the 76 px card holds instead of 2, and `DialogueHubLayout` grows the card if a summary still does not fit at the narrowest width. Topic cards (line 353) get the same treatment. No text is shortened.
- Files: `src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueTradeLayout.java`, `DialogueTradeView.java`, `DialogueScreen.java`, `DialogueHubLayout.java`, `DialogueTopicCardLayout.java` (same folder)
- Tests: `src/test/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueTradeLayoutTest.java` gains cases for a three-line and a four-line card and for the expanded strip sitting under a taller body. Proof in a client: `npcTradeReviewClient` and `monolithHubReviewClient` (rows in `gradle/review-clients.gradle`), one shot at GUI scale 3 where the summary "Assignments, progress, and rewards." clipped.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` "Left for the user by the trade build" loses its two clipping bullets.
- Depends on: nothing. Build before PR-11.

### T3. Shared keys split by speaker (size S for the Unstained, M for the Circus)

- Rulings: CD-7
- Change, Unstained: the three Observance prompts become nine keys, `hemomancy.dialogue.<acolyte|zealot|guardian>.observance.<available|active|ready>`, each written to its speaker's sheet in `docs/NPC_VOICES.md`. `UnstainedObservanceDialogueDecorator` picks the key from the speaker it decorates.
- Change, Circus: the six status keys and five aftermath keys become per-role keys, `hemomancy.circus.school.<role>.status.<step>` and `.aftermath.<route>`, for the eight faculty roles (88 lines). `CircusSchoolDialogue.tree` builds the key with the role, as it already does for `welcome`, `history` and `colleagues`. `hemomancy.circus.school.preparation` is also one key for all teachers and gets the same split.
- Change, already done: newcomer lines and item hints are per speaker today. Record that. Nine Court newcomer lines still open on "new to the blood" (noted in `docs/audits/2026-11-08-dialogue-voice/README.md`), which is a voice fix for the Court pass, not a split.
- Files: `UnstainedObservanceDialogueDecorator.java`, `CircusSchoolDialogue.java` (both in `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/`), `en_us.json`, `src/main/resources/assets/hemomancy/dialogue_briefs/unstained_church.json`, `circus_monolith.json`
- Tests: `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/UnstainedObservanceDialogueDecoratorTest.java` (asserts the speaker's own key). `route-fingerprints.txt` has 51 lines naming observance or the school. Regenerate on purpose. `HardCodedEnglishGuardrailTest` is unaffected (keys only).
- Docs: `docs/NPC_VOICES.md` open decision 11 becomes Applied, with what was split and what already was. `docs/NPC_DIALOGUE_AND_TRADE.md` the same.
- Depends on: the Circus half should be written in the same sitting as chunk 15's Circus lines (PT-59 to PT-61, D-30, D-31, CD-8), one role at a time. NQ-16-5 decides whether `hemomancy.clinical.inquiry.later` joins this task.

### T4. Mnemonist inquiries for the 41 memories (size M to L)

- Rulings: CD-14
- Change: one inquiry file and two lines per memory, in the Mnemonist's voice (they state, they file, they do not flatter). 41 files under `data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_<id>.json`, 82 keys `hemomancy.mnemonist.item_inquiry.memory_<id>.line1/.line2`, 82 briefs.
- The 41: absolute_stillness, activation_potential, blood_lattice, blood_needle_fan, blood_needle_lance, canopy_mortis, carrion_communion, chain_blood_binding, crimson_coronation, crimson_flame_conjuration, eclipse_well, expansive_blood_cloud, expulsive_updraft, funeral_bell, furnace_veins, guided_blood_shot, hematic_mortar, iron_choir, lignum_mortis, lingering_blood_binding, living_circuit, penumbral_drift, phoenix_debt, pursuing_blood_cloud, rimebound_sentence, sanguine_halo, sanguine_marionette, sanguine_tempest, soaring_updraft, sovereign_instinct, summon_avatar_armor, summon_avatar_arms, summon_avatar_complete, summon_avatar_legs, suspended_updraft, synaptic_storm, tendon_line, thread_ripper, white_verdict, worked_lignum, and Memory of Vesper (`memory_of_vesper`, which the Artificer and the Monolith already answer).
- Nothing is dropped. Three need care after the 2026-11-12 cut: Blood Needle Fan has only its R cast now, so its lines must not describe a held fan. Blood Needle Lance holds as a hovering volley. Crimson Flame Conjuration holds as painted fire. Write those from `hemomancy.manipulation.<id>.desc` and `.shaping.project` as they read today.
- Four holding memories (Tendon Line, Lignum Mortis, Carrion Communion, Furnace Veins) have nothing or little to cast, so their lines say what the held verb does, as `memory_living_land` and `memory_venous_cling` already do. Do not use the word Shaping in any new line (CL-3).
- Memory of Vesper: the Mnemonist may say what the memory is. She may not state Silentium as fact (open decision 2 binds every outpost speaker).
- Files: the 41 new JSON files, `en_us.json`, `src/main/resources/assets/hemomancy/dialogue_briefs/mnemonist_antecedent.json`
- Tests: `ItemInquiryResourceValidationTest` and `DialogueInquiryCanonTest` (both in `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/`) must stay green. Add a JVM test that every registered `memory_*` item has a Mnemonist inquiry file, so the gap cannot reopen.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` inquiry count (397 becomes 438) and the stale "385" in "Proof owed". `docs/HEMOMANCY_REFERENCE.md` line 1658 (says 317). `docs/NPC_VOICES.md` Mnemonist "Fixed text" if any new line is pinned.
- Depends on: chunk 05 for the Sculpting wording. NQ-16-4 decides whether the 24 generic files are rewritten in the same sitting.

### T5. Core-loop lessons: correction pass, one teacher at a time (size M)

- Rulings: CN-5
- What each teacher covers today (all of it already has lines):

| Teacher | Lessons (`id`: branches) | Topics from the row |
| --- | --- | --- |
| Mnemonist | `shapings`: first, work, mend, draw. `strands`: forms, stain, brand. `land`: strike, dark, light | second-wave Shapings, the field of dark |
| Artificer | `hot_runs`: uses, heat, feeder. `staff`: rings, dig, forms. `strand_line`: reach, crack, twin. `climb`: root, cleats, line | strand skills |
| Alchemist | `living_blood`: keepers, clot, gauge. `fed`: drip, basin, effusion. `temper`: fill, dry, catalysts. `humours`: sample, study, dose. `beds`: strains, timber, crops | strains, crops |
| Vicar | `verbs`: key, order, draw. `day`: food, rest, meats. `answers`: count, moon, water. `keeping`: lead, feed, brood | the vat screen, brood, hunters |

- What the 2026-11-12 cut and the 2026-11-13 HUD made false. These lines are wrong in the game today:

| Key | Says | Now |
| --- | --- | --- |
| `hemomancy.mnemonist.craft.strands.stain.line1/.line2` and its option | A plant sunk in a wet stain runs along it. | No registered memory holds a plant. The branch teaches nothing a player can do. |
| `hemomancy.artificer.craft.strand_line.reach.line1` | Taut Line "fills a jet's pressure faster". | No memory holds a jet. Taut Line has only its lash half. |
| `hemomancy.artificer.craft.strand_line.crack.line1` | Whipcrack raises "every pulse of a spray". | No memory holds a spray. |
| `hemomancy.artificer.craft.strand_line.twin.line1` | The twin is "a trailing lash, a heavier jet, a wider spray, a deeper plant". | Only the trailing lash is left. |
| `hemomancy.mnemonist.craft.strands.line2` | "Click while a strand is out to snap it, for extra blood." | A lash: click strikes once, click while holding sends it down the crosshair. Reword to the lash. |
| `hemomancy.alchemist.craft.temper.catalysts.line2` | "The phials under your blood bar show what you hold." | The Temper vials show only with the creative-only Titration Bandolier (CQ-63). |

- What still stands after the cut: every `shapings`, `land`, `hot_runs`, `staff`, `climb`, `living_blood`, `fed`, `humours`, `beds`, `verbs`, `day`, `answers` and `keeping` line, the `strands.forms` lines (already rewritten on 2026-11-12 to "Only the lashes are strands now"), and `strands.brand` (Blood Binding still brands: `BloodBindingShaping.java` line 105).
- Facts other chunks' rulings will move. I read their rows, I did not review them. Each lesson line is rewritten when its ruling is built:

| Ruling (chunk) | Lesson line it touches |
| --- | --- |
| CL-3 rename to Sculpting (05) | 17 NPC lines and options say "Shaping": `hemomancy.mnemonist.craft.shapings.*` (10), five `hemomancy.dialogue.mnemonist.option.craft.shapings*`, `hemomancy.alchemist.craft.temper.line2`, `hemomancy.mnemonist.slots.shaping` |
| CQ-44 water breaks gouges, not living veins (02) | `hemomancy.artificer.craft.hot_runs.feeder.line2` and `hemomancy.anchorite.craft.ground_marks.cut.line2` both say water does not wash a gouge out |
| CQ-7 the feeder drips 15 mL each feed (02) | `hemomancy.artificer.craft.hot_runs.feeder.line1/.line2` |
| CQ-43 about 4 furnaces per hot run (02) | `hemomancy.artificer.craft.hot_runs.heat.line1` |
| CQ-15 Furnace Veins priced per furnace (02) | `hemomancy.mnemonist.craft.shapings.mend.line2` |
| CL-14 a living vein moves blood only when something draws (02) | `hemomancy.anchorite.craft.ground_marks.vein.line2`, and the Vicar's `hemomancy.vicar.item_inquiry.earthen_vein.line2` |
| CB-21 cleats x0.5 (05) | `hemomancy.artificer.craft.climb.cleats.line1` says "drops to a quarter" |
| CN-10 cleats applied to any Hemomancy boots at the Armature (05) | `hemomancy.artificer.craft.climb.cleats.*` and its option "What are Hematic Cleats for?" |
| CQ-8 rings count only blood spent or drawn from creatures (04) | `hemomancy.artificer.craft.staff.line1` says everything moved through the Staff counts |
| CQ-41 the field of dark hides the caster only (04) | `hemomancy.mnemonist.craft.land.dark.line2` says anyone standing in it is hidden |
| LP-14 Temper burn by price band, R burns 2 to 8 (04) | `hemomancy.alchemist.craft.temper.line2` gives 1, 2 and 3 to 5, and half a measure a pulse |
| LP-15 coal and charcoal cut to 1 (01) | `hemomancy.alchemist.craft.temper.catalysts.line1/.line2` ("three from the old reagent") |
| CL-12 unexamined blood is weaker (03) | `hemomancy.alchemist.craft.humours.line1` says it "acts the same whether you've read it or not" |
| CQ-6, LP-3, LP-4 clot takes capacity and is revived by projected blood (01) | `hemomancy.alchemist.craft.living_blood.line2`, `.keepers.line1` |
| CN-9 a bloodline member stands in for an offline owner (01) | `hemomancy.alchemist.craft.living_blood.keepers.line2` |
| CQ-9 Temper fills at full blood (04) | `hemomancy.alchemist.craft.temper.fill.line1` (nothing false, a sentence to add) |
| CB-1 base regeneration 2 mL/s (04) | `hemomancy.vicar.craft.day.line2` says about a millilitre a second |
| CB-3 tainted meat 10, raw fish 4 (02) | `hemomancy.vicar.craft.day.meats.line2` says tainted meat gives the most of all |

- Change: one sitting per teacher, in this order: Mnemonist, Artificer, Alchemist, Vicar, then the Vein-Mason's one line. Each sitting fixes that teacher's rows from both tables, updates the briefs and the writing notes, and regenerates that teacher's fingerprint rows. The six "false today" rows can be fixed at once (NQ-16-1, NQ-16-2). The ruling-driven rows wait for their rulings, so the cheapest order is to run this task after chunks 01, 02, 04 and 05 are built.
- Files: `en_us.json`, `CoreLoopLessons.java` (a branch id changes if `stain` is replaced), `src/main/resources/assets/hemomancy/dialogue_briefs/alchemist.json`, `mnemonist_antecedent.json`, `artificer_anchorite_clinical.json`, `vicar_succession.json`, notes under `tools/i18n/voice_rewrite/pass5_lessons/<speaker>/`
- Tests: `CoreLoopLessonsTest` (pins lesson ids, the "no Quest Work word" rule and that all 29 core-loop pages are taught), `CoreLoopLessonGameTests` (13 tests), `route-fingerprints.txt` and `src/test/resources/dialogue/lore-options.txt` regenerated on purpose. Client proof: `lessonReviewClient`.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` lesson table (branch names, page titles) and a "Rulings that bind" line for CN-5. `docs/NPC_VOICES.md` "Fixed text" for the four teachers. `docs/CORE_LOOP_TEACHING_LEDGER.md`. `wiki/Getting-Started.md` line 302 if a lesson's subject changes.
- Depends on: chunk 05 (the Sculpting naming, and what Second Strand, Taut Line and Deep Brand do beyond the lash), chunks 01, 02, 03 and 04 for the ruling-driven rows, CQ-63 for the phials line.

### T6. The missing Liber pages (size L)

- Rulings: DS-9
- The real gaps today, checked page by page:

| Group | Has a page or a real mention | Missing |
| --- | --- | --- |
| Hematic Armature | none | The station itself, and upgrade pages for `armature_consecration` (D5) and `monolithic_armature` (D7). Neither rite has a `registerUpgradeLesson` in `LiberEntryDefinitions.java`. The other six stations' rites do. |
| Working crafts (18 in `recipe/cardinal_rite/working/`) | Own pages: Resonant Forge, Enzymatic Scriptorium, Somatic Loom, Morphling Cradle. Passing mention only: Scarlet Vanity, Fungal Implantation Pylon, Morphling Incubator, Mycelial Lantern, Mason Effigy. | Never named: Sporitic Thurible, Mnemonic Reliquary, Somnolent Seat, Runic Chisel Station, Dendritic Distributor, Vacant Effigy, Sanguine Monolith, Covenant Throne, Vascular Effigy. With the five passing mentions that is 14 crafts without a page of their own. |
| Floorless crafts (11 pinned in `WorkingCraftRosterDataTest.java`) | Living Staff, Iron Brazier, Centrifuge, Alembic, Pallid Retort and the Liber have text. | Dried Gourd, Hematic Stake, Mycelial Synapse are never named, as the row says. |
| Bosses | The four Saints and Annetta (`cosmic_forces/`). Vesper is one mention in `manipulations/pages/ruinous.json`, Naeglerophaeon one in `neurotic.json`. | Vesper, Mycophant, Naeglerophaeon, Phlegethosoma, Bloom Warden, Kindled Champion. |

- The audit's list of 13 no longer matches. `visceral_mirror` and `warp_chair` are not ids in the game (the Scarlet Vanity and the Somnolent Seat are the crafts there today, and I did not trace the renames). `semi_sentient_construct` is a block with no recipe and `mind_spike` an item with no recipe, so neither gets a page.
- Change: about 26 pages of inline English `hutoslib:page` JSON, as every Liber page is. Three for the Armature in a new `armature` chapter beside `alembic`, `centrifuge` and `resonant_forge`. Working and floorless craft pages in `the_hematic_order` (or a new chapter, NQ-16-3). Boss pages in `cosmic_forces`, which already holds the Saints and Annetta. Each page is registered in `LiberEntryDefinitions.java` with a Degree floor in `LiberKnowledgeHelper.java` and an opener in `LiberDiscoveryEvents.java`: a craft page opens at the craft's `required_degree` or on picking up its subject, the two Armature upgrade pages through `registerUpgradeLesson`, a boss page when its fight first starts (the precedent is Annetta's page opening when her route starts, kept by RV-74).
- Hold back two pages: the Sporitic Thurible (PT-31 in chunk 10 moves it toward an Apotheosis relic) and the Phlegethosoma page's summoning paragraph (CR-7 in chunk 06 changes how it appears). The Court boss pages wait for NQ-16-3.
- Tone: Order record for stations and crafts. Boss pages follow `docs/LORE_REFERENCE.md`: Vesper is he, the Mycophant is she, no God or Satan framing, nothing past Archon stated as fact in the Order's voice.
- Files: `src/main/resources/data/hemomancy/books/libersanguinium/<chapter>/pages/*.json` (and one new `chapter.json`), `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java`, `LiberDiscoveryEvents.java`, `LiberKnowledgeHelper.java`
- Tests: `src/test/java/com/vincenthuto/hemomancy/architecture/LiberPageRegistrationGuardrailTest.java` fails on any page file that is not registered or has no floor or opener, so it drives this task. Add a JVM test that every Working craft in the roster and every Armature rite has a page. Client proof: `LiberPagesClientReview` (in `src/gameTest/java/com/vincenthuto/hemomancy/gametest/`).
- Docs: `docs/STATION_UPGRADES.md` (the Armature now has pages and lessons), `docs/CARDINAL_RITES.md`, `docs/BOSSES.md`, `docs/HEMOMANCY_REFERENCE.md` Liber section, `docs/audits/2026-11-02-cleanup/README.md` row C-12b closes.
- Depends on: chunk 13 DS-4 (one Liber line for each rite without a chapter) and chunk 12 C-8 (an Apotheos section) are the same kind of work and should share the sitting. CN-10 (chunk 05) changes what the Armature page says about cleats.

### T7. One docs pass (size M, plus section G as its own L)

- Rulings: FX-21, C-7, CD-2, CD-9
- C-7: change "Partial" to "Implemented, balance pass pending" at `docs/HEMOMANCY_REFERENCE.md` line 6541 and `docs/LORE_REFERENCE.md` lines 51 and 1168. Delete the "Remaining WIP" lancea sentence at `docs/LORE_REFERENCE.md` line 1179 and say the Priestess throws her own `StainedLanceEntity`. Close the two matching bullets in `docs/BOSSES.md` (lines 117 and 123). CR-16 in chunk 07 (lower her health) is the balance pass still pending.
- CD-2 and CD-9: record both in `docs/NPC_VOICES.md` (decision 14 Applied, and a line in the Brined Court section that `.opens` text is narration) and in `docs/NPC_DIALOGUE_AND_TRADE.md` (close the three `custom.opens` notes and the decision 14 line). One leftover from CD-9: `hemomancy.vicar.votary.temper_lore` asks "How many tempers...?" and answers itself, which the Vicar's sheet forbids ("the question has to want an answer"). Fix it in the Vicar's sitting of T5.
- FX-21, each item as it stands today:

| Item | File and place | State today |
| --- | --- | --- |
| Phlegethosoma drop | `docs/HEMOMANCY_REFERENCE.md` line 6744 | Stale. Still "one to two Calcified Blood Spines". The audit read `phlegethosoma.json` as Barbed Splinter and 2 to 4 Formations. CR-7 will also change the spawn text on the same line. |
| Vesper "defeat" | `docs/HEMOMANCY_REFERENCE.md` lines 2217 to 2218 | Stale. Still "transitions on defeat" and "the final kill". CR-22 (chunk 07) changes the reward wording too. |
| Saint move counts | `docs/SAINT_MAUSOLEUMS.md` lines 142 and 144 | Stale. "fourteen moves", "fifteen moves and four strings". The audit counted 16 plus the Flood, and 21 moves and 7 strings. |
| Saint damage scaling | `docs/SAINT_MAUSOLEUMS.md` line 154 | Stale. Still "Most Saint damage is fixed magic". |
| "The Saint is D8" | `docs/SAINT_MAUSOLEUMS.md` lines 13 and 98, `docs/HEMOMANCY_REFERENCE.md` line 2013 (§5.8 heading) | Stale. C-3 (chunk 08) rules the door is the gate, so say the ward, Chapels and ring door are D8 and the tomb itself has no Degree check. |
| 115 manipulations | `docs/MANIPULATIONS_AND_CASTING.md` line 3 and the roster table under it | Half fixed. The status line says 115 (`ManipulationInit.java` has 115 registrations). The roster table is still pass 2's (Animus 23, Ferric 15, Mortem 15), and "33 Shapings in all" predates both the audit's 44 and the 2026-11-12 cut. Recount from code. |
| Shaping price range | `docs/CORE_LOOP.md` line 79 | Half fixed. Now "4 to 60 mL/s". The audit found 4 to 48 for non-strand Shapings. Recount after the cut, and after chunk 04's reprice. |
| Tainted meat | `docs/HEMOMANCY_REFERENCE.md` line 404 | Stale ("40 mL"). Line 162 already says 60. CB-3 moves it again. |
| `vascularDebuffsEnabled` default | `docs/HEMOMANCY_REFERENCE.md` line 1225 | Stale. Doc says `true`. `HemoServerConfig.java` line 289 defines `false`. |
| Two config rows | `docs/HEMOMANCY_REFERENCE.md` lines 1279 and 1287 | Stale. `claimedWillBonusCapSilentArchon` and `realEffectMinRank` are not literals anywhere under `src/main/java`. |
| Weapon-form secondary Tempers | `docs/HEMOMANCY_REFERENCE.md` §8.2 (starts line 3025) | Not rechecked. |
| Well Rested, chum width | `wiki/Blood-Systems.md` lines 277 and 114 | Stale. "twice the base on top" and "up to 12 blocks across" (12 is the radius). |
| Saints "post-release WIP" | `docs/HEMOMANCY_REFERENCE.md` line 2095 | Stale, and it is chunk 08's C-1. Do it in the same pass. |
| Inquiry count | `docs/HEMOMANCY_REFERENCE.md` line 1658 (317), `docs/NPC_DIALOGUE_AND_TRADE.md` "Proof owed" (385) | Stale. 397 files today. Set once T4 lands. |
| Archon severing | `wiki/Harbinger-Path.md` line 277 | Stale. "As Archon, you can ritually sever members". Lore gives that to the bloodline's progenitor at D6 (`docs/LORE_REFERENCE.md` lines 419 and 1222). |
| Priestess lancea | `docs/LORE_REFERENCE.md` line 1179 | Stale. Covered by C-7 above. |
| Mycophant Brood Call and Siphon, Saint loot, Hemorath's wells and ending, Putriciel's Offering, Velorum's congregation, the 153 profiles | `docs/HEMOMANCY_REFERENCE.md` | The audit's quoted phrases are no longer in the file, so these look fixed or reworded. Read each once in the pass, I did not compare them with code. |
| Cleanup audit section G | `docs/audits/2026-11-02-cleanup/README.md` lines 179 to 208, detail in `docs-and-test-drift.md` (89 findings) | Open. Spot checks still fail: five wiki pages name GeckoLib (deleted 2026-11-03), `docs/LORE_REFERENCE.md` line 414 still has the "Blood Conduit pipeline" and line 156 still has §3.3 Transcendence (C-4, chunk 12), thirteen docs still say "uncommitted". |

- Change: fix the rows above from code in one sitting (M). Section G is larger: it also rewrites reference §38 as a short table, moves thirteen dated changelog sections and seven appendices out of the reference, and corrects five wiki pages. Plan it as a second sitting (L) and work it from `docs-and-test-drift.md`, re-checking each finding first because that list is eleven days old.
- Files: `docs/HEMOMANCY_REFERENCE.md`, `docs/LORE_REFERENCE.md`, `docs/SAINT_MAUSOLEUMS.md`, `docs/MANIPULATIONS_AND_CASTING.md`, `docs/CORE_LOOP.md`, `docs/BOSSES.md`, `docs/NPC_VOICES.md`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `wiki/Blood-Systems.md`, `wiki/Harbinger-Path.md`, `wiki/Getting-Started.md`, `wiki/Home.md`, `wiki/Mod-Compatibility.md`, `wiki/Developer-Reference.md`
- Tests: none change. If the pass edits a doc that a source-text test reads, regenerate `docs/architecture/evidence/source-text-impact-map.csv` with `python gradle/source-text-impact-map.py`.
- Docs: this task is the docs. Each topic doc touched moves the answered question from "Open questions" to "Rulings that bind".
- Depends on: best run last, after the balance chunks (01, 02, 04, 06, 07, 08) have changed their numbers, or the same tables go stale again. The C-7, CD-2 and CD-9 records can be written at any time.

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-16-1 | CN-5 | Six lesson lines are false in the game today (jet, spray and plant halves, the stain run, the snap). Fix them now as lash-only, or wait for your call on what Second Strand, Taut Line and Deep Brand do beyond the lash? | A: fix now. The Artificer's `strand_line` teaches the lash halves only, and the Mnemonist's `stain` branch is replaced by "How do the three lashes differ?". B: wait for the new second effects and write once. | A, because a teacher is stating things no player can do, and the lash halves will not change. |
| NQ-16-2 | CN-5 | The Alchemist says the phials under the blood bar show your Temper. Since 2026-11-13 they show only with the creative-only Titration Bandolier. | A: drop the sentence until the Bandolier has a giver (CQ-63). B: keep it and hand the Bandolier out soon. | A, because a survival player has no phials to look at. |
| NQ-16-3 | DS-9 | Where do the new pages live? The Brined Court has no Liber page at all, so the Bloom Warden and Kindled Champion pages have no chapter. | A: an `armature` chapter, craft pages inside The Hematic Order, all six bosses in Cosmic Forces. B: the same, plus a new Brined Court chapter with one house page and the two boss pages. C: leave the two Court bosses out until the Court gets a chapter. | B if you want the Court in the Liber at all, since the Vicar refers a D5 Harbinger to it and the book never mentions it. Otherwise C. |
| NQ-16-4 | CD-14 | 24 existing Mnemonist memory inquiries only repeat the generic `memory_general` two lines (Blood Eclipse, Glacial Grasp, Hemorrhage, Pyretic Forge, Vital Effusion, Vascular Dowsing, the four canon memories and 14 more). Write their own lines in the same sitting? | A: yes, 65 memories in one Mnemonist pass. B: only the 41 with no file. | A, because to the player a generic answer is the same as none, and the speaker is already loaded. |
| NQ-16-5 | CD-7 | The 2026-11-08 audit found one more shared spoken key: `hemomancy.clinical.inquiry.later` is said by the Alchemist, the Artificer and the Mnemonist, and names "the Alchemist" in the third person even when the Alchemist says it. Split it too? | A: yes, three keys. B: leave it. | A, three short lines and it removes a plain slip. |
| NQ-16-6 | CN-5 | When Shaping becomes Sculpting, do the lesson ids follow (`craft.shapings`, node `craft_shapings`, event `mnemonist_craft_shapings`), or only the words players read? | A: words only, ids stay. B: rename the ids too. | B, because the mod is not live, the fingerprints change with the text anyway, and a stale id is the kind of drift the audits keep finding. Chunk 05 owns the rename, so answer it there if it is asked there too. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CN-5 | The correction pass over the lessons. | Build | T5, after chunks 01, 02, 04, 05 and NQ-16-1, NQ-16-2, NQ-16-6 |
| DS-9 | About 26 Liber pages and their registration. | Build | T6, NQ-16-3 |
| CD-5 | Move Annetta's line to the player. | Build | T1 |
| CD-7 | Nine Observance lines, then 88 or so Circus faculty lines. | Build | T3, the Circus half with chunk 15 |
| CD-14 | 41 Mnemonist inquiries (65 if NQ-16-4 is yes). | Build | T4, the Sculpting wording |
| FX-15 | Cards sized to their text. | Build | T2 |
| FX-21, C-7 | The docs pass, then section G. | Build | T7, best after the balance chunks |
| CD-2, CD-9 | Record the two rulings in the voice sheet and topic doc. | Build | T7 |
| CD-6 | Successors' own trees. | Parked | A later pass |
| RV-71 | Read three Vicar keys in `en_us.json`: `hemomancy.vicar.votary.rooted_vein.recovery` (line 5707), `hemomancy.vicar.item_inquiry.earthen_vein.line1` (5629) and `.line2` (5630). In game: the Rooted Vein lesson at D2, and ask him about a held Earthen Vein. Note that `.line2` will change with CL-14. His 32 lesson lines, `hemomancy.vicar.craft.*` (lines 6096 to 6127), were also written without your read. `tools/i18n/voice_rewrite/pass5_lessons/vicar/proposed.json` holds them in one place. | Owner | Nothing. Read before T5 so your notes go into his sitting |
| PR-11 | One session: a blood tithe at a Harbinger, the Unstained refusal at D1, a Court custom card, a Circus curiosity card, the Conversation box with a lore card and a lesson card, the "Today" ribbon (the Alchemist's daily card sits below the first screen), and the Scout's hub. | Owner | T2 built, so clipping is judged fixed |
| RV-12 | Trade balance and rapport lore content. | Owner | PR-11 |

## Line work by speaker

Gathered from the other chunks' rows so the writing can be batched one speaker at a time. These rows are reviewed in their own chunks, not here. "Key" is the nearest key I found in `en_us.json`. "None found" means a new key, or one I did not locate. The search was by keyword, not exhaustive.

### Every core-loop teacher

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CL-3 | 05 | Shaping is renamed Sculpting. No new line uses the old word, and 17 existing NPC lines and options change. | `hemomancy.mnemonist.craft.shapings.*`, `hemomancy.dialogue.mnemonist.option.craft.shapings*`, `hemomancy.alchemist.craft.temper.line2`, `hemomancy.mnemonist.slots.shaping` |
| PT-28 | 10 | A refusal line wherever a reward can be refused, so no one is left on a "claimed" node with nothing said. Touches every speaker with a `.claimed` node. | `hemomancy.alchemist.brewing.*.claimed`, `hemomancy.alchemist.centrifuge.*.claimed`, `hemomancy.artificer.resonant_forge.*.claimed`, `hemomancy.mnemonist.loom.*.claimed`, `hemomancy.mnemonist.scriptorium.*.claimed`, `hemomancy.vicar.condenser.druse.claimed` |
| PT-53 | 14 | With Pelagic worldgen off, hide every pointer to the Court. A gate on existing lines, no new words. | `hemomancy.vicar.item_inquiry.brine_sealed_letter.*`, `hemomancy.alchemist.item_inquiry.brine_sealed_letter.line2`, `hemomancy.voyager.item_inquiry.brine_sealed_letter.*`, `hemomancy.vicar.voyager_introduction.*` |
| the T5 table | 01, 02, 03, 04, 05 | Lesson lines whose facts move with a ruling. | see T5 |

### Vicar

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-1 | 10 | Point the D1 absorption hint at reservoirs, since a creature pays 1 mL a pulse. | `hemomancy.vicar.neophyte.absorption_hint`, `.absorption_hint.fresh` |
| PT-2 | 10 | Say the 500 mL proof counts net blood, so drawing into a full vessel counts nothing. | `hemomancy.vicar.neophyte.absorption_hint`, `hemomancy.vicar.neophyte.degree_hint` |
| PT-7 | 10 | The D3 hint names the Loom weave, the D4 hint names the Vein-Mason loadout, in his voice. | `hemomancy.vicar.initiate.degree_hint`, `hemomancy.vicar.adept.degree_hint` |
| PT-39 | 10 | Where Sanguine Quintessence comes from (Vicar is the first option, Alchemist the second). | none found. Nearest: `hemomancy.vicar.covenant.found_fane`, `hemomancy.vicar.illuminatus.founding_fane_lore` |
| PT-15 | 10 | Take the Hermit Road report at any Degree. A tree gate. Check the report lines still read right for a late reporter. | `hemomancy.vicar.hermit_road.*` (pinned phrases in his sheet) |
| PT-43 | 10 | Offer the Monolith blueprint at D5 and above. A tree gate. | not located |
| PT-86 | 10 | The D5 and D6 Main steps become Quest Work cards with a sigil, one per step. Card titles may be needed. | `hemomancy.vicar.covenant.*` |
| PT-69 | 11 | Tell a lineless D6 to found a line before binding a Throne. | `hemomancy.vicar.covenant.bind_throne` |
| PT-79 | 12 | Name the Severance to a D7 who chose Silence (the owner: "a Monolith or Vicar line"). From the Vicar it must stay hearsay. | none found |
| PT-83 | 12 | A greeting tree for a D8 Silentium, who gets the D7 Archon tree today. | `hemomancy.vicar.archon.line1/.line2` |
| D-32, PT-21, PT-58 | 15 | A D3 teaser line pointing at the Circus before the D4 Waybill. The row names no speaker. The Vicar or the Mnemonist is the natural one. | none found |
| CL-14 | 02 | A living vein moves blood only while something draws from one end. | `hemomancy.vicar.item_inquiry.earthen_vein.line2` |

### Alchemist

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-78 | 12 | Name the Fungal Spine after Communion (the owner chose the Alchemist). | none found |
| PT-33 | 10 | Offer the Specimen Jar lesson in the D3 and later trees. | `hemomancy.dialogue.alchemist.option.specimen_jar_lesson` |
| PT-39 | 10 | Second choice for the Quintessence line. | none found |
| DS-5 | 17 | Advanced brewing folds into tinctures, so the advanced brewing lines change or go. | `AdvancedBrewingDialogue.java` keys, not listed |
| CR-7 | 06 | The Phlegethosoma is called by projecting 500 mL into the basin ichor. The commission lines should say so. | `PhlegethonticCommissionDialogue.java` keys, not listed |
| NQ-16-5 | 16 | Her own "later lesson" line for a held Clairaudiograph. | `hemomancy.clinical.inquiry.later` |

### Mnemonist

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-6 | 10 | Say the indexing is optional Side work: any Blood Shot weave opens the Brotherhood. | `hemomancy.mnemonist.woven_vessel.archive.line1`, `hemomancy.dialogue.event.mnemonist_woven_vessel_*` |
| PT-71 | 10 | The D6 Anchorite counsel becomes a Quest Work card with a sigil (event `anchorite_counsel`). | not located |
| CD-14 | 16 | 41 memory inquiries (T4). | new `hemomancy.mnemonist.item_inquiry.memory_<id>.line1/.line2` |
| D-32 | 15 | Possible speaker for the D3 Circus teaser. | none found |
| HN-8 | 14 | Ruled No: the Mnemonist does not hint at the Court. Nothing to write. | none |

### Artificer

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-10 | 12 | Code follows lore: the Silent Archon vestments are offered after Silentium, not at D7. The Weight of the Frame lines change with it. | `hemomancy.artificer.assignment.weight_of_frame.*` |
| CN-10 | 05 | Cleats are applied at the Armature to any Hemomancy boots, with a radial toggle. | `hemomancy.artificer.craft.climb.cleats.*`, `hemomancy.dialogue.artificer.option.craft.climb.cleats` |
| NQ-16-5 | 16 | His own "later lesson" line. | `hemomancy.clinical.inquiry.later` |

### Hermit

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-11 | 11 | Check the Illuminatus hint against what the Covenant needs. The line no longer says "Find allies who share your blood". It now says to raise a Fane or give blood at your line's Bloodwell. | `hemomancy.hermit.illuminatus.hint` |
| PT-8 | 10 | Say the heart cannot attach to awakened blood, so an awakened player is not sent to a Hermit who cannot bless them. | `hemomancy.dialogue.event.hermit_heart_offered`, system line `hemomancy.initiation.cannot_attach` |

### Vein-Mason

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-30 | 10 | A trade card that replaces a lost Scar Blank (title, line, thanks). The Scar Blank is tagged `trade/never_sold` today, which chunk 10 must settle. | new `hemomancy.trade.cicatrix_anchorite.<offer>.*` |
| CQ-44, CL-14 | 02 | Water breaks gouges and spares living veins. A vein moves blood only when drawn on. | `hemomancy.anchorite.craft.ground_marks.cut.line2`, `.vein.line2` |

### Sanguine Monolith

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| D-23 | 10 | Warn before the press that shattering loses the free Cornerstone. | none found. Nearest: `hemomancy.monolith.archon.press_again`, `hemomancy.monolith.counsel.press_further` |
| PT-79 | 12 | Name the Severance to a D7 Silence (first of the owner's two speakers). | none found |
| PT-69 | 11 | Tell a lineless D6 to found a line first. | `hemomancy.monolith.counsel.bind_throne` |
| PT-86 | 10 | Its counsel steps as Quest Work cards, matching the Vicar's. | `hemomancy.monolith.counsel.*` |

### Fungal Entity (whispers)

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-3 | 12 | Cut the unsent D8 line. | `hemomancy.whisper.spine_growth.degree8.canon` |
| PT-72 | 12 | Warn before the ninth pome that casting and rites lock until D8. The row names no speaker. A whisper is the nearest voice. | none found. Nearest: `hemomancy.whisper.pome_drop.*` |

### Voyager and Votary Wayfarer

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-1 | 14 | The Brine-Sealed Letter inquiry offers a chart option again. | `hemomancy.voyager.item_inquiry.brine_sealed_letter.line1/.line2` |
| CD-13 | 14 | Keep: the Wayfarer names the Court at D5 and above. Nothing to write. | none |

### Brined Court roles

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-47 | 14 | A confirm step before "ask to join" spends the free switch. One per house lead. | `hemomancy.court.<role>.join.line1` |
| PT-48 | 14 | A confirm node before the Champion challenge. | not located. Compare `hemomancy.court.head_tender.challenge.line2` |
| PT-52 | 14 | A D5 gate on the Champion, with a refusal line. | none found. Compare `hemomancy.court.head_tender.join.early` |
| PT-49 | 14 | A pointer to the Shared Wake's nursery from another resident. | `hemomancy.court.nursery_tender.directions.*` exist for the reverse direction |
| PT-50 | 14 | Residents name the Vent Sanctum and the outposts. | `hemomancy.court.<role>.directions.*` |
| PT-51 | 14 | Lightkeeper: reword the feeding line so it describes the real bonus. | `hemomancy.court.lightkeeper.food.line1` |
| PT-54 | 14 | Lines past D5 and about Primal, for Court speakers who have none. | none found |
| CD-4 | 14 | Braced elder: cut "In time" so it does not read as permanent rooting. | `hemomancy.court.braced_elder.item_inquiry.morphling_holdfast.line1` |
| HN-6, HN-5 | 14 | Deferred: house greetings and Court lines in a house voice, and the lore round. Nothing to write yet. | none |

### Ringmaster

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-61 | 15 | Separate, accurate lines for "claimed by another" and "not ready". | `hemomancy.circus.finale.claimed`, `hemomancy.circus.finale.not_ready` |
| D-30 | 15 | The pact and finale need D4 and one school report. A refusal line that names both. | none found |
| D-36, PT-62 | 15 | A Succession yield scene. Lines if the scene speaks. | none found |

### Crimson Troupe faculty

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-7 | 16 | Per-role status, aftermath and preparation lines (T3). | `hemomancy.circus.school.status.*`, `.aftermath.*`, `.preparation` |
| CD-8 | 15 | Delete the dead barks. | `hemomancy.dialogue.circus_<role>.line1` to `.line3` |
| PT-59 | 15 | A Peaceful refusal line for practicals, after the recheck. | none found |
| PT-60 | 15 | The Threadkeeper gets an option to its lesson node. | not located |
| PT-63 | 15 | End a stalled two-threat practical with the retry line. | `hemomancy.circus.school.retry` |
| D-31 | 15 | The Unstained in the Circus: Liberation only. A line for a professor who will not teach them. | none found |
| D-44 | 15 | Site gifts come through NPCs. Lines if a professor hands one over. | none found |
| D-45 | 15 | Fix the guest pay ledger line. | `hemomancy.circus.school.ledger.guest.available`, `.pending` |

### Zealot, Acolyte, Guardian

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-7 | 16 | Each speaker's own Observance lines: offered, carried, ready (T3). | `hemomancy.dialogue.unstained.observance.available/.active/.ready` |

### Successors and recruits

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-9 | 10 | A consent prompt for the recruit at a founder-led reception. | none found. Compare `hemomancy.dialogue.recruit.release.prompt` |
| PT-35 | 11 | The leader confirms a signed ledger join. A prompt on the leader's side. | none found |
| CD-6 | 16 | Parked: their own trees. | none |

### Annetta Knowles

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| CD-5 | 16 | The player says the truth. Her node keeps the stage direction and her answer (T1). | `hemomancy.annetta.draught.truth.line1`, `hemomancy.dialogue.annetta.option.what_is_it` |

### Saints

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| SM-3 | 08 | The owed Chapel memory text: Hemorath's students sealing the vessel, Seraphae's "four touches, four growths". Memory text, written to the two Saint sheets. | not located |
| RV-20 | 08 | Keep the two voice sheets until each fight is played. Nothing to write. | none |

### Lines with no speaker (system or narration)

| Row | Chunk | What the line must say | Existing key |
| --- | --- | --- | --- |
| PT-26 | 10 | A line on waking for a player who took the charm off with Concentrated Blood pending. The owner's answer repeats the question, so chunk 10 must confirm it. | none found |
| PT-38 | 11 | Warn at activation that the first Unbinding only warns and still burns its Nether Star. | not located |
| PT-32 | 13 | Show the Degree requirement when a D4 stands on a D5 floor. | not located |
| PT-84 | 07 | The rematch line stays. The ruling re-arms the retry instead of rewording. | not located |
| PT-37 | 11 | The owner replaced the refusal line with the ledger crumbling to ash. A short action-bar line may still be wanted. | none found |
