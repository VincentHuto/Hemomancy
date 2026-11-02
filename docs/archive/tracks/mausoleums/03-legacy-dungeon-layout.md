# Track 3 — Legacy-dungeon layout

**Goal:** each complex is a dungeon, not a corridor of 11×11 boxes. It spirals down from the entrance through wings and galleries, each Chapel is a wing with its trial spread across several rooms, and the whole thing reads as the Saint's life walked backwards (entrance = their rest, chamber = where it began).

Owns `tools/worldgen/mausoleum_complexes.py` while it runs. Uses Track 7's kit when it lands; until then, keep rooms as shells and concentrate on topology and scale.

## Current state
- Chain: top → stairs → antechamber → descent (Track 1: 19×14×11 ossuary stair, drops 7) → gallery (Track 1: 27×12×25 nave-and-aisles loculi gallery with the iron-door treasure room) → seal hall → nave (19×8×11, three 3×3 doorways) → chapel_0/1/2 (9×7×9 boxes, focus at centre, four marks) → chamber (11×8×11).
- The seal hall, nave, Chapels and chamber sit on one level, 17 blocks below the entrance floor. Nothing descends after the descent piece. The gallery and descent are built with local helpers in the generator (`arch`, `loculus`, `field`, the `CRYPT` role palettes) that Track 7's kit should absorb.
- `size` 8, `max_distance_from_center` 116. `StructurePlacementChecks.MAUSOLEUM_FOOTPRINT_RADIUS` 72.
- `ChapelFocusBlockEntity` runs the whole trial from one block in one room: Beholding corner marks at ±2, Litany offerings at the focus, Downfall spawns at the corner marks, Repose within 3.5 blocks.

## Target shape
- **Vertical.** From the antechamber the complex descends: a stair shaft or ramp spiral of 2–3 turns (each turn a piece: `descent_turn`, with `down_*` jigsaws), landing on a **threshold hall** (Track 2) 12–18 blocks below the surface. From there a **nave** at the centre with the ring door at its far end and the Chapel wings branching off at different heights: one level with the nave, one a half-flight up (a gallery looking down into the nave), one a flight down (a crypt wing under the nave). The chamber lies past the door, a few steps down, as the lowest room.
- **Wings.** A Chapel is 3–5 pieces: an approach (corridor or stair), one or two rooms where the trial plays, and the focus room at the end. Rough extents 20–30 blocks per wing. Dead-end alcoves and side niches (loot, inscriptions, set pieces) hang off the approach so wings do not feel like a hallway to a room.
- **Scale.** Nave 25×12×15 or larger with a vaulted ceiling and a gallery level; chamber per Track 4; corridors 3 wide 4 tall minimum; big rooms 7+ tall.
- **Variation.** Each wing kind has two or three layout variants in its pool, with weights; the trial rooms are fixed per kind, the approaches vary.

## Trials across a wing
Keep `ChapelRules` and the focus BE's state machine; move what each trial touches:
- **Beholding.** The memory plays along the wing: four **witness** set pieces (Track 6) in the approach rooms flash in sequence; the player walks back to each in order. The focus BE keeps a list of witness positions (written by the generator into its NBT) instead of the ±2 corner constants.
- **Litany.** The wing is a scriptorium/ossuary: the inscription is read from a lectern-like **litany stone** at the wing's start, the offerings are given at the focus at the end; the six candidate items sit on display stands along the way so the puzzle is "which three, in what order", not a scavenger hunt.
- **Downfall.** A gauntlet: waves spawn at **spawn marks** down a long hall as the player advances (the BE tracks the challenger's position), ending at the focus.
- **Repose.** A hall of stillness: the pulses come from **pulse pylons** around the room (Track 6), not from the focus; the still radius becomes a marked circle in the floor.

The generator writes every mark's offset into the focus BE's NBT (`Marks: [{x,y,z,Kind}]`); the BE no longer hard-codes geometry. `ChapelFocusBlockEntity.CORNERS` goes.

## Jigsaw mechanics
- Raise `size` to 12–14 and `max_distance_from_center` to 160; raise `MAUSOLEUM_FOOTPRINT_RADIUS` to match the new extent (measure).
- Vertical connections use `down_*` / `up_*` jigsaw orientations; keep each vertical piece's connector on its bounding box edge.
- Every optional branch (niches, galleries) uses a pool with a `fallback` so a blocked branch produces a wall niche, never a hole into rock. Mandatory pieces (wings, nave, chamber) must never rely on fallback: the Track 0 assembly GameTest asserts them with four seeds.
- Back-connectors point at `minecraft:empty` (deferred minor from the first pass).
- `MausoleumComplexResourceTest.theNavesChapelsNeverOverlap` generalises to a box-intersection check over the whole chain for each seed.

## Generator shape
Refactor `mausoleum_complexes.py` into: `kit.py` (Track 7's architectural modules), `trials.py` (mark layouts per Chapel kind), `pieces.py` (room builders), `themes/<saint>.py` (palette, motifs, set-piece placement; theme tracks own these), `build.py` (pools, structures, tags). Keep it re-runnable from the repo root and keep `DATA_VERSION` at 3955.

## Acceptance
- Walking from the entrance to the chamber takes a few minutes and changes level at least three times.
- Each Chapel trial is completable solo in a client and uses its wing (Track 0 screenshots per wing).
- The assembly GameTest places every wing, the nave and the chamber for four seeds.
- No room is a bare box: every piece passes through the kit (once Track 7 exists).
