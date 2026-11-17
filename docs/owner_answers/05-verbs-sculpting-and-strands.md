# 05. The two verbs, Shapings (to be renamed Sculpting), strands, Staff and Living Land

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (B01, 2026-11-14) to NQ-05-1 A and NQ-05-3 to NQ-05-6 A. The tree was not committed first, so the rename is not its own commit; `refs/build2/B01-before` is the tree before it. |
| T2 | Built (P05; the last brief line in B10, 2026-11-15). |
| T3 | Built (P05; B10 2026-11-15 rewrote the five `admits` javadocs and added `lodeDrawStopsMidChannelWhenThePlayerMayNoLongerBuild` on the existing lode fixture). |
| T4 | Built (P05, then B10 2026-11-15): one rule in `TemporaryIceManager.placeOwned`, so every Frozen Cruor over a water source melts back to water (NQ-05-13), with a GameTest each for Rimebound Sentence, Glacial Rampart's R cast, an entity's Glacial Grasp and a Drudge's rampart. |
| T5 | Not built. Nothing was recorded overnight, on purpose. It no longer waits on T1: the rulings are recorded with today's word "Shaping". |
| T6 | Built (B11, 2026-11-15) to NQ-05-7 A, NQ-05-11 A and the NQ-05-8, 9, 10 and 12 defaults: `CleatsFitting` with the `hemomancy:cleats` component and the `hemomancy:takes_cleats` tag (nine boots), the fitting in `HematicArmatureBlockEntity` (250 mL, no practice), carry on lineage upgrades, `ToggleCleatsPacket` (protocol 51) and the radial slice, `HematicCleatsLayer` with the spur-only `HematicCleatsModel`, tooltips, nine JEI rows, the Artificer's lines and the Liber page. The old boot item, model hook and recipe are in `removed_assets/2026-11-15-cleats-boots/`. |
| T7 | Built (B10, 2026-11-15): the patch's lines pasted into `AGENTS.md` after a check against the code, with the Sculpting names, a rewritten lash bullet (CQ-20 B, CQ-22 A; the kits named as held by CQ-25), a no-shape-templates bullet, the dome on `DarkFields` and a HUD line. |
| T8 | Built (B10, 2026-11-15): the track files, both logs, the patch and `images/` are in `docs/archive/tracks/core_loop/`; the play script and the Track 11 ledger stay live as `docs/CORE_LOOP_PLAYTEST.md` and `docs/CORE_LOOP_TEACHING_LEDGER.md` (NQ-05-14). T5's rulings were already recorded in `CORE_LOOP.md` and `MANIPULATIONS_AND_CASTING.md`. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

- The verb framework, the Staff, Living Land and Venous Cling all stand as built. 36 of 61 rows are plain keeps and the
  code still matches them.
- The largest job is CL-3: rename "Shaping" to "Sculpting" mod-wide. It touches about 250 live files (149 Java files
  with 1,691 hits, 46 resource files, 3 test resources, 2 Gradle files, about a dozen live docs). The Temper rename
  (`c2323f8af`) was 813 files in one commit. Five naming choices are open (NQ-05-1 to NQ-05-6).
- The second largest is CN-10: Hematic Cleats stop being boots and become a fitting the Hematic Armature puts on any
  Hemomancy boots, toggled from the inner radial. It is a new design with six open choices (NQ-05-7 to NQ-05-12).
- Surprise 1: only the three lashes are strands now. The six strand skills, the twin, the pace table, the stain run
  and the per-school plant and brand looks were all answered "as built" or "yes", and each now acts on three memories
  or on none. That is 8 CONFLICT rows. None is planned.
- Surprise 2: the skill text a player reads is wrong today. Taut Line, Whipcrack and Second Strand still describe
  jets, sprays, plants and brands (`en_us.json` lines 10360, 10364, 10368). The Liber pages are still titled "The Five
  Strands" and "Eleven memories that hold".
- Surprise 3: FX-9 is already built. Round two P11 synced the Tendon Line anchor to trackers on 2026-11-11.
- Surprise 4: `AGENTS.md` does not contain the word "Shaping" at all, because the core-loop lines were never pasted
  (CQ-13). The patch's strand line is now wrong and must be rewritten before it is pasted.
- The rename needs no `PROTOCOL_VERSION` bump and changes no registry id. No payload class and no line of
  `frozen/registry-ids.txt` contains the word.
- Counts: 61 rows. BUILD 7, KEEP 36, DONE 1, MOOT 2, CONFLICT 8, UNCLEAR 0, OWNER 6, PARKED 1.

## Rulings reviewed

