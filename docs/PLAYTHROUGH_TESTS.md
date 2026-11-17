# Playthrough Tests

Status (2026-11-14, final run of the overnight build): 432 GameTests. "All 432 required tests passed :)", 59 optional
tests failed (`build/overnight/final-playthrough-latest.log`, names in `build/overnight/final-playthrough-optional.txt`).
The morning baseline was 427 tests with 98 optional failures. All 59 are in the baseline list: 39 left it, most because
the owner's 2026-11-13 rulings were built and their probes made required, and none joined. What each of the 59 waits on
is under [Proof owed and known gaps](#proof-owed-and-known-gaps). The owner's rulings are under
[Rulings that bind](#rulings-that-bind). The record of the run is
[owner_answers/BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md). (GameTest's summary line counts every
test, optional ones included.)

Status (2026-11-11, core loop round two, package P2): 427 GameTests, run under the GameTest isolation harness (64-block
grid, forced margin chunks; see [TESTING.md](TESTING.md) "Deterministic GameTests"). Every required test passes and the
98 optional probe failures are the same list as before the overhaul; the result is reproduced on 5 consecutive runs
(`docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md`, P2). `newcomerGuidanceOnlyBeforeTheHermit` is now
`newcomerGuidanceOnlyBeforeTheVicar`. Last run 2026-11-11 (round two P12, `build/p12-playthrough.log`): "All 427
required tests passed :)", "98 optional tests failed", the same 98 names as P2's runs (P10's one run read 97, when
`apotheosRiteWithoutRescue` passed). The status below is the first round's.

Status (2026-11-02): built and committed (`8d01a316c`, 2026-11-02); later probe and fix rows are in the working tree, uncommitted. The final run of this round (`build/playthrough-final-run5.log`) had 426
GameTests in `playthrough_validation`: all 315 required tests pass, and 100 of the 111 optional tests fail on purpose
(the decision probes and the `NOT RUN` probes). 214 tests are the D0 to D5 bands (170 required), 212 the late bands (145
required). Both late full runs pass. Harbinger path only: the D0 to D5 bands, the D5 to D8 bands (D5 Illuminatus, the
Brined Court, the Crimson Troupe, D6 Sanctified, D7 Archon, D8 Endgame), the late NPC usability band (`npc_late`) and the
D5 to D8 late full run (`PlaythroughLateRunGameTests`). Unstained playthroughs are not covered. (GameTest's own summary
line, "All 426 required tests passed", counts every test, optional ones included.)

## What it is

A reusable, player-centric end-to-end suite. Each test plays a stretch of the Harbinger path the way a person does:
right-clicking NPCs and choosing the options their screen offers, using items on blocks, placing stations, clicking menu
slots, holding Blood Projection and Absorption, planting a Living Staff for a rite, dying to real damage and
respawning, logging out and back in. It asserts on what the player can observe: inventory, degree, advancements, the
offered dialogue options (and that every key they show has English), chat and action-bar lines, block state.

Preconditions that are not under test are set up by fixture and labelled `// ARRANGE`. Everything under test goes
through the server entry point a real client reaches: the dialogue option and reward-choice payload handlers,
`NpcTradeService` through the trade request path, `useOn`/`use` of real items, block placement, `ServerboundGuards`-checked
menu and station payloads, and `BloodAccounting` through real projection and absorption.

Edge cases are first-class: wrong order, premature asks, repeat and double-sent claims, full inventory, Creative discard,
death and respawn mid-step, relog mid-step, two players at one NPC or station, a missing or killed NPC, forged events
from the wrong speaker or from far away.

**Decision probes** are `required = false` and carry a `// DECISION: D-n` line. Each asserts what a player would expect
and logs what actually happens, so a failing probe is a question for the user, not a bug to fix. The owner answered the
probes on 2026-11-13. A probe that still fails now marks a ruling that is not built yet, a row with no clear answer, or
a proof the GameTest server cannot run. They are listed by cause under
[Proof owed and known gaps](#proof-owed-and-known-gaps).

## How to run it

```
./gradlew.bat runPlaythroughGameTestServer --offline
```

It is on demand, not in `alphaCheck` (`gradle/game-tests.gradle`: `playthrough_validation`, `alpha: false`). A full run
takes under a minute of game time on the GameTest server. A green run ends with `All <n> required tests passed` and a
list of optional failures (the probes). Every test writes `[playthrough] <scenario>: ...` lines to the server log
(`build/playthrough-gametest/logs/latest.log`): what was offered, said, given, dropped and spent at each step. Read
those lines for the probes; they carry the evidence the questions in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) quote.

Only one Gradle build may run at a time: the build directory is shared, and a `NoClassDefFoundError` mid-run usually
means another session's build collided. Wait and run again.

## What it covers

