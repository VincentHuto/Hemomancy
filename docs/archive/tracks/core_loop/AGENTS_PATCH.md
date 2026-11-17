# AGENTS.md patch for the core loop overhaul

Date: 2026-11-11 (core-loop round two, P12). `PacketHandler.PROTOCOL_VERSION` is 49.

Neither round of the core loop overhaul edited `AGENTS.md` (CQ-13). This file holds the exact lines that would bring it
in line with both rounds: Part A replaces six existing bullets, Part B adds new bullets under `AGENTS.md`'s own
headings, and Part C lists lines considered and left out. Every class, method, tag, path and test named here was
checked on disk on 2026-11-11, and every "current text" block in Part A is copied byte for byte from `AGENTS.md`, so a
search for it finds exactly one line.

To apply: for each Part A item, find the current line (each is one bullet on one line of `AGENTS.md`) and replace it
with the replacement block's line. Then paste each Part B bullet at the end of the section it names. The long form of
each rule is in `docs/architecture/ARCHITECTURE_STANDARDS.md`; the two must agree.

## Part A: replacements

### A1. A caster chooses targets (the PlayerBlockEdits sentence)

Current text:

```text
- A caster chooses targets through `ManipulationCombatHelper`: `canHarm(player, target)` is the strict rule, `canHarm(caster, target, Reach.X)` keeps an older filter's verdicts with a named preset (moving a site to `Reach.STRICT` is a pending decision, G-2b or F-16), and `canHarmFor(source, target)` serves constructs, summons, projectiles and effect holders. A `Monster.class` gather passes `canHarm` in its own arguments (`CombatTargetingGuardrailTest`). Drudge work lives in `DrudgeActions`; heal reductions are `HealModifier` entries applied in one stage; flight is granted and revoked only through `FlightArbiter` (`HealAndFlightGuardrailTest`). A block a player's cast or payload places or breaks goes through `common/util/PlayerBlockEdits` (game mode, spawn protection, the NeoForge break or place event); `PlayerBlockEditGuardrailTest` lists the non-player editors.
```

Replacement:

```text
- A caster chooses targets through `ManipulationCombatHelper`: `canHarm(player, target)` is the strict rule, `canHarm(caster, target, Reach.X)` keeps an older filter's verdicts with a named preset (moving a site to `Reach.STRICT` is a pending decision, G-2b or F-16), and `canHarmFor(source, target)` serves constructs, summons, projectiles and effect holders. A `Monster.class` gather passes `canHarm` in its own arguments (`CombatTargetingGuardrailTest`). Drudge work lives in `DrudgeActions`; heal reductions are `HealModifier` entries applied in one stage; flight is granted and revoked only through `FlightArbiter` (`HealAndFlightGuardrailTest`). A block a player causes to be placed, broken or converted (a cast, a payload, an item, a block's use, a rite, a projectile, or a run or other world system acting for a player) goes through `common/util/PlayerBlockEdits` (`place`, `mayBreak`, `breakBlock`, `replace`, `lift` / `settle`: game mode, spawn protection, the NeoForge break or place event). `PlayerBlockEditGuardrailTest` scans `common/manipulation`, `network`, `event`, `item`, `block`, `tile`, `rite`, `entity` and `world` and counts every raw `setBlock`, `setBlockAndUpdate`, `destroyBlock` and `removeBlock` per file against `src/test/resources/architecture/player-block-edit-allow-list.txt` (count, file, reason); a guarded call elsewhere in a file does not excuse its raw edits.
```

Why: Round one added `replace`, `lift` and `settle`, and round two P3 widened `PlayerBlockEditGuardrailTest` to nine scan roots with per-file counts. Guardrail: `PlayerBlockEditGuardrailTest`.

### A2. Third-person player poses

Current text:

