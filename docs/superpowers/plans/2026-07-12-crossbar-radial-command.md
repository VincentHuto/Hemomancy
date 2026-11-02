# Crossbar Radial Command Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add persistent Crossbar command modes and a Degree 5 two-ring radial that preserves tap call/recall and performs atomic field hot-swaps.

**Architecture:** Keep authoritative state and transactions in focused common-side helpers, with the Crossbar item acting as persisted storage and entry point. A dedicated client screen reuses `GenericRadialMenu`, while one serverbound payload validates exact equipped-Crossbar identity before delegating mode changes or hot-swaps. Bound AI reads the equipped Crossbar's mode each tick, preserving attack focus as a temporary override.

**Tech Stack:** Java 21, NeoForge 1.21.1 custom payloads, Minecraft `CUSTOM_DATA`, JUnit 5 source/unit tests, NeoForge GameTests, Gradle.

## Global Constraints

- Tap release before 10 ticks retains immediate call/recall; holding for 10 ticks opens the radial and suppresses the tap.
- Sneak-right-click field cycling is removed; the Spindle owns shape selection until Skein Transposition is learned.
- The inner ring is available to an owner-attuned Crossbar; the outer ring requires one-rank Degree 5 Skein Transposition with Far Tether as prerequisite.
- Mode changes are free and exact-Crossbar scoped; hot-swaps use normal adjusted call charge and the normal summon cooldown.
- Hot-swap failures preserve charge, prepared shape, and every existing body.
- Canonical docs are authoritative and wiki wording must be derived from them.
- Preserve unrelated dirty-worktree changes; stage only files belonging to the current task.

---

## File Structure

- Create `common/entity/summon/puppeteer/PuppeteerCommandMode.java`: stable serialized mode enum and safe legacy parsing.
- Create `common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java`: authoritative mode mutation, guard anchors, validation, projected-cap calculation, and atomic swap orchestration.
- Create `common/network/summon/PacketCrossbarRadialAction.java`: discriminated command/shape request and server handler.
- Create `client/screen/summon/CrossbarRadialScreen.java`: text-only inner/outer rings and disabled reasons.
- Modify `MarionetteCrossbarItem.java`: state accessors, hold-duration item use, quick release, and removal of cycling.
- Modify `BoundSummonBehavior.java`: mode-aware target/navigation policy and focus override resumption.
- Modify `ClientEvents.java`: open the radial at the 10-tick threshold and prevent duplicate opening.
- Modify skill registry/helper/UI files: register and expose Skein Transposition.
- Modify localization, canonical references, and wiki: publish controls and progression consistently.
- Extend unit/source tests and `PuppeteeringGameTests.java`: transaction, AI, input wiring, persistence, and regression coverage.

### Task 1: Persistent Command Model and Pure Rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCommandMode.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCommandModeTest.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonRules.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonRulesTest.java`

**Interfaces:**
- Produces: `PuppeteerCommandMode { FOLLOW, GUARD, HUNT, PASSIVE }`, `serializedName()`, and `fromSerializedName(String): PuppeteerCommandMode` defaulting to `FOLLOW`.
- Produces: `PuppeteerSummonRules.projectedShapedCount(int currentShaped, int recalledMatching, int replacements): int`.

- [ ] **Step 1: Write failing enum and projection tests**

```java
@Test void unknownModeDefaultsToFollow() {
    assertEquals(PuppeteerCommandMode.FOLLOW, PuppeteerCommandMode.fromSerializedName("old_or_bad"));
}

@Test void projectedCountRemovesOnlyMatchingCohort() {
    assertEquals(3, PuppeteerSummonRules.projectedShapedCount(5, 3, 1));
}
```

- [ ] **Step 2: Run the focused tests and verify failure**

Run: `./gradlew test --tests '*PuppeteerCommandModeTest' --tests '*PuppeteerSummonRulesTest'`
Expected: compilation failure because the enum and projection method do not exist.

- [ ] **Step 3: Implement the stable enum and clamped projection rule**

