package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.AbhorentThoughtModel;
import com.vincenthuto.hemomancy.common.entity.mob.AbhorentThoughtEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbhorentThoughtRenderer extends MobRenderer<AbhorentThoughtEntity, AbhorentThoughtModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/abhorent_thought/model_abhorent_thought.png");

	public AbhorentThoughtRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new AbhorentThoughtModel(renderManagerIn.bakeLayer(AbhorentThoughtModel.abhorent_thought)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(AbhorentThoughtEntity entity) {
		return TEXTURE;

	}

}
