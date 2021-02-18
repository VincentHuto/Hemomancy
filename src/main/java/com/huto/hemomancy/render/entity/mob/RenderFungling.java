package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityFungling;
import com.huto.hemomancy.model.entity.mob.ModelFungling;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderFungling extends MobRenderer<EntityFungling, ModelFungling> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fungling/model_fungling.png");

	public RenderFungling(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelFungling(), 0.5f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityFungling entity) {
		return TEXTURE;

	}

}
