package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MycophantAwokenMaskLayer extends RenderLayer<MycophantEntity, MycophantModel> {
    private static final ResourceLocation MASK_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/endgame/mycophant_awoken_mask.png");
    private static final RenderType MASK = RenderType.eyes(MASK_TEXTURE);
    private static final int FULL_BRIGHT = 0x00F000F0;

    public MycophantAwokenMaskLayer(RenderLayerParent<MycophantEntity, MycophantModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       MycophantEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || entity.getHealth() > entity.getMaxHealth() * 0.5F) {
            return;
        }
        float pulse = ((float) Math.sin((ageInTicks + partialTick) * 0.22F) + 1.0F) * 0.5F;
        int color = packColor(1.0F, 0.28F + pulse * 0.28F, 0.02F, 0.68F + pulse * 0.32F);
        VertexConsumer vertexConsumer = buffer.getBuffer(MASK);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, FULL_BRIGHT,
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
