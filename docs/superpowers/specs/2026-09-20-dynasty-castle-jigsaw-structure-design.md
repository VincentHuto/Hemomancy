# Dynasty Castle → Jigsaw Structure

**Date:** 2026-09-20
**Status:** Approved (design)

## Problem

`DynastyCastleFeature` is a single procedural `Feature<NoneFeatureConfiguration>` that
builds one fixed castle silhouette (four random-selected "stages") every placement. It was
recently fixed to generate naturally (biome modifier + spawnlist tag), to always sit on solid
ground (`levelFoundation`), and to not overlap itself (`atSpacedAnchor` grid). It still lacks
real layout variety — every castle is the same arrangement of keep, four corner towers, curtain
walls, and gatehouse, only scaled by stage.

We want genuinely different castles: different keep shapes, tower counts and types, wall styles,
gate variants — i.e. a proper building assembled from interchangeable pieces.

## Decision

Convert to a custom jigsaw-style `Structure` that assembles generated `.nbt` template pieces.
This mirrors the mod's two established patterns rather than inventing a new one:

- **Piece generation** follows `CircusPavilionStructureProvider`: a `DataProvider` builds each
  piece's blocks (and baked entities) in memory and writes gzipped NBT via `NbtIo.writeCompressed`.
  Geometry is ported from the current `DynastyCastleFeature`. Pieces contain **no jigsaw connector
  blocks** — this deliberately avoids the fragile task of hand-authoring jigsaw block-entity NBT.
- **Assembly** follows `VigilPlacement`: the castle is assembled in code from real
  `PoolElementStructurePiece`s created via `StructurePoolElement.single("hemomancy:dynasty_castle/<piece>")`
  at computed offsets and rotations. A layout manifest chooses the keep variant and, per slot,
  randomizes which tower / wall / gate variant (or empty) is placed. Bounding-box/collision checks
  keep pieces from clipping.
- **Registration** follows `MausoleumStructure` + `StructureInit`: a custom `StructureType`
  registered through the mod's `DeferredRegister`, with `structure` and `structure_set` JSONs.

Variety therefore comes from (a) multiple keep NBTs, (b) multiple tower/wall/gate NBTs selected
per slot, and (c) multiple code layouts with different footprints and slot counts.

Two mechanisms we added to the feature are replaced by structure equivalents and removed:

- `levelFoundation` → structure `terrain_adaptation` (`beard_thin`).
- `atSpacedAnchor` grid → `structure_set` `random_spread` spacing + separation.

Target version: NeoForge 1.21.1 (structure NBT `DataVersion` from `SharedConstants`).

## Components

### 1. Piece generator (dev-time Java)

`DynastyCastlePieceGenerator` under `src/main/java/com/vincenthuto/hemomancy/common/worldgen/datagen/`,
runnable from gradle. It builds each piece's blocks in memory (geometry ported from
`DynastyCastleFeature`), then writes gzipped structure NBT via `NbtIo.writeCompressed` into:

```
src/main/resources/data/hemomancy/structure/dynasty_castle/*.nbt
```

Each written NBT root contains: `size` (3 ints), `palette` (list of block-state compounds:
`Name` + optional `Properties`), `blocks` (list of `{state, pos[3], nbt?}`), `entities`
(household spawns, see §5), and `DataVersion`. Pieces contain **no jigsaw connector blocks** —
assembly is handled in code (§3), so no jigsaw block-entity NBT is authored. The generator is
rerunnable so pieces regenerate from source.

### 2. Piece set

Start / keep (pick 1 in start pool): `keep_grand`, `keep_ruined`, `keep_squat` — different
heights and footprints, each exposing 4 wall connectors + 4 corner connectors.

Corner towers (per corner slot): `tower_tall`, `tower_short`, `tower_broken`, or empty.

Curtain walls (per edge slot): `wall_gothic_window`, `wall_straight`, `wall_breach`.

Gatehouse (one edge slot): `gate_intact`, `gate_ruined`, or empty.

### 3. Layout manifest (code-driven assembly)

`DynastyCastleLayouts` (Java) defines the assembly graph — the counterpart to `VigilModules`.
Each layout = a keep piece name + a list of slots, each slot carrying a local offset, a rotation,
and a weighted list of candidate piece names (including an "empty" option). At generation time the
structure picks one layout and, per slot, one candidate. No `template_pool` JSONs are required
because assembly happens in code via `StructurePoolElement.single(...)`.

### 4. Structure + structure set + biomes

- `data/hemomancy/worldgen/structure/dynasty_castle.json`: type `hemomancy:dynasty_castle`
  (custom), `start_height` (heightmap `WORLD_SURFACE_WG`), `terrain_adaptation: "beard_thin"`,
  `biomes`: `#hemomancy:has_structure/dynasty_castle`, `step: "surface_structures"`,
  `spawn_overrides: {}`.
- `data/hemomancy/worldgen/structure_set/dynasty_castle.json`: `minecraft:random_spread`,
  `spacing` 24, `separation` 8 (chunks), unique `salt`.
- `data/hemomancy/tags/worldgen/biome/has_structure/dynasty_castle.json`: the 7 fungal_gardens
  dimension biomes (fungal_gardens, drifting_mycelium, hyphal_spires, fungal_isles,
  sporecrown_thicket, mycelial_depths, hemorrhagic_plateau).

### 5. Household spawns

The Fargone household and external Thirster (currently spawned in `place()`) are baked into piece
NBTs as `entities` entries. `FargoneVariant` is written into each Fargone's entity tag per role,
so `keep_grand` carries elders/guards, `tower_broken` carries a lone reclaimed, etc. This removes
`spawnHousehold` / `spawnExternalRemnant` runtime logic.

### 6. Cleanup / removals

- Remove feature registration from `BaseFeatureInit`, `ConfiguredFeatureInit`, `PlacedFeatureInit`.
- Delete `data/hemomancy/neoforge/biome_modifier/add_dynasty_castle.json`.
- Delete `worldgen/configured_feature/dynasty_castle.json` and
  `worldgen/placed_feature/dynasty_castle.json`.
- Rename/replace `tags/worldgen/biome/dynasty_castle_spawnlist.json` with the
  `has_structure/dynasty_castle.json` tag.
- Delete `DynastyCastleFeature.java` (geometry now lives in the generator).

## Non-goals

- Interior room jigsaw graphs (dungeon-style branching interiors). Pieces are exterior silhouettes
  with baked household spawns, matching current behavior.
- Loot tables / new block entities inside the castle.

## Risks

- **Piece offset alignment** — because assembly is code-driven, each slot's offset/rotation is
  authored in `DynastyCastleLayouts` and must line pieces up flush (e.g. a wall's end abutting the
  keep's edge). Likely needs one in-game iteration pass to nudge offsets. Lower risk than jigsaw
  connectors since it is plain integer math we control.
- Baked palette must use exact block state property strings, or NBT load warns/drops states.
  Validate by loading each piece in a structure block and running `/place structure`.

## Testing

- Generator writes valid NBT: load each piece in a structure block in-game without error.
- `/place structure hemomancy:dynasty_castle` assembles a full castle.
- Multiple placements produce visibly different layouts (different keep + tower/wall/gate mix).
- Natural generation appears in the fungal_gardens dimension biomes with no overlaps.
- Existing worldgen source tests updated (`DynastyCastleWorldgenSourceTest`).
