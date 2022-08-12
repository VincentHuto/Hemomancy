package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.FargoneEntity;
import com.vincenthuto.hemomancy.model.entity.mob.FargoneModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FargoneRenderer extends MobRenderer<FargoneEntity, FargoneModel<FargoneEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fargone/model_fargone.png");

	public FargoneRenderer(Context renderManagerIn) {
		super(renderManagerIn, new FargoneModel<FargoneEntity>(renderManagerIn.bakeLayer(FargoneModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(FargoneEntity entity) {
		return TEXTURE;

	}

}
