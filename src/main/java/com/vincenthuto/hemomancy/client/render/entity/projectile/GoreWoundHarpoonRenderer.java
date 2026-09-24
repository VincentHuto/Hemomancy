package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.ExcoriatedSagittaryRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.GoreWoundHarpoonEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class GoreWoundHarpoonRenderer extends EntityRenderer<GoreWoundHarpoonEntity> {
    private final ModelPart barb;

    public GoreWoundHarpoonRenderer(EntityRendererProvider.Context context) {
        super(context);
        barb = context.bakeLayer(RecallBarbRenderer.LAYER);
    }

    @Override
    public void render(GoreWoundHarpoonEntity entity, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, entity.yRotO, entity.getYRot()) - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial, entity.xRotO, entity.getXRot())));
        pose.scale(0.65F, 0.65F, 0.65F);
        barb.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))),
                light, OverlayTexture.NO_OVERLAY);
        pose.popPose();

        if (entity.targetId() >= 0) {
            Entity shooter = entity.level().getEntity(entity.shooterId());
            if (shooter != null) {
                Vec3 from = shooter.getPosition(partial).add(0.0D, shooter.getBbHeight() * 0.7D, 0.0D)
                        .subtract(entity.getPosition(partial));
                VertexConsumer line = buffers.getBuffer(RenderType.leash());
                for (int plane = 0; plane < 2; plane++) {
                    for (int i = 0; i <= 24; i++) {
                        float t = i / 24.0F;
                        float x = (float) (from.x * t);
                        float y = (float) (from.y * t - Math.sin(t * Math.PI) * 0.15D);
                        float z = (float) (from.z * t);
                        float width = 0.018F;
                        for (int side = -1; side <= 1; side += 2) {
                            line.addVertex(pose.last().pose(), x + (plane == 0 ? side * width : 0.0F),
                                    y + (plane == 1 ? side * width : 0.0F), z)
                                    .setColor(180, 15, 28, 255).setLight(light);
                        }
                    }
                }
            }
        }
        super.render(entity, yaw, partial, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(GoreWoundHarpoonEntity entity) {
        return ExcoriatedSagittaryRenderer.TEXTURE;
    }
}