Paths that start with `hemomancy/` are under `src/main/java/com/vincenthuto/`.

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| CL-1 | R stays for combat casts. | KEEP | `hemomancy/common/init/ManipulationInit.java`: every converted memory keeps its cast registration. `hemomancy/common/manipulation/verb/Shapings.java` line 221: the six cut memories "cast on R as before". | T5 |
| CN-1 | The cycle key keeps skipping the two verbs. | KEEP | `hemomancy/client/event/ClientInputEvents.java` lines 125 to 130: the cycle key sends `ChangeSelectedManipPacket`, the verb key sends `BloodVerbKeyPacket`. `ChangeSelectedManipPacket` names neither verb. I did not trace the equip list itself. | T5 |
| CL-3 | Rename Shaping to Sculpting, mod-wide. | BUILD | Counted with grep (see T1). `VerbShaping`, `Shapings`, 31 implementing classes, `ShapingGuardrailTest`, 37 lang keys of the form `hemomancy.manipulation.<id>.shaping.project` or `.absorb`, 22 recipe files with a `"shaping"` field. | T1 |
| CL-4 | A claiming Shaping runs ahead of machines and reservoirs. | KEEP | `hemomancy/common/manipulation/verb/strand/StrandShaping.java` `claims` and `takesBlood`: a block that takes blood is never claimed. `EclipseDomeShaping.claims` reuses the same test. Infusion inputs and structure frames are still claimed by a lash. | T5 |
| CL-20 | Blank. Held Projection stays a weapon. | KEEP | `Shapings.java`: three lashes (`bloodLash`, `searingLashStrand`, `gloamLacerationLash`) and eight held manipulations with their own mechanic. | T5 |
| CL-21 | Converted memories keep their R cast through the first play pass, then decide. | KEEP | `ManipulationInit.java`: R casts unchanged. Note for the later decision: Blood Needle Fan, Prismatic Reproof, Hemorrhage, Conductive Mark, Insatiable Hunger and Grave Debt now have only their R cast. | T5 |
| CL-22 | Left click snaps a strand, as built. | KEEP | `ClientInputEvents.handleStrandSnap` still sends `StrandSnapPacket`. The server now asks `VerbShaping.accent`, and `takesAccent` is true on the lashes and on six of the rebuilt memories. On a lash the click lets the orb go, or cracks down the crosshair for the snap price (`StrandService.snap`). With no channel held it is the tap (`LashTapPacket`). The free reversal crack stands. Three serverbound payloads now, not one. The ruling is recorded in this wider form. | T5 |
| CL-23 | Blood Lash is the first strand. | KEEP | `ManipulationInit.java` lines 106 to 111: `blood_lash`, Animus Humilis, `setProjectionShaping(Shapings::bloodLash)`. It was re-profiled on 2026-11-12 (tap and charged whip) but is still the teaching lash. | T5 |
| CL-24 | Absorption damage names the drawer. A draw kill pays no bounty. | KEEP | `hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java` lines 312 to 314: `HemoDamageTypes.bloodAbsorption(level, user)`, with the no-bounty comment. I did not trace the bounty code. | T5 |
| CL-25 | Pace per form as built: spray 1.0, lash 0.8, plant and brand 0.6, jet 0.5, Free Hand +0.1 a level. | CONFLICT | `hemomancy/common/manipulation/verb/strand/StrandForm.java` still holds the five numbers, but only the lash reaches a player. The rebuilt memories set their own pace and Free Hand does not touch them. | none |
| CL-26 | One Absorption rider table keyed by school state. | KEEP | `hemomancy/common/damage/AbsorptionRiders.java` exists. `AbsorptionRiderEffects` line 62 still reads the brand through `StrandService.brandFor`. | T5 |
| CL-27 | Keep the off-hand second strand. | KEEP | `hemomancy/common/init/skills/LivingStaffSkillBranch.java` line 113: Second Strand, id 79, D6. `StrandService.twinLashTick` still runs. The twin now exists only on the three lashes. See CQ-22. | T5 |
| CQ-19 | Glacial Grasp's R cast should leave water when its ice melts. | BUILD | `hemomancy/common/manipulation/congeatio/GlacialGraspManip.java` line 129 calls `TemporaryIceManager.placeOwned`, which records no water. The narrow freeze (`GlacialFreezeShaping`, through `placeFor`) does. The ice hand rebuild did not touch either. | T4 |
| CQ-20 | Six strand skills. | CONFLICT | `LivingStaffSkillBranch.java` lines 93 to 116: all six exist, ids 74 to 79. `StrandSkills.java`: Taut Line's jet half and Deep Brand's set-speed half act on test-lent kits only. | none |
| CQ-21 | Whipcrack and Deep Brand stay at D5, Second Strand at D6. | KEEP | `LivingStaffSkillBranch.java`: Long Strand D2, Taut Line and Free Hand D3, Whipcrack and Deep Brand D5, Second Strand D6. Unchanged. It may move if the CQ-20 answer reshapes the line. | T5 |
| CQ-22 | Keep the twin's per-form effects. | CONFLICT | `StrandSkills.java` `TWIN_*` constants and `StrandService` (`jetPulse`, `sprayPulse`, `plantPulse`, `setBrand`) are all still there. Only the lash twin can be reached. | none |
| CQ-23 | One rider each for the three Mortem brands. | CONFLICT | `ManipulationInit.java`: Hemorrhage, Insatiable Hunger and Grave Debt carry no Shaping. `Shapings.java` line 221 records the cut. | none |
| CQ-24 | Blood Binding brands every creature it holds. | KEEP | `hemomancy/common/manipulation/animus/BloodBindingShaping.java` line 105: `StrandService.brand(player, ANIMUS, current)` for each held creature, lasting the binder's Deep Brand time. | T5 |
| CQ-25 | Keep the stain run's rules. | CONFLICT | `StrandService.trigger` and `PlantRules.stainRun` still run it, but only a memory lent a plant kit by a test can plant (`StrandGameTests` lines 1361, 857 in `FirstConversionsGameTests`). | none |
| CQ-26 | Whipcrack raises every spray pulse. | MOOT | `StrandService.sprayPulse` still does it. No registered memory sprays. Nothing a player can reach is lost. | none |
| CN-3 | Vascular Dowsing's veins toward ore, in a later visuals pass. | PARKED | `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md` P4 "Deferred": needs a new owner-only cue. Nothing built. | none |
| CN-4 | Living Land is a power, not a plant strand. | KEEP | `hemomancy/common/manipulation/animus/LivingLandShaping.java` is its own held channel. `docs/archive/tracks/core_loop/09-living-land-and-cling.md` still lists "Living Land as a strand plant" under "Still open". That line closes. | T5 |
| CN-13 | Re-check protection on every pulse of a held Shaping. | BUILD | Five Shapings skip the check on work already running: `LodeDrawShaping.admits`, `LignumMortisShaping.admits`, `PyreticForgeShaping.admits`, and `CarrionDrawShaping.admits` and `SanguineMendingShaping.admits` through `ShapingSessions.working`. | T3 |
| CN-14 | Hematic Hewing checks only the struck block in `admits`. | KEEP | `hemomancy/common/manipulation/ferric/HewingShaping.java` lines 65 to 76: struck block only, later cells checked as they break. | T5 |
| CL-16 | Living Staff digs at stone tier at the bond. | KEEP | `hemomancy/common/item/harbinger/tool/living/LivingStaffGrowthRules.java`: `BONDED(Harvest.STONE)`, then `IRON`, then `DIAMOND`. | T5 |
| CL-17 | Living Land is Animus first, Ferric second. | KEEP | `ManipulationInit.java` lines 114 to 118: `EnumBloodTemper.ANIMUS`, `setSecondaryTend(FERRIC)`. | T5 |
| CN-10 | Hematic Cleats become a fitting the Armature applies to any Hemomancy boots the player wears, from the offering bowls. They toggle from an inner radial button. | BUILD | `hemomancy/common/item/harbinger/armor/HematicCleatsItem.java` is an `ArmorItem`. `src/main/resources/data/hemomancy/recipe/hematic_cleats.json` is a crafting recipe. `ClingService.wearsCleats` tests the worn item. `HematicArmatureBlockEntity.tryProcessRestrainedPlayer` always swaps the worn piece for a fixed result. `RadialChooseManipScreen.addArmorSetAbility` shows one full-set ability and no per-piece toggle. Six choices are open. | T6 |
| CB-21 | Cleats cut cling upkeep to x0.5. | BUILD | `hemomancy/common/capability/player/harbinger/cling/ClingRules.java` line 52: `base * 0.25D`. Three lang lines say "a quarter". | T2 |
| RV-29 | The verb key also conjures and dispels the hand. | KEEP | `hemomancy/common/manipulation/verb/VerbSelection.java` `press`: conjures on an empty hand, dispels on sneak. | T5 |
| RV-30 | One reach for both verbs. | KEEP | `hemomancy/common/manipulation/verb/VerbReach.java`: `BASE = PROJECTION_REACH`, the Staff uses its focus range for both. | T5 |
| RV-31 | A Staff tap of 20 ticks or less that moved no blood calls the Primal Morphling. | KEEP | `LivingStaffItem.MORPHLING_TAP_TICKS = 20`, read at line 224. | T5 |
| RV-32 | Strands play no cast outpouring. | KEEP | `StrandShaping.presentsCast` returns false. The eight rebuilt memories do not override it, so they do play the outpouring. That is for the owner's eye (PR-20). | T5 |
| RV-33 | Sinew Cord replaces the Constrictor Cord. | KEEP | `ItemInit.sinew_cord` exists. No `ConstrictorCord` class under `src/main`. | T5 |
| RV-34 | The Shaping homes: gouge on Vascular Dowsing, trace on Hematic Beacon, item draw on Sanguine Magnetism, growth on Vital Effusion, chum by plain Projection. | KEEP | `ManipulationInit.java`: `gougeCutting`, `luxTrace`, `itemDraw` and `vitalEffusion` sit on those four memories. I did not open the chum link. | T5 |
| RV-35 | Mining as built. | KEEP | `LivingPickaxeItem`, `LivingDigging`, `LivingStaffGrowth`, `LodeDrawShaping`, `HewingShaping` all present. Numbers not re-read. | T5 |
| RV-36 | Venous Cling as built. | KEEP | `ManipulationInit.java` lines 121 to 126: Animus Humilis `VerbsOnlyManip`. `ClingService` line 136 pays upkeep through `ClingRules.upkeepPerSecond`. | T5 |
| RV-40 | Hematic Beacon's trace keeps its heading at a wall. | KEEP | `hemomancy/common/manipulation/lux/LuxTraceShaping.java` keeps a `Tip(cell, heading)`. I did not trace the wall case line by line. | T5 |
| RV-49 | The client keeps the strand pace for 6 use ticks. | KEEP | `StrandShaping.CLIENT_OPEN_GRACE_TICKS = 6`. It serves the lashes only. | T5 |
| RV-50 | Narrow Shapings are tried in attach order before the main one. An open held channel beats every claim. | KEEP | `ShapingRules.claimOrder`. `ShapingDispatcher` line 153 continues a held channel first. | T5 |
| RV-51 | Sanguine Mending's numbers. | KEEP | `SanguineMendingShaping`: `REPAIR_PER_PULSE = 10`, `HEAL_PER_PULSE = 3.0F`, `PULSES_PER_STAGE = 8`. | T5 |
| RV-52 | Crimson Flame kindles 2 lights a pulse within 2 and primes TNT only when struck. | KEEP | `FlameKindleShaping`: `RADIUS = 2`, `PER_PULSE = 2`, TNT only as the struck block. Still attached as the narrow Shaping (`ManipulationInit.java` line 401). | T5 |
| RV-53 | Glacial Grasp freezes open water and thaws vanilla and frosted ice. | KEEP | `GlacialFreezeShaping` (narrow) and `GlacialThawShaping` (Absorption) are both still attached (lines 433, 435). The main Projection Shaping is now the ice hand, which claims creatures only. | T5 |
| RV-54 | Glacial Rampart's rules. | KEEP | `GlacialRampartShaping.keepStanding`, `ShapingRules.standingTicks`. | T5 |
| RV-55 | Ferric Rampart caps 12 standing within 64 blocks. | KEEP | `FerricRampartShaping`: `MAX_SEGMENTS = 12`, `COUNT_RANGE = 64.0D`. | T5 |
| RV-56 | A blood tool beats a block's own use when the selected Shaping claims the aim. | KEEP | `hemomancy/common/event/BloodProjectionInteractionEvents.java` lines 37 to 49. | T5 |
| RV-57 | Strand skill numbers. | CONFLICT | `StrandSkills.java`: all the numbers are there. `QUICKEN_PER_LEVEL` and the 20/15/10 brand set ticks act only on test-lent kits. Same question as CQ-20. | none |
| RV-58 | Cruor Javelin is Congeatio's jet. | CONFLICT | `ManipulationInit.java` lines 814 and 815: `cruorJavelinThrow` and `cruorJavelinRecall`. It is a thrown shaft now. Still no new memory. I did not find where "3.5 spikes per block" lives. | none |
| RV-59 | First-person strand arm angles and school looks. | MOOT | `CellHandItemRenderer.applyStrandArmTransform` still has all five cases. Four serve forms no memory uses, and the lash arm now follows the whip aim. The Ferric plant pour and the prism spray have no memory. The Tenebris colours stand (`StrandRenderer.EDGE = 0xE6DDF2`). | none |
| RV-68 | Field of dark numbers. | KEEP | `hemomancy/common/world/ground/DarkFieldRules.java`: 20 ticks a light level, max 2,400, radius 3 to 8, soft edge 1.5, dry floor 0.25. `DarkFields.feed`: one field per player, centred where the light was taken. Blood Eclipse's Absorption still hangs it. Its Projection now carries a dome through the same class (`DarkFields.carry`). | T5 |
| RV-69 | Fields are session state. | KEEP | `DarkFields.FIELDS` and `CARRIED` are static maps with `clearSessionState` and `logout`. | T5 |
| RV-75 | Crimson Flame's kindling is a narrow Shaping ahead of the main one. | KEEP | `ManipulationInit.java` lines 401 and 402: `addProjectionShaping(Shapings::flameKindle)` then `setProjectionShaping(Shapings::crimsonFlamePaint)`. The main one is painted fire now, not a plant strand. | T5 |
| VX-18 | Give PLANT and BRAND per-school looks. | CONFLICT | No registered memory plants or brands through a strand. The only brand mark drawn is Blood Binding's Animus one (`StrandService.brand`). | none |
| FX-9 | Sync the Tendon Line anchor so other players see the cord. | DONE | `TendonLineShaping.sync` sends `PacketSyncTendonAnchor` with `sendToPlayerAndTrackers`. `ClingService.onStartTracking` line 154 sends it to a new watcher. `TendonLineRenderer.render` draws every player's anchor within 96 blocks. `payload-registry.txt` line 137. Built in round two P11 at protocol 49. Not seen with two clients. | none |
| CQ-13 | Paste the core loop's `AGENTS.md` lines after the CL rulings. | BUILD | `docs/archive/tracks/core_loop/AGENTS_PATCH.md`. `AGENTS.md` has no core-loop lines today. `tools/circus/*.py` is already in the tracked-tools bullet. The strand line and every "Shaping" need rewriting first (see T7). | T7 |
| CQ-14 | Move the core loop track files to `docs/archive/tracks/core_loop/`. | BUILD | `docs/archive/tracks/` exists and holds six other tracks. `docs/archive/tracks/core_loop/` holds 19 files and an `images` folder. 21 docs, 1 wiki page and 1 GameTest comment link into it. | T8 |
| PR-13 | Play: verbs and Shapings feel. | OWNER | Not playable by a reviewer. | none |
| PR-14 | Play: first conversions. | OWNER | Not playable by a reviewer. | none |
| PR-20 | Play: strands. | OWNER | The list changed on 2026-11-12. The lash is a tap and a charged, flicked whip with a capped view. Judge it in `lashTuningClient` (`gradle/review-clients.gradle` line 140). The paces, the snap and the mL/s now differ per memory. | none |
| PR-21 | Play: Staff and mining. | OWNER | Not playable by a reviewer. | none |
| PR-22 | Play: Living Land and cling. | OWNER | Not playable by a reviewer. The cleats change with T2 and T6. | none |
| PR-25 | One two-client session. | OWNER | `tools/review/review-onlooker.ps1` exists. The list grows: the relayed whip aim (`LashAimSyncPacket`), Gloam's faint lash, the dome, the beam, the volley, the javelin. | none |

