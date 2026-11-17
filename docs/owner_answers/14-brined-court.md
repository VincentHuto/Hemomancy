# 14. The Brined Court

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P14). The PT-46 gap is closed (B26, 2026-11-15): `KindledChampionTrial.facing` searches every loaded level at any distance; `aFarChallengerWhoLogsOutStillForfeits` asserts the wait and the forfeit line. |
| T2 | Built (P14), with one shared narration line for the switch warning instead of eight lead lines. |
| T3 | Built (P14). |
| T4 | Built (P14). |
| T5 | Built (P14). |
| T6 | Built (P14). The Voyager's `brine_sealed_letter.line2` was left unchanged; the new option offers the chart. |
| T7 | Built (B26, 2026-11-15): T7a lesson wording (17 lines), T7b site names (26 directions lines), T7c nursery pointer, T7d `degree.late` (3), T7e Primal lines to NQ-14-6 A (9 `strain.primal`, the option, 4 topic lines), T7f `challenge.line1` reworded. `courtPointersPastTheGarden` is required. |
| T8 | Built (P14). |
| T9 | Built (B36, 2026-11-16): Ink Cap cap and crown scales end at Apex (`MorphlingPartSpec.until`), Holdfast Mature stone knots, Sunburst Mature crust and buds larger, Rope ribbons deeper and an ink thread under the world strands. Before and after shots taken; the owner judges them. |
| T10 | Built (P14, B26, B36): the challenge route rows, the school sync, the sail flush (T10b) and, on 2026-11-16, the three effect icons (T10a, `tools/model_export/effect_icons.py`). |
| T11 | Done (P14 and the 2026-11-14 docs pass). D-22 is recorded; the client toggle stays unless the owner objects (NQ-14-1). |
| T12 | Built in part (P14, B26): the unit suite, the combined run, the playthrough suite, `court_worldgen_validation` on three seeds, one full `alphaCheck` (B26, green) and the `courtNpcReviewClient` shots. Left: the other four review clients and the tooltip, Bestiary and Liber text captures, after T9 and T10a. The owner's walk (PR-6) and tuning (PR-30) are still to do. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

The rulings turn the eleven failing Court playthrough probes into fixes, change one strain number, and keep the rest of the Court as built. None of the Court code was touched by the lash, HUD or sound work after 2026-11-11, so there are no conflicts.

The largest pieces are the Court line pass (PT-49, PT-50, PT-51, PT-54: about 60 lang keys across 14 speakers, written by chunk 16 against `docs/NPC_VOICES.md`), the strain look fixes (HN-4, needs a review client) and the three rule changes to standing and the Champion (PT-45, PT-46, PT-52).

Surprises from the code:

- HN-3 is already true. Sunburst's home rule counts water within 5 blocks below a sky-lit air block in daytime, and Sun-Fed's surplus only needs home, daytime and a full bar.
- PT-47 and PT-48: the dialogue hub already puts a generic confirm screen ("Review what this choice will do...") in front of every option that carries an event. What is missing is a warning that says what is spent. The fix is a prompt line on two options, not a new node. Read from `DialogueHubFactory`, not seen in a client.
- PT-51: the wrong verb "feed it there" is in 17 lines, not one. Twelve house speakers repeat it in their strain inquiries.
- PT-56: only `NpcTradeService` may write rapport (`NpcTradeGuardrailTest`), so the dialogue offering must pay through a new service method.
- HN-2: the set's bonus and the strain's bonus multiply. A Primal Purse at home inside the full Prismatic set will swim at x1.82.
- D-22: a client toggle that hides Morphling mutations already exists (`renderMorphlingMutationLayer`). The doc line "no client setting" is wrong.
- HK-1 is not a Court row. The `lowtide_ruin` NBTs belong to the Chamber of Will.

