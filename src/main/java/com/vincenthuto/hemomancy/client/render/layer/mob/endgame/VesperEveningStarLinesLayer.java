package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEveningStarPresentationRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VesperEveningStarLinesLayer
        extends RenderLayer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
    private static final ResourceLocation LINES_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/vesper_evening_star_lines.png");
    private static final RenderType LINES = RenderType.entityTranslucentEmissive(LINES_TEXTURE);
    private static final int RED_LINE_LIGHT = VesperEveningStarPresentationRules.RED_LINE_LIGHT;

    public VesperEveningStarLinesLayer(
            RenderLayerParent<VesperTheEveningStarEntity, VesperTheEveningStarModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !VesperEveningStarPresentationRules.shouldRenderRedLines(
                entity.getHealth(), entity.getMaxHealth(), entity.isAwaitingAbsorption())) {
            return;
        }
        float alpha = VesperEveningStarPresentationRules.redLineAlpha(ageInTicks + partialTick);
        int color = packColor(1.0F, 0.04F, 0.025F, alpha);
        VertexConsumer vertexConsumer = buffer.getBuffer(LINES);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, RED_LINE_LIGHT,
                OverlayTexture.NO_OVERLAY, color);
    }

    private static int packColor(float red, float green, float blue, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
