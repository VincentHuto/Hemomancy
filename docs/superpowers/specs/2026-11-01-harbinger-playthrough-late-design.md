# Harbinger playthrough tests, late game: design

Date: 2026-11-01. Scope: the second stage of the Harbinger path, from a fresh Degree 5 player
(`PlaythroughStates.illuminatusD5`) to Degree 8 on both paths: the bloodline and the Fane, the Brined Court, the
Crimson Troupe, D6 (Living Covenant), D7 (Archon, Monolith, Qliphoth Communion, the revelation choice) and D8
(Apotheos, Silentium, the Saint mausoleums). The focus is NPC usability: can a real player find, talk to, understand
and progress with every NPC. Unstained playthroughs are out of scope. Pending first-round decisions D-1 to D-34
(`docs/PLAYTHROUGH_TESTS.md`) are not re-decided here; a late case of the same pattern is logged under that id.

This is an extension of `docs/superpowers/specs/2026-11-01-harbinger-playthrough-tests-design.md`. Everything there
still holds: namespace, template, policy, harness rules, `// ARRANGE` labels, `PlaythroughLog` evidence.

## 1. Intent

The first suite ends on `illuminatusD5`. Past that point the only coverage is service-level (`CourtLife`,
`CourtHospitality`, `CircusPlayerProgress`), fixture-level (`HemoJourneyFixtures`, `SuccessionTestFixtures`) or
journey automation that posts `DialogueEvent` straight to the bus and force-completes rank rites
(`HarbingerJourneyAutomation` 629-675). None of it reads an NPC's tree, so lang keys, dead ends, hidden options and
pointers are unchecked, and none of it plays a Fane, a ledger join, a recruit, a Vigil helper, the Bloom or the
revelation choice the way a client does.

This round adds seven band classes and a late full run, in the same package and suite, that:

- play every late step under test through the entry point a client reaches (option payload, trade request, `useOn` and
  `use`, held projection and absorption, the Living Staff planted on a Focus, entity right-click through
  `PlayerInteractEvent.EntityInteract`, ledger and kick payloads, block-speaker dialogue, the whisper inbox, real damage
  then respawn, relog);
- arrange only what is not under test, labelled `// ARRANGE`, through new `PlaythroughStates` checkpoints that build
  on `illuminatusD5`;
- fail as `NOT RUN: <reason>` under `required = false` for anything the GameTest server cannot host (Chamber of Will,
  Fungal Gardens, seeded worldgen), so nothing passes silently.

## 2. Suite registration

- Same namespace `playthrough_validation`, same row in `gradle/game-tests.gradle` (no new row; update its `reason`
  text to "D0-D8" only if the runner touches the file anyway). Same `empty` template for most tests.
- Court and Circus site tests use the mod's own templates as the GameTest structure:
  `@GameTest(templateNamespace = "hemomancy", template = "crimson_troupe_pavilion" | "troupe_camp_<variant>" |
  "troupe_abandoned_stage" | "brined_court/tide_refuge/refuge" | "brined_court/lighthouse_garden/garden" ...)`.
  Each such test gets its own batch (65 x 14 x 65 for the pavilion). Where a site is several pieces with no single
  template (Vent Sanctum, Seats), the test uses `empty` plus `PlaythroughSites.structureSite` and
  `PlaythroughNpcs.court(...)`.
- Batches (one test per batch where marked "own"):

| Batch | Holds |
| --- | --- |
| `playthrough_d5_illuminatus` | ordinary D5 tests |
| `playthrough_d5_illuminatus_rites` | Bloodline Founding, Recall, Unbinding, Scarlet Summons, station-upgrade rites |
| `playthrough_d5_fane` (own) | every Founding Fane completion: it broadcasts to every player in the level and `FoundingFaneEvents` buffs reach any player in a 40-block lobe |
| `playthrough_brined_court`, `playthrough_brined_court_site` (own), `playthrough_brined_court_clock` (own) | dialogue only; synthetic StructureStart; game-time skips (Champion wait, rest, vacancy) |
| `playthrough_crimson_troupe`, `playthrough_crimson_troupe_pavilion` (own), `playthrough_crimson_troupe_clock` (own) | camps and dialogue; the pavilion; day time and difficulty |
| `playthrough_d6_sanctified`, `playthrough_d6_sanctified_rites` | |
| `playthrough_d7_archon`, `playthrough_d7_archon_rites`, `playthrough_d7_bloom` (own) | the Bloom aura and `QliphothBloomSavedData` are level-global, 3-chunk overlap refusal |
| `playthrough_d8_endgame`, `playthrough_d8_endgame_rites`, `playthrough_d8_mausoleum` (own) | |
| `playthrough_npc_late` | sweeps |
| `playthrough_late_run` (own) | the two late full runs |

- No JVM source-text test is added, so the source-text impact map is untouched.

## 3. Harness extension

Ownership as in round one: one harness agent writes every change below before any band class exists. Band writers do
not edit harness files; a band that needs a missing helper keeps a private one and records it for the harness owner.

### 3.1 Reused as is

`PlaythroughPlayer` (`talkTo`, `choose`, `forge*`, `openTopic`, `trade`, `use`, `useOn`, `sneakUseOn`, `place`,
`attack`, `hold*`, `castSelected`, `select`, `click`, `quickMove`, `pickUpNear`, `die`/`respawn`/`relog`/`offline`,
`fillInventory`, `creative`, `tickPlayer`, readers), `Outcome`, `DialogueView`, `PlaythroughClock` (`segment`, `until`,
`rite`), `PlaythroughSites.riteFloor`/`outpostWing`/`masonsRespite`/`clearForNextBand`/`keepTicking`,
`PlaythroughNpcs` (all Harbinger roles, `successor`, `misbegotten`, `outpostKey`), `PlaythroughCleanup`,
`PayloadContexts`, `LangKeys`. Outside the package: `MausoleumAssembly` (layout and build), `CourtLifeGameTests.FakeSite`
(lifted, see 3.5), `HemoJourneyFixtures.findClearOrigin` and `prepareQliphothCommunion`, `SuccessionTestFixtures`,
`MorphlingPowerGameTests.stack`, `VagrantMindActivationGameTests` End fixture (lines 46-70).

### 3.2 `PlaythroughRites` (new; lifted)

The private rite helpers in `PlaythroughAdeptGameTests` (lines ~1870-2000: `riteStand`, `structureCells`,
`buildStructure`, `build`, `offerings`, `usedSockets`, `braziers`, `besideSocket`, `supportSigilNodes`) move here as
public statics; the Adept class keeps one-line delegates (only the harness owner edits it). Added:

- `seatMedium(player, floor, ItemLike medium)`: a real `useItemOn` on the Cardinal Focus with the medium held
  (Sanguine Quintessence, Qliphoth Seed, Sanguine Formation, the Marionette Crossbar).
- `prepare(clock, player, floor)`: `buildStructure` (pillar columns for `sanctified_rite`, `archon_rite`,
  `founding_fane`, `apotheos_rite`) + `braziers` with the recipe's offerings + `seatMedium` when the recipe has one.
