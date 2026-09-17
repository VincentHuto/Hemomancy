package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.PhlebotomistsFieldCaseBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsFieldCaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class PhlebotomistsFieldCaseRenderer implements BlockEntityRenderer<PhlebotomistsFieldCaseBlockEntity> {
    public PhlebotomistsFieldCaseRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(PhlebotomistsFieldCaseBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = be.getBlockState();
        float progress = be.lidProgress(partialTick);
        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(PhlebotomistsFieldCaseBlock.FACING).toYRot()));
        pose.translate(-.5, 0, -.5);
        pose.translate(0, 6.0 / 16, 14.0 / 16);
        pose.mulPose(Axis.XP.rotationDegrees(105 * progress * progress * (3 - 2 * progress)));
        pose.translate(0, -6.0 / 16, -14.0 / 16);
        var mc = Minecraft.getInstance();
        var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/phlebotomists_field_case_lid")));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),
                null, model, 1, 1, 1, light, overlay);
        pose.popPose();
    }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(PhlebotomistsFieldCaseBlockEntity be) {
        return new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(1);
    }
}
