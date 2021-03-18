package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityLumpOfThought;
import com.huto.hemomancy.model.entity.mob.ModelLumpOfThought;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderLumpOfThought extends MobRenderer<EntityLumpOfThought, ModelLumpOfThought> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/lump_of_thought/model_lump_of_thought.png");

	public RenderLumpOfThought(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelLumpOfThought(), 0.8f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityLumpOfThought entity) {
		return TEXTURE;

	}

}
