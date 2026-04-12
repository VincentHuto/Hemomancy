package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.MyelinBorerModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MyelinBorerRenderer extends MobRenderer<MyelinBorerEntity, MyelinBorerModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/myelin_borer/model_myelin_borer.png");

	public MyelinBorerRenderer(Context renderManagerIn) {
		super(renderManagerIn, new MyelinBorerModel(renderManagerIn.bakeLayer(MyelinBorerModel.LAYER_LOCATION)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(MyelinBorerEntity entity) {
		return TEXTURE;
	}
}