| Band | Class | Covers |
| --- | --- | --- |
| Smoke | `PlaythroughSmokeGameTests` | The harness itself: a fresh player, a Hermit talk, a flask drink, a relog. |
| D0 onboarding | `PlaythroughOnboardingGameTests` | The Hermit's blessing, the Mortal Display touch (charm, awakening, lodestone), the farewell, the Vicar's initiation ceremony with its cancellations (walk away, damage, death, relog), a busy Vicar, starter supplies with a partial or full pack and in Creative, the Display refusing an occupied slot or awake blood, initiating before the farewell, founder-led reception and its cancellations (another line, distance, damage), the temple's fragility probes. |
| D1 Neophyte | `PlaythroughNeophyteGameTests` | The D1 Vicar hub, blueprints, conjuring and dispelling cell hands (with the refund synced), the four First Bloodcraft proofs (absorption, formation, Venous Stone, Liber structure), the claim and its kit, the Hermit Road Side card, the reward card's state, forged and far claims. |
| D2 Votary | `PlaythroughVotaryGameTests` | The D1 to D2 chain by play, First Separation (brief, samples, the assigned spin, recovery, claim), a D1 spin, the first distillation and heat, Concentrated Blood (asking, carrying, injecting, refusals, sleeping alone and with a second player awake), full-pack claims, Creative discard, the condenser collect with no room, Body Answers and Specimen Jar supplies, clinical lessons, Red Taxonomy, the Worn Vow and the Mnemonist starter. |
| D3 Initiate | `PlaythroughInitiateGameTests` | The D2 to D3 chain by play, the Woven Vessel (blank recipe, turn-in, offhand blank), the Loom by its Working craft, the first Blood Shot weave (also when Blood Shot is already known from the D2 starter, relogged mid-weave, and with a second player at the Loom), the Sanguine Brotherhood gate and ceremony, the guided Chamber (refusals here; restores only where the Chamber loads), the Warp Chair refusals, the Reef Watch Side chain, the Pallid Vessel Vocation with a full pack, the Artificer's Three Answers (brief, inspection, counsel), the D4 Circus waybill, hints and pointers. |
| D4 Adept | `PlaythroughAdeptGameTests` | The D3 to D4 chain (Woven Vessel, weave, Brotherhood) and the D4 to D5 chain (the Vicar's directive and map, the Anchorite's lesson, the supervised carve, the Scar burnt in, the Effigy, the commit, the Crimson Lodge), relogs through the Vein-Mason chapter, rank rites interrupted by death and by relog, the continuation kit, the D5 Mason strain, diagnosis and treatment, the D5 Court referral to a Voyager and the Relay Chart, the Bloodline and Fane founding rites at D4 and D5, scar names in refusals. |
| NPC usability | `PlaythroughNpcUsabilityGameTests` | Sweeps over every Harbinger NPC at every state from D0 to D5: lang keys, dead ends, options that do nothing, events a tree never offers, repeat and double-sent claims, death reclaims, claims sent dead or spectating, where rewards land, sigils against the work actually open, degree hints against the real chapter gate, trades, outposts, unique NPC loss. |
| NPC usability, late (`npc_late`) | `PlaythroughLateNpcUsabilityGameTests` | The same sweeps from a fresh D5 to D8 on both paths, crossed with the bloodline role (none, member, leader, leader with a recruit), a held Quintessence, purifying and Clarity: lang keys for every outpost NPC, the Monolith, successors, all 18 Court roles and the Circus (with Court standing and acclimation); dead and emptied nodes; root options with no hub card; every offered event routed and observable; forged unoffered late events refused (rewards, line members, the Archon path); the Vicar's D5/D6 hint against the chapter gate (all 8 Living Covenant combinations, members too); a pointer to each next step to D8 and the revelation choice after a relog; the late Trade box (no new cards past D4, reason keys, rapport, the 500 mL tithe in `BloodFlowLedger`, a successor serving only its line). The Succession cards by their player path (explain, bequest, donate with the Living Syringe) and a successor serving its line and refusing an outsider (`successionByPlay`). Probes: NPC-1, NPC-3, NPC-6, D5-5, D5-10, D7-8, D7-9, D-3 and D-10 (late). |
| D5 Illuminatus (`d5_illuminatus`) | `PlaythroughIlluminatusGameTests` | From a fresh D5: Bloodline Founding and the Founding Fane by play, founding again and joining a second line refused (LF-2), joining by a signed ledger, the Fane's refusals and relocation, a member's Bloodwell deposit (alive, D5, in the Fane), the Vicar's member guidance (LF-3), recruiting and releasing outpost Harbingers and forged recruits (LF-1), the ledger's summon, recall point, recall and disband, the pool screen's kick and relog (LF-14), the sealed Bloodline Covenant rite, Hematic Stakes, the D5 station kits with a Scriptorium inscription and the Eightfold Script, a kit pending through death (LF-4), the Crimson Vestment and Assumed Limb, the Alchemist's commissions, the Founding Fane interrupted by the caster's death or a long logout (Staff, Quintessence and offering each once afterwards), rite recovery held while dead and refused by a Creative full pack, the Rite of the Scarlet Summons (a member refused, the finale held with no member online and with too little in the pool, the kin drawn), and `chain` for the late run. Probes: D5-1, D5-2, D5-3, D5-4, D5-7, D5-8, D5-11. |
| Brined Court (`brined_court`) | `PlaythroughBrinedCourtGameTests` | The Court found by every route (the Voyager's referral and Relay Chart, a D4's Brine-sealed letter read at the Voyager, the Alchemist's lead with a worn strain, a site visit), guest standing, the Gardener's hospitality, customs by dialogue and by trade card, the Lightkeeper's lesson and bonding, joining and the free switch, the inner-house sweep and the 18-role sweep (lang keys, dead ends, routes), transgressions and the Warden, the Kindled Champion won and lost (the retry wait named first, LF-13), Hydra's and Marimo's presences (the player in the world, `enterWorld`), Morphic Nectar primalizing only for a D8 Apotheos, a Seat lead lost for good (D-10 late). Probes: D-10 (late); BC-1 to BC-4, BC-8, BC-9, BC-12 and NPC-2 were ruled and fixed (2026-11-14), and BC-5, BC-6, BC-7 and BC-10 on 2026-11-15. |
| Crimson Troupe (`crimson_troupe`) | `PlaythroughCrimsonTroupeGameTests` | Gated copies (2026-11-07): `CrimsonTroupePavilionGameTests` in `crimson_troupe_validation` runs both finales (Succession with acts 2-4 arranged), observation, the finale-reward claim and the lost-Ringmaster release through this class's bodies; "CT-n" labels name only decision probes. The D5 referral and the Scarlet Waybill, discovery and acclimation on the real pavilion template, first-talk milestones, the Crossbar attuned and charged, the Threadkeeper's inspect, report, claim and its cards (LF-7, LF-8), the school's refusals (LF-11), the Mnemonist practical and puppet, the Empty Ring, Wrong Audience and Missing Understudy investigations, the attention act and the route, a spectator owner (LF-9), faculty kept out of act 2 (LF-10), both finales, death and relog in a practical, an act and the finale, two pupils at one professor, the sweep and trades, the nine pomes blocking the Puppeteer ordeal. Probes: CT-1..CT-3, CT-5, CT-7, CT-8 (CT-6 logged). |
| D6 Sanctified (`d6_sanctified`) | `PlaythroughSanctifiedGameTests` | The Rite of the Bloodline Covenant by play (and a member's route through their own deposit), the D6 Vicar's guidance through every order of the three Living Covenant parts, two Sanguine Monolith Working crafts and the D6 Monolith tree, the Covenant Throne craft, bind and trance, the Covenant Vigil with a recruit and with a member (helper walks off, helper lost, caster relogs), the Living Covenant completed by the Throne and by the Vigil as the last part, the Rite of the Chamber of Will (LF-12), the Rite of the Hematic Order sealed before the Living Covenant, the Vein-Mason D6 chain, the Vagrant Mind inquiry, Eternal Covenant, the D6 whisper memo, an expelled member founding a line and binding a Throne, the NPC sweep. Probes: D6-1..D6-3, D6-5..D6-8, NPC-4, CHAIN-3. |
| D7 Archon (`d7_archon`) | `PlaythroughArchonGameTests` | The Rite of the Hematic Order with a recruit (and the helper dying), the Weight of the Frame by a real Armature cycle, the Monolith's press further and shatter (Seed and fragments), the Bloom of the Qliphoth with its refusals (a stranger's Bloom near, the cell blocked), pome picking, the nine pomes and the Spine, the first projection (degraded here), the revelation choice surviving death (LF-5), the Artificer's lineage recovery without the Silent Archon lineage at D7 (fix, was D7-7), `chain` for the late run. Probes: D7-1..D7-4, D7-6, CHAIN-2. |
| D8 Endgame (`d8_endgame`) | `PlaythroughEndgameGameTests` | Both roads from the revelation choice: the Rite of Apotheos by play (its refusals, a death mid-rite), the Apotheos body, the heart-node refused outside the Gardens, the Mycophant fought in the test level, its Tendril and make-room line (LF-6); the Severance cut with a Living Arsenal weapon, the wound refusal, the Memory of Vesper delivered, reissued and drawn in at a lit Iron Brazier, Silentium absorption and restrictions; the Rite of Ancestral Communion with three recruits from three outposts (refused with two); the Visitation and Possession refusals; the teachers' ending lines, the ledger's Blood Beneath bar; the Saint mausoleums (ward, Chapels, ring door, tomb, Recollection cysts). Probes: D8-1..D8-5, CHAIN-1, D-16 and D-19 (late). |
| Full run | `PlaythroughFullRunGameTests` | `harbingerD0ToD5`: one player from a fresh D0 to Degree 5 with nothing but play between the bands; at each boundary the player is compared with the fixture state the next band starts from. Optional until it has been stable for a few runs. |
| Late full run (`chain`) | `PlaythroughLateRunGameTests` | `harbingerD5ToApotheos` and `harbingerD5ToSilentium` (batches `playthrough_late_run` and `playthrough_late_run_silentium`: each grows a Bloom, and two Blooms 37 blocks apart refuse each other): one player from where `harbingerD0ToD5` ends through the bands' own chains, Bloodline Founding, the ledger, the recruited Vicar, the Founding Fane and the summon (`PlaythroughIlluminatusGameTests.chain`), the Rite of the Bloodline Covenant (`PlaythroughSanctifiedGameTests.chain`), the Living Covenant (`livingCovenantChain`: two Monolith Working crafts, the Throne craft fed with one, set down and bound, the Covenant Vigil with the summoned recruit assigned by `interactOn`, the Rite of the Chamber of Will; only `chamber_returned` is ARRANGE, logged as owed), the Rite of the Hematic Order with that recruit, the shatter, the Bloom, nine pomes and the Spine (`PlaythroughArchonGameTests.chain(helper, p, clock, recruit)`), then `apotheosChain` or `silentiumChain` and a Saint mausoleum ward crossed. At each boundary the band's `Checkpoint` is asserted, the pack and pool logged, the outpost Vicar, Alchemist and Mnemonist talked to (lang keys, no dead ends), and the floor cleared keeping the Fane heart and the pledged recruit (`clearForNextBand(helper, keepBlocks, keepEntities)`). Optional until stable. |

What is still only forged or not played at all: the Three Answers demonstration hit and fitting, the Pallid Vessel
rite and fill themselves (ARRANGE), the Rite of Hematic Succession itself (its successor is ARRANGE in `successionByPlay`;
`runSuccessionGameTestServer` plays the rite), the Apotheos caster at Ancestral Communion, the Crimson Vestment frame
consecration and Blood Lust upgrade and the Living Weapon graft (ARRANGE in the D5 band), station practice other than the
Scriptorium enchant, pome ripening (random; ARRANGE through the saved data), and every Unstained step.

### Proof owed: what the GameTest server cannot run

Each of these fails as `NOT RUN: <reason>` under `required = false` (or is labelled ARRANGE / DEGRADED in the log); the
proof is owed by a client or a seeded world:

- The guided Chamber's restore and logout branches, the Chamber of Will visit and return (`chamber_returned`, ARRANGE in
  every Living Covenant), the Vesper ordeal (`vesperOrdeal`): `hemomancy:chamber_of_will` is not loaded. Owed by
  `chamberReviewClient`, `vesperReviewClient`.
- The first projection's two minutes, hand lock and damage immunity (`firstProjectionInTheGardens`; here the return is
  forced on the next tick, DEGRADED), Gardens travel (`gardensTravel`), the heart-node respawn trap (`heartNodeRespawnTrap`,
  D-80 [D8-1]): `hemomancy:fungal_gardens` is not loaded. Owed by `heartNodeReviewClient` and a Fungal Gardens client.
- The Relay Chart's bearing to a Lighthouse Garden (`relayChartFindsAGarden`) and the Scarlet Waybill locating a camp or a
  pavilion (`waybillLocatesACamp`): seeded worldgen. Owed by `court_worldgen_validation`, `brinedSeatReviewClient` and a
  seeded-world waybill run.
- A grand rite's ORDEAL by a solo caster: `apotheosRiteByPlay`, the late run's Apotheos road and the Archon chain's Bloom
  pass with an ARRANGE rescue of dry anchors when the woken rings deadlock (logged `DEGRADED`, rescue count in the log;
  D-88 [CHAIN-1]).

## The harness

Everything lives in `src/gameTest/java/com/vincenthuto/hemomancy/gametest/playthrough/`.

