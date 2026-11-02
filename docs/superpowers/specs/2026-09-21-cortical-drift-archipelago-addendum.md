# Cortical Drift — Archipelago Redesign Addendum (2026-09-21)

Supersedes spec §2.1 (category table), §2.2 (terrain reuse), and §5 (island network) of
2026-09-20-cortical-drift-design.md. Everything else stands.

## Why
In-game test showed: (1) no visible bridges, (2) islands were reskinned vanilla continents, far too
large, (3) no Vagrant Minds found. Root causes:
1. NerveBridgeFeature treated a 6x6 lattice of sample points as "islands"; several fell on one landmass,
   so most MST edges joined points on the same continent and sagged into solid ground (setIfOpen refuses
   to overwrite terrain) -> buried, invisible cables.
2. Registering in highlands/midlands reused vanilla continental terrain by design. Wrong look.
3. structure_set spacing 96 chunks (1536 blocks) + biome gate made Minds extremely rare.

## New architecture
- Register cortical_drift ONLY via EndBiomeRegistry.registerIslandBiome (vanilla small_end_islands zones,
  which are mostly empty void). Remove highlands and midlands registration. Edge stays excluded.
- We generate every island ourselves with one feature, `hemomancy:cortical_archipelago`, replacing
  `nerve_bridge`. Region-seeded, REGION_SIZE = 384 (divisible by 16, so every chunk lies in exactly one
  region). Every node is kept fully inside its region (margin), so a chunk only ever plans ONE region and
  cables never cross region borders.
- Pure planner `CorticalArchipelagoPlan`: 22-32 islands + 3-5 floating ganglion hubs per region,
  non-overlapping, radius skewed small (60% 5-10, 30% 10-16, 10% 16-24), y in [48,196], vertical stacking
  allowed when vertical separation clears the underside. An injected filter rejects spots (biome
  footprint, Vagrant Mind clearance).
- Pure shape `CorticalIslandShape`: flat plateau top with small bumps, angular-noise edge, tapered
  underside depth radius*(1.4..2.6) with 3-6 stalactite spikes; layers myelin top / slate 3 below /
  tissue core / ganglion veins. Hash-based, no Random per sample.
- Bridges: CorticalNetworkPlan.connect over island+hub centres (existing MST + hubs). Cable endpoints
  are moved to island rims facing each other; bundles draw as 3 parallel strands; hubs are mid-air
  ganglion spheres where several cables meet.
- Placed feature has NO minecraft:biome filter (so chunks at patch edges still draw islands whose
  footprint the filter already validated as in-biome).
- Vagrant Mind: structure_set spacing 40 / separation 20. Islands within 150 blocks horizontally of any
  Mind candidate that lands in cortical_drift are rejected by the filter, so each Mind hangs in a clearing.
