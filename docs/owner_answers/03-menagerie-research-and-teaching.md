# 03. Menagerie, research, Liber reveal and the firsts

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P05). |
| T2 | Built in part (P05): Brought On and Carried by the Vein wear the goal frame. Left: Along the Red, held by the owner (CQ-45: what a plant strand is, explained first). Not touched in B10. |
| T3 | Built (P05). The owner has not judged the scale 3 shot yet. |
| T4 | Built (P05; B10 2026-11-15 closed it). NQ-03-5: all kinds can go in vessels, so no kind joins the tag. The Mnemonic Whale (4.2 wide, 0.75 drawn is 3.15 against a 3-vat vessel's 2.66) fits none; logged for the owner, nothing changed. |
| T5 | Built (B10, 2026-11-15) to NQ-03-1 A, NQ-03-2 (half), NQ-03-3 A and NQ-03-4: half the Temper from an unread kind, wild and kept, from the first draw; the action-bar line, the vessel and Bestiary lines, the Liber page, the Alchemist's lesson and the `BloodResearch` comment. |
| T6 | Built (B10, 2026-11-15): the seven school pages teach the held forms that are built, the six cut memories read R-only (CQ-23 A), and the pin catches "costs N mL" and any cut held form. The vessel screen has a `ScreenChromeReview` scene (RV-60 follow-up). |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

Most of this chunk is "keep what round two built", and the code still matches: the brood's numbers, who breeds, the met
tier, the vessel screen, the Degree floors and the grant sites all read as the sheet says.

The one real reversal is CL-12. Unread blood must now be weaker, not only unlabelled. The code has two places where a
creature's kind sets what a drawer gains (`BloodAbsorptionItem.draw` for wild creatures, `KeptSpecimen.draw` for kept
ones) and one place that says whether a kind is read (`BloodResearch.knows`). The change is small in code. What "weaker"
means is not decided: less Temper, less blood or both, by how much, and whether it bites before Degree 2, when the
Microscope first exists. Those are NQ-03-1 to NQ-03-4.

Surprises from the code:

- Along the Red cannot be earned any more. It is granted only when a plant strand runs along a wet stain, and since the
  2026-11-12 "no shape templates" work only GameTests build a plant strand. This touches CQ-45 (27 firsts as built) and
  the Along the Red third of CN-6.
- The eight school pages were last written 2026-11-11 11:59. They still describe held jets, sprays and plants for
  memories that lost or changed them on 2026-11-12 (Needle Fan, Blood Eclipse, Hematic Flare, Ferric Spikes and more).
- CR-26's "can be leashed but fit no vessel" is out of date. Since vats join (2026-11-13) the Abhorrent Thought, the
  Mortarbound and the Excoriated all fit a joined vessel. Only the Mnemonic Whale still fits nothing.
- The Titration Bandolier work does not touch the hollow phials, the vessel screen or the firsts. They draw from
  `TemperPhials`, which the HUD rebuild left alone. It does mean a survival player has no Temper vials on the HUD, so a
  weaker draw (CL-12) needs its own signal, and one Liber line ("The phials under your blood bar show what you hold")
  is false in survival until CQ-63 is answered.
- `PROTOCOL_VERSION` is 50 now (48 when CN-8 was written). Two bumps passed without the vessel form.

