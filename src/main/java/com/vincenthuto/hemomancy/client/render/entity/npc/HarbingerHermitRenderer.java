package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerHermitModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class HarbingerHermitRenderer extends MobRenderer<HarbingerHermitEntity, HarbingerHermitModel<HarbingerHermitEntity>> {

    protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/harbinger_hermit/harbinger_hermit.png");
    private static final float CHEST_BEAM_Y = 1.28F;
    private static final float CHEST_FLARE_Z = -0.16F;
    private static final float FAREWELL_MIN_BEAM_WIDTH = 0.048F;
    private static final float FAREWELL_BEAM_ALPHA = 0.76F;
    private static final int FAREWELL_BEAM_COUNT = 9;
    private static final boolean DEBUG_LOOP_FAREWELL_DEATH = false;

    public HarbingerHermitRenderer(Context context) {
        super(context, new HarbingerHermitModel<>(context.bakeLayer(HarbingerHermitModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public void render(HarbingerHermitEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float progress = getFarewellDeathRenderProgress(entity, partialTick);
        float originalShadow = this.shadowRadius;
        if (progress > 0.0F) {
            this.shadowRadius = originalShadow * (1.0F - progress);
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        this.shadowRadius = originalShadow;

        if (progress > 0.0F) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - getFarewellBodyYaw(entity, partialTick)));
            VertexConsumer visibleRays = buffer.getBuffer(RenderType.dragonRays());
            renderFarewellDeathBeams(entity, partialTick, poseStack, visibleRays, 1.0F);
            renderFarewellChestFlare(entity, partialTick, poseStack, visibleRays, 1.0F);
            renderFarewellDeathBeams(entity, partialTick, poseStack, buffer.getBuffer(RenderType.dragonRaysDepth()), 0.65F);
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(HarbingerHermitEntity entity) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(HarbingerHermitEntity entity, boolean bodyVisible, boolean translucent,
                                       boolean glowing) {
        if (bodyVisible && shouldRenderFarewellDeath(entity)) {
            float progress = getFarewellDeathRenderProgress(entity, 0.0F);
            return HemoRenderTypes.hermitFarewellDissolve(
                    getTextureLocation(entity),
                    entity.tickCount + progress,
                    progress,
                    entity.getId() * 0.137F);
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    private static boolean shouldRenderFarewellDeath(HarbingerHermitEntity entity) {
        return DEBUG_LOOP_FAREWELL_DEATH || entity.isFarewellDying();
    }

    private static float getFarewellDeathRenderProgress(HarbingerHermitEntity entity, float partialTick) {
        if (DEBUG_LOOP_FAREWELL_DEATH) {
            return ((entity.tickCount % HarbingerHermitEntity.FAREWELL_DEATH_DURATION) + partialTick)
                    / (float) HarbingerHermitEntity.FAREWELL_DEATH_DURATION;
        }
        return entity.getFarewellDeathProgress(partialTick);
    }

    private static float getFarewellBodyYaw(HarbingerHermitEntity entity, float partialTick) {
        return Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
    }

    private void renderFarewellDeathBeams(HarbingerHermitEntity entity, float partialTick, PoseStack poseStack,
                                          VertexConsumer vertexConsumer, float alphaScale) {
        float progress = getFarewellDeathRenderProgress(entity, partialTick);
        float pulse = Mth.sin(progress * Mth.PI);
        if (pulse <= 0.015F) return;

        float age = entity.tickCount + partialTick;
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < FAREWELL_BEAM_COUNT; i++) {
            float phase = entity.getId() * 0.37F + i * (Mth.PI * 2.0F / FAREWELL_BEAM_COUNT) + age * 0.045F;
            float flare = pulse * (0.62F + 0.38F * Mth.sin(age * 0.24F + i * 1.73F));
            float dirX = Mth.cos(phase) * 0.72F;
            float dirY = 0.14F + Math.abs(Mth.sin(phase * 1.47F)) * 0.68F;
            float dirZ = Mth.sin(phase) * 0.38F - 0.58F;
            float invLength = Mth.invSqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            dirX *= invLength;
            dirY *= invLength;
            dirZ *= invLength;

            float length = (0.72F + 0.95F * flare) * (1.0F - progress * 0.08F);
            float width = (FAREWELL_MIN_BEAM_WIDTH + 0.11F * flare) * (1.0F - progress * 0.25F);
            float alpha = FAREWELL_BEAM_ALPHA * pulse * (1.0F - progress * 0.18F) * alphaScale;
            float sideX = -dirZ;
            float sideZ = dirX;
            float sideInvLength = Mth.invSqrt(sideX * sideX + sideZ * sideZ + 0.0001F);
            sideX *= sideInvLength;
            sideZ *= sideInvLength;

            addBeamTriangle(vertexConsumer, matrix,
                    sideX * width, CHEST_BEAM_Y, CHEST_FLARE_Z + sideZ * width,
                    -sideX * width, CHEST_BEAM_Y, CHEST_FLARE_Z - sideZ * width,
                    dirX * length, CHEST_BEAM_Y + dirY * length, CHEST_FLARE_Z + dirZ * length,
                    alpha);
        }
    }

    private void renderFarewellChestFlare(HarbingerHermitEntity entity, float partialTick, PoseStack poseStack,
                                          VertexConsumer vertexConsumer, float alphaScale) {
        float progress = getFarewellDeathRenderProgress(entity, partialTick);
        float pulse = Mth.sin(progress * Mth.PI);
        if (pulse <= 0.015F) return;

        float age = entity.tickCount + partialTick;
        float flare = pulse * (0.74F + 0.26F * Mth.sin(age * 0.34F));
        float radius = (0.105F + 0.15F * flare) * (1.0F - progress * 0.2F);
        float depthRadius = radius * 0.58F;
        float alpha = 0.88F * pulse * (1.0F - progress * 0.18F) * alphaScale;
        Matrix4f matrix = poseStack.last().pose();

        addFlareDiamond(vertexConsumer, matrix, 0.0F, CHEST_BEAM_Y, CHEST_FLARE_Z - 0.028F,
                radius * 1.18F, radius, alpha);
        addSideFlareDiamond(vertexConsumer, matrix, 0.0F, CHEST_BEAM_Y, CHEST_FLARE_Z,
                depthRadius, radius * 0.86F, alpha * 0.58F);
    }

    private static void addBeamTriangle(VertexConsumer vertexConsumer, Matrix4f matrix,
                                        float x1, float y1, float z1,
                                        float x2, float y2, float z2,
                                        float x3, float y3, float z3,
                                        float alpha) {
        vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(1.0F, 0.04F, 0.02F, alpha);
        vertexConsumer.addVertex(matrix, x2, y2, z2).setColor(0.82F, 0.0F, 0.0F, alpha * 0.74F);
        vertexConsumer.addVertex(matrix, x3, y3, z3).setColor(1.0F, 0.12F, 0.04F, alpha * 0.18F);
    }

    private static void addFlareDiamond(VertexConsumer vertexConsumer, Matrix4f matrix,
                                        float x, float y, float z,
                                        float halfWidth, float halfHeight,
                                        float alpha) {
        addFlareTriangle(vertexConsumer, matrix,
                x, y + halfHeight, z,
                x + halfWidth, y, z,
                x, y - halfHeight, z,
                alpha);
        addFlareTriangle(vertexConsumer, matrix,
                x, y + halfHeight, z,
                x, y - halfHeight, z,
                x - halfWidth, y, z,
                alpha * 0.84F);
    }

    private static void addSideFlareDiamond(VertexConsumer vertexConsumer, Matrix4f matrix,
                                            float x, float y, float z,
                                            float halfDepth, float halfHeight,
                                            float alpha) {
        addFlareTriangle(vertexConsumer, matrix,
                x, y + halfHeight, z,
                x, y, z - halfDepth,
                x, y - halfHeight, z,
                alpha);
        addFlareTriangle(vertexConsumer, matrix,
                x, y + halfHeight, z,
                x, y - halfHeight, z,
                x, y, z + halfDepth,
                alpha * 0.74F);
    }

    private static void addFlareTriangle(VertexConsumer vertexConsumer, Matrix4f matrix,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2,
                                         float x3, float y3, float z3,
                                         float alpha) {
        vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(1.0F, 0.02F, 0.02F, alpha * 0.55F);
        vertexConsumer.addVertex(matrix, x2, y2, z2).setColor(1.0F, 0.11F, 0.04F, alpha);
        vertexConsumer.addVertex(matrix, x3, y3, z3).setColor(0.95F, 0.0F, 0.0F, alpha * 0.55F);
    }
}
