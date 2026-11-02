# Harbinger Alpha Journey Degree 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend the continuous Harbinger alpha journey through the Rite of the Votary, Degree 2, a real Vial Centrifuge separation, enzyme recovery, and a one-time Alchemist sampling-kit hand-in.

**Architecture:** Production gameplay receives one focused `FirstSeparationAssignmentHelper` plus hidden briefing/claim advancements and Alchemist dialogue events. Development-only journey code adds a Degree-2 chapter, recipe-faithful fixtures, a retained Vial Centrifuge transition, snapshot coverage, and observable server-authoritative verification. Existing `/hemo test journey` commands and the original pre-journey snapshot remain the public operator interface.

**Tech Stack:** Java 21, NeoForge 21.1.219 for Minecraft 1.21.1, NeoForge GameTest source set, JUnit/source-contract tests, Gradle `alphaCheck`.

## Global Constraints

- Work only on the current `neo-1.21.1` branch; do not create or switch branches or worktrees.
- Use `net.neoforged.*`, `DeferredHolder`, `Hemomancy.rloc(...)`, and `HemoCapabilityAccess` project patterns.
- Keep all journey controller, fixture, and GameTest classes under `src/gameTest`; release jars must contain none of them.
- Use singular 1.21 resource directories such as `data/hemomancy/advancement/` and `data/hemomancy/recipe/`.
- Preserve existing advancement IDs and existing-world progression; new briefing and claim IDs are additive.
- Follow red-green-refactor for every behavior change and run each stated failing test before implementation.
- Do not stage, commit, push, merge, or publish unless the user separately requests it.
- Preserve unrelated dirty-worktree changes.

## File Structure

- `FirstSeparationAssignmentHelper.java`: production eligibility, briefing, verified claim, and exact initialized reward construction.
- `HarbingerAlchemistDialogueTrees.java`: player-facing Degree-2 briefing and completion options only.
- `DialogueEventHandler.java`: delegates Alchemist events to the helper and safely delivers rewards.
- Hidden advancement JSON files: durable briefing and reward-claim records.
- `HemoJourneyStage.java`: ordered Degree-2 checkpoint identifiers.
- `HemoJourneySnapshot.java`: exact ownership of new journey advancements.
- `HemoJourneyFixtures.java`: recipe-faithful rite, marked Alchemist, centrifuge, deterministic samples, retained machine ownership, and cleanup.
- `HemoJourneyChecks.java`: server-authoritative Degree-2 outcomes.
- `HemoJourneyController.java`: continuous transitions, instructions, and retained-machine exception.
- `HarbingerJourneyFixtureGameTests.java`: semantic fixture and transition coverage.
- Focused JVM/source-contract tests: production assignment and development-only boundary contracts.

---

### Task 1: First Separation assignment state and exact reward

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentHelper.java`
- Create: `src/main/resources/data/hemomancy/advancement/hemomancy/first_separation_briefed.json`
- Create: `src/main/resources/data/hemomancy/advancement/hemomancy/first_separation_reward_claimed.json`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentSourceTest.java`

**Interfaces:**
- Consumes: `HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED`, `ADV_FIRST_SEPARATION_COMPLETE`, `HemoCapabilityAccess.getPlayerDegreeNumber(ServerPlayer)`.
- Produces: `ADV_BRIEFED`, `ADV_REWARD_CLAIMED`, `canBrief(ServerPlayer)`, `markBriefed(ServerPlayer)`, `isBriefed(ServerPlayer)`, `canClaim(ServerPlayer)`, `markClaimed(ServerPlayer)`, `isClaimed(ServerPlayer)`, and `rewardStacks()`.

- [ ] **Step 1: Write the failing source-contract test**

```java
public final class FirstSeparationAssignmentSourceTest {
    public static void main(String[] args) throws Exception {
        String helper = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentHelper.java"));
        assertContains(helper, "ADV_FIRST_SEPARATION_STARTED");
        assertContains(helper, "ADV_FIRST_SEPARATION_COMPLETE");
        assertContains(helper, "VialRackItem.ensureInitialized(rack)");
        assertContains(helper, "new ItemStack(ItemInit.living_syringe.get())");
        assertContains(helper, "new ItemStack(ItemInit.vial_rack.get())");
        assertContains(helper, "getPlayerDegreeNumber(player) >= 2");
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.common.mission.FirstSeparationAssignmentSourceTest --no-daemon`

Expected: FAIL because the helper and hidden advancement resources do not exist.