## Plan

### T1. Rename Shaping to Sculpting (size L)

- Rulings: CL-3
- Change: every "Shaping" that means "a manipulation used as a verb mode" becomes "Sculpting", built to the
  recommendations of NQ-05-1 to NQ-05-6 unless the owner rules otherwise. Measured scope:
  - Java file names: 44 (38 main, 4 gameTest, 2 test). `VerbShaping`, `Shapings`, `ShapingDispatcher`,
    `ShapingSessions`, `ShapingRules`, `ShapingPulse`, `StrandShaping`, `JeiShapingIcons`, 30 other `*Shaping`
    implementations, `ShapingGuardrailTest`, `ShapingDispatcherTest`, `ShapingValidationGameTests`,
    `SecondShapingsGameTests`, `ShapingsClientReview`, `SecondShapingsClientReview`.
  - Java text: 107 main files (960 hits), 22 test files (195), 20 gameTest files (536). The heaviest identifiers are
    `ShapingSessions` (165), `VerbShaping` (160), `ShapingDispatcher` (138), `Shapings` (95), `ShapingPulse` (85), and
    the setters `setProjectionShaping` (46), `setAbsorptionShaping` (13), `addProjectionShaping` (6),
    `addAbsorptionShaping` (2).
  - No package is named for it (`common/manipulation/verb`, `verb/strand`), so no package moves.
  - Lang (`en_us.json`): 61 lines. 59 keys contain the word, 37 of them
    `hemomancy.manipulation.<id>.shaping.project` or `.absorb`. 10 values say "Shaping" to the player. 12 more lines
    say a memory "shapes" a verb.
  - Data: 22 recipe files carry a `"shaping"` field (10 `recipe/brazier_transmutation`, 12 `recipe/pyretic_forge`),
    read by `BloodInfusionRecipeSerializer` and `BrazierTransmutationRecipeSerializer`. 20 Liber pages use the word, 4
    in their file name (`ground_shapings`, `mining_shapings`, `second_shapings`, `second_shapings_walls`). 3 dialogue
    brief files. The `BloodAccounting.Source` id `"shaping"` (`BloodManipulation.SHAPING`). The advancement `shaped`.
  - Tests and build: `src/gameTest/resources/dialogue/route-fingerprints.txt` (13 lines),
    `src/test/resources/dialogue/lore-options.txt` (1), `src/test/resources/architecture/player-block-edit-allow-list.txt`
    (1 path row), the GameTest batch `shaping_validation` and task `shapingGameTestServer` in
    `gradle/game-tests.gradle`, `shapingsReviewClient` and `secondShapingsReviewClient` in
    `gradle/review-clients.gradle`.
  - Docs: 49 markdown files (1,269 hits). Live ones: `HEMOMANCY_REFERENCE.md` (99 lines),
    `MANIPULATIONS_AND_CASTING.md` (42), `ARCHITECTURE_STANDARDS.md` (15), `CORE_LOOP.md` (11), `OPEN_QUESTIONS.md`
    (15), `NPC_DIALOGUE_AND_TRADE.md` (3), `NPC_VOICES.md` (2), `README.md` (1), and three wiki pages (7 lines).
    `AGENTS.md` has none.
  - Tools: 27 files under `tools/` use the word. None is tracked by git.
  - Leave alone: "shaped" in `minecraft:crafting_shaped`, "Reshapes" in the conjure descriptions, "for shaping hematic
    iron" (`material.hemomancy.hematic_iron_knapper.desc`), `FerricConstructShapes`, `HewShape`, `ModelShapes`, the
    strand "shapes" of `StrandForm`, and "no shape templates". `PELAGIC_WORLDGEN.md` (3 lines) needs a read by hand.
  - No payload class name and no registry id contains the word, so `PROTOCOL_VERSION` stays 50 and
    `frozen/registry-ids.txt` does not change.
