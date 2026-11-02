# Harbinger playthrough tests: design

Date: 2026-11-01. Scope: Harbinger path, brand-new player through Degree 4 and into Degree 5, with a heavy focus on
NPC usability. Unstained playthroughs and D6+ content are out of scope.

## 1. Intent

The existing journey (`gametest/journey/HemoJourney*`, `HarbingerJourneyAutomation`) reaches Apotheos but it is not
a playthrough. Each stage rebuilds a fixture, spawns NoAI invulnerable NPCs, posts `DialogueEvent` straight to the
bus, force-completes rank rites, links the Mortal Display to a Vicar, pre-lights braziers and inserts offerings with a
null player. It proves the state machine, not that a person can play it.

This suite plays the game as a person would:

- every step under test enters the server where a real client action enters it (right-click an NPC, choose an offered
  option through the dialogue payload handler, trade through the trade payload handler, use an item, hold a channel,
  place or right-click a block, click a menu slot, sleep in a bed, die from real damage and respawn, log out and in);
- only preconditions that are not under test are arranged by fixture, and every such line is labelled `// ARRANGE`;
- assertions are on what the player observes: inventory, degree, offered options and their lang keys, chat and
  action-bar lines, advancements, block state, ground drops near them, and the NPC sigil.

The suite also sweeps the edge cases a real server sees: wrong order, premature asks, repeat claims, full inventory,
Creative discard, death or relog mid-step, two players sharing an NPC or station, an NPC missing or killed, forged
packets.

## 2. Suite registration

- Namespace `playthrough_validation`. Every holder is `@GameTestHolder("playthrough_validation")` plus
  `@PrefixGameTestTemplate(false)`, template `empty`.
- Template: copy `src/gameTest/resources/data/npc_trade_validation/structure/empty.nbt` to
  `src/gameTest/resources/data/playthrough_validation/structure/empty.nbt`. Scenarios that need more room build at
  `HemoJourneyFixtures.findClearOrigin(player)` and clean up after themselves (see 3.6).
- Row in `gradle/game-tests.gradle` `ext.gameTestSuites`:

  ```groovy
  'playthrough_validation': [alpha: false, run: 'playthroughGameTestServer', directory: 'build/playthrough-gametest', reason: 'Long player-centric Harbinger D0-D5 playthroughs with real ceremonies and pending-decision probes; run on demand.'],
  ```

  `verifyGameTestSuites` enforces the row. `alpha: false` keeps the suite out of `alphaCheck`: rank-rite ceremonies
  tick for thousands of ticks and the `required = false` decision probes are expected to fail until the user rules.
- Run: `./gradlew.bat playthroughGameTestServer --offline` (runner agent only).
- Batches: `playthrough_<band>` for ordinary tests, `playthrough_<band>_clock` for anything that changes day time,
  weather or sleeps, so clock changes never overlap other tests.
- No new JVM source-text test is added, so `docs/architecture/evidence/source-text-impact-map.csv` is untouched. If a
  later writer adds one under `src/test`, regenerate the map with `python gradle/source-text-impact-map.py`.

## 3. Harness: `com.vincenthuto.hemomancy.gametest.playthrough`

One agent writes the harness before any band class exists. Band writers do not edit harness files. A band that needs
a new helper keeps a private one in its own class and records it for the harness owner to lift.

### 3.1 Reused, not duplicated

| Existing piece | Used for |
|---|---|
| `gametest/CapturedPlayer` | the connected player, captured payloads and chat. One additive factory is added: `CapturedPlayer.create(GameTestHelper, GameProfile)`, so a respawned or relogged player keeps the UUID. Nothing else in the file changes. |
| `NpcTradeGameTests#context` / `#open` / `#request` / `#topicTexts` (private) | copied once into `PayloadContexts` and `Dialogue` below; `NpcTradeGameTests` is left as it is |
| `gametest/PlayerFingerprints` (package-private) | exposed through a new public bridge `gametest/PlaythroughFingerprints` that delegates `saved`, `earned`, `diff`, `drops` |
| `gametest/LoomWeaveTestSupport.thread / weaveAll` | threading the engram, the one part of the Loom weave that is a client puzzle |
| `journey/HemoJourneyFixtures.findClearOrigin`, `prepareCardinalRite` | ARRANGE of rite floors and space (offerings then go in by the player) |
| `journey/HemoJourneySnapshot.capture / resetForJourney / restore` | the fresh-player baseline and cleanup |
| `SuccessionTestFixtures` | misbegotten and successor NPC variants |
| `BloodInjectionGameTests` / `PlayerMigrationGameTests` patterns | death clone (`restoreFrom` + `PlayerEvent.Clone`) and relog (`load(saveWithoutId)` + `PlayerLoggedInEvent`) |

### 3.2 `PlaythroughPlayer`

A wrapper over one `CapturedPlayer`. Every method that acts records an `Outcome` (3.4).