Counts: 33 rows. BUILD 17, KEEP 9, DONE 1, MOOT 0, CONFLICT 0, UNCLEAR 1, OWNER 2, PARKED 3.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| D-12 | Elder challenges and fuller Court trading stay parked. The Champion title gives recognition only. | PARKED | `CourtLife.grantChampion` / `revokeChampion`: `isChampion` is read nowhere outside `CourtLife`. Each of the eight `data/hemomancy/npc_trade/court_*/` folders holds only `custom.json`. | none |
| D-22 | The threshold chimney is not enterable. Other players' mutations are always visible. | KEEP | `CourtResidentSpawner.threshold`: a walkway only, no entry code. `HemoClientConfig.RENDER_MORPHLING_MUTATION_LAYER` (old, committed) lets a viewer hide the mutation layer on every player. See NQ-14-1. | T11 (doc line) |
| HN-1 | The Lighthouse Garden keeps raising its own tide rock. | KEEP | `tools/worldgen/brined_court/garden.py`: the start piece is the tide rock. | none |
| HN-2 | Mermaid's Purse +35% is the strain's own share. It no longer shrinks inside the Prismatic or Phantasmal Bloodlust set. | BUILD | `MermaidsPurseRules.swimBonus(stage, home, otherMultiplier)` caps the total at 1.35. `MermaidsPurseMorphlingItem` line 67 passes the set multiplier. `ArmorSetBonusHandler.updatePelagicMotion` adds the set as `ADD_MULTIPLIED_TOTAL`. | T8 |
| HN-3 | Sun-Fed's surplus counts underwater within 5 blocks of a lit surface. | DONE | `MorphlingHomeConditions.sunburst` (`SUNLIT_WATER_DEPTH = 5`, water column above the eyes ending in sky-lit air, daytime). `SunburstMorphlingItem.sunFed` runs only at home and asks `level.isDay()`. No test pins the underwater case. | T8 (test and doc only) |
| HN-4 | Fix the strain looks: Ink Cap's cap above its Primal hood, Holdfast and Sunburst small at Mature, Rope strands faint in daylight. | BUILD | `docs/archive/tracks/brined_court/03-strain-visuals.md` "Left for later" still lists all three. Models in `client/model/entity/morphling/`, generator `tools/model_export/morphling_strains.py`, strands in `MorphlingStrainWorldRenderer`. Not seen in a client. | T9 |
| HN-5 | "Answer in a lore round" is not an answer. Four lore questions stay open. | UNCLEAR | Spec "Still open" 3, 4, 5, 8 in `docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`. | NQ-14-2 to NQ-14-5 |
| HN-6 | Still deferred: elders challenging a title-holder, the Tier 3 pole conflict, Sleeper waking, house greetings, Court lines in a house voice. | PARKED | None of these exist in code. `CourtRole.SLEEPER` offers nothing. | none |
| HN-7 | Derive Court rapport from standing, after alpha. | PARKED | Two ladders today: `CourtProgress` standing and `NpcTradeLedger` rapport. T3 keeps both until then. | none |
| HN-8 | The Mnemonist does not hint at the Court. | KEEP | No `hemomancy.mnemonist.*` lang line names the Court, the sea or brine. | none |
| PT-45 | Standing does not depend on the order of acts. A custom and a visit make a friend either way. | BUILD | `CourtStandingRules.afterAct`: VISIT only lifts a stranger. A custom first gives GUEST, the visit then adds nothing, and each act counts once (`CourtLife.recordAct`). Probe `customBeforeVisitLocksGuest`. | T1 |
| PT-46 | Escaping a usurpation costs the same one-day wait as dying. | BUILD | `KindledChampionEntity.tick` ends with `Outcome.GONE` and a null player after `TARGET_GRACE` 100 ticks. `end()` records the wait only for `CHALLENGER_DIED`. Probe `usurpationEscapedByLogout`. | T1 |
| PT-47 | A confirm step before the free switch is spent. | BUILD | `CourtMemberDialogue.build`: the join option carries `JOIN` for both a first entry and a switch. `DialogueHubFactory.decorate` wraps it in a generic prompt node that says nothing about the switch. | T2 |
| PT-48 | A confirm step before the Champion steps out. | BUILD | Same hub wrap. The Head-tender's "Are you ready?" (`challenge.line2`) is on the reply node, shown after `KindledChampionTrial.challenge` has already called the Champion. Probe `challengeStartsWithoutConfirm`. | T2 |
| PT-49 | A pointer line to the Shared Wake's nursery. | BUILD | No directions line names the nursery. Only the Nursery tender (`CourtRole.NURSERY_TENDER`, at the nursery) offers JOIN. `CourtSchools.launch` builds schools of Edge-swimmers only. | T7 |
| PT-50 | Residents name the places: the Vent Sanctum and the outposts. | BUILD | The 28 `hemomancy.court.<role>.directions.line1/2` keys name houses and depths, never a site. "Vent Sanctum" appears only in `head_tender.greet.stranger`. Probe `courtPointersPastTheGarden`. | T7 |
| PT-51 | Reword the Lightkeeper's feeding line. | BUILD | `CourtLife.lessonMultiplier` applies x1.5 to worn bonding credit inside the strain's own house site. "Feed it there" is in 3 Lightkeeper lines, the system line `hemomancy.court.lesson.taught` and 13 house inquiry lines. | T7 |
| PT-52 | A D5 gate on the Champion challenge. | BUILD | `CourtChampionRules.challenge` has no degree input. `CourtLife.championGate` passes none. Probe `championHasNoDegreeGate`. | T1 |
| PT-53 | With Pelagic worldgen off, hide the Court pointers. | BUILD | `HemoCommonConfig.enablePelagicWorldgen()` is read by worldgen only. `CourtDiscoveryDialogue.withVicar`, `withVoyager`, `withAlchemist` and `CourtDiscoveryProgress.onLivingDrops` do not read it. | T5 |
| PT-54 | Add Court lines past D5 and about Primal. | BUILD | `CourtMemberDialogue.View` holds no degree beyond `teachable` (D5) and no strain stage. What the lines say needs NQ-14-6. | T7 |
| PT-55 | Court members cannot be leashed. | BUILD | `CourtMemberEntity` has no `canBeLeashed` override (`BloodConstructEntity` has one). `CourtHospitality.gift` gives the Lamp-colony's two Leads. Probe `heldLeadLeashesACourtMember`. | T4 |
| PT-56 | The custom pays the same rapport by trade card and by dialogue. | BUILD | `NpcTradeService` line 231 adds `offer.rapport()` (default 2 for a custom, `NpcTradeLoader`). `CourtHospitality.offer` adds none. Probe `tradeCardPaysRapportDialogueDoesNot`. | T3 |
| RV-7 | Keep the Court's built numbers. | KEEP | `CourtHospitality.REST_HEAL` 8, `CourtMemberEntity.REST_COOLDOWN` 1200 ticks, `feed` only when hungry, `CourtSchoolRules.REGION` 1024, `CourtPresences.HEAD_TENDER_RANGE` 12, Champion 220 health and armour 6, Warden 160, `MorphlingPolypEntity.OCEAN_SPAWN_ODDS` 40. | none |
| RV-41 | Keep school followers folding into a line along a shelf. | KEEP | `CourtSchoolGoal` line 115. | none |
| RV-78 | Keep Midnight's cold split, the nursery rate, the flooded Cleft and the Bell with air. | KEEP | `CourtSite`: `WAKE_NURSERY` rarity 6, `SCREENED_CLEFT` warm surface, `STILL_BELL` cold surface. The -0.15 constant and the flooding were not found by search and were not checked. | none |
| RV-79 | Keep: a wrong drops only Giving Light standing, amends restore guest, the Champion gate needs Kindling friend. | KEEP | `CourtStandingRules.afterTransgression` / `afterAmends`, `CourtChampionRules.challenge`. T1 leaves all three alone. | none |
| FX-19 | Fix the three Court minors together. | BUILD | Icons: `stung.png`, `pinned.png` and `morphling_sealed.png` are byte copies of `morphling_kinbud.png`, `morphling_holdfast.png` and `morphling_ink_cap.png`. Fingerprints: 0 `court_member_challenge` rows in `route-fingerprints.txt`. Sync: `CourtMemberEntity` syncs only TALK, WITHDRAWN and WARY. | T10 |
| PR-6 | One owner session: fresh-world walk, two-client strand sync, full `alphaCheck` with `court_worldgen_validation`. | OWNER | `gradle/game-tests.gradle`: `court_worldgen_validation` is not in `alphaCheck` (`alpha: false`). A build session can run the automated half first. | T12 |
| PR-30 | Tune the first-pass numbers in play, after PR-6. | OWNER | `MorphlingBalanceBands` and the `*Rules` classes hold the numbers. | none |
| CD-1 | The Brine-Sealed Letter's inquiry offers a chart option again. | BUILD | `CourtDiscoveryDialogue.withLetterInquiry`: unknown offers "Who was this letter meant for?" (the Voyager then draws the chart). Known adds a line and no option. `voyager.item_inquiry.brine_sealed_letter.line2` lost its offer in voice pass 3. | T6 |
| CD-4 | Cut "In time" from the Holdfast inquiry line. | BUILD | `hemomancy.court.braced_elder.item_inquiry.morphling_holdfast.line1` still reads "In time it will hold you to stone". | T6 |
| CD-13 | The Wayfarer keeps naming "the Court" at D5+. | KEEP | `HarbingerVotaryWayfarerDialogueTrees.forDegree`: `senior.line2` at D5 and above. | none |
| HK-1 | Keep the `lowtide_ruin/*` NBTs as a regeneration source. | KEEP | Eight files under `src/main/resources/data/hemomancy/structure/lowtide_ruin/`, referenced by nothing. The Chamber of Will draws OBJ models of the same names. Not a Court row. | none |

