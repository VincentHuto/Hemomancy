package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityDrudge;
import com.huto.hemomancy.model.entity.mob.ModelDrudge;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderDrudge extends MobRenderer<EntityDrudge, ModelDrudge> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/drudge/model_drudge.png");

	public RenderDrudge(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelDrudge(), 0.25f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityDrudge entity) {
		return TEXTURE;

	}

}
