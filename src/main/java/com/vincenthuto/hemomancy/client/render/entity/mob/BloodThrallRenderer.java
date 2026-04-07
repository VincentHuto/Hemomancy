package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.BloodThrallModel;
import com.vincenthuto.hemomancy.common.entity.mob.BloodThrallEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Blood Thrall — a small coagulated blood golem.
 * Tints redder / scales up slightly as it carries more blood.
 */
public class BloodThrallRenderer extends MobRenderer<BloodThrallEntity, BloodThrallModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/blood_thrall/blood_thrall.png");

    public BloodThrallRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BloodThrallModel(ctx.bakeLayer(BloodThrallModel.LAYER_LOCATION)), 0.35F);
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
    }
}
