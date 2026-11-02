# Saint Mausoleum Complexes (Section 5) — Design

Date: 2026-10-28. Source: `docs/Fungal Gardens Purpose & Mausoleum Structure_V2.md`, "Layout and degree tiers" and "Walk the Saint's life backwards". Overnight run, so every decision below is a ruling made on the user's behalf. Sections 6 and 7 fill in the Chapels and the ring door that this section places.

## Goal

Four Saint mausoleums generate in the world. Each is a jigsaw complex built from the shared parts, dressed in its Saint's palette:
- an entrance and antechamber anyone can explore;
- an outer trial hall (D3–4) with early loot;
- an inner seal only D8 players pass;
- a nave with three Chapel slots and the ring door;
- the Saint's chamber.

## Decisions

### D1. Structures
- **One structure per Saint:** `hemomancy:mausoleum_<saint>`, using the existing `hemomancy:mausoleum` structure type and its buried-site check.
  - Start pool: `hemomancy:mausoleum/<saint>/top`.
  - Size 8, otherwise the same JSON as the old mausoleum.
- **One structure set**, `hemomancy:saint_mausoleums`: random spread 40/12, all four Saints at weight 1.
  - The old `hemomancy:mausoleum` structure, set and empty biome tag stay as they are (still disabled, still pinned by tests).
- **Biomes** (`has_structure/mausoleum_<saint>`):

| Saint | Biomes |
| --- | --- |
| Hemorath | badlands, wooded and eroded badlands, savanna, savanna plateau |
| Seraphae | meadow, cherry grove, plains, sunflower plains |
| Putriciel | swamp, mangrove swamp, dark forest |
| Velorum | snowy plains, snowy taiga, ice spikes, grove, snowy slopes |

### D2. The chain (jigsaw)
The chain runs in this order:
1. `top`
2. `stairs`
3. `antechamber`
4. `trialroom`
5. `seal`
6. `nave`
7. Three Chapel slots off the nave, and the `chamber` beyond the ring door.

- **Copied pieces.** `top`, `stairs`, `antechamber` and `trialroom` are the existing hand-built templates, copied per Saint.
  - Their jigsaw pools are re-pointed to the Saint's own pools, and their materials are swapped to the Saint's palette.
  - The coloured-wool markers are replaced with the palette's wall block.
  - The 4-way variants are not used.
- **Antechamber barrels** carry `hemomancy:chests/mausoleum`. This finally wires in the orphaned table.
- **Seal hall** (generated):
  - Its outer half holds the outer reward: a barrel with `hemomancy:chests/mausoleum_treasure`, reached once the trial's offering gate opens.
  - A wall of **mausoleum seal** blocks crosses it.
- **Nave** (generated, 15 long):
  - Three Chapel doorways, each a jigsaw into `chapel_0`, `chapel_1` and `chapel_2`.
  - The **ring door** fills the east doorway, and the chamber jigsaw lies beyond it.
- **Chapels** (generated): a small room with a **chapel focus** at its centre. Its kind and slot are written into the template. Section 6 gives the focus its behaviour.

| Saint | Chapel slot 0 | Chapel slot 1 | Chapel slot 2 |
| --- | --- | --- | --- |
| Hemorath | Beholding | Litany | Downfall |
| Seraphae | Beholding | Downfall | Repose |
| Putriciel | Litany | Downfall | Repose |
| Velorum | Beholding | Litany | Repose |

- **Chamber** (generated): the Saint's sarcophagus (`SaintType` set, DORMANT) on a dais, with a signature set piece:
  - Hemorath: blood basins.
  - Seraphae: hanging chains.
  - Putriciel: soul campfires.
  - Velorum: a frozen congregation of kneeling armor stands.

### D3. Palettes

| Saint | Look |
| --- | --- |
| Hemorath | the existing venous stone and deepslate |
| Seraphae | quartz and calcite, lit by sea lanterns |
| Putriciel | mud brick and moss, with blackstone kept |
| Velorum | packed and blue ice over deepslate, lit by sea lanterns |

### D4. Blocks added here
- **`mausoleum_seal`.**
  - Unbreakable and explosion-proof.
  - A D8 player's right-click opens every seal joined to it (at most 16) for 100 ticks. Open seals have no collision; they then close.
  - Anyone else gets "The seal does not know you."
- **`ring_door`.** Unbreakable and explosion-proof, and solid for now. Section 7 makes it open.
- **`chapel_focus`.** An unbreakable altar block. Section 6 gives it a block entity and behaviour.

### D5. Generator
`tools/worldgen/mausoleum_complexes.py` writes every per-Saint template and pool, and is re-runnable.
