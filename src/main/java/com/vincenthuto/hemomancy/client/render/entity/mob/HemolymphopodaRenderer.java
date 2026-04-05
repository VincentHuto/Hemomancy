package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.HemolymphopodaModel;
import com.vincenthuto.hemomancy.common.entity.mob.HemolymphopodaEntity;

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
				0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(HemolymphopodaEntity entity) {
		return TEXTURE;
	}
}
