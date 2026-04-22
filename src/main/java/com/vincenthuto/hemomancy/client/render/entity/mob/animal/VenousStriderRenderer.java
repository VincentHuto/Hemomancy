package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.VenousStriderModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VenousStriderRenderer extends MobRenderer<VenousStriderEntity, VenousStriderModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/venous_strider/model_venous_strider.png");

	public VenousStriderRenderer(Context renderManagerIn) {
		super(renderManagerIn, new VenousStriderModel(renderManagerIn.bakeLayer(VenousStriderModel.LAYER_LOCATION)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(VenousStriderEntity entity) {
		return TEXTURE;
	}
}
