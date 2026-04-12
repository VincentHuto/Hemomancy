package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.AbyssalSiphonModel;
import com.vincenthuto.hemomancy.common.entity.mob.AbyssalSiphonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbyssalSiphonRenderer extends MobRenderer<AbyssalSiphonEntity, AbyssalSiphonModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/abyssal_siphon/model_abyssal_siphon.png");

	public AbyssalSiphonRenderer(Context renderManagerIn) {
		super(renderManagerIn, new AbyssalSiphonModel(renderManagerIn.bakeLayer(AbyssalSiphonModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(AbyssalSiphonEntity entity) {
		return TEXTURE;
	}
}
