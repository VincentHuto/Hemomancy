package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.ThirsterEntity;
import com.vincenthuto.hemomancy.model.entity.mob.ThirsterModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ThirsterRenderer extends MobRenderer<ThirsterEntity, ThirsterModel<ThirsterEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/thirster/model_thirster.png");

	public ThirsterRenderer(Context renderManagerIn) {
		super(renderManagerIn, new ThirsterModel<ThirsterEntity>(renderManagerIn.bakeLayer(ThirsterModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(ThirsterEntity entity) {
		return TEXTURE;

	}

}