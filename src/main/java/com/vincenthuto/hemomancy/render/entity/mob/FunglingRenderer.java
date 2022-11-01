package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.FunglingEntity;
import com.vincenthuto.hemomancy.model.entity.mob.FunglingModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FunglingRenderer extends MobRenderer<FunglingEntity, FunglingModel<FunglingEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fungling/model_fungling.png");

	public FunglingRenderer(Context renderManagerIn) {
		super(renderManagerIn, new FunglingModel<FunglingEntity>(renderManagerIn.bakeLayer(FunglingModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(FunglingEntity entity) {
		return TEXTURE;

	}

}
