package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.FrozenClotModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.FrozenClotEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrozenClotRenderer extends MobRenderer<FrozenClotEntity, FrozenClotModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/frozen_clot/model_frozen_clot.png");

	public FrozenClotRenderer(Context renderManagerIn) {
		super(renderManagerIn, new FrozenClotModel(renderManagerIn.bakeLayer(FrozenClotModel.LAYER_LOCATION)), 0.6F);
	}

	@Override
	public ResourceLocation getTextureLocation(FrozenClotEntity entity) {
		return TEXTURE;
	}
}