```java
public enum PuppeteerCommandMode {
    FOLLOW("follow"), GUARD("guard"), HUNT("hunt"), PASSIVE("passive");
    // serializedName() returns the constructor value.
    // fromSerializedName performs a case-insensitive match and returns FOLLOW otherwise.
}

public static int projectedShapedCount(int currentShaped, int recalledMatching, int replacements) {
    return Math.max(0, currentShaped - Math.max(0, recalledMatching)) + Math.max(0, replacements);
}
```

- [ ] **Step 4: Run focused tests and verify pass**

Run: `./gradlew test --tests '*PuppeteerCommandModeTest' --tests '*PuppeteerSummonRulesTest'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the rule boundary**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCommandMode.java src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonRules.java src/test/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCommandModeTest.java src/test/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonRulesTest.java
git commit -m "feat: add puppeteer command model"
```

### Task 2: Crossbar Persistence and Legacy Defaults

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarStateTest.java`

**Interfaces:**
- Consumes: `PuppeteerCommandMode.fromSerializedName`.
- Produces: `getCommandMode(ItemStack)`, `setCommandMode(ItemStack, PuppeteerCommandMode)`, `getGuardPosition(ItemStack): Optional<BlockPos>`, `getGuardDimension(ItemStack): Optional<ResourceKey<Level>>`, `setGuardAnchor(ItemStack, BlockPos, ResourceKey<Level>)`, and `clearGuardAnchor(ItemStack)`.

- [ ] **Step 1: Write failing data round-trip and missing-data tests**

```java
@Test void freshAndLegacyCrossbarsDefaultToFollow() {
    assertEquals(PuppeteerCommandMode.FOLLOW, MarionetteCrossbarItem.getCommandMode(crossbar()));
}

@Test void guardStateRoundTripsThroughCustomData() {
    ItemStack stack = crossbar();
    MarionetteCrossbarItem.setCommandMode(stack, PuppeteerCommandMode.GUARD);
    MarionetteCrossbarItem.setGuardAnchor(stack, new BlockPos(4, 70, -3), Level.OVERWORLD);
    assertEquals(new BlockPos(4, 70, -3), MarionetteCrossbarItem.getGuardPosition(stack).orElseThrow());
    assertEquals(Level.OVERWORLD, MarionetteCrossbarItem.getGuardDimension(stack).orElseThrow());
}
```

- [ ] **Step 2: Run the state test and verify failure**

Run: `./gradlew test --tests '*MarionetteCrossbarStateTest'`
Expected: compilation failure for missing accessors.

- [ ] **Step 3: Add `command_mode`, `guard_pos`, and `guard_dimension` custom-data accessors**

Use `BlockPos.asLong()/of(long)` and `ResourceLocation.parse` plus `ResourceKey.create(Registries.DIMENSION, id)`. Never throw for malformed legacy data; return empty anchors and Follow.

- [ ] **Step 4: Run the state test and verify pass**

Run: `./gradlew test --tests '*MarionetteCrossbarStateTest'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit persisted Crossbar commands**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarStateTest.java
git commit -m "feat: persist crossbar command state"
```

### Task 3: Skein Transposition Progression

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/skills/SummonSkillBranch.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `src/generated/resources/assets/hemomancy/lang/en_us.json`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/init/skills/SkeinTranspositionSkillSourceTest.java`

**Interfaces:**
- Produces: `SkillPointInit.skill_skein_transposition` and `SkillPointHelper.getSkeinTranspositionLevel(Player): int`.

- [ ] **Step 1: Write a source contract test for one rank, Degree 5, and Far Tether prerequisite**

Assert the branch contains `skill_skein_transposition`, `setRequiredDegree(5)`, a max level of `1`, and `SkillPointInit.skill_far_tether`; assert the helper and both language files contain matching keys.

- [ ] **Step 2: Run the source test and verify failure**

Run: `./gradlew test --tests '*SkeinTranspositionSkillSourceTest'`
Expected: FAIL because the skill is absent.

- [ ] **Step 3: Register and display Skein Transposition**

Use the next unused stable skill ID confirmed from `SkillPointInit`, place it as a Degree 5 sibling of Thread Economy, give it Far Tether as parent, one rank, and a non-overlapping summon-tree coordinate. Add name/description text explaining field hot-swap rather than generic summon strength.

- [ ] **Step 4: Run the source test and skill-tree regression tests**

