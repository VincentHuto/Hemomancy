package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.MarrowMaggotModel;
import com.vincenthuto.hemomancy.common.entity.mob.MarrowMaggotEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MarrowMaggotRenderer extends MobRenderer<MarrowMaggotEntity, MarrowMaggotModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/marrow_maggot/model_marrow_maggot.png");

	public MarrowMaggotRenderer(Context renderManagerIn) {
		super(renderManagerIn, new MarrowMaggotModel(renderManagerIn.bakeLayer(MarrowMaggotModel.LAYER_LOCATION)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(MarrowMaggotEntity entity) {
		return TEXTURE;
	}
}
