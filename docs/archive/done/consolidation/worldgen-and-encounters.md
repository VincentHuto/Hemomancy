# World generation, spawning, and encounters

## Current paths

- [EntityInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java) registers entity types and spawn placements. Biome eligibility and weights are elsewhere, including [BiomeInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java) and data resources.
- Features such as [PhlegethonticWorldgenFeature](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/PhlegethonticWorldgenFeature.java), [CorticalArchipelagoFeature](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/CorticalArchipelagoFeature.java), and [DeepOceanVentFeature](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/DeepOceanVentFeature.java) have distinct placement and inhabitant logic. [StructurePlacementChecks](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/StructurePlacementChecks.java) is an existing shared check point.
- Encounter activation lives in managers such as [VagrantMindEncounterManager](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/VagrantMindEncounterManager.java), [MycophantEncounterManager](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/MycophantEncounterManager.java), and [VesperOrdealManager](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/VesperOrdealManager.java). Durable encounter records are covered in [World persistence and reloads](world-persistence-and-reloads.md).

## Overlap and target contract

Keep four paths distinct: **generation eligibility**, **feature/structure placement**, **natural spawn predicate**, and **encounter activation or respawn**. They may share geometry rules, but success in one path says nothing about another. A mob that can be summoned may still fail natural spawning, and a structure placed by command may still have the wrong biome tag or placed-feature JSON.

For each world content family, record its biome/dimension data, configured and placed feature or structure registration, placement predicate, natural spawn entry, runtime spawn predicate, and one-time generation behavior. Reuse a pure geometry predicate when two paths truly need the same spatial rule; keep generation scheduling and active encounter state separate.

## First migration slice

1. Choose one biome mob and trace its biome entry, spawn placement, actual predicate, and feature-generated spawns if any.
2. Move duplicated spatial eligibility math to one side-neutral rule with named parameters; retain separate natural and one-time callers.
3. For a structure or feature, check its runtime JSON and biome tags alongside Java registration. Existing chunks do not replay one-time generation after a code change.
4. Validate in a fresh generated area and in a saved world where encounter persistence matters. Record which path each result exercises.

**Avoid:** treating all spawning as one hook, or changing biome/structure IDs merely to tidy names.