| Class | Role |
| --- | --- |
| `PlaythroughPlayer` | A captured server player that never joins the level. `create`, `moveTo`/`approach`/`aim`; `talkTo`/`tryTalkTo` (a right-click, returning a `DialogueView`), `choose` (the option payload, only when offered), `forge`/`forgeOption`/`forgeClaim` (negative tests), `openTopic`, `trade`; `use`, `useOn`, `sneakUseOn`, `place`, `attack`; `hold`/`holdUntil`/`startHold`/`holdTick`/`releaseHold` for channelled items; `castSelected`/`select`; `click`/`quickMove`/`closeMenu`; `pickUpNear`; `sleepThroughNight`/`leaveBed`; `damage`/`die`/`respawn`/`endReturn`/`relog`/`offline`; `fillInventory`, `creative`/`survival`, `tickPlayer`/`tickOnce`; readers `degree`, `blood`, `count`, `has`, `knows`, `chat`/`mark`/`chatSince`, `sent`. |
| `Outcome` | What one action did, measured around it: lines said (with their keys), advancements earned and lost, inventory delta, drops near the player and NPC, degree and blood before and after, payloads sent, the dialogue event and whether its reward was delivered. Fluent checks: `said`, `didNotSay`, `gave`, `gaveNothing`, `dropped`, `earned`, `degreeIs`, `deliveredIs`, `unchanged`. |
| `DialogueView`, `Dialogue` | One served dialogue flattened: reachable nodes, offered and shown events, hub cards by box with their state and sigil, the Trade box; `missingLangKeys`/`assertLangKeys`, `deadEnds`/`assertNoDeadEnds`, `emptiedNodes`, `questState`, `attention`. A one-node statement tree (an idle or refusal line) is not a dead end: the screen's footer always offers Leave there. `Dialogue.attention` reads a speaker's sigil. |
| `PlaythroughStates` | ARRANGE only: the documented end state of each band (`freshD0`, `charmBearerD0`, `neophyteD1`, `votaryD2`, `initiateD3`, `adeptD4`, `illuminatusD5`, `blood`), each built on the one before through the real grant and record helpers. The band chains and the full run check these states against play. |
| `PlaythroughClock` | A test that spans server ticks: `then`, `step`, `idle`, `everyTick`, `until`, and `segment` (steps built when the clock reaches them, so one test can lay out and play several bands in turn). `rite(...)` plays a Cardinal Rite: plants the held Staff, feeds open anchors, an optional extra target (a sigil node), then the daemon, until the rite ends; a variant can take up a rite already running or stop partway (interruption tests). Pending crafts and infusions tick on the level's own tick, as in play. |
| `PlaythroughSites`, `PlaythroughNpcs` | ARRANGE sites on the `empty` template (32 x 12 x 24, floor at relative y 4): a Blood Temple, an outpost wing of residents, the Mason's Respite yard, a rite floor, and `clearForNextBand`. NPCs spawn as the outposts spawn them and are discarded at the end. |
| `PlaythroughCleanup` | Hooks that run however a test ends. Players, NPCs, blocks, rites, biome edits and pending blood crafts register here. |
| `PlaythroughRites`, `RitePlan` | Late harness (D5 on): a rite's ground readied by hand, lifted from the Adept band: `riteStand`, `structureCells`, `buildStructure`, `build`, `offerings`, `usedSockets`, `braziers`, `besideSocket`, `supportSigilNodes`, plus `seatMedium` (a real `useItemOn` on the Focus), `prepare` (pillars, braziers, medium), `workingSubject` (a Working craft's subject by real placement). `PlaythroughClock.rite(player, focus, RitePlan)` plays a rite with `maxTicks` (default 6000 / 9000 / 14000 by size), `extraAim`, `takeUp`, `pauseWhen`, `fightThreats`, `releaseOnStill`, `onPhase(phase, hook)`, `trialPuppet`, `weapon`, `traceFirst` (during ORDEAL the extra aim, a demanded sigil, comes before refilling anchors: a covenant_pressure wave drains the anchors until its sigil is traced). A plan that fights threats strikes first in a DEFEAT wave (the false omens' puppeteers, "defeat within 18s or the rite fails"). `PlaythroughRites.stabilizeWhenDeadlocked(p, extraAim[, rescues])`: in an ORDEAL deadlock (every hungry anchor also damaged) it traces a sleeping support sigil for its stability; with `rescues` a deadlock that outlasts it by 400 ticks is rescued by filling the dry anchors (ARRANGE, logged `DEGRADED`, decision CHAIN-1). Used by the Rite of Apotheos and the Bloom of the Qliphoth. `PlaythroughRites.withoutDrainingWaves(plan, p)` (ARRANGE): pins the wave deck, the first time the rite is seen in ORDEAL, to the recipe's pool less the draining waves (used by `bloomRiteAndRefusals` and the Ancestral Communion, so they do not depend on the random deck; it does not cure CHAIN-1). `PlaythroughClock.rite` also walks the player within reach of a target more than 5 blocks away (logged once per rite), releases during a still interval only once no anchor needs repair, approaches a target straight above the centre from the player's side, and its timeout names the hungry anchors, sigil progress and `PlaythroughRites.describeAim`. `seatMedium` seats an activation-tool medium (Formation, Staff) with a sneak-click. |
| `PlaythroughPlayer` (late) | `interactOn` (EntityInteract fires), `talkToBlock`/`chooseAtBlock` (the Monolith, entity id -1), `whisper`/`openWhisper`/`chooseWhisper`/`closeWhisper`/`pendingWhispers`, `ledgerAction`, `kick` (the kicked player must `joinLevel` to be told; the leader's success line needs a profile cache the GameTest server lacks, noted on the outcome), `ledger()` (a `LedgerView` of the Assignment Ledger with the Blood Beneath bar worked out as the screen does), `clickButton`, `crossbarCallOrRecall`/`radial`/`spindlePrepare`, `consume`, `breakBlock`/`leftClickBlock`, `joinLevel` (opt-in: level and server player lists, never ticked; `getEntitiesOfClass` still misses the player), `enterWorld` (opt-in beyond `joinLevel`: the player is added to the level's entity storage with `addNewPlayer`, so entity lookups, traces and `level.getEntity(id)` find them; kept through `respawn`, dropped by `relog`/`offline`), `moveTo(level, pos)`, `owed(claim)`. `attack(target[, point])` puts the main-hand item's attribute modifiers on for the swing (a captured player never ticks, so the equipment pass never adds them): a sword hits like a sword. `respawn` carries the respawn point over as `PlayerList.respawn` does. |
| `PlaythroughStates` (late) | `foundedLineD5`, `memberOf`, `covenantD5`, `recruited`, `sanctifiedD6`, `livingCovenantD6` (`chamber_returned` ARRANGE), `archonD7`, `communedD7` (Spine by a real delivery tick), `choicePendingD7`, `silentPendingD7`, `apotheosPendingD7`, `vesperBeatenD7`, `apotheosD8`, `silentiumD8`; `courtKnown`, `courtStanding`, `circusKnownShape`, `acclimated`, `pavilionRoute`; the `Checkpoint` enum with `missingAt`/`assertAt`. |
| `PlaythroughSites`, `PlaythroughNpcs` (late) | `installStructure`/`structureSite` (synthetic StructureStart, restored at the end; `CourtLifeGameTests.FakeSite` and the Initiate band delegate to it), `courtSite`, `template`/`farOrigin` and `pavilion`/`camp`/`abandonedStage`/`courtTemplate` (the mod's real templates placed beside the test, with their saved actors, and registered as their structure: the launcher enables only the `playthrough_validation` template namespace, so a `templateNamespace = "hemomancy"` test is silently skipped; a template's actors are invisible to `getEntitiesOfClass` until its chunks are entity-ticking, so wait on `TemplateSite.ready(level)` before looking them up), `clearForNextBand(helper, keepBlocks, keepEntities)` (the late run keeps the Fane heart and the pledged recruit), `fane`, `faneFloor`, `monolith`, `covenantThrone` (the placer credited with crafting it unless `credited` is false), `qliphothBloom`, `ironBrazier`, `morphicNectarPool`, `gardensBiome`/`biome`, `mausoleum`, `stationBehindFocus`; `court`, `circus` (`CircusRole`), `brinedVotary`, `ownedMycophant`, `outpostKey(helper, n)`/`stampOutpost`, `kill`. |
| `PlaythroughNotRun`, `PlaythroughTime` | `require`/`notRun` fail as `NOT RUN: <reason>` and log the proof owed (`required = false` only); `degraded` labels a degraded run. `skipGameTime`, `setDayTime`, `difficulty`, each put back at the end (own batches). |
| `PlaythroughCleanup` (late) | Every player's level-wide leftovers are cleared at the end (`worldOwnedBy`): lines led or joined, Fanes with hearts and stakes, a running rite and rite recovery, an initiation ceremony, owned Blooms, held pavilion sites. |
| `PlaythroughLog`, `LangKeys`, `PayloadContexts`, `DialogueEventProbe` | The `[playthrough]` log lines; shipped-key lookup; a payload context for a captured player; the posted dialogue events. |

## Adding a scenario

1. Pick the band class (or `PlaythroughNpcUsabilityGameTests` for a sweep) and a batch. Tests in one batch run side by
   side on separate templates; a test that changes server-wide state (time of day, a server config) gets its own batch.
   `playthrough_validation` is already catalogued in `gradle/game-tests.gradle`; a new namespace would need its own row
   (`verifyGameTestSuites` checks).
2. Create the player with `PlaythroughPlayer.create` and set the starting state with `PlaythroughStates` plus labelled
   `// ARRANGE` lines. Lay sites with `PlaythroughSites`/`PlaythroughNpcs`, never by hand-spawning entities.
3. Play the steps under test through `talkTo`/`choose`, `useOn`, `hold`, `click`, `rite` and the rest. Never call a
   service or recorder for the step you are testing.
4. Assert on what the player sees: `Outcome` checks, `DialogueView.assertLangKeys` and `assertNoDeadEnds`, inventory and
   degree. Log what a reader of the run needs with `PlaythroughLog.note`.
5. A test that spans ticks uses `PlaythroughClock` and calls `run()` from the test method; one that does not ends with
   `helper.succeed()`.
6. When the right behaviour is a design call, write a decision probe: `required = false`, a `// DECISION: D-n` line, an
   assertion of what a player would expect, a row in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) (the question lives only
   there) and the probe's name under [Proof owed and known gaps](#proof-owed-and-known-gaps) here. When the right
   behaviour is documented and the mod is wrong, fix the mod and leave a `// FIX:` line on the test.
7. Player-facing text in a fix is an `en_us.json` key in the speaker's voice (`docs/NPC_VOICES.md`); a system line is a
   `message.hemomancy.*` key.

## Fixes the suite drove

Mod changes made because a playthrough test showed a documented behaviour broken (all 2026-11-01, committed in `8d01a316c`, unless dated):

