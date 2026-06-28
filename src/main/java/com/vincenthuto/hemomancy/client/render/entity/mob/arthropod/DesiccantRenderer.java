package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.DesiccantModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.DesiccantEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DesiccantRenderer extends MobRenderer<DesiccantEntity, DesiccantModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/desiccant/model_desiccant.png");

	public DesiccantRenderer(Context renderManagerIn) {
		super(renderManagerIn, new DesiccantModel(renderManagerIn.bakeLayer(DesiccantModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(DesiccantEntity entity) {
		return TEXTURE;
	}
}