## Plan

Run one Gradle at a time. Lines marked "voice pass" are written by chunk 16 against the speaker's sheet in `docs/NPC_VOICES.md`. Every new or changed line also gets its entry in `src/main/resources/assets/hemomancy/dialogue_briefs/court.json`.

### T1. Standing and Champion rules (size M)

- Rulings: PT-45, PT-46, PT-52
- Change:
  - PT-45. In `CourtStandingRules.afterAct`, the first visit-kind act (the site visit or the welcome gift) raises a guest to friend when that guest step came from the house's custom. The custom keeps raising one step. Result: custom plus visit makes a friend in either order, and two visit-kind acts alone still stop at guest. The act kind is not stored, so `CourtLife.recordAct` works it out from the counted act ids (`visit`, `welcome_gift`, `custom_*`). `afterTransgression`, `afterAmends` and `afterLeaving` do not change (RV-79).
  - PT-46. `Outcome.GONE` starts the same wait as a death (`CourtChampionRules.USURP_RETRY_TICKS`, 24000). `KindledChampionEntity.tick` passes the challenger to `end(GONE, player)` when they are online in any dimension. A new logout handler in `KindledChampionTrial` (beside `onDeath`) ends the fight and records the wait while the player still exists. New system line `hemomancy.court.champion.forfeit`. A dropped connection or a crash costs the day too. That follows from the ruling.
  - PT-52. `CourtChampionRules` gets `MINIMUM_DEGREE = 5` and `Gate.REFUSED_DEGREE`, checked after "already Champion" and before the wait. `CourtLife.championGate` reads it through `ProgressionGates.hasDegree`. New lines: `hemomancy.court.champion.refused_degree` (system) and `hemomancy.court.head_tender.challenge.refused_degree` (Head-tender, voice pass).
  - Voice pass: `hemomancy.court.head_tender.challenge.refused_wait` says "You asked and fell". Reword so it also fits a challenger who left.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/court/CourtStandingRules.java`, `CourtLife.java`, `CourtChampionRules.java`; `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/court/KindledChampionEntity.java`, `KindledChampionRules.java`, `KindledChampionTrial.java`; `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `CourtLifeRulesTest` (order cases for standing, the degree gate and its key). `CourtFollowerGameTests.theUsurpationRespectsItsGate` (a D4 is refused). In `PlaythroughBrinedCourtGameTests`, make `customBeforeVisitLocksGuest`, `usurpationEscapedByLogout` and `championHasNoDegreeGate` required and rename them for what now holds. `CourtMemberDialogueTest` gets the `challenge_refused_degree` node.
