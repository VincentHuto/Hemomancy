package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityChitinite;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChitinite;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderChitinite extends MobRenderer<EntityChitinite, ModelChitinite<EntityChitinite>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chitinite/model_chitinite.png");

	public RenderChitinite(Context renderManagerIn) {
		super(renderManagerIn, new ModelChitinite<EntityChitinite>(renderManagerIn.bakeLayer(ModelChitinite.LAYER_LOCATION)), 0.5F);
		//this.addLayer(new RenderChitiniteLayer(this));

	
	}

	@Override
	public ResourceLocation getTextureLocation(EntityChitinite entity) {
		return TEXTURE;

	}

}
