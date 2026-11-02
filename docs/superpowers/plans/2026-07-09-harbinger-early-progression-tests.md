# Harbinger Early Progression Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add headless, repeatable coverage for the Harbinger’s activation, Degree 1, and first-bloodcraft progression loop without changing gameplay mechanics.

**Architecture:** Extend the development-only `HemoTestScenarioCatalog` with fixture-safe scenarios and expose each through the existing `HarbingerPilotGameTests` GameTest holder. Keep pure progression boundary checks in JUnit contracts; use GameTests only where a loaded server, player attachment, recipe registry, advancement, or block interaction is required.

**Tech Stack:** Java 21, NeoForge 21.1.219 GameTest, JUnit 5, Gradle.

## Global Constraints

- Work on the existing `neo-1.21.1` branch; do not create or switch worktrees/branches.
- Do not alter Harbinger gameplay behaviour; this tranche adds test coverage and development test fixtures only.
- Keep GameTest code under `src/gameTest` so it remains outside the release jar.
- Use NeoForge APIs and the existing `HemoCapabilityAccess` attachment access layer.
- Give every scenario a stable snake_case id, focused setup/verify/cleanup, and no shared mutable fixture state.
- Follow test-first development: add the GameTest method first, run it and observe its expected unknown-scenario failure, then add the smallest catalogue implementation.
- Do not commit because the user’s existing workspace already has unrelated uncommitted changes.

---

### Task 1: Activation and Degree-1 guard scenarios

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerPilotGameTests.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestScenarioCatalog.java`
- Modify: `docs/TESTING.md`

**Interfaces:**
- Consumes: `HemoTestScenarioCatalog.find(String)`, `runScenario(GameTestHelper, String)`, `HemoCapabilityAccess.requireInitiatoryDegree(ServerPlayer)`.
- Produces: scenarios named `harbinger_inactive_cannot_bloodcraft`, `blood_temple_activation`, `sanguine_initiation_valid`, and `sanguine_initiation_rejects_invalid_setup`.

- [ ] **Step 1: Add failing GameTest entry points**

```java
@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
public static void harbingerInactiveCannotBloodcraft(GameTestHelper helper) {
    runScenario(helper, "harbinger_inactive_cannot_bloodcraft");
}
```

Add the analogous entries for the other three exact ids.

- [ ] **Step 2: Run the GameTest server and observe RED**

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: the four new GameTests fail with `IllegalArgumentException: Unknown scenario <id>` while existing scenarios remain runnable.

- [ ] **Step 3: Add the minimal catalogue scenarios**

```java
new HemoTestScenario(
    "harbinger_inactive_cannot_bloodcraft",
    "An inactive player cannot satisfy a Harbinger blood-structure gate.",
    player -> prepareDegree(player, 0),
    player -> verifyRecipeGate(player, false),
    HemoTestScenarioCatalog::restoreDegree)
```

Use explicit player-state snapshots for blood activation and degree. For initiation, exercise the existing rite completion seam if it can be invoked with a real registered recipe; otherwise add a narrow JUnit contract for the existing degree rite mapping and retain a GameTest that verifies the observable degree-1 gate and conduit registry availability. Do not manufacture a fake rite-completion API.

- [ ] **Step 4: Run the new tests GREEN**

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: all existing and new GameTests pass.

- [ ] **Step 5: Document the four scenarios**

Add their ids and one-sentence purposes to `docs/TESTING.md` under the scenario catalogue.

### Task 2: First Bloodcraft milestone and projection scenarios

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerPilotGameTests.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestScenarioCatalog.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/EarlyBloodcraftBoundaryTest.java`
- Modify: `docs/TESTING.md`

**Interfaces:**
- Consumes: `BloodVolumeEvents`, `HarbingerAdvancementGranter`, `SanguineFormationProjectionHandler`, loaded `BloodStructureRecipe` data.
- Produces: scenarios named `d1_vessel_milestone`, `blood_projection_sanguine_formation`, `d1_liber_projection`, and `d1_hematic_iron_projection`.

