# Harbinger Alpha Journey Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a real Degree-1 First Bloodcraft Vicar hand-in and a development-only connected-player journey that prepares and verifies every early Harbinger checkpoint from Mortal Display activation through reward claim.

**Architecture:** Main gameplay code owns an idempotent `FirstBloodcraftAssignmentHelper`, advancement, Vicar dialogue option, and reward handler. Development-only `src/gameTest` code owns `HemoJourneyStage`, player snapshots, fixture construction, checkpoint verification, and `/hemo test journey` commands. The normal client remains unchanged; a separate Gradle client run directory isolates the acceptance world.

**Tech Stack:** Java 21, NeoForge 21.1.219, Brigadier, Minecraft advancements/attachments, NeoForge GameTest, JUnit 5, Gradle ModDev.

## Global Constraints

- Work only on the existing branch; do not create a worktree, switch branches, commit, or push.
- Keep journey controller and fixture code under `src/gameTest`; release jars must not contain it.
- Use `HemoCapabilityAccess` for player attachments and NeoForge payload-era APIs.
- The reward is exactly 4 `hematic_iron_scrap`, 8 placeable `befouling_ash_trail` items, and 2 `sanguine_formation`.
- The hidden advancement `hemomancy:hemomancy/first_bloodcraft_reward_claimed` is the authoritative duplicate-claim guard.
- A hand-in requires `vessel_filled`, `liber_sanguinium`, and `iron_in_the_blood`.
- Checkpoint failure never advances the journey; fixture cleanup and snapshot restoration affect only journey-owned state.

---

### Task 1: First Bloodcraft eligibility and claim record

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/mission/FirstBloodcraftAssignmentHelper.java`
- Create: `src/main/resources/data/hemomancy/advancement/hemomancy/first_bloodcraft_reward_claimed.json`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/mission/FirstBloodcraftAssignmentSourceTest.java`

**Interfaces:**
- Produces: `ADV_REWARD_CLAIMED`, `canClaim(ServerPlayer)`, `isClaimed(ServerPlayer)`, `markClaimed(ServerPlayer)`, and `rewardStacks()`.

- [ ] **Step 1: Add a failing source/resource contract**

Assert that the helper references all three existing advancement predicates, defines quantities `4`, `8`, and `2`, uses `BlockInit.befouling_ash_trail.get().asItem()`, and that the advancement JSON is hidden with `minecraft:impossible`.

- [ ] **Step 2: Verify RED**

Run: `./gradlew.bat test --tests "*FirstBloodcraftAssignmentSourceTest" --no-daemon`

Expected: failure because the helper and advancement resource do not exist.

- [ ] **Step 3: Implement the helper and advancement**

`canClaim` must return true only when all three milestone predicates are true and `isClaimed` is false. `rewardStacks()` returns fresh stacks for Hematic Iron Scrap x4, Befouling Ash Trail x8, and Sanguine Formation x2. `markClaimed` delegates to `HarbingerAdvancementGranter.grantIfNotDone`.

- [ ] **Step 4: Verify GREEN**

Run the focused test command from Step 2 and expect success.

### Task 2: Vicar dialogue hand-in and player-facing documentation

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/mission/FirstBloodcraftAssignmentSourceTest.java`

**Interfaces:**
- Consumes: `FirstBloodcraftAssignmentHelper.canClaim`, `isClaimed`, `rewardStacks`, and `markClaimed`.
- Produces: `HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD`.

- [ ] **Step 1: Extend the failing contract**

Require the Degree-1 tree to expose the claim option, require `HarbingerVicarEntity` to pass claim readiness into the tree, and require `DialogueEventHandler` to re-check eligibility before granting every stack and marking the claim.

- [ ] **Step 2: Verify RED**

Run the focused source test and expect failure at the missing dialogue/event assertions.

- [ ] **Step 3: Wire the dialogue**

Add a Degree-1 greeting option with event id `vicar_claim_first_bloodcraft_reward` only when ready. Add claimed and unready response nodes/messages. Route the event in `DialogueEventHandler`, call `giveOrDropAtEntity` once for each fresh reward stack, then mark the advancement. A forged or repeated event must grant nothing.

- [ ] **Step 4: Add language and mechanics documentation**

Document the three prerequisites, exact kit, one-time claim behavior, and advancement key.

- [ ] **Step 5: Verify GREEN and compile main code**

Run: `./gradlew.bat test --tests "*FirstBloodcraftAssignmentSourceTest" --no-daemon`

Run: `./gradlew.bat compileJava --no-daemon`

Expected: both succeed.

### Task 3: Journey stage model and command surface

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyStage.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyResult.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestCommands.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`

**Interfaces:**
- Produces: stages `MORTAL_DISPLAY`, `SANGUINE_INITIATION`, `VESSEL_FILLED`, `FORMATION_PROJECTED`, `LIBER_CRAFTED`, `HEMATIC_IRON_CRAFTED`, `VICAR_REWARD`, and `COMPLETE`; controller methods `start`, `next`, `status`, `reset`, `clear`.