- `RitePlan` record and `PlaythroughClock.rite(player, focus, RitePlan)`: `maxTicks`, `extraAim` (default
  `supportSigilNodes`), `plant`, `pauseWhen`, and three new hooks:
  - `fightThreats`: while a wave is live, each tick the player attacks the nearest live entry of
    `rite.getRiteThreats()` with attack-strength reset, and refills drained anchors (Bloodlicker siphon) before the
    daemon;
  - `releaseOnStill`: let go of projection during `STILL_INTERVAL` phases (Covenant Vigil);
  - `onPhase(Phase, BiConsumer<ActiveCardinalRite, Integer>)`: a hook run each tick in a phase, used at
    `INSCRIPTION` to assign helpers (`interactOn` the recruit, or a second player's `useOn` on the station marker).
  - `trialPuppet`: for `RiteKind.PUPPETEER_TRIAL`, attack the manifested trial body until defeated.
- `workingSubject(player, floor, recipePath)`: places a Working craft's subject blocks with real placement (the
  Sanguine Monolith BlockItem lays its fillers; the Covenant Throne subject takes a placed Monolith).
- Default timeouts: lesser 6000, greater 9000, grand 14000 ticks.

### 3.3 `PlaythroughPlayer` additions

| Method | Goes through |
| --- | --- |
| `interactOn(Entity[, hand])` | `ServerPlayer.interactOn`, so `PlayerInteractEvent.EntityInteract` fires (`CardinalRiteInteractionHandler.onRightClickEntity` helper assignment, Bloom Warden amends, Specimen Jar, carousel captives). Returns an `Outcome`; does not require a dialogue. |
| `talkToBlock(BlockPos)` | empty-hand `useOn`; captures the `OpenDialoguePacket` into a `DialogueView` keyed by the block (entityId `SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID`). |
| `chooseAtBlock(BlockPos, eventId)` | `DialogueRewardChoicePacket(eventId, -1)` when `DialogueRewardClaims.requiresAcknowledgement`, else `DialogueOptionPacket(eventId, -1)`; only when offered. |
| `whisper()`, `openWhisper()`, `chooseWhisper(eventId)`, `closeWhisper(id)`, `pendingWhispers()` | the last `OpenDialoguePacket`/`OpenPendingWhisperPacket` with entityId 0; `PendingWhispers.open` as `/hemowhisper` does; `DialogueOptionPacket(eventId, 0)`; `ClosePendingWhisperPacket.handle`. |
| `ledgerAction(int)` | `PacketLedgerAction.handle` with `PayloadContexts`, the ledger held. |
| `kick(UUID)` | `PacketKickBloodlinePlayer.handle`. |
| `clickButton(menu, id)` | `menu.clickMenuButton` as `ServerboundContainerButtonClickPacket` does (Scriptorium rows, Spindle prepare). |
| `crossbarCallOrRecall()`, `radial(action, value)`, `spindlePrepare(summon)` | `MarionetteCrossbarItem#use` then `releaseUsing` under 10 ticks; `PacketCrossbarRadialAction.handle`; `PacketPuppeteersSpindleAction.handle`. |
| `consume(ItemLike)` | holds use for the item's use duration so `finishUsingItem` runs (pomes, tonic). |
| `breakBlock(pos)`, `leftClickBlock(pos)` | `gameMode.destroyBlock` (BreakEvent); `LeftClickBlock` START. |
| `joinLevel()` | opt-in: adds the player to `PlayerList.getPlayers()` and the `ServerLevel` player list (reflection in `CapturedPlayer`, one additive method), removed on `offline`, `relog` and cleanup; never ticked twice by the server. Needed wherever code finds players by `level.getPlayerByUUID`, `getNearestPlayer`, `getEntitiesOfClass` or `sLevel.getPlayers()`: Circus practicals, carousel, puppets (`BoundSummonBehavior.ownerFor`), kick/disband sync, Fane broadcast, `Bloodline.getPlayers`, Marimo first sight, the Wake nursery launch. Tests that do not need it do not call it. |
| `moveTo(ServerLevel, pos)` | same-UUID dimension change (End for the Vagrant Mind). |
| `owed(PendingDelivery.Claim)` | `claim.owed()` for `FUNGAL_SPINE`, `VesperOrdealManager.MEMORY`, `MycophantEncounterManager.TENDRIL`, the First Separation claims. |
| `ledger()` | uses the Assignment Ledger and returns the captured `OpenHarbingerAssignmentLedgerPacket`; `LedgerView` (new, harness only) decodes it and computes the Blood Beneath progress and key the way `HarbingerAssignmentLedgerScreen` 1504-1530 does. No main-code move. |

### 3.4 `PlaythroughStates` checkpoints (ARRANGE only, each on the one before)

`foundedLineD5(p)` (registered line, leader, `hasFoundedBloodline`, `ADV_BLOOD_IS_BOUND`, a signed ledger as
`presignBloodlineLedger` makes it), `memberOf(p, leader)`, `covenantD5(p, heartPos)` (Bloodwell block at
Focus+3, `FoundingFaneSavedData.consecrateHeart`, both Fane advancements; public port of
`HemoJourneyFixtures#prepareFoundingFane`), `recruited(p, npc)` (`BloodlineSavedData.addNpcMember` with type and
outpost key), `sanctifiedD6(p)`, `livingCovenantD6(p)` (`ADV_CHAMBER_RETURNED` is ARRANGE because the Chamber is not
loaded; Throne and Vigil advancements), `archonD7(p)`, `communedD7(p)` (Bloom via `prepareQliphothCommunion`, nine pomes
recorded from it, Communion done, Spine delivered by a real `PendingDelivery` tick), `choicePendingD7(p)` (witnessed +
`REVELATION_CHOICE_PENDING`), `silentPendingD7`, `apotheosPendingD7`, `vesperBeatenD7` (advancement, sever then seal,
`hemomancy:vesper_memory_pending`), `apotheosD8`, `silentiumD8`; Court: `courtKnown(p, route)`,
`courtStanding(p, house, standing)`; Circus: `circusKnownShape(p, shape)`, `acclimated(p, score)`,
`pavilionRoute(p, route, mask)`. Each registers its saved-data cleanup. A `Checkpoint` enum names the late states,
and `assertAt(helper, player, Checkpoint)` checks the facts each one sets (used by the late run, section 6).

### 3.5 Sites and NPCs

- `PlaythroughSites.structureSite(helper, ResourceKey<Structure>, BoundingBox)`: the synthetic StructureStart and
  reference, restored at cleanup; `CourtLifeGameTests.FakeSite` and `PlaythroughInitiateGameTests` ~1936 delegate to it
  (harness owner only). `courtSite(helper, CourtSite, centre)` and `pavilion(helper)`, `camp(helper, variant)`,
  `abandonedStage(helper)` wrap it for the real templates.
- `fane(helper, origin)` (ARRANGE Fane on the empty template), `faneFloor(player, origin)` (the founding_fane floor
  for the played rite, heart cell clear 3 above the Focus), `monolith(helper, pos)` (placed through the BlockItem),
  `covenantThrone(helper, pos)`, `qliphothBloom(owner)`, `ironBrazier(pos, lit)`, `morphicNectarPool(box)`,
  `gardensBiome(box)` (biome edit with cleanup), `mausoleum(helper, saint)` (MausoleumAssembly with ward anchor,
  Chapels, ring door, tomb), `stationBehindFocus(floor, UpgradeStation)` for station-upgrade rites.