Counts: 25 rows. BUILD 5, KEEP 17, DONE 0, MOOT 0, CONFLICT 1, UNCLEAR 0, OWNER 2, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CQ-27 | Brood numbers stay as built. | KEEP | `common/tile/harbinger/menagerie/BroodRules.java`: `ML_PER_HEALTH` 30, `MIN_COST` 150, `BROOD_SECONDS` 120, `BROOD_VIGOR` 75, `REST_TICKS` 6000, `YOUNG_VIGOR` 40, `YOUNG_AGE` -24000. All match. | T1 |
| CQ-28 | A Vigil and an in-range suture may finish a brood. Not hand Projection only. | KEEP | `SpecimenBrood.second` pays from either buffer with `KeptSpecimen.payBrood` and never asks how the buffer was filled. `SpecimenVessels.tendAround` (called from `SanguineVigilBlockEntity`) stops Vigor decay. I did not open the suture code itself. | T1 |
| CQ-29 | Vat breeding bypasses the ageable-mob limit on purpose, so the player has a hemomancy way to make more of any kind. Hostile kinds stay allowed. | KEEP | `SpecimenBrood.breeds`: only `EntityTypeTags.UNDEAD` and `hemomancy:menagerie_no_brood` (the Morphling Polyp) are refused. `AgeableMob` is used once, in `young`, to make a baby where the kind has one. A blaze breeds at 600 mL. The reason is not written in the code or docs yet. | T1 |
| CQ-30 | "Met" stays as built: sampling, leading in, jarring, breeding, opening the screen. Profiled kinds only. Seeing does not count. | KEEP | `BloodResearch.recordMet` and its callers (`SpecimenVessels` x4, `SpecimenBrood.born`, `LivingSyringeItem`, `BloodVialItem`, `SpecimenJarBlockItem`). It records only for a player `ClinicalBloodKnowledge.eligible` (D2, not purifying). Met is not read. See T5 for how CL-12 meets it. | T1 |
| CQ-31 | Quirks stay notes read from existing rules, for now. | KEEP | `KeptQuirks.java`, `SpecimenVessels.quirkFacts`. Nothing new is tracked. Real quirks are a later pass. | T1 |
| CQ-32 | Syringe 3 Vigor, vial 5, vial refused free on a syringe-only kind. | KEEP | `VatVigorRules.SAMPLE_COST` 5, `SYRINGE_SAMPLE_COST` 3. `SpecimenVessels.sample` returns before `kept.sample` when the profile `requiresLivingSyringe`. | T1 |
| CQ-33 | Hollow phials keep showing the count of humours on an unread kind. | KEEP | `SpecimenVesselScreen.drawBlood` and `BestiaryBloodBlock` draw `TemperPhials.drawUnread` once per Temper (1 to 3). `TemperPhials.java` is dated 2026-11-11 07:50. The HUD rebuild did not touch it. | T1 |
| CL-12 | Reversed. An unread kind's blood is weaker to that player until they examine it. Lore: body and mind do not yet understand it. | BUILD | `BloodResearch.knows` (reads `ClinicalBloodProgress.examinedSources`). Yield sites: `BloodAbsorptionItem.draw` with `TemperCharge.fillFromCreature`, and `KeptSpecimen.draw` with `VatVigorRules.temperPerDraw` and `bloodPerDraw`. Today neither asks `knows`. What, how much and from when are open (NQ-03-1 to 4). | T5 |
| CL-13 | No vat cap. One led creature at D3, two at D5, three at D7. As built for now. | KEEP | `MenagerieRules.maxLed`. No cap on vats anywhere in `common/tile/harbinger/menagerie/`. | T1 |
| CN-15 | The blaze stays bloodless to Absorption. | KEEP | `HemoEntityPredicates.NOBLOOD` lists `Blaze`. `BloodAbsorptionItem.isValidAbsorptionTarget` refuses it. `data/minecraft/blood_profiles/blaze.json` gives Flammeus and Lux, syringe only, so the vat route stands. | T1 |
| CR-26 | Keeping reads a `hemomancy:menagerie_unkeepable` tag in place of class-name checks. | BUILD | `MenagerieLeash.candidate`: `NPC_PACKAGE` and `WILL_PACKAGE` by `className.startsWith`. Bosses come from `ManipulationReactiveEvents.isBoss`, which also ends in a `startsWith("...common.entity.boss.")`. No such tag exists. The sheet's vessel-fit note is stale (see T4). | T4 |
| RV-60 | Vessel screen stays a slotless menu with no payload, 300 by 236, Met Creatures first. | KEEP | `SpecimenVesselMenu` (no `addSlot`, `addDataSlots`, `clickMenuButton`), `SpecimenVesselScreen` `WIDTH` 300 `HEIGHT` 236, `BestiaryTabState.Kind` starts with `CREATURE`. No vessel payload in `payload-registry.txt`. | T1 |
| RV-61 | Brood design stays: side by side, one keeper, parting loses what was paid, inheritance odds, hostile kinds allowed. | KEEP | `SpecimenBrood.find`, `sever`, `answers`. `BroodRules.inherits`, `INHERIT_CHANCE` 0.5. `SpecimenBrood.java` was touched 2026-11-13 for joined vats (`beside`, `lidCentre`). The rules did not change. | T1 |
| RV-62 | Brood thread arcs over the lids. Birth bloom from a block event. | KEEP | `SpecimenBrood.threadAnchor` returns `vessel.lidCentre()`, `THREAD_ARC` 0.75, `born` sends `level.blockEvent(..., 2, 0)`. `BroodThreadRenderer.java` exists. Not seen in a client by me. | T1 |
| FX-20 | Widen the panel so the creature preview shows at GUI scale 3. | BUILD | The narrow panel is the Living Bestiary's detail panel, not the vessel screen. `BestiaryTabView.previewBounds` returns null when the room is under 226 px (`PREVIEW_MIN_W` 96, gap 10, `TEXT_MIN_W` 120). At 1280 by 720, scale 3, the room is 184 px by my arithmetic. Not measured in a client. | T3 |
| CN-8 | Blank. No vessel form on the syringe draw packet for now. | KEEP | `PacketLivingSyringeDraw` carries `targetId`, an entity id. `LivingSyringeItem.sampleVessel` sends no packet. `SpecimenVessels.syringeSample` plays a sound and a line. | none |
| CQ-45 | 27 firsts and their parents, as built. | CONFLICT | All 25 files I checked under `data/hemomancy/advancement/hemomancy/` carry the parents the sheet lists. `CoreLoopTeachingGuardrailTest` pins 27. But `stain_run` (Along the Red) is granted only in `StrandService.trigger` for a plant strand, and `Strands.plant` is now called only from GameTests. 26 stand. One cannot be earned. | none |
| CQ-46 | Older pages' Degree floors stay. | KEEP | `LiberKnowledgeHelper.unlockOlderForDegree`: D1 overview, Temperament, Condenser. D2 the 16 school pages, Thelemic, two morphling pages, shared mob materials. D3 weaving, two Forge pages, stations. D5 Cradle and Nectar. D6 Annetta. D7 Canon Memories. | T1 |
| CQ-47 | School pages stay free of mL prices. | BUILD | The eight pages under `books/libersanguinium/manipulations/pages/`. One price is left: `neurotic.json` says "Axonal Transduction (PASSIVE, 150 mL per entry...". (`vivacious.json` "borrow 500 mL" is an effect, not a price.) The live price is still on the HUD in `VerbChannelOverlay`. The pages' held-form lines are stale after 2026-11-12. | T6 |
| CN-6 | Goal frame for Brought On, Along the Red and Carried by the Vein. | BUILD | `moon_brought_on.json`, `stain_run.json`, `carried_crossing.json`: all `"frame": "task"`. No test reads a frame. Two are plain edits. Along the Red waits on NQ-03-6. | T2 |
| RV-73 | What the Dead Give on every draw, even at the cap. Stoked not gated on the owner's blood. | KEEP | `CarrionDrawShaping.feed` grants after `fillFromCarrion` whatever it added. `GroundNetwork` grants `ADV_STOKED` to the run's owner when `FurnaceHeat.stoke` burns, with no blood check. | T1 |
| RV-74 | A learned memory opens its school's pages. Immaculatus floors are purity and clarity stages. Annetta's page opens when her route starts. | KEEP | `LiberKnowledgeHelper.unlockForMemory` (called from `KnownManipulationGrantHelper` and `CrudeMemoryShardItem`), `unlockForUnstained` (from `UnstainedMilestoneHandler`), `AnnettaKnowlesEntity` line 387. | T1 |
| RV-37 | "Crossing" for the Spine's trip. The first nine firsts and their parents. | KEEP | Track 11 ledger, lines 238 and 242 of `docs/CORE_LOOP_TEACHING_LEDGER.md`. `first_crack` (Whipcrack) is still granted, at `StrandService` line 414, after the lash rework. I did not re-read every "crossing" lang line. | T1 |
| PR-17 | Play the menagerie: leash, vat economy, starvation, rattle, taint, tether. | OWNER | Numbers are in `MenagerieRules` and `VatVigorRules`. "About 0.3 Temper per mL fed" is right for a 20-health kind (6 Temper for 2 Vigor at 10 mL a point). A cow gives about 0.6. | none |
| PR-24 | Play the 27 firsts and the Liber's reveal pace. | OWNER | Nothing to check by reading. Two things to know before playing are under "Still to do". | none |

