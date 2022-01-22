package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityLumpOfThought;
import com.vincenthuto.hemomancy.model.entity.mob.ModelLumpOfThought;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderLumpOfThought extends MobRenderer<EntityLumpOfThought, ModelLumpOfThought<EntityLumpOfThought>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/lump_of_thought/model_lump_of_thought.png");

	public RenderLumpOfThought(Context renderManagerIn) {
		super(renderManagerIn, new ModelLumpOfThought<EntityLumpOfThought>(renderManagerIn.bakeLayer(ModelLumpOfThought.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityLumpOfThought entity) {
		return TEXTURE;

	}

}
