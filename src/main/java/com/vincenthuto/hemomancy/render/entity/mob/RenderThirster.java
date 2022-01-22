package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityThirster;
import com.vincenthuto.hemomancy.model.entity.mob.ModelThirster;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderThirster extends MobRenderer<EntityThirster, ModelThirster<EntityThirster>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/thirster/model_thirster.png");

	public RenderThirster(Context renderManagerIn) {
		super(renderManagerIn, new ModelThirster<EntityThirster>(renderManagerIn.bakeLayer(ModelThirster.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityThirster entity) {
		return TEXTURE;

	}

}