```text
- Third-person player poses apply through `client/player/PlayerAnimationRegistry.THIRD_PERSON`, in its listed priority order (staff planting, offset reset, casting, then the first claiming pose of microscope, vial injection, syringe draw, torch breath, Unstained swing, living-weapon stroke, then the warp chair); `MixinHumanoidModel` calls only the registry. First-person hand renderers keep their pinned `RenderHandEvent` priorities (`PlayerAnimationGuardrailTest`).
```

Replacement:

```text
- Third-person player poses apply through `client/player/PlayerAnimationRegistry.THIRD_PERSON`, in its listed priority order (staff planting, offset reset, casting, then the first claiming pose of microscope, vial injection, syringe draw, torch breath, Unstained swing, living-weapon stroke, strand stroke, Venous Cling, then the warp chair); `MixinHumanoidModel` calls only the registry. First-person hand renderers keep their pinned `RenderHandEvent` priorities (`PlayerAnimationGuardrailTest`).
```

Why: `PlayerAnimationRegistry.THIRD_PERSON` on disk lists `strand_stroke` (Track 12) and `venous_cling` (Track 9) after `living_weapon_stroke` and before `somnolent_seat`. Guardrail: `PlayerAnimationGuardrailTest`.

### A3. `/hemo` is one root

Current text:

```text
- `/hemo` is one root in `common/command/HemoCommand` (operators only) wiring one `<Name>Subcommand` class per branch; add a branch as a new class, keeping the syntax pinned by `HemoCommandTreeTest` and the `hemo_command_validation` fingerprints (`HemoCommandStructureGuardrailTest`).
```

Replacement:

```text
- `/hemo` is one root in `common/command/HemoCommand` (operators only) wiring one `<Name>Subcommand` class per branch; add a branch as a new class, keeping the syntax pinned by `HemoCommandTreeTest` and the `hemo_command_validation` fingerprints (`HemoCommandStructureGuardrailTest`). The core-loop fixtures are dev-only branches of their own (`temperpool`, `tank`, `vat`, `gouge`, each `.requires(DevCommands::available)`) over `common/command/CoreLoopFixtures`, whose per-player state is a UUID session map cleared through `TransientServerStateEvents`; they are not `/hemo test` scenarios.
```

Why: Round two P11 moved the core-loop fixtures out of `gametest/CoreLoopTestScenarios` into four `<Name>Subcommand` classes. Guardrails: `HemoCommandStructureGuardrailTest`, `HemoCommandTreeTest`, `hemo_command_validation`.

### A4. Networking

Current text:

```text
- Networking is NeoForge 1.21 payload-based. Follow `common/network/PacketHandler.java`: each packet uses `CustomPacketPayload`, static `TYPE`, `STREAM_CODEC`, and `playToClient` / `playToServer` registration in its real direction (only `AntecedentEffectPacket` is bidirectional). Adding, removing or re-laying-out a payload means bumping `PacketHandler.PROTOCOL_VERSION` and rewriting `src/test/resources/network/payload-registry.txt`. Send only through the `PacketHandler` facade (`sendToPlayer`, `sendNear`, `sendTracking`, `sendToAll`, `sendToDimension`, `sendToServer`), validate serverbound claims with `ServerboundGuards`, and pace request-and-reply or key-press payloads through `common/util/ServerboundRateLimiter` (session state the hub clears). A client-bound handler that needs client classes is registered as `ClientPayloadHandlers::dispatch`, with its body in `client/network/Clientbound*Handlers`. A payload decodes, validates and calls one service method; the gameplay lives outside `common/network` (`NetworkPackageGuardrailTest`). Do **not** add legacy `SimpleChannel` code.
```

Replacement:

