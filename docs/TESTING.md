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

## Manual Harbinger journey

Launch the isolated journey client from the project root:

```powershell
./gradlew.bat runAlphaJourneyClient
```

This profile stores its world and client settings under `run-alpha-journey`, separate from the normal development client. On the first launch, select **Singleplayer**, create or open a world, and enter that world before running commands. A new isolated directory has no world to open until you create one.

Run the checkpoint journey as an operator:

```text
/hemo test journey start
/hemo test journey status
/hemo test journey next
/hemo test journey reset
/hemo test clear
```

`journey start` captures the player's pre-journey state, resets the player to the starting conditions, and prepares the first checkpoint. At each checkpoint, perform the requested gameplay action, use `journey status` to inspect the current requirements, then use `journey next` to verify them and advance. The current journey continues through the Rite of the Votary and First Separation: accept the Alchemist briefing; load the supplied Glass Bottle and Copper Ingot into the two empty Iron Braziers; light both with Blood Projection; craft and place the Vial Centrifuge; press Start on the supplied balanced machine; wait for its normal spin; take the Vivacious Enzyme; then claim the Living Syringe and initialized eight-vial rack. If verification fails, remain at that checkpoint, correct the unmet requirement, and run `journey next` again. After all checkpoints pass and the journey reports `complete`, run `journey next` once more to remove the fixtures and restore the captured state automatically.

`journey reset` removes journey-owned fixture output, clears active potion effects acquired during the run (including Blood Drunkenness), and restarts at the first checkpoint while retaining the original snapshot. `/hemo test clear` exits the journey, removes its fixtures, and restores the snapshot captured by `journey start`. Run it before returning to other manual testing. `alphaCheck` remains the automated JVM and dedicated GameTest gate; this isolated client workflow is the operator-driven complement, not a replacement.

The controller stores the fixture dimension with its origin. Invoking `next`, `reset`, or `clear` after traveling to another dimension still operates on the original fixture level; stage transitions and resets return the player there safely. `status` inspects that stored level without moving the player. If the saved dimension is unavailable, the command reports its exact resource key instead of touching the current dimension.

## In-game scenario commands

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
- `artificer_assignment_ready` — a complete Hematic Iron set makes the Worn Vow fitting available.
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

## Unstained progression smoke test

1. Begin purification and talk to an Acolyte, Zealot, and Guardian as their Observances become available. Verify the ledger groups all nine assignments under the correct directing office and the Book of Observances is only granted once and survives relog/death.
2. Reopen the matching NPC dialogue with each required offering. Verify it is consumed once, the reward is granted once, and the journal marks the assignment complete.
3. At Clarity 49, verify Glass Lungs is blocked; at 50 it may start and yields a Lethean Chalice. At Clarity 74, verify Moon-Washed Copper is blocked; at 75 it yields a Pale Silver Bell.
4. Verify a Still Art cannot be learned below its declared Clarity stage, including through a rite reward, then verify stage advancement backfills it.
5. Place a Stillwater Condenser beside source water and within four blocks of Ghost Pipe. Below 50 Purity its menu must remain locked. At 50, open its two-slot screen, insert glass bottles, and verify the water/Ghost Pipe indicators and progress channel produce Lethean Dew. A Verdigris Lattice within five blocks must light the lattice indicator, halve processing time, and double output.
6. Stand near a Verdigris Lattice as an Unstained player and verify Resistance. Spawn a tagged Hemomancy creature and verify Weakness and Slowness after a random tick.
7. Confirm no active recipe, dialogue inquiry, or registered item references `pale_silver_pickaxe` or `verdigris_censer`.