| Method | Real entry point it drives |
|---|---|
| `static create(helper, standAt)` | `CapturedPlayer.create(...).online()`, moved to `standAt`, survival, empty inventory, fresh `HemoJourneySnapshot.resetForJourney` baseline |
| `talkTo(npc)` -> `DialogueView` | `npc.interact(player, MAIN_HAND)` (reaches `mobInteract` with all its side effects), reads the last captured `OpenDialoguePacket.tree()` |
| `offeredEvents(npc)` | `talkTo` then `DialogueView.reachableEvents()` |
| `choose(npc, eventId)` -> `Outcome` | asserts `eventId` is reachable and enabled in the tree it was last shown, then mirrors `DialogueScreen#selectOption`: `DialogueRewardChoicePacket.handle(..)` when `DialogueRewardClaims.requiresAcknowledgement(eventId)`, otherwise `DialogueOptionPacket.handle(..)`, both through `PayloadContexts.of(player)` |
| `forge(eventId, entityId)` -> `Outcome` | the same payload handler with no offered-option check. Used only by negative tests (wrong state, entityId 0, wrong speaker, out of reach) |
| `openTopic(npc, topicId)` | `DialogueTopicOpenedPacket.handle`, which marks lore read so the sigil can be checked afterwards |
| `trade(npc, offerId, slots...)` -> `NpcTradeResultPacket` | `NpcTradeRequestPacket.handle` through the proxy context, after `NpcTradeRequestLimiter.logout` |
| `use(stack/hand)` | `player.gameMode.useItem(player, level, stack, hand)` |
| `useOn(pos[, face])` | `player.lookAt(EYES, centre)`, then `player.gameMode.useItemOn(...)`. It fires `RightClickBlock`, so `MachineAccessEvents` runs |
| `sneakUseOn(pos)` | the same with `setShiftKeyDown(true)` |
| `place(pos, item)` | puts the item in the hand and calls `useOn` against the face below |
| `attack(entity)` | `player.attack(entity)` (vial sampling, demonstration hits) |
| `hold(item, ticks, target)` | aims at the target, `startUsingItem`, then calls `onUseTick(level, player, stack, remaining)` once per real server tick through `helper.onEachTick`, then `stopUsingItem`. This is how Blood Absorption, Blood Projection, the Microscope and vial injection are channelled |
| `castSelected()` | `UseManipKeyPacket.handle(CAST)` through the proxy context (conjures the cell hand) |
| `select(manipulation)` | the payload behind the radial (`ChangeSelectedManipPacket`), or ARRANGE `known.setSelectedManip` when selection is not under test |
| `click(menu, slot, button, ClickType)` / `quickMove(menu, slot)` | `menu.clicked(...)` / `menu.quickMoveStack(...)` with `player.containerMenu = menu` |
| `pickUpNear(pos, item)` | `ItemEntity#playerTouch(player)` for each matching drop within 3 blocks |
| `sleepThroughNight(bedPos)` | sets night, `startSleepInBed(bedPos)`, then `stopSleepInBed(false, false)`. If `startSleepInBed` refuses because the captured player is not in the level, falls back to posting `PlayerWakeUpEvent(player, false, false)` and says so in the outcome |
| `leaveBed()` | `stopSleepInBed(false, true)` |
| `damage(amount)` / `die()` | `player.hurt(level.damageSources().generic(), amount)`; `die` uses more than max health |
| `respawn()` -> `PlaythroughPlayer` | a new `CapturedPlayer` with the same `GameProfile`, `restoreFrom(old, false)`, posts `PlayerEvent.Clone` and `PlayerRespawnEvent`, closes the old one. Root `getPersistentData` is dropped as it is in real play |
| `endReturn()` | the same with `restoreFrom(old, true)` |
| `relog()` -> `PlaythroughPlayer` | posts `PlayerLoggedOutEvent` for the old player, so `TransientServerStateEvents` and `EarlyInitiation.logout` run. Then a new same-profile player, `load(old.saveWithoutId(...))`, and `PlayerLoggedInEvent`, which runs `PlayerDataMigrations`, `PendingDelivery.onLogin`, `EarlyInitiation.login` and the sync table |
| `offline()` / `online()` | `CapturedPlayer.offline/online`, for "projector logged out during the delay" cases |
| `fillInventory(except)` | fills every main slot with unstackable filler and leaves the named slots free |
| `creative()` / `survival()` | `setGameMode` |
| `tickPlayer(n)` | posts `PlayerTickEvent.Pre/Post` n times, one per server tick. The captured player is not ticked by the level, so `EarlyInitiation`, `PendingDelivery` and `ChamberVisitService` need this |
| getters | `degree()`, `blood()`, `bloodActive()`, `count(item)`, `has(advancementPath)`, `knows(manipulation)`, `chatSince(mark)` |

### 3.3 `Dialogue` and `DialogueView`

- `DialogueView` flattens a served tree: start node, every node reachable through `nextNodeId`, including the hub's
  synthetic `__hub_*` nodes, and `presentation()` topics per `DialogueCategory` plus the Trade box.
- `reachableEvents()`, `option(eventId)`, `node(id)`, `category(eventId)` (QUESTS, CONVERSATION, INQUIRIES, TRADE) and
  `questState(eventId)`.
- `assertLangKeys(helper)` checks every node line, option text, prompt, topic title, line and teaser, and trade card
  key. A key passes when `Language.getInstance().has(key)` is true, or when it is in `/assets/hemomancy/lang/en_us.json`
  loaded once from the classpath (`LangKeys`). This is a classpath resource, not a repo read, so the
  `support/RepoPaths` rule is not involved.
- `assertNoDeadEnds()`: every `nextNodeId` resolves, and every node has a leave option or a continuation.
- `attention(npc, player)` returns `ProgressionDialogueNpc#progressionAttention`. Call it only after `talkTo`, because
  the read currently has a side effect (decision-free fix F-6).

### 3.4 `Outcome` and `PlaythroughLog`

`Outcome` is a before/after diff taken around one action:

- chat and action-bar lines added (strings, plus the translation key when the component is translatable);
- advancements earned and lost;
- inventory delta by item and components;
- `ItemEntity` drops added within 8 blocks of the player and of the NPC, with their position;
- degree before and after, blood before and after;
- the payloads sent (`DialogueRewardResultPacket.delivered`, ledger and inscription packets);
- the `DialogueEvent` returned by `dispatch`, with `wasRewardDelivered`.

Assertion helpers: `gave(item, n)`, `gaveNothing()`, `dropped(item, n, near)`, `said(key)`, `saidNothing()`,
`earned(path)`, `degreeIs(n)`, `unchanged()` (`PlaythroughFingerprints.diff` is empty apart from the ignored fields).

`PlaythroughLog.note(helper, scenario, text)` prints one line, `[playthrough] <scenario>: <text>`, to the server log.
Decision probes use it to report what was actually observed, so the runner can quote it whether the probe passes or
fails.