```text
- Networking is NeoForge 1.21 payload-based. Follow `common/network/PacketHandler.java`: each packet uses `CustomPacketPayload`, static `TYPE`, `STREAM_CODEC`, and `playToClient` / `playToServer` registration in its real direction (only `AntecedentEffectPacket` is bidirectional). Adding, removing or re-laying-out a payload means bumping `PacketHandler.PROTOCOL_VERSION` and rewriting `src/test/resources/network/payload-registry.txt`. Send only through the `PacketHandler` facade (`sendToPlayer`, `sendNear`, `sendTracking`, `sendToAll`, `sendToDimension`, `sendToServer`), validate serverbound claims with `ServerboundGuards`, and pace request-and-reply or key-press payloads through `common/util/ServerboundRateLimiter` (session state the hub clears). A client-bound handler that needs client classes is registered as `ClientPayloadHandlers::dispatch`, with its body in `client/network/Clientbound*Handlers`. A payload decodes, validates and calls one service method; the gameplay lives outside `common/network` (`NetworkPackageGuardrailTest`). What another player's client needs to draw a caster's verb (reach, active verb, selected manipulation) is sent by `common/manipulation/verb/VerbPresence` (`PacketSyncVerbPresence`, to trackers, only while a verb is channelled) and read in common code only through `ClientBridge.onlookerReach` / `onlookerSelection` / `onlookerVerb`; never read a remote player's attachments for it. Do **not** add legacy `SimpleChannel` code.
```

Why: Round two P11 added the onlooker sync (`PROTOCOL_VERSION` 49). No version number goes into `AGENTS.md`, as before. Guardrails: `PayloadProtocolVersionTest`, `CommonClientIsolationTest`.

### A5. Run `alphaCheck`

Current text:

```text
- Run `./gradlew.bat alphaCheck` for JVM contracts, dedicated GameTests, runtime resource errors and packaged asset-path/PNG-header/font-license checks; see `docs/TESTING.md` for the in-game `/hemo test` fixture workflow.
```

Replacement:

```text
- Run `./gradlew.bat alphaCheck` for JVM contracts, dedicated GameTests, runtime resource errors and packaged asset-path/PNG-header/font-license checks; see `docs/TESTING.md` for the in-game `/hemo test` fixture workflow. Every test-grid GameTest server (`runGameTestServer`, each suite launcher, and `runPlaythroughGameTestServer` for the opt-in playthrough suite) runs under the isolation harness (`src/gameTest/resources/gametest-isolation.mixins.json`, `gametest/GameTestIsolation`: a 64-block grid, two forced margin chunks, a chunk-aligned random origin; `-PgameTestOrigin=x,z`, `-PgameTestMarginChunks=N`). A GameTest that fails once and passes on a re-run is a defect: find the cause and fix it, never re-run it away (`docs/TESTING.md` "Deterministic GameTests"). The core-loop fixtures are `/hemo temperpool|tank|vat|gouge`, not `/hemo test`.
```

Why: Round two P1 added the isolation harness and P2 made the playthrough suite's required probes pass. The rules are in `docs/TESTING.md` "Deterministic GameTests" and `ARCHITECTURE_STANDARDS.md` section 11.

### A6. Version-controlled tools

Current text:

