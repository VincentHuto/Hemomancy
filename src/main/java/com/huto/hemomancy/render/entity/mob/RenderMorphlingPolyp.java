package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityMorphlingPolyp;
import com.huto.hemomancy.model.entity.mob.ModelMorphlingPolyp;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderMorphlingPolyp extends MobRenderer<EntityMorphlingPolyp, ModelMorphlingPolyp> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/morphling_polyp/model_morphling_polyp.png");

	public RenderMorphlingPolyp(Context renderManagerIn) {
		super(renderManagerIn, new ModelMorphlingPolyp(), 0.8f);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityMorphlingPolyp entity) {
		return TEXTURE;

	}

}