Run: `./gradlew test --tests '*SkeinTranspositionSkillSourceTest' --tests '*Skill*SourceTest'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the progression node**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/init src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree src/main/resources/assets/hemomancy/lang/en_us.json src/generated/resources/assets/hemomancy/lang/en_us.json src/test/java/com/vincenthuto/hemomancy/common/init/skills/SkeinTranspositionSkillSourceTest.java
git commit -m "feat: add skein transposition skill"
```

### Task 4: Authoritative Modes and Atomic Hot-Swap

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCrossbarCommandsSourceTest.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PuppeteeringGameTests.java`

**Interfaces:**
- Produces: `setMode(ServerPlayer, ItemStack, PuppeteerCommandMode): boolean` and `hotSwap(ServerPlayer, ItemStack, String): boolean`.
- Produces package-visible validation helpers returning an immutable result containing the old cohort, candidate entity, definition, cost, and projected counts.

- [ ] **Step 1: Add failing GameTests for exact-cohort success and rollback**

Create cases with two shapes on one Crossbar and one shape on another. A successful swap must discard only the old prepared cohort, retain the other bodies, spend adjusted cost, set the new prepared name, and start 20 ticks of item cooldown. Failure from low charge, cap, unknown shape, missing skill, foreign UUID, or failed insertion must preserve all prior state.

- [ ] **Step 2: Run Puppeteering GameTests and verify the new cases fail**

Run: `./gradlew runGameTestServer --tests hemomancy:puppeteering_*`
Expected: new command/hot-swap tests fail while existing puppeteering tests still execute.

- [ ] **Step 3: Extract call validation and implement the transaction coordinator**

Reuse `PuppeteerSummonFactory.create`, `summonThreadCost`, `activeSummonsForOwner`, `PuppeteerSummonRules.canRetainBody`, and exact Crossbar UUID matching. Validate and insert the candidate before recalling the cohort; if charge consumption fails, discard the candidate. Only after successful charge consumption discard the old cohort and set the selected name. Do not include claimed Wills in the cohort or shaped-count subtraction.

- [ ] **Step 4: Run unit and GameTest coverage**

Run: `./gradlew test --tests '*Puppeteer*'`
Run: `./gradlew runGameTestServer --tests hemomancy:puppeteering_*`
Expected: both commands succeed.

- [ ] **Step 5: Commit the server transaction**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java src/test/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCrossbarCommandsSourceTest.java src/gameTest/java/com/vincenthuto/hemomancy/gametest/PuppeteeringGameTests.java
git commit -m "feat: add atomic puppet hot swap"
```

### Task 5: Serverbound Radial Payload

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/summon/PacketCrossbarRadialAction.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/network/summon/CrossbarRadialPacketSourceTest.java`

**Interfaces:**
- Consumes: `PuppeteerCrossbarCommands.setMode/hotSwap`.
- Produces: payload fields `UUID crossbarId`, `Action action`, and `String value`, where `Action` is `SET_MODE` or `HOT_SWAP`.

- [ ] **Step 1: Write a failing packet source contract**

Assert a bounded UTF codec, UUID codec, serverbound registration, enqueue-on-server handling, `findEquippedCrossbar(player, crossbarId)`, and delegation only after exact equipped ownership validation.

- [ ] **Step 2: Run the packet test and verify failure**

Run: `./gradlew test --tests '*CrossbarRadialPacketSourceTest'`
Expected: FAIL because the payload is absent.

- [ ] **Step 3: Implement and register the discriminated payload**

Reject blank/oversized values, invalid mode names, unknown actions, unequipped UUIDs, and non-owned Crossbars without mutation. `SET_MODE` sends the parsed enum; `HOT_SWAP` sends the canonical definition name and relies on the command service for all current-state validation.

- [ ] **Step 4: Run packet and integration tests**

Run: `./gradlew test --tests '*CrossbarRadialPacketSourceTest' --tests '*PuppeteerIntegrationSourceTest'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit networking**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/network/summon/PacketCrossbarRadialAction.java src/main/java/com/vincenthuto/hemomancy/Hemomancy.java src/test/java/com/vincenthuto/hemomancy/common/network/summon/CrossbarRadialPacketSourceTest.java
git commit -m "feat: network crossbar radial actions"
```

### Task 6: Mode-Aware Bound AI

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BoundSummonBehavior.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCommandEvents.java`
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PuppeteeringGameTests.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/summon/PuppeteerCommandBehaviorSourceTest.java`

**Interfaces:**
- Consumes Crossbar mode/guard accessors and exact Crossbar identity.
- Produces mode-specific target acquisition and anchor navigation while retaining `focusTarget` as an override.

- [ ] **Step 1: Add failing behavior tests**

Cover Follow retaliation without free hunting, Guard hostile acquisition around the anchor, Hunt proactive `Enemy` acquisition, Passive clearing automatic targets, focus override in every mode, resumption after invalid focus, Guard fallback beyond tether/dimension, and identical behavior for a claimed Will.

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./gradlew test --tests '*PuppeteerCommandBehaviorSourceTest'`
Run: `./gradlew runGameTestServer --tests hemomancy:puppeteering_command_*`
Expected: new cases fail under unconditional hunting behavior.

