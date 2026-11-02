# Track 10 — Theme: Putriciel

Depends on Tracks 3, 6 and 7. Owns `tools/worldgen/themes/putriciel.py` and Putriciel's assets.

## Status (2026-10-28)
Built; ledger `.superpowers/sdd/2026-10-28-mausoleum-rework-10/progress.md`. New blocks: the burned/rotten family (§24.12), `rot_pyre` burns Flammeus shader fire from a block entity, `reliquary_sconce` `flame=smoulder`, `pulse_pylon` `style=pyre`, `litany_stone` `charred`, `spawn_mark` `style=ash`; the fight turns the chamber between pyres and spore glow (`PutricielChamberLightingRules`); the reference is §5.6g "Putriciel's complex".

## Who he was
Mortem and Flammeus. His gift was consecration corruption: the rot that follows a purge and comes back stronger. His complex was burned and rebuilt in cycles until they buried it. Never associated with the Order. Walked backwards: the entrance is the last pyre, the chamber is the first field he blessed.

## Palette and vocabulary
- Family: mud bricks, packed mud, mossy stone bricks, blackstone, bone bricks (Track 7 adds the family), charred wood; hematic-iron braziers; dark lacquer. Floors mossy stone with ash inlays.
- Motif: **rot and burn in bands.** Every hall alternates charred bands (blackstone, ash, `rot_pyre`s) with overgrown bands (moss, hanging roots, fungal shelves from the mod's own blocks: `infected_cap` fringe, `hyphae_block` patches). The banding gets tighter toward the chamber.
- Light: `rot_pyre`s and smouldering sconces; the overgrown bands glow faintly green from spore blocks.
- Ambience: crackling in the burned bands, wet creaking in the rotten ones; a periodic distant flare.

## Wings (Litany, Downfall, Repose)
- **Litany — the Burned Library.** A scriptorium where half the shelves are ash; the litany stone is charred and only its surviving lines name the offerings; the `offering_stand`s hold the candidates among scorched remains.
- **Downfall — the Purge Corridors.** A gauntlet that alternates: in burned sections bog revenants rise from `spawn_mark`s in the ash; in rotten sections the floor is slow (soul-sand-like `wall` variant) and the roots hang low.
- **Repose — the Ash Garden.** A still room where the `pulse_pylon`s are pyres: each pulse is a purge sweeping the room; standing still in the ash circle is the only safe ground.

## Chamber
A rotted field under a vault: the tomb's dais is an island of mud brick in a floor of moss and roots, four `rot_pyre`s at the corners, the vault's keystones charred. His burn phases (the fight) alternate the room's lighting between the pyres and the spore glow.

## Tasks
1. `themes/putriciel.py`: palette, kit theme, banding passes, root and shelf placement.
2. `rot_pyre` (Track 6) and the bone-brick family (Track 7) if not yet present.
3. Sarcophagus palette variant: charred chest, bone straps, moss in the carvings.
4. Track 0 stops.
