# Gardens Terrain, Rendering Pools & Hunter Fungi (Section 2c) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cap bridges for the Apotheos, gills that bite strangers, rendering pools that break an Apotheos's offerings into enzymes, and hunter fungi that converge on a heard footstep.

**Spec:** `docs/superpowers/specs/2026-10-28-gardens-terrain-design.md`

## Global Constraints
Follow AGENTS.md:
- particles only through `HemoParticleData`;
- lang keys, not literal text;
- no client classes under `common/`;
- `Hemomancy.rloc`;
- `@EventBusSubscriber(modid = Hemomancy.MOD_ID)` without `bus=`;
- frozen fixtures (registry ids sorted, creative-tab order) updated;
- a blood profile for the new >20-HP mob;
- dimension-only biome lists edited through `tools/worldgen/fungal_gardens_worldgen.py`;
- no payloads (no protocol bump);
- no commits.

## Review Focus
1. Non-Apotheos items in rendering bile, death drops included, are never consumed. Covered by a GameTest.
2. A bridge never replaces a non-air block and stops at the first obstruction. Covered by a GameTest.
3. Gills never trap an Apotheos, never react to mobs or creative players, and always reopen. Covered by rule tests and a GameTest.
4. Hunter fungi never hunt an Apotheos. Covered by a rule test.
5. A recipe file with a bad chance or empty results fails loudly. Covered by a resource test.

### Task 1: Rules (JVM)
- `common/world/RenderingPoolRules`:
  - `boolean mayRender(boolean throwerApotheos)`
  - `int yield(int count, int stackCount, float chance, RandomSource random)`: chance ≥ 1 gives count×stack, ≤ 0 gives 0
  - `PULSE_TICKS = 10`
- `common/world/GardensTerrainRules`:
  - `BRIDGE_REACH = 16`, `BRIDGE_GROW_TICKS = 2`, `BRIDGE_LIFETIME_TICKS = 600`, `BRIDGE_LINGER_TICKS = 100`, `BRIDGE_HOLD_RADIUS = 3`
  - `boolean mayBridge(boolean apotheos, boolean inDimension, boolean emptyHand)`
  - `Direction bridgeDirection(Direction face, Direction playerFacing)`
  - `GILL_SPREAD = 24`, `GILL_REOPEN_TICKS = 100`, `GILL_REOPEN_WAIT = 20`, `GILL_BITE = 3f`
  - `boolean gillSnaps(boolean apotheos, boolean creativeOrSpectator, boolean alive)`
- `common/entity/mob/monster/HunterFungusRules`:
  - `CONVERGE_RADIUS = 64`, `POUNCE_RADIUS = 16`, `MEMORY_TICKS = 600`, `ARRIVE_DISTANCE = 3`
  - `enum Response { IGNORE, TARGET, CONVERGE }` and `Response respond(boolean hasTarget, double distanceSqr)`
  - `boolean mayHunt(boolean apotheos, boolean creativeOrSpectator)`
- Tests first: `RenderingPoolRulesTest`, `GardensTerrainRulesTest`, `HunterFungusRulesTest`, each RED → GREEN.

### Task 2: Rendering pools
- `FluidInit` gets `RENDERING_BILE_TYPE` plus the source and flowing fluids. `BlockInit.RENDERING_BILE_BLOCK` goes in LIQUIDBLOCKS.
- `FluidClientExtensions` entry, plus a translucent render layer in `ClientSetupRegistrar`.
- Generated textures: `rendering_bile_still/flow(+mcmeta)/overlay`.
- `common/recipe/RenderingRecipe` + `serializer/RenderingRecipeSerializer` (a `Result(ItemStack stack, float chance)` codec built on `ItemStack.MAP_CODEC`), and `RecipeInit` type and serializer `rendering`.
- The 9 recipes in `data/hemomancy/recipe/rendering/`.
- `common/event/RenderingPoolEvents` (an `EntityTickEvent.Post` on ItemEntity).
- `MorphicPoolFeature(codec, fluid supplier, rim supplier)`, `BaseFeatureInit.RENDERING_POOL`, configured and placed `rendering_pool` JSON, and the python feature lists.
- Lang: `fluid_type.hemomancy.rendering_bile`, `block.hemomancy.rendering_bile_block`.
- Tests:
  - `RenderingRecipeResourceTest` (JVM): every recipe file has an input, non-empty results, chances in (0,1], and render_time > 0.
  - GameTest `renderingBileLeavesUnownedItemsAlone`.

### Task 3: Cap bridges
- `common/block/harbinger/plant/CapBridgeBlock` (FACING, REACH, GROWN; scheduled growth and withering), MODELEDBLOCKS `cap_bridge`, `noLootTable`, ITEMLESS.
- Tag `data/hemomancy/tags/block/bridging_caps.json`.
- `common/event/CapBridgeEvents` (`PlayerInteractEvent.RightClickBlock`).
- Blockstate and model (slab_top with infected_cap texture).
- Lang `block.hemomancy.cap_bridge`.
- GameTest `capBridgeGrowsUntilObstructed`.

### Task 4: Gills
- `common/block/harbinger/plant/FungalGillBlock` (OPEN; hanging survival; `snap(ServerLevel, BlockPos)` flood; scheduled reopen), MODELEDBLOCKS `fungal_gill`.
- Damage type `gill_bite` (JSON, `HemoDamageTypes.GILL_BITE`, death lang).
- Textures `fungal_gill` (cross) and `fungal_gill_shut`; blockstate, models and item model.
- Loot table (shears or Silk Touch).
- Configured `gill_curtain` (`minecraft:block_column`) and its placed feature; python lists.
- Lang and creative tab.
- GameTest `gillsSnapShutTogetherAndReopen`.

### Task 5: Hunter fungi
- `common/entity/mob/monster/HunterFungusEntity`: goals, `hearAlarm(BlockPos, long now)`, a converge goal, and static `converge(ServerPlayer)` called from `GardensNetwork.alarm`.
- `EntityInit` entry, attributes and spawn placement; egg and egg model; loot; blood profile; responders tag; python spawners.
- Client: `HunterFungusModel` and `HunterFungusRenderer`, with layer and renderer registration, and a generated texture.
- Lang and fixtures.
- GameTest `hunterFungusRemembersADistantAlarm`.

### Task 6: Docs, verification, review
HEMOMANCY_REFERENCE §5.6b and the wiki World-Content page; the impact map. Then the full `test`, `build` and GameTests, and the final review.
