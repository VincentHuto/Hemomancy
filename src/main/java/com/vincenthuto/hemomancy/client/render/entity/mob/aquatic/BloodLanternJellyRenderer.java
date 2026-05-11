package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BloodLanternJellyModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BloodLanternJellyEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class BloodLanternJellyRenderer extends MobRenderer<BloodLanternJellyEntity, BloodLanternJellyModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/blood_lantern_jelly/model_blood_lantern_jelly.png");

	public BloodLanternJellyRenderer(Context context) {
		super(context, new BloodLanternJellyModel(context.bakeLayer(BloodLanternJellyModel.LAYER_LOCATION)), 0.25F);
	}

	@Override
	public ResourceLocation getTextureLocation(BloodLanternJellyEntity entity) {
		return TEXTURE;
	}

	@Nullable
	@Override
	protected RenderType getRenderType(BloodLanternJellyEntity entity, boolean bodyVisible, boolean translucent,
			boolean glowing) {
		return RenderType.entityTranslucent(this.getTextureLocation(entity));
	}

	@Override
	protected int getBlockLightLevel(BloodLanternJellyEntity entity, BlockPos pos) {
		return Math.max(10, super.getBlockLightLevel(entity, pos));
	}
}