- Files: the 44 named Java files, `hemomancy/common/manipulation/BloodManipulation.java`,
  `hemomancy/common/init/ManipulationInit.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`,
  `src/main/resources/data/hemomancy/recipe/brazier_transmutation/`, `src/main/resources/data/hemomancy/recipe/pyretic_forge/`,
  `src/main/resources/data/hemomancy/books/libersanguinium/manipulations/pages/`,
  `src/main/resources/assets/hemomancy/dialogue_briefs/`, `gradle/game-tests.gradle`, `gradle/review-clients.gradle`,
  and the docs above.
- Tests: rename `ShapingGuardrailTest` and keep its seven checks (it reads `(set|add)(Projection|Absorption)Shaping`
  and `.getShapings(` as text). `CoreLoopTeachingGuardrailTest` reads the tooltip keys and Liber pages.
  `PlayerBlockEditGuardrailTest` needs its allow-list path row. Regenerate `route-fingerprints.txt` from its
  `.actual.txt` and diff it by hand, as round two P5 did. `CoreLoopLessonsTest`, `LiberPageRegistrationGuardrailTest`,
  `BuildRunTableGuardrailTest`, `HardCodedEnglishGuardrailTest` and `EnglishLiteralAllowListTest` must stay green.
  Regenerate `docs/architecture/evidence/source-text-impact-map.csv` with `python gradle/source-text-impact-map.py`.
  Then `./gradlew.bat alphaCheck`.
