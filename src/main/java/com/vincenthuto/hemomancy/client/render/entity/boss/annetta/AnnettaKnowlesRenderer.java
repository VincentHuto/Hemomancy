package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder renderer for Annetta Knowles (The Stained Priestess).
 * Uses the vanilla humanoid model and a blank texture so she can be spawned
 * without crashing. Replace with a dedicated model + texture when artwork
 * is ready.
 */
public class AnnettaKnowlesRenderer extends HumanoidMobRenderer<AnnettaKnowlesEntity, HumanoidModel<AnnettaKnowlesEntity>> {

    // TODO: replace with dedicated annetta_knowles texture
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

    public AnnettaKnowlesRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(AnnettaKnowlesEntity entity) {
        return TEXTURE;
    }
}