### 3.5 `PlaythroughNpcs` and `PlaythroughSites`

- `PlaythroughNpcs.hermit / vicar / alchemist / artificer / mnemonist / anchorite / voyager / votaryWayfarer
  (helper, pos)` spawn the real entity type with AI left on and no invulnerability override. They are placed on a
  floor with `restrictTo` home set the way the structure sets it, so the NPC behaves as it does in the world.
  `misbegotten(...)` and `successor(...)` reuse `SuccessionTestFixtures`.
- `PlaythroughSites.bloodTemple(helper, origin)` places `mortal_display` and `cardinal_focus`, spawns the Hermit and
  makes the three link calls `BloodTempleStructure#spawnHermit` makes. It never links to a Vicar.
- `PlaythroughSites.outpostWing(helper, origin, roles...)` gives each NPC a stand spot within 8 blocks of the player
  spot, and a far spot at 9 blocks.
- `PlaythroughSites.masonsRespite(helper, origin)` places the Respite scar station, the Mason's Effigy and the Iron
  Brazier at the offsets `tools/worldgen/masons_respite.py` uses, and spawns the Anchorite.
- `PlaythroughSites.riteFloor(player, origin, recipePath)` is ARRANGE: it wraps `HemoJourneyFixtures.prepareCardinalRite`
  for the floor and focus only. The medium, offerings, pillars and staff planting go in through `PlaythroughPlayer`.

### 3.6 `PlaythroughStates` (ARRANGE only)

Each state is the documented end state of the band before it, so a band test can start without replaying the whole
game. Each method lists exactly what it sets. It uses `DegreeProgression.advance` and the real grant helpers
(`KnownManipulationGrantHelper.grantDegreeOneUtilities`, `LiberKnowledgeHelper.unlockForDegree`), not
`setDegreeNumber`, wherever those exist.

- `freshD0(p)`: degree 0, blood inactive, slot 5 empty, no Hemomancy advancements.
- `charmBearerD0(p, hermit)`: the D0 claim done by fixture (blessing, charm, active blood, claimed hermit in both
  homes).
- `neophyteD1(p)`: the end state of initiation, including the starter kit and the four advancements.
- `votaryD2(p)`: the D1 claim's end state, including the advancement and the kit.
- `initiateD3(p)`: First Separation claimed, distill used, Concentrated Blood settled.
- `adeptD4(p)`: WOVEN_VESSEL complete and the D4 rank advancement.
- `illuminatusD5(p)`: VEIN_MASON complete and the continuation claimed.

One chain test per band (`*ChainFromPreviousBand`) instead runs the previous band's real Main steps, to catch drift
between a state fixture and real play.

### 3.7 `PlaythroughClock`

- `afterTicks(helper, n, check)` and `everyTick(helper, n, action, then)` are built on `helper.onEachTick` /
  `runAfterDelay`. Time is never jumped by editing game time.
- Session tickers that are not level-driven: `BloodInfusionManager.tick(level)`, `PendingBloodCraftManager.tick()`.
  These run once per server tick from `everyTick`, matching their real per-tick subscribers. Use them only when the
  level's own event does not reach them in a GameTest.
- `rite(player, focusPos)`: Living Staff use on the focus (`CardinalRiteActivation.tryStart(.., LIVING_STAFF_BLOCK_USE)`
  must return STARTED), then anchor feeding through `hold(blood_projection, ..)` and ordinary server ticks until
  `CardinalRiteSavedData` has no active rite. It never calls `CardinalRiteCompletion.complete`.

### 3.8 Cleanup

Every test closes its players (`try`/`finally` or `helper.onEachTick` succeed hook). It discards the NPCs it spawned
and calls `HemoJourneySnapshot.restore` when it used shared static state. Nothing writes outside the test structure
except at `findClearOrigin`, which is cleaned with `HemoJourneyFixtures.cleanup`.

## 4. Fix versus decision policy

- **Obvious fix (any writer or fixer may make it, then records it):** a crash or NPE; a missing lang key; a lost or
  duplicated reward; a wrong id; a missing speaker, proximity or alive check on a progression event; behaviour that
  contradicts `docs/HEMOMANCY_REFERENCE.md` or a topic doc where intent is clear; a softlock whose intended behaviour
  is documented; a raw enum or id shown to the player where a display name exists; a doc line that contradicts code
  whose intent is clear. A fix to an NPC line changes as little as possible and follows `docs/NPC_VOICES.md`. A new
  system (non-NPC) refusal message may be added with a new `message.hemomancy.*` key. Moving root `getPersistentData`
  claim keys follows AGENTS.md: a `PlayerDataMigrations` entry plus a `player_migration_validation` fixture.