- PT-32 (D-32, ruled 2026-11-13, built 2026-11-14): a player below the Degree of the rite their floor answers to hears `message.hemomancy.cardinal_rite_activation.this_rite_requires` with that Degree. `bloodlineFoundingOpensAtD5` and `foundingFaneOpensAtD5` assert it at D4, and the D7 Archon at the Rite of Visitation hears it (`PlaythroughEndgameGameTests`). A player past a rank rite's Degree hears `message.hemomancy.cardinal_rite_activation.you_have_already_attained` at its floor (B25, 2026-11-15); `sanctifiedSealedBeforeCovenant` and `sanctifiedRiteByPlay` assert it at D6.
- PT-34 (D-34, F-11, ruled 2026-11-13, built 2026-11-14): pending blood crafts count down once per server tick on their own level.
- PT-38 (D-38 [D5-4], ruled 2026-11-13, built 2026-11-14): the first Hematic Unbinding warns at activation and takes no Nether Star; a second activation inside the window runs the rite. `hematicUnbindingWarnsAtActivation` (was `hematicUnbindingCostsTwoStars`) is required. `hematicUnbindingOfflineShare` now opens the window through `HematicUnbindingRules.activate` and stays optional (PT-44).
- PT-42 (D-42 [D5-8], ruled 2026-11-13, built 2026-11-14): no "pool is full" line; the pool screen greys its Donate button when the pool is full. A lump donation takes only the pool's room and nothing from a full pool, so blood is no longer lost into a full pool. `fullPoolTakesNothing` (was `fullPoolDepositSaysNothing`) is required; the zero-blood half of the old probe was left as built.
- PT-45 (D-45 [BC-1], ruled 2026-11-13, built 2026-11-14): Court standing no longer depends on the order of acts. The first visit-kind act (the site visit or the welcome gift) after the house's custom made the guest makes a friend, so a custom and a visit make a friend either way; two visit-kind acts alone stop at guest. `customThenVisitMakesAFriend` (was `customBeforeVisitLocksGuest`) is required.
- PT-46 (D-46 [BC-2], ruled 2026-11-13, built 2026-11-14): leaving a usurpation costs the same one-day wait as dying. A logout ends the fight at once (`KindledChampionTrial.onLogout`); a challenger who swims out of range or changes dimension is found online after the 100-tick grace. Both say `hemomancy.court.champion.forfeit` where the player can hear it. `escapingAUsurpationCostsTheDay` (was `usurpationEscapedByLogout`) is required. Since 2026-11-15 (B26) `KindledChampionTrial.facing` searches every loaded level at any distance; `aFarChallengerWhoLogsOutStillForfeits` (required) logs out 70 blocks off inside the grace and asserts the wait and the forfeit line.
- PT-47 and PT-48 (D-47 [BC-3], D-48 [BC-4], ruled 2026-11-13, built 2026-11-14): the hub's confirm step now carries a warning. A switch between houses shows `hemomancy.dialogue.court.confirm.switch`; the challenge shows the Head-tender's `challenge.line2`, which now names leaving as well as falling. `challengeWarnsBeforeTheChampionSteps` (was `challengeStartsWithoutConfirm`) is required and reads the served hub, since the old probe sent the event directly and could not see a confirm. `joinSwitchAndThirdChange` asserts the switch warning and the plain confirm on a first entry.
- PT-52 (D-52 [BC-8], ruled 2026-11-13, built 2026-11-14): the Kindled Champion challenge needs the fifth Degree (`CourtChampionRules.MINIMUM_DEGREE`, checked after the title and before the wait). `championNeedsTheFifthDegree` (was `championHasNoDegreeGate`) is required.
- PT-53 (D-53 [BC-9], ruled 2026-11-13, built 2026-11-14): with Pelagic worldgen off the Vicar, Voyager and Alchemist add no Court topic and no Votary drops a Brine-Sealed Letter. `courtPointersPastTheGarden` asserts the flag is on in the test world (the probe is required since 2026-11-15); the off case is in `CourtDiscoveryDialogueTest`.
- PT-55 (D-55 [BC-12], ruled 2026-11-13, built 2026-11-14): Court members can't be leashed. `aHeldLeadDoesNotLeashACourtMember` (was `heldLeadLeashesACourtMember`) is required, and `innerHouseSweep` no longer empties the hand before each talk.
- PT-56 (D-56 [NPC-2], ruled 2026-11-13, built 2026-11-14): the custom given in dialogue pays the rapport of the speaker's custom card, every time, through `NpcTradeService.creditCustom`. `customPaysTheSameRapportByWordAndByCard` (was `tradeCardPaysRapportDialogueDoesNot`) is required.
- PT-72 (D-72 [D7-1], ruled 2026-11-13, built 2026-11-14): the ninth-pome power lock stays as built and is documented (reference §5.9 Stage 4). Eating the eighth counted pome says `message.hemomancy.qliphoth_pome_item.the_ninth_husk_will_lock`, and the owner's pomes show it in their tooltip until the ninth, reading the same total over all Blooms as the lock. `powerLockAfterTheNinthPome` is required and now asserts the lock holds for a Silence, a split-pome player and a pending choice, and that the eighth pome over two Blooms warned (it asserted that they could cast).
- PT-73 (D-73 [D7-2], ruled 2026-11-13, built 2026-11-14): the Monolith is bound to its builder. `strangerShattersYourMonolith` is required and now also checks that the stranger is offered no Press further and that a forged press leaves the stone whole.
- PT-75 (D-75 [D7-4], ruled 2026-11-13, built 2026-11-14): a logout pauses the first projection. `logoutPausesProjection` (was `logoutCompletesProjection`) is required: no revelation, the projection still active with its time left, and the login resends it. With no Gardens loaded the 500 mL is still spent.
- PT-78 and PT-79 (D-78 [D7-8], D-79 [D7-9], ruled 2026-11-13, built 2026-11-14): the Alchemist names the Fungal Spine after Communion, and the Vicar names the Severance to a D7 Silence. `springPointerAfterCommunion` and `severancePointerForSilence` are required; `endingLinesPerPath` counts 25 lines with the Alchemist's `communed`.
- PT-82 (D-82 [D8-3], ruled 2026-11-13, built 2026-11-14): the Eighth Degree choice opens the Truth entry. `apotheosRiteInTheLiberAtTheChoice` (was `apotheosRiteNotInTheLiber`) is required and chooses through the real whisper.
- PT-89 (D-89 [CHAIN-2], ruled 2026-11-13, built 2026-11-14): decision whispers open first. `revelationWhisperOpensFirst` is required, and `nextStepPointerSweep` now flags a choice that opens anywhere but first.
- PT-67 (D-67 [D6-5], ruled 2026-11-13, built 2026-11-14): the Rite of the Chamber of Will refuses at the Focus when no visit can start, so it attunes nothing here. `chamberRiteAttunesWithoutVisit` is required and passes; `chamberRiteSaysWhyNoVisit` asserts the refusal, the kept Staff and the brazier's offering; `livingCovenantChain` asserts the refusal and then stands in for the rite (ARRANGE).
- PT-68 (D-68 [D6-6], ruled 2026-11-13, built 2026-11-14): a plain click seats a Formation where the floor wants one. `formationSeatsOnPlainClick` is required and passes; `formationDoesNotSeatWhereNoRiteWantsIt` is its Threshold control.
- PT-81 (D-81 [D8-2], ruled 2026-11-13, built 2026-11-14): a Mycophant win is recorded by owner UUID at the killing blow and settled at the next login; a death in the yield settles it at once. `PlaythroughEndgameGameTests.mycophantYieldLogout` is required now and also checks one Tendril and the Nursery left.
- PT-6 (D-6, ruled 2026-11-13, built 2026-11-14): any Blood Shot weave opens the Brotherhood; the Mnemonist's index is optional Side work, and the ledger card and the Mnemonist's archive line say so. `weaveWithoutMnemonistUnlocksD4` asserts that now and is required.
- PT-7 (D-7, ruled 2026-11-13, built 2026-11-14): the Vicar's D3 hint names the Blood Shot weave at the Loom and his D4 hint the first scar committed through a Mason's Effigy. `vicarD3HintNamesTheRealGate` and `degreeHintMatchesTheChapterGate` are required.
- PT-12 (D-12, ruled 2026-11-13, built 2026-11-14): the outpost alchemist template sets a lit campfire in the floor under its Alembic. `outpostAlembicDistillsOverItsTemplateHeat` (was `outpostAlembicHasNoHeat`) reads the block under the template's Alembic and distills over it; required.
- PT-17 (D-17, ruled 2026-11-13, built 2026-11-14): a structure craft that closes while its crafter is offline records the proof by UUID in `OfflineCredits`, applied at the next login. New required `structureProofWaitsForTheNextLogin`; the Venous Stone half already waits for its infuser.
- PT-20 (D-20, ruled 2026-11-13, built 2026-11-14): an offline weaver's Loom climax credit (Loom use, skill point, first-weave milestone) is applied at the next login through `OfflineCredits`. `weaverOfflineDuringClimax` is required.
- PT-24 (D-24, ruled 2026-11-13, built 2026-11-14): a guided Chamber visit completes only when it runs out or ends at the paired chair; a logout, death protection, the gate or an admin return does not. `guidedChamberLogoutAndLethalDamage` still needs the Chamber-loaded client.
- PT-33 (D-33, ruled 2026-11-13, built 2026-11-14): the Specimen Jar lesson is offered on the Alchemist's greeting at D3 and above. New required `specimenJarLessonOfferedAtD3`; the unoffered-event sweep no longer carves the lesson out.
- PT-39 (D-39 [D5-5], ruled 2026-11-13, built 2026-11-14): the Vicar's Monolith rumour names the Rite of Exsanguination as the source of Sanguine Quintessence. `quintessenceSourceIsNamed` (was `quintessenceHasNoPointer`) is required.
- PT-43 (D-43 [D5-10], ruled 2026-11-13, built 2026-11-14): the Vicar offers the Monolith rumour and scrap at D5 and above, and the handler takes D5+. `monolithBlueprintFromD5` (was `monolithBlueprintOnlyAtD5`) is required.
- PT-71 (D-71 [NPC-4], ruled 2026-11-13, built 2026-11-14): the Mnemonist's Anchorite counsel is a Quest Work card with a notice sigil until the counsel is taken. `counselIsAQuestCard` (was `counselIsALoreCard`) is required.
- PT-16 (D-16, ruled 2026-11-13, built 2026-11-14): the initiation kit (Sanguine Conduit, the four starter flasks, the ledger) is owed through the `PendingDelivery` claim `OwedRewards.CLAIM` before `initiation_blood_claimed` is recorded, so a Creative discard or a full pack leaves it owed, never dropped. `creativeFullInventoryStarterKit` is required and passes; `partialStackStarterFlasksAreNotLost` now asserts held plus owed, nothing dropped.
- PT-4 (D-4, ruled 2026-11-13, built 2026-11-14): a directed D4 player who carries no Mason's Respite map and is owed none is offered the directive again and gets exactly one more. `lostMapIsReissued` (was `lostMapIsNotReissued`) is required; `deathReclaimSweep` expects that one reissue after a death.
- PT-14 (D-14, ruled 2026-11-13, built 2026-11-14): the scar of each Vein-Mason lesson (first, continuation, D5, D6) is stored when it is given, and the supervised carve, the replacement offer and the replacement read it. `temperDriftKeepsTheIssuedPattern` (was `temperDriftDuplicatesPatterns`) is required.
- PT-22 (D-22, ruled 2026-11-13, built 2026-11-14): a section left Varicose by a Thelemic cast counts once as the Mason's strain, even one already Varicose at promotion. `preStrainedSectionCountsOnce` (was `preStrainedSectionNeverCounts`) is required. Only the section the cast strains counts today, and only if it ends Varicose, not Dead. Counting any Varicose section is remaining work.
- PT-28 (D-28, ruled 2026-11-13, built 2026-11-14): a reward the speaker refuses says why: the Vein-Mason's D5 and D6 rewards and pattern replacements, the Vicar's structure blueprints, the Monolith scrap, the stained church map (Vicar and Zealot) and the Mnemonist's circus waybill. `refusedRewardSaysWhy` (was `refusedRewardNodeContradictsServer`) is required.
- PT-86 (D-86 [NPC-3], ruled 2026-11-13, built 2026-11-14): each open D5 and D6 Living Covenant step is its own Vicar Quest Work card with a notice sigil. `mainStepsAreQuests` is required; the `complete_vigil` helper wording (the line names only a recruited Vicar) is remaining work and its check in the test is a log line.
- PT-3 (D-3, ruled 2026-11-13, built in part 2026-11-14): the seven one-time NPC rewards that dropped at the NPC's feet (the Vicar's kit, the Hermit Road report, the Mnemonist starter, the Worn Vow, the Woven Vessel kit, the Mason's map, the Vein-Mason first lesson) are owed through the `PendingDelivery` claim `OwedRewards.CLAIM`. `rewardLandingSurvey`, `secondPlayerTakesTheKit` and `twoPlayersWovenVesselKit` are required. The late rewards (`rewardLandingSurveyLate`) and the shared station outputs (`twoPlayersOneCentrifuge`, `sharedEffigyChargeCredit`) are not built.
- PT-88 (D-88 [CHAIN-1], ruled 2026-11-13, built 2026-11-14): Projection fills a dry anchor before it repairs it. The solo stall was the harness: it aimed at a full, damaged anchor before a dry one. It now aims at dry anchors first (B25, 2026-11-15). `apotheosRiteWithoutRescue` is required, and `apotheosRiteByPlay`, the Bloom rite and the late run have no rescue.
- PT-21 (D-21, D-32, ruled 2026-11-13, built 2026-11-14): at the third Degree the Mnemonist's Circus card opens a teaser (`hemomancy.mnemonist.circus.teaser.line1`, `line2`) that names the open country the camps keep to and says the Waybill waits for the fourth Degree. `d3PuppetryHasAPointer` (was `d3PuppetryHasNoPointer`) is required; `mnemonistWovenTreeAtD3` now expects the Circus card at D3 and no Waybill.
- PT-58 (D-58 [CT-2], D-32, ruled 2026-11-13, built 2026-11-14): the Scarlet Waybill seeks an abandoned stage (structure tag `troupe_abandoned_stage_targets`) while the Retired Understudy's testimony is given and the stage record unread, unless the player sneaks; its search line names what it seeks. `abandonedStageHasAPointer` (was `abandonedStageHasNoPointer`) and `waybillSeeksTheStageWhileTheLeadIsOpen` are required.
- PT-59 (D-59 [CT-3], ruled 2026-11-13, built 2026-11-14): recheck first: the 2026-11-14 baseline run logged "2 threats left after 5 Peaceful ticks", so the Husks did survive that long. A practical or guest act is now refused in Peaceful with `message.hemomancy.circus.school.peaceful` and nothing spawns; one running when Peaceful is set ends with the retry line. `practicalsInPeaceful` is rewritten to that and required.
- PT-60 (D-60 [CT-5], ruled 2026-11-13, built 2026-11-14): the Threadkeeper has a Quest Work option, `hemomancy.circus.school.ask_empty_ring`, to the An Empty Ring briefing. `threadkeeperLessonReachable` (was `threadkeeperLessonUnreachable`) is required.
- PT-61 (D-61 [CT-6], ruled 2026-11-13, built 2026-11-14): `finale.claimed` is split. A site another player holds says `hemomancy.circus.finale.busy`, a settled site says `hemomancy.circus.finale.settled` (`CircusPavilionStateRules.refusalKey`), and a finale asked for on no route says `hemomancy.circus.finale.no_route`; `finale.not_ready` is for Succession only. `refusedClaimsSayWhy` is required.
- PT-62 (D-62 [CT-7], D-36, ruled 2026-11-13, built 2026-11-14): a destroyed Ringmaster (death or discard, not an unload) leaves his site vacant (a saved flag), and the Waybill passes a vacant site by as it does a finished one. The release and its line stay; dead performers still do not return. `ringmasterLostLeavesTheWaybill` (was `ringmasterLostLeavesSiteOpen`) is required.
- PT-63 (D-63 [CT-8], ruled 2026-11-13, built 2026-11-14): a practical or guest act ends with `hemomancy.circus.school.retry` as soon as fewer rehearsal threats live than the exercise needs (two for the Scarlet Mummer and the Cinder Bellows, one for the rest). `practicalTargetKilledEndsIt` (was `practicalTargetKilledStalls`) is required.

