# Heart-Node Sanctum (Section 3) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** An Apotheos's private sanctum in the Gardens: it plants, grows and keeps the owner's spawn, gives a bonus inside, and holds a death cache.

**Spec:** `docs/superpowers/specs/2026-10-28-heart-node-sanctum-design.md`

## Global Constraints
- AGENTS.md rules: lang keys, `HemoParticleData`, `Hemomancy.rloc`, `BlockEntitySync.markAndSync`, `HemoTickers.create`, frozen fixtures, and no client classes under `common/`.
- No payloads.
- No commits.

## Review Focus
1. No item is lost or duplicated by the cache: overflow, owner break, a non-owner break, keepInventory. Covered by a GameTest for store and take.
2. Only the owner opens or breaks the cache or node. Covered by rule tests.
3. Growth never replaces solid blocks. Covered by a GameTest.
4. A stale respawn never points at a removed node. Covered by the login check.
5. Planting is refused outside the dimension, off the network, or by a non-Apotheos. Covered by rule tests.

### Task 1: Rules (JVM)
`common/world/HeartNodeRules`:
- `FOOTPRINT_RADIUS=8`, `FOOTPRINT_HEIGHT=6`, `RIB_RADIUS=6`, `RIBS=8`, `GROW_TICKS=100`, `BONUS_TICKS=40`, `BONUS_SPORES=4f`, `CACHE_SEARCH=8`
- `boolean mayPlant(boolean apotheos, boolean inDimension, boolean onNetwork)`
- `boolean inFootprint(BlockPos node, BlockPos pos)`
- `boolean shouldCache(boolean apotheos, boolean keepInventory, boolean hasNode)`
- `boolean mayOpen(UUID owner, UUID actor)`
- `BlockPos ribBase(BlockPos node, int rib)`

Test: `HeartNodeRulesTest`.

### Task 2: Blocks, block entities, saved data
- `HeartNodeBlock` + `HeartNodeBlockEntity` (owner, stage; ticker grows ribs).
- `FruitingBodyCacheBlock` + `FruitingBodyCacheBlockEntity` (owner, stacks; `store`, `takeAll`).
- `HeartNodeSavedData`.
- Registration in BlockInit (MODELEDBLOCKS; cache is itemless) and BlockEntityInit.
- Textures, models, blockstates, item model, loot (node drops itself; cache has no loot table), recipe, lang, creative tab, frozen ids.

### Task 3: Events
`common/event/HeartNodeEvents`:
- placement gate (`BlockEvent.EntityPlaceEvent`) and privacy (break/place in footprint);
- bonus tick (`PlayerTickEvent.Post`, every 40 ticks);
- death cache (`LivingDropsEvent` LOWEST);
- login respawn check.

### Task 4: GameTests, docs, review
- GameTests: `fruitingBodyCacheReturnsEverythingItHolds`, `heartNodeRibsNeverReplaceSolidBlocks`.
- HEMOMANCY_REFERENCE §5.6d and the wiki.
- Full test, build and GameTests, then the final review.