- **Decision (the user's call; never change it):** balance, rates and costs; progression requirements or order; which
  NPC teaches or gives what; lore; UX redesign (where rewards land, reissue rules, consent flows); new NPC-voiced
  lines; anything where code and docs disagree without clear intent.
- Tests for a pending decision are `@GameTest(required = false)` with a `// DECISION: D-xx <one line>` comment. They
  assert the player-expected outcome and `PlaythroughLog.note` the observed one.
- Tests for an obvious fix are `required = true`. They assert the fixed behaviour, carry `// FIX: F-xx`, and are
  written before the fix.

### 4.1 Obvious fixes already identified (F-ids)

| Id | Fix |
|---|---|
| F-1 | `vicar_begin_initiation` and `vicar_release_charm` require a `HarbingerVicarEntity` speaker (add both to the `DialogueOptionPacket#dispatch` speaker list). A forged event with a founder's entity id currently initiates the player into that founder's bloodline. |
| F-2 | `EarlyInitiation.give` drops the remainder when `Inventory.add` leaves the stack non-empty. Partial inserts currently lose starter flasks. |
| F-3 | Progression events refuse `entityId == 0` and require their speaker type, through a table in `dispatch`. The events: `vicar_hermit_road_report`, `vicar_masons_respite_directive`, `give_stained_church_map`, `give_sanguine_monolith_blueprint`, `alchemist_first_separation_brief`, `alchemist_body_answers_brief`, `alchemist_red_taxonomy_*`, `alchemist_bestiary_*`, `mnemonist_reliquary_taught`, `mnemonist_anchorite_counsel`, `vein_mason_d5_diagnosis`, the D5/D6 reward events, the D6 referral and `vein_mason_replace_*`. Whisper events keep entityId 0. |
| F-4 | Artificer lesson, fork and D7 claim keys and the Mnemonist starter claim move from root persistent data into `PlayerPersisted`, with a migration and fixture. They are currently re-claimable after every death or End return. |
| F-5 | `CardinalRiteCompletionEffects.completeVascularMending` calls `VeinMasonAssignments.onTreatment(caster)`. The Mason prescribes Mending, but Mending never records the treatment. |
| F-6 | `DialogueHubFactory.decorate` stops setting `hemomancy_met_harbinger_hermit`; `HarbingerHermitEntity#mobInteract` sets it instead, under `PlayerPersisted`. Standing near a Hermit currently hides newcomer guidance, and death brings it back. |
| F-7 | Diagnosis chat passes translated section, flow and treatment names, not `.name()`. |
| F-8 | The Three Answers inspection message passes the fork family display key, not `serializedName()`. |
| F-9 | `DialogueRewards.hasItemCount` and its consume step include the offhand, so a Woven Vessel turn-in with the blank in the offhand is accepted. |
| F-10 | The Anchorite trees gain an `item_hint` node, so the 17 `dialogue_inquiry/cicatrix_anchorite` files can show. |
| F-11 | The Hermit Road lines stop promising a ledger, matching HEMOMANCY_REFERENCE:227: `hemomancy.vicar.hermit_road.report.line2`, `.report.prompt`, `.intro.line2`, `screen...step.ledger.desc` and the `hermit_road_ledger_granted` description. The advancement parent is fixed so the toast does not fire before its parent. |
| F-12 | Main-step options are filed as quests with a NOTICE sigil when actionable: the Vicar initiation offer, the Alchemist's First Separation and Concentrated Blood. The not-ready and already-claimed reward options stop showing as TURN_IN. |
| F-13 | `EarlyInitiationDialogue.alchemist` hides the Concentrated Blood option when the charm is not attached. A refused Concentrated injection says why, with a system message. |
| F-14 | `grantOrRecoverAssignmentLedger` recognises any carried ledger by item, not by item and components, so a renamed ledger is not re-minted. |
| F-15 | `NpcTradeService#serves` refuses a Hermit that is farewell-dying. |
| F-16 | Doc drift: the HEMOMANCY_REFERENCE Hermit table (the Vicar, not the Hermit, gives D1 blueprints and D2); the Cabinet at D1 (lessons start at D2); the Vicar Initiate row (outer halls only below D8); the inquiry speaker list and count. |
| F-17 | `hemomancy.vicar.item_inquiry.mortal_display.line2` says the vigil ends at the Hermit's farewell (a minimal edit in the Vicar's voice). |

Recorded but not in band: the tier-3 Vein-Mason catalyst is wrong (D6). Note it and do not fix it in this run.

## 5. Scenario catalogue

The classes are in `com.vincenthuto.hemomancy.gametest.playthrough`. H = happy chain, E = edge case, X = obvious-fix
regression (`required = true`), D = decision probe (`required = false`).

### 5.1 `d0_onboarding`: `PlaythroughOnboardingGameTests`

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| freshPlayerIsOfferedTheHermitsHeart | H | At the temple, talk to the Hermit with no blood. The tree reaches `what_heart` -> `accept_heart`; all keys exist; the Trade box shows `hermit/bread`; the sigil is NOTICE. |
| blessingThenDisplayTouchAwakensBlood | H | Choose `hermit_heart_offered`, then `useOn` the Display with an empty hand. Charm in slot 5, blood active, `the_first_awakening`, exactly one lodestone, `charm_equipped` said, display claimed. |
| hermitFarewellDissolvesTheHermit | H | Choose `hermit_accept_guidance`, then `hermit_farewell_die`. Both keys said; `talkTo` opens nothing while he dissolves; the Hermit is removed after 250 ticks. |
| lodestoneSaysNotFoundInTheTestWorld | H | `use` the lodestone. `.not_found` is said and the checked dimension is written. |
| vicarOffersInitiationToCharmBearer | H | Talk to the Vicar at D0 with the charm. `formal_introduction` offers accept and remove; no newcomer card; keys exist. |
| initiationCeremonyTicksToDegreeOne | H | Choose `vicar_begin_initiation`, then 200 real player ticks. D1; conduit, 4 flasks and ledger; both utilities known and equipped; Liber entries; the three advancements; Vicar AI restored. |
| displayBeforeBlessingIsRefused | E | `useOn` the Display first. `speak_with_this_temple_s_hermit` is said; no charm. |
| vicarFirstWithoutCharmShowsNewcomerCard | E | The Vicar shows the newcomer card and no initiation option. |
| ceremonyCancelledByDamage | E | `damage(1)` mid-ceremony. Still D0, charm on, no rewards, option offered again, and a second begin succeeds. |
| ceremonyCancelledByTeleportLogoutAndVicarLoss | E | Three sub-cases (a pearl move of more than 2 blocks, `relog`, `vicar.discard`). Each leaves the player D0 with no rewards and Vicar AI restored. |
| releaseCharmSpendsTempleButSecondTempleWorks | E | Choose `vicar_release_charm`. Slot 5 empty, blood inactive, `released` said; the first Display says already claimed; a second temple's claim plus initiation reaches D1. |
| deathBetweenBlessingAndTouch | E | Bless, `die`, `respawn`. The Display refuses; asking the Hermit again restores the blessing and the touch works. |
| deathAfterClaimKeepsInitiationOffer | X F-6 | Claim, `die`, `respawn`. The charm and active blood survive; the Vicar offers initiation and no newcomer card. |
| relogAfterClaimReactivatesBlood | E | Claim, `relog`. Blood is active and the initiation offer is still there. |
| twoPlayersOneTemple | E/D | P1 claims; P2 gets `already_claimed`. DECISION D-8: P2 re-asking the Hermit is told to take the heart. |
| twoPlayersOneVicar | E | P2's begin during P1's ceremony changes no state. A system refusal line is asserted (X F-13 style, new `message.hemomancy.*` key). |
| partialStackStarterFlasksAreNotLost | X F-2 | One slot holds 14 of 16 flasks and every other slot is full. After completion the player holds or has dropped all 4 new flasks. |
| creativeFullInventoryStarterKit | D D-16 | In Creative with a full inventory, the kit is lost and `initiation_blood_claimed` is still recorded. Logs the outcome. |
| forgedFounderInitiationIsRefused | X F-1 | `forge("vicar_begin_initiation", founder.getId())`: no ceremony, no bloodline, and the founder's projection is not swallowed. |
| forgedOnboardingEventsOutOfReach | E | Each D0 event sent from 9 blocks is dropped with no state change. |
| founderProjectionReceivesRecruit | H/D D-9 | A D5 founder (ARRANGE) holds Blood Projection on a charm-bearer for 200 ticks. D1, member of the line, `bloodline` said. DECISION: consent. |
| brokenMortalDisplayLeavesNoOath | D D-8 | Break the Display, pick it up, re-place it, use it: `no_living_oath`. Logs it. |
| wrongHermitClaimsAndSilentFarewell | D D-8 | A player who claimed temple A talks to Hermit B, who says "you have claimed my heart" and offers a farewell that does nothing. |

