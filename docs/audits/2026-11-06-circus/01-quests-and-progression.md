# Audit: Circus quests, progression and NPC dialogue (2026-11-06)

Scope: the Crimson Troupe journey from pavilion discovery to the Ringmaster's two endings. That covers the D4 referral
and Scarlet Waybill, the apprenticeship (`common/mission/circus/*`), the Side assignments, acclimation, routes, rewards,
dialogue, voice and trade, compared against the Brined Court. Code and data only. Nothing was built, run or edited
except this file.

**Summary.** 17 findings: 4 High, 8 Medium, 5 Low. The apprenticeship is the most finished part. It has seven
server-observed practicals, an inspected Crossbar, three investigations with physical evidence, a daily paid guest act,
eight Liber pages and a ledger section, and in places it is better tracked than anything the Court has. The Circus does
not work as a parallel path, though, because its culmination is cut off from its middle. The Ringmaster's pact, acts and
finale ask for none of the apprenticeship, no investigation and no Degree. Acclimation, the only gate, fills from greeting
eight NPCs, the Ringmaster's own acts and a few minutes of standing in the tent. The acts and both finales never ask the
player to puppeteer. After the finale nothing in code reads the result. The Ringmaster also cannot be clicked from the
pavilion floor (D-57), so the whole culmination is unreachable in survival without building up. The entry has two
problems as well. Every puppet ordeal from D3 needs Circus instruction, yet nothing locates a professor before D4. And
the Waybill's locator runs the same synchronous search that once stalled `/locate` for 209 s, which is the exact problem
the Court's Relay Chart was rebuilt to avoid. The finale rewards bypass `PendingDelivery`.

**Method.** I read the code paths listed in each row and checked them with greps over `src/main`, the lang file, trade
and book data, the structure NBTs (decompressed: which NPCs and record blocks each template holds),
`route-fingerprints.txt` and `PlaythroughCrimsonTroupeGameTests`. The Court side was read from
`common/capability/player/harbinger/court`, `common/entity/npc/court`, `CourtMemberDialogue` and its track files.
"`J/`" below means `src/main/java/com/vincenthuto/hemomancy/`. No GameTest was run, so runtime behaviour is read from
code, and where a claim rests on code reading alone it says so.

---

## Journey map

