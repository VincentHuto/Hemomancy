package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.HollowVesselModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HollowVesselEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HemorathOverloadLayer<T extends HollowVesselEntity> extends RenderLayer<T, HollowVesselModel<T>> {
    private static final ResourceLocation GLOW_TEXTURE =
            Hemomancy.rloc("textures/entity/hollow_vessel/hollow_vessel_overload.png");
    private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float OVERLOAD_VISUAL_THRESHOLD = 1800.0F;

    public HemorathOverloadLayer(RenderLayerParent<T, HollowVesselModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        float storedBlood = Mth.clamp(entity.getAbsorbedBlood() / OVERLOAD_VISUAL_THRESHOLD, 0.0F, 1.0F);
        int visualState = entity.getVisualState();
        if (!entity.isCollapseCharging() && !entity.isInPhase2() && storedBlood <= 0.05F
                && visualState != HollowVesselEntity.VISUAL_EMPTY_PULSE
                && visualState != HollowVesselEntity.VISUAL_EMPTY_PULSE_IMPACT
                && visualState != HollowVesselEntity.VISUAL_COLLAPSE_IMPACT
                && visualState != HollowVesselEntity.VISUAL_OVERLOAD) {
            return;
        }

        float pulse = ((float) Math.sin((ageInTicks + partialTick) * 0.28F) + 1.0F) * 0.5F;
        float alpha = Mth.clamp(0.35F + storedBlood * 0.45F + (entity.isCollapseCharging() ? 0.25F : 0.0F), 0.35F, 1.0F);
        if (visualState == HollowVesselEntity.VISUAL_OVERLOAD) {
            alpha = 1.0F;
        } else if (visualState == HollowVesselEntity.VISUAL_EMPTY_PULSE
                || visualState == HollowVesselEntity.VISUAL_EMPTY_PULSE_IMPACT) {
            alpha = Math.max(alpha, 0.65F);
        }
        int color = packColor(1.0F, 0.12F + pulse * 0.18F, 0.04F, alpha);
        VertexConsumer vertexConsumer = buffer.getBuffer(GLOW);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
    }

    private static int packColor(float red, float green, float blue, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
