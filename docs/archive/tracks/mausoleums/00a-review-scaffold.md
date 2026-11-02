# Track 0 — Review scaffold

**Goal:** one command that puts a developer inside each Saint's complex and captures it, so every other track can prove its work in a client instead of in tests.

## Current state
- Templates generate only through natural placement (`saint_mausoleums` set, 40/12 spread). Finding one means flying.
- `structure_spawner` (WIP tab, `common/item/shared/StructureSpawnerItem`) spawns a structure by id at the click, but nothing scripts it.
- Review clients are rows in `gradle/review-clients.gradle`; they are driven after startup through `operator/inbox/open.json` (see the memory note on review clients) and write screenshots into the game dir.

## Target
- A `mausoleumReviewClient` row that starts a flat, superflat-style world, spawns the four complexes side by side with a known seed and rotation, and drops the player at the entrance of the one named in `-Pmausoleum=<saint>` (default: all four in a row, 200 blocks apart).
- A driver (`src/gameTest/java/.../MausoleumClientReview.java`, like `SuccessionClientReview`) that walks a fixed camera path: entrance, outer hall, threshold, each Chapel wing, the nave and door, the chamber. It saves one screenshot per stop under `build/mausoleum-review/<saint>/`.
- `/hemo test mausoleum <saint>` (a `HemoCommand` subcommand) that spawns a complex at the player's feet in any world, for ad-hoc checks.

## Tasks
1. Add the gradle row and the review-client driver. Spawn through `JigsawPlacement` the way `HarbingerOutpostAssemblyGameTests.mandatoryJigsawWingsAssemble` does, so a dropped jigsaw branch is a visible failure.
2. Add the `/hemo test mausoleum` subcommand (pin its syntax in `HemoCommandTreeTest` and the `hemo_command_validation` fingerprints).
3. Add a structure-validation GameTest that assembles each complex from its start pool with four seeds and asserts every mandatory piece (each Chapel wing, the nave, the chamber) placed. Track 3 keeps this green as it changes the chain.
4. Write the stop list for the driver in a small data file so theme tracks can add stops.

## Acceptance
- `./gradlew.bat mausoleumReviewClient -Pmausoleum=velorum` opens into Velorum's entrance and writes the screenshot set.
- The assembly GameTest fails when a Chapel pool is emptied on purpose.
