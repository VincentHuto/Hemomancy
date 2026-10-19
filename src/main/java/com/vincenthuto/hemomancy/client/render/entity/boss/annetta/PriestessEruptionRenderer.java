package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Heading;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessTendrilRules.Wave;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessEruptionEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Blood thorns out of the floor. During the tell the ground glows red and tendril tips worm at the surface; on the
 * burst a cluster of thorned tendrils spears straight up, whips at the top, then sags and sinks back. Drawn with
 * the Priestess's own tendril tubes and glow so they read as more of her.
 */
public class PriestessEruptionRenderer extends EntityRenderer<PriestessEruptionEntity> {
    private static final ResourceLocation TENDRIL = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png");
    private static final int SEGMENTS = 12;

    public PriestessEruptionRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PriestessEruptionEntity eruption, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = eruption.tickCount + partialTick;
        float delay = eruption.delay();
        boolean major = eruption.isMajor();
        long seed = eruption.getUUID().getLeastSignificantBits();
        float since = age - delay;
        poseStack.pushPose();

        // The floor glows: a pulse that tightens and brightens through the tell, flashes on the burst, then cools.
        float tell = Mth.clamp(age / delay, 0.0F, 1.0F);
        float flash = since < 0 ? 0.0F : Math.max(0.0F, 1.0F - since / 6.0F);
        float cool = since < 0 ? 1.0F : 1.0F - Mth.clamp(since / PriestessCombatRules.ERUPTION_LIFE, 0.0F, 1.0F);
        float size = (major ? 3.4F : 2.0F) * 16.0F * (since < 0 ? 0.5F + 0.5F * tell : 1.0F + 0.3F * flash);
        float pulse = 0.5F + 0.5F * Mth.sin(age * (0.8F + tell));
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.03F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        TendrilTube.halo(poseStack.last(), buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0, 0, 0, size, size, 1.0F, 0.1F, 0.12F,
                (since < 0 ? (0.15F + 0.25F * tell) * (0.7F + 0.3F * pulse) : 0.45F * flash + 0.2F) * cool);
        poseStack.popPose();

        // Tendril space: model pixels with +Y down, as PriestessTendrilRules integrates.
        poseStack.scale(1.0F, -1.0F, 1.0F);
        PoseStack.Pose pose = poseStack.last();
        int count = major ? 9 : 6;
        float spread = major ? 9.0F : 4.5F;
        for (int i = 0; i < count; i++) {
            long hash = seed * 0x9E3779B97F4A7C15L + i * 0xBF58476D1CE4E5B9L;
            float azimuth = i * 360.0F / count + ((hash >>> 8) & 63) - 32.0F;
            float r = spread * (0.35F + 0.65F * (((hash >>> 16) & 255) / 255.0F));
            float[] root = {Mth.cos(azimuth * Mth.DEG_TO_RAD) * r, 0.5F, Mth.sin(azimuth * Mth.DEG_TO_RAD) * r};
            float full = (major ? 52.0F : 36.0F) * (0.7F + 0.3F * (((hash >>> 24) & 255) / 255.0F));
            float length;
            float droop;
            if (since < 0) {
                // Worming at the surface: short tips nosing up out of the glow.
                length = 3.5F * tell * (0.6F + 0.4F * Mth.sin(age * 0.9F + i));
                droop = 0.4F;
            } else {
                float shoot = 1.0F - (float) Math.pow(1.0F - Math.min(1.0F, since / 3.0F), 3);
                float sink = Mth.clamp((since - PriestessCombatRules.ERUPTION_LIFE + 10.0F) / 10.0F, 0.0F, 1.0F);
                length = full * shoot * (1.0F - sink);
                droop = 0.1F + 2.6F * Mth.clamp((since - 8.0F) / 14.0F, 0.0F, 1.0F);
            }
            if (length < 0.5F) continue;
            float elevation = 74.0F + ((hash >>> 32) & 15);
            Wave wave = new Wave(0.5F + 0.6F * flash, 0.18F, (hash & 0xFF) / 40.0F, ((hash >>> 40) & 0xFF) / 40.0F, droop);
            float[] line = PriestessTendrilRules.integrate(root, new Heading(azimuth, elevation), length, SEGMENTS, wave, age);
            float thick = (major ? 2.0F : 1.5F) * (0.8F + 0.2F * Math.min(1.0F, length / 10.0F));
            float[] radii = TendrilTube.taper(SEGMENTS + 1, thick, 0.04F, 2.0F, hash);
            // Fetch each buffer as it is used: asking for another render type closes the last one.
            TendrilTube.core(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(TENDRIL)), line, radii, i * 0.31F, light, OverlayTexture.NO_OVERLAY, 0.92F);
            float[] alpha = new float[SEGMENTS + 1];
            for (int k = 0; k <= SEGMENTS; k++) {
                float s = k / (float) SEGMENTS;
                alpha[k] = (0.06F + 0.3F * flash) * (1.0F - 0.5F * s) * cool;
            }
            TendrilTube.glow(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 1.8F, 1.0F, 0.1F, 0.12F, alpha);
        }
        poseStack.popPose();
        super.render(eruption, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PriestessEruptionEntity entity) {
        return TENDRIL;
    }
}