- The Monolith (2026-11-05): the stone opens the Vicar's four boxes with covenant counsel, so a held item no longer hides its tree (D-40 [D5-6]) and its D7 counsel names pressing further and the Qliphoth Seed (D-76 [D7-5]). `monolithAtD5` and `theStoneNamesTheShatter` (was `noPointerToTheShatter`) are required now, and `aBreakingStoneGivesNoCornerstone` is new: 318 required, 109 optional. [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md).

- Onboarding (2026-11-11, P2): the newcomer card's met-Vicar flag (DL-6 moved the gate from the Hermit to the Vicar) was
  never set in play; `HarbingerVicarEntity.mobInteract` now records it on the right-click. The Vicar's own "Where should
  I begin?" card stays only while the player's blood is asleep, so a charm-bearer is not sent back to a Hermit.
  `newcomerGuidanceOnlyBeforeTheVicar`, `vicarOffersInitiationToCharmBearer`, `deathAfterClaimKeepsInitiationOffer`.
- Dialogue (2026-11-11, P2): reading a lore card threw on the server when the hub also held a stack-aware item inquiry
  (`...scar_pattern#<hash>` is not a valid resource path). `OfferedDialogues.offersTopic` now matches lore cards only and
  builds read ids with `tryBuild`. `successionByPlay`; JUnit `OfferedDialoguesTest`.
