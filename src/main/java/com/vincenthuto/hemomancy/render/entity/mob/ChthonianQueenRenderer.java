package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.ChthonianQueenEntity;
import com.vincenthuto.hemomancy.model.entity.mob.ChthonianQueenModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChthonianQueenRenderer extends MobRenderer<ChthonianQueenEntity, ChthonianQueenModel<ChthonianQueenEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian_queen/model_chthonian_queen.png");

	public ChthonianQueenRenderer(Context renderManagerIn) {
		super(renderManagerIn, new ChthonianQueenModel<ChthonianQueenEntity>(renderManagerIn.bakeLayer(ChthonianQueenModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(ChthonianQueenEntity entity) {
		return TEXTURE;

	}

}