# 15. The Crimson Troupe

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P15). |
| T2 | Built (P15, B27): the lesson's Degree is checked before the Peaceful refusal since 2026-11-15. |
| T3 | Built (B27, 2026-11-15): `CircusRouteRules.pactGate`, D4 and any one report, an Unstained Liberator on The Missing Understudy and no Degree, checked at the pact and the finale; five gate lines. |
| T4 | Built (P15, B27): the Waybill's `attuned` and `not_found` lines name what it seeks since 2026-11-15. |
| T5 | Built (P15, B04, B27): D-34, D-35's Vesper rule, the vacant site; Thread Ripper costs 300 mL (B04); the bystander line shows once per act (B27). |
| T6 | Built (P15, B27): FX-25 to FX-28; the arrow and follow-slot tests, the follow query on the 10-tick beat, the arrow fix for every blood needle, and the audit E leftovers (Acrobat tuck, carousel motes) since 2026-11-15. Left: the owner's look (PR-10). |
| T7 | Built (B28, 2026-11-15): 37 set-piece blocks and the four records remodelled, `tools/model_export/circus_set_pieces.py`, the generator `tools/worldgen/circus_sites.py` over `tools/worldgen/circus/` (kit, checks, sampler, preview, build), the kit sampler and its tests. The pavilion, camp and stage builders (`pavilion.py`, `camps.py`, `stage.py`) join it with T8 and T9. Left: the owner's look. |
| T8 | Built (B29, 2026-11-15): the pavilion from `tools/worldgen/circus/pavilion.py` on the kit, one 65 x 23 x 65 template (not jigsaw pieces: every test and the review client place it whole). Two real galleries (deck heights 5 and 7) with stairs at both ends, the Ringmaster's box on the upper one, all ten actors and three records placed by the generator, three painted wagons in the backyard (the third added in the B29 fix), 30 hung lanterns and the footlights checked by `checks.py`'s light rule, three authored emitters; `afterPlace` is gone. `ringmasterReachFromTheFloor` is required and walks the path. Left: the owner's look; the jigsaw split, if the owner still wants it. The old builders moved to `removed_assets/` with T9 (B30). |
| T9 | Built (B30, 2026-11-15): five 27 x 12 x 27 camps and a 17 x 10 x 15 stage wreck from `tools/worldgen/circus/camps.py` and `stage.py` on the kit, row 1 on the ground over a foundation skirt, yards not in `#minecraft:dirt`, a practice ring with room for each professor's threats; the old builders in `tools/circus/` moved to `removed_assets/`. Left: the owner's look. |
| T10 | Built (B30, 2026-11-15): `beard_box`, a sampled gate (corners, edge midpoints, an 8-block grid, 6 blocks of slope; the pavilion's 43-block core), a 6-chunk village distance and a 6-chunk exclusion of camps and stages from pavilions, spacings 48/16, 24/8, 32/10 kept (NQ-15-2); `circusValidationServer` measured the nearest pavilion at 653, 1,968 and 3,064 blocks on seeds 42, 1337 and 8675309; the `sites` scene in `CircusClientReview`. Left: the owner's call on the pavilion's rarity. |
| T11 | Built (B30, 2026-11-15): `CircusSiteStateService` lays the generator's ruin or tenancy overlay chunk by chunk, sparing changed cells; the successor's banner and the Ringmaster's tenancy line; the finale performers leave a Liberated ruin; each professor's gift (two Filaments and a frame material) through `OwedRewards` on the first passed practical. Left: the owner's look. |
| T12 | Built (P18, B29): the overlay's three vanilla particles are replaced (P18); since 2026-11-15 its cues stand on the pavilion's marks (figures on the gallery decks, cloth on the rails, lights at trapezes, rigs and the ring curb) and no longer circle the carousel. Left: the owner's look. |
| T13 | Built (B31, 2026-11-15): a saved 60-tick `BossIntroWindow`, moves as `BossMoveTimeline`s replayed through `BossReplayClock` (the entity-event clocks and `MeleeAttackGoal` are gone), `BossFootprint` shapes drawn by `CircusEffects.footprint` from the same test, `BossBands` picking, one `CircusArena` record (radius 7, leash 24, grace 200) for the acts and the finale with troupe membership by home, the Ringmaster the one phase writer (the carousel only reports destroyed; sever and anchor hits only in CAROUSEL), the CAROUSEL objective on the bar, knives for a player he cannot reach. His row in the boss tag was built in B15. Left: the owner's play and re-film. |
| T14 | Built (B31, 2026-11-15): a saved CURTAIN phase; Succession's 60-tick yield (kneel, topper off, one line, the Pattern when the beat ends, `CircusYieldScene`), then 30 ticks standing and a vault back to his perch (NQ-15-11); Liberation's 40-tick collapse with its card; a relog, death or unload in the beat does not release the site. Sound events for the six cue candidates (the carousel's as `entity.impaled_procession.*`, since `circus_carousel` is a retired name) and a music event with no track, played like Vesper's. Left: the owner's pick by ear and a music track. |
| T15 | Built (B31, 2026-11-15): NQ-15-4 A's five moves in `CircusRingmasterMoves` beside the knife and staff: the Spotlight, Sleight (doubles drawn by the renderer), the Cue (`CircusPerformerEntity.answerCue`), the Knife Board and Strings Cut (lent acts saved). NQ-15-3 A: nothing struck was built. One required GameTest per move in `CircusRingmasterFinaleGameTests`. Left: tuning and a re-film. |
| T16 | Built (B27, 2026-11-15): seven acts on the radial's Act entry through `PacketPuppetAct` (`PROTOCOL_VERSION` 55), the Juggler's pin. Left: the owner's tuning (D-37). |
| T17 | Built (B27, 2026-11-15): Ring; the Pattern leaves the transposition ring. The relay is drawn as tendrils, not a pulse in `PuppeteerThreadRenderer`. |
| T18 | Built (B27, 2026-11-15): `DialogueTheme.CIRCUS` and one troupe crest. |
| T19 | Built (P15, the 2026-11-14 docs pass, B27 to B31): every ruling is in `docs/CIRCUS.md`; the only optional Circus GameTest left is `waybillLocatesACamp`, which needs a seeded world and closes no decision. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

Every row is a REC, so the whole 2026-11-06 Circus audit direction is now accepted. Together the rows are one Circus pass of 19 tasks.
The largest pieces are the site rebuild in a new generator (pavilion with real galleries and a reachable perch, five camps, the stage), the Ringmaster on the generic boss kit, and one player-fired act per puppet body.
No Circus source file has changed since 2026-11-07 14:48, so the sheet's "where it stood" text still matches the code and there are no conflicts with the lash, HUD or strand work.
None of the footage fixes (FX-25 to FX-28) was made after the footage. All four rows are still open work.
D-30 and D-31 collide. A D4 gate on the pact may lock most Unstained players out of the Liberation that D-31 gives them (NQ-15-6).
D-39 accepts "re-role the Juggler", but the audit's own fix (P-4) re-roled the Vulture. The D-row wins. The new role is not named (NQ-15-7).
D-18 says "strike until the Circus pass", and this is that pass. I plan the mini-bosses, snake and staff form as still struck and ask (NQ-15-3).
The one vanilla flame left in Circus code is the Cinder Bellows stage demonstration (`CircusDemonstrations` line 60). It is the likely source of the flame sprites FX-27 could not place.
D-46 is already done. D-43 names no set pieces or numbers, so it becomes two questions with proposals.
Counts: 33 rows, 24 BUILD, 5 KEEP, 1 DONE, 0 MOOT, 0 CONFLICT, 1 UNCLEAR, 1 OWNER, 1 PARKED.

## Rulings reviewed

Java paths are under `src/main/java/com/vincenthuto/hemomancy/`. GameTests are under `src/gameTest/java/com/vincenthuto/hemomancy/`. JVM tests are under `src/test/java/com/vincenthuto/hemomancy/`.

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| D-14 | Nothing from the underwater deferred pool is owed to the Circus. | KEEP | `docs/CIRCUS.md` "Rulings that bind" (C-01, C-03 keep sea content with the Court). No code. | T19 (record) |
| D-18, D-33 | Move the Ringmaster onto the generic boss kit. The mini-bosses (C-13), the snake creature and the staff form stay struck. | BUILD | `common/entity/npc/circus/CircusRingmasterEntity.java`: own `tick()` phase machine, `KNIFE_THROW` and `STAFF_STRIKE` timelines, a `MeleeAttackGoal`, no `BossIntroWindow`, no `BossReplayClock`, no arena record. No ladder, snake or staff form in `src`. | T13, T15, T19 |
| D-29, PT-57 | Real galleries and a reachable perch for everyone. Acclimation changes only the look. | BUILD | `gametest/playthrough/PlaythroughCrimsonTroupeGameTests.java` `ringmasterReachFromTheFloor` (still `required = false`). `client/screen/overlay/CircusPerceptionOverlay.java` is the only acclimation reader besides the two thresholds in `CircusProgressRules`. No gallery or per-viewer geometry in `src`. | T8, T12 |
| D-30 | The pact or finale needs D4 and one school report. First talks keep giving acclimation. | BUILD | `CircusRingmasterEntity.handleChoice` and `beginFinale`: only the attention bit and `CircusProgressRules.canReceivePact` (500). `CircusPerformerEntity.mobInteract` awards 50 per first talk. `CircusSchoolQuests.report` writes `quest.empty_ring`, `quest.wrong_audience`, `quest.missing_understudy`. | T3 |
| D-31 | The Unstained may run Liberation only. No Succession, no Pattern. | BUILD | No Circus class reads Unstained state (grep). `KnownSummonEvents.grantSummon` refuses on `UnstainedAccessRules.blocksHarbingerProgress` after the site is already settled, so the Pattern is lost silently. | T3 |
| D-32, PT-21, PT-58 | A D3 teaser line for the Circus. A Waybill mode that points at the abandoned stage. | BUILD | `common/entity/npc/dialogue/CircusIntroductionRules.java` `MINIMUM_DEGREE = 4`, `HIDDEN` below it. `common/item/harbinger/CircusWaybillItem.java` `targets`: next professor's camp or pavilions only. Probes `d3PuppetryHasNoPointer` and `abandonedStageHasNoPointer` exist. | T4 |
| D-34 | Puppet damage counts as the owner's. Troupe area attacks spare bystanders. The finale stays solo. | BUILD | `CircusRingmasterEntity.hurt`, `CircusPerformerEntity.hurt` and `CircusCarouselEntity.hurt` accept only a `Player` source. `CircusFireEaterEntity.strikeDefense` hits every non-troupe `LivingEntity` in the cone. `CircusKnifeProjectileEntity.canHitEntity` hits anything but troupe. | T5 |
| D-35 | Keep right-click cutting, with Thread Ripper as the prize. Thread Ripper must not work on Vesper's bodies. | BUILD | `common/entity/mob/monster/BloodDrunkPuppeteerEntity.java` `mobInteract` calls `severCaptive` (keep). `common/manipulation/ductilis/ThreadRipperManip.java` `protectedBody` does not read `VesperEncounterPuppetEvents.PUPPET_KEY`, so his puppets can be disrupted and unravelled. | T5 |
| D-36, PT-62 | A Succession yield scene. Ringmaster music. A site with no Ringmaster leaves the Waybill. Dead performers still do not return. | BUILD | `CircusRingmasterEntity.complete` finishes on the hit tick with no beat. `common/init/SoundInit.java` has no Circus event. `CircusWaybillSearch.pick` skips only sites where `isCompletedNear` is true, and a released site keeps outcome `NEUTRAL`. | T5, T14 |
| D-37 | The mental verbs live as one player-fired act per body. | BUILD | `common/mission/circus/CircusCurriculum.java` teaches seven bodies and nothing else. `common/item/harbinger/tool/MarionetteCrossbarItem.java` and `client/screen/summon/CrossbarRadialScreen.java` offer modes, focus and transposition only. | T16 |
| D-38 | The Ringmaster Pattern becomes a group act. | BUILD | `common/entity/summon/puppeteer/RingmasterConductorRules.java`: `activeCap` adds 1, `RELAY_RANGE = 32`. `PuppeteerCrossbarCommands.relayMode` only copies the mode to other equipped Crossbars. | T17 |
| D-39 | Re-role the Juggler, not the Vulture. The new role is not named. | BUILD | `common/entity/summon/MarrowSpitterEntity.java` `tickSentryFire` and `MarrowJugglerRules.throwTick` (three daggers at ticks 10, 15, 20). Audit P-4 proposed the Vulture instead. The D-row wins. | T16 |
| D-40 | Puppet Interference keeps both penalties. No Court and Circus synergy. | KEEP | `MorphlingUpkeepRules.PUPPET_INTERFERENCE_MULTIPLIER = 1.25`, `PuppeteerSummonRules.MORPHLING_TETHER_RANGE_MULTIPLIER = 0.75`, `MORPHLING_THREAD_UPKEEP_MULTIPLIER = 1.5`. | T19 (record) |
| D-41 | Circus NPCs move to the jointed rig later. Faculty costumes are not exempt from the no-brass rule. | PARKED | Eleven `client/model/entity/npc/Circus*Model.java` files, none on the jointed rig. `tools/circus/build_faculty.py` still paints `'brass'` and gold piping (lines 158, 183, 226, 466, 532). | none |
| D-42 | Rebuild the pavilion and camps in a new generator. Drop the old arena. | BUILD | `tools/circus/build_troupe_assets.py` exists and by the audit cannot run (not run here). `tools/worldgen/` has no Circus generator. Templates `crimson_troupe_pavilion.nbt` (2026-11-07) and `troupe_camp_*.nbt` (2026-10-08) are the old ones. | T7, T8, T9, T10 |
| D-43 | Set pieces and rarity are to be set out in the rebuild. Neither is decided. | UNCLEAR | `data/hemomancy/worldgen/structure_set/circus_pavilion.json` 48/16, `troupe_teaching_camps.json` 24/8, `troupe_abandoned_stages.json` 32/10. The answer names no set piece and no number. | NQ-15-1, NQ-15-2 |
| D-44 | No chest loot. Gifts come through NPCs. A Liberated pavilion is a visible ruin. A Succeeded one shows its new tenancy. | BUILD | `CircusRingmasterEntity.authorRuin` places six cobwebs. No `loot_table/chests` entry for the Circus. No gift code in the Circus packages. I read "ruin or new tenancy" as one look per ending. | T11 |
| D-45 | Guest pay stays as built: collected from a Threadkeeper, one act a day for the whole school. Fix the ledger line. | BUILD | `CircusSchoolQuests.guestEligible` uses one `guest.day` key. Lang `hemomancy.circus.school.ledger.guest.available` says "repeat that professor's exercise ... once per Minecraft day", which reads as per professor. | T1 |
| D-46 | One to two more minutes on `alphaCheck` for the compact finale is accepted. | DONE | `gradle/game-tests.gradle` line 6: `crimson_troupe_validation` has `alpha: true`. `gametest/playthrough/CrimsonTroupePavilionGameTests.java` holds both finales in that namespace. | none |
| PT-59 | Recheck Peaceful, then refuse a practical in Peaceful with a line. | BUILD | `common/mission/circus/CircusPracticalController.java` `begin` spawns two `EntityType.HUSK`. No difficulty check. Probe `practicalsInPeaceful` is `required = false`. I could not run it. | T2 |
| PT-60 | Give the Threadkeeper an option to its lesson node. | BUILD | `common/entity/npc/dialogue/CircusSchoolDialogue.java` `tree`: the `lesson` node is built for every role, the `take_lesson` option only for roles with a curriculum lesson. Lang `hemomancy.circus.school.circus_threadkeeper.lesson` exists and is never shown. | T1 |
| PT-61 | Separate lines for a busy site, a settled site, and a visitor with no route. | BUILD | `hemomancy.circus.finale.claimed` is sent for every failed `begin` in `CircusRingmasterEntity.beginFinale` and `CircusPerformanceController.beginNext`. `finale.not_ready` names Succession to a neutral visitor. | T1 |
| PT-63 | A killed rehearsal threat ends the practical with the retry line. | BUILD | `CircusPracticalController.cleanup` discards a dead target. `tick` never checks the target count, so the session waits for its 6000-tick expiry. | T2 |
| HN-9 | A Circus theme and crest for the trade box, with the pass. | BUILD | `common/entity/npc/dialogue/DialogueTheme.java` has `BLOOD`, `UNSTAINED`, `FUNGAL`, `STILL`, `STILL_MATERIAL`. `client/screen/dialogue/DialogueScreen.java` `crest()` falls back to the default sprite, with the comment "the Circus has none yet". | T18 |
| RV-15 | Keep the trade and visual calls: leavings card dropped, D4 bell carrot, act-themed performer cards, models authored from previews. | KEEP | `data/hemomancy/npc_trade/circus_ringmaster/` and one folder per performer exist. D-44 adds gifts beside the cards and does not change them. | T19 (record) |
| RV-16 | Keep round 1: a lost Ringmaster releases the site, CAROUSEL counts performers still there, the 32-cell Waybill cap, scripted lanterns, unseen telegraph looks. | KEEP | `CircusRingmasterEntity.releaseUnsettledSite`, `CircusWaybillSearch.MAX_RINGS = 32`, `tools/circus/light_pavilion.py`. Three later rows override parts of it. See the note under this table. | T19 (record) |
| RV-17 | Keep the implementation numbers: Thread Ripper, the 32-block Pattern relay, acclimation thresholds and rate, the Luminal Cicada as the one creature. | KEEP | `ThreadRipperManip` (30 ticks, 24 blocks), `ManipulationInit.thread_ripper` cost 35, `CircusProgressRules` (150, 500, 1000, 1 per 80 ticks). Three later rows override parts of it. See the note under this table. | T19 (record) |
| FX-25 | Bolder floor telegraphs, a clearer Strongman wind-up, larger knives, a fan that reads as three throws. | BUILD | `CircusEffects`, `CircusFacultyEntity`, `CircusKnifeThrowerEntity` and `client/render/entity/projectile/CircusKnifeProjectileRenderer.java` are all dated 2026-11-07 12:24 or older. Not fixed after the footage. | T6 |
| FX-26 | Knives must not stay in the player as vanilla arrows. Fix the Acrobat's vault slide and the lunge that ends inside the target. | BUILD | `CircusKnifeProjectileEntity` extends `BloodNeedleEntity`, which extends `AbstractArrow`. Nothing resets the arrow count. `CircusAcrobatEntity.vaultTo` uses `teleportTo`, which the client smooths. | T6 |
| FX-27 | Carousel poles and frame to hematic iron and crimson. Move the two lanterns in front of the carousel. Remove the vanilla flame sprites. | BUILD | `assets/hemomancy/textures/entity/npc/harbinger/circus/carousel.png` is dated 2026-09-07. The pavilion NBT is dated 2026-11-07 12:14, before the footage. `common/mission/circus/CircusDemonstrations.java` line 60 sends `ParticleTypes.FLAME`. | T6 |
| FX-28 | Fix the lingering skip prompt, the stacked sever messages, the vignette that vanishes on red, and puppets bunching on the owner. | BUILD | `client/render/cinematic/scene/CircusRingmasterScene.java` `length()` is 90 and it does not override `skippable`. `CircusCarouselEntity.severCaptive` sends a chat line. `CircusPerceptionOverlay` vignette colour is `0x780018`. | T6 |
| PR-10 | Re-film the Troupe after the fixes. | OWNER | `gradle/review-clients.gradle` has the `circusReviewClient` row. `gametest/CircusClientReview.java` films performers, puppets, lanterns, perception and both finales. | after T6, again after T15 |
| CD-8 | Delete the dead performer barks. | BUILD | `assets/hemomancy/lang/en_us.json` lines 3292 to 3305: twelve keys `hemomancy.dialogue.circus_<acrobat, fire_eater, knife_thrower, stilt_walker>.line1` to `line3`. No Java, test or GameTest reads them. | T1 |

Where a kept RV row and an accepted row disagree, the specific row wins:

- RV-16 "a lost Ringmaster releases the site" stays, and D-36 adds to it: the released site is also marked vacant and leaves the Waybill (T5).
- RV-16 "unseen telegraph looks" is overridden by FX-25 (T6).
- RV-16 "scripted lanterns" is overridden by FX-27 now (T6) and replaced by the rebuild's own lighting (T8).
- RV-16 "32 cells" stays as a count. If NQ-15-2 changes the spacing, the distance it covers changes with it.
- RV-17 "Thread Ripper numbers" keeps charge, range and cooldown. The cost is overridden by chunk 04's CB-5 (250 to 350 mL, from 35). D-35 adds the Vesper exclusion.
- RV-17 "32-block Pattern relay" is overridden by D-38. The 32 blocks stay as the group act's range (T17).
- RV-17 acclimation numbers stay. D-29 only changes what acclimation shows.

## Plan

Java paths are under `src/main/java/com/vincenthuto/hemomancy/`, GameTests under `src/gameTest/java/com/vincenthuto/hemomancy/`, JVM tests under `src/test/java/com/vincenthuto/hemomancy/`, resources under `src/main/resources/`. A path marked (new) does not exist yet.

Order: T1 to T6 need no design answer and can start now. T7 to T12 are the site rebuild and wait on NQ-15-1 and NQ-15-2. T13 to T17 are the fight and the powers. T13 should follow T8 because the arena record reads the new ring. T18 and T19 close the pass.

### T1. Dialogue and text fixes (size S)

- Rulings: PT-60, PT-61, D-45, CD-8
- Change:
  - PT-60: in `CircusSchoolDialogue.tree`, add a Threadkeeper-only option to the existing `lesson` node. New key `hemomancy.circus.school.ask_empty_ring`. The node text is already written.
  - PT-61: split `hemomancy.circus.finale.claimed` into `hemomancy.circus.finale.busy` (another performer holds the ring now, come back when their act ends) and `hemomancy.circus.finale.settled` (this pavilion's outcome is decided). Choose by `site.activeOwner()` and `site.outcome()`. Add `hemomancy.circus.finale.no_route` for a visitor on the neutral route. Keep `finale.not_ready` for Succession only.
  - D-45: reword `hemomancy.circus.school.ledger.guest.available` to say one guest act a day for the whole school, with any professor whose practical is passed, paid four Filaments. Proposed text: "After passing a practical, repeat one professor's exercise for four Filaments. The school pays one guest act a day, whichever professor you choose."
  - CD-8: delete the twelve bark keys. Copy them to `removed_assets/` first, as the store-removed-assets rule asks. Update item 13 in `docs/NPC_VOICES.md`.
- Files: `common/entity/npc/dialogue/CircusSchoolDialogue.java`, `common/entity/npc/circus/CircusRingmasterEntity.java`, `common/entity/npc/circus/CircusPerformanceController.java`, `assets/hemomancy/lang/en_us.json`
- Tests: make `PlaythroughCrimsonTroupeGameTests.threadkeeperLessonUnreachable` required. Add a required test for the busy, settled and no-route lines (CT-6 is only logged today inside `ringmasterLostLeavesSiteOpen`). Regenerate the Circus rows of `src/gameTest/resources/dialogue/route-fingerprints.txt` if the Threadkeeper's tree fingerprint moves. `HardCodedEnglishGuardrailTest` stays green.
- Docs: `docs/CIRCUS.md` (open questions to rulings), `docs/PLAYTHROUGH_TESTS.md` (D-60, D-61 closed), `docs/NPC_VOICES.md`, `wiki/Crimson-Troupe.md`
- Depends on: nothing. New lines follow the Threadkeeper and Ringmaster sheets in `docs/NPC_VOICES.md`. Chunk 16's CD-7 (split shared Circus status keys per speaker) touches the same lang block. Do T1 first or merge by hand.

### T2. Practicals: Peaceful and a lost threat (size S)

- Rulings: PT-59, PT-63
- Change:
  - Recheck first. Run `PlaythroughCrimsonTroupeGameTests.practicalsInPeaceful` alone and record whether the two Husks survive. By reading, `Monster` bodies are removed in Peaceful, so the 2026-11-07 pass was probably timing.
  - `CircusPracticalController.begin`: refuse in Peaceful with a new line `message.hemomancy.circus.school.peaceful`. Nothing is spawned. In `tick`, end a running session with the retry line if the difficulty turns Peaceful.
  - PT-63: in `tick`, count the living targets. End the session with `hemomancy.circus.school.retry` when fewer remain than the exercise needs (two for the Scarlet Mummer and Cinder Bellows, one for the rest).
- Files: `common/mission/circus/CircusPracticalController.java`, `assets/hemomancy/lang/en_us.json`
- Tests: rewrite `practicalsInPeaceful` to expect the refusal and make it required. Make `practicalTargetKilledStalls` required. Add a guest-act case in `gametest/CrimsonTroupePracticalGameTests.java`.
- Docs: `docs/CIRCUS.md`, `docs/PLAYTHROUGH_TESTS.md` (D-59, D-63 closed), `wiki/Crimson-Troupe.md`
- Depends on: nothing

### T3. Pact gate and the Unstained rule (size M)

- Rulings: D-30, D-31
- Change:
  - D-30: the pact (both `circus_accept_pact` and `circus_reject_pact`) needs Degree 4 through `ProgressionGates.hasDegree(player, 4)` and at least one school report on record (`quest.empty_ring`, `quest.wrong_audience` or `quest.missing_understudy`). `beginFinale` rechecks both, so a forged event is refused. The Ringmaster's greeting names what is missing. The pact options show only when both are met. First talks keep their 50 points.
  - D-31: a player for whom `UnstainedAccessRules.blocksHarbingerProgress` is true cannot accept the pact, cannot repair a route into Succession, and cannot begin a Succession finale. Each refusal has a line. Liberation stays open to them. This is the same check `KnownSummonEvents.grantSummon` already makes, moved to before the site is settled.
  - Put the decision in a pure rule in `CircusRouteRules` (route, Degree, report, Unstained) so it is tested without a server.
- Files: `common/entity/npc/circus/CircusRingmasterEntity.java`, `common/capability/player/harbinger/circus/CircusRouteRules.java`, `common/mission/circus/CircusSchoolQuests.java` (a `hasAnyReport` reader), `assets/hemomancy/lang/en_us.json`
- Tests: `common/capability/player/harbinger/circus/CircusRouteRulesTest.java`. `gametest/CircusRingmasterGameTests.java` route-gate test. `PlaythroughCrimsonTroupeGameTests` `attentionActAndRoute`, `successionFinale`, `liberationFinale` (their ARRANGE states need a report). `gametest/playthrough/PlaythroughLateNpcUsabilityGameTests.java` forged-event sweep. `gametest/journey/CircusJourneyFixtures.java`. Add `circus_accept_pact` to `gametest/DialogueRouteCharacterisationGameTests.java` and regenerate its golden.
- Docs: `docs/CIRCUS.md` (the "Neither ending checks Degree" paragraph and the rulings list), `docs/HEMOMANCY_REFERENCE.md` section 17.1, `docs/LORE_REFERENCE.md` section 6.3b for the Unstained line, `wiki/Crimson-Troupe.md` (it says the apprenticeship adds no finale requirement)
- Depends on: NQ-15-5 (which report, and pact or finale) and NQ-15-6 (the D4 gate against an Unstained Liberator). Defaults if unanswered: any one report, gate at the pact, D4 for everyone.

### T4. Pointers: D3 teaser and the Waybill's stage mode (size M)

- Rulings: D-32, PT-21, PT-58
- Change:
  - D3 teaser: `CircusIntroductionRules.introductionFor` gains a teaser state at Degree 3. The Mnemonist's Circus node shows two new lines (`hemomancy.mnemonist.circus.teaser.line1`, `line2`) with no Waybill option. The lines name the kind of country the camps keep to (the plains, forests, taiga, savanna and badlands of the biome tag), because the Waybill still starts at D4.
  - Stage mode: a new structure tag `data/hemomancy/tags/worldgen/structure/troupe_abandoned_stage_targets.json` (new) holding `hemomancy:troupe_abandoned_stage`. `CircusWaybillItem.targets` returns it when the player has the Understudy's testimony and has not read the stage record (`testimony.circus_understudy` set, `clue.stage` unset) and is not sneaking. Otherwise the order is as built. The search feedback names what the Waybill is seeking.
- Files: `common/entity/npc/dialogue/CircusIntroductionRules.java`, `common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java`, `common/item/harbinger/CircusWaybillItem.java`, `common/worldgen/structure/circus/CircusWaybillSearch.java`, `assets/hemomancy/lang/en_us.json`
- Tests: make `PlaythroughInitiateGameTests.d3PuppetryHasNoPointer` and `PlaythroughCrimsonTroupeGameTests.abandonedStageHasNoPointer` required (the second reads a fixed tag list and needs the new tag added). `common/entity/npc/dialogue/CircusIntroductionRulesTest.java`. A Waybill GameTest for the stage mode beside `waybillLocatesACamp`.
- Docs: `docs/CIRCUS.md`, `docs/PLAYTHROUGH_TESTS.md` (D-21, D-58 closed), `docs/HEMOMANCY_REFERENCE.md` section 17.1, `wiki/Crimson-Troupe.md`
- Depends on: nothing. The Mnemonist's lines follow his sheet in `docs/NPC_VOICES.md`. T9 later turns the stage into a roadside wreck, and the tag keeps working.

### T5. Fight rules: puppets, bystanders, Vesper's bodies, vacant sites (size M)

- Rulings: D-34, D-35, D-36 (the Waybill half), PT-62
- Change:
  - D-34 puppets: one helper in `CircusPavilionStateRules` decides whether a damage source acts for the site owner. It is true for the owner, for a non-trial `BoundPuppeteerSummon` the owner holds, for a Blood Cur of the owner's Hound, and for their projectiles. This is the same owner resolution `CircusPracticalController.damage` already uses. Use it in the `hurt` of the Ringmaster, the performers and the carousel, and in `die`. The Mnemonist Puppet's replay must carry its body as the source entity so it counts.
  - D-34 bystanders: the Fire Eater's cone, the Stilt Walker's slam, the faculty swing, troupe knives and the Ringmaster's staff blow go through `ManipulationCombatHelper.canHarmFor`. During an act or finale they hit only the owner and the owner's bodies. A non-owner whose hit is refused is told once on the action bar.
  - D-34 solo: no change. Record it.
  - D-35: `ThreadRipperManip.protectedBody` also protects a body marked as Vesper's encounter puppet. Add a small `isEncounterPuppet(Entity)` reader in `VesperEncounterPuppetEvents` and call that. Give the refusal its own action-bar line, since "no tether" is wrong for a body on visible strings. Right-click cutting stays.
  - D-36 vacancy: `CircusPavilionSavedData.Site` gains a saved `vacant` flag. It is set when a Ringmaster is destroyed (death or discard), not on a chunk unload. `isCompletedNear` also returns true for a vacant site, so `CircusWaybillSearch.pick` moves on. The release and its line stay as built.
- Files: `common/entity/npc/circus/CircusPavilionStateRules.java`, `CircusRingmasterEntity.java`, `CircusPerformerEntity.java`, `CircusCarouselEntity.java`, `CircusFireEaterEntity.java`, `CircusStiltWalkerEntity.java`, `CircusFacultyEntity.java`, `CircusPavilionSavedData.java` (same folder), `common/entity/projectile/CircusKnifeProjectileEntity.java`, `common/entity/summon/MnemonistPuppetEntity.java`, `common/manipulation/ductilis/ThreadRipperManip.java`, `common/entity/boss/endgame/VesperEncounterPuppetEvents.java`, `common/worldgen/structure/circus/CircusWaybillSearch.java`, `assets/hemomancy/lang/en_us.json`
- Tests: `common/entity/npc/circus/CircusPavilionStateRulesTest.java`. `common/manipulation/ductilis/ThreadRipperRulesTest.java`. New GameTests in `gametest/CircusRingmasterGameTests.java`: a puppet's hit counts in DESCENT, a bystander is not burned, a saved vacant site round-trips. `CrimsonTroupePavilionGameTests` lost-Ringmaster test adds the Waybill skip. Rewrite `ringmasterLostLeavesSiteOpen` to read the vacancy and make it required. `CombatTargetingGuardrailTest` and `PersistentDataAllowListTest` (the new reader sits in a file that already reads persistent data, and its count in `src/test/resources/architecture/persistent-data-allow-list.txt` may need raising).
- Docs: `docs/CIRCUS.md`, `docs/BOSSES.md` (Vesper's puppets and Thread Ripper), `docs/MANIPULATIONS_AND_CASTING.md` (Thread Ripper), `docs/HEMOMANCY_REFERENCE.md` section 17, `docs/PLAYTHROUGH_TESTS.md` (D-62 closed)
- Depends on: chunk 04 CB-5 owns the Thread Ripper price (250 to 350 mL). Chunk 07 G-2c owns the single boss predicate that `protectedBody` should read once it exists. Chunk 06 FX-4 moves the performers' hard-coded damage into attributes and touches the same strike methods.

### T6. Footage fixes (size M)

- Rulings: FX-25, FX-26, FX-27, FX-28
- Change:
  - FX-25: troupe footprints drawn every wind-up tick, not every third or fourth, with a bone edge over a crimson body so they read on venous stone. Put the helpers in `CircusEffects`. A larger coil for the Strongman in `CircusWindupPose` and a ground wedge for the faculty swing. Knives drawn at about twice their size with a bone trail. The Knife Thrower's fan leaves as three throws three ticks apart on a wider spread.
  - FX-26: after a knife hits, take back the arrow the base class adds to the target. Play the Acrobat's vault as a snap with a covering burst: an entity event at the commit tick tells the client to drop its position smoothing for that move. Stop the lunge short of the target's box.
  - FX-27: repaint `carousel.png` and `carousel_glow.png`: poles and frame in hematic iron, canopy and trim in crimson and bone, no gold or brass. Give `tools/circus/light_pavilion.py` a keep-out around the carousel sight line and run it again so the lanterns at template (28,5,32) and (36,5,32) move. Its `--check` must still report 0 dark cells. Replace the `ParticleTypes.FLAME` call in `CircusDemonstrations` with the troupe's ember effect and take that file off the vanilla-particle allow-list.
  - FX-28: `CircusRingmasterScene.skippable` returns false once the view is handed back (tick 52), so the prompt goes with the letterbox. Sever messages move to the action bar. The vignette changes from red to a near-black edge with a thin bone line, still subtle. Following puppets take fanned slots behind the owner, with the Hulk at the rear.
- Files: `common/entity/npc/circus/CircusEffects.java`, `CircusAcrobatEntity.java`, `CircusKnifeThrowerEntity.java`, `CircusFacultyEntity.java`, `CircusCarouselEntity.java` (same folder), `common/entity/projectile/CircusKnifeProjectileEntity.java`, `client/render/entity/projectile/CircusKnifeProjectileRenderer.java`, `client/model/entity/npc/CircusWindupPose.java`, `client/model/entity/npc/CircusAcrobatModel.java`, `client/render/cinematic/scene/CircusRingmasterScene.java`, `client/screen/overlay/CircusPerceptionOverlay.java`, `common/mission/circus/CircusDemonstrations.java`, `common/entity/summon/BoundSummonBehavior.java`, `assets/hemomancy/textures/entity/npc/harbinger/circus/carousel.png`, `carousel_glow.png`, `tools/circus/light_pavilion.py`, `data/hemomancy/structure/crimson_troupe_pavilion.nbt`, `src/test/resources/architecture/vanilla-particle-allow-list.txt`
- Tests: `VanillaParticleRatchetTest` (one count leaves). `common/entity/npc/circus/CircusCarouselResourceTest.java`, `CircusTextureUvTest.java`, `CircusPresentationRegressionTest.java`, `CircusPerformerRulesTest.java`. Proof is a client: `runCircusReviewClient` with `-PcircusReviewOnly=performers,lanterns,perception,succession,liberation`, getting past the Accessibility screen first.
- Docs: `docs/audits/2026-11-06-circus/README.md` section E (mark each line fixed), `docs/CIRCUS.md` (its "Review client: none Circus-specific" line is stale, the row exists)
- Depends on: nothing. Notes: the puppet follow slots change following for every puppet, not only in the pavilion. The arrow-count fault is in `BloodNeedleEntity` for every blood needle, and T6 fixes only the knife. The flame source is the likely one, not a proven one. Check it in the client. T8 replaces the lantern work, and T12 revisits the vignette.

### T7. Circus site kit and set-piece blocks (size L)

- Rulings: D-42, D-43, FX-27 (the no-copper rule on every new piece)
- Change: a new generator `tools/worldgen/circus_sites.py` (new) over `tools/worldgen/circus/` (new: `kit.py`, `pavilion.py`, `camps.py`, `stage.py`, `checks.py`, `preview.py`, `build.py`), on the pattern of `tools/worldgen/brined_court/` and `tools/worldgen/mausoleum/`. Output is byte-identical across runs and writes only templates and pools. Set-piece blocks come from `tools/model_export/` generators, as `puppeteers_spindle.py` does today. The list is NQ-15-1. The bar is the mausoleum's: modelled multiblocks, custom set-piece blocks, no flat run of one block larger than 3 by 3. Palette is hematic iron, bone, lacquer, canvas and thread. The old builders `tools/circus/build_troupe_assets.py`, `build_teaching_camps.py`, `troupe_templates.py` and `light_pavilion.py` move to `removed_assets/` once T8 and T9 ship.
- Files: the new generator files above, `common/init/BlockInit.java`, `common/init/CreativeTabInit.java`, block models, blockstates, loot and lang for each new block, `AGENTS.md` tracked-tools bullet, `.gitignore`
- Tests: a template check on the pattern of `common/worldgen/structure/court/CourtSeatTemplateTest.java`. `GeneratedResourceShadowGuardrailTest`. `ResourceLocationStyleGuardrailTest`. Texel density at 16 px per block.
- Docs: `docs/CIRCUS.md` ("Where it lives", worldgen numbers), `docs/architecture/ARCHITECTURE_STANDARDS.md` if a guardrail is added
- Depends on: NQ-15-1. Chunk 05's CQ-13 asks to add `tools/circus/` to AGENTS.md. That line is already there. T7 changes it to name the new generator.

### T8. Pavilion rebuild with real galleries (size L)

- Rulings: D-42, D-29, PT-57, D-43
- Change: the big top as jigsaw pieces (big top, backyard, midway) from the T7 kit. Two real galleries and stairs that exist for every player. The Ringmaster's idle perch is a box on the upper gallery that a survival player reaches on foot and can click from. The Impaled Procession stays the centre. All ten NPCs and the four evidence records are placed by the generator. Light comes from the generator's own lamps, with a `checks.py` rule that no standable interior cell has block light 0. Emitters become authored cells and the `afterPlace` hook in `CircusPavilionStructure` goes.
- Files: `data/hemomancy/structure/` (new pavilion pieces replace `crimson_troupe_pavilion.nbt`), `data/hemomancy/worldgen/structure/circus_pavilion.json`, template pools (new), `common/worldgen/structure/circus/CircusPavilionStructure.java`, `common/entity/npc/circus/CircusRingmasterEntity.java` (perch and encounter origin come from the template)
- Tests: make `ringmasterReachFromTheFloor` required and rewrite its 65 by 65 scan for the new footprint. Rewrite `gametest/CrimsonTroupeStructureGameTests.java` to check roles and set pieces, not wool positions. `gametest/playthrough/PlaythroughSites.java` `pavilion` and every test that stands on it (`CrimsonTroupePavilionGameTests`, the CT band). `common/worldgen/structure/circus/CircusPavilionWorldgenResourceTest.java` (it pins the emitter call).
- Docs: `docs/CIRCUS.md` (gaps list, worldgen table), `docs/HEMOMANCY_REFERENCE.md` section 17.1, `wiki/World-Content.md`, `wiki/Crimson-Troupe.md`, `docs/PLAYTHROUGH_TESTS.md` (D-57 closed)
- Depends on: T7, NQ-15-1

### T9. Camps and stage rebuild (size L)

- Rulings: D-42, D-43
- Change: five distinct camps from the kit, each built round its discipline's apparatus, each with its professors and a Threadkeeper. The camp names and the `troupe_teacher_<camp>` tags stay (`air`, `blades`, `pageantry`, `embers`, `memory`), so `CircusCurriculum` and the Waybill do not change. Yards are floored with a block outside `#minecraft:dirt`. The abandoned stage becomes a roadside wreck of a stage wagon with a smaller box. It keeps the stage record.
- Files: `data/hemomancy/structure/troupe_camp_*.nbt` and `troupe_abandoned_stage.nbt` (regenerated), `data/hemomancy/worldgen/structure/troupe_camp_*.json`, `troupe_abandoned_stage.json`
- Tests: `common/worldgen/structure/circus/CrimsonTroupeCampTemplateTest.java`, `gametest/CrimsonTroupeStructureGameTests.java`, `PlaythroughSites` `camp` and `abandonedStage`, `gametest/CrimsonTroupePracticalGameTests.java` (practice ring positions)
- Docs: `docs/CIRCUS.md`, `wiki/World-Content.md`
- Depends on: T7, NQ-15-1

### T10. Placement, validation run and site review scenes (size M)

- Rulings: D-43, D-42
- Change: sites bring their own ground (an earthwork ring and a foundation skirt). The terrain gate samples corners, edge midpoints and a coarse grid, and allows about 6 blocks of slope, in place of every column within 3. Add `exclusion_zone` between camps or stages and pavilions, and a village distance. Spacing values come from NQ-15-2. Add a `circus_worldgen_validation` namespace and server row on the pattern of `court_worldgen_validation` (`gametest/CourtWorldValidation.java`), over seeds 42, 1337 and 8675309. It records accepted and refused cells with reasons and the nearest site of each kind. Add site scenes (approach, big top, backyard, each camp, day and night) to `gametest/CircusClientReview.java`.
- Files: `common/worldgen/structure/StructurePlacementChecks.java`, `common/worldgen/structure/circus/TroupeFootprintScan.java`, `TroupeTerrainColumns.java`, `data/hemomancy/worldgen/structure_set/circus_pavilion.json`, `troupe_teaching_camps.json`, `troupe_abandoned_stages.json`, `gradle/game-tests.gradle`, `gradle/review-clients.gradle`, `gametest/CircusClientReview.java`
- Tests: `common/worldgen/structure/circus/TroupeFootprintScanTest.java`, `common/worldgen/structure/circus/TroupeFootprintGameTests.java` (GameTest), the new validation run. Note `CircusWaybillSearch.MAX_RINGS` covers a different distance if spacing changes.
- Docs: `docs/CIRCUS.md` worldgen numbers, `docs/TESTING.md`, `docs/PELAGIC_WORLDGEN.md` (the Court keeps 96 blocks from Circus cells)
- Depends on: T8, T9, NQ-15-2. The Court's `CourtLandmarkCells` computes Circus cells from the spacing. Chunk 14 should know if the numbers change.

### T11. Site outcomes and NPC gifts (size M)

- Rulings: D-44, D-36 (survivors)
- Change:
  - Ruin: `authorRuin` and its six cobwebs are replaced by one service that swaps authored cells of the new pavilion to a ruin state on Liberation: collapsed canvas, a splintered carousel, hanging rigging. It edits loaded cells only, saves its progress on the site, and skips blocks a player placed.
  - New tenancy: on Succession the same service raises the successor's marks. What they are is NQ-15-10.
  - Gifts: no chest loot anywhere. Containers in the new templates are set dressing that cannot be opened. Each professor gives one small gift once, on a passed practical, through a `PendingDelivery` claim so a full pack or a death does not lose it. The gift table is NQ-15-10. A gift is never a memory, kit or progression proof.
- Files: `common/entity/npc/circus/CircusRingmasterEntity.java`, a new site-state service in `common/entity/npc/circus/` (new), `CircusPavilionSavedData.java`, `common/capability/player/harbinger/circus/CircusPlayerProgress.java` and `CircusProgress.java` (the owed gifts), `common/mission/circus/CircusPracticalController.java`
- Tests: `CrimsonTroupePavilionGameTests` both finales assert the new site state. A gift claim test across death and a full pack, as the finale-reward claim has. `NpcTradeGuardrailTest` stays green (gifts are not trades).
- Docs: `docs/CIRCUS.md`, `docs/NPC_DIALOGUE_AND_TRADE.md`, `docs/HEMOMANCY_REFERENCE.md` section 17.1, `wiki/Crimson-Troupe.md`
- Depends on: T8, NQ-15-10

### T12. Perception look on the new pavilion (size M)

- Rulings: D-29 (acclimation changes only the look), FX-28 (vignette), PT-57
- Change: `CircusPerceptionOverlay` anchors its cues to authored marks in the new template (gallery cells, rigging lines, the ring curb), not to the carousel. The galleries are the same blocks for everyone. An Unaware visitor sees them bare and unsettled, and an Attuned one sees them dressed and calm. The three vanilla particles (`CRIMSON_SPORE`, `REVERSE_PORTAL`, `END_ROD`) go, replaced through `HemoParticleData`. The overlay stays subtle and never decides collision, damage or route state. Remove the never-built private geometry and `circusDistortion` text from the docs.
- Files: `client/screen/overlay/CircusPerceptionOverlay.java`, `common/capability/player/harbinger/circus/CircusProgressRules.java`, `src/test/resources/architecture/vanilla-particle-allow-list.txt`
- Tests: `common/capability/player/harbinger/circus/CircusProgressRulesTest.java`, `VanillaParticleRatchetTest`, the perception scenes in `CircusClientReview` and `gametest/HudLayerReview.java`
- Docs: `docs/CIRCUS.md` ("Proof owed and known gaps"), `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md` is archive and stays as it is
- Depends on: T8

### T13. Ringmaster on the generic boss kit (size L)

- Rulings: D-18, D-33, D-34
- Change: the Ringmaster's finale runs on the generic half of the kit in `common/entity/boss/`, with a Circus controller modelled on the Court's (`common/entity/boss/court/`), not on `SaintTrialEntity`.
  - A saved `BossIntroWindow` of 60 ticks. Knives, the bat aggregate and the troupe's `beginFinale` wait until it ends (audit B-6).
  - Each move is a `BossMoveTimeline` replayed on the client through `BossReplayClock`. The hand-rolled `throwState` and `strikeState` clocks go.
  - Footprints are `BossFootprint` shapes, so the server test and the drawing share one geometry. Picking uses `BossBands`.
  - One arena record for the ring, shared by the acts and the finale, with a leash and a grace line. The troupe is gathered by finale membership, not by a 20-block box.
  - One writer of the phase. The carousel reports "destroyed" and the Ringmaster's step moves to DESCENT, so Liberation's card plays (B-7). Severing and anchor hits are gated on CAROUSEL.
  - The bar shows the CAROUSEL objective (troupe standing or anchors left).
  - A ranged answer to a player on a pillar in DESCENT.
  - The moveset itself is T15.
- Files: `common/entity/npc/circus/CircusRingmasterEntity.java`, `CircusFinaleRules.java`, `CircusPavilionStateRules.java`, `CircusCarouselEntity.java`, `CircusPerformanceController.java` (same folder), `client/model/entity/npc/CircusRingmasterModel.java`, `client/screen/overlay/boss/frame/CircusRingmasterFrame.java`, `client/render/cinematic/scene/CircusRingmasterScene.java`
- Tests: `BossAnimationClockGuardrailTest`, `CombatTargetingGuardrailTest`. `common/entity/npc/circus/CircusFinaleRulesTest.java`. New finale tests in the gated `crimson_troupe_validation` batch for the intro lock, the bar, Liberation's card, a reload mid-RAFTERS and mid-DESCENT, and the leash. `PlaythroughCrimsonTroupeGameTests` both finales.
- Docs: `docs/BOSSES.md` (the kit section says no boss has moved onto it), `docs/CIRCUS.md`, `docs/HEMOMANCY_REFERENCE.md` section 17
- Depends on: T5, T8 (the ring geometry). Chunk 07's G-2c (one boss definition) decides whether the Ringmaster counts as a boss for capture, binding and `ThreadRipperManip.protectedBody`. He lives in `common/entity/npc/circus`, so `ManipulationReactiveEvents.isBoss` does not see him today. Do not move his package to get that. Give him a row in whatever G-2c lands. If the class moves to `common/entity/boss/`, `PackageLayoutGuardrailTest` and every `isBoss` reader change with it. D-41 keeps his model off the jointed rig, so new moves pose on the current `CircusRingmasterModel`.

### T14. Yield scene and music (size M)

- Rulings: D-36
- Change: Succession ends with a yield beat on the pattern of the Kindled Champion (`common/entity/boss/court/KindledChampionRules.java`, a 60-tick beat). The blow that would kill starts the yield. He kneels on the ring, the topper comes off, he speaks one line, and the Pattern is granted when the beat ends, not on the hit tick. He does not snap back to his perch mid-burst. Liberation gets a collapse beat before the ruin (T11). Register Circus sound events in `SoundInit` using the ids the 2026-11-13 sound audit already proposes (`entity.circus_ringmaster.phase_change`, `entity.circus_ringmaster.finale_collapse`, `entity.circus_carousel.thread_sever`, `entity.circus_carousel.anchor_break`, `entity.circus_performer.windup`) in place of `ENDER_DRAGON_GROWL`, `TOTEM_USE`, `CHAIN_BREAK` and `ANVIL_DESTROY`. Add a looping `entity.circus_ringmaster.music` played for the finale as `ENTITY_VESPER_MUSIC` is.
- Files: `common/entity/npc/circus/CircusRingmasterEntity.java`, `CircusCarouselEntity.java`, `common/init/SoundInit.java`, `assets/hemomancy/sounds.json`, `client/model/entity/npc/CircusRingmasterModel.java`, a yield scene beside `client/render/cinematic/scene/CircusRingmasterScene.java` (new), `assets/hemomancy/lang/en_us.json`
- Tests: a required GameTest that the Pattern arrives only after the yield beat and that a relog during the beat still completes. `CrimsonTroupePavilionGameTests.successionFinale`. Resource test that every new sound id has a file.
- Docs: `docs/CIRCUS.md`, `docs/BOSSES.md`, `docs/audits/2026-11-13-sound/entities_calls.md` (mark the Circus rows taken), `docs/NPC_VOICES.md` for the yield line
- Depends on: T13. NQ-15-11 (what he does after yielding). The audio files are an owner pick. Candidates sit in `tools/sound_audit/candidates/`. There is no music candidate yet.

### T15. Ringmaster moveset (size L)

- Rulings: D-33
- Change: a move table for the Ringmaster built from the Circus's mental tools, on the T13 plumbing. The audit's proposal is five moves: the Spotlight (an attention cone that roots a player who looks away), Sleight (he and two doubles swap, only the real one bleeds), the Cue (every standing performer strikes on one beat), the Knife Board (knives outline the player, a dash breaks it), and Strings Cut (in DESCENT each downed performer lends him one act). Each move is bold at the Riposte bar, per `docs/MANIPULATION_VISUALS.md`.
- Files: `common/entity/npc/circus/` (a moves class, new), `CircusRingmasterEntity.java`, `CircusEffects.java`, `client/model/entity/npc/CircusRingmasterModel.java`
- Tests: one GameTest per move in `crimson_troupe_validation` (wind-up floor of 12 ticks melee and 20 area, footprint matches the hit, `canHarmFor`). Finale scenes in `CircusClientReview`.
- Docs: `docs/BOSSES.md`, `docs/CIRCUS.md`, `wiki/Crimson-Troupe.md`
- Depends on: T13, NQ-15-4. Acts 2 to 4 stay timers while the ladder is struck (NQ-15-3).

### T16. One act per body, and the Juggler's new role (size L)

- Rulings: D-37, D-39
- Change: each of the seven taught bodies gets one act the player fires from the Crossbar. Each act has a pure rules class and its own visual. No second command surface: the act is an entry on the existing Crossbar radial. A new serverbound payload carries it, paced by `ServerboundRateLimiter`, with a `PROTOCOL_VERSION` bump and a rewritten `src/test/resources/network/payload-registry.txt`. The act list and the input are NQ-15-8. The Juggler changes role (NQ-15-7), and its practical in `CircusPracticalController.damage` (three hits on one target inside 15 ticks) changes only if the new role needs it. If the per-body facts are gathered into one table while doing this (audit P-5), add a guardrail in the style of `MorphlingStrainTableGuardrailTest`.
- Files: `common/entity/summon/` (`VeinwingVultureEntity.java`, `MarrowSpitterEntity.java`, `MarrowJugglerRules.java`, `GoreboundHulkEntity.java`, `ScarletMummerEntity.java`, `SanguineHoundEntity.java`, `CinderBellowsEntity.java`, `MnemonistPuppetEntity.java`), `common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java`, `common/item/harbinger/tool/MarionetteCrossbarItem.java`, `client/screen/summon/CrossbarRadialScreen.java`, `common/network/summon/`, `common/network/PacketHandler.java`, `assets/hemomancy/lang/en_us.json`
- Tests: a rules test per act. `NetworkPackageGuardrailTest`, `EntityCastGuardrailTest`. `gametest/CrimsonTroupePracticalGameTests.java` for the Juggler. `common/mission/circus/CircusCurriculumTest.java`. The puppets scene in `CircusClientReview`.
- Docs: `docs/CIRCUS.md`, `docs/HEMOMANCY_REFERENCE.md` section 17, `wiki/Crimson-Troupe.md`, the Liber's Summons pages
- Depends on: NQ-15-7, NQ-15-8. The acts are not strands and take no strand form, per the 2026-11-12 no-shape-templates ruling.

### T17. Ringmaster Pattern group act (size M)

- Rulings: D-38
- Change: the Pattern's office becomes a group act. The Pattern still never chains and never changes ownership. The 32-block range in `RingmasterConductorRules.RELAY_RANGE` is the act's range. The relay is drawn as a pulse along each thread in `PuppeteerThreadRenderer`. What the act does, whether the Pattern still counts as a body, and whether it stays in the transposition ring are NQ-15-9.
- Files: `common/entity/summon/puppeteer/RingmasterConductorRules.java`, `PuppeteerCrossbarCommands.java` (same folder), `common/entity/summon/RingmasterPatternEntity.java`, `common/entity/summon/BoundSummonBehavior.java`, `client/render/world/PuppeteerThreadRenderer.java`
- Tests: `common/entity/summon/puppeteer/RingmasterConductorRulesTest.java`, a GameTest for the act with one Crossbar and two bodies
- Docs: `docs/CIRCUS.md` (the Pattern's description and the "Calls made without the user" line), `docs/HEMOMANCY_REFERENCE.md` section 17, `wiki/Crimson-Troupe.md`
- Depends on: T16 if the act is "every body performs its act together". NQ-15-9.

### T18. Trade box theme and crest (size S)

- Rulings: HN-9
- Change: a `CIRCUS` value in `DialogueTheme` with its palette and background in `DialogueThemeStyle` (striped canvas, thread, bone, crimson, no brass). Circus crest sprites, so `DialogueScreen.crest()` stops falling back. `DialogueThemeStyle.crest` looks a crest up by the speaker's style id, so either each of the nine Circus speakers gets a sprite or the nine ids map to one troupe crest. One troupe crest is the smaller job and is what I would build.
- Files: `common/entity/npc/dialogue/DialogueTheme.java`, `client/screen/dialogue/DialogueThemeStyle.java`, `client/screen/dialogue/DialogueScreen.java`, `common/entity/npc/circus/CircusRingmasterEntity.java`, `common/entity/npc/dialogue/CircusSchoolDialogue.java`, new sprites under `assets/hemomancy/textures/gui/sprites/dialogue/crests/`
- Tests: `DialogueTree` writes the theme as an ordinal and reads it with `fromOrdinal`, so add the value at the end of the enum. `ScreenChromeGuardrailTest` if the background is a vein field. Proof is `npcTradeReviewClient`.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (line 160 lists the missing crest), `docs/CIRCUS.md`
- Depends on: nothing. Chunk 16's PR-11 is the owner's trade-box session and should come after this.

### T19. Docs roll-up and probe promotion (size S)

- Rulings: D-14, D-18, D-40, D-41, D-46, RV-15, RV-16, RV-17, and every BUILD row as it lands
- Change: move D-29 to D-46 and PT-57 to PT-63 from "Open questions" to "Rulings that bind" in `docs/CIRCUS.md` and in the roll-up. Strike the mini-bosses, the snake creature and the staff form from the open list with the date. Record the three RV overrides listed above. Fix the stale lines: "Review client: none Circus-specific", the private-geometry text, and the status line. Every probe that a task closes becomes `required` in that task. This task checks none is left optional.
- Files: `docs/CIRCUS.md`, `docs/OPEN_QUESTIONS.md`, `docs/PLAYTHROUGH_TESTS.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/LORE_REFERENCE.md`, `wiki/Crimson-Troupe.md`, `wiki/World-Content.md`
- Tests: `python gradle/source-text-impact-map.py` if any source-text test changed
- Docs: as listed
- Depends on: the tasks above

## Conflicts with recent work

None.

No file under the Circus packages, the Circus tests, `tools/circus/` or the shared files this plan touches (`BoundSummonBehavior`, `MarionetteCrossbarItem`, `BloodNeedleEntity`, `DialogueScreen`, `BossTelegraphs`) has changed since 2026-11-11 14:00. The 2026-11-13 sound audit is new and helps: it already names Circus sound ids, and T14 uses them.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-15-1 | D-43 | Which set pieces do the big top and each camp get? | A: the list below this table. B: a shorter list (king pole, ring curb, galleries, one wagon type, one apparatus per camp). | A, because the structure bar is modelled multiblocks, custom blocks and detailed walls, and the pavilion is the Circus's main site. |
| NQ-15-2 | D-43 | How rare are pavilions (48/16), camps (24/8) and stages (32/10)? | A: keep the three spacings, give sites their own ground and a sampled gate, measure in the validation run, then tune. B: tighten now to 36/12 for pavilions and 16/6 for camps. | A, because the one natural search found the nearest pavilion 33,752 blocks away, and that came from the all-column flat gate, not from the spacing. Target after measuring: a pavilion within about 1,500 blocks and each camp kind within about 1,200 on the three seeds. |
| NQ-15-3 | D-18, D-33 | "Strike until the Circus pass": is this pass that pass? | A: mini-bosses, snake and staff form stay struck through this pass, and acts 2 to 4 stay timers. B: build the mini-boss ladder at the end of this pass, replacing acts 2 to 4. | A, because the pass is already 19 tasks. The ladder needs the T13 controller and the T9 camps, so it is cheap to add afterwards. |
| NQ-15-4 | D-33 | What does the Ringmaster fight with once he is on the kit? | A: the five mental moves from the audit (Spotlight, Sleight, the Cue, Knife Board, Strings Cut). B: the plumbing only, with his knives and staff blow kept. | A, built as T15 after T13 lands, because the audit's finding was that the finale has nothing of the Circus's mental palette. |
| NQ-15-5 | D-30 | Which report counts, and is the gate on the pact or the finale? | A: any one of the three reports, checked at the pact and again at the finale. B: route-specific, The Wrong Audience for Succession and The Missing Understudy for Liberation. | A, because the answer says "one school report". B is the stronger tie to the school if you want it. An Empty Ring is the easy one and makes A a light gate. |
| NQ-15-6 | D-30, D-31 | Does the D4 gate apply to an Unstained player who wants Liberation? | A: yes, D4 for everyone. B: an Unstained Liberator needs The Missing Understudy report and no Degree. | B, because D4 is the Pattern's Degree and Liberation does not grant the Pattern. A player who turned Unstained before D4 could never use the one route D-31 leaves them. I did not trace what Degree a cured player keeps. |
| NQ-15-7 | D-39 | What is the Juggler's new role? | A: pin. Its third dagger in a volley roots the target for about 1.5 s in place of blood loss, which sets up the other bodies. B: interceptor. It knocks arrows and thrown things out of the air round the owner. C: decoy. It draws a false wind-up that enemies swing at. | A, because it keeps the Dagger Thrower's precision, leaves the practical (three hits inside 15 ticks) as it is, and gives the D3 pair a volley body and a control body. |
| NQ-15-8 | D-37 | Which act does each body get, and how is it fired? | Acts A: the audit's list: Mummer Spotlight, Vulture Trapeze (owner and Vulture swap), Juggler Feint, Bellows Smoke, Mnemonist Encore, Hound Scent, Hulk Lift. Acts B: your own list. Input A: an Act entry on the Crossbar radial. Input B: sneak-use of the Crossbar. | Acts A and input A. The radial already exists, so no second command surface appears. Thread cost and cooldown per act are set in the step plan. |
| NQ-15-9 | D-38 | What does the Pattern's group act do, does the Pattern still count as a body, and does it stay in the transposition ring? | A: "Ring": every body within 32 blocks performs its act together, once per cooldown. The Pattern stays a callable body that does not count against the cap and leaves the transposition ring. B: all bodies' next attacks land on one beat. The Pattern stays as built in the ring. | A, because it makes the Succession prize matter with one Crossbar. It needs T16 first. |
| NQ-15-10 | D-44, D-36 | What do the NPCs give, what does "new tenancy" show, and what do surviving performers do in a ruin? | Gifts A: each professor gives two Enthralling Filaments and one frame material for their body on a passed practical. Gifts B: a welcome gift on first talk. Tenancy A: the successor's banner on the perch and the Ringmaster's line keyed to the site's `completionOwner`. Survivors A: the four finale performers leave the ruin, faculty stay and teach. Survivors B: all stay as built. | Gifts A, tenancy A, survivors A. A gift on a passed practical ties the school to a reward, and performers juggling in a ruin undo the ruin. |
| NQ-15-11 | D-36 | After he yields, does the Ringmaster keep his perch and his trade? | A: yes. He returns to the perch and stays the trade speaker and conductor teacher. B: he leaves and the site becomes vacant. | A, because his trade cards and the conductor lesson have no other home. |
| NQ-15-12 | FX-27 | The Hematic Lantern reads amber, not crimson. Recolour it? | A: leave the block. The rebuilt big top gets its own crimson footlights. B: recolour the Hematic Lantern everywhere. | A, because the lantern is used across Harbinger sites and a recolour belongs to the visuals chunk (09), not to this pass. |

Set-piece proposal for NQ-15-1. All in hematic iron, bone, lacquer, canvas and thread. No brass, copper or gold.

- Big top: a king pole (modelled multiblock, iron-banded, bone finial) with quarter poles. A ring curb round a sawdust ring. Seating tiers. A lower and an upper gallery with real stairs. The Ringmaster's box on the upper gallery. A hub plinth under the Impaled Procession. Two rigging towers with trapeze platforms and a net. A scalloped valance and guyed stakes outside. Laced canvas walls with a wainscot, no flat run larger than 3 by 3.
- Fixtures: hanging Crossbar rigs, a Spindle rack, a mask wall with facing masks, bone-and-thread trophies, a pelt drape, the Ringmaster's podium, crimson footlights.
- Backyard: three painted wagons (body and wheel blocks as a multiblock), a cookhouse lean-to, a dressing tent with costume racks and trunks that do not open.
- Midway: a ticket booth and a banner line.
- Records: the four evidence records become four distinct objects (a stage bill, a loft rigging log, a quarters ledger, a carousel maker's plate).
- Camps, each one wagon, a canvas lean-to and a practice ring, plus: air, a rigging tower and net. Blades, a knife wheel and target board. Pageantry, a stilt rack and a weight frame. Embers, a fire pit, a bellows stand and a hound hoop. Memory, a prompt-book table and a costume trunk.
- Stage: the wreck of a stage wagon with the stage record.

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| PR-10 | Re-film with `runCircusReviewClient` after the footage fixes, and again after the fight and the rebuild. | Owner | T6, then T15 |
| D-43 | Approve or change the set-piece list and the rarity plan. | Owner | NQ-15-1, NQ-15-2 |
| D-18, D-33 | Say whether the mini-boss ladder, snake and staff form stay struck, and approve the moveset. | Owner | NQ-15-3, NQ-15-4 |
| D-30, D-31 | Say which report gates the pact and whether D4 binds an Unstained Liberator. | Owner | NQ-15-5, NQ-15-6 |
| D-37, D-38, D-39 | Approve the acts, the group act and the Juggler's role. | Owner | NQ-15-7, NQ-15-8, NQ-15-9 |
| D-44, D-36 | Approve the gifts, the tenancy look, the survivors' fate and the Ringmaster's place after yielding. | Owner | NQ-15-10, NQ-15-11 |
| D-36 | Pick the Ringmaster's music track and the Circus cue sounds by ear. | Owner | T14 |
| D-41 | Rebuild the Circus NPCs on the jointed rig. Take the brass and gold off the faculty costumes in `tools/circus/build_faculty.py` then, or pull that recolour into T6 if it should be gone before the re-film. | Later | a later pass |
| D-18 | Mini-bosses toward the Ringmaster, the snake creature, the staff form. | Parked | NQ-15-3 |
| PT-59 | Run the Peaceful probe once before adding the refusal, and record the result. | Build | T2 |
| D-35 | Thread Ripper's new price. | Build | chunk 04, CB-5 |
| D-33 | The Ringmaster's row in the single boss definition. | Build | chunk 07, G-2c |
| FX-26 | Every blood needle sticks in its target as a vanilla arrow, not only the knife. T6 fixes the knife. | Later | chunk 09 or the owner |
| D-29 | Live-client proof of the galleries, two clients at different acclimation, fresh-chunk placement and density. | Owner | T8, T10, T12 |
