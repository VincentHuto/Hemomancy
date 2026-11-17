Round two of the core loop overhaul: each package appends what it built, its calls, what it left out, and what later packages must know.

## P1: Flaky GameTests and the old-save crash

### What was built

**CQ-11, the old-save kick.** CL-28 renamed the manipulation NBT key to `temper`, so a save from before the rename
loaded every known manipulation as `null`. `KnownManipulations.getSelectedManip` then called `get(0)` on an empty list
(or returned the null key, and the save NPE'd), and the player was kicked with "Invalid player data".
- `BloodManipulation.deserialize` returns null for any tag it cannot read: a missing key, or an enum name that no longer
  exists (`IllegalArgumentException` from `valueOf` is caught).
- `KnownManipulations.deserializeNBT` drops entries whose manipulation or level is unreadable, so no null key enters the
  map; an unreadable `Selected` becomes `BLANK`, an unreadable vein is skipped.
- `getSelectedManip` never throws or returns null: a missing selection falls back to the first known form, else `BLANK`.
  `getSelectedVein` does the same with `VeinLocation.BLANK`. `getSelectedManipLevel` reads through `getSelectedManip`.
- `KnownManipulationUpkeep.safeSelectedManipulation` (a try/catch round the old crash) is gone.
- `KnownManipulationServerPacket` snapshots only non-null entries, always writes the selection and selected vein
  (decode reads both unconditionally; before, a null selection shifted the whole stream), counts only the veins it
  writes, and skips unreadable entries on decode. The wire layout is unchanged, so `PROTOCOL_VERSION` stays 48.
- No migration: the old key is not read. Old dev worlds lose their known manipulations and load cleanly.
- Proof: `KnownManipulationsOldSaveTest` (JUnit, five cases: empty list with a null selection, a pre-rename save, an
  unknown enum name, a null selection with one known form, a null vein with no veins) and the GameTest
  `OwnerSnapshotGameTests.anUnreadableManipulationSaveLoadsWithoutKickingThePlayer` (batch
  `owner_snapshot_old_manipulation_save`): a real player's attachment loads the old-shaped tag, upkeep runs, the owner
  packet round-trips its codec with no bytes left over, and login still sends it.

**The GameTest isolation harness** (`src/gameTest/resources/gametest-isolation.mixins.json`, mixins in
`gametest/mixin/GameTest*Mixin.java`, settings in `gametest/GameTestIsolation.java`). `gradle/game-tests.gradle` adds it
to the combined `runGameTestServer` and every suite launcher that uses a test grid (not the three seeded-world
generators, which have their own mixin configs).
- `GameTestGridSpacingMixin`: the gap between test structures is 64 blocks in x and z (vanilla 5 and 6).
- `GameTestChunkMarginMixin` + `GameTestChunkWaitMixin`: two chunks round every structure are forced, and a test starts
  only once they are entity-ticking. `-PgameTestMarginChunks=N` changes the margin.
- `GameTestOriginMixin`: the grid starts at a random place snapped to a chunk corner, so every run has the same chunk
  alignment. `-PgameTestOrigin=x,z` places it exactly.
- Cost: a combined run takes about 2.1 to 2.2 minutes of server time (1,682 tests), against about 1.4 before.

**The all-air room.** `src/gameTest/resources/data/hemomancy/structure/open_room.nbt` (32 x 18 x 32, no blocks) for
fixtures bigger than the 1x1 template. The ten `HarbingerJourneyFixtureGameTests` that build a journey fixture at
(14, 3, 14) use it (`OPEN_ROOM`).

**Docs.** `docs/TESTING.md` "Deterministic GameTests" (the harness and the rules for writing a test that stays
deterministic). `docs/OPEN_QUESTIONS.md` CQ-11 marked fixed. `docs/HEMOMANCY_REFERENCE.md` (the Beacon row, the
Shared Wake school paragraph) and `docs/MANIPULATIONS_AND_CASTING.md` (change table) for the two gameplay changes.

### Root causes

"Confirmed" means the mechanism was read from code and matches the failure message or a reproduction; "probable"
means the harness removes the conditions it needs and the soak has not shown it since.

| Test | Cause | Fix |
| --- | --- | --- |
| `aDripIntoACauldronSeatsACondenserAndSets` | Confirmed. `ScabDrip.drip` grows the spike on a 10% roll; the old tip is then no longer a tip and the second drip falls from nothing ("clot 4.0"). | Second drip from the spike's lowest block (`lowestTip`, shared with `aFedSpikeGrowsLonger`), and it must land in the Condenser. |
| `strandsAndPurseFillReachTrackingPlayers` | Confirmed. The block anchor at helper (2, 1, 4) was air; `MorphlingLinks.prune` drops air anchors when `gameTime % 10 == 0`, a one-in-ten hit on the manual player tick. | Stone at the anchor before tying. |
| `marrowplumeFeedsAndFeedsMoreNearBone` | Confirmed mechanism. The away host stood 40 blocks out, beside the next structure, where sibling tests place bone. | Away host inside the room, 17 blocks from the bone (search 8); asserts it is not home. |
| `hematicBeaconSpreadsFadingLinesOfLight` | Confirmed. `LuxTraceShaping.pulse` retired a tip whenever its random turn pointed at a lit or blocked cell; a quarter of turns point back onto the line, so a pattern could stop at 3 cells. | Code: a blocked turn keeps the tip's own heading before retiring (see My calls). Test: seeded player random; block light waited for, not read at a fixed tick. |
| `peacockSpiderSpawnsInDaylight` | Confirmed by diagnostics. The plot sat under the room's stone floor (helper y 1; the floor is helper y 4), so `canSeeSky` was true only while the light engine still held the sky light from before the floor was placed. Once light caught up, it failed. | Plot in the floor at helper y 4, spider at y 5, `skyAccess = true`, 200-tick timeout; the duplicate `setDayTime` is gone (the clock already holds noon). |
| `bodyAnswersJourneyUsesRealRecipeAndRestoresState`, `redTaxonomyJourneyUsesFourRealTurnIns` | Confirmed. The fixture at (14, 3, 14) on the 1x1 template reached a neighbour's stone ("occupied by preexisting block"). | All ten fixed-origin journey tests on `hemomancy:open_room`, which holds `HemoJourneyFixtures.bounds` (radius 8, height 10); the harness keeps neighbours 64 blocks off. |
| `absorptionPulsesFasterThanTheHurtCooldownLandLessOften` | Confirmed by traces ("10:20-, 18:20-": the pig never ticked; "26:14-, 34:16-": something else hit it). A pig past the 1x1 template's chunk stood in a block-ticking, not entity-ticking, chunk; neighbours 6 blocks away hit it. | The harness (margin chunks, wide grid). |
| `crossingDimensionsDeliberatelyUnravelsSummon` | Probable. The summon spawns at the owner's offset; in a non-entity-ticking chunk its haul never counts down. | The harness. |
| `veinwingFeathersEmbedStackAndRestoreMaxHealthWhenTheyExpire` | Probable. Its own tickets covered three chunks and fixed delays (4, 6, 110 ticks) assumed every entity ticked on time. | The harness, and the delays are condition waits: embedded, then (after the mob moves) back on it within half a block, then expired and max health restored. The follow check is tighter than before (half a block, not two). |
| `strongerPreyNeedsMultipleBitesBeforeReward` | Confirmed. The 100-tick timeout (siblings allow 240) expired during the second wait, whose message "Lethal second bite did not reward exactly one ambergris" is what the logs show. | Timeout 240; the first wait's message reports prey health, tick, whale target and distance. |
| `aSchoolSwimsItsLoopTogether` | Confirmed by geometry. Slot 5's station is 7.4 blocks from the leader; on the radius-8 test loop inside a 30 x 22 pool it lies outside the water at the corners, the follower cannot reach it and trails past 12 blocks. Size 6 came up one run in three. | Code: a follower whose station is not open water swims to the leader's wake at the same distance back (see My calls). Test: a seeded roll that always makes a full school of six, so slot 5 is tested every run; the 12-block bound is kept. |
| `aGourdGrowsOnlyWhereBloodReaches` and the three other growth tests in `FarmingGameTests` | Two causes. Confirmed (it failed in this package's first proof batch: "grew only 10 times in 200 checks"): a basin-fed gourd keeps the vanilla stem roll, about one in thirteen on the random passed to `randomTick`, so 200 checks average about 15 and fall to 10 or fewer roughly one run in ten. Probable: `clear()` removes the floor and the trials ran in the same tick, while stems and crops need raw brightness 9 and the light engine runs off-thread (an unlit "dry" check also passed vacuously). | `growths()` rolls on a fixed-seed `RandomSource` (`GROWTH_SEED`), so each count is the same every run; `whenLit` waits until every crop reads brightness 9 before the trials; timeouts 200 to 240. The thresholds are unchanged. |
| `aPerfectusIntrusionBitesItsWeaver` | Probable. It counted every `FrayedThreadEntity` within 12 blocks. | Counts threads of this loom that hunt this weaver: none before, exactly one after. |
| `ruptureCreatesEphemeralBloodCurs` | Probable. It counted every cur within 4 blocks. | Counts curs whose cur owner is this hound's owner; the message gives the count. |
| `aChainBindingRunsOnIntoTheNearestCreatures` | Probable. Paid 5 mL and bound nothing: the plain verb ran, so the aim found something else first (a neighbour's entity, or a pig in a non-ticking chunk). | The harness; the message now reports pig positions, whether they tick and what the aim hits. |
| `coronationSpendsOnePiercingCounterattackPerHostileHit` | Not reproduced. The mechanism is synchronous and keyed to the test's own player; no shared state was found. | The message now reports needle count, which hits landed, health before and after, `canHarm` for both attackers and the difficulty. Stable in the soak. |
| `flareSkipsProtectedTargetsWithoutRevealingThem` | Probable ("Valid flare rejected"). Neighbour entities on a 1x1 template, or a husk in a non-ticking chunk. Its PvP flip is restored in the same synchronous block, so it cannot leak. | The harness, and its own batch `manipulation_targeting_flare`. |
| `inTheOpenByDayStainsDrawNothing` | Fixed in Track 10 (`WorldAnswer.openToSky`). Verified stable. | Its sibling `aStrongPoolInAnUneasyRegionDrawsADrowned` discarded every Drowned within 24 blocks, which could reach neighbours; it now discards only what its pool drew. |
| Watchlist: `theNinthPomeLockHoldsAShapingAndSpendsNoTemper` | Not reproduced. Pulse timing is deterministic (`ShapingRules.pulseDue` on the session's last pulse); the likely cause is an aim ray meeting a neighbour's entity on the 1x1-reach layout. | The harness; the message reports what the projection returned, what the aim hits, the sand block and what the player was told. |
| Watchlist: `aSuturedMachineDrinksFromItsOwnerNearbyAndStallsWhenTheyLeave` | Not reproduced. The test body is synchronous and keyed to its own owner and station. | None beyond the harness. Stable in the soak. |
| Watchlist: `blockItemExceptionsKeepTheirOwners` | Not a flake: a deterministic failure from Track 10 (`stained_sod` is item-less on purpose), fixed there. | None. |
| Others in `BUILD_LOG.md` | `successionfinale` and `understudymnemonistpuppet` are in the opt-in playthrough suite; see Deferred. The four `holdfast*` failures came from one development run in Track 9 and never recurred. | None here. |

### My calls

- **The harness, not only per-test fixes.** The brief recommended it, and the first run with it passed all 1,682 tests,
  so no fallback to the origin-only variant was tried. One pass did not mean the harness alone was enough: later runs
  still failed `peacockSpiderSpawnsInDaylight` and `aGourdGrowsOnlyWhereBloodReaches`, which needed the per-test fixes
  below. Margin 2 chunks; it costs about 50% more run time, not double.
- **A random but chunk-aligned origin, not a fixed one.** The test world in `build/phlegethontic-gametest/world` is
  reused between runs (it is about 70 GB of region files from earlier runs); a fixed origin would put every run on the
  last run's leftover blocks and saved entities. Snapping keeps the alignment the same every run. `-PgameTestOrigin`
  still places it exactly.
- **Hematic Beacon's lines (gameplay).** A random turn that points into a wall or back onto the line keeps the tip's own
  heading before the tip retires. Patterns no longer die at three cells; they still keep to one surface, fork now and
  then and never fill an area. Overturn by deleting the fallback block in `LuxTraceShaping.pulse`.
- **Shared Wake school followers (gameplay).** A follower whose station lies in rock or air swims to the point straight
  behind the leader at its station's distance back (`CourtSchoolRules.trailAt`), else to the leader. In open ocean
  nothing changes; along a shelf or seamount the school folds into a line instead of leaving its outer members behind.
- **The school test always launches six.** The size range is still pinned by `CourtSchoolRulesTest`; the GameTest now
  checks the widest formation every run instead of one run in three.
- **Unreadable manipulations are dropped, not converted** (the no-migration rule).
- **The feather test's follow check is half a block, not two.** The old two-block box counted feathers left behind at
  the mob's old spot; the condition wait makes the tighter box fair.

### Deferred

- **The playthrough suite's flakes** (`understudymnemonistpuppet`, `successionfinale`): `BUILD_LOG.md` names them, so the
  scope's "any others" covers them, but the opt-in suite still has eight deterministic required failures, so no run of
  it can show a flake fixed. They go to the playthrough package. Its suite launcher now also runs with the isolation
  harness, which should help, but it was not run here.
- **Root causes not reproduced.** The coronation, chain-binding, ninth-pome and sutured-machine rows above are marked
  "not reproduced" or "probable": they got diagnostic messages and the harness, not a demonstrated cause. If one fails
  again, its message now says why.
- **Growth of the reused test world.** About 70 GB under `build/phlegethontic-gametest/world`, and the wider grid
  generates more chunks per run. Deleting that folder between sessions is safe (it is build output); this package did
  not delete it.
- **Temp directories left by tests.** Thousands of `blood-profile-packs-*` and `blood-response-packs-*` folders sit in the
  combined game directory; some test creates them and never removes them. Harmless, not chased here.

### Numbers the owner may want to tune

- `GameTestIsolation.GRID_GAP` (64) and `MARGIN_CHUNKS` (2, or `-PgameTestMarginChunks`).
- `CourtSchoolRules.trailAt` uses the slot's own distance back (`station(slot)[2]`, 2.2 blocks per row).

### Review clients

None: nothing here draws on the client. The beacon and school changes are server rules; their GameTests prove them.

### For later packages

- A GameTest that flakes is a defect: read `docs/TESTING.md` "Deterministic GameTests" before writing one.
- Helper coordinates are one below the template: the 32 x 12 x 24 `empty` floor is helper y 4.
- Use `hemomancy:open_room` for fixtures bigger than the 1x1 template, and compare the "All N" count after moving a test
  to another template namespace.
- Expect a combined run to take about 2.2 minutes of server time (about 2.6 with Gradle).

### Verification

Baseline: the saved logs held 43 full runs of 1,590 or more tests; the last nine 1,680-test runs had five passes and four
failures. N went from 1,681 to 1,682 (the new CQ-11 GameTest); no test was dropped by the template moves.

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test --rerun`: BUILD SUCCESSFUL, 5,785 tests, 0 failures, 0 errors (includes
  `KnownManipulationsOldSaveTest` and the new `CourtSchoolRulesTest` case).
- First harness run, before any per-test fix: "All 1682 required tests passed" (2.16 min).
- After the per-test fixes, one run failed only `peacockSpiderSpawnsInDaylight` (the helper-coordinate cause above, fixed),
  then a 10-run soak (`build/p1-soak/soak1/`) passed 10 of 10, and one run at an awkward alignment
  (`-PgameTestOrigin=7000015,7000015`, so x % 16 = 15 and z % 16 = 15) passed: "All 1682 required tests passed".
- The next five-run batch failed once, `aGourdGrowsOnlyWhereBloodReaches` ("grew only 10 times in 200 checks"), the
  binomial roll above. The count reset; `growths()` was seeded.
- After that last code change, a second 10-run soak (`build/p1-soak/soak2/`) passed 10 of 10. Its last five runs, quoted
  from `build/phlegethontic-gametest/logs/latest.log` after each:

```
[11Nov2026 01:24:43.095] All 1682 required tests passed :)
[11Nov2026 01:27:19.233] All 1682 required tests passed :)
[11Nov2026 01:29:57.882] All 1682 required tests passed :)
[11Nov2026 01:32:33.885] All 1682 required tests passed :)
[11Nov2026 01:35:12.329] All 1682 required tests passed :)
```

Each run's log and a summary are kept under `build/p1-soak/<batch>/` (build output, not tracked).

### Verified

An independent verifier read the CQ-11 code (`BloodManipulation.deserialize`, `KnownManipulations`,
`KnownManipulationServerPacket`, `KnownManipulationUpkeep`) and its two tests, the four isolation mixins and their Gradle
wiring, the two gameplay changes (`LuxTraceShaping`, `CourtSchoolGoal` / `CourtSchoolRules.trailAt`, which matches
`stationAt` with the lateral offset taken out) and the changed tests. No assertion was loosened that could be found: the
seeded growth rolls keep their thresholds, the entity counts are narrowed to the test's own entities, and the feather
follow box is tighter. The retired word appears only as the pre-rename NBT key in the two CQ-11 tests, spelled in
pieces because they reproduce an old save. The soak logs under `build/p1-soak/soak2/` are real `runGameTestServer`
runs. Nothing needed fixing. Re-run on 2026-11-11: compile BUILD SUCCESSFUL; `./gradlew.bat test --rerun` 5,785
tests, 0 failures, 0 errors; two consecutive `./gradlew.bat runGameTestServer` runs:

```
[11Nov2026 01:44:41.626] All 1682 required tests passed :)
[11Nov2026 01:47:24.963] All 1682 required tests passed :)
```

Overstated in the first report: "no test fixes needed" (corrected under My calls), and the four not-reproduced rows
are fixed only in the sense that they have not failed since (now listed under Deferred).

## P2: alphaCheck and the playthrough suite

### What was run and what it showed

- **A correction to the task text.** `alphaCheck` had been run once before this package: green at Track 11 (1,680
  tests, PROTOCOL 47, `BUILD_LOG.md` "Verification"), before the verifier fixes, CL-28, CL-2 and P1. So it was unproven
  for the current tree, not never run. The playthrough baseline was 8 required failures, not 9: `successionfinale` was
  the ninth before Track 1 and is a flake (below).
- **alphaCheck before** (`build/p2-alphacheck-before.log`): BUILD SUCCESSFUL, "All 1682 required tests passed :)",
  "Packaged client resource paths, PNG headers and font license verified.", 5,787 unit tests with no failures. That run
  already held the OfferedDialogues fix and its JUnit test (written while the build ran). A first attempt failed two
  `LegacyMainTest*` cases with `ClassNotFoundException` for `OfferedDialoguesTest`: I created that file after the run
  had compiled the test classes. It was my own collision, not a defect, and the rerun was clean.
- **Playthrough before** (the last run before this package, kept as `build/p2-playthrough-before-preharness.log`,
  2026-11-10, before P1's isolation harness): "427 GAME TESTS COMPLETE", "8 required tests failed :(", "98 optional
  tests failed".
- **First run under the harness** (with fixes A to E below already in): "1 required tests failed :(" (only
  `d5masonchainwithvascularmending`), "98 optional tests failed", the optional list identical to the pre-harness one.
  The harness itself broke nothing.
- **Playthrough after**, five consecutive runs after the last code change (`build/p2-soak-1..5-latest.log`): each
  "All 427 required tests passed :)" and "98 optional tests failed", the same 98 names every run, and the same 98 as
  before the overhaul. No probe newly fails or newly passes.
- **alphaCheck after**: see Verification at the end of this section.

### Every required failure seen in this package

"Overhaul" means caused by the uncommitted core loop work; "older" names the commit that made it.

| Test | Message | Cause | Class | Fix |
| --- | --- | --- | --- | --- |
| `successionbyplay` | `Non [a-z0-9/._-] character in path of location: hemomancy:vicar/inquiries/hemomancy/scar_pattern#d0b92fad` | Real mod bug. `OfferedDialogues.offersTopic` built a `ResourceLocation` for every topic of the offered tree. A carried scar pattern (and any stack-aware item inquiry) adds a card whose id ends in `#<hash>`, so `DialogueTopicOpenedPacket`'s handler threw on the server whenever such a player opened an unread lore card. | Older: `offersTopic` came with d5a7ddbfa (RT-21), the `#` ids with 41ef95a00. | `OfferedDialogues.listsLoreTopic` matches lore cards only and builds ids with `ResourceLocation.tryBuild`. The `#` node-id format is unchanged. Regression: JUnit `OfferedDialoguesTest` (2 cases). The test now runs to the end (bequest, donate, successor) and passes. |
| `newcomerguidanceonlybeforethehermit` | "after talking to the Hermit the newcomer card is gone" | d5a7ddbfa (DL-6, ledger row E-18 "Gate on the Vicar") moved the gate from met-Hermit to met-Vicar and removed the only `markMetHermit` call, but never added a `markMetVicar` call. Only the playthrough fixture set the flag, so in play every D0 speaker kept the card until D1. | Older: d5a7ddbfa. | Mod: `HarbingerVicarEntity.mobInteract` calls `DialogueHubFactory.markMetVicar` on the right-click (after the clarity check, never on a tree build). Test renamed `newcomerGuidanceOnlyBeforeTheVicar` and rewritten to the rule: near a Vicar keeps the card, a Hermit talk keeps it, a Vicar talk clears it, a death does not bring it back. |
| `vicaroffersinitiationtocharmbearer`, `deathafterclaimkeepsinitiationoffer` | "a player who met the Hermit sees no newcomer card" / "no contradictory newcomer card after a death" | The same commit kept the Vicar's own card ("Where should I begin?", answered "start at a Hermit's temple") for the whole of D0, so a charm-bearer who had just done that was sent back to a Hermit. The owner's playthrough spec (rows for both tests) says no newcomer card here. | Older: d5a7ddbfa. | Mod: the Vicar's own card shows at D0 only while the player's blood is asleep (`volume.isActive()`), see My calls. The charm-bearer fixture no longer marks a Vicar met. Tests unchanged bar their comments. |
| `silentiumrestrictions` | expected `the_spine_will_not_carry_you`, said `spine_dormant` | Stale test. Since the Spine-dormant ruling (93070b745, 2026-11-06) `FungalProjectionRules.travelKind` answers DORMANT for any Spine use after the first crossing, before the path refusal. | Older: 93070b745. | Test expects the dormant line, no projection and no blood taken. `PlaythroughArchonGameTests` only asserts the refused key is not said, which still holds. |
| `dispelrefundissyncedandrecorded` | "the refund is sent to the client in the same tick" | Stale test. Since d5a7ddbfa `BloodVolumeEvents.syncVolume` raises the dirty flag and `PlayerStateSync` flushes at the end of the tick to the server's player list; a captured playthrough player is not on it. The refund itself was right (+900 mL). | Older: d5a7ddbfa. | Test flushes for its player as the end-of-tick stand-in and asserts exactly one `BloodVolumeServerPacket` and a `conjuration_dispel` entry in `BloodFlowLedger`. The flush stays local to this test (see My calls). |
| `ancestralcommunionwiththreerecruits` | "the voice offers a way to end the communion: ... events=[]" | Stale tail. d5a7ddbfa removed the `communion_*` events from `AncestralCommunionDialogueTrees` (nothing handled them); the trees close on plain options. The rite itself completed. | Older: d5a7ddbfa. | Test asserts a closing option on the root, the communion counted once and `voices_in_the_vein`; the `forgeOption` tail is gone. |
| `d5masonchainwithvascularmending` | "Vascular Mending, the Mason's prescription, records the treatment" (the rite ended "structure was disturbed" and a Polished Venous Stone Bricks item dropped) | Real mod bug, found with a new ground diagnostic in `PlaythroughClock.rite`: a stone-brick floor cell (-2, 0, -2 from the Focus) went to air at CULMINATION while the caster held Projection. A missed aim at an anchor or the daemon fell through to `BloodInfusionManager.feedBlock`, the `blood_infusion` recipe turned the floor's stone bricks into Polished Venous Stone Bricks, the infusion lifted the block, and the completion check failed the rite. | Older: the infusion path predates the overhaul (the test failed on the clean 2026-11-04 commit). | Mod: a caster's own running rite claims their Projection over its ground; `BloodProjectionItem` skips infusion and the stain there (`CardinalRiteFootprintResolver.onOwnRiteGround`). Regression: `rite_kind_validation` `projectionDoesNotInfuseTheCastersOwnRiteGround` (checked to fail with the guard switched off: "spent 75.0"). |
| `sainttombwake` (new: 2 of the first 6 harness runs) | "a /kill sends the Saint back into its tomb" | Real mod bug, found with a new detail in the tomb-return warning: "occupied (Block{hemomancy:blood_stain}... at <tomb cell>)". A slain body leaves a floor stain 35% of the time (`GroundNetworkEvents.onDeath`, `KILL_STAIN_CHANCE`), and a /kill'd Saint falls in its own tomb cell; `restoreDormant` accepted only air or water there. | Overhaul (Track 6 kill stains, uncommitted). | Mod: `SaintSarcophagusBlock.blockingCell` (shared by `restoreDormant` and the warning) accepts any replaceable cell. Regression: `SaintEncounterGameTests.aBloodStainInTheTombCellDoesNotKeepTheSaintOut` (combined suite). |

No failure was closed by a rerun.

### The two named flakes and the far-site check

- **`successionfinale`.** The archived logs (`build/playthrough-gametest/logs/*.log.gz`) hold its failures: "three acts
  add 300: 809" (2026-11-01, fixed then by `PASSIVE_SLACK`) and twice "Still waiting after 200 ticks: the pavilion's
  chunks show their entities" (2026-11-08, 2026-11-10). Probable cause: the GameTest server runs ticks faster than real
  time, while a far template's entity sections load off the server thread from a large reused world, so 200 ticks could
  pass first. P1's chunk wait covers the test plot's margin, not a site 64 blocks east. Fix:
  `PlaythroughSites.SITE_READY_TICKS` (1000) for every far-site readiness wait (five pavilion tests and the smoke
  pavilion), with their timeouts raised by 1000, and the two gated copies in `CrimsonTroupePavilionGameTests` raised the
  same. Not reproduced in this package's runs.
- **`understudymnemonistpuppet`.** Archived failures: "the ordeal pays its 1400 mL; spent 1351.25" (2026-11-01, the
  window was widened then) and, four times, "the practical is not passed after 1600 ticks; puppet targeting null,
  memories []", once with a Monster Hunter advancement in the chat, so the player's own blows killed a rehearsal threat
  while the puppet held no target and recorded nothing. Every one ran before P1's harness. Probable cause: the puppet's
  automatic target came from a neighbouring test's mob in the shared RITES batch, which the 64-block grid now keeps out of
  reach. Not reproduced in the harness runs here (net spend 1351.25 or 1355.25 mL every time, inside the window). The
  failure message now names the puppet's distance from its owner and each rehearsal threat's health and distance.
- **Far sites and the wider grid.** The grid advances in x first, a plot width plus 64 apart (96 for `EMPTY`). Only one
  far point sat in a multi-test batch: the Illuminatus recall test moved its leader 96 east, the next plot's corner. It
  now goes 80 east, inside the gap. Every template-placing far site is alone in its batch, so nothing overlaps. The
  `farOrigin` Javadoc says so.
- **The report-book error** (`Error saving [1 minecraft:writable_book] ... rewardlandingsurvey...`) is in the playthrough
  log only and not part of alphaCheck's log check. Not chased.

### My calls

- **The Vicar's own newcomer card goes when the player's blood wakes**, not at D1. The brief suggested rewriting the two
  onboarding tests to expect the card through D0. I followed the owner's playthrough spec instead ("no newcomer card"
  for a charm-bearer), since the card's answer ("start at a Hermit's temple") contradicts a player who has just done
  that. Other speakers still show their card until a Vicar talk.
- **The met-Vicar flag is set by the Vicar's right-click** (`mobInteract`), after the clarity check. Before, nothing set
  it in play. This is the call DL-6 made but did not wire.
- **The charm-bearer fixture no longer marks a Vicar met.** A charm-bearer has met a Hermit; other speakers now send
  them to the Vicar until they talk to him, as in play.
- **`PlaythroughPlayer.openTopic` sends the read record only for an unread lore card**, as `DialogueScreen.openTopic`
  does, and the server now matches lore cards only. `counselIsALoreCard` (an optional probe) still fails the same way.
- **A rite claims its caster's Projection over its whole ritual bounds** (the larger of the size's ritual radius and the
  footprint radius, in x, y and z round the Focus), only for the caster's own rite. Infusion and stains are skipped
  there. Sinks, braziers, sigils, structure feeding and the Focus still take blood, and mining the floor still breaks the
  rite. Another player's Projection on someone else's rite ground is not guarded, since mining it is not either.
- **A Saint's tomb closes over any replaceable cell** (air, water, stains, grass), not only air or water. A player's
  building still keeps it open. I did not stop a /kill'd Saint from leaving a kill stain.
- **The dirty-flag flush stays inside the dispel test.** I did not make `PlaythroughPlayer.tickOnce` flush, because
  other scenarios count sent packets.
- **`SITE_READY_TICKS` is 1000.** A site that is ready sooner costs nothing.
- **The ground diagnostic stays in `PlaythroughClock.rite`.** It logs up to three blocks round the Focus that change
  while a rite runs (not after it completes, since some rites consume their structure), with the tick, the phase and
  what the player held.

### Deferred

- **The two old flakes' root causes are probable, not demonstrated.** Neither failed under the harness; both carry
  diagnostic messages now.
- **Liber text for the rite-ground rule.** The rule is in the reference, `CARDINAL_RITES.md` and `PLAYTHROUGH_TESTS.md`.
  No Liber page says what a missed Projection does on a rite floor, so there was nothing to correct.
- **The leaked temp folders and the reused GameTest worlds** (P1's note) were left alone.

### Open after this package

- `successionfinale` and `understudymnemonistpuppet` are "not reproduced" with probable causes; a future failure's
  message should settle them.
- Another player's Projection on a rite they do not cast can still infuse that rite's floor (as mining can break it).
  If the owner wants rite floors protected from everyone, that is a wider rule.

### Numbers the owner may want to tune

- `PlaythroughSites.SITE_READY_TICKS` = 1000 (test-only).
- The rite-ground reach is the existing ritual bounds (`CardinalRiteBoundaryLeashRules.ritualRadius`,
  `CardinalRiteFootprintResolver.radius`). No new number.
- `GroundRunRules.KILL_STAIN_CHANCE` = 0.35 (unchanged; it is why the tomb flake showed in 2 runs of 6).

### Notes for later packages

- The playthrough suite is green now, so a new required failure there is a real signal.
- `./gradlew.bat runPlaythroughGameTestServer --offline` takes about 2 minutes of server time and 2.5 minutes in all.
- The archived logs `build/playthrough-gametest/logs/*.log.gz` keep every past run's failures; grep them before calling
  something new.
- No payload changed: `PROTOCOL_VERSION` stays 48 and `payload-registry.txt` is untouched. No lang key was added.

### Files

- Mod: `common/entity/npc/dialogue/OfferedDialogues.java`, `common/entity/npc/dialogue/DialogueHubFactory.java`,
  `common/entity/npc/harbinger/HarbingerVicarEntity.java`, `common/item/harbinger/tool/living/BloodProjectionItem.java`,
  `common/rite/harbinger/CardinalRiteFootprintResolver.java`, `common/block/harbinger/puzzle/SaintSarcophagusBlock.java`,
  `common/entity/boss/saint/SaintTrialEntity.java` (warning text only).
- Tests: `OfferedDialoguesTest` (new JUnit); `RiteKindCharacterisationGameTests`, `SaintEncounterGameTests`; playthrough
  `PlaythroughPlayer`, `PlaythroughClock`, `PlaythroughSites`, `PlaythroughStates`, `PlaythroughNpcUsabilityGameTests`,
  `PlaythroughOnboardingGameTests`, `PlaythroughNeophyteGameTests`, `PlaythroughEndgameGameTests`,
  `PlaythroughCrimsonTroupeGameTests`, `CrimsonTroupePavilionGameTests`, `PlaythroughSmokeGameTests`,
  `PlaythroughIlluminatusGameTests`.
- Docs: `docs/PLAYTHROUGH_TESTS.md` (status, renamed test, fixes), `docs/TESTING.md` (status),
  `docs/NPC_DIALOGUE_AND_TRADE.md` and `docs/HEMOMANCY_REFERENCE.md` (newcomer rule, rite ground, tomb return),
  `docs/CARDINAL_RITES.md` (rite-ground call), `docs/SAINT_MAUSOLEUMS.md` (tomb return).

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test --rerun` (run inside the final alphaCheck call): 5,787 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer` (inside alphaCheck): "All 1684 required tests passed :)" (1,682 plus the two new
  regressions).
- `./gradlew.bat alphaCheck` (`build/p2-alphacheck-after.log`): BUILD SUCCESSFUL; "Packaged client resource paths, PNG
  headers and font license verified."; no ERROR line naming hemomancy in the combined log.
- `./gradlew.bat runRiteKindGameTestServer`: "All 33 required tests passed :)".
- `./gradlew.bat runDialogueRouteGameTestServer`: "All 1 required tests passed :)", no golden changed.
- `./gradlew.bat runPlaythroughGameTestServer --offline`, five consecutive runs after the last code change: "All 427
  required tests passed :)" and "98 optional tests failed" each time, with identical optional lists.

### Verified (first verifier pass)

