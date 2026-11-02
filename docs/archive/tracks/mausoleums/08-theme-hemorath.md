# Track 8 — Theme: Hemorath

Depends on Tracks 3 (layout), 6 (set pieces) and 7 (kit). Owns `tools/worldgen/themes/hemorath.py` and Hemorath's assets.

## Status (2026-10-28)
Built; ledger `.superpowers/sdd/2026-10-28-mausoleum-rework-08/progress.md`. Two generic theme hooks were added for it (`HALLS` and `motif`, documented in `themes/__init__.py`); the reference is §5.6g "Hemorath's complex".

## Who he was
A powerful early Hemomancer, never a true Harbinger. Animus and Ferric. His gift was eternal bloodlust, the affliction, and it was his downfall. His successors' fear of ending like him shaped the tradition Erythravane later organised into the Hematic Order, so of the four complexes his is the only one the Order itself built and keeps. Walked backwards: the entrance is the Order's reliquary, the chamber is the pit where the first of them bled him dry to make him stop.

## Palette and vocabulary
- Family: the venous-stone set (polished, bricks, cracked, chiseled, stairs, slab, wall), hematic-iron trims, bone accents, crimson lacquer. Floors deepslate tile with venous-stone inlays. No ice, no quartz.
- Motif: **basins and channels.** Blood gutters run in the floors toward the chamber; every hall has a dry basin; the deeper the hall, the wetter the channel (rendered blood surface in the lowest ones). The ring door's signature: the Silentium's blood visibly runs through the seam to him.
- Light: `reliquary_sconce` with a deep red flame; the chamber's cressets.
- Ambience: a slow heartbeat the deeper you go (a looping sound at low volume, scaled by depth); drips.

## Wings (Beholding, Litany, Downfall)
- **Beholding — the Reliquary.** A gallery of sealed niches holding the Order's relics of him; four `witness_stone` steles carved with his students. The memory replayed: the students sealing the vessel one by one. Side niches hold `chests/mausoleum` loot.
- **Litany — the Ration Hall.** A scriptorium where the Order rationed his absorption: the litany stone is a ledger, the `offering_stand`s show the six candidates as rations on iron trays. Basins along the walls, half full.
- **Downfall — the Draining Pit.** A long descending hall with basins at every landing; waves of hematic constructs rise from `spawn_mark`s in the channel as the player descends. The hall ends in the focus set into the last basin.

## Chamber
Per Track 4's great room: the dais stands in a dry round basin with channels radiating to four wall basins (`blood_basin`); the processional floor is a single long gutter. Hemorath's swelling (Section 8 follow-up) can use the wall basins as his refill.

## Tasks
1. `themes/hemorath.py`: palette, kit theme record, motif passes (gutters, basins, niches), set-piece sockets.
2. Textures for any missing venous-stone family pieces (Track 7 may have added them).
3. Sarcophagus palette variant (Track 4's `--saint`).
4. Sound: a heartbeat ambience hook (reuse the Hemorath siphon's audio if it has one).
5. Track 0 stops for all three wings and the chamber.