- Rites (2026-11-11, P2): a caster's plain Projection that missed an anchor or the daemon could infuse a block of their
  own rite floor (stone bricks into Polished Venous Stone Bricks, lifting the block), which failed the rite as
  "disturbed". A caster's own running rite now claims their Projection over its ground: no infusion and no stain there
  (`CardinalRiteFootprintResolver.onOwnRiteGround`, `BloodProjectionItem`; the method is `onRiteGround` since
  2026-11-14, when CN-2 widened the claim to every player's plain Projection). `d5MasonChainWithVascularMending`;
  `rite_kind_validation` `projectionDoesNotInfuseTheCastersOwnRiteGround`.
- Saints (2026-11-11, P2): a /kill'd Saint could not return to its tomb when its body left a blood stain in the tomb cell
  (the ground network's 35% kill stain). The tomb now closes over any replaceable cell
  (`SaintSarcophagusBlock.blockingCell`). `saintTombWake`; `SaintEncounterGameTests.aBloodStainInTheTombCellDoesNotKeepTheSaintOut`.
- Onboarding: the met-Hermit flag set by the right-click and kept under `PlayerPersisted` (replaced by the met-Vicar flag above); the Vicar's begin and release
  options honoured only from a Vicar (a forged begin could start a ceremony into a D5 founder's line); a busy Vicar says
  so (`hemomancy.initiation.officiant_busy`).
- D1: an empty hand collects from a Sanguine Condenser again; a renamed ledger is no longer re-minted; the Hermit Road
  lines no longer promise a ledger; the ledger advancement's parent fixed.
- D2: Concentrated Blood offered only with the charm attached, refused vials say why, the Alchemist's card is a notice
  only while a vial can be issued and an extra ask says the player already carries one; `first_separation_started` only
  for the assigned spin (Degree 2 and briefed); the condenser says when the pack has no room.
- D3: offhand blanks count for the Woven Vessel; the Artificer's fork and lineage names are readable; the Warp Chair says
  why it refuses.
- D4/D5: the Rite of Vascular Mending counts as the Mason's treatment; the diagnosis reads in English; the Anchorite's
  student trees carry item inquiries; unlearnt-scar refusals and pattern tooltips name the scar.
- Dialogue and trade: dead and spectating players are refused at dispatch; progression events need their own speaker
  (`DialogueEventSpeakers`) and forged unoffered events are gated as their trees are; a dissolving Hermit takes no
  trades; Main steps are quests with a notice sigil; a card is Turn In only when something can be handed in.
- Player data: Artificer and Mnemonist claim keys under `PlayerPersisted` with migrations 10 and 11.
- Blood: the conjuration dispel refund goes through `BloodFlowLedger` (recorded, synced at the end of the same tick by
  the `PlayerStateSync` flush; the test runs that flush for its captured player).

Late fixes driven by the `npc_late` band (2026-11-01, committed in `8d01a316c`; LF ids from the late design spec):

- LF-1: `recruit_harbinger` and `expel_harbinger` need a Vicar, Alchemist or Mnemonist (`DialogueEventSpeakers` rows
  and the same check in `RecruitmentDialogueEvents`), and recruiting needs Degree 5. A forged recruit at an Artificer,
  a Court member or a performer no longer adds an NPC member.
- LF-3: the Vicar's D5 hint and the ledger's line step count any registered line, not only a led one, and the written
  covenant comes first: a member is sent to the Fane (`found_fane`) or the rank rite, never told to found a line.
- LF-15: `SuccessionDialogue.decorate` gives the Succession explain, bequest and donate options hub cards, and drops a
  successor's trimmed recruit and release cards with their emptied nodes. A successor that will not serve you now
  offers Leave on its idle line.
- LF-11 (one case): a Threadkeeper claim with nothing set aside says so
  (`message.hemomancy.circus.school.nothing_to_claim`); the rest of LF-11 is the Circus band's.
- Forged events gated as their trees are: a purifying player gets no Monolith scrap, Circus waybill or Relay Chart from a
  forged option (`HemoCapabilityAccess.hasBegunPurification`, which creates no attachment).
- The purifying Mnemonist tree carries the `slots` node its crude-memories option leads to.

Late fixes driven by the `d5_illuminatus` band (`PlaythroughIlluminatusGameTests`, 2026-11-01, committed in `8d01a316c`):

- LF-2: one line per player. Bloodline Founding is refused at activation for a leader or member of a registered line
  (`message.hemomancy.cardinal_rite_completion_effects.you_already_belong_to_a_bloodline`, a `CardinalRiteCompletionEffects`
  refusal), and a signed ledger refuses a player already in another line
  (`message.hemomancy.unsigned_ledger_item.you_already_belong_to_a_bloodline`). Before, both made a double membership.
- LF-4: a station kit claimed with a full pack stays pending through death and arrives once after the respawn (it used
  to go into the corpse's pack and was lost). Kits are the `PendingDelivery` claim `StationUpgradeProgress.KITS`
  (F-19, 2026-11-14), which holds the claim for a dead player and also covers the Creative discard. The old
  `StationUpgradeProgress.deliver` is gone.
- LF-14: `Bloodline.removeMember` clamps the pool to the new maximum, as a recruit's death does (a kick left 12000 in a
  5000 pool).

Late fixes driven by the other late bands (2026-11-01 and 11-02, committed in `8d01a316c`):

- LF-5 (`d7_archon`): the revelation choice survives death. `FungalGardenTravelHelper.isRevelationChoicePending` (the root
  flag, or witnessed revelation with path NONE, Communion done and nine pomes) is what the Spine, the choice options, the
  login re-send and the whisper inbox read, so after a death the Spine serves the core-witness choice again.
- LF-6 (`d8_endgame`): at the first Mycophant victory the owner is told to make room
  (`message.hemomancy.mycophant.make_room`) while the Tendril is still owed after the first delivery attempt.
- LF-7..LF-11 (`crimson_troupe`): the Threadkeeper's claim says when the payment went out in full; its claim, report and
  inspect cards are Turn In / Urgent / Notice only when something can be done (`CircusSchoolDialogue.hub`); a spectator act
  owner fails the act; faculty stay out of acts 2-4; the school's silent refusals speak (busy, below the degree, no
  Crossbar, inspect below D3, a lesson taught, a route chosen or repaired).
- LF-12 (`d6_sanctified`): the Rite of the Chamber of Will says when no visit can begin
  (`message.hemomancy.chamber_visit.rite_unavailable`). Since PT-67 (2026-11-14) it says so at the Focus and
  does not start or attune.
- LF-13 (`brined_court`): the Champion's challenge names an outstanding retry wait before a missing strain or standing.
- LF-17, Circus half (`crimson_troupe`, docs only, 2026-11-07): CIRCUS.md and HEMOMANCY_REFERENCE §17.1 now list every
  acclimation source, including +50 for the first talk with each of the eight performer roles, and say spectacle banking
  is per player (four on Succession, one on Liberation). `firstTalkMilestones` logs it.
- D6 softlock: a Sanguine Formation medium could never be seated (a sneak-click skipped the Focus). It seats on a
  sneak-click (`SanguineFormationItem.doesSneakBypassUse`), and from this round whatever the off hand holds
  (`SanguineFormationItem.useOn` hands a sneak-click on a Focus to the Focus). Since PT-68 (2026-11-14) a
  plain click seats it too where a rite on the floor takes it as its medium.

Fixes from the final review (2026-11-02, committed in `8d01a316c`):

- `CardinalRiteSavedData.deliverRecovery` skips a dead player (rite recovery used to go into the corpse's pack and was
  lost) and counts only stacks that land (a Creative full pack used to count as delivered). Tests:
  `riteRecoveryHeldWhileDead`, `foundingFaneCasterDiesMidRite`, `foundingFaneCasterAwayMidRite`.
- The Artificer's lineage recovery offers the Silent Archon lineage to a D8 Silentium only (`ArtificerProgressSnapshot.silentium`),
  as HEMOMANCY_REFERENCE §22.8 rules; it was offered to a D7 Archon and recorded nothing (was probe D7-7;
  `silentArchonLineageAtD7` is now required).
- The successor's idle tree no longer carries an extra Leave option (the screen's footer already offers one); the suite's
  dead-end rule now knows a one-node statement tree is closed by the footer.
- HEMOMANCY_REFERENCE §12: the LF-14 pool clamp also applies to a member who leaves through the Unstained path switch.
- `dialogue_route_validation`: the `circus_school_teach` fingerprint regenerated for the LF-11 `lesson_taught` line (the only
  difference; `runDialogueRouteGameTestServer` green).
- Harness: `attack` applies the held weapon's modifiers (three per-test copies removed; the Brined Court's Champion keeps a
  deliberate overwhelming blow), `enterWorld` (Marimo's sight and Morphic Nectar now run instead of `NOT RUN`, and the LF-1
  forged pledge at another player is asserted), `TemplateSite.ready`.

## Rulings that bind

