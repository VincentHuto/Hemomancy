# Mnemonist Puppet Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `mnemonist_puppet` as a Degree 5 puppeteer summon that records recent damage against its target and replays bounded ghost echoes.

**Architecture:** Add a pure `MnemonistPuppetRules` class for replay math and buffer constraints, a `MnemonistPuppetEntity` following existing bound summon entity patterns, and a focused summon event hook that records damage events for nearby puppets targeting the damaged entity. Register the summon through the existing crossbar summon definition/factory/entity/lang/recipe paths.

**Tech Stack:** Java 21, Minecraft NeoForge 1.21.1, existing Hemomancy puppeteer summon system, pure direct Java rule harness plus Gradle build/test verification.

---

### Task 1: Pure Replay Rules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetRules.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetRulesTest.java`

- [ ] Write failing tests for memory cap, expiry, target matching, replay damage clamp, and replay recursion rejection.
- [ ] Run `./gradlew.bat testClasses; if ($LASTEXITCODE -eq 0) { java -cp "build/classes/java/main;build/classes/java/test" com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetRulesTest }` and confirm it fails because `MnemonistPuppetRules` is missing.
- [ ] Implement `MnemonistPuppetRules` with constants: max 6 memories, 160 tick expiry, 40 tick replay interval, 16 stored damage cap, 45% replay fraction, 8 replay damage cap.
- [ ] Re-run the focused direct test and confirm it passes.

### Task 2: Bound Puppet Entity and Event Hook

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetEntity.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetEvents.java`

- [ ] Implement a zombie-shaped bound summon entity with low direct stats, standard owner/crossbar synced data, save/load through `BoundSummonBehavior`, and a transient memory buffer.
- [ ] Add methods to record memories and replay one valid memory against the current target every 40 ticks.
- [ ] Add `LivingDamageEvent.Pre` hook that skips replay damage, finds nearby puppets targeting the damaged entity, and records clamped memories.

### Task 3: Registry and Player-Facing Wiring

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonDefinitions.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonFactory.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Create: `src/main/resources/data/hemomancy/recipe/puppeteer_trial/mnemonist_puppet.json`

- [ ] Register `mnemonist_puppet` as a Degree 5 summon with tactical role text, entity type, attributes, factory creation, trial creation, and a vanilla zombie renderer.
- [ ] Add language for entity, summon role, lore, and spawn egg if a spawn egg is registered.
- [ ] Add a puppeteer trial recipe using memory-flavored reagents.

### Task 4: Documentation and Verification

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] Document Mnemonist Puppet in the puppeteer summon table and notes.
- [ ] Run `./gradlew.bat compileJava`.
- [ ] Run the focused direct rule test.
- [ ] Run `./gradlew.bat test`.
- [ ] Run `./gradlew.bat build`.
