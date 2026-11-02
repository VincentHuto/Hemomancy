# Gardens Facsimiles (Section 2a) — Design

Date: 2026-10-28. Source: `docs/Fungal Gardens Purpose & Mausoleum Structure_V2.md` (the Archive layer: "the hive's warped recreations of Earth life") and `docs/Endgame Path Asymmetry …_V2.md` (the surrogate/facsimile table). This is the first slice of Section 2. Possession (Section 4) later borrows these bodies; here they are ordinary Gardens fauna.

## Goal

Eight facsimiles of Earth creatures roam the Fungal Gardens dimension. Each is recognisably the original and visibly wrong.

## Decisions

### D1. Roster

| Id | Base class | Category | Temperament |
| --- | --- | --- | --- |
| `facsimile_bat` | `Bat` | AMBIENT | Harmless flier |
| `facsimile_enderman` | `EnderMan` | MONSTER | Vanilla enderman, but never picks up or places blocks |
| `facsimile_blaze` | `Blaze` | MONSTER | Vanilla blaze |
| `facsimile_golem` | `IronGolem` | CREATURE | Neutral: wanders, fights back when struck, never targets monsters or defends villages |
| `facsimile_horse` | `Horse` | CREATURE | Wanders; cannot be tamed, ridden, fed or bred |
| `facsimile_donkey` | `Donkey` | CREATURE | As the horse; cannot take a chest |
| `facsimile_wolf` | `Wolf` | CREATURE | A wild wolf that cannot be tamed or bred |
| `facsimile_spider` | `Spider` | MONSTER | Vanilla spider |

- All eight refuse player interaction through `mobInteract` (they return PASS), and cannot breed.
- The armor-stand "fungal mannequin" waits for Section 4. It exists only as a possession body.

### D2. Spawning
- Spawns are added only to the seven dimension biomes, through `tools/worldgen/fungal_gardens_worldgen.py` `BIOME_SPAWNS`. The Overworld `fungal_gardens` biome is untouched.
- Each spawns by the Gardens' own rule: `Mob.checkMobSpawnRules` (solid ground below, no light requirement). They are the Gardens' fauna, not darkness monsters.
- The bat uses `NO_RESTRICTIONS` placement with the same rule.

| Biome | Spawns (weight, min–max) |
| --- | --- |
| mycelial_meadows | creature: horse 6 (1–3), donkey 4 (1–2), wolf 5 (2–4) |
| fungal_isles | creature: wolf 4 (1–2) |
| sporecrown_thicket | monster: spider 8 (1–2); ambient: bat 8 (2–4) |
| hyphal_spires | monster: enderman 6 (1–2); ambient: bat 6 (2–3) |
| drifting_mycelium | monster: enderman 4 (1–1) |
| mycelial_depths | monster: spider 6 (1–2), enderman 3 (1–1); ambient: bat 8 (2–4) |
| hemorrhagic_plateau | monster: blaze 6 (1–2); creature: golem 2 (1–1) |

### D3. Drops
- Every facsimile drops 0–2 `hemomancy:spore_sac` (looting adds 0–1), only when killed by a player.
- No vanilla drops: no leather, pearls, rods, iron or string. A facsimile is fungus wearing a shape.

### D4. Look
- Each renders with the vanilla model layer and vanilla texture, read at runtime. No vanilla asset is copied into the repo.
- **Growth layer:** the model is drawn a second time with our own generated overlay texture. It has the same size as the vanilla texture (bat 32×32; enderman, blaze, wolf and spider 64×32; horse and donkey 64×64; golem 128×128), with fungal blotches (pale caps, crimson hyphae) where its UVs fall. The RenderTypes are static constants in `FacsimileRenderTypes`.
- **Wrong proportions:** each entity's UUID seeds absolute per-part scales.
  - `FacsimileDeformationRules.scaleFor(seed, partIndex)` gives about 40% of parts a scale of 0.8–1.3 on each axis. The rest stay at 1.
  - The scales are set every frame for every part, because a model instance is shared across entities.
  - Rotations are left alone: vanilla animation owns them.
- Vanilla layers are kept: enderman eyes, spider eyes, golem cracks, horse markings, wolf collar. The enderman's carried-block layer, golem flower, wolf armour and horse armour layers are dropped, since none of those states can happen.

### D5. Names and eggs
- Each is named "<Creature> Facsimile" (e.g. "Bat Facsimile"), with `spawn_egg_facsimile_<x>` eggs in the main creative tab after the existing eggs.
- Egg colours are a sickly version of the original's colours.

## Out of scope
- The fungal mannequin.
- Possession abilities and Concentration.
- Drainable "colonized" facsimiles for the Apotheos.
- Antibodies, the mycelium network, terrain reactions, rendering pools (2b).
- Custom sounds: they use the vanilla sounds of their base class.