### 5.2 `d1_neophyte`: `PlaythroughNeophyteGameTests`

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| neophyteVicarOffersEveryD1Topic | H | The tree offers the hints, both blueprints, `first_bloodcraft_reward_unready` and the trade card; no second ledger; keys exist. |
| ledgerOpensAtZero | H | `use` the ledger. The packet carries degree 1 and zero progress, with the Hermit Road card at 0/2. |
| blueprintsDropOncePerTarget | H/E | Each blueprint drops one item at the Vicar with the right target; a second ask while it is carried drops nothing. |
| flaskBloodIsNotAbsorption | E | Drink a flask with `hold` through `finishUsingItem`. Volume rises; `absorbedMl` stays 0. |
| conjureAbsorptionNeedsEmptyHandAndBlood | E | `castSelected` with an item in hand is refused with its key; with 0 blood it is refused; after a flask it conjures and pays. |
| reservoirAbsorptionReachesFiveHundred | H | `hold` Blood Absorption on a filled reservoir (ARRANGE). `absorbedMl` caps at 500; the ticks taken are logged. |
| livingCowAbsorptionRate | D D-1 | `hold` on vanilla cows until 500 mL or a 6000-tick cap. Logs the cows and ticks used and asserts it is viable within the cap. |
| fullVesselAbsorptionCounts | D D-2 | A full player absorbs from a reservoir. Expects counted progress; logs 0. |
| venousStoneProjection | H | `hold` Projection on stone until it collapses; pick up the drop. Proof set; the ledger reads 1/2. Cobblestone does nothing. |
| cauldronFormationByContributor | H | Project into a cauldron, wait out condensation (retry on collapse), dispel, empty-hand `useOn`. +1 formation and the proof. |
| bystanderCollectingFormation | E | P2 collects P1's formation. P2 has no proof; P1 still lacks it; the logged note says the formation is gone. |
| hematicIronStructureCraft | H | Craft the paste and ash through menus or ARRANGE, place 8 ash by `place`, put ink in the offhand, `hold` Projection on the iron block. The block is placed, the catalyst used, the proof set. |
| fullChainClaimPromotesToD2 | H | Four proofs by real play, then the ledger reads ready. `choose` the claim (an acknowledged path): delivered true, D2, the advancement, and three drops at the Vicar picked up by `pickUpNear`. |
| prematureClaimWithZeroToThreeProofs | E | `forge` the claim at each proof count: `unready` said, D1, no drops; the tree shows the unready option. |
| repeatClaimRefused | E | At D2, the claim again at the same Vicar and at a second Vicar says `known`, gives no drops, and leaves D2. |
| claimFromNineBlocks | E | The claim at 9 blocks: `DialogueRewardResultPacket` delivered false and no state change. |
| proofsAtD0DoNotCount | E | A Liber crafted and blood absorbed at D0 leave the proofs unset after initiation. |
| proofLostWhenOfflineAtCollapse | D D-17 | `offline()` during the stone collapse: the stone drops but there is no proof. Logs it. |
| hermitRoadReportOnce | H/X F-11 | Read the remnant inscription, then report. 4 ash, the advancement, the card at 2/2; repeat says `known`; before the remnant it says `unproven`; no line promises a ledger. |
| hermitRoadAfterD2IsDeadEnd | D D-15 | A remnant read at D2 leaves the Side card at 1/2 with no report option. |
| deathMidBandKeepsProofs | E | Two proofs, `die`, `respawn`, talk to the Vicar. The proofs remain and the ledger is regranted once. |
| renamedLedgerNotReminted | X F-14 | A renamed ledger is carried; talking to the Vicar gives no second ledger. |
| secondPlayerTakesTheKit | D D-3 | P1 claims and P2 stands at the Vicar's feet and picks the kit up. Expects P1 to get it; logs who did. |

