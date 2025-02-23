package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.LeechModel;
import com.vincenthuto.hemomancy.common.entity.mob.LeechEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LeechRenderer extends MobRenderer<LeechEntity, LeechModel<LeechEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/denizen/model_denizen.png");

	public LeechRenderer(Context renderManagerIn) {
		super(renderManagerIn, new LeechModel<LeechEntity>(renderManagerIn.bakeLayer(LeechModel.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(LeechEntity entity) {
		return entity.getLeechTypeName();

	}

}
