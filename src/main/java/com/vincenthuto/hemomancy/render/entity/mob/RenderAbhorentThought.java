package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityAbhorentThought;
import com.vincenthuto.hemomancy.model.entity.mob.ModelAbhorentThought;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderAbhorentThought extends MobRenderer<EntityAbhorentThought, ModelAbhorentThought<EntityAbhorentThought>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/abhorent_thought/model_abhorent_thought.png");

	public RenderAbhorentThought(Context renderManagerIn) {
		super(renderManagerIn, new ModelAbhorentThought<EntityAbhorentThought>(renderManagerIn.bakeLayer(ModelAbhorentThought.abhorent_thought)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityAbhorentThought entity) {
		return TEXTURE;

	}

}