### 5.3 `d2_votary`: `PlaythroughVotaryGameTests`

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| votaryVicarPointsToTheAlchemist | H | The Vicar's D2 degree hint names the Alchemist and Concentrated Blood; keys exist. |
| firstSeparationBriefDeliversTwoVials | H | `choose` the brief: 2 vials and the advancement; the offer is gone afterwards. |
| centrifugeSpinRecoverAndClaim | H | Place the centrifuge, `attack` two cows, load opposite slots by `click`, `StartCentrifugeButtonPacket.start`, wait the spin, take the output by `click`, then `choose` the claim. The syringe and a rack of 8 come by insertion; the `next_distillation` key is said. |
| firstDistillationNeedsHeat | H | Place the Alembic over a campfire, insert the fungus by `click`, wait, `quickMove` the result. `distill` is used and the Concentrated topic appears. |
| concentratedTopicHiddenUntilReady | E | Before the claim, and again before the distill, the topic is absent. |
| injectAndSleepPromotesToD3 | H | `choose` Concentrated Blood, `hold` the vial until injected (pending), then `sleepThroughNight`: D3 and `awakened` said; the Alchemist moves to the initiate tree. |
| leaveBedDoesNotSettle | E | `leaveBed()` leaves the player D2 with pending still set. |
| relogAndEndReturnKeepPending | E | Pending survives `relog` and `endReturn`; one sleep gives exactly one D3. |
| wornVowDeathDoesNotPayAgain | X F-4 | Claim the Worn Vow scrap, `die`, `respawn`, talk to the Artificer: no inspection option, and a forged claim gives nothing. |
| mnemonistStarterDeathDoesNotReoffer | X F-4 | Claim the starter, stash the shard, `die`, `respawn`: the choice is not offered. |
| refusedConcentratedInjectionExplains | X F-13 | Inject a second vial while pending: a system message is said and the vial is kept. |
| concentratedTopicIsAQuestWithSigil | X F-12 | When ready, the option is in QUESTS and the sigil is NOTICE after the lore is read. |
| charmRemovedHidesConcentratedOption | X F-13 | With the charm removed (ARRANGE through the Vanity path), the option is not offered. |
| stashedConcentratedVialIsReissued | D D-19 | The vial is stashed in a chest and asked for again: a second vial is issued. Logs whether it can be injected. |
| outpostAlembicHasNoHeat | D D-12 | Place an Alembic on plain floor like the outpost's: it says "No heat". Expects it to run. |
| outpostOnlyStationsLeaveLedgerIncomplete | D D-13 | Using only stations the player did not place leaves First Separation at 7/8 after D3. |
| twoPlayersOneCentrifuge | D D-3 | P2 takes P1's tagged output. Logs that P1 loses the proof. |
| secondSpinOverwritesTheProof | D D-18 | Two assigned spins on two centrifuges: the first output no longer completes the step. |
| forgedAlchemistEventsRefused | X F-3 | The brief, body answers, taxonomy and bestiary events with entityId 0 or a Vicar speaker change nothing. |
| clinicalMicroscopeToCabinet | H | Microscope lesson with the sample held, `hold` the microscope 40 ticks, injection lesson, 3 sources, cabinet lesson: the recipes and advancements; the "next study" node advances. |
| redTaxonomyMainHandOnly | E | A sample in the offhand gives `missing_sample`; four main-hand turn-ins complete it and give 4 jars and 4 vials. |
| sleepNeedsTheWholeServer | D D-11 | With two players and the default sleep percentage, only one sleeps; no D3, and no hint tells the player. |

### 5.4 `d3_initiate`: `PlaythroughInitiateGameTests`

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| mnemonistWovenTreeAtD3 | H | The Woven Vessel is in QUESTS with NOTICE; loom, reliquary, scriptorium and chamber cards; guided chamber; no Circus; keys exist. |
| blankRecipeAwardedOnce | H/E | `choose` the blank recipe: the recipe is in the book; the second time delivers nothing. |
| wovenVesselTurnIn | H | Craft the stained stone and blank by `click` (the enzyme is ARRANGE), then `choose` the turn-in. Book, ink and 3 paper are consumed, the blank kept, the bulb and 3 Vivacious picked up, the advancement earned; a repeat says `known`. |
| turnInWithBlankInOffhand | X F-9 | The blank is in the offhand and is accepted. |
| loomRaisedByRealWorkingCraft | H | Floor (ARRANGE), subject blocks by `place`, braziers lit by the player, Staff planted, subject fed by `hold`. The Loom block entity exists and offerings are settled once. |
| firstBloodShotWeaveCompletesChapter | H | Insert by `useOn`, select with an empty hand, pay by `hold` Projection, thread, tick the climax. `memory_blood_shot` drops; first weave earned; WOVEN_VESSEL complete. |
| brotherhoodSealedWithoutWeave | E | The rite started without the weave: the sealed message names the Woven Vessel; nothing escrowed; the Staff kept. |
| brotherhoodCeremonyPromotesToD4 | H | Floor, pillars by `place`, powder in the brazier by `useOn`, then `PlaythroughClock.rite`. D4, the degree key said, the Staff returned. |
| weaveWithoutMnemonistUnlocksD4 | D D-6 | A crafted blank and the player's own bulb, never indexed, still give the chapter. Logs it. |
| weaverOfflineDuringClimax | D D-20 | `offline()` during the climax: the memory spawns but no milestone. Expects it on return; logs it. |
| vicarD3HintNamesTheRealGate | D D-7 | The Vicar's D3 degree hint mentions the Loom or Woven Vessel; logs the current line. |
| clairaudiographAfterEchoReferral | H | The Alchemist's echo referral, then the Mnemonist lesson: recipes, Liber pages and the advancement. |
| guidedChamberRestoresEverything | H | A crafting grid and a named cursor item, then `choose` the guided chamber, tick until return. Inventory, armour, grid and cursor are identical; guided complete; not attuned. |
| guidedChamberRefusedWithCursorItem | E | Cursor item held: `unavailable` said and the inventory is untouched; it can be retried. |
| guidedChamberLogoutAndLethalDamage | E | Logout mid-visit, and lethal damage inside, each restore the inventory and do not complete the lesson wrongly. |
| warpChairSilentRefusal | X F-13 | A sneak-use of the chair during an active visit says a system refusal. |
| reliquaryTaughtFromAfarRefused | X F-3 | `forge("mnemonist_reliquary_taught", 0)` grants nothing. |
| threeAnswersForkNameIsReadable | X F-8 | The inspection message shows the family's display name. |
| twoPlayersWovenVesselKit | D D-3 | P2 picks up P1's bulb and enzymes. |
| d3PuppetryHasNoPointer | D D-21 | A D3 player's Summons and Mnemonist offer no route to the Circus. Logs it. |
| deathMidBandKeepsMilestones | E | Weave, `die`, `respawn`: the milestones are kept and the rite can still start. |
| mausoleumOuterHallsTurnBackD3 | E | Walk the outer path to the threshold: pushed back with the ward line; a break is refused. |

