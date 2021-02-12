package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityFargone;
import com.huto.hemomancy.model.entity.mob.ModelFargone;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderFargone extends MobRenderer<EntityFargone, ModelFargone> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fargone/model_fargone.png");

	public RenderFargone(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelFargone(), 0.8f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityFargone entity) {
		return TEXTURE;

	}

}
