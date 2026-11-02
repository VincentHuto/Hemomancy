# Cortical Drift — End Biome Design

**Date:** 2026-09-20
**Status:** Approved design, pending implementation plan
**Biome id:** `hemomancy:cortical_drift`
**Landmark:** the Vagrant Mind

## 1. Summary

A self-contained End biome built around one colossal floating brain with a hollow,
nerve-strung cavity, surrounded by floating islands that are all wired together by
sagging nerve-fiber bundles. Every fiber — inside the brain and along the bridges —
sparks with the same nerve lightning already used by the Ferric Ductilis school, so
the biome reads as one continuous living nervous system rather than a set of props.

The biome is the Ferric Ductilis school's endgame home: `synaptic_node` is a ductilis
conductor, so conduction manipulations chain natively along the bridge network.

## 2. Generation

### 2.1 Biome placement — TerraBlender End registry

TerraBlender `4.1.0.8` (bundled at `libs/TerraBlender-neoforge-1.21.1-4.1.0.8.jar`)
supports the End through `terrablender.api.EndBiomeRegistry`, a flat registry separate
from the `Region` system. `RegionType` has only `OVERWORLD` and `NETHER` — End biomes do
not go through `Region` at all. `terrablender.mixin.MixinTheEndBiomeSource` already hooks
End biome selection, and selection runs through the same zoomed layered-noise path
(`terrablender.worldgen.noise.LayeredNoiseUtil#biomeArea` / `#createZoomedArea`) as the
Overworld and Nether, so End biomes generate as cohesive contiguous patches.

**No mixin, no `the_end.json` dimension override, and no `minecraft:end` noise-settings
override are needed.** Registration goes in the existing
`BiomeInit.commonSetup` `enqueueWork` block, alongside the Phlegethontic Nether region:

```java
if (HemoCommonConfig.ENABLE_CORTICAL_DRIFT_END_REGION.get()) {
    int weight = HemoCommonConfig.CORTICAL_DRIFT_END_REGION_WEIGHT.get();
    EndBiomeRegistry.registerHighlandsBiome(BiomeInit.CORTICAL_DRIFT, weight);
    EndBiomeRegistry.registerMidlandsBiome(BiomeInit.CORTICAL_DRIFT, weight);
    EndBiomeRegistry.registerIslandBiome(BiomeInit.CORTICAL_DRIFT, weight);
}
SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.END,
        Hemomancy.MOD_ID, CorticalDriftSurfaceRuleData.makeRules());
```

Three of the four categories are registered:

| Category | Registered | Why |
|---|---|---|
| Highlands | yes | the large plateau islands (refs 1, 2) |
| Midlands | yes | the transition ring, so a Cortical Drift patch is not seamed by vanilla `end_midlands` |
| Island | yes | the small scattered void isles between the big ones (ref 3) |
| Edge | **no** | the ring adjacent to the central island; excluded so the biome never borders the dragon arena |

Config follows the Phlegethontic precedent in `HemoCommonConfig`:
`ENABLE_CORTICAL_DRIFT_END_REGION` (boolean, default true) and
`CORTICAL_DRIFT_END_REGION_WEIGHT` (int), both restart-required.

**Known limitation:** the four End categories draw from separate weighted lists with
their own noise areas, so registering all three does not *guarantee* that a highlands
patch and its surrounding midlands both resolve to Cortical Drift. It raises the odds
substantially and any residual vanilla seam reads as the outskirts of the region. If
playtesting shows the seam is visually unacceptable, the fallback is a targeted mixin on
`TheEndBiomeSource#getNoiseBiome` with a shared region hash — deliberately *not*
pre-paid for.

### 2.2 Terrain and surface

Vanilla End island terrain is reused unchanged. The flat-topped plateaus with tapered
stalactite undersides already match the reference images closely; the distinct look comes
entirely from the surface rule reskin.

`CorticalDriftSurfaceRuleData` mirrors `PhlegethonticSurfaceRuleData` one-for-one:

```java
SurfaceRules.ifTrue(SurfaceRules.isBiome(BiomeInit.CORTICAL_DRIFT), SurfaceRules.sequence(
    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,   myelin_sheath),
    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(
        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -0.2, 0.2), ganglion_matter),
        axonal_slate)),
    SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, neural_tissue)))
```

The `Noises.SURFACE` band produces the irregular yellow seams visible through the crust.

The biome definition itself follows the existing `BiomeInit` builder pattern
(`hasPrecipitation(false)`, `BiomeSpecialEffects`), matching how `phlegethontic_basin`
is built. Effects target the reference palette: deep violet fog, near-black sky, white-
violet ambient glow. No carvers (End noise settings do not use them).

## 3. Block palette

The four colors you specified are used as a structural material vocabulary, not
decoration. Registered through `BlockInit`'s existing `DeferredRegister` groups.

| Block | Colour / role |
|---|---|
| `neural_tissue` | deep black, bulk stone of the biome, deep strata and anchor masses |
| `axonal_slate` | deep blue, banded stratum directly below the crust |
| `myelin_sheath` | white / bone, surface crust |
| `ganglion_matter` | yellow, light level 6, seams and anchor cores |
| `cortical_folds` | ridged gyri skin, brain exterior only |
| `nerve_fiber` | thin directional cable segment |
| `nerve_bundle` | thick multi-strand directional cable segment |
| `synaptic_node` | white junction, light level 10, the bright points in all three references |
| `dura_membrane` | translucent skin stretched over cavity mouths |
| `dendrite_growth` | small surface decoration, cross-model |

`synaptic_node` is added to `data/hemomancy/tags/block/ductilis_conductors.json`.

## 4. The Vagrant Mind