- `PlaythroughNpcs.court(helper, CourtRole, pos, yaw, mark)` through `CourtResidentSpawner.place`;
  `circus(helper, CircusRole, pos)` for the Ringmaster, eight performers and the carousel when not template-placed;
  `brinedVotary(helper, pos)`; `ownedMycophant(helper, owner)` (ACTIVE flag, return point, intro skipped);
  `outpostKey(helper, n)` stamps distinct outpost keys so type and outpost limits can be tested with two outposts;
  `kill(npc)` (labelled ARRANGE, out-of-world damage) for unique-NPC loss.

### 3.6 `PlaythroughNotRun` (new)

`require(helper, ResourceKey<Level>, reason)` and `notRun(helper, reason)`: fail with `NOT RUN: <reason>` and log the
proof owed (`chamberReviewClient`, `heartNodeReviewClient`, `vesperReviewClient`, `mycophantReviewClient`,
`possessionReviewClient`, `court_worldgen_validation`, `brinedSeatReviewClient`). Every such test is
`required = false`. A test that runs a degraded form on this server (the first Spine projection resolving in one tick)
says so in its log line and is not a `NOT RUN`.

### 3.7 `DialogueView`, `Outcome`, cleanup, time

- `DialogueView.unreachableNodes()` (nodes no option points to), `assertEveryRootOptionHasACard()` (root options added
  after `DialogueHubFactory.decorate` are not drawn by the hub screen), `emptiedNodes()` (non-terminal nodes left with
  no options).
- `Outcome.ignoringBroadcasts(keys...)` so parallel tests' `didNotSay` stays valid; `whisperQueued(key)`;
  `droppedNear(BlockPos, item)`.
- `PlaythroughCleanup` hooks: disband test-owned bloodlines; remove test-owned Fanes, stakes and hearts; clear
  `CardinalRiteSavedData` recovery, `EarlyInitiation` ACTIVE, `QliphothBloomSavedData`, `CircusPavilionSavedData`
  sites in bounds, the root circus active-challenge keys; restore game time, day time and difficulty.
- `PlaythroughTime.skipGameTime(helper, ticks)` / `setDayTime` / `difficulty(...)`, each restored; own batches only.

## 4. Fix versus decision policy

As in round one, section 4. Late obvious fixes get `LF-n` ids (not `F-n`, which collide with round one and the
consolidation ledger). A fix test is `required = true` with `// FIX: LF-n`, written first. A decision probe is
`required = false` with `// DECISION <band-id>: <question> Options: ...`, asserts what a player would expect and logs
what happens. A late instance of a pending round-one pattern (D-3 unowned drops, D-16 Creative discard, D-27 repeat
handouts, D-28 silent refusals, D-10 unique NPC loss, D-20 offline at completion) is a `required = false` probe tagged
with that D-id and is not given a new id. Pending architecture decisions (F-11, S-1, S-5b, A-4, G-2b, F-16) are the
user's: probe, do not change.

### 4.1 Late obvious fixes (LF-ids)

| Id | Fix | Why it is clear |
| --- | --- | --- |
| LF-1 | `recruit_harbinger` and `expel_harbinger` require a recruitable Vicar, Alchemist or Mnemonist (`DialogueEventSpeakers` rows plus an instanceof check in `RecruitmentDialogueEvents`), and recruit requires Degree 5. | HEMOMANCY_REFERENCE §12: "Degree 5+ ... recruitable NPCs are Vicar, Alchemist, and Mnemonist". A forged option now recruits a cow, a Court member or a player for +1000 mL. |
| LF-2 | Rite of Bloodline Founding refuses a caster already in a registered line (new `message.hemomancy.cardinal_rite_completion_effects.*` key, activation refusal); a signed ledger refuses a player already in another line (new `message.hemomancy.unsigned_ledger_item.*` key). | Double membership makes `getBloodlineForPlayer` pick a line by HashMap order; `EarlyInitiation.canJoin` already enforces one line. |
| LF-3 | `withCovenantGuidance` reads "in a registered bloodline" (not "leader") and checks the covenant proof first, so a D5 member is told `found_fane` (deposit) or `perform_bloodline_rite`, never `found_bloodline`. Same in the ledger (`HarbingerAssignmentLedgerItem` 59-60). | The shipped `found_fane` line already addresses members; HR §5.7. |
| LF-4 | `StationUpgradeProgress.deliver` skips dead players and keeps the kit pending while the player is dead. (The Creative-discard half is D-16, logged only.) | AGENTS.md pending-delivery rule: living player. A full-pack claim then death loses the Quill, Harness, Governor, Kit or Cornerstone. |
| LF-5 | The revelation choice survives death: `FungalWhisperDialogueEvents`, `FungalGardenTravelHelper.handleTravelUse` and `FungalProjectionEvents.onPlayerLogin` treat "witnessed, path NONE, Communion done, nine pomes" as choice pending. No key moves (the root key belongs to S-1). | A death after the forced return leaves both D8 routes sealed and every power locked; AGENTS.md: pending state survives death cloning. |
| LF-6 | The first Mycophant Tendril queued on a full pack says so: new key `message.hemomancy.mycophant.make_room`, as Vesper's Memory does. | Missing text with a direct twin. |
| LF-7 | Guest-act payment uses a separate `hemomancy.circus.school.reward.delivered` key when nothing is set aside. | Every full payment now reads "0 Filaments are set aside". |
| LF-8 | The Threadkeeper's claim, inspect and report cards are Turn In / Urgent only when something can be handed in. | Round-one rule (F-12): a card is Turn In only when something can be handed in. |
| LF-9 | A spectator act owner earns nothing: `CircusPerformanceController.onPlayerTick` fails the act for a spectator. | HR §17.1 and CIRCUS.md rulings: spectators earn nothing. |
| LF-10 | `CircusPerformanceController.armAct` filters by `participatesInFinale`, so pavilion faculty stay out of act 2. | CIRCUS.md: new faculty do not join the original cast. |
| LF-11 | Circus school refusals get system lines where they are silent: practical below the shape's degree, out of range, professor busy, no valid Crossbar; inspect below D3; claim with nothing pending; teach success; route chosen and repaired (new `message.hemomancy.circus.*` keys). | Missing text; system lines are allowed by policy. Not reward claims, so not D-28. |
| LF-12 | The Rite of the Chamber of Will says why when the visit cannot start (new `message.hemomancy.chamber.*` key). Attunement is unchanged (see D6-5). | A completed rite with no line and no visit. |
| LF-13 | The Head-tender's challenge refusal checks the retry wait before the strain, so after a lost fight the player is told about the wait, not to bring a strain the death moved into the Jar. | The `challenge.refused_wait` line exists for this case. |
| LF-14 | `Bloodline.removeMember` clamps the pool to the new maximum, as the recruit-death path does. Fix only if the kick test shows the pool above its maximum. | Consistency with `BloodVolumeEvents.ordinaryRecruitDied`. |
| LF-15 | The Succession explain, bequest and donate options get hub cards (decorate before `DialogueHubFactory.decorate`, or add cards), and a successor's trimmed recruit and release cards are removed with their nodes. | The options exist to be chosen; the hub screen never draws root options, so they are hidden. |
| LF-16 | The Head-tender's `court_member_challenge` gets its `dialogue_route_validation` fingerprint row. | BRINED_COURT.md lists it as proof owed. |
| LF-17 | Doc drift, code authoritative: HR §12 (only the rite founds a line; `DialogueEventRouter`/`RecruitmentDialogueEvents`), §5.7 (the rite creates the Bloodwell at Focus+3; stakes are a D5 floorless Blood Structure), §5.2 and §5.9 (gates in `CardinalRiteActivation` and `CardinalRiteCompletionEffects.commonRefusal`; no `SHATTER_INTERACTION_THRESHOLD`, two dialogue choices; post-shatter and post-bloom whispers only while the memo is unknown; unbound pomes never count), §5.4 (Hermit D6 hint only from an unspent temple), CIRCUS.md (+50 acclimation on the first talk to each of the 8 roles; spectacle banking is per player: 4 for Succession, 1 for Liberation). | Code is authoritative where intent is clear. |

