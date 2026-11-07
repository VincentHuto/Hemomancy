# Circus audit 05: tests, architecture compliance and docs drift (2026-11-06)

Read-only audit of how well the Crimson Troupe (the Circus) is proven by tests, how closely its code follows the
AGENTS.md rules, and where its docs disagree with the code. Nothing was built, run or edited; evidence comes from
reading both sides with grep/sed and two small read-only scripts (an NBT size reader for the templates). Line numbers
are against HEAD `40a1b3658` with a clean working tree.

**Summary.** 33 findings: 4 High, 15 Medium, 14 Low; 3 need the user's call, and one more asks about gate cost. The Circus has one dedicated GameTest
batch (16 tests) where the Court has five and the Saints have five, and its strongest proofs (both real finales on
the real pavilion, discovery on the real template, death and relog, two pupils at one professor, and the
AGENTS.md-pinned "living, non-spectator" rule) live only in the opt-in `playthrough_validation` and
`clinical_validation` suites, which `alphaCheck` never runs. Ten JVM tests pin a 33x33 Java pavilion table that no
shipped template uses. The architecture is mostly clean (no client leaks, no `Component.literal`, no English-literal
allow-list rows, blood through `BloodAccounting`, practical sessions cleared through `TransientServerStateEvents`),
but Circus perception sync sidesteps `PlayerStateSync` with a once-a-second packet to every player on the server, the
live act state sits in player root keys, unique rewards skip `PendingDelivery`, and the Waybill searches for
structures on the server thread. The docs describe two things that do not exist (private gallery geometry and the
`circusDistortion` option) and misdescribe Liberation: the captives are cut by a plain right-click, and Thread Ripper
is the prize, not the tool.

Conventions: **Severity** High = a contributor would be misled into wrong work, or a rule is broken in a way a player
meets; Medium; Low. **Confidence** Verified = both sides read; Suspected = one side inferred. **Action** fix test / fix
code / fix doc / decide. Paths: `J/` = `src/main/java/com/vincenthuto/hemomancy/`, `R/` = `src/main/resources/`,
`GT/` = `src/gameTest/java/com/vincenthuto/hemomancy/gametest/`, `UT/` = `src/test/java/com/vincenthuto/hemomancy/`,
`HR` = `docs/HEMOMANCY_REFERENCE.md`, `LR` = `docs/LORE_REFERENCE.md`. Sibling reports in this folder are cited by
their ids (Q- quests, B- bosses, P- puppetry, W- worldgen) where a finding overlaps.

---

## Test inventory

GameTests. "Gate" is whether `alphaCheck` runs the namespace (`gradle/game-tests.gradle:3-62`).