- [ ] **Step 3: Implement mode policy without duplicating entity AI**

Refactor `commonServerTick` to resolve the exact equipped Crossbar once, validate/clear stale focus, and dispatch acquisition/navigation by mode. Store focus-target UUID transiently on the bound body so focus is distinguishable from automatic targets. Guard fallback calls `setCommandMode(crossbar, FOLLOW)` and clears the anchor once, affecting all exact-Crossbar bodies consistently.

- [ ] **Step 4: Run behavior and existing summon tests**

Run: `./gradlew test --tests '*Puppeteer*'`
Run: `./gradlew runGameTestServer --tests hemomancy:puppeteering_*`
Expected: `BUILD SUCCESSFUL` and all GameTests pass.

- [ ] **Step 5: Commit command AI**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BoundSummonBehavior.java src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCommandEvents.java src/gameTest/java/com/vincenthuto/hemomancy/gametest/PuppeteeringGameTests.java src/test/java/com/vincenthuto/hemomancy/common/entity/summon/PuppeteerCommandBehaviorSourceTest.java
git commit -m "feat: apply crossbar command modes"
```

### Task 7: Hybrid Hold Input and Two-Ring Screen

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreen.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Modify: `src/generated/resources/assets/hemomancy/lang/en_us.json`
- Create: `src/test/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreenSourceTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/CrossbarUseInputSourceTest.java`

**Interfaces:**
- Consumes the radial payload and Skein Transposition helper.
- Produces a non-pausing screen opened once at `getUseDuration(stack) - getUseItemRemainingTicks() == 10` and quick action from `releaseUsing` only when elapsed ticks are below ten.

- [ ] **Step 1: Write failing input and screen contracts**

Assert the item starts use with `player.startUsingItem(hand)`, defines a use duration, calls ordinary call/recall only on sub-threshold release, contains no shift-cycle path, and the client tick opens `CrossbarRadialScreen` once. Assert the screen builds four inner text segments, conditionally builds four outer shape segments, never pauses, cancels from center/Escape, and sends one payload on valid release.

- [ ] **Step 2: Run client/input source tests and verify failure**

Run: `./gradlew test --tests '*CrossbarRadialScreenSourceTest' --tests '*CrossbarUseInputSourceTest'`
Expected: FAIL because hold behavior and screen are absent.

- [ ] **Step 3: Implement hybrid use semantics**

Use the vanilla use-duration lifecycle instead of immediately mutating on `use`. On the logical server, `releaseUsing` calls `callOrRecallSelectedSummon` only for elapsed durations below ten ticks. Client tick opening marks the active use as radial-opened so releasing after the threshold cannot send a quick action.

- [ ] **Step 4: Implement the text-only radial**

Reuse `GenericRadialMenu` and the manipulation radial's two-ring geometry. Center text shows charge/capacity, prepared shape, and current mode. Disabled outer choices show exactly one current reason in priority order: missing skill, missing trial, insufficient adjusted charge, projected cap, or invalid definition. Current shape is highlighted and selecting it sends nothing.

- [ ] **Step 5: Run focused and full client source tests**

Run: `./gradlew test --tests '*Crossbar*' --tests '*Radial*SourceTest'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit the client interaction**