Not fixed, logged: the bloodline name is concatenated English (`CardinalRiteCompletionEffects` 979) and stored in saved
data, so a key needs a storage change (NPC-6); vanilla particles in the Severance cut (`LivingSicklePruning` 61-64,
a visual pass, not this run); the PacketLedgerAction and PacketKickBloodlinePlayer gameplay inside `common/network`
(architecture, check `NetworkPackageGuardrailTest`, not this run).

## 5. Scenario catalogue

H = happy chain, E = edge, X = fix regression (`required = true`), D = decision probe, N = NOT RUN probe
(`required = false`). Every band class exposes `static void chain(helper, player, clock)` for the late run.

### 5.1 `d5_illuminatus`: `PlaythroughIlluminatusGameTests`

| Test | Kind | What |
| --- | --- | --- |
| `foundBloodlineByRite` | H | communion_lesser floor, blaze powder, Staff planted, anchors, Bastion, Bloodlicker wave fought, seal. Signed ledger dropped at the Focus, `you_have_founded`, `blood_is_bound`; the Vicar's hint moves to `found_fane`. |
| `foundingAgainIsRefused` | X LF-2 | a leader and a member plant Bloodline Founding: refused with the new key; no second line. |
| `ledgerJoinBySecondPlayer` | H | B uses A's signed ledger (`joinLevel` both): `you_have_joined`, pool max +5,000, A told `has_joined_your_bloodline`; B's Vicar shows the pledge option. |
| `ledgerJoinWhileInAnotherLineRefused` | X LF-2 | |
| `ledgerJoinHasNoGate` | D D5-1 | a D0 and an inactive-blood player join by ledger. |
| `foundFaneByRite` | H | dominion_grand + pillars, nautilus shell, Quintessence seated, Staff, wave, seal. Bloodwell at Focus+3, both advancements, broadcast (own batch). |
| `faneRefusals` | E | no line, member caster, first relocation planting warns, second inside the window relocates and removes old stakes, another Fane's ground. |
| `memberDepositEarnsCovenant` | H | B at D5 inside A's Fane holds projection on the Bloodwell: `covenant_written_in_place`; an observer, an outsider (`not_bloodline`), a D4 member, a dead or spectating member and inactive blood get nothing. |
| `fullPoolDepositSaysNothing` | D D5-8 | |
| `vicarGuidesMembersToTheirFane` | X LF-3 | member without proof hears `found_fane`, with proof `perform_bloodline_rite`. |
| `recruitAndExpelAtOutposts` | H | Vicar, Alchemist, Mnemonist at two outposts: success, pool, type and outpost limits, release card, non-leader expel `not_leader`, recruit death frees the slot (ARRANGE kill). |
| `forgedRecruitIsRefused` | X LF-1 | cow, Artificer, Anchorite, Court member, Circus performer, a player, a D4 member. |
| `ledgerSummonRecallDisband` | H | summon inside and outside the Fane, no recruits, set point in and out of range, recall, disband (ledgers burnt, members reset, Fane removed). |
| `kickAndRelog` | E | kick, online and offline member, relog reconciles; pool not above max (LF-14). |
| `kickedMemberStaleLedger` | D D5-3 | the kicked player uses the ledger they joined with: no action, no line. |
| `bloodlineRecallAndMemberPowers` | D D5-2 | a member mints a signed ledger by Recall and recruits; cannot expel, set point or disband; there is no leave. |
| `hematicUnbindingCostsTwoStars` | D D5-4 | first completion consumes the star and only warns; Disband is free. |
| `sanctifiedSealedBeforeCovenant` | E | sanctified_rite planted before the proof: `the_rank_rite_remains_sealed_complete` naming the chapter. |
| `hematicStakeExtendsFane` | E | stake formation in chain, out of chain `invalid`, non-owner break `not_owner`, heart broken collapses all (F-11/D-34 timing noted). |
| `stationKitsAtD5` | H | Quill after an enchant (Scriptorium button), Harness after a weave, Governor and Consecration Kit from the Artificer, Druse Heart from the Vicar; Eightfold Script rite; kit with a full pack delivered on the 20-tick pass. |
| `stationKitHeldWhileDead` | X LF-4 | full-pack claim, death, respawn: kit delivered once. Creative half logged as D-16. |
| `crimsonVestmentAndAssumedLimb` | H | brief, own consecration, inspect, Alchemist correspondence, Blood Lust set, demonstration hit, fitting; Assumed Limb graft, claim, arsenal kill, seven forms. Wrong-speaker correspondence logged. |
| `alchemistCommissions` | E | Deep Dark `bearing_missing` on the flat world, Basin bearing (own batch, slow search), reports, main-hand rule (D5-7 probe). |
| `monolithAtD5` | D D5-6 | ARRANGE a Monolith: D5 tree has no guidance, held item hides the tree. |

### 5.2 `brined_court`: `PlaythroughBrinedCourtGameTests`

| Test | Kind | What |
| --- | --- | --- |
| `courtDiscoveryRoutes` | H | Vicar sea topic, Voyager learn and chart (once, reissued after loss); Votary letter by a real kill, read at the Vicar; Alchemist lead with a worn Mature strain. Each first route kept. |
| `alchemistLeadNeedsAWornStrain` | E | strain in a jar or offhand: no lead. |
| `subD5LetterCarrierAtVoyager` | D BC-11 | the shown Garden option records and says nothing. |
| `relayChartFindsNothing` | E | `searching` then `not_found` on the flat world (`PlaythroughClock.until`). |
| `relayChartFindsAGarden` | N | needs a seeded Pelagic world. |
| `siteVisitDiscoversAndMakesGuest` | H | synthetic Tide Refuge start: `court.discovery`, guest of the house; dead and spectator record nothing. |
| `gardenerHospitality` | H | food when hungry, rest, gift once, rest again inside a minute (`rest.recent`, D-28 family logged), gift hidden. |
| `customByDialogueAndByTrade` | E | one standing step either path; repeat consumes and raises nothing. |
| `tradeCardPaysRapportDialogueDoesNot` | D NPC-2 | |
| `customBeforeVisitLocksGuest` | D BC-1 | friend becomes unreachable, so the Champion closes. |
| `lightkeeperLessonAndBonding` | H | D4 `lesson_early`, D5 taught, x1.5 bonding only in the strain's own house site. |
| `joinSwitchAndThirdChange` | H | join at guest, switch (`membership.switched`), third refused, stranger and D4 refused. |
| `switchSpendsTheOnlyMoveWithoutWarning` | D BC-3 | |
| `innerHouseSweep` | E | each house: visit, custom item, gift, join role; the Sleeper. |
| `transgressionWardenAndAmends` | H | break Garden growth, reach for the undercroft, hit a Gardener: one Warden per act, turned away, amends by food on the Warden (`interactOn`) and at the Gardener after a death. |
| `kindledChampionWinAndLoss` | H | gate refusals, challenge, win (title, crown with a hydra_crown), loss then the day's wait across death (own clock batch). |
| `championRefusalNamesTheWait` | X LF-13 | |
| `usurpationEscapedByLogout` | D BC-2 | logout or 64 blocks away ends it with no wait. |
| `challengeStartsWithoutConfirm` | D BC-4 | |
| `courtPointersPastTheGarden` | D BC-6 | nothing points to the Vent Sanctum or the outposts (also BC-5 Wake nursery, BC-9 Pelagic off, BC-10 nothing past D5 or Primal: one probe each, logged). |
| `lightkeeperLessonPointsAtIncubator` | D BC-7 | |
| `championHasNoDegreeGate` | D BC-8 | a D4 with a shared Incubator's Apex strain. |
| `hydraPresenceOnce` | E | first approach raises the head once; Marimo touch chorus. |
| `marimoFirstSight` | N | `livingNear` needs the level-joined player; NOT RUN unless `joinLevel` proves it. |
| `courtMemberSweep` | E | 18 roles x stranger/known/guest/member/transgressor x D4/D5: lang keys, dead ends, offered events route; `greet.transgressor` skipped for non-Giving-Light roles. |
| `primalizeOnlyForApotheos` | E | Apex strain in Morphic Nectar near a D8 Apotheos, refused for Silentium and below Apex. |

