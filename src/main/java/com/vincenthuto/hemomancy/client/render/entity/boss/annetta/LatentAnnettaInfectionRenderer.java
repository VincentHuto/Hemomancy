package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.LatentAnnettaInfectionModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LatentAnnettaInfectionRenderer extends HumanoidMobRenderer<LatentAnnettaInfectionEntity, LatentAnnettaInfectionModel<LatentAnnettaInfectionEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/latent_annetta_infection.png");

    public LatentAnnettaInfectionRenderer(EntityRendererProvider.Context context) {
        super(context, new LatentAnnettaInfectionModel<>(context.bakeLayer(LatentAnnettaInfectionModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(LatentAnnettaInfectionEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(LatentAnnettaInfectionEntity entity, PoseStack poseStack, float partialTick) {
        float scale = 1.0F;
        if (entity.getVisualState() == LatentAnnettaInfectionEntity.VISUAL_INFECTION_BLOOM) {
            scale += (float) Math.sin((entity.tickCount + partialTick) * 0.4F) * 0.05F;
        } else if (entity.getVisualState() == LatentAnnettaInfectionEntity.VISUAL_PRESSURE_SPIKE) {
            scale = 0.96F + (float) Math.sin((entity.tickCount + partialTick) * 0.8F) * 0.035F;
        }
        poseStack.scale(scale, scale, scale);
    }
}
