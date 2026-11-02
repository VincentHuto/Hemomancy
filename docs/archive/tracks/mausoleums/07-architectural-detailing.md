# Track 7 — Architectural detailing kit

**Goal:** rooms are built from an architectural vocabulary (base course, pilasters, arches, vaults, cornices, floor inlays, lighting) rather than painted as flat single-block shells. The kit is theme-agnostic; each theme track supplies its block family and motifs.

## Status (2026-10-28)
Built; ledger `.superpowers/sdd/2026-10-28-mausoleum-rework-07/progress.md`. The kit is `tools/worldgen/mausoleum/kit.py` (the package Track 3 split out, not `tools/worldgen/kit.py`), the acceptance checks `checks.py` (run by `build.py` on every template; a failure stops the generator), the entrance `entrance.py`, the previews `kit_preview.py`. The venous stone family was already complete; the bone brick family is added.

## Current state
- `Room.shell(wall, floor, ceiling)` in `mausoleum_complexes.py` fills one block type per surface. The result (screenshots) is flat ice, flat quartz, flat netherrack-red walls with no relief.
- Palettes are a single block per role (`WALL_BLOCK`, `FLOOR_BLOCK`, `LIGHT_BLOCK`) plus a swap table for the copied pieces.
- Lighting is a sea lantern or shroomlight in the ceiling.

## The kit (`tools/worldgen/kit.py`)
A `Theme` record supplies a block family and the kit paints with it:

| Role | What the theme supplies | Used for |
| --- | --- | --- |
| `base` | full block | base course, plinths, stair cores |
| `wall` | full block (+ `mossy`/`cracked` variants, optional) | wall field, with variant speckling |
| `trim` | full block + stairs + slab + wall | pilasters, cornices, arch stones, dado rails, balustrades |
| `accent` | chiseled/lacquered block | pilaster capitals, keystone, inlay crosses |
| `floor` | full block + slab | floor field, steps |
| `inlay` | full block | floor patterns (border, cross, ring under the tomb) |
| `vault` | full block + stairs | ceiling vaults |
| `light` | block or a Track 6 sconce | wall sconces every N blocks, ceiling bosses |

Modules (each takes a box in template space and paints):
- `corridor(box, width, height)`: base course, pilasters every 4, a dado at y=1, a cornice under the ceiling, a vaulted (stair-stepped) ceiling if height ≥ 5, sconces on alternating pilasters.
- `hall(box)`: corridor rules plus a double row of free-standing pillars (`trim` core, `accent` capital), a gallery ledge at mid-height when height ≥ 9, floor inlay border.
- `vault_ceiling(box)`: a barrel or cross vault built from stairs and slabs, keystone row in `accent`.
- `arch(opening)`: doorway with jambs, a stair-stepped arch and a keystone; every doorway in the complex goes through this.
- `stair_shaft(box, turns)`: the spiral descent for Track 3, with landings and balustrades.
- `niche(wall_face)`: a 1×2 recess with a Track 6 `sealed_niche` or `opened_niche` front.
- `dais(box, steps)`: stepped plinth for tombs and foci, inlay ring on top.
- `speckle(box, variants, chance)`: swaps a fraction of `wall` for its variants so large surfaces are not uniform.

The kit never places a block that lacks the property it sets (stairs need `facing`/`half`/`shape`, walls need the six side properties); `MausoleumComplexResourceTest` grows a check that every placed state's properties exist for that block (the first pass's palette swap already had this risk).

## Block families to confirm or add
- Venous stone family (Hemorath): `polished_venous_stone`, bricks, cracked, chiseled, stairs, slab, wall. Confirm every piece exists in `BlockInit`; add the missing ones (stairs/slab/wall in `STAIRBLOCKS` / `SLABBLOCKS` / a wall register) with textures at 16 px.
- Hematic iron trims (all themes): `hematic_iron_block`, chiseled, bold chiseled, pillar, pillar top exist (`consecrated_bloodwell.py` uses them).
- Bone trims: added as `bone_bricks`, `cracked_bone_bricks`, `chiseled_bone_bricks`, `bone_brick_stairs`, `bone_brick_slab`, `bone_brick_wall` (textures from `tools/model_export/bone_brick_family.py`) for Putriciel and Hemorath.
- Vanilla families for Seraphae (quartz, calcite, diorite) and Velorum (deepslate, packed/blue ice) are complete.

## Tasks
1. Write `kit.py` with the modules above and a `--preview` that renders a test room to PNG (reuse `station_kit.preview` or the voxel preview from `pelagic_ecology.py`).
2. Add the missing block-family pieces, with textures authored at 16 px (a small generator in `tools/model_export/` for the brick/stair/slab/wall textures of each new family).
3. Rebuild the copied outer pieces (`top`, `stairs`, `antechamber`) through the kit too, so the entrance matches the rest; the original hand-built NBTs become reference only.
4. Property-validity test in `MausoleumComplexResourceTest`.
5. Proof: a kit preview per module and one Track 0 screenshot of a corridor and a hall in each theme.

## Acceptance
- No surface larger than 3×3 is a single flat block.
- Every doorway has an arch; every room taller than 5 has a vault; every corridor has pilasters and sconces.
- The kit runs under `python tools/worldgen/mausoleum_complexes.py` with no manual step.
