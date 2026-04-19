package com.vincenthuto.hemomancy.client.render.item;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Renders the Qliphoth Seed item entity with animated black-and-red root
 * tendrils that probe downward, as if seeking purchase in the ground.
 */
public class QliphothSeedItemRenderer extends EntityRenderer<ItemEntity> {

    private static final int TENDRIL_COUNT = 5;
    private static final int TENDRIL_SEGMENTS = 12;
    private static final float START_Y = 0.08f;
    private static final float CORE_WIDTH = 0.022f;
    private static final float GLOW_WIDTH_MULT = 1.7f;

    // Irregular base angles — not evenly spaced, breaks the "ring of fishhooks" look
    private static final float[] BASE_ANGLES  = { 0.00f, 1.57f, 2.34f, 4.21f, 4.74f };
    // Per-tendril reach and spread — each root has its own personality
    private static final float[] TENDRIL_REACH  = { 0.38f, 0.50f, 0.32f, 0.48f, 0.42f };
    private static final float[] TENDRIL_SPREAD = { 0.20f, 0.28f, 0.17f, 0.26f, 0.24f };
    // Independent animation phases — roots move completely out of sync with each other
    private static final float[] PHASE_A = { 0.00f, 1.94f, 3.57f, 5.21f, 2.83f };
    private static final float[] PHASE_B = { 0.73f, 2.61f, 4.33f, 1.17f, 3.88f };
    private static final float[] PHASE_C = { 1.51f, 3.27f, 0.65f, 4.89f, 2.14f };

    private final net.minecraft.client.renderer.entity.ItemRenderer itemRenderer;
    private final Random random = new Random();

