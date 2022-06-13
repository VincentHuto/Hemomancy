package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityFungling;
import com.vincenthuto.hemomancy.model.entity.mob.ModelFungling;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderFungling extends MobRenderer<EntityFungling, ModelFungling<EntityFungling>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fungling/model_fungling.png");

	public RenderFungling(Context renderManagerIn) {
		super(renderManagerIn, new ModelFungling<EntityFungling>(renderManagerIn.bakeLayer(ModelFungling.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityFungling entity) {
		return TEXTURE;

	}

}
