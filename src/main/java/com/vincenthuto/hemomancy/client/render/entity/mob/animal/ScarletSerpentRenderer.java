package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ScarletSerpentModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentVariant;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ScarletSerpentRenderer extends MobRenderer<ScarletSerpentEntity, ScarletSerpentModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent.png");
	private static final ResourceLocation DESERT_TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent_desert.png");
	private static final ResourceLocation SWAMP_TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent_swamp.png");

	public ScarletSerpentRenderer(Context context) {
		super(context, new ScarletSerpentModel(context.bakeLayer(ScarletSerpentModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(ScarletSerpentEntity entity) {
		ScarletSerpentVariant variant = entity.getVariant();
		if (variant == ScarletSerpentVariant.DESERT_RED) {
			return DESERT_TEXTURE;
		}
		if (variant == ScarletSerpentVariant.SWAMP_BROWN) {
			return SWAMP_TEXTURE;
		}
		return TEXTURE;
	}
}