### 5.3 `crimson_troupe`: `PlaythroughCrimsonTroupeGameTests`

All pavilion tests `joinLevel()` and run on the real template in their own batch.

| Test | Kind | What |
| --- | --- | --- |
| `d5ReferralAndWaybill` | H | Vicar card, Mnemonist waybill once, hidden while carried, reissued after loss. |
| `waybillFindsNothing` | E | `not_found` on the flat world. |
| `waybillLocatesACamp` | N | seeded worldgen. |
| `pavilionDiscoveryAndAcclimation` | H | discovery once, +1 per 80 ticks, dead and spectator nothing, a camp is not discovery. |
| `firstTalkMilestones` | E | +50 once per role (LF-17 doc). |
| `crossbarAttuneChargePrepare` | H | bind, Spindle charge, prepare; foreign Crossbar refused. |
| `threadkeeperInspect` | E | inspected; `inspect_failed`; below D3 now says why (LF-11). |
| `understudyMnemonistPuppet` | H | D5 lesson, `puppeteer_trial_mnemonist_puppet` on a Dominion Lesser floor (Crossbar as medium, 1400 mL recorded), practical replay; refusals `instruction_required`, `already_known`, `crossbar_active`, `no_blood`. |
| `emptyRingReport` | H | call, command, recall, report once (+100). |
| `guestActPaymentLines` | X LF-7 | room, full pack (pending exact), same day refused, claim at a Threadkeeper. |
| `threadkeeperCardsOnlyWhenOwed` | X LF-8 | |
| `schoolRefusalsSpeak` | X LF-11 | |
| `wrongAudienceAndMissingUnderstudy` | H | testimonies, practicals, evidence blocks including the abandoned stage, report at D4+. |
| `abandonedStageHasNoPointer` | D CT-2 | |
| `ringmasterReachFromTheFloor` | D CT-1 | `player.canInteractWithEntity` from every floor cell: not reachable without building. |
| `attentionActAndRoute` | H | 500 gate, attention act by aim, route warning and choice, one repair. |
| `spectatorOwnerEarnsNothing` | X LF-9 | |
| `act2FacultyStayOut` | X LF-10 | |
| `successionFinale` | H | acts 2-4, 1000, finale, troupe downed, Ringmaster Pattern and Topper. |
| `liberationFinale` | H | three captives severed (`interactOn`), three anchors, Ringmaster killed. |
| `practicalTargetKilledStalls` | D CT-8 | one Husk killed: Mummer and Bellows cannot pass. |
| `practicalsInPeaceful` | D CT-3 | own clock batch. |
| `threadkeeperLessonUnreachable` | D CT-5 | `unreachableNodes()`. |
| `ringmasterLostLeavesSiteOpen` | D CT-7 | D-10 family. |
| `circusSweepAndTrades` | E | 8 performers and the Ringmaster: lang keys, dead ends, trade cards, spectacle spent once, bell locked below D4. |
| `ninePomesBlockOrdeal` | E | ordeal refused while the Pome choice is unresolved. |

### 5.4 `d6_sanctified`: `PlaythroughSanctifiedGameTests`

| Test | Kind | What |
| --- | --- | --- |
| `sanctifiedRiteByPlay` | H | from `covenantD5`: threshold_greater + pillars, Quintessence, thread, amethyst, Bastion, wave, seal. Degree 6, `degree_6_sanctified`, Staff returned; replant says `you_have_already_attained`. |
| `memberRouteToD6` | H | B (member) deposits, then plants sanctified_rite: D6. |
| `d6VicarGuidanceShrinks` | E | the list after each Living Covenant part in all six orders, then `perform_hematic_order_rite`; ledger rows. |
| `monolithWorkingAndD6Tree` | H | Working craft, placement, D6 tree points to the Chamber; held item gives the inquiry only. |
| `chamberRiteSaysWhyNoVisit` | X LF-12 | attuned, not moved, now told why. |
| `chamberRiteAttunesWithoutVisit` | D D6-5 | should the rite attune when the visit cannot start. |
| `chamberReturn` | N | Chamber of Will not loaded. |
| `covenantThroneCraftBindTrance` | H | Working craft consuming a Monolith, place, sit (respawn set, `covenant_throne_bound`), Trance 2500 mL recorded, cooldown, death respawns at the Throne. |
| `memberCannotBindThrone` | D D6-1 | D7 shut for members. |
| `anyProgenitorBindsAnyThrone` | D D6-2 | |
| `covenantVigilWithRecruit` | H | recruit summoned, assigned by `interactOn` at INSCRIPTION, waves and still intervals, `covenant_vigil_completed`; a member player claims a station instead. |
| `vigilHelperLost` | E | helper walks off, is killed (ARRANGE), caster relogs: no proof. |
| `archonSealedBeforeLivingCovenant` | E | archon_rite names "The Living Covenant". |
| `veinMasonD6Chain` | H | referral, Mnemonist counsel, matching Noetic cast, same-build recast does not count, Effigy commit, second cast, tier-3 reward once. |
| `veinMasonD6Forged` | E | early reward, counsel without referral (silent, D-28 family logged). |
| `counselIsALoreCard` | D NPC-4 | the sigil clears once read while the chain still waits. |
| `vagrantMindInquiry` | H | End fixture: bearing, visit, chorus, both reports, cards close, no reward. |
| `eternalCovenantAcrossDeath` | D D6-3 | root key (S-1): bonus lost and rite repeatable after death. |
| `d6WhisperMemo` | E | ARRANGE `tickCount`; the memo offered once. |
| `d6NpcSweep` | E | Alchemist, Mnemonist, Artificer (forged Weight of the Frame brief at D6 does nothing), unspent-temple Hermit. |

### 5.5 `d7_archon`: `PlaythroughArchonGameTests`

