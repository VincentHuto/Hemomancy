# Playthrough Tests

Status (2026-11-02): built, uncommitted. The final run of this round (`build/playthrough-final-run5.log`) had 426
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
and logs what actually happens, so a failing probe is a question for the user, not a bug to fix. Probes are listed under
[Open questions](#open-questions).

## How to run it

```
./gradlew.bat runPlaythroughGameTestServer --offline
```

It is on demand, not in `alphaCheck` (`gradle/game-tests.gradle`: `playthrough_validation`, `alpha: false`). A full run
takes under a minute of game time on the GameTest server. A green run ends with `All <n> required tests passed` and a
list of optional failures (the probes). Every test writes `[playthrough] <scenario>: ...` lines to the server log
(`build/playthrough-gametest/logs/latest.log`): what was offered, said, given, dropped and spent at each step. Read
those lines for the probes; they carry the evidence the questions below quote.

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
| Brined Court (`brined_court`) | `PlaythroughBrinedCourtGameTests` | The Court found by every route (the Voyager's referral and Relay Chart, a D4's Brine-sealed letter read at the Voyager, the Alchemist's lead with a worn strain, a site visit), guest standing, the Gardener's hospitality, customs by dialogue and by trade card, the Lightkeeper's lesson and bonding, joining and the free switch, the inner-house sweep and the 18-role sweep (lang keys, dead ends, routes), transgressions and the Warden, the Kindled Champion won and lost (the retry wait named first, LF-13), Hydra's and Marimo's presences (the player in the world, `enterWorld`), Morphic Nectar primalizing only for a D8 Apotheos, a Seat lead lost for good (D-10 late). Probes: BC-1..BC-10, BC-12, NPC-2, D-10 (late). |
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
   assertion of what a player would expect, and a row under [Open questions](#open-questions) here and in
   [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6. When the right behaviour is documented and the mod is wrong, fix the mod
   and leave a `// FIX:` line on the test.
7. Player-facing text in a fix is an `en_us.json` key in the speaker's voice (`docs/NPC_VOICES.md`); a system line is a
   `message.hemomancy.*` key.

## Fixes the suite drove

Mod changes made because a playthrough test showed a documented behaviour broken (all 2026-11-01, uncommitted, unless dated):

- The Monolith (2026-11-05): the stone opens the Vicar's four boxes with covenant counsel, so a held item no longer hides its tree (D-40 [D5-6]) and its D7 counsel names pressing further and the Qliphoth Seed (D-76 [D7-5]). `monolithAtD5` and `theStoneNamesTheShatter` (was `noPointerToTheShatter`) are required now, and `aBreakingStoneGivesNoCornerstone` is new: 318 required, 109 optional. [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md).

- Onboarding: the met-Hermit flag set by the right-click and kept under `PlayerPersisted`; the Vicar's begin and release
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
- Blood: the conjuration dispel refund goes through `BloodFlowLedger` (recorded, synced in the same tick).

Late fixes driven by the `npc_late` band (2026-11-01, uncommitted; LF ids from the late design spec):

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

Late fixes driven by the `d5_illuminatus` band (`PlaythroughIlluminatusGameTests`, 2026-11-01, uncommitted):

- LF-2: one line per player. Bloodline Founding is refused at activation for a leader or member of a registered line
  (`message.hemomancy.cardinal_rite_completion_effects.you_already_belong_to_a_bloodline`, a `CardinalRiteCompletionEffects`
  refusal), and a signed ledger refuses a player already in another line
  (`message.hemomancy.unsigned_ledger_item.you_already_belong_to_a_bloodline`). Before, both made a double membership.
- LF-4: `StationUpgradeProgress.deliver` does nothing for a dead player, so a kit claimed with a full pack stays pending
  through death and arrives once after the respawn (it used to go into the corpse's pack and was lost). The Creative
  discard half is D-16, logged only.
- LF-14: `Bloodline.removeMember` clamps the pool to the new maximum, as a recruit's death does (a kick left 12000 in a
  5000 pool).

Late fixes driven by the other late bands (2026-11-01 and 11-02, uncommitted):

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
  (`message.hemomancy.chamber_visit.rite_unavailable`); the attunement itself is D-67 [D6-5].
- LF-13 (`brined_court`): the Champion's challenge names an outstanding retry wait before a missing strain or standing.
- LF-17, Circus half (`crimson_troupe`, docs only, 2026-11-07): CIRCUS.md and HEMOMANCY_REFERENCE §17.1 now list every
  acclimation source, including +50 for the first talk with each of the eight performer roles, and say spectacle banking
  is per player (four on Succession, one on Liberation). `firstTalkMilestones` logs it.
- D6 softlock: a Sanguine Formation medium could never be seated (a sneak-click skipped the Focus). It seats on a
  sneak-click (`SanguineFormationItem.doesSneakBypassUse`), and from this round whatever the off hand holds
  (`SanguineFormationItem.useOn` hands a sneak-click on a Focus to the Focus). The plain-click question is D-68 [D6-6].

Fixes from the final review (2026-11-02, uncommitted):

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

## Open questions

Each row is a probe that fails until the user decides (a row marked "logged by" is evidence logged inside a passing test).
Ids are the tests' `// DECISION:` ids; the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 uses the same numbers
with a `PT-` prefix. The late rows (D-35 on) keep the band id the test's `// DECISION` comment names in brackets
(D5, BC Brined Court, CT Crimson Troupe, D6, D7, D8, NPC, CHAIN for the late run). D-1 to D-34 are the first round's and
are not re-decided here; a late instance of one (D-3, D-10, D-16, D-19) is logged against it.

| Id | Question | Options | Probe test(s) |
| --- | --- | --- | --- |
| D-1 | Living-creature absorption pays 1 mL per 10-tick pulse: the 500 mL proof took 53 cows and about 4 minutes of holding. Acceptable for a Neophyte with no reservoir? | Keep / raise the living rate at D1 / lower the proof / point the Vicar's hint at reservoirs and flasks | `PlaythroughNeophyteGameTests.livingCowAbsorptionRate` |
| D-1b | Blood the player projects into their own cauldron and absorbs back counts toward the 500 mL proof (500 mL in 16 ticks, no creature). | Keep as the reservoir route / exclude the player's own projected blood / count only creature and natural reservoirs | `PlaythroughVotaryGameTests.condenserRoundTripCountsAsAbsorption` |
| D-2 | Absorption at a full vessel counts 0 mL toward the proof, but the hint says every absorption counts. | Count gross absorption / keep net and reword the hint and ledger step | `PlaythroughNeophyteGameTests.fullVesselAbsorptionCounts` |
| D-3 | One-time rewards drop unowned at the NPC's feet (7 of 10 claims: the Vicar's kit, Hermit Road report, Mnemonist starter, Worn Vow, Woven Vessel kit, Mason's map, Vein-Mason lesson), so a second player can take them; the same for shared station outputs and Effigy charges. | Inventory first, drop only overflow / PendingDelivery claims / an owner or pickup delay on the drop / keep | `PlaythroughNpcUsabilityGameTests.rewardLandingSurvey`, `PlaythroughNeophyteGameTests.secondPlayerTakesTheKit`, `PlaythroughInitiateGameTests.twoPlayersWovenVesselKit`, `PlaythroughVotaryGameTests.twoPlayersOneCentrifuge`, `PlaythroughAdeptGameTests.sharedEffigyChargeCredit` |
| D-4 | The Mason's Respite map is never reissued and has no recipe. | Reissue when not carried / add a recipe / keep | `PlaythroughAdeptGameTests.lostMapIsNotReissued` |
| D-5 | The D5 Vicar hands out the Sanguine Monolith blueprint, but its Working craft needs Degree 6. | Allow the craft at D5 / have the Monolith speak from D6 and move the blueprint / an outpost Monolith | `PlaythroughAdeptGameTests.d5MonolithCannotSpeak` |
| D-6 | Any Blood Shot weave completes WOVEN_VESSEL and opens the Brotherhood; the Mnemonist's indexing is optional. | Keep the index as Side work / require the index / keep and have the ledger and teachers say it is optional | `PlaythroughInitiateGameTests.weaveWithoutMnemonistUnlocksD4` |
| D-7 | The Vicar's D3 hint says to study the Bloodline system and the D4 hint the skill tree; neither names the real gate (the Loom weave; the Vein-Mason loadout). | Rewrite both in the Vicar's voice / add a second line / keep | `PlaythroughInitiateGameTests.vicarD3HintNamesTheRealGate`, `PlaythroughNpcUsabilityGameTests.degreeHintMatchesTheChapterGate` |
| D-8 | The temple is fragile: the Display breaks in survival and loses its Hermit link; a claimed Hermit still offers his heart; a different temple's Hermit says "you have claimed my heart" and offers a farewell that does nothing; a spent temple re-offers. | Protect and relink / re-arm on release / new lines / hide the farewell unless this Hermit's heart was taken / keep | `PlaythroughOnboardingGameTests.twoPlayersOneTemple`, `brokenMortalDisplayLeavesNoOath`, `wrongHermitClaimsAndSilentFarewell`, `deathBetweenBlessingAndTouch`; logged in `PlaythroughNpcUsabilityGameTests.nothingOfferedIsDeadAtThatStateSweep` |
| D-8b | A D1 player (or any awake blood) at an unclaimed Display is told to speak with the temple's Hermit, who can never bless them. | Say the heart cannot attach to awakened blood / keep | `PlaythroughOnboardingGameTests.awakenedPlayerAtUnclaimedDisplay` |
| D-9 | A D5 founder's held projection initiates a charm-bearer into the founder's line with no prompt to the recruit, pins them for 10 s, and the founder spends 0 mL. | Add a consent prompt / keep consent-free | `PlaythroughOnboardingGameTests.founderProjectionReceivesRecruit` |
| D-10 | A killed Respite Anchorite never returns, so a D4 player holding the map has no route to the Vein-Mason chapter (the D5 Main gate). | A respawn rule for unique NPCs / another teacher gives the lesson / skip empty sites in the map / keep | `PlaythroughNpcUsabilityGameTests.uniqueNpcLossIsRecoverable` |
| D-11 | D3 needs the server to skip the night: with one of two players awake the pending player stays D2 and is told nothing. | Settle after a minimum time in bed / a line saying the night has to pass / leave it to the sleep-percentage rule | `PlaythroughVotaryGameTests.sleepNeedsTheWholeServer` (control: `sleepAloneSettlesTheBlood`) |
| D-12 | An outpost Alembic has no heat source; no line says the base Alembic needs heat. | Heat the template / a hint line from the Alchemist and ledger / drop the heat requirement | `PlaythroughVotaryGameTests.outpostAlembicHasNoHeat` |
| D-13 | Using only outpost stations: the probe shows the ledger does read 8/8 (the craft credit counts). Left: should outpost stations need a personal craft at all? | Keep / let outpost stations work for anyone | `PlaythroughVotaryGameTests.outpostOnlyStationsLeaveLedgerIncomplete` (passes) |
| D-14 | The Vein-Mason lesson is recomputed from the live tendency: after a drift the given pattern's carve is refused and a free replacement is offered (also offered while the kit still lies on the ground). | Persist the issued lesson / keep | `PlaythroughAdeptGameTests.tendencyDriftDuplicatesPatterns` |
| D-15 | The Hermit Road report is D1-only, so a remnant first read at D2 leaves the Side card at 1/2 forever. | Report at any degree / hide the card after D1 / keep and say so on the card | `PlaythroughNeophyteGameTests.hermitRoadAfterD2IsDeadEnd` |
| D-16 | A Creative player with a full pack completes initiation: the conduit, four flasks and ledger are discarded but the one-time starter claim is recorded. | Treat a Creative discard as undelivered, as PendingDelivery does / keep (Creative only) | `PlaythroughOnboardingGameTests.creativeFullInventoryStarterKit` |
| D-17 | Logging out while a Venous Stone collapse or a pending structure craft resolves drops the result but never records the proof. | Record by owner UUID, applied at next login / pause or cancel on logout / accept | `PlaythroughNeophyteGameTests.proofLostWhenOfflineAtCollapse` |
| D-18 | A second assigned centrifuge spin overwrites the first spin's id, so the first output no longer completes First Separation. | Accept any assigned spin's output / refuse a second spin until the first is recovered / keep | `PlaythroughVotaryGameTests.secondSpinOverwritesTheProof` |
| D-19 | Reissue checks look only at the carried inventory: a stashed or dropped Concentrated vial is reissued; blueprints and fittings the same. | Once per claim (track the issued item) / keep as a lost-item safety net | `PlaythroughVotaryGameTests.stashedConcentratedVialIsReissued`, `droppedConcentratedVialIsReissued` |
| D-20 | A weaver offline when the Loom climax completes gets the memory but not the first-weave milestone (a D4 soft block). | Record by UUID at next login / cancel or refund the weave on logout / accept | `PlaythroughInitiateGameTests.weaverOfflineDuringClimax` |
| D-21 | D3 puppetry needs Circus instruction, but nothing a D3 player hears points to the Circus (its card appears at D4). | A D3 teaser line / move the gate to D4 / accept discovery by exploring | `PlaythroughInitiateGameTests.d3PuppetryHasNoPointer` |
| D-22 | The D5 strain needs a section to go from not-Varicose to Varicose: arms already Varicose at promotion never count. | Count an existing Varicose section once / reword the Mason / keep | `PlaythroughAdeptGameTests.preStrainedSectionNeverCounts` |
| D-23 | An outpost resident whose marker is obstructed silently fails to spawn (seen: the Mnemonist). | A fallback spot / a log line / keep | `PlaythroughNpcUsabilityGameTests.outpostSpawnsAllResidents` |
| D-24 | Read from the code: a guided Chamber visitor who logs out, or takes lethal damage under protection, has the guided lesson completed by the early return. | Complete only on timeout or the paired exit / any return counts / complete on death protection only | `PlaythroughInitiateGameTests.guidedChamberLogoutAndLethalDamage` (needs the Chamber-loaded review client) |
| D-25 | The conjuration dispel refund reads the stack after it is shrunk, so it is always the 90% cap whatever the wear (900 mL back from a 1,000 mL conjure). | Scale with wear / keep the flat 90% | logged by `PlaythroughNeophyteGameTests.dispelRefundIsSyncedAndRecorded` |
| D-26 | The charm taken off while a Concentrated injection is pending: the topic hides, the night's rest refuses, and nothing says why the player stays D2. | A line on waking or from the Alchemist / settle without the charm / keep | `PlaythroughVotaryGameTests.charmRemovedWhilePending` |
| D-27 | The D5 Vicar's Monolith scrap can be taken again and again (3 asks, 3 carried). | One per player / reissue only when not carried / keep | `PlaythroughAdeptGameTests.monolithScrapIsRepeatable` |
| D-28 | A refused reward can leave the client on a static "claimed/given" node with no refusal line. For the Anchorite's D5 reward the premise does not hold (the option is hidden until ready; the probe passes); a forged early claim is silent. | A system refusal line wherever a reward option can be refused / keep | `PlaythroughAdeptGameTests.refusedRewardNodeContradictsServer` |
| D-29 | With the server config `VASCULAR_DEGRADATION_ON_MANIP_ENABLED` off, the D5 Mason strain can never be recorded and the Mason keeps saying "cause strain". | A fallback strain route / a Mason line / treat the config as unsupported | `PlaythroughAdeptGameTests.d5StrainWithDegradationOff` |
| D-30 | A scar blank lost before the first carve cannot be replaced (the Anchorite offers nothing; no recipe, never sold). | Reissue the blank with the pattern / let the Anchorite trade one / keep it loot-only | `PlaythroughAdeptGameTests.lostBlankBeforeCarve` |
| D-31 | No Harbinger NPC names the D4 Sporitic Thurible, even asked about a held spore sac. | An Alchemist or Artificer pointer line / leave it to the Liber | `PlaythroughAdeptGameTests.d4ThuribleHasATeacher` |
| D-32 | A D4 player planting the Staff on a finished Bloodline or Fane floor is told "the floor is complete, but no rite matches this structure", not that the rite needs Degree 5 (layered floors filter out rites the player cannot attempt before the degree message can run). | Show the degree requirement for a matching higher rite / keep | logged by `PlaythroughAdeptGameTests.bloodlineFoundingOpensAtD5`, `foundingFaneOpensAtD5` |
| D-33 | The Specimen Jar lesson is offered only from the D2 Alchemist tree, but its gate stays open, so a player who reaches D3 untaught never sees it and a forged event still teaches it. | Offer it in the D3+ trees / restrict it to D2 / keep | logged by `PlaythroughNpcUsabilityGameTests.unofferedEventsRefusedSweep` |
| D-34 | Pending blood crafts count down once per loaded level each server tick (`HarbingerCardinalRiteEvents.onLevelTick` calls the global `PendingBloodCraftManager.tick()`), so with three dimensions loaded a craft resolves about three times early, a third of the way through the client's ring collapse, and the D-17 offline window is that much shorter. This is the consolidation ledger's PENDING decision F-11, so it was not changed. | Approve F-11 (tick each craft on its own level, or once from the server tick) / keep | timing visible in `PlaythroughNeophyteGameTests.proofLostWhenOfflineAtCollapse` |
| D-35 [D5-1] | A signed ledger joins anyone who uses it: a Degree 0 player and an inactive-blood D5 joined, with no leader consent. | Keep / D1+ with active blood / the leader confirms / D5 only | `PlaythroughIlluminatusGameTests.ledgerJoinHasNoGate` |
| D-36 [D5-2] | Members may pledge outpost Harbingers and mint signed ledgers by Bloodline Recall, but cannot release, set the recall point or disband, and have no way to leave a line (see D-90 [CHAIN-3] for what that does at D6). | Add a leave action / leader-only recruit and Recall / keep | `PlaythroughIlluminatusGameTests.bloodlineRecallAndMemberPowers` |
| D-37 [D5-3] | A kicked member right-clicking their old ledger gets no line, no rejoin and keeps the ledger. | Refuse with a line / allow a rejoin / burn the kicked member's copy | `PlaythroughIlluminatusGameTests.kickedMemberStaleLedger` |
| D-38 [D5-4] | Hematic Unbinding burns its Nether Star on the first, warning-only completion; the ledger's Disband is free. | Warn at activation, as the Fane relocation does / drop the rite / make Disband cost / keep | `PlaythroughIlluminatusGameTests.hematicUnbindingCostsTwoStars` |
| D-39 [D5-5] | No speaker a D5 or D6 player can reach says where Sanguine Quintessence comes from, though six D5 to D7 steps need it. | A Vicar or Alchemist line / an item inquiry / the Liber only | `PlaythroughLateNpcUsabilityGameTests.quintessenceHasNoPointer` |
| D-41 [D5-7] | The Alchemist's Basin commission reads the sample only from the main hand at talk time, and neither commission rewards. | Any-hand check / a reward / keep | `PlaythroughIlluminatusGameTests.alchemistBasinSampleInMainHand` |
| D-42 [D5-8] | A member's Projection on the Bloodwell with the pool already full earns no covenant proof and says nothing. | Count the attempt / a line that the pool is full / keep | `PlaythroughIlluminatusGameTests.fullPoolDepositSaysNothing` |
| D-43 [D5-10] | The Vicar offers the Monolith scrap (blueprint) only at exactly D5; at D6 to D8 a player who never took it cannot get it. Note: the forged-event guard in `VicarDialogueEvents.handleGiveSanguineMonolithBlueprint` copies the tree's exactly-D5 gate (and refuses a purifying player), so whatever is ruled here (and on D-5 and D-27) must change the tree and that guard together, or the new option will hand out nothing and say nothing. | Offer at D5+ / a recipe / keep | `PlaythroughLateNpcUsabilityGameTests.monolithBlueprintOnlyAtD5` |
| D-44 [D5-11] | The second, dissolving Hematic Unbinding returns `poolBlood / memberCount` only to members online at that moment (and only as far as their vessel has room): an offline member's share is lost (0 mL against 1000 mL in the run). Their ledger burns only on its next use or tick; the pending window is a root persistent key a death clears. | Bank the offline share and pay it at login / refuse the dissolution while a member is offline / keep | `PlaythroughIlluminatusGameTests.hematicUnbindingOfflineShare` |
| D-45 [BC-1] | Court standing depends on the order of acts: a custom given before the site visit leaves the player a GUEST, which closes the Champion. | Order-independent standing / re-earnable / keep | `PlaythroughBrinedCourtGameTests.customBeforeVisitLocksGuest` |
| D-46 [BC-2] | Logging out or swimming 64 blocks off ends a usurpation with no retry wait, while dying costs a day. | The same wait for an escape / pause and resume / keep | `PlaythroughBrinedCourtGameTests.usurpationEscapedByLogout` |
| D-47 [BC-3] | "Ask to join" spends the one free house switch with no warning. | A confirm step / a warning line / keep | logged by `PlaythroughBrinedCourtGameTests.joinSwitchAndThirdChange` |
| D-48 [BC-4] | The Champion challenge starts the moment the option is chosen, with no confirmation. | A confirm node / keep | `PlaythroughBrinedCourtGameTests.challengeStartsWithoutConfirm` |
| D-49 [BC-5] | Joining the Shared Wake needs its rare nursery, and nothing points to one. | A pointer line / join from a school / keep | logged by `PlaythroughBrinedCourtGameTests.courtPointersPastTheGarden` |
| D-50 [BC-6] | The Relay Chart points only at the Lighthouse Garden; nothing locates the Vent Sanctum or the outposts. | Chart modes / residents name places / keep | logged by `PlaythroughBrinedCourtGameTests.courtPointersPastTheGarden` |
| D-51 [BC-7] | The Lightkeeper's "feed it there" implies Incubator feeding, but the x1.5 is worn upkeep only. | Reword / extend the bonus / keep | logged by `PlaythroughBrinedCourtGameTests.joinSwitchAndThirdChange` |
| D-52 [BC-8] | The Champion has no degree gate: a D4 is offered the usurpation. | A D5 gate / keep | `PlaythroughBrinedCourtGameTests.championHasNoDegreeGate` |
| D-53 [BC-9] | With Pelagic worldgen off, three NPCs still point to the Court. | Hide the pointers / a line / an unsupported preset | logged by `PlaythroughBrinedCourtGameTests.courtPointersPastTheGarden` |
| D-54 [BC-10] | No Court NPC speaks past D5 or about the Primal step. | Add lines / the Liber only | logged by `PlaythroughBrinedCourtGameTests.courtPointersPastTheGarden` |
| D-55 [BC-12] | Court members can be leashed (vanilla `Mob#interact` takes a Lead before `mobInteract`). The Lamp-colony's gift is two Leads and lands in the empty hand the player talked with, so the next right-click ties a member up instead of opening its dialogue, and a mobile member can be dragged off its mark. | `canBeLeashed` false for Court members, as for villagers / keep leashing but open the dialogue first / keep | `PlaythroughBrinedCourtGameTests.heldLeadLeashesACourtMember` |
| D-56 [NPC-2] | The Court custom trade card adds rapport (+2); the same offering made through dialogue adds none, though the doc says they pay alike. | Both pay / neither / fix the doc | `PlaythroughBrinedCourtGameTests.tradeCardPaysRapportDialogueDoesNot` |
| D-57 [CT-1] | No floor cell of the real pavilion's 1888 standable cells reaches the idle Ringmaster on his y7 perch with the server's interact check; his pact, acts, finale and trade box all need that click. The documented private galleries do not exist. | Build the galleries / real stairs or a reachable perch / lower the idle perch / accept that players build up | `PlaythroughCrimsonTroupeGameTests.ringmasterReachFromTheFloor` |
| D-58 [CT-2] | The Missing Understudy's stage record is only in `troupe_abandoned_stage`, and no waybill target or NPC line points to one. | A waybill mode or target / an NPC direction / exploration | `PlaythroughCrimsonTroupeGameTests.abandonedStageHasNoPointer` |
| D-59 [CT-3] | The rehearsal threats are Husks, which Peaceful removes on their first tick, so no practical or guest act can finish in Peaceful. | Non-monster targets / a Peaceful refusal line / accept | `PlaythroughCrimsonTroupeGameTests.practicalsInPeaceful` |
| D-60 [CT-5] | The Threadkeeper's "An Empty Ring" lesson node is built but no option leads to it, so the briefing can never be read. | Give the Threadkeeper an option to it / move the briefing into the inspect or report lines / drop the node | `PlaythroughCrimsonTroupeGameTests.threadkeeperLessonUnreachable` |
| D-61 [CT-6] | Two lines mislead: `finale.claimed` (another player holds the site) reads as permanent; a forged neutral finale's `not_ready` names Succession to a neutral visitor. (`ring_blocked`, "blocked or wet", was suspected for a second pupil at a busy professor; `twoPupilsOneProfessor` shows the second pupil simply gets a practical of their own.) | Separate lines / keep | logged by `PlaythroughCrimsonTroupeGameTests.ringmasterLostLeavesSiteOpen` |
| D-62 [CT-7] | A Ringmaster lost mid-finale leaves the site owned in RAFTERS with outcome NEUTRAL: no Ringmaster returns, the finale never ends and the sneak waybill keeps pointing there (D-10 family). Since 2026-11-07 the site is released once with `hemomancy.circus.finale.ringmaster_lost`, but still no Ringmaster returns. | A respawn rule / settle or release the site with a line / skip such sites in the waybill / accept | `PlaythroughCrimsonTroupeGameTests.ringmasterLostLeavesSiteOpen` |
| D-63 [CT-8] | A killed rehearsal threat is discarded; the Mummer (and Bellows) practical needs two, so it cannot pass and nothing says so until its five-minute expiry or a walk-away. | Replace the lost threat / end the practical with the retry line / accept | `PlaythroughCrimsonTroupeGameTests.practicalTargetKilledStalls` |
| D-64 [D6-1] | Only the progenitor can bind a Covenant Throne: a D6 member is told `not_progenitor` and can never finish the Living Covenant (see D-90 [CHAIN-3]). | Members bind the leader's Throne / another member proof / keep and say so | `PlaythroughSanctifiedGameTests.memberCannotBindThrone` |
| D-65 [D6-2] | Any progenitor can bind any Throne, another line's inside another line's Fane included (respawn set), though the Vicar says "your Fane". | Require the binder's own Fane / owner-stamp the Throne / reword the Vicar | `PlaythroughSanctifiedGameTests.anyProgenitorBindsAnyThrone` |
| D-66 [D6-3] | Eternal Covenant is a root persistent key (the S-1 family): after a death it is gone, max blood drops 5400 to 4900 mL and the rite can be started again (the advancement stays). | Move it with S-1 (PlayerPersisted or its own attachment) / keep until S-1 | `PlaythroughSanctifiedGameTests.eternalCovenantAcrossDeath` |
| D-67 [D6-5] | The Rite of the Chamber of Will attunes the caster even when no visit can start (LF-12 now says so). | Refuse before completion / attune and say so (as now) / keep | `PlaythroughSanctifiedGameTests.chamberRiteAttunesWithoutVisit` |
| D-68 [D6-6] | A Sanguine Formation medium (Monolith, Throne, Warp Chair, Scarlet Vanity crafts) seats only on a sneak-click; a plain click tries the floor and says "no rite matches this structure", with no hint to sneak. | Seat on a plain click when the floor wants a Formation medium / add "sneak to seat it" to that refusal / keep | `PlaythroughSanctifiedGameTests.formationSeatsOnPlainClick` |
| D-69 [D6-7] | The D6 Vicar's covenant hint ignores the line: a lineless D6 who kept `covenant_written_in_place` (expelled, or the line dissolved) is told to bind a Throne "to your Fane" (and return from the Chamber, keep the Vigil), which needs a line they no longer have. | Tell them to found a line first / a line about the lost line / keep | `PlaythroughSanctifiedGameTests.linelessD6VicarGuidance` |
| D-70 [D6-8] | A disbanded progenitor keeps `covenant_throne_bound` and the Throne's respawn point though the line and its Fane are gone, and the Disband says nothing about it. | Clear the Throne part with the Fane / keep it and say so on Disband / keep | `PlaythroughSanctifiedGameTests.disbandedProgenitorThrone` |
| D-71 [NPC-4] | The Mnemonist's Anchorite counsel is a lore Conversation card whose sigil clears once read, while the D6 Mason chain still waits on that counsel. | A Quest Work card with a sigil until the counsel is recorded / keep | `PlaythroughSanctifiedGameTests.counselIsALoreCard` |
| D-72 [D7-1] | From the ninth pome until D8, `HarbingerPathPermissions.isChoiceUnresolved` refuses every cast but `conjure_staff` and every rite but the Apotheos; undocumented, and it reads a pome total that adds up across Blooms while Communion is counted per Bloom. | Document it and warn before the ninth pome / lock only before the revelation / gate on Communion | `PlaythroughArchonGameTests.powerLockAfterTheNinthPome` |
| D-73 [D7-2] | Monoliths have no owner: any D7 who has crafted one can shatter another player's and take its unowned Seed and fragments. | Bind the Monolith to its builder / drops to the presser's pack / keep | `PlaythroughArchonGameTests.strangerShattersYourMonolith` |
| D-74 [D7-3] | A shatter rolls 5 to 8 fragments; the crafted cornerstone plus the Monolithic Armature rite need 8, so shattering before claiming the free cornerstone on a low roll leaves the player short, with no warning. | A warning in the press-again node / raise the minimum roll / keep | `PlaythroughArchonGameTests.shatterBeforeCornerstone` |
| D-75 [D7-4] | Logging out mid-projection runs the forced return and grants the revelation without the two minutes (and spends the 500 mL even with no Gardens loaded). | Pause and resume / refuse before paying when the Gardens are missing / keep | `PlaythroughArchonGameTests.logoutCompletesProjection` |
| D-77 [D7-6] | `MaterialsData` unlocks monolith_fragment, monolith_imbued_cloth and the four hallowed_residuum entries at D5, against the D7 shatter and the Saint D8 ruling. | Move them to D7/D8 with Monolith/Saint traces / keep them as previews | `PlaythroughArchonGameTests.atlasFragmentsAndResiduum` |
| D-78 [D7-8] | After Communion no reachable speaker names the Fungal Spine, and the Spine has no item inquiry. | A Vicar or Monolith line / a Spine item inquiry / the delivery line and the Liber only | `PlaythroughLateNpcUsabilityGameTests.springPointerAfterCommunion` (also logged by `nextStepPointerSweep`) |
| D-79 [D7-9] | A D7 Silence (SILENT_PENDING, Vesper not yet met) hears nothing that names the Severance or the Living Sickle. | A Vicar or Mnemonist line / the whisper and the Liber only / keep | `PlaythroughLateNpcUsabilityGameTests.severancePointerForSilence` (also logged by `nextStepPointerSweep`) |
| D-80 [D8-1] | An Apotheos whose heart-node forces respawn has no way out of the Gardens after a death (HR 5.6 says the return is never gated). NOT RUN here: the Gardens are not loaded. | World spawn / their bed / last Overworld position / keep | `PlaythroughEndgameGameTests.heartNodeRespawnTrap` (NOT RUN) |
| D-81 [D8-2] | A Mycophant victory is lost if the owner logs out or dies during the 120-tick yield (the D-20 pattern). | Record the win by UUID and settle at the next login / settle at the killing blow / keep | `PlaythroughEndgameGameTests.mycophantYieldLogout` |
| D-82 [D8-3] | At APOTHEOS_PENDING the Rite of Apotheos is not discovered and a blank blueprint cannot be imprinted with it; it is found only through an optional memo, or at D8. | Unlock the Truth entry at APOTHEOS_PENDING / a pointer line from a teacher or the choice whisper / keep | `PlaythroughEndgameGameTests.apotheosRiteNotInTheLiber` |
| D-83 [D8-4] | A D8 Silentium gets the same D7 Archon greeting tree from all four teachers; only the ending line differs. | A Silentium tree / keep the Archon tree | `PlaythroughEndgameGameTests.silentiumGreetedAsArchon` |
| D-84 [D8-5] | The expelled line says the hive "will remember after five minutes", but a defeated Apotheos's rematch never re-arms by itself; only a new lure starts one. | Reword the line / re-arm the rematch after the cooldown / keep | `PlaythroughEndgameGameTests.mycophantRematchLine` |
| D-85 [NPC-1] | Two lines recruited the same outpost Vicar; the ledger's summon moved him away for good, so a D0 newcomer finds no Vicar at that outpost. | Send home after use / successors only / one line per original / keep | `PlaythroughLateNpcUsabilityGameTests.summonStripsTheOutpost` |
| D-86 [NPC-3] | The D5/D6 Main guidance is the Vicar's lore card in Conversation, whose notice sigil clears after one read; it is not a Quest Work card. `complete_vigil` names only a recruited Vicar as the Vigil's helper. | A Quest card with a sigil per step / extra lines / keep | `PlaythroughLateNpcUsabilityGameTests.mainStepsAreQuests` |
| D-87 [NPC-6] | The line's default name is stored as concatenated English (`"<name>'s Blood Line"`), so it reads in English everywhere it shows. | A translatable default name with a storage change / keep | `PlaythroughLateNpcUsabilityGameTests.bloodlineNameIsEnglish` |
| D-88 [CHAIN-1] | In a grand rite's ORDEAL, dry anchors raise the deficit pressure every second, which re-breaks or re-threatens the same anchors by damage priority, and Projection pays a 50 mL repair before it fills a damaged anchor; two repairs a second are all bare Projection gives (5 mL a tick), so a solo caster never refills ("The ordeal cannot advance while the cardinal stations run dry"). It is not the wave deck: with the draining waves pinned out, the rings woken at waves 2 and 3 still came up dry and damaged and deadlocked. Seen on the Rite of Apotheos and the Bloom of the Qliphoth. | Fill a dry anchor before (or while) repairing it / press only anchors not under repair / slow the pressure or cheapen the repair at grand rites / keep (helpers or a planned Seal trace) | `PlaythroughEndgameGameTests.apotheosRiteWithoutRescue`; `apotheosRiteByPlay` and the late run pass with the logged rescue (DEGRADED) |
| D-89 [CHAIN-2] | After the forced return the revelation-choice whisper waits behind memo whispers (two in the probe; five, repeating, in the late run), and "Listen when ready" opens the oldest first. | Open decision whispers first / fold repeated memo whispers into one / keep | `PlaythroughArchonGameTests.revelationWhisperOpensFirst` (also logged by the late run's `chooseByTheSpine`) |
| D-90 [CHAIN-3] | With LF-2 (one line per player) a member cannot found a line, only a progenitor binds a Covenant Throne (D-64 [D6-1]), and members have no way to leave (`EarlyInitiation.release` takes off the charm and deactivates blood but never calls `removeMember`; D-36 [D5-2]). So every member, a D1 initiate brought in by a founder included, stops at D6 for good unless the leader expels them (an expelled member can found a line and bind a Throne: `formerMemberReachesTheThrone`). Settles D5-2 and D6-1 together. | Add a leave action / let members bind their leader's Throne / keep LF-2 and accept that members stop at D6 (and say so) | `PlaythroughSanctifiedGameTests.memberRouteToD7` |

Smaller observations from the runs, not decisions yet: the first Vein-Mason kit has no Runic Motif Paper, which the
Effigy needs (it is craftable, but no line says so); drinking a starter Bloody Flask shows the Blood Drunkenness line
("Foreign blood clouds the rite.") with no rite running; the Specimen Jar lesson says nothing in chat when its jars drop;
the Three Answers counsel sent to the Artificer instead of the Alchemist is swallowed with no line; a lit Brazier holding
an unlearnt pattern repeats its refusal every use tick; the Anchorite's entry in the reference says "he" where
`docs/NPC_VOICES.md` gives them "they"; `EarlyInitiation` cancels a ceremony at more than 4 blocks while the old band
spec said 2.

Smaller late observations, not decisions yet: a Living Staff dropped on death is gone at once (`LivingItem` lifespan 0),
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

- The user's decisions above (D-35 on), then flip the probes they settle to required.
- Proof owed by clients and seeded worlds (see "Proof owed" above): the Chamber of Will, the Fungal Gardens, seeded Court
  Seats and Circus pavilions.
- Unstained playthroughs, once that path's design settles.
- Client-driven runs with `GameplayCampaignDriver` for what the GameTest server cannot see: screens, sigils as drawn, the
  ceremony visuals.
- Flip `harbingerD0ToD5`, `harbingerD5ToApotheos` and `harbingerD5ToSilentium` to required after a few stable runs.
