package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.FunglingModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FunglingRenderer extends MobRenderer<FunglingEntity, FunglingModel<FunglingEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/fungling/model_fungling.png");

	public FunglingRenderer(Context renderManagerIn) {
		super(renderManagerIn, new FunglingModel<FunglingEntity>(renderManagerIn.bakeLayer(FunglingModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(FunglingEntity entity) {
		return TEXTURE;

	}

}