| Test | Kind | What |
| --- | --- | --- |
| `archonRiteWithHelper` | H | from `livingCovenantD6`: threshold_grand, five offerings, Bastion and Reservoir, a recruited helper assigned at INSCRIPTION. Degree 7, `degree_7_archon`. |
| `archonRiteHelperLeaves` | E | 0 helpers; the helper leaves the line or dies mid-rite: which line is shown. |
| `d7VicarFinalTruthAndMap` | E | lines, Pome empowerment; the map handed out every ask (D-27 family). |
| `monolithAcrossDegrees` | H | `talkToBlock` at D4 (`unworthy`), D5, D6, D7 (kinship). |
| `cornerstoneClaim` | H | unready, granted, known. |
| `pressFurtherShatters` | H | two choices, Seed and 5-8 fragments at `pos.above()`, block and fillers gone; from 9 blocks, after 1200 ticks or a replaced block refused silently (D-28 family). |
| `strangerShattersYourMonolith` | D D7-2 | a second D7 shatters and takes the Seed. |
| `shatterBeforeCornerstone` | D D7-3 | a 5 roll leaves the frame short. |
| `weightOfFrameChain` | H | brief, Monolithic Armature rite, first D7 upgrade, inspection, material, full set, ability, fitting. |
| `silentArchonLineageAtD7` | D D7-7 | against the 2026-10-31 ruling. |
| `bloomRiteAndRefusals` | H | Seed seated, helper, Bloom placed and owned; overlap refused, Seed kept on a blocked site. |
| `pomePicking` | E | stranger, owner, again, full pack waits, no husk; Creative full pack logged as D-16. |
| `ninePomesAndTheSpine` | H | eaten in order (out of order, another's, unbound refused or inert); toss, chest, death return; the ninth delivers the Spine; full pack, dead and carried variants. |
| `spineReclaimAtBloom` | E | sneak-use: returned, already carried, full. |
| `firstProjectionDegraded` | H | 500 mL paid, forced return next tick, revelation witnessed, core-witness whisper queued. Logged as degraded. |
| `firstProjectionInTheGardens` | N | the two-minute projection, hand lock, damage immunity. |
| `revelationChoice` | H | open, close, relog (re-sent), busy lines, choose; forged without the flag ignored. |
| `revelationChoiceSurvivesDeath` | X LF-5 | |
| `powerLockAfterTheNinthPome` | D D7-1 | every cast but `conjure_staff` and every rite refused; nine pomes over two Blooms lock without Communion. |
| `logoutCompletesProjection` | D D7-4 | |
| `noPointerToTheShatter` | D D7-5 | |
| `atlasFragmentsAndResiduum` | D D7-6 | `MaterialAtlasSpec` gates at D5. |

### 5.6 `d8_endgame`: `PlaythroughEndgameGameTests`

| Test | Kind | What |
| --- | --- | --- |
| `apotheosRiteByPlay` | H | from `apotheosPendingD7`: apotheos_grand, pillars, charcoal, 16 anchors. Degree 8 Apotheos, title, TRUTH. Refusals for SILENT_PENDING, NONE and no Communion at activation and completion; death mid-rite keeps D7 and the path. |
| `apotheosRiteNotInTheLiber` | D D8-3 | without the TRUTH memo the rite cannot be discovered. |
| `apotheosBody` | E | food refused, milk inside and outside the Gardens biome, spore load and burn, tonic. |
| `gardensTravel` | N | |
| `heartNodeRefusedOutsideGardens` | E | `heart_node.refused`, synapse refusals. |
| `heartNodeRespawnTrap` | N + D D8-1 | NOT RUN here; the decision is which destination a stranded Apotheos returns to. |
| `visitationAndPossessionRefusals` | E | refusal chains in order, ending at `no_gardens`. |
| `mycophantVictoryInTestLevel` | H | owned boss, real kill, 120-tick yield, `mycophant_defeated`, Tendril; repeat victory reward. |
| `tendrilMakeRoom` | X LF-6 | |
| `mycophantYieldLogout` | D D8-2 | D-20 analogue: logout or death during the yield loses the win. |
| `mycophantRematchLine` | D D8-5 | the expelled line promises a retry the code does not give. |
| `severanceCuts` | H | SILENT_PENDING: first cut makes the Sickle, second severs; stranger, APOTHEOS_PENDING, old Sickle, logout between cuts. |
| `vesperWoundRefusal` | E | `the_refusal_has_no_place_to`, wound kept; wrong path; stranger. |
| `vesperOrdeal` | N | |
| `vesperMemoryDelivery` | E | full pack, Creative, dead, relog, respawn: exactly one. |
| `vesperMemoryReissue` | E | sealed Bloom reissues; refused while carried or pending; chest-stashed reissue logged as D-19. |
| `silentiumAbsorption` | H | lit Iron Brazier, Living Staff absorption: Degree 8 Silentium, `conjure_sickle`; refusals (unlit, wrong mode, wrong path). |
| `silentiumRestrictions` | E | Ancestral Communion refused, no whispers, Spine refused, Silent Archon recipes only for a Silentium. |
| `endingLinesPerPath` | E | the 24 `dialogue.ending.*` keys at the right states for the four teachers. |
| `silentiumGreetedAsArchon` | D D8-4 | |
| `ledgerBloodBeneath` | E | `LedgerView` progress and key at every state, 0 to 13. |
| `mausoleumWardAndChapels` | H | D7 turned back, D8 crosses, each Chapel kind, Apotheos hallowed burn. |
| `ringDoorProperAndForced` | H | Silentium with 3 nodes, 2 nodes, Apotheos forced, contested, plain touch. |
| `saintTombWake` | E | offerings, enraged after a forced door, reward once, `/kill` returns unrewarded. |
| `recollectionCyst` | E | on fungal network ground in the test level; Creative full pack logged as D-16. |

### 5.7 `npc_late`: `PlaythroughLateNpcUsabilityGameTests`

State grid: degree 5..8 x path (NONE, SILENT_PENDING, SILENT_PENDING + Vesper, SILENTIUM, APOTHEOS_PENDING, APOTHEOS;
only valid combinations) x bloodline role (none, member, leader, leader with recruit) x hand (empty, an item), plus
purifying and Clarity refusal trees. NPCs: Vicar, Alchemist, Mnemonist, Artificer, Anchorite, Voyager, Votary Wayfarer,
Monolith (block), successors, Court roles, Circus performers, Ringmaster.

| Test | Kind | What |
| --- | --- | --- |
| `langKeysSweep_<npc>` | E | one per NPC over the grid: every shown key has English. |
| `noDeadOrUnreachableNodesSweep` | E | `assertNoDeadEnds`, `unreachableNodes`, `emptiedNodes`. |
| `rootOptionsHaveCards` | X LF-15 | successors and originals with Succession options. |
| `offeredEventsRouteSweep` | E | each offered event routes to a handler and changes something or says something. |
| `unofferedEventsRefusedSweep` | E | forged late events (recruit, expel, Monolith, whisper choices, D6 Mason, Weight of the Frame, Court, Circus) refused like their trees. |
| `degreeHintMatchesChapterGate` | E | D5 and D6 hints against `HarbingerChapterProgression.unmetChapterForTargetDegree` for leader and member. |
| `nextStepPointerSweep` | E | at each checkpoint some NPC names the real next step; states with none are logged against D7-5, D8-3, D5-5. |
| `mainStepsAreQuests` | D NPC-3 | the covenant guidance is a lore card with no Quest Work card or notice sigil; `complete_vigil` names only a Vicar. |
| `quintessenceHasNoPointer` | D D5-5 | |
| `monolithBlueprintOnlyAtD5` | D D5-10 | |
| `summonStripsTheOutpost` | D NPC-1 | two lines recruit one Vicar; a summon leaves the outpost without one for a D0 newcomer. |
| `rewardLandingSurveyLate` | D D-3 | late rewards that drop at the NPC's feet, logged under D-3. |
| `tradeBoxSweepLate` | E | no new cards past D4, tithe in blood through `BloodAccounting`, successors serve only their line. |
| `uniqueNpcLossLate` | D D-10 | the Anchorite at D6, the Artificer at D7, a recruited NPC's death mid-Vigil. |
| `bloodlineNameIsEnglish` | D NPC-6 | logged only. |

## 6. Late run (`PlaythroughLateRunGameTests`)

Two tests, `required = false`, batch `playthrough_late_run`, `timeoutTicks = 72000`, template `empty`, built from the
bands' `chain` methods with `PlaythroughClock.segment` and `PlaythroughSites.clearForNextBand` between segments:

- `harbingerD5ToApotheos`
- `harbingerD5ToSilentium`

Shared segments, starting from `PlaythroughStates.illuminatusD5` with blood refilled, the Living Staff and Blood
Projection carried, and an outpost wing (Vicar, Alchemist, Mnemonist) laid once:

1. **D5 line and Fane** (`PlaythroughIlluminatusGameTests.chain`): Bloodline Founding by play, pick up the ledger,
   recruit the outpost Vicar, Founding Fane by play, summon the Vicar into the Fane. Compare with
   `PlaythroughStates.covenantD5` + `recruited`.
2. **D5 to D6** (`PlaythroughSanctifiedGameTests.chain` part 1): sanctified_rite by play. Compare with `sanctifiedD6`.
3. **Living Covenant** (part 2): Monolith Working craft (kept for the Throne), a second Monolith Working craft (kept
   for the D7 shatter), Covenant Throne craft and binding, Covenant Vigil with the summoned Vicar assigned at
   INSCRIPTION. The Chamber of Will rite is played to completion (attunement); `ADV_CHAMBER_RETURNED` is set by a
   labelled `// ARRANGE (NOT RUN: Chamber of Will not loaded)` line and logged as proof owed. Compare with
   `livingCovenantD6`.
4. **D6 to D7** (`PlaythroughArchonGameTests.chain` part 1): archon_rite with the Vicar as helper. Compare with
   `archonD7`.
5. **Communion** (part 2): Monolith cornerstone claim, then press further twice, pick up the Seed; Bloom of the
   Qliphoth with the helper; nine pomes, each husk ripened by the `QliphothBloomSavedData` seam (ARRANGE, ripening is
   random), each picked by `useOn` and eaten by `consume`; the Spine delivered. Compare with `communedD7`.
6. **Revelation**: Spine use (degraded forced return, logged as such), the core-witness whisper opened from the inbox.
   Compare with `choicePendingD7`.
7. Path split:
   - **Apotheos** (`PlaythroughEndgameGameTests.apotheosChain`): `chooseWhisper("archon_choice_eighth_degree")`,
     apotheos_rite by play. Compare with `apotheosD8`, then talk to the four teachers (ending lines) and read the
     ledger (13/13). Gardens travel is a logged `NOT RUN` step, not a failure of the run.
   - **Silentium** (`PlaythroughEndgameGameTests.silentiumChain`): `chooseWhisper("archon_choice_silence")`, Severance
     by two cuts, the wound refused (`the_refusal_has_no_place_to`), then `vesperBeatenD7` by labelled ARRANGE (NOT
     RUN: Vesper ordeal), the Memory delivered by a real `PendingDelivery` tick, absorbed at a lit Iron Brazier.
     Compare with `silentiumD8`, then the ward at a mausoleum is crossed.

Comparison at each boundary: the D0-D5 run's `boundary` checks only the degree, so the late run goes further. Each
new checkpoint in 3.4 is paired with `PlaythroughStates.assertAt(helper, player, Checkpoint)`, which checks the
observable facts that checkpoint's fixture sets (degree, path, Communion and pome count, the listed advancements,
bloodline role and Fane heart, the carried key items), so play and fixture are held to the same list. The boundary
then logs the carried inventory and blood as the D0-D5 run does, and clears the floor. A run that diverges names the
first missing fact. The two runs stay optional until stable; `harbingerD0ToD5` is not changed.

