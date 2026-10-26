package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;

public class HemorathOverloadLayer<T extends HemorathEntity> extends RenderLayer<T, HemorathModel<T>> {
    private static final ResourceLocation GLOW_TEXTURE =
            Hemomancy.rloc("textures/entity/boss/hemorath/hemorath_overload.png");
    private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);
    private static final int FULL_BRIGHT = 0x00F000F0;

    public HemorathOverloadLayer(RenderLayerParent<T, HemorathModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        float storedBlood = HemorathVisualRules.storedPressure(entity.getAbsorbedBlood());
        var motion = HemorathAnimState.of(entity);
        motion.update(entity, partialTick);
        var pose = HemorathVisualRules.pose(entity.getVisualState(), entity.isCollapseCharging(), entity.isInPhase2(), motion.remaining());
        int visualState = entity.getVisualState();
        if (!entity.isCollapseCharging() && !entity.isInPhase2() && storedBlood <= 0.05F
                && visualState != HemorathEntity.VISUAL_EMPTY_PULSE
                && visualState != HemorathEntity.VISUAL_EMPTY_PULSE_IMPACT
                && visualState != HemorathEntity.VISUAL_COLLAPSE_IMPACT
                && visualState != HemorathEntity.VISUAL_OVERLOAD) {
            return;
        }

        float pulse = (float) Math.pow(Math.max(0, Math.sin(ageInTicks * .22F)), 4);
        float alpha = Mth.clamp(0.25F + storedBlood * 0.5F + pose.pressure() * .25F, 0.35F, 1.0F);
        if (visualState == HemorathEntity.VISUAL_OVERLOAD) {
            alpha = 1.0F;
        } else if (visualState == HemorathEntity.VISUAL_EMPTY_PULSE
                || visualState == HemorathEntity.VISUAL_EMPTY_PULSE_IMPACT) {
            alpha = Math.max(alpha, 0.65F);
        }
        int color = FastColor.ARGB32.color((int) (alpha * 255), 255, (int) (35 + pulse * 55), 24);
        VertexConsumer vertexConsumer = buffer.getBuffer(GLOW);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
    }

}
