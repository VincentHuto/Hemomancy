# Visual effects and particle calls

## Current paths

- [ParticleInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ParticleInit.java) registers particle types. Effect producers also live in manipulation, entity, rite, item, and block code.
- [ManipulationVisuals](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationVisuals.java) carries manipulation visual forms; [ManipulationSignatureEffects](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationSignatureEffects.java) draws school subjects with [VisceralMesh](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/VisceralMesh.java) surfaces.
- [ManipulationAmbientParticles](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationAmbientParticles.java) already uses [ManipulationParticleBudget](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationParticleBudget.java) and [BloodEffectParticles](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/BloodEffectParticles.java). Other authored families include [FerricDuctilisEffects](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/FerricDuctilisEffects.java), [LuxUmbraEffects](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/LuxUmbraEffects.java), [BloodProjectileEffects](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/BloodProjectileEffects.java), and [SanguineTendrilRibbonRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/world/SanguineTendrilRibbonRenderer.java).
- Direct server calls remain in individual powers, for example [CrimsonHarvestManip](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/CrimsonHarvestManip.java), [FerricResonanceManip](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/FerricResonanceManip.java), and [VitalEffusionManip](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/VitalEffusionManip.java). These mix custom and vanilla particles.

## Overlap and target contract

The repeated job is choosing an effect, its color and scale, broadcast radius, count, and side of execution. Establish a small effect vocabulary for **hit**, **cast**, **sustain**, **transfer**, **ambient**, and **world event**. Each recipe should declare its authored particle or geometry family, color source, intensity, distance budget, and whether the server broadcasts a cue or the client animates a persistent state. Keep school and creature-specific composition in their current effect classes.

Treat a direct `sendParticles` call as a review point, not an automatic replacement. A one-shot impact can stay server driven; persistent ribbons should normally be state driven and rendered client side. Route new focal effects through the Hemomancy/HutosLib authored suite when it expresses the action. Vanilla effects need an explicit visual reason.

## First migration slice

1. Inventory manipulation impact calls and record the intended appearance and recipient radius.
2. Extract one reusable impact emitter for repeated server broadcast parameters; migrate two genuinely equivalent impacts first.
3. Give persistent effect families one shared budget/visibility policy, using the existing manipulation budget as the reference. Preserve their material and motion code.
4. Review one live client scene at normal particle settings and reduced settings. Source checks cannot establish visual density or color balance.

**Avoid:** a universal particle helper with dozens of boolean arguments, or making `common/` depend on client renderer classes.