- [ ] **Step 3: Implement the helper and hidden advancements**

Use this public contract:

```java
public final class FirstSeparationAssignmentHelper {
    public static final ResourceLocation ADV_BRIEFED =
            Hemomancy.rloc("hemomancy/first_separation_briefed");
    public static final ResourceLocation ADV_REWARD_CLAIMED =
            Hemomancy.rloc("hemomancy/first_separation_reward_claimed");

    public static boolean canBrief(ServerPlayer player) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2 && !isBriefed(player);
    }

    public static boolean canClaim(ServerPlayer player) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2
                && isBriefed(player)
                && HarbingerAdvancementGranter.hasAdvancement(player,
                        HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED)
                && HarbingerAdvancementGranter.hasAdvancement(player,
                        HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE)
                && !isClaimed(player);
    }

    public static List<ItemStack> rewardStacks() {
        ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
        VialRackItem.ensureInitialized(rack);
        return List.of(new ItemStack(ItemInit.living_syringe.get()), rack);
    }
}
```

`markBriefed` and `markClaimed` must grant through `HarbingerAdvancementGranter.grantIfNotDone`, then return the verified `isBriefed`/`isClaimed` result. Both JSON advancements use `minecraft:impossible`, have no display object, and remain hidden.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.common.mission.FirstSeparationAssignmentSourceTest --no-daemon`

Expected: PASS.

---

### Task 2: Real Alchemist briefing and one-time hand-in

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentSourceTest.java`

**Interfaces:**
- Consumes: all `FirstSeparationAssignmentHelper` methods from Task 1.
- Produces: dialogue events `first_separation_brief` and `first_separation_claim`, dynamically visible Degree-2 options, exact fail-closed reward delivery.

- [ ] **Step 1: Extend the test with failing dialogue assertions**

```java
assertContains(alchemistTrees, "FirstSeparationAssignmentHelper.canBrief(player)");
assertContains(alchemistTrees, "FirstSeparationAssignmentHelper.canClaim(player)");
assertContains(alchemistTrees, "first_separation_brief");
assertContains(alchemistTrees, "first_separation_claim");
assertContains(handler, "FirstSeparationAssignmentHelper.markClaimed(player)");
assertOrder(handler, "markClaimed(player)", "rewardStacks()");
```

- [ ] **Step 2: Run the focused test and verify RED**

Run the Task-1 test command. Expected: FAIL on missing Alchemist integration.

- [ ] **Step 3: Implement dialogue options and event handling**

Add options to the player-aware Degree-2 Alchemist tree:

```java
if (FirstSeparationAssignmentHelper.canBrief(player)) {
    greetingOptions.add(new DialogueOption(
            "hemomancy.dialogue.alchemist.option.accept_first_separation",
            "first_separation_briefing", EVENT_FIRST_SEPARATION_BRIEF));
}
if (FirstSeparationAssignmentHelper.canClaim(player)) {
    greetingOptions.add(new DialogueOption(
            "hemomancy.dialogue.alchemist.option.complete_first_separation",
            "first_separation_complete", EVENT_FIRST_SEPARATION_CLAIM));
}
```

The handler must recheck `canBrief`/`canClaim`, call the verified mark method, and only then deliver copied reward stacks through inventory insertion with `player.drop(stack, false)` overflow. Forged, premature, or repeated events grant nothing.

- [ ] **Step 4: Add concise Alchemist copy**

Add translation keys for accepting the assignment, the paired-sample counterbalance lesson, returning after enzyme recovery, successful reward, and ineligible/repeated event feedback. Keep the Alchemist practical and observational rather than devotional.

- [ ] **Step 5: Run the focused test and verify GREEN**

Run the Task-1 test command. Expected: PASS.

---

### Task 3: Degree-2 stages and snapshot ownership

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyStage.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneySnapshot.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`

**Interfaces:**
- Produces ordered stages `VOTARY_RITE`, `DEGREE_2_REACHED`, `ALCHEMIST_BRIEFING`, `CENTRIFUGE_PREPARED`, `SEPARATION_STARTED`, `ENZYME_RECOVERED`, `ALCHEMIST_REWARD`, `COMPLETE`.
- Snapshot owns the five new advancement states defined in the design.

- [ ] **Step 1: Write failing ordered-stage and snapshot assertions**

Expected full order:

```java
List.of("mortal_display", "sanguine_initiation", "vessel_filled", "formation_projected",
        "liber_crafted", "hematic_iron_crafted", "vicar_reward", "votary_rite",
        "degree_2_reached", "alchemist_briefing", "centrifuge_prepared",
        "separation_started", "enzyme_recovered", "alchemist_reward", "complete")
