package com.vincenthuto.hemomancy.client.render.entity.boss.velorum;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder renderer for Velorum (The Frozen Martyr).
 * Uses the vanilla humanoid model and a blank texture so he can be spawned
 * without crashing. Replace with a dedicated model + texture when artwork
 * is ready.
 */
public class VelorumRenderer extends HumanoidMobRenderer<VelorumEntity, HumanoidModel<VelorumEntity>> {

    // TODO: replace with dedicated velorum texture
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

    public VelorumRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(VelorumEntity entity) {
        return TEXTURE;
    }
}