### 5.5 `d4_adept`: `PlaythroughAdeptGameTests`

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| vicarDirectiveDropsTheMapOnce | H | `choose` the directive: the map dropped and picked up, the advancement; the next talk shows the follow-up node only. |
| mapNotFoundInTheTestWorld | H | `use` the map: `.not_found` said. |
| anchoriteLockedBelowD4 | E | At D3 the locked tree; a forged first lesson says `unready`. |
| firstLessonDropsTheKit | H | `choose` the first lesson: pattern, blank, catalyst and knapper picked up; advancement; Liber entry; a repeat says `known`. |
| supervisedCarveAtTheRespiteStation | H | `useOn` the station (MachineAccessEvents runs), fill it by `click`, set the stencil, `tryCraft`. The Scar is output and the knapper damaged. |
| scarLearnedAtLitBrazier | H | Insert the Scar by `useOn`, light with `hold` Projection (50 mL), `hold` Absorption (100 mL). The scar is known, blood is down 150, the advancement; supervised access ends. |
| effigyPreparationCreditsTheCharger | H | Open the Effigy, select (the payload body or an extracted service), place motif paper by `useOn`, `hold` Projection 500 mL. The pattern pops and the advancement goes to the charger. |
| commitLoadoutCompletesVeinMason | H | Pattern in the lit brazier, `hold` Absorption: the active scars match, `first_effigy_loadout`, VEIN_MASON. |
| templatePatternCommitIsNotProof | E | Committing the Mason's template gives `the_loadout_takes_but_the_vein` and no proof. |
| crimsonLodgeSealedWithoutLoadout | E | The sealed message names the Vein-Mason chapter. |
| crimsonLodgeCeremonyPromotesToD5 | H | The floor, 12 pillars, the bastion sigil socket, offerings by the player, then `PlaythroughClock.rite` through 3600 ticks: D5, the advancements. |
| continuationRewardOnce | H/E | Unready before the loadout; then one kit; then `known`. |
| wrongOrUnlitBrazierOffering | E | The template pattern in a lit brazier, or the Scar in an unlit one: no rite, logs that there was no feedback. |
| lostMapIsNotReissued | D D-4 | Discard the map and ask again: `known`, no map. |
| tendencyDriftDuplicatesPatterns | D D-14 | Shift alignment after the lesson: the carve refuses the given pattern and a free replacement is offered. |
| sharedEffigyChargeCredit | D D-3 | P1 pays 300 mL and P2 finishes: P2 gets the pattern and the advancement. |
| anchoriteItemInquiryReachable | X F-10 | Holding an inquiry item shows the Anchorite's inquiry node. |
| forgedMasonEventsRefused | X F-3 | Diagnosis, D5 reward, replace and the directive with entityId 0 or the wrong speaker do nothing. |
| d5MasonChainWithVascularMending | X F-5 | At D5 after the continuation: strain to Varicose (ARRANGE by health when the memory cast is not under test), diagnosis, Vascular Mending rite, fortification, reward: tier-2 kit. |
| diagnosisMessageIsReadable | X F-7 | The diagnosis chat contains no raw enum names. |
| preStrainedSectionNeverCounts | D D-22 | A section already Varicose at D4 never records the D5 strain. |
| d5MonolithCannotSpeak | D D-5 | A D5 player has no way to make the Monolith speak; the Vicar's blueprint is for D6. Logs it. |
| d5VicarStepsBack | H | At D5 the Vicar shows the illuminatus topics and the covenant hint; no Mason's Respite option; keys exist. |
| deathBetweenLearnAndCommit | E | Learn, `die`, `respawn`: the scar is still known, the uncommitted pattern is dropped where the player died, and the commit can be redone. |

### 5.6 `npc_usability`: `PlaythroughNpcUsabilityGameTests`

Sweeps run one GameTest per NPC so a failure names its speaker. Each sweep visits degree 0 to 5 crossed with blood
inactive or active (and for D0, charm or no charm), building the state through `PlaythroughStates`.

