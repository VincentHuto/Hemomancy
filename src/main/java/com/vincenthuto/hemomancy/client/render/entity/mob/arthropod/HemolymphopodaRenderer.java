package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.HemolymphopodaEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HemolymphopodaRenderer
		extends MobRenderer<HemolymphopodaEntity, HemolymphopodaModel<HemolymphopodaEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/hemolymphopoda/model_hemolymphopoda.png");

	public HemolymphopodaRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new HemolymphopodaModel<HemolymphopodaEntity>(
						renderManagerIn.bakeLayer(HemolymphopodaModel.LAYER_LOCATION)),
				0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HemolymphopodaEntity entity) {
		return TEXTURE;
	}
}
