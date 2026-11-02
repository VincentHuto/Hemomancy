# Gardens Facsimiles (Section 2a) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eight facsimile mobs live in the Fungal Gardens dimension, visibly wrong copies of Earth creatures.

**Architecture:** Each facsimile subclasses its vanilla mob and only strips interaction or goals. Each renderer subclasses the vanilla renderer, swaps in a model subclass that applies seeded part scales, and adds one shared growth layer. The pure seeded math (`FacsimileDeformationRules`) is JVM-tested, and so are the resources (loot, lang, eggs, textures, spawns).

**Spec:** `docs/superpowers/specs/2026-10-28-gardens-facsimiles-design.md`

## Global Constraints
- Use `Hemomancy.rloc("snake_id")` throughout, and the `.build(...)` name must equal the registry id.
- Lang and loot are hand-written in `src/main/resources`, and spawn egg models are hand-written too.
- RenderTypes are built once as static constants.
- No `net.minecraft.client` under `common/`.
- Dimension biome spawners change only through the worldgen script.
- Frozen fixtures: `registry-ids.txt` is sorted alphabetically; in `creative-tab-order.txt` the new eggs go after `spawn_egg_tooth_pecks`, in registration order.
- No commits.

## Review Focus
1. A shared model instance must not leak one entity's scale onto the next. The scales are absolute for every part on every frame, pinned by the `scaleFor` determinism test.
2. Horses, donkeys and wolves must not be tamable, rideable, breedable or chestable by any item.
3. The enderman must never take blocks from Gardens terrain.
4. The golem must not attack Gardens monsters (no village-defence or monster targeting), so it doesn't thin out the Gardens.
5. The Overworld biome spawn list is unchanged.

### Task 1: Deformation rules
- Create `client/model/entity/mob/facsimile/FacsimileDeformationRules.java` with:
  - `static float[] scaleFor(long seed, int partIndex)`, which returns 3 floats, deterministic;
  - `static long seedOf(UUID)`.
- Test `FacsimileDeformationRulesTest`:
  - same inputs give equal arrays;
  - every value is in [0.8, 1.3];
  - across 200 parts of one seed, the share with any axis ≠ 1 is between 25% and 55%;
  - two different seeds differ somewhere in the first 10 parts.
- RED → implement → GREEN.

### Task 2: Entities, data, spawns
- Create `common/entity/mob/facsimile/Facsimile{Bat,Enderman,Blaze,Golem,Horse,Donkey,Wolf,Spider}.java`, where interaction returns PASS and `canMate` is false:
  - Enderman: remove the block goals by class name after `super.registerGoals()`.
  - Golem: replace `registerGoals` with float, melee attack, random stroll, look-at-player, random look, and the `HurtByTargetGoal` target.
  - Horse and donkey: `mobInteract` returns PASS, and `isFood` is false.
  - Wolf: `mobInteract` returns PASS, and `isFood` is false.
  - Bat: its own static spawn check.
- `EntityInit`: the 8 types, their attributes, and spawn placements.
- `ItemInit`: the 8 eggs.
- Add lang, loot tables, egg item models, the worldgen script `BIOME_SPAWNS` entries plus a re-run, and the frozen fixture lines.
- Test `FacsimileResourcesTest`, for each of the 8 ids:
  - the loot table drops `spore_sac` under `killed_by_player`;
  - the entity and egg lang keys exist;
  - the egg model exists;
  - the overlay texture exists;
  - at least one dimension biome lists it;
  - `worldgen/biome/fungal_gardens.json` does not.
- RED → implement → GREEN, plus `EntityTypeBuildNameGuardrail` and `HardCodedEnglish`.

### Task 3: Client
- Generate the overlay textures (`textures/entity/facsimile/<id>_growth.png`) at the vanilla sizes.
- `client/render/entity/mob/facsimile/FacsimileRenderTypes` (static constants) and `FacsimileGrowthLayer<T, M>`.
- One model subclass per family (`FacsimileBatModel`, …) whose `setupAnim` calls super and then `FacsimileDeformation.apply(parts, seed)`.
- Eight renderers, registered in `EntityRendererRegistrar`.
- Verify with `compileJava`, `RenderTypeCachingGuardrail` and `CommonClientIsolation`. The in-game look stays unverified until a client launch.

### Task 4: Docs and verification
- Add a facsimile row block to the HEMOMANCY_REFERENCE bestiary and the wiki World Content page.
- Regenerate the impact map.
- Run the full `test` and `build`, then `runGameTestServer`; only the known reds may remain.
- Run the final review.
