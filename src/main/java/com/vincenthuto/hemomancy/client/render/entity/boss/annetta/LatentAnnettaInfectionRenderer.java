package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LatentAnnettaInfectionRenderer extends HumanoidMobRenderer<LatentAnnettaInfectionEntity, HumanoidModel<LatentAnnettaInfectionEntity>> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

    public LatentAnnettaInfectionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(LatentAnnettaInfectionEntity entity) {
        return TEXTURE;
    }
}
