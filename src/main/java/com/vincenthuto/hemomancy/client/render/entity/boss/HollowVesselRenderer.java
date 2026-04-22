package com.vincenthuto.hemomancy.client.render.entity.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.HollowVesselModel;
import com.vincenthuto.hemomancy.common.entity.boss.HollowVesselEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Hollow Vessel (Saint Hemorath).
 * Uses the dedicated {@link HollowVesselModel} — an emaciated, elongated
 * humanoid distinct from the standard Harbinger Vicar silhouette.
 */
public class HollowVesselRenderer extends MobRenderer<HollowVesselEntity, HollowVesselModel<HollowVesselEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/hollow_vessel/hollow_vessel.png");

	public HollowVesselRenderer(Context context) {
		super(context, new HollowVesselModel<>(context.bakeLayer(HollowVesselModel.LAYER_LOCATION)), 0.8F);
	}

	@Override
	public ResourceLocation getTextureLocation(HollowVesselEntity entity) {
		return TEXTURE;
	}
}
