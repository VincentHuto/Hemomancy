package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.InfectedBoltEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The Priestess's bolt: a small knot of writhing tendrils inside a glow, red for blood, sickly gold for spores. */
public class InfectedBoltRenderer extends EntityRenderer<InfectedBoltEntity> {
    private static final ResourceLocation TENDRIL = Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess_tendril.png");
    private static final int STRANDS = 6;
    private static final int STATIONS = 9;

    public InfectedBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(InfectedBoltEntity bolt, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float time = bolt.tickCount + partialTick;
        boolean spore = bolt.kind() == InfectedBoltEntity.Kind.SPORE;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer core = buffer.getBuffer(RenderType.entityCutoutNoCull(TENDRIL));
        float[][] lines = new float[STRANDS][];
        for (int s = 0; s < STRANDS; s++) {
            lines[s] = strand(s, time, spore ? 2.6F : 3.4F);
            TendrilTube.core(pose, core, lines[s], TendrilTube.taper(STATIONS, 0.7F, 0.1F, 1.0F, s), s * 0.2F,
                    0xF000F0, OverlayTexture.NO_OVERLAY, spore ? 1.0F : 0.95F);
        }
        VertexConsumer glow = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
        float[] alpha = new float[STATIONS];
        for (int i = 0; i < STATIONS; i++) alpha[i] = 0.22F * (1.0F - i / (float) STATIONS);
        for (float[] line : lines) {
            TendrilTube.glow(pose, glow, line, TendrilTube.taper(STATIONS, 0.7F, 0.1F, 1.0F, 0), 2.6F,
                    spore ? 0.85F : 1.0F, spore ? 0.62F : 0.1F, spore ? 0.2F : 0.12F, alpha);
        }
        poseStack.popPose();
        super.render(bolt, yaw, partialTick, poseStack, buffer, light);
    }

    /** Each strand coils out from the knot's centre and back, turning over time. */
    private static float[] strand(int index, float time, float reach) {
        float[] line = new float[STATIONS * 3];
        float phi = index * Mth.TWO_PI / STRANDS + time * 0.35F;
        float theta = 0.6F + 0.4F * Mth.sin(index * 1.7F + time * 0.2F);
        for (int i = 0; i < STATIONS; i++) {
            float s = i / (STATIONS - 1.0F);
            float r = reach * Mth.sin(s * Mth.PI * 0.9F + 0.1F);
            float a = phi + s * 1.4F;
            line[i * 3] = Mth.cos(a) * Mth.sin(theta) * r;
            line[i * 3 + 1] = Mth.cos(theta + s) * r;
            line[i * 3 + 2] = Mth.sin(a) * Mth.sin(theta) * r;
        }
        return line;
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedBoltEntity entity) {
        return TENDRIL;
    }
}