The owner answered every playthrough probe on 2026-11-13. A ruling that is built is listed with its test under
[Fixes the suite drove](#fixes-the-suite-drove). The rulings below are the ones not built yet, or built in part, and
the two that keep the mod as it is. The plans are in `docs/owner_answers/` (the chunk number is in brackets).

D0 to D6 (chunk 10):

- PT-1 (D-1, 2026-11-13): the living absorption rate and the 500 mL proof stay. The Vicar's hint points at reservoirs. Not built yet.
- PT-1b (D-1b, 2026-11-13): blood the player projected does not count toward the D1 absorption proof. Not built yet.
- PT-2 (D-2, 2026-11-13): net counting stays, so absorption at a full vessel counts nothing. The hint that says everything counts is reworded. Not built yet.
- PT-3 (D-3, 2026-11-13): one-time NPC rewards and shared station outputs become `PendingDelivery` claims. Built for the seven one-time rewards and the starter kit. The late rewards and the station outputs are not built yet.
- PT-5 (D-5, 2026-11-13): the Sanguine Monolith Working craft opens at Degree 5. Not built yet (built on 2026-11-14, then put back: the node collides with another on the Liber map).
- PT-8 and PT-8b (D-8, D-8b, 2026-11-13): the Mortal Display is protected and relinks to its Hermit. An awakened player at an unclaimed Display is told the heart cannot attach to awakened blood. Not built yet.
- PT-9 (D-9, 2026-11-13): founder-led reception asks the recruit first. Not built yet.
- PT-11 (D-11, 2026-11-13): Concentrated Blood settles after a minimum time in bed, without the server's night skip. Not built yet.
- PT-13 (D-13, 2026-11-13): kept as built. Outpost stations keep needing a personal craft. `outpostOnlyStationsLeaveLedgerIncomplete` passes.
- PT-15 (D-15, 2026-11-13): the Hermit Road report can be made at any Degree. Not built yet.
- PT-18 (D-18, 2026-11-13): a second assigned Centrifuge spin is refused until the first output is recovered. Not built yet.
- PT-19 (D-19, 2026-11-13): kept as built. A reissue checks only the carried inventory, as a lost-item safety net. The two probes still assert the opposite and need rewriting.
- PT-23 (D-23, 2026-11-13): an outpost resident whose marker is obstructed spawns at a fallback spot. Not built yet.
- PT-27 (D-27, 2026-11-13): the answer was one Monolith scrap per player. It clashes with PT-19, which keeps reissues as a safety net, so whether a lost scrap is given again is asked again (NQ-10-7). No reissue rule is decided. Not built yet.
- PT-29 (D-29, 2026-11-13): with vascular degradation off, the Mason's D5 strain has a fallback route. Not built yet.
- PT-30 (D-30, 2026-11-13): the Anchorite trades a replacement scar blank. Not built yet.
- PT-41 (D-41 [D5-7], 2026-11-13): the Basin commission reads either hand, and the Alchemist's commissions pay a reward. Not built yet.
- PT-85 (D-85 [NPC-1], 2026-11-13): a summoned outpost NPC is sent home after use. Not built yet.

Bloodlines and the Covenant Throne (chunk 11):

All built 2026-11-15 (B22); each probe is required now and rewritten to its ruling:

- PT-35 (D-35 [D5-1], 2026-11-13, NQ-11-7, NQ-11-8): a signed ledger joins only a player of Degree 1 or higher with active blood, and only after the leader admits them on the ledger screen's petition row (at most 16 wait per line). `ledgerJoinHasNoGate` became `ledgerJoinNeedsTheGates`; `ledgerJoinBySecondPlayer` admits; new `petitionWaitsForTheLeaderAndTheApplicant`, `refusedPetitionJoinsNothing`, `forgedPetitionAnswerFromAMember`.
- PT-36 and PT-90 (D-36 [D5-2], D-90 [CHAIN-3], 2026-11-13, NQ-11-3 to NQ-11-6): members leave from the ledger screen or at the Vicar; the leader cannot. With PT-64 a member also binds the leader's Throne. `bloodlineRecallAndMemberPowers` plays the leave; `memberRouteToD7` binds the Throne.
- PT-37 (D-37 [D5-3], 2026-11-13, NQ-11-1 B, NQ-11-2): a removed player's ledgers of that line crumble when they are carried at the removal, moved in later, used, or found at login; only ledgers written before the removal (stamp `minted`, unstamped reads 0). Nothing is left. `kickedMemberStaleLedger`; `kickAndRelog` adds the login notice and crumble.
- PT-44 (D-44 [D5-11], 2026-11-13, NQ-11-9, NQ-11-10): an away member's Unbinding share is banked and paid at login (or at the respawn after a dead login), capped by room. `hematicUnbindingOfflineShare`.
- PT-64 (D-64 [D6-1], 2026-11-13, NQ-11-11 A): a member binds the leader's Throne for the bond and the respawn point; the Trance stays the Progenitor's. `memberCannotBindThrone` became `memberBindsTheLeadersThrone`.
- PT-65 (D-65 [D6-2], 2026-11-13): a Throne binds only inside the binder's line's Fane. `anyProgenitorBindsAnyThrone` became `anotherLinesProgenitorCannotBindThisThrone`.
- PT-66 (D-66 [D6-3], 2026-11-13, NQ-11-14): the Eternal Covenant seal lives on the degree attachment; the cure clears it. `eternalCovenantAcrossDeath`; new `cureClearsTheEternalCovenantSeal`.
- PT-69 (D-69 [D6-7], 2026-11-13): a lineless D6 is told to found a line first. `linelessD6VicarGuidance`.
- PT-70 (D-70 [D6-8], 2026-11-13, NQ-11-12): the Throne bond and its respawn point clear when the line ends. `disbandedProgenitorThrone`.
- PT-87 (D-87 [NPC-6], 2026-11-13): the default bloodline name is translatable. `bloodlineNameIsEnglish` became `bloodlineNameIsTranslatable`.

Rows held by other chunks:

- PT-49 (D-49 [BC-5], 2026-11-13, chunk 14): a Court resident points to the Shared Wake's nursery. Built 2026-11-15 (B26): the Gardener's, Lightkeeper's and Edge-swimmer's directions. `courtPointersPastTheGarden` is required and asserts a speaker on the way names the nursery.
- PT-50 (D-50 [BC-6], 2026-11-13, chunk 14): Court residents name the Vent Sanctum and the outposts. Built 2026-11-15 (B26); the same probe asserts the Vent Sanctum, and `CourtMemberDialogueTest.directionsNameEachHousesSite` checks every directions line.
- PT-51 (D-51 [BC-7], 2026-11-13, chunk 14): the Lightkeeper's feeding line is reworded to the real bonus. Built 2026-11-15 (B26). `lightkeeperLessonAndBonding` now asserts the lesson lines say "Wear" and never "feed" or "fed".
- PT-54 (D-54 [BC-10], 2026-11-13, chunk 14, NQ-14-6): Court speakers get lines past D5 and about the Primal step. Built 2026-11-15 (B26). `courtPointersPastTheGarden` asserts the Gardener at D8 with a Primal strain adds `degree.late` and `strain.primal`.
- PT-57 (D-57 [CT-1], D-29, 2026-11-13, chunk 15): the pavilion gets real galleries and a perch every player can reach. Built 2026-11-15 (B29, chunk 15 T8). `ringmasterReachFromTheFloor` is required: a player-sized walker paths from the main door up the gallery stairs to the Ringmaster, and a player where the path ends is in interact reach.
- PT-80 (D-80 [D8-1], 2026-11-13, chunk 12): an Apotheos stranded by the heart-node respawn returns to their bed, or to the Overworld spawn if that fails. The owner also kept the forced heart-node respawn, so the reading is asked again (NQ-12-4). Not built yet (chunk 12 T8).
- PT-83 (D-83 [D8-4], 2026-11-13, chunk 12): a D8 Silentium gets a greeting tree of their own. Built 2026-11-15 (B24); the probe is `silentiumHasAGreetingOfTheirOwn`, required.
- PT-84 (D-84 [D8-5], 2026-11-13, chunk 07): a lost Mycophant rematch re-arms once the cooldown has run, after every loss until a win (NQ-07-4). Built 2026-11-15 (B15). `mycophantRematchLine` is required: it loses a rematch, runs the cooldown down on the Gardens hunt tick and sees the claim begin as a rematch.

## Open questions

Open questions live in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md). No question text is kept here. The ones that belong to
this suite's D0 to D6 and bloodline probes: NQ-10-1 (PT-1, PT-1b), NQ-10-3 (PT-31, the Sporitic Thurible), NQ-10-5
(PT-85), NQ-10-7 (PT-27 with PT-19, the Monolith scrap), PT-11, PT-30, NQ-10-8 (PT-41). From the other chunks' probes: NQ-04-15 (PT-25, the dispel refund), NQ-12-1 (PT-74), NQ-12-2 (PT-77), NQ-12-4
(PT-80) and NQ-14-6 (the Primal half of PT-54).

Six probe rows had no clear answer on the sheet and are not recorded as rulings above. Four are asked again: PT-25
(NQ-04-15), PT-31 (NQ-10-3), PT-74 (NQ-12-1) and PT-77 (NQ-12-2). Two became planned defaults, listed under
"Calls made without the user": PT-10 (NQ-10-4) and PT-26 (NQ-10-2).

## Calls made without the user

Made by the overnight build (2026-11-14) or by this suite, and not yet seen by the owner:

- PT-12: the heat under the outpost Alembic is a lit campfire sunk in the floor, not the planned magma block. The outpost
  generator forbids Nether blocks on Order work.
- PT-7: the Vicar's new D4 hint says the scar is "committed through a Mason's Effigy". The Effigy prepares the pattern
  and the Iron Brazier commits it, so the line is to be reworded.
- PT-22: only the section the cast strains counts, and a section pushed to Dead does not.
- PT-28: refusal lines were added at the reward sites the plan named, plus the circus waybill. Not every silent return in
  the D0 to D6 route classes was audited. The lines are narration keys (`hemomancy.dialogue.event.*`).
- PT-43: at D7 and D8 the Vicar also asks an Archon or an Apotheos "Have you heard of any path beyond the seven degrees?".
- PT-4: the map is offered again only in the D4 Vicar tree.
- PT-6: the Woven Vessel ledger card still counts three steps, so a card finished without the index can read 2/3.
- PT-38: the Unbinding's warning window is session state. A server restart forgets a warning and the leader is warned
  again.
- PT-42: nothing is said or taken when Projection meets a full Bloodwell, and the zero-blood half of the old probe was
  left as built.
- The harness's own calls stand as described under [The harness](#the-harness): ARRANGE states, the rescue of dry
  anchors in a solo grand rite (DEGRADED), and optional full runs.

Planned defaults from the 2026-11-14 triage that touch a probe here. Each will be built as written unless the owner
objects; none is a ruling:

- PT-10 (NQ-10-4): unique Harbinger NPCs lost to `/kill` or the void come back after one day with no player within 48
  blocks, as Court members do. A Creative kill stays final.
- PT-26 (NQ-10-2): a player who wakes with Concentrated Blood pending and the charm off is told the blood cannot settle
  without the charm.
- PT-22: any section already Varicose counts once as the Mason's strain, at the next Thelemic cast or when the Mason's
  D5 tree opens. A Dead section still does not count.
- PT-5: the Monolith node gets an authored spot on the Liber map's deep D5 ring, then the craft opens at D5.
- PT-86: the Vicar's `complete_vigil` line names every helper the Vigil accepts.
- PT-84 (NQ-07-4): every lost rematch re-arms after the cooldown, until the player wins.

## Proof owed and known gaps

