package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.AnnettaKnowlesModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AnnettaKnowlesRenderer extends HumanoidMobRenderer<AnnettaKnowlesEntity, AnnettaKnowlesModel<AnnettaKnowlesEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/annetta_knowles.png");

    public AnnettaKnowlesRenderer(Context context) {
        super(context, new AnnettaKnowlesModel<>(context.bakeLayer(AnnettaKnowlesModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(AnnettaKnowlesEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AnnettaKnowlesEntity entity, PoseStack poseStack, float partialTick) {
        float scale = 1.0F;
        if (entity.getEncounterState() == AnnettaKnowlesEntity.EncounterState.COWERING) {
            scale = 0.92F;
        } else if (entity.getVisualState() == AnnettaKnowlesEntity.VISUAL_SILVER_AURA
                || entity.getVisualState() == AnnettaKnowlesEntity.VISUAL_CURED_SUPPORT) {
            scale = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.45F) * 0.035F;
        }
        poseStack.scale(scale, scale, scale);
    }
}
