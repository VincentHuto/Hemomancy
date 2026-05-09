package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StainedPriestessRenderer extends HumanoidMobRenderer<StainedPriestessEntity, StainedPriestessModel<StainedPriestessEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/stained_priestess.png");

    public StainedPriestessRenderer(EntityRendererProvider.Context context) {
        super(context, new StainedPriestessModel<>(context.bakeLayer(StainedPriestessModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    public ResourceLocation getTextureLocation(StainedPriestessEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(StainedPriestessEntity entity, PoseStack poseStack, float partialTick) {
        float scale = 1.0F;
        if (entity.getVisualState() == StainedPriestessEntity.VISUAL_PRESSURE_BLOOM) {
            scale += (float) Math.sin((entity.tickCount + partialTick) * 0.55F) * 0.045F;
        } else if (entity.getVisualState() == StainedPriestessEntity.VISUAL_LUNGE) {
            scale *= 0.97F;
        } else if (entity.getVisualState() == StainedPriestessEntity.VISUAL_LANCE) {
            scale *= 1.03F;
        }
        poseStack.scale(scale, scale, scale);
    }
}