## 7. What cannot run on the GameTest server

| Not run | Why | Proof owed |
| --- | --- | --- |
| Chamber of Will entry and return (D6 Living Covenant part), D5 Chamber chair budget, Vesper ordeal and victory, observational-visit deferral of late claims | dimension not loaded | `chamberReviewClient`, `vesperReviewClient`, client campaign |
| Fungal Gardens: the real first projection (2400 ticks, hand lock), Apotheos travel, heart-node planting and its respawn trap, Visitation arrival, possession, the Mycophant hunt and Nursery, natural Recollection cysts | dimension not loaded | `heartNodeReviewClient`, `possessionReviewClient`, `mycophantReviewClient` |
| Relay Chart finding a Seat, waybill locating a camp or pavilion, natural Court Seats and outposts, Shared Wake schools, fresh-chunk pavilion fit, natural mausoleum placement | seeded worldgen | `court_worldgen_validation` (seeds 42/1337/8675309), `brinedSeatReviewClient`, `brinedOutpostReviewClient`, `courtNpcReviewClient`, `mausoleumReviewClient` |
| Marimo first sight, Wake nursery launch | need a level-listed player; run only if `joinLevel` proves them | as above |
| Client-only: strain tooltip house line, atlas as drawn, hub sigils as drawn, shader readability | client | review clients |

Degraded but run: the first Spine projection (resolves in one tick because the Gardens level is null; Spine travel
also pays 500 mL and does nothing on this server). Tests label it and assert only the server-side outcome.

## 8. Early decisions (band ids)

Each is a `required = false` probe with a `// DECISION <id>:` line, and a row to add under Open questions in
`docs/PLAYTHROUGH_TESTS.md` and `docs/OPEN_QUESTIONS.md` §1.6 once the runner has the evidence.

