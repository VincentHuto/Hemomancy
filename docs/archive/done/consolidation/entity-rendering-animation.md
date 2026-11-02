# Entity rendering and animation

## Current paths

- Standard mobs use `MobRenderer`, for example [SanguineHoundRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/SanguineHoundRenderer.java) and [MycophantRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/endgame/MycophantRenderer.java). Bespoke entities use `EntityRenderer`, including [NaeglerophaeonRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/endgame/NaeglerophaeonRenderer.java) with multiple material passes.
- Translucency is selected in several places: [PhantasmalEchoRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/PhantasmalEchoRenderer.java), [PaleIntercessionRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/PaleIntercessionRenderer.java), and models such as [VelorumModel](../../src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java). [HemoRenderTypes](../../src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java) contains special materials.
- Pose reset is explicit but differs by model: [DesiccantModel](../../src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/DesiccantModel.java) resets articulated parts; [LanternTickModel](../../src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/LanternTickModel.java) has a local reset method. Registration also spans the central client setup and feature events such as [PhlegethonticClientEvents](../../src/main/java/com/vincenthuto/hemomancy/client/event/PhlegethonticClientEvents.java).

## Overlap and target contract

There are three separate concerns: **registration**, **material/layer policy**, and **pose composition**. Keep them separate. A renderer profile can name texture, cutout/translucent material, glow layer, shadow, and scale for ordinary mobs. Bespoke multi-pass bosses should remain bespoke and reuse shared material functions where appropriate.

Every procedural `setupAnim` should start from a known pose, then apply locomotion, head aim, action state, and secondary motion in that order. Parent-connected anatomy and rest pivots belong in the model asset, not in a catch-all animation utility. For Blockbench assets, preserve the 0.25 model-unit grid for cube bounds and pivots.

## First migration slice

1. Make a renderer matrix for living entities: texture, render type, emissive layer, scale, shadow, and custom passes. Group only matching rows.
2. Consolidate one simple renderer family with identical material policy; leave special disappearance and boss passes alone.
3. Audit `setupAnim` methods that add to rotations and ensure all altered parts reset each frame. Use a shared pose utility only for identical repeated motion math.
4. Inspect the actual client pose and transitions after each migrated model. Compile success cannot show seams, silhouette loss, or stale rotations.

**Avoid:** one base renderer for every mob, or erasing named creature materials to force a uniform style.
