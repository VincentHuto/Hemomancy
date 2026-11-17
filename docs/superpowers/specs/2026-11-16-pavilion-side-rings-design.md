# The pavilion's side rings: the Strongman's and the Beast Tamer's yards

2026-11-16. Grows the Crimson Troupe pavilion template from 65 × 23 × 65 to 81 × 23 × 65 so the two yards beside the
big top become proper auxiliary performance rings.

## Intent

The Strongman's yard (west) and the Beast Tamer's yard (east) read as bare sawdust rectangles dropped on the grass
(`path()` in `tools/worldgen/circus/pavilion.py`, x 3–10 and 55–61). The owner wants them to feel like part of the
circus: proper side rings, integrated with the tent and clean along their edges.

## Goals

- Each yard is a camp-sized performance ring, built like the teaching camps (`camps.py`): a rounded sawdust yard, a low
  packed-mud bank under mud-brick slabs round it, and a practice ring of radius 5 (curb, lacquer track, venous-stone
  hub) with footlights.
- The yard opens toward the tent: a gap in the bank with a sawdust path to the tent's side door, and two quarter poles
  with a pennant line over the way in.
- Edges are deliberate: the bank and its skirt finish the yard; nothing ends in a straight sawdust line on the grass.
- The apparatus stands round the ring, not in it: the Strongman's stilt rack and weight frames, the Beast Tamer's hound
  hoops (three, as now) with the hound's run clear.
- The yards are lit at night (lamp posts as in the camps, and the footlights).

## Non-goals

- No change to the big top's interior, the midway, the backyard wagons, the actors' behaviour or any server rule.
- No change to the camps or the abandoned stage.

## Anti-goals

- A yard that is a rectangle again, or that ends flush in the grass with no edge.
- Copper, brass, bronze or gold anywhere (the kit's forbidden list).
- A wider gate that stops pavilions placing on most seeds (ruled out: the gated core is unchanged).

## Approaches

- **A. Widen the one template to 81 × 65 (chosen by the owner).** The generator draws the current pavilion through an
  8-block x offset so `pavilion.py`'s own coordinates stay as they are; the new margins hold the two yards. Keeps the
  pavilion one template, which every test and the review client place whole (the owner's earlier choice).
- B. Keep 65 × 65 and add the yards as jigsaw pieces. Rejected: jigsaw blocks in the template, a yard can fail to place
  silently, the ruin overlay would not reach it, and every test and review scene would place pieces by hand.

## Decided

- **Size and offset.** `SIZE = (81, 23, 65)`. Everything the pavilion places today moves +8 in x; z is unchanged. The
  carousel moves from template (32, 1, 32) to (40, 1, 32): `CircusSiteStateService.CAROUSEL`.
- **Yards (my call after the builder check).** Eight blocks a side is too tight for a square camp yard, so each yard is
  a long rounded yard along the tent's flank, about 17 across (x) and 25 long (z), centred at template x 9 (west) and
  x 71 (east), z 32. The radius-5 ring stands in the middle; the apparatus at the yard's north and south ends; lamp
  posts at the corners. The bank stops one cell short of the template edge so a skirt finishes it. The camps build on
  row 1 over a row-0 foundation; the pavilion's ground is row 0, so the yard is built at the pavilion's own levels, not
  copied.
- **Way in.** East: the existing artists' door (template x 61 after the shift, z 29–30) faces the yard; the bank gap
  and path line up on it. West: mirror the artists' door on the west wall (x 19 after the shift, z 29–30) if those
  wall cells are clear of the west rigging tower; otherwise the west yard's gap faces north to the midway path.
- **Actors.** The Strongman's and Beast Tamer's marks move to their ring centres.
- **Guy lines.** Guy stakes that would fall inside a yard or its bank are left out (the long walls keep theirs).
- **After-states.** The ruin and tenancy overlays (`site_states.py`) are rebuilt at the new size. The ruin also reaches
  the yards modestly: a weight frame down, a run of curb gone, half the yard footlights out.
- **Terrain gate (owner ruling).** `gate_inset` goes from 11 to 19, so the gate checks the same 43-block core as today
  and placement does not change; the yards sit on beard_box ground as the old ones did.

## Left to the builder

- Exact yard shape constants, bank height, footlight angles, lamp-post spots, apparatus spots round the ring.
- How the offset is applied in `pavilion.py` (an offset frame or site wrapper), so long as `pavilion.py`'s existing
  literal coordinates do not all have to be rewritten.

## Verification

- `python tools/worldgen/circus_sites.py` writes every template with its checks passing (states, metals, multiblocks,
  flats, light).
- Unit tests updated: `CircusPavilionTemplateTest` (size 81 × 23 × 65, every actor and record still placed),
  `TroupeFootprintScanTest` if it pins the pavilion width, the site-state tests on `CAROUSEL` and `PODIUM` (both shift
  +8 in x), `CircusWorldValidation`'s pinned `{65, 65}`, and the review client's and playthrough tests' coordinates.
- GameTests: `CrimsonTroupeStructureGameTests`, the Ringmaster and finale suites, `ringmasterReachFromTheFloor`.
- `circusValidationServer` on a measured seed still places a pavilion (the gate core is unchanged).
- `circusReviewClient -PcircusReviewOnly=pavilion,sites` stills: both yards by day and night, the ruin on a yard.
- Docs: `docs/CIRCUS.md` (sizes, the pavilion paragraph, a dated entry), `docs/HEMOMANCY_REFERENCE.md` if it gives the
  size.
