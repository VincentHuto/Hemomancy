package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PrismCuttleModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleVariant;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PrismCuttleRenderer extends MobRenderer<PrismCuttleEntity, PrismCuttleModel> {
	private static final ResourceLocation DEEP_TEXTURE = Hemomancy.rloc(
			"textures/entity/prism_cuttle/model_prism_cuttle_deep.png");
	private static final ResourceLocation SAND_TEXTURE = Hemomancy.rloc(
			"textures/entity/prism_cuttle/model_prism_cuttle_sand.png");
	private static final ResourceLocation CORAL_TEXTURE = Hemomancy.rloc(
			"textures/entity/prism_cuttle/model_prism_cuttle_coral.png");
	private static final ResourceLocation KELP_TEXTURE = Hemomancy.rloc(
			"textures/entity/prism_cuttle/model_prism_cuttle_kelp.png");
	private static final ResourceLocation FLASH_TEXTURE = Hemomancy.rloc(
			"textures/entity/prism_cuttle/model_prism_cuttle_flash.png");

	public PrismCuttleRenderer(Context context) {
		super(context, new PrismCuttleModel(context.bakeLayer(PrismCuttleModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(PrismCuttleEntity entity) {
		if (entity.isFlashing()) {
			return FLASH_TEXTURE;
		}
		PrismCuttleVariant variant = entity.getVariant();
		if (variant == PrismCuttleVariant.SAND) {
			return SAND_TEXTURE;
		}
		if (variant == PrismCuttleVariant.CORAL) {
			return CORAL_TEXTURE;
		}
		if (variant == PrismCuttleVariant.KELP) {
			return KELP_TEXTURE;
		}
		return DEEP_TEXTURE;
	}
}