- Docs: `docs/BRINED_COURT.md` (Rulings that bind, Court life paragraph), `docs/HEMOMANCY_REFERENCE.md` §29, `docs/PLAYTHROUGH_TESTS.md` (D-45, D-46, D-52), `docs/OPEN_QUESTIONS.md` roll-up, `wiki/Brined-Court.md`
- Depends on: chunk 16 for two Head-tender lines

### T2. Confirm prompts for the switch and the challenge (size S)

- Rulings: PT-47, PT-48
- Change: use the hub's own confirm node. Give the option a prompt with `DialogueOptionPresentation.prompt(key)`, which `DialogueHubFactory.decorate` shows before the event can fire.
  - PT-47. When the join preview is `SWITCHED`, the join option's prompt is `hemomancy.court.<role>.join.switch`: the lead says the custom allows one move and this is it. Eight new lines, voice pass: lightkeeper, braced_elder, nursery_tender, lamp_colony, veiled_resident, bellwarden, rooted_elder, head_tender. A first entry keeps the generic prompt. If eight lines are too many, one shared narration key does the same job.
  - PT-48. The challenge option's prompt is the existing `hemomancy.court.head_tender.challenge.line2` ("...Are you ready?"), reworded in the voice pass to name leaving as well as falling (T1). The reply node keeps `challenge.line1` only.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CourtMemberDialogue.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `CourtMemberDialogueTest` (the prompt is set for a switch and for the challenge, absent for a first entry, and every prompt key has English). Rewrite the playthrough probe `challengeStartsWithoutConfirm` and the BC-3 note in `joinSwitchAndThirdChange` to assert the served tree: the event option sits on a hub node whose line is the warning. The old probe sends the event directly and cannot see a confirm screen.
- Docs: `docs/BRINED_COURT.md`, `docs/NPC_VOICES.md` (the new `join.switch` node on eight sheets), `docs/PLAYTHROUGH_TESTS.md` (D-47, D-48)
- Depends on: T1 (line wording), chunk 16

### T3. The dialogue offering pays rapport (size S)

