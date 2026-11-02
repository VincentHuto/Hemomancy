# Saint Mausoleum Complexes (Section 5) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans.

**Goal:** four generating Saint mausoleums built from shared jigsaw parts, with the D8 seal and slots for Chapels and the ring door.
**Spec:** `docs/superpowers/specs/2026-10-28-mausoleum-complexes-design.md`

## Global Constraints
- **AGENTS.md:** jigsaw structures go through `HemoJigsawStructure` (the existing `MausoleumStructure` type).
- **Templates:**
  - every pooled template is committed (`StructureTemplateCoverageTest`);
  - the only empty `has_structure` tag remains `mausoleum`.
- **Data:** frozen registry ids, lang keys only, no commits.

## Review Focus
1. Every pool element resolves to a committed NBT, and every jigsaw's pool exists.
2. The seal never lets a player below D8 through, and always closes again.
3. Each chamber's sarcophagus holds its own Saint.
4. The orphaned loot tables are reachable.
5. The palette swap never turns a stair, slab or wall into a block that lacks their properties.

### Task 1: Seal rules and blocks
- `MausoleumSealRules`: `mayPass(int degree)` (D8), `OPEN_TICKS=100`, `SPREAD=16`.
- `MausoleumSealBlock`:
  - `OPEN` property;
  - static `open(ServerLevel, BlockPos)` flood;
  - scheduled close.
- `RingDoorBlock` and `ChapelFocusBlock`: plain unbreakable blocks for now.
- Models and textures, lang, fixtures. The blocks are itemless (`ITEMLESS_BLOCKS` and the pinned GameTest list).
- Tests:
  - `MausoleumSealRulesTest`;
  - GameTest `mausoleumSealOpensJoinedSealsAndCloses`.

### Task 2: Generator and data
- `tools/worldgen/mausoleum_complexes.py`, run it.
- Structure JSONs, the set, biome tags.
- Test: `MausoleumComplexResourceTest` (JVM, reads the NBTs through nbtlib-free gzip and NbtIo). For each Saint it checks:
  - the chain's pools exist;
  - the chamber's `SaintType`;
  - three Chapel jigsaws, the ring door and seals in the nave and seal hall;
  - barrels with loot tables.

### Task 3: Docs, verification, review
