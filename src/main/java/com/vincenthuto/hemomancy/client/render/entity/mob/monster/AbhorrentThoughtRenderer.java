package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.AbhorrentThoughtModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AbhorrentThoughtEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbhorrentThoughtRenderer extends MobRenderer<AbhorrentThoughtEntity, AbhorrentThoughtModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/abhorrent_thought/model_abhorrent_thought.png");

	public AbhorrentThoughtRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new AbhorrentThoughtModel(renderManagerIn.bakeLayer(AbhorrentThoughtModel.abhorrent_thought)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(AbhorrentThoughtEntity entity) {
		return TEXTURE;

	}

}