- Rulings: PT-56
- Change: add one method to `NpcTradeService` that credits the rapport of the speaker's `custom` card (2 today) to the player's ledger. `CourtHospitality.offer` calls it after `acceptCustom`. The card path is unchanged. Both pay every time the custom is given, as the card does now. Rapport stays written only by `NpcTradeService`.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/trade/NpcTradeService.java`, `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/court/CourtHospitality.java`
- Tests: `NpcTradeServiceTest` (the new method), `NpcTradeGuardrailTest` stays green with no allow-list change, playthrough probe `tradeCardPaysRapportDialogueDoesNot` made required and renamed. `route-fingerprints.txt` changes for the `court_member_offer` rows (regenerate in T10).
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (the "exactly as the dialogue offering does" line becomes true), `docs/PLAYTHROUGH_TESTS.md` (D-56)
- Depends on: nothing. HN-7 replaces this after alpha.

### T4. No leashing Court members (size S)

- Rulings: PT-55
- Change: `CourtMemberEntity.canBeLeashed()` returns false, as for villagers. The Bloom Warden and the Kindled Champion extend it and are covered. The Lamp-colony's gift stays two Leads.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/court/CourtMemberEntity.java`
- Tests: playthrough probe `heldLeadLeashesACourtMember` made required. Drop the two "ARRANGE: an empty hand (BC-12)" workarounds in `innerHouseSweep`.
- Docs: `docs/BRINED_COURT.md`, `docs/PLAYTHROUGH_TESTS.md` (D-55)
- Depends on: nothing

### T5. Hide Court pointers when Pelagic worldgen is off (size S)

- Rulings: PT-53
- Change: `CourtDiscoveryDialogue.View` gains a "Court sites generate" flag read from `HemoCommonConfig.enablePelagicWorldgen()`. When it is false:
  - `withVicar` adds no "Who else keeps to the sea?" topic.
  - `withVoyager` adds no "Who else works these waters?" topic and no Relay Chart option.
  - `withAlchemist` adds no "Where do these strains come from?" topic.
  - `CourtDiscoveryProgress.onLivingDrops` drops no Brine-Sealed Letter. The letter's inquiry lines at the Vicar, Alchemist and Voyager all send the player to a Voyager, so the drop is a pointer too.
  - Left alone: the Wayfarer's `senior.line2` (CD-13) and the two Circus lines that mention the Court. They send nobody anywhere. Strain house lines and the Bestiary paragraph already hide until the Court is known.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CourtDiscoveryDialogue.java`, `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/court/CourtIntroductionRules.java`, `CourtDiscoveryProgress.java`
- Tests: `CourtDiscoveryDialogueTest` and `CourtIntroductionRulesTest` (flag off: no `court` node for any of the three, no letter). The BC-9 note in `courtPointersPastTheGarden` becomes an assertion on the flag that is on in the test world.
- Docs: `docs/BRINED_COURT.md` (World ruling), `docs/HEMOMANCY_REFERENCE.md` §29 "Court discovery", `wiki/Brined-Court.md`
- Depends on: nothing. Not checked: whether Voyager wrecks and their Votaries still generate with Pelagic off, and what a world made with Pelagic on then switched off should do.

### T6. Letter chart option and the Holdfast line (size S)

