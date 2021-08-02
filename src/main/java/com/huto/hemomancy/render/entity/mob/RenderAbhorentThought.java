package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityAbhorentThought;
import com.huto.hemomancy.model.entity.mob.ModelAbhorentThought;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderAbhorentThought extends MobRenderer<EntityAbhorentThought, ModelAbhorentThought> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/abhorent_thought/model_abhorent_thought.png");

	public RenderAbhorentThought(Context renderManagerIn) {
		super(renderManagerIn, new ModelAbhorentThought(), 0.8f);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityAbhorentThought entity) {
		return TEXTURE;

	}

}
