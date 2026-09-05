package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.PeacockSpiderModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class PeacockSpiderRenderer extends MobRenderer<PeacockSpiderEntity, PeacockSpiderModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/peacock_spider/model_peacock_spider.png");

	public PeacockSpiderRenderer(EntityRendererProvider.Context context) {
		super(context, new PeacockSpiderModel(context.bakeLayer(PeacockSpiderModel.LAYER_LOCATION)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(PeacockSpiderEntity entity) {
		return TEXTURE;
	}
}
