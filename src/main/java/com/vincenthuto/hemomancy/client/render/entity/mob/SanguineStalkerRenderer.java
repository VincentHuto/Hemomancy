package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.SanguineStalkerModel;
import com.vincenthuto.hemomancy.common.entity.mob.SanguineStalkerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SanguineStalkerRenderer extends MobRenderer<SanguineStalkerEntity, SanguineStalkerModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/sanguine_stalker/model_sanguine_stalker.png");

	public SanguineStalkerRenderer(Context renderManagerIn) {
		super(renderManagerIn, new SanguineStalkerModel(renderManagerIn.bakeLayer(SanguineStalkerModel.LAYER_LOCATION)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(SanguineStalkerEntity entity) {
		return TEXTURE;
	}
}