- [ ] **Step 1: Add failing command/stage source assertions**

Require `literal("journey")` with `start`, `next`, `status`, and `reset`, the exact ordered stage ids, and delegation to `HemoJourneyController`.

- [ ] **Step 2: Verify RED**

Run the harness source contract and expect failure because the journey classes and commands are absent.

- [ ] **Step 3: Add the stage/result types and command delegation**

The controller initially returns explicit not-yet-prepared failures while exposing the complete command surface. Modify `/hemo test clear` to call journey cleanup before ordinary scenario cleanup.

- [ ] **Step 4: Verify GREEN and GameTest compilation**

Run the focused contract, then `./gradlew.bat compileGameTestJava --no-daemon`; expect both to succeed.

### Task 4: Snapshot, advancement reset, and restoration

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneySnapshot.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`

**Interfaces:**
- Produces: `capture(ServerPlayer)`, `resetForJourney(ServerPlayer)`, `restore(ServerPlayer)`, and exact persistent keys beneath `hemomancy.dev_test.journey.*`.

- [ ] **Step 1: Add failing snapshot-ownership assertions**

Require snapshots for blood active/current/max, degree, every inventory slot via `ItemStack.saveOptional`, all seven relevant advancements including the claim advancement, origin/dimension, and current stage.

- [ ] **Step 2: Verify RED**

Run the harness contract and expect the missing snapshot assertions to fail.

- [ ] **Step 3: Implement capture/reset/restore**

Reset advancements by revoking every completed criterion from their real `AdvancementProgress`; restore originally-complete advancements through `grantIfNotDone`. Restore every inventory slot using `ItemStack.parse(registryAccess, tag)`. Refuse to overwrite an existing snapshot.

- [ ] **Step 4: Verify GREEN and compile**

Run the focused contract and `compileGameTestJava`; expect success.

### Task 5: Controlled fixture builder and checkpoint verification

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyChecks.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerJourneyFixtureGameTests.java`

**Interfaces:**
- Produces: `prepare(ServerPlayer, HemoJourneyStage, BlockPos)`, `cleanup(ServerPlayer, BlockPos)`, and `verify(ServerPlayer, HemoJourneyStage, BlockPos)`.

- [ ] **Step 1: Add failing fixture GameTests**

Add deterministic tests for the Mortal Display platform, Sanguine Initiation 3x3 pattern, attuned projector target, Liber 3x3 pattern, Hematic Iron 3x3 pattern, and marked Vicar spawn.

- [ ] **Step 2: Verify RED**

Run `runGameTestServer`; expect fixture tests to fail because preparation is absent.

- [ ] **Step 3: Implement deterministic fixtures**

Build a stone platform at the stored origin. Place exact registered blocks from current recipe JSON: initiation uses Engram/Stone Bricks/Hematic Iron; Liber uses Befouling Ash Trail around a Bookshelf; Hematic Iron uses Befouling Ash Trail around an Iron Block. Grant only the catalyst/interaction items needed by the active stage. Spawn one invulnerable, no-AI Vicar tagged `hemomancy.dev_test.journey`.

- [ ] **Step 4: Implement observable checks and transitions**

Each check returns every unmet condition. `next` advances exactly one stage only when all conditions pass, prepares the next fixture, teleports the player, and sends the action message. `COMPLETE` restores the snapshot and cleans the fixture.

- [ ] **Step 5: Verify GREEN**

Run `runGameTestServer`; expect all existing and new required tests to pass.

### Task 6: Isolated client run and operator guide

**Files:**
- Modify: `build.gradle`
- Modify: `docs/TESTING.md`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`

- [ ] **Step 1: Add failing run-profile/doc assertions**

Require an `alphaJourneyClient` NeoForge client run with `gameDirectory = project.file('run-alpha-journey')`, plus all journey commands and checkpoint instructions in `docs/TESTING.md`.

- [ ] **Step 2: Verify RED**

Run the harness source contract and expect failure at the missing run profile/documentation assertions.

- [ ] **Step 3: Add the run profile and guide**

The profile uses the isolated directory and the same client runtime classpath as the normal client. Document first launch/world creation, `/hemo test journey start`, `next`, `status`, `reset`, and `/hemo test clear` restoration.

- [ ] **Step 4: Verify GREEN**

Run the focused contract and `compileGameTestJava`; expect success.

### Task 7: Full verification

- [ ] Run `./gradlew.bat alphaCheck --no-daemon`; expect all JVM and GameTests to pass.
- [ ] Run `./gradlew.bat jar --no-daemon`; expect success and confirm no `gametest/journey` classes occur in the release jar.
- [ ] Run `git diff --check`; expect no whitespace errors.
- [ ] Manually launch `alphaJourneyClient`, create/open a development world, and execute the journey from `start` through restored `COMPLETE`; record any connected-client-only issue instead of claiming manual acceptance without evidence.
