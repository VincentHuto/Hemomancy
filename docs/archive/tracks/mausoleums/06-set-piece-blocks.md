# Track 6 — Set-piece blocks

**Goal:** everything that dresses a mausoleum is a modelled block with a generator, not an entity and not a vanilla stand-in. Velorum's congregation in particular is custom blocks and models, not armor stands.

## Current state
- `mausoleum_complexes.py` writes `minecraft:armor_stand` entities with a kneeling pose and packed-ice heads for Velorum's congregation (nave and chamber).
- Other set pieces are vanilla: chains (Seraphae), soul campfires (Putriciel), `blood_basin` (Hemorath, fine), tinted glass / lectern / cracked bricks / snow blocks as Chapel kind markers.
- Existing effigy-style blocks to model the approach on: `vacant_effigy`, `mason_effigy` (2-tall modelled blocks), `iron_brazier`, `mnemonic_candle`, `ossuary_clock`. Generators for these live in `tools/model_export/`.

## Blocks to add
Each is a `MODELEDBLOCKS` entry with a generator in `tools/model_export/` (model, blockstate, `.bbmodel`, preview), 16 px/block, no copper or brass, and a frozen-fixture entry. Itemless unless noted.

| Block | Used by | Notes |
| --- | --- | --- |
| `frozen_devotee` (2 tall, `facing`, `pose` 0–3: kneeling, bowed, reaching, prostrate) | Velorum nave and chamber | Rime-crusted robed figures, hands clasped; faint full-bright frost in the folds. Track 11's congregation; Section 8's "thaws and fights" later turns one into an entity in place. |
| `witness_stone` (1 tall, `lit`) | Beholding wings | A carved stele with an eye that lights when the memory touches it; the BE-less block flips `lit` from the focus. |
| `litany_stone` (2 tall, `facing`) | Litany wings | A standing inscription slab with an Abocipher face; the focus BE reads its inscription key from here. |
| `offering_stand` (1 tall, holds an item) | Litany wings | A bone-stemmed display stand with a BE holding one item for the player to read the six candidates. |
| `pulse_pylon` (3 tall, `facing`, `charge` 0–3) | Repose wings | The pylon that fires Repose's pulse; the BER flares the charge ring up before each pulse. |
| `spawn_mark` (floor plate, `kind`) | Downfall wings | A cracked floor seal the wave rises from; emits a `HemoParticleData` seep while a wave is due. |
| `containment_anchor_block` (2 tall) | Seraphae chamber and wings | The chain anchor pylon as architecture (the `ContainmentAnchorEntity` already exists for the fight; this is its dormant, built form). |
| `rot_pyre` (1 tall, `lit`) | Putriciel | A bone pyre with embers; replaces the soul campfires. |
| `sealed_niche` / `opened_niche` (wall blocks) | Outer crypt galleries (Track 1) | Niche fronts: a sealed bone-and-lacquer plate, and an open one with a shelf for a loot barrel. |
| `reliquary_sconce` (wall, `facing`) | All, lighting | Hematic-iron wall sconce with a lacquered bone-oil flame; the standard mausoleum light. |

Hemorath keeps `blood_basin`; theme tracks may ask for more and add them to this table.

## Tasks
1. One generator per block in `tools/model_export/`, previewed before registration.
2. Register, with blockstates, loot (none unless noted), lang, fixtures, and the itemless lists.
3. BE or BER only where the table says (pulse pylon, offering stand, witness/litany stones are state-only).
4. Replace the armor-stand entities in the generator with `frozen_devotee` placements; delete `Room.kneeling_stand`.
5. Replace the vanilla stand-ins (chains, campfires, markers) with the table's blocks in the generator's theme files.

## Acceptance
- No `entities` list in any mausoleum template.
- Each new block has a preview PNG checked in a client at 16 px/block.
- Track 3's wing trials read their marks from these blocks.
