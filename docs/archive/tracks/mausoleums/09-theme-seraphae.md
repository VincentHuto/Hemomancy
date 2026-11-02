# Track 9 — Theme: Seraphae

Depends on Tracks 3, 6 and 7. Owns `tools/worldgen/themes/seraphae.py` and Seraphae's assets.

## Status (2026-10-28)
Built; ledger `.superpowers/sdd/2026-10-28-mausoleum-rework-09/progress.md`. The Repose is her own round hall (`HALLS`); the kit gained `swollen_pillar`; the reference is §5.6g "Seraphae's complex".

## Who she was
Lux and Ductilis. Cursed, uncontrollable healing: everything she touched grew. Her people contained her because they could not kill her; her complex is a containment, not a tomb. Never associated with the Order. Walked backwards: the entrance is the last ward they built, the chamber is the infirmary where she first laid hands on someone and could not stop.

## Palette and vocabulary
- Family: quartz bricks, smooth quartz, calcite, diorite walls; hematic-iron chains and anchors; bone; pale lacquer. Floors smooth quartz with calcite inlay.
- Motif: **chains and overgrowth.** `containment_anchor_block`s stand in every hall with chains (vanilla chain blocks are acceptable as the links between anchor blocks) running to the walls and ceiling. Where her radiance reached, the stone has grown: flesh-pale "healed" patches of `wall` variants, bone outgrowths, swollen pillars (a pillar module that bulges at mid-height).
- Light: sea lanterns recessed behind diorite grilles; the deeper halls glow from the walls themselves (full-bright patches).
- Ambience: a faint choir drone; the anchors tick.

## Wings (Beholding, Downfall, Repose)
- **Beholding — the Ward Walk.** A gallery of anchors; the witness stones are patients' beds carved in quartz. Memory replayed: four touches, four growths.
- **Downfall — the Infirmary Row.** A long ward of beds; waves of vexes rise from `spawn_mark`s between the beds. Broken anchors lie along the way (an `anchor` variant with a snapped chain).
- **Repose — the Still Ward.** A circular room where the `pulse_pylon`s are anchors: each pulse is her reaching to heal; standing still is refusing her hand. The still circle is a calcite inlay ring.

## Chamber
A tall infirmary nave: the tomb on its dais under the largest anchor, chains from the four `containment_anchor_block`s meeting above it, bone growths climbing the pillars. Her fight's anchors (`ContainmentAnchorEntity`) spawn at the block anchors' positions.

## Tasks
1. `themes/seraphae.py`: palette, kit theme, chain/anchor and overgrowth passes.
2. `containment_anchor_block` and its snapped variant (Track 6 table).
3. Sarcophagus palette variant: pale quartz chest with bone straps.
4. Hook the fight's anchor spawn positions to the chamber's anchor blocks.
5. Track 0 stops.
