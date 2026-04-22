package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.FargoneModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FargoneRenderer extends MobRenderer<FargoneEntity, FargoneModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/fargone/model_fargone.png");

	public FargoneRenderer(Context renderManagerIn) {
		super(renderManagerIn, new FargoneModel(renderManagerIn.bakeLayer(FargoneModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(FargoneEntity entity) {
		return TEXTURE;

	}

}
