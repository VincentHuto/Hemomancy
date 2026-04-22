package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.VoidDrinkerModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.VoidDrinkerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VoidDrinkerRenderer extends MobRenderer<VoidDrinkerEntity, VoidDrinkerModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/void_drinker/model_void_drinker.png");

	public VoidDrinkerRenderer(Context renderManagerIn) {
		super(renderManagerIn, new VoidDrinkerModel(renderManagerIn.bakeLayer(VoidDrinkerModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(VoidDrinkerEntity entity) {
		return TEXTURE;
	}
}