    public QliphothSeedItemRenderer(Context ctx) {
        super(ctx);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private int getModelCount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) i = 5;
        else if (stack.getCount() > 32) i = 4;
        else if (stack.getCount() > 16) i = 3;
        else if (stack.getCount() > 1) i = 2;
        return i;
    }

    @Override
    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferIn, int packedLightIn) {

        ItemStack itemstack = entityIn.getItem();
        if (itemstack.isEmpty()) {
            super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
            return;
        }

        int seed = Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        this.random.setSeed(seed);
        BakedModel bakedModel = this.itemRenderer.getModel(itemstack, entityIn.level(), (LivingEntity) null, 0);
        boolean flag = bakedModel.isGui3d();
        int j = this.getModelCount(itemstack);

        // ── Item model (bob + spin) ──
        poseStack.pushPose();
        float bobOffset = Mth.sin((entityIn.getAge() + partialTicks) / 10.0F + entityIn.bobOffs) * 0.1F + 0.1F;
        float f2 = bakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        poseStack.translate(0.0D, bobOffset + 0.25F * f2, 0.0D);
        float f3 = (entityIn.getAge() + partialTicks) / 20.0F + entityIn.bobOffs;
        poseStack.mulPose(Vector3.YP.rotation(f3).toMoj());

        if (!flag) {
            poseStack.translate(0.0F, 0.0F, -0.09375F * (j - 1) * 0.5F);
        }

        for (int k = 0; k < j; ++k) {
            poseStack.pushPose();
            if (k > 0) {
                if (flag) {
                    poseStack.translate(
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F,
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F,
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F);
                } else {
                    poseStack.translate(
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.075F,
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.075F,
                        0.0D);
                }
            }
            this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, poseStack, bufferIn,
                    packedLightIn, OverlayTexture.NO_OVERLAY, bakedModel);
            poseStack.popPose();
            if (!flag) {
                poseStack.translate(0.0, 0.0, 0.09375F);
            }
        }
        poseStack.popPose(); // back to entity base position

        // ── Root tendrils in entity-local space ──
        float time = (float) (System.currentTimeMillis() % 200000L) / 1000.0f;
        Matrix4f mat = poseStack.last().pose();
        VertexConsumer coreVC = bufferIn.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
        VertexConsumer glowVC = bufferIn.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

        for (int i = 0; i < TENDRIL_COUNT; i++) {
            drawTendril(coreVC, glowVC, mat, time, i);
        }

        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    private static void drawTendril(VertexConsumer coreVC, VertexConsumer glowVC,
            Matrix4f mat, float time, int idx) {

        float baseAngle = BASE_ANGLES[idx];
        float reach     = TENDRIL_REACH[idx];
        float spread    = TENDRIL_SPREAD[idx];

        float[] px = new float[TENDRIL_SEGMENTS + 1];
        float[] py = new float[TENDRIL_SEGMENTS + 1];
        float[] pz = new float[TENDRIL_SEGMENTS + 1];

        for (int i = 0; i <= TENDRIL_SEGMENTS; i++) {
            float t   = (float) i / TENDRIL_SEGMENTS;
            float tSq = t * t;

            // Three independent radial waves: slow whole-arm drift, mid-length curl, fast tip twitch
            float radWave =
                (float)(Math.sin(time * 0.43f + PHASE_A[idx])             * 0.10f)
              + (float)(Math.sin(time * 1.28f + PHASE_B[idx] + t * 3.5f)  * 0.07f * t)
              + (float)(Math.sin(time * 2.51f + PHASE_C[idx] + t * 6.2f)  * 0.04f * tSq);

            // Tangential wave — curls roots sideways so they don't just point radially outward
            float tanWave =
                (float)(Math.sin(time * 0.57f + PHASE_C[idx])             * 0.09f)
              + (float)(Math.sin(time * 1.74f + PHASE_A[idx] + t * 4.0f)  * 0.06f * t);

            float angle      = baseAngle + radWave;
            float radialDist = spread * tSq;
            float tangDist   = tanWave * tSq;

            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);
            px[i] = cosA * radialDist - sinA * tangDist;
            // Slight mid-root arch — roots bow outward before drooping down
            py[i] = START_Y - t * reach
                  + (float)(Math.sin(Math.PI * t) * Math.sin(time * 0.65f + PHASE_B[idx]) * 0.025f);
            pz[i] = sinA * radialDist + cosA * tangDist;
        }

        for (int i = 0; i < TENDRIL_SEGMENTS; i++) {
            float t0 = (float) i / TENDRIL_SEGMENTS;
            float w0 = CORE_WIDTH * (1.0f - t0);
            float w1 = CORE_WIDTH * (1.0f - (float)(i + 1) / TENDRIL_SEGMENTS);

            float dx = px[i+1] - px[i], dy = py[i+1] - py[i], dz = pz[i+1] - pz[i];
            float invLen = 1.0f / Mth.sqrt(dx*dx + dy*dy + dz*dz);
            dx *= invLen; dy *= invLen; dz *= invLen;

            float ax = 1, ay = 0, az = 0;
            if (Math.abs(dx) > 0.9f) { ax = 0; az = 1; }
            float c1x = dy*az - dz*ay, c1y = dz*ax - dx*az, c1z = dx*ay - dy*ax;
            float c1len = Mth.sqrt(c1x*c1x + c1y*c1y + c1z*c1z);
            if (c1len > 1e-5f) { c1x /= c1len; c1y /= c1len; c1z /= c1len; }
            float c2x = dy*c1z - dz*c1y, c2y = dz*c1x - dx*c1z, c2z = dx*c1y - dy*c1x;

            float coreAlpha = 0.88f - t0 * 0.30f;
            float glowAlpha = 0.55f - t0 * 0.45f;
            float gw0 = w0 * GLOW_WIDTH_MULT, gw1 = w1 * GLOW_WIDTH_MULT;

            drawCrossQuad(coreVC, mat,
                    px[i], py[i], pz[i], px[i+1], py[i+1], pz[i+1],
                    c1x, c1y, c1z, c2x, c2y, c2z,
                    w0, w1, 0.06f, 0.003f, 0.003f, coreAlpha);

            drawCrossQuad(glowVC, mat,
                    px[i], py[i], pz[i], px[i+1], py[i+1], pz[i+1],
                    c1x, c1y, c1z, c2x, c2y, c2z,
                    gw0, gw1, 0.52f, 0.02f, 0.02f, glowAlpha);
        }
    }

    /**
     * Draws a cross-shaped pair of quads for one tendril segment, ensuring the
     * tendril is visible from all horizontal viewing angles.
     */
    private static void drawCrossQuad(VertexConsumer vc, Matrix4f mat,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float p1x, float p1y, float p1z,
            float p2x, float p2y, float p2z,
            float w0, float w1,
            float r, float g, float b, float a) {

        // Quad facing along perp1
        vc.vertex(mat, x0 - p1x*w0, y0 - p1y*w0, z0 - p1z*w0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0 + p1x*w0, y0 + p1y*w0, z0 + p1z*w0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1 + p1x*w1, y1 + p1y*w1, z1 + p1z*w1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1 - p1x*w1, y1 - p1y*w1, z1 - p1z*w1).color(r, g, b, a).endVertex();

        // Quad facing along perp2 (perpendicular to above)
        vc.vertex(mat, x0 - p2x*w0, y0 - p2y*w0, z0 - p2z*w0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0 + p2x*w0, y0 + p2y*w0, z0 + p2z*w0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1 + p2x*w1, y1 + p2y*w1, z1 + p2z*w1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1 - p2x*w1, y1 - p2y*w1, z1 - p2z*w1).color(r, g, b, a).endVertex();
    }
}
