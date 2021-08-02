package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityFargone;
import com.huto.hemomancy.model.entity.mob.ModelFargone;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderFargone extends MobRenderer<EntityFargone, ModelFargone> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fargone/model_fargone.png");

	public RenderFargone(Context renderManagerIn) {
		super(renderManagerIn, new ModelFargone(), 0.8f);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityFargone entity) {
		return TEXTURE;

	}

}