```text
- `tools/model_export/` (the model generators that tests read), `tools/review/` (multi-client review launchers), `tools/circus/*.py` (the troupe's body, faculty and camp builders and `light_pavilion.py`), the mausoleum generator (`tools/worldgen/mausoleum_complexes.py` + `tools/worldgen/mausoleum/`) and the Brined Court generators (`tools/worldgen/brined_court_seats.py`, `tools/worldgen/brined_court_outposts.py` + `tools/worldgen/brined_court/`) the Mason's Respite generator (`tools/worldgen/masons_respite.py`), the Harbinger sites generator (`tools/worldgen/harbinger_sites.py` + `tools/worldgen/harbinger_sites/`, the Blood Temple and the Harbinger Outpost's pieces) and the wayside sites generator (`tools/worldgen/wayside_sites.py` + `tools/worldgen/wayside/`, the Bog-Body Ossuary Niche and the Sanguine Surveyor Bivouac) are version-controlled; the rest of `tools/` stays local. A single review client is a row in the `gradle/review-clients.gradle` table, not a hand-written run block.
```

Replacement:

```text
- `tools/model_export/` (the model generators that tests read), `tools/review/` (multi-client review launchers), `tools/circus/*.py` (the troupe's body, faculty and camp builders and `light_pavilion.py`), the mausoleum generator (`tools/worldgen/mausoleum_complexes.py` + `tools/worldgen/mausoleum/`) and the Brined Court generators (`tools/worldgen/brined_court_seats.py`, `tools/worldgen/brined_court_outposts.py` + `tools/worldgen/brined_court/`) the Mason's Respite generator (`tools/worldgen/masons_respite.py`), the Harbinger sites generator (`tools/worldgen/harbinger_sites.py` + `tools/worldgen/harbinger_sites/`, the Blood Temple and the Harbinger Outpost's pieces) and the wayside sites generator (`tools/worldgen/wayside_sites.py` + `tools/worldgen/wayside/`, the Bog-Body Ossuary Niche and the Sanguine Surveyor Bivouac) are version-controlled; the rest of `tools/` stays local. A single review client is a row in the `gradle/review-clients.gradle` table, not a hand-written run block; a row marked `bare: true` also gets a `write<Run>Classpath` task so a `tools/review/` launcher (such as `review-onlooker.ps1`) can start it as plain java after one Gradle call.
```

Why: Round two P11 added `bare: true` rows and `tools/review/review-onlooker.ps1`. `tools/model_export/` already covers the new generators (`fungal_strains.py`, the stain painter in `ground_network.py`, `living_land_sprites.py`). Guardrail: `BuildRunTableGuardrailTest`.

## Part B: additions

Paste each bullet at the end of the named section of `AGENTS.md`.

### Under "Architecture map"

```text
- Projection and Absorption are verbs, not memories: the active verb is player state (`IKnownManipulations.getActiveVerb`) set by the verb key (`VerbSelection.press`) or by a verb pick on the radial's inner ring (`BloodVerbChoicePacket`, `VerbSelection.choose`, which puts the selected memory down, so the pick is the plain verb); they are never equipped, so the cycle key skips them (CL-2, ruled 2026-11-10). A selected memory shapes a verb through `setProjectionShaping(Shapings::x)` / `setAbsorptionShaping(Shapings::x)` in `ManipulationInit`, and a narrow Shaping tried ahead of the main one is `addProjectionShaping` / `addAbsorptionShaping` (in the order attached; a held channel already open keeps the channel ahead of them all). Only `ShapingDispatcher` runs them, after rites, Saints and Wills and before the plain verb, paying through `BloodManipulation.tryPerformVerbPulse`. A Shaping that must refuse for reasons of place or right implements `VerbShaping.admits`, asked before Temper or blood is priced; a price or reach that depends on the player is `bloodPerPulse(Player)` / `reachScale(Player)`; one that draws its own link from hand to target returns `drawsOwnStream()` true (`ShapingGuardrailTest`).
- A combat strand is a `Shapings` factory calling `Strands.lash/jet/spray/plant/brand(...)`, run only by `StrandService`; every strand hit goes through `StrandHits.strike`, targets come from `ManipulationCombatHelper.swept` and `cone`, and the strand package names no manipulation. What a strand skill level is worth lives in the pure `StrandSkills`; a skill a client needs to run the caster's motion, and the off-hand twin, ride `StrandCue`'s own bits (20 to 22; 23 and up are free), never the client's skill cache. The twin is a copy of the open strand, not a second channel. What Absorption gains from a creature is the `AbsorptionRiders` table keyed by school state (`ShapingGuardrailTest`, `CombatTargetingGuardrailTest`).
- A block's blood is a `BlockBloodVolume` read only through `ReservoirBlood.settled` (it clots by `CoagulationRules` against the level clock); a multiblock filler resolves to its main block's tank through `ReservoirBlood.tank`; a living holder declares `isLivingVessel()`, a block without a tank `holdsBlood()` false; clot leaves as Formations only through `HeldFormations`; who may draw is `BloodDrawRights`. A sutured machine's state is `BloodRoutingHelper.linkStatus`, kept on the tank as a `ReservoirReadout.Link` (saved only when not `NONE`) and drawn by `BloodVolumeBarWidget`. No machine makes blood, and nothing keeps blood fresh without its keeper.
- Temper, the spendable pool beside Temperament, is the `hemomancy:blood_temper` attachment; a cast or Shaping pulse charges it only through `TemperCharge` in `BloodManipulation` (planned before pricing, settled after payment), a new source fills it through `TemperCharge` (`fillFromFood`, `fillFromWill`, `fillFromKept`, `fillFromCarrion`), and catalysts are data in `temper_catalyst/<school>.json`. Temper never refuses: dry costs more blood and lands softer. State that only remembers what was last sent to a client (the Scrying Temper preview in `ManipulationDiagnosticsSync`) is a session map cleared through `TransientServerStateEvents`, never a field on the attachment: reading the attachment creates it on players who had none (`TemperGuardrailTest`).
- Who may be kept (leash, jar, vat) is one rule, `MenagerieRules.canKeep` through `MenagerieLeash.verdict`; a leash lives on the creature and only `MenagerieLeash` runs it; a kept creature is a `KeptSpecimen` in a `SpecimenVessel`. A vessel's screen is the slotless `SpecimenVesselMenu`, its values menu data slots worked out per viewer by `VesselView`, its one button dispatched to the vessel's own `pressBrood`; breeding is run by `SpecimenBrood` with every number in `BroodRules`. What a player knows of a kind's blood is `BloodResearch` (`met`, `recordMet`, examined, studied), never the stack's identified flag (`StationMenuGuardrailTest`).
- A mark blood leaves on the world (stain, gouge, living vein, Lux trace) is a `GroundMarkBlock` whose rules live in `common/world/ground/GroundNetwork`; gouge functions are data in `gouge_catalyst/<function>.json` (`GougeCatalysts`, mirrored to clients through the `hemomancy:gouge_catalysts` channel in `DatapackSyncChannels`); lit runs are drawn only by `GroundRunRenderer`. A lit run carries its owner (`GroundRunSavedData.Lit`, the six-argument `GroundNetwork.pulse`), and its block edits go through `PlayerBlockEdits` for that owner only while they are online, alive and in the run's level. A furnace burned from outside goes only through `FurnaceHeat.stoke`; Blood Eclipse's field of dark is the session service `DarkFields` with its numbers in `DarkFieldRules`.
- A crop's growth answers blood through `BloodFedGrowthRules` and one subscriber, `BloodFedGrowthEvents`; fed soil is stamped on the `fed_ground` chunk attachment through `BloodFedGround`. A fungal strain is one row of `common/block/harbinger/plant/FungalStrains` on a `#hemomancy:strain_substrates/*` tag, never a `CropBlock` and never named outside the table, with its assets from `tools/model_export/fungal_strains.py`. Chum pools live in `ChumPools`, poured only by the projection chain's water link; the hunter a pool draws is chosen by `WorldAnswer.drawHunter` and capped across kinds by `#hemomancy:chum_hunters`.
- Blood moved through the Living Staff is recorded only by `LivingStaffGrowth.record`; a digging Living form pays per block only through `LivingDigging`; every ore vein goes through `OreVeins` over `CrawlSession`.
- A block a manipulation takes out of its place for a while is lifted and set through `PlayerBlockEdits.lift` / `settle` and recorded in `LivingLandLedger` until it is back; cling state is the server-owned `hemomancy:cling` attachment with `ClingRules` shared by Venous Cling and the Holdfast morphling.
- Blood left in the world is counted only through `common/world/answer/WorldAnswer` into `BloodSpilledLedger` (waste, never work); meals are rows in `data/<ns>/blood_food/` paid by `DailyLoopEvents.eat`; ambient world-answer spawns pass `WorldAnswer.scavengersAwake`.
```

### Under "Workflows that matter"

```text
- A GameTest stays deterministic: it builds inside its own template (or the all-air `hemomancy:open_room`), gives server-wide state its own batch, never reads light the tick it placed blocks, seeds its rolls (`RandomSource.create(seed)`, `player.getRandom().setSeed`), counts only its own entities, waits on conditions (`thenWaitUntil`) rather than fixed delays, and never reads in one `onEachTick` task what another sets the same tick (they run in hash order; read inside the sequence). Helper coordinates count from one block below the template.
- A review client (`*ClientReview` under `src/gameTest`, a row in `gradle/review-clients.gradle`, user name 16 characters or fewer) takes stills, not a play session: once a new `"kind":"startup"` line appears in `<dir>/operator/journal.jsonl`, drop one `{"op":"create","name":"<user>","seed":"1"}` (or `{"op":"open","name":"<user>"}` on a reused dir) into `<dir>/operator/inbox/`; it places its scene, writes `<dir>/screenshots`, logs `<NAME>_REVIEW captured N` and stops the client. Only one Gradle process at a time, review clients included. `docs/CORE_LOOP_PLAYTEST.md` lists the core-loop ones.
```

### Under "Project-specific patterns"

```text
- A player-facing system's Liber page is a literal `entry("...")` constant registered in `LiberEntryDefinitions` with a Degree floor (`LiberKnowledgeHelper.unlockForDegree`, or a purity or clarity stage in `unlockForUnstained`) and earlier openers in `LiberDiscoveryEvents`; an unregistered page is hidden from everyone by `MemoBookFilter`, and a new memory must be named on its school's Manipulations page (`LiberPageRegistrationGuardrailTest`). A core-loop first is an `impossible`-trigger advancement in the "Core loop firsts" block of `HarbingerAdvancementGranter`, granted by one `grantIfNotDone` where the act happens, after payment; a new one bumps the count of 27 (`CoreLoopTeachingGuardrailTest`).
- A core-loop lesson an NPC teaches is one row of `common/entity/npc/dialogue/CoreLoopLessons`, shown as unread lore cards by `CoreLoopLessonDialogue.append` and taken on the first line of the speaker's own `<Npc>DialogueEvents` by `route/CoreLoopLessonRoutes.teach` (never a new subscriber or route). A lesson opens its Liber pages and nothing else; its ids use `craft` and never a Quest Work word; every key needs a brief, and a number said aloud must change with the code (`CoreLoopLessonsTest`, `CoreLoopLessonGameTests`).
```

### Under "Documentation contract"

```text
- The core loop's three rules hold everywhere: two verbs (rites, Saints and Wills claim first, then a Shaping, then the plain verb), living blood (no blood battery, no blood as power, nothing keeps blood fresh without its keeper), and Temper never refuses. A core-loop change updates `docs/CORE_LOOP.md`, the dated sections of `docs/HEMOMANCY_REFERENCE.md`, the ledger `docs/CORE_LOOP_TEACHING_LEDGER.md` and its build log together.
```

## Part C: considered and left out

- **A line naming `PROTOCOL_VERSION`'s value.** `AGENTS.md` names none today, and the number moves with every payload change; the rule to bump it is already there.
- **A replacement for the old name of the eight kinds.** `AGENTS.md` on disk already says tempers and temper everywhere (a case-insensitive search for the retired word finds nothing), so there is nothing to replace.
- **One line per generator.** `tools/model_export/` is already listed as a version-controlled directory, which covers `fungal_strains.py`, `ground_network.py` and `living_land_sprites.py`.
- **The Living Staff ring and cleat looks, the stain variants and the refusal mark.** These are visual choices with their own review clients, not rules a contributor could break without a test catching it.
- **Per-package numbers.** Numbers belong in the code and in the closing table of `docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md`, not in the project instructions.
- **Round one's separate PlayerBlockEdits and pose lines** (`docs/archive/tracks/core_loop/BUILD_LOG.md`, "Lines for AGENTS.md"). They are folded into replacements A1 and A2 rather than added as new bullets; round one's other ten lines are in Part B, corrected for round two and the CL-2 ruling.
