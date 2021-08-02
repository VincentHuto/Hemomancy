package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityFungling;
import com.huto.hemomancy.model.entity.mob.ModelFungling;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderFungling extends MobRenderer<EntityFungling, ModelFungling> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fungling/model_fungling.png");

	public RenderFungling(Context renderManagerIn) {
		super(renderManagerIn, new ModelFungling(), 0.5f);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityFungling entity) {
		return TEXTURE;

	}

}
