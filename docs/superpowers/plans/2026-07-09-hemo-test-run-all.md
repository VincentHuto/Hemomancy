# Hemo Test Run All Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `/hemo test run_all` to execute all development scenarios sequentially and summarize every result.

**Architecture:** `HemoTestCommands` owns a package-visible batch runner that performs setup, verify, and cleanup in a `finally` block. The Brigadier literal delegates to it and formats result messages through the existing command source.

**Tech Stack:** Java 21, Brigadier, NeoForge, JUnit 5.

## Global Constraints

- Work on the existing branch and do not commit existing user changes.
- Preserve existing single-scenario command behavior.
- Continue after failures and return `0` when any scenario fails.
- Always execute each scenario cleanup before proceeding.

---

### Task 1: Batch command and contract

**Files:**
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestCommands.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/testing/GameTestHarnessSourceContractTest.java`
- Modify: `docs/TESTING.md`

- [ ] Add a failing source-contract assertion for `literal("run_all")` and `runAll`.
- [ ] Run `./gradlew.bat test --tests "com.vincenthuto.hemomancy.testing.GameTestHarnessSourceContractTest" --no-daemon`; expect failure because `run_all` is absent.
- [ ] Add `run_all`, implement sequential setup/verify/clear with a `finally` cleanup, per-scenario output, and final summary.
- [ ] Update `docs/TESTING.md` with the new command and its continue-on-failure behavior.
- [ ] Rerun the focused JUnit contract; expect success.
