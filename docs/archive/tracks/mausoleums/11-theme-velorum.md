# Track 11 — Theme: Velorum

Depends on Tracks 3, 6 and 7. Owns `tools/worldgen/themes/velorum.py` and Velorum's assets.

## Status (2026-10-28)
Built; ledger `.superpowers/sdd/2026-10-28-mausoleum-rework-11/progress.md`. New blocks: the frozen monastery family (§24.13: rimed deepslate bricks/stairs/slab/wall, `rime_lantern`, `hanging_icicle`, `frozen_drip`, dark lacquer planks), `witness_devotee`; `frozen_devotee` pose 4 `striding` and `head`; `offering_stand` `style=bowl`, `pulse_pylon` `style=frost`; Velorum's tomb palette and ambience. Litany and Repose are his own halls (`HALLS`: the Rule, the Cell Row). The fight's congregation thaw is not built (Section 8). The reference is §5.6g "Velorum's complex".

## Who he was
Congeatio and Tenebris. Perfection through stillness: he stopped, and his congregation stopped with him. Nobody built his complex for him; it is his own monastery, frozen around him. Never associated with the Order. Walked backwards: the entrance is the gate the last devotee closed, the chamber is the cell where his stillness started.

## Palette and vocabulary
- Family: packed ice, blue ice, deepslate bricks and tiles, dark lacquer; hematic iron only in the fittings. Floors deepslate tile with blue-ice inlay.
- Motif: **the frozen congregation.** `frozen_devotee` blocks (Track 6) everywhere: in the halls, kneeling in rows in the nave, prostrate at the chamber. Their poses mark safe paths and answers (the Repose Chapel's rules). Rime crusts every ledge (a `trim` variant with frost on top faces), icicles from the vaults (`trim` stairs hung upside down in the kit's vault module), frozen drips down the pilasters.
- Light: blue-white, recessed sea lanterns behind ice; the devotees' frost is faintly full-bright. Dim overall; the nave is the only well-lit room.
- Ambience: near silence; a creak of ice; the player's own footsteps muffled (a low-pass on the footstep sound inside, if the audio system allows; else just the silence).

## Wings (Beholding, Litany, Repose)
- **Beholding — the Procession.** A long hall of devotees frozen mid-step; the `witness_stone`s are four of them that still turn their heads (a `lit` state flips to a turned variant). Memory replayed: the four who followed him into stillness.
- **Litany — the Rule.** The monastery's rule carved into the litany stone; the `offering_stand`s are frozen bowls on a refectory table.
- **Repose — the Cell Row.** A corridor of monks' cells; the `pulse_pylon`s are frost pillars; the still circle is a blue-ice inlay before his own cell door. Devotee poses in the cells show which cells are safe during pulses (Section 8's "poses mark safe paths" rule).

## Chamber
The nave of his monastery inverted into his cell: a vast vaulted room, devotees kneeling in rows facing the dais, the tomb an ice-crusted chest. The fight's "congregation thaws" (Section 8 follow-up) turns `frozen_devotee` blocks into entities in place; a proper awakening wakes them on the player's side.

## Tasks
1. `themes/velorum.py`: palette, kit theme, rime and icicle passes, devotee placement (poses from a small layout table per room).
2. `frozen_devotee` (Track 6) with four poses and a turned-head variant.
3. Sarcophagus palette variant: ice-crusted chest, frost in the carvings, blue-white veins.
4. Track 0 stops.
