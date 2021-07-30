package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityThirster;
import com.huto.hemomancy.model.entity.mob.ModelThirster;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import ResourceLocation;

public class RenderThirster extends MobRenderer<EntityThirster, ModelThirster> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/thirster/model_thirster.png");

	public RenderThirster(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelThirster(), 0.8f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityThirster entity) {
		return TEXTURE;

	}

}
