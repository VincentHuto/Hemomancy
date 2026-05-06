package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StainedPriestessRenderer extends HumanoidMobRenderer<StainedPriestessEntity, HumanoidModel<StainedPriestessEntity>> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

    public StainedPriestessRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.85F);
    }

    @Override
    public ResourceLocation getTextureLocation(StainedPriestessEntity entity) {
        return TEXTURE;
    }
}