## Plan

### T1. Record the keeps (size S)

- Rulings: CQ-27, CQ-28, CQ-29, CQ-30, CQ-31, CQ-32, CQ-33, CL-13, CN-15, CQ-46, RV-37, RV-60, RV-61, RV-62, RV-73, RV-74
- Change: no behaviour change. Move these rows to "Rulings that bind". For CQ-29 write the owner's reason where the
  rule lives: vat breeding is deliberately not limited to ageable mobs, so hemomancy gives the keeper a way to make
  more of any living kind. Put it in the `SpecimenBrood.breeds` javadoc and the reference. Mark CQ-31 and CL-13 "as
  built for now".
- Files: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/menagerie/SpecimenBrood.java` (comment only)
- Tests: none
- Docs: `docs/CORE_LOOP.md` ("Rulings that bind", and drop the same items from "Calls made without the user"),
  `docs/OPEN_QUESTIONS.md` section 1.8, `docs/HEMOMANCY_REFERENCE.md` (the brood paragraph),
  `docs/archive/tracks/core_loop/05-menagerie-and-research.md` ("Open questions" line)
- Depends on: nothing

### T2. Goal frames on two firsts (size S)

- Rulings: CN-6
- Change: `"frame": "task"` becomes `"frame": "goal"` in `moon_brought_on.json` and `carried_crossing.json`. Leave
  `stain_run.json` alone until NQ-03-6 is answered. Add a pin for which firsts carry the goal frame, so a later edit
  cannot drop it unseen.
- Files: `src/main/resources/data/hemomancy/advancement/hemomancy/moon_brought_on.json`,
  `src/main/resources/data/hemomancy/advancement/hemomancy/carried_crossing.json`
- Tests: extend `src/test/java/com/vincenthuto/hemomancy/architecture/CoreLoopTeachingGuardrailTest.java` (read through
  `ResourceReader`, name the goal-framed firsts)
- Docs: `docs/CORE_LOOP_TEACHING_LEDGER.md` (the R2 P9 firsts row), `docs/CORE_LOOP.md`
- Depends on: NQ-03-6 for the third file only

### T3. Bestiary preview at GUI scale 3 (size S)

- Rulings: FX-20
- Change: in `BestiaryTabView`, give the detail panel the room the preview needs on a narrow screen. By the numbers a
  wider panel alone is short by 42 px at 1280 by 720. Proposed: let the list narrow from 160 to 130 px when the detail
  panel cannot hold a preview, and lower `TEXT_MIN_W` from 120 to 108. That gives exactly the 214 px needed. If the
  review shot reads cramped, stack the preview above the text for narrow panels instead. The list width is worked out
  in four places (`draw`, `isOverList`, `entryUnder`, `previewBounds`). Make it one helper.
- Files: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabView.java`
- Tests: `src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerBestiaryTabSourceTest.java`
  and `BestiaryEntityPreviewSourceTest.java` if they pin the constants (not opened). A shot from
  `src/gameTest/java/com/vincenthuto/hemomancy/gametest/MenagerieClientReview.java` at scale 3 is the proof.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (Living Bestiary paragraph) only if it names the layout