| # | Step | Gate (server) | Teacher / pointer | Proof recorded | Code path |
|---|---|---|---|---|---|
| 0 | Find the pavilion | Living, non-spectator, inside a `circus_pavilion` piece; any Degree | None before D4 | `CircusProgress.discovered`; +1 acclimation per 80 ticks inside | `J/common/capability/player/harbinger/circus/CircusDiscoveryProgress.java:37-57` |
| 1 | First talk with each performer | Right-click a performer not downed or alert; any Degree | Performer greeting | Milestone `performer.<role>`, +50 each (8 roles = 400); Liber `troupe_school` | `J/common/entity/npc/circus/CircusPerformerEntity.java:271-283` |
| 2 | Apprenticeship card shows in the ledger | Degree 3 | Ledger card "Receive personal instruction from the Acrobat", with no place named | none | `J/client/screen/item/HarbingerAssignmentLedgerScreen.java:140,954-984` |
| 3 | D4 referral and Waybill | Degree 4, not purifying, not carrying one | Vicar topic, Mnemonist `circus` node, Waybill (use: next untaught professor's camp; sneak: unfinished pavilion) | none (the Vicar records nothing) | `J/common/entity/npc/dialogue/CircusReferralDialogue.java:11-27`, `HarbingerMnemonistDialogueTrees.java:198-249`, `route/MnemonistDialogueEvents.java:96-106`, `J/common/item/harbinger/CircusWaybillItem.java:11-41` |
| 4 | An Empty Ring | Inspect at D3 with an owned, charged Crossbar; then call, command and recall a known shape with it | Threadkeeper (its briefing node is unreachable, D-60) | `quest.empty_ring`, +100 | `J/common/mission/circus/CircusSchoolQuests.java:24-58,62-69,93-99` |
| 5 | Lesson (x7) | Shape's Degree, active blood, professor free | Professor `lesson` node and `status.*` line | `taught.<shape>`, ordeal recipes, Liber page | `J/common/capability/player/harbinger/circus/CircusApprenticeshipProgress.java:26-35` |
| 6 | Cardinal Ordeal | Instruction or already known (every shape) | `instruction_required` line | Known summon | `J/common/rite/harbinger/PuppeteerTrialRiteController.java:69-73` |
| 7 | Practical (x7) | Own Crossbar in hand, the shape's Degree, two Husk targets fit; 6000-tick expiry, 20-block leash | `exercise.<shape>` line | `practical.<shape>`, +100 | `J/common/mission/circus/CircusPracticalController.java:38-74,75-101,156-162` |
| 8 | Guest act | Practical passed, one per in-game day across all professors, nothing pending | `status.mastered` | 4 Filaments, pending in the apprenticeship tag; claimed by hand at a Threadkeeper | `CircusSchoolQuests.java:100-136` |
| 9 | The Wrong Audience | Stilt Walker and Dagger Thrower testimony, Mummer and Juggler practicals, D4 at report | Testimony lines, ledger | `quest.wrong_audience`, +150 | `CircusSchoolQuests.java:81-87` |
| 10 | The Missing Understudy | Stage, loft, quarters and carousel records plus the Understudy's testimony | Clue lines chain to each other; nothing points to the stage (D-58) | `quest.missing_understudy`, +150 | `CircusSchoolQuests.java:88-92`, `J/common/block/harbinger/decoration/CircusEvidenceBlock.java` |
| 11 | Ringmaster speaks | Acclimation 500 (silent below) | None (perch unreachable, D-57) | none | `J/common/entity/npc/circus/CircusRingmasterEntity.java:385-402` |
| 12 | Attention act | Acclimation 500, NEUTRAL route, site free | `challenge.begin.0` | Bit 0, +150, one banked spectacle | `J/common/entity/npc/circus/CircusPerformanceController.java:37-58,112-127` |
| 13 | Choose a route | Bit 0 and acclimation 500; warning, then confirm; one repair | Warning nodes | Route, bit 1, +50 | `CircusRingmasterEntity.java:252-285` |
| 14 | Succession acts 2-4 | SUCCESSION route; stay within 24 blocks for 200 / 240 / 300 ticks | `challenge.begin/hold.*` | Bits 2-4, +100 each | `CircusPerformanceController.java:60-65,80-92,115-120` |
| 15 | Finale | Succession: 1000 and bits 0-4. Liberation: the route alone. Site free and NEUTRAL | `finale.not_ready` / `claimed` | Site outcome, route `*_COMPLETE` | `J/common/capability/player/harbinger/circus/CircusRouteRules.java:24-28`, `CircusRingmasterEntity.java:287-357`, `CircusFinaleRules.java:11-23` |
| 16 | Rewards | none further | finale line | Ringmaster Pattern (Succession) or Thread Ripper and ruin (Liberation); Topper on both | `CircusRingmasterEntity.java:339-348,359-367` |
| 17 | After | none | Ringmaster `line.*_complete`; professors `aftermath.*_complete` | Nothing reads the completed route or `completionOwner` beyond dialogue | grep `SUCCESSION_COMPLETE`, `completionOwner()` |

Acclimation available: greetings 400, Ringmaster acts 500 (150 + 50 + 3 x 100), school 1100 (100 + 7 x 100 + 300), passive
without limit. Succession needs 1000, so greetings plus acts plus about 7 minutes in the tent reach it without one lesson.

---

## Compared with the Brined Court

| Concern | Brined Court | Circus | Gap |
|---|---|---|---|
| Discovery routes | Three (Vicar to Voyager, Votary letter, Alchemist with a Mature strain) plus entering a site, all converging on one "known" state with `KnownBy` | One: entering the pavilion. The Vicar and Mnemonist refer, but record nothing | Circus thinner, but adequate for a Side faction |
| Locator | Relay Chart: pure cell math, biome predicate, off-thread, cached (`05-court-discovery.md:53,96`) | Waybill: synchronous vanilla `findNearestMapStructure` on use and in `inventoryTick` (Q-3) | Circus behind, and a server-stall risk |
| World signs | One capped placed feature (blooms, markers, buoys), `worldgen/placed_feature/court_signs.json` | None. Camps and the abandoned stage are separate structures with no trail to the pavilion | Circus lacks |
| Entry pointer by Degree | D5 referral matches the D5 join gate | D3 ordeals need Circus instruction, but the referral comes at D4 (Q-4) | Circus mismatched |
| Membership and identity | Join a house (D5 plus guest standing), one free switch, per-house standing in three tiers (stranger, guest, friend) | None. Succession makes you "successor" in two lines and nothing else | Circus lacks |
| Middle feeds the culmination | Standing gates joining and the Champion; the lesson needs the house's strain | Apprenticeship and investigations pay only acclimation, which is reachable without them (Q-2) | Circus lacks |
| Culmination | Champion title by usurping a follower miniboss (retry wait) and the Bloom Warden; both recognised afterwards | One Ringmaster finale per player, ever; result unread (Q-8) | Circus thinner |
| Repeatable loop after | Customs, standing, house switch, Champion retry | Daily guest act (4 Filaments) only | Similar, both thin |
| Degree gating | D5 join and lesson; Champion has none (BC-8) | None on pact, acts or finale; lessons read the table | Both have a hole; the Circus hole hands out a D4 summon |
| Unstained | Shunned by every house through `ProgressionGates.isUnstained` | No rule. The Pattern grant is silently refused at the end (Q-6) | Circus lacks |
| Dialogue construction | `CourtMemberDialogue.build(role, entityId, View)` is pure and unit-tested in every state, offering only what the player may do now | `CircusSchoolDialogue.tree` takes the entity and player, offers every quest card at every Degree, and has no unit test (Q-10) | Circus behind |
| Voice sheets | Full sheet per role (who, view, rhythm, questions, words, habits, avoid, example) | Full sheet for the Ringmaster, one table row per professor, about 20 shared lines (Q-13) | Circus behind |
| Item inquiries | All 17 Court role folders under `dialogue_inquiry/` | None for any Circus speaker | Circus lacks |
| Route fingerprints | Every `court_member_*` and Court discovery event | 3 of 12 Circus events plus the Waybill (Q-14) | Circus behind |
| Assignment Ledger | No Court section | Referral section and an 11-card apprenticeship section, but no Ringmaster section (Q-9) | Circus ahead, with a hole |
| Liber | 3 strain pages | 8 apprenticeship pages; nothing on the pavilion, acclimation, the Ringmaster, routes, Pattern or Thread Ripper | Circus ahead on teaching, silent on the culmination |
| Advancements | none | none | equal |
| Reward delivery | Welcome gifts use `add` then `spawnAtLocation` (`CourtHospitality.java:184-185`) | Topper and Thread Ripper item use `add` then `drop`; guest pay is hand-rolled (Q-5) | Both outside `PendingDelivery`; the Circus's items are unique and never sold |
| Vacancies | `CourtVacancies` replaces members after `/kill` or the void | None; a lost Ringmaster leaves the site stuck (D-62) | Circus lacks |
| Minibosses | Two followers, each with a bar | One finale; C-13 midgame minibosses only a strong candidate | Circus thinner |
| Server-observed skill proofs | none | Seven practicals that read the summon's actual behaviour | Circus ahead |
| Investigations | none | Two, with physical records and testimony | Circus ahead |

---

## Findings

| Id | Sev | Claim | Action | User call |
|---|---|---|---|---|
| Q-1 | High | The Ringmaster cannot be clicked from the pavilion floor, and the acclimation geometry meant to reach him was never built | fix | Yes (D-57) |
| Q-2 | High | The culmination ignores the apprenticeship, the investigations and Degree; a D0 player can finish both routes | decide | Yes |
| Q-3 | High | The Scarlet Waybill runs the slow vanilla structure search on the server thread, up to nine times per use | fix | No |
| Q-4 | High | Every puppet ordeal needs Circus instruction, but nothing locates a professor before D4 | decide | Yes (PT-21) |
| Q-5 | Medium | Finale rewards and guest pay bypass `PendingDelivery`; an Unstained Succession loses the Pattern silently | fix | Minor |
| Q-6 | Medium | The Circus has no Unstained rule | decide | Yes |
| Q-7 | Medium | No act or finale asks the player to puppeteer; Thread Ripper is the reward for the act it is meant to perform | decide | Yes |
| Q-8 | Medium | Nothing follows the finale: the completed route is read only by dialogue | decide | Yes |
| Q-9 | Medium | No progress readout for acclimation, the acts or the route | fix | No |
| Q-10 | Medium | The school hub offers every quest card in every state; only the Threadkeeper's cards carry status | fix | No |
| Q-11 | Medium | The Missing Understudy can be lost: there is no pointer to the stage, and the records can be broken | fix | Minor (D-58) |
| Q-12 | Medium | Failure and recovery holes in practicals and the finale | fix | Yes (D-59, D-60, D-62, D-63) |
| Q-13 | Low | Voice: thin faculty sheets, shared lines, the Ringmaster using shared `aftermath`, 12 dead barks, no inquiries | fix | Minor |
| Q-14 | Low | Route fingerprints cover 3 of 12 Circus events | fix | No |
| Q-15 | Low | Architecture debt in Circus player state and sync | fix | No |
| Q-16 | Low | Doc drift in `CIRCUS.md`, HR §17.1 and the ledger wording | fix | No |
| Q-17 | Low | Trade cards have no tie to the apprenticeship or puppetry | expand | Minor |

### Q-1 (High): the Ringmaster is out of reach, so the culmination is closed in survival

- `PlaythroughCrimsonTroupeGameTests.ringmasterReachFromTheFloor` (`src/gameTest/.../playthrough/PlaythroughCrimsonTroupeGameTests.java:936-973`)
  checks all 1888 standable floor cells of the real pavilion template against the server's interact check, and none
  reaches the idle Ringmaster on his y7 perch. That one click carries the pact, all four acts, the finale and the Trade box
  (`J/common/entity/npc/circus/CircusRingmasterEntity.java:390-402`).
- `docs/CIRCUS.md:32` says private galleries and stairs support the player from 500 acclimation and that the upper gallery
  is solid at 1000. No server code does this. Acclimation stages feed only client particles and sounds
  (`J/client/screen/overlay/CircusPerceptionOverlay.java:59,71,86`) and the two thresholds in `CircusProgressRules`.
- **Fix.** Pick the cheapest option that keeps the reality rule (client illusion never decides collision). Either give the
  perch real template stairs or a rope ladder, or have the Ringmaster step down to the ring hub when a player at 500 or more
  comes within 12 blocks, then return to the perch. The second needs no template change and fits his CAROUSEL pose. Correct
  `CIRCUS.md:32` in the same change.

### Q-2 (High): the culmination does not depend on the middle, or on Degree

- Pact: bit 0 and acclimation 500 (`CircusRingmasterEntity.java:255-269`). Acts: NEUTRAL or SUCCESSION only
  (`CircusPerformanceController.java:37-43`). Finale: Liberation always passes, Succession needs 1000 and bits 0-4
  (`J/common/capability/player/harbinger/circus/CircusRouteRules.java:24-28`). No Degree check, no apprenticeship or
  investigation flag, and no active-blood check exists anywhere on this path.
- Acclimation comes cheap. Each first talk with a performer adds 50 (`CircusPerformerEntity.java:276`), and all eight roles
  stand in the pavilion template (decompressed `crimson_troupe_pavilion.nbt`), so that is 400 in a minute. The acts add 500.
  The 100 still missing takes 8000 ticks of standing inside. The 1100 points the school pays are surplus.
- What that hands out. Succession grants the Ringmaster Pattern, a Degree 4 shape (`PuppeteerSummonDefinitions.java:35`),
  through `KnownSummonEvents.grantSummon`, which has no Degree check (`KnownSummonEvents.java:18-26`). `requiredDegree()` is
  read only by client screens and by the recipe award (grep). Liberation hands a D0 player the Thread Ripper memory item.
- The text says this is intended: "Finish every lesson and refuse my offer, if you like. Or step into my act without every
  professor's approval" (`en_us.json:2479`); "the apprenticeship adds nothing to it" (`aftermath.succession`); "Apprenticeship
  adds no Harbinger Main or finale requirement" (`wiki/Crimson-Troupe.md:39`). That is the structural reason the Circus reads
  as one finale with a separate school beside it. The Court ties every step to the next (L5, L6, standing gates the
  Champion).
- **Fix (owner's call).** (a) Succession needs the Mummer and Juggler practicals plus The Wrong Audience (attention and
  focus, the two faculties the Ringmaster's office is built on). Liberation needs The Missing Understudy report, so the
  player knows who rides the horses. (b) Gate the pact at D4 with `ProgressionGates.hasDegree(player, 4)`, the Pattern's
  own Degree, and say so in the Ringmaster's line. (c) Stop counting first-talk milestones as pavilion acclimation, or cut
  them to 10. (d) Have the Ringmaster's warning and greeting read the investigations, for example a Liberation warning that
  names the understudies once the player has reported them.

### Q-3 (High): the Waybill's search can stall the server

- `CircusWaybillItem.findTarget` (`J/common/item/harbinger/CircusWaybillItem.java:11-41`) calls
  `level.findNearestMapStructure(..., 160, false)` on the server thread. When the nearest pavilion is already settled it
  calls it up to eight more times from offset origins (lines 34-39). The base class also runs it from `inventoryTick`
  whenever the item is held with no target in this dimension (`CovenantWaybillItem.java:61-67,102-104`).
- Camps and the abandoned stage use the same `hemomancy:circus_pavilion` structure type, with the same footprint scan
  (`data/hemomancy/worldgen/structure/troupe_camp_air.json:2`). `docs/PELAGIC_WORLDGEN.md:150` records a live `/locate` of
  the pavilion taking 209 s, with footprint checks at 68% of server-thread work. The Court's Relay Chart was built off-thread
  for exactly this reason (`docs/archive/tracks/brined_court/05-court-discovery.md:53,96`).
- **Fix.** Move the Waybill onto the Relay Chart's pattern. Predict candidate cells from the `random_spread` cell math,
  test each with the cheap biome predicate, run that on `Util.backgroundExecutor()`, and record the result later through
  the existing `attune` hook. The base class comment already plans for this: "A subclass whose search is slow records it
  later instead". Cache per dimension and cell, allow one search per player, and clear through `TransientServerStateEvents`.

### Q-4 (High): the Puppeteering entry has no pointer at D3

- All seven shapes need instruction before their ordeal (`J/common/rite/harbinger/PuppeteerTrialRiteController.java:69-73`,
  `J/common/mission/circus/CircusCurriculum.java:10-17,33-36`), and two of them are D3 shapes. The D3 ledger shows "Receive
  personal instruction from the Acrobat" (`HarbingerAssignmentLedgerScreen.java:140`, `assignmentDegree` 3), but the
  Waybill and the Mnemonist's introduction both start at D4 (`MnemonistDialogueEvents.java:101`,
  `CircusIntroductionRules.java:4`). So at D3 the player knows they need a professor and has no way to find one.
- The archived ruling assumes the opposite order: "The Mnemonist introduces the Circus at Degree 4, after the player has
  practical exposure to puppetry" (`docs/archive/tracks/circus/01-faction-and-progression.md:13`). In code, that exposure
  can only come from the Circus. Logged as PT-21 (`PlaythroughInitiateGameTests.d3PuppetryHasNoPointer`).
- **Fix (owner's call).** Either let the Mnemonist issue the Waybill at D3 in its "next untaught professor" mode only,
  keeping the D4 Circus introduction, or add one D3 Mnemonist line naming the camps, or exempt the two D3 shapes from the
  instruction gate.

### Q-5 (Medium): finale rewards and guest pay sit outside `PendingDelivery`

- Topper: `if (!player.getInventory().add(topper)) player.drop(topper, false)` (`CircusRingmasterEntity.java:347-348`). A
  Creative full inventory discards it and counts that as delivered. The Liberation kill path in `die()` accepts any damage
  source from the owner, so a projectile landing after the owner died can complete the route and add the item to a dead
  player (`CircusRingmasterEntity.java:323-332`, by code reading). The Topper is tagged `trade/never_sold` and has no
  reissue.
- The Thread Ripper memory fallback uses the same add-or-drop (`CircusRingmasterEntity.java:362-365`).
- An Unstained player who finishes Succession has the route set to `SUCCESSION_COMPLETE` and the site settled before
  `grantSummon` returns false silently (`KnownSummonEvents.java:19`, `CircusRingmasterEntity.java:338-342`). The Pattern is
  then lost for good.
- Guest pay keeps an exact `guest.pending` count and inserts into slots by hand (`CircusSchoolQuests.java:105-136`). That is
  close to the standard, but it is retried only when the player picks the Threadkeeper's claim card.
- **Fix.** Add one `PendingDelivery` claim, `CircusFinale.REWARDS`, that stores the exact owed stacks (Topper, plus the
  Thread Ripper memory when the grant was refused) in `CircusProgress` before `complete()` returns. Turn guest pay into a
  second claim fed from `guest.pending` and keep the Threadkeeper card as a manual nudge. Check the Unstained gate at the pact
  and the finale start, not after the outcome (Q-6).

### Q-6 (Medium): no Unstained rule

- No Circus class references `isUnstained`, purification or `UnstainedAccessRules` (grep over `entity/npc/circus`,
  `mission/circus`, `capability/player/harbinger/circus`, `dialogue/Circus*`). The only checks are acclimation, plus active
  blood for lessons. An Unstained player can be acclimated, choose a route and run either finale. The Court shuns
  explicitly (`docs/archive/tracks/brined_court/11-court-life-and-progression.md:37,90`).
- **Fix (owner's call).** Choose a rule: refuse the pact (the Ringmaster sees no strings to take), let the troupe perform
  but never teach, or allow Liberation only. Then apply it at the pact and in `tradesWith`, through `ProgressionGates`.

### Q-7 (Medium): the school's skills never reach the finale

- Act 0: look at a spider for 200 ticks. Acts 2-4: stay within 24 blocks while the troupe attacks, for 200, 240 and 300
  ticks (`CircusPerformanceController.java:101,112-120`; lang `challenge.begin.2/4`: "Stay inside the ring..."). Succession
  CAROUSEL: melee the troupe down. DESCENT: melee the Ringmaster to 1 HP (`CircusRingmasterEntity.java:315-319`). Nothing
  reads an active shaped body.
- Liberation severs a captive by right-clicking it (`J/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java:134-138`).
  Thread Ripper, which HR §17.1 says Liberation "uses", is the Liberation reward (`CircusRingmasterEntity.java:344`), and it
  has no other source (grep). Its carousel branch (`J/common/manipulation/ductilis/ThreadRipperManip.java:66`) needs the
  caster to own an active Liberation. A player who holds Thread Ripper has already completed, so in survival that branch
  never runs.
- **Fix (owner's call).** Make each act a coordination test. Act 2: a Mummer keeps the dolls off the player. Act 3: a body
  intercepts or draws the knife line. Act 4: two bodies at once, which is the Conductor's job. Grant Thread Ripper at the
  Liberation confirm, or at the Missing Understudy report, and make severing need it, so the reward and the act are one
  thing.

### Q-8 (Medium): nothing follows the finale

- `SUCCESSION_COMPLETE`, `LIBERATION_COMPLETE` and `Site.completionOwner()` have no readers outside dialogue keys
  (grep). The Ringmaster's `line.succession_complete`, "The pavilion knows its successor. It answers to you now."
  (`en_us.json:2328`), keys off the player's route, not the site. Any other pavilion's living Ringmaster says it too, and no
  pavilion answers to anyone.
- Route is one per player for life (`CircusRouteRules.java:11-18`). The only loop left is one guest act a day. The Court
  keeps standing, a house switch, the Champion title and its retry.
- **Fix (owner's call).** Give the successor something real: run the four acts as a host for other players, get new
  professor lines and a guest act fee, or let the pavilion's own Ringmaster yield his perch. At minimum, key the line to
  `completionOwner` so other pavilions say something different. C-13 (midgame minibosses) is the natural home for more
  culmination content.

### Q-9 (Medium): the player cannot see route progress

- The Circus ledger section lists the three investigations, the seven practicals and the guest card, nothing more
  (`HarbingerAssignmentLedgerScreen.java:954-984`, `J/client/screen/item/CircusSchoolLedgerState.java`). Acclimation,
  acts 0/2/3/4, the route, whether the repair is spent and the finale state appear nowhere in the UI. The Succession gate
  of 1000 is invisible, and `finale.not_ready` says "complete your attunement" with no number (`en_us.json:2315`).
- **Fix.** Add a "The Ringmaster's Bargain" section at D4. It shows the acclimation stage name (not the raw number, which
  keeps the mystery), one card per act, the route and the repair, and the finale. Carry it on the existing
  `PacketSyncCircusSchool` snapshot.

### Q-10 (Medium): the school hub ignores the card-state rule

- `CircusSchoolDialogue.tree` adds `take_lesson`, `practical`, `guest` and `testimony` for every professor at every Degree,
  D0 included, after mastery included (`J/common/entity/npc/dialogue/CircusSchoolDialogue.java:24-39`). Only the
  Threadkeeper's cards get a status (lines 59-83). The Ringmaster offers `finale` while Succession is not ready
  (`CircusRingmasterEntity.java:414-421`). Picking these gives refusals. `docs/NPC_DIALOGUE_AND_TRADE.md` "Card states and
  sigils" expects a notice while actionable and Turn In only for a claim.
- The tree takes the live entity and `ServerPlayer`, so it cannot be unit-tested the way `CourtMemberDialogueTest` tests the
  pure `CourtMemberDialogue.build`.
- **Fix.** Build the professor tree from a `View` record: Degree, active blood, `nextStep`, `guestEligible`, pending pay and
  route. Offer `take_lesson` only at `lesson`, `practical` only at `practical`, and `guest` only when mastered and eligible,
  each with a notice sigil. Put `testimony` only on the three speakers whose testimony counts, and drop it once both
  investigations are reported. Add a `CircusSchoolDialogueTest` over every state.

### Q-11 (Medium): The Missing Understudy can be lost

- The stage record exists only in `troupe_abandoned_stage.nbt` (spacing 32 / separation 10), and nothing locates it
  (D-58). The loft, quarters and carousel records are in the pavilion, and quarters also in the memory camp (decompressed
  NBTs).
- The records have strength 2 and no loot table: "Structure-only evidence has no survival recipe or drops"
  (`J/common/init/BlockInit.java:1274-1277`, `CircusEvidenceBlock.java`). A player who breaks one, or another player in
  multiplayer, loses that clue at that site. The carousel clue reads only with a carousel entity within 24 blocks
  (`CircusEvidenceBlock.java`, the `carousel` branch).
- **Fix.** Make the records unbreakable outside Creative (`strength(-1, 3600000)`), or record the clue on break. Give the
  Understudy's testimony, or a Waybill mode, a bearing to the nearest stage.

### Q-12 (Medium): failure and recovery holes already logged

- D-59: the targets are Husks (`CircusPracticalController.java:57`), so Peaceful removes them and no practical or guest act
  can finish.
- D-63: a killed target is discarded (`CircusPracticalController.java:111`). The Mummer and Bellows practicals need two,
  so the session sits silent until its 6000-tick expiry (line 34).
- D-62: a Ringmaster lost mid-finale leaves the site owned in RAFTERS with no respawn, and the sneak Waybill keeps pointing
  there.
- D-60: the Threadkeeper's `lesson` node is built for every role (`CircusSchoolDialogue.java:47-51`), but its option exists
  only for professors with a curriculum lesson (lines 29-33). The An Empty Ring briefing (`en_us.json:2381`) is dead text.
- **Fix.** Use non-monster targets, for example a straw dummy `PathfinderMob`, which closes D-59 and D-63 together. End a
  session at once with `retry` when a target dies. Release the site with a line when the Ringmaster is missing for 200
  ticks, and add a Ringmaster vacancy rule like `CourtVacancies`. Point an "An Empty Ring" option on the Threadkeeper at the
  existing node.

### Q-13 (Low): voice and inquiries

- `docs/NPC_VOICES.md:481-500` gives the eight professors one table row each, against full per-role sheets for the Court.
  About 20 `status.*`, `aftermath.*`, `preparation`, `report_*`, `passed` and `retry` keys are spoken by all eight (open
  decision 11).
- The Ringmaster's `school` node speaks the shared `aftermath.<route>` line (`CircusRingmasterEntity.java:429`), which his
  sheet tells writers to avoid (`NPC_VOICES.md:471`). `aftermath.neutral` has him say "Nobody's settled the Ringmaster's
  bargain yet" (`en_us.json:2334`).
- Twelve barks, `hemomancy.dialogue.circus_<acrobat|fire_eater|knife_thrower|stilt_walker>.line1-3`
  (`en_us.json:3181-3194`), have no Java reference left (decision 13).
- No `dialogue_inquiry/circus_*` folder exists, so the Inquiries box is empty for all nine Circus speakers.
- **Fix.** Give the Ringmaster his own `ringmaster.aftermath.*` keys. Split the status lines per role. Write full faculty
  sheets before that. Delete the barks, or wire them to idle performances. Add a few inquiries per professor (the Crossbar,
  Filaments, their own frame item).

### Q-14 (Low): route fingerprints cover little of the Circus

- `src/gameTest/resources/dialogue/route-fingerprints.txt:79-90` fingerprints `circus_school_teach`,
  `circus_school_inspect` and `circus_reject_pact`, plus `mnemonist_circus_waybill` at lines 202-204. There are no rows for
  `practical`, `guest`, `testimony`, `report`, `claim`, `accept_pact`, `repair_route`, `begin_next_trial` or
  `begin_finale`. Every Court event has a row.
- **Fix.** Add those nine events to `DialogueRouteCharacterisationGameTests` and regenerate the golden.

### Q-15 (Low): architecture debt in Circus state and sync

- `CircusDiscoveryProgress.onPlayerTick` sends `PacketSyncCircusPerception` to every online player once a second, wherever
  they are (`CircusDiscoveryProgress.java:38-57`). It also resends on respawn and dimension change (lines 59-67), which
  `AGENTS.md` forbids ("do not add per-system lifecycle resends"). `CircusProgress` has no `PlayerStateSync` row.
- The apprenticeship is a raw `CompoundTag` with string keys (`taught.`, `practical.`, `primer.`, `testimony.`, `clue.`,
  `quest.`, `guest.`), written from five places, a block among them (`CircusApprenticeshipProgress`, `CircusSchoolQuests`,
  `CircusSchoolDialogue.hub`, `CircusEvidenceBlock`, `PacketSyncCircusSchool`).
- Live act state is kept in player root persistent data (`CircusPerformanceController.java:29-32`, 20 allow-listed calls).
- `CircusPlayerProgress.foldLegacyRootKeys` (`CircusPlayerProgress.java:105-127`) is a migration with a sunset date and no
  caller. The mod is unreleased and the migrations table is empty, so it is dead.
- Degree checks compare `getPlayerDegreeNumber` against the lesson table (`CircusPracticalController.java:42`,
  `CircusSchoolQuests.java:19`, `CircusApprenticeshipProgress.java:29`, `CircusWaybillItem.java:15`) instead of
  `ProgressionGates.hasDegree(player, lesson.degree())`, while `CircusSchoolQuests.java:25,83` use `ProgressionGates`.
- **Fix.** Send perception only on a change of stage or inside-state, and add a `PlayerStateSync` row. Give the
  apprenticeship typed fields in `CircusProgress`, as `CourtProgress` has. Move act state into the attachment. Delete
  `foldLegacyRootKeys`. Use `ProgressionGates` throughout.

### Q-16 (Low): doc drift

- `docs/CIRCUS.md:32` lists the milestones as 100, 100 per practical and 150 per investigation. It leaves out 50 per first
  talk, 150 for attention, 100 per act and 50 for the route, and it claims private geometry (Q-1).
- `CIRCUS.md:34` names four challenges for "five challenge bits". Bit 1 is the route choice
  (`CircusRingmasterEntity.java:259,266`). It also says Liberation "uses Thread Ripper", which is the reward (Q-7). HR §17.1
  says the same.
- The guest act is one per in-game day across all professors (a single `guest.day` key, `CircusSchoolQuests.java:100-110`),
  but `ledger.guest.available` (`en_us.json:2426`) reads as one per professor. It uses `getDayTime`, which moves with
  sleeping and `/time set`. The Court's retry uses game time.
- **Fix.** Correct `CIRCUS.md` and HR §17.1. Decide per-professor or global for the guest act and say it. Switch to
  `getGameTime() / 24000`.

### Q-17 (Low): trade does not reflect the school

- Performer cards give vanilla potions, scaffolding, arrows and a name tag (`data/hemomancy/npc_trade/circus_*/*.json`).
  Only the Ringmaster's bell and the Threadkeeper's paper give Filaments. No card has a `requires` on a practical, a route
  or the Succession title, and nothing sells or repairs puppetry goods.
- **Fix.** Give each professor one card unlocked by their practical that pays their shape's upkeep or frame materials.
  Never a memory or kit, per `NpcTradeGuardrailTest`.

---

## Decisions the owner must make

Already open in `docs/OPEN_QUESTIONS.md` §1.6 (default: as built, probe failing):

- **PT-21.** D3 puppetry needs Circus instruction, but nothing points to the Circus at D3. Options: a D3 Waybill or line,
  move the gate to D4, or accept exploration.
- **D-57 [CT-1].** The idle Ringmaster's y7 perch is unreachable from all 1888 floor cells. Options: build the galleries,
  real stairs or a reachable perch, lower the perch when idle, or accept building up.
- **D-58 [CT-2].** The Missing Understudy's stage record has no pointer. Options: a Waybill mode, an NPC direction, or
  exploration.
- **D-59 [CT-3].** The Husk targets vanish in Peaceful, so no practical or guest act can finish. Options: non-monster
  targets, a Peaceful refusal line, or accept.
- **D-60 [CT-5].** The Threadkeeper's An Empty Ring briefing node is unreachable. Options: add an option, fold the briefing
  into the inspect or report lines, or drop the node.
- **D-61 [CT-6].** `finale.claimed` reads as permanent, and `not_ready` names Succession to a neutral visitor. Options:
  separate lines, or keep.
- **D-62 [CT-7].** A Ringmaster lost mid-finale leaves the site owned and stuck for good. Options: a respawn rule, release
  the site with a line, skip such sites in the Waybill, or accept.
- **D-63 [CT-8].** One killed rehearsal threat stalls a two-threat practical silently. Options: replace the threat, end with
  the retry line, or accept.

New from this audit:

- **Coupling (Q-2).** Should Succession need named practicals or The Wrong Audience, and should Liberation need The
  Missing Understudy? Or does the "the apprenticeship adds nothing" ruling stand?
- **Degree gate (Q-2).** Should the pact or the finale need D4 (the Pattern's Degree)? Today a D0 player can finish both.
- **Unstained (Q-6).** Refuse, perform but not teach, or Liberation only?
- **Thread Ripper timing (Q-7).** Grant it at the Liberation confirm or the Missing Understudy report and require it to
  sever, or keep it as the post-finale reward?
- **Acts as puppetry tests (Q-7).** Rewrite acts 2-4 so a shaped body must do the work, or keep them as endurance acts?
- **After the finale (Q-8).** Does the successor get a real role (hosting acts, a fee, the perch), and does C-13 (midgame
  minibosses) go ahead?
- **First-talk acclimation (Q-2).** Keep 50 per first talk with a performer, or reduce or remove it so acclimation means
  time in the pavilion?
- **Guest act cadence (Q-16).** One per day across the school, or one per professor?