- Rulings: CD-1, CD-4
- Change:
  - CD-1. On the Voyager's letter inquiry node, when the Court is known and the player carries no chart, add an option that fires the existing `voyager_relay_chart` event. New option key `hemomancy.dialogue.voyager.option.letter_chart` (the existing chart option says "another", which is wrong for a first chart). `hemomancy.voyager.item_inquiry.brine_sealed_letter.line2` may offer the chart again (voice pass, Voyager sheet, mind its 4 to 8% question target). The Vicar and Alchemist nodes do not change. They cannot draw charts.
  - CD-4. `hemomancy.court.braced_elder.item_inquiry.morphling_holdfast.line1` becomes "Holdfast takes the hands first, then the feet. It will hold you to stone the way it holds me to this door." The sentence still says "will hold you". Chunk 16 can add that the elder can let go if the owner wants the reading gone entirely.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CourtDiscoveryDialogue.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`, `src/main/resources/assets/hemomancy/dialogue_briefs/court.json`
- Tests: `CourtDiscoveryDialogueTest.theLetterInquiryRecordsOnlyWhileTheCourtIsUnknown` (known with no chart offers the chart, known with a chart does not). No new route: `VoyagerDialogueEvents` already handles the event.
- Docs: `docs/NPC_DIALOGUE_AND_TRADE.md` (close the two writers' notes), `docs/HEMOMANCY_REFERENCE.md` §29 letter row, `docs/OPEN_QUESTIONS.md` §1.4
- Depends on: T5 (same builder), chunk 16

### T7. Court line pass: the lesson, places, late Degrees and Primal (size L)

- Rulings: PT-49, PT-50, PT-51, PT-54
- Change:
  - PT-51. Reword every line that says a strain is "fed" in home water. The fact: after the lesson, a strain worn inside its own house's site bonds x1.5. Incubator feeding is unchanged. Keys: `hemomancy.court.lightkeeper.lesson.line1`, `.lesson.line2`, `.lesson.known`, `.item_inquiry.morphling_sunburst.line2`; the system line `hemomancy.court.lesson.taught`; and the house inquiries `bellwarden...morphling_hushbell.line2`, `edge_swimmer...morphling_mermaids_purse.line2`, `gardener...morphling_sunburst.line2`, `host...morphling_ink_cap.line2`, `lamp_colony...morphling_dead_mans_rope.line2`, `linesman...morphling_dead_mans_rope.line2`, `marrow_tender...morphling_marrowplume.line2`, `nursery_tender...morphling_mermaids_purse.line2`, `rooted_elder...morphling_marrowplume.line2`, `tidewarden...morphling_holdfast.line1` and `.line2`, `veiled_resident...morphling_ink_cap.line2`.
  - PT-50. Rule for the 28 `hemomancy.court.<role>.directions.line1/2` keys (gardener, arbor, lightkeeper, tidewarden, braced_elder, edge_swimmer, nursery_tender, linesman, lamp_colony, host, bellwarden, marrow_tender, offering_bearer, head_tender): wherever a line names a house, it also names that house's site. Sites: the Lighthouse Garden (reef), the Tide Refuge (rocky shore), the Wake nursery (open water), the Line Camp (twilight), the Screened Cleft (midnight under warm water), the Bell (midnight under cold water), the Whale Fall (carrion depths), the Vent Sanctum (the vents). The three Giving Light speakers must name the Vent Sanctum, since the Garden is where the chart ends. No code change: the node already shows two lines.
  - PT-49. The nursery pointer goes in `hemomancy.court.edge_swimmer.directions.line2`, `hemomancy.court.gardener.directions.line1` and `hemomancy.court.lightkeeper.directions.line1`: the nursery ground lies where open water shoals over the twilight slope, the school's loop does not lead to it, and the Nursery tender there is who takes people in. A line cannot carry a bearing (NQ-14-7).
  - PT-54. `CourtMemberDialogue.View` gains "sixth Degree or higher" (`ProgressionGates`) and "wears a Primal strain". New keys, first pass: `hemomancy.court.<role>.degree.late` added to the greeting at D6+ for gardener, lightkeeper and head_tender; `hemomancy.court.<role>.strain.primal` in place of `strain.own` / `strain.other` for a Primal strain, for those three and the six other house leads; a hub option `hemomancy.dialogue.court.option.primal` at the Lightkeeper and the Head-tender, shown with an Apex or Primal strain, leading to `hemomancy.court.<role>.primal.line1/2`. About 17 keys. What the Court says about the Primal step waits on NQ-14-6.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/CourtMemberDialogue.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`, `src/main/resources/assets/hemomancy/dialogue_briefs/court.json`
- Tests: `CourtMemberDialogueTest` (late and Primal views, every key has English). Playthrough `courtPointersPastTheGarden` made required and extended: a Giving Light speaker names the Vent Sanctum and the nursery, and the Gardener at D8 with a Primal strain adds lines. The BC-7 note in `lightkeeperLessonAndBonding` becomes an assertion that the lesson lines do not say "feed". `PlaythroughLateNpcUsabilityGameTests` sweeps the new nodes for lang keys and dead ends.
- Docs: `docs/NPC_VOICES.md` (new nodes on the Court sheets), `docs/BRINED_COURT.md`, `docs/PLAYTHROUGH_TESTS.md` (D-49, D-50, D-51, D-54), `wiki/Brined-Court.md`, `docs/LORE_REFERENCE.md` §6.3c once NQ-14-6 is answered
- Depends on: chunk 16 for the wording, NQ-14-6 for the Primal lines. The code half (the View fields and nodes) has to land in the same change as its lines, since `CourtMemberDialogueTest.everyLineAndOptionHasEnglish` and the playthrough lang sweeps fail on a key with no English.

### T8. Mermaid's Purse own share, and record Sun-Fed (size S)