```

Assert snapshot source contains `ADV_DEGREE_2_VOTARY`, `ADV_BRIEFED`, `ADV_FIRST_SEPARATION_STARTED`, `ADV_FIRST_SEPARATION_COMPLETE`, and `ADV_REWARD_CLAIMED` from the new helper.

- [ ] **Step 2: Run the source-contract test and verify RED**

Run: `./gradlew.bat test --tests com.vincenthuto.hemomancy.testing.GameTestHarnessSourceContractTest --no-daemon`

Expected: FAIL on old stage order and missing snapshot ownership.

- [ ] **Step 3: Add stages and exact advancement snapshot entries**

Extend the enum in the specified order. Add the five ResourceLocations to `JOURNEY_ADVANCEMENTS`; reuse the existing preflight, revoke/grant, rollback, and postcondition machinery rather than adding special cases.

- [ ] **Step 4: Run the source-contract test and verify GREEN**

Run the Step-2 command. Expected: PASS.

---

### Task 4: Recipe-faithful Votary Rite and marked Alchemist fixtures

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyChecks.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerJourneyFixtureGameTests.java`

**Interfaces:**
- Produces `prepareVotaryRite`, `spawnAlchemist`, degree/briefing verification, and action/target messages.

- [ ] **Step 1: Add failing GameTests for rite geometry and Alchemist ownership**

Assert the horizontal 5-by-5 layer at `origin.above()` equals:

```text
O E O E O
E . H . E
O H B H O
E . H . E
O E O E O
```

where `O` is Obsidian, `E` Engram Block, `H/B` Hematic Iron Block, and `.` air. Assert exactly one marked Alchemist is invulnerable and no-AI.

- [ ] **Step 2: Run `alphaCheck` and verify RED**

Run: `./gradlew.bat alphaCheck --no-daemon`

Expected: required GameTests fail because Degree-2 fixtures are absent.

- [ ] **Step 3: Implement fixture placement and checks**

Add the 5-by-5 positions to ownership planning, place the horizontal recipe exactly, ensure blood is active with at least 250 mL, and target the center block. Spawn `EntityInit.harbinger_alchemist` using the same marker/invulnerability/no-AI contract as the Vicar.

`VOTARY_RITE` verification requires degree exactly 2 and `ADV_DEGREE_2_VOTARY`. `DEGREE_2_REACHED` prepares the Alchemist fixture. `ALCHEMIST_BRIEFING` requires `FirstSeparationAssignmentHelper.isBriefed(player)`.

- [ ] **Step 4: Run `alphaCheck` and verify GREEN for these tests**

Expected: new rite and Alchemist tests pass; no prior GameTest regresses.

---

### Task 5: Deterministic retained Vial Centrifuge fixture

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java`
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyCentrifugeState.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerJourneyFixtureGameTests.java`

**Interfaces:**
- Produces `prepareCentrifuge`, `fixtureCentrifuge`, `isRetainedTransition`, deterministic Crimson Doe Blood Vials, and machine-baseline state.

- [ ] **Step 1: Add failing GameTests**

Tests must assert:

- The block entity is a `VialCentrifugeBlockEntity` and its position is journey-owned.
- Slots 2 and 6 contain sampled `blood_vial` stacks whose entity type is `EntityInit.crimson_doe`.
- `getResultFromVial(EntityInit.crimson_doe.get())` is a non-empty `vivacious_enzyme` stack.
- Opposing occupancy is balanced.
- The transition `SEPARATION_STARTED -> ENZYME_RECOVERED` retains the same block-entity position and does not call ordinary cleanup/prepare.

- [ ] **Step 2: Run `alphaCheck` and verify RED**

Expected: new centrifuge tests fail.

- [ ] **Step 3: Implement deterministic samples and retained state**

Create sampled vials by applying `DataComponents.CUSTOM_DATA` with:

```java
CompoundTag tag = new CompoundTag();
tag.putString(BloodVialItem.TAG_ENTITY_TYPE,
        BuiltInRegistries.ENTITY_TYPE.getKey(EntityInit.crimson_doe.get()).toString());
tag.putBoolean(BloodVialItem.TAG_STATE, true);
stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
```

