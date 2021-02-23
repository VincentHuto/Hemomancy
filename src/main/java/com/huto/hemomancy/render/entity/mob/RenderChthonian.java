package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityChthonian;
import com.huto.hemomancy.model.entity.mob.ModelChthonian;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderChthonian extends MobRenderer<EntityChthonian, ModelChthonian> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian/model_chthonian.png");

	public RenderChthonian(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelChthonian(), 0.45f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityChthonian entity) {
		return TEXTURE;

	}

}
