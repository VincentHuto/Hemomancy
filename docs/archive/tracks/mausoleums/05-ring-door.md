# Track 5 — The ring door

**Goal:** a dragon-claw-style set-piece door: one multiblock whose three concentric rings turn and glow as the Chapels light their nodes, fill with blood when the Silentium projects into it, and fall away; or crack and burst when the Apotheos shatters it.

## Current state
- `RingDoorBlock`: a plain cube with `nodes` 0–3 and `falling`, placed as a 3×3 of identical blocks (`ring_door_0..3.png` textures). Click handling, node refresh from nearby Chapels, blood payment (3 × 300 through `BloodAccounting`, source `ring_door`), the `PROPER` / `FORCED` record on the tomb and the skipped-Chapel list all live here and are correct; only the presentation is wrong.
- `RingDoorRules` is pure and tested.

## Target
- **Multiblock.** A controller block at the bottom centre with fillers for a **5 wide × 5 tall** face (1 deep), after the sarcophagus pattern (`IMultiBlock`, `FillerBlock`, lazy fillers for structure placement). Facing from the nave.
- **Model.** Authored by a generator in `tools/model_export/ring_door.py` (station-kit style, `.bbmodel` + preview): a venous-stone door slab in a hematic-iron frame, three concentric rings cut into it (outer, middle, inner), each ring carrying one **node** (a bone-rimmed lacquer boss) and a channel for blood. The rings are separate model parts the BER rotates.
- **BER** (`client/render/tile/harbinger/puzzle/RingDoorRenderer`): draws the slab, the three rings at their current angle, the nodes (dark, or lit with a full-bright glow and a `HemoParticleData` mote every few seconds), the blood fill as a translucent channel that advances around each ring, and the opening animation. Time comes from `AnimationTime`; nothing per-frame allocates a render type.
- **States** (block entity, synced with `BlockEntitySync.markAndSync`):
  - `nodes` lit: each lit node turns its ring to align its notch with the door's vertical seam (Chapel 1 outer, 2 middle, 3 inner), a 40-tick eased turn with a stone-grind sound.
  - `projecting`: the Silentium's click starts a 3-stage cutscene: blood runs from the player's hand into the outer ring (ring fills over 40 ticks, 300 blood paid at the start of each stage), then the middle, then the inner; a pulse of crimson through the seam; then the slab sinks into the floor over 40 ticks and the fillers clear. For Hemorath the blood visibly runs on through the seam toward the chamber (his signature).
  - `shattering`: the Apotheos's click sends hyphae cracks (a seeded crack texture layer growing over 30 ticks) across the slab, then it bursts into fragments (`HemoParticleData` burst + a few falling block-fragment cards) and the fillers clear.
  - `open`: nothing remains but the frame and the floor track.
- **Rules.** Move the stage timings into `RingDoorRules` (pure, tested): `turnTicks`, `fillTicks`, `stageCost`, `shatterTicks`.

## Tasks
1. Generator and model, previewed before any Java.
2. Controller block + BE + fillers; move the click logic from `RingDoorBlock` into the BE's stage machine; delete the cube block and the four textures; update `BlockInit`, itemless lists, fixtures, lang.
3. BER with the ring, node, fill and opening animations.
4. Generator: the nave's door socket becomes a 5×5 opening with the controller at its base; the chamber side gets the floor track the slab sinks into.
5. GameTests: `ringDoorLightsANodePerCompletedChapel` and `shatteredRingDoorTellsTheTombWhatWasSkipped` move to the BE API; add one for the projecting stages (blood paid per stage, open at the end).
6. Client proof: Track 0 stops for closed/1/2/3 nodes, mid-projection, shattering.

## Acceptance
- One door, 5×5, with moving rings and glowing nodes; the Silentium opening is a sequence the player watches, the Apotheos shattering is a burst.
- Blood accounting, the tomb record and the enrage path behave exactly as before (the rules tests are unchanged).
