package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.PhlebotomistsCabinetBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class PhlebotomistsCabinetRenderer implements BlockEntityRenderer<PhlebotomistsCabinetBlockEntity> {
    public PhlebotomistsCabinetRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(PhlebotomistsCabinetBlockEntity cabinet, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        var state = cabinet.getBlockState();
        boolean glazed = state.getValue(PhlebotomistsCabinetBlock.GLAZED);
        float progress = cabinet.doorProgress(partialTick);
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(PhlebotomistsCabinetBlock.FACING).toYRot()));
        pose.translate(-0.5, 0, -0.5);
        float eased = progress * progress * (3 - 2 * progress);
        pose.translate(14.0 / 16, 0, 1.5 / 16);
        pose.mulPose(Axis.YP.rotationDegrees(-100 * eased));
        pose.translate(-14.0 / 16, 0, -1.5 / 16);
        draw(glazed ? "door_glazed" : "door_solid", pose, buffers, light, overlay, false);
        if (glazed) draw("glass", pose, buffers, light, overlay, true);
        pose.popPose();
    }
    private static void draw(String part, PoseStack pose, MultiBufferSource buffers, int light, int overlay, boolean glass) {
        var mc = Minecraft.getInstance();
        var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/phlebotomists_cabinet_" + part)));
        var type = glass ? RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS) : RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(type), null, model, 1, 1, 1, light, overlay);
    }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(PhlebotomistsCabinetBlockEntity cabinet) {
        return new net.minecraft.world.phys.AABB(cabinet.getBlockPos()).inflate(1);
    }
}
