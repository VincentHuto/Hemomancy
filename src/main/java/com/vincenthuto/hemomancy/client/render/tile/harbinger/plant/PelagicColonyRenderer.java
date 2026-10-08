package com.vincenthuto.hemomancy.client.render.tile.harbinger.plant;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BoneWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.GiantTubeWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import java.util.EnumMap;

public class PelagicColonyRenderer implements BlockEntityRenderer<PelagicColonyBlockEntity> {
    private final EnumMap<PelagicCreatureModel.Style, PelagicCreatureModel<Mob>> models = new EnumMap<>(PelagicCreatureModel.Style.class);
    public PelagicColonyRenderer(BlockEntityRendererProvider.Context context) {
        for (var style : new PelagicCreatureModel.Style[]{PelagicCreatureModel.Style.ANEMONE, PelagicCreatureModel.Style.BONE_WORM, PelagicCreatureModel.Style.TUBE_WORM})
            models.put(style, new PelagicCreatureModel<>(context.bakeLayer(style.layer()), style));
    }
    @Override public void render(PelagicColonyBlockEntity colony, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var kind = ((PelagicColonyBlock)colony.getBlockState().getBlock()).kind;
        var style = switch (kind) {
            case ANEMONE -> PelagicCreatureModel.Style.ANEMONE;
            case BONE_WORM -> PelagicCreatureModel.Style.BONE_WORM;
            case TUBE_WORM -> PelagicCreatureModel.Style.TUBE_WORM;
        };
        var model = models.get(style);
        float age = colony.getLevel() == null ? 0 : colony.getLevel().getGameTime() + partial + colony.getBlockPos().getX() * 7;
        int worms = switch (kind) {
            case BONE_WORM -> colony.getBlockState().getValue(BoneWormColonyBlock.WORMS);
            case TUBE_WORM -> colony.getBlockState().getValue(GiantTubeWormColonyBlock.WORMS);
            default -> 5;
        };
        model.animateColony(age, Mth.lerp(partial, colony.previousExtension, colony.extension), worms);
        pose.pushPose(); pose.translate(.5, 1.5, .5); pose.scale(1, -1, -1);
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(Hemomancy.rloc("textures/entity/pelagic/" + style.id + ".png"))), light, overlay);
        pose.popPose();
    }
    @Override public AABB getRenderBoundingBox(PelagicColonyBlockEntity colony) { return new AABB(colony.getBlockPos()).expandTowards(0, 1, 0); }
}
