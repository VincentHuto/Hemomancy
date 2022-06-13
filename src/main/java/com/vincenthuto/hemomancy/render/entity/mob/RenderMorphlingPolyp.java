package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityMorphlingPolyp;
import com.vincenthuto.hemomancy.model.entity.mob.ModelMorphlingPolyp;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderMorphlingPolyp extends MobRenderer<EntityMorphlingPolyp, ModelMorphlingPolyp<EntityMorphlingPolyp>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/morphling_polyp/model_morphling_polyp.png");

	public RenderMorphlingPolyp(Context renderManagerIn) {
		super(renderManagerIn, new ModelMorphlingPolyp<EntityMorphlingPolyp>(renderManagerIn.bakeLayer(ModelMorphlingPolyp.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityMorphlingPolyp entity) {
		return TEXTURE;

	}

}