| Scenario | Kind | What it plays and asserts |
|---|---|---|
| langKeysSweep_<npc> | H | For Hermit, Vicar, Alchemist, Artificer, Mnemonist, Anchorite, Voyager and Votary Wayfarer at each state: `talkTo` opens a tree; `assertLangKeys`; `assertNoDeadEnds`. |
| offeredEventsRouteSweep_<npc> | H | Every offered event reaches a handler: the `DialogueEventRouter` claims it, and no "unhandled" warning is logged. |
| nothingOfferedIsDeadAtThatStateSweep | E | Choosing each offered reward or quest event gives a reward, a key said, or a node change. No silent no-op (catches wrong-Hermit farewell style bugs; each silent one is logged as a finding). |
| unofferedEventsRefusedSweep | E | Each progression event forged at every state where it is not offered: no reward, no degree change. |
| wrongSpeakerAndEntityZeroSweep | X F-3 | Every progression event, at a non-matching speaker and with entityId 0: nothing. |
| repeatClaimSweep | E | Every one-time claim chosen twice in one tick and again after a relog gives exactly one reward. |
| ackAndPlainDoubleSend | E | An acknowledged reward sent as both `DialogueRewardChoicePacket` and `DialogueOptionPacket` gives one reward. |
| deathReclaimSweep | X F-4 | Every one-time claim, then `die`/`respawn`, then the claim is not offered and a forged one gives nothing. |
| rewardLandingSurvey | D D-3 | Records, for every NPC reward, whether it lands in the inventory, at the player's feet or at the NPC's feet. Expects inventory or PendingDelivery. |
| attentionPointsToTheNextMainStep | H/X F-12 | Through the Main route, after reading lore, the NPC owning the next Main step shows NOTICE or URGENT and the others show NONE. |
| degreeHintMatchesTheChapterGate | D D-7 | At D1 to D4, the Vicar's degree hint names the milestone `HarbingerChapterMilestone.requiredForTargetDegree` returns. |
| newcomerGuidanceOnlyBeforeTheHermit | X F-6 | Outpost NPCs show the newcomer card at D0 until the player has talked to a Hermit, standing near one is not enough, and it survives death. |
| tradeBoxSweep_<npc> | H | At each state the Trade box opens; each card trades or refuses with a reason key that exists; rapport is per NPC; the tithe at 0 mL refuses cleanly. |
| dissolvingHermitRefusesTrade | X F-15 | A trade during the farewell is refused. |
| misbegottenAndSuccessorRefuse | E | Dialogue and trade are refused by a misbegotten NPC and a non-serving successor. |
| replacementNpcKeepsProgress | E | Kill an Alchemist mid-assignment (Creative attack) and spawn a new one: the assignment carries on. |
| uniqueNpcLossIsRecoverable | D D-10 | With the Anchorite killed, a D4 player has no route to VEIN_MASON. Logs it. |
| outpostSpawnsAllResidents | D D-23 | Place the outpost template; each resident spawns. Logs any skipped marker. |

## 6. Early decisions (D-ids)

| Id | Question | Options |
|---|---|---|
| D-1 | Living-creature absorption is 1 mL per 10-tick pulse, about 50 cows for the 500 mL proof. | keep / raise bare yield / lower the proof / steer the hint to reservoirs |
| D-2 | Absorption only counts the room below max; the hint says it counts everything. | count the absorbed blood even when full / keep and reword the hint |
| D-3 | NPC rewards drop at the NPC's feet, unowned (theft, despawn, lava). | inventory first then feet / PendingDelivery claims / set the owner on the drop / keep |
| D-4 | The Mason's Respite map (and the D0 lodestone) is never reissued. | reissue when not carried / a crafting recipe / keep |
| D-5 | The Monolith speaks from D5, but it is a D6 craft and the D5 Vicar gives its blueprint. | craft at D5 / speak from D6 and move the blueprint / an outpost Monolith |
| D-6 | WOVEN_VESSEL is satisfied by any Blood Shot weave; the Mnemonist index step is optional. | require the index / keep and reword the ledger |
| D-7 | The Vicar's D3 hint says "Bloodline system" and the D4 hint says "skill tree"; neither names the real gate. | rewrite in the Vicar's voice / keep |
| D-8 | Temple fragility: a breakable unlinkable Display, a killable Hermit, a spent temple after release, the Hermit blessing a spent temple, a wrong Hermit's misleading tree. | protect and relink / re-arm on release / new lines / keep |
| D-9 | Founder reception needs no recruit consent. | add a consent prompt / keep |
| D-10 | The unique Anchorite has no respawn path; the map can point at an empty Respite. | respawn rule / skip empty sites / keep |
| D-11 | D3 needs a server night skip; one awake player blocks it on multiplayer. | allow a personal rest / add a hint / keep |
| D-12 | The outpost Alembic has no heat source and no line says the base Alembic needs heat. | heat the template / add a hint / keep |
| D-13 | Using only outpost stations leaves First Separation at 7/8. | count use of any centrifuge / keep |
| D-14 | The Vein-Mason lesson is recomputed from live tendency (refused carve, free extra patterns). | persist the issued lesson / keep |
| D-15 | The Hermit Road report is D1-only; a remnant found later leaves 1/2. | allow the report at any degree / hide the card after D1 / keep |
| D-16 | Creative discard in `EarlyInitiation.give` still records the one-time starter claim. | treat Creative discard as not delivered (as PendingDelivery does) / keep |
| D-17 | A logout during the Venous Stone collapse or a pending structure craft loses the proof. | record by UUID / keep |
| D-18 | A second assigned spin overwrites the First Separation proof. | keep the first / keep the latest / keep both |
| D-19 | The Concentrated Blood and blueprint reissue checks only the carried inventory; the Monolith blueprint and church map have no limit at all. | once per claim / keep |
| D-20 | A weaver offline or dead during the climax loses the first-weave milestone. | grant at next login / keep |
| D-21 | D3 puppetry is gated on Circus instruction, but the Circus is hidden until D4 (the reference says it follows the Mnemonist). | point to the Circus at D3 / move the shapes to D4 / fix the doc |
| D-22 | The D5 strain needs a not-Varicose to Varicose change and the degradation config on; a pre-strained section never counts. | count an already Varicose section once / reword the Mason / keep |
| D-23 | An outpost NPC whose marker is obstructed silently fails to spawn. | a fallback spot or log / keep |

## 7. Order of work

1. The harness owner writes `playthrough/*`, the `CapturedPlayer` factory, the `PlaythroughFingerprints` bridge, the
   template and the catalogue row. No band class exists yet.
2. The band writers write their classes in parallel. Each writes only its own class and does not run Gradle.
3. The runner compiles and runs `playthroughGameTestServer --offline`, then reruns after collisions.
4. Fixers apply F-ids test-first, rerun the affected class, and update the topic docs and references they touch.
5. The final report lists the F-ids applied, the D-ids with the observed behaviour taken from `PlaythroughLog`, and any
   new findings.
