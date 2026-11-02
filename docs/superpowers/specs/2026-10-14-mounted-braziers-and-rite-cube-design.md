# Mounted Iron Braziers and the Rite Cube — Design

Date: 2026-10-14. Approved in conversation (cube scope: anywhere in the cube; hanging: chains; wall: bracket arm).

## Goal

The Iron Brazier can stand on a block (as now), mount on the side of a block, or hang from the underside of
one. A Cardinal Rite counts any lit brazier with an offering anywhere in the N×N×N space above its floor, whatever
its mount, so rites can be built into rooms with sconces and hanging braziers.

## 1. Block state and placement

- `BrazierBlock` gains vanilla `BlockStateProperties.ATTACH_FACE` (`floor`, `wall`, `ceiling`) beside `facing`.
  Default `floor`, so existing worlds and every `defaultBlockState()` caller keep a standing brazier.
- Placement follows the clicked face, trying the player's nearest looking directions in turn like a lantern/torch:
  top face → `floor`; side face → `wall`, `facing` = the clicked face (pointing away from the wall); bottom face →
  `ceiling`. `facing` for floor and ceiling is the player's horizontal direction reversed, as now.
- Support: `wall` needs the block behind (opposite `facing`) sturdy on that face; `ceiling` needs the block above
  to support its centre from below. When the support goes, the brazier breaks (drops itself; `onRemove` already
  spills its offering). `floor` keeps today's rules (no support check).
- Mirror/rotate keep the mount and rotate `facing`.

## 2. Models (`tools/model_export/iron_brazier.py`)

- The bowl, coal bed, talons and spout are one shared assembly, shifted down per mount:
  floor 0, wall −4/16, ceiling −6/16 (`BrazierMount` holds the same numbers).
- Wall: an iron wall plate on the back (z = 16 in north-facing model space; the spout faces away from the wall)
  and a curved bracket arm with a brace reaching under the bowl collar. No dais or stem.
- Hanging: a ceiling plate and hook ring under it, four hematic iron chains from the ring to the bowl rim. No
  dais or stem.
- Each mount has its own body, `_lit` and `_sanguine` models. The blockstate multipart keys on `face`, `facing`
  and `ritual_phase`. Hematic iron, venous stone and vein accents only; no copper.

## 3. Shared mount geometry (`common/block/harbinger/rite/BrazierMount`)

One pure helper, used by server and client:
- `drop(face)`: the bowl's vertical shift in blocks (0, −0.25, −0.375).
- `bowl(state, pos, height)`: `atCenterOf(pos) + (0, height + drop, 0)` — the replacement for every hard-coded
  "centre plus a height" (offering display, flames and pool, embers and soot, offering effects, absorption rites,
  daemon absorb/break points).
- `daemonStop(state, pos, focus)`: above a standing bowl (as now, +1.2), out from a wall bowl along `facing`,
  beside and below a hanging bowl toward the Focus, so the daemon never flies into the ceiling.
- `shape(face, facing)`: collision/outline shapes per mount.
- `placementFace(clickedFace)`: the pure clicked-face → mount rule.

## 4. The rite cube

- `CardinalRiteBrazierSpace.cells(half)`: every offset with x, z in [−half, half] and y in [1, 2·half] (the floor
  of size N = 2·half+1 is the bottom layer of an N×N×N cube, Focus at its bottom-centre), excluding the Focus
  column (x = z = 0, where the staff stands and the daemon rises) and the floor-level (y = 1) cells where a ceremony
  anchor sore opens. Ordered clockwise round the Focus from its left (`CardinalRiteGashRules.around`), then nearest,
  then lowest.
- `CardinalRiteFloorDefinition.brazierSpace()` returns those cells for its half. `brazierSockets()` stays the gash
  cells: the Structure Spawner's six spots, the moat bend and the floor mesh's gash.
- `CardinalRiteStationMatcher.matchedBraziers` scans `brazierSpace()` instead of the gash cells. Succession roles
  follow that order (clockwise round the Focus from its left, nearest, lowest).
- Floor mesh: any standing brazier on a floor cell (y = 1, sturdy ground) gets a flesh mound; only those on gash
  cells bend the trench. Wall and hanging braziers leave the membrane alone.
- Succession role labels mark braziers holding an offering in matcher order, instead of fixed old sockets.
- Hint screens: "Braziers count anywhere in the N×N×N space above the floor".

## 5. Testing

- JVM: brazier space (count, bounds, order, no Focus column, no anchor cells); mount geometry (drops, shapes
  within the block, daemon stops); placement-face rule; the generated blockstate covers every face × facing and
  references existing models.
- GameTests: a lesser floor resolves with a wall-mounted and a hanging brazier in its cube; a brazier one block
  above the cube is ignored; a wall brazier breaks and spills its offering when its wall is removed; placing on a
  wall face gives a wall brazier facing out.
- Visual: the model preview, plus a brazier client review run capturing standing, wall and hanging braziers lit.

## Out of scope

Unstained rites; changing the gash or spawner; new brazier recipes.
