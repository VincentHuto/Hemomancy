# Section 4b Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans.

**Goal:** the Abhorrent Thought gaze and grab, adipocere with scraping and distillation as an offering, and the Visitation rite.
**Spec:** `docs/superpowers/specs/2026-10-28-possession-hazards-wax-visitation-design.md`

## Global Constraints
- AGENTS.md rules and fixtures.
- CardinalRitePurposeDataTest, HarbingerRecipeMapDefinitions and their test count (56).
- Lang keys only.
- No commits.

## Review Focus
1. The thought behaves exactly as before toward players who are not possessing.
2. Scraping can't duplicate drops: it is gated on HARVESTED.
3. Visitation never moves a non-Apotheos, and never strands one without a return point.

### Task 1: AbhorrentGazeRules (JVM)
- `GAZE_RANGE=10`, `GAZE_TICKS=20`, `GAZE_COST=1f`, `GRAB_MIND_BROKEN_TICKS=12000`.
- `boolean gazes(boolean possessed, double distSqr, boolean sight)`.
- Test: `AbhorrentGazeRulesTest`.

### Task 2: The Abhorrent Thought
- Gaze in `customServerAiStep`.
- Grab in `doHurtTarget`.

### Task 3: Adipocere
- Item, texture, lang.
- `BogBodyBlock` axe branch.
- Bog revenant loot pool.
- Distillation recipe.
- `PossessionService` offering (+3 max).
- Rules test addition: `PossessionRules.ADIPOCERE_BONUS = 3`.

### Task 4: Visitation rite
- JSON, completion and refusal (`VisitationService`).
- Map, purpose test, lang.

### Task 5: Docs, verification, review