- Depends on: nothing

### T4. `menagerie_unkeepable` tag (size M)

- Rulings: CR-26
- Change: add `data/hemomancy/tags/entity_type/menagerie_unkeepable.json`. `MenagerieLeash.candidate` reads
  `entity.getType().is(...)` and the two `startsWith` checks (`NPC_PACKAGE`, `WILL_PACKAGE`) go. The tag lists every
  mod person, every Will kind (include the existing `#hemomancy:wills`) and every mod boss, so moving a class no
  longer changes who can be kept. `ManipulationReactiveEvents.isBoss` keeps its own package line. About 25 other call sites
  use it and it is outside this row. Keep the three refusal lines (boss, person, Will) telling them apart. The simplest way
  is two small tags the main tag includes (people, Wills) plus a new plain "cannot be kept" verdict for anything else
  in the tag. `MenagerieRules.Candidate` gains one field. Add the kinds NQ-03-5 names.
- Facts for NQ-03-5, from `MenagerieRules.vatFits` and the sizes in `EntityInit`: the Abhorrent Thought (1.5 by 3.25, 40
  health) fits a vessel 2 vats across and 2 high. The Excoriated (1.55 by 4, 52 health) fits the same. The Mortarbound
  (0.9 by 3.0, 24 health) fits 1 across and 2 high. The Mnemonic Whale (4.2 wide) fits nothing.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/menagerie/MenagerieLeash.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/entity/menagerie/MenagerieRules.java`,
  `src/main/resources/data/hemomancy/tags/entity_type/` (new files),
  `src/main/resources/assets/hemomancy/lang/en_us.json` (one refusal key)
- Tests: `src/test/java/com/vincenthuto/hemomancy/common/entity/menagerie/MenagerieRulesTest.java` (the new verdict).
  A new guardrail under `src/test/java/com/vincenthuto/hemomancy/architecture/`: every entity type whose class sits in
  `common/entity/boss`, `common/entity/npc` or `common/entity/mob/monster/will` is in the tag, and `MenagerieLeash` has
  no class-name check. `MenagerieGameTests.oneRuleRefusesPeoplePetsAndBossesForLeashJarAndVat` gains a tagged kind.
  Regenerate `docs/architecture/evidence/source-text-impact-map.csv`.
- Docs: `docs/architecture/ARCHITECTURE_STANDARDS.md` (the "Keeping a creature" row and the guardrail index),
  `docs/HEMOMANCY_REFERENCE.md` (menagerie section and the Specimen Jar paragraph), `docs/CORE_LOOP.md`,
  `wiki/Blood-Systems.md`
- Depends on: NQ-03-5 for the extra kinds. The tag itself can be built without it.
- Note: `AnimalTaint` also asks `MenagerieLeash.verdict`, so a tagged kind also refuses the Projection dose.

### T5. Unread blood is weaker (size M)

- Rulings: CL-12, and how it meets CQ-30 and CQ-33
- Change: one pure rule for "what share of a kind's yield this player takes", 1.0 when `BloodResearch.knows` (a Creative
  player always knows), the unread share otherwise. Put it beside `BloodResearch` in
  `common/mission/alchemist/`. Apply it at the two yield sites:
  - Wild: `BloodAbsorptionItem.draw`, on the Temper given by `TemperCharge.fillFromCreature` (and on `returned` if
    blood is cut too).
  - Kept: `KeptSpecimen.draw`, on `temper` and the inherited half share (and on `blood` if blood is cut too).
  Not changed: the hurt done to the creature, the Vigor a pull costs, the acclimation thresholds. Record the reduced
  amount as exposure.
- The values wait on the owner. My defaults, to be confirmed: Temper only (NQ-03-1 A), half (NQ-03-2 A), at every
  Degree (NQ-03-3 A), wild and kept draws but not injection (NQ-03-4 A).
- How it meets CQ-30: the cut keys on read (`knows`), never on met. Meeting a kind lists it in the Bestiary and
  changes nothing else. A met, unread entry should now say its blood runs weak until read.
- How it meets CQ-33: the hollow phials stay and still show the count. Add one line under them on the vessel screen
  and in the Bestiary. The yield line stays hidden until studied, as now.
- The player must be able to see it. With the Bandolier creative-only there are no Temper vials on a survival HUD.
  Add a throttled action-bar line on a weak draw (the way `SpecimenVessels.absorb` speaks once a second), and say it
  on the vial tooltip (`item.hemomancy.bloody_vial.unknown`), the vessel line
  (`message.hemomancy.menagerie.readout.unread`) and the Bestiary (`bestiary.hemomancy.creature.unread`,
  `bestiary.hemomancy.status.met`).
- Teaching text that now says the opposite: `unread_humours.json` ("A creature's blood acts the same whether or not
  you know it"), the `BloodResearch` class comment, and the Alchemist's "reading a creature" lesson (write it to his
  sheet in `docs/NPC_VOICES.md`).
- Files: `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/BloodResearch.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/menagerie/KeptSpecimen.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/TemperCharge.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SpecimenVesselScreen.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryBloodBlock.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodVialItem.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`,
  `src/main/resources/data/hemomancy/books/libersanguinium/the_hematic_order/pages/unread_humours.json`
- Tests: a new JVM test for the rule. `MenagerieGameTests.bloodIsUnreadUntilItsKindIsExamined` grows a "weak before,
  full after" check for a kept draw, and a twin for a wild draw. `aBlazeLedHomeFeedsFlammeusAndLuxUntilNeglectKillsIt`
  draws an unread blaze with a real player, so its amounts move unless it examines first. Check
  `BloodProfileGameTests`, `HematicAcclimationGameTests`, `HematicMicroscopeGameTests` and
  `ClinicalBloodProgressionGameTests` for Temper asserts on unread kinds. If blood is cut too, every GameTest that
  absorbs from a creature with a survival player is in scope, which is a much larger retune. A changed dialogue line
  moves `src/gameTest/resources/dialogue/route-fingerprints.txt`.
- Docs: `docs/archive/tracks/core_loop/05-menagerie-and-research.md` (section 4 and the third acceptance line),
  `docs/CORE_LOOP.md` ("Rulings that bind"), `docs/HEMOMANCY_REFERENCE.md` ("Research has a point (CL-12)"),
  `docs/LORE_REFERENCE.md` (the owner's reason: body and mind do not yet understand it),
  `wiki/Clinical-Blood-Tools.md`, `wiki/Blood-Systems.md`, `wiki/Hematic-Microscope.md`, `docs/OPEN_QUESTIONS.md`
- Depends on: NQ-03-1 to NQ-03-4. CQ-63 (who gives the Bandolier) decides how visible the change is on the HUD.

### T6. School pages catch up (size M)

- Rulings: CQ-47
- Change: remove the one price left on a school page ("150 mL per entry" on Axonal Transduction, `neurotic.json`).
  Rewrite the held-form lines that describe mechanics removed or rebuilt on 2026-11-12. Seen by me: Ferric Spikes
  ("plants a strand"), Needle Fan ("held it sprays"), Blood Eclipse ("held it sprays a short dark cone"), Hematic Flare
  ("held it puts out a jet"). The other ten changed memories need the same read. `strand_forms.json` and
  `second_strands.json` were already rewritten on 2026-11-12 and are the source for the new one-liners. Still no mL
  prices.
- Files: the eight files `ferric.json`, `fervent.json`, `frigid.json`, `incandescent.json`, `neurotic.json`,
  `ruinous.json`, `umbral.json`, `vivacious.json` in
  `src/main/resources/data/hemomancy/books/libersanguinium/manipulations/pages/`
- Tests: `src/test/java/com/vincenthuto/hemomancy/architecture/LiberPageRegistrationGuardrailTest.java` already holds
  "every memory named with its type". Add "no school page carries an mL price on a memory line" so CQ-47 is pinned.
- Docs: `docs/MANIPULATIONS_AND_CASTING.md` if it quotes the pages, `docs/CORE_LOOP_TEACHING_LEDGER.md`
- Depends on: the manipulation chunk's rulings on the six memories that lost their held form and on Second Strand,
  Taut Line and Deep Brand. Do the price line now and the rewrites after those rulings.

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |
| CQ-45 | 27 firsts and their parents, as built. (CN-6 also asks for a goal frame on one of them, Along the Red.) | The 2026-11-12 "no shape templates" work took the plant strand out of play. Along the Red is granted only when a plant runs along a wet stain (`StrandService.trigger`), and only GameTests build a plant now. Its Liber page (`second_strands.json`, rewritten 2026-11-12) no longer mentions a stain run. Its icon is the Ferric Spikes memory, which is now a steered run. Making it earnable as written would mean putting the plant back. | Along the Red cannot be earned. Remove it, move it to something still in play, or leave it dormant? See NQ-03-6. |

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-03-1 | CL-12 | What is weaker for an unread kind? | A: Temper only. B: blood only. C: both. | A, because Temper is the part that depends on reading the humour, the early blood economy and First Bloodcraft stay as tuned, and the retune stays inside the Temper tests. |
| NQ-03-2 | CL-12 | How much weaker? | A: half. B: a quarter. C: nothing at all until read. | A, because the player still feels the school arrive and has a clear reason to read it. C would make unprofiled play feel broken. |
| NQ-03-3 | CL-12 | From when? The Microscope lesson needs Degree 2 (`ClinicalBloodProgress.canLearn`), so a D0 or D1 player cannot read anything. | A: from the first draw, at every Degree. B: only from D2, when the player can do something about it. | A if only Temper is cut (the Microscope then pays off at D2). B if blood is cut, so the first two Degrees are not starved. Note a thick-hided kind stays weak until the player has a Living Syringe. I did not check when the syringe arrives. |
| NQ-03-4 | CL-12 | Where does it apply? | A: wild Absorption and kept draws. B: those, and an injected sample of an unread kind is weaker too. | A, because injection already needs the Injection lesson and nothing else reads a kind's yield. |
| NQ-03-5 | CR-26 | Which kinds go in `menagerie_unkeepable` beyond the people, Wills and bosses refused today? The sheet's list is stale: joined vats now hold the Abhorrent Thought, the Excoriated and the Mortarbound. | A: the Mnemonic Whale only (it can be leashed and fits nothing). B: the Whale, the Excoriated, the Abhorrent Thought and the Mortarbound. C: nothing extra. | A, because the Whale's leash is a dead end that costs upkeep, and the other three now have a vessel that fits. |
| NQ-03-6 | CQ-45, CN-6 | Along the Red cannot be earned since the plant strand left play. What happens to it? | A: remove it (26 firsts) and drop its goal frame from CN-6. B: move it to an act still in play, for example a Ferric Spikes run crossing a wet stain, and goal-frame it. C: leave it dormant until a stain mechanic returns. | A, because nothing in play touches a stain that way and its page no longer teaches it. B needs a design first. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CL-12 | Answer NQ-03-1 to NQ-03-4, then T5. | Owner | nothing |
| CR-26 | Answer NQ-03-5. The tag itself is T4. | Owner | nothing |
| CQ-45 | Answer NQ-03-6. | Owner | nothing |
| CN-6 | Two frames are T2. The third follows NQ-03-6. | Build | NQ-03-6 |
| FX-20 | Look at the scale 3 shot and say whether the narrow list reads, or whether to stack. | Owner | T3 built |
| CQ-47 | Price line now. Held-form rewrites after the manipulation chunk's rulings. | Build | manipulation chunk |
| PR-17 | Play the menagerie. If T5 lands first, an unread kind's vat gives less, so judge "about 0.3 Temper per mL" on a read kind. | Owner | T5 built, or play before it and say so |
| PR-24 | Play the firsts and the Liber pace. Know first: Along the Red cannot be earned, and `temperament/pages/temper.json` says "The phials under your blood bar show what you hold", which is false in survival while the Bandolier is creative-only. | Owner | NQ-03-6, CQ-63 |
| CN-8 | Vessel form on the syringe draw packet. "With the next protocol bump" no longer times it: the version went 48 to 50 without it. It still makes sense as a rider on the next change that touches `PacketLivingSyringeDraw` or its client state, since it re-lays the payload. Not worth a bump of its own. | Later | a syringe payload change |
| CQ-31 | Real quirks per kind. | Later | owner's call to start |
| CL-13 | Caps on led creatures and vats, revisited after play. | Later | PR-17 |
| RV-60 | The vessel screen has no `ScreenChromeReview` scene (noted in `BUILD_LOG_ROUND2.md`). Not asked for by any row here. | Later | nothing |