What the GameTest server cannot run is listed under
[Proof owed: what the GameTest server cannot run](#proof-owed-what-the-gametest-server-cannot-run). The rest is here.

### The 59 optional failures of the final run (2026-11-14)

Ruled and not built. Each probe becomes required when its task is built:

- Chunk 10 (2 left after B21, 2026-11-15): `lostBlankBeforeCarve` (PT-30, the ruled price clashes with the
  one-card-per-want trade rule) and `summonStripsTheOutpost` (PT-85, held). B21 built the rest and made them required:
  `alchemistBasinSampleInMainHand` (PT-41), `awakenedPlayerAtUnclaimedDisplay` (PT-8b), `brokenMortalDisplayLeavesNoOath`,
  `twoPlayersOneTemple` and `wrongHermitClaimsAndSilentFarewell` (PT-8), `condenserRoundTripCountsAsAbsorption`
  (PT-1b), `fullVesselAbsorptionCounts` (PT-2), `hermitRoadAfterD2IsDeadEnd` (PT-15), `d5MonolithCannotSpeak` (PT-5),
  `d5StrainWithDegradationOff` (PT-29), `founderProjectionReceivesRecruit` (PT-9), `monolithScrapIsRepeatable`
  (PT-27), `outpostSpawnsAllResidents` (PT-23), `rewardLandingSurveyLate`, `sharedEffigyChargeCredit` and
  `twoPlayersOneCentrifuge` (PT-3), `secondSpinOverwritesTheProof` (PT-18), `sleepNeedsTheWholeServer` (PT-11),
  `charmRemovedWhilePending` (PT-26).
- Chunk 11 (0 left after B22, 2026-11-15): all eleven are required now; see "Bloodlines and the Covenant Throne"
  above for their new names.
- Other chunks (2): `ringmasterReachFromTheFloor` (PT-57),
  `recollectionCystCreativeFullPack` (the late instance of PT-16: a Creative full pack must not lose the Recollection).
  B24 (2026-11-15) rewrote `silentiumGreetedAsArchon` to the PT-83 ruling as `silentiumHasAGreetingOfTheirOwn`, now
  required: each teacher greets a D8 Silentium with a tree that is neither the Archon's nor the Apotheos's.

No clear answer on the sheet (2): `uniqueNpcLossIsRecoverable` and `uniqueNpcLossLate` (PT-10, planned default
NQ-10-4). B23 (2026-11-15) built the other three answers: `shatterBeforeCornerstone` (NQ-12-1 A, 8 fragments) and
`atlasFragmentsAndResiduum` (NQ-12-2 A, with V-12-1) are rewritten to the rulings and required; `d4ThuribleHasATeacher`
is retired to `removed_assets/` because the Thurible is a D8 Apotheos relic now (NQ-10-3 A). New in B23:
`mindBrokenSurvivesADeath` (V-12-5), required.

Kept by ruling, probe not yet rewritten (1): `vesperMemoryStashedInAChest`, the late instance of PT-19.
`stashedConcentratedVialIsReissued` and `droppedConcentratedVialIsReissued` were rewritten in B21 to pin the kept
reissue and are required.

NOT RUN, the dimension or seeded world is missing (10): `chamberReturn`, `guidedChamberLogoutAndLethalDamage`,
`guidedChamberRestoresEverything`, `notRunNamesTheMissingChamber`, `vesperOrdeal` (the Chamber of Will),
`firstProjectionInTheGardens`, `gardensTravel`, `heartNodeRespawnTrap` (the Fungal Gardens; PT-80 is built and the
probe names its destination, proved outside the Gardens by `HeartNodeScreenGameTests.theWayHomeFindsTheBedElseTheWorldSpawn`),
`relayChartFindsAGarden`, `waybillLocatesACamp` (seeded worldgen).

Cause found (B21, 2026-11-15): `harbingerD5ToApotheos`, `harbingerD5ToSilentium` and `archonChainFromPreviousBand`
stopped at "the shatter drops the Seed" because the Archon chain still expected the shatter to drop the Seed. Since the
Monolith Walk (2026-11-04) the stone cracks open and the Seed waits inside for its owner's walk. The chain now breaks
the stone and walks to the Seed, as `shatterBeforeCornerstone` does. They stay optional until a few stable runs.
`courtLeadLostForGood` (D-10 late) still times out after 600 ticks: its void-lost leads open vacancies that do not
refill a skipped day later; the cause is not found (chunk 14).

### Other gaps

- PT-24 has no test that runs here. `guidedChamberLogoutAndLethalDamage` and the Chamber branch of
  `HarbingerRepairGameTests.guidedChamberStartAndReturnDoNotGrantAttunement` need the Chamber-loaded client
  (`-PchamberLoadedValidation=true`). `chamberReviewClient`, named above, is not a row in `gradle/review-clients.gradle`.
- PT-88: fixed in B25 (2026-11-15). `PlaythroughClock.anchorAim` aimed at the first anchor needing anything, by
  index, so a full, damaged anchor came before a dry, damaged one and the dry one's deficit kept re-damaging the ring.
  It now aims at dry anchors first. The deadlock wrapper and its ARRANGE rescue are gone (`removed_assets/`).
- Flake: `PlaythroughArchonGameTests.archonRiteWithHelper` failed once on 2026-11-14 (the rite stuck at INSCRIPTION
  after "could not place iron_brazier by hand; set it") and passed in every later run. Probable cause (B23): the Vicar
  was spawned before the braziers were built and could stand on a socket, so the brazier was set inside him. He is now
  spawned once the floor is built. Watch the next runs.
- PT-25: closed 2026-11-14 (B04). A dispel returns 90% of what the conjure paid (NQ-04-15), and
  `dispelRefundIsSyncedAndRecorded` now asserts that amount. The probe's comment uses the id PT-25; D-25 stays the
  Chamber gateway's id.
- PT-17: the probe's captured player is never removed from the level, so its pass does not prove that a real logout
  leaves the fed Venous Stone standing. Owed by a play check.
- PT-42: no test or review shot shows the greyed Donate button. `ScreenChromeReview` draws the pool screen with an empty
  pool.
- PT-12: the campfire under the outpost Alembic has not been seen in a client.
- The two full late runs are no longer green (see the four failures above). The first-round status line that says both
  late full runs pass is from 2026-11-02.
- The opt-in clinical suite (not this suite) has two required failures the build log names:
  `allVesselsFillCorkTransferEquipAndSerialize` and `earnedScarLessonBackfillsPracticePageWithoutNewRewards`.

## Smaller observations from the runs

Not decisions and not on the owner's sheet.

From the first round: the first Vein-Mason kit has no Runic Motif Paper, which the
Effigy needs (it is craftable, but no line says so); drinking a starter Bloody Flask shows the Blood Drunkenness line
("Foreign blood clouds the rite.") with no rite running; the Specimen Jar lesson says nothing in chat when its jars drop;
the Three Answers counsel sent to the Artificer instead of the Alchemist is swallowed with no line; a lit Brazier holding
an unlearnt pattern repeats its refusal every use tick; the Anchorite's entry in the reference says "he" where
`docs/NPC_VOICES.md` gives them "they"; `EarlyInitiation` cancels a ceremony at more than 4 blocks while the old band
spec said 2.

From the late bands: a Living Staff dropped on death is gone at once (`LivingItem` lifespan 0),
so a caster who dies mid-rite gets the Staff back into the pack they drop and must conjure a new one; the Mnemonist
practical's "strike a threat yourself" needs the Crossbar moved to the off hand (a left-click with it is the focus command)
and nothing says so; replanting the Staff on a spent `archon_rite` floor at D7 starts the Rite of Sanguine Attunement; a
gated Downfall Chapel names its gate to a D7 before the degree; an unnoted or unwritten D6 whisper memo is queued again
every 18000 ticks; the ledger summon leaves a recruit within 16 blocks where he is ("already nearby"); the Qliphoth Seed's
item entity appears a tick after the shatter; the empty template's barrier ceiling needs `skyAccess = true` for a Bloom; in `courtLeadLostForGood` (D-10 late) even the two
leads lost to void damage were not back after the skipped day (their `CourtVacancies` entries stay open past their due time
with no player near; whether the refill tick or the time-skip arrangement is at fault is unverified, so a seeded Seat
owes the proof), and vacancies from earlier test runs pile up in the GameTest world's saved data.

## Next steps

- Build the rulings listed under [Rulings that bind](#rulings-that-bind) and flip each probe to required with its task.
- The owner's answers to the open questions in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).
- Find why `courtLeadLostForGood` times out (its vacancies do not refill).
- Proof owed by clients and seeded worlds (see "Proof owed" above): the Chamber of Will, the Fungal Gardens, seeded Court
  Seats and Circus pavilions.
- Unstained playthroughs, once that path's design settles.
- Client-driven runs with `GameplayCampaignDriver` for what the GameTest server cannot see: screens, sigils as drawn, the
  ceremony visuals.
- Flip `harbingerD0ToD5`, `harbingerD5ToApotheos` and `harbingerD5ToSilentium` to required after a few stable runs.

## History

- 2026-11-01 and 2026-11-02: the suite was built in two rounds (D0 to D5, then D5 to D8), with 111 optional probes.
- 2026-11-05: the Monolith hub closed D-40 and D-76.
- 2026-11-11: core loop round two (P2) fixed the eight required failures. 427 tests, 98 optional failures.
- 2026-11-13: the owner answered every probe row on the sheet. The answers were sorted into the chunk plans in
  `docs/owner_answers/` and are recorded above.
- 2026-11-14: the overnight build (packages P02, P09, P10, P11, P12, P13, P14 and P15) built the rulings that waited on
  no open question and made their probes required. 432 tests, 59 optional failures.
  [owner_answers/BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md).
- 2026-11-14: the open-question table was removed from this document. Questions live in
  [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).
- 2026-11-14: second docs pass. The pointer gained NQ-10-7 (the Monolith scrap, promoted from a planned default to a
  question) and the other chunks' probe questions. The planned defaults that touch a probe are listed under "Calls made
  without the user". The LF-4, PT-46 and rite-ground lines were corrected against the code.
