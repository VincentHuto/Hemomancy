package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaPoolEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaPoolEntity.Kind;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A pool lying on the floor: a ragged painted puddle that breathes slightly; humor glows from beneath, and the
 * reanimating pool sends tendrils crawling in from its rim toward the body at its centre.
 */
public class AnnettaPoolRenderer extends EntityRenderer<AnnettaPoolEntity> {
    private static final ResourceLocation HUMOR = Hemomancy.rloc("textures/entity/boss/annetta/pool_humor.png");
    private static final ResourceLocation REANIMATION = Hemomancy.rloc("textures/entity/boss/annetta/pool_reanimation.png");
    private static final ResourceLocation TENDRIL = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png");
    private static final int SLICES = 24;

    public AnnettaPoolRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(AnnettaPoolEntity pool, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float time = AnimationTime.age(pool, partialTick);
        float radius = pool.radius() * (1.0F + 0.02F * Mth.sin(time * 0.08F));
        Kind kind = pool.kind();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-pool.getYRot() + time * 0.15F));
        PoseStack.Pose pose = poseStack.last();
        float lift = AnnettaPoolLayers.layer(pool.getId());
        if (kind == Kind.INFECTED) {
            // The same living blood as a brazier bowl or the Bloodwell basin, simmering, with the infection veined through it.
            float shaderTime = (float) (AnimationTime.gameTime(pool.level(), partialTick) % 24000.0D);
            // The infected pool shader: the brazier/Bloodwell blood surface, ichor veined through it, a ragged splash edge.
            VertexConsumer blood = buffer.getBuffer(HemoRenderTypes.infectedPool(shaderTime, Math.floorMod(pool.getId() * 37, 1000)));
            BloodPoolSurface.draw(pose.pose(), blood, 0.0F, lift, 0.0F, radius * 1.25F, 0, 60, 0, 238);
        } else {
            disc(pose, buffer.getBuffer(RenderType.entityTranslucent(texture(kind))), radius * 1.15F, lift, light, 1.0F);
        }
        if (kind == Kind.HUMOR) {
            glowDisc(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), radius * 0.95F, lift + 0.004F,
                    0.75F, 0.9F, 1.0F, 0.18F + 0.06F * Mth.sin(time * 0.15F));
        } else if (kind == Kind.REANIMATION) {
            crawl(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(TENDRIL)), radius, time, pool.getId(), light);
        }
        poseStack.popPose();
        super.render(pool, yaw, partialTick, poseStack, buffer, light);
    }

    private static ResourceLocation texture(Kind kind) {
        return switch (kind) {
            case INFECTED -> REANIMATION;
            case HUMOR -> HUMOR;
            case REANIMATION -> REANIMATION;
        };
    }

    private static void disc(PoseStack.Pose pose, VertexConsumer out, float radius, float y, int light, float alpha) {
        for (int i = 0; i < SLICES; i++) {
            float a0 = i * Mth.TWO_PI / SLICES, a1 = (i + 1) * Mth.TWO_PI / SLICES;
            vertex(pose, out, 0, y, 0, 0.5F, 0.5F, light, alpha);
            vertex(pose, out, Mth.cos(a0) * radius, y, Mth.sin(a0) * radius, 0.5F + Mth.cos(a0) * 0.5F, 0.5F + Mth.sin(a0) * 0.5F, light, alpha);
            vertex(pose, out, Mth.cos(a1) * radius, y, Mth.sin(a1) * radius, 0.5F + Mth.cos(a1) * 0.5F, 0.5F + Mth.sin(a1) * 0.5F, light, alpha);
            vertex(pose, out, 0, y, 0, 0.5F, 0.5F, light, alpha);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer out, float x, float y, float z, float u, float v,
            int light, float alpha) {
        out.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void glowDisc(PoseStack.Pose pose, VertexConsumer out, float radius, float y, float r, float g, float b, float alpha) {
        for (int i = 0; i < SLICES; i++) {
            float a0 = i * Mth.TWO_PI / SLICES, a1 = (i + 1) * Mth.TWO_PI / SLICES;
            for (int pass = 0; pass < 2; pass++) {
                float first = pass == 0 ? a0 : a1, second = pass == 0 ? a1 : a0;
                out.addVertex(pose, 0, y, 0).setColor(r, g, b, alpha);
                out.addVertex(pose, Mth.cos(first) * radius, y, Mth.sin(first) * radius).setColor(r, g, b, 0.0F);
                out.addVertex(pose, Mth.cos(second) * radius, y, Mth.sin(second) * radius).setColor(r, g, b, 0.0F);
                out.addVertex(pose, 0, y, 0).setColor(r, g, b, alpha);
            }
        }
    }

    /** Tendrils crawl in from the rim along the floor, curling toward the centre. */
    private static void crawl(PoseStack.Pose pose, VertexConsumer out, float radius, float time, int seed, int light) {
        int count = 9;
        int stations = 12;
        float scale = 16.0F;
        for (int t = 0; t < count; t++) {
            float base = t * Mth.TWO_PI / count + seed * 0.37F;
            float[] line = new float[stations * 3];
            for (int i = 0; i < stations; i++) {
                float s = i / (stations - 1.0F);
                float reach = radius * (1.05F - 0.75F * s);
                float angle = base + 0.5F * s + 0.18F * Mth.sin(time * 0.07F + t + s * 4.0F);
                line[i * 3] = Mth.cos(angle) * reach * scale;
                line[i * 3 + 1] = 0.6F + 0.9F * Mth.sin(s * Mth.PI) * Math.abs(Mth.sin(time * 0.05F + t));
                line[i * 3 + 2] = Mth.sin(angle) * reach * scale;
            }
            TendrilTube.core(pose, out, line, TendrilTube.taper(stations, 0.9F, 0.15F, 1.5F, seed + t), t * 0.3F,
                    light, OverlayTexture.NO_OVERLAY, 0.9F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AnnettaPoolEntity entity) {
        return texture(entity.kind());
    }
}
