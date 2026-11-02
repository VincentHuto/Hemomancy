# Blood Structure Offering Spawners Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans. Steps use checkbox syntax for tracking.

**Goal:** Generate recipe-defined offering braziers in the journey and Debug Structure Spawner.

**Architecture:** A shared pure planner expands recipe offerings into deterministic perimeter slots. Journey fixtures place empty phase-zero braziers and supply reagents; the debug spawner fills identical planned slots with representative ingredient stacks and phase 1.

**Tech Stack:** Java 21, NeoForge 1.21.1, GameTest, JUnit source contracts.

## Global Constraints

- Remain on `neo-1.21.1`; no worktrees, staging, commits, or pushes.
- Journey code remains under `src/gameTest`; shared planning and debug-spawner integration remain main source.
- Use one distinct brazier per required offering unit.

### Task 1: Shared offering planner

- [ ] Add failing tests for unique deterministic west/east/north/south perimeter slots and count expansion.
- [ ] Implement `BloodStructureOfferingPlacement.plan(...)` returning `OfferingSlot(pos, offering, representativeStack)`.
- [ ] Run focused JVM tests until green.

### Task 2: Journey and debug consumers

- [ ] Add failing GameTests/source contracts for empty phase-zero journey braziers and loaded phase-one debug braziers.
- [ ] Place journey braziers with solid support, supply exact reagents, and budget brazier lighting blood.
- [ ] Pass recipe offerings into `DebugShowcaseItem`, load representative stacks, set phase 1, and expand showcase spacing.
- [ ] Run `alphaCheck`, build/inspect the release jar, and run `git diff --check`.