- Rulings: HN-2, HN-3
- Change:
  - HN-2. Delete the three-argument `MermaidsPurseRules.swimBonus(stage, home, otherMultiplier)`. `MermaidsPurseMorphlingItem` applies `swimBonus(stage, home)` whatever armour is worn. The strain's own values stay 0 / 5 / 10 / 15 / 20 / 25%, x1.5 at home, never past 35%. With the set's x1.35 (a total multiplier) the two multiply: x1.82 for a Primal Purse at home, x1.62 at Apex away from home. That number goes to PR-30.
  - HN-3. No code. Add the missing test and write the rule down.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MermaidsPurseRules.java`, `MermaidsPurseMorphlingItem.java`
- Tests: `MorphlingStrainLadderRulesTest` (remove the three headroom asserts on lines 43 to 45). `MorphlingStrainMatrixGameTests`: a Purse row inside the full Prismatic set keeps its full modifier, and a Sunburst row 5 blocks under a daylit surface is home and 6 blocks is not. `ArmorSetBonusRulesTest` is unchanged.
- Docs: `docs/MORPHLING_STRAINS.md` (the Purse passive row and the "+35% is a ceiling on the total" paragraph, the Sunburst home row), `docs/BRINED_COURT.md` (move the Purse call from "Calls made without the user" to "Rulings that bind", drop both open questions), `docs/HEMOMANCY_REFERENCE.md` §16, `wiki/Brined-Court.md`
- Depends on: nothing

### T9. Strain look fixes (size M)

- Rulings: HN-4
- Change: three art fixes, no values given by the owner, so the build proposes and the owner looks.
  - Ink Cap: the cap no longer shows above the Primal hood.
  - Holdfast and Sunburst: larger at Mature, at 16 px per block like the other strains.
  - Dead Man's Rope strands: readable in full daylight.
- Files: `tools/model_export/morphling_strains.py` and the models it writes under `src/main/java/com/vincenthuto/hemomancy/client/model/entity/morphling/` (`InkCapHeadModel.java`, `HoldfastArmModel.java`, `HoldfastLegModel.java`, `SunburstBodyModel.java`), `src/main/java/com/vincenthuto/hemomancy/client/render/world/MorphlingStrainWorldRenderer.java`
- Tests: the existing strain model tests stay green. Proof is before and after shots from `morphlingStrainReviewClient` (`gradle/review-clients.gradle`), with the Accessibility screen skipped.
- Docs: `docs/BRINED_COURT.md` (drop the open question), `docs/MORPHLING_STRAINS.md` if a stage description changes
- Depends on: nothing. Not checked: how the generator sizes a stage, or whether the faint strands are a colour or a render-type matter.

### T10. Court minors (size M)

- Rulings: FX-19
- Change:
  - Icons. Draw `stung.png`, `pinned.png` and `morphling_sealed.png` as their own 18 px icons. The 2026-11-13 HUD now hides strain passive icons from the effect row while the jar shows (`MorphlingEffectClientExtensions`), so these three are what a player actually sees there.
  - Fingerprints. Add `CourtMemberDialogue.CHALLENGE` to the Head-tender's row in `DialogueRouteCharacterisationGameTests` and regenerate `route-fingerprints.txt`. Do this after T1 and T3, which change what those routes save and say.
  - School sync. `CourtMemberEntity` gets a synced "in a school" flag set by `joinSchool`. `CourtStrainLayer` adds `MorphlingActiveState.SHARED_WAKE` for a schooling Edge-swimmer. Entity data, not a payload, so no protocol bump.
- Files: `src/main/resources/assets/hemomancy/textures/mob_effect/stung.png`, `pinned.png`, `morphling_sealed.png`; `src/gameTest/java/com/vincenthuto/hemomancy/gametest/DialogueRouteCharacterisationGameTests.java`; `src/gameTest/resources/dialogue/route-fingerprints.txt`; `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/court/CourtMemberEntity.java`; `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/court/CourtStrainLayer.java`
- Tests: `dialogue_route_validation` green with the new rows. `CourtNpcGameTests`: a launched school's members carry the flag and a lone swimmer does not. A shot from `courtNpcReviewClient` for the flush.
- Docs: `docs/BRINED_COURT.md` "Unfinished small pieces"
- Depends on: T1, T3

### T11. Close the docs (size S)

- Rulings: every row above, plus D-22, HN-1, HN-8, RV-7, RV-41, RV-78, RV-79, CD-13
- Change: in `docs/BRINED_COURT.md`, move each answered open question and each "call made without the user" that is now ruled into "Rulings that bind", dated 2026-11-13. Correct "no client setting" for mutations (NQ-14-1). Do the same in the `docs/OPEN_QUESTIONS.md` roll-up for PT-45 to PT-56, D-12, D-22 and the §1.4 letter note. HK-1 is recorded with the Chamber of Will, not here.
- Files: `docs/BRINED_COURT.md`, `docs/OPEN_QUESTIONS.md`, `docs/PLAYTHROUGH_TESTS.md`, `docs/MORPHLING_STRAINS.md`
- Tests: none
- Docs: as listed
- Depends on: T1 to T10

### T12. Proof run before the owner session (size M)

- Rulings: PR-6, PR-30
- Change: no code. What a build session can run so the owner's session starts from green:
  - `./gradlew.bat test`, then `./gradlew.bat alphaCheck` (it includes `court_discovery_validation`, `court_life_validation`, `court_npc_validation` and `court_presence_validation`).
  - `court_worldgen_validation` through `runCourtValidationServer` with `-PcourtSeed=42`, `1337` and `8675309`. It is outside `alphaCheck` and has not run since Tracks 9 and 10.
  - The playthrough suite (`playthroughGameTestServer`, band `brined_court`) and `dialogue_route_validation`.
  - Shots from `courtNpcReviewClient`, `courtPresenceReviewClient`, `brinedSeatReviewClient`, `brinedOutpostReviewClient` and `morphlingStrainReviewClient`, including the tooltip, Bestiary and Liber text that was never captured.
- Files: `gradle/game-tests.gradle`, `gradle/review-clients.gradle` (read only)
- Tests: the runs above
- Docs: `docs/BRINED_COURT.md` "Proof owed and known gaps", `docs/TESTING.md` if a command changed
- Depends on: T1 to T10. The fresh-world walk, the two-client strand, pose and school check, and the tuning stay with the owner.

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-14-1 | D-22 | You ruled mutations "always visible", but the old client toggle `renderMorphlingMutationLayer` lets a viewer hide the mutation layer on every player, themselves included. Keep it? | A: keep the toggle and fix the doc line. B: remove the toggle. | A. It is a render option the viewer sets for their own screen, and nobody can hide their mutations from others. |
| NQ-14-2 | HN-5 | When and why does a Marimo follower take true petrification? | A: a chosen, witnessed step at the end of a long anchoring. B: it comes on its own to the long-anchored. C: leave it unsaid. | A. It keeps "every one of them can still walk away" true up to the last step. |
| NQ-14-3 | HN-5 | What becomes of Last Marrow's rooted elders when a fall is exhausted? | A: they uproot and move with the house. B: they stay and feed the ones after. C: each elder chooses. | C. The Rooted elder's line ("it will feed the ones after you") and the Bestiary ("they can still uproot") are both already in the game. |
| NQ-14-4 | HN-5 | What are the Court's customs for passage and disputes? | A: write them from what is built: a house's custom buys passage through its water, and the wronged house judges, as the Giving Light does with its Warden and amends. B: leave both unwritten until the Tier 3 work. | A. It adds lore without a new system. |
| NQ-14-5 | HN-5 | How does a house take a migrant, and must they take its strain? | A: as built: a guest is asked in, keeps any strain, may take the house's strain for its lesson, and the house left keeps them as a guest. B: a migrant must take the new house's strain. | A. The code needs no strain to join and drops the old house to guest. |
| NQ-14-6 | PT-54 | What do Court speakers say about the Primal step? Six Court roles are set at the Primal stage in `CourtRole`, from a lifetime of cultivation. A player reaches Primal only with Morphic Nectar after Apotheos (D8). | A: the Court says a lifetime in home water brings it for them, and they know the land road (the Nectar, after the eighth Degree) only by report. B: they react to a Primal strain but never say how it is reached. | A. It points the player at the real step without making the Court keepers of the Nectar. |
| NQ-14-7 | PT-49 | The nursery is rare (one valid cell in 6) and a spoken line cannot give a bearing. Is a described place enough? | A: the spoken pointer only, judged in the PR-6 walk. B: also let an Edge-swimmer or the Voyager set the Relay Chart to the nearest nursery. | A. You chose "residents name places" over chart modes in PT-50. Revisit if the walk cannot find one. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| PT-45, PT-46, PT-52 | Build the rule changes and flip three probes. | Build | T1 |
| PT-47, PT-48 | Prompt lines on the join and challenge options. | Build | T2, chunk 16 |
| PT-56 | Rapport from the dialogue offering. | Build | T3 |
| PT-55 | No leashing. | Build | T4 |
| PT-53 | Hide pointers with Pelagic off. | Build | T5 |
| CD-1, CD-4 | Letter chart option and the Holdfast line. | Build | T6, chunk 16 |
| PT-49, PT-50, PT-51 | About 45 reworded lines across 14 speakers. | Build | T7, chunk 16 |
| PT-54 | Late and Primal lines. | Build | T7, NQ-14-6, chunk 16 |
| HN-2, HN-3 | Purse own share. Sun-Fed test and doc. | Build | T8 |
| HN-4 | Strain look fixes, then the owner looks at the shots. | Build | T9 |
| FX-19 | Icons, the challenge fingerprint, school sync. | Build | T10 |
| HN-5 | Four lore answers. | Owner | NQ-14-2 to NQ-14-5 |
| D-22 | Decide the mutation toggle. | Owner | NQ-14-1 |
| PR-6 | Fresh-world walk, two-client check of strands, Court poses and the school. | Owner | T12 run green |
| PR-30 | Tune first-pass numbers in the bands, including the x1.82 Purse total inside the Prismatic set. | Owner | PR-6 |
| HN-7 | Derive Court rapport from standing. Replaces T3's payment. | Later | after alpha |
| D-12 | Elder challenges, fuller Court trading. | Parked | owner |
| HN-6 | Tier 3 pole conflict, Sleeper waking, house greetings, house-voice Court lines. | Parked | owner |
| HK-1 | Record the keep with the Chamber of Will rows. | Later | the chunk that owns housekeeping |
