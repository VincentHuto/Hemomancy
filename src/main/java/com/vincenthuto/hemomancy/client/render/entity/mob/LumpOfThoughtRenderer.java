package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.LumpOfThoughtModel;
import com.vincenthuto.hemomancy.common.entity.mob.LumpOfThoughtEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LumpOfThoughtRenderer extends MobRenderer<LumpOfThoughtEntity, LumpOfThoughtModel<LumpOfThoughtEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/lump_of_thought/model_lump_of_thought.png");

	public LumpOfThoughtRenderer(Context renderManagerIn) {
		super(renderManagerIn, new LumpOfThoughtModel<LumpOfThoughtEntity>(renderManagerIn.bakeLayer(LumpOfThoughtModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(LumpOfThoughtEntity entity) {
		return TEXTURE;

	}

}