Place two copies in slots 2 and 6. `HemoJourneyCentrifugeState` stores the owned position and baseline spin/start/completion state in player persistent data. The controller must skip cleanup and fixture rebuilding only for the start-to-recovery retained transition.

- [ ] **Step 4: Run `alphaCheck` and verify GREEN**

Expected: deterministic machine and retained-transition tests pass.

---

### Task 6: Start, recovery, and Alchemist reward verification

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyChecks.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerJourneyFixtureGameTests.java`

**Interfaces:**
- Consumes: machine state from Task 5 and helper from Task 1.
- Produces exact start/recovery/reward checks and cleanup attribution.

- [ ] **Step 1: Add failing semantic GameTests**

Cover:

```java
// Start requires a newly earned advancement and real machine progress.
assertTrue(!startPassed(false, true));
assertTrue(!startPassed(true, false));
assertTrue(startPassed(true, true));

// Recovery rejects baseline enzyme and accepts only positive inventory delta plus completion.
assertTrue(!recoveryPassed(1, 1, true));
assertTrue(!recoveryPassed(0, 1, false));
assertTrue(recoveryPassed(0, 1, true));
```

Add exact reward tests for all-inventory and split-overflow delivery. Inspect the rack with `VialRackItem.countEmptyVials(rack) == 8`; a blank uninitialized or partially filled rack must fail.

- [ ] **Step 2: Run `alphaCheck` and verify RED**

Expected: new verification tests fail.

- [ ] **Step 3: Implement checkpoint verification and messages**

`SEPARATION_STARTED` requires real `isSpinning()` plus newly complete `ADV_FIRST_SEPARATION_STARTED`. `ENZYME_RECOVERED` requires a positive Vivacious Enzyme inventory delta and complete `ADV_FIRST_SEPARATION_COMPLETE`. `ALCHEMIST_REWARD` requires all eligibility milestones and a newly verified claim plus exactly one Living Syringe and one eight-empty-vial rack across inventory and attributed drops.

Instructions must explicitly say: open the machine, press Start, wait for the spin, remove the enzyme, then speak to the marked Alchemist.

- [ ] **Step 4: Run `alphaCheck` and verify GREEN**

Expected: all required GameTests pass.

---

### Task 7: Reset, documentation, and release boundary

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneySnapshot.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`
- Modify: `docs/TESTING.md`
- Modify: `docs/HEMOMANCY_REFERENCE.md`

**Interfaces:**
- Produces complete reset/clear/restore behavior and operator documentation.

- [ ] **Step 1: Add failing reset and boundary assertions**

GameTests must prepare the centrifuge with contents, marked Alchemist, an attributable output, and a potion effect; reset must remove all of them and revoke the new journey advancements. Source-contract tests must assert the new stage order and retained-machine branch remain in `src/gameTest` only.

- [ ] **Step 2: Run `alphaCheck` and verify RED**

Expected: new reset assertions fail until cleanup covers machine state and new persistent keys.

- [ ] **Step 3: Implement cleanup and update documentation**

Remove owned machine contents with the block entity, clear Degree-2 persistent fixture keys, marked entities, owned outputs, and active effects. Document the full Degree-2 command sequence, 200-tick wait, enzyme pickup, Alchemist return, exact reward, and reset behavior. Update the reference with the implemented one-time First Separation reward.

- [ ] **Step 4: Run the full acceptance gate**

Run: `./gradlew.bat alphaCheck --no-daemon`

Expected: JVM suite passes and every required GameTest passes.

- [ ] **Step 5: Build and inspect the release jar**

Run:

```powershell
./gradlew.bat jar --no-daemon
$jar = Get-ChildItem build/libs/hemomancy-*.jar | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$entries = & jar tf $jar.FullName | Select-String 'gametest|HemoJourney'
if ($entries) { $entries; exit 1 }
```

Expected: jar succeeds and prints no GameTest/journey entries.

- [ ] **Step 6: Check the final diff**

Run: `git diff --check`

Expected: exit code 0 with no whitespace errors. CRLF conversion warnings are informational.

## Plan Self-Review

- Spec coverage: every checkpoint, production hand-in, initialized reward, retained machine, snapshot expansion, cleanup, docs, and release boundary maps to Tasks 1-7.
- Placeholder scan: no TBD/TODO or unspecified implementation steps remain.
- Type consistency: helper, stage, fixture, state, and verifier names are consistent across tasks.
- Scope: optional Degree-2 assignments and Degree 3 remain explicitly excluded.