| Class (namespace) | Tests | Gate | What it proves |
| --- | ---: | --- | --- |
| `GT/CrimsonTroupeGameTests` (`crimson_troupe_validation`) | 6 | alpha | Professor dispatch needs Degree, active blood and proximity; ordeal gate and older known shapes stay separate; stage demonstrations bypass ownership, combat and finale; guest overflow and clone keep the exact payment; the Empty Ring primer needs a real active body and one inspected Crossbar; new-body save and ally filtering use the shared lifecycle. |
| `GT/CrimsonTroupePracticalGameTests` (`crimson_troupe_validation`) | 8 | alpha | Each of the seven practical rules with no-AI bodies and scripted hits (aerial hit plus matching recall, three owned hits on one threat, five seconds of Guard plus a guard strike, two diverted threats, a Cur rupture, two threats in one breath, the pupil's own attack replayed), and the Juggler volley matching the old Blood Shot budget. Rules, not AI behaviour. |
| `GT/CrimsonTroupeStructureGameTests` (`crimson_troupe_validation`) | 1 | alpha | The real 65x14x65 pavilion and five 25x9x25 camps load, every actor id resolves, nine faculty roles are in the pavilion, each camp has a Threadkeeper, roof peaks, entrance, Spindle and crafting table, and the seven roles are covered across the camps. |
| `GT/../common/worldgen/structure/circus/TroupeFootprintGameTests` (`crimson_troupe_validation`) | 1 | alpha | The shared noise column keeps the dry-ground and height gates. |
| `GT/CircusRingmasterGameTests` (`hemomancy`) | 11 | alpha | Carousel spawns three bound captives and syncs horse slots; the perched Ringmaster cannot be moved or harmed; his idle position ignores acclimation; a failed or expired act releases the site and awards nothing; a neutral player can begin the attention act; route choice needs the attention act; controlled-performer ownership survives entity reload; pavilion ownership and outcome survive a SavedData round trip; a missing owner expires after the grace. |
| `GT/journey/JourneyAutomationGameTests` circus rows (`hemomancy`) | 3 | alpha | The journey runner starts, restores known summons, and completes Succession and Liberation at D4 on a fixture pavilion. It drives `handleChoice`, `hurt` and `captive.interact` directly (`GT/journey/CircusJourneyAutomation.java:74-123`), so it proves the state machine, not play. |
| `GT/PuppeteeringGameTests` circus rows (`hemomancy`) | 3 | alpha | Juggler daggers and hover; the Ringmaster Pattern uses its own entity; a legacy Ringmaster save converts (see A-6). |
| `GT/NpcTradeGameTests` circus rows (`npc_trade_validation`) | 3 | alpha | Every Circus speaker opens its four boxes; the Ringmaster trades curiosities and keeps his quests in Quest Work; a banked spectacle is spent. |
| `GT/DialogueRouteCharacterisationGameTests` (`dialogue_route_validation`) | 3 routes | alpha | Fingerprints for `circus_school_teach`, `circus_school_inspect`, `circus_reject_pact` and the Mnemonist's waybill (see T-4). |
| `GT/RiteKindCharacterisationGameTests` (`rite_kind_validation`) | 2+ | alpha | The Puppeteer trial kind collapses with no ready Crossbar or a vanished puppet. |
| `GT/ManipulationAcquisitionGameTests` (`hemomancy`) | 1 | alpha | The Liberation memory grant keeps an unlearnable Thread Ripper as an item and never duplicates it. |
| `GT/ClinicalBloodProgressionGameTests.circusObservationRequiresLivingNonSpectator` (`clinical_validation`) | 1 | **opt-in** | Dead and spectator ticks earn no discovery or acclimation; D0 inactive-blood observation counts. |
| `GT/playthrough/PlaythroughCrimsonTroupeGameTests` (`playthrough_validation`) | 30 (7 optional) | **opt-in** | Band CT from a fresh D5: referral and Waybill, discovery and acclimation on the real template, first-talk milestones, Crossbar, Threadkeeper inspect/report/claim and cards (LF-7, LF-8), refusals (LF-11), the Mnemonist practical and ordeal, the three investigations, the attention act and route, spectator owner (LF-9), faculty out of act 2 (LF-10), both real finales, death and relog in a practical, an act and the finale, two pupils, the sweep and trades, nine pomes blocking the ordeal. |
| `GT/playthrough/PlaythroughLateNpcUsabilityGameTests`, `...Initiate...`, `...Smoke...` circus rows (`playthrough_validation`) | several | **opt-in** | Circus lang keys and hub cards from D5 to D8; D3 has no Circus pointer (D-21). |
| `GT/PelagicWorldValidation` pavilion locate (`pelagic_worldgen_validation`, `-PpelagicPavilionLocate`) | 1 | **opt-in**, seeded | A cold vanilla locate finds one eligible pavilion and records its time. |

JVM tests (`UT/`). About 60 files mention the Circus; the ones that carry Circus proof:

| Group | Files (tests) | What it proves |
| --- | --- | --- |
| Rules (pure) | `CircusProgressRulesTest` (4), `CircusRouteRulesTest` (3), `CircusFinaleRulesTest` (2), `CircusPavilionStateRulesTest` (3), `CircusCarouselRulesTest` (2), `CircusCarouselEncounterRulesTest` (3), `CircusPerformerRulesTest` (7), `CircusCurriculumTest` (2), `CircusSchoolRulesTest` (2), `ThreadRipperRulesTest` (4), `RingmasterConductorRulesTest` (2), `TroupeFootprintScanTest` (3), `CircusCombatProofTest` (2) | Thresholds 150/500/1000, 1 point per 80 ticks, route choice and repair, finale phase order, sever/break masks, performer warning and downing, Thread Ripper outcomes, the 32-block relay and +1 cap, footprint sampling. |
| Templates | `CircusPavilionPerformerTemplateTest` (10), `CircusPavilionTemplateTest` (main) | A 33x33 Java table nobody generates any more (T-1). |
| Templates | `CrimsonTroupeCampTemplateTest` (2) | Camp roofs, headroom and instruction blocks in the real NBT. |
| Resources and models | `Circus*ResourceTest` (acrobat, carousel, faculty, fire eater, knife thrower, performer, ringmaster, stilt walker), `CircusTextureUvTest`, `CircusDownedPoseTest`, `PuppeteerSummon*Test` | Assets exist, UVs fit, poses sit in range. |
| Source-text pins | `CircusPresentationRegressionTest` (11), `CircusPerformerDollSourceTest`, `CircusPerformerEquipmentSourceTest`, `CircusPerformerThreadSourceTest`, `CircusDiscoveryIntegrationSourceTest` (main), `PuppeteerIntegrationSourceTest`, `PuppetStringsSourceTest`, `CircusPavilionWorldgenResourceTest` | That certain strings appear in certain methods (T-5). |
| Legacy `main()` | `CircusPavilionPlacementRulesTest`, `CircusIntroductionRulesTest`, `CircusPavilionTemplateTest`, `CircusDiscoveryIntegrationSourceTest`, `PuppeteerSummonRulesTest`, `MnemonistPuppetRulesTest`, `BloodDrunkPuppeteer*Test`, `PuppeteersWoolResourceTest`, `PuppeteeringThreadInquiryOwnershipTest`, `PuppeteerSummonVisualResourceTest` | Run through `UT/testing/LegacyMainTestAdapterTest`; T-6. |
| Trade, dialogue, guardrails | `NpcTradeCatalogueDataTest`, `NpcTradeLoaderTest`, `NpcTradeServiceTest`, `DialogueHubFactoryTest`, `NpcTradeGuardrailTest`, `ContentBoilerplateGuardrailTest`, `EntityModelAnimationGuardrailTest`, `RetiredNamesGuardrailTest` | Circus rows in shared catalogues and guardrails; no guardrail is Circus-specific. |

Optional probes (`required = false`, all in `playthrough_validation`):

| Test (`GT/playthrough/PlaythroughCrimsonTroupeGameTests.java`) | Label | Waits on |
| --- | --- | --- |
| `waybillLocatesACamp` (`:249`) | "CT-3" (mislabelled, T-8) | A seeded world; it is a `PlaythroughNotRun` marker, not a decision. |
| `abandonedStageHasNoPointer` (`:903`) | CT-2 / PT-58 | User: a Waybill mode, an NPC direction, or exploration. |
| `ringmasterReachFromTheFloor` (`:936`) | CT-1 / PT-57 | User: galleries, stairs, a lower perch, or "players build up". |
| `practicalTargetKilledStalls` (`:1428`) | CT-8 / PT-63 | User: replace the threat, end with the retry line, or accept. |
| `practicalsInPeaceful` (`:1466`) | CT-3 / PT-59 | User: non-monster targets, a Peaceful refusal, or accept. |
| `threadkeeperLessonUnreachable` (`:1492`) | CT-5 / PT-60 | User: an option to the briefing, move it, or drop it. |
| `ringmasterLostLeavesSiteOpen` (`:1512`) | CT-7 / PT-62, logs CT-6 | User: a respawn rule, settle or release the site, or skip it in the Waybill. |

`docs/OPEN_QUESTIONS.md:157-163` gives each the default "As built; the probe stays failing".

## Coverage compared with the Court and Saints

| Dimension | Circus | Brined Court | Saints |
| --- | --- | --- | --- |
| Dedicated alpha batches | 1: `crimson_troupe_validation` (16). The 11 Ringmaster tests sit in `hemomancy` | 5: `court_discovery` (10), `court_life` (8), `court_npc` (25), `court_presence` (6 + 10 followers), `morphling_strain` (36 + 13) | 5: `saint_kit` (8), `hemorath_fight` (19), `seraphae_fight` (9), `putriciel_fight` (18), `velorum_fight` (19); mausoleum assembly, ward and sealing in `registration_validation` (6); `SaintEncounterGameTests` (5) |
| Seeded worldgen validation | None of its own; one timed pavilion locate inside the Pelagic run | `court_worldgen_validation` (`CourtWorldValidation`, `runCourtValidationServer`, JFR travel) | Four seeds by four Saints in `MausoleumAssemblyGameTests`; `-PmausoleumNatural` review |
| Review clients (`gradle/review-clients.gradle`) | None of its own; `npcTradeReviewClient` (hub), `hudLayerReviewClient` (edge at score 200) | 5: `morphlingStrain`, `brinedSeat`, `brinedOutpost`, `courtNpc`, `courtPresence` | `mausoleum`, `sarcophagus`, `ringDoor`, `bossPresentation` with four fight flags |
| Boss presentation review | Ringmaster absent from `GT/BossPresentationClientReview.java` | n/a | All four fights, five native rounds each |
| Playthrough band | CT: 30 tests, 7 optional, starts at D5 | BC: 26 tests, 9 optional | Covered inside D7/D8 bands (`PlaythroughEndgameGameTests`) |
| Characterisation fingerprints | `dialogue_route`: 3 Circus events (Ringmaster rows empty, T-4); `combat_targeting`: 0 rows | `dialogue_route`: 12 events; `combat_targeting`: 20 rows (`GT/CombatTargetingMatrixGameTests.java`) | Fight GameTests characterise every move |
| Guardrails of its own | None | `MorphlingStrainTableGuardrailTest` | Boss clock and kit guardrails |

Circus behaviours with no automated proof in the alpha gate (opt-in coverage in brackets):

| Behaviour | Proof |
| --- | --- |
| Living non-spectator observation (AGENTS.md rule) | none in alpha [clinical, playthrough] |
| Discovery and passive acclimation on a real pavilion | none in alpha [playthrough] |
| Real Succession and Liberation finales (RAFTERS, CAROUSEL, DESCENT) | journey fixtures only [playthrough] |
| Acts 2 to 4 with the real troupe | none in alpha [playthrough, partly] |
| Thread Ripper cutting a captive (`J/common/manipulation/ductilis/ThreadRipperManip.java:65-66`) | none anywhere: every test cuts by right-click |
| Puppets damaging the Ringmaster in DESCENT | none; code refuses (B-1) |
| Private geometry, `circusDistortion` | not built (D-1) |
| Perception stages on screen and in world | `HudLayerReview` captures one stage; no review of `renderWorld` |
| Two clients at different stages; bystander isolation; troupe AoE on bystanders | none (B-8) |
| Cold restart mid-act and mid-finale | SavedData and entity round trips only |
| Ringmaster Pattern relay in the world | JVM rule only (`UT/.../RingmasterConductorRulesTest.java`) |
| Puppet Interference with a real active puppet | boolean parameter only (`GT/MorphlingPowerGameTests.java:303`) |
| Camp, abandoned-stage and pavilion placement density, fit, Waybill targets | none [one timed locate] (W-6) |
| Finale presentation (bar, scene, phase cards) | none (B-15) |
| Trade | covered |

---

## Findings: tests

**T-1** · High · Verified · fix test · user's call: no
Ten JVM tests prove a pavilion that no longer ships.
- `J/common/data/gen/CircusPavilionTemplate.java:7-11` is a 33x12x33 block table. Its provider was deleted with the
  orphan `circus_pavilion.nbt` in `d5a7ddbfa` (2026-11-02 audit B-10); `J/common/data/gen/DataGeneration.java:19-26`
  runs only the blockstate and item-model providers. Nothing in `src/main` calls the table.
- `UT/common/data/gen/CircusPavilionPerformerTemplateTest.java:17-100` (10 tests: "one centered carousel", "one
  Ringmaster on the rear rafter", "curtains form one connected wall", "two captured Prism Cuttles", entrance clear) and
  `UT/common/data/gen/CircusPavilionTemplateTest.java:8-36` assert this table.
- The shipped `R/data/hemomancy/structure/crimson_troupe_pavilion.nbt` is 65x14x65 with 11,876 blocks; its only check
  is `GT/CrimsonTroupeStructureGameTests.java:23-27` (size and nine faculty). CT-1 shows the shipped Ringmaster perch
  is unreachable, which a rafter test on the real template would have caught.
- Fix: delete the table and both tests (into `removed_assets/`), and port the meaningful assertions (one carousel
  near the centre, Ringmaster perch reachable or deliberately not, entrance clear, two jarred Cuttles) to a JVM NBT
  reader or `CrimsonTroupeStructureGameTests` over the real template.

**T-2** · High · Verified · fix test · user's call: no
The gate never runs the Circus's best proofs, including the AGENTS.md-pinned rule.
- `gradle/game-tests.gradle:34` `playthrough_validation` and `:54` `clinical_validation` are `alpha: false`.
- `GT/ClinicalBloodProgressionGameTests.java:3278` `circusObservationRequiresLivingNonSpectator` is the only direct
  test of the AGENTS.md "Pavilion observation and passive acclimation require living, non-spectator players" rule;
  `GT/playthrough/PlaythroughCrimsonTroupeGameTests.java:266` repeats it on the real template. Neither is gated.
- The two real finales (`:1113`, `:1173`), death and relog (`:1242`, `:1320`, `:1367`) and two pupils (`:1282`) are
  also opt-in. The gated finale proof is the journey runner, which calls `handleChoice`, `hurt` and `interact`
  directly (`GT/journey/CircusJourneyAutomation.java:74-123`).
- The Court's discovery, life and NPC flows and every Saint fight are gated (`gradle/game-tests.gradle:35-38, 47-51`).
- Fix: add to `crimson_troupe_validation` a living/dead/spectator observation test (port the clinical one) and a
  compact Succession and Liberation finale on the "ring" fixture the playthrough already builds (`miniRing`), plus one
  act with the real troupe.

**T-3** · Medium · Verified · fix test · user's call: no
No dedicated worldgen validation, review client or combat-targeting rows; see the gap table above.
- No `*ValidationServer` run for camps, the abandoned stage or the pavilion; the Pelagic probe only times one locate
  (`GT/PelagicWorldValidation.java:35-76`). The Court has `court_worldgen_validation`
  (`gradle/game-tests.gradle:13`). Overlaps W-6.
- `gradle/review-clients.gradle` has no Circus row; `GT/BossPresentationClientReview.java` has no Ringmaster scene
  (grep), although `docs/CIRCUS.md:7-12` describes his bar, scene and phase cards as unwatched. Overlaps B-15.
- `GT/CombatTargetingMatrixGameTests.java` has 0 Circus rows (Court 20). Thread Ripper's targeting, the carousel horse
  strikes and the troupe's AoE are unpinned.
- Fix: a `circusReviewClient` row (pavilion stages 0/150/500/1000, the three finale phases, a camp), a seeded
  `circus_worldgen_validation` modelled on `CourtWorldValidation` that locates each camp variant, the stage and the
  pavilion and records density, and Thread Ripper rows in the combat matrix.

**T-4** · Medium · Verified · fix test · user's call: no
Dialogue-route fingerprints cover 3 of 14 Circus events, and the Ringmaster rows prove nothing.
- `GT/DialogueRouteCharacterisationGameTests.java:120-123` adds `circus_school_teach`, `circus_school_inspect` and
  `circus_reject_pact` (Ringmaster and Vicar). `src/gameTest/resources/dialogue/route-fingerprints.txt:85-87` records
  `circus_reject_pact @RINGMASTER [D7] delivered=false said=[]`: the fixture Ringmaster has no carousel within 24
  blocks, so `J/common/entity/npc/circus/CircusRingmasterEntity.java:254` returns before anything happens.
- Unfingerprinted: `circus_accept_pact`, `circus_repair_route`, `circus_begin_next_trial`, `circus_begin_finale`,
  `circus_school_practical`, `circus_school_guest`, `circus_school_testimony`, `circus_school_report`,
  `circus_school_claim` (ids from `CircusRingmasterEntity.java:71-75` and `J/.../dialogue/CircusSchoolDialogue.java:27-37`).
  The Court has 12 events fingerprinted. Overlaps Q-14.
- Fix: give the Ringmaster speaker a carousel in the fixture and add every Circus event id; regenerate the file.

**T-5** · Medium · Verified · fix test · user's call: no
Circus behaviour is "proven" by source-text pins outside `architecture/`, with private read helpers.
- AGENTS.md: "New source-text assertions are for architecture guardrails under `.../architecture/` only" and "do not
  add private `read`/`assertContains` copies".
- `UT/common/entity/npc/circus/CircusPresentationRegressionTest.java:7-9` (own `source()` helper; 11 tests, e.g.
  `routeChoiceWarnsBeforeCommitAndRepairDisappearsAfterUse`, which a GameTest proves); `UT/.../dialogue/CircusDiscoveryIntegrationSourceTest.java:32-38`
  (private `read` and `assertContains`); `CircusPerformerDollSourceTest`, `CircusPerformerEquipmentSourceTest`,
  `CircusCarouselResourceTest`, `CircusRingmasterResourceTest`, `PuppeteerIntegrationSourceTest` (287 lines),
  `PuppetStringsSourceTest`, `CircusPavilionWorldgenResourceTest` each carry one or two private copies.
- Fix: delete pins whose behaviour a GameTest already proves; move the rest to `SourceAssertions`/`RepoPaths`;
  regenerate `docs/architecture/evidence/source-text-impact-map.csv`.

**T-6** · Low · Verified · fix test · user's call: no
Twelve Circus and puppet tests are legacy `main()` classes (known debt: `docs/OPEN_QUESTIONS.md:280` "335 legacy
`main()` tests").
- Listed in the JVM inventory above; two of them test dead code (`CircusPavilionPlacementRulesTest`,
  `CircusPavilionTemplateTest`, see A-6 and T-1). Fix: delete those two; convert the rest to JUnit when touched.

**T-7** · Low · Verified · fix test · user's call: no
The Ringmaster's tests are not in the Circus batch.
- `GT/CircusRingmasterGameTests.java:43` `@GameTestHolder(Hemomancy.MOD_ID)`, so `runCrimsonTroupeGameTestServer`
  skips the site, act and SavedData tests. Fix: move them to `crimson_troupe_validation`.

**T-8** · Low · Verified · fix test · user's call: no
CT labels collide in the playthrough file, and the band starts after the Circus does.
- `waybillLocatesACamp` (`:248-251`) says "CT-3" and is a not-run marker; CT-3 is the Peaceful decision (`:1465`,
  `docs/PLAYTHROUGH_TESTS.md:290`). `pavilionDiscoveryAndAcclimation` says "CT-4" (`:262`) and `firstTalkMilestones`
  "CT-5" (`:342`), while CT-5 is the Threadkeeper decision (`:1491`). The header says decisions carry
  `// DECISION CT-n` (`:94-95`).
- The band starts at a fresh D5 (`:79-80`) although the Circus opens at D3 (Vulture, Juggler) and D4 (referral, the
  Mnemonist's introduction, `J/.../dialogue/CircusIntroductionRules.java:4`).
- Fix: label the non-decision tests by topic, not CT-n; add a D3/D4 entry test (D3 lesson at a camp, D4 referral).

---

## Findings: architecture

**A-1** · Medium · Verified · fix code · user's call: no
Circus perception sync bypasses `PlayerStateSync` and sends a packet every second to every player.
- `J/common/capability/PlayerStateSync.java:140-142` has `COURT_PROGRESS`; there is no `CIRCUS_PROGRESS` row although
  `CIRCUS_PROGRESS` is a player attachment (`J/common/capability/HemoAttachmentTypes.java:178-180`).
- `J/common/capability/player/harbinger/circus/CircusDiscoveryProgress.java:38-57` sends
  `PacketSyncCircusPerception` every 20 ticks to every server player, inside a pavilion or not, D0 or D8;
  `:59-67` adds its own respawn and dimension-change resends, which AGENTS.md forbids ("do not add per-system lifecycle
  resends"). `UT/architecture/PlayerStateSyncTest.java:41-62` cannot see them because the payload is not in the table.
- Six more ad-hoc `CircusPlayerProgress.sync(player, true)` calls: `CircusPerformanceController.java:123`,
  `CircusPerformerEntity.java:277`, `CircusRingmasterEntity.java:284`, `J/common/mission/circus/CircusPracticalController.java:161`,
  and the payload is built in `CircusPlayerProgress.java:100-102`. Overlaps Q-15.
- Fix: a `CIRCUS_PROGRESS` table row with a dirty flag for acclimation; send the transient "inside" flag only when it
  changes (a per-player last-sent set registered with `TransientServerStateEvents`); delete the two lifecycle
  handlers and the ad-hoc calls.

**A-2** · Medium · Verified · fix code · user's call: no
The live act lives in player root keys, and tests read those keys.
- `J/common/entity/npc/circus/CircusPerformanceController.java:29-32` (`hemomancy.circus_active_challenge`,
  `_ticks`, `_origin`, `_subject`), 20 `getPersistentData()` calls, allow-listed at
  `src/test/resources/architecture/persistent-data-allow-list.txt:17` ("player root keys and entity markers").
- `GT/journey/CircusJourneyAutomation.java:46, 67` and `GT/playthrough/PlaythroughCrimsonTroupeGameTests.java:1421`
  hard-code the private key string.
- Fix: a non-copied field in `CircusProgress` (the act is already cleared on clone, `CircusPlayerProgress.java:130-135`)
  or a session map; a public `activeChallenge(player)` reader for tests. That removes 18 of the 20 allowances (the
  spider's `CircusTrialOwner` marker stays, or becomes a `PeacockSpiderEntity` accessor, see A-12).

**A-3** · Medium · Verified · fix code · user's call: yes (guest claim)
One-time rewards skip `PendingDelivery`.
- `J/common/entity/npc/circus/CircusRingmasterEntity.java:347-348` gives the Topper with `Inventory.add` and drops it
  into the world when full; `:362-365` does the same for the Thread Ripper memory. Completion is once per player (the
  route becomes `*_COMPLETE`, `CircusPlayerProgress.java:80-84`), so both are earned one-time claims, the case the
  AGENTS.md documentation contract routes through `PendingDelivery` (actual insertion, no Creative-discard credit,
  dead-player retry).
- `J/common/mission/circus/CircusSchoolQuests.java:112-136` hand-rolls exact-overflow insertion for guest pay; it
  works, but duplicates `PendingDelivery.insert` (`J/common/capability/PendingDelivery.java:97`) and only retries when
  the player talks to a Threadkeeper. Overlaps Q-5.
- Fix: `TOPPER` and `LIBERATION_MEMORY` claims in `PendingDelivery.Claims.ALL`; whether guest pay becomes an automatic
  claim or keeps "collect from a Threadkeeper" is the user's call.

**A-4** · Medium · Verified · fix code · user's call: no
The Scarlet Waybill searches for structures on the server thread.
- `J/common/item/harbinger/CircusWaybillItem.java:29-39`: one `findNearestMapStructure` at radius 160 chunks, then up to
  eight more at radius 96 around offsets of ±1536 blocks, per use, synchronously.
- The Court moved its Relay Chart search off-thread (`J/common/worldgen/structure/court/CourtSeatSearch.java`;
  `GT/CourtDiscoveryGameTests.java:43` "its off-thread search"). Overlaps Q-3 and W-7.
- Fix: reuse the `CourtSeatSearch` pattern and record the result when it lands.

**A-5** · Medium · Verified · fix code · user's call: no
Apprenticeship state is an untyped string-keyed `CompoundTag`, and summon ids are literals.
- `J/common/capability/player/harbinger/circus/CircusApprenticeshipProgress.java:14-44` and
  `J/common/mission/circus/CircusSchoolQuests.java:32-110` write ten key families (`taught.`, `practical.`,
  `primer.crossbar`, `primer.call|command|recall|shape`, `testimony.`, `clue.`, `quest.`, `guest.day`,
  `guest.pending`); `J/common/network/circus/PacketSyncCircusSchool.java:16-20` adds `known.` and ships the raw tag.
  A typo fails silently.
- Literal summon ids although `PuppeteerSummonDefinitions` has constants: `CircusSchoolQuests.java:85-86`,
  `CircusPracticalController.java:89, 124, 148, 153`, `J/common/mission/circus/CircusCurriculum.java:12-18`, and the
  camp mapping switch in `CircusWaybillItem.java:19-25`.
- Fix: a typed apprenticeship record with a codec inside `CircusProgress` (the mod is unreleased, no migration); use
  the definition constants; put the teacher-to-camp mapping on `CircusCurriculum.Lesson`.

**A-6** · Low · Verified · fix code · user's call: no
Dead code and leftover migrations in an unreleased mod (AGENTS.md: "a change to player data needs no migration").
- `J/common/worldgen/structure/circus/CircusPavilionPlacementRules.java:3-26` has no caller in `src/main`; it is
  imported unused at `J/common/worldgen/structure/StructurePlacementChecks.java:12`, and its 5-block limit contradicts
  the real 3-block gate at `:198`.
- `J/common/data/gen/CircusPavilionTemplate.java` (T-1).
- `J/common/capability/player/harbinger/circus/CircusPlayerProgress.java:20-27, 105-127` `foldLegacyRootKeys`
  ("sunset 2027-04-23"); its only caller is `GT/journey/HemoJourneySnapshot.java:914`.
- `J/common/entity/summon/MnemonistPuppetEntity.java:90-124` `convertLegacyRingmaster`, with
  `GT/PuppeteeringGameTests.java:644`.
- `J/common/entity/npc/circus/CircusPavilionStateRules.java:20-22` `resetOutcome` returns its argument;
  `ThreadRipperRules.Outcome.SEVER_CAPTIVE` is never produced because `ThreadRipperManip.java:73` always passes
  `captive = false`.
- `J/common/data/gen/DynastyCastlePieceProvider.java:26` links to the deleted `CircusPavilionStructureProvider`.
- Fix: delete each (into `removed_assets/`) with its tests.

**A-7** · Medium · Verified · fix code · user's call: no
25 vanilla-particle allowances across 10 Circus files, several at focal moments.
- `src/test/resources/architecture/vanilla-particle-allow-list.txt:11, 37, 39, 49-54, 79`: perception overlay 3,
  Luminal Cicada 2, Peacock Spider 5, Acrobat 3, carousel 3, Fire Breather 3, Dagger Thrower 1, Ringmaster 1, Stilt
  Walker 3, demonstrations 1. All but the Stilt Walker are "pending the vanilla particle sweep (A-33)".
- Focal: the finale's completion burst `CRIMSON_SPORE` x80 (`CircusRingmasterEntity.java:352`), the carousel ruin
  `EXPLOSION` (`CircusCarouselEntity.java:292`) and the captive-cut burst (`:215`), the Fire Breather's `FLAME`
  (`CircusFireEaterEntity.java:41-65`) while the Bellows already uses Flammeus thermal flames (`HR:2866`), and
  overlay motes `CRIMSON_SPORE`/`REVERSE_PORTAL`/`END_ROD` (`J/client/screen/overlay/CircusPerceptionOverlay.java:56-66`).
  Overlaps B-9 and W-12.
- Fix: sweep to `HemoParticleData` and the toolkit; 22 allowances can go (all but the Stilt Walker's block debris).

**A-8** · Low · Verified · fix code · user's call: no
Tuning numbers are inline in the controllers, so docs cannot cite them by name.
- `CircusPerformanceController.java:115, 120` (act lengths 200/200/240/300, rewards 150/100); `CircusRingmasterEntity.java:259, 266`
  (route choice +50), `:179-180` (swarm every 100, knife every 35), `:255, 408` (`& 1` for the attention bit);
  `CircusPerformerEntity.java:276` (first talk +50); `CircusPracticalController.java:34, 63, 93, 132` (6000-tick
  expiry, 80 HP threats, 100 guard ticks, 15-tick volley); `CircusSchoolQuests.java:102-109` (4 filaments, day =
  `getDayTime() / 24000`); `CircusCarouselEntity.java:138-140` (64-block owner range, 200-tick grace).
- No config is needed (neither the Court nor the Saints have any, `J/config/HemoServerConfig.java`). Fix: one
  `CircusRules` table alongside `CircusFinaleRules` and `ThreadRipperRules`, with named challenge bits.

**A-9** · Low · Verified · fix code · user's call: no
The school's dialogue route is not a `<Npc>DialogueEvents` class.
- `J/common/entity/npc/dialogue/route/DialogueEventRouter.java:91-95` routes `circus_school_*` through an inline
  method to `J/common/entity/npc/dialogue/CircusSchoolDialogue.java:97-128`; every other NPC has a class in
  `route/` (AGENTS.md: "one `<Npc>DialogueEvents` class per NPC").
- Two message prefixes for one school: `hemomancy.circus.school.` and `message.hemomancy.circus.school.`
  (`CircusPracticalController.java:182-184`).
- Fix: a `CircusSchoolDialogueEvents` class in the table; one key prefix.

**A-10** · Medium · Verified · decide · user's call: yes
Two combat rules nobody has tested or decided.
- `CircusRingmasterEntity.java:310`: the Ringmaster takes damage only when `source.getEntity()` is a `ServerPlayer`,
  so the Puppeteering school's own finale refuses damage from the player's puppets (B-1).
- `J/common/entity/npc/circus/CircusStiltWalkerEntity.java:88-94`: the stomp hurts every `LivingEntity` within 3.5
  blocks except the troupe, including other players and their pets (B-8).
- Fix: decide both, then pin them in `combat_targeting_validation`.

**A-11** · Low · Verified · fix code · user's call: no
Degree checks bypass `ProgressionGates`.
- `CircusApprenticeshipProgress.java:29`, `CircusPracticalController.java:42`, `CircusSchoolQuests.java:19`,
  `CircusSchoolDialogue.java:108`, `CircusWaybillItem.java:15` read `getPlayerDegreeNumber` and compare it with
  `lesson.degree()`; `CircusSchoolQuests.java:25, 83` already use `ProgressionGates.hasDegree`. Not a literal compare,
  so no guardrail fires. Fix: `ProgressionGates.hasDegree(player, lesson.degree())` throughout.

**A-12** · Low · Verified · fix code · user's call: no
Allow-list entries a cleanup can remove or correct.
- `persistent-data-allow-list.txt:18` says `CircusPracticalController` uses "player root keys and entity markers"; all
  six calls are on rehearsal targets (`CircusPracticalController.java:66, 104-114, 171-174`). Fix the reason.
- `CircusRingmasterEntity.java:243-249` (3 allowances) repeats `VampireBatEntity`'s `"CircusSwarmOwner"` key
  (`J/common/entity/mob/animal/VampireBatEntity.java:79, 126, 163`); `CircusPerformanceController.java:75` repeats
  `PeacockSpiderEntity`'s `"CircusTrialOwner"` (`PeacockSpiderEntity.java:58`). An accessor on each entity removes the
  Ringmaster's three and the controller's marker.
- With A-2 and A-7: about 21 persistent-data and 22 vanilla-particle allowances can go.

Not a problem (checked): all player state reads go through `HemoCapabilityAccess` (`circusProgress(player)`,
`J/common/capability/HemoCapabilityAccess.java:72`); practical sessions are cleared through
`TransientServerStateEvents` (`J/common/event/TransientServerStateEvents.java:164, 200`); nothing in Circus `common/`
names a client class; no `Component.literal` and no Circus rows in `english-literal-allow-list.txt`; Thread Ripper's
35 blood goes through the shared manipulation cost (`J/common/init/ManipulationInit.java:752-757`); Circus has no
serverbound payload (choices arrive as routed dialogue events), both clientbound payloads use the facade and
`ClientPayloadHandlers::dispatch` (`J/common/network/PacketHandler.java:151-153`); the overlay reads
`AnimationTime.gameTime` and a static render type; the one world edit (Liberation cobwebs,
`CircusRingmasterEntity.java:370-375`) is world-authored, not a player edit; no TODO/FIXME; the largest Circus class is
462 lines. Circus quests run beside three other quest frameworks (2026-11-02 audit D-A, still open).

---

## Findings: docs

**D-1** · High · Verified · fix doc · user's call: no
Private geometry and `circusDistortion` are described as built; neither exists.
- `docs/CIRCUS.md:32` "From 500 private geometry (galleries, stairs) supports the player; at 1000 the upper gallery and
  canopy are privately solid ... `circusDistortion` disables client refraction without hiding collision".
- No collision mask, gallery or config exists (grep of `src/main` for gallery, canopy, `circusDistortion`; the config
  classes have no Circus entry). The perception code draws flat silhouettes, cloth and diamonds only
  (`CircusPerceptionOverlay.java:102-147`). `docs/PLAYTHROUGH_TESTS.md:288` already says "The documented private
  galleries do not exist". The source is `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md:122-129`.
  Overlaps W-4. `LR:297` "apparently solid manifestations" is lore and can stay.
- Fix: move both to "Proof owed and known gaps" as not built; drop `circusDistortion`.

**D-2** · High · Verified · fix doc or fix code · user's call: yes
Liberation's mechanism is misdescribed: the captives are cut by hand, and Thread Ripper is the prize.
- `docs/CIRCUS.md:34` "Liberation ... uses Thread Ripper (30 ticks, 24 blocks, 35 blood, 12 s cooldown) to sever the
  three failed initiates"; `HR:2850` "Liberation uses Thread Ripper on the three captives".
- `J/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java:134-137` cuts a captive on a plain main-hand right-click
  by the site owner; Thread Ripper's carousel branch (`ThreadRipperManip.java:65-66`) is a second way. Every test cuts
  by right-click (`GT/journey/CircusJourneyAutomation.java:106`, `GT/playthrough/PlaythroughCrimsonTroupeGameTests.java:1198`).
- Thread Ripper is granted on Liberation's completion (`CircusRingmasterEntity.java:344, 359-366`) and is Summa rank
  (`ManipulationInit.java:753-754`), learnable at D7 (`GT/ManipulationAcquisitionGameTests.java:155-159`), so a D4
  or D5 Liberator receives a memory they cannot learn. Overlaps B-7.
- Decide: gate cutting behind Thread Ripper (and say how a D4 gets it) or keep the right-click; then fix both docs.

**D-3** · Medium · Verified · fix doc · user's call: no
The challenge bits and acclimation sources are wrong or missing.
- `docs/CIRCUS.md:34` "all five challenge bits (the peacock-spider attention act, troupe-and-doll pressure,
  knife/displacement, carousel coordination)" names four. Bit 1 is the route choice
  (`CircusRingmasterEntity.java:259, 266` `completeChallenge(player, 1, 50)`); acts are bits 0, 2, 3, 4
  (`CircusPerformanceController.java:61`); `CircusRouteRules.ALL_CHALLENGES = 0b1_1111`.
- `docs/CIRCUS.md:32` lists milestones as 100 (Empty Ring), 100 per practical, 150 per investigation. Missing: +50 for
  the first talk with each of the eight performer roles (`CircusPerformerEntity.java:276`; the playthrough notes it as
  "LF-17, doc drift" at `GT/playthrough/PlaythroughCrimsonTroupeGameTests.java:344`, but `docs/PLAYTHROUGH_TESTS.md`
  has no LF-17), +150 for the attention act and +100 per act (`CircusPerformanceController.java:120`), +50 for the
  route choice. The same omission is in `HR:2850`.
- Fix: list every source and the bit layout in CIRCUS.md and HR §17.1; add LF-17 to PLAYTHROUGH_TESTS.md.

**D-4** · Medium · Verified · fix doc · user's call: no
Worldgen numbers describe a legacy pavilion that no longer ships and miss the abandoned stage.
- `docs/CIRCUS.md:58` "legacy pavilion 33x12x33 inside a 49x49 sample ... five-block max surface variation";
  `HR:2854` "saved legacy templates remain available".
- `R/data/hemomancy/structure/` holds only `crimson_troupe_pavilion.nbt` (65x14x65), five camps (25x9x25) and
  `troupe_abandoned_stage.nbt` (25x9x25, no actors). The only terrain gate is a 3-block range over every column
  (`StructurePlacementChecks.java:198`); the 5 is dead code (A-6). Overlaps W-5 and W-18.
- `R/data/hemomancy/worldgen/structure_set/troupe_abandoned_stages.json` (spacing 32, separation 10) is in no doc. All
  seven structures share type `hemomancy:circus_pavilion` and the biome tag
  `#hemomancy:has_structure/circus_pavilion`; only the 65-wide one gets emitters
  (`CircusPavilionStructure.java:58`).
- Fix: replace the paragraph with the three shipped templates, their sets and the one gate.

**D-5** · Medium · Verified · fix doc · user's call: no
Datagen claims name a deleted provider.
- `docs/CIRCUS.md:47` "pavilion template datagen `common/data/gen/CircusPavilionStructureProvider` +
  `CircusPavilionTemplate` (the one structure provider `runData` runs)": the class does not exist.
- `docs/architecture/ARCHITECTURE_STANDARDS.md:163` "Datagen runs only the blockstate, item-model and Circus pavilion
  providers" contradicts AGENTS.md ("`runData` runs two providers") and `DataGeneration.java:19-26`.
- Fix: both lines; how the pavilion NBT is authored now is W-3's question.

**D-6** · Medium · Verified · fix doc · user's call: no
"Where it lives" misses whole packages.
- `docs/CIRCUS.md:44-57` omits `common/mission/circus/` (`CircusPracticalController`, `CircusSchoolQuests`,
  `CircusCurriculum`, `CircusDemonstrations`, `CircusSchoolRules`), `common/network/circus/` (two payloads),
  `CircusWaybillItem`, `ThreadRipperManip`/`ThreadRipperRules`, `RingmasterPatternEntity` and
  `RingmasterConductorRules`, `CircusKnifeProjectileEntity`, `BloodDrunkPuppeteerEntity` (the captives),
  `PeacockSpiderEntity`, `LuminalCicadaEntity`, the bat aggregate in `VampireBatEntity`, `CircusRingmasterFrame` and
  `CircusRingmasterScene`, `CircusSchoolLedgerState` and the ledger's Circus tab, `NpcTradeActs`; and that the carousel's
  registry id is `impaled_procession` (`R/assets/hemomancy/lang/en_us.json:1488`).
- The GameTests row misses the trade, route, rite-kind, puppeteering and clinical rows; the review row misses
  `hudLayerReviewClient`'s Circus scene (`GT/HudLayerReview.java:51, 70`).
- Fix: rewrite the table from this audit's inventory.

**D-7** · Medium · Verified · fix doc · user's call: no
`LORE_REFERENCE` §6.3b says almost nothing about the Troupe the game ships.
- `LR:293-299`: three paragraphs; no "Crimson Troupe", Ringmaster, Impaled Procession, failed initiates, Succession
  or Liberation, Threadkeeper or faculty. The Court's §6.3c has a full table and guardrails.
- Fix: a §6.3b paragraph on the Troupe's people and the two endings, in the tone of `docs/NPC_VOICES.md`.

**D-8** · Low · Verified · fix doc · user's call: no
Status and evidence lines are stale.
- `docs/CIRCUS.md:3` "built and uncommitted in parts": every Circus path is committed (last touched in `d5a7ddbfa`
  2026-11-03, `8d01a316c`, `ea7bd732d`; tree clean). Reported as DC-11 on 2026-11-02 and not fixed.
- `docs/CIRCUS.md:107` "92 focused JVM tests and 15 Circus GameTests passed on 2026-10-07": the dedicated batch now has
  16, plus 11 Ringmaster tests and the CT band.
- Fix: a dated status line in the BRINED_COURT.md style.

**D-9** · Low · Verified · fix doc · user's call: no
The dev command path is wrong.
- `docs/CIRCUS.md:54` "`/hemo test circus run`, `succession|liberation start|run`": the command is `/hemo test journey
  circus ...` (`GT/HemoTestCommands.java:30, 53-63`; `docs/TESTING.md:453-478`).

**D-10** · Low · Verified · fix doc · user's call: no
Two trade claims are wrong.
- `docs/CIRCUS.md:38` "at most four per pavilion": the challenge bits are per player and global, so a player banks at
  most four spectacles ever (`CircusPerformanceController.java:40, 120-122`; `CircusPlayerProgress.java:90-98`).
- `wiki/Crimson-Troupe.md:5` "The Ringmaster and every professor ... take curiosities": only
  `R/data/hemomancy/npc_trade/circus_ringmaster/curiosity.json` asks for `#hemomancy:trade/curiosity`; each performer
  has one items card (`npc_trade/circus_acrobat/rigging_line.json` and siblings), as CIRCUS.md's own "Calls made" says.

**D-11** · Low · Verified · fix doc · user's call: no
Puppet Interference is half described.
- `docs/CIRCUS.md:49, 64` give only the Morphling side (x1.25 upkeep while a puppet is active,
  `J/common/item/harbinger/morphlings/MorphlingUpkeepRules.java:4`). The puppet side, 0.75 tether range and x1.5 thread
  upkeep while a Morphling is equipped (`J/common/entity/summon/puppeteer/PuppeteerSummonRules.java:17-18`, `HR` §17
  "Morphling interference"), is not mentioned. Fix: one sentence and a link.

**D-12** · Low · Verified · fix doc · user's call: no
Tool tracking is out of step with AGENTS.md and HR.
- `tools/circus/*.py` (23 files) is tracked since 2026-11-02 A-28 (`.gitignore:73-75`), but AGENTS.md's list of
  version-controlled tools (AGENTS.md line 29) omits it. `HR` §17 "Puppet appearance" cites
  `tools/oneoff/PUPPET_ASSETS.md`, which is ignored (`.gitignore:70`).
- Fix: add `tools/circus/` to the AGENTS.md list; track or drop the PUPPET_ASSETS citation.

**D-13** · Low · Verified · fix doc · user's call: no
`OPEN_QUESTIONS.md` lacks the questions this audit raises.
- D-2 (Thread Ripper as tool or prize), A-10 (puppet damage on the Ringmaster; bystander AoE) and A-3 (guest pay
  retry) need rows in §1.1; PT-57's evidence (`docs/OPEN_QUESTIONS.md:157`) already depends on D-1's correction.

---

## Decisions the owner must make

1. D-2: should Liberation require Thread Ripper to cut the captives, and if so how does a D4 or D5 player get it? Or
   is the right-click the design and Thread Ripper only the prize (then it is a Summa memory most Liberators cannot
   learn yet)?
2. A-10: may a puppeteer's puppets damage the Ringmaster in DESCENT? Should troupe area attacks spare bystanders who
   do not own the site?
3. A-3: should guest pay become an automatic `PendingDelivery` claim, or stay "collect from a Threadkeeper"?
4. T-2: is it acceptable to gate a compact finale and the living-observation rule in `crimson_troupe_validation`
   (adds roughly one to two minutes to `alphaCheck`)?
