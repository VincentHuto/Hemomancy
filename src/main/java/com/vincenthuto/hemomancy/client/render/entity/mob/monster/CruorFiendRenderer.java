package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.CruorFiendModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.CruorFiendEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CruorFiendRenderer extends MobRenderer<CruorFiendEntity, CruorFiendModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/cruor_fiend/model_cruor_fiend.png");

	public CruorFiendRenderer(Context renderManagerIn) {
		super(renderManagerIn, new CruorFiendModel(renderManagerIn.bakeLayer(CruorFiendModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(CruorFiendEntity entity) {
		return TEXTURE;
	}
}
