package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.MorphlingPolypModel;
import com.vincenthuto.hemomancy.common.entity.mob.MorphlingPolypEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MorphlingPolypRenderer extends MobRenderer<MorphlingPolypEntity, MorphlingPolypModel<MorphlingPolypEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/morphling_polyp/model_morphling_polyp.png");

	public MorphlingPolypRenderer(Context renderManagerIn) {
		super(renderManagerIn, new MorphlingPolypModel<MorphlingPolypEntity>(renderManagerIn.bakeLayer(MorphlingPolypModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(MorphlingPolypEntity entity) {
		return TEXTURE;

	}

}