- Docs: the live docs listed above, the Mnemonist's lines against her sheet in `docs/NPC_VOICES.md`, and one line in
  `docs/CORE_LOOP.md` saying dated logs and archived tracks keep the old word.
- Depends on: the owner's answers to NQ-05-1 to NQ-05-6. The owner committing the current tree first, so the rename
  is its own commit as the Temper rename was. No other task in any chunk may be mid-edit in these files. Chunk 04
  (LP-10 strand prices), chunk 02 (ground Shapings), chunk 03 and chunk 16 (teaching text) and chunk 09 (visuals)
  touch the same files and should start after it.

### T2. Cleats discount to a half (size S)

- Rulings: CB-21
- Change: `ClingRules.upkeepPerSecond` multiplies by 0.5 with cleats, not 0.25. A wall costs 2 mL a second and a
  ceiling 3 mL a second. Speeds do not change. The three lines that say "a quarter" say "half".
- Files: `hemomancy/common/capability/player/harbinger/cling/ClingRules.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json` (lines 5973, 6019, 10711), `HematicCleatsItem.java` (its
  comment), `hemomancy/common/entity/npc/dialogue/CoreLoopLessons.java` if it carries the number,
  `src/main/resources/data/hemomancy/books/libersanguinium/manipulations/pages/venous_cling.json` if it carries it (I
  listed this page but did not read it).
- Tests: `src/test/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/cling/ClingRulesTest.java` (1.0
  becomes 2.0, 1.5 becomes 3.0). `ClingGameTests` if it pins the upkeep. `CoreLoopLessonsTest` requires a number said
  aloud to change with the code.
- Docs: `docs/HEMOMANCY_REFERENCE.md` 8.0.1, `docs/CORE_LOOP.md`, `wiki/Blood-Systems.md`,
  `docs/CORE_LOOP_PLAYTEST.md`, and CB-21 as a ruling in `docs/MANIPULATIONS_AND_CASTING.md` and
  `docs/OPEN_QUESTIONS.md`.
- Depends on: nothing. T6 rewrites the same three lang lines later.

### T3. Re-check protection on every pulse (size S)

- Rulings: CN-13
- Change: `admits` asks `ManipulationCombatHelper.mayAlter` and `PlayerBlockEdits.mayBreak` on every pulse. Remove the
  "already running" early returns in `LodeDrawShaping`, `LignumMortisShaping` and `PyreticForgeShaping`, and the
  `ShapingSessions.working` calls in `CarrionDrawShaping` and `SanguineMendingShaping`. Delete
  `ShapingSessions.working` once nothing calls it. A pulse refused mid-channel is unpaid, as a first pulse is. Cost to
  note: the NeoForge break event now fires on every pulse (about two a second per channel), so a claim mod that
  messages on refusal will message each pulse.
- Files: `hemomancy/common/manipulation/ferric/LodeDrawShaping.java`,
  `hemomancy/common/manipulation/mortem/LignumMortisShaping.java`,
  `hemomancy/common/manipulation/flammeus/PyreticForgeShaping.java`,
  `hemomancy/common/manipulation/mortem/CarrionDrawShaping.java`,
  `hemomancy/common/manipulation/ferric/SanguineMendingShaping.java`,
  `hemomancy/common/manipulation/verb/ShapingSessions.java`.
- Tests: GameTests in `FirstConversionsGameTests` and `SecondShapingsGameTests`: start a crawl, a heating and a mend,
  switch the player to adventure mode, and check the next pulse is refused and unpaid.
- Docs: the "Known defects, round two" line in `docs/MANIPULATIONS_AND_CASTING.md`, the `VerbShaping.admits` row in
  `docs/architecture/ARCHITECTURE_STANDARDS.md`, CN-13 as a ruling in the topic doc and `docs/OPEN_QUESTIONS.md`.
- Depends on: T1 (the same classes are renamed).

### T4. Glacial Grasp's R cast leaves water (size S)

