package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.unstained.AnnettasSanguisLanceaModel;
import com.vincenthuto.hemomancy.client.render.item.harbinger.AnnettasSanguisLanceaItemRenderer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;

/**
 * Draws Annetta's lancea in a grip frame: origin at the right hand, the point running down -Z. Shared by the
 * Priestess's hand and the spear she throws, so both are the same weapon at the same size.
 */
final class LanceaDraw {
    /** The item mesh runs 114 pixels, butt (+24) to fork tips (-90); held, it is about 1.8 times her height. */
    static final float SCALE = 0.38F;
    /** The shaft and blade are thickened so they read at her size instead of thinning to a hair. */
    private static final float GIRTH = 2.8F;
    /** Mesh Y of the grip, toward the butt: eight pixels of shaft behind the hand, 36 ahead to the point. */
    private static final float GRIP_Y = 4.0F;

    private LanceaDraw() {}

    static void draw(PoseStack poseStack, MultiBufferSource buffer, AnnettasSanguisLanceaModel model, int light, int overlay,
            float growth, float alpha) {
        if (growth <= 0.01F || alpha <= 0.01F) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        float s = SCALE * growth;
        // Thickened across the shaft only; the fork prongs splay along Z and stay their own width.
        poseStack.scale(s * GIRTH, s, s * 1.8F);
        poseStack.translate(0.0F, -GRIP_Y / 16.0F, 0.0F);
        int colour = FastColor.ARGB32.color((int) (alpha * 255.0F), 255, 255, 255);
        VertexConsumer base = buffer.getBuffer(model.renderType(AnnettasSanguisLanceaItemRenderer.texture));
        model.renderToBuffer(poseStack, base, light, overlay, colour);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderTypeInit.getCrimsonGlint()), light, overlay, colour);
        poseStack.popPose();
    }

    /** A blood glow sheathing the shaft from grip to point, swelling as a throw gathers. */
    static void charge(PoseStack.Pose pose, MultiBufferSource buffer, float charge, float time, float tip) {
        if (charge <= 0.01F) return;
        int stations = 10;
        float[] line = new float[stations * 3];
        float[] radii = new float[stations];
        float[] alpha = new float[stations];
        for (int i = 0; i < stations; i++) {
            float s = i / (stations - 1.0F);
            line[i * 3 + 2] = -tip * s;
            float throb = 0.5F + 0.5F * (float) Math.sin(time * 0.9F - s * 6.0F);
            radii[i] = (0.6F + 1.4F * charge) * (1.0F - 0.5F * s) + throb * 0.4F * charge;
            alpha[i] = charge * (0.18F + 0.22F * throb) * (1.0F - 0.4F * s);
        }
        TendrilTube.glow(pose, buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), line, radii, 1.6F, 1.0F, 0.12F, 0.15F, alpha);
    }
}
