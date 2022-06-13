package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityFargone;
import com.vincenthuto.hemomancy.model.entity.mob.ModelFargone;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderFargone extends MobRenderer<EntityFargone, ModelFargone<EntityFargone>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fargone/model_fargone.png");

	public RenderFargone(Context renderManagerIn) {
		super(renderManagerIn, new ModelFargone<EntityFargone>(renderManagerIn.bakeLayer(ModelFargone.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityFargone entity) {
		return TEXTURE;

	}

}
