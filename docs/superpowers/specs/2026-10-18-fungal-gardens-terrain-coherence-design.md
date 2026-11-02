# Fungal Gardens terrain coherence

## Why

In the Fungal Gardens dimension a biome's border had nothing to do with the land under it. Biomes were picked from the router's climate noises (`continents` at xz 0.14, `erosion` 0.28, `ridges` 0.35). Terrain was built from different samplings: `continental_shape` 0.075 and `erosion_shape` 0.22, plus three unrelated "colby" noises (gravel, soul sand, swamp). Every biome therefore showed every kind of terrain, and the dimension read as one biome with random patches of colour.

The dimension also shared `hemomancy:fungal_gardens` with the Overworld, where TerraBlender uses it to replace swamps and jungles. Biome tags therefore leaked structures both ways. Broken Churches, Hermitage Remnants, Mason's Respites and Surveyor Bivouacs could generate inside the dimension, and Dynasty Castles in Overworld swamps.

## Goal

Each dimension biome is a terrain type. The router computes one set of climate density functions, and both biome selection and terrain shape read that same set. Where a biome is, its land is.

Non-goals: new mobs, new structures, new features or blocks, Overworld Fungal Gardens changes, and any progression or travel rules.

## Biome roster and climate tiling

Selection uses continentalness C, weirdness W and humidity H. These thresholds were tuned against the validation map. Temperature, erosion and depth stay full-range for selection. The boxes tile the space without overlap.

| Biome | Where | Land |
|---|---|---|
| Fungal Isles | C < -0.28 | Fungal sea, sea level 20, floor down to y -16. Blob islands from a dedicated island noise rise steeply to y 23-33. |
| Mycelial Meadows (new, dimension only) | -0.28 <= C < 0.35, \|W\| <= 0.48, H < 0.25 (excluding Depths) | The lush heart. Low, gently rolling ground (y 26-48), morphic pools, sparse canopy. |
| Sporecrown Thicket | C >= -0.28, \|W\| <= 0.48, H >= 0.25 (excluding Depths) | Humid overgrowth. Relief grows with humidity, so the thicket is hilly. Dense canopy. |
| Hemorrhagic Plateau | C >= 0.35, \|W\| <= 0.48, H < 0.25 (excluding Depths) | Dry highland (y 48-76, rugged at low erosion). Where erosion is high it lifts into flat mesas near y 100 with escarpments. Crust body with calcified strata bands. |
| Mycelial Depths | C >= 0.12, \|W\| <= 0.08 | Winding gorge network following weirdness zero. The floor sits at y 28, above sea level and dry, cutting through highland and mesa. Walls are ribbed with calcified bands. Spore Nexus Towers rise from the floor. |
| Hyphal Spires | C >= -0.28, W >= 0.48 | Forest of tapering calcified-hyphae spires from a column noise, up to about y 200, tapering under the ceiling. |
| Drifting Mycelium | C >= -0.28, W <= -0.48 | Low ground beneath floating mycelium islands in a y 80-145 band. |

## Terrain

- Density functions live under `worldgen/density_function/fungal/`: climate (continents, erosion, ridges, humidity, temperature), masks, offset, relief amplitude, ground, spires, floating and terrain.
- Surface offset is a continentalness spline plus a highland erosion term, mesa lift, island lerp and gorge lerp. Every mask is a spline of the same climate functions the biome source reads, with its midpoint on the biome boundary.
- Ground density = 4 x (y gradient + offset) + relief amplitude x 3D relief noise.
- Spires add a column-noise cone term. Floating islands are maxed in, kept inside a vertical bell.
- `final_density` = squeeze(0.64 x interpolated(blend_density(terrain))). `initial_density_without_jaggedness` = 4 x depth, which aquifers use for sea placement.
- Cells are 4x8 (`size_vertical` 2) so spires and islands keep their shape.
- The old `continental_shape`, `erosion_shape`, `fungal_base_density` and `colby*` functions are removed.

## Surface

The existing bedrock, crust-floor and deep-crust rules stay, since tests pin them. On top of those:
- Spires bodies above y 56 are calcified hyphae.
- Drifting islands above y 72 get a mycelium top, dirt below it and a hyphae underside.
- Plateau bodies above y 54 are crust with calcified strata.
- Depths floors are conscious mass and hyphae, with ribbed walls.
- Meadows replace the old dimension Fungal Gardens surface branch.

## Decoration and spawns (dimension-only biomes)

- Feature lists are trimmed per identity, keeping one canonical order per decoration step. Spore Nexus Towers stay in the Depths and on the Plateau.
- Vanilla bats, mooshrooms and ghasts are removed. The lore says the Gardens hold alien life only.
- The Overworld `fungal_gardens` biome file is unchanged.

## Structure tags

- Dynasty Castle: Meadows, Thicket and Plateau, which have stable ground. Islands, spires, gorges and floating land are dropped, and so is Overworld Fungal Gardens.
- Mason's Respite and Hermitage Remnant drop Sporecrown Thicket. Sanguine Surveyor Bivouac drops Fungal Isles.

## Smaller fixes

- Spine arrival searches outward for broad dry ground, skipping floating islands, needle-thin spire tips and the sea.
- Client near-fog keys on Meadows instead of Fungal Gardens, and the Depths are always foggy.
- Lang entries are added for Meadows, Hyphal Spires and Drifting Mycelium.

## Verification

- JVM contract test: the router climate fields and terrain read the same `fungal/*` functions, the biome boxes do not overlap, and the dimension excludes `fungal_gardens`.
- Feature-order test stays green.
- Opt-in `fungal_gardens_worldgen_validation` GameTest samples the real seeded generator. It writes a biome/height PNG plus per-biome statistics and asserts the terrain signatures: isles mostly sea, spires tall, drifting has floating land, depths lower than surroundings and dry, plateau high.
