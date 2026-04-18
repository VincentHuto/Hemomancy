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

    // Number of tendril roots
    private static final int TENDRIL_COUNT = 5;
    // Segments per tendril (higher = smoother curve)
    private static final int TENDRIL_SEGMENTS = 8;
    // Y position where the tendril base starts (just below the seed)
    private static final float START_Y = 0.08f;
    // Total downward reach (into the ground surface)
    private static final float REACH = 0.42f;
    // Maximum horizontal spread at the tendril tip
    private static final float MAX_SPREAD = 0.22f;
    // Cross-section half-width of the black core
    private static final float CORE_WIDTH = 0.022f;
    // Glow layer is wider than the core
    private static final float GLOW_WIDTH_MULT = 1.7f;

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
            float baseAngle = (float) (i * Math.PI * 2.0 / TENDRIL_COUNT);
            drawTendril(coreVC, glowVC, mat, time, baseAngle);
        }

        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    /**
     * Draws one root tendril. The tendril starts near the seed's base, curves
     * outward and downward with a slow writhing animation, and tapers to a point.
     */
    private static void drawTendril(VertexConsumer coreVC, VertexConsumer glowVC,
            Matrix4f mat, float time, float baseAngle) {

        float[] px = new float[TENDRIL_SEGMENTS + 1];
        float[] py = new float[TENDRIL_SEGMENTS + 1];
        float[] pz = new float[TENDRIL_SEGMENTS + 1];

        for (int i = 0; i <= TENDRIL_SEGMENTS; i++) {
            float t = (float) i / TENDRIL_SEGMENTS;
            // Low-freq swing of the whole arm + high-freq probe at the tip
            float wave = (float) (
                Math.sin(time * 0.65f + baseAngle * 2.1f) * 0.07f
              + Math.sin(time * 1.9f + baseAngle * 1.3f + t * 4.5f) * 0.04f * t);
            float spread = MAX_SPREAD * t * t; // quadratic: stays close at root, fans out at tip
            float angle = baseAngle + wave;
            px[i] = (float) Math.cos(angle) * spread;
            py[i] = START_Y - t * REACH;
            pz[i] = (float) Math.sin(angle) * spread;
        }

        for (int i = 0; i < TENDRIL_SEGMENTS; i++) {
            float t0 = (float) i / TENDRIL_SEGMENTS;
            float w0 = CORE_WIDTH * (1.0f - t0);
            float w1 = CORE_WIDTH * (1.0f - (float)(i + 1) / TENDRIL_SEGMENTS);

            // Compute segment direction
            float dx = px[i+1] - px[i];
            float dy = py[i+1] - py[i];
            float dz = pz[i+1] - pz[i];
            float invLen = 1.0f / Mth.sqrt(dx*dx + dy*dy + dz*dz);
            dx *= invLen; dy *= invLen; dz *= invLen;

            // First perpendicular — choose an axis not parallel to segment
            float ax = 1, ay = 0, az = 0;
            if (Math.abs(dx) > 0.9f) { ax = 0; az = 1; }
            float c1x = dy*az - dz*ay;
            float c1y = dz*ax - dx*az;
            float c1z = dx*ay - dy*ax;
            float c1len = Mth.sqrt(c1x*c1x + c1y*c1y + c1z*c1z);
            if (c1len > 1e-5f) { c1x /= c1len; c1y /= c1len; c1z /= c1len; }

            // Second perpendicular via cross product
            float c2x = dy*c1z - dz*c1y;
            float c2y = dz*c1x - dx*c1z;
            float c2z = dx*c1y - dy*c1x;

            float coreAlpha = 0.88f - t0 * 0.30f;
            float glowAlpha = 0.55f - t0 * 0.45f;
            float gw0 = w0 * GLOW_WIDTH_MULT;
            float gw1 = w1 * GLOW_WIDTH_MULT;

            // Core: near-black, very dark red tint
            drawCrossQuad(coreVC, mat,
                    px[i], py[i], pz[i], px[i+1], py[i+1], pz[i+1],
                    c1x, c1y, c1z, c2x, c2y, c2z,
                    w0, w1, 0.06f, 0.003f, 0.003f, coreAlpha);

            // Glow: crimson/dark red halo around the core
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