- Rulings: CQ-19
- Change: Frozen Cruor placed by the R cast over a water source records the water and melts back to it, as the
  narrow freeze does. `TemporaryIceSavedData.Entry` already has the `water` flag. `placeOwned` (or the cast's call)
  sets it when the cell held a water source. NQ-05-13 asks whether Rimebound Sentence and Glacial Rampart follow.
- Files: `hemomancy/common/manipulation/congeatio/GlacialGraspManip.java`,
  `hemomancy/common/manipulation/congeatio/TemporaryIceManager.java`.
- Tests: a GameTest beside the freeze tests in `SecondShapingsGameTests`: R cast on a pond, wait on the melt by
  condition, the water is back. The raw `setBlock` count of `TemporaryIceManager` in
  `player-block-edit-allow-list.txt` must not rise.
- Docs: the Glacial Grasp row in `docs/HEMOMANCY_REFERENCE.md`, the "Second-wave Shapings" call in
  `docs/MANIPULATIONS_AND_CASTING.md`, CQ-19 as a ruling in `docs/OPEN_QUESTIONS.md`.
- Depends on: nothing. It touches no `*Shaping` file.

### T5. Record the kept rulings (size S)

- Rulings: CL-1, CN-1, CL-4, CL-20, CL-21, CL-22, CL-23, CL-24, CL-26, CL-27, CQ-21, CQ-24, CN-4, CN-14, CL-16, CL-17,
  RV-29 to RV-36, RV-40, RV-49 to RV-56, RV-68, RV-69, RV-75
- Change: no code. Move each from "Open questions" or "Calls made without the user" to "Rulings that bind", in
  today's wording. CL-22 is written as: the attack button is the accent of whatever holds the channel
  (`takesAccent` / `accent`), and the tap when nothing is held. RV-75 is written as: the kindling is narrow, ahead of
  the painted fire. RV-32 is written for the three lashes. Close "Living Land as a strand plant" (CN-4). Mark FX-9
  done.
- Files: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/CORE_LOOP.md`, `docs/OPEN_QUESTIONS.md`,
  `docs/HEMOMANCY_REFERENCE.md` where a row still says "pending".
- Tests: none.
- Docs: as above.
- Depends on: T1, so the rulings are written once with the new word.

### T6. Hematic Cleats as an Armature fitting (size L)

- Rulings: CN-10, with CB-21's number
- Change: a new design. What the code does today and what the design needs:
  - Today the cleats are boots (`HematicCleatsItem`), crafted from hematic iron boots, 2 scrap, 4 bone and a Sinew
    Cord. `ClingService.wearsCleats` tests the feet slot for that item.
  - The Armature holds a restrained player and four bowls (head, chest, legs, feet), one item a bowl
    (`ArmatureUpgradeRules.BOWL_STACK_LIMIT = 1`). An `ArmatureUpgradeRecipe` matches one worn base and one reagent,
    takes blood from the Armature's tank, and replaces the worn piece with a fixed result, copying damage and
    `CUSTOM_DATA` (`HematicArmatureBlockEntity` lines 159 to 162). All 48 recipes in `recipe/armature_upgrade/` work
    this way.
  - Need 1: an Armature operation that keeps the worn boots and marks them. It lives in
    `HematicArmatureBlockEntity`, not in a menu (`StationMenuGuardrailTest`).
  - Need 2: the mark. A data component registered with `registerComponentType` in `DataComponentInit` (fitted, and on
    or off).
  - Need 3: the Armature's own lineage upgrades (hematic iron to barbed to Blood Lust and on) must carry the mark
    across. They copy only damage and `CUSTOM_DATA` today.
  - Need 4: `ClingService.wearsCleats` reads the mark and the toggle. `ClingRules` keeps its numbers.
  - Need 5: an inner radial slice, shown while the worn boots are fitted. The inner ring today holds the two verbs,
    Conjure Staff and one full-set ability. The toggle needs a serverbound payload: `PROTOCOL_VERSION` 50 to 51,
    `payload-registry.txt` rewritten, `ServerboundGuards`, `ServerboundRateLimiter`, the gameplay in a service outside
    `common/network`.
  - Need 6: a worn look that sits over any boots (spurs and studs), in place of the full boot model
    `HematicCleatsModel`. Hematic iron and bone, no copper.
  - Need 7: a tooltip line on fitted boots, a JEI row in `HematicArmatureRecipeCategory`, the Artificer's `climb`
    lesson and item inquiry, the Liber cling page, the `clinging` advancement (listed, not read).
  - The station-upgrade contract (`docs/STATION_UPGRADES.md`) governs kits and rites that raise a station's stage.
    Fitting cleats is an ordinary Armature operation, so the contract's line "ordinary operation gates remain
    separate from station-upgrade eligibility" applies. The one touch point: every finished Armature upgrade records
    `ARMOR_UPGRADE` (and `ARMOR_UPGRADE_CONSECRATED`) as personal practice toward the Consecration kits. NQ-05-11 asks
    whether a fitting counts.
  - Anvil repair keeps data components, so the mark survives repair. I did not check whether Resonant Forge pattern
    work rebuilds the stack.
  - The mod is unreleased. If the old item goes, it goes outright, into `removed_assets/`.
- Files: `hemomancy/common/tile/harbinger/crafting/HematicArmatureBlockEntity.java`,
  `hemomancy/common/recipe/ArmatureUpgradeRecipe.java`, `hemomancy/common/recipe/ArmatureUpgradeRules.java`,
  `hemomancy/common/init/DataComponentInit.java`, `hemomancy/common/capability/player/harbinger/cling/ClingService.java`,
  `hemomancy/common/item/harbinger/armor/HematicCleatsItem.java`,
  `src/main/resources/data/hemomancy/recipe/hematic_cleats.json`,
  `hemomancy/client/screen/manips/RadialChooseManipScreen.java`, `hemomancy/common/network/PacketHandler.java`,
  `hemomancy/client/model/armor/HematicCleatsModel.java`, `hemomancy/client/item/ItemClientExtensions.java`,
  `hemomancy/client/event/LayerEvents.java`, `hemomancy/compat/jei/HematicArmatureRecipeCategory.java`,
  `hemomancy/common/init/ItemInit.java`, `hemomancy/common/init/CreativeTabGroup.java`,
  `src/main/resources/data/hemomancy/dialogue_inquiry/artificer/hemomancy/hematic_cleats.json`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`.
- Tests: `ClingRulesTest`, `ClingGameTests`, a new Armature GameTest (fits each boot that counts, refuses others,
  survives a lineage upgrade, the toggle turns the discount and the pace off and on),
  `PayloadProtocolVersionTest`, `ServerboundGuardsCoverageTest`, `NetworkPackageGuardrailTest`,
  `StationMenuGuardrailTest`, `HardCodedEnglishGuardrailTest`, and the frozen lists
  `src/gameTest/resources/frozen/registry-ids.txt` and `creative-tab-order.txt` if the item is removed.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (cling and Armature sections), `docs/CORE_LOOP.md`,
  `docs/MANIPULATIONS_AND_CASTING.md` (the Track 9 call becomes a ruling), `docs/STATION_UPGRADES.md` (one line on
  practice credit), `docs/NPC_VOICES.md` for the Artificer's lines, `wiki/Blood-Systems.md`,
  `docs/OPEN_QUESTIONS.md` (CN-10, and CQ-60 on the worn look).
- Depends on: NQ-05-7 to NQ-05-12. T2. Chunk 13 if it changes the Armature. Whoever reviews CQ-60 (the worn cleats
  look, not on the sheet).

### T7. Paste the core-loop lines into AGENTS.md (size M)

- Rulings: CQ-13
- Change: apply `docs/archive/tracks/core_loop/AGENTS_PATCH.md` after these corrections.
  - Still true as written, checked against the code: A1 (`PlayerBlockEditGuardrailTest.SCAN_ROOTS` lists the nine
    roots), A2 (`PlayerAnimationRegistry` lists `strand_stroke` and `venous_cling` before `somnolent_seat`), A3
    (`TemperPoolSubcommand`, `TankSubcommand`, `VatSubcommand`, `GougeSubcommand`, `CoreLoopFixtures` exist), A4
    (`VerbPresence`, `PacketSyncVerbPresence`, `ClientBridge.onlookerReach` / `onlookerSelection` / `onlookerVerb`
    exist), A6 (`bare: true` rows and `tools/review/review-onlooker.ps1` exist). A5's files exist
    (`gametest-isolation.mixins.json`, `GameTestIsolation`). The `tools/circus/` half of CQ-13 is already in
    `AGENTS.md`.
  - Part B, verbs and Shapings bullet: true. Rewrite it with the Sculpting names from T1, and add the attack-button
    hook (`takesAccent` / `accent`), `holdsChannel` and `movement`.
  - Part B, combat strand bullet: wrong now. It teaches `Strands.lash/jet/spray/plant/brand` as the way to add a
    strand. Only `Strands.lash` is on a memory. It says `StrandCue` bits 23 and up are free. Bits 23 and 24 carry the
    lash phase and accents have a fourth bit at 13 (`StrandCue.java` lines 53 to 73). It has no word on `LashProfile`,
    `LashStyle`, `LashTuning`, the whip aim (`LashAimPacket`, the server reads no flick from the look), the capped view
    (`LashCamera` through `MixinMouseHandler`) or the tap. Rewrite it after the CQ-20, CQ-22 and CQ-25 conflict
    answers.
  - Add one new bullet for the 2026-11-12 ruling: a held combat manipulation is its own class with its own mechanic,
    never a second user of another's kit and never a recolour.
  - Part B, ground marks bullet: true. Add that `DarkFields` also carries Blood Eclipse's dome (`carry`, `drop`), with
    its numbers in `EclipseDomeRules`.
  - Part B, Staff bullet and Living Land and cling bullet: true (`LivingStaffGrowth`, `LivingDigging`, `OreVeins`,
    `LivingLandLedger`, `ClingRules` exist). The cling bullet gains the cleats rule after T6.
  - Part B, Temper bullet and documentation-contract bullet: true in my part, but both say "Shaping".
  - Part B, teaching bullet: the count of 27 firsts matches `CoreLoopTeachingGuardrailTest` line 101.
  - Not checked here: the living blood, keeping, crops, world answer, GameTest determinism, review client and lesson
    bullets. Chunks 01, 02, 03 and 17 own those systems.
  - The patch has no line for the living hematic HUD of 2026-11-13.
  - The patch header says protocol 49. It is 50. No pasted line names the number.
- Files: `AGENTS.md`, `docs/archive/tracks/core_loop/AGENTS_PATCH.md`, `docs/architecture/ARCHITECTURE_STANDARDS.md` (its strand and
  Shaping rows must say the same).
- Tests: none read `AGENTS.md`. The guardrails the lines name must exist under those names after T1.
- Docs: `docs/MANIPULATIONS_AND_CASTING.md` quotes several `AGENTS.md` bullets under "Architecture rules" (the pose
  order line among them) and must be re-quoted.
- Depends on: T1, T6 for the cleats words, the conflict answers for the strand bullet, and chunk 17 if it also edits
  `AGENTS.md`.

### T8. Archive the core-loop track files (size S)

- Rulings: CQ-14
- Change: move the track files, both build logs, the applied `AGENTS_PATCH.md` and `images/` from `docs/archive/tracks/core_loop/` to
  `docs/archive/tracks/core_loop/`. Fix every link: 21 docs, 1 wiki page, 2 local tools files, the comment in
  `GroundworkGameTests.java` line 69, and the map in `docs/README.md`. NQ-05-14 asks which files stay live.
- Files: `docs/archive/tracks/core_loop/`, `docs/archive/tracks/`, `docs/README.md`, `docs/CORE_LOOP.md`,
  `src/gameTest/java/com/vincenthuto/hemomancy/gametest/GroundworkGameTests.java`.
- Tests: `CoreLoopTeachingGuardrailTest` names "the Track 11 ledger" in its failure messages. Update the wording if
  the ledger moves.
- Docs: `docs/README.md`, `docs/archive/README.md`.
- Depends on: T5 and T7 (rulings recorded and the patch applied first). The conflict answers, because the CL-25 row is
  not ruled until then.

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |
| CL-25 | As built: spray 1.0, lash 0.8, plant and brand 0.6, jet 0.5, Free Hand +0.1 a level. | Only the lash pace (0.8, with Free Hand) reaches a player. The rebuilt memories set their own: Hematic Flare 0.85, Blood Eclipse 0.8, Crimson Flame 0.8, Blood Needle Lance 0.75, Cruor Javelin 0.7, Ferric Spikes 0.7, Glacial Grasp 0.6. Synaptic Jolt sets none and moves by the plain Projection rule. Free Hand lifts none of them. | Keep the new paces as they are? And should Free Hand lift them too? (yes or no to each. I would say yes to both.) |
| CQ-20 | Six. | All six skills exist. They now train three lashes. Long Strand, Free Hand and Whipcrack act on lashes only. Taut Line's jet half and Deep Brand's set-speed half act on nothing. Deep Brand only lengthens Blood Binding's mark (30, 45, 60 seconds). Three skill tooltips still describe jets, sprays, plants and brands. | A: keep six, lash-only, and fix the text. B: keep six and make them reach the eight rebuilt memories (Long Strand their reach, Whipcrack their attack-button hit, Free Hand their pace). C: fold to fewer skills. (I would say B.) |
| RV-57 | Keep the numbers, Deep Brand 20/15/10 ticks among them. | Same as CQ-20. The lash numbers act. The jet quicken (+25% a level) and the brand set ticks (20/15/10) act only on kits lent to tests. | Answered by CQ-20. If A or C: may the dead numbers be deleted? |
| CQ-22 | As built: trailing lash at half damage, jet x1.4, 50-degree spray, two sunk pulses, a second brand within 4. | Only the trailing lash can happen. The other four twin effects survive in code and tests but no memory reaches them. The eight rebuilt memories have no twin. | A: Second Strand stays lash-only (D6, 4 skill points). B: each rebuilt memory gets an off-hand effect of its own (eight small designs, later). (I would say A now.) |
| CQ-23 | One rider each for the three Mortem brands. | Hemorrhage, Insatiable Hunger and Grave Debt lost their held form on 2026-11-12. They cast on R only. There is no brand to put a rider on. | A: leave them R-only and drop this ruling. B: design each a held mechanic of its own later (not a brand). (I would say A now.) |
| CQ-25 | As built: a plant on a wet stain floods up to 20 wet cells within 8 blocks. | No memory plants now. The stain run code is intact but only a test can reach it. | A: drop the stain run with the plant kit. B: give it to a live memory, such as Ferric Spikes' run following a wet stain. (A unless you want blood on the ground to matter in a fight.) |
| RV-58 | Keep: Cruor Javelin is Congeatio's jet. | You approved the rebuild on 2026-11-12. The javelin is drawn through three stages and thrown on release, and Absorption recalls a stuck one. Still no new memory. | Confirm the thrown javelin replaces the jet? (yes or no) |
| VX-18 | Yes: give PLANT and BRAND per-school looks, with batches 5 and 10. | No memory plants or brands through a strand. The Mortem brands have no held form. Crimson Flame paints fire. The only brand mark left is Blood Binding's. | A: drop VX-18. B: spend it on the eight rebuilt memories' looks instead. (I would say A, and judge the eight in play first.) |

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-05-1 | CL-3 | What is the noun? | A: "a Sculpting", plural "Sculptings". B: "a Sculpt", plural "Sculpts". | A, because it drops into every sentence that says "a Shaping" today and matches your words. |
| NQ-05-2 | CL-3 | What is the verb, and does the "Shaped" advancement change? 12 lang lines say a memory "shapes" a verb. | A: "sculpts a verb", advancement "Sculpted". B: keep "shapes" as plain English and change only the noun. | A, so the player sees one word. "Shape" stays where it means geometry. |
| NQ-05-3 | CL-3 | Do the code names change? | A: `VerbSculpting`, `Sculptings`, `SculptingDispatcher`, `SculptingSessions`, `SculptingRules`, `SculptingPulse`, every `*Sculpting` class, `setProjectionSculpting` and the other three setters, `SculptingGuardrailTest`. B: player text and docs only. | A, because `AGENTS.md` and the guardrail index name these classes, and the Temper rename changed class names too. |
| NQ-05-4 | CL-3 | Do "narrow", "main", "claiming" and "held" keep their adjectives? | A: yes ("narrow Sculpting", "claiming Sculpting"). B: new words. | A. |
| NQ-05-5 | CL-3 | Do ids change? The lang keys `.shaping.project` and `.absorb` (37), the recipe field `"shaping"` (22 files), the blood source id `shaping`, the GameTest batch `shaping_validation`, two Gradle client rows, four Liber page ids, the Mnemonist lesson id `shapings`, the advancement id `shaped`. | A: all of them. B: only what a player or datapack author sees (lang values, the recipe field). C: none. | A. The mod is unreleased, so nothing needs migrating, and the recipe field is the one a datapack would copy. |
| NQ-05-6 | CL-3 | Which docs are rewritten? | A: live docs only (references, topic docs, standards, wiki, `AGENTS.md` lines). Build logs, audits, archive, plans and ledgers keep the old word. B: every doc. | A, as the Temper rename left dated docs alone. |
| NQ-05-7 | CN-10 | What goes in the bowl, and what happens to the `hematic_cleats` item? A bowl holds one item and a recipe reads one reagent. | A: one item. The cleats item stays, crafted without the boots (scrap, bone, Sinew Cord), is no longer wearable, and goes in the feet bowl. B: the three raw materials in three bowls, the item deleted. This needs a multi-reagent Armature recipe. | A, because it fits the Armature as built. |
| NQ-05-8 | CN-10 | How is the fitting kept, and what does it survive? | A: a data component on the boots. It survives anvil repair and is carried across Armature lineage upgrades. It cannot be removed. B: as A, but removable at the Armature for the cleats back. | A. |
| NQ-05-9 | CN-10 | Which boots count? Found: hematic iron, barbed, chitinite, prismatic, Blood Lust, Edacious, Phantasmal, Sheolic, Silent Archon, Unstained. | A: an item tag holding the nine blood-side boots, Silent Archon among them. B: the eight without Silent Archon. C: any boots at all. | A. Venous Cling's own gates decide who can cling. Unstained boots stay out. |
| NQ-05-10 | CN-10 | What does the toggle turn off, and where is its state? | A: off means no discount, no faster crawl and no spurs drawn. The state is on the boots. B: off hides the spurs only. | A. |
| NQ-05-11 | CN-10 | What does a fitting need: Armature stage, Degree, blood? Does it count as "complete an armor upgrade" practice toward the Consecration kit? | A: base Armature, no Degree gate beyond the cling's own, blood as a hematic iron upgrade costs, no practice credit. B: as A, with practice credit. | A, or the cheapest fitting becomes the cheapest proof. |
| NQ-05-12 | CN-10 | The worn look. Round two P11 built a full cleated boot model. On any boots it must be an overlay. | A: spurs and studs drawn over each boot's own model. B: no worn look, tooltip only. | A, in the visuals pass. It replaces CQ-60's boot. |
| NQ-05-13 | CQ-19 | Should every R cast that lays Frozen Cruor leave water (Rimebound Sentence, Glacial Rampart), or only Glacial Grasp? | A: one rule for all Frozen Cruor. B: Glacial Grasp only. | A. One rule is simpler to teach and to test. |
| NQ-05-14 | CQ-14 | Which core-loop files stay live after the move? | A: `PLAYTEST.md` and the Track 11 ledger `11-progression-teaching-tests.md` stay live (moved to `docs/` root). Everything else is archived. B: archive all of it. | A. The play-test script serves six PR rows, and a guardrail and the documentation contract both name the ledger. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| CL-3 | Answer NQ-05-1 to NQ-05-6, then commit the tree so the rename is its own commit. | Owner | nothing |
| CL-3 | The rename itself. | Build | T1 |
| CN-10 | Answer NQ-05-7 to NQ-05-12. | Owner | nothing |
| CN-10, CB-21 | The cleats number, then the Armature fitting. | Build | T2, T6 |
| CN-13, CQ-19 | Per-pulse protection check. Water under the R cast's ice. | Build | T3, T4 |
| CQ-13, CQ-14 | `AGENTS.md` lines, then the archive move. | Build | T7, T8 |
| CL-25, CQ-20, CQ-22, CQ-23, CQ-25, RV-57, RV-58, VX-18 | Answer the eight conflict questions. | Owner | nothing |
| CQ-20, CQ-22 | Fix the Taut Line, Whipcrack and Second Strand tooltips and the Liber titles "The Five Strands" and "Eleven memories that hold". They are wrong today whichever way the conflicts go. | Later | the CQ-20 and CQ-22 answers |
| CQ-22, CQ-25, CQ-26, RV-59 | Decide whether the jet, spray, plant and brand kits, their renderer and arm cases, and their tests are deleted or kept for reuse. | Later | the CQ-20, CQ-22 and CQ-25 answers |
| CL-21 | Decide whether converted memories keep their R cast. | Owner | PR-20 played |
| PR-13 | Play the verbs: the Y key's three jobs, the Staff tap, the reticle, long channels. | Owner | nothing |
| PR-14 | Play the first conversions. | Owner | nothing |
| PR-20 | Play the three lashes in `lashTuningClient` and set the numbers (the lash ledger's Task 8 Step 3). Play the eight rebuilt memories. Judge whether they should play the cast outpouring (RV-32). | Owner | nothing |
| PR-20 | Eleven deferred minors in `.superpowers/sdd/2026-11-12-lash-rework/progress.md`. | Later | PR-20 played |
| PR-21 | Play the Staff and mining. | Owner | nothing |
| PR-22 | Play Living Land and the cling. | Owner | T2, and T6 for the cleats |
| PR-25, FX-9 | One two-client session: lashes and the relayed whip aim, twins, cling, the Tendon Line cord, fields of dark, the dome, beam, volley and javelin. | Owner | nothing |
| CN-3 | Vascular Dowsing's veins toward ore and "Lode Draw follows the veins". | Parked | a later visuals pass |
