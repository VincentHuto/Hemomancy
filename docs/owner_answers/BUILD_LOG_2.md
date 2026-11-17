# Build log 2: the answered work

This run builds the work the owner answered. He answered every question in `docs/OPEN_QUESTIONS.md` on 2026-11-14
and objected to no default in its section 2. Each package has a restore point at `refs/build2/<id>-before`, made
before it starts. Nothing is committed. The baseline step below made no restore point.

## Report

Final check on 2026-11-16. 37 packages ran (B01 to B37). All 37 were verified, none was reverted, and four needed a
second verification round (B14, B19, B21, B29). The tree is green. Unit suite: 6,200 tests, 0 failures. Combined GameTest run, twice: all 2,013 required passed both times. Playthrough: all 440 required passed, 17 optional failures, none outside the baseline.

### Final check runs

Run one at a time on 2026-11-16, from about 05:00, with no other Gradle task, server or client running. None needed a retry.

| Suite | Result | Output |
| --- | --- | --- |
| JVM unit suite | `BUILD SUCCESSFUL in 1m 45s`. 6,200 tests in 1,652 result files: 0 failures, 0 errors, 0 skipped. Baseline: 6,031. | `build/build2/FINAL-unit.txt` |
| Combined GameTest run 1 | "2013 GAME TESTS COMPLETE", "All 2013 required tests passed :)". Baseline: 1,917. | `build/build2/FINAL-gametest1.txt`, `FINAL-gametest1-latest.log` |
| Combined GameTest run 2 | "All 2013 required tests passed :)". No flake in either run. | `build/build2/FINAL-gametest2.txt`, `FINAL-gametest2-latest.log` |
| Playthrough suite | "440 GAME TESTS COMPLETE", "All 440 required tests passed :)", "17 optional tests failed". Baseline: 432 required, 59 optional failures. | `build/build2/FINAL-playthrough.txt`, `FINAL-playthrough-latest.log`, `FINAL-playthrough-optional.txt` |

New failures, not in the baseline: none. All 17 optional failures are baseline names, each still a `NOT RUN` probe or a
decision that waits on an open row: `chamberreturn`, `courtleadlostforgood`, `firstprojectioninthegardens`,
`gardenstravel`, `guidedchamberlogoutandlethaldamage`, `guidedchamberrestoreseverything`, `heartnoderespawntrap`,
`lostblankbeforecarve` (PT-30), `notrunnamesthemissingchamber`, `recollectioncystcreativefullpack`,
`relaychartfindsagarden`, `summonstripstheoutpost` (PT-85), `uniquenpclossisrecoverable` and `uniquenpclosslate`
(PT-10), `vespermemorystashedinachest`, `vesperordeal`, `waybilllocatesacamp`. The PT-88 flake
`apotheosritewithoutrescue` passed, as it has since B25. The 42 baseline names that left the list were made required
or passing by their packages. The opt-in clinical and succession suites were not rerun here; B21's verification ran
the clinical suite green (132 of 132).

### Packages

| Package | Status | Rows built | Rows skipped, and why |
| --- | --- | --- | --- |
| B01 The Sculpting rename | verified, round 1 | 05 T1 (CL-3) with NQ-05-1 A; NQ-05-3 to NQ-05-6; NQ-16-6 and 16 T5 (CN-5) the Sculpting word | "Its own commit": no commits in this build (`refs/build2/B01-before` marks the tree before it). No client: words only. |
| B02 Architecture and test housekeeping | verified, round 1 | 17 T8 (FX-24, NQ-17-5); 17 T7 (FX-14, rite sync and Chamber); 17 T5 (G-7b); 17 T3 (S-5b); 17 T11 (F-16) and its follow-up; 17 flakes; the clinical suite; the GeckoLib jar | `afalteringwillstilltakestheprojection`: cause not proven, kept as a watch. `EarlyInitiation.give` kept: playthrough fixtures still use it. |
| B03 Combat refactor | verified, round 1 | 17 T12 (F-17, NQ-17-3 A); 17 T13 (G-1, F-18); NQ-17-1 A; NQ-17-2 A; the Absolved bonus characterisation | Ink Mantle and Cryptobiosis stay on `LivingDamageEvent.Post`: moving them means rewriting both Morphling items; NQ-17-1 still holds for them. |
| B04 Cast economy and Temper | verified, round 1 | 04 T1 to T10 (LP-7, LP-9, CB-10, LP-14, CQ-9, CB-1, LP-16, CQ-8, CB-20, CB-11, the dome, PT-25, CQ-17, records) with NQ-04-1 to NQ-04-15 | 04 T5c went to B05. Core-loop track files held (CQ-14). |
| B05 The R cast price sheet | verified, round 1 | 04-T5-sheet; 04 T5c (LP-12, NQ-04-6 B) | The dated audit CSV was not regenerated: audits are snapshots. |
| B06 Living blood, part one | verified, round 1 | 01 T2 to T9 and T11 step 1 (CB-13, CB-14, CB-17, LP-5, LP-8, FX-1, CN-12, FX-2, CQ-3 to CQ-5, CB-18, CN-9, CL-6, CB-28) with NQ-01-9 to NQ-01-20 | 01 T10 and T11 steps 2 and 3 went to B07 and B08. |
| B07 Living blood: clot and revive | verified, round 1 | 01 T10 (CQ-6, LP-3, LP-4) with NQ-01-2 to NQ-01-8; config, Gauge, bar, Liber, Alchemist | The Resonant Forge's cancelled-operation refund still fills at age 0: not in the row. |
| B08 The Formation recipe pass | verified, round 1 | 01 T11 steps 2 and 3 (CB-28, NQ-01-20); teaching text for the new blank | none |
| B09 Ground network and farming | verified, round 1 | 02 T1, T2, T4 to T8 (CQ-7, RV-46, CQ-43, CQ-15, CL-14, CQ-44, CB-26, CB-3) with NQ-02-1 to NQ-02-9 | Core-loop track files held (CQ-14). |
| B10 Menagerie, firsts, strand skills | verified, round 1 | 03 T4 to T6 (CL-12, CR-26, CQ-47) with NQ-03-1 to NQ-03-5; 05 T2 to T4, T7, T8 (CQ-13, CQ-14, CQ-20 B, CQ-22 A); UN-5, UN-54, UN-55, UN-59, UN-65; the lent batch | 03 T2 Along the Red: held. `StrandSkills.QUICKEN_PER_LEVEL` and the brand set ticks: deleting them is the held dead-kits row. |
| B11 Hematic Cleats as an Armature fitting | verified, round 1 | 05 T6 (CN-10) with NQ-05-7 A to NQ-05-12 | "Feet bowl" read loosely: Armature bowls are not slot-bound, so cleats work from any bowl. The Resonant Forge keeping the mark was not checked. |
| B12 Lash follow-ups, HUD, Bandolier | verified, round 1 | UN-8, UN-56 (12 minors), UN-43 harness, UN-42 captures, UN-62, UN-64, UN-34 and UN-35 scenes, CQ-63 A | UN-41 write-back: held. The played two-client session is yours. |
| B13 Creatures | verified, round 1 | 06 T1, T2 rest, T6 to T11 (CR-1, CR-10, CR-23 to CR-25, CR-4, CR-5, CR-2, CR-14) with NQ-06-1 to NQ-06-15; C-5 mooshroom; P06 cleanup | 06 T12 and T13 went to B14 and B36. |
| B14 The Phlegethosoma summon | verified, round 2 | 06 T12 (CR-7) natural spawn and summon with NQ-06-7, NQ-06-8, NQ-06-10 | No review client: no fixture stages a Nether Basin; the looks reuse existing effects. |
| B15 Bosses | verified, round 1 | CR-16 (NQ-07-1); 07 T1 rest (NQ-07-3, D-33 boss tag); NQ-07-2; 07 T2 test; 07 T6 (PT-84, NQ-07-4); 07 T7 (PR-9) | 07 T4 live Vesper victory test: the GameTest server loads no Chamber of Will. The AGENTS.md boss helper line: not in the rows. |
| B16 Saints | verified, round 1 | 08 T2 to T6 and T8 (FX-18 flake, SM-1, SM-4, SM-3, SM-13 A) with NQ-08-18 to NQ-08-24 | No client: the Downfall fragments change which mob rises, not art. |
| B17 Saint gifts, part one | verified, round 1 | 08 D-1 spec refresh; NQ-08-1 A to NQ-08-3, NQ-08-14, NQ-08-17; never_sold widening; Crimson Tithe and the Unclosing Eye (NQ-08-5 to NQ-08-7, NQ-08-12); review client | Bloom and Hour went to B18. Heaved brick slabs, per-Saint impact geometry and the tissue furrow shader were not built; no new sound files. |
| B18 Saint gifts, part two | verified, round 1 | Bloom of Rot (NQ-08-8), Endless Hour (NQ-08-9, NQ-08-10), NQ-08-5, NQ-08-11, old visual forms removed, NQ-08-14 tree | Bespoke cast styles and per-gift impact geometry: no row names them; the shared impact is used. |
| B19 Visual engineering | verified, round 2 | 09 T9 (A-4, NQ-09-3); 09 T10 (VX-4, VX-5, NQ-09-1); 09 T1, T3 to T6 tests and scenes (FX-29); 09 T7 in part (VX-17, PR-7, NQ-09-6); the VX-1 row | The 43 A rows cast one by one need three Gradle runs at once. Six scenes the review cannot cheaply stage (Circus overlay, Curved Horn, Thurible coal, Elytra, Archon negation, Sickle cut). |
| B20 Texel batches T1 and T3 | verified, round 1 | 09 T12 (VX-10) script, UV rescale, post-audit models; 09 T14 (VX-12) fittings, generators, set-piece items; review client | Marionette Crossbar: a T4 job, left with 09 T15. Hydra Crown: already right. Rite Fragment Inscription: cropping would cut its picture. |
| B21 D0 to D6 journey | verified, round 2 | PT-19, PT-3, PT-8, PT-9, PT-2, PT-15, PT-1b, PT-11, PT-18, PT-26, PT-7, PT-6, PT-4, PT-14, PT-22, PT-29, PT-28, PT-41, PT-5, PT-27, PT-86, PT-23 with their NQ-10 answers | PT-1 the blood font: no block exists (now `OPEN_QUESTIONS.md` 1.3). PT-24: no Chamber-loaded launcher. PT-30: built and put back (1.3). PT-10 and the vacancy store: size L, waits on `courtLeadLostForGood`. PT-85: held. |
| B22 Bloodlines and the Covenant Throne | verified, round 1 | 11 T1 to T4, T6 to T12 (PT-36, PT-90, PT-37, PT-44, PT-64, PT-65, PT-70, PT-69, PT-35, PT-42, PT-87, PT-66) with NQ-11-1 B to NQ-11-14 | none |
| B23 D7 to D8 endgame | verified, round 1 | PT-74 (NQ-12-1 A), PT-77 (NQ-12-2 A, V-12-1), PT-31 (NQ-10-3 A), 12 T8 (PT-80, NQ-12-4 A, V-12-4), FX-16, NQ-12-5, V-12-2, V-12-3, V-12-5, V-12-6, the C-2 reversal recorded, the Archon flake, three chain tests | The in-Gardens walk home is not filmed: no Gardens in the GameTest server and no leave scene. |
| B24 Silentium greeting trees | verified, round 1 | 12 T11 (PT-83); the Hermit's line (UN-19 A) | none |
| B25 Rites and Working crafts | verified, round 1 | NQ-13-1 to NQ-13-5 (CB-12, F-12, CN-2, FX-12, DS-4); FX-11 tints and editor preview; PT-32; UN-48 and PT-88; PT-67; FX-13; RV-22 | 13 T14: held until a play session. |
| B26 The Brined Court | verified, round 1 | 14 T1, T7a to T7f (PT-46, PT-51, PT-50, PT-49, PT-54, PT-48) with NQ-14-6 A, NQ-14-7; 14 T10b (FX-19 flush, FX-19a); 14 T12 in part; NQ-14-2 to NQ-14-5 lore | 14 T12 rest: waited on the strain look fixes (built in B36) and needs new Court harness scenes. |
| B27 Crimson Troupe: pact, acts, Pattern | verified, round 1 | 15 T2 to T6, T16 to T18 (D-30, D-31, HN-9, PT-59, PT-58, D-34, FX-26, D-35, D-37 to D-39) with NQ-15-5 to NQ-15-9; audit E leftovers | The Ring is drawn as server-sent thread tendrils, not a renderer pulse. The Acrobat tuck and carousel motes were not caught on film. |
| B28 Crimson Troupe: site kit | verified, round 1 | 15 T7 (D-42, D-43, NQ-15-1 A): 37 set-piece blocks, four records, model and site generators, tests, review scene | `pavilion.py`, `camps.py`, `stage.py` belong to T8 and T9. Old builders move only when the jigsaw call (15 T8) is settled. |
| B29 Crimson Troupe: pavilion | verified, round 2 | 15 T8 (D-42, D-29, PT-57); 15 T12 (D-29, FX-28) | Jigsaw split: kept one template (now a "your call" row). |
| B30 Crimson Troupe: camps, stage, outcomes | verified, round 1 | 15 T9 to T11 (D-42 to D-44, NQ-15-1 A, NQ-15-2, NQ-15-10 gifts A, survivors A, tenancy) | Foundation skirt inside the pavilion: would shift every tested coordinate. The 1,500-block aim held on one seed of three (section 4 "B30 placement"). |
| B31 Crimson Troupe: the Ringmaster | verified, round 1 | 15 T13 to T15, T19 (D-18, D-33, D-34, D-36) with NQ-15-3 A, NQ-15-4 A, NQ-15-11 | `circus_carousel` sound ids renamed `impaled_procession` (retired name). No music candidate: the event is empty. Sleight doubles keep his facing. |
| B32 Dialogue | verified, round 1 | 16 T2 leftovers (FX-15); 16 T3 (CD-7, NQ-16-5); 16 T5 (CN-5, UN-9, UN-10, UN-69); line work by speaker | Stain branch: held. Vicar font hint: no font. Alchemist DS-5: parked. Vein-Mason PT-30: blocked. Faculty D-31: no ruling (now 1.4). |
| B33 Mnemonist inquiries | verified, round 1 | 16 T4 (CD-14, NQ-16-4) and its test | none |
| B34 Liber pages | verified, round 1 | 16 T6 (DS-9): Armature, crafts, Thurible, bosses; NQ-16-3 A Court pages | none |
| B35 Sound wiring and credits | verified, round 1 | Sound S1 (UN-12, droppable doubles, UN-16, UN-17); Sound S2 (UN-70, UN-18); the UN-11, UN-13, UN-14, UN-18 rulings | UN-13's struck iron and the other stacked cues need new audio (S3, S5). |
| B36 Art by generator | verified, round 1 | 06 T13 (VX-16) Hemojelly, Myelin Borer, effect icons; 14 T10a (FX-19 icons); 14 T9 (HN-4) | Polyp models, first-person arms, 09 T13 and 09 T15: the package ran long; all still in section 3. |
| B37 Housekeeping and docs drift | verified, round 1 | UN-71 (legacy `main()` tests, private helper ratchet, `getPersistentData` ratchet); 16 T7 (FX-21) ready parts, tables, section G | The 20 player root keys (S-1): gameplay state work. Section G owner's calls (now 1.4), code findings and small leftovers (now section 3). |

### Read these

Every new or changed lang key, by package. The exact English, the speaker sheet each line follows and the removed
keys are in each package's "For the owner" below.

| Package | Keys |
| --- | --- |
| B01 | Renamed: the 37 `hemomancy.manipulation.<id>.sculpting.project` / `.absorb`, `advancements.hemomancy.sculpted.*`, `blood_source.hemomancy.sculpting`, `hemomancy.jei.blood_infusion.sculpting.tip`, `hemomancy.jei.brazier_transmutation.sculpting.tip`, `hemomancy.mnemonist.slots.sculpting`, `hemomancy.dialogue.mnemonist.option.craft.sculptings` (and `.first`, `.work`, `.mend`, `.draw`), `hemomancy.mnemonist.craft.sculptings.*`, `hud.hemomancy.verb_channel.sculpting`, `message.hemomancy.temper.dry_sculpting`, `tooltip.hemomancy.blood_memory.sculpting`. Changed: `blood_projection.desc`, `blood_absorption.desc`, `alchemist.craft.temper.line2`, `vicar.craft.verbs.order.line1/2`; Liber "Sculptings of the Ground", "Mining Sculptings". |
| B02, B03, B05, B13, B15, B19, B20, B29, B36 | none |
| B04 | New: `message.hemomancy.crimson_tithe_manip.the_debt_is_*` (three, since retired by B17), `message.hemomancy.blood_binding.boss_slipped`, `.boss_spent`, `blood_source.hemomancy.talaromyces_vein`. Changed: `crimson_tithe.desc`, `skill_sated_siphon.desc`, the Talaromyces Minus tooltip. |
| B06 | New: `message.hemomancy.blood_drunkenness.gave_less`, `tooltip.hemomancy.bloody_flask.repeated`, `message.hemomancy.brazier_transmutation.needs_catalysts`, `hemomancy.jei.brazier_transmutation.catalysts.tip`, `message.hemomancy.living_blood.feed.drawing.kin`, `screen.hemomancy.blood_volume_bar_widget.drawing.kin`, `blood_source.hemomancy.blood_routing_kin`. Changed: `alchemist.enzyme_uses.culture`, `material.hemomancy.mycelial_lantern.desc`, `alchemist.craft.fed.drip.line1`; Liber `brazier_transmutation`, `fed_ground`. |
| B07 | New: `message.hemomancy.living_blood.gauge.revive`, `screen.hemomancy.blood_volume_bar_widget.revive`. Changed: `living_blood.feed.drawing`, `alchemist.craft.living_blood.line2`, `.keepers.line1/2`. |
| B08 | Changed: `hemomancy.mnemonist.woven_vessel.recipe.line1/2`. |
| B09 | New: `blood_source.hemomancy.ground_feeder`. Changed: Artificer `craft.hot_runs.heat.line1`, `.feeder.line1/2`, `item_inquiry.catalyst_feeder.line2`; Anchorite `craft.ground_marks.cut.line2`, `.vein.line2`; Vicar `item_inquiry.earthen_vein.line2`, `craft.day.meats.line2`; Mnemonist `craft.sculptings.mend.line2`; `jei.gouge_catalyst.hot`, `.projection.tip`; `furnace_veins.sculpting.project`. |
| B10 | New: `message.hemomancy.absorption.unread_weak`. Changed: `menagerie.readout.unread`, `bestiary.hemomancy.status.met`, `bestiary.hemomancy.creature.unread`, `alchemist.craft.humours.line1`, the five strand skill `.desc` keys; seven Liber school pages, `strand_forms`, `second_strands`, `unread_humours`. |
| B11 | New: `block.hemomancy.hematic_armature.cleats_fitted`, `.fitting_cleats`, `hemomancy.jei.armature.slot.cleats`, `message.hemomancy.cleats.on/off`, `screen.hemomancy.radial_choose_manip.cleats` (and `.on`, `.off`), `tooltip.hemomancy.cleats_fitted.on/off`. Changed: `tooltip.hemomancy.hematic_cleats`, Artificer `item_inquiry.hematic_cleats.line1/2`, `craft.climb.cleats.line1/2`; Liber `venous_cling`. |
| B12 | New: `hemomancy.dialogue.event.alchemist_bandolier_given`, `gui.hemomancy.lash_tuning.*` (tuning client only). Changed: `alchemist.craft.temper.catalysts.line2`, `gloam_laceration.sculpting.project`. |
| B14 | New: `hemomancy.alchemist.phlegethontic_commission.phlegethosoma`, `message.hemomancy.phlegethosoma.summon.answered`, `.narrow`, `.occupied`, `.peaceful`, `.yours`, `blood_source.hemomancy.phlegethosoma_summon`; Liber page `phlegethosoma`. |
| B16 | New: `message.hemomancy.chapel.beholding.velorum.1` to `.4` and `.done`. |
| B17 | New: `tooltip.hemomancy.blood_memory.canon_source.*` (four), `.canon_trial`, `message.hemomancy.memory_brazier_rite.trial_not_ended`, `message.hemomancy.somatic_loom_block_entity.requires_trial`, `blood_source.hemomancy.crimson_tithe_open/_taken/_burst`, `message.hemomancy.crimson_tithe.open_cost/.open/.overfill`, `message.hemomancy.unclosing_eye.open/.lapse/.lapse_no_lux`. Changed: `crimson_tithe.desc`, `unclosing_eye.desc`. Twelve removed (stored). |
| B18 | New: `message.hemomancy.bloom_of_rot.sown`, `.not_kindled`, `.ignition`, `.reclaimed`, `.theirs`; `message.hemomancy.endless_hour.planted`, `.stepped`, `.set`, `.bow`, `.bowing`; `blood_source.hemomancy.endless_hour_hold`. Changed: `bloom_of_rot.desc`, `endless_hour.desc`. Ten removed (stored). |
| B21 | New: `vicar.option.masons_respite_map_again`, `vicar.masons_respite.reissue`, `event.vicar_scrap_carried`, `anchorite.d5.cause_strain.undegraded`, `alchemist.option.clear_assigned_spin`, `alchemist.first_separation.spin_cleared`, `vial_centrifuge.assigned_spin_pending`, `initiation.blood_unsettled_charm`, `initiation.founder_offer`, `.founder_waits`, `hermit.heart_taken.line1/2`, `hermit.stranger.line1/2`, `event.hermit_heart_spent`, `dialogue.refused.too_far`, `.not_now`. Changed: `vicar.adept.degree_hint`, `vicar.blood_crafting.absorption`, `vicar.neophyte.absorption_hint`, `vicar.covenant.complete_vigil`, `anchorite.d5.cause_strain`, `alchemist.phlegethontic_commission.task`, `.recorded`, `deep_dark_commission.recorded`, the phlegethontic ledger step, `initiation.blood_rest`. |
| B22 | New: about 45 keys under `block.hemomancy.covenant_throne.*`, `hemomancy.bloodline.*` (default name, leave, petition, settle, share), `hemomancy.ledger.crumbled`, `message.hemomancy.unsigned_ledger_item.*`, `screen.hemomancy.ledger.*`, the Vicar's `leave_bloodline` and `line_has_no_fane` lines and the Monolith's `counsel.line_has_no_fane`. Changed: `vicar.covenant.found_fane`, `vicar.covenant.bind_throne`, `monolith.counsel.bind_throne`. Removed: `covenant_throne.not_progenitor`. |
| B23 | New: `screen.hemomancy.heart_node.leave` and `.tooltip`, `item.hemomancy.sporitic_thurible.message.apotheos`, `message.hemomancy.cardinal_rite_activation.sporitic_thurible_apotheos`, `alchemist.item_inquiry.bog_body.line1/2`. Removed: two. |
| B24 | New: 22 keys: `vicar.silentium.*`, `alchemist.silentium.*`, `mnemonist.silentium.*`, `artificer.silentium.*`, `artificer.silent_vestments.line1` to `3`, their four option keys, and `hermit.silentium.line2`. |
| B25 | No keys; Liber text added to seven troupe pages, the new page "Blood Gourds", The Historical Record, Mnemonic Ambergris, Bloodwork Stations, The Source, The True Purpose. |
| B26 | New (17): three `court.<role>.degree.late`, nine `court.<role>.strain.primal`, `dialogue.court.option.primal`, `court.lightkeeper.primal.line1/2`, `court.head_tender.primal.line1/2`. Changed (44): the `court.<role>.directions.*` lines, the 17 lesson lines, `head_tender.challenge.line1`. |
| B27 | New: `circus.ringmaster.gate.*` (five), `summon.act.<act>` and `.desc` (eight acts), `summon.act.cooldown`, `.no_body`, `.no_thread`, `.nothing`, `.performed`, `screen.hemomancy.crossbar_radial.act.desc`. Changed: the Waybill's `attuned` and `not_found`, `summon.marrow_spitter.lore` and `.role`, `summon.ringmaster_pattern.lore`, `puppeteers_spindle.ringmaster_partner`. |
| B28 | New (37): every `block.hemomancy.circus_*` set piece. Changed (4): the four records' names. |
| B30 | New: `message.hemomancy.circus.school.gift`, `circus.ringmaster.tenancy`, `circus.tenancy.banner`. |
| B31 | New: `circus.ringmaster.yield`, `.says`, `circus.finale.leash`, `.spotlight`, `.bar.troupe`, `.bar.anchors`, `boss.hemomancy.circus_yield.*`, `boss.hemomancy.circus_collapse.*`, seven subtitles. |
| B32 | New: the Acolyte, Zealot and Guardian observance keys, the 84 `circus.school.<role>.*` professor lines, `clinical.inquiry.alchemist_later`, `.artificer_later`, `.mnemonist_later`, `dialogue.topic.craft.summary`. Changed: corrected Artificer, Mnemonist, Alchemist and Vicar lesson lines, the two recruit accept lines, eleven Court newcomer lines. Removed (stored): the shared Unstained and Circus keys. |
| B33 | New: 130 lines, `mnemonist.item_inquiry.memory_<id>.line1/2` for 65 memories. Removed (stored): `memory_general.line1/2`. |
| B34 | No keys; 26 new Liber pages (Armature chapter, 17 crafts, three bosses, the Court chapter). |
| B35 | New: `subtitle.hemomancy.manipulation.cast`. Renamed: `subtitle.hemomancy.entity.mycophant.teleport` to `.split` ("The Mycophant splits"). |
| B37 | Changed: `hemomancy.rite.hematic_unbinding.desc`, `vicar.adept.brotherhood_lore`, `lady.discerning.v0.follow`, `advancements.hemomancy.vesper_defeated.description`, `dialogue.event.vicar_hermit_road_known`. |

Flagged by the builders for a second read: the stranger's past-the-Seed counsel (B23), "Silentium" said twice in a
row by the Vicar, Alchemist and Artificer endings (B24), the Lightkeeper's `strain.own` and `strain.other` ("grow
faster in this water", B26), and the four Canon Memory inquiries (B33).

### Look at these

| Package | What | Where |
| --- | --- | --- |
| B09 | Water over a lit vein; the hot run | `build/build2/B09-shots/ground-15-vein-water-60.png`, `ground-11-furnaces-50.png` |
| B10 | Stall pip before and after; the Will forge; the Specimen Vat screen | `build/build2/B10-pip-old.png`, `B10-pip-new.png`; `build/visual-fixes-client-review/screenshots/visual-16-forges-8.png`; `build/screen-chrome-review/screenshots/chrome-after-specimen_vessel.png` |
| B11 | Cleats spurs on the stand and on worn Blood Lust boots; the radial slice | `build/build2/B11-shots/after/`, `build/build2/B11-verify-shots/` |
| B12 | Vigils and joined vats; the UN-42 lash cases; the onlooker frames (the Eclipse dome reads as black ribs) | `build/build2/B12-shots/menagerie/`, `strand/`, `onlooker/` |
| B15 | Boss bars, openings, phase cards, the Mycophant capture | `build/build2/B15-shots/index.txt` |
| B17 | Crimson Tithe and the Unclosing Eye | `build/build2/B17-review-round2/` |
| B18 | Bloom of Rot and the Endless Hour | `build/build2/B18-review/` |
| B19 | Held forms and lashes; the onlooker sheet; the new shader shapes | `build/build2/B19-review/strand-held-forms-sheet.png`, `onlooker-sheet.png`, `vx4-shapes-sheet.png` |
| B20 | Texel batch T1 and T3 sheets | `build/build2/B20-review/texel-sheet-1-items.png`, `-2-staffs.png`, `-3-blocks.png` |
| B22 | The petition row; the greyed Donate | `build/build2/B22-review/chrome-b22-ledger_petitions.png`, `chrome-b22-bloodline_pool_full.png` |
| B23 | The Leave the Gardens button | `build/build2/B23-review/chrome-b23-heart_node.png` |
| B25 | The Rites and Crafts tints; the Chamber visit | `build/build2/B25-shots/liber-rites-and-crafts.png`, `chamber-rite-visit.png` |
| B26 | The school's sail flush at night | `build/court-npc-review/screenshots/school/08-close_day.png` to `10-close_night.png` |
| B27 | The Circus dialogue theme and crest | `build/build2/B27-shots/npc-trade-27-ringmaster-hub.png` to `-33-`, `build/build2/B27-dialogue-sheet.png` |
| B28 | The 37-piece site kit | `build/build2/B28-shots2/kit-sheet.png`, `build/model-previews/circus_*` |
| B29 | The rebuilt pavilion and its perception stages | `build/build2/B29-shots/`, `build/build2/B29-verify2-shots/`, cutaway `build/circus-kit/crimson_troupe_pavilion.png` |
| B30 | Camps, the stage, the ruin, the banner (the review's night and ruin frames show sky only; see B30 verification) | `build/build2/B30-shots/sites-sheet.png`, `build/circus-kit/troupe_camp_*.png` |
| B31 | The Ringmaster finale, yield and collapse | `build/build2/B31-shots/`, `build/build2/B31-v1-shots/` |
| B34 | The 26 new Liber pages | `build/build2/B34-shots/`, `build/build2/B34-verify1-shots/` |
| B36 | Hemojelly, Myelin Borer, the 15 new effect icons, the strain fixes | `build/build2/B36-review/` |

Not seen in any client: the Phlegethosoma's pour, rise and dive (B14), Seraphae's Downfall fragments in motion (B16),
the Gardens walk home (B23), the kicked ledger's crumble (B22), the Circus acts and the carousel motes (B27), the
title wrap at GUI scale 3 (B32). Each has a row in `OPEN_QUESTIONS.md` section 4.

### Values chosen at build time

Every number or call a builder picked where the answers left room. Each is in its package's "For the owner".

- **B02:** rite view range 128 blocks; the GameTest origin folded into 1,000,000 blocks of 0.
- **B03:** the order inside each damage stage (Sanguine Fists before the Penumbral x1.5); "what got through" read as the damage that lands, capped at what the body held, which also lowers the wound blood of an unsaved death.
- **B04:** the Talaromyces vein source counts as spend and is not reserve-protected; Sated Siphon fills the gourd to its tier volume; the Tithe debt collected every 20 ticks; a Ferric construct counts as its maker's for mending; avatar upkeep read as mL a second.
- **B05:** Funeral Bell read at 7 a body (430); Blood Aneurysm as a bolt (260); Grave Debt, Umbral Rend and Eclipse Well as area casts; conditional bonuses not counted; prices rounded to 5 mL.
- **B06:** a distilled Flask rounds to the nearest vial; the kin stand-in draws 8 mL a tick with no bonus; `catalyst_count` capped at 64; the Lantern keeps its 1,200 mL buffer.
- **B07:** blood that does not fit as fresh goes to the clot; a Thrall's carried blood keeps its age; a rite-held station reads by its own freshness.
- **B08:** a one-off D1 to D3 craft costs at most 2 Formations; the blank drops to 4; the Vitality Chalice ties at 2.
- **B09:** draw window 100 ticks; a machine draws to 600 mL; the four nearest furnace blocks count; falling water over a vein has its own value (9).
- **B10:** the unread action-bar line about once a second; Whipcrack lengthens Ferric Spikes' standing blades; Long Strand scales Hematic Flare's beam and Glacial Grasp's leash; the Will forge back to 1.0; the lending batch `sculpting_lent`.
- **B11:** the cleats toggle paced to one per 10 ticks; cleats work from any bowl; the layer draws on armour stands and humanoid mobs.
- **B12:** the Bandolier recipe (2 leather, 1 Sinew Cord, 3 glass bottles); the gift on the first branch of the Temper lesson; the HUD clock wraps hourly; the front glass at depth 200.
- **B13:** sent Will health floor 60; the Gardens roster also applies to chunk-generation spawns; the four small drops need a player kill; the polar bear needs no Living Syringe (your "so do polar bears", flagged).
- **B14:** pours within 3.5 blocks share one count; the dive lasts 40 ticks; the 7 by 7 habitat check; a refusal costs nothing; the Liber page at D5.
- **B15:** the rematch flag saved only while set; a re-armed rematch skips the hunt's whispers.
- **B16:** the Chapel fragment strikes 3 every 20 ticks and chases within 24 blocks at 0.12 a tick, with Seraphae's damage type.
- **B17:** the chain haul 0.033 a tick; "full" for the overfill is 95%; the Eye's sneak slow -70%; pulses ignore the hurt cooldown.
- **B18:** colonisation falls 1 each 40 ticks off a field; stillness out of an Hour 1.5 a tick; the rim's Obscured 20 ticks, refreshed each 10; a Saint answers within 48 blocks (the Tithe answer skips this check; see B18 verification).
- **B19:** burst speeds and vertical factors; Thrall lift 0.04, Drudge lift 0.03; the seed salt mix; shape ids 12 to 15.
- **B20:** decal threshold 4 units; Sarcophagus item scale 0.75 and offset; Pylon display scales halved.
- **B21:** the Athanor spot (548, 739); temple reach 16 blocks for relinking, 32 for the living Hermit; the founder offer repeats once a second; the Effigy falls back to the old drop when the preparer is offline.
- **B22:** the crumble sound (sand break, volume 0.9, pitch 0.7) and 14 ash motes (78, 66, 62); a 5-tick limit on petition answers; the ledger panel 190 to 244 px; the petition list capped at 64 names.
- **B23:** the leave payload limit 20 ticks; the button 100 x 14 left of Molt; the Thurible at the end of Blood Tools; a non-Apotheos loses a node at login too.
- **B24:** the Vicar and Alchemist get whole new trees; the Mnemonist and Artificer one new card each.
- **B25:** tints bone `0xFFC8BC9C` (Floorless) and hematic iron `0xFF8C9DB8` (Making); Possession on The Source, Visitation on The True Purpose.
- **B26:** the late greeting from the sixth Degree; the Primal topic from Apex; the flush alpha 0.55 plus 0.3 on a slow sine.
- **B27:** every act's thread cost, cooldown and reach (`docs/CIRCUS.md`); Ring 30 thread and 60 s; the Juggler pin 30 ticks; a 5-tick request interval; the Circus palette and crest; the one-word acts' effects.
- **B28:** lights (footlight 12, ember pit 13, cook pot 9, hound hoop 7); the net's 20% fall damage; every shape, colour and name.
- **B29:** the whole pavilion layout; deck heights 5 and 7; the box at (32, 7, 44); three wagons; 30 lantern spots; cue range 28 and rescan 2 s; cloth drop 2.2 blocks.
- **B30:** camp 27 x 12 x 27, stage 17 x 10 x 15; gate grid 8, slope 6, pavilion inset 11, village distance and exclusion 6 chunks; the seven frame materials; the ruin's tears and debris rates.
- **B31:** every move number; arena leash 24 and grace 200; yield 60, collapse 40, a 30-tick pause before he vaults home; all candidates play as variants until you pick.
- **B32:** the lesson card summary line; the recruit accept line spoken after the pledge.
- **B34:** chapter places (Armature 11, Court 4); the Court's sea-blue colours; every icon; the rack page from D2; the Dried Gourd page at D1; the `ENCOUNTER` discovery source.
- **B35:** the termite knock (`block.wood.hit`, 0.3, pitch 1.3 to 1.6, every 20 ticks); the death file cut to 3.6 s with a 0.8 s fade; pitches x1.2 and x0.76.
- **B36:** the jelly's alphas and glow; the Borer's glow; strand ink 0.6; which effect in each shared set kept its old picture.

### Tests that changed meaning

Each was rewritten to a ruling on purpose, not weakened. The full list is under each package's "Tests".

- **B02:** `allVesselsFillCorkTransferEquipAndSerialize` asserts a held open gourd does not feed (FX-1); `earnedScarLessonBackfillsPracticePageWithoutNewRewards` became `earnedScarLessonOpensPracticePageWithoutNewRewards` (the migration table is empty).
- **B03:** `phoenixRequiresReadinessAndOnlyPaysOnce` expects a save to leave 4 health (NQ-17-1).
- **B04:** the Temper, Carrion, Absorption and Sated Siphon tests moved to the new bands and rules.
- **B05:** the price pins for Insatiable Hunger, Sanguine Magnetism, Synaptic Storm, White Verdict, Rimebound Sentence and Funeral Bell.
- **B06:** a bone gives bone meal only under Vital Effusion (CQ-5); the ice chain needs catalysts.
- **B07:** a plain fill leaves clot and Projection blends age (LP-4, NQ-01-3).
- **B08:** the blank Hematic Memory is crafted with empty corners; the Mnemonist pins "a Sanguine Formation and redstone".
- **B09:** a vein moves blood only toward a drawing end (CL-14); the gouge dam tests removed (CQ-44, NQ-02-4); the feeder needs an owner.
- **B10:** Temper from a creature scales by the unread share (CL-12).
- **B11:** `cleatsCutTheUpkeep` wears fitted boots.
- **B12:** `checkEvents` marks the Bandolier given before the Temper lesson; the HUD scale test removed with its option.
- **B13:** Vampire Bat colonies live in four Nether biomes, not the whole Nether; ice fish profiles expect Congeatio.
- **B15:** the Ringmaster is a boss (D-33); `mycophantRematchLine` is required and re-arms after a loss.
- **B16:** the retired spend count stays gone (`theRetiredSpendCountStaysGone`).
- **B17:** a Canon Memory weaves only at D8 after the trial.
- **B18:** the Endless Hour is no longer a death save; its five death-save golden rows are gone.
- **B19:** `AnimationTimeGuardrailTest` drops the 15 moved classes (A-4 split).
- **B21:** fourteen playthrough probes flipped to required and rewritten to their PT rulings; the Effigy tests follow NQ-10-10.
- **B22:** `ledgerJoinHasNoGate` is now `ledgerJoinNeedsTheGates`; kicked ledgers crumble.
- **B23:** a Monolith shatter gives exactly 8; the atlas gates by Degree; `d4ThuribleHasATeacher` retired.
- **B24:** `silentiumGreetedAsArchon` is now `silentiumHasAGreetingOfTheirOwn`, required; `greetingDegree(8, SILENTIUM)` is 8.
- **B25:** `riteLockedStationStillTakesProjectedBloodUntilF12` is now `riteLockedStationRefusesProjectedBlood`; the PT-32 checks are stricter.
- **B26:** `courtPointersPastTheGarden` is required; the Lightkeeper lesson says "Wear", never "feed".
- **B27:** `daggerVolleyMatchesLegacyBloodShotImpactAndBleeding` is now `daggerVolleyKeepsItsDamageAndTheThirdDaggerPins`.
- **B29:** `ringmasterReachFromTheFloor` is required and walks a path.
- **B30:** the Liberation finale wants the ruin, not cobwebs; the camp tests read the new templates.
- **B35:** `ManipulationCastSoundsTest` pins the `sounds.json` aliases.
- Frozen lists refreshed on purpose: registry ids (B28, B31, B35), creative tab order (B28), dialogue route fingerprints (B26, B34), command and Scriptorium fingerprints (B22).

### Still open

- **Held by you:** the jet, spray and plant kits and the stain run (CQ-25); Along the Red (CQ-45) and the plant strand; the brand and plant looks (VX-18); NQ-10-5 and PT-85 (the investigation below waits on your pick); advanced brewing into tinctures (DS-5, parked); the Saint look tasks 08 T10 to T14; the rite cost pass 13 T14; visual batches 09 T11; Sound S3 to S6; the lash tuning write-back (UN-41).
- **Reversed:** Silent Severance keeps its name (C-2; 12 T13 dropped), recorded in `docs/ENDGAME_PATHS.md`.
- **Skipped in build 2, still in `OPEN_QUESTIONS.md`:** 07 T4 (live Vesper victory test), 10 T7 (PT-24), 10 T5 (the blood font), 10 T8 (PT-30), 10 T10 (PT-10), 09 T7 (the 43 A rows), 09 T13, the 09 T14 Crossbar, 09 T15, the polyp models and first-person arms, 14 T12 rest, the 15 T8 jigsaw split, the FX-21 section G rest, the `afalteringwillstilltakestheprojection` watch, the stale MnA jar pin (B02 note).
- **New questions:** D-31 (faculty) and the FX-21 section G owner's calls (`OPEN_QUESTIONS.md` 1.4).
- **Reverted packages:** none.
- **Small gaps the verifiers noted, none blocking:** no test pins Annetta at 51 (B15), six of B05's cut prices, B08's five repriced crafts, the rite sync's empty-list-once rule, the Verdigris change as behaviour or the Mortal Display lodestone change (B02), the Alchemist's Phlegethosoma line (B14), the SuccessionRites Liber unlock (B25), the Thurible Working refusal for a D8 Silentium (B23), per-act GameTests for Feint, Smoke, Encore and Lift (B27), `HemoParticleBursts` (B19), and B35's termite, flint-and-steel and death-length changes. The Crimson Tithe's Saint answer skips the 48-block challenger check (B18).

### PT-85 investigation: outpost NPCs and the ledger summon

Written on 2026-11-14 by the investigation you asked for in NQ-10-5. The harness would not let it save a file of its
own, so the full report is kept here. It is section 1.2 of `docs/OPEN_QUESTIONS.md`.

**Summary**
1. Recruiting an outpost Vicar, Alchemist or Mnemonist copies nothing and makes nothing new. It adds the real outpost
   NPC's UUID to the line. The ledger's Summon then teleports that same entity into the Fane, and nothing sends it back.
2. This is a leftover. The Succession spec and the wiki already say originals stay at the outpost and never take rite
   stations. The code never caught up: the summon, the helper check and a one-shot "return home" flag still let an
   original be carried off.
3. The path is still live. The Vicar's and the Monolith's Covenant Vigil lines tell the player to summon a recruited
   Vicar, and six playthrough probes use a summoned original as a rite helper.
4. Recommendation (option A): keep the pledge as a pool bond only. Make the summon and every rite station
   successor-only, and send any displaced original home every time. The planned "send home after use" fix and NQ-10-5
   then go away.
5. The catch: a solo player's D6 Covenant Vigil and Archon rite each need one helper. Under A, a solo player must make
   a successor at D5 first. Say if you want that gate. If not, pick option C.

**1. What the code does today**
- Recruit. `RecruitmentDialogueEvents.handleRecruitHarbinger` handles the `recruit_harbinger` event. The Vicar,
  Alchemist and Mnemonist trees offer it, at D5 only. It calls `BloodlineSavedData.addNpcMember(line, entity.getUUID(),
  type, outpostKey)`, so the outpost NPC itself becomes a line member. `Bloodline.addNpcMember` gives it 1,000 mL of
  pool capacity and a 1,000 mL rite reserve. The limits (`hasNpcMemberType`, `hasNpcMemberOutpost`) are checked per
  line, so a second line can pledge the same NPC. Nothing is copied or spawned.
- Ledger summon. `PacketLedgerAction.handleNpcSummon` runs from the ledger screen's Summon button (`LedgerScreen`). It
  loops over `getNpcMemberUUIDs()`. `teleportNpc` force-loads the recorded outpost (`loadRecordedOutpost`, a chunk
  ticket). `moveLoadedRecruit` then moves the outpost's own entity to the player with `teleportTo` or
  `changeDimension`. No home is stored and no return is queued. Only `HarbingerVicarEntity` keeps an `outpostHome`
  (set by `HarbingerOutpostStructure`), and it is only a `restrictTo` radius of 12.
- The only way back. `SuccessionResidents.returnOriginalHome` runs from `ProfessionalHarbingerEntity`'s tick for
  non-successors and moves an original back to its outpost. The first time it finds the NPC at home it sets
  `SuccessionHomeChecked`, and it never clears that flag. So an NPC that has stood at its outpost once is never
  returned after a later summon. This is why `summonStripsTheOutpost` (PT-85, in
  `PlaythroughLateNpcUsabilityGameTests`) finds no Vicar for the D0 newcomer.
- Rite stations. `CardinalRiteAllyService.eligibleNpcHelper` returns true for any line NPC member that is not a
  successor (`!npc.isSuccessor() || SuccessionResidents.helper(npc)`). So a summoned original can be given a station
  through `tryAssignNpc`, which `CardinalRiteInteractionHandler` calls. When the rite ends, `returnNpcAlliesToFane`
  only stops an original's navigation, so it stays in the Fane.
- Succession, for contrast. `SuccessionDialogue.handle` records a Bequest and fills donor vials from the original,
  which stays where it is. The Hematic Succession rite makes a new entity with its own UUID, a `SuccessorRecord` in
  `SuccessionSavedData` and a Fane workplace; `SuccessionResidents.join` adds it to the line through
  `Bloodline.addSuccessorMember`. `CardinalRiteNpcTravel.gather` brings successors to stations, and `returnHome` walks
  them back. Mnemonic Restoration rebuilds the same successor. `SuccessionDialogue.decorate` already removes the
  recruit and expel options from successors.

**2. Entry points that still take an outpost NPC**
1. Ledger Summon: the `LedgerScreen` button, `PacketLedgerAction.ACTION_SUMMON_NPCS`, `handleNpcSummon`, and its retry
   in `PacketLedgerAction.onServerTick` (`PENDING_SUMMONS`).
2. Station assignment of a summoned original: `CardinalRiteAllyService.tryAssignNpc` through `eligibleNpcHelper`.
   After that, `directToStation` can `teleportTo` the station (its `RECALL` case).
3. The missing return: `returnNpcAlliesToFane` and the one-shot `SuccessionHomeChecked` flag.
4. Teaching that sends players this way: `hemomancy.vicar.covenant.complete_vigil` and
   `hemomancy.monolith.counsel.complete_vigil` both say "summon a recruited Vicar to the Fane".

The pledge itself moves nothing. It feeds four things: the pool, the stake budget
(`HematicStakeBlock.configuredStakeBudget`), the rite reserve, and the rule that lets Projection pass through an ally
(`BloodProjectionInteractionEvents.prefersProjectionOverConversation`).

Probes that summon an original as a helper: `PlaythroughIlluminatusGameTests` (lines 697 to 709 and 1593),
`PlaythroughArchonGameTests` (251), `PlaythroughSanctifiedGameTests` (296, 1237), `PlaythroughEndgameGameTests` (763)
and `PlaythroughLateNpcUsabilityGameTests` (1703, 2146).

**3. What the lore and the references say**
- `docs/HEMATIC_SUCCESSION_DESIGN_SPEC.md` line 9: outpost NPCs "must remain persistent teachers ... rather than
  exhaustible resources that players abduct". Line 309: originals keep "ordinary recruitment and pool benefits but do
  not take physical rite-helper roles", and relocated originals are sent home.
- `wiki/Harbinger-Path.md` line 660: Succession works "without moving original teachers".
- `docs/NPC_VOICES.md` line 600: "The original teacher stays at the outpost."
- `docs/LORE_REFERENCE.md` lines 54 and 301 frame the pledge as a doctrine of propagation ("the outpost does not empty
  itself into that one bloodline"); its "Manifested successors" section says a successor carries the profession
  "without becoming the original teacher".
- `docs/HEMOMANCY_REFERENCE.md` is the stale part: line 3559 still lists "Summoning recruited NPCs" as a ledger action
  and line 3561 still describes recruits as phantom members that add 1,000 mL.

All of these agree with you: an original is pledged, never moved. Bloodline NPCs with bodies come from Succession.

**4. Options**
- **A (recommended), size M.** The summon moves successors only and never loads an outpost (remove
  `loadRecordedOutpost` and `RECRUIT_LOOKUP_TICKET`; rename the button to call the line's residents).
  `eligibleNpcHelper` accepts successors only. `returnOriginalHome` runs whenever an original stands outside its
  outpost and the outpost's chunks are loaded, with no one-shot flag. Rewrite the two `complete_vigil` lines (this also
  covers PT-86's line) as "a line member or a successor of your Fane". Move the six probes onto a successor fixture and
  flip `summonStripsTheOutpost` to "the Vicar stays". Cost: a solo player needs one successor (made at D5) before the
  D6 Covenant Vigil and the Archon rite, and three before Ancestral Communion at D7.
- **B, size L.** Remove ordinary recruitment as well; the Bequest becomes the only bond. Remove `recruit_harbinger`,
  `expel_harbinger`, the recruit nodes in three trees, the type and outpost limits and the originals' pool capacity.
  Rewrite `LORE_REFERENCE.md` lines 54 and 301, reference lines 3561 to 3565, the stake budget rule and the
  recruitment probes. Cleanest story, most churn; solo pool growth would wait on Succession too.
- **C, size M+.** As A, but a pledged original can hold a station from afar without moving ("keeps vigil from the
  outpost") and counts as a helper for a solo player. Needs a non-body helper path in
  `CardinalRiteAllyService.isAvailable` and `hasRequiredHelpers`, plus some art.

NPCs already pledged need no migration (the mod is not released). Under A and C they stay pledged pool members and the
every-time home check returns any that stand in a Fane. Under B the old entries are cleared from dev saves.

**What this changes in `OPEN_QUESTIONS.md`.** NQ-10-5 closes once you pick. The section 2 PT-85 default (two lines may
pledge one NPC) stands. PT-10 and NQ-10-4 shrink: an original could then be lost only to `/kill` or the void. The
shared vacancy store (10 T10) no longer needs `outpostHome` on every resident or a "sent home" path. PT-86's line is
reworded with the Vigil lines. The 10 T10 (PT-85) row in section 3 is already written for option A.

### How to undo

- Nothing is committed. Each package has a restore point `refs/build2/<id>-before`, made before it started, from
  `refs/build2/B01-before` (the tree after the snapshot refresh) to `refs/build2/B37-before`.
- The restore points cover tracked files only. Untracked files a package made are named in its log section, and
  some packages kept backups (`build/build2/B10-untracked`, `build/build2/B11-untracked-backup/`).
- To see one package: `git diff refs/build2/<id>-before refs/build2/<next>-before` for B01 to B36, and
  `git diff refs/build2/B37-before` for B37. Later packages build on earlier ones, so undo from the newest back.
- To drop all of build 2: restore tracked files from `refs/build2/B01-before` and delete the untracked files the
  packages added. The overnight build's own restore points (`refs/overnight/*`) are older still.
- Removed code, tests, lang keys and art were moved to `removed_assets/` with `MANIFEST` lines, not deleted.

## How to verify

Run each command from the repo root, one at a time. Only one Gradle build may run in this tree at once. Put output
under `build/build2/`.

| Suite | Command | Result line | Time in the baseline |
| --- | --- | --- | --- |
| Compile and JVM unit suite | `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain` | `BUILD SUCCESSFUL` at the end of the Gradle output. Counts come from `build/test-results/test/*.xml` (sum `tests`, `failures`, `errors`, `skipped`). Failures also print as `FAILED` lines in the Gradle output. | 1m 48s |
| Combined GameTest run | `./gradlew.bat runGameTestServer --offline --console=plain` | `build/phlegethontic-gametest/logs/latest.log`, after `GAME TESTS COMPLETE`: "All N required tests passed :)" or "N required tests failed :(" followed by the names. A required failure fails the Gradle build. | 2m 16s |
| Playthrough suite | `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain` | `build/playthrough-gametest/logs/latest.log`: "All N required tests passed :)" then "N optional tests failed" and the optional names. Optional failures do not fail the build. | 2m 6s |
| One named suite | `./gradlew.bat run<Suite>GameTestServer --offline --console=plain`, the launcher named in `gradle/game-tests.gradle` (for example `runClinicalGameTestServer`, `runSuccessionGameTestServer`, `runDialogueRouteGameTestServer`) | `<suite directory>/logs/latest.log`, same summary lines. The directory is the suite's `directory` in the `gameTestSuites` table. | not run |

Notes:
- Baseline outputs: `build/build2/baseline-unit.txt`, `baseline-gametest.txt`, `baseline-gametest-latest.log`,
  `baseline-playthrough.txt`, `baseline-playthrough-latest.log` and `baseline-playthrough-optional.txt` (the 59 sorted
  optional names).
- To compare a later playthrough run, take the names between "optional tests failed" and the closing `====` line of
  `latest.log`, sort them, and diff against `build/build2/baseline-playthrough-optional.txt`.
- `alphaCheck` was not run. It wraps the unit suite and the combined run, which are both green now.
- To cut fresh frozen lists, set `HEMOMANCY_FROZEN_SNAPSHOT_OUT` to a folder before the combined run. The two frozen
  tests then write the current lists there and pass instead of comparing. The dialogue route test always writes
  `build/phlegethontic-gametest/dialogue-route-fingerprints.actual.txt`.

## Baseline

Run on 2026-11-14 at about 21:20, after the snapshot refresh for the two GameTest suites. No other Gradle task, game
server or client was running. Two idle Gradle 8.8 daemons from an earlier session were left alone.

### Compile and JVM unit suite
- `BUILD SUCCESSFUL in 1m 48s`. All three source sets compile.
- 6,031 tests in 1,331 classes: 0 failures, 0 errors, 0 skipped.
- Failing now: none.
- The full run came before the refresh. The three unit classes that read the refreshed or edited files
  (`NpcTradeCatalogueDataTest`, `CreativeTabSectionTest`, `GameTestHarnessSourceContractTest`) were run again after
  it and pass (`build/build2/snapshot-unit-check.txt`).

### Combined GameTest run
- "1917 GAME TESTS COMPLETE IN 1.776 min", "All 1917 required tests passed :)". No optional failure.
  `BUILD SUCCESSFUL in 2m 16s`.
- Failing now: none.

### Playthrough suite
- "432 GAME TESTS COMPLETE IN 1.618 min", "All 432 required tests passed :)", "59 optional tests failed".
  `BUILD SUCCESSFUL in 2m 6s`.
- Failing now: no required test. The 59 optional names are the same, name for name, as the overnight final check
  (`build/overnight/final-playthrough-optional.txt`):
  `alchemistbasinsampleinmainhand`, `anyprogenitorbindsanythrone`, `archonchainfrompreviousband`,
  `atlasfragmentsandresiduum`, `awakenedplayeratunclaimeddisplay`, `bloodlinenameisenglish`,
  `bloodlinerecallandmemberpowers`, `brokenmortaldisplayleavesnooath`, `chamberreturn`, `charmremovedwhilepending`,
  `condenserroundtripcountsasabsorption`, `courtleadlostforgood`, `courtpointerspastthegarden`,
  `d4thuriblehasateacher`, `d5monolithcannotspeak`, `d5strainwithdegradationoff`, `disbandedprogenitorthrone`,
  `droppedconcentratedvialisreissued`, `eternalcovenantacrossdeath`, `firstprojectioninthegardens`,
  `founderprojectionreceivesrecruit`, `fullvesselabsorptioncounts`, `gardenstravel`,
  `guidedchamberlogoutandlethaldamage`, `guidedchamberrestoreseverything`, `harbingerd5toapotheos`,
  `harbingerd5tosilentium`, `heartnoderespawntrap`, `hematicunbindingofflineshare`, `hermitroadafterd2isdeadend`,
  `kickedmemberstaleledger`, `ledgerjoinhasnogate`, `linelessd6vicarguidance`, `lostblankbeforecarve`,
  `membercannotbindthrone`, `memberroutetod7`, `monolithscrapisrepeatable`, `mycophantrematchline`,
  `notrunnamesthemissingchamber`, `outpostspawnsallresidents`, `recollectioncystcreativefullpack`,
  `relaychartfindsagarden`, `rewardlandingsurveylate`, `ringmasterreachfromthefloor`, `secondspinoverwritestheproof`,
  `sharedeffigychargecredit`, `shatterbeforecornerstone`, `silentiumgreetedasarchon`, `sleepneedsthewholeserver`,
  `stashedconcentratedvialisreissued`, `summonstripstheoutpost`, `twoplayersonecentrifuge`, `twoplayersonetemple`,
  `uniquenpclossisrecoverable`, `uniquenpclosslate`, `vespermemorystashedinachest`, `vesperordeal`,
  `waybilllocatesacamp`, `wronghermitclaimsandsilentfarewell`.
- PT-88 `apotheosritewithoutrescue` passed this time. It fails in most runs, so it may show up in a later list
  without being a new failure.

### Opt-in suites (known, not run in the baseline)
- Clinical (`runClinicalGameTestServer`): `allVesselsFillCorkTransferEquipAndSerialize` fails. It still expects an
  open gourd held in the hand to feed the player; since 2026-11-14 only the worn gourd feeds (FX-1).
  `earnedScarLessonBackfillsPracticePageWithoutNewRewards` fails too and is older.
- Succession (`runSuccessionGameTestServer`): `manufactureConsumesPatternAndTwoHundredFiftyBlood` fails and is older.
- Flakes seen once or twice on 2026-11-14, not seen here: `chapelwavemobsleavenolootorexperience` (combined),
  `afalteringwillstilltakestheprojection` (`ShapingValidationGameTests`, to be renamed by the Sculpting package), `archonRiteWithHelper` (playthrough).

## Snapshots

Row "17 snapshots" (area "Architecture, tests and housekeeping"). A combined run with
`HEMOMANCY_FROZEN_SNAPSHOT_OUT=build/build2/frozen-actual` wrote the current lists
(`build/build2/pre-refresh-gametest-latest.log`: 1,917 tests, only `everydialogueroutekeepsitsoutcome` failing). Each
difference was checked against the golden before it was copied in, with LF line endings kept:

- `src/gameTest/resources/frozen/registry-ids.txt`: one line added, `minecraft:item hemomancy:titration_bandolier`,
  at line 1590 before `toxicognath`. Nothing else differed.
- `src/gameTest/resources/frozen/creative-tab-order.txt`: one line added, `hemomancy:titration_bandolier`, at line 33
  after `charm_of_vascularium`. Nothing else differed. The `dictation_table` row was already gone (F-22), and the test
  now passes over the whole file.
- `src/gameTest/resources/dialogue/route-fingerprints.txt`: lines 200 (`mnemonist_guided_chamber`), 304
  (`vicar_begin_initiation`) and 307 (`vicar_release_charm`). In each, the saved `harbinger_equipment` attachment goes
  from `Size:9` with nine render-layer flags to `Size:10` with a tenth flag `9:1b`. Line 200 was checked to differ in
  nothing else. No other line differed.

Docs: the row left `docs/OPEN_QUESTIONS.md` section 3, and its "State of the code" bullet now reads green.
`docs/TESTING.md` "Current state" carries the baseline numbers. Chunk 17's status line for T6 and the suggested order
in `docs/owner_answers/00-README.md` are updated.

## B01. The Sculpting rename

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 05 T1 (CL-3), NQ-05-1 A | "Shaping" is "Sculpting" everywhere a player or developer reads. Noun "a Sculpting", plural "Sculptings", verb "sculpts", advancement "Sculpted". "Shape" stays where it means geometry, terrain, a summon's body or a weapon form. | `src/main/resources/assets/hemomancy/lang/en_us.json` (66 lines), Liber pages under `books/libersanguinium/manipulations/pages/` |
| NQ-05-3 | 44 Java files renamed: `VerbSculpting`, `Sculptings`, `SculptingDispatcher`, `SculptingSessions`, `SculptingRules`, `SculptingPulse`, `StrandSculpting`, `JeiSculptingIcons`, the 30 `*Sculpting` classes, `SculptingGuardrailTest`, `SculptingDispatcherTest`, `SculptingValidationGameTests`, `SecondSculptingsGameTests`, `SculptingsClientReview`, `SecondSculptingsClientReview`. Setters `setProjectionSculpting` and the other three, `getSculpting(s)`, `SculptingDispatcher.trySculpt` and `.sculpts`, `BloodInfusionRecipe.isSculptedBy`, `TemperRules.Kind.SCULPTING`. | `common/manipulation/verb/`, `common/manipulation/<school>/` |
| NQ-05-4 | "narrow", "main", "claiming" and "held" kept in front of the new noun. | as above |
| NQ-05-5 | Ids follow: `.sculpting.project` / `.absorb` lang keys, the recipe field `"sculpting"` (22 recipe files and both serializers), the blood source id `sculpting` (`BloodManipulation.SCULPTING`), advancement `hemomancy:hemomancy/sculpted` (file `sculpted.json`, seven child parents), GameTest batch `sculpting_validation` and launcher `sculptingGameTestServer`, review clients `sculptingsReviewClient` and `secondSculptingsReviewClient` (users `SculptingsReview`, `Sculpting2Review`, kept to 16 letters), Liber pages `ground_sculptings`, `mining_sculptings`, `second_sculptings`, `second_sculptings_walls`. No registry id and no payload changed, so `PROTOCOL_VERSION` stays 50. | `gradle/game-tests.gradle`, `gradle/review-clients.gradle`, `data/hemomancy/recipe/`, `data/hemomancy/advancement/hemomancy/` |
| NQ-16-6, 16 T5 (CN-5) the Sculpting word | Lesson ids `craft.sculptings`, node `craft_sculptings`, event `mnemonist_craft_sculptings`. The 17 Mnemonist and Alchemist lines and options say Sculpting. Briefs, `lore-options.txt` and `route-fingerprints.txt` follow. | `CoreLoopLessons.java`, `dialogue_briefs/mnemonist_antecedent.json`, `src/gameTest/resources/dialogue/route-fingerprints.txt` |
| NQ-05-6 | Live docs rewritten. Dated logs, audits, the archive, `docs/core_loop/` track files, `AGENTS_PATCH.md`, plans and chunk files keep "Shaping". | see Docs |

### Skipped

| Row or part | Why |
| --- | --- |
| "Its own commit" (T1 plan) | No commits in this build. `refs/build2/B01-before` is the tree before the rename. |
| Review client | The rename changes words only, no look, screen layout or shader. No client launched. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL. 6,031 tests in 1,331 classes, 0 failures, 0 errors, 0 skipped. Matches the baseline (`build/build2/B01-unit.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: first run 1 of 1,917 failed, `chapelwavemobsleavenolootorexperience` ("an ordinary construct killed by a player drops experience (control)"). That file has no Shaping or Sculpting in it; it is a random XP control. Second run: "All 1917 required tests passed :)" (`build/build2/B01-gametest.txt`, `B01-gametest2.txt`). Treat the chapel control as a flake to watch.
- Tests renamed, not changed in what they check: `SculptingGuardrailTest` (seven checks, now reading `(set|add)(Projection|Absorption)Sculpting` and `.getSculptings(`), `SculptingDispatcherTest`, `SculptingValidationGameTests`, `SecondSculptingsGameTests`. Five GameTest methods renamed to match: `aClaimedBlockIsSculptedAheadOfTheCondenser`, `aClaimedFluidIsSculpted`, `aClaimedCreatureIsSculptedInsteadOfDrained`, `claimedItemsAreSculptedOnAFullBody`, `openAirIsSculptedAtFullReach`.
- Test resources rewritten with the new names: `route-fingerprints.txt` (the `mnemonist_craft_sculptings` rows), `lore-options.txt`, `player-block-edit-allow-list.txt`.
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv`.
- Proof of completeness: `grep -rn -i shaping src gradle` leaves 8 lines, all terrain or tool sense on purpose: `CourtPlacementRules` 50, `PelagicTerrainSampler` 85, `hematic_iron_knapper.desc`, two Vagrant Mind tests, `PelagicReefBlendTest`, `PelagicTerrainSamplerTest`.

### Docs

- Rewritten: `docs/HEMOMANCY_REFERENCE.md`, `docs/MANIPULATIONS_AND_CASTING.md`, `docs/CORE_LOOP.md`, `docs/CARDINAL_RITES.md`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/NPC_VOICES.md`, `docs/README.md`, `docs/TESTING.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md`, `wiki/Blood-Systems.md`, `wiki/Getting-Started.md`, `wiki/Harbinger-Path.md`. `docs/PELAGIC_WORLDGEN.md` read by hand: terrain sense only, left alone. `AGENTS.md` had no hit.
- Rulings recorded: CL-3, NQ-05-1/NQ-05-2 and NQ-05-3 to NQ-05-6 in `docs/CORE_LOOP.md` "Rulings that bind" (CL-3 line also in `MANIPULATIONS_AND_CASTING.md`); NQ-16-6 in `docs/NPC_DIALOGUE_AND_TRADE.md`.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-05-1/NQ-05-2 (and its "answers that free the most work" line), section 2 rows NQ-05-3, NQ-05-4, NQ-05-5, NQ-05-6, NQ-16-6, section 3 rows "05 T1 (CL-3)" and "16 T5 (CN-5), the Sculpting word". Row 05 T7 no longer waits on T1. In my area's rows 05 T3 rest, UN-5 and UN-65 the class and task names now read Sculpting. In other rows only code names were updated (`Sculptings`, `VerbSculpting`, `BloodBindingSculpting.pulse`, the `mnemonist.craft.sculptings.mend.line2` key); their prose was left alone. Section 4 gained row "B01 (CL-3)".
- Status tables: chunk 05 T1 and chunk 16 T5 updated.

### For the owner

- Lang keys renamed (values unchanged unless noted): the 37 `hemomancy.manipulation.<id>.sculpting.project` / `.absorb` keys; `advancements.hemomancy.sculpted.title` ("Sculpted") and `.description` ("...a memory selected that sculpts a verb..."); `blood_source.hemomancy.sculpting` ("Sculpting"); `hemomancy.jei.blood_infusion.sculpting.tip`; `hemomancy.jei.brazier_transmutation.sculpting.tip`; `hemomancy.mnemonist.slots.sculpting`; `hemomancy.dialogue.mnemonist.option.craft.sculptings` and `.first`, `.work`, `.mend`, `.draw`; `hemomancy.mnemonist.craft.sculptings.line1`, `.line2`, `.first.line1/2`, `.work.line1/2`, `.mend.line1/2`, `.draw.line1/2`; `hud.hemomancy.verb_channel.sculpting`; `message.hemomancy.temper.dry_sculpting`; `tooltip.hemomancy.blood_memory.sculpting` ("Sculpting, %s: %s").
- Values changed under unchanged keys: `hemomancy.manipulation.blood_projection.desc` and `blood_absorption.desc` ("let a selected memory sculpt it"), `hemomancy.alchemist.craft.temper.line2` ("a held Sculpting"), `hemomancy.vicar.craft.verbs.order.line1` and `.line2` ("a memory that sculpts the verb", "can sculpt a verb"). The Liber page titles and texts that named Shaping now say Sculpting ("Sculptings of the Ground", "Mining Sculptings").
- Kept on purpose: "shaped bodies" (Puppeteer summons), "Reshapes" in the conjure descriptions, "for shaping hematic iron", "What shapes can a strand take?" (strand forms), all terrain shaping.
- Values I chose: review client user names `SculptingsReview` and `Sculpting2Review`, because Minecraft caps names at 16 letters.
- Surprising: the tree was not committed before the rename, so it is mixed into the uncommitted tree. The rename alone is `git diff refs/build2/B01-before` plus the untracked renamed files.

### Verification (round 1)

Checked the tree, not the report. Pass.

- Scope: the 62 tracked files in `git diff refs/build2/B01-before` and the 82 status changes against `B01-status-before.txt` all carry the rename and nothing else. The lash tests and strand packets only change class names. No lash number, HUD design, held or reversed item touched. No payload id changed; `PROTOCOL_VERSION` stays right.
- Values: lang has "Sculpted", "Sculpting", "sculpts" and 37 `.sculpting.project`/`.absorb` keys. Recipe field `"sculpting"`, blood source `sculpting`, advancement `hemomancy/sculpted` and lesson `sculptings` are in the code. No `*Shaping*` file is left.
- Leftovers: `grep -i shaping src` gives the 8 terrain or tool lines the log lists. Three developer strings still say "Shaped" for the advancement: `CoreLoopClosingGameTests` lines 97, 118, 132 and `StrandGameTests` line 516. Minor; change them to "Sculpted" in a later pass.
- Doc nits: `docs/CORE_LOOP.md` line 311 and `docs/MANIPULATIONS_AND_CASTING.md` line 307 still list NQ-05-1 as an open id. The UN-8 row in `docs/OPEN_QUESTIONS.md` still names `gloam_laceration.shaping.project`; the key is now `.sculpting.project`.
- Unit suite: BUILD SUCCESSFUL, 6,031 tests, 0 failures (`build/build2/B01-verify-unit.txt`).
- Combined GameTest: "All 1917 required tests passed :)" (`build/build2/B01-verify-gametest.txt`).
- Playthrough: "All 432 required tests passed :)", 60 optional failures: the baseline 59 plus `apotheosritewithoutrescue` (PT-88), which the baseline notes fails in most runs (`build/build2/B01-verify-playthrough-optional.txt`).
- No client launched: the rename changes words only.

## B02. Architecture and test housekeeping

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 17 T8 (FX-24), NQ-17-5 | New manual Gradle task `cleanGameTestWorlds`. It deletes `world/` and every `blood-profile-packs-*` and `blood-response-packs-*` folder under the combined directory and each suite directory in the `gameTestSuites` table, and keeps `logs/`. It is not wired into any run. First run: 65 folders under 70 directories, the 29 GB combined world among them. | `gradle/game-tests.gradle` |
| 17 T7 (FX-14, rite sync) | `CardinalRiteSync` sends each player only the rites within `VIEW_RANGE` = 128 blocks of the rite's centre, plus the rites they cast or help from anywhere in the level. An empty list goes once, then no more empty lists until the player sees a rite again (`SENT_EMPTY`, a session set cleared through the hub). Login and dimension snapshots use the same filter. | `common/rite/harbinger/CardinalRiteSync.java`, `common/event/TransientServerStateEvents.java` |
| 17 T7 (FX-14, Chamber) | The two could disagree: a direct scene send was not in `LastSent`, so after the ordeal the 40-tick pass could skip a change the client never had. The five direct sends in `VesperOrdealManager` now go through `ChamberOfWillManager.sendScene`, which sends and forgets the player's `LastSent` mark, so the next pass resends the whole Chamber state. The lit Black Heart test was added. | `common/encounter/ChamberOfWillManager.java`, `VesperOrdealManager.java` |
| 17 T5 (G-7b) | New `ElytraEffectEvents`: when the Elytra effect (`noctifly_agaric`) is removed (milk, `/effect clear`) or expires, its flight grant is revoked if it holds one. New `FlightArbiter.grants(player, source)`. | `common/effect/ElytraEffectEvents.java`, `common/util/FlightArbiter.java` |
| 17 T3 (S-5b) | New playthrough test for the Bloom's Spine reclaim with a full pack bar the empty hand, in Survival and in Creative. The Mortal Display's lodestone now inserts through `PendingDelivery.insert` and drops only what did not land. | `MortalDisplayBlock.java`, `PlaythroughArchonGameTests.java` |
| 17 T11 (F-16) | Sheolic Crimson Retribution places `crimson_flames` through `PlayerBlockEdits.place(player, pos, state)`, so game mode, spawn protection and the place event can refuse it. | `common/armor/ability/SheolicBastionBloodlustArmorAbilityHandler.java` |
| 17 F-16 follow-up, F-16 look target, F-16 Verdigris | `MorphlingItem.findLookTarget` and `Reach.UNGATED` deleted with the matrix row (`morphlingLookTarget`) and the guardrail pin. The Verdigris Aura's targets must pass `canHarmFor(holder, target)`. | `MorphlingItem.java`, `ManipulationCombatHelper.java`, `common/effect/VerdigrisAuraEffect.java` |
| F-16 attacker bonuses | Kept as built; nothing to change. Recorded as a ruling. | - |
| 17 flakes | `chapelwavemobsleavenolootorexperience`: the control construct was a plain monster, and a monster despawns at once when the nearest player is more than 128 blocks off. In a full run that player is another test's. The control is now `setPersistenceRequired()`. `archonRiteWithHelper` failed with the grid at x 13,406,416, and the Will test at x 14,556,977 (an entity lookup whose box ran past the section index). At those distances a float holds a coordinate only to the nearest block. `GameTestIsolation.ORIGIN_RANGE` = 1,000,000 now folds the random origin into that range of 0. It stays random and chunk-aligned (RV-39 kept). | `ChapelRobustnessGameTests.java`, `GameTestIsolation.java` |
| 17 clinical suite | `allVesselsFillCorkTransferEquipAndSerialize` runs its corked, rate, dormant and overflow checks on the worn gourd (`onWornTick`) and checks that an open held gourd feeds nothing. The two older failures were tests of removed rules, rewritten to the rules now in force (see Tests). | `ClinicalBloodProgressionGameTests.java`, `SuccessionGameTests.java` |
| 17 GeckoLib jar | `libs/geckolib-neoforge-1.21.1-4.8.4.jar` moved to `removed_assets/libs/` (MANIFEST row added). `gradle/verify-dependency-bundle.ps1` and `build.gradle` never named it. | `removed_assets/libs/`, `docs/DEPENDENCY_PROVISIONING.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| 17 flakes, the Will test | Cause not proven. The one failure had no stack trace. It sat at x 14.5M, like the Archon one, so the origin fold probably covers it. The row stays as a watch. |
| Deleting `EarlyInitiation.give` | No main-code caller is left, but the playthrough fixtures use it to hand out ARRANGE stock. Kept. |
| 17 T12, 17 T13, 17 DS-5, UN-71, "17 snapshots" | Not in this package. |
| Review client | No row changes a look, a screen or a shader. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,031 tests in 1,331 classes, 0 failures, 0 errors, 0 skipped (`build/build2/B02-unit.txt`, and `B02-unit2.txt` after the doc edits). Same as the baseline.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1920 required tests passed :)" (`build/build2/B02-gametest.txt`, `B02-gametest-latest.log`). Baseline 1,917, plus 4 new, minus the deleted look-target row.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known to fail in most runs) (`build/build2/B02-playthrough-optional.txt`).
- `./gradlew.bat runClinicalGameTestServer`: "All 132 required tests passed :)". `./gradlew.bat runSuccessionGameTestServer`: "All 22 required tests passed :)". Both failed in the baseline.
- `./gradlew.bat cleanGameTestWorlds`: BUILD SUCCESSFUL, 65 folders deleted (`build/build2/B02-clean-worlds.txt`).
- Added: `FlightSourceGameTests.elytraEffectRemovedInTheAirEndsItsGrant` (G-7b: milk in the air ends the grant, and a later Edacious revoke then grounds the player). `PeriodicBroadcastGameTests.litBlackHeartRefreshesEveryPass` (the test the earlier package left out), `directFightSceneMakesThePassResend` (a direct scene makes the next pass resend), `riteSyncReachesOnlyNearViewersAndTheCaster` (the 128-block filter and the caster exception). `PlaythroughArchonGameTests.spineReclaimCountsTheSpineThatLanded` (S-5b).
- Changed: `CombatTargetingGuardrailTest.areaAndLookTargetsAskCanHarm` is renamed `areaEffectsAskCanHarm`. Its look-target pin is replaced by a Verdigris pin (`canHarmFor(entity, target)`), a no-`UNGATED` check, and a check that retribution uses `PlayerBlockEdits.place` and no raw `setBlock`. `CombatTargetingMatrixGameTests` lost the `morphlingLookTarget` row and the `UNGATED` line in the preset test, because the code is gone.
- Flipped on purpose: `allVesselsFillCorkTransferEquipAndSerialize` now asserts a held open gourd does not feed (FX-1). `earnedScarLessonBackfillsPracticePageWithoutNewRewards` became `earnedScarLessonOpensPracticePageWithoutNewRewards`: it tested a login backfill, and `PlayerDataMigrations` is empty before release. The advancement unlock opens the page now, and the no-items, no-proof and no-promotion checks stay. `manufactureConsumesPatternAndTwoHundredFiftyBlood` became `effigyPatternIsAWorkingCraftNotHandFed`: the Vacant Effigy is a Working craft (`recipe/cardinal_rite/working/vacant_effigy.json`), so a hand projection into its frame is a hint. It charges no blood, keeps the Formation and the frame, and makes no effigy. The old bodies are in `removed_assets/src/gameTest/`.
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv`.

### Docs

- Updated: `docs/TESTING.md` (opt-in suites green, the flake causes, the clean task, the origin fold), `docs/architecture/ARCHITECTURE_STANDARDS.md` (targets row, flight row, session-state row, section 5 paragraph), `docs/HEMOMANCY_REFERENCE.md` (Verdigris Aura, Noctifly Agaric effect, Sheolic retribution, `PacketSyncActiveRites`), `docs/MANIPULATIONS_AND_CASTING.md` (rulings), `wiki/School-Combat.md`, `docs/DEPENDENCY_PROVISIONING.md` (the stale GeckoLib and Curios note), `docs/architecture/CONSOLIDATION_PROGRESS.md` (S-5b, F-16, G-7b rows), `docs/audits/2026-11-02-cleanup/README.md` (C-19: RT-7 closed), chunk 17's status table.
- Rulings recorded: F-16 look target, F-16 Verdigris and F-16 attacker bonuses (2026-11-14) in `MANIPULATIONS_AND_CASTING.md` "Rulings that bind". G-7b's Elytra end is added to its line there. NQ-17-5 is in `TESTING.md` ("The GameTest world").
- `docs/OPEN_QUESTIONS.md`: section 2 rows NQ-17-5, F-16 (look target), F-16 (Verdigris) and F-16 (attacker bonuses) deleted. Section 3 rows 17 T8, both 17 T7 rows, 17 T5, 17 T3, 17 T11, 17 F-16 follow-up, 17 clinical suite and 17 GeckoLib jar deleted; 17 flakes shortened to the Will watch. The "State of the code" opt-in bullet now reads green. Section 4: the F-4, G-7b row gained the milk-in-the-air check, and a new row "B02 (F-16, FX-14)".

### For the owner

- No new or changed lang keys.
- Values I chose: the rite view range, 128 blocks; the test-origin range, 1,000,000 blocks of 0.
- Surprising: the GameTest grid could land up to 15,000,000 blocks out, where float maths in aim and geometry code rounds to whole blocks. Real players that far out may meet the same limit in rites and aiming. Nothing tests that.
- Rows in other areas this package also settles: "01 T4 (fault)" (Living blood and containers) is the same worn-gourd rewrite, now done. "08 T5 flake (FX-18)" (Saints) has its cause fixed, but its cooldown-message check is still to add. "P13 flake" (D7 to D8) is the Archon flake, fixed by the origin fold. I left those rows for their own packages.
- `gradle/dependency-bundle.json` still pins `libs/mna-3.1.11-neoforge.1.21.1.0.jar`, which the preflight script no longer lists, so the preflight would fail on "stale inventory". `DEPENDENCY_PROVISIONING.md` still says nine jars, Mana and Artifice among them. Not in this package.

### Verification (round 1)

Checked the tree against `refs/build2/B02-before` and the created-file list. Pass.

- Scope: only brief rows. No lash numbers, HUD, Silent Severance or held row touched.
- Values match the answers: manual `cleanGameTestWorlds` (not wired into a run), look target and `UNGATED` gone,
  Verdigris asks `canHarmFor(entity, target)`, retribution flames through `PlayerBlockEdits.place`, Elytra grant ends
  on remove and expire. The client replaces its rite list on each packet, so a rite out of range does clear.
- Tests: unit suite BUILD SUCCESSFUL, 6,031 tests, 0 failures (`build/build2/B02-verify-unit.txt`). Combined run
  "All 1920 required tests passed :)" (`B02-verify-gametest.txt`). Clinical "All 132 required tests passed :)"
  (`B02-verify-clinical.txt`). The two rewritten tests follow rules in force (the Vacant Effigy Working recipe is
  committed; the migration table is empty).
- Minor, not blocking: the Mortal Display lodestone change has no test of its own. The rite sync test checks
  `CardinalRiteSync.sees` only, not the one-empty-list rule.
- No game client launched: no row changes a look, a screen or a shader. No server left running.

## B03. Combat refactor: the Curved Horn, damage stages and one death-save stage

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 17 T12 (F-17), NQ-17-3 A | The Curved Horn is a Last Rite, id `hemomancy:curved_horn` (`LastRiteHelper.CURVED_HORN_ID`). Worn in the charm slot (index 5) it arms each tick only while nothing else is armed. Any other source arms over it: Phoenix Debt, the Morphling rites and the legacy Silent Refusal arm now ask `LastRiteHelper.isOpenToArm` (nothing armed, or only the Horn) instead of "nothing armed". Silent Refusal already armed over anything. Its save asks `canFire`, calls `consume`, keeps the 1 durability, Blood Rush, sound and animation, and shares the 12,000-tick cooldown. Today's rule (every death, no cooldown) is gone. | `common/event/LastRiteHelper.java`, `LastRiteRules.java`, `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java`, `ArmorSetBonusHandler.java`, `ManipulationReactiveEvents.java` |
| 17 T13 (G-1, F-18) | New `common/damage/DamageStages`, the one `LivingDamageEvent.Pre` subscriber. Attackers at HIGH: Sanguine Fists, the Absolved +2, the Penumbral ambush x1.5. Defenders at NORMAL, in this order: Second Pulse, Silver Ward, Silent Archon negation, the Sheolic stance and retribution, the memory tool's effigy, Iron Will, the avatar, the armour sets (sabatons, Chitinite, thorns, Prismatic flash), Still Waters, then the reactions (Phantasmal, scars). Death-saves at LOW: Endless Hour's deferral, then Silent Refusal, Phoenix Debt or the Curved Horn, first that fires. Readers at LOWEST: wound blood and the Formation drop, vein strain, the Mnemonist puppet's memory. Iron Will moved out of the wound handler into `BloodVolumeEvents.applyIronWill`. The Unstained handler split into `applySilverWard` and `applyAbsolvedBonus`. `onFinalDamage` split into `applyPenumbralAmbush` and `tryPhoenixDebt`. The 14 old handlers lost their `@SubscribeEvent` and are called from the table; `MnemonistPuppetEvents`, `StillWatersEvents` and the Phantasmal handler lost `@EventBusSubscriber`, since they had no other subscriber. | `common/damage/DamageStages.java` and the 14 handler files |
| NQ-17-1 A | A saving Last Rite (Phoenix Debt, the Horn) cuts the hit to what leaves 1 health (`DamageStages.leaveBarelyAlive`: health plus absorption, minus 1). Silent Refusal kept its own cut (health minus 1). Wound blood and vein strain read `DamageStages.landing`: the hit, never more than the body held (health plus absorption; health alone for Endless Hour's settlement). So a saved hit costs blood only for what got through. A 40-point hit at full health: Silent Refusal 3,000 + 95 mL, Phoenix Debt 1,200 + 95 mL, the Horn 95 mL. Phoenix Debt now ends the player at 1 health by cutting the hit, not by `setHealth(1)`, so absorption hearts are spent by a saved hit. | `DamageStages.java`, `BloodVolumeEvents.java`, `VascularSystemEvents.java` |
| NQ-17-2 A | No cap on stacked reductions. Nothing to build. The golden prints the worst pair: Iron Will with the avatar takes 5.25 of 10. | - |
| Absolved bonus characterised (row text) | Two new golden lines: a player's 10-point hit on a 40-health Desiccant, 9.68 without the bonus and 11.68 with purity 80. Cut on today's code before the refactor, unchanged after. | `CombatStageCharacterisationGameTests.java`, `combat/damage-stages.txt` |

### Skipped

| Row or part | Why |
| --- | --- |
| Moving Ink Mantle and Cryptobiosis into the death-save stage (T13 left it to the step) | Both answer a hit that has landed, from `LivingDamageEvent.Post`, inside the Morphling's combat scope with its own maturity, blood and cooldown checks. Moving them means rewriting both Morphling items. They stay on `Post`. The guardrail names them as the two `Post` rites, and `landing` caps their wound blood at what the body held, so NQ-17-1 holds for them too. |
| Review client | No look, screen or shader changed. |
| The "04 T5c" row in Cast economy | It still says it waits on 17 T12 and T13. That row is another area's; I left it. It now waits only on NQ-04-6. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,035 tests in 1,332 classes, 0 failures, 0 errors, 0 skipped (`build/build2/B03-unit2.txt`, and `B03-unit3.txt` after the doc edits). Baseline 6,031 plus the 4 new guardrail tests.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1922 required tests passed :)" (`build/build2/B03-gametest.txt`, `B03-gametest-latest.log`). 1,920 after B02, plus the 2 new Horn tests.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B03-playthrough-optional.txt`).
- `./gradlew.bat runClinicalGameTestServer --offline --console=plain`: "All 132 required tests passed :)" (`build/build2/B03-clinical.txt`).
- `./gradlew.bat runCombatOrderGameTestServer`, used to cut the goldens: 8 tests before, 10 after (`B03-combat-pre.txt`, `B03-combat-1.txt`).
- Added: `DamageStageGuardrailTest` (four checks: only `DamageStages` subscribes to `Pre`, and no `addListener` for it; the four stage priorities and who sits in the attacker and defender stages; wound blood and strain run last and read `landing`; `LastRiteHelper.consume` only in the three stage saves and the two `Post` Morphling rites, and the Horn file no longer names `LivingDeathEvent`). `LastRiteRulesTest.theCurvedHornYieldsTheSlot` (the yield rule, the Horn fires only while armed and shares the cooldown). `curvedHornSavesOnceInsideTheSharedCooldown` (two lethal hits: the first leaves 1 health and 1 wear, the second kills). `curvedHornYieldsToALaterLastRite` (a Morphling rite arms over the Horn, the Horn does not take it back, a removed Horn clears). The two Absolved golden rows.
- Changed on purpose: `ManipulationTargetingGameTests.phoenixRequiresReadinessAndOnlyPaysOnce` now expects a valid save to cut the 30-point hit at 5 health to 4 (it expected 0), per NQ-17-1. Calls renamed to the split methods, with the same checks: `ManipulationBalancePassGameTests` (Penumbral, `applyPenumbralAmbush`), `ManipulationBalancePass2StateGameTests` (unaffordable Phoenix, `DamageStages.deathSaves`), `ThermalCastReview` (`tryPhoenixDebt` returns whether it fired; it used to read health 1). The Horn row of the death-save fixture arms the Horn the way the tick does (`syncCurvedHornRite`).
- Goldens re-cut (`src/gameTest/resources/combat/`), every moved line explained:
  - `damage-stages.txt`, blood and strain now follow the reduced hit: avatar 50 to 37.5 mL (strain 3.75 kept), sabatons 50 to 42.5 and 5 to 4.25, Chitinite 50 to 37.5 and 5 to 3.75, Still Waters 50 to 35 and 5 to 3.5, avatar with sabatons 50 to 31.88 and 3.75 to 3.19, Iron Will with avatar 35 to 26.25. A zeroed hit costs nothing: the memory tool effigy and "zeroed at LOW" go from 50 mL and 5 strain to 0 and 0. Attacker bonuses now land before the readers: Sanguine Fists 50 to 60 mL and 5 to 6, Penumbral ambush 50 to 75 and 5 to 7.5. Final damage taken is unchanged on every line. Unchanged lines: the five "none" rows, Sheolic stance, Silent Archon negation, Iron Will alone, Second Pulse, Silver Ward, and the two new Absolved rows.
  - `death-saves.txt`, wound blood is capped at what the body held: "none" (death) 200 to 100 mL, Ink Mantle 700 to 600, Cryptobiosis 850 to 750 (and their pairs). Saves cut the hit to 19: Silent Refusal 3,200 to 3,095, Phoenix Debt 1,400 to 1,295 (and their pairs). Endless Hour defers before the readers: 200 to 95 mL, the 21 deferred points pay at settlement. Curved Horn: now armed (`hemomancy:curved_horn`), 95 mL, cooldown 12,000 after the save (was unarmed, 200 mL, no cooldown). "Silent Refusal + Endless Hour": the hour now holds the excess first, so the refusal does not fire (3,200 mL and 12,000 cooldown become 95 mL, no cooldown, 21 deferred). "Endless Hour + Curved Horn": the Horn is armed and not spent. Health after is unchanged on every line.
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv`.

### Docs

- Updated: `docs/architecture/ARCHITECTURE_STANDARDS.md` (two section 5 rows, the section 5 paragraph, the damage stage table in section 7, the guardrail index row), `docs/architecture/CONSOLIDATION_PROGRESS.md` (G-1/F-18 and F-17 rows built, the "what is left" line), `docs/HEMOMANCY_REFERENCE.md` (Last Rite group row, new Damage stages row, `bloodDrainPerDamage`, Iron Will's site, the Curved Horn's storage row), `docs/MANIPULATIONS_AND_CASTING.md`, `docs/TESTING.md` (the goldens), `wiki/Blood-Systems.md`, `wiki/School-Combat.md` (a damage-order paragraph), `AGENTS.md` (one clause in the combat paragraph naming `DamageStages` and its guardrail), chunk 17's status lines for T12 and T13.
- Rulings recorded under "Rulings that bind" in `docs/MANIPULATIONS_AND_CASTING.md` (combat rules): G-1/F-18 and F-17 marked built, and new lines for NQ-17-1, NQ-17-2 and NQ-17-3 (2026-11-14). Its open-question pointer no longer lists them.
- `docs/OPEN_QUESTIONS.md`: section 1 rows NQ-17-1, NQ-17-2, NQ-17-3 and the "answers that free the most work" line for them deleted. Section 3 rows 17 T12 and 17 T13 deleted. Section 4: the G-1, F-18 row now says what to feel and is "Now"; new row "B03 (F-17, NQ-17-3)".

### For the owner

- No new or changed lang keys.
- Values I chose: the order inside each stage (listed under Built). Sanguine Fists' flat bonus lands before the Penumbral x1.5, so an ambush with Fists multiplies the Fists bonus too. Second Pulse and Silver Ward stay first among the reductions, as they ran at HIGH before.
- My reading of NQ-17-1: "what got through" is the damage that lands, capped at what the body held. That also lowers the wound blood of an unsaved death (a 40-point hit at 20 health: 200 to 100 mL) and of the two `Post` Morphling saves (100 mL less). Say if a death should still cost the whole hit.
- Behaviour you will notice: thorns, the Prismatic flash and the sabatons now also answer a hit that Silent Refusal saves (the refusal used to stop that handler first). Endless Hour takes the excess before any Last Rite, so a running hour keeps your Silent Refusal, Phoenix Debt or Horn unspent. A Phoenix Debt or Horn save now spends your absorption hearts, as Silent Refusal already did.
- The Horn catches only hits that pass the damage stages. A death with no hit (a raw health set to 0) is no longer saved by it.
- With `lastRiteEnabled` off in the server config, every Last Rite fires on every lethal hit again, the Horn included. That is the existing switch.
- No look changed, so no screenshots.

### Verification (round 1)

Checked the tree against `refs/build2/B03-before` and the created-file list. Pass.

- Scope: only 17 T12 and 17 T13. No lash number, HUD, held row, payload, lang key or particle allow-list touched.
  Silent Severance keeps its name. Ink Mantle and Cryptobiosis stay on `Post`, as logged.
- Values: the Horn arms only while nothing is armed, every other source arms over it (`isOpenToArm`), and it shares
  the 12,000-tick (10-minute) cooldown (NQ-17-3 A). Saves cut the hit to leave 1 health and the readers take
  `landing` (NQ-17-1 A). No reduction cap (NQ-17-2 A). NeoForge 21.1.251 fires `Pre` before absorption is taken, so
  the health-plus-absorption buffer is right.
- Tests: `LastRiteRulesTest.theCurvedHornYieldsTheSlot`, the two Horn GameTests and `DamageStageGuardrailTest` would
  fail without the change. The Phoenix Debt test flip (0 to 4) follows NQ-17-1. No test weakened or skipped.
- Unit suite: BUILD SUCCESSFUL, 6,035 tests, 0 failures (`build/build2/B03-verify-unit.txt`).
- Combined GameTest: runs 1 and 2 each had one failure, `chapelwavemobsleavenolootorexperience` (the XP control).
  Run 3: "All 1922 required tests passed :)" (`B03-verify-gametest.txt`, `-gametest2.txt`, `-gametest3.txt`). No
  B03 code touches that kill path, and the same flake failed in B01. Treated as the known flake. B02's fix for it does
  not hold; the control sits at z 16, outside the "empty" template.
- Minor, not blocking: unsaved deaths now cost wound blood for what the body held (200 to 100 mL on the 40-point
  golden). That reaches past the saved-hit wording of NQ-17-1; the owner note asks about it. `curvedHornYieldsToALaterLastRite`
  checks "a removed Horn clears" without calling `syncCurvedHornRite` after the removal.
- No game client launched: no look, screen or shader changed. No server left running.

## B04. Cast economy and Temper, without the big price sheet

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 04 T5a (CB-7, CB-22, CB-23, CB-4 part) | Sovereign Instinct 450 to 200 mL, Ironhearted 300 to 600 mL, Void Shroud 100 to 300 mL, Radiant Wake 120 to 30 mL/s, Living Circuit 100 to 30 mL/s. | `common/init/ManipulationInit.java` |
| 04 T5b (CB-4, CB-5, LP-11), NQ-04-5 | Avatar stages 75/125/175/250/400 to 10/18/26/35/45 mL/s. Iron Choir 250 to 50. Absolute Stillness 300 to 70. Carrion Communion 220 to 50. Lignum, Canopy, Worked Lignum 100/125/175 to 30/40/50. Furnace Veins 150 to 40. Thread Ripper 35 to 300 mL. | `ManipulationInit.java` |
| 04 T1 (LP-9), NQ-04-9 A | What the pool cannot pay at a Tithe's deadline is kept as debt under `PlayerPersisted` (`hemomancy:crimson_tithe_debt`), so death keeps it. Once a second the pool pays what it can. A new Tithe is refused while debt is owed. | `common/manipulation/saint/CrimsonTitheManip.java` |
| 04 T1 (LP-7 test) | The Grave Debt GameTest runs a real owned Drudge through `DrudgeActions.graveDebt`. `GraveDebtManip` no longer adds the effect a second time; `SchoolHitHelper.markGraveDebt` lays it. | `GraveDebtManip.java`, `ManipulationBalancePass2TargetingGameTests.java` |
| 04 T2 (CB-10), P03-hour-death | A death during Endless Hour ends the hour and restarts the 1,200-tick cooldown (`EndlessHourManip.endOnDeath`). | `EndlessHourManip.java`, `ManipulationReactiveEvents.java` |
| 04 T3 (LP-14), NQ-04-8 | `TemperRules.burnPerSecond`: up to 10 mL/s burns 0.5 a second, up to 25 burns 1, up to 45 burns 1.5, above burns 2. `pulseCost(cadence, mlPerSecond)` spreads it over the cadence. `castCost` by rank is 2, 3.5, 5, 6.5, 8. A Sculpting pulse is banded by its price before the ledger; a channel or avatar pulse by its cost a second. The lash tap stays free. | `TemperRules.java`, `TemperCharge.java`, `BloodManipulation.java` |
| 04 T3 (CQ-9), NQ-04-10 B | `canDrainLivingTarget(activeBlood)`: a full body still draws a creature for its Temper. The blood past the cap is lost, or with Sated Siphon goes into the first worn gourd up to its volume (`BloodGourdItem.fillWornGourd`). | `BloodAbsorptionChannelRules.java`, `BloodAbsorptionItem.java`, `LivingStaffItem.java`, `BloodGourdItem.java` |
| 04 T4 (CB-1), LP-10 A | `bloodRegenRate` default 1.0 to 2.0 (2 mL/s). Lash prices untouched. | `config/HemoServerConfig.java` |
| 04 T6 (LP-16), NQ-04-1 to NQ-04-4, 04-T6-store | New `BossBindingRules` (total max HP / 100, at least 1; slip chance max HP / 2000 held to 10% to 60%, times 0.5 + 0.5 x health fraction; 60-tick wait) and `BossBindings` (count saved on the boss's persistent data, the slip roll, a per-player wait map cleared through `TransientServerStateEvents`). `BloodBindingManip.applyBinding` gates every boss bind. A spent boss is not eligible for the R cast, so nothing is paid. The held form binds a boss one boss-length hold at a time (60; Lingering 120, Chain 40, Lattice 80), never renews it, skips a boss while it waits after a slip, and lays the constricted slow and the brand only with a hold that landed. | `common/manipulation/animus/BossBindingRules.java`, `BossBindings.java`, `BloodBindingManip.java`, `BloodBindingSculpting.java` |
| 04 T7 (CQ-8 vein), NQ-04-5 | Each ore past the first costs 12 mL (`VeinMinerHelper.BLOOD_PER_EXTRA_ORE`, source `talaromyces_vein`, Category SCAR, counts as spend). The crawl asks `BloodAccounting.canPayInFull` before each break and pays after it, so the vein stops where the blood stops. New `CrawlSession.Gate` and an `OreVeins.mine` overload. | `VeinMinerHelper.java`, `CrawlSession.java`, `OreVeins.java` |
| 04 T7 (CB-20), NQ-04-14 | A Hewing cut costs the larger of 36 mL and the sum of `LivingDigRules.bloodPerBlock` over the blocks it will cut (`StaffMiningRules.hewPrice`). New `VerbSculpting.bloodPerPulse(ServerPlayer, VerbTarget)` so a pulse can be priced by its target. The HUD still shows the 36 mL floor. | `StaffMiningRules.java`, `HewingSculpting.java`, `VerbSculpting.java`, `BloodManipulation.java` |
| 04 T8, NQ-04-13, CB-11 | The `blood_moon` 0.75x line only for a caster at D1 or higher in the Overworld (`BloodMoonEvents.grantsCastDiscount`). The drain is unchanged: 25 mL/s, below D1, no sky check. | `BloodMoonEvents.java`, `ManipulationCostLedger.java` |
| 04 T9 (dome), NQ-04-12 A | Nothing to build: the dome stays. Row removed. | - |
| 04 PT-25, NQ-04-15 A | Conjuring stamps the paid blood on the item (new data component `hemomancy:conjure_paid`). A dispel returns 90% of it; an item nobody paid to conjure returns nothing. A cell hand keeps the stamp when the verb key turns it; a Staff weapon form refunds from the Staff stored inside it. The probe asserts the amount. | `ConjurationManip.java`, `DataComponentInit.java`, `CellHandFormHelper.java`, `PlaythroughNeophyteGameTests.java` |
| 04 CQ-17, NQ-04-11 B | Sanguine Mending heals a mendable creature that is the caster's ally by `ManipulationCombatHelper.allied`, a Ferric construct whose maker is the caster or an ally, or an unowned golem not targeting any player. `mendable_constructs` gained `hemomancy:drudge`, `hemomancy:ferric_rampart`, `hemomancy:iron_pillar`. | `SanguineMendingSculpting.java`, `data/hemomancy/tags/entity_type/mendable_constructs.json` |
| 04 T10 (records) | RV-28, CB-27 and CQ-8 records and the new rulings written in `MANIPULATIONS_AND_CASTING.md`. | docs |
| P03-dry-swap | Already built; recorded as a ruling. | docs |

### Skipped

| Row or part | Why |
| --- | --- |
| 04 T5c (LP-12), 04-T5-sheet | Next package, per the brief. |
| T10 stale strand lines, and the T3, T4, T8 notes, in `docs/core_loop/` track files | Those files stay put until the conflict answers are in (CQ-14). The references and topic docs carry the changes. |
| Review client | No row changes a look, a screen or a shader. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,039 tests, 0 failures, 0 errors (`build/build2/B04-unit2.txt`). 6,035 after B03, plus the new cases.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1929 required tests passed :)" (`build/build2/B04-gametest3.txt`, `B04-gametest-latest.log`). 1,922 after B03, plus 7 new.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 59 optional, the same names as the baseline (`build/build2/B04-playthrough-optional.txt`).
- Run history: the first combined run had 8 failures, all tests of the old numbers, rewritten as below. The second had `bloodbindinghalvesrecognizedbossduration`, a boss binding that slipped, fixed as below. The third was green.
- Added: `BossBindingRulesTest` (total and slip chance). `StaffMiningRulesTest.aCutCostsTwoMlAHardnessPointWithA36MlFloor`. `TemperRulesTest.heldFormsBurnByPriceBand`. GameTests `unpaidTitheDebtIsCarriedAndCollected`, `aDeathDuringTheHourRestartsItsCooldown`, `theBloodMoonDiscountIsForInitiatesInTheOverworld`, `mendingPicksAlliesAndPeacefulWildGolems`, `theScarredVeinStopsWhereTheBloodStops`, `aFullBodyLosesTheBloodUnlessSatedSiphonSendsItToTheGourd`, `aBossTakesItsBindingsAndThenRefuses`.
- Rewritten on purpose to the rulings: `TemperRulesTest.castsCostByRankAndPulsesByCadence` (2 to 8, banded pulses). `CarrionRulesTest` (an 8 mL/s form burns 0.25 a pulse). `BloodAbsorptionChannelRulesTest` (a full body draws). `BloodAbsorptionItemSourceTest` (Sated Siphon is now pinned in the draw, sending to the gourd). `TemperGuardrailTest` (the pulse passes its base price to `forPulse`). `ManipulationGapCombatStyleSourceTest` (the Grave Debt effect is pinned in `SchoolHitHelper`, its one adder). `TemperValidationGameTests` (Perfectus burns 8; the catalyst cases use spores (5) for a 3.5 shortfall and the enzyme for 8; the coal case reads the 40 mL/s band). `SecondSculptingsGameTests` carrion yields. `ManipulationCastFixGameTests` (registry costs, avatar upkeep 10 mL). `ManipulationBalancePass2SchoolBGameTests` (Living Circuit 30). `StaffMiningGameTests` and `GroundworkGameTests` (the vein now needs blood; the lode pays 4 x 12 mL). `dispelRefundIsSyncedAndRecorded` now asserts 90%.
- `bloodBindingHalvesRecognizedBossDuration` now casts up to 20 times until a hold lands, because a boss binding can slip (15% on a full Wither). It still checks the 60-tick boss length. The slip is the ruling, so this is not a weakening.
- Snapshots: `frozen/registry-ids.txt` gained `minecraft:data_component_type hemomancy:conjure_paid`. `command/hemo-command-fingerprints.txt` lines 44 and 45: `absolute_stillness` cost 300.0d to 70.0d, nothing else.
- `python gradle/source-text-impact-map.py` regenerated the impact map.

### Docs

- Updated: `docs/HEMOMANCY_REFERENCE.md` (regeneration and its config row, mining prices, Temper charge and filling, section 8 price rows, the Tithe and Endless Hour rows, the dispel refund, the Blood Moon effects, the Sculpting table rows for binding, Hewing and Mending, a new "Boss binding limit" paragraph), `docs/MANIPULATIONS_AND_CASTING.md`, `docs/BOSSES.md`, `docs/PLAYTHROUGH_TESTS.md` (PT-25 closed; D-25 stays the Chamber gateway's id), `wiki/Blood-Systems.md` (2 mL/s, Tithe debt).
- Rulings recorded under "Rulings that bind" in `MANIPULATIONS_AND_CASTING.md` (2026-11-14): LP-10 with CB-1, NQ-04-12, NQ-04-2, NQ-04-9, NQ-04-10, NQ-04-15, NQ-04-5, NQ-04-8, NQ-04-3, NQ-04-14, CB-11, NQ-04-1 with NQ-04-4 and 04-T6-store, NQ-04-13, NQ-04-11 with CQ-17, P03-dry-swap, P03-hour-death, CQ-8. The boss ones also in `BOSSES.md`. The older LP-9, LP-11, LP-14, LP-16, CQ-9, CB-4, CB-5, CB-7, CB-22 and CB-23 lines now say built.
- `docs/OPEN_QUESTIONS.md`: section 1 rows LP-10, NQ-04-12, NQ-04-2, NQ-04-9, NQ-04-10, NQ-04-15/PT-25, NQ-04-5, NQ-04-8, NQ-04-3, NQ-04-14, CB-11 deleted, with the two tables they emptied; the "answers that free the most work" lines shortened. Section 2 rows NQ-04-1, NQ-04-4, 04-T6-store, NQ-04-13, NQ-04-11/CQ-17, P03-dry-swap, P03-hour-death deleted; 04-T5-sheet kept for T5c. Section 3: every Cast economy row but 04 T5c deleted, and the area's count row reads 1 / 0 / 1. Section 4: PR-16 and NQ-04-4 (lash) are "Now"; six new B04 rows.
- Chunk 04 status table updated for T1 to T10, PT-25 and CQ-17.

### For the owner

- New lang keys: `message.hemomancy.crimson_tithe_manip.the_debt_is_carried` ("Your blood could not cover it. Hemorath is owed %s mL and takes it as your blood returns."), `message.hemomancy.crimson_tithe_manip.the_debt_is_still_owed` ("Hemorath is still owed %s mL. No new tithe until it is paid."), `message.hemomancy.crimson_tithe_manip.the_debt_is_paid` ("The tithe debt is paid."), `message.hemomancy.blood_binding.boss_slipped` ("The binding slips off %s."), `message.hemomancy.blood_binding.boss_spent` ("%s will not be bound again."), `blood_source.hemomancy.talaromyces_vein` ("Talaromyces Vein").
- Changed values: `hemomancy.manipulation.crimson_tithe.desc` (adds the carried debt), `skill.hemomancy.skill_sated_siphon.desc` ("A full body still draws from living targets for their Temper, but the blood it cannot hold is lost. With Sated Siphon that blood flows into your worn gourd instead."), `tooltip.hemomancy.talaromyces_minus.a_quiet_decomposer_shift_mining_ore` (adds "at 12 mL of blood for each further ore").
- Values I chose: the vein's source counts as spend (Hemorath debt) and is not reserve-protected. Sated Siphon fills the gourd up to its tier volume, with no kill-siphon multiplier. The Tithe debt is collected on the player tick every 20 ticks. A Ferric construct counts as its maker's for mending. The avatar's upkeep is read as mL a second, like the channels.
- Surprising: a hand or Staff that nobody paid to conjure (given by a fixture or a command) now refunds nothing on a dispel; it used to refund a flat 900 mL. The slip wait blocks only the held form; the R cast's own cooldown covers it. Another player's hold on a boss also stops your held form from binding that boss until the hold drops.
- A dev world keeps `bloodRegenRate = 1.0` until its server config file is edited or deleted.
- Rows in other areas this package also settles: "15 T5 (Thread Ripper price)" (Crimson Troupe) is now 300 mL in the registry; the row is left for its package. `docs/CIRCUS.md` may still quote 35 mL.
- No look changed, so no screenshots. No server or client left running.

### Verification (round 1)

Checked the tree against `refs/build2/B04-before`, the untracked backups in `build/build2/B04-untracked-before/`, and
`build/build2/B04-status-before.txt`. Every file changed since the restore point is in the log's list.

- Scope: nothing held or reversed was touched. No lash tuning file, HUD or held-manipulation design changed. T5c is
  not built. No payload was added, so the protocol stays as it was.
- Values match the answers: the five T5a prices, the NQ-04-5 channel set, Thread Ripper 300, vein 12 mL per extra
  ore, regen 2.0, burn bands 0.5/1/1.5/2 at 10/25/45 mL/s, R casts 2 to 8, bindings max HP / 100, slip
  clamp(maxHP/2000, 0.1, 0.6) x (0.5 + 0.5 x health), 60-tick wait, Hewing floor 36, dispel 90% of paid, Tithe debt
  under PlayerPersisted, Sated Siphon to the worn gourd, Blood Moon discount at D1+ in the Overworld only.
- Tests: each row has a new or rewritten test that would fail without it. The rewrites follow the rulings. The
  20-try boss binding cast still checks the 60-tick length, so it is not a weakening.
- Rules: blood goes through `BloodAccounting` (vein, Tithe debt). Player text is lang keys. The allow-list entries for
  `BossBindings` and `CrimsonTitheManip` are updated. No vanilla particle was added.
- Unit suite: BUILD SUCCESSFUL, 6,039 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B04-verify-unit.txt`).
- Combined GameTest run: "All 1929 required tests passed :)" (`build/build2/B04-verify-gametest.txt`).
- Playthrough suite not run again.
- Doc nit, not blocking: `docs/BOSSES.md` line 175, under "Not built yet", still lists LP-16 as waiting on NQ-04-2
  and NQ-04-3. That line should go.
- Not checked in play: whether a Saint that goes back to its tomb comes back as a new entity, so its count starts
  again (NQ-04-2 A). The count is saved on the entity.

## B05. The price sheet for the damaging R casts

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 04-T5-sheet | A new sheet written from the code for all 41 damaging R casts: damage read from each class, the rank target, the old and new price and how the damage was read. The dated audit csv files are not edited. | `docs/owner_answers/04-T5c-price-sheet.csv` |
| 04 T5c (LP-12), NQ-04-6 B | Targets 0.020, 0.025, 0.031, 0.039, 0.049 HP per mL by rank. A bolt is read on one body, an area cast on three, at full charge, with no conditional bonus. Price = damage counted / target, rounded to 5 mL, applied only where it cuts. Twelve casts cut in one edit: Insatiable Hunger 225 to 100, Cruor Javelin 200 to 160, Searing Lash 225 to 160, Blood Aneurysm 400 to 260, Bloom of Rot 500 to 290, Fruiting Contagion 400 to 290, Sanguine Magnetism 450 to 385, Rimebound Sentence 750 to 310, White Verdict 800 to 735, Funeral Bell 1000 to 430, Lodestone Collapse 900 to 610, Synaptic Storm 900 to 490. The other 25 are at or above target and keep their price. The four updrafts are left out. No lash or held-manipulation price moved (they are separate constants). | `common/init/ManipulationInit.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| Review client | Prices only. No look, screen or shader changed. |
| Regenerating `manipulations.csv` | The dated audit file is a snapshot and stays as it is (04-T5-sheet). The new sheet replaces it for these rows. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,039 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B05-unit.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: first run 1 of 1,929 failed, `obscuredmeleemobcanpursueandhitanearbyenemy` (a husk's melee pursuit timing; it casts nothing priced here). Run again unchanged: "All 1929 required tests passed :)" (`build/build2/B05-gametest2.txt`, `B05-gametest2-latest.log`; the first log is `B05-gametest1-latest.log`). Treat it as a flake to watch.
- Rewritten on purpose to the new prices: `ManipulationGapCombatStyleSourceTest` (Insatiable Hunger 100, Sanguine Magnetism 385) and `ManipulationCastFixGameTests` registry metadata (Synaptic Storm 490, White Verdict 735, Rimebound Sentence 310, Funeral Bell 430).
- The balance GameTests named in the plan quote none of the twelve prices and passed unchanged. No test added. No snapshot changed.

### Docs

- `docs/HEMOMANCY_REFERENCE.md` section 8.2: a "Damage prices by rank" paragraph and the twelve cost cells.
- `docs/MANIPULATIONS_AND_CASTING.md`: LP-12 now built; NQ-04-6 and 04-T5-sheet recorded under "Rulings that bind" (Cast economy, 2026-11-14, built 2026-11-15); NQ-04-6 dropped from the open-questions pointer.
- `docs/OPEN_QUESTIONS.md`: section 1 row NQ-04-6/LP-12 deleted with its emptied table, and its line under "The answers that free the most work". Section 2 row 04-T5-sheet deleted with its emptied table. Section 3 row 04 T5c deleted with its emptied table; the area's count row reads 0 / 0 / 0. Section 4: new row "B05 (damage prices)".
- Chunk 04 status line for T5 now reads built.

### For the owner

- No lang key added or changed. No description quoted a price.
- Calls I made where the rule left room:
  - Funeral Bell is read at 7 a body, without the +2 per affliction, so it falls to 430. With five afflictions it would have stayed at 1000.
  - Blood Aneurysm counts as a bolt (8 on the aimed body; the 3 splash on others not counted), so 260. As an area cast it would have stayed at 400.
  - Grave Debt counts as an area cast (its mark bursts on the bodies around), so it stays at 325. As a bolt it would be 200.
  - Umbral Rend (up to six bodies) and Eclipse Well count as area casts and stay at 450 and 900.
  - Conditional bonuses (Gloam ambush, Reproof and Verdict on Illuminated, Lodestone on the crush, Rend on Obscured) are not counted anywhere.
- Insatiable Hunger drops the most for its rank (225 to 100). Its value is the healing cut, which the rule does not price.
- No look changed, so no screenshots. No server or client left running.

### Verification (round 1)

- Scope: the diff from `refs/build2/B05-before` touches only twelve cost numbers in `ManipulationInit`, the cost quotes in `ManipulationGapCombatStyleSourceTest` and `ManipulationCastFixGameTests`, the reference and topic docs, the chunk file and `OPEN_QUESTIONS.md`. No lash feel number, held-form price (the Searing Lash strand keeps its own 30 mL/s in `Sculptings`), HUD or held or reversed item moved.
- Values: every `new_cost_ml` in `04-T5c-price-sheet.csv` matches the registered cost (41 of 41 checked by script). The damage read for Funeral Bell, Rimebound Sentence, Insatiable Hunger, Lodestone Collapse, Synaptic Storm and White Verdict matches their classes. Storm 490 and Collapse 610 match the plan's NQ-04-6 B estimate. The `HEMOMANCY_REFERENCE.md` table shows the twelve new prices.
- Note: "rounded to 5 mL" was read as nearest 5, so five cuts sit just under target (Funeral Bell 0.0488, Rimebound 0.0387, Blood Aneurysm 0.0308, Searing Lash and Cruor Javelin exactly on it). Rounding down would put all of them at or over. The owner may pick.
- Tests: six of the twelve new prices are pinned by a test (Insatiable Hunger, Sanguine Magnetism, Synaptic Storm, White Verdict, Rimebound Sentence, Funeral Bell). Blood Aneurysm, Bloom of Rot, Fruiting Contagion, Cruor Javelin, Searing Lash and Lodestone Collapse have no test that quotes their cost. No test was weakened or removed.
- Unit suite: `BUILD SUCCESSFUL`, 6,039 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B05-verify-unit.txt`).
- Combined GameTest run: "All 1929 required tests passed :)", `BUILD SUCCESSFUL` (`build/build2/B05-verify-gametest.txt`). The husk pursuit flake did not recur.
- Docs: the log matches the tree. Only this package's rows left `OPEN_QUESTIONS.md`; the area count row for Cast economy was also set to 0 / 0 / 0. Rulings are recorded in `MANIPULATIONS_AND_CASTING.md`.
- No review client: no look changed. No server or client left running.

## B06. Living blood: recipes, gourds, spikes, vessels, brazier, stand-in, repeated drinks

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 01 T2 (CB-13) | The eight `enzyme_fruiting` recipes cost 150 mL an enzyme: `blood_per_tick` 0.25 to 0.0625, `duration` stays 2,400. The code default follows. The Lantern keeps its 1,200 mL buffer (now eight enzymes' worth). The Alchemist's `enzyme_uses.culture`, the Lantern's material text and the brief say 150. | `recipe/enzyme_fruiting/*.json`, `EnzymeFruitingRecipe.java`, `MycelialLanternBlockEntity.java` |
| 01 T2 (CB-14) | Rendering chances: blaze powder, hematic iron scrap, packed ice, spider eye 0.5 to 0.25; bone, charcoal, coal, glowstone dust, ink sac, rotten flesh 0.25 to 0.10. | `recipe/rendering/*.json` |
| 01 T2 (CB-17), NQ-01-18 | `memory_blood_rush`: blood 350 to 100, enzymes Animus 1 and Flammeus 1 (it was one of each but Lux). | `recipe/memory_weaving/memory_blood_rush.json` |
| 01 T2 (LP-5), NQ-01-14 | Morphic Nectar turns 1 Hematic Iron Scrap into 1 powder (was 2). | `recipe/morphic_nectar/hematic_iron_scrap_to_powder.json` |
| 01 T3 (LP-8) | A JVM test pins `BloodLossEffect.MOB_FORMATION_CHANCE` at 0.0002. | `BloodLossFormationChanceTest.java` |
| 01 T4 (FX-1, CN-12), NQ-01-11 | A suture draws only from the worn gourd (`findOpenWornGourd`; the main and off hand are no longer read). The reference sentence that said otherwise is fixed. | `BloodRoutingHelper.java` |
| 01 T4 (fault) | Already done by B02 (the clinical test runs on the worn gourd). The row is closed. | - |
| 01 T5 (FX-2) tests | The Vicar's four flasks carry a stamp (added to the founder reception retry test). The Body Answers flask is stamped when given. A Thirster killed by a player drops stamped flasks and jugs. | `BloodInjectionGameTests.java`, `LivingBloodGameTests.java` |
| 01 T5 (FX-2 stacking, ingredient) | `bottled_at` is rounded down to the minute (`CoagulationRules.bottledStamp`). The Alembic distils a Flask into vials by its fresh share, rounded to the nearest vial (`CoagulationRules.freshCount`: 2 fresh, 1 half clotted, 0 clotted). Covenant Mantle and Morphic Tonic stay as built. The jug test found that the `add_item` loot modifier ran after `stamp_bottled`, so mob jugs dropped unstamped; `AddItemModifier` now stamps what it adds. | `BloodyFlaskItem.java`, `CoagulationRules.java`, `GhastlyAlembicBlockEntity.java`, `AddItemModifier.java` |
| 01 T6 (CQ-3), NQ-01-15 | A player's hanging scab spike records its placer, keyed by the spike's root, in a new chunk attachment `hemomancy:scab_spike_placers` (`ScabSpikePlacers`). A block added below keeps the first placer; breaking the root forgets it. `ScabDrip` asks `BloodDrawRights.mayDraw(level, placer, tank)` for every source, gutters included. A spike with no placer drips only from tanks nobody owns. | `ScabSpikePlacers.java`, `ScabDrip.java`, `BloodScabPointedDripstoneBlock.java`, `HemoAttachmentTypes.java`, `HemoCapabilityAccess.java` |
| 01 T6 (CQ-4), NQ-01-19 | `SpecimenVessels.mayHandle`: only the owner and their bloodline take a husk, lead a creature in, or sneak-pick up a jar or vat. A stranger gets `message.hemomancy.living_blood.not_yours`. An unowned vessel is open to all. Breaking stays possible. | `SpecimenVessels.java`, `SpecimenJarBlock.java`, `SpecimenVatBlock.java` |
| 01 T7 (CQ-5), NQ-01-12 | `bone_meal.json` needs Vital Effusion, `flint.json` Pyretic Forge. Every brazier transmutation names a Sculpting now. | `recipe/brazier_transmutation/` |
| 01 T7 (CB-18), NQ-01-13 | New optional recipe field `catalyst_count` (1 to 64, default 1; all are spent). Packed ice takes 3 more ice in the off hand, blue ice 3 more packed ice; blood stays 30 and 60 mL. The rite checks the count, spends it, and says `needs_catalysts` when short. JEI shows the count on the off-hand slot with a new tooltip. The recipe stream codec gained a VarInt; it is recipe sync, not a mod payload, so `PROTOCOL_VERSION` stays. | `BrazierTransmutationRecipe.java`, `BrazierTransmutationRecipeSerializer.java`, `BrazierTransmutationRite.java`, `BrazierTransmutationRecipeCategory.java` |
| 01 T8 (CN-9), NQ-01-16 | With the owner offline, the nearest bloodline member within 16 blocks of the machine (`BloodRoutingRules.STAND_IN_RANGE`), alive, D3 or higher, blood awake, stands in. They pay through `BloodAccounting.payUpkeep` (new source `blood_routing_kin`) at 8 mL a tick, above the 50% floor, with no gourd or pool draw. New link state `KIN_DRAWING`, appended to `ReservoirReadout.Link`; it reads as drawing. New Gauge and bar lines. | `BloodRoutingHelper.java`, `BloodRoutingRules.java`, `ReservoirReadout.java`, `BloodVolumeBarWidget.java` |
| 01 T9 (CL-6), NQ-01-9, NQ-01-10 | `CheapBloodInfusionRules.yieldShare`: sober 100%, then 80, 60, 40, 25% by the Blood Drunkenness level the drinker already has. The 3-minute effect is the reset. The lost share is gone; a clotted share still breaks out as whole Formations. The +15% cast cost a level stays. Action bar line when a vessel gave less; a tooltip line on every Rock, Flask and Jug. `CheapBloodInfusionHelperTest` moved to JUnit (the main version is in `removed_assets/`). | `CheapBloodInfusionRules.java`, `CheapBloodInfusionHelper.java`, `BloodyFlaskItem.java` |
| 01 T11 step 1 (CB-28), NQ-01-20 | The price sheet: 69 recipes, 101 Formations, each row with its other inputs, Degree, blood cost and Formation price at 150 mL (D1 to D2, and ungated crafting) or 100 mL (from D3). Blood Structures do spend their held Formation (`BloodStructureFeedManager.completeFeed` shrinks it), and 11 Working crafts spend a Formation as their Focus medium. | `docs/owner_answers/01-T11-formation-price-sheet.csv` |

### Skipped

| Row or part | Why |
| --- | --- |
| 01 T10, 01 T11 steps 2 and 3 | The next two packages, per the brief. |
| "Which 4 recipes were already rebalanced" (T11 step 1) | Not traceable. The 2026-11-11 audit says 71 name Formations and 67 were never rebalanced; no file says which 4. After LP-1 removed the two that made Formations, 69 remain and all are on the sheet. |
| Review client | No look or shader changed. The only screen change is a count on JEI's off-hand slot and new tooltip lines. No client launched. |
| Restore point for untracked files | `refs/build2/B06-before` covers tracked files only. Many files this package edited are untracked (for example `CORE_LOOP.md`, `LivingBloodGameTests.java`, `ScabDrip.java`, the brazier recipes) and have no backup. The tree is green, so nothing needed to be put back. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,050 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B06-unit4.txt`). One earlier run failed `LivingWorkshopResourceTest` only because I edited lang while it ran.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1938 required tests passed :)" (`build/build2/B06-gametest2.txt`, `B06-gametest2-latest.log`). 1,929 after B05, plus 9 new. The first run failed two: the mob jug (a real FX-2 bug, fixed in `AddItemModifier`) and the drip first-drop test (rewritten, below).
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B06-playthrough-optional.txt`).
- `./gradlew.bat runClinicalGameTestServer --offline --console=plain`: "All 132 required tests passed :)" (`build/build2/B06-clinical.txt`).
- Added JVM: `LivingBloodPriceDataTest` (Blood Rush price, nectar scrap), `RenderingRecipeResourceTest.renderingYieldsSitInTheTwoRuledBands` and `.everyEnzymeFruitsForOneHundredFiftyMl`, `BloodLossFormationChanceTest`, `CoagulationRulesTest` (stamp to the minute, fresh-share count), `BloodRoutingStandInRulesTest`, `CheapBloodInfusionHelperTest.repeatedDrinksGiveLessAndLess`.
- Added GameTests: `LivingBloodGameTests.aSutureDrawsOnlyFromTheWornGourd`, `.repeatedDrinksGiveLessAndLess` (250, 200, 150, 100, 62.5 mL, then 250 sober), `.mobDropJugsCarryABottledStamp`, `.theBodyAnswersFlaskIsStampedWhenGiven`, `.aHalfClottedFlaskDistilsToOneVial`, `.kinStandsInForAnOfflineOwner`; `FarmingGameTests.aSpikeDripsOnlyFromATankItsPlacerMayDraw`; `MenagerieGameTests.aStrangerMayNotEmptyFillOrLiftAnotherKeepersVessel`; `FirstConversionsGameTests.iceWantsThreeMoreInTheOffHand`.
- Rewritten on purpose to the rulings: `aBoneGivesBoneMealAndSporesWhateverIsSelected` became `aBoneGivesBoneMealAndSporesUnderVitalEffusion` (CQ-5: with nothing selected the bone stays, and gravel needs Pyretic Forge). `FirstConversionsDataTest` "three Sculptings and none" became "each under a Sculpting", with the ice chain pinned. `OnlookerPayloadsTest.everyStalledReasonReadsAsStalledAndKeepsItsStoredId` counts `KIN_DRAWING` as drawing (CN-9) and checks it is appended. `anUnstampedFlaskIsStampedBeforeItPours` expects the stamp rounded to the minute. `aDripFromAnOwnedTankSeatsTheOwnersCondenserWhichRevertsWhenSpent` and `aDripFromYourOwnTankOntoSoilIsTheFirstDrop` record the owner as the spike's placer (CQ-3: a spike nobody placed no longer drips from an owned tank). `LivingWorkshopResourceTest` pins "150 blood over 2,400 ticks". `CheapBloodInfusionHelperTest` moved from a main method to JUnit with the same cases.
- Snapshot: `frozen/registry-ids.txt` gained `neoforge:attachment_types hemomancy:scab_spike_placers`.
- `python gradle/source-text-impact-map.py` regenerated the impact map.

### Docs

- Updated: `docs/CORE_LOOP.md` (living blood rulings marked built, a new "Living blood (2026-11-14 answers and defaults, built 2026-11-15)" block, a "Build 2, B06" calls line, stale gap lines removed, the open-questions pointer), `docs/HEMOMANCY_REFERENCE.md` (the drip, menagerie vessels, ownership, owner draw and stand-in, Lantern size, carried blood and repeated drinks, the no-free-Formations paragraph, the brazier recipe format and its twelve recipes, Temper catalysts and rendering, the enzyme fruiting rows), `wiki/Blood-Systems.md`, `docs/NPC_VOICES.md` (the Alchemist's fixed text is now "150 blood over 2,400 ticks"), the Liber pages `brazier_transmutation.json` and `fed_ground.json`, the chunk 01 status table.
- Rulings recorded under "Rulings that bind" in `docs/CORE_LOOP.md`: CB-13, CB-14 (answered 2026-11-14), NQ-01-9, NQ-01-10, NQ-01-11, NQ-01-12, NQ-01-13, NQ-01-14, NQ-01-15, NQ-01-16, NQ-01-18, NQ-01-19, FX-2 stacking and ingredient; CL-6, CN-9, CQ-3, CQ-4, CQ-5, CB-17, CB-18 marked built; CB-28 notes the sheet.
- `docs/OPEN_QUESTIONS.md`: section 1 rows NQ-01-12, NQ-01-13 (with their emptied 1.2 table), NQ-01-9, CB-13, CB-14 deleted. Section 2 rows NQ-01-10, NQ-01-11, NQ-01-14, NQ-01-15, NQ-01-16, NQ-01-18, NQ-01-19, FX-2 (stacking), FX-2 (ingredient) deleted. Section 3: every living blood row but 01 T10 and 01 T11 steps 2 and 3 deleted (01 T4 (fault) included, done by B02); those two now read ready; the area's count row reads 2 / 2 / 0. Section 4: the CB-28 row names the sheet and is "now"; four new rows B06 (CL-6), B06 (brazier), B06 (Lantern, rendering), B06 (rights).

### For the owner

- New lang keys: `message.hemomancy.blood_drunkenness.gave_less` ("Foreign blood clouds the rite. The vessel gave only %s%% of its blood."), `tooltip.hemomancy.bloody_flask.repeated` ("Gives less if drunk again within three minutes."), `message.hemomancy.brazier_transmutation.needs_catalysts` ("The fire wants %s %s in your off hand."), `hemomancy.jei.brazier_transmutation.catalysts.tip` ("Hold %s of these in your off hand. All are spent."), `message.hemomancy.living_blood.feed.drawing.kin` ("Sutured. Its owner is offline, so it drinks from their kin nearby."), `screen.hemomancy.blood_volume_bar_widget.drawing.kin` ("Sutured: its owner is offline, so it drinks from their kin."), `blood_source.hemomancy.blood_routing_kin` ("Kin Suture").
- Changed values: `hemomancy.alchemist.enzyme_uses.culture` (600 to 150 blood), `material.hemomancy.mycelial_lantern.desc` (600 to 150 blood), `hemomancy.alchemist.craft.fed.drip.line1` (adds "It only takes blood whoever hung it may draw, so hang your own.", written to the Alchemist's sheet: procedure with the reason attached). The Liber pages `brazier_transmutation` and `fed_ground` are rewritten for the new rules.
- Values I chose: a distilled Flask rounds to the nearest vial and its clot is lost in the Alembic; a kin stand-in draws at the plain 8 mL a tick with no skill bonus, gourd or pool; `catalyst_count` is capped at 64; the Lantern keeps its 1,200 mL buffer.
- Surprising: mob-drop jugs were never stamped, because the `add_item` loot modifier ran after `stamp_bottled`; fixed. Blood Thralls drawing on a suture also reach the kin stand-in, since couriers use the same draw. A spike placed before this build has no placer, so it now drips only from unowned tanks; re-place spikes in a dev world.
- No look changed, so no screenshots. No server or client left running.

### Verification (round 1)

- Scope: the diff from `refs/build2/B06-before` and the new untracked files stay inside the living blood rows. No jet, spray or plant kit, stain run, lash feel number, HUD design or other held or reversed item moved. 01 T10 and 01 T11 steps 2 and 3 are not built.
- Values: the eight `enzyme_fruiting` recipes are 0.0625 mL a tick over 2,400 ticks (150 mL). Rendering is 0.25 for blaze powder, scrap, packed ice and spider eye, and 0.10 for bone, charcoal, coal, glowstone dust, ink sac and rotten flesh. `memory_blood_rush` is 100 mL with Animus 1 and Flammeus 1. The nectar scrap gives 1 powder. Bone meal names Vital Effusion and flint Pyretic Forge. Packed and blue ice take `catalyst_count` 3 of the same at 30 and 60 mL. `yieldShare` gives 100, 80, 60, 40, 25% and Blood Drunkenness lasts 3 minutes, so it resets after 3 minutes without a drink; it covers Rock, Flask and Jug (all `BloodyFlaskItem`). The stand-in is kin within 16 blocks, D3, alive, blood awake, paid through `payUpkeep` above the 50% floor. The suture reads only the worn gourd. The price sheet has 69 rows and 101 Formations, at 150 mL with no gate or D1 to D2 and 100 mL from D3 (checked by script).
- Tests: each built row has a test that would fail without it (the JVM and GameTest names in the Tests list above are all present). The old `CheapBloodInfusionHelperTest` cases all survive in the JUnit version; the main-method copy is in `removed_assets/`. The rewritten tests follow CQ-3, CQ-5 and CN-9; none was weakened.
- Unit suite: `BUILD SUCCESSFUL`, 6,050 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B06-verify-unit.txt`).
- Combined GameTest run: "All 1938 required tests passed :)" (`build/build2/B06-verify-gametest.txt`, `B06-verify-gametest-latest.log`).
- Clinical suite: "All 132 required tests passed :)" (`build/build2/B06-verify-clinical.txt`). The two baseline clinical failures no longer fail.
- Rules: new player text is all `en_us.json` keys; the kin draw uses `BloodAccounting.payUpkeep` with a named source; no payload changed (the brazier recipe stream codec is recipe sync, so `PROTOCOL_VERSION` stays); no vanilla particle added.
- Docs (non-blocking): `docs/HEMOMANCY_REFERENCE.md` line 4428, under the suture source priority, still says the gourd is read from "scar slot, main hand, then offhand". It should say only the worn gourd (NQ-01-11). Only this area's rows left `docs/OPEN_QUESTIONS.md`, and the rulings sit in `docs/CORE_LOOP.md`.
- No review client: no look changed. No server or client left running.

## B07. Living blood: clot takes room and pours revive it

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 01 T10 (CQ-6, LP-3), NQ-01-2 | A pure rule, `CoagulationRules.pour`: while a tank holds clot, half of a pour (`clotReviveShare` 0.5) is spent on the clot and used up, each spent mL waking 2 mL (`clotReviveYield` 2.0); the rest goes in fresh. What does not fit as fresh blood goes to the clot too, so the player is never charged for blood the tank cannot use. `BlockBloodVolume.revive` applies it. Projection (`IBloodReservoir.takeProjectedBlood`) uses it and charges only what was taken. The owner's example (5,000 fresh, 1,000 clot, 2,000 room, pour 1,000) gives 6,500 fresh and no clot. | `CoagulationRules.java`, `BlockBloodVolume.java`, `IBloodReservoir.java` |
| NQ-01-3 | `stampFed` and `freshen` are gone. A fill blends the tank's age by volume (`CoagulationRules.blendedAge`): 1 mL into 8,000 changes nothing. | `BlockBloodVolume.java` |
| NQ-01-4 | Only Projection and the suture's draw revive. Flask and gourd pours, gutters and couriers use `fill` into free room. The Condenser keeps its plain `fill`, so it never revives. | `IBloodReservoir.java`, `BloodRoutingHelper.java` |
| NQ-01-5 | A Blood Thrall keeps the age of what it carries (`CarriedAge` in its save data, blended by volume as it loads; blood drawn from the owner is age 0) and delivers it through `BlockBloodVolume.fillAged`, never waking clot. No payload changed. | `BloodThrallEntity.java`, `ThrallCourierSource.java` |
| NQ-01-6 | A rite-held station's clock pauses (`ReservoirBlood.riteHeld`, `BlockBloodVolume.pause`). `ReservoirBlood.held` now means a living vessel only, so a rite-held station no longer reads as "Held alive". | `ReservoirBlood.java` |
| LP-4, NQ-01-7 | `keepBufferFresh` and `FRESHEN_INTERVAL_TICKS` are gone. The suture's draw revives through the same split at `suturedClotReviveYield` 4.0, and the request is capped to what the buffer can take before the owner pays. `calculateReserveRequest` measures the reserve against the whole tank, clot included, so a buffer whose clot fills its room still asks. An idle 600 mL buffer costs its owner about 15 mL a minute. | `BloodRoutingHelper.java`, `BloodRoutingRules.java` |
| NQ-01-8 | A tank with no free room spends the whole pour on reviving (a case of the overflow rule above). | `CoagulationRules.java` |
| Config | New server section `living_blood`: `clotReviveShare` 0.5 (0 to 1), `clotReviveYield` 2.0 (0 to 64), `suturedClotReviveYield` 4.0 (0 to 64). | `HemoServerConfig.java` |
| Gauge and bar | The Gauge adds a line while a tank holds clot, with the three live values. The station bar tooltip adds a line under the clot amount. The suture's "drawing" Gauge line no longer says the buffer stays fresh. | `ReservoirReadout.java`, `BloodVolumeBarWidget.java`, `en_us.json` |
| Teaching | The Liber page "Living Blood" is rewritten for the revive rule, age blending, couriers, the suture and rite holds. Three Alchemist lines are rewritten. | `the_hematic_order/pages/living_blood.json`, `en_us.json` |

### Skipped

| Row or part | Why |
| --- | --- |
| `docs/core_loop/03-living-blood.md` | The core-loop track files stay put until the conflict answers are in (CQ-14), per the last pass. |
| Review client | No look, screen layout or shader changed. The only screen change is one tooltip line on the station bar. No client launched. |
| Restore point for untracked files | `refs/build2/B07-before` covers tracked files only. Copies of every file this package edited are in `build/build2/B07-backup/`. |
| Resonant Forge refund | A cancelled Forge operation refunds its reserved blood with a plain `fill`, which blends at age 0. Not in the row; left as it is. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,063 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B07-unit1.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1941 required tests passed :)" (`build/build2/B07-gametest1.txt`, `B07-gametest1-latest.log`). 1,938 after B06, plus 3 new.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B07-playthrough-optional.txt`).
- Added JVM: `CoagulationRulesTest` (the owner's example, no room, clot runs out, a pour takes only what the tank can use, the 4:1 suture, config defaults, age blending); `BlockBloodVolumeTest` (`aTopUpBlendsTheAgeInsteadOfMakingTheTankNew`, `aRevivingPourWakesClotAndBlendsTheAge`, `aFullClottedTankStillRevives`, `aCourierDeliveryKeepsItsAgeAndWakesNothing`, `aPausedClockKeepsTheBloodAsOldAsItWentIn`); new `SuturedBufferReviveRulesTest` (a buffer whose clot fills its room still asks; an idle buffer pays a quarter of what clots).
- Added GameTests: `LivingBloodGameTests.projectionIntoAClottedTankWakesTheClot`, `.aFullClottedTankRevivesUnderProjection` (also checks the Gauge line), `.aCourierDeliveryKeepsTheAgeOfItsSourceTank` (with a thrall save round trip).
- Rewritten on purpose to the rulings: `BlockBloodVolumeTest.aLiveLineFreshensWithoutAddingBloodAndOnlyOnceStale` removed with `freshen` (LP-4); `feedingFreshensTheWholeTankButClotStays` became `clotStaysUnderAPlainFill` (NQ-01-3). `LivingBloodGameTests.projectionFreshensTheTank` became `projectionBlendsTheAgeOfACleanTank` (NQ-01-3). `aSuturedMachineDrinksFromItsOwnerNearbyAndStallsWhenTheyLeave` now checks a full buffer is not restamped for free and that the suture spends clot / 4 to wake its clot (NQ-01-7). `aStationHeldByAnUpgradeRiteDoesNotClot` now waits 20 ticks under the hold and checks the tank kept its age (NQ-01-6).

### Docs

- Updated: `docs/CORE_LOOP.md` (rule 2, CQ-6, LP-3 and LP-4 marked built, NQ-01-2 to NQ-01-8 recorded, the open-questions pointer), `docs/HEMOMANCY_REFERENCE.md` ("Living blood": feeding blends the age, pours revive clot, couriers keep the age, rite holds pause, the suture revive; the routing paragraph), `docs/STATION_UPGRADES.md` (rite hold), `docs/core_loop/PLAYTEST.md` (the two "not built yet" lines are now play questions), `wiki/Blood-Systems.md`, `wiki/Configuration.md` (the `living_blood` section), the chunk 01 status table.
- Rulings recorded under "Rulings that bind" in `docs/CORE_LOOP.md`: NQ-01-2 and NQ-01-7 (answered 2026-11-14, built 2026-11-15), NQ-01-3, NQ-01-4, NQ-01-5, NQ-01-6, NQ-01-8; CQ-6, LP-3, LP-4 marked built.
- `docs/OPEN_QUESTIONS.md`: section 1 rows NQ-01-2 and NQ-01-7, and the "NQ-01-2 and NQ-01-7" row of the answers that free the most work, deleted. Section 2 rows NQ-01-3, NQ-01-4, NQ-01-5, NQ-01-6, NQ-01-8 deleted, and their mention in the section intro. Section 3: the 01 T10 row deleted; the area's count row reads 1 / 1 / 0. Section 4: PR-15 is "now"; new row B07 (clot revive).

### For the owner

- New lang keys: `message.hemomancy.living_blood.gauge.revive` ("While it holds clot, %s%% of what you project in wakes the clot, %s mL of clot for each mL spent. Its suture wakes %s mL for each mL it draws."), `screen.hemomancy.blood_volume_bar_widget.revive` ("Blood projected in wakes some of the clot.").
- Changed lines: `message.hemomancy.living_blood.feed.drawing` ("Sutured. It drinks from its owner, who also pays to wake its clot."); the Alchemist's `hemomancy.alchemist.craft.living_blood.line2`, `.keepers.line1` and `.keepers.line2`, written to the Alchemist's sheet (procedure with the reason attached). These are three of the 16 T5 ruling-driven lines (CQ-6, LP-3, LP-4, CN-9); `.keepers.line2` now also names the kin stand-in. The 16 T5 row was left alone, as it is another area's.
- My call: blood that does not fit as fresh also goes to the clot. With no room this is NQ-01-8; with a little room it means a pour is never charged for blood the tank cannot use. Say if you want the strict half instead.
- My call: a Thrall's carried blood does not age while carried (the thrall is alive). It keeps the age it was taken at.
- My call: a rite-held station no longer reads "Held alive" on the Gauge; it reads by its own freshness, which does not move during the rite.
- Surprising: dev-world tanks and buffers that relied on the free restamp will now clot. A sutured buffer near you costs a little blood every minute once it is past five minutes old.
- No look changed, so no screenshots. No server or client left running.

### Verification (round 1)

- Scope: the tracked diff from `refs/build2/B07-before`, the backup copies in `build/build2/B07-backup/` and the one new file (`SuturedBufferReviveRulesTest.java`) stay inside 01 T10 and NQ-01-2 to NQ-01-8. No jet, spray or plant kit, stain run, lash feel number, HUD design or other held or reversed item moved.
- Values: `clotReviveShare` 0.5, `clotReviveYield` 2.0 and `suturedClotReviveYield` 4.0 are server config keys with those defaults. The owner's example (5,000 fresh, 1,000 clot, 2,000 room, pour 1,000) gives 6,500 fresh and no clot. An idle 600 mL buffer pays 15 mL for a minute of clot. Fill blends age by volume; `stampFed`, `freshen`, `keepBufferFresh` and `FRESHEN_INTERVAL_TICKS` are gone. Projection and the suture draw revive; flask, gutter and courier pours use plain fill; the Condenser is untouched. A thrall keeps and saves `CarriedAge`; a rite hold calls `pause`. A full clotted tank spends the whole pour reviving.
- Tests: each built row has a JVM or GameTest that would fail without it. The removed `aLiveLineFreshensWithoutAddingBloodAndOnlyOnceStale` and the rewritten projection, suture and rite-hold tests follow LP-4, NQ-01-3, NQ-01-6 and NQ-01-7; the rite-hold test still checks that nothing clots. None was weakened.
- Unit suite: `BUILD SUCCESSFUL`, 6,063 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B07-verify-unit.txt`).
- Combined GameTest run: "All 1941 required tests passed :)" (`build/build2/B07-verify-gametest.txt`, `B07-verify-gametest-latest.log`).
- Clinical suite: "All 132 required tests passed :)" (`build/build2/B07-verify-clinical.txt`).
- Rules: new text is `en_us.json` keys; Projection still pays through `BloodAccounting.pay` and only for what the tank took; no payload changed, so `PROTOCOL_VERSION` stays; no vanilla particle added; nothing was deleted that needed `removed_assets/`.
- Docs: the log matches the tree. Only this area's rows left `docs/OPEN_QUESTIONS.md`, and the rulings sit under "Rulings that bind" in `docs/CORE_LOOP.md`.
- No review client: only one tooltip line changed. No server or client left running.

## B08. The Formation recipe pass

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 01 T11 steps 2 and 3 (CB-28), NQ-01-20 | Yardstick: one Formation is 150 mL at D1 to D2 and 100 mL from D3. Batch 1, early crafting: the blank Hematic Memory loses its four corner Formations (pattern ` B `, `BRB`, ` B `). It is 4 Blood Stained Stones and 1 Neurotic Enzyme, so a blank of crafted stones holds 4 Formations (600 mL), not 8 (1,200 mL). It is spent on every weave. Vicar's kit (1) and Gauge (1) kept. | `recipe/hematic_memory.json` |
| Batch 2, tinctures | All 16 kept at 1 Formation. The Formation is 150 mL beside 2,500 or 4,000 mL of blood, under 6%. | none |
| Batch 3, gourd fills | All 4 kept at 4 Formations for 200 mL (NQ-01-1 as accepted): a loss on purpose, the outlet for found Formations. | none |
| Batch 4, other crafting | Charm of Vascularium 4 to 2 (300 mL; two more Bleeding Bulbs, 3 in all). Morphling Jar 4 to 2 (6 glass). Sanguine Conduit 4 to 2 (4 scrap, side slots empty). Self-Reflection Mirror 4 to 2 (4 scrap, side slots empty). Vitality Chalice 3 to 2 (a Hematic Iron Scrap is the foot, 2 in all). The three 2-Formation crafts (Ganglionic Harness, Hematic Suture Node, Terrestrial Speculum) and the thirteen 1-Formation crafts kept. Order kept: the blank is the dearest craft at 4, then the 2s, then the 1s. | `recipe/charm_of_vascularium.json`, `morphling_jar.json`, `sanguine_conduit.json`, `self_reflection_mirror.json`, `vitality_chalice.json` |
| Batch 5, grafts | All 7 kept at 1. Each also takes a blank, so a graft falls from 9 Formations to 5. | none |
| Batch 6, Blood Structures | All 5 kept at 1 held Formation. | none |
| Batch 7, rites and Working crafts | All 15 kept at 1 Focus medium (100 mL from D3), small beside the offerings. | none |
| The sheet | `01-T11-formation-price-sheet.csv` gains `b08_formations`, `b08_formations_ml` and `b08_change` for every row. Loose Formations across the 69 recipes: 101 to 88. | `docs/owner_answers/01-T11-formation-price-sheet.csv` |
| Teaching | The Mnemonist's two recipe lines, their dialogue brief, the Liber page "Memory Weaving", the wiki and the voice sheet's fixed text follow the new blank. | `en_us.json`, `dialogue_briefs/mnemonist_antecedent.json`, `manipulations/pages/memory_weaving.json`, `wiki/Getting-Started.md`, `docs/NPC_VOICES.md` |

Changed recipes, old and new Formation cost (mL at the D1 to D2 price):

| Recipe | Old | New |
| --- | --- | --- |
| Hematic Memory (blank) | 4 loose + 4 in stones = 8 (1,200 mL) | 0 loose + 4 in stones = 4 (600 mL) |
| Charm of Vascularium | 4 (600 mL) | 2 (300 mL) |
| Morphling Jar | 4 (600 mL) | 2 (300 mL) |
| Sanguine Conduit | 4 (600 mL) | 2 (300 mL) |
| Self-Reflection Mirror | 4 (600 mL) | 2 (300 mL) |
| Vitality Chalice | 3 (450 mL) | 2 (300 mL) |
| Each Living Weapon Graft (through its blank) | 9 (1,350 mL) | 5 (750 mL) |

### Skipped

| Row or part | Why |
| --- | --- |
| Review client | No look, screen or shader changed. Only recipe data and text. No client launched. |
| Restore point for untracked files | `refs/build2/B08-before` covers tracked files only. Three edited docs are untracked: `docs/CORE_LOOP.md`, `docs/owner_answers/01-T11-formation-price-sheet.csv` and `docs/owner_answers/01-living-blood-and-containers.md`. Their B08 edits are text only and listed under Docs. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,063 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B08-unit1.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1941 required tests passed :)" (`build/build2/B08-gametest1.txt`, `B08-gametest1-latest.log`).
- `./gradlew.bat runClinicalGameTestServer --offline --console=plain`: "All 132 required tests passed :)" (`build/build2/B08-clinical.txt`).
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: first run, `wovenvesselturnin` failed (it still laid Formations in the corners). Fixed, second run "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B08-playthrough2.txt`, `B08-playthrough-optional.txt`).
- Rewritten on purpose to CB-28: `HarbingerHermitRoadMissionSourceTest` now pins "a Sanguine Formation and redstone" in the Mnemonist's lines instead of "four Sanguine Formations". `ClinicalBloodProgressionGameTests.craftedBlankCarriesWovenVesselIntoFirstLoomWeave` crafts the blank with empty corners. `PlaythroughInitiateGameTests.wovenVesselTurnIn` gives 4 Formations, not 8, and `craftBlank` no longer lays corner Formations. None was weakened: each still crafts the blank through the real recipe.

### Docs

- Updated: `docs/CORE_LOOP.md` (CB-28 marked built with the six changes; NQ-01-20 and NQ-01-1 recorded; the open-questions pointer reads "living blood none"), `docs/HEMOMANCY_REFERENCE.md` (the "No free Formations" paragraph, the Woven Vessel paragraph, the Hematic Memory item row), `docs/NPC_VOICES.md` (Mnemonist fixed text), `wiki/Getting-Started.md`, the chunk 01 status line for T11, the price sheet.
- Rulings recorded under "Rulings that bind" in `docs/CORE_LOOP.md`: CB-28 (built 2026-11-15), NQ-01-20 (answered 2026-11-14, built 2026-11-15), NQ-01-1 and LP-2 (accepted 2026-11-14, kept 2026-11-15).
- `docs/OPEN_QUESTIONS.md`: section 1 row NQ-01-20 deleted, section 2 row NQ-01-1 deleted, section 3 row "01 T11 steps 2 and 3" deleted; the three emptied living blood tables removed; the area's count row reads 0 / 0 / 0. Section 4: the CB-28 row now names the six changes and asks for a D1 to D3 craft session.
- `docs/CARDINAL_RITES.md` unchanged: no rite or Working craft changed.

### For the owner

- Changed lang keys: `hemomancy.mnemonist.woven_vessel.recipe.line1` ("Make one blank Hematic Memory from four Blood Stained Stones and one Neurotic Enzyme.") and `hemomancy.mnemonist.woven_vessel.recipe.line2` ("Find Blood Stained Stone in a Hermitage Remnant, or make each one from Venous Stone, a Sanguine Formation and redstone. Each crafted stone holds one formation, so a blank of crafted stones holds four. Chicken blood can yield Neurotic Enzyme, though some spins yield Vivacious instead."). Written to the Mnemonist's sheet: exact, in order, no question. No new keys.
- My calls: which recipes changed and the new shapes. The rule I used: a one-off craft a D1 to D3 player makes costs at most 2 Formations (300 mL), and the blank, which every weave spends, drops to 4. Recipes where the Formation is a small share of the cost (tinctures, structures, rites, crafts) kept their 1.
- My call: the Vitality Chalice ties the 4-Formation crafts at 2 rather than drop to 1, so it still costs more than the 1-Formation crafts.
- Surprising: the blank Hematic Memory no longer takes a loose Formation at all; its four come through the stones. A Remnant stone now saves a quarter of the blank's blood.
- No look changed, so no screenshots. No server or client left running.

### Verification (round 1)

- Scope: the tracked diff from `refs/build2/B08-before` holds six recipe files, the two Mnemonist lang lines and their brief, the Liber page, three test files and docs. The status diff against `build/build2/B08-status-before.txt` shows no new file. No held or reversed item, lash feel number or HUD design moved.
- Values: the six recipe files match the B08 table. The blank is ` B `, `BRB`, ` B ` with no Formation. The Charm, Jar, Conduit, Mirror and Chalice each hold 2 Formations. The price sheet's `b08` columns agree. The yardstick (150 mL to D2, 100 mL from D3) is the owner's NQ-01-20 answer, and the gourd fill stays at 4 for 200 mL per NQ-01-1.
- Tests: the blank's new shape is pinned by `ClinicalBloodProgressionGameTests` and `PlaythroughInitiateGameTests.wovenVesselTurnIn` (both craft through the real recipe with empty corners), and the lines by `HarbingerHermitRoadMissionSourceTest`. They were rewritten to the ruling, not weakened. The five other repriced crafts have no test pinning their Formation count. That is a gap but the plan named none.
- Unit suite: `BUILD SUCCESSFUL`, 6,063 tests, 0 failures, 0 errors, 0 skipped, `CardinalRitePurposeDataTest` and `WorkingCraftRosterDataTest` among them (`build/build2/B08-verify-unit.txt`).
- Combined GameTest run: "All 1941 required tests passed :)" (`build/build2/B08-verify-gametest.txt`).
- Playthrough suite: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known flake) (`build/build2/B08-verify-playthrough.txt`, `B08-verify-playthrough-optional.txt`).
- Rules: player text stays in `en_us.json` keys; no payload, blood path, delivery or particle changed; nothing was deleted that needed `removed_assets/`.
- Docs: the log matches the tree. Only this area's rows left `docs/OPEN_QUESTIONS.md`, and CB-28, NQ-01-20 and NQ-01-1 sit under "Rulings that bind" in `docs/CORE_LOOP.md`. No stale "eight formations" line is left in lang, books or wiki.
- No review client: no look changed. No server or client left running.

## B09. Ground network and farming

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 02 T1 (CQ-7, RV-46), NQ-02-2, NQ-02-1 | A Catalyst Feeder charges 15 mL for each catalyst it dissolves (`GroundRunRules.FEEDER_ML`), paid by whoever lit the run through `BloodAccounting.pay`, new source `ground_feeder` (WORLD, not a spend, reserve protected). With that player offline, dead, in another level, not active or short, it dissolves nothing and the run goes dark on time. No tank. Feed runs stay at 30 s a stage, no beat charge. | `common/world/ground/GroundNetwork.java`, `GroundRunRules.java`, `block/harbinger/ground/CatalystFeederBlock.java` |
| 02 T2 (CQ-43) and its section 2 default | One hot run burns at most 4 furnaces (`HOT_FURNACE_MAX`). The pulse walks the run nearest first and gives each lit cell a side mask (`GroundRunRules.furnaceSides`, saved as `furnace_sides` on `GroundRunSavedData.Lit`). A furnace set down later joins at the next pulse or feeder top-up. Campfires and candles are not capped. | `GroundRunRules.java`, `GroundRunSavedData.java`, `GroundNetwork.java` |
| 02 T4 (CQ-15), NQ-02-9 and its default | Furnace Veins costs 6 mL a pulse for each furnace in the bank with something to cook (`FurnaceVeinsSculpting.bloodPerPulse(ServerPlayer, VerbTarget)`, `pulsePrice`). A bank of 8 is 48 mL a pulse before the cost ledger. The client readout keeps the one-furnace price. No session map was needed: the target-aware price hook already existed. | `common/manipulation/flammeus/FurnaceVeinsSculpting.java` |
| 02 T5 (CL-14), NQ-02-3 A and its default | A vein moves blood only toward an end that draws (`GroundRunRules.drawDemand`; `veinTransfer` rewritten). Drawing: blood left the tank in the last 100 ticks (5 s, `VEIN_DRAW_WINDOW_TICKS`), or a machine under 600 mL (`BloodRoutingRules.DEFAULT_WORKING_RESERVE`). Stores never pull by level: new `IBloodReservoir.isBloodStore()` (living vessels by default, so the Tap and vats; the Venous Basin overrides; fillers delegate). The stamp is `LastDrawn` on `BlockBloodVolume`, set by `drain` and `subtractBloodVolume`; the vein moves blood with `takeAlongVein`, which stamps nothing. Both ends drawing level as before. Nothing drawing: nothing moves and no upkeep. | `GroundRunRules.java`, `GroundNetwork.java` (`tickNode`), `common/tile/BlockBloodVolume.java`, `IBloodReservoir.java`, `VenousBasinBlockEntity.java`, `FillerBlockEntity.java` |
| 02 T6 (CQ-44), NQ-02-5, gouge half | `BloodGougeBlock` is no longer waterloggable. Water that flows into its cell or is poured on it replaces it, and its clock goes. Rain and water beside it do nothing. | `block/harbinger/ground/BloodGougeBlock.java` |
| 02 T6 (CQ-44), NQ-02-4, vein half | Built. `LivingVeinBlock` holds a custom water state, `WATER` 0 to 9 (0 dry, 1 to 7 flowing, 8 source, 9 falling), as a `LiquidBlockContainer` and `BucketPickup`. Any water may flow onto it. New `MixinLivingVeinWater` on `FlowingFluid.tick`: for flowing water in a vein's cell it settles the same new water vanilla would, sets it on the vein instead of replacing the block, and spreads on. Water now flows over a vein and on past it, and drains off it when the source goes. A bucket places or takes a source. Filling in the last face, or withering, leaves the water. `GroundMarkBlock.without` keeps whatever fluid the cell held. | `LivingVeinBlock.java`, `mixin/core/MixinLivingVeinWater.java`, `hemomancy.mixins.json`, `GroundMarkBlock.java`, `GroundNetwork.java` (`asVein`, `asGouge`) |
| 02 T6 default | A wet vein that withers is washed out and leaves its water. A new vein starts dry. | `LivingVeinBlock.withered`, `GroundNetwork.asGouge` |
| 02 T7 (CB-26), NQ-02-7 | Kills count only up to 600 mL in a region (`WorldAnswerRules.KILL_SHARE_CAP_ML`, `killCounted`; `BloodSpilledLedger.spillKill` applies it after ward halving). Stains and chum are not capped. | `common/world/answer/WorldAnswerRules.java`, `WorldAnswer.java`, `BloodSpilledLedger.java` |
| 02 T8 (CB-3), NQ-02-8 | Tainted meat 20 to 10 mL a point (30 mL an item), raw fish 3 to 4, cooked fish 4 to 5. Tainted harvest stays 12. | `data/hemomancy/blood_food/tainted_meat.json`, `raw_fish.json`, `cooked_fish.json` |
| Teaching | Lines and pages that taught the old rules rewritten (keys under For the owner). Liber pages: The Ground Network, Living Veins and Basins, Second Sculptings, The Body and the Day. | `en_us.json`, `books/libersanguinium/...` |
| Review client | The water scene now uses living veins, which water runs over; the shot is renamed `vein-water`. | `src/gameTest/.../GroundNetworkClientReview.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| Restore point for untracked files | `refs/build2/B09-before` covers tracked files only, and nearly every ground network file is untracked. A tar of the untracked candidates is `build/build2/B09-backup/untracked.tar` (list in `untracked-list.txt`), plus `CORE_LOOP.md.orig`. New file: `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinLivingVeinWater.java`. |
| `docs/core_loop/06`, `07`, `10` track files | They stay put until the conflict answers are in (CQ-14), as in B07. |
| Dialogue-area rows in `OPEN_QUESTIONS.md` | The 16 T5 row and the Vicar and Vein-Mason line-work rows name lines rewritten here. They belong to the Dialogue area, so I left them. See For the owner. |
| `runSculptingGameTestServer` | Tried once to isolate two failures; it stops with "No test functions were given!". Not this package's; not fixed. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,066 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B09-unit3.txt`). 6,063 after B08, plus 3 new.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1950 required tests passed :)" (`build/build2/B09-gametest4.txt`, `B09-gametest4-latest.log`). 1,941 after B08, plus 9.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B09-playthrough-optional.txt`).
- Batch finding: with the 7 new ground network GameTests in the default batches, `mendingbeatsachippedanvilbackaftereightpulses` and `thecastpresentationfollowsthechannelnotthetarget` failed in two runs in a row (`B09-gametest1-latest.log`, `B09-gametest2-latest.log`). Those two share no code with this package. With the 7 tests in their own batch (`ground_network_draw_and_water`) everything passed, twice. So the two Sculpting session tests are sensitive to what shares their batch. Worth a look in a later housekeeping pass.
- Added JVM: `GroundRunRulesTest.aVeinMovesBloodOnlyTowardADrawingEnd` (replaces `aVeinLevelsTwoTanksWithoutOvershooting`, rewritten to CL-14 on purpose), `anEndDrawsWhenDrawnFromOrAMachineIsUnderItsReserve` (NQ-02-3 A), `aHotRunTakesTheFourNearestFurnaces` (CQ-43); `WorldAnswerRulesTest.killsStopCountingAtTheUneasyFloor` (NQ-02-7). Changed: `GroundRunSavedDataTest` round-trips `furnaceSides`; `BloodFoodDataTest.cookedIsWorthMoreAndTaintedMeatTheMost` renamed `...PaysTheMostPerPoint` and pins 4, 5 and 10 (NQ-02-8).
- Added GameTests: `aFeederStopsWhenItsOwnerIsAway` and `aFeederStopsWhenItsOwnerIsDry` (NQ-02-1); `aVeinWithNothingDrawingMovesNoBlood` (two basins, no siphon); `aDrawnTankPullsThroughTheVein` (also checks the source is not stamped and that flow stops after the window); `flowingWaterWashesAGougeAway` and `waterPouredOnAGougeWashesItAway` (NQ-02-5); `flowingWaterRunsOverALivingVein` (NQ-02-4: water flows past, the vein still pulses, the water drains off when the source goes); `waterPouredOnALivingVeinLiesOverIt` (bucket in and out); `aWaterloggedVeinFilledInLeavesItsWater` (moved from the gouge); `aWitheredVeinUnderWaterIsWashedOut` (T6 default); `DailyLoopGameTests.killsAloneNeverLiftARegionPastUneasy` (NQ-02-7).
- Flipped on purpose: `aGougeHoldsBackFlowingWater` and `aWaterloggedGougeFilledInLeavesItsWater` removed (CQ-44 and NQ-02-4 reverse P8's dam). `aFeederKeepsALitRunLit` now gives the run an owner and checks 15 mL per catalyst (an ownerless run is never topped up now). `SecondSculptingsGameTests.furnaceVeinsCooksABankWhileHeldAndGoesOutOnRelease` also checks the first pulse on a bank of two costs two furnaces. `DailyLoopGameTests.venisonCarriesAnimusAndTaintedMeatPaysTheMost` renamed `...PaysTenANutrition` and expects 30 mL, not 60. `aVeinMovesBloodOnlyWhileItsOwnerIsNear` is unchanged: both scriptoriums are machines under 600 mL, so both draw.
- Normalised: `ScriptoriumOperationCharacterisationGameTests` drops the new `LastDrawn` game-time stamp from its fingerprint, as it already drops `LastFed` and `SettledAt`. The golden file is unchanged.
- Allow-lists: `BloodAccountingGuardrailTest` lists `BlockBloodVolume` once each for `drain(` and `subtractBloodVolume(` (the stamp overrides). `player-block-edit-allow-list.txt` lists `LivingVeinBlock` with 3 raw edits (its own water state, and a bucket pickup inside the vanilla bucket path). `source-text-impact-map.csv` regenerated (one row).

### Docs

- Updated: `docs/CORE_LOOP.md` (rulings block and open-questions pointer), `docs/HEMOMANCY_REFERENCE.md` (P8 hot run and water paragraphs, gouges, feeder, living veins, meal table, kill count, the Furnace Veins row), `docs/MANIPULATIONS_AND_CASTING.md` (CQ-15 built), `wiki/Blood-Systems.md`, the chunk 02 status table.
- Rulings recorded under "Rulings that bind" in `docs/CORE_LOOP.md` (answered or accepted 2026-11-14, built 2026-11-15): CQ-7 with NQ-02-1 and NQ-02-2; CQ-43 with its default; CL-14 with NQ-02-3 and the T5 default; CQ-44 with NQ-02-4, NQ-02-5 and the T6 default; CQ-15 with NQ-02-9 and the T4 default; CB-26 with NQ-02-7; CB-3 with NQ-02-8.
- `docs/OPEN_QUESTIONS.md`: section 1 rows NQ-02-3, NQ-02-4, NQ-02-5 (1.2) and NQ-02-2, NQ-02-9, NQ-02-7, NQ-02-8 (1.4) deleted; section 2 rows NQ-02-1 and the T2, T4, T5 and T6 defaults deleted; section 3 rows 02 T1, T2, T4, T5, T6, T7, T8 deleted; the four emptied tables removed; the area's count row reads 0 / 0 / 0. Section 4: PR-18 and PR-23 now "Now" with what B09 changed; new rows "B09 (water)" and "B09 (Furnace Veins)".

### For the owner

- New lang key: `blood_source.hemomancy.ground_feeder` ("Catalyst Feeder").
- Changed lines (each to its speaker's sheet): Artificer `hemomancy.artificer.craft.hot_runs.heat.line1`, `.feeder.line1`, `.feeder.line2`, `hemomancy.artificer.item_inquiry.catalyst_feeder.line2`; Anchorite `hemomancy.anchorite.craft.ground_marks.cut.line2`, `.vein.line2` (no contractions); Vicar `hemomancy.vicar.item_inquiry.earthen_vein.line2`, `hemomancy.vicar.craft.day.meats.line2`; Mnemonist `hemomancy.mnemonist.craft.sculptings.mend.line2` (no question); JEI `hemomancy.jei.gouge_catalyst.hot`, `hemomancy.jei.gouge_catalyst.projection.tip`; `hemomancy.manipulation.furnace_veins.sculpting.project`. These cover the CB-3, CQ-7, CQ-43, CQ-44, CQ-15 and CL-14 lines of the Dialogue area's 16 T5 row, the Vicar's Earthen Vein line and the Vein-Mason's two lesson lines in the line-work table, so that area can shorten those rows.
- Values I chose: the draw window is 100 ticks (the accepted 5 s). A machine draws only up to 600 mL, not to full. When both ends draw they level by share as before. A furnace counts toward the four whether or not it holds anything (the four nearest furnace blocks). Falling water over a vein is its own value (9).
- Look to check: water over a lit vein run, `build/build2/B09-shots/ground-15-vein-water-60.png` (from `groundNetworkReviewClient`; the client got past loading and logged "captured 22"). The hot run with three furnaces is `build/build2/B09-shots/ground-11-furnaces-50.png`. The other new shots are in `build/ground-network-client-review/screenshots/`; the old ones moved to `build/build2/B09-old-review-shots/`.
- Surprising: a living vein no longer dams water at all, so a vein run cut along a stream bed sits under the water rather than holding it back. Gouges near water in old dev worlds wash away the first time water moves next to them.
- Surprising: the feeder now needs its lighter online. A lit hallway with a stocked feeder goes dark while you are logged out.
- No server or client left running.

### Verification (round 1)

- Pass. Checked the tree against `refs/build2/B09-before`, and the untracked ground files against `build/build2/B09-backup/untracked.tar`. The only new file is `MixinLivingVeinWater.java`.
- Scope: only area 02 rows. Nothing from the held list. No lash numbers, no HUD and no held manipulation touched. Jet, spray, plant and the stain run are still there.
- Values match the answers: feeder 15 mL a catalyst, paid by the lighter (`ground_feeder`, reserve protected, no top-up when away, dead or short). Furnace Veins 6 mL for each furnace with something to cook, on the server price hook `BloodManipulation` calls. Hot run cap 4, nearest first. Veins move only toward a drawing end: 100-tick window, machines under 600 mL, stores never pull. Gouges lose waterlogging. Veins hold a water state 0 to 9. Kills capped at 600 mL after ward halving. Meals: tainted meat 10, raw fish 4, cooked fish 5.
- Tests: each row has a JVM test or GameTest that fails without it. The removed gouge-dam tests and the changed meat and feeder tests follow the rulings on purpose. None was weakened.
- Unit suite: BUILD SUCCESSFUL, 6,066 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B09-verify-unit.txt`).
- Combined GameTest run: "All 1950 required tests passed :)" (`build/build2/B09-verify-gametest.txt`).
- Rules: blood goes through `BloodAccounting`. The guardrail and block-edit allow-list entries are narrow and have reasons. No payload, so no protocol bump. No new vanilla particle. The new lang key is in `en_us.json`.
- Docs: rulings are in `docs/CORE_LOOP.md`, and chunk 02 status matches. In `OPEN_QUESTIONS.md` only area 02 rows were deleted.
- Notes, not blocking: (1) the new GameTests run in their own batch (`ground_network_draw_and_water`) because two Sculpting session tests fail when they share a batch with them. That order sensitivity is still open. (2) The section 3 area row for Ground network now reads 0 / 0 / 0. (3) I looked at the water shot (`build/build2/B09-shots/ground-15-vein-water-60.png`, water over the lit veins) but did not launch a client again.

## B10. Menagerie, firsts, strand skills and the core-loop housekeeping

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 03 T5 (CL-12), NQ-03-1 A, NQ-03-2, NQ-03-3 A, NQ-03-4, CL-12 shape | A draw from an unread kind gives half its Temper (`BloodResearch.UNREAD_SHARE` = 0.5, `temperShare(read)`), from the first draw at any Degree, wild (`BloodAbsorptionItem.draw`, the whole `fillFromCreature` share and its inherited half) and kept (`KeptSpecimen.draw`, the pull and the inherited half; exposure records the reduced Temper). Blood, the creature's hurt, the Vigor a pull costs and the acclimation thresholds are unchanged; injection is unchanged. The cut keys on read (`knows`), Creative reads everything. A weak draw says so on the action bar about once a second, wild and kept. The vessel line, the Bestiary's met status and unread block, the Liber page Unread Humours, the Alchemist's `craft.humours.line1` (and its brief) and the `BloodResearch` comment now say the opposite of before. | `common/mission/alchemist/BloodResearch.java`, `BloodAbsorptionItem.java`, `KeptSpecimen.java`, `SpecimenVessels.java`, `en_us.json`, `the_hematic_order/pages/unread_humours.json`, `dialogue_briefs/alchemist.json` |
| 03 T4 (CR-26), NQ-03-5 | "All can go in vessels": no kind added to `hemomancy:menagerie_unkeepable`. Checked the Whale: 4.2 wide, so 3.15 drawn against a 3-vat vessel's 2.66 clear width. It fits no vessel. Logged, nothing changed. | none |
| 03 T6 (CQ-47), the Mortem lines, CQ-23 A, CQ-47 pin | Seven school pages rewritten to the built held forms: Needle Lance's volley, Ferric Spikes' blade run, Crimson Flame's painted line, Glacial Grasp's ice hand, Cruor Javelin's throw, Hematic Flare's searchlight, Synaptic Jolt's live run, Blood Eclipse's dome, Gloam's razor. Needle Fan, Prismatic Reproof, Conductive Mark, Hemorrhage, Insatiable Hunger and Grave Debt read R-only. The price pin also catches "costs N mL" and "N mL to cast/hold/channel/use"; a new pin refuses "held it sprays", a held jet, "plants a strand" and "brands it". | `books/libersanguinium/manipulations/pages/{ferric,fervent,frigid,incandescent,neurotic,ruinous,umbral}.json`, `LiberPageRegistrationGuardrailTest.java` |
| 03 RV-60 follow-up | `ScreenChromeReview` has a `specimen_vessel` scene: a Specimen Vat keeping a cow, opened with an empty hand. | `gametest/ScreenChromeReview.java` |
| 03 PR-24 note (CQ-63 A) | Kept: the Temper page's "The phials under your blood bar show what you hold" stays, since CQ-63 A gives the Bandolier a survival giver. Recorded as a ruling; no text change. | `docs/CORE_LOOP.md` |
| 05 T2 rest (CB-21) | The brief line says the cleats cut the upkeep "by half". | `dialogue_briefs/artificer_anchorite_clinical.json` |
| 05 T3 rest (CN-13) | The five `admits` javadocs say the check runs on every pulse. New GameTest: a Lode Draw stops unpaid, and says why, once the player turns Adventure mid-channel, on the existing lode fixture. | `LodeDrawSculpting`, `LignumMortisSculpting`, `PyreticForgeSculpting`, `CarrionDrawSculpting`, `SanguineMendingSculpting`, `StaffMiningGameTests.java` |
| 05 T4 rest (CQ-19), NQ-05-13 | One rule: `TemporaryIceManager.placeOwned` records a frozen water source for every caller, so Rimebound Sentence, Glacial Rampart's R cast and its crouch shell, a Will's and Vesper's Glacial Grasp and Rampart, and a Drudge's rampart all melt back to water. The `keepsWater` overload is gone. A GameTest each. | `congeatio/TemporaryIceManager.java`, `GlacialGraspManip.java`, `SecondSculptingsGameTests.java` |
| 05 skill text (CQ-20 B, CQ-22 A) | Long Strand, Taut Line, Free Hand, Whipcrack and Second Strand descriptions rewritten (Taut Line lash only; Second Strand the lash twin only). Liber titles "The Five Strands" to "Lashes and Held Forms", "More Strands" to "More Held Forms", subtitle "Memories that hold, and the skills that train them", and its skill sentences. `StrandSkills` javadoc says the jet and set-speed halves are no longer the skills'. | `en_us.json`, `strand_forms.json`, `second_strands.json`, `StrandSkills.java` |
| 05 skills on held manipulations (CQ-20 B) | New `common/manipulation/verb/HeldSkills`. Long Strand: `reachScale(Player)` on all eight (x1.1 a level), and Hematic Flare's beam and Glacial Grasp's leash scale with it. Free Hand: +0.1 pace a level, capped at walking, on the seven with a pace (Synaptic Jolt sets none). Whipcrack: x1.15 damage and x1.2 state a level on the six accents (dome flash, ice squeeze, beam flash, flame flare, a clicked lance needle; Ferric Spikes' standing blades deal no hit, so they stand x1.2 longer). Cruor Javelin and Synaptic Jolt have no accent. Deep Brand stays Blood Binding's (30, 45, 60 s). The dropped halves' numbers stay in `StrandSkills` for the kits CQ-25 keeps. | `HeldSkills.java`, `FlareBeamSculpting`, `EclipseDomeSculpting`, `GlacialGraspSculpting`, `CruorJavelinThrowSculpting`, `NeedleVolleySculpting`, `SynapticLiveSculpting`, `FlamePaintSculpting`, `FerricRunSculpting` |
| UN-5 | The standards row, the guardrail test's comment and the Track 12 file's "One manipulation has one form" reworded to the 2026-11-12 ruling. | `ARCHITECTURE_STANDARDS.md`, `SculptingGuardrailTest.java`, `archive/tracks/core_loop/12-strand-combat.md` |
| UN-54 | Shot both cues once. The Will's attached forge at 0.7 sat sunk in the body and read too small, so `EntityCasts` sends it at 1.0 again (the old full size). The Brazier bowl forge reads fine at 0.8 and stays. New `forges` shot in the visual fixes review. | `EntityCasts.java`, `VisualFixesClientReview.java` |
| UN-55 | The stall pip is 7 by 7 (was 5), with a broken gold stitch and a 1-pixel gold halo that pulses (alpha 30 to 140). | `client/screen/widget/BloodVolumeBarWidget.java` |
| UN-59 | Play script sitting 5 rewritten: the tap, the charged whip, the three lash identities, the eight held memories, the skills after CQ-20 B and CQ-22 A, R-only memories, and what stays in code. The two "5 by 5 pip" lines updated for UN-55. | `docs/CORE_LOOP_PLAYTEST.md` |
| UN-65 | The cause: NeoForge enables a test by its template's namespace, and every test in the sculpting, living blood and Temper suites used `minecraft:bastion/mobs/empty`. Adding `minecraft` to focused launchers would have pulled about 560 other tests into every focused run, so instead each of the three suites now carries its own copy of that template and its tests drop `templateNamespace = "minecraft"`. `verifyGameTestSuites` now refuses a `templateNamespace` other than the suite's own in a file whose suite has a launcher. `runSculptingGameTestServer` runs 88 tests. | `gradle/game-tests.gradle`, `src/gameTest/resources/data/{sculpting,living_blood,temper}_validation/structure/bastion/mobs/empty.nbt`, five GameTest classes |
| 05 T7 (CQ-13, UN-58) | `AGENTS.md`: the six Part A replacements (PlayerBlockEdits sentence, pose order, `/hemo` fixtures, onlooker sync, isolation harness with the namespace rule, `bare: true` rows) and the Part B bullets, each checked against the code: verbs in Sculpting words with `holdsChannel`, `takesAccent`/`accent` and `movement`; a rewritten lash bullet (`LashProfile`, `LashStyle`, `LashTuning`, `LashAimPacket`, `LashCamera` via `MixinMouseHandler`, `LashTapPacket`, `StrandSnapPacket`, `HeldSkills`, `StrandCue` bits 20 to 24 and 13); a new held-manipulation bullet; living blood; Temper; keeping (the tag, the unread share); ground marks with the dome on `DarkFields` and `EclipseDomeRules`; crops; Staff; Living Land and cling; the world's answer; GameTest determinism; review clients; the Liber and firsts; lessons; a HUD line; the core-loop documentation line. The standards' strand row matches. | `AGENTS.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md`, `docs/MANIPULATIONS_AND_CASTING.md` |
| 05 T8 (CQ-14), NQ-05-14 | `docs/core_loop/` moved: the track files, both build logs, `AGENTS_PATCH.md` and `images/` to `docs/archive/tracks/core_loop/`; `PLAYTEST.md` to `docs/CORE_LOOP_PLAYTEST.md` and the Track 11 ledger to `docs/CORE_LOOP_TEACHING_LEDGER.md`. Links and paths fixed in 28 files (the docs that named them, `wiki/Developer-Reference.md`, the `GroundworkGameTests` comment, `tools/core_loop/kit.py`), and `docs/README.md` rewritten for the new layout. | see the move list `build/build2/B10-archive-move.txt` |
| Section 2: NQ-03-4, CL-12 shape, CR-26 Will tag, CQ-47 pin, NQ-05-13, NQ-05-14, UN-5, UN-30 | Built above, or already built and now recorded (CR-26 Will tag by P05; UN-30 written as a ruling). | docs |
| Sculpting batch (B09's finding) | The two Sculpting tests B09 saw fail are in `SculptingValidationGameTests`'s neighbours: its tests lend a registered manipulation a stub Sculpting server-wide, so `sanguine_mending`'s real tests could run against the stub. The 12 lending tests now run in their own batch, `sculpting_lent`. | `SculptingValidationGameTests.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| 03 T2 (CN-6, CQ-45), Along the Red | Held by the owner. Not touched. |
| NQ-03-5 extra kinds | The answer adds none. The Whale fits no vessel; logged for the owner. |
| 05 T6, 05 dead kits, VX-18 | Not in this package (next package, held, held). |
| `StrandSkills.QUICKEN_PER_LEVEL` and the brand set ticks | CQ-20 B drops them from the skills and the text, which is done. Deleting the numbers is the held "05 dead kits" row, since the jet and brand kits still read them. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,068 tests, 0 failures, 0 errors (`build/build2/B10-unit3.txt`). The first run had one failure, `TemperGuardrailTest` (see below).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1959 required tests passed :)" twice (`B10-gametest2.txt`, `B10-gametest3.txt` with `-latest.log`). 1,950 after B09 plus 9 new. The first run had 5 failures: two tests that drew unread kinds, my rampart test's aim (it hit the pond's wall), and the two Sculpting batch tests.
- `./gradlew.bat runSculptingGameTestServer --offline --console=plain`: "All 88 required tests passed :)" (`B10-sculpting.txt`, `B10-sculpting-latest.log`). It ran none before (UN-65).
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B10-playthrough-optional.txt`).
- Added: `BloodResearchShareTest` (the half share). `MenagerieGameTests.anUnreadKeptKindGivesHalfItsTemper` and `anUnreadWildKindGivesHalfItsTemper` (half the Temper, the same blood). `LiberPageRegistrationGuardrailTest.theSchoolPagesTeachNoCutHeldForm`. `StaffMiningGameTests.lodeDrawStopsMidChannelWhenThePlayerMayNoLongerBuild`. `SecondSculptingsGameTests.rimeboundSentenceOnAPondMeltsBackToWater`, `glacialRampartCastInAPondMeltsBackToWater`, `anEntityCastGlacialGraspMeltsBackToWater`, `aDrudgeRampartInAPondMeltsBackToWater`. `StrandGameTests.longStrandAndFreeHandTrainTheHeldManipulations` (all eight: reach x1.3 at Long Strand 3; pace +0.3 capped, Jolt none) and `whipcrackHardensTheClickedLanceNeedle` (x1.45).
- Changed on purpose: `TemperGuardrailTest` pins `.fillFromCreature(serverPlayer, target, absorbed * BloodResearch.temperShare(read))` (CL-12). `LiberPageRegistrationGuardrailTest`'s price pattern widened (CQ-47 pin). `TemperValidationGameTests.drawingFromACreatureFillsItsSchools` and `MenagerieGameTests.heldAbsorptionOnAVatGivesItsSchoolsCutByAcclimation` now mark their kinds read first, so they still check the 1 Temper per mL rate and the acclimation cut; the unread half is checked by the two new tests. 125 tests in the sculpting, living blood and Temper suites lost `templateNamespace = "minecraft"` (same template, own namespace). 12 lending tests got their own batch.
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv`.
- Clients: `runVisualFixesReviewClient` three times (22 shots each), `runScreenChromeReviewClient` once (32 shots). Both stopped themselves; no client or server is left running.

### Docs

- Updated: `AGENTS.md`; `docs/HEMOMANCY_REFERENCE.md` (CL-12 paragraph, NQ-05-13 in 8.0.1 and the Glacial Grasp row, 8.0.2: the kits line, the skill table, the new "skills on the eight held manipulations" paragraph, Deep Brand, the twin); `docs/LORE_REFERENCE.md` 4.1 (the owner's reason for CL-12); `docs/CORE_LOOP.md`; `docs/MANIPULATIONS_AND_CASTING.md`; `docs/TESTING.md` (template namespaces, the lending batch); `docs/architecture/ARCHITECTURE_STANDARDS.md` (strand row, UN-5 row); `docs/CORE_LOOP_PLAYTEST.md` sitting 5; `docs/README.md`; `wiki/Blood-Systems.md`, `wiki/Clinical-Blood-Tools.md`; chunk files 03 and 05 status tables.
- Rulings recorded under "Rulings that bind": in `docs/CORE_LOOP.md` CL-12 built, NQ-03-1 A with NQ-03-3 A, NQ-03-2, NQ-03-4, CL-12 shape, CR-26 Will tag, NQ-03-5, CQ-47, CQ-23 A on the pages, the Temper page line, CQ-13 and CQ-14 built, NQ-05-14. In `docs/MANIPULATIONS_AND_CASTING.md` NQ-05-13, CQ-20 B, CQ-22 A, CQ-23 A, RV-58 (with CL-25), UN-5, UN-30, UN-54; CN-13 and CB-21 lines say their last parts are done (all dated 2026-11-14 for the answer).
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows "CQ-20, RV-57, UN-2, UN-3", "CQ-22, UN-1", "CQ-23, UN-7", "RV-58, CL-25", "NQ-03-1, NQ-03-3, CL-12", "NQ-03-5, CR-26", "NQ-03-2". Section 2 rows NQ-03-4, CL-12 (T5 shape), CR-26 (Will tag), CQ-47 (pin), NQ-05-13, NQ-05-14, UN-5, UN-30. Section 3 rows 03 T6, 03 RV-60 follow-up, 03 T4, 03 T5, 03 T6 Mortem lines, 03 PR-24 note, 05 T2 rest, 05 T3 rest, 05 T4 rest, UN-5, UN-54, UN-55, UN-59, UN-65, 05 T7, 05 T8, 05 skill text, 05 skills on held manipulations. Section 4: PR-17, CL-12 and PR-21 rewritten to "now"; new rows B10 (school pages), B10 (vessel chrome), B10 (CQ-20 B), B10 (UN-54), B10 (NQ-05-13), B10 (CQ-13, CQ-14). Paths to the moved core-loop files were fixed in other areas' rows; their text was not changed.

### For the owner

- New lang key: `message.hemomancy.absorption.unread_weak` ("Unread blood. Only half its Temper takes hold until you examine its kind.").
- Changed values: `message.hemomancy.menagerie.readout.unread`, `bestiary.hemomancy.status.met`, `bestiary.hemomancy.creature.unread`, `hemomancy.alchemist.craft.humours.line1` (Alchemist sheet: first person, procedure, the reason attached), `skill.hemomancy.skill_long_strand.desc`, `skill_taut_line.desc`, `skill_free_hand.desc`, `skill_whipcrack.desc`, `skill_second_strand.desc`. Liber text changed on seven school pages, `strand_forms.json`, `second_strands.json` and `unread_humours.json`.
- Values I chose: the action-bar line about once a second; Whipcrack on Ferric Spikes lengthens how long the standing blades stay (they deal no hit); Long Strand also scales Hematic Flare's beam and Glacial Grasp's leash; the Will forge back to 1.0; the two live core-loop files are named `docs/CORE_LOOP_PLAYTEST.md` and `docs/CORE_LOOP_TEACHING_LEDGER.md`; the lending batch is `sculpting_lent`.
- Looks: the stall pip before and after, `build/build2/B10-pip-old.png` and `B10-pip-new.png` (crops of `build/build2/B10-old-visual/visual-7-stalled-screen-30.png` and `build/build2/B10-visual-run1/visual-7-stalled-screen-30.png`). The forges: `build/build2/B10-visual-run2/visual-16-forges-8.png` (Will forge at 0.7, sunk in the body) and `build/visual-fixes-client-review/screenshots/visual-16-forges-8.png` (back at 1.0). The vessel chrome: `build/screen-chrome-review/screenshots/chrome-after-specimen_vessel.png`.
- Surprising: (1) The Brazier bowl forge floats about three blocks above the bowl in both forge shots. The review uses the rite's own call (`BrazierTransmutationRite`, `bowl + 0.4`), so play may show the same. Not changed. (2) The vessel chrome shot frames the screen well, but its values read as a preserved, unkept cow at Vigor 0 (the review freezes ticks and the owner did not show). The frame is what the scene checks; the values do not matter there. (3) The third forge run's world had flooded round the player (the reused save). (4) The archive keeps only markdown in git (`.gitignore`), so the 16 core-loop images (PNG and SVG) moved with the track files are now untracked; they are still on disk in `docs/archive/tracks/core_loop/images/`. (5) The Alchemist's `craft.humours.line1` is now rewritten for CL-12; the Dialogue area's "16 T5 (CN-5), ruling-driven lines" row still lists it. (6) The Mnemonic Whale fits no vessel, so leading one home is a dead end that costs upkeep (NQ-03-5 kept it keepable). (7) The UN-65 cause was wider than the row said: the living blood and Temper focused suites ran no tests either.

### Verification (round 1)

- Pass. Checked the tracked tree against `refs/build2/B10-before` and the untracked files against `build/build2/B10-untracked`. The only new source file is `HeldSkills.java`, plus the test, the three template copies and the two live core-loop docs.
- Scope: only area 03 and area 05 rows from the brief. Along the Red, the jet, spray and plant kits, the stain run, VX-18 and the UN-41 write-back are untouched (the UN-41 row only had its path fixed). `StrandSkills` changed in its javadoc only. No `LashTuning`, `LashProfile` or `LashStyle` change. The HUD change is the stall pip that UN-55 names. Silent Severance is not touched.
- Values match the answers: an unread kind gives half its Temper (`UNREAD_SHARE` 0.5) on wild and kept draws from the first draw, blood unchanged, injection untouched (NQ-03-1 A, NQ-03-2, NQ-03-4). No kind added to the unkeepable tag (NQ-03-5). Long Strand, Free Hand and Whipcrack reach the eight held manipulations; Taut Line is lash only; Second Strand is the lash twin only; Deep Brand stays 30, 45, 60 s (CQ-20 B, CQ-22 A). Every Frozen Cruor over a water source melts back to water (NQ-05-13). The paces are unchanged (RV-58).
- Tests: each built row has a test that fails without it. `TemperGuardrailTest` and the price pin were changed to the rulings on purpose. Two Temper tests now mark their kinds read so they keep checking the full rate. The template and batch edits move tests; none was removed or weakened.
- Unit suite: BUILD SUCCESSFUL, 6,068 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B10-verify-unit.txt`).
- Combined GameTest run: "All 1959 required tests passed :)" (`build/build2/B10-verify-gametest-latest.log`).
- Playthrough: "All 433 required tests passed :)", 60 optional: the baseline 59 plus `apotheosritewithoutrescue` (PT-88, known) (`build/build2/B10-verify-playthrough-optional.txt`).
- Rules: no payload, so no protocol bump. The new lang key is in `en_us.json`. No blood path changed. No new vanilla particle. The core-loop files were moved to the archive, not deleted.
- Looks: I checked the shots already in the tree and did not launch a client again: `build/build2/B10-pip-new.png`, `build/visual-fixes-client-review/screenshots/visual-16-forges-8.png` and `build/screen-chrome-review/screenshots/chrome-after-specimen_vessel.png`.
- Docs: the rulings are in `docs/CORE_LOOP.md` and `docs/MANIPULATIONS_AND_CASTING.md`. In `OPEN_QUESTIONS.md` only this package's rows were deleted. Other rows only had their core-loop paths fixed.
- Notes, not blocking: (1) The vial tooltip `item.hemomancy.bloody_vial.unknown` still just reads "An unread humour". The chunk plan wanted it to say the draw runs weak. The other lines now say it. (2) `FirstConversionsGameTests.java` changed from CRLF to LF line endings. The content change is only the template lines.

## B11. Hematic Cleats as an Armature fitting

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 05 T6 (CN-10), NQ-05-7 A | Hematic Cleats are no longer boots. The `hematic_cleats` item (same id) is a plain item, crafted from 2 hematic iron scrap, 4 bone and a Sinew Cord (`ISI` / `O O` / `O O`), no boots. It cannot be worn. Put in an Armature bowl, it is fitted to the worn boots. | `HematicCleatsItem.java`, `recipe/hematic_cleats.json`, `ItemInit.java` |
| NQ-05-11 A | The fitting runs in `HematicArmatureBlockEntity` beside the upgrades: the same windup, 100 ticks, sneak to leave. It keeps the worn boots (the same stack) and marks them. Cost 250 mL from the Armature's tank (the iron to hematic iron upgrade's blood), any Armature stage, no Degree of its own. It records no `ARMOR_UPGRADE` practice and no Artificer upgrade proof. | `HematicArmatureBlockEntity.java`, `CleatsFitting.java` |
| NQ-05-8 | The mark is the `hemomancy:cleats` data component (a boolean: true on, false off). Anvil repair keeps it. An Armature upgrade's result keeps it and its toggle (`CleatsFitting.carry`). Nothing removes it. | `DataComponentInit.java`, `CleatsFitting.java` |
| NQ-05-9 | Item tag `hemomancy:takes_cleats`: hematic iron, Barbed, Chitinite, Prismatic, Blood Lust, Edacious, Phantasmal, Sheolic and Silent Archon boots. Unstained boots and the Venous Strider Sabatons are left out. | `tags/item/takes_cleats.json` |
| NQ-05-10 | `ClingService.wearsCleats` reads the worn boots' component and its toggle. Off means full upkeep and the slow crawl (`ClingRules` keeps x0.5 and its speeds). A new inner-radial slice, shown while the worn boots are fitted, sends `ToggleCleatsPacket` (no data, `ServerboundGuards.activePlayer`, paced to one every 10 ticks). `PROTOCOL_VERSION` 50 to 51, `payload-registry.txt` rewritten. The state is saved on the boots. | `ClingService.java`, `ToggleCleatsPacket.java`, `PacketHandler.java`, `RadialChooseManipScreen.java` |
| NQ-05-12 | The whole-boot model is gone. `HematicCleatsModel` now holds only a hematic iron sole plate just proud of the boot, a thin sinew band, bone spurs off toe, heel and both sides and two studs, in the old cleats texture. `HematicCleatsLayer` draws it over the worn boots while the cleats are on, on players, armour stands and the humanoid mobs that already take armour layers. The `ItemClientExtensions` boot-model hook is removed. | `HematicCleatsModel.java`, `HematicCleatsLayer.java`, `LayerEvents.java`, `ItemClientExtensions.java` |
| T6 text | Fitted boots get a tooltip line (on or off). The cleats item's tooltip, the Armature's preparing and done messages, the toggle's action-bar line, nine JEI rows titled "Cleats Fitting", the Artificer's item inquiry and `climb` lesson lines with their brief, and the Liber Venous Cling page. | `ItemTooltipEvents.java`, `HematicArmatureRecipeCategory.java`, `JEIPlugin.java`, `en_us.json`, `artificer_anchorite_clinical.json`, `venous_cling.json` |
| Removed | The old boot item class, its recipe and the whole-boot model are kept as text in `removed_assets/2026-11-15-cleats-boots/` with a README. | `removed_assets/2026-11-15-cleats-boots/` |

### Skipped

| Row or part | Why |
| --- | --- |
| "Feet bowl" exactly | The Armature has no bowl tied to a slot: a reagent goes in the next free bowl and any bowl serves any worn piece. Requiring the feet bowl would mean filling three bowls first. The cleats work from any bowl, like every reagent. Say if you want a real feet-only bowl. |
| Resonant Forge pattern work keeping the mark | Not checked (the plan flagged it). Anvil repair and Armature upgrades keep it. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,068 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B11-unit.txt`, and again after the last model change, `build/build2/B11-unit2.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: the first run had 1 failure, my own new test. It asserted the boots kept a damage of 17, and a hematic iron boot's damage did not stay there, so the test now checks the worn stack is the very same object. Second run: "All 1965 required tests passed :)" (`build/build2/B11-gametest2.txt`, `B11-gametest2-latest.log`). That is 1,959 after B10 plus 6 new.
- `./gradlew.bat runClingGameTestServer`: "All 15 required tests passed :)" (`build/build2/B11-cling.txt`).
- Added to `ClingGameTests`: `cleatsTurnedOffCostTheFullUpkeep` (off pays the full 4 mL/s), `theRadialToggleTurnsTheWornCleatsOffAndOn`, `theNineBloodSideBootsTakeCleatsAndNoOthers` (the tag is the nine; Unstained, Strider Sabatons, iron boots and the cleats item are refused), `theArmatureFitsCleatsToTheWornBootsWithoutPractice` (same stack, on, cleats used, 250 mL taken, no practice), `theArmatureRefusesCleatsForBootsThatTakeNone`, `fittedCleatsAndTheirToggleSurviveALineageUpgrade` (hematic iron to Barbed keeps the cleats, still off).
- Changed on purpose: `cleatsCutTheUpkeep` now wears fitted hematic iron boots instead of the old cleat boots. New fixture `CleatsFixtures.fitted`. The visual fixes, Living Land, onlooker and verb review clients stage fitted boots.
- Payload tests (`PayloadProtocolVersionTest`, `ServerboundGuardsCoverageTest`, `NetworkPackageGuardrailTest`) pass with the new payload; no exemption was needed. The frozen `registry-ids.txt` gained `minecraft:data_component_type hemomancy:cleats`.
- Clients: `runVisualFixesReviewClient` three times (the old `VisualFixes` save is flooded, so a fresh `VisualFixesB11` save was made; the second run led me to thin the sinew band), `runVerbReviewClient` once. All stopped themselves; no client or server is left running.

### Docs

- Updated: `AGENTS.md` (cling bullet), `docs/HEMOMANCY_REFERENCE.md` (Venous Cling paragraph, the Venous Cling table row, the Armature progression and block-entity rows, the JEI list), `docs/CORE_LOOP.md` (protocol 51, the visual fixes row, the pointer), `docs/MANIPULATIONS_AND_CASTING.md` (CN-10 ruling, pointer), `docs/STATION_UPGRADES.md` (a rule: the fitting is an ordinary operation with no practice credit; the open-questions paragraph), `docs/CORE_LOOP_PLAYTEST.md` (sitting fixture, try list, feel line), `docs/architecture/ARCHITECTURE_STANDARDS.md` (protocol 51), `wiki/Blood-Systems.md`, chunk 05 status line T6.
- Rulings recorded under "Rulings that bind" in `docs/CORE_LOOP.md`: CN-10 built, NQ-05-7 A, NQ-05-11 A, NQ-05-8, NQ-05-9, NQ-05-10, NQ-05-12 (answers dated 2026-11-14). CN-10 in `docs/MANIPULATIONS_AND_CASTING.md` now says built.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows NQ-05-7 and NQ-05-11 (their subsection, now empty, went with them); section 2 rows NQ-05-8, NQ-05-9, NQ-05-10, NQ-05-12; section 3 row 05 T6 (CN-10). Section 4: PR-22 now reads "now"; new row B11 (CN-10).

### For the owner

- New lang keys: `block.hemomancy.hematic_armature.cleats_fitted` ("The Armature sets the cleats into %s."), `block.hemomancy.hematic_armature.fitting_cleats` ("Fitting Hematic Cleats to %s: %s mL from the Armature. No health payment. Hold %s to leave before it binds."), `hemomancy.jei.armature.slot.cleats` ("Cleats Fitting"), `message.hemomancy.cleats.on` and `.off` ("Hematic Cleats on." and "Hematic Cleats off."), `screen.hemomancy.radial_choose_manip.cleats` ("Hematic Cleats"), `screen.hemomancy.radial_choose_manip.cleats.on` and `.off`, `tooltip.hemomancy.cleats_fitted.on` and `.off`.
- Changed values: `tooltip.hemomancy.hematic_cleats`, `hemomancy.artificer.item_inquiry.hematic_cleats.line1` and `line2`, `hemomancy.artificer.craft.climb.cleats.line1` and `line2` (Artificer sheet: steps in the order the hands do them; the lesson no longer says a Saint's ward refuses them, the Liber page still does). The Liber page `venous_cling.json` changed.
- Values I chose: the toggle is paced to one every 10 ticks; the cleats work from any bowl; the layer also draws on armour stands and the armour-wearing humanoid mobs; the visual review's player now wears fitted Blood Lust boots, so a second boot model is seen.
- Looks: before, `build/build2/B11-shots/before/visual-{2,3,4}-cleats-*.png` (the old whole boot, in the flooded save). After: `build/build2/B11-shots/after/visual-2-cleats-front-20.png` and `visual-3-cleats-side-20.png` (fitted hematic iron on the right-hand stand in the picture, plain on the left), `visual-4-cleats-worn-20.png` (fitted Blood Lust boots, half hidden by the HUD) and `verb-7-radial-inner-verbs.png` (the cleats slice on the inner ring, bordered because they are on). The spurs read; the sole and band are the dark red of the old texture, which was not redrawn.
- Surprising: (1) The Dialogue area's Artificer row in section 3 still lists "the cleats lines and their option once cleats are an Armature fitting (CN-10)". I wrote the lines but left that row alone, as it is another area's. The option text "What are Hematic Cleats for?" did not need to change. (2) Chunk 16 T6's "the cleats sentence follows CN-10 later" can now be written. (3) The `clinging` advancement still uses the cleats item as its icon, which still fits. (4) The B11 restore point covers tracked files only; the untracked files I edited were copied first to `build/build2/B11-untracked-backup/`.

### Verification (round 1)

- Pass. Checked the tracked tree against `refs/build2/B11-before`, the new files against `build/build2/B11-status-before.txt`, and the edited untracked files against `build/build2/B11-untracked-backup/`.
- Scope: only 05 T6 (CN-10) and its six answers. No jet, spray or plant kit, stain run, Along the Red, VX-18 or UN-41 change. `ClingRules` is unchanged (x0.5 upkeep, the old speeds). No `LashTuning`, `LashProfile` or `LashStyle` change, and no HUD design change. Silent Severance is not touched.
- Values match: the cleats item is a plain item crafted from 2 scrap, 4 bone and a Sinew Cord, no boots, not wearable (NQ-05-7 A). The fitting keeps the same worn stack, costs 250 mL from the Armature tank, needs any Armature stage and no Degree, and records no `ARMOR_UPGRADE` practice (NQ-05-11 A). `hemomancy:cleats` boolean component, carried by `CleatsFitting.carry` on Armature upgrades (NQ-05-8). The tag holds the nine blood-side boots with Silent Archon's; Unstained and Strider Sabatons are out (NQ-05-9). Off means no discount, no fast crawl and no spurs; one serverbound payload behind `activePlayer` and the rate limiter (NQ-05-10). The whole-boot model is gone; spurs draw over any boot model (NQ-05-12).
- Tests: six new `ClingGameTests` cases, each failing without its part. `cleatsCutTheUpkeep` was moved to fitted boots on purpose. No test removed or weakened.
- Unit suite: BUILD SUCCESSFUL, 6,068 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B11-verify-unit.txt`).
- Combined GameTest run: "All 1965 required tests passed :)" (`build/build2/B11-verify-gametest-latest.log`).
- Rules: `PROTOCOL_VERSION` 51 and `payload-registry.txt` "version 51" with `ToggleCleatsPacket` moved together. New player text is in `en_us.json`. The Armature drains its own tank, not a player volume, so `BloodAccounting` does not apply. No vanilla particle added. The old item, model and recipe are in `removed_assets/2026-11-15-cleats-boots/`.
- Looks: launched `runVisualFixesReviewClient` on the `VisualFixesB11` save; it loaded the world and stopped itself. Shots: `build/build2/B11-verify-shots/visual-2-cleats-front-20.png`, `visual-3-cleats-side-20.png`, `visual-4-cleats-worn-20.png`. Spurs show on the fitted stand and the worn Blood Lust boots, none on the plain stand. No client or server left running.
- Docs: rulings are in `docs/CORE_LOOP.md`; `STATION_UPGRADES.md` has the no-practice rule. Only this package's rows left `OPEN_QUESTIONS.md`.
- Notes, not blocking: (1) The Artificer's `climb.cleats.line2` says "Hematic iron boots take them, and every pair the frame makes from those"; the tag also holds Silent Archon's boots, which are not an Armature line. (2) No test pins anvil repair keeping the component; it rides on vanilla component copying.

## B12. Lash follow-ups and the HUD: Gloam's ambush, minors, the Bandolier

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| UN-8 fix | Gloam's ambush is now only a blow from behind (outside the front 120 degrees) or at block light 4 or less. A body already Obscured no longer counts. The 1.75 is unchanged. Comments, the lang line and reference 8.0.2's Signature row say the same. No existing Gloam GameTest leaned on the clause. | `StrandService.java`, `LashStyle.java`, `Sculptings.java`, `en_us.json` |
| UN-56 (1) | A left click with a lash selected on a boat, an item frame, an end crystal or any body that is not a creature is left to vanilla. | `LashRules.clickStaysVanilla`, `ClientInputEvents.onLashTap` |
| UN-56 (2) | With the Projection tool only in the off hand and something in the main hand, a click on a creature is the main hand's attack. Open air still taps. An empty main hand still taps. | same |
| UN-56 (3) | The local caster's lash is let go the tick they stop holding Projection, so the camera cap and the aim stream end at once. A pulse cue already on its way after the release no longer brings the lash back. | `StrandClientState.tick`, `.accept` |
| UN-56 (4) | Re-checked: the local lead was already held to 70 degrees, inside the server's 85. What was left was the step: the local whip aim is now held to the server's 60 degrees a tick (per frame, scaled by the frame's length). | `LashCameraRules.limitStep`, `LashCamera.split` |
| UN-56 (5) | A watcher's new strand starts at the caster's look, not 0/0. A snap's recentred aim is relayed to watchers. | `StrandClientState.accept`, `StrandService.snap` |
| UN-56 (6) | `LashCamera.reset` runs at client login and logout. | `ClientSessionEvents.java` |
| UN-56 (7) | The whip aim the server takes goes to trackers only, not back to the caster. New facade method `PacketHandler.sendToTrackers`. No payload changed, so no protocol bump. | `StrandService.aim`, `PacketHandler.java` |
| UN-56 (8) | A tap's cooldown and chain are forgotten at respawn and at a dimension change (`StrandService.forgetTaps`). | `StrandService.java` |
| UN-56 (9) | The tap finds its lash through `SculptingDispatcher.currentClaim`, as the plan said. | `StrandService.tapLash` |
| UN-56 (10) | Already true: with damping 0 (or no lash out) the turn goes straight through in its own units. Pinned by a new unit test. | `LashCameraTest.java` |
| UN-56 (11) | `MixinMouseHandler` wraps the turn with `@WrapOperation` instead of `@Redirect`; `LashCamera.split` returns the view's share. | `MixinMouseHandler.java`, `LashCamera.java` |
| UN-56 (12) | `LashTuningClient` shows its three lines through lang keys. | `LashTuningClient.java`, `en_us.json` |
| UN-43 harness | `OnlookerClientReview`: the strand scenes are now `lash-blood` and `lash-gloam-faint` (flicked and swept through `LashCamera`), `flare-beam`, `jolt-creature`, `eclipse-dome`, `javelin-flight`, `needle-volley`. The old `lash-swept`, `jet-twin` and `spray` are gone. A full run captured 32 frames. | `OnlookerClientReview.java`, `docs/TESTING.md` |
| UN-42 captures | 15 new shots in `StrandClientReview`: vertical whip swings, a stuck javelin recalled by Absorption, Gloam's pierce accent on a tap from behind, Gloam's charged throw through a row of three husks, Searing Lash cracking three times in a row, flame paint refused by a claim (a place-event cancel on the stage), the ice hand and the flare's flash on a moving husk. New `-PstrandOnly=a,b` filter on the gradle row. Captured 64 frames. | `StrandClientReview.java`, `gradle/review-clients.gradle` |
| UN-62 | `morphlingHudScale` (`HemoClientConfig.MORPHLING_HUD_SCALE`) and its test removed; the reference clause removed. Kept as text in `removed_assets/2026-11-15-morphling-hud-scale/`. | `HemoClientConfig.java`, `MorphlingFeedingAnimationConfigTest.java` |
| UN-64 M-2 | In LEGACY_ICON mode the jar's front glass is drawn at depth 200, over the item. | `EquippedMorphlingOverlay.renderInJar`, `BloodVolumeOverlay.renderJar` |
| UN-64 M-7 | The HUD clock is game time wrapped every hour (72,000 ticks), added in double, so it never steps on an old world. | `HudHeartbeat.seconds`, `BloodVolumeOverlay.renderHUD` |
| UN-64 M-8 | `BloodVolumeOverlay.assembly(Player)` returns null before client setup; its two callers check. | `BloodVolumeOverlay.java`, `TemperMarksOverlay.java`, `TemperRackReview.java` |
| UN-34, UN-35 scenes | `menagerieReviewClient` now stages a dormant and a lit Sanguine Vigil (overview and two close-ups), a 2x2 vessel keeping a horse and a 2x2x2 vessel (overview and two close-ups). The log confirms 4 vats two across and 8 vats two high. | `MenagerieClientReview.java` |
| CQ-63 A, CQ-63 (delivery) | The Alchemist gives one Titration Bandolier the first time a D2+ player hears any branch of her Temper lesson. No other proof. It is owed through `OwedRewards` (the `PendingDelivery` claim) before the one-time flag is set; the flag is `bandolier_given` on the `blood_temper` attachment, so it survives death. A lost one is not given again. Recipe: `LSL` / `GGG` (2 leather, 1 Sinew Cord, 3 glass bottles), category equipment. The lesson router gained a `heard` hook for this. | `TitrationBandolierGift.java`, `BloodTemper.java`, `CoreLoopLessonRoutes.java`, `AlchemistDialogueEvents.java`, `recipe/titration_bandolier.json` |
| Phials sentence | It was not dropped. It now reads "The phials on your bandolier show what you hold." | `en_us.json`, `dialogue_briefs/alchemist.json` |

### Skipped

| Row or part | Why |
| --- | --- |
| UN-41 write-back | Held by the owner. |
| The two-client play session (UN-43 in section 4) | The harness is ready and ran; the played session is the owner's. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,072 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B12-unit2.txt`). The first run had one failure, `TemperGuardrailTest`, from the new gift reading `getBloodTemper`; the gift is added to its readers with the reason (it touches only the flag).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1971 required tests passed :)" (`build/build2/B12-gametest2-latest.log`). The first run failed only `everydialogueroutekeepsitsoutcome`: line 656 (`alchemist_craft_temper` at D7) now carries the Bandolier, its line, the flag and the owed list. Only that line was copied from the actual file (old copy in `build/build2/B12-route-fingerprints.before.txt`).
- `./gradlew.bat runStrandGameTestServer`: "All 74 required tests passed :)". `./gradlew.bat runCoreLoopGameTestServer`: "All 24 required tests passed :)".
- Added: `StrandGameTests.anObscuredBodyFacingTheCasterIsNoAmbush` (UN-8), `theCasterIsNotSentTheirOwnWhipAim` (7), `aTapsCooldownEndsWithTheBody` (8); `CoreLoopLessonGameTests.theAlchemistsTemperLessonGivesTheBandolierOnce`, `theBandolierWaitsForRoomAndIsNotGivenOutOfTurn`, `aLostBandolierCanBeCrafted`; unit `LashRulesTest.aClickOnABlockOrOnSomethingNotAliveIsVanillas`, `anOffHandLashLeavesACreatureToTheMainHandsWeapon`, `LashCameraRulesTest.theWhipAimMovesNoFurtherInATickThanTheServerAllows`, `LashCameraTest.anOrdinaryTurnPassesStraightThrough`, `HudHeartbeatTest.theClockStillMovesSmoothlyOnAnOldWorld`.
- Changed on purpose: `CoreLoopLessonGameTests.checkEvents` marks the Bandolier as given before the Alchemist's Temper lesson, because that lesson now also gives an item; the gift has its own tests. Removed: `MorphlingFeedingAnimationConfigTest.morphlingHudScaleDefaultsToOne` (the option is gone).
- Not run: the playthrough suite.
- Clients: `runMenagerieReviewClient` (captured 16), `runStrandReviewClient -PstrandOnly=...` (captured 64, then the recall pair again after aiming it at the real javelin), `tools/review/review-onlooker.ps1` (observer captured 32, host finished). All stopped themselves; no Java process is left.
- Restore point: `refs/build2/B12-before` covers tracked files; the untracked files I edited were copied first to `build/build2/B12-untracked-backup/` (three were rebuilt from the edited file by reversing my change).

### Docs

- Updated: `docs/HEMOMANCY_REFERENCE.md` (8.0.2 Signature row, the "Showing it" Bandolier sentence, the jar sentence), `wiki/Blood-Systems.md`, `docs/CORE_LOOP.md`, `docs/MANIPULATIONS_AND_CASTING.md`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/TESTING.md` (onlooker scenes), the impact map (regenerated), both ledgers under `.superpowers/sdd/`.
- Rulings recorded: UN-8 in `MANIPULATIONS_AND_CASTING.md`, CQ-63 A in `CORE_LOOP.md` (answers dated 2026-11-14).
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows UN-8 and CQ-63 (both subsections, now empty) and CQ-63 in "The answers that free the most work"; section 2 row CQ-63 (delivery); section 3 rows UN-56, UN-43 harness, UN-42 captures, UN-8 fix, UN-62, UN-64, UN-34/UN-35 scenes, CQ-63 build. Section 4: UN-28, UN-29, UN-42, UN-43, UN-46, UN-34, UN-35 now say what is ready; new row B12.
- No chunk plan for this package, so no status table was touched.

### For the owner

- New lang keys: `hemomancy.dialogue.event.alchemist_bandolier_given` ("The Alchemist hands you a Titration Bandolier. 'Eight phials, one for each school. Fit it in the bandolier slot at your Scarlet Vanity and they show what you hold. Lose it and make another: two leather, a sinew cord, three glass bottles.'"), `gui.hemomancy.lash_tuning.read`, `.readout`, `.unread` (tuning client only). Changed: `hemomancy.alchemist.craft.temper.catalysts.line2` (last sentence now "The phials on your bandolier show what you hold."), `hemomancy.manipulation.gloam_laceration.sculpting.project` ("from behind or in the dark").
- Values I chose: the recipe (2 leather, 1 Sinew Cord, 3 glass bottles); the gift fires on the first branch of the Temper lesson she is asked (the lesson root has no event); the HUD clock wraps once an hour, so the heartbeat jumps one frame an hour; the front glass sits at depth 200 in LEGACY_ICON mode.
- Looks to judge: Vigils and vessels in `build/build2/B12-shots/menagerie/` (`menagerie-8-vigils-40.png`, `-9-vigil-dormant-close`, `-10-vigil-lit-close`, `-11-joined-vats`, `-12-vat-horse-close`, `-13-vat-cube-close`); the UN-42 cases in `build/build2/B12-shots/strand/` (64 frames, named by shot); the onlooker frames for the new forms in `build/build2/B12-shots/onlooker/` (`onlooker-08` to `-21`).
- Surprising: (1) In the vigils shot the lit Vigil is the left one. (2) The javelin in the stuck shot lands short of its aim, near the caster; the recall shot now aims at wherever it stuck. (3) From outside, Blood Eclipse's dome is drawn as black ribs, the look you have rejected as "spaghetti" before (`onlooker-16-eclipse-dome-side.png`). (4) In the onlooker `lash-blood` frame the whip runs well past the husk.

### Verification (round 1)

Checked the tree against `refs/build2/B12-before` and, for untracked files, against `build/build2/B12-untracked-backup/`.

- Scope: only the brief's rows. No HELD or REVERSED work. No lash feel number changed: the local aim limit reuses the existing `LashTuning.maxAimStep`, and `LashTuning`, `LashProfile` and the `LashStyle` numbers are untouched. No HUD redesign.
- Values: UN-8 drops only the Obscured clause (behind and dark stay, 1.75 kept). CQ-63 A: the Alchemist's D2 Temper lesson gives one Bandolier through `OwedRewards` (the `PendingDelivery` claim), flagged on `blood_temper` (copied on death), no other proof, plus a plain recipe. UN-62 and UN-64 match their rows.
- Tests: each built row has a test that fails without it, except the client-only minors UN-56 (3), (5), (6), (11) and (12), which have no test. `checkEvents` was changed on purpose and the gift has its own three tests; the removed test belongs to the removed option.
- Unit suite: BUILD SUCCESSFUL, 6,072 tests, 0 failures (`build/build2/B12-verify-unit.txt`).
- Combined GameTest run: "All 1971 required tests passed :)" (`build/build2/B12-verify-gametest-latest.log`). Strand suite: all 74 passed. Core loop suite: all 24 passed.
- Clients: `runStrandReviewClient -PstrandOnly=lash-tap-gloam,searing-heat,lash-vertical,javelin-recall` captured 37 (`build/build2/B12-verify-shots/strand/`). `runTemperRackReviewClient` captured 44 (`build/build2/B12-verify-shots/temper/`; `temper-rack-jar_legacy_icon.png` shows the item behind the front glass). Both got past the onboarding screen and stopped themselves; no Java is left.
- Docs, not blocking: `docs/OPEN_QUESTIONS.md` section 2 still has NQ-16-2, which the phials sentence now settles. `docs/CORE_LOOP.md` line 318 still quotes the old wording "under your blood bar", but the line now reads "on your bandolier". Section 1 and section 3 keep empty "HUD and the Bandolier" tables.
- Result: pass.

## B13. Creatures: profiles, drops, stats, the Gardens roster, Wills, spawns

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 06 T1 rest (CR-1), NQ-06-1, C-5 mooshroom | `hemomancy:fungal_gardens` joins `#minecraft:is_overworld`. Its underground-ores step now starts with the vanilla Overworld ores in vanilla order (ore_dirt to ore_copper, 25 features), then its own. Joining the tag brings in `add_field_fungi`, which would have listed `small_infected_fungus` twice in one step (a feature order cycle). So the biome's own list drops `small_infected_fungus` and `stink_horns`, and the biome joins `stink_horn_spawnlist`: both now come from the shared modifiers, in the same order as in swamps. The mooshroom row is deleted (checked: the floor is erythrocytic mycelium and no mod tag lets mooshrooms spawn there). The generator's spawn table lost the mooshroom too. | `worldgen/biome/fungal_gardens.json`, `minecraft/tags/worldgen/biome/is_overworld.json`, `tags/worldgen/biome/stink_horn_spawnlist.json`, `tools/worldgen/fungal_gardens_worldgen.py` |
| 06 T2 rest (CR-10), NQ-06-9 | `vampire_bat_spawnlist` is the explicit list nether wastes, crimson forest, basalt deltas, `hemomancy:phlegethontic_basin`. | `tags/worldgen/biome/vampire_bat_spawnlist.json` |
| NQ-06-14, NQ-06-15 | Polyp and Desiccant left as built; recorded as rulings. | `docs/HEMOMANCY_REFERENCE.md` 26.7 |
| P06 cleanup | `BloodDrunkPuppeteerPolishTest` and `VenomRibCentipedeRulesTest` are JUnit tests now, on the shared `ResourceReader` and `SourceAssertions` helpers. `VenomRibCentipedeRules.NATURAL_SPAWN_WEIGHT` deleted; the test reads the real weight (4) from the modifier. | the two tests, `VenomRibCentipedeRules.java` |
| 06 T6 (CR-23, CR-24), NQ-06-11, NQ-06-12 | Goat Congeatio with `cold_native` (was Ductilis). Polar bear: `requires_living_syringe` false (see For the owner). Ice Fish Congeatio. New `minecraft:wolf` Animus and Congeatio. 18 new Hemomancy files: Leech Animus, Fargone Animus + fungal, Thirster Mortem + fungal, Puppeteer Ductilis, Barbed Urchin Ferric, Peacock Spider Lux, Vampire Bat Tenebris, Hemolymphopoda Congeatio, Lantern Tick Lux, Myelin Borer Ductilis, Chthonian Ferric, Choir Keeper Tenebris, Bloodlicker Animus, Tooth Pecks Mortem; Enthralled Doll, Morphling Polyp, Antibody, Verdigris Moth with no temper. Filled: Abhorrent Thought Ductilis + Tenebris, Lump of Thought Ductilis, Eruptus Mortem, Hunter Fungus Animus, Fungling Animus, Chthonian Queen Ferric + Animus, Mnemonic Whale Ductilis, Bog Revenant Mortem, Mortarbound Ferric, Facsimile Wolf Animus. Every other syringe flag kept; new files allow ordinary vials (as before, when they had no file). 204 profiles now (124 + 80), 118 need a Living Syringe. | `data/*/blood_profiles/` |
| 06 T7 (CR-25), NQ-06-13 | Player kills only, the mod's convention for new material pools: Pyrosome 0 to 1 `cuttlefish_chromatophores`, Hagfish 0 to 2 `minecraft:slime_ball`, Blood Lantern Jelly 0 to 1 `enthralling_filament`, Bloodlicker 0 to 1 `sanguine_formation` (new file). Each adds 0 to 1 per Looting level. | `loot_table/entities/pyrosome.json`, `hagfish.json`, `blood_lantern_jelly.json`, `bloodlicker.json` |
| 06 T8 (CR-4), NQ-06-4 | `LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE` 8 to 25. Interval stays 160 ticks. | `LanternTickRules.java` |
| 06 T9 (CR-5), NQ-06-5 | HP / armour / damage / speed: Chitinite 10/4/1/0.25, Fervent Chitinite 18/8/3/0.28 (its own `setAttributes`, used in `EntityInit`), Fungling 10/0/2/0.32, Enthralled Doll 5/0/2/0.36, Barbed Urchin 10/3/1/0.3. The urchin's touch reads its attack attribute plus its puff state (was the literal 1). The grown brood still ends at 30 HP and 8 bite. | `AbstractChitiniteEntity.java`, `FerventChitiniteEntity.java`, `FunglingEntity.java`, `EnthralledDollEntity.java`, `BarbedUrchinEntity.java`, `EntityInit.java` |
| 06 T10 (CR-2), NQ-06-2, NQ-06-3 | New `GardensRosterRules` (the table) and `GardensRosterSpawnEvents` (on `FinalizeSpawnEvent`). A roster mob that spawns `NATURAL` or `CHUNK_GENERATION` in the Gardens dimension gets base HP / armour / damage: Fungling 16/0/4, Chthonian 30/6/6, Fargone 36/0/7, Thirster 44/0/8, Lump of Thought 52/0/9, Hunter Fungus 56/4/10, Eruptus 64/4/11, Abhorrent Thought 90/8/13, then full health. Base values save with the mob. The Overworld biome, the savanna mounds and the brood (which calls `finalizeSpawn` itself as `MOB_SUMMONED`) are untouched. The Eruptus lunge hits through `doHurtTarget`, so it reads the raised attribute. | `common/entity/mob/GardensRosterRules.java`, `common/event/GardensRosterSpawnEvents.java` |
| 06 T11 (CR-14), NQ-06-6 | `WillCombatRules.sentStats`: health max(60, max health x 3 + 30 per Degree over 6), damage 8 + 2 per Degree over 6. Speed unchanged. Broken Wills unchanged. | `WillCombatRules.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| 06 T12 (CR-7), NQ-06-7, NQ-06-8, NQ-06-10 | Not in this package's brief. |
| 06 T13 (VX-16) | Not in this package's brief. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: first run 1 failure, `FieldMycologyResourceTest.fungalSurveySpecimensComeFromTheGardensFungusSelectorAndDropThemselves` (it wanted the Gardens biome to list the field fungi itself). Rewritten on purpose (below). Second run: BUILD SUCCESSFUL, 6,087 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B13-unit2.txt`). That is 6,072 after B12 plus 15.
- `./gradlew.bat runGameTestServer --offline --console=plain`: first run 3 failures, all old pins on tempers this package changed (below). Second run: "All 1974 required tests passed :)" (`build/build2/B13-gametest2-latest.log`). That is 1,971 after B12 plus 3 new.
- Feature order check: `./gradlew.bat runPelagicValidationServer -PpelagicRun=b13` generates a seeded noise Overworld with the Gardens region, so a feature order cycle would crash it. Result: the region registered, chunks generated for 43 seconds, "All 1 required tests passed :)", BUILD SUCCESSFUL, no feature order error (`build/build2/B13-pelagic-overworld.txt`; the world is in `build/pelagic-worlds/42-b13/`).
- Added: `OverworldFungalGardensSpawnResourceTest.theOverworldGardensIsAnOverworldBiomeWithVanillaOres`, `noMooshroomRowThatCanNeverSpawn`, `noFeatureModifierAddsAFeatureTheGardensAlreadyLists` (guards the cycle); `GardensRosterRulesTest` (3, the table, the climb, the gate); `WillSentStatsTest` (3, the 60/90/120 and 8/10/12 curve, the floor, Broken Wills unchanged); `LanternTickRulesTest.aLatchedTickDrainsTwentyFiveMillilitresABiteAndOutrunsRegeneration`; GameTests `EcologyGameTests.theTemplateMobsHaveTheirOwnNumbers`, `onlyNaturalGardensSpawnsTakeTheRaisedRoster`, `BloodProfileGameTests.goatsAndPolarBearsGiveACleanCongeatioDrawToALooseVial`.
- Converted to JUnit (they were `main` methods the suite never ran): `BloodDrunkPuppeteerPolishTest`, `VenomRibCentipedeRulesTest`, `LanternTickRulesTest`.
- Flipped on purpose: `VampireBatColonyResourceTest.coloniesLiveOnlyInTheNether` is now `coloniesLiveInTheNetherButNotItsWarpedOrSoulSandBiomes` (the four biomes, no warped forest, no soul sand valley, no whole-Nether tag). `IceFishResourcesTest` and `IceFishGameTests.coldSpawnTablesEggAndPureAquaticProfileAreLoaded` expect Congeatio. `PelagicResourcesTest.sharedMaterialsDoNotInventAnIngredientForEveryCreature` expects the Pyrosome and Hagfish drops and allows the vanilla slime ball. `MaterialAcceptanceGameTests.cowAndGoatBatchesConserveContainersThroughReload` expects Congeatio's enzyme from a goat. `BloodProfileGameTests.migratedProfilesRetainCentrifugeOutputsAndTargetAffinity` lets a Fungling vial give fungus or Animus's enzyme. `FieldMycologyResourceTest` checks the Gardens is in `is_overworld` and the field-fungi modifier reaches it. `WillCombatRulesTest` (a `main` method, still not run by the suite) pins the new formula text.
- Test pins edited: `legacy_memberships.json` gained or changed the 33 profiles above; `restricted_entities.json` lost `minecraft:polar_bear`.
- `python gradle/source-text-impact-map.py` regenerated the impact map.
- No client launched: no row changes a look, a screen or a shader.

### Docs

- `docs/HEMOMANCY_REFERENCE.md`: 26.7 "Rulings that bind" now lists NQ-06-1 with C-5, CR-2 with NQ-06-2 and NQ-06-3, CR-4 with NQ-06-4, CR-5 with NQ-06-5, CR-10 with NQ-06-9, NQ-06-14 and NQ-06-15, CR-14 with NQ-06-6, CR-23 and CR-24 with NQ-06-11 and NQ-06-12, CR-25 with NQ-06-13, and the P06 cleanup (answers dated 2026-11-14). The not-built list keeps CR-7 and VX-16. Bestiary rows (Chitinite, Fervent Chitinite, Fungling, Enthralled Doll, Barbed Urchin, Lantern Tick, Vampire Bat), the loot table, the Gardens biome row, the profile count, the Ice Fish note and the Will section updated. This section is the creatures' topic doc; there is no other.
- `wiki/Blood-Profiles.md`, `wiki/World-Content.md`, `wiki/Injectable-Blood-Vials.md`, `docs/PELAGIC_ECOLOGY.md`, `docs/audits/2026-11-02-cleanup/README.md` (C-5 done), chunk 06 status lines T1, T2, T6 to T11.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows NQ-06-3, NQ-06-4, NQ-06-5, NQ-06-6, NQ-06-12/NQ-06-11, NQ-06-13 (the subsection is now empty); section 2 rows NQ-06-1, C-5 mooshroom, NQ-06-2, NQ-06-9, NQ-06-14, NQ-06-15, P06 cleanup; section 3 rows P06 cleanup, 06 T1 rest, 06 T2 rest, 06 T6, 06 T7, 06 T8, 06 T9, 06 T10, 06 T11. The area count reads 2 rows, 1 ready, 1 waiting. Section 4: the tuning row now reads "now (B13)", new row B13 (NQ-06-1, NQ-06-12).

### For the owner

- No new or changed lang keys.
- Your words over the recommendation: NQ-06-12 said "A goat then gives a clean Congeatio draw so do polar bears." The polar bear was already Congeatio but needed a Living Syringe. I read "clean draw" as the recommendation meant it, a Congeatio draw to an ordinary vial, so the polar bear no longer needs the syringe. Say if you meant only the Temper and I will put the flag back.
- Values I chose: the sent Will health has a floor of 60 (a player with less than 20 max health still meets the D6 test; the old formula had a floor of 28 for the same reason). The raised roster also applies to chunk-generation spawns in the dimension, not only the natural spawn cycle. The four small drops need a player kill, like the mod's other new material pools. New profile files allow ordinary vials, as these creatures did with no file.
- Surprising: joining `is_overworld` would have listed the field fungi twice in the Gardens biome and crashed worldgen with a feature order cycle. The stink horns had to move to the shared modifier too, or the swamp's order (fungi then stink horns) would clash with the Gardens' order. The Gardens keeps the same flora; only where it is listed changed. Also, a Fungling vial in the Centrifuge now gives either infected fungus or Animus's enzyme, at random, because it has a Temper now.
- `tools/worldgen/fungal_gardens_worldgen.py` is ignored by git and `OverworldFungalGardensSpawnResourceTest.java` was untracked, so the restore point does not cover them. My change to each is small: the generator lost its mooshroom row and gained a comment; the test gained three cases beside the old one.

### Verification (round 1)

- Scope: only Creatures and spawns rows. 06 T12 and 06 T13 untouched. Nothing from the held or reversed list. No lash, HUD or held manipulation file in the diff.
- Values match the answers: the T9 table in five `setAttributes` (Fervent Chitinite now its own in `EntityInit`), the T10 roster in `GardensRosterRules` gated on the Gardens dimension and NATURAL or CHUNK_GENERATION, sent Wills 60/90/120 HP and 8/10/12 damage, Lantern Tick 25 mL per 160 ticks, all 33 profiles as in the T6 table (goat Congeatio with `cold_native`, wolf Animus and Congeatio), the four drops, the explicit bat list, the Gardens in `is_overworld` with the 25 vanilla ores, the mooshroom row gone.
- Tests: each row has a test that fails without it (`GardensRosterRulesTest`, `WillSentStatsTest`, `LanternTickRulesTest`, the two new ecology GameTests, the goat and polar bear GameTest, the bat and Gardens resource tests). The flipped pins (Ice Fish, goat enzyme, Fungling centrifuge, bat list, Pyrosome and Hagfish loot, field fungi) follow the rulings. The Puppeteer test kept every assertion when it became JUnit. Nothing weakened.
- Unit suite: BUILD SUCCESSFUL, 6,087 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B13-verify-unit.txt`).
- Combined GameTest run: "All 1974 required tests passed :)", BUILD SUCCESSFUL (`build/build2/B13-verify-gametest-latest.log`).
- AGENTS.md: no payload, lang key, blood payment, one-time gift or particle change. Nothing removed that needed `removed_assets/`.
- Docs match the tree. Only this area's rows left `docs/OPEN_QUESTIONS.md`; rulings are in `docs/HEMOMANCY_REFERENCE.md` 26.7.
- Note, not blocking: the polar bear's Living Syringe flag was cleared on a reading of "so do polar bears". The answer may only mean the Temper. It is flagged for the owner in the log and in section 4.
- No client launched: no row changes a look, screen or shader. Nothing left running.

## B14. The Phlegethosoma summon

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 06 T12 (CR-7) natural spawn | The natural spawn is gone: `add_phlegethosoma.json` and its spawn placement rule (`PhlegethosomaSpawnRules.canSpawn`) are removed. `validPool` stays. Both old files are kept in `removed_assets/2026-11-15-phlegethosoma-natural-spawn/`. The registry-ids snapshot loses `neoforge:biome_modifier hemomancy:add_phlegethosoma` on purpose. | `EntityInit.java`, `PhlegethosomaSpawnRules.java`, `src/gameTest/resources/frozen/registry-ids.txt` |
| 06 T12 (CR-7) summon, NQ-06-7 | New ichor link in the Projection chain, ahead of chum: `PhlegethosomaSummoning.tryProject`. In the Nether's Phlegethontic Basin, a pour into a 7 by 7 ichor pool pays 2 mL a tick through `BloodAccounting` (source `phlegethosoma_summon`) and adds it to that pool's count (pours within 3.5 blocks share one count). The count fades like chum, half in 45 seconds (`ChumRules.fade`). At 500 mL (about 14 seconds of pouring) one Phlegethosoma rises at the pour: homed there, persistent, targeting its caller, with its usual 60-tick emergence. Refused, at no cost and with an action-bar line: Peaceful, a narrow pool, the caller's last summons still out, another living one within 64 blocks. Visuals: blood cells and the chum bloom on the ichor while pouring, a larger bloom at the rise. No vanilla particles, no payload. | `PhlegethosomaSummoning.java`, `PhlegethosomaSummons.java` (level saved data `hemomancy_phlegethosoma_summons`), `PhlegethosomaSummonRules.java`, `BloodProjectionItem.java` |
| NQ-06-8 | A summoned one with no target for 60 seconds (1,200 ticks) dives: hazards cleared, bar hidden, invulnerable, a chum bloom and the breach's ripple ring, then it is discarded 40 ticks later with no loot and no experience. One per player: the saved data keeps each player's current summons; an older one that is not current dives the next time it ticks, so a summons in an unloaded chunk never doubles up. The record is released when the boss is killed or discarded, not when its chunk unloads. A killed summons drops its usual loot. Spawn-egg and `/summon` Phlegethosomas have no caller and never dive. | `PhlegethosomaEntity.java` |
| NQ-06-10 | Any player with awake blood (`volume.isActive()`) may summon. Teaching: one new Alchemist line on the D5 basin commission (shown after the task line), and a new Liber page, Cosmic Forces "The Phlegethosoma", which opens at D5. | `PhlegethonticCommissionDialogue.java`, `dialogue_briefs/alchemist.json`, `books/libersanguinium/cosmic_forces/pages/phlegethosoma.json`, `LiberEntryDefinitions.java`, `LiberKnowledgeHelper.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| Review client for the pour and dive | The call needs the Nether's Basin biome, which no review fixture stages, and the looks are existing effects (the chum bloom and the breach ripple ring). Building a Basin scene would grow the row past its plan. The owner sees it in play (section 4, CR-7). |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,092 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B14-unit.txt`). That is 6,087 after B13 plus 5.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1979 required tests passed :)" on the first run (`build/build2/B14-gametest-latest.log`). That is 1,974 after B13 plus 5.
- After the fix: unit suite 6,092, 0 failed; combined GameTest "All 1980 required tests passed :)"; playthrough "All 433 required tests passed :)" with the baseline's 59 optional failures. See "Fix" below.
- Added: `PhlegethosomaSummonRulesTest` (5: a held pour reaches 500 mL in 250 to 300 ticks, a two-second break costs only a few ticks, the count halves in 45 seconds, it stops at 500, the dive comes at 1,200 ticks). GameTests in `PhlegethosomaSummonGameTests` (suite `phlegethosoma_validation`, one batch each): `fiveHundredMillilitresCallsOneAndASecondCallIsRefused`, `sleepingBloodCannotCallAndTheLiberPageOpensAtDegreeFive` (added in the fix), `aNarrowPoolOrAPoolOutsideTheBasinIsRefused`, `aBrokenPourFadesLikeChum`, `aSummonsWithNothingToFightDivesAfterAMinuteAndLeavesNothing`, `anOlderSummonsDivesWhenItsCallerHasAnother`. The test level is the Overworld, so they pour through `PhlegethosomaSummoning.pour` and check `tryProject` only for its refusal outside the basin.
- Changed on purpose: `SculptingGuardrailTest.eachVerbChainAsksTheDispatcherAtItsPinnedPlace` pins the new link between `GroundNetwork.tryProject(` and `ChumPools.tryProject(`. The frozen registry list lost the biome modifier line.
- `python gradle/source-text-impact-map.py` regenerated the impact map.
- No client launched (see Skipped). Nothing left running.

### Docs

- `docs/HEMOMANCY_REFERENCE.md` 26.7: CR-7 with NQ-06-7, NQ-06-8 and NQ-06-10 recorded as built rulings (answers dated 2026-11-14); the "not built yet" CR-7 line removed; the Phlegethosoma status paragraph no longer describes natural spawning. This section is the creatures' topic doc.
- `docs/BOSSES.md`: CR-7 marked built, NQ-06-7, NQ-06-8 and NQ-06-10 under "Rulings that bind", removed from the planned defaults, the open-questions pointer and "Not built yet"; a play row added. `docs/LORE_REFERENCE.md` (it rises to blood poured in its ichor; needs only awake blood), `wiki/Phlegethontic-Nether.md`, `docs/TESTING.md` (the summon tests), chunk 06 status line T12.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows NQ-06-8 and NQ-06-10; section 2 row NQ-06-7; section 3 row 06 T12 (CR-7). Section 4 row CR-7 now reads "now" and says what to play and read. The area counts at the top were left alone.

### For the owner

- New lang keys: `hemomancy.alchemist.phlegethontic_commission.phlegethosoma` ("Hold Blood Projection on a broad pool of that ichor and keep it there until about 500 mL has gone in. Something comes up to take it. The field notes call it the Phlegethosoma, and they say it fights whoever fed it. Don't pause long, because the ichor thins what you've put in. If it's left with nothing to fight, it goes back under within a minute."), `message.hemomancy.phlegethosoma.summon.answered` ("Something vast turns under the ichor and rises to the blood."), `.narrow` ("This ichor is too narrow to hold anything that would answer."), `.occupied` ("Something already moves in this basin."), `.peaceful` ("Nothing in the ichor answers."), `.yours` ("What you called up is still out there."), `blood_source.hemomancy.phlegethosoma_summon` ("Calling from the ichor"). New Liber page text in `cosmic_forces/pages/phlegethosoma.json` (literal JSON text, like the other pages).
- Values I chose: pours within 3.5 blocks of each other share one count; the dive lasts 40 ticks (2 seconds) before it is gone; the pool must pass the existing 7 by 7 habitat check at the surface; a refusal costs nothing; the Liber page opens at D5; spawn-egg and `/summon` Phlegethosomas have no caller and never dive.
- Your words over the recommendation: NQ-06-8 asked for one summons per player as well as the dive. A new call is refused while your summons is loaded and alive. If it sits in an unloaded chunk you can call again, and the old one dives the moment it loads.
- Not seen in a client: the pour bloom, the rise and the dive. The dive sinks the body only where ichor lets it; on a floor it simply goes after 2 seconds.
- Other areas left alone: the section 3 rows 16 T6 (Liber boss pages, which says the summoning paragraph waits on CR-7) and the Alchemist line-work row (her commission line about the call) still read as before. The page and the line now exist, so those rows can be shortened by their own packages.

### Verification (round 1)

- Result: not passed. One required playthrough test fails because of this package.
- Regression: `alchemistcommissions` (playthrough suite, required) fails: "the Basin bearing says something; said=[]". Cause: `LiberEntryDefinitions` now has `registerDialogue(PhlegethonticCommissionDialogue.BEARING, PHLEGETHOSOMA)`. `DialogueOptionPacket.accept` returns as soon as `LiberKnowledgeHelper.handleDialogueEvent` unlocks a page, so the first time a player asks the Alchemist for the Basin bearing she says nothing and gives no bearing. Fix: drop that `registerDialogue` line (the page still opens at D5), or open the page from the bearing route without swallowing the event. Then rerun the playthrough suite. The baseline and every earlier playthrough run (B07 to B10) had all required tests passing.
- Unit suite: BUILD SUCCESSFUL, 6,092 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B14-verify-unit.txt`).
- Combined GameTest run: "All 1979 required tests passed :)" (`build/build2/B14-verify-gametest-latest.log`).
- Playthrough suite: "1 required tests failed", `alchemistcommissions`. The 59 optional failures are the baseline's 59 names (`build/build2/B14-verify-playthrough-latest.log`, `B14-verify-playthrough-optional.txt`).
- Scope: only CR-7 with NQ-06-7, NQ-06-8 and NQ-06-10. Nothing held or reversed. No lash, HUD or held manipulation file touched.
- Values match the answers: 500 mL per pool at 2 mL a tick, chum fade, dive after 1,200 ticks without a target, gone 40 ticks later with no loot or XP, one summons per player, awake blood gate, the Alchemist line and a Liber page.
- Tests: `PhlegethosomaSummonRulesTest` and the five `phlegethosoma_validation` GameTests cover the summon, fade, dive and one-per-player. The natural spawn removal is held by the registry-ids snapshot. No test covers the new Alchemist line or the page unlock, and no test pours with inactive blood. The `SculptingGuardrailTest` pin was rewritten on purpose for the new link.
- AGENTS.md: blood is paid through `BloodAccounting` with a named source; new text is in `en_us.json`; no payload; no vanilla particles; old files kept in `removed_assets/2026-11-15-phlegethosoma-natural-spawn/`. Minor: the dive's glow builds `ColorParticleData` inline (copied from the same file) instead of `HemoParticleData.glow`.
- Docs match the tree. Only this package's rows left `docs/OPEN_QUESTIONS.md`; rulings are in `docs/BOSSES.md` and `docs/HEMOMANCY_REFERENCE.md` 26.7. Note: section 3 row 16 T6 also plans a Phlegethosoma Liber page, so that package should reuse this one rather than add a second.
- No client launched: the summon look reuses existing effects and needs the Nether Basin. Nothing left running.

### Fix

- Blocking regression fixed: `alchemistcommissions` failed because the Basin bearing event was also a Liber unlock, and `DialogueOptionPacket.accept` stops after an unlock, so the Alchemist said nothing. The `registerDialogue(PhlegethonticCommissionDialogue.BEARING, PHLEGETHOSOMA)` line is gone and the page's `DIALOGUE` source with it. The Phlegethosoma page now opens at D5 only. The bearing dialogue is back to how it was before B14.
- Docs corrected to match: `docs/BOSSES.md` (NQ-06-10 line), `docs/HEMOMANCY_REFERENCE.md` 26.7 (NQ-06-10 line) and this section's Built and For the owner text no longer say the bearing opens the page.
- Test added: `sleepingBloodCannotCallAndTheLiberPageOpensAtDegreeFive` (`phlegethosoma_validation`, batch `fluke_summon_asleep`). Inactive blood pours nothing and pays nothing; the page is shut at D4 and open at D5. The Alchemist commission line itself still has no dedicated test.
- Style: the dive glow now uses `HemoParticleData.glow` instead of building `ColorParticleData` inline. The older inline copy at the end of the same file predates B14 and is left alone.
- Note for package 16 T6: reuse `cosmic_forces/pages/phlegethosoma` rather than adding a second Phlegethosoma page.
- Unit suite: BUILD SUCCESSFUL, 6,092 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B14-fix-unit.txt`).
- Combined GameTest run: first run failed the new test (its helper counted refusals as -1 mL; the test was wrong, not the code). Rewritten to assert each refused pour directly. Second run: "All 1980 required tests passed :)" (`build/build2/B14-fix-gametest-latest.log`). That is 1,979 plus the new test.
- Playthrough suite: "All 433 required tests passed :)", 59 optional failures, the same 59 names as the baseline (`build/build2/B14-fix-playthrough-latest.log`, `B14-fix-playthrough-optional.txt`).
- No client launched. Nothing left running.

### Verification (round 2)

- Result: passed. The tree is green and no problem is blocking.
- The round 1 regression is fixed: the `registerDialogue(BEARING, PHLEGETHOSOMA)` line and the page's `DIALOGUE` source are gone, and `alchemistcommissions` passes.
- Unit suite: BUILD SUCCESSFUL, 6,092 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B14-verify2-unit.txt`).
- Combined GameTest run: "All 1980 required tests passed :)" (`build/build2/B14-verify2-gametest-latest.log`).
- Playthrough suite: "All 433 required tests passed :)", 60 optional failures. They are the baseline's 59 plus `apotheosritewithoutrescue` (PT-88), the known flake the baseline names. Not a regression (`build/build2/B14-verify2-playthrough-latest.log`, `B14-verify2-playthrough-optional.txt`).
- Scope: only CR-7 with NQ-06-7, NQ-06-8 and NQ-06-10. Nothing held or reversed; no lash, HUD or held manipulation file touched.
- Values match the answers: 500 mL per pool at 2 mL a tick with the chum fade, a dive after 1,200 ticks without a target, gone 40 ticks later with no loot or XP, one summons per player, the awake blood gate, the Alchemist line and the Liber page (D5).
- Tests: `PhlegethosomaSummonRulesTest` and six `phlegethosoma_validation` GameTests cover the call, refusals, sleeping blood, fade, dive, replacement and the D5 page. The registry-ids snapshot holds the spawn removal. The Alchemist line has no test of its own (minor).
- AGENTS.md: `BloodAccounting` with a named source, text in `en_us.json`, no payload, custom particles only, old files in `removed_assets/2026-11-15-phlegethosoma-natural-spawn/`.
- Docs: minor wording drift. `docs/BOSSES.md` NQ-06-8 says an older summons "dives when its caller calls another", and HEMOMANCY_REFERENCE 26.7 says much the same. In the code a new call is refused while the old one is loaded and alive; the old one only dives when it was unloaded at the time of the new call. The For the owner text above has it right.
- No client launched: the looks reuse existing effects and need the Nether Basin. Nothing left running.

## B15. Bosses: Annetta's health, the boss tag readers, the Mycophant rematch

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 07 CR-16, NQ-07-1 | Annetta's max health is 51 (was 350): the 50 damage the break needs plus the 1 HP guard. The break rules (2400 ticks and 50 damage, held at 1 HP) are unchanged. | `AnnettaKnowlesEntity.java` |
| 07 T1 remainder (lists), NQ-07-3 | Rite sacrifices read the boss tag: `processSacrifices` passes `HemoEntityPredicates.isBoss(entity)` instead of the Dragon and Wither pair. `puppet_attention_immune.json` is now only `#hemomancy:bosses`. Kill blood gain x5 stays Dragon and Wither only, the one named exception in `BossDefinitionGuardrailTest`. | `HarbingerCardinalRiteEvents.java`, `tags/entity_type/puppet_attention_immune.json` |
| 07 T1 remainder (Ringmaster), D-33 boss tag row | `hemomancy:circus_ringmaster` is in `hemomancy:bosses`. `ThreadRipperManip.protectedBody` no longer names `CircusRingmasterEntity` (the tag covers him; performers still named). | `tags/entity_type/bosses.json`, `ThreadRipperManip.java` |
| NQ-07-2 | Nothing changed: the Seraphae Fragment and the Velorum Devotee stay in the tag. Recorded as a ruling. | - |
| 07 T2 (test) | The burrow gate (first wound minus 2%, so 68%, or wings grown) and the idle threshold (24 ticks at or under the second wound, else 36) moved from inline literals into `VesperCombatRules.mayGroundBurrow` and `groundedIdleTicks`. Behaviour unchanged. | `VesperCombatRules.java`, `VesperTheCrownedRefusalEntity.java` |
| 07 T6 (PT-84), NQ-07-4 | A lost rematch re-arms after the cooldown, after every loss until a win. New server-only flag `mycophantRematchOwed` on the degree attachment (NBT key `mycophant_rematch_owed`, written only while set, not in `PacketSyncDegree`, so no payload change). `failAttempt` sets it when the lost fight was a rematch by a defeated player; `settleVictory` and `resetEndgameState` clear it. New rule `MycophantEncounterRules.shouldRearmRematch` (D8, Apotheos, defeated, owed, cooldown 0, in the Gardens, alive, not creative or spectator, no fight active). `tickGardensPlayer` now checks the Gardens and calls the new `tickHunt`, which starts the claim as a rematch when the rule holds. The expelled line is unchanged. | `MycophantEncounterManager.java`, `MycophantEncounterRules.java`, `InitiatoryDegree.java`, `IInitiatoryDegree.java` |
| 07 T7 (PR-9) | Ran `runBossPresentationReviewClient` (53 stills) and `runMycophantReviewClient` (303 stills). Both got past the Accessibility screen and opened their saves. Stills and an index are in `build/build2/B15-shots/`. The Mycophant notch shows at 60% and the Crowned Refusal notches at 70/40/10. | none (stills only) |

### Skipped

| Row or part | Why |
| --- | --- |
| 07 T4 (test): a live GameTest for the Vesper victory experience | The GameTest servers load no Chamber of Will dimension, and `VesperOrdealManager.completeVictory` returns at once for an owner outside it. A live test needs a Chamber in the GameTest server or a split of the guarded method that also moves the player through `exitChamber`, which grows the row past its plan. The source assertion in `EndgameBossRewardResourceTest` still pins the 180. The row stays in section 3. |
| T1: the one `AGENTS.md` line naming the boss helper | Not in this package's section 3 rows. The rule is in `ARCHITECTURE_STANDARDS.md`; the gap bullet in `BOSSES.md` stays. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,097 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B15-unit.txt`). That is 6,092 after B14 plus 5. Re-run after the doc edits: `./gradlew.bat test --rerun`, 6,097 tests, 0 failures, 0 errors (`build/build2/B15-unit2.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1980 required tests passed :)" (`build/build2/B15-gametest-latest.log`). Same count as after B14.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 59 optional failures (`build/build2/B15-playthrough-latest.log`, `B15-playthrough-optional.txt`). Against the baseline: `mycophantrematchline` left the list (now required and passing); `apotheosritewithoutrescue` (PT-88) joined it, the known flake the baseline names.
- Added: `BossDefinitionGuardrailTest.riteSacrificesAndMummerAttentionReadTheBossTag` and `theRingmasterIsABoss` (NQ-07-3, D-33). `VesperCombatRulesTest.theGroundedBurrowOpensTwoPercentUnderTheFirstWound` and `theIdlePauseShortensAtTheSecondWound` (T2). `MycophantEncounterRulesTest.aLostRematchReArmsOnceTheCooldownHasRun` (T6).
- Flipped on purpose: `ManipulationBalancePass2TargetingGameTests.bossPredicateCoversEveryHemomancyBossBody` asserted the Ringmaster is not a boss; it now lists him with the other bosses (D-33). `PlaythroughEndgameGameTests.mycophantRematchLine` asserted the rematch re-arms through `shouldAccumulate` and was optional; it now loses a rematch, checks the flag and cooldown, runs the cooldown down through `tickHunt` and checks the claim begins as a rematch, and is required (PT-84).
- Changed on purpose: `BossDefinitionGuardrailTest` lists only the kill blood gain file as the Dragon and Wither exception (rite sacrifices left it). `ScarletMummerRulesTest` now checks the attention tag holds `#hemomancy:bosses` instead of three hand-listed ids.
- `python gradle/source-text-impact-map.py` regenerated the impact map.
- Review clients: see Built, 07 T7. Both clients closed themselves after capturing; no Java process left running.

### Docs

- `docs/BOSSES.md`: status line; G-2c line updated; PT-84 marked built; NQ-07-1, NQ-07-2, NQ-07-3, NQ-07-4 and the D-33 boss tag row recorded under "Rulings that bind" with dates; C-7 line; planned defaults for those four removed; a B15 block under "Calls made without the user"; the open-questions pointer, "Not built yet" and known gaps updated (burrow gate now tested, Vesper live test skipped and why, the stale §Mycophant text already fixed); PR-9 stills captured; history row.
- `docs/HEMOMANCY_REFERENCE.md`: Annetta's 51 health in the bestiary row and the status line; the Mycophant rematch line ("a failed rematch requires another lure" replaced); the boss definition paragraph (the Ringmaster in, rite sacrifices and attention read the tag, kill blood gain the one exception).
- `docs/architecture/ARCHITECTURE_STANDARDS.md` (the "Is it a boss" rule and the guardrail index row), `docs/MANIPULATIONS_AND_CASTING.md` (G-2c line), `docs/CIRCUS.md` (D-33: boss tag row built), `docs/PLAYTHROUGH_TESTS.md` (PT-84 built, the probe is required, the optional list count 5 to 4).
- `docs/owner_answers/07-bosses.md` status table: T1, T2, T4, T6, T7 and CR-16 updated.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 row NQ-07-1; section 2 rows NQ-07-2, NQ-07-3, NQ-07-4 and D-33 (boss tag row); section 3 rows 07 T7, 07 T2 (test), 07 CR-16, 07 T1 remainder (lists), 07 T1 remainder (Ringmaster) and 07 T6. Section 3 row 07 T4 (test) stays. Section 4: the NQ-07-1 row now says what to play; new rows NQ-07-4 (PT-84) and D-33 (boss tag); PR-9 and V-6b rows now point at the stills. The counts at the top were left alone.
- `docs/LORE_REFERENCE.md` needed no change: its Mycophant line already says 60%, and no lore line names Annetta's health or the rematch rule.

### For the owner

- No new or changed lang keys. The expelled line (`message.hemomancy.mycophant.expelled`) already promises the re-arm and is unchanged.
- Values I chose: the rematch owed flag is saved only while set, so saves and command fingerprints of players who never lost a rematch do not change. A re-armed rematch skips the hunt's whispers and visions: the claim starts on the first Gardens tick after the cooldown, as a rematch with the short opening. A lost first hunt does not set the flag (it already re-arms through exposure).
- Annetta at 51 health: her bar empties after about 50 damage, then she stands at 1 HP, fully frantic and with the tick fully swollen, until two minutes have passed. Raise the number if that reads as a stuck boss.
- The Ringmaster as a boss: he now gets every boss cap and immunity (halved Binding, the binding count, damage caps, no Paralysis, no leash, jar or Hematic Command), and the Scarlet Mummer cannot pull his attention. Also newly immune to the Mummer: the Naeglerophaeon, the Latent Infection, the Phlegethosoma, both Court bosses, the Elder Guardian, the Seraphae Fragment and the Velorum Devotee. No Hemomancy boss can now be a rite sacrifice.
- Stills to look at (PR-9): `build/build2/B15-shots/index.txt`, with `boss-presentation/` (53) and `mycophant/` (303). Notches: `boss-presentation/boss-bars_mycophant-40.png`, `boss-presentation/boss-bars_vesper-40.png`. Capture: `mycophant/mycophant-capture_pov-*.png`. The spore colours are server particles and may not show in the point-of-view capture stills; watch them in a client.
- Surprising: the Mycophant review's real-player hold probe logged one FAIL, "last brood cut free state=ATTACK outcome=-1 tendrils=4" (the other 12 checks pass). B15 did not touch the Last Brood; it may be an older fault or a timing flake in the probe. Not investigated here.
- Earlier review stills were moved, not deleted: `build/boss-presentation-review/screenshots-before-B15/` and `build/mycophant-client-review/screenshots-before-B15/`.

### Verification (round 1)

- Scope: the diff against `refs/build2/B15-before` touches only the Bosses rows. Nothing held or reversed is touched. No lash number, HUD or payload changed, so no protocol bump is needed. No lang keys, blood paths or particles changed.
- Values: Annetta `MAX_HEALTH` is 51. `processSacrifices` reads `HemoEntityPredicates.isBoss`. `puppet_attention_immune` is only `#hemomancy:bosses`. Kill blood gain stays the one Dragon and Wither exception. `circus_ringmaster` is in `hemomancy:bosses` and Thread Ripper no longer names him. The rematch flag is set on every lost rematch and cleared only on a win. The Saint adds stay in the tag.
- Tests: the new guardrail, Vesper and Mycophant rule tests and the two flipped GameTests match the rulings. None was weakened. `mycophantRematchLine` is now required and passes ("claim began=true as a rematch=true").
- Runs: unit suite BUILD SUCCESSFUL, 6,097 tests, 0 failures (`build/build2/B15-verify-unit.txt`). Combined GameTest run: "All 1980 required tests passed :)" (`build/build2/B15-verify-gametest.txt`). Playthrough: "All 433 required tests passed :)", 58 optional failures, the baseline minus `mycophantrematchline` (`build/build2/B15-verify-playthrough.txt`).
- Minor, not blocking: no test pins Annetta's 51 health (the plan named none). The `docs/BOSSES.md` status line still says "Left: the PR-9 stills" though the stills were captured; it should say the owner has still to look at them.

## B16. Saints: enrage dodges, the blood-debt flag, Chapel fixes, Velorum's text

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 08 T5 flake (FX-18) | `chapelWaveMobsLeaveNoLootOrExperience` now counts the orbs and drops in the dying tick, right after the two lethal blows, instead of 30 ticks later. Loot and experience fall in `die`, so nothing can drift, merge or be picked up by another test's player first. The test also checks both blows were lethal. The cooldown line now has its check: a D8 player who calls `begin` on a cooling focus gets `message.hemomancy.chapel.cooldown` and the trial does not start. | `ChapelRobustnessGameTests.java` |
| 08 T2 dodge 1 (SM-1, NQ-08-18) | `spawnSaintBoss` asks whether the waker or any living, non-spectator player in the chamber (the arena box from the tomb) is an Apotheos (`SaintEnrageRules.apotheosPresent`). A NONE door then wakes FORCED. PROPER and FORCED records are kept as they are. At the reward roll, `SaintEnrageRules.fragmentOnly(awakening, tombRecord, apotheosChallenger)` pays a fragment when the tomb record is NONE and the recipient or anyone in the chamber is an Apotheos. The stat enrage is not applied mid-fight. | `SaintEnrageRules.java`, `HarbingerSaintEncounterHooks.java`, `SaintTrialEntity.java` |
| 08 T2 dodge 2 (SM-1, NQ-08-19 A) | One peaceful sample per tomb, ever. `SaintSarcophagusBlockEntity.restoreDormant` no longer clears `sampleYielded`. The tomb block is placed fresh when a Saint returns, so the Saint now remembers the yield when it rises (`rememberTombSampled`, NBT `SaintTombSampled`, written only when true) and sets it back on the returned tomb. A later vial gets the existing "already yielded" line. | `SaintSarcophagusBlockEntity.java`, `SaintSarcophagusBlock.java` (javadoc), `SaintTrialEntity.java`, `HarbingerSaintEncounterHooks.java` |
| 08 T3 rest (SM-4, NQ-08-20) | `countsAsSpend` removed from `BloodAccounting.Source` (now `{id, label, category, reserveProtected}`, `Source.of(id, category, reserveProtected)`), from all 118 `Source.of` calls in 88 files and the one direct constructor in `ThelemicMemoryActivationService`. `BloodPaymentRules.settle` lost the parameter and the `addBloodSpend` call. `IBloodVolume.addBloodSpend` removed. The empty "Blood Debt Tracking" header in `BloodVolume.java` and the debt sentence in the `Source` comment removed. The `AGENTS.md` clause and `ARCHITECTURE_STANDARDS.md` updated. No player effect. | `BloodAccounting.java`, `BloodPaymentRules.java`, `IBloodVolume.java`, `BloodVolume.java`, every `Source` constant |
| 08 T4 rest (NQ-08-21) | No code. The split stays 5 as 2 / 2 / 1. Recorded as a ruling. | none |
| 08 T5 Vex (FX-18, C-9, NQ-08-23 A) | Seraphae's Downfall raises `seraphae_fragment` in place of `EntityType.VEX`. The fragment has a Chapel wave mode (`setChapelWave`, NBT `ChapelWave`): no parent and no 300-tick return. It follows the nearest player it may harm (`canHarmFor`) within its follow range (24), strikes the one it touches for its attack value (3) once every 20 ticks with Seraphae's own damage type (`saint_seraphae`), and ends only when a player's blow strikes it apart. Out of wave mode nothing changed. | `SeraphaeFragmentEntity.java`, `ChapelFocusBlockEntity.java` |
| 08 T5 Peaceful (FX-18, NQ-08-22 A) | `ChapelWaveEvents` answers `MobDespawnEvent` with DENY for any mob with the wave tag, so a wave never despawns, Peaceful included, and a Downfall cannot finish itself. | `ChapelWaveEvents.java` |
| 08 T6 rest (SM-3, NQ-08-24) | Velorum's Beholding gets four lines and a close (the four who followed him into stillness). `ChapelRules.narratesBeholding` now covers HEMORATH, SERAPHAE and VELORUM, every complex that has a Beholding. | `ChapelRules.java`, `en_us.json` |
| 08 T8 counter (SM-13 A) | Not built, by the answer: the entrance check and the spacing stay. Recorded as a ruling. | none |

### Skipped

| Row or part | Why |
| --- | --- |
| A review client for Seraphae's Downfall | The row changes which mob rises, not any art: a Chapel fragment uses the fragment's existing look, which `SeraphaeRadianceGeometry` draws for every fragment near the camera, Seraphae or not (checked in code). No client was launched, so nobody has seen the wave in motion. It is in section 4 for the owner. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,099 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B16-unit.txt`). That is 6,097 after B15 plus 2. The first run failed `SaintCombatKitGuardrailTest.saintsUseTheKitNotTheLegacyPieces`: the fragment's strike used `mobAttack`, which the kit bans in Saint files. It now uses `saint_seraphae`, and the rerun is green.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1984 required tests passed :)" (`build/build2/B16-gametest-latest.log`). That is 1,980 after B15 plus 4. The Saint fight suites run inside it.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 433 required tests passed :)", 59 optional failures (`build/build2/B16-playthrough-latest.log`, `B16-playthrough-optional.txt`). Against the baseline: `mycophantrematchline` left the list (B15 made it required), and `apotheosritewithoutrescue` (PT-88, the known flake) is in it, the same as B15.
- Added (unit): `SaintEnrageRulesTest.anApotheosAnywhereInTheChamberWakesAnUnopenedTombEnraged` and `aLateApotheosAtAnUnopenedTombGetsOnlyAFragment` (NQ-08-18). `BloodAccountingGuardrailTest.theRetiredSpendCountStaysGone` replaces `bloodSpendIsCountedOnlyByBloodAccounting`: no `addBloodSpend(` and no `countsAsSpend` anywhere in main (NQ-08-20).
- Added (GameTest): `SaintEncounterGameTests.anApotheosInTheChamberMakesAnUnopenedTombWakeForced` (a non-Apotheos relic wake beside an Apotheos rises FORCED) and `aReturnedTombRefusesASecondPeacefulDraw` (a sampled tomb keeps its yield through a rise and a return, and a vial draws nothing). `ChapelRobustnessGameTests.seraphaesDownfallRaisesHerLightFragments` (three wave fragments, still there after 45 ticks with no Seraphae, one blow ends one) and `aPeacefulDownfallDoesNotFinishItself` (Hemorath's and Seraphae's waves pass a Peaceful despawn check and the Downfall stays on step 1; the server difficulty is set back in the same tick).
- Changed on purpose: `chapelWaveMobsLeaveNoLootOrExperience` checks in the dying tick (the flake fix) and lost its 80-tick timeout. `aReloadKeepsTheTrialTimerAndTheFailCooldown` also checks the cooldown line. `ChapelRulesTest.theNarratedBeholdingsHaveALineForEachFlashAndAClose` now covers Velorum; its old "Velorum waits on NQ-08-24" assertion is flipped to Putriciel, who has no Beholding. `BloodPaymentRulesTest` lost the spend flag argument and its `spend` loops (the outcome never depended on it). `IronBrazierOfferingSourceTest` pins the new `Source.of("brazier_lighting", Category.RITE, false)` text. `SeraphaeAnchorBindingRulesTest` and `BloodPaymentRulesTest.sourcesAreLabelledByTheirIdKey` lost their `countsAsSpend()` checks. None was weakened: each dropped assertion read the removed flag.
- `python gradle/source-text-impact-map.py`: no change against the restore point.

### Docs

- `docs/SAINT_MAUSOLEUMS.md`: status line; the Beholding and Downfall rows; the sarcophagus (one sample ever) and awakening (an Apotheos in the chamber, the late Apotheos fragment) paragraphs; "Rulings that bind" gained NQ-08-18, NQ-08-19, NQ-08-20, NQ-08-21, NQ-08-22, NQ-08-23, NQ-08-24 and SM-13 (dated 2026-11-14, the day of the answers), and the SM-1, SM-4, FX-18 and C-9 lines lost their "not built" parts; a B16 block in "Calls made without the user"; the open-questions pointer lost NQ-08-19, NQ-08-21, NQ-08-23 and SM-13; "Proof owed" lost the not-built and stale-code lines, the Chapel robustness and flaky-test lines are updated, and a line says the fragments are unseen in a client; a history row.
- `docs/HEMOMANCY_REFERENCE.md`: the blood debt bullet (the flag is gone), the Chapels bullet (fragments, Peaceful, Velorum's lines), the Awakening bullet (NQ-08-18), and the sarcophagus extraction list (one sample ever, `sampleYielded` kept on return).
- `docs/LORE_REFERENCE.md` §10: Velorum's Beholding memory in place of "still to be written".
- `wiki/Saints-and-Encounters.md`: Velorum's Beholding, the Peaceful wave, her fragments, the Apotheos-in-the-chamber wake and the one peaceful syringe.
- `AGENTS.md` (the blood clause: `Source{id, category, reserveProtected}`, no `addBloodSpend(`) and `docs/architecture/ARCHITECTURE_STANDARDS.md` (the blood row and the guardrail index row).
- `docs/owner_answers/08-saints-and-mausoleums.md` status table: T2, T3, T4, T5, T6, T8 and T14 updated.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows NQ-08-19, NQ-08-23, SM-13 and NQ-08-21; section 2 rows NQ-08-18, NQ-08-20, NQ-08-22 and NQ-08-24; section 3 rows 08 T5 flake, 08 T2 dodge 1, 08 T2 dodge 2, 08 T3 rest, 08 T4 rest, 08 T5 Vex, 08 T5 Peaceful, 08 T6 rest and 08 T8 counter. Section 4: the SM-3 row now covers Velorum's lines; new rows for the fragments and the Peaceful wave (NQ-08-23, NQ-08-22) and for the two closed dodges (NQ-08-18, NQ-08-19). The counts at the top and other areas' rows were left alone (the "no speaker" row in another area still names NQ-08-24 as chunk 08's).

### For the owner

- New lang keys (no key changed), Velorum's Beholding, written as narration with no Saint speaking and no martyr framing. The first follower is "he" so the ending's "the first who stilled with him turns his head" still fits:
  - `message.hemomancy.chapel.beholding.velorum.1`: "The first kneels beside him and slows his heart to match the Saint's. He asked for this."
  - `message.hemomancy.chapel.beholding.velorum.2`: "The second opens her arms to be stilled. The cold climbs her veins, and the arms stay open."
  - `message.hemomancy.chapel.beholding.velorum.3`: "The third asks to stop after a day. Velorum tells him the asking is the last desire to give up."
  - `message.hemomancy.chapel.beholding.velorum.4`: "The fourth tries to stand. Her blood has already set, and she stays half risen, reaching for the door."
  - `message.hemomancy.chapel.beholding.velorum.done`: "Four followed him into stillness, and none of them can leave it. He calls each one a success."
- Values I chose for the Chapel fragment: it strikes for 3 (the fragment's existing attack value) once every 20 ticks while it touches you, chases within 24 blocks (its existing follow range), moves at the fight fragment's glide speed (0.12 a tick), and uses Seraphae's damage type. One blow of any strength ends it, as in her fight. Held Blood Absorption only draws fragments of her fight; a Chapel fragment is not drawn in. In Peaceful its strike does nothing, like any mob's, so a Peaceful Downfall is a walk where you still have to strike every fragment.
- The late-Apotheos check counts the reward's recipient and everyone in the chamber when the trial ends.
- Surprising: the tomb block is placed new when a Saint returns, so the yield had to ride on the Saint (like the door record already did). A Saint that cannot return to its tomb (the space is blocked) loses nothing new: the tomb is not there.
- Not seen in a client: Seraphae's Downfall with her fragments. Watch it in your session (section 4).

### Verification (round 1)

Checked the tree against `refs/build2/B16-before`, not the report. Pass.

- Scope: only the brief's rows. No lash numbers, HUD, held rows or the Silent Severance name touched. The 105 tracked files are the Saint, Chapel and blood-accounting files, the tests and the docs; every other file only drops the third `Source.of` argument. A script check found no `reserveProtected` value flipped. The three network files only change a `Source` constant, so no payload or protocol change.
- Values: NQ-08-18 (NONE door wakes FORCED with an Apotheos waker or anyone living and non-spectator in the arena; PROPER stays; late Apotheos gets the fragment only), NQ-08-19 A (`restoreDormant` keeps `sampleYielded`; the Saint carries it back), NQ-08-20 (flag, `addBloodSpend` and header gone; guardrail now forbids both), NQ-08-22 (wave tag denies `MobDespawnEvent`), NQ-08-23 A (`seraphae_fragment` in a parentless wave mode), NQ-08-24 (five Velorum keys, Velorum in `NARRATED_BEHOLDINGS`). NQ-08-21 and SM-13 need no code and have none.
- Tests: each row has a test that would fail without it. The Peaceful GameTest calls `checkDespawn` under Peaceful, so it proves the deny reaches the Peaceful branch. The dropped `countsAsSpend` assertions only read the removed flag.
- Runs (08:00 to 08:09 on 2026-11-15): unit suite BUILD SUCCESSFUL, 6,099 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B16-verify-unit.txt`). Combined GameTest run: "All 1984 required tests passed" (`build/build2/B16-verify-gametest-latest.log`). `runSaintKitGameTestServer`: all 9 passed. `runRegistrationGameTestServer` (holds `ChapelRobustnessGameTests`): all 60 passed. No server left running.
- Docs: rulings recorded in `docs/SAINT_MAUSOLEUMS.md` "Rulings that bind"; only chunk 08 rows left `docs/OPEN_QUESTIONS.md`, and three section 4 rows were added. No client launched: no row changes art, and the fragment's look is unchanged.


## B17. Saint gifts, part one: the trial record, the weave, Crimson Tithe and Unclosing Eye

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 08 D-1 build: spec refresh | A "Refresh, 2026-11-15" block at the top of the spec: what B17 built, what is left, and where the code moved on (Temper rename, freeform verbs, living HUD, no `countsAsSpend`, the 50% charge floor, the Tithe's upkeep through the channel). | `docs/superpowers/specs/2026-11-06-canon-memory-rework-design.md` |
| NQ-08-2 A, NQ-08-3 (trial record) | `SaintTrialEntity.endTrial` records the Saint for the reward's recipient and every non-spectator challenger in the arena, at any rewarded ending, proper or forced. Stored in `InitiatoryDegree` as `saint_trials_ended` (one flag per Saint, written only when one is set, copied on death, kept through an endgame reset, never inferred). `CanonGifts` maps each gift to its Saint. The Loom refuses the weave before any blood ("Requires that you have ended %s's trial."), and the grant returns a new status `TRIAL_NOT_ENDED` (brazier line, Loom readout). Creative right-click teaching is unchanged. | `InitiatoryDegree.java`, `IInitiatoryDegree.java`, `SaintTrialEntity.java`, `CanonGifts.java`, `KnownManipulationGrantHelper.java`, `SomaticLoomBlockEntity.java`, `MemoryBrazierRite.java` |
| NQ-08-1 A (weave) | All four gifts are Perfectus (the D8 floor stays). The four recipes take 2 + 2 enzymes and 300 blood. A Perfectus weave needs a Cortical loom (stage 2). | `ManipulationInit.java`, `recipe/memory_weaving/memory_<gift>.json` |
| Spec build order step 1 (`never_sold`) | The four residuum fragments and the four `memory_<gift>` items joined the trade `never_sold` tag. | `tags/item/trade/never_sold.json` |
| NQ-08-14 | White Verdict, Absolute Stillness, Carrion Communion and Fruiting Contagion no longer hang from a gift in `ManipulationTreeInit`. The gifts' own positions are unchanged (they were already leaves at their branches' ends). | `ManipulationTreeInit.java` |
| NQ-08-17 | Every gift memory's tooltip names its source ("Canon Memory of Hemorath, the Hollow Vessel", "... of Seraphae, the Chain Saint", "... of Putriciel, the Befouled Saint", "... of Velorum, Saint of the Endless Hour") and says only one who ended the trial can weave or learn it. | `BloodMemoryItem.java`, `en_us.json` |
| Spec 2.8 (`CanonGiftState`) | One session holder for the gifts' live state (Tithe holds and releases, Eye beams, Lapses, growth), cleared by `TransientServerStateEvents` on logout and server stop and by `CanonGiftEvents` on death and dimension change. New forms appended to `ManipulationVisuals.Form`: `TITHE_CHAIN`, `TITHE_VESSEL`, `TITHE_RELEASE`, `EYE_CHARGE`, `EYE_BEAM`, `EYE_APERTURE`, `EYE_GROWTH` (appended, so no ordinal moved and no protocol bump). | `CanonGiftState.java`, `CanonGiftEvents.java`, `TransientServerStateEvents.java`, `ManipulationVisuals.java` |
| D-1 Crimson Tithe (spec 3.1, NQ-08-6, NQ-08-12) | Continuous, Heart, 25 Animus alignment. 150 to open (`crimson_tithe_open`) plus the channel's 40 mL a second; held up to 160 ticks; cooldown 400 from the release. Up to five chains within 10 blocks, nearest first, in sight (Obscured only within 4, concealed only when Illuminated); latch a DIRECT Animus hit for 3 laying Pressure; relatch every 20 ticks; break past 12 blocks or 10 ticks out of sight. Each 5 ticks a chain drinks 25 (10 bloodless, x0.6 Searing, x0.5 boss) as a periodic 1.5 hit, or 25 blood from a blood-active player (`crimson_tithe_taken`), health from an inactive one, only where `canHarm` allows. Chains haul 0.033 a tick, 0.05 on Lodestone, never a boss. Heal 1 per 50 drawn, at most 10 a hold. Leak `1000 x (0.0006 + 0.006 f^2)` a tick; an iron per 150 leaked, up to 4. Brimming over 70%: 20% slower (attribute) and 20% more damage taken (`DamageStages` defenders). Release (let go, out of blood, Disrupted, 160 ticks): `6 + iron` lanes up to 10, `3 + 7f` long, 1.2 wide, first lane along the facing; `4 + 12f` Animus x Crimson Mastery, consuming Pressure for +2 a level (`SchoolCombatEvents`), Lodestone, a 0.6 push, Slowness I while standing in a flooded lane (ticks 10 to 30). Overfill: 95% or more for 20 ticks releases at full with 4 damage and 5% of max blood (`crimson_tithe_burst`). No blood returns. Player only. The old borrowed-reserve loan, its debt, its persistent keys and its allow-list line are gone. | `CrimsonTitheManip.java`, `CrimsonTitheRules.java`, `SchoolCombatEvents.java`, `DamageStages.java` |
| D-1 Crimson Tithe visuals (VX-2, part) | `CrimsonTitheGeometry`: a chain from the caster's palm (first person drops its nearest 40%) to a hook in the body: a translucent sheath, Hemorath's iron links in the solid pass (up to 24), his fluid bolus stream running home, knots at hook and palm. The vessel: blood climbing the chest with the fill, ribs pressing out when Brimming, drips that spread into fading pools, an iron spur on the forearm or shoulder per iron. The release: lanes as trenches that open, flood and close, Hemorath's iron spike clusters erupting near to far and sinking, a ray burst from the chest. Joins `OutpouringRules.woundOnly`; cast purpose AREA. | `CrimsonTitheGeometry.java`, `CanonGiftDraw.java`, `ManipulationVisualRenderer.java`, `ManipulationMaterials.java` |
| D-1 Unclosing Eye (spec 3.2, NQ-08-6/NQ-08-7) | Charged, Heart (from Head), cost 450 scaled by charge, charge 25 to 50 ticks, cooldown 500 restarted at the beam's end. Sneak speed (-70%) and no sprint while charging and while the beam is out. A 40-tick beam from the chest, range `12 + 12c`, 4 degrees plus the body's breadth, stopped by blocks, following the look at most 5 degrees a tick. Every 5 ticks: enemies take `2 + 1.5c` Lux x Crimson Mastery (first pulse DIRECT, Illuminated) and +1 growth: 3 Swelling (Slowness I), 6 Knotting (Paralysis 20, 30 on Rime, never a boss, plus Disrupted), 8 Burst (8, boss 5, x1.5 consuming Illuminated; back to 2). Searing sheds 1. Allies (players and owned bodies the caster may not harm): a hurt one heals 1 unless Necrotic; a whole one grows every other pulse, its Burst 4 and never below 1 health. Growth falls 1 per 40 ticks out of the beam. Lapse 50 ticks: no Lux casts (`BloodManipulation.passBodyGates`), 20% more damage, Illuminated. Slit 200 ticks: Illuminated, and the crosshair body within 32 is Illuminated 60 ticks, checked every 10. Player only: the entity cast, Drudge action, Will Lux-kit entry and Vesper's castable row are gone. | `UnclosingEyeManip.java`, `UnclosingEyeRules.java`, `BloodManipulation.java`, `WillCombatRules.java`, `VesperManipulationCoverage.java` |
| D-1 Unclosing Eye visuals (VX-2, part) | `UnclosingEyeGeometry`: the aperture's swollen lids parting over the charge round a bright core (not drawn in first person); the beam as a widening sheath faded near the camera, wisps, the dense white core with offset sheets and cross-sections for end-on views, a gold nerve bead per pulse, a white splash with rays and a lit furrow when it runs low; the Lapse folding to a flickering slit; sores by stage, Seraphae's bone spurs from Knotting, and the Burst's rays with a gold crackle down the limbs. Charge preview through `ManipulationVisuals.chargeForm`. | `UnclosingEyeGeometry.java` |
| Echoes and review | The Loom echoes play the new release and beam. New review row `canonGiftReviewClient` (`CanonGiftClientReview`). | `MortemEchoCasts.java`, `LuxUmbraEchoCasts.java`, `gradle/review-clients.gradle` |

### Skipped

| Row or part | Why |
| --- | --- |
| 08 D-1 build, second half | Bloom of Rot, Endless Hour, the Saints answering their gifts (NQ-08-11, built last by its default) and removing the old forms with the protocol bump are the second half of the row, not this package. Bloom of Rot and Endless Hour are Perfectus now and keep their old bodies; their entity casts and Drudge actions remain until they are rebuilt. |
| Sounds named in the spec that do not exist as wired sounds | The Tithe uses `block.chain.break`, `entity.naeglerophaeon.drain`, `entity.generic.drink`, `rite.escalate`, `entity.generic.explode`, `pointed_dripstone.place` and `rite.backlash` as listed. The Eye plays `beacon.activate` and `beacon.ambient` at the release (not at the charge's start, which is client-held), `amethyst_block.resonate` per Burst and `beacon.deactivate` at the Lapse. No new sound files. |
| Spec details left out | The heaved brick slabs at the trench lips and `HemorathImpactGeometry` / `SeraphaeImpactGeometry` per struck body (the shared `CAST_IMPACT` lands on every hit instead); the Seraphae tissue furrow shader (the furrow is a lit floor strip on the Lux material). A Ferric-secondary Rime shatter on the release is not added, since the shared rule needs a Ferric primary. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,116 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B17-unit.txt`). 6,099 after B16 plus 17 new.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1985 required tests passed :)" (`build/build2/B17-gametest-latest.log`). 1,984 after B16, minus 4 removed, plus 5 new. Holds the entity-cast and Hemorath fight suites.
- Added (unit): `CrimsonTitheRulesTest` (8: leak curve, iron, body states, draw shares, heal cap, lanes, safe wedges, timeline); `UnclosingEyeRulesTest` (6: charge and range, sweep rate the short way, stages, Searing and ally steps, ally Burst floor, beam reach); `InitiatoryDegreeSaintTrialTest` (3: round trip per Saint, nothing written or inferred when empty, kept through an endgame reset).
- Added (GameTest, `CanonGiftGameTests`, each its own batch): the registrations (Perfectus, D8, types, no entity cast, Drudge unsupported, Vesper excluded); the Tithe latch, draw, leak, release into the lanes with the wedge spared, Pressure consumed, Lodestone, no blood back, cooldown; the overfill burst (5% of max blood and damage); the drink takes blood from an active player, health from an inactive one, nothing with PvP off; the Eye's growth to Burst and back to 2, the ally heal, the Lapse with Illuminated, the cooldown and a refused Lux cast.
- Changed on purpose: `SomaticLoomWeavingGameTests.aCanonMemoryWeavesAndTeachesOnlyAtDegreeEightAfterTheTrial` (renamed; a Cortical loom; D8 without the record is now refused with no blood taken and `TRIAL_NOT_ENDED`; D8 with the record passes). `HemorathFightGameTests.fullMeterInPhaseTwoEndsTheTrialOnceWithTheReward` also checks the record is written for Hemorath only. `CanonMemoryRecipeResourceTest` pins 2 of each enzyme and 300 blood; `SupportingLoreCanonSourceTest` pins `: 2`. `ManipulationCastFixGameTests.registryTypesMatchUnifiedInputSemantics` (Quick 75 to 73, the Eye charged, the Tithe continuous) and `ManipulationChargeVisualGameTests` (17 to 18 charged) follow the new types. `EntityCastCharacterisationGameTests` and its golden lost the Eye (player only) and now assert no gift has an entity cast. `WillCombatRulesTest` drops the Eye from the Lux kit and checks no gift is in a kit. The English allow-list count for `VesperManipulationCoverage.java` went 25 to 27 (two exclusion reasons no player reads).
- Removed (stored in `removed_assets/`): `titheProvidesLiquidityAndSettlesOnlyOnce`, `unpaidTitheDebtIsCarriedAndCollected`, `crimsonTitheUsesTheBorrowedReserveAndCollectsAutomatically`, `unclosingEyeContinuouslySuppressesConcealment`: they tested the old loan, its debt and the old radius Eye, which D-1 replaces.
- `python gradle/source-text-impact-map.py`: regenerated (3,756 rows).
- Review client `runCanonGiftReviewClient`, two rounds, 24 stills each. Round one crashed on the first chain ("Not building!": the iron and bone buffers were both taken up front, and the second ended the first's batch); fixed by fetching each solid buffer where it is drawn. Round one also showed the beam vanishing end-on from behind; round two adds offset sheets and cross-section cards. Shaders and render types drew with no errors. Client closed; no server or client left running.

### Docs

- `docs/SAINT_MAUSOLEUMS.md`: the Residuum paragraph (trial record, Perfectus, 2 + 2, 300, `never_sold`), a new "The gifts" paragraph, the D-1 bullet, the C-6 ruling no longer says "the rank stays Summa", "Rulings that bind" gained D-1, NQ-08-2, NQ-08-3, NQ-08-1, NQ-08-6, NQ-08-5, NQ-08-12, NQ-08-14, NQ-08-15 and NQ-08-17 (dated 2026-11-14), the open-questions pointer and "Proof owed" lines.
- `docs/HEMOMANCY_REFERENCE.md`: §5.6h, the Saint reward table (2 + 2, 300, the Recollection), the §8 rows for both gifts rewritten and Bloom of Rot and Endless Hour marked Perfectus, §8.3 learn rule, §25.7.2 table and note.
- `docs/LORE_REFERENCE.md` §10 Canon Memories paragraph; `docs/MANIPULATIONS_AND_CASTING.md` (Lux Summa gap now visible, LP-9 and NQ-04-9 marked superseded, a D-1 ruling); `wiki/Saints-and-Encounters.md`; `wiki/School-Combat.md` (the two gifts' rows had stale schools and paths).
- The spec's refresh block; `docs/owner_answers/08-saints-and-mausoleums.md` status table gained a D-1 build line.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-08-2 and NQ-08-1/NQ-08-4, and section 2 rows NQ-08-3, NQ-08-14 and NQ-08-15. Shortened section 2 rows NQ-08-5, NQ-08-12 and NQ-08-17 to Bloom of Rot and Endless Hour, and the section 3 row "08 D-1 build" to its second half (now "ready"). D-1 and NQ-08-6 stay in section 1 (built in part). Section 4 gained "D-1 (B17)": play both gifts and look at the stills.

### For the owner

- New lang keys: `tooltip.hemomancy.blood_memory.canon_source.hemorath` / `.seraphae` / `.putriciel` / `.velorum`, `tooltip.hemomancy.blood_memory.canon_trial`, `message.hemomancy.memory_brazier_rite.trial_not_ended`, `message.hemomancy.somatic_loom_block_entity.requires_trial`, `blood_source.hemomancy.crimson_tithe_open` / `_taken` / `_burst`, `message.hemomancy.crimson_tithe.open_cost` / `.open` / `.overfill`, `message.hemomancy.unclosing_eye.open` / `.lapse` / `.lapse_no_lux`. Changed: `hemomancy.manipulation.crimson_tithe.desc`, `hemomancy.manipulation.unclosing_eye.desc`. Removed (stored): eight `message.hemomancy.crimson_tithe_manip.*`, two `message.hemomancy.unclosing_eye_manip.*`, `blood_source.hemomancy.crimson_tithe_debt`, `manipulation.hemomancy.unclosing_eye.drudge`.
- Narration lines to read: "You chain what is near and drink. Like him, you cannot hold it." / "The vessel bursts in your chest, as his did." / "Her eye opens in your chest. It does not choose what it grows." / "The eye narrows to a slit. It will not close, and you cannot hide." No Saint speaks.
- Values I chose: the chain haul 0.033 a tick (the spec gives only the Lodestone 0.05 as "1.5 times harder"); "full" for the overfill is 95% (a vessel leaking 6.6 a tick can never stay at exactly 1,000); the Eye's sneak slow is -70% movement; the slit's look check uses the existing gaze ray; the draw and the Eye's pulses ignore the hurt cooldown so every 5-tick pulse lands.
- Where the build differs from the spec's letter, by today's code: the Tithe's upkeep is the channel's 40 mL a second on the ordinary cast source, not 10 per 5 ticks named `crimson_tithe_draw`; the Eye's charge floor is the shared 50% (25 ticks), so "charge" runs 0 at 25 ticks to 1 at 50, and its cost scales with the charge like every charged cast.
- Looks for your eyes: `build/build2/B17-review-round2/` (round one in `B17-review-round1/`). Tithe: `tithe-82-*` chains, `tithe-120-*` drink, `tithe-150-*` and `tithe-168-*` Brimming, `tithe-174-*` and `tithe-186-*` the Vessel Empties. Eye: `eye-280-*` aperture, `eye-300-*` to `eye-322-*` the swept beam, `eye-340-*` and `eye-360-*` the Lapse and slit. Not seen in motion, in multiplayer or for frame cost. The chains read thin at range, and from straight behind the caster the beam hides behind their body.
- Surprising: Lux now plainly has no Summa entry in the tab (parked, D-26 and NQ-08-13). The B04 row in section 4 ("Let a Crimson Tithe come due with too little blood...") describes the old Tithe's debt, which no longer exists; it is another area's row, so I left it and say so here. Recipes now need a Cortical loom for all four gifts.

### Verification (round 1)

Checked the tree against `refs/build2/B17-before` and `build/build2/B17-status-before.txt`. Pass.

- Scope: every change belongs to the trial record, the weave, the Tithe and the Eye, or their tests and docs. No lash numbers, HUD, Silent Severance, strand kit, Bloom of Rot or Endless Hour body was touched. Bloom and Hour only changed rank and recipe (NQ-08-1 covers all four).
- Values: Perfectus, D8, 2 + 2 enzymes and 300 blood in all four recipes. The trial record sits in `InitiatoryDegree`, is written for recipient and challengers in `endTrial`, and gates both the Loom (before any blood) and the grant (`TRIAL_NOT_ENDED`). Tithe: 150 open, 40 a second, 160 hold, 400 cooldown, 5 chains in 10. Eye: Heart, charged, 450, 500 cooldown, 40-tick beam, 5 degrees a tick. No blood returns from the Tithe; the Eye grows allies. These match the spec and the answers.
- One note, not blocking: the spec says the Eye costs 450 on release. The build scales it by charge through the shared charged rule, so a floor charge costs 225. The log already says so and the answer leaves costs to play.
- Tests: the new unit and GameTest classes cover each row. The four removed GameTests tested the old loan and the old Eye and are stored in `removed_assets/`. The changed tests were rewritten to the ruling, not weakened.
- Unit suite: BUILD SUCCESSFUL, 6,116 tests, 0 failures (`build/build2/B17-verify-unit.txt`).
- Combined GameTest run: "All 1985 required tests passed :)" (`build/build2/B17-verify-gametest-latest.log`).
- `saint_kit_validation`: "All 9 required tests passed :)" (`build/build2/B17-verify-saintkit-latest.log`).
- Review client `runCanonGiftReviewClient`: world loaded past the menus, 24 stills, no render errors. Copies in `build/build2/B17-verify-review/`. Client closed.
- AGENTS.md: blood goes through `BloodAccounting.pay` with named sources; targets through `canHarm`; new forms were appended, so no protocol bump; the persistent-data line for the old debt was removed with its keys; no new vanilla particles; old code and lang keys are stored in `removed_assets/`.
- Docs, not blocking: the section 4 row in `docs/OPEN_QUESTIONS.md` points at `build/canon-gift-review/screenshots/`, while this log points at `build/build2/B17-review-round2/`; both hold the stills. `docs/SAINT_MAUSOLEUMS.md` still lists VX-2 as "Parked" in its table though two of its four looks are built.


## B18. Saint gifts, part two: Bloom of Rot, Endless Hour, the Saints' answers, the tree

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| D-1 Bloom of Rot (spec 3.3, NQ-08-8) | Quick, Body, 25 Mortem alignment, 450 through the ordinary cast price, cooldown 600 from the field's end (no cooldown runs while the field lives). The caster kneels 10 ticks (cannot walk). The field sits where it was sown, grows from radius 3 to 8 over 40 ticks, lasts 240, over floor within 2 blocks of the sown height, not in water or lava. Every 20 ticks: each harmable body on it takes 1.5 Mortem x Crimson Mastery (first pulse DIRECT, lays Necrosis) and colonisation +1, +1 still since the last pulse, +1 Rime, +1 Pressure, cap 12 from any number of fields. 4 Rooted (Slowness I), 8 Shelved (Necrosis held), 12 Overgrown (rooted 30 ticks, 5 Mortem, back to 6; players rooted through `SaintRoot`, mobs through Paralysis, a boss never). The caster on their own field gains 1 each 40 ticks (Overgrown roots 20 for 4). Kindling +1 each 10 ticks, +1 per 3 damage anything takes on the field, x2.5 for fire or Flammeus. The second press is free, never a second cooldown, works from the key and the radial (answered in `canPerformAction`, before any price): from 40 Kindling, or by itself at 100, fire runs out over 12 ticks; each body it reaches takes `2 + 0.75 x colonisation` Flammeus (x1.25 at 100), releases its banked Necrosis (new branch in `SchoolCombatEvents`), gains Searing and loses its colonisation; the caster's own burns off; the field ends 30 ticks later. Unlit at 240: +4 colonisation and 160 ticks of Necrosis on the caster. Off every field colonisation falls 1 each 40 ticks. Sounds as the spec lists. Player only. | `BloomOfRotManip.java`, `BloomOfRotRules.java`, `CanonGiftState.java`, `CanonGiftEvents.java`, `SchoolCombatEvents.java`, `ManipulationInit.java` |
| D-1 Endless Hour (spec 3.4, NQ-08-9, NQ-08-10) | Quick, Body, 600 through the ordinary cast price, upkeep 15 each 20 ticks as `endless_hour_hold` (`payUpkeep`), cooldown 900 from the Hour's end. Up to 200 ticks, radius 9, seams spreading over 20 ticks. Holds while the caster stays within 1.5 blocks of the point; stepping off ends it at once with no close; no sprint; 40% less damage while planted. Stillness 0 to 100 per harmable body: +1.2 a tick under 0.13 blocks a tick over the last 10 ticks, +0.6 swinging or using an item, +0.4 per Rime level, x1.4 in the dark rim (6 to 9 blocks); -1.5 faster; Searing -0.5 more; Disrupted counts as standing. At 100: Paralysis 60 (players 20), Rime set to 3, 6 Congeatio x Crimson Mastery DIRECT, back to 30; a boss caps at 90 and, pressed there, takes the 6 and drops to 30. The rim lays Obscured (20 ticks, refreshed each 10) on bodies not Illuminated. The close: bodies at 50 or more set, the rest take 4; the caster sets 20 ticks (Paralysis, 70% less damage; Searing ends it early). The Bow: 30% of max health taken inside one Hour breaks it, no close, 40 ticks of +50% damage and no casting (`passBodyGates`). Out of the Hour stillness drains 1.5 a tick. | `EndlessHourManip.java`, `EndlessHourRules.java`, `CanonGiftState.java`, `CanonGiftEvents.java`, `BloodManipulation.java` |
| D-1 old Hour deleted (spec 3.4 code list) | `deferDamage`, `settleDebt`, `waiveMitigation`, `transferDebt`, `isSettlement`, their calls in `ManipulationReactiveEvents` (incoming damage, tick, logout, death, clone) and `DamageStages` (the death-save and the `buffer` special case), the Sheolic Bastion exemption, `HemoDamageTypes.ENDLESS_HOUR_DEBT` and `endlessHourDebt`, the `endless_hour_debt` damage type and its four tags, the frozen `registry-ids.txt` line, both persistent-data allow-list lines (`EndlessHourManip` 17, `ManipulationReactiveEvents` 1). `ServerPlayerSpawnGraceAccessor` stays: two GameTests use it. | `DamageStages.java`, `ManipulationReactiveEvents.java`, `SheolicBastionBloodlustArmorAbilityHandler.java`, `HemoDamageTypes.java` |
| D-1 visuals (VX-2, the rest) | `BloomOfRotGeometry`: a mould sheet over the real floor cells (shared scan `GiftFloorCells`, nearest first, at most 260) with a cream rim at the growing edge, Putriciel's rosettes in one cell of six and spore sacs at the palm (solid pass, his growth material), Kindling as cream cracks and heat shimmer, kneel spores; the Ignition as a ring of his flame tongues running out, low fire behind it, a column at the caster, then char and packed ash to tick 160; colonisation as bracket rosettes climbing a body by stage. `EndlessHourGeometry`: block seams welling dark cruor with frost lips ring by ring, a thin rime glaze, liquid dark pools between 6 and 9 blocks, frost motes at a tenth speed; the close as twelve leaning cruor blades; the Bow as cracks and chips; a set body in Velorum's cruor shell panes with blades heaving round it (the caster's own drawn only low in first person); stillness as cruor and frost climbing a body. New forms appended: `BLOOM_FIELD`, `BLOOM_IGNITION`, `BLOOM_COLONY`, `HOUR_FIELD`, `HOUR_PRESERVE`, `HOUR_STILL`. | `BloomOfRotGeometry.java`, `EndlessHourGeometry.java`, `GiftFloorCells.java`, `CanonGiftDraw.java`, `ManipulationVisualRenderer.java`, `ManipulationMaterials.java` |
| NQ-08-5 (Bloom and Hour) | No entity cast and Drudge action for either (`DRUDGE_UNSUPPORTED`); Vesper's coverage marks both excluded; the Wills' Mortem kit and their close-range pick take Grave Debt in the Bloom's place. The Loom echoes play the new field, Ignition, planting and close. | `ManipulationInit.java`, `EntityCasts.java`, `DrudgeActions.java`, `VesperManipulationCoverage.java`, `VesperManipulationCaster.java`, `WillCombatRules.java`, `WillEntity.java`, `MortemEchoCasts.java`, `ThermalEchoCasts.java` |
| NQ-08-11 (each Saint answers) | Tithe on Hemorath: a chain on him feeds 40 a pulse into his meter (`HemorathEntity.feed`) and nothing into the vessel. Eye on Seraphae: in her escaping light each pulse gives 1 integrity, at most 6 a cast, and no growth. Bloom in Putriciel's fight: in a rot phase every Kindling the field gains feeds their meter one for one; in their interval no field is sown and a live one ends with no reclaim (new line). Hour in Velorum's fight: the caster keeps no Still Guard, and his stillness drift on them is x1.5 while the Hour holds. A Saint answers only a challenger of its own live fight within 48 blocks (`CanonGifts.fightOf`). | `CrimsonTitheManip.java`, `UnclosingEyeManip.java`, `BloomOfRotManip.java`, `EndlessHourManip.java`, `VelorumStillness.java`, `CanonGifts.java` |
| Old forms removed (row's last step) | `EYE`, `DEBT`, `HOUR`, `BLOOM`, `HOUR_BREAK`, `TITHE_RETURN`, `TITHE_COLLECT` left the enum and every client switch; the old debt and hour HUD line (`ManipulationVisualRenderer.hud`) and its two lang keys went with them. `PacketHandler.PROTOCOL_VERSION` 51 to 52, `payload-registry.txt` rewritten to version 52 (the payload set is unchanged). | `ManipulationVisuals.java`, `AnimusMortemGeometry.java`, `ThermalGeometry.java`, `LuxUmbraGeometry.java`, `GuiLayerRegistrar.java`, `PacketHandler.java` |
| NQ-08-14 (the four gifts on their own nodes) | No code. B17 already took the four ordinary manipulations off the gifts; the gifts were already leaves at their branches' ends. | none |

### Skipped

| Row or part | Why |
| --- | --- |
| Bespoke cast styles and tracks for the two gifts (spec 2.8) | The existing `BLOOM_OF_ROT` and `ENDLESS_HOUR` styles and tracks stay. The brief and the row name no new animation, and a new track is the casting-animation owner's look. |
| `PutricielImpactGeometry` and the per-gift impact layers | The shared `CAST_IMPACT` lands on each ignited and each set body instead, as B17 did for the Tithe and the Eye. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,124 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B18-unit.txt`). 6,116 after B17, plus 9 new, minus 1 removed.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1986 required tests passed :)" (`build/build2/B18-gametest-latest.log`). 1,985 after B17, plus 8 new, minus 7 removed. The four Saint fight suites and the entity cast and combat order suites run inside it.
- Added (unit): `BloomOfRotRulesTest` (5: growth, gain, stages, Kindling, Ignition); `EndlessHourRulesTest` (4: reach and dark, fill and drain, preservation and the boss cap, guard, set and Bow).
- Added (GameTest): `CanonGiftGameTests.bloomOfRotColonisesThenBurnsUnderTheSecondPress` (450 paid, no cooldown while live, still body colonised with Necrosis, caster colonised, owned wolf spared, press below 40 refused free, Ignition free, colonisation burned off with Searing, cooldown from the end), `fireFeedsTheKindlingAndAnUnlitFieldReclaimsItsCaster`, `theEndlessHourSetsTheStillAndThenItsCaster` (600 and upkeep, rim Obscured, 40% guard, a still body set with Rime 3 and Paralysis, close, self-set at 70% less, cooldown), `steppingOffEndsTheHourAndAHeavyWoundBowsTheCaster`; one per Saint: `HemorathFightGameTests.hisOwnTitheFeedsHisMeterAndNeverTheVessel`, `SeraphaeFightGameTests.herOwnEyeHoldsHerAndGrowsNothing`, `PutricielFightGameTests.theirOwnBloomFeedsTheirKindlingAndWillNotTakeInTheInterval`, `VelorumFightGameTests.hisOwnHourStillsItsCasterFasterAndTakesTheGuard`. The registration test now covers all four gifts.
- Changed on purpose (rulings flip them): `ManipulationBalancePass2SchoolBGameTests` Bloom damage tests read the first rot pulse (1.5) after 20 ticks, Crimson Mastery still applied once. `ManipulationTargetingGameTests` drops the Bloom and its self-poison line (the ally check moved to the Bloom GameTest). `CombatStageCharacterisationGameTests` and `combat/death-saves.txt` drop the Endless Hour rows and the `hour=` column: the Hour is no longer a death save. `EntityCastCharacterisationGameTests`, its golden and `WillCombatRulesTest` drop the Bloom (all four gifts asserted castless). `DamageStageGuardrailTest` no longer expects `deferDamage` in the death-save stage. `HudAndWorldDrawingGuardrailTest` drops the removed HUD call. `CongeatioStructureTest`, `ManipulationSignatureEffectsTest`, `ThermalCuePacketTest` (now `HOUR_PRESERVE`) and the two visual review lists drop the removed forms. The English allow-list count for `VesperManipulationCoverage.java` went 27 to 29 (two exclusion reasons no player reads).
- Removed (stored in `removed_assets/`): seven GameTests (and one helper) of the old Hour's deferral, settlement, totem, Bastion, cooldown, death and clone transfer; `LuxUmbraGeometryTest.theEyeHasTwoCardsAndPreservesItsUvMaskWhenViewedSideways` (the `EYE` form is gone).
- `python gradle/source-text-impact-map.py`: regenerated (3,756 rows).
- Review client `runCanonGiftReviewClient` (extended with Bloom and Hour beats): world loaded past the menus, 52 stills, no render or shader errors. The first create op failed for a missing seed; the second, with a seed, ran. Client closed; no server or client left running.

### Docs

- `docs/SAINT_MAUSOLEUMS.md`: "The gifts" paragraph covers all four and the Saints' answers; "Rulings that bind": D-1 built, NQ-08-6/7/8/10, NQ-08-5, NQ-08-9, NQ-08-11, NQ-08-12, NQ-08-17 and VX-2 recorded or completed (dated 2026-11-14 and 2026-11-13, the days of the answers); open-questions pointer no longer lists D-1 and NQ-08-6; "Proof owed" says the four gifts are unplayed; a B18 block in "Calls made without the user"; a history row.
- `docs/HEMOMANCY_REFERENCE.md`: §8 rows for Bloom of Rot and Endless Hour rewritten, the Tithe and Eye rows gained their Saint's answer; the Vesper coverage rows for all four gifts read excluded; the Drudge rows read unsupported; the damage stages row and the Mortem presentation line.
- `docs/LORE_REFERENCE.md` §10 Canon Memories: the Bloom, the Hour and the Saints' answers. `docs/MANIPULATIONS_AND_CASTING.md`: CB-10 and P03-hour-death marked superseded. `wiki/Saints-and-Encounters.md`, `wiki/School-Combat.md` (secondaries corrected to FLAMMEUS and TENEBRIS, no entity cast, the death-save line), `wiki/Animus-Mortem-Visuals.md`.
- The spec's refresh block; `docs/owner_answers/08-saints-and-mausoleums.md` status line for D-1 build.
- `docs/OPEN_QUESTIONS.md` deleted: section 1 rows D-1 and NQ-08-6/7/8/10; section 2 rows NQ-08-5, NQ-08-9, NQ-08-11, NQ-08-12, NQ-08-17; section 3 row "08 D-1 build, second half"; section 5 row VX-2. Section 4: the "D-1" row became "D-1 (B18)" (play the two gifts and the four answers, read the lines, look at the stills); the B17 row now points at `build/build2/B17-review-round2/` (the old stills moved to `build/canon-gift-review/screenshots-B17/`).

### For the owner

- New lang keys (narration, no Saint speaks; Putriciel is "they"):
  - `message.hemomancy.bloom_of_rot.sown`: "You kneel and give the floor your rot. It takes everything that stands on it, you too."
  - `message.hemomancy.bloom_of_rot.not_kindled`: "The rot will not take the fire until it is kindled to %s."
  - `message.hemomancy.bloom_of_rot.ignition`: "You light it, as they lit theirs. The rot burns out of everything it held."
  - `message.hemomancy.bloom_of_rot.reclaimed`: "You never lit it. The rot takes you back."
  - `message.hemomancy.bloom_of_rot.theirs`: "The fire is theirs now. Your rot will not take."
  - `message.hemomancy.endless_hour.planted`: "You plant your feet and become the still thing. Whatever stops near you will stay."
  - `message.hemomancy.endless_hour.stepped`: "You stepped off the point. The Hour does not follow."
  - `message.hemomancy.endless_hour.set`: "The Hour closes, and you set in your own cruor, as he did."
  - `message.hemomancy.endless_hour.bow`: "The wound bows you. He bowed beneath his, and the Hour broke there too."
  - `message.hemomancy.endless_hour.bowing`: "You are bowed. Nothing answers you until you straighten."
  - `blood_source.hemomancy.endless_hour_hold`: "Endless Hour Upkeep"
- Changed: `hemomancy.manipulation.bloom_of_rot.desc`, `hemomancy.manipulation.endless_hour.desc`. Removed (stored): `message.hemomancy.bloom_of_rot_manip.putriciel_s_bloom_erupts_decay_and`, `manipulation.hemomancy.bloom_of_rot.drudge`, `manipulation.hemomancy.endless_hour.drudge`, the three `message.hemomancy.endless_hour_manip.*`, `death.attack.endless_hour_debt` and `.player`, `gui.hemomancy.manipulation_visual_renderer.tithe` and `.endless_hour`.
- Values I chose: both prices go through the ordinary cast price (as the Eye's does), so Temper and the cost ledger touch them; an Hour whose upkeep cannot be paid ends with no close; colonisation off a field falls 1 each 40 ticks and stillness out of an Hour 1.5 a tick; the rim's Obscured lasts 20 ticks, refreshed each 10; Overgrown roots players with `SaintRoot` and mobs with Paralysis; the rot and the Hour's cuts are magic-typed school hits; a Saint answers within 48 blocks; the Will Mortem kit takes Grave Debt.
- Looks for your eyes, `build/build2/B18-review/`: `bloom-446-*` kneel and spores, `bloom-490-*` the field spreading, `bloom-545-*` colonised bodies and cracks, `bloom-564-*` and `bloom-572-*` the Ignition ring, `bloom-600-*` char, `bloom-650-*` ash; `hour-712-*` seams spreading, `hour-740-*` to `hour-800-*` the dark rim and bodies set in cruor, `hour-860-*` the held Hour, `hour-903-*` the close's blades and the caster's shell, `hour-914-*` the shell cracking. Not seen in motion, in multiplayer or for frame cost. The dark rim reads faintly on the review's red floor.
- Surprising: the old Endless Hour was also a death save; removing it changed the death-save golden (its five rows are gone, the others did not move). Velorum's own stillness answer lives in his rule (`VelorumStillness.drift`), which now reads the gift.

### Verification (round 1)

Checked the tree against `refs/build2/B18-before` and `build/build2/B18-status-before.txt`. Pass.

- Scope: every change belongs to Bloom of Rot, the Endless Hour, the Saints' answers, the removed forms, or their tests and docs. No lash number, living HUD design, Silent Severance, strand kit or other held row was touched. The only HUD change is the dead debt line the row removes.
- Values: `BloomOfRotRules` and `EndlessHourRules` match spec 3.3 and 3.4 (450 and 600, upkeep 15 each 20 as `endless_hour_hold` through `payUpkeep`, cooldowns 600 and 900, radius 3 to 8 over 40, 240 ticks, stages 4/8/12, Ignition `2 + 0.75 x colonisation` x1.25 at 100, radius 9, rim 6 to 9, guard 40%, preservation at 100 with boss cap 90, close at 50, self-set 20 ticks at 70% less, Bow at 30%). Players are capped at 20 ticks of Paralysis inside `Paralysis.apply`. Saint answers: Tithe 40 to Hemorath's meter, Eye 1 integrity a pulse capped at 6 in phase III, Bloom Kindling one for one and refused in the interval, Hour guard lost and drift x1.5. These match D-1, NQ-08-6/7/8/10 and the spec's table (NQ-08-11).
- Tests: `BloomOfRotRulesTest`, `EndlessHourRulesTest`, four new `CanonGiftGameTests` and one GameTest per Saint would each fail without their row. The removed old-Hour tests are stored in `removed_assets/`; the changed tests and goldens follow the ruling that the Hour is no longer a death save.
- Unit suite: BUILD SUCCESSFUL, 6,124 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B18-verify-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B18-verify-gametest-latest.log`).
- `saint_kit_validation`: "All 9 required tests passed :)" (`build/build2/B18-verify-saintkit-latest.log`).
- Review client `runCanonGiftReviewClient`: world opened past the menus, "CANON_GIFT_REVIEW captured 52", no shader or render errors. Bloom and Hour stills are in `build/build2/B18-verify-review/` (for example `bloom-572-behind.png`, the Ignition ring, and `hour-903-behind.png`, the close's blades and shells). The implementer's stills moved to `build/canon-gift-review/screenshots-B18-impl/`. Client closed; no Java process left running.
- AGENTS.md: blood goes through `BloodAccounting` with named sources; enum ordinals changed, so `PROTOCOL_VERSION` went to 52 with `payload-registry.txt` rewritten; new player text is in `en_us.json`; no new vanilla particles (ratchet green); persistent-data lines removed with the old keys; old code, data, tags, lang and tests are stored in `removed_assets/` with `MANIFEST.txt` lines.
- Docs: `docs/OPEN_QUESTIONS.md` lost only this package's rows (D-1, NQ-08-6/7/8/10, NQ-08-5/9/11/12/17, the section 3 row, VX-2); rulings sit in `docs/SAINT_MAUSOLEUMS.md`.
- One note, not blocking: the Tithe's answer fires on any `HemorathEntity` it chains, not through `CanonGifts.fightOf`, so the 48-block challenger rule the log states only governs the Eye, Bloom and Hour. A Hemorath outside a fight is rare, so this changes nothing in play today.

## B19. Visual engineering: animation time, shader shapes and seeds, review scenes, the A-row review pack

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 09 T9 (A-4, NQ-09-3, A-4 split) | New `AnimationTime.itemMillis()` (game time in milliseconds at this frame) and `ticksOrWallClock(level, partial)` (wall clock only when no level is loaded). The 13 item and armour renderers read it, so they freeze under `/tick freeze` in a hand and in a slot. The Morphling Cradle and the Unstained gauge moved their no-level fallback into the helper. `AnimationTimeGuardrailTest` lists 17 wall-clock users, down from 32, each group with its reason (3 veil, 2 cache stamps, 1 bar ease, 11 screens); the "pending A-4" wording is gone. The Specimen Jar item passed `getFrameTimeNs()` (nanoseconds) as its partial tick; it now passes the partial tick. | `AnimationTime.java`, `SilentArchonArmorRenderHelper.java`, the Memory of Vesper, Monolith Fragment, Imbued Cloth, Cornerstone, Qliphoth Pome and Seed and Scar Pattern item renderers, `MorphicNectarItemDecorator.java`, the Covenant Throne, Hematic Stake, Sanguine Monolith and Specimen Jar item renderers, `MorphlingCradleRenderer.java`, `UnstainedGaugeOverlay.java` |
| 09 T10 (VX-4) | New shader shapes: Animus 12 `ANIMUS_FOCUS` (a hot core in a soft halo) and 13 `ANIMUS_SLASH` (a thin cut tapering to both ends); Umbra 14 `UMBRA_WISP` (a round puff of shadow) and 15 `UMBRA_FOCUS` (a dark core in a violet-lit rim). 4 and 7 untouched. Shared-tier callers moved: the Animus mortar sheath, cloud heart, impact and rupture flash, Sovereign Lash flares and conjure focus (Mortem forms on the same lines keep `FOCUS`); the Umbra Well wisps, Well charge, Veil wisps, the Gloam slash landing puff and the Umbra mist. Drips, clots and buds keep the droplet. Blood Eclipse's field is untouched (a rebuilt held form). | `LuxUmbraGeometry.java`, `AnimusMortemGeometry.java`, `manipulation_animus.fsh`, `manipulation_umbra.fsh` |
| 09 T10 (VX-5, NQ-09-1) | New `CueSeed.of(packet, salt)`: the source seed XOR a mix of the game tick this client first saw the cue (`ManipulationLifecycle.born`). Used by `AnimusMortemGeometry`, `LuxUmbraGeometry`, `ThermalGeometry`, `FerricDuctilisGeometry`, the legacy draw path and the surface patches. A salt of 0 keeps the old seed, so the old draw signatures stay for tests and the encoding digest did not move. No payload change. Tested by `CueSeedTest` (added in the fix). | `CueSeed.java`, `CueSeedTest.java`, `ManipulationVisualRenderer.java` and the four geometry classes |
| 09 T1, T4, T5 (small tests) | `ParticleSpawnSiteGuardrailTest`: the Incubator renderer names no particle call and its block entity's client tick does (FX-8); each of the 16 Unstained rite completions calls its own `UnstainedRiteParticles` method, every method there has its rite, and the rite file sends no particles itself. `ItemSpriteSizeTest`: the three FX-6 sprites are 16 by 16. | the two new tests, `ARCHITECTURE_STANDARDS.md` index row |
| 09 T3 to T6 (FX-29, review scenes) | `summonParticleReviewClient` adds an unowned Iron Spike and Pillar (eruption, seep, flakes, as Vesper's and the Weave's), a Directed Blood Orb, a Hemolytic Vial burst and a dropped Morphling Polyp. `ecologyParticleReviewClient` adds a Blood Lantern Jelly and a Prism Cuttle in glass water tanks and a Chalybeate Snail harvested with a knapper. New `sweepParticleReviewClient` for batches B and E: Chamber of Will theme change, Bloodburst, Masquerade, Distorted Presence, Crimson Retribution, Neural Overload, salvage scrap, Nectar mutation. Every review run now writes `needsProbe = 2` to its The One Probe config, so the look-at tooltip (called the "Jade tooltip" in the row) is gone from shots. The station scene's clear box now reaches z -9, so its first camera stands inside it. The stale scene comment above `strandReviewClient` is rewritten. | `SummonParticleClientReview.java`, `EcologyParticleClientReview.java`, new `SweepParticleClientReview.java`, `HarbingerStationClientReview.java`, `gradle/review-clients.gradle` |
| 09 T6 (audit shapes) | New `HemoParticleBursts` (`outward`, `upward`; each particle sent with count 0 so the offset is its direction). Echo burst dark glows: 30 outward, speed 0.08, vertical 0.6. Bloodburst cells: 40 outward, 0.08, vertical 0.5. Nectar mutation serpents: 16 outward at the old speeds (0.05, 0.06), vertical 0.7. Living Sickle cells: 20 up the trunk over 2.5 blocks, 0.35 out, speed 0.03. Blood Thrall splatter lift 0.15 to 0.04; Drudge burst lift 0.1 to 0.03. | `HemoParticleBursts.java`, `PhantasmalEchoEntity.java`, `EdaciousBloodburstArmorAbilityHandler.java`, `MorphicNectarEvents.java`, `LivingSicklePruning.java`, `BloodThrallEntity.java`, `DrudgeEntity.java` |
| 09 T7 (VX-17, PR-7, NQ-09-6), in part | Captures: the three lashes and eight held forms, first and third person, 191 stills (`runStrandReviewClient`, new world `StrandReviewB19`); the same from a second player, 32 stills (`review-onlooker.ps1`); every visual form on a stand, 798 stills (`-PvisceralRoster`). Dated addendum with the re-grade of the 18 stale rows. | `docs/audits/2026-11-11-visual-balance/2026-11-15-a-row-review-and-regrade.md` |
| VX-1 conflict row | Recorded: the Mortem strand brand, the Blood Eclipse spray and the Crimson Flame plant are dropped; the R cast upgrades of batches 5, 9 and 10 stay. No code. | `MANIPULATIONS_AND_CASTING.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| 09 T7: the 43 A rows cast one by one | The casting harness runs a server and two clients as three Gradle runs at once; this build allows one Gradle command at a time. The roster shows each form's look but not its cast. Left as a short section 3 row. |
| 09 T3 to T6: scenes for the Circus perception overlay, the Curved Horn emitter, the Thurible coal, the Elytra effect, the Silent Archon negation and the Living Sickle cut | Each needs a state the scene cannot cheaply build: a pavilion and perception state, a worn horn seen from outside, a held burning thurible, an Elytra flight, a full worn set and a damage event, a Qliphoth Bloom. Named in the PR-26 row. |
| `ANIMUS_SLASH` callers | No shared-tier Animus code draws a glowing cut; the slash users are bespoke, Saint, lash or cast-impact classes (cast impacts kept as built, RV-23). The shape is ready for the visual batches. |
| An Umbra ripple | No Umbra caller draws a ring, so only the wisp and the focus were added. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,127 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B19-unit2.txt`). 6,124 after B18 plus 3 new.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1986 required tests passed :)" (`build/build2/B19-gametest2-latest.log`). Same count as B18.
- Added: `ParticleSpawnSiteGuardrailTest` (FX-8 and the Unstained rite calls); `ItemSpriteSizeTest` (FX-6 sizes); `CueSeedTest` (VX-5, added in the fix).
- Changed on purpose: `AnimationTimeGuardrailTest` drops the 15 moved classes and the pending A-4 wording, per "A-4 (split)". Not weakened: a new wall-clock user still fails it.
- `MaterialQuadsEncodingTest` digest unchanged: the test path draws with salt 0, which keeps the old seed.
- `python gradle/source-text-impact-map.py`: regenerated (3,758 rows).
- Review clients, each past the Accessibility screen, no shader or render error in any log (the VX-4 shader proof, on NVIDIA; the three shader warnings in every log are older and name other shaders), all stopped themselves:
  - `runSweepParticleReviewClient` (world `SweepReview`): `SWEEP_PARTICLE_REVIEW captured 7`, every action fired.
  - `runSummonParticleReviewClient` (`SummonReviewB19`): `captured 9`. Old shots moved to `screenshots-before-B19/` (likewise for ecology, station and strand).
  - `runEcologyParticleReviewClient` (`EcologyReviewB19b`, after adding the tanks): `captured 8`, `eye eaten: true`.
  - `runHarbingerStationReviewClient` (`StationReviewB19`): `captured 5`; no tooltip, no birch in shot 0.
  - `runStrandReviewClient` (`StrandReviewB19`): `STRAND_REVIEW captured 191`.
  - `review-onlooker.ps1`: 32 observer stills.
  - `runClient -I tools/review/manipulation-visceral-review.init.gradle -PvisceralRoster`: 798 roster stills.
  - No game client, server or Gradle task is left running.

### Docs

- `AGENTS.md`: the animation time line now states the A-4 rule.
- `docs/MANIPULATIONS_AND_CASTING.md`: Rulings that bind record VX-4 and VX-5 built, NQ-09-1, NQ-09-3, A-4 built and "A-4 (split)", the VX-1 batch members dropped, NQ-09-6, VX-17 order, FX-29 built in full and the audit shapes; the quoted `AGENTS.md` line; Proof owed for VX-17 and PR-7; the open-questions pointer no longer names VX-1.
- `docs/MANIPULATION_VISUALS.md`: the new shape ids under Structure, the per-cast seed under Animation, the intro's open questions.
- `docs/architecture/ARCHITECTURE_STANDARDS.md` (A-4 standard, two index rows), `docs/architecture/CONSOLIDATION_PROGRESS.md` (A-4 DONE), `docs/TESTING.md` (the scenes, the new sweep client, the tooltip), the new addendum.
- `docs/owner_answers/09-visuals-particles-and-texels.md` status: T1, T3, T4, T5, T6, T7, T9, T10.
- `docs/OPEN_QUESTIONS.md` deleted: section 1.1 VX-1 (three batch members); section 2 NQ-09-1, NQ-09-3, A-4 (split), NQ-09-6, VX-17 (order); section 3 "09 T3 to T6", "09 T1, T4, T5", "09 T9", "09 T10", "09 T6 (audit shapes)". Shortened: "09 T7" to the casting-harness capture; "09 T11 batches 5, 9, 10" now says the cut members are dropped. Section 4: VX-17/PR-7 points at the pack; PR-26 names the new shots and what is still unstaged; new row "A-4, VX-4, VX-5 (B19)".
- `HEMOMANCY_REFERENCE.md` and `LORE_REFERENCE.md`: no mechanic changed.

### For the owner

- No lang key added or changed. No payload change, no blood paid.
- Values I chose: the burst speeds and vertical factors above; Thrall lift 0.04 and Drudge lift 0.03; the salt mix (golden-ratio multiply, top 31 bits); shape ids 12 to 15 (kept clear of Mortem's 8 and 10).
- Look at: `build/build2/B19-review/strand-held-forms-sheet.png` and `onlooker-sheet.png` (the held forms and lashes), `vx4-shapes-sheet.png` (the new shapes; the roster camera is far off, so it only shows they draw), `build/sweep-particle-review/screenshots/`, `build/summon-particle-review/screenshots/summon-particle-5..9-*`, `build/ecology-particle-review/screenshots/ecology-particle-6..8-*`, `build/harbinger-station-review/screenshots/harbinger-station-0-none.png`.
- Surprising: Blood Eclipse's field of dark did not show at all to a second player outside it. The needle volley barely reads from behind its caster but reads well from the front. The strand scene's shots carry the vanilla movement tutorial and advancement toasts. The tooltip in old shots was The One Probe, not Jade.
- Animated items in inventory slots now stop under `/tick freeze` and in the single-player pause menu (NQ-09-3).

### Verification (round 1)

Checked the tree against `refs/build2/B19-before` and `build/build2/B19-status-before.txt`. Result: not passed, one blocking problem.

- Scope: inside the brief. No held row, no Silent Severance rename, no lash feel number and no HUD file was touched. The One Probe hiding in `gradle/review-clients.gradle` is the "hide the tooltip" part of the review-scene row.
- Values: A-4 split matches (13 renderers on `AnimationTime.itemMillis()`, Cradle and gauge fallbacks in `ticksOrWallClock`, guardrail list at 17 with reasons). NQ-09-1 matches: the salt is `cue.life.born`, the tick the client first saw the cue, and no payload changed. VX-4 ids 12 to 15 are new branches; 4 and 7 are unchanged in both shaders. VX-1 conflict row recorded in `docs/MANIPULATIONS_AND_CASTING.md`.
- Unit suite: BUILD SUCCESSFUL, 6,127 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B19-verify1-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B19-verify1-gametest.txt`, log at 11:25:56).
- Shader proof: `runSweepParticleReviewClient` opened `SweepReview`, got past the Accessibility screen and logged `SWEEP_PARTICLE_REVIEW captured 7`. No error for `manipulation_animus` or `manipulation_umbra`; only the three old uniform warnings. Shots copied to `build/build2/B19-verify1-shots/`. The client exited by itself; no java process left.
- Blocking: VX-5 has no test. `CueSeed.of` is pure and can be tested in the JVM: salt 0 keeps the old seed (entity id, or `from().hashCode()` with no entity), and two different salts give two different seeds for one packet. Add that test under `src/test/java/.../client/render/world/`.
- Not blocking: the 09 T6 burst shapes (`HemoParticleBursts`) and the VX-4 shapes have no JVM test; the plan named none and the review captures are the evidence. `HemoParticleBursts` sends one packet per particle (40 for the Bloodburst); fine for these one-off bursts, worth knowing before wider use. The PR-26 row in section 4 still lists "the Thrall and Drudge bursts fly high" as known while saying they were lowered; one known-gap line in `docs/MANIPULATIONS_AND_CASTING.md` still says VX-4 "will need a launch", and the A-4 known-gap sentence reads oddly.

### Fix

- Blocking problem fixed: new `src/test/java/com/vincenthuto/hemomancy/client/render/world/CueSeedTest.java`. It checks that salt 0 keeps the old seed (the entity id, or `from().hashCode()` with no entity), and that salts 1 to 64 give 64 different seeds for one packet, with and without an entity. No main code changed.
- Doc lines fixed: the PR-26 row in `docs/OPEN_QUESTIONS.md` section 4 no longer lists the Thrall and Drudge bursts as flying high. In `docs/MANIPULATIONS_AND_CASTING.md` Known gaps, the VX-4 line now says shapes 12 to 15 were proven in a client launch on 2026-11-15, and the A-4 sentence now reads "The A-4 line in `AGENTS.md` was brought up to date on 2026-11-15."
- Left as is (not blocking): `HemoParticleBursts` has no JVM test and sends one packet per particle; the review captures stay the evidence.
- Unit suite: `./gradlew.bat test --offline --console=plain`, BUILD SUCCESSFUL, 6,129 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B19-fix-unit.txt`). 6,127 plus the 2 new.
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B19-fix-gametest.txt`).
- No client launch: the fix changes no shader, screen or look. The shader proof from the build and verification round 1 stands (`build/build2/B19-verify1-shots/`).
- `source-text-impact-map.csv` not regenerated: the new test reads no repository file.
- No game client, server or Gradle process is left running.


### Verification (round 2)

Checked the tree against `refs/build2/B19-before` and `build/build2/B19-status-before.txt`. Result: passed, no blocking problem.

- Scope: inside the brief. No held row, no Silent Severance rename, no lash feel number, no HUD design file. No payload, lang key or blood path changed. New particles are `HemoParticleData` types; the vanilla allow-list is untouched.
- Values: A-4 split matches (13 renderers on `AnimationTime.itemMillis()`, Cradle and gauge fallbacks in `ticksOrWallClock`, 17 listed users with reasons). NQ-09-1 matches: the salt is `cue.life.born`, client side. VX-4 ids 12 to 15 are new shader branches; 4 and 7 are unchanged. VX-1 conflict row recorded.
- Tests: the round 1 blocker is fixed. `CueSeedTest` pins salt 0 to the old seed and different salts to different seeds. `AnimationTimeGuardrailTest` is rewritten to the A-4 ruling, not weakened.
- Unit suite: BUILD SUCCESSFUL, 6,129 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B19-verify2-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B19-verify2-gametest.txt`, log at 11:40:34).
- Shader proof: `runSweepParticleReviewClient` opened `SweepReview`, got past the Accessibility screen and logged `SWEEP_PARTICLE_REVIEW captured 7`. No error for `manipulation_animus` or `manipulation_umbra`; only the three old warnings for other shaders. Shots in `build/build2/B19-verify2-shots/`. The client exited by itself; no java process left.
- Not blocking, as in round 1: `HemoParticleBursts` (09 T6) has no JVM test; the plan named none.

## B20. Texel batches T1 and T3

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 09 T12 (VX-10), measuring script | New `tools/model_export/texel_density.py`: measures every JSON model the audit's way (per face, the square root of the UV span in texels over the face size in blocks, area-weighted; parents resolved through the vanilla resources jar; animated frames read from `.mcmeta`). Flags a model over 1.25 times its ideal or under 12. It reproduces the audit's figures (Cabinet specimens 1,047.5, Rune Mod Station 43.5, Puffball 4.0). | `tools/model_export/texel_density.py` |
| 09 T12 (VX-10), UV rescale | New `tools/model_export/texel_uv_rescale.py` over the batch T1 models (84 files). A face on a 16 px block texture gets a UV span equal to its size in units (capped at 16), with the position-derived offset where it showed the whole texture, else the start of its chosen part. An axis changes only when it is off by more than a quarter. `blood_fill_tiled` faces (256 px) get face size over 16. A decal (`leech_mouth`, `vat_port`, `podium_top`, `crossbow_firework`, `jar_bottom_side`, `rite_hint`) stays whole on a face 4 units or more each way and shows its centre on a smaller face. The Alembic growths' `model_ghastly_alembic` faces show the centre of their region at face size. The Resonant Forge's wax cylinder stays inside the cylinder atlas's body tile and end stamp. Planted staffs stay their held model minus the fist. The script is idempotent. Results: Cabinet and Field Case specimens 1,047.5 to 16.0, Baghnakh 212 to 16, Barbed Blade 189 to 16, Crossbow (all six) 145 to 154 to 16, Leech staff 150 to 15.9, podiums 31 to 39 to 16.3 to 16.9, Blood Crystal 7.6 to 16, Puffball 4.0 to 16, Microscope 10.8 to 15.9, Vacant Effigy 29 to 16, Scyphus 32 to 16. | the 84 models listed by `texel_uv_rescale.py` |
| 09 T12 (VX-10), post-audit models | Measured every model changed since 2026-11-11 (80 models; `build/build2/B20-density-post-audit-models.txt`). Sanguine Vigil 16.4, ground-network strain stages 16.9, the Titration Bandolier is a flat sprite. Only the 3-wide Specimen Vat reads low (9.3 to 11.7): its faces run almost 3 blocks long on one 16 px texture. Not changed: the fix is in `specimen_vat.py`, outside these rows. | none |
| 09 T14 (VX-12), staff fittings | The plan named `staff_fitting_sprites.py`, which paints the 16 px inventory icons. The staff fitting models and their 32 px sheets come from the local, untracked `tools/staff_fittings/gen_staff_fittings.py`. It now paints 16x16 sheets (4 by 4 px cells, the 8 px cell patterns mapped edge to edge) with the same UV layout and geometry. Fitting staffs 23 to 28 to 15.1 to 16.1. Old sheets kept. | `textures/item/living_staff_<fitting>.png` (7), the 14 fitting models |
| 09 T14 (VX-12), generators | Face-sized UVs at one texel per unit: Living Syringe and vial rack (each face a face-sized window of its 4 px patch; 28 to 42 to 14.9 to 16.0), the three cylinders (side strips one texel per unit, end stamp at scale 1; 25 to 27 to 16.0), the Centrifuge flywheel (cap frame face-sized at the texture centre; 27.3 to 16.0), the Terrestrial Speculum (half a UV unit per unit on its 8 px atlas, up to the quadrant; 24.7 to 14.1). Each generator was first run unchanged and reproduced the repo exactly. | `living_syringe.py`, `cylinders.py`, `vial_centrifuge.py`, `terrestrial_speculum.py`, their models and `.bbmodel` files |
| 09 T14 (VX-12), set-piece items | Pulse Pylon item: the whole pylon at scale 1, y -16 to 32 (vanilla's element bounds exactly), centre kept; display scales halved (gui 0.275, ground 0.125, fixed 0.275, head 0.3, hands 0.15). 34.1 to 17.1. Saint Sarcophagus item: scale 0.75, offset (0, -3.875, -5.9), display scales times 2/3. Its 63-pixel length cannot fit the bounds at full scale, so it reads 22.0 (was 33). Both generators assert the bounds. | `pulse_pylon.py`, `saint_sarcophagus.py`, `item/pulse_pylon.json`, `item/saint_sarcophagus.json` |
| Review client | New `texelReviewClient` (`TexelClientReview`): 63 shots of the changed models on display entities, each row against a white wall; the staff row with five Morphling heads and all seven fittings. | `TexelClientReview.java`, `gradle/review-clients.gradle` |

### Skipped

| Row or part | Why |
| --- | --- |
| 09 T14: Marionette Crossbar | Its mesh is already one texel per unit; it reads 26 to 32 because `MarionetteCrossbarItemRenderer` draws it at 0.5 to 0.62. Fixing it means baking that scale into the Java mesh, and the puppet thread knots, `CrossbarMotionRules` and Vesper's crowned weapon layer (scale 1.7) read the same mesh. That is batch T4 work and grows well past this row. Left as a section 3 row beside 09 T15. |
| 09 T14: Hydra Crown | No change needed. It reads 17.1 against an ideal of 16: its animated frame is 128 by 32 (`.mcmeta` width and height). The audit read the frame as 128 by 128. |
| 09 T12: Rite Fragment Inscription | Its only faces show the `rite_hint` picture whole on a 12-unit face. Cropping would cut the picture. It stays at 21.3. |
| 09 T12: Cabinet block specimens in the review | The Cabinet's block entity draws them, so the review shows the fill through the Field Case item, which uses the same fill. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,129 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B20-unit.txt`, and again after the review class, `B20-unit2.txt`). Same as after B19.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1986 required tests passed :)" (`build/build2/B20-gametest-latest.log`, and again after the review class, `B20-gametest2-latest.log`). Same as after B19.
- No test added, changed or flipped. The model tests that read these files stayed green: `CardinalRitePlantedStaffModelTest` (planted equals held minus the fist), `LivingSyringeModelTest`, `TerrestrialSpeculumModelTest`, `CabinetSpecimenLayoutResourceTest`, `RepositoryTextEncodingTest` (the new scripts are ASCII).
- `python tools/model_export/texel_density.py`: 130 flagged models before (`build/build2/B20-density-before.txt`), 20 after (`B20-density-after.txt`). The 20: Sanguine Omen (T2), the orphan `living_syringe_rack` export, the Humoral Barometer (density 2 on purpose) and one needle, the Sarcophagus items, the Rite Fragment Inscription, the Dendritic bouton (1.25, at the line) and the 3-wide Specimen Vat.
- `texel_uv_rescale.py --check` after the run and the fitting regeneration: no face changes (idempotent).
- `runTexelReviewClient`, three launches (the first two had a red floor and blocked wide shots; judge the third): past the Accessibility screen, `TEXEL_REVIEW captured 63`, no error in the log, the client stopped itself. No java process is left.
- `source-text-impact-map.csv` not regenerated: no test reads a new file.

### Docs

- `docs/MANIPULATIONS_AND_CASTING.md`: the VX-10 to VX-13 line says VX-10 and VX-12 are built; new ruling lines for VX-10 and VX-12 with what was built.
- `docs/TESTING.md`: the new `runTexelReviewClient` and the measuring script.
- `docs/OPEN_QUESTIONS.md`: section 3 row "09 T12 (VX-10)" deleted; "09 T14 (VX-12)" shortened to the Marionette Crossbar. Section 4: new row "VX-10, VX-12 (B20)" with the sheets.
- `docs/owner_answers/09-visuals-particles-and-texels.md` status: T12 built, T14 built in part.
- `removed_assets/2026-11-15-texel-t3-old-textures/`: the seven 32 px fitting sheets and the old fitting generator; `MANIFEST.txt` lines added.
- `HEMOMANCY_REFERENCE.md` and `LORE_REFERENCE.md`: no mechanic changed.

### For the owner

- No lang key added or changed. No payload, no blood path, no game Java changed (one new review class).
- Looks to judge: `build/build2/B20-review/texel-sheet-1-items.png`, `texel-sheet-2-staffs.png`, `texel-sheet-3-blocks.png`; full size in `build/texel-review/screenshots/texel-<nn>-<name>.png`. The earlier, worse-framed passes are in `screenshots-first-pass/` and `screenshots-second-pass/`.
- Values I chose: decal threshold 4 units; a quarter tolerance before an axis changes; Sarcophagus item scale 0.75 and offset (0, -3.875, -5.9); Pylon item display scales halved; the fitting cell pattern mapping.
- The brief said to commit the measuring script first. Nothing is committed in this build: `texel_density.py` and `texel_uv_rescale.py` sit in the tracked `tools/model_export/` folder, ready to commit.
- The fitting generator `tools/staff_fittings/gen_staff_fittings.py` is in the ignored part of `tools/`, so its 16 px change lives only on this machine. Moving it into `tools/model_export/` would keep it.
- Several T1 models are orphans that nothing in the game draws: `item/living_blaster`, `item/living_rifle`, `block/rune_mod_station`, `block/chitinite_mace`, the two Alembic growth models and `item/living_syringe_rack`. The first six were rescaled anyway; the last was left.
- Worth a look: a decal stays whole only on faces 4 units or bigger; on tiny faces it now shows its centre. The Leech mouth, the crossbow's ports and the Barbed Blade's jar bases are the places to check.

### Verification (round 1)

- Scope: the tracked diff against `refs/build2/B20-before` is 112 model, bbmodel and fitting texture files, six generators in `tools/model_export/`, `gradle/review-clients.gradle` and three docs. New files: the two scripts and `TexelClientReview.java`. No game Java, lang, payload, blood, lash or HUD file changed. Nothing held or reversed was touched.
- Values: `texel_density.py` lists the same 20 flagged models the log names. `texel_uv_rescale.py --check` makes no face change. The seven fitting sheets are 16x16. The old sheets and the old generator are in `removed_assets/2026-11-15-texel-t3-old-textures/` with `MANIFEST.txt` lines.
- Tests: the plan names no new test for T12 or T14; the model tests that read these files stay green. No test was changed.
- Unit suite: BUILD SUCCESSFUL, 6,129 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B20-verify-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B20-verify-gametest-latest.log`).
- `runTexelReviewClient`: past the Accessibility screen, `TEXEL_REVIEW captured 63`, the client stopped itself, no java left. Shots: `build/build2/B20-verify-shots/`. The implementer's shots were kept in `build/texel-review/screenshots-implementer-pass/`.
- Small doc fix needed: the `create` op in `docs/TESTING.md` has no `seed`. Without one the driver throws (`action_error`, NullPointerException on `seed`). Use `{"op":"create","name":"TexelReview","seed":1234}`.
- Result: pass.

## B21. D0 to D6 journey

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 10 T1 (PT-19) | `stashedConcentratedVialIsReissued` and `droppedConcentratedVialIsReissued` now pin the kept rule (a vial not carried is reissued) and are required. | `PlaythroughVotaryGameTests.java` |
| 10 T2 (PT-3), late rewards | Onto `OwedRewards` (owed, then delivered; nothing dropped at the NPC): the Monolith scrap, the stained church map, the Circus waybill, the Relay Chart, the Artificer's fittings, materials and D7 fitting, the Vein-Mason continuation, the D5 and D6 rewards and pattern replacements. New `OwedRewards.give` and `carriesOrOwed(player, predicate)`. A reissue checks carried or owed; a pattern replacement is refused while one is owed. | `OwedRewards.java`, `VicarDialogueEvents.java`, `MnemonistDialogueEvents.java`, `VoyagerDialogueEvents.java`, `ArtificerDialogueEvents.java`, `VeinMasonDialogueEvents.java` |
| 10 T2 (PT-3), station half (NQ-10-10) | `CentrifugeOutputSlot.mayPickup` refuses an assigned output to anyone but its player (`FirstSeparationAssignment.assignedPlayer`). The Effigy records who set the motif paper (`Preparer` in its NBT); the finished pattern goes to that player through `OwedRewards` with the first-pattern credit. If the preparer is offline at the finish, the pattern falls at the Effigy as before. | `CentrifugeOutputSlot.java`, `MasonsEffigyBlockEntity.java` |
| 10 T4 (PT-8, NQ-10-12) | The Mortal Display cannot be broken in survival while its Hermit lives (`getDestroyProgress` 0 and `onDestroyedByPlayer` refuses). A Display with no link, when placed or touched, links to the nearest Hermit within 16 blocks that no Display names. The touch tells awakened blood "cannot attach" before the blessing check. The Hermit finds his own Display (`MortalDisplayBlockEntity.of`): a heart taken by someone else gives a no-heart tree and the blessing is refused; a player whose heart came from another temple gets a stranger tree with no farewell. | `MortalDisplayBlock.java`, `MortalDisplayBlockEntity.java`, `HarbingerHermitEntity.java`, `HarbingerHermitDialogueTrees.java`, `HermitDialogueEvents.java` |
| 10 T4 (PT-9, NQ-10-9) | A founder's projection on a standing recruit starts nothing: the recruit is told who offers and to kneel (sneak), the founder that they wait (once a second, action bar). A sneaking recruit is received. No screen, no payload. | `EarlyInitiation.java` |
| 10 T5 (PT-2, PT-15) | `vicar.blood_crafting.absorption` says only blood that finds room counts. The Hermit Road report option and node are added to every Vicar tree past D1 while a wild remnant is read and unreported. | `HarbingerVicarDialogueTrees.java`, `HarbingerVicarEntity.java`, lang |
| 10 T5 (PT-1b) | Each tank keeps a per-player tally of fresh blood poured in with Blood Projection (`BlockBloodVolume.recordPoured`, NBT `Poured`). Every `projectFromEntity` call records what it added to the looked-at tank (`BlockBloodInteractions.trackPoured`). An Absorption draw takes from the drawer's own share first and that part is not counted for the D1 proof. A share is trimmed to the fresh blood left. The absorption hint says own poured blood does not count. | `BlockBloodVolume.java`, `BlockBloodInteractions.java`, `BloodProjectionItem.java` |
| 10 T6 (PT-11) | Concentrated Blood settles when the player gets up after 100 ticks in bed (`ConcentratedBlood.SETTLE_TICKS`, vanilla's sleep timer), with or without the night skip. The Chamber dream keeps its night-skip rule. `initiation.blood_rest` says a few moments in bed is enough. | `ChamberVisitEvents.java`, `ConcentratedBlood.java` |
| 10 T6 (PT-18, NQ-10-6) | `attemptStartup` returns the new `ASSIGNED_SPIN_PENDING` (last enum value) while the player's assigned spin has not been recovered. The Alchemist offers "The output of my assigned spin is lost." while one is pending (`FirstSeparationSpinDialogue`, event `alchemist_clear_assigned_spin`), which strikes it. | `VialCentrifugeBlockEntity.java`, `VialCentrifugeStartupResult.java`, `FirstSeparationAssignment.java`, `FirstSeparationSpinDialogue.java`, `AlchemistDialogueEvents.java`, `DialogueEventSpeakers.java`, `HarbingerAlchemistEntity.java` |
| 10 T6 (PT-26, NQ-10-2) | Waking rested with the blood pending and the charm off says `initiation.blood_unsettled_charm`. | `ConcentratedBlood.java` |
| 10 T7 (PT-7) | The D4 hint: "prepared at a Mason's Effigy and committed at an Iron Brazier". | lang |
| 10 T7 (PT-6) | The Woven Vessel card counts two steps (`WOVEN_VESSEL_STEPS = 2`): the blank (or the weave) and the first weave. | `HarbingerAssignmentLedgerScreen.java` |
| 10 T8 (PT-4) | From D5, a directed player with no map carried or owed gets "I lost the road to Masons Respite." in the Vicar's root (`withMasonsRespiteReissue`). | `HarbingerVicarDialogueTrees.java`, `HarbingerVicarEntity.java` |
| 10 T8 (PT-14) | The four issued-lesson keys moved from `PlayerPersisted` into the new `issued_scar_lessons` attachment (copied on death). No migration: the mod is not live. | `IssuedScarLessons.java`, `HemoAttachmentTypes.java`, `HemoCapabilityAccess.java`, `VeinMasonAssignments.java` |
| 10 T8 (PT-22) | Any section already Varicose counts once: at the next Thelemic cast and when the Mason's tree opens (`onD5TreeOpened`). A Dead section does not. | `VeinMasonAssignments.java`, `HarbingerCicatrixAnchoriteEntity.java` |
| 10 T8 (PT-29, NQ-10-11) | With `vascularDegradationOnManipEnabled` off, the first Thelemic cast after the D4 reward counts as the strain (`onThelemicCastWithoutDegradation`). The Mason then says `anchorite.d5.cause_strain.undegraded`. | `ThelemicMemoryActivationService.java`, `HarbingerCicatrixAnchoriteDialogueTrees.java` |
| 10 T8 (PT-28) | The remaining silent returns in the Vicar, Alchemist, Artificer and Mnemonist routes now say `message.hemomancy.dialogue.refused.too_far` or `.not_now` (`DialogueRewards.refuse`). | the four route classes, `DialogueRewards.java` |
| 10 T9 (PT-41, NQ-10-8) | The Basin commission reads either hand. Each commission pays 10 Alchemist rapport and 2 Vivacious Enzyme (`AlchemistCommissionReward`, rapport through the new `NpcTradeService.creditRapport`, so only trade classes write the ledger). | `PhlegethonticCommission.java`, `DeepDarkCommission.java`, `AlchemistCommissionReward.java`, `NpcTradeService.java` |
| 10 T9 (PT-5) | `sanguine_monolith.json` `required_degree` 6 to 5, roster pin back to 5. The map clash (Armature consecration and Sanguine Athanor 37.6 px apart) is fixed with one authored spot: the Athanor at (548, 739), on the outer edge of its D6 ring. | `sanguine_monolith.json`, `HarbingerRecipeMapDefinitions.java`, `WorkingCraftRosterDataTest.java` |
| 10 T9 (PT-27, NQ-10-7 A) | The Vicar gives a scrap whenever none is carried or owed, and says `vicar_scrap_carried` otherwise. | `VicarDialogueEvents.java` |
| 10 T9 (PT-86) | `vicar.covenant.complete_vigil` names a member of the line or a Harbinger sworn to it (Vicar, Alchemist, Mnemonist, Artificer). The assertion is back in `mainStepsAreQuests`. | lang |
| 10 T10 (PT-23) | New `ResidentSpawnSpot.find`: the marker, else the nearest standable cell within 3 blocks (and one up or down), else a logged warning. Used by the outpost and the Respite. | `ResidentSpawnSpot.java`, `HarbingerOutpostStructure.java`, `MasonsRespiteStructure.java` |
| Playthrough (no task) | Cause found: the Archon chain still expected the shatter to drop the Seed, but since the Monolith Walk (2026-11-04) the stone cracks and the Seed waits inside. The chain now breaks the stone and walks in, as `shatterBeforeCornerstone` does. `harbingerD5ToApotheos`, `harbingerD5ToSilentium` and `archonChainFromPreviousBand` pass (still optional). | `PlaythroughArchonGameTests.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| 10 T5 (PT-1): the blood font (NQ-10-1 A) and the hint pointing at it | No font exists. It needs a new block (a living tank that does not clot, with its own model and texture) placed by the outpost and temple generators and their templates run again. That is well past the plan's size and needs a look from the owner. The tally (PT-1b) is built, so today a D1 player draws from creatures or from any unowned tank. The Votary chain now uses an arranged unowned Alembic as the font's stand-in. |
| 10 T7 (PT-24) | Ran the playthrough suite with `-PchamberLoadedValidation=true`. The GameTest server still has no Chamber of Will, so both guided Chamber tests report NOT RUN. The property only adds clinical generator tests; no launcher loads the Chamber. |
| 10 T8 (PT-30) | Built, then put back. The trade loader drops a card that wants an item another card of its speaker wants (`NpcTradeRegistry.check`, guarded by `NpcTradeCatalogueDataTest`), and the Anchorite's Bone and Clay cards already want bone and clay. The ruled price cannot ship without changing those cards or the rule. The card, the never-sold change, the test and the two trade keys are removed. |
| 10 T10 (PT-10) and the shared vacancy store (section 2 row "10 T10") | L, and the row waits on the cause of `courtLeadLostForGood`. In that probe the two void-lost leads open vacancies that still do not refill a skipped day later; I could not find why without changing the Court's rule. Sharing that rule first would spread the fault. |
| 10 T10 (PT-85) | Held by the owner (under investigation). |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,131 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B21-unit-final.txt`). Baseline after B20: 6,129.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1986 required tests passed :)" (`build/build2/B21-gametest-final-latest.log`).
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 435 required tests passed :)", 34 optional failures (`build/build2/B21-playthrough-final-optional.txt`). No new optional failure against the baseline 59; 25 left the list (the flips, the three chain tests and `mycophantrematchline`, which is not mine).
- Snapshots refreshed with `HEMOMANCY_FROZEN_SNAPSHOT_OUT` (LF kept): `registry-ids.txt` gains `neoforge:attachment_types hemomancy:issued_scar_lessons`, nothing else. `route-fingerprints.txt`: 24 lines for 16 routes, each checked: the refusal lines on routes that used to say nothing, owed rewards landing in the inventory instead of `drops=`, and the issued lesson saved in its attachment.
- Flipped to required, each rewritten to its ruling: `stashedConcentratedVialIsReissued`, `droppedConcentratedVialIsReissued` (PT-19), `hermitRoadAfterD2IsDeadEnd` (PT-15), `fullVesselAbsorptionCounts` (PT-2, now asserts 0 counted at a full vessel and the new line), `condenserRoundTripCountsAsAbsorption` (PT-1b; its helper gained a "may fall short" mode), `twoPlayersOneCentrifuge` (NQ-10-10), `secondSpinOverwritesTheProof` (PT-18; four cows, since a drawn cow must recover, and the second spin is pressed and refused), `sleepNeedsTheWholeServer` (PT-11; both players in the world, the sleep counter set by reflection because a harness player has no living tick), `charmRemovedWhilePending` (PT-26), `sharedEffigyChargeCredit` (NQ-10-10), `d5StrainWithDegradationOff` (PT-29), `d5MonolithCannotSpeak` (PT-5), `monolithScrapIsRepeatable` (PT-27; also loses the scrap and gets one back), `alchemistBasinSampleInMainHand` (PT-41; also checks 2 enzymes and 10 rapport), `rewardLandingSurveyLate` (PT-3), `outpostSpawnsAllResidents` (PT-23), `twoPlayersOneTemple`, `brokenMortalDisplayLeavesNoOath`, `wrongHermitClaimsAndSilentFarewell`, `awakenedPlayerAtUnclaimedDisplay` (PT-8), `founderProjectionReceivesRecruit` (PT-9; a standing recruit is told and not received, then kneels).
- Added: `varicoseSectionCountsWhenTheMasonTreeOpens` and `alchemistClearsALostAssignedSpin` (playthrough), `VeinMasonAssignmentsTest.anyVaricoseSectionCountsButADeadOneDoesNot`, `BlockBloodVolumeTest.ownPouredBloodIsTakenFirstAndNeverOutlastsTheFreshBlood`.
- Changed on purpose because a ruling moved the behaviour under them: `mainStepsAreQuests` (asserts the Vigil line names other helpers), `HarbingerHermitRoadMissionSourceTest` (two steps, PT-6), `HarbingerArtificerFittingSourceTest` (fitting through `OwedRewards`, PT-3), `PhlegethonticCollectionTeachingTest` ("either hand", PT-41), `WorkingCraftRosterDataTest` (Monolith D5), `CourtDiscoveryGameTests.voyagersReissueTheChart` and `aLostLetterCanStillBeExplained` (the chart lands in the pack), `BloodInjectionGameTests.founderInitiationBindsOnlyAfterTenSeconds` (the recruit sneaks), `HarbingerJourneyAutomation.touchDrop` (a delivered reward needs no pickup), `alchemistCommissions`, `continuationRewardOnce`, `d5MasonChainWithVascularMending`, `effigyPreparationCreditsTheCharger`, `d7VicarFinalTruthAndMap`, `weightOfFrameChain` (rewards land in the pack, or stay owed with a full pack, instead of dropping), and `votaryChainFromPreviousBand` (the D1 proof from an arranged unowned reservoir, since own poured blood no longer counts). In the fix round, the five Effigy tests in `ClinicalBloodProgressionGameTests` (NQ-10-10; see Fix).
- Opt-in: the playthrough suite was also run once with `-PchamberLoadedValidation=true` for PT-24 (see Skipped).
- No review client launched. The looks touched are one Liber map node (moved about 10 px) and the Woven Vessel card's counter; no harness opens the Rites and Crafts tab or the assignment ledger, and building one is past these rows. Both are in section 4 for the owner's eye.

### Docs

- `docs/NPC_DIALOGUE_AND_TRADE.md`: a new "Owner rulings, the D0 to D6 journey" block records PT-2, PT-1b, PT-15, PT-4, PT-7, PT-6, PT-8, NQ-10-12, PT-9 with NQ-10-9, PT-11, PT-26 with NQ-10-2, PT-18, NQ-10-6, NQ-10-10, PT-14, PT-22, PT-29 with NQ-10-11, PT-28, PT-5, PT-27 with NQ-10-7, PT-41 with NQ-10-8, PT-86, PT-23. The PT-3 line covers the late rewards. Their "not built yet" lines and planned-default lines are gone; PT-30 now says why it is not built.
- `docs/HEMOMANCY_REFERENCE.md`: the settle rule, the Display and Hermit, founder consent, the absorption proof, the assigned spin, the Monolith at D5 (prose and Working craft table), the scrap, the D4 hint, the Vein-Mason strain. `docs/LORE_REFERENCE.md`: no lore changed.
- `docs/PLAYTHROUGH_TESTS.md`: the optional-failure list, the PT-19 probes and the shatter cause. `docs/NPC_VOICES.md`: the commission pin is now "either hand".
- `wiki/Harbinger-Path.md`, `wiki/Clinical-Blood-Tools.md`, `wiki/Phlegethontic-Nether.md`.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-10-7, PT-11 and NQ-10-8; section 2 rows NQ-10-2, NQ-10-6, NQ-10-9, NQ-10-10, NQ-10-11, NQ-10-12, PT-5, PT-22 and PT-86; section 3 rows 10 T1, both 10 T2 rows, 10 T4 (PT-8), 10 T4 (PT-9), 10 T5 (PT-2, PT-15), 10 T6 (PT-11), (PT-18), (PT-26), 10 T7 (PT-7), (PT-6), 10 T8 (PT-28), (PT-4), (PT-14), (PT-22), (PT-29), 10 T9 (PT-41) twice, (PT-5), (PT-27), (PT-86), 10 T10 (PT-23) and "Playthrough (no task)". Shortened: 10 T5 (PT-1) to the font, 10 T7 (PT-24) to the missing launcher, 10 T8 (PT-30) to the clash. Kept: NQ-10-1 and PT-30 in section 1 (not fully built), NQ-10-4, PT-85 and "10 T10" in section 2. Section 4: PT-9 and PT-5 are now "now"; new row "B21 (D0 to D6)".
- `docs/owner_answers/10-early-and-mid-progression.md`: every line of the status table.

### For the owner

- New lang keys: `hemomancy.dialogue.vicar.option.masons_respite_map_again` ("I lost the road to Masons Respite."), `hemomancy.vicar.masons_respite.reissue`, `hemomancy.dialogue.event.vicar_scrap_carried`, `hemomancy.anchorite.d5.cause_strain.undegraded`, `hemomancy.dialogue.alchemist.option.clear_assigned_spin`, `hemomancy.alchemist.first_separation.spin_cleared`, `message.hemomancy.vial_centrifuge.assigned_spin_pending`, `hemomancy.initiation.blood_unsettled_charm`, `hemomancy.initiation.founder_offer`, `hemomancy.initiation.founder_waits`, `hemomancy.hermit.heart_taken.line1`, `.line2`, `hemomancy.hermit.stranger.line1`, `.line2`, `hemomancy.dialogue.event.hermit_heart_spent`, `message.hemomancy.dialogue.refused.too_far`, `message.hemomancy.dialogue.refused.not_now`.
- Changed values: `hemomancy.vicar.adept.degree_hint`, `hemomancy.vicar.blood_crafting.absorption`, `hemomancy.vicar.neophyte.absorption_hint`, `hemomancy.vicar.covenant.complete_vigil`, `hemomancy.anchorite.d5.cause_strain`, `hemomancy.alchemist.phlegethontic_commission.task`, `.recorded`, `hemomancy.alchemist.deep_dark_commission.recorded`, `screen.hemomancy.harbinger_assignment_ledger.step.phlegethontic_sample.desc`, `hemomancy.initiation.blood_rest`.
- Values I chose: the authored Athanor spot (548, 739); a temple reach of 16 blocks for relinking (32 for finding the living Hermit); the founder offer repeats once a second on the action bar; the fallback spawn search also looks one block up and down; the Effigy falls back to the old drop when the preparer is offline; the tally counts any player's own pour, not only the tank owner's, so pouring into an unowned tank and drawing it back cannot pay the proof either.
- PT-30 needs a new call: change the Anchorite's Bone and Clay cards, price the blank in other goods, or allow one want across cards.
- PT-1: the font is the one large piece left of NQ-10-1. Until it exists, a D1 player's reservoir is creatures or an unowned machine.
- Surprising: the Votary chain test was passing the D1 proof only through the round trip PT-1b forbids.
- Looks to judge (no shot): the Rites and Crafts deep ring with the Monolith on it (section 4 row PT-5), and the Woven Vessel card reading 2/2.

### Verification (round 1)

Checked the tree against `refs/build2/B21-before` and the four new files. Result: not passed, one blocking regression.

- Scope: inside the brief. Nothing held or reversed was touched (no PT-85 or NQ-10-5 work, no strand kit, lash, HUD or Silent Severance change). PT-30 was put back in full: no `scar_blank.json`, and `never_sold.json` is unchanged by B21.
- Values match the answers: scrap whenever none is carried or owed (NQ-10-7 A), `SETTLE_TICKS = 100` (PT-11), 10 rapport and 2 Vivacious Enzyme per commission, paid once (NQ-10-8), `sanguine_monolith.json` at Degree 5, the 3-block spawn reach (PT-23), and the section 2 defaults built as written.
- AGENTS.md: no payload added. The new attachment is registered and read through `HemoCapabilityAccess`. Rewards go through `OwedRewards` and its `PendingDelivery` claim. Player text is lang keys, with no em dashes. No vanilla particle added.
- Unit suite: BUILD SUCCESSFUL, 6,131 tests, 0 failures (`build/build2/B21-verify-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B21-verify-gametest-latest.log`).
- Playthrough suite: "All 435 required tests passed :)", 35 optional failures (`build/build2/B21-verify-playthrough-optional.txt`). The only name not in the baseline 59 is `apotheosritewithoutrescue`, the known flake (PT-88). The three Archon chain tests pass.
- Clinical suite (`runClinicalGameTestServer`), BLOCKING: 5 required failures, all new (B08's clinical run passed all 132): `chargedEffigySelectionSurvivesInterruptionAndReload`, `effigyProjectionRejectsUnknownScarsWithoutLosingPaidWork`, `effigyProjectionRejectsInsufficientCapacityWithoutLosingPaidWork`, `personalScarCycleOpensIlluminatusWithoutPromotingEarly`, `personalEffigyPreparationThenCommitEarnsOnlyTheActorsMainProof` (`build/build2/B21-verify-clinical-latest.log`). Cause: NQ-10-10 now sends the finished Effigy pattern to the preparer through `OwedRewards`, so it lands in the pack. These tests in `ClinicalBloodProgressionGameTests.java` (for example lines 510-512, 742-746, 796-798) still count a dropped `ItemEntity` at the Effigy. Fix: rewrite each to the ruling on purpose (one pattern with the right scar ids in the preparer's inventory or owed, none on the ground), then run the clinical suite again. The two older clinical baseline failures no longer fail.
- Docs: the log matches the tree. Only chunk 10 rows left `docs/OPEN_QUESTIONS.md`, and the rulings are in `docs/NPC_DIALOGUE_AND_TRADE.md` with ids and dates. Minor: the per-speaker rows for the Hermit, the recruit prompt and the charm line in the dialogue roll-up still say "waits on NQ-10-9" or "NQ-10-2". Those rows belong to another area.
- No review client: no harness opens the Rites and Crafts map or the assignment ledger. Both looks are in section 4.

### Fix

- The blocking regression is fixed in tests only. The five Effigy tests in `ClinicalBloodProgressionGameTests.java` were rewritten to NQ-10-10 on purpose. They were not weakened.
- `personalEffigyPreparationThenCommitEarnsOnlyTheActorsMainProof`, `chargedEffigySelectionSurvivesInterruptionAndReload` and `personalScarCycleOpensIlluminatusWithoutPromotingEarly` now find exactly one pattern, with the expected scar ids, in the preparer's inventory and none on the ground. The charged test also checks that a later projection adds no second pattern. Two small helpers were added: `carriedPatterns` and `droppedPatterns`.
- The two `effigyProjectionRejects*` tests (shared helper `effigyRejectsIneligibleContributor`): the actor sets the paper and the guest finishes. Under NQ-10-10 the pattern and its credit belong to the actor. A fixture player is not on the server's player list, so the actor counts as offline: one pattern with the selected ids falls at the Effigy, the guest carries none, and neither player gets the first-pattern credit. The test now asserts that. The online-preparer case across two players is covered by the playthrough test `sharedEffigyChargeCredit`, not here.
- No main code changed.
- Clinical suite: "All 132 required tests passed :)" (`build/build2/B21-fix-clinical.txt`).
- Unit suite: BUILD SUCCESSFUL, 6,131 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B21-fix-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B21-fix-gametest.txt`).
- Playthrough suite: "All 435 required tests passed :)", 35 optional failures, the same list as round 1 (`build/build2/B21-fix-playthrough-optional.txt`). The only one not in the baseline is the known flake `apotheosritewithoutrescue`.
- The Built and Skipped tables stay as they are: no row was taken out. The NQ-10-10 line in Built holds. The Tests list gains the five clinical rewrites above.
- No server or client is left running.

### Verification (round 2)

Checked the tree against `refs/build2/B21-before` after the fix round. Result: passed.

- Scope: unchanged since round 1. 68 tracked files and the four new files listed in Built. Nothing held or reversed is touched. No payload, lash, HUD, allow-list, `never_sold` or trade card change.
- The fix: only `ClinicalBloodProgressionGameTests.java` changed. The three own-preparer tests now ask for one pattern with the right scar ids in the preparer's pack and none on the ground. The two `effigyProjectionRejects*` tests now ask that the guest who finished gets no pattern and no credit, and that the pattern falls at the Effigy. That matches `MasonsEffigyBlockEntity`: the pattern goes to the finisher when the finisher set the paper, else to the preparer if online, else it falls. No assertion was dropped.
- Spot checks of values: `ConcentratedBlood.SETTLE_TICKS = 100`, `AlchemistCommissionReward` 10 rapport and 2 Vivacious Enzyme, `sanguine_monolith.json` `required_degree` 5, the scrap given only when none is carried or owed. New lang lines have no em dash.
- Unit suite: BUILD SUCCESSFUL, 6,131 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B21-verify2-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed :)" (`build/build2/B21-verify2-gametest-latest.log`).
- Playthrough suite: "All 435 required tests passed :)", 34 optional failures, none outside the baseline 59 (`build/build2/B21-verify2-playthrough-optional.txt`).
- Clinical suite: "All 132 required tests passed :)" (`build/build2/B21-verify2-clinical-latest.log`).
- Docs: the log matches the tree. The minor note from round 1 stands: the dialogue roll-up rows for the Hermit, the recruit prompt and the charm line still say they wait on NQ-10-9 or NQ-10-2. They belong to another area.
- No review client launched, as in round 1. The two looks are in section 4. No server or client is left running.

## B22. Bloodlines and the Covenant Throne

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 11 T1 | New `BloodlineMembership`: expel, leave, dissolve, petitions and the login settlement. Kick, ledger Disband, the Unbinding, `/hemo bloodline disband`, the Severed Covenant and the Unstained cure all call it. What happens to an away member (expelled, dissolved with a share owed, admitted, refused) is kept in `BloodlineSavedData` (`pendingNotices`, outside `Bloodline.serialize()`) and settled at login, or at the respawn after a dead login. The fault read in passing is fixed: clean blood now also leaves the saved line (tick and login), so a login no longer puts it back; a cleansed leader's line is dissolved. | `BloodlineMembership.java` (new), `BloodlineSavedData.java`, `BloodVolumeEvents.java`, `PacketKickBloodlinePlayer.java`, `PacketLedgerAction.java`, `CardinalRiteCompletionEffects.java`, `BloodlineSubcommand.java`, `UnstainedCardinalRiteEvents.java`, `PathMutualExclusionHelper.java` |
| 11 T2 (PT-36, PT-90, NQ-11-3 to NQ-11-6) | `ACTION_LEAVE_BLOODLINE = 4` (an int in the existing payload, layout unchanged). The ledger screen shows Leave where the leader sees Disband; a second click confirms. The Vicar offers "I want to leave my bloodline." to members who are not the leader (event `vicar_leave_bloodline`, Vicar only). A leaver takes no share and loses that line's ledgers; the leader is refused, and the Disband button's tooltip says the Progenitor cannot leave. "Leader" and "Member" are keys. | `PacketLedgerAction.java`, `LedgerScreen.java`, `HarbingerVicarDialogueTrees.java`, `HarbingerVicarEntity.java`, `VicarDialogueEvents.java`, `DialogueEventSpeakers.java` |
| 11 T3 (PT-37, NQ-11-1 B, NQ-11-2) | Removal record per line (player to overworld game time). Every ledger is stamped `minted` when written (`UnsignedLedgerItem.sign`); no stamp reads 0. A ledger written at or before its holder's removal crumbles: at the kick or leave, on inventory tick, on use and at login. One line, `SAND_BREAK` at 0.9 / 0.7, 14 ash-grey ember motes (`HemoParticleData.ember`, colour 78, 66, 62). Nothing drops. `use` reads membership from world data, not from the item. | `BloodlineDisbandHelper.java`, `UnsignedLedgerItem.java`, `BloodlineSavedData.java`, `CardinalRiteCompletionEffects.java` |
| 11 T4 (PT-44, NQ-11-9, NQ-11-10) | The Unbinding pays each player member `pool / members`; online now, away members at login. Capped by room in the vessel; the line says how much was paid. Recorded in the blood ledger as `bloodline_share`. The Disband pays nothing. | `BloodlineMembership.java` |
| 11 T6 (PT-64, PT-65, PT-70, NQ-11-11 A, NQ-11-12) | One binding rule: active blood, a line, and the Throne inside that line's Fane (`isWithinFane(leader, pos)`). Two refusal keys replace `not_progenitor`. The Trance stays the Progenitor's; a seated member is told so and pays nothing. The bound Throne (dimension, block, respawn) is recorded on the degree attachment (`bound_throne`). Dissolve, leave and kick clear the bond: `covenant_throne_bound` revoked (new `HarbingerAdvancementGranter.revoke`), the respawn point reset when it is still the Throne's, and a line said. A completed Living Covenant is kept. The Covenant Vigil was checked: no leader-only step. | `CovenantThroneBlock.java`, `BoundThrone.java` (new), `InitiatoryDegree.java`, `IInitiatoryDegree.java`, `HarbingerAdvancementGranter.java` |
| 11 T7 (PT-69) | `CovenantProgress` reads `leadsLine` and `lineHasFane` live. At D6 with the Throne unbound: no line gives `found_bloodline`, a leader with no Fane `found_fane`, a member whose line has none the new `line_has_no_fane`; otherwise `bind_throne`. The Vicar and the Monolith both read it. `bind_throne` now says "your line's Fane". | `CovenantProgress.java`, `HarbingerVicarDialogueTrees.java`, lang |
| 11 T8 (PT-35, NQ-11-7, NQ-11-8) | A non-member's ledger use checks D1 and active blood (own lines), then writes a petition (cap 16, `MAX_PETITIONS`). The leader is told at once or the count at login. Ledger screen petition row: step, Admit, Refuse. New payloads `PacketSyncBloodlinePetitions` (clientbound) and `PacketAnswerBloodlinePetition` (serverbound: ledger in hand, `ServerboundRateLimiter` 5 ticks, then one service call). Admit re-checks an online applicant; an away one is joined and told at login. A petition goes when its applicant joins any line, leaves the Harbinger path or the line ends. The founder-officiated initiation is unchanged. `PROTOCOL_VERSION` 52 to 53, `payload-registry.txt` rewritten. | `UnsignedLedgerItem.java`, `BloodlineMembership.java`, two payloads (new), `BloodlinePetitionClientData.java` (new), `ClientboundStateHandlers.java`, `PacketHandler.java`, `LedgerScreen.java` |
| 11 T9 (PT-42, NQ-11-13) | The Vicar's `found_fane` ends with the full-pool clause. `ScreenChromeReview` gains `bloodline_pool_full` and `ledger_petitions` scenes. | lang, `ScreenChromeReview.java` |
| 11 T10 (PT-87) | A line stores `founder` and an optional `customName` (nothing sets one yet). `Bloodline.displayName()` returns `hemomancy.bloodline.default_name` with the founder's name; "No Bloodline" is `hemomancy.bloodline.none`. About fifteen callers pass the component. Two English allow-list rows removed. No migration. | `Bloodline.java` and its callers |
| 11 T11 (PT-66, NQ-11-14) | `eternal_covenant_sealed` on the degree attachment; `MaxBloodLedger` and both rite sites read it. `ETERNAL_COVENANT_TAG` is gone. The cure clears it (`resetHarbingerProgress`). The `MaxBloodLedger` (1) and `CardinalRiteCompletionEffects` (2) persistent-data rows are gone. | `MaxBloodLedger.java`, `CardinalRiteCompletionEffects.java`, `PathMutualExclusionHelper.java`, `persistent-data-allow-list.txt` |
| 11 T12 | Rulings recorded in topic docs and the references; impact map regenerated. | docs |
| Section 2: NQ-11-2 to NQ-11-14 | Built as written (see the rows above). | |

### Skipped

| Row or part | Why |
| --- | --- |
| none | Every row of the area was built. Held work was not touched (PT-85 and NQ-10-5 stay as they are). |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,138 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B22-unit2.txt`). The first run failed `PersistentDataAllowListTest` on the now-empty `CardinalRiteCompletionEffects` row; the row was removed.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1986 required tests passed :)" (`build/build2/B22-gametest2-latest.log`). The first run failed three: two fingerprint goldens (below) and `livingCovenantFixturesUseRealPathsAndRestoreState`.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 439 required tests passed :)", 24 optional failures (`build/build2/B22-playthrough-optional.txt`). The only one not in the baseline 59 is the known flake `apotheosritewithoutrescue` (PT-88). All eleven chunk-11 probes left the optional list.
- Snapshots refreshed on purpose: `src/gameTest/resources/command/hemo-command-fingerprints.txt` (8 lines) and `src/gameTest/resources/scriptorium/operation-fingerprints.txt` (260 lines). Every changed line was checked: the saved `Bloodline` tag reads `founder:""` in place of `name:"No Bloodline"` (PT-87), and the empty `NeoForgeData={}` entry is gone because nothing reads the root persistent data for the seal any more (PT-66). Nothing else differed.
- Flipped to required and rewritten to the ruling: `kickedMemberStaleLedger` (PT-37: the carried ledger crumbles at the kick, a chest copy on use, a third player's copy stays, a newer ledger petitions), `ledgerJoinHasNoGate` renamed `ledgerJoinNeedsTheGates` (PT-35), `bloodlineRecallAndMemberPowers` (PT-36: the member leaves, the Recall ledger crumbles, the leader is refused), `hematicUnbindingOfflineShare` (PT-44, plus a member who logs in dead and is paid at the respawn), `memberCannotBindThrone` renamed `memberBindsTheLeadersThrone` (PT-64, with the refused Trance), `anyProgenitorBindsAnyThrone` renamed `anotherLinesProgenitorCannotBindThisThrone` (PT-65), `disbandedProgenitorThrone` (PT-70: bond and respawn gone, line said), `memberRouteToD7` (PT-90: binds the leader's Throne), `linelessD6VicarGuidance` (PT-69), `bloodlineNameIsEnglish` renamed `bloodlineNameIsTranslatable` (PT-87), `eternalCovenantAcrossDeath` (PT-66: arranges the attachment, not the root key).
- Changed on purpose: `ledgerJoinBySecondPlayer` (the join now waits for the admit), `kickAndRelog` (the away member is logged out, told at login, and their old ledger crumbles), `SanguineMonolithDialogueTreesTest` (a lineless D6 now gets `found_bloodline`, PT-69), `BloodlineDisbandFaneSourceTest` (asserts the one service), the journey fixture for `COVENANT_THRONE_BOUND` (gives the line a Fane at the Throne, since a Throne binds only in one; this fixed `livingCovenantFixturesUseRealPathsAndRestoreState`).
- Added: GameTests `petitionWaitsForTheLeaderAndTheApplicant`, `refusedPetitionJoinsNothing`, `forgedPetitionAnswerFromAMember`, `cureClearsTheEternalCovenantSeal`; JVM `BloodlineSavedDataTest` (NBT round trip of notices, removals and petitions; kept off the ledger tag; the stale rule as a pure function; the 16 cap; disband keeps what is owed; the translatable name), `VicarCovenantGuidanceTest.degreeSixThroneStepFollowsTheLineAndItsFane`.
- Review client: `runScreenChromeReviewClient -PchromeLabel=b22`, 34 captures; it got past the menus and stopped by itself. No process is left running.

### Docs

- `docs/NPC_DIALOGUE_AND_TRADE.md`: new "Owner rulings, bloodlines" block (PT-35, NQ-11-7, NQ-11-8, NQ-11-2, NQ-11-1 B, NQ-11-5, PT-69, NQ-11-13, PT-87); the not-built lines and planned defaults removed.
- `docs/ENDGAME_PATHS.md`: PT-64, NQ-11-11, PT-65, PT-70, NQ-11-12, PT-36, NQ-11-3 to NQ-11-6 and PT-90 marked built; two calls added (the member's refused Trance, the Vigil check); the status line, open-questions line, known gaps and history updated.
- `docs/CARDINAL_RITES.md`: PT-44, NQ-11-9, NQ-11-10, PT-66 and NQ-11-14 recorded as built; their planned defaults and gaps removed.
- `docs/PLAYTHROUGH_TESTS.md`: the chunk-11 rulings with their probe names; the optional-failure list says 0 left for chunk 11.
- `docs/HEMOMANCY_REFERENCE.md` (section 12, the Covenant Throne note, the Unbinding and Eternal Covenant rows), `wiki/Harbinger-Path.md` (joining, leaving, the crumble, the Throne, the Unbinding share), `docs/NPC_VOICES.md` (Vicar and Monolith pins), `docs/architecture/CONSOLIDATION_PROGRESS.md` (the persistent-data count), `docs/architecture/evidence/source-text-impact-map.csv` (regenerated). `docs/LORE_REFERENCE.md`: no lore changed.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-11-1 and NQ-11-11; section 2 rows NQ-11-2 to NQ-11-10, NQ-11-12, NQ-11-13 and NQ-11-14; section 3 rows 11 T1, T2, T3, T4, T6, T7, T8, T9, T10, T11 and T12. Section 4: PT-37 and PT-35 are now "now" (PT-35 with the shot), and PT-42 points at the full-pool shot. Not touched: the per-speaker dialogue roll-up rows that still say they wait on NQ-11-7, NQ-11-13 or 11 T6 (another area's table).
- `docs/owner_answers/11-bloodline-and-throne.md`: every line of the status table.

### For the owner

- New lang keys: `block.hemomancy.covenant_throne.no_line`, `.not_in_line_fane`, `.trance_progenitor_only`; `hemomancy.bloodline.default_name` ("%s's Blood Line"), `.none`, `.leave.leader`, `.leave.member_notice`, `.leave.no_bloodline`, `.leave.success`, `.petition.arrived`, `.petition.gone`, `.petition.not_eligible`, `.petition.refused`, `.petition.refused_leader`, `.petition.waiting_count`, `.settle.dissolved`, `.settle.expelled`, `.share_paid`, `.throne_bond_cleared`; `hemomancy.ledger.crumbled`; `message.hemomancy.blood_volume_events.bloodline_share`; `message.hemomancy.unsigned_ledger_item.needs_active_blood`, `.needs_degree`, `.petition_full`, `.petition_sent`, `.petition_waiting`; `screen.hemomancy.ledger.admit`, `.refuse`, `.leave_bloodline`, `.leave_confirm`, `.petition_next`, `.petition_previous`, `.petition_none`, `.petitions`, `.role.leader`, `.role.member`. Vicar: `hemomancy.dialogue.vicar.option.leave_bloodline`, `.leave_bloodline.confirm`, `hemomancy.vicar.leave_bloodline.line1`, `.line2`, `hemomancy.dialogue.vicar.option.covenant.line_has_no_fane`, `hemomancy.vicar.covenant.line_has_no_fane`. Monolith: `hemomancy.monolith.counsel.line_has_no_fane`.
- Changed: `hemomancy.vicar.covenant.found_fane` (the full-pool clause), `hemomancy.vicar.covenant.bind_throne` and `hemomancy.monolith.counsel.bind_throne` ("your line's Fane"). Removed: `block.hemomancy.covenant_throne.not_progenitor`.
- Values I chose: the crumble sound (sand break, volume 0.9, pitch 0.7) and 14 ash motes in colour (78, 66, 62); a 5-tick rate limit on petition answers; the leader's ledger panel grows from 190 to 244 px for the petition row; the petition list sent is capped at 64 names of up to 64 characters.
- Looks to judge: `build/build2/B22-review/chrome-b22-ledger_petitions.png` (the leader's petition row) and `chrome-b22-bloodline_pool_full.png` (the greyed Donate; the review player's own blood is inactive, which also greys it). A member's Leave button has no shot: the chrome review player leads the line.
- Surprising: a GameTest harness player who never logged out is still found by `getPlayerList().getPlayer`, so tests of away members must call `offline()`. A cleansed leader's line is now dissolved; before, the cure left a leaderless line. At D6 a leader with no Fane hears the `found_fane` line, which is worded for D5.

### Verification (round 1)

- Scope: only chunk 11 rows changed. No held or reversed work, no lash numbers, no HUD files. Created files match the report.
- Values checked in code: NQ-11-1 B (`BloodlineSavedData.crumbles`: minted at or before the removal; a newer ledger petitions), NQ-11-11 A (Trance refused for members, no cost), NQ-11-7 cap 16, NQ-11-9 Disband pays nothing (only `UNBINDING` pays), NQ-11-10 share capped by vessel room, NQ-11-12 bond cleared on dissolve, leave and kick only, NQ-11-14 the cure clears `eternal_covenant_sealed`.
- Rules: protocol 53 and `payload-registry.txt` match the two new payloads; the serverbound answer checks the held ledger and is rate-limited; the crumble uses `HemoParticleData.ember`, the vanilla allow-list is unchanged; every new key is in `en_us.json`; `not_progenitor` has no reader left.
- Tests: each row has a required probe or JVM test; flipped probes assert the ruling, none weakened.
- Unit suite: BUILD SUCCESSFUL, 6,138 tests, 0 failures (`build/build2/B22-verify-unit.txt`).
- Combined GameTest run: "All 1986 required tests passed" (`build/build2/B22-verify-gametest.txt`).
- Playthrough: "All 439 required tests passed", 24 optional failures; the only one outside the baseline is the known flake `apotheosritewithoutrescue` (PT-88).
- Review shots `build/build2/B22-review/*.png` were taken after the last screen edit; the petition row reads cleanly. No client relaunched.
- Docs: only this area's rows left `docs/OPEN_QUESTIONS.md`; NQ-11-1 and NQ-11-11 are recorded in their topic docs.
- Result: pass.

## B23. D7 to D8 endgame

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 12 PT-74 (NQ-12-1 A) | Every Monolith shatter gives 8 Monolith Fragments (`MonolithFragmentDropRules.FRAGMENTS = 8`; the 5 to 8 roll is gone). The D-23 warning stays as built. | `MonolithFragmentDropRules.java`, `SanguineMonolithBlockEntity.java` |
| 12 PT-77 (NQ-12-2 A) with V-12-1 | Atlas gates: `monolith_fragment`, `monolith_imbued_cloth`, `monolithic_cornerstone` at D7, the four `hallowed_residuum_*` at D8, each a veiled preview one Degree early by the atlas's own rule. V-12-1: `silent_archon_chestplate` D7 to D8; `edacious_`, `phantasmal_` and `sheolic_blood_lust_chest` D5 to D7. Both `MaterialsData` predicates and `MaterialAtlasSpec` gates moved. | `MaterialsData.java`, `MaterialAtlasSpec.java` |
| 10 PT-31 (NQ-10-3 A) | The Sporitic Thurible is a D8 Apotheos relic, same effect. New `SporiticThuribleRules.mayBurn(degree, path)` (= `HarbingerPathPermissions.isApotheos`) gates lighting and the burn tick (`REQUIRED_DEGREE` 4 removed). The Working craft's `required_degree` 4 to 8, and `CardinalRiteActivation` refuses anyone but an Apotheos. Atlas `d(3)` to `d(8)` with an Apotheos-only unlock. Tab: moved to the end of Blood Tools (after `memory_thread`). Probe `d4ThuribleHasATeacher` retired to `removed_assets/`. | `SporiticThuribleRules.java`, `SporiticThuribleItem.java`, `SporiticThuribleEvents.java`, `CardinalRiteActivation.java`, `sporitic_thurible.json`, `CreativeTabGroup.java`, `MaterialsData.java`, `MaterialAtlasSpec.java` |
| 12 T8 (PT-80, NQ-12-4 A, V-12-4) | The node stays the respawn point. New `PriorRespawn` (dimension, pos, angle, forced): planting stores the respawn the node replaces on the node's entry (a replant keeps the first one; NBT `Prior`). New `FungalGardenTravelHelper.returnHome`: the stored return point, else the prior respawn (or the player's own respawn when it is outside the Gardens), run through vanilla's respawn search; a missing bed or none goes to the world spawn. Free. Used by the Spine and Podium inside the Gardens when no return point is stored (the "finds no thread" line is gone) and by a new owner-only **Leave the Gardens** button on the heart-node screen (`HeartNodeLeavePacket`, serverbound, alive, rate limit 20 ticks, node in block reach and the sender's, not on the first projection). `PROTOCOL_VERSION` 53 to 54, `payload-registry.txt` rewritten. When the node goes, its owner gets the prior respawn back (at once if online, else at login) instead of none. | `PriorRespawn.java` (new), `HeartNodeEntry.java`, `HeartNodeSavedData.java`, `HeartNodeEvents.java`, `HeartNodeBlock.java`, `FungalGardenTravelHelper.java`, `HeartNodeService.java`, `HeartNodeLeavePacket.java` (new), `PacketHandler.java`, `HeartNodeScreen.java` |
| 12 T9 item 5 (FX-16) | New `HeartNodeService.forsake`: a player whose Harbinger progress is reset (`PathMutualExclusionHelper.resetHarbingerProgress`) loses the node (it withers, synapses go inert, prior respawn back). A login also forsakes the node of anyone who is not an Apotheos. | `HeartNodeService.java`, `PathMutualExclusionHelper.java`, `HeartNodeEvents.java` |
| 12 T12 axe rule (NQ-12-5) | The rule stays as built and is now written: reference §5.6f and a new Alchemist bog body inquiry (two lines). | `dialogue_inquiry/alchemist/hemomancy/bog_body.json` (new), lang, reference |
| 12 T2 follow-up (V-12-2) | The Monolith's Archon counsel checks past-the-Seed first, so a stranger who carried a Seed out hears `past_the_seed`, not `not_your_stone`. | `SanguineMonolithDialogueTrees.java` |
| 12 possession minor (V-12-5) | `PossessionEvents.onClone`: on a death clone the respawned body gets Mind Broken with the time it had left. | `PossessionEvents.java` |
| V-12-3 | Kept as built: a node no player planted withers at its first tick with no drop. The test now also asserts no item drops. | `RegistrationGameTests.java` |
| V-12-6 | Kept as built (armor stand keeps the gear worn at return). Docs only. | docs |
| 12 T13 (C-2) | Dropped by the owner: the ability keeps the name Silent Severance. Nothing renamed; ruling recorded. | docs |
| P13 flake (`archonRiteWithHelper`) | Read the one failing log (`build/overnight/P13-playthrough-1-latest.log`): the only difference from passing runs is "could not place iron_brazier by hand; set it". The Vicar was spawned at his station before the braziers were built and could stand on a socket. He is now spawned after `prepare`, so the floor is built first. Probable cause, not proven: the failure was seen once. | `PlaythroughArchonGameTests.java` |
| "Three chain tests stop at the shatter drops the Seed" | Already fixed by B21 (the test, not the game: the Seed waits inside the cracked stone). Checked: `harbingerD5ToApotheos`, `harbingerD5ToSilentium` and `archonChainFromPreviousBand` pass in this run. Docs that still called the cause unknown are corrected. | docs |

### Skipped

| Row or part | Why |
| --- | --- |
| 12 T11 (PT-83) and 12 T11 Hermit line (UN-19) | Next package, per the brief. |
| The walk home inside the Gardens, in `heartNodeReviewClient` | The GameTest server has no Gardens and the review client has no leave scene; building one is past these rows. The destination is proved outside the Gardens by `theWayHomeFindsTheBedElseTheWorldSpawn`. Left in section 4 for the owner. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: first run 2 failures, both mine (`PlayerBlockEditGuardrailTest`: the new `removeBlock` in `HeartNodeService` needed an allow-list row; `MonolithFragmentResourceTest` pinned the old roll). Fixed; final run BUILD SUCCESSFUL, 6,140 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B23-unit2.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1989 required tests passed :)" (`build/build2/B23-gametest1-latest.log`). Baseline after B22: 1,986; three added.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 439 required tests passed :)", 21 optional failures (`build/build2/B23-playthrough1-optional.txt`). Only one is outside the baseline 59: the known flake `apotheosritewithoutrescue` (PT-88). `archonRiteWithHelper` and the three chain tests passed.
- Added: `HeartNodeScreenGameTests.leavePacketsAreGuarded`, `.theWayHomeFindsTheBedElseTheWorldSpawn` (bed, bed gone, none; the node stays the respawn), `.aForsakenNodeWithersAndGivesTheBedBack` (FX-16 item 5); `PlaythroughEndgameGameTests.mindBrokenSurvivesADeath` (V-12-5, required); `HeartNodeSavedDataTest.theRespawnBeforeTheNodeIsRememberedAndGivenBack`; `SanguineMonolithDialogueTreesTest.aStrangerPastTheSeedHearsThePastTheSeedCounsel`; `SporiticThuribleRulesTest.onlyAnApotheosMayBurnIt`.
- Flipped to required and rewritten to the ruling: `shatterBeforeCornerstone` (NQ-12-1: exactly 8 and enough for the crafted Cornerstone plus the Frame rite), `atlasFragmentsAndResiduum` (NQ-12-2 and V-12-1: each entry unlocked at its Degree, veiled one early, hidden two early, and locked for a D5 player).
- Changed on purpose: `MonolithFragmentDropRulesTest` and `MonolithFragmentResourceTest` (the fixed count), `pressFurtherShatters`'s spill assertion (8), `WorkingCraftRosterDataTest` (Thurible Degree 8), `anUnplantedHeartNodeWithersAtOnce` (also no drop, V-12-3), `heartNodeRespawnTrap` (still NOT RUN; now names its destination), `archonRiteWithHelper` (Vicar spawned after the floor).
- Retired: `PlaythroughAdeptGameTests.d4ThuribleHasATeacher` (to `removed_assets/src/gameTest/.../PlaythroughAdeptGameTests.d4ThuribleHasATeacher.java.txt`, MANIFEST row added).
- Snapshot: `src/gameTest/resources/frozen/creative-tab-order.txt` moves `hemomancy:sporitic_thurible` from after `terrestrial_speculum` to after `memory_thread`. Nothing else; the combined run's frozen test passed on it.
- Review client: `runScreenChromeReviewClient -PchromeLabel=b23`, two launches, both got past the menus and logged "captured 34" and stopped by themselves. The first shot showed the button label clipped at 86 px; widened to 100 px and shot again. No process left running.

### Docs

- `docs/ENDGAME_PATHS.md`: rulings C-2/NQ-12-3 (kept name), NQ-12-1, NQ-12-2 with V-12-1, NQ-12-4 with V-12-4, NQ-10-3, NQ-12-5, V-12-2, V-12-3, V-12-5, V-12-6 recorded with ids and dates; FX-16 and FX-17 rows updated; status line, calls, open questions, gaps (the chain tests, the flake, the heart-node and possession minors) and history updated.
- `docs/HEMOMANCY_REFERENCE.md`: the atlas rule, the way home, the node's respawn, Mind Broken through a death, the bog body tool rule (§5.6f), 8 fragments (three places), the Thurible (item row, craft table, craft note, status line). `docs/LORE_REFERENCE.md`: no lore changed.
- `docs/NPC_DIALOGUE_AND_TRADE.md` (V-12-2, NQ-12-5), `docs/PLAYTHROUGH_TESTS.md` (probes and the flake), `docs/architecture/ARCHITECTURE_STANDARDS.md` (protocol 54), `wiki/World-Content.md` (the button and the bed), `docs/architecture/evidence/source-text-impact-map.csv` (regenerated), `src/test/resources/architecture/player-block-edit-allow-list.txt` (one row).
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-12-1, NQ-12-2, NQ-12-4, NQ-10-3 and NQ-12-3/C-2; section 2 rows NQ-12-5, V-12-1 to V-12-6; section 3 rows P13 flake, 12 T8, 12 T9 item 5, 12 T12 axe rule, 12 T13, 12 PT-74, 12 PT-77, 10 PT-31, 12 T2 follow-up and 12 possession minor. Kept: UN-19 and the two 12 T11 rows (next package). Section 4: PR-4's "after" is now "Now"; new rows "B23 (D7 to D8)" and "archonRiteWithHelper".
- `docs/owner_answers/12-endgame-paths.md`: status lines T2, T8, T9, T11, T12, T13, PT-74, PT-77. `docs/owner_answers/10-early-and-mid-progression.md`: a PT-31 status line.

### For the owner

- New lang keys: `screen.hemomancy.heart_node.leave` ("Leave the Gardens"), `screen.hemomancy.heart_node.leave.tooltip` ("Go home: where you came in, else the bed you slept in before the node, else the world spawn. It costs nothing, and the node stays your respawn."), `item.hemomancy.sporitic_thurible.message.apotheos` ("The thurible answers only an Apotheos."), `message.hemomancy.cardinal_rite_activation.sporitic_thurible_apotheos` ("The floor will not take the thurible. It is grown for an Apotheos, and you are not one."), `hemomancy.alchemist.item_inquiry.bog_body.line1` ("Scrape it with an axe and you get adipocere. Take it by hand or with a scalpel and you get vivianite. The scalpel gives two and cuts you less."), `.line2` ("Either way the body is spent until it grows back. Choose the tool for the yield before you touch it."). The Alchemist lines follow their sheet (procedure, the reason attached).
- Removed keys: `item.hemomancy.sporitic_thurible.message.degree`, `message.hemomancy.fungal_garden_travel_helper.the_spine_twitches_but_finds_no` (nothing says it any more).
- Values I chose: the leave payload's rate limit (20 ticks); the button's size (100 x 14) and place (left of Molt); the Thurible's tab spot (end of Blood Tools); a player with no node and no return point in the Gardens also goes home to their own respawn if it lies outside the Gardens, else the world spawn; a non-Apotheos with a node loses it at their next login too (covers `/hemo` degree changes).
- Look to judge: `build/build2/B23-review/chrome-b23-heart_node.png` (the button beside Molt). A very long owner name in the title would reach the button.
- Surprising: the past-the-Seed counsel a stranger now hears ends "Press further if you want another", but a stranger is not offered Press further at someone else's stone. The line may want a stranger's variant. The Mycelial skill branch still uses the Thurible as its icon; I left it.
- Not seen in a client: the walk home in the Gardens, a reset Apotheos losing the node, Mind Broken after a death, the Thurible refusals.

### Verification (round 1)

Checked the tree against `refs/build2/B23-before`. Pass.

- Scope: only this area's rows. Nothing held or reversed was touched: no lash numbers, no HUD, Silent Severance not renamed. The 12 T11 rows and UN-19 are still in section 3.
- Values: 8 fragments on every shatter (`MonolithFragmentDropRules.FRAGMENTS`). Atlas: the three Monolith items at D7, the four Residua at D8, the Silent Archon chestplate at D8, the three lineage chests at D7, in both `MaterialsData` and `MaterialAtlasSpec`. Thurible: `mayBurn` is `isApotheos`, used by the item, the burn tick, the atlas and `CardinalRiteActivation`; the craft is `required_degree` 8. The node keeps the respawn; the Leave button and the Spine with no return point go to the return point, else the old respawn, else the world spawn, with no blood paid; the old respawn comes back when the node goes. Mind Broken is copied on a death clone. All match the answers.
- Tests: each built row has a test that fails without it. The rewritten tests (fragment count, atlas probe, Thurible Degree in the roster) assert the new rulings and are not weaker. The retired probe is in `removed_assets/` with a MANIFEST row.
- Rules: protocol 54 and `payload-registry.txt` match; the packet uses `ServerboundGuards`, `ServerboundRateLimiter` and one service call; player text is lang keys.
- Unit suite: BUILD SUCCESSFUL, 6,140 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B23-verify-unit.txt`).
- Combined GameTest run: "All 1989 required tests passed :)" (`build/build2/B23-verify-gametest-latest.log`).
- Playthrough: "All 439 required tests passed :)", 21 optional failures. The only name outside the baseline is `apotheosritewithoutrescue` (PT-88, known flake) (`build/build2/B23-verify-playthrough-optional.txt`). `archonRiteWithHelper`, `shatterBeforeCornerstone`, `atlasFragmentsAndResiduum`, `mindBrokenSurvivesADeath` and the three chain tests passed.
- Screen: I looked at `build/build2/B23-review/chrome-b23-heart_node.png`. The Leave the Gardens button sits left of Molt and its label fits. I did not launch a new client.
- Small things, not blocking: the section 4 row "B23 (D7 to D8)" in `OPEN_QUESTIONS.md` says "no shot was taken", but the shot above exists. No test drives the Cardinal Focus refusing the Thurible Working for a D8 Silentium; the path check is covered only through `mayBurn`.

## B24. Silentium greeting trees

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 12 T11 (PT-83) | `HarbingerPathPermissions.greetingDegree` keeps a Silentium at 8; only an eighth Degree that is neither Apotheos nor Silentium still hears the Archon tree. Each entity picks the Silentium tree with `isSilentium`. Vicar: new tree `HarbingerVicarDialogueTrees.silentium` (greeting `vicar.silentium.line1/line2`, lore card "What does the record say about a Silentium?" to `silentium_record`; Annetta's map, the Monolith rumour, recruitment and the blood-script ritual kept; the Archon's `final_truth` is not in it). Alchemist: new tree `HarbingerAlchemistDialogueTrees.silentium` through a new `forDegree(..., isSilentium)` overload, so the held-sample and First Separation lessons still merge in (lore card "What does your reading say about me?" to `reading`). Mnemonist: the woven tree takes a Silentium greeting and a lore card "What have you written about me?" (`silentium_entry`), through a new `forDegree(..., isSilentium)` overload, at Degree 8 only. Artificer: the workshop takes a Silentium greeting and a new option "Tell me about the Silent Archon vestments." (`silent_vestments`), shown only when `ArtificerProgressSnapshot.silentium` is true (CD-10). No new dialogue event. | `HarbingerPathPermissions.java`, `HarbingerVicarDialogueTrees.java`, `HarbingerAlchemistDialogueTrees.java`, `HarbingerMnemonistDialogueTrees.java`, `HarbingerArtificerDialogueTrees.java`, `HarbingerVicarEntity.java`, `HarbingerAlchemistEntity.java`, `HarbingerMnemonistEntity.java`, `en_us.json` |
| 12 T11 Hermit line (UN-19 A) | New `HarbingerHermitDialogueTrees.silentium`: the Archon tree with `hermit.silentium.line2` in place of `hermit.archon.line2` (same stage direction, same "share wisdom" event). New `forState(..., isSilentium, entityId)` overload; the Hermit entity passes `isSilentium`. | `HarbingerHermitDialogueTrees.java`, `HarbingerHermitEntity.java`, `en_us.json` |

### Skipped

| Row or part | Why |
| --- | --- |
| Review client | Text only: no look, screen or shader changed. No client launched. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,145 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B24-unit1.txt`). B23 had 6,140; five added.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1989 required tests passed :)" (`build/build2/B24-gametest1-latest.log`). The dialogue route fingerprints came out identical to the golden (no new event), so none was regenerated.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 439 required tests passed :)", 20 optional failures (`build/build2/B24-playthrough1-optional.txt`). The only name outside the baseline 59 is the known flake `apotheosritewithoutrescue` (PT-88). `silentiumgreetedasarchon` is gone from the list.
- Flipped and rewritten to the ruling: `PlaythroughEndgameGameTests.silentiumGreetedAsArchon` is now `silentiumHasAGreetingOfTheirOwn`, required. For each of the four teachers the Silentium tree must differ from the Archon's and from the Apotheos's, carry `<teacher>.silentium.line2` in its greeting, and have English for every key. It passed for all four.
- Changed on purpose: `HarbingerPathPermissionsTest` now pins `greetingDegree(8, SILENTIUM)` at 8 (was 7) and adds `greetingDegree(8, NONE)` at 7.
- Added: `SilentiumGreetingTreesTest` (five tests): the Vicar's and Alchemist's Silentium trees, and an Apotheos keeps theirs; the Mnemonist's entry at Degree 8 Silentium only, with no question in its lines (their sheet); the Artificer's vestments lesson for a Silentium only, naming Silentium, Blood Lust, Monolithic Armature, Monolith Imbued Cloth and 2,000 mL; the Hermit keeps the Archon choices with one Silentium line; every Silentium line and option has English.
- Changed: `DialogueLoreFlagTest.MIN_REACHED` 71 to 74 (the sweep reaches the three new lore cards), and three lines added to `src/test/resources/dialogue/lore-options.txt`.
- `python gradle/source-text-impact-map.py`: the map did not change.

### Docs

- `docs/ENDGAME_PATHS.md`: status line, PT-83 marked built with what it does, UN-19 A recorded under "Rulings that bind" (2026-11-14), open questions (none left for this doc), proof owed (the probe's new name, the lines the owner has not read), history.
- `docs/NPC_VOICES.md`: a Silentium note on the Hermit, Vicar, Alchemist, Mnemonist and Artificer sheets.
- `docs/NPC_DIALOGUE_AND_TRADE.md`: PT-83 and UN-19 A moved to the built rulings with their trees and option keys; history line.
- `docs/HEMOMANCY_REFERENCE.md`: Silentium rows in the Hermit, Alchemist, Vicar and Mnemonist tables (section 5.4), the Artificer paragraph (the vestments lesson goes to a Silentium only), the Vicar's NPC table row. `docs/LORE_REFERENCE.md`: no lore changed.
- `docs/PLAYTHROUGH_TESTS.md`: the probe renamed and required. `wiki/Harbinger-Path.md`: an "At the outpost" line under Silentium.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 row UN-19 and section 3 rows "12 T11 (PT-83)" and "12 T11 Hermit line (UN-19)"; the two emptied D7 to D8 tables now say nothing is left. Section 4: "PT-83 lines" is now "Now", with the Hermit's line added; PR-5 no longer waits on the greetings.
- `docs/owner_answers/12-endgame-paths.md`: status line T11.

### For the owner

- New lang keys (all new, none changed):
  - `hemomancy.vicar.silentium.line1`: "§4[The Vicar opens the register to the page after Archon. A title is written at the top of it, and nothing under it]"
  - `hemomancy.vicar.silentium.line2`: "Silentium of the Hematic Order. I have entered your name under the title. The oldest accounts gave us that word and very little else, so I will not pretend to know what you are now. Our rites are still yours, and so is this desk."
  - `hemomancy.dialogue.vicar.option.what_does_the_record_say`: "What does the record say about a Silentium?"
  - `hemomancy.vicar.silentium.record.line1`: "§oVery little, and none of it firsthand. §rThe accounts say a Silentium no longer hears the voice the rest of us were taught to listen for, and that the Order never learned to reach one again. Past that the record is silent, and I will not fill it with guesses."
  - `hemomancy.vicar.silentium.record.line2`: "So I would rather ask you. Is it quiet in you now, or is that only how it looks from this side of the desk?"
  - `hemomancy.alchemist.silentium.line1`: "§4[The Alchemist looks up from the bench and studies you for a while before they bow their head]"
  - `hemomancy.alchemist.silentium.line2`: "Silentium. I took a reading off you when you came in, and it's an Archon's reading. Whatever changed, the instruments don't see it. Anything need preparing or repairing?"
  - `hemomancy.dialogue.alchemist.option.what_does_the_reading_say`: "What does your reading say about me?"
  - `hemomancy.alchemist.silentium.reading.line1`: "Pressure and clotting read the way they did when you were Archon, and Vesper's memory sits in your blood where any memory would. I can't find anything missing. If the stories are right and you've stopped hearing something, it isn't in the glass."
  - `hemomancy.alchemist.silentium.reading.line2`: "What the memory means is the Mnemonist's to say, so ask them. I'll keep the instruments in order for you either way."
  - `hemomancy.mnemonist.silentium.line1`: "§4[The Mnemonist turns to a fresh page and writes one word at the top of it]"
  - `hemomancy.mnemonist.silentium.line2`: "One word, and I'm keeping the entry under it short until I know what belongs there. The archive's still open to you."
  - `hemomancy.dialogue.mnemonist.option.ask_about_silentium_entry`: "What have you written about me?"
  - `hemomancy.mnemonist.silentium.entry.line1`: "That you refused, that Vesper fell, and that his memory went into you through the Staff. I saw proof of each, so each is written."
  - `hemomancy.mnemonist.silentium.entry.line2`: "I listen to your blood the way I listen to anyone's, and every memory I indexed is still there. The old accounts say a Silentium stops hearing something the rest of us hear. I can't hear what's gone, so I haven't written it down."
  - `hemomancy.artificer.silentium.line1`: "Silentium, the stories call it. I've no frame for that, so I'll keep fitting the one you've got."
  - `hemomancy.artificer.silentium.line2`: "There's one new thing I can make you now. What do you need?"
  - `hemomancy.dialogue.artificer.option.silent_vestments`: "Tell me about the Silent Archon vestments."
  - `hemomancy.artificer.silent_vestments.line1`: "They're cut for a Silentium and nobody else. Bring a Blood Lust piece to a Monolithic Armature. Any lineage takes it, Edacious, Sheolic and Phantasmal too."
  - `hemomancy.artificer.silent_vestments.line2`: "One Monolith Imbued Cloth and 2,000 mL of blood for each piece. Helm, chest, legs and boots, one at a time."
  - `hemomancy.artificer.silent_vestments.line3`: "If it's your first late upgrade, I write it down as your lineage, same as any other. The set bonus needs all four pieces on."
  - `hemomancy.hermit.silentium.line2`: "Silentium of the Hematic Order. We were given that name and nothing under it. I can hear how far your blood has come, and I have no word for it past the name. I have a long memory and a heart nobody has come for. Ask for the memory and it is yours."
- Calls I made: the Vicar and Alchemist get whole new trees. The Mnemonist and Artificer keep their one tree with a new greeting and one new card, because their trees are not split by Degree. The Vicar's Silentium tree drops the Archon's "final truth" saying, since the player is past it. The Hermit keeps his stage direction and his "Ask, Archon" prompt; only his second greeting line changed, as UN-19 A asked for one line. The Vicar, Alchemist and Mnemonist cards are lore cards; the Artificer's vestments card is not.
- Worth a look: each teacher's Silentium ending line (`dialogue.ending.<teacher>.silent_archon`) still follows the greeting, so the Vicar, Alchemist and Artificer say "Silentium" twice in a row. The Mnemonist's greeting avoids the word for that reason. Say if the ending lines should drop it.
- An active Qliphoth Pome empowerment still replaces the Vicar's Silentium greeting with the pome lines, as it does for any greeting at the seventh Degree and up.
- Not mine to touch: the Vicar row under "NPC dialogue" in `OPEN_QUESTIONS.md` section 3 still lists "the Silentium greeting tree (PT-83, a whole tree)" as ready. It is built now; that area's package can shorten its row.
- Not seen in a client: none of the new lines. Read them in the dialogue box for length.

### Verification (round 1)
- Checked the tree against `refs/build2/B24-before`. Only B24 files changed, plus the new `SilentiumGreetingTreesTest`. Nothing held or reversed was touched. No lash number, HUD, payload, blood cost, particle or delivery code changed.
- Values: `greetingDegree(8, SILENTIUM)` is now 8. Each of the four teachers has a Silentium greeting that differs from both the Archon and the Apotheos tree. The Hermit keeps the Archon tree with one new line (UN-19 A). The Artificer's vestments lesson matches the four `blood_lust_to_silent_archon_*` recipes: any Blood Lust lineage, one Monolith Imbued Cloth, 2,000 mL a piece, Degree 8.
- Tests: the probe was rewritten to the ruling and made required. The old pin in `HarbingerPathPermissionsTest` was flipped to the ruling. The new unit test fails without the trees. Nothing was weakened or skipped.
- Unit suite: `BUILD SUCCESSFUL`, 6,145 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B24-verify-unit.txt`).
- Combined GameTest run: "All 1989 required tests passed :)". The dialogue route fingerprints match `src/gameTest/resources/dialogue/route-fingerprints.txt` exactly (`build/build2/B24-verify-gametest.txt`).
- Playthrough: "All 439 required tests passed :)", 20 optional failures. The only one outside the baseline is `apotheosritewithoutrescue` (PT-88, known flake). `silentiumhasagreetingoftheirown` passed for all four teachers (`build/build2/B24-verify-playthrough-optional.txt`).
- Docs match the tree. Only the D7 to D8 rows were removed from `OPEN_QUESTIONS.md`. Left for the NPC dialogue package: the Vicar row in section 3 still lists PT-83 as ready.
- No client launched: text only.

## B25. Rites and Working crafts

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| NQ-13-1 (CB-12) | Answer recorded: the boundary drain stays 25 mL a tick with 1 damage a tick. No code changed. | `docs/CARDINAL_RITES.md` |
| 13 T13, NQ-13-2 (F-12) | `IBloodReservoir.takeProjectedBlood` returns 0 unless `canReceiveBlood()`, so a rite-locked station takes no hand Projection and charges nothing. The Resonant Forge's own override (hammer repair) now checks `riteLocked` first. The Loom already refused (`refuseWhileRiteHeld`). The Monolith, Ring Door and Mason's Effigy have no rite lock. The Condenser's Projection path (`SanguineCondenserProjection`) runs earlier for every server player and never reaches the default. | `IBloodReservoir.java`, `ResonantForgeBlockEntity.java` |
| NQ-13-3 (CN-2) | Kept as built: running rites only, plain Projection only. Recorded as a ruling. | `docs/CARDINAL_RITES.md` |
| 13 T11, NQ-13-4 (FX-12) | Hungering Earth's map icon is the Sanguine Formation and Possession's is Adipocere. New GameTest: no two Harbinger rites share a map icon, and every rite has one. | `HarbingerRecipeMapDefinitions.java`, `RiteKindCharacterisationGameTests.java` |
| 13 T12, NQ-13-5 (DS-4) | One Liber line for each of the 19 Harbinger rites with no entry, and a `registerRite` row each, so completing the rite opens the page. Homes: the seven Puppeteer ordeals on their `troupe_*` pages (those pages now also list RITE as a source); the three vessel rites on a new page `the_hematic_order/pages/blood_gourds` ("Blood Gourds", ordinality 23); Bloodline Recall, Covenant Vigil and Eternal Covenant on The Historical Record; Hematic Succession and Mnemonic Restoration on Mnemonic Ambergris; the two Armature rites on Bloodwork Stations; Possession on The Source (`the_entity`); Visitation on The True Purpose (`the_truth`). Succession rites did not call the Liber on completion; `SuccessionRites` now does. Station upgrade rites open their page on eligibility, as the other upgrade lessons do. | `LiberEntryDefinitions.java`, `SuccessionRites.java`, eleven page files under `books/libersanguinium/` |
| FX-11 tints (13 T10) | `RecipeMapPurposeGroup`: Floorless is bone `0xFFC8BC9C` (was `0xFFC0303A`, close to World's `0xFFE0303C`), Making is hematic iron `0xFF8C9DB8` (was the Working floor glow `0xFFFF4F6E`). A group with its own tint uses it; the others keep their floor glow. | `RecipeMapPurposeGroup.java` |
| 13 T10 (FX-11), editor | The local skill-tree editor shows one merged "Rites & Crafts" map in place of the two tabs. Entries are shared with their tabs, so moves write back by tab. Family, link and new-family edits go to the entry's own tab. The parser returns the purpose-grouped family order. Git-ignored tool. | `tools/skill_tree_editor/src/frontend/recipeMapMerge.ts` (new), `recipeMaps.ts`, `src/server/recipeMapParser.ts`, `recipeMapWorkspace.ts`, `src/shared/types.ts` |
| 13 T3, PT-32 (rest) | A second pass in `CardinalRiteActivation`: when a Focus floor matches no rite, a Harbinger rank rite the player has already passed (Degree at or above its target) that matches the floor gives "You have already attained". The line's code is now one helper shared with the old path. | `CardinalRiteActivation.java` |
| 13 T6, UN-48, PT-88 | Diagnosis: a harness bug. `PlaythroughClock.anchorAim` aimed at dry, undamaged anchors first, then at the first anchor needing anything by index. With anchor 21 dry and damaged (108 mL short) and anchors 20 and 22 full but damaged, it aimed at 20 forever. Anchor 21 stayed dry, its deficit kept re-damaging the ring, and the repair never stayed done. In runs where the dry anchor had the lower index the probe passed. A player sees the dry anchor, so the game was fine. The aim is now dry undamaged, then dry damaged, then repair. The deadlock wrapper `stabilizeWhenDeadlocked` and its ARRANGE rescue were removed (moved to `removed_assets/`), from `apotheosRiteByPlay`, the Bloom rite and the late run. `apotheosRiteWithoutRescue` is required. | `PlaythroughClock.java`, `PlaythroughRites.java`, `PlaythroughEndgameGameTests.java`, `PlaythroughArchonGameTests.java` |
| 13 T4 (PT-67) | Proof on a world with the Chamber loaded: new review client `chamberRiteReviewClient` (`ChamberRiteClientReview`). With a Degree 6 caster it checks the Chamber is loaded, the finale check passes, the rite's effect (`beginRiteVisit`) starts a visit, the caster is attuned, the visit is active in the Chamber, a second visit is refused, and the return comes home. All 8 checks passed. | `ChamberRiteClientReview.java` (new), `gradle/review-clients.gradle` |
| 13 T8 (FX-13) | Nothing left to fix. B02 had rewritten the failing test (`effigyPatternIsAWorkingCraftNotHandFed`). The opt-in suite passes 22 of 22. | none |
| 13 RV-22 (drift) | `mind_spike` removed from `HarbingerRecipeMapDefinitions.WORKING_CRAFTS`. The test comment now says 18 Working crafts. | `HarbingerRecipeMapDefinitions.java`, `HarbingerRecipeMapDefinitionsTest.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| 13 T14 (D-21, CB-15) | Held: the cost pass follows a play session. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: first run 2 failures, both mine (a looped `registerRite` call the resource test reads as a path, and the new page constant inside the core-loop block); fixed. Then BUILD SUCCESSFUL, 6,147 tests, 0 failures (`build/build2/B25-unit2.txt`). B24 had 6,145.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1990 required tests passed :)" (`build/build2/B25-gametest1-latest.log`). B24 had 1,989.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 439 required tests passed :)", 19 optional failures, all in the baseline list. `apotheosritewithoutrescue` passed and is gone from the optional list (`build/build2/B25-playthrough1-optional.txt`).
- `./gradlew.bat runSuccessionGameTestServer --offline --console=plain`: "All 22 required tests passed :)".
- Skill-tree editor: `npx tsc --noEmit` clean; `npx vitest run`: the new merge tests and the parser tests pass. Five tests still fail that read the materials atlas and the skill layout from the live Java; they failed before this package and are outside this area.
- Flipped on purpose: `BloodAccountingGameTests.riteLockedStationStillTakesProjectedBloodUntilF12` is now `riteLockedStationRefusesProjectedBlood`. The locked Centrifuge and a locked Forge take 0 and the player keeps 1,000 mL; unlocked, the Centrifuge takes 100 mL again (NQ-13-2).
- Tightened on purpose: `PlaythroughIlluminatusGameTests.sanctifiedSealedBeforeCovenant` and `PlaythroughSanctifiedGameTests.sanctifiedRiteByPlay` now require the "already attained" line at D6 instead of accepting the floor line (PT-32).
- Made required: `PlaythroughEndgameGameTests.apotheosRiteWithoutRescue` (PT-88). Rescues removed from `apotheosRiteByPlay`, the Bloom rite in `PlaythroughArchonGameTests` and the late run.
- Added: `RiteKindCharacterisationGameTests.noTwoRitesShareAMapIcon` (FX-12). `RecipeMapPurposeGroupDataTest.everyPurposeGroupReadsApartFromTheOthers`: every pair of group tints at least 48 apart in RGB (failed on the old Floorless and World pair). `HarbingerRiteLiberRowDataTest`: every Harbinger rite file opens a Liber page (failed on the 19 before). Editor `recipeMapMerge.test.ts` (3 tests).
- Changed: the editor's `recipeMapParser.test.ts` rite count 52 to 56 (it was already stale).
- `python gradle/source-text-impact-map.py`: regenerated.
- Final check after the docs: unit suite BUILD SUCCESSFUL, 6,147 tests, 0 failures (`B25-unit3.txt`); combined "All 1990 required tests passed :)" (`B25-gametest2-latest.log`); playthrough "All 439 required tests passed :)", 19 optional, all in the baseline list, and `apotheosRiteWithoutRescue` passed again (`B25-playthrough2-optional.txt`). Two clean passes of the PT-88 probe in two runs.

### Docs

- `docs/CARDINAL_RITES.md`: status line; rulings updated for PT-32, PT-88, FX-11, FX-12, DS-4, F-12, RV-22; new rulings NQ-13-1 to NQ-13-5 and UN-48 (2026-11-14/15); calls made for PT-32, PT-88, FX-11, DS-4, PT-67; the planned-defaults list removed; open questions pointer; proof owed cut to the cost pass; history row.
- `docs/HEMOMANCY_REFERENCE.md`: section 5.3 activation (the "already attained" line), the station lessons paragraph (rite lock stops hand Projection), the Rites & Crafts map entry (tints, icon check, the Liber homes).
- `docs/STATION_UPGRADES.md`: the F-12 gap paragraph is now the built rule; NQ-13-2 left the planned-defaults line.
- `docs/architecture/CONSOLIDATION_PROGRESS.md`: F-12 to DONE.
- `docs/PLAYTHROUGH_TESTS.md`: PT-32 and PT-88 lines; the UN-48 note and the old PT-88 gap removed.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 row NQ-13-1; section 2 rows NQ-13-2, NQ-13-3, NQ-13-4, NQ-13-5, FX-11 (tints), UN-48/PT-88 and PT-32 (rest); section 3 rows 13 T10 (both), T4, T8, RV-22, T13, T6, T12, T11, T3. 13 T14 stays. Section 4 rows PT-88, FX-11 (map), FX-12, PT-67 and DS-4 now say "Now" with the screenshot paths.
- `docs/owner_answers/13-rites-and-crafts.md`: status lines T3, T4, T6, T8, T10, T11, T12, T13.
- `docs/LORE_REFERENCE.md`: no change. The new lines restate built rites.

### For the owner

- No lang keys changed. The Liber pages are English in their JSON, as every page is. The new and changed lines (each appended to its page):
  - `troupe_veinwing_vulture`, `troupe_marrow_spitter`, `troupe_sanguine_hound`, `troupe_cinder_bellows`, `troupe_scarlet_mummer`, `troupe_gorebound_hulk`, `troupe_mnemonist_puppet`: "Beat the <Vulture / Juggler / Hound / Bellows / Mummer / Hulk / Puppet> in its ordeal and the Crossbar seated in the Focus learns its shape."
  - New page "Blood Gourds" (subtitle "Vessels raised by rite"): "At Degree 3 the Rite of the Pallid Vessel wakes a Dried Gourd on a Lesser Working floor into a White Blood Gourd that holds its maker's blood. At Degree 4 the Rite of the Crimson Vessel steeps the white gourd with an amethyst shard on a Greater Working floor until it flushes red. At Degree 5 the Rite of the Ashen Vessel takes the red gourd and a ghast tear through fire, and the black gourd that comes out holds the most blood of the three."
  - The Historical Record: "A line does not end with its book. The Rite of Bloodline Recall calls an Unsigned Ancestral Ledger back out of a Harbinger's blood memory. The Brotherhood's old watch lives on in the Rite of the Covenant Vigil, where a sworn helper stands the whole Grand ordeal inside the caster's boundary and every survivor keeps ten minutes of resistance and regeneration. The Rite of the Eternal Covenant can be sworn only once, and it raises for good the most blood the caster's body can hold."
  - Mnemonic Ambergris: "The Order spends ambergris on its dead as well. In the Rite of Hematic Succession a donated vial, the caster's own vial and Mnemonic Ambergris raise a new resident for a Fane in a Vacant Effigy. The Rite of Mnemonic Restoration brings a lost resident back into the effigy from a Bound Mnemonic Remnant."
  - Bloodwork Stations: "At Degree 5 the Rite of Armature Consecration seats the Armature Consecration Kit in a Hematic Armature and closes two blood circuits. A consecrated Armature takes the armour recipes of Degrees 5 and 6. At Degree 7 the Rite of the Monolithic Armature sets a Monolithic Cornerstone into a consecrated Armature and binds three circuits. It then takes the armour recipes of Degree 7 and up."
  - The Source: "A Silentium no longer hears the hive, but the Rite of Possession lets one force their will onto a piece of the organism and walk the Gardens in a borrowed shape while an echo waits behind."
  - The True Purpose: "An Apotheos can go home in the flesh. The Rite of Visitation burns two spore sacs and carries the body into the Gardens, onto its heart-node if one beats there."
- Values I chose: the tints, bone `0xFFC8BC9C` (Floorless) and hematic iron `0xFF8C9DB8` (Making). The page homes above, with Possession on The Source and Visitation on The True Purpose (the plan had them the other way round; the organism page fits possession and the fruiting-body page fits going home). The puppet lines leave out blood costs on purpose, so the cost pass (T14) does not make them stale.
- Look at: the map, `build/build2/B25-shots/liber-rites-and-crafts.png`. The Chamber visit, `build/build2/B25-shots/chamber-rite-visit.png`. Both clients were closed.
- Surprising: the Rite of Hematic Succession and Mnemonic Restoration never opened any Liber page on completion; `SuccessionRites` now calls the same unlock as other rites. The Armature pages planned in chunk 16 (DS-9) are separate; when they land, the two Armature rows can point there.
- PT-88 was not a design problem, so no lever comes back as a question. Watch the next few playthrough runs: the probe failed in most runs before.

### Verification (round 1)

Checked the tree against `refs/build2/B25-before` and `B25-status-before.txt`. Pass.

- Scope: only the area's rows. 13 T14 is not built. No lash numbers, HUD, strand kits or Silent Severance touched. The skill-tree editor files are in the git-ignored tool.
- Values: the Centrifuge and Forge refuse Projection while rite-locked and charge nothing (NQ-13-2). Hungering Earth shows the Sanguine Formation and Possession shows Adipocere (NQ-13-4). The 19 `registerRite` rows match the 19 rites named (NQ-13-5). Tints are bone `0xFFC8BC9C` and hematic iron `0xFF8C9DB8`. `mind_spike` left the crafts set. The boundary drain is unchanged (NQ-13-1). The new Liber lines match the rite files (Degrees, floors, items, Silentium for Possession, Apotheos for Visitation).
- Tests: the F-12 test is flipped on purpose. The PT-32 playthrough asserts are stricter. `apotheosRiteWithoutRescue` is now required. New: `noTwoRitesShareAMapIcon`, `everyPurposeGroupReadsApartFromTheOthers`, `HarbingerRiteLiberRowDataTest`. Nothing was weakened. The rescue helpers are in `removed_assets/`.
- Runs (all mine, 2026-11-15): unit suite BUILD SUCCESSFUL, 6,147 tests, 0 failures (`build/build2/B25-verify-unit.txt`). Combined run: "All 1990 required tests passed :)" (`B25-verify-gametest.txt`). `rite_kind_validation`: 38 of 38. `succession_validation`: 22 of 22. Playthrough: "All 439 required tests passed :)", 19 optional failures, none outside the baseline list (`B25-verify-playthrough-optional.txt`). `apotheosRiteWithoutRescue` reached Degree 8 with no rescue.
- Minor, not blocking: no test pins the new `SuccessionRites` Liber unlock on completion. I did not relaunch a client; the builder's map screenshot shows the new tints.

## B26. The Brined Court

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 14 T1 (PT-46 gap) | `KindledChampionTrial.facing(player)` now looks for the Champion facing the player in every loaded level, at any distance (it searched a box of 64 blocks round the player). The logout, death and "already under way" checks all use it. A challenger who swims 70 blocks off and logs out inside the 100-tick grace now ends the fight at once, is charged the day and hears `hemomancy.court.champion.forfeit`. | `KindledChampionTrial.java` |
| 14 T7a (PT-51) | The 17 lesson lines say "wear", not "feed": the Lightkeeper's `lesson.line1`, `lesson.line2`, `lesson.known` and Sunburst `line2`, the system line `hemomancy.court.lesson.taught`, and the 12 house strain inquiries (Tidewarden `line1` and `line2`). The fact they carry: worn where its own house lives, a strain bonds faster. The BC-7 note is now an assertion. | `en_us.json`, `dialogue_briefs/court.json` |
| 14 T7b (PT-50) | 26 directions lines rewritten (the Nursery tender's `line1` and the Offering-bearer's `line2` name no house and stay). Wherever a line names a house it names the site: Lighthouse Garden, Tide Refuge, the Wake nursery, Line Camp, Screened Cleft, the Still Bell's bell, Whale Fall, Vent Sanctum. The Gardener, Arbor and Lightkeeper name the Vent Sanctum. `courtPointersPastTheGarden` is required. | `en_us.json`, `court.json` |
| 14 T7c (PT-49, NQ-14-7) | The nursery pointer is in the Gardener's and Lightkeeper's `directions.line1` and the Edge-swimmer's `directions.line2`: open water over the twilight slope, the school's loop does not lead there, ask the Nursery tender. Spoken lines only. | `en_us.json` |
| 14 T7d (PT-54, late) | `CourtMemberDialogue.View` gained `late` (`ProgressionGates.hasDegree(player, LATE_DEGREE)`, `LATE_DEGREE = 6`). A welcome player at D6 or above gets a `degree.late` line in the greeting at the Gardener, Lightkeeper and Head-tender. | `CourtMemberDialogue.java` |
| 14 T7e (PT-54, Primal; NQ-14-6 A) | The View gained `Ripeness` (YOUNG, APEX, PRIMAL from the worn strain's maturity). A Primal strain swaps the own or other strain line for `strain.primal` at nine roles (Gardener, Lightkeeper, Head-tender, Braced elder, Nursery tender, Lamp-colony, Veiled resident, Bellwarden, Rooted elder). With an Apex or Primal strain the Lightkeeper and the Head-tender offer "How does a strain become Primal?" leading to `primal.line1/2`: a lifetime in home water for the Court; Morphic Nectar after the eighth Degree known only by report. A 10-argument record with an 8-argument constructor that defaults to not late and YOUNG. | `CourtMemberDialogue.java`, `en_us.json` |
| 14 T7f (PT-48 follow-up) | `head_tender.challenge.line1` reads as the answer once the Champion is already called: "Then it is asked, petitioner, and the Champion rises from among the elders to answer you. It is an honor to ask for the title, and a greater one to be refused by strength." | `en_us.json` |
| 14 T10b (FX-19 flush, FX-19a) | `MorphlingStrainModel` draws a second emissive pass over the Mermaid's Purse sail while `SHARED_WAKE` is on: alpha 0.55, swelling by 0.3 on a sine (0.12 rad a tick), faded in and out with the bit's settle. It shows on a schooling Edge-swimmer and on a player holding the Wake open. `courtNpcReviewClient` has three new close shots of the live school (one day, two night). | `MorphlingStrainModel.java`, `CourtNpcClientReview.java` |
| 14 T12 (in part) | One full `alphaCheck`, green (not run since 2026-10-30). `courtNpcReviewClient` shot, 199 captures. | none |
| Lore answers NQ-14-2 to NQ-14-5 | Recorded in `LORE_REFERENCE.md` §6.3c and as rulings in `BRINED_COURT.md`. The new Primal lines draw on NQ-14-6 only. | docs |

### Skipped

| Row or part | Why |
| --- | --- |
| 14 T12, the rest | Its row waits on 14 T9 and 14 T10a, which are a later package. The shots from `morphlingStrainReviewClient`, `brinedSeatReviewClient`, `brinedOutpostReviewClient` and `courtPresenceReviewClient` would be taken again after the strain look fixes. No Court harness captures the tooltip, Bestiary dossier or Liber text, so that needs new harness scenes. The row is shortened, not deleted. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,151 tests, 0 failures (`build/build2/B26-unit1.txt`; again after the docs, `B26-unit2.txt`). B25 had 6,147.
- `./gradlew.bat runGameTestServer`: first run 1 required failure, `everyDialogueRouteKeepsItsOutcome` (mine: the `court_member_lesson` row says the reworded `lesson.taught`). I replaced that one sentence in `route-fingerprints.txt`; the file then matched the actual output line for line.
- `./gradlew.bat alphaCheck --offline --console=plain`: BUILD SUCCESSFUL. "All 1990 required tests passed :)" (`build/build2/B26-alphacheck.txt`, `B26-alphacheck-gametest-latest.log`).
- `./gradlew.bat runPlaythroughGameTestServer`: "All 440 required tests passed :)", 18 optional failures, all in the baseline list; `courtpointerspastthegarden` left it (`build/build2/B26-playthrough1-optional.txt`).
- `runCourtNpcReviewClient`: three runs (the first two framings missed the school); the third captured 199 shots and closed itself. No game process left running.
- Added: `CourtMemberDialogueTest.theLateDegreesAddAGreetingLineAtThreeSpeakers`, `aPrimalStrainIsAnsweredAndTheLeadsSpeakOfTheStep`, `directionsNameEachHousesSite` (each house named has its site), `theLessonIsAboutWearingAStrainNotFeedingIt`; `everyLineAndOptionHasEnglish` now also sweeps late views at every ripeness. Playthrough `aFarChallengerWhoLogsOutStillForfeits` (required): the PT-46 gap and the forfeit line.
- Flipped on purpose: `courtPointersPastTheGarden` is required and asserts the Vent Sanctum, the nursery and the Gardener's `degree.late` and `strain.primal` at D8 (PT-49, PT-50, PT-54). `lightkeeperLessonAndBonding` asserts the lesson says "Wear" and never "feed"/"fed" (PT-51). `route-fingerprints.txt`: one sentence, as above.

### Docs

- `docs/BRINED_COURT.md`: status line, Court life and people paragraphs, rulings PT-49, PT-50, PT-51, PT-54, FX-19 marked built, new rulings NQ-14-2 to NQ-14-7 and the PT-46 gap, two calls (the reply line, the flush values, the late and Primal rules), open questions pointer, proof owed.
- `docs/LORE_REFERENCE.md` §6.3c: petrification as a chosen witnessed step (NQ-14-2), rooted elders choose (NQ-14-3), customs between houses (NQ-14-4, NQ-14-5), the Primal step (NQ-14-6).
- `docs/HEMOMANCY_REFERENCE.md` §29: the lesson says wear, the logout search, the school flush, the late and Primal lines and the site names.
- `docs/NPC_VOICES.md`: four shared Court rules for the line pass; the Lightkeeper's words, habit and example; "nodes added" notes on ten sheets.
- `docs/PLAYTHROUGH_TESTS.md`: D-46, D-49, D-50, D-51, D-53, D-54 and the Court probe list. `wiki/Brined-Court.md`: lesson, Champion leaving, directions, late and Primal lines.
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-14-2 to NQ-14-6 (and the empty Brined Court table), section 2 rows NQ-14-7 and FX-19a, section 3 rows 14 T7a to T7f, 14 T1 and 14 T10b. Shortened 14 T12 and the "Brined Court roles" line-work row. Section 4: "T7 lines" and "FX-19 flush" are now "Now", and the PT-46 row says the 64-block gap is closed.
- `docs/owner_answers/14-brined-court.md`: status lines for T1, T7, T10 and T12.

### For the owner

- New keys (17): `hemomancy.court.gardener.degree.late`, `hemomancy.court.lightkeeper.degree.late`, `hemomancy.court.head_tender.degree.late`; `hemomancy.court.<role>.strain.primal` for gardener, lightkeeper, head_tender, braced_elder, nursery_tender, lamp_colony, veiled_resident, bellwarden, rooted_elder; `hemomancy.dialogue.court.option.primal`; `hemomancy.court.lightkeeper.primal.line1/2`, `hemomancy.court.head_tender.primal.line1/2`.
- Changed keys (44): `hemomancy.court.<role>.directions.line1` and `.line2` for gardener, arbor, lightkeeper, tidewarden, braced_elder, edge_swimmer, linesman, lamp_colony, host, bellwarden, marrow_tender, head_tender, plus `nursery_tender.directions.line2` and `offering_bearer.directions.line1`; the 17 lesson lines listed under T7a; `head_tender.challenge.line1`. Each has its brief in `dialogue_briefs/court.json`.
- Values I chose: the late greeting starts at the sixth Degree; the Primal topic shows from Apex up; the flush is alpha 0.55 plus 0.3 on a slow sine. The site of the Still Bell is spoken as "its bell", since "the Bell" next to "the Still Bell" read badly.
- Sample lines: Lightkeeper `primal.line2`, "Your Order has another road on land, I'm told: Morphic Nectar, after the eighth Degree. I only know it from what travellers say, so ask them, not me. Do you see why I can't teach it?" Veiled resident `strain.primal`, "Primal. Mine took a lifetime. Yours?"
- Surprise: the Lightkeeper's `strain.own` and `strain.other` still say a strain "will grow faster in this water". They never said "fed", so T7a did not list them; say if they should read "bonds" too.
- Look to judge (FX-19a): `build/court-npc-review/screenshots/school/08-close_day.png`, `09-close_night.png` and `10-close_night.png` (the red sail flush on the schooling swimmers at night), against a lone swimmer at `build/court-npc-review/screenshots/role_edge_swimmer/04-night_glow.png`.

### Verification (round 1)

- Scope: the diff against `refs/build2/B26-before` touches only the files listed above. Nothing held or reversed is touched: no strand kits, no lash numbers, no HUD, no Silent Severance rename, no 14 T9, T10a or the two "14 extra" rows. No payload, blood, PendingDelivery or particle change.
- Values: the Primal lines say a lifetime in home water and the Nectar after the eighth Degree only by report (NQ-14-6 A). The late greeting is D6 at the three speakers, `strain.primal` at nine roles, the Primal topic at the Lightkeeper and Head-tender from Apex up, as the plan says. The four lore answers are in `LORE_REFERENCE.md` §6.3c and `BRINED_COURT.md`.
- Tests: the new unit tests and `aFarChallengerWhoLogsOutStillForfeits` would fail without the change (the challenger stands 70 blocks off, past the old 64-block box). `courtPointersPastTheGarden` is now required and asserts more. The one fingerprint line changed is the reworded `lesson.taught` sentence. Nothing was weakened.
- Unit suite: BUILD SUCCESSFUL, 6,151 tests, 0 failures (`build/build2/B26-verify-unit.txt`).
- Combined run (includes the four Court suites, `dialogue_route_validation` and `npc_trade_validation`): "All 1990 required tests passed :)" (`build/build2/B26-verify-gametest.txt`).
- Playthrough: "All 440 required tests passed :)", 18 optional failures, every one in the baseline list. The far-logout probe logged outcome GONE, a wait of 24,000 ticks and `champion.forfeit`.
- Look: I did not relaunch the client. The shots in `build/court-npc-review/screenshots/school/08-10` exist. In `09-close_night.png` the flush is small and faint against the dark water; the owner judges it.
- Not blocking: the Lightkeeper's `strain.own` still says a strain "will grow faster in this water", which reads against the PT-51 fact (it bonds faster). The implementer flagged it for the owner.
- Result: pass, tree green.

## B27. Crimson Troupe: the pact gate, small fixes, the acts and the Pattern

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 15 T3 (D-30, D-31), NQ-15-5, NQ-15-6 B | One pure rule, `CircusRouteRules.pactGate(route, D4, any report, Understudy report, Unstained)`. A Harbinger needs Degree 4 (`PACT_DEGREE`) and any one of the three school reports. A player whose Unstained state blocks Harbinger progress gets Liberation only, on The Missing Understudy report and no Degree; Succession, a repair into Succession and a Succession finale are refused. Checked at accept, reject, repair and again at the finale, so forged events are refused. The pact options show only when the gate is met; otherwise the greeting is the gate line. First talks keep 50 points. | `CircusRouteRules.java`, `CircusRingmasterEntity.java` (`pactGate`, `refuseGate`), `CircusSchoolQuests.java` (`hasAnyReport`, `hasUnderstudyReport`) |
| 15 T18 (HN-9) | `DialogueTheme.CIRCUS`, added last (the tree sends the ordinal). Palette: speaker 0xEADBBE, text 0xCDBFAA, option 0xE2565F, frame accent bone 0x9A7C5E, striped canvas centre (3-pixel bands 0x1A0A0D and 0x230D12). Sprites `dialogue/circus/*` from `tools/oneoff/dialogue_flat_sprites.py` (circus added; every older sprite came out byte-identical). One troupe crest `dialogue/crests/circus.png` (a bone-and-crimson big top on the shared shield, `tools/oneoff/circus_crest.py`), shown for every speaker on the Circus theme through `DialogueThemeStyle.crest(theme, styleId)`. The Ringmaster's and the school's trees use the theme. | `DialogueTheme.java`, `DialogueThemeStyle.java`, `DialogueScreen.java`, `CircusSchoolDialogue.java`, `CircusRingmasterEntity.java`, `textures/gui/sprites/dialogue/circus/`, `crests/circus.png` |
| 15 T6 tests (FX-26, FX-28) | GameTest `needlesAndKnivesLeaveNoStuckArrow` (a troupe knife and a plain blood needle leave arrow count 0). JVM test `FollowSlotTest` over a new pure `BoundSummonBehavior.slotOffset` (2.5 back, 1.6 apart, Hulk 4.5 back, turns with the owner). | `CrimsonTroupePracticalGameTests.java`, `FollowSlotTest.java`, `BoundSummonBehavior.java` |
| 15 T6 cost (FX-28) | `followOwnerOnFoot` now works out the slot (and its 24-block query) only on the 10-tick beat, standing still or not. | `BoundSummonBehavior.java` |
| 15 T2 order (PT-59) | `CircusPracticalController.begin` checks the lesson's Degree before the Peaceful refusal. | `CircusPracticalController.java` |
| 15 T5 line (D-34) | The bystander line is told once per act, however many troupe members are struck: one session map (bystander to act holder) in `CircusPavilionStateRules`, reset when the holder begins a new act and cleared by the transient-state hub. The three per-entity sets are gone. | `CircusPavilionStateRules.java`, `CircusPavilionSavedData.java`, `CircusPerformerEntity.java`, `CircusCarouselEntity.java`, `CircusRingmasterEntity.java`, `TransientServerStateEvents.java` |
| 15 T4 lines (PT-58) | The Waybill's `attuned` and `not_found` lines take the sought target (`%s`): "The waybill's threads catch on %s, far off." and "The threads reach for %s and find nothing in range." `CovenantWaybillItem.record` gained an overload with the sought name; `CircusWaybillItem.seeking(tag)` names it. | `CovenantWaybillItem.java`, `CircusWaybillItem.java`, `en_us.json` |
| FX-26 (wider) | `BloodNeedleEntity.onHitEntity` takes back the arrow the base class adds, so every blood needle (and the troupe knife, which no longer does it itself) leaves no stuck arrow. | `BloodNeedleEntity.java`, `CircusKnifeProjectileEntity.java` |
| 15 T16 (D-37, D-39), NQ-15-7 A, NQ-15-8 A | Seven acts, one per taught body, in `PuppetAct` (thread / cooldown): Spotlight 8 / 15 s, Trapeze 10 / 20 s, Feint 6 / 12 s, Smoke 8 / 20 s, Encore 10 / 15 s, Scent 6 / 30 s, Lift 8 / 15 s. Reaches in `PuppetActRules` (Spotlight 12, Trapeze 24, Feint 10, Smoke 6 with 3 s invisibility, Scent 24 with 10 s glowing, Lift 6 with 1.1 forward and 0.9 up, 2 s slow fall after Trapeze and Lift). An Act entry on the Crossbar radial's inner ring sends `PacketPuppetAct(crossbarId)` (guarded by `ServerboundGuards.activePlayer`, rate-limited 5 ticks); `PuppetActs.fire` picks the selected shape's nearest body, spends thread only if the act happened, and keeps per-player cooldowns in a session map cleared by the hub. Visuals are the troupe's own (ring footprint, vault columns, thread tendrils, dark glow, glow motes); no vanilla particles. Mummer and Mnemonist expose `spotlight` and `replayNow`. Juggler: the third dagger of each volley (`MarrowJugglerRules.pinsTarget`, tick 20) pins 30 ticks through `MorphlingLock.pin` (boss cap applies); no Blood Loss. Role "Ranged Control". `PROTOCOL_VERSION` 54 to 55, `payload-registry.txt` rewritten. | `PuppetAct.java`, `PuppetActRules.java`, `PuppetActs.java`, `PacketPuppetAct.java`, `PacketHandler.java`, `CrossbarRadialScreen.java`, `ScarletMummerEntity.java`, `MnemonistPuppetEntity.java`, `MarrowSpitterEntity.java`, `MarrowJugglerRules.java`, `CircusKnifeProjectileEntity.java` |
| 15 T17 (D-38), NQ-15-9 A | Ring (30 thread, 60 s): every owned body sharing the owner session within 32 blocks of the Pattern performs its act at once, without its own cost or cooldown; a thread tendril runs from the Pattern to each body that performs. The Pattern still costs no cap slot (the existing +1 cap offset, already tested). It leaves the transposition ring: the radial no longer lists it and `hotSwap` refuses it (`PuppeteerSummonRules.inTranspositionRing`). Lore and Spindle tooltip updated. | `PuppetActs.java`, `PuppeteerSummonRules.java`, `PuppeteerCrossbarCommands.java`, `CrossbarRadialScreen.java`, `en_us.json` |
| 15 T5 (D-35, CB-5) | Thread Ripper at 300 mL was already in the registry (B04). This package recorded it in `docs/CIRCUS.md`. | `docs/CIRCUS.md` |
| Audit E leftovers | The Acrobat tucks both knees while airborne (thighs -1.15 and -1.05, calves 1.75), run after the downed pose. The carousel's horse particles are crimson glow motes, not sporitic spores. | `CircusAcrobatModel.java`, `CircusCarouselEntity.java` |
| NQ-15-12 A | Nothing to build: the Hematic Lantern is left as it is. Recorded. | `docs/CIRCUS.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| T17 relay "pulse along each thread in `PuppeteerThreadRenderer`" (plan wording) | Drawn as a server-sent thread tendril from the Pattern to each performing body instead; a client renderer change was not needed for the ruling. Listed for the owner. |
| Juggler practical change | The plan changes it only if the new role needs it; the pin role does not. |
| Review proof of the Acrobat tuck and carousel motes | A performers-only re-film ran (1,229 frames) but no frame caught the Acrobat in the air, and the carousel scene was not filmed. Listed for the owner. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,163 tests in 1,359 classes, 0 failures, 0 errors, 0 skipped (`build/build2/B27-unit2.txt`). The first run failed `ServerboundGuardsCoverageTest` on the new payload; the payload now uses `ServerboundGuards.activePlayer`.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1994 required tests passed :)" (`build/build2/B27-gametest.txt`).
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 440 required tests passed :)", 18 optional failures, every one in the baseline list (`build/build2/B27-playthrough.txt`).
- Added: `CircusRouteRulesTest` two cases (the D4 and report gate; the Unstained Liberator); `CircusPavilionStateRulesTest.aBystanderIsToldOncePerAct`; `FollowSlotTest` (three cases); `PuppetActTest` (five cases: the act table, costs and cooldowns, cooldown seconds, the Trapeze reach, the transposition ring); `DialogueThemeStyleConfigTest.theCircusShowsOneTroupeCrest` and a CIRCUS frame line; GameTests `CircusRingmasterGameTests.pactGateNamesWhatIsMissing`, `CrimsonTroupePracticalGameTests.needlesAndKnivesLeaveNoStuckArrow`, `ringPerformsEveryBodysActOnOneCrossbar` (one Crossbar, Pattern plus Mummer and Hound: Spotlight and Scent both fire, 30 thread once, the second Ring refused), `trapezeSwapsOwnerAndVulture` (also: a Spotlight with no threat costs nothing).
- Flipped on purpose: `daggerVolleyMatchesLegacyBloodShotImpactAndBleeding` is now `daggerVolleyKeepsItsDamageAndTheThirdDaggerPins` (NQ-15-7 A removes the Blood Loss and adds the pin; the damage budget check stays). `waybillFindsNothing` now expects the not-found line naming its target (PT-58).
- Arranged for the new gate (D-30): `routeChoiceRequiresCompletedAttentionAct` (D4 and a report), `PlaythroughStates.pavilionRoute` and the new `schoolReport`, `attentionActAndRoute`, `NpcTradeGameTests` Ringmaster quests, `CircusJourneyFixtures`, `CircusClientReview` finales.
- `DialogueRouteCharacterisationGameTests` now posts `circus_accept_pact` at the Ringmaster; three fingerprint rows added to `route-fingerprints.txt` (all refused, as `circus_reject_pact` is), and the test passes.
- `python gradle/source-text-impact-map.py`: no change.
- Review clients: `runNpcTradeReviewClient` (33 shots) and `runCircusReviewClient -PcircusReviewOnly=performers` (1,229 frames). Both stopped by themselves; no Java process was left running.

### Docs

- `docs/CIRCUS.md`: the pact gate, the act table, the Juggler's pin, the Ring, the bystander line, Thread Ripper at 300 mL, rulings NQ-15-5, NQ-15-6 B, NQ-15-7 A, NQ-15-8 A, NQ-15-9 A and NQ-15-12 A under "Rulings that bind"; D-30, D-31, D-37, D-38, D-39 and HN-9 marked built; builder's calls; open-questions pointer, gaps and history updated.
- `docs/HEMOMANCY_REFERENCE.md` 17.1 (the gate and the acts; the Juggler's pin), `docs/LORE_REFERENCE.md` 6.3b (who may hear the bargain), `docs/NPC_DIALOGUE_AND_TRADE.md` (HN-9 built), `wiki/Crimson-Troupe.md` (the gate and the acts).
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-15-6, NQ-15-7, NQ-15-8, NQ-15-9, NQ-15-12; section 2 rows NQ-15-5 and NQ-15-8 (input half); section 3 rows 15 T18, 15 T6 tests, 15 T6 cost, 15 T2 order, 15 T5 line, 15 T4 lines, FX-26 (wider), 15 T3, 15 T16, 15 T17, 15 T5 (D-35, CB-5) and Audit E leftovers. The Ringmaster's NPC-lines row is shortened to the yield scene. Section 4: D-37 (act numbers) is now "now" with the starting values; added B27 acts, B27 pact lines and B27 looks.
- `docs/owner_answers/15-circus.md` status: T2, T3, T4, T5, T6, T16, T17, T18.

### For the owner

- New lang keys: `hemomancy.circus.ringmaster.gate.needs_degree`, `.needs_degree_and_report`, `.needs_report`, `.needs_understudy`, `.unstained` (Ringmaster's voice); `hemomancy.summon.act.<spotlight | trapeze | feint | smoke | encore | scent | lift | ring>` and each `.desc`; `hemomancy.summon.act.cooldown`, `.no_body`, `.no_thread`, `.nothing`, `.performed` ("%s!"); `screen.hemomancy.crossbar_radial.act.desc`.
- Changed values: `item.hemomancy.circus_waybill.attuned`, `item.hemomancy.circus_waybill.not_found` (now name the target); `hemomancy.summon.marrow_spitter.lore` (pin sentence) and `.role` ("Ranged Control"); `hemomancy.summon.ringmaster_pattern.lore` (the Ring); `screen.hemomancy.puppeteers_spindle.ringmaster_partner`.
- Values I chose: every act's thread cost, cooldown and reach (table in Built and in `docs/CIRCUS.md`), Ring 30 thread and 60 s, the Juggler pin 30 ticks, the 5-tick request interval, the Circus palette and crest.
- Effects I chose for one-word acts: Feint turns threats on the owner's side onto the flying Juggler and throws its pin dagger at the nearest; Smoke drops their mark and makes the owner invisible 3 s; Lift throws the owner (not another ally) up and forward; Scent uses vanilla Glowing. Trapeze refuses a spot where the owner would not fit.
- Looks to judge: `build/build2/B27-shots/npc-trade-27-ringmaster-hub.png` to `npc-trade-33-ringmaster-trade-done.png` (the Circus theme and crest), `build/build2/B27-dialogue-sheet.png` (every theme's sprites, Circus last). Acrobat frames from the re-film: `build/build2/B27-shots/performers-00037.png`, `performers-00073.png`, `performers-00109.png` (none shows it airborne). The acts and the carousel motes were never filmed.
- Surprising: an Unstained Liberator still cannot learn Thread Ripper (the memory grant refuses Unstained players), so the memory is owed through the existing finale claim, as for any Liberator who cannot learn it yet. The Pattern already cost no cap slot before this package (its +1 cap offset); only the transposition ring changed. The Pattern's old mode relay stays beside the Ring.

### Verification (round 1)

Checked on 2026-11-15 against `refs/build2/B27-before` and the tree. Result: pass.

- Scope: the 47 changed and 8 new paths are all in this package's rows. No held or reversed work, no lash numbers, no HUD files.
- Values match the answers: the pact needs Degree 4 and any one report; an Unstained player gets Liberation only, on The Missing Understudy and no Degree, and is refused Succession, a repair into it and its finale. The Juggler's third dagger pins 30 ticks (1.5 s) with no Blood Loss. Seven acts plus Ring; Ring reaches 32 blocks (`RingmasterConductorRules.RELAY_RANGE` through `sharesOwnerSession`), 60 s cooldown, no cap slot, out of the radial and `hotSwap`, still selectable at the Spindle. Thread Ripper costs 300 in `ManipulationInit`.
- Rules: `PROTOCOL_VERSION` 55 and `payload-registry.txt` match; the payload is guarded, rate-limited and calls one service. New text is lang keys. Act effects use troupe particles; no vanilla particle added.
- Unit suite: BUILD SUCCESSFUL, 6,163 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B27-verify-unit.txt`).
- Combined GameTest run: "All 1994 required tests passed :)" (`build/build2/B27-verify-gametest.txt`).
- `runCrimsonTroupeGameTestServer`: "All 41 required tests passed :)" (`build/build2/B27-verify-troupe.txt`).
- Circus review client (`-PcircusReviewOnly=puppets,liberation`, 1,381 frames, the client closed itself). Samples: `build/build2/B27-verify-shots/puppets-00100.png`, `puppets-00400.png`, `liberation-00060.png`, `liberation-00200.png`, `liberation-00380.png`. The review has no scene for the acts, so the acts are still unfilmed.
- Minor notes, not blocking:
  - Large pink diamonds still float inside the pavilion in `liberation-00200.png` and `liberation-00380.png`. They stay still while the carousel turns, so they are probably not the horse particles. The owner should say whether these are the "pink crystals" from Audit E.
  - Two Javadoc lines were left behind when their fields were removed: "Players told this finale is someone else's" in `CircusRingmasterEntity.java` and `CircusCarouselEntity.java`. Each now sits over the constructor.
  - Feint, Smoke, Encore and Lift have no GameTest of their own. The table test and the Ring and Trapeze GameTests cover the shared path.

## B28. Crimson Troupe: the site kit and set-piece blocks

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 15 T7 (D-42, D-43), NQ-15-1 A: blocks | 37 new set-piece blocks, all in hematic iron, bone, Crimson Lacquer, blood-wood, canvas and thread, none in the copper family or gilt, none with drops (`noLootTable`), each with a name, a blockstate, models and an item model, filed in the Circus creative section. Big top: `circus_king_pole` and `circus_quarter_pole` (sections foot, shaft, collar, finial; shafts 10 and 6 px), `circus_ring_curb`, `circus_ring_curb_bend` (the curb on the diagonal), `circus_seating`, `circus_gallery_rail`, `circus_trapeze`, `circus_net` (lets through 20% of fall damage), `circus_valance`, `circus_guy_stake`, `circus_podium` (two tall), `circus_footlight` (light 12, NQ-15-12 A), `circus_crossbar_rig`, `circus_spindle_rack`, `circus_trophy`, `circus_pelt_drape`, `circus_plinth`. Backyard and midway: `circus_wagon_wheel` (two blocks across, four quarters round one hub), `circus_wagon_panel`, `circus_wrecked_panel`, `circus_cookpot` (light 9), `circus_costume_rack`, `circus_trunk` (does not open), `circus_ticket_window`, `circus_pennant_line`. Camps: `circus_knife_wheel` and `circus_stilt_rack` (two tall), `circus_target_board`, `circus_weight_frame`, `circus_fire_pit` (light 13, no spreading flame), `circus_bellows_stand`, `circus_hound_hoop` (light 7), `circus_prompt_table`. Surfaces: `circus_canvas` (crimson and bone stripes), `circus_laced_canvas`, `circus_wainscot`, `circus_sawdust` (not in `#minecraft:dirt`). | `BlockInit.java`, `CreativeTabSection.java`, `TroupePropBlock.java` (now takes an outline; the masks and rigging use it), `TroupeTallPropBlock.java`, `TroupePoleBlock.java`, `TroupeWagonWheelBlock.java`, `TroupeNetBlock.java` (all new but the first), `en_us.json` |
| 15 T7: the four records | Each record is its own object and faces a way: a stage bill on an easel, a loft rigging log on a belaying rail, a quarters ledger on a writing box, a carousel maker's plate on a pedestal. They were four cubes of stripped dark oak. Same ids and clue behaviour. | `CircusEvidenceBlock.java`, their blockstates and models |
| 15 T7: models | `tools/model_export/circus_set_pieces.py` (new) authors every piece on the `fixture_atlas` pipeline at 16 texels a block, with an atlas per piece, Blockbench projects and turnaround previews (`--preview`). Two-tall pieces split at 16; the wheel is built whole in a 32-pixel frame and cut into its quarters. | `circus_set_pieces.py`, `assets/hemomancy/{blockstates,models/block,models/item,textures/block}/circus_*`, `models/block/bbmodel/circus_*.bbmodel` |
| 15 T7: generator | `tools/worldgen/circus_sites.py` over `tools/worldgen/circus/`: `kit.py` (a Site canvas and Frames that turn what they write, with assemblies for the pole, the ring and its footlights, the plinth, seating, galleries, gallery stairs, the Ringmaster's box (reached on foot through its back corners), the rigging tower over its net, valance, laced canvas walls on a wainscot, guy lines, the painted wagon, the cookhouse, the dressing tent with its mask wall, the ticket booth and banner line, the five camp apparatus, a camp lean-to and the stage wreck), `checks.py` (states, no forbidden metal, whole multiblocks, mounts, no flat 4 x 4 of one block), `sampler.py`, `preview.py`, `build.py`. It writes the kit sampler (66 x 22 x 48, 4,326 cells) into the Crimson Troupe GameTest namespace, the same bytes every run (gzip with no timestamp). | `tools/worldgen/circus_sites.py`, `tools/worldgen/circus/*.py`, `src/gameTest/resources/data/crimson_troupe_validation/structure/circus_kit_sampler.nbt` |
| 15 T7: tracking and review | `.gitignore` tracks the new generator; the AGENTS.md tools bullet names it. `CircusClientReview` gained a `kit` scene that places the sampler and flies over it. | `.gitignore`, `AGENTS.md`, `CircusClientReview.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| `pavilion.py`, `camps.py`, `stage.py` in the plan's file list | They build the live sites, which are T8 and T9. T7 builds the kit they will use; `build.py` says where they join. No live template changed. |
| Moving the old builders in `tools/circus/` to `removed_assets/` | The plan and D-42 say this happens once T8 and T9 ship. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,168 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B28-unit3.txt`). B27 had 6,163.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1995 required tests passed :)" (`build/build2/B28-gametest2.txt`). B27 had 1,994.
- `./gradlew.bat runCrimsonTroupeGameTestServer`: "All 42 required tests passed :)" (`build/build2/B28-troupe2.txt`).
- Added: `CircusKitTemplateTest` (JVM, five cases): every set piece is on the sampler; nothing on it is copper, brass, bronze or gold; every wagon wheel has its four quarters; every piece has a name, item model and blockstate, its textures are the mod's own and painted at one texel a model pixel (16 a block); every model element stays inside -16..32 and turns 0, 22.5 or 45 degrees (the loader's rules, which caught the first wheel). GameTest `CrimsonTroupeStructureGameTests.kitSamplerStandsWhole` (required): the sampler places with every kit piece standing, and breaking one wheel quarter takes the whole wheel.
- Refreshed on purpose: `src/gameTest/resources/frozen/registry-ids.txt` (74 lines added: a block and an item for each new id) and `creative-tab-order.txt` (37 lines added in the Circus section), from a run with `HEMOMANCY_FROZEN_SNAPSHOT_OUT=build/build2/B28-frozen`. Nothing else differed. `CreativeTabSectionTest` reads that file.
- Allow-list: `player-block-edit-allow-list.txt` gained one row for `TroupeWagonWheelBlock` (it places its own other quarters after the first, as a door places its upper half).
- `python gradle/source-text-impact-map.py`: 5 rows added for the new test.
- `python tools/worldgen/circus_sites.py`: passes its checks; two runs wrote the same bytes (md5 `4daa0d48...`).
- Review client: `runCircusReviewClient -PcircusReviewOnly=kit`, twice. The first run showed the wagon wheels as missing models: the rim segments turned 11.25 degrees, which the loader refuses, and the pole items reached past 32. Both fixed and the new test guards them. The second run captured 369 frames with no model load error in the log; it got past the Accessibility screen and closed itself. No Java process was left running.

### Docs

- `docs/CIRCUS.md`: "Where it lives" (the site kit row, the records, the `kit` review scene), the NQ-15-1 A ruling under "Rulings that bind" (marked built for the kit, sites to follow), "Proof owed" (T7 built, no live site uses it yet; the owner has not seen the kit), History.
- `docs/HEMOMANCY_REFERENCE.md` 17.1: a paragraph on the site kit and the four records.
- `wiki/Crimson-Troupe.md`: a paragraph on the set pieces.
- `docs/OPEN_QUESTIONS.md`: deleted section 3 row "15 T7 (D-42, D-43)". Section 1 row NQ-15-1 stays, since T8 and T9 are not built. Section 4: added "B28 kit".
- `docs/owner_answers/15-circus.md`: status line for T7.
- `AGENTS.md`: the tracked-tools bullet names the Circus sites generator and the set-piece generator.

### For the owner

- New lang keys (37), all `block.hemomancy.<id>`: `circus_canvas` "Striped Big-Top Canvas", `circus_laced_canvas` "Laced Big-Top Canvas", `circus_wainscot` "Lacquered Wainscot", `circus_sawdust` "Ring Sawdust", `circus_wagon_panel` "Painted Wagon Panel", `circus_wrecked_panel` "Wrecked Wagon Panel", `circus_king_pole` "King Pole", `circus_quarter_pole` "Quarter Pole", `circus_ring_curb` "Ring Curb", `circus_ring_curb_bend` "Ring Curb Bend", `circus_seating` "Seating Tier", `circus_gallery_rail` "Gallery Rail", `circus_trapeze` "Trapeze", `circus_net` "Rigging Net", `circus_valance` "Scalloped Valance", `circus_guy_stake` "Guy Stake", `circus_podium` "Ringmaster's Podium", `circus_footlight` "Crimson Footlight", `circus_crossbar_rig` "Hanging Crossbar Rig", `circus_spindle_rack` "Spindle Rack", `circus_trophy` "Bone and Thread Trophy", `circus_pelt_drape` "Pelt Drape", `circus_plinth` "Procession Plinth", `circus_wagon_wheel` "Wagon Wheel", `circus_cookpot` "Cookhouse Pot", `circus_costume_rack` "Costume Rack", `circus_trunk` "Costume Trunk", `circus_ticket_window` "Ticket Window", `circus_pennant_line` "Pennant Line", `circus_knife_wheel` "Knife Wheel", `circus_target_board` "Target Board", `circus_stilt_rack` "Stilt Rack", `circus_weight_frame` "Weight Frame", `circus_fire_pit` "Ember Pit", `circus_bellows_stand` "Bellows Stand", `circus_hound_hoop` "Hound Hoop", `circus_prompt_table` "Prompt-Book Table".
- Changed lang keys (4): `block.hemomancy.circus_stage_record` "Troupe Stage Bill", `circus_loft_record` "Loft Rigging Log", `circus_quarters_record` "Quarters Ledger", `impaled_procession_record` "Carousel Maker's Plate" (they were "Troupe ... Record").
- Values I chose: lights (footlight 12, ember pit 13, cook pot 9, hound hoop 7), the net's 20% fall damage, every shape and colour, the names above, no drops for any piece (as the Court's set pieces), and placing the kit sampler in the GameTest resources so it never ships in the jar.
- Looks that need your eyes: `build/build2/B28-shots2/kit-sheet.png` and its twelve frames `kit-00005.png` to `kit-00360.png` (all 369 in `build/circus-review/screenshots/kit-*.png`); turnarounds in `build/model-previews/` (for example `circus_circus_wagon_wheel.png`, `circus_circus_king_pole.png`, `circus_circus_knife_wheel.png`, `circus_circus_podium.png`, `circus_footlight.png`, `circus_stage_record.png`); a cutaway of the sampler in `build/circus-kit/circus_kit_sampler.png`.
- Surprising: the GameTest harness set the sampler down one block higher than its template coordinates, so the GameTest finds the offset from the king pole's foot. The review camera path frames the plots loosely; some frames show the kit from far off. The wagon wheels read small next to the wagon body and the bone hub caps are large; say if you want them bigger or plainer.

### Verification (round 1)

Checked the tree on 2026-11-15, not the report. Result: pass.

- Scope: the diff against `refs/build2/B28-before` and the new files stay inside 15 T7. No live template, lash number, HUD file or held manipulation changed. Nothing from the HELD list. The frozen registry and creative-tab lists only gained lines; the four record names are the only changed lang keys, and no test or doc read the old names.
- Values: NQ-15-1 A's full list is on the kit (big top pieces, galleries and stairs, rigging towers, wagons, dressing tent, ticket booth, one apparatus per camp, stage wreck). Crimson footlights follow NQ-15-12 A. No piece is copper, brass, bronze or gold (the sampler test and `checks.py` both guard it). The flat rule is the mausoleum's 4 x 4 window.
- Tests: `CircusKitTemplateTest` (five cases) and the required GameTest `kitSamplerStandsWhole` would fail without the blocks, the sampler or the wheel's break rule. No test was weakened or removed.
- Unit suite: BUILD SUCCESSFUL, 6,168 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B28-verify-unit.txt`).
- Combined GameTest run: "All 1995 required tests passed :)" (`build/build2/B28-verify-gametest.txt`, `build/phlegethontic-gametest/logs/latest.log`).
- Generator: `python tools/worldgen/circus_sites.py` passed its checks and wrote the same bytes (md5 `4daa0d48ac9bb63c572a50c92d7cb76f`). Preview sheet rendered: `build/circus-kit/circus_kit_sampler.png`.
- Looks: I did not relaunch the review client. The implementer's frames in `build/build2/B28-shots2/kit-sheet.png` show the pieces loaded and textured in game.
- AGENTS.md rules: no payloads, blood, gifts or particles touched. Every new name is an `en_us.json` key. The wagon wheel's self-placement is on the player-block-edit allow-list. Old `tools/circus/` builders stay until T8 and T9, as the plan says.
- Docs: `CIRCUS.md`, the reference, the wiki, `15-circus.md` and section 3 and 4 of `OPEN_QUESTIONS.md` match the tree. NQ-15-1 rightly stays open for T8 and T9.

## B29. Crimson Troupe: the pavilion with real galleries and its perception look

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 15 T8 (D-42, D-29, PT-57), NQ-15-1 A for the pavilion | The pavilion is rebuilt by the generator on the B28 kit: one 65 x 23 x 65 template (was 65 x 14 x 65). Big top: an octagon 43 by 39 (21 either side of the centre in x, 19 in z, corners cut where the two offsets sum past 34), laced canvas every third column on a two-course wainscot, walls 11 high, a stepped canvas roof rising one row per two cells in from the wall to row 21, its panels alternating striped and laced. Ring of radius 7 round the Impaled Procession on its plinth (set flush in the ring floor, the carousel still at (32,1,32)), a lacquer track at radius 4, six crimson footlights. Two king poles in the curb run through the roof; four quarter poles at the ring's corners. Lower (north) gallery, the loft, deck at height 5 with the loft rigging log; upper (south) gallery, deck at height 7; each has a two-wide flight at both ends. The Ringmaster's box is built out from the upper gallery over the ring; he stands at (32,8,45) behind its front rail, with his podium behind him, and a player walks up the south-west or south-east flight, along the deck and in at the box's back corners. Seating banks north (two, either side of the door aisle) and south (under the box). Rigging towers at the east and west ends, platform at 9, trapeze reaching toward the ring over a 5 x 6 net. Doors north (main), east (artists') and south (back). Outside: midway (ticket booth, banner line, pennants over a sawdust path, the Threadkeeper's lean-to with the prompt table), backyard (three painted wagons, the third added in the fix, the cookhouse, the dressing tent), the Strongman's weight frames west and the Beast Tamer's hoops east, each on a floored yard so they never stand on void, guy stakes and a valance round the eaves. All ten actors and three records are placed by the generator; the Understudy moved to (56,1,53) in the dressing tent, the quarters ledger to (57,1,54), the loft log to (40,6,14), the maker's plate to (28,1,24). Light: 30 Hematic Lanterns hung on chains (13 under the roof, 9 under the decks and the box, 4 over the galleries, 4 at the towers) and the footlights. Three authored Abocipher emitters (Harbinger Outpost profile) at (17,6,15), (47,8,49), (26,1,50); `CircusPavilionStructure.afterPlace` is gone. | `tools/worldgen/circus/pavilion.py` (new), `kit.py` (Site places actors), `build.py`, `checks.py`, `src/main/resources/data/hemomancy/structure/crimson_troupe_pavilion.nbt`, `CircusPavilionStructure.java`, `AbocipherEmitterPlacement.java` |
| 15 T8: the light rule | `checks.py` gained the plan's light rule: on a site with an `interior`, block light runs from the kit's lamps and lit set pieces (lantern 15, ember pit 13, footlight 12, cook pot 9, hound hoop 7), one level less per step through anything not a cube, stairs and slabs counted as stopping it; every open cell under the roof on a spawnable floor (a cube, a top slab, an upside-down stair; not the net) must be lit. The pavilion passes with 0 dark cells. | `tools/worldgen/circus/checks.py` |
| 15 T8: the probe | `ringmasterReachFromTheFloor` is required. A player-sized walker (a zombie, follow range 96, never added to the world) paths from the main door to the Ringmaster; the path must reach him on his deck, and a player standing where it ends must be in the server's interact reach. | `PlaythroughCrimsonTroupeGameTests.java` |
| 15 T12 (D-29, FX-28) | The perception cues stand on the pavilion's own marks, not round the carousel. Every two seconds (or after a 6-block move) the client reads the blocks within 24 of the player: each gallery rail is a cloth mark and the open deck cell behind it a figure mark; each trapeze, Crossbar rig and ring curb is a light mark. Each stage draws its old count on the marks within 28 blocks nearest to where they were last read (the order changes only at a rescan, so cues do not hop): figures 2, 3, 4; cloth 2, 4, 6 (now drapes 2.2 blocks long hung from the rail over its open side); lights 1, 3, 6. Disturbed cues drift and sway; Attuned ones hold still. Unaware sees nothing. Client only. | `CircusPerceptionOverlay.java` |
| Tidy | `tools/circus/light_pavilion.py` no longer touches the pavilion (the generator lights it); it still checks the five camps. The unused `placeHarbingerOutpostEmitters` and `AbocipherEmitterPlacementRules` (and its test) moved to `removed_assets/`; the old pavilion NBT is in `removed_assets/src/main/resources/data/hemomancy/structure/crimson_troupe_pavilion.before-B29.nbt`. | `light_pavilion.py`, `removed_assets/` |
| Test flow | The Ringmaster's options are refused past 8 blocks (`DialogueOptionPacket`), so the CT tests speak to him from his box (`Pavilion.box()`, (31,8,46)) and walk down to the ring (`ringside()`, (32,1,37)) for the acts and the finale. `clue` takes the cell a player stands in to read a record. | `PlaythroughCrimsonTroupeGameTests.java` |
| Review scene | `CircusClientReview` gained a `pavilion` scene that walks the rebuilt pavilion by day (midway, door, ring, a gallery, the box, the loft, the backyard). The castPavilion marks follow the template, and the perception scene now stands in the ring below the box, looking up at the upper gallery. | `CircusClientReview.java` |

### Skipped

| Row or part | Why |
| --- | --- |
| 15 T8: the pavilion as jigsaw pieces (big top, backyard, midway) | Left as a row in section 3 of `docs/OPEN_QUESTIONS.md`, for the owner to keep or drop. Kept as one template. Every test, the playthrough harness and the review client place `crimson_troupe_pavilion` whole, so splitting it would only move work into test plumbing. The three areas are regions of the one template. |
| D-42: moving the old builders in `tools/circus/` to `removed_assets/` | D-42 and the plan move them once T8 and T9 ship. T9 (camps and stage) is not built. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,172 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B29-unit2.txt`). B28 had 6,168: six added, two removed with the dead emitter rules.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1995 required tests passed :)" (`build/build2/B29-gametest3.txt`, `B29-gametest3-latest.log`). The first run failed both pavilion finales (the 8-block option range, see "For the owner"); fixed in the test flow, then green twice.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 440 required tests passed :)", 17 optional failures, every one in the baseline list (`build/build2/B29-playthrough2.txt`, `B29-playthrough2-latest.log`, `B29-playthrough2-optional.txt`). B27 had 18 optional; `ringmasterreachfromthefloor` left the list because it is required now and passes (it walked 43 nodes from the door to the Ringmaster).
- `python tools/worldgen/circus_sites.py`: both templates pass `checks.py` (states, metals, multiblocks, mounts, flats, and the new light rule with 0 dark cells); two runs write the same bytes. The kit sampler is unchanged (md5 `4daa0d48...`).
- Review client: `runCircusReviewClient -PcircusReviewOnly=pavilion,perception,lanterns` (1,560 frames) and again `-PcircusReviewOnly=perception` after the nearest-first change (470 frames). Both got past the Accessibility screen, loaded the `CircusB29` save and closed themselves; no Java process was left.
- Added: `CircusPavilionTemplateTest` (JVM, six cases): the 65 x 23 x 65 footprint; all ten actors on open marks; the Ringmaster on the upper deck with his podium behind and the front rail before him; rails along both decks and a stair tread at every row from 1 to 7; the three records, six footlights, at least 20 lanterns and three emitters with the outpost profile; nothing of the old wool tent and no forbidden metal.
- Flipped on purpose: `ringmasterReachFromTheFloor` is required and rewritten from a floor-cell scan to a walked path (D-29, PT-57). `CircusPresentationRegressionTest` now asserts the overlay does not name the carousel and reads the gallery rail (T12). `CircusPavilionWorldgenResourceTest` lost its source assertion that the structure calls the emitter placement (the emitters are template cells now; the template test checks them). `CrimsonTroupeStructureGameTests.realTemplatesCoverEveryProfessor` checks the new size and the pavilion's set pieces by role.
- Removed with dead code: `AbocipherEmitterPlacementRulesTest` (to `removed_assets/`).
- `python gradle/source-text-impact-map.py`: one row added for the template test.

### Docs

- `docs/CIRCUS.md`: the perception paragraph (cues on marks), "Where it lives" (worldgen, kit, asset tooling, review client rows), the worldgen table (65x23x65) and the pavilion paragraph rewritten, rulings D-29/PT-57 (built), D-42 (pavilion built) and NQ-15-1 A (pavilion built), "Proof owed" (first two bullets), "Calls made without the user" (two B29 lines), History (B29).
- `docs/HEMOMANCY_REFERENCE.md` 17.1: the presentation paragraph (no private geometry text; galleries real, cues on marks), the template sizes, a paragraph on the rebuilt pavilion; the Abocipher ambience line.
- `wiki/Crimson-Troupe.md` (set pieces paragraph, acclimation paragraph), `wiki/World-Content.md` (the pavilion), `docs/PLAYTHROUGH_TESTS.md` (PT-57 built, probe required).
- `docs/OPEN_QUESTIONS.md`: deleted section 3 rows "15 T8 (D-42, D-29, PT-57)" and "15 T12 (D-29, FX-28)"; T10 now waits only on T9, T11 on NQ-15-10, T13 is ready. Section 1 row NQ-15-1 shortened to T9 (T7 and T8 built). Section 4: the D-29 row now waits on T10 only, the FX-27 row's lantern sentence replaced, and a new "B29 pavilion" row. No section 2 row is fully built (D-42's file move waits on T9).
- `docs/owner_answers/15-circus.md`: status lines for T8, T10, T11, T12, T13.
- Rulings: D-29 and PT-57 were already recorded under "Rulings that bind" in `docs/CIRCUS.md`; the line now says built 2026-11-15 (B29).

### For the owner

- New or changed lang keys: none.
- Values I chose: the whole layout (sizes above), deck heights 5 and 7, the box at (32,7,44), the king poles in the curb, three wagons (x 26, 36, 44), every lamp spot (30 lanterns), the three emitter cells, the actors' and records' new marks, the cue range 28 and rescan 2 s, the cloth drop 2.2 blocks.
- Looks that need your eyes: `build/build2/B29-shots/`: `lanterns-00114.png` and `lanterns-00551.png` (the big top at night: ring, king poles, galleries, stairs, seating, lamps, the Ringmaster in his box), `pavilion-00007.png` (the ring by day), `pavilion-00091.png` (the upper gallery and the box), `pavilion-00164.png` (the loft, with a figure cue), `pavilion-00328.png` (outside from the east: the Beast Tamer's yard and the artists' door), `perception-00116.png`, `-00234.png`, `-00370.png` (Disturbed, Acclimating, Attuned from the ring). All frames: `build/circus-review/screenshots/` (`lanterns-*`, `pavilion-*`, `perception-*`); the cutaway `build/circus-kit/crimson_troupe_pavilion.png`. Things I would change if you agree: the long walls read as one flat striped face from outside (the flat rule passes, but it wants buttresses or a second valance); the review camera clips a tower platform once and the last pavilion keys hold still; the cues are faint at Disturbed by design.
- Surprising: the Ringmaster's dialogue options need the player within 8 blocks, so the tests now speak to him from his box and walk down to the ring for the acts and the finale, as a player would. The cutaway sheet of the template is `build/circus-kit/crimson_troupe_pavilion.png`.

### Verification (round 1)

Checked the tree against `refs/build2/B29-before`, not the report.

- Scope: only the pavilion, its generator, the perception overlay, their tests, the review scene and docs changed. Nothing held or reversed was touched; no lash, HUD, payload or lang file changed. `afterPlace` and the unused emitter rules (with their test) are in `removed_assets/`, as is the old pavilion NBT.
- Generator: `tools/worldgen/circus_sites.py` run in a scratch copy writes a `crimson_troupe_pavilion.nbt` byte-identical to the one in the tree (65 x 23 x 65, 26,387 cells).
- Tests: `CircusPavilionTemplateTest` (new), the rewritten `CrimsonTroupeStructureGameTests` set-piece check and the rewritten `ringmasterReachFromTheFloor` (now required, a pathfinder walk from the door up the gallery stairs) would each fail on the old tent. The removed `pavilionPlacesHarbingerStyleAbocipherEmitters` pin is replaced by the template test's three authored emitters. `CircusPresentationRegressionTest` was flipped on purpose to D-29 (no carousel, rail marks).
- Unit suite: BUILD SUCCESSFUL, 6,172 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B29-verify1-unit.txt`).
- Combined GameTest run: "All 1995 required tests passed :)" (`build/build2/B29-verify1-gametest.txt`, `B29-verify1-gametest-latest.log`).
- `crimson_troupe_validation`: "All 42 required tests passed :)" (`build/build2/B29-verify1-troupe.txt`).
- Playthrough suite: "All 440 required tests passed :)", 17 optional failures, all in the baseline list (`build/build2/B29-verify1-optional.txt`). `ringmasterReachFromTheFloor` walked 43 nodes to his side.
- Review client: `runCircusReviewClient -PcircusReviewOnly=pavilion,perception` on the `CircusB29` save got past the Accessibility screen, captured 919 frames and closed itself; no Java process left. Kept frames in `build/build2/B29-verify1-shots/`: `perception-00060.png` (score 0, Disturbed), `perception-00170.png` (300), `perception-00290.png` (700), `perception-00420.png` (1000, Attuned), and `pavilion-00007.png`, `-00091.png`, `-00200.png`, `-00400.png`. Galleries, stairs, box and cues on the rails show as described. The Disturbed frame shows almost no cue.
- Problem, blocking: NQ-15-1 was answered A, the full list, which names three painted wagons in the backyard. `pavilion.py` places two (`kit.wagon` at x 36 and 44). Either add the third wagon, or keep "a third backyard wagon" open in the NQ-15-1 row of `docs/OPEN_QUESTIONS.md` and in the 15 T8 status line, which now say T8 is built.
- Problem, not blocking: T8's jigsaw split (big top, backyard, midway) was skipped as one template; this is a plan detail, not an answer, and it is logged, but the 15 T8 status line and section 3 should say it is left if the owner wants it.
- Problem, not blocking: the overlay rescans about 60,000 blocks on the render thread every 2 seconds near the site. It ran without a stall in the review, but a cheaper scan (only near a carousel, or chunk section filtering) would be safer.

### Fix

- Blocking problem fixed: the backyard now has three painted wagons, as NQ-15-1 A asks. `tools/worldgen/circus/pavilion.py` places a third `kit.wagon` at x 26, z 56, west of the back-door path; its cells were empty before. The other two stay at x 36 and 44. The pavilion NBT was regenerated (`checks.py` passed, light rule included) and is byte-identical across two runs (md5 `d6c8cd3a77b1b3fd688bcfc09c213f5f`).
- Test first: `CircusPavilionTemplateTest.theBackyardKeepsThreePaintedWagons` counts the wheel hubs (quarter 0) and wants 12, three wagons on four wheels. The old template had 8.
- Docs: "two painted wagons" is now three in `docs/CIRCUS.md`, `docs/HEMOMANCY_REFERENCE.md` and this log's Built table and values line; the "two wagons not three" note left the B29 ruling line in `CIRCUS.md`. The 15 T8 status line in `15-circus.md` names the third wagon. The skipped "three painted wagons" row is gone.
- Non-blocking: the jigsaw split is now a section 3 row in `docs/OPEN_QUESTIONS.md` ("15 T8 (jigsaw split)", your call), and the 15 T8 status line says it is left if you still want it. The overlay's 2-second block scan is left as it is; no stall showed in either review run.
- Unit suite: BUILD SUCCESSFUL, 6,173 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B29-fix-unit.txt`).
- Combined GameTest run: "All 1995 required tests passed :)" (`build/build2/B29-fix-gametest.txt`, `B29-fix-gametest-latest.log`).
- `crimson_troupe_validation`: "All 42 required tests passed :)" (`build/build2/B29-fix-troupe.txt`).
- Review client: `runCircusReviewClient -PcircusReviewOnly=pavilion,perception` on the `CircusB29` save, which places the template fresh. It got past the Accessibility screen, captured 919 frames and closed itself; no Java process left. Kept in `build/build2/B29-fix-shots/`: `perception-00060.png` (score 0), `perception-00170.png` (300), `perception-00290.png` (700), `perception-00420.png` (1000), `pavilion-00007.png`, `pavilion-00200.png`, `pavilion-00270.png` and `pavilion-00300.png` (the backyard wagons), and the cutaway `crimson_troupe_pavilion.png` (the plan view shows the three wagons).


### Verification (round 2)

Checked the tree against `refs/build2/B29-before`, not the report.

- Scope: only the pavilion, its generator, the perception overlay, their tests, the review scene and docs changed. Nothing held or reversed, no lash number, HUD, payload, lang key or particle allow-list changed. The removed emitter rules, their test and the old NBT are in `removed_assets/`.
- The fix holds: `pavilion.py` places three `kit.wagon` (x 26, 36, 44 at z 56). Running `tools/worldgen/circus_sites.py` in a scratch copy writes a pavilion byte-identical to the tree (md5 `d6c8cd3a77b1b3fd688bcfc09c213f5f`, 26,488 cells). `CircusPavilionTemplateTest.theBackyardKeepsThreePaintedWagons` would fail on the two-wagon template.
- Unit suite: BUILD SUCCESSFUL, 6,173 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B29-verify2-unit.txt`).
- Combined GameTest run: "All 1995 required tests passed :)" (`build/build2/B29-verify2-gametest.txt`, `B29-verify2-gametest-latest.log`).
- `crimson_troupe_validation`: "All 42 required tests passed :)" (`build/build2/B29-verify2-troupe.txt`).
- Playthrough suite: "All 440 required tests passed :)", 17 optional failures, every one in the baseline list (`build/build2/B29-verify2-playthrough-latest.log`). `ringmasterReachFromTheFloor` is required and walked 43 nodes to his side.
- Review client: `runCircusReviewClient -PcircusReviewOnly=pavilion,perception`, `CircusB29` save. It got past the Accessibility screen, captured 919 frames and closed itself; no Java process left. Kept in `build/build2/B29-verify2-shots/`: `perception-00060.png` (score 0, Disturbed), `perception-00170.png` (300), `perception-00290.png` (700), `perception-00420.png` (1000, Attuned: figures stand on the gallery deck behind the rail), `pavilion-00007.png`, `pavilion-00091.png`, `pavilion-00270.png` and `pavilion-00300.png` (the backyard wagons).
- Problem, not blocking: `docs/CIRCUS.md` line 189 (the NQ-15-1 A ruling) still ends "with two of the three painted wagons". It should say the three wagons are built.
- No blocking problem.

## B30. Crimson Troupe: camps, the stage, placement, outcomes and gifts

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 15 T9 (D-42, NQ-15-1 A for the camps and stage) | The five camps and the abandoned stage are written by the generator on the B28 kit. A camp is 27 x 12 x 27 (was 25 x 9 x 25). Row 1 lands on the top ground block (start height -1) over a row-0 foundation skirt of packed mud and mud bricks. The yard is a rounded square of ring sawdust with a plank row every third row, never a `#minecraft:dirt` block, ringed by a packed-mud bank under mud-brick slabs, open south, with a pennant line over the way in. A practice ring of radius 5 (curb, lacquer track at radius 3, venous-stone hub) holds the professors at (10, 16), or (10, 14) and (10, 18) in the two-professor camps; the cells four blocks east of each, a block either side, stay open for the practical's two threats. The Threadkeeper stands under a lean-to (open south) with the Puppeteer's Spindle and a crafting table; a painted wagon stands north-east; four lamp posts with hanging Hematic Lanterns stand at the quarters. The apparatus is north of the ring: air a rigging tower (platform at 4) over a net, blades a knife wheel between target boards, pageantry a stilt rack and two weight frames, embers an ember pit, a bellows stand and two hound hoops, memory a prompt-book table, trunk, costume rack and the quarters ledger. The stage is a 17 x 10 x 15 roadside wreck (start height -1): a rutted gravel road, the burnt stage wagon with its west wheels gone, the boards run out west with the stage bill at (5, 5, 5), read from the board at (4, 5, 5), a dropped wheel, a fallen trunk, a snapped quarter pole and a fallen pennant line. No camp or stage holds an openable container. | `tools/worldgen/circus/camps.py`, `stage.py` (new), `build.py`, `src/main/resources/data/hemomancy/structure/troupe_camp_*.nbt`, `troupe_abandoned_stage.nbt` |
| D-42 (file move) | The old site builders moved to `removed_assets/tools/circus/`: `build_troupe_assets.py`, `build_teaching_camps.py`, `troupe_templates.py`, `light_pavilion.py`, `nbt_io.py` (used only by `light_pavilion.py`), `preview_teaching_camps.py`, `teaching_camp_preview.html`. The old templates are in `removed_assets/src/main/resources/data/hemomancy/structure/*.before-B30.nbt`. | `removed_assets/` |
| 15 T10 (D-43, NQ-15-2) | All seven troupe structures use `beard_box` (sites bring their own ground); camps and stage start at -1 for their skirt; the pavilion keeps start height 0. The terrain gate samples the laid-out footprint's four corners first, then the edge midpoints and centre, then a grid every 8 blocks (`TroupeFootprintScan`); every sampled column must be dry ground under height 150 (`troupeGround`, unchanged), and all within 6 blocks of each other (`TROUPE_MAX_SLOPE`). The pavilion's gate skips its 11-block outer yards (`gate_inset` 11, a new optional codec field, default 0), so it samples the 43-block core: 54 columns, not the 4,225 the old gate read. No troupe site starts within 6 chunks of a village's potential start chunk whose biome takes a village (`nearVillage`, `VILLAGE_CHUNKS`). Camps and stages keep `exclusion_zone` 6 chunks from the pavilion set. Spacings kept: 48/16, 24/8, 32/10. The every-column scan, `TroupeTerrainColumns` and its mixin accessor `TroupeNoiseGeneratorAccessor` are gone (in `removed_assets/`). | `StructurePlacementChecks.java`, `TroupeFootprintScan.java`, `CircusPavilionStructure.java`, `worldgen/structure/*troupe*.json`, `circus_pavilion.json`, `worldgen/structure_set/troupe_*.json`, `hemomancy.mixins.json` |
| 15 T10: validation run | New `circus_worldgen_validation` namespace and `runCircusValidationServer` row (`-PcircusSeed=`, world in `build/circus-worlds/<seed>`; alpha false). It generates every potential start chunk of the three sets within 4,096 blocks to structure starts, records each as accepted or refused with a reason (exclusion from a pavilion, village, biome, wet or high, steep, other), the nearest site of each kind, and writes `circus-report.json`. It fails only if a kind has no site in that square. Measured on fresh worlds: nearest pavilion 653 blocks (seed 42), 1,968 (1337), 3,064 (8675309); pavilions accepted 4, 5 and 2 of about 112 cells; camps 35, 48, 45 of about 465; stages 38, 38, 53 of 256. Nearest camp of each kind: 250 to 2,600 blocks. Reports copied to `build/build2/B30-circus-report-<seed>.json`. | `gametest/CircusWorldValidation.java` (new), `gradle/game-tests.gradle`, `src/gameTest/resources/data/circus_worldgen_validation/structure/room.nbt` |
| 15 T10: review scene | `CircusClientReview` gained a `sites` scene: the five camps and the stage on a field, each flown over by day, three revisited at night, then the ruin laid over one pavilion and the tenancy over the other. | `CircusClientReview.java` |
| 15 T11 (D-44, NQ-15-10 gifts A and survivors A, tenancy default) | The generator writes two overlays in the pavilion's own coordinates, holding only the cells that change: `crimson_troupe_pavilion_ruin` (382 cells: the roof torn open in three places with canvas and board heaps below, the plinth a wrecked panel and wrecked panels and slabs across the ring, trapezes replaced by hanging chains, 40% of net cells gone, the box's valance down, a third of the lanterns and half the footlights gone) and `crimson_troupe_pavilion_tenancy` (one red wall banner with a bone border and skull at (32, 10, 46), behind the podium). `CircusSiteStateService` finds the pavilion's rotation from its podium, saves the overlay's chunks on the site, lays each chunk once it is loaded (now, then every 20 ticks), changes a cell only where the world still holds the pavilion's own block, and names the banner for the holder. `authorRuin` and its cobwebs are gone. On Liberation the finale performers still standing leave in a spore burst; the faculty stay. The Ringmaster's greeting names the holder of a Succeeded site to other visitors. On a first passed practical each professor gives two Enthralling Filaments and one frame material, owed through `OwedRewards` before the practical is recorded, then delivered. | `CircusSiteStateService.java` (new), `CircusPavilionSavedData.java`, `CircusRingmasterEntity.java`, `CircusSchoolGifts.java` (new), `CircusApprenticeshipProgress.java` (`canCompletePractical`), `CircusPracticalController.java`, `tools/worldgen/circus/site_states.py` (new), `en_us.json` |

### Skipped

| Row or part | Why |
| --- | --- |
| A foundation skirt and earthwork inside the pavilion template | The pavilion keeps its B29 template and start height 0; its ground comes from `beard_box`. A skirt row would shift every tested pavilion coordinate (the box, ringside, records, perch) for no gain the terrain adaptation does not already give. |
| The owner's aim of a pavilion within about 1,500 blocks | Measured, not met on two of three seeds (653, 1,968, 3,064). Within the ruling (spacings kept, slope about 6) the gate is as loose as it reads; the rest is biome and slope. Left for the owner as a section 4 row. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,175 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B30-unit2.txt`). B29 had 6,173.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 1997 required tests passed :)" (`build/build2/B30-gametest2.txt`, `B30-gametest2-latest.log`). B29 had 1,995.
- `./gradlew.bat runCrimsonTroupeGameTestServer`: "All 44 required tests passed :)" (`build/build2/B30-troupe2.txt`). B29 had 42.
- `./gradlew.bat runPlaythroughGameTestServer`: "All 440 required tests passed :)", 17 optional failures, every one in the baseline list (`build/build2/B30-playthrough2-optional.txt`).
- `./gradlew.bat runCircusValidationServer -PcircusSeed=42` (then 1337, 8675309), each on a fresh world: "All 1 required tests passed :)" three times (`build/build2/B30-circusval-*.txt`). The first seed-42 run had no pavilion in 4,096 blocks with the gate on the full 65-block box; the gate inset and the village biome check followed. A reused world keeps its old structure starts, so the world folder must be deleted between runs (written in `docs/TESTING.md`).
- `python tools/worldgen/circus_sites.py`: every template passes `checks.py` (the overlays get the metal check only, being partial); two runs write the same bytes. The pavilion (`d6c8cd3a...`) and kit sampler (`4daa0d48...`) are unchanged.
- Rewritten on purpose: `CrimsonTroupeCampTemplateTest` (JVM, three cases) now checks the new camps (size, a skirt with no holes, no `#minecraft:dirt` yard, each apparatus, the Spindle, crafting table and one wagon on four wheels, every professor and the Threadkeeper with headroom, the two practice-threat cells open with ground under them) and the stage (size, the bill on its board, the reading cell, wrecked panels, the road, no actors). The old one pinned the wool tents. `TroupeFootprintScanTest` is rewritten to the sampled scan (corner refused within 4 asks, each column once, 81 columns for a 65-block square). `CrimsonTroupeStructureGameTests.realTemplatesCoverEveryProfessor` checks the new camp and stage sizes and pieces by role instead of the wool tents.
- Flipped on purpose: the Liberation finale (`PlaythroughCrimsonTroupeGameTests.liberationFinale`, run in both suites) no longer wants cobwebs; it wants the plinth splintered, every loaded ruin chunk laid, the finale performers gone and the faculty there (D-44, survivors A). The Succession finale now also wants the successor's named banner and the site's holder name (tenancy).
- Added: `CircusPavilionWorldgenResourceTest.troupeSitesKeepTheirSpacingsAndBringTheirOwnGround` (the owner's three spacings, the exclusion zone on camps and stages, `beard_box` and the start heights); GameTests `CrimsonTroupePavilionGameTests.practicalGiftWaitsForRoomAndLife` (every lesson's gift is two Filaments and one material; a full pack and a death keep it owed; the living retry gives it once; a second pass gives nothing) and `ruinSparesWhatAPlayerPlaced` (the roof tears, the plinth splinters, a block a player set where a heap would fall stays, no chunk left owed).
- Removed with dead code: `TroupeFootprintGameTests.verifyNoiseColumns` (it tested the gone `TroupeTerrainColumns`; it was not a `@GameTest`, but the Pelagic locate probe called it, and that call is gone too). The old file is in `removed_assets/`.
- Allow-list: `player-block-edit-allow-list.txt` swaps the Ringmaster's cobweb row for one `CircusSiteStateService` row.
- `python gradle/source-text-impact-map.py`: one row added.
- Review client: `runCircusReviewClient -PcircusReviewOnly=sites` on the `CircusB29` save, three runs (the first framed the far pavilions out of loaded chunks; the second fixed that; the third framed the banner). Each got past the Accessibility screen, captured 634 or 635 frames and closed itself; no Java process was left. The save logs two "Exception loading structure piece minecraft:jigsaw" errors from old saved chunk data (an empty pool element), which this package did not touch.

### Docs

- `docs/CIRCUS.md`: "Where it lives" (worldgen row, a new site after-states row, asset tooling, review client and validation server), the worldgen table and paragraphs (camp and stage sizes, `beard_box`, the gate, the camps and stage, the measured distances), rulings D-42 and D-44 marked built, the NQ-15-1 A ruling's wagon text fixed (B29's verification note), "Calls made without the user" (a B30 line), "Open questions", "Proof owed", History (B30).
- `docs/HEMOMANCY_REFERENCE.md` 17.1: template sizes, the placement gate and measured distances, the kit paragraph's camps and stage, and a new site outcomes and gifts paragraph.
- `docs/LORE_REFERENCE.md` 6.3b: what each ending leaves at the pavilion. `docs/NPC_DIALOGUE_AND_TRADE.md`: the gifts take the `OwedRewards` claim. `docs/TESTING.md`: the validation run. `docs/PELAGIC_WORLDGEN.md`: the gate no longer scans every column. `docs/architecture/ARCHITECTURE_STANDARDS.md` and `AGENTS.md`: tracked tools.
- `wiki/Crimson-Troupe.md` (camps, gifts, ruin, banner) and `wiki/World-Content.md` (camp size, stage, distances; the stale "nothing points to a stage" sentence).
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-15-1, NQ-15-10 and NQ-15-2; section 2 rows NQ-15-10 (tenancy half), D-42 (file layout) and D-43 (placement gate); section 3 rows 15 T9, T10 and T11. Section 4: the D-29 row no longer waits on T10; added "B30 sites", "B30 placement" and "B30 gifts and lines".
- Rulings recorded under "Rulings that bind" in `docs/CIRCUS.md`: NQ-15-2, D-43 (placement gate), NQ-15-10 (gifts A, survivors A), NQ-15-10 (tenancy half), with D-42 and D-44 marked built.
- `docs/owner_answers/15-circus.md`: status lines for T8 (the builders moved), T9, T10 and T11.

### For the owner

- New lang keys (3): `message.hemomancy.circus.school.gift` "A gift for the passed practical: two Enthralling Filaments and %s, for the body's frame."; `hemomancy.circus.ringmaster.tenancy` "The ring belongs to %s now. I still call the acts, on their word." (Ringmaster sheet: short and final after Succession); `hemomancy.circus.tenancy.banner` "%s's Banner". No key changed or removed.
- Values I chose: camp 27 x 12 x 27 and stage 17 x 10 x 15, start height -1, the ring radius 5, professor marks, lamp spots; gate grid 8 and slope 6 (the ruling's "about 6"), pavilion gate inset 11, village distance 6 chunks (only villages whose start biome takes one), exclusion 6 chunks; the frame materials (Venous Pinion for the Acrobat, Barbed Splinter for the Dagger Thrower, Puppeteer's Wool for the Stilt Walker, Hematic Iron Scrap for the Strongman, Leather for the Beast Tamer, Blaze Powder for the Fire Eater, Engram Stamp for the Understudy); the ruin's three tears and its debris rates; the banner's colours. The village distance applies to pavilions too, not only camps and stages.
- Looks that need your eyes: `build/build2/B30-shots/sites-sheet.png`; camps by day `sites-00015.png` to `sites-00215.png`, the stage `sites-00262.png`, `-00295.png`, night `sites-00320.png` to `-00415.png` (dark: only four lamps per camp), the ruin `sites-00470.png`, `-00515.png`, the banner `sites-00600.png`, `-00630.png`; all frames `build/circus-review/screenshots/sites-*.png`; cutaways `build/circus-kit/troupe_camp_*.png`, `troupe_abandoned_stage.png`.
- Surprising: the pavilion is rare. Even with the sampled gate only 2 to 5 of about 112 pavilion cells in 4,096 blocks hold one; most fail the biome list (about 55%), the 6-block slope (about 20%) or water (about 13%). Your 1,500-block aim held on seed 42 only. Loosening the slope for the pavilion or widening its biomes would move it; I left both as ruled. The gate is also much cheaper now: the old one read every column of the footprint.
- After a Liberation the pavilion performers are gone, so their practicals are taught only at the camps from then on.

### Verification (round 1)

Checked the tree against `refs/build2/B30-before` on 2026-11-15. Result: pass, tree green, with two non-blocking problems.

- Scope: only 15 T9, T10 and T11. No lash, HUD or held-manipulation file, nothing HELD, no Silent Archon rename. No payload, so no protocol change.
- Values: spacings 48/16, 24/8, 32/10 kept (JSON and `troupeSitesKeepTheirSpacingsAndBringTheirOwnGround`). Gate samples corners, edges and an 8-block grid, slope 6; exclusion 6 chunks and a village distance. Gifts are two Enthralling Filaments and one frame material, owed through the `OwedRewards` `PendingDelivery` claim before the practical flag. After a Liberation the finale performers are discarded and the faculty stay. The banner hangs behind the podium and the Ringmaster names the holder.
- Rules: three new `en_us.json` keys, none changed. No vanilla particle added. Old builders, templates, `TroupeTerrainColumns` and its mixin accessor are in `removed_assets/`. The new block editor is on `player-block-edit-allow-list.txt`.
- Unit suite: BUILD SUCCESSFUL, 6,175 tests, 0 failures (`build/build2/B30-verify-unit.txt`).
- Combined GameTest run: "All 1997 required tests passed" (`build/build2/B30-verify-gametest.txt`).
- `crimson_troupe_validation`: "All 44 required tests passed" (`build/build2/B30-verify-troupe.txt`).
- Natural placement, fresh world seed 1337: passed. Nearest pavilion 1,968 blocks, the same as the build's report (`build/build2/B30-verify-circusval-1337.txt`).
- Review client: `runCircusReviewClient -PcircusReviewOnly=sites` on `CircusB29`. It got past the Accessibility screen, captured 634 frames and closed. No Java process was left.
- Problem 1 (look evidence, non-blocking): the `sites` scene never shows the ruin or the camps at night. Frames 305 to 545 show only sky, both in the build's run and in mine. So `sites-00470/00515` (ruin), `sites-00320` to `-00415` (night) and `sites-00295` named under "For the owner" show no site. Also `sites-00262` is a camp, not the stage; the stage is `sites-00165`. The ruin's blocks are proven by `ruinSparesWhatAPlayerPlaced` and the Liberation finale, but nobody has seen the ruin in a client. To fix: point the night keys and the siteB ruin keys at the ground, and wait for the chunks to load after `place`. Then refilm, and fix the frame list in the "B30 sites" row of section 4. My copies: `build/build2/B30-verify-shots/` (`sites-00165`, `-00262`, `-00370`, `-00470`, `-00515`, `-00600`).
- Problem 2 (non-blocking): `tools/circus/verify_troupe_assets.py` still asserts camps are 25 x 9 x 25 (line 101). It also reads a missing `src/generated/.../circus_pavilion.nbt`, which was stale before B30. No test runs it. Update or retire it.

## B31. Crimson Troupe: the Ringmaster on the boss kit, his moves and the yield scene

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 15 T13 (D-18, D-33, D-34), D-33 (boss definition) | The finale runs on the generic half of the boss kit. A saved `BossIntroWindow` of 60 ticks at the start of RAFTERS: no knife, swarm, troupe `beginFinale` or bar until it ends (RAFTERS still 160 ticks in all). Moves are `BossMoveTimeline`s synced as a move id and tick and replayed on the client through `BossReplayClock`; the entity-event `AnimationState` clocks and `MeleeAttackGoal` are gone. Each move strikes a `BossFootprint`; `CircusEffects.footprint` draws the floor from the same `hits` test (bone edge, crimson body filling with the charge). DESCENT picks by `BossBands.STANDARD`; a player he cannot path to gets knives. One `CircusArena` record (ring radius 7, leash 24 blocks, grace 200 ticks) for the acts and the finale; the troupe is the finale cast homed within the leash, not a 20-block box; a finale owner past the leash is told once (`hemomancy.circus.finale.leash`) and loses the site after the grace (the finale leash was 64 blocks before). One phase writer: the carousel only reports itself destroyed and the Ringmaster moves to DESCENT, so Liberation's card plays. Severing and anchor hits answer only in CAROUSEL. The bar names CAROUSEL's objective and fills by it. His row in `hemomancy:bosses` was already built (B15); his class stays in `common/entity/npc/circus`. | `CircusRingmasterEntity.java`, `CircusArena.java` (new), `CircusCarouselEntity.java`, `CircusPerformanceController.java`, `CircusEffects.java`, `BossBarBinding.java` (name and progress getters), `CircusRingmasterModel.java` |
| 15 T15 (D-33), NQ-15-4 A, NQ-15-3 A | `CircusRingmasterMoves`: knife (12-tick wind-up, 4 damage), staff blow (12; 2.6 blocks, 100 degrees, 7), Spotlight (20, then 40 ticks of a 2.5-block pool that follows the player at 0.12 a tick; looking more than 60 degrees away from him in it roots 30 ticks through `MorphlingLock.pin`, once a move), Sleight (20; he vaults onto a 2.4-block ring round the player with two doubles and swaps with one at tick 10; only he thrusts, 3.2 blocks, 70 degrees, 7; the doubles are drawn by `CircusRingmasterRenderer`, not entities, and last 80 ticks; striking him in the wind-up breaks it), the Cue (20; every standing finale performer lands its own move on his beat, timed by game time through `CircusPerformerEntity.answerCue`), Knife Board (20; eight knives round where the player stood, 1.8 blocks, 6; a dash out breaks it), Strings Cut (20; each performer downed as DESCENT opens lends one act, used once and saved: Acrobat vault 2.5-block disc, Knife Thrower fan 12-block 20-degree lane, Fire Eater breath 5 blocks 55 degrees and 3 s of fire, Stilt Walker slam 3-block disc; 6 damage). Picking: RAFTERS a 35-tick beat, every third beat Spotlight, Board, then Cue; CAROUSEL a 50-tick beat, the Cue on alternate beats while the troupe stands; DESCENT 16 ticks between moves, band weights in `descent`. Nothing struck by NQ-15-3 A was built. | `CircusRingmasterMoves.java` (new), `CircusRingmasterEntity.java`, `CircusPerformerEntity.java`, `CircusRingmasterRenderer.java`, `CircusRingmasterModel.java` |
| 15 T14 (D-36), NQ-15-11, D-36 (audio) | A saved `CURTAIN` phase (added before COMPLETE in `CircusPavilionStateRules.Phase`). The blow that would kill opens it in either route. Succession: 60 ticks; he kneels, the topper comes off his staff (synced, and it stays off at a Succeeded site), he says `hemomancy.circus.ringmaster.yield` at tick 12, and the Pattern and the site settle when the beat ends (`CircusYieldScene`, card `circus_yield`); then he stands 30 ticks and vaults back to his perch, still the trade speaker and conductor teacher. Liberation: 40 ticks of threads letting go one by one (card `circus_collapse_card`), then the ruin and his death. A relog, a death (`resetOwned`), the carousel's grace or a chunk unload during the beat does not release the site; it settles once the owner is back. Sound events: `entity.circus_ringmaster.phase_change` (3 candidates), `.finale_collapse` (2), `.hurt` (1), `entity.impaled_procession.thread_sever` (1), `.anchor_break` (1), `entity.circus_performer.windup` (3); each candidate is a variant, mixed to mono. They replace `ENDER_DRAGON_GROWL`, `TOTEM_USE`, `CHAIN_BREAK` and `ANVIL_DESTROY`; the performer wind-up and his hurt were silent. `entity.circus_ringmaster.music` is an intentionally empty event that `EndgameBossMusicHandler` plays through the finale. | `CircusRingmasterEntity.java`, `CircusPavilionStateRules.java`, `CircusPavilionSavedData.java`, `CircusCarouselEntity.java`, `CircusYieldScene.java` (new), `BossPresentationIds.java`, `BossSceneCatalog.java`, `SoundInit.java`, `sounds.json`, `assets/hemomancy/sounds/entity/{circus_ringmaster,impaled_procession,circus_performer}/`, `EndgameBossMusicHandler.java`, `en_us.json` |
| 15 T19 | Every remaining Circus ruling is recorded in `docs/CIRCUS.md`; the only optional Circus GameTest left is `waybillLocatesACamp` (needs a seeded world, closes no decision). | `docs/CIRCUS.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| The audit's carousel ids `entity.circus_carousel.*` | `circus_carousel` is a retired name (`RetiredNamesGuardrailTest`), so the events are `entity.impaled_procession.thread_sever` and `.anchor_break`, the entity's own id. |
| A music track | None exists; the owner supplies it (the D-36 P row). |
| Doubles that face the player | The doubles are drawn in his own pose and facing. Turning each one needs its own yaw; left as is. |
| `CircusFinaleRulesTest` (named in the T13 tests) | Unchanged; the new phase is covered by `CircusArenaTest.theClosingBeatIsAPhaseOfItsOwn`. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,186 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B31-unit2.txt`). The first run failed `RetiredNamesGuardrailTest` on `circus_carousel` in the sound ids; renamed to `impaled_procession`.
- `./gradlew.bat runCrimsonTroupeGameTestServer`: "All 59 required tests passed :)" (`build/build2/B31-troupe1.txt`). B30 had 44.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 2012 required tests passed :)" (`build/build2/B31-gametest.txt`, `B31-gametest-latest.log`). B30 had 1,997.
- `./gradlew.bat runPlaythroughGameTestServer`: "All 440 required tests passed :)", 17 optional failures, every one in the baseline list (`build/build2/B31-playthrough-optional.txt`).
- Added JVM: `CircusRingmasterMovesTest` (nine cases: every wind-up at or above its floor, the five moves on the table, the Board and staff footprints, the lent acts, looking away, the RAFTERS, CAROUSEL and DESCENT pickers) and `CircusArenaTest` (the leash and grace; CURTAIN stays CURTAIN).
- Added 15 required GameTests in `CircusRingmasterFinaleGameTests`: the opening holds everything 60 ticks; the bar's troupe and anchor objectives; the Ringmaster is the one writer of DESCENT and no thread is cut in RAFTERS; a reload mid-RAFTERS keeps the opening and one mid-DESCENT keeps the lent acts; a straying owner keeps the site 199 ticks and loses it at 200; staff, Knife Board (and a dash breaking it), Spotlight (only looking away roots), Sleight (two doubles; struck in the wind-up it breaks), Cue (two performers strike on the called game tick), Strings Cut (used once) and the knife each land on their commit tick and not before, and the staff spares a bystander in its wedge; the Pattern arrives only when the yield ends; a relog during the yield waits and the returning owner gets the Pattern; Liberation's collapse ends in the ruin.
- Rewritten on purpose: `PlaythroughCrimsonTroupeGameTests.liberationFinale` now waits for CAROUSEL before cutting threads (severing and anchors answer only there) and waits for the Ringmaster's tick to write DESCENT (one phase writer, B-7).
- Refreshed on purpose: `src/gameTest/resources/frozen/registry-ids.txt`, seven `minecraft:sound_event` lines added (the six cues and the music), from a run with `HEMOMANCY_FROZEN_SNAPSHOT_OUT=build/build2/B31-frozen`. Nothing else differed.
- Review client: `runCircusReviewClient -PcircusReviewOnly=succession,liberation`. On the reused `CircusB29` save Succession stuck in CAROUSEL: that save holds many leftover performers in the ring (the bar read 12 of 13 standing) and the review downed only those in its own 20-block box. The review now downs the arena's cast (`CircusClientReview` `troupe`). On a fresh world (`{"op":"create","name":"CircusB31","seed":12345}`) both finales settled: Succession went to DESCENT at frame 371, CURTAIN at 567 and settled at 626; Liberation went to DESCENT at 391, CURTAIN at 587 and RUIN at 626 (1,448 frames). Every launch got past the Accessibility screen and closed itself; no Java process was left.
- `python gradle/source-text-impact-map.py`: not run; no source-text test changed.

### Docs

- `docs/CIRCUS.md`: the finale presentation section rewritten (the kit, the move table, the closing beat, sound), the telegraph and stuck-state paragraphs, rulings D-18/D-33, D-33 (boss definition), D-36, D-36 (audio), NQ-15-3 A, NQ-15-4 A and NQ-15-11 under "Rulings that bind", the B31 calls, the open-questions pointer, proof owed, history and the status line.
- `docs/BOSSES.md` (the Ringmaster on the kit, rulings, pointer, history), `docs/HEMOMANCY_REFERENCE.md` 17.1 (the finale on the boss kit), `docs/LORE_REFERENCE.md` 6.3b (the yield), `docs/NPC_VOICES.md` (the yield line on the Ringmaster sheet), `docs/audits/2026-11-13-sound/entities_calls.md` (Circus rows taken), `wiki/Crimson-Troupe.md` (the fight and the yield).
- `docs/OPEN_QUESTIONS.md`: deleted section 1 rows NQ-15-3 and NQ-15-4; section 2 rows NQ-15-11/D-36, D-33 (boss definition) and D-36 (audio); section 3 rows 15 T13, T14, T15 and T19. Section 4: D-36 (audio) is now "now", with the candidates listed; added "B31 finale" and "B31 lines"; the Circus play session no longer waits on T15. Left for its own area: the "Ringmaster" row under "Line work by speaker" (the yield line it waits for is now written) and the Sound S5 row's Circus sentence.
- `docs/owner_answers/15-circus.md`: status lines T13, T14, T15 and T19.

### For the owner

- New lang keys: `hemomancy.circus.ringmaster.yield` "Enough. The hat's off, and it's yours. Go on, call the next act." (Ringmaster sheet: short and final); `hemomancy.circus.ringmaster.says` "The Ringmaster: "%s""; `hemomancy.circus.finale.leash` "You've left the ring. Go back to it, or the finale ends without you."; `hemomancy.circus.finale.spotlight` "You looked away from the Ringmaster, and the spotlight holds you where you stand."; `hemomancy.circus.finale.bar.troupe` "The Ringmaster: %s of %s performers standing"; `hemomancy.circus.finale.bar.anchors` "The Ringmaster: %s of %s anchors left"; `boss.hemomancy.circus_yield.name` and `.epithet` "The Ringmaster", "Yields the Ring"; `boss.hemomancy.circus_collapse.name` and `.epithet` "The Ringmaster", "The Threads Let Go"; seven subtitles: `subtitle.hemomancy.entity.circus_ringmaster.{phase_change,finale_collapse,hurt,music}`, `subtitle.hemomancy.entity.impaled_procession.{thread_sever,anchor_break}`, `subtitle.hemomancy.entity.circus_performer.windup`. No key changed.
- Values I chose: every move number in the Built table; the arena leash 24 and grace 200 (the old finale leash was 64 blocks; acts already failed at 24); yield 60, collapse 40 and the 30-tick pause before he vaults home; the topper staying off at a Succeeded site; all candidates of an event as variants until you pick.
- Looks to judge, in `build/build2/B31-shots/`: `succession-00030.png` and `-00120.png` (the opening and the rafters), `succession-00250.png` (the CAROUSEL bar, "3 of 4 performers standing"), `succession-00450.png` and `-00500.png` (DESCENT), `succession-00585.png` (the yield line), `succession-00610.png`, `succession-00660.png` (back on his perch after the yield), `liberation-00250.png`, `-00450.png`, `-00600.png`, `-00615.png` (the "Threads Let Go" card with a thread letting go), `-00680.png`; from the first run, `run1-succession-spotlight-01999.png` (the Spotlight's line). All frames: `build/circus-review/screenshots/` (`succession-*`, `liberation-*`). The review does not stage each mental move on its own, so Sleight's doubles and the Knife Board may not be in any frame.
- Surprising: the reused `CircusB29` review save has many leftover performers in its ring, so a finale there counts them all; use a fresh world for finale reviews. The large pink diamonds noted in B27's verification still show in some frames (`succession-00585.png`); they are not from this package. The sounds are CC0 Freesound clips (their ids are in the D-36 (audio) row of section 4).

### Verification (round 1)

Checked the tree against `refs/build2/B31-before` and `build/build2/B31-status-before.txt`. Pass.

- Scope: only Circus finale, boss bar, sound and doc files changed. Nothing from the held list, no rename of Silent Severance, no lash numbers, no HUD files. Nothing struck by NQ-15-3 A was built.
- Values: the five NQ-15-4 A moves are in `CircusRingmasterMoves` beside the knife and staff blow; wind-ups 12 melee and 20 area. NQ-15-11: after the yield he vaults back to his perch and keeps trading and teaching. D-33: his row is in `hemomancy:bosses`. D-36: seven sound events, six cues with the audit candidates and an empty music event.
- Tests: `CircusRingmasterMovesTest`, `CircusArenaTest` and 15 required `CircusRingmasterFinaleGameTests` cover each row. The one rewritten test (`PlaythroughCrimsonTroupeGameTests.liberationFinale`) now waits for CAROUSEL and for DESCENT; it still checks the same outcome. Nothing was weakened or skipped.
- Unit suite: BUILD SUCCESSFUL, 6,186 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B31-v1-unit.txt`).
- Combined GameTest run: "All 2012 required tests passed :)" (`build/build2/B31-v1-gametest.txt`).
- `runCrimsonTroupeGameTestServer` twice: "All 59 required tests passed :)" both times (`B31-v1-troupe1.txt`, `B31-v1-troupe2.txt`).
- Review client: `runCircusReviewClient -PcircusReviewOnly=succession,liberation` on a fresh world `CircusB31V1`. It got past the Accessibility screen. Succession reached CURTAIN at 567 and settled at 626; Liberation reached RUIN at 626; 1,448 frames; the client closed itself and no Java process was left. Frames in `build/build2/B31-v1-shots/`: contact sheets (`succession-sheet.png`, `succession-rafters-sheet.png`, `succession-descent-sheet.png`, `liberation-sheet.png`, `liberation-rafters-carousel-sheet.png`) and single frames (`succession-00140`, `-00170`, `-00448`, `-00585`, `-00660`, `liberation-00162`, `-00600`, `-00615`).
- Not blocking: the review does not stage each move on its own and logs no move names, so I could not save one frame per move telegraph. In the frames the floor footprints read as thin bone and crimson motes at the review camera's distance, fainter than the "bold" bar for focal moves. The owner should judge them in play (section 4 row "B31 finale"). A review scene per move would settle it.
- Docs: rulings are under "Rulings that bind" in `docs/CIRCUS.md`; only this package's rows left `docs/OPEN_QUESTIONS.md`; chunk 15 status lines match the tree.

## B32. Dialogue: shared keys, lesson corrections, trade card titles

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 16 T2 (FX-15) leftovers | A topic card's title wraps; the card grows 12 px for each extra title line (`DialogueTopicCardLayout.height(base, titleLines, summaryLines)`, `titleWidth`, `calculate(card, status, titleLines)`). `DialogueScreen.hubLayout()` is the hub as drawn (cards grown to the longest summary); `MonolithHubClientReview` clicks the Important tab from it. `NpcTradeClientReview` sets the Alchemist's rapport to the Trusted floor instead of adding to the kept save's rapport. Unit cases for hub and topic card growth. | `client/screen/dialogue/DialogueTopicCardLayout.java`, `DialogueScreen.java`, `gametest/MonolithHubClientReview.java`, `gametest/NpcTradeClientReview.java` |
| 16 T3 (CD-7), Unstained | Nine Observance prompts, `hemomancy.dialogue.<acolyte\|zealot\|guardian>.observance.<available\|active\|ready>`, each in its speaker's voice. `UnstainedObservanceDialogueDecorator.promptKey(issuer, accepted, ready)` picks the key; the Acolyte's own trees name her key. The three shared keys are removed. | `UnstainedObservanceDialogueDecorator.java`, `AcolyteDialogueTrees.java`, `en_us.json`, `dialogue_briefs/unstained_church.json` |
| 16 T3 (CD-7), Circus | Per-role keys `hemomancy.circus.school.<role>.status.<step>`, `.aftermath.<route>` and `.preparation` for the eight faculty roles, built by `CircusSchoolDialogue.statusKey` and `.aftermathKey`. Only reachable steps have a key: 5 status lines for each of the 7 teaching professors, `status.preparation` for the Threadkeeper, 5 aftermath lines and 1 preparation line each: 84 lines (the plan's 96 counted 12 status keys no greeting can reach). The Ringmaster's school node also used the shared aftermath key, so he has his own 5 (`circus.school.ringmaster.aftermath.<route>`). The 12 shared keys are removed. | `CircusSchoolDialogue.java`, `CircusRingmasterEntity.java`, `en_us.json`, `dialogue_briefs/circus_monolith.json` |
| 16 T3 (NQ-16-5) | `hemomancy.clinical.inquiry.later` is three keys, `clinical.inquiry.alchemist_later`, `.artificer_later`, `.mnemonist_later`; the 18 inquiry files (11 Alchemist, 4 Artificer, 3 Mnemonist) point at their own. | `data/hemomancy/dialogue_inquiry/<speaker>/...`, `en_us.json`, `dialogue_briefs/artificer_anchorite_clinical.json` |
| 16 T5 (CN-5), false today | Rewritten to what P03 and the 11-12 cut built: the Artificer's `strand_line.reach.line1`, `.crack.line1`, `.twin.line1` teach the lash only (the twin trails at half damage and never cracks); `staff.line1` (blood poured into a tank, basin or station grows no ring); the Mnemonist's `strands.line2` (a click with nothing held taps, a click on a gathered or swinging lash cracks it) and `land.dark.line2` (only the caster is hidden); the Alchemist's `temper.catalysts.line1` (the smallest catalyst that covers the shortfall goes, the off hand wins a tie). `.line2` already read "on your bandolier" (B12), so it kept its text. `alchemist.enzyme_uses.temper` said the same old order and was fixed with it. | `en_us.json`, `dialogue_briefs/*.json` |
| 16 T5 (CN-5), ruling-driven lines | 11 of the 14 were already rewritten by B07 (living blood, three), B09 (hot runs three, Furnace Veins, gouge, vein, meats) and B10 (humours). The three left: `alchemist.craft.temper.line2` (Humilis 2, Mediocritas 3.5, top rank 8; held forms 0.5 to 2 a second by price; caps 20 and 80, LP-14), `.temper.fill.line1` (a full body still takes the Temper, the blood it cannot hold is lost, CQ-9), `vicar.craft.day.line2` (about two millilitres a second, CB-1). | `en_us.json` |
| 16 T5 (UN-9, UN-10) | The Artificer's `hot_runs.line1` and `hot_runs.feeder.line2` reworded apart from the Vein-Mason's gouge lines; `alchemist.craft.beds.timber.line1` gives the recipe (one voyager's log and eight logs make eight timber) instead of the tooltip; `artificer.craft.climb.line.line1` stops repeating Tendon Line's text. Every lesson card shows `hemomancy.dialogue.topic.craft.summary` (`CoreLoopLessons.CARD_SUMMARY`, chosen in `DialogueHubFactory`). | `CoreLoopLessons.java`, `DialogueHubFactory.java`, `en_us.json` |
| 16 T5 (UN-69) | `CoreLoopLessonNumbersTest`: 125 mL crossing (`EarthenVeinTravelRules.LIVING_VEIN_COST`), 12 rampart blocks (`FerricRampartSculpting.MAX_SEGMENTS`), ring thresholds (`LivingStaffGrowthRules.threshold`), twin half again (`StrandSkills.TWIN_COST`), 6 mL root (`ClingRules.ROOT_BLOOD`), cast burns by rank and held burns by band (`TemperRules.castCost`, `burnPerSecond`), caps 20 and 80, ten per Will tier, half lost on death. | `src/test/.../dialogue/CoreLoopLessonNumbersTest.java` |
| Line work: Vicar (CD-9 leftover) | `vicar.votary.temper_lore` states the eight tempers instead of asking and answering. | `en_us.json` |
| Line work: Alchemist, Brined Court, successors (UN-66) | The option reads "Tell me about the Cerebral Scarring Station."; the Kindled elder's newcomer line drops "Land first"; the `recruit_offer` node shows only the consider line, and the accept line is spoken after a successful pledge (`RecruitmentDialogueEvents.acceptLine`). The Vicar's and the Alchemist's accept lines were rewritten for after the pledge; the Mnemonist's already read that way. | `HarbingerAlchemistDialogueTrees.java`, `HarbingerVicarDialogueTrees.java`, `HarbingerMnemonistDialogueTrees.java`, `route/RecruitmentDialogueEvents.java` |
| Line work: Brined Court roles | The Court newcomer lines that opened on "new to the blood" rewritten in each speaker's voice: Arbor, Bellwarden, Braced elder, Host, Lamp-colony, Lightkeeper, Linesman, Marrow-tender, Nursery tender, Rooted elder, Veiled resident (11; the row said nine). | `en_us.json`, `dialogue_briefs/court.json` |
| Section 1 CQ-50 to CQ-54 | "Keep all five." Recorded; nothing to build. | `docs/NPC_DIALOGUE_AND_TRADE.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| 16 T5 (CN-5), the stain branch | Held (CQ-25, CQ-45). Not in this package. |
| Line work: Vicar PT-1 | The D1 hint that points at the blood font waits on the font, which B21 skipped. |
| Line work: Alchemist DS-5 | Advanced brewing is parked (NQ-17-4). |
| Line work: Vein-Mason PT-30 | The trade card is still blocked by the one-want-per-speaker rule (B21). |
| Line work: Crimson Troupe faculty D-31 | Whether a professor refuses an Unstained pupil is not ruled; B27 gated only the Ringmaster's pact. No line written. |
| Line work rows already written by others | Hermit PT-8 (B21 system line), Monolith PT-69 (B22), Ringmaster D-36 (B31 yield line), faculty D-44 (B30 gift line), successors PT-9 (B21) and PT-35 (B22), no-speaker PT-26 (B21) and PT-37 (B22), the Vicar's D4 hint (B21), PT-15 (B21), PT-83 (B24), CL-14 (B09), `found_fane` (B22), the Alchemist's commission line (B14), the Artificer's cleats lines (B11). Rows deleted, no new text. |
| A wrapped topic title in a shot | No review scene has a title long enough to wrap. The layout is covered by unit cases. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: first run 1 failure, `EnglishLocalizationEncodingTest.authoredDialogueActionPromptsHaveEnglishText` (mine: `AcolyteDialogueTrees` still named the removed shared Observance key; now the Acolyte's own). Final run BUILD SUCCESSFUL, 6,193 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B32-unit3.txt`).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 2012 required tests passed :)" (`build/build2/B32-gametest.txt`). The route fingerprints did not change: the recruit routes are refused before the pledge, so the accept line is not in them.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: first run 1 required failure, `understudyMnemonistPuppet` (mine: it looked for the shared `circus.school.status.lesson`). Rewritten on purpose to the Understudy's own `circus_understudy.status.lesson` and `.status.ordeal`. Second run "All 440 required tests passed :)", 17 optional failures, the same names as B31 (`build/build2/B32-playthrough-optional.txt`).
- `./gradlew.bat runCrimsonTroupeGameTestServer --offline --console=plain`: "All 59 required tests passed :)".
- Added: `CoreLoopLessonNumbersTest` (UN-69: each spoken number against its constant); `CircusSchoolDialogueKeysTest` (every reachable professor key and the Ringmaster's aftermath keys exist, no two share a line); `UnstainedObservanceDialogueDecoratorTest.eachIssuerSpeaksItsOwnObservanceLines` (nine keys, nine different lines); `RecruitOfferNodeTest` (the offer shows only the consider line); `DialogueTopicCardLayoutTest` two cases (title and summary growth, a two-line title pushes the summary down); `DialogueHubLayoutTest.cardsGrowToTheLongestSummaryAndTheImportantTabFollowsThem`.
- Changed: `CoreLoopLessonsTest.theHubFilesEveryLessonInConversationAsUnreadLore` also checks the lesson card summary (UN-10). `PlaythroughCrimsonTroupeGameTests.understudyMnemonistPuppet` reads the Understudy's own status keys (CD-7).
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv`.
- `runNpcTradeReviewClient` with `{"op":"open","name":"NpcTradeReview"}`: got past loading, 33 shots, every check PASS, including "the Alchemist's board reads Trusted", which failed on every earlier run of this save. The client stopped itself. Shots in `build/build2/B32-shots/` (old ones moved to `build/build2/B32-old-trade-shots/`). No game client or server is left running.

### Docs

- `docs/NPC_DIALOGUE_AND_TRADE.md`: rulings CN-5 (built, less the stain branch), CQ-50 to CQ-54, NQ-16-1, NQ-16-2, UN-9, UN-10, UN-69 with UN-22, CD-7, NQ-16-5, FX-15 (title wrap), the CD-9 leftover and UN-66 under "Rulings that bind"; the calls line for CQ-50 to CQ-54 marked kept; "Open questions" no longer lists CQ-50; the known gaps rewritten (only the stain branch and D-31 left); a history line.
- `docs/NPC_VOICES.md`: open decision 11 Applied, decision 14's leftover closed, the Alchemist's and Artificer's fixed-text notes, an Observance note on the Zealot, Acolyte and Guardian sheets, the faculty sheet's per-role keys.
- `docs/HEMOMANCY_REFERENCE.md`: the core-loop lessons paragraph (card summary, lash-only lines, the numbers test) and the recruitment bullet (accept line after the pledge). `docs/LORE_REFERENCE.md`: no lore changed.
- `docs/OPEN_QUESTIONS.md`: section 1 row CQ-50 to CQ-54 deleted; section 2 rows NQ-16-1, NQ-16-2, NQ-16-5, UN-9, UN-10, UN-66, UN-69/UN-22 and FX-15 (titles) deleted; section 3 rows 16 T2, the three 16 T3 rows, 16 T5 false today, 16 T5 (UN-9, UN-10), 16 T5 (UN-69) and 16 T5 ruling-driven lines deleted; in "Line work by speaker" the Artificer, Hermit, Monolith, Brined Court, Ringmaster, Zealot/Acolyte/Guardian, successors and no-speaker rows deleted and the Vicar, Alchemist, Mnemonist, Vein-Mason and faculty rows shortened to what is left; the area's count row reads 10 / 5 / 5. Section 4 gained "B32 (lines)".
- Chunk 16 status table: T2, T3 and T5 updated.

### For the owner

- New keys (each written to its speaker's sheet): `hemomancy.dialogue.acolyte.observance.available`, `.active`, `.ready`; the same three for `zealot` and `guardian`; `hemomancy.circus.school.<role>.status.degree`, `.lesson`, `.ordeal`, `.practical`, `.mastered` for `circus_acrobat`, `circus_knife_thrower`, `circus_stilt_walker`, `circus_strongman`, `circus_beast_tamer`, `circus_fire_eater`, `circus_understudy`; `hemomancy.circus.school.circus_threadkeeper.status.preparation`; `hemomancy.circus.school.<role>.aftermath.neutral`, `.succession`, `.liberation`, `.succession_complete`, `.liberation_complete` for the eight roles and for `ringmaster`; `hemomancy.circus.school.<role>.preparation` for the eight roles; `hemomancy.clinical.inquiry.alchemist_later`, `.artificer_later`, `.mnemonist_later`; `hemomancy.dialogue.topic.craft.summary`.
- Removed keys (copies in `removed_assets/lang/B32-removed-en_us.json`): `hemomancy.clinical.inquiry.later`, `hemomancy.dialogue.unstained.observance.available`, `.active`, `.ready`, `hemomancy.circus.school.status.degree`, `.lesson`, `.mastered`, `.ordeal`, `.practical`, `.preparation`, `hemomancy.circus.school.preparation`, `hemomancy.circus.school.aftermath.liberation`, `.liberation_complete`, `.neutral`, `.succession`, `.succession_complete`.
- Changed values: `hemomancy.artificer.craft.strand_line.reach.line1`, `.crack.line1`, `.twin.line1`, `artificer.craft.staff.line1`, `artificer.craft.hot_runs.line1`, `artificer.craft.hot_runs.feeder.line2`, `artificer.craft.climb.line.line1`; `hemomancy.mnemonist.craft.strands.line2`, `mnemonist.craft.land.dark.line2`; `hemomancy.alchemist.craft.temper.line2`, `.temper.fill.line1`, `.temper.catalysts.line1`, `alchemist.craft.beds.timber.line1`, `alchemist.enzyme_uses.temper`; `hemomancy.vicar.craft.day.line2`, `vicar.votary.temper_lore`; `hemomancy.dialogue.alchemist.option.tell_me_about_chisel_station`; `hemomancy.dialogue.recruit.alchemist.accept`, `recruit.vicar.accept`; `hemomancy.dialogue.newcomer.court_arbor`, `_bellwarden`, `_braced_elder`, `_host`, `_lamp_colony`, `_lightkeeper`, `_linesman`, `_marrow_tender`, `_nursery_tender`, `_rooted_elder`, `_veiled_resident`, `_kindled_elder`.
- My calls: the lesson card summary reads "How the work is done, one question at a time."; the Circus keys cover only the steps a greeting can reach (no dead keys), so 84 professor lines, not 96; the recruit accept line is a chat line after the pledge succeeds, so a refused pledge never hears it; "a click with nothing held taps once" is how the Mnemonist now names the lash tap.
- Surprising: the Ringmaster's school node also read the shared aftermath key, so he got five lines of his own. `alchemist.enzyme_uses.temper` told the same old catalyst order as the lesson, so it changed too. The Court row said nine newcomer lines opened on "new to the blood"; eleven did.
- Look: no screen changed in a way the review shots show. The trade review's Conversation shots are in `build/build2/B32-shots/` (`npc-trade-10-alchemist-conversation-trusted.png`, `npc-trade-13-zealot-conversation.png`).

### Verification (round 1)

- Scope: the diff against `refs/build2/B32-before` and the three new tests stay inside the brief. No lash numbers, HUD, held manipulation or held row (stain branch, DS-5, PT-30, D-31) was touched. Silent Severance is not renamed. New lines use Sculpting and have no em dashes.
- Values: the Temper line (2, 3.5, 8; 0.5 to 2 a second; caps 20 and 80) matches `TemperRules` and is pinned by `CoreLoopLessonNumbersTest`. The catalyst lines match `TemperCatalysts` (best fit, off hand first). The day line matches the 2 mL a second in the reference (CB-1). The recruit accept line now shows only after a pledge succeeds; the offer node shows only the consider line.
- Tests: each row has a test that would fail without it (`CoreLoopLessonNumbersTest`, `CircusSchoolDialogueKeysTest`, `RecruitOfferNodeTest`, the new Observance, topic card and hub layout cases, the lesson card summary check). The one changed GameTest (`understudyMnemonistPuppet`) was rewritten to the Understudy's own keys, not weakened.
- Unit suite: BUILD SUCCESSFUL, 6,193 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B32-verify-unit.txt`).
- Combined GameTest run: "All 2012 required tests passed :)" (`build/build2/B32-verify-gametest.txt`).
- `runDialogueRouteGameTestServer`: "All 1 required tests passed :)" (`build/build2/B32-verify-route.txt`).
- Rules: removed keys are copied to `removed_assets/lang/B32-removed-en_us.json`. All new text is in `en_us.json`. No payload, blood, delivery or particle change.
- Docs: only this area's rows left `docs/OPEN_QUESTIONS.md`; rulings are under "Rulings that bind" in `docs/NPC_DIALOGUE_AND_TRADE.md`. No client launched: the trade review shots in `build/build2/B32-shots/` show the dialogue screen drawing, and no scene has a wrapping title. No server or client left running.
- Result: pass, tree green.

## B33. Mnemonist inquiries for every memory

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 16 T4 (CD-14) | 41 new Mnemonist inquiry files, one for each memory item that had none (Memory Thread is a material and is left out). Two lines each, `hemomancy.mnemonist.item_inquiry.memory_<id>.line1/.line2`, written from each memory's `.desc` and its Projection or Absorption text as they read today. Blood Needle Fan is a charged cast only; Blood Needle Lance holds as up to six hovering needles; Crimson Flame Conjuration kindles and paints flame; Tendon Line, Lignum Mortis, Carrion Communion and Furnace Veins say what the held verb does. Memory of Vesper gives the Brazier steps and does not name where it leads. | `data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_*.json`, `en_us.json`, `dialogue_briefs/mnemonist_antecedent.json` |
| 16 T4 (NQ-16-4) | The 24 files that pointed at the generic `memory_general` lines now point at their own two lines. The two generic keys and their briefs are removed. | the same files, `removed_assets/lang/B33-removed-en_us.json` |
| 16 T4 test | `MnemonistMemoryInquiryCoverageTest`: every `item.hemomancy.memory_*` name except `memory_thread` has a Mnemonist inquiry, its line keys are its own, they exist, and no line repeats another memory's. | `src/test/.../dialogue/MnemonistMemoryInquiryCoverageTest.java` |

Written in batches by school: Animus 20, Ductilis 8, Ferric 6, Flammeus 9, Congeatio 6, Mortem 8, Tenebris 5, Lux 2, then the Memory of Vesper. 130 lines, 130 briefs.

### Skipped

| Row or part | Why |
| --- | --- |
| None | |

### Tests

- First run of the new test alone, before any content: red, 41 "no Mnemonist inquiry file" and 24 "speaks another entry's line" (`build/build2/B33-red.txt`).
- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,194 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B33-unit.txt`). After B32: 6,193, plus the new test.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 2012 required tests passed :)" (`build/build2/B33-gametest.txt`). Same count as B32.
- Added: `MnemonistMemoryInquiryCoverageTest` (CD-14 and NQ-16-4: the gap and the generic answer cannot come back). No test changed or flipped. `ItemInquiryResourceValidationTest` and `DialogueInquiryCanonTest` stay green.
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv` (one row added).
- No client launched: no look, screen or shader changed. No server or client is left running.

### Docs

- `docs/NPC_DIALOGUE_AND_TRADE.md`: status line, the inquiry count (439 files, counted 2026-11-16), CD-14 marked built and NQ-16-4 recorded under "Rulings that bind", the two known gaps (generic lines, stale count) removed, a history line.
- `docs/HEMOMANCY_REFERENCE.md`: the item inquiry paragraph (439 files, one Mnemonist inquiry per memory, the coverage test). `docs/LORE_REFERENCE.md`: no lore changed. `docs/NPC_VOICES.md`: no line is pinned, so nothing changed.
- `docs/OPEN_QUESTIONS.md`: section 2 row NQ-16-4 and section 3 row 16 T4 deleted; the Mnemonist line-work row shortened to the stain branch; the area's count row reads 9 / 4 / 5. Section 4 gained "B33 (lines)".
- Chunk 16 status table: T4 updated.

### For the owner

- New keys, two per memory (`hemomancy.mnemonist.item_inquiry.memory_<id>.line1` and `.line2`), by school:
- Animus (20): new `expansive_blood_cloud`, `pursuing_blood_cloud`, `sanguine_tempest`, `blood_lattice`, `chain_blood_binding`, `lingering_blood_binding`, `crimson_coronation`, `guided_blood_shot`, `hematic_mortar`, `sanguine_halo`, `summon_avatar_arms`, `summon_avatar_armor`, `summon_avatar_legs`, `summon_avatar_complete`, `tendon_line`; off the generic lines `blood_aneurysm`, `blood_cloud`, `summon_avatar`, `vital_effusion`, `crimson_tithe`.
- Ductilis (8): new `activation_potential`, `living_circuit`, `sovereign_instinct`, `synaptic_storm`, `thread_ripper`, `sanguine_marionette`; off the generic lines `hemolymphal_pulse`, `sanguine_ward`.
- Ferric (6): new `blood_needle_fan`, `blood_needle_lance`, `iron_choir`; off the generic lines `blood_needle`, `sanguine_mending`, `vascular_dowsing`.
- Flammeus (9): new `crimson_flame_conjuration`, `expulsive_updraft`, `soaring_updraft`, `suspended_updraft`, `furnace_veins`, `phoenix_debt`; off the generic lines `pyretic_forge`, `sanguine_ignition`, `vitric_combustion`.
- Congeatio (6): new `absolute_stillness`, `rimebound_sentence`; off the generic lines `cryogenic_pulse`, `glacial_grasp`, `osseous_bloom`, `endless_hour`.
- Mortem (8): new `canopy_mortis`, `carrion_communion`, `funeral_bell`, `lignum_mortis`, `worked_lignum`; off the generic lines `exsanguinate`, `hemorrhage`, `bloom_of_rot`.
- Tenebris (5): new `eclipse_well`, `penumbral_drift`; off the generic lines `blood_eclipse`, `umbral_step`, `void_shroud`.
- Lux (2): new `white_verdict`; off the generic lines `unclosing_eye`.
- Vesper (1): new `of_vesper`.
- Removed keys: `hemomancy.mnemonist.item_inquiry.memory_general.line1`, `.line2` (copies in `removed_assets/lang/B33-removed-en_us.json`).
- My calls: the four Canon Memory lines name the Saint, the Somatic Loom, the eighth Degree and "only if you ended his (her) trial yourself"; Putriciel, Hemorath and Velorum are "his", Seraphae "her", from the lore reference. The Memory of Vesper's second line ends "I'll write down what it does to you after, not before." so the Mnemonist states no ending. Thread Ripper is called Liberation's reward at the Crimson Troupe's pavilion. Lines avoid questions, em dashes and the words Shaping and Sculpting.
- Surprising: nothing. The plan's 41 and 24 matched the tree exactly.

### Verification (round 1)

- Scope: the diff against `refs/build2/B33-before` touches only the 24 Mnemonist memory inquiry files, 41 new ones, `en_us.json` (130 keys added, the two `memory_general` keys removed), the Mnemonist briefs, the new test, the impact map and the docs named above. Nothing held or reversed, no lash numbers, no HUD, no Java source.
- Values: every `item.hemomancy.memory_*` name except Memory Thread (104) has its own file and two own lines. Spot checks hold against the code: Blood Needle Lance `MAX_NEEDLES = 6` (`NeedleVolleySculpting`), Tendon Line has only Projection and Absorption Sculptings, Crystalline Bloom names the item as it is shown. All 130 new keys have briefs. No line uses Shaping. `memory_general` is named nowhere under `src/`; its copy is in `removed_assets/lang/B33-removed-en_us.json`.
- Tests: `MnemonistMemoryInquiryCoverageTest` fails on a missing file, a borrowed key or a repeated line, so it fails without this work. No test changed.
- Unit suite rerun: BUILD SUCCESSFUL, 6,194 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B33-verify-unit.txt`), the inquiry tests included.
- Combined GameTest rerun: "All 2012 required tests passed :)" (`build/build2/B33-verify-gametest.txt`).
- Docs match the tree. Only NQ-16-4 and 16 T4 left `docs/OPEN_QUESTIONS.md`.
- Note, not blocking: five older memory inquiries (`blood_lash`, `hematic_hewing`, `living_land`, `lode_draw`, `venous_cling`) still have no briefs. They were outside this package.
- Verdict: pass. No server or client left running.

## B34. Liber pages

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 16 T6 (DS-9), Armature | New chapter `armature` ("The Hematic Armature", ordinality 11, after Vial Separation). Pages: The Armature Rack (opens at D2, on picking up a `hematic_armature`, or on the advancement "The Rack Listens"), Armature Consecration and The Monolithic Armature, both through `registerUpgradeLesson` (personal eligibility or the rite's completion, like the other nine lessons). The two `registerRite` rows that pointed the Armature rites at Bloodwork Stations are gone; that page now says the Armature has its own chapter. Numbers read from code: 8,000 mL reservoir, 100 ticks a piece, 250 mL cleats, 2 × 50 mL and 3 × 50 mL circuits, the six offerings of each rite file. | `books/libersanguinium/armature/`, `LiberEntryDefinitions.java`, `LiberKnowledgeHelper.java`, `LiberDiscoveryEvents.java`, `the_hematic_order/pages/bloodwork_stations.json` |
| 16 T6 (DS-9), crafts | 17 pages in The Hematic Order (ordinality 24 to 40), one per row of `LiberEntryDefinitions.CRAFT_PAGES` (page, rite, Degree): Scarlet Vanity, Mycelial Lantern, Mnemonic Reliquary, Somnolent Seat (D3); Fungal Implantation Pylon, Mason's Effigy, Cerebral Scarring Station (D4); Dendritic Distributor, Morphling Incubator, Vacant Effigy, Sanguine Monolith (D5); Covenant Throne, Sanguine Vigil (D6); Sporitic Thurible (D8); Dried Gourd (D1, its craft is D0), Hematic Stake (D5), Mycelial Synapse (D8). Each opens at that Degree, on picking up what it makes, or when its Working craft completes. The "how it is raised" sentence of each Working craft page was generated from its recipe file (tier, medium, offerings, feed). The four crafts that had pages already (Forge, Scriptorium, Loom, Cradle) keep them. | `the_hematic_order/pages/*.json` (17 new), `LiberEntryDefinitions.java` |
| 16 T6 (DS-9), Thurible page | Written as the D8 Apotheos relic B23 made it: only an Apotheos lights it or keeps it burning, the floor raises it for no one else, opens at D8. | `the_hematic_order/pages/sporitic_thurible.json` |
| 16 T6 (DS-9), bosses | Pages Vesper, The Mycophant and Naeglerophaeon in Cosmic Forces (ordinality 10 to 12). New discovery source `HemomancyDiscoverySource.ENCOUNTER` and `LiberKnowledgeHelper.unlockForEncounter`, called where each fight begins: Vesper's arrival (`VesperOrdealManager.spawnCrownedRefusal`), the Nursery entry (`MycophantEncounterManager`, waking and rematch), the Naeglerophaeon opening (`VagrantMindEncounterManager`) and the Phlegethosoma's summons (`PhlegethosomaEntity.answerSummons`; its page, written by B14, keeps its D5 floor too). Numbers read from code: three wounds, up to three puppets, the split at 60%, the Last Brood under 25%, the hunt about 15 minutes. | `cosmic_forces/pages/`, `HemomancyDiscoverySource.java`, `LiberKnowledgeHelper.java`, the three encounter managers, `PhlegethosomaEntity.java` |
| NQ-16-3 A, 16 T6 Court pages | New chapter `brined_court` ("The Brined Court", ordinality 4, between The Unstained and Cosmic Forces): The Eight Houses, The Bloom Warden, The Kindled Champion. The houses page opens when the player comes to know the Court by any route, finding a Court site included (`CourtDiscoveryProgress.markKnown`; the unlock runs before the "already known" return, so a player who knew before gets it from the next route). The Warden page opens at `BloomWardenEntity.wake`, the Champion page at `KindledChampionEntity.callOut`. | `books/libersanguinium/brined_court/`, `CourtDiscoveryProgress.java`, `BloomWardenEntity.java`, `KindledChampionEntity.java` |
| DS-9 (page homes), section 2 | Built as written: the Armature in its own chapter, craft pages in The Hematic Order, bosses in Cosmic Forces. | as above |

### Skipped

| Row or part | Why |
| --- | --- |
| none | |

### Tests

- Targeted first run: `LiberCraftAndBossPagesDataTest`, `LiberPageRegistrationGuardrailTest`, `HarbingerRiteLiberRowDataTest`, `BookResourceBindingTest` and `MemoBookPresentationTest` pass (`build/build2/B34-unit-targeted.txt`).
- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL, 6,199 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B34-unit.txt`; again after the docs, `B34-unit2.txt`). B33 had 6,194; five added.
- `./gradlew.bat runGameTestServer --offline --console=plain`: first run 1 failure, mine: `everydialogueroutekeepsitsoutcome`. Line 284 (`voyager_court_learn`) of `src/gameTest/resources/dialogue/route-fingerprints.txt` now carries the saved Liber knowledge with the Court houses page; no other line differed. That one line was refreshed on purpose. Second run: "All 2013 required tests passed :)" (`build/build2/B34-gametest2-latest.log`). B33 had 2,012; one added.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain`: "All 440 required tests passed :)", 17 optional failures, none outside the baseline list (`build/build2/B34-playthrough-latest.log`).
- Added: `LiberCraftAndBossPagesDataTest` (five tests: every Working craft has a page and the roster is 18; each craft page exists, opens at its craft's Degree and names what the craft makes; both Armature rites open the `armature` chapter; each boss page has the `ENCOUNTER` opener and a file; the Court chapter holds its three pages). It is written against the new symbols, so it does not compile without this package. `CourtDiscoveryGameTests.theCourtChapterOpensAsTheCourtIsMet` (in the combined run): no Court page before the Court, the houses page with it, the boss pages only once the Warden wakes and the Champion is called.
- Review harness: `LiberPagesClientReview` now also shoots the Armature and Court chapter contents and both leaves of every DS-9 page. Its staging uses `markKnown` for the Court, the encounter call for the bosses and the rite completion for the two Armature lessons (the staged player has no armour practice).
- `python gradle/source-text-impact-map.py`: regenerated (6 rows added).
- Client: `runLiberPagesReviewClient`, two launches. Both got past the menus, logged "LIBER_PAGES_REVIEW captured 147" and stopped by themselves. The first showed the two Armature lessons hidden for want of practice, as intended; the harness then opened them through the rite, and the second launch shot them. No java process is left.

### Docs

- `docs/OPEN_QUESTIONS.md`: deleted section 1 row NQ-16-3 (the area now reads "No question left"), section 2 row DS-9 (page homes), and section 3 rows 16 T6 Armature, crafts, bosses, Thurible page and Court pages. Section 4: new row "B34 (Liber pages)".
- Rulings recorded: DS-9 built and NQ-16-3 A in `docs/NPC_DIALOGUE_AND_TRADE.md` (rulings, status, pointer, history); NQ-16-3 A in `docs/BRINED_COURT.md` (rulings, pointer, proof, history); DS-9 in `docs/BOSSES.md` and `docs/CARDINAL_RITES.md`; `docs/STATION_UPGRADES.md` (eleven lessons, the gap paragraph is now the built rule, the decal sentence).
- `docs/HEMOMANCY_REFERENCE.md`: a new paragraph "The missing pages (DS-9 and NQ-16-3)" in the Liber section, and the page homes in the Rites & Crafts paragraph. `docs/LORE_REFERENCE.md`: no lore changed; the boss and Court pages restate it. `wiki/Blood-Systems.md`: one paragraph.
- `docs/audits/2026-11-02-cleanup/README.md`: row C-12b's coverage gaps closed. `docs/owner_answers/16-npc-dialogue-canon-and-liber.md`: T6 status line.

### For the owner

- No lang keys. Liber pages are English in their JSON, as every page is. New pages: `armature/pages/the_rack`, `consecration`, `monolithic_armature`; in `the_hematic_order/pages/`: `scarlet_vanity`, `mycelial_lantern`, `mnemonic_reliquary`, `somnolent_seat`, `fungal_implantation_pylon`, `mason_effigy`, `scar_station`, `dendritic_distributor`, `morphling_incubator`, `vacant_effigy`, `sanguine_monolith`, `covenant_throne`, `sanguine_vigil`, `sporitic_thurible`, `dried_gourd`, `hematic_stake`, `mycelial_synapse`; in `cosmic_forces/pages/`: `vesper`, `mycophant`, `naeglerophaeon`; in `brined_court/pages/`: `the_houses`, `bloom_warden`, `kindled_champion`. Changed: `bloodwork_stations` (the two Armature sentences became one pointer).
- How I read your NQ-16-3 answer ("as they the houses are discovered"): the chapter stays hidden until you learn of the Court, from a Voyager, the letter, the Alchemist or by finding a Court site. Then the houses page opens, and each boss page when its fight begins. One page cannot open house by house. Say if you want eight house pages instead, each opening at its own site.
- Values I chose: chapter places (Armature 11, after Vial Separation; Court 4, before Cosmic Forces); the Court chapter's colours (`0.1,0.2,0.25`, tab `#1E3A44`, sea blue); icons (the Armature, the Consecration Kit, the Cornerstone; the Relay Chart, a Sunburst Bloom, a Hydra Crown; the Memory of Vesper, the Mycophant Tendril, the Naeglerophaeon Ganglion; each craft its own item); the rack page from D2 (when the Artificer lets you use it); the Dried Gourd page at D1 (its craft needs no Degree); a new discovery source `ENCOUNTER` for boss pages. Craft and Armature pages set `decal: false`, like the other dense station pages.
- Frames: `build/build2/B34-shots/` (55 frames, from `liber-pages-003-contents-armature.png` to `liber-pages-057-sang-brined_court-the_houses-last.png`). Long titles in a chapter's contents end in "...", as in other chapters.
- Surprising: the Monolithic Armature page's anchor sentence was changed after the shots to match the tier-2 ring rule (fill the inner four, seal, the outer four wake with the station projection), so `liber-pages-009` and `-010` show the older wording. The Hematic Stake page names both ways a stake comes: a progenitor's empty hand, and the D5 floorless craft.
- Tone to check: Vesper (he) "is what silence costs when it takes up arms"; the Mycophant (she, the motherly register) "is not killed. She yields"; the Court page says Marimo and Hydra are never met as creatures. No God or Satan framing.

### Verification (round 1)

Checked the tree against `refs/build2/B34-before` and `build/build2/B34-status-before.txt`. Pass, tree green.

- Scope: only Liber pages, their registration, the `ENCOUNTER` source and the unlock calls at each fight start. Nothing held or reversed is touched. No lash number, HUD, payload, lang key, blood cost or particle changed.
- Values: chapter homes match DS-9 (page homes). The Court chapter has one house page and the two boss pages (NQ-16-3 A). The Thurible page matches the code: the rite file (D8, Lesser Working floor, Spore Sac, Chain and Brown Mushroom, 1,000 mL feed) and the Apotheos gate in `SporiticThuribleRules` and `CardinalRiteActivation`. The craft Degrees are checked against the recipe files by the new test.
- Tests: `LiberCraftAndBossPagesDataTest` (5) and `CourtDiscoveryGameTests.theCourtChapterOpensAsTheCourtIsMet` fail without the package. The one route fingerprint line (`voyager_court_learn` D7) changed only by the new Liber entry. No test weakened.
- Unit suite: BUILD SUCCESSFUL, 6,199 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B34-verify1-unit.txt`).
- Combined GameTest run: "All 2013 required tests passed :)" (`build/build2/B34-verify1-gametest-latest.log`).
- Client: `runLiberPagesReviewClient`, new world `LiberPagesV1`. It got past the menus, logged "LIBER_PAGES_REVIEW captured 147" and closed itself. No java process is left. The 32 frames of the new pages are in `build/build2/B34-verify1-shots/`. Frames 009 and 010 now show the corrected Monolithic Armature wording. Every page renders and turns.
- Non-blocking: the houses page opens whenever the Court becomes known, a Voyager's word included, not only when a house is found. That reads the owner's "as the houses are discovered" loosely; the For the owner list already asks him. `PhlegethosomaEntity.answerSummons` names the Liber classes by full package path instead of imports. The box between page numbers in chapter contents is in every chapter, not new.


## B35. Sound: wiring and the credits routine

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| Sound S1, UN-12 (cast events) | 62 `manipulation.<id>.cast` events, one per manipulation that sounds on cast, listed in `ManipulationCastSounds.CAST_EVENTS` and registered as `SoundInit.MANIPULATION_CAST`. In `sounds.json` each aliases the vanilla cue it used before (`"type": "event"`) with that cue's volume and pitch, so a sourced file is a data change only. The old `Cue` switch is gone. Player casts play the event for the 36 ids in `PLAYER_CASTS` (the old table less Umbral Rend). `EntityCasts` plays the event through `playEntityCast` for all 29 entity casts and names no `SoundEvents`. Where a manipulation had a player cue, the entity now uses it: Activation Potential (0.25 / 1.25 instead of 0.55 / 1.5), and Blood Shot, Blood Needle and Deadly Gaze, which were silent. Two entity casts keep a relative pitch: Hematic Flare x1.2 when the target is concealed, Gloam Laceration x0.76 on an ambush. Blood Cloud's entity cast plays its event (ghast shot, volume 1) instead of level event 1016 (volume 10). Vitric Combustion's entity cue goes from volume 1.1 to 1.0. All events share one new subtitle key. | `ManipulationCastSounds.java`, `SoundInit.java`, `EntityCasts.java`, `sounds.json`, `en_us.json`, `src/gameTest/resources/frozen/registry-ids.txt` |
| Sound S1, the droppable doubles | Crimson Flame's Projection no longer plays flint-and-steel over TNT priming (TNT plays its own primed sound). Umbral Rend left the cast table; its own sweep (volume 0.8, pitch 0.55 or 0.7 by targets struck) now plays `manipulation.umbral_rend.cast`, so the sweep sounds once. | `FlameKindleSculpting.java`, `UmbralRendManip.java` |
| Sound S1, UN-16 | `entity.mycophant.teleport` is now `entity.mycophant.split` (event, file `entity/mycophant/split.ogg`, subtitle key, `SoundInit.ENTITY_MYCOPHANT_SPLIT`, the Clairaudiograph's Mycophant list, the frozen registry ids). The death file is cut from 9.86 s to 3.6 s with a 0.8 s fade-out, mono, 44.1 kHz. The code plays it at pitch 0.6, so it now lasts 6 s in game, the length of the yield scene (120 ticks). The untrimmed mono file is `removed_assets/src/main/resources/assets/hemomancy/sounds/entity/mycophant/death_mono_untrimmed.ogg` (the stereo original was already there as `death.ogg`). | `SoundInit.java`, `MycophantMoves.java`, `sounds.json`, `en_us.json`, `clairaudiograph_sounds/mycophant.json`, `sounds/entity/mycophant/{split,death}.ogg` |
| Sound S1, UN-17 | A termite chews with vanilla `block.wood.hit` at volume 0.3, pitch 1.3 to 1.6, every 20 ticks (`CHEW_SOUND_INTERVAL`) instead of the zombie door bang every 8 ticks. A Stained Priestess plunge sounds only thorns 0 and 6 of its seven-thorn line (`PriestessCombatRules.PLUNGE_LINE`, `plungeThornSounds`); the four ring thorns round the spear are silent. A silent thorn is an eruption with `setSilent(true)`; `PriestessEruptionEntity.spawn` has an overload with `sounded`. | `ChewWoodGoal.java`, `PriestessEruptionEntity.java`, `StainedPriestessEntity.java`, `PriestessCombatRules.java` |
| Sound S2, UN-70 and UN-18 | `docs/SOUND_CREDITS.md`: one row per file under `assets/hemomancy/sounds/` (121 files: 113 `.ogg`, 8 `.mp3`). The 11 Crimson Troupe files of B31 have their Freesound id, title, author, CC0 1.0 and link (matched to the candidates by length). The other 110 read "source not recorded". `SoundCreditsTest` fails on a file with no row, a row with no file, a doubled row, a sourced row whose licence is not `CC0 1.0` or `Sonniss GDC`, or a CC0 row with no author or Freesound link. The sourcing routine (re-run the inventory, take the pick, fetch, edit Sonniss files, mono when positional, trim at the played pitch, name, credit, check the timbre rule) is in `docs/SOUND.md`. `tools/sound_audit/inventory.py` was re-run and taught the cast events (local, untracked): 242 events, 871 vanilla call sites, none unregistered, only the three `antecedent.*` records unused by code (the Clairaudiograph plays them by id). | `docs/SOUND_CREDITS.md` (new), `SoundCreditsTest.java` (new), `docs/SOUND.md` (new), `tools/sound_audit/inventory.py` |
| UN-11, UN-13, UN-14, UN-18 (rulings) | Recorded under "Rulings that bind" in `docs/SOUND.md`, with the reference (section 35), the lore reference (section 7.4, the timbre rule), `SAINT_MAUSOLEUMS.md`, `BOSSES.md`, `MANIPULATIONS_AND_CASTING.md` and one line in `AGENTS.md`. No audio was added. | docs, `AGENTS.md` |

### Skipped

| Row or part | Why |
| --- | --- |
| UN-13's Funeral Bell and Velorum toll as struck iron | Needs new audio (Sound S3 and S5). `manipulation.funeral_bell.cast` still aliases the vanilla bell; the ruling is recorded and marked "Not built yet". |
| The other stacked cues in the audit notes (Annetta's head burst, shield bash and break stab, Putriciel's steps and sludge, Velorum's toll, Seraphae's convulsion, the Mycophant pairs, the Silent Archon Severance pair, the rite staff impact) | The audit marks these as "one designed file" or "drop the vanilla layer after replacement", not as droppable now. They need new audio. |
| The plummet's nine thorns | UN-17 names the plunge only. The plummet still sounds all nine thorns on one tick. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL in 2m 25s, 6,206 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B35-unit2.txt`). The first run (`B35-unit.txt`) failed three new `ManipulationCastSoundsTest` methods: `SoundInit` cannot load in a JVM test, so the id list moved to `ManipulationCastSounds.CAST_EVENTS`.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "2013 GAME TESTS COMPLETE", "All 2013 required tests passed :)", BUILD SUCCESSFUL in 2m 28s (`build/build2/B35-gametest.txt`, `B35-gametest-latest.log`). The frozen registry-id golden passes with the changed lines.
- After the doc edits, `ArchitectureStandardsIndexTest`, `SoundCreditsTest`, `ManipulationCastSoundsTest`, `EntityCastGuardrailTest`, `RepositoryTextEncodingTest` and `AnnettaCombatRulesTest` were run again: BUILD SUCCESSFUL (`B35-unit-docs.txt`).
- Rewritten on purpose: `ManipulationCastSoundsTest`. It pinned the old `Cue` enum; it now pins the same 22 cues, volumes and pitches as the `sounds.json` alias of each `manipulation.<id>.cast` event, checks the list against `sounds.json`, keeps the "passive and retired audio left alone" check, and checks that Umbral Rend is not doubled.
- Added: `EntityCastGuardrailTest.entityCastsSoundThroughTheCastEvents` (no `SoundEvents`, `playSound` or `levelEvent` in `EntityCasts`; every named id has an event). `SoundCreditsTest` (two methods). `AnnettaCombatRulesTest.onlyTheEndsOfAPlungeLineSound`.
- Changed on purpose: `src/gameTest/resources/frozen/registry-ids.txt`, 62 `manipulation.*.cast` sound events added and `entity.mycophant.teleport` renamed to `entity.mycophant.split`, in sorted place, CRLF kept.
- `docs/architecture/evidence/source-text-impact-map.csv` regenerated: three rows added (the two sound tests reading `sounds.json`, `SoundCreditsTest` reading `SOUND_CREDITS.md`).
- No client was launched: no row changes a look, a screen or a shader.

### Docs

- New: `docs/SOUND.md` (the topic doc: where it lives, the sourcing routine, rulings UN-11, UN-13, UN-14, UN-18 with UN-12, UN-16, UN-17, UN-36 and UN-70, the calls made, proof owed) and `docs/SOUND_CREDITS.md`. Both are in the `docs/README.md` topic table; the README's sound-audit paragraph now points at `SOUND.md`.
- `docs/HEMOMANCY_REFERENCE.md` section 35: the split and cast-event rows, 242 events, the sourcing rules. `docs/LORE_REFERENCE.md` section 7.4: the timbre rule. `BOSSES.md`: UN-16 marked built, the Mycophant sound gap replaced by the plummet note, the UN-14 pointer answered. `SAINT_MAUSOLEUMS.md` and `MANIPULATIONS_AND_CASTING.md`: the UN-13 and UN-14 pointers answered. `architecture/ARCHITECTURE_STANDARDS.md`: the `EntityCastGuardrailTest` row. `AGENTS.md`: one sound line.
- `docs/OPEN_QUESTIONS.md`: section 2 Sound rows UN-12, UN-16, UN-17, UN-36 and UN-70 deleted (a one-line pointer replaces the table); section 3 rows Sound S1 and S2 deleted, Sound S3 no longer waits on them, the area line is now 4 / 0 / 4; section 4 gained "B35 (casts)", "B35 (thinning)" and UN-70 (fill in the credits). The section 1 rows UN-11, UN-13, UN-14 and UN-18 stay: their files are Sound S3 to S5.
- No chunk file: this package had no chunk plan.

### For the owner

- New lang key: `subtitle.hemomancy.manipulation.cast` = "Blood manipulation cast" (shared by all 62 cast events). Changed: `subtitle.hemomancy.entity.mycophant.teleport` ("The Mycophant shifts") is now `subtitle.hemomancy.entity.mycophant.split` = "The Mycophant splits".
- Values I chose: the termite's knock (`block.wood.hit`, volume 0.3, pitch 1.3 to 1.6, every 20 ticks); the death file cut to 3.6 s, not 6 s, because it plays at pitch 0.6 (6 s in game), with a 0.8 s fade; the plunge's ring thorns silent as well as the middle of the line; the relative pitches x1.2 and x0.76 that keep Hematic Flare's and Gloam Laceration's entity cues as they were.
- Surprising: an entity's Blood Cloud is now heard over about 16 blocks; level event 1016 played the ghast shot at volume 10 (about 160 blocks). Funeral Bell still rings a vanilla bell until its struck-iron file arrives (UN-13). The credits list has 121 files, not 102: B31 added 11, and eight `.mp3` files under `sounds/entity/samples/` ship though no event plays them. The rename of `teleport.ogg` to `split.ogg` was made with `git mv`, so it is staged.
- Nothing to look at; listen in play (section 4 rows "B35 (casts)" and "B35 (thinning)").

### Verification (round 1)

- Scope: only Sound S1 and S2 work in the diff. No lash numbers, HUD, held manipulations, Silent Severance or held sound rows touched. No new audio added.
- Values match the answers: 62 cast events alias the old cues with the old volume and pitch (checked in sounds.json against the removed switch and the old EntityCasts lines; Flare x1.2 and Gloam x0.76 give the old 1.75 and 0.65). split.ogg renamed everywhere; death.ogg is 3.6 s mono 44.1 kHz, played at pitch 0.6 for 6 s; untrimmed copy in removed_assets. Termite: block.wood.hit, 0.3, 1.3 to 1.6, every 20 ticks. Plunge: thorns 0 and 6 sound, ring thorns silent. Crimson Flame's TNT path plays no flint-and-steel. Credits file has 121 rows for 121 files.
- Tests: ManipulationCastSoundsTest rewritten to the event aliases, not weakened. New EntityCastGuardrailTest method, SoundCreditsTest and AnnettaCombatRulesTest plunge test. Not covered by a test (non-blocking): the termite chew, the flint-and-steel drop and the death file length.
- Unit suite: BUILD SUCCESSFUL, 6,206 tests, 0 failures, 0 errors, 0 skipped (build/build2/B35-verify-unit.txt).
- Combined GameTest run: All 2013 required tests passed, BUILD SUCCESSFUL (build/build2/B35-verify-gametest.txt).
- AGENTS.md: lang keys added and renamed in en_us.json, no payloads, no blood or delivery code, no new vanilla particles, removed file moved to removed_assets. One AGENTS.md sound line added.
- Docs: OPEN_QUESTIONS.md lost only the Sound section 2 rows and Sound S1 and S2; UN-11, UN-13, UN-14 and UN-18 stay in section 1 with their work in S3 to S5. Rulings are in docs/SOUND.md. Log matches the tree.
- No client launched: nothing changes a look, screen or shader. Verdict: pass.

## B36. Art by generator: placeholder mobs, Court strain looks and icons, texel batches T2 and T4

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| 06 T13 (VX-16), Hemojelly | New generator. A stepped bell (12 wide skirt, 10 and 8 wide dome steps, 4 wide crown) over a clotted heart (4x3x4 with a hot core), four frilled oral arms (two 6-unit segments) and eight 1x1 stinging tendrils of four 8-unit joints (two blocks, the depth of its feeding net). Whole cubes, 128x64 atlas at 16 px per block. The bell and arms are translucent (alpha 178 and 200, tendrils 220); the model now renders `entityTranslucent` and draws the heart first so it shows through. Glow layer: the heart and sparse beads, brightening with each beat and full while it holds prey. The model is now a `HierarchicalModel` on the generated mesh: the bell contracts and lifts, the tendrils trail a beat behind, a capture draws them in. | `tools/model_export/hemojelly.py`, `HemojellyMesh.java`, `HemojellyModel.java`, `HemojellyRenderer.java`, `model_hemojelly.png`, `model_hemojelly_glow.png`, `HemojellyMesh.bbmodel` |
| 06 T13 (VX-16), Myelin Borer | New generator. A lacquered boring head with a ringed bore and violet palps, a thorax and five ivory sheath segments (widths 5, 5, 4, 3, 2) whose front and back faces are raw crimson nodes, four pairs of hooked legs, three pairs of bristles and a short stinger. Colours taken from the Myelin Sheath and Nerve Fiber blocks. 128x64 atlas. Glow: the nodes, the palp tips and the cheek pits, faint when calm, bright while agitated. The model keeps the travelling wave, leg steps and palp feel on the generated mesh, and the blink still hides it. | `tools/model_export/myelin_borer.py`, `MyelinBorerMesh.java`, `MyelinBorerModel.java`, `MyelinBorerRenderer.java`, `model_myelin_borer.png`, `model_myelin_borer_glow.png`, `MyelinBorerMesh.bbmodel` |
| 06 T13 (VX-16), review rows | `hemojellyReviewClient` and `myelinBorerReviewClient` reuse `AnimalModelClientReview`. It now floods the arena for a water animal (5 deep, sand floor) and shoots it three times below the surface and once above; frames a small land animal closer (distance times max(width, height) / 1.4, clamped 0.4 to 1); and takes a floor block from `hemomancy.animalModelReview.floor` (the Borer stands on Nerve Fiber). The Doe and Strider framings are unchanged (factor 1); the Fungling's is a little closer. | `AnimalModelClientReview.java`, `gradle/review-clients.gradle` |
| 06 T13 (VX-16), effect icons | 28 icons in 13 sets were byte copies. In each set the effect the picture fits keeps it (hemophagy, transfusion_saturation, neural_overload, mnemonic_screams, impaired_recovery, arachnid_anastomosis, necrosis, ferric_transfusion, poison_resistance, veiled and the three strain passives). The other 15 get their own 18x18 art from a new generator: grave_debt (bleeding headstone), insatiable_hunger (toothed maw), blood_drunkenness (sloshing iron goblet), conductive_mark (brand ring with a spark), monolithic_dislocation (a slab knocked out of its outline), counterfeit_scar (stitched scar on a mismatched patch), hemolysis (a split red cell), web_mobility (web), wither_resistance (bone ward over a black skull), explosion_resistance (iron ward before a blast), cryoprotection (blood drop in a frost crystal), ender_resistance (arrow snapped on a plate). No two of the 64 icons are identical now. New `effectIconReviewClient` shows each set in the HUD row and the inventory list. | `tools/model_export/effect_icons.py`, 15 PNGs under `textures/mob_effect/`, `EffectIconClientReview.java`, `gradle/review-clients.gradle` |
| 14 T10a (FX-19 icons) | Built in the same generator: `stung` (barbed stinger in skin with a bead of blood), `pinned` (iron stake through brine roots), `morphling_sealed` (vivianite jar under a blood-wax seal). | as above |
| 14 T9 (HN-4) | Ink Cap: a part may now end at a stage (`MorphlingPartSpec.until`, default 5, with `showsAt`); the cap and crown scales end at Apex, so only the ink hood shows at Primal. Holdfast: a stage-3 `forearm_knots` part on the root band, five stone lumps (1.25 to 2.5 units). Sunburst: the Mature back crust is 8 by 7.5 and one unit thick (was 7 by 5 and half a unit), with a lower ridge; the frond buds are 2 wide and 4.75 tall (were 1 and 2.75). Dead Man's Rope: ribbon material (96, 84, 198) at alpha 215 (was (116, 106, 214) at 175); every rope strand and the Primal train draw a dark Umbra ink thread under their glow (`STRAND_INK` 0.6). The Woven Current's Court strands share the strand code, so they get the thread too. When the generator reran, only the four named models changed (it reproduces the rest byte for byte). | `tools/model_export/morphling_strains.py`, `MorphlingPartSpec.java`, `MorphlingStrainModel.java`, `InkCapHeadModel.java`, `HoldfastArmModel.java`, `SunburstBodyModel.java`, `MorphlingStrainWorldRenderer.java`, the rope, Holdfast and Sunburst textures and `.bbmodel` files |

### Skipped

| Row or part | Why |
| --- | --- |
| 14 extra (polyp models) | Not started. The package ran long; the brief says stop after a whole model and log the rest. Still ready. |
| 14 extra (first-person arms) | Not started, same reason. Still ready. |
| 09 T13 (VX-11, NQ-09-4) | Not started, same reason. The twelve textures and the worn-armour review scene remain. |
| 09 T15 (VX-13, NQ-09-5), with the Marionette Crossbar part of 09 T14 | Not started, same reason. The ten generators and the Unstained weapons bake remain. |
| VX-16 rest (blocks, sabatons, Forge and Loom) | Outside this row by the owner's order; still its own row. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL in 1m 47s, 6,206 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B36-unit.txt`). Same as after B35.
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 2013 required tests passed :)", BUILD SUCCESSFUL in 2m 27s (`build/build2/B36-gametest.txt`, `B36-gametest-latest.log`). Same as after B35.
- No test added, changed or flipped; the plan names none. `MorphlingMutationRegistryTest` and `MorphlingPassiveEffectPresentationTest` stay green with the new part field and icons. `source-text-impact-map.csv` not regenerated: no test reads a new file.
- Review clients, each past the Accessibility screen (the gradle factory skips it) and each stopped by itself: `runHemojellyReviewClient` (two passes; judge the second), `runMyelinBorerReviewClient` (two passes; the first framed it too small), `runEffectIconReviewClient` (two passes; the first opened the creative inventory; `EFFECT_ICON_REVIEW captured 12`), and `runMorphlingStrainReviewClient -PmorphlingStrainReviewOnly=...` before and after (`MORPHLING_STRAIN_REVIEW captured 41` each). No client or server is left running.

### Docs

- `docs/BRINED_COURT.md`: the HN-4 and FX-19 rulings say what was built; the "Not built, ruled" proof line now points at the shots.
- `docs/HEMOMANCY_REFERENCE.md`: the VX-16 ruling moved from "not built yet" to the built creature rulings, with what was built.
- `docs/TESTING.md`: the three new review rows and the icon generator.
- `docs/audits/2026-11-02-cleanup/README.md`: C-11 marked partly done.
- `docs/OPEN_QUESTIONS.md`: section 3 rows "06 T13 (VX-16)", "14 T9 (HN-4)" and "14 T10a (FX-19 icons)" deleted (Creatures and spawns now reads "Nothing left to build"); the "14 T12" row's state is now ready. Section 4 rows VX-16, HN-4 and FX-19 icons now name the shots and read "now". No section 1 or 2 row carried these ids.
- Chunk status lines: 06 T13 built; 14 T9 built; 14 T10 built.
- `removed_assets/2026-11-16-b36-placeholder-art/`: the two old mob textures and the 15 old icons, with a `MANIFEST.txt` line.
- `LORE_REFERENCE.md` and `MORPHLING_STRAINS.md`: no mechanic or stage text changed.

### For the owner

- No lang key added or changed. No payload, blood path or gameplay code changed.
- Looks to judge (sheets in `build/build2/B36-review/`): `hemojelly-sheet.png` and `myelin-borer-sheet.png` (full shots in `build/hemojelly-review/screenshots/` and `build/myelin-borer-review/screenshots/`); `effect-icons-8x.png` and `effect-icons-sheet.png` (full shots in `build/effect-icon-review/screenshots/`); `strains-before-after.png` (top row before, bottom after) and `rope-before-after.png` (full shots in `build/morphling-strain-review/screenshots-B36-before/` and `screenshots/`). Generator previews: `build/hemojelly_preview.png`, `build/myelin_borer_preview.png`.
- Values I chose: all the shapes and colours above; the jelly's alphas (178, 200, 220) and glow levels (0.45 to 0.85 with the beat, 1.0 while feeding); the Borer's glow (0.35 to 0.5 calm, 0.85 to 1.0 agitated); the strand ink at 0.6; which effect in each shared set kept the old picture.
- The new icons are 18x18, the size vanilla draws; the older ones are 16x16. The three strain passive icons hide from the HUD row while the jar shows, so `effects-4-hud.png` and `effects-5-hud.png` show fewer icons than the inventory shots.
- The Holdfast and Sunburst Mature growth still draws at 0.7 scale (every part grows in at 0.7, 0.88 and 1.0), so the change is larger than before but modest. Say if you want more, or want Mature to take some Apex parts instead.
- The Hemojelly's bell sits above the water line (its surface float), so from below it reads grey through the surface; the above-water shot shows its colour.

### Verification (round 1)

- Pass, tree green. Checked the tree against `refs/build2/B36-before` and `build/build2/B36-status-before.txt`: 44 changed and 10 new files, all inside the brief (the two mobs, the effect icons, the four strain models, the review harness and docs). No lash, HUD, payload, lang, blood or particle file changed. No held or reversed row touched. The skipped rows (polyp models, first-person arms, 09 T13, 09 T15) are still in `docs/OPEN_QUESTIONS.md` section 3 as ready or waiting.
- Values: there were no owner numbers; the plan says the build proposes and the owner judges. The shipped textures are 128x64 for both mobs and 18x18 for the 15 new icons. No two effect PNGs share a hash. The generators are untracked but not ignored, so they will be version-controlled.
- Unit suite: BUILD SUCCESSFUL, 6,206 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B36-verify-unit.txt`).
- Combined run: the first run had 1 required failure, `abandonsremovedpreyandresumescruising` (Mnemonic Whale, "Whale did not resume cruising after losing prey"; `build/build2/B36-verify-gametest-run1-latest.log`). B36 touches no whale code. The rerun passed all 2013 (`build/build2/B36-verify-gametest2.txt`, `B36-verify-gametest-latest.log`). Treated as a flake; worth watching.
- Review clients, each got past Accessibility, captured and stopped by itself; no Java left running. Shots copied to `build/build2/B36-verify-shots/`: `hemojelly/` (16), `myelin-borer/` (16), `effect-icons/` (12, `EFFECT_ICON_REVIEW captured 12`), `morphling-strains/` (`-PmorphlingStrainReviewOnly=ink_cap-primal,holdfast-mature,sunburst-mature,dead_mans_rope`, `captured 47`). The Ink Cap at Primal shows the hood with no cap; the rope strands carry a dark thread.
- Non-blocking: no test was added. The plans name none beyond the existing ones and the shots, but `MorphlingPartSpec.showsAt` is new logic, and a small unit test on it (and one that no two `mob_effect` PNGs match) would fail without this work.
- Non-blocking: the log says "No two of the 64 icons". There are 61 icon PNGs; the other 3 files are `.mcmeta`.

## B37. Housekeeping and the docs-drift pass

### Built

| Row or task | What changed, with the values | Main files |
| --- | --- | --- |
| UN-71, legacy `main()` tests | All 293 legacy `main()` test classes are JUnit now: 288 got `@Test void allChecksPass()` in place of `public static void main(String[] args)`, and the four that already had a JUnit wrapper calling `main(new String[0])` lost the wrapper and kept its name. `LegacyMainTestAdapterTest` and `LegacyMainTestSuiteContractTest` (the reflective runner and its contract) and `LuxUmbraCombatContractTest` (it only called two other tests' `main`) went to `removed_assets/src/test/`. New guardrail `LegacyMainTestGuardrailTest`: no `*Test.java` declares `main()`, except `RadialVeinBorderRenderTest`, whose `main` is the GPU review entry its init script calls with an output folder. | 288 test files, `architecture/LegacyMainTestGuardrailTest.java` |
| UN-71, private test helper ratchet | 431 private `read`/`assertContains`/`assertNotContains`/`assertDoesNotContain` copies in 215 test files were removed for static imports of `support/SourceAssertions`, but only where the body was the same check (a plain `contains` test with the label first; a `readString` of a path, of the repo root or of a relative path, with or without the CRLF fold). `assertDoesNotContain` calls became `assertNotContains`. `PrivateTestHelperRatchetTest.CEILING` 593 to 101 (the count left). | `PrivateTestHelperRatchetTest.java`, 215 test files |
| UN-71, `getPersistentData()` ratchet | Re-checked: 84 files, 357 calls (the row said 93). Three reasons named migrations that no longer exist (`mnemonist_starter_claim`, `artificer_claim_keys`, `chamber_access`); they now say what the calls hold. `ChamberVisitService` lost five unused `LEGACY_*` root-key constants (recorded in `removed_assets/MANIFEST.txt`). Counts unchanged. | `persistent-data-allow-list.txt`, `ChamberVisitService.java` |
| 16 T7 (FX-21), ready parts | Weapon-form secondary Tempers in reference 8.2 from `ManipulationInit` (blade Animus/Ferric, axe Ferric/Animus, spear Lux/Ferric, claws Tenebris/Mortem, crossbow Ductilis/Ferric, torch Flammeus/Lux, flail Congeatio/Ferric, sickle Mortem/Ferric, staff Ferric/Animus; the verbs Ferric/Animus); "axe is Mortem" fixed. The group that looked fixed was read against code and holds: Mycophant brood (14 blocks, 80 ticks, 8 damage, x1.8, 30 health, 8 bite, cap 2, grief 50, brood cap 4), Saint loot (one roll: one Hallowed Residuum, 1 to 2 Quintessence), Hemorath's four wells of 1,000, Putriciel's Offering (a hit of 4), Velorum's devotees (wake 40 ticks, pin 30). `LORE_REFERENCE.md`'s three "ritually sever" lines now say the leader removes a member on the bloodline pool screen. The old MnA design tables of reference 3.3 and 37.1 moved to the archive. The lancea sentence was already right; the `BOSSES.md` bullet that waited on it is closed. | `HEMOMANCY_REFERENCE.md`, `LORE_REFERENCE.md`, `BOSSES.md` |
| 16 T7 (FX-21), tables | Phlegethosoma: drop 2 to 4 Formations and 1 to 2 Barbed Splinters (the Barbed rows in 20.1 and 22.3 named the old Calcified Blood Spine), spawn summoned only (already right). Saint move counts (Hemorath 18, Seraphae 17, Putriciel 20, Velorum 21), damage scaling and "the Saint is D8" were already right. Manipulation roster recounted (115; Animus 28 = 10/8/6/2/2, Ferric 19 = 5/7/4/2/1, Ductilis 15 = 4/5/2/2/2, Flammeus 13 = 5/5/1/1/1, Mortem 13 = 4/3/1/2/3, Tenebris 10 = 2/5/1/1/1, Congeatio 9 = 2/3/1/1/2, Lux 8 = 1/4/0/1/2) and 33 memories with 39 Sculptings (unchanged). Sculpting price range from each class's blood per pulse and cadence: the plain Sculptings 4 to 48 mL/s (Glacial Thaw 4, Glacial Rampart 48), the eight held manipulations 35 to 60, the lashes 15 to 60. Tainted meat: 10 mL a nutrition point, 30 an item (the 60 mL line fixed). Twelve families hold 29 forms (was 28). | `MANIPULATIONS_AND_CASTING.md`, `CORE_LOOP.md`, `HEMOMANCY_REFERENCE.md` |
| 16 T7 (FX-21), section G: the reference | Section 38 is a short table: 21 rows, each system's state and the doc that holds it. The thirteen dated notes above the table of contents, the seven appendices after section 38, section 38's old list and the old MnA tables moved to `docs/archive/done/HEMOMANCY_REFERENCE_DATED_NOTES.md` (links rewritten). Corrected from code: the Qliphoth pome (ripens on the Bloom, the owner picks it), `CardinalRiteEvents` (gone; `CardinalRiteActivation.tryStart`, `CardinalRiteCompletionEffects`, `HarbingerCardinalRiteEvents`), config ranges and three "see code" defaults, five vein sections, the Hermit's D1 row, the Liber recipe, the root advancement (The Red Thread), the seven-station upgrade catalog, the Fane's 180-second activation, the Fungal Dimension bullets, purity sources (abstinence +1.0, empty blood +0.30 a minute) and pacing, `ProgressionGates.unstainedStage`, Still Arts granted by advancements only (no rite grants one), Silver Veil (Verdigris Aura and +10 purity), the Severed Covenant row, the effect table (two MnA rows out, the 29 untabled effects named), the skill table (10 rows fixed, 37 skills added from `common/init/skills`), the Primal gate (Apotheos path), eight puppet shapes (Mummer trial 950), the Drudge gate and refill, retired ids out of the Vesper and Drudge tables, weapon damage arguments, tool tiers (four), the Velorum Mask (D6, crafted), recipe types, JEI categories, key bindings (thirteen registered, with defaults), commands, particles (`ColorParticleOptions`), the Liber and the Harbinger suite (six tabs), entity loot (87), biomes (Basin and Cortical Drift added), the Wake nursery placement, boss openings (five added), and 46 dead image links removed. The untabled entities (107) and advancements (159) are listed by id. | `HEMOMANCY_REFERENCE.md`, `archive/done/HEMOMANCY_REFERENCE_DATED_NOTES.md` |
| 16 T7 (FX-21), section G: lore, topic docs, wiki | `LORE_REFERENCE.md`: Rite of Apotheos, the Unsigned Ancestral Ledger, seven ordeals, no Blood Conduit tiers, the Neophyte's four proofs, the D3 wake, Degree 5 as the summit, the Spine dormant after the revelation, the Harbinger Lodestone, the PendingDelivery rewards, the Siphonophore's Ductilis, the outer halls without a trial, local-only links. Topic docs: the "uncommitted" lines checked against `git log` (Brined Court, NPC trade, playthrough suite, Saint kit, Circus round 1 are committed), the Vacant Effigy as a Working craft, Succession braziers, Pelagic loot and seven species, special floors, set pieces (twelve), the Condenser pages' `"decal": false`, the TESTING headings. Wiki: Blood-Systems (real manipulations per temper, vascular, skills, constructs, effects), Getting-Started (items, biomes, keys, the Unstained steps), Unstained-Path (stages, purity sources, the cure, Clarity, Still Arts, the church, equipment), Harbinger-Path (Degree 8, armour, tools, the special floors), School-Combat (retired rows out), tool paths under `tools/review/`, branch links to `neo-1.21.1`. | `LORE_REFERENCE.md`, the topic docs, 17 wiki pages |
| 16 T7 (FX-21), five lang slips | `hemomancy.rite.hematic_unbinding.desc` (an unbinding rite, not a severance rite), `hemomancy.vicar.adept.brotherhood_lore` (Hematic Order), `hemomancy.lady.discerning.v0.follow` (Silver Ward at the Clarity stage), `advancements.hemomancy.vesper_defeated.description` (no Silent Archon name before the memory), `hemomancy.dialogue.event.vicar_hermit_road_known` (the road is one entry). | `en_us.json` |

### Skipped

| Row or part | Why |
| --- | --- |
| UN-71, the 20 "player root keys not yet moved (S-1)" rows | Moving them into attachments is gameplay state work, not housekeeping; the ratchet stays exact. |
| Section G findings marked "user's call: yes" | DC-15, DC-32, DC-34 (the Lowtide theme gate), DC-38 (tier-3 scar gate), DC-46 (the Loom's Blood Moon discount), DC-47, DC-60, DC-70 (Chamber size), DC-85 (the MnA brainstorm instruction in `AGENTS.md`), DC-86, DC-89. Left for their rows. |
| Section G findings that need code | DC-1 (`Mth.smoothstep` in three renderers), DC-58 (the Antecedent epilogue reaches a Silentium), DC-78 (staged files). Not docs work. |
| Section G, still open | DC-22 (topic-doc headings in eight docs), DC-73 (20 wiki pages not linked from Home; images embedded from `build/`), DC-81 (the impact map still reads only literal paths; regenerated), DC-23 and DC-25 detail counts, DC-27 (`thanks_fine`), DC-62 (Saint epithets, Voluntas Sola). Small and no ruling needed; one more docs sitting. |

### Tests

- `./gradlew.bat compileJava compileTestJava compileGameTestJava test --rerun --offline --console=plain`: BUILD SUCCESSFUL in 1m 42s, 6,200 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B37-unit.txt`). After B36: 6,206. The 293 dynamic adapter tests, the contract test and the Lux wrapper went (-295), the 288 converted classes and the guardrail came (+289).
- `./gradlew.bat runGameTestServer --offline --console=plain`: "All 2013 required tests passed :)", BUILD SUCCESSFUL in 2m 39s (`build/build2/B37-gametest.txt`, `B37-gametest-latest.log`). Same as B36.
- Added: `LegacyMainTestGuardrailTest` (fails if a test class brings back a `main()`).
- Changed: `PrivateTestHelperRatchetTest.CEILING` 593 to 101 (tightened, not loosened). `NpcTradeGuardrailTest`'s message points at `NPC_DIALOGUE_AND_TRADE.md`. The converted tests assert what they asserted before; only the runner changed.
- Removed: `LegacyMainTestAdapterTest`, `LegacyMainTestSuiteContractTest`, `LuxUmbraCombatContractTest` (kept in `removed_assets/src/test/`).
- `python gradle/source-text-impact-map.py` regenerated `docs/architecture/evidence/source-text-impact-map.csv` (3,775 rows, 619 tests).
- No review client: no row changes a look, a screen or a shader.

### Docs

- Updated: `HEMOMANCY_REFERENCE.md`, `LORE_REFERENCE.md`, `MANIPULATIONS_AND_CASTING.md`, `CORE_LOOP.md`, `NPC_DIALOGUE_AND_TRADE.md`, `BRINED_COURT.md`, `BOSSES.md`, `CIRCUS.md`, `CARDINAL_RITES.md`, `SAINT_MAUSOLEUMS.md`, `ENDGAME_PATHS.md`, `PELAGIC_ECOLOGY.md`, `PELAGIC_WORLDGEN.md`, `PLAYTHROUGH_TESTS.md`, `HEMATIC_SUCCESSION_DESIGN_SPEC.md`, `MANIPULATION_VISUALS.md`, `TESTING.md` (UN-71 note, review rows no doc named, scenario headings), `README.md`, `architecture/ARCHITECTURE_STANDARDS.md` (guardrail index, the JUnit rule), `AGENTS.md` (names `CardinalRitePurposeTest`), `audits/2026-11-02-cleanup/README.md` (section G worked), the wiki, and the new `archive/done/HEMOMANCY_REFERENCE_DATED_NOTES.md`.
- `docs/OPEN_QUESTIONS.md`: section 3 rows "16 T7 (FX-21), ready parts", "16 T7 (FX-21), tables", "16 T7 (FX-21), section G" and "UN-71" deleted. No section 1 or 2 row carried these ids. Section 4 gained "B37 (docs)".
- Ruling recorded: FX-21 now reads built (2026-11-16, B37) under "Rulings that bind" in `NPC_DIALOGUE_AND_TRADE.md`.
- Chunk status lines: chunk 16 T7 built; chunk 17 gained a UN-71 line.

### For the owner

- Changed lang keys (no new ones): `hemomancy.rite.hematic_unbinding.desc`, `hemomancy.vicar.adept.brotherhood_lore`, `hemomancy.lady.discerning.v0.follow`, `advancements.hemomancy.vesper_defeated.description`, `hemomancy.dialogue.event.vicar_hermit_road_known`. The Vicar's line now reads "That road is reported and entered already. The ledger keeps one entry."
- Values chosen: none in game. The converted tests' method name `allChecksPass` is mine.
- Surprising: the reference's skill table lacked 37 of 80 skills and had seven wrong Degrees; the retired manipulations are gone from the registry although the reference said they stayed; two effects (Arcane Resonance, Sanguine Clarity) were still documented after the MnA removal; the Living Blade's "25 damage" was its speed argument (its damage argument is 3), and the Barbed Blade and Chitinite Mace are plain swords with no attributes of their own.
- Section G is not fully closed: see Skipped. The reference's per-entity and per-advancement tables still list only part of what is registered; the rest are named by id.

### Verification (round 1)

- Unit suite, run again: BUILD SUCCESSFUL, 6,200 tests, 0 failures, 0 errors, 0 skipped (`build/build2/B37-verify-unit.txt`).
- Combined GameTest run, run again: "All 2013 required tests passed :)" (`build/build2/B37-verify-gametest.txt`).
- Scope: main code changes are comments, five unused `ChamberVisitService` constants (in the manifest), three Liber pages' `"decal": false` and five lang lines. Nothing held or reversed, no lash numbers, no HUD.
- Tests: all 288 `allChecksPass` methods carry `@Test` and each class has a result file. Per file, no converted or helper-swapped test lost an assertion call; the only drop is the moved `LegacyMainTestSuiteContractTest`. The ratchet tightened (593 to 101).
- Values spot-checked: tainted meat 10 mL a point (`blood_food/tainted_meat.json`) times nutrition 3 is 30 an item.
- Docs, not blocking: the "16 T7 (FX-21), section G" row was deleted whole, but the Skipped table leaves DC-15, 32, 34, 38, 46, 47, 60, 70, 85, 86, 89 (owner's call), DC-1, 58, 78 (code) and DC-22, 23, 25, 27, 62, 73, 81 undone. No row in `OPEN_QUESTIONS.md` names them now, though the audit README says they were "left for their rows". Put back a shortened section 3 row listing them.
- Result: pass.
