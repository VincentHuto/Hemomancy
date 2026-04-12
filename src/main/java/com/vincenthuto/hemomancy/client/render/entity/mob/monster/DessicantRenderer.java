package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.DessicantModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.DessicantEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DessicantRenderer extends MobRenderer<DessicantEntity, DessicantModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/dessicant/model_dessicant.png");

	public DessicantRenderer(Context renderManagerIn) {
		super(renderManagerIn, new DessicantModel(renderManagerIn.bakeLayer(DessicantModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(DessicantEntity entity) {
		return TEXTURE;
	}
}