| Id | Question | Options |
| --- | --- | --- |
| D5-1 | A signed-ledger join has no degree, active-blood or leader-consent gate. | Keep / D1+ active blood / leader confirms / D5 only |
| D5-2 | Members may recruit and mint signed ledgers by Bloodline Recall but cannot expel, set the recall point or disband, and have no voluntary leave. | Add a leave action / leader-only recruit and Recall / keep |
| D5-3 | A kicked member holding their old ledger gets no action and no line. | Refuse with a line / allow rejoin / burn the kicked member's ledger copy |
| D5-4 | Hematic Unbinding consumes its Nether Star and only warns; the ledger Disband is free. | Warn at activation like Fane relocation / drop the rite / make Disband cost / keep |
| D5-5 | No NPC says where Sanguine Quintessence comes from (needed by at least six D5-D7 steps). | A Vicar or Alchemist line / item inquiry / Liber only |
| D5-6 | The D5 Monolith tree carries no covenant guidance, and any held item hides its whole tree. | Add guidance to the D5/D6 tree / keep inquiry plus tree / keep |
| D5-7 | The Basin commission reads the sample only in the main hand at talk time; neither commission rewards. | Any-hand check / a reward / keep |
| D5-8 | A full-pool or zero deposit earns no covenant proof and says nothing. | Count the attempt / a line saying the pool is full / keep |
| D5-10 | The Monolith blueprint is offered only at exactly D5. | Offer at D5+ / a recipe / keep |
| BC-1 | Court standing depends on act order: a custom before any visit locks the house at guest. | Order-independent standing / allow re-earning / keep |
| BC-2 | Logging out or leaving 64 blocks ends a usurpation with no wait; dying costs a day. | Same wait for escape / pause and resume / keep |
| BC-3 | "Ask to join" spends the one free house switch with no warning. | Confirm step / warning line / keep |
| BC-4 | The Champion challenge fires on selecting the option. | Confirm node / keep |
| BC-5 | Joining the Shared Wake needs its rare nursery, and nothing points to it. | A pointer line / joinable from a school / keep |
| BC-6 | The Relay Chart points only at the nearest Lighthouse Garden; nothing locates the Vent Sanctum or the outposts. | Chart modes / resident directions name places / keep |
| BC-7 | The Lightkeeper's "Feed it there" implies Incubator feeding, but the x1.5 applies to worn upkeep only. | Reword / extend to the Incubator / keep |
| BC-8 | The Champion has no degree gate (join and lesson need D5). | D5 gate / keep |
| BC-9 | With Pelagic worldgen off, three NPCs still send the player to the Court. | Hide the pointers / a line / unsupported preset |
| BC-10 | No Court NPC speaks past D5 or about Primal strains. | Add D6-D8 or Primal lines / Liber only |
| BC-11 | A sub-D5 letter-carrier sees a Voyager Garden option that does nothing. | Offer the letter reading there / a refusal line / hide it |
| CT-1 | The idle Ringmaster's perch is out of reach from the floor; the "private galleries" are documented but not built. | Build the geometry / stairs / lower the idle perch / accept building |
| CT-2 | Nothing points to the abandoned stage, the only source of the Missing Understudy's stage clue. | Waybill target / NPC direction / exploration |
| CT-3 | Husk rehearsal targets despawn in Peaceful, so practicals and guest acts cannot finish. | Non-monster targets / a Peaceful line / accept |
| CT-5 | The Threadkeeper's An Empty Ring lesson node is unreachable. | Add the option / drop the node |
| CT-6 | `finale.claimed` is used for a temporary owner and a settled site; `not_ready` names Succession for a forged NEUTRAL finale; `ring_blocked` says "blocked or wet" when another pupil's targets block it. | Separate lines / keep |
| CT-7 | A lost Ringmaster or carousel leaves the site unsettled forever, and the sneak waybill keeps pointing there. | A respawn rule / settle the site / keep (D-10 family) |
| CT-8 | One killed rehearsal target stalls the two-target practicals until expiry. | Replace the target / fail at once with a line / keep |
| D6-1 | Only the progenitor can bind a Covenant Throne, so a member who reached D6 can never reach D7. | Members bind the leader's Throne / another member proof / keep and say so |
| D6-2 | Any progenitor can bind on any Throne, another line's included, though the Vicar says "your Fane". | Require the binder's own Fane / owner-stamp / reword |
| D6-3 | Eternal Covenant is a root key (S-1): lost on death and repeatable. | Move it with S-1 / keep until S-1 |
| D6-5 | The Chamber of Will rite attunes even when the visit cannot start. | Refuse before completion / attune and say so (LF-12 adds the line) |
| D7-1 | From the ninth pome to D8 every cast but `conjure_staff` and every rite is refused, undocumented; the count adds up across Blooms. | Document and warn before the ninth pome / lock only before the revelation / gate on Communion |
| D7-2 | Monoliths have no owner: any D7 can shatter one and take the unowned Seed and fragments. | Bind to the builder / give drops to the presser / keep |
| D7-3 | Shattering before claiming the cornerstone on a 5 roll leaves the Weight of the Frame short. | Warning line / raise the minimum / keep |
| D7-4 | Logging out mid-projection completes it and grants the revelation. | Pause and resume / refuse before spending when the Gardens are missing / keep |
| D7-5 | No D7 NPC names the shatter, the Seed or the Bloom. | A Vicar or Monolith line / discovery and Liber |
| D7-6 | Monolith fragments, imbued cloth and hallowed residuum unlock at D5 in the atlas. | Move to D7/D8 / keep as previews |
| D7-7 | The silent_archon lineage is offered at D7, against "all Silent Archon power is D8 Silentium only". | Hide until D8 Silentium / keep |
| D8-1 | An Apotheos whose heart-node forces respawn has no way home after any death (HR §5.6 says return is never gated). | World spawn / bed / last Overworld position / keep |
| D8-2 | A Mycophant victory is lost if the owner logs out or dies in the 120-tick yield (D-20 analogue). | Record by UUID / settle at the kill / keep |
| D8-3 | The Apotheos rite is discoverable only through an optional memo or at D8. | Unlock at APOTHEOS_PENDING / a pointer line / keep |
| D8-4 | A D8 Silentium is greeted with the D7 Archon tree. | A Silentium tree / keep |
| D8-5 | The failed-rematch line promises a retry after five minutes; a rematch needs a new lure. | Reword / re-arm the rematch / keep |
| NPC-1 | A ledger summon moves a shared outpost NPC away for good, and several lines can recruit one NPC. | Send home after use / successors only / one line per original / keep |
| NPC-2 | The Court custom trade card adds rapport; the dialogue offering does not, though the doc says they pay alike. | Both add / neither / fix the doc |
| NPC-3 | The D5 and D6 Main guidance is a lore card whose sigil clears after one read; no Quest Work card; `complete_vigil` names only a Vicar. | Quest card with a sigil per step / extra lines / keep |
| NPC-4 | The Mnemonist's Anchorite counsel is a lore card while the chain waits on it. | Quest card / keep |
| NPC-6 | The bloodline name is concatenated English stored in saved data. | A translatable default name with a storage change / keep |

## 9. Order of work

1. The harness owner writes section 3: `PlaythroughRites`, the `PlaythroughPlayer`, `PlaythroughStates`,
   `PlaythroughSites`, `PlaythroughNpcs`, `DialogueView`, `Outcome` and `PlaythroughCleanup` additions,
   `PlaythroughNotRun`, `PlaythroughTime`, `LedgerView`, the `CapturedPlayer.joinLevel` method, the
   `structureSite` lift (with `CourtLifeGameTests.FakeSite` and the Adept and Initiate delegates). No band class yet.
2. Band writers write their classes in parallel, each only its own class, no Gradle.
3. The runner compiles and runs `runPlaythroughGameTestServer --offline`, reruns after build collisions.
4. Fixers apply LF-ids test-first, rerun the affected class, update HEMOMANCY_REFERENCE, the topic docs
   (`BRINED_COURT.md`, `CIRCUS.md`, `ENDGAME_PATHS.md`, `NPC_DIALOGUE_AND_TRADE.md`, `CARDINAL_RITES.md`) and
   `docs/PLAYTHROUGH_TESTS.md` (bands, harness, open questions).
5. The final report lists LF-ids applied, the decision probes with the observed behaviour from `PlaythroughLog`, the
   NOT RUN list with its proof owed, and new findings.
