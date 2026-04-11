package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.HemojellyModel;
import com.vincenthuto.hemomancy.common.entity.mob.HemojellyEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HemojellyRenderer extends MobRenderer<HemojellyEntity, HemojellyModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/hemojelly/model_hemojelly.png");

	public HemojellyRenderer(Context renderManagerIn) {
		super(renderManagerIn, new HemojellyModel(renderManagerIn.bakeLayer(HemojellyModel.LAYER_LOCATION)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(HemojellyEntity entity) {
		return TEXTURE;
	}
}
