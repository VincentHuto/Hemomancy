# Saint Mausoleum rework — work paths

Date: 2026-10-28. The overnight Section 5–7 build (`docs/superpowers/specs/2026-10-28-mausoleum-complexes-design.md`, generator `tools/worldgen/mausoleum_complexes.py`) produced a working but low-quality pass: small box rooms, a 3×3 seal, a 3×3 ring door, armor stands as a congregation, flat single-block walls, and a legacy offering-gate trial. The quality bar is a **legacy dungeon**: a complex that spirals down through themed wings, with a door and a tomb that read as set pieces, not as tiles.

Each file below is one independent track. A session takes one track, reads this README first, and leaves the others alone. Where a track depends on another, it says so at the top.

| # | Track | File | Depends on |
| --- | --- | --- | --- |
| 0 | Review scaffold (spawn every complex, screenshot it) | [00a-review-scaffold.md](00a-review-scaffold.md) | — |
| 1 | Placement depth and the outer halls (drop the offering gate) | [01-placement-and-outer-halls.md](01-placement-and-outer-halls.md) | — |
| 2 | Consecrated ward: no seal block; glow layer + hit effect | [02-consecrated-ward.md](02-consecrated-ward.md) | — |
| 3 | Legacy-dungeon layout: spiral, wings, bigger Chapels | [03-legacy-dungeon-layout.md](03-legacy-dungeon-layout.md) | 7 (kit) helps, not required |
| 4 | Sarcophagus and chamber at scale | [04-sarcophagus-and-chamber.md](04-sarcophagus-and-chamber.md) | — |
| 5 | Ring door multiblock with animated rings and nodes | [05-ring-door.md](05-ring-door.md) | — |
| 6 | Set-piece blocks (congregation and the rest), no armor stands | [06-set-piece-blocks.md](06-set-piece-blocks.md) | — |
| 7 | Architectural detailing kit | [07-architectural-detailing.md](07-architectural-detailing.md) | — |
| 8 | Theme: Hemorath | [08-theme-hemorath.md](08-theme-hemorath.md) | 3, 6, 7 |
| 9 | Theme: Seraphae | [09-theme-seraphae.md](09-theme-seraphae.md) | 3, 6, 7 |
| 10 | Theme: Putriciel | [10-theme-putriciel.md](10-theme-putriciel.md) | 3, 6, 7 |
| 11 | Theme: Velorum | [11-theme-velorum.md](11-theme-velorum.md) | 3, 6, 7 |

Tracks 1, 2, 4, 5, 6 and 7 can run at the same time. Track 3 reshapes the generator, so a session on 3 should own `mausoleum_complexes.py` while it runs; the theme tracks come after 3 lands and each owns one Saint's section of the generator plus that Saint's assets.

## What stays

- `MausoleumStructure` / `HemoJigsawStructure`, the four `mausoleum_<saint>` structures, the `saint_mausoleums` set and the biome tags.
- `MausoleumEvents` (no break, place, explosion or piston inside a hallowed piece; hallowed-ground burn for an Apotheos).
- The Chapel trial logic in `ChapelFocusBlockEntity` and `ChapelRules` (Beholding, Litany, Downfall, Repose). Track 3 spreads each trial across a wing; the rules carry over.
- `RingDoorRules`, the tomb's door record (`SaintSarcophagusBlockEntity.DoorMode`, skipped Chapels) and `SaintEnrageRules`. Track 5 replaces the block, not the rules.
- Loot wiring (`chests/mausoleum`, `chests/mausoleum_treasure`), the Recollections, the residuum fragments.

## What goes

- `MausoleumSealBlock`, `MausoleumSealRules`, their tests and the two seal GameTests (Track 2).
- The `trialroom` copies and the `blood_trial_altar` / `offering_gate` loop in the chain (Track 1).
- `RingDoorBlock` as a 3×3 of cube blocks and the `ring_door_0..3` textures (Track 5).
- Armor-stand entities in the generator (`kneeling_stand`) (Track 6).
- The plain `Room.shell()` box rooms (Track 7 gives the kit; Track 3 uses it).

## Shared rules for every track

- Read `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md` first. Lang keys only; particles through `HemoParticleData`; render types as static constants or `SeededRenderTypes`; world-space drawing from `WorldEffectDispatcher`; `AnimationTime` for animation; no copper, brass or bronze on anything blood-side (hematic iron, bone, lacquer, venous stone instead).
- 16 px per block texel density for every new texture and model.
- Block models are authored by a generator in `tools/model_export/` (see `consecrated_bloodwell.py`, `saint_sarcophagus.py`, `station_kit.py`) that also writes a `.bbmodel` and a preview PNG. Hand-written JSON models are not acceptable for set pieces.
- Structure templates come from `tools/worldgen/mausoleum_complexes.py`. Change the generator, re-run it, never hand-edit an NBT.
- Every piece must be checked in a client (Track 0's scaffold). A track is not done on tests alone.
- Frozen fixtures (`registry-ids.txt`, `creative-tab-order.txt`), the itemless-block list in `RegistrationGameTests` and `BlockInit.ITEMLESS_BLOCKS` change with every new block.
- No commits; leave work in the tree.

## After the rework

All eleven tracks finished on 2026-10-28. [12-next-pass.md](12-next-pass.md) sums up what was built, the rulings that
still apply, and what is open for the next pass. Start there.

## Ledgers from the first pass

`.superpowers/sdd/2026-10-28-mausoleum-complexes/progress.md`, `…/2026-10-28-mausoleum-chapels/` and `…/2026-10-28-saint-awakening/` hold the rulings and the deferred minors of the overnight build. Read the one for your track before starting; several deferred minors become in-scope here.