```powershell
git add src/main/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreen.java src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java src/main/resources/assets/hemomancy/lang/en_us.json src/generated/resources/assets/hemomancy/lang/en_us.json src/test/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreenSourceTest.java src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/CrossbarUseInputSourceTest.java
git commit -m "feat: add crossbar command radial"
```

### Task 8: Canonical Documentation and Wiki Derivation

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `docs/LORE_REFERENCE.md`
- Modify: `wiki/Harbinger-Path.md`
- Modify: `wiki/Advanced-Mechanics.md`
- Modify: `wiki/Developer-Reference.md`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/summon/CrossbarDocumentationConsistencyTest.java`

**Interfaces:**
- Consumes the completed controls and progression.
- Produces canonical mechanic wording and derivative wiki text with no field sneak-cycle claim.

- [ ] **Step 1: Write a failing consistency test**

Assert canonical docs and wiki contain `Skein Transposition`, `Follow`, `Guard`, `Hunt`, `Passive`, the ten-tick hold/quick-tap distinction, and Spindle-before-Degree-5 wording. Assert no current Crossbar section says sneak-use cycles shapes.

- [ ] **Step 2: Run the documentation test and verify failure**

Run: `./gradlew test --tests '*CrossbarDocumentationConsistencyTest'`
Expected: FAIL on existing sneak-cycle text and missing skill/modes.

- [ ] **Step 3: Update canonical references first**

Replace the Crossbar control paragraph in `HEMOMANCY_REFERENCE.md`; extend the puppeteering lore/mechanics paragraph in `LORE_REFERENCE.md` only where command behavior needs canonical explanation. Do not invent new metaphysics for the radial or skill.

- [ ] **Step 4: Derive wiki descriptions from the canonical docs**

Keep wording shorter but semantically identical: Spindle preparation before Degree 5, tap call/recall, hold radial, exact-Crossbar modes, and atomic selected-cohort hot-swap.

- [ ] **Step 5: Run documentation and lore consistency tests**

Run: `./gradlew test --tests '*CrossbarDocumentationConsistencyTest' --tests '*Lore*' --tests '*Wiki*'`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit documentation synchronization**

```powershell
git add docs/HEMOMANCY_REFERENCE.md docs/LORE_REFERENCE.md wiki/Harbinger-Path.md wiki/Advanced-Mechanics.md wiki/Developer-Reference.md src/test/java/com/vincenthuto/hemomancy/common/summon/CrossbarDocumentationConsistencyTest.java
git commit -m "docs: standardize crossbar radial commands"
```

### Task 9: Full Regression and Manual Interaction Check

**Files:**
- Modify only files implicated by verified failures from Tasks 1-8.

**Interfaces:**
- Consumes all preceding deliverables.
- Produces a verified implementation with no known regressions.

- [ ] **Step 1: Run formatting and repository unit tests**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL` with zero failed tests.

- [ ] **Step 2: Run the full Hemomancy GameTest server**

Run: `./gradlew runGameTestServer`
Expected: server exits successfully with all registered GameTests passing.

- [ ] **Step 3: Launch a development client and perform the interaction matrix**

Run: `./gradlew runClient`
Expected manual observations: sub-10-tick release calls/recalls once; hold opens once; center/Escape cancels; inner modes work before Skein Transposition; outer ring is absent before and present after unlock; disabled reasons match server rules; successful swap retains unrelated bodies; failed swap changes nothing; multiplayer screen does not pause.

- [ ] **Step 4: Verify generated and primary localization remain identical**

Run: `git diff --no-index -- src/main/resources/assets/hemomancy/lang/en_us.json src/generated/resources/assets/hemomancy/lang/en_us.json`
Expected: no output and exit code 0.

- [ ] **Step 5: Inspect the final scoped diff**

Run: `git status --short` and `git diff --check`
Expected: no whitespace errors; only intentional Crossbar work plus pre-existing unrelated user changes.

- [ ] **Step 6: Record verification corrections in their owning task**

If verification exposes a defect, return to the task that owns that file, add a failing regression test there, implement the correction, rerun that task's focused commands, and use that task's explicit `git add` list. When no defect is found, make no additional commit.
