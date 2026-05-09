package com.vincenthuto.hemomancy.client.render.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VelorumRenderer extends HumanoidMobRenderer<VelorumEntity, VelorumModel<VelorumEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/velorum/velorum.png");

    public VelorumRenderer(Context context) {
        super(context, new VelorumModel<>(context.bakeLayer(VelorumModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(VelorumEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(VelorumEntity entity, PoseStack poseStack, float partialTick) {
        float scale = 1.0F;
        if (entity.isMartyrdom()) {
            scale *= 1.02F + (float) Math.sin((entity.tickCount + partialTick) * 0.5F) * 0.015F;
        }
        if (entity.getVisualState() == VelorumEntity.VISUAL_FROST_NOVA) {
            scale *= 0.98F;
        } else if (entity.getVisualState() == VelorumEntity.VISUAL_VEIL) {
            scale *= 1.04F;
        } else if (entity.getVisualState() == VelorumEntity.VISUAL_SILENCE_DRAIN) {
            scale *= 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.7F) * 0.04F;
        }
        poseStack.scale(scale, scale, scale);
    }
}
