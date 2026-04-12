package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.BloodThrallModel;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Renders the Blood Thrall — a small coagulated blood golem.
 * Scales up slightly when carrying blood.
 * When awaiting target selection, renders a blue venous leash
 * (a droopy, organic line-strip) from the thrall to the owner's hand.
 */
public class BloodThrallRenderer extends MobRenderer<BloodThrallEntity, BloodThrallModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/blood_thrall/blood_thrall.png");

    /** Number of segments in the venous leash line strip */
    private static final int LEASH_SEGMENTS = 24;
    /** Number of segments in the collar ring */
    private static final int COLLAR_SEGMENTS = 24;
    /** Radius of the collar ring */
    private static final float COLLAR_RADIUS = 0.35f;

    public BloodThrallRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BloodThrallModel(ctx.bakeLayer(BloodThrallModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public boolean shouldRender(BloodThrallEntity entity,
                                net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        // Always render when awaiting target so the leash to the player stays visible
        if (entity.getAwaitingTarget()) return true;
        return super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    @Override
    public ResourceLocation getTextureLocation(BloodThrallEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BloodThrallEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Scale up slightly when carrying blood
        float carryRatio = entity.getCarryRatio();
        float scale = 1.0f + carryRatio * 0.2f;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();

        // Render blue venous leash when awaiting target selection
        if (entity.getAwaitingTarget() && entity.getOwnerUUID() != null) {
            Player owner = entity.level().getPlayerByUUID(entity.getOwnerUUID());
            if (owner != null) {
                renderVenousLeash(entity, owner, partialTicks, poseStack, buffer);
            }
        }
    }

    /**
     * Renders a droopy, organic venous leash from the thrall to the owner's hand
     * using a line-strip with catenary sag and sinusoidal wobble.
     */
    private void renderVenousLeash(BloodThrallEntity thrall, Player owner, float partialTicks,
                                   PoseStack poseStack, MultiBufferSource buffer) {
        // The renderer's PoseStack origin is at the entity's feet, so raise
        // the leash anchor to the thrall's body center.
        float leashStartY = thrall.getBbHeight() * 0.5f + 0.5f;
        poseStack.pushPose();
        poseStack.translate(0, leashStartY, 0);

        // ── Compute interpolated world positions ──
        // Thrall body center
        double thrallX = Mth.lerp(partialTicks, thrall.xOld, thrall.getX());
        double thrallY = Mth.lerp(partialTicks, thrall.yOld, thrall.getY()) + leashStartY;
        double thrallZ = Mth.lerp(partialTicks, thrall.zOld, thrall.getZ());

        // Owner hand position
        double ownerX = Mth.lerp(partialTicks, owner.xOld, owner.getX());
        double ownerY = Mth.lerp(partialTicks, owner.yOld, owner.getY()) + owner.getBbHeight() * 0.65;
        double ownerZ = Mth.lerp(partialTicks, owner.zOld, owner.getZ());

        // Offset toward main hand side
        float yawRad = (float) Math.toRadians(Mth.lerp(partialTicks, owner.yRotO, owner.getYRot()));
        ownerX -= Math.cos(yawRad) * 0.35;
        ownerZ -= Math.sin(yawRad) * 0.35;

        // Crouch offset
        if (owner.isCrouching()) {
            ownerY -= 0.2;
        }

        // ── Relative offset from thrall leash anchor to owner hand ──
        float dx = (float) (ownerX - thrallX);
        float dy = (float) (ownerY - thrallY);
        float dz = (float) (ownerZ - thrallZ);

        // Time-based animation for organic pulsing
        float time = (thrall.tickCount + partialTicks) * 0.15f;

        PoseStack.Pose pose = poseStack.last();

        // ── Render collar ring at the leash anchor (thrall end) ──
        // Use RenderType.lines() so collar segments stay independent from the leash strip
        VertexConsumer collarConsumer = buffer.getBuffer(RenderType.lines());
        renderCollar(pose, collarConsumer, time);

        // ── Render the leash line strip ──
        VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lineStrip());

        for (int i = 0; i <= LEASH_SEGMENTS; i++) {
            float t = (float) i / LEASH_SEGMENTS;
            float tNext = (float) (i + 1) / LEASH_SEGMENTS;

            // ── Position along the line with catenary sag ──
            float px = dx * t;
            float py = dy * t;
            float pz = dz * t;

            // Catenary droop — strongest in the middle, like a hanging vein
            float sag = (float) (-Math.sin(t * Math.PI) * 0.4f);
            // Add a subtle pulsing to the sag
            sag *= 1.0f + 0.15f * (float) Math.sin(time * 2.0 + t * 4.0);
            py += sag;

            // ── Organic sinusoidal wobble ──
            float wobblePhase = t * 10.0f + time;
            float wobbleX = (float) (Math.sin(wobblePhase) * 0.04);
            float wobbleZ = (float) (Math.cos(wobblePhase * 0.7 + 1.3) * 0.04);
            // Secondary slower wobble for organic irregularity
            wobbleX += (float) (Math.sin(wobblePhase * 0.4 + 2.0) * 0.02);
            wobbleZ += (float) (Math.cos(wobblePhase * 0.3 + 0.7) * 0.02);
            px += wobbleX;
            pz += wobbleZ;

            // ── Normal direction (for lineStrip) ──
            float nx = dx * tNext - px;
            float ny = dy * tNext + (float) (-Math.sin(tNext * Math.PI) * 0.4f) - py;
            float nz = dz * tNext - pz;
            float nLen = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            if (nLen < 1e-5f) { nx = 0; ny = 1; nz = 0; nLen = 1; }
            nx /= nLen;
            ny /= nLen;
            nz /= nLen;

            // ── Color: dark blue-purple venous, pulsing along the length ──
            float pulse = (float) (Math.sin(wobblePhase * 0.5) * 0.5 + 0.5);
            int r = (int) (20 + pulse * 30);   // 20..50
            int g = (int) (10 + pulse * 40);   // 10..50
            int b = (int) (120 + pulse * 80);  // 120..200
            int a = (int) (180 + 40 * (float) Math.sin(t * Math.PI)); // brighter in middle

            lineConsumer.vertex(pose.pose(), px, py, pz)
                    .color(r, g, b, a)
                    .normal(pose.normal(), nx, ny, nz)
                    .endVertex();
        }

        poseStack.popPose();
    }

    /**
     * Renders a small pulsing collar ring at the origin (leash anchor on the thrall).
     * Uses RenderType.lines() so each segment is independent.
     */
    private void renderCollar(PoseStack.Pose pose, VertexConsumer consumer, float time) {
        // Slight pulse on the collar radius
        float pulse = 1.0f + 0.1f * (float) Math.sin(time * 3.0);
        float rad = COLLAR_RADIUS * pulse;

        for (int i = 0; i < COLLAR_SEGMENTS; i++) {
            float angle0 = (float) (i * 2.0 * Math.PI / COLLAR_SEGMENTS);
            float angle1 = (float) ((i + 1) * 2.0 * Math.PI / COLLAR_SEGMENTS);

            float x0 = (float) Math.cos(angle0) * rad;
            float z0 = (float) Math.sin(angle0) * rad;
            float x1 = (float) Math.cos(angle1) * rad;
            float z1 = (float) Math.sin(angle1) * rad;

            // Normal along the segment direction
            float nx = x1 - x0;
            float nz = z1 - z0;
            float nLen = Mth.sqrt(nx * nx + nz * nz);
            if (nLen < 1e-5f) { nx = 0; nz = 1; nLen = 1; }
            nx /= nLen;
            nz /= nLen;

            // Color: slightly brighter venous blue for the collar
            float colPulse = (float) (Math.sin(angle0 * 2.0 + time) * 0.5 + 0.5);
            int cr = (int) (30 + colPulse * 25);
            int cg = (int) (15 + colPulse * 35);
            int cb = (int) (140 + colPulse * 60);

            // Each line segment needs two vertices
            consumer.vertex(pose.pose(), x0, 0, z0)
                    .color(cr, cg, cb, 220)
                    .normal(pose.normal(), nx, 0, nz)
                    .endVertex();
            consumer.vertex(pose.pose(), x1, 0, z1)
                    .color(cr, cg, cb, 220)
                    .normal(pose.normal(), nx, 0, nz)
                    .endVertex();
        }
    }
}