One per biome instance, 150–250 blocks across. Procedural — far too large for NBT
templates.

**Classes:** `VagrantMindStructure` (codec shape copied from `DynastyCastleStructure`,
registered in `StructureInit` via `explicitStructureTypeTyping`) and `VagrantMindPieces`
(real `PoolElementStructurePiece`s at computed offsets, following the `VigilPlacement`
pattern already used by `DynastyCastleStructure` — no jigsaw connector blocks).

**Shell generation:**
1. Union 10–16 noise-perturbed ellipsoid lobes as a metaball field.
2. Hollow to a shell 6–10 blocks thick.
3. Carve gyri with a ridged-noise band pass across the outer surface (`cortical_folds`).
4. Split the hemispheres with a sagittal fissure.
5. Trail a brainstem drip column downward from the underside (ref 1).

**Cavity interior:** ~20 anchor points on the inner shell, connected by paths drawn with
`VoxelBresenhamIterator` (already present in `common/worldgen/feature/`) with midpoint
droop. Places `nerve_fiber` / `nerve_bundle` along each path and a `synaptic_node` at
every branch point. The cavity is enterable.

**Placement data:** `data/hemomancy/worldgen/structure/vagrant_mind.json` and a matching
`structure_set`, gated on `#hemomancy:has_structure/vagrant_mind`. Unlike
`dynasty_castle.json`, End structures use `"terrain_adaptation": "none"` and a constant
absolute `start_height` with **no** `project_start_to_heightmap` — End islands float, so
there is no surface to project onto. The brain occupies an explicit y-band above the
island layer.

## 5. Island network

Every island carries at least one fiber connection; hub islands carry 3–5 (ref 3).

**`CorticalNetworkPlan`** — a region-level planner following the cross-chunk-deterministic
pattern already used by `PhlegethonticVeinPath` and `EscharianOvergrowthAnchors`. From
the region seed it:

1. Samples island positions from the chunk generator's column heights.
2. Builds a minimum spanning tree over them, guaranteeing every island is connected.
3. Adds a small number of extra cycle edges so hub islands accumulate multiple bundles.
4. Emits catenary-sagging cable paths for each edge.

**`NerveBridgeFeature`** — runs per-chunk at `VEGETAL_DECORATION`/`SURFACE_STRUCTURES`
and draws only the portion of any planned cable that crosses that chunk. Because the plan
is derived from the region seed rather than per-chunk randomness, cables never break at
chunk borders.

**Anchors** — where a cable lands on an island, generate a root mass of `neural_tissue`
and `axonal_slate` shot through with `ganglion_matter` seams, capped by a white
`synaptic_node` at the terminus.

## 6. Nerve lightning

Both existing systems accept arbitrary world points, which is exactly what static world
fibers require. Verified signatures:

- `LightningTesterSpawner.spawn(ServerLevel, Vec3 start, Vec3 end, LightningTestConfig)`
- `TendrilAnchor.Point(Vec3)` — consumed by `NerveLinkPacket`

**Layer 1 — arcs.** `CorticalArcManager`, server-side, patterned on
`ConductionManager`. Finds `synaptic_node` blocks near players and fires
`LightningTesterSpawner.spawn` along fiber runs on an interval, using one shared
Hemomancy nerve preset (violet-white outer, pale inner) so it reads identically to
`DuctilisLightningEffects`. Rate-limited by player count and distance; only loaded chunks
within render distance.

**Layer 2 — filaments.** Long bridges emit `NerveLinkPacket` with `TendrilAnchor.Point`
endpoints so `FerricDuctilisEffects` draws the soft glowing tendril overlay along the
cable — the look in refs 2 and 3.

Per `AGENTS.md`, the ambient layer stays on the authored glow/tendril/lightning toolkit.
No vanilla particle clouds. A low pulse ambient loop and mood sound complete the space,
registered through `SoundInit`.

## 7. Gameplay payload

- **Ferric Ductilis endgame home.** `synaptic_node` as a ductilis conductor means
  conduction manipulations chain along the bridge network with no new mechanics.
- **New materials.** The block palette is harvestable and feeds existing Hemomancy
  crafting.
- **No new boss.** Vesper the Evening Star already occupies endgame-boss space. The brain
  cavity core gets a rite site / high-value harvest instead.
- **Mobs reuse the existing roster** at first. A nerve-native End mob is deferred to a
  later pass and is explicitly out of scope here.

## 8. Lore

The Vagrant Mind is a dormant organ, not a deity — nervous tissue the infection never
reached, or something that thought too long and stopped. No God/Satan framing, consistent
with the Fungal Entity and Pale Lady treatment. Readable by both Harbinger and Unstained
framings: the Hematic Order reads it as preserved memory to be tapped; Lethean
still-water language reads it as something that finally went quiet.

## 9. Testing

- JVM source tests in the repo's `*SourceTest` style: surface rule wiring, TerraBlender
  End registration, `CorticalNetworkPlan` connectivity (assert every island is reachable
  and the MST has no orphans), determinism across repeated planning with the same seed.
- GameTest coverage for Vagrant Mind placement and cavity fiber generation.
- `/hemo test` fixture per `docs/TESTING.md`.
- `./gradlew.bat alphaCheck` as the gate.

## 10. Documentation contract

Per `AGENTS.md`, updates required on completion:
`docs/HEMOMANCY_REFERENCE.md` (biome, blocks, ductilis interaction),
`docs/LORE_REFERENCE.md` (the Vagrant Mind), and a `wiki/` page for the biome.

## 11. Out of scope

- A nerve-native End mob or boss.
- Any change to the vanilla central End island or dragon fight.
- The fallback `TheEndBiomeSource` mixin (only if playtesting demands it).