Read the P2 diff file by file (the rest of the tree's uncommitted work set aside) and checked the result lines in
`build/p2-alphacheck-before.log`, `build/p2-alphacheck-after.log`, `build/p2-playthrough-before-preharness.log` and
`build/p2-soak-1..5-latest.log`: they say what the section says, and every P2 source file predates the five soak runs.
The four mod fixes are contained: no payload, no lang key, no blood paid outside `BloodAccounting` (the rite-ground
guard only skips infusion and the stain and pays nothing), no block edit, no vanilla particle, no "tendency". The three
stale-test rewrites assert the new behaviour rather than dropping it. No client-side change, so no review client.

Overstated or left behind, now fixed:
- **The met-Hermit wording survived in three places.** `docs/NPC_DIALOGUE_AND_TRADE.md` still said the met-Hermit flag
  is set by the right-click; the `persistent-data-allow-list.txt` reason for `DialogueHubFactory` still named the
  met-Hermit flag; the D0 claim helper's Javadoc in `PlaythroughOnboardingGameTests` said the Hermit talk sets the state
  the Vicar reads. All three now name the met-Vicar flag or the woken blood.
- **`SITE_READY_TICKS` split a Javadoc.** It was inserted between `TemplateSite`'s Javadoc and the record, leaving that
  comment on the constant. Moved back.
- **A flake surfaced in my first GameTest run:** "1 required tests failed", `marrowplumefeedsandfeedsmorenearbone`,
  "Marrow Feeding heals 0.5: 1.0". Cause: Marrowplume feeds when `gameTime % PROC_TICKS == 0` (`HouseMorphlingItem.proc`),
  and the test drove `PROC_TICKS + 1` ticks, so it fed twice whenever the first driven tick fell on the period (1 run
  in 40). Not a P2 change, but a flake is a defect. Fix: the test drives exactly `PROC_TICKS` ticks, which always hold
  one proc (`MorphlingPowerGameTests`). The other `PROC_TICKS + 1` drives (`MorphlingStrainMatrixGameTests`
  Mermaid's Purse, Dead Man's Rope) assert nothing a second proc changes (a cooldown-gated heal, a Glowing reveal), and
  the matrix Marrowplume drive is exactly 80 ticks (two feeds, one Root Network), so they were left.

Runs after these fixes, one Gradle process at a time:
- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test --rerun`: 5,787 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer`, two consecutive runs: "All 1684 required tests passed :)" both times
  (`build/p2v-gt2.log`, `build/p2v-gt3.log`). The run before the Marrowplume fix (`build/p2v-gt1.log`) read
  "1 required tests failed :(".
- Not re-run by the verifier: `alphaCheck` and the playthrough suite (no mod code changed after the soak runs; the
  verifier's edits are a test drive length, comments and docs).

## P3: Known defects from the first run

Ten defects from the first run's verifiers (A to J below). Every fix has a new or extended test; nothing changed a
balance number. No payload was added or re-laid out, so `PROTOCOL_VERSION` stays 48 and `payload-registry.txt` is
unchanged. No lang key was added (every new refusal reuses `message.hemomancy.player_block_edits.refused`). No player
attachment changed shape.

### What was built, by letter

**A. CQ-2: five Shapings refuse before any price.** `LignumMortisShaping`, `LodeDrawShaping`, `PyreticForgeShaping`,
`HewingShaping` and `VitalEffusionShaping` (all under `common/manipulation/`) now override `VerbShaping.admits`, which
`BloodManipulation.tryPerformVerbPulse` calls before `TemperCharge.forPulse` and payment. Lignum Mortis and Lode Draw
admit at once when the running crawl or draw (same mode and dimension) already holds or touches the target, so a
running crawl does not fire a break check every pulse; otherwise they check `ManipulationCombatHelper.mayAlter` and
`PlayerBlockEdits.mayBreak`, and the duplicate check left `begin`. Pyretic Forge admits items always, admits a block
it is already heating for the same recipe, and otherwise runs the check that used to sit in `heatBlock` (the final
`convert` can still be cancelled and still says so). Hewing checks the struck block only; its per-cell
`PlayerBlockEdits.breakBlock` and the `refused` branch for later cells are unchanged. Vital Effusion's
`mayInteract` and `mayUseItemAt(..., BONE_MEAL)` check moved verbatim. All five say
`message.hemomancy.player_block_edits.refused` in dark red. Tests (adventure-mode player, blood unchanged, Temper
pool unchanged, the line said, the world unchanged): `shaping_validation`
`lignumRefusesBeforeAnyBloodWhereThePlayerMayNotBuild` and `pyreticRefusesBeforeAnyBloodWhereThePlayerMayNotBuild`;
`staff_mining_validation` `lodeDrawBreaksNothingWhereThePlayerMayNotBuild` (extended with the blood, Temper and line
asserts) and the new `hewingRefusesBeforeAnyBloodWhereThePlayerMayNotBuild`; `farming_validation`
`vitalEffusionRefusesBeforeAnyBloodWhereThePlayerMayNotBuild`. None of the three files held a test asserting a
paid-then-refused pulse, so nothing there needed rewriting. `LignumMortisWiringTest` and `ShapingGuardrailTest` pin
nothing the move touched and stayed green.

**B. Hot and cold runs edit for the run's owner.** `GroundRunSavedData.Lit` gained `@Nullable UUID owner`, saved as the
optional NBT key `owner` (absent means none; no migration). `GroundNetwork.pulse` has a six-argument form with the
owner; the five-argument form passes null. `pulseFromPlayer` passes the player, the catalyst feeder passes the owner
already on the cell it relights. `work` resolves the owner (`owner(level, uuid)`: online, alive and
`player.level() == level`, since `PlayerBlockEdits` writes into the player's own level) and hands it to `heat` and
`chill`: campfire and candle lighting is `PlayerBlockEdits.replace(owner, next, lit)`, water freezing is
`PlayerBlockEdits.replace(owner, next, FROSTED_ICE)` followed by the existing scheduled melt. Smelting, pulling,
feeding crops, holding crops back and the dark state are not block edits and run with or without an owner. The class
javadoc names the hot and cold edits. Tests in `ground_network_validation`: `aHotRunSmeltsWhatLiesOnItAndLightsACampfire`
and `aColdRunFreezesStillWaterAndHoldsCropsBack` now pass a `CapturedPlayer` owner and wait on the campfire or ice
appearing instead of a fixed tick; new `aHotRunLightsNothingWhereItsOwnerMayNotBuild` (campfire and candle stay
unlit, the item still smelts), `aColdRunFreezesNothingWhereItsOwnerMayNotBuild` (water stays, the crop is still held
back) and `anOwnerlessHotRunLightsNothing`. Save round trip: new JUnit `GroundRunSavedDataTest`.
`GroundNetworkClientReview` (and the `/hemo test` lit-run fixture in `CoreLoopTestScenarios`) pulse with the player's
UUID so the hot and cold scenes still light.

**C1. The catalyst feeder no longer predicts from an empty table.** `GougeCatalysts.syncSnapshot()` resolves the table
the way `of` does (named items first, then every `TemperCatalysts.all()` item whose school has a rate, ticks
`round(ticksPerWorth * worth)`, clamped to at least 1 for the codec) into one `FunctionFile` per function with no
`temper`, keyed `hemomancy:<function>` so the file-name rule in `parse` holds. `receiveSync` parses each with the same
`parse`, skips a bad one with a warning, and publishes (`bySchool` comes out empty). New datapack sync channel
`hemomancy:gouge_catalysts` in `DatapackSyncChannels.CHANNELS` (`skipIntegrated` true, at most
`GougeCatalysts.MAX_SYNCED_FUNCTIONS` 16 definitions of `MAX_SYNCED_JSON_LENGTH` 32,767 characters). No payload was added
(the channel rides the existing `DatapackSyncPacket`); `ReloadListenerGuardrailTest` and the payload-registry test pin
neither the channel list nor its size. `GougeCatalysts.tables()` / `restore(...)` exist so a test can put the live
tables back. Test: `ground_network_validation` `aClientMirrorsTheCatalystTableTheFeederReads` (a GameTest, not the
JUnit the brief asked for: `ItemStack` and the item registry need a running game; see My calls).

**C2. Bone meal on an unfed gourd stem.** `BloodFedGround.visiblyFed(Level, crop)` reads what both sides see: a planting
basin under or beside the soil whose block state shows `PlantingBasinBlock.FED`, a lit feeding run beside the crop, a
wet stain beside it. `feedingRunBeside` and `wetStainBeside` are now `Level`-typed and shared with the server's
`feeding`, so the two cannot drift; the server keeps reading the basin's block entity (it has to spend from it).
`BloodFedGrowthEvents.onBonemeal` now refuses on the client too (`setSuccessful(false)`, which also cancels) when the
visible read is unfed. Test: `farming_validation` `theClientReadsOnlyTheFeedingItCanSee` (unfed reads unfed, a wet stain
and a basin showing fed read fed, a stamp-only soil reads unfed while `BloodFedGround.level` reads fed).
`aGourdGrowsOnlyWhereBloodReaches` stays green.

**D. Two worked basins on one gutter network.** `VenousBasinBlock.Run` gained `streams` (the stream this basin alone
gives each gutter) and `others` (the other worked basins on the network). `flowWorked` finds the others through the
spouts on its whole connected network, walked uphill, level and downhill (`networkBasins`, at most `2 * MAX_REACH`
gutters; the verifier widened this from a level-or-uphill walk, see Verified), and writes
`GroundRunRules.sharedFill(own, others' streamAt(gutter)...)`, the highest stream any basin gives the gutter. Its own
`Run` (and so delivery) uses its own stream only. `VenousBasinBlockEntity.streamAt(pos)` reads the last walk; after a
walk whose own stream map changed, the basin marks the others stale, and only then, so two basins never wake each
other forever (own streams do not depend on the others). `feedingBasin` (used by `ScabDrip`) now returns the nearest
basin with fresh blood and falls back to the first dry one. Tests: JUnit `GroundRunRulesTest`
`aSharedGutterShowsTheHighestStreamAnyBasinGivesIt`; GameTest `ground_network_validation`
`twoBasinsOnOneGutterLineKeepTheWetOnesStream` (a full and a dry basin on one line; after both rewalk, the dry one
last, every gutter shows the full one's stream; a drip under the line picks the full one; clot the full one and the
gutters dry). The allow-list reasons for `VenousBasinBlock` and its block entity still hold.

**E. The Scrying Temper preview follows the pool.** `ManipulationDiagnosticsSync.refreshTemperPreview(ServerPlayer)`
works out the selected memory's `TemperCharge.forCast(player, selected, 1.0F, false)` as outcome plus cost multiplier
(BLANK and a null charge read "none" and touch no Temper state) and sends only `PacketSyncManipulationCostDiagnostics`
when that differs from what was last sent; `sync` records the value too. It runs from the `TEMPER_POOL` row of
`PlayerStateSync` (now the three-lambda form: the flush sends the pool, then refreshes) and once a second from
`TemperEvents.onPlayerTick`, so picking up or using a catalyst is caught. The last-sent value is a session map in
`ManipulationDiagnosticsSync` (cleared and dropped on logout through `TransientServerStateEvents`), not a field on
`BloodTemper`; see My calls. Test: `temper_validation` `theCostPreviewFollowsThePoolBetweenBloodSyncs` (one snapshot
after the selection sync; a pool change that keeps the line sends none; emptying the school sends exactly one whose
Temper line reads dry; a flush with nothing changed sends none). `TemperGuardrailTest` stays green and no other packet
count changed.

**F. Drip-made Condensers belong to the tank's owner and revert.** `ScabDrip.Source` gained `owner()` (the tank's
`BlockBloodVolume.owner()`), passed through `land` to `SanguineCondenserBlock.convertCauldronByDrip(level, pos, owner)`,
which calls the new `SanguineCondenserBlockEntity.claimOwner(UUID)` on a fresh seat only (owner, the volume's owner,
crystallization from the owner when online, else 0); an existing Condenser's owner never changes by a drip. New
block-entity operation `receiveDrip(double)` (acceptable, add clot, `everFilled = true`, sync) replaces the old
`ScabDrip.collect` body, so a spent stage-0 drip Condenser goes back to a cauldron through `revertIfSpent` and the
next drip seats it again. The `BloodAccountingGuardrailTest` pin on `ScabDrip` (one drain) still holds. Tests:
`farming_validation` `aDripIntoACauldronSeatsACondenserAndSets` (kept, now asserts the null owner for an unowned tank)
and the new `aDripFromAnOwnedTankSeatsTheOwnersCondenserWhichRevertsWhenSpent` (owner matches; a stranger may not draw,
the owner may; scraped empty it reverts to `Blocks.CAULDRON`; the next drip seats it again).

**G. CQ-10: strand pace only while a strand is out.** `StrandService.isOut(Player)`: on the server the open session, on a
client level `ClientBridge.isStrandOut` (default false; `ClientBridgeImpl` answers from `StrandClientState.has`).
`StrandShaping.movement` returns the form's pace only when `isOut`, or on the client during the first
`CLIENT_OPEN_GRACE_TICKS` (6) use ticks while `ShapingDispatcher.currentClaim(player, PROJECT)` is this Shaping, so the
pace does not hitch while the first cue is in flight. Tests in `strand_validation`: `eachStrandSetsThePlayersPace`
rewritten (no declared pace before the paid first pulse, the form's pace with the strand out, none after release; a
fresh player per strand, since the pulse clock in `ShapingSessions.LAST_PULSE` is the player's and a second strand
opened in the same tick waits out the first one's cadence) and the new
`projectionIntoAStationWithAStrandSelectedKeepsThePlainPace` (no declared pace, the station fills).

**H. `PlayerBlockEditGuardrailTest` per site.** Scan roots now include `common/rite`, `common/entity` and `common/world`.
The test counts raw sites per file (`setBlock`, `setBlockAndUpdate`, `destroyBlock`, `removeBlock`); the list
`player-block-edit-allow-list.txt` is now `count<TAB>file<TAB>reason`, 111 rows, every scanned file with a raw site
listed at its exact count whether or not it also names a guard; stale rows and wrong counts fail. `SaintArena` moved
from the test's map into the list, and the map and its second test are gone. Every site in the seven guarded-but-raw
files and the new roots was read. Converted (player is the actor):
- `CrimsonFlameConjurationManip`: the flame is `PlayerBlockEdits.place` (a cancelled place says the existing
  "cannot place flames there" line).
- `FurnaceVeinsManip`: melting frosted ice and Frozen Cruor is `PlayerBlockEdits.lift`.
- `LivingFlailImpactEffects`: freezing water and laying snow are `replace` / `place` for the thrower, and only a
  `ServerPlayer` thrower in the impact's level changes terrain; the hand-rolled `protectionAllows` place-event check
  went (PlayerBlockEdits fires the event). `LivingFlailReworkSourceTest`'s "placement hook" pin now reads
  `PlayerBlockEdits.`.
- `UnstainedCardinalRiteEvents` (Silver Dawn): each conversion is `PlayerBlockEdits.replace(caster, ...)`, only while
  the caster is in the rite's level.
Tests in the `hemomancy` batch (`GroundworkGameTests`): `crimsonFlamesAreTheCastersPlacement` (a place-event listener
cancelling for the caster stops the flames, then nothing refuses and they take), `furnaceVeinsMeltsIceAsTheCastersEdit`
(a break-event listener keeps the ice), `aFlailImpactFreezesWaterOnlyWhereItsThrowerMayBuild` (adventure freezes
nothing, survival freezes), `theSilverDawnCleansesOnlyWhereItsCasterMayBuild` (adventure leaves venous stone, survival
cleanses it). Listed honestly instead (with the reason in the list): `EngramStampItem` (re-cuts the very block the
player used inside the vanilla use path), `TerrestrialSpeculumItem` and `EarthenVeinTravelManager` (remove the
player's own temporary origin), `PendingBloodCraftManager`, `CardinalRiteCompletion`, `CardinalRiteCompletionEffects`,
`WorkingCraftRites`, `SuccessionRites` (a craft or rite consuming its own pattern, subject or effigy, or setting its
own result), `HarbingerCardinalRiteEvents:822` (a collapsed ceremony cracking its own fragile floor cells as
backlash), `CrimsonFireballEntity` (a mob's fireball, already gated on `canEntityGrief`; a player's sets nothing),
every boss and mob edit, `BloodwoodGrowthHandler` (a fed sapling growing, its clearance checked against the feeder),
`SanguineCondenserBlock`, `ScabDrip`, `WorldAnswer`, `GroundNetwork` (the marks' own natural changes) and
`LivingLandLedger` (moved ground settling back, through `PlayerBlockEdits.settle` first when the owner is here).
`ARCHITECTURE_STANDARDS.md` (the rule row and the guardrail row) and the `PlayerBlockEdits` javadoc say the same;
`source-text-impact-map.csv` regenerated.

**I. Fillers read the main block's tank.** `ReservoirBlood.tank(BlockEntity)` resolves a `FillerBlockEntity` to its main
block entity when that is an `IBloodReservoir` and not itself a filler; `settled` and `held` resolve through it.
`FillerBlockEntity` gained `mainBlockEntity()` and overrides `holdsBlood()` (and `isLivingVessel()`) to follow its
main, false with none, so an orphan filler reads as no tank through the existing `!holdsBlood()` branch.
`BloodDrawRights.mayDraw` and `ReservoirReadout` needed no change of their own; `ScabDrip.fromTank` syncs
`ReservoirBlood.tank(tank)`. Test: `living_blood_validation` `aFillerReadsItsMainBlocksTankAndOwner` (a Ghastly
Alembic: the filler resolves to the main, reads its 400 mL, a stranger may not draw through it and the owner may, an
orphan filler reads capacity 0). No JUnit case: `BlockBloodVolumeTest` has no level or block entities to host one.

**J. The cycle key.** Leave it. The cycle key (`ChangeSelectedManipPacket`) steps the selected memory among equipped
entries; the verbs are never equipped (`ManipulationEquipHelper.isVerb`, `normalizeEquippedNames`) and the inner-ring
ruling makes them verb picks that leave the selected memory alone. Putting them in the cycle would make one key step
two different states, which is neither trivial nor consistent with that ruling. No code changed.

**Docs.** `OPEN_QUESTIONS.md` 1.8: CQ-2 and CQ-10 fixed, CQ-3's Condenser half fixed (spike draw rights open), CQ-7's
edits half fixed (catalyst-kept runs open). `HEMOMANCY_REFERENCE.md`: the `admits` step names the five Shapings; the
strand pace applies while a strand is out; hot and cold runs edit for their owner; the feeder's mirrored table;
shared gutters; drip Condenser ownership and reverting; the Temper preview refresh; fillers. `MANIPULATIONS_AND_CASTING.md`:
a calls entry and a history row. `ARCHITECTURE_STANDARDS.md` as above. `CORE_LOOP.md` repeats none of these details, so
it is unchanged. Liber pages: `fed_ground` (the drip Condenser's owner and its return to iron),
`the_ground_network` (hot and cold runs light and freeze only for the one whose blood lit them),
`living_veins_and_basins` (shared gutters), `strand_forms` (a strand slows you while it is out).

### My calls

- Task J: leave the cycle key, in the brief's exact words above.
- A run with no owner, or whose owner is offline, dead or in another dimension, still smelts and holds crops back but
  lights and freezes nothing. It fits the keeper rule and is the only way to respect protection without a player.
- A catalyst feeder keeps a run for whoever lit it (the owner on the cell it relights), never for whoever stocked it.
- The Temper preview's last-sent value is a session map in `ManipulationDiagnosticsSync`, not a transient field on
  `BloodTemper` as the brief said. Storing it on the attachment created `blood_temper` (and, through `forCast`,
  `blood_temperament` and `scars`) on players that had none, which changed `hemo_command_validation`'s
  `everyhemobranchkeepsitsoutcome` fingerprints in my first GameTest run. The golden was not touched; the cause was
  removed. A player's first refresh after login may send one cost snapshot.
- The preview compares outcome and cost multiplier, so a pool that falls but stays tempered sends nothing.
- The gouge catalyst mirror is proven by a GameTest (`aClientMirrorsTheCatalystTableTheFeederReads`), not a JUnit:
  unit tests here do not bootstrap the item registry, and `ItemStack` needs it. The swap and restore happen in one call
  on the server thread, so no other test sees the mirrored table.
- Synced school catalysts are clamped to at least 1 tick so the codec accepts them; the client only asks whether an item
  is a catalyst, so the clamp changes nothing it does.
- Bone-meal prediction: soil fed only by a stamp (a drip, a gutter, Vital Effusion's soak) reads unfed on the client,
  which then predicts no use; the server grows the crop and its own answer corrects the hand. Under-prediction, never a
  ghost growth. Syncing stamps would need a chunk-attachment sync.
- The client strand grace is 6 use ticks (`StrandShaping.CLIENT_OPEN_GRACE_TICKS`), only while this Shaping claims the
  aim.
- Pyretic Forge admits a block it is already heating only when the recipe id still matches, so a block changed under the
  beam is checked again.
- Hewing's `admits` checks the struck block only; the cut's other cells are still refused one by one as they break.
- A drip-made Condenser takes the tank owner's crystallization when that owner is online and 0 otherwise.
- `feedingBasin` prefers any fresh basin on the network over the nearest dry one, so a drip under a shared line never
  goes dry while one basin still holds blood.
- `HarbingerCardinalRiteEvents`' collapse backlash was listed, not converted: converting it to `breakBlock` with an
  empty tool would also stop its stone from dropping. The cracked cells are the rite's own floor, built by the caster.
- `EngramStampItem` was listed, not converted: it re-cuts the clicked block inside the vanilla use path, which already
  checked `mayInteract`, the right-click event and (for items) adventure mode; a stamp used without a player (a fake
  player or dispenser) would otherwise stop working.
- `CrimsonFireballEntity` was listed: its block edit only ever runs for a mob owner allowed to grief.
- The Living Flail changes terrain only for a player thrower; it never had another kind of thrower.
- Furnace Veins melts with `lift` (no drops, break checks) rather than `replace` with air, so no place event fires for air.

### Deferred

- CQ-3's other half: a scab spike still drips from any tank above it, with no draw-rights check on the spike itself.
- CQ-7's other half: a catalyst feeder keeps a feed or hot run working with no blood after the first pulse.
- The gouge catalyst mirror is proven against the server's own snapshot only. The integrated client skips the channel,
  so a review client cannot show it; a dedicated-server check would need the multiplayer launchers.
- Projection with a strand selected into a station on another player's client: other players' selections are not
  synced, so their streams already follow the plain pick; nothing changed there.

### Review client

- `runStrandReviewClient` (existing row `strandReviewClient`, `StrandClientReview`): run once after task G, nothing visual changed. Log line "STRAND_REVIEW captured 17" (`build/p3-strand-review.log`), the same 17 scenes as before (lash sweep first and third person, Blood Lash crack, jet held and third person, spray over a crowd, plant sinking, brand held and third person). The lash, jet and plant shots were opened and read: each strand draws from the hand to its target in first person as before, so the server-side pace change and the client grace did not disturb the first-person strand. No new harness was needed.

### Numbers the owner may want to tune

- `StrandShaping.CLIENT_OPEN_GRACE_TICKS` = 6 (client-only pace grace at a strand's open).
- `GougeCatalysts.MAX_SYNCED_FUNCTIONS` = 16 and `MAX_SYNCED_JSON_LENGTH` = 32,767 (sync caps; the table holds 7
  functions of well under 2 KB each).
- `TemperEvents` refreshes the Temper preview once a second (the existing `SETTLE_INTERVAL_TICKS` 20).
- No gameplay number changed.

### For later packages

- `GroundNetwork.pulse` has an owner form; anything that lights a run for a player should pass their UUID, or hot and
  cold runs will light and freeze nothing.
- `PlayerBlockEditGuardrailTest` now counts every raw edit per file under nine roots. Adding or removing a raw
  `setBlock` anywhere in `common/manipulation`, `network`, `event`, `item`, `block`, `tile`, `rite`, `entity` or
  `world` means updating that file's count (or routing the edit through `PlayerBlockEdits`).
- `ReservoirBlood.settled(filler)` is now the main block's volume (same object). Code that wrote to a filler's own
  attachment expecting it to stay separate would now write the main's; none was found.
- The pulse clock (`ShapingSessions.LAST_PULSE`) is per player across sessions: a test that opens two Shapings in one
  tick for one player sees the second wait out the first's cadence.
- New GameTests must not create player attachments a command fingerprint does not expect; reading an attachment
  through `HemoCapabilityAccess` creates it.

### Verification

One Gradle process at a time, in this order:
- First `runGameTestServer` after tasks A to I (`build/p3-gt1.log`): "2 required tests failed :(". `eachstrandsetstheplayerspace`
  ("hematic_flare did not open on the floor it was aimed at": a jet does not open on a block; the test now aims every
  form but the plant at open air) and `everyhemobranchkeepsitsoutcome` (fingerprints gained `blood_temper`,
  `blood_temperament` and `scars`: the preview store on `BloodTemper` created them; moved to a session map, see My calls;
  the golden was not changed).
- Second run (`build/p3-gt2.log`): "1 required tests failed :(", `eachstrandsetstheplayerspace` again, "hematic_flare did
  not open where it was aimed". Root cause: `ShapingSessions.LAST_PULSE` is per player across sessions, so the second
  strand opened by the same player in the same tick waited out the first one's cadence. Fix: a fresh player per strand.
  Deterministic, not a flake.
- After the last code change:
  - `./gradlew.bat compileJava compileTestJava compileGameTestJava`: "BUILD SUCCESSFUL in 7s".
  - `./gradlew.bat test --rerun`: "BUILD SUCCESSFUL in 1m 29s", 5,788 tests, 0 failures, 0 errors, 0 skipped
    (`build/p3-test2.log`), including the rewritten `PlayerBlockEditGuardrailTest` with exact counts.
  - `./gradlew.bat runGameTestServer`, two consecutive runs: "All 1702 required tests passed :)" (`build/p3-gt3.log`)
    and "All 1702 required tests passed :)" (`build/p3-gt4.log`). 18 new GameTests over P2's 1,684.
- `runStrandReviewClient` as above. No git write command was run; `AGENTS.md` is untouched; nothing was deleted.

### Verified

An independent verifier read the scope, this section and every file the package touched. A to C, E to J hold as
written: the five Shapings refuse in `admits` before `TemperCharge.forPulse` and payment; hot and cold runs light and
freeze only through `PlayerBlockEdits` for an online owner in the run's level; the catalyst mirror rides the existing
`DatapackSyncPacket` (no payload, `PROTOCOL_VERSION` 48 is right); bone meal is refused on the client from block-state
reads only; the Temper preview is a session map cleared through `TransientServerStateEvents`; drip Condensers take
the tank's owner and revert; strand pace needs a strand out; `PlayerBlockEditGuardrailTest` is strictly stronger than
the old file-level test (per-file exact counts over nine roots) and its reasons read true where spot-checked; fillers
resolve through `ReservoirBlood.tank`, which is the only `BLOCK_BLOOD_VOLUME` read. The strand review shots
(`build/strand-client-review/screenshots`, lash sweep and jet held) were opened: the strands still draw from the hand.
No "tendency", no new vanilla particle, no literal English, no golden changed.

D overstated. Its "other basins" search walked level-or-uphill from the basin's own spout only, so two basins on
raised branches that run downhill into one trunk never found each other and still overwrote the trunk's fill; and that
walk and `feedingBasin` both stopped when the set of *looked-at* cells (gutters and the stone round them) passed
`MAX_REACH`, which is about nine gutters, not 64. Fixed in `VenousBasinBlock`: `upstreamBasins` became `networkBasins`,
an uphill, level and downhill walk of the connected worked gutters counted by gutters walked (at most `2 * MAX_REACH`),
which also makes the per-gutter spout check inside `flowWorked`'s own walk redundant (removed); `feedingBasin` now counts
gutters walked against `MAX_REACH`. New GameTest `ground_network_validation`
`basinsOnBranchesThatJoinDownhillShareTheTrunk` (a full and a dry basin on raised branches meeting in a trunk; after the
dry one walks last the trunk shows the full one's stream, and each lists the other); it fails on the old search.

Runs after the fix, one Gradle process at a time: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
"BUILD SUCCESSFUL" (`build/p3v-compile.log`); `./gradlew.bat test` "BUILD SUCCESSFUL", 5,788 tests, 0 failures, 0
errors, 0 skipped (`build/p3v-test.log`); `./gradlew.bat runGameTestServer` twice in a row, "All 1703 required tests
passed :)" both times (`build/p3v-gt1.log`, `build/p3v-gt2.log`).

## P4: Second-wave Shapings

Seven memories from the second wave of `02-first-conversions.md` now shape a verb, in the doc's order: Sanguine
Mending, Crimson Flame Conjuration, Furnace Veins, Glacial Grasp (freeze and thaw), Glacial Rampart, Ferric Rampart and
Carrion Communion. Each keeps its R cast (CL-21). Vascular Dowsing's "veins toward ore" pairing (Task 9) is deferred.
No payload, attachment, block, item or entity type was added: `PROTOCOL_VERSION` is still 48, and
`payload-registry.txt` and `frozen/registry-ids.txt` are unchanged.

### What was built

**Task 0, narrow Shapings.** `BloodManipulation` gained `addProjectionShaping` / `addAbsorptionShaping` (ordered lists
of narrow Shapings) and `getShapings(verb)` (narrow first, then main; `ShapingRules.claimOrder`). `getShaping(verb)`
still returns the main one. `ShapingDispatcher`:
- `selected` returns the main Shaping, or the first narrow one when there is no main;
- the new `shapings(player, verb)` lists them in claim order;
- `currentClaim` and `tryShape` try them in that order;
- `tryShape` first lets any open held channel continue (so a strand swept over water stays a strand);
- `shapes` is true when the list is not empty.

`ShapingSessions.held` and `track` now continue a session only for the same Shaping. When a session changes from a
presenting Shaping to a strand, the cast presentation stops. The new `ShapingSessions.working(player, shaping, target)`
lets `admits` skip the break-event check on a target already being worked. `VerbChannelOverlay` prices the Shaping that
claims the aim. `ShapingGuardrailTest` and `CoreLoopTeachingGuardrailTest` read `(set|add)` and `.getShapings(`.

**Shared pieces:**
- `ShapingRules.sweepBetween` holds the gouge's sweep join, now shared; `GougeCuttingShaping` uses it.
- `verb/SweepTrail` holds the ramparts' last cell per player, cleared through `TransientServerStateEvents`.
- `world/ground/SwitchableLights` (`kindled`, `snuffed`) is now the one copy of the campfire and candle lit-state logic.
  The hot gouge run in `GroundNetwork` and `EclipseSnuffShaping` use it.
- `BloodFlowVisuals.from` draws a tendril from a world point into the caster.
- `TemperCharge.fillFromCarrion` fills Mortem only, up to the cap.

**The Shapings** (factories in `verb/Shapings`, each attached once in `ManipulationInit`):

| Shaping | Class | Attached as |
| --- | --- | --- |
| Sanguine Mending | `ferric/SanguineMendingShaping` | `setProjectionShaping(Shapings::sanguineMending)` |
| Crimson Flame Conjuration | `flammeus/FlameKindleShaping` | `setProjectionShaping(Shapings::flameKindle)` |
| Furnace Veins | `flammeus/FurnaceVeinsShaping` | `setProjectionShaping(Shapings::furnaceVeins)` |
| Glacial Grasp, freeze | `congeatio/GlacialFreezeShaping` | `addProjectionShaping(Shapings::glacialFreeze)`, ahead of the plant |
| Glacial Grasp, thaw | `congeatio/GlacialThawShaping` | `setAbsorptionShaping(Shapings::glacialThaw)` |
| Glacial Rampart | `congeatio/GlacialRampartShaping` | `setProjectionShaping(Shapings::glacialRampart)` |
| Ferric Rampart | `ferric/FerricRampartShaping` | `setProjectionShaping(Shapings::ferricRampart)` |
| Carrion Communion | `mortem/CarrionDrawShaping` with pure `CarrionRules` | `setAbsorptionShaping(Shapings::carrionDraw)` |

**Supporting changes:**
- Furnace fuel goes through a new accessor mixin, `mixin/core/FurnaceFuelAccessor` (`litTime`, `litDuration`,
  `recipeType`), registered in `hemomancy.mixins.json`. No access transformer.
- Ice:
  - `TemporaryIceManager` gained `placeFor` (through `PlayerBlockEdits.place`), `extend` and `thaw` (through
    `PlayerBlockEdits.replace` / `lift`).
  - `TemporaryIceSavedData.Entry` gained a `water` flag, saved under the key `water`. Expiry restores water or air
    through the same single raw `setBlock`, so the allow-list count (4) is unchanged.
- Ferric:
  - `FerricConstructShapes.Kind.SEGMENT` is appended after `SPIKE`, with `Kind.isWall()`. The entity's two wall checks
    use `isWall`.
  - New `FerricConstructEntity.ownerId()` and `FerricPlacement.segment`.
  - `FerricDuctilisGeometry` draws a SEGMENT as three staggered plates.
- `BloodProjectionInteractionEvents.onRightClickBlock` now lets a blood hand or the Staff channel when the selected
  Shaping claims the aim. The review client caught this: right-clicking a furnace or an anvil had opened its screen
  instead.

**Data:** the tags `data/hemomancy/tags/entity_type/mendable_constructs.json`, `tags/item/carrion_remains.json` and
`tags/block/carrion_blocks.json`.

**Lang:**
- `hemomancy.manipulation.<id>.shaping.project` for sanguine_mending, crimson_flame_conjuration, furnace_veins,
  glacial_rampart and ferric_rampart, and the rewritten glacial_grasp line.
- `.shaping.absorb` for glacial_grasp and carrion_communion.
- `message.hemomancy.furnace_veins.nothing_to_burn` and `message.hemomancy.ferric_rampart.too_many`.

**Liber:** two pages, `manipulations/pages/second_shapings` ("Mending, Fire and the Dead", ordinality 30) and
`second_shapings_walls` ("Ice and Iron Walls", 31). Both are registered in `LiberEntryDefinitions` (`SECOND_SHAPINGS`,
`SECOND_SHAPINGS_WALLS`, in `CORE_LOOP_PAGES`) and opened by the seven memory items (`LiberDiscoveryEvents`). The
core-loop page count is now 26 (guardrail and ledger updated). `CoreLoopTeachingClientReview` lists both pages.

**Tests:**
- GameTests: new `SecondShapingsGameTests` (30 tests, `shaping_validation`), plus seven pin rows in
  `FirstConversionsGameTests`. Its fixtures are now package-private and reused, not copied. The new tests cover:
  - the Task 0 order: water freezes ahead of the plant, ground and dry ice open the plant, a plant already out keeps
    the channel over water, and the narrow freeze's pin;
  - each Shaping on each target kind, paying exactly its pinned pulses;
  - unpaid refusals in adventure mode, and the empty furnace bank and the 13th segment refused;
  - fall-through to a station, and the Iron Brazier left alone;
  - ice melting back to water and a furnace going out on release, both waited on by condition;
  - six "an awakened sigil still takes" cases.
- JUnit:
  - new: `CarrionRulesTest`, `FurnaceVeinsBurnTest`;
  - extended: `ShapingDispatcherTest` (claim order, sweep join) and `FerricConstructShapesTest` (the segment, ordinal
    pinned).

**Docs:**
- `HEMOMANCY_REFERENCE.md` (top note, 8.0.1 rows and two paragraphs, each manipulation's row);
- `MANIPULATIONS_AND_CASTING.md` (status, section, file row, rule, calls, history);
- `CORE_LOOP.md`;
- `architecture/ARCHITECTURE_STANDARDS.md` (the `add*Shaping` rule and the guardrail row);
- `wiki/Blood-Systems.md`;
- `core_loop/11-progression-teaching-tests.md` (ledger rows);
- `OPEN_QUESTIONS.md` §1.8 (CQ-15 to CQ-19).

The source-text impact map was regenerated and is unchanged.

### My calls

- **The narrow slot.**
  - Narrow Shapings are tried in the order attached and before the main one.
  - An open held channel beats every claim.
  - `selected` names the main Shaping, so the plant strand's pace and the strand tests are unaffected.
  - A Shaping session continues only for the same Shaping on the same target.
- **Sanguine Mending.**
  - Dropped tools and armour mend 10 durability a pulse; golems heal 3 HP a pulse (both times the work scale).
  - A chipped or damaged anvil goes back one stage on the 8th paid pulse on it (counted by `pulse.index()`, so a
    change of target starts again), and its facing is kept.
  - The mendable list is iron and snow golems not targeting the caster, not `canHarm` (CQ-17).
  - Gear that is worn or held stays the R cast's.
- **Crimson Flame Conjuration.**
  - Kindles two lights a pulse within 2, mirroring Blood Eclipse.
  - Primes TNT only when the TNT is the struck block: the block is lifted through `PlayerBlockEdits` first, then lit
    with the player as igniter.
  - Never claims an Iron Brazier (now an explicit check, since an empty brazier does not count as taking blood), a
    furnace, or a block that takes blood.
- **Furnace Veins.**
  - The bank is radius 2, at most 8 furnaces, priced as one stream (CQ-15).
  - A pulse raises a furnace's burn to `burnTicks(workScale)`: 15 ticks at full strength, less when dry, never more
    with the Staff. Cooking is never faster than with coal, and fuel in the slot is not burned while the stream holds.
  - The struck furnace is lit through `PlayerBlockEdits.replace` when it is not already lit.
  - A bank with nothing to cook is refused unpaid with its own line.
- **Glacial Grasp freeze.**
  - Wants FLUID and BLOCK. It claims open water, and Frozen Cruor only while open water lies beside it, so a held
    sweep walks a path but dry ice still takes the plant. This narrowing came from the strand review shot, where
    leftover ice had taken the plant's channel.
  - Ice placed by a Shaping goes through `PlayerBlockEdits.place`, not `replace`: freezing water is placing into a
    replaceable cell, and it saves a break event per cell.
  - The Shaping's ice melts back to the water it froze. The R cast's ice still melts to air (CQ-19, left as it is under
    CL-21).
- **Glacial Grasp thaw.** Vanilla ice and frosted ice thaw to water (to air in an ultra-warm dimension). Packed and blue
  ice are left alone.
- **Glacial Rampart.**
  - Block targets only.
  - Aimed at the wall's own ice, it holds the channel and builds nothing, so the wall never creeps back toward the
    player. (As first built that pulse was paid for nothing; the verifier made it keep the held stretch standing, see
    Verified.)
  - A cell any living body (or the caster's own box) touches is left open; a cap of ice above a body's head is allowed.
- **Ferric Rampart.**
  - Claims any face with room in front of it, the ice wall's rule. The first build required a sturdy floor in reach,
    which no client-side pick can see well.
  - Where no segment can stand, that pulse raises nothing.
  - The first segment of a sweep faces the player; later ones face across the sweep.
  - At most 12 standing per player, counted within 64 blocks; past that the pulse is refused unpaid.
- **Carrion Communion.**
  - Yields: flesh 2, bone 3 (its catalyst worth), skull 10, bone block 24 Mortem, and 1 HP per remain (CQ-16).
  - On the ground the Staff draws more remains, not more from each, and a dry pulse draws less.
  - A skull takes 2 pulses and a bone block 4, lifted with no drop.
  - The net Temper per pulse is always positive (`CarrionRulesTest`).
  - The item tag is `carrion_remains`, not `carrion` as the brief proposed, to keep it apart from the existing
    `carrion_foods`.
- **Liber.** The text ran over one page, so it was split into two pages (30 and 31). Each spreads over 2 to 3 leaves in
  the review shots.
- **Right-click.** A blood tool now beats a block's own use whenever the selected Shaping claims the aim, the same rule
  blood endpoints already had.

### Deferred

- **Task 9, Vascular Dowsing toward ore.** `Form.ORE` draws only a locator box (`ManipulationVisualRenderer`), not a
  from-to line. The only from-to lines (`BloodFlowVisuals`) start or end at the caster and go to everyone nearby. An
  owner-only line from a cut cell to an ore needs a new cue, which means a new `Form` or packet field, and a review pass
  of its own. "Lode Draw follows the veins" stays deferred with it.
- **Crimson Flame Conjuration as a Plant strand** (Track 12). When that strand is built, this registration becomes
  `addProjectionShaping(Shapings::flameKindle)` and the strand takes `setProjectionShaping` (CQ-18).
- **Ferric Spikes** stays unconverted (a Track 12 strand candidate).
- **No advancement and no NPC line** names the seven new Shapings (ledger row "Not yet taught").

### Review client

`secondShapingsReviewClient` (`SecondShapingsClientReview`, property `hemomancy.secondShapingsReview`, user
`Shapings2Review`, log `SECOND_SHAPINGS_REVIEW captured N`) captured 19 shots in three runs. Every shot was opened and
judged.

- **Run 1** found the real bug fixed above: right-clicking an anvil or a furnace opened its screen, so the mending,
  kindling and furnace shots showed GUIs.
- **Run 3** confirmed what the shots show:
  - A dropped diamond pickaxe and an iron golem each take a living-blood tendril and blood-iron plates.
  - A damaged anvil is worked. (Run 3's last frame came before the eighth pulse, so it still showed a damaged anvil and
    did not prove the stage change; the verifier's run does, see Verified.)
  - A campfire and a ring of candles are lit with flame tongues.
  - Three furnaces in a row burn with tall FURNACE flames from one stream.
  - A frozen path lies across a pond, with the HUD reading "Glacial Grasp, 10 mL/s", which proves the narrow freeze
    prices the overlay rather than the plant's 40. Absorption thaws a channel back to water at 4 mL/s.
  - A swept ice wall: the server log counted 27 cruor blocks, nine full columns. The cruor's large-panel texture makes
    it read as two slabs at that range, the same look as the R cast's wall.
  - A swept iron wall of nine joined segments, seen in third person.
  - Rotten flesh and a bone block drawn into the hand along a Communion tendril, with bone clots rising.

The harness turns the client's view for sweeps and logs what a wall shot left standing.

`runStrandReviewClient` was run twice. The first run showed the plant not opening in its old world, because leftover
ice had been claimed by the freeze; that run led to the cruor-claim narrowing. The second run shows strand=PLANT with
its build arc. `runCoreLoopTeachingReviewClient` opened both new pages: all their leaves render and nothing is cut off.

### Numbers the owner may want to tune

| Shaping | mL/s | Cadence | Reach scale | Work by hand (times the work scale) | Where |
| --- | --- | --- | --- | --- | --- |
| Sanguine Mending | 40 | 10 | 1.0 | 10 durability or 3 HP a pulse; an anvil stage per 8 pulses | `SanguineMendingShaping` |
| Crimson Flame Conjuration | 12 | 10 | 1.5 | 2 lights a pulse within 2 | `FlameKindleShaping` |
| Furnace Veins | 12 | 10 | 1.0 | the bank burns 15 ticks a pulse (radius 2, max 8) | `FurnaceVeinsShaping` |
| Glacial Grasp, freeze | 10 | 10 | 1.5 | 5 cells a pulse in radius 1; 200-tick ice | `GlacialFreezeShaping` |
| Glacial Grasp, thaw | 4 | 10 | 1.5 | 3 cells a pulse within 1 | `GlacialThawShaping` |
| Glacial Rampart | 48 | 5 | 1.5 | a 3-high column, joined across 3 cells; 240-tick ice | `GlacialRampartShaping` |
| Ferric Rampart | 40 | 10 | 1.5 | a 1x3 segment, joined across 3 cells; 200 ticks; 12 standing | `FerricRampartShaping` |
| Carrion Communion | 8 | 10 | 1.5 | 1 remain a pulse; Mortem 2 / 3 / 10 / 24; 1 HP each | `CarrionDrawShaping`, `CarrionRules` |

Every Shaping also burns 0.5 Temper of its school per 10 ticks of cadence (`TemperCharge.forPulse`). Glacial Rampart
burns 0.25 per pulse at cadence 5.

### For later packages

- To put a second Shaping on a manipulation that already has a main one, use `addProjectionShaping` /
  `addAbsorptionShaping` (narrow, tried first). A narrow Shaping must claim strictly less than the main one, or it
  takes the main one's targets. Prove the order with a GameTest the way Glacial Grasp's ordering tests do.
- `ShapingDispatcher.selected` is the main Shaping. Code that needs the one actually working should use
  `currentClaim(...).selected()`.
- The right-click rule in `BloodProjectionInteractionEvents` means any claiming Shaping beats a block's screen. A
  Shaping that claims a block with a GUI is reached by holding the verb; the block's GUI is reached with another item
  in hand, or by sneaking with the Staff.
- `SweepTrail` serves any future swept Shaping; key it by the Shaping instance.
- `TemporaryIceManager.placeFor` / `thaw` are the player-edit paths for ice; the old `place` / `placeOwned` stay for
  casts and bosses.
- The per-suite GameTest launcher `runShapingGameTestServer` runs no tests. Its namespace filter drops every test that
  uses a vanilla template (`minecraft`), and the whole shaping suite does. Use `runGameTestServer`. This was not fixed
  here (it belongs to `gradle/game-tests.gradle`'s owner).

### Verification

Runs were made one Gradle process at a time, after the last code change (the cruor-claim narrowing and its GameTest):
- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: "BUILD SUCCESSFUL" (`build/p4-compile.log`).
- `./gradlew.bat test`: "BUILD SUCCESSFUL", 5,797 tests (5,788 plus 9 new), 0 failures, 0 errors, 0 skipped
  (`build/p4-test.log`). No golden changed and no `*.actual.txt` was left.
- `./gradlew.bat runGameTestServer` twice in a row: "All 1733 required tests passed :)" both times
  (`build/p4-gt-final-1.log`, `build/p4-gt-final-2.log`). That is 1,703 plus the 30 new tests.

Two earlier runs:
- `build/p4-gametest-1.log`: 5 failures, all in new tests. Three sigil tests lacked a claimable target behind the
  sigil; Ferric Rampart's floor rule did not claim there; the brazier test struck the floor past the brazier's low
  shape. All five are fixed.
- `build/p4-gametest-2.log`: all 1,732 passed.

No git write command was run; `AGENTS.md` is untouched; nothing was deleted.

### Verified

An independent verifier read the scope, this section and the package's code, tests, tags, lang, Liber pages and
docs, and opened every review shot. The seven Shapings are wired as described: each pays through
`tryPerformVerbPulse` (so `BloodAccounting.payUpkeep` and `TemperCharge`), refuses in `admits` before any price,
edits blocks through `PlayerBlockEdits` (or `TemporaryIceManager.placeFor` / `thaw`, which use it), uses only Hemomancy
visuals, has its lang keys and its Liber line, and keeps its R cast. No payload or registry changed. The second-wave
table's other rows were already built in round one (Vital Effusion, Sanguine Magnetism, Hematic Beacon, Blood Eclipse,
Living Land; chum is plain Projection), so Vascular Dowsing's veins toward ore is the only row left, as deferred above.

Fixed by the verifier:
- **Glacial Rampart paid for nothing.** Held on its own ice (which the aim strikes as soon as a column rises in front
  of it) each pulse cost 12 mL and did nothing. It now keeps the held stretch standing (`keepStanding`: tracked ice
  within a column's height and one cell to each side is extended to the wall's lifetime) and still raises nothing new.
  New GameTest `glacialRampartHeldOnItsOwnWallKeepsItStanding`; lang, the walls Liber page and the reference row say so.
- **Dry pulses landed no softer on the two Ramparts** (the third rule). New pure `ShapingRules.standingTicks(base,
  workScale)`: full time at full strength, shorter when dry, never under a quarter, never longer with the Staff (it
  raises more, not longer). Both Ramparts use it for their ice lifetime and segment duration. Unit test
  `ShapingDispatcherTest.aDryPulseRaisesAWallThatStandsShorter`. My call: the quarter floor.
- **Stale reference row.** `HEMOMANCY_REFERENCE.md` still gave Ferric Rampart's first-build claim rule (a sturdy floor
  in reach); it now states the built rule (a face with a replaceable cell before it).
- **The anvil shot did not prove the mend.** `SecondShapingsClientReview` now runs each shot for 112 ticks (was 90),
  holds the anvil for 108 and captures at 106, and logs the anvil's state at the end of its shot.

Left as built (noted, not defects): a narrow Shaping's `SweepTrail` mark is only forgotten on a release that finds a
session open, so a new sweep started within three cells of an old one's end can join it; no Absorption sigil test exists
for the thaw or Carrion, the same as every earlier Absorption Shaping.

Runs after the last change, one Gradle process at a time:
- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL (`build/p4v-compile.log`).
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,798 tests, 0 failures, 0 errors, 0 skipped (`build/p4v-test.log`). No
  golden changed, no `*.actual.txt`; the source-text impact map regenerates unchanged.
- `./gradlew.bat runGameTestServer` twice in a row: "All 1734 required tests passed :)" both times
  (`build/p4v-gt1.log`, `build/p4v-gt2.log`); 1,733 plus the new rampart test. No flake seen.
- `runSecondShapingsReviewClient` (`build/p4v-review.log`, 19 shots, all opened): the anvil shot now reads "Chipped
  Anvil" with the server logging `mending-anvil left chipped_anvil` (damaged to chipped); the ice wall still leaves 27
  cruor blocks and the iron wall 9 segments; every other shot matches the description above. The walls Liber page gained
  one sentence and was not relaunched in the teaching client; HutosLib paginates it.

## P5: Strand skill line and second-wave strands

P5 is built. Eleven more memories hold a strand, Blood Binding brands what it holds, a plant sunk into a wet stain runs
along it, the Living Staff branch has six strand skills (the late one is CL-27's off-hand strand), the first-person
hand works each form, and a Saint test proves the Saint claims ahead of a strand. No payload, attachment, block, item,
entity type or registry id was added: PROTOCOL_VERSION is still 48, `payload-registry.txt` and `frozen/registry-ids.txt`
are untouched, and the creative tab order is unchanged.

### What was built

**The skill rules.** New pure class `common/manipulation/verb/strand/StrandSkills`: what each level is worth (length
scale, lash tautness, jet quicken, accent damage and state, pace, brand set and lapse ticks) and the twin's numbers.
`JetRules.tick(state, id, quicken)` and `fillTicks`, `BrandRules.tick(state, id, setTicks)` and `progress(state,
setTicks)`, and `SprayRules.TWIN_MIN_DOT` are new overloads; the old signatures are unchanged.

**The six skills** (Living Staff branch, `LivingStaffSkillBranch` inside the skill-editor markers; fields in
`SkillPointInit`; readers `getLongStrandLevel`, `getTautLineLevel`, `getFreeHandLevel`, `getWhipcrackLevel`,
`getDeepBrandLevel`, `hasSecondStrand` in `SkillPointHelper`, three-method pattern):

| Id | Skill | Max | Degree | Cost (SP) | Parents | Tree position | Icon |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 74 | Long Strand | 3 | 2 | 2 | Living Conduit | 363, 386 (surface) | `memory_blood_lash` |
| 75 | Taut Line | 2 | 3 | 2 | Long Strand | 270, 344 (surface) | `memory_searing_lash` |
| 76 | Free Hand | 3 | 3 | 2 | Long Strand | 323, 286 (surface) | `blood_projection` |
| 77 | Whipcrack | 3 | 5 | 3 | Taut Line, Deep Base | 343, 541 (deep) | `memory_gloam_laceration` |
| 78 | Deep Brand | 2 | 5 | 2 | Long Strand, Deep Base | 382, 530 (deep) | `memory_hemorrhage` |
| 79 | Second Strand | 1 | 6 | 4 | Whipcrack, Free Hand, Deep Base | 302, 571 (deep) | `blood_absorption` |

Wiring: `VerbShaping` gains `default double bloodPerPulse(Player)` and `default double reachScale(Player)`.
`ShapingDispatcher.reach`, `BloodManipulation.tryPerformVerbPulse`'s price and `VerbChannelOverlay` read the player
forms; `StrandShaping` overrides both (Long Strand on the reach, the twin's 1.5 on the price) and adds Free Hand to
`movement`. `StrandService.length` multiplies by Long Strand. `lashTick` passes Taut Line's `taut`; the jet's hold
passes its quicken. `StrandHits.strike` raises an accent's damage by Whipcrack and `stateTicks` lengthens its state.
The brand sets with Deep Brand's set ticks and lasts its lapse. The session (`StrandSession`) fixes the four skill
levels and the twin when the strand opens.

**Client parity.** `StrandCue.encode` gains a 7-argument form: bits 20-21 carry the skill the form's motion reads (Taut
Line for a lash or jet, Deep Brand for a brand, 0 to 3, clamped) and bit 22 the twin. The 5-argument form encodes the
same count as before. `ManipulationVisualPacket.count` is a full VarInt, so no wire change. `StrandClientState.follow`
runs the caster's tautness, quicken and set ticks from the cue; length already came from the server in the cue's
radius.

**Second Strand, the twin (CL-27).** `StrandService.twinReady(Player)` (Second Strand unlocked and the hand opposite the
channelling hand empty or an `ICellHand`) and `twinFor(Player)` (the open session's answer on the server, else the
prediction). The twin lash is a second `LashRules.State` from the mirrored hand at 0.85 of the lead's tautness whose
sweeps strike at half through `StrandHits.strike(..., lockouts)` with the session's own `twinLastHit`; it never cracks.
A twin jet hits 1.4 times; a twin spray cone opens to 50 degrees; a twin plant sinks two pulses a paid pulse (cap
unchanged); a twin brand, when it sets, also brands the nearest other harmable creature within 4 blocks.

**Eleven strands** (one `Shapings` factory and one `ManipulationInit` line each; every R cast, entity cast and Drudge
action kept):

| Manipulation | School | Form | Factory | mL a pulse | Scale |
| --- | --- | --- | --- | --- | --- |
| `gloam_laceration` | Tenebris | Lash | `gloamLacerationLash` | 20 | 0.85 |
| `cruor_javelin` | Congeatio | Jet | `cruorJavelinJet` | 25 | 1.15 |
| `blood_needle_lance` | Ferric | Jet | `needleLanceJet` | 17.5 | 0.9 |
| `blood_needle_fan` | Ferric | Spray | `needleFanSpray` | 20 | 0.85 |
| `synaptic_jolt` | Ductilis | Spray | `synapticJoltSpray` | 20 | 0.85 |
| `prismatic_reproof` | Lux | Spray | `prismaticReproofSpray` | 30 | 1.1 |
| `crimson_flame_conjuration` | Flammeus | Plant | `crimsonFlamePlant` | 17.5 | 0.9 |
| `ferric_spikes` | Ferric | Plant | `ferricSpikesPlant` | 25 | 1.2 |
| `conductive_mark` | Ductilis | Brand | `conductiveMarkBrand` | 20 | 1.0 |
| `insatiable_hunger` | Mortem | Brand | `insatiableHungerBrand` | 15 | 1.0 |
| `grave_debt` | Mortem | Brand | `graveDebtBrand` | 30 | 1.0, state 360 ticks |

Crimson Flame Conjuration's kindling moved to `addProjectionShaping` and the plant is its main Shaping (CQ-18).

**Blood Binding is the Animus brand.** New `StrandService.brand(ServerPlayer, EnumBloodTemper, LivingEntity)`;
`BloodBindingShaping.pulse` calls it for every creature the binding holds.

**A plant runs along a wet stain.** `GroundNetwork.wetStainAt`, `isWetStain` and `spendStain` (the last through
`PlayerBlockEdits.replace`, so the `GroundNetwork` row in `player-block-edit-allow-list.txt` is unchanged).
`PlantRules.stainCells` and `stainRun` (pure flood, six neighbours, within 8 blocks, walk order). `StrandService`'s
`Eruption` record now carries its radius, damage, a shared struck set and the stain cell; a plant on a stain queues one
0.75-wide eruption per cell, 2 ticks apart, and each spends its cell.

**Client.** `client/render/world/StrandSchoolGeometry` (new): Ductilis arcs that re-jag every other tick, an arc spray,
a prism spray, cruor shards and a spearhead, iron flechettes down a jet and across a spray, iron eruption spikes.
`StrandRenderer`: a twin drawn again from the off hand (`FirstPersonPalm` when a cell hand is drawn there, else the
rule hand), a Tenebris lash (dark umbra body with a pale edge and seam), the school bodies above, a Flammeus fire
column, spike counts scaled with the eruption radius, and a solid-pass `collectIron` plus solid-pass accents (the
Ferric eruption's iron). `ManipulationVisualRenderer` visits `STRAND_ACCENT` in the solid pass and calls
`collectIron`. `FirstPersonPalm` now keeps a palm per arm, so both hands' palms are known when both draw.
`CellHandItemRenderer.applyStrandArmTransform` poses the first-person arm per form (no new `RenderHandEvent`
subscriber). `StrandPlayerPose` points the free arm along the twin.

**Lang:** `skill.hemomancy.skill_long_strand`, `skill_taut_line`, `skill_free_hand`, `skill_whipcrack`,
`skill_deep_brand`, `skill_second_strand` and their `.desc`; `hemomancy.manipulation.<id>.shaping.project` for the ten
new strand memories; `crimson_flame_conjuration` and `glacial_grasp` rewritten for the plant and the stain run.

**Liber:** `manipulations/pages/second_strands.json` "More Strands" (ordinality 32), `LiberEntryDefinitions.SECOND_STRANDS`
in `CORE_LOOP_PAGES`, opened by the eleven memory items in `LiberDiscoveryEvents`; `strand_forms.json` points to it. The
core loop has 27 pages (`CoreLoopTeachingGuardrailTest` raised).

**Tests.** JUnit: new `StrandSkillsTest` (6); additions to `LashRulesTest` (a taut tip follows closer and never
overshoots), `JetRulesTest` (quicken), `SprayPlantBrandRulesTest` (stain cells, flood order and gaps, reach, deep brand,
twin cone) and `StrandCueTest` (skill and twin round trip, old counts unchanged, clamping). GameTests in
`StrandGameTests` (20 new): one behaviour test per new strand (form opens, the hit names the caster and carries the
school, or the brand takes in the school), `crimsonFlameKindlesFirstAndPlantsOnBareStone`,
`longStrandAndFreeHandChangeLengthAndPace`, `tautLineFillsTheJetFaster`, `whipcrackHardensAndLengthensTheCrack`,
`deepBrandSetsFasterAndLastsLonger`, `secondStrandTwinsFromAFreeOffHandForHalfAgain`, `aPlantOnAWetStainRunsAlongIt`,
`aBoundCreatureCarriesTheBindersBrand`, `aSaintClaimsAheadOfAStrand`; `eachStrandCostsAsPinned` and
`eachStrandSetsThePlayersPace` extended with the eleven. `FirstConversionsGameTests`: pins for the eleven, the Crimson
Flame row now pins its plant (35 mL/s) and the kindling is pinned as the narrow Shaping (12 mL/s); its `price` helper
reads the Shaping that claims the aim through `bloodPerPulse(player)`. `SecondShapingsGameTests`'s brazier test reads the
kindling as the first of the Shapings.

**Goldens (intended).** `src/gameTest/resources/command/hemo-command-fingerprints.txt` (18 lines) and
`src/gameTest/resources/dialogue/route-fingerprints.txt` (6 lines) were rewritten from their `*.actual.txt`. A
character diff of every changed line shows one insertion each: the six new skill entries, ids 74 to 79, all LOCKED at
level 0, in the `skill_progress` list. Nothing else moved.

**Docs:** HEMOMANCY_REFERENCE (Strand combat summary; 8.0.2 table rows, the Animus brand, the stain run, the skill
table, the twin, the first-person arm and school shapes; the Living Staff skill table and implementation table),
MANIPULATIONS_AND_CASTING (summary, code map row, decided-without-you entry, change table), CORE_LOOP (Strands row),
wiki/Blood-Systems ("More strands", "Training the strands"), the Track 11 ledger (R2 P5 rows), ARCHITECTURE_STANDARDS
(the strand row), OPEN_QUESTIONS 1.8 (CL-25 and CL-27 updated, CQ-18 built, CQ-20 to CQ-26). The source-text impact map
was regenerated.

### My calls

- **Six skills, not five.** The package text called Free Hand the off-hand strand; the track doc names Free Hand as pace
  and Second Strand as CL-27's off-hand strand. Both are built (CQ-20).
- **Placement.** The surface ring round Living Conduit is full between the absorption line and the core branch (a
  search over free cells found no spot 40 tree units clear for five nodes), so Long Strand (D2), Taut Line and Free Hand
  (D3) sit on the surface and Whipcrack and Deep Brand are Degree 5 on the deep layer, Second Strand Degree 6 (CQ-21).
  The skills tab shots show no overlap; nodes sit a little off their degree rings, as some existing ones do.
- **Skill numbers:** Long Strand x1.1/1.2/1.3 on length and the pick's reach; Taut Line +20% tip follow and +25% jet
  fill a level; Free Hand +0.1 pace a level, never past walking; Whipcrack +15% accent damage and +20% accent state a
  level; Deep Brand sets in 20/15/10 ticks and lasts 600/900/1200. Costs and xp costs as in the table above.
- **The twin is a copy of the open strand, not a second channel** (vanilla uses one item at a time). It pours only from
  an empty or cell-hand off hand, fixed when the strand opens; the numbers per form are in CQ-22. The twin never refuses
  the strand: an off hand holding an item just opens the strand alone.
- **Every spray pulse is an accent**, so Whipcrack raises all spray hits and states (CQ-26).
- **Cruor Javelin is Congeatio's jet**; no new manipulation, memory or registry id.
- **Three Mortem brands** differ only by price and Grave Debt's longer Necrosis (CQ-23).
- **Blood Binding brands every creature it holds**, each pulse, for the binder's Deep Brand duration; the set flourish
  shows only when no brand of theirs was on it (CQ-24).
- **The stain run** (CQ-25): six-connected wet cells within 8 blocks, 4 plus 2 a sunk pulse (at most 20), each 0.75
  wide, 2 ticks apart, the plant's damage by blood sunk, one hit per body per run; each cell it erupts in goes tacky
  through `PlayerBlockEdits` as the caster's edit. The run is fixed when the plant goes off, so a stain ageing mid-run
  does not cut it. A stain must lie on the struck face of the planted block.
- **Spikes per eruption:** about 3.5 per block of radius, 3 to 12, so a 20-cell run is about 60 spikes, not 240.
- **First-person arm** (applied in view space before the arm's own transform): lash yaw x1.4 toward the chain's second
  node, clamped to 25 degrees, pitch clamped to 18, a 6-degree throw-through while reversing; jet thrust out and up 14
  to 18 degrees with a tremor that grows with pressure squared; spray roll of 16 degrees; plant palm pitched down 22 to
  32 degrees as it fills; brand pulled back 0.14 blocks and cocked 16 degrees as it takes.
- **School looks:** Tenebris is an umbra body over a cold violet-white edge (`0xE6DDF2`) with a pale seam, and its crack
  adds pale-edged cuts; Ferric's plant pour is drawn in blood (the Ductilis flow read as a nerve); prism light is white,
  gold, crimson and a pale rose (never silver).
- **`FirstPersonPalm` per arm.** The palm record was one slot shared by both arms, so a second drawn hand would have
  stolen the main hand's strand root. It now keeps one per arm; every caller reads the same API.
- **The Saint test** uses the Hemorath fixture's level-added mock player (`DuctilisGameTests.player`); the arena finds
  challengers by an entity search, which a `CapturedPlayer` does not satisfy. It proves the strand's channel is gone two
  ticks after the aim meets the Saint and that every pass of the chain with the aim on him spent nothing; a blood
  equality check was dropped because the player's volume moves for reasons that are not the strand.

### Deferred

- **No new advancement or NPC line** for a first twin, a stain run or a brand by binding; the Liber, tooltips and skill
  descriptions teach them. A voice pass for the Mnemonist or Artificer would be the place.
- **The first-person off hand** draws no arm when it is empty (vanilla draws no empty off hand), so a twin from an empty
  off hand leaves the screen's lower edge from the rule hand. With a cell hand in the off hand that arm is drawn and
  posed.
- **Two-client check of other players' twins and skills on the cue** was not run; the cue round trip is unit-tested and
  the client code reads the cue, not the local cache.

### Review client

`runStrandReviewClient` (row `strandReviewClient`, user `StrandReview`), three runs; the last
(`build/p5-review-3.log`, save `StrandReviewP5c`) logged `STRAND_REVIEW captured 64` across 37 shots. Every new shot was
opened and judged:

- Gloam Laceration, first and third person: after the first run it read as a thin grey thread; widened (body x1.8, a
  wider pale edge and a fuller dark sheath) it reads as a dark cut edged in light in first person, and its crack shows
  the pale-edged tear. From the side camera it was still a thread; the verifier fixed that (see "Verified").
- Cruor Javelin: a dark cruor beam with glass flakes riding it into the husk. Blood Needle Lance: blood-iron flechettes
  streaming down the line with barbs, lodged at the contact (the first run's lance shots opened no strand because the
  harness set the needle family's baseline to the fan's level; fixed to the higher of the two).
- Blood Needle Fan: a fan of iron flechettes (raised from 10 to 16 and enlarged after the first run). Synaptic Jolt:
  re-forking gold-white arcs to each husk (thickened). Prismatic Reproof: prism rays in white, gold, crimson and rose
  from a hot knot (thickened).
- Crimson Flame Conjuration: the plant pour, then a tall fire column and tongues round the point. Ferric Spikes: the
  pour, then iron spikes driven up round the point (doubled in height after the first run, now bold from the side).
- The three brands: an arc tether and arc knot for Conductive Mark, the dark Mortem tether and ring for the two Mortem
  brands.
- Twin lash: the second lash leaves from the lower left in first person and from the off arm in third person, which now
  points along it.
- Stain run: fire bursts run from the planted cell along the stain and round its corner to the husk, first and third
  person. The first two runs showed a ring instead: the harness aimed at the block's middle, so the ray struck the block
  in front and the plant was not on the stain (logged `plantStain=null`); aiming at the top face fixed it
  (`plantStain=BlockPos{...}`), and the GameTest aims the same way.
- Skills tab: surface (zoomed to 105%) shows Long Strand, Taut Line and Free Hand clear of Persistent Arsenal, Crimson
  Projection and the core nodes; deep shows Whipcrack, Deep Brand and Second Strand clear of Arterial Cadence and
  Unbound Siphon, traces running to their parents.
- First-person poses: the jet's arm is thrust and braced at the target, the spray's palm rolled, the plant's palm pitched
  down, the lash's arm swung with the stroke, the brand's drawn back; each strand still leaves the drawn palm with no kink.

The harness now hides the first-person hand while the side camera is in use, moves the pointer to a corner for the skills
tab, and logs the stain diagnostics.

### Numbers the owner may want to tune

- `StrandSkills`: `LENGTH_PER_LEVEL` 0.1, `TAUT_PER_LEVEL` 0.2, `QUICKEN_PER_LEVEL` 0.25, `ACCENT_DAMAGE_PER_LEVEL`
  0.15, `ACCENT_STATE_PER_LEVEL` 0.2, `PACE_PER_LEVEL` 0.1, brand set 20/15/10 and lapse 600/900/1200, `TWIN_COST` 1.5,
  `TWIN_TAUT` 0.85, `TWIN_SWEEP` 0.5, `TWIN_JET` 1.4, `TWIN_PLANT_PULSES` 2, `TWIN_BRAND_RADIUS` 4.
- `SprayRules.TWIN_MIN_DOT` (50 degrees).
- `PlantRules`: `STAIN_BASE_CELLS` 4, `STAIN_CELLS_PER_PULSE` 2, `STAIN_MAX_CELLS` 20, `STAIN_REACH` 8,
  `STAIN_STEP_TICKS` 2, `STAIN_CELL_RADIUS` 0.75.
- The eleven strands' mL a pulse and damage scale in `Shapings` (table above).
- Skill costs, degrees and tree positions in `LivingStaffSkillBranch` (ids 74 to 79).
- First-person pose angles in `CellHandItemRenderer.applyStrandArmTransform`.

### For later packages

- A price or reach that depends on the player goes through `VerbShaping.bloodPerPulse(Player)` and `reachScale(Player)`;
  tests should price through `FirstConversionsGameTests.price`, which now reads the claiming Shaping.
- `ShapingDispatcher.selected` names Crimson Flame Conjuration's plant now; its kindling is `shapings(...).getFirst()`.
- A strand's skill a client needs rides `StrandCue` bits 20-22; bits 23 and up are free.
- A family form's GameTest fixture must know the form's baseline at the form's level (`ManipulationFamilyRegistry.form`),
  or `tryPerformVerbPulse` refuses it ("You have not absorbed this form's memory"). `StrandGameTests.prepare` does this.
- A plant on a stain needs the stain on the struck face's cell: aim at the block's top face, not its middle.
- Skill ids 0 to 79 are used; the next is 80. Six new skills move every `skill_progress` line in the two fingerprint
  goldens; regenerate and diff them the same way.
- The Saint fixture needs a level-added player (`DuctilisGameTests.player`), not a `CapturedPlayer`.

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: "BUILD SUCCESSFUL".
- `./gradlew.bat test`: "BUILD SUCCESSFUL", 5,812 tests, 0 failures, 0 errors, 0 skipped (`build/p5-test.log`).
- `./gradlew.bat runGameTestServer` twice in a row after the last code change: "All 1754 required tests passed :)"
  both times (`build/p5-gt-final-1.log`, `build/p5-gt-final-2.log`). That is P4's verified 1,734 plus the 20 new tests.
- Earlier runs, for the record: the first run failed 8 (the needle and spike forms refused for want of their baseline in
  the fixture, the Saint test's player was not in the level, the kindle test aimed out of the plant's reach, and the two
  goldens); the second failed only the Saint test (challenger search); the third failed the Saint test's blood equality,
  replaced as described. No flake was seen.
- No git write command was run, only one Gradle process ran at a time, `AGENTS.md` is untouched, and nothing was deleted.

### Verified

An independent first-pass check read the code against the scope and the rules: every new strand hit goes through
`StrandHits.strike`, the twin's price goes through `bloodPerPulse(Player)` into `tryPerformVerbPulse`'s payment, the stain
run's block edits go through `PlayerBlockEdits.replace`, no payload, vanilla particle, literal or "tendency" was added, and
the two goldens differ only by the six LOCKED skill entries. Three things were overstated or missing, and were fixed:

- **Gloam Laceration in third person was still a thread.** The review's shots showed it far thinner than the Searing Lash
  from the side camera, so "reads as a dark cut" held only in first person. The Tenebris lash now keeps its width out to
  the tip (`StrandRenderer.lash`: the body widens to 2.1 times toward the tip instead of tapering, and the pale halo and
  dark sheath are a little stronger). Relaunched (`build/p5v-review.log`, save `StrandReviewP5v`, `STRAND_REVIEW captured
  64`): from the side it is now a dark band with a visible pale edge; first person is unchanged in character.
- **The twin's per-form effects had no GameTest.** `secondStrandTwinsFromAFreeOffHandForHalfAgain` proves only the flag
  and the 1.5 price. New `secondStrandBrandsASecondBodyAndSinksTwice` proves through the real chain that a twin brand
  also takes on a pig two blocks from the aimed one and not one eight away, and that one paid pulse of a twin plant sinks
  two. The twin lash, jet and cone remain unit-level only (`SprayPlantBrandRulesTest` for the cone).
- **Deep Brand's description said "ticks" to the player.** It now reads in seconds.

The Mortem brand tethers (Insatiable Hunger, Grave Debt) are thin from the side, as the first wave's Hemorrhage is; left
as they are, since the brand's focal moment is the set flourish, not the tether.

Results after the fixes: compile "BUILD SUCCESSFUL" (`build/p5v-compile.log`); `./gradlew.bat test` "BUILD SUCCESSFUL",
5,812 tests, 0 failures, 0 errors, 0 skipped (`build/p5v-test.log`); `runGameTestServer` twice in a row, "All 1755
required tests passed :)" both times (`build/p5v-gt1.log`, `build/p5v-gt2.log`; 1,754 plus the new twin test). No flake.

## P6: Menagerie: vat screen, sampling, Bestiary, breeding

P6 is built in the working tree, uncommitted. A kept creature's jar or vat now has a screen in place of the chat
readout; the Living Syringe samples kept creatures and a vial is refused on a syringe-only kind; the Living Bestiary
lists every kind the player has met; and two kept creatures of one kind can breed. PROTOCOL_VERSION is still 48 and
`payload-registry.txt` is unchanged: the screen is a menu with data slots, and everything a client draws rides the
block entities' existing update tags.

### What was built

**The brood's rules and state.**
- `common/tile/harbinger/menagerie/BroodRules` (pure): every number below, `broodCost`, `perSecond`, `share`,
  `gestates`, `progress`, `atTerm`, `paidSeconds`, `drainsVigor`, `inherits`, the `State` enum (NONE, GESTATING, PAUSED,
  AT_TERM, RESTING) and the `Refusal` enum, each with its lang key, plus `ownSide` and `mateSide`.
- `KeptSpecimen` gained the pairing: NBT keys `BroodMate` (a block position as a long), `BroodBearer`, `BroodPaid` and
  `BroodRestUntil`, each written only when set. `pair`, `unpair`, `payBrood(buffer, amount)` (the one new buffer drain,
  so `BloodAccountingGuardrailTest` allows `KeptSpecimen.java` two drains now), `spendVigor`, `restUntil`, and the client
  `quicken` / `quickening` for block event 2. `keep`, `clear` and dying drop the pairing; dying and breaking out also
  release the mate. A picked-up vessel keeps its rest but not its pairing (`SpecimenJarData.setKeptState` has a rest
  overload). `sample(level, vessel, cost)` takes the cost; the old two-argument form is the vial's.
- `SpecimenVessel` now extends `MenuProvider` (display name `container.hemomancy.specimen_vessel`, opens
  `SpecimenVesselMenu`) and has `fits(Mob)`, delegating to the blocks' static `fits`.
- `VatVigorRules.SYRINGE_SAMPLE_COST = 3` and `VatVigorRules.word(vigor)` (thriving, wan, failing), now shared by the
  readout and the screen.

**The brood in the world: `SpecimenBrood`.** `find` (a dry run: the pressing vessel's own refusal, then the first
eligible neighbour in N, E, S, W order at the same height), `press` (pairs, or parts a paired vessel), `sever` and
`releaseMate` (break a pair on both sides), `second` (run from `SpecimenVessels.serverTick` after the creature eats:
either side drops a pairing its mate no longer answers; the bearer pays its share from its own buffer or the mate's,
drains Vigor every tenth paid second and delivers at term), `deliver` and `young` (made with `EntityType.create`, no
`finalizeSpawn`, `setAge(-24000)` for an `AgeableMob`, the inherited school set on the attachment before
`captureEntity`, the temporary entity discarded), `state`, `paidAndCost`, `mateOf`, `loaded` (never loads a chunk; an
unloaded mate pauses the brood), `breeds(type)`, `glassCentre`, `threadAnchor` and `threadPoint` (the arc both the
motes and the client thread follow). Tag `hemomancy:menagerie_no_brood` (`data/hemomancy/tags/entity_type/`) holds the
Morphling Polyp. Resends: a pair syncs on a state change or a visible 5% step, otherwise only `setChanged`.

**The screen.**
- `common/menu/tile/functional/SpecimenVesselMenu`: no slots, the vessel's position and a `ContainerData`. Button 0
  calls the vessel's own `SpecimenVessel.pressBrood` (which runs `SpecimenBrood.press`); the menu does no work
  (`StationMenuGuardrailTest` holds). As first built the menu called `SpecimenBrood.press` itself; the verifier moved
  the operation onto the vessel, as the scope asks. `stillValid` needs the vessel,
  a creature in it, and the viewer within 8 blocks. Menu type `hemomancy:specimen_vessel` in `ContainerInit`.
- `common/tile/harbinger/menagerie/VesselView`: the data slots, worked out on the server for one viewer each broadcast
  (Vigor x100, buffer, capacity, flags, the yield x100 only for a studied kind, feed price x10, brood state, progress in
  thousandths, cost capped at 32,000, and the live brood refusal for this viewer). `SpecimenVessels.viewData` builds it.
- `client/screen/tile/functional/SpecimenVesselScreen` (300 x 236), registered in `MenuScreenRegistrar`: a
  `VeinBackgroundRenderer` field of its own seed on the machine palette, ring border and corner accents and bevels from
  `ScreenDrawUtils`; the creature turning in a glass well (`SpecimenJarRenderer.display`, a husk lying fallen under a
  dark shade), the Vigor bar and word, keeper, buffer bar, feed price, tending, the blood (hollow phials while unread,
  Tempers when read, a draw's yield and the quirks in small type when studied, else how many more to study), and the
  brood panel with state, progress bar, paid of cost, and one button ("Let them breed" / "Part them"), disabled with the
  refusal as its tooltip.
- `client/screen/widget/TemperPhials`: the HUD's phial art moved out of `TemperMarksOverlay` (which now calls it), with
  a scale and a hollow grey `drawUnread`.
- `SpecimenVessels.useEmptyHand`: husk and lead-in unchanged; an occupied vessel opens the menu (`player.openMenu`) and
  meets its kind instead of printing lines. It opens only for an empty main hand (`SpecimenVessels.opensScreen`); both
  blocks' `useWithoutItem` return PASS on both sides when the hand holds something and the creature lives, so a held
  item keeps its own use. `readout(...)` is kept for tests and its keys are reused by the screen.

**Sampling.**
- `SpecimenVessels.sample` refuses a vial on a kept kind whose profile `requiresLivingSyringe`, with
  `BloodSamplingResult.REQUIRES_LIVING_SYRINGE`'s message and no Vigor paid.
- `SpecimenVessels.syringeSample` (same preserved and CL-8 checks, 3 Vigor, the shared private `donor` tag
  `vessel:<pos>:<band>`) returns a `Drawn(type, donor)`.
- `LivingSyringeItem`: `fillVialFromTarget` split into `prepare` (load or eject, find the slot), `store` (write the
  sample, a finish hook for the succession and Warden cases, record the collection) and `finishDraw`; the entity path is
  unchanged in order and effect. `useOn` on either half of a vessel with a living creature, not sneaking, runs
  `sampleVessel`: the rack or vial is found first, so nothing to fill costs nothing. No `PacketLivingSyringeDraw` is sent
  for a vessel draw (its client state reads a target entity); the draw plays a sound and says so.

**Research and the Bestiary.**
- `ClinicalBloodProgress`: `metSources` (`recordMet`, `hasMet`, `metSources`, and an `examinedSources` view), NBT
  `Met` written only when not empty. `BloodResearch.met` (met or known; Creative everything) and `recordMet(player,
  type)`, which records only a kind with a blood profile, only for a player `ClinicalBloodKnowledge.eligible` (Degree
  2, not purifying), and syncs only when new. Called from the vial and syringe entity draws, both vessel samples,
  lead-in, jar capture, opening the screen, and a birth (for the keeper).
- `BestiaryTabState`: `Kind.CREATURE` first; a five-argument `rebuild` taking the met kinds outside the roster (the
  four-argument form still works); met, read and studied counts. `BestiaryTabController` builds the met map from
  `BloodProfileData.snapshot(true)`. `BestiaryTabView`: the "Met Creatures" section, creature status keys, the counts on
  the title line, and a blood block (`BestiaryBloodBlock`) under a met creature's name and under a roster specimen's
  authored text. `BestiaryEntityPreview.hasPreview` now takes any registered kind with living attributes; creation is
  guarded and a failure is remembered for the session and falls back to the plain panel.

**Client effects.** `client/render/tile/harbinger/functional/BroodThreadRenderer`, called from both vessel renderers:
two Animus strands (`ManipulationMaterials.ANIMUS`, the Marionette tether's material, `VisceralMesh` segments) twisted
round an arc from the mate's lid to the bearer's, rising 0.75 blocks, thicker and brighter with progress, dim while
paused, with two beads running toward the bearer; and, while a young quickens, nine blood tendrils thrown out from the
cradle's glass, growing as they fade over 30 ticks. The young also swells and settles in its glass. Server side, fine
glow motes run along the arc each paid second, and the birth sends blood-cell, glow and dark-glow bursts, a
`BLOOD_SPLASH` burst of six from the lid and an `ANIMUS_IMPACT`, with a sniffer-egg hatch and a wet break. Render
bounds widen to cover the thread and the bloom. No vanilla particle, no new render type.

**Advancement and teaching.**
- Advancement `hemomancy:first_brood` "Of Your Own Keeping" (parent `kept`, granted to the keeper at a birth),
  `HarbingerAdvancementGranter.ADV_FIRST_BROOD` in the core-loop firsts (now ten).
- Liber page `the_hematic_order/pages/the_brood.json` (ordinality 21; 19 and 20 were taken), `THE_BROOD` in the core
  loop block and `CORE_LOOP_PAGES` (now 28), opened at the Third Degree, by the Specimen Vat item and by the
  advancement. `unread_humours.json` now says screens, and that a thick-hided kind still needs the syringe in glass.
  `CoreLoopTeachingClientReview` lists the page and the first.
- Lang prefixes: `container.hemomancy.specimen_vessel`, `screen.hemomancy.specimen_vessel.*`,
  `message.hemomancy.menagerie.brood.*` (states, refusals, paired, parted, born, never),
  `message.hemomancy.menagerie.quirk.*`, `message.hemomancy.menagerie.syringe_sample`,
  `bestiary.hemomancy.section.creatures`, `bestiary.hemomancy.status.met|read|studied`, `bestiary.hemomancy.creature.*`,
  `screen.hemomancy.bestiary_tab_view.creature_counts`, `advancements.hemomancy.first_brood.*`;
  `item.hemomancy.living_syringe.restricted_samples` rewritten. The Alchemist's vat inquiry does not describe the
  readout, so it is unchanged.
- Docs: `HEMOMANCY_REFERENCE.md` (the menagerie section's sampling and research paragraphs, three new paragraphs for the
  screen, the Bestiary and the brood, the Specimen Jar row, the Living Bestiary paragraph), `CORE_LOOP.md`,
  `ARCHITECTURE_STANDARDS.md` (the keeping and knowledge rows), `wiki/Blood-Systems.md`, `wiki/Clinical-Blood-Tools.md`,
  `05-menagerie-and-research.md` (a "Built in round two" list), the Track 11 ledger (five R2 P6 rows),
  `OPEN_QUESTIONS.md` §1.8 (CQ-27 to CQ-33). `source-text-impact-map.csv` regenerated.

### My calls

- **D1, the screen is a slotless menu with no payload.** Button presses use vanilla `clickMenuButton`; values travel as
  data slots, scaled to fit a short. PROTOCOL_VERSION stays 48.
- **D2, a "met" tier below read and studied.** Sampling (wild or kept), leading in, jarring, opening the screen and a
  birth meet a kind; only profiled kinds; only for a player the clinical lessons are open to, so a Degree 0 or 1 player
  gets no new attachment data. Creative meets and reads everything, so a Creative Bestiary lists every profiled kind.
- **D3, quirks are notes, not mechanics:** syringe, upkeep per Vigor point, jar or vat, rattles (hostile), forces the
  glass (hostile with 20 health or more), breeds and at what cost, will not breed, and (on a vessel's screen only) an
  inherited school. Nothing new is tracked.
- **D4, sampling in glass:** a vial is refused at no cost on a syringe-only kind; the syringe draws any kept creature
  for 3 Vigor (vial 5), under the same keeper's rights and preserved refusal; the rack is resolved before any charge.
- **D5, the brood, designed from scratch.**
  - Two living kept creatures of the same type, in vessels touching north, east, south or west at the same height (a
    vat counts by its lower half; jar, vat or mixed), with the same owner.
  - The keeper or their bloodline presses on one screen; that vessel bears. The mate is the first eligible neighbour in
    N, E, S, W order. A creature is never in two pairs.
  - Pressing a paired vessel's button parts the pair, from either side; what was paid is lost.
  - Refused: preserved, not yours, a husk, undead, tagged `menagerie_no_brood`, resting, no mate, another keeper's mate,
    a husk mate, a paired mate, a resting mate.
  - Each vessel second, after feeding, while both parents hold 75 Vigor, the brood takes `cost / 120` from the bearer's
    buffer, or from the mate's if the bearer's is short; nothing is taken otherwise.
  - Cost `max(150, 30 x maxHealth)` mL: a cow 300, a blaze 600.
  - Each parent loses 1 Vigor every 10 paid seconds, 20 at the birth, and both rest 6,000 ticks.
  - The young goes into the first empty vessel beside the bearer, then beside the mate, that it fits and that is unowned
    or the keeper's; that vessel takes the keeper as owner. With none, it waits at term at no further cost. It is a baby
    where the kind ages, starts at 40 Vigor, and is never spawned loose, never an item, never a drop.
  - Inheritance: a school both parents carry passes for certain; one only one carries passes half the time (the level's
    random); when they carry different ones, the bearer's on a roll under a half, else the mate's.
  - Breaking, lifting or emptying a vessel, a parent dying or breaking out, all part the pair on both sides.
  - Hostile kinds may breed (a blaze pair works). Only the undead and the polyp are out.
- The brood's button lives on both vessels' screens; the refusal shown is a live dry run of the press for this viewer.
- Met Creatures is the first section of the Bestiary, so a player who has met anything sees it selected first.
- An unread kind shows as many hollow phials as its Tempers (one to three): the count shows before the kinds do (CQ-33).
- The screen is 300 by 236 GUI pixels, so it fits a 1280 by 720 window at GUI scale 3. Quirks are drawn at 0.75 scale.
- The thread arcs over the lids, not between the glass centres: side by side the glass columns are an eighth of a block
  apart and a centre-to-centre thread hid inside them (the first review launch showed nothing).
- The birth's bloom is drawn by the cradle's renderer from block event 2, not a new particle or payload.
- No `PacketLivingSyringeDraw` for a vessel draw: its client state reads a target entity and a vessel has none.
- The undead check uses the vanilla `EntityTypeTags.UNDEAD` tag; a mod creature that should not breed goes in
  `hemomancy:menagerie_no_brood`.
- The `Met` set also holds kinds met before they were examined; `BloodResearch.met` counts an examined kind as met.

### Deferred, and why

- **The syringe draw animation on vessels.** `PacketLivingSyringeDraw` names a target entity and its client state reads
  it every tick; a vessel has none. A position form would change the payload layout. The draw plays a sound and says so.
- **`ScreenChromeReview` scene.** Its scenes open screens from items or the client; a vessel menu needs a staged block
  and a server open, so the vessel screen is proven by `vesselReviewClient` instead.
- **NPC lines.** No NPC names the screen or the brood; the Alchemist's vat inquiry still speaks of keeping only. The
  Liber page, the advancement and the tooltips teach it.
- **Vessel ownership for strangers** (husk take-out, pick-up, leading into someone else's empty vessel) is unchanged,
  as Track 5 left it for the owner.
- **The Bestiary preview** of generated dossiers is built and guarded, but at GUI scale 3 the detail panel is narrower
  than the preview needs (the roster's previews are hidden there too), so the review shots show the text dossier.

### Review client

`vesselReviewClient` (row in `gradle/review-clients.gradle`, harness `VesselClientReview`, property
`hemomancy.vesselReview`, user `VesselReview`, dir `build/vessel-client-review`). Drop
`{"op":"create","name":"VesselReview","seed":"1"}` on a fresh dir, `{"op":"open","name":"VesselReview"}` after; it logs
`VESSEL_REVIEW captured 18`. Launched four times; every shot of every launch was opened.
- First launch: the cost line ran under the button, the quirks had no room and never drew, the husk was a bright red
  slab off to one side, the thread was invisible (hidden inside the glass), the Bestiary tab never opened (the client did
  not know the Degree), and the birth burst was small. Fixed: a taller, wider screen; the cost line truncated and
  shorter; the husk shifted and shaded; the arc over the lids; the Degree synced in the harness; a bolder burst and the
  bloom.
- Last launch (`build/p6-review-4.log`, 18 shots): the blaze vat's screen unread (two hollow grey phials, the unread
  line), read (orange Flammeus and white Lux phials, "Examine 2 more"), and studied (the yield and six quirk lines, all
  inside the panel); a husk pig lying dark in its well; a preserved Chalybeate Snail jar ("Preserved. No one keeps it.",
  no buffer); a cow pair gestating at about half ("Quickening", "155 of 300 mL paid", "Part them"); a lone pig with the
  refusal tooltip over the disabled button; two cow vats joined by a red twisted arc over their lids beside an empty vat,
  from the front and from the side; the birth at 6, 14 and 40 ticks (blood tendrils and a splash thrown from the empty
  vat, then the calf standing small in it, "A young Cow quickens in the glass."); the Bestiary with a met-unread blaze
  under Met Creatures (hollow phials), studied (phials, properties), and scrolled to its yield and notes; a Living
  Syringe on a blaze vat ("Drew Blaze through the syringe, for 3 Vigor.").
- Judged: text fits at 1280 by 720, unread phials read as blanks not breakage, no copper, brass or silver tones (Ferric's
  phial is its grey school colour, as on the HUD), the thread and bloom read at 5 to 6 blocks, the vein field stays
  behind the text.
- Overstated in the first report: in that last launch the cow in the gestating shot was cut off by the well (its head
  hung outside the left edge), and the husk pig was a bright red slab (the death-pose hurt tint at full GUI light under a
  thin shade), not "lying dark". The verifier fixed both (see Verified).

### Numbers the owner may want to tune

- `BroodRules`: `BROOD_VIGOR` 75, `ML_PER_HEALTH` 30, `MIN_COST` 150, `BROOD_SECONDS` 120, `VIGOR_DRAIN_EVERY` 10,
  `VIGOR_DRAIN` 1, `BIRTH_VIGOR` 20, `REST_TICKS` 6,000, `YOUNG_VIGOR` 40, `YOUNG_AGE` -24,000, `INHERIT_CHANCE` 0.5,
  `MAX_SHOWN_COST` 32,000.
- `VatVigorRules.SYRINGE_SAMPLE_COST` 3 (vial `SAMPLE_COST` 5).
- `SpecimenBrood.THREAD_ARC` 0.75 blocks; `SYNC_STEPS` 20 (resend every 5%).
- `BroodThreadRenderer`: width 0.045 plus 0.045 at term, alpha 0.75 fed (0.4 paused) plus 0.25 at term, nine bloom arms
  over 30 ticks; `KeptSpecimen.quickening` lasts 30 ticks.
- `SpecimenVesselMenu` reach 8 blocks.

### For later packages

- A new vessel kind implements `SpecimenVessel` (now a `MenuProvider` with `fits(Mob)`) and calls
  `BroodThreadRenderer.draw` from its renderer.
- Read the brood through `SpecimenBrood.state` / `paidAndCost`, never the raw NBT; pair and part only through `press`
  and `sever` so both sides change together.
- A system that meets a creature calls `BloodResearch.recordMet(player, type)`; it guards eligibility and profiles.
- Phials go through `client/screen/widget/TemperPhials`.
- A held item on a living kept creature gets PASS from the block; an item that should act on a vessel does it in its
  own `useOn` (see `LivingSyringeItem.vesselAt`).
- GameTests drive the brood with `SpecimenBrood.second` directly; `serverTick` runs once a second at an offset set by
  the position.
- Counts: required GameTests 1,768 (35 in `menagerie_validation`); unit tests 5,825. The frozen registry snapshot gained
  `minecraft:menu hemomancy:specimen_vessel` on purpose.

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: "BUILD SUCCESSFUL" (`build/p6-compile-final.log`).
- `./gradlew.bat test --rerun`: "BUILD SUCCESSFUL", 5,825 tests, 0 failures, 0 errors, 0 skipped
  (`build/p6-test-final.log`; P5 had 5,812, plus `BroodRulesTest` 9, `KeptQuirksTest` 3 and one `VatVigorRulesTest`
  case). Pins changed on purpose: `CoreLoopTeachingGuardrailTest` pages 27 to 28 and firsts 9 to 10;
  `BloodAccountingGuardrailTest` allows `KeptSpecimen.java` two drains.
- `runMenagerieGameTestServer` first: "All 35 required tests passed" (`build/p6-menagerie-gt.log`).
- `./gradlew.bat runGameTestServer`: the first run failed one test,
  `everyhemomancyregistryidmatchesthefrozensnapshot` ("expected 'minecraft:menu hemomancy:stillwater_condenser', found
  'minecraft:menu hemomancy:specimen_vessel'"), the intended new menu; the line was added to
  `src/gameTest/resources/frozen/registry-ids.txt`. Then three runs in a row: "All 1768 required tests passed :)"
  (`build/p6-gt-2.log`, `build/p6-gt-3.log`, `build/p6-gt-final.log`). That is P5's 1,755 plus 13 new tests. No flake.
- The 13 new GameTests: `anEmptyHandOpensTheVesselScreenNotChat`, `aBloodHandOnAVesselFeedsAndDoesNotOpenTheScreen`,
  `theVesselMenuShowsYieldOnlyToAStudiedKeeper`, `aVialIsRefusedOnASyringeOnlyKind`,
  `aSyringeSamplesAKeptCreatureForThreeVigor` (through `gameMode.useItemOn` on the vat's top half, with a stranger, a
  preserved vat and an empty syringe), `samplingOrKeepingMeetsAKindForTheBestiary`,
  `twoFedCowsInAdjacentVatsBearACalfIntoAnEmptyVat` (exact 300 mL, a baby at 40 Vigor, parents at 68 and resting, the
  cradle the keeper's, no drops, orbs or loose cows, the advancement), `aBroodPausesUnderTheVigorMarkAndTakesNoBlood`,
  `aBroodWaitsAtTermUntilAnEmptyVesselStandsBeside` (another keeper's empty vat is skipped),
  `strangersTheDeadDifferentKindsAndRestingPairsAreRefused` (every refusal, and a second press parts),
  `breakingOrPickingUpAParentLosesTheBrood` (break, sneak lift through `gameMode.useItemOn`, and a parent dying),
  `aCalfOfParentsWithAnInheritedTemperCarriesIt`, `theBroodNeverAdvancesWithoutBloodInTheBuffers` (and no press, no
  brood). The 22 Track 5 tests pass unmodified.
- PROTOCOL_VERSION is "48" and `payload-registry.txt` was not touched. No git write command was run, only one Gradle
  process ran at a time, `AGENTS.md` is untouched, and nothing was deleted from the tree (only the review client's own
  screenshots in `build/` between launches).

### Verified

An independent verifier read the scope, this section and the package's changes (the brood, the vessel menu and view,
the screen, the syringe and vial paths, research and the Bestiary, lang, the Liber page, the advancement, the tag and
the 13 GameTests) and checked them against the three rules and the guardrails. Every lang key the new code names,
including the built ones (brood states and refusals, keeper words, quirk ids), is in `en_us.json`; no payload was added;
the only new drain is the brood's on a block buffer; no vanilla particle, literal or "tendency" was added. The brood
is paid only from living buffers and never advances on empty ones. Fixed in place:
- **The menu did the brood's work by calling the service itself.** The scope puts operations in the block entity:
  `SpecimenVessel.pressBrood(level, presser)` now owns it and the menu's button dispatches to it.
  `twoFedCowsInAdjacentVatsBearACalfIntoAnEmptyVat` now pairs through the opened screen's `clickMenuButton`, not a
  direct `press`.
- **The cow was cut off in the screen's glass.** A turning body was fitted at 1.5 times its box width, but a cow's head
  overhangs its box. It is now fitted at 1.9 times (the long-bodied factor the jar renderer uses).
- **The husk was bright red.** The death pose's hurt tint shows at full GUI light; the shade over a husk is now
  `0xC8070203`, so it lies as a dim, blood-dark shape as it does in the world.
- **The Liber page said the brood takes "more for a bigger body"** after "about two minutes", which reads as more time.
  It now says a bigger body costs more blood (the time is fixed at 120 fed seconds).

`vesselReviewClient` was relaunched once (`build/p6v-review-1.log`, "VESSEL_REVIEW captured 18"); the cow now stands
whole inside its well, the husk pig lies dark, the pig's refusal tooltip and every other shot read as before.
- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL (`build/p6v-compile.log`).
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,825 tests, 0 failures, 0 errors, 0 skipped (`build/p6v-test.log`).
- `./gradlew.bat runGameTestServer` twice in a row: "All 1768 required tests passed :)" both times
  (`build/p6v-gt1.log`, `build/p6v-gt2.log`). No flake, no golden changed.

## P7: Farming: fungal strains as crops and the rest

The rest of Track 7 (`docs/archive/tracks/core_loop/07-farming-and-water.md`): the eight spores planted as fungal strains on their own
substrates, which gives Ravenous Heartwood and the Salt-Stained Voyager Log their jobs; Lux and Flammeus catalyst crops;
tainted harvest from over-fed vanilla crops; floor stalagmites under a drip; and chum hunters chosen by the water.

### What was built

**Fungal strains.**
- `common/block/harbinger/plant/FungalStrains` is the one table: one row per strain (spore id, block id
  `<name>_strain`, `EnumBloodTemper`, substrate group, mote colour). It implements `StringRepresentable`. Its
  `Substrate` enum (`FLESH`, `BRINED`, `COMMON`) carries each group's tag `hemomancy:strain_substrates/<group>` and the
  spore tooltip key `item.hemomancy.spores.substrate.<group>`; `FungalStrains.ANY_SUBSTRATE` is the umbrella tag
  `hemomancy:strain_substrates`.
- `FungalStrainBlock extends BushBlock implements BonemealableBlock`: `age` 0 to 3 (`AGE_3`, so `BloodFedGround.ripe`
  reads it), stands only on its row's substrate tag (its own `canSurvive`, whatever the soil says about plants), no
  light needed, no collision, `SoundType.FUNGUS`, random ticks only while unripe. Its growth goes through
  `CommonHooks.canCropGrow` / `fireCropGrowPost`, so `BloodFedGrowthEvents` stays the one crop subscriber. Bone meal adds
  one stage. A ripe strain sheds `HemoParticleData.sporiticSpore` motes in its colour. `playerDestroy` of a ripe strain
  by a player with active blood grants the new core-loop first (the active-blood gate was added by the verifier). It is deliberately not a `CropBlock`: `ClochePlanting` plants any `CropBlock` item, and
  a spore in the cloche must stay an enzyme culture.
- Pure numbers in `FungalStrainRules` (`GROW_ROLL` 6, `MAX_AGE` 3, `BONUS_SPORE_CHANCE` 0.5, `BONEMEAL_STAGES` 1).
- `BlockInit` registers the eight blocks from the table into `MODELEDBLOCKS` (`BlockInit.strain(FungalStrains)`).
  Incandescent gives light 3 when ripe. `MANUAL_BLOCK_ITEMS` is now built by `manualBlockItems()` with `Map.ofEntries`
  plus the table's eight pairs (it was a `Map.of` at its 10-pair limit).
- The eight spore items in `ItemInit` are now `ItemNameBlockItem`s for their strains (same ids, rarity and stack size).
- All eight strains are in `hemomancy:needs_blood_to_grow`. `ScabDrip.isSoil` also accepts `#hemomancy:strain_substrates`,
  so a drip on a strain or its wood stamps the wood. A planting basin beside the substrate block feeds at level 2 through
  the existing `soil.relative(side)` check; a lit feeding run beside the strain does too.
- Advancement `hemomancy/first_strain_bed` ("A Bed of Strains"), `HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED` in
  the "Core loop firsts" section; `CoreLoopTeachingGuardrailTest` now expects 11 firsts.

**Salt-Stained Timber.** New `hemomancy:salt_stained_timber` (`COLUMNBLOCKS`, `RotatedPillarBlock`, map colour terracotta
white, axe tag, own loot). Recipes `recipe/salt_stained_timber_from_voyager_log.json` (voyager log + 8 `#minecraft:logs`
-> 8) and `recipe/salt_stained_timber.json` (1 timber + 1 dried kelp + 4 `#minecraft:logs` -> 5). The voyager log gets a
third tooltip line, `item.hemomancy.salt_stained_voyager_log.tooltip.culture`; its five NPC inquiries are unchanged.

**Catalyst crops.** `CatalystCropBlock extends CropBlock` (own `AGE_3`, `MAX_AGE` 3, shapes, seed by `ResourceLocation`,
a record codec). Blocks `hemomancy:glowroot` (light 4 when ripe) and `hemomancy:emberpod`, items of the same ids as
`ItemNameBlockItem` (manual block items). New `data/minecraft/tags/block/crops.json` lists both (so planting basins, the
cloche and Vital Effusion take them); both are in `needs_blood_to_grow`. `temper_catalyst/lux.json` and `flammeus.json`
gain them at worth 2. Loot: 1 unripe, 2 or 3 ripe. Recipes `recipe/glowroot.json` (carrot + glowstone dust) and
`recipe/emberpod.json` (nether wart + blaze powder).

**Tainted harvest.** `TaintedMeatItem` now takes its tooltip key as a constructor argument; `hemomancy:tainted_harvest`
is a second instance (food 3 / 0.3). `blood_food/tainted_harvest.json` (12 mL per nutrition point), and it is in
`gouge_catalyst/feed.json` at 1,200 ticks. New global loot modifier `hemomancy:tainted_harvest`
(`common/loot/modifier/TaintedHarvestModifier`, registered as `LootModifierInit.TAINTED_HARVEST`, data
`loot_modifiers/tainted_harvest.json` with `crops`, `item` and `chance` 0.08, appended to
`data/neoforge/loot_modifiers/global_loot_modifiers.json`). It adds one Tainted Harvest to a ripe `#minecraft:crops` block
that is not `needs_blood_to_grow` when `BloodFedGround.level` at it is exactly 2, on the loot context's random. The cloche
is excluded by the Mycelial Lantern standing at the loot origin. Pure rule `BloodFedGrowthRules.taints`, constant
`TAINT_CHANCE`.

**Floor stalagmites.** In `ScabDrip`: new `Landing.FLOOR_SPIKE`, tag `hemomancy:scab_floor` (`ScabDrip.SCAB_FLOOR`:
blood-scorched scab, `#minecraft:base_stone_overworld`, `#minecraft:base_stone_nether`, venous, polished and infested
venous stone), `isFloorSpike`, and `raiseFloorSpike`. A drop that first hits an upward scab spike lengthens it; a drop on
bare scab floor with a sturdy top raises one; both on `BloodFedGrowthRules.floorSpikeMayRise` (chance 0.1, at most 3
blocks, at least `FLOOR_SPIKE_GAP` 2 empty blocks under the hanging tip). The hanging spike's own growth now also stops 2
blocks above a floor spike, so the gap holds from both sides. `land` takes the tip and the drip's random.

**Chum hunters.** `WorldAnswerRules.HunterWaters {OPEN, HOME, SWAMP}`, `hunterWaters(home, swamp)` and `hunterDepth`
(2, or 1 for a swamp). `WorldAnswer.drawHunter` keeps its six-argument form, which reads the biome
(`#hemomancy:chum_home_waters` then `Tags.Biomes.IS_SWAMP`, `WorldAnswer.hunterWatersAt`), and gains an overload taking
`HunterWaters`. HOME spawns a Brined Votary, SWAMP a Bog Revenant on the nearest dry bank cell (else in the water), OPEN a
Drowned as before. The cap counts all three through the new entity tag `hemomancy:chum_hunters`
(`WorldAnswer.CHUM_HUNTERS`). A draw shows two `CHUM_BLOOM`s, a `BLOOD_SPLASH`, an `ANIMUS_IMPACT` and blood-cell and
glow bursts at the spot, with a low elder-guardian curse sound. No new `Form`.

**Assets.** New version-controlled generator `tools/model_export/fungal_strains.py` paints and writes: 32 strain stage
textures (`textures/block/strain/<name>_stage<n>`), 8 crop stage textures (`textures/block/crop/`), the timber's side and
top, item icons for glowroot, emberpod and tainted harvest; blockstates, `cross` and `crop` models with `cutout`, the
timber's column models and item model, item models, the strain, timber and crop loot tables, the four recipes, and the
block and item names (inserted beside related keys, the lang file's order kept). Strain shapes by group: stacked bracket
shelves for flesh, salt-crusted buttons for Brined, thin capped stems for the rest.

**Lang** (no `Component.literal`): `block.hemomancy.<strain>_strain` x8, `block.hemomancy.salt_stained_timber`,
`block.`/`item.hemomancy.glowroot` and `.emberpod`, `item.hemomancy.tainted_harvest` and `.tooltip`,
`item.hemomancy.spores.substrate.flesh|brined|common` (added in `ItemTooltipEvents.appendSporeSubstrateTooltip`),
`item.hemomancy.salt_stained_voyager_log.tooltip.culture`, `material.hemomancy.ravenous_heartwood.desc` (rewritten to give
its job), `material.hemomancy.salt_stained_timber.desc`, `.glowroot.desc`, `.emberpod.desc`, `.tainted_harvest.desc`,
`advancements.hemomancy.first_strain_bed.title` / `.description`.

**Atlas and tabs.** `MaterialsData` and `MaterialAtlasSpec` gain aligned entries: `salt_stained_timber` (fungal ecology,
D3, at 930,524) and `glowroot`, `emberpod`, `tainted_harvest` (gourds and vessels, D2, at y 654). Creative tabs:
`salt_stained_timber` after the voyager log in `CreativeTabSection.BRINED_COURT`; `tainted_harvest`, `glowroot`,
`emberpod` after `tainted_meat` in `CreativeTabGroup.FOOD`. Strain blocks have no item of their own.

**Liber.** New page `the_hematic_order/pages/strain_beds.json` ("Strain Beds", ordinality 22), registered as
`LiberEntryDefinitions.STRAIN_BEDS`, the 29th core-loop page; opened at the Third Degree (`LiberKnowledgeHelper`), by the
eight spores, Ravenous Heartwood, the timber, glowroot, emberpod and Tainted Harvest, and by the strain-bed first.
`fed_ground` points on to it; `chum` and `the_world_answers` say which hunter answers by water.

**Docs.** `07-farming-and-water.md` (status, the Salt-Stained Log correction), `CORE_LOOP.md` (Farming row, 29 pages and
11 firsts), `HEMOMANCY_REFERENCE.md` (a "Strain beds" block in Farming and water, the Hunters bullet in chum, the
scavenger paragraph and config row, the Voyager Log, Ravenous Heartwood and Brined Votary rows), `LORE_REFERENCE.md`
(one note on the Votary answering blood and the salt's culture), `ARCHITECTURE_STANDARDS.md` (a Fungal strains row, the
hunter sentence in the chum row), `wiki/Blood-Systems.md`, the Track 11 ledger (four R2 P7 rows), `OPEN_QUESTIONS.md`
(CQ-34 to CQ-40). The impact map was regenerated.

No payload, attachment, menu or block entity was added. `PROTOCOL_VERSION` is still 48 and `payload-registry.txt` is
untouched.

### My calls

- The track doc's "Salt-Stained Log" is not a block. I read it as the lore item `salt_stained_voyager_log` and made it the
  inoculant of a new block, Salt-Stained Timber, the Brined substrate (CQ-36).
- The renewable timber recipe gives 5: the four inoculated logs plus the seed timber returned. Timber is not in
  `#minecraft:logs`, so it cannot feed its own recipe as a log.
- Substrate groups (CQ-35): flesh = Vivacious, Fervent, Ruinous on Ravenous Heartwood; Brined = Neurotic, Frigid, Ferric
  on timber; common = Incandescent, Umbral on podzol, mycelium, erythrocytic mycelium and mycelium erythrocytic dirt.
- A strain is a `BushBlock`, not a `CropBlock`, so the cloche keeps culturing spores and First Culture is untouched. A
  strain does not grow under the bell; the cloche only grows crops.
- Strains need no light. Their own roll is 1 in 6 a growth check; fed ground gates them like the gourd (never unfed, a
  third of the checks under a drip, all of them beside a fresh basin or feeding run). Bone meal adds one stage on fed
  ground. Ravenous Heartwood is not self-feeding (living blood rule).
- Strain harvest: the planted spore back, ripe a second at 50%. No new harvest item; spores stay worth 5 as catalysts and
  still culture enzymes (CQ-39 notes the economy shift).
- The strain-bed first is granted in code when a player cuts a ripe strain, not by the vanilla `placed_block` trigger:
  placing proves nothing about blood, and the guardrail wants core-loop firsts granted where the act happens.
- Catalyst crops: only Lux (glowroot) and Flammeus (emberpod), worth 2, as the doc says "first"; both need fed ground,
  both are their own seed (1 unripe, 2 or 3 ripe). Ripe glowroot gives light 4. Incandescent strains give light 3 ripe.
- Tainted harvest: 8%, only at level exactly 2 (a fresh basin or a feeding run). Level 1 and the cloche never taint. A
  ripe crop broken by anything (a player, a villager, water) can taint. Item: food 3/0.3, 12 mL per nutrition point (less
  than tainted meat's 20, as it is plant food), gouge feed 1,200 ticks (a third of bone meal).
- Floor spikes: chance 0.1, height 3, gap 2; any `scab_floor` block with a sturdy top raises one, including plain stone.
  A drop landing on a floor spike feeds nothing. The hanging spike now also stops 2 blocks above a floor spike (a small
  change to Track 7's spike growth, only where a floor spike stands).
- Chum hunters (CQ-34): home waters beat swamp. A Bog Revenant walks, so it is set on the nearest dry bank cell within 3
  blocks, and a swamp pond only needs one block of water. The cap of 1 counts all three kinds. Drawn hunters are
  `MobSpawnType.EVENT` and despawn normally (neither entity marks EVENT spawns persistent).
- The hunter cue: two blooms, a blood splash and the Animus impact from the toolkit, plus blood cells and glow; the
  elder guardian curse sound at low volume and pitch. No new `Form`.
- Atlas placement: timber in fungal ecology at D3, the crops and Tainted Harvest with the gourds at D2.
- Lang keys for the five Liber pages stay literal English in the page JSON, as every HutosLib page does.

### Deferred, and why

- **Catalyst crops for Animus, Mortem, Ductilis, Congeatio, Ferric and Tenebris** (CQ-38): the doc asks for Lux and
  Flammeus first, and the eight strains already give every school a renewable catalyst (spores, worth 5).
- **NPC lines** for strains, the timber, the crops and the hunters: no lesson-tree package yet; the voyager log's five
  inquiries were deliberately left unchanged.
- **The swamp and home-waters biome reading** (`hunterWatersAt`) is proven only by the pure rule's unit test and by the
  GameTest checking that the test world's plains read OPEN; a GameTest world has no reef or swamp.
- **JEI**: no new category. The four recipes are ordinary crafting and show in JEI's crafting view; the cloche's crop
  growing is not shown, as before.

### Review client

`farmingReviewClient` (same row, dir `build/farming-client-review`, user `FarmReview`), harness `FarmingClientReview`
with five new shots; it now logs `FARMING_REVIEW captured 16`. Launched twice; every new shot opened and judged.
- `strain-bed`: all eight strains ripe on Ravenous Heartwood, Salt-Stained Timber, podzol and mycelium, fed basins behind,
  and a row of Fervent at ages 0 to 3 in front. The three shapes read as fungi at 16 px.
- `strain-close`: the flesh strains' stacked bracket shelves on heartwood, the Brined buttons (yellow, ice blue, rust gill
  tints under salt caps) on raised timber.
- `catalyst-crops`: ripe and young emberpod and glowroot in fed basins, glowroot under a cloche.
- `floor-spike` (two shots): two hanging spikes under basins, floor spikes below them two blocks short, a drop in flight.
- `chum-hunter` (three shots): a Brined Votary rising in a strong pool's bloom (the `HunterWaters.HOME` overload), the
  splash and impact tendrils at once, then the bloom spread over the pond.
What the launches caught: the first bracket strains read as fir trees, then as leaves (fixed twice in the generator,
now overlapping shelves with no stem); Tainted Harvest's first icons read as a stick and a trident (now a bruised,
blood-veined root); the timber's salt was a birch-like blotch (now sparse crust); the hunter cue was only a faint bloom
in its first frame (the splash and impact were added); the floor-spike framing was too close.

### Numbers the owner may want to tune

- `FungalStrainRules`: `GROW_ROLL` 6, `BONUS_SPORE_CHANCE` 0.5 (the 0.5 also lives in each strain's loot table),
  `BONEMEAL_STAGES` 1.
- `BloodFedGrowthRules`: `TAINT_CHANCE` 0.08 (also `loot_modifiers/tainted_harvest.json`), `FLOOR_GROW_CHANCE` 0.1,
  `MAX_FLOOR_SPIKE` 3, `FLOOR_SPIKE_GAP` 2.
- `WorldAnswerRules`: `HUNTER_DEPTH` 2, `SWAMP_HUNTER_DEPTH` 1 (`HUNTER_POOL_STRENGTH`, `HUNTER_CAP` unchanged).
- Catalyst worth 2: `temper_catalyst/lux.json`, `flammeus.json`. Crop yield in `loot_table/blocks/glowroot.json` and
  `emberpod.json` (generator). Timber recipe counts (generator). Tainted Harvest food in `ItemInit`, blood in
  `blood_food/tainted_harvest.json`, gouge feed in `gouge_catalyst/feed.json`.
- Light: ripe glowroot 4, ripe Incandescent strain 3 (`BlockInit`).

### For later packages

- A new strain is a row in `FungalStrains` plus a spore item; never name a `<x>_strain` block elsewhere. Substrates are
  tags. Rerun `tools/model_export/fungal_strains.py` after changing palettes, shapes, loot, recipes or names; it inserts
  lang keys beside related keys and keeps the file's order.
- Strains and catalyst crops grow through the crop hooks: do not add a second `CropGrowEvent` subscriber.
- `ScabDrip.land` now takes the tip and the random; `Landing.FLOOR_SPIKE` is new; a floor spike blocks hanging growth 2
  blocks above it. `player-block-edit-allow-list.txt`: `ScabDrip` 2, `FungalStrainBlock` 2.
- Loot GameTests: neighbouring seeds give near-identical first floats from Java's random; spread them
  (`FarmingGameTests.spread`). My first strain loot test read 199 single and 1 double before this.
- `TemperValidationGameTests` pins the catalyst table at 28 items now.
- `RegistrationGameTests.blockItemExceptionsKeepTheirOwners` expects the strain blocks, glowroot and emberpod as manual
  block items.
- Goldens changed on purpose: `frozen/registry-ids.txt` (10 blocks, 4 items, the `tainted_harvest` loot modifier
  serializer) and `frozen/creative-tab-order.txt` (Tainted Harvest, Glowroot, Emberpod after Tainted Meat; the timber after
  the voyager log). Regenerated with `HEMOMANCY_FROZEN_SNAPSHOT_OUT` and diffed line by line; nothing else moved.
- Counts: required GameTests 1,775 (7 new: 6 in `farming_validation`, 1 in `daily_loop`); unit tests 5,832 (7 new).

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,832 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer` twice in a row after the last code change: "All 1775 required tests passed :)" both
  times. Before that, one run with the golden output variable set (1775 passed) and one first run that failed 4: the two
  goldens (intended), the strain loot test's seed correlation (a test bug, fixed with spread seeds) and the catalyst-count
  pin (updated to 28). The builder saw no flake, but there was one: see "Verified" below.
- `./gradlew.bat runFarmingReviewClient`: "FARMING_REVIEW captured 16", launched twice, every new shot opened.
- No git write command was run, only one Gradle process ran at a time, `AGENTS.md` is untouched and nothing was deleted
  from the tree (only build screenshots were cleared between launches).

### Verified

An independent pass read the scope, the code, the data, the tests and the docs, and opened the review client's new shots
(`build/farming-client-review/screenshots`, `farming-5` to `farming-9`). The strain beds read as fungi, the Brined
buttons as salt-crusted, the crops and floor spikes are clear, and the hunter's rise is bold and made of toolkit forms
only. No payload, vanilla particle, literal, "tendency", blood payment or player block edit was added, and the golden
diffs are the new ids and tab entries only. The verifier found and fixed three things:

- **A flake the floor spikes caused.** The verifier's first GameTest run failed Track 7's `aFedSpikeGrowsLonger` ("a fed
  spike never grew in 200 drips"). Its drip lands on the stone test floor, which is `#hemomancy:scab_floor`, so the
  drops also raise a floor spike on `level.random`. Once that spike reaches two blocks, the new two-block gap stops the
  hanging spike from ever growing, so the test failed whenever the floor spike won the race. Fixed in the test, not the
  rule: it now drips onto glass, which raises nothing, on a seeded random. The floor-spike test already covers what
  happens over stone.
- **The strain-bed first went to anyone.** Cutting a ripe strain granted the Degree 3 advancement, and with it the Liber
  page, to any player, Unstained included. It now needs active blood (`volume.isActive()`), like the other core-loop
  firsts, which only Harbinger acts reach. `aRipeStrainReturnsItsSporeAndSometimesAnother` now also checks that a player
  without active blood and an unripe cut earn nothing. `HEMOMANCY_REFERENCE.md` was updated to match.
- **The cloche exclusion had no test.** `anOverFedCropCanTaintItsHarvestAndADripFedOneNeverDoes` now puts a Mycelial
  Lantern on ground that a fresh basin over-feeds (checked to read level 2) and asserts that 400 ripe-wheat harvests at
  the lantern's position never taint.

Left as the builder recorded them: the other six catalyst crops (CQ-38; the track doc says Lux and Flammeus "first"),
the missing NPC lines, and the biome-reading hunter path, which is proven only by the unit rule. The swamp hunter test
does not check that the Bog Revenant lands on the bank; in that pool the bank is often out of reach, and then it spawns
in the water as documented.

Final checks, run one at a time: compile BUILD SUCCESSFUL; `./gradlew.bat test` 5,832 tests, 0 failures, 0 errors;
`./gradlew.bat runGameTestServer` twice in a row after the last change, "All 1775 required tests passed :)" both times.

## P8: Ground network: dark field, vein travel, furnaces

The rest of Track 6 (`docs/archive/tracks/core_loop/06-ground-network.md`): Blood Eclipse's field of dark, hot runs burning furnaces,
Earthen Vein travel carried by living veins with the D2 Rooted lesson retuned, a JEI category for gouge catalysts, and
gouges that hold water (the doc's "permanent until removed"). No payload, attachment, block, item, entity or shader was
added. `PROTOCOL_VERSION` is still 48; `payload-registry.txt`, `frozen/registry-ids.txt` and
`frozen/creative-tab-order.txt` are unchanged and no golden was touched.

### What was built

**Hot runs burn furnaces.**
- New `common/world/ground/FurnaceHeat`: `cooks(level, furnace)` (moved unchanged from `FurnaceVeinsShaping`, which now
  delegates) and `stoke(editor, level, pos, burn)`. An unlit furnace is lit through `PlayerBlockEdits.replace` before
  its burn time is touched; a lit one is checked with `ManipulationCombatHelper.mayAlter` (game mode and
  `mayInteract`, the rule `replace` applies) without an edit. Only then is `litTime`/`litDuration` raised to `burn`,
  never lowered. A furnace with nothing it can cook is never lit. The existing `mixin/core/FurnaceFuelAccessor` is
  cast, no second accessor.
- `FurnaceVeinsShaping.pulse` calls `FurnaceHeat.stoke`, so there is one copy. Its bank, visuals and `burnTicks` are
  unchanged (`FurnaceVeinsBurnTest` green). One behaviour fix rides along: the Shaping used to raise a furnace's burn
  time even when the lighting edit was refused; now a refused furnace is skipped.
- `GroundNetwork.heat` stokes all six neighbours of every hot cell on every effect tick (owner online, alive and in
  the level only) with `GroundRunRules.HOT_FURNACE_BURN = EFFECT_INTERVAL + 5` (15 ticks). Javadocs of
  `GroundFunction.HOT`, `GroundNetwork` and `SwitchableLights` updated.

**The field of dark (Tenebris, Blood Eclipse's Absorption).**
- `EclipseSnuffShaping.snuff` returns the block light it took (read before the edit with `getLightEmission`), -1 when
  refused. `pulse` sums `DarkFieldRules.charge` per light, scales by the pulse's `workScale` through
  `DarkFieldRules.fieldTicks`, and calls `DarkFields.feed(player, centre of the aimed block, ticks)`. Nothing taken,
  no field; the Shaping still claims only where something can be snuffed.
- New `common/world/ground/DarkFieldRules` (pure) and `DarkFields` (server session service, `Map<UUID, Field>` with
  `record Field(dimension, centre, until)`), cleared and logout-removed through `TransientServerStateEvents`.
- Effects: a new `LevelTickEvent.Post` handler in `GroundNetworkEvents` calls `DarkFields.tick(level)`; every 10 ticks
  each field in that dimension lifts if spent, gives every `Mob` inside `OBSCURED` for 30 ticks (the
  `GroundNetwork.darken` pattern) and every 20 ticks resends its cue.
- Hiding: `SchoolAwareness.canPerceive` treats a body inside any field as `VEILED` (`DarkFields.covers`, server only,
  returns at once while no field hangs). So a creature more than 4 blocks off cannot target or keep a target standing in
  the dark; Illuminated bodies and scripted bosses are still seen. Players are not given Obscured; their dark is fog.
- Visual, no payload or shader: `ManipulationVisuals.Form.UMBRAL_FIELD` appended at the end (ordinal-encoded), material
  `TENEBRIS`, position-keyed in `ManipulationVisualRenderer.accept` (a resend refreshes, a burst with 0 ticks retires).
  `LuxUmbraGeometry` draws it on the umbra material as an overlay: a dark `POOL` wash on the floor of the light's cell,
  four low haze cards, six slow wisps drifting in, a broken violet-cored `SLASH` rim in six arcs and four ink strokes
  drawn inward (252 vertices, inside `LuxUmbraGeometryTest`'s 320 budget).
- Fog: new `client/event/UmbralFieldFog` (the `VelorumGloamFog` pattern; `RenderFog` in `FOG_TERRAIN` mode and
  `ComputeFogColor`), depth from the new `ManipulationVisualRenderer.umbralDepth(viewer, time)` (deepest live cue by
  `DarkFieldRules.depth`), eased over 5 ticks, near 3 and far 14 at full depth, colour (0.075, 0.05, 0.115), scaled by
  the darkness-effect setting, skipped when the far plane is already closer than 14.
- The snuff, the focal moment: per light taken, 16 dark glow motes and 5 violet-edged ones at the light, an
  `UMBRA_MIST` burst sized by the light, a short `BloodFlowVisuals.from` stream (style `EXTRACTION`) from the light into
  the caster, and a line of dark motes running toward the hand; a low sculk-catalyst bloom under the extinguish sound.
  No vanilla particles.

**Earthen Vein travel reads living veins.**
- `GroundNetwork.livingRunJoins(level, a, b)`: walks living vein cells out from `a` as a run joins (faces and edges,
  at most `MAX_RUN` cells, never loading a chunk) and asks whether one touches `b` (new pure
  `GroundRunRules.joined`).
- `EarthenVeinTravelRules.LIVING_VEIN_COST = 125` and a five-argument `cost(..., carried)`; the four-argument form is
  the uncarried one.
- `EarthenVeinTravelManager`: private `costFor`/`carried` used by both `destinations(...)` (which now takes the source
  level) and `select(...)`, so the display and the charge agree. A Speculum's temporary origin is never carried. A
  carried selection shows `message.hemomancy.earthen_vein_travel_manager.carried_by_the_living_vein`.
- The Rooted lesson (Vicar, one speaker): `hemomancy.vicar.votary.rooted_vein.recovery` keeps every pinned phrase and
  adds that the Earthen Vein is the first living vein, an opening to travel by, and that from the Third Degree tissue
  grown from a Vein Node joins two openings for a crossing of 125. `hemomancy.vicar.item_inquiry.earthen_vein.line1|2`
  no longer say veins in sequence carry workshop blood (they contradicted living blood and CL-14).
  `material.hemomancy.earthen_vein.desc` rewritten. The three `about` fields in
  `assets/hemomancy/dialogue_briefs/vicar_succession.json` updated. No node or option changed, so the dialogue
  fingerprints and `EquipmentLessonDialogueTest` are untouched.

**JEI: Gouge Catalysts.**
- `GougeCatalysts.listed()` (named items plus each school catalyst where the Temper table is loaded, timed as `of`
  times it, sorted by item id); `syncSnapshot` now uses it. `GougeCatalysts.onClientTable`, a plain `Runnable` hook run
  at the end of `receiveSync` (common names no client or compat class).
- New `compat/jei/GougeCatalystRecipeCategory` with its `Display(function, entries)` record, one page per function
  with catalysts (seven), in the `JeiPanel` chrome: the Projection hand ("projected"), a slot cycling the function's
  catalysts ("off hand", tooltip "Lights the run for m:ss"), the progress arrow, a pulsing swatch of
  `GroundFunction.color()` (dark is an empty outlined groove), the function's name and one wrapped line of what the run
  does. Catalysts: `blood_projection` and `catalyst_feeder`. A remote mirror arriving after JEI started hides the old
  pages and adds the new through the runtime (only the pages that changed, since JEI hides by equality).
- Lang: `hemomancy.jei.gouge_catalyst` (title), `.projected`, `.off_hand`, `.projection.tip`, `.time`, and one line per
  function (`.light`, `.hot`, `.point`, `.cold`, `.pull`, `.feed`, `.dark`). Added to `JeiClientReview.TYPES`.

**Gouges hold water.**
- `BloodGougeBlock` (so `LivingVeinBlock`) gains `WATERLOGGED` (default false) and implements
  `SimpleWaterloggedBlock` the glow-lichen way: `getFluidState`, `propagatesSkylightDown`, and `updateShape` scheduling
  the water tick. A shapeless gouge with a `LiquidBlockContainer` holds flowing water back (flowing water cannot be
  placed into it); a source bucketed onto it, or two sources beside it, lie over it. `updateShape` and
  `GroundMarkBlock.without` leave water, not air, when the last face of a waterlogged cell goes, so Absorption filling
  one in leaves the water (`tryAbsorb` replaces with water through `PlayerBlockEdits`). `asVein`/`asGouge` carry the
  property. Stains and Lux traces still wash.
- `GroundNetwork.markAt` also finds a mark when the ray stops at the water lying over it (the verb ray clips fluids):
  the mark is in the struck cell. Without this, Absorption and Projection could not reach a waterlogged gouge.
- `WorldEffectDispatcher` now draws `GroundRunRenderer` at `AFTER_CUTOUT_BLOCKS`, with the camera's model-view matrix
  applied (the block-layer stages run before the level renderer pushes the camera onto the model-view stack). Drawn
  after translucent blocks as before, a lit strip under water was hidden by the water's depth; now water and glass blend
  over it. Every rite visual still draws over it later.
- The gouge and vein blockstates are multipart on the faces only, so every state still resolves (the generator
  `tools/model_export/ground_network.py` needed no change).

**Teaching and docs.**
- Lang: Blood Eclipse's Absorption tooltip (`hemomancy.manipulation.blood_eclipse.shaping.absorb`) names the field;
  the Vicar lines, atlas line, carried-travel line and JEI keys above.
- Liber (amended, no new page; `CORE_LOOP_PAGES` stays 29): `the_ground_network` (furnaces, gouges hold water),
  `living_veins_and_basins` (the 125 crossing), `ground_shapings` (the field of dark).
- Docs: `06-ground-network.md` (the four items marked built, the water ruling), `CORE_LOOP.md`, `HEMOMANCY_REFERENCE.md`
  (new dated section at the top, the Track 6 section, the Blood Eclipse rows, the Earthen Vein cost row, the renderer
  stage), `MANIPULATIONS_AND_CASTING.md`, `11-progression-teaching-tests.md` (the D2 Rooted row done),
  `wiki/Blood-Systems.md`, `ARCHITECTURE_STANDARDS.md` (one line: `FurnaceHeat.stoke`, `DarkFields`),
  `OPEN_QUESTIONS.md` 1.8 (CQ-41 to CQ-44, header line). `gradle/review-clients.gradle` row comment.
  `NPC_DIALOGUE_AND_TRADE.md` does not quote the lesson, so it is unchanged. The source-text impact map was
  regenerated and did not change.

### Tests

- JUnit: new `DarkFieldRulesTest` (6 cases: charge by light, the dry floor, the cap, radius bounds, join-or-move, depth
  across the edge); `EarthenVeinTravelRulesTest.aLivingVeinCarriesACrossingForAFlatPriceWithinOneDimension`;
  `GroundRunRulesTest.cellsJoinByFaceOrEdgeButNotCorner`.
- GameTests in `ground_network_validation` (11 new): `aHotRunBurnsTheFurnacesBesideIt` (furnace, smoker, blast furnace
  burn and cook; an empty furnace stays cold), `aHotRunLightsNoFurnaceWhereItsOwnerMayNotBuild` (adventure owner),
  `aFurnaceGoesOutWhenTheRunGoesDark` (within 40 ticks), the ownerless hot-run test extended (a loaded furnace stays
  unlit with `litTime` 0), `theCatalystTableListsEveryFunction` (every function but the bare run listed; glowstone dust
  lights, coal heats, a compass points, an ink sac darkens), `bloodEclipseHangsAFieldOfDarkWhereItTookTheLight` (four
  candles: 240 ticks, radius 3.6, at the candle, a husk inside Obscured), `aCreatureFarOffLosesSightOfWhoeverStandsInTheField`
  (a husk 14 blocks off loses the caster standing in the field and cannot retarget; another keeps a player outside),
  `aFieldOfDarkLiftsOnTime` (one candle: 60 ticks), `snuffingNothingHangsNoField`, `aDryPullHangsAShorterFieldNeverNone`
  (180 of 240), `aGougeHoldsBackFlowingWater` (a walled source whose only way out is the gouge; still there and dry after
  60 ticks, then lights), `aWaterloggedGougeFilledInLeavesItsWater`.
- GameTests in `earthen_vein_travel_validation` (2 new): `aLivingVeinBetweenTwoOpeningsCarriesTheCrossingCheaply`
  (display shows 125 and the opening takes exactly 125, then is sated) and
  `openingsAGougeOnlyJoinsCostWhatTheDistanceDoes` (one cell of plain gouge in the run: 500).
- Every new GameTest waits on conditions, uses its own players and mobs, and clears what it builds.

A flake found and fixed during the package: `aDryPullHangsAShorterFieldNeverNone` passed its first run and failed the
second with "a dry field hung 0 ticks". Cause: `GameTestHelper.onEachTick` registers into `GameTestInfo`'s
`Object2LongOpenHashMap<Runnable>`, which runs per-tick tasks in hash order, so my recorder sometimes ran before the
caster's Absorption tick that hung the field, and the sequence (which runs after all per-tick tasks) then passed in that
same tick with nothing recorded. The two field-length tests now read the field inside the sequence's own condition. The
first field test had the same latent bug and got the same fix.

Two first-run failures that were test or code bugs, not flakes: the water test's source sat in the open, where water
flows only toward the nearest drop, so it never reached the gouge (now walled in); and Absorption could not reach a
waterlogged gouge until `markAt` learned to read the struck water cell.

### My calls

- **The field hides players too (CQ-41).** Anyone standing in a field, the caster included, is veiled from creatures
  more than 4 blocks off, by the same rule as `VEILED`. It is stealth on a cheap Absorption Shaping; the owner may want
  a cost or a caster-only limit.
- **Field numbers.** 20 ticks per level of light taken, at least 40; at most 2,400; radius 3 + remaining/400, 3 to 8;
  soft fog edge 1.5 blocks. A dry pull hangs `workScale` of the time (0.75 dry), never under a quarter. One field per
  player; a snuff within the current radius adds time and keeps the centre, one farther off starts a new field and the
  old lifts. Creatures inside get Obscured for 30 ticks, refreshed every 10.
- **Session state, not saved.** A restart lifts every field; a client that arrives mid-field sees it at the next
  one-second resend. Chosen to keep the protocol at 48.
- **The field's centre** is the centre of the block the Shaping is aimed at, not each light's own block.
- **Furnaces per hot run (CQ-43).** Every furnace beside any hot cell, no cap, 15 ticks of burn a beat, the owner online,
  alive and in the level. Together with CQ-7's open half a coal-stocked feeder runs a furnace bank while the owner is
  merely in the level. Fuel in the slot is not burned while the run holds.
- **Carried travel (CQ-42).** A flat 125 mL between two of the player's stented Earthen Veins that one living run
  joins (at most 64 cells, faces and edges); nodes are not destinations; a Speculum's temporary origin is never carried.
- **Gouges block water like lichen (CQ-44).** A shapeless, almost invisible mark stops a stream. Water poured onto one
  lies over it. Stains and Lux traces still wash.
- **Ground runs draw before translucent blocks.** Moving `GroundRunRenderer` to `AFTER_CUTOUT_BLOCKS` was needed for a
  lit run under water to show; it also makes stained glass in front of a run blend correctly.
- **The snuff's stream** uses `BloodFlowVisuals.from` with the `EXTRACTION` style (the Mortem flow material), the
  nearest existing toolkit stream toward a caster; there is no Tenebris flow style.
- **Vicar lines** written to his sheet without the owner's line-by-line review; the six pinned phrases are kept. The
  inquiry's line1 ends on a question that wants an answer ("How many have you claimed so far?"; the verifier replaced a
  self-answered rhetorical question that broke his sheet).
- **JEI page order** follows `GroundFunction` (light, hot, point, cold, pull, feed, dark); the bare run has no page.

### Deferred, and why

- No first-field or first-carried-crossing advancement: the core-loop firsts count of 11 is pinned and a later package
  owns the missing advancements. The Liber, tooltips and the Vicar teach them.
- The JEI mirror path on a dedicated server (the gouge table arriving after JEI starts and being re-added through the
  runtime) is unproven: an integrated client skips the channel, so no review client or GameTest here exercises it.
- The fog's interplay with the three other `RenderFog` handlers (Velorum's gloam, the school darkness, rite air) is
  guarded only by the far-plane check; it was not seen inside a Saint fight or a rite.
- A two-client look at another player's field (the cue and fog are per client from the same resent cue) was not run.

### Review client

`groundNetworkReviewClient` (dir `build/ground-network-client-review`, user and save `GroundReview`): harness
`GroundNetworkClientReview` gained five shots and two eclipse captures; it logs `GROUND_NETWORK_REVIEW captured 22`.
Launched six times; every new shot opened and judged.
- `furnaces`: a hot run with a lit furnace, smoker and blast furnace behind it, each cooking, and the empty furnace at
  the end cold.
- `eclipse` at 10, 17, 25, 60: the candles go out with dark plumes, a dark stream from each candle into the hand, the
  field's rim appearing, then the caster standing in the fog.
- `field-outside`: a lantern-lit room with the field over snuffed candles: a dark wash and a broken violet rim, the room
  readable through it, no wall.
- `field-inside`: violet-black fog closing in, lanterns at the edge still visible; not black.
- `vein-travel`: two Earthen Veins joined by a living vein, the travel display open and the marker's tooltip reading
  "125 mB" (the harness turns the camera onto the first marker and logs its price).
- `gouge-water`: a pool walled on three sides whose fourth side is a lit gouge run; the water stops at the run, the
  floor beyond stays dry, and a short lit run shows through the water under the pool.
What the launches caught: the field from outside was nearly invisible at first (wash 0.38, wisps 0.3; raised to a 0.72
wash, 0.5 wisps and four low haze cards); the snuff read as a small puff (mist doubled and the stream to the hand
added); the fog was close to black (colour lifted); the first water shot flooded the room and hid the run; and lit runs
under water were hidden entirely (water writes depth before the runs drew), which led to the renderer stage move; the
first try at that stage drew nothing until the camera matrix was applied.

`jeiReviewClient -PjeiReviewOnly=gouge_catalyst -PjeiReviewPages=6` (dir `build/jei-client-review`, user `JeiReview`):
all seven pages, 1/7 to 7/7. The first launch had the "projected" and "off hand" captions overlapping; the slots were
spaced (hand x 14, input x 60) and relaunched.

### Numbers the owner may want to tune

- `DarkFieldRules`: `TICKS_PER_LIGHT` 20, `MIN_CHARGE` 40, `MAX_TICKS` 2,400, `MIN_RADIUS` 3, `MAX_RADIUS` 8,
  `TICKS_PER_BLOCK` 400, `SOFT_EDGE` 1.5, `DRY_FLOOR` 0.25.
- `DarkFields`: `WORK_INTERVAL` 10, `OBSCURED_TICKS` 30, `RESEND_INTERVAL` 20. The 4-block sight floor is
  `SchoolStateRules.canPerceive`'s, shared with Veiled.
- `UmbralFieldFog`: `NEAR` 3, `FAR` 14, colour, `EASE` 5.
- `GroundRunRules.HOT_FURNACE_BURN` 15.
- `EarthenVeinTravelRules.LIVING_VEIN_COST` 125.
- Visual weights in `LuxUmbraGeometry`'s `UMBRAL_FIELD` case and `EclipseSnuffShaping.drawIn`.

### For later packages

- A furnace burned from outside goes through `FurnaceHeat.stoke`; do not set `litTime` before the lighting edit.
- `DarkFields.covers` is read every creature tick through `SchoolAwareness.canPerceive`; keep it a short loop.
- `ManipulationVisuals.Form` gained `UMBRAL_FIELD` at the end; append after it.
- `BloodGougeBlock` states now carry `WATERLOGGED`; anything that rebuilds a gouge or vein state from scratch must copy
  it (as `asVein`/`asGouge` do), and a mark-finding ray may stop at the water over a waterlogged gouge (`markAt` handles
  it).
- `GroundRunRenderer` draws at `AFTER_CUTOUT_BLOCKS` with an explicit model-view matrix; a renderer moved into a block
  stage needs the same.
- In GameTests, never read state from a `helper.onEachTick` task that depends on another per-tick task having run in
  the same tick: they run in hash order. Read inside the sequence.
- Counts: unit tests 5,840 (8 new); required GameTests 1,789 (14 new, one added by the verifier).

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,840 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer` twice in a row after the last code change: "All 1788 required tests passed :)" both
  times. Earlier runs in this package: run 1 failed the two water tests (the open-floor water and `markAt`, above); run 2
  failed `aDryPullHangsAShorterFieldNeverNone` (the per-tick hash-order flake, root cause above and fixed).
- `./gradlew.bat runGroundNetworkReviewClient`: "GROUND_NETWORK_REVIEW captured 22" (sixth launch), every new shot
  opened and judged; `./gradlew.bat runJeiReviewClient -PjeiReviewOnly=gouge_catalyst -PjeiReviewPages=6`: seven pages
  captured and judged (second launch).
- `PROTOCOL_VERSION` 48; no `*.actual.txt` written; `payload-registry.txt` and the frozen files not touched by this
  package. No git write command was run, only one Gradle process ran at a time, `AGENTS.md` is untouched and nothing was
  deleted from the tree (only review screenshots under `build/` were moved aside between launches).

### Verified

An independent verifier read the package's code, tests, lang, Liber and docs against the scope and the project rules, and
opened every new review shot. The report held in the main; these were wrong or missing and were fixed in place:
- **A refused snuff still put a lit run out.** `EclipseSnuffShaping.snuff` forgot the gouge cell's clock before
  `PlayerBlockEdits.replace` was asked, so when the edit was refused the cell's next scheduled tick found no clock and
  darkened it: a block change past `PlayerBlockEdits`. The clock is now forgotten only after the edit succeeds. New
  GameTest `bloodEclipseLeavesALitRunWhereTheCasterMayNotBuild` (an Adventure caster holds Eclipse on a lit run; it stays
  lit and no field hangs).
- **Fog colour without fog.** `UmbralFieldFog` tinted the fog colour violet even when it had stood aside for a closer
  dark (a Saint's gloam, a rite's air). The colour now follows the last terrain fog it actually drew.
- **Lang.** The carried-crossing line read "carries you: project blood into the opening: 0 / %s mB" (two colons); now two
  sentences. The Vicar's `item_inquiry.earthen_vein.line1` asked and answered its own question, which his sheet forbids
  ("the question has to want an answer"); it now asks how many veins the player has claimed (brief updated).
  `rooted_vein.recovery` names the 125 as "of your blood". Pinned phrases unchanged.
- Tidy: fully qualified names in `GroundMarkBlock.without` replaced by imports; an overlong `FurnaceVeinsShaping` javadoc
  line rewrapped.
- The ground review client was relaunched on the verified build (the earlier shots were taken a minute after the
  `WorldEffectDispatcher` edit): "GROUND_NETWORK_REVIEW captured 22"; furnaces, field outside and inside, eclipse,
  vein travel and gouge-water shots match the report, the lit run under water shows through it. The JEI shots were
  opened and are as described.

Checked and fine: no payload, attachment or golden changed (`PROTOCOL_VERSION` 48, no `*.actual.txt`); every block edit
in the package goes through `PlayerBlockEdits` (furnace lighting, snuffs, the water left by a filled gouge); blood is
paid by the verb pulse as before; only `HemoParticleData` particles; no "tendency"; dry pulls shorten the field and never
refuse; the new GameTests assert real outcomes. The deferred items stand as written.

Verifier's runs, one Gradle process at a time: `compileJava compileTestJava compileGameTestJava` BUILD SUCCESSFUL;
`test` 5,840 tests, 0 failures, 0 errors, 0 skipped; `runGameTestServer` twice in a row after the last change, "All 1789
required tests passed :)" both times.

## P9: Advancements and the hidden Liber pages

Two jobs: the core-loop firsts the rest of the overhaul never had, and CQ-1, the older Liber pages that were written but
never registered, so `MemoBookFilter` hid them from everyone. No payload, attachment, block, item or entity was added.
`PROTOCOL_VERSION` is still 48 and `payload-registry.txt` is untouched. One golden changed, on purpose (see Tests).

### What was built

**Sixteen more core-loop firsts (27 in all).** Each is an `impossible`-trigger advancement in
`data/hemomancy/advancement/hemomancy/`, a constant in the "Core loop firsts" block of `HarbingerAdvancementGranter`,
two lang keys (`advancements.hemomancy.<id>.title` and `.description`), one `grantIfNotDone` where the act happens after
its payment, and an advancement unlock in `LiberDiscoveryEvents` onto the page about the act.

| Id | Title | Granted in | Gate | Opens | Parent |
| --- | --- | --- | --- | --- | --- |
| `first_drip` | The First Drop | `ScabDrip.land`, the soil branch (`creditFirstDrop`) | the tank's owner, online in the level, alive, blood awake | Fed Ground | Set Through |
| `basin_harvest` | Fed and Reaped | new `BloodFedGrowthEvents.onHarvest` (`BlockEvent.BreakEvent`, lowest priority, uncancelled) | ripe crop, ground at level 2, blood awake | Fed Ground | The First Drop |
| `tainted_harvest` | Something Else Came Up | `TaintedHarvestModifier`, where the tainted stack is added | breaker (`THIS_ENTITY`) a server player with blood awake | Strain Beds, Fed Ground | Fed and Reaped |
| `chum_catch` | Something Bit | `ChumCatchModifier`, when a catch replaces the loot | fisher read from `ATTACKING_ENTITY` (vanilla's hook passes its owner there), else the hook's owner; blood awake | Chum | Shaped |
| `in_glass` | In Glass | `SpecimenVessels.leadIn`, after the creature is kept | | The Menagerie | Kept |
| `studied` | Studied | `ClinicalBloodKnowledge.examined`, the individual that completes a kind's study | clinical eligibility (already there) | Unread Humours | In Glass |
| `living_land_strike` | The Ground Rose | `LivingLandStrikeEntity.land`, when the hurt lands | | Living Land | Shaped |
| `full_night` | A Full Night | `DailyLoopEvents.rest` | an awake Harbinger (already there) | The Body and the Day | Meat Is Blood |
| `moon_brought_on` | Brought On | `BloodMoonEvents` through `WorldAnswer.creditMoonBroughtOn` | see My calls | The World Answers, Blood Moons | Degree 2 |
| `carrion_drawn` | What the Dead Give | `CarrionDrawShaping.feed`, each remain drawn | | Mending, Fire and the Dead | Shaped |
| `twin_strand` | Both Hands | `StrandService.pulse`, a paid pulse with the twin out | | More Strands | Whipcrack |
| `stain_run` | Along the Red | `StrandService.trigger`, when the plant runs along a stain | | More Strands | Whipcrack |
| `bound_and_branded` | Bound and Branded | `StrandService.brand`, a fresh brand (only Blood Binding calls it) | | Blood Binding | Kept |
| `field_of_dark` | Snuffed | `EclipseSnuffShaping`, when `DarkFields.feed` hangs a field | | Shapings of the Ground | Shaped |
| `carried_crossing` | Carried by the Vein | `EarthenVeinTravelManager.project`, when the opening is sated for a carried crossing | | Living Veins and Basins | Cut and Lit |
| `stoked` | Stoked | `GroundNetwork.heat`, when `FurnaceHeat.stoke` burns a furnace | the run's resolved owner | The Ground Network | Cut and Lit |

New pure rule `WorldAnswerRules.broughtOn(phase, pressure, roll)`. `BloodMoonEvents` now draws its one nightly roll into a
local and, when the moon starts and `broughtOn` holds, calls `WorldAnswer.creditMoonBroughtOn(level)`, which credits each
player through `creditMoonBroughtOn(level, player)` (alive, blood awake, own `BloodSpilledLedger.pressure` above zero).
The moon's odds are unchanged: still exactly one `nextDouble()` a night.

**CQ-1: forty older pages registered.** `LiberEntryDefinitions` has a new "Older pages (CQ-1...)" block after
`CORE_LOOP_PAGES` (so the core-loop guardrail still counts 29): 30 Liber Sanguinium constants and 10 Liber Immaculatus
constants, the lists `OLDER_PAGES` and `OLDER_IMMACULATUS_PAGES`, and `manipulationsPage(EnumBloodTemper)` and
`temperamentPage(EnumBloodTemper)`. Every one has a floor and earlier openers:
- `LiberKnowledgeHelper.unlockForDegree` gains `unlockOlderForDegree` (floors in the table below). It runs on every Degree
  gain and at login, so it is retroactive.
- New `LiberKnowledgeHelper.unlockForMemory(player, manipulation)`: a memory learned opens the overview, its school's page
  in both chapters, and whatever its memory item would open on pickup (so learning Pyretic Forge opens Pyretic Forge and
  Brazier Transmutation even if the memory never lay on the ground). Called from `KnownManipulationGrantHelper.addMemory`
  (brazier absorption, the Ringmaster, Living Weapon unlocks, Creative) and `CrudeMemoryShardItem`.
- New `LiberKnowledgeHelper.unlockForUnstained(player, progress)`: Liber Immaculatus has no Degree, so its floor is the
  purity or clarity stage, from `UnstainedMilestoneHandler`'s 100-tick check (only once purification has begun).
- `LiberDiscoveryEvents.initOlderPageUnlocks`: the item and advancement openers. Strain items come from
  `MorphlingStrains.itemPaths()`, so no strain id is named. Three memory items (Pyretic Forge, Glacial Grasp, Vital
  Effusion) now also open Brazier Transmutation.
- `AnnettaKnowlesEntity.startHarbingerRoute` opens Annetta Knowles for the Harbinger who unseals the jar.

| Page | Floor | Earlier openers | Text |
| --- | --- | --- | --- |
| `manipulations/overview` | D1 | Shaped; `hematic_memory`; the nine crude memory shards; any memory learned | Rewritten: verbs, Shapings, strands, Temper never refuses, price on tooltip and HUD |
| `manipulations/<school>` x8 | D2 | that school's enzyme; its `enzyme_mastery_*`; a memory of the school learned | Rewritten from the registry: every memory of the school by its lang name and cast type, one line of effect, its Shaping or strand; no mL prices. Removed memories that no longer exist (Sanguine Excavation, Vital Reservoir, Ferric Resonance, Ferric Transmutation, Crimson Sight, Glacial Bastion, Blood Eclipse Mantle) and the false Pyretic Forge line |
| `manipulations/memory_weaving` | D3 | `hematic_memory`, `somatic_loom`; the two Woven Vessel advancements | Read, unchanged (matches the Loom's own lines) |
| `manipulations/saint_memories` | D7 | the four Residuums and Recollections | Read, unchanged |
| `manipulations/thelemic_memories` | D2 | the eight tinctures | Read, unchanged |
| `temperament/temperament` | D1 | `bloody_vial`; Carried; a memory learned | Two sentences added: Temperament is never spent; Temper is not Temperament |
| `temperament/<school>` x8 | D2 | that school's enzyme and spores; its mastery; a memory learned | "Lux-tended" and "Ferric-tended" became "-tempered"; otherwise unchanged lore |
| `resonant_forge/recording`, `maintenance` | D3 | `resonant_forge`, `wax_cylinder`, `corpse_wax_cylinder` | Recording gains the basin's clotting (see Living Blood) |
| `the_hematic_order/bloodwork_stations` | D3 | `scarlet_vanity`, `fungal_implantation_pylon` | Read, unchanged |
| `the_hematic_order/sanguine_condenser` | D1 | `sanguine_condenser`, `sanguine_formation`; Set Through | Clot sets as well as fresh blood; a drip seats one (yours under your tank); stone "infuses" to Venous Stone |
| `morphling_cultivation`, `morphling_strains` | D2 | jar, polyp, incubator; every strain item | Read, unchanged |
| `morphling_cradle_and_nectar` | D5 | `morphling_cradle` | Read, unchanged |
| `the_infection/shared_mob_materials` | D2 | the six shared materials | Read against the loot tables; unchanged |
| `cosmic_forces/annetta_knowles` | D6 | the scout's notes, her two weapons; the Harbinger route's start | Read, unchanged (pinned by two tests) |
| Immaculatus `stages_of_purity`, `living_the_life`, `ghost_pipe` | purification begun | Pledged Unstained; Blessed by the Altar; `ghost_pipe`, `hemolytic_plating` | Read, unchanged |
| Immaculatus `what_purity_grants`, `virid_salis_brushing` | Tainted | Tainted; `virid_salis_trail` | Read, unchanged |
| Immaculatus `lethean_gifts`, `white_humor_purification` | Cleansing | Cleansing; Lethean Dew, poppy, Pale Silver Bell, `pale_humor_flask` | Read, unchanged |
| Immaculatus `stages_of_clarity` | Purified or Clarity | Purified, Clarity Awakened; `pale_silver_ingot` | Read, unchanged |
| Immaculatus `still_arts` | Clarity unlocked | Clarity Awakened, Discerning | Read, unchanged |
| Immaculatus `pale_intercession` | Resolute Clarity | Resolute; `pallid_icon` | Read, unchanged |

### Tests

- JUnit: new `architecture/LiberPageRegistrationGuardrailTest`. Every `*/pages/*.json` of both books is registered (the
  three loop-registered families are named, each with its reason); every older-page constant is in a list and named by
  an opener; the eight school pages name only real memories with their type, and name every registered memory.
  `CoreLoopTeachingGuardrailTest` pins 27 firsts (still 29 core pages). `WorldAnswerRulesTest` gains
  `aMoonIsBroughtOnOnlyByTheRollThePressureAdded`. The guardrail index in `ARCHITECTURE_STANDARDS.md` has its row, and the
  impact map was regenerated.
- GameTests, first-granted asserts in the suites that already drive each act: `MenagerieGameTests` (In Glass; Studied not
  at two individuals and granted at the third), `DailyLoopGameTests` (A Full Night, not on leaving bed early),
  `SecondShapingsGameTests` (carrion), `LivingLandGameTests` (floor strike), `StrandGameTests` (Both Hands granted exactly
  when the twin pours, stain run, binding brand), `GroundNetworkGameTests` (Stoked, Snuffed), `EarthenVeinTravelGameTests`
  (Carried by the Vein for the living run only, not the gouge one).
- New GameTests: `FarmingGameTests.aDripFromYourOwnTankOntoSoilIsTheFirstDrop`, `aRipeCropCutFromBasinGroundIsFedAndReaped`,
  `findingATaintedHarvestIsItsFirst`; `ChumGameTests.aChumCatchIsTheAwakeFishersFirst`;
  `DailyLoopGameTests.aMoonBroughtOnCreditsWhoeverStoodInTheCount` (restores the ledger region it set). Each has a
  sleeping-blood or unripe negative.
- `CoreLoopClosingGameTests`: the Liber test also checks The Ground Rose opens Living Land and nothing else; new
  `theOlderPagesAreRegisteredAndOpenByDegreeItemAndMemory` (every page of both loaded books registered; an enzyme opens
  only its school; a learned memory opens its school, the overview and its own Shaping page; D6 keeps Canon Memories
  shut; D8 opens all thirty; the Unstained stages open the ten).
- Golden changed on purpose: `src/gameTest/resources/dialogue/route-fingerprints.txt`, rows 212 and 213 only
  (`mnemonist_woven_vessel_turn_in`). Earning the Woven Vessel advancement now opens Deliberate Memories, so the saved
  player carries that Liber entry. Nothing else in the file moved.

### My calls

- The sixteen ids, titles, icons and parents in the table above (CQ-45). The chain The First Drop, Fed and Reaped,
  Something Else Came Up hangs under Set Through; In Glass then Studied under Kept; Both Hands and Along the Red under
  Whipcrack.
- Brought On (CQ-48): credited when the night's one roll lands between the phase's chance and the pressure-raised chance,
  to every awake player standing where their own region's pressure is above zero. The moon's odds are unchanged.
- Carried by the Vein (CQ-49) is granted when the opening is fed in full for a carried crossing, not on arrival. The
  teleport runs on the player tick, and the opening already says "carried" at that point.
- What the Dead Give is granted on each remain drawn, even when Mortem Temper is already at its cap: the act is the draw,
  not the gain.
- Stoked is granted on any furnace the run burns, lit or kept lit. It is not gated on the owner's blood, since only a
  Harbinger lights a run.
- Degree floors (CQ-46): overview, Temperament and the Condenser at D1; school pages, Thelemic memories, the two morphling
  pages and shared mob materials at D2; memory weaving, the Forge pages and bloodwork stations at D3; Cradle and Nectar at
  D5; Annetta at D6; Canon Memories at D7.
- Learning a memory opens its school's pages in both chapters, the overview and its memory item's own pages. A crude shard
  counts.
- Liber Immaculatus floors are purity and clarity stages, run from the existing milestone check. A player on the clarity
  stages counts as past every purity stage.
- The eight school pages list every memory by school with its cast type and one line, and no mL prices (CQ-47). Verb-only
  memories and staff forms are named without a type. The Blood Binding and Blood Avatar forms are named in full so the
  guardrail can hold "every memory is on a page".
- Annetta's page opens when a Harbinger starts her route; the Unstained route has its own Immaculatus memo.
- The Liber Immaculatus pages keep their text: nothing there is Harbinger core loop.
- No page relies on an item alone (an item opens only on a ground pickup): every older page has a floor.

### Deferred, and why

- Seventy-two pages are still hidden after the review's staging, each with a reason: they open by rites, dialogue, memos
  or a ground pickup the staging does not perform (the station upgrade lessons, clinical lessons, the troupe and
  antecedent families, the Saint and Qliphoth pages, Hermit and Scriptorium dialogue pages, the Immaculatus memo pages).
  Six core-loop pages are item- or first-only (Lignum Mortis, Pyretic Forge, Tendon Line, Brazier Transmutation, Mining
  by Projection, Ice and Iron Walls); learning their memory now opens them, so they were given no Degree floor.
- Every new first uses the `task` frame; no goal or challenge frames for the harder ones. The owner may want some.
- Players who earned an advancement before this package do not get its new page (no migration; dev worlds only). Degree
  floors are retroactive at login.
- The Liber Immaculatus pages were read but only registered, not edited (Unstained ritual work is deferred by the owner).

### Review client

`LiberPagesClientReview` and the row `liberPagesReviewClient` (`runLiberPagesReviewClient`, dir `build/liber-pages-review`,
user `LiberPages`). It stages only through real triggers (the Degree path to D8, `unlockForItem` for eight items, a memory
learned, every Hemomancy advancement but Stoked, the Unstained stages), logs
`LIBER_PAGES_REVIEW staged; still hidden after every real trigger: 72 [...]` with each page's sources (none of the forty
is in it), and shoots Stoked's toast, the Manipulations and Temperament chapter lists, the first and last leaf of all forty
older pages, and the Hemomancy tab. Launch 1: "LIBER_PAGES_REVIEW captured 86"; its three tab shots were too tight to show
every first, so launch 2 centres on nine branch roots: "LIBER_PAGES_REVIEW captured 92". Every page shot was opened: the
text paginates cleanly (the Animus page runs to three spreads), and each new first shows under its parent.

### Numbers the owner may want to tune

- The Degree floors: `LiberKnowledgeHelper.unlockOlderForDegree`.
- The Unstained stage floors: `LiberKnowledgeHelper.unlockForUnstained`.
- Which items and advancements open each page: `LiberDiscoveryEvents.initOlderPageUnlocks`.
- The firsts' parents, icons and frames: `data/hemomancy/advancement/hemomancy/<id>.json`.

### For later packages

- A new Liber page needs a literal `entry("...")` constant, a registration, a floor and an opener, or
  `LiberPageRegistrationGuardrailTest` fails. A new memory must be named on its school's page (the same test).
- A new core-loop first must bump the 27 in `CoreLoopTeachingGuardrailTest`.
- `LiberKnowledgeHelper.unlockForMemory` is the hook for "learned a memory"; NPC lesson trees can reuse it.

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,844 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer` twice in a row after the last code change: "All 1795 required tests passed :)" both
  times. The first run in this package failed only `everydialogueroutekeepsitsoutcome`, the intended golden change above.
- `./gradlew.bat runLiberPagesReviewClient`: "LIBER_PAGES_REVIEW captured 92" (second launch), every shot judged.
- `PROTOCOL_VERSION` 48; `payload-registry.txt` untouched; no new `*.actual.txt` (the three in the GameTest directory
  predate this package). No git write command was run, one Gradle process at a time, `AGENTS.md` untouched, nothing
  deleted from the tree.

### Verified

An independent pass read every grant site, the older-page block and its openers, the advancement files and lang, the
rewritten pages, the new guardrail and GameTests, and the review client's shots (contents list, overview, Animus page,
Condenser page, the Shaped tab). Each first is granted by one `grantIfNotDone` after the act and its payment; every
advancement parent and icon id exists; every item named as an opener is a registered item; no payload, no blood paid
outside `BloodAccounting`, no block edit outside `PlayerBlockEdits`, no vanilla particle and no "tendency" was added. The
golden change (rows 212 and 213, `memory_weaving` added under ADVANCEMENT) is the intended one. Two qualifications to
the text above: "every older page has a floor" holds for the forty older pages, but six core-loop pages remain item-,
first- or memory-opened only (see Deferred); and `everyOlderPageHasAFloorOrAnOpener` checks only that each constant is
named in the helper or the events class, while the real floor-and-opener behaviour is held by the GameTest
`theOlderPagesAreRegisteredAndOpenByDegreeItemAndMemory`. One fix: `CoreLoopTeachingGuardrailTest.grantSites` matched a
constant by substring, so `ADV_CARRIED` would have counted a file that granted only `ADV_CARRIED_CROSSING`; it now
matches the whole word (`ADV_CARRIED` still has its own site in `TemperCharge`). After the fix: compile BUILD
SUCCESSFUL; `./gradlew.bat test` 5,844 tests, 0 failures, 0 errors, 0 skipped; `./gradlew.bat runGameTestServer` twice in
a row, "All 1795 required tests passed :)" both times; no new `*.actual.txt`.

## P10: NPC lesson trees

Each system of the core loop now has a real lesson, told through the existing dialogue system by the outpost speaker
who already teaches the work beside it. No payload, attachment, advancement, item, block or registry entry was added;
PROTOCOL_VERSION is still 48 and `payload-registry.txt` is untouched. No git write command was run, only one Gradle
process ran at a time, AGENTS.md is untouched and nothing was deleted.

### What was built

- **The table.** `common/entity/npc/dialogue/CoreLoopLessons`: one row per lesson (hub speaker id, lang key, id,
  Degree range, branches with their line counts, Liber pages), with the key, node and event names derived from the row.
  Pages are `LiberEntryDefinitions` constants, so the table names no path by hand.
- **The cards.** `CoreLoopLessonDialogue.append(tree, speaker, player)` gates on awake blood and not Unstained, then
  the pure `append(tree, speaker, degree)` adds one lore root option per lesson whose Degree range holds, before the
  greeting's item question and leave option. The card opens a lesson node (two lines and a question per branch, plus
  the speaker's leave); each branch node answers in two lines (the Hermit's `ground` in one) and offers the shared back
  option (`hemomancy.dialogue.<npcKey>.option.craft.back`) and leave. Every branch option carries the lesson's event and
  its node, so asking anything teaches; the card itself carries no event, so the hub adds no confirm step. It is called
  at the end of `progressionDialogue` in the six entities, so the overhead sigil sees the cards too.
- **The route.** Package-private `route/CoreLoopLessonRoutes.teach(event, speaker, type)`, called on the first line
  of `VicarDialogueEvents`, `AlchemistDialogueEvents`, `MnemonistDialogueEvents`, `ArtificerDialogueEvents`,
  `HermitDialogueEvents` and `VeinMasonDialogueEvents` (no new subscriber, no new `ROUTES` entry; the largest file is
  316 lines). It takes the event when it is one of that speaker's lessons and opens the lesson's pages through
  `LiberKnowledgeHelper.unlockEntries(..., HemomancyDiscoverySource.DIALOGUE)` only when the speaker is the right type,
  alive and within 8 blocks, and the player is alive, blood awake, not Unstained and inside the Degree range.
  `rewardDelivered` is whether anything opened (a page or its DIALOGUE source).
- **Speaker table.** `DialogueEventSpeakers.PREFIXES` is now a `Map.ofEntries` with six more prefixes:
  `hermit_craft_`, `vicar_craft_`, `alchemist_craft_`, `mnemonist_craft_`, `artificer_craft_`, `anchorite_craft_`
  (the Hermit and Artificer entity classes are new to the table). No existing event id starts with any of them.
- **Liber.** The `CORE_LOOP_PAGES` register loop adds `HemomancyDiscoverySource.DIALOGUE`.
  `CoreLoopTeachingGuardrailTest` still pins 27 firsts and 29 pages and needed no change.
- **Text.** 260 new `en_us.json` keys: 21 root options, 64 branch options, 6 back options, 169 NPC lines, written one
  speaker at a time to their sheet (Hermit, Vicar, Alchemist, Mnemonist, Artificer, Vein-Mason). Lang prefixes
  `hemomancy.<npcKey>.craft.*` and `hemomancy.dialogue.<npcKey>.option.craft.*`, npcKey `hermit`, `vicar`,
  `alchemist`, `mnemonist`, `artificer`, `anchorite`. Each key has a `{about, tone}` brief in that speaker's existing
  brief file (`lady_fungal_wanderers.json` for the Hermit, `vicar_succession.json`, `alchemist.json`,
  `mnemonist_antecedent.json`, `artificer_anchorite_clinical.json` for the Artificer and the Vein-Mason), inserted by
  text so no existing line changed bytes. Working notes, `proposed.json`, `briefs.json` and the merge script are in the
  local (ignored) `tools/i18n/voice_rewrite/pass5_lessons/<speaker>/`.
- **Item inquiries (brief Task 8).** Six new files under `data/hemomancy/dialogue_inquiry/`: `alchemist/hemomancy/`
  `salt_stained_timber`, `glowroot`, `emberpod`, `tainted_harvest`; `artificer/hemomancy/catalyst_feeder`;
  `cicatrix_anchorite/hemomancy/venous_basin` (12 lines, 12 briefs). 397 inquiry files are on disk now. The voyager log's
  five inquiries are unchanged.

| Speaker | Lesson | Degrees | Branches | Liber pages it opens |
|---|---|---|---|---|
| Hermit | `verbs` | 0 to 4 | out, in, spilled | The Two Verbs |
| Hermit | `answer` | 1 to 4 | ground, night, drinkers | The World Answers |
| Vicar | `verbs` | 1+ | key, order, draw | The Two Verbs, The Draw and the Mark |
| Vicar | `day` | 1+ | food, rest, meats | The Body and the Day |
| Vicar | `answers` | 2+ | count, moon, water | The World Answers, Chum |
| Vicar | `keeping` | 3+ | lead, feed, brood | The Menagerie, The Brood |
| Alchemist | `living_blood` | 1+ | keepers, clot, gauge | Living Blood, Sanguine Condenser |
| Alchemist | `fed` | 1+ | drip, basin, effusion | Fed Ground |
| Alchemist | `temper` | 2+ | fill, dry, catalysts | Temper, Temperament |
| Alchemist | `humours` | 2+ | sample, study, dose | Unread Humours |
| Alchemist | `beds` | 3+ | strains, timber, crops | Strain Beds |
| Mnemonist | `shapings` | 1+ | first, work, mend, draw | Lignum Mortis, Pyretic Forge, Brazier Transmutation, Second Shapings |
| Mnemonist | `strands` | 2+ | forms, stain, brand | Strands, Strand Forms, Second Strands, Blood Binding |
| Mnemonist | `land` | 3+ | strike, dark, light | Living Land, Ground Shapings |
| Artificer | `hot_runs` | 1+ | uses, heat, feeder | The Ground Network, Ground Shapings |
| Artificer | `staff` | 2+ | rings, dig, forms | The Staff Grows, Mining Shapings |
| Artificer | `strand_line` | 2+ | reach, crack, twin | Second Strands, Strand Forms |
| Artificer | `climb` | 2+ | root, cleats, line | Venous Cling, Tendon Line |
| Vein-Mason | `ground_marks` | 4+ | cut, vein, basin | The Ground Network, Living Veins and Basins |
| Vein-Mason | `carried` | 4+ | join, price, limits | Living Veins and Basins |
| Vein-Mason | `walls` | 4+ | ice, iron, scars | Second Shapings Walls, Temper |

All 29 core-loop pages are taught by at least one lesson.

### Tests

- JUnit `common/entity/npc/dialogue/CoreLoopLessonsTest` (7 cases): every key a lesson shows exists and every new key
  has a brief; no option key, node or event id carries a Quest Work, item-question or leave word; event ids are unique
  and carry their speaker's prefix; the pure `append` adds exactly the lessons each Degree from 0 to 8 allows, as lore
  cards with no event, before the closing options, with every branch carrying the event and leading back and out; the
  hub files every lesson as unread lore in Conversation with no confirm node and none in Quest Work; every core-loop
  page is taught and every lesson page is registered; the voice pins (Mnemonist asks no question, Vein-Mason uses no
  contraction, no "tendency", dash, ellipsis or "D5"-style Degree).
- `src/test/resources/dialogue/lore-options.txt`: 21 new lines, one per lesson, kept sorted. `MIN_REACHED` stays 72
  (the sweep never reaches decorator options).
- GameTests `CoreLoopLessonGameTests` (`core_loop_validation`, batch `core_loop_lessons`, 13 tests): for each speaker,
  a right click at the Degree that shows all their lessons serves every lesson as an unread lore card with its node and
  none in Quest Work, and lists none one Degree outside (the Hermit at the fifth, the Vein-Mason at the third, the rest
  at 0), with sleeping blood or while purifying; for each speaker, every lesson's event through
  `DialogueOptionPacket.accept` opens its pages, changes nothing but the Liber knowledge attachment and earns nothing,
  and told twice opens nothing new; one test sends every lesson's event to another speaker, with no speaker, from
  twelve blocks, from a dead player, below or above its Degrees, with sleeping blood and while purifying, and checks no
  page opens. A deliberate mutant (the Degree check and the speaker-type check removed) made the refusal test fail with
  39 named pages, so it can fail.
- `DialogueRouteCharacterisationGameTests.routes()` gains the 21 events with their speakers, before
  `no_route_takes_this_event`.

### Goldens changed, and why

- `src/gameTest/resources/dialogue/route-fingerprints.txt`: 63 rows added (21 events, three rows each), intended.
  Before copying I diffed the `.actual.txt` against the golden: every existing row was byte-identical and only the 63
  new rows were added, at lines 628 to 690. The D7 rows read `delivered=true` with a `liber_knowledge` diff for every
  Vicar, Alchemist, Mnemonist, Artificer and Vein-Mason lesson, and `delivered=false` for the two Hermit lessons (his
  fourth-Degree cap); the D0 rows and the `->NONE` rows are `delivered=false saved={}`. Each D7 row also shows an
  empty `unstained_progress` attachment created by the guard's read, as the Scriptorium lesson's row already does.
- `src/test/resources/dialogue/lore-options.txt`: the 21 lesson lines, intended.

### My calls

- **The assignment.** I followed the brief's table and changed it where the code or a sheet disagreed:
  - Twenty-one lessons, not twenty. The Alchemist's fed ground (a first-Degree page) and strain beds (a third-Degree
    page) are two lessons, so a first-Degree player is not told about Ravenous Heartwood beds early.
  - The Vicar's `verbs` asks about the Draw and the Mark (`draw`) instead of a `dry` branch, because Temper only opens
    at the second Degree and the Alchemist's `temper` lesson teaches the dry cast.
  - The Vicar's `day` ends on the Order's meats and tainted meat (`meats`), not `spend`: the page has nothing on
    spending.
  - The Alchemist's `humours` lesson ends on dosing (`dose`). The vessel's screen and the brood went to the Vicar's
    `keeping` (third Degree, when the first creature can be led).
  - The Tendon Line is taught by the Artificer's `climb` lesson with Venous Cling and the cleats, as a movement lesson.
  - The Staff lesson's third branch is the pickaxe form, not the brief's "call": I found no short-tap call of the
    Primal Morphling in the code.
  - Degrees: the Artificer's Staff, strand and climb lessons at the second Degree (his workshop tree and the Staff's
    skill branch start there); the Mnemonist's strands at the second, land at the third; strain beds at the third (the
    page's floor). Memories have no learning gate in code, so these follow the Liber floors.
- **Ids say `craft`.** `craft_<id>` nodes and `<npcKey>_craft_<id>` events, never lesson, brief, task, report, claim,
  bestiary, reward or quest (CoreLoopLessonsTest pins it). The Mnemonist's first branch is `first`, not `claim`.
- **Lessons are lore cards** (CQ-51). They light the speaker's NOTICE sigil until read, as other lore does.
- **The Hermit stops at the fourth Degree** and teaches only to awake blood (CQ-52). The Vein-Mason starts at the
  fourth, where their locked greeting ends (CQ-53).
- **A lesson opens Liber pages and nothing else** (CQ-54). `rewardDelivered` is true the first time a lesson adds its
  DIALOGUE source even when the page was already open by Degree, so the route fingerprints and the GameTest have a
  deterministic signal; pages already open are marked unread again in the Liber, which reads as "go and reread".
- **Successors tell their profession's lessons** in the original voice, as they do every other line (CQ-55).
- **The route guard checks the speaker itself.** The packet's `DialogueEventSpeakers` prefix check drops a wrong
  speaker first; the route also checks type, life and reach, because the fingerprint test posts on the bus directly.
  It does not depend on `OfferedDialogues`.
- **Lines fit the box at GUI scale 3.** The review client measures each node's spoken text against the content box.
  Four nodes ran to nine or more wrapped lines and were trimmed (the Vein-Mason's `vein` from three lines to two, and
  one sentence each in the Vicar's `brood`, the Mnemonist's `forms` and the Vein-Mason's `basin`). Four card titles
  were cut at the box edge and were shortened (the Vein-Mason's `ground_marks`, the Artificer's `strand_line`, the
  Vicar's `day` and `answers`).
- **Voice.** The Alchemist and the Artificer are contracted, though less than their sheets' whole-speaker figures,
  because procedure lines with numbers leave little room. The Mnemonist kept a few contractions to stay apart from the
  Vein-Mason. The Vicar asks at least one real question per lesson; the Vein-Mason asks one, about where the body feels
  a dry temper.
- **Inquiry voices.** The Alchemist answers for the timber, both catalyst crops and Tainted Harvest; the Artificer for
  the Catalyst Feeder (a device); the Vein-Mason for the Venous Basin, under their `inquiry.*` key style.

### Measured (lesson lines only, `pass4/scan.py metrics`)

| Speaker | Lines | Words per sentence | Questions | Contractions per 1,000 words | Pattern hits |
|---|---|---|---|---|---|
| Hermit | 15 | 12.1 | 0% | 0 | 0 |
| Vicar | 32 | 15.2 | 10.4% | 0 | 0 (one short closer fixed) |
| Alchemist | 40 | 15.3 | 0% | 21.2 | 1 (`slot`, a named control) |
| Mnemonist | 26 | 13.2 | 0% | 3.8 | 0 |
| Artificer | 32 | 12.6 | 0% | 21.3 | 0 |
| Vein-Mason | 24 | 11.4 | 1.8% | 0 | 0 |

These were measured over the lesson lines alone, so the contrast map in `docs/NPC_VOICES.md` was not changed. No
sentence is shared between two speakers' lessons (checked by script).

### Numbers said in dialogue (all from the Liber pages and the code; the owner may tune them)

- Hermit: the count halves every two days.
- Vicar: verb key Y; blood back about a millilitre a second; fewer than three shanks half, starving none, full half
  again; forty-eight from a steak; Well Rested five minutes, twice as fast; count halves every two days; three
  Vivianite at most; one, two, three creatures at the third, fifth, seventh Degree; three seconds to leash; a third of
  its health; brood above three quarters of Vigor for about two minutes; parents rest five minutes.
- Alchemist: about five minutes fresh; a tenth each minute; an hour; suture from the third Degree within sixteen
  blocks; drip feeds about five minutes; Lantern a stage every thirty seconds for 25 mL; Temper burn 1, 2, 3 to 5 and
  half a measure; twenty of a school, up to eighty in your own; ten per Will tier; death takes half; catalysts ten,
  five, three; vial two health, syringe one, half a minute; three individuals or three Vigor levels; eight logs, then
  one timber and kelp seed four logs. Inquiries: glowroot and emberpod worth two against an enzyme's ten.
- Mnemonist: Lignum Mortis about sixteen millilitres a second; Pyretic Forge and Crimson Flame two a pulse; anvil
  after eight pulses; five strand forms; eleven more strand memories; brand drawn from twice the reach; Living Land
  30 mL a second, two columns from the third rank of mastery; hidden beyond four blocks; Beacon lines dim over four
  minutes.
- Artificer: up to sixty-four cells; rings at 5,000, 25,000, 100,000, 300,000 and 750,000 mL; half a block, a block,
  a tenth faster, a block, swaps 25 mL less down to 25; tunnel one by two, three by three; 3 mL stone, 6 mL ore; skills
  at the second, third, fifth and sixth Degree; twin half again; cling 6 mL to root, 4 mL a second on a wall, 6 under a
  ceiling; cleats a quarter; Tendon Line three times the reach, snaps a third past it.
- Vein-Mason: sixty-four cells; sixteen vein cells from the third Degree; thirty-two blocks; one millilitre a second;
  a day to wither; about five minutes for a basin; carried crossing one hundred and twenty-five; 500 to 3,000 without;
  ice three high; twelve iron blocks; a quarter less burn per scar tier.

Where they live: `EarthenVeinTravelRules.LIVING_VEIN_COST` and `cost`, `FerricRampartShaping.MAX_SEGMENTS`,
`LivingStaffGrowthRules` thresholds, `LivingStaffSkillBranch` Degrees, `StrandSkills.TWIN_COST`, the Liber pages for the
rest. A tuned number must be changed in the lesson line too: no test pins a spoken number to code.

### Deferred, and why

- **Unstained speakers, the Court, the Circus, the Monolith, the Voyager and the Wayfarer get no lessons.** Unstained
  work is deferred by the owner; the others do not teach the Harbinger core loop.
- **No lesson-only advancement** (27 firsts are pinned) and no reward for hearing one, by design (CQ-54).
- **The playthrough suite was run once** (see Verification); its unread-lore sigil expectations are listed there.
- **Successors keep the original speaker's lesson voice**; writing successor lines is a separate voice pass.

### Review client

`lessonReviewClient` (`LessonClientReview`, row in `gradle/review-clients.gradle`, dir `build/lesson-review`, user
`LessonReview`, documented in `docs/TESTING.md`). A fresh game directory first needs one click op on the accessibility
screen's Continue button. Final run (fresh save): 99 screenshots in `build/lesson-review/screenshots`, 114
`LESSON_REVIEW PASS` lines and no FAIL: all 21 lessons are unread lore cards in their speaker's Conversation box; every
lesson node and branch node's spoken text fits the content box at 1280x720, GUI scale 3, without scrolling ("0 lesson
nodes have more text than the box shows"); brief mode shows a brief for every lesson key ("every lesson key has a brief
[]"). I read the Conversation shots of all six speakers, the longest nodes (Alchemist `living_blood`, Vicar
`keeping.lead`, Vein-Mason `ground_marks.basin` and `vein`, Mnemonist `strands.forms`) and the two brief-mode shots:
no raw key, no cut text, and card titles fit after the four were shortened. Earlier runs found the cut card title and
the four over-long nodes (fixed above), and a reused save reads every lesson as already read (correct: the read record
is saved).

**Correction (verifier).** The paragraph above overstates what those 99 shots showed. The harness called the screen's
`openTopic` and `selectOption` directly, which skips the `resetScroll` a click runs (`DialogueScreen.activate`), so every
lesson and branch shot kept the scroll of the "conversation-end" shot and showed the bottom of the node (the questions),
not the spoken text a player sees first; and for every speaker but the Hermit and the Vein-Mason no shot showed the lesson
cards at all (the "end" shot lands on rapport cards). The numeric fit measure was sound. The verifier fixed the harness
(see Verified) and re-ran it.

### For later packages

- A new lesson is one row in `CoreLoopLessons` plus its keys and briefs; `CoreLoopLessonsTest` fails until every key
  and brief exists, `lore-options.txt` needs its line, and the route fingerprint golden gains three rows.
- Keep lesson ids free of Quest Work words.
- If a number in a lesson changes in code, change the line; the list above is the index.
- Other sessions write to the shared Windows temp folder: keep scratch scripts under `build/`.

### A flake fixed on the way

`FarmingGameTests.aDripFromYourOwnTankOntoSoilIsTheFirstDrop` (round two P9) failed once in the second full run with
"the second drip did not land on the soil". Cause: the first drip grows the spike a block one time in ten
(`BloodFedGrowthRules.DRIP_GROW_CHANCE` 0.1, on the level's random), after which the old tip is no longer a tip and
drips nothing. The second drip now falls from `lowestTip`, the same fix P1 made for the cauldron drip test. Test code
only; no mod code changed.

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava --offline`: BUILD SUCCESSFUL.
- `./gradlew.bat test --rerun --offline`: BUILD SUCCESSFUL; 5,851 tests, 0 failures, 0 errors, 0 skipped (P9 had
  5,844; the 7 new are `CoreLoopLessonsTest`).
- `./gradlew.bat runGameTestServer --offline`, twice in a row after the last code change: "All 1808 required tests
  passed :)" both times (P9 had 1,795; the 13 new are `CoreLoopLessonGameTests`). The run before the flake fix read
  "All 1808 required tests passed :)" then "1 required tests failed :(" (the drip test above). No new `*.actual.txt`.
- `./gradlew.bat runCoreLoopGameTestServer --offline`: "All 21 required tests passed :)"; with the mutant guard, "1
  required tests failed :(" naming 39 pages opened out of turn.
- `./gradlew.bat runDialogueRouteGameTestServer --offline` wrote the `.actual.txt` whose 63 added rows were copied to the
  golden (see Goldens); the full runs above pass against it.
- `./gradlew.bat runPlaythroughGameTestServer --offline`, once: "All 427 required tests passed :)", "97 optional tests
  failed". Against P2's run-5 log, no test fails now that did not fail then; `apotheosritewithoutrescue` (optional) no
  longer fails, and `sainttombwake` was P2's own fix. The unread lesson cards moved no playthrough expectation.
- Review client: see above; 114 PASS lines, no FAIL, 99 screenshots.

### Verified (independent verifier, first pass)

Checked against the scope, the three rules and the project rules: no payload, attachment, block entity or blood path was
added (PROTOCOL_VERSION 48, `payload-registry.txt` untouched); no new `DialogueEvent` subscriber or `ROUTES` entry; every
lesson key and brief exists; no "tendency", dash, ellipsis or "D5" shorthand; the Mnemonist asks nothing and the
Vein-Mason uses no contraction; spot-checked numbers (verb key Y, cast key R, Temper caps 20 and 80, cling 6 mL root,
Tendon Line three times reach and a third past it, Ferric Rampart 12, carried crossing 125) match the code. The refusal
GameTest exercises every guard and the route fingerprints add rows only. Fixed:

- The six entity call sites named `CoreLoopLessonDialogue` fully qualified and passed the speaker as a bare string
  (`"alchemist"`, `"cicatrix_anchorite"`...); they now use the class name (the package is already imported) and the
  `CoreLoopLessons` speaker constants, so a renamed speaker id cannot silently drop a speaker's lessons.
- `CoreLoopLessonsTest`'s Vein-Mason contraction pin missed the "'s" forms (it's, that's, there's, what's, let's...);
  it now catches them. The lines already passed.
- `LessonClientReview` now resets the scroll after opening a lesson or asking a branch, as a click does, and adds a
  `<speaker>-conversation-lessons` shot scrolled to the first lesson card. Re-run on a fresh save (`LessonReviewV`):
  105 screenshots, 114 `LESSON_REVIEW PASS`, no FAIL. I read the six lesson-card shots and a spread of node tops
  (Alchemist `living_blood`, Vicar `keeping.lead`, Vein-Mason `ground_marks.vein`, Mnemonist `strands.forms`,
  Artificer `climb.root`, the brief-mode pair): unread lore cards with their sigil, all spoken text visible from its
  first line, nothing cut. `docs/TESTING.md` says what the shots now are. The pre-fix shots are kept in
  `build/lesson-review/screenshots_p10_before`.

Left open (owner's call, not defects): the Artificer's `hot_runs` and the Vein-Mason's `ground_marks.cut` both teach
cutting a gouge, with near-twin sentences on water stopping at the cut; two lesson sentences repeat existing text word
for word (`beds.timber.line1` the voyager log tooltip, `climb.line.line1` the Tendon Line Shaping text); every lesson
card carries the shared Conversation summary "Talk, and what they know of the old work.", which reads oddly on a
practical lesson but is the hub's one summary for every Conversation card.

Final runs: compile BUILD SUCCESSFUL; `test` BUILD SUCCESSFUL, 5,851 tests, 0 failures, 0 errors, 0 skipped;
`runGameTestServer` twice in a row: "All 1808 required tests passed :)" both times, no new `*.actual.txt`.

## P11: Visual fixes, two-client looks, test fixtures

Built in the working tree, uncommitted. One protocol bump for the package: `PacketHandler.PROTOCOL_VERSION` is 49 and
`src/test/resources/network/payload-registry.txt` was rewritten (two payloads added; two existing layouts changed).

### Payloads (protocol 48 to 49)

- `PacketSyncTendonAnchor` now carries the owner's entity id and goes to the owner and the players tracking them
  (`TendonLineShaping.sync` uses `sendToPlayerAndTrackers`). `TendonLineShaping.sendTo` answers a start-tracking from
  `ClingService.onStartTracking`, which now covers the Tendon anchor as well as the cling. The `TENDON_ANCHOR` sync row
  is unchanged.
- New clientbound `PacketSyncVerbPresence(entityId, projectReach, absorbReach, activeVerb, selected)`, sent by
  `common/manipulation/verb/VerbPresence` (a subscriber with a UUID session map cleared through
  `TransientServerStateEvents`). It is computed only for a player using a verb tool with awake blood: forced on the
  first use tick, then every 10 ticks only when it changed, and to a watcher who starts tracking mid-channel. The client
  keeps it in `client/data/OnlookerVerbState` (cleared with the strand state in `ClientSessionEvents`). Common code reads
  it through three new `ClientBridge` methods (`onlookerReach`, `onlookerSelection`, `onlookerVerb`), which answer only
  for players other than the local one. `VerbReach.of`, `ShapingDispatcher.selectedManipulation` (now package-visible)
  and `VerbChannel.activeVerb` ask the bridge first on a client level. `CellHandParticleEffects` no longer limits shaped
  streams to the local player.
- New clientbound `VerbRefusalPacket(ticks)`, sent by `ShapingDispatcher.showRefusal` where a pulse is refused (the
  claimed path, and a held channel letting go). The client keeps the deadline in `client/data/VerbRefusalClientState`.
- `PacketSyncActiveRites` writes one VarInt `staffRings` at the end of each entry. `ActiveRiteData.RiteEntry` gained
  `withStaffRings` and `getStaffRings` (no new constructor). `CardinalRiteSync` fills it from the escrowed Staff's stamp
  (`ActiveCardinalRite.escrowedStaffRings`), else from the owner's rings while they are online. No saved key changed.

### The seven visual fixes

1. **Tendon Line cord** (`TendonLineRenderer`, rewritten). It draws every rooted line in the dimension within 96
   blocks, from the drawn palm (new `CellHandStreams.palm`, now shared with `StrandRenderer.palm`): a WISP sheath, a
   RIBBON body 0.10 to 0.07 wide, a pale seam, one sinew wound along the whole cord (7 turns, 0.045 off the middle) and
   a clot card at the root. Slack sag is 0.22; held Absorption pulls it taut (sag 0) with a faster pulse. The bead trail
   was the cell hand's own stream flying to the anchor: the new `VerbShaping.drawsOwnStream()` (true for both Tendon
   Shapings) makes the hand pour none. The guessed hand point and its four offset constants are gone; the legacy source
   test `TendonLineRendererSourceTest` now pins the new behaviour.
2. **Pyretic Forge**: `presentsCast` is false, so no Flammeus outpouring. Each pulse sends a short
   `BloodFlowVisuals.toward` LIVING tendril into the work. The `FORGE` cue now scales with its radius below one block
   (`ThermalGeometry`); Pyretic sends it at 0.45 sunk 0.14 under the items (0.8 above a fired block), and
   `ManipulationAmbientParticles` throws custom ember licks off the rim of every forge cue as it lands.
3. **Stains** (`tools/model_export/ground_network.py`): three splashes per age, `blood_stain_<wet|tacky|dry>_<0..2>`,
   each variant keeping one outline across the ages (a pool with a noisy radius, 3 or 4 arms of 1 to 2 px ending in a
   drop, satellite drops, all a pixel inside the face). Tacky is a maroon skin with a darker heart and a brighter raised
   rim at alpha 200; dry is a cracked brown ring with clear cracks and flaked gaps round a mostly clear middle at alpha
   160. The multipart `apply` lists the three, so vanilla picks one by position. The six old files are in
   `removed_assets/` with MANIFEST rows.
4. **Worn cleats**: `client/model/armor/HematicCleatsModel` (boots only: an iron shell and sole, a sinew ankle wrap, two
   toe spurs, a heel spur, a spur splayed off each side, two studs), its layer registered in `LayerEvents`, the
   extension `ItemClientExtensions.hematicCleatsItem` (only `getHumanoidArmorModel`, so the flat icon stays),
   `HematicCleatsItem.getArmorTexture`, and the texture `textures/models/armor/hematic_cleats.png` (64 x 32) painted by
   `tools/model_export/living_land_sprites.py` (`worn_cleats`).
5. **Planted Staff rings**: `LivingStaffRingModel.withRings(base, rings)` with a weak model cache and
   `PLANTED_BAND_LIFT` of 3 px; `CardinalRiteBoundaryRenderer.drawPlantedStaff` wraps the planted model with it.
6. **A stalled machine's own screen**: `ReservoirReadout.Link` (none, drawing, owner offline, owner dead, below degree,
   blood dormant, out of reach, below fane degree, outside fane) with `Feed.of(link)`; `BloodRoutingHelper.linkStatus`
   (`canUseLink` is now that status being drawing); `BlockBloodVolume.link`, saved under `Link` only when not none;
   `BloodRoutingEvents.keepLinkStatus` on the routing pass resends with `BlockEntitySync.markAndSync` only on a change;
   `ReservoirReading.link`; `BloodVolumeBarWidget` draws a 5 x 5 broken-stitch pip on the bar's top edge and a gold
   reason line (or a grey drawing line) on hover. All seven station screens get it through the shared widget. The Gauge
   names the reason too: the single old `message.hemomancy.living_blood.feed.stalled` key was replaced by one per reason.
7. **Refused claim**: `VerbChannelOverlay` draws the four ticks pulled out 2 px, each crossed by a 3 px stroke in bone
   (`0xD8B8A888`), and "Refused" in place of the price, while `VerbRefusalClientState` holds.

### Fixtures as subcommands

`common/command/CoreLoopFixtures` holds placing, checking and clearing, the constants `RUN_LENGTH` 5, `VAT_VIGOR` 60,
`VAT_BUFFER` 400 and `TANK_FED` 600, and two UUID session maps (the fixture, the Temper snapshot) cleared through
`TransientServerStateEvents`. Four dev-only branches are wired in `HemoCommand.register`: `TemperPoolSubcommand`
(`/hemo temperpool carried|dry|restore [player]`), `TankSubcommand` (`clot|clear`), `VatSubcommand` (`fill|clear`) and
`GougeSubcommand` (`light|clear`). Each branch builds its actions with `CoreLoopFixtures.action` (the verifier folded the
four identical private `action`/`report` copies into it). Feedback uses `commands.hemomancy.temperpool.*`, `tank.*`, `vat.*`, `gouge.*` and
`fixture.*`, with amounts and names only.

Moving the fixtures exposed a real bug in the old scenario: the vat's lower half was placed with shape updates before its
upper half existed, broke off and dropped an item. It now goes in with `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`, and the
clear removes the halves top down the same way.

`gametest/CoreLoopTestScenarios` moved to `removed_assets/`; `HemoTestScenarioCatalog` lists only the Harbinger pilots;
`GameTestHarnessSourceContractTest` and `CoreLoopClosingGameTests` (which now runs the branches through the server's
dispatcher) were rewritten; `HemoCommandCharacterisationGameTests.fingerprint` clears each probe's fixture after counting
what it spawned. `TemperGuardrailTest` lists `CoreLoopFixtures` as a pool reader with its reason.

Goldens changed on purpose: `src/test/resources/command/hemo-tree.txt` (only the four new branches added) and
`src/gameTest/resources/command/hemo-command-fingerprints.txt` (18 lines added, none changed or removed).

### Tests added or changed

- JUnit `common/network/capa/harbinger/OnlookerPayloadsTest` (6 cases): round trips of the three new or changed small
  payloads, the `Link` and `Feed` mapping, and that every stalled reason is worded on the screen and the Gauge.
- `FirstConversionsGameTests`: the Lignum adventure-mode refusal now asserts exactly one `VerbRefusalPacket` with a
  cadence plus 4 ticks; the paid Pyretic item pulse asserts none.
- `LivingBloodGameTests`: the sutured-machine test asserts the tank's synced status is drawing near the owner (and that an
  unchanged state is not resent), out of reach at 40 blocks (and in the tank's tag), and below degree at the second
  Degree; the offline-owner test asserts owner offline.
- `CoreLoopClosingGameTests`: the four fixture tests run `/hemo temperpool`, `tank`, `vat` and `gouge` through the
  dispatcher and clear them.

### Lang and Liber

New keys, inserted as text: `screen.hemomancy.blood_volume_bar_widget.drawing` and `.stalled.<reason>` (7),
`message.hemomancy.living_blood.feed.stalled.<reason>` (7, replacing the old single key),
`hud.hemomancy.verb_channel.refused`, and 15 `commands.hemomancy.temperpool|tank|vat|gouge|fixture.*` keys. Liber:
`the_hematic_order/pages/living_blood` now says a sutured machine's own gauge shows a broken stitch and its reason. The
Venous Cling page never said the cleats look like boots, so it is unchanged. The page and first counts are unchanged
(29 and 27).

Docs updated: HEMOMANCY_REFERENCE (Tendon Line, stains, Staff rings, cleats, the stall reason, 8.0.1 additions, dev
fixtures), MANIPULATIONS_AND_CASTING, CORE_LOOP, ARCHITECTURE_STANDARDS (presence sync, own-stream Shapings, stall state,
fixture subcommands), TESTING (both new review launches, fixture text), NPC_DIALOGUE_AND_TRADE (protocol 49),
OPEN_QUESTIONS (CQ-56 to CQ-62; the two-client proof-owed line), 11-progression-teaching-tests (two fixture rows),
`tools/review/README.md` and `wiki/Blood-Systems.md`. The impact map was regenerated.

### Review tasks

- `visualFixesReviewClient` (`gametest/VisualFixesClientReview`, `build/visual-fixes-client-review`): 20 frames,
  `VISUAL_FIXES_REVIEW captured 20`, every one opened. Stains read as splashes on floor and wall in all three ages; the
  cleats show their spurs beside plain boots, front and side, and worn; the five-ring planted Staff shows five bands
  above the throat, the zero-ring one none, and the melting one still renders; the stalled Scriptorium shows the pip
  and, on hover, "Sutured, but stalled: its owner is below the third Degree."; the refused shot shows the barred bone
  ticks and "Refused", the claimed shot the red ticks and the price; Pyretic Forge shows the raw iron and then the
  ingots with no column across the view; the Tendon Line is one continuous cord from the palm to the clot on the wall
  from the side and in first person, and pulls taut while hauled. The first launch crashed opening the Scriptorium menu
  in the tick its block was placed (the client had no block entity yet); the harness now opens it ten ticks later.
- Onlooker review (`gametest/OnlookerClientReview`, `tools/review/review-onlooker.ps1`, rows `onlookerReviewClient` and
  `onlookerObserverClient`, port 25581): the host logged `ONLOOKER_REVIEW host finished` and the observer `ONLOOKER_REVIEW
  captured 26`, then a second run of the cling scenes alone (`-Only cling`, 4 frames) after their cameras were moved out
  of the ceiling. The full run's frames are kept in `build/p11/onlooker-run1`. From the observer: the bare stream lands
  on the near stone, and the Staff stream (five rings, Sanguine Reach 3) lands on and infuses the stone 10 blocks off
  (A2 reach); Lignum Mortis's stream reaches the log and fells it, and Pyretic Forge's Absorption draws from the brazier
  (A2 selection); the lash, the jet with its twin from the free off hand, the needle spray and the brand tether at Taut
  Line 2 and Deep Brand 2 all draw for the onlooker; the cling's roots and the cleats' spurs show on a wall and under a
  ceiling; the Tendon cord shows thrown and hauled (A1); the field of dark shows from outside as a dark wash on the floor.
- The launcher's one Gradle call runs `--no-daemon`, and both clients then start as bare java with no Gradle alive. The
  run task adds its classpath and `-Dfml.modFolders` only when it executes, so the new `write<Run>Classpath` tasks (for
  table rows marked `bare: true`) write both into `build/moddev/<run>RunClasspath.txt`.

### My calls

- Port 25581 for the onlooker host.
- The presence payload is sent only while a verb is channelled, and carries the active verb as well as both reaches and
  the selection. The brief's fields left out the verb; without it a remote Staff always read Projection.
- A relog forgets a player's fixture and Temper snapshot; the fixture's blocks stay.
- The four branch names `temperpool`, `tank`, `vat` and `gouge`. Each `clear` takes away only its own kind, and a player
  has one world fixture at a time.
- The refusal mark is pale bone `0xD8B8A888`, a little clear rather than the brief's opaque `0xFFB8A888`, lasting the
  Shaping's cadence plus 4 ticks. An out-of-blood refusal shows it too.
- Pyretic Forge drops the full outpouring. Its forge is 0.45 size sunk 0.14 under items and 0.8 above a fired block, and
  the FORGE cue now scales by radius below one block for every sender. That also shrinks two senders no review client
  shot: a Will's attached forge in `EntityCasts` (0.7) and the Brazier transmutation's bowl forge (0.8).
- Three stain variants per age, each keeping its outline as it ages.
- The cleats keep a flat icon. The worn model adds side spurs, which the brief did not ask for, because the toe and heel
  spurs alone were too small to read in the first shots; the iron was lightened after those shots read maroon.
- Staff rings travel over the wire and are never saved. The escrowed Staff's stamp wins, else the owner's rings while
  online; planted bands are lifted 3 px.
- A fane suture whose owner is below the fifth Degree is its own reason (`BELOW_FANE_DEGREE`), so the enum has nine
  values, not the brief's eight.
- `ClingService.onStartTracking` also sends the Tendon anchor, rather than adding another subscriber.
- The cord's sheath is 1.9 times the body's width and its seam 0.28 times; draw distance 96 blocks.

### Deferred

- The onlooker's lash did not visibly sweep in the frames taken (the host turns its view from the client tick, and the
  frames caught it straight). Nothing seen needed the cue smoothing in `StrandClientState.follow`, so it was not changed.
- A remote Shaping's own `reachScale(player)` and `claims` still read the caster's unsynced state on the onlooker's
  client (Long Strand's length, for one), so some strands may land slightly off for onlookers (CQ-56).
- Pyretic Forge in third person keeps the use pose but has no casting arm, and from behind the player's body hides the
  heap. Recorded, not changed.
- While a hauled player moves fast, the cell hand's ambient cells are left behind as a short streak (seen by the
  onlooker). It is not the bead trail; left as is.
- The field of dark reads from outside as a dark floor wash only; its fog is for those inside. Unchanged.
- `ShapingsClientReview` was not relaunched: the new harness covers its Pyretic Forge and Tendon Line shots in first and
  third person.

### Numbers to tune

- `TendonLineRenderer`: `HAND_WIDTH` 0.10, `ROOT_WIDTH` 0.07, `SLACK_SAG` 0.22, `SLACK_PULSE` 0.32, `TAUT_PULSE` 0.9,
  `SINEW_TURNS` 7, `SINEW_RADIUS` 0.045, `DRAW_DISTANCE` 96.
- `PyreticForgeShaping`: `ITEM_FORGE_SCALE` 0.45, `ITEM_FORGE_SINK` 0.14, `BLOCK_FORGE_SCALE` 0.8.
- `ShapingDispatcher.REFUSAL_MARK_GRACE_TICKS` 4; `VerbPresence.REFRESH_TICKS` 10.
- `LivingStaffRingModel.PLANTED_BAND_LIFT` 3 px.
- `CoreLoopFixtures`: `RUN_LENGTH` 5, `VAT_VIGOR` 60, `VAT_BUFFER` 400, `TANK_FED` 600, `AHEAD` 2.
- The stain painter in `ground_network.py`: `STAIN_ALPHA` (wet 228, tacky 200, dry 160), crack width 0.13 radians, flake
  chance 0.12.

### For later packages

- PROTOCOL_VERSION is 49. Any payload change bumps it again and rewrites the registry.
- A Shaping that draws its own link from hand to target returns `drawsOwnStream()` true.
- Another player's verb on the client is read through `ClientBridge.onlooker*` by `VerbReach`, `ShapingDispatcher` and
  `VerbChannel`; never read a remote player's attachments for reach or selection.
- A tank's `Link` key exists only while it is sutured; tag comparisons on unsutured tanks are unchanged.
- For another two-client review, copy `OnlookerClientReview` and `review-onlooker.ps1`, and mark the rows `bare: true`.

### Verification

- `./gradlew.bat compileJava compileTestJava compileGameTestJava`: BUILD SUCCESSFUL.
- `./gradlew.bat test`: BUILD SUCCESSFUL, 5,857 tests, 0 failures, 0 errors, 0 skipped.
- `./gradlew.bat runGameTestServer` twice after the last code change: "All 1808 required tests passed :)" both times,
  with no new `*.actual.txt`. Before the golden update, two runs failed only `everyhemobranchkeepsitsoutcome` on the new
  fingerprint lines; the first of them also showed the vat fixture's stray item, fixed above.
- No git write command was run. Only one Gradle process ran at a time, and none while the two onlooker clients ran.
  AGENTS.md is untouched. Removed files went to `removed_assets/` with MANIFEST rows.

### Verified

An independent verifier (first pass) read the package against the scope, the three rules and the guardrails. Protocol 49
and the registry snapshot match the three changed or new payloads; every new lang key exists (the stalled reasons are
built from `Link.key()` and all seven are worded); no vanilla particle, literal English, "tendency", blood payment or
player block edit was added. The verifier opened every `visualFixesReviewClient` frame and the onlooker frames
(`build/p11/onlooker-run1` and the `-Only cling` re-run in `build/onlooker-observer/screenshots`), and no client code
changed after they were taken. They show what the record says, with these limits: the stall pip is a small 5 x 5 mark,
so a stall reads clearly only on hover; the `pyretic-third` frame faces the caster and does not show the heap (already
deferred); and the `lash-swept` frames show a straight lash (already deferred). The one fix: the four fixture
subcommands each carried identical private `action`/`report` helpers, now one `CoreLoopFixtures.action`. The claim that
the fingerprint golden only gained lines could not be checked against a pre-P11 copy, because the whole overhaul is
uncommitted. After the fix: compile BUILD SUCCESSFUL; `./gradlew.bat test` 5,857 tests, 0 failures, 0 errors, 0
skipped; `./gradlew.bat runGameTestServer` twice in a row, "All 1808 required tests passed :)" both times, with no new
`*.actual.txt`.

## P12: Docs, AGENTS patch, play-test script, proof

P12 closes round two. It changed no mod code, no test code, no lang key, no payload and no golden: it made the docs
true for both rounds, wrote the `AGENTS.md` patch and the play-test script, wrote this section and the closing section
below, and ran the final proof. `PROTOCOL_VERSION` is still 49. `AGENTS.md`, `docs/OWNER_ANSWER_SHEET.md` and
`docs/audits/2026-11-11-visual-balance/` were not touched.

### What was done

- **New files.** `docs/archive/tracks/core_loop/AGENTS_PATCH.md`: six Part A replacements, each with the current line copied byte for
  byte from `AGENTS.md` (checked by script: each is found exactly once) and its replacement; fifteen Part B bullets
  under `AGENTS.md`'s own four headings; Part C, six lines considered and left out. `docs/CORE_LOOP_PLAYTEST.md`:
  fourteen sittings, each with Stills, Fixture, Try in order, Feel and balance questions (111 in all, each naming where
  its number or rule lives) and Known limits, a common setup block from `hemo-tree.txt`, and a checklist.
- **`docs/OPEN_QUESTIONS.md` §1.8:** the intro rewritten for two rounds with links to both logs, the patch and the
  script; CL-2 marked ruled with what was built; CQ-4's vial half marked fixed (P6); CQ-12 and CQ-14 say round two left
  them; CQ-13 points at the patch; CQ-40 moved into numeric order; the closing paragraph no longer claims eight
  required playthrough failures, and notes that CQ-50 to CQ-62 are not on the answer sheet. §2's core-loop row names 27
  firsts and the round-two list; §3's core-loop bullet points at the script, and "Known test noise" is rewritten with
  this package's proof.
- **`docs/CORE_LOOP.md`:** the status paragraph for both rounds and protocol 49; four new rows in "Where it lives"
  (Onlookers, Visual fixes, Dev fixtures, Deterministic GameTests) and the Menagerie row extended; CL-2 added to
  "Rulings that bind"; "Calls made without the user", "Open questions" and "Proof owed" brought up to round two; the
  history table now has the CL-2 ruling and P1 to P12 in order.
- **Track files:** `00-README.md` (status, a "Round two" table of P1 to P12 and the three new files, the owner-decisions
  status); a short "Built in round two" section in `02`, `03`, `04`, `08`, `09`, `10` and `12` (05, 06, 07 and 11 already
  had theirs); `12-strand-combat.md`'s status line says built.
- **`docs/HEMOMANCY_REFERENCE.md`** (surgical; a read of lines 1 to 735 against both logs): the core-loop page count
  (24, now 29) and the third Degree's pages; a pointer from the Track 11 NPC paragraph to the P10 lessons; the
  validation paragraph's "five fixtures" and "all 24 pages"; Track 10's "Not built ... and advancements"; Temper's
  filling (Carrion) and catalysts (glowroot, emberpod); and a core-loop line in section 4's notable payloads (the verb
  key and verb pick, the snap, verb presence, the refusal mark, the Tendon anchor, staff rings on active rites).
- **`docs/LORE_REFERENCE.md`:** one paragraph after §4.1 for round two's lore (the brood, strain beds, carrion, the
  field of dark, the carried crossing, hunters), and §6.4a's Vial Centrifuge sentence corrected: it no longer stores
  250 mL a spin (living blood; `VialCentrifuge*` has no blood gain, matching `RetiredNamesGuardrailTest`).
- **Wiki:** `Blood-Systems.md` (Strain Beds among the third Degree's pages; the stale "verbs, not wheel entries" now
  says they sit on the wheel's inner ring; the refused-claim mark; the planted Staff's bands); one paragraph or bullet
  appended to `Home.md`, `Harbinger-Path.md` (the core loop by Degree), `School-Combat.md` (strands),
  `World-Content.md` (strain beds, hunters, floor spikes) and `Developer-Reference.md` (protocol 49, the fixtures, the
  isolation harness, the review clients). No pinned line was reworded.
- **Other topic docs:** `docs/README.md` (the core-loop row names both logs, the script and the patch; a paragraph on the
  owner answer sheet and the 2026-11-11 audit); `docs/TESTING.md` (a play-test pointer after the fixture commands;
  the playthrough paragraph's counts reconciled); `docs/PLAYTHROUGH_TESTS.md` (this run's line).
- **`docs/architecture/ARCHITECTURE_STANDARDS.md`:** "Protects" made true for both rounds on `PayloadProtocolVersionTest`
  (49), `DialogueRoutingGuardrailTest` (the 400-line cap, lessons inside the speakers' classes),
  `HemoCommandStructureGuardrailTest` (the fixture branches), `CoreLoopTeachingGuardrailTest` (27 firsts, 29 pages) and
  `TemperGuardrailTest` (the fixture reader); section 5 gains the filler tank, the run owner, the vessel menu's data
  slots and a core-loop lesson row; section 11 gains the determinism rules and `bare: true` rows; a new paragraph after
  the index lists the core-loop behaviour and data tests of both rounds and the fourteen core-loop GameTest namespaces.
  Every `*Test.java` under `architecture/` was already in the index.
- **Impact map:** `python gradle/source-text-impact-map.py` ("3712 rows, 602 tests pin files, 426 pin Java source,
  1141 Java files pinned"); the CSV is byte-identical to P11's, since no test changed.

### My calls

1. **Proof order.** The three consecutive `runGameTestServer` runs and the playthrough suite ran first, while the docs
   were being edited: no mod or test code changed in this package and no GameTest reads `docs/` or `wiki/` (checked by
   search). The JUnit suite and `alphaCheck`, which read docs, ran after the last doc edit.
2. **Read-only inputs stay read-only.** `docs/OWNER_ANSWER_SHEET.md` and `docs/audits/2026-11-11-visual-balance/` were
   read, cited by id and pointed at from `docs/README.md`, never edited.
3. **The script uses the `/hemo` branches, not `/hemo test`.** The core-loop fixtures are `/hemo temperpool|tank|vat|gouge`
   (`hemo-tree.txt`); every other setup is a real command from the same tree or an item. Review clients are described
   as stills only, because each places its scene, shoots and stops itself.
4. **Six Part A replacements, not four.** Besides the PlayerBlockEdits, pose, `/hemo` and networking lines, the patch
   replaces the `alphaCheck` line (isolation harness, the flake rule, the playthrough launcher, where the fixtures are)
   and the tools line (`bare: true` rows), because both are now wrong by omission.
5. **No new CQ id.** Nothing this package decided changes play; the patch is CQ-13's answer in waiting.
6. **Strands are seventeen memories**, not the sixteen the brief counted: sixteen `Strands.*` factories are attached by
   name, and `searingLashStrand` makes the seventeenth. The wiki says seventeen.
7. **The Centrifuge sentence in the lore reference** was corrected to the code (no blood made), not left as lore.
8. **The optional probe `apotheosritewithoutrescue`** failed in this run as in all five of P2's, and passed once in
   P10's single run. It is an optional decision probe that keeps a question open, so it was recorded, not chased.

### Deferred

- Nothing was left half done. What both rounds leave open is in the closing section below, by system.
- No review client was launched: nothing client-side changed in this package.
- The reused GameTest worlds (`build/phlegethontic-gametest/world`, about 70 GB at P1, and the playthrough world) and the
  leaked `blood-profile-packs-*` folders were left alone (334 GB free on the drive before the runs).

### For later packages

- `AGENTS.md` is unchanged; the patch's Part A blocks must still be found verbatim before pasting. If anyone edits
  those lines first, regenerate the current blocks from disk.
- `docs/CORE_LOOP_PLAYTEST.md` names commands from `src/test/resources/command/hemo-tree.txt`; a branch renamed there
  needs the script changed too.
- A number in the closing table that an NPC or the Liber says aloud is marked; tuning it means editing `en_us.json` or
  the page JSON as well.

### Verification

Run strictly one Gradle process at a time, each to its own log under `build/` (no code changed in the package, so the
order is as in My calls 1):

- `./gradlew.bat compileJava compileTestJava compileGameTestJava --console=plain` (`build/p12-compile.log`): "BUILD
  SUCCESSFUL in 7s", every task up to date.
- `./gradlew.bat runGameTestServer --console=plain`, three times in a row with nothing between them but doc edits
  (`build/p12-gt1.log`, `p12-gt2.log`, `p12-gt3.log`):

```
[16:32:37] [Server thread/INFO] [minecraft/GameTestServer]: All 1808 required tests passed :)
[16:35:56] [Server thread/INFO] [minecraft/GameTestServer]: All 1808 required tests passed :)
[16:39:01] [Server thread/INFO] [minecraft/GameTestServer]: All 1808 required tests passed :)
```

  Each read "1808 GAME TESTS COMPLETE" in 2.42 to 2.48 minutes and "BUILD SUCCESSFUL". No `*.actual.txt` was written.
- `./gradlew.bat runPlaythroughGameTestServer --offline --console=plain` (`build/p12-playthrough.log`): "All 427
  required tests passed :)", "98 optional tests failed", "BUILD SUCCESSFUL in 2m 34s". The 98 optional names
  (`build/p12-playthrough-optional.txt`) are identical to P2's fifth soak run (`build/p2-soak-5-optional.txt`; `diff`
  empty). Against P10's single run the one difference is `apotheosritewithoutrescue`, which failed here and passed there.
- `./gradlew.bat test --rerun --console=plain` (`build/p12-test.log`), after the docs above were written: "BUILD
  SUCCESSFUL in 1m 34s"; the XML reports total 5,857 tests, 0 failures, 0 errors, 0 skipped. No doc pin moved.
- `./gradlew.bat alphaCheck --console=plain` (`build/p12-alphacheck.log`): `:test` was UP-TO-DATE (the run above), and
  the combined GameTest server ran fresh, a fourth consecutive green run: "[16:56:55] ... All 1808 required tests passed
  :)"; then `verifyGameTestResourceLog`, `verifyPackagedClientResources` ("Packaged client resource paths, PNG headers
  and font license verified.") and "BUILD SUCCESSFUL in 2m 56s".
- `./gradlew.bat test --rerun --console=plain` once more after the last doc edit but this line
  (`build/p12-test-final.log`; TESTING.md and PLAYTHROUGH_TESTS.md had changed after the first run): "BUILD SUCCESSFUL in
  1m 34s", 5,857 tests, 0 failures, 0 errors, 0 skipped. Then, after this section was written,
  `./gradlew.bat test --tests "*RepositoryTextEncodingTest" --tests "*ArchitectureStandardsIndexTest" --console=plain`
  (`build/p12-doc-guards.log`): BUILD SUCCESSFUL.
- `git status --short AGENTS.md docs/OWNER_ANSWER_SHEET.md docs/audits/2026-11-11-visual-balance` shows `AGENTS.md`
  unmodified and the other two untracked as before, with their modification times unchanged (2026-11-09 20:17:40 and
  2026-11-11 13:36:28). No git write command was run, only one Gradle process ran at a time, and nothing was deleted.

### Verified

An independent verifier checked this package on 2026-11-11 and found nothing to correct. The proof logs above exist
and say what is quoted. A script confirmed that each of the six Part A "current text" blocks in `AGENTS_PATCH.md` is
found exactly once in `AGENTS.md`. Every class the patch names exists, and so do the methods it names, among them
`ReservoirBlood.settled`/`tank`, the four `TemperCharge.fill*`, the `ClientBridge.onlooker*` trio,
`PlayerBlockEdits.lift`/`settle`/`replace`, `SpecimenVesselMenu` dispatching to `pressBrood`, and `DevCommands::available`.
The other details checked out too: `StrandCue` bits 20 to 22, the pose order (`strand_stroke`, `venous_cling` before
`somnolent_seat`), the pinned counts (27 firsts, 29 pages) and `PROTOCOL_VERSION` "49". In `PLAYTEST.md`, every review
client row, directory and user name matches `gradle/review-clients.gradle`, and every `/hemo` path is in `hemo-tree.txt`
(the two misses are the `manipulations add <id>` arguments). In the closing table, twenty-five balance constants were
spot-checked against the code, and all match. The closing "My calls" list holds the counts it claims, per package. "Tendency" appears in the
touched docs only as a reference to the CL-28 rename. AGENTS.md is unmodified. The verifier's own runs, one at a time:
compile "BUILD SUCCESSFUL in 7s" (`build/p12v-compile.log`); `./gradlew.bat test --rerun` "BUILD SUCCESSFUL in 1m 33s",
5,857 tests, 0 failures, 0 errors, 0 skipped (`build/p12v-test.log`); two consecutive `runGameTestServer` runs,
"[17:07:44] ... All 1808 required tests passed :)" and "[17:10:42] ... All 1808 required tests passed :)"
(`build/p12v-gt1.log`, `p12v-gt2.log`), no `*.actual.txt` written.

## The whole round: built, calls, deferred, numbers

Round two ran on 2026-11-11 as twelve packages, each built by one agent and checked by an independent verifier, one
Gradle process at a time, all uncommitted. The owner's standing instruction: build everything, pick sensible numbers,
record every undecided design as a call, tune afterwards.

### Everything built, by package

| Package | Built | Required GameTests | JUnit tests |
| --- | --- | --- | --- |
| Round one end | (Track 11 verified, CL-28, the CL-2 correction) | 1,681 | see `BUILD_LOG.md` |
| P1 | The GameTest isolation harness (64-block grid, two margin chunks, chunk-aligned origin, `hemomancy:open_room`); root causes for every named and watchlist flake; Hematic Beacon's lines and the Shared Wake school; CQ-11, the old-save kick | 1,682 | 5,785 |
| P2 | `alphaCheck` green; the playthrough suite's required probes passing under the harness; four mod bugs (stack-aware inquiry ids, the met-Vicar flag, the caster's own rite ground, a stain in a Saint's tomb) and three stale tests | 1,684 | 5,787 |
| P3 | `admits` on five older Shapings (CQ-2); run owners for hot and cold runs; the gouge catalyst mirror; client bone meal; shared gutters; the Temper preview refresh; drip Condenser owners; strand pace only while out (CQ-10); `PlayerBlockEditGuardrailTest` per site over nine roots; fillers reading their main tank | 1,703 | 5,788 |
| P4 | Narrow Shapings; Sanguine Mending, Crimson Flame Conjuration, Furnace Veins, Glacial Grasp freeze and thaw, Glacial and Ferric Ramparts, Carrion Communion; two Liber pages | 1,734 | 5,798 |
| P5 | Six strand skills (ids 74 to 79, `StrandSkills`); eleven more strands; Blood Binding as the Animus brand; the stain run; the twin (CL-27); school shapes and first-person arm poses; a Saint test | 1,755 | 5,812 |
| P6 | The vessel screen (`SpecimenVesselMenu`, `VesselView`); syringe sampling in glass; the met tier and the wider Bestiary; quirks; the brood (`BroodRules`, `SpecimenBrood`); `first_brood` | 1,768 | 5,825 |
| P7 | Eight fungal strains on their substrates (`FungalStrains`); Salt-Stained Timber; glowroot and emberpod; Tainted Harvest; floor spikes; chum hunters by water; `first_strain_bed`; the `fungal_strains.py` generator | 1,775 | 5,832 |
| P8 | `FurnaceHeat` and hot runs burning furnaces; Blood Eclipse's field of dark (`DarkFields`, fog, `UMBRAL_FIELD`); carried Earthen Vein crossings (125 mL); waterlogged gouges; the JEI Gouge Catalysts category | 1,789 | 5,840 |
| P9 | Sixteen more core-loop firsts (27); forty older Liber pages registered with floors and openers (CQ-1); the school pages rewritten; `LiberPageRegistrationGuardrailTest` | 1,795 | 5,844 |
| P10 | 21 NPC lessons over six speakers (`CoreLoopLessons`, 260 lang keys); six item inquiries | 1,808 | 5,851 |
| P11 | Seven visual fixes; the onlooker sync (`PacketSyncVerbPresence`, `VerbRefusalPacket`; protocol 48 to 49); the fixtures as four dev `/hemo` branches; the onlooker two-client review | 1,808 | 5,857 |
| P12 | Docs for both rounds, `AGENTS_PATCH.md`, `PLAYTEST.md`, this section, the final proof | 1,808 | 5,857 |

The playthrough suite: "8 required tests failed" before the harness; "All 427 required tests passed" with 98 optional
probe failures since P2 (P10's one run read 97: `apotheosritewithoutrescue` passed once).

### Every "My calls" decision, in one list

Counts per package, matching each section's "My calls" heading (top-level bullets; sub-bullets folded into their
line): P1 7, P2 9, P3 18, P4 11 (plus one verifier call), P5 14, P6 14, P7 14, P8 11, P9 12, P10 10, P11 12, P12 8.
That is 140 calls, plus the P4 verifier's. CQ ids are the roll-up's.

**P1 (7)**
1. The isolation harness, not only per-test fixes; margin 2 chunks, about 50% more run time.
2. A random but chunk-aligned origin, not a fixed one (the test world is reused); `-PgameTestOrigin` still places it.
3. Hematic Beacon: a blocked random turn keeps the tip's own heading before retiring (`LuxTraceShaping.pulse`).
4. Shared Wake schools: a follower whose station is not open water swims behind the leader (`CourtSchoolRules.trailAt`).
5. The school GameTest always launches a full school of six.
6. Unreadable pre-rename manipulations are dropped, not converted (no migrations).
7. The feather test's follow check is half a block, not two.

**P2 (9)**
1. The Vicar's own newcomer card goes when the player's blood wakes, not at D1.
2. The met-Vicar flag is set by the Vicar's right-click (`mobInteract`), after the clarity check.
3. The charm-bearer fixture no longer marks a Vicar met.
4. `PlaythroughPlayer.openTopic` sends the read record only for an unread lore card; the server matches lore cards only.
5. A caster's own running rite claims their Projection over its ritual bounds: no infusion, no stain there.
6. A Saint's tomb closes over any replaceable cell (`SaintSarcophagusBlock.blockingCell`).
7. The dirty-flag flush stays inside the dispel test.
8. `PlaythroughSites.SITE_READY_TICKS` is 1000.
9. The ground diagnostic stays in `PlaythroughClock.rite`.

**P3 (18)**
1. Task J: the cycle key stays as it is.
2. An ownerless or absent-owner run smelts and holds crops but lights and freezes nothing.
3. A catalyst feeder keeps a run for whoever lit it, never whoever stocked it.
4. The Temper preview's last-sent value is a session map, not a field on `BloodTemper`.
5. The preview compares outcome and multiplier only.
6. The gouge catalyst mirror is proven by a GameTest, not a JUnit.
7. Synced school catalysts are clamped to at least 1 tick.
8. Bone meal on stamp-only soil under-predicts on the client rather than syncing stamps.
9. Client strand grace is 6 use ticks (`StrandShaping.CLIENT_OPEN_GRACE_TICKS`).
10. Pyretic Forge admits a block it is heating only while the recipe id matches.
11. Hewing's `admits` checks the struck block only (CN-14).
12. A drip-made Condenser takes the owner's crystallization when online, else 0.
13. `feedingBasin` prefers any fresh basin over the nearest dry one.
14. The collapse backlash in `HarbingerCardinalRiteEvents` was listed, not converted.
15. `EngramStampItem` was listed, not converted.
16. `CrimsonFireballEntity` was listed.
17. The Living Flail changes terrain only for a player thrower.
18. Furnace Veins melts with `lift`, not replace-with-air.

**P4 (11, plus one verifier call)**
1. The narrow slot: narrow Shapings in the order attached, before the main one; an open channel beats every claim; `selected` names the main one; a session continues only for the same Shaping.
2. Sanguine Mending: 10 durability or 3 HP a pulse; an anvil stage on the 8th pulse, facing kept; iron and snow golems not targeting the caster, not `canHarm` (CQ-17); worn or held gear stays the R cast's.
3. Crimson Flame Conjuration: two lights a pulse within 2; TNT only when struck, lifted then lit as the player's; never an Iron Brazier or a blood-taking block.
4. Furnace Veins: a bank of radius 2, at most 8, priced as one stream (CQ-15); 15 ticks of burn a pulse, less dry, never more with the Staff; an empty bank refused unpaid.
5. Glacial Grasp freeze: open water, and cruor only beside open water; ice placed with `PlayerBlockEdits.place`; it melts back to water (the R cast's to air, CQ-19).
6. Glacial Grasp thaw: ice and frosted ice to water; packed and blue ice left.
7. Glacial Rampart: block targets; held on its own ice it builds nothing; cells a body touches stay open.
8. Ferric Rampart: any face with room before it; the first segment faces the player; at most 12 standing within 64 blocks.
9. Carrion Communion: flesh 2, bone 3, skull 10, bone block 24 Mortem and 1 HP each (CQ-16); the Staff draws more remains, not more from each; skull 2 pulses, bone block 4; tag `carrion_remains`.
10. Liber: two pages (ordinality 30 and 31).
11. A blood tool beats a block's own use whenever the selected Shaping claims the aim.
12. (Verifier) A dry wall stands shorter, never under a quarter (`ShapingRules.standingTicks`).

**P5 (14)**
1. Six strand skills, not five (CQ-20).
2. Placement: Long Strand D2, Taut Line and Free Hand D3 on the surface; Whipcrack and Deep Brand D5 and Second Strand D6 on the deep layer (CQ-21).
3. Skill numbers: Long Strand x1.1/1.2/1.3; Taut Line +20% follow and +25% jet fill a level; Free Hand +0.1 pace a level up to walking; Whipcrack +15% accent damage and +20% accent state a level; Deep Brand sets in 20/15/10 and lasts 600/900/1200.
4. The twin is a copy of the open strand from an empty or cell-hand off hand, never refusing the strand (CQ-22).
5. Every spray pulse is an accent (CQ-26).
6. Cruor Javelin is Congeatio's jet; no new memory.
7. Three Mortem brands differ only by price and Grave Debt's Necrosis (CQ-23).
8. Blood Binding brands every creature it holds (CQ-24).
9. The stain run: six-connected wet cells within 8, 4 plus 2 a pulse up to 20, 0.75 wide, 2 ticks apart, one hit a body, fixed at release (CQ-25).
10. Eruption spikes about 3.5 per block of radius, 3 to 12.
11. First-person arm angles per form (lash, jet, spray, plant, brand).
12. School looks: Tenebris edge `0xE6DDF2`, Ferric's plant pour in blood, prism light with no silver.
13. `FirstPersonPalm` keeps one palm per arm.
14. The Saint test uses the Hemorath fixture's level-added player and proves not-fed by zero spend.

**P6 (14)**
1. D1: the vessel screen is a slotless menu with data slots and no payload.
2. D2: a met tier below read and studied; profiled kinds only; Creative meets everything (CQ-30).
3. D3: quirks are notes read from existing rules (CQ-31).
4. D4: a vial is refused at no cost on a syringe-only kind; the syringe draws for 3 Vigor (CQ-32).
5. D5, the brood (CQ-27 to CQ-29): neighbours N, E, S, W at one height with one owner; the pressed vessel bears; refusals; 75 Vigor; cost max(150, 30 x maxHealth) over 120 fed seconds; 1 Vigor per 10 paid seconds and 20 at birth; rest 6,000; delivery into an adjacent empty vessel, never loose; inheritance rules; parting on break, lift, death; hostile kinds may breed.
6. The brood's button is on both screens, its refusal a live dry run.
7. Met Creatures is the Bestiary's first section.
8. Unread kinds show as many hollow phials as Tempers (CQ-33).
9. The screen is 300 by 236; quirks at 0.75 scale.
10. The thread arcs 0.75 over the lids.
11. The birth's bloom comes from block event 2, no new particle or payload.
12. No `PacketLivingSyringeDraw` for a vessel draw.
13. The undead check uses `EntityTypeTags.UNDEAD`; other kinds go in `menagerie_no_brood`.
14. The `Met` set also holds kinds met before examination.

**P7 (14)**
1. The "Salt-Stained Log" is the voyager's lore log, inoculant of a new block, Salt-Stained Timber (CQ-36).
2. The renewable timber recipe gives 5 (the seed returned); timber is not a log.
3. The substrate groups (CQ-35).
4. A strain is a `BushBlock`, not a `CropBlock`, so the cloche keeps culturing spores.
5. Strains need no light; their roll is 1 in 6; bone meal adds one stage on fed ground; Heartwood is not self-feeding.
6. Strain harvest: the spore back, a second at 50% when ripe (CQ-39).
7. The strain-bed first is granted in code on a ripe cut.
8. Catalyst crops: Lux and Flammeus only, worth 2, needing fed ground, their own seed; ripe glowroot light 4, Incandescent light 3 (CQ-38).
9. Tainted Harvest: 8%, only at level 2; food 3/0.3; 12 mL a point; gouge feed 1,200 ticks (CQ-37).
10. Floor spikes: 10%, up to 3, a gap of 2; the hanging spike also stops 2 above (CQ-40).
11. Chum hunters: home waters beat swamp; a Bog Revenant on the nearest bank within 3; cap 1 across kinds; EVENT spawns (CQ-34).
12. The hunter cue from toolkit forms only, with the elder guardian curse sound low.
13. Atlas: timber in fungal ecology at D3, crops and Tainted Harvest with the gourds at D2.
14. Liber page text stays literal English in the page JSON, as every HutosLib page does.

**P8 (11)**
1. The field of dark hides players too, the caster included (CQ-41).
2. Field numbers: 20 ticks per light level, at least 40, at most 2,400; radius 3 to 8; soft edge 1.5; dry floor a quarter; one field per player; Obscured 30 ticks refreshed every 10.
3. Fields are session state, not saved.
4. The field's centre is the aimed block.
5. Furnaces per hot run: every one beside a hot cell, no cap, 15 ticks a beat (CQ-43).
6. The carried crossing: a flat 125 mL between two stented Earthen Veins one living run joins (CQ-42).
7. Gouges hold water like glow lichen (CQ-44).
8. Ground runs draw at `AFTER_CUTOUT_BLOCKS`.
9. The snuff's stream uses the `EXTRACTION` flow style.
10. The Vicar's lines were written to his sheet without the owner's line-by-line review.
11. JEI pages follow `GroundFunction` order; the bare run has none.

**P9 (12)**
1. The sixteen first ids, titles, icons and parents (CQ-45).
2. Brought On credited between the phase's chance and the raised chance, to every awake player on counted ground (CQ-48).
3. Carried by the Vein on payment, not arrival (CQ-49).
4. What the Dead Give on every remain, even at the Mortem cap.
5. Stoked on any furnace the run burns, not gated on the owner's blood.
6. The older pages' Degree floors (CQ-46).
7. Learning a memory opens its school's pages in both chapters, the overview and its item's pages.
8. Liber Immaculatus floors by purity and clarity stage.
9. School pages list every memory with type and one line, no prices (CQ-47).
10. Annetta's page opens on the Harbinger route only.
11. Liber Immaculatus text not edited.
12. No page relies on an item alone: every older page has a floor.

**P10 (10)**
1. The assignment: 21 lessons (fed ground and strain beds apart); the Vicar's `verbs` asks about the Draw and the Mark; `day` ends on meats; the vessel screen and brood go to the Vicar's `keeping`; the Tendon Line to the Artificer's `climb`; the Staff lesson's third branch is the pickaxe; Degrees follow the Liber floors (CQ-50).
2. Ids say `craft`, never a Quest Work word.
3. Lessons are lore cards that light the NOTICE sigil (CQ-51).
4. The Hermit stops at the fourth Degree; the Vein-Mason starts there (CQ-52, CQ-53).
5. A lesson opens Liber pages and nothing else; `rewardDelivered` true on a first DIALOGUE source (CQ-54).
6. Successors tell their profession's lessons in the original voice (CQ-55).
7. The route guard checks speaker type, life and reach itself.
8. Lines fit the box at 1280 by 720, GUI scale 3; four nodes and four card titles trimmed.
9. Voice: the Alchemist and Artificer contracted, the Mnemonist a little, the Vicar asks a real question per lesson, the Vein-Mason one.
10. Inquiry voices: the Alchemist for the timber, crops and Tainted Harvest; the Artificer for the feeder; the Vein-Mason for the basin.

**P11 (12)**
1. Port 25581 for the onlooker host.
2. The presence payload is sent only while a verb is channelled and carries the active verb too (CQ-56).
3. A relog forgets a player's fixture and Temper snapshot; the blocks stay (CQ-62).
4. The branch names `temperpool`, `tank`, `vat`, `gouge`; each `clear` only its own kind; one world fixture a player.
5. The refusal mark is pale bone `0xD8B8A888`, for the cadence plus 4 ticks, out-of-blood included (CQ-57).
6. Pyretic Forge drops the outpouring; the FORGE cue scales below radius 1 for every sender (CQ-58).
7. Three stain variants per age, one outline each (CQ-59).
8. The cleats keep a flat icon; side spurs added; the iron lightened (CQ-60).
9. Staff rings travel over the wire, never saved; the escrowed stamp wins; bands lifted 3 px (CQ-61).
10. `Link` has nine values: `BELOW_FANE_DEGREE` is its own.
11. `ClingService.onStartTracking` also sends the Tendon anchor.
12. The cord: sheath 1.9 times the body, seam 0.28 times, draw distance 96.

**P12 (8)**: the eight in this package's "My calls" above (proof order; read-only inputs; `/hemo` branches in the
script; six replacements; no new CQ id; seventeen strand memories; the Centrifuge sentence; the optional Apotheos probe).

### Everything still deferred, by system

Merged from every package's "Deferred" and "Open after verify". Round one's closing list (`BUILD_LOG.md`, "Everything
deferred across the run, by track") is walked item by item at the end of this section.

- **Verbs and Shapings.** Vascular Dowsing's veins toward ore and "Lode Draw follows the veins" (need an owner-only cue,
  CN-3). A narrow Shaping's `SweepTrail` mark is forgotten only on a release that finds a session open. No Absorption
  sigil-wins GameTest for the thaw or Carrion. A held session is not re-checked for protection each pulse (CN-13).
  Selection-free brazier recipes (CQ-5). The converted memories' R casts (CL-21). The Will's own projection range and
  the three-argument absorb overload. Strain built by item or fluid draws. The Tendon anchor is not saved; no JEI row
  for Pyretic Forge's smelt at the brazier; a haul builds no strain.
- **Strands.** The lash spike 12a. The twin lash, jet and cone proven at unit level only, and the twin lash's sweeps
  mostly inside the lead's hurt cooldown. Thin Mortem brand tethers from the side. An empty off hand draws no
  first-person arm for the twin. No first for a twin, stain run or binding brand beyond P9's three firsts; no two-client
  proof of skill-driven motion beyond P11's frames (the lash looked straight). Server cost of many lashing players.
  Infusion inputs and structure frames claimed by a strand (CL-4). CQ-56: a remote Shaping's reach and claims read the
  caster's unsynced skills on an onlooker's client.
- **Living blood.** A scab spike drips from any tank above it (CQ-3's open half). Hopper extraction of clot outside the
  Condenser; nothing implements `IBloodRoutingTarget`; the Vitality Chalice; a bloodline member standing in for an
  offline owner (CN-9); the 67 Formation recipes (CB-28); couriers and rite holds keeping blood fresh (CQ-6); the two
  free Formation sources (LP-1). Another player's Projection can still infuse someone else's rite floor (CN-2). The
  stall pip is small; a stall is clear only on hover.
- **Temper.** A full body draws no Temper (CQ-9); the scar floor is a refill (LP-13); no JEI or tooltip line for a
  catalyst's worth; dry strength reaches damage and Shaping work only.
- **Menagerie.** Vessel ownership for strangers (CQ-4's open half). No syringe draw animation on vessels (needs a payload
  change, CN-8). No `ScreenChromeReview` scene for the vessel screen. Bestiary dossier previews hidden at GUI scale 3. A
  husk or preserved specimen shows a disabled brood button. No tug-of-war for a led creature. A brood kept by a Vigil or
  suture (CQ-28).
- **Ground network.** A catalyst feeder keeps a run working with no blood after the first pulse (CQ-7's open half), and
  with CQ-43's uncapped furnaces. The gouge catalyst sync and JEI mirror unproven on a dedicated server. The field's fog
  not seen inside a Saint fight or a rite; no two-client look at a field from inside; two fields with one centre share a
  client cue.
- **Farming and water.** Catalyst crops for six schools (CQ-38). The hunter biome reading proven by unit rules only; a
  Bog Revenant spawns in water with no bank in 3. No JEI for the cloche's crops. A torchflower at age 1 can taint. A
  basin charges for a bonus the crop's own roll would have given.
- **Staff and mining.** The Talaromyces vein and farmable rings (CQ-8); `lastVeinMineStart` still guards veins; the
  Living Crossbow keeps durability; spawn protection itself is untested (adventure mode stands in).
- **Living Land and cling.** Living Land as a strand plant (CN-4); no restart GameTest; the client sets the crawl speed;
  the cling HUD shows the root price; the cleats are a crafting recipe, not an Armature upgrade (CN-10). Pyretic Forge
  has no casting arm in third person. A fast-hauled player trails a short streak of ambient cells.
- **The day and the world's answer.** Bestiary world pages; Wills read region pressure, not recent spend; basin clotting
  does not count as spilled; no Tenebris or Lux answer; no spreading sod; a bat scans a box once a second; the Parish
  (CL-19).
- **Teaching.** Six core-loop pages with no Degree floor (Lignum Mortis, Pyretic Forge, Tendon Line, Brazier
  Transmutation, Mining by Projection, Ice and Iron Walls). Every first uses the task frame (CN-6). Old dev worlds do not
  get pages for advancements earned before P9. Liber Immaculatus text unedited; Unstained speakers, the Court, the
  Circus, the Monolith, the Voyager and the Wayfarer teach no lessons; successors keep the original voice; no test pins a
  spoken number to the code. The duplicated gouge lesson (Artificer and Vein-Mason) and two lesson lines that repeat
  existing text. Every lesson card shows the hub's shared Conversation summary.
- **Visuals.** The FORGE cue's shrink on a Will's forge and the brazier bowl never seen in a review client.
  `ShapingsClientReview` was not relaunched after P11.
- **Tests and tooling.** `runShapingGameTestServer` runs zero tests (its namespace filter drops vanilla-template tests;
  `gradle/game-tests.gradle`). The playthrough flakes `successionfinale` and `understudymnemonistpuppet`, and the four
  watchlist tests (coronation, chain-binding, ninth-pome, sutured-machine), have probable, not demonstrated, causes; none
  has failed since P1 and P2. The optional probe `apotheosritewithoutrescue` passed once in P10 and failed in every
  other run. The reused GameTest worlds (about 70 GB) and leaked `blood-profile-packs-*` temp folders. `AGENTS.md` not
  edited (the patch is ready).

Round one's deferred list, item by item (closed in Pn, or still open):

- **Track 0.** Rimebound Sentence and Glacial Grasp `mayAlter` gaps: open. `common/rite` and `common/entity` as scan
  roots: closed P3. A file naming `PlayerBlockEdits` once counting as guarded: closed P3 (per-site counts). Court
  transgressions and Myelin Borer agitation answering break checks: open. `VeinMasonScarLesson` ranking tempers itself:
  open.
- **Track 1.** Other players' streams following the plain pick and the onlooker's reach: closed P11. No Saint GameTest:
  closed P5 (`aSaintClaimsAheadOfAStrand`). The Will's range and the 5.5 absorb overload: open. No radial verb entry:
  superseded by the CL-2 ruling (2026-11-10). Strain from item or fluid draws: open.
- **Track 2.** The second-wave Shapings: closed P4, but for Vascular Dowsing toward ore. R casts kept (CL-21): open.
  Other players not seeing a Tendon Line: closed P11. The anchor not saved: open. No JEI row for Pyretic Forge at the
  brazier: open. A haul builds no strain: open. The Memory Thread a material: open (by design). Selection-free brazier
  recipes (CQ-5): open. The old-save crash (CQ-11): closed P1.
- **Track 3.** Hopper extraction: open. `IBloodRoutingTarget`: open. A stalled machine's screen not saying why: closed
  P11. The Vitality Chalice: open. A bloodline stand-in: open. The 67 Formation recipes: open. Fillers reading their own
  tank: closed P3. Couriers keeping blood fresh (CQ-6): open.
- **Track 4.** The blaze bloodless to Absorption: open by design (CN-15). A full body draws no Temper (CQ-9): open.
  Rendering recipes held by a test: open. Dry strength reach: open. No JEI or tooltip worth line: open. No `/hemo`
  Temper pool branch: closed P11 (`/hemo temperpool`). The Scrying preview going stale: closed P3.
- **Track 5.** No vat screen: closed P6. Temper marks not "unread": closed P6 (hollow phials). The Bestiary a fixed
  roster: closed P6. No syringe sampling from a vessel: closed P6. No quirks: closed P6 (as notes). No breeding: closed
  P6. No tug-of-war for a led creature: open. Vessel ownership (CQ-4): vial half closed P6, ownership half open.
- **Track 6.** The Tenebris field of dark: closed P8. Earthen Vein travel ignoring living veins: closed P8. Hot runs not
  lighting furnaces: closed P8. Water washing gouges away: closed P8. JEI for gouge catalysts: closed P8. Runs kept by
  catalysts and their edits (CQ-7): edits closed P3, the catalyst half open. A client ghost placement at the feeder:
  closed P3. Two basins on one gutter network flickering: closed P3.
- **Track 7.** Fungal strains as crops: closed P7. Catalyst crops: Lux and Flammeus closed P7, six open (CQ-38). Tainted
  harvest: closed P7. Floor stalagmites: closed P7. Drip draw rights (CQ-3): Condenser half closed P3, spike half open.
  Bone meal refused only on the server: closed P3. A basin charging for a bonus the crop's roll would have given: open.
- **Track 8.** Vascular Dowsing pairing with Lode Draw: open. The cut's shape by stance: open (by design). The planted
  Staff showing no rings: closed P11. The free Talaromyces vein and farmable age (CQ-8): open. `lastVeinMineStart`:
  open. The Living Crossbow's durability: open. No real spawn-protected test: open (adventure-mode refusals added P3).
- **Track 9.** Living Land as a strand plant: open. The older Shapings without `admits` (CQ-2): closed P3. No HUD
  refusal mark: closed P11. No restart GameTest: open. The client's crawl speed: open. No two-client look: closed P11.
  The cling HUD's root price: open. Cleats looking like iron boots: closed P11. Cleats as a crafting recipe: open.
- **Track 10.** Bestiary world pages: open. Wills reading region pressure: open. Basin clotting not spilled: open. Only
  vanilla drowned answering chum: closed P7. No Tenebris or Lux answer and no spreading sod: open. A bat's box scan:
  open.
- **Track 12.** The projection strand skill line: closed P5. The second-wave strands: closed P5. No two-client observer
  launch: closed P11. No Saint case: closed P5. Server cost of ten lashing players: open. The first-person hand keeping
  its use pose: closed P5. A plant not running along a wet stain: closed P5. Strand pace off the claim (CQ-10): closed
  P3. Infusion inputs and structure frames claimed by a strand (CL-4): open.
- **Teaching across the run.** Firsts for only nine systems: closed P9 (27). The missing firsts for a drip, basin
  harvest, chum catch, vat, study, Living Land strike, full night and Blood Moon: closed P9. No NPC lesson tree per
  system: closed P10.

### Every balance number, with where it lives

The 2026-11-11 tuning sheet (`docs/audits/2026-11-11-visual-balance/tuning-sheet.csv`, 239 rows) already lists round
one's numbers and many of round two's: body and income, coagulation, Temper (including Carrion's yields and the
glowroot and emberpod worth), every strand's price and the Second Strand cost, every Shaping's price (the second wave
included), the ground network (the hot-run furnace burn, the carried crossing, the dark field's main numbers), farming
(the taint chance, the strain rules, chum), the menagerie (the vat, sampling and brood summary), the Staff and the
world's answer. Read those there. The table below holds what the sheet lacks, plus round two's numbers spot-checked on
disk in this package. "Said aloud" marks a number an NPC lesson or the Liber states, so tuning it means editing
`en_us.json` or the page JSON too.

| System | Number | Value | Where | Said aloud |
| --- | --- | --- | --- | --- |
| Strand skills | Long Strand length per level | +0.1 (x1.1/1.2/1.3) | `StrandSkills.LENGTH_PER_LEVEL` | Artificer `strand_line` |
| Strand skills | Taut Line tip follow / jet quicken per level | +0.2 / +0.25 | `StrandSkills.TAUT_PER_LEVEL`, `QUICKEN_PER_LEVEL` | |
| Strand skills | Free Hand pace per level | +0.1, never past walking | `StrandSkills.PACE_PER_LEVEL` | Artificer |
| Strand skills | Whipcrack accent damage / state per level | +0.15 / +0.2 | `StrandSkills.ACCENT_DAMAGE_PER_LEVEL`, `ACCENT_STATE_PER_LEVEL` | |
| Strand skills | Deep Brand set / lapse | 20/15/10 and 600/900/1200 ticks | `StrandSkills` | skill description (seconds) |
| Strand skills | Twin cost, tautness, sweep, jet, plant pulses, brand radius | 1.5, 0.85, 0.5, 1.4, 2, 4 | `StrandSkills.TWIN_*` | Artificer ("half again") |
| Strand skills | Twin spray cone | 50 degrees | `SprayRules.TWIN_MIN_DOT` | |
| Strand skills | Skill Degrees, costs, tree places | ids 74 to 79 | `LivingStaffSkillBranch` | Artificer (Degrees) |
| Stain run | Base, per pulse, max cells; reach; step; width | 4, 2, 20; 8; 2 ticks; 0.75 | `PlantRules.STAIN_*` | |
| Strands | Client pace grace at open | 6 use ticks | `StrandShaping.CLIENT_OPEN_GRACE_TICKS` | |
| Second Shapings | Dry wall floor | a quarter of standing time | `ShapingRules.standingTicks` | |
| Second Shapings | Ferric Rampart cap | 12 segments | `FerricRampartShaping.MAX_SEGMENTS` | Vein-Mason `walls` |
| Second Shapings | Furnace Veins burn per pulse | 15 ticks | `FurnaceVeinsShaping` (`burnTicks`) | |
| Menagerie | Brood numbers | 75 Vigor; 30 mL per health, at least 150; 120 s; 1 Vigor per 10 s; 20 at birth; rest 6,000; young 40 at age -24,000; inherit 0.5; shown cost cap 32,000 | `BroodRules` | Vicar `keeping` (three quarters, two minutes, five minutes); Liber `the_brood` |
| Menagerie | Syringe / vial sample | 3 / 5 Vigor | `VatVigorRules.SYRINGE_SAMPLE_COST`, `SAMPLE_COST` | Alchemist `humours` |
| Menagerie | Thread arc, resend step | 0.75 blocks; every 5% | `SpecimenBrood.THREAD_ARC`, `SYNC_STEPS` | |
| Menagerie | Thread look | width 0.045 (+0.045 at term), 24 segments, 9 bloom arms over 30 ticks | `BroodThreadRenderer` | |
| Menagerie | Vessel screen reach | 8 blocks | `SpecimenVesselMenu.REACH_SQR` | |
| Farming | Floor spikes | chance 0.1, up to 3, gap 2 | `BloodFedGrowthRules.FLOOR_GROW_CHANCE`, `MAX_FLOOR_SPIKE`, `FLOOR_SPIKE_GAP` | |
| Farming | Drip spike growth | 0.1 a drip, up to 7 | `BloodFedGrowthRules.DRIP_GROW_CHANCE` | |
| Farming | Strain bone meal | one stage | `FungalStrainRules.BONEMEAL_STAGES` | |
| Farming | Light when ripe | glowroot 4, Incandescent strain 3 | `BlockInit` | |
| Farming | Timber recipes | log + 8 logs = 8; timber + kelp + 4 logs = 5 | `recipe/salt_stained_timber_from_voyager_log.json`, `salt_stained_timber.json` (generator `fungal_strains.py`) | Alchemist `beds` |
| Farming | Catalyst crop yield | 1 unripe, 2 or 3 ripe | `loot_table/blocks/glowroot.json`, `emberpod.json` | |
| Farming | Tainted Harvest food | 3 / 0.3 | `ItemInit` | |
| Chum | Hunter depth, swamp depth, pool strength, cap | 2, 1, 0.75, 1 | `WorldAnswerRules.HUNTER_DEPTH`, `SWAMP_HUNTER_DEPTH`, `HUNTER_POOL_STRENGTH`, `HUNTER_CAP` | |
| Ground | Dark field work, Obscured, resend | 10, 30, 20 ticks | `DarkFields.WORK_INTERVAL`, `OBSCURED_TICKS`, `RESEND_INTERVAL` | Mnemonist `land` (hidden past four blocks) |
| Ground | Dark field radius per remaining ticks, soft edge | 1 block per 400 ticks; 1.5 | `DarkFieldRules.TICKS_PER_BLOCK`, `SOFT_EDGE` | |
| Ground | Field fog | near 3, far 14, ease 5 ticks | `UmbralFieldFog.NEAR`, `FAR`, `EASE` | |
| Ground | Carried crossing | 125 mL | `EarthenVeinTravelRules.LIVING_VEIN_COST` | Vein-Mason `carried`, Vicar `rooted_vein.recovery`, Liber `living_veins_and_basins` |
| Ground | Gouge catalyst sync caps | 16 functions, 32,767 characters | `GougeCatalysts.MAX_SYNCED_FUNCTIONS`, `MAX_SYNCED_JSON_LENGTH` | |
| Visual fixes | Tendon cord | widths 0.10 to 0.07; sag 0.22; pulses 0.32 / 0.9; sinew 7 turns at 0.045; draw 96 blocks | `TendonLineRenderer` | |
| Visual fixes | Pyretic forge | items 0.45 sunk 0.14; blocks 0.8 | `PyreticForgeShaping.ITEM_FORGE_SCALE`, `ITEM_FORGE_SINK`, `BLOCK_FORGE_SCALE` | |
| Visual fixes | Refusal mark | cadence + 4 ticks, `0xD8B8A888` | `ShapingDispatcher.REFUSAL_MARK_GRACE_TICKS`, `VerbChannelOverlay` | |
| Visual fixes | Planted Staff bands lift | 3 px | `LivingStaffRingModel.PLANTED_BAND_LIFT` | |
| Visual fixes | Stain alpha | wet 228, tacky 200, dry 160 | `tools/model_export/ground_network.py` `STAIN_ALPHA` | |
| Onlookers | Presence resend | every 10 ticks on change | `VerbPresence.REFRESH_TICKS` | |
| Fixtures | Run length, vat Vigor and buffer, tank feed, distance ahead | 5, 60, 400, 600, 2 | `CoreLoopFixtures` | |
| Teaching | Older pages' Degree floors | D1 to D7 | `LiberKnowledgeHelper.unlockOlderForDegree` | |
| Teaching | Liber Immaculatus stage floors | by purity and clarity stage | `LiberKnowledgeHelper.unlockForUnstained` | |
| Teaching | Lesson Degree ranges | Hermit 0 to 4, Vein-Mason from 4, others from 1 to 3 | `CoreLoopLessons` | |
| Teaching | First parents, icons, frames | task frame for all 27 | `data/hemomancy/advancement/hemomancy/<id>.json` | |
| Tests | GameTest grid gap, margin chunks | 64; 2 | `GameTestIsolation.GRID_GAP`, `MARGIN_CHUNKS` (`-PgameTestMarginChunks`) | |
| Tests | Far-site readiness | 1,000 ticks | `PlaythroughSites.SITE_READY_TICKS` | |
| Shared Wake | School follower trail distance | its station's distance back (2.2 blocks a row) | `CourtSchoolRules.trailAt` | |

The other numbers P10's lessons say aloud (`BUILD_LOG_ROUND2.md`, P10, "Numbers said in dialogue") are all in the
tuning sheet or the table above: the regeneration and meal numbers, the five-minute window, Temper caps 20 and 80, the
catalyst worths, the cling's 6 and 4 mL, the cleats' quarter, the Tendon Line's three times reach, the Staff's ring
thresholds and the vein allowances.

### Not yet on the owner answer sheet

`docs/OWNER_ANSWER_SHEET.md` read P1 to P9 only. Not on it:

- CQ-50 to CQ-55 (P10's lessons: who teaches what, lore cards and the NOTICE sigil, the Hermit's ceiling, the
  Vein-Mason's floor, pages only, successors' voice).
- CQ-56 to CQ-62 (P11: verb presence for onlookers, the refusal mark, Pyretic Forge without the outpouring, stains, the
  worn cleats, planted Staff rings and the stall reason, the fixture commands).
- P10's verifier notes: the duplicated gouge lesson and the two lesson lines that repeat existing text.
- P11's verifier notes: the FORGE cue's shrink on a Will's forge and the brazier bowl, and the small stall pip.
- Rows the sheet holds that P10 and P11 answered in passing: CN-5 (NPC lessons for the core loop: built in P10) and
  CN-10 (the cleats' look: built in P11; the recipe-or-upgrade half stays open).

### Proof

- Required GameTests 1,808 (from 1,682 after P1), JUnit 5,857 (from 5,785), playthrough "All 427 required tests passed"
  with the same 98 optional probe failures as before the overhaul, `alphaCheck` BUILD SUCCESSFUL. The quotes are in P12's
  "Verification" above: four consecutive green combined GameTest runs (three by hand, the fourth inside `alphaCheck`),
  the playthrough run, the JUnit totals and the final check of the docs.
- `PROTOCOL_VERSION` 49; `payload-registry.txt` last rewritten in P11; no golden changed in P12.

## After round two: owner corrections

- **2026-11-11, the lead moved from Blood Binding to the Tendon Line.** The owner pointed out that Blood Binding holds a creature still, so a binding that turns into a leash made no sense. Blood Binding now only holds. The Tendon Line's throw (Projection) claims a creature as well as a block: thrown into a creature that may be kept and is passive or at a third of its health, the line roots in it and it is led at once (`MenagerieLeash.tieLine`); a refusal is checked in `admits`, so it costs nothing and says why. Held Absorption on a creature you lead reels it in (`TendonLineShaping.reelIn`). `MenagerieLeash.heldByBinding`, its hold map and `MenagerieRules.LEASH_HOLD_TICKS` are gone. Lang, the menagerie and Tendon Line Liber pages, the Vicar's lesson, the vat tooltip, the Kept advancement, the reference, the wiki and the play-test script say so. GameTests: `aTendonLineThrownIntoAWeakenedHostileLeadsIt`, `aHealthyHostileIsNotLedPaysNothingAndSaysWhy`, `bloodBindingHoldsAWeakenedCreatureButNeverLeadsIt`, `heldAbsorptionReelsALedCreatureIn`.
- **2026-11-11, the inner-ring verb pick is the plain verb.** Picking Projection or Absorption on the radial's inner ring now puts the selected memory down (`VerbSelection.choose`).
