package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityChthonian;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChthonian;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderChthonian extends MobRenderer<EntityChthonian, ModelChthonian<EntityChthonian>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian/model_chthonian.png");

	public RenderChthonian(Context renderManagerIn) {
		super(renderManagerIn, new ModelChthonian<EntityChthonian>(renderManagerIn.bakeLayer(ModelChthonian.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityChthonian entity) {
		return TEXTURE;

	}

}
