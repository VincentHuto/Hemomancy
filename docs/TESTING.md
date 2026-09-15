# Hemomancy testing

Hemomancy now has two complementary test layers. JVM tests catch fast logic and resource regressions; NeoForge GameTests load the real server registries and exercise player-facing progression fixtures. The GameTest code lives in `src/gameTest`, is available in development runs, and is not included in the release jar.

## Everyday commands

From the project root on Windows:

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat alphaCheck
```

- `test` runs the native JUnit tests and adapts every legacy `public static void main` contract into an individually reported JUnit dynamic test.
- `runGameTestServer` starts a headless NeoForge server, runs the registered progression scenarios, and exits non-zero if a required scenario fails.
- `alphaCheck` runs both layers in order. Use this before an alpha build or whenever progression, crafting, quests, or rewards change.

## Living weapons

```powershell
./gradlew.bat -I tools/living_weapon_validation.init.gradle test --tests '*Living*' --tests '*LuxUmbra*' --tests '*Gloam*' --tests '*Thermal*' runLivingWeaponGameTest compileJava processResources
```

The isolated server uses `build/living-weapons-gametest`. Its weapon scenarios cover saved spear charge, failed and uncooled hits, primary and nearby burst damage, ally exclusion, axe swing duration and pool timing, and Blood Bolt impact scaling and bounded chaining. JVM checks cover the hand curves, three-cut geometry, spherical Lux extent, and torch fan motion and visibility.

For visual acceptance, inspect partial and full spear charge in hand, inventory, on the ground, and during the Living Staff morph. Check the axe swing in both hands and from another player, three separate Umbral cuts, and the torch's full fan from both ends. With paired claws, check alternating arms on clicks and held-attack repeats in first person, third person, and from another player, including the left-handed setting. An unrelated offhand item should keep its normal behavior. Shader compilation and server tests do not establish those views.

## Unstained weapon animations

Run `./gradlew.bat -I tools/living_weapon_validation.init.gradle test --tests '*UnstainedWeapon*' --tests '*LivingAxePlayerPose*' runLivingWeaponGameTest` for pose curves and synchronized swing durations. The dagger should start with its blade pointing down and lift its tip upward and outward ahead of the hilt, the glaive should cross a broad horizontal fan, and the hammer should visibly lift its head before the downstroke while retaining the Living Axe's timing and third-person pose. Check first person and third person, both handedness settings, offhand swings, Annetta's dagger, and return to the resting grip. Confirm the existing hit effects and weapon mechanics during combat.

## Phlegethontic Nether

```powershell
./gradlew.bat test --tests '*Phlegethontic*' compileGameTestJava
./gradlew.bat runGameTestServer
./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=42 -PphlegethonticRun=fresh-unique-name
./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=42 -PphlegethonticRun=reverse-unique-name -PphlegethonticOrder=reverse
```

Use a new run name for every fresh-world measurement. The validation server uses `build/phlegethontic-worlds/<seed>-<run>` and writes `phlegethontic-validation.json` there. It sets the actual world seed, adds a second TerraBlender Nether region and foreign-namespace Nether biome, generates 576 chunks, audits all feature writes, examines an inner 100-chunk sample, and advances 120 ticks with fluid ticking enabled. Its feature timings combine the queue, Basin materialization, and deferred vein completion per generated chunk; they exclude ore-reservation interception overhead and are not whole-generator overhead benchmarks.

`tools/model_export/compare_phlegethontic_worlds.py` compares region-file block states in two matching runs. It reports river/vein differences separately from high cavern-surface scab changed by vanilla lava and magma decoration. It requires `nbtlib`. The [validation report](phlegethontic-nether-worldgen/VALIDATION.md) records tested seeds, measured targets, and visual/multiplayer evidence.

`PhlegethonticCavernTest` checks domed overhead clearance, tapering before a protected boundary, and continuous overlapping vaults across negative chunk coordinates. For visual regression, compare fresh worlds at the same seed and camera position; loading an old save does not rebuild its roof.

`PhlegethonticGameTests` is explicitly registered by `DevTestHooks`. It covers real codecs and modifier injection, thermal immunity and Blood Loss, multi-block contact cadence, ticking fluid containment with foreign blocks, full-size spawning, home return, clotting, bucket rejection, tether exclusion/escape/collision/lifetime, piercing projectiles, and persistence. The positive control deliberately spills uncontained ichor to prove that the containment tests advance real fluid ticks. Test entities are removed after the batch.

The three existing combat-targeting cases that spawn beyond their original one-block template now use `combat_targeting_room`. Keeping their complete target footprint in loaded chunks avoids generation-grid-dependent entity-query failures.

The isolated `runPhlegethonticReviewClient` opens `build/phlegethontic-client/saves/Phlegethontic Final Review`; `runPhlegethonticObserverClient` joins `localhost:25566` from a separate game directory. These profiles and the `review.json` driver belong to development only. The driver can send commands to its integrated server, capture through Minecraft's screenshot API, hold/release Sneak, and write server player/tether status. Never point these disposable review profiles at a normal save. Compile before launching the clients and avoid rebuilding dependency JARs while either is running.

### Escharian Scyphus colonies

Run `./gradlew.bat test --tests '*Escharian*' compileGameTestJava` for the five authored count models and rotations, deterministic count variation, patch budgets, contour connectivity, attachment, 50–70% patch coverage, complete dark-face pile coverage, solid tapered pile dimensions, ground-only rims, surface-following infested foundations and stone transitions, obstruction rejection, legacy configuration, and anchor preference checks. The runtime acceptance test stacks Scyphus from one through five on all six faces in dry and waterlogged states, checks count-aware shapes and drops, rejects a sixth placement, and repeats support-loss and white-rim checks.

Run `./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=20260912 -PphlegethonticRun=UNIQUE` using a new run name. The disposable server tests actual BlockItem placement on all six faces, dry and waterlogged, selection thickness, rotation/mirror, empty-hand loot, replanting, support-loss drops, and white-rim placement rejection. The fresh Nether report scans 576 chunks for plant directions, backing validity, ground rims, and sample ichor proximity. It requires wall colonies, ceiling colonies, and ground piles for this seed. Every generated colony must have infested and regular venous stone nearby, and every ground-pile base must rest on infested venous stone. The original 100-chunk terrain/fluid checks remain.

In the disposable review client, first enter spectator mode and teleport to `(1010,206,1000)` in the Nether. Wait for the area to load; the item-entity drop checks require loaded chunks. Then write `{"sequence":1500,"operation":"escharian"}` to `build/phlegethontic-client/review.json`. This also runs the attachment/harvest checks with the real player, then places six orientation fixtures at X 1000–1020, Y 202, Z 1000 and four production-layout ground piles at X 1000, 1018, 1036, and 1054, Y 200, Z 1020. Inspect from above and below; the cup base must touch the support and its opening face away. Inspect natural colonies against uneven walls and ceilings using coordinates from the validation report. Capture screenshots through the driver's `screenshot` operation. Existing chunks cannot demonstrate the replacement generator.

## Automatic Harbinger, Unstained, and Circus journeys

Launch the isolated client below, create or open a disposable world, grant yourself operator permission, and run one of these development-only commands:

```text
/hemo test journey harbinger run
/hemo test journey unstained run
/hemo test journey unstained cure run
/hemo test journey unstained novitiate run
/hemo test journey circus run
/hemo test journey circus succession run
/hemo test journey circus liberation run
/hemo test journey run_all
```

`unstained run` is the cure alias. `circus run` executes Succession and Liberation using separate pavilion fixtures. `run_all` executes Harbinger, Unstained cure, Unstained novitiate, Circus Succession, then Circus Liberation. A server-tick runner prepares each existing fixture, invokes the same server gameplay hooks exercised by the fixture GameTests, and advances only after the authoritative journey check passes. It restores the captured player snapshot—including Circus progress, known summons, known manipulations, and inventory—and removes journey-owned blocks, entities, drops, and temporary world state after each successful route.

`/hemo test journey harbinger status` or `/hemo test journey unstained status` reports the route, checkpoint, automation state, and any latched failure. Reissuing the matching `run` resumes the current checkpoint. Exceptions and timeouts stop the runner without advancing; the snapshot and current fixture remain available for inspection. `/hemo test clear` cancels the runner, removes its fixtures, and restores the snapshot. Manual `start`, `next`, and `reset` commands stop automation before taking control.

This automation validates server-side progression, stations, rites, dialogue events, observances, assignments, travel, ceremonies, item use, and pickup hooks. It does not validate mouse/key handling, dialogue or HUD rendering, animations, particles, sound, or other client visuals. Use the manual journey commands for those checks.

## Manual Circus journeys

Use the isolated journey client and an operator account:

```text
/hemo test journey circus succession start
/hemo test journey circus liberation start
/hemo test journey circus status
/hemo test journey circus next
/hemo test journey circus reset
/hemo test clear
```

Each route builds a compact pavilion arena with the Ringmaster, carousel, and all four performers. The checkpoints cover generated-site discovery, all performer introductions, Attuned perception, the neutral attention act, explicit route choice, the remaining three Succession acts, both finale paths, carousel captives and anchors, Ringmaster completion, and route rewards. Fixtures accelerate passive acclimation and protect the operator with temporary resistance during automatic combat, but route choice, act startup, encounter phases, target rules, completion, and rewards still use their production server paths.

The discovery checkpoint deliberately requires a real generated pavilion in manual mode: use the Scarlet Waybill or `/locate structure hemomancy:circus_pavilion`, enter it, then return and advance. Automatic mode marks that already-covered discovery hook because a compact fixture is not a registered worldgen structure. At the Attuned and reward checkpoints, pause automation with the matching manual route if you need to judge overlays, models, animation, particles, sound, Crossbar radial behavior, or Thread Ripper aiming. Those are client observations and cannot honestly be passed by headless automation.

## Manual Harbinger journey

Launch the isolated journey client from the project root:

```powershell
./gradlew.bat runAlphaJourneyClient
```

This profile stores its world and client settings under `run-alpha-journey`, separate from the normal development client. On the first launch, select **Singleplayer**, create or open a world, and enter that world before running commands. A new isolated directory has no world to open until you create one.

Run the checkpoint journey as an operator:

```text
/hemo test journey harbinger start
/hemo test journey harbinger status
/hemo test journey harbinger next
/hemo test journey harbinger reset
/hemo test clear
```

`journey harbinger start` captures the player's pre-journey state, resets the player to the starting conditions, and prepares the first checkpoint. At each checkpoint, perform the requested gameplay action, use `journey harbinger status` to inspect the current requirements, then use `journey harbinger next` to verify them and advance. The route runs from Sanguine Initiation through Degree 8 Apotheos. It includes the First Remnant and Vicar report, every rank-up rite, First Separation, The Body Answers, Red Taxonomy, Living Bestiary, Hyphae discovery, Enzyme Mastery, First Culture, Woven Vessel, first Noetic mark recognition, all five Artificer assignments, the Vein-Mason D4-D6 route, Founding Fane, Living Covenant, and Qliphoth Communion. Fixtures supply the exact machine, ingredients, entities, or loadout needed for the next real gameplay trigger; they do not directly award the checkpoint outcome.

The Three Answers also includes its optional Barbed research correspondence. Capture the supplied Barbed Urchin, Desiccant, and Venom-Rib Centipede in separate hotbar jars, ask the marked Alchemist to record each filled jar, then claim the research reward. Weight of the Frame continues after the Archon rite through a real Monolithic Armature upgrade, Edacious inspection, Bloodburst activation, and fitting. After that, consume the nine supplied same-bloom Qliphoth pomes, choose the Eighth Degree in the opened fungal revelation, and invoke the prepared Apotheos grand station. Grand ceremonies are accelerated only after their real station match and activation succeeds.

After the Sanctified rite, run `journey harbinger next` once to enter the Chamber of Will through the real Degree-6 rite visit and again to return. Right-click the supplied Covenant Throne once, then advance to the Covenant Vigil. Invoke the prepared real Vigil with the Living Staff, run `journey harbinger next` to fill its owned anchors, enter inscription, assign the marked Vicar through the bloodline ally service, and fast-complete the ordeal, then run `journey harbinger next` once more to verify both the Vigil and Living Covenant milestones. This deliberately skips the 60-second combat ordeal while retaining station matching, rite activation, helper eligibility, completion rewards, and chapter closure. The dedicated GameTest server does not load the Chamber dimension, so its transition remains a live-client check; throne binding, Vigil activation/completion, helper rewards, and exact restoration of respawn and Chamber flags are automated. Any original bloodline, per-dimension Fane records, respawn binding, Chamber attunement flags, Muscle Memory state, recipe-book knowledge, Living Bestiary catalogue, Artificer persistent assignment keys, full initiatory-degree state, and base blood tendency are restored on reset/clear. If verification fails, remain at that checkpoint, correct the unmet requirement, and run `journey harbinger next` again. After all checkpoints pass and the journey reports `complete`, run `journey harbinger next` once more to remove the fixtures and restore the captured state automatically.

The route performs the real Living Staff blood-structure craft and all Artificer, Mnemonist, Alchemist, Vicar, and Vein-Mason progression used by the chosen Barbed/Edacious path. Discovery coverage includes a loaded blood-echo inscription, item-pickup discovery, dialogue, rite, degree, and advancement-backed Liber unlocks exercised naturally by the route. It does not enumerate all 30 authored inscriptions or alternate armor forks; those share the tested loaders and trigger paths. The current dedicated server suite registers 146 required GameTests. The remaining completion gate is the live-client smoke pass, especially dialogue screens, keybind-driven actions, the Chamber transition, rendering, and the full command-to-command operator flow.

`journey harbinger reset` removes journey-owned fixture output, clears active potion effects acquired during the run (including Blood Drunkenness), and restarts at the first checkpoint while retaining the original snapshot. `/hemo test clear` exits either journey, removes its fixtures, and restores the snapshot captured by its `start` command. Run it before returning to other manual testing. `alphaCheck` remains the automated JVM and dedicated GameTest gate; this isolated client workflow is the operator-driven complement, not a replacement.

The controller stores the fixture dimension with its origin. Invoking `next`, `reset`, or `clear` after traveling to another dimension still operates on the original fixture level; stage transitions and resets return the player there safely. `status` inspects that stored level without moving the player. If the saved dimension is unavailable, the command reports its exact resource key instead of touching the current dimension.

## Manual UNSTAINED journey

Use the same isolated client and an operator account:

```text
/hemo test journey unstained cure start
/hemo test journey unstained novitiate start
/hemo test journey unstained start
/hemo test journey unstained status
/hemo test journey unstained next
/hemo test journey unstained reset
/hemo test clear
```

The cure route proceeds from genuine blood suppression through degree-scaled Lethean Baptism, treatment Observances, full Purity, irreversible Closed Vein cleansing, Clarity preparation, and the Clarity Ascension pledge. The novitiate route exercises all five healthy service vows with the real Retort, Condenser, consecration, protective rite, Podium, and pledge actions. Both routes retain snapshot restoration; the original `journey unstained start` remains a cure-route alias. Post-pledge checkpoints continue through Glass Lungs, the Pale Vigil, Moon-Washed Copper, the Pale Watch, Resolute Still Arts, Enlightenment, and Lethean Font.

The fixtures shorten passive Purity and Clarity accumulation by positioning the player immediately before the next proof. They do not claim the proof: the player must still use the real Podium, complete the real rite, work the real Condenser, interact with the correct NPC dialogue, surrender the required offering, and receive the real milestone reward. Failed verification stays on the current checkpoint. A failed fixture transition latches the completed checkpoint so retrying `journey unstained next` does not consume or perform it twice.

## Field mycology smoke test

1. Find or place each Red Taxonomy plant and break it in Survival. Infected Fungus, Stinkhorn Fungus, Puffball Fungus, Sarcodes, Rafflesia, and Devil's Tooth must drop themselves. Bleeding Heart must drop one to three Bleeding Bulbs normally and the plant itself with Silk Touch. Repeat representative breaks with explosions and confirm the declared survival/decay behavior.
2. Break potted Ghost Pipe, Sarcodes, and Lethean Poppy. Each must return both the flower pot and its contained plant.
3. Submit a first unique Red Taxonomy specimen to the Alchemist with no sampling kit. Confirm exactly one empty Blood Vial is delivered, then submit a repeat and a second unique specimen and confirm neither duplicates that first-submission vial. Four unique specimens may still complete the optional catalogue.
4. Confirm the Alchemist and Assignment Ledger explain the Bleeding Bulb, Foul Paste, and Spore Sac lanes, including the Fungling sample to Infected Fungus route.
5. Craft Infested Wood from one log and one Foul Paste. Leave air above it at brightness 7 or lower and wait for random growth; only Infected Fungus, Hyphae, or Stinkhorn Fungus may appear. Occupy the block above and confirm the growth never replaces it.
6. Distill Devil's Tooth and confirm it produces two Foul Paste. Use JEI to follow Foul Paste into Infested Wood/Befouling Ash and Spore Sac into spores/Hyphal Substrate.

## In-game scenario commands

## Chamber progression and perspective checks

Use `/hemo chamber theme next [player]`, `previous`, and `cycle` to preview normal themes without player-eligibility filtering; use `set <theme>` to select any registered preview, including `vesper_fight` and `mycophant_nursery`, and `reset` to clear it. Use `/hemo chamber size set <radius> [player]` to override the player's accessible chamber radius from 3 through 10; `/hemo chamber size reset [player]` returns to progression sizing. The Orb of Perspective must never select either encounter theme.

For a live progression pass, enter the Chamber and move through tier radii 4, 6, 8, and 10. Confirm each newly unlocked band appears in the same server tick, the previous Sporitic Crystal corners move outward without replacing blocks substituted by the player, and placement, movement clamp, item rescue, safe return, and client border all follow the same radius. Repeat a progression change outside the Chamber, then enter and confirm heartbeat/login recovery builds the missing band.

Craft the Orb with `MEM / EBE / MEM` (Monolith Fragment, Echo Shard, Blood Crystal Shard). Throw it beyond the platform and below floor Y minus 3. Verify stable progression-filtered cycling, persistence after logout/reload, rejection outside the owner's cell and during both encounters, one activation per toss, inventory-first return, and a beside-owner return with normal pickup delay when inventory is full. Also test two separated player cells and logout immediately after throwing.

Development client/server runs add the following operator-only commands beneath the existing `/hemo` root:

```text
/hemo test list
/hemo test setup <scenario>
/hemo test verify <scenario>
/hemo test run <scenario>
/hemo test run_all
/hemo test status
/hemo test clear
```

`setup` prepares a fixture without immediately checking it, which leaves room to interact with a GUI, NPC, structure, or item manually. `verify` checks the prepared outcome. `run` performs setup and verification immediately for state-driven scenarios. `clear` removes the active fixture and only clears equipment/state owned by that fixture.

`run_all` clears any active fixture, then runs every registered scenario in catalogue order. Each scenario is cleaned up before the next begins. It continues after failures, prints each result, and ends with a passed/total summary plus the failed scenario ids.

The initial catalog is:

- `blood_structure_locked` — degree 5 must not satisfy the degree-6 Covenant Throne recipe.
- `blood_structure_unlocked` — degree 6 must satisfy the same recipe.
- `artificer_assignment_ready` — a briefed player wearing complete Hematic Iron makes the Worn Vow fitting available.
- `ArtificerProgressionGameTests` covers ordered D2 inspection, all three recorded fork reagents and real set responses, Blood Lust and Living Arsenal gameplay hooks, and all four D7 material/registered-ability routes against loaded registries.
- `artificer_reward_claimed` — repeated reward-claim marking remains idempotent.

- `uninitiated_cannot_pass_bloodcraft_degree_gate` — a Degree-0 player is rejected by the loaded degree-6 Covenant Throne bloodcraft gate.
- `sanguine_initiation_recipe_loaded` — the Sanguine Initiation recipe is available from the loaded server registry.
- `sanguine_initiation_degree_mapping` — Sanguine Initiation retains its Degree-1 rank-up mapping and registered Sanguine Conduit reward.

These are deliberately narrow pilots. They prove the harness through crafting locks, progression boundaries, assignment readiness, and reward claim state before more expensive end-to-end scenarios are added.

## Adding a scenario

1. Add one `HemoTestScenario` to `HemoTestScenarioCatalog` with a stable snake-case id, a focused setup action, one verification action, and cleanup limited to the fixture's own state.
2. Add a matching method to `HarbingerPilotGameTests` (or a new focused GameTest class) so it runs headlessly.
3. If manual interaction is useful, keep setup and verification separate so `/hemo test setup` can pause at the exact gameplay boundary under test.
4. Add or update a JVM contract for pure rules and resource shape. GameTests should cover integration that genuinely requires loaded registries, recipes, advancements, attachments, blocks, entities, or server ticks.
5. Run `./gradlew.bat alphaCheck`.

Avoid sharing mutable fixture state between tests. Prefer throwaway players and explicit cleanup, and assert observable outcomes rather than implementation details when the real gameplay API is available.

## Phlegethontic Bombardier ecology

1. Locate newly generated Phlegethontic Basin terrain and confirm Bombardiers spawn only with Escharian Overgrowth directly beneath them.
2. Approach a camouflaged specimen. Confirm it remains visible and outlineable, rises into its warning pose, and does not become angry or attack.
3. Strike one specimen. Confirm only Bombardiers bound to the same connected outcropping retaliate.
4. Check the flame from its abdominal nozzle at the sweep edge, behind the creature, and behind solid cover. Confirm the 30-tick wind-up, final eight-tick aim lock, four discrete pulses, and 16-tick sweep.
5. Observe cooling both on and away from Overgrowth. It should recharge in 120 ticks on habitat and 240 ticks elsewhere while trying to return to its outcropping.
6. Reload once during anger and once during cooling. Confirm anger persists and an interrupted wind-up or firing state resumes as cooling rather than duplicating an attack.
7. Snapshot the outcropping before firing, pathfinding, grazing, and cooling. Confirm no block state changes and no terrain ignition.
8. Compare the spray beside a Flammeus manipulation. Confirm the shared pinkish-red and black-tipped flame language, with no vanilla-orange fallback.
9. Inspect `latest.log` for missing entity models, textures, biome modifiers, loot tables, or client-only classloading errors.

The dedicated `PhlegethonticBombardierGameTests` batch covers colony isolation, wind-up safety, front/rear cone selection, cooling transition, and the no-mutation invariant.

## Unstained progression smoke test

1. Begin purification and talk to an Acolyte, Zealot, and Guardian as their Observances become available. Verify the ledger groups all nine assignments under the correct directing office and the Book of Observances is only granted once and survives relog/death.
2. Reopen the matching NPC dialogue with each required offering. Verify it is consumed once, the reward is granted once, and the journal marks the assignment complete.
3. At Clarity 49, verify Glass Lungs is blocked; at 50 it may start and yields a Lethean Chalice. At Clarity 74, verify Moon-Washed Copper is blocked; at 75 it yields a Pale Silver Bell.
4. Verify a Still Art cannot be learned below its declared Clarity stage, including through a rite reward, then verify stage advancement backfills it.
5. Place a Stillwater Condenser beside source water and within four blocks of Ghost Pipe. Below 50 Purity its menu must remain locked. At 50, open its two-slot screen, insert glass bottles, and verify the water/Ghost Pipe indicators and progress channel produce Lethean Dew. A Verdigris Lattice within five blocks must light the lattice indicator, halve processing time, and double output.
6. Stand near a Verdigris Lattice as an Unstained player and verify Resistance. Spawn a tagged Hemomancy creature and verify Weakness and Slowness after a random tick.
7. Confirm no active recipe, dialogue inquiry, or registered item references `pale_silver_pickaxe` or `verdigris_censer`.

## Cicatrix Anchorite D4-D6 smoke test

1. Complete the existing D4 scar lesson, lose an unlearned issued pattern, and verify replacement is offered once no matching template remains in inventory.
2. At D5, use a Thelemic Memory to enter Varicose and confirm physical damage and Noetic casting do not satisfy the milestone. Receive diagnosis, inspect exact health and routed-memory tooltips, then use Salve or Poultice and complete Hematic Fortification.
3. Claim the tier-two reward twice and verify only one reward is granted. Confirm a previously completed Fortification receives automatic credit.
4. At D6, receive the referral, obtain Mnemonist counsel, cast a non-mechanical Noetic Memory matching an active cerebral scar, commit a different Effigy set, and cast another matching Noetic Memory.
5. Relog with active scars and verify effective alignment is unchanged before and after the relog. Remove and re-equip scars and verify saved base alignment never drifts.
6. Inspect the separate collapsible D5 and D6 ledger cards and both return-ready toasts. Confirm the tier-three reward warns that Deep Inscription remains required.