- [ ] **Step 1: Add the four failing GameTest entry points**

```java
@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
public static void d1VesselMilestone(GameTestHelper helper) {
    runScenario(helper, "d1_vessel_milestone");
}
```

Add the analogous entries for the other exact ids.

- [ ] **Step 2: Verify RED**

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: each new entry fails because its catalogue id is unknown.

- [ ] **Step 3: Add only deterministic assertions**

```java
private static HemoTestResult verifyVesselThreshold(ServerPlayer player) {
    return HarbingerAdvancementGranter.hasVesselFilled(player)
        ? HemoTestResult.pass("5000 ml vessel milestone recorded")
        : HemoTestResult.fail("5000 ml vessel milestone was not recorded");
}
```

Drive the real blood-volume and advancement systems where their public server seams permit it. Keep the generic-block projection collapse chance out of GameTests; assert its fixed cost/gate behaviour in the JUnit boundary contract. The projector-tag no-collapse path may be GameTested only if it can be made deterministic.

- [ ] **Step 4: Run the focused JUnit and GameTest checks GREEN**

Run: `./gradlew.bat test --tests "*EarlyBloodcraftBoundaryTest" --no-daemon`

Expected: all cases pass.

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: all GameTests pass.

- [ ] **Step 5: Document the four scenarios**

Add their ids and explicit boundaries to `docs/TESTING.md`.

### Task 3: Vicar return eligibility and reward idempotency

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerPilotGameTests.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestScenarioCatalog.java`
- Create or modify: focused JUnit contract beside `HarbingerAdvancementGranter` or the Vicar assignment helper.
- Modify: `docs/TESTING.md`

**Interfaces:**
- Consumes: `HarbingerAdvancementGranter.hasVesselFilled/isLiberSanguinumCrafted/hasIronInTheBlood` and the existing Vicar assignment/reward helper.
- Produces: `first_bloodcraft_return_locked`, `first_bloodcraft_return_ready`, and `first_bloodcraft_reward_once` scenarios.

- [ ] **Step 1: Add three failing GameTest entry points**

```java
@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
public static void firstBloodcraftReturnLocked(GameTestHelper helper) {
    runScenario(helper, "first_bloodcraft_return_locked");
}
```

Add analogous methods for `first_bloodcraft_return_ready` and `first_bloodcraft_reward_once`.

- [ ] **Step 2: Verify RED**

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: these three tests report their ids as unknown.

- [ ] **Step 3: Implement fixture snapshots and predicates**

```java
private static HemoTestResult verifyFirstBloodcraftReady(ServerPlayer player) {
    return ExistingVicarAssignmentHelper.canCompleteFirstBloodcraft(player)
        ? HemoTestResult.pass("First Bloodcraft return is available")
        : HemoTestResult.fail("First Bloodcraft return is unexpectedly unavailable");
}
```

Use the actual existing helper name discovered during implementation. Snapshot and restore only the advancement/reward keys owned by the scenario. A repeated reward attempt must leave the reward claim/key state unchanged and must not add a second item.

- [ ] **Step 4: Run GREEN**

Run: `./gradlew.bat test --no-daemon`

Expected: all JVM tests pass.

Run: `./gradlew.bat runGameTestServer --no-daemon`

Expected: all GameTests pass.

- [ ] **Step 5: Update test documentation**

Group the early Harbinger scenarios by activation, Degree 1, Bloodcraft, and Vicar return flow.

### Task 4: Full alpha verification

**Files:**
- Verify only.

- [ ] **Step 1: Inspect the final diff**

Run: `git diff --check`

Expected: exit code 0 and no whitespace errors.

- [ ] **Step 2: Run the complete progression test gate**

Run: `./gradlew.bat alphaCheck --no-daemon`

Expected: JUnit and every registered GameTest pass.

- [ ] **Step 3: Confirm release-jar separation**

Run: `./gradlew.bat jar --no-daemon`

Expected: exit code 0. Inspect the jar to confirm no `gametest` test classes are packaged.
